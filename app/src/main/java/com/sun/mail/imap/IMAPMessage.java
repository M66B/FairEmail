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

package com.sun.mail.imap;

import java.util.Date;
import java.io.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import com.sun.mail.util.ReadableMime;
import com.sun.mail.iap.*;
import com.sun.mail.imap.protocol.*;

/**
 * This class implements an IMAPMessage object. <p>
 *
 * An IMAPMessage object starts out as a light-weight object. It gets
 * filled-in incrementally when a request is made for some item. Or
 * when a prefetch is done using the FetchProfile. <p>
 *
 * An IMAPMessage has a messageNumber and a sequenceNumber. The 
 * messageNumber is its index into its containing folder's messageCache.
 * The sequenceNumber is its IMAP sequence-number.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */
/*
 * The lock hierarchy is that the lock on the IMAPMessage object, if
 * it's acquired at all, must be acquired before the message cache lock.
 * The IMAPMessage lock protects the message flags, sort of.
 *
 * XXX - I'm not convinced that all fields of IMAPMessage are properly
 * protected by locks.
 */

public class IMAPMessage extends MimeMessage implements ReadableMime {
    protected BODYSTRUCTURE bs;		// BODYSTRUCTURE
    protected ENVELOPE envelope;	// ENVELOPE

    /**
     * A map of the extension FETCH items.  In addition to saving the
     * data in this map, an entry in this map indicates that we *have*
     * the data, and so it doesn't need to be fetched again.  The map
     * is created only when needed, to avoid significantly increasing
     * the effective size of an IMAPMessage object.
     *
     * @since JavaMail 1.4.6
     */
    protected Map<String, Object> items;		// Map<String,Object>

    private Date receivedDate;		// INTERNALDATE
    private long size = -1;		// RFC822.SIZE

    private Boolean peek;		// use BODY.PEEK when fetching content?

    // this message's IMAP UID
    private volatile long uid = -1;

    // this message's IMAP MODSEQ - RFC 4551 CONDSTORE
    private volatile long modseq = -1;

    // this message's IMAP sectionId (null for toplevel message, 
    // 	non-null for a nested message)
    protected String sectionId;

    // processed values
    private String type;		// Content-Type (with params)
    private String subject;		// decoded (Unicode) subject
    private String description;		// decoded (Unicode) desc

    // Indicates that we've loaded *all* headers for this message
    private volatile boolean headersLoaded = false;

    // Indicates that we've cached the body of this message
    private volatile boolean bodyLoaded = false;

    /* Hashtable of names of headers we've loaded from the server.
     * Used in isHeaderLoaded() and getHeaderLoaded() to keep track
     * of those headers we've attempted to load from the server. We
     * need this table of names to avoid multiple attempts at loading
     * headers that don't exist for a particular message.
     *
     * Could this somehow be included in the InternetHeaders object ??
     */
    private Hashtable<String, String> loadedHeaders
	    = new Hashtable<>(1);

    // This is our Envelope
    static final String EnvelopeCmd = "ENVELOPE INTERNALDATE RFC822.SIZE";

    /**
     * Constructor.
     *
     * @param	folder	the folder containing this message
     * @param	msgnum	the message sequence number
     */
    protected IMAPMessage(IMAPFolder folder, int msgnum) {
	super(folder, msgnum);
	flags = null;
    }

    /**
     * Constructor, for use by IMAPNestedMessage.
     *
     * @param	session	the Session
     */
    protected IMAPMessage(Session session) {
	super(session);
    }

    /**
     * Get this message's folder's protocol connection.
     * Throws FolderClosedException, if the protocol connection
     * is not available.
     *
     * ASSERT: Must hold the messageCacheLock.
     *
     * @return	the IMAPProtocol object for the containing folder
     * @exception	ProtocolException for protocol errors
     * @exception	FolderClosedException if the folder is closed
     */
    protected IMAPProtocol getProtocol()
			    throws ProtocolException, FolderClosedException {
	((IMAPFolder)folder).waitIfIdle();
	IMAPProtocol p = ((IMAPFolder)folder).protocol;
	if (p == null)
	    throw new FolderClosedException(folder);
	else
	    return p;
    }

    /*
     * Is this an IMAP4 REV1 server?
     */
    protected boolean isREV1() throws FolderClosedException {
	// access the folder's protocol object without waiting
	// for IDLE to complete
	IMAPProtocol p = ((IMAPFolder)folder).protocol;
	if (p == null)
	    throw new FolderClosedException(folder);
	else
	    return p.isREV1();
    }

    /**
     * Get the messageCacheLock, associated with this Message's
     * Folder.
     *
     * @return	the message cache lock object
     */
    protected Object getMessageCacheLock() {
	return ((IMAPFolder)folder).messageCacheLock;
    }

    /**
     * Get this message's IMAP sequence number.
     *
     * ASSERT: This method must be called only when holding the
     * 	messageCacheLock.
     *
     * @return	the message sequence number
     */
    protected int getSequenceNumber() {
	return ((IMAPFolder)folder).messageCache.seqnumOf(getMessageNumber());
    }

    /**
     * Wrapper around the protected method Message.setMessageNumber() to 
     * make that method accessible to IMAPFolder.
     */
    @Override
    protected void setMessageNumber(int msgnum) {
	super.setMessageNumber(msgnum);
    }

    /**
     * Return the UID for this message.
     * Returns -1 if not known; use UIDFolder.getUID() in this case.
     *
     * @return	the UID
     * @see	javax.mail.UIDFolder#getUID
     */
    protected long getUID() {
	return uid;
    }

    protected void setUID(long uid) {
	this.uid = uid;
    }

    /**
     * Return the modification sequence number (MODSEQ) for this message.
     * Returns -1 if not known.
     *
     * @return	the modification sequence number
     * @exception	MessagingException for failures
     * @see	"RFC 4551"
     * @since	JavaMail 1.5.1
     */
    public synchronized long getModSeq() throws MessagingException {
	if (modseq != -1)
	    return modseq;

	synchronized (getMessageCacheLock()) { // Acquire Lock
	    try {
		IMAPProtocol p = getProtocol();
		checkExpunged(); // insure that message is not expunged
		MODSEQ ms = p.fetchMODSEQ(getSequenceNumber());

		if (ms != null)
		    modseq = ms.modseq;
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		throw new MessagingException(pex.getMessage(), pex);
	    }
	}
	return modseq;
    }

    long _getModSeq() {
	return modseq;
    }

    void setModSeq(long modseq) {
	this.modseq = modseq;
    }

    // expose to MessageCache
    @Override
    protected void setExpunged(boolean set) {
	super.setExpunged(set);
    }

    // Convenience routine
    protected void checkExpunged() throws MessageRemovedException {
	if (expunged)
	    throw new MessageRemovedException();
    }

    /**
     * Do a NOOP to force any untagged EXPUNGE responses
     * and then check if this message is expunged.
     *
     * @exception	MessageRemovedException if the message has been removed
     * @exception	FolderClosedException if the folder has been closed
     */
    protected void forceCheckExpunged()
			throws MessageRemovedException, FolderClosedException {
	synchronized (getMessageCacheLock()) {
	    try {
		getProtocol().noop();
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		// ignore it
	    }
	}
	if (expunged)
	    throw new MessageRemovedException();
    }

    // Return the block size for FETCH requests
    // MUST be overridden by IMAPNestedMessage
    protected int getFetchBlockSize() {
	return ((IMAPStore)folder.getStore()).getFetchBlockSize();
    }

    // Should we ignore the size in the BODYSTRUCTURE?
    // MUST be overridden by IMAPNestedMessage
    protected boolean ignoreBodyStructureSize() {
	return ((IMAPStore)folder.getStore()).ignoreBodyStructureSize();
    }

    /**
     * Get the "From" attribute.
     */
    @Override
    public Address[] getFrom() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getFrom();
	loadEnvelope();
	InternetAddress[] a = envelope.from;
	/*
	 * Per RFC 2822, the From header is required, and thus the IMAP
	 * spec also requires that it be present, but we know that in
	 * practice it is often missing.  Some servers fill in the
	 * From field with the Sender field in this case, but at least
	 * Exchange 2007 does not.  Use the same fallback strategy used
	 * by MimeMessage.
	 */
	if (a == null || a.length == 0)
	    a = envelope.sender;
	return aaclone(a);
    }

    @Override
    public void setFrom(Address address) throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public void addFrom(Address[] addresses) throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    /**
     * Get the "Sender" attribute.
     */
    @Override
    public Address getSender() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getSender();
	loadEnvelope();
	if (envelope.sender != null && envelope.sender.length > 0)
		return (envelope.sender)[0];	// there can be only one sender
	else 
		return null;
    }
	

    @Override
    public void setSender(Address address) throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }    

    /**
     * Get the desired Recipient type.
     */
    @Override
    public Address[] getRecipients(Message.RecipientType type)
				throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getRecipients(type);
	loadEnvelope();

	if (type == Message.RecipientType.TO)
	    return aaclone(envelope.to);
	else if (type == Message.RecipientType.CC)
	    return aaclone(envelope.cc);
	else if (type == Message.RecipientType.BCC)
	    return aaclone(envelope.bcc);
	else
	    return super.getRecipients(type);
    }

    @Override
    public void setRecipients(Message.RecipientType type, Address[] addresses)
			throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public void addRecipients(Message.RecipientType type, Address[] addresses)
			throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get the ReplyTo addresses.
     */
    @Override
    public Address[] getReplyTo() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getReplyTo();
	loadEnvelope();
	/*
	 * The IMAP spec requires that the Reply-To field never be
	 * null, but at least Exchange 2007 fails to fill it in in
	 * some cases.  Use the same fallback strategy used by
	 * MimeMessage.
	 */
	if (envelope.replyTo == null || envelope.replyTo.length == 0)
	    return getFrom();
	return aaclone(envelope.replyTo);
    }

    @Override
    public void setReplyTo(Address[] addresses) throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get the decoded subject.
     */
    @Override
    public String getSubject() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getSubject();

	if (subject != null) // already cached ?
	    return subject;

	loadEnvelope();
	if (envelope.subject == null) // no subject
	    return null;

	// Cache and return the decoded value.
	try {
	    // The server *should* unfold the value, but just in case it
	    // doesn't we unfold it here.
	    subject =
		MimeUtility.decodeText(MimeUtility.unfold(envelope.subject));
	} catch (UnsupportedEncodingException ex) {
	    subject = envelope.subject;
	}

	return subject;
    }

    @Override
    public void setSubject(String subject, String charset) 
		throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get the SentDate.
     */
    @Override
    public Date getSentDate() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getSentDate();
	loadEnvelope();
	if (envelope.date == null)
	    return null;
	else
	    return new Date(envelope.date.getTime());
    }

    @Override
    public void setSentDate(Date d) throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get the received date (INTERNALDATE).
     */
    @Override
    public Date getReceivedDate() throws MessagingException {
	checkExpunged();
	if (receivedDate == null)
	    loadEnvelope(); // have to go to the server for this
	if (receivedDate == null)
	    return null;
	else
	    return new Date(receivedDate.getTime());
    }

    /**
     * Get the message size. <p>
     *
     * Note that this returns RFC822.SIZE.  That is, it's the
     * size of the whole message, header and body included.
     * Note also that if the size of the message is greater than
     * Integer.MAX_VALUE (2GB), this method returns Integer.MAX_VALUE.
     */
    @Override
    public int getSize() throws MessagingException {
	checkExpunged();
	// if bodyLoaded, size is already set
	if (size == -1)
	    loadEnvelope();	// XXX - could just fetch the size
	if (size > Integer.MAX_VALUE)
	    return Integer.MAX_VALUE;	// the best we can do...
	else
	    return (int)size;
    }

    /**
     * Get the message size as a long. <p>
     *
     * Suitable for messages that might be larger than 2GB.
     * @return	the message size as a long integer
     * @exception	MessagingException for failures
     * @since	JavaMail 1.6
     */
    public long getSizeLong() throws MessagingException {
	checkExpunged();
	// if bodyLoaded, size is already set
	if (size == -1)
	    loadEnvelope();	// XXX - could just fetch the size
	return size;
    }

    /**
     * Get the total number of lines. <p>
     *
     * Returns the "body_fld_lines" field from the
     * BODYSTRUCTURE. Note that this field is available
     * only for text/plain and message/rfc822 types
     */
    @Override
    public int getLineCount() throws MessagingException {
	checkExpunged();
	// XXX - superclass doesn't implement this
	loadBODYSTRUCTURE();
	return bs.lines;
    }

    /** 
     * Get the content language.
     */
    @Override
    public String[] getContentLanguage() throws MessagingException {
    	checkExpunged();
	if (bodyLoaded)
	    return super.getContentLanguage();
    	loadBODYSTRUCTURE();
    	if (bs.language != null)
	    return bs.language.clone();
    	else
	    return null;
    }
 
    @Override
    public void setContentLanguage(String[] languages)
				throws MessagingException {
    	throw new IllegalWriteException("IMAPMessage is read-only");
    }
 
    /**
     * Get the In-Reply-To header.
     *
     * @return	the In-Reply-To header
     * @exception	MessagingException for failures
     * @since	JavaMail 1.3.3
     */
    public String getInReplyTo() throws MessagingException {
    	checkExpunged();
	if (bodyLoaded)
	    return super.getHeader("In-Reply-To", " ");
    	loadEnvelope();
    	return envelope.inReplyTo;
    }
 
    /**
     * Get the Content-Type.
     *
     * Generate this header from the BODYSTRUCTURE. Append parameters
     * as well.
     */
    @Override
    public synchronized String getContentType() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getContentType();

	// If we haven't cached the type yet ..
	if (type == null) {
	    loadBODYSTRUCTURE();
	    // generate content-type from BODYSTRUCTURE
	    ContentType ct = new ContentType(bs.type, bs.subtype, bs.cParams);
	    type = ct.toString();
	}
	return type;
    }

    /**
     * Get the Content-Disposition.
     */
    @Override
    public String getDisposition() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getDisposition();
	loadBODYSTRUCTURE();
	return bs.disposition;
    }

    @Override
    public void setDisposition(String disposition) throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get the Content-Transfer-Encoding.
     */
    @Override
    public String getEncoding() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getEncoding();
	loadBODYSTRUCTURE();
	return bs.encoding;
    }

    /**
     * Get the Content-ID.
     */
    @Override
    public String getContentID() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getContentID();
	loadBODYSTRUCTURE();
	return bs.id;
    }

    @Override
    public void setContentID(String cid) throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get the Content-MD5.
     */
    @Override
    public String getContentMD5() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getContentMD5();
	loadBODYSTRUCTURE();
	return bs.md5;
    }

    @Override
    public void setContentMD5(String md5) throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get the decoded Content-Description.
     */
    @Override
    public String getDescription() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getDescription();

	if (description != null) // cached value ?
	    return description;
	
	loadBODYSTRUCTURE();
	if (bs.description == null)
	    return null;
	
	try {
	    description = MimeUtility.decodeText(bs.description);
	} catch (UnsupportedEncodingException ex) {
	    description = bs.description;
	}

	return description;
    }

    @Override
    public void setDescription(String description, String charset) 
			throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get the Message-ID.
     */
    @Override
    public String getMessageID() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getMessageID();
	loadEnvelope();
	return envelope.messageId;
    }

    /**
     * Get the "filename" Disposition parameter. (Only available in
     * IMAP4rev1). If thats not available, get the "name" ContentType
     * parameter.
     */
    @Override
    public String getFileName() throws MessagingException {
	checkExpunged();
	if (bodyLoaded)
	    return super.getFileName();

	String filename = null;
	loadBODYSTRUCTURE();

	if (bs.dParams != null)
	    filename = bs.dParams.get("filename");
	if (filename == null && bs.cParams != null)
	    filename = bs.cParams.get("name");
	return filename;
    }

    @Override
    public void setFileName(String filename) throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get all the bytes for this message. Overrides getContentStream()
     * in MimeMessage. This method is ultimately used by the DataHandler
     * to obtain the input stream for this message.
     *
     * @see javax.mail.internet.MimeMessage#getContentStream
     */
    @Override
    protected InputStream getContentStream() throws MessagingException {
	if (bodyLoaded)
	    return super.getContentStream();
	InputStream is = null;
	boolean pk = getPeek();	// get before acquiring message cache lock

        // Acquire MessageCacheLock, to freeze seqnum.
        synchronized(getMessageCacheLock()) {
	    try {
		IMAPProtocol p = getProtocol();

		// This message could be expunged when we were waiting
		// to acquire the lock ...
		checkExpunged();

		if (p.isREV1() && (getFetchBlockSize() != -1)) // IMAP4rev1
		    return new IMAPInputStream(this, toSection("TEXT"),
				    bs != null && !ignoreBodyStructureSize() ?
					bs.size : -1, pk);

		if (p.isREV1()) {
		    BODY b;
		    if (pk)
			b = p.peekBody(getSequenceNumber(), toSection("TEXT"));
		    else
			b = p.fetchBody(getSequenceNumber(), toSection("TEXT"));
		    if (b != null)
			is = b.getByteArrayInputStream();
		} else {
		    RFC822DATA rd = p.fetchRFC822(getSequenceNumber(), "TEXT");
		    if (rd != null)
			is = rd.getByteArrayInputStream();
		}
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		forceCheckExpunged();
		throw new MessagingException(pex.getMessage(), pex);
	    }
	}

	if (is == null) {
	    forceCheckExpunged();	// may throw MessageRemovedException
	    // nope, the server doesn't think it's expunged.
	    // can't tell the difference between the server returning NIL
	    // and some other error that caused null to be returned above,
	    // so we'll just assume it was empty content.
	    is = new ByteArrayInputStream(new byte[0]);
	}
	return is;
    }

    /**
     * Get the DataHandler object for this message.
     */
    @Override
    public synchronized DataHandler getDataHandler()
		throws MessagingException {
	checkExpunged();

	if (dh == null && !bodyLoaded) {
	    loadBODYSTRUCTURE();
	    if (type == null) { // type not yet computed
		// generate content-type from BODYSTRUCTURE
		ContentType ct = new ContentType(bs.type, bs.subtype,
						 bs.cParams);
		type = ct.toString();
	    }

	    /* Special-case Multipart and Nested content. All other
	     * cases are handled by the superclass.
	     */
	    if (bs.isMulti())
		dh = new DataHandler(
			new IMAPMultipartDataSource(this, bs.bodies, 
						    sectionId, this)
		     );
	    else if (bs.isNested() && isREV1() && bs.envelope != null)
		/* Nested messages are handled specially only for
		 * IMAP4rev1. IMAP4 doesn't provide enough support to 
		 * FETCH the components of nested messages
		 */
		dh = new DataHandler(
			    new IMAPNestedMessage(this, 
				bs.bodies[0], 
				bs.envelope,
				sectionId == null ? "1" : sectionId + ".1"),
			    type
		     );
	}

	return super.getDataHandler();
    }

    @Override
    public void setDataHandler(DataHandler content) 
			throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Return the MIME format stream corresponding to this message.
     *
     * @return	the MIME format stream
     * @since	JavaMail 1.4.5
     */
    @Override
    public InputStream getMimeStream() throws MessagingException {
	// XXX - need an "if (bodyLoaded)" version
	InputStream is = null;
	boolean pk = getPeek();	// get before acquiring message cache lock

        // Acquire MessageCacheLock, to freeze seqnum.
        synchronized(getMessageCacheLock()) {
	    try {
		IMAPProtocol p = getProtocol();

		checkExpunged(); // insure this message is not expunged

		if (p.isREV1() && (getFetchBlockSize() != -1)) // IMAP4rev1
		    return new IMAPInputStream(this, sectionId, -1, pk);

		if (p.isREV1()) {
		    BODY b;
		    if (pk)
			b = p.peekBody(getSequenceNumber(), sectionId);
		    else
			b = p.fetchBody(getSequenceNumber(), sectionId);
		    if (b != null)
			is = b.getByteArrayInputStream();
		} else {
		    RFC822DATA rd = p.fetchRFC822(getSequenceNumber(), null);
		    if (rd != null)
			is = rd.getByteArrayInputStream();
		}
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		forceCheckExpunged();
		throw new MessagingException(pex.getMessage(), pex);
	    }
	}

	if (is == null) {
	    forceCheckExpunged();	// may throw MessageRemovedException
	    // nope, the server doesn't think it's expunged.
	    // can't tell the difference between the server returning NIL
	    // and some other error that caused null to be returned above,
	    // so we'll just assume it was empty content.
	    is = new ByteArrayInputStream(new byte[0]);
	}
	return is;
    }

    /**
     * Write out the bytes into the given OutputStream.
     */
    @Override
    public void writeTo(OutputStream os)
				throws IOException, MessagingException {
	if (bodyLoaded) {
	    super.writeTo(os);
	    return;
	}
	InputStream is = getMimeStream();
	try {
	    // write out the bytes
	    byte[] bytes = new byte[16*1024];
	    int count;
	    while ((count = is.read(bytes)) != -1)
		os.write(bytes, 0, count);
	} finally {
	    is.close();
	}
    }

    /**
     * Get the named header.
     */
    @Override
    public String[] getHeader(String name) throws MessagingException {
	checkExpunged();

	if (isHeaderLoaded(name)) // already loaded ?
	    return headers.getHeader(name);

	// Load this particular header
	InputStream is = null;

        // Acquire MessageCacheLock, to freeze seqnum.
        synchronized(getMessageCacheLock()) {
	    try {
		IMAPProtocol p = getProtocol();

		// This message could be expunged when we were waiting
		// to acquire the lock ...
		checkExpunged();

		if (p.isREV1()) {
		    BODY b = p.peekBody(getSequenceNumber(), 
				toSection("HEADER.FIELDS (" + name + ")")
			     );
		    if (b != null)
			is = b.getByteArrayInputStream();
		} else {
		    RFC822DATA rd = p.fetchRFC822(getSequenceNumber(), 
					"HEADER.LINES (" + name + ")");
		    if (rd != null)
			is = rd.getByteArrayInputStream();
		}
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		forceCheckExpunged();
		throw new MessagingException(pex.getMessage(), pex);
	    }
	}

	// if we get this far without "is" being set, something has gone
	// wrong; prevent a later NullPointerException and return null here
	if (is == null)
	    return null;

	if (headers == null)
	    headers = new InternetHeaders();
	headers.load(is); // load this header into the Headers object.
	setHeaderLoaded(name); // Mark this header as loaded

	return headers.getHeader(name);
    }

    /**
     * Get the named header.
     */
    @Override
    public String getHeader(String name, String delimiter)
			throws MessagingException {
	checkExpunged();

	// force the header to be loaded by invoking getHeader(name)
	if (getHeader(name) == null)
	    return null;
	return headers.getHeader(name, delimiter);
    }

    @Override
    public void setHeader(String name, String value)
			throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    @Override
    public void addHeader(String name, String value)
			throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }
	    
    @Override
    public void removeHeader(String name)
			throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get all headers.
     */
    @Override
    public Enumeration<Header> getAllHeaders() throws MessagingException {
	checkExpunged();
	loadHeaders();
	return super.getAllHeaders();
    }

    /**
     * Get matching headers.
     */
    @Override
    public Enumeration<Header> getMatchingHeaders(String[] names)
			throws MessagingException {
	checkExpunged();
	loadHeaders();
	return super.getMatchingHeaders(names);
    }

    /**
     * Get non-matching headers.
     */
    @Override
    public Enumeration<Header> getNonMatchingHeaders(String[] names)
			throws MessagingException {
	checkExpunged();
	loadHeaders();
	return super.getNonMatchingHeaders(names);
    }

    @Override
    public void addHeaderLine(String line) throws MessagingException {
	throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /**
     * Get all header-lines.
     */
    @Override
    public Enumeration<String> getAllHeaderLines() throws MessagingException {
	checkExpunged();
	loadHeaders();
	return super.getAllHeaderLines();
    }

    /**
     * Get all matching header-lines.
     */
    @Override
    public Enumeration<String> getMatchingHeaderLines(String[] names)
			throws MessagingException {
	checkExpunged();
	loadHeaders();
	return super.getMatchingHeaderLines(names);
    }

    /**
     * Get all non-matching headerlines.
     */
    @Override
    public Enumeration<String> getNonMatchingHeaderLines(String[] names)
			throws MessagingException {
	checkExpunged();
	loadHeaders();
	return super.getNonMatchingHeaderLines(names);
    }

    /**
     * Get the Flags for this message.
     */
    @Override
    public synchronized Flags getFlags() throws MessagingException {
	checkExpunged();
	loadFlags();
	return super.getFlags();
    }

    /**
     * Test if the given Flags are set in this message.
     */
    @Override
    public synchronized boolean isSet(Flags.Flag flag)
				throws MessagingException {
	checkExpunged();
	loadFlags();
	return super.isSet(flag);
    }

    /**
     * Set/Unset the given flags in this message.
     */
    @Override
    public synchronized void setFlags(Flags flag, boolean set)
			throws MessagingException {
        // Acquire MessageCacheLock, to freeze seqnum.
        synchronized(getMessageCacheLock()) {
	    try {
		IMAPProtocol p = getProtocol();
		checkExpunged(); // Insure that this message is not expunged
		p.storeFlags(getSequenceNumber(), flag, set);
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		throw new MessagingException(pex.getMessage(), pex);
	    }
	}
    }

    /**
     * Set whether or not to use the PEEK variant of FETCH when
     * fetching message content.  This overrides the default
     * value from the "mail.imap.peek" property.
     *
     * @param	peek	the peek flag
     * @since	JavaMail 1.3.3
     */
    public synchronized void setPeek(boolean peek) {
	this.peek = Boolean.valueOf(peek);
    }

    /**
     * Get whether or not to use the PEEK variant of FETCH when
     * fetching message content.
     *
     * @return	the peek flag
     * @since	JavaMail 1.3.3
     */
    public synchronized boolean getPeek() {
	if (peek == null)
	    return ((IMAPStore)folder.getStore()).getPeek();
	else
	    return peek.booleanValue();
    }

    /**
     * Invalidate cached header and envelope information for this
     * message.  Subsequent accesses of this information will
     * cause it to be fetched from the server.
     *
     * @since	JavaMail 1.3.3
     */
    public synchronized void invalidateHeaders() {
	headersLoaded = false;
	loadedHeaders.clear();
	headers = null;
	envelope = null;
	bs = null;
	receivedDate = null;
	size = -1;
	type = null;
	subject = null;
	description = null;
	flags = null;
	content = null;
	contentStream = null;
	bodyLoaded = false;
    }

    /**
     * This class implements the test to be done on each
     * message in the folder. The test is to check whether the
     * message has already cached all the items requested in the
     * FetchProfile. If any item is missing, the test succeeds and
     * breaks out.
     */
    public static class FetchProfileCondition implements Utility.Condition {
	private boolean needEnvelope = false;
	private boolean needFlags = false;
	private boolean needBodyStructure = false;
	private boolean needUID = false;
	private boolean needHeaders = false;
	private boolean needSize = false;
	private boolean needMessage = false;
	private boolean needRDate = false;
	private String[] hdrs = null;
	private Set<FetchItem> need = new HashSet<>();

	/**
	 * Create a FetchProfileCondition to determine if we need to fetch
	 * any of the information specified in the FetchProfile.
	 *
	 * @param	fp	the FetchProfile
	 * @param	fitems	the FETCH items
	 */
	@SuppressWarnings("deprecation")	// for FetchProfile.Item.SIZE
	public FetchProfileCondition(FetchProfile fp, FetchItem[] fitems) {
	    if (fp.contains(FetchProfile.Item.ENVELOPE))
		needEnvelope = true;
	    if (fp.contains(FetchProfile.Item.FLAGS))
		needFlags = true;
	    if (fp.contains(FetchProfile.Item.CONTENT_INFO))
		needBodyStructure = true;
	    if (fp.contains(FetchProfile.Item.SIZE))
		needSize = true;
	    if (fp.contains(UIDFolder.FetchProfileItem.UID))
		needUID = true;
	    if (fp.contains(IMAPFolder.FetchProfileItem.HEADERS))
		needHeaders = true;
	    if (fp.contains(IMAPFolder.FetchProfileItem.SIZE))
		needSize = true;
	    if (fp.contains(IMAPFolder.FetchProfileItem.MESSAGE))
		needMessage = true;
	    if (fp.contains(IMAPFolder.FetchProfileItem.INTERNALDATE))
		needRDate = true;
	    hdrs = fp.getHeaderNames();
	    for (int i = 0; i < fitems.length; i++) {
		if (fp.contains(fitems[i].getFetchProfileItem()))
		    need.add(fitems[i]);
	    }
	}

	/**
	 * Return true if we NEED to fetch the requested information
	 * for the specified message.
	 */
	@Override
	public boolean test(IMAPMessage m) {
	    if (needEnvelope && m._getEnvelope() == null && !m.bodyLoaded)
		return true; // no envelope
	    if (needFlags && m._getFlags() == null)
		return true; // no flags
	    if (needBodyStructure && m._getBodyStructure() == null &&
								!m.bodyLoaded)
		return true; // no BODYSTRUCTURE
	    if (needUID && m.getUID() == -1)	// no UID
		return true;
	    if (needHeaders && !m.areHeadersLoaded()) // no headers
		return true;
	    if (needSize && m.size == -1 && !m.bodyLoaded) // no size
		return true;
	    if (needMessage && !m.bodyLoaded)		// no message body
		return true;
	    if (needRDate && m.receivedDate == null)	// no received date
		return true;

	    // Is the desired header present ?
	    for (int i = 0; i < hdrs.length; i++) {
		if (!m.isHeaderLoaded(hdrs[i]))
		    return true; // Nope, return
	    }
	    Iterator<FetchItem> it = need.iterator();
	    while (it.hasNext()) {
		FetchItem fitem = it.next();
		if (m.items == null || m.items.get(fitem.getName()) == null)
		    return true;
	    }

	    return false;
	}
    }

    /**
     * Apply the data in the FETCH item to this message.
     *
     * ASSERT: Must hold the messageCacheLock.
     *
     * @param	item	the fetch item
     * @param	hdrs	the headers we're asking for
     * @param	allHeaders load all headers?
     * @return		did we handle this fetch item?
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.6
     */
    protected boolean handleFetchItem(Item item,
				String[] hdrs, boolean allHeaders)
				throws MessagingException {
	// Check for the FLAGS item
	if (item instanceof Flags)
	    flags = (Flags)item;
	// Check for ENVELOPE items
	else if (item instanceof ENVELOPE)
	    envelope = (ENVELOPE)item;
	else if (item instanceof INTERNALDATE)
	    receivedDate = ((INTERNALDATE)item).getDate();
	else if (item instanceof RFC822SIZE)
	    size = ((RFC822SIZE)item).size;
	else if (item instanceof MODSEQ)
	    modseq = ((MODSEQ)item).modseq;

	// Check for the BODYSTRUCTURE item
	else if (item instanceof BODYSTRUCTURE)
	    bs = (BODYSTRUCTURE)item;
	// Check for the UID item
	else if (item instanceof UID) {
	    UID u = (UID)item;
	    uid = u.uid; // set uid
	    // add entry into uid table
	    if (((IMAPFolder)folder).uidTable == null)
		((IMAPFolder) folder).uidTable
			= new Hashtable<>();
	    ((IMAPFolder)folder).uidTable.put(Long.valueOf(u.uid), this);
	}

	// Check for header items
	else if (item instanceof RFC822DATA ||
		 item instanceof BODY) {
	    InputStream headerStream;
	    boolean isHeader;
	    if (item instanceof RFC822DATA) { // IMAP4
		headerStream = 
		    ((RFC822DATA)item).getByteArrayInputStream();
		isHeader = ((RFC822DATA)item).isHeader();
	    } else {	// IMAP4rev1
		headerStream = 
		    ((BODY)item).getByteArrayInputStream();
		isHeader = ((BODY)item).isHeader();
	    }

	    if (!isHeader) {
		// load the entire message by using the superclass
		// MimeMessage.parse method
		// first, save the size of the message
		try {
		    size = headerStream.available();
		} catch (IOException ex) {
		    // should never occur
		}
		parse(headerStream);
		bodyLoaded = true;
		setHeadersLoaded(true);
	    } else {
		// Load the obtained headers.
		InternetHeaders h = new InternetHeaders();
		// Some IMAP servers (e.g., gmx.net) return NIL 
		// instead of a string just containing a CR/LF
		// when the header list is empty.
		if (headerStream != null)
		    h.load(headerStream);
		if (headers == null || allHeaders)
		    headers = h;
		else {
		    /*
		     * This is really painful.  A second fetch
		     * of the same headers (which might occur because
		     * a new header was added to the set requested)
		     * will return headers we already know about.
		     * In this case, only load the headers we haven't
		     * seen before to avoid adding duplicates of
		     * headers we already have.
		     *
		     * XXX - There's a race condition here if another
		     * thread is reading headers in the same message
		     * object, because InternetHeaders is not thread
		     * safe.
		     */
		    Enumeration<Header> e = h.getAllHeaders();
		    while (e.hasMoreElements()) {
			Header he = e.nextElement();
			if (!isHeaderLoaded(he.getName()))
			    headers.addHeader(
					he.getName(), he.getValue());
		    }
		}

		// if we asked for all headers, assume we got them
		if (allHeaders)
		    setHeadersLoaded(true);
		else {
		    // Mark all headers we asked for as 'loaded'
		    for (int k = 0; k < hdrs.length; k++)
			setHeaderLoaded(hdrs[k]);
		}
	    }
	} else
	    return false;	// not handled
	return true;		// something above handled it
    }

    /**
     * Apply the data in the extension FETCH items to this message.
     * This method adds all the items to the items map.
     * Subclasses may override this method to call super and then
     * also copy the data to a more convenient form.
     *
     * ASSERT: Must hold the messageCacheLock.
     *
     * @param	extensionItems	the Map to add fetch items to
     * @since JavaMail 1.4.6
     */
    protected void handleExtensionFetchItems(
	    Map<String, Object> extensionItems) {
	if (extensionItems == null || extensionItems.isEmpty())
	    return;
	if (items == null)
	    items = new HashMap<>();
	items.putAll(extensionItems);
    }

    /**
     * Fetch an individual item for the current message.
     * Note that handleExtensionFetchItems will have been called
     * to store this item in the message before this method
     * returns.
     *
     * @param	fitem	the FetchItem
     * @return		the data associated with the FetchItem
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.6
     */
    protected Object fetchItem(FetchItem fitem)
				throws MessagingException {

	// Acquire MessageCacheLock, to freeze seqnum.
	synchronized(getMessageCacheLock()) {
	    Object robj = null;

	    try {
		IMAPProtocol p = getProtocol();

		checkExpunged(); // Insure that this message is not expunged

		int seqnum = getSequenceNumber();
		Response[] r = p.fetch(seqnum, fitem.getName());

		for (int i = 0; i < r.length; i++) {
		    // If this response is NOT a FetchResponse or if it does
		    // not match our seqnum, skip.
		    if (r[i] == null ||
			!(r[i] instanceof FetchResponse) ||
			((FetchResponse)r[i]).getNumber() != seqnum)
			continue;

		    FetchResponse f = (FetchResponse)r[i];
		    handleExtensionFetchItems(f.getExtensionItems());
		    if (items != null) {
			Object o = items.get(fitem.getName());
			if (o != null)
			    robj = o;
		    }
		}

		// ((IMAPFolder)folder).handleResponses(r);
		p.notifyResponseHandlers(r);
		p.handleResult(r[r.length - 1]);
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		forceCheckExpunged();
		throw new MessagingException(pex.getMessage(), pex);
	    }
	    return robj;

	} // Release MessageCacheLock
    }

    /**
     * Return the data associated with the FetchItem.
     * If the data hasn't been fetched, call the fetchItem
     * method to fetch it.  Returns null if there is no
     * data for the FetchItem.
     *
     * @param	fitem	the FetchItem
     * @return		the data associated with the FetchItem
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.6
     */
    public synchronized Object getItem(FetchItem fitem)
				throws MessagingException {
	Object item = items == null ? null : items.get(fitem.getName());
	if (item == null)
	    item = fetchItem(fitem);
	return item;
    }

    /*
     * Load the Envelope for this message.
     */
    private synchronized void loadEnvelope() throws MessagingException {
	if (envelope != null) // already loaded
	    return;

	Response[] r = null;

	// Acquire MessageCacheLock, to freeze seqnum.
	synchronized(getMessageCacheLock()) {
	    try {
		IMAPProtocol p = getProtocol();

		checkExpunged(); // Insure that this message is not expunged

		int seqnum = getSequenceNumber();
		r = p.fetch(seqnum, EnvelopeCmd);

		for (int i = 0; i < r.length; i++) {
		    // If this response is NOT a FetchResponse or if it does
		    // not match our seqnum, skip.
		    if (r[i] == null ||
			!(r[i] instanceof FetchResponse) ||
			((FetchResponse)r[i]).getNumber() != seqnum)
			continue;

		    FetchResponse f = (FetchResponse)r[i];
		    
		    // Look for the Envelope items.
		    int count = f.getItemCount();
		    for (int j = 0; j < count; j++) {
			Item item = f.getItem(j);
			
			if (item instanceof ENVELOPE)
			    envelope = (ENVELOPE)item;
			else if (item instanceof INTERNALDATE)
			    receivedDate = ((INTERNALDATE)item).getDate();
			else if (item instanceof RFC822SIZE)
			    size = ((RFC822SIZE)item).size;
		    }
		}

		// ((IMAPFolder)folder).handleResponses(r);
		p.notifyResponseHandlers(r);
		p.handleResult(r[r.length - 1]);
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		forceCheckExpunged();
		throw new MessagingException(pex.getMessage(), pex);
	    }

	} // Release MessageCacheLock

	if (envelope == null)
	    throw new MessagingException("Failed to load IMAP envelope");
    }

    /*
     * Load the BODYSTRUCTURE
     */
    private synchronized void loadBODYSTRUCTURE() 
		throws MessagingException {
	if (bs != null) // already loaded
	    return;

	// Acquire MessageCacheLock, to freeze seqnum.
	synchronized(getMessageCacheLock()) {
	    try {
		IMAPProtocol p = getProtocol();

		// This message could be expunged when we were waiting 
		// to acquire the lock ...
		checkExpunged();

		bs = p.fetchBodyStructure(getSequenceNumber());
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		forceCheckExpunged();
		throw new MessagingException(pex.getMessage(), pex);
	    }
	    if (bs == null) {
		// if the FETCH is successful, we should always get a
		// BODYSTRUCTURE, but some servers fail to return it
		// if the message has been expunged
		forceCheckExpunged();
		throw new MessagingException("Unable to load BODYSTRUCTURE");
	    }
	}
    }

    /*
     * Load all headers.
     */
    private synchronized void loadHeaders() throws MessagingException {
	if (headersLoaded)
	    return;

	InputStream is = null;

	// Acquire MessageCacheLock, to freeze seqnum.
	synchronized (getMessageCacheLock()) {
	    try {
		IMAPProtocol p = getProtocol();

		// This message could be expunged when we were waiting 
		// to acquire the lock ...
		checkExpunged();

		if (p.isREV1()) {
		    BODY b = p.peekBody(getSequenceNumber(), 
					 toSection("HEADER"));
		    if (b != null)
			is = b.getByteArrayInputStream();
		} else {
		    RFC822DATA rd = p.fetchRFC822(getSequenceNumber(), 
						  "HEADER");
		    if (rd != null)
			is = rd.getByteArrayInputStream();
		}
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		forceCheckExpunged();
		throw new MessagingException(pex.getMessage(), pex);
	    }
	} // Release MessageCacheLock

	if (is == null)
	    throw new MessagingException("Cannot load header");
	headers = new InternetHeaders(is);
	headersLoaded = true;
    }

    /*
     * Load this message's Flags
     */
    private synchronized void loadFlags() throws MessagingException {
	if (flags != null)
	    return;
	
	// Acquire MessageCacheLock, to freeze seqnum.
	synchronized(getMessageCacheLock()) {
	    try {
		IMAPProtocol p = getProtocol();

		// This message could be expunged when we were waiting 
		// to acquire the lock ...
		checkExpunged();

		flags = p.fetchFlags(getSequenceNumber());
		// make sure flags is always set, even if server is broken
		if (flags == null)
		    flags = new Flags();
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		forceCheckExpunged();
		throw new MessagingException(pex.getMessage(), pex);
	    }
	} // Release MessageCacheLock
    }

    /*
     * Are all headers loaded?
     */
    private boolean areHeadersLoaded() {
	return headersLoaded;
    }

    /*
     * Set whether all headers are loaded.
     */
    private void setHeadersLoaded(boolean loaded) {
	headersLoaded = loaded;
    }

    /* 
     * Check if the given header was ever loaded from the server
     */
    private boolean isHeaderLoaded(String name) {
	if (headersLoaded) // All headers for this message have been loaded
	    return true;
	
	return loadedHeaders.containsKey(name.toUpperCase(Locale.ENGLISH));
    }

    /*
     * Mark that the given headers have been loaded from the server.
     */
    private void setHeaderLoaded(String name) {
	loadedHeaders.put(name.toUpperCase(Locale.ENGLISH), name);
    }

    /*
     * Convert the given FETCH item identifier to the approriate 
     * section-string for this message.
     */
    private String toSection(String what) {
	if (sectionId == null)
	    return what;
	else
	    return sectionId + "." + what;
    }

    /*
     * Clone an array of InternetAddresses.
     */
    private InternetAddress[] aaclone(InternetAddress[] aa) {
	if (aa == null)
	    return null;
	else
	    return aa.clone();
    }

    private Flags _getFlags() {
	return flags;
    }

    private ENVELOPE _getEnvelope() {
	return envelope;
    }

    private BODYSTRUCTURE _getBodyStructure() {
	return bs;
    }

    /***********************************************************
     * accessor routines to make available certain private/protected
     * fields to other classes in this package.
     ***********************************************************/

    /*
     * Called by IMAPFolder.
     * Must not be synchronized.
     */
    void _setFlags(Flags flags) {
	this.flags = flags;
    }

    /*
     * Called by IMAPNestedMessage.
     */
    Session _getSession() {
	return session;
    }
}
