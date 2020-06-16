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
import javax.mail.internet.ParameterList;
import com.sun.mail.iap.*; 
import com.sun.mail.util.PropUtil;

/**
 * A BODYSTRUCTURE response.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class BODYSTRUCTURE implements Item {
    
    static final char[] name =
	{'B','O','D','Y','S','T','R','U','C','T','U','R','E'};
    public int msgno;

    public String type;		// Type
    public String subtype;	// Subtype
    public String encoding;	// Encoding
    public int lines = -1;	// Size in lines
    public int size = -1;	// Size in bytes
    public String disposition;	// Disposition
    public String id;		// Content-ID
    public String description;	// Content-Description
    public String md5;		// MD-5 checksum
    public String attachment;	// Attachment name
    public ParameterList cParams; // Body parameters
    public ParameterList dParams; // Disposition parameters
    public String[] language;	// Language
    public BODYSTRUCTURE[] bodies; // array of BODYSTRUCTURE objects
				   //  for multipart & message/rfc822
    public ENVELOPE envelope;	// for message/rfc822

    private static int SINGLE	= 1;
    private static int MULTI	= 2;
    private static int NESTED	= 3;
    private int processedType;	// MULTI | SINGLE | NESTED

    // special debugging output to debug parsing errors
    private static final boolean parseDebug =
	PropUtil.getBooleanSystemProperty("mail.imap.parse.debug", false);


    public BODYSTRUCTURE(FetchResponse r) throws ParsingException {
	if (parseDebug)
	    System.out.println("DEBUG IMAP: parsing BODYSTRUCTURE");
	msgno = r.getNumber();
	if (parseDebug)
	    System.out.println("DEBUG IMAP: msgno " + msgno);

	r.skipSpaces();

	if (r.readByte() != '(')
	    throw new ParsingException(
		"BODYSTRUCTURE parse error: missing ``('' at start");

	if (r.peekByte() == '(') { // multipart
	    if (parseDebug)
		System.out.println("DEBUG IMAP: parsing multipart");
	    type = "multipart";
	    processedType = MULTI;
	    List<BODYSTRUCTURE> v = new ArrayList<>(1);
	    int i = 1;
	    do {
		v.add(new BODYSTRUCTURE(r));
		/*
		 * Even though the IMAP spec says there can't be any spaces
		 * between parts, some servers erroneously put a space in
		 * here.  In the spirit of "be liberal in what you accept",
		 * we skip it.
		 */
		r.skipSpaces();
	    } while (r.peekByte() == '(');

	    // setup bodies.
	    bodies = v.toArray(new BODYSTRUCTURE[v.size()]);

	    subtype = r.readString(); // subtype
	    if (parseDebug)
		System.out.println("DEBUG IMAP: subtype " + subtype);

	    if (r.isNextNonSpace(')')) { // done
		if (parseDebug)
		    System.out.println("DEBUG IMAP: parse DONE");
		return;
	    }

	    // Else, we have extension data

	    if (parseDebug)
		System.out.println("DEBUG IMAP: parsing extension data");
	    // Body parameters
	    cParams = parseParameters(r);
	    if (r.isNextNonSpace(')')) { // done
		if (parseDebug)
		    System.out.println("DEBUG IMAP: body parameters DONE");
		return;
	    }
	    
	    // Disposition
	    byte b = r.peekByte();
	    if (b == '(') {
		if (parseDebug)
		    System.out.println("DEBUG IMAP: parse disposition");
		r.readByte();
		disposition = r.readString();
		if (parseDebug)
		    System.out.println("DEBUG IMAP: disposition " +
							disposition);
		dParams = parseParameters(r);
		if (!r.isNextNonSpace(')')) // eat the end ')'
		    throw new ParsingException(
			"BODYSTRUCTURE parse error: " +
			"missing ``)'' at end of disposition in multipart");
		if (parseDebug)
		    System.out.println("DEBUG IMAP: disposition DONE");
	    } else if (b == 'N' || b == 'n') {
		if (parseDebug)
		    System.out.println("DEBUG IMAP: disposition NIL");
		r.skip(3); // skip 'NIL'
	    } else {
		/*
		throw new ParsingException(
		    "BODYSTRUCTURE parse error: " +
		    type + "/" + subtype + ": " +
		    "bad multipart disposition, b " + b);
		*/
		if (parseDebug)
		    System.out.println("DEBUG IMAP: bad multipart disposition" +
					", applying Exchange bug workaround");
		description = r.readString();
		if (parseDebug)
		    System.out.println("DEBUG IMAP: multipart description " +
					description);
		// Throw away whatever comes after it, since we have no
		// idea what it's supposed to be
		while (r.readByte() == ' ')
		    parseBodyExtension(r);
		return;
	    }

	    // RFC3501 allows no body-fld-lang after body-fld-disp,
	    // even though RFC2060 required it
	    if (r.isNextNonSpace(')')) {
		if (parseDebug)
		    System.out.println("DEBUG IMAP: no body-fld-lang");
		return; // done
	    }

	    // Language
	    if (r.peekByte() == '(') { // a list follows
		language = r.readStringList();
		if (parseDebug)
		    System.out.println(
			"DEBUG IMAP: language len " + language.length);
	    } else {
		String l = r.readString();
		if (l != null) {
		    String[] la = { l };
		    language = la;
		    if (parseDebug)
			System.out.println("DEBUG IMAP: language " + l);
		}
	    }

	    // RFC3501 defines an optional "body location" next,
	    // but for now we ignore it along with other extensions.

	    // Throw away any further extension data
	    while (r.readByte() == ' ')
		parseBodyExtension(r);
	} else if (r.peekByte() == ')') {	// (illegal) empty body
	    /*
	     * Domino will fail to return the body structure of nested messages.
	     * Fake it by providing an empty message.  Could probably do better
	     * with more work...
	     */
	    /*
	     * XXX - this prevents the exception, but without the exception
	     * the application has no way to know the data from the message
	     * is missing.
	     *
	    if (parseDebug)
		System.out.println("DEBUG IMAP: empty body, fake it");
	    r.readByte();
	    type = "text";
	    subtype = "plain";
	    lines = 0;
	    size = 0;
	     */
	    throw new ParsingException(
			    "BODYSTRUCTURE parse error: missing body content");
	} else { // Single part
	    if (parseDebug)
		System.out.println("DEBUG IMAP: single part");
	    type = r.readString();
	    if (parseDebug)
		System.out.println("DEBUG IMAP: type " + type);
	    processedType = SINGLE;
	    subtype = r.readString();
	    if (parseDebug)
		System.out.println("DEBUG IMAP: subtype " + subtype);

	    // SIMS 4.0 returns NIL for a Content-Type of "binary", fix it here
	    if (type == null) {
		type = "application";
		subtype = "octet-stream";
	    }
	    cParams = parseParameters(r);
	    if (parseDebug)
		System.out.println("DEBUG IMAP: cParams " + cParams);
	    id = r.readString();
	    if (parseDebug)
		System.out.println("DEBUG IMAP: id " + id);
	    description = r.readString();
	    if (parseDebug)
		System.out.println("DEBUG IMAP: description " + description);
	    /*
	     * XXX - Work around bug in Exchange 2010 that
	     *       returns unquoted string.
	     */
	    encoding = r.readAtomString();
	    if (encoding != null && encoding.equalsIgnoreCase("NIL")) {
		if (parseDebug)
		    System.out.println("DEBUG IMAP: NIL encoding" +
					", applying Exchange bug workaround");
		encoding = null;
	    }
	    /*
	     * XXX - Work around bug in office365.com that returns
	     *	     a string with a trailing space in some cases.
	     */
	    if (encoding != null)
		encoding = encoding.trim();
	    if (parseDebug)
		System.out.println("DEBUG IMAP: encoding " + encoding);
	    size = r.readNumber();
	    if (parseDebug)
		System.out.println("DEBUG IMAP: size " + size);
	    if (size < 0)
		throw new ParsingException(
			    "BODYSTRUCTURE parse error: bad ``size'' element");

	    // "text/*" & "message/rfc822" types have additional data ..
	    if (type.equalsIgnoreCase("text")) {
		lines = r.readNumber();
		if (parseDebug)
		    System.out.println("DEBUG IMAP: lines " + lines);
		if (lines < 0)
		    throw new ParsingException(
			    "BODYSTRUCTURE parse error: bad ``lines'' element");
	    } else if (type.equalsIgnoreCase("message") &&
		     subtype.equalsIgnoreCase("rfc822")) {
		// Nested message
		processedType = NESTED;
		// The envelope comes next, but sadly Gmail handles nested
		// messages just like simple body parts and fails to return
		// the envelope and body structure of the message (sort of
		// like IMAP4 before rev1).
		r.skipSpaces();
		if (r.peekByte() == '(') {	// the envelope follows
		    envelope = new ENVELOPE(r);
		    if (parseDebug)
			System.out.println(
			    "DEBUG IMAP: got envelope of nested message");
		    BODYSTRUCTURE[] bs = { new BODYSTRUCTURE(r) };
		    bodies = bs;
		    lines = r.readNumber();
		    if (parseDebug)
			System.out.println("DEBUG IMAP: lines " + lines);
		    if (lines < 0)
			throw new ParsingException(
			    "BODYSTRUCTURE parse error: bad ``lines'' element");
		} else {
		    if (parseDebug)
			System.out.println("DEBUG IMAP: " +
			    "missing envelope and body of nested message");
		}
	    } else {
		// Detect common error of including lines element on other types
		r.skipSpaces();
		byte bn = r.peekByte();
		if (Character.isDigit((char)bn)) // number
		    throw new ParsingException(
			    "BODYSTRUCTURE parse error: server erroneously " +
				"included ``lines'' element with type " +
				type + "/" + subtype);
	    }

	    if (r.isNextNonSpace(')')) {
		if (parseDebug)
		    System.out.println("DEBUG IMAP: parse DONE");
		return; // done
	    }

	    // Optional extension data

	    // MD5
	    md5 = r.readString();
	    if (r.isNextNonSpace(')')) {
		if (parseDebug)
		    System.out.println("DEBUG IMAP: no MD5 DONE");
		return; // done
	    }
	    
	    // Disposition
	    byte b = r.readByte();
	    if (b == '(') {
		disposition = r.readString();
		if (parseDebug)
		    System.out.println("DEBUG IMAP: disposition " +
							disposition);
		dParams = parseParameters(r);
		if (parseDebug)
		    System.out.println("DEBUG IMAP: dParams " + dParams);
		if (!r.isNextNonSpace(')')) // eat the end ')'
		    throw new ParsingException(
			"BODYSTRUCTURE parse error: " +
			"missing ``)'' at end of disposition");
	    } else if (b == 'N' || b == 'n') {
		if (parseDebug)
		    System.out.println("DEBUG IMAP: disposition NIL");
		r.skip(2); // skip 'NIL'
	    } else {
		throw new ParsingException(
		    "BODYSTRUCTURE parse error: " +
		    type + "/" + subtype + ": " +
		    "bad single part disposition, b " + b);
	    }

	    if (r.isNextNonSpace(')')) {
		if (parseDebug)
		    System.out.println("DEBUG IMAP: disposition DONE");
		return; // done
	    }
	    
	    // Language
	    if (r.peekByte() == '(') { // a list follows
		language = r.readStringList();
		if (parseDebug)
		    System.out.println("DEBUG IMAP: language len " +
							language.length);
	    } else { // protocol is unnessarily complex here
		String l = r.readString();
		if (l != null) {
		    String[] la = { l };
		    language = la;
		    if (parseDebug)
			System.out.println("DEBUG IMAP: language " + l);
		}
	    }

	    // RFC3501 defines an optional "body location" next,
	    // but for now we ignore it along with other extensions.

	    // Throw away any further extension data
	    while (r.readByte() == ' ')
		parseBodyExtension(r);
	    if (parseDebug)
		System.out.println("DEBUG IMAP: all DONE");
	}
    }

    public boolean isMulti() {
	return processedType == MULTI;
    }

    public boolean isSingle() {
	return processedType == SINGLE;
    }

    public boolean isNested() {
	return processedType == NESTED;
    }

    private ParameterList parseParameters(Response r)
			throws ParsingException {
	r.skipSpaces();

	ParameterList list = null;
	byte b = r.readByte();
	if (b == '(') {
	    list = new ParameterList();
	    do {
		String name = r.readString();
		if (parseDebug)
		    System.out.println("DEBUG IMAP: parameter name " + name);
		if (name == null)
		    throw new ParsingException(
			"BODYSTRUCTURE parse error: " +
			type + "/" + subtype + ": " +
			"null name in parameter list");
		String value = r.readString();
		if (parseDebug)
		    System.out.println("DEBUG IMAP: parameter value " + value);
		if (value == null) {	// work around buggy servers
		    if (parseDebug)
			System.out.println("DEBUG IMAP: NIL parameter value" +
					", applying Exchange bug workaround");
		    value = "";
		}
		list.set(name, value);
	    } while (!r.isNextNonSpace(')'));
	    list.combineSegments();
	} else if (b == 'N' || b == 'n') {
	    if (parseDebug)
		System.out.println("DEBUG IMAP: parameter list NIL");
	    r.skip(2);
	} else
	    throw new ParsingException("Parameter list parse error");

	return list;
    }

    private void parseBodyExtension(Response r) throws ParsingException {
	r.skipSpaces();

	byte b = r.peekByte();
	if (b == '(') {
	    r.skip(1); // skip '('
	    do {
		parseBodyExtension(r);
	    } while (!r.isNextNonSpace(')'));
	} else if (Character.isDigit((char)b)) // number
	    r.readNumber();
	else // nstring
	    r.readString();
    }
}
