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

package com.sun.mail.util;

import javax.mail.MessagingException;

/**
 * A MessagingException that indicates a socket connection attempt failed.
 * Unlike java.net.ConnectException, it includes details of what we
 * were trying to connect to.  The underlying exception is available
 * as the "cause" of this exception.
 *
 * @see		java.net.ConnectException
 * @author	Bill Shannon
 * @since 	JavaMail 1.5.0
 */

public class MailConnectException extends MessagingException {
    private String host;
    private int port;
    private int cto;

    private static final long serialVersionUID = -3818807731125317729L;

    /**
     * Constructs a MailConnectException.
     *
     * @param	cex	the SocketConnectException with the details
     */
    public MailConnectException(SocketConnectException cex) {
	super(
	    "Couldn't connect to host, port: " +
	    cex.getHost() + ", " + cex.getPort() +
	    "; timeout " + cex.getConnectionTimeout() +
	    (cex.getMessage() != null ? ("; " + cex.getMessage()) : ""));
	// extract the details and save them here
	this.host = cex.getHost();
	this.port = cex.getPort();
	this.cto = cex.getConnectionTimeout();
	setNextException(cex.getException());
    }

    /**
     * The host we were trying to connect to.
     *
     * @return	the host
     */
    public String getHost() {
	return host;
    }

    /**
     * The port we were trying to connect to.
     *
     * @return	the port
     */
    public int getPort() {
	return port;
    }

    /**
     * The timeout used for the connection attempt.
     *
     * @return	the connection timeout
     */
    public int getConnectionTimeout() {
	return cto;
    }
}
