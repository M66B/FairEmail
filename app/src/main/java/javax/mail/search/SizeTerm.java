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
 * This class implements comparisons for Message sizes.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class SizeTerm extends IntegerComparisonTerm {

    private static final long serialVersionUID = -2556219451005103709L;

    /**
     * Constructor.
     *
     * @param comparison	the Comparison type
     * @param size		the size
     */
    public SizeTerm(int comparison, int size) {
	super(comparison, size);
    }

    /**
     * The match method.
     *
     * @param msg	the size comparator is applied to this Message's size
     * @return		true if the size is equal, otherwise false 
     */
    @Override
    public boolean match(Message msg) {
	int size;

	try {
	    size = msg.getSize();
	} catch (Exception e) {
	    return false;
	}

	if (size == -1)
	    return false;

	return super.match(size);
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof SizeTerm))
	    return false;
	return super.equals(obj);
    }
}
