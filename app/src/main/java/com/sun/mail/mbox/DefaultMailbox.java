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

public class DefaultMailbox extends Mailbox {
    private final String home;

    private static final boolean homeRelative =
				Boolean.getBoolean("mail.mbox.homerelative");

    public DefaultMailbox() {
	home = System.getProperty("user.home");
    }

    public MailFile getMailFile(String user, String folder) {
	return new DefaultMailFile(filename(user, folder));
    }

    public String filename(String user, String folder) {
	try {
	    char c = folder.charAt(0);
	    if (c == File.separatorChar) {
		return folder;
	    } else if (c == '~') {
		int i = folder.indexOf(File.separatorChar);
		String tail = "";
		if (i > 0) {
		    tail = folder.substring(i);
		    folder = folder.substring(0, i);
		}
		return home + tail;
	    } else {
		if (folder.equalsIgnoreCase("INBOX"))
		    folder = "INBOX";
		if (homeRelative)
		    return home + File.separator + folder;
		else
		    return folder;
	    }
	} catch (StringIndexOutOfBoundsException e) {
	    return folder;
	}
    }
}

class DefaultMailFile extends File implements MailFile {
    protected transient RandomAccessFile file;

    private static final long serialVersionUID = 3713116697523761684L;

    DefaultMailFile(String name) {
	super(name);
    }

    public boolean lock(String mode) {
	try {
	    file = new RandomAccessFile(this, mode);
	    return true;
	} catch (FileNotFoundException fe) {
	    return false;
	} catch (IOException ie) {
	    file = null;
	    return false;
	}
    }

    public void unlock() { 
	if (file != null) {
	    try {
		file.close();
	    } catch (IOException e) {
		// ignore it
	    }
	    file = null;
	}
    }

    public void touchlock() {
    }

    public FileDescriptor getFD() {
	if (file == null)
	    return null;
	try {
	    return file.getFD();
	} catch (IOException e) {
	    return null;
	}
    }
}
