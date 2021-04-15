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

package com.sun.mail.mbox;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.sun.mail.util.LineInputStream;

/**
 * The SunV3Multipart class is an implementation of the abstract Multipart
 * class that uses SunV3 conventions for the multipart data. <p>
 *
 * @author  Bill Shannon
 */

public class SunV3Multipart extends MimeMultipart {
    private boolean parsing;

    /**
     * Constructs a SunV3Multipart object and its bodyparts from the 
     * given DataSource. <p>
     *
     * @param	ds	DataSource, can be a MultipartDataSource
     */
    public SunV3Multipart(DataSource ds) throws MessagingException {
	super(ds);
    }

    /**
     * Set the subtype.  Throws MethodNotSupportedException.
     *
     * @param	subtype		Subtype
     */
    public void setSubType(String subtype) throws MessagingException {
	throw new MethodNotSupportedException(
		"can't change SunV3Multipart subtype");
    }

    /**
     * Get the BodyPart referred to by the given ContentID (CID). 
     * Throws MethodNotSupportException.
     */
    public synchronized BodyPart getBodyPart(String CID) 
			throws MessagingException {
	throw new MethodNotSupportedException(
		"SunV3Multipart doesn't support Content-ID");
    }

    /**
     * Update headers.  Throws MethodNotSupportException.
     */
    protected void updateHeaders() throws MessagingException {
	throw new MethodNotSupportedException("SunV3Multipart not writable");
    }

    /**
     * Iterates through all the parts and outputs each SunV3 part
     * separated by a boundary.
     */
    public void writeTo(OutputStream os)
				throws IOException, MessagingException {
	throw new MethodNotSupportedException(
		"SunV3Multipart writeTo not supported");
    }

    private static final String boundary = "----------";

    /*
     * Parse the contents of this multipart message and create the
     * child body parts.
     */
    protected synchronized void parse() throws MessagingException {
	/*
	 * If the data has already been parsed, or we're in the middle of
	 * parsing it, there's nothing to do.  The latter will occur when
	 * we call addBodyPart, which will call parse again.  We really
	 * want to be able to call super.super.addBodyPart.
	 */
	if (parsed || parsing)
	    return;

	InputStream in = null;

	try {
	    in = ds.getInputStream();
	    if (!(in instanceof ByteArrayInputStream) &&
		!(in instanceof BufferedInputStream))
		in = new BufferedInputStream(in);
	} catch (IOException ex) {
	    throw new MessagingException("No inputstream from datasource");
	} catch (RuntimeException ex) {
	    throw new MessagingException("No inputstream from datasource");
	}

	byte[] bndbytes = boundary.getBytes(StandardCharsets.ISO_8859_1);
	int bl = bndbytes.length;

	String line;
	parsing = true;
	try {
	    /*
	     * Skip any kind of junk until we get to the first
	     * boundary line.
	     */
	    LineInputStream lin = new LineInputStream(in);
	    while ((line = lin.readLine()) != null) {
		if (line.trim().equals(boundary))
		    break;
	    }
	    if (line == null)
		throw new MessagingException("Missing start boundary");

	    /*
	     * Read and process body parts until we see the
	     * terminating boundary line (or EOF).
	     */
	    for (;;) {
		/*
		 * Collect the headers for this body part.
		 */
		InternetHeaders headers = new InternetHeaders(in);

		if (!in.markSupported())
		    throw new MessagingException("Stream doesn't support mark");

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int b;

		/*
		 * Read and save the content bytes in buf.
		 */
		while ((b = in.read()) >= 0) {
		    if (b == '\r' || b == '\n') {
			/*
			 * Found the end of a line, check whether the
			 * next line is a boundary.
			 */
			int i;
			in.mark(bl + 4 + 1);	// "4" for possible "--\r\n"
			if (b == '\r' && in.read() != '\n') {
			    in.reset();
			    in.mark(bl + 4);
			}
			// read bytes, matching against the boundary
			for (i = 0; i < bl; i++)
			    if (in.read() != bndbytes[i])
				break;
			if (i == bl) {
			    int b2 = in.read();
			    // check for end of line
			    if (b2 == '\n')
				break;	// got it!  break out of the while loop
			    if (b2 == '\r') {
				in.mark(1);
				if (in.read() != '\n')
				    in.reset();
				break;	// got it!  break out of the while loop
			    }
			}
			// failed to match, reset and proceed normally
			in.reset();
		    }
		    buf.write(b);
		}

		/*
		 * Create a SunV3BodyPart to represent this body part.
		 */
		SunV3BodyPart body =
			new SunV3BodyPart(headers, buf.toByteArray());
		addBodyPart(body);
		if (b < 0)
		    break;
	    }
	} catch (IOException e) {
	    throw new MessagingException("IO Error");	// XXX
	} finally {
	    parsing = false;
	}

	parsed = true;
    }
}
