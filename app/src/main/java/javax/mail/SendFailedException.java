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

/**
 * This exception is thrown when the message cannot be sent.<p>
 * 
 * The exception includes those addresses to which the message could not be
 * sent as well as the valid addresses to which the message was sent and
 * valid addresses to which the message was not sent.
 *
 * @see	javax.mail.Transport#send
 * @see	javax.mail.Transport#sendMessage
 * @see	javax.mail.event.TransportEvent
 *
 * @author John Mani
 * @author Max Spivak
 */

public class SendFailedException extends MessagingException {
    transient protected Address[] invalid;
    transient protected Address[] validSent;
    transient protected Address[] validUnsent;

    private static final long serialVersionUID = -6457531621682372913L;

    /**
     * Constructs a SendFailedException with no detail message.
     */
    public SendFailedException() {
	super();
    }

    /**
     * Constructs a SendFailedException with the specified detail message.
     * @param s		the detail message
     */
    public SendFailedException(String s) {
	super(s);
    }

    /**
     * Constructs a SendFailedException with the specified 
     * Exception and detail message. The specified exception is chained
     * to this exception.
     * @param s		the detail message
     * @param e		the embedded exception
     * @see	#getNextException
     * @see	#setNextException
     */
    public SendFailedException(String s, Exception e) {
	super(s, e);
    }


    /**
     * Constructs a SendFailedException with the specified string
     * and the specified address objects.
     *
     * @param msg	the detail message
     * @param ex        the embedded exception
     * @param validSent valid addresses to which message was sent
     * @param validUnsent valid addresses to which message was not sent
     * @param invalid 	the invalid addresses
     * @see	#getNextException
     * @see	#setNextException
     */
    public SendFailedException(String msg, Exception ex, Address[] validSent, 
			       Address[] validUnsent, Address[] invalid) {
	super(msg, ex);
	this.validSent = validSent;
	this.validUnsent = validUnsent;
	this.invalid = invalid;
    }

    /**
     * Return the addresses to which this message was sent succesfully.
     * @return Addresses to which the message was sent successfully or null
     */
    public Address[] getValidSentAddresses() {
	return validSent;
    }

    /**
     * Return the addresses that are valid but to which this message 
     * was not sent.
     * @return Addresses that are valid but to which the message was 
     *         not sent successfully or null
     */
    public Address[] getValidUnsentAddresses() {
	return validUnsent;
    }

    /**
     * Return the addresses to which this message could not be sent.
     *
     * @return Addresses to which the message sending failed or null;
     */
    public Address[] getInvalidAddresses() {
	return invalid;
    }
}
