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
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.HttpsURLConnection;

public class DisconnectBlacklist {
    private static final Map<String, List<String>> map = new HashMap<>();
    private static final ExecutorService executor = Helper.getBackgroundExecutor(1, "disconnect");

    private final static int FETCH_TIMEOUT = 20 * 1000; // milliseconds
    private final static String LIST = "https://raw.githubusercontent.com/disconnectme/disconnect-tracking-protection/master/services.json";

    static void init(Context context) {
        final File file = getFile(context);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (file.exists())
                        init(file);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private static void init(File file) throws IOException, JSONException {
        synchronized (map) {
            long start = SystemClock.elapsedRealtime();

            map.clear();

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
                            String site = sites.next();
                            JSONArray jdomains = jsites.getJSONArray(site);
                            for (int d = 0; d < jdomains.length(); d++) {
                                String domain = jdomains.getString(d).toLowerCase(Locale.ROOT);
                                if (!map.containsKey(domain))
                                    map.put(domain, new ArrayList<>());
                                List<String> list = map.get(domain);
                                if (!list.contains(category))
                                    list.add(category);
                            }
                        }
                    }
                }
            }

            long elapsed = SystemClock.elapsedRealtime() - start;
            Log.i("Disconnect domains=" + map.size() + " elapsed=" + elapsed + " ms");
        }
    }

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

        init(file);
    }

    static List<String> getCategories(String domain) {
        return _getCategories(domain);
    }

    static boolean isTracking(String host) {
        List<String> categories = _getCategories(host);
        if (categories == null || categories.size() == 0)
            return false;
        return !categories.contains("Content");
    }

    private static List<String> _getCategories(String domain) {
        if (domain == null)
            return null;

        synchronized (map) {
            String d = domain.toLowerCase(Locale.ROOT);
            while (d.contains(".")) {
                List<String> result = map.get(d);
                if (result != null)
                    return result;
                int dot = d.indexOf(".");
                d = d.substring(dot + 1);
            }
        }

        return null;
    }

    private static File getFile(Context context) {
        return new File(context.getFilesDir(), "disconnect-blacklist.json");
    }

    static Long getTime(Context context) {
        File file = getFile(context);
        return (file.exists() ? file.lastModified() : null);
    }
}
