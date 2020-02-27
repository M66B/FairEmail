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

import java.io.*;

/**
 * This class implements a BASE64 encoder.  It is implemented as
 * a FilterOutputStream, so one can just wrap this class around
 * any output stream and write bytes into this filter.  The encoding
 * is done as the bytes are written out.
 * 
 * @author John Mani
 * @author Bill Shannon
 */

public class BASE64EncoderStream extends FilterOutputStream {
    private byte[] buffer; 	// cache of bytes that are yet to be encoded
    private int bufsize = 0;	// size of the cache
    private byte[] outbuf; 	// line size output buffer
    private int count = 0; 	// number of bytes that have been output
    private int bytesPerLine;	// number of bytes per line
    private int lineLimit;	// number of input bytes to output bytesPerLine
    private boolean noCRLF = false;

    private static byte[] newline = new byte[] { '\r', '\n' };

    /**
     * Create a BASE64 encoder that encodes the specified output stream.
     *
     * @param out        the output stream
     * @param bytesPerLine  number of bytes per line. The encoder inserts
     * 			a CRLF sequence after the specified number of bytes,
     *			unless bytesPerLine is Integer.MAX_VALUE, in which
     *			case no CRLF is inserted.  bytesPerLine is rounded
     *			down to a multiple of 4.
     */
    public BASE64EncoderStream(OutputStream out, int bytesPerLine) {
	super(out);
	buffer = new byte[3];
	if (bytesPerLine == Integer.MAX_VALUE || bytesPerLine < 4) {
	    noCRLF = true;
	    bytesPerLine = 76;
	}
	bytesPerLine = (bytesPerLine / 4) * 4;	// Rounded down to 4n
	this.bytesPerLine = bytesPerLine;	// save it
        lineLimit = bytesPerLine / 4 * 3;

	if (noCRLF) {
	    outbuf = new byte[bytesPerLine];
	} else {
	    outbuf = new byte[bytesPerLine + 2];
	    outbuf[bytesPerLine] = (byte)'\r';
	    outbuf[bytesPerLine + 1] = (byte)'\n';
	}
    }

    /**
     * Create a BASE64 encoder that encodes the specified input stream.
     * Inserts the CRLF sequence after outputting 76 bytes.
     *
     * @param out        the output stream
     */
    public BASE64EncoderStream(OutputStream out) {
	this(out, 76);	
    }

    /**
     * Encodes <code>len</code> bytes from the specified
     * <code>byte</code> array starting at offset <code>off</code> to
     * this output stream.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public synchronized void write(byte[] b, int off, int len)
				throws IOException {
	int end = off + len;

	// finish off incomplete coding unit
	while (bufsize != 0 && off < end)
	    write(b[off++]);

	// finish off line
	int blen = ((bytesPerLine - count) / 4) * 3;
	if (off + blen <= end) {
	    // number of bytes that will be produced in outbuf
	    int outlen = encodedSize(blen);
	    if (!noCRLF) {
		outbuf[outlen++] = (byte)'\r';
		outbuf[outlen++] = (byte)'\n';
	    }
	    out.write(encode(b, off, blen, outbuf), 0, outlen);
	    off += blen;
	    count = 0;
	}

	// do bulk encoding a line at a time.
	for (; off + lineLimit <= end; off += lineLimit)
	    out.write(encode(b, off, lineLimit, outbuf));

	// handle remaining partial line
	if (off + 3 <= end) {
	    blen = end - off;
	    blen = (blen / 3) * 3;	// round down
	    // number of bytes that will be produced in outbuf
	    int outlen = encodedSize(blen);
	    out.write(encode(b, off, blen, outbuf), 0, outlen);
	    off += blen;
	    count += outlen;
	}

	// start next coding unit
	for (; off < end; off++)
	    write(b[off]);
    }

    /**
     * Encodes <code>b.length</code> bytes to this output stream.
     *
     * @param      b   the data to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void write(byte[] b) throws IOException {
	write(b, 0, b.length);
    }

    /**
     * Encodes the specified <code>byte</code> to this output stream.
     *
     * @param      c   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public synchronized void write(int c) throws IOException {
	buffer[bufsize++] = (byte)c;
	if (bufsize == 3) { // Encoding unit = 3 bytes
	    encode();
	    bufsize = 0;
	}
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be encoded out to the stream. 
     *
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public synchronized void flush() throws IOException {
	if (bufsize > 0) { // If there's unencoded characters in the buffer ..
	    encode();      // .. encode them
	    bufsize = 0;
	}
	out.flush();
    }

    /**
     * Forces any buffered output bytes to be encoded out to the stream
     * and closes this output stream
     */
    @Override
    public synchronized void close() throws IOException {
	flush();
	if (count > 0 && !noCRLF) {
	    out.write(newline);
	    out.flush();
	}
	out.close();
    }

    /** This array maps the characters to their 6 bit values */
    private final static char pem_array[] = {
	'A','B','C','D','E','F','G','H', // 0
	'I','J','K','L','M','N','O','P', // 1
	'Q','R','S','T','U','V','W','X', // 2
	'Y','Z','a','b','c','d','e','f', // 3
	'g','h','i','j','k','l','m','n', // 4
	'o','p','q','r','s','t','u','v', // 5
	'w','x','y','z','0','1','2','3', // 6
	'4','5','6','7','8','9','+','/'  // 7
    };

    /**
     * Encode the data stored in <code>buffer</code>.
     * Uses <code>outbuf</code> to store the encoded
     * data before writing.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    private void encode() throws IOException {
	int osize = encodedSize(bufsize);
	out.write(encode(buffer, 0, bufsize, outbuf), 0, osize);
	// increment count
	count += osize;
	// If writing out this encoded unit caused overflow,
	// start a new line.
	if (count >= bytesPerLine) {
	    if (!noCRLF)
		out.write(newline);
	    count = 0;
	}
    }

    /**
     * Base64 encode a byte array.  No line breaks are inserted.
     * This method is suitable for short strings, such as those
     * in the IMAP AUTHENTICATE protocol, but not to encode the
     * entire content of a MIME part.
     *
     * @param	inbuf	the byte array
     * @return		the encoded byte array
     */
    public static byte[] encode(byte[] inbuf) {
	if (inbuf.length == 0)
	    return inbuf;
	return encode(inbuf, 0, inbuf.length, null);
    }

    /**
     * Internal use only version of encode.  Allow specifying which
     * part of the input buffer to encode.  If outbuf is non-null,
     * it's used as is.  Otherwise, a new output buffer is allocated.
     */
    private static byte[] encode(byte[] inbuf, int off, int size,
				byte[] outbuf) {
	if (outbuf == null)
	    outbuf = new byte[encodedSize(size)];
	int inpos, outpos;
	int val;
	for (inpos = off, outpos = 0; size >= 3; size -= 3, outpos += 4) {
	    val = inbuf[inpos++] & 0xff;
	    val <<= 8;
	    val |= inbuf[inpos++] & 0xff;
	    val <<= 8;
	    val |= inbuf[inpos++] & 0xff;
	    outbuf[outpos+3] = (byte)pem_array[val & 0x3f];
	    val >>= 6;
	    outbuf[outpos+2] = (byte)pem_array[val & 0x3f];
	    val >>= 6;
	    outbuf[outpos+1] = (byte)pem_array[val & 0x3f];
	    val >>= 6;
	    outbuf[outpos+0] = (byte)pem_array[val & 0x3f];
	}
	// done with groups of three, finish up any odd bytes left
	if (size == 1) {
	    val = inbuf[inpos++] & 0xff;
	    val <<= 4;
	    outbuf[outpos+3] = (byte)'=';	// pad character;
	    outbuf[outpos+2] = (byte)'=';	// pad character;
	    outbuf[outpos+1] = (byte)pem_array[val & 0x3f];
	    val >>= 6;
	    outbuf[outpos+0] = (byte)pem_array[val & 0x3f];
	} else if (size == 2) {
	    val = inbuf[inpos++] & 0xff;
	    val <<= 8;
	    val |= inbuf[inpos++] & 0xff;
	    val <<= 2;
	    outbuf[outpos+3] = (byte)'=';	// pad character;
	    outbuf[outpos+2] = (byte)pem_array[val & 0x3f];
	    val >>= 6;
	    outbuf[outpos+1] = (byte)pem_array[val & 0x3f];
	    val >>= 6;
	    outbuf[outpos+0] = (byte)pem_array[val & 0x3f];
	}
	return outbuf;
    }

    /**
     * Return the corresponding encoded size for the given number
     * of bytes, not including any CRLF.
     */
    private static int encodedSize(int size) {
	return ((size + 2) / 3) * 4;
    }

    /*** begin TEST program
    public static void main(String argv[]) throws Exception {
	FileInputStream infile = new FileInputStream(argv[0]);
	BASE64EncoderStream encoder = new BASE64EncoderStream(System.out);
	int c;

	while ((c = infile.read()) != -1)
	    encoder.write(c);
	encoder.close();
    }
    *** end TEST program **/
}
