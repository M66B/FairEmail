/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.mail.gimap;

import java.io.*;

import javax.mail.*;
import javax.mail.internet.*;

import com.sun.mail.util.*;
import com.sun.mail.iap.*;
import com.sun.mail.imap.*;
import com.sun.mail.imap.protocol.*;
import com.sun.mail.gimap.protocol.*;

/**
 * A Gmail message.  Adds methods to access Gmail-specific per-message data.
 *
 * @since JavaMail 1.4.6
 * @author Bill Shannon
 */

public class GmailMessage extends IMAPMessage {
    /**
     * Constructor.
     *
     * @param	folder	the containing folder
     * @param	msgnum	the message sequence number
     */
    protected GmailMessage(IMAPFolder folder, int msgnum) {
	super(folder, msgnum);
    }

    /**
     * Constructor, for use by IMAPNestedMessage.
     *
     * @param	session	the Session
     */
    protected GmailMessage(Session session) {
	super(session);
    }

    /**
     * Return the Gmail unique message ID.
     *
     * @return	the message ID
     * @exception	MessagingException for failures
     */
    public long getMsgId() throws MessagingException {
	Long msgid = (Long)getItem(GmailProtocol.MSGID_ITEM);
	if (msgid != null)
	    return msgid.longValue();
	else
	    return -1;
    }

    /**
     * Return the Gmail unique thread ID.
     *
     * @return	the thread ID
     * @exception	MessagingException for failures
     */
    public long getThrId() throws MessagingException {
	Long thrid = (Long)getItem(GmailProtocol.THRID_ITEM);
	if (thrid != null)
	    return thrid.longValue();
	else
	    return -1;
    }

    /**
     * Return the Gmail labels associated with this message.
     *
     * @return	array of labels, or empty array if none
     * @exception	MessagingException for failures
     */
    public String[] getLabels() throws MessagingException {
	String[] labels = (String[])getItem(GmailProtocol.LABELS_ITEM);
	if (labels != null)
	    return (String[])(labels.clone());
	else
	    return new String[0];
    }

    /**
     * Set/Unset the given labels on this message.
     *
     * @param	labels	the labels to add or remove
     * @param	set	true to add labels, false to remove
     * @exception	MessagingException for failures
     * @since JavaMail 1.5.5
     */
    public synchronized void setLabels(String[] labels, boolean set)
			throws MessagingException {
        // Acquire MessageCacheLock, to freeze seqnum.
        synchronized(getMessageCacheLock()) {
	    try {
		IMAPProtocol ip = getProtocol();
		assert ip instanceof GmailProtocol;
		GmailProtocol p = (GmailProtocol)ip;
		checkExpunged(); // Insure that this message is not expunged
		p.storeLabels(getSequenceNumber(), labels, set);
	    } catch (ConnectionException cex) {
		throw new FolderClosedException(folder, cex.getMessage());
	    } catch (ProtocolException pex) {
		throw new MessagingException(pex.getMessage(), pex);
	    }
	}
    }

    /**
     * Clear any cached labels for this message.
     * The Gmail labels for a messge will be cached when first accessed
     * using either the fetch method or the getLabels method.  Gmail provides
     * no notification when the labels have been changed by another application
     * so applications may need to clear the cache if accessing the labels for
     * a message more than once while the Folder is open.
     *
     * @since JavaMail 1.5.6
     */
    public synchronized void clearCachedLabels() {
	if (items != null)
	    items.remove(GmailProtocol.LABELS_ITEM.getName());
    }
}
