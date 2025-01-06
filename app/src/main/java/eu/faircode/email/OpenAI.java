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
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Spannable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OpenAI {
    static final String DEFAULT_MODEL = "gpt-4o";
    static final float DEFAULT_TEMPERATURE = 0.5f;
    static final String DEFAULT_SUMMARY_PROMPT = "Summarize the following text:";
    static final String DEFAULT_ANSWER_PROMPT = "Answer this message:";

    static final String SYSTEM = "system";
    static final String ASSISTANT = "assistant";
    static final String USER = "user";

    // https://cookbook.openai.com/examples/gpt4o/introduction_to_gpt4o
    static final String CONTENT_TEXT = "text";
    static final String CONTENT_IMAGE = "image_url";

    private static final int TIMEOUT = 45; // seconds
    private static final int SCALE2PIXELS = 1440; // medium

    static boolean isAvailable(Context context) {
        if (TextUtils.isEmpty(BuildConfig.OPENAI_ENDPOINT))
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("openai_enabled", false);
        String apikey = prefs.getString("openai_apikey", null);

        return (enabled &&
                (!TextUtils.isEmpty(apikey) || !Objects.equals(getUri(context), BuildConfig.OPENAI_ENDPOINT)));
    }

    static void checkModeration(Context context, String text) throws JSONException, IOException {
        // https://platform.openai.com/docs/api-reference/moderations/create
        JSONObject jrequest = new JSONObject();
        jrequest.put("input", text);
        JSONObject jresponse = call(context, "POST", "moderations", jrequest);
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
        JSONObject jresponse = call(context, "POST", "embeddings", jrequest);
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

            if (message.content.length == 1 && CONTENT_TEXT.equals(message.content[0].type))
                jmessage.put("content", message.content[0].content);
            else {
                JSONArray jcontents = new JSONArray();
                for (Content content : message.content) {
                    JSONObject jcontent = new JSONObject();
                    jcontent.put("type", content.type);
                    if (CONTENT_IMAGE.equals(content.type)) {
                        JSONObject jimage = new JSONObject();
                        jimage.put("url", content.content);
                        jcontent.put(content.type, jimage);
                    } else
                        jcontent.put(content.type, content.content);
                    jcontents.put(jcontent);
                }
                jmessage.put("content", jcontents);
            }

            jmessages.put(jmessage);
        }

        JSONObject jquestion = new JSONObject();
        jquestion.put("model", model);
        jquestion.put("messages", jmessages);
        if (temperature != null)
            jquestion.put("temperature", temperature);
        jquestion.put("n", n);
        JSONObject jresponse = call(context, "POST", "chat/completions", jquestion);

        JSONArray jchoices = jresponse.getJSONArray("choices");
        Message[] choices = new Message[jchoices.length()];
        for (int i = 0; i < jchoices.length(); i++) {
            JSONObject jchoice = jchoices.getJSONObject(i);
            JSONObject jmessage = jchoice.getJSONObject("message");
            choices[i] = new Message(jmessage.getString("role"),
                    new Content[]{new Content(CONTENT_TEXT, jmessage.getString("content"))});
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
                String error = "Error " + status + ": " + connection.getResponseMessage();
                String detail = null;
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
                    if (is != null)
                        detail = Helper.readStream(is);
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                Log.w("OpenAI error=" + error + " detail=" + detail);
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
            Log.i("OpenAI response=" + response);

            try {
                // https://platform.openai.com/docs/guides/rate-limits/rate-limits-in-headers
                for (Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                    String key = entries.getKey();
                    if (key != null && key.startsWith("x-ratelimit"))
                        for (String value : entries.getValue())
                            Log.i("OpenAI", key + "=" + value);
                }
            } catch (Throwable ex) {
                Log.w(ex);
            }

            return new JSONObject(response);
        } finally {
            connection.disconnect();
            long elapsed = new Date().getTime() - start;
            Log.i("OpenAI elapsed=" + (elapsed / 1000f));
        }
    }

    static class Content {
        private String type;
        private String content;

        public Content(String type, String content) {
            this.type = type;
            this.content = content;
        }

        public String getType() {
            return this.type;
        }

        public String getContent() {
            return this.content;
        }

        static Content[] get(Spannable ssb, long id, Context context) {
            DB db = DB.getInstance(context);
            List<OpenAI.Content> contents = new ArrayList<>();
            int start = 0;
            while (start < ssb.length()) {
                int end = ssb.nextSpanTransition(start, ssb.length(), ImageSpanEx.class);

                String text = ssb.subSequence(start, end).toString().trim()
                        .replace("\u00a0", "")
                        .replace("\ufffc", "");
                Log.i("OpenAI content " + start + "..." + end +
                        " text=[" + Helper.getPrintableString(text, true) + "]");

                if (!TextUtils.isEmpty(text))
                    contents.add(new OpenAI.Content(OpenAI.CONTENT_TEXT, text));

                if (end < ssb.length()) {
                    ImageSpanEx[] spans = ssb.getSpans(end, end, ImageSpanEx.class);
                    Log.i("OpenAI images=" + (spans == null ? null : spans.length));
                    if (spans != null && spans.length == 1) {
                        int e = ssb.getSpanEnd(spans[0]);

                        String url = null;
                        String src = spans[0].getSource();
                        Log.i("OpenAI image url=" + src);
                        if (src != null && src.startsWith("cid:")) {
                            String cid = '<' + src.substring(4) + '>';
                            EntityAttachment attachment = db.attachment().getAttachment(id, cid);
                            if (attachment != null && attachment.available) {
                                File file = attachment.getFile(context);
                                try (InputStream is = new FileInputStream(file)) {
                                    Bitmap bm = ImageHelper.getScaledBitmap(is, null, null, SCALE2PIXELS);
                                    Helper.ByteArrayInOutStream bos = new Helper.ByteArrayInOutStream();
                                    bm.compress(Bitmap.CompressFormat.PNG, ImageHelper.DEFAULT_PNG_COMPRESSION, bos);
                                    url = ImageHelper.getDataUri(bos.getInputStream(), "image/png");
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }
                            }
                        } else
                            url = src;
                        if (url != null)
                            contents.add(new OpenAI.Content(OpenAI.CONTENT_IMAGE, url));

                        end = e;
                    }
                }

                start = (end > start ? end : start + 1);
            }

            return contents.toArray(new OpenAI.Content[0]);
        }
    }

    static class Message {
        private final String role; // system, user, assistant
        private final Content[] content;

        public Message(String role, Content[] content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return this.role;
        }

        public Content[] getContent() {
            return this.content;
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (this.content != null)
                for (Content c : this.content) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(c.type).append(':').append(c.content);
                }
            return this.role + ": " + sb;
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
