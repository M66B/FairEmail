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

import java.util.Properties;
import java.util.logging.Level;
import java.lang.reflect.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.Collections;
import java.util.Map;

import com.sun.mail.util.PropUtil;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.SocketConnectException;
import com.sun.mail.util.MailConnectException;

/**
 * A POP3 Message Store.  Contains only one folder, "INBOX".
 *
 * See the <a href="package-summary.html">com.sun.mail.pop3</a> package
 * documentation for further information on the POP3 protocol provider. <p>
 *
 * @author      Bill Shannon
 * @author      John Mani
 */
public class POP3Store extends Store {

    private String name = "pop3";		// my protocol name
    private int defaultPort = 110;		// default POP3 port
    private boolean isSSL = false;		// use SSL?

    private Protocol port = null;		// POP3 port for self
    private POP3Folder portOwner = null;	// folder owning port
    private String host = null;			// host
    private int portNum = -1;
    private String user = null;
    private String passwd = null;
    private boolean useStartTLS = false;
    private boolean requireStartTLS = false;
    private boolean usingSSL = false;
    private Map<String, String> capabilities;
    private MailLogger logger;

    // following set here and accessed by other classes in this package
    volatile Constructor<?> messageConstructor = null;
    volatile boolean rsetBeforeQuit = false;
    volatile boolean disableTop = false;
    volatile boolean forgetTopHeaders = false;
    volatile boolean supportsUidl = true;
    volatile boolean cacheWriteTo = false;
    volatile boolean useFileCache = false;
    volatile File fileCacheDir = null;
    volatile boolean keepMessageContent = false;
    volatile boolean finalizeCleanClose = false;

    public POP3Store(Session session, URLName url) {
	this(session, url, "pop3", false);
    }

    public POP3Store(Session session, URLName url,
				String name, boolean isSSL) {
	super(session, url);
	if (url != null)
	    name = url.getProtocol();
	this.name = name;
	logger = new MailLogger(this.getClass(), "DEBUG POP3",
				session.getDebug(), session.getDebugOut());

	if (!isSSL)
	    isSSL = PropUtil.getBooleanProperty(session.getProperties(),
				"mail." + name + ".ssl.enable", false);
	if (isSSL)
	    this.defaultPort = 995;
	else
	    this.defaultPort = 110;
	this.isSSL = isSSL;

	rsetBeforeQuit = getBoolProp("rsetbeforequit");
	disableTop = getBoolProp("disabletop");
	forgetTopHeaders = getBoolProp("forgettopheaders");
	cacheWriteTo = getBoolProp("cachewriteto");
	useFileCache = getBoolProp("filecache.enable");
	String dir = session.getProperty("mail." + name + ".filecache.dir");
	if (dir != null && logger.isLoggable(Level.CONFIG))
	    logger.config("mail." + name + ".filecache.dir: " + dir);
	if (dir != null)
	    fileCacheDir = new File(dir);
	keepMessageContent = getBoolProp("keepmessagecontent");

	// mail.pop3.starttls.enable enables use of STLS command
	useStartTLS = getBoolProp("starttls.enable");

	// mail.pop3.starttls.required requires use of STLS command
	requireStartTLS = getBoolProp("starttls.required");

	// mail.pop3.finalizecleanclose requires clean close when finalizing
	finalizeCleanClose = getBoolProp("finalizecleanclose");

	String s = session.getProperty("mail." + name + ".message.class");
	if (s != null) {
	    logger.log(Level.CONFIG, "message class: {0}", s);
	    try {
		ClassLoader cl = this.getClass().getClassLoader();

		// now load the class
		Class<?> messageClass = null;
		try {
		    // First try the "application's" class loader.
		    // This should eventually be replaced by
		    // Thread.currentThread().getContextClassLoader().
		    messageClass = Class.forName(s, false, cl);
		} catch (ClassNotFoundException ex1) {
		    // That didn't work, now try the "system" class loader.
		    // (Need both of these because JDK 1.1 class loaders
		    // may not delegate to their parent class loader.)
		    messageClass = Class.forName(s);
		}

		Class<?>[] c = {javax.mail.Folder.class, int.class};
		messageConstructor = messageClass.getConstructor(c);
	    } catch (Exception ex) {
		logger.log(Level.CONFIG, "failed to load message class", ex);
	    }
	}
    }

    /**
     * Get the value of a boolean property.
     * Print out the value if logging is enabled.
     */
    private final synchronized boolean getBoolProp(String prop) {
	prop = "mail." + name + "." + prop;
	boolean val = PropUtil.getBooleanProperty(session.getProperties(),
						    prop, false);
	if (logger.isLoggable(Level.CONFIG))
	    logger.config(prop + ": " + val);
	return val;
    }

    /**
     * Get a reference to the session.
     */
    synchronized Session getSession() {
        return session;
    }

    @Override
    protected synchronized boolean protocolConnect(String host, int portNum,
		String user, String passwd) throws MessagingException {
		    
	// check for non-null values of host, password, user
	if (host == null || passwd == null || user == null)
	    return false;

	// if port is not specified, set it to value of mail.pop3.port
        // property if it exists, otherwise default to 110
        if (portNum == -1)
	    portNum = PropUtil.getIntProperty(session.getProperties(),
				"mail." + name + ".port", -1);

	if (portNum == -1)
	    portNum = defaultPort;

	this.host = host;
	this.portNum = portNum;
	this.user = user;
	this.passwd = passwd;
	try {
	    port = getPort(null);
	} catch (EOFException eex) { 
		throw new AuthenticationFailedException(eex.getMessage());
	} catch (SocketConnectException scex) {
	    throw new MailConnectException(scex);
	} catch (IOException ioex) { 
	    throw new MessagingException("Connect failed", ioex);
	}

	return true;
    }

    /**
     * Check whether this store is connected. Override superclass
     * method, to actually ping our server connection.
     */
    /*
     * Note that we maintain somewhat of an illusion of being connected
     * even if we're not really connected.  This is because a Folder
     * can use the connection and close it when it's done.  If we then
     * ask whether the Store's connected we want the answer to be true,
     * as long as we can reconnect at that point.  This means that we
     * need to be able to reconnect the Store on demand.
     */
    @Override
    public synchronized boolean isConnected() {
	if (!super.isConnected())
	    // if we haven't been connected at all, don't bother with
	    // the NOOP.
	    return false;
	try {
	    if (port == null)
		port = getPort(null);
	    else if (!port.noop())
		throw new IOException("NOOP failed");
	    return true;
	} catch (IOException ioex) {
	    // no longer connected, close it down
	    try {
		super.close();		// notifies listeners
	    } catch (MessagingException mex) {
		// ignore it
	    }
	    return false;
	}
    }

    synchronized Protocol getPort(POP3Folder owner) throws IOException {
	Protocol p;

	// if we already have a port, remember who's using it
	if (port != null && portOwner == null) {
	    portOwner = owner;
	    return port;
	}

	// need a new port, create it and try to login
	p = new Protocol(host, portNum, logger,
	    session.getProperties(), "mail." + name, isSSL);

	if (useStartTLS || requireStartTLS) {
	    if (p.hasCapability("STLS")) {
		if (p.stls()) {
		    // success, refresh capabilities
		    p.setCapabilities(p.capa());
		} else if (requireStartTLS) {
		    logger.fine("STLS required but failed");
		    throw cleanupAndThrow(p,
			    new EOFException("STLS required but failed"));
		}
	    } else if (requireStartTLS) {
		logger.fine("STLS required but not supported");
		throw cleanupAndThrow(p,
			new EOFException("STLS required but not supported"));
	    }
	}

	capabilities = p.getCapabilities();	// save for later, may be null
	usingSSL = p.isSSL();			// in case anyone asks

	/*
	 * If we haven't explicitly disabled use of the TOP command,
	 * and the server has provided its capabilities,
	 * and the server doesn't support the TOP command,
	 * disable the TOP command.
	 */
	if (!disableTop &&
		capabilities != null && !capabilities.containsKey("TOP")) {
	    disableTop = true;
	    logger.fine("server doesn't support TOP, disabling it");
	}

	supportsUidl = capabilities == null || capabilities.containsKey("UIDL");

	String msg = null;
	if ((msg = p.login(user, passwd)) != null) {
	    throw cleanupAndThrow(p, new EOFException(msg));
	}

	/*
	 * If a Folder closes the port, and then a Folder
	 * is opened, the Store won't have a port.  In that
	 * case, the getPort call will come from Folder.open,
	 * but we need to keep track of the port in the Store
	 * so that a later call to Folder.isOpen, which calls
	 * Store.isConnected, will use the same port.
	 */
	if (port == null && owner != null) {
	    port = p;
	    portOwner = owner;
	}
	if (portOwner == null)
	    portOwner = owner;
	return p;
    }

    private static IOException cleanupAndThrow(Protocol p, IOException ife) {
	try {
	    p.quit();
	} catch (Throwable thr) {
	    if (isRecoverable(thr)) {
		ife.addSuppressed(thr);
	    } else {
		thr.addSuppressed(ife);
		if (thr instanceof Error) {
		    throw (Error) thr;
		}
		if (thr instanceof RuntimeException) {
		    throw (RuntimeException) thr;
		}
		throw new RuntimeException("unexpected exception", thr);
	    }
	}
	return ife;
    }

    private static boolean isRecoverable(Throwable t) {
	return (t instanceof Exception) || (t instanceof LinkageError);
    }

    synchronized void closePort(POP3Folder owner) {
	if (portOwner == owner) {
	    port = null;
	    portOwner = null;
	}
    }

    @Override
    public synchronized void close() throws MessagingException {
	close(false);
    }

    synchronized void close(boolean force) throws MessagingException {
	try {
	    if (port != null) {
		if (force)
		    port.close();
		else
		    port.quit();
	    }
	} catch (IOException ioex) {
	} finally {
	    port = null;

	    // to set the state and send the closed connection event
	    super.close();
	}
    }

    @Override
    public Folder getDefaultFolder() throws MessagingException {
	checkConnected();
	return new DefaultFolder(this);
    }

    /**
     * Only the name "INBOX" is supported.
     */
    @Override
    public Folder getFolder(String name) throws MessagingException {
	checkConnected();
	return new POP3Folder(this, name);
    }

    @Override
    public Folder getFolder(URLName url) throws MessagingException {
	checkConnected();
	return new POP3Folder(this, url.getFile());
    }

    /**
     * Return a Map of the capabilities the server provided,
     * as per RFC 2449.  If the server doesn't support RFC 2449,
     * an emtpy Map is returned.  The returned Map can not be modified.
     * The key to the Map is the upper case capability name as
     * a String.  The value of the entry is the entire String
     * capability line returned by the server. <p>
     *
     * For example, to check if the server supports the STLS capability, use:
     * <code>if (store.capabilities().containsKey("STLS")) ...</code>
     *
     * @return	Map of capabilities
     * @exception	MessagingException for failures
     * @since	JavaMail 1.4.3
     */
    public Map<String, String> capabilities() throws MessagingException {
	Map<String, String> c;
	synchronized (this) {
	    c = capabilities;
	}
	if (c != null)
	    return Collections.unmodifiableMap(c);
	else
	    return Collections.<String, String>emptyMap();
    }

    /**
     * Is this POP3Store using SSL to connect to the server?
     *
     * @return	true if using SSL
     * @since	JavaMail 1.4.6
     */
    public synchronized boolean isSSL() {
	return usingSSL;
    }

    @Override
    protected void finalize() throws Throwable {
	try {
	    if (port != null)	// don't force a connection attempt
		close(!finalizeCleanClose);
	} finally {
	    super.finalize();
	}
    }

    private void checkConnected() throws MessagingException {
	if (!super.isConnected())
	    throw new MessagingException("Not connected");
    }
}
