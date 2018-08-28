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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;

public class FragmentIdentity extends FragmentEx {
    private ViewGroup view;
    private EditText etName;
    private EditText etEmail;
    private EditText etReplyTo;
    private Spinner spProvider;
    private Spinner spAccount;
    private EditText etHost;
    private CheckBox cbStartTls;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private TextView tvLink;
    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;
    private CheckBox cbStoreSent;
    private Button btnSave;
    private ProgressBar pbSave;
    private ImageButton ibDelete;
    private ProgressBar pbWait;
    private Group grpInstructions;

    private long id = -1;

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
        setSubtitle(R.string.title_edit_identity);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_identity, container, false);

        // Get controls
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etReplyTo = view.findViewById(R.id.etReplyTo);
        spProvider = view.findViewById(R.id.spProvider);
        spAccount = view.findViewById(R.id.spAccount);
        etHost = view.findViewById(R.id.etHost);
        cbStartTls = view.findViewById(R.id.cbStartTls);
        etPort = view.findViewById(R.id.etPort);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvLink = view.findViewById(R.id.tvLink);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbStoreSent = view.findViewById(R.id.cbStoreSent);
        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        ibDelete = view.findViewById(R.id.ibDelete);
        pbWait = view.findViewById(R.id.pbWait);
        grpInstructions = view.findViewById(R.id.grpInstructions);

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
                Integer tag = (Integer) adapterView.getTag();
                if (tag != null && tag.equals(position))
                    return;
                adapterView.setTag(position);

                EntityAccount account = (EntityAccount) adapterView.getAdapter().getItem(position);

                // Select associated provider
                for (int pos = 1; pos < spProvider.getAdapter().getCount(); pos++) {
                    Provider provider = (Provider) spProvider.getItemAtPosition(pos);
                    if (provider.imap_host.equals(account.host) && provider.imap_port == account.port) {
                        spProvider.setSelection(pos);
                        break;
                    }
                }

                // Copy account user name
                etUser.setText(account.user);

                // Copy account password
                tilPassword.getEditText().setText(account.password);
                tilPassword.setPasswordVisibilityToggleEnabled(position == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Integer tag = (Integer) adapterView.getTag();
                if (tag != null && tag.equals(position))
                    return;
                adapterView.setTag(position);

                Provider provider = (Provider) adapterView.getSelectedItem();

                // Set associated host/port/starttls
                etHost.setText(provider.smtp_host);
                etPort.setText(position == 0 ? null : Integer.toString(provider.smtp_port));
                cbStartTls.setChecked(provider.starttls);

                // Show link to instructions
                tvLink.setText(Html.fromHtml("<a href=\"" + provider.link + "\">" + provider.link + "</a>"));
                grpInstructions.setVisibility(provider.link == null ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

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
                args.putInt("auth_type", account == null ? Helper.AUTH_TYPE_PASSWORD : account.auth_type);
                args.putString("host", etHost.getText().toString());
                args.putBoolean("starttls", cbStartTls.isChecked());
                args.putString("port", etPort.getText().toString());
                args.putString("user", etUser.getText().toString());
                args.putString("password", tilPassword.getEditText().getText().toString());
                args.putBoolean("synchronize", cbSynchronize.isChecked());
                args.putBoolean("primary", cbPrimary.isChecked());
                args.putBoolean("store_sent", cbStoreSent.isChecked());

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) throws Throwable {
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
                        int auth_type = args.getInt("auth_type");
                        boolean synchronize = args.getBoolean("synchronize");
                        boolean primary = args.getBoolean("primary");
                        boolean store_sent = args.getBoolean("store_sent");

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

                        // Refresh token
                        if (id >= 0 && auth_type == Helper.AUTH_TYPE_GMAIL)
                            password = Helper.refreshToken(getContext(), "com.google", user, password);

                        // Check SMTP server
                        if (synchronize) {
                            Properties props = MessageHelper.getSessionProperties(context, auth_type);
                            Session isession = Session.getInstance(props, null);
                            isession.setDebug(true);
                            Transport itransport = isession.getTransport(starttls ? "smtp" : "smtps");
                            try {
                                itransport.connect(host, Integer.parseInt(port), user, password);
                            } finally {
                                itransport.close();
                            }
                        }

                        DB db = DB.getInstance(getContext());
                        try {
                            db.beginTransaction();

                            EntityIdentity identity = db.identity().getIdentity(id);
                            boolean update = (identity != null);
                            if (identity == null)
                                identity = new EntityIdentity();
                            identity.name = name;
                            identity.email = email;
                            identity.replyto = replyto;
                            identity.account = account;
                            identity.host = host;
                            identity.port = Integer.parseInt(port);
                            identity.starttls = starttls;
                            identity.user = user;
                            identity.password = password;
                            identity.auth_type = auth_type;
                            identity.synchronize = synchronize;
                            identity.primary = (identity.synchronize && primary);
                            identity.store_sent = store_sent;

                            if (!identity.synchronize)
                                identity.error = null;

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

                        ServiceSynchronize.reload(getContext(), "save identity");

                        return null;
                    }

                    @Override
                    protected void onLoaded(Bundle args, Void data) {
                        getFragmentManager().popBackStack();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.setViewsEnabled(view, true);
                        btnSave.setEnabled(true);
                        pbSave.setVisibility(View.GONE);

                        Toast.makeText(getContext(), Helper.formatThrowable(ex), Toast.LENGTH_LONG).show();
                    }
                }.load(FragmentIdentity.this, args);
            }
        });

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder
                        .setMessage(R.string.title_identity_delete)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Helper.setViewsEnabled(view, false);
                                btnSave.setEnabled(false);
                                pbWait.setVisibility(View.VISIBLE);

                                Bundle args = new Bundle();
                                args.putLong("id", id);

                                new SimpleTask<Void>() {
                                    @Override
                                    protected Void onLoad(Context context, Bundle args) {
                                        long id = args.getLong("id");
                                        DB.getInstance(context).identity().deleteIdentity(id);
                                        ServiceSynchronize.reload(getContext(), "delete identity");
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
                                }.load(FragmentIdentity.this, args);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);
        tilPassword.setPasswordVisibilityToggleEnabled(id < 0);
        tvLink.setMovementMethod(LinkMovementMethod.getInstance());
        btnSave.setEnabled(false);
        pbSave.setVisibility(View.GONE);
        ibDelete.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("account", spAccount.getSelectedItemPosition());
        outState.putInt("provider", spProvider.getSelectedItemPosition());
        outState.putString("password", tilPassword.getEditText().getText().toString());
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DB db = DB.getInstance(getContext());

        // Observe identity
        db.identity().liveIdentity(id).observe(getViewLifecycleOwner(), new Observer<EntityIdentity>() {
            boolean once = false;

            @Override
            public void onChanged(@Nullable final EntityIdentity identity) {
                if (savedInstanceState == null) {
                    if (once)
                        return;
                    once = true;

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
                    cbStoreSent.setChecked(identity == null ? false : identity.store_sent);

                    etName.requestFocus();
                } else
                    tilPassword.getEditText().setText(savedInstanceState.getString("password"));

                Helper.setViewsEnabled(view, true);

                grpInstructions.setVisibility(View.GONE);
                cbPrimary.setEnabled(cbSynchronize.isChecked());

                // Consider previous save/delete as cancelled
                ibDelete.setVisibility(identity == null ? View.GONE : View.VISIBLE);
                btnSave.setEnabled(true);
                pbWait.setVisibility(View.GONE);

                db.account().liveAccounts().removeObservers(getViewLifecycleOwner());
                db.account().liveAccounts().observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
                    @Override
                    public void onChanged(List<EntityAccount> accounts) {
                        if (accounts == null)
                            accounts = new ArrayList<>();

                        EntityAccount unselected = new EntityAccount();
                        unselected.id = -1L;
                        unselected.name = getString(R.string.title_select);
                        unselected.primary = false;
                        accounts.add(0, unselected);

                        ArrayAdapter<EntityAccount> aa = new ArrayAdapter<>(getContext(), R.layout.spinner_item, accounts);
                        aa.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        spAccount.setAdapter(aa);

                        // Get providers
                        List<Provider> providers = Provider.loadProfiles(getContext());
                        providers.add(0, new Provider(getString(R.string.title_custom)));

                        ArrayAdapter<Provider> adapterProfile = new ArrayAdapter<>(getContext(), R.layout.spinner_item, providers);
                        adapterProfile.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        spProvider.setAdapter(adapterProfile);

                        if (savedInstanceState == null) {
                            spProvider.setTag(0);
                            spProvider.setSelection(0);
                            if (identity != null)
                                for (int pos = 1; pos < providers.size(); pos++)
                                    if (providers.get(pos).smtp_host.equals(identity.host)) {
                                        spProvider.setTag(pos);
                                        spProvider.setSelection(pos);
                                        break;
                                    }

                            spAccount.setTag(0);
                            spAccount.setSelection(0);
                            for (int pos = 0; pos < accounts.size(); pos++)
                                if (accounts.get(pos).id == (identity == null ? -1 : identity.account)) {
                                    spAccount.setTag(pos);
                                    spAccount.setSelection(pos);
                                    // OAuth token could be updated
                                    if (pos > 0 && accounts.get(pos).auth_type != Helper.AUTH_TYPE_PASSWORD)
                                        tilPassword.getEditText().setText(accounts.get(pos).password);
                                    break;
                                }
                        } else {
                            int provider = savedInstanceState.getInt("provider");
                            spProvider.setTag(provider);
                            spProvider.setSelection(provider);

                            int account = savedInstanceState.getInt("account");
                            spAccount.setTag(account);
                            spAccount.setSelection(account);
                        }

                        Provider provider = (Provider) spProvider.getSelectedItem();
                        tvLink.setText(Html.fromHtml("<a href=\"" + provider.link + "\">" + provider.link + "</a>"));
                        grpInstructions.setVisibility(provider.link == null ? View.GONE : View.VISIBLE);
                    }
                });
            }
        });
    }
}
