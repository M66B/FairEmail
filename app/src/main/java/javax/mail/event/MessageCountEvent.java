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
 * This class notifies changes in the number of messages in a folder. <p>
 *
 * Note that some folder types may only deliver MessageCountEvents at
 * certain times or after certain operations.  IMAP in particular will
 * only notify the client of MessageCountEvents when a client issues a
 * new command.  Refer to
 * <A HREF="http://www.ietf.org/rfc/rfc3501.txt" TARGET="_top">RFC 3501</A>
 * for details.
 * A client may want to "poll" the folder by occasionally calling the
 * {@link javax.mail.Folder#getMessageCount getMessageCount} or
 * {@link javax.mail.Folder#isOpen isOpen} methods
 * to solicit any such notifications.
 *
 * @author John Mani
 */

public class MessageCountEvent extends MailEvent {

    /** The messages were added to their folder */
    public static final int ADDED 		= 1;
    /** The messages were removed from their folder */
    public static final int REMOVED 		= 2;

    /**
     * The event type.
     *
     * @serial
     */
    protected int type;

    /**
     * If true, this event is the result of an explicit
     * expunge by this client, and the messages in this 
     * folder have been renumbered to account for this.
     * If false, this event is the result of an expunge
     * by external sources.
     *
     * @serial
     */
    protected boolean removed;

    /**
     * The messages.
     */
    transient protected Message[] msgs;

    private static final long serialVersionUID = -7447022340837897369L;

    /**
     * Constructor.
     * @param folder  	The containing folder
     * @param type	The event type
     * @param removed	If true, this event is the result of an explicit
     *			expunge by this client, and the messages in this 
     *			folder have been renumbered to account for this.
     *			If false, this event is the result of an expunge
     *			by external sources.
     *
     * @param msgs	The messages added/removed
     */
    public MessageCountEvent(Folder folder, int type, 
			     boolean removed, Message[] msgs) {
	super(folder);
	this.type = type;
	this.removed = removed;
	this.msgs = msgs;
    }

    /**
     * Return the type of this event.
     * @return  type
     */
    public int getType() {
	return type;
    }

    /**
     * Indicates whether this event is the result of an explicit
     * expunge by this client, or due to an expunge from external
     * sources. If <code>true</code>, this event is due to an
     * explicit expunge and hence all remaining messages in this
     * folder have been renumbered. If <code>false</code>, this event
     * is due to an external expunge. <p>
     *
     * Note that this method is valid only if the type of this event
     * is <code>REMOVED</code>
     *
     * @return	true if the message has been removed
     */
    public boolean isRemoved() {
	return removed;
    }

    /**
     * Return the array of messages added or removed.
     * @return array of messages
     */
    public Message[] getMessages() {
	return msgs;
    }

    /**
     * Invokes the appropriate MessageCountListener method.
     */
    @Override
    public void dispatch(Object listener) {
	if (type == ADDED)
	    ((MessageCountListener)listener).messagesAdded(this);
	else // REMOVED
	    ((MessageCountListener)listener).messagesRemoved(this);
    }
}
