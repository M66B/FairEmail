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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class OpenAI {
    private static final int MAX_OPENAI_LEN = 1000; // characters
    private static final int TIMEOUT = 30; // seconds

    static boolean isAvailable(Context context) {
        if (TextUtils.isEmpty(BuildConfig.OPENAI_ENDPOINT))
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("openai_enabled", false);
        String apikey = prefs.getString("openai_apikey", null);

        return (enabled &&
                (!TextUtils.isEmpty(apikey) || !Objects.equals(getUri(context), BuildConfig.OPENAI_ENDPOINT)));
    }

    static Pair<Double, Double> getGrants(Context context) throws JSONException, IOException {
        // dashboard/billing/credit_grants
        // {
        //  "object": "credit_summary",
        //  "total_granted": <float>,
        //  "total_used": <float>,
        //  "total_available": <float>,
        //  "grants": {
        //    "object": "list",
        //    "data": [
        //      {
        //        "object": "credit_grant",
        //        "id": "<guid>>",
        //        "grant_amount": <float>,
        //        "used_amount": <float>>,
        //        "effective_at": <unixtime>,
        //        "expires_at": <unixtime>
        //      }
        //    ]
        //  }
        //}

        JSONObject grants = call(context, "GET", "dashboard/billing/credit_grants", null);
        return new Pair<>(
                grants.getDouble("total_used"),
                grants.getDouble("total_granted"));
    }

    static void checkModeration(Context context, String text) throws JSONException, IOException {
        // https://platform.openai.com/docs/api-reference/moderations/create
        JSONObject jrequest = new JSONObject();
        jrequest.put("input", text);
        JSONObject jresponse = call(context, "POST", "v1/moderations", jrequest);
        JSONArray jresults = jresponse.getJSONArray("results");
        for (int i = 0; i < jresults.length(); i++) {
            JSONObject jresult = jresults.getJSONObject(i);
            if (jresult.getBoolean("flagged")) {
                List<String> violations = new ArrayList<>();
                JSONObject jcategories = jresult.getJSONObject("categories");
                JSONObject jcategory_scores = jresult.getJSONObject("category_scores");
                Iterator<String> keys = jcategories.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jcategories.get(key);
                    if (Boolean.TRUE.equals(value)) {
                        Double score = (jcategories.has(key) ? jcategory_scores.getDouble(key) : null);
                        violations.add(key + (score == null ? "" : ":" + Math.round(score * 100) + "%"));
                    }
                }
                throw new IllegalArgumentException(TextUtils.join(", ", violations));
            }
        }
    }

    static double[] getEmbedding(Context context, String text, String model) throws JSONException, IOException {
        // https://platform.openai.com/docs/api-reference/embeddings
        JSONObject jrequest = new JSONObject();
        jrequest.put("input", text);
        jrequest.put("model", model == null ? "text-embedding-ada-002" : model);
        JSONObject jresponse = call(context, "POST", "v1/embeddings", jrequest);
        JSONObject jdata = jresponse.getJSONArray("data").getJSONObject(0);
        JSONArray jembedding = jdata.getJSONArray("embedding");
        double[] result = new double[jembedding.length()];
        for (int i = 0; i < jembedding.length(); i++)
            result[i] = jembedding.getDouble(i);
        return result;
    }

    static Message[] completeChat(Context context, String model, Message[] messages, Float temperature, int n) throws JSONException, IOException {
        // https://platform.openai.com/docs/guides/chat/introduction
        // https://platform.openai.com/docs/api-reference/chat/create
        JSONArray jmessages = new JSONArray();
        for (Message message : messages) {
            JSONObject jmessage = new JSONObject();
            jmessage.put("role", message.role);
            jmessage.put("content", message.content);
            jmessages.put(jmessage);
        }

        JSONObject jquestion = new JSONObject();
        jquestion.put("model", model);
        jquestion.put("messages", jmessages);
        if (temperature != null)
            jquestion.put("temperature", temperature);
        jquestion.put("n", n);
        JSONObject jresponse = call(context, "POST", "v1/chat/completions", jquestion);

        JSONArray jchoices = jresponse.getJSONArray("choices");
        Message[] choices = new Message[jchoices.length()];
        for (int i = 0; i < jchoices.length(); i++) {
            JSONObject jchoice = jchoices.getJSONObject(i);
            JSONObject jmessage = jchoice.getJSONObject("message");
            choices[i] = new Message(jmessage.getString("role"), jmessage.getString("content"));
        }

        return choices;
    }

    private static String getUri(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String endpoint = prefs.getString("openai_uri", BuildConfig.OPENAI_ENDPOINT);
        if (!endpoint.endsWith("/"))
            endpoint += "/";
        return endpoint;
    }

    private static JSONObject call(Context context, String method, String path, JSONObject args) throws JSONException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String apikey = prefs.getString("openai_apikey", null);

        // https://platform.openai.com/docs/api-reference/introduction
        Uri uri = Uri.parse(getUri(context)).buildUpon().appendEncodedPath(path).build();
        Log.i("OpenAI uri=" + uri);

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
        connection.setRequestProperty("Authorization", "Bearer " + apikey);
        connection.connect();

        try {
            if (args != null) {
                String json = args.toString();
                Log.i("OpenAI request=" + json);
                connection.getOutputStream().write(json.getBytes());
            }

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                // https://platform.openai.com/docs/guides/error-codes/api-errors
                String error = "Error " + status + ": " + connection.getResponseMessage();
                try {
                    // HTTP 429
                    // {
                    //    "error": {
                    //        "message": "You exceeded your current quota, please check your plan and billing details.",
                    //        "type": "insufficient_quota",
                    //        "param": null,
                    //        "code": null
                    //    }
                    //}
                    InputStream is = connection.getErrorStream();
                    if (is != null) {
                        String err = Helper.readStream(is);
                        if (BuildConfig.DEBUG)
                            error += "\n" + err;
                        else {
                            Log.w(new Throwable(err));
                            try {
                                JSONObject jerror = new JSONObject(err).getJSONObject("error");
                                error += "\n" + jerror.getString("type") + ": " + jerror.getString("message");
                            } catch (JSONException ignored) {
                                error += "\n" + err;
                            }
                        }
                    }
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                throw new IOException(error);
            }

            String response = Helper.readStream(connection.getInputStream());
            Log.i("OpenAI response=" + response);

            return new JSONObject(response);
        } finally {
            connection.disconnect();
            long elapsed = new Date().getTime() - start;
            Log.i("OpenAI elapsed=" + (elapsed / 1000f));
        }
    }

    static String truncateParagraphs(@NonNull String text) {
        return truncateParagraphs(text, MAX_OPENAI_LEN);
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
        private final String role; //  // system, user, assistant
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return this.role;
        }

        public String getContent() {
            return this.content;
        }

        @NonNull
        @Override
        public String toString() {
            return this.role + ": " + this.content;
        }
    }

    static class Embedding {
        public static double getSimilarity(double[] v1, double[] v2) {
            if (v1.length != v2.length)
                throw new IllegalArgumentException("Invalid vector length=" + v1.length + "/" + v2.length);
            double dotProduct = dotProduct(v1, v2);
            double magV1 = magnitude(v1);
            double magV2 = magnitude(v2);
            return dotProduct / (magV1 * magV2);
        }

        private static double dotProduct(double[] v1, double[] v2) {
            float val = 0;
            for (int i = 0; i <= v1.length - 1; i++)
                val += v1[i] * v2[i];
            return val;
        }

        private static double magnitude(double[] v) {
            return Math.sqrt(dotProduct(v, v));
        }
    }
}
