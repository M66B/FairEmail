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

import java.io.*;
import java.net.*;
import java.util.*;
import javax.mail.event.*;

/**
 * An abstract class that models a message store and its
 * access protocol, for storing and retrieving messages. 
 * Subclasses provide actual implementations. <p>
 *
 * Note that <code>Store</code> extends the <code>Service</code>
 * class, which provides many common methods for naming stores,
 * connecting to stores, and listening to connection events.
 *
 * @author John Mani
 * @author Bill Shannon
 *
 * @see javax.mail.Service
 * @see javax.mail.event.ConnectionEvent
 * @see javax.mail.event.StoreEvent
 */

public abstract class Store extends Service {

    /**
     * Constructor.
     *
     * @param	session Session object for this Store.
     * @param	urlname	URLName object to be used for this Store
     */
    protected Store(Session session, URLName urlname) {
	super(session, urlname);
    }

    /**
     * Returns a Folder object that represents the 'root' of
     * the default namespace presented to the user by the Store.
     *
     * @return the root Folder
     * @exception	IllegalStateException if this Store is not connected.
     * @exception 	MessagingException for other failures
     */
    public abstract Folder getDefaultFolder() throws MessagingException;

    /**
     * Return the Folder object corresponding to the given name. Note
     * that a Folder object is returned even if the named folder does
     * not physically exist on the Store. The <code>exists()</code> 
     * method on the folder object indicates whether this folder really
     * exists. <p>
     *
     * Folder objects are not cached by the Store, so invoking this
     * method on the same name multiple times will return that many
     * distinct Folder objects.
     *
     * @param name 	The name of the Folder. In some Stores, name can
     *			be an absolute path if it starts with the
     *			hierarchy delimiter. Else it is interpreted
     *			relative to the 'root' of this namespace.
     * @return		Folder object
     * @exception 	IllegalStateException if this Store is not connected.
     * @exception 	MessagingException for other failures
     * @see 		Folder#exists
     * @see		Folder#create
     */
    public abstract Folder getFolder(String name)
			throws MessagingException;

    /**
     * Return a closed Folder object, corresponding to the given 
     * URLName. The store specified in the given URLName should
     * refer to this Store object. <p>
     *
     * Implementations of this method may obtain the name of the
     * actual folder using the <code>getFile()</code> method on
     * URLName, and use that name to create the folder.
     * 
     * @param url	URLName that denotes a folder
     * @return		Folder object
     * @exception 	IllegalStateException if this Store is not connected.
     * @exception 	MessagingException for other failures
     * @see 		URLName
     */
    public abstract Folder getFolder(URLName url)
			throws MessagingException;

    /**
     * Return a set of folders representing the <i>personal</i> namespaces
     * for the current user.  A personal namespace is a set of names that
     * is considered within the personal scope of the authenticated user.
     * Typically, only the authenticated user has access to mail folders
     * in their personal namespace.  If an INBOX exists for a user, it
     * must appear within the user's personal namespace.  In the
     * typical case, there should be only one personal namespace for each
     * user in each Store. <p>
     *
     * This implementation returns an array with a single entry containing
     * the return value of the <code>getDefaultFolder</code> method.
     * Subclasses should override this method to return appropriate information.
     *
     * @return		array of Folder objects
     * @exception 	IllegalStateException if this Store is not connected.
     * @exception 	MessagingException for other failures
     * @since		JavaMail 1.2
     */
    public Folder[] getPersonalNamespaces() throws MessagingException {
	return new Folder[] { getDefaultFolder() };
    }

    /**
     * Return a set of folders representing the namespaces for
     * <code>user</code>.  The namespaces returned represent the
     * personal namespaces for the user.  To access mail folders in the
     * other user's namespace, the currently authenticated user must be
     * explicitly granted access rights.  For example, it is common for
     * a manager to grant to their secretary access rights to their
     * mail folders. <p>
     *
     * This implementation returns an empty array.  Subclasses should
     * override this method to return appropriate information.
     *
     * @param	user	the user name
     * @return		array of Folder objects
     * @exception 	IllegalStateException if this Store is not connected.
     * @exception 	MessagingException for other failures
     * @since		JavaMail 1.2
     */
    public Folder[] getUserNamespaces(String user)
				throws MessagingException {
	return new Folder[0];
    }

    /**
     * Return a set of folders representing the <i>shared</i> namespaces.
     * A shared namespace is a namespace that consists of mail folders
     * that are intended to be shared amongst users and do not exist
     * within a user's personal namespace. <p>
     *
     * This implementation returns an empty array.  Subclasses should
     * override this method to return appropriate information.
     *
     * @exception 	IllegalStateException if this Store is not connected.
     * @exception 	MessagingException for other failures
     * @return		array of Folder objects
     * @since		JavaMail 1.2
     */
    public Folder[] getSharedNamespaces() throws MessagingException {
	return new Folder[0];
    }

    // Vector of Store listeners
    private volatile Vector<StoreListener> storeListeners = null;

    /**
     * Add a listener for StoreEvents on this Store. <p>
     *
     * The default implementation provided here adds this listener
     * to an internal list of StoreListeners.
     *
     * @param l         the Listener for Store events
     * @see             javax.mail.event.StoreEvent
     */
    public synchronized void addStoreListener(StoreListener l) {
	if (storeListeners == null)
	    storeListeners = new Vector<>();
	storeListeners.addElement(l);
    }

    /**
     * Remove a listener for Store events. <p>
     *
     * The default implementation provided here removes this listener
     * from the internal list of StoreListeners.
     *
     * @param l         the listener
     * @see             #addStoreListener
     */
    public synchronized void removeStoreListener(StoreListener l) {
	if (storeListeners != null)
	    storeListeners.removeElement(l);
    }

    /**
     * Notify all StoreListeners. Store implementations are
     * expected to use this method to broadcast StoreEvents. <p>
     *
     * The provided default implementation queues the event into
     * an internal event queue. An event dispatcher thread dequeues
     * events from the queue and dispatches them to the registered
     * StoreListeners. Note that the event dispatching occurs
     * in a separate thread, thus avoiding potential deadlock problems.
     *
     * @param	type	the StoreEvent type
     * @param	message	a message for the StoreEvent
     */
    protected void notifyStoreListeners(int type, String message) {
   	if (storeListeners == null)
	    return;
	
	StoreEvent e = new StoreEvent(this, type, message);
	queueEvent(e, storeListeners);
    }

    // Vector of folder listeners
    private volatile Vector<FolderListener> folderListeners = null;

    /**
     * Add a listener for Folder events on any Folder object 
     * obtained from this Store. FolderEvents are delivered to
     * FolderListeners on the affected Folder as well as to 
     * FolderListeners on the containing Store. <p>
     *
     * The default implementation provided here adds this listener
     * to an internal list of FolderListeners.
     *
     * @param l         the Listener for Folder events
     * @see             javax.mail.event.FolderEvent
     */
    public synchronized void addFolderListener(FolderListener l) {
   	if (folderListeners == null)
	    folderListeners = new Vector<>();
	folderListeners.addElement(l);
    }

    /**
     * Remove a listener for Folder events. <p>
     *
     * The default implementation provided here removes this listener
     * from the internal list of FolderListeners.
     *
     * @param l         the listener
     * @see             #addFolderListener
     */
    public synchronized void removeFolderListener(FolderListener l) {
   	if (folderListeners != null)
	    folderListeners.removeElement(l);
    }

    /**
     * Notify all FolderListeners. Store implementations are
     * expected to use this method to broadcast Folder events. <p>
     *
     * The provided default implementation queues the event into
     * an internal event queue. An event dispatcher thread dequeues
     * events from the queue and dispatches them to the registered
     * FolderListeners. Note that the event dispatching occurs
     * in a separate thread, thus avoiding potential deadlock problems.
     *
     * @param	type	type of FolderEvent
     * @param	folder	affected Folder
     * @see		#notifyFolderRenamedListeners
     */
    protected void notifyFolderListeners(int type, Folder folder) {
   	if (folderListeners == null) 
	    return;
	
	FolderEvent e = new FolderEvent(this, folder, type);
	queueEvent(e, folderListeners);
    }

    /**
     * Notify all FolderListeners about the renaming of a folder.
     * Store implementations are expected to use this method to broadcast 
     * Folder events indicating the renaming of folders. <p>
     *
     * The provided default implementation queues the event into
     * an internal event queue. An event dispatcher thread dequeues
     * events from the queue and dispatches them to the registered
     * FolderListeners. Note that the event dispatching occurs
     * in a separate thread, thus avoiding potential deadlock problems.
     *
     * @param	oldF	the folder being renamed
     * @param	newF	the folder representing the new name.
     * @since	JavaMail 1.1
     */
    protected void notifyFolderRenamedListeners(Folder oldF, Folder newF) {
   	if (folderListeners == null) 
	    return;
	
	FolderEvent e = new FolderEvent(this, oldF, newF,FolderEvent.RENAMED);
	queueEvent(e, folderListeners);
    }
}
