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
import android.util.Base64;
import android.util.Base64OutputStream;

import androidx.preference.PreferenceManager;

import net.openid.appauth.AuthState;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class MicrosoftGraph {
    static final int GRAPH_TIMEOUT = 20; // seconds
    static final String GRAPH_ENDPOINT = "https://graph.microsoft.com/v1.0/me/";

    static void send(Context context, EntityIdentity ident, MimeMessage imessage) throws IOException, JSONException, MessagingException {
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            db.identity().setIdentityState(ident.id, "connecting");

            AuthState authState = AuthState.jsonDeserialize(ident.password);
            ServiceAuthenticator.OAuthRefresh(context, ident.provider, ident.auth_type, ident.user, authState, false);
            Long expiration = authState.getAccessTokenExpirationTime();
            if (expiration != null)
                EntityLog.log(context, ident.user + " token expiration=" + new Date(expiration));

            String newPassword = authState.jsonSerializeString();
            if (!Objects.equals(ident.password, newPassword))
                db.identity().setIdentityPassword(ident.id, newPassword);

            // https://learn.microsoft.com/en-us/graph/api/user-sendmail?view=graph-rest-1.0
            URL url = new URL(MicrosoftGraph.GRAPH_ENDPOINT + "sendMail");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setReadTimeout(MicrosoftGraph.GRAPH_TIMEOUT * 1000);
            connection.setConnectTimeout(MicrosoftGraph.GRAPH_TIMEOUT * 1000);
            ConnectionHelper.setUserAgent(context, connection);
            connection.setRequestProperty("Authorization", "Bearer " + authState.getAccessToken());
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.connect();

            try {
                db.identity().setIdentityState(ident.id, "connected");

                EntityLog.log(context, "Sending via Graph user=" + ident.user);

                long start = new Date().getTime();
                try (OutputStream out = new Base64OutputStream(connection.getOutputStream(), Base64.DEFAULT | Base64.NO_CLOSE)) {
                    imessage.writeTo(out);
                }
                long end = new Date().getTime();

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_ACCEPTED) {
                    EntityLog.log(context, "Sent via Graph " + ident.user + " elapse=" + (end - start) + " ms");
                    boolean log = prefs.getBoolean("protocol", false);
                    if (log || BuildConfig.DEBUG)
                        try {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            imessage.writeTo(bos);
                            for (String line : bos.toString().split("\\r?\\n"))
                                if (log)
                                    EntityLog.log(context, line);
                                else
                                    Log.i("graph", ident.user + " " + line);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                } else {
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
            } finally {
                connection.disconnect();
            }
        } finally {
            db.identity().setIdentityState(ident.id, null);
        }
    }
}
