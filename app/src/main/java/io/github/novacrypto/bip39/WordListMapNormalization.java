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

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

class WordListMapNormalization implements NFKDNormalizer {
    private final Map<CharSequence, String> normalizedMap = new HashMap<>();

    WordListMapNormalization(final WordList wordList) {
        for (int i = 0; i < 1 << 11; i++) {
            final String word = wordList.getWord(i);
            final String normalized = Normalizer.normalize(word, Normalizer.Form.NFKD);
            normalizedMap.put(word, normalized);
            normalizedMap.put(normalized, normalized);
            normalizedMap.put(Normalizer.normalize(word, Normalizer.Form.NFC), normalized);
        }
    }

    @Override
    public String normalize(final CharSequence charSequence) {
        final String normalized = normalizedMap.get(charSequence);
        if (normalized != null)
            return normalized;
        return Normalizer.normalize(charSequence, Normalizer.Form.NFKD);
    }
}