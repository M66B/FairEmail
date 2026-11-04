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

    Copyright 2018-2026 by Marcel Bokhorst (M66B)
*/

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_GMAIL;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_OAUTH;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.net.ssl.HttpsURLConnection;

public class GmailSendAs {
    // https://developers.google.com/workspace/gmail/api/reference/rest/v1/users.settings.sendAs/list

    static final int SENDAS_TIMEOUT = 20; // seconds
    static final String SENDAS_ENDPOINT = "https://gmail.googleapis.com/gmail/v1/users/me/settings/sendAs";

    static boolean isGmail(EntityIdentity identity) {
        return (identity.auth_type == AUTH_TYPE_GMAIL ||
                (identity.auth_type == AUTH_TYPE_OAUTH && "gmail".equals(identity.provider)));
    }

    static int importAliases(Context context, long id)
            throws AuthenticatorException, OperationCanceledException, IOException, JSONException, MessagingException {
        DB db = DB.getInstance(context);

        EntityIdentity main = db.identity().getIdentity(id);
        if (main == null || !isGmail(main))
            return 0;

        JSONArray jsendas = getSendAs(context, main);

        Set<String> emails = new HashSet<>();
        for (EntityIdentity identity : db.identity().getIdentities(main.account))
            emails.add(identity.email.toLowerCase(Locale.ROOT));

        int count = 0;
        try {
            db.beginTransaction();

            for (int i = 0; i < jsendas.length(); i++) {
                JSONObject jalias = jsendas.getJSONObject(i);
                String email = jalias.optString("sendAsEmail");
                boolean primary = jalias.optBoolean("isPrimary");
                String status = jalias.optString("verificationStatus");
                boolean exists = emails.contains(email.toLowerCase(Locale.ROOT));
                EntityLog.log(context, EntityLog.Type.Account, main.account, null, null,
                        "Gmail/sendAs " + email + " primary=" + primary + " status=" + status + " exists=" + exists);

                if (TextUtils.isEmpty(email) || primary || exists || !"accepted".equals(status))
                    continue;

                String name = jalias.optString("displayName");
                String replyto = jalias.optString("replyToAddress");

                EntityIdentity identity = db.identity().getIdentity(id);
                identity.id = null;
                identity.uuid = UUID.randomUUID().toString();
                identity.name = (TextUtils.isEmpty(name) ? main.name : name);
                identity.email = email;
                identity.display = null;
                identity.replyto = (TextUtils.isEmpty(replyto) ? null : replyto);
                identity.primary = false;
                identity.sign_key = null;
                identity.sign_key_alias = null;
                identity.error = null;
                identity.last_connected = null;
                identity.id = db.identity().insertIdentity(identity);

                emails.add(email.toLowerCase(Locale.ROOT));
                count++;
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (count > 0) {
            Core.clearIdentities();
            FairEmailBackupAgent.dataChanged(context);
        }

        return count;
    }

    private static JSONArray getSendAs(Context context, EntityIdentity identity)
            throws AuthenticatorException, OperationCanceledException, IOException, JSONException, MessagingException {
        ServiceAuthenticator authenticator = new ServiceAuthenticator(context,
                identity.auth_type, identity.provider,
                identity.user, identity.password,
                new ServiceAuthenticator.IAuthenticated() {
                    @Override
                    public void onPasswordChanged(Context context, String newPassword) {
                        DB db = DB.getInstance(context);
                        identity.password = newPassword;
                        int identities = db.identity().setIdentityPassword(identity.account, identity.user, identity.password, identity.auth_type, identity.auth_type, identity.provider);
                        EntityLog.log(context, EntityLog.Type.Account, identity.account, null, null,
                                identity.email + "/" + identity.user + " token refreshed=" + identities);
                    }
                });

        for (int attempt = 0; ; attempt++) {
            String token = authenticator.refreshToken(attempt > 0);

            URL url = new URL(SENDAS_ENDPOINT);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(SENDAS_TIMEOUT * 1000);
            connection.setConnectTimeout(SENDAS_TIMEOUT * 1000);
            ConnectionHelper.setUserAgent(context, connection);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.connect();

            try {
                int status = connection.getResponseCode();
                if (status == HttpsURLConnection.HTTP_OK) {
                    String response = Helper.readStream(connection.getInputStream());
                    JSONObject jroot = new JSONObject(response);
                    JSONArray jsendas = jroot.optJSONArray("sendAs");
                    return (jsendas == null ? new JSONArray() : jsendas);
                }

                // Expired or revoked token: retry once with a forced refresh
                if (status == HttpsURLConnection.HTTP_UNAUTHORIZED && attempt == 0)
                    continue;

                String error = "Error " + status + ": " + connection.getResponseMessage();
                try {
                    InputStream is = connection.getErrorStream();
                    if (is != null)
                        error += "\n" + Helper.readStream(is);
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                throw new IOException(error);
            } finally {
                connection.disconnect();
            }
        }
    }
}
