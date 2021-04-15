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

package com.sun.mail.mbox;

import java.io.*;
import javax.mail.*;

public class MboxStore extends Store {

    String user;
    String home;
    Mailbox mb;
    static Flags permFlags;

    static {
	// we support all flags
	permFlags = new Flags();
	permFlags.add(Flags.Flag.SEEN);
	permFlags.add(Flags.Flag.RECENT);
	permFlags.add(Flags.Flag.DELETED);
	permFlags.add(Flags.Flag.FLAGGED);
	permFlags.add(Flags.Flag.ANSWERED);
	permFlags.add(Flags.Flag.DRAFT);
	permFlags.add(Flags.Flag.USER);
    }

    public MboxStore(Session session, URLName url) {
	super(session, url);

	// XXX - handle security exception
	user = System.getProperty("user.name");
	home = System.getProperty("user.home");
	String os = System.getProperty("os.name");
	try {
	    String cl = "com.sun.mail.mbox." + os + "Mailbox";
	    mb = (Mailbox)Class.forName(cl).
					getDeclaredConstructor().newInstance();
	} catch (Exception e) {
	    mb = new DefaultMailbox();
	}
    }

    /**
     * Since we do not have any authentication
     * to do and we do not want a dialog put up asking the user for a 
     * password we always succeed in connecting.
     * But if we're given a password, that means the user is
     * doing something wrong so fail the request.
     */
    protected boolean protocolConnect(String host, int port, String user,
				String passwd) throws MessagingException {

	if (passwd != null)
	    throw new AuthenticationFailedException(
				"mbox does not allow passwords");
	// XXX - should we use the user?
	return true;
    }

    protected void setURLName(URLName url) {
	// host, user, password, and file don't matter so we strip them out
	if (url != null && (url.getUsername() != null ||
			    url.getHost() != null ||
			    url.getFile() != null))
	    url = new URLName(url.getProtocol(), null, -1, null, null, null);
	super.setURLName(url);
    }


    public Folder getDefaultFolder() throws MessagingException {
	checkConnected();

	return new MboxFolder(this, null);
    }

    public Folder getFolder(String name) throws MessagingException {
	checkConnected();

	return new MboxFolder(this, name);
    }

    public Folder getFolder(URLName url) throws MessagingException {
	checkConnected();
	return getFolder(url.getFile());
    }

    private void checkConnected() throws MessagingException {
	if (!isConnected())
	    throw new MessagingException("Not connected");
    }

    MailFile getMailFile(String folder) {
	return mb.getMailFile(user, folder);
    }

    Session getSession() {
	return session;
    }
}
