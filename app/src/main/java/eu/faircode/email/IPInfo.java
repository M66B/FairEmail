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
    private static Map<InetAddress, String> hostOrganization = new HashMap<>();

    private final static int FETCH_TIMEOUT = 15 * 1000; // milliseconds

    static String[] getOrganization(Uri uri, Context context) throws IOException, ParseException {
        if ("mailto".equals(uri.getScheme())) {
            MailTo email = MailTo.parse(uri.toString());
            String to = email.getTo();
            if (to == null || !to.contains("@"))
                throw new UnknownHostException();
            String domain = to.substring(to.indexOf('@') + 1);
            InetAddress address = ConnectionHelper.lookupMx(domain, context);
            if (address == null)
                throw new UnknownHostException();
            return new String[]{domain, getOrganization(address)};
        } else {
            String host = uri.getHost();
            if (host == null)
                throw new UnknownHostException();
            InetAddress address = InetAddress.getByName(host);
            return new String[]{host, getOrganization(address)};
        }
    }

    private static String getOrganization(InetAddress address) throws IOException {
        synchronized (hostOrganization) {
            if (hostOrganization.containsKey(address))
                return hostOrganization.get(address);
        }
        URL url = new URL("https://ipinfo.io/" + address.getHostAddress() + "/org");
        Log.i("GET " + url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(FETCH_TIMEOUT);
        connection.connect();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String organization = reader.readLine();
            if ("undefined".equals(organization))
                organization = null;
            synchronized (hostOrganization) {
                hostOrganization.put(address, organization);
            }
            return organization;
        }
    }
}
