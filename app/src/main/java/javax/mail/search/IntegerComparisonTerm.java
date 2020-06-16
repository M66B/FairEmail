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
 * This class implements comparisons for integers.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public abstract class IntegerComparisonTerm extends ComparisonTerm {
    /**
     * The number.
     *
     * @serial
     */
    protected int number;

    private static final long serialVersionUID = -6963571240154302484L;

    protected IntegerComparisonTerm(int comparison, int number) {
	this.comparison = comparison;
	this.number = number;
    }

    /**
     * Return the number to compare with.
     *
     * @return	the number
     */
    public int getNumber() {
	return number;
    }

    /**
     * Return the type of comparison.
     *
     * @return	the comparison type
     */
    public int getComparison() {
	return comparison;
    }

    protected boolean match(int i) {
	switch (comparison) {
	    case LE: 
		return i <= number;
	    case LT:
		return i < number;
	    case EQ:
		return i == number;
	    case NE:
		return i != number;
	    case GT:
		return i > number;
	    case GE:
		return i >= number;
	    default:
		return false;
	}
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof IntegerComparisonTerm))
	    return false;
	IntegerComparisonTerm ict = (IntegerComparisonTerm)obj;
	return ict.number == this.number && super.equals(obj);
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return number + super.hashCode();
    }
}
