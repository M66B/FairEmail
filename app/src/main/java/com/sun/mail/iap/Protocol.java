/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.iap;

import android.text.TextUtils;

import java.util.Properties;
import java.io.*;
import java.nio.channels.SocketChannel;
import java.net.*;
import javax.net.ssl.SSLSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.lang.reflect.Field;

import com.sun.mail.util.PropUtil;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;

/**
 * General protocol handling code for IMAP-like protocols. <p>
 *
 * The Protocol object is multithread safe.
 *
 * @author  John Mani
 * @author  Max Spivak
 * @author  Bill Shannon
 */

public class Protocol {
    protected String host;
    private Socket socket;
    // in case we turn on TLS, we'll need these later
    protected boolean quote;
    protected MailLogger logger;
    protected MailLogger traceLogger;
    protected Properties props;
    protected String prefix;

    private TraceInputStream traceInput;	// the Tracer
    private volatile ResponseInputStream input;

    private TraceOutputStream traceOutput;	// the Tracer
    private volatile DataOutputStream output;

    private int tagCounter = 0;
    private final String tagPrefix;

    private String localHostName;

    private final List<ResponseHandler> handlers
	    = new CopyOnWriteArrayList<>();

    private volatile long timestamp;

    // package private, to allow testing
    static final AtomicInteger tagNum = new AtomicInteger();

    private static final byte[] CRLF = { (byte)'\r', (byte)'\n'};
 
    /**
     * Constructor. <p>
     * 
     * Opens a connection to the given host at given port.
     *
     * @param host	host to connect to
     * @param port	portnumber to connect to
     * @param props     Properties object used by this protocol
     * @param prefix 	Prefix to prepend to property keys
     * @param isSSL 	use SSL?
     * @param logger 	log messages here
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public Protocol(String host, int port, 
		    Properties props, String prefix,
		    boolean isSSL, MailLogger logger)
		    throws IOException, ProtocolException {
	boolean connected = false;		// did constructor succeed?
	tagPrefix = computePrefix(props, prefix);
	try {
	    this.host = host;
	    this.props = props;
	    this.prefix = prefix;
	    this.logger = logger;
	    traceLogger = logger.getSubLogger("protocol", null);

	    socket = SocketFetcher.getSocket(host, port, props, prefix, isSSL);
	    quote = PropUtil.getBooleanProperty(props,
					"mail.debug.quote", false);

	    initStreams();

	    // Read server greeting
	    processGreeting(readResponse());

	    timestamp = System.currentTimeMillis();

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

    private void initStreams() throws IOException {
	traceInput = new TraceInputStream(socket.getInputStream(), traceLogger);
	traceInput.setQuote(quote);
	input = new ResponseInputStream(traceInput);

	traceOutput =
	    new TraceOutputStream(socket.getOutputStream(), traceLogger);
	traceOutput.setQuote(quote);
	output = new DataOutputStream(new BufferedOutputStream(traceOutput));
    }

    /**
     * Compute the tag prefix to be used for this connection.
     * Start with "A" - "Z", then "AA" - "ZZ", and finally "AAA" - "ZZZ".
     * Wrap around after that.
     */
    private String computePrefix(Properties props, String prefix) {
	// XXX - in case someone depends on the tag prefix
	if (PropUtil.getBooleanProperty(props,
				    prefix + ".reusetagprefix", false))
	    return "A";
	// tag prefix, wrap around after three letters
	int n = tagNum.getAndIncrement() % (26*26*26 + 26*26 + 26);
	String tagPrefix;
	if (n < 26)
	    tagPrefix = new String(new char[] { (char)('A' + n) });
	else if (n < (26*26 + 26)) {
	    n -= 26;
	    tagPrefix = new String(new char[] {
			    (char)('A' + n/26), (char)('A' + n%26) });
	} else {
	    n -= (26*26 + 26);
	    tagPrefix = new String(new char[] {
		(char)('A' + n/(26*26)),
		(char)('A' + (n%(26*26))/26),
		(char)('A' + n%26) });
	}
	return tagPrefix;
    }

    /**
     * Constructor for debugging.
     *
     * @param in	the InputStream to read from
     * @param out	the PrintStream to write to
     * @param props     Properties object used by this protocol
     * @param debug	true to enable debugging output
     * @exception	IOException	for I/O errors
     */
    public Protocol(InputStream in, PrintStream out, Properties props,
				boolean debug) throws IOException {
	this.host = "localhost";
	this.props = props;
	this.quote = false;
	tagPrefix = computePrefix(props, "mail.imap");
	logger = new MailLogger(this.getClass(), "DEBUG", debug, System.out);
	traceLogger = logger.getSubLogger("protocol", null);

	// XXX - inlined initStreams, won't allow later startTLS
	traceInput = new TraceInputStream(in, traceLogger);
	traceInput.setQuote(quote);
	input = new ResponseInputStream(traceInput);

	traceOutput = new TraceOutputStream(out, traceLogger);
	traceOutput.setQuote(quote);
	output = new DataOutputStream(new BufferedOutputStream(traceOutput));

        timestamp = System.currentTimeMillis();
    }

    /**
     * Returns the timestamp.
     *
     * @return	the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
 
    /**
     * Adds a response handler.
     *
     * @param	h	the response handler
     */
    public void addResponseHandler(ResponseHandler h) {
	handlers.add(h);
    }

    /**
     * Removed the specified response handler.
     *
     * @param	h	the response handler
     */
    public void removeResponseHandler(ResponseHandler h) {
	handlers.remove(h);
    }

    /**
     * Notify response handlers
     *
     * @param	responses	the responses
     */
    public void notifyResponseHandlers(Response[] responses) {
	if (handlers.isEmpty()) {
	    return;
	}

	for (Response r : responses) {
	    if (r != null) {
		for (ResponseHandler rh : handlers) {
		    if (rh != null) {
			rh.handleResponse(r);
		    }
		}
	    }
	}
    }

    protected void processGreeting(Response r) throws ProtocolException {
	if (r.isBYE())
	    throw new ConnectionException(this, r);
    }

    /**
     * Return the Protocol's InputStream.
     *
     * @return	the input stream
     */
    protected ResponseInputStream getInputStream() {
	return input;
    }

    /**
     * Return the Protocol's OutputStream
     *
     * @return	the output stream
     */
    protected OutputStream getOutputStream() {
	return output;
    }

    /**
     * Returns whether this Protocol supports non-synchronizing literals
     * Default is false. Subclasses should override this if required
     *
     * @return	true if the server supports non-synchronizing literals
     */
    protected synchronized boolean supportsNonSyncLiterals() {
	return false;
    }

    public Response readResponse() 
		throws IOException, ProtocolException {
	return new Response(this);
    }

    /**
     * Is another response available in our buffer?
     *
     * @return	true if another response is in the buffer
     * @since	JavaMail 1.5.4
     */
    public boolean hasResponse() {
	/*
	 * XXX - Really should peek ahead in the buffer to see
	 * if there's a *complete* response available, but if there
	 * isn't who's going to read more data into the buffer 
	 * until there is?
	 */
	try {
	    return input.available() > 0;
	} catch (IOException ex) {
	}
	return false;
    }

    /**
     * Return a buffer to be used to read a response.
     * The default implementation returns null, which causes
     * a new buffer to be allocated for every response.
     *
     * @return	the buffer to use
     * @since	JavaMail 1.4.1
     */
    protected ByteArray getResponseBuffer() {
	return null;
    }

    public String writeCommand(String command, Argument args) 
		throws IOException, ProtocolException {
	// assert Thread.holdsLock(this);
	// can't assert because it's called from constructor
	String tag = tagPrefix + Integer.toString(tagCounter++); // unique tag

	output.writeBytes(tag + " " + command);
    
	if (args != null) {
	    output.write(' ');
	    args.write(this);
	}

	output.write(CRLF);
	output.flush();
	return tag;
    }

    /**
     * Send a command to the server. Collect all responses until either
     * the corresponding command completion response or a BYE response 
     * (indicating server failure).  Return all the collected responses.
     *
     * @param	command	the command
     * @param	args	the arguments
     * @return		array of Response objects returned by the server
     */
    public synchronized Response[] command(String command, Argument args) {
	if (socket == null)
		return new Response[]{Response.byeResponse(new SocketException("disconnected"))};
	if ("LOGOUT".equals(command))
		try {
			socket.setSoTimeout(10 * 1000);
		} catch (SocketException ex) {
			eu.faircode.email.Log.e(ex);
		}

	commandStart(command);
	List<Response> v = new ArrayList<>();
	boolean done = false;
	String tag = null;

	// write the command
	try {
	    tag = writeCommand(command, args);
	} catch (LiteralException lex) {
	    v.add(lex.getResponse());
	    done = true;
	} catch (Exception ex) {
	    // Convert this into a BYE response
	    v.add(Response.byeResponse(ex));
	    done = true;
	}

	Response byeResp = null;
	while (!done) {
	    Response r = null;
	    try {
		r = readResponse();
	    } catch (IOException ioex) {
		if (byeResp == null)	// convert this into a BYE response
		    byeResp = Response.byeResponse(ioex);
		// else, connection closed after BYE was sent
		break;
	    } catch (ProtocolException pex) {
		logger.log(Level.FINE, "ignoring bad response", pex);
		continue; // skip this response
	    }

	    if (r.isBYE()) {
		byeResp = r;
		continue;
	    }

	    v.add(r);

	    // If this is a matching command completion response, we are done
	    if (r.isTagged() && r.getTag().equals(tag))
		done = true;
	}

	if (byeResp != null)
		v.add(byeResp);	// must be last
	Response[] responses = new Response[v.size()];
	v.toArray(responses);
        timestamp = System.currentTimeMillis();
	commandEnd();
	return responses;
    }

    /**
     * Convenience routine to handle OK, NO, BAD and BYE responses.
     *
     * @param	response	the response
     * @exception	ProtocolException	for protocol failures
     */
    public void handleResult(Response response) throws ProtocolException {
	if (response.isOK())
	    return;
	else if (response.isNO())
	    throw new CommandFailedException(response);
	else if (response.isBAD())
	    throw new BadCommandException(response);
	else if (response.isBYE()) {
	    disconnect();
	    throw new ConnectionException(this, response);
	}
    }

    /**
     * Convenience routine to handle simple IAP commands
     * that do not have responses specific to that command.
     *
     * @param	cmd	the command
     * @param	args	the arguments
     * @exception	ProtocolException	for protocol failures
     */
    public void simpleCommand(String cmd, Argument args)
			throws ProtocolException {
	// Issue command
	Response[] r = command(cmd, args);

	// dispatch untagged responses
	notifyResponseHandlers(r);

	// Handle result of this command
	handleResult(r[r.length-1]);
    }

    /**
     * Start TLS on the current connection.
     * <code>cmd</code> is the command to issue to start TLS negotiation.
     * If the command succeeds, we begin TLS negotiation.
     * If the socket is already an SSLSocket this is a nop and the command
     * is not issued.
     *
     * @param	cmd	the command to issue
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public synchronized void startTLS(String cmd)
				throws IOException, ProtocolException {
	if (socket instanceof SSLSocket)
	    return;	// nothing to do
	simpleCommand(cmd, null);
	socket = SocketFetcher.startTLS(socket, host, props, prefix);
	initStreams();
    }

    /**
     * Start compression on the current connection.
     * <code>cmd</code> is the command to issue to start compression.
     * If the command succeeds, we begin compression.
     *
     * @param	cmd	the command to issue
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public synchronized void startCompression(String cmd)
				throws IOException, ProtocolException {
	// XXX - check whether compression is already enabled?
	simpleCommand(cmd, null);

	// need to create our own Inflater and Deflater in order to set nowrap
	Inflater inf = new Inflater(true);
	traceInput = new TraceInputStream(new InflaterInputStream(
			    socket.getInputStream(), inf), traceLogger);
	traceInput.setQuote(quote);
	input = new ResponseInputStream(traceInput);

	// configure the Deflater
	int level = PropUtil.getIntProperty(props, prefix + ".compress.level",
						Deflater.DEFAULT_COMPRESSION);
	int strategy = PropUtil.getIntProperty(props,
						prefix + ".compress.strategy",
						Deflater.DEFAULT_STRATEGY);
	if (logger.isLoggable(Level.FINE))
	    logger.log(Level.FINE,
		"Creating Deflater with compression level {0} and strategy {1}",
		new Object[] { level, strategy });
	Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
	try {
	    def.setLevel(level);
	} catch (IllegalArgumentException ex) {
	    logger.log(Level.FINE, "Ignoring bad compression level", ex);
	}
	try {
	    def.setStrategy(strategy);
	} catch (IllegalArgumentException ex) {
	    logger.log(Level.FINE, "Ignoring bad compression strategy", ex);
	}
	traceOutput = new TraceOutputStream(new DeflaterOutputStream(
			    socket.getOutputStream(), def, true), traceLogger);
	traceOutput.setQuote(quote);
	output = new DataOutputStream(new BufferedOutputStream(traceOutput));
    }

    /**
     * Is this connection using an SSL socket?
     *
     * @return	true if using SSL
     * @since	JavaMail 1.4.6
     */
    public boolean isSSL() {
	return socket instanceof SSLSocket;
    }

    /**
     * Return the address the socket connected to.
     *
     * @return	the InetAddress the socket is connected to
     * @since	JavaMail 1.5.2
     */
    public InetAddress getInetAddress() {
	return socket.getInetAddress();
    }

    /**
     * Return the SocketChannel associated with this connection, if any.
     *
     * @return	the SocketChannel
     * @since	JavaMail 1.5.2
     */
    public SocketChannel getChannel() {
	SocketChannel ret = socket.getChannel();
	if (ret != null)
		return ret;

	if (socket instanceof SSLSocket) {
		ret = Protocol.findSocketChannel(socket);
	}
	return ret;
    }

    /**
     * Android is broken and SSL wrapped sockets don't delegate
     * the getChannel method to the wrapped Socket.
     * 
     * @param socket a non null socket
     * @return the SocketChannel or null if not found
     */
    private static SocketChannel findSocketChannel(Socket socket) {
	//Search class hierarchy for field name socket regardless of modifier.
	for (Class<?> k = socket.getClass(); k != Object.class; k = k.getSuperclass()) {
		try {
			Field f = k.getDeclaredField("socket");
			f.setAccessible(true);
			Socket s = (Socket) f.get(socket);
			SocketChannel ret = s.getChannel();
			if (ret != null) {
				return ret;
			}
		} catch (Exception ignore) {
			//ignore anything that might go wrong
		}
	}
	
	//Search class hierarchy for fields that can hold a Socket
	//or subclass regardless of modifier.  Fields declared as super types of Socket
	//will be ignored.
	for (Class<?> k = socket.getClass(); k != Object.class; k = k.getSuperclass()) {
		try {
			for (Field f : k.getDeclaredFields()) {
				if (Socket.class.isAssignableFrom(f.getType())) {
					try {
						f.setAccessible(true);
						Socket s = (Socket) f.get(socket);
						SocketChannel ret = s.getChannel();
						if (ret != null) {
							return ret;
						}
					} catch (Exception ignore) {
						//ignore anything that might go wrong
					}
				}
			}
		} catch (Exception ignore) {
			//ignore anything that might go wrong
		}
	}
	return null;
    }

    /**
     * Return the local SocketAddress (host and port) for this
     * end of the connection.
     *
     * @return	the SocketAddress
     * @since	Jakarta Mail 1.6.4
     */
    public SocketAddress getLocalSocketAddress() {
	return socket.getLocalSocketAddress();
    }

    /**
     * Does the server support UTF-8?
     * This implementation returns false.
     * Subclasses should override as appropriate.
     *
     * @return	true if the server supports UTF-8
     * @since JavaMail 1.6.0
     */
    public boolean supportsUtf8() {
	return false;
    }

    /**
     * Disconnect.
     */
    protected synchronized void disconnect() {
	if (socket != null) {
	    try {
		socket.close();
	    } catch (IOException e) {
		// ignore it
	    }
	    socket = null;
		if (traceInput != null && traceOutput != null)
			eu.faircode.email.TrafficStatsHelper.report(host, prefix,
					traceOutput.getSent(), traceInput.getReceived());
	}
    }

    /**
     * Get the name of the local host.
     * The property &lt;prefix&gt;.localhost overrides
     * &lt;prefix&gt;.localaddress,
     * which overrides what InetAddress would tell us.
     *
     * @return	the name of the local host
     */
    protected synchronized String getLocalHost() {
	// get our hostname and cache it for future use
	if (localHostName == null || localHostName.length() <= 0)
	    localHostName =
		    props.getProperty(prefix + ".localhost");
	if (localHostName == null || localHostName.length() <= 0)
	    localHostName =
		    props.getProperty(prefix + ".localaddress");
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
	    if (socket != null && socket.isBound()) {
		InetAddress localHost = socket.getLocalAddress();
		localHostName = localHost.getCanonicalHostName();
		// if we can't get our name, use local address literal
		if (localHostName == null)
		    // XXX - not correct for IPv6
		    localHostName = "[" + localHost.getHostAddress() + "]";
	    }
	}
	if (TextUtils.isEmpty(localHostName))
		localHostName = "localhost";
	return localHostName;
    }

    /**
     * Is protocol tracing enabled?
     *
     * @return	true if protocol tracing is enabled
     */
    protected boolean isTracing() {
	return traceLogger.isLoggable(Level.FINEST);
    }

    /**
     * Temporarily turn off protocol tracing, e.g., to prevent
     * tracing the authentication sequence, including the password.
     */
    protected void suspendTracing() {
	if (traceLogger.isLoggable(Level.FINEST)) {
	    traceInput.setTrace(false);
	    traceOutput.setTrace(false);
	}
    }

    /**
     * Resume protocol tracing, if it was enabled to begin with.
     */
    protected void resumeTracing() {
	if (traceLogger.isLoggable(Level.FINEST)) {
	    traceInput.setTrace(true);
	    traceOutput.setTrace(true);
	}
    }

    /**
     * Finalizer.
     */
    @Override
    protected void finalize() throws Throwable {
	try {
	    disconnect();
	} finally {
	    super.finalize();
	}
    }

    /*
     * Probe points for GlassFish monitoring.
     */
    private void commandStart(String command) { }
    private void commandEnd() { }
}
