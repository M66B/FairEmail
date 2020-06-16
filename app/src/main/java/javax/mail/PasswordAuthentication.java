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
 * The class PasswordAuthentication is a data holder that is used by
 * Authenticator.  It is simply a repository for a user name and a password.
 *
 * @see java.net.PasswordAuthentication
 * @see javax.mail.Authenticator
 * @see javax.mail.Authenticator#getPasswordAuthentication()
 *
 * @author  Bill Foote
 */

public final class PasswordAuthentication {

    private final String userName;
    private final String password;

    /** 
     * Initialize a new PasswordAuthentication
     * @param userName the user name
     * @param password The user's password
     */
    public PasswordAuthentication(String userName, String password) {
	this.userName = userName;
	this.password = password;
    }

    /**
     * @return the user name
     */
    public String getUserName() {
	return userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
	return password;
    }
}
