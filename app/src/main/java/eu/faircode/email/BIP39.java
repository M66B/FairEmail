package eu.faircode.email;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.Locale;

public class BIP39 {
    // https://github.com/bitcoin/bips/tree/master/bip-0039
    // https://github.com/bitcoin/bips/pull/1129
    static String getWord(@NonNull Locale locale, int index, Context context) {
        try (InputStream is = context.getAssets().open("bip39/" + locale.getLanguage() + ".txt")) {
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
