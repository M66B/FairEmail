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

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenAI {
    static final String URI_ENDPOINT = "https://api.openai.com/";
    static final String URI_PRIVACY = "https://openai.com/policies/privacy-policy";

    private static final int TIMEOUT = 30; // seconds

    static boolean isAvailable(Context context) {
        if (BuildConfig.PLAY_STORE_RELEASE)
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("openai_enabled", false);
        String apikey = prefs.getString("openai_apikey", null);

        return (enabled && !TextUtils.isEmpty(apikey));
    }

    static Message[] completeChat(Context context, Message[] messages, int n) throws JSONException, IOException {
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
        jquestion.put("model", "gpt-3.5-turbo");
        jquestion.put("messages", jmessages);
        jquestion.put("n", n);
        JSONObject jresponse = call(context, "v1/chat/completions", jquestion);

        JSONArray jchoices = jresponse.getJSONArray("choices");
        Message[] choices = new Message[jchoices.length()];
        for (int i = 0; i < jchoices.length(); i++) {
            JSONObject jchoice = jchoices.getJSONObject(i);
            JSONObject jmessage = jchoice.getJSONObject("message");
            choices[i] = new Message(jmessage.getString("role"), jmessage.getString("content"));
        }

        return choices;
    }

    private static JSONObject call(Context context, String method, JSONObject args) throws JSONException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String apikey = prefs.getString("openai_apikey", null);

        // https://platform.openai.com/docs/api-reference/introduction
        Uri uri = Uri.parse(URI_ENDPOINT).buildUpon().appendEncodedPath(method).build();
        Log.i("OpenAI uri=" + uri);

        String json = args.toString();
        Log.i("OpenAI request=" + json);

        URL url = new URL(uri.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setReadTimeout(TIMEOUT * 1000);
        connection.setConnectTimeout(TIMEOUT * 1000);
        ConnectionHelper.setUserAgent(context, connection);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apikey);
        connection.connect();

        try {
            connection.getOutputStream().write(json.getBytes());

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                // https://platform.openai.com/docs/guides/error-codes/api-errors
                String error = "Error " + status + ": " + connection.getResponseMessage();
                try {
                    InputStream is = connection.getErrorStream();
                    if (is != null)
                        error += "\n" + Helper.readStream(is);
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
        }
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
}
