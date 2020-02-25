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

package javax.mail.event;

import java.util.*;
import javax.mail.*;

/**
 * This class models Folder <em>existence</em> events. FolderEvents are
 * delivered to FolderListeners registered on the affected Folder as
 * well as the containing Store. <p>
 *
 * Service providers vary widely in their ability to notify clients of
 * these events.  At a minimum, service providers must notify listeners
 * registered on the same Store or Folder object on which the operation
 * occurs.  Service providers may also notify listeners when changes
 * are made through operations on other objects in the same virtual
 * machine, or by other clients in the same or other hosts.  Such
 * notifications are not required and are typically not supported
 * by mail protocols (including IMAP).
 *
 * @author John Mani
 * @author Bill Shannon
 */

public class FolderEvent extends MailEvent {

    /** The folder was created. */
    public static final int CREATED 		= 1;
    /** The folder was deleted. */
    public static final int DELETED 		= 2;
    /** The folder was renamed. */
    public static final int RENAMED 		= 3;

    /**
     * The event type.
     *
     * @serial
     */
    protected int type;

    /**
     * The folder the event occurred on.
     */
    transient protected Folder folder;

    /**
     * The folder that represents the new name, in case of a RENAMED event.
     *
     * @since	JavaMail 1.1
     */
    transient protected Folder newFolder;

    private static final long serialVersionUID = 5278131310563694307L;

    /**
     * Constructor. <p>
     *
     * @param source  	The source of the event
     * @param folder	The affected folder
     * @param type	The event type
     */
    public FolderEvent(Object source, Folder folder, int type) {
	this(source, folder, folder, type);
    }

    /**
     * Constructor. Use for RENAMED events.
     *
     * @param source  	The source of the event
     * @param oldFolder	The folder that is renamed
     * @param newFolder	The folder that represents the new name
     * @param type	The event type
     * @since		JavaMail 1.1
     */
    public FolderEvent(Object source, Folder oldFolder, 
		       Folder newFolder, int type) {
	super(source);
	this.folder = oldFolder;
	this.newFolder = newFolder;
	this.type = type;
    }

    /**
     * Return the type of this event.
     *
     * @return  type
     */
    public int getType() {
	return type;
    }

    /**
     * Return the affected folder.
     *
     * @return 		the affected folder
     * @see 		#getNewFolder
     */
    public Folder getFolder() {
	return folder;
    }

    /**
     * If this event indicates that a folder is renamed, (i.e, the event type
     * is RENAMED), then this method returns the Folder object representing the
     * new name. <p>
     *
     * The <code>getFolder()</code> method returns the folder that is renamed.
     *
     * @return		Folder representing the new name.
     * @see		#getFolder
     * @since		JavaMail 1.1
     */
    public Folder getNewFolder() {
	return newFolder;
    }

    /**
     * Invokes the appropriate FolderListener method
     */
    @Override
    public void dispatch(Object listener) {
	if (type == CREATED)
	    ((FolderListener)listener).folderCreated(this);
	else if (type == DELETED)
	    ((FolderListener)listener).folderDeleted(this);
	else if (type == RENAMED)
	    ((FolderListener)listener).folderRenamed(this);
    }
}
