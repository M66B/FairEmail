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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.net.MailTo;
import android.net.ParseException;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class IPInfo {
    private static Map<String, String> hostOrganization = new HashMap<>();

    static String getOrganization(Uri uri) throws IOException, ParseException {
        if ("mailto".equals(uri.getScheme())) {
            MailTo email = MailTo.parse(uri.toString());
            String to = email.getTo();
            if (to == null || !to.contains("@"))
                throw new UnknownHostException();
            String host = to.substring(to.indexOf('@') + 1);
            return getOrganization(host);
        } else {
            String host = uri.getHost();
            if (host == null)
                throw new UnknownHostException();
            return getOrganization(host);
        }
    }

    private static String getOrganization(String host) throws IOException {
        synchronized (hostOrganization) {
            if (hostOrganization.containsKey(host))
                return hostOrganization.get(host);
        }
        InetAddress address = InetAddress.getByName(host);
        URL url = new URL("https://ipinfo.io/" + address.getHostAddress() + "/org");
        Log.i("GET " + url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(15 * 1000);
        connection.connect();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String organization = reader.readLine();
            if ("undefined".equals(organization))
                organization = null;
            synchronized (hostOrganization) {
                hostOrganization.put(host, organization);
            }
            return organization;
        }
    }
}
