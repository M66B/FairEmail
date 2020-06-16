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

import java.net.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.Locale;


/**
 * The name of a URL. This class represents a URL name and also 
 * provides the basic parsing functionality to parse most internet 
 * standard URL schemes. <p>
 *
 * Note that this class differs from <code>java.net.URL</code> 
 * in that this class just represents the name of a URL, it does 
 * not model the connection to a URL.
 *
 * @author	Christopher Cotton
 * @author	Bill Shannon
 */

public class URLName {

    /**
     * The full version of the URL
     */
    protected String fullURL;

    /** 
     * The protocol to use (ftp, http, nntp, imap, pop3 ... etc.) . 
     */
    private String protocol;

    /** 
     * The username to use when connecting
     */
    private String username;

    /** 
     * The password to use when connecting.
     */
    private String password;

    /** 
     * The host name to which to connect. 
     */
    private String host;

    /**
     * The host's IP address, used in equals and hashCode.
     * Computed on demand.
     */
    private InetAddress hostAddress;
    private boolean hostAddressKnown = false;

    /** 
     * The protocol port to connect to. 
     */
    private int port = -1;

    /** 
     * The specified file name on that host. 
     */
    private String file;

    /** 
     * # reference. 
     */
    private String ref;

    /**
     * Our hash code.
     */
    private int hashCode = 0;

    /**
     * A way to turn off encoding, just in case...
     */
    private static boolean doEncode = true;

    static {
	try {
	    doEncode = !Boolean.getBoolean("mail.URLName.dontencode");
	} catch (Exception ex) {
	    // ignore any errors
	}
    }

    /**
     * Creates a URLName object from the specified protocol,
     * host, port number, file, username, and password. Specifying a port
     * number of -1 indicates that the URL should use the default port for
     * the protocol.
     *
     * @param	protocol	the protocol
     * @param	host		the host name
     * @param	port		the port number
     * @param	file		the file
     * @param	username	the user name
     * @param	password	the password
     */
    public URLName(
	String protocol,
	String host,
	int port,
	String file,
	String username,
	String password
	)
    {
	this.protocol = protocol;
	this.host = host;
	this.port = port;
	int refStart;
	if (file != null && (refStart = file.indexOf('#')) != -1) {
	    this.file = file.substring(0, refStart);
	    this.ref = file.substring(refStart + 1);
	} else {
	    this.file = file;
	    this.ref = null;
	}
	this.username = doEncode ? encode(username) : username;
	this.password = doEncode ? encode(password) : password;
    }

    /**
     * Construct a URLName from a java.net.URL object.
     *
     * @param	url	the URL
     */
    public URLName(URL url) {
	this(url.toString());
    }

    /**
     * Construct a URLName from the string.  Parses out all the possible
     * information (protocol, host, port, file, username, password).
     *
     * @param	url	the URL string
     */
    public URLName(String url) {
	parseString(url);
    }

    /**
     * Constructs a string representation of this URLName.
     */
    @Override
    public String toString() {
	if (fullURL == null) {
	    // add the "protocol:"
	    StringBuilder tempURL = new StringBuilder();
	    if (protocol != null) {
		tempURL.append(protocol);
		tempURL.append(":");
	    }

	    if (username != null || host != null) {
		// add the "//"
		tempURL.append("//");
		
		// add the user:password@
		// XXX - can you just have a password? without a username?
		if (username != null) {
		    tempURL.append(username);
		
		    if (password != null){
			tempURL.append(":");
			tempURL.append(password);
		    }
		
		    tempURL.append("@");
		}
	    
		// add host
		if (host != null) {
		    tempURL.append(host);
		}
	    
		// add port (if needed)
		if (port != -1) {
		    tempURL.append(":");
		    tempURL.append(Integer.toString(port));
		}
		if (file != null)
		    tempURL.append("/");
	    }
	    
	    // add the file
	    if (file != null) {
		tempURL.append(file);
	    }
	    
	    // add the ref
	    if (ref != null) {
		tempURL.append("#");
		tempURL.append(ref);
	    }

	    // create the fullURL now
	    fullURL = tempURL.toString();
	}

	return fullURL;
    }

    /**
     * Method which does all of the work of parsing the string.
     *
     * @param	url	the URL string to parse
     */
    protected void parseString(String url) {
	// initialize everything in case called from subclass
	// (URLName really should be a final class)
	protocol = file = ref = host = username = password = null;
	port = -1;

	int len = url.length();

	// find the protocol
	// XXX - should check for only legal characters before the colon
	// (legal: a-z, A-Z, 0-9, "+", ".", "-")
	int protocolEnd = url.indexOf(':');
        if (protocolEnd != -1)
	    protocol = url.substring(0, protocolEnd);

	// is this an Internet standard URL that contains a host name?
	if (url.regionMatches(protocolEnd + 1, "//", 0, 2)) {
	    // find where the file starts
	    String fullhost = null;
	    int fileStart = url.indexOf('/', protocolEnd + 3);
	    if (fileStart != -1) {
		fullhost = url.substring(protocolEnd + 3, fileStart);
		if (fileStart + 1 < len)
		    file = url.substring(fileStart + 1);
		else
		    file = "";
	    } else
		fullhost = url.substring(protocolEnd + 3);

	    // examine the fullhost, for username password etc.
	    int i = fullhost.indexOf('@');
	    if (i != -1) {
		String fulluserpass = fullhost.substring(0, i);
		fullhost = fullhost.substring(i + 1);

		// get user and password
		int passindex = fulluserpass.indexOf(':');
		if (passindex != -1) {
		    username = fulluserpass.substring(0, passindex);
		    password = fulluserpass.substring(passindex + 1);
		} else {
		    username = fulluserpass;
		}
	    }
	    
	    // get the port (if there)
	    int portindex;
	    if (fullhost.length() > 0 && fullhost.charAt(0) == '[') {
		// an IPv6 address?
		portindex = fullhost.indexOf(':', fullhost.indexOf(']'));
	    } else {
		portindex = fullhost.indexOf(':');
	    }
	    if (portindex != -1) {
		String portstring = fullhost.substring(portindex + 1);
		if (portstring.length() > 0) {
		    try {
			port = Integer.parseInt(portstring);
		    } catch (NumberFormatException nfex) {
			port = -1;
		    }
		}
		
		host = fullhost.substring(0, portindex);
	    } else {
		host = fullhost;
	    }
	} else {
	    if (protocolEnd + 1 < len)
		file = url.substring(protocolEnd + 1);
	}

	// extract the reference from the file name, if any
	int refStart;
	if (file != null && (refStart = file.indexOf('#')) != -1) {
	    ref = file.substring(refStart + 1);
	    file = file.substring(0, refStart);
	}
    }
    
    /**
     * Returns the port number of this URLName.
     * Returns -1 if the port is not set. 
     *
     * @return	the port number
     */
    public int getPort() {
	return port;
    }

    /**
     * Returns the protocol of this URLName.
     * Returns null if this URLName has no protocol.
     *
     * @return	the protocol
     */
    public String getProtocol() {
	return protocol;
    }

    /**
     * Returns the file name of this URLName.
     * Returns null if this URLName has no file name.
     *
     * @return	the file name of this URLName
     */
    public String getFile() {
	return file;
    }

    /**
     * Returns the reference of this URLName.
     * Returns null if this URLName has no reference.
     *
     * @return	the reference part of the URLName
     */
    public String getRef() {
	return ref;
    }

    /**
     * Returns the host of this URLName.
     * Returns null if this URLName has no host.
     *
     * @return	the host name
     */
    public String getHost() {
	return host;
    }

    /**
     * Returns the user name of this URLName.
     * Returns null if this URLName has no user name.
     *
     * @return	the user name
     */
    public String getUsername() {
	return doEncode ? decode(username) : username;
    }

    /**
     * Returns the password of this URLName.
     * Returns null if this URLName has no password.
     *
     * @return	the password
     */
    public String getPassword() {
	return doEncode ? decode(password) : password;
    }

    /**
     * Constructs a URL from the URLName.
     *
     * @return	the URL
     * @exception	MalformedURLException if the URL is malformed
     */
    public URL getURL() throws MalformedURLException {
	// URL expects the file to include the separating "/"
	String f = getFile();
	if (f == null)
	    f = "";
	else
	    f = "/" + f;
        return new URL(getProtocol(), getHost(), getPort(), f);
    }

    /**
     * Compares two URLNames. The result is true if and only if the
     * argument is not null and is a URLName object that represents the
     * same URLName as this object. Two URLName objects are equal if
     * they have the same protocol and the same host,
     * the same port number on the host, the same username,
     * and the same file on the host. The fields (host, username,
     * file) are also considered the same if they are both
     * null.  <p>
     *
     * Hosts are considered equal if the names are equal (case independent)
     * or if host name lookups for them both succeed and they both reference
     * the same IP address. <p>
     *
     * Note that URLName has no knowledge of default port numbers for
     * particular protocols, so "imap://host" and "imap://host:143"
     * would not compare as equal. <p>
     *
     * Note also that the password field is not included in the comparison,
     * nor is any reference field appended to the filename.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof URLName))
	    return false;
	URLName u2 = (URLName)obj;

	// compare protocols
	if (!(protocol == u2.protocol ||
		(protocol != null && protocol.equals(u2.protocol))))
	    return false;

	// compare hosts
	InetAddress a1 = getHostAddress(), a2 = u2.getHostAddress();
	// if we have internet address for both, and they're not the same, fail
	if (a1 != null && a2 != null) {
	    if (!a1.equals(a2))
		return false;
	// else, if we have host names for both, and they're not the same, fail
	} else if (host != null && u2.host != null) {
	    if (!host.equalsIgnoreCase(u2.host))
		return false;
	// else, if not both null
	} else if (host != u2.host) {
	    return false;
	}
	// at this point, hosts match

	// compare usernames
	if (!(username == u2.username ||
		(username != null && username.equals(u2.username))))
	    return false;

	// Forget about password since it doesn't
	// really denote a different store.

	// compare files
	String f1 = file == null ? "" : file;
	String f2 = u2.file == null ? "" : u2.file;

	if (!f1.equals(f2))
	    return false;

	// compare ports
	if (port != u2.port)
	    return false;

	// all comparisons succeeded, they're equal
        return true;
    }

    /**
     * Compute the hash code for this URLName.
     */
    @Override
    public int hashCode() {
	if (hashCode != 0)
	    return hashCode;
	if (protocol != null)
	    hashCode += protocol.hashCode();
	InetAddress addr = getHostAddress();
	if (addr != null)
	    hashCode += addr.hashCode();
	else if (host != null)
	    hashCode += host.toLowerCase(Locale.ENGLISH).hashCode();
	if (username != null)
	    hashCode += username.hashCode();
	if (file != null)
	    hashCode += file.hashCode();
	hashCode += port;
	return hashCode;
    }

    /**
     * Get the IP address of our host.  Look up the
     * name the first time and remember that we've done
     * so, whether the lookup fails or not.
     */
    private synchronized InetAddress getHostAddress() {
	if (hostAddressKnown)
	    return hostAddress;
	if (host == null)
	    return null;
	try {
	    hostAddress = InetAddress.getByName(host);
	} catch (UnknownHostException ex) {
	    hostAddress = null;
	}
	hostAddressKnown = true;
	return hostAddress;
    }

    /**
     * The class contains a utility method for converting a
     * <code>String</code> into a MIME format called
     * "<code>x-www-form-urlencoded</code>" format.
     * <p>
     * To convert a <code>String</code>, each character is examined in turn:
     * <ul>
     * <li>The ASCII characters '<code>a</code>' through '<code>z</code>',
     *     '<code>A</code>' through '<code>Z</code>', '<code>0</code>'
     *     through '<code>9</code>', and &quot;.&quot;, &quot;-&quot;, 
     * &quot;*&quot;, &quot;_&quot; remain the same.
     * <li>The space character '<code>&nbsp;</code>' is converted into a
     *     plus sign '<code>+</code>'.
     * <li>All other characters are converted into the 3-character string
     *     "<code>%<i>xy</i></code>", where <i>xy</i> is the two-digit
     *     hexadecimal representation of the lower 8-bits of the character.
     * </ul>
     *
     * @author  Herb Jellinek
     * @since   JDK1.0
     */
    static BitSet dontNeedEncoding;
    static final int caseDiff = ('a' - 'A');

    /* The list of characters that are not encoded have been determined by
       referencing O'Reilly's "HTML: The Definitive Guide" (page 164). */

    static {
	dontNeedEncoding = new BitSet(256);
	int i;
	for (i = 'a'; i <= 'z'; i++) {
	    dontNeedEncoding.set(i);
	}
	for (i = 'A'; i <= 'Z'; i++) {
	    dontNeedEncoding.set(i);
	}
	for (i = '0'; i <= '9'; i++) {
	    dontNeedEncoding.set(i);
	}
	/* encoding a space to a + is done in the encode() method */
	dontNeedEncoding.set(' ');
	dontNeedEncoding.set('-');
	dontNeedEncoding.set('_');
	dontNeedEncoding.set('.');
	dontNeedEncoding.set('*');
    }

    /**
     * Translates a string into <code>x-www-form-urlencoded</code> format.
     *
     * @param   s   <code>String</code> to be translated.
     * @return  the translated <code>String</code>.
     */
    static String encode(String s) {
	if (s == null)
	    return null;
	// the common case is no encoding is needed
	for (int i = 0; i < s.length(); i++) {
	    int c = (int)s.charAt(i);
	    if (c == ' ' || !dontNeedEncoding.get(c))
		return _encode(s);
	}
	return s;
    }

    private static String _encode(String s) {
	int maxBytesPerChar = 10;
        StringBuilder out = new StringBuilder(s.length());
	ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
	OutputStreamWriter writer = new OutputStreamWriter(buf);

	for (int i = 0; i < s.length(); i++) {
	    int c = (int)s.charAt(i);
	    if (dontNeedEncoding.get(c)) {
		if (c == ' ') {
		    c = '+';
		}
		out.append((char)c);
	    } else {
		// convert to external encoding before hex conversion
		try {
		    writer.write(c);
		    writer.flush();
		} catch(IOException e) {
		    buf.reset();
		    continue;
		}
		byte[] ba = buf.toByteArray();
		for (int j = 0; j < ba.length; j++) {
		    out.append('%');
		    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
		    // converting to use uppercase letter as part of
		    // the hex value if ch is a letter.
		    if (Character.isLetter(ch)) {
			ch -= caseDiff;
		    }
		    out.append(ch);
		    ch = Character.forDigit(ba[j] & 0xF, 16);
		    if (Character.isLetter(ch)) {
			ch -= caseDiff;
		    }
		    out.append(ch);
		}
		buf.reset();
	    }
	}

	return out.toString();
    }


    /**
     * The class contains a utility method for converting from
     * a MIME format called "<code>x-www-form-urlencoded</code>"
     * to a <code>String</code>
     * <p>
     * To convert to a <code>String</code>, each character is examined in turn:
     * <ul>
     * <li>The ASCII characters '<code>a</code>' through '<code>z</code>',
     * '<code>A</code>' through '<code>Z</code>', and '<code>0</code>'
     * through '<code>9</code>' remain the same.
     * <li>The plus sign '<code>+</code>'is converted into a
     * space character '<code>&nbsp;</code>'.
     * <li>The remaining characters are represented by 3-character
     * strings which begin with the percent sign,
     * "<code>%<i>xy</i></code>", where <i>xy</i> is the two-digit
     * hexadecimal representation of the lower 8-bits of the character.
     * </ul>
     *
     * @author  Mark Chamness
     * @author  Michael McCloskey
     * @since   1.2
     */

    /**
     * Decodes a &quot;x-www-form-urlencoded&quot; 
     * to a <code>String</code>.
     * @param s the <code>String</code> to decode
     * @return the newly decoded <code>String</code>
     */
    static String decode(String s) {
	if (s == null)
	    return null;
	if (indexOfAny(s, "+%") == -1)
	    return s;		// the common case

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '+':
                    sb.append(' ');
                    break;
                case '%':
                    try {
                        sb.append((char)Integer.parseInt(
                                        s.substring(i+1,i+3),16));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
			    "Illegal URL encoded value: " +
			    s.substring(i,i+3));
                    }
                    i += 2;
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        // Undo conversion to external encoding
        String result = sb.toString();
        try {
            byte[] inputBytes = result.getBytes("8859_1");
            result = new String(inputBytes);
        } catch (UnsupportedEncodingException e) {
            // The system should always have 8859_1
        }
        return result;
    }

    /**
     * Return the first index of any of the characters in "any" in "s",
     * or -1 if none are found.
     *
     * This should be a method on String.
     */
    private static int indexOfAny(String s, String any) {
	return indexOfAny(s, any, 0);
    }

    private static int indexOfAny(String s, String any, int start) {
	try {
	    int len = s.length();
	    for (int i = start; i < len; i++) {
		if (any.indexOf(s.charAt(i)) >= 0)
		    return i;
	    }
	    return -1;
	} catch (StringIndexOutOfBoundsException e) {
	    return -1;
	}
    }

    /*
    // Do not remove, this is needed when testing new URL cases
    public static void main(String[] argv) {
	String [] testURLNames = {
	    "protocol://userid:password@host:119/file",
	    "http://funny/folder/file.html",
	    "http://funny/folder/file.html#ref",
	    "http://funny/folder/file.html#",
	    "http://funny/#ref",
	    "imap://jmr:secret@labyrinth//var/mail/jmr",
	    "nntp://fred@labyrinth:143/save/it/now.mbox",
	    "imap://jmr@labyrinth/INBOX",
	    "imap://labryrinth",
	    "imap://labryrinth/",
	    "file:",
	    "file:INBOX",
	    "file:/home/shannon/mail/foo",
	    "/tmp/foo",
	    "//host/tmp/foo",
	    ":/tmp/foo",
	    "/really/weird:/tmp/foo#bar",
	    ""
	};

	URLName url =
	    new URLName("protocol", "host", 119, "file", "userid", "password");
	System.out.println("Test URL: " + url.toString());
	if (argv.length == 0) {
	    for (int i = 0; i < testURLNames.length; i++) {
		print(testURLNames[i]);
		System.out.println();
	    }
	} else {
	    for (int i = 0; i < argv.length; i++) {
		print(argv[i]);
		System.out.println();
	    }
	    if (argv.length == 2) {
		URLName u1 = new URLName(argv[0]);
		URLName u2 = new URLName(argv[1]);
		System.out.println("URL1 hash code: " + u1.hashCode());
		System.out.println("URL2 hash code: " + u2.hashCode());
		if (u1.equals(u2))
		    System.out.println("success, equal");
		else
		    System.out.println("fail, not equal");
		if (u2.equals(u1))
		    System.out.println("success, equal");
		else
		    System.out.println("fail, not equal");
		if (u1.hashCode() == u2.hashCode())
		    System.out.println("success, hashCodes equal");
		else
		    System.out.println("fail, hashCodes not equal");
	    }
	}
    }

    private static void print(String name) {
	URLName url = new URLName(name);
	System.out.println("Original URL: " + name);
	System.out.println("The fullUrl : " + url.toString());
	if (!name.equals(url.toString()))
	    System.out.println("            : NOT EQUAL!");
	System.out.println("The protocol is: " + url.getProtocol());
	System.out.println("The host is: " + url.getHost());
	System.out.println("The port is: " + url.getPort());
	System.out.println("The user is: " + url.getUsername());
	System.out.println("The password is: " + url.getPassword());
	System.out.println("The file is: " + url.getFile());
	System.out.println("The ref is: " + url.getRef());
    }
    */
}
