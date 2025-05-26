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

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.text.method.LinkMovementMethodCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.textfield.TextInputLayout;

import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Store;

public class FragmentQuickSetup extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private TextView tvPrivacy;
    private TextView tvPrivacyApp;
    private EditText etName;
    private EditText etEmail;
    private TextInputLayout tilPassword;
    private TextView tvCharacters;
    private TextView tvOutlookModern;
    private Button btnCheck;
    private ContentLoadingProgressBar pbCheck;
    private TextView tvPatience;
    private TextView tvProgress;

    private TextView tvArgument;
    private TextView tvError;
    private TextView tvErrorHint;
    private Button btnManual;
    private TextView tvInstructions;
    private Button btnHelp;
    private Button btnSupport;

    private TextView tvUser;
    private TextView tvImap;
    private TextView tvSmtp;

    private TextView tvImapFingerprint;
    private TextView tvImapDnsNames;
    private TextView tvSmtpFingerprint;
    private TextView tvSmtpDnsNames;

    private CheckBox cbUpdate;
    private Button btnSave;
    private ContentLoadingProgressBar pbSave;

    private Group grpSetup;
    private Group grpCertificate;
    private Group grpError;
    private Group grpManual;

    private int title;
    private boolean update;
    private EmailProvider bestProvider = null;
    private Bundle bestArgs = null;

    private static final String PRIVACY_URI = "https://www.mozilla.org/privacy/";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("fair:password", tilPassword == null ? null : tilPassword.getEditText().getText().toString());
        outState.putParcelable("fair:best", bestProvider);
        outState.putParcelable("fair:args", bestArgs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        title = args.getInt("title", R.string.title_setup_other);
        update = args.getBoolean("update", true);

        lockOrientation();
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(title);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_quick_setup, container, false);
        scroll = view.findViewById(R.id.scroll);

        // Get controls
        tvPrivacy = view.findViewById(R.id.tvPrivacy);
        tvPrivacyApp = view.findViewById(R.id.tvPrivacyApp);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvCharacters = view.findViewById(R.id.tvCharacters);
        tvOutlookModern = view.findViewById(R.id.tvOutlookModern);
        btnCheck = view.findViewById(R.id.btnCheck);
        pbCheck = view.findViewById(R.id.pbCheck);
        tvPatience = view.findViewById(R.id.tvPatience);
        tvProgress = view.findViewById(R.id.tvProgress);

        tvArgument = view.findViewById(R.id.tvArgument);
        tvError = view.findViewById(R.id.tvError);
        tvErrorHint = view.findViewById(R.id.tvErrorHint);
        btnManual = view.findViewById(R.id.btnManual);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnSupport = view.findViewById(R.id.btnSupport);

        tvUser = view.findViewById(R.id.tvUser);
        tvImap = view.findViewById(R.id.tvImap);
        tvSmtp = view.findViewById(R.id.tvSmtp);

        tvImapFingerprint = view.findViewById(R.id.tvImapFingerprint);
        tvImapDnsNames = view.findViewById(R.id.tvImapDnsNames);
        tvSmtpFingerprint = view.findViewById(R.id.tvSmtpFingerprint);
        tvSmtpDnsNames = view.findViewById(R.id.tvSmtpDnsNames);

        cbUpdate = view.findViewById(R.id.cbUpdate);
        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);

        grpSetup = view.findViewById(R.id.grpSetup);
        grpCertificate = view.findViewById(R.id.grpCertificate);
        grpError = view.findViewById(R.id.grpError);
        grpManual = view.findViewById(R.id.grpManual);

        // Wire controls

        tvPrivacy.setPaintFlags(tvPrivacy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(PRIVACY_URI), false);
            }
        });

        tvPrivacyApp.setPaintFlags(tvPrivacyApp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPrivacyApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.PRIVACY_URI), false);
            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().toLowerCase(Locale.ROOT);
                boolean outlook = (email.contains("@outlook.") ||
                        email.contains("@hotmail.") ||
                        email.contains("@live."));
                tvOutlookModern.setVisibility(outlook ? View.VISIBLE : View.GONE);
            }
        });
        tilPassword.setHintEnabled(false);

        tilPassword.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    onSave(true);
                    return true;
                }
                return false;
            }
        });

        tilPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (tvCharacters == null || tilPassword == null)
                    return;

                String password = s.toString();
                boolean warning = (Helper.containsWhiteSpace(password) ||
                        Helper.containsControlChars(password));
                tvCharacters.setVisibility(warning &&
                        tilPassword.getVisibility() == View.VISIBLE
                        ? View.VISIBLE : View.GONE);
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave(true);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave(false);
            }
        });

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, (Uri) btnHelp.getTag());
                Helper.view(getContext(), intent);
            }
        });

        btnManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("manual", true)
                        .putExtra("scroll", true));
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "Quick:support"), false);
            }
        });

        // Initialize
        tvCharacters.setVisibility(View.GONE);
        tvOutlookModern.setVisibility(View.GONE);
        tvImapFingerprint.setText(null);
        tvSmtpFingerprint.setText(null);
        pbCheck.setVisibility(View.GONE);
        tvPatience.setVisibility(View.GONE);
        tvProgress.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        tvArgument.setVisibility(View.GONE);
        tvErrorHint.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);
        tvInstructions.setMovementMethod(LinkMovementMethodCompat.getInstance());
        btnHelp.setVisibility(View.GONE);
        cbUpdate.setChecked(update);
        cbUpdate.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
        grpSetup.setVisibility(View.GONE);
        grpCertificate.setVisibility(View.GONE);
        grpError.setVisibility(View.GONE);
        grpManual.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            tilPassword.getEditText().setText(savedInstanceState.getString("fair:password"));
            bestProvider = savedInstanceState.getParcelable("fair:best");
            bestArgs = savedInstanceState.getParcelable("fair:args");
            showResult(bestProvider, bestArgs);
        }

        return view;
    }

    private void onSave(boolean check) {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = tilPassword.getEditText().getText().toString();
        String warning = null;
        if (TextUtils.isEmpty(name))
            warning = getString(R.string.title_no_name);
        else if (TextUtils.isEmpty(email))
            warning = getString(R.string.title_no_email);
        else if (!Helper.EMAIL_ADDRESS.matcher(email).matches())
            warning = getString(R.string.title_email_invalid, email);
        else if (TextUtils.isEmpty(password))
            warning = getString(R.string.title_no_password);
        else {
            ConnectivityManager cm = Helper.getSystemService(getContext(), ConnectivityManager.class);
            NetworkInfo ani = (cm == null ? null : cm.getActiveNetworkInfo());
            if (ani == null || !ani.isConnected())
                warning = getString(R.string.title_no_internet);
        }

        if (warning != null) {
            tvArgument.setText(warning);
            tvArgument.setVisibility(View.VISIBLE);
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;
                    scroll.smoothScrollTo(0, tvArgument.getBottom());
                }
            });
            return;
        }

        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("email", email);
        args.putString("password", password);
        args.putBoolean("update", cbUpdate.isChecked());
        args.putBoolean("check", check);
        args.putParcelable("best", bestProvider);

        new SimpleTask<EmailProvider>() {
            @Override
            protected void onPreExecute(Bundle args) {
                boolean check = args.getBoolean("check");

                Helper.setViewsEnabled(view, false);
                pbCheck.setVisibility(check ? View.VISIBLE : View.GONE);
                tvPatience.setVisibility(check ? View.VISIBLE : View.GONE);
                pbSave.setVisibility(check ? View.GONE : View.VISIBLE);
                grpError.setVisibility(View.GONE);
                grpManual.setVisibility(View.GONE);
                tvArgument.setVisibility(View.GONE);
                tvErrorHint.setVisibility(View.GONE);
                tvInstructions.setVisibility(View.GONE);
                btnHelp.setVisibility(View.GONE);
                cbUpdate.setVisibility(check ? View.GONE : View.VISIBLE);
                btnSave.setVisibility(check ? View.GONE : View.VISIBLE);
                grpSetup.setVisibility(check ? View.GONE : View.VISIBLE);
                if (check)
                    grpCertificate.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                Helper.setViewsEnabled(view, true);
                pbCheck.setVisibility(View.GONE);
                tvPatience.setVisibility(View.GONE);
                tvProgress.setVisibility(View.GONE);
                pbSave.setVisibility(View.GONE);
            }

            @Override
            protected EmailProvider onExecute(Context context, Bundle args) throws Throwable {
                String name = args.getString("name");
                String email = args.getString("email");
                String password = args.getString("password");
                boolean check = args.getBoolean("check");
                EmailProvider best = args.getParcelable("best");

                int at = email.indexOf('@');
                String username = email.substring(0, at);

                Throwable fail = null;
                List<EmailProvider> providers;
                if (best == null)
                    providers = EmailProvider.fromEmail(context, email, EmailProvider.Discover.ALL,
                            new EmailProvider.IDiscovery() {
                                @Override
                                public void onStatus(String status) {
                                    postProgress(status);
                                }
                            });
                else
                    providers = Arrays.asList(best);
                for (EmailProvider provider : providers)
                    try {
                        EntityLog.log(context, "Checking" +
                                " imap=" + provider.imap + " smtp=" + provider.smtp);
                        postProgress(provider.imap + "/" + provider.smtp);

                        if (fail == null)
                            args.putParcelable("provider", provider);

                        List<String> users;
                        if (provider.user == EmailProvider.UserType.LOCAL)
                            users = Arrays.asList(username, email);
                        else if (provider.user == EmailProvider.UserType.VALUE) {
                            String user = provider.username;
                            if (user.startsWith("*@"))
                                user = username + user.substring(1);
                            users = Arrays.asList(user, email, username);
                        } else
                            users = Arrays.asList(email, username);
                        Log.i("User type=" + provider.user +
                                " users=" + TextUtils.join(", ", users));

                        List<EntityFolder> folders;
                        String imap_fingerprint = null;
                        String smtp_fingerprint = null;
                        X509Certificate imap_certificate = null;
                        X509Certificate smtp_certificate = null;

                        String user = null;
                        String aprotocol = (provider.imap.starttls ? "imap" : "imaps");
                        int aencryption = (provider.imap.starttls ? EmailService.ENCRYPTION_STARTTLS : EmailService.ENCRYPTION_SSL);
                        try (EmailService iservice = new EmailService(context,
                                aprotocol, null, aencryption, false, false, false,
                                EmailService.PURPOSE_CHECK, true)) {
                            List<Throwable> exceptions = new ArrayList<>();
                            for (int i = 0; i < users.size(); i++) {
                                user = users.get(i);
                                Log.i("Trying with user=" + user);
                                try {
                                    iservice.connect(
                                            false, provider.imap.host, provider.imap.port,
                                            AUTH_TYPE_PASSWORD, null,
                                            user, password,
                                            null, null);
                                    break;
                                } catch (EmailService.UntrustedException ex) {
                                    imap_certificate = ex.getCertificate();
                                    imap_fingerprint = EntityCertificate.getKeyFingerprint(imap_certificate);
                                    iservice.connect(
                                            false, provider.imap.host, provider.imap.port,
                                            AUTH_TYPE_PASSWORD, null,
                                            user, password,
                                            null, imap_fingerprint);
                                    break;
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                    // Why not AuthenticationFailedException?
                                    // Some providers terminate the connection with an invalid username
                                    exceptions.add(ex);
                                    if (i + 1 == users.size()) {
                                        for (Throwable e : exceptions)
                                            if (e instanceof AuthenticationFailedException)
                                                throw ex;
                                        throw exceptions.get(0);
                                    }
                                }
                            }

                            folders = iservice.getFolders(provider.imap.host);

                            if (!check) {
                                boolean drafts = false;
                                boolean sent = false;
                                boolean archive = false;
                                boolean trash = false;
                                boolean junk = false;
                                boolean other = false;
                                for (EntityFolder folder : folders)
                                    switch (folder.type) {
                                        case EntityFolder.INBOX:
                                            break;
                                        case EntityFolder.DRAFTS:
                                            drafts = true;
                                            break;
                                        case EntityFolder.SENT:
                                            sent = true;
                                            break;
                                        case EntityFolder.ARCHIVE:
                                            archive = true;
                                            break;
                                        case EntityFolder.TRASH:
                                            trash = true;
                                            break;
                                        case EntityFolder.JUNK:
                                            junk = true;
                                            break;
                                        default:
                                            other = true;
                                            break;
                                    }

                                if (!other && !(drafts && sent && archive && trash && junk))
                                    try {
                                        Store istore = iservice.getStore();

                                        String n = "";
                                        Folder[] ns = istore.getPersonalNamespaces();
                                        if (ns != null && ns.length == 1) {
                                            n = ns[0].getFullName();
                                            if (!TextUtils.isEmpty(n))
                                                n += ns[0].getSeparator();
                                        }

                                        Log.i("Creating system folders" +
                                                " namespace=" + n +
                                                " drafts=" + drafts +
                                                " sent=" + sent +
                                                " archive=" + archive +
                                                " trash=" + trash +
                                                " junk=" + junk);

                                        if (!drafts)
                                            istore.getFolder(n + EntityFolder.DRAFTS).create(Folder.HOLDS_MESSAGES);
                                        if (!sent)
                                            istore.getFolder(n + EntityFolder.SENT).create(Folder.HOLDS_MESSAGES);
                                        if (!archive)
                                            istore.getFolder(n + EntityFolder.ARCHIVE).create(Folder.HOLDS_MESSAGES);
                                        if (!trash)
                                            istore.getFolder(n + EntityFolder.TRASH).create(Folder.HOLDS_MESSAGES);
                                        if (!junk)
                                            istore.getFolder(n + EntityFolder.JUNK).create(Folder.HOLDS_MESSAGES);

                                        folders = iservice.getFolders(provider.imap.host);
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                            }
                        }

                        Long max_size;
                        String iprotocol = (provider.smtp.starttls ? "smtp" : "smtps");
                        int iencryption = (provider.smtp.starttls ? EmailService.ENCRYPTION_STARTTLS : EmailService.ENCRYPTION_SSL);
                        try (EmailService iservice = new EmailService(context,
                                iprotocol, null, iencryption, false, false, false,
                                EmailService.PURPOSE_CHECK, true)) {
                            iservice.setUseIp(provider.useip, null);
                            try {
                                iservice.connect(
                                        false, provider.smtp.host, provider.smtp.port,
                                        AUTH_TYPE_PASSWORD, null,
                                        user, password,
                                        null, null);
                            } catch (EmailService.UntrustedException ex) {
                                smtp_certificate = ex.getCertificate();
                                smtp_fingerprint = EntityCertificate.getKeyFingerprint(smtp_certificate);
                                iservice.connect(
                                        false, provider.smtp.host, provider.smtp.port,
                                        AUTH_TYPE_PASSWORD, null,
                                        user, password,
                                        null, smtp_fingerprint);
                            }

                            max_size = iservice.getMaxSize();
                        }

                        if (check) {
                            args.putString("user", user);
                            args.putSerializable("imap_certificate", imap_certificate);
                            args.putSerializable("smtp_certificate", smtp_certificate);
                            return provider;
                        }

                        EntityAccount update = null;
                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            EntityAccount primary = db.account().getPrimaryAccount();

                            if (args.getBoolean("update")) {
                                List<EntityAccount> accounts = db.account().getAccounts(user, EntityAccount.TYPE_IMAP);
                                if (accounts != null && accounts.size() == 1)
                                    update = accounts.get(0);
                            }

                            if (update == null) {
                                // Create account
                                EntityAccount account = new EntityAccount();

                                account.host = provider.imap.host;
                                account.encryption = aencryption;
                                account.insecure = (BuildConfig.PLAY_STORE_RELEASE && !provider.imap.isSecure());
                                account.port = provider.imap.port;
                                account.auth_type = AUTH_TYPE_PASSWORD;
                                account.user = user;
                                account.password = password;
                                account.fingerprint = imap_fingerprint;

                                account.name = provider.name + "/" + username;

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
                                EntityLog.log(context, "Quick added account=" + account.name);

                                // Create folders
                                for (EntityFolder folder : folders) {
                                    EntityFolder existing = db.folder().getFolderByName(account.id, folder.name);
                                    if (existing == null) {
                                        folder.account = account.id;
                                        folder.setSpecials(account);
                                        folder.id = db.folder().insertFolder(folder);
                                        EntityLog.log(context, "Quick added folder=" + folder.name + " type=" + folder.type);
                                        if (folder.synchronize)
                                            EntityOperation.sync(context, folder.id, true);
                                    }
                                }

                                // Set swipe left/right folder
                                FragmentDialogSwipes.setDefaultFolderActions(context, account);

                                // Create identity
                                EntityIdentity identity = new EntityIdentity();
                                identity.name = name;
                                identity.email = email;
                                identity.account = account.id;

                                identity.host = provider.smtp.host;
                                identity.encryption = iencryption;
                                identity.insecure = (BuildConfig.PLAY_STORE_RELEASE && !provider.smtp.isSecure());
                                identity.port = provider.smtp.port;
                                identity.auth_type = AUTH_TYPE_PASSWORD;
                                identity.user = user;
                                identity.password = password;
                                identity.fingerprint = smtp_fingerprint;

                                identity.use_ip = provider.useip;
                                identity.synchronize = true;
                                identity.primary = true;
                                identity.max_size = max_size;

                                identity.id = db.identity().insertIdentity(identity);
                                EntityLog.log(context, "Quick added identity=" + identity.name + " email=" + identity.email);
                            } else {
                                args.putLong("account", update.id);
                                EntityLog.log(context, "Quick setup update account=" + update.name);

                                db.account().setAccountSynchronize(update.id, true);
                                db.account().setAccountPassword(update.id, password, AUTH_TYPE_PASSWORD, null);
                                db.account().setAccountFingerprint(update.id, imap_fingerprint,
                                        BuildConfig.PLAY_STORE_RELEASE && !TextUtils.isEmpty(imap_fingerprint));

                                db.identity().setIdentityPassword(update.id, update.user, password, update.auth_type, AUTH_TYPE_PASSWORD, null);
                                db.identity().setIdentityFingerprint(update.id, smtp_fingerprint,
                                        BuildConfig.PLAY_STORE_RELEASE && !TextUtils.isEmpty(smtp_fingerprint));
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        ServiceSynchronize.eval(context, "quick setup");
                        args.putBoolean("updated", update != null);

                        FairEmailBackupAgent.dataChanged(context);

                        return provider;
                    } catch (Throwable ex) {
                        Log.w(ex);
                        if (fail == null)
                            fail = ex;
                    }

                if (fail != null)
                    throw fail;

                return null;
            }

            @Override
            protected void onProgress(CharSequence status, Bundle data) {
                tvProgress.setText(status);
                tvProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onExecuted(Bundle args, EmailProvider result) {
                setManual(false);

                boolean check = args.getBoolean("check");
                if (check) {
                    bestProvider = result;
                    bestArgs = args;
                    showResult(bestProvider, bestArgs);
                } else {
                    boolean updated = args.getBoolean("updated");
                    if (updated) {
                        finish();
                        ToastEx.makeText(getContext(), R.string.title_setup_oauth_updated, Toast.LENGTH_LONG).show();
                    } else {
                        FragmentDialogAccount fragment = new FragmentDialogAccount();
                        fragment.setArguments(args);
                        fragment.setTargetFragment(FragmentQuickSetup.this, ActivitySetup.REQUEST_DONE);
                        fragment.show(getParentFragmentManager(), "quick:review");
                    }
                }
            }

            @Override
            protected void onException(final Bundle args, Throwable ex) {
                Log.e(ex);
                setManual(true);
                EmailProvider provider = args.getParcelable("provider");

                etName.clearFocus();
                etEmail.clearFocus();
                Helper.hideKeyboard(view);

                if (ex instanceof AuthenticationFailedException) {
                    String message;
                    if (provider != null && provider.imap != null &&
                            ("outlook.office365.com".equals(provider.imap.host) ||
                                    "imap-mail.outlook.com".equals(provider.imap.host)))
                        message = getString(R.string.title_setup_no_auth_outlook);
                    else
                        message = getString(R.string.title_setup_no_auth_hint);
                    if (provider != null && provider.appPassword)
                        message += "\n\n" + getString(R.string.title_setup_app_password_hint);
                    tvErrorHint.setText(message);
                    tvErrorHint.setVisibility(View.VISIBLE);
                    if (provider == null)
                        grpManual.setVisibility(View.VISIBLE);
                } else
                    grpManual.setVisibility(View.VISIBLE);

                if (ex instanceof IllegalArgumentException || ex instanceof UnknownHostException) {
                    tvError.setText(new ThrowableWrapper(ex).getSafeMessage());
                    grpError.setVisibility(View.VISIBLE);

                    getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                return;
                            scroll.smoothScrollTo(0, btnSupport.getBottom());
                        }
                    });
                } else {
                    tvError.setText(Log.formatThrowable(ex, false));
                    grpError.setVisibility(View.VISIBLE);

                    if (provider != null && provider.link != null) {
                        Uri uri = Uri.parse(provider.link);
                        btnHelp.setTag(uri);
                        btnHelp.setVisibility(View.VISIBLE);
                    }

                    if (provider != null && provider.documentation != null) {
                        tvInstructions.setText(HtmlHelper.fromHtml(provider.documentation.toString(), getContext()));
                        tvInstructions.setVisibility(View.VISIBLE);
                    }

                    if (provider != null &&
                            provider.imap != null && provider.smtp != null) {
                        tvUser.setText(TextUtils.isEmpty(provider.username) ? "-" : provider.username);
                        tvImap.setText(provider.imap.toString());
                        tvSmtp.setText(provider.smtp.toString());
                        grpSetup.setVisibility(View.VISIBLE);
                        grpCertificate.setVisibility(View.GONE);
                    }

                    getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                return;
                            scroll.smoothScrollTo(0, btnSupport.getBottom());
                        }
                    });
                }
            }

            private void setManual(boolean manual) {
                FragmentActivity activity = getActivity();
                if (activity == null)
                    return;

                Intent intent = activity.getIntent();
                if (intent == null)
                    return;

                intent.putExtra("manual", manual);
            }
        }.execute(this, args, "setup:quick");
    }

    private void showResult(EmailProvider provider, Bundle args) {
        X509Certificate imap_certificate = (args == null ? null
                : (X509Certificate) args.getSerializable("imap_certificate"));
        X509Certificate smtp_certificate = (args == null ? null
                : (X509Certificate) args.getSerializable("smtp_certificate"));

        List<String> imapNames = new ArrayList<>();
        if (imap_certificate != null)
            try {
                imapNames = EntityCertificate.getDnsNames(imap_certificate);
            } catch (Throwable ignored) {
            }
        boolean imapMatches = (provider != null &&
                EntityCertificate.matches(provider.imap.host, imapNames));

        List<String> smtpNames = new ArrayList<>();
        if (smtp_certificate != null)
            try {
                smtpNames = EntityCertificate.getDnsNames(smtp_certificate);
            } catch (Throwable ignored) {
            }
        boolean smtpMatches = (provider != null &&
                EntityCertificate.matches(provider.imap.host, smtpNames));

        tvUser.setText((args == null ? null : args.getString("user")));
        tvImap.setText(provider == null ? null : provider.imap.toString());
        tvSmtp.setText(provider == null ? null : provider.smtp.toString());
        grpSetup.setVisibility(provider == null ? View.GONE : View.VISIBLE);

        tvImapFingerprint.setText(EntityCertificate.getKeyFingerprint(imap_certificate));
        tvImapDnsNames.setText(TextUtils.join(", ", imapNames));
        tvImapDnsNames.setTypeface(imapMatches ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);
        tvSmtpFingerprint.setText(EntityCertificate.getKeyFingerprint(smtp_certificate));
        tvSmtpDnsNames.setText(TextUtils.join(", ", smtpNames));
        tvSmtpDnsNames.setTypeface(smtpMatches ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);

        grpCertificate.setVisibility(
                imap_certificate == null && smtp_certificate == null
                        ? View.GONE : View.VISIBLE);

        cbUpdate.setVisibility(provider == null ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(provider == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case ActivitySetup.REQUEST_DONE:
                    finish();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }
}
