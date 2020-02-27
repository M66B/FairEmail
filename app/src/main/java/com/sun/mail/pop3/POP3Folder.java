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

package com.sun.mail.pop3;

import javax.mail.*;
import javax.mail.event.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.lang.reflect.Constructor;

import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.MailLogger;
import java.util.ArrayList;
import java.util.List;

/**
 * A POP3 Folder (can only be "INBOX").
 *
 * See the <a href="package-summary.html">com.sun.mail.pop3</a> package
 * documentation for further information on the POP3 protocol provider. <p>
 *
 * @author      Bill Shannon
 * @author	John Mani (ported to the javax.mail APIs)
 */
public class POP3Folder extends Folder {

    private String name;
    private POP3Store store;
    private volatile Protocol port;
    private int total;
    private int size;
    private boolean exists = false;
    private volatile boolean opened = false;
    private POP3Message[] message_cache;
    private boolean doneUidl = false;
    private volatile TempFile fileCache = null;
    private boolean forceClose;

    MailLogger logger;	// package private, for POP3Message

    protected POP3Folder(POP3Store store, String name) {
	super(store);
	this.name = name;
	this.store = store;
	if (name.equalsIgnoreCase("INBOX"))
	    exists = true;
	logger = new MailLogger(this.getClass(), "DEBUG POP3",
	    store.getSession().getDebug(), store.getSession().getDebugOut());
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public String getFullName() {
	return name;
    }

    @Override
    public Folder getParent() {
	return new DefaultFolder(store);
    }

    /**
     * Always true for the folder "INBOX", always false for
     * any other name.
     *
     * @return	true for INBOX, false otherwise
     */
    @Override
    public boolean exists() {
	return exists;
    }

    /**
     * Always throws <code>MessagingException</code> because no POP3 folders
     * can contain subfolders.
     *
     * @exception	MessagingException	always
     */
    @Override
    public Folder[] list(String pattern) throws MessagingException {
	throw new MessagingException("not a directory");
    }

    /**
     * Always returns a NUL character because POP3 doesn't support a hierarchy.
     *
     * @return	NUL
     */
    @Override
    public char getSeparator() {
	return '\0';
    }

    /**
     * Always returns Folder.HOLDS_MESSAGES.
     *
     * @return	Folder.HOLDS_MESSAGES
     */
    @Override
    public int getType() {
	return HOLDS_MESSAGES;
    }

    /**
     * Always returns <code>false</code>; the POP3 protocol doesn't
     * support creating folders.
     *
     * @return	false
     */
    @Override
    public boolean create(int type) throws MessagingException {
	return false;
    }

    /**
     * Always returns <code>false</code>; the POP3 protocol provides
     * no way to determine when a new message arrives.
     *
     * @return	false
     */
    @Override
    public boolean hasNewMessages() throws MessagingException {
	return false;    // no way to know
    }

    /**
     * Always throws <code>MessagingException</code> because no POP3 folders
     * can contain subfolders.
     *
     * @exception	MessagingException	always
     */
    @Override
    public Folder getFolder(String name) throws MessagingException {
	throw new MessagingException("not a directory");
    }

    /**
     * Always throws <code>MethodNotSupportedException</code>
     * because the POP3 protocol doesn't allow the INBOX to
     * be deleted.
     *
     * @exception	MethodNotSupportedException	always
     */
    @Override
    public boolean delete(boolean recurse) throws MessagingException {
	throw new MethodNotSupportedException("delete");
    }

    /**
     * Always throws <code>MethodNotSupportedException</code>
     * because the POP3 protocol doesn't support multiple folders.
     *
     * @exception	MethodNotSupportedException	always
     */
    @Override
    public boolean renameTo(Folder f) throws MessagingException {
	throw new MethodNotSupportedException("renameTo");
    }

    /**
     * Throws <code>FolderNotFoundException</code> unless this
     * folder is named "INBOX".
     *
     * @exception	FolderNotFoundException	if not INBOX
     * @exception	AuthenticationFailedException	authentication failures
     * @exception	MessagingException	other open failures
     */
    @Override
    public synchronized void open(int mode) throws MessagingException {
	checkClosed();
	if (!exists)
	    throw new FolderNotFoundException(this, "folder is not INBOX");

	try {
	    port = store.getPort(this);
	    Status s = port.stat();
	    total = s.total;
	    size = s.size;
	    this.mode = mode;
	    if (store.useFileCache) {
		try {
		    fileCache = new TempFile(store.fileCacheDir);
		} catch (IOException ex) {
		    logger.log(Level.FINE, "failed to create file cache", ex);
		    throw ex;	// caught below
		}
	    }
	    opened = true;
	} catch (IOException ioex) {
	    try {
		if (port != null)
		    port.quit();
	    } catch (IOException ioex2) {
		// ignore
	    } finally {
		port = null;
		store.closePort(this);
	    }
	    throw new MessagingException("Open failed", ioex);
	}

	// Create the message cache array of appropriate size
	message_cache = new POP3Message[total];
	doneUidl = false;

	notifyConnectionListeners(ConnectionEvent.OPENED);
    }

    @Override
    public synchronized void close(boolean expunge) throws MessagingException {
	checkOpen();

	try {
	    /*
	     * Some POP3 servers will mark messages for deletion when
	     * they're read.  To prevent such messages from being
	     * deleted before the client deletes them, you can set
	     * the mail.pop3.rsetbeforequit property to true.  This
	     * causes us to issue a POP3 RSET command to clear all
	     * the "marked for deletion" flags.  We can then explicitly
	     * delete messages as desired.
	     */
	    if (store.rsetBeforeQuit && !forceClose)
		port.rset();
	    POP3Message m;
	    if (expunge && mode == READ_WRITE && !forceClose) {
		// find all messages marked deleted and issue DELE commands
		for (int i = 0; i < message_cache.length; i++) {
		    if ((m = message_cache[i]) != null) {
			if (m.isSet(Flags.Flag.DELETED))
			    try {
				port.dele(i + 1);
			    } catch (IOException ioex) {
				throw new MessagingException(
				    "Exception deleting messages during close",
				    ioex);
			    }
		    }
		}
	    }

	    /*
	     * Flush and free all cached data for the messages.
	     */
	    for (int i = 0; i < message_cache.length; i++) {
		if ((m = message_cache[i]) != null)
		    m.invalidate(true);
	    }

	    if (forceClose)
		port.close();
	    else
		port.quit();
	} catch (IOException ex) {
	    // do nothing
	} finally {
	    port = null;
	    store.closePort(this);
	    message_cache = null;
	    opened = false;
	    notifyConnectionListeners(ConnectionEvent.CLOSED);
	    if (fileCache != null) {
		fileCache.close();
		fileCache = null;
	    }
	}
    }

    @Override
    public synchronized boolean isOpen() {
	if (!opened)
	    return false;
	try {
	    if (!port.noop())
		throw new IOException("NOOP failed");
	} catch (IOException ioex) {
	    try {
		close(false);
	    } catch (MessagingException mex) {
		// ignore it
	    }
	    return false;
	}
	return true;
    }

    /**
     * Always returns an empty <code>Flags</code> object because
     * the POP3 protocol doesn't support any permanent flags.
     *
     * @return	empty Flags object
     */
    @Override
    public Flags getPermanentFlags() {
	return new Flags(); // empty flags object
    }

    /**
     * Will not change while the folder is open because the POP3
     * protocol doesn't support notification of new messages
     * arriving in open folders.
     */
    @Override
    public synchronized int getMessageCount() throws MessagingException {
	if (!opened)
	    return -1;
	checkReadable();
	return total;
    }

    @Override
    public synchronized Message getMessage(int msgno)
					throws MessagingException {
	checkOpen();

	POP3Message m;

	// Assuming that msgno is <= total 
	if ((m = message_cache[msgno-1]) == null) {
	    m = createMessage(this, msgno);
	    message_cache[msgno-1] = m;
	}
	return m;
    }

    protected POP3Message createMessage(Folder f, int msgno)
				throws MessagingException {
	POP3Message m = null;
	Constructor<?> cons = store.messageConstructor;
	if (cons != null) {
	    try {
		Object[] o = { this, Integer.valueOf(msgno) };
		m = (POP3Message)cons.newInstance(o);
	    } catch (Exception ex) {
		// ignore
	    }
	}
	if (m == null)
	    m = new POP3Message(this, msgno);
	return m;
    }

    /**
     * Always throws <code>MethodNotSupportedException</code>
     * because the POP3 protocol doesn't support appending messages.
     *
     * @exception	MethodNotSupportedException	always
     */
    @Override
    public void appendMessages(Message[] msgs) throws MessagingException {
	throw new MethodNotSupportedException("Append not supported");	
    }

    /**
     * Always throws <code>MethodNotSupportedException</code>
     * because the POP3 protocol doesn't support expunging messages
     * without closing the folder; call the {@link #close close} method
     * with the <code>expunge</code> argument set to <code>true</code>
     * instead.
     *
     * @exception	MethodNotSupportedException	always
     */
    @Override
    public Message[] expunge() throws MessagingException {
	throw new MethodNotSupportedException("Expunge not supported");
    }

    /**
     * Prefetch information about POP3 messages.
     * If the FetchProfile contains <code>UIDFolder.FetchProfileItem.UID</code>,
     * POP3 UIDs for all messages in the folder are fetched using the POP3
     * UIDL command.
     * If the FetchProfile contains <code>FetchProfile.Item.ENVELOPE</code>,
     * the headers and size of all messages are fetched using the POP3 TOP
     * and LIST commands.
     */
    @Override
    public synchronized void fetch(Message[] msgs, FetchProfile fp)
				throws MessagingException {
	checkReadable();
	if (!doneUidl && store.supportsUidl &&
		fp.contains(UIDFolder.FetchProfileItem.UID)) {
	    /*
	     * Since the POP3 protocol only lets us fetch the UID
	     * for a single message or for all messages, we go ahead
	     * and fetch UIDs for all messages here, ignoring the msgs
	     * parameter.  We could be more intelligent and base this
	     * decision on the number of messages fetched, or the
	     * percentage of the total number of messages fetched.
	     */
	    String[] uids = new String[message_cache.length];
	    try {
		if (!port.uidl(uids))
		    return;
	    } catch (EOFException eex) {
		close(false);
		throw new FolderClosedException(this, eex.toString());
	    } catch (IOException ex) {
		throw new MessagingException("error getting UIDL", ex);
	    }
	    for (int i = 0; i < uids.length; i++) {
		if (uids[i] == null)
		    continue;
		POP3Message m = (POP3Message)getMessage(i + 1);
		m.uid = uids[i];
	    }
	    doneUidl = true;	// only do this once
	}
	if (fp.contains(FetchProfile.Item.ENVELOPE)) {
	    for (int i = 0; i < msgs.length; i++) {
		try {
		    POP3Message msg = (POP3Message)msgs[i];
		    // fetch headers
		    msg.getHeader("");
		    // fetch message size
		    msg.getSize();
		} catch (MessageRemovedException mex) {
		    // should never happen, but ignore it if it does
		}
	    }
	}
    }

    /**
     * Return the unique ID string for this message, or null if
     * not available.  Uses the POP3 UIDL command.
     *
     * @param	msg	the message
     * @return          unique ID string
     * @exception	MessagingException for failures
     */
    public synchronized String getUID(Message msg) throws MessagingException {
	checkOpen();
	if (!(msg instanceof POP3Message))
	    throw new MessagingException("message is not a POP3Message");
	POP3Message m = (POP3Message)msg;
	try {
	    if (!store.supportsUidl)
		return null;
	    if (m.uid == POP3Message.UNKNOWN)
		m.uid = port.uidl(m.getMessageNumber());
	    return m.uid;
	} catch (EOFException eex) {
	    close(false);
	    throw new FolderClosedException(this, eex.toString());
	} catch (IOException ex) {
	    throw new MessagingException("error getting UIDL", ex);
	}
    }

    /**
     * Return the size of this folder, as was returned by the POP3 STAT
     * command when this folder was opened.
     *
     * @return		folder size
     * @exception	IllegalStateException	if the folder isn't open
     * @exception	MessagingException for other failures
     */
    public synchronized int getSize() throws MessagingException {
	checkOpen();
	return size;
    }

    /**
     * Return the sizes of all messages in this folder, as returned
     * by the POP3 LIST command.  Each entry in the array corresponds
     * to a message; entry <i>i</i> corresponds to message number <i>i+1</i>.
     *
     * @return		array of message sizes
     * @exception	IllegalStateException	if the folder isn't open
     * @exception	MessagingException for other failures
     * @since		JavaMail 1.3.3
     */
    public synchronized int[] getSizes() throws MessagingException {
	checkOpen();
	int sizes[] = new int[total];
	InputStream is = null;
	LineInputStream lis = null;
	try {
	    is = port.list();
	    lis = new LineInputStream(is);
	    String line;
	    while ((line = lis.readLine()) != null) {
		try {
		    StringTokenizer st = new StringTokenizer(line);
		    int msgnum = Integer.parseInt(st.nextToken());
		    int size = Integer.parseInt(st.nextToken());
		    if (msgnum > 0 && msgnum <= total)
			sizes[msgnum - 1] = size;
		} catch (RuntimeException e) {
		}
	    }
	} catch (IOException ex) {
	    // ignore it?
	} finally {
	    try {
		if (lis != null)
		    lis.close();
	    } catch (IOException cex) { }
	    try {
		if (is != null)
		    is.close();
	    } catch (IOException cex) { }
	}
	return sizes;
    }

    /**
     * Return the raw results of the POP3 LIST command with no arguments.
     *
     * @return		InputStream containing results
     * @exception	IllegalStateException	if the folder isn't open
     * @exception	IOException for I/O errors talking to the server
     * @exception	MessagingException for other errors
     * @since		JavaMail 1.3.3
     */
    public synchronized InputStream listCommand()
				throws MessagingException, IOException {
	checkOpen();
	return port.list();
    }

    /**
     * Close the folder when we're finalized.
     */
    @Override
    protected void finalize() throws Throwable {
	forceClose = !store.finalizeCleanClose;
	try {
	    if (opened)
		close(false);
	} finally {
	    super.finalize();
	    forceClose = false;
	}
    }

    /* Ensure the folder is open */
    private void checkOpen() throws IllegalStateException {
	if (!opened) 
	    throw new IllegalStateException("Folder is not Open");
    }

    /* Ensure the folder is not open */
    private void checkClosed() throws IllegalStateException {
	if (opened) 
	    throw new IllegalStateException("Folder is Open");
    }

    /* Ensure the folder is open & readable */
    private void checkReadable() throws IllegalStateException {
	if (!opened || (mode != READ_ONLY && mode != READ_WRITE))
	    throw new IllegalStateException("Folder is not Readable");
    }

    /* Ensure the folder is open & writable */
    /*
    private void checkWritable() throws IllegalStateException {
	if (!opened || mode != READ_WRITE)
	    throw new IllegalStateException("Folder is not Writable");
    }
    */

    /**
     * Centralize access to the Protocol object by POP3Message
     * objects so that they will fail appropriately when the folder
     * is closed.
     */
    Protocol getProtocol() throws MessagingException {
	Protocol p = port;	// read it before close() can set it to null
	checkOpen();
	// close() might happen here
	return p;
    }

    /*
     * Only here to make accessible to POP3Message.
     */
    @Override
    protected void notifyMessageChangedListeners(int type, Message m) {
	super.notifyMessageChangedListeners(type, m);
    }

    /**
     * Used by POP3Message.
     */
    TempFile getFileCache() {
	return fileCache;
    }
}
