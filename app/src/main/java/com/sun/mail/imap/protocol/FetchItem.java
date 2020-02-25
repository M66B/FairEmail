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

package com.sun.mail.imap.protocol;

import java.lang.reflect.*;

import javax.mail.FetchProfile;
import com.sun.mail.iap.ParsingException;

/**
 * Metadata describing a FETCH item.
 * Note that the "name" field MUST be in uppercase. <p>
 *
 * @author  Bill Shannon
 * @since JavaMail 1.4.6
 */

public abstract class FetchItem { 
    private String name;
    private FetchProfile.Item fetchProfileItem;

    public FetchItem(String name, FetchProfile.Item fetchProfileItem) {
	this.name = name;
	this.fetchProfileItem = fetchProfileItem;
    }

    public String getName() {
	return name;
    }

    public FetchProfile.Item getFetchProfileItem() {
	return fetchProfileItem;
    }

    /**
     * Parse the item into some kind of object appropriate for the item.
     * Note that the item name will have been parsed and skipped already.
     *
     * @param	r	the response
     * @return		the fetch item
     * @exception	ParsingException	for parsing failures
     */
    public abstract Object parseItem(FetchResponse r) throws ParsingException;
}
