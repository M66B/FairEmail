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

import java.util.LinkedList;
import java.util.List;

final class CharSequenceSplitter {

    private final char separator1;
    private final char separator2;

    CharSequenceSplitter(final char separator1, final char separator2) {
        this.separator1 = separator1;
        this.separator2 = separator2;
    }

    List<CharSequence> split(final CharSequence charSequence) {
        final LinkedList<CharSequence> list = new LinkedList<>();
        int start = 0;
        final int length = charSequence.length();
        for (int i = 0; i < length; i++) {
            final char c = charSequence.charAt(i);
            if (c == separator1 || c == separator2) {
                list.add(charSequence.subSequence(start, i));
                start = i + 1;
            }
        }
        list.add(charSequence.subSequence(start, length));
        return list;
    }
}