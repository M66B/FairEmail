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

import java.util.Locale;
import javax.mail.Message;

/**
 * This class implements comparisons for Message headers.
 * The comparison is case-insensitive.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class HeaderTerm extends StringTerm {
    /**
     * The name of the header.
     *
     * @serial
     */
    private String headerName;

    private static final long serialVersionUID = 8342514650333389122L;

    /**
     * Constructor.
     *
     * @param headerName The name of the header
     * @param pattern    The pattern to search for
     */
    public HeaderTerm(String headerName, String pattern) {
	super(pattern);
	this.headerName = headerName;
    }

    /**
     * Return the name of the header to compare with.
     *
     * @return	the name of the header
     */
    public String getHeaderName() {
	return headerName;
    }

    /**
     * The header match method.
     *
     * @param msg	The match is applied to this Message's header
     * @return		true if the match succeeds, otherwise false
     */
    @Override
    public boolean match(Message msg) {
	String[] headers;

	try {
	    headers = msg.getHeader(headerName);
	} catch (Exception e) {
	    return false;
	}

	if (headers == null)
	    return false;

	for (int i=0; i < headers.length; i++)
	    if (super.match(headers[i]))
		return true;
	return false;
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof HeaderTerm))
	    return false;
	HeaderTerm ht = (HeaderTerm)obj;
	// XXX - depends on header comparisons being case independent
	return ht.headerName.equalsIgnoreCase(headerName) && super.equals(ht);
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	// XXX - depends on header comparisons being case independent
	return headerName.toLowerCase(Locale.ENGLISH).hashCode() +
					super.hashCode();
    }
}
