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

package com.sun.mail.mbox;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Convert the various newline conventions to the local platform's
 * newline convention.  Optionally, make sure the output ends with
 * a blank line.
 */
public class NewlineOutputStream extends FilterOutputStream {
    private int lastb = -1;
    private int bol = 1; // number of times in a row we're at beginning of line
    private final boolean endWithBlankLine;
    private static final byte[] newline;

    static {
	String s = null;
	try {
	    s = System.lineSeparator();
	} catch (SecurityException sex) {
	    // ignore, should never happen
	}
	if (s == null || s.length() <= 0)
	    s = "\n";
	newline = s.getBytes(StandardCharsets.ISO_8859_1);
    }

    public NewlineOutputStream(OutputStream os) {
	this(os, false);
    }

    public NewlineOutputStream(OutputStream os, boolean endWithBlankLine) {
	super(os);
	this.endWithBlankLine = endWithBlankLine;
    }

    public void write(int b) throws IOException {
	if (b == '\r') {
	    out.write(newline);
	    bol++;
	} else if (b == '\n') {
	    if (lastb != '\r') {
		out.write(newline);
		bol++;
	    }
	} else {
	    out.write(b);
	    bol = 0;	// no longer at beginning of line
	}
	lastb = b;
    }

    public void write(byte b[]) throws IOException {
	write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
	for (int i = 0 ; i < len ; i++) {
	    write(b[off + i]);
	}
    }

    public void flush() throws IOException {
	if (endWithBlankLine) {
	    if (bol == 0) {
		// not at bol, return to bol and add a blank line
		out.write(newline);
		out.write(newline);
	    } else if (bol == 1) {
		// at bol, add a blank line
		out.write(newline);
	    }
	}
	bol = 2;
	out.flush();
    }
}
