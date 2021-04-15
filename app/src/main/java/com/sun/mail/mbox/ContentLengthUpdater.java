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
 * Update the Content-Length header in the message written to the stream.
 */
class ContentLengthUpdater extends FilterOutputStream {
    private String contentLength;
    private boolean inHeader = true;
    private boolean sawContentLength = false;
    private int lastb1 = -1, lastb2 = -1;
    private StringBuilder line = new StringBuilder();

    public ContentLengthUpdater(OutputStream os, long contentLength) {
	super(os);
	this.contentLength = "Content-Length: " + contentLength;
    }

    public void write(int b) throws IOException {
	if (inHeader) {
	    String eol = "\n";
	    // First, determine if we're still in the header.
	    if (b == '\r') {
		// if line terminator is CR
		if (lastb1 == '\r') {
		    inHeader = false;
		    eol = "\r";
		// else, if line terminator is CRLF
		} else if (lastb1 == '\n' && lastb2 == '\r') {
		    inHeader = false;
		    eol = "\r\n";
		}
	    // else, if line terminator is \n
	    } else if (b == '\n') {
		if (lastb1 == '\n') {
		    inHeader = false;
		    eol = "\n";
		}
	    }

	    // If we're no longer in the header, and we haven't seen
	    // a Content-Length header yet, it's time to put one out.
	    if (!inHeader && !sawContentLength) {
		out.write(contentLength.getBytes("iso-8859-1"));
		out.write(eol.getBytes("iso-8859-1"));
	    }

	    // If we have a full line, see if it's a Content-Length header.
	    if (b == '\r' || (b == '\n' && lastb1 != '\r')) {
		if (line.toString().regionMatches(true, 0,
					"content-length:", 0, 15)) {
		    // yup, got it
		    sawContentLength = true;
		    // put out the new version
		    out.write(contentLength.getBytes("iso-8859-1"));
		} else {
		    // not a Content-Length header, just write it out
		    out.write(line.toString().getBytes("iso-8859-1"));
		}
		line.setLength(0);	// clear buffer for next line
	    }
	    if (b == '\r' || b == '\n')
		out.write(b);	// write out line terminator immediately
	    else
		line.append((char)b);	// accumulate characters of the line

	    // rotate saved characters for next time through loop
	    lastb2 = lastb1;
	    lastb1 = b;
	} else
	    out.write(b);		// not in the header, just write it out
    }

    public void write(byte[] b) throws IOException {
	if (inHeader)
	    write(b, 0, b.length);
	else
	    out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
	if (inHeader) {
	    for (int i = 0 ; i < len ; i++) {
		write(b[off + i]);
	    }
	} else
	    out.write(b, off, len);
    }

    // for testing
    public static void main(String argv[]) throws Exception {
	int b;
	ContentLengthUpdater os =
	    new ContentLengthUpdater(System.out, Long.parseLong(argv[0]));
	while ((b = System.in.read()) >= 0)
	    os.write(b);
	os.flush();
    }
}
