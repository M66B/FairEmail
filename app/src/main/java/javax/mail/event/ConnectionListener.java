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

import java.util.*;

/**
 * This is the Listener interface for Connection events.
 *
 * @author John Mani
 */

public interface ConnectionListener extends java.util.EventListener {

    /**
     * Invoked when a Store/Folder/Transport is opened.
     *
     * @param	e	the ConnectionEvent
     */
    public void opened(ConnectionEvent e);

    /**
     * Invoked when a Store is disconnected. Note that a folder
     * cannot be disconnected, so a folder will not fire this event
     *
     * @param	e	the ConnectionEvent
     */
    public void disconnected(ConnectionEvent e);

    /**
     * Invoked when a Store/Folder/Transport is closed.
     *
     * @param	e	the ConnectionEvent
     */
    public void closed(ConnectionEvent e);
}
