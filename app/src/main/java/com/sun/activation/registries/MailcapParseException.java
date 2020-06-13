/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package	com.sun.activation.registries;

/**
 *	A class to encapsulate Mailcap parsing related exceptions
 */
public class MailcapParseException extends Exception {

    public MailcapParseException() {
	super();
    }

    public MailcapParseException(String inInfo) {
	super(inInfo);
    }
}
