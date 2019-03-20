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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;

import static android.accounts.AccountManager.newChooseAccountIntent;

public class FragmentAccount extends FragmentBase {
    private ViewGroup view;

    private Spinner spProvider;

    private EditText etDomain;
    private Button btnAutoConfig;

    private Button btnAuthorize;
    private EditText etHost;
    private RadioGroup rgEncryption;
    private CheckBox cbInsecure;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private EditText etRealm;

    private EditText etName;
    private Button btnColor;
    private View vwColor;
    private ImageView ibColorDefault;

    private Button btnAdvanced;
    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;
    private CheckBox cbNotify;
    private CheckBox cbBrowse;
    private EditText etInterval;
    private EditText etPrefix;

    private Button btnCheck;
    private ContentLoadingProgressBar pbCheck;
    private TextView tvIdle;
    private TextView tvMove;
    private TextView tvUtf8;

    private ArrayAdapter<EntityFolder> adapter;
    private Spinner spDrafts;
    private Spinner spSent;
    private Spinner spAll;
    private Spinner spTrash;
    private Spinner spJunk;
    private Spinner spLeft;
    private Spinner spRight;

    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private TextView tvError;

    private ContentLoadingProgressBar pbWait;

    private Group grpServer;
    private Group grpAuthorize;
    private Group grpAdvanced;
    private Group grpFolders;

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
        setSubtitle(R.string.title_edit_account);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_account, container, false);

        // Get controls
        spProvider = view.findViewById(R.id.spProvider);

        etDomain = view.findViewById(R.id.etDomain);
        btnAutoConfig = view.findViewById(R.id.btnAutoConfig);

        btnAuthorize = view.findViewById(R.id.btnAuthorize);
        etHost = view.findViewById(R.id.etHost);
        etPort = view.findViewById(R.id.etPort);
        rgEncryption = view.findViewById(R.id.rgEncryption);
        cbInsecure = view.findViewById(R.id.cbInsecure);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        etRealm = view.findViewById(R.id.etRealm);

        etName = view.findViewById(R.id.etName);
        btnColor = view.findViewById(R.id.btnColor);
        vwColor = view.findViewById(R.id.vwColor);
        ibColorDefault = view.findViewById(R.id.ibColorDefault);

        btnAdvanced = view.findViewById(R.id.btnAdvanced);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbNotify = view.findViewById(R.id.cbNotify);
        cbBrowse = view.findViewById(R.id.cbBrowse);
        etInterval = view.findViewById(R.id.etInterval);
        etPrefix = view.findViewById(R.id.etPrefix);

        btnCheck = view.findViewById(R.id.btnCheck);
        pbCheck = view.findViewById(R.id.pbCheck);

        tvIdle = view.findViewById(R.id.tvIdle);
        tvMove = view.findViewById(R.id.tvMove);
        tvUtf8 = view.findViewById(R.id.tvUtf8);

        spDrafts = view.findViewById(R.id.spDrafts);
        spSent = view.findViewById(R.id.spSent);
        spAll = view.findViewById(R.id.spAll);
        spTrash = view.findViewById(R.id.spTrash);
        spJunk = view.findViewById(R.id.spJunk);
        spLeft = view.findViewById(R.id.spLeft);
        spRight = view.findViewById(R.id.spRight);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        tvError = view.findViewById(R.id.tvError);

        pbWait = view.findViewById(R.id.pbWait);

        grpServer = view.findViewById(R.id.grpServer);
        grpAuthorize = view.findViewById(R.id.grpAuthorize);
        grpAdvanced = view.findViewById(R.id.grpAdvanced);
        grpFolders = view.findViewById(R.id.grpFolders);

        // Wire controls

        spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long itemid) {
                EmailProvider provider = (EmailProvider) adapterView.getSelectedItem();
                grpServer.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                grpAuthorize.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

                btnAuthorize.setVisibility(provider.type == null ? View.GONE : View.VISIBLE);

                btnAdvanced.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                if (position == 0)
                    grpAdvanced.setVisibility(View.GONE);

                btnCheck.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                tvIdle.setVisibility(View.GONE);
                tvMove.setVisibility(View.GONE);
                tvUtf8.setVisibility(View.GONE);

                Object tag = adapterView.getTag();
                if (tag != null && (Integer) tag == position)
                    return;
                adapterView.setTag(position);

                auth_type = Helper.AUTH_TYPE_PASSWORD;

                etHost.setText(provider.imap_host);
                etPort.setText(provider.imap_host == null ? null : Integer.toString(provider.imap_port));
                rgEncryption.check(provider.imap_starttls ? R.id.radio_starttls : R.id.radio_ssl);

                etUser.setTag(null);
                etUser.setText(null);
                tilPassword.getEditText().setText(null);
                etRealm.setText(null);
                tilPassword.setEnabled(true);
                etRealm.setEnabled(true);

                etName.setText(position > 1 ? provider.name : null);
                etPrefix.setText(provider.prefix);

                grpFolders.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
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

        rgEncryption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int id) {
                etPort.setHint(id == R.id.radio_starttls ? "143" : "993");
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
                    tilPassword.setPasswordVisibilityToggleEnabled(true);
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

        btnAuthorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailProvider provider = (EmailProvider) spProvider.getSelectedItem();
                Log.i("Authorize " + provider);

                if ("com.google".equals(provider.type)) {
                    String permission = Manifest.permission.GET_ACCOUNTS;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O &&
                            !Helper.hasPermission(getContext(), permission)) {
                        Log.i("Requesting " + permission);
                        requestPermissions(new String[]{permission}, ActivitySetup.REQUEST_PERMISSION);
                    } else
                        selectAccount();
                }
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

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbPrimary.setEnabled(checked);
            }
        });

        cbNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !Helper.isPro(getContext())) {
                    cbNotify.setChecked(false);

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                    fragmentTransaction.commit();
                }
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheck();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });

        adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityFolder>());
        adapter.setDropDownViewResource(R.layout.spinner_item1_dropdown);

        spDrafts.setAdapter(adapter);
        spSent.setAdapter(adapter);
        spAll.setAdapter(adapter);
        spTrash.setAdapter(adapter);
        spJunk.setAdapter(adapter);
        spLeft.setAdapter(adapter);
        spRight.setAdapter(adapter);

        // Initialize
        Helper.setViewsEnabled(view, false);

        btnAutoConfig.setEnabled(false);

        btnAuthorize.setVisibility(View.GONE);
        rgEncryption.setVisibility(View.GONE);
        cbInsecure.setVisibility(View.GONE);
        tilPassword.setPasswordVisibilityToggleEnabled(id < 0);

        btnAdvanced.setVisibility(View.GONE);

        tvIdle.setVisibility(View.GONE);
        tvMove.setVisibility(View.GONE);
        tvUtf8.setVisibility(View.GONE);

        btnCheck.setVisibility(View.GONE);
        pbCheck.setVisibility(View.GONE);

        btnSave.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        grpServer.setVisibility(View.GONE);
        grpAuthorize.setVisibility(View.GONE);
        grpAdvanced.setVisibility(View.GONE);
        grpFolders.setVisibility(View.GONE);

        return view;
    }

    private void onAutoConfig() {
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
                etHost.setText(provider.imap_host);
                etPort.setText(Integer.toString(provider.imap_port));
                rgEncryption.check(provider.imap_starttls ? R.id.radio_starttls : R.id.radio_ssl);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException || ex instanceof UnknownHostException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentAccount.this, args, "account:config");
    }

    private void onCheck() {
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putInt("auth_type", auth_type);
        args.putString("host", etHost.getText().toString());
        args.putBoolean("starttls", rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putString("user", etUser.getText().toString());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putString("realm", etRealm.getText().toString());

        new SimpleTask<CheckResult>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbCheck.setVisibility(View.VISIBLE);
                tvIdle.setVisibility(View.GONE);
                tvMove.setVisibility(View.GONE);
                tvUtf8.setVisibility(View.GONE);
                grpFolders.setVisibility(View.GONE);
                tvError.setVisibility(View.GONE);
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
                int auth_type = args.getInt("auth_type");
                String host = args.getString("host");
                boolean starttls = args.getBoolean("starttls");
                boolean insecure = args.getBoolean("insecure");
                String port = args.getString("port");
                String user = args.getString("user");
                String password = args.getString("password");
                String realm = args.getString("realm");

                if (TextUtils.isEmpty(host))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (starttls ? "143" : "993");
                if (TextUtils.isEmpty(user))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (TextUtils.isEmpty(password) && !insecure)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));

                if (TextUtils.isEmpty(realm))
                    realm = null;

                DB db = DB.getInstance(context);

                CheckResult result = new CheckResult();
                result.account = db.account().getAccount(id);
                result.folders = new ArrayList<>();

                // Check IMAP server / get folders
                Properties props = MessageHelper.getSessionProperties(auth_type, realm, insecure);
                Session isession = Session.getInstance(props, null);
                isession.setDebug(true);
                try (Store istore = isession.getStore("imap" + (starttls ? "" : "s"))) {
                    try {
                        istore.connect(host, Integer.parseInt(port), user, password);
                    } catch (AuthenticationFailedException ex) {
                        if (auth_type == Helper.AUTH_TYPE_GMAIL) {
                            password = Helper.refreshToken(context, "com.google", user, password);
                            istore.connect(host, Integer.parseInt(port), user, password);
                        } else
                            throw ex;
                    }

                    result.idle = ((IMAPStore) istore).hasCapability("IDLE");
                    result.move = ((IMAPStore) istore).hasCapability("MOVE");

                    boolean inbox = false;
                    boolean archive = false;
                    boolean drafts = false;
                    boolean trash = false;
                    boolean sent = false;
                    boolean junk = false;
                    EntityFolder altArchive = null;
                    EntityFolder altDrafts = null;
                    EntityFolder altTrash = null;
                    EntityFolder altSent = null;
                    EntityFolder altJunk = null;

                    for (Folder ifolder : istore.getDefaultFolder().list("*")) {
                        // Check folder attributes
                        String fullName = ifolder.getFullName();
                        String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                        Log.i(fullName + " attrs=" + TextUtils.join(" ", attrs));
                        String type = EntityFolder.getType(attrs, fullName);

                        if (type != null) {
                            // Create entry
                            EntityFolder folder = db.folder().getFolderByName(id, fullName);
                            if (folder == null) {
                                int sync = EntityFolder.SYSTEM_FOLDER_SYNC.indexOf(type);
                                folder = new EntityFolder();
                                folder.name = fullName;
                                folder.type = type;
                                folder.synchronize = (sync >= 0);
                                folder.download = (sync < 0 ? true : EntityFolder.SYSTEM_FOLDER_DOWNLOAD.get(sync));
                                folder.sync_days = EntityFolder.DEFAULT_SYNC;
                                folder.keep_days = EntityFolder.DEFAULT_KEEP;
                            }
                            result.folders.add(folder);

                            if (EntityFolder.USER.equals(type)) {
                                if (folder.name.toLowerCase().contains("archive"))
                                    altArchive = folder;
                                if (folder.name.toLowerCase().contains("draft"))
                                    altDrafts = folder;
                                if (folder.name.toLowerCase().contains("trash"))
                                    altTrash = folder;
                                if (folder.name.toLowerCase().contains("sent"))
                                    altSent = folder;
                                if (folder.name.toLowerCase().contains("junk"))
                                    altJunk = folder;
                            } else {
                                if (EntityFolder.INBOX.equals(type))
                                    inbox = true;
                                else if (EntityFolder.ARCHIVE.equals(type))
                                    archive = true;
                                else if (EntityFolder.DRAFTS.equals(type))
                                    drafts = true;
                                else if (EntityFolder.TRASH.equals(type))
                                    trash = true;
                                else if (EntityFolder.SENT.equals(type))
                                    sent = true;
                                else if (EntityFolder.JUNK.equals(type))
                                    junk = true;
                            }

                            if (EntityFolder.INBOX.equals(type))
                                result.utf8 = (Boolean) ((IMAPFolder) ifolder).doCommand(new IMAPFolder.ProtocolCommand() {
                                    @Override
                                    public Object doCommand(IMAPProtocol protocol) {
                                        return protocol.supportsUtf8();
                                    }
                                });

                            Log.i(folder.name + " id=" + folder.id +
                                    " type=" + folder.type + " attr=" + TextUtils.join(",", attrs));
                        }
                    }

                    if (!inbox)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_inbox));
                    if (!archive && altArchive != null)
                        altArchive.type = EntityFolder.ARCHIVE;
                    if (!drafts && altDrafts != null)
                        altDrafts.type = EntityFolder.DRAFTS;
                    if (!trash && altTrash != null)
                        altTrash.type = EntityFolder.TRASH;
                    if (!sent && altSent != null)
                        altSent.type = EntityFolder.SENT;
                    if (!junk && altJunk != null)
                        altJunk.type = EntityFolder.JUNK;

                    for (EntityFolder folder : result.folders)
                        folder.display = folder.getDisplayName(getContext());
                    EntityFolder.sort(getContext(), result.folders, true);

                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, CheckResult result) {
                tvIdle.setVisibility(result.idle ? View.GONE : View.VISIBLE);
                tvMove.setVisibility(result.move ? View.GONE : View.VISIBLE);
                tvUtf8.setVisibility(result.utf8 == null || result.utf8 ? View.GONE : View.VISIBLE);

                setFolders(result.folders, result.account);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        ((ScrollView) view).smoothScrollTo(0, btnSave.getBottom());
                    }
                });
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                grpFolders.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);

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
        }.execute(FragmentAccount.this, args, "account:check");
    }

    private void onSave() {
        EntityFolder drafts = (EntityFolder) spDrafts.getSelectedItem();
        EntityFolder sent = (EntityFolder) spSent.getSelectedItem();
        EntityFolder all = (EntityFolder) spAll.getSelectedItem();
        EntityFolder trash = (EntityFolder) spTrash.getSelectedItem();
        EntityFolder junk = (EntityFolder) spJunk.getSelectedItem();
        EntityFolder left = (EntityFolder) spLeft.getSelectedItem();
        EntityFolder right = (EntityFolder) spRight.getSelectedItem();

        if (drafts != null && drafts.type == null)
            drafts = null;
        if (sent != null && sent.type == null)
            sent = null;
        if (all != null && all.type == null)
            all = null;
        if (trash != null && trash.type == null)
            trash = null;
        if (junk != null && junk.type == null)
            junk = null;
        if (left != null && left.type == null)
            left = null;
        if (right != null && right.type == null)
            right = null;

        Bundle args = new Bundle();
        args.putLong("id", id);

        args.putInt("auth_type", auth_type);
        args.putString("host", etHost.getText().toString());
        args.putBoolean("starttls", rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putString("user", etUser.getText().toString());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putString("realm", etRealm.getText().toString());

        args.putString("name", etName.getText().toString());
        args.putInt("color", color);

        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("primary", cbPrimary.isChecked());
        args.putBoolean("notify", cbNotify.isChecked());
        args.putBoolean("browse", cbBrowse.isChecked());
        args.putString("interval", etInterval.getText().toString());
        args.putString("prefix", etPrefix.getText().toString());

        args.putSerializable("drafts", drafts);
        args.putSerializable("sent", sent);
        args.putSerializable("all", all);
        args.putSerializable("trash", trash);
        args.putSerializable("junk", junk);
        args.putSerializable("left", left);
        args.putSerializable("right", right);

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

                int auth_type = args.getInt("auth_type");
                String host = args.getString("host");
                boolean starttls = args.getBoolean("starttls");
                boolean insecure = args.getBoolean("insecure");
                String port = args.getString("port");
                String user = args.getString("user");
                String password = args.getString("password");
                String realm = args.getString("realm");

                String name = args.getString("name");
                Integer color = args.getInt("color");

                boolean synchronize = args.getBoolean("synchronize");
                boolean primary = args.getBoolean("primary");
                boolean notify = args.getBoolean("notify");
                boolean browse = args.getBoolean("browse");
                String interval = args.getString("interval");
                String prefix = args.getString("prefix");

                EntityFolder drafts = (EntityFolder) args.getSerializable("drafts");
                EntityFolder sent = (EntityFolder) args.getSerializable("sent");
                EntityFolder all = (EntityFolder) args.getSerializable("all");
                EntityFolder trash = (EntityFolder) args.getSerializable("trash");
                EntityFolder junk = (EntityFolder) args.getSerializable("junk");
                EntityFolder left = (EntityFolder) args.getSerializable("left");
                EntityFolder right = (EntityFolder) args.getSerializable("right");

                if (TextUtils.isEmpty(host))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (starttls ? "143" : "993");
                if (TextUtils.isEmpty(user))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (synchronize && TextUtils.isEmpty(password) && !insecure)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));
                if (TextUtils.isEmpty(interval))
                    interval = Integer.toString(EntityAccount.DEFAULT_KEEP_ALIVE_INTERVAL);

                if (TextUtils.isEmpty(realm))
                    realm = null;

                if (Color.TRANSPARENT == color)
                    color = null;
                if (TextUtils.isEmpty(prefix))
                    prefix = null;

                Character separator = null;
                long now = new Date().getTime();

                DB db = DB.getInstance(context);
                EntityAccount account = db.account().getAccount(id);

                String accountRealm = (account == null ? null : account.realm);

                boolean check = (synchronize && (account == null ||
                        auth_type != account.auth_type ||
                        !host.equals(account.host) || Integer.parseInt(port) != account.port ||
                        !user.equals(account.user) || !password.equals(account.password) ||
                        !Objects.equals(realm, accountRealm)));
                boolean reload = (check || account == null ||
                        !Objects.equals(account.prefix, prefix) ||
                        account.synchronize != synchronize ||
                        account.notify != notify ||
                        !account.poll_interval.equals(Integer.parseInt(interval)));

                Long last_connected = null;
                if (account != null && synchronize == account.synchronize)
                    last_connected = account.last_connected;

                // Check IMAP server
                EntityFolder inbox = null;
                if (check) {
                    Properties props = MessageHelper.getSessionProperties(auth_type, realm, insecure);
                    Session isession = Session.getInstance(props, null);
                    isession.setDebug(true);

                    try (Store istore = isession.getStore("imap" + (starttls ? "" : "s"))) {
                        try {
                            istore.connect(host, Integer.parseInt(port), user, password);
                        } catch (AuthenticationFailedException ex) {
                            if (auth_type == Helper.AUTH_TYPE_GMAIL) {
                                password = Helper.refreshToken(context, "com.google", user, password);
                                istore.connect(host, Integer.parseInt(port), user, password);
                            } else
                                throw ex;
                        }
                        separator = istore.getDefaultFolder().getSeparator();

                        for (Folder ifolder : istore.getDefaultFolder().list("*")) {
                            // Check folder attributes
                            String fullName = ifolder.getFullName();
                            String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                            Log.i(fullName + " attrs=" + TextUtils.join(" ", attrs));
                            String type = EntityFolder.getType(attrs, fullName);

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

                if (TextUtils.isEmpty(name))
                    name = user;

                try {
                    db.beginTransaction();

                    boolean update = (account != null);
                    if (account == null)
                        account = new EntityAccount();

                    account.auth_type = auth_type;
                    account.host = host;
                    account.starttls = starttls;
                    account.insecure = insecure;
                    account.port = Integer.parseInt(port);
                    account.user = user;
                    account.password = password;
                    account.realm = realm;

                    account.name = name;
                    account.color = color;

                    account.synchronize = synchronize;
                    account.primary = (account.synchronize && primary);
                    account.notify = notify;
                    account.browse = browse;
                    account.poll_interval = Integer.parseInt(interval);
                    account.prefix = prefix;

                    if (!update)
                        account.created = now;

                    account.error = null;
                    account.last_connected = last_connected;

                    if (account.primary)
                        db.account().resetPrimary();

                    if (!Helper.isPro(context)) {
                        account.color = null;
                        account.notify = false;
                    }

                    if (update)
                        db.account().updateAccount(account);
                    else
                        account.id = db.account().insertAccount(account);
                    EntityLog.log(context, (update ? "Updated" : "Added") + " account=" + account.name);

                    // Make sure the channel exists on commit
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        if (account.notify) {
                            // Add or update notification channel
                            account.deleteNotificationChannel(context);
                            account.createNotificationChannel(context);
                        } else if (!account.synchronize)
                            account.deleteNotificationChannel(context);

                    List<EntityFolder> folders = new ArrayList<>();

                    if (inbox != null)
                        folders.add(inbox);

                    if (drafts != null) {
                        drafts.type = EntityFolder.DRAFTS;
                        folders.add(drafts);
                    }

                    if (sent != null) {
                        sent.type = EntityFolder.SENT;
                        folders.add(sent);
                    }
                    if (all != null) {
                        all.type = EntityFolder.ARCHIVE;
                        folders.add(all);
                    }
                    if (trash != null) {
                        trash.type = EntityFolder.TRASH;
                        folders.add(trash);
                    }
                    if (junk != null) {
                        junk.type = EntityFolder.JUNK;
                        folders.add(junk);
                    }

                    if (left != null) {
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

                    if (right != null) {
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

                    db.folder().setFoldersUser(account.id);

                    for (EntityFolder folder : folders) {
                        if (account.prefix != null && folder.name.startsWith(account.prefix + separator))
                            folder.display = folder.name.substring(account.prefix.length() + 1);

                        EntityFolder existing = db.folder().getFolderByName(account.id, folder.name);
                        if (existing == null) {
                            folder.account = account.id;
                            EntityLog.log(context, "Added folder=" + folder.name + " type=" + folder.type);
                            folder.id = db.folder().insertFolder(folder);
                        } else {
                            EntityLog.log(context, "Updated folder=" + folder.name + " type=" + folder.type);
                            db.folder().setFolderType(existing.id, folder.type);
                        }
                    }

                    account.swipe_left = (left == null ? null : left.id);
                    account.swipe_right = (right == null ? null : right.id);
                    db.account().updateAccount(account);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (reload)
                    ServiceSynchronize.reload(context, "save account");

                if (!synchronize) {
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel("receive", account.id.intValue());
                }

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
        }.execute(FragmentAccount.this, args, "account:save");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("fair:provider", spProvider.getSelectedItemPosition());
        outState.putInt("fair:auth_type", auth_type);
        outState.putString("fair:password", tilPassword.getEditText().getText().toString());
        outState.putInt("fair:advanced", grpAdvanced.getVisibility());
        outState.putInt("fair:color", color);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<EntityAccount>() {
            @Override
            protected EntityAccount onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                return DB.getInstance(context).account().getAccount(id);
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
                    auth_type = (account == null ? Helper.AUTH_TYPE_PASSWORD : account.auth_type);

                    if (account != null) {
                        boolean found = false;
                        for (int pos = 2; pos < providers.size(); pos++) {
                            EmailProvider provider = providers.get(pos);
                            if (provider.imap_host.equals(account.host) &&
                                    provider.imap_port == account.port) {
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

                    rgEncryption.check(account != null && account.starttls ? R.id.radio_starttls : R.id.radio_ssl);
                    cbInsecure.setChecked(account == null ? false : account.insecure);

                    etUser.setTag(account == null || auth_type == Helper.AUTH_TYPE_PASSWORD ? null : account.user);
                    etUser.setText(account == null ? null : account.user);
                    tilPassword.getEditText().setText(account == null ? null : account.password);
                    etRealm.setText(account == null ? null : account.realm);

                    etName.setText(account == null ? null : account.name);
                    etPrefix.setText(account == null ? null : account.prefix);
                    cbNotify.setChecked(account == null ? false : account.notify);

                    cbSynchronize.setChecked(account == null ? true : account.synchronize);
                    cbPrimary.setChecked(account == null ? false : account.primary);
                    cbBrowse.setChecked(account == null ? true : account.browse);
                    etInterval.setText(account == null ? "" : Long.toString(account.poll_interval));

                    color = (account == null || account.color == null ? Color.TRANSPARENT : account.color);

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
                            Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                        }
                    }.execute(FragmentAccount.this, new Bundle(), "account:primary");
                } else {
                    int provider = savedInstanceState.getInt("fair:provider");
                    spProvider.setTag(provider);
                    spProvider.setSelection(provider);

                    auth_type = savedInstanceState.getInt("fair:auth_type");
                    tilPassword.getEditText().setText(savedInstanceState.getString("fair:password"));
                    grpAdvanced.setVisibility(savedInstanceState.getInt("fair:advanced"));
                    color = savedInstanceState.getInt("fair:color");
                }

                Helper.setViewsEnabled(view, true);

                tilPassword.setEnabled(auth_type == Helper.AUTH_TYPE_PASSWORD);
                etRealm.setEnabled(auth_type == Helper.AUTH_TYPE_PASSWORD);

                setColor(color);
                cbPrimary.setEnabled(cbSynchronize.isChecked());

                // Consider previous check/save/delete as cancelled
                pbWait.setVisibility(View.GONE);

                args.putLong("account", account == null ? -1 : account.id);

                new SimpleTask<List<EntityFolder>>() {
                    @Override
                    protected List<EntityFolder> onExecute(Context context, Bundle args) {
                        long account = args.getLong("account");

                        DB db = DB.getInstance(context);
                        List<EntityFolder> folders = db.folder().getFolders(account);

                        if (folders != null) {
                            for (EntityFolder folder : folders)
                                folder.display = folder.getDisplayName(getContext());
                            EntityFolder.sort(getContext(), folders, true);
                        }

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
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(FragmentAccount.this, args, "account:folders");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
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
        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setMessage(R.string.title_account_delete)
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
                                db.account().setAccountTbd(id);

                                ServiceSynchronize.reload(context, "delete account");

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
                        }.execute(FragmentAccount.this, args, "account:delete");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == ActivitySetup.REQUEST_PERMISSION)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectAccount();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == ActivitySetup.REQUEST_CHOOSE_ACCOUNT) {
                String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

                AccountManager am = AccountManager.get(getContext());
                Account[] accounts = am.getAccountsByType(type);
                Log.i("Accounts=" + accounts.length);
                for (final Account account : accounts)
                    if (name.equals(account.name)) {
                        btnAuthorize.setEnabled(false);
                        etUser.setEnabled(false);
                        tilPassword.setEnabled(false);
                        etRealm.setEnabled(false);
                        btnCheck.setEnabled(false);
                        btnSave.setEnabled(false);
                        final Snackbar snackbar = Snackbar.make(view, R.string.title_authorizing, Snackbar.LENGTH_SHORT);
                        snackbar.show();

                        am.getAuthToken(
                                account,
                                Helper.getAuthTokenType(type),
                                new Bundle(),
                                getActivity(),
                                new AccountManagerCallback<Bundle>() {
                                    @Override
                                    public void run(AccountManagerFuture<Bundle> future) {
                                        try {
                                            Bundle bundle = future.getResult();
                                            String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                                            Log.i("Got token");

                                            auth_type = Helper.AUTH_TYPE_GMAIL;
                                            etUser.setTag(account.name);
                                            etUser.setText(account.name);
                                            etUser.setTag(account.name);
                                            tilPassword.getEditText().setText(token);
                                            etRealm.setText(null);
                                        } catch (Throwable ex) {
                                            Log.e(ex);
                                            if (ex instanceof OperationCanceledException ||
                                                    ex instanceof AuthenticatorException ||
                                                    ex instanceof IOException)
                                                Snackbar.make(view, Helper.formatThrowable(ex), Snackbar.LENGTH_LONG).show();
                                            else
                                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                                        } finally {
                                            btnAuthorize.setEnabled(true);
                                            etUser.setEnabled(true);
                                            tilPassword.setEnabled(auth_type == Helper.AUTH_TYPE_PASSWORD);
                                            tilPassword.setPasswordVisibilityToggleEnabled(auth_type == Helper.AUTH_TYPE_PASSWORD);
                                            etRealm.setEnabled(auth_type == Helper.AUTH_TYPE_PASSWORD);
                                            btnCheck.setEnabled(true);
                                            btnSave.setEnabled(true);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    snackbar.dismiss();
                                                }
                                            }, 1000);
                                        }
                                    }
                                },
                                null);
                        break;
                    }
            }
    }

    private void selectAccount() {
        Log.i("Select account");
        EmailProvider provider = (EmailProvider) spProvider.getSelectedItem();
        if (provider.type != null)
            startActivityForResult(
                    Helper.getChooser(getContext(), newChooseAccountIntent(
                            null,
                            null,
                            new String[]{provider.type},
                            false,
                            null,
                            null,
                            null,
                            null)),
                    ActivitySetup.REQUEST_CHOOSE_ACCOUNT);
    }

    private void setColor(int color) {
        FragmentAccount.this.color = color;

        GradientDrawable border = new GradientDrawable();
        border.setColor(color);
        border.setStroke(1, Helper.resolveColor(getContext(), R.attr.colorSeparator));
        vwColor.setBackground(border);
    }

    private void setFolders(List<EntityFolder> folders, EntityAccount account) {
        EntityFolder none = new EntityFolder();
        none.id = -1L;
        none.name = "-";
        folders.add(0, none);

        adapter.clear();
        adapter.addAll(folders);

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

            if (EntityFolder.DRAFTS.equals(folder.type))
                spDrafts.setSelection(pos);
            else if (EntityFolder.SENT.equals(folder.type))
                spSent.setSelection(pos);
            else if (EntityFolder.ARCHIVE.equals(folder.type))
                spAll.setSelection(pos);
            else if (EntityFolder.TRASH.equals(folder.type))
                spTrash.setSelection(pos);
            else if (EntityFolder.JUNK.equals(folder.type))
                spJunk.setSelection(pos);

            if (left == null ? (account == null && leftDefault.equals(folder.type)) : left.equals(folder.id))
                spLeft.setSelection(pos);

            if (right == null ? (account == null && rightDefault.equals(folder.type)) : right.equals(folder.id))
                spRight.setSelection(pos);
        }

        grpFolders.setVisibility(folders.size() > 1 ? View.VISIBLE : View.GONE);
        btnSave.setVisibility(folders.size() > 1 ? View.VISIBLE : View.GONE);
    }

    private class CheckResult {
        EntityAccount account;
        List<EntityFolder> folders;
        boolean idle;
        boolean move;
        Boolean utf8;
    }
}
