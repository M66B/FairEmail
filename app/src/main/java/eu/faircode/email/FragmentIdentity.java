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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Session;
import javax.mail.Transport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

public class FragmentIdentity extends FragmentEx {
    private List<Provider> providers;

    private ViewGroup view;
    private EditText etName;
    private EditText etEmail;
    private EditText etReplyTo;
    private Spinner spProfile;
    private Spinner spAccount;
    private EditText etHost;
    private CheckBox cbStartTls;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;
    private Button btnSave;
    private ProgressBar pbSave;
    private ImageButton ibDelete;
    private ProgressBar pbWait;

    private ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_identity);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_identity, container, false);

        // Get arguments
        Bundle args = getArguments();
        final long id = (args == null ? -1 : args.getLong("id", -1));

        // Get providers
        providers = Provider.loadProfiles(getContext());
        providers.add(0, new Provider(getString(R.string.title_custom)));

        // Get controls
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etReplyTo = view.findViewById(R.id.etReplyTo);
        spProfile = view.findViewById(R.id.spProvider);
        spAccount = view.findViewById(R.id.spAccount);
        etHost = view.findViewById(R.id.etHost);
        cbStartTls = view.findViewById(R.id.cbStartTls);
        etPort = view.findViewById(R.id.etPort);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        ibDelete = view.findViewById(R.id.ibDelete);
        pbWait = view.findViewById(R.id.pbWait);

        // Wire controls

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                etUser.setText(s.toString());
            }
        });

        spAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                EntityAccount account = (EntityAccount) adapterView.getAdapter().getItem(position);
                if (account.id >= 0 && TextUtils.isEmpty(tilPassword.getEditText().getText().toString())) {
                    tilPassword.getEditText().setText(account.password);
                    tilPassword.setPasswordVisibilityToggleEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Provider provider = providers.get(position);
                if (provider.smtp_port != 0) {
                    etHost.setText(provider.smtp_host);
                    etPort.setText(Integer.toString(provider.smtp_port));
                    cbStartTls.setChecked(provider.starttls);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayAdapter<Provider> adapterProfile = new ArrayAdapter<>(getContext(), R.layout.spinner_item, providers);
        adapterProfile.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spProfile.setAdapter(adapterProfile);

        cbStartTls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                etPort.setHint(checked ? "587" : "465");
            }
        });

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbPrimary.setEnabled(checked);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.setViewsEnabled(view, false);
                btnSave.setEnabled(false);
                pbSave.setVisibility(View.VISIBLE);

                EntityAccount account = (EntityAccount) spAccount.getSelectedItem();

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putString("name", etName.getText().toString());
                args.putString("email", etEmail.getText().toString());
                args.putString("replyto", etReplyTo.getText().toString());
                args.putLong("account", account == null ? -1 : account.id);
                args.putString("host", etHost.getText().toString());
                args.putBoolean("starttls", cbStartTls.isChecked());
                args.putString("port", etPort.getText().toString());
                args.putString("user", etUser.getText().toString());
                args.putString("password", tilPassword.getEditText().getText().toString());
                args.putBoolean("synchronize", cbSynchronize.isChecked());
                args.putBoolean("primary", cbPrimary.isChecked());

                LoaderManager.getInstance(FragmentIdentity.this)
                        .restartLoader(ActivityView.LOADER_IDENTITY_PUT, args, putLoaderCallbacks).forceLoad();
            }
        });

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder
                        .setMessage(R.string.title_identity_delete)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getFragmentManager().popBackStack();
                                // TODO: spinner
                                executor.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            DB.getInstance(getContext()).identity().deleteIdentity(id);
                                        } catch (Throwable ex) {
                                            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);
        tilPassword.setPasswordVisibilityToggleEnabled(id < 0);
        btnSave.setEnabled(false);
        pbSave.setVisibility(View.GONE);
        ibDelete.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        long id = (args == null ? -1 : args.getLong("id", -1));

        final DB db = DB.getInstance(getContext());

        // Observe identity
        db.identity().liveIdentity(id).observe(getViewLifecycleOwner(), new Observer<EntityIdentity>() {
            @Override
            public void onChanged(@Nullable final EntityIdentity identity) {
                etName.setText(identity == null ? null : identity.name);
                etEmail.setText(identity == null ? null : identity.email);
                etReplyTo.setText(identity == null ? null : identity.replyto);
                etHost.setText(identity == null ? null : identity.host);
                cbStartTls.setChecked(identity == null ? false : identity.starttls);
                etPort.setText(identity == null ? null : Long.toString(identity.port));
                etUser.setText(identity == null ? null : identity.user);
                tilPassword.getEditText().setText(identity == null ? null : identity.password);
                cbSynchronize.setChecked(identity == null ? true : identity.synchronize);
                cbPrimary.setChecked(identity == null ? true : identity.primary);
                cbPrimary.setEnabled(identity == null ? true : identity.synchronize);
                ibDelete.setVisibility(identity == null ? View.GONE : View.VISIBLE);

                Helper.setViewsEnabled(view, true);
                btnSave.setEnabled(true);
                pbWait.setVisibility(View.GONE);

                db.account().liveAccounts().removeObservers(getViewLifecycleOwner());
                db.account().liveAccounts().observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
                    @Override
                    public void onChanged(List<EntityAccount> accounts) {

                        EntityAccount unselected = new EntityAccount();
                        unselected.id = -1L;
                        unselected.name = "";
                        unselected.primary = false;
                        accounts.add(0, unselected);

                        ArrayAdapter<EntityAccount> adapterAccount = new ArrayAdapter<>(getContext(), R.layout.spinner_item, accounts);
                        adapterAccount.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        spAccount.setAdapter(adapterAccount);

                        for (int pos = 0; pos < accounts.size(); pos++)
                            if (accounts.get(pos).id == (identity == null ? -1 : identity.account)) {
                                spAccount.setSelection(pos);
                                break;
                            }
                    }
                });
            }
        });
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
                long id = args.getLong("id");
                String name = args.getString("name");
                String email = args.getString("email");
                String replyto = args.getString("replyto");
                long account = args.getLong("account");
                String host = args.getString("host");
                boolean starttls = args.getBoolean("starttls");
                String port = args.getString("port");
                String user = args.getString("user");
                String password = args.getString("password");

                if (TextUtils.isEmpty(name))
                    throw new IllegalArgumentException(getContext().getString(R.string.title_no_name));
                if (TextUtils.isEmpty(email))
                    throw new IllegalArgumentException(getContext().getString(R.string.title_no_email));
                if (account < 0)
                    throw new IllegalArgumentException(getContext().getString(R.string.title_no_account));
                if (TextUtils.isEmpty(host))
                    throw new IllegalArgumentException(getContext().getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    throw new IllegalArgumentException(getContext().getString(R.string.title_no_port));
                if (TextUtils.isEmpty(user))
                    throw new IllegalArgumentException(getContext().getString(R.string.title_no_user));
                if (TextUtils.isEmpty(password))
                    throw new IllegalArgumentException(getContext().getString(R.string.title_no_password));

                if (TextUtils.isEmpty(replyto))
                    replyto = null;

                DB db = DB.getInstance(getContext());
                EntityIdentity identity = db.identity().getIdentity(id);
                boolean update = (identity != null);
                if (identity == null)
                    identity = new EntityIdentity();
                identity.name = name;
                identity.email = email;
                identity.replyto = replyto;
                identity.account = account;
                identity.host = Objects.requireNonNull(host);
                identity.port = Integer.parseInt(port);
                identity.starttls = starttls;
                identity.user = user;
                identity.password = password;
                identity.synchronize = args.getBoolean("synchronize");
                identity.primary = (identity.synchronize && args.getBoolean("primary"));

                // Check SMTP server
                if (identity.synchronize) {
                    Properties props = MessageHelper.getSessionProperties();
                    Session isession = Session.getInstance(props, null);
                    Transport itransport = isession.getTransport(identity.starttls ? "smtp" : "smtps");
                    try {
                        itransport.connect(identity.host, identity.port, identity.user, identity.password);
                    } finally {
                        itransport.close();
                    }
                }

                try {
                    db.beginTransaction();

                    if (identity.primary)
                        db.identity().resetPrimary();

                    if (update)
                        db.identity().updateIdentity(identity);
                    else
                        identity.id = db.identity().insertIdentity(identity);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

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
            LoaderManager.getInstance(FragmentIdentity.this).destroyLoader(loader.getId());

            Helper.setViewsEnabled(view, true);
            btnSave.setEnabled(true);
            pbSave.setVisibility(View.GONE);

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
