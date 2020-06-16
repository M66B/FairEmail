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

package javax.mail.internet;

/**
 * The exception thrown when a wrongly formatted address is encountered.
 *
 * @author Bill Shannon
 * @author Max Spivak
 */

public class AddressException extends ParseException {
    /**
     * The string being parsed.
     *
     * @serial
     */
    protected String ref = null;

    /**
     * The index in the string where the error occurred, or -1 if not known.
     *
     * @serial
     */
    protected int pos = -1;

    private static final long serialVersionUID = 9134583443539323120L;

    /**
     * Constructs an AddressException with no detail message.
     */
    public AddressException() {
	super();
    }

    /**
     * Constructs an AddressException with the specified detail message.
     * @param s		the detail message
     */
    public AddressException(String s) {
	super(s);
    }

    /**
     * Constructs an AddressException with the specified detail message
     * and reference info.
     *
     * @param	s	the detail message
     * @param	ref	the string being parsed
     */
    public AddressException(String s, String ref) {
	super(s);
	this.ref = ref;
    }

    /**
     * Constructs an AddressException with the specified detail message
     * and reference info.
     *
     * @param	s	the detail message
     * @param	ref	the string being parsed
     * @param	pos	the position of the error
     */
    public AddressException(String s, String ref, int pos) {
	super(s);
	this.ref = ref;
	this.pos = pos;
    }

    /**
     * Get the string that was being parsed when the error was detected
     * (null if not relevant).
     *
     * @return	the string that was being parsed
     */
    public String getRef() {
	return ref;
    }

    /**
     * Get the position with the reference string where the error was
     * detected (-1 if not relevant).
     *
     * @return	the position within the string of the error
     */
    public int getPos() {
	return pos;
    }

    @Override
    public String toString() {
	String s = super.toString();
	if (ref == null)
	    return s;
	s += " in string ``" + ref + "''";
	if (pos < 0)
	    return s;
	return s + " at position " + pos;
    }
}
