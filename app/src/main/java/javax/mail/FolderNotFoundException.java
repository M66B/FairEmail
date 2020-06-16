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

package javax.mail;

import java.lang.*;

/**
 * This exception is thrown by Folder methods, when those
 * methods are invoked on a non existent folder.
 *
 * @author John Mani
 */

public class FolderNotFoundException extends MessagingException {
    transient private Folder folder;

    private static final long serialVersionUID = 472612108891249403L;

    /**
     * Constructs a FolderNotFoundException with no detail message.
     */
    public FolderNotFoundException() {
	super();
    }

    /**
     * Constructs a FolderNotFoundException.
     *
     * @param folder	The Folder
     * @since		JavaMail 1.2 
     */
    public FolderNotFoundException(Folder folder) {
	super();
        this.folder = folder;
    }

    /**
     * Constructs a FolderNotFoundException with the specified
     * detail message.
     *
     * @param folder	The Folder
     * @param s		The detailed error message
     * @since		JavaMail 1.2
     */
    public FolderNotFoundException(Folder folder, String s) {
	super(s);
	this.folder = folder;
    }

    /**
     * Constructs a FolderNotFoundException with the specified
     * detail message and embedded exception.  The exception is chained
     * to this exception.
     *
     * @param folder	The Folder
     * @param s		The detailed error message
     * @param e		The embedded exception
     * @since		JavaMail 1.5
     */
    public FolderNotFoundException(Folder folder, String s, Exception e) {
	super(s, e);
	this.folder = folder;
    }

    /**
     * Constructs a FolderNotFoundException with the specified detail message
     * and the specified folder.
     *
     * @param s		The detail message
     * @param folder	The Folder
     */
    public FolderNotFoundException(String s, Folder folder) {
	super(s);
	this.folder = folder;
    }

    /**
     * Returns the offending Folder object.
     *
     * @return	the Folder object. Note that the returned value can be
     * 		<code>null</code>.
     */
    public Folder getFolder() {
	return folder;
    }
}
