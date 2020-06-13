/*
 * Copyright (c) 2005, 2019 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Copied from OpenJDK with permission.
 */

package com.sun.mail.auth;

import java.security.*;

//import static sun.security.provider.ByteArrayAccess.*;

/**
 * The MD4 class is used to compute an MD4 message digest over a given
 * buffer of bytes. It is an implementation of the RSA Data Security Inc
 * MD4 algorithim as described in internet RFC 1320.
 *
 * @author      Andreas Sterbenz
 * @author      Bill Shannon (adapted for Jakarta Mail)
 */
public final class MD4 {

    // state of this object
    private final int[] state;
    // temporary buffer, used by implCompress()
    private final int[] x;

    // size of the input to the compression function in bytes
    private static final int blockSize = 64;

    // buffer to store partial blocks, blockSize bytes large
    private final byte[] buffer = new byte[blockSize];
    // offset into buffer
    private int bufOfs;

    // number of bytes processed so far.
    // also used as a flag to indicate reset status
    // -1: need to call engineReset() before next call to update()
    //  0: is already reset
    private long bytesProcessed;

    // rotation constants
    private static final int S11 = 3;
    private static final int S12 = 7;
    private static final int S13 = 11;
    private static final int S14 = 19;
    private static final int S21 = 3;
    private static final int S22 = 5;
    private static final int S23 = 9;
    private static final int S24 = 13;
    private static final int S31 = 3;
    private static final int S32 = 9;
    private static final int S33 = 11;
    private static final int S34 = 15;

    private static final byte[] padding;

    static {
        padding = new byte[136];
        padding[0] = (byte)0x80;
    }

    /**
     * Standard constructor, creates a new MD4 instance.
     */
    public MD4() {
        state = new int[4];
        x = new int[16];
        implReset();
    }

    /**
     * Compute and return the message digest of the input byte array.
     *
     * @param	in	the input byte array
     * @return	the message digest byte array
     */
    public byte[] digest(byte[] in) {
	implReset();
	engineUpdate(in, 0, in.length);
	byte[] out = new byte[16];
	implDigest(out, 0);
	return out;
    }

    /**
     * Reset the state of this object.
     */
    private void implReset() {
        // Load magic initialization constants.
        state[0] = 0x67452301;
        state[1] = 0xefcdab89;
        state[2] = 0x98badcfe;
        state[3] = 0x10325476;
        bufOfs = 0;
        bytesProcessed = 0;
    }

    /**
     * Perform the final computations, any buffered bytes are added
     * to the digest, the count is added to the digest, and the resulting
     * digest is stored.
     */
    private void implDigest(byte[] out, int ofs) {
        long bitsProcessed = bytesProcessed << 3;

        int index = (int)bytesProcessed & 0x3f;
        int padLen = (index < 56) ? (56 - index) : (120 - index);
        engineUpdate(padding, 0, padLen);

        //i2bLittle4((int)bitsProcessed, buffer, 56);
        //i2bLittle4((int)(bitsProcessed >>> 32), buffer, 60);
	buffer[56] = (byte)bitsProcessed;
	buffer[57] = (byte)(bitsProcessed>>8);
	buffer[58] = (byte)(bitsProcessed>>16);
	buffer[59] = (byte)(bitsProcessed>>24);
	buffer[60] = (byte)(bitsProcessed>>32);
	buffer[61] = (byte)(bitsProcessed>>40);
	buffer[62] = (byte)(bitsProcessed>>48);
	buffer[63] = (byte)(bitsProcessed>>56);
        implCompress(buffer, 0);

        //i2bLittle(state, 0, out, ofs, 16);
	for (int i = 0; i < state.length; i++) {
	    int x = state[i];
	    out[ofs++] = (byte)x;
	    out[ofs++] = (byte)(x>>8);
	    out[ofs++] = (byte)(x>>16);
	    out[ofs++] = (byte)(x>>24);
	}
    }

    private void engineUpdate(byte[] b, int ofs, int len) {
        if (len == 0) {
            return;
        }
        if ((ofs < 0) || (len < 0) || (ofs > b.length - len)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (bytesProcessed < 0) {
            implReset();
        }
        bytesProcessed += len;
        // if buffer is not empty, we need to fill it before proceeding
        if (bufOfs != 0) {
            int n = Math.min(len, blockSize - bufOfs);
            System.arraycopy(b, ofs, buffer, bufOfs, n);
            bufOfs += n;
            ofs += n;
            len -= n;
            if (bufOfs >= blockSize) {
                // compress completed block now
                implCompress(buffer, 0);
                bufOfs = 0;
            }
        }
        // compress complete blocks
        while (len >= blockSize) {
            implCompress(b, ofs);
            len -= blockSize;
            ofs += blockSize;
        }
        // copy remainder to buffer
        if (len > 0) {
            System.arraycopy(b, ofs, buffer, 0, len);
            bufOfs = len;
        }
    }

    private static int FF(int a, int b, int c, int d, int x, int s) {
        a += ((b & c) | ((~b) & d)) + x;
        return ((a << s) | (a >>> (32 - s)));
    }

    private static int GG(int a, int b, int c, int d, int x, int s) {
        a += ((b & c) | (b & d) | (c & d)) + x + 0x5a827999;
        return ((a << s) | (a >>> (32 - s)));
    }

    private static int HH(int a, int b, int c, int d, int x, int s) {
        a += ((b ^ c) ^ d) + x + 0x6ed9eba1;
        return ((a << s) | (a >>> (32 - s)));
    }

    /**
     * This is where the functions come together as the generic MD4
     * transformation operation. It consumes 64
     * bytes from the buffer, beginning at the specified offset.
     */
    private void implCompress(byte[] buf, int ofs) {
        //b2iLittle64(buf, ofs, x);
	for (int xfs = 0; xfs < x.length; xfs++) {
	    x[xfs] = (buf[ofs] & 0xff) | ((buf[ofs+1] & 0xff) << 8) |
		((buf[ofs+2] & 0xff) << 16) | ((buf[ofs+3] & 0xff) << 24);
	    ofs += 4;
	}

        int a = state[0];
        int b = state[1];
        int c = state[2];
        int d = state[3];

        /* Round 1 */
        a = FF (a, b, c, d, x[ 0], S11); /* 1 */
        d = FF (d, a, b, c, x[ 1], S12); /* 2 */
        c = FF (c, d, a, b, x[ 2], S13); /* 3 */
        b = FF (b, c, d, a, x[ 3], S14); /* 4 */
        a = FF (a, b, c, d, x[ 4], S11); /* 5 */
        d = FF (d, a, b, c, x[ 5], S12); /* 6 */
        c = FF (c, d, a, b, x[ 6], S13); /* 7 */
        b = FF (b, c, d, a, x[ 7], S14); /* 8 */
        a = FF (a, b, c, d, x[ 8], S11); /* 9 */
        d = FF (d, a, b, c, x[ 9], S12); /* 10 */
        c = FF (c, d, a, b, x[10], S13); /* 11 */
        b = FF (b, c, d, a, x[11], S14); /* 12 */
        a = FF (a, b, c, d, x[12], S11); /* 13 */
        d = FF (d, a, b, c, x[13], S12); /* 14 */
        c = FF (c, d, a, b, x[14], S13); /* 15 */
        b = FF (b, c, d, a, x[15], S14); /* 16 */

        /* Round 2 */
        a = GG (a, b, c, d, x[ 0], S21); /* 17 */
        d = GG (d, a, b, c, x[ 4], S22); /* 18 */
        c = GG (c, d, a, b, x[ 8], S23); /* 19 */
        b = GG (b, c, d, a, x[12], S24); /* 20 */
        a = GG (a, b, c, d, x[ 1], S21); /* 21 */
        d = GG (d, a, b, c, x[ 5], S22); /* 22 */
        c = GG (c, d, a, b, x[ 9], S23); /* 23 */
        b = GG (b, c, d, a, x[13], S24); /* 24 */
        a = GG (a, b, c, d, x[ 2], S21); /* 25 */
        d = GG (d, a, b, c, x[ 6], S22); /* 26 */
        c = GG (c, d, a, b, x[10], S23); /* 27 */
        b = GG (b, c, d, a, x[14], S24); /* 28 */
        a = GG (a, b, c, d, x[ 3], S21); /* 29 */
        d = GG (d, a, b, c, x[ 7], S22); /* 30 */
        c = GG (c, d, a, b, x[11], S23); /* 31 */
        b = GG (b, c, d, a, x[15], S24); /* 32 */

        /* Round 3 */
        a = HH (a, b, c, d, x[ 0], S31); /* 33 */
        d = HH (d, a, b, c, x[ 8], S32); /* 34 */
        c = HH (c, d, a, b, x[ 4], S33); /* 35 */
        b = HH (b, c, d, a, x[12], S34); /* 36 */
        a = HH (a, b, c, d, x[ 2], S31); /* 37 */
        d = HH (d, a, b, c, x[10], S32); /* 38 */
        c = HH (c, d, a, b, x[ 6], S33); /* 39 */
        b = HH (b, c, d, a, x[14], S34); /* 40 */
        a = HH (a, b, c, d, x[ 1], S31); /* 41 */
        d = HH (d, a, b, c, x[ 9], S32); /* 42 */
        c = HH (c, d, a, b, x[ 5], S33); /* 43 */
        b = HH (b, c, d, a, x[13], S34); /* 44 */
        a = HH (a, b, c, d, x[ 3], S31); /* 45 */
        d = HH (d, a, b, c, x[11], S32); /* 46 */
        c = HH (c, d, a, b, x[ 7], S33); /* 47 */
        b = HH (b, c, d, a, x[15], S34); /* 48 */

        state[0] += a;
        state[1] += b;
        state[2] += c;
        state[3] += d;
    }
}
