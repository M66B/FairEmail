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

import java.io.IOException;

/**
 * JavaBeans components that are Jakarta Activation aware implement
 * this interface to find out which command verb they're being asked
 * to perform, and to obtain the DataHandler representing the
 * data they should operate on.  JavaBeans that don't implement
 * this interface may be used as well.  Such commands may obtain
 * the data using the Externalizable interface, or using an
 * application-specific method.
 */
public interface CommandObject {

    /**
     * Initialize the Command with the verb it is requested to handle
     * and the DataHandler that describes the data it will
     * operate on. <b>NOTE:</b> it is acceptable for the caller
     * to pass <i>null</i> as the value for <code>DataHandler</code>.
     *
     * @param verb The Command Verb this object refers to.
     * @param dh The DataHandler.
     * @exception	IOException	for failures accessing data
     */
    public void setCommandContext(String verb, DataHandler dh)
						throws IOException;
}
