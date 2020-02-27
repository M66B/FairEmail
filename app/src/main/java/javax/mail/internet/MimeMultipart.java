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

package javax.mail.internet;

import javax.mail.*;
import javax.activation.*;
import java.util.*;
import java.io.*;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.PropUtil;

/**
 * The MimeMultipart class is an implementation of the abstract Multipart
 * class that uses MIME conventions for the multipart data. <p>
 *
 * A MimeMultipart is obtained from a MimePart whose primary type
 * is "multipart" (by invoking the part's <code>getContent()</code> method)
 * or it can be created by a client as part of creating a new MimeMessage. <p>
 *
 * The default multipart subtype is "mixed".  The other multipart
 * subtypes, such as "alternative", "related", and so on, can be
 * implemented as subclasses of MimeMultipart with additional methods
 * to implement the additional semantics of that type of multipart
 * content. The intent is that service providers, mail JavaBean writers
 * and mail clients will write many such subclasses and their Command
 * Beans, and will install them into the JavaBeans Activation
 * Framework, so that any Jakarta Mail implementation and its clients can
 * transparently find and use these classes. Thus, a MIME multipart
 * handler is treated just like any other type handler, thereby
 * decoupling the process of providing multipart handlers from the
 * Jakarta Mail API. Lacking these additional MimeMultipart subclasses,
 * all subtypes of MIME multipart data appear as MimeMultipart objects. <p>
 *
 * An application can directly construct a MIME multipart object of any
 * subtype by using the <code>MimeMultipart(String subtype)</code>
 * constructor.  For example, to create a "multipart/alternative" object,
 * use <code>new MimeMultipart("alternative")</code>. <p>
 *
 * The <code>mail.mime.multipart.ignoremissingendboundary</code>
 * property may be set to <code>false</code> to cause a
 * <code>MessagingException</code> to be thrown if the multipart
 * data does not end with the required end boundary line.  If this
 * property is set to <code>true</code> or not set, missing end
 * boundaries are not considered an error and the final body part
 * ends at the end of the data. <p>
 *
 * The <code>mail.mime.multipart.ignoremissingboundaryparameter</code>
 * System property may be set to <code>false</code> to cause a
 * <code>MessagingException</code> to be thrown if the Content-Type
 * of the MimeMultipart does not include a <code>boundary</code> parameter.
 * If this property is set to <code>true</code> or not set, the multipart
 * parsing code will look for a line that looks like a bounary line and
 * use that as the boundary separating the parts. <p>
 *
 * The <code>mail.mime.multipart.ignoreexistingboundaryparameter</code>
 * System property may be set to <code>true</code> to cause any boundary
 * to be ignored and instead search for a boundary line in the message
 * as with <code>mail.mime.multipart.ignoremissingboundaryparameter</code>. <p>
 *
 * Normally, when writing out a MimeMultipart that contains no body
 * parts, or when trying to parse a multipart message with no body parts,
 * a <code>MessagingException</code> is thrown.  The MIME spec does not allow
 * multipart content with no body parts.  The
 * <code>mail.mime.multipart.allowempty</code> System property may be set to
 * <code>true</code> to override this behavior.
 * When writing out such a MimeMultipart, a single empty part will be
 * included.  When reading such a multipart, a MimeMultipart will be created
 * with no body parts.
 *
 * @author  John Mani
 * @author  Bill Shannon
 * @author  Max Spivak
 */

public class MimeMultipart extends Multipart {

    /**
     * The DataSource supplying our InputStream.
     */
    protected DataSource ds = null;

    /**
     * Have we parsed the data from our InputStream yet?
     * Defaults to true; set to false when our constructor is
     * given a DataSource with an InputStream that we need to
     * parse.
     */
    protected boolean parsed = true;

    /**
     * Have we seen the final bounary line?
     *
     * @since	JavaMail 1.5
     */
    protected boolean complete = true;

    /**
     * The MIME multipart preamble text, the text that
     * occurs before the first boundary line.
     *
     * @since	JavaMail 1.5
     */
    protected String preamble = null;

    /**
     * Flag corresponding to the "mail.mime.multipart.ignoremissingendboundary"
     * property, set in the {@link #initializeProperties} method called from
     * constructors and the parse method.
     *
     * @since	JavaMail 1.5
     */
    protected boolean ignoreMissingEndBoundary = true;

    /**
     * Flag corresponding to the
     * "mail.mime.multipart.ignoremissingboundaryparameter"
     * property, set in the {@link #initializeProperties} method called from
     * constructors and the parse method.
     *
     * @since	JavaMail 1.5
     */
    protected boolean ignoreMissingBoundaryParameter = true;

    /**
     * Flag corresponding to the
     * "mail.mime.multipart.ignoreexistingboundaryparameter"
     * property, set in the {@link #initializeProperties} method called from
     * constructors and the parse method.
     *
     * @since	JavaMail 1.5
     */
    protected boolean ignoreExistingBoundaryParameter = false;

    /**
     * Flag corresponding to the "mail.mime.multipart.allowempty"
     * property, set in the {@link #initializeProperties} method called from
     * constructors and the parse method.
     *
     * @since	JavaMail 1.5
     */
    protected boolean allowEmpty = false;

    /**
     * Default constructor. An empty MimeMultipart object
     * is created. Its content type is set to "multipart/mixed".
     * A unique boundary string is generated and this string is
     * setup as the "boundary" parameter for the 
     * <code>contentType</code> field. <p>
     *
     * MimeBodyParts may be added later.
     */
    public MimeMultipart() {
	this("mixed");
    }

    /**
     * Construct a MimeMultipart object of the given subtype.
     * A unique boundary string is generated and this string is
     * setup as the "boundary" parameter for the 
     * <code>contentType</code> field.
     * Calls the {@link #initializeProperties} method.<p>
     *
     * MimeBodyParts may be added later.
     *
     * @param	subtype	the MIME content subtype
     */
    public MimeMultipart(String subtype) {
	super();
	/*
	 * Compute a boundary string.
	 */
	String boundary = UniqueValue.getUniqueBoundaryValue();
	ContentType cType = new ContentType("multipart", subtype, null);
	cType.setParameter("boundary", boundary);
	contentType = cType.toString();
	initializeProperties();
    }

    /**
     * Construct a MimeMultipart object of the default "mixed" subtype,
     * and with the given body parts.  More body parts may be added later.
     *
     * @param	parts	the body parts
     * @exception	MessagingException for failures
     * @since	JavaMail 1.5
     */
    public MimeMultipart(BodyPart... parts) throws MessagingException {
	this();
	for (BodyPart bp : parts)
	    super.addBodyPart(bp);
    }

    /**
     * Construct a MimeMultipart object of the given subtype
     * and with the given body parts.  More body parts may be added later.
     *
     * @param	subtype	the MIME content subtype
     * @param	parts	the body parts
     * @exception	MessagingException for failures
     * @since	JavaMail 1.5
     */
    public MimeMultipart(String subtype, BodyPart... parts)
				throws MessagingException {
	this(subtype);
	for (BodyPart bp : parts)
	    super.addBodyPart(bp);
    }

    /**
     * Constructs a MimeMultipart object and its bodyparts from the 
     * given DataSource. <p>
     *
     * This constructor handles as a special case the situation where the
     * given DataSource is a MultipartDataSource object.  In this case, this
     * method just invokes the superclass (i.e., Multipart) constructor
     * that takes a MultipartDataSource object. <p>
     *
     * Otherwise, the DataSource is assumed to provide a MIME multipart 
     * byte stream.  The <code>parsed</code> flag is set to false.  When
     * the data for the body parts are needed, the parser extracts the
     * "boundary" parameter from the content type of this DataSource,
     * skips the 'preamble' and reads bytes till the terminating
     * boundary and creates MimeBodyParts for each part of the stream.
     *
     * @param	ds	DataSource, can be a MultipartDataSource
     * @exception	ParseException for failures parsing the message
     * @exception	MessagingException for other failures
     */
    public MimeMultipart(DataSource ds) throws MessagingException {
	super();

	if (ds instanceof MessageAware) {
	    MessageContext mc = ((MessageAware)ds).getMessageContext();
	    setParent(mc.getPart());
	}

	if (ds instanceof MultipartDataSource) {
	    // ask super to do this for us.
	    setMultipartDataSource((MultipartDataSource)ds);
	    return;
	}

	// 'ds' was not a MultipartDataSource, we have
	// to parse this ourself.
	parsed = false;
	this.ds = ds;
	contentType = ds.getContentType();
    }

    /**
     * Initialize flags that control parsing behavior,
     * based on System properties described above in
     * the class documentation.
     *
     * @since	JavaMail 1.5
     */
    protected void initializeProperties() {
	// read properties that control parsing

	// default to true
	ignoreMissingEndBoundary = PropUtil.getBooleanSystemProperty(
	    "mail.mime.multipart.ignoremissingendboundary", true);
	// default to true
	ignoreMissingBoundaryParameter = PropUtil.getBooleanSystemProperty(
	    "mail.mime.multipart.ignoremissingboundaryparameter", true);
	// default to false
	ignoreExistingBoundaryParameter = PropUtil.getBooleanSystemProperty(
	    "mail.mime.multipart.ignoreexistingboundaryparameter", false);
	// default to false
	allowEmpty = PropUtil.getBooleanSystemProperty(
	    "mail.mime.multipart.allowempty", false);
    }

    /**
     * Set the subtype. This method should be invoked only on a new
     * MimeMultipart object created by the client. The default subtype
     * of such a multipart object is "mixed". <p>
     *
     * @param	subtype		Subtype
     * @exception	MessagingException for failures
     */
    public synchronized void setSubType(String subtype) 
			throws MessagingException {
	ContentType cType = new ContentType(contentType);	
	cType.setSubType(subtype);
	contentType = cType.toString();
    }

    /**
     * Return the number of enclosed BodyPart objects.
     *
     * @return		number of parts
     */
    @Override
    public synchronized int getCount() throws MessagingException {
	parse();
	return super.getCount();
    }

    /**
     * Get the specified BodyPart.  BodyParts are numbered starting at 0.
     *
     * @param index	the index of the desired BodyPart
     * @return		the Part
     * @exception       MessagingException if no such BodyPart exists
     */
    @Override
    public synchronized BodyPart getBodyPart(int index) 
			throws MessagingException {
	parse();
	return super.getBodyPart(index);
    }

    /**
     * Get the MimeBodyPart referred to by the given ContentID (CID). 
     * Returns null if the part is not found.
     *
     * @param  CID 	the ContentID of the desired part
     * @return          the Part
     * @exception	MessagingException for failures
     */
    public synchronized BodyPart getBodyPart(String CID) 
			throws MessagingException {
	parse();

	int count = getCount();
	for (int i = 0; i < count; i++) {
	   MimeBodyPart part = (MimeBodyPart)getBodyPart(i);
	   String s = part.getContentID();
	   if (s != null && s.equals(CID))
		return part;    
	}
	return null;
    }

    /**
     * Remove the specified part from the multipart message.
     * Shifts all the parts after the removed part down one.
     *
     * @param   part	The part to remove
     * @return		true if part removed, false otherwise
     * @exception	MessagingException if no such Part exists
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     */
    @Override
    public boolean removeBodyPart(BodyPart part) throws MessagingException {
	parse();
	return super.removeBodyPart(part);
    }

    /**
     * Remove the part at specified location (starting from 0).
     * Shifts all the parts after the removed part down one.
     *
     * @param   index	Index of the part to remove
     * @exception       IndexOutOfBoundsException if the given index
     *			is out of range.
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception	MessagingException for other failures
     */
    @Override
    public void removeBodyPart(int index) throws MessagingException {
	parse();
	super.removeBodyPart(index);
    }

    /**
     * Adds a Part to the multipart.  The BodyPart is appended to 
     * the list of existing Parts.
     *
     * @param  part  The Part to be appended
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception       MessagingException for other failures
     */
    @Override
    public synchronized void addBodyPart(BodyPart part) 
		throws MessagingException {
	parse();
	super.addBodyPart(part);
    }

    /**
     * Adds a BodyPart at position <code>index</code>.
     * If <code>index</code> is not the last one in the list,
     * the subsequent parts are shifted up. If <code>index</code>
     * is larger than the number of parts present, the
     * BodyPart is appended to the end.
     *
     * @param  part  The BodyPart to be inserted
     * @param  index Location where to insert the part
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception       MessagingException for other failures
     */
    @Override
    public synchronized void addBodyPart(BodyPart part, int index) 
				throws MessagingException {
	parse();
	super.addBodyPart(part, index);
    }

    /**
     * Return true if the final boundary line for this
     * multipart was seen.  When parsing multipart content,
     * this class will (by default) terminate parsing with
     * no error if the end of input is reached before seeing
     * the final multipart boundary line.  In such a case,
     * this method will return false.  (If the System property
     * "mail.mime.multipart.ignoremissingendboundary" is set to
     * false, parsing such a message will instead throw a
     * MessagingException.)
     *
     * @return	true if the final boundary line was seen
     * @exception	MessagingException for failures
     * @since		JavaMail 1.4
     */
    public synchronized boolean isComplete() throws MessagingException {
	parse();
	return complete;
    }

    /**
     * Get the preamble text, if any, that appears before the
     * first body part of this multipart.  Some protocols,
     * such as IMAP, will not allow access to the preamble text.
     *
     * @return		the preamble text, or null if no preamble
     * @exception	MessagingException for failures
     * @since		JavaMail 1.4
     */
    public synchronized String getPreamble() throws MessagingException {
	parse();
	return preamble;
    }

    /**
     * Set the preamble text to be included before the first
     * body part.  Applications should generally not include
     * any preamble text.  In some cases it may be helpful to
     * include preamble text with instructions for users of
     * pre-MIME software.  The preamble text should be complete
     * lines, including newlines.
     *
     * @param	preamble	the preamble text
     * @exception	MessagingException for failures
     * @since		JavaMail 1.4
     */
    public synchronized void setPreamble(String preamble)
				throws MessagingException {
	this.preamble = preamble;
    }

    /**
     * Update headers. The default implementation here just
     * calls the <code>updateHeaders</code> method on each of its
     * children BodyParts. <p>
     *
     * Note that the boundary parameter is already set up when
     * a new and empty MimeMultipart object is created. <p>
     *
     * This method is called when the <code>saveChanges</code>
     * method is invoked on the Message object containing this
     * Multipart. This is typically done as part of the Message
     * send process, however note that a client is free to call
     * it any number of times. So if the header updating process is 
     * expensive for a specific MimeMultipart subclass, then it
     * might itself want to track whether its internal state actually
     * did change, and do the header updating only if necessary.
     *
     * @exception	MessagingException for failures
     */
    protected synchronized void updateHeaders() throws MessagingException {
	parse();
	for (int i = 0; i < parts.size(); i++)
	    ((MimeBodyPart)parts.elementAt(i)).updateHeaders();
    }

    /**
     * Iterates through all the parts and outputs each MIME part
     * separated by a boundary.
     */
    @Override
    public synchronized void writeTo(OutputStream os)
				throws IOException, MessagingException {
	parse();
	
	String boundary = "--" + 
		(new ContentType(contentType)).getParameter("boundary");
	LineOutputStream los = new LineOutputStream(os);

	// if there's a preamble, write it out
	if (preamble != null) {
	    byte[] pb = ASCIIUtility.getBytes(preamble);
	    los.write(pb);
	    // make sure it ends with a newline
	    if (pb.length > 0 &&
		    !(pb[pb.length-1] == '\r' || pb[pb.length-1] == '\n')) {
		los.writeln();
	    }
	    // XXX - could force a blank line before start boundary
	}

	if (parts.size() == 0) {
	    if (allowEmpty) {
		// write out a single empty body part
		los.writeln(boundary); // put out boundary
		los.writeln(); // put out empty line
	    } else {
		throw new MessagingException("Empty multipart: " + contentType);
	    }
	} else {
	    for (int i = 0; i < parts.size(); i++) {
		los.writeln(boundary); // put out boundary
		((MimeBodyPart)parts.elementAt(i)).writeTo(os);
		los.writeln(); // put out empty line
	    }
	}

	// put out last boundary
	los.writeln(boundary + "--");
    }

    /**
     * Parse the InputStream from our DataSource, constructing the
     * appropriate MimeBodyParts.  The <code>parsed</code> flag is
     * set to true, and if true on entry nothing is done.  This
     * method is called by all other methods that need data for
     * the body parts, to make sure the data has been parsed.
     * The {@link #initializeProperties} method is called before
     * parsing the data.
     *
     * @exception	ParseException for failures parsing the message
     * @exception	MessagingException for other failures
     * @since	JavaMail 1.2
     */
    protected synchronized void parse() throws MessagingException {
	if (parsed)
	    return;

	initializeProperties();

	InputStream in = null;
	SharedInputStream sin = null;
	long start = 0, end = 0;

	try {
	    in = ds.getInputStream();
	    if (!(in instanceof ByteArrayInputStream) &&
		!(in instanceof BufferedInputStream) &&
		!(in instanceof SharedInputStream))
		in = new BufferedInputStream(in);
	} catch (Exception ex) {
	    throw new MessagingException("No inputstream from datasource", ex);
	}
	if (in instanceof SharedInputStream)
	    sin = (SharedInputStream)in;

	ContentType cType = new ContentType(contentType);
	String boundary = null;
	if (!ignoreExistingBoundaryParameter) {
	    String bp = cType.getParameter("boundary");
	    if (bp != null)
		boundary = "--" + bp;
	}
	if (boundary == null && !ignoreMissingBoundaryParameter &&
		!ignoreExistingBoundaryParameter)
	    throw new ParseException("Missing boundary parameter");

	try {
	    // Skip and save the preamble
	    LineInputStream lin = new LineInputStream(in);
	    StringBuilder preamblesb = null;
	    String line;
	    while ((line = lin.readLine()) != null) {
		/*
		 * Strip trailing whitespace.  Can't use trim method
		 * because it's too aggressive.  Some bogus MIME
		 * messages will include control characters in the
		 * boundary string.
		 */
		int i;
		for (i = line.length() - 1; i >= 0; i--) {
		    char c = line.charAt(i);
		    if (!(c == ' ' || c == '\t'))
			break;
		}
		line = line.substring(0, i + 1);
		if (boundary != null) {
		    if (line.equals(boundary))
			break;
		    if (line.length() == boundary.length() + 2 &&
			    line.startsWith(boundary) && line.endsWith("--")) {
			line = null;	// signal end of multipart
			break;
		    }
		} else {
		    /*
		     * Boundary hasn't been defined, does this line
		     * look like a boundary?  If so, assume it is
		     * the boundary and save it.
		     */
		    if (line.length() > 2 && line.startsWith("--")) {
			if (line.length() > 4 && allDashes(line)) {
			    /*
			     * The first boundary-like line we find is
			     * probably *not* the end-of-multipart boundary
			     * line.  More likely it's a line full of dashes
			     * in the preamble text.  Just keep reading.
			     */
			} else {
			    boundary = line;
			    break;
			}
		    }
		}

		// save the preamble after skipping blank lines
		if (line.length() > 0) {
		    // accumulate the preamble
		    if (preamblesb == null)
			preamblesb = new StringBuilder(line.length() + 2);
		    preamblesb.append(line).append(System.lineSeparator());
		}
	    }

	    if (preamblesb != null)
		preamble = preamblesb.toString();

	    if (line == null) {
		if (allowEmpty)
		    return;
		else
		    throw new ParseException("Missing start boundary");
	    }

	    // save individual boundary bytes for comparison later
	    byte[] bndbytes = ASCIIUtility.getBytes(boundary);
	    int bl = bndbytes.length;

	    /*
	     * Compile Boyer-Moore parsing tables.
	     */

	    // initialize Bad Character Shift table
	    int[] bcs = new int[256];
	    for (int i = 0; i < bl; i++)
		bcs[bndbytes[i] & 0xff] = i + 1;

	    // initialize Good Suffix Shift table
	    int[] gss = new int[bl];
	NEXT:
	    for (int i = bl; i > 0; i--) {
		int j;	// the beginning index of the suffix being considered
		for (j = bl - 1; j >= i; j--) {
		    // Testing for good suffix
		    if (bndbytes[j] == bndbytes[j - i]) {
			// bndbytes[j..len] is a good suffix
			gss[j - 1] = i;
		    } else {
		       // No match. The array has already been
		       // filled up with correct values before.
		       continue NEXT;
		    }
		}
		while (j > 0)
		    gss[--j] = i;
	    }
	    gss[bl - 1] = 1;
 
	    /*
	     * Read and process body parts until we see the
	     * terminating boundary line (or EOF).
	     */
	    boolean done = false;
	getparts:
	    while (!done) {
		InternetHeaders headers = null;
		if (sin != null) {
		    start = sin.getPosition();
		    // skip headers
		    while ((line = lin.readLine()) != null && line.length() > 0)
			;
		    if (line == null) {
			if (!ignoreMissingEndBoundary)
			    throw new ParseException(
					"missing multipart end boundary");
			// assume there's just a missing end boundary
			complete = false;
			break getparts;
		    }
		} else {
		    // collect the headers for this body part
		    headers = createInternetHeaders(in);
		}

		if (!in.markSupported())
		    throw new MessagingException("Stream doesn't support mark");

		ByteArrayOutputStream buf = null;
		// if we don't have a shared input stream, we copy the data
		if (sin == null)
		    buf = new ByteArrayOutputStream();
		else
		    end = sin.getPosition();
		int b;

		/*
		 * These buffers contain the bytes we're checking
		 * for a match.  inbuf is the current buffer and
		 * previnbuf is the previous buffer.  We need the
		 * previous buffer to check that we're preceeded
		 * by an EOL.
		 */
		// XXX - a smarter algorithm would use a sliding window
		//	 over a larger buffer
		byte[] inbuf = new byte[bl];
		byte[] previnbuf = new byte[bl];
		int inSize = 0;		// number of valid bytes in inbuf
		int prevSize = 0;	// number of valid bytes in previnbuf
		int eolLen;
		boolean first = true;

		/*
		 * Read and save the content bytes in buf.
		 */
		for (;;) {
		    in.mark(bl + 4 + 1000); // bnd + "--\r\n" + lots of LWSP
		    eolLen = 0;
		    inSize = readFully(in, inbuf, 0, bl);
		    if (inSize < bl) {
			// hit EOF
			if (!ignoreMissingEndBoundary)
			    throw new ParseException(
					"missing multipart end boundary");
			if (sin != null)
			    end = sin.getPosition();
			complete = false;
			done = true;
			break;
		    }
		    // check whether inbuf contains a boundary string
		    int i;
		    for (i = bl - 1; i >= 0; i--) {
			if (inbuf[i] != bndbytes[i])
			    break;
		    }
		    if (i < 0) {	// matched all bytes
			eolLen = 0;
			if (!first) {
			    // working backwards, find out if we were preceeded
			    // by an EOL, and if so find its length
			    b = previnbuf[prevSize - 1];
			    if (b == '\r' || b == '\n') {
				eolLen = 1;
				if (b == '\n' && prevSize >= 2) {
				    b = previnbuf[prevSize - 2];
				    if (b == '\r')
					eolLen = 2;
				}
			    }
			}
			if (first || eolLen > 0) {	// yes, preceed by EOL
			    if (sin != null) {
				// update "end", in case this really is
				// a valid boundary
				end = sin.getPosition() - bl - eolLen;
			    }
			    // matched the boundary, check for last boundary
			    int b2 = in.read();
			    if (b2 == '-') {
				if (in.read() == '-') {
				    complete = true;
				    done = true;
				    break;	// ignore trailing text
				}
			    }
			    // skip linear whitespace
			    while (b2 == ' ' || b2 == '\t')
				b2 = in.read();
			    // check for end of line
			    if (b2 == '\n')
				break;	// got it!  break out of the loop
			    if (b2 == '\r') {
				in.mark(1);
				if (in.read() != '\n')
				    in.reset();
				break;	// got it!  break out of the loop
			    }
			}
			i = 0;
		    }

		    /*
		     * Get here if boundary didn't match,
		     * wasn't preceeded by EOL, or wasn't
		     * followed by whitespace or EOL.
		     */

		    // compute how many bytes we can skip
		    int skip = Math.max(i + 1 - bcs[inbuf[i] & 0x7f], gss[i]);
		    // want to keep at least two characters
		    if (skip < 2) {
			// only skipping one byte, save one byte
			// from previous buffer as well
			// first, write out bytes we're done with
			if (sin == null && prevSize > 1)
			    buf.write(previnbuf, 0, prevSize - 1);
			in.reset();
			skipFully(in, 1);
			if (prevSize >= 1) {	// is there a byte to save?
			    // yes, save one from previous and one from current
			    previnbuf[0] = previnbuf[prevSize - 1];
			    previnbuf[1] = inbuf[0];
			    prevSize = 2;
			} else {
			    // no previous bytes to save, can only save current
			    previnbuf[0] = inbuf[0];
			    prevSize = 1;
			}
		    } else {
			// first, write out data from previous buffer before
			// we dump it
			if (prevSize > 0 && sin == null)
			    buf.write(previnbuf, 0, prevSize);
			// all the bytes we're skipping are saved in previnbuf
			prevSize = skip;
			in.reset();
			skipFully(in, prevSize);
			// swap buffers
			byte[] tmp = inbuf;
			inbuf = previnbuf;
			previnbuf = tmp;
		    }
		    first = false;
		}

		/*
		 * Create a MimeBody element to represent this body part.
		 */
		MimeBodyPart part;
		if (sin != null) {
		    part = createMimeBodyPartIs(sin.newStream(start, end));
		} else {
		    // write out data from previous buffer, not including EOL
		    if (prevSize - eolLen > 0)
			buf.write(previnbuf, 0, prevSize - eolLen);
		    // if we didn't find a trailing boundary,
		    // the current buffer has data we need too
		    if (!complete && inSize > 0)
			buf.write(inbuf, 0, inSize);
		    part = createMimeBodyPart(headers, buf.toByteArray());
		}
		super.addBodyPart(part);
	    }
	} catch (IOException ioex) {
	    throw new MessagingException("IO Error", ioex);
	} finally {
	    try {
		in.close();
	    } catch (IOException cex) {
		// ignore
	    }
	}

	parsed = true;
    }

    /**
     * Is the string all dashes ('-')?
     */
    private static boolean allDashes(String s) {
	for (int i = 0; i < s.length(); i++) {
	    if (s.charAt(i) != '-')
		return false;
	}
	return true;
    }

    /**
     * Read data from the input stream to fill the buffer starting
     * at the specified offset with the specified number of bytes.
     * If len is zero, return zero.  If at EOF, return -1.  Otherwise,
     * return the number of bytes read.  Call the read method on the
     * input stream as many times as necessary to read len bytes.
     *
     * @param	in	InputStream to read from
     * @param	buf	buffer to read into
     * @param	off	offset in the buffer for first byte
     * @param	len	number of bytes to read
     * @return		-1 on EOF, otherwise number of bytes read
     * @exception	IOException	on I/O errors
     */
    private static int readFully(InputStream in, byte[] buf, int off, int len)
				throws IOException {
	if (len == 0)
	    return 0;
	int total = 0;
	while (len > 0) {
	    int bsize = in.read(buf, off, len);
	    if (bsize <= 0)	// should never be zero
		break;
	    off += bsize;
	    total += bsize;
	    len -= bsize;
	}
	return total > 0 ? total : -1;
    }

    /**
     * Skip the specified number of bytes, repeatedly calling
     * the skip method as necessary.
     */
    private void skipFully(InputStream in, long offset) throws IOException {
	while (offset > 0) {
	    long cur = in.skip(offset);
	    if (cur <= 0)
		throw new EOFException("can't skip");
	    offset -= cur;
	}
    }

    /**
     * Create and return an InternetHeaders object that loads the
     * headers from the given InputStream.  Subclasses can override
     * this method to return a subclass of InternetHeaders, if
     * necessary.  This implementation simply constructs and returns
     * an InternetHeaders object.
     *
     * @param	is	the InputStream to read the headers from
     * @return	an InternetHeaders object
     * @exception  	MessagingException for failures
     * @since		JavaMail 1.2
     */
    protected InternetHeaders createInternetHeaders(InputStream is)
				throws MessagingException {
	return new InternetHeaders(is);
    }

    /**
     * Create and return a MimeBodyPart object to represent a
     * body part parsed from the InputStream.  Subclasses can override
     * this method to return a subclass of MimeBodyPart, if
     * necessary.  This implementation simply constructs and returns
     * a MimeBodyPart object.
     *
     * @param	headers		the headers for the body part
     * @param	content		the content of the body part
     * @return	a MimeBodyPart
     * @exception  		MessagingException for failures
     * @since			JavaMail 1.2
     */
    protected MimeBodyPart createMimeBodyPart(InternetHeaders headers,
				byte[] content) throws MessagingException {
	return new MimeBodyPart(headers, content);
    }

    /**
     * Create and return a MimeBodyPart object to represent a
     * body part parsed from the InputStream.  Subclasses can override
     * this method to return a subclass of MimeBodyPart, if
     * necessary.  This implementation simply constructs and returns
     * a MimeBodyPart object.
     *
     * @param	is		InputStream containing the body part
     * @return	a MimeBodyPart
     * @exception  		MessagingException for failures
     * @since			JavaMail 1.2
     */
    protected MimeBodyPart createMimeBodyPart(InputStream is)
				throws MessagingException {
	return new MimeBodyPart(is);
    }

    private MimeBodyPart createMimeBodyPartIs(InputStream is)
				throws MessagingException {
	try {
	    return createMimeBodyPart(is);
	} finally {
	    try {
		is.close();
	    } catch (IOException ex) {
		// ignore it
	    }
	}
    }
}
