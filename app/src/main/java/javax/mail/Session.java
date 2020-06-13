/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.concurrent.Executor;

import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.DefaultProvider;

/**
 * The Session class represents a mail session and is not subclassed.
 * It collects together properties and defaults used by the mail API's.
 * A single default session can be shared by multiple applications on the
 * desktop.  Unshared sessions can also be created. <p>
 *
 * The Session class provides access to the protocol providers that
 * implement the <code>Store</code>, <code>Transport</code>, and related
 * classes.  The protocol providers are configured using the following files:
 * <ul>
 *  <li> <code>javamail.providers</code> and
 * 	<code>javamail.default.providers</code> </li>
 *  <li> <code>javamail.address.map</code> and
 * 	<code>javamail.default.address.map</code> </li>
 * </ul>
 * <p>
 * Each <code>javamail.</code><i>X</i> resource file is searched for using
 * three methods in the following order:
 * <ol>
 *  <li> <code><i>java.home</i>/<i>conf</i>/javamail.</code><i>X</i> </li>
 *  <li> <code>META-INF/javamail.</code><i>X</i> </li>
 *  <li> <code>META-INF/javamail.default.</code><i>X</i> </li>
 * </ol>
 * <p>
 * (Where <i>java.home</i> is the value of the "java.home" System property
 * and <i>conf</i> is the directory named "conf" if it exists,
 * otherwise the directory named "lib"; the "conf" directory was
 * introduced in JDK 1.9.)
 * <p>
 * The first method allows the user to include their own version of the
 * resource file by placing it in the <i>conf</i> directory where the
 * <code>java.home</code> property points.  The second method allows an
 * application that uses the Jakarta Mail APIs to include their own resource
 * files in their application's or jar file's <code>META-INF</code>
 * directory.  The <code>javamail.default.</code><i>X</i> default files
 * are part of the Jakarta Mail <code>mail.jar</code> file and should not be
 * supplied by users. <p>
 *
 * File location depends upon how the <code>ClassLoader</code> method
 * <code>getResource</code> is implemented.  Usually, the
 * <code>getResource</code> method searches through CLASSPATH until it
 * finds the requested file and then stops. <p>
 *
 * The ordering of entries in the resource files matters.  If multiple
 * entries exist, the first entries take precedence over the later
 * entries.  For example, the first IMAP provider found will be set as the
 * default IMAP implementation until explicitly changed by the
 * application.  The user- or system-supplied resource files augment, they
 * do not override, the default files included with the Jakarta Mail APIs.
 * This means that all entries in all files loaded will be available. <p>
 *
 * <b><code>javamail.providers</code></b> and
 * <b><code>javamail.default.providers</code></b><p>
 *
 * These resource files specify the stores and transports that are
 * available on the system, allowing an application to "discover" what
 * store and transport implementations are available.  The protocol
 * implementations are listed one per line.  The file format defines four
 * attributes that describe a protocol implementation.  Each attribute is
 * an "="-separated name-value pair with the name in lowercase. Each
 * name-value pair is semi-colon (";") separated.  The following names
 * are defined.
 *
 * <table border=1>
 * <caption>
 * Attribute Names in Providers Files
 * </caption>
 * <tr>
 * <th>Name</th><th>Description</th>
 * </tr>
 * <tr>
 * <td>protocol</td>
 * <td>Name assigned to protocol.
 * For example, <code>smtp</code> for Transport.</td>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td>Valid entries are <code>store</code> and <code>transport</code>.</td>
 * </tr>
 * <tr>
 * <td>class</td>
 * <td>Class name that implements this protocol.</td>
 * </tr>
 * <tr>
 * <td>vendor</td>
 * <td>Optional string identifying the vendor.</td>
 * </tr>
 * <tr>
 * <td>version</td>
 * <td>Optional string identifying the version.</td>
 * </tr>
 * </table><p>
 *
 * Here's an example of <code>META-INF/javamail.default.providers</code>
 * file contents:
 * <pre>
 * protocol=imap; type=store; class=com.sun.mail.imap.IMAPStore; vendor=Oracle;
 * protocol=smtp; type=transport; class=com.sun.mail.smtp.SMTPTransport; vendor=Oracle;
 * </pre><p>
 *
 * The current implementation also supports configuring providers using
 * the Java SE {@link java.util.ServiceLoader ServiceLoader} mechanism.
 * When creating your own provider, create a {@link Provider} subclass,
 * for example:
 * <pre>
 * package com.example;
 *
 * import javax.mail.Provider;
 *
 * public class MyProvider extends Provider {
 *     public MyProvider() {
 *         super(Provider.Type.STORE, "myprot", MyStore.class.getName(),
 *             "Example", null);
 *     }
 * }
 * </pre>
 * Then include a file named <code>META-INF/services/javax.mail.Provider</code>
 * in your jar file that lists the name of your Provider class:
 * <pre>
 * com.example.MyProvider
 * </pre>
 * <p>
 *
 * <b><code>javamail.address.map</code></b> and
 * <b><code>javamail.default.address.map</code></b><p>
 *
 * These resource files map transport address types to the transport
 * protocol.  The <code>getType</code> method of
 * <code>javax.mail.Address</code> returns the address type.  The
 * <code>javamail.address.map</code> file maps the transport type to the
 * protocol.  The file format is a series of name-value pairs.  Each key
 * name should correspond to an address type that is currently installed
 * on the system; there should also be an entry for each
 * <code>javax.mail.Address</code> implementation that is present if it is
 * to be used.  For example, the
 * <code>javax.mail.internet.InternetAddress</code> method
 * <code>getType</code> returns "rfc822". Each referenced protocol should
 * be installed on the system.  For the case of <code>news</code>, below,
 * the client should install a Transport provider supporting the nntp
 * protocol. <p>
 *
 * Here are the typical contents of a <code>javamail.address.map</code> file:
 * <pre>
 * rfc822=smtp
 * news=nntp
 * </pre>
 *
 * @author John Mani
 * @author Bill Shannon
 * @author Max Spivak
 */

public final class Session {

    private final Properties props;
    private final Authenticator authenticator;
    private final Hashtable<URLName, PasswordAuthentication> authTable
	    = new Hashtable<>();
    private boolean debug = false;
    private PrintStream out;			// debug output stream
    private MailLogger logger;
    private final List<Provider> providers = new ArrayList<>();
    private final Map<String, Provider> providersByProtocol = new HashMap<>();
    private final Map<String, Provider> providersByClassName = new HashMap<>();
    private final Properties addressMap = new Properties();
						// maps type to protocol
    // the queue of events to be delivered, if mail.event.scope===session
    private final EventQueue q;

    // The default session.
    private static Session defaultSession = null;

    private static final String confDir;

    static {
	String dir = null;
	try {
	    dir = AccessController.doPrivileged(
		new PrivilegedAction<String>() {
		    @Override
		    public String run() {
			String home = System.getProperty("java.home");
			String newdir = home + File.separator + "conf";
			File conf = new File(newdir);
			if (conf.exists())
			    return newdir + File.separator;
			else
			    return home + File.separator +
				    "lib" + File.separator;
		    }
		});
	} catch (Exception ex) {
	    // ignore any exceptions
	}
	confDir = dir;
    }

    // Constructor is not public
    private Session(Properties props, Authenticator authenticator) {
	this.props = props;
	this.authenticator = authenticator;

	if (Boolean.valueOf(props.getProperty("mail.debug")).booleanValue())
	    debug = true;

	initLogger();
	logger.log(Level.CONFIG, "Jakarta Mail version {0}", Version.version);

	// get the Class associated with the Authenticator
	Class<?> cl;
	if (authenticator != null)
	    cl = authenticator.getClass();
	else
	    cl = this.getClass();
	// load the resources
	loadProviders(cl);
	loadAddressMap(cl);
	q = new EventQueue((Executor)props.get("mail.event.executor"));
    }

    private final synchronized void initLogger() {
	logger = new MailLogger(this.getClass(), "DEBUG", debug, getDebugOut());
    }

    /**
     * Get a new Session object.
     *
     * @param	props	Properties object that hold relevant properties.<br>
     *                  It is expected that the client supplies values
     *                  for the properties listed in Appendix A of the
     *                  Jakarta Mail spec (particularly  mail.store.protocol, 
     *                  mail.transport.protocol, mail.host, mail.user, 
     *                  and mail.from) as the defaults are unlikely to 
     *                  work in all cases.
     * @param	authenticator Authenticator object used to call back to
     *			the application when a user name and password is
     *			needed.
     * @return		a new Session object
     * @see	javax.mail.Authenticator
     */
    public static Session getInstance(Properties props,
					Authenticator authenticator) {
	return new Session(props, authenticator);
    }

    /**
     * Get a new Session object.
     *
     * @param	props	Properties object that hold relevant properties.<br>
     *                  It is expected that the client supplies values
     *                  for the properties listed in Appendix A of the
     *                  Jakarta Mail spec (particularly  mail.store.protocol, 
     *                  mail.transport.protocol, mail.host, mail.user, 
     *                  and mail.from) as the defaults are unlikely to 
     *                  work in all cases.
     * @return		a new Session object
     * @since		JavaMail 1.2
     */
    public static Session getInstance(Properties props) {
	return new Session(props, null);
    }

    /**
     * Get the default Session object. If a default has not yet been
     * setup, a new Session object is created and installed as the 
     * default. <p>
     *
     * Since the default session is potentially available to all
     * code executing in the same Java virtual machine, and the session
     * can contain security sensitive information such as user names
     * and passwords, access to the default session is restricted.
     * The Authenticator object, which must be created by the caller,
     * is used indirectly to check access permission.  The Authenticator
     * object passed in when the session is created is compared with
     * the Authenticator object passed in to subsequent requests to
     * get the default session.  If both objects are the same, or are
     * from the same ClassLoader, the request is allowed.  Otherwise,
     * it is denied.  <p>
     *
     * Note that if the Authenticator object used to create the session
     * is null, anyone can get the default session by passing in null.  <p>
     *
     * Note also that the Properties object is used only the first time
     * this method is called, when a new Session object is created.
     * Subsequent calls return the Session object that was created by the
     * first call, and ignore the passed Properties object.  Use the
     * <code>getInstance</code> method to get a new Session object every
     * time the method is called. <p>
     *
     * Additional security Permission objects may be used to
     * control access to the default session. <p>
     *
     * In the current implementation, if a SecurityManager is set, the
     * caller must have the <code>RuntimePermission("setFactory")</code>
     * permission.
     *
     * @param	props	Properties object. Used only if a new Session
     *			object is created.<br>
     *                  It is expected that the client supplies values
     *                  for the properties listed in Appendix A of the
     *                  Jakarta Mail spec (particularly  mail.store.protocol, 
     *                  mail.transport.protocol, mail.host, mail.user, 
     *                  and mail.from) as the defaults are unlikely to 
     *                  work in all cases.
     * @param	authenticator Authenticator object.  Used only if a
     *			new Session object is created.  Otherwise, 
     *			it must match the Authenticator used to create
     *			the Session.
     * @return		the default Session object
     */
    public static synchronized Session getDefaultInstance(Properties props,
					Authenticator authenticator) {
	if (defaultSession == null) {
	    SecurityManager security = System.getSecurityManager();
	    if (security != null)
		security.checkSetFactory();
	    defaultSession = new Session(props, authenticator);
	} else {
	    // have to check whether caller is allowed to see default session
	    if (defaultSession.authenticator == authenticator)
		;	// either same object or both null, either way OK
	    else if (defaultSession.authenticator != null &&
		    authenticator != null &&
		    defaultSession.authenticator.getClass().getClassLoader() ==
			authenticator.getClass().getClassLoader())
		;	// both objects came from the same class loader, OK
	    else
		// anything else is not allowed
		throw new SecurityException("Access to default session denied");
	}

	return defaultSession;
    }

    /**
     * Get the default Session object. If a default has not yet been
     * setup, a new Session object is created and installed as the 
     * default. <p>
     *
     * Note that a default session created with no Authenticator is
     * available to all code executing in the same Java virtual
     * machine, and the session can contain security sensitive
     * information such as user names and passwords.
     *
     * @param	props	Properties object. Used only if a new Session
     *			object is created.<br>
     *                  It is expected that the client supplies values
     *                  for the properties listed in Appendix A of the
     *                  Jakarta Mail spec (particularly  mail.store.protocol, 
     *                  mail.transport.protocol, mail.host, mail.user, 
     *                  and mail.from) as the defaults are unlikely to 
     *                  work in all cases.
     * @return		the default Session object
     * @since		JavaMail 1.2
     */
    public static Session getDefaultInstance(Properties props) {
        return getDefaultInstance(props, null);
    }

    /**
     * Set the debug setting for this Session.
     * <p>
     * Since the debug setting can be turned on only after the Session
     * has been created, to turn on debugging in the Session
     * constructor, set the property <code>mail.debug</code> in the
     * Properties object passed in to the constructor to true.  The
     * value of the <code>mail.debug</code> property is used to
     * initialize the per-Session debugging flag.  Subsequent calls to
     * the <code>setDebug</code> method manipulate the per-Session
     * debugging flag and have no affect on the <code>mail.debug</code>
     * property.
     *
     * @param debug	Debug setting
     */
    public synchronized void setDebug(boolean debug) {
	this.debug = debug;
	initLogger();
	logger.log(Level.CONFIG, "setDebug: Jakarta Mail version {0}",
				    Version.version);
    }

    /**
     * Get the debug setting for this Session.
     *
     * @return current debug setting
     */
    public synchronized boolean getDebug() {
	return debug;
    }

    /**
     * Set the stream to be used for debugging output for this session.
     * If <code>out</code> is null, <code>System.out</code> will be used.
     * Note that debugging output that occurs before any session is created,
     * as a result of setting the <code>mail.debug</code> system property,
     * will always be sent to <code>System.out</code>.
     *
     * @param	out	the PrintStream to use for debugging output
     * @since		JavaMail 1.3
     */
    public synchronized void setDebugOut(PrintStream out) {
	this.out = out;
	initLogger();
    }

    /**
     * Returns the stream to be used for debugging output.  If no stream
     * has been set, <code>System.out</code> is returned.
     *
     * @return		the PrintStream to use for debugging output
     * @since		JavaMail 1.3
     */
    public synchronized PrintStream getDebugOut() {
	if (out == null)
	    return System.out;
	else
	    return out;
    }

    /**
     * This method returns an array of all the implementations installed 
     * via the javamail.[default.]providers files that can
     * be loaded using the ClassLoader available to this application.
     *
     * @return Array of configured providers
     */
    public synchronized Provider[] getProviders() {
	Provider[] _providers = new Provider[providers.size()];
	providers.toArray(_providers);
	return _providers;
    }

    /**
     * Returns the default Provider for the protocol
     * specified. Checks mail.&lt;protocol&gt;.class property
     * first and if it exists, returns the Provider
     * associated with this implementation. If it doesn't exist, 
     * returns the Provider that appeared first in the 
     * configuration files. If an implementation for the protocol 
     * isn't found, throws NoSuchProviderException
     *
     * @param  protocol  Configured protocol (i.e. smtp, imap, etc)
     * @return Currently configured Provider for the specified protocol
     * @exception	NoSuchProviderException If a provider for the given
     *			protocol is not found.
     */
    public synchronized Provider getProvider(String protocol)
	                                throws NoSuchProviderException {

	if (protocol == null || protocol.length() <= 0) {
	    throw new NoSuchProviderException("Invalid protocol: null");
	}

	Provider _provider = null;

	// check if the mail.<protocol>.class property exists
	String _className = props.getProperty("mail."+protocol+".class");
	if (_className != null) {
	    if (logger.isLoggable(Level.FINE)) {
		logger.fine("mail."+protocol+
				   ".class property exists and points to " + 
				   _className);
	    }
	    _provider = providersByClassName.get(_className);
	} 

	if (_provider != null) {
	    return _provider;
	} else {
	    // returning currently default protocol in providersByProtocol
	    _provider = providersByProtocol.get(protocol);
	}

	if (_provider == null) {
	    throw new NoSuchProviderException("No provider for " + protocol);
	} else {
	    if (logger.isLoggable(Level.FINE)) {
		logger.fine("getProvider() returning " + _provider.toString());
	    }
	    return _provider;
	}
    }

    /**
     * Set the passed Provider to be the default implementation
     * for the protocol in Provider.protocol overriding any previous values.
     *
     * @param provider Currently configured Provider which will be 
     * set as the default for the protocol
     * @exception	NoSuchProviderException If the provider passed in
     *			is invalid.
     */
    public synchronized void setProvider(Provider provider)
				throws NoSuchProviderException {
	if (provider == null) {
	    throw new NoSuchProviderException("Can't set null provider");
	}
	providersByProtocol.put(provider.getProtocol(), provider);
	providersByClassName.put(provider.getClassName(), provider);
	props.put("mail." + provider.getProtocol() + ".class", 
		  provider.getClassName());
    }


    /**
     * Get a Store object that implements this user's desired Store
     * protocol. The <code>mail.store.protocol</code> property specifies the
     * desired protocol. If an appropriate Store object is not obtained, 
     * NoSuchProviderException is thrown
     *
     * @return 		a Store object 
     * @exception	NoSuchProviderException If a provider for the given
     *			protocol is not found.
     */
    public Store getStore() throws NoSuchProviderException {
	return getStore(getProperty("mail.store.protocol"));
    }

    /**
     * Get a Store object that implements the specified protocol. If an
     * appropriate Store object cannot be obtained, 
     * NoSuchProviderException is thrown.
     *
     * @param	        protocol	the Store protocol
     * @return		a Store object 
     * @exception	NoSuchProviderException If a provider for the given
     *			protocol is not found.
     */
    public Store getStore(String protocol) throws NoSuchProviderException {
	return getStore(new URLName(protocol, null, -1, null, null, null));
    }


    /**
     * Get a Store object for the given URLName. If the requested Store
     * object cannot be obtained, NoSuchProviderException is thrown.
     *
     * The "scheme" part of the URL string (Refer RFC 1738) is used 
     * to locate the Store protocol. <p>
     *
     * @param	url	URLName that represents the desired Store
     * @return		a closed Store object
     * @see		#getFolder(URLName)
     * @see		javax.mail.URLName
     * @exception	NoSuchProviderException If a provider for the given
     *			URLName is not found.
     */
    public Store getStore(URLName url) throws NoSuchProviderException {
	String protocol = url.getProtocol();
	Provider p = getProvider(protocol);
	return getStore(p, url);
    }

    /**
     * Get an instance of the store specified by Provider. Instantiates
     * the store and returns it.
     * 
     * @param provider Store Provider that will be instantiated
     * @return Instantiated Store
     * @exception	NoSuchProviderException If a provider for the given
     *			Provider is not found.
     */
    public Store getStore(Provider provider) throws NoSuchProviderException {
	return getStore(provider, null);
    }


    /**
     * Get an instance of the store specified by Provider. If the URLName
     * is not null, uses it, otherwise creates a new one. Instantiates
     * the store and returns it. This is a private method used by
     * getStore(Provider) and getStore(URLName)
     * 
     * @param provider Store Provider that will be instantiated
     * @param url      URLName used to instantiate the Store
     * @return Instantiated Store
     * @exception	NoSuchProviderException If a provider for the given
     *			Provider/URLName is not found.
     */
    private Store getStore(Provider provider, URLName url) 
	throws NoSuchProviderException {

	// make sure we have the correct type of provider
	if (provider == null || provider.getType() != Provider.Type.STORE ) {
	    throw new NoSuchProviderException("invalid provider");
	}

	return getService(provider, url, Store.class);
    }

    /**
     * Get a closed Folder object for the given URLName. If the requested
     * Folder object cannot be obtained, null is returned. <p>
     *
     * The "scheme" part of the URL string (Refer RFC 1738) is used
     * to locate the Store protocol. The rest of the URL string (that is,
     * the "schemepart", as per RFC 1738) is used by that Store
     * in a protocol dependent manner to locate and instantiate the
     * appropriate Folder object. <p>
     *
     * Note that RFC 1738 also specifies the syntax for the 
     * "schemepart" for IP-based protocols (IMAP4, POP3, etc.).
     * Providers of IP-based mail Stores should implement that
     * syntax for referring to Folders. <p>
     *
     * @param	url	URLName that represents the desired folder
     * @return		Folder
     * @see		#getStore(URLName)
     * @see		javax.mail.URLName
     * @exception	NoSuchProviderException If a provider for the given
     *			URLName is not found.
     * @exception	MessagingException if the Folder could not be 
     *			located or created.
     */
    public Folder getFolder(URLName url)
		throws MessagingException {
	// First get the Store
	Store store = getStore(url);
	store.connect();
	return store.getFolder(url);
    }

    /**
     * Get a Transport object that implements this user's desired 
     * Transport protcol. The <code>mail.transport.protocol</code> property 
     * specifies the desired protocol. If an appropriate Transport 
     * object cannot be obtained, MessagingException is thrown.
     *
     * @return 		a Transport object 
     * @exception	NoSuchProviderException If the provider is not found.
     */
    public Transport getTransport() throws NoSuchProviderException {
	String prot = getProperty("mail.transport.protocol");
	if (prot != null)
	    return getTransport(prot);
	// if the property isn't set, use the protocol for "rfc822"
	prot = (String)addressMap.get("rfc822");
	if (prot != null)
	    return getTransport(prot);
	return getTransport("smtp");	// if all else fails
    }

    /**
     * Get a Transport object that implements the specified protocol.
     * If an appropriate Transport object cannot be obtained, null is
     * returned.
     *
     * @param	protocol the Transport protocol
     * @return 		a Transport object 
     * @exception	NoSuchProviderException If provider for the given
     *			protocol is not found.
     */
    public Transport getTransport(String protocol)
				throws NoSuchProviderException {
	return getTransport(new URLName(protocol, null, -1, null, null, null));
    }


    /**
     * Get a Transport object for the given URLName. If the requested 
     * Transport object cannot be obtained, NoSuchProviderException is thrown.
     *
     * The "scheme" part of the URL string (Refer RFC 1738) is used 
     * to locate the Transport protocol. <p>
     *
     * @param	url	URLName that represents the desired Transport
     * @return		a closed Transport object
     * @see		javax.mail.URLName
     * @exception	NoSuchProviderException If a provider for the given
     *			URLName is not found.
     */
    public Transport getTransport(URLName url) throws NoSuchProviderException {
	String protocol = url.getProtocol();
	Provider p = getProvider(protocol);
	return getTransport(p, url);
    }

    /**
     * Get an instance of the transport specified in the Provider. Instantiates
     * the transport and returns it.
     * 
     * @param provider Transport Provider that will be instantiated
     * @return Instantiated Transport
     * @exception	NoSuchProviderException If provider for the given
     *			provider is not found.
     */
    public Transport getTransport(Provider provider) 
	                                     throws NoSuchProviderException {
	return getTransport(provider, null);
    }

    /**
     * Get a Transport object that can transport a Message of the
     * specified address type.
     *
     * @param	address	an address for which a Transport is needed
     * @return	A Transport object
     * @see javax.mail.Address
     * @exception	NoSuchProviderException If provider for the 
     *			Address type is not found
     */
    public Transport getTransport(Address address) 
	                                     throws NoSuchProviderException {

	String transportProtocol;
	transportProtocol =
	    getProperty("mail.transport.protocol." + address.getType());
	if (transportProtocol != null)
	    return getTransport(transportProtocol);
	transportProtocol = (String)addressMap.get(address.getType());
	if (transportProtocol != null)
	    return getTransport(transportProtocol);
	throw new NoSuchProviderException("No provider for Address type: "+
						address.getType());
    }

    /**
     * Get a Transport object using the given provider and urlname.
     *
     * @param	provider	the provider to use
     * @param	url		urlname to use (can be null)
     * @return A Transport object
     * @exception	NoSuchProviderException	If no provider or the provider
     *			was the wrong class.	
     */

    private Transport getTransport(Provider provider, URLName url)
					throws NoSuchProviderException {
	// make sure we have the correct type of provider
	if (provider == null || provider.getType() != Provider.Type.TRANSPORT) {
	    throw new NoSuchProviderException("invalid provider");
	}

	return getService(provider, url, Transport.class);
    }

    /**
     * Get a Service object.  Needs a provider object, but will
     * create a URLName if needed.  It attempts to instantiate
     * the correct class.
     *
     * @param provider	which provider to use
     * @param url	which URLName to use (can be null)
     * @param type	the service type (class)
     * @exception	NoSuchProviderException	thrown when the class cannot be
     *			found or when it does not have the correct constructor
     *			(Session, URLName), or if it is not derived from
     *			Service.
     */
    private <T extends Service> T getService(Provider provider, URLName url,
					Class<T> type)
					throws NoSuchProviderException {
	// need a provider and url
	if (provider == null) {
	    throw new NoSuchProviderException("null");
	}

	// create a url if needed
	if (url == null) {
	    url = new URLName(provider.getProtocol(), null, -1, 
			      null, null, null);
	}

	Object service = null;
	
	// get the ClassLoader associated with the Authenticator
	ClassLoader cl;
	if (authenticator != null)
	    cl = authenticator.getClass().getClassLoader();
	else
	    cl = this.getClass().getClassLoader();

	// now load the class
	Class<?> serviceClass = null;
	try {
	    // First try the "application's" class loader.
	    ClassLoader ccl = getContextClassLoader();
	    if (ccl != null)
		try {
		    serviceClass =
			Class.forName(provider.getClassName(), false, ccl);
		} catch (ClassNotFoundException ex) {
		    // ignore it
		}
	    if (serviceClass == null || !type.isAssignableFrom(serviceClass))
		serviceClass =
		    Class.forName(provider.getClassName(), false, cl);
            
	    if (!type.isAssignableFrom(serviceClass))
		throw new ClassCastException(
				type.getName() + " " + serviceClass.getName());
	} catch (Exception ex1) {
	    // That didn't work, now try the "system" class loader.
	    // (Need both of these because JDK 1.1 class loaders
	    // may not delegate to their parent class loader.)
	    try {
		serviceClass = Class.forName(provider.getClassName());
		if (!type.isAssignableFrom(serviceClass))
		    throw new ClassCastException(
				type.getName() + " " + serviceClass.getName());
	    } catch (Exception ex) {
		// Nothing worked, give up.
		logger.log(Level.FINE, "Exception loading provider", ex);
		throw new NoSuchProviderException(provider.getProtocol());
	    }
	}

	// construct an instance of the class
	try {
	    Class<?>[] c = {javax.mail.Session.class, javax.mail.URLName.class};
	    Constructor<?> cons = serviceClass.getConstructor(c);

	    Object[] o = {this, url};
	    service = cons.newInstance(o);

	} catch (Exception ex) {
	    logger.log(Level.FINE, "Exception loading provider", ex);
	    throw new NoSuchProviderException(provider.getProtocol());
	}

	return type.cast(service);
    }

    /**
     * Save a PasswordAuthentication for this (store or transport) URLName.
     * If pw is null the entry corresponding to the URLName is removed.
     * <p>
     * This is normally used only by the store or transport implementations
     * to allow authentication information to be shared among multiple
     * uses of a session.
     *
     * @param	url	the URLName
     * @param	pw	the PasswordAuthentication to save
     */
    public void setPasswordAuthentication(URLName url,
					  PasswordAuthentication pw) {
	if (pw == null)
	    authTable.remove(url);
	else
	    authTable.put(url, pw);
    }

    /**
     * Return any saved PasswordAuthentication for this (store or transport)
     * URLName.  Normally used only by store or transport implementations.
     *
     * @param	url	the URLName
     * @return	the PasswordAuthentication corresponding to the URLName
     */
    public PasswordAuthentication getPasswordAuthentication(URLName url) {
	return authTable.get(url);
    }

    /**
     * Call back to the application to get the needed user name and password.
     * The application should put up a dialog something like:
     * <pre>
     * Connecting to &lt;protocol&gt; mail service on host &lt;addr&gt;, port &lt;port&gt;.
     * &lt;prompt&gt;
     *
     * User Name: &lt;defaultUserName&gt;
     * Password:
     * </pre>
     *
     * @param	addr		InetAddress of the host.  may be null.
     * @param	port		the port on the host
     * @param	protocol	protocol scheme (e.g. imap, pop3, etc.)
     * @param	prompt		any additional String to show as part of
     *                          the prompt; may be null.
     * @param	defaultUserName	the default username. may be null.
     * @return	the authentication which was collected by the authenticator; 
     *          may be null.
     */
    public PasswordAuthentication requestPasswordAuthentication(
	InetAddress addr, int port,
	String protocol, String prompt, String defaultUserName) {

	if (authenticator != null) {
	    return authenticator.requestPasswordAuthentication(
		addr, port, protocol, prompt, defaultUserName);
	} else {
	    return null;
	}
    }

    /**
     * Returns the Properties object associated with this Session
     *
     * @return		Properties object
     */
    public Properties getProperties() { 
   	return props; 
    }

    /**
     * Returns the value of the specified property. Returns null
     * if this property does not exist.
     *
     * @param	name	the property name
     * @return		String that is the property value
     */
    public String getProperty(String name) { 
   	return props.getProperty(name); 
    }

    /**
     * Load the protocol providers config files.
     */
    private void loadProviders(Class<?> cl) {
	StreamLoader loader = new StreamLoader() {
	    @Override
	    public void load(InputStream is) throws IOException {
		loadProvidersFromStream(is);
	    }
	};

	// load system-wide javamail.providers from the
	// <java.home>/{conf,lib} directory
	try {
	    if (confDir != null)
		loadFile(confDir + "javamail.providers", loader);
	} catch (SecurityException ex) {}

	// next, add all the non-default services
	ServiceLoader<Provider> sl = ServiceLoader.load(Provider.class);
	for (Provider p : sl) {
	    if (!p.getClass().isAnnotationPresent(DefaultProvider.class))
		addProvider(p);
	}

	// load the META-INF/javamail.providers file supplied by an application
	loadAllResources("META-INF/javamail.providers", cl, loader);

	// load default META-INF/javamail.default.providers from mail.jar file
	loadResource("/META-INF/javamail.default.providers", cl, loader, false);

	// finally, add all the default services
	sl = ServiceLoader.load(Provider.class);
	for (Provider p : sl) {
	    if (p.getClass().isAnnotationPresent(DefaultProvider.class))
		addProvider(p);
	}

	/*
	 * If we haven't loaded any providers, fake it.
	 */
	if (providers.size() == 0) {
	    logger.config("failed to load any providers, using defaults");
	    // failed to load any providers, initialize with our defaults
	    addProvider(new Provider(Provider.Type.STORE,
			"imap", "com.sun.mail.imap.IMAPStore",
			"Oracle", Version.version));
	    addProvider(new Provider(Provider.Type.STORE,
			"imaps", "com.sun.mail.imap.IMAPSSLStore",
			"Oracle", Version.version));
	    addProvider(new Provider(Provider.Type.STORE,
			"pop3", "com.sun.mail.pop3.POP3Store",
			"Oracle", Version.version));
	    addProvider(new Provider(Provider.Type.STORE,
			"pop3s", "com.sun.mail.pop3.POP3SSLStore",
			"Oracle", Version.version));
	    addProvider(new Provider(Provider.Type.TRANSPORT,
			"smtp", "com.sun.mail.smtp.SMTPTransport",
			"Oracle", Version.version));
	    addProvider(new Provider(Provider.Type.TRANSPORT,
			"smtps", "com.sun.mail.smtp.SMTPSSLTransport",
			"Oracle", Version.version));
	}

	if (logger.isLoggable(Level.CONFIG)) {
	    // dump the output of the tables for debugging
	    logger.config("Tables of loaded providers");
	    logger.config("Providers Listed By Class Name: " + 
	       providersByClassName.toString());
	    logger.config("Providers Listed By Protocol: " + 
	       providersByProtocol.toString());
	}
    }

    private void loadProvidersFromStream(InputStream is) 
				throws IOException {
	if (is != null) {
	    LineInputStream lis = new LineInputStream(is);
	    String currLine;

	    // load and process one line at a time using LineInputStream
	    while ((currLine = lis.readLine()) != null) {

		if (currLine.startsWith("#"))
		    continue;
		if (currLine.trim().length() == 0)
		    continue;	// skip blank line
		Provider.Type type = null;
		String protocol = null, className = null;
		String vendor = null, version = null;
		    
		// separate line into key-value tuples
		StringTokenizer tuples = new StringTokenizer(currLine,";");
		while (tuples.hasMoreTokens()) {
		    String currTuple = tuples.nextToken().trim();
			
		    // set the value of each attribute based on its key
		    int sep = currTuple.indexOf("=");
		    if (currTuple.startsWith("protocol=")) {
			protocol = currTuple.substring(sep+1);
		    } else if (currTuple.startsWith("type=")) {
			String strType = currTuple.substring(sep+1);
			if (strType.equalsIgnoreCase("store")) {
			    type = Provider.Type.STORE;
			} else if (strType.equalsIgnoreCase("transport")) {
			    type = Provider.Type.TRANSPORT;
		    	}
		    } else if (currTuple.startsWith("class=")) {
			className = currTuple.substring(sep+1);
		    } else if (currTuple.startsWith("vendor=")) {
			vendor = currTuple.substring(sep+1);
		    } else if (currTuple.startsWith("version=")) {
			version = currTuple.substring(sep+1);
		    }
		}

		// check if a valid Provider; else, continue
		if (type == null || protocol == null || className == null 
		    || protocol.length() <= 0 || className.length() <= 0) {
			
		    logger.log(Level.CONFIG, "Bad provider entry: {0}",
						currLine);
		    continue;
		}
		Provider provider = new Provider(type, protocol, className,
					         vendor, version);

		// add the newly-created Provider to the lookup tables
		addProvider(provider);
	    }
	}
    }

    /**
     * Add a provider to the session.
     *
     * @param	provider	the provider to add
     * @since	JavaMail 1.4
     */
    public synchronized void addProvider(Provider provider) {
	providers.add(provider);
	providersByClassName.put(provider.getClassName(), provider);
	if (!providersByProtocol.containsKey(provider.getProtocol()))
	    providersByProtocol.put(provider.getProtocol(), provider);
    }

    // load maps in reverse order of preference so that the preferred
    // map is loaded last since its entries will override the previous ones
    private void loadAddressMap(Class<?> cl) {
	StreamLoader loader = new StreamLoader() {
	    @Override
	    public void load(InputStream is) throws IOException {
		addressMap.load(is);
	    }
	};

	// load default META-INF/javamail.default.address.map from mail.jar
	loadResource("/META-INF/javamail.default.address.map", cl, loader, true);

	// load the META-INF/javamail.address.map file supplied by an app
	loadAllResources("META-INF/javamail.address.map", cl, loader);

	// load system-wide javamail.address.map from the
	// <java.home>/{conf,lib} directory
	try {
	    if (confDir != null)
		loadFile(confDir + "javamail.address.map", loader);
	} catch (SecurityException ex) {}

	if (addressMap.isEmpty()) {
	    logger.config("failed to load address map, using defaults");
	    addressMap.put("rfc822", "smtp");
	}
    }

    /**
     * Set the default transport protocol to use for addresses of
     * the specified type.  Normally the default is set by the
     * <code>javamail.default.address.map</code> or
     * <code>javamail.address.map</code> files or resources.
     *
     * @param	addresstype	type of address
     * @param	protocol	name of protocol
     * @see #getTransport(Address)
     * @since	JavaMail 1.4
     */
    public synchronized void setProtocolForAddress(String addresstype,
				String protocol) {
	if (protocol == null)
	    addressMap.remove(addresstype);
	else
	    addressMap.put(addresstype, protocol);
    }

    /**
     * Load from the named file.
     */
    private void loadFile(String name, StreamLoader loader) {
	InputStream clis = null;
	try {
	    clis = new BufferedInputStream(new FileInputStream(name));
	    loader.load(clis);
	    logger.log(Level.CONFIG, "successfully loaded file: {0}", name);
	} catch (FileNotFoundException fex) {
	    // ignore it
	} catch (IOException e) {
	    if (logger.isLoggable(Level.CONFIG))
		logger.log(Level.CONFIG, "not loading file: " + name, e);
	} catch (SecurityException sex) {
	    if (logger.isLoggable(Level.CONFIG))
		logger.log(Level.CONFIG, "not loading file: " + name, sex);
	} finally {
	    try {
		if (clis != null)
		    clis.close();
	    } catch (IOException ex) { }	// ignore it
	}
    }

    /**
     * Load from the named resource.
     */
    private void loadResource(String name, Class<?> cl, StreamLoader loader,
				boolean expected) {
	InputStream clis = null;
	try {
	    clis = getResourceAsStream(cl, name);
	    if (clis != null) {
		loader.load(clis);
		logger.log(Level.CONFIG, "successfully loaded resource: {0}",
					    name);
	    } else {
		if (expected)
		    logger.log(Level.WARNING,
				    "expected resource not found: {0}", name);
	    }
	} catch (IOException e) {
	    logger.log(Level.CONFIG, "Exception loading resource", e);
	} catch (SecurityException sex) {
	    logger.log(Level.CONFIG, "Exception loading resource", sex);
	} finally {
	    try {
		if (clis != null)
		    clis.close();
	    } catch (IOException ex) { }	// ignore it
	}
    }

    /**
     * Load all of the named resource.
     */
    private void loadAllResources(String name, Class<?> cl,
	    StreamLoader loader) {
	boolean anyLoaded = false;
	try {
	    URL[] urls;
	    ClassLoader cld = null;
	    // First try the "application's" class loader.
	    cld = getContextClassLoader();
	    if (cld == null)
		cld = cl.getClassLoader();
	    if (cld != null)
		urls = getResources(cld, name);
	    else
		urls = getSystemResources(name);
	    if (urls != null) {
		for (int i = 0; i < urls.length; i++) {
		    URL url = urls[i];
		    InputStream clis = null;
		    logger.log(Level.CONFIG, "URL {0}", url);
		    try {
			clis = openStream(url);
			if (clis != null) {
			    loader.load(clis);
			    anyLoaded = true;
			    logger.log(Level.CONFIG,
				"successfully loaded resource: {0}", url);
			} else {
			    logger.log(Level.CONFIG,
				"not loading resource: {0}", url);
			}
		    } catch (FileNotFoundException fex) {
			// ignore it
		    } catch (IOException ioex) {
			logger.log(Level.CONFIG, "Exception loading resource",
				    ioex);
		    } catch (SecurityException sex) {
			logger.log(Level.CONFIG, "Exception loading resource",
				    sex);
		    } finally {
			try {
			    if (clis != null)
				clis.close();
			} catch (IOException cex) { }
		    }
		}
	    }
	} catch (Exception ex) {
	    logger.log(Level.CONFIG, "Exception loading resource", ex);
	}

	// if failed to load anything, fall back to old technique, just in case
	if (!anyLoaded) {
	    /*
	    logger.config("!anyLoaded");
	    */
	    loadResource("/" + name, cl, loader, false);
	}
    }

    /*
     * Following are security related methods that work on JDK 1.2 or newer.
     */

    static ClassLoader getContextClassLoader() {
	return AccessController.doPrivileged(
		new PrivilegedAction<ClassLoader>() {
		    @Override
		    public ClassLoader run() {
			ClassLoader cl = null;
			try {
			    cl = Thread.currentThread().getContextClassLoader();
			} catch (SecurityException ex) {
			}
			return cl;
		    }
		}
	);
    }

    private static InputStream getResourceAsStream(final Class<?> c,
				final String name) throws IOException {
	try {
	    return AccessController.doPrivileged(
		    new PrivilegedExceptionAction<InputStream>() {
			@Override
			public InputStream run() throws IOException {
			    try {
				return c.getResourceAsStream(name);
			    } catch (RuntimeException e) {
				// gracefully handle ClassLoader bugs (Tomcat)
				IOException ioex = new IOException(
				    "ClassLoader.getResourceAsStream failed");
				ioex.initCause(e);
				throw ioex;
			    }
			}
		    }
	    );
	} catch (PrivilegedActionException e) {
	    throw (IOException)e.getException();
	}
    }

    private static URL[] getResources(final ClassLoader cl, final String name) {
	return AccessController.doPrivileged(new PrivilegedAction<URL[]>() {
	    @Override
	    public URL[] run() {
		URL[] ret = null;
		try {
		    List<URL> v = Collections.list(cl.getResources(name));
		    if (!v.isEmpty()) {
			ret = new URL[v.size()];
			v.toArray(ret);
		    }
		} catch (IOException ioex) {
		} catch (SecurityException ex) { }
		return ret;
	    }
	});
    }

    private static URL[] getSystemResources(final String name) {
	return AccessController.doPrivileged(new PrivilegedAction<URL[]>() {
	    @Override
	    public URL[] run() {
		URL[] ret = null;
		try {
		    List<URL> v = Collections.list(
			    ClassLoader.getSystemResources(name));
		    if (!v.isEmpty()) {
			ret = new URL[v.size()];
			v.toArray(ret);
		    }
		} catch (IOException ioex) {
		} catch (SecurityException ex) { }
		return ret;
	    }
	});
    }

    private static InputStream openStream(final URL url) throws IOException {
	try {
	    return AccessController.doPrivileged(
		    new PrivilegedExceptionAction<InputStream>() {
			@Override
			public InputStream run() throws IOException {
			    return url.openStream();
			}
		    }
	    );
	} catch (PrivilegedActionException e) {
	    throw (IOException)e.getException();
	}
    }

    EventQueue getEventQueue() {
	return q;
    }
}

/**
 * Support interface to generalize
 * code that loads resources from stream.
 */
interface StreamLoader {
    public void load(InputStream is) throws IOException;
}
