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

import java.lang.*;

/**
 * The base class for all exceptions thrown by the Messaging classes
 *
 * @author John Mani
 * @author Bill Shannon
 */

public class MessagingException extends Exception {

    /**
     * The next exception in the chain.
     *
     * @serial
     */
    private Exception next;

    private static final long serialVersionUID = -7569192289819959253L;

    /**
     * Constructs a MessagingException with no detail message.
     */
    public MessagingException() {
	super();
	initCause(null);	// prevent anyone else from setting it
    }

    /**
     * Constructs a MessagingException with the specified detail message.
     *
     * @param s		the detail message
     */
    public MessagingException(String s) {
	super(s);
	initCause(null);	// prevent anyone else from setting it
    }

    /**
     * Constructs a MessagingException with the specified 
     * Exception and detail message. The specified exception is chained
     * to this exception.
     *
     * @param s		the detail message
     * @param e		the embedded exception
     * @see	#getNextException
     * @see	#setNextException
     * @see	#getCause
     */
    public MessagingException(String s, Exception e) {
	super(s);
	next = e;
	initCause(null);	// prevent anyone else from setting it
    }

    /**
     * Get the next exception chained to this one. If the
     * next exception is a MessagingException, the chain
     * may extend further.
     *
     * @return	next Exception, null if none.
     */
    public synchronized Exception getNextException() {
	return next;
    }

    /**
     * Overrides the <code>getCause</code> method of <code>Throwable</code>
     * to return the next exception in the chain of nested exceptions.
     *
     * @return	next Exception, null if none.
     */
    @Override
    public synchronized Throwable getCause() {
	return next;
    }

    /**
     * Add an exception to the end of the chain. If the end
     * is <strong>not</strong> a MessagingException, this 
     * exception cannot be added to the end.
     *
     * @param	ex	the new end of the Exception chain
     * @return		<code>true</code> if this Exception
     *			was added, <code>false</code> otherwise.
     */
    public synchronized boolean setNextException(Exception ex) {
	Exception theEnd = this;
	while (theEnd instanceof MessagingException &&
	       ((MessagingException)theEnd).next != null) {
	    theEnd = ((MessagingException)theEnd).next;
	}
	// If the end is a MessagingException, we can add this 
	// exception to the chain.
	if (theEnd instanceof MessagingException) {
	    ((MessagingException)theEnd).next = ex;
	    return true;
	} else
	    return false;
    }

    /**
     * Override toString method to provide information on
     * nested exceptions.
     */
    @Override
    public synchronized String toString() {
	String s = super.toString();
	Exception n = next;
	if (n == null)
	    return s;
	StringBuilder sb = new StringBuilder(s == null ? "" : s);
	while (n != null) {
	    sb.append(";\n  nested exception is:\n\t");
	    if (n instanceof MessagingException) {
		MessagingException mex = (MessagingException)n;
		sb.append(mex.superToString());
		n = mex.next;
	    } else {
		sb.append(n.toString());
		n = null;
	    }
	}
	return sb.toString();
    }

    /**
     * Return the "toString" information for this exception,
     * without any information on nested exceptions.
     */
    private final String superToString() {
	return super.toString();
    }
}
