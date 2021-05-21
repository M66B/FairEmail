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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class DeepL {
    // https://www.deepl.com/docs-api/
    private static final int DEEPL_TIMEOUT = 20; // seconds

    // curl https://api-free.deepl.com/v2/languages \
    //	-d auth_key=42c191db-21ba-9b96-2464-47a9a5e81b4a:fx \
    //	-d type=target

    public static List<Pair<String, String>> getTargetLanguages(Context context) {
        try (InputStream is = context.getAssets().open("deepl.json")) {
            String json = Helper.readStream(is);
            JSONArray jarray = new JSONArray(json);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            List<Pair<String, String>> languages = new ArrayList<>();
            Map<String, Integer> frequencies = new HashMap<>();
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jlanguage = jarray.getJSONObject(i);
                String name = jlanguage.getString("name");
                String target = jlanguage.getString("language");

                Locale locale = Locale.forLanguageTag(target);
                if (locale != null)
                    name = locale.getDisplayName();

                int frequency = prefs.getInt("translated_" + target, 0);
                if (BuildConfig.DEBUG && frequency > 0)
                    name += " ★";

                languages.add(new Pair<>(name, target));
                frequencies.put(target, frequency);
            }

            Collator collator = Collator.getInstance(Locale.getDefault());
            collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
            Collections.sort(languages, new Comparator<Pair<String, String>>() {
                @Override
                public int compare(Pair<String, String> l1, Pair<String, String> l2) {
                    int freq1 = frequencies.get(l1.second);
                    int freq2 = frequencies.get(l2.second);

                    if (freq1 == freq2)
                        return collator.compare(l1.first, l2.first);
                    else
                        return -Integer.compare(freq1, freq2);
                }
            });

            return languages;
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    public static String translate(String text, String target, Context context) throws IOException, JSONException {
        String request =
                "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8.name()) +
                        "&target_lang=" + URLEncoder.encode(target, StandardCharsets.UTF_8.name());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = prefs.getString("deepl_key", null);

        URL url = new URL(getBaseUri(context) + "translate?auth_key=" + key);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setReadTimeout(DEEPL_TIMEOUT * 1000);
        connection.setConnectTimeout(DEEPL_TIMEOUT * 1000);
        connection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Content-Length", Integer.toString(request.length()));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.connect();

        try {
            connection.getOutputStream().write(request.getBytes());

            int status = connection.getResponseCode();
            if (status != HttpsURLConnection.HTTP_OK) {
                String error = "Error " + status + ": " + connection.getResponseMessage();
                try {
                    error += "\n" + Helper.readStream(connection.getErrorStream());
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                throw new FileNotFoundException(error);
            }

            String response = Helper.readStream(connection.getInputStream());

            JSONObject jroot = new JSONObject(response);
            JSONArray jtranslations = jroot.getJSONArray("translations");
            if (jtranslations.length() == 0)
                throw new FileNotFoundException();
            JSONObject jtranslation = (JSONObject) jtranslations.get(0);
            String detected = jtranslation.getString("detected_source_language");
            String translated = jtranslation.getString("text");
            return translated;
        } finally {
            connection.disconnect();
        }
    }

    public static Integer[] getUsage(Context context) throws IOException, JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = prefs.getString("deepl_key", null);

        URL url = new URL(getBaseUri(context) + "usage?auth_key=" + key);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setReadTimeout(DEEPL_TIMEOUT * 1000);
        connection.setConnectTimeout(DEEPL_TIMEOUT * 1000);
        connection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
        connection.connect();

        try {
            int status = connection.getResponseCode();
            if (status != HttpsURLConnection.HTTP_OK) {
                String error = "Error " + status + ": " + connection.getResponseMessage();
                try {
                    error += "\n" + Helper.readStream(connection.getErrorStream());
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                throw new FileNotFoundException(error);
            }

            String response = Helper.readStream(connection.getInputStream());

            JSONObject jroot = new JSONObject(response);
            int count = jroot.getInt("character_count");
            int limit = jroot.getInt("character_limit");
            return new Integer[]{count, limit};
        } finally {
            connection.disconnect();
        }
    }

    private static String getBaseUri(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String domain = prefs.getString("deepl_domain", "api-free.deepl.com");
        return "https://" + domain + "/v2/";
    }
}
