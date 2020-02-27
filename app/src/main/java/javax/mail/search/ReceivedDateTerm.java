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

import java.util.Date;
import javax.mail.Message;

/**
 * This class implements comparisons for the Message Received date
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class ReceivedDateTerm extends DateTerm {

    private static final long serialVersionUID = -2756695246195503170L;

    /**
     * Constructor.
     *
     * @param comparison	the Comparison type
     * @param date		the date to be compared
     */
    public ReceivedDateTerm(int comparison, Date date) {
	super(comparison, date);
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

	return super.match(d);
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof ReceivedDateTerm))
	    return false;
	return super.equals(obj);
    }
}
