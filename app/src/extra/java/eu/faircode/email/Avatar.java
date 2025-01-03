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
import android.graphics.Bitmap;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

public class Avatar {
    static final String GRAVATAR_PRIVACY_URI = "https://automattic.com/privacy/";
    static final String LIBRAVATAR_PRIVACY_URI = "https://www.libravatar.org/privacy/";
    static final String DDG_URI = "https://icons.duckduckgo.com/ip3/";
    static final String DDG_PRIVACY_URI = "https://duckduckgo.com/privacy";

    private static final String GRAVATAR_URI = "https://www.gravatar.com/avatar/";
    private static final int GRAVATAR_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
    private static final int GRAVATAR_READ_TIMEOUT = 10 * 1000; // milliseconds
    private static final int LIBRAVATAR_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
    private static final int LIBRAVATAR_READ_TIMEOUT = 10 * 1000; // milliseconds
    private static final String LIBRAVATAR_DNS = "_avatars-sec._tcp,_avatars._tcp";
    private static final String LIBRAVATAR_URI = "https://seccdn.libravatar.org/avatar/";

    static Callable<ContactInfo.Favicon> getGravatar(String email, int scaleToPixels, Context context) {
        return new Callable<ContactInfo.Favicon>() {
            @Override
            public ContactInfo.Favicon call() throws Exception {
                String hash = Helper.md5(email.getBytes());
                URL url = new URL(GRAVATAR_URI + hash + "?d=404");
                Log.i("Gravatar key=" + email + " url=" + url);

                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(GRAVATAR_READ_TIMEOUT);
                urlConnection.setConnectTimeout(GRAVATAR_CONNECT_TIMEOUT);
                ConnectionHelper.setUserAgent(context, urlConnection);
                urlConnection.connect();

                try {
                    int status = urlConnection.getResponseCode();
                    if (status == HttpsURLConnection.HTTP_OK) {
                        // Positive reply
                        Bitmap bitmap = ImageHelper.getScaledBitmap(urlConnection.getInputStream(), url.toString(), null, scaleToPixels);
                        return (bitmap == null ? null : new ContactInfo.Favicon(bitmap, "gravatar", false));
                    } else if (status == HttpsURLConnection.HTTP_NOT_FOUND) {
                        // Negative reply
                        return null;
                    } else
                        throw new IOException("Error " + status + ": " + urlConnection.getResponseMessage());
                } finally {
                    urlConnection.disconnect();
                }
            }
        };
    }

    static Callable<ContactInfo.Favicon> getLibravatar(String email, int scaleToPixels, Context context) {
        return new Callable<ContactInfo.Favicon>() {
            @Override
            public ContactInfo.Favicon call() throws Exception {
                String domain = UriHelper.getEmailDomain(email);

                // https://wiki.libravatar.org/api/
                String baseUrl = LIBRAVATAR_URI;
                for (String dns : LIBRAVATAR_DNS.split(",")) {
                    DnsHelper.DnsRecord[] records = DnsHelper.lookup(context, dns + "." + domain, "srv");
                    if (records.length > 0) {
                        baseUrl = (records[0].port == 443 ? "https" : "http") + "://" + records[0].response + "/avatar/";
                        break;
                    }
                }

                String hash = Helper.md5(email.getBytes());

                URL url = new URL(baseUrl + hash + "?d=404");
                Log.i("Libravatar key=" + email + " url=" + url);

                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(LIBRAVATAR_READ_TIMEOUT);
                urlConnection.setConnectTimeout(LIBRAVATAR_CONNECT_TIMEOUT);
                ConnectionHelper.setUserAgent(context, urlConnection);
                urlConnection.connect();

                try {
                    int status = urlConnection.getResponseCode();
                    if (status == HttpsURLConnection.HTTP_OK) {
                        // Positive reply
                        Bitmap bitmap = ImageHelper.getScaledBitmap(urlConnection.getInputStream(), url.toString(), null, scaleToPixels);
                        return (bitmap == null ? null : new ContactInfo.Favicon(bitmap, "libravatar", false));
                    } else if (status == HttpsURLConnection.HTTP_NOT_FOUND) {
                        // Negative reply
                        return null;
                    } else
                        throw new IOException("Error " + status + ": " + urlConnection.getResponseMessage());
                } finally {
                    urlConnection.disconnect();
                }
            }
        };
    }
}