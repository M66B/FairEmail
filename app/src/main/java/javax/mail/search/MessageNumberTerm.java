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

package javax.mail.search;

import javax.mail.Message;

/**
 * This class implements comparisons for Message numbers.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class MessageNumberTerm extends IntegerComparisonTerm {

    private static final long serialVersionUID = -5379625829658623812L;

    /**
     * Constructor.
     *
     * @param number  the Message number
     */
    public MessageNumberTerm(int number) {
	super(EQ, number);
    }

    /**
     * The match method.
     *
     * @param msg	the Message number is matched with this Message
     * @return		true if the match succeeds, otherwise false
     */
    @Override
    public boolean match(Message msg) {
	int msgno;

	try {
	    msgno = msg.getMessageNumber();
	} catch (Exception e) {
	    return false;
	}
	
	return super.match(msgno);
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof MessageNumberTerm))
	    return false;
	return super.equals(obj);
    }
}
