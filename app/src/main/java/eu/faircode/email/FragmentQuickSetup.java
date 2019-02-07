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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.EditText;
import android.widget.TextView;

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

import static android.accounts.AccountManager.newChooseAccountIntent;
import static android.app.Activity.RESULT_OK;

public class FragmentQuickSetup extends FragmentBase {
    private ViewGroup view;

    private EditText etName;
    private EditText etEmail;
    private Button btnAuthorize;
    private TextInputLayout tilPassword;
    private Button btnCheck;
    private TextView tvError;
    private TextView tvInstructions;

    private int auth_type = Helper.AUTH_TYPE_PASSWORD;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_quick_setup, container, false);

        // Get controls
        etName = view.findViewById(R.id.etName);
        btnAuthorize = view.findViewById(R.id.btnAuthorize);
        etEmail = view.findViewById(R.id.etEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        btnCheck = view.findViewById(R.id.btnCheck);
        tvError = view.findViewById(R.id.tvError);
        tvInstructions = view.findViewById(R.id.tvInstructions);

        // Wire controls

        btnAuthorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String permission = Manifest.permission.GET_ACCOUNTS;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O &&
                        !Helper.hasPermission(getContext(), permission)) {
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

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheck();
            }
        });

        // Initialize
        tvError.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);
        tvInstructions.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }

    private void onCheck() {
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
                btnCheck.setEnabled(false);
                tvError.setVisibility(View.GONE);
                tvInstructions.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                etName.setEnabled(true);
                etEmail.setEnabled(true);
                tilPassword.setEnabled(true);
                btnAuthorize.setEnabled(true);
                btnCheck.setEnabled(true);
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

                {
                    Properties props = MessageHelper.getSessionProperties(auth_type, null, false);
                    Session isession = Session.getInstance(props, null);
                    isession.setDebug(true);
                    IMAPStore istore = null;
                    try {
                        istore = (IMAPStore) isession.getStore(provider.imap_starttls ? "imap" : "imaps");
                        istore.connect(provider.imap_host, provider.imap_port, user, password);

                        separator = istore.getDefaultFolder().getSeparator();

                        boolean inbox = false;
                        boolean drafts = false;
                        for (Folder ifolder : istore.getDefaultFolder().list("*")) {
                            String fullName = ifolder.getFullName();
                            String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                            String type = EntityFolder.getType(attrs, fullName);

                            Log.i(fullName + " attrs=" + TextUtils.join(" ", attrs) + " type=" + type);

                            if (type != null && !EntityFolder.USER.equals(type)) {
                                int sync = EntityFolder.SYSTEM_FOLDER_SYNC.indexOf(type);
                                EntityFolder folder = new EntityFolder();
                                folder.name = fullName;
                                folder.type = type;
                                folder.level = EntityFolder.getLevel(separator, folder.name);
                                folder.synchronize = (sync >= 0);
                                folder.download = (sync < 0 ? true : EntityFolder.SYSTEM_FOLDER_DOWNLOAD.get(sync));
                                folder.sync_days = EntityFolder.DEFAULT_SYNC;
                                folder.keep_days = EntityFolder.DEFAULT_KEEP;
                                folders.add(folder);

                                if (EntityFolder.INBOX.equals(type))
                                    inbox = true;
                                if (EntityFolder.DRAFTS.equals(type))
                                    drafts = true;
                            }
                        }

                        if (!inbox || !drafts)
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
                    EntityLog.log(context, "Quick added account=" + account.name);

                    // Create folders
                    for (EntityFolder folder : folders) {
                        folder.account = account.id;
                        folder.id = db.folder().insertFolder(folder);
                        EntityLog.log(context, "Quick added folder=" + folder.name + " type=" + folder.type);
                    }

                    // Set swipe left/right folder
                    for (EntityFolder folder : folders)
                        if (EntityFolder.TRASH.equals(folder.type))
                            account.swipe_left = folder.id;
                        else if (EntityFolder.ARCHIVE.equals(folder.type))
                            account.swipe_right = folder.id;

                    if (account.swipe_right == null && account.swipe_left != null)
                        account.swipe_right = account.swipe_left;

                    db.account().updateAccount(account);

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
                    EntityLog.log(context, "Quick added identity=" + identity.name + " email=" + identity.email);

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
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
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
                    tvError.setText(Helper.formatThrowable(ex));
                    tvError.setVisibility(View.VISIBLE);
                }
            }
        }.execute(FragmentQuickSetup.this, args, "setup:quick");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ActivitySetup.REQUEST_CHOOSE_ACCOUNT)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectAccount();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivitySetup.REQUEST_CHOOSE_ACCOUNT)
            if (resultCode == RESULT_OK && data != null)
                accountSelected(data);
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

    private void accountSelected(Intent data) {
        Log.i("Selected account");
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
                btnCheck.setEnabled(false);
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
                                    btnCheck.setEnabled(true);
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
}
