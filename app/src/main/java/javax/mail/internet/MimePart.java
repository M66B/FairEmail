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

import javax.mail.*;
import java.io.*;
import java.util.Enumeration;

/**
 * The MimePart interface models an <strong>Entity</strong> as defined
 * by MIME (RFC2045, Section 2.4). <p>
 *
 * MimePart extends the Part interface to add additional RFC822 and MIME
 * specific semantics and attributes. It provides the base interface for
 * the MimeMessage and  MimeBodyPart classes 
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
 * @see		MimeUtility
 * @see		javax.mail.Part
 * @author 	John Mani
 */

public interface MimePart extends Part {

    /**
     * Get the values of all header fields available for this header,
     * returned as a single String, with the values separated by the 
     * delimiter. If the delimiter is <code>null</code>, only the 
     * first value is returned.
     *
     * @param name		the name of this header
     * @param delimiter		delimiter between fields in returned string
     * @return                  the value fields for all headers with 
     *				this name
     * @exception       	MessagingException for failures
     */
    public String getHeader(String name, String delimiter)
				throws MessagingException;

    /**
     * Add a raw RFC822 header-line. 
     *
     * @param	line	the line to add
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this Part is
     *			obtained from a READ_ONLY folder
     * @exception       MessagingException for other failures
     */
    public void addHeaderLine(String line) throws MessagingException;

    /**
     * Get all header lines as an Enumeration of Strings. A Header
     * line is a raw RFC822 header-line, containing both the "name" 
     * and "value" field. 
     *
     * @return	an Enumeration of Strings
     * @exception	MessagingException for failures
     */
    public Enumeration<String> getAllHeaderLines() throws MessagingException;

    /**
     * Get matching header lines as an Enumeration of Strings. 
     * A Header line is a raw RFC822 header-line, containing both 
     * the "name" and "value" field.
     *
     * @param	names	the headers to return
     * @return	an Enumeration of Strings
     * @exception	MessagingException for failures
     */
    public Enumeration<String> getMatchingHeaderLines(String[] names)
			throws MessagingException;

    /**
     * Get non-matching header lines as an Enumeration of Strings. 
     * A Header line is a raw RFC822 header-line, containing both 
     * the "name"  and "value" field.
     *
     * @param	names	the headers to not return
     * @return	an Enumeration of Strings
     * @exception	MessagingException for failures
     */
    public Enumeration<String> getNonMatchingHeaderLines(String[] names)
			throws MessagingException;

    /**
     * Get the transfer encoding of this part.
     *
     * @return		content-transfer-encoding
     * @exception	MessagingException for failures
     */
    public String getEncoding() throws MessagingException;

    /**
     * Get the Content-ID of this part. Returns null if none present.
     *
     * @return		content-ID
     * @exception	MessagingException for failures
     */
    public String getContentID() throws MessagingException;

    /**
     * Get the Content-MD5 digest of this part. Returns null if
     * none present.
     *
     * @return		content-MD5
     * @exception	MessagingException for failures
     */
    public String getContentMD5() throws MessagingException;

    /**
     * Set the Content-MD5 of this part.
     *
     * @param  md5	the MD5 value
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this Part is
     *			obtained from a READ_ONLY folder
     */
    public void setContentMD5(String md5) throws MessagingException;

    /**
     * Get the language tags specified in the Content-Language header
     * of this MimePart. The Content-Language header is defined by
     * RFC 1766. Returns <code>null</code> if this header is not
     * available.
     *
     * @return	array of content language strings
     * @exception	MessagingException for failures
     */
    public String[] getContentLanguage() throws MessagingException;

    /**
     * Set the Content-Language header of this MimePart. The
     * Content-Language header is defined by RFC1766.
     *
     * @param languages	array of language tags
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this Part is
     *			obtained from a READ_ONLY folder
     */
    public void setContentLanguage(String[] languages)
			throws MessagingException;
    
    /**
     * Convenience method that sets the given String as this
     * part's content, with a MIME type of "text/plain". If the
     * string contains non US-ASCII characters. it will be encoded
     * using the platform's default charset. The charset is also
     * used to set the "charset" parameter. <p>
     *
     * Note that there may be a performance penalty if
     * <code>text</code> is large, since this method may have
     * to scan all the characters to determine what charset to
     * use. <p>
     *
     * If the charset is already known, use the
     * <code>setText</code> method that takes the charset parameter.
     *
     * @param	text	the text content to set
     * @exception	MessagingException	if an error occurs
     * @see	#setText(String text, String charset)
     */
    @Override
    public void setText(String text) throws MessagingException;

    /**
     * Convenience method that sets the given String as this part's
     * content, with a MIME type of "text/plain" and the specified
     * charset. The given Unicode string will be charset-encoded
     * using the specified charset. The charset is also used to set
     * "charset" parameter.
     *
     * @param	text	the text content to set
     * @param	charset	the charset to use for the text
     * @exception	MessagingException	if an error occurs
     */
    public void setText(String text, String charset)
			throws MessagingException;

    /**
     * Convenience method that sets the given String as this part's
     * content, with a primary MIME type of "text" and the specified
     * MIME subtype.  The given Unicode string will be charset-encoded
     * using the specified charset. The charset is also used to set
     * the "charset" parameter.
     *
     * @param	text	the text content to set
     * @param	charset	the charset to use for the text
     * @param	subtype	the MIME subtype to use (e.g., "html")
     * @exception	MessagingException	if an error occurs
     * @since	JavaMail 1.4
     */
    public void setText(String text, String charset, String subtype)
                        throws MessagingException;
}
