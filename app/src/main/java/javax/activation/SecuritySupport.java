/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.activation;

import java.security.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Security related methods that only work on J2SE 1.2 and newer.
 */
class SecuritySupport {

    private SecuritySupport() {
	// private constructor, can't create an instance
    }

    public static ClassLoader getContextClassLoader() {
	return (ClassLoader)
		AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		ClassLoader cl = null;
		try {
		    cl = Thread.currentThread().getContextClassLoader();
		} catch (SecurityException ex) { }
		return cl;
	    }
	});
    }

    public static InputStream getResourceAsStream(final Class c,
				final String name) throws IOException {
	try {
	    return (InputStream)
		AccessController.doPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws IOException {
			return c.getResourceAsStream(name);
		    }
		});
	} catch (PrivilegedActionException e) {
	    throw (IOException)e.getException();
	}
    }

    public static URL[] getResources(final ClassLoader cl, final String name) {
	return (URL[])
		AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		URL[] ret = null;
		try {
		    List v = new ArrayList();
		    Enumeration e = cl.getResources(name);
		    while (e != null && e.hasMoreElements()) {
			URL url = (URL)e.nextElement();
			if (url != null)
			    v.add(url);
		    }
		    if (v.size() > 0) {
			ret = new URL[v.size()];
			ret = (URL[])v.toArray(ret);
		    }
		} catch (IOException ioex) {
		} catch (SecurityException ex) { }
		return ret;
	    }
	});
    }

    public static URL[] getSystemResources(final String name) {
	return (URL[])
		AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		URL[] ret = null;
		try {
		    List v = new ArrayList();
		    Enumeration e = ClassLoader.getSystemResources(name);
		    while (e != null && e.hasMoreElements()) {
			URL url = (URL)e.nextElement();
			if (url != null)
			    v.add(url);
		    }
		    if (v.size() > 0) {
			ret = new URL[v.size()];
			ret = (URL[])v.toArray(ret);
		    }
		} catch (IOException ioex) {
		} catch (SecurityException ex) { }
		return ret;
	    }
	});
    }

    public static InputStream openStream(final URL url) throws IOException {
	try {
	    return (InputStream)
		AccessController.doPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws IOException {
			return url.openStream();
		    }
		});
	} catch (PrivilegedActionException e) {
	    throw (IOException)e.getException();
	}
    }
}
