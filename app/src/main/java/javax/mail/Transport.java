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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.mail.event.*;

/**
 * An abstract class that models a message transport.
 * Subclasses provide actual implementations. <p>
 *
 * Note that <code>Transport</code> extends the <code>Service</code>
 * class, which provides many common methods for naming transports,
 * connecting to transports, and listening to connection events.
 *
 * @author John Mani
 * @author Max Spivak
 * @author Bill Shannon
 * 
 * @see javax.mail.Service
 * @see javax.mail.event.ConnectionEvent
 * @see javax.mail.event.TransportEvent
 */

public abstract class Transport extends Service {

    /**
     * Constructor.
     *
     * @param	session Session object for this Transport.
     * @param	urlname	URLName object to be used for this Transport
     */
    public Transport(Session session, URLName urlname) {
	super(session, urlname);
    }

    /**
     * Send a message.  The message will be sent to all recipient
     * addresses specified in the message (as returned from the
     * <code>Message</code> method <code>getAllRecipients</code>),
     * using message transports appropriate to each address.  The
     * <code>send</code> method calls the <code>saveChanges</code>
     * method on the message before sending it. <p>
     *
     * If any of the recipient addresses is detected to be invalid by
     * the Transport during message submission, a SendFailedException
     * is thrown.  Clients can get more detail about the failure by examining
     * the exception.  Whether or not the message is still sent successfully
     * to any valid addresses depends on the Transport implementation.  See 
     * SendFailedException for more details.  Note also that success does 
     * not imply that the message was delivered to the ultimate recipient,
     * as failures may occur in later stages of delivery.  Once a Transport 
     * accepts a message for delivery to a recipient, failures that occur later
     * should be reported to the user via another mechanism, such as
     * returning the undeliverable message. <p>
     *
     * In typical usage, a SendFailedException reflects an error detected
     * by the server.  The details of the SendFailedException will usually
     * contain the error message from the server (such as an SMTP error
     * message).  An address may be detected as invalid for a variety of
     * reasons - the address may not exist, the address may have invalid
     * syntax, the address may have exceeded its quota, etc. <p>
     *
     * Note that <code>send</code> is a static method that creates and
     * manages its own connection.  Any connection associated with any
     * Transport instance used to invoke this method is ignored and not
     * used.  This method should only be invoked using the form
     * <code>Transport.send(msg);</code>, and should never be invoked
     * using an instance variable.
     *
     * @param	msg	the message to send
     * @exception	SendFailedException if the message could not
     *			be sent to some or any of the recipients.
     * @exception	MessagingException for other failures
     * @see		Message#saveChanges
     * @see		Message#getAllRecipients
     * @see		#send(Message, Address[])
     * @see		javax.mail.SendFailedException
     */
    public static void send(Message msg) throws MessagingException {
	msg.saveChanges(); // do this first
	send0(msg, msg.getAllRecipients(), null, null);
    }

    /**
     * Send the message to the specified addresses, ignoring any
     * recipients specified in the message itself. The
     * <code>send</code> method calls the <code>saveChanges</code>
     * method on the message before sending it. <p>
     *
     * @param	msg	the message to send
     * @param	addresses the addresses to which to send the message
     * @exception	SendFailedException if the message could not
     *			be sent to some or any of the recipients.
     * @exception	MessagingException for other failures
     * @see		Message#saveChanges
     * @see             #send(Message)
     * @see		javax.mail.SendFailedException
     */
    public static void send(Message msg, Address[] addresses) 
		throws MessagingException {

	msg.saveChanges();
	send0(msg, addresses, null, null);
    }

    /**
     * Send a message.  The message will be sent to all recipient
     * addresses specified in the message (as returned from the
     * <code>Message</code> method <code>getAllRecipients</code>).
     * The <code>send</code> method calls the <code>saveChanges</code>
     * method on the message before sending it. <p>
     *
     * Use the specified user name and password to authenticate to
     * the mail server.
     *
     * @param	msg	the message to send
     * @param	user	the user name
     * @param	password this user's password
     * @exception	SendFailedException if the message could not
     *			be sent to some or any of the recipients.
     * @exception	MessagingException for other failures
     * @see		Message#saveChanges
     * @see             #send(Message)
     * @see		javax.mail.SendFailedException
     * @since		JavaMail 1.5
     */
    public static void send(Message msg,
		String user, String password) throws MessagingException {

	msg.saveChanges();
	send0(msg, msg.getAllRecipients(), user, password);
    }

    /**
     * Send the message to the specified addresses, ignoring any
     * recipients specified in the message itself. The
     * <code>send</code> method calls the <code>saveChanges</code>
     * method on the message before sending it. <p>
     *
     * Use the specified user name and password to authenticate to
     * the mail server.
     *
     * @param	msg	the message to send
     * @param	addresses the addresses to which to send the message
     * @param	user	the user name
     * @param	password this user's password
     * @exception	SendFailedException if the message could not
     *			be sent to some or any of the recipients.
     * @exception	MessagingException for other failures
     * @see		Message#saveChanges
     * @see             #send(Message)
     * @see		javax.mail.SendFailedException
     * @since		JavaMail 1.5
     */
    public static void send(Message msg, Address[] addresses,
		String user, String password) throws MessagingException {

	msg.saveChanges();
	send0(msg, addresses, user, password);
    }

    // send, but without the saveChanges
    private static void send0(Message msg, Address[] addresses,
		String user, String password) throws MessagingException {

	if (addresses == null || addresses.length == 0)
	    throw new SendFailedException("No recipient addresses");

	/*
	 * protocols is a map containing the addresses
	 * indexed by address type
	 */
	Map<String, List<Address>> protocols
		= new HashMap<>();

	// Lists of addresses
	List<Address> invalid = new ArrayList<>();
	List<Address> validSent = new ArrayList<>();
	List<Address> validUnsent = new ArrayList<>();

	for (int i = 0; i < addresses.length; i++) {
	    // is this address type already in the map?
	    if (protocols.containsKey(addresses[i].getType())) {
		List<Address> v = protocols.get(addresses[i].getType());
		v.add(addresses[i]);
	    } else {
		// need to add a new protocol
		List<Address> w = new ArrayList<>();
		w.add(addresses[i]);
		protocols.put(addresses[i].getType(), w);
	    }
	}

	int dsize = protocols.size();
	if (dsize == 0)
	    throw new SendFailedException("No recipient addresses");

	Session s = (msg.session != null) ? msg.session :
		     Session.getDefaultInstance(System.getProperties(), null);
	Transport transport;

	/*
	 * Optimize the case of a single protocol.
	 */
	if (dsize == 1) {
	    transport = s.getTransport(addresses[0]);
	    try {
		if (user != null)
		    transport.connect(user, password);
		else
		    transport.connect();
		transport.sendMessage(msg, addresses);
	    } finally {
		transport.close();
	    }
	    return;
	}

	/*
	 * More than one protocol.  Have to do them one at a time
	 * and collect addresses and chain exceptions.
	 */
	MessagingException chainedEx = null;
	boolean sendFailed = false;

	for(List<Address> v : protocols.values()) {
	    Address[] protaddresses = new Address[v.size()];
	    v.toArray(protaddresses);

	    // Get a Transport that can handle this address type.
	    if ((transport = s.getTransport(protaddresses[0])) == null) {
		// Could not find an appropriate Transport ..
		// Mark these addresses invalid.
		for (int j = 0; j < protaddresses.length; j++)
		    invalid.add(protaddresses[j]);
		continue;
	    }
	    try {
		transport.connect();
		transport.sendMessage(msg, protaddresses);
	    } catch (SendFailedException sex) {
		sendFailed = true;
		// chain the exception we're catching to any previous ones
		if (chainedEx == null)
		    chainedEx = sex;
		else
		    chainedEx.setNextException(sex);

		// retrieve invalid addresses
		Address[] a = sex.getInvalidAddresses();
		if (a != null)
		    for (int j = 0; j < a.length; j++) 
			invalid.add(a[j]);

		// retrieve validSent addresses
		a = sex.getValidSentAddresses();
		if (a != null)
		    for (int k = 0; k < a.length; k++) 
			validSent.add(a[k]);

		// retrieve validUnsent addresses
		Address[] c = sex.getValidUnsentAddresses();
		if (c != null)
		    for (int l = 0; l < c.length; l++) 
			validUnsent.add(c[l]);
	    } catch (MessagingException mex) {
		sendFailed = true;
		// chain the exception we're catching to any previous ones
		if (chainedEx == null)
		    chainedEx = mex;
		else
		    chainedEx.setNextException(mex);
	    } finally {
		transport.close();
	    }
	}

	// done with all protocols. throw exception if something failed
	if (sendFailed || invalid.size() != 0 || validUnsent.size() != 0) { 
	    Address[] a = null, b = null, c = null;

	    // copy address lists into arrays
	    if (validSent.size() > 0) {
		a = new Address[validSent.size()];
		validSent.toArray(a);
	    }
	    if (validUnsent.size() > 0) {
		b = new Address[validUnsent.size()];
		validUnsent.toArray(b);
	    }
	    if (invalid.size() > 0) {
		c = new Address[invalid.size()];
		invalid.toArray(c);
	    }
	    throw new SendFailedException("Sending failed", chainedEx, 
					  a, b, c);
	}
    }

    /**
     * Send the Message to the specified list of addresses. An appropriate
     * TransportEvent indicating the delivery status is delivered to any 
     * TransportListener registered on this Transport. Also, if any of
     * the addresses is invalid, a SendFailedException is thrown.
     * Whether or not the message is still sent succesfully to
     * any valid addresses depends on the Transport implementation. <p>
     *
     * Unlike the static <code>send</code> method, the <code>sendMessage</code>
     * method does <em>not</em> call the <code>saveChanges</code> method on
     * the message; the caller should do so.
     *
     * @param msg	The Message to be sent
     * @param addresses	array of addresses to send this message to
     * @see 		javax.mail.event.TransportEvent
     * @exception SendFailedException if the send failed because of
     *			invalid addresses.
     * @exception MessagingException if the connection is dead or not in the 
     * 				connected state
     */
    public abstract void sendMessage(Message msg, Address[] addresses) 
				throws MessagingException;

    // Vector of Transport listeners
    private volatile Vector<TransportListener> transportListeners = null;

    /**
     * Add a listener for Transport events. <p>
     *
     * The default implementation provided here adds this listener
     * to an internal list of TransportListeners.
     *
     * @param l         the Listener for Transport events
     * @see             javax.mail.event.TransportEvent
     */
    public synchronized void addTransportListener(TransportListener l) {
	if (transportListeners == null)
	    transportListeners = new Vector<>();
	transportListeners.addElement(l);
    }

    /**
     * Remove a listener for Transport events. <p>
     *
     * The default implementation provided here removes this listener
     * from the internal list of TransportListeners.
     *
     * @param l         the listener
     * @see             #addTransportListener
     */
    public synchronized void removeTransportListener(TransportListener l) {
	if (transportListeners != null)
	    transportListeners.removeElement(l);
    }

    /**
     * Notify all TransportListeners. Transport implementations are
     * expected to use this method to broadcast TransportEvents.<p>
     *
     * The provided default implementation queues the event into
     * an internal event queue. An event dispatcher thread dequeues
     * events from the queue and dispatches them to the registered
     * TransportListeners. Note that the event dispatching occurs
     * in a separate thread, thus avoiding potential deadlock problems.
     *
     * @param	type	the TransportEvent type
     * @param	validSent valid addresses to which message was sent
     * @param	validUnsent valid addresses to which message was not sent
     * @param	invalid the invalid addresses
     * @param	msg	the message
     */
    protected void notifyTransportListeners(int type, Address[] validSent,
					    Address[] validUnsent,
					    Address[] invalid, Message msg) {
	if (transportListeners == null)
	    return;
	
	TransportEvent e = new TransportEvent(this, type, validSent, 
					      validUnsent, invalid, msg);
	queueEvent(e, transportListeners);
    }
}
