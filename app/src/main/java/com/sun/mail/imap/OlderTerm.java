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

import java.util.Date;
import javax.mail.Message;
import javax.mail.search.SearchTerm;

/**
 * Find messages that are older than a given interval (in seconds).
 * Relies on the server implementing the WITHIN search extension
 * (<A HREF="http://www.ietf.org/rfc/rfc5032.txt">RFC 5032</A>).
 *
 * @since	JavaMail 1.5.1
 * @author	Bill Shannon
 */
public final class OlderTerm extends SearchTerm {

    private int interval;

    private static final long serialVersionUID = 3951078948727995682L;

    /**
     * Constructor.
     *
     * @param interval	number of seconds older
     */
    public OlderTerm(int interval) {
	this.interval = interval;
    }

    /**
     * Return the interval.
     *
     * @return	the interval
     */
    public int getInterval() {
	return interval;
    }

    /**
     * The match method.
     *
     * @param msg	the date comparator is applied to this Message's
     *			received date
     * @return		true if the comparison succeeds, otherwise false
     */
    @Override
    public boolean match(Message msg) {
	Date d;

	try {
	    d = msg.getReceivedDate();
	} catch (Exception e) {
	    return false;
	}

	if (d == null)
	    return false;

	return d.getTime() <=
		    System.currentTimeMillis() - ((long)interval * 1000);
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof OlderTerm))
	    return false;
	return interval == ((OlderTerm)obj).interval;
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return interval;
    }
}
