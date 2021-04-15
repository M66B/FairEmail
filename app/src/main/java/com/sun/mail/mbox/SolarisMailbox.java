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

import java.io.File;

public class SolarisMailbox extends Mailbox {
    private final String home;
    private final String user;

    private static final boolean homeRelative =
				Boolean.getBoolean("mail.mbox.homerelative");

    public SolarisMailbox() {
	String h = System.getenv("HOME");
	if (h == null)
	    h = System.getProperty("user.home");
	home = h;
	user = System.getProperty("user.name");
    }

    public MailFile getMailFile(String user, String folder) {
	if (folder.equalsIgnoreCase("INBOX"))
	    return new UNIXInbox(user, filename(user, folder));
	else
	    return new UNIXFolder(filename(user, folder));
    }

    /**
     * Given a name of a mailbox folder, expand it to a full path name.
     */
    public String filename(String user, String folder) {
	try {
	    switch (folder.charAt(0)) {
	    case '/':
		return folder;
	    case '~':
		int i = folder.indexOf(File.separatorChar);
		String tail = "";
		if (i > 0) {
		    tail = folder.substring(i);
		    folder = folder.substring(0, i);
		}
		if (folder.length() == 1)
		    return home + tail;
		else
		    return "/home/" + folder.substring(1) + tail;	// XXX
	    default:
		if (folder.equalsIgnoreCase("INBOX")) {
		    if (user == null)	// XXX - should never happen
			user = this.user;
		    String inbox = System.getenv("MAIL");
		    if (inbox == null)
			inbox = "/var/mail/" + user;
		    return inbox;
		} else {
		    if (homeRelative)
			return home + File.separator + folder;
		    else
			return folder;
		}
	    }
	} catch (StringIndexOutOfBoundsException e) {
	    return folder;
	}
    }
}
