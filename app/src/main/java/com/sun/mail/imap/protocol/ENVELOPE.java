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
import java.util.Date;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeUtility;
import com.sun.mail.iap.*;
import com.sun.mail.util.PropUtil;

/**
 * The ENEVELOPE item of an IMAP FETCH response.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class ENVELOPE implements Item {
    
    // IMAP item name
    static final char[] name = {'E','N','V','E','L','O','P','E'};
    public int msgno;

    public Date date = null;
    public String subject;
    public InternetAddress[] from;
    public InternetAddress[] sender;
    public InternetAddress[] replyTo;
    public InternetAddress[] to;
    public InternetAddress[] cc;
    public InternetAddress[] bcc;
    public String inReplyTo;
    public String messageId;

    // Used to parse dates
    private static final MailDateFormat mailDateFormat = new MailDateFormat();

    // special debugging output to debug parsing errors
    private static final boolean parseDebug =
	PropUtil.getBooleanSystemProperty("mail.imap.parse.debug", false);
    
    public ENVELOPE(FetchResponse r) throws ParsingException {
	if (parseDebug)
	    System.out.println("parse ENVELOPE");
	msgno = r.getNumber();

	r.skipSpaces();

	if (r.readByte() != '(')
	    throw new ParsingException("ENVELOPE parse error");
	
	String s = r.readString();
	if (s != null) {
	    try {
            synchronized (mailDateFormat) {
                date = mailDateFormat.parse(s);
            }
	    } catch (ParseException pex) {
	    }
	}
	if (parseDebug)
	    System.out.println("  Date: " + date);

	subject = r.readString();
	if (parseDebug)
	    System.out.println("  Subject: " + subject);
	if (parseDebug)
	    System.out.println("  From addresses:");
	from = parseAddressList(r);
	if (parseDebug)
	    System.out.println("  Sender addresses:");
	sender = parseAddressList(r);
	if (parseDebug)
	    System.out.println("  Reply-To addresses:");
	replyTo = parseAddressList(r);
	if (parseDebug)
	    System.out.println("  To addresses:");
	to = parseAddressList(r);
	if (parseDebug)
	    System.out.println("  Cc addresses:");
	cc = parseAddressList(r);
	if (parseDebug)
	    System.out.println("  Bcc addresses:");
	bcc = parseAddressList(r);
	inReplyTo = r.readString();
	if (parseDebug)
	    System.out.println("  In-Reply-To: " + inReplyTo);
	messageId = r.readString();
	if (parseDebug)
	    System.out.println("  Message-ID: " + messageId);

	if (!r.isNextNonSpace(')'))
	    throw new ParsingException("ENVELOPE parse error");
    }

    private InternetAddress[] parseAddressList(Response r) 
		throws ParsingException {
	r.skipSpaces(); // skip leading spaces

	byte b = r.readByte();
	if (b == '(') {
	    /*
	     * Some broken servers (e.g., Yahoo Mail) return an empty
	     * list instead of NIL.  Handle that here even though it
	     * doesn't conform to the IMAP spec.
	     */
	    if (r.isNextNonSpace(')'))
		return null;

	    List<InternetAddress> v = new ArrayList<>();

	    do {
		IMAPAddress a = new IMAPAddress(r);
		if (parseDebug)
		    System.out.println("    Address: " + a);
		// if we see an end-of-group address at the top, ignore it
		if (!a.isEndOfGroup())
		    v.add(a);
	    } while (!r.isNextNonSpace(')'));

	    return v.toArray(new InternetAddress[v.size()]);
	} else if (b == 'N' || b == 'n') { // NIL
	    r.skip(2); // skip 'NIL'
	    return null;
	} else
	    throw new ParsingException("ADDRESS parse error");
    }
}

class IMAPAddress extends InternetAddress {
    private boolean group = false;
    private InternetAddress[] grouplist;
    private String groupname;

    private static final long serialVersionUID = -3835822029483122232L;

    IMAPAddress(Response r) throws ParsingException {
        r.skipSpaces(); // skip leading spaces

        if (r.readByte() != '(')
            throw new ParsingException("ADDRESS parse error");

        encodedPersonal = r.readString();

        r.readString(); // throw away address_list
	String mb = r.readString();
	String host = r.readString();
	// skip bogus spaces inserted by Yahoo IMAP server if
	// "undisclosed-recipients" is a recipient
	r.skipSpaces();
	if (!r.isNextNonSpace(')')) // skip past terminating ')'
            throw new ParsingException("ADDRESS parse error");

	if (host == null) {
	    // it's a group list, start or end
	    group = true;
	    groupname = mb;
	    if (groupname == null)	// end of group list
		return;
	    // Accumulate a group list.  The members of the group
	    // are accumulated in a List and the corresponding string
	    // representation of the group is accumulated in a StringBuilder.
	    StringBuilder sb = new StringBuilder();
	    sb.append(groupname).append(':');
	    List<InternetAddress> v = new ArrayList<>();
	    while (r.peekByte() != ')') {
		IMAPAddress a = new IMAPAddress(r);
		if (a.isEndOfGroup())	// reached end of group
		    break;
		if (v.size() != 0)	// if not first element, need a comma
		    sb.append(',');
		sb.append(a.toString());
		v.add(a);
	    }
	    sb.append(';');
	    address = sb.toString();
	    grouplist = v.toArray(new IMAPAddress[v.size()]);
	} else {
	    if (mb == null || mb.length() == 0)
		address = host;
	    else if (host.length() == 0)
		address = mb;
	    else
		address = mb + "@" + host;
	}

    }

    boolean isEndOfGroup() {
	return group && groupname == null;
    }

    @Override
    public boolean isGroup() {
	return group;
    }

    @Override
    public InternetAddress[] getGroup(boolean strict) throws AddressException {
	if (grouplist == null)
	    return null;
	return grouplist.clone();
    }
}
