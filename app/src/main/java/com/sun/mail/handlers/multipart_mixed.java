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
import javax.activation.*;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;


public class multipart_mixed extends handler_base {
    private static ActivationDataFlavor[] myDF = {
	new ActivationDataFlavor(Multipart.class,
				    "multipart/mixed", "Multipart")
    };

    @Override
    protected ActivationDataFlavor[] getDataFlavors() {
	return myDF;
    }

    /**
     * Return the content.
     */
    @Override
    public Object getContent(DataSource ds) throws IOException {
	try {
	    return new MimeMultipart(ds); 
	} catch (MessagingException e) {
	    IOException ioex =
		new IOException("Exception while constructing MimeMultipart");
	    ioex.initCause(e);
	    throw ioex;
	}
    }
    
    /**
     * Write the object to the output stream, using the specific MIME type.
     */
    @Override
    public void writeTo(Object obj, String mimeType, OutputStream os) 
			throws IOException {
	if (!(obj instanceof Multipart))
	    throw new IOException("\"" + getDataFlavors()[0].getMimeType() +
		"\" DataContentHandler requires Multipart object, " +
		"was given object of type " + obj.getClass().toString() +
		"; obj.cl " + obj.getClass().getClassLoader() +
		", Multipart.cl " + Multipart.class.getClassLoader());

	try {
	    ((Multipart)obj).writeTo(os);
	} catch (MessagingException e) {
	    IOException ioex =
		new IOException("Exception writing Multipart");
	    ioex.initCause(e);
	    throw ioex;
	}
    }
}
