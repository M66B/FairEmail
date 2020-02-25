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

import javax.mail.Address;

/**
 * This class implements Message Address comparisons.
 *
 * @author Bill Shannon
 * @author John Mani
 */

public abstract class AddressTerm extends SearchTerm {
    /**
     * The address.
     *
     * @serial
     */
    protected Address address;

    private static final long serialVersionUID = 2005405551929769980L;

    protected AddressTerm(Address address) {
	this.address = address;
    }

    /**
     * Return the address to match with.
     *
     * @return	the adddress
     */
    public Address getAddress() {
	return address;
    }

    /**
     * Match against the argument Address.
     *
     * @param	a	the address to match
     * @return	true if it matches
     */
    protected boolean match(Address a) {
	return (a.equals(address));
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof AddressTerm))
	    return false;
	AddressTerm at = (AddressTerm)obj;
	return at.address.equals(this.address);
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return address.hashCode();
    }
}
