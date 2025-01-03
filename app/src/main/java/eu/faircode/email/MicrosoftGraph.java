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
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Base64OutputStream;

import androidx.preference.PreferenceManager;

import net.openid.appauth.AuthState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.HttpsURLConnection;

public class MicrosoftGraph {
    static final int GRAPH_TIMEOUT = 20; // seconds
    static final String GRAPH_ENDPOINT = "https://graph.microsoft.com/v1.0/me/";

    static void send(Context context, EntityIdentity ident, MimeMessage imessage) throws IOException, JSONException, MessagingException {
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            db.identity().setIdentityState(ident.id, "connecting");

            AuthState authState = AuthState.jsonDeserialize(ident.password);
            ServiceAuthenticator.OAuthRefresh(context, ident.provider, ident.auth_type, ident.user, authState, true);
            Long expiration = authState.getAccessTokenExpirationTime();
            if (expiration != null)
                EntityLog.log(context, ident.user + " token expiration=" + new Date(expiration));

            String newPassword = authState.jsonSerializeString();
            if (!Objects.equals(ident.password, newPassword))
                db.identity().setIdentityPassword(ident.id, newPassword);

            // https://learn.microsoft.com/en-us/graph/api/user-sendmail?view=graph-rest-1.0
            URL url = new URL(MicrosoftGraph.GRAPH_ENDPOINT + "sendMail");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
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
                if (status == HttpsURLConnection.HTTP_ACCEPTED) {
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

    static int downloadContacts(Context context, long account, AuthState authState) throws IOException, JSONException, MessagingException {
        int count = 0;
        DB db = DB.getInstance(context);

        ServiceAuthenticator.OAuthRefresh(context,
                "outlookgraph", ServiceAuthenticator.AUTH_TYPE_GRAPH, "contacts", authState, BuildConfig.DEBUG);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("graph.contacts." + account, authState.jsonSerializeString()).apply();

        // https://learn.microsoft.com/en-us/graph/api/user-list-contacts?view=graph-rest-1.0&tabs=http
        URL url = new URL(MicrosoftGraph.GRAPH_ENDPOINT + "contacts");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(GRAPH_TIMEOUT * 1000);
        connection.setConnectTimeout(GRAPH_TIMEOUT * 1000);
        ConnectionHelper.setUserAgent(context, connection);
        connection.setRequestProperty("Authorization", "Bearer " + authState.getAccessToken());
        connection.connect();

        try {
            int status = connection.getResponseCode();
            if (status == HttpsURLConnection.HTTP_OK) {
                String response = Helper.readStream(connection.getInputStream());
                JSONObject jroot = new JSONObject(response);
                JSONArray jvalue = jroot.getJSONArray("value");
                for (int i = 0; i < jvalue.length(); i++) {
                    JSONObject jcontact = jvalue.getJSONObject(i);
                    String displayName = jcontact.optString("displayName");
                    if (TextUtils.isEmpty(displayName))
                        displayName = null;
                    if (jcontact.has("emailAddresses")) {
                        JSONArray jemailAddresses = jcontact.getJSONArray("emailAddresses");
                        for (int j = 0; j < jemailAddresses.length(); j++) {
                            JSONObject jemail = jemailAddresses.getJSONObject(j);
                            String email = jemail.optString("address");
                            if (!TextUtils.isEmpty(email)) {
                                EntityContact contact = db.contact().getContact(account, EntityContact.TYPE_TO, email);
                                EntityLog.log(context, "Graph/contacts " + displayName + " <" + email + ">" +
                                        " account=" + account + " exists=" + (contact != null));
                                if (contact == null) {
                                    contact = new EntityContact();
                                    contact.account = account;
                                    contact.type = EntityContact.TYPE_TO;
                                    contact.email = email;
                                    contact.name = displayName;
                                    contact.times_contacted = 0;
                                    contact.first_contacted = new Date().getTime();
                                    contact.last_contacted = contact.first_contacted;
                                    db.contact().insertContact(contact);
                                    count++;
                                }
                            }
                        }
                    }
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

        return count;
    }
}
