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

import java.util.Arrays;
import java.util.Comparator;

import javax.mail.*;

import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.UIDSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Holder for some static utility methods.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public final class Utility {

    // Cannot be initialized
    private Utility() { }

    /**
     * Run thru the given array of messages, apply the given
     * Condition on each message and generate sets of contiguous 
     * sequence-numbers for the successful messages. If a message 
     * in the given array is found to be expunged, it is ignored.
     *
     * ASSERT: Since this method uses and returns message sequence
     * numbers, you should use this method only when holding the
     * messageCacheLock.
     *
     * @param	msgs	the messages
     * @param	cond	the condition to check
     * @return		the MessageSet array
     */
    public static MessageSet[] toMessageSet(Message[] msgs, Condition cond) {
	List<MessageSet> v = new ArrayList<>(1);
	int current, next;

	IMAPMessage msg;
	for (int i = 0; i < msgs.length; i++) {
	    msg = (IMAPMessage)msgs[i];
	    if (msg.isExpunged()) // expunged message, skip it
		continue;

	    current = msg.getSequenceNumber();
	    // Apply the condition. If it fails, skip it.
	    if ((cond != null) && !cond.test(msg))
		continue;
	    
	    MessageSet set = new MessageSet();
	    set.start = current;

	    // Look for contiguous sequence numbers
	    for (++i; i < msgs.length; i++) {
		// get next message
		msg = (IMAPMessage)msgs[i];

		if (msg.isExpunged()) // expunged message, skip it
		    continue;
		next = msg.getSequenceNumber();

		// Does this message match our condition ?
		if ((cond != null) && !cond.test(msg))
		    continue;
		
		if (next == current+1)
		    current = next;
		else { // break in sequence
		    // We need to reexamine this message at the top of
		    // the outer loop, so decrement 'i' to cancel the
		    // outer loop's autoincrement 
		    i--;
		    break;
		}
	    }
	    set.end = current;
	    v.add(set);
	}
	
	if (v.isEmpty()) // No valid messages
	    return null;
	else {
	    return v.toArray(new MessageSet[v.size()]);
	}
    }
    /**
     * Sort (a copy of) the given array of messages and then
     * run thru the sorted array of messages, apply the given
     * Condition on each message and generate sets of contiguous 
     * sequence-numbers for the successful messages. If a message 
     * in the given array is found to be expunged, it is ignored.
     *
     * ASSERT: Since this method uses and returns message sequence
     * numbers, you should use this method only when holding the
     * messageCacheLock.
     *
     * @param	msgs	the messages
     * @param	cond	the condition to check
     * @return		the MessageSet array
     * @since JavaMail 1.5.4
     */
    public static MessageSet[] toMessageSetSorted(Message[] msgs,
							    Condition cond) {
	/*
	 * XXX - This is quick and dirty.  A more efficient strategy would be
	 * to generate an array of message numbers by applying the condition
	 * (with zero indicating the message doesn't satisfy the condition),
	 * sort it, and then convert it to a MessageSet skipping all the zeroes.
	 */
	msgs = msgs.clone();
	Arrays.sort(msgs,
	    new Comparator<Message>() {
		@Override
		public int compare(Message msg1, Message msg2) {
		    return msg1.getMessageNumber() - msg2.getMessageNumber();
		}
	    });
	return toMessageSet(msgs, cond);
    }

    /**
     * Return UIDSets for the messages.  Note that the UIDs
     * must have already been fetched for the messages.
     *
     * @param	msgs	the messages
     * @return		the UIDSet array
     */
    public static UIDSet[] toUIDSet(Message[] msgs) {
	List<UIDSet> v = new ArrayList<>(1);
	long current, next;

	IMAPMessage msg;
	for (int i = 0; i < msgs.length; i++) {
	    msg = (IMAPMessage)msgs[i];
	    if (msg.isExpunged()) // expunged message, skip it
		continue;

	    current = msg.getUID();
 
	    UIDSet set = new UIDSet();
	    set.start = current;

	    // Look for contiguous UIDs
	    for (++i; i < msgs.length; i++) {
		// get next message
		msg = (IMAPMessage)msgs[i];

		if (msg.isExpunged()) // expunged message, skip it
		    continue;
		next = msg.getUID();

		if (next == current+1)
		    current = next;
		else { // break in sequence
		    // We need to reexamine this message at the top of
		    // the outer loop, so decrement 'i' to cancel the
		    // outer loop's autoincrement 
		    i--;
		    break;
		}
	    }
	    set.end = current;
	    v.add(set);
	}

	if (v.isEmpty()) // No valid messages
	    return null;
	else {
	    return v.toArray(new UIDSet[v.size()]);
	}
    }

    /**
     * Make the ResyncData UIDSet available to IMAPProtocol,
     * which is in a different package.  Note that this class
     * is not included in the public javadocs, thus "hiding"
     * this method.
     *
     * @param	rd	the ResyncData
     * @return		the UIDSet array
     * @since	JavaMail 1.5.1
     */
    public static UIDSet[] getResyncUIDSet(ResyncData rd) {
	return rd.getUIDSet();
    }

    /**
     * This interface defines the test to be executed in 
     * <code>toMessageSet()</code>. 
     */
    public static interface Condition {
	public boolean test(IMAPMessage message);
    }
}
