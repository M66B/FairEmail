package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import static android.accounts.AccountManager.newChooseAccountIntent;

public class FragmentAccount extends FragmentEx {
    private ViewGroup view;

    private Spinner spProvider;
    private EditText etHost;
    private EditText etDomain;
    private Button btnAutoConfig;
    private EditText etPort;
    private Button btnAuthorize;
    private EditText etUser;
    private TextInputLayout tilPassword;

    private Button btnAdvanced;

    private TextView tvName;
    private EditText etName;
    private Button btnColor;
    private View vwColor;
    private ImageView ibColorDefault;
    private EditText etSignature;
    private ImageButton ibPro;

    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;
    private EditText etInterval;

    private Button btnCheck;
    private ProgressBar pbCheck;

    private TextView tvIdle;

    private ArrayAdapter<EntityFolder> adapter;
    private Spinner spDrafts;
    private Spinner spSent;
    private Spinner spAll;
    private Spinner spTrash;
    private Spinner spJunk;

    private Button btnSave;
    private ProgressBar pbSave;
    private ImageButton ibDelete;
    private ProgressBar pbWait;

    private Group grpServer;
    private Group grpAuthorize;
    private Group grpAdvanced;
    private Group grpFolders;

    private long id = -1;
    private int color = Color.TRANSPARENT;
    private String authorized = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        id = (args == null ? -1 : args.getLong("id", -1));
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_account);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_account, container, false);

        // Get controls
        spProvider = view.findViewById(R.id.spProvider);

        etDomain = view.findViewById(R.id.etDomain);
        btnAutoConfig = view.findViewById(R.id.btnAutoConfig);

        etHost = view.findViewById(R.id.etHost);
        etPort = view.findViewById(R.id.etPort);

        btnAuthorize = view.findViewById(R.id.btnAuthorize);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);

        btnAdvanced = view.findViewById(R.id.btnAdvanced);

        etName = view.findViewById(R.id.etName);
        tvName = view.findViewById(R.id.tvName);
        btnColor = view.findViewById(R.id.btnColor);
        vwColor = view.findViewById(R.id.vwColor);
        ibColorDefault = view.findViewById(R.id.ibColorDefault);
        etSignature = view.findViewById(R.id.etSignature);
        ibPro = view.findViewById(R.id.ibPro);

        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        etInterval = view.findViewById(R.id.etInterval);

        btnCheck = view.findViewById(R.id.btnCheck);
        pbCheck = view.findViewById(R.id.pbCheck);

        tvIdle = view.findViewById(R.id.tvIdle);

        spDrafts = view.findViewById(R.id.spDrafts);
        spSent = view.findViewById(R.id.spSent);
        spAll = view.findViewById(R.id.spAll);
        spTrash = view.findViewById(R.id.spTrash);
        spJunk = view.findViewById(R.id.spJunk);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);

        ibDelete = view.findViewById(R.id.ibDelete);

        pbWait = view.findViewById(R.id.pbWait);

        grpServer = view.findViewById(R.id.grpServer);
        grpAuthorize = view.findViewById(R.id.grpAuthorize);
        grpAdvanced = view.findViewById(R.id.grpAdvanced);
        grpFolders = view.findViewById(R.id.grpFolders);

        // Wire controls

        spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Provider provider = (Provider) adapterView.getSelectedItem();
                grpServer.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                grpAuthorize.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

                btnAuthorize.setVisibility(provider.type == null ? View.GONE : View.VISIBLE);

                btnAdvanced.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                if (position == 0)
                    grpAdvanced.setVisibility(View.GONE);

                btnCheck.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                tvIdle.setVisibility(View.GONE);
                grpFolders.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);

                Object tag = adapterView.getTag();
                if (tag != null && (Integer) tag == position)
                    return;
                adapterView.setTag(position);

                etHost.setText(provider.imap_host);
                etPort.setText(provider.imap_host == null ? null : Integer.toString(provider.imap_port));

                etUser.setText(null);
                tilPassword.getEditText().setText(null);

                etName.setText(position > 1 ? provider.name : null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnAutoConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etDomain.setEnabled(false);
                btnAutoConfig.setEnabled(false);

                Bundle args = new Bundle();
                args.putString("domain", etDomain.getText().toString());

                new SimpleTask<SRVRecord>() {
                    @Override
                    protected SRVRecord onLoad(Context context, Bundle args) throws Throwable {
                        String domain = args.getString("domain");
                        Record[] records = new Lookup("_imaps._tcp." + domain, Type.SRV).run();
                        if (records != null)
                            for (int i = 0; i < records.length; i++) {
                                SRVRecord srv = (SRVRecord) records[i];
                                Log.i(Helper.TAG, "SRV=" + srv);
                                return srv;
                            }

                        throw new IllegalArgumentException(getString(R.string.title_no_settings));
                    }

                    @Override
                    protected void onLoaded(Bundle args, SRVRecord srv) {
                        etDomain.setEnabled(true);
                        btnAutoConfig.setEnabled(true);
                        if (srv != null) {
                            etHost.setText(srv.getTarget().toString(true));
                            etPort.setText(Integer.toString(srv.getPort()));
                        }
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        etDomain.setEnabled(true);
                        btnAutoConfig.setEnabled(true);
                        if (ex instanceof IllegalArgumentException)
                            Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                        else
                            Helper.unexpectedError(getContext(), ex);
                    }
                }.load(FragmentAccount.this, args);
            }
        });

        tilPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (authorized != null && !authorized.equals(s.toString()))
                    authorized = null;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnAuthorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String permission = Manifest.permission.GET_ACCOUNTS;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O &&
                        ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.i(Helper.TAG, "Requesting " + permission);
                    requestPermissions(new String[]{permission}, ActivitySetup.REQUEST_PERMISSION);
                } else
                    selectAccount();
            }
        });

        btnAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = (grpAdvanced.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                grpAdvanced.setVisibility(visibility);
                if (visibility == View.VISIBLE)
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            ((ScrollView) view).smoothScrollTo(0, tvName.getTop());
                        }
                    });
            }
        });

        vwColor.setBackgroundColor(color);
        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Helper.isPro(getContext())) {
                    int[] colors = getContext().getResources().getIntArray(R.array.colorPicker);
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
                    colorPickerDialog.initialize(R.string.title_account_color, colors, color, 4, colors.length);
                    colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int color) {
                            setColor(color);
                        }
                    });
                    colorPickerDialog.show(getFragmentManager(), "colorpicker");
                } else {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.hide(FragmentAccount.this);
                    fragmentTransaction.add(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                    fragmentTransaction.commit();
                }
            }
        });

        ibColorDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(Color.TRANSPARENT);
            }
        });

        ibPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.hide(FragmentAccount.this);
                fragmentTransaction.add(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                fragmentTransaction.commit();
            }
        });

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbPrimary.setEnabled(checked);
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.setViewsEnabled(view, false);
                btnAuthorize.setEnabled(false);
                btnCheck.setEnabled(false);
                pbCheck.setVisibility(View.VISIBLE);
                tvIdle.setVisibility(View.GONE);
                grpFolders.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);

                Provider provider = (Provider) spProvider.getSelectedItem();

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putString("host", etHost.getText().toString());
                args.putString("port", etPort.getText().toString());
                args.putString("user", etUser.getText().toString());
                args.putString("password", tilPassword.getEditText().getText().toString());
                args.putInt("auth_type", authorized == null ? Helper.AUTH_TYPE_PASSWORD : provider.getAuthType());

                new SimpleTask<CheckResult>() {
                    @Override
                    protected CheckResult onLoad(Context context, Bundle args) throws Throwable {
                        long id = args.getLong("id");
                        String host = args.getString("host");
                        String port = args.getString("port");
                        String user = args.getString("user");
                        String password = args.getString("password");
                        int auth_type = args.getInt("auth_type");

                        if (TextUtils.isEmpty(host))
                            throw new Throwable(getContext().getString(R.string.title_no_host));
                        if (TextUtils.isEmpty(port))
                            port = "993";
                        if (TextUtils.isEmpty(user))
                            throw new Throwable(getContext().getString(R.string.title_no_user));
                        if (TextUtils.isEmpty(password))
                            throw new Throwable(getContext().getString(R.string.title_no_password));

                        CheckResult result = new CheckResult();
                        result.folders = new ArrayList<>();

                        // Check IMAP server / get folders
                        Properties props = MessageHelper.getSessionProperties(auth_type);
                        Session isession = Session.getInstance(props, null);
                        isession.setDebug(true);
                        IMAPStore istore = null;
                        try {
                            istore = (IMAPStore) isession.getStore("imaps");
                            try {
                                istore.connect(host, Integer.parseInt(port), user, password);
                            } catch (AuthenticationFailedException ex) {
                                if (auth_type == Helper.AUTH_TYPE_GMAIL) {
                                    password = Helper.refreshToken(context, "com.google", user, password);
                                    istore.connect(host, Integer.parseInt(port), user, password);
                                } else
                                    throw ex;
                            }

                            if (!istore.hasCapability("UIDPLUS"))
                                throw new MessagingException(getContext().getString(R.string.title_no_uidplus));

                            result.idle = istore.hasCapability("IDLE");

                            for (Folder ifolder : istore.getDefaultFolder().list("*")) {
                                String type = null;

                                // First check folder attributes
                                boolean selectable = true;
                                String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                                for (String attr : attrs) {
                                    if ("\\Noselect".equals(attr))
                                        selectable = false;
                                    if (attr.startsWith("\\")) {
                                        int index = EntityFolder.SYSTEM_FOLDER_ATTR.indexOf(attr.substring(1));
                                        if (index >= 0) {
                                            type = EntityFolder.SYSTEM_FOLDER_TYPE.get(index);
                                            break;
                                        }
                                    }
                                }

                                if (selectable) {
                                    // Next check folder full name
                                    if (type == null) {
                                        String fullname = ifolder.getFullName();
                                        for (String attr : EntityFolder.SYSTEM_FOLDER_ATTR)
                                            if (attr.equals(fullname)) {
                                                int index = EntityFolder.SYSTEM_FOLDER_ATTR.indexOf(attr);
                                                type = EntityFolder.SYSTEM_FOLDER_TYPE.get(index);
                                                break;
                                            }
                                    }

                                    // Create entry
                                    DB db = DB.getInstance(context);
                                    EntityFolder folder = db.folder().getFolderByName(id, ifolder.getFullName());
                                    if (folder == null) {
                                        folder = new EntityFolder();
                                        folder.name = ifolder.getFullName();
                                        folder.type = (type == null ? EntityFolder.USER : type);
                                        folder.synchronize = (type != null && EntityFolder.SYSTEM_FOLDER_SYNC.contains(type));
                                        folder.after = (type == null ? EntityFolder.DEFAULT_USER_SYNC : EntityFolder.DEFAULT_SYSTEM_SYNC);
                                    }
                                    result.folders.add(folder);

                                    Log.i(Helper.TAG, folder.name + " id=" + folder.id +
                                            " type=" + folder.type + " attr=" + TextUtils.join(",", attrs));
                                }
                            }

                        } finally {
                            if (istore != null)
                                istore.close();
                        }

                        return result;
                    }

                    @Override
                    protected void onLoaded(Bundle args, CheckResult result) {
                        Helper.setViewsEnabled(view, true);
                        btnAuthorize.setEnabled(true);
                        btnCheck.setEnabled(true);
                        pbCheck.setVisibility(View.GONE);

                        tvIdle.setVisibility(result.idle ? View.GONE : View.VISIBLE);

                        setFolders(result.folders);

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                ((ScrollView) view).smoothScrollTo(0, btnSave.getBottom());
                            }
                        });
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.setViewsEnabled(view, true);
                        btnAuthorize.setEnabled(true);
                        btnCheck.setEnabled(true);
                        pbCheck.setVisibility(View.GONE);
                        grpFolders.setVisibility(View.GONE);
                        btnSave.setVisibility(View.GONE);

                        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                                .setMessage(Helper.formatThrowable(ex))
                                .setPositiveButton(android.R.string.cancel, null)
                                .create()
                                .show();
                    }
                }.load(FragmentAccount.this, args);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.setViewsEnabled(view, false);
                btnAuthorize.setEnabled(false);
                btnCheck.setEnabled(false);
                btnSave.setEnabled(false);
                pbSave.setVisibility(View.VISIBLE);

                Provider provider = (Provider) spProvider.getSelectedItem();

                EntityFolder drafts = (EntityFolder) spDrafts.getSelectedItem();
                EntityFolder sent = (EntityFolder) spSent.getSelectedItem();
                EntityFolder all = (EntityFolder) spAll.getSelectedItem();
                EntityFolder trash = (EntityFolder) spTrash.getSelectedItem();
                EntityFolder junk = (EntityFolder) spJunk.getSelectedItem();

                if (drafts != null && drafts.type == null)
                    drafts = null;
                if (sent != null && sent.type == null)
                    sent = null;
                if (all != null && all.type == null)
                    all = null;
                if (trash != null && trash.type == null)
                    trash = null;
                if (junk != null && junk.type == null)
                    junk = null;

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putString("host", etHost.getText().toString());
                args.putString("port", etPort.getText().toString());
                args.putString("user", etUser.getText().toString());
                args.putString("password", tilPassword.getEditText().getText().toString());
                args.putInt("auth_type", authorized == null ? Helper.AUTH_TYPE_PASSWORD : provider.getAuthType());

                args.putString("name", etName.getText().toString());
                args.putInt("color", color);
                args.putString("signature", Html.toHtml(etSignature.getText()));

                args.putBoolean("synchronize", cbSynchronize.isChecked());
                args.putBoolean("primary", cbPrimary.isChecked());
                args.putString("interval", etInterval.getText().toString());

                args.putSerializable("drafts", drafts);
                args.putSerializable("sent", sent);
                args.putSerializable("all", all);
                args.putSerializable("trash", trash);
                args.putSerializable("junk", junk);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) throws Throwable {
                        String host = args.getString("host");
                        String port = args.getString("port");
                        String user = args.getString("user");
                        String password = args.getString("password");
                        int auth_type = args.getInt("auth_type");

                        String name = args.getString("name");
                        Integer color = args.getInt("color");
                        String signature = args.getString("signature");

                        boolean synchronize = args.getBoolean("synchronize");
                        boolean primary = args.getBoolean("primary");
                        String interval = args.getString("interval");

                        EntityFolder drafts = (EntityFolder) args.getSerializable("drafts");
                        EntityFolder sent = (EntityFolder) args.getSerializable("sent");
                        EntityFolder all = (EntityFolder) args.getSerializable("all");
                        EntityFolder trash = (EntityFolder) args.getSerializable("trash");
                        EntityFolder junk = (EntityFolder) args.getSerializable("junk");

                        if (TextUtils.isEmpty(host))
                            throw new Throwable(getContext().getString(R.string.title_no_host));
                        if (TextUtils.isEmpty(port))
                            port = "993";
                        if (TextUtils.isEmpty(user))
                            throw new Throwable(getContext().getString(R.string.title_no_user));
                        if (TextUtils.isEmpty(password))
                            throw new Throwable(getContext().getString(R.string.title_no_password));
                        if (TextUtils.isEmpty(interval))
                            interval = "9";
                        if (synchronize && drafts == null)
                            throw new Throwable(getContext().getString(R.string.title_no_drafts));
                        if (Color.TRANSPARENT == color)
                            color = null;

                        // Check IMAP server
                        if (synchronize) {
                            Session isession = Session.getInstance(MessageHelper.getSessionProperties(auth_type), null);
                            isession.setDebug(true);
                            IMAPStore istore = null;
                            try {
                                istore = (IMAPStore) isession.getStore("imaps");
                                try {
                                    istore.connect(host, Integer.parseInt(port), user, password);
                                } catch (AuthenticationFailedException ex) {
                                    if (auth_type == Helper.AUTH_TYPE_GMAIL) {
                                        password = Helper.refreshToken(context, "com.google", user, password);
                                        istore.connect(host, Integer.parseInt(port), user, password);
                                    } else
                                        throw ex;
                                }

                                if (!istore.hasCapability("UIDPLUS"))
                                    throw new MessagingException(getContext().getString(R.string.title_no_uidplus));
                            } finally {
                                if (istore != null)
                                    istore.close();
                            }
                        }

                        if (TextUtils.isEmpty(name))
                            name = user;

                        DB db = DB.getInstance(getContext());
                        try {
                            db.beginTransaction();

                            EntityAccount account = db.account().getAccount(args.getLong("id"));
                            boolean update = (account != null);
                            if (account == null)
                                account = new EntityAccount();

                            account.host = host;
                            account.port = Integer.parseInt(port);
                            account.user = user;
                            account.password = password;
                            account.auth_type = auth_type;

                            account.name = name;
                            account.color = color;
                            account.signature = signature;

                            account.synchronize = synchronize;
                            account.primary = (account.synchronize && primary);
                            account.poll_interval = Integer.parseInt(interval);

                            account.store_sent = false; // obsolete
                            account.seen_until = null; // obsolete

                            if (!synchronize)
                                account.error = null;

                            if (account.primary)
                                db.account().resetPrimary();

                            if (update)
                                db.account().updateAccount(account);
                            else
                                account.id = db.account().insertAccount(account);

                            List<EntityFolder> folders = new ArrayList<>();

                            EntityFolder inbox = new EntityFolder();
                            inbox.name = "INBOX";
                            inbox.type = EntityFolder.INBOX;
                            inbox.synchronize = true;
                            inbox.unified = true;
                            inbox.after = EntityFolder.DEFAULT_INBOX_SYNC;

                            folders.add(inbox);

                            if (drafts != null) {
                                drafts.type = EntityFolder.DRAFTS;
                                folders.add(drafts);
                            }

                            if (sent != null) {
                                sent.type = EntityFolder.SENT;
                                folders.add(sent);
                            }
                            if (all != null) {
                                all.type = EntityFolder.ARCHIVE;
                                folders.add(all);
                            }
                            if (trash != null) {
                                trash.type = EntityFolder.TRASH;
                                folders.add(trash);
                            }
                            if (junk != null) {
                                junk.type = EntityFolder.JUNK;
                                folders.add(junk);
                            }

                            for (EntityFolder folder : folders) {
                                db.folder().setFolderUser(account.id, folder.type);
                                EntityFolder existing = db.folder().getFolderByName(account.id, folder.name);
                                if (existing == null) {
                                    folder.account = account.id;
                                    Log.i(Helper.TAG, "Creating folder=" + folder.name + " (" + folder.type + ")");
                                    folder.id = db.folder().insertFolder(folder);
                                } else
                                    db.folder().setFolderType(existing.id, folder.type);
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        ServiceSynchronize.reload(getContext(), "save account");

                        return null;
                    }

                    @Override
                    protected void onLoaded(Bundle args, Void data) {
                        getFragmentManager().popBackStack();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.setViewsEnabled(view, true);
                        btnAuthorize.setEnabled(true);
                        btnCheck.setEnabled(true);
                        btnSave.setEnabled(true);
                        pbSave.setVisibility(View.GONE);

                        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                                .setMessage(Helper.formatThrowable(ex))
                                .setPositiveButton(android.R.string.cancel, null)
                                .create()
                                .show();
                    }
                }.load(FragmentAccount.this, args);
            }
        });

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                        .setMessage(R.string.title_account_delete)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Helper.setViewsEnabled(view, false);
                                btnAuthorize.setEnabled(false);
                                btnCheck.setEnabled(false);
                                btnSave.setEnabled(false);
                                pbWait.setVisibility(View.VISIBLE);

                                Bundle args = new Bundle();
                                args.putLong("id", id);

                                new SimpleTask<Void>() {
                                    @Override
                                    protected Void onLoad(Context context, Bundle args) {
                                        long id = args.getLong("id");
                                        DB.getInstance(context).account().deleteAccount(id);
                                        ServiceSynchronize.reload(getContext(), "delete account");
                                        return null;
                                    }

                                    @Override
                                    protected void onLoaded(Bundle args, Void data) {
                                        getFragmentManager().popBackStack();
                                    }

                                    @Override
                                    protected void onException(Bundle args, Throwable ex) {
                                        Helper.unexpectedError(getContext(), ex);
                                    }
                                }.load(FragmentAccount.this, args);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, new ArrayList<EntityFolder>());
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spDrafts.setAdapter(adapter);
        spSent.setAdapter(adapter);
        spAll.setAdapter(adapter);
        spTrash.setAdapter(adapter);
        spJunk.setAdapter(adapter);

        // Initialize
        Helper.setViewsEnabled(view, false);
        btnAuthorize.setVisibility(View.GONE);
        tilPassword.setPasswordVisibilityToggleEnabled(id < 0);

        btnAdvanced.setVisibility(View.GONE);

        tvIdle.setVisibility(View.GONE);

        btnCheck.setVisibility(View.GONE);
        pbCheck.setVisibility(View.GONE);

        btnSave.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);

        ibDelete.setVisibility(View.GONE);

        grpServer.setVisibility(View.GONE);
        grpAuthorize.setVisibility(View.GONE);
        grpAdvanced.setVisibility(View.GONE);
        grpFolders.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("provider", spProvider.getSelectedItemPosition());
        outState.putString("authorized", authorized);
        outState.putString("password", tilPassword.getEditText().getText().toString());
        outState.putInt("advanced", grpAdvanced.getVisibility());
        outState.putInt("color", color);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DB db = DB.getInstance(getContext());

        // Observe
        db.account().liveAccount(id).observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
            private boolean once = false;

            @Override
            public void onChanged(@Nullable EntityAccount account) {
                if (once)
                    return;
                once = true;

                // Get providers
                List<Provider> providers = Provider.loadProfiles(getContext());
                providers.add(0, new Provider(getString(R.string.title_select)));
                providers.add(1, new Provider(getString(R.string.title_custom)));

                ArrayAdapter<Provider> padapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, providers);
                padapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spProvider.setAdapter(padapter);

                if (savedInstanceState == null) {
                    if (account != null) {
                        boolean found = false;
                        for (int pos = 2; pos < providers.size(); pos++) {
                            Provider provider = providers.get(pos);
                            if (provider.imap_host.equals(account.host) &&
                                    provider.imap_port == account.port) {
                                found = true;
                                spProvider.setTag(pos);
                                spProvider.setSelection(pos);
                                break;
                            }
                        }
                        if (!found) {
                            spProvider.setTag(1);
                            spProvider.setSelection(1);
                        }
                        etHost.setText(account.host);
                        etPort.setText(Long.toString(account.port));
                    }

                    authorized = (account != null && account.auth_type != Helper.AUTH_TYPE_PASSWORD ? account.password : null);
                    etUser.setText(account == null ? null : account.user);
                    tilPassword.getEditText().setText(account == null ? null : account.password);

                    etName.setText(account == null ? null : account.name);
                    etSignature.setText(account == null || account.signature == null ? null : Html.fromHtml(account.signature));

                    cbSynchronize.setChecked(account == null ? true : account.synchronize);
                    cbPrimary.setChecked(account == null ? true : account.primary);
                    etInterval.setText(Long.toString(account == null ? 9 : account.poll_interval));

                    color = (account == null || account.color == null ? Color.TRANSPARENT : account.color);

                    if (account == null)
                        new SimpleTask<Integer>() {
                            @Override
                            protected Integer onLoad(Context context, Bundle args) {
                                return DB.getInstance(context).account().getSynchronizingAccountCount();
                            }

                            @Override
                            protected void onLoaded(Bundle args, Integer count) {
                                cbPrimary.setChecked(count == 0);
                            }
                        }.load(FragmentAccount.this, new Bundle());
                } else {
                    int provider = savedInstanceState.getInt("provider");
                    spProvider.setTag(provider);
                    spProvider.setSelection(provider);

                    authorized = savedInstanceState.getString("authorized");
                    tilPassword.getEditText().setText(savedInstanceState.getString("password"));
                    grpAdvanced.setVisibility(savedInstanceState.getInt("advanced"));
                    color = savedInstanceState.getInt("color");
                }

                Helper.setViewsEnabled(view, true);

                setColor(color);

                boolean pro = Helper.isPro(getContext());
                etSignature.setHint(pro ? R.string.title_optional : R.string.title_pro_feature);
                etSignature.setEnabled(pro);
                if (pro) {
                    ViewGroup.LayoutParams lp = ibPro.getLayoutParams();
                    lp.height = 0;
                    lp.width = 0;
                    ibPro.setLayoutParams(lp);
                }

                cbPrimary.setEnabled(cbSynchronize.isChecked());

                // Consider previous check/save/delete as cancelled
                ibDelete.setVisibility(account == null ? View.GONE : View.VISIBLE);
                pbWait.setVisibility(View.GONE);

                if (account != null) {
                    db.folder().liveFolders(account.id).observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
                        @Override
                        public void onChanged(final List<TupleFolderEx> _folders) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    List<EntityFolder> folders = new ArrayList<>();
                                    if (_folders != null)
                                        folders.addAll(_folders);
                                    setFolders(folders);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void selectAccount() {
        Log.i(Helper.TAG, "Select account");
        Provider provider = (Provider) spProvider.getSelectedItem();
        if (provider.type != null)
            startActivityForResult(newChooseAccountIntent(
                    null,
                    null,
                    new String[]{provider.type},
                    null,
                    null,
                    null,
                    null), ActivitySetup.REQUEST_CHOOSE_ACCOUNT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == ActivitySetup.REQUEST_PERMISSION)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectAccount();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(Helper.TAG, "Activity result request=" + requestCode + " result=" + resultCode + " data=" + data);
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == ActivitySetup.REQUEST_CHOOSE_ACCOUNT) {
                String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

                AccountManager am = AccountManager.get(getContext());
                Account[] accounts = am.getAccountsByType(type);
                Log.i(Helper.TAG, "Accounts=" + accounts.length);
                for (final Account account : accounts)
                    if (name.equals(account.name)) {
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
                                            Log.i(Helper.TAG, "Got token");

                                            authorized = token;
                                            etUser.setText(account.name);
                                            tilPassword.getEditText().setText(token);
                                        } catch (Throwable ex) {
                                            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                            snackbar.setText(Helper.formatThrowable(ex));
                                        } finally {
                                            snackbar.dismiss();
                                        }
                                    }
                                },
                                null);
                        break;
                    }
            }
    }

    private void setColor(int color) {
        FragmentAccount.this.color = color;

        GradientDrawable border = new GradientDrawable();
        border.setColor(color);
        border.setStroke(1, Helper.resolveColor(getContext(), R.attr.colorSeparator));
        vwColor.setBackground(border);
    }

    private void setFolders(List<EntityFolder> folders) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(folders, new Comparator<EntityFolder>() {
            @Override
            public int compare(EntityFolder f1, EntityFolder f2) {
                int s = Integer.compare(
                        EntityFolder.FOLDER_SORT_ORDER.indexOf(f1.type),
                        EntityFolder.FOLDER_SORT_ORDER.indexOf(f2.type));
                if (s != 0)
                    return s;
                int c = -f1.synchronize.compareTo(f2.synchronize);
                if (c != 0)
                    return c;
                return collator.compare(
                        f1.name == null ? "" : f1.name,
                        f2.name == null ? "" : f2.name);
            }
        });

        EntityFolder none = new EntityFolder();
        none.name = "";
        folders.add(0, none);

        adapter.clear();
        adapter.addAll(folders);

        for (int pos = 0; pos < folders.size(); pos++) {
            if (EntityFolder.DRAFTS.equals(folders.get(pos).type))
                spDrafts.setSelection(pos);
            else if (EntityFolder.SENT.equals(folders.get(pos).type))
                spSent.setSelection(pos);
            else if (EntityFolder.ARCHIVE.equals(folders.get(pos).type))
                spAll.setSelection(pos);
            else if (EntityFolder.TRASH.equals(folders.get(pos).type))
                spTrash.setSelection(pos);
            else if (EntityFolder.JUNK.equals(folders.get(pos).type))
                spJunk.setSelection(pos);
        }

        grpFolders.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
    }

    private class CheckResult {
        List<EntityFolder> folders;
        boolean idle;
    }
}
