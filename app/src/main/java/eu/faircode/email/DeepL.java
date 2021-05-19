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

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class DeepL {
    private static final int DEEPL_TIMEOUT = 20; // seconds
    private static final String DEEPL_BASE_URI = "https://api-free.deepl.com/v2/";

    public static String translate(String text, String target, Context context) throws IOException, JSONException {
        String request =
                "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8.name()) +
                        "&target_lang=" + URLEncoder.encode(target, StandardCharsets.UTF_8.name());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String deepl = prefs.getString("deepl", null);

        URL url = new URL(DEEPL_BASE_URI + "translate?auth_key=" + deepl);
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
        String deepl = prefs.getString("deepl", null);

        URL url = new URL(DEEPL_BASE_URI + "usage?auth_key=" + deepl);
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
}
