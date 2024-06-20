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
import java.util.Set;

import org.minidns.dnsmessage.DnsMessage;
import org.minidns.dnsqueryresult.DnsQueryResult;
import org.minidns.record.RRSIG;
import org.minidns.record.Record;

public class DnssecQueryResult {

    public final DnsMessage synthesizedResponse;
    public final DnsQueryResult dnsQueryResult;

    private final Set<Record<RRSIG>> signatures;
    private final Set<DnssecUnverifiedReason> dnssecUnverifiedReasons;

    DnssecQueryResult(DnsMessage synthesizedResponse, DnsQueryResult dnsQueryResult, Set<Record<RRSIG>> signatures,
            Set<DnssecUnverifiedReason> dnssecUnverifiedReasons) {
        this.synthesizedResponse = synthesizedResponse;
        this.dnsQueryResult = dnsQueryResult;
        this.signatures = Collections.unmodifiableSet(signatures);
        if (dnssecUnverifiedReasons == null) {
            this.dnssecUnverifiedReasons = Collections.emptySet();
        } else {
            this.dnssecUnverifiedReasons = Collections.unmodifiableSet(dnssecUnverifiedReasons);
        }
    }

    public boolean isAuthenticData() {
        return dnssecUnverifiedReasons.isEmpty();
    }

    public Set<Record<RRSIG>> getSignatures() {
        return signatures;
    }

    public Set<DnssecUnverifiedReason> getUnverifiedReasons() {
        return dnssecUnverifiedReasons;
    }

}
