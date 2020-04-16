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

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Date;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_NONE;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;

public class FragmentPop extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private EditText etHost;
    private RadioGroup rgEncryption;
    private CheckBox cbInsecure;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private TextView tvCharacters;

    private EditText etName;
    private ViewButtonColor btnColor;
    private TextView tvColorPro;

    private CheckBox cbSynchronize;
    private CheckBox cbNotify;
    private TextView tvNotifyPro;
    private CheckBox cbOnDemand;
    private CheckBox cbPrimary;
    private CheckBox cbLeaveServer;
    private CheckBox cbLeaveDeleted;
    private CheckBox cbLeaveDevice;
    private EditText etMax;
    private EditText etInterval;

    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private TextView tvError;
    private Group grpError;

    private ContentLoadingProgressBar pbWait;

    private long id = -1;
    private boolean saving = false;

    private static final int REQUEST_COLOR = 1;
    private static final int REQUEST_DELETE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        id = args.getLong("id", -1);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_account);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_pop, container, false);
        scroll = view.findViewById(R.id.scroll);

        // Get controls
        etHost = view.findViewById(R.id.etHost);
        etPort = view.findViewById(R.id.etPort);
        rgEncryption = view.findViewById(R.id.rgEncryption);
        cbInsecure = view.findViewById(R.id.cbInsecure);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvCharacters = view.findViewById(R.id.tvCharacters);

        etName = view.findViewById(R.id.etName);
        btnColor = view.findViewById(R.id.btnColor);
        tvColorPro = view.findViewById(R.id.tvColorPro);

        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbOnDemand = view.findViewById(R.id.cbOnDemand);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbNotify = view.findViewById(R.id.cbNotify);
        tvNotifyPro = view.findViewById(R.id.tvNotifyPro);
        cbLeaveServer = view.findViewById(R.id.cbLeaveServer);
        cbLeaveDeleted = view.findViewById(R.id.cbLeaveDeleted);
        cbLeaveDevice = view.findViewById(R.id.cbLeaveDevice);
        etMax = view.findViewById(R.id.etMax);
        etInterval = view.findViewById(R.id.etInterval);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);

        tvError = view.findViewById(R.id.tvError);
        grpError = view.findViewById(R.id.grpError);

        pbWait = view.findViewById(R.id.pbWait);

        tilPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s))
                    tilPassword.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
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

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("color", btnColor.getColor());
                args.putString("title", getString(R.string.title_color));
                args.putBoolean("reset", true);

                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentPop.this, REQUEST_COLOR);
                fragment.show(getParentFragmentManager(), "account:color");
            }
        });

        Helper.linkPro(tvColorPro);

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbOnDemand.setEnabled(checked);
                cbPrimary.setEnabled(checked);
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Helper.hide(cbNotify);
            Helper.hide(view.findViewById(R.id.tvNotifyPro));
        }

        Helper.linkPro(tvNotifyPro);

        etInterval.setHint(Integer.toString(EntityAccount.DEFAULT_POLL_INTERVAL));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);

        tilPassword.setEndIconMode(id < 0 || Helper.isSecure(getContext()) ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);
        tvCharacters.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        grpError.setVisibility(View.GONE);

        return view;
    }

    private void onSave() {
        Bundle args = new Bundle();
        args.putLong("id", id);

        args.putString("host", etHost.getText().toString().trim());
        args.putBoolean("starttls", rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putString("user", etUser.getText().toString());
        args.putString("password", tilPassword.getEditText().getText().toString());

        args.putString("name", etName.getText().toString());
        args.putInt("color", btnColor.getColor());

        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("ondemand", cbOnDemand.isChecked());
        args.putBoolean("primary", cbPrimary.isChecked());
        args.putBoolean("notify", cbNotify.isChecked());
        args.putBoolean("leave_server", cbLeaveServer.isChecked());
        args.putBoolean("leave_deleted", cbLeaveDeleted.isChecked());
        args.putBoolean("leave_device", cbLeaveDevice.isChecked());
        args.putString("max", etMax.getText().toString());
        args.putString("interval", etInterval.getText().toString());

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                grpError.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                saving = false;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, true);
                pbSave.setVisibility(View.GONE);
            }

            @Override
            protected Boolean onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

                String host = args.getString("host");
                boolean starttls = args.getBoolean("starttls");
                boolean insecure = args.getBoolean("insecure");
                String port = args.getString("port");
                String user = args.getString("user").trim();
                String password = args.getString("password");

                String name = args.getString("name");
                Integer color = args.getInt("color");

                boolean synchronize = args.getBoolean("synchronize");
                boolean ondemand = args.getBoolean("ondemand");
                boolean primary = args.getBoolean("primary");
                boolean notify = args.getBoolean("notify");
                boolean leave_server = args.getBoolean("leave_server");
                boolean leave_deleted = args.getBoolean("leave_deleted");
                boolean leave_device = args.getBoolean("leave_device");
                String max = args.getString("max");
                String interval = args.getString("interval");

                boolean pro = ActivityBilling.isPro(context);

                if (host.contains(":")) {
                    Uri h = Uri.parse(host);
                    host = h.getHost();
                }

                if (TextUtils.isEmpty(host))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = "995";
                if (TextUtils.isEmpty(user))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (synchronize && TextUtils.isEmpty(password) && !insecure)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));
                if (TextUtils.isEmpty(interval))
                    interval = Integer.toString(EntityAccount.DEFAULT_POLL_INTERVAL);

                if (TextUtils.isEmpty(name))
                    name = user;
                if (color == Color.TRANSPARENT || !pro)
                    color = null;

                long now = new Date().getTime();

                DB db = DB.getInstance(context);
                EntityAccount account = db.account().getAccount(id);

                boolean check = (synchronize && (account == null ||
                        !account.synchronize ||
                        account.error != null ||
                        !account.host.equals(host) ||
                        !account.starttls.equals(starttls) ||
                        !account.insecure.equals(insecure) ||
                        !account.port.equals(Integer.parseInt(port)) ||
                        !account.user.equals(user) ||
                        !account.password.equals(password)));
                Log.i("Account check=" + check);

                Long last_connected = null;
                if (account != null && synchronize == account.synchronize)
                    last_connected = account.last_connected;

                // Check POP3 server
                if (check) {
                    String protocol = "pop3" + (starttls ? "" : "s");
                    try (EmailService iservice = new EmailService(
                            context, protocol, null, insecure, EmailService.PURPOSE_CHECK, true)) {
                        iservice.connect(
                                host, Integer.parseInt(port),
                                EmailService.AUTH_TYPE_PASSWORD, null,
                                user, password,
                                null, null);
                    }
                }

                try {
                    db.beginTransaction();

                    if (account != null && !account.password.equals(password)) {
                        String domain = DnsHelper.getParentDomain(account.host);
                        String match = (Objects.equals(account.host, domain) ? account.host : "%." + domain);
                        int count = db.identity().setIdentityPassword(account.id, account.user, password, match);
                        Log.i("Updated passwords=" + count + " match=" + match);
                    }

                    boolean update = (account != null);
                    if (account == null)
                        account = new EntityAccount();

                    account.protocol = EntityAccount.TYPE_POP;
                    account.host = host;
                    account.starttls = starttls;
                    account.insecure = insecure;
                    account.port = Integer.parseInt(port);
                    account.auth_type = EmailService.AUTH_TYPE_PASSWORD;
                    account.user = user;
                    account.password = password;

                    account.name = name;
                    account.color = color;

                    account.synchronize = synchronize;
                    account.ondemand = ondemand;
                    account.primary = (account.synchronize && primary);
                    account.notify = notify;
                    account.leave_on_server = leave_server;
                    account.leave_deleted = leave_deleted;
                    account.leave_on_device = leave_device;
                    account.max_messages = (TextUtils.isEmpty(max) ? null : Integer.parseInt(max));
                    account.poll_interval = Integer.parseInt(interval);

                    if (!update)
                        account.created = now;

                    account.warning = null;
                    account.error = null;
                    account.last_connected = last_connected;

                    if (account.primary)
                        db.account().resetPrimary();

                    if (update)
                        db.account().updateAccount(account);
                    else
                        account.id = db.account().insertAccount(account);
                    EntityLog.log(context, (update ? "Updated" : "Added") + " account=" + account.name);

                    // Make sure the channel exists on commit
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (account.notify) {
                            // Add or update notification channel
                            account.deleteNotificationChannel(context);
                            account.createNotificationChannel(context);
                        } else if (!account.synchronize)
                            account.deleteNotificationChannel(context);
                    }

                    EntityFolder inbox = db.folder().getFolderByType(account.id, EntityFolder.INBOX);
                    if (inbox == null) {
                        inbox = new EntityFolder();
                        inbox.account = account.id;
                        inbox.name = "INBOX";
                        inbox.type = EntityFolder.INBOX;
                        inbox.synchronize = true;
                        inbox.unified = true;
                        inbox.notify = true;
                        inbox.sync_days = Integer.MAX_VALUE;
                        inbox.keep_days = Integer.MAX_VALUE;
                        inbox.initialize = 0;
                        inbox.id = db.folder().insertFolder(inbox);

                        if (account.synchronize)
                            EntityOperation.sync(context, inbox.id, false);
                    }

                    EntityFolder drafts = db.folder().getFolderByType(account.id, EntityFolder.DRAFTS);
                    if (drafts == null) {
                        drafts = new EntityFolder();
                        drafts.account = account.id;
                        drafts.name = context.getString(R.string.title_folder_drafts);
                        drafts.type = EntityFolder.DRAFTS;
                        drafts.synchronize = false;
                        drafts.unified = false;
                        drafts.notify = false;
                        drafts.sync_days = Integer.MAX_VALUE;
                        drafts.keep_days = Integer.MAX_VALUE;
                        drafts.initialize = 0;
                        drafts.id = db.folder().insertFolder(drafts);
                    }

                    EntityFolder sent = db.folder().getFolderByType(account.id, EntityFolder.SENT);
                    if (sent == null) {
                        sent = new EntityFolder();
                        sent.account = account.id;
                        sent.name = context.getString(R.string.title_folder_sent);
                        sent.type = EntityFolder.SENT;
                        sent.synchronize = false;
                        sent.unified = false;
                        sent.notify = false;
                        sent.sync_days = Integer.MAX_VALUE;
                        sent.keep_days = Integer.MAX_VALUE;
                        sent.initialize = 0;
                        sent.id = db.folder().insertFolder(sent);
                    }

                    EntityFolder trash = db.folder().getFolderByType(account.id, EntityFolder.TRASH);
                    if (trash == null) {
                        trash = new EntityFolder();
                        trash.account = account.id;
                        trash.name = context.getString(R.string.title_folder_trash);
                        trash.type = EntityFolder.TRASH;
                        trash.synchronize = false;
                        trash.unified = false;
                        trash.notify = false;
                        trash.sync_days = Integer.MAX_VALUE;
                        trash.keep_days = Integer.MAX_VALUE;
                        trash.initialize = 0;
                        trash.id = db.folder().insertFolder(trash);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "POP3");

                if (!synchronize) {
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel("receive:" + account.id, 1);
                    nm.cancel("alert:" + account.id, 1);
                }

                return false;
            }

            @Override
            protected void onExecuted(Bundle args, Boolean dirty) {
                getParentFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else {
                    tvError.setText(Log.formatThrowable(ex, false));
                    grpError.setVisibility(View.VISIBLE);

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            scroll.smoothScrollTo(0, tvError.getBottom());
                        }
                    });
                }
            }
        }.execute(this, args, "account:save");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("fair:password", tilPassword.getEditText().getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<EntityAccount>() {
            @Override
            protected EntityAccount onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                return db.account().getAccount(id);
            }

            @Override
            protected void onExecuted(Bundle args, final EntityAccount account) {
                if (savedInstanceState == null) {
                    etHost.setText(account == null ? null : account.host);
                    etPort.setText(account == null ? null : Long.toString(account.port));

                    rgEncryption.check(account != null && account.starttls ? R.id.radio_starttls : R.id.radio_ssl);
                    cbInsecure.setChecked(account == null ? false : account.insecure);

                    etUser.setText(account == null ? null : account.user);
                    tilPassword.getEditText().setText(account == null ? null : account.password);

                    etName.setText(account == null ? null : account.name);
                    btnColor.setColor(account == null ? null : account.color);

                    boolean pro = ActivityBilling.isPro(getContext());
                    cbNotify.setChecked(account != null && account.notify && pro);
                    cbNotify.setEnabled(pro);

                    cbSynchronize.setChecked(account == null ? true : account.synchronize);
                    cbOnDemand.setChecked(account == null ? false : account.ondemand);
                    cbPrimary.setChecked(account == null ? false : account.primary);
                    cbLeaveServer.setChecked(account == null ? true : account.leave_on_server);
                    cbLeaveDeleted.setChecked(account == null ? true : account.leave_deleted);
                    cbLeaveDevice.setChecked(account == null ? false : account.leave_on_device);
                    etMax.setText(Integer.toString(account == null || account.max_messages == null
                            ? EntityAccount.DEFAULT_MAX_MESSAGES : account.max_messages));
                    etInterval.setText(account == null ? "" : Long.toString(account.poll_interval));

                    new SimpleTask<EntityAccount>() {
                        @Override
                        protected EntityAccount onExecute(Context context, Bundle args) {
                            return DB.getInstance(context).account().getPrimaryAccount();
                        }

                        @Override
                        protected void onExecuted(Bundle args, EntityAccount primary) {
                            if (primary == null)
                                cbPrimary.setChecked(true);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentPop.this, new Bundle(), "account:primary");
                } else {
                    tilPassword.getEditText().setText(savedInstanceState.getString("fair:password"));
                }

                Helper.setViewsEnabled(view, true);

                cbOnDemand.setEnabled(cbSynchronize.isChecked());
                cbPrimary.setEnabled(cbSynchronize.isChecked());

                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "account:get");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_delete).setVisible(id > 0 && !saving);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                onMenuDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuDelete() {
        Bundle aargs = new Bundle();
        aargs.putString("question", getString(R.string.title_account_delete));

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(aargs);
        fragment.setTargetFragment(FragmentPop.this, REQUEST_DELETE);
        fragment.show(getParentFragmentManager(), "account:delete");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_COLOR:
                    if (resultCode == RESULT_OK && data != null) {
                        if (ActivityBilling.isPro(getContext())) {
                            Bundle args = data.getBundleExtra("args");
                            btnColor.setColor(args.getInt("color"));
                        } else
                            startActivity(new Intent(getContext(), ActivityBilling.class));
                    }
                    break;
                case REQUEST_DELETE:
                    if (resultCode == RESULT_OK)
                        onDelete();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onDelete() {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<Void>() {
            @Override
            protected void onPostExecute(Bundle args) {
                Helper.setViewsEnabled(view, false);
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                db.account().setAccountTbd(id);

                ServiceSynchronize.eval(context, "delete account");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getParentFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "account:delete");
    }
}
