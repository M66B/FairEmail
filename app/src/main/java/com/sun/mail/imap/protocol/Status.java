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

import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

import com.sun.mail.iap.*;

/**
 * STATUS response.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class Status { 
    public String mbox = null;
    public int total = -1;
    public int recent = -1;
    public long uidnext = -1;
    public long uidvalidity = -1;
    public int unseen = -1;
    public long highestmodseq = -1;
    public Map<String,Long> items;	// any unknown items

    static final String[] standardItems =
	{ "MESSAGES", "RECENT", "UNSEEN", "UIDNEXT", "UIDVALIDITY" };

    public Status(Response r) throws ParsingException {
	// mailbox := astring
	mbox = r.readAtomString();
	if (!r.supportsUtf8())
	    mbox = BASE64MailboxDecoder.decode(mbox);

	// Workaround buggy IMAP servers that don't quote folder names
	// with spaces.
	final StringBuilder buffer = new StringBuilder();
	boolean onlySpaces = true;

	while (r.peekByte() != '(' && r.peekByte() != 0) {
	    final char next = (char)r.readByte();

	    buffer.append(next);

	    if (next != ' ') {
		onlySpaces = false;
	    }
	}

	if (!onlySpaces) {
	    mbox = (mbox + buffer).trim();
	}

	if (r.readByte() != '(')
	    throw new ParsingException("parse error in STATUS");
	
	do {
	    String attr = r.readAtom();
	    if (attr == null)
		throw new ParsingException("parse error in STATUS");
	    if (attr.equalsIgnoreCase("MESSAGES"))
		total = r.readNumber();
	    else if (attr.equalsIgnoreCase("RECENT"))
		recent = r.readNumber();
	    else if (attr.equalsIgnoreCase("UIDNEXT"))
		uidnext = r.readLong();
	    else if (attr.equalsIgnoreCase("UIDVALIDITY"))
		uidvalidity = r.readLong();
	    else if (attr.equalsIgnoreCase("UNSEEN"))
		unseen = r.readNumber();
	    else if (attr.equalsIgnoreCase("HIGHESTMODSEQ"))
		highestmodseq = r.readLong();
	    else {
		if (items == null)
		    items = new HashMap<>();
		items.put(attr.toUpperCase(Locale.ENGLISH),
			    Long.valueOf(r.readLong()));
	    }
	} while (!r.isNextNonSpace(')'));
    }

    /**
     * Get the value for the STATUS item.
     *
     * @param	item	the STATUS item
     * @return		the value
     * @since	JavaMail 1.5.2
     */
    public long getItem(String item) {
	item = item.toUpperCase(Locale.ENGLISH);
	Long v;
	long ret = -1;
	if (items != null && (v = items.get(item)) != null)
	    ret = v.longValue();
	else if (item.equals("MESSAGES"))
	    ret = total;
	else if (item.equals("RECENT"))
	    ret = recent;
	else if (item.equals("UIDNEXT"))
	    ret = uidnext;
	else if (item.equals("UIDVALIDITY"))
	    ret = uidvalidity;
	else if (item.equals("UNSEEN"))
	    ret = unseen;
	else if (item.equals("HIGHESTMODSEQ"))
	    ret = highestmodseq;
	return ret;
    }

    public static void add(Status s1, Status s2) {
	if (s2.total != -1)
	    s1.total = s2.total;
	if (s2.recent != -1)
	    s1.recent = s2.recent;
	if (s2.uidnext != -1)
	    s1.uidnext = s2.uidnext;
	if (s2.uidvalidity != -1)
	    s1.uidvalidity = s2.uidvalidity;
	if (s2.unseen != -1)
	    s1.unseen = s2.unseen;
	if (s2.highestmodseq != -1)
	    s1.highestmodseq = s2.highestmodseq;
	if (s1.items == null)
	    s1.items = s2.items;
	else if (s2.items != null)
	    s1.items.putAll(s2.items);
    }
}
