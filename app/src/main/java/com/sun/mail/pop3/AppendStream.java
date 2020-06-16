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

package com.sun.mail.pop3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * A stream for writing to the temp file, and when done can return a stream for
 * reading the data just written. NOTE: We assume that only one thread is
 * writing to the file at a time.
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

    @Override
    public void write(int b) throws IOException {
	raf.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
	raf.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
	raf.write(b, off, len);
    }

    @Override
    public synchronized void close() throws IOException {
	end = tf.updateLength();
	raf = null;	// no more writing allowed
    }

    public synchronized InputStream getInputStream() throws IOException {
	return tf.newStream(start, end);
    }
}
