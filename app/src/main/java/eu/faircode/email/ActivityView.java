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

import android.annotation.SuppressLint;
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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.widget.NestedScrollView;
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

import com.google.android.material.snackbar.Snackbar;

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

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED;

public class ActivityView extends ActivityBilling implements FragmentManager.OnBackStackChangedListener {
    private String startup;

    private View view;

    private View content_separator;
    private View content_pane;

    private TwoStateOwner owner = new TwoStateOwner("drawer");
    private DrawerLayoutEx drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NestedScrollView drawerContainer;
    private ImageButton ibExpanderAccount;
    private RecyclerView rvAccount;
    private ImageButton ibExpanderUnified;
    private RecyclerView rvUnified;
    private RecyclerView rvFolder;
    private RecyclerView rvMenu;
    private ImageButton ibExpanderExtra;
    private RecyclerView rvMenuExtra;

    private AdapterNavAccount adapterNavAccount;
    private AdapterNavUnified adapterNavUnified;
    private AdapterNavFolder adapterNavFolder;
    private AdapterNavMenu adapterNavMenu;
    private AdapterNavMenu adapterNavMenuExtra;

    private boolean exit = false;
    private boolean searching = false;
    private Snackbar lastSnackbar = null;

    static final int REQUEST_UNIFIED = 1;
    static final int REQUEST_WHY = 2;
    static final int REQUEST_ALERT = 3;
    static final int REQUEST_THREAD = 4;
    static final int REQUEST_OUTBOX = 5;
    static final int REQUEST_ERROR = 6;
    static final int REQUEST_UPDATE = 7;
    static final int REQUEST_WIDGET = 8;

    static final String ACTION_VIEW_FOLDERS = BuildConfig.APPLICATION_ID + ".VIEW_FOLDERS";
    static final String ACTION_VIEW_MESSAGES = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGES";
    static final String ACTION_SEARCH = BuildConfig.APPLICATION_ID + ".SEARCH";
    static final String ACTION_VIEW_THREAD = BuildConfig.APPLICATION_ID + ".VIEW_THREAD";
    static final String ACTION_EDIT_FOLDER = BuildConfig.APPLICATION_ID + ".EDIT_FOLDER";
    static final String ACTION_EDIT_ANSWERS = BuildConfig.APPLICATION_ID + ".EDIT_ANSWERS";
    static final String ACTION_EDIT_ANSWER = BuildConfig.APPLICATION_ID + ".EDIT_ANSWER";
    static final String ACTION_EDIT_RULES = BuildConfig.APPLICATION_ID + ".EDIT_RULES";
    static final String ACTION_EDIT_RULE = BuildConfig.APPLICATION_ID + ".EDIT_RULE";
    static final String ACTION_NEW_MESSAGE = BuildConfig.APPLICATION_ID + ".NEW_MESSAGE";

    private static final int UPDATE_TIMEOUT = 15 * 1000; // milliseconds
    private static final long EXIT_DELAY = 2500L; // milliseconds
    static final long UPDATE_INTERVAL = (BuildConfig.BETA_RELEASE ? 4 : 12) * 3600 * 1000L; // milliseconds

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, false);

        // Workaround stale intents from recent apps screen
        boolean recents = (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
        if (recents) {
            Intent intent = getIntent();
            intent.setAction(null);
            setIntent(intent);
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(creceiver, new IntentFilter(ACTION_NEW_MESSAGE));

        if (savedInstanceState != null)
            searching = savedInstanceState.getBoolean("fair:searching");

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        startup = prefs.getString("startup", "unified");

        Configuration config = getResources().getConfiguration();
        final boolean normal = config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL);
        final boolean portrait2 = prefs.getBoolean("portrait2", false);
        final boolean landscape = prefs.getBoolean("landscape", true);
        final boolean landscape3 = prefs.getBoolean("landscape3", true);
        Log.i("Orientation=" + config.orientation + " normal=" + normal +
                " landscape=" + landscape + "/" + landscape3);

        boolean portrait = (config.orientation == ORIENTATION_PORTRAIT || !normal || !landscape);
        view = LayoutInflater.from(this).inflate(portrait
                ? (portrait2 ? R.layout.activity_view_portrait_split : R.layout.activity_view_portrait)
                : R.layout.activity_view_landscape, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        content_separator = findViewById(R.id.content_separator);
        content_pane = findViewById(R.id.content_pane);

        if ((portrait ? portrait2 : !landscape3) && Helper.isFoldable()) {
            View content_frame = findViewById(R.id.content_frame);
            ViewGroup.LayoutParams lparam = content_frame.getLayoutParams();
            if (lparam instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) lparam).weight = 1;
                content_frame.setLayoutParams(lparam);
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout);

        final ViewGroup childContent = (ViewGroup) drawerLayout.getChildAt(0);
        final ViewGroup childDrawer = (ViewGroup) drawerLayout.getChildAt(1);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                Log.i("Drawer closed");
                owner.stop();

                drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED);
                childContent.setPaddingRelative(0, 0, 0, 0);

                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                Log.i("Drawer opened");
                owner.start();

                if (normal && landscape3 &&
                        config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_OPEN);
                    childContent.setPaddingRelative(childDrawer.getLayoutParams().width, 0, 0, 0);
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                Log.i("Drawer offset=" + slideOffset);
                if (slideOffset > 0)
                    owner.start();
                else
                    owner.stop();

                if (normal && landscape3 &&
                        config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    childContent.setPaddingRelative(
                            Math.round(slideOffset * childDrawer.getLayoutParams().width), 0, 0, 0);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        drawerContainer = findViewById(R.id.drawer_container);

        int drawerWidth;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        if (portrait || !landscape3) {
            int actionBarHeight;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, dm);
            else
                actionBarHeight = Helper.dp2pixels(this, 56);

            int screenWidth = Math.min(dm.widthPixels, dm.heightPixels);
            int dp320 = Helper.dp2pixels(this, 320);
            drawerWidth = Math.min(screenWidth - actionBarHeight, dp320);
        } else
            drawerWidth = Helper.dp2pixels(this, 300);

        ViewGroup.LayoutParams lparam = drawerContainer.getLayoutParams();
        lparam.width = drawerWidth;
        drawerContainer.setLayoutParams(lparam);

        // Accounts
        ibExpanderAccount = drawerContainer.findViewById(R.id.ibExpanderAccount);

        rvAccount = drawerContainer.findViewById(R.id.rvAccount);
        rvAccount.setLayoutManager(new LinearLayoutManager(this));
        adapterNavAccount = new AdapterNavAccount(this, this);
        rvAccount.setAdapter(adapterNavAccount);

        boolean nav_account = prefs.getBoolean("nav_account", true);
        ibExpanderAccount.setImageLevel(nav_account ? 0 /* less */ : 1 /* more */);
        rvAccount.setVisibility(nav_account ? View.VISIBLE : View.GONE);

        ibExpanderAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean nav_account = !prefs.getBoolean("nav_account", true);
                prefs.edit().putBoolean("nav_account", nav_account).apply();
                ibExpanderAccount.setImageLevel(nav_account ? 0 /* less */ : 1 /* more */);
                rvAccount.setVisibility(nav_account ? View.VISIBLE : View.GONE);
            }
        });

        // Unified system folders
        ibExpanderUnified = drawerContainer.findViewById(R.id.ibExpanderUnified);

        rvUnified = drawerContainer.findViewById(R.id.rvUnified);
        rvUnified.setLayoutManager(new LinearLayoutManager(this));
        adapterNavUnified = new AdapterNavUnified(this, this);
        rvUnified.setAdapter(adapterNavUnified);

        boolean unified_system = prefs.getBoolean("unified_system", true);
        ibExpanderUnified.setImageLevel(unified_system ? 0 /* less */ : 1 /* more */);
        rvUnified.setVisibility(unified_system ? View.VISIBLE : View.GONE);

        ibExpanderUnified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean unified_system = !prefs.getBoolean("unified_system", true);
                prefs.edit().putBoolean("unified_system", unified_system).apply();
                ibExpanderUnified.setImageLevel(unified_system ? 0 /* less */ : 1 /* more */);
                rvUnified.setVisibility(unified_system ? View.VISIBLE : View.GONE);
            }
        });

        // Navigation folders
        rvFolder = drawerContainer.findViewById(R.id.rvFolder);
        rvFolder.setLayoutManager(new LinearLayoutManager(this));
        adapterNavFolder = new AdapterNavFolder(this, this);
        rvFolder.setAdapter(adapterNavFolder);

        rvMenu = drawerContainer.findViewById(R.id.rvMenu);
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        adapterNavMenu = new AdapterNavMenu(this, this);
        rvMenu.setAdapter(adapterNavMenu);

        // Extra menus
        ibExpanderExtra = drawerContainer.findViewById(R.id.ibExpanderExtra);

        rvMenuExtra = drawerContainer.findViewById(R.id.rvMenuExtra);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvMenuExtra.setLayoutManager(llm);
        adapterNavMenuExtra = new AdapterNavMenu(this, this);
        rvMenuExtra.setAdapter(adapterNavMenuExtra);

        final Drawable d = getDrawable(R.drawable.divider);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, llm.getOrientation()) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(view);
                NavMenuItem menu = adapterNavMenuExtra.get(pos);
                outRect.set(0, 0, 0, menu != null && menu.isSeparated() ? d.getIntrinsicHeight() : 0);
            }
        };
        itemDecorator.setDrawable(d);
        rvMenuExtra.addItemDecoration(itemDecorator);

        boolean minimal = prefs.getBoolean("minimal", false);
        ibExpanderExtra.setImageLevel(minimal ? 1 /* more */ : 0 /* less */);
        rvMenuExtra.setVisibility(minimal ? View.GONE : View.VISIBLE);

        ibExpanderExtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean minimal = !prefs.getBoolean("minimal", false);
                prefs.edit().putBoolean("minimal", minimal).apply();
                ibExpanderExtra.setImageLevel(minimal ? 1 /* more */ : 0 /* less */);
                rvMenuExtra.setVisibility(minimal ? View.GONE : View.VISIBLE);
                if (!minimal)
                    getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            drawerContainer.fullScroll(View.FOCUS_DOWN);
                        }
                    });
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        // Initialize

        if (content_pane != null) {
            content_separator.setVisibility(View.GONE);
            content_pane.setVisibility(View.GONE);
        }

        if (getSupportFragmentManager().getFragments().size() == 0 &&
                !getIntent().hasExtra(Intent.EXTRA_PROCESS_TEXT))
            init();

        if (savedInstanceState != null)
            drawerToggle.setDrawerIndicatorEnabled(savedInstanceState.getBoolean("fair:toggle"));

        checkFirst();
        checkCrash();

        Shortcuts.update(this, this);
    }

    private void init() {
        Bundle args = new Bundle();

        long account = getIntent().getLongExtra("account", -1);

        FragmentBase fragment;
        switch (startup) {
            case "accounts":
                fragment = new FragmentAccounts();
                args.putBoolean("settings", false);
                break;
            case "folders":
                fragment = new FragmentFolders();
                args.putLong("account", account);
                break;
            case "primary":
                fragment = new FragmentFolders();
                if (account < 0)
                    args.putBoolean("primary", true);
                else
                    args.putLong("account", account);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:toggle", drawerToggle.isDrawerIndicatorEnabled());
        outState.putBoolean("fair:searching", searching);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Fixed menus

        PackageManager pm = getPackageManager();
        final List<NavMenuItem> menus = new ArrayList<>();

        final NavMenuItem navOperations = new NavMenuItem(R.drawable.twotone_dns_24, R.string.menu_operations, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuOperations();
            }
        });

        menus.add(navOperations);

        menus.add(new NavMenuItem(R.drawable.twotone_list_alt_24, R.string.title_log, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onShowLog();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_reply_24, R.string.menu_answers, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuAnswers();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_settings_24, R.string.menu_setup, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuSetup();
            }
        }));

        adapterNavMenu.set(menus);

        // Collapsible menus

        List<NavMenuItem> extra = new ArrayList<>();

        extra.add(new NavMenuItem(R.drawable.twotone_help_24, R.string.menu_legend, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuLegend();
            }
        }));

        extra.add(new NavMenuItem(R.drawable.twotone_question_answer_24, R.string.menu_faq, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuFAQ();
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onDebugInfo();
            }
        }).setExternal(true));

        extra.add(new NavMenuItem(R.drawable.twotone_feedback_24, R.string.menu_issue, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuIssue();
            }
        }).setExternal(true));

        extra.add(new NavMenuItem(R.drawable.twotone_language_24, R.string.menu_translate, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuTranslate();
            }
        }).setExternal(true));

        if (Helper.isPlayStoreInstall() && false)
            extra.add(new NavMenuItem(R.drawable.twotone_bug_report_24, R.string.menu_test, new Runnable() {
                @Override
                public void run() {
                    if (!drawerLayout.isLocked(drawerContainer))
                        drawerLayout.closeDrawer(drawerContainer);
                    onMenuTest();
                }
            }).setExternal(true));

        extra.add(new NavMenuItem(R.drawable.twotone_account_box_24, R.string.menu_privacy, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuPrivacy();
            }
        }));

        extra.add(new NavMenuItem(R.drawable.twotone_info_24, R.string.menu_about, new Runnable() {
            @Override
            public void run() {
                onMenuAbout();
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (!Helper.isPlayStoreInstall()) {
                    if (!drawerLayout.isLocked(drawerContainer))
                        drawerLayout.closeDrawer(drawerContainer);
                    checkUpdate(true);
                }
            }
        }).setSeparated());

        extra.add(new NavMenuItem(R.drawable.twotone_monetization_on_24, R.string.menu_pro, new Runnable() {
            @Override
            public void run() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                startActivity(new Intent(ActivityView.this, ActivityBilling.class));
            }
        }));

        if ((Helper.isPlayStoreInstall() || BuildConfig.DEBUG))
            extra.add(new NavMenuItem(R.drawable.twotone_star_24, R.string.menu_rate, new Runnable() {
                @Override
                public void run() {
                    if (!drawerLayout.isLocked(drawerContainer))
                        drawerLayout.closeDrawer(drawerContainer);
                    onMenuRate();
                }
            }).setExternal(true));

        adapterNavMenuExtra.set(extra);

        // Live data

        DB db = DB.getInstance(this);

        db.account().liveAccountsEx(false).observe(owner, new Observer<List<TupleAccountEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleAccountEx> accounts) {
                if (accounts == null)
                    accounts = new ArrayList<>();
                adapterNavAccount.set(accounts);
            }
        });

        db.folder().liveUnified().observe(owner, new Observer<List<TupleFolderUnified>>() {
            @Override
            public void onChanged(List<TupleFolderUnified> folders) {
                if (folders == null)
                    folders = new ArrayList<>();
                adapterNavUnified.set(folders);
            }
        });

        db.folder().liveNavigation().observe(owner, new Observer<List<TupleFolderNav>>() {
            @Override
            public void onChanged(List<TupleFolderNav> folders) {
                if (folders == null)
                    folders = new ArrayList<>();
                adapterNavFolder.set(folders);
            }
        });

        db.operation().liveStats().observe(owner, new Observer<TupleOperationStats>() {
            @Override
            public void onChanged(TupleOperationStats stats) {
                navOperations.setWarning(stats != null && stats.errors != null && stats.errors > 0);
                navOperations.setCount(stats == null ? 0 : stats.pending);
                adapterNavMenu.notifyDataSetChanged();
            }
        });

        Log.i("Drawer start");
        owner.start();

        drawerLayout.setup(getResources().getConfiguration(), drawerContainer, drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
        lbm.registerReceiver(receiver, iff);

        ServiceSynchronize.state(this, true);

        checkUpdate(false);
        checkIntent();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);

        ServiceSynchronize.state(this, false);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(creceiver);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerLayout.setup(newConfig, drawerContainer, drawerToggle);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (drawerLayout.isDrawerOpen(drawerContainer) &&
                (!drawerLayout.isLocked(drawerContainer) || count == 1))
            drawerLayout.closeDrawer(drawerContainer);
        else {
            if (exit || count > 1)
                super.onBackPressed();
            else if (!backHandled()) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityView.this);
                boolean double_back = prefs.getBoolean("double_back", true);
                if (searching || !double_back)
                    super.onBackPressed();
                else {
                    exit = true;
                    ToastEx.makeText(this, R.string.app_exit, Toast.LENGTH_SHORT).show();
                    getMainHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exit = false;
                        }
                    }, EXIT_DELAY);
                }
            }
        }
    }

    @Override
    public void onBackStackChanged() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0)
            finish();
        else {
            if (drawerLayout.isDrawerOpen(drawerContainer) &&
                    !drawerLayout.isLocked(drawerContainer))
                drawerLayout.closeDrawer(drawerContainer);
            drawerToggle.setDrawerIndicatorEnabled(count == 1);

            if (content_pane != null) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_pane);
                content_separator.setVisibility(fragment == null ? View.GONE : View.VISIBLE);
                content_pane.setVisibility(fragment == null ? View.GONE : View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            int count = getSupportFragmentManager().getBackStackEntryCount();
            if (count == 1 && drawerLayout.isLocked(drawerContainer))
                drawerLayout.closeDrawer(drawerContainer);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void undo(String title, final Bundle args, final SimpleTask<Void> move, final SimpleTask<Void> show) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int undo_timeout = prefs.getInt("undo_timeout", 5000);

        if (undo_timeout == 0) {
            move.execute(this, args, "undo:move");
            return;
        }

        if (drawerLayout == null || drawerLayout.getChildCount() == 0) {
            Log.e("Undo: drawer missing");
            return;
        }

        final View content = drawerLayout.getChildAt(0);

        final Snackbar snackbar = Snackbar.make(content, title, Snackbar.LENGTH_INDEFINITE)
                .setGestureInsetBottomIgnored(true);
        lastSnackbar = snackbar;

        snackbar.setAction(R.string.title_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Undo cancel");
                snackbar.getView().setTag(true);
                snackbar.dismiss();
            }
        });

        snackbar.addCallback(new Snackbar.Callback() {
            private int margin;

            @Override
            public void onShown(Snackbar sb) {
                ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) content.getLayoutParams();
                margin = lparam.bottomMargin;
                lparam.bottomMargin += snackbar.getView().getHeight();
                content.setLayoutParams(lparam);
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (snackbar.getView().getTag() == null)
                    move.execute(ActivityView.this, args, "undo:move");
                else
                    show.execute(ActivityView.this, args, "undo:show");

                ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) content.getLayoutParams();
                lparam.bottomMargin = margin;
                content.setLayoutParams(lparam);
            }
        });
        snackbar.show();

        // Wait
        getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("Undo timeout");
                if (snackbar.isShown())
                    snackbar.dismiss();
            }
        }, undo_timeout);
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
                ToastEx.makeText(ActivityView.this,
                        Log.formatThrowable(ex, false), Toast.LENGTH_LONG).show();
            }
        }.execute(this, new Bundle(), "crash:log");
    }

    private void checkUpdate(boolean always) {
        if (Helper.isPlayStoreInstall() || !Helper.hasValidFingerprint(this))
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
                    urlConnection.setReadTimeout(UPDATE_TIMEOUT);
                    urlConnection.setConnectTimeout(UPDATE_TIMEOUT);
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
                    //if (!jroot.has("html_url") || jroot.isNull("html_url"))
                    //    throw new IOException("html_url field missing");
                    if (!jroot.has("assets") || jroot.isNull("assets"))
                        throw new IOException("assets section missing");

                    // Get update info
                    UpdateInfo info = new UpdateInfo();
                    info.tag_name = jroot.getString("tag_name");
                    //info.html_url = jroot.getString("html_url");
                    info.html_url = BuildConfig.GITHUB_LATEST_URI;

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
                        ToastEx.makeText(ActivityView.this, BuildConfig.VERSION_NAME, Toast.LENGTH_LONG).show();
                    return;
                }

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(ActivityView.this, "update")
                                .setSmallIcon(R.drawable.twotone_get_app_24)
                                .setContentTitle(getString(R.string.title_updated, info.tag_name))
                                .setContentText(info.html_url)
                                .setAutoCancel(true)
                                .setShowWhen(false)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

                Intent update = new Intent(Intent.ACTION_VIEW, Uri.parse(info.html_url));
                update.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent piUpdate = PendingIntent.getActivity(
                        ActivityView.this, REQUEST_UPDATE, update, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(piUpdate);

                try {
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(Helper.NOTIFICATION_UPDATE, builder.build());
                } catch (Throwable ex) {
                    Log.w(ex);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (args.getBoolean("always"))
                    if (ex instanceof IllegalArgumentException || ex instanceof IOException)
                        ToastEx.makeText(ActivityView.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    else
                        Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "update:check");
    }

    private void checkIntent() {
        Intent intent = getIntent();

        // Refresh from widget
        if (intent.getBooleanExtra("refresh", false)) {
            intent.removeExtra("refresh");
            setIntent(intent);

            ServiceUI.sync(this, null);
        }

        String action = intent.getAction();
        Log.i("View intent=" + intent + " action=" + action);
        if (action != null) {
            intent.setAction(null);
            setIntent(intent);

            if (action.startsWith("unified")) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getSupportFragmentManager().popBackStack("unified", 0);

                if (action.contains(":")) {
                    Intent clear = new Intent(this, ServiceUI.class)
                            .setAction(action.replace("unified", "clear"));
                    startService(clear);
                }

            } else if (action.startsWith("folders")) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getSupportFragmentManager().popBackStack("unified", 0);

                long account = Long.parseLong(action.split(":", 2)[1]);
                if (account > 0)
                    onMenuFolders(account);

            } else if (action.startsWith("folder")) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getSupportFragmentManager().popBackStack("unified", 0);

                String[] parts = action.split(":");
                long folder = Long.parseLong(parts[1]);
                if (folder > 0) {
                    intent.putExtra("folder", folder);
                    onViewMessages(intent);
                }

                if (parts.length > 2) {
                    Intent clear = new Intent(this, ServiceUI.class)
                            .setAction("clear:" + parts[2]);
                    startService(clear);
                }

            } else if ("why".equals(action)) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getSupportFragmentManager().popBackStack("unified", 0);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityView.this);
                boolean why = prefs.getBoolean("why", false);
                if (!why || BuildConfig.DEBUG) {
                    prefs.edit().putBoolean("why", true).apply();
                    Helper.viewFAQ(this, 2);
                }

            } else if ("alert".equals(action) || "error".equals(action)) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getSupportFragmentManager().popBackStack("unified", 0);

                Helper.viewFAQ(this, "alert".equals(action) ? 23 : 22);

            } else if ("outbox".equals(action)) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getSupportFragmentManager().popBackStack("unified", 0);

                onMenuOutbox();

            } else if (action.startsWith("thread")) {
                intent.putExtra("thread", action.split(":", 2)[1]);
                onViewThread(intent);

            } else if (action.equals("widget")) {
                long account = intent.getLongExtra("widget_account", -1);
                long folder = intent.getLongExtra("widget_folder", -1);
                String type = intent.getStringExtra("widget_type");
                if (account > 0 && folder > 0 && !TextUtils.isEmpty(type)) {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        getSupportFragmentManager().popBackStack("messages", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                        Bundle args = new Bundle();
                        args.putLong("account", account);
                        args.putLong("folder", folder);
                        args.putString("type", type);

                        FragmentMessages fragment = new FragmentMessages();
                        fragment.setArguments(args);

                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("messages");
                        fragmentTransaction.commit();
                    }
                }
                onViewThread(intent);
            }
        }

        if (intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
            CharSequence csearch = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            String search = (csearch == null ? null : csearch.toString());
            if (!TextUtils.isEmpty(search)) {
                searching = true;
                FragmentMessages.search(
                        ActivityView.this, ActivityView.this, getSupportFragmentManager(),
                        -1, -1, false, search);
            }

            intent.removeExtra(Intent.EXTRA_PROCESS_TEXT);
            setIntent(intent);
        }
    }

    private void onMenuFolders(long account) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
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

        new SimpleTask<EntityFolder>() {
            @Override
            protected EntityFolder onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                EntityFolder outbox = db.folder().getOutbox();
                return outbox;
            }

            @Override
            protected void onExecuted(Bundle args, EntityFolder outbox) {
                if (outbox == null)
                    return;

                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getSupportFragmentManager().popBackStack("unified", 0);

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ActivityView.this);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                .putExtra("account", -1L)
                                .putExtra("folder", outbox.id)
                                .putExtra("type", outbox.type));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "menu:outbox");
    }

    private void onMenuOperations() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("operations", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentOperations()).addToBackStack("operations");
        fragmentTransaction.commit();
    }

    private void onMenuAnswers() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("answers", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAnswers()).addToBackStack("answers");
        fragmentTransaction.commit();
    }

    private void onMenuSetup() {
        startActivity(new Intent(ActivityView.this, ActivitySetup.class));
    }

    private void onMenuLegend() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("legend", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLegend()).addToBackStack("legend");
        fragmentTransaction.commit();
    }

    private void onMenuTest() {
        Helper.view(this, Uri.parse(Helper.TEST_URI), false);
    }

    private void onMenuFAQ() {
        Helper.viewFAQ(this, 0);
    }

    private void onMenuTranslate() {
        Helper.viewFAQ(this, 26);
    }

    private void onMenuIssue() {
        startActivity(Helper.getIntentIssue(this));
    }

    private void onMenuPrivacy() {
        Bundle args = new Bundle();
        args.putString("name", "PRIVACY.md");
        FragmentDialogMarkdown fragment = new FragmentDialogMarkdown();
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), "privacy");
    }

    private void onMenuAbout() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("about", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAbout()).addToBackStack("about");
        fragmentTransaction.commit();
    }

    private void onMenuRate() {
        new FragmentDialogRate().show(getSupportFragmentManager(), "rate");
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
                    ToastEx.makeText(ActivityView.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getSupportFragmentManager(), ex);
            }

        }.execute(this, new Bundle(), "debug:info");
    }

    private void onShowLog() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("logs", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLogs()).addToBackStack("logs");
        fragmentTransaction.commit();
    }

    private BroadcastReceiver creceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onNewMessage(intent);
        }
    };

    private List<Long> updatedFolders = new ArrayList<>();

    boolean isFolderUpdated(long folder) {
        boolean value = updatedFolders.contains(folder);
        if (value)
            updatedFolders.remove(folder);
        return value;
    }

    private void onNewMessage(Intent intent) {
        long folder = intent.getLongExtra("folder", -1);
        boolean unified = intent.getBooleanExtra("unified", false);

        if (!updatedFolders.contains(folder))
            updatedFolders.add(folder);
        if (unified && !updatedFolders.contains(-1L))
            updatedFolders.add(-1L);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
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
            }
        }
    };

    private void onViewFolders(Intent intent) {
        long account = intent.getLongExtra("id", -1);
        onMenuFolders(account);
    }

    private void onViewMessages(Intent intent) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            getSupportFragmentManager().popBackStack("messages", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            //if (content_pane != null)
            //    getSupportFragmentManager().popBackStack("unified", 0);
        }

        Bundle args = new Bundle();
        args.putString("type", intent.getStringExtra("type"));
        args.putLong("account", intent.getLongExtra("account", -1));
        args.putLong("folder", intent.getLongExtra("folder", -1));

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("messages");
        fragmentTransaction.commit();
    }

    private void onSearchMessages(Intent intent) {
        long account = intent.getLongExtra("account", -1);
        long folder = intent.getLongExtra("folder", -1);
        String query = intent.getStringExtra("query");
        FragmentMessages.search(
                this, this, getSupportFragmentManager(),
                account, folder, false, query);
    }

    private void onViewThread(Intent intent) {
        boolean found = intent.getBooleanExtra("found", false);

        if (lastSnackbar != null && lastSnackbar.isShown())
            lastSnackbar.dismiss();

        if (!found && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("account", intent.getLongExtra("account", -1));
        args.putLong("folder", intent.getLongExtra("folder", -1));
        args.putString("thread", intent.getStringExtra("thread"));
        args.putLong("id", intent.getLongExtra("id", -1));
        args.putBoolean("filter_archive", intent.getBooleanExtra("filter_archive", true));
        args.putBoolean("found", found);

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        int pane;
        if (content_pane == null)
            pane = R.id.content_frame;
        else {
            pane = R.id.content_pane;
            content_separator.setVisibility(View.VISIBLE);
            content_pane.setVisibility(View.VISIBLE);
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

    private class UpdateInfo {
        String tag_name; // version
        String html_url;
    }

    public static class FragmentDialogFirst extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View dview = inflater.inflate(R.layout.dialog_first, null);
            Button btnBatteryInfo = dview.findViewById(R.id.btnBatteryInfo);
            Button btnReformatInfo = dview.findViewById(R.id.btnReformatInfo);

            btnBatteryInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(getContext(), 39);
                }
            });

            btnReformatInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(getContext(), 35);
                }
            });

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            prefs.edit().putBoolean("first", false).apply();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentDialogRate extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setMessage(R.string.title_issue)
                    .setPositiveButton(R.string.title_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Helper.viewFAQ(getContext(), 0);
                        }
                    })
                    .setNegativeButton(R.string.title_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Helper.view(getContext(), Helper.getIntentRate(getContext()));
                        }
                    })
                    .create();
        }
    }
}
