/*
 *  BIP39 library, a Java implementation of BIP39
 *  Copyright (C) 2017-2019 Alan Evans, NovaCrypto
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
 *  Original source: https://github.com/NovaCrypto/BIP39
 *  You can contact the authors via github issues.
 */

package io.github.novacrypto.bip39;

import io.github.novacrypto.toruntime.CheckedExceptionToRuntime;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import static io.github.novacrypto.toruntime.CheckedExceptionToRuntime.toRuntime;

/**
 * Not available in all Java implementations, for example will not find the implementation before Android API 26+.
 * See https://developer.android.com/reference/javax/crypto/SecretKeyFactory.html for more details.
 */
public enum JavaxPBKDF2WithHmacSHA512 implements PBKDF2WithHmacSHA512 {
    INSTANCE;

    private SecretKeyFactory skf = getPbkdf2WithHmacSHA512();

    @Override
    public byte[] hash(char[] chars, byte[] salt) {
        final PBEKeySpec spec = new PBEKeySpec(chars, salt, 2048, 512);
        final byte[] encoded = generateSecretKey(spec).getEncoded();
        spec.clearPassword();
        return encoded;
    }

    private SecretKey generateSecretKey(final PBEKeySpec spec) {
        return toRuntime(new CheckedExceptionToRuntime.Func<SecretKey>() {
            @Override
            public SecretKey run() throws Exception {
                return skf.generateSecret(spec);
            }
        });
    }

    private static SecretKeyFactory getPbkdf2WithHmacSHA512() {
        return toRuntime(new CheckedExceptionToRuntime.Func<SecretKeyFactory>() {
            @Override
            public SecretKeyFactory run() throws Exception {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            }
        });
    }
}