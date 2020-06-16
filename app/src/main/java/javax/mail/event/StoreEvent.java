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
 * This class models notifications from the Store connection. These
 * notifications can be ALERTS or NOTICES. ALERTS must be presented
 * to the user in a fashion that calls the user's attention to the
 * message.
 *
 * @author John Mani
 */

public class StoreEvent extends MailEvent {

    /**
     * Indicates that this message is an ALERT.
     */
    public static final int ALERT 		= 1;

    /**
     * Indicates that this message is a NOTICE.
     */
    public static final int NOTICE 		= 2;

    /**
     * The event type.
     *
     * @serial
     */
    protected int type;

    /**
     * The message text to be presented to the user.
     *
     * @serial
     */
    protected String message;

    private static final long serialVersionUID = 1938704919992515330L;

    /**
     * Construct a StoreEvent.
     *
     * @param	store	the source Store
     * @param	type	the event type
     * @param	message	a message assoicated with the event
     */
    public StoreEvent(Store store, int type, String message) {
	super(store);
	this.type = type;
	this.message = message;
    }

    /**
     * Return the type of this event.
     *
     * @return  type
     * @see #ALERT
     * @see #NOTICE
     */
    public int getMessageType() {
	return type;
    }

    /**
     * Get the message from the Store.
     *
     * @return message from the Store
     */
    public String getMessage() {
	return message;
    }

    /**
     * Invokes the appropriate StoreListener method.
     */
    @Override
    public void dispatch(Object listener) {
	((StoreListener)listener).notification(this);
    }
}
