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

package javax.mail.search;

import javax.mail.Message;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;

/**
 * This abstract class implements string comparisons for Message 
 * addresses. <p>
 *
 * Note that this class differs from the <code>AddressTerm</code> class
 * in that this class does comparisons on address strings rather than
 * Address objects.
 *
 * @since       JavaMail 1.1
 */

public abstract class AddressStringTerm extends StringTerm {

    private static final long serialVersionUID = 3086821234204980368L;

    /**
     * Constructor.
     *
     * @param pattern   the address pattern to be compared.
     */
    protected AddressStringTerm(String pattern) {
	super(pattern, true); // we need case-insensitive comparison.
    }

    /**
     * Check whether the address pattern specified in the constructor is
     * a substring of the string representation of the given Address
     * object. <p>
     *
     * Note that if the string representation of the given Address object
     * contains charset or transfer encodings, the encodings must be 
     * accounted for, during the match process. <p>
     *
     * @param   a 	The comparison is applied to this Address object.
     * @return          true if the match succeeds, otherwise false.
     */
    protected boolean match(Address a) {
	if (a instanceof InternetAddress) {
	    InternetAddress ia = (InternetAddress)a;
	    // We dont use toString() to get "a"'s String representation,
	    // because InternetAddress.toString() returns a RFC 2047 
	    // encoded string, which isn't what we need here.

	    return super.match(ia.toUnicodeString());
	} else
	    return super.match(a.toString());
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof AddressStringTerm))
	    return false;
	return super.equals(obj);
    }
}
