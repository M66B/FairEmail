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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.LocaleList;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.widget.EditText;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class LanguageTool {
    static final String LT_URI = "https://api.languagetool.org/v2/";
    static final String LT_URI_PLUS = "https://api.languagetoolplus.com/v2/";
    private static final int LT_TIMEOUT = 20; // seconds

    private static JSONArray jlanguages = null;

    static boolean isEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("lt_enabled", false);
    }

    static boolean isAuto(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean lt_enabled = prefs.getBoolean("lt_enabled", false);
        boolean lt_auto = prefs.getBoolean("lt_auto", true);
        return (lt_enabled && lt_auto);
    }

    static JSONArray getLanguages(Context context) throws IOException, JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lt_uri = prefs.getString("lt_uri", LT_URI_PLUS);

        // https://languagetool.org/http-api/swagger-ui/#!/default/get_words
        Uri uri = Uri.parse(lt_uri).buildUpon().appendPath("languages").build();
        Log.i("LT uri=" + uri);

        URL url = new URL(uri.toString());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(false);
        connection.setReadTimeout(LT_TIMEOUT * 1000);
        connection.setConnectTimeout(LT_TIMEOUT * 1000);
        ConnectionHelper.setUserAgent(context, connection);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.connect();

        try {
            checkStatus(connection);

            String response = Helper.readStream(connection.getInputStream());
            Log.i("LT response=" + response);

            return new JSONArray(response);
        } finally {
            connection.disconnect();
        }
    }

    static List<Suggestion> getSuggestions(Context context, CharSequence text) throws IOException, JSONException {
        if (TextUtils.isEmpty(text))
            return new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean lt_picky = prefs.getBoolean("lt_picky", false);
        String lt_user = prefs.getString("lt_user", null);
        String lt_key = prefs.getString("lt_key", null);
        boolean isPlus = (!TextUtils.isEmpty(lt_user) && !TextUtils.isEmpty(lt_key));
        String lt_uri = prefs.getString("lt_uri", isPlus ? LT_URI_PLUS : LT_URI);

        // https://languagetool.org/http-api/swagger-ui/#!/default/post_check
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("text", text.toString())
                .appendQueryParameter("language", "auto");

        // curl -X GET --header 'Accept: application/json' 'https://api.languagetool.org/v2/languages'
        if (jlanguages == null)
            jlanguages = getLanguages(context);

        List<Locale> locales = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            locales.add(Locale.getDefault());
        else {
            LocaleList ll = context.getResources().getConfiguration().getLocales();
            for (int i = 0; i < ll.size(); i++)
                locales.add(ll.get(i));
        }

        List<String> code = new ArrayList<>();
        for (Locale locale : locales)
            for (int i = 0; i < jlanguages.length(); i++) {
                JSONObject jlanguage = jlanguages.getJSONObject(i);
                String c = jlanguage.optString("longCode");
                if (locale.toLanguageTag().equals(c) && c.contains("-")) {
                    code.add(c);
                    break;
                }
            }

        if (code.size() > 0)
            builder.appendQueryParameter("preferredVariants", TextUtils.join(",", code));

        String motherTongue = null;
        String slocale = Resources.getSystem().getConfiguration().locale.toLanguageTag();
        for (int i = 0; i < jlanguages.length(); i++) {
            JSONObject jlanguage = jlanguages.getJSONObject(i);
            String c = jlanguage.optString("longCode");
            if (TextUtils.isEmpty(c))
                continue;
            if (slocale.equals(c)) {
                motherTongue = c;
                break;
            }
            if (slocale.split("-")[0].equals(c))
                motherTongue = c;
        }

        if (motherTongue != null)
            builder.appendQueryParameter("motherTongue", motherTongue);

        if (lt_picky)
            builder.appendQueryParameter("level", "picky");

        if (isPlus)
            builder
                    .appendQueryParameter("username", lt_user)
                    .appendQueryParameter("apiKey", lt_key);

        Uri uri = Uri.parse(lt_uri).buildUpon().appendPath("check").build();
        String request = builder.build().toString().substring(1);

        Log.i("LT uri=" + uri + " request=" + request);

        URL url = new URL(uri.toString());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setReadTimeout(LT_TIMEOUT * 1000);
        connection.setConnectTimeout(LT_TIMEOUT * 1000);
        ConnectionHelper.setUserAgent(context, connection);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(request.length()));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.connect();

        try {
            connection.getOutputStream().write(request.getBytes());
            checkStatus(connection);

            String response = Helper.readStream(connection.getInputStream());
            Log.i("LT response=" + response);

            List<Suggestion> result = new ArrayList<>();

            JSONObject jroot = new JSONObject(response);
            JSONArray jmatches = jroot.getJSONArray("matches");
            for (int i = 0; i < jmatches.length(); i++) {
                JSONObject jmatch = jmatches.getJSONObject(i);

                Suggestion suggestion = new Suggestion();
                suggestion.title = jmatch.getString("shortMessage");
                suggestion.description = jmatch.getString("message");
                suggestion.offset = jmatch.getInt("offset");
                suggestion.length = jmatch.getInt("length");

                JSONArray jreplacements = jmatch.getJSONArray("replacements");

                suggestion.replacements = new ArrayList<>();
                for (int j = 0; j < jreplacements.length(); j++) {
                    JSONObject jreplacement = jreplacements.getJSONObject(j);
                    suggestion.replacements.add(jreplacement.getString("value"));
                }

                if (suggestion.replacements.size() > 0)
                    result.add(suggestion);
            }

            return result;
        } finally {
            connection.disconnect();
        }
    }

    static boolean modifyDictionary(Context context, String word, String dictionary, boolean add) throws IOException, JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lt_user = prefs.getString("lt_user", null);
        String lt_key = prefs.getString("lt_key", null);
        String lt_uri = prefs.getString("lt_uri", LT_URI_PLUS);

        if (TextUtils.isEmpty(lt_user) || TextUtils.isEmpty(lt_key))
            return false;

        // https://languagetool.org/http-api/swagger-ui/#!/default/post_words_add
        // https://languagetool.org/http-api/swagger-ui/#!/default/post_words_delete
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("word", word)
                .appendQueryParameter("username", lt_user)
                .appendQueryParameter("apiKey", lt_key);

        if (dictionary != null)
            builder.appendQueryParameter("dict", dictionary);

        Uri uri = Uri.parse(lt_uri).buildUpon()
                .appendPath(add ? "words/add" : "words/delete")
                .build();
        String request = builder.build().toString().substring(1);

        Log.i("LT uri=" + uri + " request=" + request);

        URL url = new URL(uri.toString());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setReadTimeout(LT_TIMEOUT * 1000);
        connection.setConnectTimeout(LT_TIMEOUT * 1000);
        ConnectionHelper.setUserAgent(context, connection);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(request.length()));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.connect();

        try {
            connection.getOutputStream().write(request.getBytes());
            checkStatus(connection);

            String response = Helper.readStream(connection.getInputStream());
            Log.i("LT response=" + response);

            JSONObject jroot = new JSONObject(response);
            return jroot.getBoolean(add ? "added" : "deleted");
        } finally {
            connection.disconnect();
        }
    }

    static List<String> getWords(Context context, String[] dictionary) throws IOException, JSONException {
        List<String> result = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lt_user = prefs.getString("lt_user", null);
        String lt_key = prefs.getString("lt_key", null);
        String lt_uri = prefs.getString("lt_uri", LT_URI_PLUS);

        if (TextUtils.isEmpty(lt_user) || TextUtils.isEmpty(lt_key))
            return result;

        // https://languagetool.org/http-api/swagger-ui/#!/default/get_words
        Uri.Builder builder = Uri.parse(lt_uri).buildUpon()
                .appendPath("words")
                .appendQueryParameter("offset", "0")
                .appendQueryParameter("limit", "500")
                .appendQueryParameter("username", lt_user)
                .appendQueryParameter("apiKey", lt_key);

        if (dictionary != null && dictionary.length > 0)
            builder.appendQueryParameter("dicts", TextUtils.join(",", dictionary));

        Uri uri = builder.build();
        Log.i("LT uri=" + uri);

        URL url = new URL(uri.toString());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(false);
        connection.setReadTimeout(LT_TIMEOUT * 1000);
        connection.setConnectTimeout(LT_TIMEOUT * 1000);
        ConnectionHelper.setUserAgent(context, connection);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.connect();

        try {
            checkStatus(connection);

            String response = Helper.readStream(connection.getInputStream());
            Log.i("LT response=" + response);

            JSONObject jroot = new JSONObject(response);
            JSONArray jwords = jroot.getJSONArray("words");
            for (int i = 0; i < jwords.length(); i++)
                result.add(jwords.getString(i));

            return result;
        } finally {
            connection.disconnect();
        }
    }

    static void applySuggestions(EditText etBody, int start, int end, List<Suggestion> suggestions) {
        Editable edit = etBody.getText();
        if (edit == null)
            return;

        // https://developer.android.com/reference/android/text/style/SuggestionSpan
        for (SuggestionSpanEx suggestion : edit.getSpans(start, end, SuggestionSpanEx.class)) {
            Log.i("LT removing=" + suggestion);
            edit.removeSpan(suggestion);
        }

        if (suggestions != null)
            for (LanguageTool.Suggestion suggestion : suggestions) {
                Log.i("LT adding=" + suggestion);
                SuggestionSpan span = new SuggestionSpanEx(etBody.getContext(),
                        suggestion.replacements.toArray(new String[0]),
                        SuggestionSpan.FLAG_MISSPELLED);
                int s = start + suggestion.offset;
                int e = s + suggestion.length;
                if (s < 0 || s > edit.length() || e < 0 || e > edit.length()) {
                    Log.w("LT " + s + "..." + e + " length=" + edit.length());
                    continue;
                }
                edit.setSpan(span, s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
    }

    static boolean isPremium(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lt_user = prefs.getString("lt_user", null);
        String lt_key = prefs.getString("lt_key", null);
        return (!TextUtils.isEmpty(lt_user) && !TextUtils.isEmpty(lt_key));
    }

    private static void checkStatus(HttpsURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        if (status != HttpsURLConnection.HTTP_OK) {
            String error = "Error " + status + ": " + connection.getResponseMessage();
            try {
                InputStream is = connection.getErrorStream();
                if (is != null)
                    error += "\n" + Helper.readStream(is);
            } catch (Throwable ex) {
                Log.w(ex);
            }
            Log.w("LT " + error);
            throw new FileNotFoundException(error);
        }
    }

    static class Suggestion {
        String title; // shortMessage
        String description; // message
        int offset;
        int length;
        List<String> replacements;

        @Override
        public String toString() {
            return title;
        }
    }

    private static class SuggestionSpanEx extends SuggestionSpan {
        private final int highlightColor;
        private final int dp3;

        public SuggestionSpanEx(Context context, String[] suggestions, int flags) {
            super(context, suggestions, flags);
            highlightColor = Helper.resolveColor(context,
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                            ? android.R.attr.textColorHighlight
                            : android.R.attr.colorError);
            dp3 = Helper.dp2pixels(context, 2);
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                tp.bgColor = highlightColor;
            else {
                tp.underlineColor = highlightColor;
                tp.underlineThickness = dp3;
            }
        }
    }
}
