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
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;

public class FragmentIdentity extends Fragment {
    private List<Provider> providers;

    private Spinner spProfile;
    private EditText etName;
    private EditText etEmail;
    private EditText etReplyTo;
    private EditText etHost;
    private CheckBox cbStartTls;
    private EditText etPort;
    private EditText etUser;
    private EditText etPassword;
    private CheckBox cbPrimary;
    private CheckBox cbSynchronize;
    private Button btnOk;
    private ProgressBar pbCheck;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_identity, container, false);

        // Get arguments
        Bundle args = getArguments();
        final long id = args.getLong("id", -1);

        // Get providers
        providers = Provider.loadProfiles(getContext());
        providers.add(0, new Provider(getString(R.string.title_custom)));

        // Get controls
        spProfile = view.findViewById(R.id.spProvider);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etReplyTo = view.findViewById(R.id.etReplyTo);
        etHost = view.findViewById(R.id.etHost);
        cbStartTls = view.findViewById(R.id.cbStartTls);
        etPort = view.findViewById(R.id.etPort);
        etUser = view.findViewById(R.id.etUser);
        etPassword = view.findViewById(R.id.etPassword);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        btnOk = view.findViewById(R.id.btnOk);
        pbCheck = view.findViewById(R.id.pbCheck);

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

        cbStartTls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                etPort.setHint(checked ? "587" : "465");
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

        ArrayAdapter<Provider> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, providers);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spProfile.setAdapter(adapter);

        pbCheck.setVisibility(View.GONE);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOk.setEnabled(false);
                pbCheck.setVisibility(View.VISIBLE);

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putString("name", etName.getText().toString());
                args.putString("email", etEmail.getText().toString());
                args.putString("replyto", etReplyTo.getText().toString());
                args.putString("host", etHost.getText().toString());
                args.putBoolean("starttls", cbStartTls.isChecked());
                args.putString("port", etPort.getText().toString());
                args.putString("user", etUser.getText().toString());
                args.putString("password", etPassword.getText().toString());
                args.putBoolean("primary", cbPrimary.isChecked());
                args.putBoolean("synchronize", cbSynchronize.isChecked());

                getLoaderManager().restartLoader(ActivityView.LOADER_IDENTITY_PUT, args, putLoaderCallbacks).forceLoad();
            }
        });

        DB.getInstance(getContext()).identity().liveIdentity(id).observe(this, new Observer<EntityIdentity>() {
            @Override
            public void onChanged(@Nullable EntityIdentity identity) {
                etName.setText(identity == null ? null : identity.name);
                etEmail.setText(identity == null ? null : identity.email);
                etReplyTo.setText(identity == null ? null : identity.replyto);
                etHost.setText(identity == null ? null : identity.host);
                cbStartTls.setChecked(identity == null ? false : identity.starttls);
                etPort.setText(identity == null ? null : Long.toString(identity.port));
                etUser.setText(identity == null ? null : identity.user);
                etPassword.setText(identity == null ? null : identity.password);
                cbPrimary.setChecked(identity == null ? true : identity.primary);
                cbSynchronize.setChecked(identity == null ? true : identity.synchronize);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.title_edit_identity);
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
                String host = args.getString("host");
                boolean starttls = args.getBoolean("starttls");
                String port = args.getString("port");
                if (TextUtils.isEmpty(port))
                    port = "0";

                DB db = DB.getInstance(getContext());
                EntityIdentity identity = db.identity().getIdentity(id);
                boolean update = (identity != null);
                if (identity == null)
                    identity = new EntityIdentity();
                identity.name = Objects.requireNonNull(args.getString("name"));
                identity.email = Objects.requireNonNull(args.getString("email"));
                identity.replyto = args.getString("replyto");
                identity.host = host;
                identity.port = Integer.parseInt(port);
                identity.starttls = starttls;
                identity.user = Objects.requireNonNull(args.getString("user"));
                identity.password = Objects.requireNonNull(args.getString("password"));
                identity.primary = args.getBoolean("primary");
                identity.synchronize = args.getBoolean("synchronize");

                if (TextUtils.isEmpty(identity.name))
                    throw new IllegalArgumentException(getContext().getString(R.string.title_no_name));

                if (TextUtils.isEmpty(identity.email))
                    throw new IllegalArgumentException(getContext().getString(R.string.title_no_email));

                // Check SMTP server
                if (identity.synchronize) {
                    Properties props = MessageHelper.getSessionProperties();
                    Session isession = Session.getDefaultInstance(props, null);
                    Transport itransport = isession.getTransport(identity.starttls ? "smtp" : "smtps");
                    try {
                        itransport.connect(identity.host, identity.port, identity.user, identity.password);
                    } finally {
                        itransport.close();
                    }
                }

                if (identity.primary)
                    db.identity().resetPrimary();

                if (update)
                    db.identity().updateIdentity(identity);
                else
                    identity.id = db.identity().insertIdentity(identity);

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
            PutLoader loader = new PutLoader(getActivity());
            loader.setArgs(args);
            return loader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Throwable> loader, Throwable ex) {
            getLoaderManager().destroyLoader(loader.getId());

            btnOk.setEnabled(true);
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
