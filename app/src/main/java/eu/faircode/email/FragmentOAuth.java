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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretPost;
import net.openid.appauth.NoClientAuthentication;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.browser.BrowserDescriptor;
import net.openid.appauth.browser.BrowserMatcher;
import net.openid.appauth.browser.Browsers;
import net.openid.appauth.browser.VersionRange;
import net.openid.appauth.browser.VersionedBrowserMatcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class FragmentOAuth extends FragmentBase {
    private String name;

    private ViewGroup view;
    private ScrollView scroll;

    private TextView tvGrantHint;
    private Button btnOAuth;
    private ContentLoadingProgressBar pbOAuth;
    private TextView tvAuthorized;
    private TextView tvGmailHint;

    private TextView tvError;
    private Group grpError;
    private TextView tvGmailDraftsHint;

    private static final int OAUTH_TIMEOUT = 20 * 1000; // milliseconds

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        name = args.getString("name");
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(name);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_oauth, container, false);
        scroll = view.findViewById(R.id.scroll);

        // Get controls
        tvGrantHint = view.findViewById(R.id.tvGrantHint);
        btnOAuth = view.findViewById(R.id.btnOAuth);
        pbOAuth = view.findViewById(R.id.pbOAuth);
        tvAuthorized = view.findViewById(R.id.tvAuthorized);
        tvGmailHint = view.findViewById(R.id.tvGmailHint);

        tvError = view.findViewById(R.id.tvError);
        grpError = view.findViewById(R.id.grpError);
        tvGmailDraftsHint = view.findViewById(R.id.tvGmailDraftsHint);

        // Wire controls

        btnOAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAuthorize();
            }
        });

        // Initialize
        tvGrantHint.setText(getString(R.string.title_setup_oauth_rationale, name));
        pbOAuth.setVisibility(View.GONE);
        tvAuthorized.setVisibility(View.GONE);
        tvGmailHint.setVisibility("Gmail".equals(name) ? View.VISIBLE : View.GONE);
        hideError();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_quick_setup, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                onMenuHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuHelp() {
        Bundle args = new Bundle();
        args.putString("name", "SETUP.md");

        FragmentDialogMarkdown fragment = new FragmentDialogMarkdown();
        fragment.setArguments(args);
        fragment.show(getChildFragmentManager(), "help");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ActivitySetup.REQUEST_OAUTH:
                if (resultCode == RESULT_OK && data != null)
                    onHandleOAuth(data);
                break;
            case ActivitySetup.REQUEST_DONE:
                finish();
                break;
        }
    }

    private void onAuthorize() {
        try {
            btnOAuth.setEnabled(false);
            pbOAuth.setVisibility(View.VISIBLE);
            hideError();

            for (EmailProvider provider : EmailProvider.loadProfiles(getContext()))
                if (provider.name.equals(name) && provider.oauth != null) {
                    AppAuthConfiguration appAuthConfig = new AppAuthConfiguration.Builder()
                            .setBrowserMatcher(new BrowserMatcher() {
                                @Override
                                public boolean matches(@NonNull BrowserDescriptor descriptor) {
                                    BrowserMatcher sbrowser = new VersionedBrowserMatcher(
                                            Browsers.SBrowser.PACKAGE_NAME,
                                            Browsers.SBrowser.SIGNATURE_SET,
                                            true,
                                            VersionRange.atMost("5.3"));
                                    return !sbrowser.matches(descriptor);
                                }
                            })
                            .build();

                    AuthorizationService authService = new AuthorizationService(getContext(), appAuthConfig);

                    AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                            Uri.parse(provider.oauth.authorizationEndpoint),
                            Uri.parse(provider.oauth.tokenEndpoint));

                    AuthState authState = new AuthState(serviceConfig);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    prefs.edit().putString("oauth." + provider.name, authState.jsonSerializeString()).apply();

                    Map<String, String> params = new HashMap<>();
                    if ("Gmail".equals(provider.name))
                        params.put("access_type", "offline");

                    AuthorizationRequest.Builder authRequestBuilder =
                            new AuthorizationRequest.Builder(
                                    serviceConfig,
                                    provider.oauth.clientId,
                                    ResponseTypeValues.CODE,
                                    Uri.parse(provider.oauth.redirectUri))
                                    .setScopes(provider.oauth.scopes)
                                    .setState(provider.name)
                                    .setAdditionalParameters(params);

                    if ("Gmail".equals(provider.name) && BuildConfig.DEBUG)
                        authRequestBuilder.setPrompt("consent");

                    AuthorizationRequest authRequest = authRequestBuilder.build();

                    Log.i("OAuth request provider=" + provider.name);
                    if (BuildConfig.DEBUG)
                        Log.i("OAuth uri=" + authRequest.toUri());
                    Intent authIntent = authService.getAuthorizationRequestIntent(authRequest);
                    startActivityForResult(authIntent, ActivitySetup.REQUEST_OAUTH);

                    return;
                }

            throw new IllegalArgumentException("Unknown provider=" + name);
        } catch (Throwable ex) {
            showError(ex);
            btnOAuth.setEnabled(true);
            pbOAuth.setVisibility(View.GONE);
        }
    }

    private void onHandleOAuth(@NonNull Intent data) {
        try {
            AuthorizationResponse auth = AuthorizationResponse.fromIntent(data);
            if (auth == null)
                throw AuthorizationException.fromIntent(data);

            tvAuthorized.setVisibility(View.VISIBLE);

            for (final EmailProvider provider : EmailProvider.loadProfiles(getContext()))
                if (provider.name.equals(auth.state)) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    final AuthState authState = AuthState.jsonDeserialize(prefs.getString("oauth." + provider.name, null));
                    prefs.edit().remove("oauth." + provider.name).apply();

                    Log.i("OAuth get token provider=" + provider.name);
                    authState.update(auth, null);
                    if (BuildConfig.DEBUG)
                        Log.i("OAuth response=" + authState.jsonSerializeString());

                    AuthorizationService authService = new AuthorizationService(getContext());

                    ClientAuthentication clientAuth;
                    if (provider.oauth.clientSecret == null)
                        clientAuth = NoClientAuthentication.INSTANCE;
                    else
                        clientAuth = new ClientSecretPost(provider.oauth.clientSecret);

                    authService.performTokenRequest(
                            auth.createTokenExchangeRequest(),
                            clientAuth,
                            new AuthorizationService.TokenResponseCallback() {
                                @Override
                                public void onTokenRequestCompleted(TokenResponse access, AuthorizationException error) {
                                    try {
                                        if (access == null)
                                            throw error;

                                        Log.i("OAuth got token provider=" + provider.name);
                                        authState.update(access, null);
                                        if (BuildConfig.DEBUG)
                                            Log.i("OAuth response=" + authState.jsonSerializeString());

                                        if (TextUtils.isEmpty(access.refreshToken))
                                            throw new IllegalStateException("No refresh token");

                                        onOAuthorized(access.accessToken, authState);
                                    } catch (Throwable ex) {
                                        showError(ex);
                                    }
                                }
                            });

                    return;
                }

            throw new IllegalArgumentException("Unknown state=" + auth.state);
        } catch (Throwable ex) {
            showError(ex);
            btnOAuth.setEnabled(true);
            pbOAuth.setVisibility(View.GONE);
        }
    }

    private void onOAuthorized(String accessToken, AuthState state) {
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("token", accessToken);
        args.putString("state", state.jsonSerializeString());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                String name = args.getString("name");
                String token = args.getString("token");
                String state = args.getString("state");

                String primaryEmail = null;
                List<Pair<String, String>> identities = new ArrayList<>();

                if ("Gmail".equals(name)) {
                    // https://developers.google.com/gmail/api/v1/reference/users/getProfile
                    URL url = new URL("https://www.googleapis.com/gmail/v1/users/me/settings/sendAs");
                    Log.i("Fetching " + url);

                    HttpURLConnection request = (HttpURLConnection) url.openConnection();
                    request.setReadTimeout(OAUTH_TIMEOUT);
                    request.setConnectTimeout(OAUTH_TIMEOUT);
                    request.setRequestMethod("GET");
                    request.setDoInput(true);
                    request.setRequestProperty("Authorization", "Bearer " + token);
                    request.setRequestProperty("Accept", "application/json");
                    request.connect();

                    String json;
                    try {
                        json = Helper.readStream(request.getInputStream(), StandardCharsets.UTF_8.name());
                        Log.i("Response=" + json);
                    } finally {
                        request.disconnect();
                    }

                    JSONObject data = new JSONObject(json);
                    if (data.has("sendAs")) {
                        JSONArray sendAs = (JSONArray) data.get("sendAs");
                        for (int i = 0; i < sendAs.length(); i++) {
                            JSONObject send = (JSONObject) sendAs.get(i);
                            String sendAsEmail = send.optString("sendAsEmail");
                            String displayName = send.optString("displayName");
                            if (!TextUtils.isEmpty(sendAsEmail)) {
                                if (send.optBoolean("isPrimary"))
                                    primaryEmail = sendAsEmail;
                                if (TextUtils.isEmpty(displayName))
                                    displayName = name;
                                identities.add(new Pair<>(sendAsEmail, displayName));
                            }
                        }
                    }

                } else if ("Outlook/Office365".equals(name)) {
                    // https://docs.microsoft.com/en-us/graph/api/user-get?view=graph-rest-1.0&tabs=http#http-request
                    URL url = new URL("https://graph.microsoft.com/v1.0/me?$select=displayName,otherMails");
                    Log.i("Fetching " + url);

                    HttpURLConnection request = (HttpURLConnection) url.openConnection();
                    request.setReadTimeout(OAUTH_TIMEOUT);
                    request.setConnectTimeout(OAUTH_TIMEOUT);
                    request.setRequestMethod("GET");
                    request.setDoInput(true);
                    request.setRequestProperty("Authorization", "Bearer " + token);
                    request.setRequestProperty("Content-Type", "application/json");
                    request.connect();

                    String json;
                    try {
                        json = Helper.readStream(request.getInputStream(), StandardCharsets.UTF_8.name());
                        Log.i("Response=" + json);
                    } finally {
                        request.disconnect();
                    }

                    JSONObject data = new JSONObject(json);
                    if (data.has("otherMails")) {
                        JSONArray otherMails = data.getJSONArray("otherMails");

                        String displayName = data.getString("displayName");
                        for (int i = 0; i < otherMails.length(); i++) {
                            String email = (String) otherMails.get(i);
                            if (i == 0)
                                primaryEmail = email;
                            if (TextUtils.isEmpty(displayName))
                                displayName = name;
                            identities.add(new Pair<>(email, displayName));
                        }
                    }
                } else
                    throw new IllegalArgumentException("Unknown provider=" + name);

                if (TextUtils.isEmpty(primaryEmail) || identities.size() == 0)
                    throw new IllegalArgumentException("Primary email address not found");

                Log.i("OAuth email=" + primaryEmail);
                for (Pair<String, String> identity : identities)
                    Log.i("OAuth identity=" + identity.first + "/" + identity.second);

                for (EmailProvider provider : EmailProvider.loadProfiles(context))
                    if (provider.name.equals(name)) {

                        List<EntityFolder> folders;

                        Log.i("OAuth checking IMAP provider=" + provider.name);
                        String aprotocol = provider.imap.starttls ? "imap" : "imaps";
                        try (MailService iservice = new MailService(context, aprotocol, null, false, true, true)) {
                            iservice.connect(provider.imap.host, provider.imap.port, MailService.AUTH_TYPE_OAUTH, primaryEmail, state, null);

                            folders = iservice.getFolders();

                            if (folders == null)
                                throw new IllegalArgumentException(context.getString(R.string.title_setup_no_system_folders));
                        }

                        Log.i("OAuth checking SMTP provider=" + provider.name);
                        String iprotocol = provider.smtp.starttls ? "smtp" : "smtps";
                        try (MailService iservice = new MailService(context, iprotocol, null, false, true, true)) {
                            iservice.connect(provider.smtp.host, provider.smtp.port, MailService.AUTH_TYPE_OAUTH, primaryEmail, state, null);
                        }

                        Log.i("OAuth passed provider=" + provider.name);

                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            EntityAccount primary = db.account().getPrimaryAccount();

                            // Create account
                            EntityAccount account = new EntityAccount();

                            account.host = provider.imap.host;
                            account.starttls = provider.imap.starttls;
                            account.port = provider.imap.port;
                            account.auth_type = MailService.AUTH_TYPE_OAUTH;
                            account.user = primaryEmail;
                            account.password = state;

                            account.name = provider.name;

                            account.synchronize = true;
                            account.primary = (primary == null);

                            account.created = new Date().getTime();
                            account.last_connected = account.created;

                            account.id = db.account().insertAccount(account);
                            args.putLong("account", account.id);
                            EntityLog.log(context, "OAuth account=" + account.name);

                            // Create folders
                            for (EntityFolder folder : folders) {
                                folder.account = account.id;
                                folder.id = db.folder().insertFolder(folder);
                                EntityLog.log(context, "OAuth folder=" + folder.name + " type=" + folder.type);
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
                                ident.starttls = provider.smtp.starttls;
                                ident.port = provider.smtp.port;
                                ident.auth_type = MailService.AUTH_TYPE_OAUTH;
                                ident.user = primaryEmail;
                                ident.password = state;
                                ident.synchronize = true;
                                ident.primary = ident.user.equals(ident.email);

                                ident.id = db.identity().insertIdentity(ident);
                                EntityLog.log(context, "OAuth identity=" + ident.name + " email=" + ident.email);
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        ServiceSynchronize.eval(context, "OAuth");
                    }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                pbOAuth.setVisibility(View.GONE);

                FragmentReview fragment = new FragmentReview();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentOAuth.this, ActivitySetup.REQUEST_DONE);
                fragment.show(getParentFragmentManager(), "oauth:review");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                showError(ex);
                btnOAuth.setEnabled(true);
                pbOAuth.setVisibility(View.GONE);
            }
        }.execute(this, args, "oauth:configure");
    }

    void showError(Throwable ex) {
        Log.e(ex);

        pbOAuth.setVisibility(View.GONE);

        if (ex instanceof IllegalArgumentException)
            tvError.setText(ex.getMessage());
        else
            tvError.setText(Log.formatThrowable(ex));

        grpError.setVisibility(View.VISIBLE);

        if ("Gmail".equals(name))
            tvGmailDraftsHint.setVisibility(View.VISIBLE);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scroll.smoothScrollTo(0, tvError.getBottom());
            }
        });
    }

    void hideError() {
        grpError.setVisibility(View.GONE);
        tvGmailDraftsHint.setVisibility(View.GONE);
    }
}
