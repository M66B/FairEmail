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

package com.sun.mail.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CharacterCodingException;

/**
 * LineInputStream supports reading CRLF terminated lines that
 * contain only US-ASCII characters from an input stream. Provides
 * functionality that is similar to the deprecated 
 * <code>DataInputStream.readLine()</code>. Expected use is to read
 * lines as String objects from an IMAP/SMTP/etc. stream. <p>
 *
 * This class also supports UTF-8 data by calling the appropriate
 * constructor.  Or, if the System property <code>mail.mime.allowutf8</code>
 * is set to true, an attempt will be made to interpret the data as UTF-8,
 * falling back to treating it as an 8-bit charset if that fails. <p>
 *
 * LineInputStream is implemented as a FilterInputStream, so one can just
 * wrap it around any input stream and read bytes from this filter.
 * 
 * @author John Mani
 * @author Bill Shannon
 */

public class LineInputStream extends FilterInputStream {

    private boolean allowutf8;
    private byte[] lineBuffer = null; // reusable byte buffer
    private CharsetDecoder decoder;

    private static boolean defaultutf8 =
	PropUtil.getBooleanSystemProperty("mail.mime.allowutf8", false);
    private static int MAX_INCR = 1024*1024;	// 1MB

    public LineInputStream(InputStream in) {
	this(in, false);
    }

    /**
     * @param	in	the InputStream
     * @param	allowutf8	allow UTF-8 characters?
     * @since	JavaMail 1.6
     */
    public LineInputStream(InputStream in, boolean allowutf8) {
	super(in);
	this.allowutf8 = allowutf8;
	if (!allowutf8 && defaultutf8) {
	    decoder = StandardCharsets.UTF_8.newDecoder();
	    decoder.onMalformedInput(CodingErrorAction.REPORT);
	    decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
	}
    }

    /**
     * Read a line containing only ASCII characters from the input 
     * stream. A line is terminated by a CR or NL or CR-NL sequence.
     * A common error is a CR-CR-NL sequence, which will also terminate
     * a line.
     * The line terminator is not returned as part of the returned 
     * String. Returns null if no data is available. <p>
     *
     * This class is similar to the deprecated 
     * <code>DataInputStream.readLine()</code>
     *
     * @return		the line
     * @exception	IOException	for I/O errors
     */
    @SuppressWarnings("deprecation")	// for old String constructor
    public String readLine() throws IOException {
	//InputStream in = this.in;
	byte[] buf = lineBuffer;

	if (buf == null)
	    buf = lineBuffer = new byte[128];

	int c1;
	int room = buf.length;
	int offset = 0;

	while ((c1 = in.read()) != -1) {
	    if (c1 == '\n') // Got NL, outa here.
		break;
	    else if (c1 == '\r') {
		// Got CR, is the next char NL ?
		boolean twoCRs = false;
		if (in.markSupported())
		    in.mark(2);
		int c2 = in.read();
		if (c2 == '\r') {		// discard extraneous CR
		    twoCRs = true;
		    c2 = in.read();
		}
		if (c2 != '\n') {
		    /*
		     * If the stream supports it (which we hope will always
		     * be the case), reset to after the first CR.  Otherwise,
		     * we wrap a PushbackInputStream around the stream so we
		     * can unread the characters we don't need.  The only
		     * problem with that is that the caller might stop
		     * reading from this LineInputStream, throw it away,
		     * and then start reading from the underlying stream.
		     * If that happens, the pushed back characters will be
		     * lost forever.
		     */
		    if (in.markSupported())
			in.reset();
		    else {
			if (!(in instanceof PushbackInputStream))
			    in /*= this.in*/ = new PushbackInputStream(in, 2);
			if (c2 != -1)
			    ((PushbackInputStream)in).unread(c2);
			if (twoCRs)
			    ((PushbackInputStream)in).unread('\r');
		    }
		}
		break; // outa here.
	    }

	    // Not CR, NL or CR-NL ...
	    // .. Insert the byte into our byte buffer
	    if (--room < 0) { // No room, need to grow.
		if (buf.length < MAX_INCR)
		    buf = new byte[buf.length * 2];
		else
		    buf = new byte[buf.length + MAX_INCR];
		room = buf.length - offset - 1;
		System.arraycopy(lineBuffer, 0, buf, 0, offset);
		lineBuffer = buf;
	    }
	    buf[offset++] = (byte)c1;
	}

	if ((c1 == -1) && (offset == 0))
	    return null;

	if (allowutf8)
	    return new String(buf, 0, offset, StandardCharsets.UTF_8);
	else {
	    if (defaultutf8) {
		// try to decode it as UTF-8
		try {
		    return decoder.decode(ByteBuffer.wrap(buf, 0, offset)).
					    toString();
		} catch (CharacterCodingException cex) {
		    // looks like it's not valid UTF-8 data,
		    // fall through and treat it as an 8-bit charset
		}
	    }
	    return new String(buf, 0, 0, offset);
	}
    }
}
