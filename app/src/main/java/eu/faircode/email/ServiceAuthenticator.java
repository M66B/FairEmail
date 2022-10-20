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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import static eu.faircode.email.GmailState.TYPE_GOOGLE;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretPost;
import net.openid.appauth.NoClientAuthentication;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;

public class ServiceAuthenticator extends Authenticator {
    private Context context;
    private int auth;
    private String provider;
    private String user;
    private String password;
    private IAuthenticated intf;

    static final int AUTH_TYPE_PASSWORD = 1;
    static final int AUTH_TYPE_GMAIL = 2;
    static final int AUTH_TYPE_OAUTH = 3;

    static final long MIN_REFRESH_INTERVAL = 15 * 60 * 1000L;
    static final long MIN_FORCE_REFRESH_INTERVAL = 15 * 60 * 1000L;
    static final int MAX_TOKEN_WAIT = 90; // seconds

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
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String token = password;
        try {
            token = refreshToken(false);
        } catch (Throwable ex) {
            if (ex.getCause() instanceof InterruptedException)
                Log.i(ex);
            else if (ex.getMessage() != null && ex.getMessage().startsWith("OAuth refresh"))
                Log.w(ex);
            else
                Log.e(ex);
        }

        Log.i(user + " returning " +
                (auth == AUTH_TYPE_PASSWORD ? "password" : "token") +
                (BuildConfig.DEBUG ? "=" + token : ""));
        return new PasswordAuthentication(user, token);
    }

    String refreshToken(boolean forceRefresh) throws AuthenticatorException, OperationCanceledException, IOException, JSONException, MessagingException {
        if (auth == AUTH_TYPE_GMAIL) {
            GmailState authState = GmailState.jsonDeserialize(password);
            authState.refresh(context, "android", user, forceRefresh);
            Long expiration = authState.getAccessTokenExpirationTime();
            if (expiration != null)
                EntityLog.log(context, user + " token expiration=" + new Date(expiration));

            String newPassword = authState.jsonSerializeString();
            if (!Objects.equals(password, newPassword)) {
                password = newPassword;
                if (intf != null)
                    intf.onPasswordChanged(context, password);
            }

            return authState.getAccessToken();
        } else if (auth == AUTH_TYPE_OAUTH && provider != null) {
            AuthState authState = AuthState.jsonDeserialize(password);
            OAuthRefresh(context, provider, user, authState, forceRefresh);
            Long expiration = authState.getAccessTokenExpirationTime();
            if (expiration != null)
                EntityLog.log(context, user + " token expiration=" + new Date(expiration));

            String newPassword = authState.jsonSerializeString();
            if (!Objects.equals(password, newPassword)) {
                password = newPassword;
                if (intf != null)
                    intf.onPasswordChanged(context, password);
            }

            return authState.getAccessToken();
        } else
            return password;
    }

    Long getAccessTokenExpirationTime() {
        try {
            if (auth == AUTH_TYPE_GMAIL) {
                GmailState authState = GmailState.jsonDeserialize(password);
                return authState.getAccessTokenExpirationTime();
            } else if (auth == AUTH_TYPE_OAUTH) {
                AuthState authState = AuthState.jsonDeserialize(password);
                return authState.getAccessTokenExpirationTime();
            }
        } catch (JSONException ex) {
            Log.e(ex);
        }
        return null;
    }

    interface IAuthenticated {
        void onPasswordChanged(Context context, String newPassword);
    }

    private static void OAuthRefresh(Context context, String id, String user, AuthState authState, boolean forceRefresh)
            throws MessagingException {
        try {
            long now = new Date().getTime();
            Long expiration = authState.getAccessTokenExpirationTime();
            boolean needsRefresh = (expiration != null && expiration < now);

            if (!needsRefresh && forceRefresh &&
                    expiration != null &&
                    expiration - MIN_FORCE_REFRESH_INTERVAL < now)
                needsRefresh = true;

            if (needsRefresh)
                authState.setNeedsTokenRefresh(true);

            EntityLog.log(context, EntityLog.Type.General, "Token user=" + id + ":" + user +
                    " expiration=" + (expiration == null ? null : new Date(expiration)) +
                    " need=" + needsRefresh + "/" + authState.getNeedsTokenRefresh() +
                    " force=" + forceRefresh);

            ClientAuthentication clientAuth;
            EmailProvider provider = EmailProvider.getProvider(context, id);
            if (provider.oauth.clientSecret == null)
                clientAuth = NoClientAuthentication.INSTANCE;
            else
                clientAuth = new ClientSecretPost(provider.oauth.clientSecret);

            ErrorHolder holder = new ErrorHolder();
            Semaphore semaphore = new Semaphore(0);

            Log.i("OAuth refresh user=" + id + ":" + user);
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

            if (!semaphore.tryAcquire(MAX_TOKEN_WAIT, TimeUnit.SECONDS))
                throw new InterruptedException("Timeout getting token id=" + id);

            Log.i("OAuth refreshed user=" + id + ":" + user);

            if (holder.error != null) {
                if (!BuildConfig.PLAY_STORE_RELEASE)
                    Log.e(new Throwable("Token refresh failed id=" + id, holder.error));
                throw holder.error;
            }
        } catch (Exception ex) {
            throw new MessagingException("OAuth refresh id=" + id, ex);
        }
    }

    static String getAuthTokenType(String type) {
        // https://developers.google.com/gmail/imap/xoauth2-protocol
        if (TYPE_GOOGLE.equals(type))
            return "oauth2:https://mail.google.com/";
        return null;
    }

    private static class ErrorHolder {
        AuthorizationException error;
    }
}
