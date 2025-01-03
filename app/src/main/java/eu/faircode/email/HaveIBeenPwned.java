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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

public class HaveIBeenPwned {
    // https://haveibeenpwned.com/API/v3

    private final static int FETCH_TIMEOUT = 15 * 1000; // milliseconds

    static Integer check(String password, Context context) throws NoSuchAlgorithmException, IOException {
        String hashed = Helper.sha1(password.getBytes());
        String range = hashed.substring(0, 5);
        String rest = hashed.substring(5);

        URL url = new URL(BuildConfig.PWNED_ENDPOINT + "range/" + range);
        Log.i("GET " + url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Add-Padding", "true");
        connection.setReadTimeout(FETCH_TIMEOUT);
        connection.setConnectTimeout(FETCH_TIMEOUT);
        ConnectionHelper.setUserAgent(context, connection);
        connection.connect();

        try {
            int status = connection.getResponseCode();
            if (status != HttpsURLConnection.HTTP_OK)
                throw new IOException("Error " + status + ": " + connection.getResponseMessage());

            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && rest.equalsIgnoreCase(parts[0]))
                    return Helper.parseInt(parts[1]);
            }
        } finally {
            connection.disconnect();
        }

        return null;
    }
}
