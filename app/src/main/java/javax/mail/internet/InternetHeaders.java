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

import java.io.*;
import java.util.*;
import javax.mail.*;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.PropUtil;

/**
 * InternetHeaders is a utility class that manages RFC822 style
 * headers. Given an RFC822 format message stream, it reads lines
 * until the blank line that indicates end of header. The input stream
 * is positioned at the start of the body. The lines are stored 
 * within the object and can be extracted as either Strings or
 * {@link javax.mail.Header} objects. <p>
 *
 * This class is mostly intended for service providers. MimeMessage
 * and MimeBody use this class for holding their headers.
 * 
 * <hr> <strong>A note on RFC822 and MIME headers</strong><p>
 *
 * RFC822 and MIME header fields <strong>must</strong> contain only 
 * US-ASCII characters. If a header contains non US-ASCII characters,
 * it must be encoded as per the rules in RFC 2047. The MimeUtility
 * class provided in this package can be used to to achieve this. 
 * Callers of the <code>setHeader</code>, <code>addHeader</code>, and
 * <code>addHeaderLine</code> methods are responsible for enforcing
 * the MIME requirements for the specified headers.  In addition, these
 * header fields must be folded (wrapped) before being sent if they
 * exceed the line length limitation for the transport (1000 bytes for
 * SMTP).  Received headers may have been folded.  The application is
 * responsible for folding and unfolding headers as appropriate. <p>
 *
 * The current implementation supports the System property
 * <code>mail.mime.ignorewhitespacelines</code>, which if set to true
 * will cause a line containing only whitespace to be considered
 * a blank line terminating the header.
 *
 * @see	javax.mail.internet.MimeUtility
 * @author John Mani
 * @author Bill Shannon
 */

public class InternetHeaders {
    private static final boolean ignoreWhitespaceLines =
	PropUtil.getBooleanSystemProperty("mail.mime.ignorewhitespacelines",
					    false);

    /**
     * An individual internet header.  This class is only used by
     * subclasses of InternetHeaders. <p>
     *
     * An InternetHeader object with a null value is used as a placeholder
     * for headers of that name, to preserve the order of headers.
     * A placeholder InternetHeader object with a name of ":" marks
     * the location in the list of headers where new headers are
     * added by default.
     *
     * @since	JavaMail 1.4
     */
    protected static final class InternetHeader extends Header {
	/*
	 * Note that the value field from the superclass
	 * isn't used in this class.  We extract the value
	 * from the line field as needed.  We store the line
	 * rather than just the value to ensure that we can
	 * get back the exact original line, with the original
	 * whitespace, etc.
	 */
	String line;    // the entire RFC822 header "line",
			// or null if placeholder

	/**
	 * Constructor that takes a line and splits out
	 * the header name.
	 *
	 * @param	l	the header line
	 */
	public InternetHeader(String l) {
	    super("", "");	// XXX - we'll change it later
	    int i = l.indexOf(':');
	    if (i < 0) {
		// should never happen
		name = l.trim();
	    } else {
		name = l.substring(0, i).trim();
	    }
	    line = l;
	}

	/**
	 * Constructor that takes a header name and value.
	 *
	 * @param	n	the name of the header
	 * @param	v	the value of the header
	 */
	public InternetHeader(String n, String v) {
	    super(n, "");
	    if (v != null)
		line = n + ": " + v;
	    else
		line = null;
	}

	/**
	 * Return the "value" part of the header line.
	 */
	@Override
	public String getValue() {
	    int i = line.indexOf(':');
	    if (i < 0)
		return line;
	    // skip whitespace after ':'
	    int j;
	    for (j = i + 1; j < line.length(); j++) {
		char c = line.charAt(j);
		if (!(c == ' ' || c == '\t' || c == '\r' || c == '\n'))
		    break;
	    }
	    return line.substring(j);
	}
    }

    /*
     * The enumeration object used to enumerate an
     * InternetHeaders object.  Can return
     * either a String or a Header object.
     */
    static class MatchEnum {
	private Iterator<InternetHeader> e;	// enum object of headers List
	// XXX - is this overkill?  should we step through in index
	// order instead?
	private String names[];	// names to match, or not
	private boolean match;	// return matching headers?
	private boolean want_line;	// return header lines?
	private InternetHeader next_header; // the next header to be returned

	/*
	 * Constructor.  Initialize the enumeration for the entire
	 * List of headers, the set of headers, whether to return
	 * matching or non-matching headers, and whether to return
	 * header lines or Header objects.
	 */
	MatchEnum(List<InternetHeader> v, String n[], boolean m, boolean l) {
	    e = v.iterator();
	    names = n;
	    match = m;
	    want_line = l;
	    next_header = null;
	}

	/*
	 * Any more elements in this enumeration?
	 */
	public boolean hasMoreElements() {
	    // if necessary, prefetch the next matching header,
	    // and remember it.
	    if (next_header == null)
		next_header = nextMatch();
	    return next_header != null;
	}

	/*
	 * Return the next element.
	 */
	public Object nextElement() {
	    if (next_header == null)
		next_header = nextMatch();

	    if (next_header == null)
		throw new NoSuchElementException("No more headers");

	    InternetHeader h = next_header;
	    next_header = null;
	    if (want_line)
		return h.line;
	    else
		return new Header(h.getName(), h.getValue());
	}

	/*
	 * Return the next Header object according to the match
	 * criteria, or null if none left.
	 */
	private InternetHeader nextMatch() {
	    next:
	    while (e.hasNext()) {
		InternetHeader h = e.next();

		// skip "place holder" headers
		if (h.line == null)
		    continue;

		// if no names to match against, return appropriately
		if (names == null)
		    return match ? null : h;

		// check whether this header matches any of the names
		for (int i = 0; i < names.length; i++) {
		    if (names[i].equalsIgnoreCase(h.getName())) {
			if (match)
			    return h;
			else
			    // found a match, but we're
			    // looking for non-matches.
			    // try next header.
			    continue next;
		    }
		}
		// found no matches.  if that's what we wanted, return it.
		if (!match)
		    return h;
	    }
	    return null;
	}
    }

    static class MatchStringEnum extends MatchEnum
	    implements Enumeration<String> {

	MatchStringEnum(List<InternetHeader> v, String[] n, boolean m) {
	    super(v, n, m, true);
	}

	@Override
	public String nextElement() {
	    return (String) super.nextElement();
	}

    }

    static class MatchHeaderEnum extends MatchEnum
	    implements Enumeration<Header> {

	MatchHeaderEnum(List<InternetHeader> v, String[] n, boolean m) {
	    super(v, n, m, false);
	}

	@Override
	public Header nextElement() {
	    return (Header) super.nextElement();
	}

    }

    /**
     * The actual list of Headers, including placeholder entries.
     * Placeholder entries are Headers with a null value and
     * are never seen by clients of the InternetHeaders class.
     * Placeholder entries are used to keep track of the preferred
     * order of headers.  Headers are never actually removed from
     * the list, they're converted into placeholder entries.
     * New headers are added after existing headers of the same name
     * (or before in the case of <code>Received</code> and
     * <code>Return-Path</code> headers).  If no existing header
     * or placeholder for the header is found, new headers are
     * added after the special placeholder with the name ":".
     *
     * @since	JavaMail 1.4
     */
    protected List<InternetHeader> headers;

    /**
     * Create an empty InternetHeaders object.  Placeholder entries
     * are inserted to indicate the preferred order of headers.
     */
    public InternetHeaders() { 
   	headers = new ArrayList<>(40); 
	headers.add(new InternetHeader("Return-Path", null));
	headers.add(new InternetHeader("Received", null));
	headers.add(new InternetHeader("Resent-Date", null));
	headers.add(new InternetHeader("Resent-From", null));
	headers.add(new InternetHeader("Resent-Sender", null));
	headers.add(new InternetHeader("Resent-To", null));
	headers.add(new InternetHeader("Resent-Cc", null));
	headers.add(new InternetHeader("Resent-Bcc", null));
	headers.add(new InternetHeader("Resent-Message-Id", null));
	headers.add(new InternetHeader("Date", null));
	headers.add(new InternetHeader("From", null));
	headers.add(new InternetHeader("Sender", null));
	headers.add(new InternetHeader("Reply-To", null));
	headers.add(new InternetHeader("To", null));
	headers.add(new InternetHeader("Cc", null));
	headers.add(new InternetHeader("Bcc", null));
	headers.add(new InternetHeader("Message-Id", null));
	headers.add(new InternetHeader("In-Reply-To", null));
	headers.add(new InternetHeader("References", null));
	headers.add(new InternetHeader("Subject", null));
	headers.add(new InternetHeader("Comments", null));
	headers.add(new InternetHeader("Keywords", null));
	headers.add(new InternetHeader("Errors-To", null));
	headers.add(new InternetHeader("MIME-Version", null));
	headers.add(new InternetHeader("Content-Type", null));
	headers.add(new InternetHeader("Content-Transfer-Encoding", null));
	headers.add(new InternetHeader("Content-MD5", null));
	headers.add(new InternetHeader(":", null));
	headers.add(new InternetHeader("Content-Length", null));
	headers.add(new InternetHeader("Status", null));
    }

    /**
     * Read and parse the given RFC822 message stream till the 
     * blank line separating the header from the body. The input 
     * stream is left positioned at the start of the body. The 
     * header lines are stored internally. <p>
     *
     * For efficiency, wrap a BufferedInputStream around the actual
     * input stream and pass it as the parameter. <p>
     *
     * No placeholder entries are inserted; the original order of
     * the headers is preserved.
     *
     * @param	is 	RFC822 input stream
     * @exception	MessagingException for any I/O error reading the stream
     */
    public InternetHeaders(InputStream is) throws MessagingException {
	this(is, false);
    }

    /**
     * Read and parse the given RFC822 message stream till the 
     * blank line separating the header from the body. The input 
     * stream is left positioned at the start of the body. The 
     * header lines are stored internally. <p>
     *
     * For efficiency, wrap a BufferedInputStream around the actual
     * input stream and pass it as the parameter. <p>
     *
     * No placeholder entries are inserted; the original order of
     * the headers is preserved.
     *
     * @param	is 	RFC822 input stream
     * @param	allowutf8 	if UTF-8 encoded headers are allowed
     * @exception	MessagingException for any I/O error reading the stream
     * @since		JavaMail 1.6
     */
    public InternetHeaders(InputStream is, boolean allowutf8)
				throws MessagingException {
   	headers = new ArrayList<>(40); 
	load(is, allowutf8);
    }

    /**
     * Read and parse the given RFC822 message stream till the
     * blank line separating the header from the body. Store the
     * header lines inside this InternetHeaders object. The order
     * of header lines is preserved. <p>
     *
     * Note that the header lines are added into this InternetHeaders
     * object, so any existing headers in this object will not be
     * affected.  Headers are added to the end of the existing list
     * of headers, in order.
     *
     * @param	is 	RFC822 input stream
     * @exception	MessagingException for any I/O error reading the stream
     */
    public void load(InputStream is) throws MessagingException {
	load(is, false);
    }

    /**
     * Read and parse the given RFC822 message stream till the
     * blank line separating the header from the body. Store the
     * header lines inside this InternetHeaders object. The order
     * of header lines is preserved. <p>
     *
     * Note that the header lines are added into this InternetHeaders
     * object, so any existing headers in this object will not be
     * affected.  Headers are added to the end of the existing list
     * of headers, in order.
     *
     * @param	is 	RFC822 input stream
     * @param	allowutf8 	if UTF-8 encoded headers are allowed
     * @exception	MessagingException for any I/O error reading the stream
     * @since		JavaMail 1.6
     */
    public void load(InputStream is, boolean allowutf8)
				throws MessagingException {
	// Read header lines until a blank line. It is valid
	// to have BodyParts with no header lines.
	String line;
	LineInputStream lis = new LineInputStream(is, allowutf8);
	String prevline = null;	// the previous header line, as a string
	// a buffer to accumulate the header in, when we know it's needed
	StringBuilder lineBuffer = new StringBuilder();

	try {
	    // if the first line being read is a continuation line,
	    // we ignore it if it's otherwise empty or we treat it as
	    // a non-continuation line if it has non-whitespace content
	    boolean first = true;
	    do {
		line = lis.readLine();
		if (line != null &&
			(line.startsWith(" ") || line.startsWith("\t"))) {
		    // continuation of header
		    if (prevline != null) {
			lineBuffer.append(prevline);
			prevline = null;
		    }
		    if (first) {
			String lt = line.trim();
			if (lt.length() > 0)
			    lineBuffer.append(lt);
		    } else {
			if (lineBuffer.length() > 0)
			    lineBuffer.append("\r\n");
			lineBuffer.append(line);
		    }
		} else {
		    // new header
		    if (prevline != null)
			addHeaderLine(prevline);
		    else if (lineBuffer.length() > 0) {
			// store previous header first
			addHeaderLine(lineBuffer.toString());
			lineBuffer.setLength(0);
		    }
		    prevline = line;
		}
		first = false;
	    } while (line != null && !isEmpty(line));
	} catch (IOException ioex) {
	    throw new MessagingException("Error in input stream", ioex);
	}
    }

    /**
     * Is this line an empty (blank) line?
     */
    private static final boolean isEmpty(String line) {
	return line.length() == 0 ||
	    (ignoreWhitespaceLines && line.trim().length() == 0);
    }

    /**
     * Return all the values for the specified header. The
     * values are String objects.  Returns <code>null</code>
     * if no headers with the specified name exist.
     *
     * @param	name 	header name
     * @return		array of header values, or null if none
     */
    public String[] getHeader(String name) {
	Iterator<InternetHeader> e = headers.iterator();
	// XXX - should we just step through in index order?
	List<String> v = new ArrayList<>(); // accumulate return values

	while (e.hasNext()) {
	    InternetHeader h = e.next();
	    if (name.equalsIgnoreCase(h.getName()) && h.line != null) {
		v.add(h.getValue());
	    }
	}
	if (v.size() == 0)
	    return (null);
	// convert List to an array for return
	String r[] = new String[v.size()];
	r = v.toArray(r);
	return (r);
    }

    /**
     * Get all the headers for this header name, returned as a single
     * String, with headers separated by the delimiter. If the
     * delimiter is <code>null</code>, only the first header is 
     * returned.  Returns <code>null</code>
     * if no headers with the specified name exist.
     *
     * @param	name 		header name
     * @param   delimiter	delimiter
     * @return                  the value fields for all headers with
     *				this name, or null if none
     */
    public String getHeader(String name, String delimiter) {
	String s[] = getHeader(name);

	if (s == null)
	    return null;
	
	if ((s.length == 1) || delimiter == null)
	    return s[0];
	
	StringBuilder r = new StringBuilder(s[0]);
	for (int i = 1; i < s.length; i++) {
	    r.append(delimiter);
	    r.append(s[i]);
	}
	return r.toString();
    }

    /**
     * Change the first header line that matches name
     * to have value, adding a new header if no existing header
     * matches. Remove all matching headers but the first. <p>
     *
     * Note that RFC822 headers can only contain US-ASCII characters
     *
     * @param	name	header name
     * @param	value	header value
     */
    public void setHeader(String name, String value) {
	boolean found = false;

	for (int i = 0; i < headers.size(); i++) {
	    InternetHeader h = headers.get(i);
	    if (name.equalsIgnoreCase(h.getName())) {
		if (!found) {
		    int j;
		    if (h.line != null && (j = h.line.indexOf(':')) >= 0) {
			h.line = h.line.substring(0, j + 1) + " " + value;
			// preserves capitalization, spacing
		    } else {
			h.line = name + ": " + value;
		    }
		    found = true;
		} else {
		    headers.remove(i);
		    i--;    // have to look at i again
		}
	    }
	}
    
	if (!found) {
	    addHeader(name, value);
	}
    }

    /**
     * Add a header with the specified name and value to the header list. <p>
     *
     * The current implementation knows about the preferred order of most
     * well-known headers and will insert headers in that order.  In
     * addition, it knows that <code>Received</code> headers should be
     * inserted in reverse order (newest before oldest), and that they
     * should appear at the beginning of the headers, preceeded only by
     * a possible <code>Return-Path</code> header.  <p>
     *
     * Note that RFC822 headers can only contain US-ASCII characters.
     *
     * @param	name	header name
     * @param	value	header value
     */ 
    public void addHeader(String name, String value) {
	int pos = headers.size();
	boolean addReverse =
	    name.equalsIgnoreCase("Received") ||
	    name.equalsIgnoreCase("Return-Path");
	if (addReverse)
	    pos = 0;
	for (int i = headers.size() - 1; i >= 0; i--) {
	    InternetHeader h = headers.get(i);
	    if (name.equalsIgnoreCase(h.getName())) {
		if (addReverse) {
		    pos = i;
		} else {
		    headers.add(i + 1, new InternetHeader(name, value));
		    return;
		}
	    }
	    // marker for default place to add new headers
	    if (!addReverse && h.getName().equals(":"))
		pos = i;
	}
	headers.add(pos, new InternetHeader(name, value));
    }

    /**
     * Remove all header entries that match the given name
     * @param	name 	header name
     */
    public void removeHeader(String name) { 
	for (int i = 0; i < headers.size(); i++) {
	    InternetHeader h = headers.get(i);
	    if (name.equalsIgnoreCase(h.getName())) {
		h.line = null;
		//headers.remove(i);
		//i--;    // have to look at i again
	    }
	}
    }

    /**
     * Return all the headers as an Enumeration of
     * {@link javax.mail.Header} objects.
     *
     * @return	Enumeration of Header objects	
     */
    public Enumeration<Header> getAllHeaders() {
	return (new MatchHeaderEnum(headers, null, false));
    }

    /**
     * Return all matching {@link javax.mail.Header} objects.
     *
     * @param	names	the headers to return
     * @return	Enumeration of matching Header objects	
     */
    public Enumeration<Header> getMatchingHeaders(String[] names) {
	return (new MatchHeaderEnum(headers, names, true));
    }

    /**
     * Return all non-matching {@link javax.mail.Header} objects.
     *
     * @param	names	the headers to not return
     * @return	Enumeration of non-matching Header objects	
     */
    public Enumeration<Header> getNonMatchingHeaders(String[] names) {
	return (new MatchHeaderEnum(headers, names, false));
    }

    /**
     * Add an RFC822 header line to the header store.
     * If the line starts with a space or tab (a continuation line),
     * add it to the last header line in the list.  Otherwise,
     * append the new header line to the list.  <p>
     *
     * Note that RFC822 headers can only contain US-ASCII characters
     *
     * @param	line	raw RFC822 header line
     */
    public void addHeaderLine(String line) {
	try {
	    char c = line.charAt(0);
	    if (c == ' ' || c == '\t') {
		InternetHeader h = headers.get(headers.size() - 1);
		h.line += "\r\n" + line;
	    } else
		headers.add(new InternetHeader(line));
	} catch (StringIndexOutOfBoundsException e) {
	    // line is empty, ignore it
	    return;
	} catch (NoSuchElementException e) {
	    // XXX - list is empty?
	}
    }

    /**
     * Return all the header lines as an Enumeration of Strings.
     *
     * @return	Enumeration of Strings of all header lines
     */
    public Enumeration<String> getAllHeaderLines() { 
	return (getNonMatchingHeaderLines(null));
    }

    /**
     * Return all matching header lines as an Enumeration of Strings.
     *
     * @param	names	the headers to return
     * @return	Enumeration of Strings of all matching header lines
     */
    public Enumeration<String> getMatchingHeaderLines(String[] names) {
	return (new MatchStringEnum(headers, names, true));	
    }

    /**
     * Return all non-matching header lines
     *
     * @param	names	the headers to not return
     * @return	Enumeration of Strings of all non-matching header lines
     */
    public Enumeration<String> getNonMatchingHeaderLines(String[] names) {
	return (new MatchStringEnum(headers, names, false));
    }
}
