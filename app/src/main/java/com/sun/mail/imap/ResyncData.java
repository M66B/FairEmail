/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.mail.imap;

import com.sun.mail.imap.protocol.UIDSet;

/**
 * Resynchronization data as defined by the QRESYNC extension
 * (<A HREF="http://www.ietf.org/rfc/rfc5162.txt">RFC 5162</A>).
 * An instance of <CODE>ResyncData</CODE> is supplied to the
 * {@link com.sun.mail.imap.IMAPFolder#open(int,com.sun.mail.imap.ResyncData)
 * IMAPFolder open} method.
 * The CONDSTORE <CODE>ResyncData</CODE> instance is used to enable the
 * CONDSTORE extension
 * (<A HREF="http://www.ietf.org/rfc/rfc4551.txt">RFC 4551</A>).
 * A <CODE>ResyncData</CODE> instance with uidvalidity and modseq values
 * is used to enable the QRESYNC extension.
 *
 * @since	JavaMail 1.5.1
 * @author	Bill Shannon
 */

public class ResyncData { 
    private long uidvalidity = -1;
    private long modseq = -1;
    private UIDSet[] uids = null;

    /**
     * Used to enable only the CONDSTORE extension.
     */
    public static final ResyncData CONDSTORE = new ResyncData(-1, -1);

    /**
     * Used to report on changes since the specified modseq.
     * If the UIDVALIDITY of the folder has changed, no message
     * changes will be reported.  The application must check the
     * UIDVALIDITY of the folder after open to make sure it's
     * the expected folder.
     *
     * @param	uidvalidity	the UIDVALIDITY
     * @param	modseq		the MODSEQ
     */
    public ResyncData(long uidvalidity, long modseq) {
	this.uidvalidity = uidvalidity;
	this.modseq = modseq;
	this.uids = null;
    }

    /**
     * Used to limit the reported message changes to those with UIDs
     * in the specified range.
     *
     * @param	uidvalidity	the UIDVALIDITY
     * @param	modseq		the MODSEQ
     * @param	uidFirst	the first UID
     * @param	uidLast		the last UID
     */
    public ResyncData(long uidvalidity, long modseq,
				long uidFirst, long uidLast) {
	this.uidvalidity = uidvalidity;
	this.modseq = modseq;
	this.uids = new UIDSet[] { new UIDSet(uidFirst, uidLast) };
    }

    /**
     * Used to limit the reported message changes to those with the
     * specified UIDs.
     *
     * @param	uidvalidity	the UIDVALIDITY
     * @param	modseq		the MODSEQ
     * @param	uids		the UID values
     */
    public ResyncData(long uidvalidity, long modseq, long[] uids) {
	this.uidvalidity = uidvalidity;
	this.modseq = modseq;
	this.uids = UIDSet.createUIDSets(uids);
    }

    /**
     * Get the UIDVALIDITY value specified when this instance was created.
     *
     * @return	the UIDVALIDITY value
     */
    public long getUIDValidity() {
	return uidvalidity;
    }

    /**
     * Get the MODSEQ value specified when this instance was created.
     *
     * @return	the MODSEQ value
     */
    public long getModSeq() {
	return modseq;
    }

    /*
     * Package private.  IMAPProtocol gets this data indirectly
     * using Utility.getResyncUIDSet().
     */
    UIDSet[] getUIDSet() {
	return uids;
    }
}
