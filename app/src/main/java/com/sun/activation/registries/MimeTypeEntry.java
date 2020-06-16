/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.activation.registries;

import java.lang.*;

public class MimeTypeEntry {
    private String type;
    private String extension;

    public MimeTypeEntry(String mime_type, String file_ext) {
	type = mime_type;
	extension = file_ext;
    }

    public String getMIMEType() {
	return type;
    }

    public String getFileExtension() {
	return extension;
    }

    public String toString() {
	return "MIMETypeEntry: " + type + ", " + extension;
    }
}
