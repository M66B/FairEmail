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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import static android.accounts.AccountManager.newChooseAccountIntent;

public class FragmentAccount extends FragmentEx {
    private ViewGroup view;
    private EditText etName;
    private Spinner spProvider;
    private EditText etHost;
    private EditText etPort;
    private Button btnAuthorize;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private TextView tvLink;
    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;
    private EditText etInterval;
    private Button btnCheck;
    private ProgressBar pbCheck;
    private Spinner spDrafts;
    private Spinner spSent;
    private Spinner spAll;
    private Spinner spTrash;
    private Spinner spJunk;
    private Button btnSave;
    private ProgressBar pbSave;
    private ImageButton ibDelete;
    private ProgressBar pbWait;
    private Group grpInstructions;
    private Group grpInterval;
    private Group grpFolders;

    private long id = -1;
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
        etName = view.findViewById(R.id.etName);
        etHost = view.findViewById(R.id.etHost);
        btnAuthorize = view.findViewById(R.id.btnAuthorize);
        etPort = view.findViewById(R.id.etPort);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvLink = view.findViewById(R.id.tvLink);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        etInterval = view.findViewById(R.id.etInterval);
        btnCheck = view.findViewById(R.id.btnCheck);
        pbCheck = view.findViewById(R.id.pbCheck);
        spDrafts = view.findViewById(R.id.spDrafts);
        spSent = view.findViewById(R.id.spSent);
        spAll = view.findViewById(R.id.spAll);
        spTrash = view.findViewById(R.id.spTrash);
        spJunk = view.findViewById(R.id.spJunk);
        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        ibDelete = view.findViewById(R.id.ibDelete);
        pbWait = view.findViewById(R.id.pbWait);
        grpInstructions = view.findViewById(R.id.grpInstructions);
        grpInterval = view.findViewById(R.id.grpInterval);
        grpFolders = view.findViewById(R.id.grpFolders);

        // Wire controls

        spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Integer tag = (Integer) adapterView.getTag();
                if (tag != null && tag.equals(position))
                    return;
                adapterView.setTag(position);

                Provider provider = (Provider) adapterView.getSelectedItem();

                etName.setText(provider.name);

                btnAuthorize.setVisibility(provider.type == null ? View.GONE : View.VISIBLE);
                if (authorized != null) {
                    authorized = null;
                    etUser.setText(null);
                    tilPassword.getEditText().setText(null);
                }

                etHost.setText(provider.imap_host);
                etPort.setText(position == 0 ? null : Integer.toString(provider.imap_port));

                tvLink.setText(Html.fromHtml("<a href=\"" + provider.link + "\">" + provider.link + "</a>"));
                grpInstructions.setVisibility(provider.link == null ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
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

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbPrimary.setEnabled(checked);
                btnCheck.setVisibility(checked ? View.VISIBLE : View.GONE);
                btnSave.setVisibility(checked ? View.GONE : View.VISIBLE);
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.setViewsEnabled(view, false);
                btnAuthorize.setEnabled(false);
                btnCheck.setEnabled(false);
                pbCheck.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.GONE);
                grpFolders.setVisibility(View.GONE);

                Provider provider = (Provider) spProvider.getSelectedItem();

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putString("name", etName.getText().toString());
                args.putString("host", etHost.getText().toString());
                args.putString("port", etPort.getText().toString());
                args.putString("user", etUser.getText().toString());
                args.putString("password", tilPassword.getEditText().getText().toString());
                args.putInt("auth_type", authorized == null ? Helper.AUTH_TYPE_PASSWORD : provider.getAuthType());
                args.putBoolean("synchronize", cbSynchronize.isChecked());
                args.putBoolean("primary", cbPrimary.isChecked());

                new SimpleTask<List<EntityFolder>>() {
                    @Override
                    protected List<EntityFolder> onLoad(Context context, Bundle args) throws Throwable {
                        long id = args.getLong("id");
                        String host = args.getString("host");
                        String port = args.getString("port");
                        String user = args.getString("user");
                        String password = args.getString("password");
                        int auth_type = args.getInt("auth_type");

                        if (TextUtils.isEmpty(host))
                            throw new Throwable(getContext().getString(R.string.title_no_host));
                        if (TextUtils.isEmpty(port))
                            throw new Throwable(getContext().getString(R.string.title_no_port));
                        if (TextUtils.isEmpty(user))
                            throw new Throwable(getContext().getString(R.string.title_no_user));
                        if (TextUtils.isEmpty(password))
                            throw new Throwable(getContext().getString(R.string.title_no_password));

                        // Refresh token
                        if (id >= 0 && auth_type == Helper.AUTH_TYPE_GMAIL) {
                            password = Helper.refreshToken(getContext(), "com.google", user, password);
                            args.putString("password", password);
                        }

                        // Check IMAP server / get folders
                        List<EntityFolder> folders = new ArrayList<>();
                        Properties props = MessageHelper.getSessionProperties(context, auth_type);
                        Session isession = Session.getInstance(props, null);
                        isession.setDebug(true);
                        IMAPStore istore = null;
                        try {
                            istore = (IMAPStore) isession.getStore("imaps");
                            istore.connect(host, Integer.parseInt(port), user, password);

                            if (!istore.hasCapability("IDLE"))
                                throw new MessagingException(getContext().getString(R.string.title_no_idle));

                            if (!istore.hasCapability("UIDPLUS"))
                                throw new MessagingException(getContext().getString(R.string.title_no_uidplus));

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
                                    DB db = DB.getInstance(getContext());
                                    EntityFolder folder = db.folder().getFolderByName(id, ifolder.getFullName());
                                    if (folder == null) {
                                        folder = new EntityFolder();
                                        folder.name = ifolder.getFullName();
                                        folder.type = (type == null ? EntityFolder.USER : type);
                                        folder.synchronize = (type != null && EntityFolder.SYSTEM_FOLDER_SYNC.contains(type));
                                        folder.after = (type == null ? EntityFolder.DEFAULT_USER_SYNC : EntityFolder.DEFAULT_SYSTEM_SYNC);
                                    }
                                    folders.add(folder);

                                    Log.i(Helper.TAG, folder.name + " id=" + folder.id +
                                            " type=" + folder.type + " attr=" + TextUtils.join(",", attrs));
                                }
                            }

                        } finally {
                            if (istore != null)
                                istore.close();
                        }

                        return folders;
                    }

                    @Override
                    protected void onLoaded(Bundle args, List<EntityFolder> folders) {
                        Helper.setViewsEnabled(view, true);
                        btnAuthorize.setEnabled(true);
                        btnCheck.setEnabled(true);
                        pbCheck.setVisibility(View.GONE);

                        // Refreshed token
                        tilPassword.getEditText().setText(args.getString("password"));

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

                        ArrayAdapter<EntityFolder> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, folders);
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

                        spDrafts.setAdapter(adapter);
                        spSent.setAdapter(adapter);
                        spAll.setAdapter(adapter);
                        spTrash.setAdapter(adapter);
                        spJunk.setAdapter(adapter);

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
                        Toast.makeText(getContext(), Helper.formatThrowable(ex), Toast.LENGTH_LONG).show();
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
                args.putString("name", etName.getText().toString());
                args.putString("host", etHost.getText().toString());
                args.putString("port", etPort.getText().toString());
                args.putString("user", etUser.getText().toString());
                args.putString("password", tilPassword.getEditText().getText().toString());
                args.putInt("auth_type", authorized == null ? Helper.AUTH_TYPE_PASSWORD : provider.getAuthType());
                args.putBoolean("synchronize", cbSynchronize.isChecked());
                args.putBoolean("primary", cbPrimary.isChecked());
                args.putString("poll_interval", etInterval.getText().toString());
                args.putParcelable("drafts", drafts);
                args.putParcelable("sent", sent);
                args.putParcelable("all", all);
                args.putParcelable("trash", trash);
                args.putParcelable("junk", junk);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) throws Throwable {
                        String name = args.getString("name");
                        String host = args.getString("host");
                        String port = args.getString("port");
                        String user = args.getString("user");
                        String password = args.getString("password");
                        int auth_type = args.getInt("auth_type");
                        boolean synchronize = args.getBoolean("synchronize");
                        boolean primary = args.getBoolean("primary");
                        String poll_interval = args.getString("poll_interval");
                        EntityFolder drafts = args.getParcelable("drafts");
                        EntityFolder sent = args.getParcelable("sent");
                        EntityFolder all = args.getParcelable("all");
                        EntityFolder trash = args.getParcelable("trash");
                        EntityFolder junk = args.getParcelable("junk");

                        if (TextUtils.isEmpty(host))
                            throw new Throwable(getContext().getString(R.string.title_no_host));
                        if (TextUtils.isEmpty(port))
                            throw new Throwable(getContext().getString(R.string.title_no_port));
                        if (TextUtils.isEmpty(user))
                            throw new Throwable(getContext().getString(R.string.title_no_user));
                        if (TextUtils.isEmpty(password))
                            throw new Throwable(getContext().getString(R.string.title_no_password));
                        if (synchronize && drafts == null)
                            throw new Throwable(getContext().getString(R.string.title_no_drafts));

                        if (TextUtils.isEmpty(poll_interval))
                            poll_interval = "9";

                        // Check IMAP server
                        if (synchronize) {
                            Session isession = Session.getInstance(MessageHelper.getSessionProperties(context, auth_type), null);
                            isession.setDebug(true);
                            IMAPStore istore = null;
                            try {
                                istore = (IMAPStore) isession.getStore("imaps");
                                istore.connect(host, Integer.parseInt(port), user, password);

                                if (!istore.hasCapability("UIDPLUS"))
                                    throw new MessagingException(getContext().getString(R.string.title_no_uidplus));
                            } finally {
                                if (istore != null)
                                    istore.close();
                            }
                        }

                        if (TextUtils.isEmpty(name))
                            name = host + "/" + user;

                        DB db = DB.getInstance(getContext());
                        try {
                            db.beginTransaction();

                            EntityAccount account = db.account().getAccount(args.getLong("id"));
                            boolean update = (account != null);
                            if (account == null)
                                account = new EntityAccount();
                            account.name = name;
                            account.host = host;
                            account.port = Integer.parseInt(port);
                            account.user = user;
                            account.password = password;
                            account.auth_type = auth_type;
                            account.synchronize = synchronize;
                            account.primary = (account.synchronize && primary);
                            account.store_sent = false;
                            account.poll_interval = Integer.parseInt(poll_interval);

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

                        Toast.makeText(getContext(), Helper.formatThrowable(ex), Toast.LENGTH_LONG).show();
                    }
                }.load(FragmentAccount.this, args);
            }
        });

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder
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
                                        Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }.load(FragmentAccount.this, args);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);
        btnAuthorize.setVisibility(View.GONE);
        tilPassword.setPasswordVisibilityToggleEnabled(id < 0);
        tvLink.setMovementMethod(LinkMovementMethod.getInstance());
        btnAuthorize.setEnabled(false);
        grpInstructions.setVisibility(View.GONE);
        grpInterval.setVisibility(View.GONE);
        btnCheck.setEnabled(false);
        pbCheck.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        grpFolders.setVisibility(View.GONE);
        ibDelete.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("provider", spProvider.getSelectedItemPosition());
        outState.putString("authorized", authorized);
        outState.putString("password", tilPassword.getEditText().getText().toString());
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Observe
        DB.getInstance(getContext()).account().liveAccount(id).observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
            boolean once = false;

            @Override
            public void onChanged(@Nullable EntityAccount account) {
                // Get providers
                List<Provider> providers = Provider.loadProfiles(getContext());
                providers.add(0, new Provider(getString(R.string.title_custom)));

                ArrayAdapter<Provider> padapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, providers);
                padapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spProvider.setAdapter(padapter);

                if (savedInstanceState == null) {
                    if (once)
                        return;
                    once = true;

                    spProvider.setTag(0);
                    spProvider.setSelection(0);
                    if (account != null) {
                        for (int pos = 1; pos < providers.size(); pos++)
                            if (providers.get(pos).imap_host.equals(account.host)) {
                                spProvider.setTag(pos);
                                spProvider.setSelection(pos);
                                break;
                            }
                    }

                    etName.setText(account == null ? null : account.name);

                    etHost.setText(account == null ? null : account.host);
                    etPort.setText(account == null ? null : Long.toString(account.port));

                    authorized = (account != null && account.auth_type != Helper.AUTH_TYPE_PASSWORD ? account.password : null);
                    etUser.setText(account == null ? null : account.user);
                    tilPassword.getEditText().setText(account == null ? null : account.password);

                    cbSynchronize.setChecked(account == null ? true : account.synchronize);
                    cbPrimary.setChecked(account == null ? true : account.primary);
                    etInterval.setText(account == null ? "9" : Integer.toString(account.poll_interval));
                } else {
                    int provider = savedInstanceState.getInt("provider");
                    spProvider.setTag(provider);
                    spProvider.setSelection(provider);

                    authorized = savedInstanceState.getString("authorized");
                    tilPassword.getEditText().setText(savedInstanceState.getString("password"));
                }

                Helper.setViewsEnabled(view, true);

                Provider provider = (Provider) spProvider.getSelectedItem();
                btnAuthorize.setVisibility(provider.getAuthType() == Helper.AUTH_TYPE_PASSWORD ? View.GONE : View.VISIBLE);
                tvLink.setText(Html.fromHtml("<a href=\"" + provider.link + "\">" + provider.link + "</a>"));
                grpInstructions.setVisibility(provider.link == null ? View.GONE : View.VISIBLE);

                cbPrimary.setEnabled(cbSynchronize.isChecked());

                grpInterval.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

                btnCheck.setVisibility(cbSynchronize.isChecked() ? View.VISIBLE : View.GONE);
                btnSave.setVisibility(cbSynchronize.isChecked() ? View.GONE : View.VISIBLE);

                // Consider previous check/save/delete as cancelled
                ibDelete.setVisibility(account == null ? View.GONE : View.VISIBLE);
                btnAuthorize.setEnabled(true);
                btnCheck.setEnabled(true);
                pbWait.setVisibility(View.GONE);
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
                                            Toast.makeText(getContext(), Helper.formatThrowable(ex), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                },
                                null);
                        break;
                    }
            }
    }
}
