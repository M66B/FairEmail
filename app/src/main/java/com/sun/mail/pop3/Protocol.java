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

package com.sun.mail.pop3;

import java.util.*;
import java.net.*;
import java.io.*;
import java.security.*;
import java.util.logging.Level;
import javax.net.ssl.SSLSocket;

import com.sun.mail.util.PropUtil;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;
import com.sun.mail.util.SharedByteArrayOutputStream;

class Response {
    boolean ok = false;		// true if "+OK"
    String data = null;		// rest of line after "+OK" or "-ERR"
    InputStream bytes = null;	// all the bytes from a multi-line response
}

/**
 * This class provides a POP3 connection and implements 
 * the POP3 protocol requests.
 *
 * APOP support courtesy of "chamness".
 *
 * @author      Bill Shannon
 */
class Protocol {
    private Socket socket;		// POP3 socket
    private String host;		// host we're connected to
    private Properties props;		// session properties
    private String prefix;		// protocol name prefix, for props
    private BufferedReader input;	// input buf
    private PrintWriter output;		// output buf
    private TraceInputStream traceInput;
    private TraceOutputStream traceOutput;
    private MailLogger logger;
    private MailLogger traceLogger;
    private String apopChallenge = null;
    private Map<String, String> capabilities = null;
    private boolean pipelining;
    private boolean noauthdebug = true;	// hide auth info in debug output
    private boolean traceSuspended;	// temporarily suspend tracing

    private static final int POP3_PORT = 110; // standard POP3 port
    private static final String CRLF = "\r\n";
    // sometimes the returned size isn't quite big enough
    private static final int SLOP = 128;

    /** 
     * Open a connection to the POP3 server.
     */
    Protocol(String host, int port, MailLogger logger,
			Properties props, String prefix, boolean isSSL)
			throws IOException {
	this.host = host;
	this.props = props;
	this.prefix = prefix;
	this.logger = logger;
	traceLogger = logger.getSubLogger("protocol", null);
	noauthdebug = !PropUtil.getBooleanProperty(props,
			    "mail.debug.auth", false);

	Response r;
	boolean enableAPOP = getBoolProp(props, prefix + ".apop.enable");
	boolean disableCapa = getBoolProp(props, prefix + ".disablecapa");
	try {
	    if (port == -1)
		port = POP3_PORT;
	    if (logger.isLoggable(Level.FINE))
		logger.fine("connecting to host \"" + host +
				"\", port " + port + ", isSSL " + isSSL);

	    socket = SocketFetcher.getSocket(host, port, props, prefix, isSSL);
	    initStreams();
	    r = simpleCommand(null);
	} catch (IOException ioe) {
	    throw cleanupAndThrow(socket, ioe);
	}

	if (!r.ok) {
	    throw cleanupAndThrow(socket, new IOException("Connect failed"));
	}
	if (enableAPOP && r.data != null) {
	    int challStart = r.data.indexOf('<');	// start of challenge
	    int challEnd = r.data.indexOf('>', challStart); // end of challenge
	    if (challStart != -1 && challEnd != -1)
		apopChallenge = r.data.substring(challStart, challEnd + 1);
	    logger.log(Level.FINE, "APOP challenge: {0}", apopChallenge);
	}

	// if server supports RFC 2449, set capabilities
	if (!disableCapa)
	    setCapabilities(capa());

	pipelining = hasCapability("PIPELINING") ||
	    PropUtil.getBooleanProperty(props, prefix + ".pipelining", false);
	if (pipelining)
	    logger.config("PIPELINING enabled");
    }

    private static IOException cleanupAndThrow(Socket socket, IOException ife) {
	try {
	    socket.close();
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

    /**
     * Get the value of a boolean property.
     * Print out the value if logging is enabled.
     */
    private final synchronized boolean getBoolProp(Properties props,
				String prop) {
	boolean val = PropUtil.getBooleanProperty(props, prop, false);
	if (logger.isLoggable(Level.CONFIG))
	    logger.config(prop + ": " + val);
	return val;
    }

    private void initStreams() throws IOException {
	boolean quote = PropUtil.getBooleanProperty(props,
					"mail.debug.quote", false);
	traceInput =
	    new TraceInputStream(socket.getInputStream(), traceLogger);
	traceInput.setQuote(quote);

	traceOutput =
	    new TraceOutputStream(socket.getOutputStream(), traceLogger);
	traceOutput.setQuote(quote);

	// should be US-ASCII, but not all JDK's support it so use iso-8859-1
	input = new BufferedReader(new InputStreamReader(traceInput,
							    "iso-8859-1"));
	output = new PrintWriter(
		    new BufferedWriter(
			new OutputStreamWriter(traceOutput, "iso-8859-1")));
    }

    @Override
    protected void finalize() throws Throwable {
	try {
	    if (socket != null)	// Forgot to logout ?!
		quit();
	} finally {
	    super.finalize();
	}
    }

    /**
     * Parse the capabilities from a CAPA response.
     */
    synchronized void setCapabilities(InputStream in) {
	if (in == null) {
	    capabilities = null;
	    return;
	}

	capabilities = new HashMap<>(10);
	BufferedReader r = null;
	try {
	    r = new BufferedReader(new InputStreamReader(in, "us-ascii"));
	} catch (UnsupportedEncodingException ex) {
	    // should never happen
	    assert false;
	}
	String s;
	try {
	    while ((s = r.readLine()) != null) {
		String cap = s;
		int i = cap.indexOf(' ');
		if (i > 0)
		    cap = cap.substring(0, i);
		capabilities.put(cap.toUpperCase(Locale.ENGLISH), s);
	    }
	} catch (IOException ex) {
	    // should never happen
	} finally {
	    try {
		in.close();
	    } catch (IOException ex) { }
	}
    }

    /**
     * Check whether the given capability is supported by
     * this server. Returns <code>true</code> if so, otherwise
     * returns false.
     */
    synchronized boolean hasCapability(String c) {
	return capabilities != null &&
		capabilities.containsKey(c.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Return the map of capabilities returned by the server.
     */
    synchronized Map<String, String> getCapabilities() {
	return capabilities;
    }

    /**
     * Login to the server, using the USER and PASS commands.
     */
    synchronized String login(String user, String password)
					throws IOException {
	Response r;
	// only pipeline password if connection is secure
	boolean batch = pipelining && socket instanceof SSLSocket;

	try {

	if (noauthdebug && isTracing()) {
	    logger.fine("authentication command trace suppressed");
	    suspendTracing();
	}
	String dpw = null;
	if (apopChallenge != null)
	    dpw = getDigest(password);
	if (apopChallenge != null && dpw != null) {
	    r = simpleCommand("APOP " + user + " " + dpw);
	} else if (batch) {
	    String cmd = "USER " + user;
	    batchCommandStart(cmd);
	    issueCommand(cmd);
	    cmd = "PASS " + password;
	    batchCommandContinue(cmd);
	    issueCommand(cmd);
	    r = readResponse();
	    if (!r.ok) {
		String err = r.data != null ? r.data : "USER command failed";
		readResponse();	// read and ignore PASS response
		batchCommandEnd();
		return err;
	    }
	    r = readResponse();
	    batchCommandEnd();
	} else {
	    r = simpleCommand("USER " + user);
	    if (!r.ok)
		return r.data != null ? r.data : "USER command failed";
	    r = simpleCommand("PASS " + password);
	}
	if (noauthdebug && isTracing())
	    logger.log(Level.FINE, "authentication command {0}",
			(r.ok ? "succeeded" : "failed"));
	if (!r.ok)
	    return r.data != null ? r.data : "login failed";
	return null;

	} finally {
	    resumeTracing();
	}
    }

    /**
     * Gets the APOP message digest. 
     * From RFC 1939:
     *
     * The 'digest' parameter is calculated by applying the MD5
     * algorithm [RFC1321] to a string consisting of the timestamp
     * (including angle-brackets) followed by a shared secret.
     * The 'digest' parameter itself is a 16-octet value which is
     * sent in hexadecimal format, using lower-case ASCII characters.
     *
     * @param	password	The APOP password
     * @return		The APOP digest or an empty string if an error occurs.
     */
    private String getDigest(String password) {
	String key = apopChallenge + password;
	byte[] digest;
	try {
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    digest = md.digest(key.getBytes("iso-8859-1"));	// XXX
	} catch (NoSuchAlgorithmException nsae) {
	    return null;
	} catch (UnsupportedEncodingException uee) {
	    return null;
	}
	return toHex(digest);
    }

    private static char[] digits = {
	'0', '1', '2', '3', '4', '5', '6', '7',
	'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
     * Convert a byte array to a string of hex digits representing the bytes.
     */
    private static String toHex(byte[] bytes) {
	char[] result = new char[bytes.length * 2];

	for (int index = 0, i = 0; index < bytes.length; index++) {
	    int temp = bytes[index] & 0xFF;
	    result[i++] = digits[temp >> 4];
	    result[i++] = digits[temp & 0xF];
	}
	return new String(result);
    }

    /**
     * Close down the connection, sending the QUIT command.
     */
    synchronized boolean quit() throws IOException {
	boolean ok = false;
	try {
	    Response r = simpleCommand("QUIT");
	    ok = r.ok;
	} finally {
	    close();
	}
	return ok;
    }

    /**
     * Close the connection without sending any commands.
     */
    void close() {
	try {
	    if (socket != null)
		socket.close();
	} catch (IOException ex) {
	    // ignore it
	} finally {
	    socket = null;
	    input = null;
	    output = null;
	}
    }

    /**
     * Return the total number of messages and mailbox size,
     * using the STAT command.
     */
    synchronized Status stat() throws IOException {
	Response r = simpleCommand("STAT");
	Status s = new Status();

	/*
	 * Normally the STAT command shouldn't fail but apparently it
	 * does when accessing Hotmail too often, returning:
	 * -ERR login allowed only every 15 minutes
	 * (Why it doesn't just fail the login, I don't know.)
	 * This is a serious failure that we don't want to hide
	 * from the user.
	 */
	if (!r.ok)
	    throw new IOException("STAT command failed: " + r.data);

	if (r.data != null) {
	    try {
		StringTokenizer st = new StringTokenizer(r.data);
		s.total = Integer.parseInt(st.nextToken());
		s.size = Integer.parseInt(st.nextToken());
	    } catch (RuntimeException e) {
	    }
	}
	return s;
    }

    /**
     * Return the size of the message using the LIST command.
     */
    synchronized int list(int msg) throws IOException {
	Response r = simpleCommand("LIST " + msg);
	int size = -1;
	if (r.ok && r.data != null) {
	    try {
		StringTokenizer st = new StringTokenizer(r.data);
		st.nextToken();    // skip message number
		size = Integer.parseInt(st.nextToken());
	    } catch (RuntimeException e) {
		// ignore it
	    }
	}
	return size;
    }

    /**
     * Return the size of all messages using the LIST command.
     */
    synchronized InputStream list() throws IOException {
	Response r = multilineCommand("LIST", 128); // 128 == output size est
	return r.bytes;
    }

    /**
     * Retrieve the specified message.
     * Given an estimate of the message's size we can be more efficient,
     * preallocating the array and returning a SharedInputStream to allow
     * us to share the array.
     */
    synchronized InputStream retr(int msg, int size) throws IOException {
	Response r;
	String cmd;
	boolean batch = size == 0 && pipelining;
	if (batch) {
	    cmd = "LIST " + msg;
	    batchCommandStart(cmd);
	    issueCommand(cmd);
	    cmd = "RETR " + msg;
	    batchCommandContinue(cmd);
	    issueCommand(cmd);
	    r = readResponse();
	    if (r.ok && r.data != null) {
		// parse the LIST response to get the message size
		try {
		    StringTokenizer st = new StringTokenizer(r.data);
		    st.nextToken();    // skip message number
		    size = Integer.parseInt(st.nextToken());
		    // don't allow ridiculous sizes
		    if (size > 1024*1024*1024 || size < 0)
			size = 0;
		    else {
			if (logger.isLoggable(Level.FINE))
			    logger.fine("pipeline message size " + size);
			size += SLOP;
		    }
		} catch (RuntimeException e) {
		}
	    }
	    r = readResponse();
	    if (r.ok)
		r.bytes = readMultilineResponse(size + SLOP);
	    batchCommandEnd();
	} else {
	    cmd = "RETR " + msg;
	    multilineCommandStart(cmd);
	    issueCommand(cmd);
	    r = readResponse();
	    if (!r.ok) {
		multilineCommandEnd();
		return null;
	    }

	    /*
	     * Many servers return a response to the RETR command of the form:
	     * +OK 832 octets
	     * If we don't have a size guess already, try to parse the response
	     * for data in that format and use it if found.  It's only a guess,
	     * but it might be a good guess.
	     */
	    if (size <= 0 && r.data != null) {
		try {
		    StringTokenizer st = new StringTokenizer(r.data);
		    String s = st.nextToken();
		    String octets = st.nextToken();
		    if (octets.equals("octets")) {
			size = Integer.parseInt(s);
			// don't allow ridiculous sizes
			if (size > 1024*1024*1024 || size < 0)
			    size = 0;
			else {
			    if (logger.isLoggable(Level.FINE))
				logger.fine("guessing message size: " + size);
			    size += SLOP;
			}
		    }
		} catch (RuntimeException e) {
		}
	    }
	    r.bytes = readMultilineResponse(size);
	    multilineCommandEnd();
	}
	if (r.ok) {
	    if (size > 0 && logger.isLoggable(Level.FINE))
		logger.fine("got message size " + r.bytes.available());
	}
	return r.bytes;
    }

    /**
     * Retrieve the specified message and stream the content to the
     * specified OutputStream.  Return true on success.
     */
    synchronized boolean retr(int msg, OutputStream os) throws IOException {
	String cmd = "RETR " + msg;
	multilineCommandStart(cmd);
	issueCommand(cmd);
	Response r = readResponse();
	if (!r.ok) {
	    multilineCommandEnd();
	    return false;
	}

	Throwable terr = null;
	int b, lastb = '\n';
	try {
	    while ((b = input.read()) >= 0) {
		if (lastb == '\n' && b == '.') {
		    b = input.read();
		    if (b == '\r') {
			// end of response, consume LF as well
			b = input.read();
			break;
		    }
		}

		/*
		 * Keep writing unless we get an error while writing,
		 * which we defer until all of the data has been read.
		 */
		if (terr == null) {
		    try {
			os.write(b);
		    } catch (IOException ex) {
			logger.log(Level.FINE, "exception while streaming", ex);
			terr = ex;
		    } catch (RuntimeException ex) {
			logger.log(Level.FINE, "exception while streaming", ex);
			terr = ex;
		    }
		}
		lastb = b;
	    }
	} catch (InterruptedIOException iioex) {
	    /*
	     * As above in simpleCommand, close the socket to recover.
	     */
	    try {
		socket.close();
	    } catch (IOException cex) { }
	    throw iioex;
	}
	if (b < 0)
	    throw new EOFException("EOF on socket");

	// was there a deferred error?
	if (terr != null) {
	    if (terr instanceof IOException)
		throw (IOException)terr;
	    if (terr instanceof RuntimeException)
		throw (RuntimeException)terr;
	    assert false;	// can't get here
	}
	multilineCommandEnd();
	return true;
    }

    /**
     * Return the message header and the first n lines of the message.
     */
    synchronized InputStream top(int msg, int n) throws IOException {
	Response r = multilineCommand("TOP " + msg + " " + n, 0);
	return r.bytes;
    }

    /**
     * Delete (permanently) the specified message.
     */
    synchronized boolean dele(int msg) throws IOException {
	Response r = simpleCommand("DELE " + msg);
	return r.ok;
    }

    /**
     * Return the UIDL string for the message.
     */
    synchronized String uidl(int msg) throws IOException {
	Response r = simpleCommand("UIDL " + msg);
	if (!r.ok)
	    return null;
	int i = r.data.indexOf(' ');
	if (i > 0)
	    return r.data.substring(i + 1);
	else
	    return null;
    }

    /**
     * Return the UIDL strings for all messages.
     * The UID for msg #N is returned in uids[N-1].
     */
    synchronized boolean uidl(String[] uids) throws IOException {
	Response r = multilineCommand("UIDL", 15 * uids.length);
	if (!r.ok)
	    return false;
	LineInputStream lis = new LineInputStream(r.bytes);
	String line = null;
	while ((line = lis.readLine()) != null) {
	    int i = line.indexOf(' ');
	    if (i < 1 || i >= line.length())
		continue;
	    int n = Integer.parseInt(line.substring(0, i));
	    if (n > 0 && n <= uids.length)
		uids[n - 1] = line.substring(i + 1);
	}
	try {
	    r.bytes.close();
	} catch (IOException ex) {
	    // ignore it
	}
	return true;
    }

    /**
     * Do a NOOP.
     */
    synchronized boolean noop() throws IOException {
	Response r = simpleCommand("NOOP");
	return r.ok;
    }

    /**
     * Do an RSET.
     */
    synchronized boolean rset() throws IOException {
	Response r = simpleCommand("RSET");
	return r.ok;
    }

    /**
     * Start TLS using STLS command specified by RFC 2595.
     * If already using SSL, this is a nop and the STLS command is not issued.
     */
    synchronized boolean stls() throws IOException {
	if (socket instanceof SSLSocket)
	    return true;	// nothing to do
	Response r = simpleCommand("STLS");
	if (r.ok) {
	    // it worked, now switch the socket into TLS mode
	    try {
		socket = SocketFetcher.startTLS(socket, host, props, prefix);
		initStreams();
	    } catch (IOException ioex) {
		try {
		    socket.close();
		} finally {
		    socket = null;
		    input = null;
		    output = null;
		}
		IOException sioex =
		    new IOException("Could not convert socket to TLS");
		sioex.initCause(ioex);
		throw sioex;
	    }
	}
	return r.ok;
    }

    /**
     * Is this connection using SSL?
     */
    synchronized boolean isSSL() {
	return socket instanceof SSLSocket;
    }

    /**
     * Get server capabilities using CAPA command specified by RFC 2449.
     * Returns null if not supported.
     */
    synchronized InputStream capa() throws IOException {
	Response r = multilineCommand("CAPA", 128); // 128 == output size est
	if (!r.ok)
	    return null;
	return r.bytes;
    }

    /**
     * Issue a simple POP3 command and return the response.
     */
    private Response simpleCommand(String cmd) throws IOException {
	simpleCommandStart(cmd);
	issueCommand(cmd);
	Response r = readResponse();
	simpleCommandEnd();
	return r;
    }

    /**
     * Send the specified command.
     */
    private void issueCommand(String cmd) throws IOException {
	if (socket == null)
	    throw new IOException("Folder is closed");	// XXX

	if (cmd != null) {
	    cmd += CRLF;
	    output.print(cmd);	// do it in one write
	    output.flush();
	}
    }

    /**
     * Read the response to a command.
     */
    private Response readResponse() throws IOException {
	String line = null;
	try {
	    line = input.readLine();
	} catch (InterruptedIOException iioex) {
	    /*
	     * If we get a timeout while using the socket, we have no idea
	     * what state the connection is in.  The server could still be
	     * alive, but slow, and could still be sending data.  The only
	     * safe way to recover is to drop the connection.
	     */
	    try {
		socket.close();
	    } catch (IOException cex) { }
	    throw new EOFException(iioex.getMessage());
	} catch (SocketException ex) {
	    /*
	     * If we get an error while using the socket, we have no idea
	     * what state the connection is in.  The server could still be
	     * alive, but slow, and could still be sending data.  The only
	     * safe way to recover is to drop the connection.
	     */
	    try {
		socket.close();
	    } catch (IOException cex) { }
	    throw new EOFException(ex.getMessage());
	}

	if (line == null) {
	    traceLogger.finest("<EOF>");
	    throw new EOFException("EOF on socket");
	}
	Response r = new Response();
	if (line.startsWith("+OK"))
	    r.ok = true;
	else if (line.startsWith("-ERR"))
	    r.ok = false;
	else
	    throw new IOException("Unexpected response: " + line);
	int i;
	if ((i = line.indexOf(' ')) >= 0)
	    r.data = line.substring(i + 1);
	return r;
    }

    /**
     * Issue a POP3 command that expects a multi-line response.
     * <code>size</code> is an estimate of the response size.
     */
    private Response multilineCommand(String cmd, int size) throws IOException {
	multilineCommandStart(cmd);
	issueCommand(cmd);
	Response r = readResponse();
	if (!r.ok) {
	    multilineCommandEnd();
	    return r;
	}
	r.bytes = readMultilineResponse(size);
	multilineCommandEnd();
	return r;
    }

    /**
     * Read the response to a multiline command after the command response.
     * The size parameter indicates the expected size of the response;
     * the actual size can be different.  Returns an InputStream to the
     * response bytes.
     */
    private InputStream readMultilineResponse(int size) throws IOException {
	SharedByteArrayOutputStream buf = new SharedByteArrayOutputStream(size);
	int b, lastb = '\n';
	try {
	    while ((b = input.read()) >= 0) {
		if (lastb == '\n' && b == '.') {
		    b = input.read();
		    if (b == '\r') {
			// end of response, consume LF as well
			b = input.read();
			break;
		    }
		}
		buf.write(b);
		lastb = b;
	    }
	} catch (InterruptedIOException iioex) {
	    /*
	     * As above in readResponse, close the socket to recover.
	     */
	    try {
		socket.close();
	    } catch (IOException cex) { }
	    throw iioex;
	}
	if (b < 0)
	    throw new EOFException("EOF on socket");
	return buf.toStream();
    }

    /**
     * Is protocol tracing enabled?
     */
    protected boolean isTracing() {
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

    /*
     * Probe points for GlassFish monitoring.
     */
    private void simpleCommandStart(String command) { }
    private void simpleCommandEnd() { }
    private void multilineCommandStart(String command) { }
    private void multilineCommandEnd() { }
    private void batchCommandStart(String command) { }
    private void batchCommandContinue(String command) { }
    private void batchCommandEnd() { }
}
