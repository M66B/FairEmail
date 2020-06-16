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
import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.ParseException;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.MimeUtil;
import com.sun.mail.util.MessageRemovedIOException;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.LineOutputStream;
import javax.mail.util.SharedByteArrayInputStream;

/**
 * This class represents a MIME style email message. It implements
 * the <code>Message</code> abstract class and the <code>MimePart</code>
 * interface. <p>
 *
 * Clients wanting to create new MIME style messages will instantiate
 * an empty MimeMessage object and then fill it with appropriate 
 * attributes and content. <p>
 * 
 * Service providers that implement MIME compliant backend stores may
 * want to subclass MimeMessage and override certain methods to provide
 * specific implementations. The simplest case is probably a provider
 * that generates a MIME style input stream and leaves the parsing of
 * the stream to this class. <p>
 *
 * MimeMessage uses the <code>InternetHeaders</code> class to parse and
 * store the top level RFC 822 headers of a message. <p>
 *
 * The <code>mail.mime.address.strict</code> session property controls
 * the parsing of address headers.  By default, strict parsing of address
 * headers is done.  If this property is set to <code>"false"</code>,
 * strict parsing is not done and many illegal addresses that sometimes
 * occur in real messages are allowed.  See the <code>InternetAddress</code>
 * class for details.
 *
 * <hr><strong>A note on RFC 822 and MIME headers</strong><p>
 *
 * RFC 822 header fields <strong>must</strong> contain only 
 * US-ASCII characters. MIME allows non ASCII characters to be present
 * in certain portions of certain headers, by encoding those characters.
 * RFC 2047 specifies the rules for doing this. The MimeUtility
 * class provided in this package can be used to to achieve this.
 * Callers of the <code>setHeader</code>, <code>addHeader</code>, and
 * <code>addHeaderLine</code> methods are responsible for enforcing
 * the MIME requirements for the specified headers.  In addition, these
 * header fields must be folded (wrapped) before being sent if they
 * exceed the line length limitation for the transport (1000 bytes for
 * SMTP).  Received headers may have been folded.  The application is
 * responsible for folding and unfolding headers as appropriate. <p>
 *
 * @author John Mani
 * @author Bill Shannon
 * @author Max Spivak
 * @author Kanwar Oberoi
 * @see	javax.mail.internet.MimeUtility
 * @see	javax.mail.Part
 * @see	javax.mail.Message
 * @see	javax.mail.internet.MimePart
 * @see	javax.mail.internet.InternetAddress
 */

public class MimeMessage extends Message implements MimePart {

    /**
     * The DataHandler object representing this Message's content.
     */
    protected DataHandler dh;

    /**
     * Byte array that holds the bytes of this Message's content.
     */
    protected byte[] content;

    /**
     * If the data for this message was supplied by an
     * InputStream that implements the SharedInputStream interface,
     * <code>contentStream</code> is another such stream representing
     * the content of this message.  In this case, <code>content</code>
     * will be null.
     *
     * @since	JavaMail 1.2
     */
    protected InputStream contentStream;

    /**
     * The InternetHeaders object that stores the header
     * of this message.
     */
    protected InternetHeaders headers;

    /**
     * The Flags for this message. 
     */
    protected Flags flags;

    /**
     * A flag indicating whether the message has been modified.
     * If the message has not been modified, any data in the
     * <code>content</code> array is assumed to be valid and is used
     * directly in the <code>writeTo</code> method.  This flag is
     * set to true when an empty message is created or when the
     * <code>saveChanges</code> method is called.
     *
     * @since	JavaMail 1.2
     */
    protected boolean modified = false;

    /**
     * Does the <code>saveChanges</code> method need to be called on
     * this message?  This flag is set to false by the public constructor
     * and set to true by the <code>saveChanges</code> method.  The
     * <code>writeTo</code> method checks this flag and calls the
     * <code>saveChanges</code> method as necessary.  This avoids the
     * common mistake of forgetting to call the <code>saveChanges</code>
     * method on a newly constructed message.
     *
     * @since	JavaMail 1.2
     */
    protected boolean saved = false;

    /**
     * If our content is a Multipart or Message object, we save it
     * the first time it's created by parsing a stream so that changes
     * to the contained objects will not be lost. <p>
     *
     * If this field is not null, it's return by the {@link #getContent}
     * method.  The {@link #getContent} method sets this field if it
     * would return a Multipart or MimeMessage object.  This field is
     * is cleared by the {@link #setDataHandler} method.
     *
     * @since	JavaMail 1.5
     */
    protected Object cachedContent;

    // Used to parse dates
    private static final MailDateFormat mailDateFormat = new MailDateFormat();

    // Should addresses in headers be parsed in "strict" mode?
    private boolean strict = true;
    // Is UTF-8 allowed in headers?
    private boolean allowutf8 = false;

    /**
     * Default constructor. An empty message object is created.
     * The <code>headers</code> field is set to an empty InternetHeaders
     * object. The <code>flags</code> field is set to an empty Flags
     * object. The <code>modified</code> flag is set to true.
     *
     * @param	session	the Sesssion
     */
    public MimeMessage(Session session) {
	super(session);
	modified = true;
	headers = new InternetHeaders();
	flags = new Flags();	// empty flags object
	initStrict();
    }

    /**
     * Constructs a MimeMessage by reading and parsing the data from the
     * specified MIME InputStream. The InputStream will be left positioned
     * at the end of the data for the message. Note that the input stream
     * parse is done within this constructor itself. <p>
     *
     * The input stream contains an entire MIME formatted message with
     * headers and data.
     *
     * @param session	Session object for this message
     * @param is	the message input stream
     * @exception	MessagingException for failures
     */
    public MimeMessage(Session session, InputStream is) 
			throws MessagingException {
	super(session);
	flags = new Flags(); // empty Flags object
	initStrict();
	parse(is);
	saved = true;
    }

    /**
     * Constructs a new MimeMessage with content initialized from the
     * <code>source</code> MimeMessage.  The new message is independent
     * of the original. <p>
     *
     * Note: The current implementation is rather inefficient, copying
     * the data more times than strictly necessary.
     *
     * @param	source	the message to copy content from
     * @exception	MessagingException for failures
     * @since		JavaMail 1.2
     */
    public MimeMessage(MimeMessage source) throws MessagingException {
	super(source.session);
	flags = source.getFlags();
	if (flags == null)	// make sure flags is always set
	    flags = new Flags();
	ByteArrayOutputStream bos;
	int size = source.getSize();
	if (size > 0)
	    bos = new ByteArrayOutputStream(size);
	else
	    bos = new ByteArrayOutputStream();
	try {
	    strict = source.strict;
	    source.writeTo(bos);
	    bos.close();
	    SharedByteArrayInputStream bis =
			    new SharedByteArrayInputStream(bos.toByteArray());
	    parse(bis);
	    bis.close();
	    saved = true;
	} catch (IOException ex) {
	    // should never happen, but just in case...
	    throw new MessagingException("IOException while copying message",
					    ex);
	}
    }

    /**
     * Constructs an empty MimeMessage object with the given Folder
     * and message number. <p>
     *
     * This method is for providers subclassing <code>MimeMessage</code>.
     *
     * @param	folder	the Folder this message is from
     * @param	msgnum	the number of this message
     */
    protected MimeMessage(Folder folder, int msgnum) {
	super(folder, msgnum);
	flags = new Flags();  // empty Flags object
	saved = true;
	initStrict();
    }

    /**
     * Constructs a MimeMessage by reading and parsing the data from the
     * specified MIME InputStream. The InputStream will be left positioned
     * at the end of the data for the message. Note that the input stream
     * parse is done within this constructor itself. <p>
     *
     * This method is for providers subclassing <code>MimeMessage</code>.
     *
     * @param folder	The containing folder.
     * @param is	the message input stream
     * @param msgnum	Message number of this message within its folder
     * @exception	MessagingException for failures
     */
    protected MimeMessage(Folder folder, InputStream is, int msgnum)
		throws MessagingException {
	this(folder, msgnum);
	initStrict();
	parse(is);
    }

    /**
     * Constructs a MimeMessage from the given InternetHeaders object
     * and content.
     *
     * This method is for providers subclassing <code>MimeMessage</code>.
     *
     * @param folder	The containing folder.
     * @param headers	The headers
     * @param content	The message content
     * @param msgnum	Message number of this message within its folder
     * @exception	MessagingException for failures
     */
    protected MimeMessage(Folder folder, InternetHeaders headers,
		byte[] content, int msgnum) throws MessagingException {
	this(folder, msgnum);
	this.headers = headers;
	this.content = content;
	initStrict();
    }

    /**
     * Set the strict flag based on property.
     */
    private void initStrict() {
	if (session != null) {
	    Properties props = session.getProperties();
	    strict = PropUtil.getBooleanProperty(props,
				    "mail.mime.address.strict", true);
	    allowutf8 = PropUtil.getBooleanProperty(props,
				    "mail.mime.allowutf8", false);
	}
    }

    /**
     * Parse the InputStream setting the <code>headers</code> and
     * <code>content</code> fields appropriately.  Also resets the
     * <code>modified</code> flag. <p>
     *
     * This method is intended for use by subclasses that need to
     * control when the InputStream is parsed.
     *
     * @param is	The message input stream
     * @exception	MessagingException for failures
     */
    protected void parse(InputStream is) throws MessagingException {

	if (!(is instanceof ByteArrayInputStream) &&
	    !(is instanceof BufferedInputStream) &&
	    !(is instanceof SharedInputStream))
	    is = new BufferedInputStream(is);
	
	headers = createInternetHeaders(is);

	if (is instanceof SharedInputStream) {
	    SharedInputStream sis = (SharedInputStream)is;
	    contentStream = sis.newStream(sis.getPosition(), -1);
	} else {
	    try {
		content = ASCIIUtility.getBytes(is);
	    } catch (IOException ioex) {
		throw new MessagingException("IOException", ioex);
	    }
	}

	modified = false;
    }

    /** 
     * Returns the value of the RFC 822 "From" header fields. If this 
     * header field is absent, the "Sender" header field is used. 
     * If the "Sender" header field is also absent, <code>null</code>
     * is returned.<p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @return		Address object
     * @exception	MessagingException for failures
     * @see	#headers
     */
    @Override
    public Address[] getFrom() throws MessagingException {
	Address[] a = getAddressHeader("From");
	if (a == null)
	    a = getAddressHeader("Sender");
	
	return a;
    }

    /**
     * Set the RFC 822 "From" header field. Any existing values are 
     * replaced with the given address. If address is <code>null</code>,
     * this header is removed.
     *
     * @param address	the sender of this message
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    @Override
    public void setFrom(Address address) throws MessagingException {
	setAddressHeader("From", new Address[] { address });
    }

    /**
     * Set the RFC 822 "From" header field. Any existing values are 
     * replaced with the given addresses. If address is <code>null</code>,
     * this header is removed.
     *
     * @param address	the sender(s) of this message
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     * @since		JvaMail 1.5
     */
    public void setFrom(String address) throws MessagingException {
	setAddressHeader("From", InternetAddress.parse(address));
    }

    /**
     * Set the RFC 822 "From" header field using the value of the
     * <code>InternetAddress.getLocalAddress</code> method.
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    @Override
    public void setFrom() throws MessagingException {
	InternetAddress me = null;
	try {
	    me = InternetAddress._getLocalAddress(session);
	} catch (Exception ex) {
	    // if anything goes wrong (SecurityException, UnknownHostException),
	    // chain the exception
	    throw new MessagingException("No From address", ex);
	}
	if (me != null)
	    setFrom(me);
	else
	    throw new MessagingException("No From address");
    }

    /**
     * Add the specified addresses to the existing "From" field. If
     * the "From" field does not already exist, it is created.
     *
     * @param addresses	the senders of this message
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    @Override
    public void addFrom(Address[] addresses) throws MessagingException {
	addAddressHeader("From", addresses);
    }

    /** 
     * Returns the value of the RFC 822 "Sender" header field.
     * If the "Sender" header field is absent, <code>null</code>
     * is returned.<p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @return		Address object
     * @exception	MessagingException for failures
     * @see		#headers
     * @since		JavaMail 1.3
     */
    public Address getSender() throws MessagingException {
	Address[] a = getAddressHeader("Sender");
	if (a == null || a.length == 0)
	    return null;
	return a[0];	// there can be only one
    }

    /**
     * Set the RFC 822 "Sender" header field. Any existing values are 
     * replaced with the given address. If address is <code>null</code>,
     * this header is removed.
     *
     * @param address	the sender of this message
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     * @since		JavaMail 1.3
     */
    public void setSender(Address address) throws MessagingException {
	setAddressHeader("Sender", new Address[] { address });
    }

    /**
     * This inner class extends the javax.mail.Message.RecipientType
     * class to add additional RecipientTypes. The one additional
     * RecipientType currently defined here is NEWSGROUPS.
     *
     * @see javax.mail.Message.RecipientType
     */
    public static class RecipientType extends Message.RecipientType {

	private static final long serialVersionUID = -5468290701714395543L;

	/**
	 * The "Newsgroup" (Usenet news) recipients.
	 */
	public static final RecipientType NEWSGROUPS =
					new RecipientType("Newsgroups");
	protected RecipientType(String type) {
	    super(type);
	}

	@Override
	protected Object readResolve() throws ObjectStreamException {
	    if (type.equals("Newsgroups"))
		return NEWSGROUPS;
	    else
		return super.readResolve();
	}
    }

    /**
     * Returns the recepients specified by the type. The mapping
     * between the type and the corresponding RFC 822 header is
     * as follows:
     * <pre>
     *		Message.RecipientType.TO		"To"
     *		Message.RecipientType.CC		"Cc"
     *		Message.RecipientType.BCC		"Bcc"
     *		MimeMessage.RecipientType.NEWSGROUPS	"Newsgroups"
     * </pre><br>
     *
     * Returns null if the header specified by the type is not found
     * or if its value is empty. <p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @param           type	Type of recepient
     * @return          array of Address objects
     * @exception       MessagingException if header could not
     *                  be retrieved
     * @exception       AddressException if the header is misformatted
     * @see		#headers
     * @see		javax.mail.Message.RecipientType#TO
     * @see		javax.mail.Message.RecipientType#CC
     * @see		javax.mail.Message.RecipientType#BCC
     * @see		javax.mail.internet.MimeMessage.RecipientType#NEWSGROUPS
     */
    @Override
    public Address[] getRecipients(Message.RecipientType type)
				throws MessagingException {
	if (type == RecipientType.NEWSGROUPS) {
	    String s = getHeader("Newsgroups", ",");
	    return (s == null) ? null : NewsAddress.parse(s);
	} else
	    return getAddressHeader(getHeaderName(type));
    }

    /**
     * Get all the recipient addresses for the message.
     * Extracts the TO, CC, BCC, and NEWSGROUPS recipients.
     *
     * @return          array of Address objects
     * @exception       MessagingException for failures
     * @see		javax.mail.Message.RecipientType#TO
     * @see		javax.mail.Message.RecipientType#CC
     * @see		javax.mail.Message.RecipientType#BCC
     * @see		javax.mail.internet.MimeMessage.RecipientType#NEWSGROUPS
     */
    @Override
    public Address[] getAllRecipients() throws MessagingException {
	Address[] all = super.getAllRecipients();
	Address[] ng = getRecipients(RecipientType.NEWSGROUPS);

	if (ng == null)
	    return all;		// the common case
	if (all == null)
	    return ng;		// a rare case

	Address[] addresses = new Address[all.length + ng.length];
	System.arraycopy(all, 0, addresses, 0, all.length);
	System.arraycopy(ng, 0, addresses, all.length, ng.length);
	return addresses;
    }
	
    /**
     * Set the specified recipient type to the given addresses.
     * If the address parameter is <code>null</code>, the corresponding
     * recipient field is removed.
     *
     * @param type	Recipient type
     * @param addresses	Addresses
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     * @see		#getRecipients
     */
    @Override
    public void setRecipients(Message.RecipientType type, Address[] addresses)
                                throws MessagingException {
	if (type == RecipientType.NEWSGROUPS) {
	    if (addresses == null || addresses.length == 0)
		removeHeader("Newsgroups");
	    else
		setHeader("Newsgroups", NewsAddress.toString(addresses));
	} else
	    setAddressHeader(getHeaderName(type), addresses);
    }

    /**
     * Set the specified recipient type to the given addresses.
     * If the address parameter is <code>null</code>, the corresponding
     * recipient field is removed.
     *   
     * @param type      Recipient type
     * @param addresses Addresses
     * @exception       AddressException if the attempt to parse the
     *                  addresses String fails
     * @exception       IllegalWriteException if the underlying
     *                  implementation does not support modification
     *                  of existing values
     * @exception       IllegalStateException if this message is
     *                  obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     * @see             #getRecipients
     * @since           JavaMail 1.2
     */
    public void setRecipients(Message.RecipientType type, String addresses)
                                throws MessagingException {
        if (type == RecipientType.NEWSGROUPS) {
            if (addresses == null || addresses.length() == 0)
                removeHeader("Newsgroups");
            else
                setHeader("Newsgroups", addresses);
        } else
            setAddressHeader(getHeaderName(type),
		addresses == null ? null : InternetAddress.parse(addresses));
    }

    /**
     * Add the given addresses to the specified recipient type.
     *
     * @param type	Recipient type
     * @param addresses	Addresses
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    @Override
    public void addRecipients(Message.RecipientType type, Address[] addresses)
                                throws MessagingException {
	if (type == RecipientType.NEWSGROUPS) {
	    String s = NewsAddress.toString(addresses);
	    if (s != null)
		addHeader("Newsgroups", s);
	} else
	    addAddressHeader(getHeaderName(type), addresses);
    }

    /**
     * Add the given addresses to the specified recipient type.
     * 
     * @param type      Recipient type
     * @param addresses Addresses
     * @exception       AddressException if the attempt to parse the
     *                  addresses String fails
     * @exception       IllegalWriteException if the underlying
     *                  implementation does not support modification
     *                  of existing values
     * @exception       IllegalStateException if this message is
     *                  obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     * @since           JavaMail 1.2
     */
    public void addRecipients(Message.RecipientType type, String addresses)
                                throws MessagingException {
        if (type == RecipientType.NEWSGROUPS) {
            if (addresses != null && addresses.length() != 0)
                addHeader("Newsgroups", addresses);
        } else
            addAddressHeader(getHeaderName(type),
		    InternetAddress.parse(addresses));
    }
 
    /**
     * Return the value of the RFC 822 "Reply-To" header field. If
     * this header is unavailable or its value is absent, then
     * the <code>getFrom</code> method is called and its value is returned.
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @exception	MessagingException for failures
     * @see		#headers
     */
    @Override
    public Address[] getReplyTo() throws MessagingException {
	Address[] a = getAddressHeader("Reply-To");
	if (a == null || a.length == 0)
	    a = getFrom();
	return a;
    }

    /**
     * Set the RFC 822 "Reply-To" header field. If the address 
     * parameter is <code>null</code>, this header is removed.
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    @Override
    public void setReplyTo(Address[] addresses) throws MessagingException {
	setAddressHeader("Reply-To", addresses);
    }

    // Convenience method to get addresses
    private Address[] getAddressHeader(String name) 
			throws MessagingException {
	String s = getHeader(name, ",");
	return (s == null) ? null : InternetAddress.parseHeader(s, strict);
    }

    // Convenience method to set addresses
    private void setAddressHeader(String name, Address[] addresses)
			throws MessagingException {
	String s;
	if (allowutf8)
	    s = InternetAddress.toUnicodeString(addresses, name.length() + 2);
	else
	    s = InternetAddress.toString(addresses, name.length() + 2);
	if (s == null)
	    removeHeader(name);
	else
	    setHeader(name, s);
    }

    private void addAddressHeader(String name, Address[] addresses)
			throws MessagingException {
	if (addresses == null || addresses.length == 0)
	    return;
	Address[] a = getAddressHeader(name);
	Address[] anew;
	if (a == null || a.length == 0)
	    anew = addresses;
	else {
	    anew = new Address[a.length + addresses.length];
	    System.arraycopy(a, 0, anew, 0, a.length);
	    System.arraycopy(addresses, 0, anew, a.length, addresses.length);
	}
	String s;
	if (allowutf8)
	    s = InternetAddress.toUnicodeString(anew, name.length() + 2);
	else
	    s = InternetAddress.toString(anew, name.length() + 2);
	if (s == null)
	    return;
	setHeader(name, s);
    }

    /**
     * Returns the value of the "Subject" header field. Returns null 
     * if the subject field is unavailable or its value is absent. <p>
     *
     * If the subject is encoded as per RFC 2047, it is decoded and
     * converted into Unicode. If the decoding or conversion fails, the
     * raw data is returned as is. <p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @return          Subject
     * @exception	MessagingException for failures
     * @see		#headers
     */
    @Override
    public String getSubject() throws MessagingException {
	String rawvalue = getHeader("Subject", null);

	if (rawvalue == null)
	    return null;

	try {
	    return MimeUtility.decodeText(MimeUtility.unfold(rawvalue));
	} catch (UnsupportedEncodingException ex) {
	    return rawvalue;
	}
    }

    /**
     * Set the "Subject" header field. If the subject contains 
     * non US-ASCII characters, it will be encoded using the 
     * platform's default charset. If the subject contains only 
     * US-ASCII characters, no encoding is done and it is used 
     * as-is. If the subject is null, the existing "Subject" field
     * is removed. <p>
     *
     * The application must ensure that the subject does not contain
     * any line breaks. <p>
     *
     * Note that if the charset encoding process fails, a
     * MessagingException is thrown, and an UnsupportedEncodingException
     * is included in the chain of nested exceptions within the
     * MessagingException.
     *
     * @param 	subject		The subject
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    @Override
    public void setSubject(String subject) throws MessagingException {
	setSubject(subject, null);
    }

    /**
     * Set the "Subject" header field. If the subject contains non 
     * US-ASCII characters, it will be encoded using the specified
     * charset. If the subject contains only US-ASCII characters, no 
     * encoding is done and it is used as-is. If the subject is null, 
     * the existing "Subject" header field is removed. <p>
     *
     * The application must ensure that the subject does not contain
     * any line breaks. <p>
     *
     * Note that if the charset encoding process fails, a
     * MessagingException is thrown, and an UnsupportedEncodingException
     * is included in the chain of nested exceptions within the
     * MessagingException.
     *
     * @param	subject		The subject
     * @param	charset		The charset 
     * @exception		IllegalWriteException if the underlying
     *				implementation does not support modification
     *				of existing values
     * @exception		IllegalStateException if this message is
     *				obtained from a READ_ONLY folder.
     * @exception		MessagingException for other failures
     */
    public void setSubject(String subject, String charset)
			throws MessagingException {
	if (subject == null) {
	    removeHeader("Subject");
	} else {
	    try {
		setHeader("Subject", MimeUtility.fold(9,
		    MimeUtility.encodeText(subject, charset, null)));
	    } catch (UnsupportedEncodingException uex) {
		throw new MessagingException("Encoding error", uex);
	    }
	}
    }

    /**
     * Returns the value of the RFC 822 "Date" field. This is the date 
     * on which this message was sent. Returns null if this field is 
     * unavailable or its value is absent. <p>
     * 
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @return          The sent Date
     * @exception	MessagingException for failures
     */
    @Override
    public Date getSentDate() throws MessagingException {
	String s = getHeader("Date", null);
	if (s != null) {
	    try {
		synchronized (mailDateFormat) {
		    return mailDateFormat.parse(s);
		}
	    } catch (ParseException pex) {
		return null;
	    }
	}
	
	return null;
    }

    /**
     * Set the RFC 822 "Date" header field. This is the date on which the
     * creator of the message indicates that the message is complete
     * and ready for delivery. If the date parameter is 
     * <code>null</code>, the existing "Date" field is removed.
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    @Override
    public void setSentDate(Date d) throws MessagingException {
	if (d == null)
	    removeHeader("Date");
	else {
	    synchronized (mailDateFormat) {
		setHeader("Date", mailDateFormat.format(d));
	    }
	}
    }

    /**
     * Returns the Date on this message was received. Returns 
     * <code>null</code> if this date cannot be obtained. <p>
     *
     * Note that RFC 822 does not define a field for the received
     * date. Hence only implementations that can provide this date
     * need return a valid value. <p>
     *
     * This implementation returns <code>null</code>.
     *
     * @return          the date this message was received
     * @exception	MessagingException for failures
     */
    @Override
    public Date getReceivedDate() throws MessagingException {
	return null;	
    }

    /**
     * Return the size of the content of this message in bytes. 
     * Return -1 if the size cannot be determined. <p>
     *
     * Note that this number may not be an exact measure of the
     * content size and may or may not account for any transfer
     * encoding of the content. <p>
     *
     * This implementation returns the size of the <code>content</code>
     * array (if not null), or, if <code>contentStream</code> is not
     * null, and the <code>available</code> method returns a positive
     * number, it returns that number as the size.  Otherwise, it returns
     * -1.
     *
     * @return          size of content in bytes
     * @exception	MessagingException for failures
     */  
    @Override
    public int getSize() throws MessagingException {
	if (content != null)
	    return content.length;
	if (contentStream != null) {
	    try {
		int size = contentStream.available();
		// only believe the size if it's greater than zero, since zero
		// is the default returned by the InputStream class itself
		if (size > 0)
		    return size;
	    } catch (IOException ex) {
		// ignore it
	    }
	}
	return -1;
    }

    /**
     * Return the number of lines for the content of this message.
     * Return -1 if this number cannot be determined. <p>
     *
     * Note that this number may not be an exact measure of the 
     * content length and may or may not account for any transfer 
     * encoding of the content. <p>
     *
     * This implementation returns -1.
     *
     * @return          number of lines in the content.
     * @exception	MessagingException for failures
     */  
    @Override
    public int getLineCount() throws MessagingException {
	return -1;
    }

    /**
     * Returns the value of the RFC 822 "Content-Type" header field. 
     * This represents the content-type of the content of this 
     * message. This value must not be null. If this field is 
     * unavailable, "text/plain" should be returned. <p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @return          The ContentType of this part
     * @exception	MessagingException for failures
     * @see             javax.activation.DataHandler
     */
    @Override
    public String getContentType() throws MessagingException {
	String s = getHeader("Content-Type", null);
	s = MimeUtil.cleanContentType(this, s);
	if (s == null)
	    return "text/plain";
	return s;
    }

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
     * @param	mimeType	the MIME type to check
     * @return			true if it matches the MIME type
     * @exception		MessagingException for failures
     */
    @Override
    public boolean isMimeType(String mimeType) throws MessagingException {
	return MimeBodyPart.isMimeType(this, mimeType);
    }

    /**
     * Returns the disposition from the "Content-Disposition" header field.
     * This represents the disposition of this part. The disposition
     * describes how the part should be presented to the user. <p>
     *
     * If the Content-Disposition field is unavailable, 
     * <code>null</code> is returned. <p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @return          disposition of this part, or null if unknown
     * @exception	MessagingException for failures
     */
    @Override
    public String getDisposition() throws MessagingException {
	return MimeBodyPart.getDisposition(this);
    }

    /**
     * Set the disposition in the "Content-Disposition" header field
     * of this body part.  If the disposition is null, any existing
     * "Content-Disposition" header field is removed.
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    @Override
    public void setDisposition(String disposition) throws MessagingException {
	MimeBodyPart.setDisposition(this, disposition);
    }

    /**
     * Returns the content transfer encoding from the
     * "Content-Transfer-Encoding" header
     * field. Returns <code>null</code> if the header is unavailable
     * or its value is absent. <p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @return          content-transfer-encoding
     * @exception	MessagingException for failures
     */
    @Override
    public String getEncoding() throws MessagingException {
	return MimeBodyPart.getEncoding(this);
    }

    /**
     * Returns the value of the "Content-ID" header field. Returns
     * <code>null</code> if the field is unavailable or its value is 
     * absent. <p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @return          content-ID
     * @exception	MessagingException for failures
     */
    @Override
    public String getContentID() throws MessagingException {
	return getHeader("Content-Id", null);
    }

    /**
     * Set the "Content-ID" header field of this Message.
     * If the <code>cid</code> parameter is null, any existing 
     * "Content-ID" is removed.
     *
     * @param	cid	the content ID
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    public void setContentID(String cid) throws MessagingException {
	if (cid == null)
	    removeHeader("Content-ID");
	else
	    setHeader("Content-ID", cid);
    }

    /**
     * Return the value of the "Content-MD5" header field. Returns 
     * <code>null</code> if this field is unavailable or its value
     * is absent. <p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @return          content-MD5
     * @exception	MessagingException for failures
     */
    @Override
    public String getContentMD5() throws MessagingException {
	return getHeader("Content-MD5", null);
    }

    /**
     * Set the "Content-MD5" header field of this Message.
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    @Override
    public void setContentMD5(String md5) throws MessagingException {
	setHeader("Content-MD5", md5);
    }

    /**
     * Returns the "Content-Description" header field of this Message.
     * This typically associates some descriptive information with 
     * this part. Returns null if this field is unavailable or its
     * value is absent. <p>
     *
     * If the Content-Description field is encoded as per RFC 2047,
     * it is decoded and converted into Unicode. If the decoding or 
     * conversion fails, the raw data is returned as-is <p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     * 
     * @return	content-description
     * @exception	MessagingException for failures
     */
    @Override
    public String getDescription() throws MessagingException {
	return MimeBodyPart.getDescription(this);
    }

    /**
     * Set the "Content-Description" header field for this Message.
     * If the description parameter is <code>null</code>, then any 
     * existing "Content-Description" fields are removed. <p>
     *
     * If the description contains non US-ASCII characters, it will 
     * be encoded using the platform's default charset. If the 
     * description contains only US-ASCII characters, no encoding 
     * is done and it is used as-is. <p>
     *
     * Note that if the charset encoding process fails, a
     * MessagingException is thrown, and an UnsupportedEncodingException
     * is included in the chain of nested exceptions within the
     * MessagingException.
     * 
     * @param description content-description
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException An
     * 			UnsupportedEncodingException may be included
     *			in the exception chain if the charset
     *			conversion fails.
     */
    @Override
    public void setDescription(String description) throws MessagingException {
	setDescription(description, null);
    }

    /**
     * Set the "Content-Description" header field for this Message.
     * If the description parameter is <code>null</code>, then any 
     * existing "Content-Description" fields are removed. <p>
     *
     * If the description contains non US-ASCII characters, it will 
     * be encoded using the specified charset. If the description 
     * contains only US-ASCII characters, no encoding  is done and 
     * it is used as-is. <p>
     *
     * Note that if the charset encoding process fails, a
     * MessagingException is thrown, and an UnsupportedEncodingException
     * is included in the chain of nested exceptions within the
     * MessagingException.
     * 
     * @param	description	Description
     * @param	charset		Charset for encoding
     * @exception		IllegalWriteException if the underlying
     *				implementation does not support modification
     * @exception		IllegalStateException if this message is
     *				obtained from a READ_ONLY folder.
     * @exception		MessagingException An
     * 				UnsupportedEncodingException may be included
     *				in the exception chain if the charset
     *				conversion fails.
     */
    public void setDescription(String description, String charset) 
		throws MessagingException {
	MimeBodyPart.setDescription(this, description, charset);
    }

    /**
     * Get the languages specified in the "Content-Language" header
     * field of this message. The Content-Language header is defined by
     * RFC 1766. Returns <code>null</code> if this field is unavailable
     * or its value is absent. <p>
     *
     * This implementation uses the <code>getHeader</code> method
     * to obtain the requisite header field.
     *
     * @return			value of content-language header.
     * @exception		MessagingException for failures
     */
    @Override
    public String[] getContentLanguage() throws MessagingException {
	return MimeBodyPart.getContentLanguage(this);
    }

    /**
     * Set the "Content-Language" header of this MimePart. The
     * Content-Language header is defined by RFC 1766.
     *
     * @param languages 	array of language tags
     * @exception		IllegalWriteException if the underlying
     *				implementation does not support modification
     * @exception		IllegalStateException if this message is
     *				obtained from a READ_ONLY folder.
     * @exception		MessagingException for other failures
     */
    @Override
    public void setContentLanguage(String[] languages)
			throws MessagingException {
	MimeBodyPart.setContentLanguage(this, languages);
    }

    /**
     * Returns the value of the "Message-ID" header field. Returns
     * null if this field is unavailable or its value is absent. <p>
     *
     * The default implementation provided here uses the
     * <code>getHeader</code> method to return the value of the
     * "Message-ID" field.
     *
     * @return     Message-ID
     * @exception  MessagingException if the retrieval of this field
     *			causes any exception.
     * @see        javax.mail.search.MessageIDTerm
     * @since 	   JavaMail 1.1
     */
    public String getMessageID() throws MessagingException {
	return getHeader("Message-ID", null);
    }

    /**
     * Get the filename associated with this Message. <p>
     *
     * Returns the value of the "filename" parameter from the
     * "Content-Disposition" header field of this message. If it's
     * not available, returns the value of the "name" parameter from
     * the "Content-Type" header field of this BodyPart.
     * Returns <code>null</code> if both are absent. <p>
     *
     * If the <code>mail.mime.encodefilename</code> System property
     * is set to true, the {@link MimeUtility#decodeText
     * MimeUtility.decodeText} method will be used to decode the
     * filename.  While such encoding is not supported by the MIME
     * spec, many mailers use this technique to support non-ASCII
     * characters in filenames.  The default value of this property
     * is false.
     *
     * @return	filename
     * @exception		MessagingException for failures
     */
    @Override
    public String getFileName() throws MessagingException {
	return MimeBodyPart.getFileName(this);
    }

    /**
     * Set the filename associated with this part, if possible. <p>
     *
     * Sets the "filename" parameter of the "Content-Disposition"
     * header field of this message. <p>
     *
     * If the <code>mail.mime.encodefilename</code> System property
     * is set to true, the {@link MimeUtility#encodeText
     * MimeUtility.encodeText} method will be used to encode the
     * filename.  While such encoding is not supported by the MIME
     * spec, many mailers use this technique to support non-ASCII
     * characters in filenames.  The default value of this property
     * is false.
     *
     * @exception		IllegalWriteException if the underlying
     *				implementation does not support modification
     * @exception		IllegalStateException if this message is
     *				obtained from a READ_ONLY folder.
     * @exception		MessagingException for other failures
     */
    @Override
    public void setFileName(String filename) throws MessagingException {
	MimeBodyPart.setFileName(this, filename);	
    }

    private String getHeaderName(Message.RecipientType type)
				throws MessagingException {
	String headerName;

	if (type == Message.RecipientType.TO)
	    headerName = "To";
	else if (type == Message.RecipientType.CC)
	    headerName = "Cc";
	else if (type == Message.RecipientType.BCC)
	    headerName = "Bcc";
	else if (type == MimeMessage.RecipientType.NEWSGROUPS)
	    headerName = "Newsgroups";
	else
	    throw new MessagingException("Invalid Recipient Type");
	return headerName;
    }


    /**
     * Return a decoded input stream for this Message's "content". <p>
     *
     * This implementation obtains the input stream from the DataHandler,
     * that is, it invokes <code>getDataHandler().getInputStream()</code>.
     *
     * @return 		an InputStream
     * @exception       IOException this is typically thrown by the
     *			DataHandler. Refer to the documentation for
     *			javax.activation.DataHandler for more details.
     * @exception	MessagingException for other failures
     *
     * @see	#getContentStream
     * @see 	javax.activation.DataHandler#getInputStream
     */
    @Override
    public InputStream getInputStream() 
		throws IOException, MessagingException {
	return getDataHandler().getInputStream();
    }

    /**
     * Produce the raw bytes of the content. This method is used during
     * parsing, to create a DataHandler object for the content. Subclasses
     * that can provide a separate input stream for just the message 
     * content might want to override this method. <p>
     *
     * This implementation returns a SharedInputStream, if
     * <code>contentStream</code> is not null.  Otherwise, it
     * returns a ByteArrayInputStream constructed
     * out of the <code>content</code> byte array.
     *
     * @return	an InputStream containing the raw bytes
     * @exception	MessagingException for failures
     * @see #content
     */
    protected InputStream getContentStream() throws MessagingException {
	if (contentStream != null)
	    return ((SharedInputStream)contentStream).newStream(0, -1);
	if (content != null)
	    return new SharedByteArrayInputStream(content);

	throw new MessagingException("No MimeMessage content");
    }

    /**
     * Return an InputStream to the raw data with any Content-Transfer-Encoding
     * intact.  This method is useful if the "Content-Transfer-Encoding"
     * header is incorrect or corrupt, which would prevent the
     * <code>getInputStream</code> method or <code>getContent</code> method
     * from returning the correct data.  In such a case the application may
     * use this method and attempt to decode the raw data itself. <p>
     *
     * This implementation simply calls the <code>getContentStream</code>
     * method.
     *
     * @return	an InputStream containing the raw bytes
     * @exception	MessagingException for failures
     * @see	#getInputStream
     * @see	#getContentStream
     * @since	JavaMail 1.2
     */
    public InputStream getRawInputStream() throws MessagingException {
	return getContentStream();
    }

    /**                                                            
     * Return a DataHandler for this Message's content. <p>
     *
     * The implementation provided here works approximately as follows.
     * Note the use of the <code>getContentStream</code> method to 
     * generate the byte stream for the content. Also note that
     * any transfer-decoding is done automatically within this method.
     *
     * <blockquote><pre>
     *  getDataHandler() {
     *      if (dh == null) {
     *          dh = new DataHandler(new MimePartDataSource(this));
     *      }
     *      return dh;
     *  }
     *
     *  class MimePartDataSource implements DataSource {
     *      public getInputStream() {
     *          return MimeUtility.decode(
     *		     getContentStream(), getEncoding());
     *      }
     *	
     *		.... &lt;other DataSource methods&gt;
     *  }
     * </pre></blockquote><p>
     *
     * @exception	MessagingException for failures
     */
    @Override
    public synchronized DataHandler getDataHandler() 
		throws MessagingException {
	if (dh == null)
	    dh = new MimeBodyPart.MimePartDataHandler(this);
	return dh;
    }

    /**
     * Return the content as a Java object. The type of this
     * object is dependent on the content itself. For 
     * example, the native format of a "text/plain" content
     * is usually a String object. The native format for a "multipart"
     * message is always a Multipart subclass. For content types that are
     * unknown to the DataHandler system, an input stream is returned
     * as the content. <p>
     *
     * This implementation obtains the content from the DataHandler,
     * that is, it invokes <code>getDataHandler().getContent()</code>.
     * If the content is a Multipart or Message object and was created by
     * parsing a stream, the object is cached and returned in subsequent
     * calls so that modifications to the content will not be lost.
     *
     * @return          Object
     * @see		javax.mail.Part
     * @see 		javax.activation.DataHandler#getContent
     * @exception       IOException this is typically thrown by the
     *			DataHandler. Refer to the documentation for
     *			javax.activation.DataHandler for more details.
     * @exception       MessagingException for other failures
     */
    @Override
    public Object getContent() throws IOException, MessagingException {
	if (cachedContent != null)
	    return cachedContent;
	Object c;
	try {
	    c = getDataHandler().getContent();
	} catch (FolderClosedIOException fex) {
	    throw new FolderClosedException(fex.getFolder(), fex.getMessage());
	} catch (MessageRemovedIOException mex) {
	    throw new MessageRemovedException(mex.getMessage());
	}
	if (MimeBodyPart.cacheMultipart &&
		(c instanceof Multipart || c instanceof Message) &&
		(content != null || contentStream != null)) {
	    cachedContent = c;
	    /*
	     * We may abandon the input stream so make sure
	     * the MimeMultipart has consumed the stream.
	     */
	    if (c instanceof MimeMultipart)
		((MimeMultipart)c).parse();
	}
	return c;
    }

    /**
     * This method provides the mechanism to set this part's content.
     * The given DataHandler object should wrap the actual content.
     *
     * @param   dh      The DataHandler for the content.
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    @Override
    public synchronized void setDataHandler(DataHandler dh) 
		throws MessagingException {
	this.dh = dh;
	cachedContent = null;
	MimeBodyPart.invalidateContentHeaders(this);
    }

    /**
     * A convenience method for setting this Message's content. <p>
     *
     * The content is wrapped in a DataHandler object. Note that a
     * DataContentHandler class for the specified type should be
     * available to the JavaMail implementation for this to work right.
     * i.e., to do <code>setContent(foobar, "application/x-foobar")</code>,
     * a DataContentHandler for "application/x-foobar" should be installed.
     * Refer to the Java Activation Framework for more information.
     *
     * @param	o	the content object
     * @param	type	Mime type of the object
     * @exception       IllegalWriteException if the underlying
     *			implementation does not support modification of
     *			existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    @Override
    public void setContent(Object o, String type) 
			throws MessagingException {
	if (o instanceof Multipart)
	    setContent((Multipart)o);
	else
	    setDataHandler(new DataHandler(o, type));
    }

    /**
     * Convenience method that sets the given String as this
     * part's content, with a MIME type of "text/plain". If the
     * string contains non US-ASCII characters. it will be encoded
     * using the platform's default charset. The charset is also
     * used to set the "charset" parameter.<p>
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
    public void setText(String text) throws MessagingException {
	setText(text, null);
    }

    /**
     * Convenience method that sets the given String as this part's
     * content, with a MIME type of "text/plain" and the specified
     * charset. The given Unicode string will be charset-encoded
     * using the specified charset. The charset is also used to set
     * the "charset" parameter.
     *
     * @param	text	the text content to set
     * @param	charset	the charset to use for the text
     * @exception	MessagingException	if an error occurs
     */
    @Override
    public void setText(String text, String charset)
			throws MessagingException {
	MimeBodyPart.setText(this, text, charset, "plain");
    }

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
    @Override
    public void setText(String text, String charset, String subtype)
                        throws MessagingException {
	MimeBodyPart.setText(this, text, charset, subtype);
    }

    /**
     * This method sets the Message's content to a Multipart object.
     *
     * @param  mp      The multipart object that is the Message's content
     * @exception       IllegalWriteException if the underlying
     *			implementation does not support modification of
     *			existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    @Override
    public void setContent(Multipart mp) throws MessagingException {
	setDataHandler(new DataHandler(mp, mp.getContentType()));
	mp.setParent(this);
    }

    /**
     * Get a new Message suitable for a reply to this message.
     * The new Message will have its attributes and headers 
     * set up appropriately.  Note that this new message object
     * will be empty, i.e., it will <strong>not</strong> have a "content".
     * These will have to be suitably filled in by the client. <p>
     *
     * If <code>replyToAll</code> is set, the new Message will be addressed
     * to all recipients of this message.  Otherwise, the reply will be
     * addressed to only the sender of this message (using the value
     * of the <code>getReplyTo</code> method).  <p>
     *
     * The "Subject" field is filled in with the original subject
     * prefixed with "Re:" (unless it already starts with "Re:").
     * The "In-Reply-To" header is set in the new message if this
     * message has a "Message-Id" header.  The <code>ANSWERED</code>
     * flag is set in this message.
     *
     * The current implementation also sets the "References" header
     * in the new message to include the contents of the "References"
     * header (or, if missing, the "In-Reply-To" header) in this message,
     * plus the contents of the "Message-Id" header of this message,
     * as described in RFC 2822.
     *
     * @param	replyToAll	reply should be sent to all recipients
     *				of this message
     * @return		the reply Message
     * @exception	MessagingException for failures
     */
    @Override
    public Message reply(boolean replyToAll) throws MessagingException {
	return reply(replyToAll, true);
    }

    /**
     * Get a new Message suitable for a reply to this message.
     * The new Message will have its attributes and headers 
     * set up appropriately.  Note that this new message object
     * will be empty, i.e., it will <strong>not</strong> have a "content".
     * These will have to be suitably filled in by the client. <p>
     *
     * If <code>replyToAll</code> is set, the new Message will be addressed
     * to all recipients of this message.  Otherwise, the reply will be
     * addressed to only the sender of this message (using the value
     * of the <code>getReplyTo</code> method).  <p>
     *
     * If <code>setAnswered</code> is set, the
     * {@link javax.mail.Flags.Flag#ANSWERED ANSWERED} flag is set
     * in this message. <p>
     *
     * The "Subject" field is filled in with the original subject
     * prefixed with "Re:" (unless it already starts with "Re:").
     * The "In-Reply-To" header is set in the new message if this
     * message has a "Message-Id" header.
     *
     * The current implementation also sets the "References" header
     * in the new message to include the contents of the "References"
     * header (or, if missing, the "In-Reply-To" header) in this message,
     * plus the contents of the "Message-Id" header of this message,
     * as described in RFC 2822.
     *
     * @param	replyToAll	reply should be sent to all recipients
     *				of this message
     * @param	setAnswered	set the ANSWERED flag in this message?
     * @return		the reply Message
     * @exception	MessagingException for failures
     * @since		JavaMail 1.5
     */
    public Message reply(boolean replyToAll, boolean setAnswered)
				throws MessagingException {
	MimeMessage reply = createMimeMessage(session);
	/*
	 * Have to manipulate the raw Subject header so that we don't lose
	 * any encoding information.  This is safe because "Re:" isn't
	 * internationalized and (generally) isn't encoded.  If the entire
	 * Subject header is encoded, prefixing it with "Re: " still leaves
	 * a valid and correct encoded header.
	 */
	String subject = getHeader("Subject", null);
	if (subject != null) {
	    if (!subject.regionMatches(true, 0, "Re: ", 0, 4))
		subject = "Re: " + subject;
	    reply.setHeader("Subject", subject);
	}
	Address a[] = getReplyTo();
	reply.setRecipients(Message.RecipientType.TO, a);
	if (replyToAll) {
	    List<Address> v = new ArrayList<>();
	    // add my own address to list
	    InternetAddress me = InternetAddress.getLocalAddress(session);
	    if (me != null)
		v.add(me);
	    // add any alternate names I'm known by
	    String alternates = null;
	    if (session != null)
		alternates = session.getProperty("mail.alternates");
	    if (alternates != null)
		eliminateDuplicates(v,
				InternetAddress.parse(alternates, false));
	    // should we Cc all other original recipients?
	    String replyallccStr = null;
	    boolean replyallcc = false;
	    if (session != null)
		replyallcc = PropUtil.getBooleanProperty(
						session.getProperties(),
						"mail.replyallcc", false);
	    // add the recipients from the To field so far
	    eliminateDuplicates(v, a);
	    a = getRecipients(Message.RecipientType.TO);
	    a = eliminateDuplicates(v, a);
	    if (a != null && a.length > 0) {
		if (replyallcc)
		    reply.addRecipients(Message.RecipientType.CC, a);
		else
		    reply.addRecipients(Message.RecipientType.TO, a);
	    }
	    a = getRecipients(Message.RecipientType.CC);
	    a = eliminateDuplicates(v, a);
	    if (a != null && a.length > 0)
		reply.addRecipients(Message.RecipientType.CC, a);
	    // don't eliminate duplicate newsgroups
	    a = getRecipients(RecipientType.NEWSGROUPS);
	    if (a != null && a.length > 0)
		reply.setRecipients(RecipientType.NEWSGROUPS, a);
	}

	String msgId = getHeader("Message-Id", null);
	if (msgId != null)
	    reply.setHeader("In-Reply-To", msgId);

	/*
	 * Set the References header as described in RFC 2822:
	 *
	 * The "References:" field will contain the contents of the parent's
	 * "References:" field (if any) followed by the contents of the parent's
	 * "Message-ID:" field (if any).  If the parent message does not contain
	 * a "References:" field but does have an "In-Reply-To:" field
	 * containing a single message identifier, then the "References:" field
	 * will contain the contents of the parent's "In-Reply-To:" field
	 * followed by the contents of the parent's "Message-ID:" field (if
	 * any).  If the parent has none of the "References:", "In-Reply-To:",
	 * or "Message-ID:" fields, then the new message will have no
	 * "References:" field.
	 */
	String refs = getHeader("References", " ");
	if (refs == null) {
	    // XXX - should only use if it contains a single message identifier
	    refs = getHeader("In-Reply-To", " ");
	}
	if (msgId != null) {
	    if (refs != null)
		refs = MimeUtility.unfold(refs) + " " + msgId;
	    else
		refs = msgId;
	}
	if (refs != null)
	    reply.setHeader("References", MimeUtility.fold(12, refs));

	if (setAnswered) {
	    try {
		setFlags(answeredFlag, true);
	    } catch (MessagingException mex) {
		// ignore it
	    }
	}
	return reply;
    }

    // used above in reply()
    private static final Flags answeredFlag = new Flags(Flags.Flag.ANSWERED);

    /**
     * Check addrs for any duplicates that may already be in v.
     * Return a new array without the duplicates.  Add any new
     * addresses to v.  Note that the input array may be modified.
     */
    private Address[] eliminateDuplicates(List<Address> v, Address[] addrs) {
	if (addrs == null)
	    return null;
	int gone = 0;
	for (int i = 0; i < addrs.length; i++) {
	    boolean found = false;
	    // search the list for this address
	    for (int j = 0; j < v.size(); j++) {
		if (((InternetAddress)v.get(j)).equals(addrs[i])) {
		    // found it; count it and remove it from the input array
		    found = true;
		    gone++;
		    addrs[i] = null;
		    break;
		}
	    }
	    if (!found)
		v.add(addrs[i]);	// add new address to list
	}
	// if we found any duplicates, squish the array
	if (gone != 0) {
	    Address[] a;
	    // new array should be same type as original array
	    // XXX - there must be a better way, perhaps reflection?
	    if (addrs instanceof InternetAddress[])
		a = new InternetAddress[addrs.length - gone];
	    else
		a = new Address[addrs.length - gone];
	    for (int i = 0, j = 0; i < addrs.length; i++)
		if (addrs[i] != null)
		    a[j++] = addrs[i];
	    addrs = a;
	}
	return addrs;
    }

    /**
     * Output the message as an RFC 822 format stream. <p>
     *
     * Note that, depending on how the messag was constructed, it may
     * use a variety of line termination conventions.  Generally the
     * output should be sent through an appropriate FilterOutputStream
     * that converts the line terminators to the desired form, either
     * CRLF for MIME compatibility and for use in Internet protocols,
     * or the local platform's line terminator for storage in a local
     * text file. <p>
     *
     * This implementation calls the <code>writeTo(OutputStream,
     * String[])</code> method with a null ignore list.
     *
     * @exception IOException	if an error occurs writing to the stream
     *				or if an error is generated by the
     *				javax.activation layer.
     * @exception MessagingException for other failures
     * @see javax.activation.DataHandler#writeTo
     */
    @Override
    public void writeTo(OutputStream os)
				throws IOException, MessagingException {
	writeTo(os, null);
    }

    /**
     * Output the message as an RFC 822 format stream, without
     * specified headers.  If the <code>saved</code> flag is not set,
     * the <code>saveChanges</code> method is called.
     * If the <code>modified</code> flag is not
     * set and the <code>content</code> array is not null, the
     * <code>content</code> array is written directly, after
     * writing the appropriate message headers.
     *
     * @param	os		the stream to write to
     * @param	ignoreList	the headers to not include in the output
     * @exception IOException	if an error occurs writing to the stream
     *				or if an error is generated by the
     *				javax.activation layer.
     * @exception javax.mail.MessagingException for other failures
     * @see javax.activation.DataHandler#writeTo
     */
    public void writeTo(OutputStream os, String[] ignoreList)
				throws IOException, MessagingException {
	if (!saved)
	    saveChanges();

	if (modified) {
	    MimeBodyPart.writeTo(this, os, ignoreList);
	    return;
	}

	// Else, the content is untouched, so we can just output it
	// First, write out the header
	Enumeration<String> hdrLines = getNonMatchingHeaderLines(ignoreList);
	LineOutputStream los = new LineOutputStream(os, allowutf8);
	while (hdrLines.hasMoreElements())
	    los.writeln(hdrLines.nextElement());

	// The CRLF separator between header and content
	los.writeln();

	// Finally, the content. 
	if (content == null) {
	    // call getContentStream to give subclass a chance to
	    // provide the data on demand
	    InputStream is = null;
	    byte[] buf = new byte[8192];
	    try {
		is = getContentStream();
		// now copy the data to the output stream
		int len;
		while ((len = is.read(buf)) > 0)
		    os.write(buf, 0, len);
	    } finally {
		if (is != null)
		    is.close();
		buf = null;
	    }
	} else {
	    os.write(content);
	}
	os.flush();
    }

    /**
     * Get all the headers for this header_name. Note that certain
     * headers may be encoded as per RFC 2047 if they contain 
     * non US-ASCII characters and these should be decoded. <p>
     *
     * This implementation obtains the headers from the 
     * <code>headers</code> InternetHeaders object.
     *
     * @param	name	name of header
     * @return	array of headers
     * @exception       MessagingException for failures
     * @see 	javax.mail.internet.MimeUtility
     */
    @Override
    public String[] getHeader(String name)
			throws MessagingException {
	return headers.getHeader(name);
    }

    /**
     * Get all the headers for this header name, returned as a single
     * String, with headers separated by the delimiter. If the
     * delimiter is <code>null</code>, only the first header is 
     * returned.
     *
     * @param name		the name of this header
     * @param delimiter		separator between values
     * @return                  the value fields for all headers with 
     *				this name
     * @exception       	MessagingException for failures
     */
    @Override
    public String getHeader(String name, String delimiter)
				throws MessagingException {
	return headers.getHeader(name, delimiter);
    }

    /**
     * Set the value for this header_name. Replaces all existing
     * header values with this new value. Note that RFC 822 headers
     * must contain only US-ASCII characters, so a header that
     * contains non US-ASCII characters must have been encoded by the
     * caller as per the rules of RFC 2047.
     *
     * @param	name 	header name
     * @param	value	header value
     * @see 	javax.mail.internet.MimeUtility
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    @Override
    public void setHeader(String name, String value)
                                throws MessagingException {
	headers.setHeader(name, value);	
    }

    /**
     * Add this value to the existing values for this header_name.
     * Note that RFC 822 headers must contain only US-ASCII 
     * characters, so a header that contains non US-ASCII characters 
     * must have been encoded as per the rules of RFC 2047.
     *
     * @param	name 	header name
     * @param	value	header value
     * @see 	javax.mail.internet.MimeUtility
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    @Override
    public void addHeader(String name, String value)
                                throws MessagingException {
	headers.addHeader(name, value);
    }

    /**
     * Remove all headers with this name.
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    @Override
    public void removeHeader(String name)
                                throws MessagingException {
	headers.removeHeader(name);
    }

    /**
     * Return all the headers from this Message as an enumeration
     * of Header objects. <p>
     *
     * Note that certain headers may be encoded as per RFC 2047 
     * if they contain non US-ASCII characters and these should 
     * be decoded. <p>
     *
     * This implementation obtains the headers from the 
     * <code>headers</code> InternetHeaders object.
     *
     * @return	array of header objects
     * @exception  MessagingException for failures
     * @see 	javax.mail.internet.MimeUtility
     */
    @Override
    public Enumeration<Header> getAllHeaders() throws MessagingException {
	return headers.getAllHeaders();	
    }

    /**
     * Return matching headers from this Message as an Enumeration of
     * Header objects. This implementation obtains the headers from
     * the <code>headers</code> InternetHeaders object.
     *
     * @exception  MessagingException for failures
     */
    @Override
    public Enumeration<Header> getMatchingHeaders(String[] names)
			throws MessagingException {
	return headers.getMatchingHeaders(names);
    }

    /**
     * Return non-matching headers from this Message as an
     * Enumeration of Header objects. This implementation 
     * obtains the header from the <code>headers</code> InternetHeaders object.
     *
     * @exception  MessagingException for failures
     */
    @Override
    public Enumeration<Header> getNonMatchingHeaders(String[] names)
			throws MessagingException {
	return headers.getNonMatchingHeaders(names);
    }

    /**
     * Add a raw RFC 822 header-line. 
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception  	MessagingException for other failures
     */
    @Override
    public void addHeaderLine(String line) throws MessagingException {
	headers.addHeaderLine(line);
    }

    /**
     * Get all header lines as an Enumeration of Strings. A Header
     * line is a raw RFC 822 header-line, containing both the "name" 
     * and "value" field. 
     *
     * @exception  	MessagingException for failures
     */
    @Override
    public Enumeration<String> getAllHeaderLines() throws MessagingException {
	return headers.getAllHeaderLines();
    }

    /**
     * Get matching header lines as an Enumeration of Strings. 
     * A Header line is a raw RFC 822 header-line, containing both 
     * the "name" and "value" field.
     *
     * @exception  	MessagingException for failures
     */
    @Override
    public Enumeration<String> getMatchingHeaderLines(String[] names)
                                        throws MessagingException {
	return headers.getMatchingHeaderLines(names);
    }

    /**
     * Get non-matching header lines as an Enumeration of Strings. 
     * A Header line is a raw RFC 822 header-line, containing both 
     * the "name" and "value" field.
     *
     * @exception  	MessagingException for failures
     */
    @Override
    public Enumeration<String> getNonMatchingHeaderLines(String[] names)
                                        throws MessagingException {
	return headers.getNonMatchingHeaderLines(names);
    }

    /**
     * Return a <code>Flags</code> object containing the flags for 
     * this message. <p>
     *
     * Note that a clone of the internal Flags object is returned, so
     * modifying the returned Flags object will not affect the flags
     * of this message.
     *
     * @return          Flags object containing the flags for this message
     * @exception  	MessagingException for failures
     * @see 		javax.mail.Flags
     */
    @Override
    public synchronized Flags getFlags() throws MessagingException {
	return (Flags)flags.clone();
    }

    /**
     * Check whether the flag specified in the <code>flag</code>
     * argument is set in this message. <p>
     *
     * This implementation checks this message's internal 
     * <code>flags</code> object.
     *
     * @param flag	the flag
     * @return		value of the specified flag for this message
     * @exception       MessagingException for failures
     * @see 		javax.mail.Flags.Flag
     * @see		javax.mail.Flags.Flag#ANSWERED
     * @see		javax.mail.Flags.Flag#DELETED
     * @see		javax.mail.Flags.Flag#DRAFT
     * @see		javax.mail.Flags.Flag#FLAGGED
     * @see		javax.mail.Flags.Flag#RECENT
     * @see		javax.mail.Flags.Flag#SEEN
     */
    @Override
    public synchronized boolean isSet(Flags.Flag flag)
				throws MessagingException {
	return (flags.contains(flag));
    }

    /**
     * Set the flags for this message. <p>
     *
     * This implementation modifies the <code>flags</code> field.
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception  	MessagingException for other failures
     */
    @Override
    public synchronized void setFlags(Flags flag, boolean set)
			throws MessagingException {
	if (set)
	    flags.add(flag);
	else
	    flags.remove(flag);
    }

    /**
     * Updates the appropriate header fields of this message to be
     * consistent with the message's contents. If this message is
     * contained in a Folder, any changes made to this message are
     * committed to the containing folder. <p>
     *
     * If any part of a message's headers or contents are changed,
     * <code>saveChanges</code> must be called to ensure that those
     * changes are permanent. Otherwise, any such modifications may or 
     * may not be saved, depending on the folder implementation. <p>
     *
     * Messages obtained from folders opened READ_ONLY should not be
     * modified and saveChanges should not be called on such messages. <p>
     *
     * This method sets the <code>modified</code> flag to true, the
     * <code>save</code> flag to true, and then calls the
     * <code>updateHeaders</code> method.
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception  	MessagingException for other failures
     */
    @Override
    public void saveChanges() throws MessagingException {
	modified = true;
	saved = true;
	updateHeaders();
    }

    /**
     * Update the Message-ID header.  This method is called
     * by the <code>updateHeaders</code> and allows a subclass
     * to override only the algorithm for choosing a Message-ID.
     *
     * @exception  	MessagingException for failures
     * @since		JavaMail 1.4
     */
    protected void updateMessageID() throws MessagingException {
	setHeader("Message-ID", 
		  "<" + UniqueValue.getUniqueMessageIDValue(session) + ">");
          
    }	

    /**
     * Called by the <code>saveChanges</code> method to actually
     * update the MIME headers.  The implementation here sets the
     * <code>Content-Transfer-Encoding</code> header (if needed
     * and not already set), the <code>Date</code> header (if
     * not already set), the <code>MIME-Version</code> header
     * and the <code>Message-ID</code> header. Also, if the content
     * of this message is a <code>MimeMultipart</code>, its
     * <code>updateHeaders</code> method is called. <p>
     *
     * If the {@link #cachedContent} field is not null (that is,
     * it references a Multipart or Message object), then
     * that object is used to set a new DataHandler, any
     * stream data used to create this object is discarded,
     * and the {@link #cachedContent} field is cleared.
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception  	MessagingException for other failures
     */
    protected synchronized void updateHeaders() throws MessagingException {
	MimeBodyPart.updateHeaders(this);	
	setHeader("MIME-Version", "1.0");
	if (getHeader("Date") == null)
	    setSentDate(new Date());
        updateMessageID();

	if (cachedContent != null) {
	    dh = new DataHandler(cachedContent, getContentType());
	    cachedContent = null;
	    content = null;
	    if (contentStream != null) {
		try {
		    contentStream.close();
		} catch (IOException ioex) { }	// nothing to do
	    }
	    contentStream = null;
	}
    }

    /**
     * Create and return an InternetHeaders object that loads the
     * headers from the given InputStream.  Subclasses can override
     * this method to return a subclass of InternetHeaders, if
     * necessary.  This implementation simply constructs and returns
     * an InternetHeaders object.
     *
     * @return	an InternetHeaders object
     * @param	is	the InputStream to read the headers from
     * @exception  	MessagingException for failures
     * @since		JavaMail 1.2
     */
    protected InternetHeaders createInternetHeaders(InputStream is)
				throws MessagingException {
	return new InternetHeaders(is, allowutf8);
    }

    /**
     * Create and return a MimeMessage object.  The reply method
     * uses this method to create the MimeMessage object that it
     * will return.  Subclasses can override this method to return
     * a subclass of MimeMessage.  This implementation simply constructs
     * and returns a MimeMessage object using the supplied Session.
     *
     * @param	session	the Session to use for the new message
     * @return		the new MimeMessage object
     * @exception  	MessagingException for failures
     * @since		JavaMail 1.4
     */
    protected MimeMessage createMimeMessage(Session session)
				throws MessagingException {
	return new MimeMessage(session);
    }
}
