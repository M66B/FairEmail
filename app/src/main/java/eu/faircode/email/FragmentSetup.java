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
import android.accounts.AccountsException;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.accounts.AccountManager.newChooseAccountIntent;

public class FragmentSetup extends FragmentBase {
    private ViewGroup view;

    private TextView tvWelcome;
    private ImageButton ibWelcome;

    private Button btnHelp;
    private Button btnQuick;

    private TextView tvAccountDone;
    private Button btnAccount;
    private TextView tvNoPrimaryDrafts;

    private TextView tvIdentityDone;
    private Button btnIdentity;
    private TextView tvNoIdentities;

    private TextView tvPermissionsDone;
    private Button btnPermissions;

    private TextView tvDozeDone;
    private Button btnDoze;
    private Button btnBattery;

    private Button btnDataSaver;

    private Button btnInbox;

    private Group grpWelcome;
    private Group grpDataSaver;

    private int textColorPrimary;
    private int colorWarning;
    private Drawable check;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);

        textColorPrimary = Helper.resolveColor(getContext(), android.R.attr.textColorPrimary);
        colorWarning = Helper.resolveColor(getContext(), R.attr.colorWarning);
        check = getResources().getDrawable(R.drawable.baseline_check_24, getContext().getTheme());

        view = (ViewGroup) inflater.inflate(R.layout.fragment_setup, container, false);

        // Get controls
        tvWelcome = view.findViewById(R.id.tvWelcome);
        ibWelcome = view.findViewById(R.id.ibWelcome);

        btnHelp = view.findViewById(R.id.btnHelp);
        btnQuick = view.findViewById(R.id.btnQuick);

        tvAccountDone = view.findViewById(R.id.tvAccountDone);
        btnAccount = view.findViewById(R.id.btnAccount);
        tvNoPrimaryDrafts = view.findViewById(R.id.tvNoPrimaryDrafts);

        tvIdentityDone = view.findViewById(R.id.tvIdentityDone);
        btnIdentity = view.findViewById(R.id.btnIdentity);
        tvNoIdentities = view.findViewById(R.id.tvNoIdentities);

        tvPermissionsDone = view.findViewById(R.id.tvPermissionsDone);
        btnPermissions = view.findViewById(R.id.btnPermissions);

        tvDozeDone = view.findViewById(R.id.tvDozeDone);
        btnDoze = view.findViewById(R.id.btnDoze);
        btnBattery = view.findViewById(R.id.btnBattery);

        btnDataSaver = view.findViewById(R.id.btnDataSaver);

        btnInbox = view.findViewById(R.id.btnInbox);

        grpWelcome = view.findViewById(R.id.grpWelcome);
        grpDataSaver = view.findViewById(R.id.grpDataSaver);

        PackageManager pm = getContext().getPackageManager();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Wire controls

        tvWelcome.setText(getString(R.string.title_setup_welcome)
                .replaceAll("^\\s+", "").replaceAll("\\s+", " "));

        ibWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putBoolean("welcome", false).apply();
                grpWelcome.setVisibility(View.GONE);
            }
        });

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putString("name", "SETUP.md");
                FragmentDialogMarkdown fragment = new FragmentDialogMarkdown();
                fragment.setArguments(args);
                fragment.show(getChildFragmentManager(), "help");
            }
        });

        btnQuick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), btnQuick);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_setup_gmail, 1, R.string.title_setup_gmail)
                        .setEnabled(Helper.hasValidFingerprint(getContext()));
                popupMenu.getMenu().add(Menu.NONE, R.string.title_setup_other, 2, R.string.title_setup_other);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.string.title_setup_gmail:
                                onGmail();
                                return true;
                            case R.string.title_setup_other:
                                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_QUICK_SETUP));
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_VIEW_ACCOUNTS));
            }
        });

        btnIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_VIEW_IDENTITIES));
            }
        });

        btnPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPermissions.setEnabled(false);
                String permission = Manifest.permission.READ_CONTACTS;
                requestPermissions(new String[]{permission}, ActivitySetup.REQUEST_PERMISSION);
            }
        });

        btnDoze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentDialogDoze().show(getFragmentManager(), "setup:doze");
            }
        });

        btnBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(getContext(), 39);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            final Intent settings = new Intent(
                    Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID));

            btnDataSaver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(settings);
                }
            });
            btnDataSaver.setEnabled(settings.resolveActivity(pm) != null);
        }

        btnInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentBase) getParentFragment()).finish();
            }
        });

        // Initialize
        tvAccountDone.setText(null);
        tvAccountDone.setCompoundDrawables(null, null, null, null);
        tvNoPrimaryDrafts.setVisibility(View.GONE);

        tvIdentityDone.setText(null);
        tvIdentityDone.setCompoundDrawables(null, null, null, null);
        btnIdentity.setEnabled(false);
        tvNoIdentities.setVisibility(View.GONE);

        tvPermissionsDone.setText(null);
        tvPermissionsDone.setCompoundDrawables(null, null, null, null);

        tvDozeDone.setText(null);
        tvDozeDone.setCompoundDrawables(null, null, null, null);
        btnDoze.setEnabled(false);

        btnInbox.setEnabled(false);

        boolean welcome = prefs.getBoolean("welcome", true);
        grpWelcome.setVisibility(welcome ? View.VISIBLE : View.GONE);
        grpDataSaver.setVisibility(View.GONE);

        setContactsPermission(hasPermission(Manifest.permission.READ_CONTACTS));

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
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "outbox:create");

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DB db = DB.getInstance(getContext());
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        db.account().liveSynchronizingAccounts().observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
            private boolean done = false;

            @Override
            public void onChanged(@Nullable List<EntityAccount> accounts) {
                done = (accounts != null && accounts.size() > 0);

                getActivity().invalidateOptionsMenu();

                tvAccountDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvAccountDone.setTextColor(done ? textColorPrimary : colorWarning);
                tvAccountDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);

                btnIdentity.setEnabled(done);
                btnInbox.setEnabled(done);

                prefs.edit().putBoolean("has_accounts", done).apply();

                if (done)
                    new SimpleTask<EntityFolder>() {
                        @Override
                        protected EntityFolder onExecute(Context context, Bundle args) {
                            DB db = DB.getInstance(context);
                            return db.folder().getPrimaryDrafts();
                        }

                        @Override
                        protected void onExecuted(Bundle args, EntityFolder drafts) {
                            tvNoPrimaryDrafts.setVisibility(drafts == null ? View.VISIBLE : View.GONE);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getFragmentManager(), ex);
                        }
                    }.execute(FragmentSetup.this, new Bundle(), "setup:drafts");
            }
        });

        db.identity().liveIdentities(true).observe(getViewLifecycleOwner(), new Observer<List<TupleIdentityEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleIdentityEx> identities) {
                boolean done = (identities != null && identities.size() > 0);
                tvIdentityDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvIdentityDone.setTextColor(done ? textColorPrimary : colorWarning);
                tvIdentityDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);
                tvNoIdentities.setVisibility(done ? View.GONE : View.VISIBLE);
            }
        });

        // Backward compatibility
        PackageManager pm = getContext().getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(getContext(), ActivitySearch.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Doze
        boolean ignoring = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                ignoring = (pm != null && pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID));
            }
        }

        btnDoze.setEnabled(!ignoring);

        // https://issuetracker.google.com/issues/37070074
        //ignoring = (ignoring || Build.VERSION.SDK_INT != Build.VERSION_CODES.M);

        tvDozeDone.setText(ignoring ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvDozeDone.setTextColor(ignoring ? textColorPrimary : colorWarning);
        tvDozeDone.setCompoundDrawablesWithIntrinsicBounds(ignoring ? check : null, null, null, null);

        // https://developer.android.com/training/basics/network-ops/data-saver.html
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                int status = cm.getRestrictBackgroundStatus();
                grpDataSaver.setVisibility(
                        status == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
                                ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        for (int i = 0; i < permissions.length; i++)
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                if (Manifest.permission.READ_CONTACTS.equals(permissions[i]))
                    setContactsPermission(true);
            } else
                granted = false;

        if (requestCode == ActivitySetup.REQUEST_CHOOSE_ACCOUNT)
            if (granted)
                selectAccount();
    }

    private void setContactsPermission(boolean granted) {
        if (granted)
            ContactInfo.init(getContext());

        tvPermissionsDone.setText(granted ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvPermissionsDone.setTextColor(granted ? textColorPrimary : colorWarning);
        tvPermissionsDone.setCompoundDrawablesWithIntrinsicBounds(granted ? check : null, null, null, null);
        btnPermissions.setEnabled(!granted);
    }

    private void onGmail() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_CONTACTS); // profile
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            permissions.add(Manifest.permission.GET_ACCOUNTS);

        boolean granted = true;
        for (String permission : permissions)
            if (!hasPermission(permission)) {
                granted = false;
                break;
            }

        if (granted)
            selectAccount();
        else
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.title_setup_gmail_rationale)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(permissions.toArray(new String[0]), ActivitySetup.REQUEST_CHOOSE_ACCOUNT);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ActivitySetup.REQUEST_CHOOSE_ACCOUNT:
                if (resultCode == Activity.RESULT_OK && data != null)
                    onAccountSelected(data);
                break;
        }
    }

    private void onAccountSelected(Intent data) {
        String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccountsByType(type);
        for (final Account account : accounts)
            if (name.equals(account.name)) {
                Snackbar.make(view, R.string.title_authorizing, Snackbar.LENGTH_LONG).show();

                am.getAuthToken(
                        account,
                        MailService.getAuthTokenType(type),
                        new Bundle(),
                        getActivity(),
                        new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                try {
                                    Bundle bundle = future.getResult();
                                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                                    Log.i("Got token");
                                    onAuthorized(name, token);
                                } catch (Throwable ex) {
                                    if (ex instanceof AccountsException || ex instanceof IOException) {
                                        Log.w(ex);
                                        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                            Snackbar.make(view, Helper.formatThrowable(ex), Snackbar.LENGTH_LONG).show();
                                    } else
                                        Helper.unexpectedError(getFragmentManager(), ex);
                                }
                            }
                        },
                        null);
                break;
            }
    }

    private void onAuthorized(String user, String password) {
        Bundle args = new Bundle();
        args.putString("user", user);
        args.putString("password", password);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                String user = args.getString("user");
                String password = args.getString("password");

                if (!user.contains("@"))
                    throw new IllegalArgumentException(
                            context.getString(R.string.title_email_invalid, user));

                String domain = user.split("@")[1];
                EmailProvider provider = EmailProvider.fromDomain(context, domain, EmailProvider.Discover.ALL);

                List<EntityFolder> folders;

                String aprotocol = provider.imap.starttls ? "imap" : "imaps";
                try (MailService iservice = new MailService(context, aprotocol, null, false, true)) {
                    iservice.connect(provider.imap.host, provider.imap.port, MailService.AUTH_TYPE_GMAIL, user, password);

                    folders = iservice.getFolders();

                    if (folders == null)
                        throw new IllegalArgumentException(
                                context.getString(R.string.title_setup_no_settings, domain));
                }

                String iprotocol = provider.smtp.starttls ? "smtp" : "smtps";
                try (MailService iservice = new MailService(context, iprotocol, null, false, true)) {
                    iservice.connect(provider.smtp.host, provider.smtp.port, MailService.AUTH_TYPE_GMAIL, user, password);
                }

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityAccount primary = db.account().getPrimaryAccount();

                    // Create account
                    EntityAccount account = new EntityAccount();

                    account.host = provider.imap.host;
                    account.starttls = provider.imap.starttls;
                    account.port = provider.imap.port;
                    account.auth_type = MailService.AUTH_TYPE_GMAIL;
                    account.user = user;
                    account.password = password;

                    account.name = provider.name;

                    account.synchronize = true;
                    account.primary = (primary == null);

                    account.created = new Date().getTime();
                    account.last_connected = account.created;

                    account.id = db.account().insertAccount(account);
                    args.putLong("account", account.id);
                    EntityLog.log(context, "Gmail account=" + account.name);

                    // Create folders
                    for (EntityFolder folder : folders) {
                        folder.account = account.id;
                        folder.id = db.folder().insertFolder(folder);
                        EntityLog.log(context, "Gmail folder=" + folder.name + " type=" + folder.type);
                    }

                    // Set swipe left/right folder
                    for (EntityFolder folder : folders)
                        if (EntityFolder.TRASH.equals(folder.type))
                            account.swipe_left = folder.id;
                        else if (EntityFolder.ARCHIVE.equals(folder.type))
                            account.swipe_right = folder.id;

                    db.account().updateAccount(account);

                    String name = user.split("@")[0];
                    try (Cursor cursor = context.getContentResolver().query(
                            ContactsContract.Profile.CONTENT_URI,
                            new String[]{ContactsContract.Profile.DISPLAY_NAME}, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            int colDisplay = cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME);
                            name = cursor.getString(colDisplay);
                        }
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                    // Create identity
                    EntityIdentity identity = new EntityIdentity();
                    identity.name = name;
                    identity.email = user;
                    identity.account = account.id;

                    identity.host = provider.smtp.host;
                    identity.starttls = provider.smtp.starttls;
                    identity.port = provider.smtp.port;
                    identity.auth_type = MailService.AUTH_TYPE_GMAIL;
                    identity.user = user;
                    identity.password = password;
                    identity.synchronize = true;
                    identity.primary = true;

                    identity.id = db.identity().insertIdentity(identity);
                    args.putLong("identity", identity.id);
                    EntityLog.log(context, "Gmail identity=" + identity.name + " email=" + identity.email);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.reload(getContext(), "Gmail");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                FragmentQuickSetup.FragmentDialogDone fragment = new FragmentQuickSetup.FragmentDialogDone();
                fragment.setArguments(args);
                fragment.show(getFragmentManager(), "gmail:done");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException || ex instanceof UnknownHostException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(view, Helper.formatThrowable(ex, false), Snackbar.LENGTH_LONG).show();
            }
        }.execute(this, args, "setup:gmail");
    }

    public static class FragmentDialogDoze extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
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
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
