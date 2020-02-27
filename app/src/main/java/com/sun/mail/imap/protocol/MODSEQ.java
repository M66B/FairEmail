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

package com.sun.mail.imap.protocol;

import com.sun.mail.iap.*; 

/**
 * This class represents the MODSEQ data item.
 *
 * @since	JavaMail 1.5.1
 * @author	Bill Shannon
 */

public class MODSEQ implements Item {
    
    static final char[] name = {'M','O','D','S','E','Q'};
    public int seqnum;

    public long modseq;

    /**
     * Constructor.
     *
     * @param	r	the FetchResponse
     * @exception	ParsingException	for parsing failures
     */
    public MODSEQ(FetchResponse r) throws ParsingException {
	seqnum = r.getNumber();
	r.skipSpaces();

	if (r.readByte() != '(')
	    throw new ParsingException("MODSEQ parse error");

	modseq = r.readLong();

	if (!r.isNextNonSpace(')'))
	    throw new ParsingException("MODSEQ parse error");
    }
}
