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

public class UNIXFolder extends UNIXFile implements MailFile {
    protected transient RandomAccessFile file;

    private static final long serialVersionUID = -254578891263785591L;

    public UNIXFolder(String name) {
	super(name);
    }

    public boolean lock(String mode) {
	try {
	    file = new RandomAccessFile(this, mode);
	    switch (lockType) {
	    case NONE:
		return true;
	    case NATIVE:
	    default:
		return UNIXFile.lock(file.getFD(), mode);
	    case JAVA:
		return file.getChannel().
		    tryLock(0L, Long.MAX_VALUE, !mode.equals("rw")) != null;
	    }
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
