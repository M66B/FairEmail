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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_OK;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_OAUTH;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationManagementActivity;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretPost;
import net.openid.appauth.CodeVerifierUtil;
import net.openid.appauth.GrantTypeValues;
import net.openid.appauth.NoClientAuthentication;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.browser.BrowserDescriptor;
import net.openid.appauth.browser.BrowserMatcher;
import net.openid.appauth.browser.Browsers;
import net.openid.appauth.browser.VersionRange;
import net.openid.appauth.browser.VersionedBrowserMatcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.AuthenticationFailedException;
import javax.net.ssl.HttpsURLConnection;

public class FragmentOAuth extends FragmentBase {
    private String id;
    private String name;
    private String privacy;
    private boolean askAccount;

    private String personal;
    private String address;
    private boolean update;

    private ViewGroup view;
    private ScrollView scroll;

    private TextView tvTitle;
    private TextView tvPrivacy;
    private EditText etName;
    private EditText etEmail;
    private CheckBox cbUpdate;
    private Button btnOAuth;
    private ContentLoadingProgressBar pbOAuth;
    private TextView tvConfiguring;
    private TextView tvGmailHint;

    private TextView tvError;
    private TextView tvGmailDraftsHint;
    private TextView tvOfficeAuthHint;
    private Button btnSupport;

    private Group grpError;

    private static final int MAILRU_TIMEOUT = 20 * 1000; // milliseconds

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        id = args.getString("id");
        name = args.getString("name");
        privacy = args.getString("privacy");
        askAccount = args.getBoolean("askAccount", false);

        personal = args.getString("personal");
        address = args.getString("address");
        update = args.getBoolean("update");
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(name);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_oauth, container, false);
        scroll = view.findViewById(R.id.scroll);

        // Get controls
        tvTitle = view.findViewById(R.id.tvTitle);
        tvPrivacy = view.findViewById(R.id.tvPrivacy);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        cbUpdate = view.findViewById(R.id.cbUpdate);
        btnOAuth = view.findViewById(R.id.btnOAuth);
        pbOAuth = view.findViewById(R.id.pbOAuth);
        tvConfiguring = view.findViewById(R.id.tvConfiguring);
        tvGmailHint = view.findViewById(R.id.tvGmailHint);

        tvError = view.findViewById(R.id.tvError);
        tvGmailDraftsHint = view.findViewById(R.id.tvGmailDraftsHint);
        tvOfficeAuthHint = view.findViewById(R.id.tvOfficeAuthHint);
        btnSupport = view.findViewById(R.id.btnSupport);

        grpError = view.findViewById(R.id.grpError);

        // Wire controls

        tvPrivacy.setVisibility(TextUtils.isEmpty(privacy) ? View.GONE : View.VISIBLE);
        tvPrivacy.setPaintFlags(tvPrivacy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(privacy), false);
            }
        });

        btnOAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAuthorize();
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext()), false);
            }
        });

        // Initialize
        tvTitle.setText(getString(R.string.title_setup_oauth_rationale, name));
        etName.setVisibility(askAccount ? View.VISIBLE : View.GONE);
        etEmail.setVisibility(askAccount ? View.VISIBLE : View.GONE);
        pbOAuth.setVisibility(View.GONE);
        tvConfiguring.setVisibility(View.GONE);
        tvGmailHint.setVisibility("gmail".equals(id) ? View.VISIBLE : View.GONE);
        hideError();

        etName.setText(personal);
        etEmail.setText(address);
        cbUpdate.setChecked(update);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case ActivitySetup.REQUEST_OAUTH:
                    if (resultCode == RESULT_OK && data != null)
                        onHandleOAuth(data);
                    else
                        onHandleCancel();
                    break;
                case ActivitySetup.REQUEST_DONE:
                    finish();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onAuthorize() {
        try {
            if (askAccount) {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();

                if (TextUtils.isEmpty(name))
                    throw new IllegalArgumentException(getString(R.string.title_no_name));

                if (TextUtils.isEmpty(email))
                    throw new IllegalArgumentException(getString(R.string.title_no_email));

                int backslash = email.indexOf('\\');
                if (backslash > 0)
                    email = email.substring(0, backslash);

                if (!Helper.EMAIL_ADDRESS.matcher(email).matches())
                    throw new IllegalArgumentException(getString(R.string.title_email_invalid, email));
            }

            etName.clearFocus();
            etEmail.clearFocus();
            Helper.hideKeyboard(view);

            etName.setEnabled(false);
            etEmail.setEnabled(false);
            cbUpdate.setEnabled(false);
            btnOAuth.setEnabled(false);
            pbOAuth.setVisibility(View.VISIBLE);
            hideError();

            final Context context = getContext();
            PackageManager pm = context.getPackageManager();
            EmailProvider provider = EmailProvider.getProvider(context, id);

            int flags = PackageManager.GET_RESOLVED_FILTER;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                flags |= PackageManager.MATCH_ALL;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
            List<ResolveInfo> ris = pm.queryIntentActivities(intent, flags);
            EntityLog.log(context, "Browsers=" + (ris == null ? null : ris.size()));
            if (ris != null)
                for (ResolveInfo ri : ris) {
                    Intent serviceIntent = new Intent();
                    serviceIntent.setAction("android.support.customtabs.action.CustomTabsService");
                    serviceIntent.setPackage(ri.activityInfo.packageName);
                    boolean tabs = (pm.resolveService(serviceIntent, 0) != null);

                    StringBuilder sb = new StringBuilder();
                    sb.append("Browser=").append(ri.activityInfo.packageName);
                    sb.append(" tabs=").append(tabs);
                    sb.append(" view=").append(ri.filter.hasAction(Intent.ACTION_VIEW));
                    sb.append(" browsable=").append(ri.filter.hasCategory(Intent.CATEGORY_BROWSABLE));
                    sb.append(" authorities=").append(ri.filter.authoritiesIterator() != null);
                    sb.append(" schemes=");

                    boolean first = true;
                    Iterator<String> schemeIter = ri.filter.schemesIterator();
                    while (schemeIter.hasNext()) {
                        String scheme = schemeIter.next();
                        if (first)
                            first = false;
                        else
                            sb.append(',');
                        sb.append(scheme);
                    }

                    EntityLog.log(context, sb.toString());
                }

            AppAuthConfiguration appAuthConfig = new AppAuthConfiguration.Builder()
                    .setBrowserMatcher(new BrowserMatcher() {
                        final BrowserMatcher SBROWSER = new VersionedBrowserMatcher(
                                Browsers.SBrowser.PACKAGE_NAME,
                                Browsers.SBrowser.SIGNATURE_SET,
                                true,
                                VersionRange.atMost("5.3"));

                        @Override
                        public boolean matches(@NonNull BrowserDescriptor descriptor) {
                            boolean accept =
                                    (!SBROWSER.matches(descriptor) && !descriptor.useCustomTab);
                            EntityLog.log(context,
                                    "Browser=" + descriptor.packageName +
                                            ":" + descriptor.version +
                                            " tabs=" + descriptor.useCustomTab + "" +
                                            " accept=" + accept +
                                            " provider=" + provider.id);
                            return accept;
                        }
                    })
                    .build();

            AuthorizationService authService = new AuthorizationService(context, appAuthConfig);

            AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                    Uri.parse(provider.oauth.authorizationEndpoint),
                    Uri.parse(provider.oauth.tokenEndpoint));

            AuthState authState = new AuthState(serviceConfig);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString("oauth." + provider.id, authState.jsonSerializeString()).apply();

            Map<String, String> params = new HashMap<>();

            if ("gmail".equals(provider.id))
                params.put("access_type", "offline");

            if ("yandex".equals(provider.id)) {
                params.put("device_name", "Android/FairEmail");
                params.put("force_confirm", "true");
            }

            if ("mailru".equals(provider.id))
                params.put("prompt_force", "1");

            AuthorizationRequest.Builder authRequestBuilder =
                    new AuthorizationRequest.Builder(
                            serviceConfig,
                            provider.oauth.clientId,
                            ResponseTypeValues.CODE,
                            Uri.parse(provider.oauth.redirectUri))
                            .setScopes(provider.oauth.scopes)
                            .setState(provider.id)
                            .setAdditionalParameters(params);

            if (askAccount) {
                String address = etEmail.getText().toString().trim();
                int backslash = address.indexOf('\\');
                if (backslash > 0)
                    authRequestBuilder.setLoginHint(address.substring(0, backslash));
                else
                    authRequestBuilder.setLoginHint(address);
            }

            if (provider.oauth.pcke)
                authRequestBuilder.setCodeVerifier(CodeVerifierUtil.generateRandomCodeVerifier());

            // For offline access
            if ("gmail".equals(provider.id))
                authRequestBuilder.setPrompt("consent");

            // https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow
            if ("office365".equals(provider.id))
                authRequestBuilder.setPrompt("select_account");
            if ("outlook".equals(provider.id))
                authRequestBuilder.setPrompt("consent");

            AuthorizationRequest authRequest = authRequestBuilder.build();

            EntityLog.log(context, "OAuth request provider=" + provider.id + " uri=" + authRequest.toUri());
            Intent authIntent;
            try {
                authIntent = authService.getAuthorizationRequestIntent(authRequest);
            } catch (ActivityNotFoundException ex) {
                Log.w(ex);
                authIntent =
                        AuthorizationManagementActivity.createStartForResultIntent(
                                context, authRequest,
                                new Intent(Intent.ACTION_VIEW, authRequest.toUri()));
            }

            startActivityForResult(authIntent, ActivitySetup.REQUEST_OAUTH);
        } catch (Throwable ex) {
            showError(ex);
        }
    }

    private void onHandleOAuth(@NonNull Intent data) {
        try {
            etName.setEnabled(true);
            etEmail.setEnabled(true);
            cbUpdate.setEnabled(true);

            AuthorizationResponse auth = AuthorizationResponse.fromIntent(data);
            if (auth == null)
                throw AuthorizationException.fromIntent(data);

            final EmailProvider provider = EmailProvider.getProvider(getContext(), auth.state);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String json = prefs.getString("oauth." + provider.id, null);
            prefs.edit().remove("oauth." + provider.id).apply();

            final AuthState authState = AuthState.jsonDeserialize(json);

            Log.i("OAuth get token provider=" + provider.id);
            authState.update(auth, null);
            if (BuildConfig.DEBUG)
                Log.i("OAuth response=" + authState.jsonSerializeString());

            AuthorizationService authService = new AuthorizationService(getContext());

            ClientAuthentication clientAuth;
            if (provider.oauth.clientSecret == null)
                clientAuth = NoClientAuthentication.INSTANCE;
            else
                clientAuth = new ClientSecretPost(provider.oauth.clientSecret);

            TokenRequest.Builder builder = new TokenRequest.Builder(
                    auth.request.configuration,
                    auth.request.clientId)
                    .setGrantType(GrantTypeValues.AUTHORIZATION_CODE)
                    .setRedirectUri(auth.request.redirectUri)
                    .setCodeVerifier(auth.request.codeVerifier)
                    .setAuthorizationCode(auth.authorizationCode)
                    .setAdditionalParameters(Collections.<String, String>emptyMap())
                    .setNonce(auth.request.nonce);

            if ("office365".equals(provider.id) || "outlook".equals(provider.id))
                builder.setScope(TextUtils.join(" ", provider.oauth.scopes));

            TokenRequest request = builder.build();

            authService.performTokenRequest(
                    request,
                    clientAuth,
                    new AuthorizationService.TokenResponseCallback() {
                        @Override
                        public void onTokenRequestCompleted(TokenResponse access, AuthorizationException error) {
                            try {
                                if (access == null)
                                    throw error;

                                Log.i("OAuth got token provider=" + provider.id);
                                if (BuildConfig.DEBUG)
                                    Log.i("TokenResponse=" + access.jsonSerializeString());
                                authState.update(access, null);
                                if (BuildConfig.DEBUG)
                                    Log.i("OAuth response=" + authState.jsonSerializeString());

                                if (TextUtils.isEmpty(access.refreshToken))
                                    throw new IllegalStateException("No refresh token");

                                onOAuthorized(access.accessToken, access.idToken, authState);
                            } catch (Throwable ex) {
                                showError(ex);
                            }
                        }
                    });
        } catch (Throwable ex) {
            showError(ex);
        }
    }

    private void onOAuthorized(String accessToken, String idToken, AuthState state) {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("name", name);
        args.putString("token", accessToken);
        args.putString("jwt", idToken);
        args.putString("state", state.jsonSerializeString());
        args.putBoolean("askAccount", askAccount);
        args.putString("personal", etName.getText().toString().trim());
        args.putString("address", etEmail.getText().toString().trim());
        args.putBoolean("update", cbUpdate.isChecked());

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                tvConfiguring.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                tvConfiguring.setVisibility(View.GONE);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                String id = args.getString("id");
                String name = args.getString("name");
                String token = args.getString("token");
                String jwt = args.getString("jwt");
                String state = args.getString("state");
                boolean askAccount = args.getBoolean("askAccount", false);
                String personal = args.getString("personal");
                String address = args.getString("address");

                EmailProvider provider = EmailProvider.getProvider(context, id);
                String aprotocol = (provider.imap.starttls ? "imap" : "imaps");
                int aencryption = (provider.imap.starttls ? EmailService.ENCRYPTION_STARTTLS : EmailService.ENCRYPTION_SSL);
                String iprotocol = (provider.smtp.starttls ? "smtp" : "smtps");
                int iencryption = (provider.smtp.starttls ? EmailService.ENCRYPTION_STARTTLS : EmailService.ENCRYPTION_SSL);

                if ("outlook".equals(id) && BuildConfig.DEBUG) {
                    DB db = DB.getInstance(context);

                    // Create account
                    EntityAccount account = new EntityAccount();

                    account.host = provider.imap.host;
                    account.encryption = aencryption;
                    account.port = provider.imap.port;
                    account.auth_type = AUTH_TYPE_OAUTH;
                    account.provider = provider.id;
                    account.user = address;
                    account.password = state;

                    int at = account.user.indexOf('@');
                    String user = account.user.substring(0, at);

                    account.name = provider.name + "/" + user;

                    account.synchronize = true;
                    account.primary = false;

                    if (provider.keepalive > 0)
                        account.poll_interval = provider.keepalive;

                    account.partial_fetch = provider.partial;

                    account.created = new Date().getTime();
                    account.last_connected = account.created;

                    account.id = db.account().insertAccount(account);
                    args.putLong("account", account.id);
                    EntityLog.log(context, "OAuth account=" + account.name);

                    EntityFolder folder = new EntityFolder("INBOX", EntityFolder.INBOX);
                    folder.account = account.id;
                    folder.setProperties();
                    folder.setSpecials(account);
                    folder.id = db.folder().insertFolder(folder);
                    EntityLog.log(context, "OAuth folder=" + folder.name + " type=" + folder.type);
                    if (folder.synchronize)
                        EntityOperation.sync(context, folder.id, true);

                    return null;
                }

                /*
                 * Outlook shared mailbox
                 * Authenticate: main/shared account
                 * IMAP: shared account
                 * SMTP: main account
                 * https://docs.microsoft.com/en-us/exchange/client-developer/legacy-protocols/how-to-authenticate-an-imap-pop-smtp-application-by-using-oauth#sasl-xoauth2-authentication-for-shared-mailboxes-in-office-365
                 */

                String username;
                String sharedname;
                int backslash = address.indexOf('\\');
                if (backslash > 0) {
                    username = address.substring(0, backslash);
                    sharedname = address.substring(backslash + 1);
                } else {
                    username = address;
                    sharedname = null;
                }

                List<String> usernames = new ArrayList<>();
                usernames.add(sharedname == null ? username : sharedname);

                if (token != null && sharedname == null) {
                    // https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens
                    String[] segments = token.split("\\.");
                    if (segments.length > 1)
                        try {
                            String payload = new String(Base64.decode(segments[1], Base64.DEFAULT));
                            EntityLog.log(context, "token payload=" + payload);
                            JSONObject jpayload = new JSONObject(payload);

                            if (jpayload.has("preferred_username")) {
                                String u = jpayload.getString("preferred_username");
                                if (!TextUtils.isEmpty(u) && !usernames.contains(u))
                                    usernames.add(u);
                            }

                            if (jpayload.has("unique_name")) {
                                String u = jpayload.getString("unique_name");
                                if (!TextUtils.isEmpty(u) && !usernames.contains(u))
                                    usernames.add(u);
                            }

                            if (jpayload.has("upn")) {
                                String u = jpayload.getString("upn");
                                if (!TextUtils.isEmpty(u) && !usernames.contains(u))
                                    usernames.add(u);
                            }
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                }

                if (jwt != null && sharedname == null) {
                    // https://docs.microsoft.com/en-us/azure/active-directory/develop/id-tokens
                    String[] segments = jwt.split("\\.");
                    if (segments.length > 1)
                        try {
                            // https://jwt.ms/
                            String payload = new String(Base64.decode(segments[1], Base64.DEFAULT));
                            EntityLog.log(context, "jwt payload=" + payload);
                            JSONObject jpayload = new JSONObject(payload);

                            if (jpayload.has("preferred_username")) {
                                String u = jpayload.getString("preferred_username");
                                if (!TextUtils.isEmpty(u) && !usernames.contains(u))
                                    usernames.add(u);
                            }

                            if (jpayload.has("email")) {
                                String u = jpayload.getString("email");
                                if (!TextUtils.isEmpty(u) && !usernames.contains(u))
                                    usernames.add(u);
                            }

                            if (jpayload.has("unique_name")) {
                                String u = jpayload.getString("unique_name");
                                if (!TextUtils.isEmpty(u) && !usernames.contains(u))
                                    usernames.add(u);
                            }

                            if (jpayload.has("verified_primary_email")) {
                                JSONArray jsecondary =
                                        jpayload.getJSONArray("verified_primary_email");
                                for (int i = 0; i < jsecondary.length(); i++) {
                                    String u = jsecondary.getString(i);
                                    if (!TextUtils.isEmpty(u) && !usernames.contains(u))
                                        usernames.add(u);
                                }
                            }

                            if (jpayload.has("verified_secondary_email")) {
                                JSONArray jsecondary =
                                        jpayload.getJSONArray("verified_secondary_email");
                                for (int i = 0; i < jsecondary.length(); i++) {
                                    String u = jsecondary.getString(i);
                                    if (!TextUtils.isEmpty(u) && !usernames.contains(u))
                                        usernames.add(u);
                                }
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                }

                if (usernames.size() > 1)
                    for (String alt : usernames) {
                        EntityLog.log(context, "Trying username=" + alt);
                        try {
                            try (EmailService aservice = new EmailService(
                                    context, aprotocol, null, aencryption, false,
                                    EmailService.PURPOSE_CHECK, true)) {
                                aservice.connect(
                                        provider.imap.host, provider.imap.port,
                                        AUTH_TYPE_OAUTH, provider.id,
                                        alt, state,
                                        null, null);
                            }
                            try (EmailService iservice = new EmailService(
                                    context, iprotocol, null, iencryption, false,
                                    EmailService.PURPOSE_CHECK, true)) {
                                iservice.connect(
                                        provider.smtp.host, provider.smtp.port,
                                        AUTH_TYPE_OAUTH, provider.id,
                                        alt, state,
                                        null, null);
                            }
                            EntityLog.log(context, "Using username=" + alt);
                            username = alt;
                            break;
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                    }

                List<Pair<String, String>> identities = new ArrayList<>();

                if (askAccount)
                    identities.add(new Pair<>(username, personal));
                else if ("mailru".equals(id)) {
                    URL url = new URL("https://oauth.mail.ru/userinfo?access_token=" + token);
                    Log.i("GET " + url);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(MAILRU_TIMEOUT);
                    connection.setConnectTimeout(MAILRU_TIMEOUT);
                    connection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
                    connection.connect();

                    try {
                        int status = connection.getResponseCode();
                        if (status != HttpsURLConnection.HTTP_OK)
                            throw new FileNotFoundException("Error " + status + ": " + connection.getResponseMessage());

                        String json = Helper.readStream(connection.getInputStream());
                        Log.i("json=" + json);
                        JSONObject data = new JSONObject(json);
                        name = data.getString("name");
                        username = data.getString("email");
                        identities.add(new Pair<>(username, name));
                    } finally {
                        connection.disconnect();
                    }

                } else
                    throw new IllegalArgumentException("Unknown provider=" + id);

                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ani = (cm == null ? null : cm.getActiveNetworkInfo());
                if (ani == null || !ani.isConnected())
                    throw new IllegalArgumentException(context.getString(R.string.title_no_internet));

                Log.i("OAuth username=" + username);
                for (Pair<String, String> identity : identities)
                    Log.i("OAuth identity=" + identity.first + "/" + identity.second);

                List<EntityFolder> folders;

                Log.i("OAuth checking IMAP provider=" + provider.id);
                try (EmailService aservice = new EmailService(
                        context, aprotocol, null, aencryption, false,
                        EmailService.PURPOSE_CHECK, true)) {
                    aservice.connect(
                            provider.imap.host, provider.imap.port,
                            AUTH_TYPE_OAUTH, provider.id,
                            sharedname == null ? username : sharedname, state,
                            null, null);

                    folders = aservice.getFolders();
                }

                Log.i("OAuth checking SMTP provider=" + provider.id);
                Long max_size;

                try (EmailService iservice = new EmailService(
                        context, iprotocol, null, iencryption, false,
                        EmailService.PURPOSE_CHECK, true)) {
                    iservice.connect(
                            provider.smtp.host, provider.smtp.port,
                            AUTH_TYPE_OAUTH, provider.id,
                            username, state,
                            null, null);
                    max_size = iservice.getMaxSize();
                }

                Log.i("OAuth passed provider=" + provider.id);

                EntityAccount update = null;
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    if (args.getBoolean("update")) {
                        List<EntityAccount> accounts =
                                db.account().getAccounts(username, AUTH_TYPE_OAUTH);
                        if (accounts != null && accounts.size() == 1)
                            update = accounts.get(0);
                    }

                    if (update == null) {
                        EntityAccount primary = db.account().getPrimaryAccount();

                        // Create account
                        EntityAccount account = new EntityAccount();

                        account.host = provider.imap.host;
                        account.encryption = aencryption;
                        account.port = provider.imap.port;
                        account.auth_type = AUTH_TYPE_OAUTH;
                        account.provider = provider.id;
                        account.user = (sharedname == null ? username : sharedname);
                        account.password = state;

                        int at = account.user.indexOf('@');
                        String user = account.user.substring(0, at);

                        account.name = provider.name + "/" + user;

                        account.synchronize = true;
                        account.primary = (primary == null);

                        if (provider.keepalive > 0)
                            account.poll_interval = provider.keepalive;

                        account.partial_fetch = provider.partial;

                        account.created = new Date().getTime();
                        account.last_connected = account.created;

                        account.id = db.account().insertAccount(account);
                        args.putLong("account", account.id);
                        EntityLog.log(context, "OAuth account=" + account.name);

                        // Create folders
                        for (EntityFolder folder : folders) {
                            EntityFolder existing = db.folder().getFolderByName(account.id, folder.name);
                            if (existing == null) {
                                folder.account = account.id;
                                folder.setSpecials(account);
                                folder.id = db.folder().insertFolder(folder);
                                EntityLog.log(context, "OAuth folder=" + folder.name + " type=" + folder.type);
                                if (folder.synchronize)
                                    EntityOperation.sync(context, folder.id, true);
                            }
                        }

                        // Set swipe left/right folder
                        for (EntityFolder folder : folders)
                            if (EntityFolder.TRASH.equals(folder.type))
                                account.swipe_left = folder.id;
                            else if (EntityFolder.ARCHIVE.equals(folder.type))
                                account.swipe_right = folder.id;

                        db.account().updateAccount(account);

                        // Create identities
                        for (Pair<String, String> identity : identities) {
                            EntityIdentity ident = new EntityIdentity();
                            ident.name = identity.second;
                            ident.email = identity.first;
                            ident.account = account.id;

                            ident.host = provider.smtp.host;
                            ident.encryption = iencryption;
                            ident.port = provider.smtp.port;
                            ident.auth_type = AUTH_TYPE_OAUTH;
                            ident.provider = provider.id;
                            ident.user = username;
                            ident.password = state;
                            ident.use_ip = provider.useip;
                            ident.synchronize = true;
                            ident.primary = ident.user.equals(ident.email);
                            ident.max_size = max_size;

                            ident.id = db.identity().insertIdentity(ident);
                            EntityLog.log(context, "OAuth identity=" + ident.name + " email=" + ident.email);
                        }
                    } else {
                        args.putLong("account", update.id);
                        EntityLog.log(context, "OAuth update account=" + update.name);
                        db.account().setAccountSynchronize(update.id, true);
                        db.account().setAccountPassword(update.id, state);
                        db.identity().setIdentityPassword(update.id, update.user, state, update.auth_type);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (update == null)
                    ServiceSynchronize.eval(context, "OAuth");
                else {
                    args.putBoolean("updated", true);
                    ServiceSynchronize.reload(context, update.id, true, "OAuth");
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                pbOAuth.setVisibility(View.GONE);

                boolean updated = args.getBoolean("updated");
                if (updated) {
                    finish();
                    ToastEx.makeText(getContext(), R.string.title_setup_oauth_updated, Toast.LENGTH_LONG).show();
                } else {
                    FragmentDialogAccount fragment = new FragmentDialogAccount();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentOAuth.this, ActivitySetup.REQUEST_DONE);
                    fragment.show(getParentFragmentManager(), "oauth:review");
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                showError(ex);
            }
        }.execute(this, args, "oauth:configure");
    }

    private void onHandleCancel() {
        etName.setEnabled(true);
        etEmail.setEnabled(true);
        cbUpdate.setEnabled(true);
        btnOAuth.setEnabled(true);
        pbOAuth.setVisibility(View.GONE);
    }

    private void showError(Throwable ex) {
        Log.e(ex);

        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        if (ex instanceof IllegalArgumentException)
            tvError.setText(ex.getMessage());
        else
            tvError.setText(Log.formatThrowable(ex, false));

        grpError.setVisibility(View.VISIBLE);

        if ("gmail".equals(id))
            tvGmailDraftsHint.setVisibility(View.VISIBLE);

        if ("office365".equals(id) || "outlook".equals(id)) {
            if (ex instanceof AuthenticationFailedException)
                tvOfficeAuthHint.setVisibility(View.VISIBLE);
        }

        etName.setEnabled(true);
        etEmail.setEnabled(true);
        cbUpdate.setEnabled(true);
        btnOAuth.setEnabled(true);
        pbOAuth.setVisibility(View.GONE);

        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;
                scroll.smoothScrollTo(0, tvError.getBottom());
            }
        });
    }

    private void hideError() {
        grpError.setVisibility(View.GONE);
        tvGmailDraftsHint.setVisibility(View.GONE);
        tvOfficeAuthHint.setVisibility(View.GONE);
    }
}
