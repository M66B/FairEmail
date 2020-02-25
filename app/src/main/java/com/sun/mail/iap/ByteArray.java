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

package com.sun.mail.iap;

import java.io.ByteArrayInputStream;

/**
 * A simple wrapper around a byte array, with a start position and
 * count of bytes.
 *
 * @author  John Mani
 */

public class ByteArray {
    private byte[] bytes; // the byte array
    private int start;	  // start position
    private int count;	  // count of bytes

    /**
     * Constructor
     *
     * @param	b	the byte array to wrap
     * @param	start	start position in byte array
     * @param	count	number of bytes in byte array
     */
    public ByteArray(byte[] b, int start, int count) {
	bytes = b;
	this.start = start;
	this.count = count;
    }

    /**
     * Constructor that creates a byte array of the specified size.
     *
     * @param	size	the size of the ByteArray
     * @since	JavaMail 1.4.1
     */
    public ByteArray(int size) {
	this(new byte[size], 0, size);
    }

    /**
     * Returns the internal byte array. Note that this is a live
     * reference to the actual data, not a copy.
     *
     * @return	the wrapped byte array
     */
    public byte[] getBytes() {
	return bytes;
    }

    /**
     * Returns a new byte array that is a copy of the data.
     *
     * @return	a new byte array with the bytes from start for count
     */
    public byte[] getNewBytes() {
	byte[] b = new byte[count];
	System.arraycopy(bytes, start, b, 0, count);
	return b;
    }

    /**
     * Returns the start position
     *
     * @return	the start position
     */
    public int getStart() {
	return start;
    }

    /**
     * Returns the count of bytes
     *
     * @return	the number of bytes
     */
    public int getCount() {
	return count;
    }

    /**
     * Set the count of bytes.
     *
     * @param	count	the number of bytes
     * @since	JavaMail 1.4.1
     */
    public void setCount(int count) {
	this.count = count;
    }

    /**
     * Returns a ByteArrayInputStream.
     *
     * @return	the ByteArrayInputStream
     */
    public ByteArrayInputStream toByteArrayInputStream() {
	return new ByteArrayInputStream(bytes, start, count);
    }

    /**
     * Grow the byte array by incr bytes.
     *
     * @param	incr	how much to grow
     * @since	JavaMail 1.4.1
     */
    public void grow(int incr) {
	byte[] nbuf = new byte[bytes.length + incr];
	System.arraycopy(bytes, 0, nbuf, 0, bytes.length);
	bytes = nbuf;
    }
}
