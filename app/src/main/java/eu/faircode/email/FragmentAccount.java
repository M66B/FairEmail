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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_OK;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_GMAIL;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_OAUTH;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.core.text.method.LinkMovementMethodCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FragmentAccount extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private Spinner spProvider;
    private TextView tvGmailHint;

    private EditText etDomain;
    private Button btnAutoConfig;
    private ContentLoadingProgressBar pbAutoConfig;

    private CheckBox cbDnsSec;
    private EditText etHost;
    private RadioGroup rgEncryption;
    private CheckBox cbInsecure;
    private TextView tvInsecureRemark;
    private CheckBox cbDane;

    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private TextView tvAppPassword;
    private TextView tvPasswordStorage;
    private Button btnCertificate;
    private TextView tvCertificate;
    private EditText etRealm;

    private EditText etName;
    private ArrayAdapter<String> adapterCategory;
    private AutoCompleteTextView etCategory;
    private ViewButtonColor btnColor;
    private TextView tvColorPro;

    private Button btnAvatar;
    private TextView tvAvatarPro;

    private Button btnCalendar;
    private TextView tvCalendarPro;

    private Button btnAdvanced;
    private CheckBox cbSynchronize;
    private CheckBox cbIgnoreSchedule;
    private CheckBox cbOnDemand;
    private TextView tvLeave;
    private CheckBox cbPrimary;
    private CheckBox cbNotify;
    private TextView tvNotifyRemark;
    private CheckBox cbSummary;
    private TextView tvNotifyPro;
    private CheckBox cbBrowse;
    private CheckBox cbAutoSeen;
    private EditText etInterval;
    private CheckBox cbNoop;
    private CheckBox cbPartialFetch;
    private CheckBox cbRawFetch;
    private CheckBox cbIgnoreSize;
    private RadioGroup rgDate;
    private CheckBox cbUnicode;
    private CheckBox cbUnmetered;
    private CheckBox cbVpnOnly;

    private Button btnCheck;
    private ContentLoadingProgressBar pbCheck;
    private TextView tvIdle;

    private ArrayAdapter<EntityFolder> adapter;
    private Spinner spDrafts;
    private Spinner spSent;
    private TextView tvSentWarning;
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
    private Group grpCalendar;
    private Group grpAdvanced;
    private Group grpFolders;
    private Group grpError;

    private long id = -1;
    private long copy = -1;
    private int auth = AUTH_TYPE_PASSWORD;
    private String provider = null;
    private String avatar = null;
    private String calendar = null;
    private String certificate = null;
    private boolean saving = false;

    private static final int REQUEST_COLOR = 1;
    private static final int REQUEST_AVATAR = 2;
    private static final int REQUEST_CALENDAR = 3;
    private static final int REQUEST_SAVE = 4;
    private static final int REQUEST_DELETE = 5;

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

        cbDnsSec = view.findViewById(R.id.cbDnsSec);
        etHost = view.findViewById(R.id.etHost);
        etPort = view.findViewById(R.id.etPort);
        rgEncryption = view.findViewById(R.id.rgEncryption);
        cbInsecure = view.findViewById(R.id.cbInsecure);
        tvInsecureRemark = view.findViewById(R.id.tvInsecureRemark);
        cbDane = view.findViewById(R.id.cbDane);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvAppPassword = view.findViewById(R.id.tvAppPassword);
        tvPasswordStorage = view.findViewById(R.id.tvPasswordStorage);
        btnCertificate = view.findViewById(R.id.btnCertificate);
        tvCertificate = view.findViewById(R.id.tvCertificate);
        etRealm = view.findViewById(R.id.etRealm);

        etName = view.findViewById(R.id.etName);
        etCategory = view.findViewById(R.id.etCategory);
        btnColor = view.findViewById(R.id.btnColor);
        tvColorPro = view.findViewById(R.id.tvColorPro);

        btnAvatar = view.findViewById(R.id.btnAvatar);
        tvAvatarPro = view.findViewById(R.id.tvAvatarPro);

        btnCalendar = view.findViewById(R.id.btnCalendar);
        tvCalendarPro = view.findViewById(R.id.tvCalendarPro);

        btnAdvanced = view.findViewById(R.id.btnAdvanced);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbIgnoreSchedule = view.findViewById(R.id.cbIgnoreSchedule);
        cbOnDemand = view.findViewById(R.id.cbOnDemand);
        tvLeave = view.findViewById(R.id.tvLeave);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbNotify = view.findViewById(R.id.cbNotify);
        tvNotifyRemark = view.findViewById(R.id.tvNotifyRemark);
        cbSummary = view.findViewById(R.id.cbSummary);
        tvNotifyPro = view.findViewById(R.id.tvNotifyPro);
        cbBrowse = view.findViewById(R.id.cbBrowse);
        cbAutoSeen = view.findViewById(R.id.cbAutoSeen);
        etInterval = view.findViewById(R.id.etInterval);
        cbNoop = view.findViewById(R.id.cbNoop);
        cbPartialFetch = view.findViewById(R.id.cbPartialFetch);
        cbRawFetch = view.findViewById(R.id.cbRawFetch);
        cbIgnoreSize = view.findViewById(R.id.cbIgnoreSize);
        rgDate = view.findViewById(R.id.rgDate);
        cbUnicode = view.findViewById(R.id.cbUnicode);
        cbUnmetered = view.findViewById(R.id.cbUnmeteredOnly);
        cbVpnOnly = view.findViewById(R.id.cbVpnOnly);

        btnCheck = view.findViewById(R.id.btnCheck);
        pbCheck = view.findViewById(R.id.pbCheck);

        tvIdle = view.findViewById(R.id.tvIdle);

        spDrafts = view.findViewById(R.id.spDrafts);
        spSent = view.findViewById(R.id.spSent);
        tvSentWarning = view.findViewById(R.id.tvSentWarning);
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
        grpCalendar = view.findViewById(R.id.grpCalendar);
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
                grpCalendar.setVisibility(position > 0 && !BuildConfig.PLAY_STORE_RELEASE ? View.VISIBLE : View.GONE);

                btnAdvanced.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                if (position == 0)
                    grpAdvanced.setVisibility(View.GONE);

                btnCheck.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                tvIdle.setVisibility(View.GONE);

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
                tvAppPassword.setVisibility(EntityAccount.isOutlook(provider.id)
                        ? View.VISIBLE : View.GONE);
                certificate = null;
                tvCertificate.setText(R.string.title_optional);
                etRealm.setText(null);
                cbTrust.setChecked(false);

                etName.setText(position > 1 ? provider.name : null);
                etInterval.setText(provider.keepalive > 0 ? Integer.toString(provider.keepalive) : null);
                cbNoop.setChecked(provider.noop);
                cbPartialFetch.setChecked(provider.partial);
                cbRawFetch.setChecked(provider.raw);

                tvSentWarning.setVisibility(View.GONE);
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

        cbInsecure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                cbDane.setEnabled(!checked);
            }
        });

        tvInsecureRemark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 4);
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
                //   tilPassword.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (tilPassword == null)
                    return;

                String password = s.toString();
                boolean warning = (Helper.containsWhiteSpace(password) ||
                        Helper.containsControlChars(password));
                tilPassword.setHelperText(
                        warning ? getString(R.string.title_setup_password_chars) : null);
                tilPassword.setHelperTextEnabled(warning);
            }
        });

        tvAppPassword.setVisibility(View.GONE);
        tvAppPassword.setPaintFlags(tvAppPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvAppPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(view.getContext(), 14);
            }
        });

        tvPasswordStorage.setPaintFlags(tvPasswordStorage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPasswordStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 37);
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

        adapterCategory = new ArrayAdapter<>(getContext(), R.layout.spinner_item1_dropdown, android.R.id.text1);
        etCategory.setThreshold(1);
        etCategory.setAdapter(adapterCategory);

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

        btnAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                Helper.openAdvanced(v.getContext(), intent);
                startActivityForResult(intent, REQUEST_AVATAR);
            }
        });

        btnAvatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                avatar = null;
                return true;
            }
        });

        Helper.linkPro(tvAvatarPro);

        btnCalendar.setEnabled(Helper.hasPermission(getContext(), Manifest.permission.WRITE_CALENDAR));
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("calendar", calendar);

                FragmentDialogCalendar fragment = new FragmentDialogCalendar();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentAccount.this, REQUEST_CALENDAR);
                fragment.show(getParentFragmentManager(), "account:calendar");
            }
        });

        Helper.linkPro(tvCalendarPro);

        btnAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = (grpAdvanced.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                grpAdvanced.setVisibility(visibility);
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

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbIgnoreSchedule.setEnabled(checked);
                cbOnDemand.setEnabled(checked);
                cbPrimary.setEnabled(checked);
            }
        });

        tvLeave.getPaint().setUnderlineText(true);
        tvLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 134);
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Helper.hide(cbNotify);
            Helper.hide(tvNotifyRemark);
            Helper.hide(view.findViewById(R.id.tvNotifyPro));
        }

        cbNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbSummary.setEnabled(cbNotify.isEnabled() && isChecked);
            }
        });

        tvNotifyRemark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 145);
            }
        });

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
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "Account:support"), false);
            }
        });

        adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityFolder>());
        adapter.setDropDownViewResource(R.layout.spinner_item1_dropdown);

        spDrafts.setAdapter(adapter);
        spSent.setAdapter(adapter);
        spArchive.setAdapter(adapter);
        spTrash.setAdapter(adapter);
        spJunk.setAdapter(adapter);

        tvSentWarning.setVisibility(View.GONE);
        spSent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tvSentWarning.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvSentWarning.setVisibility(View.VISIBLE);
            }
        });

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

        if (!DnsHelper.hasDnsSec()) {
            Helper.hide(cbDnsSec);
            Helper.hide(view.findViewById(R.id.tvDnsRemark));
            Helper.hide(cbDane);
            Helper.hide(view.findViewById(R.id.tvDaneRemark));
        }

        if (!SSLHelper.customTrustManager()) {
            Helper.hide(cbInsecure);
            Helper.hide(tvInsecureRemark);
            Helper.hide(cbDane);
            Helper.hide(view.findViewById(R.id.tvDaneRemark));
        }

        if (id < 0)
            tilPassword.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
        else
            Helper.setupPasswordToggle(getActivity(), tilPassword);

        btnAdvanced.setVisibility(View.GONE);

        tvIdle.setVisibility(View.GONE);

        btnCheck.setVisibility(View.GONE);
        pbCheck.setVisibility(View.GONE);

        btnSave.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        cbIdentity.setVisibility(View.GONE);

        cbTrust.setVisibility(View.GONE);
        btnHelp.setVisibility(View.GONE);
        btnSupport.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);
        tvInstructions.setMovementMethod(LinkMovementMethodCompat.getInstance());

        grpServer.setVisibility(View.GONE);
        grpCalendar.setVisibility(View.GONE);
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
                return EmailProvider
                        .fromDomain(context, domain, EmailProvider.Discover.IMAP)
                        .get(0);
            }

            @Override
            protected void onExecuted(Bundle args, EmailProvider provider) {
                etHost.setText(provider.imap.host);
                etPort.setText(Integer.toString(provider.imap.port));
                rgEncryption.check(provider.imap.starttls ? R.id.radio_starttls : R.id.radio_ssl);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex.getMessage() != null &&
                        (ex instanceof UnknownHostException ||
                                ex instanceof FileNotFoundException ||
                                ex instanceof IllegalArgumentException))
                    Helper.setSnackbarOptions(
                                    Snackbar.make(view, new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG))
                            .show();
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
        args.putBoolean("dnssec", cbDnsSec.isChecked());
        args.putString("host", etHost.getText().toString().trim().replace(" ", ""));
        args.putInt("encryption", encryption);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putBoolean("dane", cbDane.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putInt("auth", auth);
        args.putString("provider", provider);
        args.putString("user", etUser.getText().toString().trim());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putString("certificate", certificate);
        args.putString("realm", etRealm.getText().toString());
        args.putString("fingerprint", cbTrust.isChecked() ? (String) cbTrust.getTag() : null);
        args.putBoolean("unicode", cbUnicode.isChecked());

        new SimpleTask<CheckResult>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbCheck.setVisibility(View.VISIBLE);
                tvIdle.setVisibility(View.GONE);
                tvSentWarning.setVisibility(View.GONE);
                grpFolders.setVisibility(View.GONE);
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
                pbCheck.setVisibility(View.GONE);
            }

            @Override
            protected CheckResult onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                boolean dnssec = args.getBoolean("dnssec");
                String host = args.getString("host");
                int encryption = args.getInt("encryption");
                boolean insecure = args.getBoolean("insecure");
                boolean dane = args.getBoolean("dane");
                String port = args.getString("port");
                int auth = args.getInt("auth");
                String provider = args.getString("provider");
                String user = args.getString("user");
                String password = args.getString("password");
                String certificate = args.getString("certificate");
                String realm = args.getString("realm");
                String fingerprint = args.getString("fingerprint");
                boolean unicode = args.getBoolean("unicode");

                int semi = host.indexOf(':');
                if (semi > 0 && host.indexOf(':', semi + 1) < 0)
                    host = host.substring(0, semi);

                if (TextUtils.isEmpty(host))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (encryption == EmailService.ENCRYPTION_SSL ? "993" : "143");
                if (TextUtils.isEmpty(user) && !insecure)
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
                try (EmailService iservice = new EmailService(context,
                        protocol, realm, encryption, insecure, dane, unicode,
                        EmailService.PURPOSE_CHECK, true)) {
                    iservice.connect(
                            dnssec, host, Integer.parseInt(port),
                            auth, provider,
                            user, password,
                            certificate, fingerprint);

                    result.idle = iservice.hasCapability("IDLE");
                    result.utf8 =
                            iservice.hasCapability("UTF8=ACCEPT") ||
                                    iservice.hasCapability("UTF8=ONLY");

                    for (EntityFolder f : iservice.getFolders(host)) {
                        EntityFolder folder = db.folder().getFolderByName(id, f.name);
                        if (folder == null)
                            folder = new EntityFolder(f.name, f.type);
                        result.folders.add(folder);
                    }

                    EntityFolder.guessTypes(result.folders, host);

                    if (result.folders.size() > 0)
                        Collections.sort(result.folders, result.folders.get(0).getComparator(null));
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, CheckResult result) {
                tvIdle.setVisibility(result.idle ? View.GONE : View.VISIBLE);
                if (!result.idle)
                    etInterval.setText(Integer.toString(EntityAccount.DEFAULT_POLL_INTERVAL));

                setFolders(result.folders, result.account);

                if (!cbTrust.isChecked())
                    cbTrust.setVisibility(View.GONE);

                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                            return;
                        scroll.smoothScrollTo(0, cbIdentity.getBottom());
                    }
                });
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                tvSentWarning.setVisibility(View.GONE);
                grpFolders.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
                cbIdentity.setVisibility(View.GONE);

                if (ex instanceof IllegalArgumentException)
                    Helper.setSnackbarOptions(
                                    Snackbar.make(view, new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG))
                            .show();
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

        args.putBoolean("dnssec", cbDnsSec.isChecked());
        args.putString("host", etHost.getText().toString().trim().replace(" ", ""));
        args.putInt("encryption", encryption);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putBoolean("dane", cbDane.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putInt("auth", auth);
        args.putString("provider", provider);
        args.putString("user", etUser.getText().toString().trim());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putString("certificate", certificate);
        args.putString("realm", etRealm.getText().toString());
        args.putString("fingerprint", cbTrust.isChecked() ? (String) cbTrust.getTag() : null);

        args.putString("name", etName.getText().toString());
        args.putString("category", etCategory.getText().toString());
        args.putInt("color", btnColor.getColor());
        args.putString("avatar", avatar);
        args.putString("calendar", calendar);

        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("ignore_schedule", cbIgnoreSchedule.isChecked());
        args.putBoolean("ondemand", cbOnDemand.isChecked());
        args.putBoolean("primary", cbPrimary.isChecked());
        args.putBoolean("notify", cbNotify.isChecked());
        args.putBoolean("summary", cbSummary.isChecked());
        args.putBoolean("browse", cbBrowse.isChecked());
        args.putBoolean("auto_seen", cbAutoSeen.isChecked());
        args.putString("interval", etInterval.getText().toString());
        args.putBoolean("noop", cbNoop.isChecked());
        args.putBoolean("partial_fetch", cbPartialFetch.isChecked());
        args.putBoolean("raw_fetch", cbRawFetch.isChecked());
        args.putBoolean("ignore_size", cbIgnoreSize.isChecked());
        args.putBoolean("use_date", rgDate.getCheckedRadioButtonId() == R.id.radio_date_header);
        args.putBoolean("use_received", rgDate.getCheckedRadioButtonId() == R.id.radio_received_header);
        args.putBoolean("unicode", cbUnicode.isChecked());
        args.putBoolean("unmetered", cbUnmetered.isChecked());
        args.putBoolean("vpn_only", cbVpnOnly.isChecked());

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

                boolean dnssec = args.getBoolean("dnssec");
                String host = args.getString("host");
                int encryption = args.getInt("encryption");
                boolean insecure = args.getBoolean("insecure");
                boolean dane = args.getBoolean("dane");
                String port = args.getString("port");
                int auth = args.getInt("auth");
                String provider = args.getString("provider");
                String user = args.getString("user").trim();
                String password = args.getString("password");
                String certificate = args.getString("certificate");
                String realm = args.getString("realm");
                String fingerprint = args.getString("fingerprint");

                String name = args.getString("name");
                String category = args.getString("category");
                Integer color = args.getInt("color");
                String avatar = args.getString("avatar");
                String calendar = args.getString("calendar");

                boolean synchronize = args.getBoolean("synchronize");
                boolean ignore_schedule = args.getBoolean("ignore_schedule");
                boolean ondemand = args.getBoolean("ondemand");
                boolean primary = args.getBoolean("primary");
                boolean notify = args.getBoolean("notify");
                boolean summary = args.getBoolean("summary");
                boolean browse = args.getBoolean("browse");
                boolean auto_seen = args.getBoolean("auto_seen");
                String interval = args.getString("interval");
                boolean noop = args.getBoolean("noop");
                boolean partial_fetch = args.getBoolean("partial_fetch");
                boolean raw_fetch = args.getBoolean("raw_fetch");
                boolean ignore_size = args.getBoolean("ignore_size");
                boolean use_date = args.getBoolean("use_date");
                boolean use_received = args.getBoolean("use_received");
                boolean unicode = args.getBoolean("unicode");
                boolean unmetered = args.getBoolean("unmetered");
                boolean vpn_only = args.getBoolean("vpn_only");

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

                int semi = host.indexOf(':');
                if (semi > 0 && host.indexOf(':', semi + 1) < 0)
                    host = host.substring(0, semi);

                if (TextUtils.isEmpty(host) && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (encryption == EmailService.ENCRYPTION_SSL ? "993" : "143");
                if (TextUtils.isEmpty(user) && !insecure && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (synchronize && TextUtils.isEmpty(password) && !insecure && certificate == null && !should)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));
                int poll_interval = (TextUtils.isEmpty(interval)
                        ? EntityAccount.DEFAULT_KEEP_ALIVE_INTERVAL : Integer.parseInt(interval));

                if (TextUtils.isEmpty(realm))
                    realm = null;
                if (TextUtils.isEmpty(name))
                    name = user;
                if (TextUtils.isEmpty(category))
                    category = null;
                if (color == Color.TRANSPARENT || !pro)
                    color = null;
                if (!pro) {
                    notify = false;
                    summary = false;
                }

                long now = new Date().getTime();

                DB db = DB.getInstance(context);
                EntityAccount account = db.account().getAccount(id);

                JSONObject jconditions = new JSONObject();
                if (account != null && account.conditions != null)
                    try {
                        jconditions = new JSONObject(account.conditions);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                if (should) {
                    if (account == null)
                        return !TextUtils.isEmpty(host) && !TextUtils.isEmpty(user);

                    if (!Objects.equals(account.dnssec, dnssec))
                        return true;
                    if (!Objects.equals(account.host, host))
                        return true;
                    if (!Objects.equals(account.encryption, encryption))
                        return true;
                    if (!Objects.equals(account.insecure, insecure))
                        return true;
                    if (!Objects.equals(account.dane, dane))
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
                    if (!Objects.equals(account.category, category))
                        return true;
                    if (!Objects.equals(account.color, color))
                        return true;
                    if (!Objects.equals(account.avatar, avatar))
                        return true;
                    if (!Objects.equals(account.calendar, calendar))
                        return true;
                    if (!Objects.equals(account.synchronize, synchronize))
                        return true;
                    if (ignore_schedule != jconditions.optBoolean("ignore_schedule"))
                        return true;
                    if (!Objects.equals(account.ondemand, ondemand))
                        return true;
                    if (!Objects.equals(account.primary, account.synchronize && primary))
                        return true;
                    if (!Objects.equals(account.notify, notify))
                        return true;
                    if (!Objects.equals(account.summary, summary))
                        return true;
                    if (!Objects.equals(account.browse, browse))
                        return true;
                    if (!Objects.equals(account.auto_seen, auto_seen))
                        return true;
                    if (!Objects.equals(account.poll_interval, poll_interval))
                        return true;
                    if (!Objects.equals(account.keep_alive_noop, noop))
                        return true;
                    if (!Objects.equals(account.partial_fetch, partial_fetch))
                        return true;
                    if (!Objects.equals(account.raw_fetch, raw_fetch))
                        return true;
                    if (!Objects.equals(account.ignore_size, ignore_size))
                        return true;
                    if (!Objects.equals(account.use_date, use_date))
                        return true;
                    if (!Objects.equals(account.use_received, use_received))
                        return true;
                    if (!Objects.equals(account.unicode, unicode))
                        return true;
                    if (unmetered != jconditions.optBoolean("unmetered"))
                        return true;
                    if (vpn_only != jconditions.optBoolean("vpn_only"))
                        return true;
                    if (account.error != null && account.synchronize)
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
                        !account.dnssec.equals(dnssec) ||
                        !account.host.equals(host) ||
                        !account.encryption.equals(encryption) ||
                        !account.insecure.equals(insecure) ||
                        !account.dane.equals(dane) ||
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
                    try (EmailService iservice = new EmailService(context,
                            protocol, realm, encryption, insecure, dane, unicode,
                            EmailService.PURPOSE_CHECK, true)) {
                        iservice.connect(
                                dnssec, host, Integer.parseInt(port),
                                auth, provider,
                                user, password,
                                certificate, fingerprint);

                        for (EntityFolder f : iservice.getFolders(host))
                            if (EntityFolder.INBOX.equals(f.type)) {
                                inbox = new EntityFolder();
                                inbox.name = f.name;
                                inbox.type = f.type;
                                inbox.synchronize = true;
                                inbox.unified = true;
                                inbox.notify = true;
                                inbox.sync_days = EntityFolder.DEFAULT_SYNC;
                                inbox.keep_days = EntityFolder.DEFAULT_KEEP;
                            }
                    }
                }

                boolean reschedule = (ignore_schedule != jconditions.optBoolean("ignore_schedule"));

                try {
                    db.beginTransaction();

                    if (account != null && !account.password.equals(password)) {
                        String root = UriHelper.getRootDomain(context, account.host);
                        String match = (root == null || root.equals(account.host) ? account.host : "%." + root);
                        int count = db.identity().setIdentityPassword(account.id, account.user, password, auth, match);
                        Log.i("Updated passwords=" + count + " match=" + match);
                    }

                    boolean update = (account != null);
                    if (account == null)
                        account = new EntityAccount();

                    account.dnssec = dnssec;
                    account.host = host;
                    account.encryption = encryption;
                    account.insecure = insecure;
                    account.dane = dane;
                    account.port = Integer.parseInt(port);
                    account.auth_type = auth;
                    account.user = user;
                    account.password = password;
                    account.certificate_alias = certificate;
                    account.provider = provider;
                    account.realm = realm;
                    account.fingerprint = fingerprint;

                    account.name = name;
                    account.category = category;
                    account.color = color;
                    account.avatar = avatar;
                    account.calendar = calendar;

                    account.synchronize = synchronize;
                    jconditions.put("ignore_schedule", ignore_schedule);
                    account.ondemand = ondemand;
                    account.primary = (account.synchronize && primary);
                    account.notify = notify;
                    account.summary = summary;
                    account.browse = browse;
                    account.auto_seen = auto_seen;

                    if (account.poll_interval != poll_interval) {
                        account.keep_alive_ok = false;
                        account.keep_alive_failed = 0;
                        account.keep_alive_succeeded = 0;
                    }
                    account.poll_interval = Math.max(1, poll_interval);
                    account.keep_alive_noop = noop;
                    account.partial_fetch = partial_fetch;
                    account.raw_fetch = raw_fetch;
                    account.ignore_size = ignore_size;
                    account.use_date = use_date;
                    account.use_received = use_received;

                    account.unicode = unicode;

                    jconditions.put("unmetered", unmetered);
                    jconditions.put("vpn_only", vpn_only);
                    account.conditions = jconditions.toString();

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

                    Map<String, EntityFolder> current = new HashMap<>();
                    for (EntityFolder folder : db.folder().getFolders(account.id, false, false)) {
                        Log.i("Got folder=" + folder.name);
                        current.put(folder.name, folder);
                    }

                    db.folder().setFoldersUser(account.id);

                    for (EntityFolder folder : folders) {
                        Log.i("Checking folder=" + folder.name + ":" + folder.type);
                        EntityFolder existing = current.get(folder.name);
                        EntityFolder indb = db.folder().getFolderByName(account.id, folder.name);
                        if (existing == null && indb != null) {
                            existing = indb;
                            for (EntityFolder f : current.values())
                                Log.breadcrumb("Debug", "From db", f.name + ":" + f.type);
                            for (EntityFolder f : folders)
                                Log.breadcrumb("Debug", "From config", f.name + ":" + f.type);
                            Log.e("Exists in db folder=" + indb.name + ":" + indb.type +
                                    " not exists folder=" + folder.name + ":" + folder.type);
                        }
                        if (existing == null) {
                            folder.id = null;
                            folder.account = account.id;
                            folder.setSpecials(account);
                            folder.id = db.folder().insertFolder(folder);
                            EntityLog.log(context, "Added folder=" + folder.name + ":" + folder.type);
                            if (folder.synchronize && account.synchronize)
                                EntityOperation.sync(context, folder.id, true);
                        } else {
                            db.folder().setFolderType(existing.id, folder.type);
                            if (folder.synchronize && account.synchronize &&
                                    !Objects.equals(existing.type, folder.type)) {
                                EntityLog.log(context, "Updated folder=" + folder.name + ":" + folder.type +
                                        " existing=" + existing.name + ":" + existing.type);
                                db.folder().setFolderSynchronize(existing.id, true);
                                EntityOperation.sync(context, existing.id, true);
                            }
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

                if (reschedule)
                    ServiceSynchronize.reschedule(context);
                else
                    ServiceSynchronize.eval(context, "save account");

                if (!synchronize) {
                    NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                    nm.cancel("receive:" + account.id, NotificationHelper.NOTIFICATION_TAGGED);
                    nm.cancel("alert:" + account.id, NotificationHelper.NOTIFICATION_TAGGED);
                }

                args.putBoolean("saved", true);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("unset." + account.id + "." + EntityFolder.DRAFTS, drafts == null);
                editor.putBoolean("unset." + account.id + "." + EntityFolder.SENT, sent == null);
                editor.putBoolean("unset." + account.id + "." + EntityFolder.ARCHIVE, archive == null);
                editor.putBoolean("unset." + account.id + "." + EntityFolder.TRASH, trash == null);
                editor.putBoolean("unset." + account.id + "." + EntityFolder.JUNK, junk == null);
                editor.apply();

                FairEmailBackupAgent.dataChanged(context);

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

                    finish();

                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        boolean saved = args.getBoolean("saved");
                        if (saved && cbIdentity.isChecked()) {
                            Bundle aargs = new Bundle();
                            aargs.putLong("account", args.getLong("account"));

                            FragmentIdentity fragment = new FragmentIdentity();
                            fragment.setArguments(aargs);
                            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.leave_to_left);
                            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("identity");
                            fragmentTransaction.commit();
                        }
                    }
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Helper.setSnackbarOptions(
                                    Snackbar.make(view, new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG))
                            .show();
                else
                    showError(ex);
            }
        }.execute(this, args, "account:save");
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
        outState.putInt("fair:provider", spProvider == null ? 0 : spProvider.getSelectedItemPosition());
        outState.putString("fair:certificate", certificate);
        outState.putString("fair:password", tilPassword == null ? null : tilPassword.getEditText().getText().toString());
        outState.putInt("fair:advanced", grpAdvanced == null ? View.VISIBLE : grpAdvanced.getVisibility());
        outState.putInt("fair:auth", auth);
        outState.putString("fair:authprovider", provider);
        outState.putString("fair:avatar", avatar);
        outState.putString("fair:calendar", calendar);
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

                List<String> categories = db.account().getAccountCategories();
                if (categories == null)
                    categories = new ArrayList<>();
                args.putStringArrayList("categories", new ArrayList<>(categories));

                List<EntityIdentity> identities = db.identity().getIdentities(id);
                if (identities != null && identities.size() == 1)
                    args.putString("personal", identities.get(0).name);

                return db.account().getAccount(id);
            }

            @Override
            protected void onExecuted(Bundle args, final EntityAccount account) {
                // Get providers
                final Context context = getContext();

                int colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
                int textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

                List<EmailProvider> providers = EmailProvider.getProviders(context);
                providers.add(0, new EmailProvider(getString(R.string.title_select)));
                providers.add(1, new EmailProvider(getString(R.string.title_custom)));

                ArrayAdapter<EmailProvider> aaProvider =
                        new ArrayAdapter<EmailProvider>(context, R.layout.spinner_item1, android.R.id.text1, providers) {
                            @NonNull
                            @Override
                            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                return updateView(position, super.getView(position, convertView, parent));
                            }

                            @Override
                            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                return updateView(position, super.getDropDownView(position, convertView, parent));
                            }

                            private View updateView(int position, View view) {
                                TextView tv = view.findViewById(android.R.id.text1);
                                tv.setTypeface(null, position == 1 ? Typeface.BOLD : Typeface.NORMAL);
                                tv.setTextColor(position == 1 ? colorAccent : textColorSecondary);

                                return view;
                            }
                        };
                aaProvider.setDropDownViewResource(R.layout.spinner_item1_dropdown);
                spProvider.setAdapter(aaProvider);

                adapterCategory.clear();
                adapterCategory.addAll(args.getStringArrayList("categories"));

                if (savedInstanceState == null) {
                    JSONObject jcondition = new JSONObject();
                    try {
                        if (account != null && account.conditions != null)
                            jcondition = new JSONObject(account.conditions);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                    cbDnsSec.setChecked(account == null ? false : account.dnssec);

                    if (account != null) {
                        boolean found = false;
                        for (int pos = 2; pos < providers.size(); pos++) {
                            EmailProvider provider = providers.get(pos);
                            if ((provider.oauth != null) ==
                                    (account.auth_type == AUTH_TYPE_GMAIL || account.auth_type == AUTH_TYPE_OAUTH) &&
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
                    cbDane.setChecked(account == null ? false : account.dane);
                    cbDane.setEnabled(!cbInsecure.isChecked());

                    etUser.setText(account == null ? null : account.user);
                    tilPassword.getEditText().setText(account == null ? null : account.password);
                    tvAppPassword.setVisibility(account != null && EntityAccount.isOutlook(account.provider)
                            ? View.VISIBLE : View.GONE);
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
                    etCategory.setText(account == null ? null : account.category);
                    btnColor.setColor(account == null ? null : account.color);

                    cbNotify.setChecked(account != null && account.notify);
                    cbSummary.setChecked(account != null && account.summary);

                    cbSynchronize.setChecked(account == null ? true : account.synchronize);
                    cbIgnoreSchedule.setChecked(jcondition.optBoolean("ignore_schedule"));
                    cbOnDemand.setChecked(account == null ? false : account.ondemand);
                    cbPrimary.setChecked(account == null ? false : account.primary);
                    cbBrowse.setChecked(account == null ? true : account.browse);
                    cbAutoSeen.setChecked(account == null ? true : account.auto_seen);
                    etInterval.setText(account == null ? "" : Long.toString(account.poll_interval));
                    cbNoop.setChecked(account == null ? true : account.keep_alive_noop);
                    cbPartialFetch.setChecked(account == null ? true : account.partial_fetch);
                    cbRawFetch.setChecked(account == null ? false : account.raw_fetch);
                    cbIgnoreSize.setChecked(account == null ? false : account.ignore_size);
                    cbUnicode.setChecked(account == null ? false : account.unicode);
                    cbUnmetered.setChecked(jcondition.optBoolean("unmetered"));
                    cbVpnOnly.setChecked(jcondition.optBoolean("vpn_only"));

                    if (account != null && account.use_date)
                        rgDate.check(R.id.radio_date_header);
                    else if (account != null && account.use_received)
                        rgDate.check(R.id.radio_received_header);
                    else
                        rgDate.check(R.id.radio_server_time);

                    auth = (account == null ? AUTH_TYPE_PASSWORD : account.auth_type);
                    provider = (account == null ? null : account.provider);
                    avatar = (account == null ? null : account.avatar);
                    calendar = (account == null ? null : account.calendar);

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
                    avatar = savedInstanceState.getString("fair:avatar");
                    calendar = savedInstanceState.getString("fair:calendar");
                }

                Helper.setViewsEnabled(view, true);
                boolean pro = ActivityBilling.isPro(context);
                cbNotify.setEnabled(pro);
                cbSummary.setEnabled(pro && cbNotify.isChecked());

                if (auth != AUTH_TYPE_PASSWORD) {
                    etUser.setEnabled(false);
                    tilPassword.getEditText().setEnabled(false);
                    btnCertificate.setEnabled(false);

                    tilPassword.setEndIconDrawable(R.drawable.twotone_edit_24);
                    tilPassword.setEndIconOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, FragmentAccount.this, view);

                            popupMenu.getMenu().add(Menu.NONE, R.string.title_account_auth_update, 1, R.string.title_account_auth_update);
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_account_auth_password, 2, R.string.title_account_auth_password);

                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    int id = item.getItemId();
                                    if (id == R.string.title_account_auth_update) {
                                        onUpdate();
                                        return true;
                                    } else if (id == R.string.title_account_auth_password) {
                                        onPassword();
                                        return true;
                                    } else
                                        return false;
                                }

                                private void onUpdate() {
                                    Fragment fragment;
                                    if (auth == AUTH_TYPE_GMAIL)
                                        fragment = new FragmentGmail();
                                    else if (auth == AUTH_TYPE_OAUTH)
                                        fragment = new FragmentOAuth();
                                    else if (auth == AUTH_TYPE_PASSWORD) {
                                        onPassword();
                                        return;
                                    } else {
                                        Log.e("Unknown auth=" + auth);
                                        return;
                                    }

                                    try {
                                        Bundle aargs = new Bundle();
                                        if (auth == AUTH_TYPE_OAUTH) {
                                            if (account == null)
                                                throw new IllegalArgumentException("Account missing");

                                            EmailProvider provider =
                                                    EmailProvider.getProvider(view.getContext(), account.provider);
                                            aargs.putString("id", provider.id);
                                            aargs.putString("name", provider.description);
                                            aargs.putString("privacy", provider.oauth.privacy);
                                            aargs.putBoolean("askAccount", provider.oauth.askAccount);
                                        }
                                        aargs.putString("personal", args.getString("personal"));
                                        aargs.putString("address", etUser.getText().toString());
                                        aargs.putBoolean("update", true);

                                        fragment.setArguments(aargs);

                                        finish();

                                        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                                        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("quick");
                                        fragmentTransaction.commit();
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
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

                cbIgnoreSchedule.setEnabled(cbSynchronize.isChecked());
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
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onSave(true);
            return true;
        } else if (itemId == R.id.menu_delete) {
            onMenuDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                case REQUEST_AVATAR:
                    if (resultCode == RESULT_OK && data != null)
                        onImageSelected(data.getData());
                    else
                        avatar = null;
                    break;
                case REQUEST_CALENDAR:
                    if (resultCode == RESULT_OK && data != null) {
                        if (ActivityBilling.isPro(getContext())) {
                            Bundle args = data.getBundleExtra("args");
                            JSONObject jobject = new JSONObject();
                            jobject.put("id", args.getLong("id"));
                            jobject.put("account", args.getString("account"));
                            jobject.put("type", args.getString("type"));
                            jobject.put("name", args.getString("name"));
                            calendar = jobject.toString();
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
                                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                    return;
                                scroll.smoothScrollTo(0, (save ? btnSave : btnCheck).getBottom());
                            }
                        });
                        if (save || !cbSynchronize.isChecked())
                            onSave(false);
                        else
                            onCheck();
                    } else
                        finish();
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

    private void onImageSelected(Uri uri) {
        final Context context = getContext();

        if (!ActivityBilling.isPro(context)) {
            startActivity(new Intent(context, ActivityBilling.class));
            return;
        }

        try {
            NoStreamException.check(uri, context);

            context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (!Helper.isPersisted(context, uri, true, false))
                throw new IllegalStateException("No permission granted to access selected image " + uri);

            avatar = uri.toString();
        } catch (NoStreamException ex) {
            ex.report(getActivity());
        } catch (Throwable ex) {
            Log.unexpectedError(getParentFragmentManager(), ex);
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
                finish();
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
        seen.name = context.getString(R.string.title_seen_unseen);
        folders.add(seen);

        EntityFolder snooze = new EntityFolder();
        snooze.id = EntityMessage.SWIPE_ACTION_SNOOZE;
        snooze.name = context.getString(R.string.title_snooze_now);
        folders.add(snooze);

        EntityFolder hide = new EntityFolder();
        hide.id = EntityMessage.SWIPE_ACTION_HIDE;
        hide.name = context.getString(R.string.title_hide);
        folders.add(hide);

        EntityFolder flag = new EntityFolder();
        flag.id = EntityMessage.SWIPE_ACTION_FLAG;
        flag.name = context.getString(R.string.title_flag);
        folders.add(flag);

        EntityFolder importance = new EntityFolder();
        importance.id = EntityMessage.SWIPE_ACTION_IMPORTANCE;
        importance.name = context.getString(R.string.title_set_importance);
        folders.add(importance);

        if (!Helper.isPlayStoreInstall()) {
            EntityFolder tts = new EntityFolder();
            tts.id = EntityMessage.SWIPE_ACTION_TTS;
            tts.name = context.getString(R.string.title_rule_tts);
            folders.add(tts);
        }

        if (AI.isAvailable(context)) {
            EntityFolder summarize = new EntityFolder();
            summarize.id = EntityMessage.SWIPE_ACTION_SUMMARIZE;
            summarize.name = context.getString(R.string.title_summarize);
            folders.add(summarize);
        }

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
