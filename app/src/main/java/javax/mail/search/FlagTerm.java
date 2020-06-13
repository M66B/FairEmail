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

import javax.mail.*;

/**
 * This class implements comparisons for Message Flags.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class FlagTerm extends SearchTerm {

    /**
     * Indicates whether to test for the presence or
     * absence of the specified Flag. If <code>true</code>,
     * then test whether all the specified flags are present, else
     * test whether all the specified flags are absent.
     *
     * @serial
     */
    private boolean set;

    /**
     * Flags object containing the flags to test.
     *
     * @serial
     */
    private Flags flags;

    private static final long serialVersionUID = -142991500302030647L;

    /**
     * Constructor.
     *
     * @param flags	Flags object containing the flags to check for
     * @param set	the flag setting to check for
     */
    public FlagTerm(Flags flags, boolean set) {
	this.flags = flags;
	this.set = set;
    }

    /**
     * Return the Flags to test.
     *
     * @return	the flags
     */
    public Flags getFlags() {
	return (Flags)flags.clone();
    }

    /**
     * Return true if testing whether the flags are set.
     *
     * @return	true if testing whether the flags are set
     */
    public boolean getTestSet() {
	return set;
    }

    /**
     * The comparison method.
     *
     * @param msg	The flag comparison is applied to this Message
     * @return		true if the comparson succeeds, otherwise false.
     */
    @Override
    public boolean match(Message msg) {

	try {
	    Flags f = msg.getFlags();
	    if (set) { // This is easy
		if (f.contains(flags))
		    return true;
		else 
		    return false;
	    }

	    // Return true if ALL flags in the passed in Flags
	    // object are NOT set in this Message.

	    // Got to do this the hard way ...
	    Flags.Flag[] sf = flags.getSystemFlags();

	    // Check each flag in the passed in Flags object
	    for (int i = 0; i < sf.length; i++) {
		if (f.contains(sf[i]))
		    // this flag IS set in this Message, get out.
		    return false;
	    }

	    String[] s = flags.getUserFlags();

	    // Check each flag in the passed in Flags object
	    for (int i = 0; i < s.length; i++) {
		if (f.contains(s[i]))
		    // this flag IS set in this Message, get out.
		    return false;
	    }

	    return true;

	} catch (MessagingException e) {
	    return false;
	} catch (RuntimeException e) {
	    return false;
	}
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof FlagTerm))
	    return false;
	FlagTerm ft = (FlagTerm)obj;
	return ft.set == this.set && ft.flags.equals(this.flags);
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return set ? flags.hashCode() : ~flags.hashCode();
    }
}
