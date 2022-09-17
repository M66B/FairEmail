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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_OK;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_NONE;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FragmentPop extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private EditText etHost;
    private RadioGroup rgEncryption;
    private CheckBox cbInsecure;
    private TextView tvInsecureRemark;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private TextView tvPasswordStorage;

    private EditText etName;
    private EditText etCategory;
    private ViewButtonColor btnColor;
    private TextView tvColorPro;

    private CheckBox cbSynchronize;
    private CheckBox cbIgnoreSchedule;
    private CheckBox cbOnDemand;
    private CheckBox cbPrimary;
    private CheckBox cbNotify;
    private TextView tvNotifyPro;
    private CheckBox cbAutoSeen;
    private CheckBox cbLeaveServer;
    private CheckBox cbLeaveDeleted;
    private CheckBox cbLeaveDevice;
    private EditText etMax;
    private EditText etInterval;
    private CheckBox cbUnmetered;
    private CheckBox cbVpnOnly;

    private ArrayAdapter<EntityFolder> adapterSwipe;
    private Spinner spLeft;
    private Spinner spRight;

    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private CheckBox cbIdentity;
    private TextView tvError;
    private Group grpError;

    private ContentLoadingProgressBar pbWait;

    private long id = -1;
    private int auth = AUTH_TYPE_PASSWORD;
    private boolean saving = false;

    private static final int REQUEST_COLOR = 1;
    private static final int REQUEST_SAVE = 2;
    private static final int REQUEST_DELETE = 3;

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
        tvInsecureRemark = view.findViewById(R.id.tvInsecureRemark);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvPasswordStorage = view.findViewById(R.id.tvPasswordStorage);

        etName = view.findViewById(R.id.etName);
        etCategory = view.findViewById(R.id.etCategory);
        btnColor = view.findViewById(R.id.btnColor);
        tvColorPro = view.findViewById(R.id.tvColorPro);

        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbIgnoreSchedule = view.findViewById(R.id.cbIgnoreSchedule);
        cbOnDemand = view.findViewById(R.id.cbOnDemand);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbNotify = view.findViewById(R.id.cbNotify);
        tvNotifyPro = view.findViewById(R.id.tvNotifyPro);
        cbAutoSeen = view.findViewById(R.id.cbAutoSeen);
        cbLeaveServer = view.findViewById(R.id.cbLeaveServer);
        cbLeaveDeleted = view.findViewById(R.id.cbLeaveDeleted);
        cbLeaveDevice = view.findViewById(R.id.cbLeaveDevice);
        etMax = view.findViewById(R.id.etMax);
        etInterval = view.findViewById(R.id.etInterval);
        cbUnmetered = view.findViewById(R.id.cbUnmeteredOnly);
        cbVpnOnly = view.findViewById(R.id.cbVpnOnly);

        spLeft = view.findViewById(R.id.spLeft);
        spRight = view.findViewById(R.id.spRight);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        cbIdentity = view.findViewById(R.id.cbIdentity);

        tvError = view.findViewById(R.id.tvError);
        grpError = view.findViewById(R.id.grpError);

        pbWait = view.findViewById(R.id.pbWait);

        rgEncryption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int id) {
                etPort.setHint(id == R.id.radio_ssl ? "995" : "110");
            }
        });

        tvInsecureRemark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 4);
            }
        });

        tilPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // https://github.com/material-components/material-components-android/issues/503
                //if (TextUtils.isEmpty(s))
                //    tilPassword.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (tilPassword == null)
                    return;

                String password = s.toString();
                boolean warning = (Helper.containsWhiteSpace(password) ||
                        Helper.containsControlChars(password));
                tilPassword.setHelperText(
                        warning ? getString(R.string.title_setup_password_chars) : null);
                tilPassword.setHelperTextEnabled(warning);
            }
        });

        tvPasswordStorage.setPaintFlags(tvPasswordStorage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPasswordStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 37);
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
                cbIgnoreSchedule.setEnabled(checked);
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

        adapterSwipe = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, getSwipeActions());
        adapterSwipe.setDropDownViewResource(R.layout.spinner_item1_dropdown);

        spLeft.setAdapter(adapterSwipe);
        spRight.setAdapter(adapterSwipe);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave(false);
            }
        });

        setBackPressedCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Helper.isKeyboardVisible(view))
                    Helper.hideKeyboard(view);
                else
                    onSave(true);
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);

        tilPassword.setEndIconMode(id < 0 || Helper.isSecure(getContext()) ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);
        pbSave.setVisibility(View.GONE);
        grpError.setVisibility(View.GONE);

        return view;
    }

    private void onSave(boolean should) {
        Bundle args = new Bundle();
        args.putLong("id", id);

        int encryption;
        if (rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls)
            encryption = EmailService.ENCRYPTION_STARTTLS;
        else if (rgEncryption.getCheckedRadioButtonId() == R.id.radio_none)
            encryption = EmailService.ENCRYPTION_NONE;
        else
            encryption = EmailService.ENCRYPTION_SSL;

        args.putString("host", etHost.getText().toString().trim().replace(" ", ""));
        args.putInt("encryption", encryption);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putInt("auth", auth);
        args.putString("user", etUser.getText().toString());
        args.putString("password", tilPassword.getEditText().getText().toString());

        args.putString("name", etName.getText().toString());
        args.putString("category", etCategory.getText().toString());
        args.putInt("color", btnColor.getColor());

        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("ignore_schedule", cbIgnoreSchedule.isChecked());
        args.putBoolean("ondemand", cbOnDemand.isChecked());
        args.putBoolean("primary", cbPrimary.isChecked());
        args.putBoolean("notify", cbNotify.isChecked());
        args.putBoolean("auto_seen", cbAutoSeen.isChecked());

        args.putBoolean("leave_server", cbLeaveServer.isChecked());
        args.putBoolean("leave_deleted", cbLeaveDeleted.isChecked());
        args.putBoolean("leave_device", cbLeaveDevice.isChecked());
        args.putString("max", etMax.getText().toString());
        args.putString("interval", etInterval.getText().toString());
        args.putBoolean("unmetered", cbUnmetered.isChecked());
        args.putBoolean("vpn_only", cbVpnOnly.isChecked());

        args.putLong("left", ((EntityFolder) spLeft.getSelectedItem()).id);
        args.putLong("right", ((EntityFolder) spRight.getSelectedItem()).id);

        args.putBoolean("should", should);

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbSave.setVisibility(View.VISIBLE);
                grpError.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                saving = false;
                invalidateOptionsMenu();
                Helper.setViewsEnabled(view, true);
                pbSave.setVisibility(View.GONE);
            }

            @Override
            protected Boolean onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

                String host = args.getString("host");
                int encryption = args.getInt("encryption");
                boolean insecure = args.getBoolean("insecure");
                String port = args.getString("port");
                int auth = args.getInt("auth");
                String user = args.getString("user").trim();
                String password = args.getString("password");

                String name = args.getString("name");
                String category = args.getString("category");
                Integer color = args.getInt("color");

                boolean synchronize = args.getBoolean("synchronize");
                boolean ignore_schedule = args.getBoolean("ignore_schedule");
                boolean ondemand = args.getBoolean("ondemand");
                boolean primary = args.getBoolean("primary");
                boolean notify = args.getBoolean("notify");
                boolean auto_seen = args.getBoolean("auto_seen");
                boolean leave_server = args.getBoolean("leave_server");
                boolean leave_deleted = args.getBoolean("leave_deleted");
                boolean leave_device = args.getBoolean("leave_device");
                String max = args.getString("max");
                String interval = args.getString("interval");
                boolean unmetered = args.getBoolean("unmetered");
                boolean vpn_only = args.getBoolean("vpn_only");

                long left = args.getLong("left");
                long right = args.getLong("right");

                boolean pro = ActivityBilling.isPro(context);
                boolean should = args.getBoolean("should");

                int semi = host.indexOf(':');
                if (semi > 0 && host.indexOf(':', semi + 1) < 0)
                    host = host.substring(0, semi);

                if (TextUtils.isEmpty(host) && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (encryption == EmailService.ENCRYPTION_SSL ? "995" : "110");
                if (TextUtils.isEmpty(user) && !insecure && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (synchronize && TextUtils.isEmpty(password) && !insecure && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));
                if (TextUtils.isEmpty(interval))
                    interval = Integer.toString(EntityAccount.DEFAULT_POLL_INTERVAL);
                Integer max_messages = (TextUtils.isEmpty(max) ? null : Integer.parseInt(max));
                int poll_interval = Math.max(1, Integer.parseInt(interval));

                if (TextUtils.isEmpty(name))
                    name = user;
                if (TextUtils.isEmpty(category))
                    category = null;
                if (color == Color.TRANSPARENT || !pro)
                    color = null;
                if (!pro)
                    notify = false;

                long now = new Date().getTime();

                DB db = DB.getInstance(context);
                EntityAccount account = db.account().getAccount(id);

                JSONObject jconditions = new JSONObject();
                if (account != null && account.conditions != null)
                    try {
                        jconditions = new JSONObject(account.conditions);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                if (should) {
                    if (account == null)
                        return !TextUtils.isEmpty(host) && !TextUtils.isEmpty(user);

                    if (!Objects.equals(account.host, host))
                        return true;
                    if (!Objects.equals(account.encryption, encryption))
                        return true;
                    if (!Objects.equals(account.insecure, insecure))
                        return true;
                    if (!Objects.equals(account.port, Integer.parseInt(port)))
                        return true;
                    if (!Objects.equals(account.user, user))
                        return true;
                    if (!Objects.equals(account.password, password))
                        return true;
                    if (!Objects.equals(account.name, name))
                        return true;
                    if (!Objects.equals(account.category, category))
                        return true;
                    if (!Objects.equals(account.color, color))
                        return true;
                    if (!Objects.equals(account.synchronize, synchronize))
                        return true;
                    if (ignore_schedule != jconditions.optBoolean("ignore_schedule"))
                        return true;
                    if (!Objects.equals(account.ondemand, ondemand))
                        return true;
                    if (!Objects.equals(account.primary, account.synchronize && primary))
                        return true;
                    if (!Objects.equals(account.notify, notify))
                        return true;
                    if (!Objects.equals(account.auto_seen, auto_seen))
                        return true;
                    if (!Objects.equals(account.leave_on_server, leave_server))
                        return true;
                    if (!Objects.equals(account.leave_deleted, leave_deleted))
                        return true;
                    if (!Objects.equals(account.leave_on_device, leave_device))
                        return true;
                    if (!Objects.equals(account.max_messages, max_messages))
                        return true;
                    if (!Objects.equals(account.poll_interval, poll_interval))
                        return true;
                    if (unmetered != jconditions.optBoolean("unmetered"))
                        return true;
                    if (vpn_only != jconditions.optBoolean("vpn_only"))
                        return true;

                    if (!Objects.equals(account.swipe_left, left))
                        return true;
                    if (!Objects.equals(account.swipe_right, right))
                        return true;

                    return false;
                }

                boolean check = (synchronize && (account == null ||
                        !account.synchronize ||
                        account.error != null ||
                        !account.host.equals(host) ||
                        !account.encryption.equals(encryption) ||
                        !account.insecure.equals(insecure) ||
                        !account.port.equals(Integer.parseInt(port)) ||
                        !account.user.equals(user) ||
                        !account.password.equals(password) ||
                        BuildConfig.DEBUG));
                Log.i("Account check=" + check);

                Long last_connected = null;
                if (account != null && synchronize == account.synchronize)
                    last_connected = account.last_connected;

                // Check POP3 server
                if (check) {
                    String protocol = "pop3" + (encryption == EmailService.ENCRYPTION_SSL ? "s" : "");
                    try (EmailService iservice = new EmailService(
                            context, protocol, null, encryption, insecure, false,
                            EmailService.PURPOSE_CHECK, true)) {
                        iservice.connect(
                                host, Integer.parseInt(port),
                                auth, null,
                                user, password,
                                null, null);
                    }
                }

                boolean reschedule = (ignore_schedule != jconditions.optBoolean("ignore_schedule"));

                try {
                    db.beginTransaction();

                    if (account != null && !account.password.equals(password)) {
                        String root = UriHelper.getRootDomain(context, account.host);
                        String match = (root == null || root.equals(account.host) ? account.host : "%." + root);
                        int count = db.identity().setIdentityPassword(account.id, account.user, password, auth, match);
                        Log.i("Updated passwords=" + count + " match=" + match);
                    }

                    boolean update = (account != null);
                    if (account == null)
                        account = new EntityAccount();

                    account.protocol = EntityAccount.TYPE_POP;
                    account.host = host;
                    account.encryption = encryption;
                    account.insecure = insecure;
                    account.port = Integer.parseInt(port);
                    account.auth_type = auth;
                    account.user = user;
                    account.password = password;

                    account.name = name;
                    account.category = category;
                    account.color = color;

                    account.synchronize = synchronize;
                    jconditions.put("ignore_schedule", ignore_schedule);
                    account.ondemand = ondemand;
                    account.primary = (account.synchronize && primary);
                    account.notify = notify;
                    account.auto_seen = auto_seen;
                    account.leave_on_server = leave_server;
                    account.leave_deleted = leave_deleted;
                    account.leave_on_device = leave_device;
                    account.max_messages = max_messages;
                    account.poll_interval = poll_interval;

                    jconditions.put("unmetered", unmetered);
                    jconditions.put("vpn_only", vpn_only);
                    account.conditions = jconditions.toString();

                    account.swipe_left = left;
                    account.swipe_right = right;

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

                    args.putLong("account", account.id);
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

                    for (EntityFolder folder : EntityFolder.getPopFolders(context)) {
                        EntityFolder existing = db.folder().getFolderByType(account.id, folder.type);
                        if (existing == null) {
                            folder.account = account.id;
                            folder.id = db.folder().insertFolder(folder);
                            existing = folder;
                        }

                        if (account.synchronize && existing.synchronize)
                            EntityOperation.sync(context, existing.id, true);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (reschedule)
                    ServiceSynchronize.reschedule(context);
                else
                    ServiceSynchronize.eval(context, "POP3");

                if (!synchronize) {
                    NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                    nm.cancel("receive:" + account.id, NotificationHelper.NOTIFICATION_TAGGED);
                    nm.cancel("alert:" + account.id, NotificationHelper.NOTIFICATION_TAGGED);
                }

                args.putBoolean("saved", true);

                return false;
            }

            @Override
            protected void onExecuted(Bundle args, Boolean dirty) {
                if (dirty) {
                    Bundle aargs = new Bundle();
                    aargs.putString("question", getString(R.string.title_ask_save));

                    FragmentDialogAsk fragment = new FragmentDialogAsk();
                    fragment.setArguments(aargs);
                    fragment.setTargetFragment(FragmentPop.this, REQUEST_SAVE);
                    fragment.show(getParentFragmentManager(), "account:save");
                } else {
                    Context context = getContext();
                    if (context != null)
                        WidgetUnified.updateData(context); // Update color stripe

                    finish();

                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        boolean saved = args.getBoolean("saved");
                        if (saved && cbIdentity.isChecked()) {
                            Bundle aargs = new Bundle();
                            aargs.putLong("account", args.getLong("account"));

                            FragmentIdentity fragment = new FragmentIdentity();
                            fragment.setArguments(aargs);
                            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.leave_to_left);
                            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("identity");
                            fragmentTransaction.commit();
                        }
                    }
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else {
                    tvError.setText(Log.formatThrowable(ex, false));
                    grpError.setVisibility(View.VISIBLE);

                    getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                return;
                            scroll.smoothScrollTo(0, tvError.getBottom());
                        }
                    });
                }
            }
        }.execute(this, args, "account:save");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("fair:password", tilPassword == null ? null : tilPassword.getEditText().getText().toString());
        outState.putInt("fair:auth", auth);
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
                    JSONObject jcondition = new JSONObject();
                    try {
                        if (account != null && account.conditions != null)
                            jcondition = new JSONObject(account.conditions);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                    etHost.setText(account == null ? null : account.host);
                    etPort.setText(account == null ? null : Long.toString(account.port));

                    if (account != null && account.encryption == EmailService.ENCRYPTION_STARTTLS)
                        rgEncryption.check(R.id.radio_starttls);
                    else if (account != null && account.encryption == EmailService.ENCRYPTION_NONE)
                        rgEncryption.check(R.id.radio_none);
                    else
                        rgEncryption.check(R.id.radio_ssl);

                    cbInsecure.setChecked(account == null ? false : account.insecure);

                    etUser.setText(account == null ? null : account.user);
                    tilPassword.getEditText().setText(account == null ? null : account.password);

                    etName.setText(account == null ? null : account.name);
                    etCategory.setText(account == null ? null : account.category);
                    btnColor.setColor(account == null ? null : account.color);

                    cbSynchronize.setChecked(account == null ? true : account.synchronize);
                    cbIgnoreSchedule.setChecked(jcondition.optBoolean("ignore_schedule"));
                    cbOnDemand.setChecked(account == null ? false : account.ondemand);
                    cbPrimary.setChecked(account == null ? false : account.primary);

                    boolean pro = ActivityBilling.isPro(getContext());
                    cbNotify.setChecked(account != null && account.notify && pro);
                    cbNotify.setEnabled(pro);

                    cbAutoSeen.setChecked(account == null ? true : account.auto_seen);

                    cbLeaveServer.setChecked(account == null ? true : account.leave_on_server);
                    cbLeaveDeleted.setChecked(account == null ? true : account.leave_deleted);
                    cbLeaveDevice.setChecked(account == null ? false : account.leave_on_device);

                    if (account != null && account.max_messages == null)
                        etMax.setText(null);
                    else
                        etMax.setText(Integer.toString(account == null
                                ? EntityAccount.DEFAULT_MAX_MESSAGES : account.max_messages));

                    etInterval.setText(account == null ? "" : Long.toString(account.poll_interval));
                    cbUnmetered.setChecked(jcondition.optBoolean("unmetered"));
                    cbVpnOnly.setChecked(jcondition.optBoolean("vpn_only"));
                    cbIdentity.setChecked(account == null);

                    List<EntityFolder> folders = getSwipeActions();
                    for (int pos = 0; pos < folders.size(); pos++) {
                        EntityFolder folder = folders.get(pos);

                        if (account == null || account.swipe_left == null
                                ? EntityMessage.SWIPE_ACTION_DELETE.equals(folder.id)
                                : account.swipe_left.equals(folder.id))
                            spLeft.setSelection(pos);

                        if (account == null || account.swipe_right == null
                                ? EntityMessage.SWIPE_ACTION_SEEN.equals(folder.id)
                                : account.swipe_right.equals(folder.id))
                            spRight.setSelection(pos);
                    }

                    auth = (account == null ? AUTH_TYPE_PASSWORD : account.auth_type);

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
                    auth = savedInstanceState.getInt("fair:auth");
                }

                Helper.setViewsEnabled(view, true);

                if (auth != AUTH_TYPE_PASSWORD) {
                    etUser.setEnabled(false);
                    tilPassword.getEditText().setEnabled(false);
                }

                cbIgnoreSchedule.setEnabled(cbSynchronize.isChecked());
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
        if (item.getItemId() == R.id.menu_delete) {
            onMenuDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuDelete() {
        Bundle aargs = new Bundle();
        aargs.putString("question", getString(R.string.title_account_delete));
        aargs.putBoolean("warning", true);

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
                case REQUEST_SAVE:
                    if (resultCode == RESULT_OK) {
                        getMainHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                    return;
                                scroll.smoothScrollTo(0, btnSave.getBottom());
                            }
                        });
                        onSave(false);
                    } else
                        finish();
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
                finish();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "account:delete");
    }

    private List<EntityFolder> getSwipeActions() {
        List<EntityFolder> folders = new ArrayList<>();

        EntityFolder ask = new EntityFolder();
        ask.id = EntityMessage.SWIPE_ACTION_ASK;
        ask.name = getString(R.string.title_ask_what);
        folders.add(ask);

        EntityFolder seen = new EntityFolder();
        seen.id = EntityMessage.SWIPE_ACTION_SEEN;
        seen.name = getString(R.string.title_seen);
        folders.add(seen);

        EntityFolder flag = new EntityFolder();
        flag.id = EntityMessage.SWIPE_ACTION_FLAG;
        flag.name = getString(R.string.title_flag);
        folders.add(flag);

        EntityFolder snooze = new EntityFolder();
        snooze.id = EntityMessage.SWIPE_ACTION_SNOOZE;
        snooze.name = getString(R.string.title_snooze_now);
        folders.add(snooze);

        EntityFolder hide = new EntityFolder();
        hide.id = EntityMessage.SWIPE_ACTION_HIDE;
        hide.name = getString(R.string.title_hide);
        folders.add(hide);

        EntityFolder junk = new EntityFolder();
        junk.id = EntityMessage.SWIPE_ACTION_JUNK;
        junk.name = getString(R.string.title_report_spam);
        folders.add(junk);

        EntityFolder delete = new EntityFolder();
        delete.id = EntityMessage.SWIPE_ACTION_DELETE;
        delete.name = getString(R.string.title_delete_permanently);
        folders.add(delete);

        return folders;
    }
}
