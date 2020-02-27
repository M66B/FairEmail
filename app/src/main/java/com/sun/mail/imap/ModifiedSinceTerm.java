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

import javax.mail.Message;
import javax.mail.search.SearchTerm;

/**
 * Find messages that have been modified since a given MODSEQ value.
 * Relies on the server implementing the CONDSTORE extension
 * (<A HREF="http://www.ietf.org/rfc/rfc4551.txt">RFC 4551</A>).
 *
 * @since	JavaMail 1.5.1
 * @author	Bill Shannon
 */
public final class ModifiedSinceTerm extends SearchTerm {

    private long modseq;

    private static final long serialVersionUID = 5151457469634727992L;

    /**
     * Constructor.
     *
     * @param modseq	modification sequence number
     */
    public ModifiedSinceTerm(long modseq) {
	this.modseq = modseq;
    }

    /**
     * Return the modseq.
     *
     * @return	the modseq
     */
    public long getModSeq() {
	return modseq;
    }

    /**
     * The match method.
     *
     * @param msg	the date comparator is applied to this Message's
     *			MODSEQ
     * @return		true if the comparison succeeds, otherwise false
     */
    @Override
    public boolean match(Message msg) {
	long m;

	try {
	    if (msg instanceof IMAPMessage)
		m = ((IMAPMessage)msg).getModSeq();
	    else
		return false;
	} catch (Exception e) {
	    return false;
	}

	return m >= modseq;
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof ModifiedSinceTerm))
	    return false;
	return modseq == ((ModifiedSinceTerm)obj).modseq;
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return (int)modseq;
    }
}
