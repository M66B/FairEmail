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

import java.net.InetAddress;

/**
 * The class Authenticator represents an object that knows how to obtain
 * authentication for a network connection.  Usually, it will do this
 * by prompting the user for information.
 * <p>
 * Applications use this class by creating a subclass, and registering
 * an instance of that subclass with the session when it is created.
 * When authentication is required, the system will invoke a method
 * on the subclass (like getPasswordAuthentication).  The subclass's
 * method can query about the authentication being requested with a
 * number of inherited methods (getRequestingXXX()), and form an
 * appropriate message for the user.
 * <p>
 * All methods that request authentication have a default implementation
 * that fails.
 *
 * @see java.net.Authenticator
 * @see javax.mail.Session#getInstance(java.util.Properties,
 *					javax.mail.Authenticator)
 * @see javax.mail.Session#getDefaultInstance(java.util.Properties,
 *					javax.mail.Authenticator)
 * @see javax.mail.Session#requestPasswordAuthentication
 * @see javax.mail.PasswordAuthentication
 *
 * @author  Bill Foote
 * @author  Bill Shannon
 */

// There are no abstract methods, but to be useful the user must
// subclass.
public abstract class Authenticator {

    private InetAddress requestingSite;
    private int requestingPort;
    private String requestingProtocol;
    private String requestingPrompt;
    private String requestingUserName;

    /**
     * Ask the authenticator for a password.
     * <p>
     *
     * @param addr The InetAddress of the site requesting authorization,
     *             or null if not known.
     * @param port the port for the requested connection
     * @param protocol The protocol that's requesting the connection
     *          (@see java.net.Authenticator.getProtocol())
     * @param prompt A prompt string for the user
     *
     * @return The username/password, or null if one can't be gotten.
     */
    final synchronized PasswordAuthentication requestPasswordAuthentication(
				InetAddress addr, int port, String protocol,
				String prompt, String defaultUserName) {
	requestingSite = addr;
	requestingPort = port;
	requestingProtocol = protocol;
	requestingPrompt = prompt;
	requestingUserName = defaultUserName;
	return getPasswordAuthentication();
    }

    /**
     * @return the InetAddress of the site requesting authorization, or null
     *		if it's not available.
     */
    protected final InetAddress getRequestingSite() {
	return requestingSite;
    }

    /**
     * @return the port for the requested connection
     */
    protected final int getRequestingPort() {
	return requestingPort;
    }

    /**
     * Give the protocol that's requesting the connection.  Often this
     * will be based on a URLName.
     *
     * @return the protcol
     *
     * @see javax.mail.URLName#getProtocol
     */
    protected final String getRequestingProtocol() {
	return requestingProtocol;
    }

    /**
     * @return the prompt string given by the requestor
     */
    protected final String getRequestingPrompt() {
	return requestingPrompt;
    }

    /**
     * @return the default user name given by the requestor
     */
    protected final String getDefaultUserName() {
	return requestingUserName;
    }

    /**
     * Called when password authentication is needed.  Subclasses should
     * override the default implementation, which returns null. <p>
     *
     * Note that if this method uses a dialog to prompt the user for this
     * information, the dialog needs to block until the user supplies the
     * information.  This method can not simply return after showing the
     * dialog.
     * @return The PasswordAuthentication collected from the
     *		user, or null if none is provided.
     */
    protected PasswordAuthentication getPasswordAuthentication() {
	return null;
    }
}
