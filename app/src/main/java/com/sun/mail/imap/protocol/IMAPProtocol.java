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

package com.sun.mail.imap.protocol;

import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.nio.charset.StandardCharsets;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.*;

import com.sun.mail.util.PropUtil;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.iap.*;
import com.sun.mail.auth.Ntlm;

import com.sun.mail.imap.ACL;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.CopyUID;
import com.sun.mail.imap.SortTerm;
import com.sun.mail.imap.ResyncData;
import com.sun.mail.imap.Utility;

/**
 * This class extends the iap.Protocol object and implements IMAP
 * semantics. In general, there is a method corresponding to each
 * IMAP protocol command. The typical implementation issues the
 * appropriate protocol command, collects all responses, processes
 * those responses that are specific to this command and then
 * dispatches the rest (the unsolicited ones) to the dispatcher
 * using the <code>notifyResponseHandlers(r)</code>.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class IMAPProtocol extends Protocol {
    
    private boolean connected = false;	// did constructor succeed?
    private boolean rev1 = false;	// REV1 server ?
    private boolean referralException;	// throw exception for IMAP REFERRAL?
    private boolean noauthdebug = true;	// hide auth info in debug output
    private boolean authenticated;	// authenticated?
    // WARNING: authenticated may be set to true in superclass
    //		constructor, don't initialize it here.

    private Map<String, String> capabilities;
    // WARNING: capabilities may be initialized as a result of superclass
    //		constructor, don't initialize it here.
    private List<String> authmechs;
    // WARNING: authmechs may be initialized as a result of superclass
    //		constructor, don't initialize it here.
    private boolean utf8;		// UTF-8 support enabled?

    protected SearchSequence searchSequence;
    protected String[] searchCharsets; 	// array of search charsets

    protected Set<String> enabled;	// enabled capabilities - RFC 5161

    private String name;
    private SaslAuthenticator saslAuthenticator;	// if SASL is being used
    private String proxyAuthUser;	// user name used with PROXYAUTH

    private ByteArray ba;		// a buffer for fetchBody

    private static final byte[] CRLF = { (byte)'\r', (byte)'\n'};

    private static final FetchItem[] fetchItems = { };

    /**
     * Constructor.
     * Opens a connection to the given host at given port.
     *
     * @param	name	the protocol name
     * @param	host	host to connect to
     * @param	port	port number to connect to
     * @param	props	Properties object used by this protocol
     * @param	isSSL	true if SSL should be used
     * @param	logger	the MailLogger to use for debug output
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public IMAPProtocol(String name, String host, int port, 
			Properties props, boolean isSSL, MailLogger logger)
			throws IOException, ProtocolException {
	super(host, port, props, "mail." + name, isSSL, logger);

	try {
	    this.name = name;
	    noauthdebug =
		!PropUtil.getBooleanProperty(props, "mail.debug.auth", false);

	    // in case it was not initialized in processGreeting
	    referralException = PropUtil.getBooleanProperty(props,
				prefix + ".referralexception", false);

	    if (capabilities == null)
		capability();

	    if (hasCapability("IMAP4rev1"))
		rev1 = true;

	    searchCharsets = new String[2]; // 2, for now.
	    searchCharsets[0] = "UTF-8";
	    searchCharsets[1] = MimeUtility.mimeCharset(
				    MimeUtility.getDefaultJavaCharset()
				);

	    connected = true;	// must be last statement in constructor
	} finally {
	    /*
	     * If we get here because an exception was thrown, we need
	     * to disconnect to avoid leaving a connected socket that
	     * no one will be able to use because this object was never
	     * completely constructed.
	     */
	    if (!connected)
		disconnect();
	}
    }

    /**
     * Constructor for debugging.
     *
     * @param	in	the InputStream from which to read
     * @param	out	the PrintStream to which to write
     * @param	props	Properties object used by this protocol
     * @param	debug	true to enable debugging output
     * @exception	IOException	for I/O errors
     */
    public IMAPProtocol(InputStream in, PrintStream out,
			Properties props, boolean debug)
			throws IOException {
	super(in, out, props, debug);

	this.name = "imap";
	noauthdebug =
	    !PropUtil.getBooleanProperty(props, "mail.debug.auth", false);

	if (capabilities == null)
	    capabilities = new HashMap<>();

	searchCharsets = new String[2]; // 2, for now.
	searchCharsets[0] = "UTF-8";
	searchCharsets[1] = MimeUtility.mimeCharset(
				MimeUtility.getDefaultJavaCharset()
			    );

	connected = true;	// must be last statement in constructor
    }

    /**
     * Return an array of FetchItem objects describing the
     * FETCH items supported by this protocol.  Subclasses may
     * override this method to combine their FetchItems with
     * the FetchItems returned by the superclass.
     *
     * @return	an array of FetchItem objects
     * @since JavaMail 1.4.6
     */
    public FetchItem[] getFetchItems() {
	return fetchItems;
    }

    /**
     * CAPABILITY command.
     *
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.1.1"
     */
    public void capability() throws ProtocolException {
	// Check CAPABILITY
	Response[] r = command("CAPABILITY", null);
	Response response = r[r.length-1];

	if (response.isOK())
	    handleCapabilityResponse(r);
	handleResult(response);
    }

    /**
     * Handle any untagged CAPABILITY response in the Response array.
     *
     * @param	r	the responses
     */
    public void handleCapabilityResponse(Response[] r) {
	boolean first = true;
	for (int i = 0, len = r.length; i < len; i++) {
	    if (!(r[i] instanceof IMAPResponse))
		continue;

	    IMAPResponse ir = (IMAPResponse)r[i];

	    // Handle *all* untagged CAPABILITY responses.
	    // Though the spec seemingly states that only
	    // one CAPABILITY response string is allowed (6.1.1),
	    // some server vendors claim otherwise.
	    if (ir.keyEquals("CAPABILITY")) {
		if (first) {
		    // clear out current when first response seen
		    capabilities = new HashMap<>(10);
		    authmechs = new ArrayList<>(5);
		    first = false;
		}
		parseCapabilities(ir);
	    }
	}
    }

    /**
     * If the response contains a CAPABILITY response code, extract
     * it and save the capabilities.
     *
     * @param	r	the response
     */
    protected void setCapabilities(Response r) {
	byte b;
	while ((b = r.readByte()) > 0 && b != (byte)'[')
	    ;
	if (b == 0)
	    return;
	String s;
	s = r.readAtom();
	if (!s.equalsIgnoreCase("CAPABILITY"))
	    return;
	capabilities = new HashMap<>(10);
	authmechs = new ArrayList<>(5);
	parseCapabilities(r);
    }

    /**
     * Parse the capabilities from a CAPABILITY response or from
     * a CAPABILITY response code attached to (e.g.) an OK response.
     *
     * @param	r	the CAPABILITY response
     */
    protected void parseCapabilities(Response r) {
	String s;
	while ((s = r.readAtom()) != null) {
	    if (s.length() == 0) {
		if (r.peekByte() == (byte)']')
		    break;
		/*
		 * Probably found something here that's not an atom.
		 * Rather than loop forever or fail completely, we'll
		 * try to skip this bogus capability.  This is known
		 * to happen with:
		 *   Netscape Messaging Server 4.03 (built Apr 27 1999)
		 * that returns:
		 *   * CAPABILITY * CAPABILITY IMAP4 IMAP4rev1 ...
		 * The "*" in the middle of the capability list causes
		 * us to loop forever here.
		 */
		r.skipToken();
	    } else {
		capabilities.put(s.toUpperCase(Locale.ENGLISH), s);
		if (s.regionMatches(true, 0, "AUTH=", 0, 5)) {
		    authmechs.add(s.substring(5));
		    if (logger.isLoggable(Level.FINE))
			logger.fine("AUTH: " + s.substring(5));
		}
	    }
	}
    }

    /**
     * Check the greeting when first connecting; look for PREAUTH response.
     *
     * @param	r	the greeting response
     * @exception	ProtocolException	for protocol failures
     */
    @Override
    protected void processGreeting(Response r) throws ProtocolException {
	if (r.isBYE()) {
	    checkReferral(r);	// may throw exception
	    throw new ConnectionException(this, r);
	}
	if (r.isOK()) {			// check if it's OK
	    // XXX - is a REFERRAL response code really allowed here?
	    // XXX - referralException hasn't been initialized in c'tor yet
	    referralException = PropUtil.getBooleanProperty(props,
				prefix + ".referralexception", false);
	    if (referralException)
		checkReferral(r);
	    setCapabilities(r);
	    return;
	}
	// only other choice is PREAUTH
	assert r instanceof IMAPResponse;
	IMAPResponse ir = (IMAPResponse)r;
	if (ir.keyEquals("PREAUTH")) {
	    authenticated = true;
	    setCapabilities(r);
	} else {
	    disconnect();
	    throw new ConnectionException(this, r);
	}
    }

    /**
     * Check for an IMAP login REFERRAL response code.
     *
     * @exception	IMAPReferralException	if REFERRAL response code found
     * @see "RFC 2221"
     */
    private void checkReferral(Response r) throws IMAPReferralException {
	String s = r.getRest();	// get the text after the response
	if (s.startsWith("[")) {	// a response code
	    int i = s.indexOf(' ');
	    if (i > 0 && s.substring(1, i).equalsIgnoreCase("REFERRAL")) {
		String url, msg;
		int j = s.indexOf(']');
		if (j > 0) {	// should always be true;
		    url = s.substring(i + 1, j);
		    msg = s.substring(j + 1).trim();
		} else {
		    url = s.substring(i + 1);
		    msg = "";
		}
		if (r.isBYE())
		    disconnect();
		throw new IMAPReferralException(msg, url);
	    }
	}
    }

    /**
     * Returns <code>true</code> if the connection has been authenticated,
     * either due to a successful login, or due to a PREAUTH greeting response.
     *
     * @return	true if the connection has been authenticated
     */
    public boolean isAuthenticated() {
	return authenticated;
    }

    /**
     * Returns <code>true</code> if this is an IMAP4rev1 server
     *
     * @return	true if this is an IMAP4rev1 server
     */
    public boolean isREV1() {
	return rev1;
    }

    /**
     * Returns whether this Protocol supports non-synchronizing literals.
     *
     * @return	true if non-synchronizing literals are supported
     */
    @Override
    protected boolean supportsNonSyncLiterals() {
	return hasCapability("LITERAL+");
    }

    /**
     * Read a response from the server.
     *
     * @return	the response
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    @Override
    public Response readResponse() throws IOException, ProtocolException {
	// assert Thread.holdsLock(this);
	// can't assert because it's called from constructor
	IMAPResponse r = new IMAPResponse(this);
	if (r.keyEquals("FETCH"))
	    r = new FetchResponse(r, getFetchItems());
	return r;
    }

    /**
     * Check whether the given capability is supported by
     * this server. Returns <code>true</code> if so, otherwise
     * returns false.
     *
     * @param	c	the capability name
     * @return		true if the server has the capability
     */
    public boolean hasCapability(String c) {
	if (c.endsWith("*")) {
	    c = c.substring(0, c.length() - 1).toUpperCase(Locale.ENGLISH);
	    Iterator<String> it = capabilities.keySet().iterator();
	    while (it.hasNext()) {
		if (it.next().startsWith(c))
		    return true;
	    }
	    return false;
	}
	return capabilities.containsKey(c.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Return the map of capabilities returned by the server.
     *
     * @return	the Map of capabilities
     * @since	JavaMail 1.4.1
     */
    public Map<String, String> getCapabilities() {
	return capabilities;
    }

    /**
     * Does the server support UTF-8?
     *
     * @since JavaMail 1.6.0
     */
    public boolean supportsUtf8() {
	return utf8;
    }

    /**
     * Close socket connection.
     *
     * This method just makes the Protocol.disconnect() method
     * public.
     */
    @Override
    public void disconnect() {
	super.disconnect();
	authenticated = false;	// just in case
    }

    /**
     * The NOOP command.
     *
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.1.2"
     */
    public void noop() throws ProtocolException {
	logger.fine("IMAPProtocol noop");
	simpleCommand("NOOP", null);
    }

    /**
     * LOGOUT Command.
     *
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.1.3"
     */
    public void logout() throws ProtocolException {
	try {
		if (!authenticated)
			return;

	    Response[] r = command("LOGOUT", null);

	    authenticated = false;
	    // dispatch any unsolicited responses.
	    //  NOTE that the BYE response is dispatched here as well
	    notifyResponseHandlers(r);
	} finally {
	    disconnect();
	}
    }

    /**
     * LOGIN Command.
     * 
     * @param  u		the username
     * @param  p		the password
     * @throws ProtocolException as thrown by {@link Protocol#handleResult}.
     * @see "RFC2060, section 6.2.2"
     */
    public void login(String u, String p) throws ProtocolException {
	Argument args = new Argument();
	args.writeString(u);
	args.writeString(p);

	Response[] r = null;
	try {
	    if (noauthdebug && isTracing()) {
		logger.fine("LOGIN command trace suppressed");
		suspendTracing();
	    }
	    r = command("LOGIN", args);
	} finally {
	    resumeTracing();
	}

	// handle an illegal but not uncommon untagged CAPABILTY response
	handleCapabilityResponse(r);

	// dispatch untagged responses
	notifyResponseHandlers(r);

	// Handle result of this command
	if (noauthdebug && isTracing())
	    logger.fine("LOGIN command result: " + r[r.length-1]);
	handleLoginResult(r[r.length-1]);
	// If the response includes a CAPABILITY response code, process it
	setCapabilities(r[r.length-1]);
	// if we get this far without an exception, we're authenticated
	authenticated = true;
    }

    /**
     * The AUTHENTICATE command with AUTH=LOGIN authenticate scheme
     *
     * @param  u		the username
     * @param  p		the password
     * @throws ProtocolException as thrown by {@link Protocol#handleResult}.
     * @see "RFC2060, section 6.2.1"
     */
    public synchronized void authlogin(String u, String p)
				throws ProtocolException {
	List<Response> v = new ArrayList<>();
	String tag = null;
	Response r = null;
	boolean done = false;

	try {

	if (noauthdebug && isTracing()) {
	    logger.fine("AUTHENTICATE LOGIN command trace suppressed");
	    suspendTracing();
	}

	try {
	    tag = writeCommand("AUTHENTICATE LOGIN", null);
	} catch (Exception ex) {
	    // Convert this into a BYE response
	    r = Response.byeResponse(ex);
	    done = true;
	}

	OutputStream os = getOutputStream(); // stream to IMAP server

	/* Wrap a BASE64Encoder around a ByteArrayOutputstream
	 * to craft b64 encoded username and password strings
	 *
	 * Note that the encoded bytes should be sent "as-is" to the
	 * server, *not* as literals or quoted-strings.
	 *
	 * Also note that unlike the B64 definition in MIME, CRLFs 
	 * should *not* be inserted during the encoding process. So, I
	 * use Integer.MAX_VALUE (0x7fffffff (> 1G)) as the bytesPerLine,
	 * which should be sufficiently large !
	 *
	 * Finally, format the line in a buffer so it can be sent as
	 * a single packet, to avoid triggering a bug in SUN's SIMS 2.0
	 * server caused by patch 105346.
	 */

	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);
	boolean first = true;

	while (!done) { // loop till we are done
	    try {
		r = readResponse();
	    	if (r.isContinuation()) {
		    // Server challenge ..
		    String s;
		    if (first) { // Send encoded username
			s = u;
			first = false;
		    } else 	// Send encoded password
			s = p;
		    
		    // obtain b64 encoded bytes
		    b64os.write(s.getBytes(StandardCharsets.UTF_8));
		    b64os.flush(); 	// complete the encoding

		    bos.write(CRLF); 	// CRLF termination
		    os.write(bos.toByteArray()); // write out line
		    os.flush(); 	// flush the stream
		    bos.reset(); 	// reset buffer
		} else if (r.isTagged() && r.getTag().equals(tag))
		    // Ah, our tagged response
		    done = true;
		else if (r.isBYE()) // outta here
		    done = true;
		// hmm .. unsolicited response here ?!
	    } catch (Exception ioex) {
		// convert this into a BYE response
		r = Response.byeResponse(ioex);
		done = true;
	    }
	    v.add(r);
	}

	} finally {
	    resumeTracing();
	}

	Response[] responses = v.toArray(new Response[v.size()]);

	// handle an illegal but not uncommon untagged CAPABILTY response
	handleCapabilityResponse(responses);

	/*
	 * Dispatch untagged responses.
	 * NOTE: in our current upper level IMAP classes, we add the
	 * responseHandler to the Protocol object only *after* the 
	 * connection has been authenticated. So, for now, the below
	 * code really ends up being just a no-op.
	 */
	notifyResponseHandlers(responses);

	// Handle the final OK, NO, BAD or BYE response
	if (noauthdebug && isTracing())
	    logger.fine("AUTHENTICATE LOGIN command result: " + r);
	handleLoginResult(r);
	// If the response includes a CAPABILITY response code, process it
	setCapabilities(r);
	// if we get this far without an exception, we're authenticated
	authenticated = true;
    }

	public synchronized void authclassic(String u, String p)
			throws ProtocolException {
		List<Response> v = new ArrayList<>();
		String tag = null;
		Response r = null;
		boolean done = false;

		try {

			if (noauthdebug && isTracing()) {
				logger.fine("LOGIN command trace suppressed");
				suspendTracing();
			}

			try {
				Argument arg = new Argument();
				arg.writeNString(u);
				arg.writeNString(p);
				tag = writeCommand("LOGIN", arg);
			} catch (Exception ex) {
				r = Response.byeResponse(ex);
				done = true;
			}

			while (!done) {
				try {
					r = readResponse();
					if (r.isTagged() && r.getTag().equals(tag))
						done = true;
					else if (r.isBYE()) // outta here
						done = true;
				} catch (Exception ioex) {
					r = Response.byeResponse(ioex);
					done = true;
				}
				v.add(r);
			}

		} finally {
			resumeTracing();
		}

		Response[] responses = v.toArray(new Response[v.size()]);

		handleCapabilityResponse(responses);
		notifyResponseHandlers(responses);

		if (noauthdebug && isTracing())
			logger.fine("LOGIN command result: " + r);
		handleLoginResult(r);
		setCapabilities(r);
		authenticated = true;
	}

    /**
     * The AUTHENTICATE command with AUTH=PLAIN authentication scheme.
     * This is based heavly on the {@link #authlogin} method.
     *
     * @param  authzid		the authorization id
     * @param  u		the username
     * @param  p		the password
     * @throws ProtocolException as thrown by {@link Protocol#handleResult}.
     * @see "RFC3501, section 6.2.2"
     * @see "RFC2595, section 6"
     * @since  JavaMail 1.3.2
     */
    public synchronized void authplain(String authzid, String u, String p)
				throws ProtocolException {
	List<Response> v = new ArrayList<>();
	String tag = null;
	Response r = null;
	boolean done = false;

	try {

	if (noauthdebug && isTracing()) {
	    logger.fine("AUTHENTICATE PLAIN command trace suppressed");
	    suspendTracing();
	}

	try {
	    tag = writeCommand("AUTHENTICATE PLAIN", null);
	} catch (Exception ex) {
	    // Convert this into a BYE response
	    r = Response.byeResponse(ex);
	    done = true;
	}

	OutputStream os = getOutputStream(); // stream to IMAP server

	/* Wrap a BASE64Encoder around a ByteArrayOutputstream
	 * to craft b64 encoded username and password strings
	 *
	 * Note that the encoded bytes should be sent "as-is" to the
	 * server, *not* as literals or quoted-strings.
	 *
	 * Also note that unlike the B64 definition in MIME, CRLFs
	 * should *not* be inserted during the encoding process. So, I
	 * use Integer.MAX_VALUE (0x7fffffff (> 1G)) as the bytesPerLine,
	 * which should be sufficiently large !
	 *
	 * Finally, format the line in a buffer so it can be sent as
	 * a single packet, to avoid triggering a bug in SUN's SIMS 2.0
	 * server caused by patch 105346.
	 */

	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);

	while (!done) { // loop till we are done
	    try {
		r = readResponse();
		if (r.isContinuation()) {
		    // Server challenge ..
		    final String nullByte = "\0";
		    String s = (authzid == null ? "" : authzid) +
				    nullByte + u + nullByte + p;

		    // obtain b64 encoded bytes
		    b64os.write(s.getBytes(StandardCharsets.UTF_8));
		    b64os.flush(); 	// complete the encoding

		    bos.write(CRLF); 	// CRLF termination
		    os.write(bos.toByteArray()); // write out line
		    os.flush(); 	// flush the stream
		    bos.reset(); 	// reset buffer
		} else if (r.isTagged() && r.getTag().equals(tag))
		    // Ah, our tagged response
		    done = true;
		else if (r.isBYE()) // outta here
		    done = true;
		// hmm .. unsolicited response here ?!
	    } catch (Exception ioex) {
		// convert this into a BYE response
		r = Response.byeResponse(ioex);
		done = true;
	    }
	    v.add(r);
	}

	} finally {
	    resumeTracing();
	}

	Response[] responses = v.toArray(new Response[v.size()]);

	// handle an illegal but not uncommon untagged CAPABILTY response
	handleCapabilityResponse(responses);

	/*
	 * Dispatch untagged responses.
	 * NOTE: in our current upper level IMAP classes, we add the
	 * responseHandler to the Protocol object only *after* the
	 * connection has been authenticated. So, for now, the below
	 * code really ends up being just a no-op.
	 */
	notifyResponseHandlers(responses);

	// Handle the final OK, NO, BAD or BYE response
	if (noauthdebug && isTracing())
	    logger.fine("AUTHENTICATE PLAIN command result: " + r);
	handleLoginResult(r);
	// If the response includes a CAPABILITY response code, process it
	setCapabilities(r);
	// if we get this far without an exception, we're authenticated
	authenticated = true;
    }

    /**
     * The AUTHENTICATE command with AUTH=NTLM authentication scheme.
     * This is based heavly on the {@link #authlogin} method.
     *
     * @param  authzid		the authorization id
     * @param  u		the username
     * @param  p		the password
     * @throws ProtocolException as thrown by {@link Protocol#handleResult}.
     * @see "RFC3501, section 6.2.2"
     * @see "RFC2595, section 6"
     * @since  JavaMail 1.4.3
     */
    public synchronized void authntlm(String authzid, String u, String p)
				throws ProtocolException {
	List<Response> v = new ArrayList<>();
	String tag = null;
	Response r = null;
	boolean done = false;

	String type1Msg = null;
	int flags = PropUtil.getIntProperty(props,
	    "mail." + name + ".auth.ntlm.flags", 0);
	boolean v2 = PropUtil.getBooleanProperty(props,
	    "mail." + name + ".auth.ntlm.v2", true);
	String domain = props.getProperty(
	    "mail." + name + ".auth.ntlm.domain", "");
	Ntlm ntlm = new Ntlm(domain, getLocalHost(), u, p, logger);

	try {

	if (noauthdebug && isTracing()) {
	    logger.fine("AUTHENTICATE NTLM command trace suppressed");
	    suspendTracing();
	}

	try {
	    tag = writeCommand("AUTHENTICATE NTLM", null);
	} catch (Exception ex) {
	    // Convert this into a BYE response
	    r = Response.byeResponse(ex);
	    done = true;
	}

	OutputStream os = getOutputStream(); // stream to IMAP server
	boolean first = true;

	while (!done) { // loop till we are done
	    try {
		r = readResponse();
	    	if (r.isContinuation()) {
		    // Server challenge ..
		    String s;
		    if (first) {
			s = ntlm.generateType1Msg(flags, v2);
			first = false;
		    } else {
			s = ntlm.generateType3Msg(r.getRest());
		    }
 
		    os.write(s.getBytes(StandardCharsets.UTF_8));
		    os.write(CRLF); 	// CRLF termination
		    os.flush(); 	// flush the stream
		} else if (r.isTagged() && r.getTag().equals(tag))
		    // Ah, our tagged response
		    done = true;
		else if (r.isBYE()) // outta here
		    done = true;
		// hmm .. unsolicited response here ?!
	    } catch (Exception ioex) {
		// convert this into a BYE response
		r = Response.byeResponse(ioex);
		done = true;
	    }
	    v.add(r);
	}

	} finally {
	    resumeTracing();
	}

	Response[] responses = v.toArray(new Response[v.size()]);

	// handle an illegal but not uncommon untagged CAPABILTY response
	handleCapabilityResponse(responses);

	/*
	 * Dispatch untagged responses.
	 * NOTE: in our current upper level IMAP classes, we add the
	 * responseHandler to the Protocol object only *after* the
	 * connection has been authenticated. So, for now, the below
	 * code really ends up being just a no-op.
	 */
	notifyResponseHandlers(responses);

	// Handle the final OK, NO, BAD or BYE response
	if (noauthdebug && isTracing())
	    logger.fine("AUTHENTICATE NTLM command result: " + r);
	handleLoginResult(r);
	// If the response includes a CAPABILITY response code, process it
	setCapabilities(r);
	// if we get this far without an exception, we're authenticated
	authenticated = true;
    }

    /**
     * The AUTHENTICATE command with AUTH=XOAUTH2 authentication scheme.
     * This is based heavly on the {@link #authlogin} method.
     *
     * @param  u		the username
     * @param  p		the password
     * @throws ProtocolException as thrown by {@link Protocol#handleResult}.
     * @see "RFC3501, section 6.2.2"
     * @see "RFC2595, section 6"
     * @since  JavaMail 1.5.5
     */
    public synchronized void authoauth2(String u, String p)
				throws ProtocolException {
	List<Response> v = new ArrayList<>();
	String tag = null;
	Response r = null;
	boolean done = false;

	try {

	if (noauthdebug && isTracing()) {
	    logger.fine("AUTHENTICATE XOAUTH2 command trace suppressed");
	    suspendTracing();
	}

	try {
	    Argument args = new Argument();
	    args.writeAtom("XOAUTH2");
	    if (hasCapability("SASL-IR")) {
		String resp = "user=" + u + "\001auth=Bearer " + p + "\001\001";
		byte[] ba = BASE64EncoderStream.encode(
				    resp.getBytes(StandardCharsets.UTF_8));
		String irs = ASCIIUtility.toString(ba, 0, ba.length);
		args.writeAtom(irs);
	    }
	    tag = writeCommand("AUTHENTICATE", args);
	} catch (Exception ex) {
	    // Convert this into a BYE response
	    r = Response.byeResponse(ex);
	    done = true;
	}

	OutputStream os = getOutputStream(); // stream to IMAP server

	while (!done) { // loop till we are done
	    try {
		r = readResponse();
		if (r.isContinuation()) {
		    // Server challenge ..
		    String resp = "user=" + u + "\001auth=Bearer " +
				    p + "\001\001";
		    byte[] b = BASE64EncoderStream.encode(
				    resp.getBytes(StandardCharsets.UTF_8));
		    os.write(b);	// write out response
		    os.write(CRLF); 	// CRLF termination
		    os.flush(); 	// flush the stream
		} else if (r.isTagged() && r.getTag().equals(tag))
		    // Ah, our tagged response
		    done = true;
		else if (r.isBYE()) // outta here
		    done = true;
		// hmm .. unsolicited response here ?!
	    } catch (Exception ioex) {
		// convert this into a BYE response
		r = Response.byeResponse(ioex);
		done = true;
	    }
	    v.add(r);
	}

	} finally {
	    resumeTracing();
	}

	Response[] responses = v.toArray(new Response[v.size()]);

	// handle an illegal but not uncommon untagged CAPABILTY response
	handleCapabilityResponse(responses);

	/*
	 * Dispatch untagged responses.
	 * NOTE: in our current upper level IMAP classes, we add the
	 * responseHandler to the Protocol object only *after* the
	 * connection has been authenticated. So, for now, the below
	 * code really ends up being just a no-op.
	 */
	notifyResponseHandlers(responses);

	// Handle the final OK, NO, BAD or BYE response
	if (noauthdebug && isTracing())
	    logger.fine("AUTHENTICATE XOAUTH2 command result: " + r);
	handleLoginResult(r);
	// If the response includes a CAPABILITY response code, process it
	setCapabilities(r);
	// if we get this far without an exception, we're authenticated
	authenticated = true;
    }

    /**
     * SASL-based login.
     *
     * @param	allowed	the SASL mechanisms we're allowed to use
     * @param	realm	the SASL realm
     * @param	authzid	the authorization id
     * @param	u	the username
     * @param	p	the password
     * @exception	ProtocolException	for protocol failures
     */
    public void sasllogin(String[] allowed, String realm, String authzid,
				String u, String p) throws ProtocolException {
	boolean useCanonicalHostName = PropUtil.getBooleanProperty(props,
		    "mail." + name + ".sasl.usecanonicalhostname", false);
	String serviceHost;
	if (useCanonicalHostName)
	    serviceHost = getInetAddress().getCanonicalHostName();
	else
	    serviceHost = host;
	if (saslAuthenticator == null) {
	    try {
		Class<?> sac = Class.forName(
		    "com.sun.mail.imap.protocol.IMAPSaslAuthenticator");
		Constructor<?> c = sac.getConstructor(new Class<?>[] {
					IMAPProtocol.class,
					String.class,
					Properties.class,
					MailLogger.class,
					String.class
					});
		saslAuthenticator = (SaslAuthenticator)c.newInstance(
					new Object[] {
					this,
					name,
					props,
					logger,
					serviceHost
					});
	    } catch (Exception ex) {
		logger.log(Level.FINE, "Can't load SASL authenticator", ex);
		// probably because we're running on a system without SASL
		return;	// not authenticated, try without SASL
	    }
	}

	// were any allowed mechanisms specified?
	List<String> v;
	if (allowed != null && allowed.length > 0) {
	    // remove anything not supported by the server
	    v = new ArrayList<>(allowed.length);
	    for (int i = 0; i < allowed.length; i++)
		if (authmechs.contains(allowed[i]))	// XXX - case must match
		    v.add(allowed[i]);
	} else {
	    // everything is allowed
	    v = authmechs;
	}
	String[] mechs = v.toArray(new String[v.size()]);

	try {

	    if (noauthdebug && isTracing()) {
		logger.fine("SASL authentication command trace suppressed");
		suspendTracing();
	    }

	    if (saslAuthenticator.authenticate(mechs, realm, authzid, u, p)) {
		if (noauthdebug && isTracing())
		    logger.fine("SASL authentication succeeded");
		authenticated = true;
	    } else {
		if (noauthdebug && isTracing())
		    logger.fine("SASL authentication failed");
	    }
	} finally {
	    resumeTracing();
	}
    }

    // XXX - for IMAPSaslAuthenticator access to protected method
    OutputStream getIMAPOutputStream() {
	return getOutputStream();
    }

    /**
     * Handle the result response for a LOGIN or AUTHENTICATE command.
     * Look for IMAP login REFERRAL.
     *
     * @param	r	the response
     * @exception	ProtocolException	for protocol failures
     * @since	JavaMail 1.5.5
     */
    protected void handleLoginResult(Response r) throws ProtocolException {
	if (hasCapability("LOGIN-REFERRALS") &&
		(!r.isOK() || referralException))
	    checkReferral(r);
	handleResult(r);
    }

    /**
     * PROXYAUTH Command.
     * 
     * @param	u	the PROXYAUTH user name
     * @exception	ProtocolException	for protocol failures
     * @see "Netscape/iPlanet/SunONE Messaging Server extension"
     */
    public void proxyauth(String u) throws ProtocolException {
	Argument args = new Argument();
	args.writeString(u);

	simpleCommand("PROXYAUTH", args);
	proxyAuthUser = u;
    }

    /**
     * Get the user name used with the PROXYAUTH command.
     * Returns null if PROXYAUTH was not used.
     *
     * @return	the PROXYAUTH user name
     * @since	JavaMail 1.5.1
     */
    public String getProxyAuthUser() {
	return proxyAuthUser;
    }

    /**
     * UNAUTHENTICATE Command.
     * 
     * @exception	ProtocolException	for protocol failures
     * @see "Netscape/iPlanet/SunONE Messaging Server extension"
     * @since	JavaMail 1.5.1
     */
    public void unauthenticate() throws ProtocolException {
	if (!hasCapability("X-UNAUTHENTICATE"))
	    throw new BadCommandException("UNAUTHENTICATE not supported");
	simpleCommand("UNAUTHENTICATE", null);
	authenticated = false;
    }

    /**
     * ID Command, for Yahoo! Mail IMAP server.
     *
     * @param	guid	the GUID
     * @exception	ProtocolException	for protocol failures
     * @deprecated As of JavaMail 1.5.1, replaced by
     *		{@link #id(Map) id(Map&lt;String,String&gt;)}
     * @since JavaMail 1.4.4
     */
    @Deprecated
    public void id(String guid) throws ProtocolException {
	// support this for now, but remove it soon
	Map<String,String> gmap = new HashMap<>();
	gmap.put("GUID", guid);
	id(gmap);
    }

    /**
     * STARTTLS Command.
     * 
     * @exception	ProtocolException	for protocol failures
     * @see "RFC3501, section 6.2.1"
     */
    public void startTLS() throws ProtocolException {
	try {
	    super.startTLS("STARTTLS");
	} catch (ProtocolException pex) {
	    logger.log(Level.FINE, "STARTTLS ProtocolException", pex);
	    // ProtocolException just means the command wasn't recognized,
	    // or failed.  This should never happen if we check the
	    // CAPABILITY first.
	    throw pex;
	} catch (Exception ex) {
	    logger.log(Level.FINE, "STARTTLS Exception", ex);
	    // any other exception means we have to shut down the connection
	    // generate an artificial BYE response and disconnect
	    Response[] r = { Response.byeResponse(ex) };
	    notifyResponseHandlers(r);
	    disconnect();
	    throw new ProtocolException("STARTTLS failure", ex);
	}
    }

    /**
     * COMPRESS Command.  Only supports DEFLATE.
     * 
     * @exception	ProtocolException	for protocol failures
     * @see "RFC 4978"
     */
    public void compress() throws ProtocolException {
	try {
	    super.startCompression("COMPRESS DEFLATE");
	} catch (ProtocolException pex) {
	    logger.log(Level.FINE, "COMPRESS ProtocolException", pex);
	    // ProtocolException just means the command wasn't recognized,
	    // or failed.  This should never happen if we check the
	    // CAPABILITY first.
	    throw pex;
	} catch (Exception ex) {
	    logger.log(Level.FINE, "COMPRESS Exception", ex);
	    // any other exception means we have to shut down the connection
	    // generate an artificial BYE response and disconnect
	    Response[] r = { Response.byeResponse(ex) };
	    notifyResponseHandlers(r);
	    disconnect();
	    throw new ProtocolException("COMPRESS failure", ex);
	}
    }

    /**
     * Encode a mailbox name appropriately depending on whether or not
     * the server supports UTF-8, and add the encoded name to the
     * Argument.
     *
     * @param	args	the arguments
     * @param	name	the name to encode
     * @since	JavaMail 1.6.0
     */
    protected void writeMailboxName(Argument args, String name) {
	if (utf8)
	    args.writeString(name, StandardCharsets.UTF_8);
	else
	    // encode the mbox as per RFC2060
	    args.writeString(BASE64MailboxEncoder.encode(name));
    }

    /**
     * SELECT Command.
     *
     * @param	mbox	the mailbox name
     * @return		MailboxInfo if successful
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.1"
     */
    public MailboxInfo select(String mbox) throws ProtocolException {
	return select(mbox, null);
    }

    /**
     * SELECT Command with QRESYNC data.
     *
     * @param	mbox	the mailbox name
     * @param	rd	the ResyncData
     * @return		MailboxInfo if successful
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.1"
     * @see "RFC5162, section 3.1"
     * @since	JavaMail 1.5.1
     */
    public MailboxInfo select(String mbox, ResyncData rd)
				throws ProtocolException {
	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	if (rd != null) {
	    if (rd == ResyncData.CONDSTORE) {
		if (!hasCapability("CONDSTORE"))
		    throw new BadCommandException("CONDSTORE not supported");
		args.writeArgument(new Argument().writeAtom("CONDSTORE"));
	    } else {
		if (!hasCapability("QRESYNC")) 
		    throw new BadCommandException("QRESYNC not supported");
		args.writeArgument(resyncArgs(rd));
	    }
	}

	Response[] r = command("SELECT", args);

	// Note that MailboxInfo also removes those responses 
	// it knows about
	MailboxInfo minfo = new MailboxInfo(r);
	
	// dispatch any remaining untagged responses
	notifyResponseHandlers(r);

	Response response = r[r.length-1];

	if (response.isOK()) { // command succesful 
	    if (response.toString().indexOf("READ-ONLY") != -1)
		minfo.mode = Folder.READ_ONLY;
	    else
		minfo.mode = Folder.READ_WRITE;
	} 
	
	handleResult(response);
	return minfo;
    }

    /**
     * EXAMINE Command.
     *
     * @param	mbox	the mailbox name
     * @return		MailboxInfo if successful
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.2"
     */
    public MailboxInfo examine(String mbox) throws ProtocolException {
	return examine(mbox, null);
    }

    /**
     * EXAMINE Command with QRESYNC data.
     *
     * @param	mbox	the mailbox name
     * @param	rd	the ResyncData
     * @return		MailboxInfo if successful
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.2"
     * @see "RFC5162, section 3.1"
     * @since	JavaMail 1.5.1
     */
    public MailboxInfo examine(String mbox, ResyncData rd)
				throws ProtocolException {
	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	if (rd != null) {
	    if (rd == ResyncData.CONDSTORE) {
		if (!hasCapability("CONDSTORE"))
		    throw new BadCommandException("CONDSTORE not supported");
		args.writeArgument(new Argument().writeAtom("CONDSTORE"));
	    } else {
		if (!hasCapability("QRESYNC")) 
		    throw new BadCommandException("QRESYNC not supported");
		args.writeArgument(resyncArgs(rd));
	    }
	}

	Response[] r = command("EXAMINE", args);

	// Note that MailboxInfo also removes those responses
	// it knows about
	MailboxInfo minfo = new MailboxInfo(r);
	minfo.mode = Folder.READ_ONLY; // Obviously

	// dispatch any remaining untagged responses
	notifyResponseHandlers(r);

	handleResult(r[r.length-1]);
	return minfo;
    }

    /**
     * Generate a QRESYNC argument list based on the ResyncData.
     */
    private static Argument resyncArgs(ResyncData rd) {
	Argument cmd = new Argument();
	cmd.writeAtom("QRESYNC");
	Argument args = new Argument();
	args.writeNumber(rd.getUIDValidity());
	args.writeNumber(rd.getModSeq());
	UIDSet[] uids = Utility.getResyncUIDSet(rd);
	if (uids != null)
	    args.writeString(UIDSet.toString(uids));
	cmd.writeArgument(args);
	return cmd;
    }

    /**
     * ENABLE Command.
     *
     * @param	cap	the name of the capability to enable
     * @exception	ProtocolException	for protocol failures
     * @see "RFC 5161"
     * @since	JavaMail 1.5.1
     */
    public void enable(String cap) throws ProtocolException {
	if (!hasCapability("ENABLE")) 
	    throw new BadCommandException("ENABLE not supported");
	Argument args = new Argument();
	args.writeAtom(cap);
	simpleCommand("ENABLE", args);
	if (enabled == null)
	    enabled = new HashSet<>();
	enabled.add(cap.toUpperCase(Locale.ENGLISH));

	// update the utf8 flag
	utf8 = isEnabled("UTF8=ACCEPT");
    }

    /**
     * Is the capability/extension enabled?
     *
     * @param	cap	the capability name
     * @return		true if enabled
     * @see "RFC 5161"
     * @since	JavaMail 1.5.1
     */
    public boolean isEnabled(String cap) {
	if (enabled == null)
	    return false;
	else
	    return enabled.contains(cap.toUpperCase(Locale.ENGLISH));
    }

    /**
     * UNSELECT Command.
     *
     * @exception	ProtocolException	for protocol failures
     * @see "RFC 3691"
     * @since	JavaMail 1.4.4
     */
    public void unselect() throws ProtocolException {
	if (!hasCapability("UNSELECT")) 
	    throw new BadCommandException("UNSELECT not supported");
	simpleCommand("UNSELECT", null);
    }

    /**
     * STATUS Command.
     *
     * @param	mbox	the mailbox
     * @param	items	the STATUS items to request
     * @return		STATUS results
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.10"
     */
    public Status status(String mbox, String[] items) 
		throws ProtocolException {
	if (!isREV1() && !hasCapability("IMAP4SUNVERSION")) 
	    // STATUS is rev1 only, however the non-rev1 SIMS2.0 
	    // does support this.
	    throw new BadCommandException("STATUS not supported");

	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	Argument itemArgs = new Argument();
	if (items == null)
	    items = Status.standardItems;

	for (int i = 0, len = items.length; i < len; i++)
	    itemArgs.writeAtom(items[i]);
	args.writeArgument(itemArgs);

	Response[] r = command("STATUS", args);

	Status status = null;
	Response response = r[r.length-1];

	// Grab all STATUS responses
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("STATUS")) {
		    if (status == null)
			status = new Status(ir);
		    else // collect 'em all
			Status.add(status, new Status(ir));
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return status;
    }

    /**
     * CREATE Command.
     *
     * @param	mbox	the mailbox to create
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.3"
     */
    public void create(String mbox) throws ProtocolException {
	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	simpleCommand("CREATE", args);
    }

    /**
     * DELETE Command.
     *
     * @param	mbox	the mailbox to delete
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.4"
     */
    public void delete(String mbox) throws ProtocolException {
	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	simpleCommand("DELETE", args);
    }

    /**
     * RENAME Command.
     *
     * @param	o	old mailbox name
     * @param	n	new mailbox name
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.5"
     */
    public void rename(String o, String n) throws ProtocolException {
	Argument args = new Argument();	
	writeMailboxName(args, o);
	writeMailboxName(args, n);

	simpleCommand("RENAME", args);
    }

    /**
     * SUBSCRIBE Command.
     *
     * @param	mbox	the mailbox
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.6"
     */
    public void subscribe(String mbox) throws ProtocolException {
	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	simpleCommand("SUBSCRIBE", args);
    }

    /**
     * UNSUBSCRIBE Command.
     *
     * @param	mbox	the mailbox
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.7"
     */
    public void unsubscribe(String mbox) throws ProtocolException {
	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	simpleCommand("UNSUBSCRIBE", args);
    }

    /**
     * LIST Command.
     *
     * @param	ref	reference string
     * @param	pattern	pattern to list
     * @return		LIST results
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.8"
     */
    public ListInfo[] list(String ref, String pattern) 
			throws ProtocolException {
	return doList("LIST", ref, pattern);
    }

    /**
     * LSUB Command.
     *
     * @param	ref	reference string
     * @param	pattern	pattern to list
     * @return		LSUB results
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.9"
     */
    public ListInfo[] lsub(String ref, String pattern) 
			throws ProtocolException {
	return doList("LSUB", ref, pattern);
    }

    /**
     * Execute the specified LIST-like command (e.g., "LIST" or "LSUB"),
     * using the reference and pattern.
     *
     * @param	cmd	the list command
     * @param	ref	the reference string
     * @param	pat	the pattern
     * @return		array of ListInfo results
     * @exception	ProtocolException	for protocol failures
     * @since JavaMail 1.4.6
     */
    protected ListInfo[] doList(String cmd, String ref, String pat)
			throws ProtocolException {
	Argument args = new Argument();	
	writeMailboxName(args, ref);
	writeMailboxName(args, pat);

	Response[] r = command(cmd, args);

	ListInfo[] linfo = null;
	Response response = r[r.length-1];

	if (response.isOK()) { // command succesful 
	    List<ListInfo> v = new ArrayList<>(1);
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals(cmd)) {
		    v.add(new ListInfo(ir));
		    r[i] = null;
		}
	    }
	    if (v.size() > 0) {
		linfo = v.toArray(new ListInfo[v.size()]);
	    }
	}
	
	// Dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return linfo;
    }
		
    /**
     * APPEND Command.
     *
     * @param	mbox	the mailbox
     * @param	f	the message Flags
     * @param	d	the message date
     * @param	data	the message data
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.11"
     */
    public void append(String mbox, Flags f, Date d,
			Literal data) throws ProtocolException {
	appenduid(mbox, f, d, data, false);	// ignore return value
    }

    /**
     * APPEND Command, return uid from APPENDUID response code.
     *
     * @param	mbox	the mailbox
     * @param	f	the message Flags
     * @param	d	the message date
     * @param	data	the message data
     * @return		APPENDUID data
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.3.11"
     */
    public AppendUID appenduid(String mbox, Flags f, Date d,
			Literal data) throws ProtocolException {
	return appenduid(mbox, f, d, data, true);
    }

    public AppendUID appenduid(String mbox, Flags f, Date d,
			Literal data, boolean uid) throws ProtocolException {
	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	if (f != null) { // set Flags in appended message
	    // can't set the \Recent flag in APPEND
	    if (f.contains(Flags.Flag.RECENT)) {
		f = new Flags(f);		// copy, don't modify orig
		f.remove(Flags.Flag.RECENT);	// remove RECENT from copy
	    }

	    /*
	     * HACK ALERT: We want the flag_list to be written out
	     * without any checking/processing of the bytes in it. If
	     * I use writeString(), the flag_list will end up being
	     * quoted since it contains "illegal" characters. So I
	     * am depending on implementation knowledge that writeAtom()
	     * does not do any checking/processing - it just writes out
	     * the bytes. What we really need is a writeFoo() that just
	     * dumps out its argument.
	     */
	    args.writeAtom(createFlagList(f));
	}
	if (d != null) // set INTERNALDATE in appended message
	    args.writeString(INTERNALDATE.format(d));

	args.writeBytes(data);

	Response[] r = command("APPEND", args);

	// dispatch untagged responses
	notifyResponseHandlers(r);

	// Handle result of this command
	handleResult(r[r.length-1]);

	if (uid)
	    return getAppendUID(r[r.length-1]);
	else
	    return null;
    }

    /**
     * If the response contains an APPENDUID response code, extract
     * it and return an AppendUID object with the information.
     */
    private AppendUID getAppendUID(Response r) {
	if (!r.isOK())
	    return null;
	byte b;
	while ((b = r.readByte()) > 0 && b != (byte)'[')
	    ;
	if (b == 0)
	    return null;
	String s;
	s = r.readAtom();
	if (!s.equalsIgnoreCase("APPENDUID"))
	    return null;

	long uidvalidity = r.readLong();
	long uid = r.readLong();
	return new AppendUID(uidvalidity, uid);
    }

    /**
     * CHECK Command.
     *
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.4.1"
     */
    public void check() throws ProtocolException {
	simpleCommand("CHECK", null);
    }

    /**
     * CLOSE Command.
     *
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.4.2"
     */
    public void close() throws ProtocolException {
	simpleCommand("CLOSE", null);
    }

    /**
     * EXPUNGE Command.
     *
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2060, section 6.4.3"
     */
    public void expunge() throws ProtocolException {
	simpleCommand("EXPUNGE", null);
    }

    /**
     * UID EXPUNGE Command.
     *
     * @param	set	UIDs to expunge
     * @exception	ProtocolException	for protocol failures
     * @see "RFC4315, section 2"
     */
    public void uidexpunge(UIDSet[] set) throws ProtocolException {
	if (!hasCapability("UIDPLUS")) 
	    throw new BadCommandException("UID EXPUNGE not supported");
	simpleCommand("UID EXPUNGE " + UIDSet.toString(set), null);
    }

    /**
     * Fetch the BODYSTRUCTURE of the specified message.
     *
     * @param	msgno	the message number
     * @return		the BODYSTRUCTURE item
     * @exception	ProtocolException	for protocol failures
     */
    public BODYSTRUCTURE fetchBodyStructure(int msgno) 
			throws ProtocolException {
	Response[] r = fetch(msgno, "BODYSTRUCTURE");
	notifyResponseHandlers(r);

	Response response = r[r.length-1];
	if (response.isOK())
	    return FetchResponse.getItem(r, msgno, BODYSTRUCTURE.class);
	else if (response.isNO())
	    return null;
	else {
	    handleResult(response);
	    return null;
	}
    }

    /**
     * Fetch given BODY section, without marking the message
     * as SEEN.
     *
     * @param	msgno	the message number
     * @param	section	the body section
     * @return		the BODY item
     * @exception	ProtocolException	for protocol failures
     */
    public BODY peekBody(int msgno, String section)
			throws ProtocolException {
	return fetchBody(msgno, section, true);
    }

    /**
     * Fetch given BODY section.
     *
     * @param	msgno	the message number
     * @param	section	the body section
     * @return		the BODY item
     * @exception	ProtocolException	for protocol failures
     */
    public BODY fetchBody(int msgno, String section)
			throws ProtocolException {
	return fetchBody(msgno, section, false);
    }

    protected BODY fetchBody(int msgno, String section, boolean peek)
			throws ProtocolException {
	Response[] r;

	if (section == null)
	    section = "";
	String body = (peek ? "BODY.PEEK[" : "BODY[") + section + "]";
	return fetchSectionBody(msgno, section, body);
    }

    /**
     * Partial FETCH of given BODY section, without setting SEEN flag.
     *
     * @param	msgno	the message number
     * @param	section	the body section
     * @param	start	starting byte count
     * @param	size	number of bytes to fetch
     * @return		the BODY item
     * @exception	ProtocolException	for protocol failures
     */
    public BODY peekBody(int msgno, String section, int start, int size)
			throws ProtocolException {
	return fetchBody(msgno, section, start, size, true, null);
    }

    /**
     * Partial FETCH of given BODY section.
     *
     * @param	msgno	the message number
     * @param	section	the body section
     * @param	start	starting byte count
     * @param	size	number of bytes to fetch
     * @return		the BODY item
     * @exception	ProtocolException	for protocol failures
     */
    public BODY fetchBody(int msgno, String section, int start, int size)
			throws ProtocolException {
	return fetchBody(msgno, section, start, size, false, null);
    }

    /**
     * Partial FETCH of given BODY section, without setting SEEN flag.
     *
     * @param	msgno	the message number
     * @param	section	the body section
     * @param	start	starting byte count
     * @param	size	number of bytes to fetch
     * @param	ba	the buffer into which to read the response
     * @return		the BODY item
     * @exception	ProtocolException	for protocol failures
     */
    public BODY peekBody(int msgno, String section, int start, int size,
				ByteArray ba) throws ProtocolException {
	return fetchBody(msgno, section, start, size, true, ba);
    }

    /**
     * Partial FETCH of given BODY section.
     *
     * @param	msgno	the message number
     * @param	section	the body section
     * @param	start	starting byte count
     * @param	size	number of bytes to fetch
     * @param	ba	the buffer into which to read the response
     * @return		the BODY item
     * @exception	ProtocolException	for protocol failures
     */
    public BODY fetchBody(int msgno, String section, int start, int size,
				ByteArray ba) throws ProtocolException {
	return fetchBody(msgno, section, start, size, false, ba);
    }

    protected BODY fetchBody(int msgno, String section, int start, int size,
			boolean peek, ByteArray ba) throws ProtocolException {
	this.ba = ba;	// save for later use by getResponseBuffer
	if (section == null)
	    section = "";
	String body = (peek ? "BODY.PEEK[" : "BODY[") + section + "]<" +
			String.valueOf(start) + "." +
			String.valueOf(size) + ">";
	return fetchSectionBody(msgno, section, body);
    }

    /**
     * Fetch the given body section of the given message, using the
     * body string "body".
     *
     * @param	msgno	the message number
     * @param	section	the body section
     * @param	body	the body string
     * @return		the BODY item
     * @exception	ProtocolException	for protocol failures
     */
    protected BODY fetchSectionBody(int msgno, String section, String body)
			throws ProtocolException {
	Response[] r;

	r = fetch(msgno, body);
	notifyResponseHandlers(r);

	Response response = r[r.length-1];
	if (response.isOK()) {
	    List<BODY> bl = FetchResponse.getItems(r, msgno, BODY.class);
	    if (bl.size() == 1)
		return bl.get(0);	// the common case
	    if (logger.isLoggable(Level.FINEST))
		logger.finest("got " + bl.size() +
				" BODY responses for section " + section);
	    // more then one BODY response, have to find the right one
	    for (BODY br : bl) {
		if (logger.isLoggable(Level.FINEST))
		    logger.finest("got BODY section " + br.getSection());
		if (br.getSection().equalsIgnoreCase(section))
		    return br;	// that's the one!
	    }
	    return null;	// couldn't find it
	} else if (response.isNO())
	    return null;
	else {
	    handleResult(response);
	    return null;
	}
    }

    /**
     * Return a buffer to read a response into.
     * The buffer is provided by fetchBody and is
     * used only once.
     *
     * @return	the buffer to use
     */
    @Override
    protected ByteArray getResponseBuffer() {
	ByteArray ret = ba;
	ba = null;
	return ret;
    }

    /**
     * Fetch the specified RFC822 Data item. 'what' names
     * the item to be fetched. 'what' can be <code>null</code>
     * to fetch the whole message.
     *
     * @param	msgno	the message number
     * @param	what	the item to fetch
     * @return		the RFC822DATA item
     * @exception	ProtocolException	for protocol failures
     */
    public RFC822DATA fetchRFC822(int msgno, String what)
			throws ProtocolException {
	Response[] r = fetch(msgno,
			     what == null ? "RFC822" : "RFC822." + what
			    );

	// dispatch untagged responses
	notifyResponseHandlers(r);

	Response response = r[r.length-1]; 
	if (response.isOK())
	    return FetchResponse.getItem(r, msgno, RFC822DATA.class);
	else if (response.isNO())
	    return null;
	else {
	    handleResult(response);
	    return null;
	}
    }

    /**
     * Fetch the FLAGS for the given message.
     *
     * @param	msgno	the message number
     * @return		the Flags
     * @exception	ProtocolException	for protocol failures
     */
    public Flags fetchFlags(int msgno) throws ProtocolException {
	Flags flags = null;
	Response[] r = fetch(msgno, "FLAGS");

	// Search for our FLAGS response
	for (int i = 0, len = r.length; i < len; i++) {
	    if (r[i] == null ||
		!(r[i] instanceof FetchResponse) ||
		((FetchResponse)r[i]).getNumber() != msgno)
		continue;		
	    
	    FetchResponse fr = (FetchResponse)r[i];
	    if ((flags = fr.getItem(FLAGS.class)) != null) {
		r[i] = null; // remove this response
		break;
	    }
	}

	// dispatch untagged responses
	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);
	return flags;
    }

    /**
     * Fetch the IMAP UID for the given message.
     *
     * @param	msgno	the message number
     * @return		the UID
     * @exception	ProtocolException	for protocol failures
     */
    public UID fetchUID(int msgno) throws ProtocolException {
	Response[] r = fetch(msgno, "UID");

	// dispatch untagged responses
	notifyResponseHandlers(r);

	Response response = r[r.length-1]; 
	if (response.isOK())
	    return FetchResponse.getItem(r, msgno, UID.class);
	else if (response.isNO()) // XXX: Issue NOOP ?
	    return null;
	else {
	    handleResult(response);
	    return null; // NOTREACHED
	}
    }

    /**
     * Fetch the IMAP MODSEQ for the given message.
     *
     * @param	msgno	the message number
     * @return		the MODSEQ
     * @exception	ProtocolException	for protocol failures
     * @since	JavaMail 1.5.1
     */
    public MODSEQ fetchMODSEQ(int msgno) throws ProtocolException {
	Response[] r = fetch(msgno, "MODSEQ");

	// dispatch untagged responses
	notifyResponseHandlers(r);

	Response response = r[r.length-1]; 
	if (response.isOK())
	    return FetchResponse.getItem(r, msgno, MODSEQ.class);
	else if (response.isNO()) // XXX: Issue NOOP ?
	    return null;
	else {
	    handleResult(response);
	    return null; // NOTREACHED
	}
    }
		
    /**
     * Get the sequence number for the given UID.  Nothing is returned;
     * the FETCH UID response must be handled by the reponse handler,
     * along with any possible EXPUNGE responses, to ensure that the
     * UID is matched with the correct sequence number.
     *
     * @param	uid	the UID
     * @exception	ProtocolException	for protocol failures
     * @since	JavaMail 1.5.3
     */
    public void fetchSequenceNumber(long uid) throws ProtocolException {
	Response[] r = fetch(String.valueOf(uid), "UID", true);	

	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);
    }

    /**
     * Get the sequence numbers for UIDs ranging from start till end.
     * Since the range may be large and sparse, an array of the UIDs actually
     * found is returned.  The caller must map these to messages after
     * the FETCH UID responses have been handled by the reponse handler,
     * along with any possible EXPUNGE responses, to ensure that the
     * UIDs are matched with the correct sequence numbers.
     *
     * @param	start	first UID
     * @param	end	last UID
     * @return		array of sequence numbers
     * @exception	ProtocolException	for protocol failures
     * @since	JavaMail 1.5.3
     */
    public long[] fetchSequenceNumbers(long start, long end)
			throws ProtocolException {
	Response[] r = fetch(String.valueOf(start) + ":" + 
				(end == UIDFolder.LASTUID ? "*" : 
				String.valueOf(end)),
			     "UID", true);	

	UID u;
	List<UID> v = new ArrayList<>();
	for (int i = 0, len = r.length; i < len; i++) {
	    if (r[i] == null || !(r[i] instanceof FetchResponse))
		continue;
	    
	    FetchResponse fr = (FetchResponse)r[i];
	    if ((u = fr.getItem(UID.class)) != null)
		v.add(u);
	}
		
	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);

	long[] lv = new long[v.size()];
	for (int i = 0; i < v.size(); i++)
	    lv[i] = v.get(i).uid;
	return lv;
    }
 
    /**
     * Get the sequence numbers for UIDs specified in the array.
     * Nothing is returned.  The caller must map the UIDs to messages after
     * the FETCH UID responses have been handled by the reponse handler,
     * along with any possible EXPUNGE responses, to ensure that the
     * UIDs are matched with the correct sequence numbers.
     *
     * @param	uids	the UIDs
     * @exception	ProtocolException	for protocol failures
     * @since	JavaMail 1.5.3
     */
    public void fetchSequenceNumbers(long[] uids) throws ProtocolException {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < uids.length; i++) {
	    if (i > 0)
		sb.append(",");
	    sb.append(String.valueOf(uids[i]));
	}

	Response[] r = fetch(sb.toString(), "UID", true);	

	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);
    }

    /**
     * Get the sequence numbers for messages changed since the given
     * modseq and with UIDs ranging from start till end.
     * Also, prefetch the flags for the returned messages.
     *
     * @param	start	first UID
     * @param	end	last UID
     * @param	modseq	the MODSEQ
     * @return		array of sequence numbers
     * @exception	ProtocolException	for protocol failures
     * @see	"RFC 4551"
     * @since	JavaMail 1.5.1
     */
    public int[] uidfetchChangedSince(long start, long end, long modseq)
			throws ProtocolException {
	String msgSequence = String.valueOf(start) + ":" + 
				(end == UIDFolder.LASTUID ? "*" : 
				String.valueOf(end));
	Response[] r = command("UID FETCH " + msgSequence +
		" (FLAGS) (CHANGEDSINCE " + String.valueOf(modseq) + ")", null);

	List<Integer> v = new ArrayList<>();
	for (int i = 0, len = r.length; i < len; i++) {
	    if (r[i] == null || !(r[i] instanceof FetchResponse))
		continue;
 
	    FetchResponse fr = (FetchResponse)r[i];
	    v.add(Integer.valueOf(fr.getNumber()));
	}
		
	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);

	// Copy the list into 'matches'
	int vsize = v.size();
	int[] matches = new int[vsize];
	for (int i = 0; i < vsize; i++)
	    matches[i] = v.get(i).intValue();
	return matches;
    }

    public Response[] fetch(MessageSet[] msgsets, String what)
			throws ProtocolException {
	return fetch(MessageSet.toString(msgsets), what, false);
    }

    public Response[] fetch(int start, int end, String what)
			throws ProtocolException {
	return fetch(String.valueOf(start) + ":" + String.valueOf(end), 
		     what, false);
    }

    public Response[] fetch(int msg, String what) 
			throws ProtocolException {
	return fetch(String.valueOf(msg), what, false);
    }

    private Response[] fetch(String msgSequence, String what, boolean uid)
			throws ProtocolException {
	if (uid)
	    return command("UID FETCH " + msgSequence +" (" + what + ")",null);
	else
	    return command("FETCH " + msgSequence + " (" + what + ")", null);
    }

    /**
     * COPY command.
     *
     * @param	msgsets	the messages to copy
     * @param	mbox	the mailbox to copy them to
     * @exception	ProtocolException	for protocol failures
     */
    public void copy(MessageSet[] msgsets, String mbox)
			throws ProtocolException {
	copyuid(MessageSet.toString(msgsets), mbox, false);
    }

    /**
     * COPY command.
     *
     * @param	start	start message number
     * @param	end	end message number
     * @param	mbox	the mailbox to copy them to
     * @exception	ProtocolException	for protocol failures
     */
    public void copy(int start, int end, String mbox)
			throws ProtocolException {
	copyuid(String.valueOf(start) + ":" + String.valueOf(end),
		    mbox, false);
    }

    /**
     * COPY command, return uid from COPYUID response code.
     *
     * @param	msgsets	the messages to copy
     * @param	mbox	the mailbox to copy them to
     * @return		COPYUID response data
     * @exception	ProtocolException	for protocol failures
     * @see "RFC 4315, section 3"
     */
    public CopyUID copyuid(MessageSet[] msgsets, String mbox)
			throws ProtocolException {
	return copyuid(MessageSet.toString(msgsets), mbox, true);
    }

    /**
     * COPY command, return uid from COPYUID response code.
     *
     * @param	start	start message number
     * @param	end	end message number
     * @param	mbox	the mailbox to copy them to
     * @return		COPYUID response data
     * @exception	ProtocolException	for protocol failures
     * @see "RFC 4315, section 3"
     */
    public CopyUID copyuid(int start, int end, String mbox)
			throws ProtocolException {
	return copyuid(String.valueOf(start) + ":" + String.valueOf(end),
		    mbox, true);
    }

    private CopyUID copyuid(String msgSequence, String mbox, boolean uid)
				throws ProtocolException {
	if (uid && !hasCapability("UIDPLUS")) 
	    throw new BadCommandException("UIDPLUS not supported");

	Argument args = new Argument();	
	args.writeAtom(msgSequence);
	writeMailboxName(args, mbox);

	Response[] r = command("COPY", args);

	// dispatch untagged responses
	notifyResponseHandlers(r);

	// Handle result of this command
	handleResult(r[r.length-1]);

	if (uid)
	    return getCopyUID(r);
	else
	    return null;
    }

    /**
     * MOVE command.
     *
     * @param	msgsets	the messages to move
     * @param	mbox	the mailbox to move them to
     * @exception	ProtocolException	for protocol failures
     * @see "RFC 6851"
     * @since	JavaMail 1.5.4
     */
    public void move(MessageSet[] msgsets, String mbox)
			throws ProtocolException {
	moveuid(MessageSet.toString(msgsets), mbox, false);
    }

    /**
     * MOVE command.
     *
     * @param	start	start message number
     * @param	end	end message number
     * @param	mbox	the mailbox to move them to
     * @exception	ProtocolException	for protocol failures
     * @see "RFC 6851"
     * @since	JavaMail 1.5.4
     */
    public void move(int start, int end, String mbox)
			throws ProtocolException {
	moveuid(String.valueOf(start) + ":" + String.valueOf(end),
		    mbox, false);
    }

    /**
     * MOVE Command, return uid from COPYUID response code.
     *
     * @param	msgsets	the messages to move
     * @param	mbox	the mailbox to move them to
     * @return		COPYUID response data
     * @exception	ProtocolException	for protocol failures
     * @see "RFC 6851"
     * @see "RFC 4315, section 3"
     * @since	JavaMail 1.5.4
     */
    public CopyUID moveuid(MessageSet[] msgsets, String mbox)
			throws ProtocolException {
	return moveuid(MessageSet.toString(msgsets), mbox, true);
    }

    /**
     * MOVE Command, return uid from COPYUID response code.
     *
     * @param	start	start message number
     * @param	end	end message number
     * @param	mbox	the mailbox to move them to
     * @return		COPYUID response data
     * @exception	ProtocolException	for protocol failures
     * @see "RFC 6851"
     * @see "RFC 4315, section 3"
     * @since	JavaMail 1.5.4
     */
    public CopyUID moveuid(int start, int end, String mbox)
			throws ProtocolException {
	return moveuid(String.valueOf(start) + ":" + String.valueOf(end),
		    mbox, true);
    }

    /**
     * MOVE Command, return uid from COPYUID response code.
     *
     * @see "RFC 6851"
     * @see "RFC 4315, section 3"
     * @since	JavaMail 1.5.4
     */
    private CopyUID moveuid(String msgSequence, String mbox, boolean uid)
				throws ProtocolException {
	if (!hasCapability("MOVE")) 
	    throw new BadCommandException("MOVE not supported");
	if (uid && !hasCapability("UIDPLUS")) 
	    throw new BadCommandException("UIDPLUS not supported");

	Argument args = new Argument();	
	args.writeAtom(msgSequence);
	writeMailboxName(args, mbox);

	Response[] r = command("MOVE", args);

	// dispatch untagged responses
	notifyResponseHandlers(r);

	// Handle result of this command
	handleResult(r[r.length-1]);

	if (uid)
	    return getCopyUID(r);
	else
	    return null;
    }

    /**
     * If the response contains a COPYUID response code, extract
     * it and return a CopyUID object with the information.
     *
     * @param	rr	the responses to examine
     * @return		the COPYUID response code data, or null if not found
     * @since	JavaMail 1.5.4
     */
    protected CopyUID getCopyUID(Response[] rr) {
	// most likely in the last response, so start there and work backward
	for (int i = rr.length - 1; i >= 0; i--) {
	    Response r = rr[i];
	    if (r == null || !r.isOK())
		continue;
	    byte b;
	    while ((b = r.readByte()) > 0 && b != (byte)'[')
		;
	    if (b == 0)
		continue;
	    String s;
	    s = r.readAtom();
	    if (!s.equalsIgnoreCase("COPYUID"))
		continue;

	    // XXX - need to merge more than one response for MOVE?
	    long uidvalidity = r.readLong();
	    String src = r.readAtom();
	    String dst = r.readAtom();
	    return new CopyUID(uidvalidity,
			    UIDSet.parseUIDSets(src), UIDSet.parseUIDSets(dst));
	}
	return null;
    }

    public void storeFlags(MessageSet[] msgsets, Flags flags, boolean set)
			throws ProtocolException {
	storeFlags(MessageSet.toString(msgsets), flags, set);
    }

    public void storeFlags(int start, int end, Flags flags, boolean set)
			throws ProtocolException {
	storeFlags(String.valueOf(start) + ":" + String.valueOf(end),
		   flags, set);
    }

    /**
     * Set the specified flags on this message.
     *
     * @param	msg	the message number
     * @param	flags	the flags
     * @param	set	true to set, false to clear
     * @exception	ProtocolException	for protocol failures
     */
    public void storeFlags(int msg, Flags flags, boolean set)
			throws ProtocolException { 
	storeFlags(String.valueOf(msg), flags, set);
    }

    private void storeFlags(String msgset, Flags flags, boolean set)
			throws ProtocolException {
	Response[] r;
	if (set)
	    r = command("STORE " + msgset + " +FLAGS " + 
			 createFlagList(flags), null);
	else
	    r = command("STORE " + msgset + " -FLAGS " + 
			createFlagList(flags), null);
	
	// Dispatch untagged responses
	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);
    }

    /**
     * Creates an IMAP flag_list from the given Flags object.
     *
     * @param	flags	the flags
     * @return		the IMAP flag_list
     * @since	JavaMail 1.5.4
     */
    protected String createFlagList(Flags flags) {
	StringBuilder sb = new StringBuilder("("); // start of flag_list

	Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags
	boolean first = true;
	for (int i = 0; i < sf.length; i++) {
	    String s;
	    Flags.Flag f = sf[i];
	    if (f == Flags.Flag.ANSWERED)
		s = "\\Answered";
	    else if (f == Flags.Flag.DELETED)
		s = "\\Deleted";
	    else if (f == Flags.Flag.DRAFT)
		s = "\\Draft";
	    else if (f == Flags.Flag.FLAGGED)
		s = "\\Flagged";
	    else if (f == Flags.Flag.RECENT)
		s = "\\Recent";
	    else if (f == Flags.Flag.SEEN)
		s = "\\Seen";
	    else
		continue;	// skip it
	    if (first)
		first = false;
	    else
		sb.append(' ');
	    sb.append(s);
	}

	String[] uf = flags.getUserFlags(); // get the user flag strings
	for (int i = 0; i < uf.length; i++) {
	    if (first)
		first = false;
	    else
		sb.append(' ');
	    sb.append(uf[i]);
	}

	sb.append(")"); // terminate flag_list
	return sb.toString();
    }

    /**
     * Issue the given search criterion on the specified message sets.
     * Returns array of matching sequence numbers. An empty array
     * is returned if no matches are found.
     *
     * @param	msgsets	array of MessageSets
     * @param	term	SearchTerm
     * @return		array of matching sequence numbers.
     * @exception	ProtocolException	for protocol failures
     * @exception	SearchException	for search failures
     */
    public int[] search(MessageSet[] msgsets, SearchTerm term)
			throws ProtocolException, SearchException {
	return search(MessageSet.toString(msgsets), term);
    }

    /**
     * Issue the given search criterion on all messages in this folder.
     * Returns array of matching sequence numbers. An empty array
     * is returned if no matches are found.
     *
     * @param	term	SearchTerm
     * @return		array of matching sequence numbers.
     * @exception	ProtocolException	for protocol failures
     * @exception	SearchException	for search failures
     */
    public int[] search(SearchTerm term) 
			throws ProtocolException, SearchException {
	return search("ALL", term);
    }

    /*
     * Apply the given SearchTerm on the specified sequence.
     * Returns array of matching sequence numbers. Note that an empty
     * array is returned for no matches.
     */
    private int[] search(String msgSequence, SearchTerm term)
			throws ProtocolException, SearchException {
	// Check if the search "text" terms contain only ASCII chars,
	// or if utf8 support has been enabled (in which case CHARSET
	// is not allowed; see RFC 6855, section 3, last paragraph)
	if (supportsUtf8() || SearchSequence.isAscii(term)) {
	    try {
		return issueSearch(msgSequence, term, null);
	    } catch (IOException ioex) { /* will not happen */ }
	}

	/*
	 * The search "text" terms do contain non-ASCII chars and utf8
	 * support has not been enabled.  We need to use:
	 * "SEARCH CHARSET <charset> ..."
	 * The charsets we try to use are UTF-8 and the locale's
	 * default charset. If the server supports UTF-8, great, 
	 * always use it. Else we try to use the default charset.
	 */

	// Cycle thru the list of charsets
	for (int i = 0; i < searchCharsets.length; i++) {
	    if (searchCharsets[i] == null)
		continue;

	    try {
		return issueSearch(msgSequence, term, searchCharsets[i]);
	    } catch (CommandFailedException cfx) {
		/*
		 * Server returned NO. For now, I'll just assume that 
		 * this indicates that this charset is unsupported.
		 * We can check the BADCHARSET response code once
		 * that's spec'd into the IMAP RFC ..
		 */
		searchCharsets[i] = null;
		continue;
	    } catch (IOException ioex) {
		/* Charset conversion failed. Try the next one */
		continue;
	    } catch (ProtocolException pex) {
		throw pex;
	    } catch (SearchException sex) {
		throw sex;
	    }
	}

	// No luck.
	throw new SearchException("Search failed");
    }

    /* Apply the given SearchTerm on the specified sequence, using the
     * given charset. <p>
     * Returns array of matching sequence numbers. Note that an empty
     * array is returned for no matches.
     */
    private int[] issueSearch(String msgSequence, SearchTerm term,
      			      String charset) 
	     throws ProtocolException, SearchException, IOException {

	// Generate a search-sequence with the given charset
	Argument args = getSearchSequence().generateSequence(term, 
			  charset == null ? null : 
					    MimeUtility.javaCharset(charset)
			);
	args.writeAtom(msgSequence);

	Response[] r;

	if (charset == null) // text is all US-ASCII
	    r = command("SEARCH", args);
	else
	    r = command("SEARCH CHARSET " + charset, args);

	Response response = r[r.length-1];
	int[] matches = null;

	// Grab all SEARCH responses
	if (response.isOK()) { // command succesful
	    List<Integer> v = new ArrayList<>();
	    int num;
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		// There *will* be one SEARCH response.
		if (ir.keyEquals("SEARCH")) {
		    while ((num = ir.readNumber()) != -1)
			v.add(Integer.valueOf(num));
		    r[i] = null;
		}
	    }

	    // Copy the list into 'matches'
	    int vsize = v.size();
	    matches = new int[vsize];
	    for (int i = 0; i < vsize; i++)
		matches[i] = v.get(i).intValue();
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return matches;
    }

    /**
     * Get the SearchSequence object.
     * The SearchSequence object instance is saved in the searchSequence
     * field.  Subclasses of IMAPProtocol may override this method to
     * return a subclass of SearchSequence, in order to add support for
     * product-specific search terms.
     *
     * @return	the SearchSequence
     * @since JavaMail 1.4.6
     */
    protected SearchSequence getSearchSequence() {
	if (searchSequence == null)
	    searchSequence = new SearchSequence(this);
	return searchSequence;
    }

    /**
     * Sort messages in the folder according to the specified sort criteria.
     * If the search term is not null, limit the sort to only the messages
     * that match the search term.
     * Returns an array of sorted sequence numbers. An empty array
     * is returned if no matches are found.
     *
     * @param	term	sort criteria
     * @param	sterm	SearchTerm
     * @return		array of matching sequence numbers.
     * @exception	ProtocolException	for protocol failures
     * @exception	SearchException	for search failures
     *
     * @see	"RFC 5256"
     * @since	JavaMail 1.4.4
     */
    public int[] sort(SortTerm[] term, SearchTerm sterm)
			throws ProtocolException, SearchException {
	if (!hasCapability("SORT*")) 
	    throw new BadCommandException("SORT not supported");

	if (term == null || term.length == 0)
	    throw new BadCommandException("Must have at least one sort term");

	Argument args = new Argument();
	Argument sargs = new Argument();
	for (int i = 0; i < term.length; i++)
	    sargs.writeAtom(term[i].toString());
	args.writeArgument(sargs);	// sort criteria

	args.writeAtom("UTF-8");	// charset specification
	if (sterm != null) {
	    try {
		args.append(
		    getSearchSequence().generateSequence(sterm, "UTF-8"));
	    } catch (IOException ioex) {
		// should never happen
		throw new SearchException(ioex.toString());
	    }
	} else
	    args.writeAtom("ALL");

	Response[] r = command("SORT", args);
	Response response = r[r.length-1];
	int[] matches = null;

	// Grab all SORT responses
	if (response.isOK()) { // command succesful
	    List<Integer> v = new ArrayList<>();
	    int num;
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("SORT")) {
		    while ((num = ir.readNumber()) != -1)
			v.add(Integer.valueOf(num));
		    r[i] = null;
		}
	    }

	    // Copy the list into 'matches'
	    int vsize = v.size();
	    matches = new int[vsize];
	    for (int i = 0; i < vsize; i++)
		matches[i] = v.get(i).intValue();
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return matches;
    }

    /**
     * NAMESPACE Command.
     *
     * @return	the namespaces
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2342"
     */
    public Namespaces namespace() throws ProtocolException {
	if (!hasCapability("NAMESPACE")) 
	    throw new BadCommandException("NAMESPACE not supported");

	Response[] r = command("NAMESPACE", null);

	Namespaces namespace = null;
	Response response = r[r.length-1];

	// Grab NAMESPACE response
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("NAMESPACE")) {
		    if (namespace == null)
			namespace = new Namespaces(ir);
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return namespace;
    }

    /**
     * GETQUOTAROOT Command.
     *
     * Returns an array of Quota objects, representing the quotas
     * for this mailbox and, indirectly, the quotaroots for this
     * mailbox.
     *
     * @param	mbox	the mailbox
     * @return		array of Quota objects
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2087"
     */
    public Quota[] getQuotaRoot(String mbox) throws ProtocolException {
	if (!hasCapability("QUOTA")) 
	    throw new BadCommandException("GETQUOTAROOT not supported");

	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	Response[] r = command("GETQUOTAROOT", args);

	Response response = r[r.length-1];

	Map<String, Quota> tab = new HashMap<>();

	// Grab all QUOTAROOT and QUOTA responses
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("QUOTAROOT")) {
		    // quotaroot_response
		    //		       ::= "QUOTAROOT" SP astring *(SP astring)

		    // read name of mailbox and throw away
		    ir.readAtomString();
		    // for each quotaroot add a placeholder quota
		    String root = null;
		    while ((root = ir.readAtomString()) != null &&
			    root.length() > 0)
			tab.put(root, new Quota(root));
		    r[i] = null;
		} else if (ir.keyEquals("QUOTA")) {
		    Quota quota = parseQuota(ir);
		    Quota q = tab.get(quota.quotaRoot);
		    if (q != null && q.resources != null) {
			// merge resources
			int newl = q.resources.length + quota.resources.length;
			Quota.Resource[] newr = new Quota.Resource[newl];
			System.arraycopy(q.resources, 0, newr, 0,
							q.resources.length);
			System.arraycopy(quota.resources, 0,
			    newr, q.resources.length, quota.resources.length);
			quota.resources = newr;
		    }
		    tab.put(quota.quotaRoot, quota);
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);

	return tab.values().toArray(new Quota[tab.size()]);
    }

    /**
     * GETQUOTA Command.
     *
     * Returns an array of Quota objects, representing the quotas
     * for this quotaroot.
     *
     * @param	root	the quotaroot
     * @return		the quotas
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2087"
     */
    public Quota[] getQuota(String root) throws ProtocolException {
	if (!hasCapability("QUOTA")) 
	    throw new BadCommandException("QUOTA not supported");

	Argument args = new Argument();	
	args.writeString(root);		// XXX - could be UTF-8?

	Response[] r = command("GETQUOTA", args);

	Quota quota = null;
	List<Quota> v = new ArrayList<>();
	Response response = r[r.length-1];

	// Grab all QUOTA responses
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("QUOTA")) {
		    quota = parseQuota(ir);
		    v.add(quota);
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return v.toArray(new Quota[v.size()]);
    }

    /**
     * SETQUOTA Command.
     *
     * Set the indicated quota on the corresponding quotaroot.
     *
     * @param	quota	the quota to set
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2087"
     */
    public void setQuota(Quota quota) throws ProtocolException {
	if (!hasCapability("QUOTA")) 
	    throw new BadCommandException("QUOTA not supported");

	Argument args = new Argument();	
	args.writeString(quota.quotaRoot);	// XXX - could be UTF-8?
	Argument qargs = new Argument();	
	if (quota.resources != null) {
	    for (int i = 0; i < quota.resources.length; i++) {
		qargs.writeAtom(quota.resources[i].name);
		qargs.writeNumber(quota.resources[i].limit);
	    }
	}
	args.writeArgument(qargs);

	Response[] r = command("SETQUOTA", args);
	Response response = r[r.length-1];

	// XXX - It's not clear from the RFC whether the SETQUOTA command
	// will provoke untagged QUOTA responses.  If it does, perhaps
	// we should grab them here and return them?

	/*
	Quota quota = null;
	List<Quota> v = new ArrayList<Quota>();

	// Grab all QUOTA responses
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("QUOTA")) {
		    quota = parseQuota(ir);
		    v.add(quota);
		    r[i] = null;
		}
	    }
	}
	*/

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	/*
	return v.toArray(new Quota[v.size()]);
	*/
    }

    /**
     * Parse a QUOTA response.
     */
    private Quota parseQuota(Response r) throws ParsingException {
	// quota_response ::= "QUOTA" SP astring SP quota_list
	String quotaRoot = r.readAtomString();	// quotaroot ::= astring
	Quota q = new Quota(quotaRoot);
	r.skipSpaces();
	// quota_list ::= "(" #quota_resource ")"
	if (r.readByte() != '(')
	    throw new ParsingException("parse error in QUOTA");

	List<Quota.Resource> v = new ArrayList<>();
	while (!r.isNextNonSpace(')')) {
	    // quota_resource ::= atom SP number SP number
	    String name = r.readAtom();
	    if (name != null) {
		long usage = r.readLong();
		long limit = r.readLong();
		Quota.Resource res = new Quota.Resource(name, usage, limit);
		v.add(res);
	    }
	}
	q.resources = v.toArray(new Quota.Resource[v.size()]);
	return q;
    }


    /**
     * SETACL Command.
     *
     * @param	mbox	the mailbox
     * @param	modifier	the ACL modifier
     * @param	acl	the ACL
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2086"
     */
    public void setACL(String mbox, char modifier, ACL acl)
				throws ProtocolException {
	if (!hasCapability("ACL")) 
	    throw new BadCommandException("ACL not supported");

	Argument args = new Argument();	
	writeMailboxName(args, mbox);
	args.writeString(acl.getName());
	String rights = acl.getRights().toString();
	if (modifier == '+' || modifier == '-')
	    rights = modifier + rights;
	args.writeString(rights);

	Response[] r = command("SETACL", args);
	Response response = r[r.length-1];

	// dispatch untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
    }

    /**
     * DELETEACL Command.
     *
     * @param	mbox	the mailbox
     * @param	user	the user
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2086"
     */
    public void deleteACL(String mbox, String user) throws ProtocolException {
	if (!hasCapability("ACL")) 
	    throw new BadCommandException("ACL not supported");

	Argument args = new Argument();	
	writeMailboxName(args, mbox);
	args.writeString(user);		// XXX - could be UTF-8?

	Response[] r = command("DELETEACL", args);
	Response response = r[r.length-1];

	// dispatch untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
    }

    /**
     * GETACL Command.
     *
     * @param	mbox	the mailbox
     * @return		the ACL array
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2086"
     */
    public ACL[] getACL(String mbox) throws ProtocolException {
	if (!hasCapability("ACL")) 
	    throw new BadCommandException("ACL not supported");

	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	Response[] r = command("GETACL", args);
	Response response = r[r.length-1];

	// Grab all ACL responses
	List<ACL> v = new ArrayList<>();
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("ACL")) {
		    // acl_data ::= "ACL" SPACE mailbox
		    //		*(SPACE identifier SPACE rights)
		    // read name of mailbox and throw away
		    ir.readAtomString();
		    String name = null;
		    while ((name = ir.readAtomString()) != null) {
			String rights = ir.readAtomString();
			if (rights == null)
			    break;
			ACL acl = new ACL(name, new Rights(rights));
			v.add(acl);
		    }
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return v.toArray(new ACL[v.size()]);
    }

    /**
     * LISTRIGHTS Command.
     *
     * @param	mbox	the mailbox
     * @param	user	the user rights to return
     * @return		the rights array
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2086"
     */
    public Rights[] listRights(String mbox, String user)
				throws ProtocolException {
	if (!hasCapability("ACL")) 
	    throw new BadCommandException("ACL not supported");

	Argument args = new Argument();	
	writeMailboxName(args, mbox);
	args.writeString(user);		// XXX - could be UTF-8?

	Response[] r = command("LISTRIGHTS", args);
	Response response = r[r.length-1];

	// Grab LISTRIGHTS response
	List<Rights> v = new ArrayList<>();
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("LISTRIGHTS")) {
		    // listrights_data ::= "LISTRIGHTS" SPACE mailbox
		    //		SPACE identifier SPACE rights *(SPACE rights)
		    // read name of mailbox and throw away
		    ir.readAtomString();
		    // read identifier and throw away
		    ir.readAtomString();
		    String rights;
		    while ((rights = ir.readAtomString()) != null)
			v.add(new Rights(rights));
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return v.toArray(new Rights[v.size()]);
    }

    /**
     * MYRIGHTS Command.
     *
     * @param	mbox	the mailbox
     * @return		the rights
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2086"
     */
    public Rights myRights(String mbox) throws ProtocolException {
	if (!hasCapability("ACL")) 
	    throw new BadCommandException("ACL not supported");

	Argument args = new Argument();	
	writeMailboxName(args, mbox);

	Response[] r = command("MYRIGHTS", args);
	Response response = r[r.length-1];

	// Grab MYRIGHTS response
	Rights rights = null;
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("MYRIGHTS")) {
		    // myrights_data ::= "MYRIGHTS" SPACE mailbox SPACE rights
		    // read name of mailbox and throw away
		    ir.readAtomString();
		    String rs = ir.readAtomString();
		    if (rights == null)
			rights = new Rights(rs);
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return rights;
    }

    /*
     * The tag used on the IDLE command.  Set by idleStart() and
     * used in processIdleResponse() to determine if the response
     * is the matching end tag.
     */
    private volatile String idleTag;

    /**
     * IDLE Command. <p>
     *
     * If the server supports the IDLE command extension, the IDLE
     * command is issued and this method blocks until a response has
     * been received.  Once the first response has been received, the
     * IDLE command is terminated and all responses are collected and
     * handled and this method returns. <p>
     *
     * Note that while this method is blocked waiting for a response,
     * no other threads may issue any commands to the server that would
     * use this same connection.
     *
     * @exception	ProtocolException	for protocol failures
     * @see "RFC2177"
     * @since	JavaMail 1.4.1
     */
    public synchronized void idleStart() throws ProtocolException {
	if (!hasCapability("IDLE")) 
	    throw new BadCommandException("IDLE not supported");

	List<Response> v = new ArrayList<>();
	boolean done = false;
	Response r = null;

	// write the command
	try {
	    idleTag = writeCommand("IDLE", null);
	} catch (LiteralException lex) {
	    v.add(lex.getResponse());
	    done = true;
	} catch (Exception ex) {
	    // Convert this into a BYE response
	    v.add(Response.byeResponse(ex));
	    done = true;
	}

	while (!done) {
	    try {
		r = readResponse();
	    } catch (IOException ioex) {
		// convert this into a BYE response
		r = Response.byeResponse(ioex);
	    } catch (ProtocolException pex) {
		continue; // skip this response
	    }

	    v.add(r);

	    if (r.isContinuation() || r.isBYE())
		done = true;
	}

	Response[] responses = v.toArray(new Response[v.size()]);
	r = responses[responses.length-1];

	// dispatch remaining untagged responses
	notifyResponseHandlers(responses);
	if (!r.isContinuation())
	    handleResult(r);
    }

    /**
     * While an IDLE command is in progress, read a response
     * sent from the server.  The response is read with no locks
     * held so that when the read blocks waiting for the response
     * from the server it's not holding locks that would prevent
     * other threads from interrupting the IDLE command.
     *
     * @return	the response
     * @since	JavaMail 1.4.1
     */
    public synchronized Response readIdleResponse() {
	if (idleTag == null)
	    return null;	// IDLE not in progress
	Response r = null;
	try {
	    r = readResponse();
	} catch (IOException ioex) {
	    // convert this into a BYE response
	    r = Response.byeResponse(ioex);
	} catch (ProtocolException pex) {
	    // convert this into a BYE response
	    r = Response.byeResponse(pex);
	}
	return r;
    }

    /**
     * Process a response returned by readIdleResponse().
     * This method will be called with appropriate locks
     * held so that the processing of the response is safe.
     *
     * @param	r	the response
     * @return		true if IDLE is done
     * @exception	ProtocolException	for protocol failures
     * @since	JavaMail 1.4.1
     */
    public boolean processIdleResponse(Response r) throws ProtocolException {
	Response[] responses = new Response[1];
	responses[0] = r;
	boolean done = false;		// done reading responses?
	notifyResponseHandlers(responses);

	if (r.isBYE()) // shouldn't wait for command completion response
	    done = true;

	// If this is a matching command completion response, we are done
	if (r.isTagged() && r.getTag().equals(idleTag))
	    done = true;

	if (done)
	    idleTag = null;	// no longer in IDLE

	handleResult(r);
	return !done;
    }

    // the DONE command to break out of IDLE
    private static final byte[] DONE = { 'D', 'O', 'N', 'E', '\r', '\n' };

    /**
     * Abort an IDLE command.  While one thread is blocked in
     * readIdleResponse(), another thread will use this method
     * to abort the IDLE command, which will cause the server
     * to send the closing tag for the IDLE command, which
     * readIdleResponse() and processIdleResponse() will see
     * and terminate the IDLE state.
     *
     * @since	JavaMail 1.4.1
     */
    public void idleAbort() {
	OutputStream os = getOutputStream();
	try {
	    os.write(DONE);
	    os.flush();
	} catch (Exception ex) {
	    // nothing to do, hope to detect it again later
	    logger.log(Level.FINEST, "Exception aborting IDLE", ex);
	}
    }

    /**
     * ID Command.
     *
     * @param	clientParams	map of names and values
     * @return			map of names and values from server
     * @exception	ProtocolException	for protocol failures
     * @see "RFC 2971"
     * @since	JavaMail 1.5.1
     */
    public Map<String, String> id(Map<String, String> clientParams)
				throws ProtocolException {
	if (!hasCapability("ID")) 
	    throw new BadCommandException("ID not supported");

	Response[] r = command("ID", ID.getArgumentList(clientParams));

	ID id = null;
	Response response = r[r.length-1];

	// Grab ID response
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("ID")) {
		    if (id == null)
			id = new ID(ir);
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return id == null ? null : id.getServerParams();
    }
}
