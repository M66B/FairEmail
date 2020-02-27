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
import javax.mail.*;

/**
 * This class models Connection events.
 *
 * @author John Mani
 */

public class ConnectionEvent extends MailEvent  {

    /** A connection was opened. */
    public static final int OPENED 		= 1;
    /** A connection was disconnected (not currently used). */
    public static final int DISCONNECTED 	= 2;
    /** A connection was closed. */
    public static final int CLOSED 		= 3;

    /**
     * The event type.
     *
     * @serial
     */
    protected int type;

    private static final long serialVersionUID = -1855480171284792957L;

    /**
     * Construct a ConnectionEvent.
     *
     * @param	source  The source object
     * @param	type	the event type
     */
    public ConnectionEvent(Object source, int type) {
	super(source);
	this.type = type;
    }

    /**
     * Return the type of this event
     * @return  type
     */
    public int getType() {
	return type;
    }

    /**
     * Invokes the appropriate ConnectionListener method
     */
    @Override
    public void dispatch(Object listener) {
	if (type == OPENED)
	    ((ConnectionListener)listener).opened(this);
	else if (type == DISCONNECTED)
	    ((ConnectionListener)listener).disconnected(this);
	else if (type == CLOSED)
	    ((ConnectionListener)listener).closed(this);
    }
}
