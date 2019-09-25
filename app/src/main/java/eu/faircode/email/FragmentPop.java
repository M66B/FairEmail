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

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Date;
import java.util.List;

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

    private EditText etName;
    private Button btnColor;
    private View vwColor;
    private ImageButton ibColorDefault;
    private TextView tvColorPro;

    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;
    private CheckBox cbLeave;
    private EditText etInterval;

    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private TextView tvError;

    private ContentLoadingProgressBar pbWait;

    private long id = -1;
    private boolean saving = false;
    private int color = Color.TRANSPARENT;

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

        etName = view.findViewById(R.id.etName);
        btnColor = view.findViewById(R.id.btnColor);
        vwColor = view.findViewById(R.id.vwColor);
        ibColorDefault = view.findViewById(R.id.ibColorDefault);
        tvColorPro = view.findViewById(R.id.tvColorPro);

        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbLeave = view.findViewById(R.id.cbLeave);
        etInterval = view.findViewById(R.id.etInterval);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);

        tvError = view.findViewById(R.id.tvError);

        pbWait = view.findViewById(R.id.pbWait);

        setColor(color);
        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.initialize(R.string.title_color, color, new Bundle(), getContext());
                fragment.setTargetFragment(FragmentPop.this, REQUEST_COLOR);
                fragment.show(getFragmentManager(), "account:color");
            }
        });

        ibColorDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(Color.TRANSPARENT);
            }
        });

        Helper.linkPro(tvColorPro);

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbPrimary.setEnabled(checked);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);

        tilPassword.setEndIconMode(id < 0 ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);

        pbSave.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        return view;
    }

    private void onSave() {
        Bundle args = new Bundle();
        args.putLong("id", id);

        args.putString("host", etHost.getText().toString());
        args.putBoolean("starttls", rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putString("user", etUser.getText().toString());
        args.putString("password", tilPassword.getEditText().getText().toString());

        args.putString("name", etName.getText().toString());
        args.putInt("color", color);

        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("primary", cbPrimary.isChecked());
        args.putBoolean("leave", cbLeave.isChecked());
        args.putString("interval", etInterval.getText().toString());

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                tvError.setVisibility(View.GONE);
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
                boolean primary = args.getBoolean("primary");
                boolean leave = args.getBoolean("leave");
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
                    interval = Integer.toString(EntityAccount.DEFAULT_KEEP_ALIVE_INTERVAL);

                if (TextUtils.isEmpty(name))
                    name = user;
                if (color == Color.TRANSPARENT || !pro)
                    color = null;

                long now = new Date().getTime();

                DB db = DB.getInstance(context);
                EntityAccount account = db.account().getAccount(id);

                boolean check = (synchronize && (account == null ||
                        !account.synchronize || account.error != null ||
                        !account.insecure.equals(insecure) ||
                        !host.equals(account.host) || Integer.parseInt(port) != account.port ||
                        !user.equals(account.user) || !password.equals(account.password)));
                boolean reload = (check || account == null ||
                        account.synchronize != synchronize ||
                        account.browse != leave ||
                        !account.poll_interval.equals(Integer.parseInt(interval)));
                Log.i("Account check=" + check + " reload=" + reload);

                Long last_connected = null;
                if (account != null && synchronize == account.synchronize)
                    last_connected = account.last_connected;

                // Check POP3 server
                if (check) {
                    String protocol = "pop3" + (starttls ? "" : "s");
                    try (MailService iservice = new MailService(context, protocol, null, insecure, true)) {
                        iservice.connect(host, Integer.parseInt(port), MailService.AUTH_TYPE_PASSWORD, user, password);
                    }
                }

                try {
                    db.beginTransaction();

                    if (account != null && !account.password.equals(password)) {
                        List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                        for (EntityIdentity identity : identities)
                            if (identity.password.equals(account.password) &&
                                    ConnectionHelper.isSameDomain(identity.host, account.host)) {
                                Log.i("Changing identity password host=" + identity.host);
                                identity.password = password;
                                db.identity().updateIdentity(identity);
                            }
                    }

                    boolean update = (account != null);
                    if (account == null)
                        account = new EntityAccount();

                    account.pop = true;
                    account.host = host;
                    account.starttls = starttls;
                    account.insecure = insecure;
                    account.port = Integer.parseInt(port);
                    account.auth_type = MailService.AUTH_TYPE_PASSWORD;
                    account.user = user;
                    account.password = password;

                    account.name = name;
                    account.color = color;

                    account.synchronize = synchronize;
                    account.primary = (account.synchronize && primary);
                    account.browse = leave;
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

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (reload)
                    ServiceSynchronize.reload(context, "save account");

                if (!synchronize) {
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel("receive:" + account.id, 1);
                    nm.cancel("alert:" + account.id, 1);
                }

                return false;
            }

            @Override
            protected void onExecuted(Bundle args, Boolean dirty) {
                getFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else {
                    tvError.setText(Helper.formatThrowable(ex, false));
                    tvError.setVisibility(View.VISIBLE);

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
        outState.putInt("fair:color", color);
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

                    cbSynchronize.setChecked(account == null ? true : account.synchronize);
                    cbPrimary.setChecked(account == null ? false : account.primary);
                    cbLeave.setChecked(account == null ? true : account.browse);
                    etInterval.setText(account == null ? "" : Long.toString(account.poll_interval));

                    color = (account == null || account.color == null ? Color.TRANSPARENT : account.color);

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
                            Helper.unexpectedError(getFragmentManager(), ex);
                        }
                    }.execute(FragmentPop.this, new Bundle(), "account:primary");
                } else {
                    tilPassword.getEditText().setText(savedInstanceState.getString("fair:password"));
                    color = savedInstanceState.getInt("fair:color");
                }

                setColor(color);
                cbPrimary.setEnabled(cbSynchronize.isChecked());

                Helper.setViewsEnabled(view, true);

                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
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

    private void setColor(int color) {
        this.color = color;

        GradientDrawable border = new GradientDrawable();
        border.setColor(color);
        border.setStroke(1, Helper.resolveColor(getContext(), R.attr.colorSeparator));
        vwColor.setBackground(border);
    }

    private void onMenuDelete() {
        Bundle aargs = new Bundle();
        aargs.putString("question", getString(R.string.title_account_delete));

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(aargs);
        fragment.setTargetFragment(FragmentPop.this, REQUEST_DELETE);
        fragment.show(getFragmentManager(), "account:delete");
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
                            setColor(args.getInt("color"));
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

                ServiceSynchronize.reload(context, "delete account");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "account:delete");
    }
}
