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

import org.minidns.dnslabel.DnsLabel;
import org.minidns.dnsmessage.Question;
import org.minidns.dnsname.DnsName;
import org.minidns.dnssec.DnssecUnverifiedReason.AlgorithmExceptionThrownReason;
import org.minidns.dnssec.DnssecUnverifiedReason.AlgorithmNotSupportedReason;
import org.minidns.dnssec.DnssecUnverifiedReason.NSECDoesNotMatchReason;
import org.minidns.dnssec.algorithms.AlgorithmMap;
import org.minidns.record.DNSKEY;
import org.minidns.record.Data;
import org.minidns.record.DelegatingDnssecRR;
import org.minidns.record.NSEC;
import org.minidns.record.NSEC3;
import org.minidns.record.RRSIG;
import org.minidns.record.Record;
import org.minidns.util.Base32;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class Verifier {
    private static final AlgorithmMap algorithmMap = AlgorithmMap.INSTANCE;

    public static DnssecUnverifiedReason verify(Record<DNSKEY> dnskeyRecord, DelegatingDnssecRR ds) throws DnssecValidationFailedException {
        DNSKEY dnskey = dnskeyRecord.payloadData;
        DigestCalculator digestCalculator = algorithmMap.getDsDigestCalculator(ds.digestType);
        if (digestCalculator == null) {
            return new AlgorithmNotSupportedReason(ds.digestTypeByte, ds.getType(), dnskeyRecord);
        }

        byte[] dnskeyData = dnskey.toByteArray();
        byte[] dnskeyOwner = dnskeyRecord.name.getBytes();
        byte[] combined = new byte[dnskeyOwner.length + dnskeyData.length];
        System.arraycopy(dnskeyOwner, 0, combined, 0, dnskeyOwner.length);
        System.arraycopy(dnskeyData, 0, combined, dnskeyOwner.length, dnskeyData.length);
        byte[] digest;
        try {
            digest = digestCalculator.digest(combined);
        } catch (Exception e) {
            return new AlgorithmExceptionThrownReason(ds.digestType, "DS", dnskeyRecord, e);
        }

        if (!ds.digestEquals(digest)) {
            // TODO: Add 'ds' and 'digest' to this exception, and rename the exception to "DigestComparisionFailedException".
            throw new DnssecValidationFailedException(dnskeyRecord, "SEP is not properly signed by parent DS!");
        }
        return null;
    }

    public static DnssecUnverifiedReason verify(List<Record<? extends Data>> records, RRSIG rrsig, DNSKEY key) throws IOException {
        SignatureVerifier signatureVerifier = algorithmMap.getSignatureVerifier(rrsig.algorithm);
        if (signatureVerifier == null) {
            return new AlgorithmNotSupportedReason(rrsig.algorithmByte, rrsig.getType(), records.get(0));
        }

        byte[] combine = combine(rrsig, records);
        if (signatureVerifier.verify(combine, rrsig, key)) {
            return null;
        } else {
            throw new DnssecValidationFailedException(records, "Signature is invalid.");
        }
    }

    public static DnssecUnverifiedReason verifyNsec(Record<NSEC> nsecRecord, Question q) {
        NSEC nsec = nsecRecord.payloadData;
        if (nsecRecord.name.equals(q.name) && !nsec.types.contains(q.type)) {
            // records with same name but different types exist
            return null;
        } else if (nsecMatches(q.name, nsecRecord.name, nsec.next)) {
            return null;
        }
        return new NSECDoesNotMatchReason(q, nsecRecord);
    }

    public static DnssecUnverifiedReason verifyNsec3(DnsName zone, Record<NSEC3> nsec3record, Question q) {
        NSEC3 nsec3 = nsec3record.payloadData;
        DigestCalculator digestCalculator = algorithmMap.getNsecDigestCalculator(nsec3.hashAlgorithm);
        if (digestCalculator == null) {
            return new AlgorithmNotSupportedReason(nsec3.hashAlgorithmByte, nsec3.getType(), nsec3record);
        }

        byte[] bytes = nsec3hash(digestCalculator, nsec3, q.name, nsec3.iterations);
        String s = Base32.encodeToString(bytes);
        DnsName computedNsec3Record = DnsName.from(s + "." + zone);
        if (nsec3record.name.equals(computedNsec3Record)) {
            if (nsec3.types.contains(q.type)) {
                // TODO: Refine exception thrown in this case.
                return new NSECDoesNotMatchReason(q, nsec3record);
            }
            return null;
        }
        if (nsecMatches(s, nsec3record.name.getHostpart(), Base32.encodeToString(nsec3.getNextHashed()))) {
            return null;
        }
        return new NSECDoesNotMatchReason(q, nsec3record);
    }

    static byte[] combine(RRSIG rrsig, List<Record<? extends Data>> records) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        // Write RRSIG without signature
        try {
            rrsig.writePartialSignature(dos);

            DnsName sigName = records.get(0).name;
            if (!sigName.isRootLabel()) {
                if (sigName.getLabelCount() < rrsig.labels) {
                    // TODO: This is currently not covered by the unit tests.
                    throw new DnssecValidationFailedException("Invalid RRsig record");
                }

                if (sigName.getLabelCount() > rrsig.labels) {
                    // TODO: This is currently not covered by the unit tests.
                    // Expand wildcards
                    sigName = DnsName.from(DnsLabel.WILDCARD_LABEL, sigName.stripToLabels(rrsig.labels));
                }
            }

            List<byte[]> recordBytes = new ArrayList<>(records.size());
            for (Record<? extends Data> record : records) {
                Record<Data> ref = new Record<Data>(sigName, record.type, record.clazzValue, rrsig.originalTtl, record.payloadData);
                recordBytes.add(ref.toByteArray());
            }

            // Sort correctly (cause they might be ordered randomly) as per RFC 4034 § 6.3.
            final int offset = sigName.size() + 10; // Where the RDATA begins
            Collections.sort(recordBytes, new Comparator<byte[]>() {
                @Override
                public int compare(byte[] b1, byte[] b2) {
                    for (int i = offset; i < b1.length && i < b2.length; i++) {
                        if (b1[i] != b2[i]) {
                            return (b1[i] & 0xFF) - (b2[i] & 0xFF);
                        }
                    }
                    return b1.length - b2.length;
                }
            });

            for (byte[] recordByte : recordBytes) {
                dos.write(recordByte);
            }
            dos.flush();
        } catch (IOException e) {
            // Never happens
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    static boolean nsecMatches(String test, String lowerBound, String upperBound) {
        return nsecMatches(DnsName.from(test), DnsName.from(lowerBound), DnsName.from(upperBound));
    }

    /**
     * Tests if a nsec domain name is part of an NSEC record.
     *
     * @param test       test domain name
     * @param lowerBound inclusive lower bound
     * @param upperBound exclusive upper bound
     * @return test domain name is covered by NSEC record
     */
    static boolean nsecMatches(DnsName test, DnsName lowerBound, DnsName upperBound) {
        int lowerParts = lowerBound.getLabelCount();
        int upperParts = upperBound.getLabelCount();
        int testParts = test.getLabelCount();

        if (testParts > lowerParts && !test.isChildOf(lowerBound) && test.stripToLabels(lowerParts).compareTo(lowerBound) < 0)
            return false;
        if (testParts <= lowerParts && test.compareTo(lowerBound.stripToLabels(testParts)) < 0)
            return false;

        if (testParts > upperParts && !test.isChildOf(upperBound) && test.stripToLabels(upperParts).compareTo(upperBound) > 0)
            return false;
        if (testParts <= upperParts && test.compareTo(upperBound.stripToLabels(testParts)) >= 0)
            return false;

        return true;
    }

    static byte[] nsec3hash(DigestCalculator digestCalculator, NSEC3 nsec3, DnsName ownerName, int iterations) {
        return nsec3hash(digestCalculator, nsec3.getSalt(), ownerName.getBytes(), iterations);
    }

    /**
     * Derived from RFC 5155 Section 5.
     *
     * @param digestCalculator the digest calculator.
     * @param salt the salt.
     * @param data the data.
     * @param iterations the number of iterations.
     * @return the NSEC3 hash.
     */
    static byte[] nsec3hash(DigestCalculator digestCalculator, byte[] salt, byte[] data, int iterations) {
        while (iterations-- >= 0) {
            byte[] combined = new byte[data.length + salt.length];
            System.arraycopy(data, 0, combined, 0, data.length);
            System.arraycopy(salt, 0, combined, data.length, salt.length);
            data = digestCalculator.digest(combined);
        }
        return data;
    }
}
