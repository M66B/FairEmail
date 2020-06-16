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

import java.util.List;
import java.util.ArrayList;

import com.sun.mail.iap.*;

/**
 * A LIST response.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class ListInfo { 
    public String name = null;
    public char separator = '/';
    public boolean hasInferiors = true;
    public boolean canOpen = true;
    public int changeState = INDETERMINATE;
    public String[] attrs;

    public static final int CHANGED		= 1;
    public static final int UNCHANGED		= 2;
    public static final int INDETERMINATE	= 3;

    public ListInfo(IMAPResponse r) throws ParsingException {
	String[] s = r.readSimpleList();

	List<String> v = new ArrayList<>();	// accumulate attributes
	if (s != null) {
	    // non-empty attribute list
	    for (int i = 0; i < s.length; i++) {
		if (s[i].equalsIgnoreCase("\\Marked"))
		    changeState = CHANGED;
		else if (s[i].equalsIgnoreCase("\\Unmarked"))
		    changeState = UNCHANGED;
		else if (s[i].equalsIgnoreCase("\\Noselect"))
		    canOpen = false;
		else if (s[i].equalsIgnoreCase("\\Noinferiors"))
		    hasInferiors = false;
		v.add(s[i]);
	    }
	}
	attrs = v.toArray(new String[v.size()]);

	r.skipSpaces();
	if (r.readByte() == '"') {
	    if ((separator = (char)r.readByte()) == '\\')
		// escaped separator character
		separator = (char)r.readByte();	
	    r.skip(1); // skip <">
	} else // NIL
	    r.skip(2);
	
	r.skipSpaces();
	name = r.readAtomString();

	if (!r.supportsUtf8())
	    // decode the name (using RFC2060's modified UTF7)
	    name = BASE64MailboxDecoder.decode(name);
    }
}
