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

package com.sun.mail.imap;

import com.sun.mail.iap.*;

/**
 * Information from the APPENDUID response code
 * defined by the UIDPLUS extension -
 * <A HREF="http://www.ietf.org/rfc/rfc4315.txt">RFC 4315</A>.
 *
 * @author  Bill Shannon
 */

public class AppendUID { 
    public long uidvalidity = -1;
    public long uid = -1;

    public AppendUID(long uidvalidity, long uid) {
	this.uidvalidity = uidvalidity;
	this.uid = uid;
    }
}
