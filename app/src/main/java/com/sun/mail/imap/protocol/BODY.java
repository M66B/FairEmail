/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
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
 * The BODY fetch response item.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class BODY implements Item {
    
    static final char[] name = {'B','O','D','Y'};

    private final int msgno;
    private final ByteArray data;
    private final String section;
    private final int origin;
    private final boolean isHeader;

    /**
     * Constructor
     *
     * @param	r	the FetchResponse
     * @exception	ParsingException	for parsing failures
     */
    public BODY(FetchResponse r) throws ParsingException {
	msgno = r.getNumber();

	r.skipSpaces();

	if (r.readByte() != '[')
	    throw new ParsingException(
		    "BODY parse error: missing ``['' at section start");
	section = r.readString(']');
	if (r.readByte() != ']')
	    throw new ParsingException(
		    "BODY parse error: missing ``]'' at section end");
	isHeader = section.regionMatches(true, 0, "HEADER", 0, 6);

	if (r.readByte() == '<') { // origin
	    origin = r.readNumber();
	    r.skip(1); // skip '>';
	} else
	    origin = -1;

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

    public String getSection() {
	return section;
    }

    /**
     * @since	Jakarta Mail 1.6.4
     */
    public int getOrigin() {
	return origin;
    }
}
