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
 * Convert lines into the canonical format, that is, terminate lines with the
 * CRLF sequence.
 * 
 * @author John Mani
 */
public class CRLFOutputStream extends FilterOutputStream {
    protected int lastb = -1;
    protected boolean atBOL = true;	// at beginning of line?
    private static final byte[] newline = { (byte)'\r', (byte)'\n' };

    public CRLFOutputStream(OutputStream os) {
	super(os);
    }

    @Override
    public void write(int b) throws IOException {
	if (b == '\r') {
	    writeln();
	} else if (b == '\n') {
	    if (lastb != '\r')
		writeln();
	} else {
	    out.write(b);
	    atBOL = false;
	}
	lastb = b;
    }

    @Override
    public void write(byte b[]) throws IOException {
	write(b, 0, b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
	int start = off;
	
	len += off;
	for (int i = start; i < len ; i++) {
	    if (b[i] == '\r') {
		out.write(b, start, i - start);
		writeln();
		start = i + 1;
	    } else if (b[i] == '\n') {
		if (lastb != '\r') {
		    out.write(b, start, i - start);
		    writeln();
		}
		start = i + 1;
	    }
	    lastb = b[i];
	}
	if ((len - start) > 0) {
	    out.write(b, start, len - start);
	    atBOL = false;
	}
    }

    /*
     * Just write out a new line, something similar to out.println()
     */
    public void writeln() throws IOException {
        out.write(newline);
	atBOL = true;
    }
}
