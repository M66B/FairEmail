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

import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * This implementation is useful for older Java implementations, for example it is suitable for all Android API levels.
 */
public enum SpongyCastlePBKDF2WithHmacSHA512 implements PBKDF2WithHmacSHA512 {
    INSTANCE;

    @Override
    public byte[] hash(char[] chars, byte[] salt) {
        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA512Digest());
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(chars), salt, 2048);
        KeyParameter key = (KeyParameter) generator.generateDerivedMacParameters(512);
        return key.getKey();
    }
}