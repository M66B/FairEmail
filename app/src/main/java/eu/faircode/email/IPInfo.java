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
import android.net.ParseException;
import android.net.Uri;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.IDN;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class IPInfo {
    public String org;
    public String city;
    public String region;
    public String country;

    private static final Map<InetAddress, IPInfo> addressOrganization = new HashMap<>();

    private final static int FETCH_TIMEOUT = 15 * 1000; // milliseconds

    static Pair<InetAddress, IPInfo> getOrganization(@NonNull Uri uri, Context context) throws IOException, ParseException, JSONException {
        String host = UriHelper.getHost(uri);
        if (host == null)
            throw new UnknownHostException();

        try {
            host = IDN.toASCII(host, IDN.ALLOW_UNASSIGNED);
        } catch (Throwable ex) {
            Log.i(ex);
        }

        InetAddress address = DnsHelper.getByName(context, host);
        return new Pair<>(address, getOrganization(address, context));
    }

    static IPInfo getOrganization(InetAddress address, Context context) throws IOException, JSONException {
        synchronized (addressOrganization) {
            if (addressOrganization.containsKey(address))
                return addressOrganization.get(address);
        }

        // https://ipinfo.io/developers
        // Possible alternative: https://www.ip2location.io/ip2location-documentation

        //{
        //  "ip": "8.8.8.8",
        //  "hostname": "dns.google",
        //  "anycast": true,
        //  "city": "Mountain View",
        //  "region": "California",
        //  "country": "US",
        //  "loc": "37.4056,-122.0775",
        //  "org": "AS15169 Google LLC",
        //  "postal": "94043",
        //  "timezone": "America/Los_Angeles",
        //  "readme": "https://ipinfo.io/missingauth"
        //}

        URL url = new URL("https://ipinfo.io/" + address.getHostAddress() + "/json");
        Log.i("GET " + url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(FETCH_TIMEOUT);
        connection.setConnectTimeout(FETCH_TIMEOUT);
        ConnectionHelper.setUserAgent(context, connection);
        connection.connect();

        IPInfo info = new IPInfo();
        try {
            int status = connection.getResponseCode();
            if (status != HttpsURLConnection.HTTP_OK)
                throw new FileNotFoundException("Error " + status + ": " + connection.getResponseMessage());

            String response = Helper.readStream(connection.getInputStream());
            JSONObject jroot = new JSONObject(response);
            info.org = jroot.optString("org");
            info.city = jroot.optString("city");
            info.region = jroot.optString("region");
            info.country = jroot.optString("country");
        } finally {
            connection.disconnect();
        }

        synchronized (addressOrganization) {
            addressOrganization.put(address, info);
        }

        return info;
    }
}
