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
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.constraintlayout.widget.Group;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.HttpsURLConnection;

import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_NO_SIGNATURE;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_VALID_KEY_CONFIRMED;

public class ActivityView extends ActivityBilling implements FragmentManager.OnBackStackChangedListener {
    private String startup;

    private View view;
    private Group grpPane;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ScrollView drawerContainer;
    private RecyclerView rvAccount;
    private RecyclerView rvFolder;
    private RecyclerView rvMenu;
    private ImageView ivExpander;
    private RecyclerView rvMenuExtra;

    private long message = -1;
    private long attachment = -1;

    private WebView printWebView = null;
    private OpenPgpServiceConnection pgpService;

    static final int REQUEST_UNIFIED = 1;
    static final int REQUEST_WHY = 2;
    static final int REQUEST_THREAD = 3;
    static final int REQUEST_OUTBOX = 4;
    static final int REQUEST_ERROR = 5;

    static final int REQUEST_RAW = 1;
    static final int REQUEST_ATTACHMENT = 2;
    static final int REQUEST_ATTACHMENTS = 3;
    static final int REQUEST_DECRYPT = 4;
    static final int REQUEST_SENDER = 5;
    static final int REQUEST_RECIPIENT = 6;

    static final String ACTION_VIEW_FOLDERS = BuildConfig.APPLICATION_ID + ".VIEW_FOLDERS";
    static final String ACTION_VIEW_MESSAGES = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGES";
    static final String ACTION_SEARCH = BuildConfig.APPLICATION_ID + ".SEARCH";
    static final String ACTION_VIEW_THREAD = BuildConfig.APPLICATION_ID + ".VIEW_THREAD";
    static final String ACTION_STORE_RAW = BuildConfig.APPLICATION_ID + ".STORE_RAW";
    static final String ACTION_EDIT_FOLDER = BuildConfig.APPLICATION_ID + ".EDIT_FOLDER";
    static final String ACTION_EDIT_ANSWERS = BuildConfig.APPLICATION_ID + ".EDIT_ANSWERS";
    static final String ACTION_EDIT_ANSWER = BuildConfig.APPLICATION_ID + ".EDIT_ANSWER";
    static final String ACTION_EDIT_RULES = BuildConfig.APPLICATION_ID + ".EDIT_RULES";
    static final String ACTION_EDIT_RULE = BuildConfig.APPLICATION_ID + ".EDIT_RULE";
    static final String ACTION_STORE_ATTACHMENT = BuildConfig.APPLICATION_ID + ".STORE_ATTACHMENT";
    static final String ACTION_STORE_ATTACHMENTS = BuildConfig.APPLICATION_ID + ".STORE_ATTACHMENTS";
    static final String ACTION_COLOR = BuildConfig.APPLICATION_ID + ".COLOR";
    static final String ACTION_PRINT = BuildConfig.APPLICATION_ID + ".PRINT";
    static final String ACTION_DECRYPT = BuildConfig.APPLICATION_ID + ".DECRYPT";
    static final String ACTION_SHOW_PRO = BuildConfig.APPLICATION_ID + ".SHOW_PRO";

    static final long UPDATE_INTERVAL = (BuildConfig.BETA_RELEASE ? 4 : 12) * 3600 * 1000L; // milliseconds

    private static final String PGP_BEGIN_MESSAGE = "-----BEGIN PGP MESSAGE-----";
    private static final String PGP_END_MESSAGE = "-----END PGP MESSAGE-----";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        startup = prefs.getString("startup", "unified");

        view = LayoutInflater.from(this).inflate(R.layout.activity_view, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        grpPane = findViewById(R.id.grpPane);

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

        rvAccount = drawerContainer.findViewById(R.id.rvAccount);
        rvAccount.setLayoutManager(new LinearLayoutManager(this));
        final AdapterNavAccount aadapter = new AdapterNavAccount(this, this);
        rvAccount.setAdapter(aadapter);

        rvFolder = drawerContainer.findViewById(R.id.rvFolder);
        rvFolder.setLayoutManager(new LinearLayoutManager(this));
        final AdapterNavFolder fadapter = new AdapterNavFolder(this, this);
        rvFolder.setAdapter(fadapter);

        rvMenu = drawerContainer.findViewById(R.id.rvMenu);
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        final AdapterNavMenu madapter = new AdapterNavMenu(this, this);
        rvMenu.setAdapter(madapter);

        ivExpander = drawerContainer.findViewById(R.id.ivExpander);

        rvMenuExtra = drawerContainer.findViewById(R.id.rvMenuExtra);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvMenuExtra.setLayoutManager(llm);
        final AdapterNavMenu eadapter = new AdapterNavMenu(this, this);
        rvMenuExtra.setAdapter(eadapter);

        final Drawable d = getDrawable(R.drawable.divider);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, llm.getOrientation()) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(view);
                NavMenuItem menu = eadapter.get(pos);
                outRect.set(0, 0, 0, menu != null && menu.isSeparated() ? d.getIntrinsicHeight() : 0);
            }
        };
        itemDecorator.setDrawable(d);
        rvMenuExtra.addItemDecoration(itemDecorator);

        boolean minimal = prefs.getBoolean("minimal", false);
        rvMenuExtra.setVisibility(minimal ? View.GONE : View.VISIBLE);
        ivExpander.setImageLevel(minimal ? 1 /* more */ : 0 /* less */);

        ivExpander.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean minimal = !prefs.getBoolean("minimal", false);
                prefs.edit().putBoolean("minimal", minimal).apply();
                rvMenuExtra.setVisibility(minimal ? View.GONE : View.VISIBLE);
                ivExpander.setImageLevel(minimal ? 1 /* more */ : 0 /* less */);
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        PackageManager pm = getPackageManager();
        final List<NavMenuItem> menus = new ArrayList<>();

        final NavMenuItem navOperations = new NavMenuItem(R.drawable.baseline_dns_24, R.string.menu_operations, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuOperations();
            }
        }, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onShowLog();
            }
        });

        menus.add(navOperations);

        menus.add(new NavMenuItem(R.drawable.baseline_reply_24, R.string.menu_answers, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuAnswers();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.baseline_settings_applications_24, R.string.menu_setup, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuSetup();
            }
        }, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onReset();
            }
        }));

        madapter.set(menus);

        List<NavMenuItem> extra = new ArrayList<>();

        extra.add(new NavMenuItem(R.drawable.baseline_help_24, R.string.menu_legend, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuLegend();
            }
        }));

        if (Helper.getIntentFAQ().resolveActivity(pm) != null)
            extra.add(new NavMenuItem(R.drawable.baseline_question_answer_24, R.string.menu_faq, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onMenuFAQ();
                }
            }, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onDebugInfo();
                }
            }));

        if (Helper.getIntentIssue(this).resolveActivity(pm) != null)
            extra.add(new NavMenuItem(R.drawable.baseline_feedback_24, R.string.menu_issue, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onMenuIssue();
                }
            }));

        if (Helper.getIntentPrivacy().resolveActivity(pm) != null)
            extra.add(new NavMenuItem(R.drawable.baseline_account_box_24, R.string.menu_privacy, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onMenuPrivacy();
                }
            }));

        extra.add(new NavMenuItem(R.drawable.baseline_info_24, R.string.menu_about, new Runnable() {
            @Override
            public void run() {
                onMenuAbout();
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (!Helper.isPlayStoreInstall(ActivityView.this)) {
                    drawerLayout.closeDrawer(drawerContainer);
                    checkUpdate(true);
                }
            }
        }).setSeparated());

        if (getIntentPro() == null || getIntentPro().resolveActivity(pm) != null)
            extra.add(new NavMenuItem(R.drawable.baseline_monetization_on_24, R.string.menu_pro, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onShowPro(null);
                }
            }));

        if ((getIntentInvite().resolveActivity(pm) != null))
            extra.add(new NavMenuItem(R.drawable.baseline_people_24, R.string.menu_invite, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onMenuInvite();
                }
            }));

        if (getIntentRate().resolveActivity(pm) != null)
            extra.add(new NavMenuItem(R.drawable.baseline_star_24, R.string.menu_rate, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onMenuRate();
                }
            }));

        if (getIntentOtherApps().resolveActivity(pm) != null)
            extra.add(new NavMenuItem(R.drawable.baseline_get_app_24, R.string.menu_other, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onMenuOtherApps();
                }
            }));

        eadapter.set(extra);

        DB db = DB.getInstance(this);

        db.account().liveAccountsEx(false).observe(this, new Observer<List<TupleAccountEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleAccountEx> accounts) {
                if (accounts == null)
                    accounts = new ArrayList<>();
                aadapter.set(accounts);
            }
        });

        db.folder().liveNavigation().observe(this, new Observer<List<TupleFolderNav>>() {
            @Override
            public void onChanged(List<TupleFolderNav> folders) {
                if (folders == null)
                    folders = new ArrayList<>();
                fadapter.set(folders);
            }
        });

        db.operation().liveStats().observe(this, new Observer<TupleOperationStats>() {
            @Override
            public void onChanged(TupleOperationStats stats) {
                navOperations.setWarning(stats != null && stats.errors != null && stats.errors > 0);
                navOperations.setCount(stats == null ? 0 : stats.pending);
                madapter.notifyDataSetChanged();
            }
        });

        if (getSupportFragmentManager().getFragments().size() == 0 &&
                !getIntent().hasExtra(Intent.EXTRA_PROCESS_TEXT))
            init();

        if (savedInstanceState != null)
            drawerToggle.setDrawerIndicatorEnabled(savedInstanceState.getBoolean("fair:toggle"));

        new Handler().post(checkIntent);

        checkFirst();
        checkCrash();

        pgpService = new OpenPgpServiceConnection(this, "org.sufficientlysecure.keychain");
        pgpService.bindToService();

        Shortcuts.update(this, this);
    }

    private void init() {
        Bundle args = new Bundle();

        FragmentBase fragment;
        switch (startup) {
            case "accounts":
                fragment = new FragmentAccounts();
                args.putBoolean("settings", false);
                break;
            case "folders":
                fragment = new FragmentFolders();
                break;
            default:
                fragment = new FragmentMessages();
        }

        fragment.setArguments(args);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        for (Fragment existing : fm.getFragments())
            fragmentTransaction.remove(existing);
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("unified");
        fragmentTransaction.commit();
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

                if ("unified".equals(action)) {
                    getSupportFragmentManager().popBackStack("unified", 0);

                } else if ("why".equals(action)) {
                    getSupportFragmentManager().popBackStack("unified", 0);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityView.this);
                    boolean why = prefs.getBoolean("why", false);
                    if (!why) {
                        prefs.edit().putBoolean("why", true).apply();

                        Intent iwhy = new Intent(Intent.ACTION_VIEW);
                        iwhy.setData(Uri.parse(Helper.FAQ_URI + "#user-content-faq2"));
                        iwhy.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (iwhy.resolveActivity(getPackageManager()) != null)
                            startActivity(iwhy);
                    }

                } else if ("outbox".equals(action))
                    onMenuOutbox();

                else if ("error".equals(action)) {
                    Intent ifaq = new Intent(Intent.ACTION_VIEW);
                    ifaq.setData(Uri.parse(Helper.FAQ_URI + "#frequently-asked-questions"));
                    ifaq.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (ifaq.resolveActivity(getPackageManager()) != null)
                        startActivity(ifaq);

                } else if (action.startsWith("thread")) {
                    intent.putExtra("thread", action.split(":", 2)[1]);
                    onViewThread(intent);
                }
            }

            if (intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
                String search = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();

                intent.removeExtra(Intent.EXTRA_PROCESS_TEXT);
                setIntent(intent);

                FragmentMessages.search(
                        ActivityView.this, ActivityView.this, getSupportFragmentManager(),
                        -1, false, search);
            }
        }
    };

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
        iff.addAction(ACTION_VIEW_FOLDERS);
        iff.addAction(ACTION_VIEW_MESSAGES);
        iff.addAction(ACTION_SEARCH);
        iff.addAction(ACTION_VIEW_THREAD);
        iff.addAction(ACTION_STORE_RAW);
        iff.addAction(ACTION_EDIT_FOLDER);
        iff.addAction(ACTION_EDIT_ANSWERS);
        iff.addAction(ACTION_EDIT_ANSWER);
        iff.addAction(ACTION_EDIT_RULES);
        iff.addAction(ACTION_EDIT_RULE);
        iff.addAction(ACTION_STORE_ATTACHMENT);
        iff.addAction(ACTION_STORE_ATTACHMENTS);
        iff.addAction(ACTION_COLOR);
        iff.addAction(ACTION_PRINT);
        iff.addAction(ACTION_DECRYPT);
        iff.addAction(ACTION_SHOW_PRO);
        lbm.registerReceiver(receiver, iff);

        if (!pgpService.isBound())
            pgpService.bindToService();

        checkUpdate(false);
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
        if (drawerLayout.isDrawerOpen(drawerContainer))
            drawerLayout.closeDrawer(drawerContainer);
        else
            super.onBackPressed();
    }

    @Override
    public void onBackStackChanged() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0)
            finish();
        else {
            if (drawerLayout.isDrawerOpen(drawerContainer))
                drawerLayout.closeDrawer(drawerContainer);
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
                        String line;
                        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                            while ((line = in.readLine()) != null)
                                sb.append(line).append("\r\n");
                        }

                        return Log.getDebugInfo(context, R.string.title_crash_info_remark, null, sb.toString()).id;
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
        }.execute(this, new Bundle(), "crash:log");
    }

    private class UpdateInfo {
        String tag_name; // version
        boolean prerelease;
        String html_url;
    }

    private void checkUpdate(boolean always) {
        if (Helper.isPlayStoreInstall(this))
            return;

        long now = new Date().getTime();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!always && !prefs.getBoolean("updates", true))
            return;
        if (!always && prefs.getLong("last_update_check", 0) + UPDATE_INTERVAL > now)
            return;
        prefs.edit().putLong("last_update_check", now).apply();

        Bundle args = new Bundle();
        args.putBoolean("always", always);

        new SimpleTask<UpdateInfo>() {
            @Override
            protected UpdateInfo onExecute(Context context, Bundle args) throws Throwable {
                StringBuilder response = new StringBuilder();
                HttpsURLConnection urlConnection = null;
                try {
                    URL latest = new URL(BuildConfig.GITHUB_LATEST_API);
                    urlConnection = (HttpsURLConnection) latest.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(false);
                    urlConnection.connect();

                    int status = urlConnection.getResponseCode();
                    InputStream inputStream = (status == HttpsURLConnection.HTTP_OK
                            ? urlConnection.getInputStream() : urlConnection.getErrorStream());

                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = br.readLine()) != null)
                        response.append(line);

                    if (status == HttpsURLConnection.HTTP_FORBIDDEN) {
                        // {"message":"API rate limit exceeded for ...","documentation_url":"https://developer.github.com/v3/#rate-limiting"}
                        JSONObject jmessage = new JSONObject(response.toString());
                        if (jmessage.has("message"))
                            throw new IllegalArgumentException(jmessage.getString("message"));
                        throw new IOException("HTTP " + status + ": " + response.toString());
                    }
                    if (status != HttpsURLConnection.HTTP_OK)
                        throw new IOException("HTTP " + status + ": " + response.toString());

                    JSONObject jroot = new JSONObject(response.toString());

                    if (!jroot.has("tag_name"))
                        throw new IOException("tag_name field missing");
                    if (!jroot.has("html_url"))
                        throw new IOException("html_url field missing");
                    if (!jroot.has("assets"))
                        throw new IOException("assets section missing");

                    // Get update info
                    UpdateInfo info = new UpdateInfo();
                    info.tag_name = jroot.getString("tag_name");
                    info.prerelease = jroot.getBoolean("prerelease");
                    info.html_url = jroot.getString("html_url");

                    // Check if new release
                    JSONArray jassets = jroot.getJSONArray("assets");
                    for (int i = 0; i < jassets.length(); i++) {
                        JSONObject jasset = jassets.getJSONObject(i);
                        if (jasset.has("name")) {
                            String name = jasset.getString("name");
                            if (name != null && name.endsWith(".apk")) {
                                Log.i("Latest version=" + info.tag_name + " prerelease=" + info.prerelease);
                                if (BuildConfig.VERSION_NAME.equals(info.tag_name))
                                    return null;
                                else
                                    return info;
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
                boolean always = args.getBoolean("always");
                if (info == null) {
                    if (always) {
                        Toast toast = Toast.makeText(ActivityView.this, BuildConfig.VERSION_NAME, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    return;
                }

                if (!always && info.prerelease)
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
                if (args.getBoolean("always"))
                    if (ex instanceof IllegalArgumentException || ex instanceof IOException)
                        Snackbar.make(getVisibleView(), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                    else
                        Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
            }
        }.execute(this, args, "update:check");
    }

    private Intent getIntentInvite() {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.title_try)).append("\n\n");
        sb.append(BuildConfig.INVITE_URI).append("\n\n");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        return intent;
    }

    private Intent getIntentRate() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        if (intent.resolveActivity(getPackageManager()) == null)
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
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

    private void onMenuOutbox() {
        Bundle args = new Bundle();

        new SimpleTask<Long>() {
            @Override
            protected Long onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                EntityFolder outbox = db.folder().getOutbox();
                return (outbox == null ? -1 : outbox.id);
            }

            @Override
            protected void onExecuted(Bundle args, Long folder) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                    getSupportFragmentManager().popBackStack("unified", 0);

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ActivityView.this);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                .putExtra("account", -1L)
                                .putExtra("folder", folder));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
            }
        }.execute(this, args, "menu:inbox");
    }

    private void onMenuOperations() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("operations", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentOperations()).addToBackStack("operations");
        fragmentTransaction.commit();
    }

    private void onMenuAnswers() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("answers", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAnswers()).addToBackStack("answers");
        fragmentTransaction.commit();
    }

    private void onMenuSetup() {
        startActivity(new Intent(ActivityView.this, ActivitySetup.class));
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

    private void onMenuIssue() {
        startActivity(Helper.getIntentIssue(this));
    }

    private void onMenuPrivacy() {
        Helper.view(this, this, Helper.getIntentPrivacy());
    }

    private void onMenuAbout() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("about", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAbout()).addToBackStack("about");
        fragmentTransaction.commit();
    }

    private void onMenuInvite() {
        startActivity(getIntentInvite());
    }

    private void onMenuRate() {
        Intent faq = Helper.getIntentFAQ();
        if (faq.resolveActivity(getPackageManager()) == null)
            Helper.view(this, this, getIntentRate());
        else {
            new DialogBuilderLifecycle(this, this)
                    .setMessage(R.string.title_issue)
                    .setPositiveButton(R.string.title_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Helper.view(ActivityView.this, ActivityView.this, Helper.getIntentFAQ());
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

    private void onMenuOtherApps() {
        Helper.view(this, this, getIntentOtherApps());
    }

    private void onReset() {
        ServiceSynchronize.reset(this);
    }

    private void onDebugInfo() {
        new SimpleTask<Long>() {
            @Override
            protected Long onExecute(Context context, Bundle args) throws IOException {
                return Log.getDebugInfo(context, R.string.title_debug_info_remark, null, null).id;
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

        }.execute(this, new Bundle(), "debug:info");
    }

    private void onShowLog() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("logs", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLogs()).addToBackStack("logs");
        fragmentTransaction.commit();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                String action = intent.getAction();

                if (ACTION_VIEW_FOLDERS.equals(action))
                    onViewFolders(intent);
                else if (ACTION_VIEW_MESSAGES.equals(action))
                    onViewMessages(intent);
                else if (ACTION_SEARCH.equals(action))
                    onSearchMessages(intent);
                else if (ACTION_VIEW_THREAD.equals(action))
                    onViewThread(intent);
                else if (ACTION_STORE_RAW.equals(action))
                    onStoreRaw(intent);
                else if (ACTION_EDIT_FOLDER.equals(action))
                    onEditFolder(intent);
                else if (ACTION_EDIT_ANSWERS.equals(action))
                    onEditAnswers(intent);
                else if (ACTION_EDIT_ANSWER.equals(action))
                    onEditAnswer(intent);
                else if (ACTION_EDIT_RULES.equals(action))
                    onEditRules(intent);
                else if (ACTION_EDIT_RULE.equals(action))
                    onEditRule(intent);
                else if (ACTION_STORE_ATTACHMENT.equals(action))
                    onStoreAttachment(intent);
                else if (ACTION_STORE_ATTACHMENTS.equals(action))
                    onStoreAttachments(intent);
                else if (ACTION_COLOR.equals(action))
                    onColor(intent);
                else if (ACTION_PRINT.equals(action))
                    onPrint(intent);
                else if (ACTION_DECRYPT.equals(action))
                    onDecrypt(intent);
                else if (ACTION_SHOW_PRO.equals(action))
                    onShowPro(intent);
            }
        }
    };

    private void onViewFolders(Intent intent) {
        long account = intent.getLongExtra("id", -1);
        onMenuFolders(account);
    }

    private void onViewMessages(Intent intent) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("messages", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("account", intent.getLongExtra("account", -1));
        args.putLong("folder", intent.getLongExtra("folder", -1));

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("messages");
        fragmentTransaction.commit();
    }

    private void onSearchMessages(Intent intent) {
        long folder = intent.getLongExtra("folder", -1);
        String query = intent.getStringExtra("query");
        FragmentMessages.search(
                this, this, getSupportFragmentManager(),
                folder, false, query);
    }

    private void onViewThread(Intent intent) {
        boolean found = intent.getBooleanExtra("found", false);

        if (!found && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("account", intent.getLongExtra("account", -1));
        args.putString("thread", intent.getStringExtra("thread"));
        args.putLong("id", intent.getLongExtra("id", -1));
        args.putBoolean("found", found);

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

    private void onStoreRaw(Intent intent) {
        message = intent.getLongExtra("id", -1);
        Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        create.addCategory(Intent.CATEGORY_OPENABLE);
        create.setType("*/*");
        create.putExtra(Intent.EXTRA_TITLE, "email.eml");
        if (create.resolveActivity(getPackageManager()) == null)
            Snackbar.make(getVisibleView(), R.string.title_no_saf, Snackbar.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(this, create), REQUEST_RAW);
    }

    private void onEditFolder(Intent intent) {
        FragmentFolder fragment = new FragmentFolder();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folder");
        fragmentTransaction.commit();
    }

    private void onEditAnswers(Intent intent) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAnswers()).addToBackStack("answers");
        fragmentTransaction.commit();
    }

    private void onEditAnswer(Intent intent) {
        FragmentAnswer fragment = new FragmentAnswer();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("answer");
        fragmentTransaction.commit();
    }

    private void onEditRules(Intent intent) {
        FragmentRules fragment = new FragmentRules();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("rules");
        fragmentTransaction.commit();
    }

    private void onEditRule(Intent intent) {
        FragmentRule fragment = new FragmentRule();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("rule");
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

    private void onStoreAttachments(Intent intent) {
        message = intent.getLongExtra("id", -1);
        Intent tree = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        //tree.putExtra("android.content.extra.SHOW_ADVANCED", true);
        if (tree.resolveActivity(getPackageManager()) == null)
            Snackbar.make(getVisibleView(), R.string.title_no_saf, Snackbar.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(this, tree), REQUEST_ATTACHMENTS);
    }

    private void onColor(final Intent intent) {
        int color = intent.getIntExtra("color", -1);
        int[] colors = getResources().getIntArray(R.array.colorPicker);
        ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
        colorPickerDialog.initialize(R.string.title_flag_color, colors, color, 4, colors.length);
        colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                if (!Helper.isPro(ActivityView.this)) {
                    onShowPro(null);
                    return;
                }

                Bundle args = new Bundle();
                args.putLong("id", intent.getLongExtra("id", -1));
                args.putInt("color", color);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(final Context context, Bundle args) {
                        final long id = args.getLong("id");
                        final int color = args.getInt("color");

                        final DB db = DB.getInstance(context);
                        db.runInTransaction(new Runnable() {
                            @Override
                            public void run() {
                                EntityMessage message = db.message().getMessage(id);
                                if (message == null)
                                    return;

                                EntityOperation.queue(context, message, EntityOperation.FLAG, true, color);
                            }
                        });
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
                    }
                }.execute(ActivityView.this, ActivityView.this, args, "message:color");
            }
        });
        colorPickerDialog.show(getSupportFragmentManager(), "colorpicker");
    }

    private void onPrint(Intent intent) {
        long id = intent.getLongExtra("id", -1);

        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<String[]>() {
            @Override
            protected String[] onExecute(Context context, Bundle args) throws IOException {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null || !message.content)
                    return null;

                File file = message.getFile(context);
                if (!file.exists())
                    return null;

                String html = Helper.readText(file);
                html = HtmlHelper.getHtmlEmbedded(context, id, html);

                return new String[]{message.subject, html};
            }

            @Override
            protected void onExecuted(Bundle args, final String[] data) {
                if (data == null)
                    return;

                // https://developer.android.com/training/printing/html-docs.html
                printWebView = new WebView(ActivityView.this);
                WebSettings settings = printWebView.getSettings();
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                settings.setAllowFileAccess(false);

                printWebView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return false;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        try {
                            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                            String jobName = getString(R.string.app_name);
                            if (!TextUtils.isEmpty(data[0]))
                                jobName += " - " + data[0];
                            PrintDocumentAdapter adapter = printWebView.createPrintDocumentAdapter(jobName);
                            printManager.print(jobName, adapter, new PrintAttributes.Builder().build());
                        } catch (Throwable ex) {
                            Log.e(ex);
                        } finally {
                            printWebView = null;
                        }
                    }
                });

                printWebView.loadDataWithBaseURL("about:blank", data[1], "text/html", "UTF-8", null);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
            }
        }.execute(ActivityView.this, args, "message:print");
    }

    private void onDecrypt(Intent intent) {
        if (!Helper.isPro(this)) {
            onShowPro(intent);
            return;
        }

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
    }

    private void onShowPro(Intent intent) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("pro", FragmentManager.POP_BACK_STACK_INCLUSIVE);

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
                    if (EntityAttachment.PGP_MESSAGE.equals(attachment.encryption)) {
                        if (!attachment.available)
                            throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));

                        File file = attachment.getFile(context);
                        encrypted = new BufferedInputStream(new FileInputStream(file));
                        break;
                    }

                if (encrypted == null) {
                    EntityMessage message = db.message().getMessage(id);
                    if (message != null && message.content) {
                        File file = message.getFile(context);
                        if (file.exists()) {
                            // https://tools.ietf.org/html/rfc4880#section-6.2
                            String body = Helper.readText(file);
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
                    }
                }

                if (encrypted == null)
                    throw new IllegalArgumentException(context.getString(R.string.title_not_encrypted));

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
                                Helper.writeText(m.getFile(context), decrypted.toString());

                                db.message().setMessageStored(id, new Date().getTime());

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                        } else {
                            // Decode message
                            Properties props = MessageHelper.getSessionProperties(
                                    ConnectionHelper.AUTH_TYPE_PASSWORD, null, false);
                            Session isession = Session.getInstance(props, null);
                            ByteArrayInputStream is = new ByteArrayInputStream(decrypted.toByteArray());
                            MimeMessage imessage = new MimeMessage(isession, is);
                            MessageHelper helper = new MessageHelper(imessage);
                            MessageHelper.MessageParts parts = helper.getMessageParts();

                            try {
                                db.beginTransaction();

                                // Write decrypted body
                                EntityMessage m = db.message().getMessage(id);
                                Helper.writeText(m.getFile(context), parts.getHtml(context));

                                // Remove previously decrypted attachments
                                for (EntityAttachment local : attachments)
                                    if (local.encryption == null)
                                        db.attachment().deleteAttachment(local.id);

                                int sequence = db.attachment().getAttachmentSequence(id);

                                // Add decrypted attachments
                                List<EntityAttachment> remotes = parts.getAttachments();
                                for (int index = 0; index < remotes.size(); index++) {
                                    EntityAttachment remote = remotes.get(index);
                                    remote.message = id;
                                    remote.sequence = ++sequence;
                                    remote.id = db.attachment().insertAttachment(remote);
                                    try {
                                        parts.downloadAttachment(context, index, remote.id, remote.name);
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                                }

                                db.message().setMessageStored(id, new Date().getTime());

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }
                        }

                        // Check signature status
                        OpenPgpSignatureResult sigResult = result.getParcelableExtra(OpenPgpApi.RESULT_SIGNATURE);
                        int sresult = (sigResult == null ? RESULT_NO_SIGNATURE : sigResult.getResult());
                        int resid;
                        if (sresult == RESULT_NO_SIGNATURE)
                            resid = R.string.title_signature_none;
                        else if (sresult == RESULT_VALID_KEY_CONFIRMED)
                            resid = R.string.title_signature_valid;
                        else
                            resid = R.string.title_signature_invalid;
                        Snackbar.make(getVisibleView(), resid, Snackbar.LENGTH_LONG).show();

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
        }.execute(ActivityView.this, args, "decrypt");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == REQUEST_RAW) {
                if (data != null)
                    saveRaw(data);
            } else if (requestCode == REQUEST_ATTACHMENT) {
                if (data != null)
                    saveAttachment(data);
            } else if (requestCode == REQUEST_ATTACHMENTS) {
                if (data != null)
                    saveAttachments(data);

            } else if (requestCode == REQUEST_DECRYPT) {
                if (data != null)
                    decrypt(data, message);
            }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveRaw(Intent data) {
        Bundle args = new Bundle();
        args.putLong("id", message);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if ("file".equals(uri.getScheme())) {
                    Log.w("Save raw uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    throw new FileNotFoundException();
                File file = message.getRawFile(context);
                Log.i("Raw file=" + file);

                ParcelFileDescriptor pfd = null;
                OutputStream os = null;
                InputStream is = null;
                try {
                    pfd = context.getContentResolver().openFileDescriptor(uri, "w");
                    os = new FileOutputStream(pfd.getFileDescriptor());
                    is = new BufferedInputStream(new FileInputStream(file));

                    byte[] buffer = new byte[MessageHelper.ATTACHMENT_BUFFER_SIZE];
                    int read;
                    while ((read = is.read(buffer)) != -1)
                        os.write(buffer, 0, read);
                } finally {
                    try {
                        if (pfd != null)
                            pfd.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                    try {
                        if (os != null)
                            os.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                    try {
                        if (is != null)
                            is.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                Toast.makeText(ActivityView.this, R.string.title_raw_saved, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(getVisibleView(), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
            }
        }.execute(this, args, "raw:save");
    }

    private void saveAttachment(Intent data) {
        Bundle args = new Bundle();
        args.putLong("id", attachment);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if ("file".equals(uri.getScheme())) {
                    Log.w("Save attachment uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                EntityAttachment attachment = db.attachment().getAttachment(id);
                if (attachment == null)
                    return null;
                File file = attachment.getFile(context);

                ParcelFileDescriptor pfd = null;
                OutputStream os = null;
                InputStream is = null;
                try {
                    pfd = context.getContentResolver().openFileDescriptor(uri, "w");
                    os = new FileOutputStream(pfd.getFileDescriptor());
                    is = new BufferedInputStream(new FileInputStream(file));

                    byte[] buffer = new byte[MessageHelper.ATTACHMENT_BUFFER_SIZE];
                    int read;
                    while ((read = is.read(buffer)) != -1)
                        os.write(buffer, 0, read);
                } finally {
                    try {
                        if (pfd != null)
                            pfd.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                    try {
                        if (os != null)
                            os.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                    try {
                        if (is != null)
                            is.close();
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
        }.execute(this, args, "attachment:save");
    }

    private void saveAttachments(Intent data) {
        Bundle args = new Bundle();
        args.putLong("id", message);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                DB db = DB.getInstance(context);
                DocumentFile tree = DocumentFile.fromTreeUri(context, uri);
                List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                for (EntityAttachment attachment : attachments) {
                    File file = attachment.getFile(context);

                    String name = Helper.sanitizeFilename(attachment.name);
                    if (TextUtils.isEmpty(name))
                        name = Long.toString(attachment.id);
                    DocumentFile document = tree.createFile(attachment.type, name);
                    if (document == null)
                        throw new FileNotFoundException(uri + ":" + name);

                    ParcelFileDescriptor pfd = null;
                    OutputStream os = null;
                    InputStream is = null;
                    try {
                        pfd = context.getContentResolver().openFileDescriptor(document.getUri(), "w");
                        os = new FileOutputStream(pfd.getFileDescriptor());
                        is = new BufferedInputStream(new FileInputStream(file));

                        byte[] buffer = new byte[MessageHelper.ATTACHMENT_BUFFER_SIZE];
                        int read;
                        while ((read = is.read(buffer)) != -1)
                            os.write(buffer, 0, read);
                    } finally {
                        try {
                            if (pfd != null)
                                pfd.close();
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                        try {
                            if (os != null)
                                os.close();
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                        try {
                            if (is != null)
                                is.close();
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                Toast.makeText(ActivityView.this, R.string.title_attachments_saved, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(ActivityView.this, ActivityView.this, ex);
            }
        }.execute(this, args, "attachments:save");
    }
}
