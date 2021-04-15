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
import java.io.FileDescriptor;
import java.util.StringTokenizer;

public class UNIXFile extends File {
    protected static final boolean loaded;
    protected static final int lockType;

    private static final long serialVersionUID = -7972156315284146651L;

    public UNIXFile(String name) {
	super(name);
    }

    // lock type enum
    protected static final int NONE = 0;
    protected static final int NATIVE = 1;
    protected static final int JAVA = 2;

    static {
	String lt = System.getProperty("mail.mbox.locktype", "native");
	int type = NATIVE;
	if (lt.equalsIgnoreCase("none"))
	    type = NONE;
	else if (lt.equalsIgnoreCase("java"))
	    type = JAVA;
	lockType = type;

	boolean lloaded = false;
	if (lockType == NATIVE) {
	    try {
		System.loadLibrary("mbox");
		lloaded = true;
	    } catch (UnsatisfiedLinkError e) {
		String classpath = System.getProperty("java.class.path");
		String sep = System.getProperty("path.separator");
		String arch = System.getProperty("os.arch");
		StringTokenizer st = new StringTokenizer(classpath, sep);
		while (st.hasMoreTokens()) {
		    String path = st.nextToken();
		    if (path.endsWith("/classes") ||
			    path.endsWith("/mail.jar") ||
			    path.endsWith("/javax.mail.jar")) {
			int i = path.lastIndexOf('/');
			String libdir = path.substring(0, i + 1) + "lib/";
			String lib = libdir + arch + "/libmbox.so";
			try {
			    System.load(lib);
			    lloaded = true;
			    break;
			} catch (UnsatisfiedLinkError e2) {
			    lib = libdir + "libmbox.so";
			    try {
				System.load(lib);
				lloaded = true;
				break;
			    } catch (UnsatisfiedLinkError e3) {
				continue;
			    }
			}
		    }
		}
	    }
	}
	loaded = lloaded;
	if (loaded)
	    initIDs(FileDescriptor.class, FileDescriptor.in);
    }

    /**
     * Return the access time of the file.
     */
    public static long lastAccessed(File file) {
	return lastAccessed0(file.getPath());
    }

    public long lastAccessed() {
	return lastAccessed0(getPath());
    }

    private static native void initIDs(Class<FileDescriptor> fdClass,
					FileDescriptor stdin);

    /**
     * Lock the file referred to by fd.  The string mode is "r"
     * for a read lock or "rw" for a write lock.  Don't block
     * if lock can't be acquired.
     */
    public static boolean lock(FileDescriptor fd, String mode) {
	return lock(fd, mode, false);
    }

    /**
     * Lock the file referred to by fd.  The string mode is "r"
     * for a read lock or "rw" for a write lock.  If block is set,
     * block waiting for the lock if necessary.
     */
    private static boolean lock(FileDescriptor fd, String mode, boolean block) {
	//return loaded && lock0(fd, mode);
	if (loaded) {
	    boolean ret;
	    //System.out.println("UNIXFile.lock(" + fd + ", " + mode + ")");
	    ret = lock0(fd, mode, block);
	    //System.out.println("UNIXFile.lock returns " + ret);
	    return ret;
	}
	return false;
    }

    private static native boolean lock0(FileDescriptor fd, String mode,
								boolean block);

    public static native long lastAccessed0(String name);
}
