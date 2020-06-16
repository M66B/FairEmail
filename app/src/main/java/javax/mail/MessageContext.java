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
 * The context in which a piece of Message content is contained.  A
 * <code>MessageContext</code> object is returned by the
 * <code>getMessageContext</code> method of the
 * <code>MessageAware</code> interface.  <code>MessageAware</code> is
 * typically implemented by <code>DataSources</code> to allow a
 * <code>DataContentHandler</code> to pass on information about the
 * context in which a data content object is operating.
 *
 * @see javax.mail.MessageAware
 * @see javax.activation.DataSource
 * @see javax.activation.DataContentHandler
 * @since	JavaMail 1.1
 */
public class MessageContext {
    private Part part;

    /**
     * Create a MessageContext object describing the context of the given Part.
     *
     * @param	part	the Part
     */
    public MessageContext(Part part) {
	this.part = part;
    }

    /**
     * Return the Part that contains the content.
     *
     * @return	the containing Part, or null if not known
     */
    public Part getPart() {
	return part;
    }

    /**
     * Return the Message that contains the content.
     * Follows the parent chain up through containing Multipart
     * objects until it comes to a Message object, or null.
     *
     * @return	the containing Message, or null if not known
     */
    public Message getMessage() {
	try {
	    return getMessage(part);
	} catch (MessagingException ex) {
	    return null;
	}
    }

    /**
     * Return the Message containing an arbitrary Part.
     * Follows the parent chain up through containing Multipart
     * objects until it comes to a Message object, or null.
     *
     * @return	the containing Message, or null if none
     * @see javax.mail.BodyPart#getParent
     * @see javax.mail.Multipart#getParent
     */
    private static Message getMessage(Part p) throws MessagingException {
	while (p != null) {
	    if (p instanceof Message)
		return (Message)p;
	    BodyPart bp = (BodyPart)p;
	    Multipart mp = bp.getParent();
	    if (mp == null)	// MimeBodyPart might not be in a MimeMultipart
		return null;
	    p = mp.getParent();
	}
	return null;
    }

    /**
     * Return the Session we're operating in.
     *
     * @return	the Session, or null if not known
     */
    public Session getSession() {
	Message msg = getMessage();
	return msg != null ? msg.getSession() : null;
    }
}
