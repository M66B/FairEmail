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

/**
 * This class implements comparisons for the From Address header.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class FromTerm extends AddressTerm {

    private static final long serialVersionUID = 5214730291502658665L;

    /**
     * Constructor
     * @param address	The Address to be compared
     */
    public FromTerm(Address address) {
	super(address);
    }

    /**
     * The address comparator.
     *
     * @param msg	The address comparison is applied to this Message
     * @return		true if the comparison succeeds, otherwise false
     */
    @Override
    public boolean match(Message msg) {
	Address[] from;

	try {
	    from = msg.getFrom();
	} catch (Exception e) {
	    return false;
	}

	if (from == null)
	    return false;

	for (int i=0; i < from.length; i++)
	    if (super.match(from[i]))
		return true;
	return false;
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof FromTerm))
	    return false;
	return super.equals(obj);
    }
}
