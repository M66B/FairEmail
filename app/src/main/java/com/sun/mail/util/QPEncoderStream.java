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
 * This class implements a Quoted Printable Encoder. It is implemented as
 * a FilterOutputStream, so one can just wrap this class around
 * any output stream and write bytes into this filter. The Encoding
 * is done as the bytes are written out.
 * 
 * @author John Mani
 */

public class QPEncoderStream extends FilterOutputStream {
    private int count = 0; 	// number of bytes that have been output
    private int bytesPerLine;	// number of bytes per line
    private boolean gotSpace = false;
    private boolean gotCR = false;

    /**
     * Create a QP encoder that encodes the specified input stream
     * @param out        the output stream
     * @param bytesPerLine  the number of bytes per line. The encoder
     *                   inserts a CRLF sequence after this many number
     *                   of bytes.
     */
    public QPEncoderStream(OutputStream out, int bytesPerLine) {
	super(out);
	// Subtract 1 to account for the '=' in the soft-return 
	// at the end of a line
	this.bytesPerLine = bytesPerLine - 1;
    }

    /**
     * Create a QP encoder that encodes the specified input stream.
     * Inserts the CRLF sequence after outputting 76 bytes.
     * @param out        the output stream
     */
    public QPEncoderStream(OutputStream out) {
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
    public void write(byte[] b, int off, int len) throws IOException {
	for (int i = 0; i < len; i++)
	    write(b[off + i]);
    }

    /**
     * Encodes <code>b.length</code> bytes to this output stream.
     * @param      b   the data to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void write(byte[] b) throws IOException {
	write(b, 0, b.length);
    }

    /**
     * Encodes the specified <code>byte</code> to this output stream.
     * @param      c   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void write(int c) throws IOException {
	c = c & 0xff; // Turn off the MSB.
	if (gotSpace) { // previous character was <SPACE>
	    if (c == '\r' || c == '\n')
		// if CR/LF, we need to encode the <SPACE> char
		output(' ', true);
	    else // no encoding required, just output the char
		output(' ', false);
	    gotSpace = false;
	}

	if (c == '\r') {
	    gotCR = true;
	    outputCRLF();
	} else {
	    if (c == '\n') {
		if (gotCR) 
		    // This is a CRLF sequence, we already output the 
		    // corresponding CRLF when we got the CR, so ignore this
		    ;
		else
		    outputCRLF();
	    } else if (c == ' ') {
		gotSpace = true;
	    } else if (c < 040 || c >= 0177 || c == '=')
		// Encoding required. 
		output(c, true);
	    else // No encoding required
		output(c, false);
	    // whatever it was, it wasn't a CR
	    gotCR = false;
	}
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be encoded out to the stream.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
	if (gotSpace) {
	    output(' ', true);
	    gotSpace = false;
	}
	out.flush();
    }

    /**
     * Forces any buffered output bytes to be encoded out to the stream
     * and closes this output stream.
     *
     * @exception	IOException	for I/O errors
     */
    @Override
    public void close() throws IOException {
	flush();
	out.close();
    }

    private void outputCRLF() throws IOException {
	out.write('\r');
	out.write('\n');
	count = 0;
    }

    // The encoding table
    private final static char hex[] = {
	'0','1', '2', '3', '4', '5', '6', '7',
	'8','9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    protected void output(int c, boolean encode) throws IOException {
	if (encode) {
	    if ((count += 3) > bytesPerLine) {
		out.write('=');
	    	out.write('\r');
	    	out.write('\n');
		count = 3; // set the next line's length
	    }
	    out.write('=');
	    out.write(hex[c >> 4]);
	    out.write(hex[c & 0xf]);
	} else {
	    if (++count > bytesPerLine) {
		out.write('=');
	    	out.write('\r');
	    	out.write('\n');
		count = 1; // set the next line's length
	    }
	    out.write(c);
	}
    }

    /**** begin TEST program ***
    public static void main(String argv[]) throws Exception {
        FileInputStream infile = new FileInputStream(argv[0]);
        QPEncoderStream encoder = new QPEncoderStream(System.out);
        int c;
 
        while ((c = infile.read()) != -1)
            encoder.write(c);
        encoder.close();
    }
    *** end TEST program ***/
}
