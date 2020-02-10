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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;

import com.google.android.material.textfield.TextInputLayout;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import javax.mail.AuthenticationFailedException;

public class FragmentQuickSetup extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private EditText etName;
    private EditText etEmail;
    private TextInputLayout tilPassword;
    private TextView tvCharacters;
    private Button btnCheck;
    private ContentLoadingProgressBar pbCheck;

    private TextView tvError;
    private TextView tvErrorHint;
    private Button btnHelp;
    private Button btnSupport;
    private TextView tvInstructions;

    private TextView tvImap;
    private TextView tvSmtp;
    private Button btnSave;
    private ContentLoadingProgressBar pbSave;

    private Group grpSetup;
    private Group grpError;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup_quick);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_quick_setup, container, false);
        scroll = view.findViewById(R.id.scroll);

        // Get controls
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvCharacters = view.findViewById(R.id.tvCharacters);
        btnCheck = view.findViewById(R.id.btnCheck);
        pbCheck = view.findViewById(R.id.pbCheck);

        tvError = view.findViewById(R.id.tvError);
        tvErrorHint = view.findViewById(R.id.tvErrorHint);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnSupport = view.findViewById(R.id.btnSupport);
        tvInstructions = view.findViewById(R.id.tvInstructions);

        tvImap = view.findViewById(R.id.tvImap);
        tvSmtp = view.findViewById(R.id.tvSmtp);
        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);

        grpSetup = view.findViewById(R.id.grpSetup);
        grpError = view.findViewById(R.id.grpError);

        // Wire controls

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

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.view(getContext(), Uri.parse(Helper.SUPPORT_URI), false);
            }
        });

        // Initialize
        tvCharacters.setVisibility(View.GONE);
        pbCheck.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        btnHelp.setVisibility(View.GONE);
        btnSupport.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);
        tvInstructions.setMovementMethod(LinkMovementMethod.getInstance());
        grpSetup.setVisibility(View.GONE);
        grpError.setVisibility(View.GONE);

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

    private void onSave(boolean check) {
        Bundle args = new Bundle();
        args.putString("name", etName.getText().toString().trim());
        args.putString("email", etEmail.getText().toString().trim());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putBoolean("check", check);

        new SimpleTask<EmailProvider>() {
            @Override
            protected void onPreExecute(Bundle args) {
                boolean check = args.getBoolean("check");

                Helper.setViewsEnabled(view, false);
                pbCheck.setVisibility(check ? View.VISIBLE : View.GONE);
                pbSave.setVisibility(check ? View.GONE : View.VISIBLE);
                grpError.setVisibility(View.GONE);
                btnHelp.setVisibility(View.GONE);
                btnSupport.setVisibility(View.GONE);
                tvInstructions.setVisibility(View.GONE);
                grpSetup.setVisibility(check ? View.GONE : View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                Helper.setViewsEnabled(view, true);
                pbCheck.setVisibility(View.GONE);
                pbSave.setVisibility(View.GONE);
            }

            @Override
            protected EmailProvider onExecute(Context context, Bundle args) throws Throwable {
                String name = args.getString("name");
                String email = args.getString("email");
                String password = args.getString("password");
                boolean check = args.getBoolean("check");

                if (TextUtils.isEmpty(name))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_name));
                if (TextUtils.isEmpty(email))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_email));
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, email));
                if (TextUtils.isEmpty(password))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));

                EmailProvider provider = EmailProvider.fromEmail(context, email, EmailProvider.Discover.ALL);
                args.putBoolean("appPassword", provider.appPassword);

                if (provider.link != null)
                    args.putString("link", provider.link);
                if (provider.documentation != null)
                    args.putString("documentation", provider.documentation.toString());

                int at = email.indexOf('@');
                String username = email.substring(0, at);

                String user = (provider.user == EmailProvider.UserType.EMAIL ? email : username);
                Log.i("User type=" + provider.user + " name=" + user);

                List<EntityFolder> folders;

                String aprotocol = provider.imap.starttls ? "imap" : "imaps";
                try (EmailService iservice = new EmailService(
                        context, aprotocol, null, false, EmailService.PURPOSE_CHECK, true)) {
                    try {
                        iservice.connect(
                                provider.imap.host, provider.imap.port,
                                EmailService.AUTH_TYPE_PASSWORD, null,
                                user, password,
                                null, null);
                    } catch (AuthenticationFailedException ex) {
                        if (!user.equals(username)) {
                            Log.w(ex);
                            user = username;
                            Log.i("Retry with user=" + user);
                            iservice.connect(
                                    provider.imap.host, provider.imap.port,
                                    EmailService.AUTH_TYPE_PASSWORD, null,
                                    user, password,
                                    null, null);
                        } else
                            throw ex;
                    }

                    folders = iservice.getFolders();

                    if (folders == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_setup_no_system_folders));
                }

                String iprotocol = provider.smtp.starttls ? "smtp" : "smtps";
                try (EmailService iservice = new EmailService(
                        context, iprotocol, null, false, EmailService.PURPOSE_CHECK, true)) {
                    iservice.setUseIp(provider.useip);
                    iservice.connect(
                            provider.smtp.host, provider.smtp.port,
                            EmailService.AUTH_TYPE_PASSWORD, null,
                            user, password,
                            null, null);
                }

                if (check)
                    return provider;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityAccount primary = db.account().getPrimaryAccount();

                    // Create account
                    EntityAccount account = new EntityAccount();

                    account.host = provider.imap.host;
                    account.starttls = provider.imap.starttls;
                    account.port = provider.imap.port;
                    account.auth_type = EmailService.AUTH_TYPE_PASSWORD;
                    account.user = user;
                    account.password = password;

                    account.name = provider.name;

                    account.synchronize = true;
                    account.primary = (primary == null);

                    if (provider.keepalive > 0)
                        account.poll_interval = provider.keepalive;

                    account.partial_fetch = provider.partial;

                    account.created = new Date().getTime();
                    account.last_connected = account.created;

                    account.id = db.account().insertAccount(account);
                    args.putLong("account", account.id);
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

                    db.account().updateAccount(account);

                    // Create identity
                    EntityIdentity identity = new EntityIdentity();
                    identity.name = name;
                    identity.email = email;
                    identity.account = account.id;

                    identity.host = provider.smtp.host;
                    identity.starttls = provider.smtp.starttls;
                    identity.port = provider.smtp.port;
                    identity.auth_type = EmailService.AUTH_TYPE_PASSWORD;
                    identity.user = user;
                    identity.password = password;
                    identity.use_ip = provider.useip;
                    identity.synchronize = true;
                    identity.primary = true;

                    identity.id = db.identity().insertIdentity(identity);
                    EntityLog.log(context, "Quick added identity=" + identity.name + " email=" + identity.email);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "quick setup");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, EmailProvider result) {
                boolean check = args.getBoolean("check");
                if (check) {
                    tvImap.setText(result == null ? null
                            : result.imap.host + ":" + result.imap.port + (result.imap.starttls ? " starttls" : " ssl"));
                    tvSmtp.setText(result == null ? null
                            : result.smtp.host + ":" + result.smtp.port + (result.smtp.starttls ? " starttls" : " ssl"));
                    grpSetup.setVisibility(result == null ? View.GONE : View.VISIBLE);
                } else {
                    FragmentReview fragment = new FragmentReview();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentQuickSetup.this, ActivitySetup.REQUEST_DONE);
                    fragment.show(getParentFragmentManager(), "quick:review");
                }
            }

            @Override
            protected void onException(final Bundle args, Throwable ex) {
                Log.e(ex);

                if (ex instanceof AuthenticationFailedException) {
                    boolean appPassword = args.getBoolean("appPassword");
                    String message = getString(R.string.title_setup_no_auth_hint);
                    if (appPassword)
                        message += "\n" + getString(R.string.title_setup_app_password_hint);
                    tvErrorHint.setText(message);
                } else
                    tvErrorHint.setText(R.string.title_setup_no_settings_hint);

                if (ex instanceof IllegalArgumentException || ex instanceof UnknownHostException) {
                    tvError.setText(ex.getMessage());
                    grpError.setVisibility(View.VISIBLE);

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            scroll.smoothScrollTo(0, tvErrorHint.getBottom());
                        }
                    });
                } else {
                    tvError.setText(Log.formatThrowable(ex, false));
                    grpError.setVisibility(View.VISIBLE);

                    if (args.containsKey("link")) {
                        Uri uri = Uri.parse(args.getString("link"));
                        btnHelp.setTag(uri);
                        btnHelp.setVisibility(View.VISIBLE);
                    }

                    btnSupport.setVisibility(View.VISIBLE);

                    if (args.containsKey("documentation")) {
                        tvInstructions.setText(HtmlHelper.fromHtml(args.getString("documentation")));
                        tvInstructions.setVisibility(View.VISIBLE);
                    }

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (args.containsKey("documentation"))
                                scroll.smoothScrollTo(0, tvInstructions.getBottom());
                            else
                                scroll.smoothScrollTo(0, btnSupport.getBottom());
                        }
                    });
                }
            }
        }.execute(this, args, "setup:quick");
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
