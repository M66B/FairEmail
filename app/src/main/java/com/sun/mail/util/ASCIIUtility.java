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

package com.sun.mail.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class ASCIIUtility {

    // Private constructor so that this class is not instantiated
    private ASCIIUtility() { }
	
    /**
     * Convert the bytes within the specified range of the given byte 
     * array into a signed integer in the given radix . The range extends 
     * from <code>start</code> till, but not including <code>end</code>. <p>
     *
     * Based on java.lang.Integer.parseInt()
     *
     * @param	b	the bytes
     * @param	start	the first byte offset
     * @param	end	the last byte offset
     * @param	radix	the radix
     * @return		the integer value
     * @exception	NumberFormatException	for conversion errors
     */
    public static int parseInt(byte[] b, int start, int end, int radix)
		throws NumberFormatException {
	if (b == null)
	    throw new NumberFormatException("null");
	
	int result = 0;
	boolean negative = false;
	int i = start;
	int limit;
	int multmin;
	int digit;

	if (end > start) {
	    if (b[i] == '-') {
		negative = true;
		limit = Integer.MIN_VALUE;
		i++;
	    } else {
		limit = -Integer.MAX_VALUE;
	    }
	    multmin = limit / radix;
	    if (i < end) {
		digit = Character.digit((char)b[i++], radix);
		if (digit < 0) {
		    throw new NumberFormatException(
			"illegal number: " + toString(b, start, end)
			);
		} else {
		    result = -digit;
		}
	    }
	    while (i < end) {
		// Accumulating negatively avoids surprises near MAX_VALUE
		digit = Character.digit((char)b[i++], radix);
		if (digit < 0) {
		    throw new NumberFormatException("illegal number");
		}
		if (result < multmin) {
		    throw new NumberFormatException("illegal number");
		}
		result *= radix;
		if (result < limit + digit) {
		    throw new NumberFormatException("illegal number");
		}
		result -= digit;
	    }
	} else {
	    throw new NumberFormatException("illegal number");
	}
	if (negative) {
	    if (i > start + 1) {
		return result;
	    } else {	/* Only got "-" */
		throw new NumberFormatException("illegal number");
	    }
	} else {
	    return -result;
	}
    }

    /**
     * Convert the bytes within the specified range of the given byte 
     * array into a signed integer . The range extends from 
     * <code>start</code> till, but not including <code>end</code>.
     *
     * @param	b	the bytes
     * @param	start	the first byte offset
     * @param	end	the last byte offset
     * @return		the integer value
     * @exception	NumberFormatException	for conversion errors
     */
    public static int parseInt(byte[] b, int start, int end)
		throws NumberFormatException {
	return parseInt(b, start, end, 10);
    }

    /**
     * Convert the bytes within the specified range of the given byte 
     * array into a signed long in the given radix . The range extends 
     * from <code>start</code> till, but not including <code>end</code>. <p>
     *
     * Based on java.lang.Long.parseLong()
     *
     * @param	b	the bytes
     * @param	start	the first byte offset
     * @param	end	the last byte offset
     * @param	radix	the radix
     * @return		the long value
     * @exception	NumberFormatException	for conversion errors
     */
    public static long parseLong(byte[] b, int start, int end, int radix)
		throws NumberFormatException {
	if (b == null)
	    throw new NumberFormatException("null");
	
	long result = 0;
	boolean negative = false;
	int i = start;
	long limit;
	long multmin;
	int digit;

	if (end > start) {
	    if (b[i] == '-') {
		negative = true;
		limit = Long.MIN_VALUE;
		i++;
	    } else {
		limit = -Long.MAX_VALUE;
	    }
	    multmin = limit / radix;
	    if (i < end) {
		digit = Character.digit((char)b[i++], radix);
		if (digit < 0) {
		    throw new NumberFormatException(
			"illegal number: " + toString(b, start, end)
			);
		} else {
		    result = -digit;
		}
	    }
	    while (i < end) {
		// Accumulating negatively avoids surprises near MAX_VALUE
		digit = Character.digit((char)b[i++], radix);
		if (digit < 0) {
		    throw new NumberFormatException("illegal number");
		}
		if (result < multmin) {
		    throw new NumberFormatException("illegal number");
		}
		result *= radix;
		if (result < limit + digit) {
		    throw new NumberFormatException("illegal number");
		}
		result -= digit;
	    }
	} else {
	    throw new NumberFormatException("illegal number");
	}
	if (negative) {
	    if (i > start + 1) {
		return result;
	    } else {	/* Only got "-" */
		throw new NumberFormatException("illegal number");
	    }
	} else {
	    return -result;
	}
    }

    /**
     * Convert the bytes within the specified range of the given byte 
     * array into a signed long . The range extends from 
     * <code>start</code> till, but not including <code>end</code>. <p>
     *
     * @param	b	the bytes
     * @param	start	the first byte offset
     * @param	end	the last byte offset
     * @return		the long value
     * @exception	NumberFormatException	for conversion errors
     */
    public static long parseLong(byte[] b, int start, int end)
		throws NumberFormatException {
	return parseLong(b, start, end, 10);
    }

    /**
     * Convert the bytes within the specified range of the given byte 
     * array into a String. The range extends from <code>start</code>
     * till, but not including <code>end</code>.
     *
     * @param	b	the bytes
     * @param	start	the first byte offset
     * @param	end	the last byte offset
     * @return		the String
     */
    public static String toString(byte[] b, int start, int end) {
	int size = end - start;
	char[] theChars = new char[size];

	for (int i = 0, j = start; i < size; )
	    theChars[i++] = (char)(b[j++]&0xff);
	
	return new String(theChars);
    }

    /**
     * Convert the bytes into a String.
     *
     * @param	b	the bytes
     * @return		the String
     * @since	JavaMail 1.4.4
     */
    public static String toString(byte[] b) {
	return toString(b, 0, b.length);
    }

    public static String toString(ByteArrayInputStream is) {
	int size = is.available();
	char[] theChars = new char[size];
	byte[] bytes    = new byte[size];

	is.read(bytes, 0, size);
	for (int i = 0; i < size;)
	    theChars[i] = (char)(bytes[i++]&0xff);
	
	return new String(theChars);
    }


    public static byte[] getBytes(String s) {
	char [] chars= s.toCharArray();
	int size = chars.length;
	byte[] bytes = new byte[size];
    	
	for (int i = 0; i < size;)
	    bytes[i] = (byte) chars[i++];
	return bytes;
    }

    public static byte[] getBytes(InputStream is) throws IOException {

	int len;
	int size = 1024;
	byte [] buf;


	if (is instanceof ByteArrayInputStream) {
	    size = is.available();
	    buf = new byte[size];
	    len = is.read(buf, 0, size);
	}
	else {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    buf = new byte[size];
	    while ((len = is.read(buf, 0, size)) != -1)
		bos.write(buf, 0, len);
	    buf = bos.toByteArray();
	}
	return buf;
    }
}
