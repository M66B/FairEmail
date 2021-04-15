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
import java.io.FilenameFilter;

public interface FileInterface {
    /**
     * Gets the name of the file. This method does not include the
     * directory.
     * @return the file name.
     */
    public String getName();

    /**
     * Gets the path of the file.
     * @return the file path.
     */
    public String getPath();

    /**
     * Gets the absolute path of the file.
     * @return the absolute file path.
     */
    public String getAbsolutePath();

    /**
     * Gets the official, canonical path of the File.
     * @return canonical path
     */
    // XXX - JDK1.1
    // public String getCanonicalPath();

    /**
     * Gets the name of the parent directory.
     * @return the parent directory, or null if one is not found.
     */
    public String getParent();

    /**
     * Returns a boolean indicating whether or not a file exists.
     */
    public boolean exists();

    /**
     * Returns a boolean indicating whether or not a writable file 
     * exists. 
     */
    public boolean canWrite();

    /**
     * Returns a boolean indicating whether or not a readable file 
     * exists.
     */
    public boolean canRead();

    /**
     * Returns a boolean indicating whether or not a normal file 
     * exists.
     */
    public boolean isFile();

    /**
     * Returns a boolean indicating whether or not a directory file 
     * exists.
     */
    public boolean isDirectory();

    /**
     * Returns a boolean indicating whether the file name is absolute.
     */
    public boolean isAbsolute();

    /**
     * Returns the last modification time. The return value should
     * only be used to compare modification dates. It is meaningless
     * as an absolute time.
     */
    public long lastModified();

    /**
     * Returns the length of the file. 
     */
    public long length();

    /**
     * Creates a directory and returns a boolean indicating the
     * success of the creation.  Will return false if the directory already
     * exists.
     */
    public boolean mkdir();

    /**
     * Renames a file and returns a boolean indicating whether 
     * or not this method was successful.
     * @param dest the new file name
     */
    public boolean renameTo(File dest);

    /**
     * Creates all directories in this path.  This method 
     * returns true if the target (deepest) directory was created,
     * false if the target directory was not created (e.g., if it
     * existed previously).
     */
    public boolean mkdirs();

    /**
     * Lists the files in a directory. Works only on directories.
     * @return an array of file names.  This list will include all
     * files in the directory except the equivalent of "." and ".." .
     */
    public String[] list();

    /**
     * Uses the specified filter to list files in a directory. 
     * @param filter the filter used to select file names
     * @return the filter selected files in this directory.
     * @see FilenameFilter
     */
    public String[] list(FilenameFilter filter);

    /**
     * Deletes the specified file. Returns true
     * if the file could be deleted.
     */
    public boolean delete();
}
