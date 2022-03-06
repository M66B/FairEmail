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

package io.github.novacrypto.bip39.Validation;

public final class WordNotFoundException extends Exception {
    private final CharSequence word;
    private final CharSequence suggestion1;
    private final CharSequence suggestion2;

    public WordNotFoundException(
            final CharSequence word,
            final CharSequence suggestion1,
            final CharSequence suggestion2) {
        super(String.format(
                "Word not found in word list \"%s\", suggestions \"%s\", \"%s\"",
                word,
                suggestion1,
                suggestion2));
        this.word = word;
        this.suggestion1 = suggestion1;
        this.suggestion2 = suggestion2;
    }

    public CharSequence getWord() {
        return word;
    }

    public CharSequence getSuggestion1() {
        return suggestion1;
    }

    public CharSequence getSuggestion2() {
        return suggestion2;
    }
}