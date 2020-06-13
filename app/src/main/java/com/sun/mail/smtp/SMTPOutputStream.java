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
import com.sun.mail.util.CRLFOutputStream;

/**
 * In addition to converting lines into the canonical format,
 * i.e., terminating lines with the CRLF sequence, escapes the "."
 * by adding another "." to any "." that appears in the beginning
 * of a line.  See RFC821 section 4.5.2.
 * 
 * @author Max Spivak
 * @see CRLFOutputStream
 */
public class SMTPOutputStream extends CRLFOutputStream {
    public SMTPOutputStream(OutputStream os) {
	super(os);
    }

    @Override
    public void write(int b) throws IOException {
	// if that last character was a newline, and the current
	// character is ".", we always write out an extra ".".
	if ((lastb == '\n' || lastb == '\r' || lastb == -1) && b == '.') {
	    out.write('.');
	}
	
	super.write(b);
    }

    /* 
     * This method has been added to improve performance.
     */
    @Override
    public void write(byte b[], int off, int len) throws IOException {
	int lastc = (lastb == -1) ? '\n' : lastb;
	int start = off;
	
	len += off;
	for (int i = off; i < len; i++) {
	    if ((lastc == '\n' || lastc == '\r') && b[i] == '.') {
		super.write(b, start, i - start);
		out.write('.');
		start = i;
	    }
	    lastc = b[i];
	}
	if ((len - start) > 0)
	    super.write(b, start, len - start);
    }

    /**
     * Override flush method in FilterOutputStream.
     *
     * The MimeMessage writeTo method flushes its buffer at the end,
     * but we don't want to flush data out to the socket until we've
     * also written the terminating "\r\n.\r\n".
     *
     * We buffer nothing so there's nothing to flush.  We depend
     * on the fact that CRLFOutputStream also buffers nothing.
     * SMTPTransport will manually flush the socket before reading
     * the response.
     */
    @Override
    public void flush() {
	// do nothing
    }

    /**
     * Ensure we're at the beginning of a line.
     * Write CRLF if not.
     *
     * @exception	IOException	if the write fails
     */
    public void ensureAtBOL() throws IOException {
	if (!atBOL)
	    super.writeln();
    }
}
