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

package javax.mail;

import java.util.Vector;

/**
 * Clients use a FetchProfile to list the Message attributes that 
 * it wishes to prefetch from the server for a range of messages.<p>
 *
 * Messages obtained from a Folder are light-weight objects that 
 * typically start off as empty references to the actual messages.
 * Such a Message object is filled in "on-demand" when the appropriate 
 * get*() methods are invoked on that particular Message. Certain
 * server-based message access protocols (Ex: IMAP) allow batch
 * fetching of message attributes for a range of messages in a single
 * request. Clients that want to use message attributes for a range of
 * Messages (Example: to display the top-level headers in a headerlist)
 * might want to use the optimization provided by such servers. The
 * <code>FetchProfile</code> allows the client to indicate this desire
 * to the server. <p>
 *
 * Note that implementations are not obligated to support
 * FetchProfiles, since there might be cases where the backend service 
 * does not allow easy, efficient fetching of such profiles. <p>
 *
 * Sample code that illustrates the use of a FetchProfile is given
 * below:
 * <blockquote>
 * <pre>
 *
 *  Message[] msgs = folder.getMessages();
 *
 *  FetchProfile fp = new FetchProfile();
 *  fp.add(FetchProfile.Item.ENVELOPE);
 *  fp.add("X-mailer");
 *  folder.fetch(msgs, fp);
 *
 * </pre></blockquote><p>
 *
 * @see javax.mail.Folder#fetch
 * @author John Mani
 * @author Bill Shannon
 */

public class FetchProfile {

    private Vector<Item> specials; // specials
    private Vector<String> headers; // vector of header names

    /**
     * This inner class is the base class of all items that
     * can be requested in a FetchProfile. The items currently
     * defined here are <code>ENVELOPE</code>, <code>CONTENT_INFO</code>
     * and <code>FLAGS</code>. The <code>UIDFolder</code> interface 
     * defines the <code>UID</code> Item as well. <p>
     *
     * Note that this class only has a protected constructor, therby
     * restricting new Item types to either this class or subclasses.
     * This effectively implements a enumeration of allowed Item types.
     *
     * @see UIDFolder
     */

    public static class Item {
	/**
	 * This is the Envelope item. <p>
	 *
	 * The Envelope is an aggregration of the common attributes
	 * of a Message. Implementations should include the following
	 * attributes: From, To, Cc, Bcc, ReplyTo, Subject and Date.
	 * More items may be included as well. <p>
	 *
	 * For implementations of the IMAP4 protocol (RFC 2060), the 
	 * Envelope should include the ENVELOPE data item. More items
	 * may be included too.
	 */
	public static final Item ENVELOPE = new Item("ENVELOPE");

	/**
	 * This item is for fetching information about the 
	 * content of the message. <p>
	 *
	 * This includes all the attributes that describe the content
	 * of the message. Implementations should include the following
	 * attributes: ContentType, ContentDisposition, 
	 * ContentDescription, Size and LineCount. Other items may be
	 * included as well.
	 */
	public static final Item CONTENT_INFO = new Item("CONTENT_INFO");

	/**
	 * SIZE is a fetch profile item that can be included in a
	 * <code>FetchProfile</code> during a fetch request to a Folder.
	 * This item indicates that the sizes of the messages in the specified 
	 * range should be prefetched. <p>
	 *
	 * @since	JavaMail 1.5
	 */
	public static final Item SIZE = new Item("SIZE");

	/**
	 * This is the Flags item.
	 */
	public static final Item FLAGS = new Item("FLAGS");

	private String name;

	/**
	 * Constructor for an item.  The name is used only for debugging.
	 *
	 * @param	name	the item name
	 */
	protected Item(String name) {
	    this.name = name;
	}

	/**
	 * Include the name in the toString return value for debugging.
	 */
	@Override
	public String toString() {
	    return getClass().getName() + "[" + name + "]";
	}
    }

    /**
     * Create an empty FetchProfile.
     */
    public FetchProfile() { 
	specials = null;
	headers = null;
    }
    
    /**
     * Add the given special item as one of the attributes to
     * be prefetched.
     *
     * @param	item		the special item to be fetched
     * @see	FetchProfile.Item#ENVELOPE
     * @see	FetchProfile.Item#CONTENT_INFO
     * @see	FetchProfile.Item#FLAGS
     */
    public void add(Item item) { 
	if (specials == null)
	    specials = new Vector<>();
	specials.addElement(item);
    }

    /**
     * Add the specified header-field to the list of attributes
     * to be prefetched.
     *
     * @param	headerName	header to be prefetched
     */
    public void add(String headerName) { 
   	if (headers == null)
	    headers = new Vector<>();
	headers.addElement(headerName);
    }

    /**
     * Returns true if the fetch profile contains the given special item.
     *
     * @param	item	the Item to test
     * @return true if the fetch profile contains the given special item
     */
    public boolean contains(Item item) { 
   	return specials != null && specials.contains(item);
    }

    /**
     * Returns true if the fetch profile contains the given header name.
     *
     * @param	headerName	the header to test
     * @return	true if the fetch profile contains the given header name
     */
    public boolean contains(String headerName) { 
   	return headers != null && headers.contains(headerName);
    }

    /**
     * Get the items set in this profile. 
     *
     * @return		items set in this profile
     */
    public Item[] getItems() { 
	if (specials == null)
	    return new Item[0];

   	Item[] s = new Item[specials.size()];
	specials.copyInto(s);
	return s;
    }

    /**
     * Get the names of the header-fields set in this profile. 
     *
     * @return		headers set in this profile
     */
    public String[] getHeaderNames() { 
	if (headers == null)
	    return new String[0];

   	String[] s = new String[headers.size()];
	headers.copyInto(s);
	return s;
    }
}
