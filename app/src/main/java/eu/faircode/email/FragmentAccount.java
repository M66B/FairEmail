package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;

public class FragmentAccount extends FragmentEx {
    private List<Provider> providers;

    private EditText etName;
    private Spinner spProfile;
    private EditText etHost;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;
    private Button btnSave;
    private ProgressBar pbCheck;
    // TODO: loading spinner

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_account);

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Get arguments
        Bundle args = getArguments();
        final long id = (args == null ? -1 : args.getLong("id", -1));

        // Get providers
        providers = Provider.loadProfiles(getContext());
        providers.add(0, new Provider(getString(R.string.title_custom)));

        // Get controls
        spProfile = view.findViewById(R.id.spProvider);
        etName = view.findViewById(R.id.etName);
        etHost = view.findViewById(R.id.etHost);
        etPort = view.findViewById(R.id.etPort);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        btnSave = view.findViewById(R.id.btnSave);
        pbCheck = view.findViewById(R.id.pbCheck);

        // Wire controls

        spProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Provider provider = providers.get(position);
                if (provider.imap_port != 0) {
                    etName.setText(provider.name);
                    etHost.setText(provider.imap_host);
                    etPort.setText(Integer.toString(provider.imap_port));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayAdapter<Provider> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, providers);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spProfile.setAdapter(adapter);

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbPrimary.setEnabled(checked);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSave.setEnabled(false);
                pbCheck.setVisibility(View.VISIBLE);

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putString("name", etName.getText().toString());
                args.putString("host", etHost.getText().toString());
                args.putString("port", etPort.getText().toString());
                args.putString("user", etUser.getText().toString());
                args.putString("password", tilPassword.getEditText().getText().toString());
                args.putBoolean("synchronize", cbSynchronize.isChecked());
                args.putBoolean("primary", cbPrimary.isChecked());

                getLoaderManager().restartLoader(ActivityView.LOADER_ACCOUNT_PUT, args, putLoaderCallbacks).forceLoad();
            }
        });

        // Initialize
        tilPassword.setPasswordVisibilityToggleEnabled(id < 0);
        pbCheck.setVisibility(View.GONE);

        // Observe
        DB.getInstance(getContext()).account().liveAccount(id).observe(this, new Observer<EntityAccount>() {
            @Override
            public void onChanged(@Nullable EntityAccount account) {
                etName.setText(account == null ? null : account.name);
                etHost.setText(account == null ? null : account.host);
                etPort.setText(account == null ? null : Long.toString(account.port));
                etUser.setText(account == null ? null : account.user);
                tilPassword.getEditText().setText(account == null ? null : account.password);
                cbSynchronize.setChecked(account == null ? true : account.synchronize);
                cbPrimary.setChecked(account == null ? true : account.primary);
                cbPrimary.setEnabled(account == null ? true : account.synchronize);
            }
        });

        return view;
    }

    private static class PutLoader extends AsyncTaskLoader<Throwable> {
        private Bundle args;

        PutLoader(Context context) {
            super(context);
        }

        void setArgs(Bundle args) {
            this.args = args;
        }

        @Override
        public Throwable loadInBackground() {
            try {
                String name = args.getString("name");
                String host = args.getString("host");
                String port = args.getString("port");
                String user = args.getString("user");
                String password = args.getString("password");

                if (TextUtils.isEmpty(host))
                    throw new Throwable(getContext().getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    throw new Throwable(getContext().getString(R.string.title_no_port));
                if (TextUtils.isEmpty(user))
                    throw new Throwable(getContext().getString(R.string.title_no_user));
                if (TextUtils.isEmpty(password))
                    throw new Throwable(getContext().getString(R.string.title_no_password));

                if (TextUtils.isEmpty(name))
                    name = host + "/" + user;

                DB db = DB.getInstance(getContext());
                EntityAccount account = db.account().getAccount(args.getLong("id"));
                boolean update = (account != null);
                if (account == null)
                    account = new EntityAccount();
                account.name = name;
                account.host = host;
                account.port = Integer.parseInt(port);
                account.user = user;
                account.password = password;
                account.synchronize = args.getBoolean("synchronize");
                account.primary = (account.synchronize && args.getBoolean("primary"));

                // Check IMAP server
                List<EntityFolder> folders = new ArrayList<>();
                if (account.synchronize) {
                    Session isession = Session.getInstance(MessageHelper.getSessionProperties(), null);
                    IMAPStore istore = null;
                    try {
                        istore = (IMAPStore) isession.getStore("imaps");
                        istore.connect(account.host, account.port, account.user, account.password);

                        if (!istore.hasCapability("IDLE"))
                            throw new MessagingException(getContext().getString(R.string.title_no_idle));

                        // Find system folders
                        boolean drafts = false;
                        for (Folder ifolder : istore.getDefaultFolder().list("*")) {
                            String type = null;

                            // First check folder attributes
                            String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                            for (String attr : attrs) {
                                if (attr.startsWith("\\")) {
                                    int index = EntityFolder.SYSTEM_FOLDER_ATTR.indexOf(attr.substring(1));
                                    if (index >= 0) {
                                        type = EntityFolder.SYSTEM_FOLDER_TYPE.get(index);
                                        break;
                                    }
                                }
                            }

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

                            if (type != null) {
                                EntityFolder folder = new EntityFolder();
                                folder.name = ifolder.getFullName();
                                folder.type = type;
                                folder.synchronize = EntityFolder.SYSTEM_FOLDER_SYNC.contains(folder.type);
                                folder.after = EntityFolder.DEFAULT_STANDARD_SYNC;
                                folders.add(folder);

                                Log.i(Helper.TAG, account.name +
                                        " system=" + folder.name +
                                        " type=" + folder.type + " attr=" + TextUtils.join(",", attrs));

                                if (EntityFolder.TYPE_DRAFTS.equals(folder.type))
                                    drafts = true;
                            }
                        }
/*
                        if (!drafts) {
                            EntityFolder folder = new EntityFolder();
                            folder.name = getContext().getString(R.string.title_local_drafts);
                            folder.type = EntityFolder.TYPE_DRAFTS;
                            folder.synchronize = false;
                            folder.after = 0;
                            folders.add(folder);
                        }
*/
                    } finally {
                        if (istore != null)
                            istore.close();
                    }
                }

                if (account.primary)
                    db.account().resetPrimary();

                try {
                    db.beginTransaction();
                    if (update)
                        db.account().updateAccount(account);
                    else
                        account.id = db.account().insertAccount(account);

                    EntityFolder inbox = new EntityFolder();
                    inbox.name = "INBOX";
                    inbox.type = EntityFolder.TYPE_INBOX;
                    inbox.synchronize = true;
                    inbox.after = EntityFolder.DEFAULT_INBOX_SYNC;
                    folders.add(0, inbox);

                    for (EntityFolder folder : folders)
                        if (db.folder().getFolderByName(account.id, folder.name) == null) {
                            folder.account = account.id;
                            Log.i(Helper.TAG, "Creating folder=" + folder.name + " (" + folder.type + ")");
                            folder.id = db.folder().insertFolder(folder);
                        }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.restart(getContext(), "account");

                return null;
            } catch (Throwable ex) {
                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                return ex;
            }
        }
    }

    private LoaderManager.LoaderCallbacks putLoaderCallbacks = new LoaderManager.LoaderCallbacks<Throwable>() {
        @NonNull
        @Override
        public Loader<Throwable> onCreateLoader(int id, Bundle args) {
            PutLoader loader = new PutLoader(getContext());
            loader.setArgs(args);
            return loader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Throwable> loader, Throwable ex) {
            getLoaderManager().destroyLoader(loader.getId());

            btnSave.setEnabled(true);
            pbCheck.setVisibility(View.GONE);

            if (ex == null)
                getFragmentManager().popBackStack();
            else {
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                Toast.makeText(getContext(), Helper.formatThrowable(ex), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Throwable> loader) {
        }
    };
}
