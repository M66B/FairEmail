/*
 * Copyright 2015-2020 the original author or authors
 *
 * This software is licensed under the Apache License, Version 2.0,
 * the GNU Lesser General Public License version 2 or later ("LGPL")
 * and the WTFPL.
 * You may choose either license to govern your use of this software only
 * upon the condition that you accept all of the terms of either
 * the Apache License 2.0, the LGPL 2.1+ or the WTFPL.
 */
package org.minidns.dnssec;

import org.minidns.DnsCache;
import org.minidns.dnsmessage.DnsMessage;
import org.minidns.dnsmessage.Question;
import org.minidns.dnsname.DnsName;
import org.minidns.dnsqueryresult.DnsQueryResult;
import org.minidns.dnssec.DnssecUnverifiedReason.NoActiveSignaturesReason;
import org.minidns.dnssec.DnssecUnverifiedReason.NoSecureEntryPointReason;
import org.minidns.dnssec.DnssecUnverifiedReason.NoSignaturesReason;
import org.minidns.dnssec.DnssecUnverifiedReason.NoTrustAnchorReason;
import org.minidns.dnssec.DnssecValidationFailedException.AuthorityDoesNotContainSoa;
import org.minidns.iterative.ReliableDnsClient;
import org.minidns.record.DLV;
import org.minidns.record.DNSKEY;
import org.minidns.record.DS;
import org.minidns.record.Data;
import org.minidns.record.DelegatingDnssecRR;
import org.minidns.record.NSEC;
import org.minidns.record.NSEC3;
import org.minidns.record.RRSIG;
import org.minidns.record.Record;
import org.minidns.record.Record.CLASS;
import org.minidns.record.Record.TYPE;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DnssecClient extends ReliableDnsClient {

    /**
     * The root zone's KSK.
     * The ID of the current key is "Klajeyz", and the key tag value is "20326".
     */
    private static final BigInteger rootEntryKey = new BigInteger("1628686155461064465348252249725010996177649738666492500572664444461532807739744536029771810659241049343994038053541290419968870563183856865780916376571550372513476957870843322273120879361960335192976656756972171258658400305760429696147778001233984421619267530978084631948434496468785021389956803104620471232008587410372348519229650742022804219634190734272506220018657920136902014393834092648785514548876370028925405557661759399901378816916683122474038734912535425670533237815676134840739565610963796427401855723026687073600445461090736240030247906095053875491225879656640052743394090544036297390104110989318819106653199917493");

    private static final DnsName DEFAULT_DLV = DnsName.from("dlv.isc.org");

    /**
     * Create a new DNSSEC aware DNS client using the global default cache.
     */
    public DnssecClient() {
        this(DEFAULT_CACHE);
    }

    /**
     * Create a new DNSSEC aware DNS client with the given DNS cache.
     *
     * @param cache The backend DNS cache.
     */
    public DnssecClient(DnsCache cache) {
        super(cache);
        addSecureEntryPoint(DnsName.ROOT, rootEntryKey.toByteArray());
    }

    /**
     * Known secure entry points (SEPs).
     */
    private final Map<DnsName, byte[]> knownSeps = new ConcurrentHashMap<>();

    private boolean stripSignatureRecords = true;

    /**
     * The active DNSSEC Look-aside Validation Registry. May be <code>null</code>.
     */
    private DnsName dlv;

    @Override
    public DnsQueryResult query(Question q) throws IOException {
        DnssecQueryResult dnssecQueryResult =  queryDnssec(q);
        if (!dnssecQueryResult.isAuthenticData()) {
            // TODO: Refine exception.
            throw new IOException();
        }
        return dnssecQueryResult.dnsQueryResult;
    }

    public DnssecQueryResult queryDnssec(CharSequence name, TYPE type) throws IOException {
        Question q = new Question(name, type, CLASS.IN);
        return queryDnssec(q);
    }

    public DnssecQueryResult queryDnssec(Question q) throws IOException {
        DnsQueryResult dnsQueryResult = super.query(q);
        DnssecQueryResult dnssecQueryResult = performVerification(dnsQueryResult);
        return dnssecQueryResult;
    }

    private DnssecQueryResult performVerification(DnsQueryResult dnsQueryResult) throws IOException {
        if (dnsQueryResult == null) return null;

        DnsMessage dnsMessage = dnsQueryResult.response;
        DnsMessage.Builder messageBuilder = dnsMessage.asBuilder();

        Set<DnssecUnverifiedReason> unverifiedReasons = verify(dnsMessage);

        messageBuilder.setAuthenticData(unverifiedReasons.isEmpty());

        List<Record<? extends Data>> answers = dnsMessage.answerSection;
        List<Record<? extends Data>> nameserverRecords = dnsMessage.authoritySection;
        List<Record<? extends Data>> additionalResourceRecords = dnsMessage.additionalSection;
        Set<Record<RRSIG>> signatures = new HashSet<>();
        Record.filter(signatures, RRSIG.class, answers);
        Record.filter(signatures, RRSIG.class, nameserverRecords);
        Record.filter(signatures, RRSIG.class, additionalResourceRecords);

        if (stripSignatureRecords) {
            messageBuilder.setAnswers(stripSignatureRecords(answers));
            messageBuilder.setNameserverRecords(stripSignatureRecords(nameserverRecords));
            messageBuilder.setAdditionalResourceRecords(stripSignatureRecords(additionalResourceRecords));
        }

        return new DnssecQueryResult(messageBuilder.build(), dnsQueryResult, signatures, unverifiedReasons);
    }

    private static List<Record<? extends Data>> stripSignatureRecords(List<Record<? extends Data>> records) {
        if (records.isEmpty()) return records;
        List<Record<? extends Data>> recordList = new ArrayList<>(records.size());
        for (Record<? extends Data> record : records) {
            if (record.type != TYPE.RRSIG) {
                recordList.add(record);
            }
        }
        return recordList;
    }

    private Set<DnssecUnverifiedReason> verify(DnsMessage dnsMessage) throws IOException {
        if (!dnsMessage.answerSection.isEmpty()) {
            return verifyAnswer(dnsMessage);
        } else {
            return verifyNsec(dnsMessage);
        }
    }

    private Set<DnssecUnverifiedReason> verifyAnswer(DnsMessage dnsMessage) throws IOException {
        Question q = dnsMessage.questions.get(0);
        List<Record<? extends Data>> answers = dnsMessage.answerSection;
        List<Record<? extends Data>> toBeVerified = dnsMessage.copyAnswers();
        VerifySignaturesResult verifiedSignatures = verifySignatures(q, answers, toBeVerified);
        Set<DnssecUnverifiedReason> result = verifiedSignatures.reasons;
        if (!result.isEmpty()) {
            return result;
        }

        // Keep SEPs separated, we only need one valid SEP.
        boolean sepSignatureValid = false;
        Set<DnssecUnverifiedReason> sepReasons = new HashSet<>();
        for (Iterator<Record<? extends Data>> iterator = toBeVerified.iterator(); iterator.hasNext(); ) {
            Record<DNSKEY> record = iterator.next().ifPossibleAs(DNSKEY.class);
            if (record == null) {
                continue;
            }

            // Verify all DNSKEYs as if it was a SEP. If we find a single SEP we are safe.
            Set<DnssecUnverifiedReason> reasons = verifySecureEntryPoint(record);
            if (reasons.isEmpty()) {
                sepSignatureValid = true;
            } else {
                sepReasons.addAll(reasons);
            }
            if (!verifiedSignatures.sepSignaturePresent) {
                LOGGER.finer("SEP key is not self-signed.");
            }
            iterator.remove();
        }

        if (verifiedSignatures.sepSignaturePresent && !sepSignatureValid) {
            result.addAll(sepReasons);
        }
        if (verifiedSignatures.sepSignatureRequired && !verifiedSignatures.sepSignaturePresent) {
            result.add(new NoSecureEntryPointReason(q.name));
        }
        if (!toBeVerified.isEmpty()) {
            if (toBeVerified.size() != answers.size()) {
                throw new DnssecValidationFailedException(q, "Only some records are signed!");
            } else {
                result.add(new NoSignaturesReason(q));
            }
        }
        return result;
    }

    private Set<DnssecUnverifiedReason> verifyNsec(DnsMessage dnsMessage) throws IOException {
        Set<DnssecUnverifiedReason> result = new HashSet<>();
        Question q = dnsMessage.questions.get(0);
        boolean validNsec = false;
        boolean nsecPresent = false;

        // Get the SOA RR that has to be in the authority section. Note that we will verify its signature later, after
        // we have verified the NSEC3 RR. And although the data form the SOA RR is only required for NSEC3 we check for
        // its existence here, since it would be invalid if there is none.
        // TODO: Add a reference to the relevant RFC parts which specify that there has to be a SOA RR in X.
        DnsName zone = null;
        List<Record<? extends Data>> authoritySection = dnsMessage.authoritySection;
        for (Record<? extends Data> authorityRecord : authoritySection) {
            if (authorityRecord.type == TYPE.SOA) {
                zone = authorityRecord.name;
                break;
            }
        }
        if (zone == null)
            throw new AuthorityDoesNotContainSoa(dnsMessage);

        // TODO Examine if it is better to verify the RRs in the authority section *before* we verify NSEC(3). We
        // currently do it the other way around.

        // TODO: This whole logic needs to be changed. It currently checks one NSEC(3) record after another, when it
        // should first determine if we are dealing with NSEC or NSEC3 and the verify the whole response.
        for (Record<? extends Data> record : authoritySection) {
            DnssecUnverifiedReason reason;

            switch (record.type) {
            case NSEC:
                nsecPresent = true;
                Record<NSEC> nsecRecord = record.as(NSEC.class);
                reason = Verifier.verifyNsec(nsecRecord, q);
                break;
            case NSEC3:
                nsecPresent = true;
                Record<NSEC3> nsec3Record = record.as(NSEC3.class);
                reason = Verifier.verifyNsec3(zone, nsec3Record, q);
                break;
            default:
                continue;
            }

            if (reason != null) {
                result.add(reason);
            } else {
                validNsec = true;
            }
        }

        // TODO: Shouldn't we also throw if !nsecPresent?
        if (nsecPresent && !validNsec) {
            throw new DnssecValidationFailedException(q, "Invalid NSEC!");
        }

        List<Record<? extends Data>> toBeVerified = dnsMessage.copyAuthority();
        VerifySignaturesResult verifiedSignatures = verifySignatures(q, authoritySection, toBeVerified);
        if (validNsec && verifiedSignatures.reasons.isEmpty()) {
            result.clear();
        } else {
            result.addAll(verifiedSignatures.reasons);
        }

        if (!toBeVerified.isEmpty() && toBeVerified.size() != authoritySection.size()) {
            // TODO Refine this exception and include the missing toBeVerified RRs and the whole DnsMessage into it.
            throw new DnssecValidationFailedException(q, "Only some resource records from the authority section are signed!");
        }

        return result;
    }

    private static class VerifySignaturesResult {
        boolean sepSignatureRequired = false;
        boolean sepSignaturePresent = false;
        Set<DnssecUnverifiedReason> reasons = new HashSet<>();
    }

    private VerifySignaturesResult verifySignatures(Question q, Collection<Record<? extends Data>> reference, List<Record<? extends Data>> toBeVerified) throws IOException {
        final Date now = new Date();
        final List<RRSIG> outdatedRrSigs = new LinkedList<>();
        VerifySignaturesResult result = new VerifySignaturesResult();
        final List<Record<RRSIG>> rrsigs = new ArrayList<>(toBeVerified.size());

        for (Record<? extends Data> recordToBeVerified : toBeVerified) {
            Record<RRSIG> record = recordToBeVerified.ifPossibleAs(RRSIG.class);
            if (record == null) continue;

            RRSIG rrsig = record.payloadData;
            if (rrsig.signatureExpiration.compareTo(now) < 0 || rrsig.signatureInception.compareTo(now) > 0) {
                // This RRSIG is out of date, but there might be one that is not.
                outdatedRrSigs.add(rrsig);
                continue;
            }
            rrsigs.add(record);
        }

        if (rrsigs.isEmpty()) {
            if (!outdatedRrSigs.isEmpty()) {
                result.reasons.add(new NoActiveSignaturesReason(q, outdatedRrSigs));
            } else {
                // TODO: Check if QNAME results should have signatures and add a different reason if there are RRSIGs
                // expected compared to when not.
                result.reasons.add(new NoSignaturesReason(q));
            }
            return result;
        }

        for (Record<RRSIG> sigRecord : rrsigs) {
            RRSIG rrsig = sigRecord.payloadData;

            List<Record<? extends Data>> records = new ArrayList<>(reference.size());
            for (Record<? extends Data> record : reference) {
                if (record.type == rrsig.typeCovered && record.name.equals(sigRecord.name)) {
                    records.add(record);
                }
            }

            Set<DnssecUnverifiedReason> reasons = verifySignedRecords(q, rrsig, records);
            result.reasons.addAll(reasons);

            if (q.name.equals(rrsig.signerName) && rrsig.typeCovered == TYPE.DNSKEY) {
                for (Iterator<Record<? extends Data>> iterator = records.iterator(); iterator.hasNext(); ) {
                    Record<DNSKEY> dnsKeyRecord = iterator.next().ifPossibleAs(DNSKEY.class);
                    // dnsKeyRecord should never be null here.
                    DNSKEY dnskey = dnsKeyRecord.payloadData;
                    // DNSKEYs are verified separately, so don't mark them verified now.
                    iterator.remove();
                    if (dnskey.getKeyTag() == rrsig.keyTag) {
                        result.sepSignaturePresent = true;
                    }
                }
                // DNSKEY's should be signed by a SEP
                result.sepSignatureRequired = true;
            }

            if (!isParentOrSelf(sigRecord.name.ace, rrsig.signerName.ace)) {
                LOGGER.finer("Records at " + sigRecord.name + " are cross-signed with a key from " + rrsig.signerName);
            } else {
                toBeVerified.removeAll(records);
            }
            toBeVerified.remove(sigRecord);
        }
        return result;
    }

    private static boolean isParentOrSelf(String child, String parent) {
        if (child.equals(parent)) return true;
        if (parent.isEmpty()) return true;
        String[] childSplit = child.split("\\.");
        String[] parentSplit = parent.split("\\.");
        if (parentSplit.length > childSplit.length) return false;
        for (int i = 1; i <= parentSplit.length; i++) {
            if (!parentSplit[parentSplit.length - i].equals(childSplit[childSplit.length - i])) {
                return false;
            }
        }
        return true;
    }

    private Set<DnssecUnverifiedReason> verifySignedRecords(Question q, RRSIG rrsig, List<Record<? extends Data>> records) throws IOException {
        Set<DnssecUnverifiedReason> result = new HashSet<>();
        DNSKEY dnskey = null;

        if (rrsig.typeCovered == TYPE.DNSKEY) {
            // Key must be present
            List<Record<DNSKEY>> dnskeyRrs = Record.filter(DNSKEY.class, records);
            for (Record<DNSKEY> dnsKeyRecord : dnskeyRrs) {
                if (dnsKeyRecord.payloadData.getKeyTag() == rrsig.keyTag) {
                    dnskey = dnsKeyRecord.payloadData;
                    break;
                }
            }
        } else if (q.type == TYPE.DS && rrsig.signerName.equals(q.name)) {
            // We should not probe for the self signed DS negative response, as it will be an endless loop.
            result.add(new NoTrustAnchorReason(q.name));
            return result;
        } else {
            DnssecQueryResult dnskeyRes = queryDnssec(rrsig.signerName, TYPE.DNSKEY);
            result.addAll(dnskeyRes.getUnverifiedReasons());
            List<Record<DNSKEY>> dnskeyRrs = dnskeyRes.dnsQueryResult.response.filterAnswerSectionBy(DNSKEY.class);
            for (Record<DNSKEY> dnsKeyRecord : dnskeyRrs) {
                if (dnsKeyRecord.payloadData.getKeyTag() == rrsig.keyTag) {
                    dnskey = dnsKeyRecord.payloadData;
                    break;
                }
            }
        }

        if (dnskey == null) {
            throw new DnssecValidationFailedException(q, records.size() + " " + rrsig.typeCovered + " record(s) are signed using an unknown key.");
        }

        DnssecUnverifiedReason unverifiedReason = Verifier.verify(records, rrsig, dnskey);
        if (unverifiedReason != null) {
            result.add(unverifiedReason);
        }

        return result;
    }

    private Set<DnssecUnverifiedReason> verifySecureEntryPoint(final Record<DNSKEY> sepRecord) throws IOException {
        final DNSKEY dnskey = sepRecord.payloadData;

        Set<DnssecUnverifiedReason> unverifiedReasons = new HashSet<>();
        Set<DnssecUnverifiedReason> activeReasons = new HashSet<>();
        if (knownSeps.containsKey(sepRecord.name)) {
            if (dnskey.keyEquals(knownSeps.get(sepRecord.name))) {
                return unverifiedReasons;
            } else {
                unverifiedReasons.add(new DnssecUnverifiedReason.ConflictsWithSep(sepRecord));
                return unverifiedReasons;
            }
        }

        // If we are looking for the SEP of the root zone at this point, then the client was not
        // configured with one and we can abort stating the reason.
        if (sepRecord.name.isRootLabel()) {
           unverifiedReasons.add(new DnssecUnverifiedReason.NoRootSecureEntryPointReason());
           return unverifiedReasons;
        }

        DelegatingDnssecRR delegation = null;
        DnssecQueryResult dsResp = queryDnssec(sepRecord.name, TYPE.DS);
        unverifiedReasons.addAll(dsResp.getUnverifiedReasons());

        List<Record<DS>> dsRrs = dsResp.dnsQueryResult.response.filterAnswerSectionBy(DS.class);
        for (Record<DS> dsRecord : dsRrs) {
            DS ds = dsRecord.payloadData;
            if (dnskey.getKeyTag() == ds.keyTag) {
                delegation = ds;
                activeReasons = dsResp.getUnverifiedReasons();
                break;
            }
        }

        if (delegation == null) {
            LOGGER.fine("There is no DS record for " + sepRecord.name + ", server gives empty result");
        }

        if (delegation == null && dlv != null && !dlv.isChildOf(sepRecord.name)) {
            DnssecQueryResult dlvResp = queryDnssec(DnsName.from(sepRecord.name, dlv), TYPE.DLV);
            unverifiedReasons.addAll(dlvResp.getUnverifiedReasons());

            List<Record<DLV>> dlvRrs = dlvResp.dnsQueryResult.response.filterAnswerSectionBy(DLV.class);
            for (Record<DLV> dlvRecord : dlvRrs) {
                if (sepRecord.payloadData.getKeyTag() == dlvRecord.payloadData.keyTag) {
                    LOGGER.fine("Found DLV for " + sepRecord.name + ", awesome.");
                    delegation = dlvRecord.payloadData;
                    activeReasons = dlvResp.getUnverifiedReasons();
                    break;
                }
            }
        }

        if (delegation != null) {
            DnssecUnverifiedReason unverifiedReason = Verifier.verify(sepRecord, delegation);
            if (unverifiedReason != null) {
                unverifiedReasons.add(unverifiedReason);
            } else {
                unverifiedReasons = activeReasons;
            }
        } else if (unverifiedReasons.isEmpty()) {
            unverifiedReasons.add(new NoTrustAnchorReason(sepRecord.name));
        }
        return unverifiedReasons;
    }

    @Override
    protected DnsMessage.Builder newQuestion(DnsMessage.Builder message) {
        message.getEdnsBuilder().setUdpPayloadSize(dataSource.getUdpPayloadSize()).setDnssecOk();
        message.setCheckingDisabled(true);
        return super.newQuestion(message);
    }

    @Override
    protected String isResponseAcceptable(DnsMessage response) {
        boolean dnssecOk = response.isDnssecOk();
        if (!dnssecOk) {
            // This is a deliberate violation of RFC 6840 § 5.6. I doubt that
            // "resolvers MUST ignore the DO bit in responses" does any good. Also we basically ignore the DO bit after
            // the fall back to iterative mode.
            return "DNSSEC OK (DO) flag not set in response";
        }
        boolean checkingDisabled = response.checkingDisabled;
        if (!checkingDisabled) {
            return "CHECKING DISABLED (CD) flag not set in response";
        }
        return super.isResponseAcceptable(response);
    }

    /**
     * Add a new secure entry point to the list of known secure entry points.
     *
     * A secure entry point acts as a trust anchor. By default, the only secure entry point is the key signing key
     * provided by the root zone.
     *
     * @param name The domain name originating the key. Once the secure entry point for this domain is requested,
     *             the resolver will use this key without further verification instead of using the DNS system to
     *             verify the key.
     * @param key  The secure entry point corresponding to the domain name. This key can be retrieved by requesting
     *             the DNSKEY record for the domain and using the key with first flags bit set
     *             (also called key signing key)
     */
    public void addSecureEntryPoint(DnsName name, byte[] key) {
        knownSeps.put(name, key);
    }

    /**
     * Remove the secure entry point stored for a domain name.
     *
     * @param name The domain name of which the corresponding secure entry point shall be removed. For the root zone,
     *             use the empty string here.
     */
    public void removeSecureEntryPoint(DnsName name) {
        knownSeps.remove(name);
    }

    /**
     * Clears the list of known secure entry points.
     *
     * This will also remove the secure entry point of the root zone and
     * thus render this instance useless until a new secure entry point is added.
     */
    public void clearSecureEntryPoints() {
        knownSeps.clear();
    }

    /**
     * Whether signature records (RRSIG) are stripped from the resulting {@link DnsMessage}.
     *
     * Default is {@code true}.
     *
     * @return Whether signature records are stripped.
     */
    public boolean isStripSignatureRecords() {
        return stripSignatureRecords;
    }

    /**
     * Enable or disable stripping of signature records (RRSIG) from the result {@link DnsMessage}.
     * @param stripSignatureRecords Whether signature records shall be stripped.
     */
    public void setStripSignatureRecords(boolean stripSignatureRecords) {
        this.stripSignatureRecords = stripSignatureRecords;
    }

    /**
     * Enables DNSSEC Lookaside Validation (DLV) using the default DLV service at dlv.isc.org.
     */
    public void enableLookasideValidation() {
        configureLookasideValidation(DEFAULT_DLV);
    }

    /**
     * Disables DNSSEC Lookaside Validation (DLV).
     * DLV is disabled by default, this is only required if {@link #enableLookasideValidation()} was used before.
     */
    public void disableLookasideValidation() {
        configureLookasideValidation(null);
    }

    /**
     * Enables DNSSEC Lookaside Validation (DLV) using the given DLV service.
     *
     * @param dlv The domain name of the DLV service to be used or {@code null} to disable DLV.
     */
    public void configureLookasideValidation(DnsName dlv) {
        this.dlv = dlv;
    }
}
