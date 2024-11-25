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

import java.util.Collections;
import java.util.List;

import org.minidns.constants.DnssecConstants.DigestAlgorithm;
import org.minidns.dnsmessage.Question;
import org.minidns.dnsname.DnsName;
import org.minidns.record.DNSKEY;
import org.minidns.record.Data;
import org.minidns.record.RRSIG;
import org.minidns.record.Record;
import org.minidns.record.Record.TYPE;

public abstract class DnssecUnverifiedReason {
    public abstract String getReasonString();

    @Override
    public String toString() {
        return getReasonString();
    }

    @Override
    public int hashCode() {
        return getReasonString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DnssecUnverifiedReason && ((DnssecUnverifiedReason) obj).getReasonString().equals(getReasonString());
    }

    public static class AlgorithmNotSupportedReason extends DnssecUnverifiedReason {
        private final String algorithm;
        private final TYPE type;
        private final Record<? extends Data> record;

        public AlgorithmNotSupportedReason(byte algorithm, TYPE type, Record<? extends Data> record) {
            this.algorithm = Integer.toString(algorithm & 0xff);
            this.type = type;
            this.record = record;
        }

        @Override
        public String getReasonString() {
            return type.name() + " algorithm " + algorithm + " required to verify " + record.name + " is unknown or not supported by platform";
        }
    }

    public static class AlgorithmExceptionThrownReason extends DnssecUnverifiedReason {
        private final int algorithmNumber;
        private final String kind;
        private final Exception reason;
        private final Record<? extends Data> record;

        public AlgorithmExceptionThrownReason(DigestAlgorithm algorithm, String kind, Record<? extends Data> record, Exception reason) {
            this.algorithmNumber = algorithm.value;
            this.kind = kind;
            this.record = record;
            this.reason = reason;
        }

        @Override
        public String getReasonString() {
            return kind + " algorithm " + algorithmNumber + " threw exception while verifying " + record.name + ": " + reason;
        }
    }

    public static class ConflictsWithSep extends DnssecUnverifiedReason {
        private final Record<DNSKEY> record;

        public ConflictsWithSep(Record<DNSKEY> record) {
            this.record = record;
        }

        @Override
        public String getReasonString() {
            return "Zone " + record.name.ace + " is in list of known SEPs, but DNSKEY from response mismatches!";
        }
    }

    public static class NoTrustAnchorReason extends DnssecUnverifiedReason {
        private final DnsName zone;

        public NoTrustAnchorReason(DnsName zone) {
            this.zone = zone;
        }

        @Override
        public String getReasonString() {
            return "No trust anchor was found for zone " + zone + ". Try enabling DLV";
        }
    }

    public static class NoSecureEntryPointReason extends DnssecUnverifiedReason {
        private final DnsName zone;

        public NoSecureEntryPointReason(DnsName zone) {
            this.zone = zone;
        }

        @Override
        public String getReasonString() {
            return "No secure entry point was found for zone " + zone;
        }
    }

    public static class NoRootSecureEntryPointReason extends DnssecUnverifiedReason {
        public NoRootSecureEntryPointReason() {
        }

        @Override
        public String getReasonString() {
            return "No secure entry point was found for the root zone (\"Did you forget to configure a root SEP?\")";
        }
    }

    public static class NoSignaturesReason extends DnssecUnverifiedReason {
        private final Question question;

        public NoSignaturesReason(Question question) {
            this.question = question;
        }

        @Override
        public String getReasonString() {
            return "No signatures were attached to answer on question for " + question.type + " at " + question.name;
        }
    }

    public static class NoActiveSignaturesReason extends DnssecUnverifiedReason {
        private final Question question;
        private final List<RRSIG> outdatedRrSigs;

        public NoActiveSignaturesReason(Question question, List<RRSIG> outdatedRrSigs) {
            this.question = question;
            assert !outdatedRrSigs.isEmpty();
            this.outdatedRrSigs = Collections.unmodifiableList(outdatedRrSigs);
        }

        @Override
        public String getReasonString() {
            return "No currently active signatures were attached to answer on question for " + question.type + " at " + question.name;
        }

        public List<RRSIG> getOutdatedRrSigs() {
            return outdatedRrSigs;
        }
    }

    public static class NSECDoesNotMatchReason extends DnssecUnverifiedReason {
        private final Question question;
        private final Record<? extends Data> record;

        public NSECDoesNotMatchReason(Question question, Record<? extends Data> record) {
            this.question = question;
            this.record = record;
        }

        @Override
        public String getReasonString() {
            return "NSEC " + record.name + " does nat match question for " + question.type + " at " + question.name;
        }
    }
}
