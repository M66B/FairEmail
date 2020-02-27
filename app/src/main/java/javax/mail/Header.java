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
 * The Header class stores a name/value pair to represent headers.
 *
 * @author John Mani
 */

public class Header {

    /**
     * The name of the header.
     *
     * @since	JavaMail 1.4
     */
    protected String name;

    /**
     * The value of the header.
     *
     * @since	JavaMail 1.4
     */
    protected String value;

    /**
     * Construct a Header object.
     *
     * @param name	name of the header
     * @param value	value of the header
     */
    public Header(String name, String value) {
	this.name = name;
	this.value = value;
    }

    /**
     * Returns the name of this header.
     *
     * @return 		name of the header
     */
    public String getName() {
	return name;
    }

    /**
     * Returns the value of this header.
     *
     * @return 		value of the header
     */
    public String getValue() {
	return value;
    }
}
