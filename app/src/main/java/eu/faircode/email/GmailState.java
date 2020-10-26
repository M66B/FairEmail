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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class GmailState {
    private String token;
    private long acquired;

    static final String TYPE_GOOGLE = "com.google";
    private static final long TOKEN_LIFETIME = 3600 * 1000L; // milliseconds

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

    void refresh(@NonNull Context context, @NonNull String user, boolean expire) throws AuthenticatorException, OperationCanceledException, IOException {
        if (expire || acquired + TOKEN_LIFETIME < new Date().getTime())
            try {
                if (token != null) {
                    EntityLog.log(context, "Invalidating token user=" + user);
                    AccountManager am = AccountManager.get(context);
                    am.invalidateAuthToken(TYPE_GOOGLE, token);
                }
                token = null;
                acquired = 0;
            } catch (Throwable ex) {
                Log.e(ex);
            }

        Account account = getAccount(context, user);
        if (account == null)
            throw new AuthenticatorException("Account not found for " + user);

        EntityLog.log(context, "Getting token user=" + user);
        AccountManager am = AccountManager.get(context);
        String newToken = am.blockingGetAuthToken(
                account,
                ServiceAuthenticator.getAuthTokenType(TYPE_GOOGLE),
                true);

        if (newToken != null && !newToken.equals(token)) {
            token = newToken;
            acquired = new Date().getTime();
        }

        if (token == null)
            throw new AuthenticatorException("No token for " + user);
    }

    static Account getAccount(Context context, String user) {
        AccountManager am = AccountManager.get(context);
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
            return new GmailState(password, new Date().getTime());
        }
    }
}
