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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
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
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.mail.Folder;

import static android.app.Activity.RESULT_OK;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_NONE;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_GMAIL;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_OAUTH;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

public class FragmentAccount extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private Spinner spProvider;
    private TextView tvGmailHint;

    private EditText etDomain;
    private Button btnAutoConfig;
    private ContentLoadingProgressBar pbAutoConfig;

    private EditText etHost;
    private RadioGroup rgEncryption;
    private CheckBox cbInsecure;
    private EditText etPort;
    private ImageButton ibAccount;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private TextView tvCharacters;
    private Button btnCertificate;
    private TextView tvCertificate;
    private EditText etRealm;

    private EditText etName;
    private ViewButtonColor btnColor;
    private TextView tvColorPro;

    private Button btnAdvanced;
    private CheckBox cbSynchronize;
    private CheckBox cbOnDemand;
    private TextView tvLeave;
    private CheckBox cbPrimary;
    private CheckBox cbNotify;
    private TextView tvNotifyPro;
    private CheckBox cbBrowse;
    private CheckBox cbAutoSeen;
    private EditText etInterval;
    private CheckBox cbPartialFetch;
    private CheckBox cbIgnoreSize;
    private RadioGroup rgDate;

    private Button btnCheck;
    private ContentLoadingProgressBar pbCheck;
    private TextView tvIdle;
    private TextView tvUtf8;

    private ArrayAdapter<EntityFolder> adapter;
    private Spinner spDrafts;
    private Spinner spSent;
    private Spinner spArchive;
    private Spinner spTrash;
    private Spinner spJunk;

    private ArrayAdapter<EntityFolder> adapterSwipe;
    private Spinner spLeft;
    private Spinner spRight;

    private Spinner spMove;

    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private CheckBox cbIdentity;
    private TextView tvError;
    private CheckBox cbTrust;
    private Button btnHelp;
    private Button btnSupport;
    private TextView tvInstructions;

    private ContentLoadingProgressBar pbWait;

    private Group grpServer;
    private Group grpAuthorize;
    private Group grpAdvanced;
    private Group grpFolders;
    private Group grpError;

    private long id = -1;
    private long copy = -1;
    private int auth = AUTH_TYPE_PASSWORD;
    private String provider = null;
    private String certificate = null;
    private boolean saving = false;

    private static final int REQUEST_COLOR = 1;
    private static final int REQUEST_SAVE = 2;
    private static final int REQUEST_DELETE = 3;

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
        setSubtitle(R.string.title_edit_account);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_account, container, false);
        scroll = view.findViewById(R.id.scroll);

        // Get controls
        spProvider = view.findViewById(R.id.spProvider);
        tvGmailHint = view.findViewById(R.id.tvGmailHint);

        etDomain = view.findViewById(R.id.etDomain);
        btnAutoConfig = view.findViewById(R.id.btnAutoConfig);
        pbAutoConfig = view.findViewById(R.id.pbAutoConfig);

        etHost = view.findViewById(R.id.etHost);
        etPort = view.findViewById(R.id.etPort);
        rgEncryption = view.findViewById(R.id.rgEncryption);
        cbInsecure = view.findViewById(R.id.cbInsecure);
        ibAccount = view.findViewById(R.id.ibAccount);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvCharacters = view.findViewById(R.id.tvCharacters);
        btnCertificate = view.findViewById(R.id.btnCertificate);
        tvCertificate = view.findViewById(R.id.tvCertificate);
        etRealm = view.findViewById(R.id.etRealm);

        etName = view.findViewById(R.id.etName);
        btnColor = view.findViewById(R.id.btnColor);
        tvColorPro = view.findViewById(R.id.tvColorPro);

        btnAdvanced = view.findViewById(R.id.btnAdvanced);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbOnDemand = view.findViewById(R.id.cbOnDemand);
        tvLeave = view.findViewById(R.id.tvLeave);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbNotify = view.findViewById(R.id.cbNotify);
        tvNotifyPro = view.findViewById(R.id.tvNotifyPro);
        cbBrowse = view.findViewById(R.id.cbBrowse);
        cbAutoSeen = view.findViewById(R.id.cbAutoSeen);
        etInterval = view.findViewById(R.id.etInterval);
        cbPartialFetch = view.findViewById(R.id.cbPartialFetch);
        cbIgnoreSize = view.findViewById(R.id.cbIgnoreSize);
        rgDate = view.findViewById(R.id.rgDate);

        btnCheck = view.findViewById(R.id.btnCheck);
        pbCheck = view.findViewById(R.id.pbCheck);

        tvIdle = view.findViewById(R.id.tvIdle);
        tvUtf8 = view.findViewById(R.id.tvUtf8);

        spDrafts = view.findViewById(R.id.spDrafts);
        spSent = view.findViewById(R.id.spSent);
        spArchive = view.findViewById(R.id.spArchive);
        spTrash = view.findViewById(R.id.spTrash);
        spJunk = view.findViewById(R.id.spJunk);
        spLeft = view.findViewById(R.id.spLeft);
        spRight = view.findViewById(R.id.spRight);
        spMove = view.findViewById(R.id.spMove);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        cbIdentity = view.findViewById(R.id.cbIdentity);

        tvError = view.findViewById(R.id.tvError);
        cbTrust = view.findViewById(R.id.cbTrust);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnSupport = view.findViewById(R.id.btnSupport);
        tvInstructions = view.findViewById(R.id.tvInstructions);

        pbWait = view.findViewById(R.id.pbWait);

        grpServer = view.findViewById(R.id.grpServer);
        grpAuthorize = view.findViewById(R.id.grpAuthorize);
        grpAdvanced = view.findViewById(R.id.grpAdvanced);
        grpFolders = view.findViewById(R.id.grpFolders);
        grpError = view.findViewById(R.id.grpError);

        // Wire controls

        spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long itemid) {
                EmailProvider provider = (EmailProvider) adapterView.getSelectedItem();
                tvGmailHint.setVisibility(
                        auth == AUTH_TYPE_PASSWORD && "gmail".equals(provider.id)
                                ? View.VISIBLE : View.GONE);
                grpServer.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                grpAuthorize.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

                btnAdvanced.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                if (position == 0)
                    grpAdvanced.setVisibility(View.GONE);

                btnCheck.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                tvIdle.setVisibility(View.GONE);
                tvUtf8.setVisibility(View.GONE);

                Object tag = adapterView.getTag();
                if (tag != null && (Integer) tag == position)
                    return;
                adapterView.setTag(position);

                etHost.setText(provider.imap.host);
                etPort.setText(provider.imap.host == null ? null : Integer.toString(provider.imap.port));
                rgEncryption.check(provider.imap.starttls ? R.id.radio_starttls : R.id.radio_ssl);

                etUser.setTag(null);
                etUser.setText(null);
                tilPassword.getEditText().setText(null);
                certificate = null;
                tvCertificate.setText(R.string.title_optional);
                etRealm.setText(null);
                cbTrust.setChecked(false);

                etName.setText(position > 1 ? provider.name : null);
                etInterval.setText(provider.keepalive > 0 ? Integer.toString(provider.keepalive) : null);
                cbPartialFetch.setChecked(provider.partial);

                grpFolders.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
                cbIdentity.setVisibility(View.GONE);
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
                etPort.setHint(id == R.id.radio_ssl ? "993" : "143");
            }
        });

        ibAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sync = new Intent(Settings.ACTION_SYNC_SETTINGS)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (sync.resolveActivity(v.getContext().getPackageManager()) != null)
                    v.getContext().startActivity(sync);
            }
        });

        tilPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s))
                    tilPassword.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                boolean warning = (Helper.containsWhiteSpace(password) ||
                        Helper.containsControlChars(password));
                tvCharacters.setVisibility(warning &&
                        tilPassword.getVisibility() == View.VISIBLE
                        ? View.VISIBLE : View.GONE);
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
                        tvCertificate.setText(getString(R.string.title_optional));
                    }
                });
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
                fragment.setTargetFragment(FragmentAccount.this, REQUEST_COLOR);
                fragment.show(getParentFragmentManager(), "account:color");
            }
        });

        Helper.linkPro(tvColorPro);

        btnAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = (grpAdvanced.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                grpAdvanced.setVisibility(visibility);
                if (visibility == View.VISIBLE)
                    getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            scroll.smoothScrollTo(0, btnAdvanced.getTop());
                        }
                    });
            }
        });

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbOnDemand.setEnabled(checked);
                cbPrimary.setEnabled(checked);
            }
        });

        tvLeave.getPaint().setUnderlineText(true);
        tvLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(getContext(), 134);
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Helper.hide(cbNotify);
            Helper.hide(view.findViewById(R.id.tvNotifyPro));
        }

        Helper.linkPro(tvNotifyPro);

        etInterval.setHint(Integer.toString(EntityAccount.DEFAULT_KEEP_ALIVE_INTERVAL));

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheck();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave(false);
            }
        });

        addKeyPressedListener(new ActivityBase.IKeyPressedListener() {
            @Override
            public boolean onKeyPressed(KeyEvent event) {
                return false;
            }

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
                Helper.view(getContext(), Uri.parse(Helper.SUPPORT_URI), false);
            }
        });

        adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityFolder>());
        adapter.setDropDownViewResource(R.layout.spinner_item1_dropdown);

        spDrafts.setAdapter(adapter);
        spSent.setAdapter(adapter);
        spArchive.setAdapter(adapter);
        spTrash.setAdapter(adapter);
        spJunk.setAdapter(adapter);

        adapterSwipe = new ArrayAdapter<EntityFolder>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityFolder>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return localize(position, super.getView(position, convertView, parent));
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return localize(position, super.getDropDownView(position, convertView, parent));
            }

            private View localize(int position, View view) {
                EntityFolder folder = getItem(position);
                if (folder != null) {
                    TextView tv = view.findViewById(android.R.id.text1);
                    tv.setText(EntityFolder.localizeName(view.getContext(), folder.name));
                }
                return view;
            }
        };
        adapterSwipe.setDropDownViewResource(R.layout.spinner_item1_dropdown);

        spLeft.setAdapter(adapterSwipe);
        spRight.setAdapter(adapterSwipe);

        spMove.setAdapter(adapter);

        // Initialize
        Helper.setViewsEnabled(view, false);

        tvGmailHint.setVisibility(View.GONE);

        btnAutoConfig.setEnabled(false);
        pbAutoConfig.setVisibility(View.GONE);

        rgEncryption.setVisibility(View.GONE);
        cbInsecure.setVisibility(View.GONE);
        tilPassword.setEndIconMode(id < 0 || Helper.isSecure(getContext()) ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);
        tvCharacters.setVisibility(View.GONE);

        btnAdvanced.setVisibility(View.GONE);

        tvIdle.setVisibility(View.GONE);
        tvUtf8.setVisibility(View.GONE);

        btnCheck.setVisibility(View.GONE);
        pbCheck.setVisibility(View.GONE);

        btnSave.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        cbIdentity.setVisibility(View.GONE);

        cbTrust.setVisibility(View.GONE);
        btnHelp.setVisibility(View.GONE);
        btnSupport.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);
        tvInstructions.setMovementMethod(LinkMovementMethod.getInstance());

        grpServer.setVisibility(View.GONE);
        grpAuthorize.setVisibility(View.GONE);
        grpAdvanced.setVisibility(View.GONE);
        grpFolders.setVisibility(View.GONE);
        grpError.setVisibility(View.GONE);

        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    private void onAutoConfig() {
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
                return EmailProvider.fromDomain(context, domain, EmailProvider.Discover.IMAP);
            }

            @Override
            protected void onExecuted(Bundle args, EmailProvider provider) {
                etHost.setText(provider.imap.host);
                etPort.setText(Integer.toString(provider.imap.port));
                rgEncryption.check(provider.imap.starttls ? R.id.radio_starttls : R.id.radio_ssl);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException || ex instanceof UnknownHostException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "account:config");
    }

    private void onCheck() {
        int encryption;
        if (rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls)
            encryption = EmailService.ENCRYPTION_STARTTLS;
        else if (rgEncryption.getCheckedRadioButtonId() == R.id.radio_none)
            encryption = EmailService.ENCRYPTION_NONE;
        else
            encryption = EmailService.ENCRYPTION_SSL;

        Bundle args = new Bundle();
        args.putLong("id", id);
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

        new SimpleTask<CheckResult>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbCheck.setVisibility(View.VISIBLE);
                tvIdle.setVisibility(View.GONE);
                tvUtf8.setVisibility(View.GONE);
                grpFolders.setVisibility(View.GONE);
                grpError.setVisibility(View.GONE);
                btnHelp.setVisibility(View.GONE);
                btnSupport.setVisibility(View.GONE);
                tvInstructions.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                saving = false;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, true);
                pbCheck.setVisibility(View.GONE);
            }

            @Override
            protected CheckResult onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                String host = args.getString("host");
                int encryption = args.getInt("encryption");
                boolean insecure = args.getBoolean("insecure");
                String port = args.getString("port");
                int auth = args.getInt("auth");
                String provider = args.getString("provider");
                String user = args.getString("user");
                String password = args.getString("password");
                String certificate = args.getString("certificate");
                String realm = args.getString("realm");
                String fingerprint = args.getString("fingerprint");

                if (host.contains(":")) {
                    Uri h = Uri.parse(host);
                    host = h.getHost();
                }

                if (TextUtils.isEmpty(host))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (encryption == EmailService.ENCRYPTION_SSL ? "993" : "143");
                if (TextUtils.isEmpty(user))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (TextUtils.isEmpty(password) && !insecure && certificate == null)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));

                if (TextUtils.isEmpty(realm))
                    realm = null;

                DB db = DB.getInstance(context);

                CheckResult result = new CheckResult();
                result.account = db.account().getAccount(id);
                result.folders = new ArrayList<>();

                // Check IMAP server / get folders
                String protocol = "imap" + (encryption == EmailService.ENCRYPTION_SSL ? "s" : "");
                try (EmailService iservice = new EmailService(
                        context, protocol, realm, encryption, insecure,
                        EmailService.PURPOSE_CHECK, true)) {
                    iservice.connect(
                            host, Integer.parseInt(port),
                            auth, provider,
                            user, password,
                            certificate, fingerprint);

                    result.idle = iservice.hasCapability("IDLE");

                    boolean inbox = false;
                    for (Folder ifolder : iservice.getStore().getDefaultFolder().list("*")) {
                        // Check folder attributes
                        String fullName = ifolder.getFullName();
                        String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                        Log.i(fullName + " attrs=" + TextUtils.join(" ", attrs));
                        String type = EntityFolder.getType(attrs, fullName, true);

                        boolean selectable = true;
                        for (String attr : attrs)
                            if (attr.equalsIgnoreCase("\\NoSelect"))
                                selectable = false;
                        selectable = selectable && ((ifolder.getType() & IMAPFolder.HOLDS_MESSAGES) != 0);

                        if (type != null && selectable) {
                            // Create entry
                            EntityFolder folder = db.folder().getFolderByName(id, fullName);
                            if (folder == null)
                                folder = new EntityFolder(fullName, type);
                            result.folders.add(folder);

                            if (EntityFolder.INBOX.equals(type)) {
                                inbox = true;

                                result.utf8 = (Boolean) ((IMAPFolder) ifolder).doCommand(new IMAPFolder.ProtocolCommand() {
                                    @Override
                                    public Object doCommand(IMAPProtocol protocol) {
                                        return protocol.supportsUtf8();
                                    }
                                });
                            }

                            Log.i(folder.name + " id=" + folder.id +
                                    " type=" + folder.type + " attr=" + TextUtils.join(",", attrs));
                        }
                    }

                    EntityFolder.guessTypes(result.folders, iservice.getStore().getDefaultFolder().getSeparator());

                    if (!inbox)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_inbox));

                    if (result.folders.size() > 0)
                        Collections.sort(result.folders, result.folders.get(0).getComparator(null));
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, CheckResult result) {
                tvIdle.setVisibility(result.idle ? View.GONE : View.VISIBLE);
                tvUtf8.setVisibility(result.utf8 == null || result.utf8 ? View.GONE : View.VISIBLE);
                if (!result.idle)
                    etInterval.setText(Integer.toString(EntityAccount.DEFAULT_POLL_INTERVAL));

                setFolders(result.folders, result.account);

                if (!cbTrust.isChecked())
                    cbTrust.setVisibility(View.GONE);

                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.smoothScrollTo(0, cbIdentity.getBottom());
                    }
                });
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                grpFolders.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
                cbIdentity.setVisibility(View.GONE);

                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    showError(ex);
            }
        }.execute(this, args, "account:check");
    }

    private void onSave(boolean should) {
        EntityFolder drafts = (EntityFolder) spDrafts.getSelectedItem();
        EntityFolder sent = (EntityFolder) spSent.getSelectedItem();
        EntityFolder archive = (EntityFolder) spArchive.getSelectedItem();
        EntityFolder trash = (EntityFolder) spTrash.getSelectedItem();
        EntityFolder junk = (EntityFolder) spJunk.getSelectedItem();
        EntityFolder left = (EntityFolder) spLeft.getSelectedItem();
        EntityFolder right = (EntityFolder) spRight.getSelectedItem();
        EntityFolder move = (EntityFolder) spMove.getSelectedItem();

        if (drafts != null && drafts.id != null && drafts.id == 0L)
            drafts = null;
        if (sent != null && sent.id != null && sent.id == 0L)
            sent = null;
        if (archive != null && archive.id != null && archive.id == 0L)
            archive = null;
        if (trash != null && trash.id != null && trash.id == 0L)
            trash = null;
        if (junk != null && junk.id != null && junk.id == 0L)
            junk = null;

        if (left != null && left.id != null && left.id == 0L)
            left = null;
        if (right != null && right.id != null && right.id == 0L)
            right = null;

        if (move != null && move.id != null && move.id == 0L)
            move = null;

        Bundle args = new Bundle();
        args.putLong("id", id);

        int encryption;
        if (rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls)
            encryption = EmailService.ENCRYPTION_STARTTLS;
        else if (rgEncryption.getCheckedRadioButtonId() == R.id.radio_none)
            encryption = EmailService.ENCRYPTION_NONE;
        else
            encryption = EmailService.ENCRYPTION_SSL;

        args.putString("host", etHost.getText().toString().trim());
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

        args.putString("name", etName.getText().toString());
        args.putInt("color", btnColor.getColor());

        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("ondemand", cbOnDemand.isChecked());
        args.putBoolean("primary", cbPrimary.isChecked());
        args.putBoolean("notify", cbNotify.isChecked());
        args.putBoolean("browse", cbBrowse.isChecked());
        args.putBoolean("auto_seen", cbAutoSeen.isChecked());
        args.putString("interval", etInterval.getText().toString());
        args.putBoolean("partial_fetch", cbPartialFetch.isChecked());
        args.putBoolean("ignore_size", cbIgnoreSize.isChecked());
        args.putBoolean("use_date", rgDate.getCheckedRadioButtonId() == R.id.radio_date_header);
        args.putBoolean("use_received", rgDate.getCheckedRadioButtonId() == R.id.radio_received_header);

        args.putSerializable("drafts", drafts);
        args.putSerializable("sent", sent);
        args.putSerializable("archive", archive);
        args.putSerializable("trash", trash);
        args.putSerializable("junk", junk);
        args.putSerializable("left", left);
        args.putSerializable("right", right);
        args.putSerializable("move", move);

        args.putBoolean("should", should);

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
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
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, true);
                pbSave.setVisibility(View.GONE);
            }

            @Override
            protected Boolean onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

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

                String name = args.getString("name");
                Integer color = args.getInt("color");

                boolean synchronize = args.getBoolean("synchronize");
                boolean ondemand = args.getBoolean("ondemand");
                boolean primary = args.getBoolean("primary");
                boolean notify = args.getBoolean("notify");
                boolean browse = args.getBoolean("browse");
                boolean auto_seen = args.getBoolean("auto_seen");
                String interval = args.getString("interval");
                boolean partial_fetch = args.getBoolean("partial_fetch");
                boolean ignore_size = args.getBoolean("ignore_size");
                boolean use_date = args.getBoolean("use_date");
                boolean use_received = args.getBoolean("use_received");

                EntityFolder drafts = (EntityFolder) args.getSerializable("drafts");
                EntityFolder sent = (EntityFolder) args.getSerializable("sent");
                EntityFolder archive = (EntityFolder) args.getSerializable("archive");
                EntityFolder trash = (EntityFolder) args.getSerializable("trash");
                EntityFolder junk = (EntityFolder) args.getSerializable("junk");
                EntityFolder left = (EntityFolder) args.getSerializable("left");
                EntityFolder right = (EntityFolder) args.getSerializable("right");
                EntityFolder move = (EntityFolder) args.getSerializable("move");

                boolean pro = ActivityBilling.isPro(context);
                boolean should = args.getBoolean("should");

                if (host.contains(":")) {
                    Uri h = Uri.parse(host);
                    host = h.getHost();
                }

                if (TextUtils.isEmpty(host) && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (encryption == EmailService.ENCRYPTION_SSL ? "993" : "143");
                if (TextUtils.isEmpty(user) && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (synchronize && TextUtils.isEmpty(password) && !insecure && certificate == null && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));
                int poll_interval = (TextUtils.isEmpty(interval)
                        ? EntityAccount.DEFAULT_KEEP_ALIVE_INTERVAL : Integer.parseInt(interval));

                if (TextUtils.isEmpty(realm))
                    realm = null;
                if (TextUtils.isEmpty(name))
                    name = user;
                if (color == Color.TRANSPARENT || !pro)
                    color = null;
                if (!pro)
                    notify = false;

                long now = new Date().getTime();

                DB db = DB.getInstance(context);
                EntityAccount account = db.account().getAccount(id);

                if (should) {
                    if (account == null)
                        return !TextUtils.isEmpty(host) && !TextUtils.isEmpty(user);

                    if (!Objects.equals(account.host, host))
                        return true;
                    if (!Objects.equals(account.encryption, encryption))
                        return true;
                    if (!Objects.equals(account.insecure, insecure))
                        return true;
                    if (!Objects.equals(account.port, Integer.parseInt(port)))
                        return true;
                    if (account.auth_type != auth)
                        return true;
                    if (!Objects.equals(account.user, user))
                        return true;
                    if (!Objects.equals(account.password, password))
                        return true;
                    if (!Objects.equals(account.certificate_alias, certificate))
                        return true;
                    if (!Objects.equals(account.realm, realm))
                        return true;
                    if (!Objects.equals(account.fingerprint, fingerprint))
                        return true;
                    if (!Objects.equals(account.name, name))
                        return true;
                    if (!Objects.equals(account.color, color))
                        return true;
                    if (!Objects.equals(account.synchronize, synchronize))
                        return true;
                    if (!Objects.equals(account.ondemand, ondemand))
                        return true;
                    if (!Objects.equals(account.primary, account.synchronize && primary))
                        return true;
                    if (!Objects.equals(account.notify, notify))
                        return true;
                    if (!Objects.equals(account.browse, browse))
                        return true;
                    if (!Objects.equals(account.auto_seen, auto_seen))
                        return true;
                    if (!Objects.equals(account.poll_interval, poll_interval))
                        return true;
                    if (!Objects.equals(account.partial_fetch, partial_fetch))
                        return true;
                    if (!Objects.equals(account.ignore_size, ignore_size))
                        return true;
                    if (!Objects.equals(account.use_date, use_date))
                        return true;
                    if (!Objects.equals(account.use_received, use_received))
                        return true;

                    EntityFolder edrafts = db.folder().getFolderByType(account.id, EntityFolder.DRAFTS);
                    if (!Objects.equals(edrafts == null ? null : edrafts.id, drafts == null ? null : drafts.id))
                        return true;

                    EntityFolder esent = db.folder().getFolderByType(account.id, EntityFolder.SENT);
                    if (!Objects.equals(esent == null ? null : esent.id, sent == null ? null : sent.id))
                        return true;

                    EntityFolder earchive = db.folder().getFolderByType(account.id, EntityFolder.ARCHIVE);
                    if (!Objects.equals(earchive == null ? null : earchive.id, archive == null ? null : archive.id))
                        return true;

                    EntityFolder etrash = db.folder().getFolderByType(account.id, EntityFolder.TRASH);
                    if (!Objects.equals(etrash == null ? null : etrash.id, trash == null ? null : trash.id))
                        return true;

                    EntityFolder ejunk = db.folder().getFolderByType(account.id, EntityFolder.JUNK);
                    if (!Objects.equals(ejunk == null ? null : ejunk.id, junk == null ? null : junk.id))
                        return true;

                    if (!Objects.equals(account.swipe_left, left == null ? null : left.id))
                        return true;
                    if (!Objects.equals(account.swipe_right, right == null ? null : right.id))
                        return true;

                    if (!Objects.equals(account.move_to, move == null ? null : move.id))
                        return true;

                    return false;
                }

                String accountRealm = (account == null ? null : account.realm);

                boolean check = (synchronize && (account == null ||
                        !account.synchronize ||
                        account.error != null ||
                        !account.host.equals(host) ||
                        !account.encryption.equals(encryption) ||
                        !account.insecure.equals(insecure) ||
                        !account.port.equals(Integer.parseInt(port)) ||
                        !account.user.equals(user) ||
                        !account.password.equals(password) ||
                        !Objects.equals(account.certificate_alias, certificate) ||
                        !Objects.equals(realm, accountRealm) ||
                        !Objects.equals(account.fingerprint, fingerprint) ||
                        BuildConfig.DEBUG));
                Log.i("Account check=" + check);

                Long last_connected = null;
                if (account != null && synchronize == account.synchronize)
                    last_connected = account.last_connected;

                // Check IMAP server
                EntityFolder inbox = null;
                if (check) {
                    String protocol = "imap" + (encryption == EmailService.ENCRYPTION_SSL ? "s" : "");
                    try (EmailService iservice = new EmailService(
                            context, protocol, realm, encryption, insecure,
                            EmailService.PURPOSE_CHECK, true)) {
                        iservice.connect(
                                host, Integer.parseInt(port),
                                auth, provider,
                                user, password,
                                certificate, fingerprint);

                        for (Folder ifolder : iservice.getStore().getDefaultFolder().list("*")) {
                            // Check folder attributes
                            String fullName = ifolder.getFullName();
                            String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                            Log.i(fullName + " attrs=" + TextUtils.join(" ", attrs));
                            String type = EntityFolder.getType(attrs, fullName, true);

                            if (EntityFolder.INBOX.equals(type)) {
                                inbox = new EntityFolder();
                                inbox.name = fullName;
                                inbox.type = type;
                                inbox.synchronize = true;
                                inbox.unified = true;
                                inbox.notify = true;
                                inbox.sync_days = EntityFolder.DEFAULT_SYNC;
                                inbox.keep_days = EntityFolder.DEFAULT_KEEP;
                            }
                        }
                    }
                }

                try {
                    db.beginTransaction();

                    if (account != null && !account.password.equals(password)) {
                        String domain = DnsHelper.getParentDomain(account.host);
                        String match = (Objects.equals(account.host, domain) ? account.host : "%." + domain);
                        int count = db.identity().setIdentityPassword(account.id, account.user, password, match);
                        Log.i("Updated passwords=" + count + " match=" + match);
                    }

                    boolean update = (account != null);
                    if (account == null)
                        account = new EntityAccount();

                    account.host = host;
                    account.encryption = encryption;
                    account.insecure = insecure;
                    account.port = Integer.parseInt(port);
                    account.auth_type = auth;
                    account.user = user;
                    account.password = password;
                    account.certificate_alias = certificate;
                    account.provider = provider;
                    account.realm = realm;
                    account.fingerprint = fingerprint;

                    account.name = name;
                    account.color = color;

                    account.synchronize = synchronize;
                    account.ondemand = ondemand;
                    account.primary = (account.synchronize && primary);
                    account.notify = notify;
                    account.browse = browse;
                    account.auto_seen = auto_seen;

                    if (account.poll_interval != poll_interval) {
                        account.keep_alive_ok = false;
                        account.keep_alive_failed = 0;
                        account.keep_alive_succeeded = 0;
                    }
                    account.poll_interval = Math.max(1, poll_interval);

                    account.partial_fetch = partial_fetch;
                    account.ignore_size = ignore_size;
                    account.use_date = use_date;
                    account.use_received = use_received;

                    if (!update)
                        account.created = now;

                    account.warning = null;
                    account.error = null;
                    account.last_connected = last_connected;

                    if (account.primary)
                        db.account().resetPrimary();

                    if (update)
                        db.account().updateAccount(account);
                    else
                        account.id = db.account().insertAccount(account);

                    args.putLong("account", account.id);
                    EntityLog.log(context, (update ? "Updated" : "Added") + " account=" + account.name);

                    // Make sure the channel exists on commit
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (account.notify) {
                            // Add or update notification channel
                            account.deleteNotificationChannel(context);
                            account.createNotificationChannel(context);
                        } else if (!account.synchronize)
                            account.deleteNotificationChannel(context);
                    }

                    List<EntityFolder> folders = new ArrayList<>();

                    if (inbox != null)
                        folders.add(inbox);

                    if (drafts != null) {
                        drafts.type = EntityFolder.DRAFTS;
                        drafts.poll = EntityFolder.shouldPoll(drafts.type);
                        folders.add(drafts);
                    }

                    if (sent != null) {
                        sent.type = EntityFolder.SENT;
                        sent.poll = EntityFolder.shouldPoll(sent.type);
                        folders.add(sent);
                    }
                    if (archive != null) {
                        archive.type = EntityFolder.ARCHIVE;
                        archive.poll = EntityFolder.shouldPoll(archive.type);
                        folders.add(archive);
                    }
                    if (trash != null) {
                        trash.type = EntityFolder.TRASH;
                        trash.poll = EntityFolder.shouldPoll(trash.type);
                        folders.add(trash);
                    }
                    if (junk != null) {
                        junk.type = EntityFolder.JUNK;
                        junk.poll = EntityFolder.shouldPoll(junk.type);
                        folders.add(junk);
                    }

                    if (left != null && !(left.id != null && left.id < 0)) {
                        boolean found = false;
                        for (EntityFolder folder : folders)
                            if (left.name.equals(folder.name)) {
                                found = true;
                                break;
                            }
                        if (!found) {
                            left.type = EntityFolder.USER;
                            folders.add(left);
                        }
                    }

                    if (right != null && !(right.id != null && right.id < 0)) {
                        boolean found = false;
                        for (EntityFolder folder : folders)
                            if (right.name.equals(folder.name)) {
                                found = true;
                                break;
                            }
                        if (!found) {
                            right.type = EntityFolder.USER;
                            folders.add(right);
                        }
                    }

                    if (move != null && !(move.id != null && move.id < 0)) {
                        boolean found = false;
                        for (EntityFolder folder : folders)
                            if (move.name.equals(folder.name)) {
                                found = true;
                                break;
                            }
                        if (!found) {
                            move.type = EntityFolder.USER;
                            folders.add(move);
                        }
                    }

                    db.folder().setFoldersUser(account.id);

                    for (EntityFolder folder : folders) {
                        EntityFolder existing = db.folder().getFolderByName(account.id, folder.name);
                        if (existing == null) {
                            folder.account = account.id;
                            EntityLog.log(context, "Added folder=" + folder.name + " type=" + folder.type);
                            folder.id = db.folder().insertFolder(folder);
                            if (folder.synchronize)
                                EntityOperation.sync(context, folder.id, false);
                        } else {
                            EntityLog.log(context, "Updated folder=" + folder.name + " type=" + folder.type);
                            db.folder().setFolderType(existing.id, folder.type);
                        }
                    }

                    account.swipe_left = (left == null ? null : left.id);
                    account.swipe_right = (right == null ? null : right.id);
                    account.move_to = (move == null ? null : move.id);
                    db.account().updateAccount(account);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "save account");

                if (!synchronize) {
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel("receive:" + account.id, 1);
                    nm.cancel("alert:" + account.id, 1);
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
                    fragment.setTargetFragment(FragmentAccount.this, REQUEST_SAVE);
                    fragment.show(getParentFragmentManager(), "account:save");
                } else {
                    Context context = getContext();
                    if (context != null)
                        WidgetUnified.updateData(context); // Update color stripe

                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        getParentFragmentManager().popBackStack();

                        if (cbIdentity.isChecked()) {
                            Bundle aargs = new Bundle();
                            aargs.putLong("account", args.getLong("account"));

                            FragmentIdentity fragment = new FragmentIdentity();
                            fragment.setArguments(aargs);
                            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("identity");
                            fragmentTransaction.commit();
                        }
                    }
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    showError(ex);
            }
        }.execute(this, args, "account:save");
    }

    private void showError(Throwable ex) {
        tvError.setText(Log.formatThrowable(ex, false));
        grpError.setVisibility(View.VISIBLE);

        if (ex instanceof EmailService.UntrustedException) {
            EmailService.UntrustedException e = (EmailService.UntrustedException) ex;
            cbTrust.setTag(e.getFingerprint());
            cbTrust.setText(getString(R.string.title_trust, e.getFingerprint()));
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
            tvInstructions.setText(HtmlHelper.fromHtml(provider.documentation.toString(), true, getContext()));
            tvInstructions.setVisibility(View.VISIBLE);
        }

        getMainHandler().post(new Runnable() {
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
        outState.putInt("fair:provider", spProvider.getSelectedItemPosition());
        outState.putString("fair:certificate", certificate);
        outState.putString("fair:password", tilPassword.getEditText().getText().toString());
        outState.putInt("fair:advanced", grpAdvanced.getVisibility());
        outState.putInt("fair:auth", auth);
        outState.putString("fair:authprovider", provider);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", copy < 0 ? id : copy);

        new SimpleTask<EntityAccount>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                // Consider previous check/save/delete as cancelled
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected EntityAccount onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                return db.account().getAccount(id);
            }

            @Override
            protected void onExecuted(Bundle args, final EntityAccount account) {
                // Get providers
                List<EmailProvider> providers = EmailProvider.loadProfiles(getContext());
                providers.add(0, new EmailProvider(getString(R.string.title_select)));
                providers.add(1, new EmailProvider(getString(R.string.title_custom)));

                ArrayAdapter<EmailProvider> aaProvider =
                        new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, providers);
                aaProvider.setDropDownViewResource(R.layout.spinner_item1_dropdown);
                spProvider.setAdapter(aaProvider);

                if (savedInstanceState == null) {
                    if (account != null) {
                        boolean found = false;
                        for (int pos = 2; pos < providers.size(); pos++) {
                            EmailProvider provider = providers.get(pos);
                            if ((provider.oauth != null) == (account.auth_type == AUTH_TYPE_OAUTH) &&
                                    provider.imap.host.equals(account.host) &&
                                    provider.imap.port == account.port &&
                                    provider.imap.starttls == (account.encryption == EmailService.ENCRYPTION_STARTTLS)) {
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

                    if (account != null && account.encryption == EmailService.ENCRYPTION_STARTTLS)
                        rgEncryption.check(R.id.radio_starttls);
                    else if (account != null && account.encryption == EmailService.ENCRYPTION_NONE)
                        rgEncryption.check(R.id.radio_none);
                    else
                        rgEncryption.check(R.id.radio_ssl);

                    cbInsecure.setChecked(account == null ? false : account.insecure);

                    etUser.setText(account == null ? null : account.user);
                    tilPassword.getEditText().setText(account == null ? null : account.password);
                    certificate = (account == null ? null : account.certificate_alias);
                    tvCertificate.setText(certificate == null ? getString(R.string.title_optional) : certificate);
                    etRealm.setText(account == null ? null : account.realm);

                    if (account == null || account.fingerprint == null) {
                        cbTrust.setTag(null);
                        cbTrust.setChecked(false);
                    } else {
                        cbTrust.setTag(account.fingerprint);
                        cbTrust.setChecked(true);
                        cbTrust.setText(getString(R.string.title_trust, account.fingerprint));
                    }

                    etName.setText(account == null ? null : account.name);
                    btnColor.setColor(account == null ? null : account.color);

                    boolean pro = ActivityBilling.isPro(getContext());
                    cbNotify.setChecked(account != null && account.notify && pro);
                    cbNotify.setEnabled(pro);

                    cbSynchronize.setChecked(account == null ? true : account.synchronize);
                    cbOnDemand.setChecked(account == null ? false : account.ondemand);
                    cbPrimary.setChecked(account == null ? false : account.primary);
                    cbBrowse.setChecked(account == null ? true : account.browse);
                    cbAutoSeen.setChecked(account == null ? true : account.auto_seen);
                    etInterval.setText(account == null ? "" : Long.toString(account.poll_interval));
                    cbPartialFetch.setChecked(account == null ? true : account.partial_fetch);
                    cbIgnoreSize.setChecked(account == null ? false : account.ignore_size);

                    if (account != null && account.use_date)
                        rgDate.check(R.id.radio_date_header);
                    else if (account != null && account.use_received)
                        rgDate.check(R.id.radio_received_header);
                    else
                        rgDate.check(R.id.radio_server_time);

                    auth = (account == null ? AUTH_TYPE_PASSWORD : account.auth_type);
                    provider = (account == null ? null : account.provider);

                    new SimpleTask<EntityAccount>() {
                        @Override
                        protected EntityAccount onExecute(Context context, Bundle args) {
                            return DB.getInstance(context).account().getPrimaryAccount();
                        }

                        @Override
                        protected void onExecuted(Bundle args, EntityAccount primary) {
                            if (primary == null)
                                cbPrimary.setChecked(true);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentAccount.this, new Bundle(), "account:primary");
                } else {
                    int p = savedInstanceState.getInt("fair:provider");
                    spProvider.setTag(p);
                    spProvider.setSelection(p);

                    certificate = savedInstanceState.getString("fair:certificate");
                    tvCertificate.setText(certificate == null ? getString(R.string.title_optional) : certificate);

                    tilPassword.getEditText().setText(savedInstanceState.getString("fair:password"));
                    grpAdvanced.setVisibility(savedInstanceState.getInt("fair:advanced"));
                    auth = savedInstanceState.getInt("fair:auth");
                    provider = savedInstanceState.getString("fair:authprovider");
                }

                Helper.setViewsEnabled(view, true);

                if (auth != AUTH_TYPE_PASSWORD) {
                    etUser.setEnabled(false);
                    tilPassword.setEnabled(false);
                    btnCertificate.setEnabled(false);
                }
                ibAccount.setEnabled(auth == AUTH_TYPE_GMAIL);

                cbOnDemand.setEnabled(cbSynchronize.isChecked());
                cbPrimary.setEnabled(cbSynchronize.isChecked());

                if (copy < 0 && account != null) {
                    args.putLong("account", account.id);

                    final SimpleTask task = new SimpleTask<List<EntityFolder>>() {
                        @Override
                        protected List<EntityFolder> onExecute(Context context, Bundle args) {
                            long account = args.getLong("account");

                            DB db = DB.getInstance(context);
                            List<EntityFolder> folders = db.folder().getFolders(account, false, true);

                            if (folders != null && folders.size() > 0)
                                Collections.sort(folders, folders.get(0).getComparator(null));

                            return folders;
                        }

                        @Override
                        protected void onExecuted(Bundle args, List<EntityFolder> folders) {
                            if (folders == null)
                                folders = new ArrayList<>();
                            setFolders(folders, account);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    };

                    // Load after provider has been selected
                    getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            task.execute(FragmentAccount.this, args, "account:folders");
                        }
                    });
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "account:get");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account, menu);
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
        aargs.putString("question", getString(R.string.title_account_delete));
        aargs.putBoolean("warning", true);

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(aargs);
        fragment.setTargetFragment(FragmentAccount.this, REQUEST_DELETE);
        fragment.show(getParentFragmentManager(), "account:delete");
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
                        final boolean save = (btnSave.getVisibility() == View.VISIBLE);
                        getMainHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                scroll.smoothScrollTo(0, (save ? btnSave : btnCheck).getBottom());
                            }
                        });
                        if (save)
                            onSave(false);
                        else
                            onCheck();
                    } else if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        getParentFragmentManager().popBackStack();
                    break;
                case REQUEST_DELETE:
                    if (resultCode == RESULT_OK)
                        onDelete();
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
                db.account().setAccountTbd(id);

                ServiceSynchronize.eval(context, "delete account");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getParentFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "account:delete");
    }

    private void setFolders(List<EntityFolder> _folders, EntityAccount account) {
        {
            List<EntityFolder> folders = new ArrayList<>();

            EntityFolder none = new EntityFolder();
            none.id = 0L;
            none.name = "-";
            folders.add(none);

            for (EntityFolder folder : _folders)
                if (!EntityFolder.INBOX.equals(folder.type))
                    folders.add(folder);

            adapter.clear();
            adapter.addAll(folders);

            for (int pos = 0; pos < folders.size(); pos++) {
                EntityFolder folder = folders.get(pos);

                if (EntityFolder.DRAFTS.equals(folder.type))
                    spDrafts.setSelection(pos);
                else if (EntityFolder.SENT.equals(folder.type))
                    spSent.setSelection(pos);
                else if (EntityFolder.ARCHIVE.equals(folder.type))
                    spArchive.setSelection(pos);
                else if (EntityFolder.TRASH.equals(folder.type))
                    spTrash.setSelection(pos);
                else if (EntityFolder.JUNK.equals(folder.type))
                    spJunk.setSelection(pos);

                if (account != null &&
                        account.move_to != null && account.move_to.equals(folder.id))
                    spMove.setSelection(pos);
            }
        }

        {
            List<EntityFolder> folders = getFolderActions(getContext());
            folders.addAll(_folders);

            adapterSwipe.clear();
            adapterSwipe.addAll(folders);

            Long left = (account == null ? null : account.swipe_left);
            Long right = (account == null ? null : account.swipe_right);

            String leftDefault = EntityFolder.TRASH;
            String rightDefault = EntityFolder.TRASH;
            for (EntityFolder folder : folders)
                if (EntityFolder.ARCHIVE.equals(folder.type)) {
                    rightDefault = folder.type;
                    break;
                }

            for (int pos = 0; pos < folders.size(); pos++) {
                EntityFolder folder = folders.get(pos);

                if (left == null ? (account == null && leftDefault.equals(folder.type)) : left.equals(folder.id))
                    spLeft.setSelection(pos);

                if (right == null ? (account == null && rightDefault.equals(folder.type)) : right.equals(folder.id))
                    spRight.setSelection(pos);
            }
        }

        cbIdentity.setChecked(account == null);

        grpFolders.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
        cbIdentity.setVisibility(View.VISIBLE);

        if (cbTrust.isChecked())
            cbTrust.setVisibility(View.VISIBLE);
    }

    static List<EntityFolder> getFolderActions(Context context) {
        List<EntityFolder> folders = new ArrayList<>();

        EntityFolder none = new EntityFolder();
        none.id = 0L;
        none.name = "-";
        folders.add(none);

        EntityFolder ask = new EntityFolder();
        ask.id = EntityMessage.SWIPE_ACTION_ASK;
        ask.name = context.getString(R.string.title_ask_what);
        folders.add(ask);

        EntityFolder seen = new EntityFolder();
        seen.id = EntityMessage.SWIPE_ACTION_SEEN;
        seen.name = context.getString(R.string.title_seen);
        folders.add(seen);

        EntityFolder flag = new EntityFolder();
        flag.id = EntityMessage.SWIPE_ACTION_FLAG;
        flag.name = context.getString(R.string.title_flag);
        folders.add(flag);

        EntityFolder snooze = new EntityFolder();
        snooze.id = EntityMessage.SWIPE_ACTION_SNOOZE;
        snooze.name = context.getString(R.string.title_snooze_now);
        folders.add(snooze);

        EntityFolder hide = new EntityFolder();
        hide.id = EntityMessage.SWIPE_ACTION_HIDE;
        hide.name = context.getString(R.string.title_hide);
        folders.add(hide);

        EntityFolder move = new EntityFolder();
        move.id = EntityMessage.SWIPE_ACTION_MOVE;
        move.name = context.getString(R.string.title_move);
        folders.add(move);

        EntityFolder junk = new EntityFolder();
        junk.id = EntityMessage.SWIPE_ACTION_JUNK;
        junk.name = context.getString(R.string.title_report_spam);
        folders.add(junk);

        EntityFolder delete = new EntityFolder();
        delete.id = EntityMessage.SWIPE_ACTION_DELETE;
        delete.name = context.getString(R.string.title_delete_permanently);
        folders.add(delete);

        return folders;
    }

    private static class CheckResult {
        EntityAccount account;
        List<EntityFolder> folders;
        boolean idle;
        Boolean utf8;
    }
}
