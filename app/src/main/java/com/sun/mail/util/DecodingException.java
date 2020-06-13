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
 * A special IOException that indicates a failure to decode data due
 * to an error in the formatting of the data.  This allows applications
 * to distinguish decoding errors from other I/O errors.
 *
 * @author Bill Shannon
 */

public class DecodingException extends IOException {

    private static final long serialVersionUID = -6913647794421459390L;

    /**
     * Constructor.
     *
     * @param	s	the exception message
     */
    public DecodingException(String s) {
	super(s);
    }
}
