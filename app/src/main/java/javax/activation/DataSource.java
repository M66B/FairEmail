/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.activation;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * The DataSource interface provides Jakarta Activation
 * with an abstraction of an arbitrary collection of data.  It
 * provides a type for that data as well as access
 * to it in the form of <code>InputStreams</code> and
 * <code>OutputStreams</code> where appropriate.
 */

public interface DataSource {

    /**
     * This method returns an <code>InputStream</code> representing
     * the data and throws the appropriate exception if it can
     * not do so.  Note that a new <code>InputStream</code> object must be
     * returned each time this method is called, and the stream must be
     * positioned at the beginning of the data.
     *
     * @return an InputStream
     * @exception	IOException	for failures creating the InputStream
     */
    public InputStream getInputStream() throws IOException;

    /**
     * This method returns an <code>OutputStream</code> where the
     * data can be written and throws the appropriate exception if it can
     * not do so.  Note that a new <code>OutputStream</code> object must
     * be returned each time this method is called, and the stream must
     * be positioned at the location the data is to be written.
     *
     * @return an OutputStream
     * @exception	IOException	for failures creating the OutputStream
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * This method returns the MIME type of the data in the form of a
     * string. It should always return a valid type. It is suggested
     * that getContentType return "application/octet-stream" if the
     * DataSource implementation can not determine the data type.
     *
     * @return the MIME Type
     */
    public String getContentType();

    /**
     * Return the <i>name</i> of this object where the name of the object
     * is dependant on the nature of the underlying objects. DataSources
     * encapsulating files may choose to return the filename of the object.
     * (Typically this would be the last component of the filename, not an
     * entire pathname.)
     *
     * @return the name of the object.
     */
    public String getName();
}
