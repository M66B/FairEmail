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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BIP39 {
    // https://github.com/bitcoin/bips/tree/master/bip-0039
    // https://github.com/bitcoin/bips/pull/1129
    static String getMnemonic(@NonNull Locale locale, int words, Context context) {
        List<String> list = new ArrayList<>();
        SecureRandom rnd = new SecureRandom();
        for (int i = 0; i < words; i++)
            list.add(BIP39.getWord(locale, rnd.nextInt(2048), context));
        String delimiter = ("ja".equals(locale.getLanguage()) ? "\u3000" : " ");
        return TextUtils.join(delimiter, list);
    }

    private static String getWord(@NonNull Locale locale, int index, Context context) {
        String lang = locale.getLanguage();
        if ("zh".equals(lang) && "CN".equals(locale.getCountry()))
            lang = "zh_cn";
        try (InputStream is = context.getAssets().open("bip39/" + lang + ".txt")) {
            return getWord(is, index);
        } catch (Throwable ex) {
            Log.w(ex);
            try (InputStream is = context.getAssets().open("bip39/en.txt")) {
                return getWord(is, index);
            } catch (Throwable exex) {
                Log.e(exex);
                return getRandomWord(5);
            }
        }
    }

    private static String getWord(InputStream is, int index) throws IOException {
        String word = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        for (int i = 0; i <= index; i++)
            word = br.readLine();
        return word;
    }

    private static String getRandomWord(int len) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append((char) ('a' + rnd.nextInt(26)));
        return sb.toString();
    }
}
