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
 * This class implements string comparisons for the Recipient Address
 * headers. <p>
 *
 * Note that this class differs from the <code>RecipientTerm</code> class
 * in that this class does comparisons on address strings rather than Address
 * objects. The string comparisons are case-insensitive.
 *
 * @since       JavaMail 1.1
 */

public final class RecipientStringTerm extends AddressStringTerm {

    /**
     * The recipient type.
     *
     * @serial
     */
    private Message.RecipientType type;

    private static final long serialVersionUID = -8293562089611618849L;

    /**
     * Constructor.
     *
     * @param type      the recipient type
     * @param pattern   the address pattern to be compared.
     */
    public RecipientStringTerm(Message.RecipientType type, String pattern) {
	super(pattern);
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
     * Check whether the address specified in the constructor is
     * a substring of the recipient address of this Message.
     *
     * @param   msg 	The comparison is applied to this Message's recipient
     *		    	address.
     * @return          true if the match succeeds, otherwise false.
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
	if (!(obj instanceof RecipientStringTerm))
	    return false;
	RecipientStringTerm rst = (RecipientStringTerm)obj;
	return rst.type.equals(this.type) && super.equals(obj);
    }

    /**
     * Compute a hashCode for this object.
     */
    @Override
    public int hashCode() {
	return type.hashCode() + super.hashCode();
    }
}
