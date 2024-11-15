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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_OK;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_GRAPH;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_OAUTH;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.net.ssl.HttpsURLConnection;

public class FragmentOAuth extends FragmentBase {
    private String id;
    private String name;
    private String privacy;
    private boolean askAccount;
    private boolean askTenant;

    private String personal;
    private String address;
    private boolean pop;
    private boolean recent;
    private boolean update;

    private ViewGroup view;
    private ScrollView scroll;

    private TextView tvTitle;
    private TextView tvPrivacy;
    private TextView tvPrivacyApp;
    private EditText etName;
    private EditText etEmail;
    private EditText etTenant;
    private CheckBox cbInboundOnly;
    private CheckBox cbPop;
    private CheckBox cbRecent;
    private CheckBox cbUpdate;
    private TextView tvEdge;
    private TextView tvBrave;
    private Button btnOAuth;
    private ContentLoadingProgressBar pbOAuth;
    private TextView tvConfiguring;
    private TextView tvGmailHint;
    private TextView tvGmailLoginHint;
    private TextView tvGmailLoginMax;
    private TextView tvOutlookHint;

    private TextView tvError;
    private TextView tvOfficeAuthHint;
    private Button btnSupport;
    private Button btnHelp;

    private Group grpTenant;
    private Group grpError;

    private static final String FAIREMAIL_RANDOM = "fairemail.random";
    private static final String FAIREMAIL_EXPIRE = "fairemail.expire";
    private static final int MAILRU_TIMEOUT = 20 * 1000; // milliseconds
    private static final long OAUTH_TIMEOUT = 20 * 60 * 1000L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        id = args.getString("id");
        name = args.getString("name");
        privacy = args.getString("privacy");
        askAccount = args.getBoolean("askAccount", false);
        askTenant = args.getBoolean("askTenant", false);

        personal = args.getString("personal");
        address = args.getString("address");
        pop = args.getBoolean("pop", false);
        recent = args.getBoolean("recent", false);
        update = args.getBoolean("update", true);

        lockOrientation();
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
        tvPrivacyApp = view.findViewById(R.id.tvPrivacyApp);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etTenant = view.findViewById(R.id.etTenant);
        cbInboundOnly = view.findViewById(R.id.cbInboundOnly);
        cbPop = view.findViewById(R.id.cbPop);
        cbRecent = view.findViewById(R.id.cbRecent);
        cbUpdate = view.findViewById(R.id.cbUpdate);
        tvEdge = view.findViewById(R.id.tvEdge);
        tvBrave = view.findViewById(R.id.tvBrave);
        btnOAuth = view.findViewById(R.id.btnOAuth);
        pbOAuth = view.findViewById(R.id.pbOAuth);
        tvConfiguring = view.findViewById(R.id.tvConfiguring);
        tvGmailHint = view.findViewById(R.id.tvGmailHint);
        tvGmailLoginHint = view.findViewById(R.id.tvGmailLoginHint);
        tvGmailLoginMax = view.findViewById(R.id.tvGmailLoginMax);
        tvOutlookHint = view.findViewById(R.id.tvOutlookHint);

        tvError = view.findViewById(R.id.tvError);
        tvOfficeAuthHint = view.findViewById(R.id.tvOfficeAuthHint);
        btnSupport = view.findViewById(R.id.btnSupport);
        btnHelp = view.findViewById(R.id.btnHelp);

        grpTenant = view.findViewById(R.id.grpTenant);
        grpError = view.findViewById(R.id.grpError);

        // Wire controls

        tvPrivacy.setPaintFlags(tvPrivacy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(privacy))
                    Helper.view(v.getContext(), Uri.parse(Helper.PRIVACY_URI), false);
                else
                    Helper.view(v.getContext(), Uri.parse(privacy), false);
            }
        });

        tvPrivacy.setVisibility(TextUtils.isEmpty(privacy) ? View.GONE : View.VISIBLE);
        tvPrivacyApp.setPaintFlags(tvPrivacyApp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPrivacyApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.PRIVACY_URI), false);
            }
        });

        cbPop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                cbRecent.setVisibility(checked && "gmail".equals(id) ? View.VISIBLE : View.GONE);
            }
        });

        if ("gmail".equals(id)) {
            // https://developers.google.com/identity/branding-guidelines
            final Context context = getContext();
            final boolean dark = Helper.isDarkTheme(context);
            int dp12 = Helper.dp2pixels(context, 12);
            int dp24 = Helper.dp2pixels(context, 24);
            Drawable g = ContextCompat.getDrawable(context, R.drawable.google_logo);
            g.setBounds(0, 0, g.getIntrinsicWidth(), g.getIntrinsicHeight());
            btnOAuth.setCompoundDrawablesRelative(g, null, null, null);
            btnOAuth.setCompoundDrawablePadding(dp24);
            btnOAuth.setText(R.string.title_setup_google_sign_in);
            btnOAuth.setTextColor(new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_enabled},
                            new int[]{-android.R.attr.state_enabled},
                    },
                    new int[]{
                            dark ? Color.WHITE : Color.DKGRAY, // 0xff444444
                            Color.LTGRAY // 0xffcccccc
                    }
            ));
            btnOAuth.setBackground(ContextCompat.getDrawable(context, dark
                    ? R.drawable.google_signin_background_dark
                    : R.drawable.google_signin_background_light));
            btnOAuth.setPaddingRelative(dp12, 0, dp12, 0);
        }


        boolean edge = false;
        boolean brave = false;
        try {
            PackageManager pm = getContext().getPackageManager();
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse("http://example.com"));
            int flags = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? 0 : PackageManager.MATCH_ALL);
            List<ResolveInfo> browsers = pm.queryIntentActivities(intent, flags);
            for (ResolveInfo browser : browsers)
                if (TextUtils.isEmpty(browser.activityInfo.packageName))
                    continue;
                else if (browser.activityInfo.packageName.startsWith("com.brave.browser")) // _beta _nightly
                    brave = true;
                else if (browser.activityInfo.packageName.startsWith("com.microsoft.emmx")) // .beta .canary .dev
                    edge = true;
                else if (browser.activityInfo.packageName.startsWith("org.mozilla.firefox"))
                    ;
                else if (browser.activityInfo.packageName.equals("org.mozilla.focus"))
                    ;
        } catch (Throwable ex) {
            Log.e(ex);
        }

        tvEdge.setVisibility(edge ? View.VISIBLE : View.GONE);
        tvBrave.setVisibility(brave ? View.VISIBLE : View.GONE);

        btnOAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAuthorize(false);
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "OAuth:support"), false);
            }
        });

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EmailProvider provider = EmailProvider.getProvider(v.getContext(), id);
                    Helper.view(v.getContext(), Uri.parse(provider.link), false);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });

        // Initialize
        tvTitle.setText(getString(R.string.title_setup_oauth_rationale, name));
        etName.setVisibility(askAccount ? View.VISIBLE : View.GONE);
        etEmail.setVisibility(askAccount ? View.VISIBLE : View.GONE);
        grpTenant.setVisibility(askTenant ? View.VISIBLE : View.GONE);
        cbPop.setVisibility(pop ? View.VISIBLE : View.GONE);
        cbRecent.setVisibility(View.GONE);
        pbOAuth.setVisibility(View.GONE);
        tvConfiguring.setVisibility(View.GONE);
        tvGmailHint.setVisibility("gmail".equals(id) ? View.VISIBLE : View.GONE);
        tvGmailLoginHint.setVisibility("gmail".equals(id) ? View.VISIBLE : View.GONE);
        tvGmailLoginMax.setVisibility("gmail".equals(id) ? View.VISIBLE : View.GONE);
        tvOutlookHint.setVisibility("outlookgraph".equals(id) ? View.VISIBLE : View.GONE);
        hideError();

        etName.setText(personal);
        etEmail.setText(address);
        etTenant.setText(null);
        cbInboundOnly.setChecked(false);
        cbPop.setChecked(false);
        cbRecent.setChecked(false);
        cbUpdate.setChecked(update);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case ActivitySetup.REQUEST_OAUTH:
                    Helper.setAuthenticated(getContext());
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

    private void onAuthorize(boolean graph) {
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
            etTenant.clearFocus();
            Helper.hideKeyboard(view);

            setEnabled(false);
            btnOAuth.setEnabled(false);
            pbOAuth.setVisibility(View.VISIBLE);
            hideError();

            Log.breadcrumb("onAuthorize", "id", id);

            final Context context = getContext();
            EmailProvider provider = EmailProvider.getProvider(context, id);
            EmailProvider.OAuth oauth = (graph ? provider.graph : provider.oauth);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean oauth_tabs = prefs.getBoolean("oauth_tabs", true);

            AppAuthConfiguration.Builder appAuthConfig = new AppAuthConfiguration.Builder();

            AuthorizationService authService;
            try {
                appAuthConfig.setBrowserMatcher(getBrowserMatcher(context, oauth_tabs, provider));
                authService = new AuthorizationService(context, appAuthConfig.build());
            } catch (Throwable ex) {
                /*
                    Unihertz, works with Chrome
                    java.lang.SecurityException: Not allowed to bind to service Intent { act=android.support.customtabs.action.CustomTabsService pkg=org.mozilla.focus }
                        at android.app.ContextImpl.bindServiceCommon(ContextImpl.java:1985)
                        at android.app.ContextImpl.bindService(ContextImpl.java:1897)
                        at android.content.ContextWrapper.bindService(ContextWrapper.java:812)
                        at android.content.ContextWrapper.bindService(ContextWrapper.java:812)
                        at androidx.browser.customtabs.CustomTabsClient.bindCustomTabsService(SourceFile:26)
                        at net.openid.appauth.browser.CustomTabManager.bind(SourceFile:27)
                        at net.openid.appauth.AuthorizationService.<init>(SourceFile:12)
                        at net.openid.appauth.AuthorizationService.<init>(SourceFile:4)
                        at eu.faircode.email.FragmentOAuth.onAuthorize(SourceFile:431)
                 */
                Log.e(ex);
                appAuthConfig.setBrowserMatcher(getBrowserMatcher(context, false, provider));
                authService = new AuthorizationService(context, appAuthConfig.build());
            }

            String authorizationEndpoint = oauth.authorizationEndpoint;
            String tokenEndpoint = oauth.tokenEndpoint;
            String tenant = etTenant.getText().toString().trim();

            if (TextUtils.isEmpty(tenant))
                tenant = "common";

            authorizationEndpoint = authorizationEndpoint.replace("{tenant}", tenant);
            tokenEndpoint = tokenEndpoint.replace("{tenant}", tenant);

            AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                    Uri.parse(authorizationEndpoint),
                    Uri.parse(tokenEndpoint));

            int random = Math.abs(new SecureRandom().nextInt());
            long expire = new Date().getTime() + OAUTH_TIMEOUT;
            AuthState authState = new AuthState(serviceConfig);
            String key = "oauth." + provider.id + (graph ? ":graph" : "");
            JSONObject jauthstate = authState.jsonSerialize();
            jauthstate.put(FAIREMAIL_RANDOM, random);
            jauthstate.put(FAIREMAIL_EXPIRE, expire);
            prefs.edit().putString(key, jauthstate.toString()).apply();

            Map<String, String> params = (oauth.parameters == null
                    ? new LinkedHashMap<>()
                    : oauth.parameters);

            String clientId = oauth.clientId;
            Uri redirectUri = Uri.parse(oauth.redirectUri);
            if ("gmail".equals(id) && BuildConfig.DEBUG && false) {
                clientId = "803253368361-hr8kelm53hqodj7c6brdjeb2ctn5jg3p.apps.googleusercontent.com";
                redirectUri = Uri.parse("eu.faircode.email.debug:/");
            }

            // https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest
            AuthorizationRequest.Builder authRequestBuilder =
                    new AuthorizationRequest.Builder(
                            serviceConfig,
                            clientId,
                            ResponseTypeValues.CODE,
                            redirectUri)
                            .setScopes(oauth.scopes)
                            .setState(provider.id + ":" + random + ":" + (graph ? ":graph" : ""))
                            .setAdditionalParameters(params);

            if (askAccount) {
                String address = etEmail.getText().toString().trim();
                int backslash = address.indexOf('\\');
                if (backslash > 0)
                    authRequestBuilder.setLoginHint(address.substring(0, backslash));
                else
                    authRequestBuilder.setLoginHint(address);
            }

            if (!TextUtils.isEmpty(oauth.prompt))
                authRequestBuilder.setPrompt(oauth.prompt);

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

    private BrowserMatcher getBrowserMatcher(Context context, boolean tabs, EmailProvider provider) {
        return new BrowserMatcher() {
            // https://github.com/openid/AppAuth-Android/issues/116
            final BrowserMatcher SBROWSER = new VersionedBrowserMatcher(
                    Browsers.SBrowser.PACKAGE_NAME,
                    Browsers.SBrowser.SIGNATURE_SET,
                    false,
                    VersionRange.atMost("5.3"));
            final BrowserMatcher SBROWSER_TAB = new VersionedBrowserMatcher(
                    Browsers.SBrowser.PACKAGE_NAME,
                    Browsers.SBrowser.SIGNATURE_SET,
                    true,
                    VersionRange.atMost("5.3"));

            @Override
            public boolean matches(@NonNull BrowserDescriptor descriptor) {
                if (BuildConfig.DEBUG) {
                    boolean edge = "org.mozilla.focus".equals(descriptor.packageName);
                    Log.i("MMM " + descriptor.packageName + "=" + edge);
                    return edge;
                }
                boolean accept = !(SBROWSER.matches(descriptor) || SBROWSER_TAB.matches(descriptor));

                if (descriptor.useCustomTab && !tabs)
                    accept = false;

                EntityLog.log(context,
                        "OAuth browser=" + descriptor.packageName +
                                ":" + descriptor.version +
                                " tabs=" + descriptor.useCustomTab + "/" + tabs +
                                " accept=" + accept +
                                " provider=" + provider.id);
                return accept;
            }
        };
    }

    private void onHandleOAuth(@NonNull Intent data) {
        try {
            setEnabled(true);

            Log.breadcrumb("onHandleOAuth", "id", id);

            AuthorizationResponse auth = AuthorizationResponse.fromIntent(data);
            if (auth == null) {
                AuthorizationException ex = AuthorizationException.fromIntent(data);
                if (ex == null)
                    throw new IllegalArgumentException("No response data");
                else
                    throw ex;
            }

            String id = auth.state.split(":")[0];
            int returnedRandom = Integer.parseInt(auth.state.split(":")[1]);
            boolean graph = auth.state.endsWith(":graph");
            final EmailProvider provider = EmailProvider.getProvider(getContext(), id);
            EmailProvider.OAuth oauth = (graph ? provider.graph : provider.oauth);

            if (provider.graph != null &&
                    provider.graph.enabled &&
                    !graph)
                setEnabled(false);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String key = "oauth." + provider.id + (graph ? ":graph" : "");
            String json = prefs.getString(key, null);
            JSONObject jauthstate = new JSONObject(json);
            int random = jauthstate.optInt(FAIREMAIL_RANDOM, -1);
            long expire = jauthstate.optLong(FAIREMAIL_EXPIRE, -1);
            jauthstate.remove(FAIREMAIL_RANDOM);
            prefs.edit().remove("oauth." + auth.state).apply();
            long now = new Date().getTime();

            if (random != returnedRandom)
                throw new SecurityException("random " + random + " <> " + returnedRandom);
            if (expire < now)
                throw new SecurityException("Session expired " + new Date(expire) + " < " + new Date(now));

            final AuthState authState = AuthState.jsonDeserialize(jauthstate);

            Log.i("OAuth get token provider=" + provider.id + " state=" + auth.state);
            authState.update(auth, null);

            AuthorizationService authService = new AuthorizationService(getContext());

            ClientAuthentication clientAuth;
            if (oauth.clientSecret == null)
                clientAuth = NoClientAuthentication.INSTANCE;
            else
                clientAuth = new ClientSecretPost(oauth.clientSecret);

            TokenRequest.Builder builder = new TokenRequest.Builder(
                    auth.request.configuration,
                    auth.request.clientId)
                    .setGrantType(GrantTypeValues.AUTHORIZATION_CODE)
                    .setRedirectUri(auth.request.redirectUri)
                    .setCodeVerifier(auth.request.codeVerifier)
                    .setAuthorizationCode(auth.authorizationCode)
                    .setAdditionalParameters(Collections.<String, String>emptyMap())
                    .setNonce(auth.request.nonce);

            if (oauth.tokenScopes)
                builder.setScope(TextUtils.join(" ", oauth.scopes));

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

                                Set<String> scopeSet = access.getScopeSet();
                                String[] scopes = (scopeSet == null ? new String[0] : scopeSet.toArray(new String[0]));
                                Log.i("OAuth got token provider=" + provider.id +
                                        " state=" + auth.state +
                                        " scopes=" + TextUtils.join(",", scopes));

                                if (TextUtils.isEmpty(access.refreshToken))
                                    throw new IllegalStateException("No refresh token");

                                authState.update(access, null);

                                if (provider.graph == null || !provider.graph.enabled)
                                    onOAuthorized(
                                            new String[]{access.accessToken},
                                            new String[]{access.idToken},
                                            new AuthState[]{authState});
                                else {
                                    if (graph) {
                                        String key0 = "oauth." + provider.id;
                                        String json0 = prefs.getString(key0, null);
                                        prefs.edit().remove(key0).apply();
                                        AuthState state0 = AuthState.jsonDeserialize(json0);
                                        onOAuthorized(
                                                new String[]{state0.getAccessToken(), authState.getAccessToken()},
                                                new String[]{state0.getIdToken(), authState.getIdToken()},
                                                new AuthState[]{state0, authState});
                                    } else {
                                        prefs.edit().putString("oauth." + provider.id, authState.jsonSerializeString()).apply();
                                        onAuthorize(true);
                                    }
                                }
                            } catch (Throwable ex) {
                                showError(ex);
                            }
                        }
                    });
        } catch (Throwable ex) {
            showError(ex);
        }
    }

    private void onOAuthorized(String[] accessToken, String[] idToken, AuthState[] state) {
        Log.breadcrumb("onOAuthorized", "id", id);

        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        List<String> states = new ArrayList<>();
        for (AuthState s : state)
            states.add(s.jsonSerializeString());

        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("name", name);
        args.putStringArray("token", accessToken);
        args.putStringArray("jwt", idToken);
        args.putStringArray("state", states.toArray(new String[0]));
        args.putBoolean("askAccount", askAccount);
        args.putString("personal", etName.getText().toString().trim());
        args.putString("address", etEmail.getText().toString().trim());
        args.putBoolean("inbound_only", cbInboundOnly.isChecked());
        args.putBoolean("pop", cbPop.isChecked());
        args.putBoolean("recent", cbRecent.isChecked());
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
                String[] token = args.getStringArray("token");
                String[] jwt = args.getStringArray("jwt");
                String[] state = args.getStringArray("state");
                boolean askAccount = args.getBoolean("askAccount", false);
                String personal = args.getString("personal");
                String address = args.getString("address");
                boolean inbound_only = args.getBoolean("inbound_only");
                boolean pop = args.getBoolean("pop");
                boolean recent = args.getBoolean("recent");

                EmailProvider provider = EmailProvider.getProvider(context, id);
                if (provider.pop == null)
                    pop = false;
                EmailProvider.Server inbound = (pop ? provider.pop : provider.imap);
                String aprotocol = (pop ? (inbound.starttls ? "pop3" : "pop3s") : (inbound.starttls ? "imap" : "imaps"));
                int aencryption = (inbound.starttls ? EmailService.ENCRYPTION_STARTTLS : EmailService.ENCRYPTION_SSL);
                String iprotocol = (provider.smtp.starttls ? "smtp" : "smtps");
                int iencryption = (provider.smtp.starttls ? EmailService.ENCRYPTION_STARTTLS : EmailService.ENCRYPTION_SSL);

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

                EntityLog.log(context, "OAuth id=" + id + " user=" + username + " shared=" + sharedname);

                if (token[0] != null && sharedname == null && !"gmail".equals(id)) {
                    // https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens
                    String[] segments = token[0].split("\\.");
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

                if (jwt[0] != null && sharedname == null) {
                    // https://docs.microsoft.com/en-us/azure/active-directory/develop/id-tokens
                    String[] segments = jwt[0].split("\\.");
                    if (segments.length > 1)
                        try {
                            // https://jwt.ms/
                            String payload = new String(Base64.decode(segments[1], Base64.DEFAULT));
                            EntityLog.log(context, "jwt payload=" + payload);
                            JSONObject jpayload = new JSONObject(payload);

                            if (jpayload.has("iat"))
                                Log.i("Issued at " + new Date(jpayload.getLong("iat") * 1000L));
                            if (jpayload.has("nbf"))
                                Log.i("Not before " + new Date(jpayload.getLong("nbf") * 1000L));
                            if (jpayload.has("exp"))
                                Log.i("Expiration time " + new Date(jpayload.getLong("exp") * 1000L));

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
                            try (EmailService aservice = new EmailService(context,
                                    aprotocol, null, aencryption, false, false, false,
                                    EmailService.PURPOSE_CHECK, true)) {
                                aservice.connect(
                                        false, inbound.host, inbound.port,
                                        AUTH_TYPE_OAUTH, provider.id,
                                        alt, state[0],
                                        null, null);
                            }
                            if (state.length == 1) {
                                try (EmailService iservice = new EmailService(context,
                                        iprotocol, null, iencryption, false, false, false,
                                        EmailService.PURPOSE_CHECK, true)) {
                                    iservice.connect(
                                            false, provider.smtp.host, provider.smtp.port,
                                            AUTH_TYPE_OAUTH, provider.id,
                                            alt, state[0],
                                            null, null);
                                }
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
                    URL url = new URL("https://oauth.mail.ru/userinfo?access_token=" + token[0]);
                    Log.i("GET " + url);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(MAILRU_TIMEOUT);
                    connection.setConnectTimeout(MAILRU_TIMEOUT);
                    ConnectionHelper.setUserAgent(context, connection);
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

                ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
                NetworkInfo ani = (cm == null ? null : cm.getActiveNetworkInfo());
                if (ani == null || !ani.isConnected())
                    throw new IllegalArgumentException(context.getString(R.string.title_no_internet));

                if (pop && recent && "gmail".equals(id))
                    username = "recent:" + username;

                EntityLog.log(context, "OAuth username=" + username + " shared=" + sharedname);
                for (Pair<String, String> identity : identities)
                    EntityLog.log(context, "OAuth identity=" + identity.first + "/" + identity.second);

                List<EntityFolder> folders;

                EntityLog.log(context, "OAuth checking IMAP/POP3 provider=" + provider.id);
                try (EmailService aservice = new EmailService(context,
                        aprotocol, null, aencryption, false, false, false,
                        EmailService.PURPOSE_CHECK, true)) {
                    aservice.connect(
                            false, inbound.host, inbound.port,
                            AUTH_TYPE_OAUTH, provider.id,
                            sharedname == null ? username : sharedname, state[0],
                            null, null);

                    if (pop)
                        folders = EntityFolder.getPopFolders(context);
                    else
                        folders = aservice.getFolders();
                }

                Long max_size = null;
                if (!inbound_only && state.length == 1) {
                    EntityLog.log(context, "OAuth checking SMTP provider=" + provider.id);

                    if (false && BuildConfig.DEBUG)
                        throw new AuthenticationFailedException("535 5.7.139 Authentication unsuccessful, SmtpClientAuthentication is disabled for the Tenant.");

                    try (EmailService iservice = new EmailService(context,
                            iprotocol, null, iencryption, false, false, false,
                            EmailService.PURPOSE_CHECK, true)) {
                        iservice.connect(
                                false, provider.smtp.host, provider.smtp.port,
                                AUTH_TYPE_OAUTH, provider.id,
                                username, state[0],
                                null, null);
                        max_size = iservice.getMaxSize();
                    }
                }

                EntityLog.log(context, "OAuth passed provider=" + provider.id);

                EntityAccount update = null;
                int protocol = (pop ? EntityAccount.TYPE_POP : EntityAccount.TYPE_IMAP);
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    if (args.getBoolean("update")) {
                        List<EntityAccount> accounts = db.account().getAccounts(sharedname == null ? username : sharedname, protocol);
                        if (accounts != null && accounts.size() == 1)
                            update = accounts.get(0);
                    }

                    if (update == null) {
                        EntityAccount primary = db.account().getPrimaryAccount();

                        // Create account
                        EntityAccount account = new EntityAccount();

                        account.protocol = protocol;
                        account.host = inbound.host;
                        account.encryption = aencryption;
                        account.port = inbound.port;
                        account.auth_type = AUTH_TYPE_OAUTH;
                        account.provider = provider.id;
                        account.user = (sharedname == null ? username : sharedname);
                        account.password = state[0];

                        int at = account.user.indexOf('@');
                        String user = account.user.substring(0, at);

                        account.name = provider.name + "/" + user;

                        account.synchronize = true;
                        account.primary = (primary == null);

                        if (provider.keepalive > 0)
                            account.poll_interval = provider.keepalive;
                        account.keep_alive_noop = provider.noop;

                        account.partial_fetch = provider.partial;
                        account.raw_fetch = provider.raw;

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
                        if (pop) {
                            account.swipe_left = EntityMessage.SWIPE_ACTION_DELETE;
                            account.swipe_right = EntityMessage.SWIPE_ACTION_SEEN;
                        } else
                            FragmentDialogSwipes.setDefaultFolderActions(context, account);

                        db.account().updateAccount(account);

                        // Create identities
                        if (!inbound_only)
                            for (Pair<String, String> identity : identities) {
                                EntityIdentity ident = new EntityIdentity();
                                ident.name = identity.second;
                                ident.email = identity.first;
                                ident.account = account.id;

                                ident.host = provider.smtp.host;
                                ident.encryption = iencryption;
                                ident.port = provider.smtp.port;
                                ident.auth_type = (state.length == 1 ? AUTH_TYPE_OAUTH : AUTH_TYPE_GRAPH);
                                ident.provider = provider.id;
                                ident.user = username;
                                ident.password = state[state.length - 1];
                                ident.use_ip = provider.useip;
                                ident.synchronize = true;
                                ident.primary = ident.user.equals(ident.email);
                                ident.max_size = max_size;

                                ident.id = db.identity().insertIdentity(ident);
                                EntityLog.log(context, "OAuth identity=" + ident.name + " email=" + ident.email);
                            }

                        args.putBoolean("pop", pop);
                    } else {
                        args.putLong("account", update.id);
                        EntityLog.log(context, "OAuth update account=" + update.name);
                        db.account().setAccountSynchronize(update.id, true);
                        db.account().setAccountPassword(update.id, state[0], AUTH_TYPE_OAUTH, provider.id);
                        db.identity().setIdentityPassword(update.id, username,
                                state[state.length - 1],
                                null,
                                (state.length == 1 ? AUTH_TYPE_OAUTH : AUTH_TYPE_GRAPH),
                                provider.id);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                EntityLog.log(context, "OAuth done");

                ServiceSynchronize.eval(context, "OAuth");
                args.putBoolean("updated", update != null);

                FairEmailBackupAgent.dataChanged(context);

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
        setEnabled(true);
        btnOAuth.setEnabled(true);
        pbOAuth.setVisibility(View.GONE);
    }

    private void setEnabled(boolean enabled) {
        etName.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etTenant.setEnabled(enabled);
        cbInboundOnly.setEnabled(enabled);
        cbPop.setEnabled(enabled);
        cbRecent.setEnabled(enabled);
        cbUpdate.setEnabled(enabled);
    }

    private void showError(Throwable ex) {
        Log.e(ex);

        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        if (ex instanceof IllegalArgumentException)
            tvError.setText(new ThrowableWrapper(ex).getSafeMessage());
        else
            tvError.setText(Log.formatThrowable(ex, false));

        grpError.setVisibility(View.VISIBLE);

        if (EntityAccount.isOutlook(id)) {
            if (ex instanceof AuthenticationFailedException) {
                if (ex.getMessage() != null &&
                        ex.getMessage().contains("535 5.7.3 Authentication unsuccessful"))
                    tvOfficeAuthHint.setText(R.string.title_setup_office_auth_5_7_3);
                else if (ex.getMessage() != null &&
                        ex.getMessage().contains("SmtpClientAuthentication is disabled"))
                    // 535 5.7.139 Authentication unsuccessful, SmtpClientAuthentication is disabled for the Tenant
                    tvOfficeAuthHint.setText(R.string.title_setup_office_auth_5_7_139);
                else
                    tvOfficeAuthHint.setText(R.string.title_setup_office_auth);
                tvOfficeAuthHint.setVisibility(View.VISIBLE);
            } else if (ex instanceof MessagingException) {
                boolean notconnected = false;
                Throwable e = ex;
                while (e != null) {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("User is authenticated but not connected")) {
                        notconnected = true;
                        break;
                    }
                    e = e.getCause();
                }
                if (notconnected) {
                    tvOfficeAuthHint.setText(R.string.title_setup_office_not_connected);
                    tvOfficeAuthHint.setVisibility(View.VISIBLE);
                }
            }
        }

        EmailProvider provider;
        try {
            provider = EmailProvider.getProvider(getContext(), id);
        } catch (Throwable exex) {
            Log.e(exex);
            provider = null;
        }

        btnHelp.setVisibility(provider != null && provider.link != null ? View.VISIBLE : View.GONE);

        setEnabled(true);
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
        btnHelp.setVisibility(View.GONE);
        grpError.setVisibility(View.GONE);
        tvOfficeAuthHint.setVisibility(View.GONE);
    }
}
