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
import java.io.*;

/**
 * This class represents a SunV3 BodyPart.
 *
 * @author Bill Shannon
 * @see javax.mail.Part
 * @see javax.mail.internet.MimePart
 * @see javax.mail.internet.MimeBodyPart
 */

public class SunV3BodyPart extends MimeBodyPart {
    /**
     * Constructs a SunV3BodyPart using the given header and
     * content bytes. <p>
     *
     * Used by providers.
     *
     * @param	headers	The header of this part
     * @param	content	bytes representing the body of this part.
     */
    public SunV3BodyPart(InternetHeaders headers, byte[] content) 
			throws MessagingException {
	super(headers, content);
    }

    /**
     * Return the size of the content of this BodyPart in bytes.
     * Return -1 if the size cannot be determined. <p>
     *
     * Note that this number may not be an exact measure of the
     * content size and may or may not account for any transfer
     * encoding of the content. <p>
     *
     * @return size in bytes
     */
    public int getSize() throws MessagingException {
	String s = getHeader("X-Sun-Content-Length", null);
	try {
	    return Integer.parseInt(s);
	} catch (NumberFormatException ex) {
	    return -1;
	}
    }

    /**
     * Return the number of lines for the content of this Part.
     * Return -1 if this number cannot be determined. <p>
     *
     * Note that this number may not be an exact measure of the 
     * content length and may or may not account for any transfer 
     * encoding of the content. 
     */  
     public int getLineCount() throws MessagingException {
	String s = getHeader("X-Sun-Content-Lines", null);
	try {
	    return Integer.parseInt(s);
	} catch (NumberFormatException ex) {
	    return -1;
	}
    }

    /*
     * This is just enough to get us going.
     *
     * For more complete transformation from V3 to MIME, refer to
     * sun_att.c from the Sun IMAP server code.
     */
    static class MimeV3Map {
	String mime;
	String v3;

	MimeV3Map(String mime, String v3) {
	    this.mime = mime;
	    this.v3 = v3;
	}

	private static MimeV3Map[] mimeV3Table = new MimeV3Map[] {
	    new MimeV3Map("text/plain", "text"),
	    new MimeV3Map("text/plain", "default"),
	    new MimeV3Map("multipart/x-sun-attachment", "X-sun-attachment"),
	    new MimeV3Map("application/postscript", "postscript-file"),
	    new MimeV3Map("image/gif", "gif-file")
	    // audio-file
	    // cshell-script
	};

	// V3 Content-Type to MIME Content-Type
	static String toMime(String s) {
	    for (int i = 0; i < mimeV3Table.length; i++) {
		if (mimeV3Table[i].v3.equalsIgnoreCase(s))
		    return mimeV3Table[i].mime;
	    }
	    return "application/x-" + s;
	}

	// MIME Content-Type to V3 Content-Type
	static String toV3(String s) {
	    for (int i = 0; i < mimeV3Table.length; i++) {
		if (mimeV3Table[i].mime.equalsIgnoreCase(s))
		    return mimeV3Table[i].v3;
	    }
	    return s;
	}
    }

    /**
     * Returns the value of the RFC822 "Content-Type" header field.
     * This represents the content-type of the content of this
     * BodyPart. This value must not be null. If this field is
     * unavailable, "text/plain" should be returned. <p>
     *
     * This implementation uses <code>getHeader(name)</code>
     * to obtain the requisite header field.
     *
     * @return	Content-Type of this BodyPart
     */
    public String getContentType() throws MessagingException {
	String ct = getHeader("Content-Type", null);
	if (ct == null)
	    ct = getHeader("X-Sun-Data-Type", null);
	if (ct == null)
	    ct = "text/plain";
	else if (ct.indexOf('/') < 0)
	    ct = MimeV3Map.toMime(ct);
	return ct;
    }

    /**
     * Returns the value of the "Content-Transfer-Encoding" header
     * field. Returns <code>null</code> if the header is unavailable
     * or its value is absent. <p>
     *
     * This implementation uses <code>getHeader(name)</code>
     * to obtain the requisite header field.
     *
     * @see #headers
     */
    public String getEncoding() throws MessagingException {
	String enc = super.getEncoding();
	if (enc == null)
	    enc = getHeader("X-Sun-Encoding-Info", null);
	return enc;
    }

    /**
     * Returns the "Content-Description" header field of this BodyPart.
     * This typically associates some descriptive information with 
     * this part. Returns null if this field is unavailable or its
     * value is absent. <p>
     *
     * If the Content-Description field is encoded as per RFC 2047,
     * it is decoded and converted into Unicode. If the decoding or 
     * conversion fails, the raw data is returned as-is <p>
     *
     * This implementation uses <code>getHeader(name)</code>
     * to obtain the requisite header field.
     * 
     * @return	content-description
     */
    public String getDescription() throws MessagingException {
	String desc = super.getDescription();
	if (desc == null)
	    desc = getHeader("X-Sun-Data-Description", null);
	return desc;
    }

    /**
     * Set the "Content-Description" header field for this BodyPart.
     * If the description parameter is <code>null</code>, then any 
     * existing "Content-Description" fields are removed. <p>
     *
     * If the description contains non US-ASCII characters, it will 
     * be encoded using the platform's default charset. If the 
     * description contains only US-ASCII characters, no encoding 
     * is done and it is used as-is.
     * 
     * @param description content-description
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this BodyPart is
     *			obtained from a READ_ONLY folder.
     */
    public void setDescription(String description) throws MessagingException {
	throw new MethodNotSupportedException("SunV3BodyPart not writable");
    }

    /**
     * Set the "Content-Description" header field for this BodyPart.
     * If the description parameter is <code>null</code>, then any 
     * existing "Content-Description" fields are removed. <p>
     *
     * If the description contains non US-ASCII characters, it will 
     * be encoded using the specified charset. If the description 
     * contains only US-ASCII characters, no encoding  is done and 
     * it is used as-is
     * 
     * @param	description	Description
     * @param	charset		Charset for encoding
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this BodyPart is
     *			obtained from a READ_ONLY folder.
     */
    public void setDescription(String description, String charset) 
		throws MessagingException {
	throw new MethodNotSupportedException("SunV3BodyPart not writable");
    }

    /**
     * Get the filename associated with this BodyPart. <p>
     *
     * Returns the value of the "filename" parameter from the
     * "Content-Disposition" header field of this BodyPart. If its
     * not available, returns the value of the "name" parameter from
     * the "Content-Type" header field of this BodyPart.
     * Returns <code>null</code> if both are absent.
     *
     * @return	filename
     */
    public String getFileName() throws MessagingException {
	String name = super.getFileName();
	if (name == null)
	    name = getHeader("X-Sun-Data-Name", null);
	return name;
    }

    /**
     * Set the filename associated with this BodyPart, if possible. <p>
     *
     * Sets the "filename" parameter of the "Content-Disposition"
     * header field of this BodyPart.
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this BodyPart is
     *			obtained from a READ_ONLY folder.
     */
    public void setFileName(String filename) throws MessagingException {
	throw new MethodNotSupportedException("SunV3BodyPart not writable");
    }

    /**
     * This method provides the mechanism to set this BodyPart's content.
     * The given DataHandler object should wrap the actual content.
     * 
     * @param   dh      The DataHandler for the content
     * @exception       IllegalWriteException if the underlying
     * 			implementation does not support modification
     * @exception	IllegalStateException if this BodyPart is
     *			obtained from a READ_ONLY folder.
     */                 
    public void setDataHandler(DataHandler dh) 
		throws MessagingException {
	throw new MethodNotSupportedException("SunV3BodyPart not writable");
    }

    /**
     * Output the BodyPart as a RFC822 format stream.
     *
     * @exception MessagingException
     * @exception IOException	if an error occurs writing to the
     *				stream or if an error is generated
     *				by the javax.activation layer.
     * @see javax.activation.DataHandler#writeTo()
     */
    public void writeTo(OutputStream os)
				throws IOException, MessagingException {
	throw new MethodNotSupportedException("SunV3BodyPart writeTo");
    }

    /**
     * This is the method that has the 'smarts' to query the 'content'
     * and update the appropriate headers. Typical headers that get
     * set here are: Content-Type, Content-Encoding, boundary (for
     * multipart). Now, the tricky part here is when to actually
     * activate this method:
     *
     * - A Message being crafted by a mail-application will certainly
     * need to activate this method at some point to fill up its internal
     * headers. Typically this is triggered off by our writeTo() method.
     *
     * - A message read-in from a MessageStore will have obtained
     * all its headers from the store, and so does'nt need this.
     * However, if this message is editable and if any edits have
     * been made to either the content or message-structure, we might
     * need to resync our headers. Typically this is triggered off by
     * the Message.saveChanges() methods.
     */
    protected void updateHeaders() throws MessagingException {
	throw new MethodNotSupportedException("SunV3BodyPart updateHeaders");
    }
}
