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

import java.io.*;

/**
 * A temporary file used to cache POP3 messages.
 */
class TempFile {

    private File file;	// the temp file name
    private WritableSharedFile sf;

    /**
     * Create a temp file in the specified directory (if not null).
     * The file will be deleted when the JVM exits.
     */
    public TempFile(File dir) throws IOException {
	file = File.createTempFile("pop3.", ".mbox", dir);
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

    @Override
    protected void finalize() throws Throwable {
	try {
	    close();
	} finally {
	    super.finalize();
	}
    }
}
