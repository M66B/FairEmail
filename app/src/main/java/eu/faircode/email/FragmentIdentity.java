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

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static android.app.Activity.RESULT_OK;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_NONE;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;

public class FragmentIdentity extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private EditText etName;
    private EditText etEmail;

    private Spinner spAccount;

    private EditText etDisplay;
    private Button btnColor;
    private View vwColor;
    private ImageButton ibColorDefault;
    private TextView tvColorPro;
    private EditText etSignature;
    private Button btnHtml;

    private Button btnAdvanced;
    private Spinner spProvider;
    private EditText etDomain;
    private Button btnAutoConfig;
    private ContentLoadingProgressBar pbAutoConfig;
    private EditText etHost;
    private RadioGroup rgEncryption;
    private CheckBox cbInsecure;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private EditText etRealm;
    private CheckBox cbUseIp;

    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;

    private CheckBox cbSenderExtra;
    private TextView etSenderExtra;
    private EditText etReplyTo;
    private EditText etBcc;
    private TextView tvEncryptPro;
    private CheckBox cbEncrypt;
    private CheckBox cbDeliveryReceipt;
    private CheckBox cbReadReceipt;

    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private TextView tvError;
    private Button btnHelp;
    private Button btnSupport;
    private TextView tvInstructions;

    private ContentLoadingProgressBar pbWait;

    private Group grpAuthorize;
    private Group grpAdvanced;

    private long id = -1;
    private long copy = -1;
    private int auth = MailService.AUTH_TYPE_PASSWORD;
    private boolean saving = false;
    private int color = Color.TRANSPARENT;

    private static final int REQUEST_COLOR = 1;
    private static final int REQUEST_SAVE = 2;
    private static final int REQUEST_DELETE = 3;
    private static final int REQUEST_HTML = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        if (args.getBoolean("copy"))
            copy = args.getLong("id", -1);
        else
            id = args.getLong("id", -1);
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
        vwColor = view.findViewById(R.id.vwColor);
        ibColorDefault = view.findViewById(R.id.ibColorDefault);
        tvColorPro = view.findViewById(R.id.tvColorPro);
        etSignature = view.findViewById(R.id.etSignature);
        btnHtml = view.findViewById(R.id.btnHtml);

        btnAdvanced = view.findViewById(R.id.btnAdvanced);
        spProvider = view.findViewById(R.id.spProvider);

        etDomain = view.findViewById(R.id.etDomain);
        btnAutoConfig = view.findViewById(R.id.btnAutoConfig);
        pbAutoConfig = view.findViewById(R.id.pbAutoConfig);

        etHost = view.findViewById(R.id.etHost);
        rgEncryption = view.findViewById(R.id.rgEncryption);
        cbInsecure = view.findViewById(R.id.cbInsecure);
        etPort = view.findViewById(R.id.etPort);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        etRealm = view.findViewById(R.id.etRealm);
        cbUseIp = view.findViewById(R.id.cbUseIp);

        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);

        cbSenderExtra = view.findViewById(R.id.cbSenderExtra);
        etSenderExtra = view.findViewById(R.id.etSenderExtra);
        etReplyTo = view.findViewById(R.id.etReplyTo);
        etBcc = view.findViewById(R.id.etBcc);
        tvEncryptPro = view.findViewById(R.id.tvEncryptPro);
        cbEncrypt = view.findViewById(R.id.cbEncrypt);
        cbDeliveryReceipt = view.findViewById(R.id.cbDeliveryReceipt);
        cbReadReceipt = view.findViewById(R.id.cbReadReceipt);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        tvError = view.findViewById(R.id.tvError);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnSupport = view.findViewById(R.id.btnSupport);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        tvInstructions.setMovementMethod(LinkMovementMethod.getInstance());

        pbWait = view.findViewById(R.id.pbWait);

        grpAuthorize = view.findViewById(R.id.grpAuthorize);
        grpAdvanced = view.findViewById(R.id.grpAdvanced);

        // Wire controls

        spAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                grpAuthorize.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                if (position == 0) {
                    tvError.setVisibility(View.GONE);
                    btnHelp.setVisibility(View.GONE);
                    btnSupport.setVisibility(View.GONE);
                    tvInstructions.setVisibility(View.GONE);
                    grpAdvanced.setVisibility(View.GONE);
                }
                tilPassword.setEndIconMode(position == 0 ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);

                Integer tag = (Integer) adapterView.getTag();
                if (Objects.equals(tag, position))
                    return;
                adapterView.setTag(position);

                EntityAccount account = (EntityAccount) adapterView.getAdapter().getItem(position);

                // Select associated provider
                if (position == 0)
                    spProvider.setSelection(0);
                else {
                    boolean found = false;
                    for (int pos = 1; pos < spProvider.getAdapter().getCount(); pos++) {
                        EmailProvider provider = (EmailProvider) spProvider.getItemAtPosition(pos);
                        if (provider.imap.host.equals(account.host) &&
                                provider.imap.port == account.port &&
                                provider.imap.starttls == account.starttls) {
                            found = true;

                            spProvider.setSelection(pos);

                            // This is needed because the spinner might be invisible
                            etHost.setText(provider.smtp.host);
                            etPort.setText(Integer.toString(provider.smtp.port));
                            rgEncryption.check(provider.smtp.starttls ? R.id.radio_starttls : R.id.radio_ssl);

                            break;
                        }
                    }
                    if (!found)
                        grpAdvanced.setVisibility(View.VISIBLE);
                }

                // Copy account credentials
                etEmail.setText(account.user);
                etUser.setText(account.user);
                tilPassword.getEditText().setText(account.password);
                etRealm.setText(account.realm);
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
                String[] email = editable.toString().split("@");
                etDomain.setText(email.length < 2 ? null : email[1]);
            }
        });

        setColor(color);
        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.initialize(R.string.title_flag_color, color, new Bundle(), getContext());
                fragment.setTargetFragment(FragmentIdentity.this, REQUEST_COLOR);
                fragment.show(getFragmentManager(), "identity:color");
            }
        });

        ibColorDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(Color.TRANSPARENT);
            }
        });

        Helper.linkPro(tvColorPro);

        etSignature.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                SpannableStringBuilder ssb = new SpannableStringBuilder(editable);
                Helper.clearComposingText(ssb);
                if (TextUtils.isEmpty(editable.toString()))
                    etSignature.setTag(null);
                else
                    etSignature.setTag(HtmlHelper.toHtml(ssb));
            }
        });

        btnHtml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("html", (String) etSignature.getTag());

                FragmentDialogHtml fragment = new FragmentDialogHtml();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentIdentity.this, REQUEST_HTML);
                fragment.show(getFragmentManager(), "identity:html");
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
                            scroll.smoothScrollTo(0, btnAdvanced.getTop());
                        }
                    });
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

                // Set associated host/port/starttls
                etHost.setText(provider.smtp.host);
                etPort.setText(position == 0 ? null : Integer.toString(provider.smtp.port));
                rgEncryption.check(provider.smtp.starttls ? R.id.radio_starttls : R.id.radio_ssl);
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
                etPort.setHint(id == R.id.radio_starttls ? "587" : "465");
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
                onSave(false);
            }
        });

        addBackPressedListener(new ActivityBase.IBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                onSave(true);
                return true;
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
            public void onClick(View view) {
                Helper.view(getContext(), Uri.parse(Helper.FAQ_URI + "#user-content-authorizing-accounts"), false);
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);
        btnAutoConfig.setEnabled(false);
        pbAutoConfig.setVisibility(View.GONE);
        cbInsecure.setVisibility(View.GONE);
        tilPassword.setEndIconMode(id < 0 ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);

        Helper.linkPro(tvEncryptPro);

        btnAdvanced.setVisibility(View.GONE);

        btnSave.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        btnHelp.setVisibility(View.GONE);
        btnSupport.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);

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
                return EmailProvider.fromDomain(context, domain, EmailProvider.Discover.SMTP);
            }

            @Override
            protected void onExecuted(Bundle args, EmailProvider provider) {
                etHost.setText(provider.smtp.host);
                etPort.setText(Integer.toString(provider.smtp.port));
                rgEncryption.check(provider.smtp.starttls ? R.id.radio_starttls : R.id.radio_ssl);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException || ex instanceof UnknownHostException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "identity:config");
    }

    private void onSave(boolean should) {
        EntityAccount account = (EntityAccount) spAccount.getSelectedItem();

        String name = etName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            CharSequence hint = etName.getHint();
            if (!TextUtils.isEmpty(hint))
                name = hint.toString();
        }

        etSignature.clearComposingText();

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("name", name);
        args.putString("email", etEmail.getText().toString().trim());
        args.putString("display", etDisplay.getText().toString());
        args.putBoolean("sender_extra", cbSenderExtra.isChecked());
        args.putString("sender_extra_regex", etSenderExtra.getText().toString());
        args.putString("replyto", etReplyTo.getText().toString().trim());
        args.putString("bcc", etBcc.getText().toString().trim());
        args.putBoolean("encrypt", cbEncrypt.isChecked());
        args.putBoolean("delivery_receipt", cbDeliveryReceipt.isChecked());
        args.putBoolean("read_receipt", cbReadReceipt.isChecked());
        args.putLong("account", account == null ? -1 : account.id);
        args.putString("host", etHost.getText().toString());
        args.putBoolean("starttls", rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putInt("auth", auth);
        args.putString("user", etUser.getText().toString());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putString("realm", etRealm.getText().toString());
        args.putBoolean("use_ip", cbUseIp.isChecked());
        args.putInt("color", color);
        args.putString("signature", (String) etSignature.getTag());
        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("primary", cbPrimary.isChecked());

        args.putBoolean("should", should);

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbSave.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.GONE);
                btnHelp.setVisibility(View.GONE);
                btnSupport.setVisibility(View.GONE);
                tvInstructions.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                saving = false;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, true);
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
                boolean starttls = args.getBoolean("starttls");
                boolean insecure = args.getBoolean("insecure");
                String port = args.getString("port");
                int auth = args.getInt("auth");
                String user = args.getString("user").trim();
                String password = args.getString("password");
                String realm = args.getString("realm");
                boolean use_ip = args.getBoolean("use_ip");
                boolean synchronize = args.getBoolean("synchronize");
                boolean primary = args.getBoolean("primary");

                boolean sender_extra = args.getBoolean("sender_extra");
                String sender_extra_regex = args.getString("sender_extra_regex");
                String replyto = args.getString("replyto");
                String bcc = args.getString("bcc");
                boolean encrypt = args.getBoolean("encrypt");
                boolean delivery_receipt = args.getBoolean("delivery_receipt");
                boolean read_receipt = args.getBoolean("read_receipt");

                boolean should = args.getBoolean("should");

                if (host.contains(":")) {
                    Uri h = Uri.parse(host);
                    host = h.getHost();
                }

                if (!should && TextUtils.isEmpty(name))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_name));
                if (!should && TextUtils.isEmpty(email))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_email));
                if (!should && !Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, email));
                if (!should && TextUtils.isEmpty(host))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (starttls ? "587" : "465");
                if (!should && TextUtils.isEmpty(user))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (!should && synchronize && TextUtils.isEmpty(password) && !insecure)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));

                if (!should && !TextUtils.isEmpty(replyto)) {
                    try {
                        InternetAddress[] addresses = InternetAddress.parse(replyto);
                        if (addresses.length != 1)
                            throw new AddressException();
                    } catch (AddressException ex) {
                        throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, replyto));
                    }
                }
                if (!should && !TextUtils.isEmpty(bcc))
                    try {
                        InternetAddress.parse(bcc);
                    } catch (AddressException ex) {
                        throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, bcc));
                    }

                if (TextUtils.isEmpty(display))
                    display = null;

                if (TextUtils.isEmpty(realm))
                    realm = null;

                if (TextUtils.isEmpty(sender_extra_regex))
                    sender_extra_regex = null;

                if (TextUtils.isEmpty(replyto))
                    replyto = null;

                if (TextUtils.isEmpty(bcc))
                    bcc = null;

                if (color == Color.TRANSPARENT || !ActivityBilling.isPro(context))
                    color = null;
                if (TextUtils.isEmpty(signature))
                    signature = null;

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
                    if (!Objects.equals(identity.starttls, starttls))
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
                    if (!Objects.equals(identity.realm, realm))
                        return true;
                    if (!Objects.equals(identity.use_ip, use_ip))
                        return true;
                    if (!Objects.equals(identity.synchronize, synchronize))
                        return true;
                    if (!Objects.equals(identity.primary, (identity.synchronize && primary)))
                        return true;
                    if (!Objects.equals(identity.sender_extra, sender_extra))
                        return true;
                    if (!Objects.equals(identity.sender_extra_regex, sender_extra_regex))
                        return true;
                    if (!Objects.equals(identity.replyto, replyto))
                        return true;
                    if (!Objects.equals(identity.bcc, bcc))
                        return true;
                    if (!Objects.equals(identity.encrypt, encrypt))
                        return true;
                    if (!Objects.equals(identity.delivery_receipt, delivery_receipt))
                        return true;
                    if (!Objects.equals(identity.read_receipt, read_receipt))
                        return true;
                    if (identity.error != null)
                        return true;

                    return false;
                }

                String identityRealm = (identity == null ? null : identity.realm);

                boolean check = (synchronize && (identity == null ||
                        !identity.synchronize || identity.error != null ||
                        !identity.insecure.equals(insecure) ||
                        !host.equals(identity.host) || Integer.parseInt(port) != identity.port ||
                        !user.equals(identity.user) || !password.equals(identity.password) ||
                        !Objects.equals(realm, identityRealm) ||
                        use_ip != identity.use_ip));
                Log.i("Identity check=" + check);

                Long last_connected = null;
                if (identity != null && synchronize == identity.synchronize)
                    last_connected = identity.last_connected;

                // Check SMTP server
                if (check) {
                    // Create transport
                    String protocol = (starttls ? "smtp" : "smtps");
                    try (MailService iservice = new MailService(context, protocol, realm, insecure, true)) {
                        iservice.setUseIp(use_ip);
                        iservice.connect(host, Integer.parseInt(port), auth, user, password);
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

                    identity.host = host;
                    identity.starttls = starttls;
                    identity.insecure = insecure;
                    identity.port = Integer.parseInt(port);
                    identity.auth_type = auth;
                    if (auth == MailService.AUTH_TYPE_PASSWORD) {
                        identity.user = user;
                        identity.password = password;
                    }
                    identity.realm = realm;
                    identity.use_ip = use_ip;
                    identity.synchronize = synchronize;
                    identity.primary = (identity.synchronize && primary);

                    identity.sender_extra = sender_extra;
                    identity.sender_extra_regex = sender_extra_regex;
                    identity.replyto = replyto;
                    identity.bcc = bcc;
                    identity.encrypt = encrypt;
                    identity.delivery_receipt = delivery_receipt;
                    identity.read_receipt = read_receipt;
                    identity.sent_folder = null;
                    identity.sign_key = null;
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

                if (!synchronize) {
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel("send:" + identity.id, 1);
                }

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
                    fragment.show(getFragmentManager(), "identity:save");
                } else if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    showError(ex);
            }
        }.execute(this, args, "identity:save");
    }

    private void showError(Throwable ex) {
        tvError.setText(Helper.formatThrowable(ex, false));
        tvError.setVisibility(View.VISIBLE);

        final EmailProvider provider = (EmailProvider) spProvider.getSelectedItem();
        if (provider != null && provider.link != null) {
            Uri uri = Uri.parse(provider.link);
            btnHelp.setTag(uri);
            btnHelp.setVisibility(View.VISIBLE);
        }

        btnSupport.setVisibility(View.VISIBLE);

        if (provider != null && provider.documentation != null) {
            tvInstructions.setText(HtmlHelper.fromHtml(provider.documentation.toString()));
            tvInstructions.setVisibility(View.VISIBLE);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (provider != null && provider.documentation != null)
                    scroll.smoothScrollTo(0, tvInstructions.getBottom());
                else
                    scroll.smoothScrollTo(0, btnSupport.getBottom());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("fair:account", spAccount.getSelectedItemPosition());
        outState.putInt("fair:provider", spProvider.getSelectedItemPosition());
        outState.putString("fair:password", tilPassword.getEditText().getText().toString());
        outState.putInt("fair:advanced", grpAdvanced.getVisibility());
        outState.putInt("fair:auth", auth);
        outState.putInt("fair:color", color);
        outState.putString("fair:html", (String) etSignature.getTag());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", copy < 0 ? id : copy);

        new SimpleTask<EntityIdentity>() {
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

                    String signature = (identity == null ? null : identity.signature);
                    etSignature.setText(TextUtils.isEmpty(signature) ? null : HtmlHelper.fromHtml(signature));
                    etSignature.setTag(signature);

                    etHost.setText(identity == null ? null : identity.host);
                    rgEncryption.check(identity != null && identity.starttls ? R.id.radio_starttls : R.id.radio_ssl);
                    cbInsecure.setChecked(identity == null ? false : identity.insecure);
                    etPort.setText(identity == null ? null : Long.toString(identity.port));
                    etUser.setText(identity == null ? null : identity.user);
                    tilPassword.getEditText().setText(identity == null ? null : identity.password);
                    etRealm.setText(identity == null ? null : identity.realm);
                    cbUseIp.setChecked(identity == null ? true : identity.use_ip);
                    cbSynchronize.setChecked(identity == null ? true : identity.synchronize);
                    cbPrimary.setChecked(identity == null ? true : identity.primary);

                    cbSenderExtra.setChecked(identity != null && identity.sender_extra);
                    etSenderExtra.setText(identity == null ? null : identity.sender_extra_regex);
                    etReplyTo.setText(identity == null ? null : identity.replyto);
                    etBcc.setText(identity == null ? null : identity.bcc);
                    cbEncrypt.setChecked(identity == null ? false : identity.encrypt);
                    cbDeliveryReceipt.setChecked(identity == null ? false : identity.delivery_receipt);
                    cbReadReceipt.setChecked(identity == null ? false : identity.read_receipt);

                    auth = (identity == null ? MailService.AUTH_TYPE_PASSWORD : identity.auth_type);
                    color = (identity == null || identity.color == null ? Color.TRANSPARENT : identity.color);

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
                                Helper.unexpectedError(getFragmentManager(), ex);
                            }
                        }.execute(FragmentIdentity.this, new Bundle(), "identity:count");
                } else {
                    tilPassword.getEditText().setText(savedInstanceState.getString("fair:password"));
                    grpAdvanced.setVisibility(savedInstanceState.getInt("fair:advanced"));
                    auth = savedInstanceState.getInt("fair:auth");
                    color = savedInstanceState.getInt("fair:color");
                    etSignature.setTag(savedInstanceState.getString("fair:html"));
                }

                Helper.setViewsEnabled(view, true);

                if (auth != MailService.AUTH_TYPE_PASSWORD) {
                    etUser.setEnabled(false);
                    tilPassword.setEnabled(false);
                }

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
                                for (int pos = 1; pos < providers.size(); pos++) {
                                    EmailProvider provider = providers.get(pos);
                                    if (provider.smtp.host.equals(identity.host) &&
                                            provider.smtp.port == identity.port &&
                                            provider.smtp.starttls == identity.starttls) {
                                        spProvider.setTag(pos);
                                        spProvider.setSelection(pos);
                                        break;
                                    }
                                }

                            spAccount.setTag(0);
                            spAccount.setSelection(0);
                            for (int pos = 0; pos < accounts.size(); pos++) {
                                EntityAccount account = accounts.get(pos);
                                if (account.id.equals((identity == null ? -1 : identity.account))) {
                                    spAccount.setTag(pos);
                                    spAccount.setSelection(pos);
                                    break;
                                }
                            }
                        } else {
                            int provider = savedInstanceState.getInt("fair:provider");
                            spProvider.setTag(provider);
                            spProvider.setSelection(provider);

                            int account = savedInstanceState.getInt("fair:account");
                            spAccount.setTag(account);
                            spAccount.setSelection(account);
                        }
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getFragmentManager(), ex);
                    }
                }.execute(FragmentIdentity.this, args, "identity:accounts:get");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
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
        Bundle aargs = new Bundle();
        aargs.putString("question", getString(R.string.title_identity_delete));

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(aargs);
        fragment.setTargetFragment(FragmentIdentity.this, REQUEST_DELETE);
        fragment.show(getFragmentManager(), "identity:delete");
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
                            setColor(args.getInt("color"));
                        } else
                            startActivity(new Intent(getContext(), ActivityBilling.class));
                    }
                    break;
                case REQUEST_SAVE:
                    if (resultCode == RESULT_OK) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                scroll.smoothScrollTo(0, btnSave.getBottom());
                            }
                        });
                        onSave(false);
                    } else if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        getFragmentManager().popBackStack();
                    break;
                case REQUEST_DELETE:
                    if (resultCode == RESULT_OK)
                        onDelete();
                    break;
                case REQUEST_HTML:
                    if (resultCode == RESULT_OK && data != null)
                        onHtml(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void setColor(int color) {
        this.color = color;

        GradientDrawable border = new GradientDrawable();
        border.setColor(color);
        border.setStroke(1, Helper.resolveColor(getContext(), R.attr.colorSeparator));
        vwColor.setBackground(border);
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
                db.identity().setIdentityTbd(id);

                ServiceSynchronize.reload(context, "delete identity");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "identity:delete");
    }

    private void onHtml(Bundle args) {
        String html = args.getString("html");
        etSignature.setText(HtmlHelper.fromHtml(html));
        etSignature.setTag(html);
    }

    public static class FragmentDialogHtml extends FragmentDialogBase {
        private EditText etHtml;

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            outState.putString("fair:html", etHtml.getText().toString());
            super.onSaveInstanceState(outState);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            String html;
            if (savedInstanceState == null)
                html = getArguments().getString("html");
            else
                html = savedInstanceState.getString("fair:html");

            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_signature, null);
            etHtml = dview.findViewById(R.id.etHtml);
            etHtml.setText(html);

            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.title_edit_html)
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String html = etHtml.getText().toString();
                            getArguments().putString("html", html);
                            sendResult(RESULT_OK);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
