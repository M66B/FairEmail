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
 * Count the number of bytes in the body of the message written to the stream.
 */
class ContentLengthCounter extends OutputStream {
    private long size = 0;
    private boolean inHeader = true;
    private int lastb1 = -1, lastb2 = -1;

    public void write(int b) throws IOException {
	if (inHeader) {
	    // if line terminator is CR
	    if (b == '\r' && lastb1 == '\r')
		inHeader = false;
	    else if (b == '\n') {
		// if line terminator is \n
		if (lastb1 == '\n')
		    inHeader = false;
		// if line terminator is CRLF
		else if (lastb1 == '\r' && lastb2 == '\n')
		    inHeader = false;
	    }
	    lastb2 = lastb1;
	    lastb1 = b;
	} else
	    size++;
    }

    public void write(byte[] b) throws IOException {
	if (inHeader)
	    super.write(b);
	else
	    size += b.length;
    }

    public void write(byte[] b, int off, int len) throws IOException {
	if (inHeader)
	    super.write(b, off, len);
	else
	    size += len;
    }

    public long getSize() {
	return size;
    }

    /*
    public static void main(String argv[]) throws Exception {
	int b;
	ContentLengthCounter os = new ContentLengthCounter();
	while ((b = System.in.read()) >= 0)
	    os.write(b);
	System.out.println("size " + os.getSize());
    }
    */
}
