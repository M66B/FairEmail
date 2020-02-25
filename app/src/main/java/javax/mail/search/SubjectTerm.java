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
 * This class implements comparisons for the message Subject header.
 * The comparison is case-insensitive.  The pattern is a simple string
 * that must appear as a substring in the Subject.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class SubjectTerm extends StringTerm {

    private static final long serialVersionUID = 7481568618055573432L;

    /**
     * Constructor.
     *
     * @param pattern  the pattern to search for
     */
    public SubjectTerm(String pattern) {
	// Note: comparison is case-insensitive
	super(pattern);
    }

    /**
     * The match method.
     *
     * @param msg	the pattern match is applied to this Message's 
     *			subject header
     * @return		true if the pattern match succeeds, otherwise false
     */
    @Override
    public boolean match(Message msg) {
	String subj;

	try {
	    subj = msg.getSubject();
	} catch (Exception e) {
	    return false;
	}

	if (subj == null)
	    return false;

	return super.match(subj);
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof SubjectTerm))
	    return false;
	return super.equals(obj);
    }
}
