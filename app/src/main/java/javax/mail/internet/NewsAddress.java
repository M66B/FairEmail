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

package javax.mail.internet;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Locale;
import javax.mail.*;

/**
 * This class models an RFC1036 newsgroup address.
 *
 * @author Bill Shannon
 * @author John Mani
 */

public class NewsAddress extends Address {

    protected String newsgroup;
    protected String host;	// may be null

    private static final long serialVersionUID = -4203797299824684143L;

    /**
     * Default constructor.
     */
    public NewsAddress() { }

    /**
     * Construct a NewsAddress with the given newsgroup.
     *
     * @param newsgroup	the newsgroup
     */
    public NewsAddress(String newsgroup) {
	this(newsgroup, null);
    }

    /**
     * Construct a NewsAddress with the given newsgroup and host.
     *
     * @param newsgroup	the newsgroup
     * @param host	the host
     */
    public NewsAddress(String newsgroup, String host) {
	// XXX - this method should throw an exception so we can report
	// illegal addresses, but for now just remove whitespace
	this.newsgroup = newsgroup.replaceAll("\\s+", "");
	this.host = host;
    }

    /**
     * Return the type of this address.  The type of a NewsAddress
     * is "news".
     */
    @Override
    public String getType() {
	return "news";
    }

    /**
     * Set the newsgroup.
     *
     * @param	newsgroup	the newsgroup
     */
    public void setNewsgroup(String newsgroup) {
	this.newsgroup = newsgroup;
    }

    /**
     * Get the newsgroup.
     *
     * @return	newsgroup
     */
    public String getNewsgroup() {
	return newsgroup;
    }

    /**
     * Set the host.
     *
     * @param	host	the host
     */
    public void setHost(String host) {
	this.host = host;
    }

    /**
     * Get the host.
     *
     * @return	host
     */
    public String getHost() {
	return host;
    }

    /**
     * Convert this address into a RFC 1036 address.
     *
     * @return		newsgroup
     */
    @Override
    public String toString() {
	return newsgroup;
    }

    /**
     * The equality operator.
     */
    @Override
    public boolean equals(Object a) {
	if (!(a instanceof NewsAddress))
	    return false;

	NewsAddress s = (NewsAddress)a;
	return ((newsgroup == null && s.newsgroup == null) ||
	     (newsgroup != null && newsgroup.equals(s.newsgroup))) &&
	    ((host == null && s.host == null) ||
	     (host != null && s.host != null && host.equalsIgnoreCase(s.host)));
    }

    /**
     * Compute a hash code for the address.
     */
    @Override
    public int hashCode() {
	int hash = 0;
	if (newsgroup != null)
	    hash += newsgroup.hashCode();
	if (host != null)
	    hash += host.toLowerCase(Locale.ENGLISH).hashCode();
	return hash;
    }

    /**
     * Convert the given array of NewsAddress objects into
     * a comma separated sequence of address strings. The
     * resulting string contains only US-ASCII characters, and
     * hence is mail-safe.
     *
     * @param addresses	array of NewsAddress objects
     * @exception   	ClassCastException if any address object in the
     *              	given array is not a NewsAddress objects. Note
     *              	that this is a RuntimeException.
     * @return	    	comma separated address strings
     */
    public static String toString(Address[] addresses) {
	if (addresses == null || addresses.length == 0)
	    return null;

	StringBuilder s =
		new StringBuilder(((NewsAddress)addresses[0]).toString());
	int used = s.length();
	for (int i = 1; i < addresses.length; i++) {
	    s.append(",");
	    used++;
	    String ng = ((NewsAddress)addresses[i]).toString();
	    if (used + ng.length() > 76) {
		s.append("\r\n\t");
		used = 8;
	    }
	    s.append(ng);
	    used += ng.length();
	}
	
	return s.toString();
    }

    /**
     * Parse the given comma separated sequence of newsgroups into
     * NewsAddress objects.
     *
     * @param newsgroups	comma separated newsgroup string
     * @return			array of NewsAddress objects
     * @exception		AddressException if the parse failed
     */
    public static NewsAddress[] parse(String newsgroups) 
				throws AddressException {
	// XXX - verify format of newsgroup name?
	StringTokenizer st = new StringTokenizer(newsgroups, ",");
	List<NewsAddress> nglist = new ArrayList<>();
	while (st.hasMoreTokens()) {
	    String ng = st.nextToken();
	    nglist.add(new NewsAddress(ng));
	}
	return nglist.toArray(new NewsAddress[nglist.size()]);
    }
}
