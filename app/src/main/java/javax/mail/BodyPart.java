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

package javax.mail;

/**
 * This class models a Part that is contained within a Multipart.
 * This is an abstract class. Subclasses provide actual implementations.<p>
 *
 * BodyPart implements the Part interface. Thus, it contains a set of
 * attributes and a "content".
 *
 * @author John Mani
 * @author Bill Shannon
 */

public abstract class BodyPart implements Part {

    /**
     * The <code>Multipart</code> object containing this <code>BodyPart</code>,
     * if known.
     * @since	JavaMail 1.1
     */
    protected Multipart parent;

    /**
     * Return the containing <code>Multipart</code> object,
     * or <code>null</code> if not known.
     *
     * @return	the parent Multipart
     */
    public Multipart getParent() {
	return parent;
    }

    /**
     * Set the parent of this <code>BodyPart</code> to be the specified
     * <code>Multipart</code>.  Normally called by <code>Multipart</code>'s
     * <code>addBodyPart</code> method.  <code>parent</code> may be
     * <code>null</code> if the <code>BodyPart</code> is being removed
     * from its containing <code>Multipart</code>.
     * @since	JavaMail 1.1
     */
    void setParent(Multipart parent) {
	this.parent = parent;
    }
}
