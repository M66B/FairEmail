/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;
import java.net.*;
import java.io.*;
import java.security.*;

import com.sun.mail.util.PropUtil;
import javax.mail.util.SharedFileInputStream;

/**
 * A temporary file used to cache messages.
 */
class TempFile {

    private File file;	// the temp file name
    private WritableSharedFile sf;

    /**
     * Create a temp file in the specified directory (if not null).
     * The file will be deleted when the JVM exits.
     */
    public TempFile(File dir) throws IOException {
	file = File.createTempFile("mbox.", ".mbox", dir);
	// XXX - need JDK 6 to set permissions on the file to owner-only
	file.deleteOnExit();
	sf = new WritableSharedFile(file);
    }

    /**
     * Return a stream for appending to the temp file.
     */
    public AppendStream getAppendStream() throws IOException {
	return sf.getAppendStream();
    }

    /**
     * Return a stream for reading from part of the file.
     */
    public InputStream newStream(long start, long end) {
	return sf.newStream(start, end);
    }

    public long length() {
	return file.length();
    }

    /**
     * Close and remove this temp file.
     */
    public void close() {
	try {
	    sf.close();
	} catch (IOException ex) {
	    // ignore it
	}
	file.delete();
    }

    protected void finalize() throws Throwable {
	try {
	    close();
	} finally {
	    super.finalize();
	}
    }
}

/**
 * A subclass of SharedFileInputStream that also allows writing.
 */
class WritableSharedFile extends SharedFileInputStream {
    private RandomAccessFile raf;
    private AppendStream af;

    public WritableSharedFile(File file) throws IOException {
	super(file);
	try {
	    raf = new RandomAccessFile(file, "rw");
	} catch (IOException ex) {
	    // if anything goes wrong opening the writable file,
	    // close the readable file too
	    super.close();
	}
    }

    /**
     * Return the writable version of this file.
     */
    public RandomAccessFile getWritableFile() {
	return raf;
    }

    /**
     * Close the readable and writable files.
     */
    public void close() throws IOException {
	try {
	    super.close();
	} finally {
	    raf.close();
	}
    }

    /**
     * Update the size of the readable file after writing
     * to the file.  Updates the length to be the current
     * size of the file.
     */
    synchronized long updateLength() throws IOException {
	datalen = in.length();
	af = null;
	return datalen;
    }

    /**
     * Return a new AppendStream, but only if one isn't in active use.
     */
    public synchronized AppendStream getAppendStream() throws IOException {
	if (af != null)
	    throw new IOException(
		"file cache only supports single threaded access");
	af = new AppendStream(this);
	return af;
    }
}

/**
 * A stream for writing to the temp file, and when done
 * can return a stream for reading the data just written.
 * NOTE: We assume that only one thread is writing to the
 * file at a time.
 */
class AppendStream extends OutputStream {
    private final WritableSharedFile tf;
    private RandomAccessFile raf;
    private final long start;
    private long end;

    public AppendStream(WritableSharedFile tf) throws IOException {
	this.tf = tf;
	raf = tf.getWritableFile();
	start = raf.length();
	raf.seek(start);
    }

    public void write(int b) throws IOException {
	raf.write(b);
    }

    public void write(byte[] b) throws IOException {
	raf.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
	raf.write(b, off, len);
    }

    public synchronized void close() throws IOException {
	end = tf.updateLength();
	raf = null;	// no more writing allowed
    }

    public synchronized InputStream getInputStream() throws IOException {
	return tf.newStream(start, end);
    }
}
