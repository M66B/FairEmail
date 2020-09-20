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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import net.openid.appauth.CodeVerifierUtil;
import net.openid.appauth.NoClientAuthentication;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.browser.BrowserDescriptor;
import net.openid.appauth.browser.BrowserMatcher;
import net.openid.appauth.browser.Browsers;
import net.openid.appauth.browser.VersionRange;
import net.openid.appauth.browser.VersionedBrowserMatcher;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.AuthenticationFailedException;

import static android.app.Activity.RESULT_OK;

public class FragmentOAuth extends FragmentBase {
    private String id;
    private String name;
    private boolean askAccount;

    private ViewGroup view;
    private ScrollView scroll;

    private TextView tvGrantHint;
    private EditText etName;
    private EditText etEmail;
    private Button btnOAuth;
    private ContentLoadingProgressBar pbOAuth;
    private TextView tvConfiguring;
    private TextView tvGmailHint;

    private TextView tvError;
    private TextView tvGmailDraftsHint;
    private TextView tvOfficeAuthHint;
    private Button btnSupport;

    private Group grpError;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        id = args.getString("id");
        name = args.getString("name");
        askAccount = args.getBoolean("askAccount", false);
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
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
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

        btnOAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAuthorize();
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.view(getContext(), Uri.parse(Helper.SUPPORT_URI), false);
            }
        });

        // Initialize
        tvGrantHint.setText(getString(R.string.title_setup_oauth_rationale, name));
        etName.setVisibility(askAccount ? View.VISIBLE : View.GONE);
        etEmail.setVisibility(askAccount ? View.VISIBLE : View.GONE);
        pbOAuth.setVisibility(View.GONE);
        tvConfiguring.setVisibility(View.GONE);
        tvGmailHint.setVisibility("gmail".equals(id) ? View.VISIBLE : View.GONE);
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
                if (!Helper.EMAIL_ADDRESS.matcher(email).matches())
                    throw new IllegalArgumentException(getString(R.string.title_email_invalid, email));
            }

            etName.setEnabled(false);
            etEmail.setEnabled(false);
            btnOAuth.setEnabled(false);
            pbOAuth.setVisibility(View.VISIBLE);
            hideError();

            EmailProvider provider = EmailProvider.getProvider(getContext(), id);

            AppAuthConfiguration appAuthConfig = new AppAuthConfiguration.Builder()
                    .setBrowserMatcher(new BrowserMatcher() {
                        @Override
                        public boolean matches(@NonNull BrowserDescriptor descriptor) {
                            BrowserMatcher sbrowser = new VersionedBrowserMatcher(
                                    Browsers.SBrowser.PACKAGE_NAME,
                                    Browsers.SBrowser.SIGNATURE_SET,
                                    true,
                                    VersionRange.atMost("5.3"));
                            return (!sbrowser.matches(descriptor) &&
                                    (!"gmail".equals(provider.id) || !descriptor.useCustomTab));
                        }
                    })
                    .build();

            AuthorizationService authService = new AuthorizationService(getContext(), appAuthConfig);

            AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                    Uri.parse(provider.oauth.authorizationEndpoint),
                    Uri.parse(provider.oauth.tokenEndpoint));

            AuthState authState = new AuthState(serviceConfig);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.edit().putString("oauth." + provider.id, authState.jsonSerializeString()).apply();

            Map<String, String> params = new HashMap<>();

            if ("gmail".equals(provider.id))
                params.put("access_type", "offline");

            if ("yandex".equals(provider.id)) {
                params.put("device_name", "Android/FairEmail");
                params.put("force_confirm", "true");
            }

            AuthorizationRequest.Builder authRequestBuilder =
                    new AuthorizationRequest.Builder(
                            serviceConfig,
                            provider.oauth.clientId,
                            ResponseTypeValues.CODE,
                            Uri.parse(provider.oauth.redirectUri))
                            .setScopes(provider.oauth.scopes)
                            .setState(provider.id)
                            .setAdditionalParameters(params);

            if (askAccount)
                authRequestBuilder.setLoginHint(etEmail.getText().toString().trim());

            if (provider.oauth.pcke)
                authRequestBuilder.setCodeVerifier(CodeVerifierUtil.generateRandomCodeVerifier());

            // For offline access
            if ("gmail".equals(provider.id))
                authRequestBuilder.setPrompt("consent");

            if ("office365".equals(provider.id))
                authRequestBuilder.setPrompt("select_account");

            AuthorizationRequest authRequest = authRequestBuilder.build();

            Log.i("OAuth request provider=" + provider.id + " uri=" + authRequest.toUri());
            Intent authIntent = null;
            try {
                authIntent = authService.getAuthorizationRequestIntent(authRequest);
            } catch (ActivityNotFoundException ex) {
                throw new ActivityNotFoundException("Browser not found");
            }
            PackageManager pm = getContext().getPackageManager();
            if (authIntent.resolveActivity(pm) == null) // action whitelisted
                throw new ActivityNotFoundException(authIntent.toString());
            else
                startActivityForResult(authIntent, ActivitySetup.REQUEST_OAUTH);
        } catch (Throwable ex) {
            showError(ex);
        }
    }

    private void onHandleOAuth(@NonNull Intent data) {
        try {
            etName.setEnabled(true);
            etEmail.setEnabled(true);

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

            authService.performTokenRequest(
                    auth.createTokenExchangeRequest(),
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
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("name", name);
        args.putString("token", accessToken);
        args.putString("jwt", idToken);
        args.putString("state", state.jsonSerializeString());
        args.putBoolean("askAccount", askAccount);
        args.putString("personal", etName.getText().toString().trim());
        args.putString("address", etEmail.getText().toString().trim());

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

                String username = address;

                if (accessToken != null) {
                    // https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens
                    String[] segments = accessToken.split("\\.");
                    if (segments.length > 1)
                        try {
                            String payload = new String(Base64.decode(segments[1], Base64.DEFAULT));
                            EntityLog.log(context, "token payload=" + payload);
                            JSONObject jpayload = new JSONObject(payload);
                            if (jpayload.has("unique_name")) {
                                String unique_name = jpayload.getString("unique_name");
                                if (!TextUtils.isEmpty(unique_name) && !unique_name.equals(address)) {
                                    try (EmailService iservice = new EmailService(
                                            context, aprotocol, null, aencryption, false,
                                            EmailService.PURPOSE_CHECK, true)) {
                                        iservice.connect(
                                                provider.imap.host, provider.imap.port,
                                                EmailService.AUTH_TYPE_OAUTH, provider.id,
                                                unique_name, state,
                                                null, null);
                                        username = unique_name;
                                        Log.i("token unique_name=" + unique_name);
                                    } catch (Throwable ex) {
                                        Log.w(ex);
                                    }
                                }
                            }
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                }

                if (jwt != null) {
                    // https://docs.microsoft.com/en-us/azure/active-directory/develop/id-tokens
                    String[] segments = jwt.split("\\.");
                    if (segments.length > 1)
                        try {
                            // https://jwt.ms/
                            String payload = new String(Base64.decode(segments[1], Base64.DEFAULT));
                            EntityLog.log(context, "jwt payload=" + payload);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                }

                List<Pair<String, String>> identities = new ArrayList<>();

                if (askAccount)
                    identities.add(new Pair<>(address, personal));
                else
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
                try (EmailService iservice = new EmailService(
                        context, aprotocol, null, aencryption, false,
                        EmailService.PURPOSE_CHECK, true)) {
                    iservice.connect(
                            provider.imap.host, provider.imap.port,
                            EmailService.AUTH_TYPE_OAUTH, provider.id,
                            username, state,
                            null, null);

                    folders = iservice.getFolders();
                }

                Log.i("OAuth checking SMTP provider=" + provider.id);
                Long max_size;
                String iprotocol = (provider.smtp.starttls ? "smtp" : "smtps");
                int iencryption = (provider.smtp.starttls ? EmailService.ENCRYPTION_STARTTLS : EmailService.ENCRYPTION_SSL);

                try (EmailService iservice = new EmailService(
                        context, iprotocol, null, iencryption, false,
                        EmailService.PURPOSE_CHECK, true)) {
                    iservice.connect(
                            provider.smtp.host, provider.smtp.port,
                            EmailService.AUTH_TYPE_OAUTH, provider.id,
                            username, state,
                            null, null);
                    max_size = iservice.getMaxSize();
                }

                Log.i("OAuth passed provider=" + provider.id);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityAccount primary = db.account().getPrimaryAccount();

                    // Create account
                    EntityAccount account = new EntityAccount();

                    account.host = provider.imap.host;
                    account.encryption = aencryption;
                    account.port = provider.imap.port;
                    account.auth_type = EmailService.AUTH_TYPE_OAUTH;
                    account.provider = provider.id;
                    account.user = username;
                    account.password = state;

                    int at = address.indexOf('@');
                    String user = address.substring(0, at);

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
                            folder.id = db.folder().insertFolder(folder);
                            EntityLog.log(context, "OAuth folder=" + folder.name + " type=" + folder.type);
                            if (folder.synchronize)
                                EntityOperation.sync(context, folder.id, false);
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
                        ident.auth_type = EmailService.AUTH_TYPE_OAUTH;
                        ident.provider = provider.id;
                        ident.user = username;
                        ident.password = state;
                        ident.synchronize = true;
                        ident.primary = ident.user.equals(ident.email);
                        ident.max_size = max_size;

                        ident.id = db.identity().insertIdentity(ident);
                        EntityLog.log(context, "OAuth identity=" + ident.name + " email=" + ident.email);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "OAuth");

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
            }
        }.execute(this, args, "oauth:configure");
    }

    private void onHandleCancel() {
        etName.setEnabled(true);
        etEmail.setEnabled(true);
        btnOAuth.setEnabled(true);
        pbOAuth.setVisibility(View.GONE);
    }

    private void showError(Throwable ex) {
        Log.e(ex);

        if (ex instanceof IllegalArgumentException)
            tvError.setText(ex.getMessage());
        else
            tvError.setText(Log.formatThrowable(ex));

        grpError.setVisibility(View.VISIBLE);

        if ("gmail".equals(id))
            tvGmailDraftsHint.setVisibility(View.VISIBLE);

        if ("office365".equals(id) &&
                ex instanceof AuthenticationFailedException)
            tvOfficeAuthHint.setVisibility(View.VISIBLE);

        etName.setEnabled(true);
        etEmail.setEnabled(true);
        btnOAuth.setEnabled(true);
        pbOAuth.setVisibility(View.GONE);

        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
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
