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

public class CommandFailedException extends ProtocolException {

    private static final long serialVersionUID = 793932807880443631L;

    /**
     * Constructs an CommandFailedException with no detail message.
     */
    public CommandFailedException() {
	super();
    }

    /**
     * Constructs an CommandFailedException with the specified detail message.
     * @param s		the detail message
     */
    public CommandFailedException(String s) {
	super(s);
    }

    /**
     * Constructs an CommandFailedException with the specified Response.
     * @param r		the Response.
     */
    public CommandFailedException(Response r) {
	super(r);
    }
}
