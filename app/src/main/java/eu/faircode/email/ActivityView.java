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
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ActivityView extends ActivityBilling implements FragmentManager.OnBackStackChangedListener {
    private boolean unified;
    private boolean threading;

    private View view;
    private DrawerLayout drawerLayout;
    private Group grpPane;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private long message = -1;
    private long attachment = -1;

    private OpenPgpServiceConnection pgpService;

    private static final int ATTACHMENT_BUFFER_SIZE = 8192; // bytes

    static final int REQUEST_UNIFIED = 1;
    static final int REQUEST_THREAD = 2;

    static final int REQUEST_ATTACHMENT = 1;
    static final int REQUEST_DECRYPT = 2;

    static final String ACTION_VIEW_MESSAGES = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGES";
    static final String ACTION_VIEW_THREAD = BuildConfig.APPLICATION_ID + ".VIEW_THREAD";
    static final String ACTION_VIEW_FULL = BuildConfig.APPLICATION_ID + ".VIEW_FULL";
    static final String ACTION_EDIT_FOLDER = BuildConfig.APPLICATION_ID + ".EDIT_FOLDER";
    static final String ACTION_EDIT_ANSWER = BuildConfig.APPLICATION_ID + ".EDIT_ANSWER";
    static final String ACTION_STORE_ATTACHMENT = BuildConfig.APPLICATION_ID + ".STORE_ATTACHMENT";
    static final String ACTION_DECRYPT = BuildConfig.APPLICATION_ID + ".DECRYPT";
    static final String ACTION_SHOW_PRO = BuildConfig.APPLICATION_ID + ".SHOW_PRO";

    static final long UPDATE_INTERVAL = 12 * 3600 * 1000L; // milliseconds

    private static final String PGP_BEGIN_MESSAGE = "-----BEGIN PGP MESSAGE-----";
    private static final String PGP_END_MESSAGE = "-----END PGP MESSAGE-----";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        unified = prefs.getBoolean("unified", true);
        threading = prefs.getBoolean("threading", true);

        view = LayoutInflater.from(this).inflate(R.layout.activity_view, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Helper.resolveColor(this, R.attr.colorDrawerScrim));

        grpPane = findViewById(R.id.grpPane);

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
                    case -1:
                        onMenuFolders((long) item.getData());
                        break;
                    case R.string.menu_setup:
                        onMenuSetup();
                        break;
                    case R.string.menu_answers:
                        onMenuAnswers();
                        break;
                    case R.string.menu_operations:
                        onMenuOperations();
                        break;
                    case R.string.menu_legend:
                        onMenuLegend();
                        break;
                    case R.string.menu_faq:
                        onMenuFAQ();
                        break;
                    case R.string.menu_pro:
                        onMenuPro();
                        break;
                    case R.string.menu_privacy:
                        onMenuPrivacy();
                        break;
                    case R.string.menu_about:
                        onMenuAbout();
                        break;
                    case R.string.menu_rate:
                        onMenuRate();
                        break;
                    case R.string.menu_invite:
                        onMenuInvite();
                        break;
                    case R.string.menu_other:
                        onMenuOtherApps();
                        break;
                }

                drawerLayout.closeDrawer(drawerList);
            }
        });
        drawerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerItem item = (DrawerItem) parent.getAdapter().getItem(position);
                switch (item.getId()) {
                    case -1:
                        onMenuInbox((long) item.getData());
                        break;
                    case R.string.menu_setup:
                        onReload();
                        break;
                    case R.string.menu_faq:
                        onDebugInfo();
                        break;
                    case R.string.menu_privacy:
                        onCleanup();
                        break;
                    case R.string.menu_about:
                        onShowLog();
                        break;
                    default:
                        return false;
                }

                drawerLayout.closeDrawer(drawerList);
                return true;
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        DB.getInstance(this).account().liveAccounts(true, threading).observe(this, new Observer<List<TupleAccountEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleAccountEx> accounts) {
                if (accounts == null)
                    accounts = new ArrayList<>();

                ArrayAdapterDrawer drawerArray = new ArrayAdapterDrawer(ActivityView.this);

                final Collator collator = Collator.getInstance(Locale.getDefault());
                collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                Collections.sort(accounts, new Comparator<EntityAccount>() {
                    @Override
                    public int compare(EntityAccount a1, EntityAccount a2) {
                        return collator.compare(a1.name, a2.name);
                    }
                });

                for (TupleAccountEx account : accounts)
                    drawerArray.add(new DrawerItem(
                            R.layout.item_drawer, -1,
                            "connected".equals(account.state) ? R.drawable.baseline_folder_24 : R.drawable.baseline_folder_open_24,
                            account.color,
                            account.unseen > 0 ? getString(R.string.title_unseen_count, account.name, account.unseen) : account.name,
                            account.unseen > 0,
                            account.id));

                drawerArray.add(new DrawerItem(R.layout.item_drawer_separator));

                drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_settings_applications_24, R.string.menu_setup));
                drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_reply_24, R.string.menu_answers));

                drawerArray.add(new DrawerItem(R.layout.item_drawer_separator));

                drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_list_24, R.string.menu_operations));
                drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_help_24, R.string.menu_legend));

                if (getIntentFAQ().resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_question_answer_24, R.string.menu_faq));

                Intent pro = getIntentPro();
                if (pro == null || pro.resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_monetization_on_24, R.string.menu_pro));

                if (Helper.getIntentPrivacy().resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_account_box_24, R.string.menu_privacy));

                drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_info_24, R.string.menu_about));

                drawerArray.add(new DrawerItem(R.layout.item_drawer_separator));

                if (getIntentInvite().resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_people_24, R.string.menu_invite));

                if (getIntentRate().resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_star_24, R.string.menu_rate));

                if (getIntentOtherApps().resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_get_app_24, R.string.menu_other));

                drawerList.setAdapter(drawerArray);
            }
        });

        if (getSupportFragmentManager().getFragments().size() == 0 &&
                !getIntent().hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
            FragmentEx fragment = (unified ? new FragmentMessages() : new FragmentFolders());
            fragment.setArguments(new Bundle());

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("unified");
            fragmentTransaction.commit();
        }

        if (savedInstanceState != null)
            drawerToggle.setDrawerIndicatorEnabled(savedInstanceState.getBoolean("toggle"));

        new Handler().post(checkIntent);

        checkFirst();
        checkCrash();
        if (!Helper.isPlayStoreInstall(this))
            checkUpdate();

        pgpService = new OpenPgpServiceConnection(this, "org.sufficientlysecure.keychain");
        pgpService.bindToService();

        updateShortcuts();
    }

    private Runnable checkIntent = new Runnable() {
        @Override
        public void run() {
            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                return;

            Intent intent = getIntent();

            String action = intent.getAction();
            Log.i("View intent=" + intent + " action=" + action);
            if (action != null) {
                intent.setAction(null);
                setIntent(intent);

                if ("unified".equals(action))
                    getSupportFragmentManager().popBackStack("unified", 0);

                else if ("error".equals(action))
                    onDebugInfo();

                else if (action.startsWith("thread")) {
                    ViewModelMessages model = ViewModelProviders.of(ActivityView.this).get(ViewModelMessages.class);
                    model.setMessages(null);

                    intent.putExtra("thread", action.split(":", 2)[1]);
                    onViewThread(intent);
                }
            }

            if (intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
                String search = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();

                intent.removeExtra(Intent.EXTRA_PROCESS_TEXT);
                setIntent(intent);

                if (Helper.isPro(ActivityView.this)) {
                    Bundle args = new Bundle();
                    args.putString("search", search);

                    new SimpleTask<Long>() {
                        @Override
                        protected Long onExecute(Context context, Bundle args) {
                            DB db = DB.getInstance(context);

                            db.message().resetSearch();

                            EntityFolder archive = db.folder().getPrimaryArchive();
                            if (archive == null)
                                throw new IllegalArgumentException("No primary archive");
                            return archive.id;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Long archive) {
                            Bundle sargs = new Bundle();
                            sargs.putLong("folder", archive);
                            sargs.putString("search", args.getString("search"));

                            FragmentMessages fragment = new FragmentMessages();
                            fragment.setArguments(sargs);

                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("search");
                            fragmentTransaction.commit();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
                        }
                    }.execute(ActivityView.this, args);
                } else {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                    fragmentTransaction.commit();
                }
            }
        }
    };

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        new Handler().post(checkIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_VIEW_MESSAGES);
        iff.addAction(ACTION_VIEW_THREAD);
        iff.addAction(ACTION_VIEW_FULL);
        iff.addAction(ACTION_EDIT_FOLDER);
        iff.addAction(ACTION_EDIT_ANSWER);
        iff.addAction(ACTION_STORE_ATTACHMENT);
        iff.addAction(ACTION_DECRYPT);
        iff.addAction(ACTION_SHOW_PRO);
        lbm.registerReceiver(receiver, iff);

        if (!pgpService.isBound())
            pgpService.bindToService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        if (pgpService != null)
            pgpService.unbindFromService();

        super.onDestroy();
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
        if (count == 0)
            finish();
        else {
            if (drawerLayout.isDrawerOpen(drawerList))
                drawerLayout.closeDrawer(drawerList);
            drawerToggle.setDrawerIndicatorEnabled(count == 1);

            if (grpPane != null) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_pane);
                grpPane.setVisibility(fragment == null ? View.GONE : View.VISIBLE);
            }
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
            default:
                return false;
        }
    }

    private void checkFirst() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("first", true)) {
            new DialogBuilderLifecycle(this, this)
                    .setMessage(getString(R.string.title_hint_sync))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit().putBoolean("first", false).apply();
                        }
                    })
                    .show();
        }
    }

    private void checkCrash() {
        new SimpleTask<Long>() {
            @Override
            protected Long onExecute(Context context, Bundle args) throws Throwable {
                File file = new File(context.getCacheDir(), "crash.log");
                if (file.exists()) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        BufferedReader in = null;
                        try {
                            String line;
                            in = new BufferedReader(new FileReader(file));
                            while ((line = in.readLine()) != null)
                                sb.append(line).append("\r\n");
                        } finally {
                            if (in != null)
                                in.close();
                        }

                        return Helper.getDebugInfo(context, R.string.title_crash_info_remark, null, sb.toString()).id;
                    } finally {
                        file.delete();
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Long id) {
                if (id != null)
                    startActivity(
                            new Intent(ActivityView.this, ActivityCompose.class)
                                    .putExtra("action", "edit")
                                    .putExtra("id", id));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(getVisibleView(), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Toast.makeText(ActivityView.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.execute(this, new Bundle());
    }

    private class UpdateInfo {
        String tag_name; // version
        String html_url;
    }

    private void checkUpdate() {
        long now = new Date().getTime();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("updates", true))
            return;
        if (prefs.getLong("last_update_check", 0) + UPDATE_INTERVAL > now)
            return;
        prefs.edit().putLong("last_update_check", now).apply();

        new SimpleTask<UpdateInfo>() {
            @Override
            protected UpdateInfo onExecute(Context context, Bundle args) throws Throwable {
                StringBuilder json = new StringBuilder();
                HttpsURLConnection urlConnection = null;
                try {
                    URL latest = new URL(BuildConfig.GITHUB_LATEST_API);
                    urlConnection = (HttpsURLConnection) latest.openConnection();
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String line;
                    while ((line = br.readLine()) != null)
                        json.append(line);

                    JSONObject jroot = new JSONObject(json.toString());

                    if (jroot.has("tag_name") &&
                            jroot.has("html_url") &&
                            jroot.has("assets")) {
                        // Get update info
                        UpdateInfo info = new UpdateInfo();
                        info.tag_name = jroot.getString("tag_name");
                        info.html_url = jroot.getString("html_url");
                        if (TextUtils.isEmpty(info.html_url))
                            return null;

                        // Check if new release
                        JSONArray jassets = jroot.getJSONArray("assets");
                        for (int i = 0; i < jassets.length(); i++) {
                            JSONObject jasset = jassets.getJSONObject(i);
                            if (jasset.has("name")) {
                                String name = jasset.getString("name");
                                if (name != null && name.endsWith(".apk")) {
                                    if (TextUtils.isEmpty(info.tag_name))
                                        info.tag_name = name;

                                    Log.i("Latest version=" + info.tag_name);
                                    if (BuildConfig.VERSION_NAME.equals(info.tag_name))
                                        break;
                                    else
                                        return info;
                                }
                            }
                        }
                    }

                    return null;
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }

            @Override
            protected void onExecuted(Bundle args, UpdateInfo info) {
                if (info == null)
                    return;

                final Intent update = new Intent(Intent.ACTION_VIEW, Uri.parse(info.html_url));
                if (update.resolveActivity(getPackageManager()) != null)
                    new DialogBuilderLifecycle(ActivityView.this, ActivityView.this)
                            .setMessage(getString(R.string.title_updated, info.tag_name))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Helper.view(ActivityView.this, ActivityView.this, update);
                                }
                            })
                            .show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (BuildConfig.DEBUG)
                    Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
            }
        }.execute(this, new Bundle());
    }

    private void updateShortcuts() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1)
            return;

        ShortcutManager sm = (ShortcutManager) getSystemService(Context.SHORTCUT_SERVICE);

        List<ShortcutInfo> shortcuts = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = null;
            try {
                // https://developer.android.com/guide/topics/providers/contacts-provider#ObsoleteData
                cursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        new String[]{
                                ContactsContract.RawContacts._ID,
                                ContactsContract.Contacts.LOOKUP_KEY,
                                ContactsContract.Contacts.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Email.DATA,
                                ContactsContract.Contacts.STARRED,
                                ContactsContract.Contacts.TIMES_CONTACTED,
                                ContactsContract.Contacts.LAST_TIME_CONTACTED
                        },
                        ContactsContract.CommonDataKinds.Email.DATA + " <> ''",
                        null,
                        ContactsContract.Contacts.STARRED + " DESC" +
                                ", " + ContactsContract.Contacts.TIMES_CONTACTED + " DESC" +
                                ", " + ContactsContract.Contacts.LAST_TIME_CONTACTED + " DESC");
                while (cursor != null && cursor.moveToNext())
                    try {
                        long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        int starred = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED));
                        int times = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED));
                        long last = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED));

                        InternetAddress address = new InternetAddress(email, name);
                        Log.i("Shortcut id=" + id + " address=" + address +
                                " starred=" + starred + " times=" + times + " last=" + last);

                        if (starred == 0 && times == 0 && last == 0)
                            continue;

                        Uri uri = ContactsContract.Contacts.getLookupUri(
                                cursor.getLong(cursor.getColumnIndex(ContactsContract.RawContacts._ID)),
                                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
                        InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                                getContentResolver(), uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        Icon icon = (bitmap == null
                                ? Icon.createWithResource(this, R.drawable.ic_shortcut_email)
                                : Icon.createWithBitmap(bitmap));

                        Intent intent = new Intent(this, ActivityCompose.class);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setData(Uri.parse("mailto:" + address));

                        shortcuts.add(
                                new ShortcutInfo.Builder(this, Long.toString(id))
                                        .setIcon(icon)
                                        .setRank(shortcuts.size() + 1)
                                        .setShortLabel(name)
                                        .setIntent(intent)
                                        .build());

                        if (sm.getManifestShortcuts().size() + shortcuts.size() >= sm.getMaxShortcutCountPerActivity())
                            break;
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }

        sm.setDynamicShortcuts(shortcuts);
    }

    private Intent getIntentFAQ() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/M66B/open-source-email/blob/master/FAQ.md"));
        return intent;
    }

    private Intent getIntentRate() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        if (intent.resolveActivity(getPackageManager()) == null)
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
        return intent;
    }

    private Intent getIntentInvite() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.title_try) + "\n\nhttps://email.faircode.eu/\n\n");
        return intent;
    }

    private Intent getIntentOtherApps() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/dev?id=8420080860664580239"));
        return intent;
    }

    private void onMenuFolders(long account) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("unified", 0);

        Bundle args = new Bundle();
        args.putLong("account", account);

        FragmentFolders fragment = new FragmentFolders();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folders");
        fragmentTransaction.commit();
    }

    private void onMenuInbox(long account) {
        Bundle args = new Bundle();
        args.putLong("account", account);

        new SimpleTask<Long>() {
            @Override
            protected Long onExecute(Context context, Bundle args) {
                long account = args.getLong("account");
                return DB.getInstance(context).folder().getFolderByType(account, EntityFolder.INBOX).id;
            }

            @Override
            protected void onExecuted(Bundle args, Long folder) {
                long account = args.getLong("account");

                getSupportFragmentManager().popBackStack("unified", 0);

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ActivityView.this);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                .putExtra("account", account)
                                .putExtra("folder", folder));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
            }
        }.execute(this, args);
    }

    private void onMenuSetup() {
        startActivity(new Intent(ActivityView.this, ActivitySetup.class));
    }

    private void onMenuAnswers() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAnswers()).addToBackStack("answers");
        fragmentTransaction.commit();
    }

    private void onMenuOperations() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentOperations()).addToBackStack("operations");
        fragmentTransaction.commit();
    }

    private void onMenuLegend() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLegend()).addToBackStack("legend");
        fragmentTransaction.commit();
    }

    private void onMenuFAQ() {
        Helper.view(this, this, getIntentFAQ());
    }

    private void onMenuPro() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
        fragmentTransaction.commit();
    }

    private void onMenuPrivacy() {
        Helper.view(this, this, Helper.getIntentPrivacy());
    }

    private void onMenuAbout() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAbout()).addToBackStack("about");
        fragmentTransaction.commit();
    }

    private void onMenuRate() {
        Intent faq = getIntentFAQ();
        if (faq.resolveActivity(getPackageManager()) == null)
            Helper.view(this, this, getIntentRate());
        else {
            new DialogBuilderLifecycle(this, this)
                    .setMessage(R.string.title_issue)
                    .setPositiveButton(R.string.title_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Helper.view(ActivityView.this, ActivityView.this, getIntentFAQ());
                        }
                    })
                    .setNegativeButton(R.string.title_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Helper.view(ActivityView.this, ActivityView.this, getIntentRate());
                        }
                    })
                    .show();
        }
    }

    private void onMenuInvite() {
        startActivity(getIntentInvite());
    }

    private void onMenuOtherApps() {
        Helper.view(this, this, getIntentOtherApps());
    }

    private void onReload() {
        ServiceSynchronize.reload(this, "manual reload");
    }

    private void onDebugInfo() {
        new SimpleTask<Long>() {
            @Override
            protected Long onExecute(Context context, Bundle args) throws IOException {
                return Helper.getDebugInfo(context, R.string.title_debug_info_remark, null, null).id;
            }

            @Override
            protected void onExecuted(Bundle args, Long id) {
                startActivity(new Intent(ActivityView.this, ActivityCompose.class)
                        .putExtra("action", "edit")
                        .putExtra("id", id));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(getVisibleView(), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Toast.makeText(ActivityView.this, ex.toString(), Toast.LENGTH_LONG).show();
            }

        }.execute(this, new Bundle());
    }

    private void onCleanup() {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                JobDaily.cleanup(ActivityView.this);
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
            }
        }.execute(this, new Bundle());
    }

    private void onShowLog() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("logs", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLogs()).addToBackStack("logs");
        fragmentTransaction.commit();
    }

    private class DrawerItem {
        private int layout;
        private int id;
        private int icon;
        private Integer color;
        private String title;
        private boolean highlight;
        private Object data;

        DrawerItem(int layout) {
            this.id = 0;
            this.layout = layout;
        }

        DrawerItem(Context context, int layout, int icon, int title) {
            this.layout = layout;
            this.id = title;
            this.icon = icon;
            this.title = context.getString(title);
        }

        DrawerItem(int layout, int id, int icon, Integer color, String title, boolean highlight, Object data) {
            this.layout = layout;
            this.id = id;
            this.icon = icon;
            this.color = color;
            this.title = title;
            this.highlight = highlight;
            this.data = data;
        }

        public int getId() {
            return this.id;
        }

        public Object getData() {
            return this.data;
        }
    }

    private static class ArrayAdapterDrawer extends ArrayAdapter<DrawerItem> {
        ArrayAdapterDrawer(@NonNull Context context) {
            super(context, -1);
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            DrawerItem item = getItem(position);
            View row = LayoutInflater.from(getContext()).inflate(item.layout, null);

            ImageView iv = row.findViewById(R.id.ivItem);
            TextView tv = row.findViewById(R.id.tvItem);

            if (iv != null) {
                iv.setImageResource(item.icon);
                if (item.color != null)
                    iv.setColorFilter(item.color);
            }

            if (tv != null) {
                tv.setText(item.title);

                tv.setTextColor(Helper.resolveColor(getContext(), item.highlight ? R.attr.colorUnread : android.R.attr.textColorSecondary
                ));
            }

            return row;
        }

        @Override
        public boolean isEnabled(int position) {
            DrawerItem item = getItem(position);
            return (item != null && item.id != 0);
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                String action = intent.getAction();

                if (ACTION_VIEW_MESSAGES.equals(action))
                    onViewMessages(intent);
                else if (ACTION_VIEW_THREAD.equals(action))
                    onViewThread(intent);
                else if (ACTION_VIEW_FULL.equals(action))
                    onViewFull(intent);
                else if (ACTION_EDIT_FOLDER.equals(action))
                    onEditFolder(intent);
                else if (ACTION_EDIT_ANSWER.equals(action))
                    onEditAnswer(intent);
                else if (ACTION_STORE_ATTACHMENT.equals(action))
                    onStoreAttachment(intent);
                else if (ACTION_DECRYPT.equals(action))
                    onDecrypt(intent);
                else if (ACTION_SHOW_PRO.equals(action))
                    onShowPro(intent);
            }
        }
    };

    private void onViewMessages(Intent intent) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("messages", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("account", intent.getLongExtra("account", -1));
        args.putLong("folder", intent.getLongExtra("folder", -1));
        args.putBoolean("outgoing", intent.getBooleanExtra("outgoing", false));

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("messages");
        fragmentTransaction.commit();
    }

    private void onViewThread(Intent intent) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("account", intent.getLongExtra("account", -1));
        args.putString("thread", intent.getStringExtra("thread"));
        args.putLong("id", intent.getLongExtra("id", -1));

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        int pane;
        if (grpPane == null)
            pane = R.id.content_frame;
        else {
            pane = R.id.content_pane;
            grpPane.setVisibility(View.VISIBLE);
            args.putBoolean("pane", true);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(pane, fragment).addToBackStack("thread");
        fragmentTransaction.commit();
    }

    private void onViewFull(Intent intent) {
        FragmentWebView fragment = new FragmentWebView();
        fragment.setArguments(intent.getExtras());

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("webview");
        fragmentTransaction.commit();
    }

    private void onEditFolder(Intent intent) {
        FragmentFolder fragment = new FragmentFolder();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folder");
        fragmentTransaction.commit();
    }

    private void onEditAnswer(Intent intent) {
        FragmentAnswer fragment = new FragmentAnswer();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("answer");
        fragmentTransaction.commit();
    }

    private void onStoreAttachment(Intent intent) {
        attachment = intent.getLongExtra("id", -1);
        Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        create.addCategory(Intent.CATEGORY_OPENABLE);
        create.setType(intent.getStringExtra("type"));
        create.putExtra(Intent.EXTRA_TITLE, intent.getStringExtra("name"));
        if (create.resolveActivity(getPackageManager()) == null)
            Snackbar.make(getVisibleView(), R.string.title_no_saf, Snackbar.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(this, create), REQUEST_ATTACHMENT);
    }

    private void onDecrypt(Intent intent) {
        if (Helper.isPro(this)) {
            if (pgpService.isBound()) {
                Intent data = new Intent();
                data.setAction(OpenPgpApi.ACTION_DECRYPT_VERIFY);

                decrypt(data, intent.getLongExtra("id", -1));
            } else {
                Snackbar snackbar = Snackbar.make(getVisibleView(), R.string.title_no_openpgp, Snackbar.LENGTH_LONG);
                if (Helper.getIntentOpenKeychain().resolveActivity(getPackageManager()) != null)
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(Helper.getIntentOpenKeychain());
                        }
                    });
                snackbar.show();
            }
        } else
            onShowPro(intent);
    }

    private void onShowPro(Intent intent) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
        fragmentTransaction.commit();
    }

    private void decrypt(Intent data, long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putParcelable("data", data);

        new SimpleTask<PendingIntent>() {
            @Override
            protected PendingIntent onExecute(Context context, Bundle args) throws Throwable {
                // Get arguments
                long id = args.getLong("id");
                Intent data = args.getParcelable("data");

                DB db = DB.getInstance(context);

                boolean inline = false;
                InputStream encrypted = null;

                // Find encrypted data
                List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                for (EntityAttachment attachment : attachments)
                    if ("encrypted.asc".equals(attachment.name)) {
                        if (!attachment.available)
                            throw new IllegalArgumentException(getString(R.string.title_attachments_missing));

                        encrypted = new FileInputStream(EntityAttachment.getFile(context, attachment.id));
                        break;
                    }

                if (encrypted == null) {
                    EntityMessage message = db.message().getMessage(id);
                    String body = message.read(context);

                    // https://tools.ietf.org/html/rfc4880#section-6.2
                    int begin = body.indexOf(PGP_BEGIN_MESSAGE);
                    int end = body.indexOf(PGP_END_MESSAGE);
                    if (begin >= 0 && begin < end) {
                        String section = body.substring(begin, end + PGP_END_MESSAGE.length());
                        String[] lines = section.split("<br />");
                        List<String> disarmored = new ArrayList<>();
                        for (String line : lines)
                            if (!TextUtils.isEmpty(line) && !line.contains(": "))
                                disarmored.add(line);
                        section = TextUtils.join("\n\r", disarmored);

                        inline = true;
                        encrypted = new ByteArrayInputStream(section.getBytes());
                    }
                }

                if (encrypted == null)
                    throw new IllegalArgumentException(getString(R.string.title_not_encrypted));

                ByteArrayOutputStream decrypted = new ByteArrayOutputStream();

                // Decrypt message
                OpenPgpApi api = new OpenPgpApi(context, pgpService.getService());
                Intent result = api.executeApi(data, encrypted, decrypted);

                Log.i("PGP result=" + result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR));
                switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
                    case OpenPgpApi.RESULT_CODE_SUCCESS:
                        if (inline) {
                            try {
                                db.beginTransaction();

                                // Write decrypted body
                                EntityMessage m = db.message().getMessage(id);
                                m.write(context, decrypted.toString());

                                db.message().setMessageStored(id, new Date().getTime());

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                        } else {
                            // Decode message
                            Properties props = MessageHelper.getSessionProperties(Helper.AUTH_TYPE_PASSWORD, false);
                            Session isession = Session.getInstance(props, null);
                            ByteArrayInputStream is = new ByteArrayInputStream(decrypted.toByteArray());
                            MimeMessage imessage = new MimeMessage(isession, is);
                            MessageHelper helper = new MessageHelper(imessage);

                            try {
                                db.beginTransaction();

                                // Write decrypted body
                                EntityMessage m = db.message().getMessage(id);
                                m.write(context, helper.getHtml());

                                // Remove previously decrypted attachments
                                for (EntityAttachment a : attachments)
                                    if (!"encrypted.asc".equals(a.name))
                                        db.attachment().deleteAttachment(a.id);

                                // Add decrypted attachments
                                int sequence = db.attachment().getAttachmentSequence(id);
                                for (EntityAttachment a : helper.getAttachments()) {
                                    a.message = id;
                                    a.sequence = ++sequence;
                                    a.id = db.attachment().insertAttachment(a);
                                }

                                db.message().setMessageStored(id, new Date().getTime());

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }
                        }

                        break;

                    case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
                        message = id;
                        return result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);

                    case OpenPgpApi.RESULT_CODE_ERROR:
                        OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                        throw new IllegalArgumentException(error.getMessage());
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, PendingIntent pi) {
                if (pi != null)
                    try {
                        Log.i("PGP executing pi=" + pi);
                        startIntentSenderForResult(
                                pi.getIntentSender(),
                                ActivityView.REQUEST_DECRYPT,
                                null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException ex) {
                        Log.e(ex);
                        Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
                    }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(getVisibleView(), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
            }
        }.execute(ActivityView.this, args);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == REQUEST_ATTACHMENT) {
                if (data != null) {
                    Bundle args = new Bundle();
                    args.putLong("id", attachment);
                    args.putParcelable("uri", data.getData());

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                            long id = args.getLong("id");
                            Uri uri = args.getParcelable("uri");

                            if ("file".equals(uri.getScheme()))
                                throw new IllegalArgumentException(context.getString(R.string.title_no_stream));

                            File file = EntityAttachment.getFile(context, id);

                            ParcelFileDescriptor pfd = null;
                            FileOutputStream fos = null;
                            FileInputStream fis = null;
                            try {
                                pfd = context.getContentResolver().openFileDescriptor(uri, "w");
                                fos = new FileOutputStream(pfd.getFileDescriptor());
                                fis = new FileInputStream(file);

                                byte[] buffer = new byte[ATTACHMENT_BUFFER_SIZE];
                                int read;
                                while ((read = fis.read(buffer)) != -1)
                                    fos.write(buffer, 0, read);
                            } finally {
                                try {
                                    if (pfd != null)
                                        pfd.close();
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }
                                try {
                                    if (fos != null)
                                        fos.close();
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }
                                try {
                                    if (fis != null)
                                        fis.close();
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }
                            }

                            return null;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Void data) {
                            Toast.makeText(ActivityView.this, R.string.title_attachment_saved, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            if (ex instanceof IllegalArgumentException)
                                Snackbar.make(getVisibleView(), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                            else
                                Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
                        }
                    }.execute(this, args);
                }
            } else if (requestCode == REQUEST_DECRYPT) {
                if (data != null)
                    decrypt(data, message);
            }
    }
}
