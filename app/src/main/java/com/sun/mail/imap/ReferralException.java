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

import javax.mail.AuthenticationFailedException;

/**
 * A special kind of AuthenticationFailedException that indicates that
 * the reason for the failure was an IMAP REFERRAL in the response code.
 * See <a href="http://www.ietf.org/rfc/rfc2221.txt">RFC 2221</a> for details.
 *
 * @since JavaMail 1.5.5
 */

public class ReferralException extends AuthenticationFailedException {

    private String url;
    private String text;

    private static final long serialVersionUID = -3414063558596287683L;

    /**
     * Constructs an ReferralException with the specified URL and text.
     *
     * @param text	the detail message
     * @param url	the URL
     */
    public ReferralException(String url, String text) {
	super("[REFERRAL " + url + "] " + text);
	this.url = url;
	this.text = text;
    }

    /**
     * Return the IMAP URL in the referral.
     *
     * @return	the IMAP URL
     */
    public String getUrl() {
	return url;
    }

    /**
     * Return the text sent by the server along with the referral.
     *
     * @return	the text
     */
    public String getText() {
	return text;
    }
}
