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

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretPost;
import net.openid.appauth.NoClientAuthentication;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;

class ServiceAuthenticator extends Authenticator {
    private Context context;
    private int auth;
    private String provider;
    private String user;
    private String password;
    private IAuthenticated intf;
    private long refreshed;

    static final int AUTH_TYPE_PASSWORD = 1;
    static final int AUTH_TYPE_GMAIL = 2;
    static final int AUTH_TYPE_OAUTH = 3;

    static final String TYPE_GOOGLE = "com.google";
    private static final long GMAIL_EXPIRY = 3600 * 1000L;

    ServiceAuthenticator(
            Context context,
            int auth, String provider,
            String user, String password,
            IAuthenticated intf) {
        this.context = context.getApplicationContext();
        this.auth = auth;
        this.provider = provider;
        this.user = user;
        this.password = password;
        this.intf = intf;
        this.refreshed = new Date().getTime();
    }

    void expire() {
        if (auth == AUTH_TYPE_GMAIL) {
            EntityLog.log(context, user + " token expired");
            expireGmailToken(context, password);
            password = null;
        }
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String token = password;
        try {
            if (auth == AUTH_TYPE_GMAIL) {
                long now = new Date().getTime();
                if (now - refreshed > GMAIL_EXPIRY)
                    expire();

                String oldToken = password;
                token = getGmailToken(context, user);
                password = token;

                if (intf != null && !Objects.equals(oldToken, token))
                    intf.onPasswordChanged(password);
            } else if (auth == AUTH_TYPE_OAUTH) {
                AuthState authState = AuthState.jsonDeserialize(password);
                String oldToken = authState.getAccessToken();
                OAuthRefresh(context, provider, authState);
                token = authState.getAccessToken();
                password = authState.jsonSerializeString();

                if (intf != null && !Objects.equals(oldToken, token))
                    intf.onPasswordChanged(password);
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
        Log.i(user + " returning password");
        return new PasswordAuthentication(user, token);
    }

    interface IAuthenticated {
        void onPasswordChanged(String newPassword);
    }

    static String getGmailToken(Context context, String user) throws AuthenticatorException, OperationCanceledException, IOException {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(TYPE_GOOGLE);
        for (Account account : accounts)
            if (user.equals(account.name)) {
                Log.i("Getting token user=" + user);
                String token = am.blockingGetAuthToken(account, getAuthTokenType(TYPE_GOOGLE), true);
                if (token == null)
                    throw new AuthenticatorException("No token for " + user);

                return token;
            }

        throw new AuthenticatorException("Account not found for " + user);
    }

    private static void expireGmailToken(Context context, String token) {
        try {
            AccountManager am = AccountManager.get(context);
            am.invalidateAuthToken(TYPE_GOOGLE, token);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static void OAuthRefresh(Context context, String id, AuthState authState) throws MessagingException {
        try {
            ClientAuthentication clientAuth;
            EmailProvider provider = EmailProvider.getProvider(context, id);
            if (provider.oauth.clientSecret == null)
                clientAuth = NoClientAuthentication.INSTANCE;
            else
                clientAuth = new ClientSecretPost(provider.oauth.clientSecret);

            ErrorHolder holder = new ErrorHolder();
            Semaphore semaphore = new Semaphore(0);

            Log.i("OAuth refresh");
            AuthorizationService authService = new AuthorizationService(context);
            authState.performActionWithFreshTokens(
                    authService,
                    clientAuth,
                    new AuthState.AuthStateAction() {
                        @Override
                        public void execute(String accessToken, String idToken, AuthorizationException error) {
                            if (error != null)
                                holder.error = error;
                            semaphore.release();
                        }
                    });

            semaphore.acquire();
            Log.i("OAuth refreshed");

            if (holder.error != null)
                throw holder.error;
        } catch (Exception ex) {
            throw new MessagingException("OAuth refresh", ex);
        }
    }

    static String getAuthTokenType(String type) {
        // https://developers.google.com/gmail/imap/xoauth2-protocol
        if ("com.google".equals(type))
            return "oauth2:https://mail.google.com/";
        return null;
    }

    private static class ErrorHolder {
        AuthorizationException error;
    }
}
