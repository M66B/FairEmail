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

import android.app.Person;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.Transliterator;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.textclassifier.ConversationAction;
import android.view.textclassifier.ConversationActions;
import android.view.textclassifier.TextClassificationManager;
import android.view.textclassifier.TextClassifier;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TextHelper {
    private static final int MIN_WORDS = 7;
    private static final int MAX_DETECT_SAMPLE_SIZE = 8192;
    private static final float MIN_DETECT_PROBABILITY = 0.80f;
    private static final String TRANSLITERATOR = "Any-Latin; Latin-ASCII";
    private static final int MAX_CONVERSATION_SAMPLE_SIZE = 8192;
    private static final long MAX_CONVERSATION_DURATION = 2500; // milliseconds

    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "text");

    static {
        System.loadLibrary("fairemail");
    }

    private static native DetectResult jni_detect_language(byte[] octets);

    static Locale detectLanguage(Context context, String text) {
        // Why not ML kit?
        // https://developers.google.com/ml-kit/terms

        if (TextUtils.isEmpty(text))
            return null;

        if (text.split("\\s+").length < MIN_WORDS)
            return null;

        byte[] octets = text.getBytes();
        byte[] sample;
        if (octets.length < MAX_DETECT_SAMPLE_SIZE)
            sample = octets;
        else {
            sample = new byte[MAX_DETECT_SAMPLE_SIZE];
            System.arraycopy(octets, 0, sample, 0, MAX_DETECT_SAMPLE_SIZE);
        }

        long start = new Date().getTime();
        Log.i("cld3 sample=" + sample.length);
        DetectResult result = jni_detect_language(sample);
        long elapse = new Date().getTime() - start;
        Log.i("cld3 language=" + result + " elapse=" + elapse);

        if (result.probability < MIN_DETECT_PROBABILITY)
            return null;

        try {
            return Locale.forLanguageTag(result.language);
        } catch (Throwable ex) {
            Log.w(ex);
            return null;
        }
    }

    static boolean canTransliterate() {
        if (!BuildConfig.DEBUG)
            return false;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            return false;

        try {
            Transliterator.getInstance(TRANSLITERATOR);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    static boolean isSingleScript(String s) {
        // https://en.wikipedia.org/wiki/IDN_homograph_attack

        if (TextUtils.isEmpty(s))
            return true;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return true;

        int codepoint;
        Character.UnicodeScript script;
        List<Character.UnicodeScript> scripts = new ArrayList<>();
        for (int i = 0; i < s.length(); ) {
            codepoint = s.codePointAt(i);
            i += Character.charCount(codepoint);

            if (Character.isSpaceChar(codepoint))
                continue;

            script = Character.UnicodeScript.of(codepoint);

            if (Character.UnicodeScript.COMMON.equals(script))
                continue;

            if (!scripts.contains(script))
                scripts.add(script);
        }

        if (scripts.size() <= 1)
            return true;

        scripts.remove(Character.UnicodeScript.HAN); // Chinese/Japanese
        scripts.remove(Character.UnicodeScript.HIRAGANA); // Japanese
        scripts.remove(Character.UnicodeScript.KATAKANA); // Japanese

        if (scripts.size() == 0)
            return true; // All Chinese/Japanese
        if (scripts.size() > 1)
            return false;

        // Chinese/Japanese + Latin
        return Character.UnicodeScript.LATIN.equals(scripts.get(0));
    }

    static String getNonLatinCodepoints(String text) {
        int codepoint;
        Character.UnicodeScript us;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); ) {
            codepoint = text.codePointAt(i);
            i += Character.charCount(codepoint);
            us = Character.UnicodeScript.of(codepoint);
            if (!Character.isSpaceChar(codepoint) &&
                    !Character.UnicodeScript.COMMON.equals(us) &&
                    !Character.UnicodeScript.LATIN.equals(us))
                sb.append('<').append(Integer.toHexString(codepoint)).append('>');
            sb.append(Character.toChars(codepoint));
        }
        return sb.toString();
    }

    static String normalizeNotification(Context context, String text) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notify_transliterate = prefs.getBoolean("notify_transliterate", false);
        boolean notify_ascii = prefs.getBoolean("notify_ascii", false);

        if (notify_transliterate)
            text = transliterate(context, text);

        if (notify_ascii) {
            text = Normalizer.normalize(text, Normalizer.Form.NFKD)
                    .replaceAll("[^\\p{ASCII}]", "");
        }

        return text;
    }

    static String transliterate(Context context, String text) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            return text;

        try {
            // http://userguide.icu-project.org/transforms/general
            return Transliterator.getInstance(TRANSLITERATOR).transliterate(text);
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return text;
    }

    static ConversationActions getConversationActions(
            Context context, String[] texts, boolean replies, boolean outgoing, long time) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            return null;

        TextClassificationManager tcm = Helper.getSystemService(context, TextClassificationManager.class);
        if (tcm == null)
            return null;

        Person author = outgoing
                ? ConversationActions.Message.PERSON_USER_SELF
                : ConversationActions.Message.PERSON_USER_OTHERS;
        ZonedDateTime dt = new Date(time)
                .toInstant()
                .atZone(ZoneId.systemDefault());
        List<ConversationActions.Message> input = new ArrayList<>();
        for (String text : texts)
            if (!TextUtils.isEmpty(text)) {
                if (text.length() > MAX_CONVERSATION_SAMPLE_SIZE)
                    text = text.substring(0, MAX_CONVERSATION_SAMPLE_SIZE);
                input.add(new ConversationActions.Message.Builder(author)
                        .setReferenceTime(dt)
                        .setText(text)
                        .build());
            }

        Set<String> excluded = new HashSet<>(Arrays.asList(
                ConversationAction.TYPE_OPEN_URL,
                ConversationAction.TYPE_SEND_EMAIL
        ));
        if (!replies)
            excluded.add(ConversationAction.TYPE_TEXT_REPLY);
        TextClassifier.EntityConfig config =
                new TextClassifier.EntityConfig.Builder()
                        .setExcludedTypes(excluded)
                        .build();

        List<String> hints = Collections.unmodifiableList(Arrays.asList(
                ConversationActions.Request.HINT_FOR_IN_APP
        ));
        ConversationActions.Request request =
                new ConversationActions.Request.Builder(input)
                        .setTypeConfig(config)
                        .setHints(hints)
                        .build();

        Future<ConversationActions> future = executor.submit(new Callable<ConversationActions>() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.Q)
            public ConversationActions call() throws Exception {
                long start = SystemClock.elapsedRealtime();
                try {
                    return tcm.getTextClassifier().suggestConversationActions(request);
                } finally {
                    long elapse = SystemClock.elapsedRealtime() - start;
                    Log.i("Conversation actions=" + elapse + " ms");
                }
            }
        });

        try {
            return future.get(MAX_CONVERSATION_DURATION, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            Log.e(new Throwable("Conversation actions", ex));
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putBoolean("conversation_actions", false).apply();
            return null;
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    public static String formatXml(String xml, int indent) {
        try {
            Document d = Jsoup.parse(xml, "", Parser.xmlParser());
            d.outputSettings().prettyPrint(true).outline(true).indentAmount(indent);
            return d.html();
        } catch (Throwable ex) {
            Log.e(ex);
            return xml;
        }
    }

    private static class DetectResult {
        String language;
        float probability;
        boolean is_reliable;
        float proportion;

        DetectResult(String language, float probability, boolean is_reliable, float proportion) {
            this.language = language;
            this.probability = probability;
            this.is_reliable = is_reliable;
            this.proportion = proportion;
        }

        @Override
        public String toString() {
            return language + " p=" + probability + " r=" + is_reliable + " pr=" + proportion;
        }
    }
}
