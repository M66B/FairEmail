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

package com.sun.mail.imap.protocol;

import java.util.List;
import java.util.ArrayList;

/**
 * This class holds the 'start' and 'end' for a range of messages.
 */
public class MessageSet {

    public int start;
    public int end;

    public MessageSet() { }

    public MessageSet(int start, int end) {
	this.start = start;
	this.end = end;
    }

    /**
     * Count the total number of elements in a MessageSet
     *
     * @return	how many messages in this MessageSet
     */
    public int size() {
	return end - start + 1;
    }

    /**
     * Convert an array of integers into an array of MessageSets
     *
     * @param	msgs	the messages
     * @return		array of MessageSet objects
     */
    public static MessageSet[] createMessageSets(int[] msgs) {
	List<MessageSet> v = new ArrayList<>();
	int i,j;

	for (i=0; i < msgs.length; i++) {
	    MessageSet ms = new MessageSet();
	    ms.start = msgs[i];

	    // Look for contiguous elements
	    for (j=i+1; j < msgs.length; j++) {
		if (msgs[j] != msgs[j-1] +1)
		    break;
	    }
	    ms.end = msgs[j-1];
	    v.add(ms);
	    i = j-1; // i gets incremented @ top of the loop
	}
	return v.toArray(new MessageSet[v.size()]);	
    }

    /**
     * Convert an array of MessageSets into an IMAP sequence range
     *
     * @param	msgsets	the MessageSets
     * @return		IMAP sequence string
     */
    public static String toString(MessageSet[] msgsets) {
	if (msgsets == null || msgsets.length == 0) // Empty msgset
	    return null; 

	int i = 0;  // msgset index
	StringBuilder s = new StringBuilder();
	int size = msgsets.length;
	int start, end;

	for (;;) {
	    start = msgsets[i].start;
	    end = msgsets[i].end;

	    if (end > start)
		s.append(start).append(':').append(end);
	    else // end == start means only one element
		s.append(start);
	
	    i++; // Next MessageSet
	    if (i >= size) // No more MessageSets
		break;
	    else
		s.append(',');
	}
	return s.toString();
    }

	
    /*
     * Count the total number of elements in an array of MessageSets
     */
    public static int size(MessageSet[] msgsets) {
	int count = 0;

	if (msgsets == null) // Null msgset
	    return 0; 

	for (int i=0; i < msgsets.length; i++)
	    count += msgsets[i].size();
	
	return count;
    }
}
