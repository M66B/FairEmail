/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.activation;

//import java.awt.datatransfer.DataFlavor;
//import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.activation.DataSource;

/**
 * The DataContentHandler interface is implemented by objects that can
 * be used to extend the capabilities of the DataHandler's implementation
 * of the Transferable interface. Through <code>DataContentHandlers</code>
 * the framework can be extended to convert streams in to objects, and
 * to write objects to streams. <p>
 *
 * Applications don't generally call the methods in DataContentHandlers
 * directly. Instead, an application calls the equivalent methods in
 * DataHandler. The DataHandler will attempt to find an appropriate
 * DataContentHandler that corresponds to its MIME type using the
 * current DataContentHandlerFactory. The DataHandler then calls
 * through to the methods in the DataContentHandler.
 */

public interface DataContentHandler {
    /**
     * Returns an array of DataFlavor objects indicating the flavors the
     * data can be provided in. The array should be ordered according to
     * preference for providing the data (from most richly descriptive to
     * least descriptive).
     *
     * @return The DataFlavors.
     */
    public ActivationDataFlavor[] getTransferDataFlavors();

    /**
     * Returns an object which represents the data to be transferred.
     * The class of the object returned is defined by the representation class
     * of the flavor.
     *
     * @param df The DataFlavor representing the requested type.
     * @param ds The DataSource representing the data to be converted.
     * @return The constructed Object.
     * @exception IOException	if the data can't be accessed
     */
    public Object getTransferData(ActivationDataFlavor df, DataSource ds)
				throws /*UnsupportedFlavorException,*/ IOException;

    /**
     * Return an object representing the data in its most preferred form.
     * Generally this will be the form described by the first DataFlavor
     * returned by the <code>getTransferDataFlavors</code> method.
     *
     * @param ds The DataSource representing the data to be converted.
     * @return The constructed Object.
     * @exception IOException	if the data can't be accessed
     */
    public Object getContent(DataSource ds) throws IOException;

    /**
     * Convert the object to a byte stream of the specified MIME type
     * and write it to the output stream.
     *
     * @param obj	The object to be converted.
     * @param mimeType	The requested MIME type of the resulting byte stream.
     * @param os	The output stream into which to write the converted
     *			byte stream.
     * @exception IOException	errors writing to the stream
     */
    public void writeTo(Object obj, String mimeType, OutputStream os)
	                                               throws IOException;
}
