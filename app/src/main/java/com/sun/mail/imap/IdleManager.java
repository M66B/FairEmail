/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.imap;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import javax.mail.*;

import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.MailLogger;

/**
 * IdleManager uses the optional IMAP IDLE command
 * (<A HREF="http://www.ietf.org/rfc/rfc2177.txt">RFC 2177</A>)
 * to watch multiple folders for new messages.
 * IdleManager uses an Executor to execute tasks in separate threads.
 * An Executor is typically provided by an ExecutorService.
 * For example, for a Java SE application:
 * <blockquote><pre>
 *	ExecutorService es = Executors.newCachedThreadPool();
 *	final IdleManager idleManager = new IdleManager(session, es);
 * </pre></blockquote>
 * For a Java EE 7 application:
 * <blockquote><pre>
 *	{@literal @}Resource
 *	ManagedExecutorService es;
 *	final IdleManager idleManager = new IdleManager(session, es);
 * </pre></blockquote>
 * To watch for new messages in a folder, open the folder, register a listener,
 * and ask the IdleManager to watch the folder:
 * <blockquote><pre>
 *	Folder folder = store.getFolder("INBOX");
 *	folder.open(Folder.READ_WRITE);
 *	folder.addMessageCountListener(new MessageCountAdapter() {
 *	    public void messagesAdded(MessageCountEvent ev) {
 *		Folder folder = (Folder)ev.getSource();
 *		Message[] msgs = ev.getMessages();
 *		System.out.println("Folder: " + folder +
 *		    " got " + msgs.length + " new messages");
 *		try {
 *		    // process new messages
 *		    idleManager.watch(folder); // keep watching for new messages
 *		} catch (MessagingException mex) {
 *		    // handle exception related to the Folder
 *		}
 *	    }
 *	});
 *	idleManager.watch(folder);
 * </pre></blockquote>
 * This delivers the events for each folder in a separate thread, <b>NOT</b>
 * using the Executor.  To deliver all events in a single thread
 * using the Executor, set the following properties for the Session
 * (once), and then add listeners and watch the folder as above.
 * <blockquote><pre>
 *	// the following should be done once...
 *	Properties props = session.getProperties();
 *	props.put("mail.event.scope", "session"); // or "application"
 *	props.put("mail.event.executor", es);
 * </pre></blockquote>
 * Note that, after processing new messages in your listener, or doing any
 * other operations on the folder in any other thread, you need to tell
 * the IdleManager to watch for more new messages.  Unless, of course, you
 * close the folder.
 * <p>
 * The IdleManager is created with a Session, which it uses only to control
 * debug output.  A single IdleManager instance can watch multiple Folders
 * from multiple Stores and multiple Sessions.
 * <p>
 * Due to limitations in the Java SE nio support, a
 * {@link java.nio.channels.SocketChannel SocketChannel} must be used instead
 * of a {@link java.net.Socket Socket} to connect to the server.  However,
 * SocketChannels don't support all the features of Sockets, such as connecting
 * through a SOCKS proxy server.  SocketChannels also don't support
 * simultaneous read and write, which means that the
 * {@link com.sun.mail.imap.IMAPFolder#idle idle} method can't be used if
 * SocketChannels are being used; use this IdleManager instead.
 * To enable support for SocketChannels instead of Sockets, set the
 * <code>mail.imap.usesocketchannels</code> property in the Session used to
 * access the IMAP Folder.  (Or <code>mail.imaps.usesocketchannels</code> if
 * you're using the "imaps" protocol.)  This will effect all connections in
 * that Session, but you can create another Session without this property set
 * if you need to use the features that are incompatible with SocketChannels.
 * <p>
 * NOTE: The IdleManager, and all APIs and properties related to it, should
 * be considered <strong>EXPERIMENTAL</strong>.  They may be changed in the
 * future in ways that are incompatible with applications using the
 * current APIs.
 *
 * @since JavaMail 1.5.2
 */
public class IdleManager {
    private Executor es;
    private Selector selector;
    private MailLogger logger;
    private volatile boolean die = false;
    private volatile boolean running;
    private Queue<IMAPFolder> toWatch = new ConcurrentLinkedQueue<>();
    private Queue<IMAPFolder> toAbort = new ConcurrentLinkedQueue<>();

    /**
     * Create an IdleManager.  The Session is used only to configure
     * debugging output.  The Executor is used to create the
     * "select" thread.
     *
     * @param	session	the Session containing configuration information
     * @param	es	the Executor used to create threads
     * @exception	IOException	for Selector failures
     */
    public IdleManager(Session session, Executor es) throws IOException {
	this.es = es;
	logger = new MailLogger(this.getClass(), "DEBUG IMAP",
				session.getDebug(), session.getDebugOut());
	selector = Selector.open();
	es.execute(new Runnable() {
	    @Override
	    public void run() {
		logger.fine("IdleManager select starting");
		try {
		    running = true;
		    select();
		} finally {
		    running = false;
		    logger.fine("IdleManager select terminating");
		}
	    }
	});
    }

    /**
     * Is the IdleManager currently running?  The IdleManager starts
     * running when the Executor schedules its task.  The IdleManager
     * stops running after its task detects the stop request from the
     * {@link #stop stop} method, or if it terminates abnormally due
     * to an unexpected error.
     *
     * @return	true if the IdleMaanger is running
     * @since JavaMail 1.5.5
     */
    public boolean isRunning() {
	return running;
    }

    /**
     * Watch the Folder for new messages and other events using the IMAP IDLE
     * command.
     *
     * @param	folder	the folder to watch
     * @exception	MessagingException	for errors related to the folder
     */
    public void watch(Folder folder)
				throws MessagingException {
	if (die)	// XXX - should be IllegalStateException?
	    throw new MessagingException("IdleManager is not running");
	if (!(folder instanceof IMAPFolder))
	    throw new MessagingException("Can only watch IMAP folders");
	IMAPFolder ifolder = (IMAPFolder)folder;
	SocketChannel sc = ifolder.getChannel();
	if (sc == null) {
	    if (folder.isOpen())
		throw new MessagingException(
					"Folder is not using SocketChannels");
	    else
		throw new MessagingException("Folder is not open");
	}
	if (logger.isLoggable(Level.FINEST))
	    logger.log(Level.FINEST, "IdleManager watching {0}",
							folderName(ifolder));
	// keep trying to start the IDLE command until we're successful.
	// may block if we're in the middle of aborting an IDLE command.
	int tries = 0;
	while (!ifolder.startIdle(this)) {
	    if (logger.isLoggable(Level.FINEST))
		logger.log(Level.FINEST,
			    "IdleManager.watch startIdle failed for {0}",
			    folderName(ifolder));
	    tries++;
	}
	if (logger.isLoggable(Level.FINEST)) {
	    if (tries > 0)
		logger.log(Level.FINEST,
			"IdleManager.watch startIdle succeeded for {0}" +
			" after " + tries + " tries",
			folderName(ifolder));
	    else
		logger.log(Level.FINEST,
			"IdleManager.watch startIdle succeeded for {0}",
			folderName(ifolder));
	}
	synchronized (this) {
	    toWatch.add(ifolder);
	    selector.wakeup();
	}
    }

    /**
     * Request that the specified folder abort an IDLE command.
     * We can't do the abort directly because the DONE message needs
     * to be sent through the (potentially) SSL socket, which means
     * we need to be in blocking I/O mode.  We can only switch to
     * blocking I/O mode when not selecting, so wake up the selector,
     * which will process this request when it wakes up.
     */
    void requestAbort(IMAPFolder folder) {
	toAbort.add(folder);
	selector.wakeup();
    }

    /**
     * Run the {@link java.nio.channels.Selector#select select} loop
     * to poll each watched folder for events sent from the server.
     */
    private void select() {
	die = false;
	try {
	    while (!die) {
		watchAll();
		logger.finest("IdleManager waiting...");
		int ns = selector.select();
		if (logger.isLoggable(Level.FINEST))
		    logger.log(Level.FINEST,
			"IdleManager selected {0} channels", ns);
		if (die || Thread.currentThread().isInterrupted())
		    break;

		/*
		 * Process any selected folders.  We cancel the
		 * selection key for any selected folder, so if we
		 * need to continue watching that folder it's added
		 * to the toWatch list again.  We can't actually
		 * register that folder again until the previous
		 * selection key is cancelled, so we call selectNow()
		 * just for the side effect of cancelling the selection
		 * keys.  But if selectNow() selects something, we
		 * process it before adding folders from the toWatch
		 * queue.  And so on until there is nothing to do, at
		 * which point it's safe to register folders from the
		 * toWatch queue.  This should be "fair" since each
		 * selection key is used only once before being added
		 * to the toWatch list.
		 */
		do {
		    processKeys();
		} while (selector.selectNow() > 0 || !toAbort.isEmpty());
	    }
	} catch (InterruptedIOException ex) {
	    logger.log(Level.FINEST, "IdleManager interrupted", ex);
	} catch (IOException ex) {
	    logger.log(Level.FINEST, "IdleManager got I/O exception", ex);
	} catch (Exception ex) {
	    logger.log(Level.FINEST, "IdleManager got exception", ex);
	} finally {
	    die = true;	// prevent new watches in case of exception
	    logger.finest("IdleManager unwatchAll");
	    try {
		unwatchAll();
		selector.close();
	    } catch (IOException ex2) {
		// nothing to do...
		logger.log(Level.FINEST, "IdleManager unwatch exception", ex2);
	    }
	    logger.fine("IdleManager exiting");
	}
    }

    /**
     * Register all of the folders in the queue with the selector,
     * switching them to nonblocking I/O mode first.
     */
    private void watchAll() {
	/*
	 * Pull each of the folders from the toWatch queue
	 * and register it.
	 */
	IMAPFolder folder;
	while ((folder = toWatch.poll()) != null) {
	    if (logger.isLoggable(Level.FINEST))
		logger.log(Level.FINEST,
		    "IdleManager adding {0} to selector", folderName(folder));
	    try {
		SocketChannel sc = folder.getChannel();
		if (sc == null)
		    continue;
		// has to be non-blocking to select
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ, folder);
	    } catch (IOException ex) {
		// oh well, nothing to do
		logger.log(Level.FINEST,
		    "IdleManager can't register folder", ex);
	    } catch (CancelledKeyException ex) {
		// this should never happen
		logger.log(Level.FINEST,
		    "IdleManager can't register folder", ex);
	    }
	}
    }

    /**
     * Process the selected keys.
     */
    private void processKeys() throws IOException {
	IMAPFolder folder;

	/*
	 * First, process any channels with data to read.
	 */
	Set<SelectionKey> selectedKeys = selector.selectedKeys();
	/*
	 * XXX - this is simpler, but it can fail with
	 *	 ConcurrentModificationException
	 *
	for (SelectionKey sk : selectedKeys) {
	    selectedKeys.remove(sk);	// only process each key once
	    ...
	}
	*/
	Iterator<SelectionKey> it = selectedKeys.iterator();
	while (it.hasNext()) {
	    SelectionKey sk = it.next();
	    it.remove();	// only process each key once
	    // have to cancel so we can switch back to blocking I/O mode
	    sk.cancel();
	    folder = (IMAPFolder)sk.attachment();
	    if (logger.isLoggable(Level.FINEST))
		logger.log(Level.FINEST,
		    "IdleManager selected folder: {0}", folderName(folder));
	    SelectableChannel sc = sk.channel();
	    // switch back to blocking to allow normal I/O
	    sc.configureBlocking(true);
	    try {
		if (folder.handleIdle(false)) {
		    if (logger.isLoggable(Level.FINEST))
			logger.log(Level.FINEST,
			    "IdleManager continue watching folder {0}",
							folderName(folder));
		    // more to do with this folder, select on it again
		    toWatch.add(folder);
		} else {
		    // done watching this folder,
		    if (logger.isLoggable(Level.FINEST))
			logger.log(Level.FINEST,
			    "IdleManager done watching folder {0}",
							folderName(folder));
		}
	    } catch (MessagingException ex) {
		// something went wrong, stop watching this folder
		logger.log(Level.FINEST,
		    "IdleManager got exception for folder: " +
						    folderName(folder), ex);
	    }
	}

	/*
	 * Now, process any folders that we need to abort.
	 */
	while ((folder = toAbort.poll()) != null) {
	    if (logger.isLoggable(Level.FINEST))
		logger.log(Level.FINEST,
		    "IdleManager aborting IDLE for folder: {0}",
							folderName(folder));
	    SocketChannel sc = folder.getChannel();
	    if (sc == null)
		continue;
	    SelectionKey sk = sc.keyFor(selector);
	    // have to cancel so we can switch back to blocking I/O mode
	    if (sk != null)
		sk.cancel();
	    // switch back to blocking to allow normal I/O
	    sc.configureBlocking(true);

	    // if there's a read timeout, have to do the abort in a new thread
	    Socket sock = sc.socket();
	    if (sock != null && sock.getSoTimeout() > 0) {
		logger.finest("IdleManager requesting DONE with timeout");
		toWatch.remove(folder);
		final IMAPFolder folder0 = folder;
		es.execute(new Runnable() {
		    @Override
		    public void run() {
			// send the DONE and wait for the response
			folder0.idleAbortWait();
		    }
		});
	    } else {
		folder.idleAbort();	// send the DONE message
		// watch for OK response to DONE
		// XXX - what if we also added it above?  should be a nop
		toWatch.add(folder);
	    }
	}
    }

    /**
     * Stop watching all folders.  Cancel any selection keys and,
     * most importantly, switch the channel back to blocking mode.
     * If there's any folders waiting to be watched, need to abort
     * them too.
     */
    private void unwatchAll() {
	IMAPFolder folder;
	Set<SelectionKey> keys = selector.keys();
	for (SelectionKey sk : keys) {
	    // have to cancel so we can switch back to blocking I/O mode
	    sk.cancel();
	    folder = (IMAPFolder)sk.attachment();
	    if (logger.isLoggable(Level.FINEST))
		logger.log(Level.FINEST,
		    "IdleManager no longer watching folder: {0}",
							folderName(folder));
	    SelectableChannel sc = sk.channel();
	    // switch back to blocking to allow normal I/O
	    try {
		sc.configureBlocking(true);
		folder.idleAbortWait();	// send the DONE message and wait
	    } catch (IOException ex) {
		// ignore it, channel might be closed
		logger.log(Level.FINEST,
		    "IdleManager exception while aborting idle for folder: " +
						    folderName(folder), ex);
	    }
	}

	/*
	 * Finally, process any folders waiting to be watched.
	 */
	while ((folder = toWatch.poll()) != null) {
	    if (logger.isLoggable(Level.FINEST))
		logger.log(Level.FINEST,
		    "IdleManager aborting IDLE for unwatched folder: {0}",
							folderName(folder));
	    SocketChannel sc = folder.getChannel();
	    if (sc == null)
		continue;
	    try {
		// channel should still be in blocking mode, but make sure
		sc.configureBlocking(true);
		folder.idleAbortWait();	// send the DONE message and wait
	    } catch (IOException ex) {
		// ignore it, channel might be closed
		logger.log(Level.FINEST,
		    "IdleManager exception while aborting idle for folder: " +
						    folderName(folder), ex);
	    }
	}
    }

    /**
     * Stop the IdleManager.  The IdleManager can not be restarted.
     */
    public synchronized void stop() {
	die = true;
	logger.fine("IdleManager stopping");
	selector.wakeup();
    }

    /**
     * Return the fully qualified name of the folder, for use in log messages.
     * Essentially just the getURLName method, but ignoring the
     * MessagingException that can never happen.
     */
    private static String folderName(Folder folder) {
	try {
	    return folder.getURLName().toString();
	} catch (MessagingException mex) {
	    // can't happen
	    return folder.getStore().toString() + "/" + folder.toString();
	}
    }
}
