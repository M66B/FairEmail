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

import com.sun.mail.iap.ProtocolException;

/**
 * A ProtocolException that includes IMAP login referral information.
 *
 * @since JavaMail 1.5.5
 */

public class IMAPReferralException extends ProtocolException {

    private String url;

    private static final long serialVersionUID = 2578770669364251968L;

    /**
     * Constructs an IMAPReferralException with the specified detail message.
     * and URL.
     *
     * @param s		the detail message
     * @param url	the URL
     */
    public IMAPReferralException(String s, String url) {
	super(s);
	this.url = url;
    }

    /**
     * Return the IMAP URL in the referral.
     *
     * @return	the IMAP URL
     */
    public String getUrl() {
	return url;
    }
}
