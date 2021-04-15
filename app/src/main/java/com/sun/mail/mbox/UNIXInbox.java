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

public class UNIXInbox extends UNIXFolder implements InboxFile {
    private final String user;

    private static final long serialVersionUID = 651261842162777620L;

    /*
     * Superclass UNIXFile loads the library containing all the
     * native code and sets the "loaded" flag if successful.
     */

    public UNIXInbox(String user, String name) {
	super(name);
	this.user = user;
	if (user == null)
	    throw new NullPointerException("user name is null in UNIXInbox");
    }

    public boolean lock(String mode) {
	if (lockType == NATIVE) {
	    if (!loaded)
		return false;
	    if (!maillock(user, 5))
		return false;
	}
	if (!super.lock(mode)) {
	    if (loaded)
		mailunlock();
	    return false;
	}
	return true;
    }

    public void unlock() { 
	super.unlock();
	if (loaded)
	    mailunlock();
    }

    public void touchlock() {
	if (loaded)
	    touchlock0();
    }

    private transient RandomAccessFile lockfile; // the user's ~/.Maillock file
    private transient String lockfileName;	// its name

    public boolean openLock(String mode) {
	if (mode.equals("r"))
	    return true;
	if (lockfileName == null) {
	    String home = System.getProperty("user.home");
	    lockfileName = home + File.separator + ".Maillock";
	}
	try {
	    lockfile = new RandomAccessFile(lockfileName, mode);
	    boolean ret;
	    switch (lockType) {
	    case NONE:
		ret = true;
		break;
	    case NATIVE:
	    default:
		ret = UNIXFile.lock(lockfile.getFD(), mode);
		break;
	    case JAVA:
		ret = lockfile.getChannel().
		    tryLock(0L, Long.MAX_VALUE, !mode.equals("rw")) != null;
		break;
	    }
	    if (!ret)
		closeLock();
	    return ret;
	} catch (IOException ex) {
	}
	return false;
    }

    public void closeLock() {
	if (lockfile == null)
	    return;
	try {
	    lockfile.close();
	} catch (IOException ex) {
	} finally {
	    lockfile = null;
	}
    }

    public boolean equals(Object o) {
	if (!(o instanceof UNIXInbox))
	    return false;
	UNIXInbox other = (UNIXInbox)o;
	return user.equals(other.user) && super.equals(other);
    }

    public int hashCode() {
	return super.hashCode() + user.hashCode();
    }

    private native boolean maillock(String user, int retryCount);
    private native void mailunlock();
    private native void touchlock0();
}
