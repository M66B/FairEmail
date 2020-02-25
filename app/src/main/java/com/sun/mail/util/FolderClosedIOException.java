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

package com.sun.mail.util;

import java.io.IOException;
import javax.mail.Folder;

/**
 * A variant of FolderClosedException that can be thrown from methods
 * that only throw IOException.  The getContent method will catch this
 * exception and translate it back to FolderClosedException.
 *
 * @author Bill Shannon
 */

public class FolderClosedIOException extends IOException {
    transient private Folder folder;

    private static final long serialVersionUID = 4281122580365555735L;
    
    /**
     * Constructor
     * @param folder	the Folder
     */
    public FolderClosedIOException(Folder folder) {
	this(folder, null);
    }

    /**
     * Constructor
     * @param folder 	the Folder
     * @param message	the detailed error message
     */
    public FolderClosedIOException(Folder folder, String message) {
	super(message);
	this.folder = folder;
    }

    /**
     * Returns the dead Folder object
     *
     * @return	the dead Folder
     */
    public Folder getFolder() {
	return folder;
    }
}
