/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.smtp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SSLSocket;

import javax.mail.*;
import javax.mail.event.*;
import javax.mail.internet.*;

import com.sun.mail.util.PropUtil;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.MailConnectException;
import com.sun.mail.util.SocketConnectException;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;
import com.sun.mail.auth.Ntlm;

/**
 * This class implements the Transport abstract class using SMTP for
 * message submission and transport. <p>
 *
 * See the <a href="package-summary.html">com.sun.mail.smtp</a> package
 * documentation for further information on the SMTP protocol provider. <p>
 *
 * This class includes many protected methods that allow a subclass to
 * extend this class and add support for non-standard SMTP commands.
 * The {@link #issueCommand} and {@link #sendCommand} methods can be
 * used to send simple SMTP commands.  Other methods such as the
 * {@link #mailFrom} and {@link #data} methods can be overridden to
 * insert new commands before or after the corresponding SMTP commands.
 * For example, a subclass could do this to send the XACT command
 * before sending the DATA command:
 * <pre>
 *	protected OutputStream data() throws MessagingException {
 *	    if (supportsExtension("XACCOUNTING"))
 *	        issueCommand("XACT", 25);
 *	    return super.data();
 *	}
 * </pre>
 *
 * @author Max Spivak
 * @author Bill Shannon
 * @author Dean Gibson (DIGEST-MD5 authentication)
 * @author Lu\u00EDs Serralheiro (NTLM authentication)
 *
 * @see javax.mail.event.ConnectionEvent
 * @see javax.mail.event.TransportEvent
 */

public class SMTPTransport extends Transport {

    private String name = "smtp";	// Name of this protocol
    private int defaultPort = 25;	// default SMTP port
    private boolean isSSL = false;	// use SSL?
    private String host;		// host we're connected to

    // Following fields valid only during the sendMessage method.
    private MimeMessage message;	// Message to be sent
    private Address[] addresses;	// Addresses to which to send the msg
    // Valid sent, valid unsent and invalid addresses
    private Address[] validSentAddr, validUnsentAddr, invalidAddr;
    // Did we send the message even though some addresses were invalid?
    private boolean sendPartiallyFailed = false;
    // If so, here's an exception we need to throw
    private MessagingException exception;
    // stream where message data is written
    private SMTPOutputStream dataStream;

    // Map of SMTP service extensions supported by server, if EHLO used.
    private Hashtable<String, String> extMap;

    private Map<String, Authenticator> authenticators
	    = new HashMap<>();
    private String defaultAuthenticationMechanisms;	// set in constructor

    private boolean quitWait = false;	// true if we should wait
    private boolean quitOnSessionReject = false;   // true if we should send quit when session initiation is rejected

    private String saslRealm = UNKNOWN;
    private String authorizationID = UNKNOWN;
    private boolean enableSASL = false;	// enable SASL authentication
    private boolean useCanonicalHostName = false; // use canonical host name?
    private String[] saslMechanisms = UNKNOWN_SA;

    private String ntlmDomain = UNKNOWN; // for ntlm authentication

    private boolean reportSuccess;	// throw an exception even on success
    private boolean useStartTLS;	// use STARTTLS command
    private boolean requireStartTLS;	// require STARTTLS command
    private boolean useRset;		// use RSET instead of NOOP
    private boolean noopStrict = true;	// NOOP must return 250 for success

    private MailLogger logger;		// debug logger
    private MailLogger traceLogger;	// protocol trace logger
    private String localHostName;	// our own host name
    private String lastServerResponse;	// last SMTP response
    private int lastReturnCode;		// last SMTP return code
    private boolean notificationDone;	// only notify once per send

    private SaslAuthenticator saslAuthenticator; // if SASL is being used

    private boolean noauthdebug = true;	// hide auth info in debug output
    private boolean debugusername;	// include username in debug output?
    private boolean debugpassword;	// include password in debug output?
    private boolean allowutf8;		// allow UTF-8 usernames and passwords?
    private int chunkSize;		// chunk size if CHUNKING supported

    /** Headers that should not be included when sending */
    private static final String[] ignoreList = { "Bcc", "Content-Length" };
    private static final byte[] CRLF = { (byte)'\r', (byte)'\n' };
    private static final String UNKNOWN = "UNKNOWN";	// place holder
    private static final String[] UNKNOWN_SA = new String[0]; // place holder

    /**
     * Constructor that takes a Session object and a URLName
     * that represents a specific SMTP server.
     *
     * @param	session	the Session
     * @param	urlname	the URLName of this transport
     */
    public SMTPTransport(Session session, URLName urlname) {
	this(session, urlname, "smtp", false);
    }

    /**
     * Constructor used by this class and by SMTPSSLTransport subclass.
     *
     * @param	session	the Session
     * @param	urlname	the URLName of this transport
     * @param	name	the protocol name of this transport
     * @param	isSSL	use SSL to connect?
     */
    protected SMTPTransport(Session session, URLName urlname,
				String name, boolean isSSL) {
	super(session, urlname);
	Properties props = session.getProperties();

	logger = new MailLogger(this.getClass(), "DEBUG SMTP",
				session.getDebug(), session.getDebugOut());
	traceLogger = logger.getSubLogger("protocol", null);
	noauthdebug = !PropUtil.getBooleanProperty(props,
			    "mail.debug.auth", false);
	debugusername = PropUtil.getBooleanProperty(props,
			"mail.debug.auth.username", true);
	debugpassword = PropUtil.getBooleanProperty(props,
			"mail.debug.auth.password", false);
	if (urlname != null)
	    name = urlname.getProtocol();
	this.name = name;
	if (!isSSL)
	    isSSL = PropUtil.getBooleanProperty(props,
				"mail." + name + ".ssl.enable", false);
	if (isSSL)
	    this.defaultPort = 465;
	else
	    this.defaultPort = 25;
	this.isSSL = isSSL;

	// setting mail.smtp.quitwait to false causes us to not wait for the
	// response from the QUIT command
	quitWait = PropUtil.getBooleanProperty(props,
				"mail." + name + ".quitwait", true);

	// setting mail.smtp.quitonsessionreject to false causes us to directly
	// close the socket without sending a QUIT command
	quitOnSessionReject = PropUtil.getBooleanProperty(props,
            "mail." + name + ".quitonsessionreject", false);

	// mail.smtp.reportsuccess causes us to throw an exception on success
	reportSuccess = PropUtil.getBooleanProperty(props,
				"mail." + name + ".reportsuccess", false);

	// mail.smtp.starttls.enable enables use of STARTTLS command
	useStartTLS = PropUtil.getBooleanProperty(props,
				"mail." + name + ".starttls.enable", false);

	// mail.smtp.starttls.required requires use of STARTTLS command
	requireStartTLS = PropUtil.getBooleanProperty(props,
				"mail." + name + ".starttls.required", false);

	// mail.smtp.userset causes us to use RSET instead of NOOP
	// for isConnected
	useRset = PropUtil.getBooleanProperty(props,
				"mail." + name + ".userset", false);

	// mail.smtp.noop.strict requires 250 response to indicate success
	noopStrict = PropUtil.getBooleanProperty(props,
				"mail." + name + ".noop.strict", true);

	// check if SASL is enabled
	enableSASL = PropUtil.getBooleanProperty(props,
	    "mail." + name + ".sasl.enable", false);
	if (enableSASL)
	    logger.config("enable SASL");
	useCanonicalHostName = PropUtil.getBooleanProperty(props,
	    "mail." + name + ".sasl.usecanonicalhostname", false);
	if (useCanonicalHostName)
	    logger.config("use canonical host name");

	allowutf8 = PropUtil.getBooleanProperty(props,
	    "mail.mime.allowutf8", false);
	if (allowutf8)
	    logger.config("allow UTF-8");

	chunkSize = PropUtil.getIntProperty(props,
	    "mail." + name + ".chunksize", -1);
	if (chunkSize > 0 && logger.isLoggable(Level.CONFIG))
	    logger.config("chunk size " + chunkSize);

	// created here, because they're inner classes that reference "this"
	Authenticator[] a = new Authenticator[] {
	    new LoginAuthenticator(),
	    new PlainAuthenticator(),
	    new DigestMD5Authenticator(),
	    new NtlmAuthenticator(),
	    new OAuth2Authenticator()
	};
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < a.length; i++) {
	    authenticators.put(a[i].getMechanism(), a[i]);
	    sb.append(a[i].getMechanism()).append(' ');
	}
	defaultAuthenticationMechanisms = sb.toString();
    }

    /**
     * Get the name of the local host, for use in the EHLO and HELO commands.
     * The property mail.smtp.localhost overrides mail.smtp.localaddress,
     * which overrides what InetAddress would tell us.
     *
     * @return	the local host name
     */
    public synchronized String getLocalHost() {
	// get our hostname and cache it for future use
	if (localHostName == null || localHostName.length() <= 0)
	    localHostName =
		    session.getProperty("mail." + name + ".localhost");
	if (localHostName == null || localHostName.length() <= 0)
	    localHostName =
		    session.getProperty("mail." + name + ".localaddress");
	try {
	    if (localHostName == null || localHostName.length() <= 0) {
		InetAddress localHost = InetAddress.getLocalHost();
		localHostName = localHost.getCanonicalHostName();
		// if we can't get our name, use local address literal
		if (localHostName == null)
		    // XXX - not correct for IPv6
		    localHostName = "[" + localHost.getHostAddress() + "]";
	    }
	} catch (UnknownHostException uhex) {
	}

	// last chance, try to get our address from our socket
	if (localHostName == null || localHostName.length() <= 0) {
	    if (serverSocket != null && serverSocket.isBound()) {
		InetAddress localHost = serverSocket.getLocalAddress();
		localHostName = localHost.getCanonicalHostName();
		// if we can't get our name, use local address literal
		if (localHostName == null)
		    // XXX - not correct for IPv6
		    localHostName = "[" + localHost.getHostAddress() + "]";
	    }
	}
	return localHostName;
    }

    /**
     * Set the name of the local host, for use in the EHLO and HELO commands.
     *
     * @param	localhost	the local host name
     * @since JavaMail 1.3.1
     */
    public synchronized void setLocalHost(String localhost) {
	localHostName = localhost;
    }

    /**
     * Start the SMTP protocol on the given socket, which was already
     * connected by the caller.  Useful for implementing the SMTP ATRN
     * command (RFC 2645) where an existing connection is used when
     * the server reverses roles and becomes the client.
     *
     * @param	socket	the already connected socket
     * @exception	MessagingException for failures
     * @since JavaMail 1.3.3
     */
    public synchronized void connect(Socket socket) throws MessagingException {
	serverSocket = socket;
	super.connect();
    }

    /**
     * Gets the authorization ID to be used for authentication.
     *
     * @return	the authorization ID to use for authentication.
     *
     * @since JavaMail 1.4.4
     */
    public synchronized String getAuthorizationId() {
	if (authorizationID == UNKNOWN) {
	    authorizationID =
		session.getProperty("mail." + name + ".sasl.authorizationid");
	}
	return authorizationID;
    }

    /**
     * Sets the authorization ID to be used for authentication.
     *
     * @param	authzid		the authorization ID to use for
     *				authentication.
     *
     * @since JavaMail 1.4.4
     */
    public synchronized void setAuthorizationID(String authzid) {
	this.authorizationID = authzid;
    }

    /**
     * Is SASL authentication enabled?
     *
     * @return	true if SASL authentication is enabled
     *
     * @since JavaMail 1.4.4
     */
    public synchronized boolean getSASLEnabled() {
	return enableSASL;
    }

    /**
     * Set whether SASL authentication is enabled.
     *
     * @param	enableSASL	should we enable SASL authentication?
     *
     * @since JavaMail 1.4.4
     */
    public synchronized void setSASLEnabled(boolean enableSASL) {
	this.enableSASL = enableSASL;
    }

    /**
     * Gets the SASL realm to be used for DIGEST-MD5 authentication.
     *
     * @return	the name of the realm to use for SASL authentication.
     *
     * @since JavaMail 1.3.1
     */
    public synchronized String getSASLRealm() {
	if (saslRealm == UNKNOWN) {
	    saslRealm = session.getProperty("mail." + name + ".sasl.realm");
	    if (saslRealm == null)	// try old name
		saslRealm = session.getProperty("mail." + name + ".saslrealm");
	}
	return saslRealm;
    }

    /**
     * Sets the SASL realm to be used for DIGEST-MD5 authentication.
     *
     * @param	saslRealm	the name of the realm to use for
     *				SASL authentication.
     *
     * @since JavaMail 1.3.1
     */
    public synchronized void setSASLRealm(String saslRealm) {
	this.saslRealm = saslRealm;
    }

    /**
     * Should SASL use the canonical host name?
     *
     * @return	true if SASL should use the canonical host name
     *
     * @since JavaMail 1.5.2
     */
    public synchronized boolean getUseCanonicalHostName() {
	return useCanonicalHostName;
    }

    /**
     * Set whether SASL should use the canonical host name.
     *
     * @param	useCanonicalHostName	should SASL use the canonical host name?
     *
     * @since JavaMail 1.5.2
     */
    public synchronized void setUseCanonicalHostName(
						boolean useCanonicalHostName) {
	this.useCanonicalHostName = useCanonicalHostName;
    }

    /**
     * Get the list of SASL mechanisms to consider if SASL authentication
     * is enabled.  If the list is empty or null, all available SASL mechanisms
     * are considered.
     *
     * @return	the array of SASL mechanisms to consider
     *
     * @since JavaMail 1.4.4
     */
    public synchronized String[] getSASLMechanisms() {
	if (saslMechanisms == UNKNOWN_SA) {
	    List<String> v = new ArrayList<>(5);
	    String s = session.getProperty("mail." + name + ".sasl.mechanisms");
	    if (s != null && s.length() > 0) {
		if (logger.isLoggable(Level.FINE))
		    logger.fine("SASL mechanisms allowed: " + s);
		StringTokenizer st = new StringTokenizer(s, " ,");
		while (st.hasMoreTokens()) {
		    String m = st.nextToken();
		    if (m.length() > 0)
			v.add(m);
		}
	    }
	    saslMechanisms = new String[v.size()];
	    v.toArray(saslMechanisms);
	}
	if (saslMechanisms == null)
	    return null;
	return saslMechanisms.clone();
    }

    /**
     * Set the list of SASL mechanisms to consider if SASL authentication
     * is enabled.  If the list is empty or null, all available SASL mechanisms
     * are considered.
     *
     * @param	mechanisms	the array of SASL mechanisms to consider
     *
     * @since JavaMail 1.4.4
     */
    public synchronized void setSASLMechanisms(String[] mechanisms) {
	if (mechanisms != null)
	    mechanisms = mechanisms.clone();
	this.saslMechanisms = mechanisms;
    }

    /**
     * Gets the NTLM domain to be used for NTLM authentication.
     *
     * @return	the name of the domain to use for NTLM authentication.
     *
     * @since JavaMail 1.4.3
     */
    public synchronized String getNTLMDomain() {
	if (ntlmDomain == UNKNOWN) {
	    ntlmDomain =
		session.getProperty("mail." + name + ".auth.ntlm.domain");
	}
	return ntlmDomain;
    }

    /**
     * Sets the NTLM domain to be used for NTLM authentication.
     *
     * @param	ntlmDomain	the name of the domain to use for
     *				NTLM authentication.
     *
     * @since JavaMail 1.4.3
     */
    public synchronized void setNTLMDomain(String ntlmDomain) {
	this.ntlmDomain = ntlmDomain;
    }

    /**
     * Should we report even successful sends by throwing an exception?
     * If so, a <code>SendFailedException</code> will always be thrown and
     * an {@link com.sun.mail.smtp.SMTPAddressSucceededException
     * SMTPAddressSucceededException} will be included in the exception
     * chain for each successful address, along with the usual
     * {@link com.sun.mail.smtp.SMTPAddressFailedException
     * SMTPAddressFailedException} for each unsuccessful address.
     *
     * @return	true if an exception will be thrown on successful sends.
     *
     * @since JavaMail 1.3.2
     */
    public synchronized boolean getReportSuccess() {
	return reportSuccess;
    }

    /**
     * Set whether successful sends should be reported by throwing
     * an exception.
     *
     * @param	reportSuccess	should we throw an exception on success?
     *
     * @since JavaMail 1.3.2
     */
    public synchronized void setReportSuccess(boolean reportSuccess) {
	this.reportSuccess = reportSuccess;
    }

    /**
     * Should we use the STARTTLS command to secure the connection
     * if the server supports it?
     *
     * @return	true if the STARTTLS command will be used
     *
     * @since JavaMail 1.3.2
     */
    public synchronized boolean getStartTLS() {
	return useStartTLS;
    }

    /**
     * Set whether the STARTTLS command should be used.
     *
     * @param	useStartTLS	should we use the STARTTLS command?
     *
     * @since JavaMail 1.3.2
     */
    public synchronized void setStartTLS(boolean useStartTLS) {
	this.useStartTLS = useStartTLS;
    }

    /**
     * Should we require the STARTTLS command to secure the connection?
     *
     * @return	true if the STARTTLS command will be required
     *
     * @since JavaMail 1.4.2
     */
    public synchronized boolean getRequireStartTLS() {
	return requireStartTLS;
    }

    /**
     * Set whether the STARTTLS command should be required.
     *
     * @param	requireStartTLS	should we require the STARTTLS command?
     *
     * @since JavaMail 1.4.2
     */
    public synchronized void setRequireStartTLS(boolean requireStartTLS) {
	this.requireStartTLS = requireStartTLS;
    }

    /**
     * Is this Transport using SSL to connect to the server?
     *
     * @return	true if using SSL
     * @since	JavaMail 1.4.6
     */
    public synchronized boolean isSSL() {
	return serverSocket instanceof SSLSocket;
    }

    /**
     * Should we use the RSET command instead of the NOOP command
     * in the @{link #isConnected isConnected} method?
     *
     * @return	true if RSET will be used
     *
     * @since JavaMail 1.4
     */
    public synchronized boolean getUseRset() {
	return useRset;
    }

    /**
     * Set whether the RSET command should be used instead of the
     * NOOP command in the @{link #isConnected isConnected} method.
     *
     * @param	useRset	should we use the RSET command?
     *
     * @since JavaMail 1.4
     */
    public synchronized void setUseRset(boolean useRset) {
	this.useRset = useRset;
    }

    /**
     * Is the NOOP command required to return a response code
     * of 250 to indicate success?
     *
     * @return	true if NOOP must return 250
     *
     * @since JavaMail 1.4.3
     */
    public synchronized boolean getNoopStrict() {
	return noopStrict;
    }

    /**
     * Set whether the NOOP command is required to return a response code
     * of 250 to indicate success.
     *
     * @param	noopStrict is NOOP required to return 250?
     *
     * @since JavaMail 1.4.3
     */
    public synchronized void setNoopStrict(boolean noopStrict) {
	this.noopStrict = noopStrict;
    }

    /**
     * Return the last response we got from the server.
     * A failed send is often followed by an RSET command,
     * but the response from the RSET command is not saved.
     * Instead, this returns the response from the command
     * before the RSET command.
     *
     * @return	last response from server
     *
     * @since JavaMail 1.3.2
     */
    public synchronized String getLastServerResponse() {
	return lastServerResponse;
    }

    /**
     * Return the return code from the last response we got from the server.
     *
     * @return	return code from last response from server
     *
     * @since JavaMail 1.4.1
     */
    public synchronized int getLastReturnCode() {
	return lastReturnCode;
    }

    /**
     * Performs the actual protocol-specific connection attempt.
     * Will attempt to connect to "localhost" if the host was null. <p>
     *
     * Unless mail.smtp.ehlo is set to false, we'll try to identify
     * ourselves using the ESMTP command EHLO.
     *
     * If mail.smtp.auth is set to true, we insist on having a username
     * and password, and will try to authenticate ourselves if the server
     * supports the AUTH extension (RFC 2554).
     *
     * @param	host		  the name of the host to connect to
     * @param	port		  the port to use (-1 means use default port)
     * @param	user		  the name of the user to login as
     * @param	password	  the user's password
     * @return	true if connection successful, false if authentication failed
     * @exception MessagingException	for non-authentication failures
     */
    @Override
    protected synchronized boolean protocolConnect(String host, int port,
				String user, String password)
				throws MessagingException {
	Properties props = session.getProperties();

	// setting mail.smtp.auth to true enables attempts to use AUTH
	boolean useAuth = PropUtil.getBooleanProperty(props,
					"mail." + name + ".auth", false);

	/*
	 * If mail.smtp.auth is set, make sure we have a valid username
	 * and password, even if we might not end up using it (e.g.,
	 * because the server doesn't support ESMTP or doesn't support
	 * the AUTH extension).
	 */
	if (useAuth && (user == null || password == null)) {
	    if (logger.isLoggable(Level.FINE)) {
		logger.fine("need username and password for authentication");
		logger.fine("protocolConnect returning false" +
				", host=" + host +
				", user=" + traceUser(user) +
				", password=" + tracePassword(password));
	    }
	    return false;
	}

	// setting mail.smtp.ehlo to false disables attempts to use EHLO
	boolean useEhlo =  PropUtil.getBooleanProperty(props,
					"mail." + name + ".ehlo", true);
	if (logger.isLoggable(Level.FINE))
	    logger.fine("useEhlo " + useEhlo + ", useAuth " + useAuth);

	/*
	 * If port is not specified, set it to value of mail.smtp.port
         * property if it exists, otherwise default to 25.
	 */
        if (port == -1)
	    port = PropUtil.getIntProperty(props,
					"mail." + name + ".port", -1);
        if (port == -1)
	    port = defaultPort;

	if (host == null || host.length() == 0)
	    host = "localhost";

	/*
	 * If anything goes wrong, we need to be sure
	 * to close the connection.
	 */
	boolean connected = false;
	try {

	    if (serverSocket != null)
		openServer();	// only happens from connect(socket)
	    else
		openServer(host, port);

	    boolean succeed = false;
	    if (useEhlo)
		succeed = ehlo(getLocalHost());
	    if (!succeed)
		helo(getLocalHost());

	    if (useStartTLS || requireStartTLS) {
		if (serverSocket instanceof SSLSocket) {
		    logger.fine("STARTTLS requested but already using SSL");
		} else if (supportsExtension("STARTTLS")) {
		    startTLS();
		    /*
		     * Have to issue another EHLO to update list of extensions
		     * supported, especially authentication mechanisms.
		     * Don't know if this could ever fail, but we ignore
		     * failure.
		     */
		    ehlo(getLocalHost());
		} else if (requireStartTLS) {
		    logger.fine("STARTTLS required but not supported");
		    throw new MessagingException(
			"STARTTLS is required but " +
			"host does not support STARTTLS");
		}
	    }

	    if (allowutf8 && !supportsExtension("SMTPUTF8"))
		logger.log(Level.INFO, "mail.mime.allowutf8 set " +
			    "but server doesn't advertise SMTPUTF8 support");

	    if ((useAuth || (user != null && password != null)) &&
		  (supportsExtension("AUTH") ||
		   supportsExtension("AUTH=LOGIN"))) {
		if (logger.isLoggable(Level.FINE))
		    logger.fine("protocolConnect login" +
				", host=" + host +
				", user=" + traceUser(user) +
				", password=" + tracePassword(password));
		connected = authenticate(user, password);
		return connected;
	    }

	    // we connected correctly
	    connected = true;
	    return true;

	} finally {
	    // if we didn't connect successfully,
	    // make sure the connection is closed
	    if (!connected) {
		try {
		    closeConnection();
		} catch (MessagingException mex) {
		    // ignore it
		}
	    }
	}
    }

    /**
     * Authenticate to the server.
     */
    private boolean authenticate(String user, String passwd)
				throws MessagingException {
	// setting mail.smtp.auth.mechanisms controls which mechanisms will
	// be used, and in what order they'll be considered.  only the first
	// match is used.
	String mechs = session.getProperty("mail." + name + ".auth.mechanisms");
	if (mechs == null)
	    mechs = defaultAuthenticationMechanisms;

	String authzid = getAuthorizationId();
	if (authzid == null)
	    authzid = user;
	if (enableSASL) {
	    logger.fine("Authenticate with SASL");
	    try {
		if (sasllogin(getSASLMechanisms(), getSASLRealm(), authzid,
				user, passwd)) {
		    return true;	// success
		} else {
		    logger.fine("SASL authentication failed");
		    return false;
		}
	    } catch (UnsupportedOperationException ex) {
		logger.log(Level.FINE, "SASL support failed", ex);
		// if the SASL support fails, fall back to non-SASL
	    }
	}

	if (logger.isLoggable(Level.FINE))
	    logger.fine("Attempt to authenticate using mechanisms: " + mechs);

	/*
	 * Loop through the list of mechanisms supplied by the user
	 * (or defaulted) and try each in turn.  If the server supports
	 * the mechanism and we have an authenticator for the mechanism,
	 * and it hasn't been disabled, use it.
	 */
	StringTokenizer st = new StringTokenizer(mechs);
	while (st.hasMoreTokens()) {
	    String m = st.nextToken();
	    m = m.toUpperCase(Locale.ENGLISH);
	    Authenticator a = authenticators.get(m);
	    if (a == null) {
		logger.log(Level.FINE, "no authenticator for mechanism {0}", m);
		continue;
	    }

	    if (!supportsAuthentication(m)) {
		logger.log(Level.FINE, "mechanism {0} not supported by server",
					m);
		continue;
	    }

	    /*
	     * If using the default mechanisms, check if this one is disabled.
	     */
	    if (mechs == defaultAuthenticationMechanisms) {
		String dprop = "mail." + name + ".auth." +
				    m.toLowerCase(Locale.ENGLISH) + ".disable";
		boolean disabled = PropUtil.getBooleanProperty(
						session.getProperties(),
						dprop, !a.enabled());
		if (disabled) {
		    if (logger.isLoggable(Level.FINE))
			logger.fine("mechanism " + m +
					" disabled by property: " + dprop);
		    continue;
		}
	    }

	    // only the first supported and enabled mechanism is used
	    logger.log(Level.FINE, "Using mechanism {0}", m);
	    return a.authenticate(host, authzid, user, passwd);
	}

	// if no authentication mechanism found, fail
	throw new AuthenticationFailedException(
	    "No authentication mechanisms supported by both server and client");
    }

    /**
     * Abstract base class for SMTP authentication mechanism implementations.
     */
    private abstract class Authenticator {
	protected int resp;	// the response code, used by subclasses
	private final String mech; // the mechanism name, set in the constructor
	private final boolean enabled; // is this mechanism enabled by default?

	Authenticator(String mech) {
	    this(mech, true);
	}

	Authenticator(String mech, boolean enabled) {
	    this.mech = mech.toUpperCase(Locale.ENGLISH);
	    this.enabled = enabled;
	}

	String getMechanism() {
	    return mech;
	}

	boolean enabled() {
	    return enabled;
	}

	/**
	 * Start the authentication handshake by issuing the AUTH command.
	 * Delegate to the doAuth method to do the mechanism-specific
	 * part of the handshake.
	 */
	boolean authenticate(String host, String authzid,
			String user, String passwd) throws MessagingException {
	    Throwable thrown = null;
	    try {
		// use "initial response" capability, if supported
		String ir = getInitialResponse(host, authzid, user, passwd);
		if (noauthdebug && isTracing()) {
		    logger.fine("AUTH " + mech + " command trace suppressed");
		    suspendTracing();
		}
		if (ir != null)
		    resp = simpleCommand("AUTH " + mech + " " +
					    (ir.length() == 0 ? "=" : ir));
		else
		    resp = simpleCommand("AUTH " + mech);

		/*
		 * A 530 response indicates that the server wants us to
		 * issue a STARTTLS command first.  Do that and try again.
		 */
		if (resp == 530) {
		    startTLS();
		    if (ir != null)
			resp = simpleCommand("AUTH " + mech + " " + ir);
		    else
			resp = simpleCommand("AUTH " + mech);
		}
		if (resp == 334)
		    doAuth(host, authzid, user, passwd);
	    } catch (IOException ex) {	// should never happen, ignore
		logger.log(Level.FINE, "AUTH " + mech + " failed", ex);
	    } catch (Throwable t) {	// crypto can't be initialized?
		logger.log(Level.FINE, "AUTH " + mech + " failed", t);
		thrown = t;
	    } finally {
		if (noauthdebug && isTracing())
		    logger.fine("AUTH " + mech + " " +
				    (resp == 235 ? "succeeded" : "failed"));
		resumeTracing();
		if (resp != 235) {
		    closeConnection();
		    if (thrown != null) {
			if (thrown instanceof Error)
			    throw (Error)thrown;
			if (thrown instanceof Exception)
			    throw new AuthenticationFailedException(
					    getLastServerResponse(),
					    (Exception)thrown);
			assert false : "unknown Throwable";	// can't happen
		    }
		    throw new AuthenticationFailedException(
					    getLastServerResponse());
		}
	    }
	    return true;
	}

	/**
	 * Provide the initial response to use in the AUTH command,
	 * or null if not supported.  Subclasses that support the
	 * initial response capability will override this method.
	 */
	String getInitialResponse(String host, String authzid, String user,
		    String passwd) throws MessagingException, IOException {
	    return null;
	}

	abstract void doAuth(String host, String authzid, String user,
		    String passwd) throws MessagingException, IOException;
    }

    /**
     * Perform the authentication handshake for LOGIN authentication.
     */
    private class LoginAuthenticator extends Authenticator {
	LoginAuthenticator() {
	    super("LOGIN");
	}

	@Override
	void doAuth(String host, String authzid, String user, String passwd)
				    throws MessagingException, IOException {
	    // send username
	    resp = simpleCommand(BASE64EncoderStream.encode(
				user.getBytes(StandardCharsets.UTF_8)));
	    if (resp == 334) {
		// send passwd
		resp = simpleCommand(BASE64EncoderStream.encode(
				passwd.getBytes(StandardCharsets.UTF_8)));
	    }
	}
    }

    /**
     * Perform the authentication handshake for PLAIN authentication.
     */
    private class PlainAuthenticator extends Authenticator {
	PlainAuthenticator() {
	    super("PLAIN");
	}

	@Override
	String getInitialResponse(String host, String authzid, String user,
			String passwd) throws MessagingException, IOException {
	    // return "authzid<NUL>user<NUL>passwd"
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    OutputStream b64os =
			new BASE64EncoderStream(bos, Integer.MAX_VALUE);
	    if (authzid != null)
		b64os.write(authzid.getBytes(StandardCharsets.UTF_8));
	    b64os.write(0);
	    b64os.write(user.getBytes(StandardCharsets.UTF_8));
	    b64os.write(0);
	    b64os.write(passwd.getBytes(StandardCharsets.UTF_8));
	    b64os.flush(); 	// complete the encoding

	    return ASCIIUtility.toString(bos.toByteArray());
	}

	@Override
	void doAuth(String host, String authzid, String user, String passwd)
				    throws MessagingException, IOException {
	    // should never get here
	    throw new AuthenticationFailedException("PLAIN asked for more");
	}
    }

    /**
     * Perform the authentication handshake for DIGEST-MD5 authentication.
     */
    private class DigestMD5Authenticator extends Authenticator {
	private DigestMD5 md5support;	// only create if needed

	DigestMD5Authenticator() {
	    super("DIGEST-MD5");
	}

	private synchronized DigestMD5 getMD5() {
	    if (md5support == null)
		md5support = new DigestMD5(logger);
	    return md5support;
	}

	@Override
	void doAuth(String host, String authzid, String user, String passwd)
				    throws MessagingException, IOException {
	    DigestMD5 md5 = getMD5();
	    assert md5 != null;

	    byte[] b = md5.authClient(host, user, passwd, getSASLRealm(),
					getLastServerResponse());
	    resp = simpleCommand(b);
	    if (resp == 334) { // client authenticated by server
		if (!md5.authServer(getLastServerResponse())) {
		    // server NOT authenticated by client !!!
		    resp = -1;
		} else {
		    // send null response
		    resp = simpleCommand(new byte[0]);
		}
	    }
	}
    }

    /**
     * Perform the authentication handshake for NTLM authentication.
     */
    private class NtlmAuthenticator extends Authenticator {
	private Ntlm ntlm;

	NtlmAuthenticator() {
	    super("NTLM");
	}

	@Override
	String getInitialResponse(String host, String authzid, String user,
		String passwd) throws MessagingException, IOException {
	    ntlm = new Ntlm(getNTLMDomain(), getLocalHost(),
				user, passwd, logger);

	    int flags = PropUtil.getIntProperty(
		    session.getProperties(),
		    "mail." + name + ".auth.ntlm.flags", 0);
	    boolean v2 = PropUtil.getBooleanProperty(
		    session.getProperties(),
		    "mail." + name + ".auth.ntlm.v2", true);

	    String type1 = ntlm.generateType1Msg(flags, v2);
	    return type1;
	}

	@Override
	void doAuth(String host, String authzid, String user, String passwd)
		throws MessagingException, IOException {
	    assert ntlm != null;
	    String type3 = ntlm.generateType3Msg(
		    getLastServerResponse().substring(4).trim());

	    resp = simpleCommand(type3);
	}
    }

    /**
     * Perform the authentication handshake for XOAUTH2 authentication.
     */
    private class OAuth2Authenticator extends Authenticator {

	OAuth2Authenticator() {
	    super("XOAUTH2", false);	// disabled by default
	}

	@Override
	String getInitialResponse(String host, String authzid, String user,
		String passwd) throws MessagingException, IOException {
	    String resp = "user=" + user + "\001auth=Bearer " +
			    passwd + "\001\001";
	    byte[] b = BASE64EncoderStream.encode(
					resp.getBytes(StandardCharsets.UTF_8));
	    return ASCIIUtility.toString(b);
	}

	@Override
	void doAuth(String host, String authzid, String user, String passwd)
		throws MessagingException, IOException {
	    // should never get here
	    throw new AuthenticationFailedException("OAUTH2 asked for more");
	}
    }

    /**
     * SASL-based login.
     *
     * @param	allowed	the allowed SASL mechanisms
     * @param	realm	the SASL realm
     * @param	authzid	the authorization ID
     * @param	u	the user name for authentication
     * @param	p	the password for authentication
     * @return		true for success
     * @exception	MessagingException for failures
     */
    private boolean sasllogin(String[] allowed, String realm, String authzid,
				String u, String p) throws MessagingException {
	String serviceHost;
	if (useCanonicalHostName)
	    serviceHost = serverSocket.getInetAddress().getCanonicalHostName();
	else
	    serviceHost = host;
	if (saslAuthenticator == null) {
	    try {
		Class<?> sac = Class.forName(
		    "com.sun.mail.smtp.SMTPSaslAuthenticator");
		Constructor<?> c = sac.getConstructor(new Class<?>[] {
					SMTPTransport.class,
					String.class,
					Properties.class,
					MailLogger.class,
					String.class
					});
		saslAuthenticator = (SaslAuthenticator)c.newInstance(
					new Object[] {
					this,
					name,
					session.getProperties(),
					logger,
					serviceHost
					});
	    } catch (Exception ex) {
		logger.log(Level.FINE, "Can't load SASL authenticator", ex);
		// probably because we're running on a system without SASL
		return false;	// not authenticated, try without SASL
	    }
	}

	// were any allowed mechanisms specified?
	List<String> v;
	if (allowed != null && allowed.length > 0) {
	    // remove anything not supported by the server
	    v = new ArrayList<>(allowed.length);
	    for (int i = 0; i < allowed.length; i++)
		if (supportsAuthentication(allowed[i]))	// XXX - case must match
		    v.add(allowed[i]);
	} else {
	    // everything is allowed
	    v = new ArrayList<>();
	    if (extMap != null) {
		String a = extMap.get("AUTH");
		if (a != null) {
		    StringTokenizer st = new StringTokenizer(a);
		    while (st.hasMoreTokens())
			v.add(st.nextToken());
		}
	    }
	}
	String[] mechs = v.toArray(new String[v.size()]);
	try {
	    if (noauthdebug && isTracing()) {
		logger.fine("SASL AUTH command trace suppressed");
		suspendTracing();
	    }
	    return saslAuthenticator.authenticate(mechs, realm, authzid, u, p);
	} finally {
	    resumeTracing();
	}
    }

    /**
     * Send the Message to the specified list of addresses.<p>
     *
     * If all the <code>addresses</code> succeed the SMTP check
     * using the <code>RCPT TO:</code> command, we attempt to send the message.
     * A TransportEvent of type MESSAGE_DELIVERED is fired indicating the
     * successful submission of a message to the SMTP host.<p>
     *
     * If some of the <code>addresses</code> fail the SMTP check,
     * and the <code>mail.smtp.sendpartial</code> property is not set,
     * sending is aborted. The TransportEvent of type MESSAGE_NOT_DELIVERED
     * is fired containing the valid and invalid addresses. The
     * SendFailedException is also thrown. <p>
     *
     * If some of the <code>addresses</code> fail the SMTP check,
     * and the <code>mail.smtp.sendpartial</code> property is set to true,
     * the message is sent. The TransportEvent of type
     * MESSAGE_PARTIALLY_DELIVERED
     * is fired containing the valid and invalid addresses. The
     * SMTPSendFailedException is also thrown. <p>
     *
     * MessagingException is thrown if the message can't write out
     * an RFC822-compliant stream using its <code>writeTo</code> method. <p>
     *
     * @param message	The MimeMessage to be sent
     * @param addresses	List of addresses to send this message to
     * @see 		javax.mail.event.TransportEvent
     * @exception       SMTPSendFailedException if the send failed because of
     *			an SMTP command error
     * @exception       SendFailedException if the send failed because of
     *			invalid addresses.
     * @exception       MessagingException if the connection is dead
     *                  or not in the connected state or if the message is
     *                  not a MimeMessage.
     */
    @Override
    public synchronized void sendMessage(Message message, Address[] addresses)
		    throws MessagingException, SendFailedException {

	sendMessageStart(message != null ? message.getSubject() : "");
	checkConnected();

	// check if the message is a valid MIME/RFC822 message and that
	// it has all valid InternetAddresses; fail if not
        if (!(message instanceof MimeMessage)) {
	    logger.fine("Can only send RFC822 msgs");
	    throw new MessagingException("SMTP can only send RFC822 messages");
	}
    if (addresses == null || addresses.length == 0) {
        throw new SendFailedException("No recipient addresses");
    }
	for (int i = 0; i < addresses.length; i++) {
	    if (!(addresses[i] instanceof InternetAddress)) {
		throw new MessagingException(addresses[i] +
					     " is not an InternetAddress");
	    }
	}

	this.message = (MimeMessage)message;
	this.addresses = addresses;
	validUnsentAddr = addresses;	// until we know better
	expandGroups();

	boolean use8bit = false;
	if (message instanceof SMTPMessage)
	    use8bit = ((SMTPMessage)message).getAllow8bitMIME();
	if (!use8bit)
	    use8bit = PropUtil.getBooleanProperty(session.getProperties(),
				"mail." + name + ".allow8bitmime", false);
	if (logger.isLoggable(Level.FINE))
	    logger.fine("use8bit " + use8bit);
	if (use8bit && supportsExtension("8BITMIME")) {
	    if (convertTo8Bit(this.message)) {
		// in case we made any changes, save those changes
		// XXX - this will change the Message-ID
		try {
		    this.message.saveChanges();
		} catch (MessagingException mex) {
		    // ignore it
		}
	    }
	}

	try {
	    mailFrom();
	    rcptTo();
	    if (chunkSize > 0 && supportsExtension("CHUNKING")) {
		/*
		 * Use BDAT to send the data in chunks.
		 * Note that even though the BDAT command is able to send
		 * messages that contain binary data, we can't use it to
		 * do that because a) we still need to canonicalize the
		 * line terminators for text data, which we can't tell apart
		 * from the message content, and b) the message content is
		 * encoded before we even know that we can use BDAT.
		 */
		this.message.writeTo(bdat(), ignoreList);
		finishBdat();
	    } else {
		this.message.writeTo(data(), ignoreList);
		finishData();
	    }
	    if (sendPartiallyFailed) {
		// throw the exception,
		// fire TransportEvent.MESSAGE_PARTIALLY_DELIVERED event
		logger.fine("Sending partially failed " +
			"because of invalid destination addresses");
		notifyTransportListeners(
			TransportEvent.MESSAGE_PARTIALLY_DELIVERED,
			validSentAddr, validUnsentAddr, invalidAddr,
			this.message);

		throw new SMTPSendFailedException(".", lastReturnCode,
				lastServerResponse, exception,
				validSentAddr, validUnsentAddr, invalidAddr);
	    }
	    logger.fine("message successfully delivered to mail server");
	    notifyTransportListeners(TransportEvent.MESSAGE_DELIVERED,
				     validSentAddr, validUnsentAddr,
				     invalidAddr, this.message);
	} catch (MessagingException mex) {
	    logger.log(Level.FINE, "MessagingException while sending", mex);
	    // the MessagingException might be wrapping an IOException
	    if (mex.getNextException() instanceof IOException) {
		// if we catch an IOException, it means that we want
		// to drop the connection so that the message isn't sent
		logger.fine("nested IOException, closing");
		try {
		    closeConnection();
		} catch (MessagingException cex) { /* ignore it */ }
	    }
	    addressesFailed();
	    notifyTransportListeners(TransportEvent.MESSAGE_NOT_DELIVERED,
				     validSentAddr, validUnsentAddr,
				     invalidAddr, this.message);

	    throw mex;
	} catch (IOException ex) {
	    logger.log(Level.FINE, "IOException while sending, closing", ex);
	    // if we catch an IOException, it means that we want
	    // to drop the connection so that the message isn't sent
	    try {
		closeConnection();
	    } catch (MessagingException mex) { /* ignore it */ }
	    addressesFailed();
	    notifyTransportListeners(TransportEvent.MESSAGE_NOT_DELIVERED,
				     validSentAddr, validUnsentAddr,
				     invalidAddr, this.message);

	    throw new MessagingException("IOException while sending message",
					 ex);
	} finally {
	    // no reason to keep this data around
	    validSentAddr = validUnsentAddr = invalidAddr = null;
	    this.addresses = null;
	    this.message = null;
	    this.exception = null;
	    sendPartiallyFailed = false;
	    notificationDone = false;	// reset for next send
	}
	sendMessageEnd();
    }

    /**
     * The send failed, fix the address arrays to report the failure correctly.
     */
    private void addressesFailed() {
	if (validSentAddr != null) {
	    if (validUnsentAddr != null) {
		Address newa[] =
		    new Address[validSentAddr.length + validUnsentAddr.length];
		System.arraycopy(validSentAddr, 0,
			newa, 0, validSentAddr.length);
		System.arraycopy(validUnsentAddr, 0,
			newa, validSentAddr.length, validUnsentAddr.length);
		validSentAddr = null;
		validUnsentAddr = newa;
	    } else {
		validUnsentAddr = validSentAddr;
		validSentAddr = null;
	    }
	}
    }

    /**
     * Close the Transport and terminate the connection to the server.
     */
    @Override
    public synchronized void close() throws MessagingException {
	if (!super.isConnected()) // Already closed.
	    return;
	try {
	    if (serverSocket != null) {
		sendCommand("QUIT");
		if (quitWait) {
		    int resp = readServerResponse();
		    if (resp != 221 && resp != -1 &&
			    logger.isLoggable(Level.FINE))
			logger.fine("QUIT failed with " + resp);
		}
	    }
	} finally {
	    closeConnection();
	}
    }

    private void closeConnection() throws MessagingException {
	try {
	    if (serverSocket != null)
		serverSocket.close();
	} catch (IOException ioex) {	    // shouldn't happen
	    throw new MessagingException("Server Close Failed", ioex);
	} finally {
	    serverSocket = null;
	    serverOutput = null;
	    serverInput = null;
	    lineInputStream = null;
	    if (super.isConnected())	// only notify if already connected
		super.close();
	}
    }

    /**
     * Check whether the transport is connected. Override superclass
     * method, to actually ping our server connection.
     */
    @Override
    public synchronized boolean isConnected() {
	if (!super.isConnected())
	    // if we haven't been connected at all, don't bother with NOOP
	    return false;

	try {
	    // sendmail may respond slowly to NOOP after many requests
	    // so if mail.smtp.userset is set we use RSET instead of NOOP.
	    if (useRset)
		sendCommand("RSET");
	    else
		sendCommand("NOOP");
	    int resp = readServerResponse();

	    /*
	     * NOOP should return 250 on success, however, SIMS 3.2 returns
	     * 200, so we work around it.
	     *
	     * Hotmail didn't used to implement the NOOP command at all so
	     * assume any kind of response means we're still connected.
	     * That is, any response except 421, which means the server
	     * is shutting down the connection.
	     *
	     * Some versions of Exchange return 451 instead of 421 when
	     * timing out a connection.
	     *
	     * Argh!
	     *
	     * If mail.smtp.noop.strict is set to false, be tolerant of
	     * servers that return the wrong response code for success.
	     */
	    if (resp >= 0 && (noopStrict ? resp == 250 : resp != 421)) {
		return true;
	    } else {
		try {
		    closeConnection();
		} catch (MessagingException mex) {
		    // ignore it
		}
		return false;
	    }
	} catch (Exception ex) {
	    try {
		closeConnection();
	    } catch (MessagingException mex) {
		// ignore it
	    }
	    return false;
	}
    }

    /**
     * Notify all TransportListeners.  Keep track of whether notification
     * has been done so as to only notify once per send.
     *
     * @since	JavaMail 1.4.2
     */
    @Override
    protected void notifyTransportListeners(int type, Address[] validSent,
					    Address[] validUnsent,
					    Address[] invalid, Message msg) {

	if (!notificationDone) {
	    super.notifyTransportListeners(type, validSent, validUnsent,
		invalid, msg);
	    notificationDone = true;
	}
    }

    /**
     * Expand any group addresses.
     */
    private void expandGroups() {
	List<Address> groups = null;
	for (int i = 0; i < addresses.length; i++) {
	    InternetAddress a = (InternetAddress)addresses[i];
	    if (a.isGroup()) {
		if (groups == null) {
		    // first group, catch up with where we are
		    groups = new ArrayList<>();
		    for (int k = 0; k < i; k++)
			groups.add(addresses[k]);
		}
		// parse it and add each individual address
		try {
		    InternetAddress[] ia = a.getGroup(true);
		    if (ia != null) {
			for (int j = 0; j < ia.length; j++)
			    groups.add(ia[j]);
		    } else
			groups.add(a);
		} catch (ParseException pex) {
		    // parse failed, add the whole thing
		    groups.add(a);
		}
	    } else {
		// if we've started accumulating a list, add this to it
		if (groups != null)
		    groups.add(a);
	    }
	}

	// if we have a new list, convert it back to an array
	if (groups != null) {
	    InternetAddress[] newa = new InternetAddress[groups.size()];
	    groups.toArray(newa);
	    addresses = newa;
	}
    }

    /**
     * If the Part is a text part and has a Content-Transfer-Encoding
     * of "quoted-printable" or "base64", and it obeys the rules for
     * "8bit" encoding, change the encoding to "8bit".  If the part is
     * a multipart, recursively process all its parts.
     *
     * @return	true	if any changes were made
     *
     * XXX - This is really quite a hack.
     */
    private boolean convertTo8Bit(MimePart part) {
	boolean changed = false;
	try {
	    if (part.isMimeType("text/*")) {
		String enc = part.getEncoding();
		if (enc != null && (enc.equalsIgnoreCase("quoted-printable") ||
		    enc.equalsIgnoreCase("base64"))) {
		    InputStream is = null;
		    try {
			is = part.getInputStream();
			if (is8Bit(is)) {
			    /*
			     * If the message was created using an InputStream
			     * then we have to extract the content as an object
			     * and set it back as an object so that the content
			     * will be re-encoded.
			     *
			     * If the message was not created using an
			     * InputStream, the following should have no effect.
			     */
			    part.setContent(part.getContent(),
					    part.getContentType());
			    part.setHeader("Content-Transfer-Encoding", "8bit");
			    changed = true;
			}
		    } finally {
			if (is != null) {
			    try {
				is.close();
			    } catch (IOException ex2) {
				// ignore it
			    }
			}
		    }
		}
	    } else if (part.isMimeType("multipart/*")) {
		MimeMultipart mp = (MimeMultipart)part.getContent();
		int count = mp.getCount();
		for (int i = 0; i < count; i++) {
		    if (convertTo8Bit((MimePart)mp.getBodyPart(i)))
			changed = true;
		}
	    }
	} catch (IOException ioex) {
	    // any exception causes us to give up
	} catch (MessagingException mex) {
	    // any exception causes us to give up
	}
	return changed;
    }

    /**
     * Check whether the data in the given InputStream follows the
     * rules for 8bit text.  Lines have to be 998 characters or less
     * and no NULs are allowed.  CR and LF must occur in pairs but we
     * don't check that because we assume this is text and we convert
     * all CR/LF combinations into canonical CRLF later.
     */
    private boolean is8Bit(InputStream is) {
	int b;
	int linelen = 0;
	boolean need8bit = false;
	try {
	    while ((b = is.read()) >= 0) {
		b &= 0xff;
		if (b == '\r' || b == '\n')
		    linelen = 0;
		else if (b == 0)
		    return false;
		else {
		    linelen++;
		    if (linelen > 998)	// 1000 - CRLF
			return false;
		}
		if (b > 0x7f)
		    need8bit = true;
	    }
	} catch (IOException ex) {
	    return false;
	}
	if (need8bit)
	    logger.fine("found an 8bit part");
	return need8bit;
    }

    @Override
    protected void finalize() throws Throwable {
	try {
	    closeConnection();
	} catch (MessagingException mex) {
	    // ignore it
	} finally {
	    super.finalize();
	}
    }

    ///////////////////// smtp stuff ///////////////////////
    private BufferedInputStream serverInput;
    private LineInputStream     lineInputStream;
    private OutputStream        serverOutput;
    private Socket              serverSocket;
    private TraceInputStream	traceInput;
    private TraceOutputStream	traceOutput;

    /////// smtp protocol //////

    /**
     * Issue the <code>HELO</code> command.
     *
     * @param	domain	our domain
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    protected void helo(String domain) throws MessagingException {
	if (domain != null)
	    issueCommand("HELO " + domain, 250);
	else
	    issueCommand("HELO", 250);
    }

    /**
     * Issue the <code>EHLO</code> command.
     * Collect the returned list of service extensions.
     *
     * @param	domain	our domain
     * @return		true if command succeeds
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    protected boolean ehlo(String domain) throws MessagingException {
	String cmd;
	if (domain != null)
	    cmd = "EHLO " + domain;
	else
	    cmd = "EHLO";
	sendCommand(cmd);
	int resp = readServerResponse();
	if (resp == 250) {
	    // extract the supported service extensions
	    BufferedReader rd =
		new BufferedReader(new StringReader(lastServerResponse));
	    String line;
	    extMap = new Hashtable<>();
	    try {
		boolean first = true;
		while ((line = rd.readLine()) != null) {
		    if (first) {	// skip first line which is the greeting
			first = false;
			continue;
		    }
		    if (line.length() < 5)
			continue;		// shouldn't happen
		    line = line.substring(4);	// skip response code
		    int i = line.indexOf(' ');
		    String arg = "";
		    if (i > 0) {
			arg = line.substring(i + 1);
			line = line.substring(0, i);
		    }
		    if (logger.isLoggable(Level.FINE))
			logger.fine("Found extension \"" +
					    line + "\", arg \"" + arg + "\"");
		    extMap.put(line.toUpperCase(Locale.ENGLISH), arg);
		}
	    } catch (IOException ex) { }	// can't happen
	}
	return resp == 250;
    }

    /**
     * Issue the <code>MAIL FROM:</code> command to start sending a message. <p>
     *
     * Gets the sender's address in the following order:
     * <ol>
     * <li>SMTPMessage.getEnvelopeFrom()</li>
     * <li>mail.smtp.from property</li>
     * <li>From: header in the message</li>
     * <li>System username using the
     * InternetAddress.getLocalAddress() method</li>
     * </ol>
     *
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    protected void mailFrom() throws MessagingException {
	String from = null;
	if (message instanceof SMTPMessage)
	    from = ((SMTPMessage)message).getEnvelopeFrom();
	if (from == null || from.length() <= 0)
	    from = session.getProperty("mail." + name + ".from");
	if (from == null || from.length() <= 0) {
	    Address[] fa;
	    Address me;
	    if (message != null && (fa = message.getFrom()) != null &&
		    fa.length > 0)
		me = fa[0];
	    else
		me = InternetAddress.getLocalAddress(session);

	    if (me != null)
		from = ((InternetAddress)me).getAddress();
	    else
		throw new MessagingException(
					"can't determine local email address");
	}

	String cmd = "MAIL FROM:" + normalizeAddress(from);

	if (allowutf8 && supportsExtension("SMTPUTF8"))
	    cmd += " SMTPUTF8";

	// request delivery status notification?
	if (supportsExtension("DSN")) {
	    String ret = null;
	    if (message instanceof SMTPMessage)
		ret = ((SMTPMessage)message).getDSNRet();
	    if (ret == null)
		ret = session.getProperty("mail." + name + ".dsn.ret");
	    // XXX - check for legal syntax?
	    if (ret != null)
		cmd += " RET=" + ret;
	}

	/*
	 * If an RFC 2554 submitter has been specified, and the server
	 * supports the AUTH extension, include the AUTH= element on
	 * the MAIL FROM command.
	 */
	if (supportsExtension("AUTH")) {
	    String submitter = null;
	    if (message instanceof SMTPMessage)
		submitter = ((SMTPMessage)message).getSubmitter();
	    if (submitter == null)
		submitter = session.getProperty("mail." + name + ".submitter");
	    // XXX - check for legal syntax?
	    if (submitter != null) {
		try {
		    String s = xtext(submitter,
				    allowutf8 && supportsExtension("SMTPUTF8"));
		    cmd += " AUTH=" + s;
		} catch (IllegalArgumentException ex) {
		    if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "ignoring invalid submitter: " +
			    submitter, ex);
		}
	    }
	}

	/*
	 * Have any extensions to the MAIL command been specified?
	 */
	String ext = null;
	if (message instanceof SMTPMessage)
	    ext = ((SMTPMessage)message).getMailExtension();
	if (ext == null)
	    ext = session.getProperty("mail." + name + ".mailextension");
	if (ext != null && ext.length() > 0)
	    cmd += " " + ext;

	try {
	    issueSendCommand(cmd, 250);
	} catch (SMTPSendFailedException ex) {
	    int retCode = ex.getReturnCode();
	    switch (retCode) {
	    case 550: case 553: case 503: case 551: case 501:
		// given address is invalid
		try {
		    ex.setNextException(new SMTPSenderFailedException(
			new InternetAddress(from), cmd,
			retCode, ex.getMessage()));
		} catch (AddressException aex) {
		    // oh well...
		}
		break;
	    default:
		break;
	    }
	    throw ex;
	}
    }

    /**
     * Sends each address to the SMTP host using the <code>RCPT TO:</code>
     * command and copies the address either into
     * the validSentAddr or invalidAddr arrays.
     * Sets the <code>sendFailed</code>
     * flag to true if any addresses failed.
     *
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    /*
     * success/failure/error possibilities from the RCPT command
     * from rfc821, section 4.3
     * S: 250, 251
     * F: 550, 551, 552, 553, 450, 451, 452
     * E: 500, 501, 503, 421
     *
     * and how we map the above error/failure conditions to valid/invalid
     * address lists that are reported in the thrown exception:
     * invalid addr: 550, 501, 503, 551, 553
     * valid addr: 552 (quota), 450, 451, 452 (quota), 421 (srvr abort)
     */
    protected void rcptTo() throws MessagingException {
	List<InternetAddress> valid = new ArrayList<>();
	List<InternetAddress> validUnsent = new ArrayList<>();
	List<InternetAddress> invalid = new ArrayList<>();
	int retCode = -1;
	MessagingException mex = null;
	boolean sendFailed = false;
	MessagingException sfex = null;
	validSentAddr = validUnsentAddr = invalidAddr = null;
	boolean sendPartial = false;
	if (message instanceof SMTPMessage)
	    sendPartial = ((SMTPMessage)message).getSendPartial();
	if (!sendPartial)
	    sendPartial = PropUtil.getBooleanProperty(session.getProperties(),
					"mail." + name + ".sendpartial", false);
	if (sendPartial)
	    logger.fine("sendPartial set");

	boolean dsn = false;
	String notify = null;
	if (supportsExtension("DSN")) {
	    if (message instanceof SMTPMessage)
		notify = ((SMTPMessage)message).getDSNNotify();
	    if (notify == null)
		notify = session.getProperty("mail." + name + ".dsn.notify");
	    // XXX - check for legal syntax?
	    if (notify != null)
		dsn = true;
	}

	// try the addresses one at a time
	for (int i = 0; i < addresses.length; i++) {

	    sfex = null;
	    InternetAddress ia = (InternetAddress)addresses[i];
	    String cmd = "RCPT TO:" + normalizeAddress(ia.getAddress());
	    if (dsn)
		cmd += " NOTIFY=" + notify;
	    // send the addresses to the SMTP server
	    sendCommand(cmd);
	    // check the server's response for address validity
	    retCode = readServerResponse();
	    switch (retCode) {
	    case 250: case 251:
		valid.add(ia);
		if (!reportSuccess)
		    break;

		// user wants exception even when successful, including
		// details of the return code

		// create and chain the exception
		sfex = new SMTPAddressSucceededException(ia, cmd, retCode,
							lastServerResponse);
		if (mex == null)
		    mex = sfex;
		else
		    mex.setNextException(sfex);
		break;

	    case 550: case 553: case 503: case 551: case 501:
		// given address is invalid
		if (!sendPartial)
		    sendFailed = true;
		invalid.add(ia);
		// create and chain the exception
		sfex = new SMTPAddressFailedException(ia, cmd, retCode,
							lastServerResponse);
		if (mex == null)
		    mex = sfex;
		else
		    mex.setNextException(sfex);
		break;

	    case 552: case 450: case 451: case 452:
		// given address is valid
		if (!sendPartial)
		    sendFailed = true;
		validUnsent.add(ia);
		// create and chain the exception
		sfex = new SMTPAddressFailedException(ia, cmd, retCode,
							lastServerResponse);
		if (mex == null)
		    mex = sfex;
		else
		    mex.setNextException(sfex);
		break;

	    default:
		// handle remaining 4xy & 5xy codes
		if (retCode >= 400 && retCode <= 499) {
		    // assume address is valid, although we don't really know
		    validUnsent.add(ia);
		} else if (retCode >= 500 && retCode <= 599) {
		    // assume address is invalid, although we don't really know
		    invalid.add(ia);
		} else {
		    // completely unexpected response, just give up
		    if (logger.isLoggable(Level.FINE))
			logger.fine("got response code " + retCode +
			    ", with response: " + lastServerResponse);
		    String _lsr = lastServerResponse; // else rset will nuke it
		    int _lrc = lastReturnCode;
		    if (serverSocket != null)	// hasn't already been closed
			issueCommand("RSET", -1);
		    lastServerResponse = _lsr;	// restore, for get
		    lastReturnCode = _lrc;
		    throw new SMTPAddressFailedException(ia, cmd, retCode,
								_lsr);
		}
		if (!sendPartial)
		    sendFailed = true;
		// create and chain the exception
		sfex = new SMTPAddressFailedException(ia, cmd, retCode,
							lastServerResponse);
		if (mex == null)
		    mex = sfex;
		else
		    mex.setNextException(sfex);
		break;
	    }
	}

	// if we're willing to send to a partial list, and we found no
	// valid addresses, that's complete failure
	if (sendPartial && valid.size() == 0)
	    sendFailed = true;

	// copy the lists into appropriate arrays
	if (sendFailed) {
	    // copy invalid addrs
	    invalidAddr = new Address[invalid.size()];
	    invalid.toArray(invalidAddr);

	    // copy all valid addresses to validUnsent, since something failed
	    validUnsentAddr = new Address[valid.size() + validUnsent.size()];
	    int i = 0;
	    for (int j = 0; j < valid.size(); j++)
		validUnsentAddr[i++] = (Address)valid.get(j);
	    for (int j = 0; j < validUnsent.size(); j++)
		validUnsentAddr[i++] = (Address)validUnsent.get(j);
	} else if (reportSuccess || (sendPartial &&
			(invalid.size() > 0 || validUnsent.size() > 0))) {
	    // we'll go on to send the message, but after sending we'll
	    // throw an exception with this exception nested
	    sendPartiallyFailed = true;
	    exception = mex;

	    // copy invalid addrs
	    invalidAddr = new Address[invalid.size()];
	    invalid.toArray(invalidAddr);

	    // copy valid unsent addresses to validUnsent
	    validUnsentAddr = new Address[validUnsent.size()];
	    validUnsent.toArray(validUnsentAddr);

	    // copy valid addresses to validSent
	    validSentAddr = new Address[valid.size()];
	    valid.toArray(validSentAddr);
	} else {        // all addresses pass
	    validSentAddr = addresses;
	}


	// print out the debug info
	if (logger.isLoggable(Level.FINE)) {
	    if (validSentAddr != null && validSentAddr.length > 0) {
		logger.fine("Verified Addresses");
		for (int l = 0; l < validSentAddr.length; l++) {
		    logger.fine("  " + validSentAddr[l]);
		}
	    }
	    if (validUnsentAddr != null && validUnsentAddr.length > 0) {
		logger.fine("Valid Unsent Addresses");
		for (int j = 0; j < validUnsentAddr.length; j++) {
		    logger.fine("  " + validUnsentAddr[j]);
		}
	    }
	    if (invalidAddr != null && invalidAddr.length > 0) {
		logger.fine("Invalid Addresses");
		for (int k = 0; k < invalidAddr.length; k++) {
		    logger.fine("  " + invalidAddr[k]);
		}
	    }
	}

	// throw the exception, fire TransportEvent.MESSAGE_NOT_DELIVERED event
	if (sendFailed) {
	    logger.fine(
		"Sending failed because of invalid destination addresses");
	    notifyTransportListeners(TransportEvent.MESSAGE_NOT_DELIVERED,
				     validSentAddr, validUnsentAddr,
				     invalidAddr, this.message);

	    // reset the connection so more sends are allowed
	    String lsr = lastServerResponse;	// save, for get
	    int lrc = lastReturnCode;
	    try {
		if (serverSocket != null)
		    issueCommand("RSET", -1);
	    } catch (MessagingException ex) {
		// if can't reset, best to close the connection
		try {
		    close();
		} catch (MessagingException ex2) {
		    // thrown by close()--ignore, will close() later anyway
		    logger.log(Level.FINE, "close failed", ex2);
		}
	    } finally {
		lastServerResponse = lsr;	// restore
		lastReturnCode = lrc;
	    }

	    throw new SendFailedException("Invalid Addresses", mex,
					  validSentAddr,
					  validUnsentAddr, invalidAddr);
	}
    }

    /**
     * Send the <code>DATA</code> command to the SMTP host and return
     * an OutputStream to which the data is to be written.
     *
     * @return		the stream to write to
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    protected OutputStream data() throws MessagingException {
	assert Thread.holdsLock(this);
	issueSendCommand("DATA", 354);
	dataStream = new SMTPOutputStream(serverOutput);
	return dataStream;
    }

    /**
     * Terminate the sent data.
     *
     * @exception	IOException for I/O errors
     * @exception	MessagingException for other failures
     * @since JavaMail 1.4.1
     */
    protected void finishData() throws IOException, MessagingException {
	assert Thread.holdsLock(this);
	dataStream.ensureAtBOL();
	issueSendCommand(".", 250);
    }

    /**
     * Return a stream that will use the SMTP BDAT command to send data.
     *
     * @return		the stream to write to
     * @exception	MessagingException for failures
     * @since JavaMail 1.6.0
     */
    protected OutputStream bdat() throws MessagingException {
	assert Thread.holdsLock(this);
	dataStream = new BDATOutputStream(serverOutput, chunkSize);
	return dataStream;
    }

    /**
     * Terminate the sent data.
     *
     * @exception	IOException for I/O errors
     * @exception	MessagingException for other failures
     * @since JavaMail 1.6.0
     */
    protected void finishBdat() throws IOException, MessagingException {
	assert Thread.holdsLock(this);
	dataStream.ensureAtBOL();
	dataStream.close();	// doesn't close underlying socket
    }

    /**
     * Issue the <code>STARTTLS</code> command and switch the socket to
     * TLS mode if it succeeds.
     *
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    protected void startTLS() throws MessagingException {
	issueCommand("STARTTLS", 220);
	// it worked, now switch the socket into TLS mode
	try {
	    serverSocket = SocketFetcher.startTLS(serverSocket, host,
				session.getProperties(), "mail." + name);
	    initStreams();
	} catch (IOException ioex) {
	    closeConnection();
	    throw new MessagingException("Could not convert socket to TLS",
								ioex);
	}
    }

    /////// primitives ///////

    /**
     * Connect to host on port and start the SMTP protocol.
     */
    private void openServer(String host, int port)
				throws MessagingException {

        if (logger.isLoggable(Level.FINE))
	    logger.fine("trying to connect to host \"" + host +
				"\", port " + port + ", isSSL " + isSSL);

	try {
	    Properties props = session.getProperties();

	    serverSocket = SocketFetcher.getSocket(host, port,
		props, "mail." + name, isSSL);

	    // socket factory may've chosen a different port,
	    // update it for the debug messages that follow
	    port = serverSocket.getPort();
	    // save host name for startTLS
	    this.host = host;

	    initStreams();

	    int r = -1;
	    if ((r = readServerResponse()) != 220) {
		String failResponse = lastServerResponse;
		try {
		    if (quitOnSessionReject) {
			sendCommand("QUIT");
			if (quitWait) {
			    int resp = readServerResponse();
			    if (resp != 221 && resp != -1 &&
				    logger.isLoggable(Level.FINE))
				logger.fine("QUIT failed with " + resp);
			}
		    }
		} catch (Exception e) {
		    if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "QUIT failed", e);
		} finally {
		    serverSocket.close();
		    serverSocket = null;
		    serverOutput = null;
		    serverInput = null;
		    lineInputStream = null;
		}
		if (logger.isLoggable(Level.FINE))
		    logger.fine("got bad greeting from host \"" +
				host + "\", port: " + port +
				", response: " + failResponse);
		throw new MessagingException(
				"Got bad greeting from SMTP host: " + host +
				", port: " + port +
				", response: " + failResponse);
	    } else {
		if (logger.isLoggable(Level.FINE))
		    logger.fine("connected to host \"" +
				       host + "\", port: " + port);
	    }
	} catch (UnknownHostException uhex) {
	    throw new MessagingException("Unknown SMTP host: " + host, uhex);
	} catch (SocketConnectException scex) {
	    throw new MailConnectException(scex);
	} catch (IOException ioe) {
	    throw new MessagingException("Could not connect to SMTP host: " +
				    host + ", port: " + port, ioe);
	}
    }

    /**
     * Start the protocol to the server on serverSocket,
     * assumed to be provided and connected by the caller.
     */
    private void openServer() throws MessagingException {
	int port = -1;
	host = "UNKNOWN";
	try {
	    port = serverSocket.getPort();
	    host = serverSocket.getInetAddress().getHostName();
	    if (logger.isLoggable(Level.FINE))
		logger.fine("starting protocol to host \"" +
					host + "\", port " + port);

	    initStreams();

	    int r = -1;
	    if ((r = readServerResponse()) != 220) {
        try {
            if (quitOnSessionReject) {
                sendCommand("QUIT");
                if (quitWait) {
                    int resp = readServerResponse();
                    if (resp != 221 && resp != -1 &&
                        logger.isLoggable(Level.FINE))
                        logger.fine("QUIT failed with " + resp);
                }
            }
        } catch (Exception e) {
            if (logger.isLoggable(Level.FINE))
                logger.log(Level.FINE, "QUIT failed", e);
        } finally {
            serverSocket.close();
            serverSocket = null;
            serverOutput = null;
            serverInput = null;
            lineInputStream = null;
        }
		if (logger.isLoggable(Level.FINE))
		    logger.fine("got bad greeting from host \"" +
				    host + "\", port: " + port +
				    ", response: " + r);
		throw new MessagingException(
			"Got bad greeting from SMTP host: " + host +
				    ", port: " + port +
				    ", response: " + r);
	    } else {
		if (logger.isLoggable(Level.FINE))
		    logger.fine("protocol started to host \"" +
				       host + "\", port: " + port);
	    }
	} catch (IOException ioe) {
	    throw new MessagingException(
				    "Could not start protocol to SMTP host: " +
				    host + ", port: " + port, ioe);
	}
    }


    private void initStreams() throws IOException {
	boolean quote = PropUtil.getBooleanProperty(session.getProperties(),
					"mail.debug.quote", false);

	traceInput =
	    new TraceInputStream(serverSocket.getInputStream(), traceLogger);
	traceInput.setQuote(quote);

	traceOutput =
	    new TraceOutputStream(serverSocket.getOutputStream(), traceLogger);
	traceOutput.setQuote(quote);

	serverOutput =
	    new BufferedOutputStream(traceOutput);
	serverInput =
	    new BufferedInputStream(traceInput);
	lineInputStream = new LineInputStream(serverInput);
    }

    /**
     * Is protocol tracing enabled?
     */
    private boolean isTracing() {
	return traceLogger.isLoggable(Level.FINEST);
    }

    /**
     * Temporarily turn off protocol tracing, e.g., to prevent
     * tracing the authentication sequence, including the password.
     */
    private void suspendTracing() {
	if (traceLogger.isLoggable(Level.FINEST)) {
	    traceInput.setTrace(false);
	    traceOutput.setTrace(false);
	}
    }

    /**
     * Resume protocol tracing, if it was enabled to begin with.
     */
    private void resumeTracing() {
	if (traceLogger.isLoggable(Level.FINEST)) {
	    traceInput.setTrace(true);
	    traceOutput.setTrace(true);
	}
    }

    /**
     * Send the command to the server.  If the expected response code
     * is not received, throw a MessagingException.
     *
     * @param	cmd	the command to send
     * @param	expect	the expected response code (-1 means don't care)
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    public synchronized void issueCommand(String cmd, int expect)
				throws MessagingException {
	sendCommand(cmd);

	// if server responded with an unexpected return code,
	// throw the exception, notifying the client of the response
	int resp = readServerResponse();
	if (expect != -1 && resp != expect)
	    throw new MessagingException(lastServerResponse);
    }

    /**
     * Issue a command that's part of sending a message.
     */
    private void issueSendCommand(String cmd, int expect)
				throws MessagingException {
	sendCommand(cmd);

	// if server responded with an unexpected return code,
	// throw the exception, notifying the client of the response
	int ret;
	if ((ret = readServerResponse()) != expect) {
	    // assume message was not sent to anyone,
	    // combine valid sent & unsent addresses
	    int vsl = validSentAddr == null ? 0 : validSentAddr.length;
	    int vul = validUnsentAddr == null ? 0 : validUnsentAddr.length;
	    Address[] valid = new Address[vsl + vul];
	    if (vsl > 0)
		System.arraycopy(validSentAddr, 0, valid, 0, vsl);
	    if (vul > 0)
		System.arraycopy(validUnsentAddr, 0, valid, vsl, vul);
	    validSentAddr = null;
	    validUnsentAddr = valid;
	    if (logger.isLoggable(Level.FINE))
		logger.fine("got response code " + ret +
		    ", with response: " + lastServerResponse);
	    String _lsr = lastServerResponse; // else rset will nuke it
	    int _lrc = lastReturnCode;
	    if (serverSocket != null)	// hasn't already been closed
		issueCommand("RSET", -1);
	    lastServerResponse = _lsr;	// restore, for get
	    lastReturnCode = _lrc;
	    throw new SMTPSendFailedException(cmd, ret, lastServerResponse,
			exception, validSentAddr, validUnsentAddr, invalidAddr);
	}
    }

    /**
     * Send the command to the server and return the response code
     * from the server.
     *
     * @param	cmd	the command
     * @return		the response code
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    public synchronized int simpleCommand(String cmd)
				throws MessagingException {
	sendCommand(cmd);
	return readServerResponse();
    }

    /**
     * Send the command to the server and return the response code
     * from the server.
     *
     * @param	cmd	the command
     * @return		the response code
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    protected int simpleCommand(byte[] cmd) throws MessagingException {
	assert Thread.holdsLock(this);
	sendCommand(cmd);
	return readServerResponse();
    }

    /**
     * Sends command <code>cmd</code> to the server terminating
     * it with <code>CRLF</code>.
     *
     * @param	cmd	the command
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    protected void sendCommand(String cmd) throws MessagingException {
	sendCommand(toBytes(cmd));
    }

    private void sendCommand(byte[] cmdBytes) throws MessagingException {
	assert Thread.holdsLock(this);
	//if (logger.isLoggable(Level.FINE))
	    //logger.fine("SENT: " + new String(cmdBytes, 0));

        try {
	    serverOutput.write(cmdBytes);
	    serverOutput.write(CRLF);
	    serverOutput.flush();
	} catch (IOException ex) {
	    throw new MessagingException("Can't send command to SMTP host", ex);
	}
    }

    /**
     * Reads server reponse returning the <code>returnCode</code>
     * as the number.  Returns -1 on failure. Sets
     * <code>lastServerResponse</code> and <code>lastReturnCode</code>.
     *
     * @return		server response code
     * @exception	MessagingException for failures
     * @since JavaMail 1.4.1
     */
    protected int readServerResponse() throws MessagingException {
	assert Thread.holdsLock(this);
        String serverResponse = "";
        int returnCode = 0;
	StringBuilder buf = new StringBuilder(100);

	// read the server response line(s) and add them to the buffer
	// that stores the response
        try {
	    String line = null;

	    do {
		line = lineInputStream.readLine();
		if (line == null) {
		    serverResponse = buf.toString();
		    if (serverResponse.length() == 0)
			serverResponse = "[EOF]";
		    lastServerResponse = serverResponse;
		    lastReturnCode = -1;
		    logger.log(Level.FINE, "EOF: {0}", serverResponse);
		    return -1;
		}
		buf.append(line);
		buf.append("\n");
	    } while (isNotLastLine(line));

            serverResponse = buf.toString();
        } catch (IOException ioex) {
	    logger.log(Level.FINE, "exception reading response", ioex);
            //ioex.printStackTrace(out);
	    lastServerResponse = "";
	    lastReturnCode = 0;
	    throw new MessagingException("Exception reading response", ioex);
            //returnCode = -1;
        }

	// print debug info
        //if (logger.isLoggable(Level.FINE))
            //logger.fine("RCVD: " + serverResponse);

	// parse out the return code
        if (serverResponse.length() >= 3) {
            try {
                returnCode = Integer.parseInt(serverResponse.substring(0, 3));
            } catch (NumberFormatException nfe) {
		try {
		    close();
		} catch (MessagingException mex) {
		    // thrown by close()--ignore, will close() later anyway
		    logger.log(Level.FINE, "close failed", mex);
		}
		returnCode = -1;
            } catch (StringIndexOutOfBoundsException ex) {
		try {
		    close();
		} catch (MessagingException mex) {
		    // thrown by close()--ignore, will close() later anyway
		    logger.log(Level.FINE, "close failed", mex);
		}
                returnCode = -1;
	    }
	} else {
	    returnCode = -1;
	}
	if (returnCode == -1)
	    logger.log(Level.FINE, "bad server response: {0}", serverResponse);

        lastServerResponse = serverResponse;
	lastReturnCode = returnCode;
        return returnCode;
    }

    /**
     * Check if we're in the connected state.  Don't bother checking
     * whether the server is still alive, that will be detected later.
     *
     * @exception	IllegalStateException	if not connected
     *
     * @since JavaMail 1.4.1
     */
    protected void checkConnected() {
	if (!super.isConnected())
	    throw new IllegalStateException("Not connected");
    }

    // tests if the <code>line</code> is an intermediate line according to SMTP
    private boolean isNotLastLine(String line) {
        return line != null && line.length() >= 4 && line.charAt(3) == '-';
    }

    // wraps an address in "<>"'s if necessary
    private String normalizeAddress(String addr) {
	if ((!addr.startsWith("<")) && (!addr.endsWith(">")))
	    return "<" + addr + ">";
	else
	    return addr;
    }

    /**
     * Return true if the SMTP server supports the specified service
     * extension.  Extensions are reported as results of the EHLO
     * command when connecting to the server. See
     * <A HREF="http://www.ietf.org/rfc/rfc1869.txt">RFC 1869</A>
     * and other RFCs that define specific extensions.
     *
     * @param	ext	the service extension name
     * @return		true if the extension is supported
     *
     * @since JavaMail 1.3.2
     */
    public boolean supportsExtension(String ext) {
	return extMap != null &&
			extMap.get(ext.toUpperCase(Locale.ENGLISH)) != null;
    }

    /**
     * Return the parameter the server provided for the specified
     * service extension, or null if the extension isn't supported.
     *
     * @param	ext	the service extension name
     * @return		the extension parameter
     *
     * @since JavaMail 1.3.2
     */
    public String getExtensionParameter(String ext) {
	return extMap == null ? null :
			extMap.get(ext.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Does the server we're connected to support the specified
     * authentication mechanism?  Uses the extension information
     * returned by the server from the EHLO command.
     *
     * @param	auth	the authentication mechanism
     * @return		true if the authentication mechanism is supported
     *
     * @since JavaMail 1.4.1
     */
    protected boolean supportsAuthentication(String auth) {
	assert Thread.holdsLock(this);
	if (extMap == null)
	    return false;
	String a = extMap.get("AUTH");
	if (a == null)
	    return false;
	StringTokenizer st = new StringTokenizer(a);
	while (st.hasMoreTokens()) {
	    String tok = st.nextToken();
	    if (tok.equalsIgnoreCase(auth))
		return true;
	}
	// hack for buggy servers that advertise capability incorrectly
	if (auth.equalsIgnoreCase("LOGIN") && supportsExtension("AUTH=LOGIN")) {
	    logger.fine("use AUTH=LOGIN hack");
	    return true;
	}
	return false;
    }

    private static char[] hexchar = {
	'0', '1', '2', '3', '4', '5', '6', '7',
	'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * Convert a string to RFC 1891 xtext format.
     *
     * <pre>
     *     xtext = *( xchar / hexchar )
     *
     *     xchar = any ASCII CHAR between "!" (33) and "~" (126) inclusive,
     *          except for "+" and "=".
     *
     * ; "hexchar"s are intended to encode octets that cannot appear
     * ; as ASCII characters within an esmtp-value.
     *
     *     hexchar = ASCII "+" immediately followed by two upper case
     *          hexadecimal digits
     * </pre>
     *
     * @param	s	the string to convert
     * @return	the xtext format string
     * @since JavaMail 1.4.1
     */
    // XXX - keeping this around only for compatibility
    protected static String xtext(String s) {
	return xtext(s, false);
    }

    /**
     * Like xtext(s), but allow UTF-8 strings.
     *
     * @param	s	the string to convert
     * @param	utf8	convert string to UTF-8 first?
     * @return	the xtext format string
     * @since JavaMail 1.6.0
     */
    protected static String xtext(String s, boolean utf8) {
	StringBuilder sb = null;
	byte[] bytes;
	if (utf8)
	    bytes = s.getBytes(StandardCharsets.UTF_8);
	else
	    bytes = ASCIIUtility.getBytes(s);
	for (int i = 0; i < bytes.length; i++) {
	    char c = (char)(((int)bytes[i])&0xff);
	    if (!utf8 && c >= 128)	// not ASCII
		throw new IllegalArgumentException(
			    "Non-ASCII character in SMTP submitter: " + s);
	    if (c < '!' || c > '~' || c == '+' || c == '=') {
		// not printable ASCII
		if (sb == null) {
		    sb = new StringBuilder(s.length() + 4);
		    sb.append(s.substring(0, i));
		}
		sb.append('+');
		sb.append(hexchar[(((int)c)& 0xf0) >> 4]);
		sb.append(hexchar[((int)c)& 0x0f]);
	    } else {
		if (sb != null)
		    sb.append(c);
	    }
	}
	return sb != null ? sb.toString() : s;
    }

    private String traceUser(String user) {
	return debugusername ? user : "<user name suppressed>";
    }

    private String tracePassword(String password) {
	return debugpassword ? password :
				(password == null ? "<null>" : "<non-null>");
    }

    /**
     * Convert the String to either ASCII or UTF-8 bytes
     * depending on allowutf8.
     */
    private byte[] toBytes(String s) {
	if (allowutf8)
	    return s.getBytes(StandardCharsets.UTF_8);
	else
	    // don't use StandardCharsets.US_ASCII because it rejects non-ASCII
	    return ASCIIUtility.getBytes(s);
    }

    /*
     * Probe points for GlassFish monitoring.
     */
    private void sendMessageStart(String subject) { }
    private void sendMessageEnd() { }


    /**
     * An SMTPOutputStream that wraps a ChunkedOutputStream.
     */
    private class BDATOutputStream extends SMTPOutputStream {

	/**
	 * Create a BDATOutputStream that wraps a ChunkedOutputStream
	 * of the given size and built on top of the specified
	 * underlying output stream.
	 *
	 * @param	out	the underlying output stream
	 * @param	size	the chunk size
	 */
	public BDATOutputStream(OutputStream out, int size) {
	    super(new ChunkedOutputStream(out, size));
	}

	/**
	 * Close this output stream.
	 *
	 * @exception	IOException	for I/O errors
	 */
	@Override
	public void close() throws IOException {
	    out.close();
	}
    }

    /**
     * An OutputStream that buffers data in chunks and uses the
     * RFC 3030 BDAT SMTP command to send each chunk.
     */
    private class ChunkedOutputStream extends OutputStream {
	private final OutputStream out;
	private final byte[] buf;
	private int count = 0;

	/**
	 * Create a ChunkedOutputStream built on top of the specified
	 * underlying output stream.
	 *
	 * @param	out	the underlying output stream
	 * @param	size	the chunk size
	 */
	public ChunkedOutputStream(OutputStream out, int size) {
	    this.out = out;
	    buf = new byte[size];
	}

	/**
	 * Writes the specified <code>byte</code> to this output stream.
	 *
	 * @param	b	the byte to write
	 * @exception	IOException	for I/O errors
	 */
	@Override
	public void write(int b) throws IOException {
	    buf[count++] = (byte)b;
	    if (count >= buf.length)
		flush();
	}

	/**
	 * Writes len bytes to this output stream starting at off.
	 *
	 * @param	b	bytes to write
	 * @param	off	offset in array
	 * @param	len	number of bytes to write
	 * @exception	IOException	for I/O errors
	 */
	@Override
	public void write(byte b[], int off, int len) throws IOException {
	    while (len > 0) {
		int size = Math.min(buf.length - count, len);
		if (size == buf.length) {
		    // avoid the copy
		    bdat(b, off, size, false);
		} else {
		    System.arraycopy(b, off, buf, count, size);
		    count += size;
		}
		off += size;
		len -= size;
		if (count >= buf.length)
		    flush();
	    }
	}

	/**
	 * Flush this output stream.
	 *
	 * @exception	IOException	for I/O errors
	 */
	@Override
	public void flush() throws IOException {
	    bdat(buf, 0, count, false);
	    count = 0;
	}

	/**
	 * Close this output stream.
	 *
	 * @exception	IOException	for I/O errors
	 */
	@Override
	public void close() throws IOException {
	    bdat(buf, 0, count, true);
	    count = 0;
	}

	/**
	 * Send the specified bytes using the BDAT command.
	 */
	private void bdat(byte[] b, int off, int len, boolean last)
				throws IOException {
	    if (len > 0 || last) {
		try {
		    if (last)
			sendCommand("BDAT " + len + " LAST");
		    else
			sendCommand("BDAT " + len);
		    out.write(b, off, len);
		    out.flush();
		    int ret = readServerResponse();
		    if (ret != 250)
			throw new IOException(lastServerResponse);
		} catch (MessagingException mex) {
		    throw new IOException("BDAT write exception", mex);
		}
	    }
	}
    }
}
