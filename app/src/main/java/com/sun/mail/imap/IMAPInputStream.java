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

package com.sun.mail.imap;

import java.io.*;
import javax.mail.*;
import com.sun.mail.imap.protocol.*;
import com.sun.mail.iap.*;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MessageRemovedIOException;

/**
 * This class implements an IMAP data stream.
 *
 * @author  John Mani
 */

public class IMAPInputStream extends InputStream {
    private IMAPMessage msg; // this message
    private String section;  // section-id
    private int pos;	  // track the position within the IMAP datastream
    private int blksize;  // number of bytes to read in each FETCH request
    private int max;	  // the total number of bytes in this section.
			  //  -1 indicates unknown
    private byte[] buf;   // the buffer obtained from fetchBODY()
    private int bufcount; // The index one greater than the index of the
			  // last valid byte in 'buf'
    private int bufpos;   // The current position within 'buf'
    private boolean lastBuffer; // is this the last buffer of data?
    private boolean peek; // peek instead of fetch?
    private ByteArray readbuf; // reuse for each read

    // Allocate this much extra space in the read buffer to allow
    // space for the FETCH response overhead
    private static final int slop = 64;


    /**
     * Create an IMAPInputStream.
     *
     * @param	msg	the IMAPMessage the data will come from
     * @param	section	the IMAP section/part identifier for the data
     * @param	max	the number of bytes in this section
     * @param	peek	peek instead of fetch?
     */
    public IMAPInputStream(IMAPMessage msg, String section, int max,
				boolean peek) {
	this.msg = msg;
	this.section = section;
	this.max = max;
	this.peek = peek;
	pos = 0;
	blksize = msg.getFetchBlockSize();
    }

    /**
     * Do a NOOP to force any untagged EXPUNGE responses
     * and then check if this message is expunged.
     */
    private void forceCheckExpunged()
		    throws MessageRemovedIOException, FolderClosedIOException {
	synchronized (msg.getMessageCacheLock()) {
	    try {
		msg.getProtocol().noop();
	    } catch (ConnectionException cex) {
		throw new FolderClosedIOException(msg.getFolder(),
						cex.getMessage());
	    } catch (FolderClosedException fex) {
		throw new FolderClosedIOException(fex.getFolder(),
						fex.getMessage());
	    } catch (ProtocolException pex) {
		// ignore it
	    }
	}
	if (msg.isExpunged())
	    throw new MessageRemovedIOException();
    }

    /**
     * Fetch more data from the server. This method assumes that all
     * data has already been read in, hence bufpos > bufcount.
     */
    private void fill() throws IOException {
	/*
	 * If we've read the last buffer, there's no more to read.
	 * If we know the total number of bytes available from this
	 * section, let's check if we have consumed that many bytes.
	 */
	if (lastBuffer || max != -1 && pos >= max) {
	    if (pos == 0)
		checkSeen();
	    readbuf = null;	// XXX - return to pool?
	    return; // the caller of fill() will return -1.
	}

	BODY b = null;
	if (readbuf == null)
	    readbuf = new ByteArray(blksize + slop);

	ByteArray ba;
	int cnt;
	// Acquire MessageCacheLock, to freeze seqnum.
	synchronized (msg.getMessageCacheLock()) {
	    try {
		IMAPProtocol p = msg.getProtocol();

		// Check whether this message is expunged
		if (msg.isExpunged())
		    throw new MessageRemovedIOException(
				"No content for expunged message");

		int seqnum = msg.getSequenceNumber();
		cnt = blksize;
		if (max != -1 && pos + blksize > max)
		    cnt = max - pos;
		if (peek)
		    b = p.peekBody(seqnum, section, pos, cnt, readbuf);
		else
		    b = p.fetchBody(seqnum, section, pos, cnt, readbuf);
	    } catch (ProtocolException pex) {
		forceCheckExpunged();
		throw new IOException(pex.getMessage());
	    } catch (FolderClosedException fex) {
		throw new FolderClosedIOException(fex.getFolder(),
						fex.getMessage());
	    }

	    if (b == null || ((ba = b.getByteArray()) == null)) {
		forceCheckExpunged();
		// nope, the server doesn't think it's expunged.
		// can't tell the difference between the server returning NIL
		// and some other error that caused null to be returned above,
		// so we'll just assume it was empty content.
		ba = new ByteArray(0);
	    }
	}

	// make sure the SEEN flag is set after reading the first chunk
	if (pos == 0)
	    checkSeen();

	// setup new values ..
	buf = ba.getBytes();
	bufpos = ba.getStart();
	int n = ba.getCount();    // will be zero, if all data has been
				  // consumed from the server.

	int origin = b != null ? b.getOrigin() : pos;
	if (origin < 0) {
	    /*
	     * Some versions of Exchange will return the entire message
	     * body even though we only ask for a chunk, and the returned
	     * data won't specify an "origin".  If this happens, and we
	     * get more data than we asked for, assume it's the entire
	     * message body.
	     */
	    if (pos == 0) {
		/*
		 * If we got more or less than we asked for,
		 * this is the last buffer of data.
		 */
		lastBuffer = n != cnt;
	    } else {
		/*
		 * We asked for data NOT starting at the beginning,
		 * but we got back data starting at the beginning.
		 * Possibly we could extract the needed data from
		 * some part of the data we got back, but really this
		 * should never happen so we just assume something is
		 * broken and terminate the data here.
		 */
		n = 0;
		lastBuffer = true;
	    }
	} else if (origin == pos) {
	    /*
	     * If we got less than we asked for,
	     * this is the last buffer of data.
	     */
	    lastBuffer = n < cnt;
	} else {
	    /*
	     * We got back data that doesn't match the request.
	     * Just terminate the data here.
	     */
	    n = 0;
	    lastBuffer = true;
	}

	bufcount = bufpos + n;
	pos += n;

    }

    /**
     * Reads the next byte of data from this buffered input stream.
     * If no byte is available, the value <code>-1</code> is returned.
     */
    @Override
    public synchronized int read() throws IOException {
	if (bufpos >= bufcount) {
	    fill();
	    if (bufpos >= bufcount)
		return -1;	// EOF
	}
	return buf[bufpos++] & 0xff;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this
     * input stream into the given buffer. <p>
     *
     * Returns the total number of bytes read into the buffer,
     * or <code>-1</code> if there is no more data. <p>
     *
     * Note that this method mimics the "weird !" semantics of
     * BufferedInputStream in that the number of bytes actually
     * returned may be less that the requested value. So callers
     * of this routine should be aware of this and must check
     * the return value to insure that they have obtained the
     * requisite number of bytes.
     */
    @Override
    public synchronized int read(byte b[], int off, int len) 
		throws IOException {

	int avail = bufcount - bufpos;
	if (avail <= 0) {
	    fill();
	    avail = bufcount - bufpos;
	    if (avail <= 0)
		return -1; // EOF
	}
	int cnt = (avail < len) ? avail : len;
	System.arraycopy(buf, bufpos, b, off, cnt);
	bufpos += cnt;
	return cnt;
    }

    /**
     * Reads up to <code>b.length</code> bytes of data from this input
     * stream into an array of bytes. <p>
     *
     * Returns the total number of bytes read into the buffer, or
     * <code>-1</code> is there is no more data. <p>
     *
     * Note that this method mimics the "weird !" semantics of
     * BufferedInputStream in that the number of bytes actually
     * returned may be less that the requested value. So callers
     * of this routine should be aware of this and must check
     * the return value to insure that they have obtained the
     * requisite number of bytes.
     */
    @Override
    public int read(byte b[]) throws IOException {
	return read(b, 0, b.length);
    }

    /**
     * Returns the number of bytes that can be read from this input
     * stream without blocking.
     */
    @Override
    public synchronized int available() throws IOException {
	return (bufcount - bufpos);
    }

    /**
     * Normally the SEEN flag will have been set by now, but if not,
     * force it to be set (as long as the folder isn't open read-only
     * and we're not peeking).
     * And of course, if there's no folder (e.g., a nested message)
     * don't do anything.
     */
    private void checkSeen() {
	if (peek)	// if we're peeking, don't set the SEEN flag
	    return;
	try {
	    Folder f = msg.getFolder();
	    if (f != null && f.getMode() != Folder.READ_ONLY &&
		    !msg.isSet(Flags.Flag.SEEN))
		msg.setFlag(Flags.Flag.SEEN, true);
	} catch (MessagingException ex) {
	    // ignore it
	}
    }
}
