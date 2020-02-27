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

package com.sun.mail.imap;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.event.MessageCountEvent;

/**
 * This class provides notification of messages that have been removed
 * since the folder was last synchronized.
 *
 * @since	JavaMail 1.5.1
 * @author	Bill Shannon
 */

public class MessageVanishedEvent extends MessageCountEvent {

    /**
     * The message UIDs.
     */
    private long[] uids;

    // a reusable empty array
    private static final Message[] noMessages = { };

    private static final long serialVersionUID = 2142028010250024922L;

    /**
     * Constructor.
     *
     * @param folder  	the containing folder
     * @param uids	the UIDs for the vanished messages
     */
    public MessageVanishedEvent(Folder folder, long[] uids) {
	super(folder, REMOVED, true, noMessages);
	this.uids = uids;
    }

    /**
     * Return the UIDs for this event.
     *
     * @return  the UIDs
     */
    public long[] getUIDs() {
	return uids;
    }
}
