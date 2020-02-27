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

package com.sun.mail.pop3;

import javax.mail.*;

/**
 * The POP3 DefaultFolder.  Only contains the "INBOX" folder.
 *
 * @author Christopher Cotton
 */
public class DefaultFolder extends Folder {

    DefaultFolder(POP3Store store) {
	super(store);
    }

    @Override
    public String getName() {
	return "";
    }

    @Override
    public String getFullName() {
	return "";
    }

    @Override
    public Folder getParent() {
	return null;
    }

    @Override
    public boolean exists() {
	return true;
    }

    @Override
    public Folder[] list(String pattern) throws MessagingException {
	Folder[] f = { getInbox() };
	return f;
    }

    @Override
    public char getSeparator() {
	return '/';
    }

    @Override
    public int getType() {
	return HOLDS_FOLDERS;
    }

    @Override
    public boolean create(int type) throws MessagingException {
	return false;
    }

    @Override
    public boolean hasNewMessages() throws MessagingException {
	return false;
    }

    @Override
    public Folder getFolder(String name) throws MessagingException {
	if (!name.equalsIgnoreCase("INBOX")) {
	    throw new MessagingException("only INBOX supported");
	} else {
	    return getInbox();
	}
    }

    protected Folder getInbox() throws MessagingException {
	return getStore().getFolder("INBOX");
    }
    

    @Override
    public boolean delete(boolean recurse) throws MessagingException {
	throw new MethodNotSupportedException("delete");
    }

    @Override
    public boolean renameTo(Folder f) throws MessagingException {
	throw new MethodNotSupportedException("renameTo");
    }

    @Override
    public void open(int mode) throws MessagingException {
	throw new MethodNotSupportedException("open");
    }

    @Override
    public void close(boolean expunge) throws MessagingException {
	throw new MethodNotSupportedException("close");
    }

    @Override
    public boolean isOpen() {
	return false;
    }

    @Override
    public Flags getPermanentFlags() {
	return new Flags(); // empty flags object
    }

    @Override
    public int getMessageCount() throws MessagingException {
	return 0;
    }

    @Override
    public Message getMessage(int msgno) throws MessagingException {
	throw new MethodNotSupportedException("getMessage");
    }

    @Override
    public void appendMessages(Message[] msgs) throws MessagingException {
	throw new MethodNotSupportedException("Append not supported");	
    }

    @Override
    public Message[] expunge() throws MessagingException {
	throw new MethodNotSupportedException("expunge");	
    }
}
