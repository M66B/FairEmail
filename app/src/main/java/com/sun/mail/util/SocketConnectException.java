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

import java.io.IOException;

/**
 * An IOException that indicates a socket connection attempt failed.
 * Unlike java.net.ConnectException, it includes details of what we
 * were trying to connect to.
 *
 * @see		java.net.ConnectException
 * @author	Bill Shannon
 * @since 	JavaMail 1.5.0
 */

public class SocketConnectException extends IOException {
    /**
     * The socket host name.
     */
    private String host;
    /**
     * The socket port.
     */
    private int port;
    /**
     * The connection timeout.
     */
    private int cto;
    /**
     * The generated serial id.
     */
    private static final long serialVersionUID = 3997871560538755463L;

    /**
     * Constructs a SocketConnectException.
     *
     * @param	msg	error message detail
     * @param	cause	the underlying exception that indicates the failure
     * @param	host	the host we were trying to connect to
     * @param	port	the port we were trying to connect to
     * @param	cto	the timeout for the connection attempt
     */
    public SocketConnectException(String msg, Exception cause,
				    String host, int port, int cto) {
	super(msg);
	initCause(cause);
	this.host = host;
	this.port = port;
	this.cto = cto;
    }

    /**
     * The exception that caused the failure.
     *
     * @return	the exception
     */
    public Exception getException() {
	// the "cause" is always an Exception; see constructor above
	Throwable t = getCause();
	assert t == null || t instanceof Exception;
	return (Exception) t;
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
