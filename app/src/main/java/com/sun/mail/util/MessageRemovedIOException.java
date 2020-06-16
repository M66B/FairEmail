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

import java.io.IOException;

/**
 * A variant of MessageRemovedException that can be thrown from methods
 * that only throw IOException.  The getContent method will catch this
 * exception and translate it back to MessageRemovedException.
 *
 * @see	   javax.mail.Message#isExpunged()
 * @see	   javax.mail.Message#getMessageNumber()
 * @author Bill Shannon
 */

public class MessageRemovedIOException extends IOException {

    private static final long serialVersionUID = 4280468026581616424L;

    /**
     * Constructs a MessageRemovedIOException with no detail message.
     */
    public MessageRemovedIOException() {
	super();
    }

    /**
     * Constructs a MessageRemovedIOException with the specified detail message.
     * @param s		the detail message
     */
    public MessageRemovedIOException(String s) {
	super(s);
    }
}
