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
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.NotificationCompat;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

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

    private boolean exit = false;

    static final int REQUEST_UNIFIED = 1;
    static final int REQUEST_WHY = 2;
    static final int REQUEST_THREAD = 3;
    static final int REQUEST_OUTBOX = 4;
    static final int REQUEST_ERROR = 5;
    static final int REQUEST_UPDATE = 6;

    static final String ACTION_VIEW_FOLDERS = BuildConfig.APPLICATION_ID + ".VIEW_FOLDERS";
    static final String ACTION_VIEW_MESSAGES = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGES";
    static final String ACTION_SEARCH = BuildConfig.APPLICATION_ID + ".SEARCH";
    static final String ACTION_VIEW_THREAD = BuildConfig.APPLICATION_ID + ".VIEW_THREAD";
    static final String ACTION_EDIT_FOLDER = BuildConfig.APPLICATION_ID + ".EDIT_FOLDER";
    static final String ACTION_EDIT_ANSWERS = BuildConfig.APPLICATION_ID + ".EDIT_ANSWERS";
    static final String ACTION_EDIT_ANSWER = BuildConfig.APPLICATION_ID + ".EDIT_ANSWER";
    static final String ACTION_EDIT_RULES = BuildConfig.APPLICATION_ID + ".EDIT_RULES";
    static final String ACTION_EDIT_RULE = BuildConfig.APPLICATION_ID + ".EDIT_RULE";
    static final String ACTION_SHOW_PRO = BuildConfig.APPLICATION_ID + ".SHOW_PRO";

    static final long UPDATE_INTERVAL = (BuildConfig.BETA_RELEASE ? 4 : 12) * 3600 * 1000L; // milliseconds

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

        if ((getIntentInvite(this).resolveActivity(pm) != null))
            extra.add(new NavMenuItem(R.drawable.baseline_people_24, R.string.menu_invite, new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(drawerContainer);
                    onMenuInvite();
                }
            }));

        if (getIntentRate(this).resolveActivity(pm) != null)
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
                else if (action.startsWith("thread")) {
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
        iff.addAction(ACTION_EDIT_FOLDER);
        iff.addAction(ACTION_EDIT_ANSWERS);
        iff.addAction(ACTION_EDIT_ANSWER);
        iff.addAction(ACTION_EDIT_RULES);
        iff.addAction(ACTION_EDIT_RULE);
        iff.addAction(ACTION_SHOW_PRO);
        lbm.registerReceiver(receiver, iff);

        checkUpdate(false);
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
        else {
            int count = getSupportFragmentManager().getBackStackEntryCount();
            if (exit || count > 1)
                super.onBackPressed();
            else if (!backHandled()) {
                exit = true;
                Toast.makeText(this, R.string.app_exit, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 2500);
            }
        }
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("first", true))
            new FragmentDialogFirst().show(getSupportFragmentManager(), "first");
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
                Toast.makeText(ActivityView.this,
                        Helper.formatThrowable(ex, false), Toast.LENGTH_LONG).show();
            }
        }.execute(this, new Bundle(), "crash:log");
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

                    if (!jroot.has("tag_name") || jroot.isNull("tag_name"))
                        throw new IOException("tag_name field missing");
                    if (!jroot.has("html_url") || jroot.isNull("html_url"))
                        throw new IOException("html_url field missing");
                    if (!jroot.has("assets") || jroot.isNull("assets"))
                        throw new IOException("assets section missing");

                    // Get update info
                    UpdateInfo info = new UpdateInfo();
                    info.tag_name = jroot.getString("tag_name");
                    info.html_url = jroot.getString("html_url");

                    // Check if new release
                    JSONArray jassets = jroot.getJSONArray("assets");
                    for (int i = 0; i < jassets.length(); i++) {
                        JSONObject jasset = jassets.getJSONObject(i);
                        if (jasset.has("name") && !jasset.isNull("name")) {
                            String name = jasset.getString("name");
                            if (name.endsWith(".apk")) {
                                Log.i("Latest version=" + info.tag_name);
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
                    if (always)
                        Toast.makeText(ActivityView.this, BuildConfig.VERSION_NAME, Toast.LENGTH_LONG).show();
                    return;
                }

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(ActivityView.this, "update")
                                .setSmallIcon(R.drawable.baseline_system_update_24)
                                .setContentTitle(getString(R.string.title_updated, info.tag_name))
                                .setAutoCancel(true)
                                .setShowWhen(false)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

                Intent update = new Intent(Intent.ACTION_VIEW, Uri.parse(info.html_url));
                if (update.resolveActivity(getPackageManager()) != null) {
                    PendingIntent piUpdate = PendingIntent.getActivity(
                            ActivityView.this, REQUEST_UPDATE, update, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(piUpdate);
                }

                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(Helper.NOTIFICATION_UPDATE, builder.build());
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (args.getBoolean("always"))
                    if (ex instanceof IllegalArgumentException || ex instanceof IOException)
                        Toast.makeText(ActivityView.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    else
                        Helper.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "update:check");
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
                Helper.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "menu:outbox");
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
        startActivity(getIntentInvite(this));
    }

    private void onMenuRate() {
        Intent faq = Helper.getIntentFAQ();
        if (faq.resolveActivity(getPackageManager()) == null)
            Helper.view(this, this, getIntentRate(this));
        else
            new FragmentDialogRate().show(getSupportFragmentManager(), "rate");
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
                    Toast.makeText(ActivityView.this, ex.getMessage(), Toast.LENGTH_LONG).show();
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

    private void onShowPro(Intent intent) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getSupportFragmentManager().popBackStack("pro", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
        fragmentTransaction.commit();
    }

    private class UpdateInfo {
        String tag_name; // version
        String html_url;
    }

    private static Intent getIntentInvite(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.title_try)).append("\n\n");
        sb.append(BuildConfig.INVITE_URI).append("\n\n");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        return intent;
    }

    private static Intent getIntentRate(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        if (intent.resolveActivity(context.getPackageManager()) == null)
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
        return intent;
    }

    public static class FragmentDialogFirst extends DialogFragmentEx {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.title_hint_sync))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            prefs.edit().putBoolean("first", false).apply();
                        }
                    })
                    .create();
        }
    }

    public static class FragmentDialogRate extends DialogFragmentEx {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setMessage(R.string.title_issue)
                    .setPositiveButton(R.string.title_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Helper.view(getContext(), getActivity(), Helper.getIntentFAQ());
                        }
                    })
                    .setNegativeButton(R.string.title_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Helper.view(getContext(), getActivity(), getIntentRate(getContext()));
                        }
                    })
                    .create();
        }
    }
}
