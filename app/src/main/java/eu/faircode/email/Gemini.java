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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Gemini {
    // https://ai.google.dev/models/gemini
    static final String DEFAULT_MODEL = "gemini-pro";
    static final float DEFAULT_TEMPERATURE = 0.9f;
    static final String DEFAULT_SUMMARY_PROMPT = "Summarize the following text:";
    static final String DEFAULT_ANSWER_PROMPT = "Answer this message:";

    static final String MODEL = "model";
    static final String USER = "user";

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

    static Message[] generate(Context context, String model, Message[] messages, Float temperature, int n) throws JSONException, IOException {
        //https://ai.google.dev/api/rest/v1beta/models/generateContent
        JSONArray jcontents = new JSONArray();
        for (Message message : messages) {
            JSONArray jparts = new JSONArray();
            for (String text : message.getContent()) {
                JSONObject jtext = new JSONObject();
                jtext.put("text", text);
                jparts.put(jtext);
            }

            JSONObject jcontent = new JSONObject();
            jcontent.put("parts", jparts);
            jcontent.put("role", message.role);

            jcontents.put(jcontent);
        }

        // https://ai.google.dev/api/rest/v1beta/GenerationConfig
        JSONObject jconfig = new JSONObject();
        if (temperature != null)
            jconfig.put("temperature", temperature);
        jconfig.put("candidate_count", n);

        // https://ai.google.dev/api/rest/v1beta/SafetySetting
        JSONArray jsafety = new JSONArray();

        JSONObject jsex = new JSONObject();
        jsex.put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT");
        jsex.put("threshold", "BLOCK_ONLY_HIGH");
        jsafety.put(jsex);

        JSONObject jhate = new JSONObject();
        jhate.put("category", "HARM_CATEGORY_HATE_SPEECH");
        jhate.put("threshold", "BLOCK_ONLY_HIGH");
        jsafety.put(jhate);

        JSONObject jharass = new JSONObject();
        jharass.put("category", "HARM_CATEGORY_HARASSMENT");
        jharass.put("threshold", "BLOCK_ONLY_HIGH");
        jsafety.put(jharass);

        JSONObject jdanger = new JSONObject();
        jdanger.put("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
        jdanger.put("threshold", "BLOCK_ONLY_HIGH");
        jsafety.put(jdanger);

        JSONObject jrequest = new JSONObject();
        jrequest.put("contents", jcontents);
        jrequest.put("generationConfig", jconfig);
        jrequest.put("safetySettings", jsafety);

        String path = "models/" + Uri.encode(model) + ":generateContent";

        JSONObject jresponse = call(context, "POST", path, jrequest);

        List<Message> result = new ArrayList<>();

        JSONArray jcandidates = jresponse.optJSONArray("candidates");
        for (int i = 0; i < jcandidates.length(); i++) {
            JSONObject jcandidate = jcandidates.getJSONObject(i);

            if (!jcandidate.has("content"))
                throw new IOException(jresponse.toString(2));

            JSONObject jcontent = jcandidate.getJSONObject("content");

            String role = jcontent.getString("role");

            List<String> texts = new ArrayList<>();
            JSONArray jparts = jcontent.getJSONArray("parts");
            for (int j = 0; j < jparts.length(); j++) {
                JSONObject jpart = jparts.getJSONObject(j);
                texts.add(jpart.getString("text"));
            }

            result.add(new Message(role, texts.toArray(new String[0])));
        }

        return result.toArray(new Message[0]);
    }

    private static String getUri(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("gemini_uri", BuildConfig.GEMINI_ENDPOINT);
    }

    private static JSONObject call(Context context, String method, String path, JSONObject args) throws JSONException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String apikey = prefs.getString("gemini_apikey", null);

        // https://ai.google.dev/api/rest
        // https://ai.google.dev/tutorials/rest_quickstart
        Uri uri = Uri.parse(getUri(context)).buildUpon()
                .appendEncodedPath(path)
                .build();
        Log.i("Gemini uri=" + uri);

        long start = new Date().getTime();

        URL url = new URL(uri.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

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
            if (status != HttpURLConnection.HTTP_OK) {
                String error = "Error " + status + ": " + connection.getResponseMessage();
                String detail = null;
                try {
                    InputStream is = connection.getErrorStream();
                    if (is != null)
                        detail = Helper.readStream(is);
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                Log.w("Gemini error=" + error + " detail=" + detail);
                if (detail != null)
                    try {
                        JSONObject jroot = new JSONObject(detail);
                        JSONObject jerror = jroot.optJSONObject("error");
                        if (jerror != null) {
                            String msg = jerror.optString("message");
                            if (!TextUtils.isEmpty(msg))
                                detail = msg;
                        }
                    } catch (Throwable ignored) {
                    }
                throw new IOException(TextUtils.isEmpty(detail) ? error : detail);
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

    static class Message {
        private final String role; // model, user
        private final String[] content;

        public Message(String role, String[] content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return this.role;
        }

        public String[] getContent() {
            return this.content;
        }

        @NonNull
        @Override
        public String toString() {
            return this.role + ": " + (this.content == null ? null : TextUtils.join(", ", this.content));
        }
    }
}
