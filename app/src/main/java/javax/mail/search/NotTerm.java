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
 * This class implements the logical NEGATION operator.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class NotTerm extends SearchTerm {
    /**
     * The search term to negate.
     *
     * @serial
     */
    private SearchTerm term;

    private static final long serialVersionUID = 7152293214217310216L;

    public NotTerm(SearchTerm t) {
	term = t;
    }

    /**
     * Return the term to negate.
     *
     * @return	the Term
     */
    public SearchTerm getTerm() {
	return term;
    }

    /* The NOT operation */
    @Override
    public boolean match(Message msg) {
	return !term.match(msg);
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof NotTerm))
	    return false;
	NotTerm nt = (NotTerm)obj;
	return nt.term.equals(this.term);
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return term.hashCode() << 1;
    }
}
