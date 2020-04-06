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
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;

import java.util.Date;
import java.util.List;

import static android.accounts.AccountManager.newChooseAccountIntent;
import static android.app.Activity.RESULT_OK;

public class FragmentGmail extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private Button btnGrant;
    private TextView tvGranted;
    private EditText etName;
    private Button btnSelect;
    private ContentLoadingProgressBar pbSelect;

    private TextView tvError;
    private Button btnSupport;

    private Group grpError;

    private static String TYPE_GOOGLE = "com.google";

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup_quick);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_gmail, container, false);
        scroll = view.findViewById(R.id.scroll);

        // Get controls
        btnGrant = view.findViewById(R.id.btnGrant);
        tvGranted = view.findViewById(R.id.tvGranted);
        etName = view.findViewById(R.id.etName);
        btnSelect = view.findViewById(R.id.btnSelect);
        pbSelect = view.findViewById(R.id.pbSelect);

        tvError = view.findViewById(R.id.tvError);
        btnSupport = view.findViewById(R.id.btnSupport);

        grpError = view.findViewById(R.id.grpError);

        // Wire controls

        btnGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(Helper.getOAuthPermissions(), ActivitySetup.REQUEST_CHOOSE_ACCOUNT);
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    grpError.setVisibility(View.GONE);

                    String name = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(name))
                        throw new IllegalArgumentException(getString(R.string.title_no_name));


                    Intent intent = newChooseAccountIntent(
                            null,
                            null,
                            new String[]{TYPE_GOOGLE},
                            false,
                            null,
                            null,
                            null,
                            null);
                    PackageManager pm = getContext().getPackageManager();
                    if (intent.resolveActivity(pm) == null)
                        throw new IllegalArgumentException(getString(R.string.title_no_viewer, intent));
                    startActivityForResult(intent, ActivitySetup.REQUEST_CHOOSE_ACCOUNT);
                } catch (Throwable ex) {
                    if (ex instanceof IllegalArgumentException)
                        tvError.setText(ex.getMessage());
                    else
                        tvError.setText(Log.formatThrowable(ex, false));
                    grpError.setVisibility(View.VISIBLE);
                }
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.view(getContext(), Uri.parse(Helper.SUPPORT_URI), false);
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);
        pbSelect.setVisibility(View.GONE);
        grpError.setVisibility(View.GONE);

        boolean granted = Helper.hasPermissions(getContext(), Helper.getOAuthPermissions());
        setGranted(granted);

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        for (int i = 0; i < permissions.length; i++)
            granted = (granted && grantResults[i] == PackageManager.PERMISSION_GRANTED);

        setGranted(granted);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case ActivitySetup.REQUEST_CHOOSE_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null)
                        onAccountSelected(data);
                    else
                        onNoAccountSelected();
                    break;
                case ActivitySetup.REQUEST_DONE:
                    finish();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void setGranted(boolean granted) {
        btnGrant.setEnabled(!granted);
        tvGranted.setVisibility(granted ? View.VISIBLE : View.GONE);

        if (granted) {
            try (Cursor cursor = getContext().getContentResolver().query(
                    ContactsContract.Profile.CONTENT_URI,
                    new String[]{ContactsContract.Profile.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int colDisplay = cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME);
                    etName.setText(cursor.getString(colDisplay));
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        etName.setEnabled(granted);
        btnSelect.setEnabled(granted);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                etName.requestFocus();
            }
        });
    }

    private void onNoAccountSelected() {
        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccountsByType(TYPE_GOOGLE);
        if (accounts.length == 0)
            ToastEx.makeText(getContext(), R.string.title_no_account, Toast.LENGTH_LONG).show();
    }

    private void onAccountSelected(Intent data) {
        String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccountsByType(type);
        for (final Account account : accounts)
            if (name.equals(account.name)) {
                am.getAuthToken(
                        account,
                        EmailService.getAuthTokenType(type),
                        new Bundle(),
                        getActivity(),
                        new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                try {
                                    Bundle bundle = future.getResult();
                                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                                    if (token == null)
                                        throw new IllegalArgumentException("no token");
                                    Log.i("Got token");

                                    onAuthorized(name, token);
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                    tvError.setText(Log.formatThrowable(ex));
                                    grpError.setVisibility(View.VISIBLE);

                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            scroll.smoothScrollTo(0, tvError.getBottom());
                                        }
                                    });
                                }
                            }
                        },
                        null);
                break;
            }
    }

    private void onAuthorized(String user, String password) {
        Bundle args = new Bundle();
        args.putString("name", etName.getText().toString().trim());
        args.putString("user", user);
        args.putString("password", password);

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                etName.setEnabled(false);
                btnSelect.setEnabled(false);
                pbSelect.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                etName.setEnabled(true);
                btnSelect.setEnabled(true);
                pbSelect.setVisibility(View.GONE);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                String name = args.getString("name");
                String user = args.getString("user");
                String password = args.getString("password");

                // Safety checks
                if (!Helper.EMAIL_ADDRESS.matcher(user).matches())
                    throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, user));
                if (TextUtils.isEmpty(password))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));

                EmailProvider provider = EmailProvider.fromDomain(context, "gmail.com", EmailProvider.Discover.ALL);

                List<EntityFolder> folders;

                String aprotocol = provider.imap.starttls ? "imap" : "imaps";
                try (EmailService iservice = new EmailService(
                        context, aprotocol, null, false, EmailService.PURPOSE_CHECK, true)) {
                    iservice.connect(
                            provider.imap.host, provider.imap.port,
                            EmailService.AUTH_TYPE_GMAIL, null,
                            user, password,
                            null, null);

                    folders = iservice.getFolders();

                    if (folders == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_setup_no_system_folders));
                }

                String iprotocol = provider.smtp.starttls ? "smtp" : "smtps";
                try (EmailService iservice = new EmailService(
                        context, iprotocol, null, false, EmailService.PURPOSE_CHECK, true)) {
                    iservice.connect(
                            provider.smtp.host, provider.smtp.port,
                            EmailService.AUTH_TYPE_GMAIL, null,
                            user, password,
                            null, null);
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
                    account.auth_type = EmailService.AUTH_TYPE_GMAIL;
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
                        EntityFolder existing = db.folder().getFolderByName(account.id, folder.name);
                        if (existing == null) {
                            folder.account = account.id;
                            folder.id = db.folder().insertFolder(folder);
                            EntityLog.log(context, "Gmail folder=" + folder.name + " type=" + folder.type);
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

                    if (TextUtils.isEmpty(name))
                        name = user.split("@")[0];

                    // Create identity
                    EntityIdentity identity = new EntityIdentity();
                    identity.name = name;
                    identity.email = user;
                    identity.account = account.id;

                    identity.host = provider.smtp.host;
                    identity.starttls = provider.smtp.starttls;
                    identity.port = provider.smtp.port;
                    identity.auth_type = EmailService.AUTH_TYPE_GMAIL;
                    identity.user = user;
                    identity.password = password;
                    identity.synchronize = true;
                    identity.primary = true;

                    identity.id = db.identity().insertIdentity(identity);
                    EntityLog.log(context, "Gmail identity=" + identity.name + " email=" + identity.email);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "Gmail");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                FragmentReview fragment = new FragmentReview();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentGmail.this, ActivitySetup.REQUEST_DONE);
                fragment.show(getParentFragmentManager(), "quick:review");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.e(ex);

                if (ex instanceof IllegalArgumentException)
                    tvError.setText(ex.getMessage());
                else
                    tvError.setText(Log.formatThrowable(ex));
                grpError.setVisibility(View.VISIBLE);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.smoothScrollTo(0, tvError.getBottom());
                    }
                });
            }
        }.execute(this, args, "setup:gmail");
    }
}
