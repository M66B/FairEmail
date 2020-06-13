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
import javax.mail.Address;

/**
 * This class implements comparisons for the Recipient Address headers.
 *
 * @author Bill Shannon
 * @author John Mani
 */
public final class RecipientTerm extends AddressTerm {

    /**
     * The recipient type.
     *
     * @serial
     */
    private Message.RecipientType type;

    private static final long serialVersionUID = 6548700653122680468L;

    /**
     * Constructor.
     *
     * @param type	the recipient type
     * @param address	the address to match for
     */
    public RecipientTerm(Message.RecipientType type, Address address) {
	super(address);
	this.type = type;
    }

    /**
     * Return the type of recipient to match with.
     *
     * @return	the recipient type
     */
    public Message.RecipientType getRecipientType() {
	return type;
    }

    /**
     * The match method.
     *
     * @param msg	The address match is applied to this Message's recepient
     *			address
     * @return		true if the match succeeds, otherwise false
     */
    @Override
    public boolean match(Message msg) {
	Address[] recipients;

	try {
 	    recipients = msg.getRecipients(type);
	} catch (Exception e) {
	    return false;
	}

	if (recipients == null)
	    return false;

	for (int i=0; i < recipients.length; i++)
	    if (super.match(recipients[i]))
		return true;
	return false;
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof RecipientTerm))
	    return false;
	RecipientTerm rt = (RecipientTerm)obj;
	return rt.type.equals(this.type) && super.equals(obj);
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return type.hashCode() + super.hashCode();
    }
}
