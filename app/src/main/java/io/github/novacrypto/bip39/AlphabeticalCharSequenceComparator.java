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

import java.util.Comparator;

enum CharSequenceComparators implements Comparator<CharSequence> {

    ALPHABETICAL {
        @Override
        public int compare(final CharSequence o1, final CharSequence o2) {
            final int length1 = o1.length();
            final int length2 = o2.length();
            final int length = Math.min(length1, length2);
            for (int i = 0; i < length; i++) {
                final int compare = Character.compare(o1.charAt(i), o2.charAt(i));
                if (compare != 0) return compare;
            }
            return Integer.compare(length1, length2);
        }
    }

}