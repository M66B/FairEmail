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
 * The exception thrown when a write is attempted on a read-only attribute
 * of any Messaging object. 
 *
 * @author John Mani
 */

public class IllegalWriteException extends MessagingException {

    private static final long serialVersionUID = 3974370223328268013L;

    /**
     * Constructs an IllegalWriteException with no detail message.
     */
    public IllegalWriteException() {
	super();
    }

    /**
     * Constructs an IllegalWriteException with the specified
     * detail message.
     *
     * @param s		The detailed error message
     */
    public IllegalWriteException(String s) {
	super(s);
    }

    /**
     * Constructs an IllegalWriteException with the specified
     * detail message and embedded exception.  The exception is chained
     * to this exception.
     *
     * @param s		The detailed error message
     * @param e		The embedded exception
     * @since		JavaMail 1.5
     */
    public IllegalWriteException(String s, Exception e) {
	super(s, e);
    }
}
