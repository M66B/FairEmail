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

/**
 * This exception is thrown when an attempt is made to open a folder
 * read-write access when the folder is marked read-only. <p>
 *
 * The getMessage() method returns more detailed information about the
 * error that caused this exception. <p>
 *
 * @author Jim Glennon
 */

public class ReadOnlyFolderException extends MessagingException {
    transient private Folder folder;

    private static final long serialVersionUID = 5711829372799039325L;
    
    /**
     * Constructs a ReadOnlyFolderException with the specified
     * folder and no detail message.
     *
     * @param folder	the Folder
     * @since 		JavaMail 1.2
     */
    public ReadOnlyFolderException(Folder folder) {
	this(folder, null);
    }

    /**
     * Constructs a ReadOnlyFolderException with the specified
     * detail message.
     *
     * @param folder 	The Folder
     * @param message	The detailed error message
     * @since 		JavaMail 1.2
     */
    public ReadOnlyFolderException(Folder folder, String message) {
	super(message);
	this.folder = folder;
    }

    /**
     * Constructs a ReadOnlyFolderException with the specified
     * detail message and embedded exception.  The exception is chained
     * to this exception.
     *
     * @param folder 	The Folder
     * @param message	The detailed error message
     * @param e		The embedded exception
     * @since		JavaMail 1.5
     */
    public ReadOnlyFolderException(Folder folder, String message, Exception e) {
	super(message, e);
	this.folder = folder;
    }

    /**
     * Returns the Folder object.
     *
     * @return	the Folder
     * @since 		JavaMail 1.2
     */
    public Folder getFolder() {
	return folder;
    }
}
