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
import java.util.*;

/**
 * A support class that contains the state and logic needed when
 * loading messages from a folder.
 */
final class MessageLoader {
    private final TempFile temp;
    private FileInputStream fis = null;
    private OutputStream fos = null;
    private int pos, len;	// position in and length of buffer
    private long off;		// current offset in temp file
    private long prevend;	// the end of the previous message in temp file
    private MboxFolder.MessageMetadata md;
    private byte[] buf = null;
    // the length of the longest header we'll need to look at
    private static final int LINELEN = "Content-Length: XXXXXXXXXX".length();
    private char[] line;

    public MessageLoader(TempFile temp) {
	this.temp = temp;
    }

    /**
     * Load messages from the given file descriptor, starting at the
     * specified offset, adding the MessageMetadata to the list. <p>
     *
     * The data is assumed to be in UNIX mbox format, with newlines
     * only as the line terminators.
     */
    public int load(FileDescriptor fd, long offset,
				List<MboxFolder.MessageMetadata> msgs)
				throws IOException {
	// XXX - could allocate and deallocate buffers here
	int loaded = 0;
	try {
	    fis = new FileInputStream(fd);
	    if (fis.skip(offset) != offset)
		throw new EOFException("Failed to skip to offset " + offset);
	    this.off = prevend = temp.length();
	    pos = len = 0;
	    line = new char[LINELEN];
	    buf = new byte[64 * 1024];
	    fos = temp.getAppendStream();
	    int n;
	    // keep loading messages as long as we have headers
	    while ((n = skipHeader(loaded == 0)) >= 0) {
		long start;
		if (n == 0) {
		    // didn't find a Content-Length, skip the body
		    start = skipBody();
		    if (start < 0) {
			// md.end = -1;
			md.dataend = -1;
			msgs.add(md);
			loaded++;
			break;
		    }
		    md.dataend = start;
		} else {
		    // skip over the body
		    skip(n);
		    md.dataend = off;
		    int b;
		    // skip any blank lines after the body
		    while ((b = get()) >= 0) {
			if (b != '\n')
			    break;
		    }
		    start = off;
		    if (b >= 0)
			start--;	// back up one byte if not at EOF
		}
		// md.end = start;
		prevend = start;
		msgs.add(md);
		loaded++;
	    }
	} finally {
	    try {
		fis.close();
	    } catch (IOException ex) {
		// ignore
	    }
	    try {
		fos.close();
	    } catch (IOException ex) {
		// ignore
	    }
	    line = null;
	    buf = null;
	}
	return loaded;
    }

    /**
     * Skip over the message header, returning the content length
     * of the body, or 0 if no Content-Length header was seen.
     * Update the MessageMetadata based on the headers seen.
     * return -1 on EOF.
     */
    private int skipHeader(boolean first)  throws IOException {
	int clen = 0;
	boolean bol = true;
	int lpos = -1;
	int b;
	boolean saw_unix_from = false;
	int lineno = 0;
	md = new MboxFolder.MessageMetadata();
	md.start = prevend;
	md.recent = true;
	while ((b = get()) >= 0) {
	    if (bol) {
		if (b == '\n')
		    break;
		lpos = 0;
	    }
	    if (b == '\n') {
		bol = true;
		lineno++;
		// newline at end of line, was the line one of the headers
		// we're looking for?
		if (lpos > 7) {
		    // XXX - make this more efficient?
		    String s = new String(line, 0, lpos);
		    // fast check for Content-Length header
		    if (lineno == 1 && line[0] == 'F' && isPrefix(s, "From ")) {
			saw_unix_from = true;
		    } else if (line[7] == '-' &&
				isPrefix(s, "Content-Length:")) {
			s = s.substring(15).trim();
			try {
			    clen = Integer.parseInt(s);
			} catch (NumberFormatException ex) {
			    // ignore it
			}
		    // fast check for Status header
		    } else if ((line[1] == 't' || line[1] == 'T') &&
				isPrefix(s, "Status:")) {
			if (s.indexOf('O') >= 0)
			    md.recent = false;
		    // fast check for X-Status header
		    } else if ((line[3] == 't' || line[3] == 'T') &&
				isPrefix(s, "X-Status:")) {
			if (s.indexOf('D') >= 0)
			    md.deleted = true;
		    // fast check for X-Dt-Delete-Time header
		    } else if (line[4] == '-' &&
				isPrefix(s, "X-Dt-Delete-Time:")) {
			md.deleted = true;
		    // fast check for X-IMAP header
		    } else if (line[5] == 'P' && s.startsWith("X-IMAP:")) {
			md.imap = true;
		    }
		}
	    } else {
		// accumlate data in line buffer
		bol = false;
		if (lpos < 0)	// ignoring this line
		    continue;
		if (lpos == 0 && (b == ' ' || b == '\t'))
		    lpos = -1;	// ignore continuation lines
		else if (lpos < line.length)
		    line[lpos++] = (char)b;
	    }
	}

	// if we hit EOF, or this is the first message we're loading and
	// it doesn't have a UNIX From line, return EOF.
	// (After the first message, UNIX From lines are seen by skipBody
	// to terminate the message.)
	if (b < 0 || (first && !saw_unix_from))
	    return -1;
	else
	    return clen;
    }

    /**
     * Does "s" start with "pre", ignoring case?
     */
    private static final boolean isPrefix(String s, String pre) {
	return s.regionMatches(true, 0, pre, 0, pre.length());
    }

    /**
     * Skip over the body of the message looking for a line that starts
     * with "From ".  If found, return the offset of the beginning of
     * that line.  Return -1 on EOF.
     */
    private long skipBody() throws IOException {
	boolean bol = true;
	int lpos = -1;
	long loff = off;
	int b;
	while ((b = get()) >= 0) {
	    if (bol) {
		lpos = 0;
		loff = off - 1;
	    }
	    if (b == '\n') {
		bol = true;
		if (lpos >= 5) {	// have enough data to test?
		    if (line[0] == 'F' && line[1] == 'r' && line[2] == 'o' &&
			line[3] == 'm' && line[4] == ' ')
			return loff;
		}
	    } else {
		bol = false;
		if (lpos < 0)
		    continue;
		if (lpos == 0 && b != 'F')
		    lpos = -1;		// ignore lines that don't start with F
		else if (lpos < 5)	// only need first 5 chars to test
		    line[lpos++] = (char)b;
	    }
	}
	return -1;
    }

    /**
     * Skip "n" bytes, returning how much we were able to skip.
     */
    private final int skip(int n) throws IOException {
	int n0 = n;
	if (pos + n < len) {
	    pos += n;	// can do it all within this buffer
	    off += n;
	} else {
	    do {
		n -= (len - pos);	// skip rest of this buffer
		off += (len - pos);
		fill();
		if (len <= 0)	// ran out of data
		    return n0 - n;
	    } while (n > len);
	    pos += n;
	    off += n;
	}
	return n0;
    }

    /**
     * Return the next byte.
     */
    private final int get() throws IOException {
	if (pos >= len)
	    fill();
	if (pos >= len)
	    return -1;
	else {
	    off++;
	    return buf[pos++] & 0xff;
	}
    }

    /**
     * Fill our buffer with more data.
     * Every buffer we read is also written to the temp file.
     */
    private final void fill() throws IOException {
	len = fis.read(buf);
	pos = 0;
	if (len > 0)
	    fos.write(buf, 0, len);
    }
}
