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

import android.app.Notification;
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
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class ActivitySetup extends ActivityBilling implements FragmentManager.OnBackStackChangedListener {
    private View view;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ConstraintLayout drawerContainer;
    private RecyclerView rvMenu;

    private boolean hasAccount;
    private String password;

    private static final int KEY_ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    static final int REQUEST_PERMISSION = 1;
    static final int REQUEST_CHOOSE_ACCOUNT = 2;

    static final int REQUEST_SOUND = 3;

    static final int REQUEST_EXPORT = 4;
    static final int REQUEST_IMPORT = 5;

    static final String ACTION_EDIT_ACCOUNT = BuildConfig.APPLICATION_ID + ".EDIT_ACCOUNT";
    static final String ACTION_EDIT_IDENTITY = BuildConfig.APPLICATION_ID + ".EDIT_IDENTITY";
    static final String ACTION_SHOW_PRO = BuildConfig.APPLICATION_ID + ".SHOW_PRO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = LayoutInflater.from(this).inflate(R.layout.activity_setup, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Helper.resolveColor(this, R.attr.colorDrawerScrim));

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getString(R.string.app_name));
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
                NavMenuItem menu = adapter.get(pos);
                outRect.set(0, 0, 0, menu != null && menu.isSeparated() ? d.getIntrinsicHeight() : 0);
            }
        };
        itemDecorator.setDrawable(d);
        rvMenu.addItemDecoration(itemDecorator);

        PackageManager pm = getPackageManager();
        final List<NavMenuItem> menus = new ArrayList<>();

        menus.add(new NavMenuItem(R.drawable.baseline_archive_24, R.string.title_setup_export, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuExport();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.baseline_unarchive_24, R.string.title_setup_import, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuImport();
            }
        }).setSeparated());

        if (getIntentNotifications(this).resolveActivity(pm) != null)
            menus.add(new NavMenuItem(R.drawable.baseline_notifications_24, R.string.title_setup_notifications, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onManageNotifications();
                }
            }));

        menus.add(new NavMenuItem(R.drawable.baseline_reorder_24, R.string.title_setup_reorder_accounts, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuOrder(R.string.title_setup_reorder_accounts, EntityAccount.class);
            }
        }));

        menus.add(new NavMenuItem(R.drawable.baseline_reorder_24, R.string.title_setup_reorder_folders, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuOrder(R.string.title_setup_reorder_folders, TupleFolderSort.class);
            }
        }));

        menus.add(new NavMenuItem(R.drawable.baseline_palette_24, R.string.title_setup_theme, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuTheme();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.baseline_person_24, R.string.menu_contacts, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuContacts();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.baseline_settings_applications_24, R.string.title_setup_advanced, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuOptions();
            }
        }).setSeparated());

        menus.add(new NavMenuItem(R.drawable.baseline_help_24, R.string.menu_legend, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuLegend();
            }
        }));

        if (Helper.getIntentFAQ().resolveActivity(pm) != null)
            menus.add(new NavMenuItem(R.drawable.baseline_question_answer_24, R.string.menu_faq, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onMenuFAQ();
                }
            }));

        if (Helper.getIntentPrivacy().resolveActivity(pm) != null)
            menus.add(new NavMenuItem(R.drawable.baseline_account_box_24, R.string.menu_privacy, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onMenuPrivacy();
                }
            }, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onCleanup();
                }
            }));

        menus.add(new NavMenuItem(R.drawable.baseline_info_24, R.string.menu_about, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuAbout();
            }
        }));

        adapter.set(menus);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (getSupportFragmentManager().getFragments().size() == 0) {
            Intent intent = getIntent();
            String target = intent.getStringExtra("target");

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if ("accounts".equals(target))
                fragmentTransaction.replace(R.id.content_frame, new FragmentAccounts()).addToBackStack("accounts");
            else
                fragmentTransaction.replace(R.id.content_frame, new FragmentSetup()).addToBackStack("setup");
            fragmentTransaction.commit();

            if (intent.hasExtra("target")) {
                intent.removeExtra("target");
                setIntent(intent);
            }
        }

        if (savedInstanceState != null)
            drawerToggle.setDrawerIndicatorEnabled(savedInstanceState.getBoolean("fair:toggle"));

        DB.getInstance(this).account().liveSynchronizingAccounts().observe(this, new Observer<List<EntityAccount>>() {
            @Override
            public void onChanged(List<EntityAccount> accounts) {
                hasAccount = (accounts != null && accounts.size() > 0);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:toggle", drawerToggle.isDrawerIndicatorEnabled());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_EDIT_ACCOUNT);
        iff.addAction(ACTION_EDIT_IDENTITY);
        iff.addAction(ACTION_SHOW_PRO);
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawerContainer))
            drawerLayout.closeDrawer(drawerContainer);
        else
            super.onBackPressed();
    }

    @Override
    public void onBackStackChanged() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (hasAccount)
                startActivity(new Intent(this, ActivityView.class));
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

        switch (item.getItemId()) {
            case android.R.id.home:
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                    getSupportFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null)
            if (requestCode == REQUEST_EXPORT)
                handleExport(data, this.password);
            else if (requestCode == REQUEST_IMPORT)
                handleImport(data, this.password);
    }

    private void onMenuExport() {
        if (!Helper.isPro(this)) {
            onShowPro(null);
            return;
        }

        try {
            askPassword(true);
        } catch (Throwable ex) {
            Helper.unexpectedError(this, this, ex);
        }
    }

    private void onMenuImport() {
        try {
            askPassword(false);
        } catch (Throwable ex) {
            Helper.unexpectedError(this, this, ex);
        }
    }

    private void askPassword(final boolean export) {
        View dview = LayoutInflater.from(this).inflate(R.layout.dialog_password, null);
        final TextInputLayout etPassword1 = dview.findViewById(R.id.tilPassword1);
        final TextInputLayout etPassword2 = dview.findViewById(R.id.tilPassword2);
        TextView tvImportHint = dview.findViewById(R.id.tvImporthint);

        etPassword2.setVisibility(export ? View.VISIBLE : View.GONE);
        tvImportHint.setVisibility(export ? View.GONE : View.VISIBLE);

        new DialogBuilderLifecycle(this, this)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password1 = etPassword1.getEditText().getText().toString();
                        String password2 = etPassword2.getEditText().getText().toString();

                        if (!BuildConfig.DEBUG && TextUtils.isEmpty(password1))
                            Snackbar.make(view, R.string.title_setup_password_missing, Snackbar.LENGTH_LONG).show();
                        else {
                            if (!export || password1.equals(password2)) {
                                ActivitySetup.this.password = password1;
                                startActivityForResult(
                                        Helper.getChooser(
                                                ActivitySetup.this,
                                                export ? getIntentExport() : getIntentImport()),
                                        export ? REQUEST_EXPORT : REQUEST_IMPORT);
                            } else
                                Snackbar.make(view, R.string.title_setup_password_different, Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .show();
    }

    private void onManageNotifications() {
        startActivity(getIntentNotifications(this));
    }

    private void onMenuOrder(int title, Class clazz) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("order", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putString("class", clazz.getName());

        FragmentOrder fragment = new FragmentOrder();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("order");
        fragmentTransaction.commit();
    }

    private void onMenuTheme() {
        View dview = LayoutInflater.from(this).inflate(R.layout.dialog_theme, null);
        final RadioGroup rgTheme = dview.findViewById(R.id.rgTheme);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("theme", "light");

        switch (theme) {
            case "dark":
                rgTheme.check(R.id.rbThemeDark);
                break;
            case "black":
                rgTheme.check(R.id.rbThemeBlack);
                break;
            case "system":
                rgTheme.check(R.id.rbThemeSystem);
                break;
            default:
                rgTheme.check(R.id.rbThemeLight);
        }

        rgTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbThemeLight:
                        prefs.edit().putString("theme", "light").apply();
                        break;
                    case R.id.rbThemeDark:
                        prefs.edit().putString("theme", "dark").apply();
                        break;
                    case R.id.rbThemeBlack:
                        prefs.edit().putString("theme", "black").apply();
                        break;
                    case R.id.rbThemeSystem:
                        prefs.edit().putString("theme", "system").apply();
                        break;
                }
            }
        });

        new DialogBuilderLifecycle(this, this)
                .setView(dview)
                .show();
    }

    private void onMenuContacts() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("contacts", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentContacts()).addToBackStack("contacts");
        fragmentTransaction.commit();
    }

    private void onMenuOptions() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("options", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentOptions()).addToBackStack("options");
        fragmentTransaction.commit();
    }

    private void onMenuLegend() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("legend", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLegend()).addToBackStack("legend");
        fragmentTransaction.commit();
    }

    private void onMenuFAQ() {
        Helper.view(this, this, Helper.getIntentFAQ());
    }

    private void onMenuPrivacy() {
        Helper.view(this, this, Helper.getIntentPrivacy());
    }

    private void onCleanup() {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                WorkerCleanup.cleanup(context, true);
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(ActivitySetup.this, ActivitySetup.this, ex);
            }
        }.execute(this, new Bundle(), "cleanup:run");
    }

    private void onMenuAbout() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("about", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAbout()).addToBackStack("about");
        fragmentTransaction.commit();
    }

    private static Intent getIntentNotifications(Context context) {
        return new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra("app_package", context.getPackageName())
                .putExtra("app_uid", context.getApplicationInfo().uid)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
    }

    private static Intent getIntentExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "fairemail_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date().getTime()) + ".backup");
        return intent;
    }

    private static Intent getIntentImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
    }

    private void handleExport(Intent data, String password) {
        Bundle args = new Bundle();
        args.putParcelable("uri", data.getData());
        args.putString("password", password);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");
                String password = args.getString("password");

                if ("file".equals(uri.getScheme())) {
                    Log.w("Export uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                Log.i("Collecting data");
                DB db = DB.getInstance(context);
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

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
                                JSONObject jchannel = channelToJSON(channel);
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
                    for (EntityFolder folder : db.folder().getFolders(account.id)) {
                        JSONObject jfolder = folder.toJSON();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = nm.getNotificationChannel(
                                    EntityFolder.getNotificationChannelId(folder.id));
                            if (channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE) {
                                JSONObject jchannel = channelToJSON(channel);
                                jfolder.put("channel", jchannel);
                                Log.i("Exported folder channel=" + jchannel);
                            }
                        }

                        JSONArray jrules = new JSONArray();
                        for (EntityRule rule : db.rule().getRules(folder.id))
                            jrules.put(rule.toJSON());
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

                // Settings
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                JSONArray jsettings = new JSONArray();
                for (String key : prefs.getAll().keySet())
                    if (!"pro".equals(key)) {
                        JSONObject jsetting = new JSONObject();
                        jsetting.put("key", key);
                        jsetting.put("value", prefs.getAll().get(key));
                        jsettings.put(jsetting);
                    }

                JSONObject jexport = new JSONObject();
                jexport.put("accounts", jaccounts);
                jexport.put("answers", janswers);
                jexport.put("settings", jsettings);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    JSONArray jchannels = new JSONArray();
                    for (NotificationChannel channel : nm.getNotificationChannels()) {
                        String id = channel.getId();
                        if (id.startsWith("notification.") && id.contains("@") &&
                                channel.getImportance() != NotificationManager.IMPORTANCE_NONE) {
                            JSONObject jchannel = channelToJSON(channel);
                            jchannels.put(jchannel);
                            Log.i("Exported contact channel=" + jchannel);
                        }
                    }
                    jexport.put("channels", jchannels);
                }

                ContentResolver resolver = context.getContentResolver();
                DocumentFile file = DocumentFile.fromSingleUri(context, uri);
                try (OutputStream raw = new BufferedOutputStream(resolver.openOutputStream(uri))) {
                    Log.i("Writing URI=" + uri + " name=" + file.getName() + " virtual=" + file.isVirtual());

                    if (TextUtils.isEmpty(password))
                        raw.write(jexport.toString(2).getBytes());
                    else {
                        byte[] salt = new byte[16];
                        SecureRandom random = new SecureRandom();
                        random.nextBytes(salt);

                        // https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Cipher
                        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, KEY_ITERATIONS, KEY_LENGTH);
                        SecretKey secret = keyFactory.generateSecret(keySpec);
                        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        cipher.init(Cipher.ENCRYPT_MODE, secret);

                        raw.write(salt);
                        raw.write(cipher.getIV());

                        OutputStream cout = new CipherOutputStream(raw, cipher);
                        cout.write(jexport.toString(2).getBytes());
                        cout.flush();
                        raw.write(cipher.doFinal());
                    }

                    Log.i("Exported data");
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                Snackbar.make(view, R.string.title_setup_exported, Snackbar.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(ActivitySetup.this, ActivitySetup.this, ex);
            }
        }.execute(this, args, "setup:export");
    }

    private void handleImport(Intent data, String password) {
        Bundle args = new Bundle();
        args.putParcelable("uri", data.getData());
        args.putString("password", password);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");
                String password = args.getString("password");

                if ("file".equals(uri.getScheme())) {
                    Log.w("Import uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                StringBuilder data = new StringBuilder();
                Log.i("Reading URI=" + uri);
                ContentResolver resolver = context.getContentResolver();
                AssetFileDescriptor descriptor = resolver.openTypedAssetFileDescriptor(uri, "*/*", null);
                try (InputStream raw = new BufferedInputStream(descriptor.createInputStream())) {

                    InputStream in;
                    if (TextUtils.isEmpty(password))
                        in = raw;
                    else {
                        byte[] salt = new byte[16];
                        byte[] prefix = new byte[16];
                        if (raw.read(salt) != salt.length)
                            throw new IOException("length");
                        if (raw.read(prefix) != prefix.length)
                            throw new IOException("length");

                        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, KEY_ITERATIONS, KEY_LENGTH);
                        SecretKey secret = keyFactory.generateSecret(keySpec);
                        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        IvParameterSpec iv = new IvParameterSpec(prefix);
                        cipher.init(Cipher.DECRYPT_MODE, secret, iv);

                        in = new CipherInputStream(raw, cipher);
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null)
                        data.append(line);
                }

                Log.i("Importing data");
                JSONObject jimport = new JSONObject(data.toString());

                DB db = DB.getInstance(context);
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                try {
                    db.beginTransaction();

                    // Answers
                    Map<Long, Long> xAnswer = new HashMap<>();
                    JSONArray janswers = jimport.getJSONArray("answers");
                    for (int a = 0; a < janswers.length(); a++) {
                        JSONObject janswer = (JSONObject) janswers.get(a);
                        EntityAnswer answer = EntityAnswer.fromJSON(janswer);
                        long id = answer.id;
                        answer.id = null;

                        answer.id = db.answer().insertAnswer(answer);
                        xAnswer.put(id, answer.id);

                        Log.i("Imported answer=" + answer.name);
                    }

                    // Accounts
                    JSONArray jaccounts = jimport.getJSONArray("accounts");
                    for (int a = 0; a < jaccounts.length(); a++) {
                        JSONObject jaccount = (JSONObject) jaccounts.get(a);
                        EntityAccount account = EntityAccount.fromJSON(jaccount);

                        // Forward referenced
                        Long swipe_left = account.swipe_left;
                        Long swipe_right = account.swipe_right;
                        account.swipe_left = null;
                        account.swipe_right = null;

                        account.created = new Date().getTime();
                        account.id = db.account().insertAccount(account);
                        Log.i("Imported account=" + account.name);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            account.deleteNotificationChannel(context);
                            if (account.notify)
                                if (jaccount.has("channel")) {
                                    NotificationChannelGroup group = new NotificationChannelGroup("group." + account.id, account.name);
                                    nm.createNotificationChannelGroup(group);

                                    JSONObject jchannel = (JSONObject) jaccount.get("channel");
                                    jchannel.put("id", EntityAccount.getNotificationChannelId(account.id));
                                    jchannel.put("group", group.getId());
                                    nm.createNotificationChannel(channelFromJSON(context, jchannel));

                                    Log.i("Imported account channel=" + jchannel);
                                } else
                                    account.createNotificationChannel(context);
                        }

                        Map<Long, Long> xIdentity = new HashMap<>();
                        JSONArray jidentities = (JSONArray) jaccount.get("identities");
                        for (int i = 0; i < jidentities.length(); i++) {
                            JSONObject jidentity = (JSONObject) jidentities.get(i);
                            EntityIdentity identity = EntityIdentity.fromJSON(jidentity);
                            long id = identity.id;
                            identity.id = null;

                            identity.account = account.id;
                            identity.id = db.identity().insertIdentity(identity);
                            xIdentity.put(id, identity.id);

                            Log.i("Imported identity=" + identity.email);
                        }

                        Map<Long, Long> xFolder = new HashMap<>();
                        List<EntityRule> rules = new ArrayList<>();

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

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (jfolder.has("channel")) {
                                    NotificationChannelGroup group = new NotificationChannelGroup("group." + account.id, account.name);
                                    nm.createNotificationChannelGroup(group);

                                    JSONObject jchannel = (JSONObject) jfolder.get("channel");
                                    jchannel.put("id", EntityFolder.getNotificationChannelId(folder.id));
                                    jchannel.put("group", group.getId());
                                    nm.createNotificationChannel(channelFromJSON(context, jchannel));

                                    Log.i("Imported folder channel=" + jchannel);
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
                            Log.i("Imported folder=" + folder.name);
                        }

                        for (EntityRule rule : rules) {
                            try {
                                JSONObject jaction = new JSONObject(rule.action);
                                int type = jaction.getInt("type");

                                switch (type) {
                                    case EntityRule.TYPE_MOVE:
                                    case EntityRule.TYPE_COPY:
                                        long target = jaction.getLong("target");
                                        Log.i("XLAT target " + target + " > " + xFolder.get(target));
                                        jaction.put("target", xFolder.get(target));
                                        break;
                                    case EntityRule.TYPE_ANSWER:
                                        long iid = jaction.getLong("identity");
                                        long aid = jaction.getLong("answer");
                                        Log.i("XLAT identity " + iid + " > " + xIdentity.get(iid));
                                        Log.i("XLAT target " + aid + " > " + xAnswer.get(aid));
                                        jaction.put("identity", xIdentity.get(iid));
                                        jaction.put("answer", xAnswer.get(aid));
                                        break;
                                }
                            } catch (JSONException ex) {
                                Log.e(ex);
                            }

                            db.rule().insertRule(rule);
                        }

                        // Contacts
                        if (jaccount.has("contacts")) {
                            JSONArray jcontacts = jaccount.getJSONArray("contacts");
                            for (int c = 0; c < jcontacts.length(); c++) {
                                JSONObject jcontact = (JSONObject) jcontacts.get(c);
                                EntityContact contact = EntityContact.fromJSON(jcontact);
                                contact.account = account.id;
                                if (db.contact().getContact(contact.account, contact.type, contact.email) == null) {
                                    contact.id = db.contact().insertContact(contact);
                                    Log.i("Imported contact=" + contact);
                                }
                            }
                        }

                        // Update swipe left/right
                        db.account().updateAccount(account);
                    }

                    // Settings
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = prefs.edit();
                    JSONArray jsettings = jimport.getJSONArray("settings");
                    for (int s = 0; s < jsettings.length(); s++) {
                        JSONObject jsetting = (JSONObject) jsettings.get(s);
                        String key = jsetting.getString("key");
                        if (!"pro".equals(key)) {
                            Object value = jsetting.get("value");
                            if (value instanceof Boolean)
                                editor.putBoolean(key, (Boolean) value);
                            else if (value instanceof Integer)
                                editor.putInt(key, (Integer) value);
                            else if (value instanceof Long)
                                editor.putLong(key, (Long) value);
                            else if (value instanceof String)
                                editor.putString(key, (String) value);
                            else
                                throw new IllegalArgumentException("Unknown settings type key=" + key);
                            Log.i("Imported setting=" + key);
                        }
                    }
                    editor.apply();
                    ApplicationEx.upgrade(context);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (jimport.has("channels")) {
                            JSONArray jchannels = jimport.getJSONArray("channels");
                            for (int i = 0; i < jchannels.length(); i++) {
                                JSONObject jchannel = (JSONObject) jchannels.get(i);
                                nm.createNotificationChannel(channelFromJSON(context, jchannel));

                                Log.i("Imported contact channel=" + jchannel);
                            }
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                Log.i("Imported data");
                ServiceSynchronize.reload(context, "import");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                Snackbar.make(view, R.string.title_setup_imported, Snackbar.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex.getCause() instanceof BadPaddingException)
                    Snackbar.make(view, R.string.title_setup_password_invalid, Snackbar.LENGTH_LONG).show();
                else if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(ActivitySetup.this, ActivitySetup.this, ex);
            }
        }.execute(this, args, "setup:import");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private JSONObject channelToJSON(NotificationChannel channel) throws JSONException {
        JSONObject jchannel = new JSONObject();

        jchannel.put("id", channel.getId());
        jchannel.put("group", channel.getGroup());
        jchannel.put("name", channel.getName());
        jchannel.put("description", channel.getDescription());

        jchannel.put("importance", channel.getImportance());
        jchannel.put("dnd", channel.canBypassDnd());
        jchannel.put("visibility", channel.getLockscreenVisibility());
        jchannel.put("badge", channel.canShowBadge());

        Uri sound = channel.getSound();
        if (sound != null)
            jchannel.put("sound", sound.toString());
        // audio attributes

        jchannel.put("light", channel.shouldShowLights());
        // color

        jchannel.put("vibrate", channel.shouldVibrate());
        // pattern

        return jchannel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static NotificationChannel channelFromJSON(Context context, JSONObject jchannel) throws JSONException {
        NotificationChannel channel = new NotificationChannel(
                jchannel.getString("id"),
                jchannel.getString("name"),
                jchannel.getInt("importance"));

        channel.setGroup(jchannel.getString("group"));

        if (jchannel.has("description") && !jchannel.isNull("description"))
            channel.setDescription(jchannel.getString("description"));

        channel.setBypassDnd(jchannel.getBoolean("dnd"));
        channel.setLockscreenVisibility(jchannel.getInt("visibility"));
        channel.setShowBadge(jchannel.getBoolean("badge"));

        if (jchannel.has("sound") && !jchannel.isNull("sound")) {
            Uri uri = Uri.parse(jchannel.getString("sound"));
            Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
            if (ringtone != null)
                channel.setSound(uri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        }

        channel.enableLights(jchannel.getBoolean("light"));
        channel.enableVibration(jchannel.getBoolean("vibrate"));

        return channel;
    }

    private void onEditAccount(Intent intent) {
        FragmentAccount fragment = new FragmentAccount();
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

    private void onShowPro(Intent intent) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("pro", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
        fragmentTransaction.commit();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_EDIT_ACCOUNT.equals(intent.getAction()))
                onEditAccount(intent);
            else if (ACTION_EDIT_IDENTITY.equals(intent.getAction()))
                onEditIdentity(intent);
            else if (ACTION_SHOW_PRO.equals(intent.getAction()))
                onShowPro(intent);
        }
    };
}
