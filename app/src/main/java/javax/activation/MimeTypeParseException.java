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
 * A class to encapsulate MimeType parsing related exceptions.
 */
public class MimeTypeParseException extends Exception {

    /**
     * Constructs a MimeTypeParseException with no specified detail message. 
     */
    public MimeTypeParseException() {
	super();
    }

    /**
     * Constructs a MimeTypeParseException with the specified detail message. 
     *
     * @param   s   the detail message.
     */
    public MimeTypeParseException(String s) {
	super(s);
    }
}
