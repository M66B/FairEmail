package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import java.security.SecureRandom;

import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;

public class MnemonicHelper {
    // https://github.com/NovaCrypto/BIP39

    static String get(byte[] entropy) {
        StringBuilder sb = new StringBuilder();
        new MnemonicGenerator(English.INSTANCE).createMnemonic(entropy, sb::append);
        return sb.toString();
    }

    static String get(String hex) {
        return get(fromHex(hex));
    }

    static byte[] generate() {
        byte[] entropy = new byte[Words.TWELVE.byteLength()];
        new SecureRandom().nextBytes(entropy);
        return entropy;
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] =
                    (byte) ((Character.digit(hex.charAt(i), 16) << 4) +
                            Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
