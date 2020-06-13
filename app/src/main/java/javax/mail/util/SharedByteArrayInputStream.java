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

package javax.mail.util;

import java.io.*;
import javax.mail.internet.SharedInputStream;

/**
 * A ByteArrayInputStream that implements the SharedInputStream interface,
 * allowing the underlying byte array to be shared between multiple readers.
 *
 * @author  Bill Shannon
 * @since JavaMail 1.4
 */

public class SharedByteArrayInputStream extends ByteArrayInputStream
				implements SharedInputStream {
    /**
     * Position within shared buffer that this stream starts at.
     */
    protected int start = 0;

    /**
     * Create a SharedByteArrayInputStream representing the entire
     * byte array.
     *
     * @param	buf	the byte array
     */
    public SharedByteArrayInputStream(byte[] buf) {
	super(buf);
    }

    /**
     * Create a SharedByteArrayInputStream representing the part
     * of the byte array from <code>offset</code> for <code>length</code>
     * bytes.
     *
     * @param	buf	the byte array
     * @param	offset	offset in byte array to first byte to include
     * @param	length	number of bytes to include
     */
    public SharedByteArrayInputStream(byte[] buf, int offset, int length) {
	super(buf, offset, length);
	start = offset;
    }

    /**
     * Return the current position in the InputStream, as an
     * offset from the beginning of the InputStream.
     *
     * @return  the current position
     */
    @Override
    public long getPosition() {
	return pos - start;
    }

    /**
     * Return a new InputStream representing a subset of the data
     * from this InputStream, starting at <code>start</code> (inclusive)
     * up to <code>end</code> (exclusive).  <code>start</code> must be
     * non-negative.  If <code>end</code> is -1, the new stream ends
     * at the same place as this stream.  The returned InputStream
     * will also implement the SharedInputStream interface.
     *
     * @param	start	the starting position
     * @param	end	the ending position + 1
     * @return		the new stream
     */
    @Override
    public InputStream newStream(long start, long end) {
	if (start < 0)
	    throw new IllegalArgumentException("start < 0");
	if (end == -1)
	    end = count - this.start;
	return new SharedByteArrayInputStream(buf,
				this.start + (int)start, (int)(end - start));
    }
}
