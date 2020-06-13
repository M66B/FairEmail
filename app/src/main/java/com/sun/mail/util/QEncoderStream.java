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
 * This class implements a Q Encoder as defined by RFC 2047 for 
 * encoding MIME headers. It subclasses the QPEncoderStream class.
 * 
 * @author John Mani
 */

public class QEncoderStream extends QPEncoderStream {

    private String specials;
    private static String WORD_SPECIALS = "=_?\"#$%&'(),.:;<>@[\\]^`{|}~";
    private static String TEXT_SPECIALS = "=_?";

    /**
     * Create a Q encoder that encodes the specified input stream
     * @param out        the output stream
     * @param encodingWord true if we are Q-encoding a word within a
     *			phrase.
     */
    public QEncoderStream(OutputStream out, boolean encodingWord) {
	super(out, Integer.MAX_VALUE); // MAX_VALUE is 2^31, should
				       // suffice (!) to indicate that
				       // CRLFs should not be inserted
				       // when encoding rfc822 headers

	// a RFC822 "word" token has more restrictions than a
	// RFC822 "text" token.
	specials = encodingWord ? WORD_SPECIALS : TEXT_SPECIALS;
    }

    /**
     * Encodes the specified <code>byte</code> to this output stream.
     * @param      c   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void write(int c) throws IOException {
	c = c & 0xff; // Turn off the MSB.
	if (c == ' ')
	    output('_', false);
	else if (c < 040 || c >= 0177 || specials.indexOf(c) >= 0)
	    // Encoding required. 
	    output(c, true);
	else // No encoding required
	    output(c, false);
    }

    /**
     * Returns the length of the encoded version of this byte array.
     *
     * @param	b	the byte array
     * @param	encodingWord	true if encoding words, false if encoding text
     * @return		the length
     */
    public static int encodedLength(byte[] b, boolean encodingWord) {
	int len = 0;
	String specials = encodingWord ? WORD_SPECIALS: TEXT_SPECIALS;
	for (int i = 0; i < b.length; i++) {
	    int c = b[i] & 0xff; // Mask off MSB
	    if (c < 040 || c >= 0177 || specials.indexOf(c) >= 0)
		// needs encoding
		len += 3; // Q-encoding is 1 -> 3 conversion
	    else
		len++;
	}
	return len;
    }

    /**** begin TEST program ***
    public static void main(String argv[]) throws Exception {
        FileInputStream infile = new FileInputStream(argv[0]);
        QEncoderStream encoder = new QEncoderStream(System.out);
        int c;
 
        while ((c = infile.read()) != -1)
            encoder.write(c);
        encoder.close();
    }
    *** end TEST program ***/
}
