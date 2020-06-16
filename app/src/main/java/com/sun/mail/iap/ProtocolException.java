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

package com.sun.mail.iap;

/**
 * @author John Mani
 */

public class ProtocolException extends Exception {
    protected transient Response response = null;

    private static final long serialVersionUID = -4360500807971797439L;

    /**
     * Constructs a ProtocolException with no detail message.
     */
    public ProtocolException() {
	super();
    }

    /**
     * Constructs a ProtocolException with the specified detail message.
     *
     * @param message		the detail message
     */
    public ProtocolException(String message) {
	super(message);
    }

    /**
     * Constructs a ProtocolException with the specified detail message
     * and cause.
     *
     * @param message		the detail message
     * @param cause		the cause
     */
    public ProtocolException(String message, Throwable cause) {
	super(message, cause);
    }

    /**
     * Constructs a ProtocolException with the specified Response object.
     *
     * @param	r	the Response
     */
    public ProtocolException(Response r) {
	super(r.toString());
	response = r;
    }

    /**
     * Return the offending Response object.
     *
     * @return	the Response object
     */
    public Response getResponse() {
	return response;
    }
}
