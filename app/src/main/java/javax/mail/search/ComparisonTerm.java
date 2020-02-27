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

/**
 * This class models the comparison operator. This is an abstract
 * class; subclasses implement comparisons for different datatypes.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public abstract class ComparisonTerm extends SearchTerm {
    public static final int LE = 1;
    public static final int LT = 2;
    public static final int EQ = 3;
    public static final int NE = 4;
    public static final int GT = 5;
    public static final int GE = 6;

    /**
     * The comparison.
     *
     * @serial
     */
    protected int comparison;

    private static final long serialVersionUID = 1456646953666474308L;

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof ComparisonTerm))
	    return false;
	ComparisonTerm ct = (ComparisonTerm)obj;
	return ct.comparison == this.comparison;
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return comparison;
    }
}
