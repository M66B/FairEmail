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
import java.util.logging.Level;

/**
 * This class is a FilterInputStream that writes the bytes
 * being read from the given input stream into the given output
 * stream. This class is typically used to provide a trace of
 * the data that is being retrieved from an input stream.
 *
 * @author John Mani
 */

public class TraceInputStream extends FilterInputStream {
    private boolean trace = false;
    private boolean quote = false;
    private OutputStream traceOut;

    /**
     * Creates an input stream filter built on top of the specified
     * input stream.
     *   
     * @param   in   the underlying input stream.
     * @param   logger	log trace here
     */
    public TraceInputStream(InputStream in, MailLogger logger) {
	super(in);
	this.trace = logger.isLoggable(Level.FINEST);
	this.traceOut = new LogOutputStream(logger);
    }

    /**
     * Creates an input stream filter built on top of the specified
     * input stream.
     *   
     * @param   in   the underlying input stream.
     * @param	traceOut	the trace stream.
     */
    public TraceInputStream(InputStream in, OutputStream traceOut) {
	super(in);
	this.traceOut = traceOut;
    }

    /**
     * Set trace mode.
     * @param	trace	the trace mode
     */
    public void setTrace(boolean trace) {
	this.trace = trace;
    }

    /**
     * Set quote mode.
     * @param	quote	the quote mode
     */
    public void setQuote(boolean quote) {
	this.quote = quote;
    }

    /**
     * Reads the next byte of data from this input stream. Returns
     * <code>-1</code> if no data is available. Writes out the read
     * byte into the trace stream, if trace mode is <code>true</code>
     */
    @Override
    public int read() throws IOException {
	int b = in.read();
	if (trace && b != -1) {
	    if (quote)
		writeByte(b);
	    else
		traceOut.write(b);
	}
	return b;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. Returns <code>-1</code> if no more data
     * is available. Writes out the read bytes into the trace stream, if 
     * trace mode is <code>true</code>
     */
    @Override
    public int read(byte b[], int off, int len) throws IOException {
	int count = in.read(b, off, len);
	if (trace && count != -1) {
	    if (quote) {
		for (int i = 0; i < count; i++)
		    writeByte(b[off + i]);
	    } else
		traceOut.write(b, off, count);
	}
	return count;
    }

    /**
     * Write a byte in a way that every byte value is printable ASCII.
     */
    private final void writeByte(int b) throws IOException {
	b &= 0xff;
	if (b > 0x7f) {
	    traceOut.write('M');
	    traceOut.write('-');
	    b &= 0x7f;
	}
	if (b == '\r') {
	    traceOut.write('\\');
	    traceOut.write('r');
	} else if (b == '\n') {
	    traceOut.write('\\');
	    traceOut.write('n');
	    traceOut.write('\n');
	} else if (b == '\t') {
	    traceOut.write('\\');
	    traceOut.write('t');
	} else if (b < ' ') {
	    traceOut.write('^');
	    traceOut.write('@' + b);
	} else {
	    traceOut.write(b);
	}
    }
}
