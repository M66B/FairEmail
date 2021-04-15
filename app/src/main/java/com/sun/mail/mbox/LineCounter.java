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

/**
 * Count number of lines output.
 */
class LineCounter extends FilterOutputStream {
    private int lastb = -1;
    protected int lineCount;

    public LineCounter(OutputStream os) {
	super(os);
    }

    public void write(int b) throws IOException {
	// If we have a full line, count it.
	if (b == '\r' || (b == '\n' && lastb != '\r'))
	    lineCount++;
	out.write(b);
	lastb = b;
    }

    public void write(byte[] b) throws IOException {
	write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
	for (int i = 0 ; i < len ; i++) {
	    write(b[off + i]);
	}
    }

    public int getLineCount() {
	return lineCount;
    }

    // for testing
    public static void main(String argv[]) throws Exception {
	int b;
	LineCounter os =
	    new LineCounter(System.out);
	while ((b = System.in.read()) >= 0)
	    os.write(b);
	os.flush();
	System.out.println(os.getLineCount());
    }
}
