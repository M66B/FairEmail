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

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Consumer;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

public class ActivityView extends ActivityBilling implements FragmentManager.OnBackStackChangedListener {
    private String startup;
    private boolean nav_expanded;
    private boolean nav_pinned;
    private boolean nav_options;
    private int colorDrawerScrim;

    private WindowInfoTrackerCallbackAdapter infoTracker;
    private int layoutId;
    private View view;

    private View content_separator;
    private View content_pane;

    private TwoStateOwner owner;
    private DrawerLayoutEx drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NestedScrollView drawerContainer;
    private ImageButton ibExpanderNav;
    private ImageButton ibPin;
    private ImageButton ibHide;
    private ImageButton ibSettings;
    private ImageButton ibFetchMore;
    private ImageButton ibForceSync;

    private View vSeparatorOptions;
    private ImageButton ibExpanderAccount;

    private RecyclerView rvAccount;
    private ImageButton ibExpanderUnified;

    private ImageButton ibExpanderSearch;
    private RecyclerView rvSearch;
    private View vSeparatorSearch;

    private RecyclerView rvUnified;
    private ImageButton ibExpanderMenu;

    private RecyclerView rvMenu;
    private ImageButton ibExpanderExtra;

    private RecyclerView rvMenuExtra;

    private Group grpOptions;

    private AdapterNavAccountFolder adapterNavAccount;
    private AdapterNavUnified adapterNavUnified;
    private AdapterNavSearch adapterNavSearch;
    private AdapterNavMenu adapterNavMenu;
    private AdapterNavMenu adapterNavMenuExtra;

    private boolean initialized = false;
    private boolean exit = false;
    private boolean searching = false;
    private int lastBackStackCount = 0;
    private Snackbar lastSnackbar = null;

    static final int PI_UNIFIED = 1;
    static final int PI_WHY = 2;
    static final int PI_THREAD = 3;
    static final int PI_OUTBOX = 4;
    static final int PI_UPDATE = 5;
    static final int PI_ANNOUNCEMENT = 6;
    static final int PI_WIDGET = 7;
    static final int PI_POWER = 8;

    static final String ACTION_VIEW_FOLDERS = BuildConfig.APPLICATION_ID + ".VIEW_FOLDERS";
    static final String ACTION_VIEW_MESSAGES = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGES";
    static final String ACTION_SEARCH_ADDRESS = BuildConfig.APPLICATION_ID + ".SEARCH_ADDRESS";
    static final String ACTION_VIEW_THREAD = BuildConfig.APPLICATION_ID + ".VIEW_THREAD";
    static final String ACTION_EDIT_FOLDER = BuildConfig.APPLICATION_ID + ".EDIT_FOLDER";
    static final String ACTION_VIEW_OUTBOX = BuildConfig.APPLICATION_ID + ".VIEW_OUTBOX";
    static final String ACTION_EDIT_ANSWERS = BuildConfig.APPLICATION_ID + ".EDIT_ANSWERS";
    static final String ACTION_EDIT_ANSWER = BuildConfig.APPLICATION_ID + ".EDIT_ANSWER";
    static final String ACTION_EDIT_RULES = BuildConfig.APPLICATION_ID + ".EDIT_RULES";
    static final String ACTION_EDIT_RULE = BuildConfig.APPLICATION_ID + ".EDIT_RULE";
    static final String ACTION_NEW_MESSAGE = BuildConfig.APPLICATION_ID + ".NEW_MESSAGE";
    static final String ACTION_SENT_MESSAGE = BuildConfig.APPLICATION_ID + ".SENT_MESSAGE";

    private static final int UPDATE_TIMEOUT = 15 * 1000; // milliseconds
    private static final long EXIT_DELAY = 2500L; // milliseconds
    static final long UPDATE_DAILY = (BuildConfig.BETA_RELEASE ? 4 : 12) * 3600 * 1000L; // milliseconds
    static final long UPDATE_WEEKLY = 7 * 24 * 3600 * 1000L; // milliseconds

    private static final int ANNOUNCEMENT_TIMEOUT = 15 * 1000; // milliseconds
    private static final long ANNOUNCEMENT_INTERVAL = 4 * 3600 * 1000L; // milliseconds

    static final int REQUEST_FLAG_COLOR = 1;

    private static final int REQUEST_RULES_ACCOUNT = 2001;
    private static final int REQUEST_RULES_FOLDER = 2002;
    private static final int REQUEST_DEBUG_INFO = 7000;

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, false);

        if (savedInstanceState != null) {
            Intent intent = savedInstanceState.getParcelable("fair:intent");
            if (intent != null)
                setIntent(intent);
        }

        // Workaround stale intents from recent apps screen
        boolean recents = (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
        if (recents) {
            Intent intent = getIntent();
            Log.i("Stale intent=" + intent);
            intent.setAction(null);
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_NEW_MESSAGE);
        iff.addAction((ACTION_SENT_MESSAGE));
        lbm.registerReceiver(creceiver, iff);

        if (savedInstanceState != null) {
            initialized = savedInstanceState.getBoolean("fair:initialized");
            searching = savedInstanceState.getBoolean("fair:searching");
        }

        colorDrawerScrim = Helper.resolveColor(this, R.attr.colorDrawerScrim);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        startup = prefs.getString("startup", "unified");
        nav_expanded = getDrawerExpanded();
        nav_pinned = getDrawerPinned();
        nav_options = prefs.getBoolean("nav_options", true);

        // Fix imported settings from other device
        if (nav_expanded && nav_pinned && !canExpandAndPin())
            nav_pinned = false;

        infoTracker = new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(this));

        Configuration config = getResources().getConfiguration();
        boolean portrait2 = prefs.getBoolean("portrait2", false);
        boolean portrait2c = prefs.getBoolean("portrait2c", false);
        int portrait_min_size = prefs.getInt("portrait_min_size", 0);
        boolean landscape = prefs.getBoolean("landscape", true);
        int landscape_min_size = prefs.getInt("landscape_min_size", 0);
        int layout = (config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        Log.i("Orientation=" + config.orientation + " layout=" + layout +
                " portrait rows=" + portrait2 + " cols=" + portrait2c + " min=" + portrait_min_size +
                " landscape cols=" + landscape + " min=" + landscape_min_size);
        boolean duo = Helper.isSurfaceDuo();
        boolean canFold = Helper.canFold(this);
        boolean close_pane = prefs.getBoolean("close_pane", !duo && !canFold);
        boolean nav_categories = prefs.getBoolean("nav_categories", false);

        // 1=small, 2=normal, 3=large, 4=xlarge
        if (layout > 0)
            layout--;

        if (layout < (config.orientation == ORIENTATION_PORTRAIT ? portrait_min_size : landscape_min_size))
            layoutId = R.layout.activity_view_portrait;
        else if (config.orientation == ORIENTATION_PORTRAIT && portrait2c)
            layoutId = R.layout.activity_view_landscape_split;
        else if (config.orientation == ORIENTATION_PORTRAIT || !landscape)
            layoutId = (portrait2 ? R.layout.activity_view_portrait_split : R.layout.activity_view_portrait);
        else
            layoutId = R.layout.activity_view_landscape_split;

        view = LayoutInflater.from(this).inflate(layoutId, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        content_separator = findViewById(R.id.content_separator);
        content_pane = findViewById(R.id.content_pane);

        if (content_pane != null) {
            // Special: Surface Duo
            if (duo) {
                View content_frame = findViewById(R.id.content_frame);
                ViewGroup.LayoutParams lparam = content_frame.getLayoutParams();
                if (lparam instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) lparam).weight = 1; // 50/50
                    content_frame.setLayoutParams(lparam);
                }
                if (duo) {
                    // https://docs.microsoft.com/en-us/dual-screen/android/duo-dimensions
                    int seam = (Helper.isSurfaceDuo2() ? 26 : 34);
                    content_separator.getLayoutParams().width = Helper.dp2pixels(this, seam);
                }
            } else {
                int column_width = prefs.getInt("column_width", canFold ? 50 : 67);
                ViewGroup.LayoutParams lparam = content_pane.getLayoutParams();
                if (lparam instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) lparam).weight =
                            (float) (100 - column_width) / column_width * 2;
                    content_pane.setLayoutParams(lparam);
                }
            }
        }

        owner = new TwoStateOwner(this, "drawer");
        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
            public void onStateChanged() {
                Log.i("Drawer state=" + owner.getLifecycle().getCurrentState());
            }
        });
        drawerLayout = findViewById(R.id.drawer_layout);

        final ViewGroup childContent = (ViewGroup) drawerLayout.getChildAt(0);
        final ViewGroup childDrawer = (ViewGroup) drawerLayout.getChildAt(1);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                Log.i("Drawer closed");
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;

                owner.stop();

                drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED);
                childContent.setPaddingRelative(0, 0, 0, 0);

                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                Log.i("Drawer opened");
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;

                owner.start();

                if (nav_pinned) {
                    drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_OPEN);
                    int padding = childDrawer.getLayoutParams().width;
                    childContent.setPaddingRelative(padding, 0, 0, 0);
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                if (BuildConfig.DEBUG)
                    Log.i("Drawer slide=" + slideOffset);
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;

                if (slideOffset > 0)
                    owner.start();
                else
                    owner.stop();

                if (nav_pinned) {
                    int padding = Math.round(slideOffset * childDrawer.getLayoutParams().width);
                    childContent.setPaddingRelative(padding, 0, 0, 0);
                }
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        drawerContainer = findViewById(R.id.drawer_container);
        ibExpanderNav = drawerContainer.findViewById(R.id.ibExpanderNav);
        ibPin = drawerContainer.findViewById(R.id.ibPin);
        ibHide = drawerContainer.findViewById(R.id.ibHide);
        ibSettings = drawerContainer.findViewById(R.id.ibSettings);
        ibFetchMore = drawerContainer.findViewById(R.id.ibFetchMore);
        ibForceSync = drawerContainer.findViewById(R.id.ibForceSync);
        vSeparatorOptions = drawerContainer.findViewById(R.id.vSeparatorOptions);
        grpOptions = drawerContainer.findViewById(R.id.grpOptions);

        ibExpanderAccount = drawerContainer.findViewById(R.id.ibExpanderAccount);
        rvAccount = drawerContainer.findViewById(R.id.rvAccount);

        ibExpanderUnified = drawerContainer.findViewById(R.id.ibExpanderUnified);
        rvUnified = drawerContainer.findViewById(R.id.rvUnified);

        ibExpanderSearch = drawerContainer.findViewById(R.id.ibExpanderSearch);
        rvSearch = drawerContainer.findViewById(R.id.rvSearch);
        vSeparatorSearch = drawerContainer.findViewById(R.id.vSeparatorSearch);

        ibExpanderMenu = drawerContainer.findViewById(R.id.ibExpanderMenu);
        rvMenu = drawerContainer.findViewById(R.id.rvMenu);

        ibExpanderExtra = drawerContainer.findViewById(R.id.ibExpanderExtra);
        rvMenuExtra = drawerContainer.findViewById(R.id.rvMenuExtra);

        ViewGroup.LayoutParams lparam = drawerContainer.getLayoutParams();
        lparam.width = getDrawerWidth();
        drawerContainer.setLayoutParams(lparam);

        // Navigation expander
        ibExpanderNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav_expanded = !nav_expanded;
                if (nav_expanded && nav_pinned && !canExpandAndPin()) {
                    nav_pinned = false;
                    setDrawerPinned(nav_pinned);
                }
                setDrawerExpanded(nav_expanded);
            }
        });
        ibExpanderNav.setImageLevel(nav_expanded ? 0 : 1);

        // Navigation pinning
        ibPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nav_pinned = !nav_pinned;
                if (nav_pinned && nav_expanded && !canExpandAndPin()) {
                    nav_expanded = false;
                    setDrawerExpanded(nav_expanded);
                }
                setDrawerPinned(nav_pinned);
            }
        });
        ibPin.setImageLevel(nav_pinned ? 1 : 0);

        ibHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dview = LayoutInflater.from(ActivityView.this).inflate(R.layout.dialog_nav_options, null);
                new AlertDialog.Builder(ActivityView.this)
                        .setView(dview)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().putBoolean("nav_options", false).apply();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        // Navigation settings
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(ActivityView.this, owner, ibSettings);

                for (int i = 0; i < FragmentOptions.PAGE_TITLES.length; i++)
                    popupMenu.getMenu()
                            .add(Menu.NONE, i, i, FragmentOptions.PAGE_TITLES[i])
                            .setIcon(FragmentOptions.PAGE_ICONS[i]);

                popupMenu.insertIcons(ActivityView.this);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String tab = FragmentOptions.TAB_LABELS.get(item.getOrder());
                        startActivity(new Intent(ActivityView.this, ActivitySetup.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("tab", tab));
                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        ibSettings.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivity(new Intent(ActivityView.this, ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                return true;
            }
        });

        // Fetch more messages
        ibFetchMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putLong("folder", -1L); // Unified inbox

                FragmentDialogSync sync = new FragmentDialogSync();
                sync.setArguments(args);
                sync.show(getSupportFragmentManager(), "nav:fetch");
            }
        });

        // Force sync
        ibForceSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) throws Throwable {
                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            List<EntityAccount> accounts = db.account().getSynchronizingAccounts(null);
                            for (EntityAccount account : accounts) {
                                List<EntityFolder> folders = db.folder().getSynchronizingFolders(account.id);
                                for (EntityFolder folder : folders)
                                    EntityOperation.sync(context, folder.id, true, true, true);
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        ServiceSynchronize.reload(context, null, true, "refresh");
                        ServiceSend.start(context);

                        return null;
                    }

                    @Override
                    protected void onExecuted(Bundle args, Void data) {
                        ToastEx.makeText(ActivityView.this, R.string.title_force_sync, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getSupportFragmentManager(), ex);
                    }
                }.execute(ActivityView.this, new Bundle(), "nav:force");
            }
        });

        ibExpanderNav.setVisibility(nav_options ? View.VISIBLE : View.GONE);
        grpOptions.setVisibility(nav_expanded && nav_options ? View.VISIBLE : View.GONE);
        vSeparatorOptions.setVisibility(nav_options ? View.VISIBLE : View.GONE);

        // Accounts
        LinearLayoutManager llmAccounts = new LinearLayoutManager(this);
        rvAccount.setLayoutManager(llmAccounts);
        adapterNavAccount = new AdapterNavAccountFolder(this, this);
        rvAccount.setAdapter(adapterNavAccount);

        if (nav_categories) {
            LayoutInflater inflater = LayoutInflater.from(this);
            DividerItemDecoration categoryDecorator = new DividerItemDecoration(this, llmAccounts.getOrientation()) {
                @Override
                public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    int count = parent.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View view = parent.getChildAt(i);
                        int pos = parent.getChildAdapterPosition(view);

                        View header = getView(view, parent, pos);
                        if (header != null) {
                            canvas.save();
                            canvas.translate(0, parent.getChildAt(i).getTop() - header.getMeasuredHeight());
                            header.draw(canvas);
                            canvas.restore();
                        }
                    }
                }

                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    int pos = parent.getChildAdapterPosition(view);
                    View header = getView(view, parent, pos);
                    if (header == null)
                        outRect.setEmpty();
                    else
                        outRect.top = header.getMeasuredHeight();
                }

                private View getView(View view, RecyclerView parent, int pos) {
                    if (pos == NO_POSITION)
                        return null;

                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return null;

                    TupleAccountFolder prev = adapterNavAccount.getItemAtPosition(pos - 1);
                    TupleAccountFolder account = adapterNavAccount.getItemAtPosition(pos);
                    if (pos > 0 && prev == null)
                        return null;
                    if (account == null)
                        return null;

                    if (pos > 0) {
                        if (Objects.equals(prev.category, account.category))
                            return null;
                    } else {
                        if (account.category == null)
                            return null;
                    }

                    View header = inflater.inflate(R.layout.item_nav_group, parent, false);
                    TextView tvCategory = header.findViewById(R.id.tvCategory);

                    tvCategory.setText(account.category);

                    header.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

                    return header;
                }
            };
            rvAccount.addItemDecoration(categoryDecorator);
        }

        boolean nav_account = prefs.getBoolean("nav_account", true);
        boolean nav_folder = prefs.getBoolean("nav_folder", true);
        ibExpanderAccount.setImageLevel(nav_account || nav_folder ? 0 /* less */ : 1 /* more */);
        rvAccount.setVisibility(nav_account || nav_folder ? View.VISIBLE : View.GONE);

        ibExpanderAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean nav_account = prefs.getBoolean("nav_account", true);
                boolean nav_folder = prefs.getBoolean("nav_folder", true);
                boolean nav_quick = prefs.getBoolean("nav_quick", true);
                boolean expanded = (nav_account || nav_folder);

                if (expanded && nav_quick && adapterNavAccount.hasFolders())
                    nav_quick = false;
                else {
                    expanded = !expanded;
                    if (expanded)
                        nav_quick = true;
                }

                prefs.edit()
                        .putBoolean("nav_account", expanded)
                        .putBoolean("nav_folder", expanded)
                        .putBoolean("nav_quick", nav_quick)
                        .apply();

                adapterNavAccount.setFolders(nav_quick);

                if (expanded && nav_quick && adapterNavAccount.hasFolders())
                    ibExpanderAccount.setImageLevel(2 /* unfold less */);
                else
                    ibExpanderAccount.setImageLevel(expanded ? 0 /* less */ : 1 /* more */);
                rvAccount.setVisibility(expanded ? View.VISIBLE : View.GONE);
            }
        });

        // Unified system folders
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

        // Menus
        rvSearch.setLayoutManager(new LinearLayoutManager(this));
        adapterNavSearch = new AdapterNavSearch(this, this, getSupportFragmentManager());
        rvSearch.setAdapter(adapterNavSearch);

        boolean nav_search = prefs.getBoolean("nav_search", true);
        ibExpanderSearch.setImageLevel(nav_search ? 0 /* less */ : 1 /* more */);
        ibExpanderSearch.setVisibility(View.GONE);
        rvSearch.setVisibility(View.GONE);
        vSeparatorSearch.setVisibility(View.GONE);

        ibExpanderSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean nav_search = !prefs.getBoolean("nav_search", true);
                prefs.edit().putBoolean("nav_search", nav_search).apply();
                ibExpanderSearch.setImageLevel(nav_search ? 0 /* less */ : 1 /* more */);
                rvSearch.setVisibility(nav_search ? View.VISIBLE : View.GONE);
            }
        });

        // Menus
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        adapterNavMenu = new AdapterNavMenu(this, this);
        rvMenu.setAdapter(adapterNavMenu);

        boolean nav_menu = prefs.getBoolean("nav_menu", true);
        ibExpanderMenu.setImageLevel(nav_menu ? 0 /* less */ : 1 /* more */);
        rvMenu.setVisibility(nav_menu ? View.VISIBLE : View.GONE);

        ibExpanderMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean nav_menu = !prefs.getBoolean("nav_menu", true);
                prefs.edit().putBoolean("nav_menu", nav_menu).apply();
                ibExpanderMenu.setImageLevel(nav_menu ? 0 /* less */ : 1 /* more */);
                rvMenu.setVisibility(nav_menu ? View.VISIBLE : View.GONE);
            }
        });

        // Extra menus
        LinearLayoutManager llmMenuExtra = new LinearLayoutManager(this);
        rvMenuExtra.setLayoutManager(llmMenuExtra);
        adapterNavMenuExtra = new AdapterNavMenu(this, this);
        rvMenuExtra.setAdapter(adapterNavMenuExtra);

        final Drawable d = getDrawable(R.drawable.divider);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, llmMenuExtra.getOrientation()) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(view);
                NavMenuItem menu = (adapterNavMenuExtra == null ? null : adapterNavMenuExtra.get(pos));
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
                    getMainHandler().post(new RunnableEx("fullScroll") {
                        @Override
                        public void delegate() {
                            drawerContainer.fullScroll(View.FOCUS_DOWN);
                        }
                    });
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        // Initialize

        if (content_pane != null) {
            content_separator.setVisibility(close_pane ? View.GONE : View.INVISIBLE);
            content_pane.setVisibility(close_pane ? View.GONE : View.INVISIBLE);
        }

        FragmentManager fm = getSupportFragmentManager();

        int count = fm.getBackStackEntryCount();
        if (count > 1 && "thread".equals(fm.getBackStackEntryAt(count - 1).getName())) {
            Fragment fragment = fm.findFragmentByTag("thread");
            if (fragment != null) {
                if (fragment.getId() == (content_pane == null ? R.id.content_pane : R.id.content_frame)) {
                    Log.i("Moving pane=" + (content_pane != null) + " fragment=" + fragment);
                    fm.popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    Fragment newFragment = Helper.recreateFragment(fragment, fm);
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(content_pane == null ? R.id.content_frame : R.id.content_pane, newFragment, "thread")
                            .addToBackStack("thread");
                    ft.commit();
                }

                if (content_pane != null) {
                    content_separator.setVisibility(View.VISIBLE);
                    content_pane.setVisibility(View.VISIBLE);
                }
            }
        }

        if (getSupportFragmentManager().getFragments().size() == 0) {
            Intent intent = getIntent();
            boolean search = (intent != null && intent.hasExtra(Intent.EXTRA_PROCESS_TEXT));
            boolean standalone = (intent != null && intent.getBooleanExtra("standalone", false));
            boolean unified = (intent != null && "unified".equals(intent.getAction()));
            if (!search && !(standalone && !unified))
                init();
            else
                initialized = true;
        }

        if (savedInstanceState != null)
            drawerToggle.setDrawerIndicatorEnabled(savedInstanceState.getBoolean("fair:toggle"));

        if (!"inbox".equals(startup))
            checkFirst();
        checkBanner();
        checkCrash();

        Shortcuts.update(this, this);
    }

    public boolean isSplit() {
        return (layoutId == R.layout.activity_view_portrait_split ||
                layoutId == R.layout.activity_view_landscape_split);
    }

    @Override
    public void onBackPressedFragment() {
        backPressedCallback.handleOnBackPressed();
    }

    private OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (Helper.isKeyboardVisible(view))
                Helper.hideKeyboard(view);
            else
                onExit();
        }
    };

    private void init() {
        Bundle args = new Bundle();

        if ("inbox".equals(startup)) {
            new SimpleTask<EntityFolder>() {
                @Override
                protected void onPreExecute(Bundle args) {
                    initialized = false;
                }

                @Override
                protected EntityFolder onExecute(Context context, Bundle args) throws Throwable {
                    DB db = DB.getInstance(context);
                    return db.folder().getFolderPrimary(EntityFolder.INBOX);
                }

                @Override
                protected void onExecuted(Bundle args, EntityFolder inbox) {
                    FragmentBase fragment = new FragmentMessages();
                    if (inbox != null) {
                        args.putString("type", inbox.type);
                        args.putLong("account", inbox.account);
                        args.putLong("folder", inbox.id);
                    }
                    fragment.setArguments(args);
                    setFragment(fragment);
                    checkIntent();
                    checkFirst();
                    initialized = true;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getSupportFragmentManager(), ex);
                }
            }.execute(this, new Bundle(), "primary");
            return;
        }

        FragmentBase fragment;
        switch (startup) {
            case "accounts":
                fragment = new FragmentAccounts();
                args.putBoolean("settings", false);
                break;
            case "folders":
                fragment = new FragmentFolders();
                args.putBoolean("unified", true);
                break;
            case "primary":
                fragment = new FragmentFolders();
                args.putBoolean("primary", true);
                break;
            default:
                fragment = new FragmentMessages();
        }

        fragment.setArguments(args);
        setFragment(fragment);
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        for (Fragment existing : fm.getFragments())
            fragmentTransaction.remove(existing);
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("unified");
        fragmentTransaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("fair:intent", getIntent());
        outState.putBoolean("fair:toggle", drawerToggle == null || drawerToggle.isDrawerIndicatorEnabled());
        outState.putBoolean("fair:initialized", initialized);
        outState.putBoolean("fair:searching", searching);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Fixed menus

        final List<NavMenuItem> menus = new ArrayList<>();

        final NavMenuItem navOperations = new NavMenuItem(R.drawable.twotone_dns_24, R.string.menu_operations, new RunnableEx("view:operations") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuOperations();
            }
        });

        menus.add(navOperations);

        menus.add(new NavMenuItem(R.drawable.twotone_list_alt_24, R.string.title_log, new RunnableEx("view:log") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onShowLog();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_text_snippet_24, R.string.menu_answers, new RunnableEx("view:templates") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuAnswers();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_filter_alt_24, R.string.menu_rules, new RunnableEx("view:rules") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuRulesAccount();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_settings_24, R.string.menu_setup, new RunnableEx("view:settings") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuSetup();
            }
        }, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (BuildConfig.DEBUG)
                    try {
                        DnsBlockList.clearCache();
                        ContactInfo.clearCache(ActivityView.this);
                        ToastEx.makeText(ActivityView.this, R.string.title_completed, Toast.LENGTH_LONG).show();
                    } catch (Throwable ex) {
                        Log.unexpectedError(getSupportFragmentManager(), ex);
                    }
                return BuildConfig.DEBUG;
            }
        }));

        adapterNavMenu.set(menus, nav_expanded);

        // Collapsible menus

        List<NavMenuItem> extra = new ArrayList<>();

        extra.add(new NavMenuItem(R.drawable.twotone_help_24, R.string.menu_legend, new RunnableEx("view:legend") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuLegend();
            }
        }));

        extra.add(new NavMenuItem(R.drawable.twotone_support_24, R.string.menu_faq, new RunnableEx("view:support") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuFAQ();
            }
        }, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (DebugHelper.isAvailable()) {
                    if (!drawerLayout.isLocked(drawerContainer))
                        drawerLayout.closeDrawer(drawerContainer);
                    onDebugInfo();
                    return true;
                } else
                    return false;
            }
        }).setExternal(true));

        extra.add(new NavMenuItem(R.drawable.twotone_feedback_24, R.string.menu_issue, new RunnableEx("view:report") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuIssue();
            }
        }, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                CoalMine.check();
                return BuildConfig.DEBUG;
            }
        }).setExternal(true));

        extra.add(new NavMenuItem(R.drawable.twotone_language_24, R.string.menu_translate, new RunnableEx("view:translate") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuTranslate();
            }
        }).setExternal(true));

        if (Helper.isPlayStoreInstall() && false)
            extra.add(new NavMenuItem(R.drawable.twotone_bug_report_24, R.string.menu_test, new RunnableEx("view:test") {
                @Override
                public void delegate() {
                    if (!drawerLayout.isLocked(drawerContainer))
                        drawerLayout.closeDrawer(drawerContainer);
                    onMenuTest();
                }
            }).setExternal(true));

        extra.add(new NavMenuItem(R.drawable.twotone_account_circle_24, R.string.menu_privacy, new RunnableEx("view:privacy") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                onMenuPrivacy();
            }
        }).setExternal(true));

        extra.add(new NavMenuItem(R.drawable.twotone_info_24, R.string.menu_about, new RunnableEx("view:about") {
            @Override
            public void delegate() {
                onMenuAbout();
            }
        }, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                boolean play = Helper.isPlayStoreInstall();
                if (!play) {
                    if (!drawerLayout.isLocked(drawerContainer))
                        drawerLayout.closeDrawer(drawerContainer);
                    checkUpdate(true);
                    checkAnnouncements(true);
                }
                return !play;
            }
        }).setSeparated().setSubtitle(BuildConfig.VERSION_NAME));

        extra.add(new NavMenuItem(R.drawable.twotone_monetization_on_24, R.string.menu_pro, new RunnableEx("view:pro") {
            @Override
            public void delegate() {
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                startActivity(new Intent(ActivityView.this, ActivityBilling.class));
            }
        }).setExtraIcon(ActivityBilling.isPro(this) ? R.drawable.twotone_check_24 : 0));

        if ((Helper.isPlayStoreInstall() || BuildConfig.DEBUG))
            extra.add(new NavMenuItem(R.drawable.twotone_star_24, R.string.menu_rate, new RunnableEx("view:rate") {
                @Override
                public void delegate() {
                    if (!drawerLayout.isLocked(drawerContainer))
                        drawerLayout.closeDrawer(drawerContainer);
                    onMenuRate();
                }
            }).setExternal(true));

        adapterNavMenuExtra.set(extra, nav_expanded);

        // Live data

        DB db = DB.getInstance(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        db.account().liveAccountFolder(false).observe(owner, new Observer<List<TupleAccountFolder>>() {
            @Override
            public void onChanged(@Nullable List<TupleAccountFolder> accounts) {
                if (accounts == null)
                    accounts = new ArrayList<>();

                boolean nav_account = prefs.getBoolean("nav_account", true);
                boolean nav_folder = prefs.getBoolean("nav_folder", true);
                boolean nav_quick = prefs.getBoolean("nav_quick", true);
                boolean expanded = (nav_account || nav_folder);

                adapterNavAccount.set(accounts, nav_expanded, nav_quick);

                if (expanded && nav_quick && adapterNavAccount.hasFolders())
                    ibExpanderAccount.setImageLevel(2 /* unfold less */);
                else
                    ibExpanderAccount.setImageLevel(expanded ? 0 /* less */ : 1 /* more */);
            }
        });

        db.folder().liveUnified().observe(owner, new Observer<List<TupleFolderUnified>>() {
            @Override
            public void onChanged(List<TupleFolderUnified> folders) {
                if (folders == null)
                    folders = new ArrayList<>();
                adapterNavUnified.set(folders, nav_expanded);
            }
        });

        db.search().liveSearches().observe(owner, new Observer<List<EntitySearch>>() {
            @Override
            public void onChanged(List<EntitySearch> searches) {
                if (searches == null)
                    searches = new ArrayList<>();
                adapterNavSearch.set(searches, nav_expanded);

                boolean nav_search = prefs.getBoolean("nav_search", true);
                ibExpanderSearch.setVisibility(searches.size() > 0 ? View.VISIBLE : View.GONE);
                rvSearch.setVisibility(searches.size() > 0 && nav_search ? View.VISIBLE : View.GONE);
                vSeparatorSearch.setVisibility(searches.size() > 0 ? View.VISIBLE : View.GONE);
            }
        });

        db.operation().liveStats().observe(owner, new Observer<TupleOperationStats>() {
            private Boolean lastWarning = null;
            private Integer lastCount = null;

            @Override
            public void onChanged(TupleOperationStats stats) {
                boolean warning = (stats != null && stats.errors != null && stats.errors > 0);
                int count = (stats == null ? 0 : stats.pending);

                if (Objects.equals(lastWarning, warning) && Objects.equals(lastCount, count))
                    return;

                lastWarning = warning;
                lastCount = count;

                navOperations.setWarning(warning);
                navOperations.setCount(count);

                int pos = adapterNavMenu.getPosition(navOperations);
                if (pos < 0)
                    adapterNavMenu.notifyDataSetChanged();
                else
                    adapterNavMenu.notifyItemChanged(pos);
            }
        });

        Log.i("Drawer start");
        owner.start();

        setupDrawer();
        drawerToggle.syncState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private final java.util.function.Consumer<Integer> recordingCallback = new java.util.function.Consumer<Integer>() {
        @Override
        public void accept(Integer state) {
            if (state == WindowManager.SCREEN_RECORDING_STATE_VISIBLE)
                getMainHandler().post(new RunnableEx("screenrecording") {
                    @Override
                    public void delegate() {
                        ToastEx.makeText(ActivityView.this, R.string.title_screen_recording, Toast.LENGTH_LONG).show();
                    }
                });
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        infoTracker.addWindowLayoutInfoListener(this, Runnable::run, layoutStateChangeCallback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM &&
                hasPermission(Manifest.permission.DETECT_SCREEN_RECORDING))
            try {
                WindowManager wm = Helper.getSystemService(this, WindowManager.class);
                wm.addScreenRecordingCallback(Helper.getUIExecutor(), recordingCallback);
            } catch (Throwable ex) {
                Log.e(ex);
            }
    }

    @Override
    protected void onStop() {
        super.onStop();

        infoTracker.removeWindowLayoutInfoListener(layoutStateChangeCallback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM &&
                hasPermission(Manifest.permission.DETECT_SCREEN_RECORDING))
            try {
                WindowManager wm = Helper.getSystemService(this, WindowManager.class);
                wm.removeScreenRecordingCallback(recordingCallback);
            } catch (Throwable ex) {
                Log.e(ex);
            }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_VIEW_FOLDERS);
        iff.addAction(ACTION_VIEW_MESSAGES);
        iff.addAction(ACTION_SEARCH_ADDRESS);
        iff.addAction(ACTION_VIEW_THREAD);
        iff.addAction(ACTION_EDIT_FOLDER);
        iff.addAction(ACTION_VIEW_OUTBOX);
        iff.addAction(ACTION_EDIT_ANSWERS);
        iff.addAction(ACTION_EDIT_ANSWER);
        iff.addAction(ACTION_EDIT_RULES);
        iff.addAction(ACTION_EDIT_RULE);
        lbm.registerReceiver(receiver, iff);

        boolean open = drawerLayout.isDrawerOpen(drawerContainer);
        Log.i("Drawer resume open=" + open);
        if (open)
            owner.start();

        checkUpdate(false);
        checkAnnouncements(false);
        if (initialized || !"inbox".equals(startup))
            checkIntent();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);

        Log.i("Drawer pause");
        owner.stop();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(creceiver);
        super.onDestroy();
        infoTracker = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_FLAG_COLOR:
                    if (resultCode == RESULT_OK && data != null)
                        onSearchFlagColor(data.getBundleExtra("args"));
                    break;
                case REQUEST_RULES_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null)
                        onMenuRulesFolder(data.getBundleExtra("args"));
                    break;
                case REQUEST_RULES_FOLDER:
                    if (resultCode == RESULT_OK && data != null)
                        onMenuRules(data.getBundleExtra("args"));
                    break;
                case REQUEST_DEBUG_INFO:
                    if (resultCode == RESULT_OK && data != null)
                        onDebugInfo(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        nav_pinned = getDrawerPinned();
        setupDrawer();
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        super.onSharedPreferenceChanged(prefs, key);
        if ("nav_options".equals(key)) {
            nav_options = prefs.getBoolean(key, true);
            ibExpanderNav.setVisibility(nav_options ? View.VISIBLE : View.GONE);
            grpOptions.setVisibility(nav_expanded && nav_options ? View.VISIBLE : View.GONE);
            vSeparatorOptions.setVisibility(nav_options ? View.VISIBLE : View.GONE);
        }
    }

    private void setupDrawer() {
        if (nav_pinned) {
            drawerLayout.setScrimColor(Color.TRANSPARENT);
            drawerLayout.openDrawer(drawerContainer, false);
            drawerToggle.onDrawerOpened(drawerContainer);
        } else {
            drawerLayout.setScrimColor(colorDrawerScrim);
            drawerLayout.closeDrawer(drawerContainer, false);
            drawerToggle.onDrawerClosed(drawerContainer);
        }
    }

    private boolean getDrawerExpanded() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean legacy = prefs.getBoolean("nav_expanded", true);
        return prefs.getBoolean("nav_expanded_" + getOrientation(), legacy);
    }

    private void setDrawerExpanded(boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .remove("nav_expanded") // legacy
                .putBoolean("nav_expanded_" + getOrientation(), value)
                .apply();

        ViewGroup.LayoutParams lparam = drawerContainer.getLayoutParams();
        lparam.width = getDrawerWidth();
        drawerContainer.setLayoutParams(lparam);

        ViewGroup childContent = (ViewGroup) drawerLayout.getChildAt(0);
        ViewGroup childDrawer = (ViewGroup) drawerLayout.getChildAt(1);
        int padding = (nav_pinned ? childDrawer.getLayoutParams().width : 0);
        childContent.setPaddingRelative(padding, 0, 0, 0);

        grpOptions.setVisibility(nav_expanded ? View.VISIBLE : View.GONE);
        ibExpanderNav.setImageLevel(nav_expanded ? 0 : 1);

        adapterNavAccount.setExpanded(nav_expanded);
        adapterNavUnified.setExpanded(nav_expanded);
        adapterNavMenu.setExpanded(nav_expanded);
        adapterNavMenuExtra.setExpanded(nav_expanded);
    }

    private boolean getDrawerPinned() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration config = getResources().getConfiguration();
        boolean legacy;
        if (config.orientation == ORIENTATION_PORTRAIT)
            legacy = prefs.getBoolean("portrait3", false);
        else
            legacy = prefs.getBoolean("landscape3", true);
        return prefs.getBoolean("nav_pinned_" + getOrientation(), legacy);
    }

    private void setDrawerPinned(boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .remove("portrait3") // legacy
                .remove("landscape3") // legacy
                .putBoolean("nav_pinned_" + getOrientation(), value)
                .apply();

        drawerLayout.setDrawerLockMode(nav_pinned ? LOCK_MODE_LOCKED_OPEN : LOCK_MODE_UNLOCKED);
        drawerLayout.setScrimColor(nav_pinned ? Color.TRANSPARENT : colorDrawerScrim);
        drawerLayout.openDrawer(drawerContainer, false);

        ViewGroup.LayoutParams lparam = drawerContainer.getLayoutParams();
        lparam.width = getDrawerWidth();
        drawerContainer.setLayoutParams(lparam);

        ViewGroup childContent = (ViewGroup) drawerLayout.getChildAt(0);
        ViewGroup childDrawer = (ViewGroup) drawerLayout.getChildAt(1);
        int padding = (nav_pinned ? childDrawer.getLayoutParams().width : 0);
        childContent.setPaddingRelative(padding, 0, 0, 0);

        ibPin.setImageLevel(nav_pinned ? 1 : 0);
    }

    private String getOrientation() {
        Configuration config = getResources().getConfiguration();
        return (config.orientation == ORIENTATION_PORTRAIT ? "portrait" : "landscape");
    }

    private int getDrawerWidth() {
        if (!nav_expanded)
            return Helper.dp2pixels(this, 48); // one icon + padding

        if (nav_pinned)
            return getDrawerWidthPinned();
        else {
            int actionBarHeight = Helper.getActionBarHeight(this);
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int screenWidth = Math.min(dm.widthPixels, dm.heightPixels);
            // Screen width 320 - action bar 56 = 264 dp
            // Icons 6 x (24 width + 2x6 padding) = 216 dp
            int drawerWidth = screenWidth - actionBarHeight;

            // https://github.com/flutter/flutter/issues/123380
            // https://m3.material.io/components/navigation-drawer/specs
            Configuration config = getResources().getConfiguration();
            if (config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_XLARGE)) {
                int dp360 = Helper.dp2pixels(this, 360);
                return Math.min(drawerWidth, dp360);
            } else {
                int dp320 = Helper.dp2pixels(this, 320);
                return Math.min(drawerWidth, dp320);
            }
        }
    }

    private int getDrawerWidthPinned() {
        int dp300 = Helper.dp2pixels(this, 300);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int maxWidth = dm.widthPixels - dp300;
        return Math.min(dp300, maxWidth);
    }

    private boolean canExpandAndPin() {
        int dp200 = Helper.dp2pixels(this, 200);
        return (getDrawerWidthPinned() >= dp200);
    }

    private void onExit() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (!nav_pinned &&
                drawerLayout.isDrawerOpen(drawerContainer) &&
                (!drawerLayout.isLocked(drawerContainer) || count == 1))
            drawerLayout.closeDrawer(drawerContainer);
        else {
            if (exit || count > 1)
                performBack();
            else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityView.this);
                boolean double_back = prefs.getBoolean("double_back", false);

                Intent intent = getIntent();
                boolean standalone = (intent != null && intent.getBooleanExtra("standalone", false));

                if (searching || !double_back || standalone)
                    performBack();
                else {
                    exit = true;
                    ToastEx.makeText(ActivityView.this, R.string.app_exit, Toast.LENGTH_SHORT).show();
                    getMainHandler().postDelayed(new RunnableEx("view:exit") {
                        @Override
                        public void delegate() {
                            exit = false;
                        }
                    }, EXIT_DELAY);
                }
            }
        }
    }

    @Override
    public void onBackStackChanged() {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0)
            finish();
        else {
            if (count < lastBackStackCount) {
                Intent intent = getIntent();
                intent.setAction(null);
                Log.i("Reset intent");
            }
            lastBackStackCount = count;

            if (drawerLayout.isDrawerOpen(drawerContainer) &&
                    !drawerLayout.isLocked(drawerContainer))
                drawerLayout.closeDrawer(drawerContainer);
            drawerToggle.setDrawerIndicatorEnabled(count == 1);

            if (content_pane != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean close_pane = prefs.getBoolean("close_pane", !Helper.isSurfaceDuo());
                boolean thread = "thread".equals(getSupportFragmentManager().getBackStackEntryAt(count - 1).getName());
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_pane);
                int visibility = (!thread || fragment == null ? (close_pane ? View.GONE : View.INVISIBLE) : View.VISIBLE);
                content_separator.setVisibility(visibility);
                content_pane.setVisibility(visibility);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            if (nav_pinned)
                onExit();
            else {
                int count = getSupportFragmentManager().getBackStackEntryCount();
                if (count == 1 && drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void undo(String title, final Bundle args, final SimpleTask<Void> move, final SimpleTask<Void> show) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int undo_timeout = prefs.getInt("undo_timeout", 5000);

        if (undo_timeout == 0) {
            if (move != null) {
                move.execute(this, args, "undo:move");
                show.cancel(this);
            }
        } else
            undo(undo_timeout, title, args, move, show);
    }

    private Snackbar undo(long undo_timeout, String title, final Bundle args, final SimpleTask move, final SimpleTask show) {
        if (drawerLayout == null || drawerLayout.getChildCount() == 0) {
            Log.e("Undo: drawer missing");
            if (show != null) {
                show.execute(this, args, "undo:show");
                if (move != null)
                    move.cancel(this);
            }
            return null;
        }

        final View content = drawerLayout.getChildAt(0);

        final Snackbar snackbar = Helper.setSnackbarOptions(
                Snackbar.make(content, title, Snackbar.LENGTH_INDEFINITE));
        Helper.setSnackbarLines(snackbar, 7);

        lastSnackbar = snackbar;

        final Runnable timeout = new RunnableEx("view:undo") {
            @Override
            public void delegate() {
                Log.i("Undo timeout");
                snackbar.dismiss();
                if (move != null) {
                    move.execute(ActivityView.this, args, "undo:move");
                    if (show != null)
                        show.cancel(ActivityView.this);
                }
            }
        };

        snackbar.setAction(R.string.title_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Undo cancel");
                content.removeCallbacks(timeout);
                snackbar.dismiss();
                if (show != null) {
                    show.execute(ActivityView.this, args, "undo:show");
                    if (move != null)
                        move.cancel(ActivityView.this);
                }
            }
        });

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) content.getLayoutParams();
                lparam.bottomMargin = snackbar.getView().getHeight();
                content.setLayoutParams(lparam);
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) content.getLayoutParams();
                lparam.bottomMargin = 0;
                content.setLayoutParams(lparam);
            }
        });

        snackbar.show();

        content.postDelayed(timeout, undo_timeout);

        return snackbar;
    }

    private void checkFirst() {
        String version = BuildConfig.VERSION_NAME + BuildConfig.REVISION;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean first = prefs.getBoolean("first", true);
        boolean show_changelog = prefs.getBoolean("show_changelog", true);

        if (first)
            new FragmentDialogFirst().show(getSupportFragmentManager(), "first");
        else if (show_changelog) {
            // checkFirst: onCreate
            // checkIntent: onResume
            Intent intent = getIntent();
            String action = (intent == null ? null : intent.getAction());
            if (action != null &&
                    (action.startsWith("thread") || action.startsWith("widget")))
                return;

            String last = prefs.getString("changelog", null);
            if (!version.equals(last)) {
                Bundle args = new Bundle();
                args.putString("name", "CHANGELOG.md");
                args.putString("option", "show_changelog");
                FragmentDialogMarkdown fragment = new FragmentDialogMarkdown();
                fragment.setArguments(args);
                fragment.show(getSupportFragmentManager(), "changelog");

                prefs.edit().putString("changelog", version).apply();
            }
        }
    }

    private void checkBanner() {
        long now = new Date().getTime();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long banner_hidden = prefs.getLong("banner_hidden", 0);
        if (banner_hidden > 0 && now > banner_hidden)
            prefs.edit().remove("banner_hidden").apply();
    }

    private void checkCrash() {
        new SimpleTask<Long>() {
            @Override
            protected Long onExecute(Context context, Bundle args) throws Throwable {
                File file = new File(context.getFilesDir(), DebugHelper.CRASH_LOG_NAME);
                if (file.exists()) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        String line;
                        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                            while ((line = in.readLine()) != null)
                                sb.append(line).append("\r\n");
                        }

                        EntityMessage m = DebugHelper.getDebugInfo(context,
                                "crash", R.string.title_crash_info_remark, null, sb.toString(), null);
                        return (m == null ? null : m.id);
                    } finally {
                        Helper.secureDelete(file);
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Long id) {
                if (id == null)
                    return;
                startActivity(
                        new Intent(ActivityView.this, ActivityCompose.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("action", "edit")
                                .putExtra("id", id));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                ToastEx.makeText(ActivityView.this,
                        Log.formatThrowable(ex, false), Toast.LENGTH_LONG).show();
            }
        }.execute(this, new Bundle(), DebugHelper.CRASH_LOG_NAME);
    }

    private void checkUpdate(boolean always) {
        if (Helper.isPlayStoreInstall())
            return;
        if (!Helper.hasValidFingerprint(this) && !(always && BuildConfig.DEBUG))
            return;

        long now = new Date().getTime();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean updates = prefs.getBoolean("updates", true);
        boolean beta = prefs.getBoolean("beta", false) && false;
        boolean weekly = prefs.getBoolean("weekly", Helper.hasPlayStore(this));
        long last_update_check = prefs.getLong("last_update_check", 0);

        if (!always && !updates)
            return;
        if (!always && last_update_check + (weekly ? UPDATE_WEEKLY : UPDATE_DAILY) > now)
            return;

        prefs.edit().putLong("last_update_check", now).apply();

        Bundle args = new Bundle();
        args.putBoolean("always", always);
        args.putBoolean("beta", beta);

        new SimpleTask<UpdateInfo>() {
            @Override
            protected UpdateInfo onExecute(Context context, Bundle args) throws Throwable {
                boolean beta = args.getBoolean("beta");

                if (BuildConfig.DEBUG)
                    DnsHelper.test(context);

                StringBuilder response = new StringBuilder();
                HttpsURLConnection urlConnection = null;
                try {
                    URL latest = new URL(beta ? BuildConfig.BITBUCKET_DOWNLOADS_API : BuildConfig.GITHUB_LATEST_API);
                    urlConnection = (HttpsURLConnection) latest.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(UPDATE_TIMEOUT);
                    urlConnection.setConnectTimeout(UPDATE_TIMEOUT);
                    urlConnection.setDoOutput(false);
                    ConnectionHelper.setUserAgent(context, urlConnection);
                    urlConnection.connect();

                    int status = urlConnection.getResponseCode();
                    InputStream inputStream = (status == HttpsURLConnection.HTTP_OK
                            ? urlConnection.getInputStream() : urlConnection.getErrorStream());

                    if (inputStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = br.readLine()) != null)
                            response.append(line);
                    }

                    if (status == HttpsURLConnection.HTTP_FORBIDDEN) {
                        // {"message":"API rate limit exceeded for ...","documentation_url":"https://developer.github.com/v3/#rate-limiting"}
                        JSONObject jmessage = new JSONObject(response.toString());
                        if (jmessage.has("message"))
                            throw new IllegalArgumentException(jmessage.getString("message"));
                        throw new IOException("HTTP " + status + ": " + response);
                    }
                    if (status == HttpsURLConnection.HTTP_BAD_GATEWAY)
                        throw new IOException("HTTP " + status);
                    if (status != HttpsURLConnection.HTTP_OK)
                        throw new IOException("HTTP " + status + ": " + response);

                    JSONObject jroot = new JSONObject(response.toString());

                    if (beta) {
                        if (!jroot.has("values"))
                            throw new IOException("values field missing");

                        JSONArray jvalues = jroot.getJSONArray("values");
                        for (int i = 0; i < jvalues.length(); i++) {
                            JSONObject jitem = jvalues.getJSONObject(i);
                            if (!jitem.has("links"))
                                continue;

                            JSONObject jlinks = jitem.getJSONObject("links");
                            if (!jlinks.has("self"))
                                continue;

                            JSONObject jself = jlinks.getJSONObject("self");
                            if (!jself.has("href"))
                                continue;

                            // .../FairEmail-v1.1995a-play-preview-release.apk
                            String link = jself.getString("href");
                            if (!link.endsWith(".apk"))
                                continue;

                            int slash = link.lastIndexOf('/');
                            if (slash < 0)
                                continue;

                            String[] c = link.substring(slash + 1).split("-");
                            if (c.length < 4 ||
                                    !"FairEmail".equals(c[0]) ||
                                    c[1].length() < 8 ||
                                    !"github".equals(c[2]) ||
                                    !"update".equals(c[3]))
                                continue;

                            // v1.1995a
                            Integer version = Helper.parseInt(c[1].substring(3, c[1].length() - 1));
                            if (version == null)
                                continue;
                            char revision = c[1].charAt(c[1].length() - 1);

                            int v = BuildConfig.VERSION_CODE;
                            char r = BuildConfig.REVISION.charAt(0);
                            if (BuildConfig.DEBUG || version > v || (version == v && revision > r)) {
                                UpdateInfo info = new UpdateInfo();
                                info.tag_name = c[1];
                                info.html_url = BuildConfig.BITBUCKET_DOWNLOADS_URI;
                                info.download_url = link;
                                return info;
                            }
                        }
                    } else {
                        if (!jroot.has("tag_name") || jroot.isNull("tag_name"))
                            throw new IOException("tag_name field missing");
                        if (!jroot.has("assets") || jroot.isNull("assets"))
                            throw new IOException("assets section missing");

                        // Get update info
                        UpdateInfo info = new UpdateInfo();
                        info.tag_name = jroot.getString("tag_name");
                        info.html_url = BuildConfig.GITHUB_LATEST_URI;

                        // Check if new release
                        JSONArray jassets = jroot.getJSONArray("assets");
                        for (int i = 0; i < jassets.length(); i++) {
                            JSONObject jasset = jassets.getJSONObject(i);
                            if (jasset.has("name") && !jasset.isNull("name")) {
                                String name = jasset.getString("name");
                                if (name.endsWith(".apk") && name.contains("github")) {
                                    info.download_url = jasset.optString("browser_download_url");
                                    Log.i("Latest version=" + info.tag_name);
                                    if (BuildConfig.DEBUG)
                                        return info;
                                    try {
                                        if (Double.parseDouble(info.tag_name) <=
                                                Double.parseDouble(BuildConfig.VERSION_NAME))
                                            return null;
                                        else
                                            return info;
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                        if (BuildConfig.VERSION_NAME.equals(info.tag_name))
                                            return null;
                                        else
                                            return info;
                                    }
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
                boolean always = args.getBoolean("always");
                if (info == null) {
                    if (always)
                        ToastEx.makeText(ActivityView.this, R.string.title_no_update, Toast.LENGTH_LONG).show();
                    return;
                }

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(ActivityView.this, "update")
                                .setSmallIcon(R.drawable.baseline_get_app_white_24)
                                .setContentTitle(getString(R.string.title_updated, info.tag_name))
                                .setContentText(info.html_url)
                                .setAutoCancel(true)
                                .setShowWhen(false)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

                Intent update = new Intent(Intent.ACTION_VIEW, Uri.parse(info.html_url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent piUpdate = PendingIntentCompat.getActivity(
                        ActivityView.this, PI_UPDATE, update, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(piUpdate);

                Intent manage = new Intent(ActivityView.this, ActivitySetup.class)
                        .setAction("misc")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("tab", "misc");
                PendingIntent piManage = PendingIntentCompat.getActivity(
                        ActivityView.this, ActivitySetup.PI_MISC, manage, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionManage = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_settings_24,
                        getString(R.string.title_setup_manage),
                        piManage);
                builder.addAction(actionManage.build());

                if (!TextUtils.isEmpty(info.download_url)) {
                    Intent download = new Intent(Intent.ACTION_VIEW, Uri.parse(info.download_url))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent piDownload = PendingIntentCompat.getActivity(
                            ActivityView.this, 0, download, 0);
                    NotificationCompat.Action.Builder actionDownload = new NotificationCompat.Action.Builder(
                            R.drawable.twotone_cloud_download_24,
                            getString(R.string.title_download),
                            piDownload);
                    builder.addAction(actionDownload.build());
                }

                try {
                    NotificationManager nm =
                            Helper.getSystemService(ActivityView.this, NotificationManager.class);
                    if (NotificationHelper.areNotificationsEnabled(nm))
                        nm.notify(NotificationHelper.NOTIFICATION_UPDATE,
                                builder.build());
                } catch (Throwable ex) {
                    Log.w(ex);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (args.getBoolean("always")) {
                    boolean report = !(ex instanceof IllegalArgumentException || ex instanceof IOException);
                    Log.unexpectedError(getSupportFragmentManager(), ex, report);
                }
            }
        }.execute(this, args, "update:check");
    }

    private void checkAnnouncements(boolean always) {
        if (TextUtils.isEmpty(BuildConfig.ANNOUNCEMENT_URI))
            return;

        long now = new Date().getTime();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean announcements = prefs.getBoolean("announcements", true);
        long last_announcement_check = prefs.getLong("last_announcement_check", 0);

        if (!always && !announcements)
            return;
        if (!always && last_announcement_check + ANNOUNCEMENT_INTERVAL > now)
            return;

        prefs.edit().putLong("last_announcement_check", now).apply();

        Bundle args = new Bundle();
        args.putBoolean("always", always);

        new SimpleTask<List<Announcement>>() {
            @Override
            protected List<Announcement> onExecute(Context context, Bundle args) throws Throwable {
                StringBuilder response = new StringBuilder();
                HttpsURLConnection urlConnection = null;
                try {
                    URL latest = new URL(BuildConfig.ANNOUNCEMENT_URI);
                    urlConnection = (HttpsURLConnection) latest.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(ANNOUNCEMENT_TIMEOUT);
                    urlConnection.setConnectTimeout(ANNOUNCEMENT_TIMEOUT);
                    urlConnection.setDoOutput(false);
                    ConnectionHelper.setUserAgent(context, urlConnection);
                    urlConnection.connect();

                    int status = urlConnection.getResponseCode();
                    InputStream inputStream = (status == HttpsURLConnection.HTTP_OK
                            ? urlConnection.getInputStream() : urlConnection.getErrorStream());

                    if (inputStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = br.readLine()) != null)
                            response.append(line);
                    }

                    if (status != HttpsURLConnection.HTTP_OK)
                        throw new IOException("HTTP " + status + ": " + response);

                    DateFormat DTF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);

                    List<Announcement> announcements = new ArrayList<>();

                    JSONObject jroot = new JSONObject(response.toString());
                    JSONArray jannouncements = jroot.getJSONArray("Announcements");
                    for (int i = 0; i < jannouncements.length(); i++) {
                        JSONObject jannouncement = jannouncements.getJSONObject(i);

                        String language = Locale.getDefault().getLanguage();

                        String title = jannouncement.optString("Title." + language);
                        if (TextUtils.isEmpty(title))
                            title = jannouncement.getString("Title");

                        String text = jannouncement.optString("Text." + language);
                        if (TextUtils.isEmpty(text))
                            text = jannouncement.getString("Text");

                        Announcement announcement = new Announcement();
                        announcement.id = jannouncement.getInt("ID");
                        announcement.test = jannouncement.optBoolean("Test");
                        announcement.play = jannouncement.optBoolean("Play", false);
                        announcement.fdroid = jannouncement.optBoolean("FDroid", true);
                        announcement.title = title;
                        announcement.text = HtmlHelper.fromHtml(text, context);
                        announcement.minVersion = jannouncement.optInt("minVersion", 0);
                        announcement.maxVersion = jannouncement.optInt("maxVersion", BuildConfig.VERSION_CODE);
                        if (jannouncement.has("Link"))
                            announcement.link = Uri.parse(jannouncement.getString("Link"));
                        announcement.expires = DTF.parse(jannouncement.getString("Expires")
                                .replace("Z", "+00:00"));
                        announcements.add(announcement);
                    }

                    return announcements;
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }

            @Override
            protected void onExecuted(Bundle args, List<Announcement> announcements) {
                boolean always = args.getBoolean("always");

                NotificationManager nm =
                        Helper.getSystemService(ActivityView.this, NotificationManager.class);
                if (!NotificationHelper.areNotificationsEnabled(nm))
                    return;

                SharedPreferences.Editor editor = prefs.edit();

                for (Announcement announcement : announcements) {
                    String key = "announcement." + announcement.id;
                    if (announcement.isExpired()) {
                        editor.remove(key);
                        nm.cancel(announcement.id);
                    } else {
                        boolean notified = prefs.getBoolean(key, false);
                        if (notified && !always)
                            continue;
                        editor.putBoolean(key, true);

                        NotificationCompat.Builder builder =
                                new NotificationCompat.Builder(ActivityView.this, "announcements")
                                        .setSmallIcon(R.drawable.baseline_campaign_white_24)
                                        .setContentTitle(announcement.title)
                                        .setContentText(announcement.text)
                                        .setAutoCancel(true)
                                        .setShowWhen(true)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                                        .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                                        .setStyle(new NotificationCompat.BigTextStyle()
                                                .bigText(announcement.text));

                        if (announcement.link != null) {
                            Intent link = new Intent(Intent.ACTION_VIEW, announcement.link)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            PendingIntent piLink = PendingIntentCompat.getActivity(
                                    ActivityView.this, PI_ANNOUNCEMENT, link, PendingIntent.FLAG_UPDATE_CURRENT);
                            builder.setContentIntent(piLink);
                        }

                        Intent manage = new Intent(ActivityView.this, ActivitySetup.class)
                                .setAction("misc")
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("tab", "misc");
                        PendingIntent piManage = PendingIntentCompat.getActivity(
                                ActivityView.this, ActivitySetup.PI_MISC, manage, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Action.Builder actionManage = new NotificationCompat.Action.Builder(
                                R.drawable.twotone_settings_24,
                                getString(R.string.title_setup_manage),
                                piManage);
                        builder.addAction(actionManage.build());

                        nm.notify(announcement.id, builder.build());
                    }
                }

                editor.apply();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (args.getBoolean("always"))
                    Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "announcements:check");
    }

    private void checkIntent() {
        Intent intent = getIntent();
        Log.i("View intent=" + intent +
                " " + TextUtils.join(", ", Log.getExtras(intent.getExtras())));

        // Refresh from widget
        if (intent.getBooleanExtra("refresh", false)) {
            intent.removeExtra("refresh");

            int version = intent.getIntExtra("version", 0);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityView.this);
            boolean sync_on_launch = prefs.getBoolean("sync_on_launch", false);
            if (sync_on_launch || version < 1541)
                ServiceUI.sync(this, null);
        }

        String action = intent.getAction();
        if (action != null) {
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

            } else if ("outbox".equals(action)) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getSupportFragmentManager().popBackStack("unified", 0);

                onMenuOutbox();

            } else if (action.startsWith("thread")) {
                long id = Long.parseLong(action.split(":", 2)[1]);
                long account = intent.getLongExtra("account", -1);
                long folder = intent.getLongExtra("folder", -1);
                String type = intent.getStringExtra("type");
                boolean ignore = intent.getBooleanExtra("ignore", false);
                long group = intent.getLongExtra("group", -1L);
                if (ignore)
                    ServiceUI.ignore(this, id, group);
                intent.putExtra("id", id);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityView.this);
                boolean notify_open_folder = prefs.getBoolean("notify_open_folder", false);
                if (account > 0 && folder > 0 && !TextUtils.isEmpty(type) && notify_open_folder) {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        if (group >= 0)
                            getSupportFragmentManager().popBackStack("unified", 0);
                        else {
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
                }
                onViewThread(intent);

            } else if (action.startsWith("widget")) {
                long account = intent.getLongExtra("widget_account", -1);
                long folder = intent.getLongExtra("widget_folder", -1);
                String type = intent.getStringExtra("widget_type");
                if (account > 0 && folder > 0 && !TextUtils.isEmpty(type)) {
                    if (!intent.getBooleanExtra("standalone", false) &&
                            getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
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

            intent.setAction(null);
        }

        if (intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
            CharSequence csearch = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            String search = (csearch == null ? null : csearch.toString());
            if (!TextUtils.isEmpty(search)) {
                searching = true;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean fts = prefs.getBoolean("fts", false);

                BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();
                criteria.query = search;
                criteria.fts = fts;

                FragmentMessages.search(
                        ActivityView.this, ActivityView.this, getSupportFragmentManager(),
                        -1, -1, false, criteria);
            }

            intent.removeExtra(Intent.EXTRA_PROCESS_TEXT);
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

    private void onMenuRulesAccount() {
        new SimpleTask<EntityAccount>() {
            @Override
            protected EntityAccount onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);

                List<EntityAccount> accounts = db.account().getSynchronizingAccounts(null);
                if (accounts != null && accounts.size() == 1)
                    return accounts.get(0);

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, EntityAccount account) {
                if (account == null) {
                    FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
                    fragment.setArguments(new Bundle());
                    fragment.setTargetActivity(ActivityView.this, REQUEST_RULES_ACCOUNT);
                    fragment.show(getSupportFragmentManager(), "rules:account");
                } else {
                    args.putLong("account", account.id);
                    args.putInt("protocol", account.protocol);
                    args.putString("name", account.name);
                    onMenuRulesFolder(args);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "rules:account");
    }

    private void onSearchFlagColor(Bundle args) {
        BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();
        criteria.with_flagged = true;
        criteria.with_flag_color = args.getInt("color");

        final long account = args.getLong("account", -1);
        final long folder = args.getLong("folder", -1);

        FragmentMessages.search(
                this, this, getSupportFragmentManager(),
                account, folder,
                false,
                criteria);
    }

    private void onMenuRulesFolder(Bundle args) {
        args.putInt("icon", R.drawable.twotone_filter_alt_24);
        args.putString("title", getString(R.string.title_select));
        args.putLongArray("disabled", new long[0]);

        FragmentDialogSelectFolder fragment = new FragmentDialogSelectFolder();
        fragment.setArguments(args);
        fragment.setTargetActivity(this, REQUEST_RULES_FOLDER);
        fragment.show(getSupportFragmentManager(), "rules:folder");
    }

    private void onMenuRules(Bundle args) {
        FragmentRules fragment = new FragmentRules();
        fragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("rules");
        fragmentTransaction.commit();
    }

    private void onMenuSetup() {
        startActivity(new Intent(ActivityView.this, ActivitySetup.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
        startActivity(Helper.getIntentIssue(this, "View:issue"));
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

    private void onMenuRate() {
        new FragmentDialogRate().show(getSupportFragmentManager(), "rate");
    }

    private void onDebugInfo() {
        FragmentDialogDebug fragment = new FragmentDialogDebug();
        fragment.setArguments(new Bundle());
        fragment.setTargetActivity(this, REQUEST_DEBUG_INFO);
        fragment.show(getSupportFragmentManager(), "debug");
    }

    private void onDebugInfo(Bundle args) {
        Log.logBundle(args);
        new SimpleTask<Long>() {
            @Override
            protected void onPreExecute(Bundle args) {
                ToastEx.makeText(ActivityView.this, R.string.title_debug_info, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Long onExecute(Context context, Bundle args) throws IOException, JSONException {
                boolean send = args.getBoolean("send");

                EntityMessage m = DebugHelper.getDebugInfo(context,
                        "main", R.string.title_debug_info_remark, null, null, args);
                if (m == null)
                    return null;

                if (send) {
                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage draft = db.message().getMessage(m.id);
                        if (draft != null) {
                            draft.folder = EntityFolder.getOutbox(context).id;
                            db.message().updateMessage(draft);

                            EntityOperation.queue(context, draft, EntityOperation.SEND);

                            db.setTransactionSuccessful();

                            args.putBoolean("sent", true);
                            return null;
                        }
                    } finally {
                        db.endTransaction();
                    }
                }

                return m.id;
            }

            @Override
            protected void onExecuted(Bundle args, Long id) {
                if (id == null)
                    return;

                boolean sent = args.getBoolean("sent");
                if (sent) {
                    ToastEx.makeText(ActivityView.this, R.string.title_debug_info_send, Toast.LENGTH_LONG).show();
                    ServiceSend.start(ActivityView.this);
                    return;
                }

                if (id == null)
                    return;
                startActivity(new Intent(ActivityView.this, ActivityCompose.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("action", "edit")
                        .putExtra("id", id));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                boolean report = !(ex instanceof IllegalArgumentException);
                Log.unexpectedError(getSupportFragmentManager(), ex, report);
            }

        }.execute(this, args, "debug:info");
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
            String action = intent.getAction();
            if (ACTION_NEW_MESSAGE.equals(action))
                onNewMessage(intent);
            else if (ACTION_SENT_MESSAGE.equals(action))
                onSentMessage(intent);
        }
    };

    private List<Pair<Long, String>> updatedFolders = new ArrayList<>();

    boolean isFolderUpdated(Long folder, String type) {
        Pair<Long, String> key = new Pair<>(
                folder == null ? -1L : folder,
                folder == null ? type : null);
        boolean value = updatedFolders.contains(key);
        if (value)
            updatedFolders.remove(key);
        return value;
    }

    private void onNewMessage(Intent intent) {
        long folder = intent.getLongExtra("folder", -1);
        String type = intent.getStringExtra("type");
        boolean unified = intent.getBooleanExtra("unified", false);

        Pair<Long, String> pfolder = new Pair<>(folder, null);
        if (!updatedFolders.contains(pfolder))
            updatedFolders.add(pfolder);

        Pair<Long, String> ptype = new Pair<>(-1L, type);
        if (!updatedFolders.contains(ptype))
            updatedFolders.add(ptype);

        if (unified) {
            Pair<Long, String> punified = new Pair<>(-1L, null);
            if (!updatedFolders.contains(punified))
                updatedFolders.add(punified);
        }
    }

    private void onSentMessage(Intent intent) {
        long id = intent.getLongExtra("id", -1L);
        long at = intent.getLongExtra("at", -1L);
        String to = intent.getStringExtra("to");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean send_undo = prefs.getBoolean("send_undo", false);
        int send_delayed = prefs.getInt("send_delayed", 0) * 1000;

        if (!send_undo || send_delayed == 0)
            return;

        long timeout = at - new Date().getTime();
        if (timeout < 1000L)
            return;
        if (timeout > send_delayed)
            timeout = send_delayed;

        DB db = DB.getInstance(this);
        LiveData<TupleMessageEx> ld = db.message().liveMessage(id);

        Bundle args = new Bundle();
        args.putLong("id", id);

        final SimpleTask<Long> undo = new SimpleTask<Long>() {
            @Override
            protected void onPostExecute(Bundle args) {
                ld.removeObservers(ActivityView.this);
            }

            @Override
            protected Long onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                return ActivityCompose.undoSend(id, context);
            }

            @Override
            protected void onExecuted(Bundle args, Long id) {
                if (id == null)
                    return;

                startActivity(
                        new Intent(ActivityView.this, ActivityCompose.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("action", "edit")
                                .putExtra("id", id));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        };

        final SimpleTask<Void> done = new SimpleTask<Void>() {
            @Override
            protected void onPostExecute(Bundle args) {
                ld.removeObservers(ActivityView.this);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        };

        final Snackbar snackbar = undo(timeout, getString(R.string.title_sending_to, to), args, done, undo);
        if (snackbar == null)
            return;

        ld.observe(this, new Observer<TupleMessageEx>() {
            @Override
            public void onChanged(TupleMessageEx value) {
                if (value != null)
                    return;
                if (snackbar.isShown())
                    snackbar.dismiss();
                ld.removeObserver(this);
            }
        });
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
                else if (ACTION_SEARCH_ADDRESS.equals(action))
                    onSearchAddress(intent);
                else if (ACTION_VIEW_THREAD.equals(action))
                    onViewThread(intent);
                else if (ACTION_EDIT_FOLDER.equals(action))
                    onEditFolder(intent);
                else if (ACTION_VIEW_OUTBOX.equals(action))
                    onMenuOutbox();
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
        boolean unified = intent.getBooleanExtra("unified", false);
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            if (unified && "unified".equals(startup)) {
                getSupportFragmentManager().popBackStack("unified", 0);
                if (!drawerLayout.isLocked(drawerContainer))
                    drawerLayout.closeDrawer(drawerContainer);
                return;
            } else {
                getSupportFragmentManager().popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().popBackStack("messages", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean foldernav = prefs.getBoolean("foldernav", false);
        if (foldernav) {
            long account = intent.getLongExtra("account", -1);
            if (account > 0) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getSupportFragmentManager().popBackStack("unified", 0);
                onMenuFolders(account);
            }
        }

        Bundle args = new Bundle();
        args.putString("type", intent.getStringExtra("type"));
        args.putString("category", intent.getStringExtra("category"));
        args.putLong("account", intent.getLongExtra("account", -1));
        args.putLong("folder", intent.getLongExtra("folder", -1));

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment)
                .addToBackStack(unified ? "unified" : "messages");
        fragmentTransaction.commit();
    }

    private void onSearchAddress(Intent intent) {
        long account = intent.getLongExtra("account", -1);
        long folder = intent.getLongExtra("folder", -1);
        String query = intent.getStringExtra("query");
        boolean sender_only = intent.getBooleanExtra("sender_only", false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean fts = prefs.getBoolean("fts", false);

        BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();
        criteria.query = query;
        criteria.fts = fts;
        criteria.in_senders = true;
        if (sender_only) {
            criteria.in_recipients = false;
            criteria.in_subject = false;
            criteria.in_keywords = false;
            criteria.in_message = false;
            criteria.in_notes = false;
            criteria.in_trash = false;
        }

        FragmentMessages.search(
                this, this, getSupportFragmentManager(),
                account, folder, false, criteria);
    }

    private void onViewThread(Intent intent) {
        boolean found = intent.getBooleanExtra("found", false);

        if (lastSnackbar != null && lastSnackbar.isShown())
            lastSnackbar.dismiss();

        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            if (found) {
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                if (fragments.size() > 0) {
                    Bundle args = fragments.get(fragments.size() - 1).getArguments();
                    if (args != null && args.getBoolean("found"))
                        getSupportFragmentManager().popBackStack();
                }
            } else
                getSupportFragmentManager().popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("account", intent.getLongExtra("account", -1));
        args.putLong("folder", intent.getLongExtra("folder", -1));
        args.putString("thread", intent.getStringExtra("thread"));
        args.putLong("id", intent.getLongExtra("id", -1));
        args.putInt("lpos", intent.getIntExtra("lpos", -1));
        args.putBoolean("filter_archive", intent.getBooleanExtra("filter_archive", true));
        args.putBoolean("found", found);
        args.putString("searched", intent.getStringExtra("searched"));
        args.putBoolean("searchedPartial", intent.getBooleanExtra("searchedPartial", false));
        args.putBoolean("pinned", intent.getBooleanExtra("pinned", false));
        args.putString("msgid", intent.getStringExtra("msgid"));

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
        fragmentTransaction.replace(pane, fragment, "thread").addToBackStack("thread");
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
        String download_url;
    }

    private class Announcement {
        int id;
        boolean test;
        boolean play;
        boolean fdroid;
        String title;
        Spanned text;
        int minVersion;
        int maxVersion;
        Uri link;
        Date expires;

        boolean isExpired() {
            if (this.test && !BuildConfig.DEBUG)
                return true;
            if (this.play && !BuildConfig.PLAY_STORE_RELEASE)
                return true;
            if (this.fdroid && !BuildConfig.FDROID_RELEASE)
                return true;
            if (this.minVersion > BuildConfig.VERSION_CODE || BuildConfig.VERSION_CODE > this.maxVersion)
                return true;
            if (expires == null)
                return true;
            return (expires.getTime() < new Date().getTime());
        }
    }

    private final Consumer<WindowLayoutInfo> layoutStateChangeCallback = new Consumer<WindowLayoutInfo>() {
        @Override
        public void accept(WindowLayoutInfo info) {
            // Window layout=WindowLayoutInfo{
            //   DisplayFeatures[HardwareFoldingFeature {
            //       Bounds { [1104,0,1104,1840] }, type=FOLD, state=FLAT }
            //   ]
            // }
            EntityLog.log(ActivityView.this, "Window layout=" + info);
        }
    };
}
