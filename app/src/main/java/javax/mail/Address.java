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

import java.io.Serializable;

/**
 * This abstract class models the addresses in a message.
 * Subclasses provide specific implementations.  Subclasses
 * will typically be serializable so that (for example) the
 * use of Address objects in search terms can be serialized
 * along with the search terms.
 *
 * @author John Mani
 * @author Bill Shannon
 */

public abstract class Address implements Serializable {

    private static final long serialVersionUID = -5822459626751992278L;

    /**
     * Return a type string that identifies this address type.
     *
     * @return	address type
     * @see	javax.mail.internet.InternetAddress
     */
    public abstract String getType();

    /**
     * Return a String representation of this address object.
     *
     * @return	string representation of this address
     */
    @Override
    public abstract String toString();

    /**
     * The equality operator.  Subclasses should provide an
     * implementation of this method that supports value equality
     * (do the two Address objects represent the same destination?),
     * not object reference equality.  A subclass must also provide
     * a corresponding implementation of the <code>hashCode</code>
     * method that preserves the general contract of
     * <code>equals</code> and <code>hashCode</code> - objects that
     * compare as equal must have the same hashCode.
     *
     * @param	address	Address object
     */
    @Override
    public abstract boolean equals(Object address);
}
