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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spanned;
import android.text.TextUtils;
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
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Transport;

import static android.app.Activity.RESULT_OK;

public class FragmentQuickSetup extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private EditText etName;
    private EditText etEmail;
    private TextInputLayout tilPassword;
    private Button btnCheck;

    private TextView tvError;
    private Button btnHelp;
    private TextView tvInstructions;

    private TextView tvImap;
    private TextView tvSmtp;
    private Button btnSave;
    private Group grpSetup;

    private static final int REQUEST_DONE = 1;

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
        btnCheck = view.findViewById(R.id.btnCheck);

        tvError = view.findViewById(R.id.tvError);
        btnHelp = view.findViewById(R.id.btnHelp);
        tvInstructions = view.findViewById(R.id.tvInstructions);

        tvImap = view.findViewById(R.id.tvImap);
        tvSmtp = view.findViewById(R.id.tvSmtp);
        btnSave = view.findViewById(R.id.btnSave);
        grpSetup = view.findViewById(R.id.grpSetup);

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

        // Initialize
        tvError.setVisibility(View.GONE);
        btnHelp.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);
        tvInstructions.setMovementMethod(LinkMovementMethod.getInstance());
        grpSetup.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_quick_setup, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        PackageManager pm = getContext().getPackageManager();
        menu.findItem(R.id.menu_help).setVisible(Helper.getIntentSetupHelp().resolveActivity(pm) != null);
        super.onPrepareOptionsMenu(menu);
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
        startActivity(Helper.getIntentSetupHelp());
    }

    private void onSave(boolean check) {
        Bundle args = new Bundle();
        args.putString("name", etName.getText().toString());
        args.putString("email", etEmail.getText().toString().trim());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putBoolean("check", check);

        new SimpleTask<EmailProvider>() {
            @Override
            protected void onPreExecute(Bundle args) {
                boolean check = args.getBoolean("check");

                Helper.setViewsEnabled(view, false);
                tvError.setVisibility(View.GONE);
                btnHelp.setVisibility(View.GONE);
                tvInstructions.setVisibility(View.GONE);
                grpSetup.setVisibility(check ? View.GONE : View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                Helper.setViewsEnabled(view, true);
            }

            @Override
            protected EmailProvider onExecute(Context context, Bundle args) throws Throwable {
                String name = args.getString("name");
                String email = args.getString("email").trim();
                String password = args.getString("password");
                boolean check = args.getBoolean("check");

                if (TextUtils.isEmpty(name))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_name));
                if (TextUtils.isEmpty(email))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_email));
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    throw new IllegalArgumentException(context.getString(R.string.title_email_invalid));

                String[] dparts = email.split("@");
                EmailProvider provider = EmailProvider.fromDomain(context, dparts[1]);

                if (provider.helpUrl != null)
                    args.putString("help", provider.helpUrl);
                if (provider.documentation != null)
                    args.putString("documentation", provider.documentation.toString());

                String user = (provider.user == EmailProvider.UserType.EMAIL ? email : dparts[0]);

                List<EntityFolder> folders = new ArrayList<>();
                long now = new Date().getTime();

                {
                    Properties props = MessageHelper.getSessionProperties(null, false);
                    Session isession = Session.getInstance(props, null);
                    isession.setDebug(true);
                    try (IMAPStore istore = (IMAPStore) isession.getStore(provider.imap_starttls ? "imap" : "imaps")) {
                        istore.connect(provider.imap_host, provider.imap_port, user, password);

                        boolean inbox = false;
                        boolean drafts = false;
                        EntityFolder altDrafts = null;
                        for (Folder ifolder : istore.getDefaultFolder().list("*")) {
                            String fullName = ifolder.getFullName();
                            String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                            String type = EntityFolder.getType(attrs, fullName, true);

                            Log.i(fullName + " attrs=" + TextUtils.join(" ", attrs) + " type=" + type);

                            boolean isAltDrafts = fullName.toLowerCase().contains("draft");

                            if (type != null && (!EntityFolder.USER.equals(type) || isAltDrafts)) {
                                if (isAltDrafts)
                                    type = EntityFolder.DRAFTS;

                                int sync = EntityFolder.SYSTEM_FOLDER_SYNC.indexOf(type);
                                EntityFolder folder = new EntityFolder();
                                folder.name = fullName;
                                folder.type = type;
                                folder.synchronize = (sync >= 0);
                                folder.download = (sync < 0 ? true : EntityFolder.SYSTEM_FOLDER_DOWNLOAD.get(sync));
                                folder.sync_days = EntityFolder.DEFAULT_SYNC;
                                folder.keep_days = EntityFolder.DEFAULT_KEEP;

                                if (isAltDrafts)
                                    altDrafts = folder;
                                else
                                    folders.add(folder);

                                if (EntityFolder.INBOX.equals(type)) {
                                    folder.unified = true;
                                    folder.notify = true;
                                    inbox = true;
                                }
                                if (EntityFolder.DRAFTS.equals(type))
                                    drafts = true;
                            }
                        }

                        Log.i("Quick inbox=" + inbox + " drafts=" + drafts);

                        if (!drafts && altDrafts != null) {
                            drafts = true;
                            folders.add(altDrafts);
                            Log.i("Quick alt drafts=" + altDrafts.name);
                        }

                        if (!inbox || !drafts)
                            throw new IllegalArgumentException(
                                    context.getString(R.string.title_setup_no_settings, dparts[1]));
                    }
                }

                {
                    Properties props = MessageHelper.getSessionProperties(null, false);
                    Session isession = Session.getInstance(props, null);
                    isession.setDebug(true);
                    try (Transport itransport = isession.getTransport(provider.smtp_starttls ? "smtp" : "smtps")) {
                        itransport.connect(provider.smtp_host, provider.smtp_port, user, password);
                    }
                }

                if (check)
                    return provider;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();
                    EntityAccount primary = db.account().getPrimaryAccount();

                    // Create account
                    EntityAccount account = new EntityAccount();

                    account.auth_type = ConnectionHelper.AUTH_TYPE_PASSWORD;
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
                    account.poll_interval = EntityAccount.DEFAULT_KEEP_ALIVE_INTERVAL;

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

                    CharSequence promote = context.getText(R.string.app_promote);
                    if (promote instanceof Spanned)
                        identity.signature = HtmlHelper.toHtml((Spanned) promote);
                    else
                        identity.signature = null;

                    identity.auth_type = ConnectionHelper.AUTH_TYPE_PASSWORD;
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
            protected void onExecuted(Bundle args, EmailProvider result) {
                boolean check = args.getBoolean("check");
                if (check) {
                    tvImap.setText(result == null ? null
                            : result.imap_host + ":" + result.imap_port + (result.imap_starttls ? " starttls" : " ssl"));
                    tvSmtp.setText(result == null ? null
                            : result.smtp_host + ":" + result.smtp_port + (result.smtp_starttls ? " starttls" : " ssl"));
                    grpSetup.setVisibility(result == null ? View.GONE : View.VISIBLE);
                } else {
                    FragmentDialogDone fragment = new FragmentDialogDone();
                    fragment.setTargetFragment(FragmentQuickSetup.this, REQUEST_DONE);
                    fragment.show(getFragmentManager(), "quick:done");
                }
            }

            @Override
            protected void onException(final Bundle args, Throwable ex) {
                Log.i("Quick ex=" + Helper.formatThrowable(ex, false));

                if (ex instanceof IllegalArgumentException || ex instanceof UnknownHostException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else {
                    tvError.setText(Helper.formatThrowable(ex, false));
                    tvError.setVisibility(View.VISIBLE);
                }

                if (args.containsKey("help")) {
                    Uri uri = Uri.parse(args.getString("help"));
                    btnHelp.setTag(uri);
                    btnHelp.setVisibility(View.VISIBLE);
                }

                if (args.containsKey("documentation")) {
                    tvInstructions.setText(HtmlHelper.fromHtml(args.getString("documentation")));
                    tvInstructions.setVisibility(View.VISIBLE);
                }

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (args.containsKey("documentation"))
                            scroll.smoothScrollTo(0, tvInstructions.getBottom());
                        else if (args.containsKey("help"))
                            scroll.smoothScrollTo(0, btnHelp.getBottom());
                        else if (tvError.getVisibility() == View.VISIBLE)
                            scroll.smoothScrollTo(0, tvError.getBottom());
                    }
                });

            }
        }.execute(FragmentQuickSetup.this, args, "setup:quick");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_DONE:
                    finish();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    public static class FragmentDialogDone extends DialogFragmentEx {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setMessage(R.string.title_setup_quick_success)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendResult(RESULT_OK);
                        }
                    })
                    .create();
        }
    }
}
