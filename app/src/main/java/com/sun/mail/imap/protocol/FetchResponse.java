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

import java.io.*;
import java.util.*;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.iap.*;

/**
 * This class represents a FETCH response obtained from the input stream
 * of an IMAP server.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class FetchResponse extends IMAPResponse {
    /*
     * Regular Items are saved in the items array.
     * Extension items (items handled by subclasses
     * that extend the IMAP provider) are saved in the
     * extensionItems map, indexed by the FETCH item name.
     * The map is only created when needed.
     *
     * XXX - Should consider unifying the handling of
     * regular items and extension items.
     */
    private Item[] items;
    private Map<String, Object> extensionItems;
    private final FetchItem[] fitems;

    public FetchResponse(Protocol p) 
		throws IOException, ProtocolException {
	super(p);
	fitems = null;
	parse();
    }

    public FetchResponse(IMAPResponse r)
		throws IOException, ProtocolException {
	this(r, null);
    }

    /**
     * Construct a FetchResponse that handles the additional FetchItems.
     *
     * @param	r	the IMAPResponse
     * @param	fitems	the fetch items
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     * @since JavaMail 1.4.6
     */
    public FetchResponse(IMAPResponse r, FetchItem[] fitems)
		throws IOException, ProtocolException {
	super(r);
	this.fitems = fitems;
	parse();
    }

    public int getItemCount() {
	return items.length;
    }

    public Item getItem(int index) {
	return items[index];
    }

    public <T extends Item> T getItem(Class<T> c) {
	for (int i = 0; i < items.length; i++) {
	    if (c.isInstance(items[i]))
		return c.cast(items[i]);
	}

	return null;
    }

    /**
     * Return the first fetch response item of the given class
     * for the given message number.
     *
     * @param	r	the responses
     * @param	msgno	the message number
     * @param	c	the class
     * @param	<T>	the type of fetch item
     * @return		the fetch item
     */
    public static <T extends Item> T getItem(Response[] r, int msgno,
				Class<T> c) {
	if (r == null)
	    return null;

	for (int i = 0; i < r.length; i++) {

	    if (r[i] == null ||
		!(r[i] instanceof FetchResponse) ||
		((FetchResponse)r[i]).getNumber() != msgno)
		continue;

	    FetchResponse f = (FetchResponse)r[i];
	    for (int j = 0; j < f.items.length; j++) {
		if (c.isInstance(f.items[j]))
		    return c.cast(f.items[j]);
	    }
	}

	return null;
    }

    /**
     * Return all fetch response items of the given class
     * for the given message number.
     *
     * @param	r	the responses
     * @param	msgno	the message number
     * @param	c	the class
     * @param	<T>	the type of fetch items
     * @return		the list of fetch items
     * @since JavaMail 1.5.2
     */
    public static <T extends Item> List<T> getItems(Response[] r, int msgno,
				Class<T> c) {
	List<T> items = new ArrayList<>();

	if (r == null)
	    return items;

	for (int i = 0; i < r.length; i++) {

	    if (r[i] == null ||
		!(r[i] instanceof FetchResponse) ||
		((FetchResponse)r[i]).getNumber() != msgno)
		continue;

	    FetchResponse f = (FetchResponse)r[i];
	    for (int j = 0; j < f.items.length; j++) {
		if (c.isInstance(f.items[j]))
		    items.add(c.cast(f.items[j]));
	    }
	}

	return items;
    }

    /**
     * Return a map of the extension items found in this fetch response.
     * The map is indexed by extension item name.  Callers should not
     * modify the map.
     *
     * @return	Map of extension items, or null if none
     * @since JavaMail 1.4.6
     */
    public Map<String, Object> getExtensionItems() {
	return extensionItems;
    }

    private final static char[] HEADER = {'.','H','E','A','D','E','R'};
    private final static char[] TEXT = {'.','T','E','X','T'};

    private void parse() throws ParsingException {
	if (!isNextNonSpace('('))
	    throw new ParsingException(
		"error in FETCH parsing, missing '(' at index " + index);

	List<Item> v = new ArrayList<>();
	Item i = null;
	skipSpaces();
	do {

	    if (index >= size)
		throw new ParsingException(
		"error in FETCH parsing, ran off end of buffer, size " + size);

	    i = parseItem();
	    if (i != null)
		v.add(i);
	    else if (!parseExtensionItem())
		throw new ParsingException(
		    "error in FETCH parsing, unrecognized item at index " +
		    index + ", starts with \"" + next20() + "\"");
	} while (!isNextNonSpace(')'));

	items = v.toArray(new Item[v.size()]);
    }

    /**
     * Return the next 20 characters in the buffer, for exception messages.
     */
    private String next20() {
	if (index + 20 > size)
	    return ASCIIUtility.toString(buffer, index, size);
	else
	    return ASCIIUtility.toString(buffer, index, index + 20) + "...";
    }

    /**
     * Parse the item at the current position in the buffer,
     * skipping over the item if successful.  Otherwise, return null
     * and leave the buffer position unmodified.
     */
    @SuppressWarnings("empty")
    private Item parseItem() throws ParsingException {
	switch (buffer[index]) {
	case 'E': case 'e':
	    if (match(ENVELOPE.name))
		return new ENVELOPE(this);
	    break;
	case 'F': case 'f':
	    if (match(FLAGS.name))
		return new FLAGS((IMAPResponse)this);
	    break;
	case 'I': case 'i':
	    if (match(INTERNALDATE.name))
		return new INTERNALDATE(this);
	    break;
	case 'B': case 'b':
	    if (match(BODYSTRUCTURE.name))
		return new BODYSTRUCTURE(this);
	    else if (match(BODY.name)) {
		if (buffer[index] == '[')
		    return new BODY(this);
		else
		    return new BODYSTRUCTURE(this);
	    }
	    break;
	case 'R': case 'r':
	    if (match(RFC822SIZE.name))
		return new RFC822SIZE(this);
	    else if (match(RFC822DATA.name)) {
		boolean isHeader = false;
		if (match(HEADER))
		    isHeader = true;	// skip ".HEADER"
		else if (match(TEXT))
		    isHeader = false;	// skip ".TEXT"
		return new RFC822DATA(this, isHeader);
	    }
	    break;
	case 'U': case 'u':
	    if (match(UID.name))
		return new UID(this);
	    break;
	case 'M': case 'm':
	    if (match(MODSEQ.name))
		return new MODSEQ(this);
	    break;
	default: 
	    break;
	}
	return null;
    }

    /**
     * If this item is a known extension item, parse it.
     */
    private boolean parseExtensionItem() throws ParsingException {
	if (fitems == null)
	    return false;
	for (int i = 0; i < fitems.length; i++) {
	    if (match(fitems[i].getName())) {
		if (extensionItems == null)
		    extensionItems = new HashMap<>();
		extensionItems.put(fitems[i].getName(),
				    fitems[i].parseItem(this));
		return true;
	    }
	}
	return false;
    }

    /**
     * Does the current buffer match the given item name?
     * itemName is the name of the IMAP item to compare against.
     * NOTE that itemName *must* be all uppercase.
     * If the match is successful, the buffer pointer (index)
     * is incremented past the matched item.
     */
    private boolean match(char[] itemName) {
	int len = itemName.length;
	for (int i = 0, j = index; i < len;)
	    // IMAP tokens are case-insensitive. We store itemNames in
	    // uppercase, so convert operand to uppercase before comparing.
	    if (Character.toUpperCase((char)buffer[j++]) != itemName[i++])
		return false;
	index += len;
	return true;
    }

    /**
     * Does the current buffer match the given item name?
     * itemName is the name of the IMAP item to compare against.
     * NOTE that itemName *must* be all uppercase.
     * If the match is successful, the buffer pointer (index)
     * is incremented past the matched item.
     */
    private boolean match(String itemName) {
	int len = itemName.length();
	for (int i = 0, j = index; i < len;)
	    // IMAP tokens are case-insensitive. We store itemNames in
	    // uppercase, so convert operand to uppercase before comparing.
	    if (Character.toUpperCase((char)buffer[j++]) !=
		    itemName.charAt(i++))
		return false;
	index += len;
	return true;
    }
}
