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
 * This exception is thrown when a method is invoked on a Messaging object
 * and the Folder that owns that object has died due to some reason. <p>
 *
 * Following the exception, the Folder is reset to the "closed" state. 
 * All messaging objects owned by the Folder should be considered invalid. 
 * The Folder can be reopened using the "open" method to reestablish the 
 * lost connection. <p>
 *
 * The getMessage() method returns more detailed information about the
 * error that caused this exception. <p>
 *
 * @author John Mani
 */

public class FolderClosedException extends MessagingException {
    transient private Folder folder;

    private static final long serialVersionUID = 1687879213433302315L;
    
    /**
     * Constructs a FolderClosedException.
     *
     * @param folder	The Folder
     */
    public FolderClosedException(Folder folder) {
	this(folder, null);
    }

    /**
     * Constructs a FolderClosedException with the specified
     * detail message.
     *
     * @param folder 	The Folder
     * @param message	The detailed error message
     */
    public FolderClosedException(Folder folder, String message) {
	super(message);
	this.folder = folder;
    }

    /**
     * Constructs a FolderClosedException with the specified
     * detail message and embedded exception.  The exception is chained
     * to this exception.
     *
     * @param folder 	The Folder
     * @param message	The detailed error message
     * @param e		The embedded exception
     * @since		JavaMail 1.5
     */
    public FolderClosedException(Folder folder, String message, Exception e) {
	super(message, e);
	this.folder = folder;
    }

    /**
     * Returns the dead Folder object
     *
     * @return	the dead Folder object
     */
    public Folder getFolder() {
	return folder;
    }
}
