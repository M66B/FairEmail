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

package com.sun.mail.handlers;

import java.io.*;
import java.util.Properties;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;


/**
 * @author	Christopher Cotton
 */


public class message_rfc822 extends handler_base {

    private static ActivationDataFlavor[] ourDataFlavor = {
	new ActivationDataFlavor(Message.class, "message/rfc822", "Message")
    };

    @Override
    protected ActivationDataFlavor[] getDataFlavors() {
	return ourDataFlavor;
    }

    /**
     * Return the content.
     */
    @Override
    public Object getContent(DataSource ds) throws IOException {
	// create a new MimeMessage
	try {
	    Session session;
	    if (ds instanceof MessageAware) {
		MessageContext mc = ((MessageAware)ds).getMessageContext();
		session = mc.getSession();
	    } else {
		// Hopefully a rare case.  Also hopefully the application
		// has created a default Session that can just be returned
		// here.  If not, the one we create here is better than
		// nothing, but overall not a really good answer.
		session = Session.getDefaultInstance(new Properties(), null);
	    }
	    return new MimeMessage(session, ds.getInputStream());
	} catch (MessagingException me) {
	    IOException ioex =
		new IOException("Exception creating MimeMessage in " +
		    "message/rfc822 DataContentHandler");
	    ioex.initCause(me);
	    throw ioex;
	}
    }
    
    /**
     * Write the object as a byte stream.
     */
    @Override
    public void writeTo(Object obj, String mimeType, OutputStream os) 
			throws IOException {
	if (!(obj instanceof Message))
	    throw new IOException("\"" + getDataFlavors()[0].getMimeType() +
		"\" DataContentHandler requires Message object, " +
		"was given object of type " + obj.getClass().toString() +
		"; obj.cl " + obj.getClass().getClassLoader() +
		", Message.cl " + Message.class.getClassLoader());

	// if the object is a message, we know how to write that out
	Message m = (Message)obj;
	try {
	    m.writeTo(os);
	} catch (MessagingException me) {
	    IOException ioex = new IOException("Exception writing message");
	    ioex.initCause(me);
	    throw ioex;
	}
    }
}
