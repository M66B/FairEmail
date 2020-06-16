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

import javax.mail.MessagingException;

/**
 * A Message or message Part whose data can be read as a MIME format
 * stream.  Note that the MIME stream will include both the headers
 * and the body of the message or part.  This should be the same data
 * that is produced by the writeTo method, but in a readable form.
 *
 * @author	Bill Shannon
 * @since	JavaMail 1.4.5
 */
public interface ReadableMime {
    /**
     * Return the MIME format stream corresponding to this message part.
     *
     * @return	the MIME format stream
     * @exception	MessagingException for failures
     */
    public InputStream getMimeStream() throws MessagingException;
}
