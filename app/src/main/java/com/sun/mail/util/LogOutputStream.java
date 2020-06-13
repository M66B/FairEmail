/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

/**
 * Capture output lines and send them to the mail logger.
 */
public class LogOutputStream extends OutputStream {
    protected MailLogger logger;
    protected Level level;

    private int lastb = -1;
    private byte[] buf = new byte[80];
    private int pos = 0;

    /**
     * Log to the specified logger.
     *
     * @param	logger	the MailLogger
     */
    public LogOutputStream(MailLogger logger) {
	this.logger = logger;
	this.level = Level.FINEST;
    }

    @Override
    public void write(int b) throws IOException {
	if (!logger.isLoggable(level))
	    return;

	if (b == '\r') {
	    logBuf();
	} else if (b == '\n') {
	    if (lastb != '\r')
		logBuf();
	} else {
	    expandCapacity(1);
	    buf[pos++] = (byte)b;
	}
	lastb = b;
    }

    @Override
    public void write(byte b[]) throws IOException {
	write(b, 0, b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
	int start = off;
	
	if (!logger.isLoggable(level))
	    return;
	len += off;
	for (int i = start; i < len ; i++) {
	    if (b[i] == '\r') {
		expandCapacity(i - start);
		System.arraycopy(b, start, buf, pos, i - start);
		pos += i - start;
		logBuf();
		start = i + 1;
	    } else if (b[i] == '\n') {
		if (lastb != '\r') {
		    expandCapacity(i - start);
		    System.arraycopy(b, start, buf, pos, i - start);
		    pos += i - start;
		    logBuf();
		}
		start = i + 1;
	    }
	    lastb = b[i];
	}
	if ((len - start) > 0) {
	    expandCapacity(len - start);
	    System.arraycopy(b, start, buf, pos, len - start);
	    pos += len - start;
	}
    }

    /**
     * Log the specified message.
     * Can be overridden by subclass to do different logging.
     *
     * @param	msg	the message to log
     */
    protected void log(String msg) {
	logger.log(level, msg);
    }

    /**
     * Convert the buffer to a string and log it.
     */
    private void logBuf() {
	String msg = new String(buf, 0, pos);
	pos = 0;
	log(msg);
    }

    /**
     * Ensure that the buffer can hold at least len bytes
     * beyond the current position.
     */
    private void expandCapacity(int len) {
	while (pos + len > buf.length) {
	    byte[] nb = new byte[buf.length * 2];
	    System.arraycopy(buf, 0, nb, 0, pos);
	    buf = nb;
	}
    }
}
