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

/**
 * This class implements comparisons for Dates
 *
 * @author Bill Shannon
 * @author John Mani
 */
public abstract class DateTerm extends ComparisonTerm {
    /**
     * The date.
     *
     * @serial
     */
    protected Date date;

    private static final long serialVersionUID = 4818873430063720043L;

    /**
     * Constructor.
     * @param comparison the comparison type
     * @param date  The Date to be compared against
     */
    protected DateTerm(int comparison, Date date) {
	this.comparison = comparison;
	this.date = date;
    }

    /**
     * Return the Date to compare with.
     *
     * @return	the date
     */
    public Date getDate() {
	return new Date(date.getTime());
    }

    /**
     * Return the type of comparison.
     *
     * @return	the comparison type
     */
    public int getComparison() {
	return comparison;
    }

    /**
     * The date comparison method.
     *
     * @param d	the date in the constructor is compared with this date
     * @return  true if the dates match, otherwise false
     */
    protected boolean match(Date d) {
	switch (comparison) {
	    case LE: 
		return d.before(date) || d.equals(date);
	    case LT:
		return d.before(date);
	    case EQ:
		return d.equals(date);
	    case NE:
		return !d.equals(date);
	    case GT:
		return d.after(date);
	    case GE:
		return d.after(date) || d.equals(date);
	    default:
		return false;
	}
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof DateTerm))
	    return false;
	DateTerm dt = (DateTerm)obj;
	return dt.date.equals(this.date) && super.equals(obj);
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return date.hashCode() + super.hashCode();
    }
}
