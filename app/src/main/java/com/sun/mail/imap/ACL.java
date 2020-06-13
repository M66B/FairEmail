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

package com.sun.mail.imap;

import java.util.*;

/**
 * An access control list entry for a particular authentication identifier
 * (user or group).  Associates a set of Rights with the identifier.
 * See RFC 2086.
 * <p>
 *
 * @author Bill Shannon
 */

public class ACL implements Cloneable {

    private String name;
    private Rights rights;

    /**
     * Construct an ACL entry for the given identifier and with no rights.
     *
     * @param	name	the identifier name
     */
    public ACL(String name) {
	this.name = name;
	this.rights = new Rights();
    }

    /**
     * Construct an ACL entry for the given identifier with the given rights.
     *
     * @param	name	the identifier name
     * @param	rights	the rights
     */
    public ACL(String name, Rights rights) {
	this.name = name;
	this.rights = rights;
    }

    /**
     * Get the identifier name for this ACL entry.
     *
     * @return	the identifier name
     */
    public String getName() {
	return name;
    }

    /**
     * Set the rights associated with this ACL entry.
     *
     * @param	rights	the rights
     */
    public void setRights(Rights rights) {
	this.rights = rights;
    }

    /**
     * Get the rights associated with this ACL entry.
     * Returns the actual Rights object referenced by this ACL;
     * modifications to the Rights object will effect this ACL.
     *
     * @return	the rights
     */
    public Rights getRights() {
	return rights;
    }

    /**
     * Clone this ACL entry.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	ACL acl = (ACL)super.clone();
	acl.rights = (Rights)this.rights.clone();
	return acl;
    }
}
