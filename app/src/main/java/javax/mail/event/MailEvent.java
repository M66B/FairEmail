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

package javax.mail.event;

import java.util.EventObject;

/**
 * Common base class for mail events, defining the dispatch method.
 *
 * @author Bill Shannon
 */

public abstract class MailEvent extends EventObject {
    private static final long serialVersionUID = 1846275636325456631L;

    /**
     * Construct a MailEvent referring to the given source.
     *
     * @param	source	the source of the event
     */
    public MailEvent(Object source) {
        super(source);
    }

    /**
     * This method invokes the appropriate method on a listener for
     * this event. Subclasses provide the implementation.
     *
     * @param	listener	the listener to invoke on
     */
    public abstract void dispatch(Object listener);
}
