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

package com.sun.mail.smtp;

import javax.mail.Address;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;

/**
 * This exception is thrown when the message cannot be sent. <p>
 * 
 * This exception will usually appear first in a chained list of exceptions,
 * followed by SMTPAddressFailedExceptions and/or
 * SMTPAddressSucceededExceptions, * one per address.
 * This exception corresponds to one of the SMTP commands used to
 * send a message, such as the MAIL, DATA, and "end of data" commands,
 * but not including the RCPT command.
 *
 * @since JavaMail 1.3.2
 */

public class SMTPSendFailedException extends SendFailedException {
    protected InternetAddress addr;	// address that failed
    protected String cmd;		// command issued to server
    protected int rc;			// return code from SMTP server

    private static final long serialVersionUID = 8049122628728932894L;

    /**
     * Constructs an SMTPSendFailedException with the specified 
     * address, return code, and error string.
     *
     * @param cmd	the command that was sent to the SMTP server
     * @param rc	the SMTP return code indicating the failure
     * @param err	the error string from the SMTP server
     * @param ex	a chained exception
     * @param vs	the valid addresses the message was sent to
     * @param vus	the valid addresses the message was not sent to
     * @param inv	the invalid addresses
     */
    public SMTPSendFailedException(String cmd, int rc, String err, Exception ex,
				Address[] vs, Address[] vus, Address[] inv) {
	super(err, ex, vs, vus, inv);
	this.cmd = cmd;
	this.rc = rc;
    }

    /**
     * Return the command that failed.
     *
     * @return	the command
     */
    public String getCommand() {
	return cmd;
    }

    /**
     * Return the return code from the SMTP server that indicates the
     * reason for the failure.  See
     * <A HREF="http://www.ietf.org/rfc/rfc821.txt">RFC 821</A>
     * for interpretation of the return code.
     *
     * @return	the return code
     */
    public int getReturnCode() {
	return rc;
    }
}
