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
 * This class models Message change events.
 *
 * @author John Mani
 */

public class MessageChangedEvent extends MailEvent {

    /** The message's flags changed. */
    public static final int FLAGS_CHANGED 	= 1;
    /** The message's envelope (headers, but not body) changed. */
    public static final int ENVELOPE_CHANGED 	= 2;

    /**
     * The event type.
     *
     * @serial
     */
    protected int type;

    /**
     * The message that changed.
     */
    transient protected Message msg;

    private static final long serialVersionUID = -4974972972105535108L;

    /**
     * Constructor.
     * @param source  	The folder that owns the message
     * @param type	The change type
     * @param msg	The changed message 
     */
    public MessageChangedEvent(Object source, int type, Message msg) {
	super(source);
	this.msg = msg;
	this.type = type;
    }

    /**
     * Return the type of this event.
     * @return  type
     */
    public int getMessageChangeType() {
	return type;
    }

    /**
     * Return the changed Message.
     * @return  the message
     */
    public Message getMessage() {
	return msg;
    }

    /**
     * Invokes the appropriate MessageChangedListener method.
     */
    @Override
    public void dispatch(Object listener) {
	((MessageChangedListener)listener).messageChanged(this);
    }
}
