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
 * This class implements the match method for Strings. The current
 * implementation provides only for substring matching. We
 * could add comparisons (like strcmp ...).
 *
 * @author Bill Shannon
 * @author John Mani
 */
public abstract class StringTerm extends SearchTerm {
    /**
     * The pattern.
     *
     * @serial
     */
    protected String pattern;

    /**
     * Ignore case when comparing?
     *
     * @serial
     */
    protected boolean ignoreCase;

    private static final long serialVersionUID = 1274042129007696269L;

    /**
     * Construct a StringTerm with the given pattern.
     * Case will be ignored.
     *
     * @param	pattern		the pattern
     */
    protected StringTerm(String pattern) {
	this.pattern = pattern;
	ignoreCase = true;
    }

    /**
     * Construct a StringTerm with the given pattern and ignoreCase flag.
     *
     * @param	pattern		the pattern
     * @param	ignoreCase	should we ignore case?
     */
    protected StringTerm(String pattern, boolean ignoreCase) {
	this.pattern = pattern;
	this.ignoreCase = ignoreCase;
    }

    /**
     * Return the string to match with.
     *
     * @return	the string to match
     */
    public String getPattern() {
	return pattern;
    }

    /**
     * Return true if we should ignore case when matching.
     *
     * @return	true if we should ignore case
     */
    public boolean getIgnoreCase() {
	return ignoreCase;
    }

    protected boolean match(String s) {
	int len = s.length() - pattern.length();
	for (int i=0; i <= len; i++) {
	    if (s.regionMatches(ignoreCase, i, 
				pattern, 0, pattern.length()))
		return true;
	}
	return false;
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof StringTerm))
	    return false;
	StringTerm st = (StringTerm)obj;
	if (ignoreCase)
	    return st.pattern.equalsIgnoreCase(this.pattern) &&
		    st.ignoreCase == this.ignoreCase;
	else
	    return st.pattern.equals(this.pattern) &&
		    st.ignoreCase == this.ignoreCase;
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return ignoreCase ? pattern.hashCode() : ~pattern.hashCode();
    }
}
