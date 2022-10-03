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
import android.os.Build;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class LanguageTool {
    static final String LT_URI = "https://api.languagetool.org/v2/";
    private static final int LT_TIMEOUT = 20; // seconds

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

    static List<Suggestion> getSuggestions(Context context, CharSequence text) throws IOException, JSONException {
        if (TextUtils.isEmpty(text))
            return new ArrayList<>();

        // https://languagetool.org/http-api/swagger-ui/#!/default/post_check
        String request =
                "text=" + URLEncoder.encode(text.toString(), StandardCharsets.UTF_8.name()) +
                        "&language=auto";

        // curl -X GET --header 'Accept: application/json' 'https://api.languagetool.org/v2/languages'
        JSONArray jlanguages;
        try (InputStream is = context.getAssets().open("lt.json")) {
            String json = Helper.readStream(is);
            jlanguages = new JSONArray(json);
        }

        String code = null;
        Locale locale = Locale.getDefault();
        for (int i = 0; i < jlanguages.length(); i++) {
            JSONObject jlanguage = jlanguages.getJSONObject(i);
            String c = jlanguage.optString("longCode");
            if (locale.toLanguageTag().equals(c) && c.contains("-")) {
                code = c;
                break;
            }
        }

        if (code != null)
            request += "&preferredVariants=" + code;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean lt_picky = prefs.getBoolean("lt_picky", false);

        if (lt_picky)
            request += "&level=picky";

        String uri = prefs.getString("lt_uri", LT_URI);
        if (!uri.endsWith("/"))
            uri += '/';

        Log.i("LT locale=" + locale + " uri=" + uri + " request=" + request);

        URL url = new URL(uri + "check");
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
                throw new FileNotFoundException(error);
            }

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
