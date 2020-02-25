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

import java.util.*;
import com.sun.mail.iap.*;

/**
 * This class and its inner class represent the response to the
 * NAMESPACE command. <p>
 *
 * See <A HREF="http://www.ietf.org/rfc/rfc2342.txt">RFC 2342</A>.
 *
 * @author Bill Shannon
 */

public class Namespaces {

    /**
     * A single namespace entry.
     */
    public static class Namespace {
	/**
	 * Prefix string for the namespace.
	 */
	public String prefix;

	/**
	 * Delimiter between names in this namespace.
	 */
	public char delimiter;

	/**
	 * Parse a namespace element out of the response.
	 *
	 * @param	r	the Response to parse
	 * @exception	ProtocolException	for any protocol errors
	 */
	public Namespace(Response r) throws ProtocolException {
	    // Namespace_Element = "(" string SP (<"> QUOTED_CHAR <"> / nil)
	    //		*(Namespace_Response_Extension) ")"
	    if (!r.isNextNonSpace('('))
		throw new ProtocolException(
					"Missing '(' at start of Namespace");
	    // first, the prefix
	    prefix = r.readString();
	    if (!r.supportsUtf8())
		prefix = BASE64MailboxDecoder.decode(prefix);
	    r.skipSpaces();
	    // delimiter is a quoted character or NIL
	    if (r.peekByte() == '"') {
		r.readByte();
		delimiter = (char)r.readByte();
		if (delimiter == '\\')
		    delimiter = (char)r.readByte();
		if (r.readByte() != '"')
		    throw new ProtocolException(
				    "Missing '\"' at end of QUOTED_CHAR");
	    } else {
		String s = r.readAtom();
		if (s == null)
		    throw new ProtocolException("Expected NIL, got null");
		if (!s.equalsIgnoreCase("NIL"))
		    throw new ProtocolException("Expected NIL, got " + s);
		delimiter = 0;
	    }
	    // at end of Namespace data?
	    if (r.isNextNonSpace(')'))
		return;

	    // otherwise, must be a Namespace_Response_Extension
	    //    Namespace_Response_Extension = SP string SP
	    //	    "(" string *(SP string) ")"
	    r.readString();
	    r.skipSpaces();
	    r.readStringList();
	    if (!r.isNextNonSpace(')'))
		throw new ProtocolException("Missing ')' at end of Namespace");
	}
    };

    /**
     * The personal namespaces.
     * May be null.
     */
    public Namespace[] personal;

    /**
     * The namespaces for other users.
     * May be null.
     */
    public Namespace[] otherUsers;

    /**
     * The shared namespace.
     * May be null.
     */
    public Namespace[] shared;

    /**
     * Parse out all the namespaces.
     *
     * @param	r	the Response to parse
     * @throws	ProtocolException	for any protocol errors
     */
    public Namespaces(Response r) throws ProtocolException {
	personal = getNamespaces(r);
	otherUsers = getNamespaces(r);
	shared = getNamespaces(r);
    }

    /**
     * Parse out one of the three sets of namespaces.
     */
    private Namespace[] getNamespaces(Response r) throws ProtocolException {
	//    Namespace = nil / "(" 1*( Namespace_Element) ")"
	if (r.isNextNonSpace('(')) {
	    List<Namespace> v = new ArrayList<>();
	    do {
		Namespace ns = new Namespace(r);
		v.add(ns);
	    } while (!r.isNextNonSpace(')'));
	    return v.toArray(new Namespace[v.size()]);
	} else {
	    String s = r.readAtom();
	    if (s == null)
		throw new ProtocolException("Expected NIL, got null");
	    if (!s.equalsIgnoreCase("NIL"))
		throw new ProtocolException("Expected NIL, got " + s);
	    return null;
	}
    }
}
