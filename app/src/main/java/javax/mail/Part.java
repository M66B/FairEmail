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

package javax.mail;

import java.io.*;
import java.util.Enumeration;
import javax.activation.DataHandler;

/**
 * The <code>Part</code> interface is the common base interface for 
 * Messages and BodyParts. <p>
 *
 * Part consists of a set of attributes and a "Content".<p>
 *
 * <strong> Attributes: </strong> <p>
 *
 * The Jakarta Mail API defines a set of standard Part attributes that are
 * considered to be common to most existing Mail systems. These
 * attributes have their own settor and gettor methods. Mail systems 
 * may support other Part attributes as well, these are represented as 
 * name-value pairs where both the name and value are Strings.<p>
 *
 * <strong> Content: </strong> <p>
 *
 * The <strong>data type</strong> of the "content" is returned by
 * the <code>getContentType()</code> method. The MIME typing system
 * is used to name data types. <p>
 *
 * The "content" of a Part is available in various formats:
 * <ul>
 * <li> As a DataHandler - using the <code>getDataHandler()</code> method.
 * The "content" of a Part is also available through a 
 * <code>javax.activation.DataHandler</code> object. The DataHandler 
 * object allows clients to discover the operations available on the
 * content, and to instantiate the appropriate component to perform
 * those operations. 
 *
 * <li> As an input stream - using the <code>getInputStream()</code> method.
 * Any mail-specific encodings are decoded before this stream is returned.
 *
 * <li> As a Java object - using the <code>getContent()</code> method.
 * This method returns the "content" as a Java object.
 * The returned object is of course dependent on the content
 * itself. In particular, a "multipart" Part's content is always a 
 * Multipart or subclass thereof.  That is, <code>getContent()</code> on a 
 * "multipart" type Part will always return a Multipart (or subclass) object.
 * </ul>
 *
 * Part provides the <code>writeTo()</code> method that streams
 * out its bytestream in mail-safe form suitable for transmission. 
 * This bytestream is typically an aggregation of the Part attributes
 * and its content's bytestream. <p>
 *
 * Message and BodyPart implement the Part interface. Note that in
 * MIME parlance, Part models an Entity (RFC 2045, Section 2.4).
 *
 * @author John Mani
 */

public interface Part {

    /**
     * Return the size of the content of this part in bytes.
     * Return -1 if the size cannot be determined. <p>
     *
     * Note that the size may not be an exact measure of the content
     * size and may or may not account for any transfer encoding
     * of the content. The size is appropriate for display in a 
     * user interface to give the user a rough idea of the size
     * of this part.
     *
     * @return		size of content in bytes
     * @exception	MessagingException for failures
     */
    public int getSize() throws MessagingException;

    /**
     * Return the number of lines in the content of this part. 
     * Return -1 if the number cannot be determined.
     *
     * Note that this number may not be an exact measure of the 
     * content length and may or may not account for any transfer 
     * encoding of the content. 
     *
     * @return		number of lines in the content.
     * @exception	MessagingException for failures
     */
    public int getLineCount() throws MessagingException;

    /**
     * Returns the Content-Type of the content of this part.
     * Returns null if the Content-Type could not be determined. <p>
     *
     * The MIME typing system is used to name Content-types.
     *
     * @return		The ContentType of this part
     * @exception	MessagingException for failures
     * @see		javax.activation.DataHandler
     */
    public String getContentType() throws MessagingException;

    /**
     * Is this Part of the specified MIME type?  This method
     * compares <strong>only the <code>primaryType</code> and 
     * <code>subType</code></strong>.
     * The parameters of the content types are ignored. <p>
     *
     * For example, this method will return <code>true</code> when
     * comparing a Part of content type <strong>"text/plain"</strong>
     * with <strong>"text/plain; charset=foobar"</strong>. <p>
     *
     * If the <code>subType</code> of <code>mimeType</code> is the
     * special character '*', then the subtype is ignored during the
     * comparison.
     *
     * @param	mimeType	the MIME type to test
     * @return	true if this part is of the specified type
     * @exception	MessagingException for failures
     */
    public boolean isMimeType(String mimeType) throws MessagingException;

    /**
     * This part should be presented as an attachment.
     * @see #getDisposition
     * @see #setDisposition
     */
    public static final String ATTACHMENT = "attachment";

    /**
     * This part should be presented inline.
     * @see #getDisposition
     * @see #setDisposition
     */
    public static final String INLINE = "inline";

    /**
     * Return the disposition of this part.  The disposition
     * describes how the part should be presented to the user.
     * (See RFC 2183.)  The return value should be considered
     * without regard to case.  For example:
     * <blockquote><pre>
     * String disp = part.getDisposition();
     * if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT))
     *	// treat as attachment if not first part
     * </pre></blockquote>
     *
     * @return		disposition of this part, or null if unknown
     * @exception	MessagingException for failures
     * @see #ATTACHMENT
     * @see #INLINE
     * @see #getFileName
     */
    public String getDisposition() throws MessagingException;

    /**
     * Set the disposition of this part.
     *
     * @param	disposition	disposition of this part
     * @exception	IllegalWriteException if the underlying implementation
     *			does not support modification of this header
     * @exception	IllegalStateException if this Part is obtained
     *			from a READ_ONLY folder
     * @exception	MessagingException for other failures
     * @see #ATTACHMENT
     * @see #INLINE
     * @see #setFileName
     */
    public void setDisposition(String disposition) throws MessagingException;

    /**
     * Return a description String for this part. This typically
     * associates some descriptive information with this part.
     * Returns null if none is available.
     *
     * @return		description of this part
     * @exception	MessagingException for failures
     */
    public String getDescription() throws MessagingException;

    /**
     * Set a description String for this part. This typically
     * associates some descriptive information with this part.
     *
     * @param	description	description of this part
     * @exception	IllegalWriteException if the underlying implementation
     *			does not support modification of this header
     * @exception	IllegalStateException if this Part is obtained
     *			from a READ_ONLY folder
     * @exception	MessagingException for other failures
     */
    public void setDescription(String description) throws MessagingException;

    /**
     * Get the filename associated with this part, if possible.
     * Useful if this part represents an "attachment" that was
     * loaded from a file.  The filename will usually be a simple
     * name, not including directory components.
     *
     * @return	Filename to associate with this part
     * @exception	MessagingException for failures
     */
    public String getFileName() throws MessagingException;

    /**
     * Set the filename associated with this part, if possible.
     * Useful if this part represents an "attachment" that was
     * loaded from a file.  The filename will usually be a simple
     * name, not including directory components.
     *
     * @param	filename	Filename to associate with this part
     * @exception	IllegalWriteException if the underlying implementation
     *			does not support modification of this header
     * @exception	IllegalStateException if this Part is obtained
     *			from a READ_ONLY folder
     * @exception	MessagingException for other failures
     */
    public void setFileName(String filename) throws MessagingException;

    /**
     * Return an input stream for this part's "content". Any 
     * mail-specific transfer encodings will be decoded before the 
     * input stream is provided. <p>
     *
     * This is typically a convenience method that just invokes
     * the DataHandler's <code>getInputStream()</code> method.
     *
     * @return an InputStream
     * @exception	IOException this is typically thrown by the 
     *			DataHandler. Refer to the documentation for 
     *			javax.activation.DataHandler for more details.
     * @exception	MessagingException for other failures
     * @see #getDataHandler
     * @see javax.activation.DataHandler#getInputStream
     */
    public InputStream getInputStream() 
		throws IOException, MessagingException;
    
    /**
     * Return a DataHandler for the content within this part. The
     * DataHandler allows clients to operate on as well as retrieve
     * the content.
     *
     * @return		DataHandler for the content
     * @exception 	MessagingException for failures
     */
    public DataHandler getDataHandler() throws MessagingException;

    /**
     * Return the content as a Java object. The type of the returned 
     * object is of course dependent on the content itself. For example,
     * the object returned for "text/plain" content is usually a String 
     * object. The object returned for a "multipart" content is always a
     * Multipart subclass. For content-types that are  unknown to the
     * DataHandler system, an input stream is returned as the content <p>
     *
     * This is a convenience method that just invokes the DataHandler's
     * getContent() method
     *
     * @return		Object
     * @exception	IOException this is typically thrown by the 
     *			DataHandler. Refer to the documentation for 
     *			javax.activation.DataHandler for more details.
     * @exception 	MessagingException for other failures
     *
     * @see javax.activation.DataHandler#getContent
     */
    public Object getContent() throws IOException, MessagingException;

    /**
     * This method provides the mechanism to set this part's content.
     * The DataHandler wraps around the actual content.
     *
     * @param	dh	The DataHandler for the content.
     * @exception	IllegalWriteException if the underlying implementation
     *			does not support modification of existing values
     * @exception	IllegalStateException if this Part is obtained
     *			from a READ_ONLY folder
     * @exception 	MessagingException for other failures
     */
    public void setDataHandler(DataHandler dh) throws MessagingException;

    /**
     * A convenience method for setting this part's content.  The part
     * internally wraps the content in a DataHandler. <p>
     *
     * Note that a DataContentHandler class for the specified type should 
     * be available to the Jakarta Mail implementation for this to work right.
     * i.e., to do <code>setContent(foobar, "application/x-foobar")</code>,
     * a DataContentHandler for "application/x-foobar" should be installed.
     * Refer to the Java Activation Framework for more information.
     *
     * @param	obj	A java object.
     * @param	type	MIME type of this object.
     * @exception	IllegalWriteException if the underlying implementation
     *			does not support modification of existing values
     * @exception	IllegalStateException if this Part is obtained
     *			from a READ_ONLY folder
     * @exception 	MessagingException for other failures
     */
    public void setContent(Object obj, String type) 
			throws MessagingException;

    /**
     * A convenience method that sets the given String as this
     * part's content with a MIME type of "text/plain". 
     *
     * @param  text    	The text that is the Message's content.
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification of 
     *			existing values
     * @exception	IllegalStateException if this Part is obtained
     *			from a READ_ONLY folder
     * @exception 	MessagingException for other failures
     */
    public void setText(String text) throws MessagingException;

    /**
     * This method sets the given Multipart object as this message's
     * content.
     *
     * @param  mp      	The multipart object that is the Message's content
     * @exception	IllegalWriteException if the underlying 
     *			implementation	does not support modification of 
     *			existing values
     * @exception	IllegalStateException if this Part is obtained
     *			from a READ_ONLY folder
     * @exception 	MessagingException for other failures
     */
    public void setContent(Multipart mp) throws MessagingException;

    /**
     * Output a bytestream for this Part. This bytestream is
     * typically an aggregration of the Part attributes and
     * an appropriately encoded bytestream from its 'content'. <p>
     *
     * Classes that implement the Part interface decide on
     * the appropriate encoding algorithm to be used. <p>
     *
     * The bytestream is typically used for sending.
     *
     * @param	os	the stream to write to
     * @exception IOException		if an error occurs writing to the 
     *					stream or if an error is generated
     *					by the javax.activation layer.
     * @exception MessagingException	if an error occurs fetching the
     *					data to be written
     *
     * @see javax.activation.DataHandler#writeTo
     */
    public void writeTo(OutputStream os) throws IOException, MessagingException;

    /**
     * Get all the headers for this header name. Returns <code>null</code>
     * if no headers for this header name are available.
     *
     * @param header_name       the name of this header
     * @return                  the value fields for all headers with 
     *				this name
     * @exception       	MessagingException for failures
     */
    public String[] getHeader(String header_name)
				throws MessagingException;
    
    /**
     * Set the value for this header_name. Replaces all existing
     * header values with this new value.
     *
     * @param header_name       the name of this header
     * @param header_value      the value for this header
     * @exception		IllegalWriteException if the underlying 
     *				implementation does not support modification 
     *				of existing values
     * @exception		IllegalStateException if this Part is 
     *				obtained from a READ_ONLY folder
     * @exception       	MessagingException for other failures
     */
    public void setHeader(String header_name, String header_value)
				throws MessagingException;
    /**
     * Add this value to the existing values for this header_name.
     *
     * @param header_name       the name of this header
     * @param header_value      the value for this header
     * @exception		IllegalWriteException if the underlying 
     *				implementation does not support modification 
     *				of existing values
     * @exception		IllegalStateException if this Part is 
     *				obtained from a READ_ONLY folder
     * @exception       	MessagingException for other failures
     */
    public void addHeader(String header_name, String header_value)
				throws MessagingException;
    /**
     * Remove all headers with this name.
     *
     * @param header_name       the name of this header
     * @exception		IllegalWriteException if the underlying 
     *				implementation does not support modification 
     *				of existing values
     * @exception		IllegalStateException if this Part is 
     *				obtained from a READ_ONLY folder
     * @exception       	MessagingException for other failures
     */
    public void removeHeader(String header_name)
				throws MessagingException;

    /**
     * Return all the headers from this part as an Enumeration of
     * Header objects.
     *
     * @return  enumeration of Header objects
     * @exception       MessagingException for failures
     */
    public Enumeration<Header> getAllHeaders() throws MessagingException;

    /**
     * Return matching headers from this part as an Enumeration of
     * Header objects.
     *
     * @param	header_names	the headers to match
     * @return  enumeration of Header objects
     * @exception       MessagingException for failures
     */
    public Enumeration<Header> getMatchingHeaders(String[] header_names)
				throws MessagingException;

    /**
     * Return non-matching headers from this envelope as an Enumeration
     * of Header objects.
     *
     * @param	header_names	the headers to not match
     * @return  enumeration of Header objects
     * @exception       MessagingException for failures
     */
    public Enumeration<Header> getNonMatchingHeaders(String[] header_names) 
				throws MessagingException;
}
