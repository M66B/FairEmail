/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
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

import javax.mail.Provider;

import com.sun.mail.util.DefaultProvider;

/**
 * The POP3 SSL protocol provider.
 */
@DefaultProvider	// Remove this annotation if you copy this provider
public class POP3SSLProvider extends Provider {
    public POP3SSLProvider() {
	super(Provider.Type.STORE, "pop3s", POP3SSLStore.class.getName(),
	    "Oracle", null);
    }
}
