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
import java.util.Enumeration;
import javax.mail.*;

import com.sun.mail.util.LineOutputStream;

/**
 * A MimeBodyPart that handles data that has already been encoded.
 * This class is useful when constructing a message and attaching
 * data that has already been encoded (for example, using base64
 * encoding).  The data may have been encoded by the application,
 * or may have been stored in a file or database in encoded form.
 * The encoding is supplied when this object is created.  The data
 * is attached to this object in the usual fashion, by using the
 * <code>setText</code>, <code>setContent</code>, or
 * <code>setDataHandler</code> methods.
 *
 * @since	JavaMail 1.4
 */

public class PreencodedMimeBodyPart extends MimeBodyPart {
    private String encoding;

    /**
     * Create a PreencodedMimeBodyPart that assumes the data is
     * encoded using the specified encoding.  The encoding must
     * be a MIME supported Content-Transfer-Encoding.
     *
     * @param	encoding	the Content-Transfer-Encoding
     */
    public PreencodedMimeBodyPart(String encoding) {
	this.encoding = encoding;
    }

    /**
     * Returns the content transfer encoding specified when
     * this object was created.
     */
    @Override
    public String getEncoding() throws MessagingException {
	return encoding;
    }

    /**
     * Output the body part as an RFC 822 format stream.
     *
     * @exception IOException	if an error occurs writing to the
     *				stream or if an error is generated
     *				by the javax.activation layer.
     * @exception MessagingException for other failures
     * @see javax.activation.DataHandler#writeTo
     */
    @Override
    public void writeTo(OutputStream os)
			throws IOException, MessagingException {

	// see if we already have a LOS
	LineOutputStream los = null;
	if (os instanceof LineOutputStream) {
	    los = (LineOutputStream) os;
	} else {
	    los = new LineOutputStream(os);
	}

	// First, write out the header
	Enumeration<String> hdrLines = getAllHeaderLines();
	while (hdrLines.hasMoreElements())
	    los.writeln(hdrLines.nextElement());

	// The CRLF separator between header and content
	los.writeln();

	// Finally, the content, already encoded.
	getDataHandler().writeTo(os);
	os.flush();
    }

    /**
     * Force the <code>Content-Transfer-Encoding</code> header to use
     * the encoding that was specified when this object was created.
     */
    @Override
    protected void updateHeaders() throws MessagingException {
	super.updateHeaders();
	MimeBodyPart.setEncoding(this, encoding);
    }
}
