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

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.net.UnknownHostException;
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

public class FragmentIdentity extends FragmentBase {
    private ViewGroup view;

    private EditText etName;
    private EditText etEmail;

    private Spinner spAccount;

    private EditText etDisplay;
    private Button btnColor;
    private View vwColor;
    private ImageView ibColorDefault;
    private EditText etSignature;
    private Button btnHtml;

    private Button btnAdvanced;
    private Spinner spProvider;
    private EditText etDomain;
    private Button btnAutoConfig;
    private EditText etHost;
    private CheckBox cbStartTls;
    private CheckBox cbInsecure;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private EditText etRealm;

    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;

    private EditText etReplyTo;
    private EditText etBcc;
    private CheckBox cbPlainOnly;
    private CheckBox cbDeliveryReceipt;
    private CheckBox cbReadReceipt;

    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private TextView tvError;

    private ContentLoadingProgressBar pbWait;

    private Group grpAuthorize;
    private Group grpAdvanced;

    private long id = -1;
    private boolean saving = false;
    private int auth_type = Helper.AUTH_TYPE_PASSWORD;
    private int color = Color.TRANSPARENT;

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
        setSubtitle(R.string.title_edit_identity);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_identity, container, false);

        // Get controls
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        spAccount = view.findViewById(R.id.spAccount);
        etDisplay = view.findViewById(R.id.etDisplay);
        btnColor = view.findViewById(R.id.btnColor);
        vwColor = view.findViewById(R.id.vwColor);
        ibColorDefault = view.findViewById(R.id.ibColorDefault);
        etSignature = view.findViewById(R.id.etSignature);
        btnHtml = view.findViewById(R.id.btnHtml);

        btnAdvanced = view.findViewById(R.id.btnAdvanced);
        spProvider = view.findViewById(R.id.spProvider);

        etDomain = view.findViewById(R.id.etDomain);
        btnAutoConfig = view.findViewById(R.id.btnAutoConfig);

        etHost = view.findViewById(R.id.etHost);
        cbStartTls = view.findViewById(R.id.cbStartTls);
        cbInsecure = view.findViewById(R.id.cbInsecure);
        etPort = view.findViewById(R.id.etPort);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        etRealm = view.findViewById(R.id.etRealm);

        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);

        etReplyTo = view.findViewById(R.id.etReplyTo);
        etBcc = view.findViewById(R.id.etBcc);
        cbPlainOnly = view.findViewById(R.id.cbPlainOnly);
        cbDeliveryReceipt = view.findViewById(R.id.cbDeliveryReceipt);
        cbReadReceipt = view.findViewById(R.id.cbReadReceipt);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        tvError = view.findViewById(R.id.tvError);

        pbWait = view.findViewById(R.id.pbWait);

        grpAuthorize = view.findViewById(R.id.grpAuthorize);
        grpAdvanced = view.findViewById(R.id.grpAdvanced);

        // Wire controls

        spAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                grpAuthorize.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                if (position == 0)
                    grpAdvanced.setVisibility(View.GONE);
                tilPassword.setPasswordVisibilityToggleEnabled(position == 0);

                Integer tag = (Integer) adapterView.getTag();
                if (tag != null && tag.equals(position))
                    return;
                adapterView.setTag(position);

                EntityAccount account = (EntityAccount) adapterView.getAdapter().getItem(position);
                auth_type = account.auth_type;

                // Select associated provider
                if (position == 0)
                    spProvider.setSelection(0);
                else {
                    boolean found = false;
                    for (int pos = 1; pos < spProvider.getAdapter().getCount(); pos++) {
                        EmailProvider provider = (EmailProvider) spProvider.getItemAtPosition(pos);
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

                // Copy account credentials
                etEmail.setText(account.user);
                etUser.setTag(auth_type == Helper.AUTH_TYPE_PASSWORD ? null : account.user);
                etUser.setText(account.user);
                tilPassword.getEditText().setText(account.password);
                etRealm.setText(account.realm);
                tilPassword.setEnabled(auth_type == Helper.AUTH_TYPE_PASSWORD);
                etRealm.setEnabled(auth_type == Helper.AUTH_TYPE_PASSWORD);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        etUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String user = etUser.getText().toString();
                if (auth_type != Helper.AUTH_TYPE_PASSWORD && !user.equals(etUser.getTag())) {
                    auth_type = Helper.AUTH_TYPE_PASSWORD;
                    tilPassword.getEditText().setText(null);
                    tilPassword.setEnabled(true);
                    etRealm.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
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
                    fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
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

        btnHtml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_html, null);
                final EditText etHtml = dview.findViewById(R.id.etHtml);
                etHtml.setText(HtmlHelper.toHtml(etSignature.getText()));

                new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                        .setView(dview)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Spanned html = HtmlHelper.fromHtml(etHtml.getText().toString());
                                etSignature.setText(html);
                            }
                        })
                        .show();
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
                            ((ScrollView) view).smoothScrollTo(0, btnAdvanced.getTop());
                        }
                    });
            }
        });

        spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Integer tag = (Integer) adapterView.getTag();
                if (tag != null && tag.equals(position))
                    return;
                adapterView.setTag(position);

                EmailProvider provider = (EmailProvider) adapterView.getSelectedItem();

                // Set associated host/port/starttls
                etHost.setText(provider.smtp_host);
                etPort.setText(position == 0 ? null : Integer.toString(provider.smtp_port));
                cbStartTls.setChecked(provider.smtp_starttls);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        etDomain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                btnAutoConfig.setEnabled(text.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnAutoConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAutoConfig();
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
                onSave();
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);
        btnAutoConfig.setEnabled(false);
        cbInsecure.setVisibility(View.GONE);
        tilPassword.setPasswordVisibilityToggleEnabled(id < 0);
        btnSave.setVisibility(View.GONE);
        btnAdvanced.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        grpAuthorize.setVisibility(View.GONE);
        grpAdvanced.setVisibility(View.GONE);

        return view;
    }

    private void onAutoConfig() {
        etDomain.setEnabled(false);
        btnAutoConfig.setEnabled(false);

        Bundle args = new Bundle();
        args.putString("domain", etDomain.getText().toString());

        new SimpleTask<EmailProvider>() {
            @Override
            protected void onPreExecute(Bundle args) {
                etDomain.setEnabled(false);
                btnAutoConfig.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                etDomain.setEnabled(true);
                btnAutoConfig.setEnabled(true);
            }

            @Override
            protected EmailProvider onExecute(Context context, Bundle args) throws Throwable {
                String domain = args.getString("domain");
                return EmailProvider.fromDomain(context, domain);
            }

            @Override
            protected void onExecuted(Bundle args, EmailProvider provider) {
                etHost.setText(provider.smtp_host);
                etPort.setText(Integer.toString(provider.smtp_port));
                cbStartTls.setChecked(provider.smtp_starttls);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException || ex instanceof UnknownHostException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentIdentity.this, args, "identity:config");
    }

    private void onSave() {
        EntityAccount account = (EntityAccount) spAccount.getSelectedItem();

        String name = etName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            CharSequence hint = etName.getHint();
            if (!TextUtils.isEmpty(hint))
                name = hint.toString();
        }

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("name", name);
        args.putString("email", etEmail.getText().toString().trim());
        args.putString("display", etDisplay.getText().toString());
        args.putString("replyto", etReplyTo.getText().toString().trim());
        args.putString("bcc", etBcc.getText().toString().trim());
        args.putBoolean("plain_only", cbPlainOnly.isChecked());
        args.putBoolean("delivery_receipt", cbDeliveryReceipt.isChecked());
        args.putBoolean("read_receipt", cbReadReceipt.isChecked());
        args.putLong("account", account == null ? -1 : account.id);
        args.putInt("auth_type", auth_type);
        args.putString("host", etHost.getText().toString());
        args.putBoolean("starttls", cbStartTls.isChecked());
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putString("user", etUser.getText().toString());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putString("realm", etRealm.getText().toString());
        args.putInt("color", color);
        args.putString("signature", HtmlHelper.toHtml(etSignature.getText()));
        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("primary", cbPrimary.isChecked());

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbSave.setVisibility(View.VISIBLE);
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
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                String name = args.getString("name");
                String email = args.getString("email");
                long account = args.getLong("account");

                String display = args.getString("display");
                Integer color = args.getInt("color");
                String signature = args.getString("signature");

                int auth_type = args.getInt("auth_type");
                String host = args.getString("host");
                boolean starttls = args.getBoolean("starttls");
                boolean insecure = args.getBoolean("insecure");
                String port = args.getString("port");
                String user = args.getString("user");
                String password = args.getString("password");
                String realm = args.getString("realm");
                boolean synchronize = args.getBoolean("synchronize");
                boolean primary = args.getBoolean("primary");

                String replyto = args.getString("replyto");
                String bcc = args.getString("bcc");
                boolean plain_only = args.getBoolean("plain_only");
                boolean delivery_receipt = args.getBoolean("delivery_receipt");
                boolean read_receipt = args.getBoolean("read_receipt");

                if (TextUtils.isEmpty(name))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_name));
                if (TextUtils.isEmpty(email))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_email));
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    throw new IllegalArgumentException(context.getString(R.string.title_email_invalid));
                if (TextUtils.isEmpty(host))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (starttls ? "587" : "465");
                if (TextUtils.isEmpty(user))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (synchronize && TextUtils.isEmpty(password) && !insecure)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));

                email = email.toLowerCase();

                if (TextUtils.isEmpty(display))
                    display = null;

                if (TextUtils.isEmpty(realm))
                    realm = null;

                if (TextUtils.isEmpty(replyto))
                    replyto = null;
                else
                    replyto = replyto.toLowerCase();

                if (TextUtils.isEmpty(bcc))
                    bcc = null;
                else
                    bcc = bcc.toLowerCase();

                if (Color.TRANSPARENT == color)
                    color = null;

                DB db = DB.getInstance(context);
                EntityIdentity identity = db.identity().getIdentity(id);

                String identityRealm = (identity == null ? null : identity.realm);

                boolean check = (synchronize && (identity == null ||
                        auth_type != identity.auth_type ||
                        !host.equals(identity.host) || Integer.parseInt(port) != identity.port ||
                        !user.equals(identity.user) || !password.equals(identity.password) ||
                        (realm == null ? identityRealm != null : !realm.equals(identityRealm))));
                boolean reload = (identity == null || identity.synchronize != synchronize || check);

                Long last_connected = null;
                if (identity != null && synchronize == identity.synchronize)
                    last_connected = identity.last_connected;

                // Check SMTP server
                if (check) {
                    String protocol = (starttls ? "smtp" : "smtps");
                    Properties props = MessageHelper.getSessionProperties(auth_type, realm, insecure);
                    Session isession = Session.getInstance(props, null);
                    isession.setDebug(true);
                    Transport itransport = isession.getTransport(protocol);
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
                    identity.email = email;
                    identity.account = account;
                    identity.display = display;
                    identity.color = color;
                    identity.signature = signature;

                    identity.auth_type = auth_type;
                    identity.host = host;
                    identity.starttls = starttls;
                    identity.insecure = insecure;
                    identity.port = Integer.parseInt(port);
                    identity.user = user;
                    identity.password = password;
                    identity.realm = realm;
                    identity.synchronize = synchronize;
                    identity.primary = (identity.synchronize && primary);

                    identity.replyto = replyto;
                    identity.bcc = bcc;
                    identity.plain_only = plain_only;
                    identity.delivery_receipt = delivery_receipt;
                    identity.read_receipt = read_receipt;
                    identity.error = null;
                    identity.last_connected = last_connected;

                    if (identity.primary)
                        db.identity().resetPrimary(account);

                    if (update)
                        db.identity().updateIdentity(identity);
                    else
                        identity.id = db.identity().insertIdentity(identity);
                    EntityLog.log(context, (update ? "Updated" : "Added") +
                            " identity=" + identity.name + " email=" + identity.email);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (reload)
                    ServiceSynchronize.reload(context, "save identity");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                getFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else {
                    tvError.setText(Helper.formatThrowable(ex));
                    tvError.setVisibility(View.VISIBLE);
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            ((ScrollView) view).smoothScrollTo(0, tvError.getBottom());
                        }
                    });
                }
            }
        }.execute(FragmentIdentity.this, args, "identity:save");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("account", spAccount.getSelectedItemPosition());
        outState.putInt("provider", spProvider.getSelectedItemPosition());
        outState.putInt("auth_type", auth_type);
        outState.putString("password", tilPassword.getEditText().getText().toString());
        outState.putInt("advanced", grpAdvanced.getVisibility());
        outState.putInt("color", color);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<EntityIdentity>() {
            @Override
            protected EntityIdentity onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                return DB.getInstance(context).identity().getIdentity(id);
            }

            @Override
            protected void onExecuted(Bundle args, final EntityIdentity identity) {
                if (savedInstanceState == null) {
                    auth_type = (identity == null ? Helper.AUTH_TYPE_PASSWORD : identity.auth_type);

                    etName.setText(identity == null ? null : identity.name);
                    etEmail.setText(identity == null ? null : identity.email);

                    etDisplay.setText(identity == null ? null : identity.display);
                    etSignature.setText(identity == null ||
                            TextUtils.isEmpty(identity.signature) ? null : HtmlHelper.fromHtml(identity.signature));

                    etHost.setText(identity == null ? null : identity.host);
                    cbStartTls.setChecked(identity == null ? false : identity.starttls);
                    cbInsecure.setChecked(identity == null ? false : identity.insecure);
                    etPort.setText(identity == null ? null : Long.toString(identity.port));
                    etUser.setTag(identity == null || auth_type == Helper.AUTH_TYPE_PASSWORD ? null : identity.user);
                    etUser.setText(identity == null ? null : identity.user);
                    tilPassword.getEditText().setText(identity == null ? null : identity.password);
                    etRealm.setText(identity == null ? null : identity.realm);
                    cbSynchronize.setChecked(identity == null ? true : identity.synchronize);
                    cbPrimary.setChecked(identity == null ? true : identity.primary);

                    etReplyTo.setText(identity == null ? null : identity.replyto);
                    etBcc.setText(identity == null ? null : identity.bcc);
                    cbPlainOnly.setChecked(identity == null ? false : identity.plain_only);
                    cbDeliveryReceipt.setChecked(identity == null ? false : identity.delivery_receipt);
                    cbReadReceipt.setChecked(identity == null ? false : identity.read_receipt);

                    color = (identity == null || identity.color == null ? Color.TRANSPARENT : identity.color);

                    etName.requestFocus();

                    if (identity == null)
                        new SimpleTask<Integer>() {
                            @Override
                            protected Integer onExecute(Context context, Bundle args) {
                                return DB.getInstance(context).identity().getSynchronizingIdentityCount();
                            }

                            @Override
                            protected void onExecuted(Bundle args, Integer count) {
                                cbPrimary.setChecked(count == 0);
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentIdentity.this, new Bundle(), "identity:count");
                } else {
                    auth_type = savedInstanceState.getInt("auth_type");
                    tilPassword.getEditText().setText(savedInstanceState.getString("password"));
                    grpAdvanced.setVisibility(savedInstanceState.getInt("advanced"));
                    color = savedInstanceState.getInt("color");
                }

                Helper.setViewsEnabled(view, true);

                tilPassword.setEnabled(auth_type == Helper.AUTH_TYPE_PASSWORD);
                etRealm.setEnabled(auth_type == Helper.AUTH_TYPE_PASSWORD);

                setColor(color);

                cbPrimary.setEnabled(cbSynchronize.isChecked());

                pbWait.setVisibility(View.GONE);

                new SimpleTask<List<EntityAccount>>() {
                    @Override
                    protected List<EntityAccount> onExecute(Context context, Bundle args) {
                        return DB.getInstance(context).account().getAccounts();
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                        if (accounts == null)
                            accounts = new ArrayList<>();

                        EntityAccount unselected = new EntityAccount();
                        unselected.id = -1L;
                        unselected.auth_type = Helper.AUTH_TYPE_PASSWORD;
                        unselected.name = getString(R.string.title_select);
                        unselected.primary = false;
                        accounts.add(0, unselected);

                        ArrayAdapter<EntityAccount> aaAccount =
                                new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, accounts);
                        aaAccount.setDropDownViewResource(R.layout.spinner_item1_dropdown);
                        spAccount.setAdapter(aaAccount);

                        // Get providers
                        List<EmailProvider> providers = EmailProvider.loadProfiles(getContext());
                        providers.add(0, new EmailProvider(getString(R.string.title_custom)));

                        ArrayAdapter<EmailProvider> aaProfile =
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
                            for (int pos = 0; pos < accounts.size(); pos++) {
                                EntityAccount account = accounts.get(pos);
                                if (account.id.equals((identity == null ? -1 : identity.account))) {
                                    spAccount.setTag(pos);
                                    spAccount.setSelection(pos);
                                    // OAuth token could be updated
                                    if (pos > 0 && accounts.get(pos).auth_type != Helper.AUTH_TYPE_PASSWORD)
                                        tilPassword.getEditText().setText(accounts.get(pos).password);
                                    break;
                                }
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

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(FragmentIdentity.this, args, "identity:accounts:get");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "identity:get");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_identity, menu);
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
        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setMessage(R.string.title_identity_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                                db.identity().setIdentityTbd(id);

                                ServiceSynchronize.reload(context, "delete identity");

                                return null;
                            }

                            @Override
                            protected void onExecuted(Bundle args, Void data) {
                                getFragmentManager().popBackStack();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentIdentity.this, args, "identity:delete");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setColor(int color) {
        FragmentIdentity.this.color = color;

        GradientDrawable border = new GradientDrawable();
        border.setColor(color);
        border.setStroke(1, Helper.resolveColor(getContext(), R.attr.colorSeparator));
        vwColor.setBackground(border);
    }
}
