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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.mail.util.SharedFileInputStream;

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
    @Override
    public void close() throws IOException {
	try {
	    super.close();
	} finally {
	    raf.close();
	}
    }

    /**
     * Update the size of the readable file after writing to the file. Updates
     * the length to be the current size of the file.
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
	if (af != null) {
	    throw new IOException(
		    "POP3 file cache only supports single threaded access");
	}
	af = new AppendStream(this);
	return af;
    }
}
