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

import javax.mail.Flags;
import com.sun.mail.iap.*; 

/**
 * This class 
 *
 * @author  John Mani
 */

public class FLAGS extends Flags implements Item {

    // IMAP item name
    static final char[] name = {'F','L','A','G','S'};
    public int msgno;

    private static final long serialVersionUID = 439049847053756670L;

    /**
     * Constructor.
     *
     * @param	r	the IMAPResponse
     * @exception	ParsingException	for parsing failures
     */
    public FLAGS(IMAPResponse r) throws ParsingException {
	msgno = r.getNumber();

	r.skipSpaces();
	String[] flags = r.readSimpleList();
	if (flags != null) { // if not empty flaglist
	    for (int i = 0; i < flags.length; i++) {
		String s = flags[i];
		if (s.length() >= 2 && s.charAt(0) == '\\') {
		    switch (Character.toUpperCase(s.charAt(1))) {
		    case 'S': // \Seen
			add(Flags.Flag.SEEN);
			break;
		    case 'R': // \Recent
			add(Flags.Flag.RECENT);
			break;
		    case 'D':
			if (s.length() >= 3) {
			    char c = s.charAt(2);
			    if (c == 'e' || c == 'E') // \Deleted
				add(Flags.Flag.DELETED);
			    else if (c == 'r' || c == 'R') // \Draft
				add(Flags.Flag.DRAFT);
			} else
			    add(s);	// unknown, treat it as a user flag
			break;
		    case 'A': // \Answered
			add(Flags.Flag.ANSWERED);
			break;
		    case 'F': // \Flagged
			add(Flags.Flag.FLAGGED);
			break;
		    case '*': // \*
			add(Flags.Flag.USER);
			break;
		    default:
			add(s);		// unknown, treat it as a user flag
			break;
		    }
		} else
		    add(s);
	    }
	}
    }
}
