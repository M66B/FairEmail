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

package javax.mail;

import java.util.Date;
import java.io.*;
import javax.mail.search.SearchTerm;

/**
 * This class models an email message. This is an abstract class.  
 * Subclasses provide actual implementations. <p>
 *
 * Message implements the Part interface. Message contains a set of
 * attributes and a "content". Messages within a folder also have a 
 * set of flags that describe its state within the folder.<p>
 *
 * Message defines some new attributes in addition to those defined
 * in the <code>Part</code> interface. These attributes specify meta-data
 * for the message - i.e., addressing  and descriptive information about 
 * the message. <p>
 *
 * Message objects are obtained either from a Folder or by constructing
 * a new Message object of the appropriate subclass. Messages that have
 * been received are normally retrieved from a folder named "INBOX". <p>
 *
 * A Message object obtained from a folder is just a lightweight
 * reference to the actual message. The Message is 'lazily' filled 
 * up (on demand) when each item is requested from the message. Note
 * that certain folder implementations may return Message objects that
 * are pre-filled with certain user-specified items.
 
 * To send a message, an appropriate subclass of Message (e.g., 
 * MimeMessage) is instantiated, the attributes and content are 
 * filled in, and the message is sent using the <code>Transport.send</code> 
 * method. <p>
 *
 * @author John Mani
 * @author Bill Shannon
 * @author Max Spivak
 * @see	   javax.mail.Part
 */

public abstract class Message implements Part {

    /**
     * The number of this message within its folder, or zero if
     * the message was not retrieved from a folder.
     */
    protected int msgnum = 0;

    /**
     * True if this message has been expunged.
     */
    protected boolean expunged 	= false;

    /**
     * The containing folder, if this message is obtained from a folder
     */
    protected Folder folder	= null;

    /**
     * The Session object for this Message
     */
    protected Session session	= null;

    /**
     * No-arg version of the constructor.
     */
    protected Message() { }

    /**
     * Constructor that takes a Folder and a message number. 
     * Used by Folder implementations.
     *
     * @param	folder	containing folder
     * @param	msgnum	this message's sequence number within this folder
     */
    protected Message(Folder folder, int msgnum) {
	this.folder = folder;
	this.msgnum = msgnum;
	session = folder.store.session;
    }

    /**
     * Constructor that takes a Session. Used for client created
     * Message objects.
     *
     * @param	session	A Session object
     */
    protected Message(Session session) {
	this.session = session;
    }

    /**
     * Return the Session used when this message was created.
     *
     * @return		the message's Session
     * @since		JavaMail 1.5
     */
    public Session getSession() {
	return session;
    }

    /**
     * Returns the "From" attribute. The "From" attribute contains
     * the identity of the person(s) who wished this message to 
     * be sent. <p>
     * 
     * In certain implementations, this may be different
     * from the entity that actually sent the message. <p>
     *
     * This method returns <code>null</code> if this attribute
     * is not present in this message. Returns an empty array if
     * this attribute is present, but contains no addresses.
     *
     * @return          array of Address objects
     * @exception       MessagingException for failures
     */
    public abstract Address[] getFrom() throws MessagingException;

    /**
     * Set the "From" attribute in this Message. The value of this
     * attribute is obtained from the property "mail.user". If this
     * property is absent, the system property "user.name" is used.
     *
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    public abstract void setFrom() throws MessagingException;

    /**
     * Set the "From" attribute in this Message.
     *
     * @param address   the sender
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    public abstract void setFrom(Address address) 
			throws MessagingException;

    /**
     * Add these addresses to the existing "From" attribute 
     *
     * @param addresses	the senders
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    public abstract void addFrom(Address[] addresses) 
			throws MessagingException;

    /**
     * This inner class defines the types of recipients allowed by
     * the Message class. The currently defined types are TO,
     * CC and BCC.
     *
     * Note that this class only has a protected constructor, thereby
     * restricting new Recipient types to either this class or subclasses.
     * This effectively implements an enumeration of the allowed Recipient
     * types.
     *
     * The following code sample shows how to use this class to obtain
     * the "TO" recipients from a message.
     * <blockquote><pre>
     *
     * Message msg = folder.getMessages(1);
     * Address[] a = m.getRecipients(Message.RecipientType.TO);
     *
     * </pre></blockquote>
     *
     * @see javax.mail.Message#getRecipients
     * @see javax.mail.Message#setRecipients
     * @see javax.mail.Message#addRecipients
     */
    public static class RecipientType implements Serializable {
	/**
	 * The "To" (primary) recipients.
	 */
	public static final RecipientType TO = new RecipientType("To");
	/**
	 * The "Cc" (carbon copy) recipients.
	 */
	public static final RecipientType CC = new RecipientType("Cc");
	/**
	 * The "Bcc" (blind carbon copy) recipients.
	 */
	public static final RecipientType BCC = new RecipientType("Bcc");

	/**
	 * The type of recipient, usually the name of a corresponding
	 * Internet standard header.
	 *
	 * @serial
	 */
	protected String type;

	private static final long serialVersionUID = -7479791750606340008L;

	/**
	 * Constructor for use by subclasses.
	 *
	 * @param	type	the recipient type
	 */
	protected RecipientType(String type) {
	    this.type = type;
	}

	/**
	 * When deserializing a RecipientType, we need to make sure to
	 * return only one of the known static final instances defined
	 * in this class.  Subclasses must implement their own
	 * <code>readResolve</code> method that checks for their known
	 * instances before calling this super method.
	 *
	 * @return	the RecipientType object instance
	 * @exception	ObjectStreamException for object stream errors
	 */
	protected Object readResolve() throws ObjectStreamException {
	    if (type.equals("To"))
		return TO;
	    else if (type.equals("Cc"))
		return CC;
	    else if (type.equals("Bcc"))
		return BCC;
	    else
		throw new InvalidObjectException(
		    "Attempt to resolve unknown RecipientType: " + type);
	}

	@Override
	public String toString() {
	    return type;
	}
    }

    /**
     * Get all the recipient addresses of the given type. <p>
     *
     * This method returns <code>null</code> if no recipients of
     * the given type are present in this message. It may return an 
     * empty array if the header is present, but contains no addresses.
     *
     * @param type      the recipient type
     * @return          array of Address objects
     * @exception       MessagingException for failures
     * @see Message.RecipientType#TO
     * @see Message.RecipientType#CC
     * @see Message.RecipientType#BCC
     */
    public abstract Address[] getRecipients(RecipientType type)
                                throws MessagingException;

    /**
     * Get all the recipient addresses for the message.
     * The default implementation extracts the TO, CC, and BCC
     * recipients using the <code>getRecipients</code> method. <p>
     *
     * This method returns <code>null</code> if none of the recipient
     * headers are present in this message.  It may Return an empty array
     * if any recipient header is present, but contains no addresses.
     *
     * @return          array of Address objects
     * @exception       MessagingException for failures
     * @see Message.RecipientType#TO
     * @see Message.RecipientType#CC
     * @see Message.RecipientType#BCC
     * @see #getRecipients
     */
    public Address[] getAllRecipients() throws MessagingException {
	Address[] to = getRecipients(RecipientType.TO);
	Address[] cc = getRecipients(RecipientType.CC);
	Address[] bcc = getRecipients(RecipientType.BCC);

	if (cc == null && bcc == null)
	    return to;		// a common case

	int numRecip =
	    (to != null ? to.length : 0) +
	    (cc != null ? cc.length : 0) +
	    (bcc != null ? bcc.length : 0);
	Address[] addresses = new Address[numRecip];
	int pos = 0;
	if (to != null) {
	    System.arraycopy(to, 0, addresses, pos, to.length);
	    pos += to.length;
	}
	if (cc != null) {
	    System.arraycopy(cc, 0, addresses, pos, cc.length);
	    pos += cc.length;
	}
	if (bcc != null) {
	    System.arraycopy(bcc, 0, addresses, pos, bcc.length);
	    // pos += bcc.length;
	}
	return addresses;
    }

    /**
     * Set the recipient addresses.  All addresses of the specified
     * type are replaced by the addresses parameter.
     *
     * @param type      the recipient type
     * @param addresses the addresses
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    public abstract void setRecipients(RecipientType type, Address[] addresses)
                                throws MessagingException;

    /**
     * Set the recipient address.  All addresses of the specified
     * type are replaced by the address parameter. <p>
     *
     * The default implementation uses the <code>setRecipients</code> method.
     *
     * @param type      the recipient type
     * @param address	the address
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values
     * @exception       MessagingException for other failures
     */
    public void setRecipient(RecipientType type, Address address)
                                throws MessagingException {
	if (address == null)
	    setRecipients(type, null);
	else {
	    Address[] a = new Address[1];
	    a[0] = address;
	    setRecipients(type, a);
	}
    }

    /**
     * Add these recipient addresses to the existing ones of the given type.
     *
     * @param type      the recipient type
     * @param addresses the addresses
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    public abstract void addRecipients(RecipientType type, Address[] addresses)
                                throws MessagingException;

    /**
     * Add this recipient address to the existing ones of the given type. <p>
     *
     * The default implementation uses the <code>addRecipients</code> method.
     *
     * @param type      the recipient type
     * @param address	the address
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values
     * @exception       MessagingException for other failures
     */
    public void addRecipient(RecipientType type, Address address)
                                throws MessagingException {
	Address[] a = new Address[1];
	a[0] = address;
	addRecipients(type, a);
    }

    /**
     * Get the addresses to which replies should be directed.
     * This will usually be the sender of the message, but
     * some messages may direct replies to a different address. <p>
     *
     * The default implementation simply calls the <code>getFrom</code>
     * method. <p>
     *
     * This method returns <code>null</code> if the corresponding
     * header is not present. Returns an empty array if the header
     * is present, but contains no addresses.
     *
     * @return          addresses to which replies should be directed
     * @exception       MessagingException for failures
     * @see		#getFrom
     */
    public Address[] getReplyTo() throws MessagingException {
        return getFrom();
    }

    /**
     * Set the addresses to which replies should be directed.
     * (Normally only a single address will be specified.)
     * Not all message types allow this to be specified separately
     * from the sender of the message. <p>
     *
     * The default implementation provided here just throws the
     * MethodNotSupportedException.
     *
     * @param addresses addresses to which replies should be directed
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MethodNotSupportedException if the underlying 
     *			implementation does not support setting this
     *			attribute
     * @exception       MessagingException for other failures
     */
    public void setReplyTo(Address[] addresses) throws MessagingException {
	throw new MethodNotSupportedException("setReplyTo not supported");
    }

    /**
     * Get the subject of this message.
     *
     * @return          the subject
     * @exception       MessagingException for failures
     */
    public abstract String getSubject() throws MessagingException;

    /**
     * Set the subject of this message.
     *
     * @param subject   the subject
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    public abstract void setSubject(String subject) 
			throws MessagingException;

    /**
     * Get the date this message was sent.
     *
     * @return          the date this message was sent
     * @exception       MessagingException for failures
     */
    public abstract Date getSentDate() throws MessagingException;

    /**
     * Set the sent date of this message.
     *
     * @param date      the sent date of this message
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     */
    public abstract void setSentDate(Date date) throws MessagingException;

    /**
     * Get the date this message was received.
     *
     * @return          the date this message was received
     * @exception       MessagingException for failures
     */
    public abstract Date getReceivedDate() throws MessagingException;

    /**
     * Returns a <code>Flags</code> object containing the flags for 
     * this message. <p>
     *
     * Modifying any of the flags in this returned Flags object will 
     * not affect the flags of this message. Use <code>setFlags()</code>
     * to do that. <p>
     *
     * @return		Flags object containing the flags for this message
     * @see 		javax.mail.Flags
     * @see 		#setFlags
     * @exception       MessagingException for failures
     */
    public abstract Flags getFlags() throws MessagingException;

    /**
     * Check whether the flag specified in the <code>flag</code>
     * argument is set in this message. <p>
     *
     * The default implementation uses <code>getFlags</code>.
     *
     * @param flag	the flag
     * @return		value of the specified flag for this message
     * @see 		javax.mail.Flags.Flag
     * @see		javax.mail.Flags.Flag#ANSWERED
     * @see		javax.mail.Flags.Flag#DELETED
     * @see		javax.mail.Flags.Flag#DRAFT
     * @see		javax.mail.Flags.Flag#FLAGGED
     * @see		javax.mail.Flags.Flag#RECENT
     * @see		javax.mail.Flags.Flag#SEEN
     * @exception       MessagingException for failures
     */
    public boolean isSet(Flags.Flag flag) throws MessagingException {
	return getFlags().contains(flag);
    }

    /**
     * Set the specified flags on this message to the specified value.
     * Note that any flags in this message that are not specified in
     * the given <code>Flags</code> object are unaffected. <p>
     *
     * This will result in a <code>MessageChangedEvent</code> being 
     * delivered to any MessageChangedListener registered on this 
     * Message's containing folder.
     *
     * @param flag	Flags object containing the flags to be set
     * @param set	the value to be set
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values.
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     * @see		javax.mail.event.MessageChangedEvent
     */
    public abstract void setFlags(Flags flag, boolean set)
				throws MessagingException;

    /**
     * Set the specified flag on this message to the specified value.
     *
     * This will result in a <code>MessageChangedEvent</code> being 
     * delivered to any MessageChangedListener registered on this 
     * Message's containing folder. <p>
     *
     * The default implementation uses the <code>setFlags</code> method.
     *
     * @param flag	Flags.Flag object containing the flag to be set
     * @param set	the value to be set
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values.
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception       MessagingException for other failures
     * @see		javax.mail.event.MessageChangedEvent
     */
    public void setFlag(Flags.Flag flag, boolean set)
				throws MessagingException {
	Flags f = new Flags(flag);
	setFlags(f, set);
    }

    /**
     * Get the Message number for this Message.
     * A Message object's message number is the relative
     * position of this Message in its Folder. Note that the message
     * number for a particular Message can change during a session
     * if other messages in the Folder are deleted and expunged. <p>
     *
     * Valid message numbers start at 1. Messages that do not belong
     * to any folder (like newly composed or derived messages) have 0
     * as their message number.
     *
     * @return	the message number
     */
    public int getMessageNumber() {
	return msgnum;
    }

    /**
     * Set the Message number for this Message. This method is
     * invoked only by the implementation classes.
     *
     * @param	msgnum	the message number
     */
    protected void setMessageNumber(int msgnum) {
	this.msgnum = msgnum;
    }

    /**
     * Get the folder from which this message was obtained. If
     * this is a new message or nested message, this method returns
     * null.
     *
     * @return	the containing folder
     */
    public Folder getFolder() {
	return folder;
    }

    /**
     * Checks whether this message is expunged. All other methods except
     * <code>getMessageNumber()</code> are invalid on an expunged 
     * Message object. <p>
     *
     * Messages that are expunged due to an explict <code>expunge()</code>
     * request on the containing Folder are removed from the Folder 
     * immediately. Messages that are externally expunged by another source
     * are marked "expunged" and return true for the isExpunged() method, 
     * but they are not removed from the Folder until an explicit 
     * <code>expunge()</code> is done on the Folder. <p>
     * 
     * See the description of <code>expunge()</code> for more details on
     * expunge handling.
     *
     * @return	true if the message is expunged
     * @see	Folder#expunge
     */
    public boolean isExpunged() {
	return expunged;
    }

    /**
     * Sets the expunged flag for this Message. This method is to
     * be used only by the implementation classes.
     *
     * @param expunged	the expunged flag
     */
    protected void setExpunged(boolean expunged) {
	this.expunged = expunged;
    }

    /**
     * Get a new Message suitable for a reply to this message.
     * The new Message will have its attributes and headers 
     * set up appropriately.  Note that this new message object
     * will be empty, that is, it will <strong>not</strong> have a "content".
     * These will have to be suitably filled in by the client. <p>
     *
     * If <code>replyToAll</code> is set, the new Message will be addressed
     * to all recipients of this message.  Otherwise, the reply will be
     * addressed to only the sender of this message (using the value
     * of the <code>getReplyTo</code> method).  <p>
     *
     * The "Subject" field is filled in with the original subject
     * prefixed with "Re:" (unless it already starts with "Re:"). <p>
     *
     * The reply message will use the same session as this message.
     *
     * @param	replyToAll	reply should be sent to all recipients
     *				of this message
     * @return		the reply Message
     * @exception	MessagingException for failures
     */
    public abstract Message reply(boolean replyToAll) throws MessagingException;

    /**
     * Save any changes made to this message into the message-store
     * when the containing folder is closed, if the message is contained
     * in a folder.  (Some implementations may save the changes
     * immediately.)  Update any header fields to be consistent with the
     * changed message contents.  If any part of a message's headers or
     * contents are changed, saveChanges must be called to ensure that
     * those changes are permanent.  If saveChanges is not called, any
     * such modifications may or may not be saved, depending on the
     * message store and folder implementation. <p>
     *
     * Messages obtained from folders opened READ_ONLY should not be
     * modified and saveChanges should not be called on such messages.
     *
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	IllegalWriteException if the underlying 
     *			implementation does not support modification 
     *			of existing values.
     * @exception       MessagingException for other failures
     */
    public abstract void saveChanges() throws MessagingException;

    /**
     * Apply the specified Search criterion to this message.
     *
     * @param term	the Search criterion
     * @return		true if the Message matches this search
     *			criterion, false otherwise.
     * @exception       MessagingException for failures
     * @see		javax.mail.search.SearchTerm
     */
    public boolean match(SearchTerm term) throws MessagingException {
	return term.match(this);
    }
}
