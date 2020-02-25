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

import java.io.IOException;
import java.io.OutputStream;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * DataContentHandler for text/xml.
 *
 * @author Anil Vijendran
 * @author Bill Shannon
 */
public class text_xml extends text_plain {

    private static final ActivationDataFlavor[] flavors = {
	new ActivationDataFlavor(String.class, "text/xml", "XML String"),
	new ActivationDataFlavor(String.class, "application/xml", "XML String"),
	new ActivationDataFlavor(StreamSource.class, "text/xml", "XML"),
	new ActivationDataFlavor(StreamSource.class, "application/xml", "XML")
    };

    @Override
    protected ActivationDataFlavor[] getDataFlavors() {
	return flavors;
    }

    @Override
    protected Object getData(ActivationDataFlavor aFlavor, DataSource ds)
				throws IOException {
	if (aFlavor.getRepresentationClass() == String.class)
	    return super.getContent(ds);
	else if (aFlavor.getRepresentationClass() == StreamSource.class)
	    return new StreamSource(ds.getInputStream());
	else
	    return null;        // XXX - should never happen
    }

    /**
     */
    @Override
    public void writeTo(Object obj, String mimeType, OutputStream os)
				    throws IOException {
	if (!isXmlType(mimeType))
	    throw new IOException(
		"Invalid content type \"" + mimeType + "\" for text/xml DCH");
	if (obj instanceof String) {
	    super.writeTo(obj, mimeType, os);
	    return;
	}
	if (!(obj instanceof DataSource || obj instanceof Source)) {
	     throw new IOException("Invalid Object type = "+obj.getClass()+
		". XmlDCH can only convert DataSource or Source to XML.");
	}

	try {
	    Transformer transformer =
		TransformerFactory.newInstance().newTransformer();
	    StreamResult result = new StreamResult(os);
	    if (obj instanceof DataSource) {
		// Streaming transform applies only to
		// javax.xml.transform.StreamSource
		transformer.transform(
		    new StreamSource(((DataSource)obj).getInputStream()),
		    result);
	    } else {
		transformer.transform((Source)obj, result);
	    }
	} catch (TransformerException ex) {
	    IOException ioex = new IOException(
		"Unable to run the JAXP transformer on a stream "
		    + ex.getMessage());
	    ioex.initCause(ex);
	    throw ioex;
	} catch (RuntimeException ex) {
	    IOException ioex = new IOException(
		"Unable to run the JAXP transformer on a stream "
		    + ex.getMessage());
	    ioex.initCause(ex);
	    throw ioex;
	}
    }

    private boolean isXmlType(String type) {
	try {
	    ContentType ct = new ContentType(type);
	    return ct.getSubType().equals("xml") &&
		    (ct.getPrimaryType().equals("text") ||
		    ct.getPrimaryType().equals("application"));
	} catch (ParseException ex) {
	    return false;
	} catch (RuntimeException ex) {
	    return false;
	}
    }
}
