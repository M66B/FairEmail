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

import java.io.*;
import javax.mail.*;
import com.sun.mail.imap.protocol.*;
import com.sun.mail.iap.ProtocolException;

/**
 * This class implements a nested IMAP message
 *
 * @author  John Mani
 */

public class IMAPNestedMessage extends IMAPMessage {
    private IMAPMessage msg; // the enclosure of this nested message

    /**
     * Package private constructor. <p>
     *
     * Note that nested messages have no containing folder, nor 
     * a message number.
     */
    IMAPNestedMessage(IMAPMessage m, BODYSTRUCTURE b, ENVELOPE e, String sid) {
	super(m._getSession());
	msg = m;
	bs = b;
	envelope = e;
	sectionId = sid;
	setPeek(m.getPeek());
    }

    /*
     * Get the enclosing message's Protocol object. Overrides
     * IMAPMessage.getProtocol().
     */
    @Override
    protected IMAPProtocol getProtocol()
			throws ProtocolException, FolderClosedException {
	return msg.getProtocol();
    }

    /*
     * Is this an IMAP4 REV1 server?
     */
    @Override
    protected boolean isREV1() throws FolderClosedException {
	return msg.isREV1();
    }

    /*
     * Get the enclosing message's messageCacheLock. Overrides
     * IMAPMessage.getMessageCacheLock().
     */
    @Override
    protected Object getMessageCacheLock() {
	return msg.getMessageCacheLock();
    }

    /*
     * Get the enclosing message's sequence number. Overrides
     * IMAPMessage.getSequenceNumber().
     */
    @Override
    protected int getSequenceNumber() {
	return msg.getSequenceNumber();
    }

    /*
     * Check whether the enclosing message is expunged. Overrides 
     * IMAPMessage.checkExpunged().
     */
    @Override
    protected void checkExpunged() throws MessageRemovedException {
	msg.checkExpunged();
    }

    /*
     * Check whether the enclosing message is expunged. Overrides
     * Message.isExpunged().
     */
    @Override
    public boolean isExpunged() {
	return msg.isExpunged();
    }

    /*
     * Get the enclosing message's fetchBlockSize. 
     */
    @Override
    protected int getFetchBlockSize() {
	return msg.getFetchBlockSize();
    }

    /*
     * Get the enclosing message's ignoreBodyStructureSize. 
     */
    @Override
    protected boolean ignoreBodyStructureSize() {
	return msg.ignoreBodyStructureSize();
    }

    /*
     * IMAPMessage uses RFC822.SIZE. We use the "size" field from
     * our BODYSTRUCTURE.
     */
    @Override
    public int getSize() throws MessagingException {
	return bs.size;
    }

    /*
     * Disallow setting flags on nested messages
     */
    @Override
    public synchronized void setFlags(Flags flag, boolean set) 
			throws MessagingException {
	// Cannot set FLAGS on a nested IMAP message	
	throw new MethodNotSupportedException(
		"Cannot set flags on this nested message");
    }
}
