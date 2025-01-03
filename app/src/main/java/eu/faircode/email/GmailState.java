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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GmailState {
    private String token;
    private long acquired;

    static final String TYPE_GOOGLE = "com.google";
    private static final long TOKEN_LIFETIME = 60 * 60 * 1000L; // milliseconds

    private GmailState(String token, long acquired) {
        this.token = token;
        this.acquired = acquired;
    }

    @NonNull
    String getAccessToken() throws AuthenticatorException {
        if (token == null)
            throw new AuthenticatorException("no token");
        return token;
    }

    Long getAccessTokenExpirationTime() {
        if (token == null || acquired == 0)
            return null;
        else
            return acquired + TOKEN_LIFETIME;
    }

    void refresh(@NonNull Context context, String id, @NonNull String user, boolean forceRefresh)
            throws AuthenticatorException, OperationCanceledException, IOException {
        long now = new Date().getTime();
        Long expiration = getAccessTokenExpirationTime();
        boolean needsRefresh = (expiration != null && expiration < now);
        boolean neededRefresh = needsRefresh;

        if (!needsRefresh && forceRefresh &&
                expiration != null &&
                expiration - ServiceAuthenticator.MIN_FORCE_REFRESH_INTERVAL < now)
            needsRefresh = true;

        Map<String, String> crumb = new HashMap<>();
        crumb.put("id", id);
        crumb.put("force", Boolean.toString(forceRefresh));
        crumb.put("need", Boolean.toString(needsRefresh));
        crumb.put("needed", Boolean.toString(neededRefresh));
        crumb.put("token", Boolean.toString(token != null));
        crumb.put("expiration", expiration == null ? "n/a" : ((expiration - now) / 1000L) + " s");
        Log.breadcrumb("Token refresh", crumb);

        EntityLog.log(context, EntityLog.Type.General, "Token refresh user=" + id + ":" + user +
                " force=" + forceRefresh +
                " need=" + needsRefresh +
                " needed=" + neededRefresh +
                " expiration=" + (expiration == null ? null : new Date(expiration)));
        try {
            if (needsRefresh && token != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String key = "token." + id + "." + user;
                long last_refresh = prefs.getLong(key, 0);
                long ago = now - last_refresh;
                if (ago < ServiceAuthenticator.MIN_REFRESH_INTERVAL) {
                    crumb.put("ago", (ago / 1000L) + " s");
                    Log.breadcrumb("Blocked token refresh", crumb);
                    EntityLog.log(context, "Blocked token refresh id=" + id +
                            " force=" + forceRefresh +
                            " ago=" + (ago / 1000L) + " s" +
                            " exp=" + (expiration == null ? -1 : (expiration - now) / 1000L) + " s");
                } else
                    try {
                        EntityLog.log(context, EntityLog.Type.General, "Invalidating token user=" + id + ":" + user);
                        AccountManager am = AccountManager.get(context);
                        am.invalidateAuthToken(TYPE_GOOGLE, token);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    } finally {
                        prefs.edit().putLong(key, now).apply();
                    }
            }

            Account account = getAccount(context, user.replace("recent:", ""));
            if (account == null)
                throw new AuthenticatorException("Account not found for " + id + ":" + user);

            EntityLog.log(context, EntityLog.Type.General, "Getting token user=" + id + ":" + user);
            AccountManager am = AccountManager.get(context);
            String newToken = am.blockingGetAuthToken(
                    account,
                    ServiceAuthenticator.getAuthTokenType(TYPE_GOOGLE),
                    true);

            crumb.put("acquired", Boolean.toString(newToken != null));
            if (newToken != null)
                crumb.put("refreshed", Boolean.toString(!newToken.equals(token)));
            Log.breadcrumb("Token get", crumb);

            if (newToken != null && !newToken.equals(token)) {
                token = newToken;
                acquired = new Date().getTime();
            } else if (needsRefresh) {
                EntityLog.log(context, EntityLog.Type.General, "Token refresh failed user=" + id + ":" + user);
                if (!BuildConfig.PLAY_STORE_RELEASE)
                    Log.e("Token refresh failed id=" + id);
            }

            if (token == null)
                throw new AuthenticatorException("Got no token id=" + id);
        } catch (Throwable ex) {
            Log.e(ex);
            throw ex;
        }
    }

    static Account getAccount(Context context, String user) {
        AccountManager am = AccountManager.get(context);
        if (am == null)
            return null;
        Account[] accounts = am.getAccountsByType(TYPE_GOOGLE);
        for (Account account : accounts)
            if (Objects.equals(account.name, user))
                return account;
        return null;
    }

    public String jsonSerializeString() {
        try {
            JSONObject jobject = new JSONObject();
            jobject.put("token", token);
            jobject.put("acquired", acquired);
            return jobject.toString();
        } catch (JSONException ex) {
            Log.e(ex);
            return null;
        }
    }

    static GmailState jsonDeserialize(@NonNull String password) {
        try {
            JSONObject jobject = new JSONObject(password);
            String token = jobject.getString("token");
            long acquired = jobject.getLong("acquired");
            return new GmailState(token, acquired);
        } catch (JSONException ex) {
            Log.i(ex);
            return new GmailState(password, new Date().getTime());
        }
    }
}
