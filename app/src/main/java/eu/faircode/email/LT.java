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

public class LT {
    private static final int LT_TIMEOUT = 20; // seconds

    static List<Suggestion> getSuggestions(Context context, CharSequence text) throws IOException, JSONException {
        // https://languagetool.org/http-api/swagger-ui/#!/default/post_check
        String request =
                "text=" + URLEncoder.encode(text.toString(), StandardCharsets.UTF_8.name()) +
                        "&language=auto";

        // curl -X GET --header 'Accept: application/json' 'https://api.languagetool.org/v2/languages
        String code = null;
        JSONArray jlanguages;
        Locale locale = Locale.getDefault();
        try (InputStream is = context.getAssets().open("lt.json")) {
            String json = Helper.readStream(is);
            jlanguages = new JSONArray(json);
        }
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

        Log.i("LT locale=" + locale + " request=" + request);

        URL url = new URL(BuildConfig.LT_URI + "check");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setReadTimeout(LT_TIMEOUT * 1000);
        connection.setConnectTimeout(LT_TIMEOUT * 1000);
        connection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
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
}
