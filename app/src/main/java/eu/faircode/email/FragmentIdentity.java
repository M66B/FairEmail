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
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_OAUTH;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class FragmentIdentity extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private EditText etName;
    private EditText etEmail;

    private Spinner spAccount;

    private EditText etDisplay;
    private ViewButtonColor btnColor;
    private TextView tvColorPro;
    private Button btnSignature;

    private Button btnAdvanced;
    private Spinner spProvider;
    private EditText etDomain;
    private Button btnAutoConfig;
    private ContentLoadingProgressBar pbAutoConfig;
    private EditText etHost;
    private RadioGroup rgEncryption;
    private CheckBox cbInsecure;
    private TextView tvInsecureRemark;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private TextView tvPasswordStorage;
    private Button btnCertificate;
    private TextView tvCertificate;
    private EditText etRealm;
    private CheckBox cbUseIp;
    private EditText etEhlo;

    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;
    private CheckBox cbSelf;

    private CheckBox cbSenderExtra;
    private CheckBox cbSenderExtraName;
    private TextView etSenderExtra;
    private ImageButton ibSenderExtra;
    private EditText etReplyTo;
    private EditText etCc;
    private EditText etBcc;
    private EditText etInternal;
    private CheckBox cbSignDefault;
    private CheckBox cbEncryptDefault;
    private CheckBox cbUnicode;
    private CheckBox cbOctetMime;
    private EditText etMaxSize;

    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private TextView tvError;
    private CheckBox cbTrust;
    private Button btnHelp;
    private Button btnSupport;
    private TextView tvInstructions;

    private ContentLoadingProgressBar pbWait;

    private Group grpAuthorize;
    private Group grpAdvanced;
    private Group grpError;

    private long id = -1;
    private long copy = -1;
    private long account = -1;
    private int auth = AUTH_TYPE_PASSWORD;
    private String provider = null;
    private String certificate = null;
    private String signature = null;
    private boolean saving = false;

    private static final int REQUEST_COLOR = 1;
    private static final int REQUEST_SAVE = 2;
    private static final int REQUEST_DELETE = 3;
    private static final int REQUEST_SIGNATURE = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        if (args.getBoolean("copy"))
            copy = args.getLong("id", -1);
        else
            id = args.getLong("id", -1);

        account = args.getLong("account", -1);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_identity);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_identity, container, false);
        scroll = view.findViewById(R.id.scroll);

        // Get controls
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        spAccount = view.findViewById(R.id.spAccount);
        etDisplay = view.findViewById(R.id.etDisplay);
        btnColor = view.findViewById(R.id.btnColor);
        tvColorPro = view.findViewById(R.id.tvColorPro);
        btnSignature = view.findViewById(R.id.btnSignature);

        btnAdvanced = view.findViewById(R.id.btnAdvanced);
        spProvider = view.findViewById(R.id.spProvider);

        etDomain = view.findViewById(R.id.etDomain);
        btnAutoConfig = view.findViewById(R.id.btnAutoConfig);
        pbAutoConfig = view.findViewById(R.id.pbAutoConfig);

        etHost = view.findViewById(R.id.etHost);
        rgEncryption = view.findViewById(R.id.rgEncryption);
        cbInsecure = view.findViewById(R.id.cbInsecure);
        tvInsecureRemark = view.findViewById(R.id.tvInsecureRemark);
        etPort = view.findViewById(R.id.etPort);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvPasswordStorage = view.findViewById(R.id.tvPasswordStorage);
        btnCertificate = view.findViewById(R.id.btnCertificate);
        tvCertificate = view.findViewById(R.id.tvCertificate);
        etRealm = view.findViewById(R.id.etRealm);
        cbUseIp = view.findViewById(R.id.cbUseIp);
        etEhlo = view.findViewById(R.id.etEhlo);

        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbSelf = view.findViewById(R.id.cbSelf);

        cbSenderExtra = view.findViewById(R.id.cbSenderExtra);
        cbSenderExtraName = view.findViewById(R.id.cbSenderExtraName);
        etSenderExtra = view.findViewById(R.id.etSenderExtra);
        ibSenderExtra = view.findViewById(R.id.ibSenderExtra);
        etReplyTo = view.findViewById(R.id.etReplyTo);
        etCc = view.findViewById(R.id.etCc);
        etBcc = view.findViewById(R.id.etBcc);
        etInternal = view.findViewById(R.id.etInternal);
        cbSignDefault = view.findViewById(R.id.cbSignDefault);
        cbEncryptDefault = view.findViewById(R.id.cbEncryptDefault);
        cbUnicode = view.findViewById(R.id.cbUnicode);
        cbOctetMime = view.findViewById(R.id.cbOctetMime);
        etMaxSize = view.findViewById(R.id.etMaxSize);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        tvError = view.findViewById(R.id.tvError);
        cbTrust = view.findViewById(R.id.cbTrust);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnSupport = view.findViewById(R.id.btnSupport);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        tvInstructions.setMovementMethod(LinkMovementMethod.getInstance());

        pbWait = view.findViewById(R.id.pbWait);

        grpAuthorize = view.findViewById(R.id.grpAuthorize);
        grpAdvanced = view.findViewById(R.id.grpAdvanced);
        grpError = view.findViewById(R.id.grpError);

        // Wire controls

        spAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;

                grpAuthorize.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                if (position == 0) {
                    grpError.setVisibility(View.GONE);
                    btnHelp.setVisibility(View.GONE);
                    btnSupport.setVisibility(View.GONE);
                    tvInstructions.setVisibility(View.GONE);
                    grpAdvanced.setVisibility(View.GONE);
                }

                Integer tag = (Integer) adapterView.getTag();
                if (Objects.equals(tag, position))
                    return;
                adapterView.setTag(position);

                if (FragmentIdentity.this.id < 0) {
                    EntityAccount account = (EntityAccount) adapterView.getAdapter().getItem(position);

                    boolean found = false;
                    for (int pos = 1; pos < spProvider.getAdapter().getCount(); pos++) {
                        EmailProvider provider = (EmailProvider) spProvider.getItemAtPosition(pos);
                        if (provider.imap.host.equals(account.host) &&
                                provider.imap.port == account.port &&
                                provider.imap.starttls == (account.encryption == EmailService.ENCRYPTION_STARTTLS)) {
                            found = true;
                            spProvider.setTag(pos);
                            spProvider.setSelection(pos);
                            setProvider(provider);
                            break;
                        }
                    }
                    if (!found) {
                        spProvider.setTag(0);
                        spProvider.setSelection(0);
                        setProvider((EmailProvider) spProvider.getItemAtPosition(0));
                        if (account.host == null || account.host.startsWith("imap"))
                            etHost.setText(null);
                        else
                            etHost.setText(account.host);
                        grpAdvanced.setVisibility(View.VISIBLE);
                    }

                    setAccount(account);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (etDomain == null)
                    return;

                String[] email = editable.toString().split("@");
                etDomain.setText(email.length < 2 ? null : email[1]);
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

                checkPassword(s.toString());
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
                fragment.setTargetFragment(FragmentIdentity.this, REQUEST_COLOR);
                fragment.show(getParentFragmentManager(), "identity:color");
            }
        });

        Helper.linkPro(tvColorPro);

        btnSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ActivitySignature.class);
                intent.putExtra("html", signature);
                startActivityForResult(intent, REQUEST_SIGNATURE);
            }
        });

        btnAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = (grpAdvanced.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                grpAdvanced.setVisibility(visibility);
                checkPassword(tilPassword.getEditText().getText().toString());
                if (visibility == View.VISIBLE)
                    getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                return;
                            scroll.smoothScrollTo(0, btnAdvanced.getTop());
                        }
                    });
            }
        });

        cbSenderExtra.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbSenderExtraName.setEnabled(isChecked);
            }
        });

        ibSenderExtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 34);
            }
        });

        spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Integer tag = (Integer) adapterView.getTag();
                if (Objects.equals(tag, position))
                    return;
                adapterView.setTag(position);

                EmailProvider provider = (EmailProvider) adapterView.getSelectedItem();
                if (provider != null)
                    setProvider(provider);

                EntityAccount account = (EntityAccount) spAccount.getSelectedItem();
                if (account != null && Objects.equals(account.host, provider.imap.host))
                    setAccount(account);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnAutoConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAutoConfig();
            }
        });

        rgEncryption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int id) {
                if (id == R.id.radio_starttls)
                    etPort.setHint("587");
                else if (id == R.id.radio_none)
                    etPort.setHint("25");
                else
                    etPort.setHint("465");
            }
        });

        tvInsecureRemark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 4);
            }
        });

        btnCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.selectKeyAlias(getActivity(), getViewLifecycleOwner(), null, new Helper.IKeyAlias() {
                    @Override
                    public void onSelected(String alias) {
                        certificate = alias;
                        tvCertificate.setText(alias);
                    }

                    @Override
                    public void onNothingSelected() {
                        certificate = null;
                        tvCertificate.setText(R.string.title_optional);
                    }
                });
            }
        });

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbPrimary.setEnabled(checked);
            }
        });

        cbEncryptDefault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbSignDefault.setEnabled(!isChecked);
            }
        });

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

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, (Uri) btnHelp.getTag());
                Helper.view(getContext(), intent);
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "Identity:support"), false);
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);
        btnAutoConfig.setEnabled(false);
        pbAutoConfig.setVisibility(View.GONE);
        cbInsecure.setVisibility(View.GONE);
        tilPassword.setEndIconMode(id < 0 || Helper.isSecure(getContext()) ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);

        btnAdvanced.setVisibility(View.GONE);

        etEhlo.setHint(EmailService.getDefaultEhlo());

        btnSave.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        cbTrust.setVisibility(View.GONE);
        btnHelp.setVisibility(View.GONE);
        btnSupport.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);

        grpAuthorize.setVisibility(View.GONE);
        grpAdvanced.setVisibility(View.GONE);
        grpError.setVisibility(View.GONE);

        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    private void setAccount(EntityAccount account) {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        auth = account.auth_type;
        provider = account.provider;
        etEmail.setText(account.user);
        etUser.setText(account.user);
        tilPassword.getEditText().setText(account.password);
        //tilPassword.setEndIconMode(Helper.isSecure(getContext()) ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);
        certificate = account.certificate_alias;
        tvCertificate.setText(certificate == null ? getString(R.string.title_optional) : certificate);
        etRealm.setText(account.realm);
        cbTrust.setChecked(false);

        etUser.setEnabled(auth == AUTH_TYPE_PASSWORD);
        tilPassword.getEditText().setEnabled(auth == AUTH_TYPE_PASSWORD);
        btnCertificate.setEnabled(auth == AUTH_TYPE_PASSWORD);
    }

    private void setProvider(EmailProvider provider) {
        etHost.setText(provider.smtp.host);
        etPort.setText(provider.smtp.port == 0 ? null : Integer.toString(provider.smtp.port));
        rgEncryption.check(provider.smtp.starttls ? R.id.radio_starttls : R.id.radio_ssl);
        cbUseIp.setChecked(provider.useip);
        etEhlo.setText(null);
    }

    private void onAutoConfig() {
        etDomain.setEnabled(false);
        btnAutoConfig.setEnabled(false);

        Bundle args = new Bundle();
        args.putString("domain", etDomain.getText().toString().trim());

        new SimpleTask<EmailProvider>() {
            @Override
            protected void onPreExecute(Bundle args) {
                etDomain.setEnabled(false);
                btnAutoConfig.setEnabled(false);
                pbAutoConfig.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                etDomain.setEnabled(true);
                btnAutoConfig.setEnabled(true);
                pbAutoConfig.setVisibility(View.GONE);
            }

            @Override
            protected EmailProvider onExecute(Context context, Bundle args) throws Throwable {
                String domain = args.getString("domain");
                return EmailProvider
                        .fromDomain(context, domain, EmailProvider.Discover.SMTP)
                        .get(0);
            }

            @Override
            protected void onExecuted(Bundle args, EmailProvider provider) {
                etHost.setText(provider.smtp.host);
                etPort.setText(Integer.toString(provider.smtp.port));
                rgEncryption.check(provider.smtp.starttls ? R.id.radio_starttls : R.id.radio_ssl);
                cbUseIp.setChecked(provider.useip);
                etEhlo.setText(null);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex.getMessage() != null &&
                        (ex instanceof UnknownHostException ||
                                ex instanceof FileNotFoundException ||
                                ex instanceof IllegalArgumentException))
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "identity:config");
    }

    private void checkPassword(String password) {
        boolean warning = (Helper.containsWhiteSpace(password) ||
                Helper.containsControlChars(password));
        tilPassword.setHelperText(
                warning ? getString(R.string.title_setup_password_chars) : null);
        tilPassword.setHelperTextEnabled(warning);
    }

    private void onSave(boolean should) {
        EntityAccount account = (EntityAccount) spAccount.getSelectedItem();

        String name = etName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            CharSequence hint = etName.getHint();
            if (!TextUtils.isEmpty(hint))
                name = hint.toString();
        }

        int encryption;
        if (rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls)
            encryption = EmailService.ENCRYPTION_STARTTLS;
        else if (rgEncryption.getCheckedRadioButtonId() == R.id.radio_none)
            encryption = EmailService.ENCRYPTION_NONE;
        else
            encryption = EmailService.ENCRYPTION_SSL;

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("name", name);
        args.putString("email", etEmail.getText().toString().trim());
        args.putString("display", etDisplay.getText().toString());
        args.putInt("color", btnColor.getColor());
        args.putBoolean("sender_extra", cbSenderExtra.isChecked());
        args.putBoolean("sender_extra_name", cbSenderExtraName.isChecked());
        args.putString("sender_extra_regex", etSenderExtra.getText().toString());
        args.putString("replyto", etReplyTo.getText().toString().trim());
        args.putString("cc", etCc.getText().toString().trim());
        args.putString("bcc", etBcc.getText().toString().trim());
        args.putString("internal", etInternal.getText().toString().replaceAll(" ", ""));
        args.putBoolean("sign_default", cbSignDefault.isChecked());
        args.putBoolean("encrypt_default", cbEncryptDefault.isChecked());
        args.putBoolean("unicode", cbUnicode.isChecked());
        args.putBoolean("octetmime", cbOctetMime.isChecked());
        args.putString("max_size", etMaxSize.getText().toString());
        args.putLong("account", account == null ? -1 : account.id);
        args.putString("host", etHost.getText().toString().trim().replace(" ", ""));
        args.putInt("encryption", encryption);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putInt("auth", auth);
        args.putString("provider", provider);
        args.putString("user", etUser.getText().toString().trim());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putString("certificate", certificate);
        args.putString("realm", etRealm.getText().toString());
        args.putString("fingerprint", cbTrust.isChecked() ? (String) cbTrust.getTag() : null);
        args.putBoolean("use_ip", cbUseIp.isChecked());
        args.putString("ehlo", etEhlo.getText().toString());
        args.putString("signature", signature);
        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("primary", cbPrimary.isChecked());
        args.putBoolean("self", cbSelf.isChecked());

        args.putBoolean("should", should);

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbSave.setVisibility(View.VISIBLE);
                grpError.setVisibility(View.GONE);
                btnHelp.setVisibility(View.GONE);
                btnSupport.setVisibility(View.GONE);
                tvInstructions.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                saving = false;
                invalidateOptionsMenu();
                Helper.setViewsEnabled(view, true);
                if (auth != AUTH_TYPE_PASSWORD) {
                    etUser.setEnabled(false);
                    tilPassword.getEditText().setEnabled(false);
                    btnCertificate.setEnabled(false);
                }
                pbSave.setVisibility(View.GONE);
            }

            @Override
            protected Boolean onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                String name = args.getString("name");
                String email = args.getString("email");
                long account = args.getLong("account");

                String display = args.getString("display");
                Integer color = args.getInt("color");
                String signature = args.getString("signature");

                String host = args.getString("host");
                int encryption = args.getInt("encryption");
                boolean insecure = args.getBoolean("insecure");
                String port = args.getString("port");
                int auth = args.getInt("auth");
                String provider = args.getString("provider");
                String user = args.getString("user").trim();
                String password = args.getString("password");
                String certificate = args.getString("certificate");
                String realm = args.getString("realm");
                String fingerprint = args.getString("fingerprint");
                boolean use_ip = args.getBoolean("use_ip");
                String ehlo = args.getString("ehlo");
                boolean synchronize = args.getBoolean("synchronize");
                boolean primary = args.getBoolean("primary");
                boolean self = args.getBoolean("self");

                boolean sender_extra = args.getBoolean("sender_extra");
                boolean sender_extra_name = args.getBoolean("sender_extra_name");
                String sender_extra_regex = args.getString("sender_extra_regex");
                String replyto = args.getString("replyto");
                String cc = args.getString("cc");
                String bcc = args.getString("bcc");
                String internal = args.getString("internal");
                boolean sign_default = args.getBoolean("sign_default");
                boolean encrypt_default = args.getBoolean("encrypt_default");
                boolean unicode = args.getBoolean("unicode");
                boolean octetmime = args.getBoolean("octetmime");
                String max_size = args.getString("max_size");

                boolean should = args.getBoolean("should");

                int semi = host.indexOf(':');
                if (semi > 0 && host.indexOf(':', semi + 1) < 0)
                    host = host.substring(0, semi);

                //if (TextUtils.isEmpty(name) && !should)
                //    throw new IllegalArgumentException(context.getString(R.string.title_no_name));
                if (TextUtils.isEmpty(email) && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_email));
                if (!Helper.EMAIL_ADDRESS.matcher(email).matches() && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, email));
                if (TextUtils.isEmpty(host) && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    if (encryption == EmailService.ENCRYPTION_STARTTLS)
                        port = "587";
                    else if (encryption == EmailService.ENCRYPTION_NONE)
                        port = "25";
                    else
                        port = "465";
                if (TextUtils.isEmpty(user) && !insecure && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (synchronize && TextUtils.isEmpty(password) && !insecure && certificate == null && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));

                if (!TextUtils.isEmpty(replyto) && !should) {
                    try {
                        InternetAddress[] addresses = InternetAddress.parse(replyto);
                        if (addresses.length != 1)
                            throw new AddressException();
                        addresses[0].validate();
                    } catch (AddressException ex) {
                        throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, replyto));
                    }
                }

                if (!TextUtils.isEmpty(cc) && !should)
                    try {
                        for (InternetAddress address : InternetAddress.parse(cc))
                            address.validate();
                    } catch (AddressException ex) {
                        throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, cc));
                    }

                if (!TextUtils.isEmpty(bcc) && !should)
                    try {
                        for (InternetAddress address : InternetAddress.parse(bcc))
                            address.validate();
                    } catch (AddressException ex) {
                        throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, bcc));
                    }

                if (TextUtils.isEmpty(internal))
                    internal = null;

                if (TextUtils.isEmpty(display))
                    display = null;

                if (TextUtils.isEmpty(realm))
                    realm = null;

                if (TextUtils.isEmpty(ehlo))
                    ehlo = null;

                if (TextUtils.isEmpty(sender_extra_regex))
                    sender_extra_regex = null;

                if (TextUtils.isEmpty(replyto))
                    replyto = null;

                if (TextUtils.isEmpty(cc))
                    cc = null;

                if (TextUtils.isEmpty(bcc))
                    bcc = null;

                if (color == Color.TRANSPARENT || !ActivityBilling.isPro(context))
                    color = null;
                if (TextUtils.isEmpty(signature))
                    signature = null;

                Long user_max_size = (TextUtils.isEmpty(max_size) ? null : Integer.parseInt(max_size) * 1000 * 1000L);

                DB db = DB.getInstance(context);
                EntityIdentity identity = db.identity().getIdentity(id);

                if (should) {
                    if (identity == null)
                        return !TextUtils.isEmpty(host) && !TextUtils.isEmpty(user);

                    if (!Objects.equals(identity.name, name))
                        return true;
                    if (!Objects.equals(identity.email, email))
                        return true;
                    if (!Objects.equals(identity.account, account))
                        return true;
                    if (!Objects.equals(identity.display, display))
                        return true;
                    if (!Objects.equals(identity.color, color))
                        return true;
                    if (!Objects.equals(identity.signature, signature))
                        return true;
                    if (!Objects.equals(identity.host, host))
                        return true;
                    if (!Objects.equals(identity.encryption, encryption))
                        return true;
                    if (!Objects.equals(identity.insecure, insecure))
                        return true;
                    if (!Objects.equals(identity.port, Integer.parseInt(port)))
                        return true;
                    if (identity.auth_type != auth)
                        return true;
                    if (!Objects.equals(identity.user, user))
                        return true;
                    if (!Objects.equals(identity.password, password))
                        return true;
                    if (!Objects.equals(identity.certificate_alias, certificate))
                        return true;
                    if (!Objects.equals(identity.realm, realm))
                        return true;
                    if (!Objects.equals(identity.fingerprint, fingerprint))
                        return true;
                    if (!Objects.equals(identity.use_ip, use_ip))
                        return true;
                    if (!Objects.equals(identity.ehlo, ehlo))
                        return true;
                    if (!Objects.equals(identity.synchronize, synchronize))
                        return true;
                    if (!Objects.equals(identity.primary, (identity.synchronize && primary)))
                        return true;
                    if (!Objects.equals(identity.self, self))
                        return true;
                    if (!Objects.equals(identity.sender_extra, sender_extra))
                        return true;
                    if (!Objects.equals(identity.sender_extra_name, sender_extra_name))
                        return true;
                    if (!Objects.equals(identity.sender_extra_regex, sender_extra_regex))
                        return true;
                    if (!Objects.equals(identity.replyto, replyto))
                        return true;
                    if (!Objects.equals(identity.cc, cc))
                        return true;
                    if (!Objects.equals(identity.bcc, bcc))
                        return true;
                    if (!Objects.equals(identity.internal, internal))
                        return true;
                    if (!Objects.equals(identity.sign_default, sign_default))
                        return true;
                    if (!Objects.equals(identity.encrypt_default, encrypt_default))
                        return true;
                    if (!Objects.equals(identity.unicode, unicode))
                        return true;
                    if (!Objects.equals(identity.octetmime, octetmime))
                        return true;
                    if (user_max_size != null && !Objects.equals(identity.max_size, user_max_size))
                        return true;
                    if (identity.error != null && identity.synchronize)
                        return true;

                    return false;
                }

                String identityRealm = (identity == null ? null : identity.realm);

                boolean check = (synchronize && (identity == null ||
                        !identity.synchronize || identity.error != null ||
                        !host.equals(identity.host) ||
                        encryption != identity.encryption ||
                        insecure != identity.insecure ||
                        Integer.parseInt(port) != identity.port ||
                        !user.equals(identity.user) ||
                        !password.equals(identity.password) ||
                        !Objects.equals(certificate, identity.certificate_alias) ||
                        !Objects.equals(realm, identityRealm) ||
                        !Objects.equals(fingerprint, identity.fingerprint) ||
                        use_ip != identity.use_ip ||
                        !Objects.equals(ehlo, identity.ehlo) ||
                        (user_max_size != null && !Objects.equals(user_max_size, identity.max_size)) ||
                        BuildConfig.DEBUG));
                Log.i("Identity check=" + check);

                Long last_connected = null;
                if (identity != null && synchronize == identity.synchronize)
                    last_connected = identity.last_connected;

                // Check SMTP server
                Long server_max_size = null;
                if (check) {
                    // Create transport
                    String protocol = (encryption == EmailService.ENCRYPTION_SSL ? "smtps" : "smtp");
                    try (EmailService iservice = new EmailService(
                            context, protocol, realm, encryption, insecure, unicode,
                            EmailService.PURPOSE_CHECK, true)) {
                        iservice.setUseIp(use_ip, ehlo);
                        iservice.connect(
                                host, Integer.parseInt(port),
                                auth, provider,
                                user, password,
                                certificate, fingerprint);
                        server_max_size = iservice.getMaxSize();
                    }
                }

                try {
                    db.beginTransaction();

                    if (identity != null && !identity.password.equals(password)) {
                        int count = db.identity().setIdentityPassword(
                                identity.account,
                                identity.user, password, identity.auth_type,
                                identity.host);
                        Log.i("Updated passwords=" + count);
                    }

                    boolean update = (identity != null);
                    if (identity == null)
                        identity = new EntityIdentity();
                    identity.name = name;
                    identity.email = email;
                    identity.account = account;
                    identity.display = display;
                    identity.color = color;
                    identity.signature = signature;

                    identity.host = host;
                    identity.encryption = encryption;
                    identity.insecure = insecure;
                    identity.port = Integer.parseInt(port);
                    identity.auth_type = auth;
                    identity.user = user;
                    identity.password = password;
                    identity.certificate_alias = certificate;
                    identity.provider = provider;
                    identity.realm = realm;
                    identity.fingerprint = fingerprint;
                    identity.use_ip = use_ip;
                    identity.ehlo = ehlo;
                    identity.synchronize = synchronize;
                    identity.primary = (identity.synchronize && primary);
                    identity.self = self;

                    identity.sender_extra = sender_extra;
                    identity.sender_extra_name = sender_extra_name;
                    identity.sender_extra_regex = sender_extra_regex;
                    identity.replyto = replyto;
                    identity.cc = cc;
                    identity.bcc = bcc;
                    identity.internal = internal;
                    identity.sign_default = sign_default;
                    identity.encrypt_default = encrypt_default;
                    identity.unicode = unicode;
                    identity.octetmime = octetmime;
                    identity.sent_folder = null;
                    identity.sign_key = null;
                    identity.sign_key_alias = null;
                    identity.error = null;
                    identity.last_connected = last_connected;
                    identity.max_size = (user_max_size == null ? server_max_size : user_max_size);

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

                Core.clearIdentities();

                return false;
            }

            @Override
            protected void onExecuted(Bundle args, Boolean dirty) {
                if (dirty) {
                    Bundle aargs = new Bundle();
                    aargs.putString("question", getString(R.string.title_ask_save));

                    FragmentDialogAsk fragment = new FragmentDialogAsk();
                    fragment.setArguments(aargs);
                    fragment.setTargetFragment(FragmentIdentity.this, REQUEST_SAVE);
                    fragment.show(getParentFragmentManager(), "identity:save");
                } else if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    finish();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    showError(ex);
            }
        }.execute(this, args, "identity:save");
    }

    private void showError(Throwable ex) {
        tvError.setText(Log.formatThrowable(ex, false));
        grpError.setVisibility(View.VISIBLE);

        if (ex instanceof EmailService.UntrustedException) {
            X509Certificate certificate = ((EmailService.UntrustedException) ex).getCertificate();
            String fingerprint = EntityCertificate.getKeyFingerprint(certificate);
            cbTrust.setTag(fingerprint);
            cbTrust.setText(getString(R.string.title_trust, fingerprint));
            cbTrust.setVisibility(View.VISIBLE);
        }

        final EmailProvider provider = (EmailProvider) spProvider.getSelectedItem();
        if (provider != null && provider.link != null) {
            Uri uri = Uri.parse(provider.link);
            btnHelp.setTag(uri);
            btnHelp.setVisibility(View.VISIBLE);
        }

        btnSupport.setVisibility(View.VISIBLE);

        if (provider != null && provider.documentation != null) {
            tvInstructions.setText(HtmlHelper.fromHtml(provider.documentation.toString(), getContext()));
            tvInstructions.setVisibility(View.VISIBLE);
        }

        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;
                if (provider != null && provider.documentation != null)
                    scroll.smoothScrollTo(0, tvInstructions.getBottom());
                else
                    scroll.smoothScrollTo(0, btnSupport.getBottom());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("fair:account", spAccount == null ? 0 : spAccount.getSelectedItemPosition());
        outState.putInt("fair:provider", spProvider == null ? 0 : spProvider.getSelectedItemPosition());
        outState.putString("fair:certificate", certificate);
        outState.putString("fair:password", tilPassword == null ? null : tilPassword.getEditText().getText().toString());
        outState.putInt("fair:advanced", grpAdvanced == null ? View.VISIBLE : grpAdvanced.getVisibility());
        outState.putInt("fair:auth", auth);
        outState.putString("fair:authprovider", provider);
        outState.putString("fair:html", signature);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", copy < 0 ? id : copy);

        new SimpleTask<EntityIdentity>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected EntityIdentity onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                return DB.getInstance(context).identity().getIdentity(id);
            }

            @Override
            protected void onExecuted(Bundle args, final EntityIdentity identity) {
                if (savedInstanceState == null) {
                    etName.setText(identity == null ? null : identity.name);
                    etEmail.setText(identity == null ? null : identity.email);

                    etDisplay.setText(identity == null ? null : identity.display);
                    btnColor.setColor(identity == null ? null : identity.color);

                    if (signature == null)
                        signature = (identity == null ? null : identity.signature);

                    etHost.setText(identity == null ? null : identity.host);

                    if (identity != null && identity.encryption == EmailService.ENCRYPTION_STARTTLS)
                        rgEncryption.check(R.id.radio_starttls);
                    else if (identity != null && identity.encryption == EmailService.ENCRYPTION_NONE)
                        rgEncryption.check(R.id.radio_none);
                    else
                        rgEncryption.check(R.id.radio_ssl);

                    cbInsecure.setChecked(identity == null ? false : identity.insecure);
                    etPort.setText(identity == null ? null : Long.toString(identity.port));
                    etUser.setText(identity == null ? null : identity.user);
                    tilPassword.getEditText().setText(identity == null ? null : identity.password);
                    certificate = (identity == null ? null : identity.certificate_alias);
                    tvCertificate.setText(certificate == null ? getString(R.string.title_optional) : certificate);
                    etRealm.setText(identity == null ? null : identity.realm);

                    if (identity == null || identity.fingerprint == null) {
                        cbTrust.setTag(null);
                        cbTrust.setChecked(false);
                        cbTrust.setVisibility(View.GONE);
                    } else {
                        cbTrust.setTag(identity.fingerprint);
                        cbTrust.setChecked(true);
                        cbTrust.setText(getString(R.string.title_trust, identity.fingerprint));
                        cbTrust.setVisibility(View.VISIBLE);
                    }

                    cbUseIp.setChecked(identity == null ? true : identity.use_ip);
                    etEhlo.setText(identity == null ? null : identity.ehlo);
                    cbSynchronize.setChecked(identity == null ? true : identity.synchronize);
                    cbPrimary.setChecked(identity == null ? true : identity.primary);
                    cbSelf.setChecked(identity == null ? true : identity.self);

                    cbSenderExtra.setChecked(identity != null && identity.sender_extra);
                    cbSenderExtraName.setChecked(identity != null && identity.sender_extra_name);
                    etSenderExtra.setText(identity == null ? null : identity.sender_extra_regex);
                    etReplyTo.setText(identity == null ? null : identity.replyto);
                    etCc.setText(identity == null ? null : identity.cc);
                    etBcc.setText(identity == null ? null : identity.bcc);
                    etInternal.setText(identity == null ? null : identity.internal);
                    cbSignDefault.setChecked(identity != null && identity.sign_default);
                    cbEncryptDefault.setChecked(identity != null && identity.encrypt_default);
                    cbUnicode.setChecked(identity != null && identity.unicode);
                    cbOctetMime.setChecked(identity != null && identity.octetmime);

                    auth = (identity == null ? AUTH_TYPE_PASSWORD : identity.auth_type);
                    provider = (identity == null ? null : identity.provider);

                    if (identity == null || copy > 0)
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
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(FragmentIdentity.this, new Bundle(), "identity:count");
                } else {
                    certificate = savedInstanceState.getString("fair:certificate");
                    tvCertificate.setText(certificate == null ? getString(R.string.title_optional) : certificate);

                    tilPassword.getEditText().setText(savedInstanceState.getString("fair:password"));
                    grpAdvanced.setVisibility(savedInstanceState.getInt("fair:advanced"));
                    auth = savedInstanceState.getInt("fair:auth");
                    provider = savedInstanceState.getString("fair:authprovider");
                    if (signature == null)
                        signature = savedInstanceState.getString("fair:html");
                }

                Helper.setViewsEnabled(view, true);

                if (auth != AUTH_TYPE_PASSWORD) {
                    etUser.setEnabled(false);
                    tilPassword.getEditText().setEnabled(false);
                    btnCertificate.setEnabled(false);

                    tilPassword.setEndIconDrawable(R.drawable.twotone_edit_24);
                    tilPassword.setEndIconOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), FragmentIdentity.this, view);

                            popupMenu.getMenu().add(Menu.NONE, R.string.title_account_auth_password, 1, R.string.title_account_auth_password);

                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    int id = item.getItemId();
                                    if (id == R.string.title_account_auth_password) {
                                        onPassword();
                                        return true;
                                    } else
                                        return false;
                                }

                                private void onPassword() {
                                    auth = AUTH_TYPE_PASSWORD;
                                    etUser.setEnabled(true);
                                    tilPassword.getEditText().setText(null);
                                    tilPassword.getEditText().setEnabled(true);
                                    tilPassword.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
                                    tilPassword.requestFocus();
                                }
                            });

                            popupMenu.show();
                        }
                    });
                }

                cbPrimary.setEnabled(cbSynchronize.isChecked());
                cbSenderExtraName.setEnabled(cbSenderExtra.isChecked());

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean sign_default = prefs.getBoolean("sign_default", false);
                boolean encrypt_default = prefs.getBoolean("encrypt_default", false);
                cbSignDefault.setEnabled(!sign_default && !cbEncryptDefault.isChecked());
                cbEncryptDefault.setEnabled(!encrypt_default);

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
                        for (int pos = 1; pos < providers.size(); pos++) {
                            EmailProvider provider = providers.get(pos);
                            if ((provider.oauth != null) == (identity.auth_type == AUTH_TYPE_OAUTH) &&
                                    provider.smtp.host.equals(identity.host) &&
                                    provider.smtp.port == identity.port &&
                                    provider.smtp.starttls == (identity.encryption == EmailService.ENCRYPTION_STARTTLS)) {
                                spProvider.setTag(pos);
                                spProvider.setSelection(pos);
                                break;
                            }
                        }
                } else {
                    int provider = savedInstanceState.getInt("fair:provider");
                    spProvider.setTag(provider);
                    spProvider.setSelection(provider);
                }

                // Get accounts
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
                        unselected.auth_type = AUTH_TYPE_PASSWORD;
                        unselected.name = getString(R.string.title_select);
                        unselected.primary = false;
                        accounts.add(0, unselected);

                        ArrayAdapter<EntityAccount> aaAccount =
                                new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, accounts);
                        aaAccount.setDropDownViewResource(R.layout.spinner_item1_dropdown);
                        spAccount.setAdapter(aaAccount);

                        if (savedInstanceState == null) {
                            spAccount.setTag(0);
                            spAccount.setSelection(0);
                            for (int pos = 0; pos < accounts.size(); pos++) {
                                EntityAccount account = accounts.get(pos);
                                if (account.id.equals(identity == null ? FragmentIdentity.this.account : identity.account)) {
                                    if (identity != null)
                                        spAccount.setTag(pos);
                                    spAccount.setSelection(pos);
                                    break;
                                }
                            }
                        } else {
                            int account = savedInstanceState.getInt("fair:account");
                            spAccount.setTag(account);
                            spAccount.setSelection(account);
                        }
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentIdentity.this, args, "identity:accounts:get");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
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
        if (item.getItemId() == R.id.menu_delete) {
            onMenuDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuDelete() {
        Bundle aargs = new Bundle();
        aargs.putString("question", getString(R.string.title_identity_delete));
        aargs.putBoolean("warning", true);

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(aargs);
        fragment.setTargetFragment(FragmentIdentity.this, REQUEST_DELETE);
        fragment.show(getParentFragmentManager(), "identity:delete");
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
                case REQUEST_SIGNATURE:
                    if (resultCode == RESULT_OK && data != null)
                        onHtml(data.getExtras());
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
                db.identity().deleteIdentity(id);

                Core.clearIdentities();

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
        }.execute(this, args, "identity:delete");
    }

    private void onHtml(Bundle args) {
        signature = args.getString("html");
    }
}
