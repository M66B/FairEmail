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
 * This exception is thrown when a method is invoked on a Messaging object
 * and the Store that owns that object has died due to some reason.
 * This exception should be treated as a fatal error; in particular any
 * messaging object belonging to that Store must be considered invalid. <p>
 *
 * The connect method may be invoked on the dead Store object to 
 * revive it. <p>
 *
 * The getMessage() method returns more detailed information about the
 * error that caused this exception. <p>
 *
 * @author John Mani
 */

public class StoreClosedException extends MessagingException {
    transient private Store store;

    private static final long serialVersionUID = -3145392336120082655L;

    /**
     * Constructs a StoreClosedException with no detail message.
     *
     * @param store	The dead Store object
     */
    public StoreClosedException(Store store) {
	this(store, null);
    }

    /**
     * Constructs a StoreClosedException with the specified
     * detail message.
     *
     * @param store	The dead Store object
     * @param message	The detailed error message
     */
    public StoreClosedException(Store store, String message) {
	super(message);
	this.store = store;
    }

    /**
     * Constructs a StoreClosedException with the specified
     * detail message and embedded exception.  The exception is chained
     * to this exception.
     *
     * @param store	The dead Store object
     * @param message	The detailed error message
     * @param e		The embedded exception
     * @since		JavaMail 1.5
     */
    public StoreClosedException(Store store, String message, Exception e) {
	super(message, e);
	this.store = store;
    }

    /**
     * Returns the dead Store object.
     *
     * @return	the dead Store object
     */
    public Store getStore() {
	return store;
    }
}
