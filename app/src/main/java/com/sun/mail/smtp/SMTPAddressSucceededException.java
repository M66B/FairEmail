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

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

/**
 * This exception is chained off a SendFailedException when the
 * <code>mail.smtp.reportsuccess</code> property is true.  It
 * indicates an address to which the message was sent.  The command
 * will be an SMTP RCPT command and the return code will be the
 * return code from that command.
 *
 * @since JavaMail 1.3.2
 */

public class SMTPAddressSucceededException extends MessagingException {
    protected InternetAddress addr;	// address that succeeded
    protected String cmd;		// command issued to server
    protected int rc;			// return code from SMTP server

    private static final long serialVersionUID = -1168335848623096749L;

    /**
     * Constructs an SMTPAddressSucceededException with the specified 
     * address, return code, and error string.
     *
     * @param addr	the address that succeeded
     * @param cmd	the command that was sent to the SMTP server
     * @param rc	the SMTP return code indicating the success
     * @param err	the error string from the SMTP server
     */
    public SMTPAddressSucceededException(InternetAddress addr,
				String cmd, int rc, String err) {
	super(err);
	this.addr = addr;
	this.cmd = cmd;
	this.rc = rc;
    }

    /**
     * Return the address that succeeded.
     *
     * @return	the address
     */
    public InternetAddress getAddress() {
	return addr;
    }

    /**
     * Return the command that succeeded.
     *
     * @return	the command
     */
    public String getCommand() {
	return cmd;
    }


    /**
     * Return the return code from the SMTP server that indicates the
     * reason for the success.  See
     * <A HREF="http://www.ietf.org/rfc/rfc821.txt">RFC 821</A>
     * for interpretation of the return code.
     *
     * @return	the return code
     */
    public int getReturnCode() {
	return rc;
    }
}
