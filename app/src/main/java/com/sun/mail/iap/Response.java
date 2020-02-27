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

package com.sun.mail.iap;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

import com.sun.mail.util.ASCIIUtility;

/**
 * This class represents a response obtained from the input stream
 * of an IMAP server.
 *
 * @author John Mani
 * @author Bill Shannon
 */

public class Response {
    protected int index;  // internal index (updated during the parse)
    protected int pindex; // index after parse, for reset
    protected int size;   // number of valid bytes in our buffer
    protected byte[] buffer = null;
    protected int type = 0;
    protected String tag = null;
    /** @since JavaMail 1.5.4 */
    protected Exception ex;
    protected boolean utf8;

    private static final int increment = 100;

    // The first and second bits indicate whether this response
    // is a Continuation, Tagged or Untagged
    public final static int TAG_MASK 	 = 0x03;
    public final static int CONTINUATION = 0x01;
    public final static int TAGGED 	 = 0x02;
    public final static int UNTAGGED 	 = 0x03;

    // The third, fourth and fifth bits indicate whether this response
    // is an OK, NO, BAD or BYE response
    public final static int TYPE_MASK 	 = 0x1C;
    public final static int OK 	 	 = 0x04;
    public final static int NO 	 	 = 0x08;
    public final static int BAD	 	 = 0x0C;
    public final static int BYE	 	 = 0x10;

    // The sixth bit indicates whether a BYE response is synthetic or real
    public final static int SYNTHETIC 	 = 0x20;

    /**
     * An ATOM is any CHAR delimited by:
     * SPACE | CTL | '(' | ')' | '{' | '%' | '*' | '"' | '\' | ']'
     * (CTL is handled in readDelimString.)
     */
    private static String ATOM_CHAR_DELIM = " (){%*\"\\]";

    /**
     * An ASTRING_CHAR is any CHAR delimited by:
     * SPACE | CTL | '(' | ')' | '{' | '%' | '*' | '"' | '\'
     * (CTL is handled in readDelimString.)
     */
    private static String ASTRING_CHAR_DELIM = " (){%*\"\\";

    public Response(String s) {
	this(s, true);
    }

    /**
     * Constructor for testing.
     *
     * @param	s	the response string
     * @param	supportsUtf8	allow UTF-8 in response?
     * @since	JavaMail 1.6.0
     */
    public Response(String s, boolean supportsUtf8) {
	if (supportsUtf8)
	    buffer = s.getBytes(StandardCharsets.UTF_8);
	else
	    buffer = s.getBytes(StandardCharsets.US_ASCII);
	size = buffer.length;
	utf8 = supportsUtf8;
	parse();
    }

    /**
     * Read a new Response from the given Protocol
     *
     * @param	p	the Protocol object
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public Response(Protocol p) throws IOException, ProtocolException {
	// read one response into 'buffer'
	ByteArray ba = p.getResponseBuffer();
	ByteArray response = p.getInputStream().readResponse(ba);
	buffer = response.getBytes();
	size = response.getCount() - 2; // Skip the terminating CRLF
	utf8 = p.supportsUtf8();

	parse();
    }

    /**
     * Copy constructor.
     *
     * @param	r	the Response to copy
     */
    public Response(Response r) {
	index = r.index;
	pindex = r.pindex;
	size = r.size;
	buffer = r.buffer;
	type = r.type;
	tag = r.tag;
	ex = r.ex;
	utf8 = r.utf8;
    }

    /**
     * Return a Response object that looks like a BYE protocol response.
     * Include the details of the exception in the response string.
     *
     * @param	ex	the exception
     * @return		the synthetic Response object
     */
    public static Response byeResponse(Exception ex) {
	String err = "* BYE Jakarta Mail Exception: " + ex.toString();
	err = err.replace('\r', ' ').replace('\n', ' ');
	Response r = new Response(err);
	r.type |= SYNTHETIC;
	r.ex = ex;
	return r;
    }

    /**
     * Does the server support UTF-8?
     *
     * @return		true if the server supports UTF-8
     * @since	JavaMail 1.6.0
     */
    public boolean supportsUtf8() {
	return utf8;
    }

    private void parse() {
	index = 0; // position internal index at start

	if (size == 0)	// empty line
	    return;
	if (buffer[index] == '+') { // Continuation statement
	    type |= CONTINUATION;
	    index += 1; // Position beyond the '+'
	    return;	// return
	} else if (buffer[index] == '*') { // Untagged statement
	    type |= UNTAGGED;
	    index += 1; // Position beyond the '*'
	} else {  // Tagged statement
	    type |= TAGGED;
	    tag = readAtom();	// read the TAG, index positioned beyond tag
	    if (tag == null)
		tag = "";	// avoid possible NPE
	}

	int mark = index; // mark
	String s = readAtom();	// updates index
	if (s == null)
	    s = "";		// avoid possible NPE
	if (s.equalsIgnoreCase("OK"))
	    type |= OK;
	else if (s.equalsIgnoreCase("NO"))
	    type |= NO;
	else if (s.equalsIgnoreCase("BAD"))
	    type |= BAD;
	else if (s.equalsIgnoreCase("BYE"))
	    type |= BYE;
	else
	    index = mark; // reset

	pindex = index;
	return;
    }

    public void skipSpaces() {
	while (index < size && buffer[index] == ' ')
	    index++;
    }

    /**
     * Skip past any spaces.  If the next non-space character is c,
     * consume it and return true.  Otherwise stop at that point
     * and return false.
     *
     * @param	c	the character to look for
     * @return		true if the character is found
     */
    public boolean isNextNonSpace(char c) {
	skipSpaces();
	if (index < size && buffer[index] == (byte)c) {
	    index++;
	    return true;
	}
	return false;
    }

    /**
     * Skip to the next space, for use in error recovery while parsing.
     */
    public void skipToken() {
	while (index < size && buffer[index] != ' ')
	    index++;
    }

    public void skip(int count) {
	index += count;
    }

    public byte peekByte() {
	if (index < size)
	    return buffer[index];
	else
	    return 0;		// XXX - how else to signal error?
    }

    /**
     * Return the next byte from this Statement.
     *
     * @return the next byte
     */
    public byte readByte() {
	if (index < size)
	    return buffer[index++];
	else
	    return 0;		// XXX - how else to signal error?
    }

    /**
     * Extract an ATOM, starting at the current position. Updates
     * the internal index to beyond the Atom.
     *
     * @return an Atom
     */
    public String readAtom() {
	return readDelimString(ATOM_CHAR_DELIM);
    }

    /**
     * Extract a string stopping at control characters or any
     * character in delim.
     */
    private String readDelimString(String delim) {
	skipSpaces();

	if (index >= size) // already at end of response
	    return null;

	int b;
	int start = index;
	while (index < size && ((b = (((int)buffer[index])&0xff)) >= ' ') &&
	       delim.indexOf((char)b) < 0 && b != 0x7f)
	    index++;

	return toString(buffer, start, index);
    }

    /**
     * Read a string as an arbitrary sequence of characters,
     * stopping at the delimiter  Used to read part of a
     * response code inside [].
     *
     * @param	delim	the delimiter character
     * @return		the string
     */
    public String readString(char delim) {
	skipSpaces();

	if (index >= size) // already at end of response
	    return null;

	int start = index;
	while (index < size && buffer[index] != delim)
	    index++;

	return toString(buffer, start, index);
    }

    public String[] readStringList() {
	return readStringList(false);
    }

    public String[] readAtomStringList() {
	return readStringList(true);
    }

    private String[] readStringList(boolean atom) {
	skipSpaces();

	if (buffer[index] != '(') { // not what we expected
	    return null;
	}
	index++; // skip '('

	// to handle buggy IMAP servers, we tolerate multiple spaces as
	// well as spaces after the left paren or before the right paren
	List<String> result = new ArrayList<>();
	while (!isNextNonSpace(')')) {
	    String s = atom ? readAtomString() : readString();
	    if (s == null)	// not the expected string or atom
		break;
	    result.add(s);
	}

	return result.toArray(new String[result.size()]);
    }

    /**
     * Extract an integer, starting at the current position. Updates the
     * internal index to beyond the number. Returns -1 if  a number was 
     * not found.
     *
     * @return  a number
     */
    public int readNumber() {
	// Skip leading spaces
	skipSpaces();

        int start = index;
        while (index < size && Character.isDigit((char)buffer[index]))
            index++;

        if (index > start) {
	    try {
		return ASCIIUtility.parseInt(buffer, start, index);
	    } catch (NumberFormatException nex) { }
	}

	return -1;
    }

    /**
     * Extract a long number, starting at the current position. Updates the
     * internal index to beyond the number. Returns -1 if a long number
     * was not found.
     *
     * @return  a long
     */
    public long readLong() {
	// Skip leading spaces
	skipSpaces();

        int start = index;
        while (index < size && Character.isDigit((char)buffer[index]))
            index++;

        if (index > start) {
	    try {
		return ASCIIUtility.parseLong(buffer, start, index);
	    } catch (NumberFormatException nex) { }
	}

	return -1;
    }

    /**
     * Extract a NSTRING, starting at the current position. Return it as
     * a String. The sequence 'NIL' is returned as null
     *
     * NSTRING := QuotedString | Literal | "NIL"
     *
     * @return  a String
     */
    public String readString() {
	return (String)parseString(false, true);
    }

    /**
     * Extract a NSTRING, starting at the current position. Return it as
     * a ByteArrayInputStream. The sequence 'NIL' is returned as null
     *
     * NSTRING := QuotedString | Literal | "NIL"
     *
     * @return  a ByteArrayInputStream
     */
    public ByteArrayInputStream readBytes() {
	ByteArray ba = readByteArray();
	if (ba != null)
	    return ba.toByteArrayInputStream();
	else
	    return null;
    }

    /**
     * Extract a NSTRING, starting at the current position. Return it as
     * a ByteArray. The sequence 'NIL' is returned as null
     *
     * NSTRING := QuotedString | Literal | "NIL"
     *
     * @return  a ByteArray
     */
    public ByteArray readByteArray() {
	/*
	 * Special case, return the data after the continuation uninterpreted.
	 * It's usually a challenge for an AUTHENTICATE command.
	 */
	if (isContinuation()) {
	    skipSpaces();
	    return new ByteArray(buffer, index, size - index);
	}
	return (ByteArray)parseString(false, false);
    }

    /**
     * Extract an ASTRING, starting at the current position
     * and return as a String. An ASTRING can be a QuotedString, a
     * Literal or an Atom (plus ']').
     *
     * Any errors in parsing returns null
     *
     * ASTRING := QuotedString | Literal | 1*ASTRING_CHAR
     *
     * @return a String
     */ 
    public String readAtomString() {
	return (String)parseString(true, true);
    }

    /**
     * Generic parsing routine that can parse out a Quoted-String,
     * Literal or Atom and return the parsed token as a String
     * or a ByteArray. Errors or NIL data will return null.
     */
    private Object parseString(boolean parseAtoms, boolean returnString) {
	byte b;

	// Skip leading spaces
	skipSpaces();
	
	b = buffer[index];
	if (b == '"') { // QuotedString
	    index++; // skip the quote
	    int start = index;
	    int copyto = index;

	    while (index < size && (b = buffer[index]) != '"') {
		if (b == '\\') // skip escaped byte
		    index++;
		if (index != copyto) { // only copy if we need to
		    // Beware: this is a destructive copy. I'm 
		    // pretty sure this is OK, but ... ;>
		    buffer[copyto] = buffer[index];
		}
		copyto++;
		index++;
	    }
	    if (index >= size) {
		// didn't find terminating quote, something is seriously wrong
		//throw new ArrayIndexOutOfBoundsException(
		//		    "index = " + index + ", size = " + size);
		return null;
	    } else
		index++; // skip past the terminating quote

	    if (returnString) 
		return toString(buffer, start, copyto);
	    else
		return new ByteArray(buffer, start, copyto-start);
	} else if (b == '{') { // Literal
	    int start = ++index; // note the start position

	    while (buffer[index] != '}')
		index++;

	    int count = 0;
	    try {
		count = ASCIIUtility.parseInt(buffer, start, index);
	    } catch (NumberFormatException nex) { 
	   	// throw new ParsingException();
		return null;
	    }

	    start = index + 3; // skip "}\r\n"
	    index = start + count; // position index to beyond the literal

	    if (returnString) // return as String
		return toString(buffer, start, start + count);
	    else
	    	return new ByteArray(buffer, start, count);
	} else if (parseAtoms) { // parse as ASTRING-CHARs
	    int start = index;	// track this, so that we can use to
				// creating ByteArrayInputStream below.
	    String s = readDelimString(ASTRING_CHAR_DELIM);
	    if (returnString)
		return s;
	    else  // *very* unlikely
		return new ByteArray(buffer, start, index);
	} else if (b == 'N' || b == 'n') { // the only valid value is 'NIL'
	    index += 3; // skip past NIL
	    return null;
	}
	return null; // Error
    }

    private String toString(byte[] buffer, int start, int end) {
	return utf8 ?
		new String(buffer, start, end - start, StandardCharsets.UTF_8) :
		ASCIIUtility.toString(buffer, start, end);
    }

    public int getType() {
	return type;
    }

    public boolean isContinuation() {
	return ((type & TAG_MASK) == CONTINUATION);
    }

    public boolean isTagged() {
	return ((type & TAG_MASK) == TAGGED);
    }

    public boolean isUnTagged() {
	return ((type & TAG_MASK) == UNTAGGED);
    }

    public boolean isOK() {
	return ((type & TYPE_MASK) == OK);
    }

    public boolean isNO() {
	return ((type & TYPE_MASK) == NO);
    }

    public boolean isBAD() {
	return ((type & TYPE_MASK) == BAD);
    }

    public boolean isBYE() {
	return ((type & TYPE_MASK) == BYE);
    }

    public boolean isSynthetic() {
	return ((type & SYNTHETIC) == SYNTHETIC);
    }

    /**
     * Return the tag, if this is a tagged statement.
     *
     * @return tag of this tagged statement
     */
    public String getTag() {
	return tag;
    }

    /**
     * Return the rest of the response as a string, usually used to
     * return the arbitrary message text after a NO response.
     *
     * @return	the rest of the response
     */
    public String getRest() {
	skipSpaces();
	return toString(buffer, index, size);
    }

    /**
     * Return the exception for a synthetic BYE response.
     *
     * @return	the exception
     * @since	JavaMail 1.5.4
     */
    public Exception getException() {
	return ex;
    }

    /**
     * Reset pointer to beginning of response.
     */
    public void reset() {
	index = pindex;
    }

    @Override
    public String toString() {
	return toString(buffer, 0, size);
    }

}
