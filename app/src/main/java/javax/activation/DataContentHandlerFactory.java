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

/**
 * This interface defines a factory for <code>DataContentHandlers</code>. An
 * implementation of this interface should map a MIME type into an
 * instance of DataContentHandler. The design pattern for classes implementing
 * this interface is the same as for the ContentHandler mechanism used in
 * <code>java.net.URL</code>.
 */

public interface DataContentHandlerFactory {

    /**
     * Creates a new DataContentHandler object for the MIME type.
     *
     * @param mimeType the MIME type to create the DataContentHandler for.
     * @return The new <code>DataContentHandler</code>, or <i>null</i>
     * if none are found.
     */
    public DataContentHandler createDataContentHandler(String mimeType);
}
