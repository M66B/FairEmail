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

import java.io.ByteArrayInputStream;
import com.sun.mail.iap.*; 
import com.sun.mail.util.ASCIIUtility;

/**
 * The RFC822 response data item.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class RFC822DATA implements Item {
   
    static final char[] name = {'R','F','C','8','2','2'};
    private final int msgno;
    private final ByteArray data;
    private final boolean isHeader;

    /**
     * Constructor, header flag is false.
     *
     * @param	r	the FetchResponse
     * @exception	ParsingException	for parsing failures
     */
    public RFC822DATA(FetchResponse r) throws ParsingException {
	this(r, false);
    }

    /**
     * Constructor, specifying header flag.
     *
     * @param	r	the FetchResponse
     * @param	isHeader	just header information?
     * @exception	ParsingException	for parsing failures
     */
    public RFC822DATA(FetchResponse r, boolean isHeader)
				throws ParsingException {
	this.isHeader = isHeader;
	msgno = r.getNumber();
	r.skipSpaces();
	data = r.readByteArray();
    }

    public ByteArray getByteArray() {
	return data;
    }

    public ByteArrayInputStream getByteArrayInputStream() {
	if (data != null)
	    return data.toByteArrayInputStream();
	else
	    return null;
    }

    public boolean isHeader() {
	return isHeader;
    }
}
