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

public class BadCommandException extends ProtocolException {

    private static final long serialVersionUID = 5769722539397237515L;

    /**
     * Constructs an BadCommandException with no detail message.
     */
    public BadCommandException() {
	super();
    }

    /**
     * Constructs an BadCommandException with the specified detail message.
     * @param s		the detail message
     */
    public BadCommandException(String s) {
	super(s);
    }

    /**
     * Constructs an BadCommandException with the specified Response.
     * @param r		the Response
     */
    public BadCommandException(Response r) {
	super(r);
    }
}
