/*
 *  SHA-256 library
 *
 *  Copyright (C) 2017-2022 Alan Evans, NovaCrypto
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Original source: https://github.com/NovaCrypto/SHA256
 *  You can contact the authors via github issues.
 */

package io.github.novacrypto.hashing;

import io.github.novacrypto.toruntime.CheckedExceptionToRuntime;

import java.security.MessageDigest;

import static io.github.novacrypto.toruntime.CheckedExceptionToRuntime.toRuntime;

public final class Sha256 {

    Sha256() {
    }

    public static byte[] sha256(final byte[] bytes) {
        return sha256(bytes, 0, bytes.length);
    }

    public static byte[] sha256(final byte[] bytes, final int offset, final int length) {
        final MessageDigest digest = sha256();
        digest.update(bytes, offset, length);
        return digest.digest();
    }

    public static byte[] sha256Twice(final byte[] bytes) {
        return sha256Twice(bytes, 0, bytes.length);
    }

    public static byte[] sha256Twice(final byte[] bytes, final int offset, final int length) {
        final MessageDigest digest = sha256();
        digest.update(bytes, offset, length);
        digest.update(digest.digest());
        return digest.digest();
    }

    private static MessageDigest sha256() {
        return toRuntime(new CheckedExceptionToRuntime.Func<MessageDigest>() {
            @Override
            public MessageDigest run() throws Exception {
                return MessageDigest.getInstance("SHA-256");
            }
        });
    }
}