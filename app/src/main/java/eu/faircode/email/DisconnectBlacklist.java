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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class DisconnectBlacklist {
    private final static int FETCH_TIMEOUT = 20 * 1000; // milliseconds
    private final static String LIST = "https://raw.githubusercontent.com/mozilla-services/shavar-prod-lists/master/disconnect-blacklist.json";

    static void download(Context context) throws IOException, JSONException {
        File file = getFile(context);

        URL url = new URL(LIST);
        Log.i("GET " + url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(FETCH_TIMEOUT);
        connection.setConnectTimeout(FETCH_TIMEOUT);
        connection.connect();

        try {
            String response = Helper.readStream(connection.getInputStream(), StandardCharsets.UTF_8.name());
            Helper.writeText(file, response);
        } finally {
            connection.disconnect();
        }
    }

    static List<String> getCategories(String domain, Context context) throws IOException, JSONException {
        if (domain == null)
            return null;

        File file = getFile(context);
        if (!file.exists())
            return null;

        List<String> result = new ArrayList<>();

        String json = Helper.readText(file);
        JSONObject jdisconnect = new JSONObject(json);
        JSONObject jcategories = (JSONObject) jdisconnect.get("categories");
        Iterator<String> categories = jcategories.keys();
        while (categories.hasNext()) {
            String category = categories.next();
            JSONArray jcategory = jcategories.getJSONArray(category);
            for (int c = 0; c < jcategory.length(); c++) {
                JSONObject jblock = (JSONObject) jcategory.get(c);
                Iterator<String> names = jblock.keys();
                if (names.hasNext()) {
                    String name = names.next();
                    JSONObject jsites = (JSONObject) jblock.get(name);
                    Iterator<String> sites = jsites.keys();
                    if (sites.hasNext()) {
                        List<String> domains = new ArrayList<>();

                        String site = sites.next();
                        String host = Uri.parse(site).getHost();
                        if (host != null)
                            domains.add(host);

                        JSONArray jdomains = jsites.getJSONArray(site);
                        for (int d = 0; d < jdomains.length(); d++)
                            domains.add(jdomains.getString(d));

                        for (String d : domains)
                            if (domain.equalsIgnoreCase(d) && !result.contains(category))
                                result.add(category);
                    }
                }
            }
        }

        return (result.size() == 0 ? null : result);
    }

    private static File getFile(Context context) {
        return new File(context.getFilesDir(), "disconnect-blacklist.json");
    }

    static Long getTime(Context context) {
        File file = getFile(context);
        return (file.exists() ? file.lastModified() : null);
    }
}
