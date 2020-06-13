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

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The FileTypeMap is an abstract class that provides a data typing
 * interface for files. Implementations of this class will
 * implement the getContentType methods which will derive a content
 * type from a file name or a File object. FileTypeMaps could use any
 * scheme to determine the data type, from examining the file extension
 * of a file (like the MimetypesFileTypeMap) to opening the file and
 * trying to derive its type from the contents of the file. The
 * FileDataSource class uses the default FileTypeMap (a MimetypesFileTypeMap
 * unless changed) to determine the content type of files.
 *
 * @see javax.activation.FileTypeMap
 * @see javax.activation.FileDataSource
 * @see javax.activation.MimetypesFileTypeMap
 */

public abstract class FileTypeMap {

    private static FileTypeMap defaultMap = null;
    private static Map<ClassLoader,FileTypeMap> map =
				new WeakHashMap<ClassLoader,FileTypeMap>();

    /**
     * The default constructor.
     */
    public FileTypeMap() {
	super();
    }

    /**
     * Return the type of the file object. This method should
     * always return a valid MIME type.
     *
     * @param file A file to be typed.
     * @return The content type.
     */
    abstract public String getContentType(File file);

    /**
     * Return the type of the file passed in.  This method should
     * always return a valid MIME type.
     *
     * @param filename the pathname of the file.
     * @return The content type.
     */
    abstract public String getContentType(String filename);

    /**
     * Sets the default FileTypeMap for the system. This instance
     * will be returned to callers of getDefaultFileTypeMap.
     *
     * @param fileTypeMap The FileTypeMap.
     * @exception SecurityException if the caller doesn't have permission
     *					to change the default
     */
    public static synchronized void setDefaultFileTypeMap(FileTypeMap fileTypeMap) {
	SecurityManager security = System.getSecurityManager();
	if (security != null) {
	    try {
		// if it's ok with the SecurityManager, it's ok with me...
		security.checkSetFactory();
	    } catch (SecurityException ex) {
		// otherwise, we also allow it if this code and the
		// factory come from the same (non-system) class loader (e.g.,
		// the JAF classes were loaded with the applet classes).
		ClassLoader cl = FileTypeMap.class.getClassLoader();
		if (cl == null || cl.getParent() == null ||
		    cl != fileTypeMap.getClass().getClassLoader())
		    throw ex;
	    }
	}
	// remove any per-thread-context-class-loader FileTypeMap
	map.remove(SecuritySupport.getContextClassLoader());
	defaultMap = fileTypeMap;	
    }

    /**
     * Return the default FileTypeMap for the system.
     * If setDefaultFileTypeMap was called, return
     * that instance, otherwise return an instance of
     * <code>MimetypesFileTypeMap</code>.
     *
     * @return The default FileTypeMap
     * @see javax.activation.FileTypeMap#setDefaultFileTypeMap
     */
    public static synchronized FileTypeMap getDefaultFileTypeMap() {
	if (defaultMap != null)
	    return defaultMap;

	// fetch per-thread-context-class-loader default
	ClassLoader tccl = SecuritySupport.getContextClassLoader();
	FileTypeMap def = map.get(tccl);
	if (def == null) {
	    def = new MimetypesFileTypeMap();
	    map.put(tccl, def);
	}
	return def;
    }
}
