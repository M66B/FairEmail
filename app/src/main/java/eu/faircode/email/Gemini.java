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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class Gemini {
    // https://ai.google.dev/models/gemini
    private static final int MAX_GEMINI_LEN = 4000; // characters
    private static final int TIMEOUT = 30; // seconds

    static boolean isAvailable(Context context) {
        if (TextUtils.isEmpty(BuildConfig.GEMINI_ENDPOINT))
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("gemini_enabled", false);
        String apikey = prefs.getString("gemini_apikey", null);

        return (enabled &&
                (!TextUtils.isEmpty(apikey) || !Objects.equals(getUri(context), BuildConfig.GEMINI_ENDPOINT)));
    }

    static String[] generate(Context context, String model, String[] texts) throws JSONException, IOException {
        JSONArray jpart = new JSONArray();
        for (String text : texts) {
            JSONObject jtext = new JSONObject();
            jtext.put("text", text);
            jpart.put(jtext);
        }

        JSONObject jcontent0 = new JSONObject();
        jcontent0.put("parts", jpart);
        JSONArray jcontents = new JSONArray();
        jcontents.put(jcontent0);
        JSONObject jrequest = new JSONObject();
        jrequest.put("contents", jcontents);

        String path = "models/" + Uri.encode(model) + ":generateContent";

        JSONObject jresponse = call(context, "POST", path, jrequest);

        // {
        //   "promptFeedback": {
        //     "blockReason": "SAFETY",
        //     "safetyRatings": [
        //       {
        //         "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
        //         "probability": "NEGLIGIBLE"
        //       },
        //       {
        //         "category": "HARM_CATEGORY_HATE_SPEECH",
        //         "probability": "NEGLIGIBLE"
        //       },
        //       {
        //         "category": "HARM_CATEGORY_HARASSMENT",
        //         "probability": "MEDIUM"
        //       },
        //       {
        //         "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
        //         "probability": "NEGLIGIBLE"
        //       }
        //     ]
        //   }
        // }

        JSONArray jcandidates = jresponse.optJSONArray("candidates");
        if (jcandidates == null || jcandidates.length() < 1)
            throw new IOException(jresponse.toString(2));
        JSONObject jcontent = jcandidates.getJSONObject(0).optJSONObject("content");
        if (jcontent == null)
            throw new IOException(jresponse.toString(2));
        JSONArray jparts = jcontent.optJSONArray("parts");
        if (jparts == null || jparts.length() < 1)
            throw new IOException(jresponse.toString(2));
        JSONObject jtext = jparts.getJSONObject(0);
        if (!jtext.has("text"))
            throw new IOException(jresponse.toString(2));
        return new String[]{jtext.getString("text")};
    }

    private static String getUri(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("gemini_uri", BuildConfig.GEMINI_ENDPOINT);
    }

    private static JSONObject call(Context context, String method, String path, JSONObject args) throws JSONException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String apikey = prefs.getString("gemini_apikey", null);

        // https://ai.google.dev/tutorials/rest_quickstart
        // https://ai.google.dev/api/rest
        Uri uri = Uri.parse(getUri(context)).buildUpon()
                .appendEncodedPath(path)
                .build();
        Log.i("Gemini uri=" + uri);

        long start = new Date().getTime();

        URL url = new URL(uri.toString());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setDoOutput(args != null);
        connection.setDoInput(true);
        connection.setReadTimeout(TIMEOUT * 1000);
        connection.setConnectTimeout(TIMEOUT * 1000);
        ConnectionHelper.setUserAgent(context, connection);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("x-goog-api-key", apikey);
        connection.connect();

        try {
            if (args != null) {
                String json = args.toString();
                Log.i("Gemini request=" + json);
                connection.getOutputStream().write(json.getBytes());
            }

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
                Log.w("Gemini error=" + error);
                throw new IOException(error);
            }

            String response = Helper.readStream(connection.getInputStream());
            Log.i("Gemini response=" + response);

            return new JSONObject(response);
        } finally {
            connection.disconnect();
            long elapsed = new Date().getTime() - start;
            Log.i("Gemini elapsed=" + (elapsed / 1000f));
        }
    }

    static String truncateParagraphs(@NonNull String text) {
        return truncateParagraphs(text, MAX_GEMINI_LEN);
    }

    static String truncateParagraphs(@NonNull String text, int maxlen) {
        String[] paragraphs = text.split("[\\r\\n]+");

        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (i < paragraphs.length &&
                sb.length() + paragraphs[i].length() + 1 < maxlen)
            sb.append(paragraphs[i++]).append('\n');

        return sb.toString();
    }
}
