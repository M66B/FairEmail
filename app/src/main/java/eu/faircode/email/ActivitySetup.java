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
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ActivitySetup extends ActivityBilling implements FragmentManager.OnBackStackChangedListener {
    private View view;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private boolean hasAccount;

    private static final int KEY_ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    static final int REQUEST_PERMISSION = 1;
    static final int REQUEST_CHOOSE_ACCOUNT = 2;

    static final int REQUEST_SOUND = 3;

    static final int REQUEST_EXPORT = 4;
    static final int REQUEST_IMPORT = 5;

    static final int REQUEST_ERROR = 6;

    static final String ACTION_EDIT_ACCOUNT = BuildConfig.APPLICATION_ID + ".EDIT_ACCOUNT";
    static final String ACTION_EDIT_IDENTITY = BuildConfig.APPLICATION_ID + ".EDIT_IDENTITY";

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

        drawerList = findViewById(R.id.drawer_list);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerItem item = (DrawerItem) parent.getAdapter().getItem(position);
                switch (item.getId()) {
                    case R.string.title_setup_help:
                        onMenuHelp();
                        break;
                    case R.string.title_setup_notifications:
                        onManageNotifications();
                        break;
                    case R.string.title_setup_export:
                        onMenuExport();
                        break;
                    case R.string.title_setup_import:
                        onMenuImport();
                        break;
                    case R.string.title_setup_light_theme:
                    case R.string.title_setup_dark_theme:
                    case R.string.title_setup_black_theme:
                        onMenuTheme(item.getId());
                        break;
                    case R.string.title_setup_advanced:
                        onMenuOptions();
                        break;
                    case R.string.menu_legend:
                        onMenuLegend();
                        break;
                    case R.string.menu_faq:
                        onMenuFAQ();
                        break;
                    case R.string.menu_privacy:
                        onMenuPrivacy();
                        break;
                    case R.string.menu_about:
                        onMenuAbout();
                        break;
                }

                drawerLayout.closeDrawer(drawerList);
            }
        });

        PackageManager pm = getPackageManager();
        DrawerAdapter drawerArray = new DrawerAdapter(this);

        if (getIntentHelp().resolveActivity(pm) != null)
            drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_live_help_24, R.string.title_setup_help));

        drawerArray.add(new DrawerItem(R.layout.item_drawer_separator));

        if (getIntentNotifications(this).resolveActivity(pm) != null)
            drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_notifications_24, R.string.title_setup_notifications));

        if (getIntentExport().resolveActivity(pm) != null)
            drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_archive_24, R.string.title_setup_export));
        if (getIntentImport().resolveActivity(pm) != null)
            drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_unarchive_24, R.string.title_setup_import));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("theme", "light");
        if ("dark".equals(theme))
            drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_palette_24, R.string.title_setup_black_theme));
        else if ("black".equals(theme))
            drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_palette_24, R.string.title_setup_light_theme));
        else
            drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_palette_24, R.string.title_setup_dark_theme));

        drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_settings_applications_24, R.string.title_setup_advanced));

        drawerArray.add(new DrawerItem(R.layout.item_drawer_separator));

        drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_help_24, R.string.menu_legend));
        if (Helper.getIntentFAQ().resolveActivity(getPackageManager()) != null)
            drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_question_answer_24, R.string.menu_faq));
        if (Helper.getIntentPrivacy().resolveActivity(getPackageManager()) != null)
            drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_account_box_24, R.string.menu_privacy));
        drawerArray.add(new DrawerItem(this, R.layout.item_drawer, R.drawable.baseline_info_24, R.string.menu_about));

        drawerList.setAdapter(drawerArray);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (getSupportFragmentManager().getFragments().size() == 0) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new FragmentSetup()).addToBackStack("setup");
            fragmentTransaction.commit();
        }

        if (savedInstanceState != null)
            drawerToggle.setDrawerIndicatorEnabled(savedInstanceState.getBoolean("toggle"));

        DB.getInstance(this).account().liveAccounts(true).observe(this, new Observer<List<EntityAccount>>() {
            @Override
            public void onChanged(List<EntityAccount> accounts) {
                hasAccount = (accounts != null && accounts.size() > 0);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("toggle", drawerToggle.isDrawerIndicatorEnabled());
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
        if (drawerLayout.isDrawerOpen(drawerList))
            drawerLayout.closeDrawer(drawerList);
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
            if (drawerLayout.isDrawerOpen(drawerList))
                drawerLayout.closeDrawer(drawerList);
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
        if (requestCode == ActivitySetup.REQUEST_EXPORT || requestCode == ActivitySetup.REQUEST_IMPORT)
            if (resultCode == RESULT_OK && data != null)
                fileSelected(requestCode == ActivitySetup.REQUEST_EXPORT, data);
    }

    private void fileSelected(final boolean export, final Intent data) {
        View dview = LayoutInflater.from(this).inflate(R.layout.dialog_password, null);
        final TextInputLayout etPassword1 = dview.findViewById(R.id.tilPassword1);
        final TextInputLayout etPassword2 = dview.findViewById(R.id.tilPassword2);

        new DialogBuilderLifecycle(this, this)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String password1 = etPassword1.getEditText().getText().toString();
                        String password2 = etPassword2.getEditText().getText().toString();

                        if (TextUtils.isEmpty(password1))
                            Snackbar.make(view, R.string.title_setup_password_missing, Snackbar.LENGTH_LONG).show();
                        else {
                            if (password1.equals(password2)) {
                                if (export)
                                    handleExport(data, password1);
                                else
                                    handleImport(data, password1);
                            } else
                                Snackbar.make(view, R.string.title_setup_password_different, Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .show();
    }

    private void onMenuHelp() {
        startActivity(getIntentHelp());
    }

    private void onManageNotifications() {
        startActivity(getIntentNotifications(this));
    }

    private void onMenuExport() {
        if (Helper.isPro(this))
            try {
                startActivityForResult(Helper.getChooser(this, getIntentExport()), ActivitySetup.REQUEST_EXPORT);
            } catch (Throwable ex) {
                Helper.unexpectedError(this, this, ex);
            }
        else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
            fragmentTransaction.commit();
        }
    }

    private void onMenuImport() {
        try {
            startActivityForResult(Helper.getChooser(this, getIntentImport()), ActivitySetup.REQUEST_IMPORT);
        } catch (Throwable ex) {
            Helper.unexpectedError(this, this, ex);
        }
    }

    private void onMenuTheme(int id) {
        if (Helper.isPro(this)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            switch (id) {
                case R.string.title_setup_light_theme:
                    prefs.edit().putString("theme", "light").apply();
                    break;
                case R.string.title_setup_dark_theme:
                    prefs.edit().putString("theme", "dark").apply();
                    break;
                case R.string.title_setup_black_theme:
                    prefs.edit().putString("theme", "black").apply();
                    break;
            }
        } else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
            fragmentTransaction.commit();
        }
    }

    private void onMenuOptions() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentOptions()).addToBackStack("options");
        fragmentTransaction.commit();
    }

    private void onMenuLegend() {
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

    private void onMenuAbout() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAbout()).addToBackStack("about");
        fragmentTransaction.commit();
    }

    private Intent getIntentHelp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/M66B/open-source-email/blob/master/SETUP.md#setup-help"));
        return intent;
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
        intent.putExtra(Intent.EXTRA_TITLE, "fairemail_backup_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date().getTime()) + ".json");
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

                OutputStream out = null;
                try {
                    Log.i("Writing URI=" + uri);

                    byte[] salt = new byte[16];
                    SecureRandom random = new SecureRandom();
                    random.nextBytes(salt);

                    // https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Cipher
                    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                    KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, KEY_ITERATIONS, KEY_LENGTH);
                    SecretKey secret = keyFactory.generateSecret(keySpec);
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, secret);

                    OutputStream raw = context.getContentResolver().openOutputStream(uri);
                    raw.write(salt);
                    raw.write(cipher.getIV());
                    out = new CipherOutputStream(raw, cipher);

                    DB db = DB.getInstance(context);

                    // Accounts
                    JSONArray jaccounts = new JSONArray();
                    for (EntityAccount account : db.account().getAccounts()) {
                        // Account
                        JSONObject jaccount = account.toJSON();

                        // Identities
                        JSONArray jidentities = new JSONArray();
                        for (EntityIdentity identity : db.identity().getIdentities(account.id))
                            jidentities.put(identity.toJSON());
                        jaccount.put("identities", jidentities);

                        // Folders
                        JSONArray jfolders = new JSONArray();
                        for (EntityFolder folder : db.folder().getFolders(account.id))
                            jfolders.put(folder.toJSON());
                        jaccount.put("folders", jfolders);

                        jaccounts.put(jaccount);
                    }

                    // Answers
                    JSONArray janswers = new JSONArray();
                    for (EntityAnswer answer : db.answer().getAnswers())
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

                    out.write(jexport.toString(2).getBytes());

                    Log.i("Exported data");
                } finally {
                    if (out != null)
                        out.close();
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

                InputStream in = null;
                try {
                    Log.i("Reading URI=" + uri);
                    ContentResolver resolver = context.getContentResolver();
                    AssetFileDescriptor descriptor = resolver.openTypedAssetFileDescriptor(uri, "*/*", null);
                    InputStream raw = descriptor.createInputStream();

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

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        response.append(line);
                    Log.i("Importing " + resolver.toString());

                    JSONObject jimport = new JSONObject(response.toString());

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        JSONArray jaccounts = jimport.getJSONArray("accounts");
                        for (int a = 0; a < jaccounts.length(); a++) {
                            JSONObject jaccount = (JSONObject) jaccounts.get(a);
                            EntityAccount account = EntityAccount.fromJSON(jaccount);
                            account.created = new Date().getTime();
                            account.id = db.account().insertAccount(account);
                            Log.i("Imported account=" + account.name);

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                                if (account.notify)
                                    account.createNotificationChannel(context);

                            JSONArray jidentities = (JSONArray) jaccount.get("identities");
                            for (int i = 0; i < jidentities.length(); i++) {
                                JSONObject jidentity = (JSONObject) jidentities.get(i);
                                EntityIdentity identity = EntityIdentity.fromJSON(jidentity);
                                identity.account = account.id;
                                identity.id = db.identity().insertIdentity(identity);
                                Log.i("Imported identity=" + identity.email);
                            }

                            JSONArray jfolders = (JSONArray) jaccount.get("folders");
                            for (int f = 0; f < jfolders.length(); f++) {
                                JSONObject jfolder = (JSONObject) jfolders.get(f);
                                EntityFolder folder = EntityFolder.fromJSON(jfolder);
                                folder.account = account.id;
                                folder.id = db.folder().insertFolder(folder);
                                Log.i("Imported folder=" + folder.name);
                            }
                        }

                        JSONArray janswers = jimport.getJSONArray("answers");
                        for (int a = 0; a < janswers.length(); a++) {
                            JSONObject janswer = (JSONObject) janswers.get(a);
                            EntityAnswer answer = EntityAnswer.fromJSON(janswer);
                            answer.id = db.answer().insertAnswer(answer);
                            Log.i("Imported answer=" + answer.name);
                        }

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        JSONArray jsettings = jimport.getJSONArray("settings");
                        for (int s = 0; s < jsettings.length(); s++) {
                            JSONObject jsetting = (JSONObject) jsettings.get(s);
                            String key = jsetting.getString("key");
                            if (!"pro".equals(key) && !"theme".equals(key)) {
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

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    Log.i("Imported data");
                    ServiceSynchronize.reload(context, "import");
                } finally {
                    if (in != null)
                        in.close();
                }

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

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_EDIT_ACCOUNT.equals(intent.getAction()))
                onEditAccount(intent);
            else if (ACTION_EDIT_IDENTITY.equals(intent.getAction()))
                onEditIdentity(intent);
        }
    };
}
