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

package com.sun.mail.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class is to support writing out Strings as a sequence of bytes
 * terminated by a CRLF sequence. The String must contain only US-ASCII
 * characters.<p>
 *
 * The expected use is to write out RFC822 style headers to an output
 * stream. <p>
 *
 * @author John Mani
 * @author Bill Shannon
 */

public class LineOutputStream extends FilterOutputStream {
    private boolean allowutf8;

    private static byte[] newline;

    static {
	newline = new byte[2];
	newline[0] = (byte)'\r';
	newline[1] = (byte)'\n';
    }

    public LineOutputStream(OutputStream out) {
	this(out, false);
    }

    /**
     * @param	out	the OutputStream
     * @param	allowutf8	allow UTF-8 characters?
     * @since	JavaMail 1.6
     */
    public LineOutputStream(OutputStream out, boolean allowutf8) {
	super(out);
	this.allowutf8 = allowutf8;
    }

    public void writeln(String s) throws IOException {
	byte[] bytes;
	if (allowutf8)
	    bytes = s.getBytes(StandardCharsets.UTF_8);
	else
	    bytes = ASCIIUtility.getBytes(s);
	out.write(bytes);
	out.write(newline);
    }

    public void writeln() throws IOException {
	out.write(newline);
    }
}
