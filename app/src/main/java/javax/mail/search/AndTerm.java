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
 * This class implements the logical AND operator on individual
 * SearchTerms.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class AndTerm extends SearchTerm {

    /**
     * The array of terms on which the AND operator should be
     * applied.
     *
     * @serial
     */
    private SearchTerm[] terms;

    private static final long serialVersionUID = -3583274505380989582L;

    /**
     * Constructor that takes two terms.
     * 
     * @param t1 first term
     * @param t2 second term
     */
    public AndTerm(SearchTerm t1, SearchTerm t2) {
	terms = new SearchTerm[2];
	terms[0] = t1;
	terms[1] = t2;
    }

    /**
     * Constructor that takes an array of SearchTerms.
     * 
     * @param t  array of terms
     */
    public AndTerm(SearchTerm[] t) {
	terms = new SearchTerm[t.length]; // clone the array
	for (int i = 0; i < t.length; i++)
	    terms[i] = t[i];
    }

    /**
     * Return the search terms.
     *
     * @return	the search terms
     */
    public SearchTerm[] getTerms() {
	return terms.clone();
    }

    /**
     * The AND operation. <p>
     *
     * The terms specified in the constructor are applied to
     * the given object and the AND operator is applied to their results.
     *
     * @param msg	The specified SearchTerms are applied to this Message
     *			and the AND operator is applied to their results.
     * @return		true if the AND succeds, otherwise false
     */
    @Override
    public boolean match(Message msg) {
	for (int i=0; i < terms.length; i++)
	    if (!terms[i].match(msg))
		return false;
	return true;
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof AndTerm))
	    return false;
	AndTerm at = (AndTerm)obj;
	if (at.terms.length != terms.length)
	    return false;
	for (int i=0; i < terms.length; i++)
	    if (!terms[i].equals(at.terms[i]))
		return false;
	return true;
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	int hash = 0;
	for (int i=0; i < terms.length; i++)
	    hash += terms[i].hashCode();
	return hash;
    }
}
