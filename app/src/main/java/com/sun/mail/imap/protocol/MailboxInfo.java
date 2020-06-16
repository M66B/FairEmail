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

import javax.mail.Flags;

import com.sun.mail.iap.*;

/**
 * Information collected when opening a mailbox.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class MailboxInfo { 
    /** The available flags. */
    public Flags availableFlags = null;
    /** The permanent flags. */
    public Flags permanentFlags = null;
    /** The total number of messages. */
    public int total = -1;
    /** The number of recent messages. */
    public int recent = -1;
    /** The first unseen message. */
    public int first = -1;
    /** The UIDVALIDITY. */
    public long uidvalidity = -1;
    /** The next UID value to be assigned. */
    public long uidnext = -1;
    /** UIDs are not sticky. */
    public boolean uidNotSticky = false;	// RFC 4315
    /** The highest MODSEQ value. */
    public long highestmodseq = -1;	// RFC 4551 - CONDSTORE
    /** Folder.READ_WRITE or Folder.READ_ONLY, set by IMAPProtocol. */
    public int mode;
    /** VANISHED or FETCH responses received while opening the mailbox. */
    public List<IMAPResponse> responses;

    /**
     * Collect the information about this mailbox from the
     * responses to a SELECT or EXAMINE.
     *
     * @param	r	the responses
     * @exception	ParsingException	for errors parsing the responses
     */
    public MailboxInfo(Response[] r) throws ParsingException {
	for (int i = 0; i < r.length; i++) {
	    if (r[i] == null || !(r[i] instanceof IMAPResponse))
		continue;

	    IMAPResponse ir = (IMAPResponse)r[i];

	    if (ir.keyEquals("EXISTS")) {
		total = ir.getNumber();
		r[i] = null; // remove this response
	    } else if (ir.keyEquals("RECENT")) {
		recent = ir.getNumber();
		r[i] = null; // remove this response
	    } else if (ir.keyEquals("FLAGS")) {
		availableFlags = new FLAGS(ir);
		r[i] = null; // remove this response
	    } else if (ir.keyEquals("VANISHED")) {
		if (responses == null)
		    responses = new ArrayList<>();
		responses.add(ir);
		r[i] = null; // remove this response
	    } else if (ir.keyEquals("FETCH")) {
		if (responses == null)
		    responses = new ArrayList<>();
		responses.add(ir);
		r[i] = null; // remove this response
	    } else if (ir.isUnTagged() && ir.isOK()) {
		/*
		 * should be one of:
		 * 	* OK [UNSEEN 12]
		 * 	* OK [UIDVALIDITY 3857529045]
		 * 	* OK [PERMANENTFLAGS (\Deleted)]
		 * 	* OK [UIDNEXT 44]
		 * 	* OK [HIGHESTMODSEQ 103]
		 */
		ir.skipSpaces();

		if (ir.readByte() != '[') {	// huh ???
		    ir.reset();
		    continue;
		}

		boolean handled = true;
		String s = ir.readAtom();
		if (s.equalsIgnoreCase("UNSEEN"))
		    first = ir.readNumber();
		else if (s.equalsIgnoreCase("UIDVALIDITY"))
		    uidvalidity = ir.readLong();
		else if (s.equalsIgnoreCase("PERMANENTFLAGS"))
		    permanentFlags = new FLAGS(ir);
		else if (s.equalsIgnoreCase("UIDNEXT"))
		    uidnext = ir.readLong();
		else if (s.equalsIgnoreCase("HIGHESTMODSEQ"))
		    highestmodseq = ir.readLong();
		else
		    handled = false;	// possibly an ALERT

		if (handled)
		    r[i] = null; // remove this response
		else
		    ir.reset();	// so ALERT can be read
	    } else if (ir.isUnTagged() && ir.isNO()) {
		/*
		 * should be one of:
		 * 	* NO [UIDNOTSTICKY]
		 */
		ir.skipSpaces();

		if (ir.readByte() != '[') {	// huh ???
		    ir.reset();
		    continue;
		}

		boolean handled = true;
		String s = ir.readAtom();
		if (s.equalsIgnoreCase("UIDNOTSTICKY"))
		    uidNotSticky = true;
		else
		    handled = false;	// possibly an ALERT

		if (handled)
		    r[i] = null; // remove this response
		else
		    ir.reset();	// so ALERT can be read
	    }
	}

	/*
	 * The PERMANENTFLAGS response code is optional, and if
	 * not present implies that all flags in the required FLAGS
	 * response can be changed permanently.
	 */
	if (permanentFlags == null) {
	    if (availableFlags != null)
		permanentFlags = new Flags(availableFlags);
	    else
		permanentFlags = new Flags();
	}
    }
}
