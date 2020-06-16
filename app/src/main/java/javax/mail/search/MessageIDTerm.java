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

package javax.mail.search;

import javax.mail.Message;

/**
 * This term models the RFC822 "MessageId" - a message-id for 
 * Internet messages that is supposed to be unique per message.
 * Clients can use this term to search a folder for a message given
 * its MessageId. <p>
 *
 * The MessageId is represented as a String.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class MessageIDTerm extends StringTerm {

    private static final long serialVersionUID = -2121096296454691963L;

    /**
     * Constructor.
     *
     * @param msgid  the msgid to search for
     */
    public MessageIDTerm(String msgid) {
	// Note: comparison is case-insensitive
	super(msgid);
    }

    /**
     * The match method.
     *
     * @param msg	the match is applied to this Message's 
     *			Message-ID header
     * @return		true if the match succeeds, otherwise false
     */
    @Override
    public boolean match(Message msg) {
	String[] s;

	try {
	    s = msg.getHeader("Message-ID");
	} catch (Exception e) {
	    return false;
	}

	if (s == null)
	    return false;

	for (int i=0; i < s.length; i++)
	    if (super.match(s[i]))
		return true;
	return false;
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof MessageIDTerm))
	    return false;
	return super.equals(obj);
    }
}
