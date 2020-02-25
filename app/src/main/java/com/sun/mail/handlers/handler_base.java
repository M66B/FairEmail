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
import javax.activation.*;

/**
 * Base class for other DataContentHandlers.
 */
public abstract class handler_base implements DataContentHandler {

    /**
     * Return an array of ActivationDataFlavors that we support.
     * Usually there will be only one.
     *
     * @return	array of ActivationDataFlavors that we support
     */
    protected abstract ActivationDataFlavor[] getDataFlavors();

    /**
     * Given the flavor that matched, return the appropriate type of object.
     * Usually there's only one flavor so just call getContent.
     *
     * @param	aFlavor	the ActivationDataFlavor
     * @param	ds	DataSource containing the data
     * @return	the object
     * @exception	IOException	for errors reading the data
     */
    protected Object getData(ActivationDataFlavor aFlavor, DataSource ds)
				throws IOException {
	return getContent(ds);
    }

    /**
     * Return the DataFlavors for this <code>DataContentHandler</code>.
     *
     * @return The DataFlavors
     */
    public ActivationDataFlavor[] getTransferDataFlavors() {
	return getDataFlavors().clone();
    }

    /**
     * Return the Transfer Data of type DataFlavor from InputStream.
     *
     * @param	df	The DataFlavor
     * @param	ds	The DataSource corresponding to the data
     * @return	the object
     * @exception	IOException	for errors reading the data
     */
    public Object getTransferData(ActivationDataFlavor df, DataSource ds) 
			throws IOException {
	ActivationDataFlavor[] adf = getDataFlavors();
	for (int i = 0; i < adf.length; i++) {
	    // use ActivationDataFlavor.equals, which properly
	    // ignores Content-Type parameters in comparison
	    if (adf[i].equals(df))
		return getData(adf[i], ds);
	}
	return null;
    }
}
