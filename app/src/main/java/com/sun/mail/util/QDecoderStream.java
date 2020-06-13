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
 * This class implements a Q Decoder as defined in RFC 2047
 * for decoding MIME headers. It subclasses the QPDecoderStream class.
 * 
 * @author John Mani
 */

public class QDecoderStream extends QPDecoderStream {

    /**
     * Create a Q-decoder that decodes the specified input stream.
     * @param in        the input stream
     */
    public QDecoderStream(InputStream in) {
	super(in);
    }

    /**
     * Read the next decoded byte from this input stream. The byte
     * is returned as an <code>int</code> in the range <code>0</code>
     * to <code>255</code>. If no byte is available because the end of
     * the stream has been reached, the value <code>-1</code> is returned.
     * This method blocks until input data is available, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
	int c = in.read();

	if (c == '_') // Return '_' as ' '
	    return ' ';
	else if (c == '=') {
	    // QP Encoded atom. Get the next two bytes ..
	    ba[0] = (byte)in.read();
	    ba[1] = (byte)in.read();
	    // .. and decode them
	    try {
		return ASCIIUtility.parseInt(ba, 0, 2, 16);
	    } catch (NumberFormatException nex) {
		throw new DecodingException(
			"QDecoder: Error in QP stream " + nex.getMessage());
	    }
	} else
	    return c;
    }
}
