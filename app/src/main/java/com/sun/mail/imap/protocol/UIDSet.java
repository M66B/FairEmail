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
import java.util.StringTokenizer;

/**
 * This class holds the 'start' and 'end' for a range of UIDs.
 * Just like MessageSet except using long instead of int.
 */
public class UIDSet {

    public long start;
    public long end;

    public UIDSet() { }

    public UIDSet(long start, long end) {
	this.start = start;
	this.end = end;
    }

    /**
     * Count the total number of elements in a UIDSet
     *
     * @return	the number of elements
     */
    public long size() {
	return end - start + 1;
    }

    /**
     * Convert an array of longs into an array of UIDSets
     *
     * @param	uids	the UIDs
     * @return		array of UIDSet objects
     */
    public static UIDSet[] createUIDSets(long[] uids) {
	if (uids == null)
	    return null;
	List<UIDSet> v = new ArrayList<>();
	int i,j;

	for (i=0; i < uids.length; i++) {
	    UIDSet ms = new UIDSet();
	    ms.start = uids[i];

	    // Look for contiguous elements
	    for (j=i+1; j < uids.length; j++) {
		if (uids[j] != uids[j-1] +1)
		    break;
	    }
	    ms.end = uids[j-1];
	    v.add(ms);
	    i = j-1; // i gets incremented @ top of the loop
	}
	UIDSet[] uidset = new UIDSet[v.size()];	
	return v.toArray(uidset);
    }

    /**
     * Parse a string in IMAP UID range format.
     *
     * @param	uids	UID string
     * @return		array of UIDSet objects
     * @since	JavaMail 1.5.1
     */
    public static UIDSet[] parseUIDSets(String uids) {
	if (uids == null)
	    return null;
	List<UIDSet> v = new ArrayList<>();
	StringTokenizer st = new StringTokenizer(uids, ",:", true);
	long start = -1;
	UIDSet cur = null;
	try {
	    while(st.hasMoreTokens()) {
		String s = st.nextToken();
		if (s.equals(",")) {
		    if (cur != null)
			v.add(cur);
		    cur = null;
		} else if (s.equals(":")) {
		    // nothing to do, wait for next number
		} else {	// better be a number
		    long n = Long.parseLong(s);
		    if (cur != null)
			cur.end = n;
		    else
			cur = new UIDSet(n, n);
		}
	    }
	} catch (NumberFormatException nex) {
	    // give up and return what we have so far
	}
	if (cur != null)
	    v.add(cur);
	UIDSet[] uidset = new UIDSet[v.size()];
	return v.toArray(uidset);
    }

    /**
     * Convert an array of UIDSets into an IMAP sequence range.
     *
     * @param	uidset	the UIDSets
     * @return		the IMAP sequence string
     */
    public static String toString(UIDSet[] uidset) {
	if (uidset == null)
	    return null;
	if (uidset.length == 0) // Empty uidset
	    return "";

	int i = 0;  // uidset index
	StringBuilder s = new StringBuilder();
	int size = uidset.length;
	long start, end;

	for (;;) {
	    start = uidset[i].start;
	    end = uidset[i].end;

	    if (end > start)
		s.append(start).append(':').append(end);
	    else // end == start means only one element
		s.append(start);
	
	    i++; // Next UIDSet
	    if (i >= size) // No more UIDSets
		break;
	    else
		s.append(',');
	}
	return s.toString();
    }

    /**
     * Convert an array of UIDSets into a array of long UIDs.
     *
     * @param	uidset	the UIDSets
     * @return		arrray of UIDs
     * @since	JavaMail 1.5.1
     */
    public static long[] toArray(UIDSet[] uidset) {
	//return toArray(uidset, -1);
	if (uidset == null)
	    return null;
	long[] uids = new long[(int)UIDSet.size(uidset)];
	int i = 0;
	for (UIDSet u : uidset) {
	    for (long n = u.start; n <= u.end; n++)
		uids[i++] = n;
	}
	return uids;
    }

    /**
     * Convert an array of UIDSets into a array of long UIDs.
     * Don't include any UIDs larger than uidmax.
     *
     * @param	uidset	the UIDSets
     * @param	uidmax	maximum UID
     * @return		arrray of UIDs
     * @since	JavaMail 1.5.1
     */
    public static long[] toArray(UIDSet[] uidset, long uidmax) {
	if (uidset == null)
	    return null;
	long[] uids = new long[(int)UIDSet.size(uidset, uidmax)];
	int i = 0;
	for (UIDSet u : uidset) {
	    for (long n = u.start; n <= u.end; n++) {
		if (uidmax >= 0 && n > uidmax)
		    break;
		uids[i++] = n;
	    }
	}
	return uids;
    }

    /**
     * Count the total number of elements in an array of UIDSets.
     *
     * @param	uidset	the UIDSets
     * @return		the number of elements
     */
    public static long size(UIDSet[] uidset) {
	long count = 0;

	if (uidset != null)
	    for (UIDSet u : uidset)
		count += u.size();
	
	return count;
    }

    /**
     * Count the total number of elements in an array of UIDSets.
     * Don't count UIDs greater then uidmax.
     *
     * @since	JavaMail 1.5.1
     */
    private static long size(UIDSet[] uidset, long uidmax) {
	long count = 0;

	if (uidset != null)
	    for (UIDSet u : uidset) {
		if (uidmax < 0)
		    count += u.size();
		else if (u.start <= uidmax) {
		    if (u.end < uidmax)
			count += u.end - u.start + 1;
		    else
			count += uidmax - u.start + 1;
		}
	    }
	
	return count;
    }
}
