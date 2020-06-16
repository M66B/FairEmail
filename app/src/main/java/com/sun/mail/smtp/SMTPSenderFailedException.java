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

import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;

/**
 * This exception is thrown when the message cannot be sent. <p>
 * 
 * The exception includes the sender's address, which the mail server
 * rejected.
 *
 * @since JavaMail 1.4.4
 */

public class SMTPSenderFailedException extends SendFailedException {
    protected InternetAddress addr;	// address that failed
    protected String cmd;		// command issued to server
    protected int rc;			// return code from SMTP server

    private static final long serialVersionUID = 514540454964476947L;

    /**
     * Constructs an SMTPSenderFailedException with the specified 
     * address, return code, and error string.
     *
     * @param addr	the address that failed
     * @param cmd	the command that was sent to the SMTP server
     * @param rc	the SMTP return code indicating the failure
     * @param err	the error string from the SMTP server
     */
    public SMTPSenderFailedException(InternetAddress addr, String cmd, int rc,
				String err) {
	super(err);
	this.addr = addr;
	this.cmd = cmd;
	this.rc = rc;
    }

    /**
     * Return the address that failed.
     *
     * @return	the address
     */
    public InternetAddress getAddress() {
	return addr;
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
