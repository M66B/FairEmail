/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import javax.mail.util.SharedByteArrayInputStream;

/**
 * A ByteArrayOutputStream that allows us to share the byte array
 * rather than copy it.  Eventually could replace this with something
 * that doesn't require a single contiguous byte array.
 *
 * @author	Bill Shannon
 * @since	JavaMail 1.4.5
 */
public class SharedByteArrayOutputStream extends ByteArrayOutputStream {
    public SharedByteArrayOutputStream(int size) {
	super(size);
    }

    public InputStream toStream() {
	return new SharedByteArrayInputStream(buf, 0, count);
    }
}
