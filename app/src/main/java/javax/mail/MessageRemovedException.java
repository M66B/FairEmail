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
 * The exception thrown when an invalid method is invoked on an expunged
 * Message. The only valid methods on an expunged Message are
 * <code>isExpunged()</code> and <code>getMessageNumber()</code>.
 *
 * @see	   javax.mail.Message#isExpunged()
 * @see	   javax.mail.Message#getMessageNumber()
 * @author John Mani
 */

public class MessageRemovedException extends MessagingException {

    private static final long serialVersionUID = 1951292550679528690L;

    /**
     * Constructs a MessageRemovedException with no detail message.
     */
    public MessageRemovedException() {
	super();
    }

    /**
     * Constructs a MessageRemovedException with the specified
     * detail message.
     *
     * @param s		The detailed error message
     */
    public MessageRemovedException(String s) {
	super(s);
    }

    /**
     * Constructs a MessageRemovedException with the specified
     * detail message and embedded exception.  The exception is chained
     * to this exception.
     *
     * @param s		The detailed error message
     * @param e		The embedded exception
     * @since		JavaMail 1.5
     */
    public MessageRemovedException(String s, Exception e) {
	super(s, e);
    }
}
