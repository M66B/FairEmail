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

package javax.mail.internet;

import java.util.*;

/**
 * This class tokenizes RFC822 and MIME headers into the basic
 * symbols specified by RFC822 and MIME. <p>
 *
 * This class handles folded headers (ie headers with embedded
 * CRLF SPACE sequences). The folds are removed in the returned
 * tokens. 
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class HeaderTokenizer {

    /**
     * The Token class represents tokens returned by the 
     * HeaderTokenizer.
     */
    public static class Token {

	private int type;
	private String value;

	/**
	 * Token type indicating an ATOM.
	 */
	public static final int ATOM 		= -1;

	/**
	 * Token type indicating a quoted string. The value 
	 * field contains the string without the quotes.
 	 */
	public static final int QUOTEDSTRING 	= -2;

	/**
	 * Token type indicating a comment. The value field 
	 * contains the comment string without the comment 
	 * start and end symbols.
	 */
	public static final int COMMENT		= -3;

	/**
	 * Token type indicating end of input.
	 */
	public static final int  EOF 		= -4;

	/**
	 * Constructor.
	 * @param	type	Token type
	 * @param	value	Token value
	 */
	public Token(int type, String value) {
	     this.type = type;
	     this.value = value;
	}

	/**
	 * Return the type of the token. If the token represents a
	 * delimiter or a control character, the type is that character
	 * itself, converted to an integer. Otherwise, it's value is 
	 * one of the following:
	 * <ul>
	 * <li><code>ATOM</code> A sequence of ASCII characters 
	 *	delimited by either SPACE, CTL, "(", &lt;"&gt; or the 
	 *	specified SPECIALS
	 * <li><code>QUOTEDSTRING</code> A sequence of ASCII characters
	 *	within quotes
	 * <li><code>COMMENT</code> A sequence of ASCII characters 
	 *	within "(" and ")".
	 * <li><code>EOF</code> End of header
	 * </ul>
	 *
	 * @return	the token type
	 */
	public int getType() {
	    return type;
	}

	/**
	 * Returns the value of the token just read. When the current
	 * token is a quoted string, this field contains the body of the
	 * string, without the quotes. When the current token is a comment,
	 * this field contains the body of the comment.
	 *
	 * @return	token value
	 */
	public String getValue() {
	    return value;
	}
    }

    private String string; // the string to be tokenized
    private boolean skipComments; // should comments be skipped ?
    private String delimiters; // delimiter string
    private int currentPos; // current parse position
    private int maxPos; // string length
    private int nextPos; // track start of next Token for next()
    private int peekPos; // track start of next Token for peek()

    /**
     * RFC822 specials
     */
    public final static String RFC822 = "()<>@,;:\\\"\t .[]";

    /**
     * MIME specials
     */
    public final static String MIME = "()<>@,;:\\\"\t []/?=";

    // The EOF Token
    private final static Token EOFToken = new Token(Token.EOF, null);

    /**
     * Constructor that takes a rfc822 style header.
     *
     * @param	header	The rfc822 header to be tokenized
     * @param	delimiters      Set of delimiter characters 
     *				to be used to delimit ATOMS. These
     *				are usually <code>RFC822</code> or 
     *				<code>MIME</code>
     * @param   skipComments  If true, comments are skipped and
     *				not returned as tokens
     */
    public HeaderTokenizer(String header, String delimiters,
    			   boolean skipComments) {
	string = (header == null) ? "" : header; // paranoia ?!
	this.skipComments = skipComments;
	this.delimiters = delimiters;
	currentPos = nextPos = peekPos = 0;
	maxPos = string.length();
    }

    /**
     * Constructor. Comments are ignored and not returned as tokens
     *
     * @param	header  The header that is tokenized
     * @param	delimiters  The delimiters to be used
     */
    public HeaderTokenizer(String header, String delimiters) {
	this(header, delimiters, true);
    }

    /**
     * Constructor. The RFC822 defined delimiters - RFC822 - are
     * used to delimit ATOMS. Also comments are skipped and not
     * returned as tokens
     *
     * @param	header	the header string
     */
    public HeaderTokenizer(String header)  {
	this(header, RFC822);
    }

    /**
     * Parses the next token from this String. <p>
     *
     * Clients sit in a loop calling next() to parse successive
     * tokens until an EOF Token is returned.
     *
     * @return		the next Token
     * @exception	ParseException if the parse fails
     */
    public Token next() throws ParseException { 
	return next('\0', false);
    }

    /**
     * Parses the next token from this String.
     * If endOfAtom is not NUL, the token extends until the
     * endOfAtom character is seen, or to the end of the header.
     * This method is useful when parsing headers that don't
     * obey the MIME specification, e.g., by failing to quote
     * parameter values that contain spaces.
     *
     * @param	endOfAtom	if not NUL, character marking end of token
     * @return		the next Token
     * @exception	ParseException if the parse fails
     * @since		JavaMail 1.5
     */
    public Token next(char endOfAtom) throws ParseException { 
	return next(endOfAtom, false);
    }

    /**
     * Parses the next token from this String.
     * endOfAtom is handled as above.  If keepEscapes is true,
     * any backslash escapes are preserved in the returned string.
     * This method is useful when parsing headers that don't
     * obey the MIME specification, e.g., by failing to escape
     * backslashes in the filename parameter.
     *
     * @param	endOfAtom	if not NUL, character marking end of token
     * @param	keepEscapes	keep all backslashes in returned string?
     * @return		the next Token
     * @exception	ParseException if the parse fails
     * @since		JavaMail 1.5
     */
    public Token next(char endOfAtom, boolean keepEscapes)
				throws ParseException { 
	Token tk;

	currentPos = nextPos; // setup currentPos
	tk = getNext(endOfAtom, keepEscapes);
	nextPos = peekPos = currentPos; // update currentPos and peekPos
	return tk;
    }

    /**
     * Peek at the next token, without actually removing the token
     * from the parse stream. Invoking this method multiple times
     * will return successive tokens, until <code>next()</code> is
     * called. <p>
     *
     * @return		the next Token
     * @exception	ParseException if the parse fails
     */
    public Token peek() throws ParseException {
	Token tk;

	currentPos = peekPos; // setup currentPos
	tk = getNext('\0', false);
	peekPos = currentPos; // update peekPos
	return tk;
    }

    /**
     * Return the rest of the Header.
     *
     * @return String	rest of header. null is returned if we are
     *			already at end of header
     */
    public String getRemainder() {
	if (nextPos >= string.length())
	    return null;
	return string.substring(nextPos);
    }

    /*
     * Return the next token starting from 'currentPos'. After the
     * parse, 'currentPos' is updated to point to the start of the 
     * next token.
     */
    private Token getNext(char endOfAtom, boolean keepEscapes)
				throws ParseException {
	// If we're already at end of string, return EOF
	if (currentPos >= maxPos)
	    return EOFToken;

	// Skip white-space, position currentPos beyond the space
	if (skipWhiteSpace() == Token.EOF)
	    return EOFToken;

	char c; 
	int start; 
	boolean filter = false;
	
	c = string.charAt(currentPos);

	// Check or Skip comments and position currentPos
	// beyond the comment
	while (c == '(') {
	    // Parsing comment ..
	    int nesting;
	    for (start = ++currentPos, nesting = 1; 
		 nesting > 0 && currentPos < maxPos;
		 currentPos++) {
		c = string.charAt(currentPos);
		if (c == '\\') {  // Escape sequence
		    currentPos++; // skip the escaped character
		    filter = true;
		} else if (c == '\r')
		    filter = true;
		else if (c == '(')
		    nesting++;
		else if (c == ')')
		    nesting--;
	    }
	    if (nesting != 0)
		throw new ParseException("Unbalanced comments");

	    if (!skipComments) {
		// Return the comment, if we are asked to.
		// Note that the comment start & end markers are ignored.
		String s;
		if (filter) // need to go thru the token again.
		    s = filterToken(string, start, currentPos-1, keepEscapes);
		else
		    s = string.substring(start,currentPos-1);

		return new Token(Token.COMMENT, s);
	    }

	    // Skip any whitespace after the comment.
	    if (skipWhiteSpace() == Token.EOF)
		return EOFToken;
	    c = string.charAt(currentPos);
	}

	// Check for quoted-string and position currentPos 
	//  beyond the terminating quote
	if (c == '"') {
	    currentPos++;	// skip initial quote
	    return collectString('"', keepEscapes);
	}
	
	// Check for SPECIAL or CTL
	if (c < 040 || c >= 0177 || delimiters.indexOf(c) >= 0) {
	    if (endOfAtom > 0 && c != endOfAtom) {
		// not expecting a special character here,
		// pretend it's a quoted string
		return collectString(endOfAtom, keepEscapes);
	    }
	    currentPos++; // re-position currentPos
	    char ch[] = new char[1];
	    ch[0] = c;
	    return new Token((int)c, new String(ch));
	}

	// Check for ATOM
	for (start = currentPos; currentPos < maxPos; currentPos++) {
	    c = string.charAt(currentPos);
	    // ATOM is delimited by either SPACE, CTL, "(", <"> 
	    // or the specified SPECIALS
	    if (c < 040 || c >= 0177 || c == '(' || c == ' ' ||
			c == '"' || delimiters.indexOf(c) >= 0) {
		if (endOfAtom > 0 && c != endOfAtom) {
		    // not the expected atom after all;
		    // back up and pretend it's a quoted string
		    currentPos = start;
		    return collectString(endOfAtom, keepEscapes);
		}
		break;
	    }
	}
	return new Token(Token.ATOM, string.substring(start, currentPos));
    }

    private Token collectString(char eos, boolean keepEscapes)
				throws ParseException {
	int start;
	boolean filter = false;
	for (start = currentPos; currentPos < maxPos; currentPos++) {
	    char c = string.charAt(currentPos);
	    if (c == '\\') { // Escape sequence
		currentPos++;
		filter = true;
	    } else if (c == '\r')
		filter = true;
	    else if (c == eos) {
		currentPos++;
		String s;

		if (filter)
		    s = filterToken(string, start, currentPos-1, keepEscapes);
		else
		    s = string.substring(start, currentPos-1);

		if (c != '"') {		// not a real quoted string
		    s = trimWhiteSpace(s);
		    currentPos--;	// back up before the eos char
		}

		return new Token(Token.QUOTEDSTRING, s);
	    }
	}

	// ran off the end of the string

	// if we're looking for a matching quote, that's an error
	if (eos == '"')
	    throw new ParseException("Unbalanced quoted string");

	// otherwise, just return whatever's left
	String s;
	if (filter)
	    s = filterToken(string, start, currentPos, keepEscapes);
	else
	    s = string.substring(start, currentPos);
	s = trimWhiteSpace(s);
	return new Token(Token.QUOTEDSTRING, s);
    }

    // Skip SPACE, HT, CR and NL
    private int skipWhiteSpace() {
	char c;
	for (; currentPos < maxPos; currentPos++)
	    if (((c = string.charAt(currentPos)) != ' ') && 
		(c != '\t') && (c != '\r') && (c != '\n'))
		return currentPos;
	return Token.EOF;
    }

    // Trim SPACE, HT, CR and NL from end of string
    private static String trimWhiteSpace(String s) {
	char c;
	int i;
	for (i = s.length() - 1; i >= 0; i--) {
	    if (((c = s.charAt(i)) != ' ') && 
		(c != '\t') && (c != '\r') && (c != '\n'))
		break;
	}
	if (i <= 0)
	    return "";
	else
	    return s.substring(0, i + 1);
    }

    /* Process escape sequences and embedded LWSPs from a comment or
     * quoted string.
     */
    private static String filterToken(String s, int start, int end,
				boolean keepEscapes) {
	StringBuilder sb = new StringBuilder();
	char c;
	boolean gotEscape = false;
	boolean gotCR = false;

	for (int i = start; i < end; i++) {
	    c = s.charAt(i);
	    if (c == '\n' && gotCR) {
		// This LF is part of an unescaped 
		// CRLF sequence (i.e, LWSP). Skip it.
		gotCR = false;
		continue;
	    }

	    gotCR = false;
	    if (!gotEscape) {
		// Previous character was NOT '\'
		if (c == '\\') // skip this character
		    gotEscape = true;
		else if (c == '\r') // skip this character
		    gotCR = true;
		else // append this character
		    sb.append(c);
	    } else {
		// Previous character was '\'. So no need to 
		// bother with any special processing, just 
		// append this character.  If keepEscapes is
		// set, keep the backslash.  IE6 fails to escape
		// backslashes in quoted strings in HTTP headers,
		// e.g., in the filename parameter.
		if (keepEscapes)
		    sb.append('\\');
		sb.append(c);
		gotEscape = false;
	    }
	}
	return sb.toString();
    }
}
