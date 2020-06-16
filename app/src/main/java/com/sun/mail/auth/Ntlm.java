/*
 * Copyright (c) 2005, 2019 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Copied from OpenJDK with permission.
 */

package com.sun.mail.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.MailLogger;


/**
 * NTLMAuthentication:
 *
 * @author Michael McMahon
 * @author Bill Shannon (adapted for Jakarta Mail)
 */
public class Ntlm {

    private byte[] type1;
    private byte[] type3;

    private SecretKeyFactory fac;
    private Cipher cipher;
    private MD4 md4;
    private String hostname;
    private String ntdomain;
    private String username;
    private String password;

    private Mac hmac;

    private MailLogger logger;

    // NTLM flags, as defined in Microsoft NTLM spec
    // https://msdn.microsoft.com/en-us/library/cc236621.aspx
    private static final int NTLMSSP_NEGOTIATE_UNICODE	= 0x00000001;
    private static final int NTLMSSP_NEGOTIATE_OEM	= 0x00000002;
    private static final int NTLMSSP_REQUEST_TARGET	= 0x00000004;
    private static final int NTLMSSP_NEGOTIATE_SIGN	= 0x00000010;
    private static final int NTLMSSP_NEGOTIATE_SEAL	= 0x00000020;
    private static final int NTLMSSP_NEGOTIATE_DATAGRAM	= 0x00000040;
    private static final int NTLMSSP_NEGOTIATE_LM_KEY	= 0x00000080;
    private static final int NTLMSSP_NEGOTIATE_NTLM	= 0x00000200;
    private static final int NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED	= 0x00001000;
    private static final int NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED	= 0x00002000;
    private static final int NTLMSSP_NEGOTIATE_ALWAYS_SIGN	= 0x00008000;
    private static final int NTLMSSP_TARGET_TYPE_DOMAIN	= 0x00010000;
    private static final int NTLMSSP_TARGET_TYPE_SERVER	= 0x00020000;
    private static final int NTLMSSP_NEGOTIATE_EXTENDED_SESSIONSECURITY	= 0x00080000;
    private static final int NTLMSSP_NEGOTIATE_IDENTIFY	= 0x00100000;
    private static final int NTLMSSP_REQUEST_NON_NT_SESSION_KEY	= 0x00400000;
    private static final int NTLMSSP_NEGOTIATE_TARGET_INFO	= 0x00800000;
    private static final int NTLMSSP_NEGOTIATE_VERSION	= 0x02000000;
    private static final int NTLMSSP_NEGOTIATE_128	= 0x20000000;
    private static final int NTLMSSP_NEGOTIATE_KEY_EXCH	= 0x40000000;
    private static final int NTLMSSP_NEGOTIATE_56	= 0x80000000;

    private static final byte RESPONSERVERSION = 1;
    private static final byte HIRESPONSERVERSION = 1;
    private static final byte[] Z6 = new byte[] { 0, 0, 0, 0, 0, 0 };
    private static final byte[] Z4 = new byte[] { 0, 0, 0, 0 };

    private void init0() {
        type1 = new byte[256];	// hopefully large enough
        type3 = new byte[512];	// ditto
        System.arraycopy(new byte[] {'N','T','L','M','S','S','P',0,1}, 0,
			    type1, 0, 9);
        System.arraycopy(new byte[] {'N','T','L','M','S','S','P',0,3}, 0,
			    type3, 0, 9);

        try {
            fac = SecretKeyFactory.getInstance("DES");
            cipher = Cipher.getInstance("DES/ECB/NoPadding");
            md4 = new MD4();
        } catch (NoSuchPaddingException e) {
            assert false;
        } catch (NoSuchAlgorithmException e) {
            assert false;
        }
    };

    /**
     * Create an NTLM authenticator.
     * Username may be specified as domain\\username in the Authenticator.
     * If this notation is not used, then the domain will be taken
     * from the ntdomain parameter.
     *
     * @param	ntdomain	the NT domain
     * @param	hostname	the host name
     * @param	username	the user name
     * @param	password	the password
     * @param	logger		the MailLogger
     */
    public Ntlm(String ntdomain, String hostname, String username,
				String password, MailLogger logger) {
	int i = hostname.indexOf('.');
	if (i != -1) {
	    hostname = hostname.substring(0, i);
	}
        i = username.indexOf('\\');
        if (i != -1) {
            ntdomain = username.substring(0, i).toUpperCase(Locale.ENGLISH);
            username = username.substring(i+1);
        } else if (ntdomain == null) {
	    ntdomain = "";
	}
	this.ntdomain = ntdomain;
	this.hostname = hostname;
	this.username = username;
	this.password = password;
	this.logger = logger.getLogger(this.getClass(), "DEBUG NTLM");
        init0();
    }

    private void copybytes(byte[] dest, int destpos, String src, String enc) {
        try {
            byte[] x = src.getBytes(enc);
            System.arraycopy(x, 0, dest, destpos, x.length);
        } catch (UnsupportedEncodingException e) {
            assert false;
        }
    }

    // for compatibility, just in case
    public String generateType1Msg(int flags) {
	return generateType1Msg(flags, false);
    }

    public String generateType1Msg(int flags, boolean v2) {
        int dlen = ntdomain.length();
	int type1flags = 
		NTLMSSP_NEGOTIATE_UNICODE |
		NTLMSSP_NEGOTIATE_OEM |
		NTLMSSP_NEGOTIATE_NTLM |
		NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED |
		NTLMSSP_NEGOTIATE_ALWAYS_SIGN |
		flags;
	if (dlen != 0)
	    type1flags |= NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED;
	if (v2)
	    type1flags |= NTLMSSP_NEGOTIATE_EXTENDED_SESSIONSECURITY;
	writeInt(type1, 12, type1flags);
        type1[28] = (byte) 0x20;	// host name offset
	writeShort(type1, 16, dlen);
	writeShort(type1, 18, dlen);

        int hlen = hostname.length();
	writeShort(type1, 24, hlen);
	writeShort(type1, 26, hlen);

        copybytes(type1, 32, hostname, "iso-8859-1");
        copybytes(type1, hlen+32, ntdomain, "iso-8859-1");
	writeInt(type1, 20, hlen+32);

        byte[] msg = new byte[32 + hlen + dlen];
        System.arraycopy(type1, 0, msg, 0, 32 + hlen + dlen);
	if (logger.isLoggable(Level.FINE))
	    logger.fine("type 1 message: " + toHex(msg));

        String result = null;
	try {
	    result = new String(BASE64EncoderStream.encode(msg), "iso-8859-1");
        } catch (UnsupportedEncodingException e) {
            assert false;
        }
        return result;
    }

    /**
     * Convert a 7 byte array to an 8 byte array (for a des key with parity).
     * Input starts at offset off.
     */
    private byte[] makeDesKey(byte[] input, int off) {
        int[] in = new int[input.length];
        for (int i = 0; i < in.length; i++) {
            in[i] = input[i] < 0 ? input[i] + 256: input[i];
        }
        byte[] out = new byte[8];
        out[0] = (byte)in[off+0];
        out[1] = (byte)(((in[off+0] << 7) & 0xFF) | (in[off+1] >> 1));
        out[2] = (byte)(((in[off+1] << 6) & 0xFF) | (in[off+2] >> 2));
        out[3] = (byte)(((in[off+2] << 5) & 0xFF) | (in[off+3] >> 3));
        out[4] = (byte)(((in[off+3] << 4) & 0xFF) | (in[off+4] >> 4));
        out[5] = (byte)(((in[off+4] << 3) & 0xFF) | (in[off+5] >> 5));
        out[6] = (byte)(((in[off+5] << 2) & 0xFF) | (in[off+6] >> 6));
        out[7] = (byte)((in[off+6] << 1) & 0xFF);
        return out;
    }

    /**
     * Compute hash-based message authentication code for NTLMv2.
     */
    private byte[] hmacMD5(byte[] key, byte[] text) {
	try {
	    if (hmac == null)
		hmac = Mac.getInstance("HmacMD5");
	} catch (NoSuchAlgorithmException ex) {
	    throw new AssertionError();
	}
	try {
	    byte[] nk = new byte[16];
	    System.arraycopy(key, 0, nk, 0, key.length > 16 ? 16 : key.length);
	    SecretKeySpec skey = new SecretKeySpec(nk, "HmacMD5");
	    hmac.init(skey);
	    return hmac.doFinal(text);
	} catch (InvalidKeyException ex) {
	    assert false;
	} catch (RuntimeException e) {
	    assert false;
	}
	return null;
    }

    private byte[] calcLMHash() throws GeneralSecurityException {
        byte[] magic = {0x4b, 0x47, 0x53, 0x21, 0x40, 0x23, 0x24, 0x25};
        byte[] pwb = null;
	try {
	    pwb = password.toUpperCase(Locale.ENGLISH).getBytes("iso-8859-1");
	} catch (UnsupportedEncodingException ex) {
	    // should never happen
	    assert false;
	}
        byte[] pwb1 = new byte[14];
        int len = password.length();
        if (len > 14)
            len = 14;
        System.arraycopy(pwb, 0, pwb1, 0, len); /* Zero padded */

        DESKeySpec dks1 = new DESKeySpec(makeDesKey(pwb1, 0));
        DESKeySpec dks2 = new DESKeySpec(makeDesKey(pwb1, 7));

        SecretKey key1 = fac.generateSecret(dks1);
        SecretKey key2 = fac.generateSecret(dks2);
        cipher.init(Cipher.ENCRYPT_MODE, key1);
        byte[] out1 = cipher.doFinal(magic, 0, 8);
        cipher.init(Cipher.ENCRYPT_MODE, key2);
        byte[] out2 = cipher.doFinal(magic, 0, 8);

        byte[] result = new byte [21];
        System.arraycopy(out1, 0, result, 0, 8);
        System.arraycopy(out2, 0, result, 8, 8);
        return result;
    }

    private byte[] calcNTHash() throws GeneralSecurityException {
        byte[] pw = null;
        try {
            pw = password.getBytes("UnicodeLittleUnmarked");
        } catch (UnsupportedEncodingException e) {
            assert false;
        }
        byte[] out = md4.digest(pw);
        byte[] result = new byte[21];
        System.arraycopy(out, 0, result, 0, 16);
        return result;
    }

    /*
     * Key is a 21 byte array.  Split it into 3 7 byte chunks,
     * convert each to 8 byte DES keys, encrypt the text arg with
     * each key and return the three results in a sequential [].
     */
    private byte[] calcResponse(byte[] key, byte[] text)
				throws GeneralSecurityException {
        assert key.length == 21;
        DESKeySpec dks1 = new DESKeySpec(makeDesKey(key, 0));
        DESKeySpec dks2 = new DESKeySpec(makeDesKey(key, 7));
        DESKeySpec dks3 = new DESKeySpec(makeDesKey(key, 14));
        SecretKey key1 = fac.generateSecret(dks1);
        SecretKey key2 = fac.generateSecret(dks2);
        SecretKey key3 = fac.generateSecret(dks3);
        cipher.init(Cipher.ENCRYPT_MODE, key1);
        byte[] out1 = cipher.doFinal(text, 0, 8);
        cipher.init(Cipher.ENCRYPT_MODE, key2);
        byte[] out2 = cipher.doFinal(text, 0, 8);
        cipher.init(Cipher.ENCRYPT_MODE, key3);
        byte[] out3 = cipher.doFinal(text, 0, 8);
        byte[] result = new byte[24];
        System.arraycopy(out1, 0, result, 0, 8);
        System.arraycopy(out2, 0, result, 8, 8);
        System.arraycopy(out3, 0, result, 16, 8);
        return result;
    }

    /*
     * Calculate the NTLMv2 response based on the nthash, additional data,
     * and the original challenge.
     */
    private byte[] calcV2Response(byte[] nthash, byte[] blob, byte[] challenge)
				throws GeneralSecurityException {
        byte[] txt = null;
	try {
	    txt = (username.toUpperCase(Locale.ENGLISH) + ntdomain).
			    getBytes("UnicodeLittleUnmarked");
	} catch (UnsupportedEncodingException ex) {
	    // should never happen
	    assert false;
	}
	byte[] ntlmv2hash = hmacMD5(nthash, txt);
	byte[] cb = new byte[blob.length + 8];
	System.arraycopy(challenge, 0, cb, 0, 8);
	System.arraycopy(blob, 0, cb, 8, blob.length);
	byte[] result = new byte[blob.length + 16];
	System.arraycopy(hmacMD5(ntlmv2hash, cb), 0, result, 0, 16);
	System.arraycopy(blob, 0, result, 16, blob.length);
	return result;
    }

    public String generateType3Msg(String type2msg) {
	try {

	/* First decode the type2 message to get the server challenge */
	/* challenge is located at type2[24] for 8 bytes */
	byte[] type2 = null;
	try {
	    type2 = BASE64DecoderStream.decode(type2msg.getBytes("us-ascii"));
	} catch (UnsupportedEncodingException ex) {
	    // should never happen
	    assert false;
	}
	if (logger.isLoggable(Level.FINE))
	    logger.fine("type 2 message: " + toHex(type2));

        byte[] challenge = new byte[8];
        System.arraycopy(type2, 24, challenge, 0, 8);

	int type3flags = 
		NTLMSSP_NEGOTIATE_UNICODE |
		NTLMSSP_NEGOTIATE_NTLM |
		NTLMSSP_NEGOTIATE_ALWAYS_SIGN;

        int ulen = username.length()*2;
	writeShort(type3, 36, ulen);
	writeShort(type3, 38, ulen);
        int dlen = ntdomain.length()*2;
	writeShort(type3, 28, dlen);
	writeShort(type3, 30, dlen);
        int hlen = hostname.length()*2;
	writeShort(type3, 44, hlen);
	writeShort(type3, 46, hlen);

        int l = 64;
        copybytes(type3, l, ntdomain, "UnicodeLittleUnmarked");
	writeInt(type3, 32, l);
        l += dlen;
        copybytes(type3, l, username, "UnicodeLittleUnmarked");
	writeInt(type3, 40, l);
        l += ulen;
        copybytes(type3, l, hostname, "UnicodeLittleUnmarked");
	writeInt(type3, 48, l);
        l += hlen;

        byte[] msg = null;
	byte[] lmresponse = null;
	byte[] ntresponse = null;
	int flags = readInt(type2, 20);

	// did the server agree to NTLMv2?
	if ((flags & NTLMSSP_NEGOTIATE_EXTENDED_SESSIONSECURITY) != 0) {
	    // yes, create an NTLMv2 response
	    logger.fine("Using NTLMv2");
	    type3flags |= NTLMSSP_NEGOTIATE_EXTENDED_SESSIONSECURITY;
	    byte[] nonce = new byte[8];
	    // XXX - allow user to specify Random instance via properties?
	    (new Random()).nextBytes(nonce);
	    byte[] nthash = calcNTHash();
	    lmresponse = calcV2Response(nthash, nonce, challenge);
	    byte[] targetInfo = new byte[0];
	    if ((flags & NTLMSSP_NEGOTIATE_TARGET_INFO) != 0) {
		int tlen = readShort(type2, 40);
		int toff = readInt(type2, 44);
		targetInfo = new byte[tlen];
		System.arraycopy(type2, toff, targetInfo, 0, tlen);
	    }
	    byte[] blob = new byte[32 + targetInfo.length];
	    blob[0] = RESPONSERVERSION;
	    blob[1] = HIRESPONSERVERSION;
	    System.arraycopy(Z6, 0, blob, 2, 6);
	    // convert time to NT format
	    long now = (System.currentTimeMillis() + 11644473600000L) * 10000L;
	    for (int i = 0; i < 8; i++) {
		blob[8 + i] = (byte)(now & 0xff);
		now >>= 8;
	    }
	    System.arraycopy(nonce, 0, blob, 16, 8);
	    System.arraycopy(Z4, 0, blob, 24, 4);
	    System.arraycopy(targetInfo, 0, blob, 28, targetInfo.length);
	    System.arraycopy(Z4, 0, blob, 28 + targetInfo.length, 4);
	    ntresponse = calcV2Response(nthash, blob, challenge);
	} else {
	    byte[] lmhash = calcLMHash();
	    lmresponse = calcResponse(lmhash, challenge);
	    byte[] nthash = calcNTHash();
	    ntresponse = calcResponse(nthash, challenge);
	}
	System.arraycopy(lmresponse, 0, type3, l, lmresponse.length);
	writeShort(type3, 12, lmresponse.length);
	writeShort(type3, 14, lmresponse.length);
	writeInt(type3, 16, l);
	l += 24;
	System.arraycopy(ntresponse, 0, type3, l, ntresponse.length);
	writeShort(type3, 20, ntresponse.length);
	writeShort(type3, 22, ntresponse.length);
	writeInt(type3, 24, l);
	l += ntresponse.length;
	writeShort(type3, 56, l);

	msg = new byte[l];
	System.arraycopy(type3, 0, msg, 0, l);

	writeInt(type3, 60, type3flags);

	if (logger.isLoggable(Level.FINE))
	    logger.fine("type 3 message: " + toHex(msg));

        String result = null;
	try {
	    result = new String(BASE64EncoderStream.encode(msg), "iso-8859-1");
        } catch (UnsupportedEncodingException e) {
            assert false;
        }
        return result;

	} catch (GeneralSecurityException ex) {
	    // should never happen
	    logger.log(Level.FINE, "GeneralSecurityException", ex);
	    return "";	// will fail later
	}
    }

    private static int readShort(byte[] b, int off) {
	return (((int)b[off]) & 0xff) |
	    ((((int)b[off+1]) & 0xff) << 8);
    }

    private void writeShort(byte[] b, int off, int data) {
        b[off] = (byte) (data & 0xff);
        b[off+1] = (byte) ((data >> 8) & 0xff);
    }

    private static int readInt(byte[] b, int off) {
	return (((int)b[off]) & 0xff) |
	    ((((int)b[off+1]) & 0xff) << 8) |
	    ((((int)b[off+2]) & 0xff) << 16) |
	    ((((int)b[off+3]) & 0xff) << 24);
    }

    private void writeInt(byte[] b, int off, int data) {
        b[off] = (byte) (data & 0xff);
        b[off+1] = (byte) ((data >> 8) & 0xff);
        b[off+2] = (byte) ((data >> 16) & 0xff);
        b[off+3] = (byte) ((data >> 24) & 0xff);
    }

    private static char[] hex =
	{ '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };

    private static String toHex(byte[] b) {
	StringBuilder sb = new StringBuilder(b.length * 3);
	for (int i = 0; i < b.length; i++)
	    sb.append(hex[(b[i]>>4)&0xF]).append(hex[b[i]&0xF]).append(' ');
	return sb.toString();
    }
}
