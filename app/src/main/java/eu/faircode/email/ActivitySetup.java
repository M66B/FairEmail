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

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_GMAIL;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class ActivitySetup extends ActivityBase implements FragmentManager.OnBackStackChangedListener {
    private View view;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ConstraintLayout drawerContainer;
    private RecyclerView rvMenu;

    private boolean hasAccount;
    private String password;
    private boolean import_accounts;
    private boolean import_delete;
    private boolean import_rules;
    private boolean import_contacts;
    private boolean import_answers;
    private boolean import_settings;

    static final int REQUEST_SOUND_INBOUND = 1;
    static final int REQUEST_SOUND_OUTBOUND = 2;
    static final int REQUEST_EXPORT = 3;
    static final int REQUEST_IMPORT = 4;
    static final int REQUEST_CHOOSE_ACCOUNT = 5;
    static final int REQUEST_DONE = 6;
    static final int REQUEST_IMPORT_CERTIFICATE = 7;
    static final int REQUEST_OAUTH = 8;
    static final int REQUEST_STILL = 9;
    static final int REQUEST_SELECT_IDENTITY = 10;
    static final int REQUEST_EDIT_SIGNATURE = 11;
    static final int REQUEST_DELETE_ACCOUNT = 12;
    static final int REQUEST_IMPORT_PROVIDERS = 13;

    static final int PI_MISC = 1;

    static final String ACTION_QUICK_GMAIL = BuildConfig.APPLICATION_ID + ".ACTION_QUICK_GMAIL";
    static final String ACTION_QUICK_OAUTH = BuildConfig.APPLICATION_ID + ".ACTION_QUICK_OAUTH";
    static final String ACTION_QUICK_SETUP = BuildConfig.APPLICATION_ID + ".ACTION_QUICK_SETUP";
    static final String ACTION_QUICK_POP3 = BuildConfig.APPLICATION_ID + ".ACTION_QUICK_POP3";
    static final String ACTION_VIEW_ACCOUNTS = BuildConfig.APPLICATION_ID + ".ACTION_VIEW_ACCOUNTS";
    static final String ACTION_VIEW_IDENTITIES = BuildConfig.APPLICATION_ID + ".ACTION_VIEW_IDENTITIES";
    static final String ACTION_EDIT_ACCOUNT = BuildConfig.APPLICATION_ID + ".EDIT_ACCOUNT";
    static final String ACTION_EDIT_IDENTITY = BuildConfig.APPLICATION_ID + ".EDIT_IDENTITY";
    static final String ACTION_MANAGE_LOCAL_CONTACTS = BuildConfig.APPLICATION_ID + ".MANAGE_LOCAL_CONTACTS";
    static final String ACTION_MANAGE_CERTIFICATES = BuildConfig.APPLICATION_ID + ".MANAGE_CERTIFICATES";
    static final String ACTION_IMPORT_CERTIFICATE = BuildConfig.APPLICATION_ID + ".IMPORT_CERTIFICATE";
    static final String ACTION_SETUP_REORDER = BuildConfig.APPLICATION_ID + ".SETUP_REORDER";
    static final String ACTION_SETUP_MORE = BuildConfig.APPLICATION_ID + ".SETUP_MORE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = LayoutInflater.from(this).inflate(R.layout.activity_setup, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Helper.resolveColor(this, R.attr.colorDrawerScrim));

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        drawerContainer = findViewById(R.id.drawer_container);
        rvMenu = drawerContainer.findViewById(R.id.rvMenu);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvMenu.setLayoutManager(llm);
        final AdapterNavMenu adapter = new AdapterNavMenu(this, this);
        rvMenu.setAdapter(adapter);

        final Drawable d = getDrawable(R.drawable.divider);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, llm.getOrientation()) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(view);
                NavMenuItem menu = (adapter == null ? null : adapter.get(pos));
                outRect.set(0, 0, 0, menu != null && menu.isSeparated() ? d.getIntrinsicHeight() : 0);
            }
        };
        itemDecorator.setDrawable(d);
        rvMenu.addItemDecoration(itemDecorator);

        final List<NavMenuItem> menus = new ArrayList<>();

        int colorWarning = Helper.resolveColor(this, R.attr.colorWarning);
        menus.add(new NavMenuItem(R.drawable.twotone_close_24, R.string.title_setup_close, new Runnable() {
            @Override
            public void run() {
                onMenuClose();
            }
        }).setColor(colorWarning).setSeparated());

        menus.add(new NavMenuItem(R.drawable.twotone_archive_24, R.string.title_setup_export, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuExport();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_unarchive_24, R.string.title_setup_import, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuImport();
            }
        }).setSeparated());

        menus.add(new NavMenuItem(R.drawable.twotone_reorder_24, R.string.title_setup_reorder_accounts, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuOrder(R.string.title_setup_reorder_accounts, EntityAccount.class.getName());
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_reorder_24, R.string.title_setup_reorder_folders, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuOrder(R.string.title_setup_reorder_folders, TupleFolderSort.class.getName());
            }
        }).setSeparated());

        menus.add(new NavMenuItem(R.drawable.twotone_list_alt_24, R.string.title_log, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuLog();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_help_24, R.string.menu_legend, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuLegend();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_support_24, R.string.menu_faq, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuFAQ();
            }
        }, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                drawerLayout.closeDrawer(drawerContainer);
                onDebugInfo();
                return true;
            }
        }).setExternal(true));

        menus.add(new NavMenuItem(R.drawable.twotone_feedback_24, R.string.menu_issue, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuIssue();
            }
        }).setExternal(true));

        menus.add(new NavMenuItem(R.drawable.twotone_account_circle_24, R.string.menu_privacy, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuPrivacy();
            }
        }).setExternal(true));

        menus.add(new NavMenuItem(R.drawable.twotone_info_24, R.string.menu_about, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuAbout();
            }
        }, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                startActivity(new Intent(ActivitySetup.this, ActivityBilling.class));
                return true;
            }
        }).setSubtitle(BuildConfig.VERSION_NAME));

        adapter.set(menus, true);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Helper.isKeyboardVisible(view))
                    Helper.hideKeyboard(view);
                else
                    onExit();
            }
        });

        if (getSupportFragmentManager().getFragments().size() == 0) {
            Intent intent = getIntent();
            String target = intent.getStringExtra("target");
            long id = intent.getLongExtra("id", -1L);

            if ("accounts".equals(target) && id > 0)
                onEditAccount(intent);
            else if ("identities".equals(target) && id > 0)
                onEditIdentity(intent);
            else {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                if ("accounts".equals(target))
                    fragmentTransaction.replace(R.id.content_frame, new FragmentAccounts()).addToBackStack("accounts");
                else
                    fragmentTransaction.replace(R.id.content_frame, new FragmentOptions()).addToBackStack("options");
                fragmentTransaction.commit();
            }

            if (intent.hasExtra("target")) {
                intent.removeExtra("target");
                setIntent(intent);
            }
        }

        if (savedInstanceState != null) {
            drawerToggle.setDrawerIndicatorEnabled(savedInstanceState.getBoolean("fair:toggle"));
            password = savedInstanceState.getString("fair:password");
            import_accounts = savedInstanceState.getBoolean("fair:import_accounts");
            import_delete = savedInstanceState.getBoolean("fair:import_delete");
            import_rules = savedInstanceState.getBoolean("fair:import_rules");
            import_contacts = savedInstanceState.getBoolean("fair:import_contacts");
            import_answers = savedInstanceState.getBoolean("fair:import_answers");
            import_settings = savedInstanceState.getBoolean("fair:import_settings");
        }

        DB db = DB.getInstance(this);

        db.account().liveSynchronizingAccounts().observe(this, new Observer<List<EntityAccount>>() {
            @Override
            public void onChanged(List<EntityAccount> accounts) {
                hasAccount = (accounts != null && accounts.size() > 0);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:toggle", drawerToggle == null || drawerToggle.isDrawerIndicatorEnabled());
        outState.putString("fair:password", password);
        outState.putBoolean("fair:import_accounts", import_accounts);
        outState.putBoolean("fair:import_delete", import_delete);
        outState.putBoolean("fair:import_rules", import_rules);
        outState.putBoolean("fair:import_contacts", import_contacts);
        outState.putBoolean("fair:import_answers", import_answers);
        outState.putBoolean("fair:import_settings", import_settings);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();

        Intent intent = getIntent();
        boolean navigate = intent.hasExtra("navigate");
        if (navigate) {
            intent.removeExtra("navigate");
            setIntent(intent);
            onSetupMore(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_QUICK_GMAIL);
        iff.addAction(ACTION_QUICK_OAUTH);
        iff.addAction(ACTION_QUICK_SETUP);
        iff.addAction(ACTION_QUICK_POP3);
        iff.addAction(ACTION_VIEW_ACCOUNTS);
        iff.addAction(ACTION_VIEW_IDENTITIES);
        iff.addAction(ACTION_EDIT_ACCOUNT);
        iff.addAction(ACTION_EDIT_IDENTITY);
        iff.addAction(ACTION_MANAGE_LOCAL_CONTACTS);
        iff.addAction(ACTION_MANAGE_CERTIFICATES);
        iff.addAction(ACTION_IMPORT_CERTIFICATE);
        iff.addAction(ACTION_SETUP_REORDER);
        iff.addAction(ACTION_SETUP_MORE);
        lbm.registerReceiver(receiver, iff);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public void onExit() {
        if (drawerLayout.isDrawerOpen(drawerContainer))
            drawerLayout.closeDrawer(drawerContainer);
        else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                performBack();
                return;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean setup_reminder = prefs.getBoolean("setup_reminder", true);

            boolean hasContactPermissions =
                    hasPermission(android.Manifest.permission.READ_CONTACTS);
            boolean hasNotificationPermissions =
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                            hasPermission(Manifest.permission.POST_NOTIFICATIONS));
            boolean isIgnoring = !Boolean.FALSE.equals(Helper.isIgnoringOptimizations(this));

            if (!setup_reminder ||
                    (hasContactPermissions && hasNotificationPermissions && isIgnoring))
                performBack();
            else {
                FragmentDialogPermissions fragment = new FragmentDialogPermissions();
                fragment.setTargetActivity(this, REQUEST_STILL);
                fragment.show(getSupportFragmentManager(), "setup:still");
            }
        }
    }

    @Override
    public void onBackStackChanged() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (hasAccount) {
                startActivity(new Intent(this, ActivityView.class));
                finishAndRemoveTask();
            } else
                finish();
        } else {
            if (drawerLayout.isDrawerOpen(drawerContainer))
                drawerLayout.closeDrawer(drawerContainer);
            drawerToggle.setDrawerIndicatorEnabled(count == 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;

        int itemId = item.getItemId();
        if (itemId == R.id.menu_close)
            onMenuClose();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_EXPORT:
                    if (resultCode == RESULT_OK && data != null)
                        handleExport(data);
                    break;
                case REQUEST_IMPORT:
                    if (resultCode == RESULT_OK && data != null)
                        handleImport(data);
                    break;
                case REQUEST_IMPORT_CERTIFICATE:
                    if (resultCode == RESULT_OK && data != null)
                        handleImportCertificate(data);
                    break;
                case REQUEST_IMPORT_PROVIDERS:
                    if (resultCode == RESULT_OK && data != null)
                        handleImportProviders(data);
                    break;
                case ActivitySetup.REQUEST_STILL:
                    if (resultCode == Activity.RESULT_OK) {
                        Bundle result = new Bundle();
                        result.putInt("page", 0);
                        getSupportFragmentManager().setFragmentResult("options:tab", result);
                    } else
                        performBack();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onMenuClose() {
        drawerLayout.closeDrawer(drawerContainer, false);
        onExit();
    }

    private void onMenuExport() {
        if (ActivityBilling.isPro(this))
            askPassword(true);
        else
            startActivity(new Intent(this, ActivityBilling.class));
    }

    private void onMenuImport() {
        askPassword(false);
    }

    private void askPassword(final boolean export) {
        Intent intent = (export ? getIntentExport() : getIntentImport());
        if (intent.resolveActivity(getPackageManager()) == null) { //  // system/GET_CONTENT whitelisted
            ToastEx.makeText(this, R.string.title_no_saf, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            FragmentDialogBase fragment =
                    (export ? new FragmentDialogExport() : new FragmentDialogImport());
            fragment.show(getSupportFragmentManager(), "password");
        } catch (Throwable ex) {
            Log.unexpectedError(getSupportFragmentManager(), ex);
        }
    }

    private void onMenuOrder(int title, String className) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("order", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putString("class", className);

        FragmentOrder fragment = new FragmentOrder();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("order");
        fragmentTransaction.commit();
    }

    private void onMenuLog() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("logs", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLogs()).addToBackStack("logs");
        fragmentTransaction.commit();
    }

    private void onMenuLegend() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("legend", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLegend()).addToBackStack("legend");
        fragmentTransaction.commit();
    }

    private void onMenuFAQ() {
        Helper.viewFAQ(this, 0);
    }

    private void onDebugInfo() {
        new SimpleTask<Long>() {
            @Override
            protected Long onExecute(Context context, Bundle args) throws IOException, JSONException {
                return Log.getDebugInfo(context, R.string.title_debug_info_remark, null, null).id;
            }

            @Override
            protected void onExecuted(Bundle args, Long id) {
                startActivity(new Intent(ActivitySetup.this, ActivityCompose.class)
                        .putExtra("action", "edit")
                        .putExtra("id", id));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    ToastEx.makeText(ActivitySetup.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getSupportFragmentManager(), ex);
            }

        }.execute(this, new Bundle(), "debug:info");
    }

    private void onMenuIssue() {
        startActivity(Helper.getIntentIssue(this, "Setup:issue"));
    }

    private void onMenuPrivacy() {
        Helper.view(this, Helper.getPrivacyUri(this), false);
    }

    private void onMenuAbout() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("about", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAbout()).addToBackStack("about");
        fragmentTransaction.commit();
    }

    private void handleExport(Intent data) {
        Bundle args = new Bundle();
        args.putParcelable("uri", data.getData());
        args.putString("password", this.password);

        new SimpleTask<Void>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(ActivitySetup.this, R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                if (uri == null)
                    throw new FileNotFoundException();

                String password = args.getString("password");
                EntityLog.log(context, "Exporting " + uri);

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Export uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                Log.i("Collecting data");
                DB db = DB.getInstance(context);
                NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);

                // Accounts
                JSONArray jaccounts = new JSONArray();
                for (EntityAccount account : db.account().getAccounts()) {
                    // Account
                    JSONObject jaccount = account.toJSON();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (account.notify) {
                            NotificationChannel channel = nm.getNotificationChannel(
                                    EntityAccount.getNotificationChannelId(account.id));
                            if (channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE) {
                                JSONObject jchannel = NotificationHelper.channelToJSON(channel);
                                jaccount.put("channel", jchannel);
                                Log.i("Exported account channel=" + jchannel);
                            }
                        }
                    }

                    // Identities
                    JSONArray jidentities = new JSONArray();
                    for (EntityIdentity identity : db.identity().getIdentities(account.id))
                        jidentities.put(identity.toJSON());
                    jaccount.put("identities", jidentities);

                    // Folders
                    JSONArray jfolders = new JSONArray();
                    for (EntityFolder folder : db.folder().getFolders(account.id, false, true)) {
                        JSONObject jfolder = folder.toJSON();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = nm.getNotificationChannel(
                                    EntityFolder.getNotificationChannelId(folder.id));
                            if (channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE) {
                                JSONObject jchannel = NotificationHelper.channelToJSON(channel);
                                jfolder.put("channel", jchannel);
                                Log.i("Exported folder channel=" + jchannel);
                            }
                        }

                        JSONArray jrules = new JSONArray();
                        for (EntityRule rule : db.rule().getRules(folder.id)) {
                            try {
                                JSONObject jaction = new JSONObject(rule.action);
                                int type = jaction.getInt("type");
                                switch (type) {
                                    case EntityRule.TYPE_MOVE:
                                    case EntityRule.TYPE_COPY:
                                        long target = jaction.optLong("target", -1L);
                                        EntityFolder f = db.folder().getFolder(target);
                                        EntityAccount a = (f == null ? null : db.account().getAccount(f.account));
                                        if (a != null)
                                            jaction.put("targetAccountUuid", a.uuid);
                                        if (f != null)
                                            jaction.put("targetFolderName", f.name);
                                        break;
                                    case EntityRule.TYPE_ANSWER:
                                        long identity = jaction.optLong("identity", -1L);
                                        long answer = jaction.optLong("answer", -1L);
                                        EntityIdentity i = db.identity().getIdentity(identity);
                                        EntityAnswer t = db.answer().getAnswer(answer);
                                        if (i != null)
                                            jaction.put("identityUuid", i.uuid);
                                        if (t != null)
                                            jaction.put("answerUuid", t.uuid);
                                        break;
                                }
                                rule.action = jaction.toString();
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }

                            jrules.put(rule.toJSON());
                        }
                        jfolder.put("rules", jrules);

                        jfolders.put(jfolder);
                    }
                    jaccount.put("folders", jfolders);

                    // Contacts
                    JSONArray jcontacts = new JSONArray();
                    for (EntityContact contact : db.contact().getContacts(account.id))
                        jcontacts.put(contact.toJSON());
                    jaccount.put("contacts", jcontacts);

                    jaccounts.put(jaccount);
                }

                // Answers
                JSONArray janswers = new JSONArray();
                for (EntityAnswer answer : db.answer().getAnswers(true))
                    janswers.put(answer.toJSON());

                // Certificates
                JSONArray jcertificates = new JSONArray();
                for (EntityCertificate certificate : db.certificate().getCertificates())
                    jcertificates.put(certificate.toJSON());

                // Settings
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                JSONArray jsettings = new JSONArray();
                for (String key : prefs.getAll().keySet()) {
                    JSONObject jsetting = new JSONObject();
                    Object value = prefs.getAll().get(key);
                    jsetting.put("key", key);
                    jsetting.put("value", value);
                    if (value instanceof Boolean)
                        jsetting.put("type", "bool");
                    else if (value instanceof Integer)
                        jsetting.put("type", "int");
                    else if (value instanceof Long)
                        jsetting.put("type", "long");
                    else if (value instanceof String)
                        jsetting.put("type", "string");
                    else if (value != null) {
                        String type = value.getClass().getName();
                        Log.w("Unknown type=" + type);
                        jsetting.put("type", type);
                    }
                    jsettings.put(jsetting);
                }

                JSONObject jsearch = new JSONObject();
                jsearch.put("key", "external_search");
                jsearch.put("value", Helper.isComponentEnabled(context, ActivitySearch.class));
                jsearch.put("type", "bool");
                jsettings.put(jsearch);

                JSONObject jexport = new JSONObject();
                jexport.put("accounts", jaccounts);
                jexport.put("answers", janswers);
                jexport.put("certificates", jcertificates);
                jexport.put("settings", jsettings);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    JSONArray jchannels = new JSONArray();
                    for (NotificationChannel channel : nm.getNotificationChannels()) {
                        String id = channel.getId();
                        if (id.startsWith("notification.") && id.contains("@") &&
                                channel.getImportance() != NotificationManager.IMPORTANCE_NONE) {
                            JSONObject jchannel = NotificationHelper.channelToJSON(channel);
                            jchannels.put(jchannel);
                            Log.i("Exported contact channel=" + jchannel);
                        }
                    }
                    jexport.put("channels", jchannels);
                }

                ContentResolver resolver = context.getContentResolver();
                DocumentFile file = DocumentFile.fromSingleUri(context, uri);
                try (OutputStream raw = resolver.openOutputStream(uri)) {
                    Log.i("Writing URI=" + uri + " name=" + file.getName() + " virtual=" + file.isVirtual());
                    if (raw == null)
                        throw new FileNotFoundException(uri.toString());

                    if (TextUtils.isEmpty(password))
                        raw.write(jexport.toString(2).getBytes());
                    else {
                        byte[] salt = new byte[16];
                        SecureRandom random = new SecureRandom();
                        random.nextBytes(salt);

                        // https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Cipher
                        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
                        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 120000, 256);
                        SecretKey secret = keyFactory.generateSecret(keySpec);
                        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                        cipher.init(Cipher.ENCRYPT_MODE, secret);

                        raw.write("___FairEmail___".getBytes(StandardCharsets.US_ASCII));
                        raw.write(1); // version
                        raw.write(salt);
                        raw.write(cipher.getIV());

                        OutputStream cout = new CipherOutputStream(raw, cipher);
                        cout.write(jexport.toString(2).getBytes());
                        cout.flush();
                        raw.write(cipher.doFinal());
                    }
                }

                Log.i("Exported data");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(ActivitySetup.this, R.string.title_setup_exported, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onDestroyed(Bundle args) {
                if (toast != null) {
                    toast.cancel();
                    toast = null;
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                boolean expected =
                        (ex instanceof IllegalArgumentException ||
                                ex instanceof FileNotFoundException ||
                                ex instanceof SecurityException);
                Log.unexpectedError(getSupportFragmentManager(), ex, !expected);
            }
        }.execute(this, args, "setup:export");
    }

    private void handleImport(Intent data) {
        Uri uri = data.getData();

        if (uri != null)
            try {
                DocumentFile df = DocumentFile.fromSingleUri(this, uri);
                if (df != null) {
                    String name = df.getName();
                    String ext = Helper.getExtension(name);
                    if ("k9s".equals(ext)) {
                        handleK9Import(uri);
                        return;
                    }
                }
            } catch (Throwable ex) {
                Log.w(ex);
            }

        final int colorWarning = Helper.resolveColor(this, R.attr.colorWarning);

        View dview = LayoutInflater.from(this).inflate(R.layout.dialog_import_progress, null);
        TextView tvLog = dview.findViewById(R.id.tvLog);
        tvLog.setText(null);

        Map<String, Object> defer = Collections.synchronizedMap(new HashMap<>());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(dview.getContext());
                        SharedPreferences.Editor editor = prefs.edit();

                        for (String key : defer.keySet()) {
                            Object value = defer.get(key);
                            if (value instanceof Boolean)
                                editor.putBoolean(key, (Boolean) value);
                            else if (value instanceof String)
                                editor.putString(key, (String) value);
                        }

                        editor.apply();
                    }
                })
                .show();

        Button ok = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        ok.setEnabled(false);

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        args.putString("password", this.password);
        args.putBoolean("import_accounts", this.import_accounts);
        args.putBoolean("import_delete", this.import_delete);
        args.putBoolean("import_rules", this.import_rules);
        args.putBoolean("import_contacts", this.import_contacts);
        args.putBoolean("import_answers", this.import_answers);
        args.putBoolean("import_settings", this.import_settings);

        new SimpleTask<Void>() {
            private SpannableStringBuilder ssb = new SpannableStringBuilder();

            @Override
            protected void onProgress(CharSequence status, Bundle data) {
                ssb.append(status).append("\n");
                tvLog.setText(ssb);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                ok.setEnabled(true);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");
                String password = args.getString("password");
                boolean import_accounts = args.getBoolean("import_accounts");
                boolean import_delete = args.getBoolean("import_delete");
                boolean import_rules = args.getBoolean("import_rules");
                boolean import_contacts = args.getBoolean("import_contacts");
                boolean import_answers = args.getBoolean("import_answers");
                boolean import_settings = args.getBoolean("import_settings");
                EntityLog.log(context, "Importing " + uri +
                        " accounts=" + import_accounts +
                        " delete=" + import_delete +
                        " rules=" + import_rules +
                        " contacts=" + import_contacts +
                        " answers=" + import_answers +
                        " settings=" + import_settings);

                NoStreamException.check(uri, context);

                StringBuilder data = new StringBuilder();
                Log.i("Reading URI=" + uri);
                ContentResolver resolver = context.getContentResolver();
                InputStream is = resolver.openInputStream(uri);
                if (is == null)
                    throw new FileNotFoundException(uri.toString());
                try (InputStream raw = new BufferedInputStream(is)) {
                    InputStream in;
                    if (TextUtils.isEmpty(password))
                        in = raw;
                    else {
                        byte[] salt = new byte[16];
                        Helper.readBuffer(raw, salt);

                        int version = 0;
                        String magic = new String(salt, 0, 15, StandardCharsets.US_ASCII);
                        if ("___FairEmail___".equals(magic)) {
                            version = salt[15];
                            Helper.readBuffer(raw, salt);
                        }

                        int ivLen = (version == 0 ? 16 : 12);
                        String derivation = (version == 0 ? "PBKDF2WithHmacSHA1" : "PBKDF2WithHmacSHA512");
                        int iterations = (version == 0 ? 65536 : 120000);
                        int keyLen = 256;
                        String transformation = (version == 0 ? "AES/CBC/PKCS5Padding" : "AES/GCM/NoPadding");
                        Log.i("Import version=" + version +
                                " ivLen=" + ivLen +
                                " derivation=" + derivation +
                                " iterations=" + iterations +
                                " keyLen=" + keyLen +
                                " transformation=" + transformation);

                        byte[] iv = new byte[ivLen];
                        Helper.readBuffer(raw, iv);

                        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(derivation);
                        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
                        SecretKey secret = keyFactory.generateSecret(keySpec);
                        Cipher cipher = Cipher.getInstance(transformation);
                        IvParameterSpec ivSpec = new IvParameterSpec(iv);
                        cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);

                        in = new CipherInputStream(raw, cipher);
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null)
                        data.append(line);
                }

                String json = data.toString();
                if (!json.startsWith("{") || !json.endsWith("}")) {
                    Log.i("Invalid JSON");
                    throw new IllegalArgumentException(context.getString(R.string.title_setup_password_invalid));
                }

                Log.i("Importing data");
                JSONObject jimport = new JSONObject(json);

                DB db = DB.getInstance(context);
                NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                try {
                    db.beginTransaction();

                    Map<Long, Long> xAnswer = new HashMap<>();
                    Map<Long, Long> xIdentity = new HashMap<>();
                    Map<Long, Long> xFolder = new HashMap<>();
                    List<EntityRule> rules = new ArrayList<>();

                    if (import_answers) {
                        postProgress(context.getString(R.string.title_setup_import_answers), null);

                        // Answers
                        JSONArray janswers = jimport.getJSONArray("answers");
                        for (int a = 0; a < janswers.length(); a++) {
                            JSONObject janswer = (JSONObject) janswers.get(a);
                            EntityAnswer answer = EntityAnswer.fromJSON(janswer);
                            long id = answer.id;
                            answer.id = null;

                            EntityAnswer existing = db.answer().getAnswerByUUID(answer.uuid);
                            if (existing != null)
                                db.answer().deleteAnswer(existing.id);

                            answer.id = db.answer().insertAnswer(answer);
                            xAnswer.put(id, answer.id);

                            Log.i("Imported answer=" + answer.name + " id=" + answer.id + " (" + id + ")");
                        }
                    }

                    if (import_accounts) {
                        EntityAccount primary = db.account().getPrimaryAccount();

                        // Accounts
                        JSONArray jaccounts = jimport.getJSONArray("accounts");
                        for (int a = 0; a < jaccounts.length(); a++) {
                            JSONObject jaccount = (JSONObject) jaccounts.get(a);
                            EntityAccount account = EntityAccount.fromJSON(jaccount);
                            postProgress(context.getString(R.string.title_importing_account, account.name));

                            if (import_delete) {
                                EntityAccount delete = db.account().getAccount(account.auth_type, account.user);
                                if (delete != null)
                                    db.account().deleteAccount(delete.id);
                            }

                            EntityAccount existing = db.account().getAccountByUUID(account.uuid);
                            if (existing != null) {
                                SpannableStringBuilder ssb = new SpannableStringBuilder();
                                ssb.append(context.getString(R.string.title_importing_exists));
                                ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), 0);
                                postProgress(ssb);
                                EntityLog.log(context, "Existing account=" + account.name +
                                        "id=" + account.id);
                                continue;
                            }

                            if (account.auth_type == AUTH_TYPE_GMAIL &&
                                    GmailState.getAccount(context, account.user) == null) {
                                SpannableStringBuilder ssb = new SpannableStringBuilder();
                                ssb.append(account.name).append(": ");
                                ssb.append(context.getString(R.string.title_importing_wizard));
                                ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), 0);
                                ssb.setSpan(new ForegroundColorSpan(colorWarning), 0, ssb.length(), 0);
                                postProgress(ssb);
                                EntityLog.log(context, "Run wizard account=" + account.name +
                                        "id=" + account.id);
                                account.synchronize = false;
                            }

                            Long aid = account.id;
                            account.id = null;

                            if (primary != null)
                                account.primary = false;

                            // Forward referenced
                            Long swipe_left = account.swipe_left;
                            Long swipe_right = account.swipe_right;
                            Long move_to = account.move_to;
                            if (account.swipe_left != null && account.swipe_left > 0)
                                account.swipe_left = null;
                            if (account.swipe_right != null && account.swipe_right > 0)
                                account.swipe_right = null;
                            account.move_to = null;

                            account.created = new Date().getTime();
                            account.id = db.account().insertAccount(account);
                            EntityLog.log(context, "Imported account=" + account.name +
                                    " id=" + account.id + " (" + aid + ")");

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                account.deleteNotificationChannel(context);

                                if (account.notify)
                                    if (jaccount.has("channel"))
                                        try {
                                            NotificationChannelGroup group = new NotificationChannelGroup("group." + account.id, account.name);
                                            nm.createNotificationChannelGroup(group);

                                            JSONObject jchannel = (JSONObject) jaccount.get("channel");
                                            jchannel.put("id", EntityAccount.getNotificationChannelId(account.id));
                                            jchannel.put("group", group.getId());
                                            nm.createNotificationChannel(NotificationHelper.channelFromJSON(context, jchannel));

                                            Log.i("Imported account channel=" + jchannel);
                                        } catch (Throwable ex) {
                                            Log.e(ex);
                                            account.createNotificationChannel(context);
                                        }
                                    else
                                        account.createNotificationChannel(context);
                            }

                            JSONArray jidentities = (JSONArray) jaccount.get("identities");
                            for (int i = 0; i < jidentities.length(); i++) {
                                JSONObject jidentity = (JSONObject) jidentities.get(i);
                                EntityIdentity identity = EntityIdentity.fromJSON(jidentity);
                                postProgress(context.getString(R.string.title_importing_identity, identity.email));

                                long id = identity.id;
                                identity.id = null;

                                identity.account = account.id;
                                identity.id = db.identity().insertIdentity(identity);
                                xIdentity.put(id, identity.id);

                                Log.i("Imported identity=" + identity.email + " id=" + identity + id + " (" + id + ")");
                            }

                            JSONArray jfolders = (JSONArray) jaccount.get("folders");
                            for (int f = 0; f < jfolders.length(); f++) {
                                JSONObject jfolder = (JSONObject) jfolders.get(f);
                                EntityFolder folder = EntityFolder.fromJSON(jfolder);
                                long id = folder.id;
                                folder.id = null;

                                folder.account = account.id;
                                folder.id = db.folder().insertFolder(folder);
                                xFolder.put(id, folder.id);

                                if (Objects.equals(swipe_left, id))
                                    account.swipe_left = folder.id;
                                if (Objects.equals(swipe_right, id))
                                    account.swipe_right = folder.id;
                                if (Objects.equals(move_to, id))
                                    account.move_to = folder.id;

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    String channelId = EntityFolder.getNotificationChannelId(folder.id);
                                    nm.deleteNotificationChannel(channelId);

                                    if (jfolder.has("channel"))
                                        try {
                                            NotificationChannelGroup group = new NotificationChannelGroup("group." + account.id, account.name);
                                            nm.createNotificationChannelGroup(group);

                                            JSONObject jchannel = (JSONObject) jfolder.get("channel");
                                            jchannel.put("id", channelId);
                                            jchannel.put("group", group.getId());
                                            nm.createNotificationChannel(NotificationHelper.channelFromJSON(context, jchannel));

                                            Log.i("Imported folder channel=" + jchannel);
                                        } catch (Throwable ex) {
                                            Log.e(ex);
                                        }
                                }

                                if (jfolder.has("rules")) {
                                    JSONArray jrules = jfolder.getJSONArray("rules");
                                    for (int r = 0; r < jrules.length(); r++) {
                                        JSONObject jrule = (JSONObject) jrules.get(r);
                                        EntityRule rule = EntityRule.fromJSON(jrule);
                                        rule.folder = folder.id;
                                        rules.add(rule);
                                    }
                                }
                                Log.i("Imported folder=" + folder.name + " id=" + folder.id + " (" + id + ")");
                            }

                            if (import_contacts) {
                                // Contacts
                                if (jaccount.has("contacts")) {
                                    postProgress(context.getString(R.string.title_setup_import_contacts), null);

                                    JSONArray jcontacts = jaccount.getJSONArray("contacts");
                                    for (int c = 0; c < jcontacts.length(); c++) {
                                        JSONObject jcontact = (JSONObject) jcontacts.get(c);
                                        EntityContact contact = EntityContact.fromJSON(jcontact);
                                        contact.account = account.id;
                                        contact.identity = xIdentity.get(contact.identity);
                                        if (db.contact().getContact(contact.account, contact.type, contact.email) == null)
                                            contact.id = db.contact().insertContact(contact);
                                    }
                                    Log.i("Imported contacts=" + jcontacts.length());
                                }
                            }

                            // Update swipe left/right
                            db.account().updateAccount(account);
                        }

                        if (import_rules) {
                            postProgress(context.getString(R.string.title_setup_import_rules), null);
                            for (EntityRule rule : rules) {
                                try {
                                    JSONObject jaction = new JSONObject(rule.action);

                                    int type = jaction.getInt("type");
                                    switch (type) {
                                        case EntityRule.TYPE_MOVE:
                                        case EntityRule.TYPE_COPY:
                                            String targetAccountUuid = jaction.optString("targetAccountUuid");
                                            String targetFolderName = jaction.optString("targetFolderName");
                                            if (!TextUtils.isEmpty(targetAccountUuid) && !TextUtils.isEmpty(targetFolderName)) {
                                                EntityAccount a = db.account().getAccountByUUID(targetAccountUuid);
                                                if (a != null) {
                                                    EntityFolder f = db.folder().getFolderByName(a.id, targetFolderName);
                                                    if (f != null) {
                                                        jaction.put("target", f.id);
                                                        break;
                                                    }
                                                }
                                            }

                                            // Legacy
                                            long target = jaction.getLong("target");
                                            Long tid = xFolder.get(target);
                                            Log.i("XLAT target " + target + " > " + tid);
                                            if (tid != null)
                                                jaction.put("target", tid);
                                            break;
                                        case EntityRule.TYPE_ANSWER:
                                            String identityUuid = jaction.optString("identityUuid");
                                            String answerUuid = jaction.optString("answerUuid");
                                            if (!TextUtils.isEmpty(identityUuid) && !TextUtils.isEmpty(answerUuid)) {
                                                EntityIdentity i = db.identity().getIdentityByUUID(identityUuid);
                                                EntityAnswer a = db.answer().getAnswerByUUID(answerUuid);
                                                if (i != null && a != null) {
                                                    jaction.put("identity", i.id);
                                                    jaction.put("answer", a.id);
                                                    break;
                                                }
                                            }

                                            // Legacy
                                            long identity = jaction.getLong("identity");
                                            long answer = jaction.getLong("answer");
                                            Long iid = xIdentity.get(identity);
                                            Long aid = xAnswer.get(answer);
                                            Log.i("XLAT identity " + identity + " > " + iid);
                                            Log.i("XLAT answer " + answer + " > " + aid);
                                            jaction.put("identity", iid);
                                            jaction.put("answer", aid);
                                            break;
                                    }

                                    rule.action = jaction.toString();
                                } catch (JSONException ex) {
                                    Log.e(ex);
                                }

                                db.rule().insertRule(rule);
                            }
                        }
                    }

                    if (import_settings) {
                        postProgress(context.getString(R.string.title_setup_import_settings), null);

                        // Certificates
                        if (jimport.has("certificates")) {
                            JSONArray jcertificates = jimport.getJSONArray("certificates");
                            for (int c = 0; c < jcertificates.length(); c++) {
                                JSONObject jcertificate = (JSONObject) jcertificates.get(c);
                                EntityCertificate certificate = EntityCertificate.fromJSON(jcertificate);
                                EntityCertificate record = db.certificate().getCertificate(certificate.fingerprint, certificate.email);
                                if (record == null) {
                                    db.certificate().insertCertificate(certificate);
                                    Log.i("Imported certificate=" + certificate.email);
                                }
                            }
                        }

                        // Settings
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        JSONArray jsettings = jimport.getJSONArray("settings");
                        for (int s = 0; s < jsettings.length(); s++) {
                            JSONObject jsetting = (JSONObject) jsettings.get(s);
                            String key = jsetting.getString("key");

                            if ("pro".equals(key) && !BuildConfig.DEBUG)
                                continue;

                            if ("accept_unsupported".equals(key))
                                continue;

                            if ("biometrics".equals(key) || "pin".equals(key))
                                continue;

                            if ("alert_once".equals(key) && !Helper.isXiaomi())
                                continue;

                            if ("background_service".equals(key) &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                continue;

                            if ("query_threads".equals(key))
                                continue;

                            // Prevent restart
                            if ("secure".equals(key) ||
                                    "load_emoji".equals(key) ||
                                    "shortcuts".equals(key) ||
                                    "language".equals(key) ||
                                    "wal".equals(key))
                                continue;

                            if ("theme".equals(key) || "beige".equals(key)) {
                                defer.put(key, jsetting.get("value"));
                                continue;
                            }

                            if (key != null && key.startsWith("widget."))
                                continue;

                            if ("external_search".equals(key)) {
                                boolean external_search = jsetting.getBoolean("value");
                                Helper.enableComponent(context, ActivitySearch.class, external_search);
                                continue;
                            }

                            if ("external_storage".equals(key))
                                continue;

                            Object value = jsetting.get("value");
                            String type = jsetting.optString("type");
                            Log.i("Setting name=" + key + " value=" + value + " type=" + type);
                            switch (type) {
                                case "bool":
                                    editor.putBoolean(key, (Boolean) value);
                                    break;
                                case "int":
                                    editor.putInt(key, (Integer) value);
                                    break;
                                case "long":
                                    if (value instanceof Integer)
                                        editor.putLong(key, Long.valueOf((Integer) value));
                                    else
                                        editor.putLong(key, (Long) value);
                                    break;
                                case "string":
                                    editor.putString(key, (String) value);
                                    break;
                                default:
                                    Log.w("Inferring type of value=" + value);
                                    if (value instanceof Boolean)
                                        editor.putBoolean(key, (Boolean) value);
                                    else if (value instanceof Integer) {
                                        Integer i = (Integer) value;
                                        if (key.endsWith(".account"))
                                            editor.putLong(key, Long.valueOf(i));
                                        else
                                            editor.putInt(key, i);
                                    } else if (value instanceof Long)
                                        editor.putLong(key, (Long) value);
                                    else if (value instanceof String)
                                        editor.putString(key, (String) value);
                                    else
                                        throw new IllegalArgumentException("Unknown settings type key=" + key);
                            }

                            Log.i("Imported setting=" + key);
                        }
                        editor.apply();
                        ApplicationEx.upgrade(context);
                    }

                    if (import_accounts) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (jimport.has("channels")) {
                                JSONArray jchannels = jimport.getJSONArray("channels");
                                for (int i = 0; i < jchannels.length(); i++)
                                    try {
                                        JSONObject jchannel = (JSONObject) jchannels.get(i);

                                        String channelId = jchannel.getString("id");
                                        nm.deleteNotificationChannel(channelId);

                                        nm.createNotificationChannel(NotificationHelper.channelFromJSON(context, jchannel));

                                        Log.i("Imported contact channel=" + jchannel);
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                            }
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "import");
                Log.i("Imported data");

                SpannableStringBuilder ssb = new SpannableStringBuilder();
                ssb.append(context.getString(R.string.title_setup_imported));
                ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), 0);
                postProgress(ssb, null);
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof NoStreamException)
                    ((NoStreamException) ex).report(ActivitySetup.this);
                else {
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    if (ex.getCause() instanceof BadPaddingException /* GCM: AEADBadTagException */)
                        ssb.append(getString(R.string.title_setup_password_invalid));
                    else if (ex instanceof IOException && ex.getCause() instanceof IllegalBlockSizeException)
                        ssb.append(getString(R.string.title_setup_import_invalid));
                    if (ssb.length() > 0) {
                        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), 0);
                        ssb.setSpan(new ForegroundColorSpan(colorWarning), 0, ssb.length(), 0);
                        ssb.append("\n\n");
                    }
                    ssb.append(ex.toString());
                    onProgress(ssb, null);
                }
            }
        }.execute(this, args, "setup:import");
    }

    private void handleK9Import(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable("uri", uri);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                DB db = DB.getInstance(context);
                ContentResolver resolver = context.getContentResolver();
                InputStream is = resolver.openInputStream(uri);
                if (is == null)
                    throw new FileNotFoundException(uri.toString());
                try (InputStream bis = new BufferedInputStream(is)) {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser xml = factory.newPullParser();
                    xml.setInput(new InputStreamReader(bis));

                    EntityAccount account = null;
                    EntityIdentity identity = null;
                    boolean inIdentity = false;
                    String iname = null;
                    String iemail = null;
                    List<Pair<String, String>> identities = new ArrayList<>();

                    int eventType = xml.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            String name = xml.getName();
                            switch (name) {
                                case "account":
                                    account = new EntityAccount();
                                    account.auth_type = ServiceAuthenticator.AUTH_TYPE_PASSWORD;
                                    account.password = "";
                                    account.synchronize = false;
                                    account.primary = false;
                                    break;
                                case "incoming-server":
                                    if (account != null) {
                                        String itype = xml.getAttributeValue(null, "type");
                                        if ("IMAP".equals(itype))
                                            account.protocol = EntityAccount.TYPE_IMAP;
                                        else if ("POP3".equals(itype))
                                            account.protocol = EntityAccount.TYPE_POP;
                                        else
                                            account = null;
                                    }
                                    break;
                                case "outgoing-server":
                                    String otype = xml.getAttributeValue(null, "type");
                                    if ("SMTP".equals(otype)) {
                                        identity = new EntityIdentity();
                                        identity.auth_type = ServiceAuthenticator.AUTH_TYPE_PASSWORD;
                                        identity.password = "";
                                        identity.synchronize = false;
                                        identity.primary = false;
                                    }
                                    break;
                                case "host":
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        String host = xml.getText();
                                        if (identity != null)
                                            identity.host = host;
                                        else if (account != null)
                                            account.host = host;
                                    }
                                    break;
                                case "port":
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        int port = Integer.parseInt(xml.getText());
                                        if (identity != null)
                                            identity.port = port;
                                        else if (account != null)
                                            account.port = port;
                                    }
                                    break;
                                case "connection-security":
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        String etype = xml.getText();

                                        int encryption;
                                        if ("STARTTLS_REQUIRED".equals(etype))
                                            encryption = EmailService.ENCRYPTION_STARTTLS;
                                        else if ("SSL_TLS_REQUIRED".equals(etype))
                                            encryption = EmailService.ENCRYPTION_SSL;
                                        else
                                            encryption = EmailService.ENCRYPTION_NONE;

                                        if (identity != null)
                                            identity.encryption = encryption;
                                        else if (account != null)
                                            account.encryption = encryption;
                                    }
                                    break;
                                case "authentication-type":
                                    eventType = xml.next();
                                    if (eventType != XmlPullParser.TEXT || !"PLAIN".equals(xml.getText())) {
                                        account = null;
                                        identity = null;
                                    }
                                    break;
                                case "username":
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        String user = xml.getText();
                                        if (identity != null)
                                            identity.user = user;
                                        else if (account != null)
                                            account.user = user;
                                    }
                                    break;
                                case "name":
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        if (inIdentity)
                                            iname = xml.getText();
                                        else {
                                            if (account != null)
                                                account.name = xml.getText();
                                        }
                                    }
                                    break;
                                case "email":
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        if (inIdentity)
                                            iemail = xml.getText();
                                    }
                                    break;
                                case "identity":
                                    inIdentity = true;
                                    break;
                            }

                        } else if (eventType == XmlPullParser.END_TAG) {
                            String name = xml.getName();
                            switch (name) {
                                case "account":
                                    if (account != null && identity != null) {
                                        if (TextUtils.isEmpty(account.name))
                                            account.name = account.user;
                                        if (BuildConfig.DEBUG)
                                            account.name = "K9/" + account.name;

                                        try {
                                            db.beginTransaction();

                                            account.id = db.account().insertAccount(account);
                                            identity.account = account.id;
                                            for (Pair<String, String> i : identities) {
                                                identity.id = null;
                                                identity.name = i.first;
                                                identity.email = i.second;
                                                if (TextUtils.isEmpty(identity.name))
                                                    identity.name = identity.user;
                                                if (TextUtils.isEmpty(identity.email))
                                                    identity.email = identity.user;
                                                identity.id = db.identity().insertIdentity(identity);
                                            }

                                            db.setTransactionSuccessful();
                                        } finally {
                                            account = null;
                                            identity = null;
                                            identities.clear();
                                            db.endTransaction();
                                        }
                                    }
                                    break;
                                case "identity":
                                    identities.add(new Pair<>(iname, iemail));
                                    iname = null;
                                    iemail = null;
                                    inIdentity = false;
                                    break;
                            }
                        }

                        eventType = xml.next();
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(ActivitySetup.this, R.string.title_setup_imported, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "setup:k9");
    }

    private void handleImportCertificate(Intent data) {
        Uri uri = data.getData();
        if (uri != null) {
            Bundle args = new Bundle();
            args.putParcelable("uri", uri);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) throws Throwable {
                    Uri uri = args.getParcelable("uri");
                    Log.i("Import certificate uri=" + uri);

                    if (uri == null)
                        throw new FileNotFoundException();

                    boolean der = false;
                    String extension = Helper.getExtension(uri.getLastPathSegment());
                    Log.i("Extension=" + extension);
                    if (!"pem".equalsIgnoreCase(extension))
                        try {
                            DocumentFile dfile = DocumentFile.fromSingleUri(context, uri);
                            String type = dfile.getType();
                            Log.i("Type=" + type);
                            if ("application/octet-stream".equals(type))
                                der = true;
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                    Log.i("DER=" + der);

                    X509Certificate cert;
                    CertificateFactory fact = CertificateFactory.getInstance("X.509");
                    try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                        if (is == null)
                            throw new FileNotFoundException(uri.toString());

                        if (der)
                            cert = (X509Certificate) fact.generateCertificate(is);
                        else {
                            // throws DecoderException extends IllegalStateException
                            PemObject pem = new PemReader(new InputStreamReader(is)).readPemObject();
                            if (pem == null)
                                throw new IllegalArgumentException("Invalid key file");
                            ByteArrayInputStream bis = new ByteArrayInputStream(pem.getContent());
                            cert = (X509Certificate) fact.generateCertificate(bis);
                        }
                    }

                    String fingerprint = EntityCertificate.getFingerprintSha256(cert);
                    List<String> emails = EntityCertificate.getEmailAddresses(cert);

                    if (emails.size() == 0)
                        throw new IllegalArgumentException("No email address found in key");

                    DB db = DB.getInstance(context);
                    for (String email : emails) {
                        EntityCertificate record = db.certificate().getCertificate(fingerprint, email);
                        if (record == null) {
                            record = EntityCertificate.from(cert, email);
                            record.id = db.certificate().insertCertificate(record);
                        }
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    // DecoderException: unable to decode base64 string: invalid characters encountered in base64 data
                    if (ex instanceof DecoderException)
                        ex = new Throwable("Are you trying to import a PGP key as an S/MIME key?", ex);
                    boolean expected =
                            (ex instanceof IllegalArgumentException ||
                                    ex instanceof FileNotFoundException ||
                                    ex instanceof CertificateException ||
                                    ex instanceof DecoderException ||
                                    ex instanceof SecurityException);
                    Log.unexpectedError(getSupportFragmentManager(), ex, !expected);
                }
            }.execute(this, args, "setup:cert");
        }
    }

    private void handleImportProviders(Intent data) {
        Bundle args = new Bundle();
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                Log.i("Reading URI=" + uri);
                ContentResolver resolver = context.getContentResolver();
                InputStream is = resolver.openInputStream(uri);
                if (is == null)
                    throw new FileNotFoundException(uri.toString());
                EmailProvider.importProfiles(is, context);

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(ActivitySetup.this, R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "import:providers");
    }

    private void onGmail(Intent intent) {
        FragmentGmail fragment = new FragmentGmail();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("quick");
        fragmentTransaction.commit();
    }

    private void onOAuth(Intent intent) {
        FragmentOAuth fragment = new FragmentOAuth();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("quick");
        fragmentTransaction.commit();
    }

    private void onQuickSetup(Intent intent) {
        FragmentQuickSetup fragment = new FragmentQuickSetup();
        fragment.setArguments(new Bundle());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("quick");
        fragmentTransaction.commit();
    }

    private void onQuickPop3(Intent intent) {
        FragmentBase fragment = new FragmentPop();
        fragment.setArguments(new Bundle());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("account");
        fragmentTransaction.commit();
    }

    private void onViewAccounts(Intent intent) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAccounts()).addToBackStack("accounts");
        fragmentTransaction.commit();
    }

    private void onViewIdentities(Intent intent) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentIdentities()).addToBackStack("identities");
        fragmentTransaction.commit();
    }

    private void onEditAccount(Intent intent) {
        int protocol = intent.getIntExtra("protocol", EntityAccount.TYPE_IMAP);
        FragmentBase fragment;
        switch (protocol) {
            case EntityAccount.TYPE_IMAP:
                fragment = new FragmentAccount();
                break;
            case EntityAccount.TYPE_POP:
                fragment = new FragmentPop();
                break;
            default:
                throw new IllegalArgumentException("Unknown protocol=" + protocol);
        }
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("account");
        fragmentTransaction.commit();
    }

    private void onEditIdentity(Intent intent) {
        FragmentIdentity fragment = new FragmentIdentity();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("identity");
        fragmentTransaction.commit();
    }

    private void onManageLocalContacts(Intent intent) {
        Bundle args = new Bundle();
        // All accounts
        args.putBoolean("junk", intent.getBooleanExtra("junk", false));

        FragmentContacts fragment = new FragmentContacts();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("contacts");
        fragmentTransaction.commit();
    }

    private void onManageCertificates(Intent intent) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentCertificates()).addToBackStack("certificates");
        fragmentTransaction.commit();
    }

    private void onImportCertificate(Intent intent) {
        Intent open = new Intent(Intent.ACTION_GET_CONTENT);
        open.addCategory(Intent.CATEGORY_OPENABLE);
        open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        open.setType("*/*");
        if (open.resolveActivity(getPackageManager()) == null)  // system whitelisted
            ToastEx.makeText(this, R.string.title_no_saf, Toast.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(this, open), REQUEST_IMPORT_CERTIFICATE);
    }

    private void onSetupMore(Intent intent) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private static Intent getIntentExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "fairemail_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date().getTime()) + ".backup");
        Helper.openAdvanced(intent);
        return intent;
    }

    private static Intent getIntentImport() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("*/*");
        return intent;
    }

    public static class FragmentDialogExport extends FragmentDialogBase {
        private TextInputLayout tilPassword1;
        private TextInputLayout tilPassword2;

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            outState.putString("fair:password1", tilPassword1 == null ? null : tilPassword1.getEditText().getText().toString());
            outState.putString("fair:password2", tilPassword2 == null ? null : tilPassword2.getEditText().getText().toString());
            super.onSaveInstanceState(outState);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Context context = getContext();
            View dview = LayoutInflater.from(context).inflate(R.layout.dialog_export, null);
            tilPassword1 = dview.findViewById(R.id.tilPassword1);
            tilPassword2 = dview.findViewById(R.id.tilPassword2);

            if (savedInstanceState != null) {
                tilPassword1.getEditText().setText(savedInstanceState.getString("fair:password1"));
                tilPassword2.getEditText().setText(savedInstanceState.getString("fair:password2"));
            }

            Dialog dialog = new AlertDialog.Builder(context)
                    .setView(dview)
                    .setPositiveButton(R.string.title_save_file, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((ActivitySetup) getActivity()).password =
                                    tilPassword1.getEditText().getText().toString();
                            getActivity().startActivityForResult(
                                    Helper.getChooser(context, getIntentExport()), REQUEST_EXPORT);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            return dialog;
        }

        @Override
        public void onStart() {
            super.onStart();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean debug = (BuildConfig.DEBUG || prefs.getBoolean("debug", false));

            Button btnOk = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);

            TextWatcher w = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Do nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Do nothing
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String p1 = tilPassword1.getEditText().getText().toString();
                    String p2 = tilPassword2.getEditText().getText().toString();
                    btnOk.setEnabled((debug || !TextUtils.isEmpty(p1)) && p1.equals(p2));
                    tilPassword2.setHint(!TextUtils.isEmpty(p2) && !p2.equals(p1)
                            ? R.string.title_setup_password_different
                            : R.string.title_setup_password_repeat);
                }
            };

            tilPassword1.getEditText().addTextChangedListener(w);
            tilPassword2.getEditText().addTextChangedListener(w);
            w.afterTextChanged(null);

            tilPassword2.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        btnOk.performClick();
                        return true;
                    } else
                        return false;
                }
            });
        }
    }

    public static class FragmentDialogImport extends FragmentDialogBase {
        private TextInputLayout tilPassword1;

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            outState.putString("fair:password1", tilPassword1 == null ? null : tilPassword1.getEditText().getText().toString());
            super.onSaveInstanceState(outState);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Context context = getContext();
            View dview = LayoutInflater.from(context).inflate(R.layout.dialog_import, null);
            tilPassword1 = dview.findViewById(R.id.tilPassword1);
            CheckBox cbAccounts = dview.findViewById(R.id.cbAccounts);
            CheckBox cbDelete = dview.findViewById(R.id.cbDelete);
            CheckBox cbRules = dview.findViewById(R.id.cbRules);
            CheckBox cbContacts = dview.findViewById(R.id.cbContacts);
            CheckBox cbAnswers = dview.findViewById(R.id.cbAnswers);
            CheckBox cbSettings = dview.findViewById(R.id.cbSettings);

            cbAccounts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    cbRules.setEnabled(checked);
                    cbContacts.setEnabled(checked);
                }
            });

            if (savedInstanceState != null)
                tilPassword1.getEditText().setText(savedInstanceState.getString("fair:password1"));

            Dialog dialog = new AlertDialog.Builder(context)
                    .setView(dview)
                    .setPositiveButton(R.string.title_add_image_select, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String password1 = tilPassword1.getEditText().getText().toString();

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            boolean debug = prefs.getBoolean("debug", false);

                            if (TextUtils.isEmpty(password1) && !(debug || BuildConfig.DEBUG))
                                ToastEx.makeText(context, R.string.title_setup_password_missing, Toast.LENGTH_LONG).show();
                            else {
                                ActivitySetup activity = (ActivitySetup) getActivity();
                                activity.password = password1;
                                activity.import_accounts = cbAccounts.isChecked();
                                activity.import_delete = cbDelete.isChecked();
                                activity.import_rules = cbRules.isChecked();
                                activity.import_contacts = cbContacts.isChecked();
                                activity.import_answers = cbAnswers.isChecked();
                                activity.import_settings = cbSettings.isChecked();
                                getActivity().startActivityForResult(
                                        Helper.getChooser(context, getIntentImport()), REQUEST_IMPORT);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            return dialog;
        }

        @Override
        public void onStart() {
            super.onStart();

            Button btnOk = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);

            tilPassword1.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        btnOk.performClick();
                        return true;
                    } else
                        return false;
                }
            });
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                String action = intent.getAction();
                if (ACTION_QUICK_GMAIL.equals(action))
                    onGmail(intent);
                else if (ACTION_QUICK_OAUTH.equals(action))
                    onOAuth(intent);
                else if (ACTION_QUICK_SETUP.equals(action))
                    onQuickSetup(intent);
                else if (ACTION_QUICK_POP3.equals(action))
                    onQuickPop3(intent);
                else if (ACTION_VIEW_ACCOUNTS.equals(action))
                    onViewAccounts(intent);
                else if (ACTION_VIEW_IDENTITIES.equals(action))
                    onViewIdentities(intent);
                else if (ACTION_EDIT_ACCOUNT.equals(action))
                    onEditAccount(intent);
                else if (ACTION_EDIT_IDENTITY.equals(action))
                    onEditIdentity(intent);
                else if (ACTION_MANAGE_LOCAL_CONTACTS.equals(action))
                    onManageLocalContacts(intent);
                else if (ACTION_MANAGE_CERTIFICATES.equals(action))
                    onManageCertificates(intent);
                else if (ACTION_IMPORT_CERTIFICATE.equals(action))
                    onImportCertificate(intent);
                else if (ACTION_SETUP_REORDER.equals(action))
                    onMenuOrder(R.string.title_setup_reorder_accounts, intent.getStringExtra("className"));
                else if (ACTION_SETUP_MORE.equals(action))
                    onSetupMore(intent);
            }
        }
    };
}
