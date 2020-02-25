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

package com.sun.mail.imap.protocol;

import com.sun.mail.iap.*; 

/**
 * An RFC822SIZE FETCH item.
 *
 * @author  John Mani
 */

public class RFC822SIZE implements Item {
    
    static final char[] name = {'R','F','C','8','2','2','.','S','I','Z','E'};
    public int msgno;

    public long size;

    /**
     * Constructor.
     *
     * @param	r	the FetchResponse
     * @exception	ParsingException	for parsing failures
     */
    public RFC822SIZE(FetchResponse r) throws ParsingException {
	msgno = r.getNumber();
	r.skipSpaces();
	size = r.readLong();		
    }
}
