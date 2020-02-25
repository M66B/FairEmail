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

package com.sun.mail.smtp;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.security.*;
import java.nio.charset.StandardCharsets;

import com.sun.mail.util.MailLogger;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.BASE64DecoderStream;

/**
 * DIGEST-MD5 authentication support.
 *
 * @author Dean Gibson
 * @author Bill Shannon
 */

public class DigestMD5 {

    private MailLogger logger;
    private MessageDigest md5;
    private String uri;
    private String clientResponse;

    public DigestMD5(MailLogger logger) {
	this.logger = logger.getLogger(this.getClass(), "DEBUG DIGEST-MD5");
	logger.config("DIGEST-MD5 Loaded");
    }

    /**
     * Return client's authentication response to server's challenge.
     *
     * @param	host	the host name
     * @param	user	the user name
     * @param	passwd	the user's password
     * @param	realm	the security realm
     * @param	serverChallenge	the challenge from the server
     * @return byte array with client's response
     * @exception	IOException	for I/O errors
     */
    public byte[] authClient(String host, String user, String passwd,
				String realm, String serverChallenge)
				throws IOException {
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);
	SecureRandom random;
	try {
	    //random = SecureRandom.getInstance("SHA1PRNG");
	    random = new SecureRandom();
	    md5 = MessageDigest.getInstance("MD5");
	} catch (NoSuchAlgorithmException ex) {
	    logger.log(Level.FINE, "NoSuchAlgorithmException", ex);
	    throw new IOException(ex.toString());
	}
	StringBuilder result = new StringBuilder();

	uri = "smtp/" + host;
	String nc = "00000001";
	String qop = "auth";
	byte[] bytes = new byte[32];	// arbitrary size ...
	int resp;

	logger.fine("Begin authentication ...");

	// Code based on http://www.ietf.org/rfc/rfc2831.txt
	Map<String, String> map = tokenize(serverChallenge);

	if (realm == null) {
	    String text = map.get("realm");
	    realm = text != null ? new StringTokenizer(text, ",").nextToken()
				 : host;
	}

	// server challenge random value
	String nonce = map.get("nonce");

	// Does server support UTF-8 usernames and passwords?
	String charset = map.get("charset");
	boolean utf8 = charset != null && charset.equalsIgnoreCase("utf-8");

	random.nextBytes(bytes);
	b64os.write(bytes);
	b64os.flush();

	// client challenge random value
	String cnonce = bos.toString("iso-8859-1");	// really ASCII?
	bos.reset();

	// DIGEST-MD5 computation, common portion (order critical)
	if (utf8) {
	    String up = user + ":" + realm + ":" + passwd;
	    md5.update(md5.digest(up.getBytes(StandardCharsets.UTF_8)));
	} else
	    md5.update(md5.digest(
		ASCIIUtility.getBytes(user + ":" + realm + ":" + passwd)));
	md5.update(ASCIIUtility.getBytes(":" + nonce + ":" + cnonce));
	clientResponse = toHex(md5.digest())
		+ ":" + nonce  + ":" + nc + ":" + cnonce + ":" + qop + ":";
	
	// DIGEST-MD5 computation, client response (order critical)
	md5.update(ASCIIUtility.getBytes("AUTHENTICATE:" + uri));
	md5.update(ASCIIUtility.getBytes(clientResponse + toHex(md5.digest())));

	// build response text (order not critical)
	result.append("username=\"" + user + "\"");
	result.append(",realm=\"" + realm + "\"");
	result.append(",qop=" + qop);
	result.append(",nc=" + nc);
	result.append(",nonce=\"" + nonce + "\"");
	result.append(",cnonce=\"" + cnonce + "\"");
	result.append(",digest-uri=\"" + uri + "\"");
	if (utf8)
	    result.append(",charset=\"utf-8\"");
	result.append(",response=" + toHex(md5.digest()));

	if (logger.isLoggable(Level.FINE))
	    logger.fine("Response => " + result.toString());
	b64os.write(ASCIIUtility.getBytes(result.toString()));
	b64os.flush();
	return bos.toByteArray();
    }

    /**
     * Allow the client to authenticate the server based on its
     * response.
     *
     * @param	serverResponse	the response that was received from the server
     * @return	true if server is authenticated
     * @exception	IOException	for character conversion failures
     */
    public boolean authServer(String serverResponse) throws IOException {
	Map<String, String> map = tokenize(serverResponse);
	// DIGEST-MD5 computation, server response (order critical)
	md5.update(ASCIIUtility.getBytes(":" + uri));
	md5.update(ASCIIUtility.getBytes(clientResponse + toHex(md5.digest())));
	String text = toHex(md5.digest());
	if (!text.equals(map.get("rspauth"))) {
	    if (logger.isLoggable(Level.FINE))
		logger.fine("Expected => rspauth=" + text);
	    return false;	// server NOT authenticated by client !!!
	}
	return true;
    }

    /**
     * Tokenize a response from the server.
     *
     * @return	Map containing key/value pairs from server
     */
    @SuppressWarnings("fallthrough")
    private Map<String, String> tokenize(String serverResponse)
	    throws IOException {
	Map<String, String> map	= new HashMap<>();
	byte[] bytes = serverResponse.getBytes("iso-8859-1");	// really ASCII?
	String key = null;
	int ttype;
	StreamTokenizer	tokens
		= new StreamTokenizer(
		    new InputStreamReader(
		      new BASE64DecoderStream(
			new ByteArrayInputStream(bytes, 4, bytes.length - 4)
		      ), "iso-8859-1"	// really ASCII?
		    )
		  );

	tokens.ordinaryChars('0', '9');	// reset digits
	tokens.wordChars('0', '9');	// digits may start words
	while ((ttype = tokens.nextToken()) != StreamTokenizer.TT_EOF) {
	    switch (ttype) {
	    case StreamTokenizer.TT_WORD:
		if (key == null) {
		    key = tokens.sval;
		    break;
		}
		// fall-thru
	    case '"':
		if (logger.isLoggable(Level.FINE))
		    logger.fine("Received => " +
			 	 key + "='" + tokens.sval + "'");
		if (map.containsKey(key)) {  // concatenate multiple values
		    map.put(key, map.get(key) + "," + tokens.sval);
		} else {
		    map.put(key, tokens.sval);
		}
		key = null;
		break;
	    default:	// XXX - should never happen?
		break;
	    }
	}
	return map;
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
}
