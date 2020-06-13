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
 * This exception is thrown when the connect method on a Store or
 * Transport object fails due to an authentication failure (e.g.,
 * bad user name or password).
 *
 * @author Bill Shannon
 */

public class AuthenticationFailedException extends MessagingException {

    private static final long serialVersionUID = 492080754054436511L;

    /**
     * Constructs an AuthenticationFailedException.
     */
    public AuthenticationFailedException() {
	super();
    }

    /**
     * Constructs an AuthenticationFailedException with the specified
     * detail message.
     *
     * @param message	The detailed error message
     */
    public AuthenticationFailedException(String message) {
	super(message);
    }

    /**
     * Constructs an AuthenticationFailedException with the specified
     * detail message and embedded exception.  The exception is chained
     * to this exception.
     *
     * @param message	The detailed error message
     * @param e		The embedded exception
     * @since		JavaMail 1.5
     */
    public AuthenticationFailedException(String message, Exception e) {
	super(message, e);
    }
}
