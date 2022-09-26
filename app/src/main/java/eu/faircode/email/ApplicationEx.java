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

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.strictmode.Violation;
import android.text.TextUtils;
import android.util.Printer;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.emoji2.text.DefaultEmojiCompatConfig;
import androidx.emoji2.text.EmojiCompat;
import androidx.emoji2.text.FontRequestEmojiCompatConfig;
import androidx.preference.PreferenceManager;
import androidx.work.WorkManager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ApplicationEx extends Application
        implements androidx.work.Configuration.Provider, SharedPreferences.OnSharedPreferenceChangeListener {
    private Thread.UncaughtExceptionHandler prev = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(getLocalizedContext(base));
    }

    static Context getLocalizedContext(Context context) {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && false)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.contains("english")) {
            boolean english = prefs.getBoolean("english", false);
            if (english)
                prefs.edit()
                        .remove("english")
                        .putString("language", Locale.US.toLanguageTag())
                        .commit(); // apply won't work here
        }

        try {
            String language = prefs.getString("language", null);
            if (language != null) {
                if ("de-AT".equals(language) || "de-LI".equals(language))
                    language = "de-DE";
                Locale locale = Locale.forLanguageTag(language);
                Log.i("Set language=" + language + " locale=" + locale);
                Locale.setDefault(locale);
                Configuration config;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                    config = new Configuration(context.getResources().getConfiguration());
                else
                    config = new Configuration();
                config.setLocale(locale);
                return context.createConfigurationContext(config);
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return context;
    }

    @NonNull
    public androidx.work.Configuration getWorkManagerConfiguration() {
        return new androidx.work.Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        long start = new Date().getTime();
        Log.i("App create" +
                " version=" + BuildConfig.VERSION_NAME + BuildConfig.REVISION +
                " process=" + android.os.Process.myPid());
        Log.logMemory(this, "App");

        if (BuildConfig.DEBUG)
            UriHelper.test(this);

        CoalMine.install(this);

        registerActivityLifecycleCallbacks(lifecycleCallbacks);

        getMainLooper().setMessageLogging(new Printer() {
            @Override
            public void println(String msg) {
                if (BuildConfig.DEBUG)
                    Log.d("Loop: " + msg);
            }
        });

        if (BuildConfig.DEBUG &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                    .detectNonSdkApiUsage()
                    .penaltyListener(getMainExecutor(), new StrictMode.OnVmViolationListener() {
                        @Override
                        public void onVmViolation(Violation v) {
                            String message = v.getMessage();
                            if (message != null &&
                                    (message.contains("AbstractConscryptSocket") ||
                                            message.contains("computeFitSystemWindows") ||
                                            message.contains("makeOptionalFitsSystemWindows")))
                                return;

                            StackTraceElement[] stack = v.getStackTrace();
                            for (StackTraceElement ste : stack) {
                                String clazz = ste.getClassName();
                                if (clazz == null)
                                    continue;
                                if (clazz.startsWith("leakcanary."))
                                    return;
                                if ("com.sun.mail.util.WriteTimeoutSocket".equals(clazz))
                                    return;
                                if (clazz.startsWith("org.chromium") ||
                                        clazz.startsWith("com.android.webview.chromium") ||
                                        clazz.startsWith("androidx.appcompat.widget"))
                                    return;
                            }

                            Log.e(v);
                        }
                    })
                    .build();
            StrictMode.setVmPolicy(policy);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean crash_reports = prefs.getBoolean("crash_reports", false);
        final boolean leak_canary = prefs.getBoolean("leak_canary", false);
        final boolean load_emoji = prefs.getBoolean("load_emoji", false);

        prev = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
                if (!crash_reports && Log.isOwnFault(ex)) {
                    Log.e(ex);

                    if (BuildConfig.BETA_RELEASE ||
                            !Helper.isPlayStoreInstall())
                        Log.writeCrashLog(ApplicationEx.this, ex);

                    if (prev != null)
                        prev.uncaughtException(thread, ex);
                } else {
                    Log.w(ex);
                    System.exit(1);
                }
            }
        });

        Log.setup(this);
        CoalMine.setup(leak_canary);

        upgrade(this);

        try {
            boolean tcp_keep_alive = prefs.getBoolean("tcp_keep_alive", false);
            System.setProperty("fairemail.tcp_keep_alive", Boolean.toString(tcp_keep_alive));
        } catch (Throwable ex) {
            Log.e(ex);
        }

        prefs.registerOnSharedPreferenceChangeListener(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            NotificationHelper.createNotificationChannels(this);

        DB.setupViewInvalidation(this);

        if (Helper.hasWebView(this))
            CookieManager.getInstance().setAcceptCookie(false);

        // https://issuetracker.google.com/issues/233525229
        Log.i("Load emoji=" + load_emoji);
        if (!load_emoji)
            try {
                FontRequestEmojiCompatConfig crying = DefaultEmojiCompatConfig.create(this);
                if (crying != null) {
                    crying.setMetadataLoadStrategy(EmojiCompat.LOAD_STRATEGY_MANUAL);
                    EmojiCompat.init(crying);
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }

        EmailProvider.init(this);
        EncryptionHelper.init(this);
        MessageHelper.setSystemProperties(this);

        ContactInfo.init(this);

        DisconnectBlacklist.init(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ServiceSynchronize.watchdog(this);
            ServiceSend.watchdog(this);
        }

        boolean work_manager = prefs.getBoolean("work_manager", true);
        Log.i("Work manager=" + work_manager);
        if (work_manager) {
            // Legacy
            try {
                WorkManager.getInstance(this).cancelUniqueWork("WorkerWatchdog");
            } catch (IllegalStateException ex) {
                Log.e(ex);
            }

            WorkerAutoUpdate.init(this);
            WorkerCleanup.init(this);
        }

        registerReceiver(onScreenOff, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        long end = new Date().getTime();
        Log.i("App created " + (end - start) + " ms");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            switch (key) {
                case "enabled":
                    ServiceSynchronize.reschedule(this);
                    WorkerCleanup.init(this);
                    ServiceSynchronize.scheduleWatchdog(this);
                    WidgetSync.update(this);
                    break;
                case "poll_interval":
                case "schedule":
                case "schedule_start":
                case "schedule_end":
                case "schedule_day0":
                case "schedule_day1":
                case "schedule_day2":
                case "schedule_day3":
                case "schedule_day4":
                case "schedule_day5":
                case "schedule_day6":
                    ServiceSynchronize.reschedule(this);
                    break;
                case "check_blocklist":
                case "use_blocklist":
                    DnsBlockList.clearCache();
                    break;
                case "watchdog":
                    ServiceSynchronize.scheduleWatchdog(this);
                    break;
                case "secure": // privacy
                case "load_emoji": // privacy
                case "shortcuts": // misc
                case "language": // misc
                case "wal": // misc
                    // Should be excluded for import
                    restart(this, key);
                    break;
                case "debug":
                case "log_level":
                    Log.setLevel(this);
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static void restart(Context context, String reason) {
        Log.i("Restart because " + reason);
        Intent intent = new Intent(context, ActivityMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    @Override
    public void onTrimMemory(int level) {
        Log.logMemory(this, "Trim memory level=" + level);
        Map<String, String> crumb = new HashMap<>();
        crumb.put("level", Integer.toString(level));
        Log.breadcrumb("trim", crumb);
        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        Log.logMemory(this, "Low memory");
        Map<String, String> crumb = new HashMap<>();
        crumb.put("free", Integer.toString(Log.getFreeMemMb()));
        Log.breadcrumb("low", crumb);

        ContactInfo.clearCache(this, false);

        super.onLowMemory();
    }

    static void upgrade(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int version = prefs.getInt("version", BuildConfig.VERSION_CODE);
        if (version != BuildConfig.VERSION_CODE)
            EntityLog.log(context, "Upgrading from " + version + " to " + BuildConfig.VERSION_CODE);

        SharedPreferences.Editor editor = prefs.edit();

        if (version < BuildConfig.VERSION_CODE)
            editor.remove("crash_report_count");

        if (!BuildConfig.TEST_RELEASE)
            editor.remove("test1").remove("test2").remove("test3").remove("test4").remove("test5");

        if (version < 468) {
            editor.remove("notify_trash");
            editor.remove("notify_archive");
            editor.remove("notify_reply");
            editor.remove("notify_flag");
            editor.remove("notify_seen");

        } else if (version < 601) {
            editor.putBoolean("contact_images", prefs.getBoolean("autoimages", true));
            editor.remove("autoimages");

        } else if (version < 612) {
            if (prefs.getBoolean("autonext", false))
                editor.putString("onclose", "next");
            editor.remove("autonext");

        } else if (version < 693) {
            editor.remove("message_swipe");
            editor.remove("message_select");

        } else if (version < 696) {
            String theme = prefs.getString("theme", "light");
            if ("grey".equals(theme))
                editor.putString("theme", "grey_dark");

            if (prefs.contains("ascending")) {
                editor.putBoolean("ascending_list", prefs.getBoolean("ascending", false));
                editor.remove("ascending");
            }

        } else if (version < 701) {
            if (prefs.getBoolean("suggest_local", false)) {
                editor.putBoolean("suggest_sent", true);
                editor.remove("suggest_local");
            }

        } else if (version < 703) {
            if (!prefs.getBoolean("style_toolbar", true)) {
                editor.putBoolean("compose_media", false);
                editor.remove("style_toolbar");
            }

        } else if (version < 709) {
            if (prefs.getBoolean("swipe_reversed", false)) {
                editor.putBoolean("reversed", true);
                editor.remove("swipe_reversed");
            }

        } else if (version < 741)
            editor.remove("send_dialog");

        else if (version < 751) {
            if (prefs.contains("notify_snooze_duration")) {
                int minutes = prefs.getInt("notify_snooze_duration", 60);
                int hours = (int) Math.ceil(minutes / 60.0);
                editor.putInt("default_snooze", hours);
                editor.remove("notify_snooze_duration");
            }

        } else if (version < 819) {
            if (prefs.contains("no_history")) {
                editor.putBoolean("secure", prefs.getBoolean("no_history", false));
                editor.remove("no_history");
            }

            if (prefs.contains("zoom")) {
                int zoom = prefs.getInt("zoom", 1);
                editor.putInt("view_zoom", zoom);
                editor.putInt("compose_zoom", zoom);
                editor.remove("zoom");
            }

        } else if (version < 844) {
            if (prefs.getBoolean("schedule", false))
                editor.putBoolean("enabled", true);

        } else if (version < 874) {
            if (prefs.contains("experiments") &&
                    prefs.getBoolean("experiments", false))
                editor.putBoolean("quick_filter", true);
            editor.remove("experiments");

        } else if (version < 889) {
            if (prefs.contains("autoresize")) {
                boolean autoresize = prefs.getBoolean("autoresize", true);
                editor.putBoolean("resize_images", autoresize);
                editor.putBoolean("resize_attachments", autoresize);
                editor.remove("autoresize");
            }
        } else if (version < 930) {
            boolean large = context.getResources().getConfiguration()
                    .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
            editor.putBoolean("landscape3", large);
        } else if (version < 949) {
            if (prefs.contains("automove")) {
                boolean automove = prefs.getBoolean("automove", false);
                editor.putBoolean("move_1_confirmed", automove);
                editor.remove("automove");
            }
        } else if (version < 972) {
            if (prefs.contains("signature_end")) {
                boolean signature_end = prefs.getBoolean("signature_end", false);
                if (signature_end)
                    editor.putInt("signature_location", 2);
                editor.remove("signature_end");
            }
        } else if (version < 978) {
            if (!prefs.contains("poll_interval"))
                editor.putInt("poll_interval", 0);
            editor.remove("first");
        } else if (version < 1021) {
            boolean highlight_unread = prefs.getBoolean("highlight_unread", false);
            if (!highlight_unread)
                editor.putBoolean("highlight_unread", highlight_unread);
        } else if (version < 1121) {
            if (!Helper.isPlayStoreInstall())
                editor.putBoolean("experiments", true);
        } else if (version < 1124) {
            editor.remove("experiments");
        } else if (version < 1181) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                editor.remove("background_service");
        } else if (version < 1195)
            editor.remove("auto_optimize");
        else if (version < 1229) {
            boolean monospaced = prefs.getBoolean("monospaced", false);
            if (monospaced && !BuildConfig.DEBUG)
                editor.putBoolean("text_font", false);
        } else if (version < 1238) {
            if (!prefs.contains("subject_ellipsize"))
                editor.putString("subject_ellipsize", "middle");
            if (!prefs.contains("auto_optimize"))
                editor.putBoolean("auto_optimize", false);
        } else if (version < 1253) {
            int threads = prefs.getInt("query_threads", 4);
            if (threads == 4)
                editor.remove("query_threads");
        } else if (version < 1264) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N ||
                    "Blackview".equalsIgnoreCase(Build.MANUFACTURER) ||
                    "OnePlus".equalsIgnoreCase(Build.MANUFACTURER) ||
                    "HUAWEI".equalsIgnoreCase(Build.MANUFACTURER))
                editor.putInt("query_threads", 2);
        } else if (version < 1274)
            ContactInfo.clearCache(context); // Favicon background
        else if (version < 1336) {
            if (!prefs.contains("beige"))
                editor.putBoolean("beige", false);
        } else if (version < 1385)
            editor.remove("parse_classes");
        else if (version < 1401)
            editor.remove("tcp_keep_alive");
        else if (version < 1407)
            editor.remove("print_html_confirmed");
        else if (version < 1413)
            editor.remove("experiments");
        else if (version < 1439) {
            if (!BuildConfig.DEBUG)
                editor.remove("experiments");
        } else if (version < 1461) {
            if (!prefs.contains("theme"))
                editor.putString("theme", "blue_orange_light");
        } else if (version < 1463) {
            if (!prefs.contains("autoscroll"))
                editor.putBoolean("autoscroll", true);
        } else if (version < 1477) {
            if (!BuildConfig.DEBUG)
                editor.remove("experiments");
        } else if (version < 1524) {
            if (BuildConfig.PLAY_STORE_RELEASE)
                editor.remove("experiments");
        } else if (version < 1525) {
            if (!prefs.contains("download"))
                editor.putInt("download", 512 * 1024);
        } else if (version < 1533) {
            if (!prefs.contains("biometrics_notify"))
                editor.putBoolean("biometrics_notify", false);
        } else if (version < 1535) {
            editor.remove("identities_asked");
            editor.remove("identities_primary_hint");
        } else if (version < 1539) {
            if (!prefs.contains("double_back"))
                editor.putBoolean("double_back", true);
        } else if (version < 1540) {
            Map<String, ?> all = prefs.getAll();
            for (String key : all.keySet())
                if (key.startsWith("widget.") && key.endsWith(".semi")) {
                    String[] k = key.split("\\.");
                    if (k.length == 3)
                        try {
                            int appWidgetId = Integer.parseInt(k[1]);
                            editor.remove("widget." + appWidgetId + ".background");
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                }
        } else if (version < 1556) {
            if (prefs.contains("last_search")) {
                editor.putString("last_search1", prefs.getString("last_search", null));
                editor.remove("last_search");
            }
        } else if (version < 1558) {
            if (!prefs.contains("button_extra"))
                editor.putBoolean("button_extra", true);
        } else if (version < 1598) {
            if (prefs.contains("deepl")) {
                String key = prefs.getString("deepl", null);
                editor.putString("deepl_key", key).remove("deepl");
            }
        } else if (version < 1630) {
            boolean experiments = prefs.getBoolean("experiments", false);
            if (experiments)
                editor.putBoolean("deepl_enabled", true);
        } else if (version < 1678) {
            Configuration config = context.getResources().getConfiguration();
            boolean normal = config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL);
            if (!normal) {
                if (!prefs.contains("landscape"))
                    editor.putBoolean("landscape", false);
                if (!prefs.contains("landscape3"))
                    editor.putBoolean("landscape3", false);
            }
        } else if (version < 1721) {
            if (!prefs.contains("discard_delete"))
                editor.putBoolean("discard_delete", false);
        } else if (version < 1753)
            repairFolders(context);
        else if (version < 1772)
            editor.remove("conversation_actions");
        else if (version < 1781) {
            if (prefs.contains("sort")) {
                String sort = prefs.getString("sort", "time");
                editor.putString("sort_unified", sort);
            }
            if (prefs.contains("ascending_list")) {
                boolean ascending = prefs.getBoolean("ascending_list", false);
                editor.putBoolean("ascending_unified", ascending);
            }
        } else if (version < 1835) {
            boolean monospaced = prefs.getBoolean("monospaced", false);

            String compose_font = prefs.getString("compose_font", "");
            if (TextUtils.isEmpty(compose_font))
                editor.putString("compose_font", monospaced ? "monospace" : "sans-serif");

            if (monospaced) {
                String display_font = prefs.getString("display_font", "");
                if (TextUtils.isEmpty(display_font))
                    editor.putString("display_font", "monospace");
            }

            editor.remove("monospaced");
        } else if (version < 1837) {
            if (!prefs.contains("compact_folders"))
                editor.putBoolean("compact_folders", false);
        } else if (version < 1839) {
            boolean reply_all = prefs.getBoolean("reply_all", false);
            if (reply_all)
                editor.remove("reply_all").putString("answer_action", "reply_all");
        } else if (version < 1847) {
            if (Helper.isAccessibilityEnabled(context))
                editor.putBoolean("send_chips", false);
        } else if (version < 1855) {
            if (!prefs.contains("preview_lines"))
                editor.putInt("preview_lines", 2);
        } else if (version < 1874) {
            boolean cards = prefs.getBoolean("cards", true);
            if (!cards)
                editor.remove("view_padding");
        } else if (version < 1888) {
            int class_min_difference = prefs.getInt("class_min_difference", 50);
            if (class_min_difference == 0)
                editor.putBoolean("classification", false);
        } else if (version < 1918) {
            if (prefs.contains("browse_links")) {
                boolean browse_links = prefs.getBoolean("browse_links", false);
                editor.remove("browse_links")
                        .putBoolean("open_with_tabs", !browse_links);
            }
        } else if (version < 1927) {
            if (!prefs.contains("auto_identity"))
                editor.putBoolean("auto_identity", true);
        } else if (version < 1931)
            editor.remove("button_force_light").remove("fake_dark");
        else if (version < 1933) {
            editor.putBoolean("lt_enabled", true);
            if (prefs.contains("disable_top")) {
                editor.putBoolean("use_top", !prefs.getBoolean("disable_top", false));
                editor.remove("disable_top");
            }
        } else if (version < 1947)
            editor.putBoolean("accept_unsupported", true);
        else if (version < 1951) {
            if (prefs.contains("open_unsafe"))
                editor.putBoolean("open_safe", !prefs.getBoolean("open_unsafe", true));
        } else if (version < 1955) {
            if (!prefs.contains("doubletap"))
                editor.putBoolean("doubletap", true);
        } else if (version < 1960)
            editor.remove("sqlite_auto_vacuum");
        else if (version < 1961) {
            if (!prefs.contains("photo_picker"))
                editor.putBoolean("photo_picker", true);
        } else if (version < 1966)
            editor.remove("hide_timezone");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !BuildConfig.DEBUG)
            editor.remove("background_service");

        if (version < BuildConfig.VERSION_CODE)
            editor.putInt("previous_version", version);
        editor.putInt("version", BuildConfig.VERSION_CODE);

        editor.apply();
    }

    static void repairFolders(Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("Repair folders");
                    DB db = DB.getInstance(context);

                    List<EntityAccount> accounts = db.account().getAccounts();
                    if (accounts == null)
                        return;

                    for (EntityAccount account : accounts) {
                        if (account.protocol != EntityAccount.TYPE_IMAP)
                            continue;

                        EntityFolder inbox = db.folder().getFolderByType(account.id, EntityFolder.INBOX);
                        if (inbox == null || !inbox.synchronize) {
                            List<EntityFolder> folders = db.folder().getFolders(account.id, false, false);
                            if (folders == null)
                                continue;

                            for (EntityFolder folder : folders) {
                                if (inbox == null && "inbox".equalsIgnoreCase(folder.name))
                                    folder.type = EntityFolder.INBOX;

                                if (!folder.local &&
                                        !EntityFolder.USER.equals(folder.type) &&
                                        !EntityFolder.SYSTEM.equals(folder.type)) {
                                    EntityLog.log(context, "Repairing " + account.name + ":" + folder.type);
                                    folder.setProperties();
                                    folder.setSpecials(account);
                                    db.folder().updateFolder(folder);
                                }
                            }
                        }
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }).start();
    }

    private final ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifecycleCallbacks() {
        private long last = 0;

        @Override
        public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            log(activity, "onActivityPreCreated");
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            log(activity, "onActivityCreated");
        }

        @Override
        public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            log(activity, "onActivityPostCreated");
        }

        @Override
        public void onActivityPreStarted(@NonNull Activity activity) {
            log(activity, "onActivityPreStarted");
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            log(activity, "onActivityStarted");
        }

        @Override
        public void onActivityPostStarted(@NonNull Activity activity) {
            log(activity, "onActivityPostStarted");
        }

        @Override
        public void onActivityPreResumed(@NonNull Activity activity) {
            log(activity, "onActivityPreResumed");
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            log(activity, "onActivityResumed");
        }

        @Override
        public void onActivityPostResumed(@NonNull Activity activity) {
            log(activity, "onActivityPostResumed");
        }

        @Override
        public void onActivityPrePaused(@NonNull Activity activity) {
            log(activity, "onActivityPrePaused");
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            log(activity, "onActivityPaused");
        }

        @Override
        public void onActivityPostPaused(@NonNull Activity activity) {
            log(activity, "onActivityPostPaused");
        }

        @Override
        public void onActivityPreStopped(@NonNull Activity activity) {
            log(activity, "onActivityPreStopped");
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            log(activity, "onActivityStopped");
        }

        @Override
        public void onActivityPostStopped(@NonNull Activity activity) {
            log(activity, "onActivityPostStopped");
        }

        @Override
        public void onActivityPreSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            log(activity, "onActivityPreSaveInstanceState");
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            log(activity, "onActivitySaveInstanceState");
        }

        @Override
        public void onActivityPostSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            log(activity, "onActivityPostSaveInstanceState");
        }

        @Override
        public void onActivityPreDestroyed(@NonNull Activity activity) {
            log(activity, "onActivityPreDestroyed");
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            log(activity, "onActivityDestroyed");
            Helper.clearViews(activity);
        }

        @Override
        public void onActivityPostDestroyed(@NonNull Activity activity) {
            log(activity, "onActivityPostDestroyed");
        }

        private void log(@NonNull Activity activity, @NonNull String what) {
            long start = last;
            last = SystemClock.elapsedRealtime();
            long elapsed = (start == 0 ? 0 : last - start);
            Log.i(activity.getClass().getSimpleName() + " " + what + " " + elapsed + " ms");
        }
    };

    private final BroadcastReceiver onScreenOff = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Received " + intent);
            Log.logExtras(intent);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean autolock = prefs.getBoolean("autolock", true);
            if (autolock)
                Helper.clearAuthentication(ApplicationEx.this);
        }
    };

    private static Handler handler = null;

    synchronized static Handler getMainHandler() {
        if (handler == null)
            handler = new Handler(Looper.getMainLooper());
        return handler;
    }
}
