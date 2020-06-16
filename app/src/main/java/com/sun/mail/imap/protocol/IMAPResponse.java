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

import java.io.*;
import java.util.*;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.iap.*;

/**
 * This class represents a response obtained from the input stream
 * of an IMAP server.
 *
 * @author  John Mani
 */

public class IMAPResponse extends Response {
    private String key;
    private int number;

    public IMAPResponse(Protocol c) throws IOException, ProtocolException {
	super(c);
	init();
    }

    private void init() throws IOException, ProtocolException {
	// continue parsing if this is an untagged response
	if (isUnTagged() && !isOK() && !isNO() && !isBAD() && !isBYE()) {
	    key = readAtom();

	    // Is this response of the form "* <number> <command>"
	    try {
		number = Integer.parseInt(key);
		key = readAtom();
	    } catch (NumberFormatException ne) { }
	}
    }

    /**
     * Copy constructor.
     *
     * @param	r	the IMAPResponse to copy
     */
    public IMAPResponse(IMAPResponse r) {
	super((Response)r);
	key = r.key;
	number = r.number;
    }

    /**
     * For testing.
     *
     * @param	r	the response string
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public IMAPResponse(String r) throws IOException, ProtocolException {
	this(r, true);
    }

    /**
     * For testing.
     *
     * @param	r	the response string
     * @param	utf8	UTF-8 allowed?
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     * @since	JavaMail 1.6.0
     */
    public IMAPResponse(String r, boolean utf8)
				throws IOException, ProtocolException {
	super(r, utf8);
	init();
    }

    /**
     * Read a list of space-separated "flag-extension" sequences and 
     * return the list as a array of Strings. An empty list is returned
     * as null.  Each item is expected to be an atom, possibly preceeded
     * by a backslash, but we aren't that strict; we just look for strings
     * separated by spaces and terminated by a right paren.  We assume items
     * are always ASCII.
     *
     * @return	the list items as a String array
     */
    public String[] readSimpleList() {
	skipSpaces();

	if (buffer[index] != '(') // not what we expected
	    return null;
	index++; // skip '('

	List<String> v = new ArrayList<>();
	int start;
	for (start = index; buffer[index] != ')'; index++) {
	    if (buffer[index] == ' ') { // got one item
		v.add(ASCIIUtility.toString(buffer, start, index));
		start = index+1; // index gets incremented at the top
	    }
	}
	if (index > start) // get the last item
	    v.add(ASCIIUtility.toString(buffer, start, index));
	index++; // skip ')'
	
	int size = v.size();
	if (size > 0)
	    return v.toArray(new String[size]);
	else  // empty list
	    return null;
    }

    public String getKey() {
	return key;
    }

    public boolean keyEquals(String k) {
	if (key != null && key.equalsIgnoreCase(k))
	    return true;
	else
	    return false;
    }

    public int getNumber() {
	return number;
    }
}
