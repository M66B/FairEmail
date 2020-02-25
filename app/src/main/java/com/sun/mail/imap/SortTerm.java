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

/**
 * A particular sort criteria, as defined by
 * <A HREF="http://www.ietf.org/rfc/rfc5256.txt">RFC 5256</A>.
 * Sort criteria are used with the
 * {@link IMAPFolder#getSortedMessages getSortedMessages} method.
 * Multiple sort criteria are specified in an array with the order in
 * the array specifying the order in which the sort criteria are applied.
 *
 * @since JavaMail 1.4.4
 */
public final class SortTerm {
    /**
     * Sort by message arrival date and time.
     */
    public static final SortTerm ARRIVAL = new SortTerm("ARRIVAL");

    /**
     * Sort by email address of first Cc recipient.
     */
    public static final SortTerm CC = new SortTerm("CC");

    /**
     * Sort by sent date and time.
     */
    public static final SortTerm DATE = new SortTerm("DATE");

    /**
     * Sort by first From email address.
     */
    public static final SortTerm FROM = new SortTerm("FROM");

    /**
     * Reverse the sort order of the following item.
     */
    public static final SortTerm REVERSE = new SortTerm("REVERSE");

    /**
     * Sort by the message size.
     */
    public static final SortTerm SIZE = new SortTerm("SIZE");

    /**
     * Sort by the base subject text.  Note that the "base subject"
     * is defined by RFC 5256 and doesn't include items such as "Re:"
     * in the subject header.
     */
    public static final SortTerm SUBJECT = new SortTerm("SUBJECT");

    /**
     * Sort by email address of first To recipient.
     */
    public static final SortTerm TO = new SortTerm("TO");

    private String term;
    private SortTerm(String term) {
	this.term = term;
    }

    @Override
    public String toString() {
	return term;
    }
}
