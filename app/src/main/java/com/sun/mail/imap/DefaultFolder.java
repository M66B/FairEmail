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
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;

/**
 * The default IMAP folder (root of the naming hierarchy).
 *
 * @author  John Mani
 */

public class DefaultFolder extends IMAPFolder {
    
    protected DefaultFolder(IMAPStore store) {
	super("", UNKNOWN_SEPARATOR, store, null);
	exists = true; // of course
	type = HOLDS_FOLDERS; // obviously
    }

    @Override
    public synchronized String getName() {
	return fullName;
    }

    @Override
    public Folder getParent() {
	return null;
    }

    @Override
    public synchronized Folder[] list(final String pattern)
				throws MessagingException {
	ListInfo[] li = null;

	li = (ListInfo[])doCommand(new ProtocolCommand() {
	    @Override
	    public Object doCommand(IMAPProtocol p) throws ProtocolException {
		return p.list("", pattern);
	    }
	});

	if (li == null)
	    return new Folder[0];

	IMAPFolder[] folders = new IMAPFolder[li.length];
	for (int i = 0; i < folders.length; i++)
	    folders[i] = ((IMAPStore)store).newIMAPFolder(li[i]);
	return folders;
    }

    @Override
    public synchronized Folder[] listSubscribed(final String pattern)
				throws MessagingException {
	ListInfo[] li = null;

	li = (ListInfo[])doCommand(new ProtocolCommand() {
	    @Override
	    public Object doCommand(IMAPProtocol p) throws ProtocolException {
		return p.lsub("", pattern);
	    }
	});

	if (li == null)
	    return new Folder[0];

	IMAPFolder[] folders = new IMAPFolder[li.length];
	for (int i = 0; i < folders.length; i++)
	    folders[i] = ((IMAPStore)store).newIMAPFolder(li[i]);
	return folders;
    }

    @Override
    public boolean hasNewMessages() throws MessagingException {
	// Not applicable on DefaultFolder
	return false;
    }

    @Override
    public Folder getFolder(String name) throws MessagingException {
	return ((IMAPStore)store).newIMAPFolder(name, UNKNOWN_SEPARATOR);
    }

    @Override
    public boolean delete(boolean recurse) throws MessagingException {  
	// Not applicable on DefaultFolder
	throw new MethodNotSupportedException("Cannot delete Default Folder");
    }

    @Override
    public boolean renameTo(Folder f) throws MessagingException {
	// Not applicable on DefaultFolder
	throw new MethodNotSupportedException("Cannot rename Default Folder");
    }

    @Override
    public void appendMessages(Message[] msgs) throws MessagingException {
	// Not applicable on DefaultFolder
	throw new MethodNotSupportedException("Cannot append to Default Folder");
    }

    @Override
    public Message[] expunge() throws MessagingException {
	// Not applicable on DefaultFolder
	throw new MethodNotSupportedException("Cannot expunge Default Folder");
    }
}
