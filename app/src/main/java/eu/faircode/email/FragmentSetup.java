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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Transport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import static android.accounts.AccountManager.newChooseAccountIntent;
import static android.app.Activity.RESULT_OK;

public class FragmentSetup extends FragmentEx {
    private ViewGroup view;

    private EditText etName;
    private EditText etEmail;
    private Button btnAuthorize;
    private TextInputLayout tilPassword;
    private Button btnQuick;
    private TextView tvQuickError;
    private Group grpQuickError;
    private TextView tvInstructions;

    private Button btnAccount;
    private TextView tvAccountDone;
    private TextView tvNoPrimaryDrafts;

    private Button btnIdentity;
    private TextView tvIdentityDone;

    private Button btnPermissions;
    private TextView tvPermissionsDone;

    private Button btnDoze;
    private TextView tvDozeDone;

    private Button btnData;

    private Button btnNotifications;

    private ToggleButton tbDarkTheme;
    private CheckBox cbBlackTheme;

    private Button btnOptions;

    private Drawable check;

    private int auth_type = Helper.AUTH_TYPE_PASSWORD;

    private static final String[] permissions = new String[]{
            Manifest.permission.READ_CONTACTS
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);

        check = getResources().getDrawable(R.drawable.baseline_check_24, getContext().getTheme());

        view = (ViewGroup) inflater.inflate(R.layout.fragment_setup, container, false);

        // Get controls
        etName = view.findViewById(R.id.etName);
        btnAuthorize = view.findViewById(R.id.btnAuthorize);
        etEmail = view.findViewById(R.id.etEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        btnQuick = view.findViewById(R.id.btnQuick);
        tvQuickError = view.findViewById(R.id.tvQuickError);
        grpQuickError = view.findViewById(R.id.grpQuickError);
        tvInstructions = view.findViewById(R.id.tvInstructions);

        btnAccount = view.findViewById(R.id.btnAccount);
        tvAccountDone = view.findViewById(R.id.tvAccountDone);
        tvNoPrimaryDrafts = view.findViewById(R.id.tvNoPrimaryDrafts);

        btnIdentity = view.findViewById(R.id.btnIdentity);
        tvIdentityDone = view.findViewById(R.id.tvIdentityDone);

        btnPermissions = view.findViewById(R.id.btnPermissions);
        tvPermissionsDone = view.findViewById(R.id.tvPermissionsDone);

        btnDoze = view.findViewById(R.id.btnDoze);
        tvDozeDone = view.findViewById(R.id.tvDozeDone);

        btnNotifications = view.findViewById(R.id.btnNotifications);

        btnData = view.findViewById(R.id.btnData);

        tbDarkTheme = view.findViewById(R.id.tbDarkTheme);
        cbBlackTheme = view.findViewById(R.id.cbBlackTheme);
        btnOptions = view.findViewById(R.id.btnOptions);

        // Wire controls

        btnAuthorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String permission = Manifest.permission.GET_ACCOUNTS;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O &&
                        ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("Requesting " + permission);
                    requestPermissions(new String[]{permission}, ActivitySetup.REQUEST_CHOOSE_ACCOUNT);
                } else
                    selectAccount();
            }
        });

        TextWatcher credentialsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                auth_type = Helper.AUTH_TYPE_PASSWORD;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etEmail.addTextChangedListener(credentialsWatcher);
        tilPassword.getEditText().addTextChangedListener(credentialsWatcher);

        tilPassword.setHintEnabled(false);

        btnQuick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("name", etName.getText().toString());
                args.putString("email", etEmail.getText().toString().trim());
                args.putString("password", tilPassword.getEditText().getText().toString());
                args.putInt("auth_type", auth_type);

                new SimpleTask<Void>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        etName.setEnabled(false);
                        etEmail.setEnabled(false);
                        tilPassword.setEnabled(false);
                        btnAuthorize.setEnabled(false);
                        btnQuick.setEnabled(false);
                        grpQuickError.setVisibility(View.GONE);
                        tvInstructions.setVisibility(View.GONE);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        etName.setEnabled(true);
                        etEmail.setEnabled(true);
                        tilPassword.setEnabled(true);
                        btnAuthorize.setEnabled(true);
                        btnQuick.setEnabled(true);
                    }

                    @Override
                    protected Void onExecute(Context context, Bundle args) throws Throwable {
                        String name = args.getString("name");
                        String email = args.getString("email");
                        String password = args.getString("password");
                        int auth_type = args.getInt("auth_type");

                        if (TextUtils.isEmpty(name))
                            throw new IllegalArgumentException(context.getString(R.string.title_no_name));
                        if (TextUtils.isEmpty(email))
                            throw new IllegalArgumentException(context.getString(R.string.title_no_email));
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                            throw new IllegalArgumentException(context.getString(R.string.title_email_invalid));

                        String[] dparts = email.split("@");
                        EmailProvider provider = EmailProvider.fromDomain(context, dparts[1]);

                        if (provider.documentation != null)
                            args.putString("documentation", provider.documentation.toString());

                        String user = (provider.user == EmailProvider.UserType.EMAIL ? email : dparts[0]);

                        Character separator;
                        long now = new Date().getTime();

                        List<EntityFolder> folders = new ArrayList<>();

                        EntityFolder inbox = new EntityFolder();
                        inbox.name = "INBOX";
                        inbox.type = EntityFolder.INBOX;
                        inbox.level = 0;
                        inbox.synchronize = true;
                        inbox.unified = true;
                        inbox.notify = true;
                        inbox.sync_days = EntityFolder.DEFAULT_SYNC;
                        inbox.keep_days = EntityFolder.DEFAULT_KEEP;
                        folders.add(inbox);

                        {
                            Properties props = MessageHelper.getSessionProperties(auth_type, null, false);
                            Session isession = Session.getInstance(props, null);
                            isession.setDebug(true);
                            IMAPStore istore = null;
                            try {
                                istore = (IMAPStore) isession.getStore(provider.imap_starttls ? "imap" : "imaps");
                                istore.connect(provider.imap_host, provider.imap_port, user, password);

                                separator = istore.getDefaultFolder().getSeparator();

                                boolean drafts = false;
                                for (Folder ifolder : istore.getDefaultFolder().list("*")) {
                                    String type = null;
                                    boolean selectable = true;
                                    String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                                    Log.i(ifolder.getFullName() + " attrs=" + TextUtils.join(" ", attrs));
                                    for (String attr : attrs) {
                                        if ("\\Noselect".equals(attr) || "\\NonExistent".equals(attr))
                                            selectable = false;
                                        if (attr.startsWith("\\")) {
                                            int index = EntityFolder.SYSTEM_FOLDER_ATTR.indexOf(attr.substring(1));
                                            if (index >= 0) {
                                                type = EntityFolder.SYSTEM_FOLDER_TYPE.get(index);
                                                break;
                                            }
                                        }
                                    }

                                    if (selectable && type != null) {
                                        int sync = EntityFolder.SYSTEM_FOLDER_SYNC.indexOf(type);
                                        EntityFolder folder = new EntityFolder();
                                        folder.name = ifolder.getFullName();
                                        folder.type = type;
                                        folder.level = EntityFolder.getLevel(separator, folder.name);
                                        folder.synchronize = (sync >= 0);
                                        folder.download = (sync < 0 ? true : EntityFolder.SYSTEM_FOLDER_DOWNLOAD.get(sync));
                                        folder.sync_days = EntityFolder.DEFAULT_SYNC;
                                        folder.keep_days = EntityFolder.DEFAULT_KEEP;
                                        folders.add(folder);

                                        if (EntityFolder.DRAFTS.equals(type))
                                            drafts = true;
                                    }
                                }

                                if (!drafts)
                                    throw new IllegalArgumentException(
                                            context.getString(R.string.title_setup_no_settings, dparts[1]));
                            } finally {
                                if (istore != null)
                                    istore.close();
                            }
                        }

                        {
                            Properties props = MessageHelper.getSessionProperties(auth_type, null, false);
                            Session isession = Session.getInstance(props, null);
                            isession.setDebug(true);
                            Transport itransport = isession.getTransport(provider.smtp_starttls ? "smtp" : "smtps");
                            try {
                                itransport.connect(provider.smtp_host, provider.smtp_port, user, password);
                            } finally {
                                itransport.close();
                            }
                        }

                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();
                            EntityAccount primary = db.account().getPrimaryAccount();

                            // Create account
                            EntityAccount account = new EntityAccount();

                            account.auth_type = auth_type;
                            account.host = provider.imap_host;
                            account.starttls = provider.imap_starttls;
                            account.insecure = false;
                            account.port = provider.imap_port;
                            account.user = user;
                            account.password = password;

                            account.name = provider.name;
                            account.color = null;

                            account.synchronize = true;
                            account.primary = (primary == null);
                            account.notify = false;
                            account.browse = true;
                            account.poll_interval = 19;
                            account.prefix = provider.prefix;

                            account.created = now;
                            account.error = null;
                            account.last_connected = now;

                            account.id = db.account().insertAccount(account);

                            // Create folders
                            for (EntityFolder folder : folders) {
                                folder.account = account.id;
                                folder.id = db.folder().insertFolder(folder);
                            }

                            // Create identity
                            EntityIdentity identity = new EntityIdentity();
                            identity.name = name;
                            identity.email = email;
                            identity.account = account.id;

                            identity.display = null;
                            identity.color = null;
                            identity.signature = null;

                            identity.auth_type = auth_type;
                            identity.host = provider.smtp_host;
                            identity.starttls = provider.smtp_starttls;
                            identity.insecure = false;
                            identity.port = provider.smtp_port;
                            identity.user = user;
                            identity.password = password;
                            identity.synchronize = true;
                            identity.primary = true;

                            identity.replyto = null;
                            identity.bcc = null;
                            identity.delivery_receipt = false;
                            identity.read_receipt = false;
                            identity.store_sent = false;
                            identity.sent_folder = null;
                            identity.error = null;

                            identity.id = db.identity().insertIdentity(identity);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        ServiceSynchronize.reload(context, "quick setup");

                        return null;
                    }

                    @Override
                    protected void onExecuted(Bundle args, Void data) {
                        etName.setText(null);
                        etEmail.setText(null);
                        tilPassword.getEditText().setText(null);

                        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                                .setMessage(R.string.title_setup_quick_success)
                                .setPositiveButton(android.R.string.ok, null)
                                .setNeutralButton(R.string.title_folder_inbox, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .create()
                                .show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        if (args.containsKey("documentation")) {
                            tvInstructions.setText(Html.fromHtml(args.getString("documentation")));
                            tvInstructions.setVisibility(View.VISIBLE);
                        }

                        if (ex instanceof IllegalArgumentException)
                            Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                        else {
                            tvQuickError.setText(Helper.formatThrowable(ex));
                            grpQuickError.setVisibility(View.VISIBLE);
                        }
                    }
                }.execute(FragmentSetup.this, args, "setup:quick");
            }
        });

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentAccounts()).addToBackStack("accounts");
                fragmentTransaction.commit();
            }
        });

        btnIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentIdentities()).addToBackStack("identities");
                fragmentTransaction.commit();
            }
        });

        btnPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPermissions.setEnabled(false);
                requestPermissions(permissions, ActivitySetup.REQUEST_PERMISSION);
            }
        });

        btnDoze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                        .setMessage(R.string.title_setup_doze_instructions)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }
                            }
                        })
                        .create()
                        .show();
            }
        });

        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.N)
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });

        PackageManager pm = getContext().getPackageManager();
        btnNotifications.setVisibility(getIntentNotifications(getContext()).resolveActivity(pm) == null ? View.GONE : View.VISIBLE);
        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(getIntentNotifications(getContext()));
            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String theme = prefs.getString("theme", "light");
        boolean light = "light".equals(theme);
        tbDarkTheme.setTag(!light);
        tbDarkTheme.setChecked(!light);
        tbDarkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                if (Helper.isPro(getContext())) {
                    if (checked != (Boolean) button.getTag()) {
                        button.setTag(checked);
                        tbDarkTheme.setChecked(checked);
                        prefs.edit().putString("theme", checked ? "dark" : "light").apply();
                    }
                } else {
                    prefs.edit().remove("theme").apply();
                    if (checked) {
                        tbDarkTheme.setChecked(false);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                        fragmentTransaction.commit();
                    }
                }
                cbBlackTheme.setVisibility(tbDarkTheme.isChecked() ? View.VISIBLE : View.GONE);
            }
        });

        cbBlackTheme.setChecked("black".equals(theme));
        cbBlackTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putString("theme", checked ? "black" : "dark").apply();
            }
        });
        cbBlackTheme.setVisibility(tbDarkTheme.isChecked() ? View.VISIBLE : View.GONE);

        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentOptions()).addToBackStack("options");
                fragmentTransaction.commit();
            }
        });

        // Initialize
        grpQuickError.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);
        tvInstructions.setMovementMethod(LinkMovementMethod.getInstance());

        tvAccountDone.setText(null);
        tvAccountDone.setCompoundDrawables(null, null, null, null);
        tvNoPrimaryDrafts.setVisibility(View.GONE);

        btnIdentity.setEnabled(false);
        tvIdentityDone.setText(null);
        tvIdentityDone.setCompoundDrawables(null, null, null, null);

        tvPermissionsDone.setText(null);
        tvPermissionsDone.setCompoundDrawables(null, null, null, null);

        btnDoze.setEnabled(false);
        tvDozeDone.setText(null);
        tvDozeDone.setCompoundDrawables(null, null, null, null);

        btnData.setVisibility(View.GONE);

        int[] grantResults = new int[permissions.length];
        for (int i = 0; i < permissions.length; i++)
            grantResults[i] = ContextCompat.checkSelfPermission(getActivity(), permissions[i]);

        checkPermissions(permissions, grantResults, true);

        // Create outbox
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder outbox = db.folder().getOutbox();
                    if (outbox == null) {
                        outbox = new EntityFolder();
                        outbox.name = "OUTBOX";
                        outbox.type = EntityFolder.OUTBOX;
                        outbox.level = 0;
                        outbox.synchronize = false;
                        outbox.sync_days = 0;
                        outbox.keep_days = 0;
                        outbox.id = db.folder().insertFolder(outbox);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, new Bundle(), "outbox:create");

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DB db = DB.getInstance(getContext());

        db.account().liveAccounts(true).observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
            private boolean done = false;
            private LiveData<EntityFolder> livePrimaryDrafts = null;
            private LiveData<EntityFolder> livePrimaryArchive = null;

            @Override
            public void onChanged(@Nullable List<EntityAccount> accounts) {
                done = (accounts != null && accounts.size() > 0);

                btnIdentity.setEnabled(done);
                tvAccountDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvAccountDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);

                if (livePrimaryDrafts == null)
                    livePrimaryDrafts = db.folder().livePrimaryDrafts();
                else
                    livePrimaryDrafts.removeObservers(getViewLifecycleOwner());

                if (livePrimaryArchive == null)
                    livePrimaryArchive = db.folder().livePrimaryArchive();
                else
                    livePrimaryArchive.removeObservers(getViewLifecycleOwner());

                livePrimaryDrafts.observe(getViewLifecycleOwner(), new Observer<EntityFolder>() {
                    @Override
                    public void onChanged(EntityFolder drafts) {
                        tvNoPrimaryDrafts.setVisibility(done && drafts == null ? View.VISIBLE : View.GONE);
                    }
                });

                livePrimaryArchive.observe(getViewLifecycleOwner(), new Observer<EntityFolder>() {
                    @Override
                    public void onChanged(EntityFolder archive) {
                        PackageManager pm = getContext().getPackageManager();
                        pm.setComponentEnabledSetting(
                                new ComponentName(getContext(), ActivitySearch.class),
                                archive == null
                                        ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                                        : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);
                    }
                });
            }
        });

        db.identity().liveIdentities(null, true).observe(getViewLifecycleOwner(), new Observer<List<EntityIdentity>>() {
            @Override
            public void onChanged(@Nullable List<EntityIdentity> identities) {
                boolean done = (identities != null && identities.size() > 0);
                tvIdentityDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvIdentityDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean ignoring = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                ignoring = pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID);
            }
        }
        btnDoze.setEnabled(!ignoring);
        tvDozeDone.setText(ignoring ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvDozeDone.setCompoundDrawablesWithIntrinsicBounds(ignoring ? check : null, null, null, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean saving = (cm.getRestrictBackgroundStatus() == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED);
            btnData.setVisibility(saving ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ActivitySetup.REQUEST_PERMISSION)
            checkPermissions(permissions, grantResults, false);
        else if (requestCode == ActivitySetup.REQUEST_CHOOSE_ACCOUNT)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectAccount();
    }

    private void checkPermissions(String[] permissions, @NonNull int[] grantResults, boolean init) {
        boolean has = (grantResults.length > 0);
        for (int result : grantResults)
            if (result != PackageManager.PERMISSION_GRANTED) {
                has = false;
                break;
            }

        btnPermissions.setEnabled(!has);
        tvPermissionsDone.setText(has ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvPermissionsDone.setCompoundDrawablesWithIntrinsicBounds(has ? check : null, null, null, null);

        if (has && !init)
            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(context);
                    for (EntityFolder folder : db.folder().getFoldersSynchronizing())
                        EntityOperation.sync(db, folder.id);
                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                }
            }.execute(FragmentSetup.this, new Bundle(), "setup:sync");
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        if (requestCode == ActivitySetup.REQUEST_CHOOSE_ACCOUNT) {
            if (resultCode == RESULT_OK && data != null)
                accountSelected(data);
        }
    }

    private void accountSelected(Intent data) {
        String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccountsByType(type);
        Log.i("Accounts=" + accounts.length);
        for (final Account account : accounts)
            if (name.equals(account.name)) {
                etEmail.setEnabled(false);
                tilPassword.setEnabled(false);
                btnAuthorize.setEnabled(false);
                btnQuick.setEnabled(false);
                final Snackbar snackbar = Snackbar.make(view, R.string.title_authorizing, Snackbar.LENGTH_SHORT);
                snackbar.show();

                am.getAuthToken(
                        account,
                        Helper.getAuthTokenType(type),
                        new Bundle(),
                        getActivity(),
                        new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                try {
                                    Bundle bundle = future.getResult();
                                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                                    Log.i("Got token");

                                    etEmail.setText(account.name);
                                    tilPassword.getEditText().setText(token);
                                    auth_type = Helper.AUTH_TYPE_GMAIL;
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                    if (ex instanceof OperationCanceledException ||
                                            ex instanceof AuthenticatorException ||
                                            ex instanceof IOException)
                                        Snackbar.make(view, Helper.formatThrowable(ex), Snackbar.LENGTH_LONG).show();
                                    else
                                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                                } finally {
                                    etEmail.setEnabled(true);
                                    tilPassword.setEnabled(true);
                                    btnAuthorize.setEnabled(true);
                                    btnQuick.setEnabled(true);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            snackbar.dismiss();
                                        }
                                    }, 1000);
                                }
                            }
                        },
                        null);
                break;
            }
    }

    private static Intent getIntentNotifications(Context context) {
        return new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra("app_package", context.getPackageName())
                .putExtra("app_uid", context.getApplicationInfo().uid)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
    }

    private void selectAccount() {
        Log.i("Select account");
        startActivityForResult(
                Helper.getChooser(getContext(), newChooseAccountIntent(
                        null,
                        null,
                        new String[]{"com.google"},
                        false,
                        null,
                        null,
                        null,
                        null)),
                ActivitySetup.REQUEST_CHOOSE_ACCOUNT);
    }
}
