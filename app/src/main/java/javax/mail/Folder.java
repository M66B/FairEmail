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
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executor;
import javax.mail.search.SearchTerm;
import javax.mail.event.*;

/**
 * Folder is an abstract class that represents a folder for mail
 * messages. Subclasses implement protocol specific Folders.<p>
 *
 * Folders can contain Messages, other Folders or both, thus providing
 * a tree-like hierarchy rooted at the Store's default folder. (Note 
 * that some Folder implementations may not allow both Messages and 
 * other Folders in the same Folder).<p>
 *
 * The interpretation of folder names is implementation dependent.
 * The different levels of hierarchy in a folder's full name
 * are separated from each other by the hierarchy delimiter 
 * character.<p>
 *
 * The case-insensitive full folder name (that is, the full name
 * relative to the default folder for a Store) <strong>INBOX</strong>
 * is reserved to mean the "primary folder for this user on this
 * server".  Not all Stores will provide an INBOX folder, and not
 * all users will have an INBOX folder at all times.  The name
 * <strong>INBOX</strong> is reserved to refer to this folder,
 * when it exists, in Stores that provide it. <p>
 *
 * A Folder object obtained from a Store need not actually exist
 * in the backend store. The <code>exists</code> method tests whether
 * the folder exists or not. The <code>create</code> method
 * creates a Folder. <p>
 *
 * A Folder is initially in the closed state. Certain methods are valid
 * in this state; the documentation for those methods note this.  A
 * Folder is opened by calling its 'open' method. All Folder methods,
 * except <code>open</code>, <code>delete</code> and 
 * <code>renameTo</code>, are valid in this state. <p>
 *
 * The only way to get a Folder is by invoking the 
 * <code>getFolder</code> method on Store, Folder, or Session, or by invoking 
 * the <code>list</code> or <code>listSubscribed</code> methods 
 * on Folder. Folder objects returned by the above methods are not 
 * cached by the Store. Thus, invoking the <code>getFolder</code> method
 * with the same folder name multiple times will return distinct Folder 
 * objects.  Likewise for the <code>list</code> and <code>listSubscribed</code>
 * methods. <p>
 *
 * The Message objects within the Folder are cached by the Folder.
 * Thus, invoking <code>getMessage(msgno)</code> on the same message number
 * multiple times will return the same Message object, until an 
 * expunge is done on this Folder. <p>
 *
 * Message objects from a Folder are only valid while a Folder is open
 * and should not be accessed after the Folder is closed, even if the
 * Folder is subsequently reopened.  Instead, new Message objects must
 * be fetched from the Folder after the Folder is reopened. <p>
 *
 * Note that a Message's message number can change within a
 * session if the containing Folder is expunged using the expunge
 * method.  Clients that use message numbers as references to messages
 * should be aware of this and should be prepared to deal with this
 * situation (probably by flushing out existing message number references
 * and reloading them). Because of this complexity, it is better for
 * clients to use Message objects as references to messages, rather than
 * message numbers. Expunged Message objects still have to be
 * pruned, but other Message objects in that folder are not affected by the 
 * expunge.
 *
 * @author John Mani
 * @author Bill Shannon
 */

public abstract class Folder implements AutoCloseable {

    /**
     * The parent store.
     */
    protected Store store;

    /**
     * The open mode of this folder.  The open mode is
     * <code>Folder.READ_ONLY</code>, <code>Folder.READ_WRITE</code>,
     * or -1 if not known.
     * @since	JavaMail 1.1
     */
    protected int mode = -1;

    /*
     * The queue of events to be delivered.
     */
    private final EventQueue q;

    /**
     * Constructor that takes a Store object.
     *
     * @param store the Store that holds this folder
     */
    protected Folder(Store store) {
	this.store = store;

	// create or choose the appropriate event queue
	Session session = store.getSession();
	String scope =
	    session.getProperties().getProperty("mail.event.scope", "folder");
	Executor executor =
		(Executor)session.getProperties().get("mail.event.executor");
	if (scope.equalsIgnoreCase("application"))
	    q = EventQueue.getApplicationEventQueue(executor);
	else if (scope.equalsIgnoreCase("session"))
	    q = session.getEventQueue();
	else if (scope.equalsIgnoreCase("store"))
	    q = store.getEventQueue();
	else // if (scope.equalsIgnoreCase("folder"))
	    q = new EventQueue(executor);
    }

    /**
     * Returns the name of this Folder. <p>
     *
     * This method can be invoked on a closed Folder.
     *
     * @return		name of the Folder
     */
    public abstract String getName();

    /**
     * Returns the full name of this Folder. If the folder resides under
     * the root hierarchy of this Store, the returned name is relative
     * to the root. Otherwise an absolute name, starting with the 
     * hierarchy delimiter, is returned. <p>
     *
     * This method can be invoked on a closed Folder.
     *
     * @return		full name of the Folder
     */
    public abstract String getFullName();

    /**
     * Return a URLName representing this folder.  The returned URLName
     * does <em>not</em> include the password used to access the store.
     *
     * @return	the URLName representing this folder
     * @exception	MessagingException for failures
     * @see	URLName
     * @since	JavaMail 1.1
     */
    public URLName getURLName() throws MessagingException {
	URLName storeURL = getStore().getURLName();
	String fullname = getFullName();
	StringBuilder encodedName = new StringBuilder();

	if (fullname != null) {
	    /*
	    // We need to encode each of the folder's names.
	    char separator = getSeparator();
	    StringTokenizer tok = new StringTokenizer(
		fullname, new Character(separator).toString(), true);

	    while (tok.hasMoreTokens()) {
		String s = tok.nextToken();
		if (s.charAt(0) == separator)
		    encodedName.append(separator);
		else
		    // XXX - should encode, but since there's no decoder...
		    //encodedName.append(java.net.URLEncoder.encode(s));
		    encodedName.append(s);
	    }
	    */
	    // append the whole thing, until we can encode
	    encodedName.append(fullname);
	}

	/*
	 * Sure would be convenient if URLName had a
	 * constructor that took a base URLName.
	 */
	return new URLName(storeURL.getProtocol(), storeURL.getHost(),
			    storeURL.getPort(), encodedName.toString(),
			    storeURL.getUsername(),
			    null /* no password */);
    }

    /**
     * Returns the Store that owns this Folder object.
     * This method can be invoked on a closed Folder.
     *
     * @return 		the Store
     */
    public Store getStore() {
	return store;
    }

    /**
     * Returns the parent folder of this folder.
     * This method can be invoked on a closed Folder. If this folder
     * is the top of a folder hierarchy, this method returns null. <p>
     *
     * Note that since Folder objects are not cached, invoking this method
     * returns a new distinct Folder object.
     *
     * @return		Parent folder
     * @exception	MessagingException for failures
     */
    public abstract Folder getParent() throws MessagingException;

    /**
     * Tests if this folder physically exists on the Store.
     * This method can be invoked on a closed Folder.
     *
     * @return true if the folder exists, otherwise false
     * @see    #create
     * @exception	MessagingException typically if the connection 
     *			to the server is lost.
     */
    public abstract boolean exists() throws MessagingException;

    /**
     * Returns a list of Folders belonging to this Folder's namespace
     * that match the specified pattern. Patterns may contain the wildcard
     * characters <code>"%"</code>, which matches any character except hierarchy
     * delimiters, and <code>"*"</code>, which matches any character. <p>
     *
     * As an example, given the folder hierarchy: <pre>
     *    Personal/
     *       Finance/
     *          Stocks
     *          Bonus
     *          StockOptions
     *       Jokes
     * </pre>
     * <code>list("*")</code> on "Personal" will return the whole 
     * hierarchy. <br>
     * <code>list("%")</code> on "Personal" will return "Finance" and 
     * "Jokes". <br>
     * <code>list("Jokes")</code> on "Personal" will return "Jokes".<br>
     * <code>list("Stock*")</code> on "Finance" will return "Stocks"
     * and "StockOptions". <p>
     *
     * Folder objects are not cached by the Store, so invoking this
     * method on the same pattern multiple times will return that many
     * distinct Folder objects. <p>
     *
     * This method can be invoked on a closed Folder.
     *
     * @param pattern	the match pattern
     * @return		array of matching Folder objects. An empty
     *			array is returned if no matching Folders exist.
     * @see 		#listSubscribed
     * @exception 	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception 	MessagingException for other failures
     */
    public abstract Folder[] list(String pattern) throws MessagingException;

    /**
     * Returns a list of subscribed Folders belonging to this Folder's
     * namespace that match the specified pattern. If the folder does
     * not support subscription, this method should resolve to
     * <code>list</code>.
     * (The default implementation provided here, does just this).
     * The pattern can contain wildcards as for <code>list</code>. <p>
     *
     * Note that, at a given level of the folder hierarchy, a particular
     * folder may not be subscribed, but folders underneath that folder
     * in the folder hierarchy may be subscribed.  In order to allow
     * walking the folder hierarchy, such unsubscribed folders may be
     * returned, indicating that a folder lower in the hierarchy is
     * subscribed.  The <code>isSubscribed</code> method on a folder will
     * tell whether any particular folder is actually subscribed. <p>
     *
     * Folder objects are not cached by the Store, so invoking this
     * method on the same pattern multiple times will return that many
     * distinct Folder objects. <p>
     *
     * This method can be invoked on a closed Folder.
     *
     * @param pattern	the match pattern
     * @return		array of matching subscribed Folder objects. An
     *			empty array is returned if no matching
     *			subscribed folders exist.
     * @see 		#list
     * @exception 	FolderNotFoundException if this folder does
     *			not exist.
     * @exception 	MessagingException for other failures
     */
    public Folder[] listSubscribed(String pattern) throws MessagingException {
	return list(pattern);
    }

    /**
     * Convenience method that returns the list of folders under this
     * Folder. This method just calls the <code>list(String pattern)</code>
     * method with <code>"%"</code> as the match pattern. This method can
     * be invoked on a closed Folder.
     *
     * @return		array of Folder objects under this Folder. An
     *			empty array is returned if no subfolders exist.
     * @see		#list
     * @exception 	FolderNotFoundException if this folder does
     *			not exist.
     * @exception 	MessagingException for other failures
     */

    public Folder[] list() throws MessagingException {
	return list("%");
    }

    /**
     * Convenience method that returns the list of subscribed folders 
     * under this Folder. This method just calls the
     * <code>listSubscribed(String pattern)</code> method with <code>"%"</code>
     * as the match pattern. This method can be invoked on a closed Folder.
     *
     * @return		array of subscribed Folder objects under this 
     *			Folder. An empty array is returned if no subscribed 
     *			subfolders exist.
     * @see		#listSubscribed
     * @exception 	FolderNotFoundException if this folder does
     *			not exist.
     * @exception 	MessagingException for other failures
     */
    public Folder[] listSubscribed() throws MessagingException {
	return listSubscribed("%");
    }

    /**
     * Return the delimiter character that separates this Folder's pathname
     * from the names of immediate subfolders. This method can be invoked 
     * on a closed Folder.
     *
     * @exception 	FolderNotFoundException if the implementation
     *			requires the folder to exist, but it does not
     * @return          Hierarchy separator character
     */
    public abstract char getSeparator() throws MessagingException;

    /**
     * This folder can contain messages
     */
    public final static int HOLDS_MESSAGES = 0x01;

    /**
     * This folder can contain other folders
     */
    public final static int HOLDS_FOLDERS  = 0x02;

    /**
     * Returns the type of this Folder, that is, whether this folder can hold
     * messages or subfolders or both. The returned value is an integer
     * bitfield with the appropriate bits set. This method can be invoked
     * on a closed folder.
     * 
     * @return 		integer with appropriate bits set
     * @exception 	FolderNotFoundException if this folder does 
     *			not exist.
     * @see 		#HOLDS_FOLDERS 
     * @see		#HOLDS_MESSAGES
     */
    public abstract int getType() throws MessagingException; 

    /**
     * Create this folder on the Store. When this folder is created, any
     * folders in its path that do not exist are also created. <p>
     *
     * If the creation is successful, a CREATED FolderEvent is delivered
     * to any FolderListeners registered on this Folder and this Store.
     *
     * @param  type	The type of this folder. 
     *
     * @return		true if the creation succeeds, else false.
     * @exception 	MessagingException for failures
     * @see 		#HOLDS_FOLDERS
     * @see		#HOLDS_MESSAGES
     * @see		javax.mail.event.FolderEvent
     */
    public abstract boolean create(int type) throws MessagingException;

    /**
     * Returns true if this Folder is subscribed. <p>
     *
     * This method can be invoked on a closed Folder. <p>
     *
     * The default implementation provided here just returns true.
     *
     * @return		true if this Folder is subscribed
     */
    public boolean isSubscribed() {
	return true;
    }

    /**
     * Subscribe or unsubscribe this Folder. Not all Stores support
     * subscription. <p>
     *
     * This method can be invoked on a closed Folder. <p>
     *
     * The implementation provided here just throws the
     * MethodNotSupportedException.
     *
     * @param subscribe	true to subscribe, false to unsubscribe
     * @exception 	FolderNotFoundException if this folder does
     *			not exist.
     * @exception 	MethodNotSupportedException if this store
     *			does not support subscription
     * @exception 	MessagingException for other failures
     */
    public void setSubscribed(boolean subscribe) 
			throws MessagingException {
	throw new MethodNotSupportedException();
    }

    /**
     * Returns true if this Folder has new messages since the last time
     * this indication was reset.  When this indication is set or reset
     * depends on the Folder implementation (and in the case of IMAP,
     * depends on the server).  This method can be used to implement
     * a lightweight "check for new mail" operation on a Folder without
     * opening it.  (For example, a thread that monitors a mailbox and
     * flags when it has new mail.)  This method should indicate whether
     * any messages in the Folder have the <code>RECENT</code> flag set. <p>
     *
     * Note that this is not an incremental check for new mail, i.e.,
     * it cannot be used to determine whether any new messages have
     * arrived since the last time this method was invoked. To
     * implement incremental checks, the Folder needs to be opened. <p>
     *
     * This method can be invoked on a closed Folder that can contain
     * Messages.
     *
     * @return		true if the Store has new Messages
     * @exception	FolderNotFoundException if this folder does
     *			not exist.
     * @exception 	MessagingException for other failures
     */
    public abstract boolean hasNewMessages() throws MessagingException;

    /**
     * Return the Folder object corresponding to the given name. Note that
     * this folder does not physically have to exist in the Store. The
     * <code>exists()</code> method on a Folder indicates whether it really
     * exists on the Store. <p>
     *
     * In some Stores, name can be an absolute path if it starts with the
     * hierarchy delimiter.  Otherwise, it is interpreted relative to
     * this Folder. <p>
     *
     * Folder objects are not cached by the Store, so invoking this
     * method on the same name multiple times will return that many
     * distinct Folder objects. <p>
     *
     * This method can be invoked on a closed Folder.
     *
     * @param name 	name of the Folder
     * @return		Folder object
     * @exception 	MessagingException for failures
     */
    public abstract Folder getFolder(String name)
				throws MessagingException;

    /**
     * Delete this Folder. This method will succeed only on a closed
     * Folder. <p>
     *
     * The <code>recurse</code> flag controls whether the deletion affects
     * subfolders or not. If true, all subfolders are deleted, then this
     * folder itself is deleted. If false, the behaviour is dependent on
     * the folder type and is elaborated below:
     *
     * <ul>
     * <li>
     * The folder can contain only messages: (type == HOLDS_MESSAGES).
     * <br>
     * All messages within the folder are removed. The folder 
     * itself is then removed. An appropriate FolderEvent is generated by 
     * the Store and this folder.
     *
     * <li>
     * The folder can contain only subfolders: (type == HOLDS_FOLDERS).
     * <br>
     * If this folder is empty (does not contain any 
     * subfolders at all), it is removed. An appropriate FolderEvent is 
     * generated by the Store and this folder.<br>
     * If this folder contains any subfolders, the delete fails 
     * and returns false.
     *
     * <li>
     * The folder can contain subfolders as well as messages: <br>
     * If the folder is empty (no messages or subfolders), it
     * is removed. If the folder contains no subfolders, but only messages,
     * then all messages are removed. The folder itself is then removed.
     * In both the above cases, an appropriate FolderEvent is
     * generated by the Store and this folder. <p>
     *
     * If the folder contains subfolders there are 3 possible
     * choices an implementation is free to do:
     * 
     *  <ol>
     *   <li> The operation fails, irrespective of whether this folder
     * contains messages or not. Some implementations might elect to go
     * with this simple approach. The delete() method returns false.
     *
     *   <li> Any messages within the folder are removed. Subfolders
     * are not removed. The folder itself is not removed or affected
     * in any manner. The delete() method returns true. And the 
     * exists() method on this folder will return true indicating that
     * this folder still exists. <br>
     * An appropriate FolderEvent is generated by the Store and this folder.
     *
     *   <li> Any messages within the folder are removed. Subfolders are
     * not removed. The folder itself changes its type from 
     * HOLDS_FOLDERS | HOLDS_MESSAGES to HOLDS_FOLDERS. Thus new 
     * messages cannot be added to this folder, but new subfolders can
     * be created underneath. The delete() method returns true indicating
     * success. The exists() method on this folder will return true
     * indicating that this folder still exists. <br>
     * An appropriate FolderEvent is generated by the Store and this folder.
     * </ol>
     * </ul>
     *
     * @param	recurse	also delete subfolders?
     * @return		true if the Folder is deleted successfully
     * @exception	FolderNotFoundException if this folder does 
     *			not exist
     * @exception	IllegalStateException if this folder is not in 
     *			the closed state.
     * @exception       MessagingException for other failures
     * @see		javax.mail.event.FolderEvent
     */
    public abstract boolean delete(boolean recurse) 
				throws MessagingException;

    /**
     * Rename this Folder. This method will succeed only on a closed
     * Folder. <p>
     *
     * If the rename is successful, a RENAMED FolderEvent is delivered
     * to FolderListeners registered on this folder and its containing
     * Store.
     *
     * @param f		a folder representing the new name for this Folder
     * @return		true if the Folder is renamed successfully
     * @exception	FolderNotFoundException if this folder does 
     *			not exist
     * @exception	IllegalStateException if this folder is not in 
     *			the closed state.
     * @exception       MessagingException for other failures
     * @see		javax.mail.event.FolderEvent
     */
    public abstract boolean renameTo(Folder f) throws MessagingException;

    /**
     * The Folder is read only.  The state and contents of this
     * folder cannot be modified.
     */
    public static final int READ_ONLY 	= 1;

    /**
     * The state and contents of this folder can be modified.
     */
    public static final int READ_WRITE 	= 2;

    /**
     * Open this Folder. This method is valid only on Folders that
     * can contain Messages and that are closed. <p>
     *
     * If this folder is opened successfully, an OPENED ConnectionEvent
     * is delivered to any ConnectionListeners registered on this 
     * Folder. <p>
     *
     * The effect of opening multiple connections to the same folder
     * on a specifc Store is implementation dependent. Some implementations
     * allow multiple readers, but only one writer. Others allow
     * multiple writers as well as readers.
     *
     * @param mode	open the Folder READ_ONLY or READ_WRITE
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception	IllegalStateException if this folder is not in 
     *			the closed state.
     * @exception       MessagingException for other failures
     * @see 		#READ_ONLY
     * @see 		#READ_WRITE
     * @see 		#getType()
     * @see 		javax.mail.event.ConnectionEvent
     */
    public abstract void open(int mode) throws MessagingException;

    /**
     * Close this Folder. This method is valid only on open Folders. <p>
     *
     * A CLOSED ConnectionEvent is delivered to any ConnectionListeners
     * registered on this Folder. Note that the folder is closed even
     * if this method terminates abnormally by throwing a
     * MessagingException.
     *
     * @param expunge	expunges all deleted messages if this flag is true
     * @exception	IllegalStateException if this folder is not opened
     * @exception       MessagingException for other failures
     * @see 		javax.mail.event.ConnectionEvent
     */
    public abstract void close(boolean expunge) throws MessagingException;

    /**
     * Close this Folder and expunge deleted messages. <p>
     *
     * A CLOSED ConnectionEvent is delivered to any ConnectionListeners
     * registered on this Folder. Note that the folder is closed even
     * if this method terminates abnormally by throwing a
     * MessagingException. <p>
     *
     * This method supports the {@link java.lang.AutoCloseable AutoCloseable}
     * interface. <p>
     *
     * This implementation calls <code>close(true)</code>.
     *
     * @exception	IllegalStateException if this folder is not opened
     * @exception       MessagingException for other failures
     * @see 		javax.mail.event.ConnectionEvent
     * @since		JavaMail 1.6
     */
    @Override
    public void close() throws MessagingException {
	close(true);
    }

    /**
     * Indicates whether this Folder is in the 'open' state.
     * @return  true if this Folder is in the 'open' state.
     */
    public abstract boolean isOpen();

    /**
     * Return the open mode of this folder.  Returns
     * <code>Folder.READ_ONLY</code>, <code>Folder.READ_WRITE</code>,
     * or -1 if the open mode is not known (usually only because an older
     * <code>Folder</code> provider has not been updated to use this new
     * method).
     *
     * @exception	IllegalStateException if this folder is not opened
     * @return	        the open mode of this folder
     * @since		JavaMail 1.1
     */
    public synchronized int getMode() {
	if (!isOpen())
	    throw new IllegalStateException("Folder not open");
	return mode;
    }
 
    /**
     * Get the permanent flags supported by this Folder. Returns a Flags
     * object that contains all the flags supported. <p>
     *
     * The special flag <code>Flags.Flag.USER </code> indicates that this Folder
     * supports arbitrary user-defined flags. <p>
     *
     * The supported permanent flags for a folder may not be available
     * until the folder is opened.
     * 
     * @return 		permanent flags, or null if not known
     */
    public abstract Flags getPermanentFlags();

    /**
     * Get total number of messages in this Folder. <p>
     *
     * This method can be invoked on a closed folder. However, note
     * that for some folder implementations, getting the total message
     * count can be an expensive operation involving actually opening 
     * the folder. In such cases, a provider can choose not to support 
     * this functionality in the closed state, in which case this method
     * must return -1. <p>
     *
     * Clients invoking this method on a closed folder must be aware
     * that this is a potentially expensive operation. Clients must
     * also be prepared to handle a return value of -1 in this case.
     * 
     * @return 		total number of messages. -1 may be returned
     *			by certain implementations if this method is
     *			invoked on a closed folder.
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception       MessagingException for other failures
     */
    public abstract int getMessageCount() throws MessagingException;

    /**
     * Get the number of new messages in this Folder. <p>
     *
     * This method can be invoked on a closed folder. However, note
     * that for some folder implementations, getting the new message
     * count can be an expensive operation involving actually opening 
     * the folder. In such cases, a provider can choose not to support 
     * this functionality in the closed state, in which case this method
     * must return -1. <p>
     *
     * Clients invoking this method on a closed folder must be aware
     * that this is a potentially expensive operation. Clients must
     * also be prepared to handle a return value of -1 in this case. <p>
     *
     * This implementation returns -1 if this folder is closed. Else
     * this implementation gets each Message in the folder using
     * <code>getMessage(int)</code> and checks whether its
     * <code>RECENT</code> flag is set. The total number of messages
     * that have this flag set is returned.
     *
     * @return 		number of new messages. -1 may be returned
     *			by certain implementations if this method is
     *			invoked on a closed folder.
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception       MessagingException for other failures
     */
    public synchronized int getNewMessageCount() 
			throws MessagingException {
	if (!isOpen())
	    return -1;

	int newmsgs = 0;
	int total = getMessageCount();
	for (int i = 1; i <= total; i++) {
	    try {
		if (getMessage(i).isSet(Flags.Flag.RECENT))
		    newmsgs++;
	    } catch (MessageRemovedException me) {
		// This is an expunged message, ignore it.
		continue;
	    }
	}
	return newmsgs;
    }

    /**
     * Get the total number of unread messages in this Folder. <p>
     *
     * This method can be invoked on a closed folder. However, note
     * that for some folder implementations, getting the unread message
     * count can be an expensive operation involving actually opening 
     * the folder. In such cases, a provider can choose not to support 
     * this functionality in the closed state, in which case this method
     * must return -1. <p>
     *
     * Clients invoking this method on a closed folder must be aware
     * that this is a potentially expensive operation. Clients must
     * also be prepared to handle a return value of -1 in this case. <p>
     *
     * This implementation returns -1 if this folder is closed. Else
     * this implementation gets each Message in the folder using
     * <code>getMessage(int)</code> and checks whether its
     * <code>SEEN</code> flag is set. The total number of messages
     * that do not have this flag set is returned.
     *
     * @return 		total number of unread messages. -1 may be returned
     *			by certain implementations if this method is
     *			invoked on a closed folder.
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception       MessagingException for other failures
     */
    public synchronized int getUnreadMessageCount() 
			throws MessagingException {
	if (!isOpen())
	    return -1;

	int unread = 0;
	int total = getMessageCount();
	for (int i = 1; i <= total; i++) {
	    try {
		if (!getMessage(i).isSet(Flags.Flag.SEEN))
		    unread++;
	    } catch (MessageRemovedException me) {
		// This is an expunged message, ignore it.
		continue;
	    }
	}
	return unread;
    }

    /**
     * Get the number of deleted messages in this Folder. <p>
     *
     * This method can be invoked on a closed folder. However, note
     * that for some folder implementations, getting the deleted message
     * count can be an expensive operation involving actually opening 
     * the folder. In such cases, a provider can choose not to support 
     * this functionality in the closed state, in which case this method
     * must return -1. <p>
     *
     * Clients invoking this method on a closed folder must be aware
     * that this is a potentially expensive operation. Clients must
     * also be prepared to handle a return value of -1 in this case. <p>
     *
     * This implementation returns -1 if this folder is closed. Else
     * this implementation gets each Message in the folder using
     * <code>getMessage(int)</code> and checks whether its
     * <code>DELETED</code> flag is set. The total number of messages
     * that have this flag set is returned.
     *
     * @return 		number of deleted messages. -1 may be returned
     *			by certain implementations if this method is
     *			invoked on a closed folder.
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception       MessagingException for other failures
     * @since		JavaMail 1.3
     */
    public synchronized int getDeletedMessageCount() throws MessagingException {
	if (!isOpen())
	    return -1;

	int deleted = 0;
	int total = getMessageCount();
	for (int i = 1; i <= total; i++) {
	    try {
		if (getMessage(i).isSet(Flags.Flag.DELETED))
		    deleted++;
	    } catch (MessageRemovedException me) {
		// This is an expunged message, ignore it.
		continue;
	    }
	}
	return deleted;
    }

    /**
     * Get the Message object corresponding to the given message
     * number.  A Message object's message number is the relative
     * position of this Message in its Folder. Messages are numbered
     * starting at 1 through the total number of message in the folder.
     * Note that the message number for a particular Message can change
     * during a session if other messages in the Folder are deleted and
     * the Folder is expunged. <p>
     *
     * Message objects are light-weight references to the actual message
     * that get filled up on demand. Hence Folder implementations are 
     * expected to provide light-weight Message objects. <p>
     *
     * Unlike Folder objects, repeated calls to getMessage with the
     * same message number will return the same Message object, as
     * long as no messages in this folder have been expunged. <p>
     *
     * Since message numbers can change within a session if the folder
     * is expunged , clients are advised not to use message numbers as 
     * references to messages. Use Message objects instead.
     *
     * @param msgnum	the message number
     * @return 		the Message object
     * @see		#getMessageCount
     * @see		#fetch
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception	IllegalStateException if this folder is not opened
     * @exception	IndexOutOfBoundsException if the message number
     *			is out of range.
     * @exception 	MessagingException for other failures
     */
    public abstract Message getMessage(int msgnum)
				throws MessagingException;

    /**
     * Get the Message objects for message numbers ranging from start
     * through end, both start and end inclusive. Note that message 
     * numbers start at 1, not 0. <p>
     *
     * Message objects are light-weight references to the actual message
     * that get filled up on demand. Hence Folder implementations are 
     * expected to provide light-weight Message objects. <p>
     *
     * This implementation uses getMessage(index) to obtain the required
     * Message objects. Note that the returned array must contain 
     * <code>(end-start+1)</code> Message objects.
     * 
     * @param start	the number of the first message
     * @param end	the number of the last message
     * @return 		the Message objects
     * @see		#fetch
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception	IllegalStateException if this folder is not opened.
     * @exception	IndexOutOfBoundsException if the start or end
     *			message numbers are out of range.
     * @exception 	MessagingException for other failures
     */ 
    public synchronized Message[] getMessages(int start, int end) 
			throws MessagingException {
	Message[] msgs = new Message[end - start +1];
	for (int i = start; i <= end; i++)
	    msgs[i - start] = getMessage(i);
	return msgs;
    }

    /**
     * Get the Message objects for message numbers specified in
     * the array. <p>
     *
     * Message objects are light-weight references to the actual message
     * that get filled up on demand. Hence Folder implementations are 
     * expected to provide light-weight Message objects. <p>
     *
     * This implementation uses getMessage(index) to obtain the required
     * Message objects. Note that the returned array must contain 
     * <code>msgnums.length</code> Message objects
     *
     * @param msgnums	the array of message numbers
     * @return 		the array of Message objects. 
     * @see		#fetch
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception	IllegalStateException if this folder is not opened.
     * @exception	IndexOutOfBoundsException if any message number
     *			in the given array is out of range.
     * @exception 	MessagingException for other failures
     */ 
    public synchronized Message[] getMessages(int[] msgnums)
			throws MessagingException {
	int len = msgnums.length;
	Message[] msgs = new Message[len];
	for (int i = 0; i < len; i++)
	    msgs[i] = getMessage(msgnums[i]);
	return msgs;
    }

    /**
     * Get all Message objects from this Folder. Returns an empty array
     * if the folder is empty.
     *
     * Clients can use Message objects (instead of sequence numbers) 
     * as references to the messages within a folder; this method supplies 
     * the Message objects to the client. Folder implementations are 
     * expected to provide light-weight Message objects, which get
     * filled on demand. <p>
     *
     * This implementation invokes <code>getMessageCount()</code> to get
     * the current message count and then uses <code>getMessage()</code>
     * to get Message objects from 1 till the message count.
     *
     * @return 		array of Message objects, empty array if folder
     *			is empty.
     * @see		#fetch
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception	IllegalStateException if this folder is not opened.
     * @exception 	MessagingException for other failures
     */ 
    public synchronized Message[] getMessages() throws MessagingException {
	if (!isOpen())	// otherwise getMessageCount might return -1
	    throw new IllegalStateException("Folder not open");
	int total = getMessageCount();
	Message[] msgs = new Message[total];
	for (int i = 1; i <= total; i++)
	    msgs[i-1] = getMessage(i);	
	return msgs;
    }

    /**
     * Append given Messages to this folder. This method can be 
     * invoked on a closed Folder. An appropriate MessageCountEvent 
     * is delivered to any MessageCountListener registered on this 
     * folder when the messages arrive in the folder. <p>
     *
     * Folder implementations must not abort this operation if a
     * Message in the given message array turns out to be an
     * expunged Message.
     *
     * @param msgs	array of Messages to be appended
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception 	MessagingException if the append failed.
     */
    public abstract void appendMessages(Message[] msgs)
				throws MessagingException;

    /**
     * Prefetch the items specified in the FetchProfile for the
     * given Messages. <p>
     *
     * Clients use this method to indicate that the specified items are 
     * needed en-masse for the given message range. Implementations are 
     * expected to retrieve these items for the given message range in
     * a efficient manner. Note that this method is just a hint to the
     * implementation to prefetch the desired items. <p>
     *
     * An example is a client filling its header-view window with
     * the Subject, From and X-mailer headers for all messages in the 
     * folder.
     * <blockquote><pre>
     *
     *  Message[] msgs = folder.getMessages();
     *
     *  FetchProfile fp = new FetchProfile();
     *  fp.add(FetchProfile.Item.ENVELOPE);
     *  fp.add("X-mailer");
     *  folder.fetch(msgs, fp);
     *  
     *  for (int i = 0; i &lt; folder.getMessageCount(); i++) {
     *      display(msg[i].getFrom());
     *      display(msg[i].getSubject());
     *      display(msg[i].getHeader("X-mailer"));
     *  }
     *
     * </pre></blockquote><p>
     *
     * The implementation provided here just returns without
     * doing anything useful. Providers wanting to provide a real 
     * implementation for this method should override this method.
     *
     * @param msgs	fetch items for these messages
     * @param fp	the FetchProfile
     * @exception	IllegalStateException if this folder is not opened
     * @exception	MessagingException for other failures
     */
    public void fetch(Message[] msgs, FetchProfile fp)
			throws MessagingException {
	return;
    }

    /**
     * Set the specified flags on the messages specified in the array.
     * This will result in appropriate MessageChangedEvents being
     * delivered to any MessageChangedListener registered on this
     * Message's containing folder. <p>
     *
     * Note that the specified Message objects <strong>must</strong> 
     * belong to this folder. Certain Folder implementations can
     * optimize the operation of setting Flags for a group of messages,
     * so clients might want to use this method, rather than invoking
     * <code>Message.setFlags</code> for each Message. <p>
     *
     * This implementation degenerates to invoking <code>setFlags()</code>
     * on each Message object. Specific Folder implementations that can 
     * optimize this case should do so. 
     * Also, an implementation must not abort the operation if a Message 
     * in the array turns out to be an expunged Message.
     *
     * @param msgs	the array of message objects
     * @param flag	Flags object containing the flags to be set
     * @param value	set the flags to this boolean value
     * @exception	IllegalStateException if this folder is not opened
     *			or if it has been opened READ_ONLY.
     * @exception 	MessagingException for other failures
     * @see		Message#setFlags
     * @see		javax.mail.event.MessageChangedEvent
     */
    public synchronized void setFlags(Message[] msgs,
			Flags flag, boolean value) throws  MessagingException {
	for (int i = 0; i < msgs.length; i++) {
	    try {
		msgs[i].setFlags(flag, value);
	    } catch (MessageRemovedException me) {
		// This message is expunged, skip 
	    }
	}
    }

    /**
     * Set the specified flags on the messages numbered from start
     * through end, both start and end inclusive. Note that message 
     * numbers start at 1, not 0.
     * This will result in appropriate MessageChangedEvents being
     * delivered to any MessageChangedListener registered on this
     * Message's containing folder. <p>
     *
     * Certain Folder implementations can
     * optimize the operation of setting Flags for a group of messages,
     * so clients might want to use this method, rather than invoking
     * <code>Message.setFlags</code> for each Message. <p>
     *
     * The default implementation uses <code>getMessage(int)</code> to
     * get each <code>Message</code> object and then invokes
     * <code>setFlags</code> on that object to set the flags.
     * Specific Folder implementations that can optimize this case should do so.
     * Also, an implementation must not abort the operation if a message 
     * number refers to an expunged message.
     *
     * @param start	the number of the first message
     * @param end	the number of the last message
     * @param flag	Flags object containing the flags to be set
     * @param value	set the flags to this boolean value
     * @exception	IllegalStateException if this folder is not opened
     *			or if it has been opened READ_ONLY.
     * @exception	IndexOutOfBoundsException if the start or end
     *			message numbers are out of range.
     * @exception 	MessagingException for other failures
     * @see		Message#setFlags
     * @see		javax.mail.event.MessageChangedEvent
     */
    public synchronized void setFlags(int start, int end,
			Flags flag, boolean value) throws MessagingException {
	for (int i = start; i <= end; i++) {
	    try {
		Message msg = getMessage(i);
		msg.setFlags(flag, value);
	    } catch (MessageRemovedException me) {
		// This message is expunged, skip 
	    }
	}
    }

    /**
     * Set the specified flags on the messages whose message numbers
     * are in the array.
     * This will result in appropriate MessageChangedEvents being
     * delivered to any MessageChangedListener registered on this
     * Message's containing folder. <p>
     *
     * Certain Folder implementations can
     * optimize the operation of setting Flags for a group of messages,
     * so clients might want to use this method, rather than invoking
     * <code>Message.setFlags</code> for each Message. <p>
     *
     * The default implementation uses <code>getMessage(int)</code> to
     * get each <code>Message</code> object and then invokes
     * <code>setFlags</code> on that object to set the flags.
     * Specific Folder implementations that can optimize this case should do so.
     * Also, an implementation must not abort the operation if a message 
     * number refers to an expunged message.
     *
     * @param msgnums	the array of message numbers
     * @param flag	Flags object containing the flags to be set
     * @param value	set the flags to this boolean value
     * @exception	IllegalStateException if this folder is not opened
     *			or if it has been opened READ_ONLY.
     * @exception	IndexOutOfBoundsException if any message number
     *			in the given array is out of range.
     * @exception 	MessagingException for other failures
     * @see		Message#setFlags
     * @see		javax.mail.event.MessageChangedEvent
     */
    public synchronized void setFlags(int[] msgnums,
			Flags flag, boolean value) throws MessagingException {
	for (int i = 0; i < msgnums.length; i++) {
	    try {
		Message msg = getMessage(msgnums[i]);
		msg.setFlags(flag, value);
	    } catch (MessageRemovedException me) {
		// This message is expunged, skip 
	    }
	}
    }

    /**
     * Copy the specified Messages from this Folder into another 
     * Folder. This operation appends these Messages to the 
     * destination Folder. The destination Folder does not have to 
     * be opened.  An appropriate MessageCountEvent 
     * is delivered to any MessageCountListener registered on the 
     * destination folder when the messages arrive in the folder. <p>
     *
     * Note that the specified Message objects <strong>must</strong> 
     * belong to this folder. Folder implementations might be able
     * to optimize this method by doing server-side copies. <p>
     *
     * This implementation just invokes <code>appendMessages()</code>
     * on the destination folder to append the given Messages. Specific
     * folder implementations that support server-side copies should
     * do so, if the destination folder's Store is the same as this
     * folder's Store. 
     * Also, an implementation must not abort the operation if a 
     * Message in the array turns out to be an expunged Message.
     *
     * @param msgs	the array of message objects
     * @param folder	the folder to copy the messages to
     * @exception	FolderNotFoundException if the destination
     *			folder does not exist.
     * @exception	IllegalStateException if this folder is not opened.
     * @exception	MessagingException for other failures
     * @see		#appendMessages
     */
    public void copyMessages(Message[] msgs, Folder folder)
				throws MessagingException {
	if (!folder.exists())
	    throw new FolderNotFoundException(
			folder.getFullName() + " does not exist",
			folder);

	folder.appendMessages(msgs);
    }

    /**
     * Expunge (permanently remove) messages marked DELETED. Returns an
     * array containing the expunged message objects.  The
     * <code>getMessageNumber</code> method
     * on each of these message objects returns that Message's original
     * (that is, prior to the expunge) sequence number. A MessageCountEvent 
     * containing the expunged messages is delivered to any 
     * MessageCountListeners registered on the folder. <p>
     *
     * Expunge causes the renumbering of Message objects subsequent to
     * the expunged messages. Clients that use message numbers as 
     * references to messages should be aware of this and should be 
     * prepared to deal with the situation (probably by flushing out 
     * existing message number caches and reloading them). Because of 
     * this complexity, it is better for clients to use Message objects
     * as references to messages, rather than message numbers. Any 
     * expunged Messages objects still have to be pruned, but other 
     * Messages in that folder are not affected by the expunge. <p>
     *
     * After a message is expunged, only the <code>isExpunged</code> and 
     * <code>getMessageNumber</code> methods are still valid on the
     * corresponding Message object; other methods may throw
     * <code>MessageRemovedException</code>
     *
     * @return		array of expunged Message objects
     * @exception	FolderNotFoundException if this folder does not
     *			exist
     * @exception	IllegalStateException if this folder is not opened.
     * @exception       MessagingException for other failures
     * @see		Message#isExpunged
     * @see		javax.mail.event.MessageCountEvent
     */
    public abstract Message[] expunge() throws MessagingException;

    /**
     * Search this Folder for messages matching the specified
     * search criterion. Returns an array containing the matching
     * messages . Returns an empty array if no matches were found. <p>
     *
     * This implementation invokes 
     * <code>search(term, getMessages())</code>, to apply the search 
     * over all the messages in this folder. Providers that can implement
     * server-side searching might want to override this method to provide
     * a more efficient implementation.
     *
     * @param term	the search criterion
     * @return 		array of matching messages 
     * @exception       javax.mail.search.SearchException if the search 
     *			term is too complex for the implementation to handle.
     * @exception	FolderNotFoundException if this folder does 
     *			not exist.
     * @exception	IllegalStateException if this folder is not opened.
     * @exception       MessagingException for other failures
     * @see		javax.mail.search.SearchTerm
     */
    public Message[] search(SearchTerm term) throws MessagingException {
	return search(term, getMessages());
    }

    /**
     * Search the given array of messages for those that match the 
     * specified search criterion. Returns an array containing the 
     * matching messages. Returns an empty array if no matches were 
     * found. <p>
     *
     * Note that the specified Message objects <strong>must</strong> 
     * belong to this folder. <p>
     *
     * This implementation iterates through the given array of messages,
     * and applies the search criterion on each message by calling
     * its <code>match()</code> method with the given term. The
     * messages that succeed in the match are returned. Providers
     * that can implement server-side searching might want to override
     * this method to provide a more efficient implementation. If the
     * search term is too complex or contains user-defined terms that
     * cannot be executed on the server, providers may elect to either
     * throw a SearchException or degenerate to client-side searching by
     * calling <code>super.search()</code> to invoke this implementation.
     *
     * @param term	the search criterion
     * @param msgs 	the messages to be searched
     * @return 		array of matching messages 
     * @exception       javax.mail.search.SearchException if the search 
     *			term is too complex for the implementation to handle.
     * @exception	IllegalStateException if this folder is not opened
     * @exception       MessagingException for other failures
     * @see		javax.mail.search.SearchTerm
     */
    public Message[] search(SearchTerm term, Message[] msgs)
				throws MessagingException {
	List<Message> matchedMsgs = new ArrayList<>();

	// Run thru the given messages
	for (Message msg : msgs) {
	    try {
		if (msg.match(term)) // matched
		    matchedMsgs.add(msg); // add it
	    } catch(MessageRemovedException mrex) { }
	}

	return matchedMsgs.toArray(new Message[matchedMsgs.size()]);
    }

    /*
     * The set of listeners are stored in Vectors appropriate to their
     * type.  We mark all listener Vectors as "volatile" because, while
     * we initialize them inside this folder's synchronization lock,
     * they are accessed (checked for null) in the "notify" methods,
     * which can't be synchronized due to lock ordering constraints.
     * Since the listener fields (the handles on the Vector objects)
     * are only ever set, and are never cleared, we believe this is
     * safe.  The code that dispatches the notifications will either
     * see the null and assume there are no listeners or will see the
     * Vector and will process the listeners.  There's an inherent race
     * between adding a listener and notifying the listeners; the lack
     * of synchronization during notification does not make the race
     * condition significantly worse.  If one thread is setting a
     * listener at the "same" time an event is being dispatched, the
     * dispatch code might not see the listener right away.  The
     * dispatch code doesn't have to worry about the Vector handle
     * being set to null, and thus using an out-of-date set of
     * listeners, because we never set the field to null.
     */

    // Vector of connection listeners.
    private volatile Vector<ConnectionListener> connectionListeners = null;

    /**
     * Add a listener for Connection events on this Folder. <p>
     *
     * The implementation provided here adds this listener
     * to an internal list of ConnectionListeners.
     *
     * @param l 	the Listener for Connection events
     * @see		javax.mail.event.ConnectionEvent
     */
    public synchronized void
    addConnectionListener(ConnectionListener l) { 
   	if (connectionListeners == null) 
	    connectionListeners = new Vector<>();
	connectionListeners.addElement(l);
    }

    /**
     * Remove a Connection event listener. <p>
     *
     * The implementation provided here removes this listener
     * from the internal list of ConnectionListeners.
     *
     * @param l 	the listener
     * @see		#addConnectionListener
     */
    public synchronized void
    removeConnectionListener(ConnectionListener l) { 
   	if (connectionListeners != null) 
	    connectionListeners.removeElement(l);
    }

    /**
     * Notify all ConnectionListeners. Folder implementations are
     * expected to use this method to broadcast connection events. <p>
     *
     * The provided implementation queues the event into
     * an internal event queue. An event dispatcher thread dequeues
     * events from the queue and dispatches them to the registered
     * ConnectionListeners. Note that the event dispatching occurs
     * in a separate thread, thus avoiding potential deadlock problems.
     *
     * @param type	the ConnectionEvent type
     * @see		javax.mail.event.ConnectionEvent
     */
    protected void notifyConnectionListeners(int type) {
   	if (connectionListeners != null) {
	    ConnectionEvent e = new ConnectionEvent(this, type);
	    queueEvent(e, connectionListeners);
	}

	/* Fix for broken JDK1.1.x Garbage collector :
	 *  The 'conservative' GC in JDK1.1.x occasionally fails to
	 *  garbage-collect Threads which are in the wait state.
	 *  This would result in thread (and consequently memory) leaks.
	 * 
	 * We attempt to fix this by sending a 'terminator' event
	 * to the queue, after we've sent the CLOSED event. The
	 * terminator event causes the event-dispatching thread to
	 * self destruct.
	 */
	if (type == ConnectionEvent.CLOSED)
	    q.terminateQueue();
    }

    // Vector of folder listeners
    private volatile Vector<FolderListener> folderListeners = null;

    /**
     * Add a listener for Folder events on this Folder. <p>
     *
     * The implementation provided here adds this listener
     * to an internal list of FolderListeners.
     *
     * @param l 	the Listener for Folder events
     * @see		javax.mail.event.FolderEvent
     */
    public synchronized void addFolderListener(FolderListener l) { 
   	if (folderListeners == null)
	    folderListeners = new Vector<>();
	folderListeners.addElement(l);
    }

    /**
     * Remove a Folder event listener. <p>
     *
     * The implementation provided here removes this listener
     * from the internal list of FolderListeners.
     *
     * @param l 	the listener
     * @see		#addFolderListener
     */
    public synchronized void removeFolderListener(FolderListener l) {
	if (folderListeners != null)
	    folderListeners.removeElement(l);
    }

    /**
     * Notify all FolderListeners registered on this Folder and
     * this folder's Store. Folder implementations are expected
     * to use this method to broadcast Folder events. <p>
     *
     * The implementation provided here queues the event into
     * an internal event queue. An event dispatcher thread dequeues
     * events from the queue and dispatches them to the 
     * FolderListeners registered on this folder. The implementation
     * also invokes <code>notifyFolderListeners</code> on this folder's
     * Store to notify any FolderListeners registered on the store.
     *
     * @param type	type of FolderEvent
     * @see		#notifyFolderRenamedListeners
     */
    protected void notifyFolderListeners(int type) { 
   	if (folderListeners != null) {
	    FolderEvent e = new FolderEvent(this, this, type);
	    queueEvent(e, folderListeners);
	}
	store.notifyFolderListeners(type, this);
    }

    /**
     * Notify all FolderListeners registered on this Folder and
     * this folder's Store about the renaming of this folder.
     * Folder implementations are expected to use this method to
     * broadcast Folder events indicating the renaming of folders. <p>
     *
     * The implementation provided here queues the event into
     * an internal event queue. An event dispatcher thread dequeues
     * events from the queue and dispatches them to the 
     * FolderListeners registered on this folder. The implementation
     * also invokes <code>notifyFolderRenamedListeners</code> on this 
     * folder's Store to notify any FolderListeners registered on the store.
     *
     * @param	folder	Folder representing the new name.
     * @see		#notifyFolderListeners
     * @since		JavaMail 1.1
     */
    protected void notifyFolderRenamedListeners(Folder folder) {
   	if (folderListeners != null) {
	    FolderEvent e = new FolderEvent(this, this, folder,
					    FolderEvent.RENAMED);
	    queueEvent(e, folderListeners);
	}
	store.notifyFolderRenamedListeners(this, folder);
    }

    // Vector of MessageCount listeners
    private volatile Vector<MessageCountListener> messageCountListeners = null;

    /**
     * Add a listener for MessageCount events on this Folder. <p>
     *
     * The implementation provided here adds this listener
     * to an internal list of MessageCountListeners.
     *
     * @param l 	the Listener for MessageCount events
     * @see		javax.mail.event.MessageCountEvent
     */
    public synchronized void addMessageCountListener(MessageCountListener l) { 
   	if (messageCountListeners == null)
	    messageCountListeners = new Vector<>();
	messageCountListeners.addElement(l);
    }

    /**
     * Remove a MessageCount listener. <p>
     *
     * The implementation provided here removes this listener
     * from the internal list of MessageCountListeners.
     *
     * @param l 	the listener
     * @see		#addMessageCountListener
     */
    public synchronized void
			removeMessageCountListener(MessageCountListener l) { 
   	if (messageCountListeners != null) 
	    messageCountListeners.removeElement(l); 
    }

    /**
     * Notify all MessageCountListeners about the addition of messages
     * into this folder. Folder implementations are expected to use this 
     * method to broadcast MessageCount events for indicating arrival of
     * new messages. <p>
     *
     * The provided implementation queues the event into
     * an internal event queue. An event dispatcher thread dequeues
     * events from the queue and dispatches them to the registered
     * MessageCountListeners. Note that the event dispatching occurs
     * in a separate thread, thus avoiding potential deadlock problems.
     *
     * @param	msgs	the messages that were added
     */
    protected void notifyMessageAddedListeners(Message[] msgs) { 
   	if (messageCountListeners == null)
	    return;

	MessageCountEvent e = new MessageCountEvent(
					this, 
					MessageCountEvent.ADDED, 
					false,
					msgs);

   	queueEvent(e, messageCountListeners); 
    }

    /**
     * Notify all MessageCountListeners about the removal of messages
     * from this Folder. Folder implementations are expected to use this 
     * method to broadcast MessageCount events indicating removal of
     * messages. <p>
     *
     * The provided implementation queues the event into
     * an internal event queue. An event dispatcher thread dequeues
     * events from the queue and dispatches them to the registered
     * MessageCountListeners. Note that the event dispatching occurs
     * in a separate thread, thus avoiding potential deadlock problems.
     *
     * @param	removed	was the message removed by this client?
     * @param	msgs	the messages that were removed
     */
    protected void notifyMessageRemovedListeners(boolean removed, 
						 Message[] msgs) { 
   	if (messageCountListeners == null)
	    return;

	MessageCountEvent e = new MessageCountEvent(
					this, 
					MessageCountEvent.REMOVED, 
					removed,
					msgs);
   	queueEvent(e, messageCountListeners); 
    }

    // Vector of MessageChanged listeners.
    private volatile Vector<MessageChangedListener> messageChangedListeners
	    = null;

    /**
     * Add a listener for MessageChanged events on this Folder. <p>
     *
     * The implementation provided here adds this listener
     * to an internal list of MessageChangedListeners.
     *
     * @param l 	the Listener for MessageChanged events
     * @see		javax.mail.event.MessageChangedEvent
     */
    public synchronized void
			addMessageChangedListener(MessageChangedListener l) { 
   	if (messageChangedListeners == null)
	    messageChangedListeners = new Vector<>();
	messageChangedListeners.addElement(l);
    }

    /**
     * Remove a MessageChanged listener. <p>
     *
     * The implementation provided here removes this listener
     * from the internal list of MessageChangedListeners.
     *
     * @param l 	the listener
     * @see		#addMessageChangedListener
     */
    public synchronized void
		removeMessageChangedListener(MessageChangedListener l) { 
   	if (messageChangedListeners != null) 
	    messageChangedListeners.removeElement(l);
    }

    /**
     * Notify all MessageChangedListeners. Folder implementations are
     * expected to use this method to broadcast MessageChanged events. <p>
     *
     * The provided implementation queues the event into
     * an internal event queue. An event dispatcher thread dequeues
     * events from the queue and dispatches them to registered
     * MessageChangedListeners. Note that the event dispatching occurs
     * in a separate thread, thus avoiding potential deadlock problems.
     *
     * @param	type	the MessageChangedEvent type
     * @param	msg	the message that changed
     */
    protected void notifyMessageChangedListeners(int type, Message msg) {
	if (messageChangedListeners == null)
	    return;
	
	MessageChangedEvent e = new MessageChangedEvent(this, type, msg);
	queueEvent(e, messageChangedListeners);
    }

    /*
     * Add the event and vector of listeners to the queue to be delivered.
     */
    @SuppressWarnings("unchecked")
    private void queueEvent(MailEvent event,
	    Vector<? extends EventListener> vector) {
	/*
         * Copy the vector in order to freeze the state of the set
         * of EventListeners the event should be delivered to prior
         * to delivery.  This ensures that any changes made to the
         * Vector from a target listener's method during the delivery
         * of this event will not take effect until after the event is
         * delivered.
         */
	Vector<? extends EventListener> v = (Vector)vector.clone();
	q.enqueue(event, v);
    }

    @Override
    protected void finalize() throws Throwable {
	try {
	    q.terminateQueue();
	} finally {
	    super.finalize();
	}
    }

    /**
     * override the default toString(), it will return the String
     * from Folder.getFullName() or if that is null, it will use
     * the default toString() behavior.
     */

    @Override
    public String toString() {
	String s = getFullName();
	if (s != null)
	    return s;
	else
	    return super.toString();
    }
}
