/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.util;

import java.lang.reflect.*;
import java.security.*;

import javax.mail.internet.MimePart;

/**
 * General MIME-related utility methods.
 *
 * @author	Bill Shannon
 * @since	JavaMail 1.4.4
 */
public class MimeUtil {

    private static final Method cleanContentType;

    static {
	Method meth = null;
	try {
	    String cth = System.getProperty("mail.mime.contenttypehandler");
	    if (cth != null) {
		ClassLoader cl = getContextClassLoader();
		Class<?> clsHandler = null;
		if (cl != null) {
		    try {
			clsHandler = Class.forName(cth, false, cl);
		    } catch (ClassNotFoundException cex) { }
		}
		if (clsHandler == null)
		    clsHandler = Class.forName(cth);
		meth = clsHandler.getMethod("cleanContentType",
			new Class<?>[] { MimePart.class, String.class });
	    }
	} catch (ClassNotFoundException ex) {
	    // ignore it
	} catch (NoSuchMethodException ex) {
	    // ignore it
	} catch (RuntimeException ex) {
	    // ignore it
	} finally {
	    cleanContentType = meth;
	}
    }

    // No one should instantiate this class.
    private MimeUtil() {
    }

    /**
     * If a Content-Type handler has been specified,
     * call it to clean up the Content-Type value.
     *
     * @param	mp	the MimePart
     * @param	contentType	the Content-Type value
     * @return		the cleaned Content-Type value
     */
    public static String cleanContentType(MimePart mp, String contentType) {
	if (cleanContentType != null) {
	    try {
		return (String)cleanContentType.invoke(null,
					    new Object[] { mp, contentType });
	    } catch (Exception ex) {
		return contentType;
	    }
	} else
	    return contentType;
    }

    /**
     * Convenience method to get our context class loader.
     * Assert any privileges we might have and then call the
     * Thread.getContextClassLoader method.
     */
    private static ClassLoader getContextClassLoader() {
	return
	AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
	    @Override
	    public ClassLoader run() {
		ClassLoader cl = null;
		try {
		    cl = Thread.currentThread().getContextClassLoader();
		} catch (SecurityException ex) { }
		return cl;
	    }
	});
    }
}
