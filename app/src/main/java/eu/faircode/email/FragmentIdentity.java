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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Session;
import javax.mail.Transport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

public class FragmentIdentity extends FragmentEx {
    private ViewGroup view;
    private EditText etName;
    private Spinner spAccount;
    private Button btnAdvanced;
    private TextView tvEmail;
    private EditText etEmail;
    private EditText etReplyTo;
    private Spinner spProvider;
    private EditText etDomain;
    private Button btnAutoConfig;
    private EditText etHost;
    private CheckBox cbStartTls;
    private CheckBox cbInsecure;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private Button btnColor;
    private View vwColor;
    private ImageView ibColorDefault;
    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;
    private CheckBox cbStoreSent;
    private Button btnSave;
    private ProgressBar pbSave;
    private ImageButton ibDelete;
    private ProgressBar pbWait;
    private Group grpAdvanced;

    private long id = -1;
    private int color = Color.TRANSPARENT;

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final boolean insecure = prefs.getBoolean("insecure", false);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_identity, container, false);

        // Get controls
        etName = view.findViewById(R.id.etName);
        spAccount = view.findViewById(R.id.spAccount);

        btnAdvanced = view.findViewById(R.id.btnAdvanced);
        tvEmail = view.findViewById(R.id.tvEmail);
        etEmail = view.findViewById(R.id.etEmail);
        etReplyTo = view.findViewById(R.id.etReplyTo);

        spProvider = view.findViewById(R.id.spProvider);

        etDomain = view.findViewById(R.id.etDomain);
        btnAutoConfig = view.findViewById(R.id.btnAutoConfig);

        etHost = view.findViewById(R.id.etHost);
        cbStartTls = view.findViewById(R.id.cbStartTls);
        cbInsecure = view.findViewById(R.id.cbInsecure);
        etPort = view.findViewById(R.id.etPort);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);

        btnColor = view.findViewById(R.id.btnColor);
        vwColor = view.findViewById(R.id.vwColor);
        ibColorDefault = view.findViewById(R.id.ibColorDefault);

        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbStoreSent = view.findViewById(R.id.cbStoreSent);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        ibDelete = view.findViewById(R.id.ibDelete);
        pbWait = view.findViewById(R.id.pbWait);
        grpAdvanced = view.findViewById(R.id.grpAdvanced);

        // Wire controls

        spAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                btnAdvanced.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                if (position == 0)
                    grpAdvanced.setVisibility(View.GONE);
                tilPassword.setPasswordVisibilityToggleEnabled(position == 0);
                btnSave.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

                Integer tag = (Integer) adapterView.getTag();
                if (tag != null && tag.equals(position))
                    return;
                adapterView.setTag(position);

                EntityAccount account = (EntityAccount) adapterView.getAdapter().getItem(position);

                // Select associated provider
                if (position == 0)
                    spProvider.setSelection(0);
                else {
                    boolean found = false;
                    for (int pos = 1; pos < spProvider.getAdapter().getCount(); pos++) {
                        Provider provider = (Provider) spProvider.getItemAtPosition(pos);
                        if (provider.imap_host.equals(account.host) &&
                                provider.imap_port == account.port) {
                            found = true;

                            spProvider.setSelection(pos);

                            // This is needed because the spinner might be invisible
                            etHost.setText(provider.smtp_host);
                            etPort.setText(Integer.toString(provider.smtp_port));
                            cbStartTls.setChecked(provider.smtp_starttls);

                            break;
                        }
                    }
                    if (!found)
                        grpAdvanced.setVisibility(View.VISIBLE);
                }

                // Copy account user name
                etEmail.setText(account.user);
                etUser.setText(account.user);

                // Copy account password
                tilPassword.getEditText().setText(account.password);
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
                cbStartTls.setChecked(provider.smtp_starttls);
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
                        Record[] records = new Lookup("_submission._tcp." + domain, Type.SRV).run();
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
                            cbStartTls.setChecked(srv.getPort() == 587);
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
                }.load(FragmentIdentity.this, args);
            }
        });

        btnAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = (grpAdvanced.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                grpAdvanced.setVisibility(visibility);
                cbInsecure.setVisibility(insecure ? visibility : View.GONE);
                if (visibility == View.VISIBLE)
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            ((ScrollView) view).smoothScrollTo(0, tvEmail.getTop());
                        }
                    });
            }
        });

        cbStartTls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                etPort.setHint(checked ? "587" : "465");
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
                    fragmentTransaction.hide(FragmentIdentity.this);
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
                args.putInt("auth_type", account == null || account.auth_type == null ? Helper.AUTH_TYPE_PASSWORD : account.auth_type);
                args.putString("host", etHost.getText().toString());
                args.putBoolean("starttls", cbStartTls.isChecked());
                args.putBoolean("insecure", cbInsecure.isChecked());
                args.putString("port", etPort.getText().toString());
                args.putString("user", etUser.getText().toString());
                args.putString("password", tilPassword.getEditText().getText().toString());
                args.putInt("color", color);
                args.putBoolean("synchronize", cbSynchronize.isChecked());
                args.putBoolean("primary", cbPrimary.isChecked());
                args.putBoolean("store_sent", cbStoreSent.isChecked());

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) throws Throwable {
                        long id = args.getLong("id");
                        String name = args.getString("name");
                        long account = args.getLong("account");
                        String email = args.getString("email");
                        String replyto = args.getString("replyto");
                        String host = args.getString("host");
                        boolean starttls = args.getBoolean("starttls");
                        boolean insecure = args.getBoolean("insecure");
                        String port = args.getString("port");
                        String user = args.getString("user");
                        String password = args.getString("password");
                        Integer color = args.getInt("color");
                        int auth_type = args.getInt("auth_type");
                        boolean synchronize = args.getBoolean("synchronize");
                        boolean primary = args.getBoolean("primary");
                        boolean store_sent = args.getBoolean("store_sent");

                        if (TextUtils.isEmpty(name))
                            throw new IllegalArgumentException(getContext().getString(R.string.title_no_name));
                        if (TextUtils.isEmpty(email))
                            throw new IllegalArgumentException(getContext().getString(R.string.title_no_email));
                        if (TextUtils.isEmpty(host))
                            throw new IllegalArgumentException(getContext().getString(R.string.title_no_host));
                        if (TextUtils.isEmpty(port))
                            port = (starttls ? "587" : "465");
                        if (TextUtils.isEmpty(user))
                            throw new IllegalArgumentException(getContext().getString(R.string.title_no_user));
                        if (TextUtils.isEmpty(password) && !insecure)
                            throw new IllegalArgumentException(getContext().getString(R.string.title_no_password));

                        email = email.toLowerCase();
                        if (TextUtils.isEmpty(replyto))
                            replyto = null;
                        else
                            replyto = replyto.toLowerCase();
                        if (Color.TRANSPARENT == color)
                            color = null;

                        DB db = DB.getInstance(context);
                        EntityIdentity identity = db.identity().getIdentity(id);

                        boolean check = (synchronize && (identity == null ||
                                !host.equals(identity.host) || Integer.parseInt(port) != identity.port ||
                                !user.equals(identity.user) || !password.equals(identity.password)));

                        // Check SMTP server
                        if (check) {
                            Properties props = MessageHelper.getSessionProperties(auth_type, insecure);
                            Session isession = Session.getInstance(props, null);
                            isession.setDebug(true);
                            Transport itransport = isession.getTransport(starttls ? "smtp" : "smtps");
                            try {
                                try {
                                    itransport.connect(host, Integer.parseInt(port), user, password);
                                } catch (AuthenticationFailedException ex) {
                                    if (auth_type == Helper.AUTH_TYPE_GMAIL) {
                                        password = Helper.refreshToken(context, "com.google", user, password);
                                        itransport.connect(host, Integer.parseInt(port), user, password);
                                    } else
                                        throw ex;
                                }
                            } finally {
                                itransport.close();
                            }
                        }

                        try {
                            db.beginTransaction();

                            boolean update = (identity != null);
                            if (identity == null)
                                identity = new EntityIdentity();
                            identity.name = name;
                            identity.account = account;
                            identity.email = email;
                            identity.replyto = replyto;
                            identity.host = host;
                            identity.starttls = starttls;
                            identity.insecure = insecure;
                            identity.port = Integer.parseInt(port);
                            identity.user = user;
                            identity.password = password;
                            identity.color = color;
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

                        if (check)
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

                        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                                .setMessage(Helper.formatThrowable(ex))
                                .setPositiveButton(android.R.string.cancel, null)
                                .create()
                                .show();
                    }
                }.load(FragmentIdentity.this, args);
            }
        });

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
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
                                        DB db = DB.getInstance(context);
                                        EntityIdentity identity = db.identity().getIdentity(id);
                                        db.identity().deleteIdentity(id);
                                        if (identity.synchronize)
                                            ServiceSynchronize.reload(getContext(), "delete identity");
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
                                }.load(FragmentIdentity.this, args);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);
        cbInsecure.setVisibility(View.GONE);
        tilPassword.setPasswordVisibilityToggleEnabled(id < 0);
        btnSave.setVisibility(View.GONE);
        btnAdvanced.setVisibility(View.GONE);
        grpAdvanced.setVisibility(View.GONE);
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
        outState.putInt("advanced", grpAdvanced.getVisibility());
        outState.putInt("color", color);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DB db = DB.getInstance(getContext());

        // Observe identity
        db.identity().liveIdentity(id).observe(getViewLifecycleOwner(), new Observer<EntityIdentity>() {
            private boolean once = false;

            @Override
            public void onChanged(@Nullable final EntityIdentity identity) {
                if (once)
                    return;
                once = true;

                if (savedInstanceState == null) {
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

                    color = (identity == null || identity.color == null ? Color.TRANSPARENT : identity.color);

                    etName.requestFocus();

                    if (identity == null)
                        new SimpleTask<Integer>() {
                            @Override
                            protected Integer onLoad(Context context, Bundle args) {
                                return DB.getInstance(context).identity().getSynchronizingIdentityCount();
                            }

                            @Override
                            protected void onLoaded(Bundle args, Integer count) {
                                cbPrimary.setChecked(count == 0);
                            }
                        }.load(FragmentIdentity.this, new Bundle());
                } else {
                    tilPassword.getEditText().setText(savedInstanceState.getString("password"));
                    grpAdvanced.setVisibility(savedInstanceState.getInt("advanced"));
                    color = savedInstanceState.getInt("color");
                }

                Helper.setViewsEnabled(view, true);

                setColor(color);
                cbPrimary.setEnabled(cbSynchronize.isChecked());

                // Consider previous save/delete as cancelled
                ibDelete.setVisibility(identity == null ? View.GONE : View.VISIBLE);
                pbWait.setVisibility(View.GONE);

                db.account().liveAccounts().removeObservers(getViewLifecycleOwner());
                db.account().liveAccounts().observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
                    private boolean once = false;

                    @Override
                    public void onChanged(List<EntityAccount> accounts) {
                        if (once)
                            return;
                        once = true;

                        if (accounts == null)
                            accounts = new ArrayList<>();

                        EntityAccount unselected = new EntityAccount();
                        unselected.id = -1L;
                        unselected.name = getString(R.string.title_select);
                        unselected.primary = false;
                        accounts.add(0, unselected);

                        ArrayAdapter<EntityAccount> aaAccount =
                                new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, accounts);
                        aaAccount.setDropDownViewResource(R.layout.spinner_item1_dropdown);
                        spAccount.setAdapter(aaAccount);

                        // Get providers
                        List<Provider> providers = Provider.loadProfiles(getContext());
                        providers.add(0, new Provider(getString(R.string.title_custom)));

                        ArrayAdapter<Provider> aaProfile =
                                new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, providers);
                        aaProfile.setDropDownViewResource(R.layout.spinner_item1_dropdown);
                        spProvider.setAdapter(aaProfile);

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
                    }
                });
            }
        });
    }

    private void setColor(int color) {
        FragmentIdentity.this.color = color;

        GradientDrawable border = new GradientDrawable();
        border.setColor(color);
        border.setStroke(1, Helper.resolveColor(getContext(), R.attr.colorSeparator));
        vwColor.setBackground(border);
    }
}
