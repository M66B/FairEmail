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
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;
import androidx.emoji2.text.DefaultEmojiCompatConfig;
import androidx.emoji2.text.EmojiCompat;
import androidx.emoji2.text.FontRequestEmojiCompatConfig;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
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

    private static final Object lock = new Object();
    private static final Map<Integer, Context> themeCache = new HashMap<>();

    @Override
    protected void attachBaseContext(Context base) {
        FairEmailLoggingProvider.setup(base);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Log.i("App shutdown" +
                        " version=" + BuildConfig.VERSION_NAME + BuildConfig.REVISION +
                        " process=" + android.os.Process.myPid());
            }
        });

        super.attachBaseContext(getLocalizedContext(base));
    }

    static Context getLocalizedContext(Context context) {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && false)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());

        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            if (prefs.contains("english")) {
                boolean english = prefs.getBoolean("english", false);
                if (english)
                    prefs.edit()
                            .remove("english")
                            .putString("language", Locale.US.toLanguageTag())
                            .commit(); // apply won't work here
            }

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
            /*
                Redmi zircon / Android 15
                Exception java.lang.RuntimeException:
                  at android.app.LoadedApk.makeApplicationInner (LoadedApk.java:1548)
                  at android.app.LoadedApk.makeApplicationInner (LoadedApk.java:1469)
                  at android.app.ActivityThread.handleBindApplication (ActivityThread.java:8185)
                  at android.app.ActivityThread.-$$Nest$mhandleBindApplication (Unknown Source)
                  at android.app.ActivityThread$H.handleMessage (ActivityThread.java:2679)
                  at android.os.Handler.dispatchMessage (Handler.java:107)
                  at android.os.Looper.loopOnce (Looper.java:249)
                  at android.os.Looper.loop (Looper.java:337)
                  at android.app.ActivityThread.main (ActivityThread.java:9503)
                  at java.lang.reflect.Method.invoke
                  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run (RuntimeInit.java:636)
                  at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:1005)
                Caused by java.lang.IllegalStateException: SharedPreferences in credential encrypted storage are not available until after user (id 0) is unlocked
                  at android.app.ContextImpl.getSharedPreferences (ContextImpl.java:643)
                  at android.app.ContextImpl.getSharedPreferences (ContextImpl.java:621)
                  at androidx.preference.PreferenceManager.getDefaultSharedPreferences (PreferenceManager.java:119)
                  at eu.faircode.email.ApplicationEx.getLocalizedContext (ApplicationEx.java:90)
                  at eu.faircode.email.ApplicationEx.attachBaseContext (ApplicationEx.java:83)
                  at android.app.Application.attach (Application.java:368)
                  at android.app.Instrumentation.newApplication (Instrumentation.java:1356)
                  at android.app.LoadedApk.makeApplicationInner (LoadedApk.java:1541)
             */
        }

        return context;
    }

    static Context getThemedContext(Context context, int style) {
        synchronized (themeCache) {
            Context tcontext = themeCache.get(style);
            if (tcontext == null) {
                tcontext = new ContextThemeWrapper(context.getApplicationContext(), style);
                themeCache.put(style, tcontext);
            }
            return tcontext;
        }
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean crash_reports = prefs.getBoolean("crash_reports", false);
        final boolean leak_canary = prefs.getBoolean("leak_canary", BuildConfig.TEST_RELEASE);
        final boolean load_emoji = prefs.getBoolean("load_emoji", false);

        prev = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
                if (!crash_reports && Log.isOwnFault(ex)) {
                    Log.e(ex);

                    if (BuildConfig.BETA_RELEASE ||
                            !Helper.isPlayStoreInstall())
                        DebugHelper.writeCrashLog(ApplicationEx.this, ex);

                    if (prev != null)
                        prev.uncaughtException(thread, ex);
                } else {
                    Log.w(ex);
                    System.exit(1);
                }
            }
        });

        ConnectionHelper.setupProxy(this);

        if (BuildConfig.DEBUG)
            UriHelper.test(this);

        CoalMine.install(this);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void onStart() {
                log(true);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onStop() {
                log(false);
            }

            private void log(boolean foreground) {
                Log.i("App foreground=" + foreground);
                Log.breadcrumb("app", "foreground", Boolean.toString(foreground));
            }
        });

        registerActivityLifecycleCallbacks(lifecycleCallbacks);

        if (BuildConfig.DEBUG)
            getMainLooper().setMessageLogging(new Printer() {
                @Override
                public void println(String msg) {
                    Log.d("Loop: " + msg);
                }
            });

        if (BuildConfig.DEBUG &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && false) {
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

        Log.setup(this);
        CoalMine.setup(leak_canary);

        upgrade(this);

        try {
            boolean tcp_keep_alive = prefs.getBoolean("tcp_keep_alive", false);
            if (tcp_keep_alive)
                System.setProperty("fairemail.tcp_keep_alive", Boolean.toString(tcp_keep_alive));
            else
                System.clearProperty("fairemail.tcp_keep_alive");
        } catch (Throwable ex) {
            Log.e(ex);
        }

        prefs.registerOnSharedPreferenceChangeListener(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            NotificationHelper.createNotificationChannels(this);

        DB.setupViewInvalidation(this);

        // https://issuetracker.google.com/issues/341313071
        // https://developer.android.com/guide/navigation/custom-back/support-animations#fragments
        // https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture#opt-predictive
        FragmentManager.enablePredictiveBack(false);

        // https://issuetracker.google.com/issues/233525229
        Log.i("Load emoji=" + load_emoji);
        try {
            FontRequestEmojiCompatConfig crying = DefaultEmojiCompatConfig.create(this);
            if (crying != null) {
                crying.setMetadataLoadStrategy(EmojiCompat.LOAD_STRATEGY_MANUAL);
                EmojiCompat.init(crying);
                if (load_emoji)
                    EmojiCompat.get().load();
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        EmailProvider.init(this);
        EncryptionHelper.init(this);
        MessageHelper.setSystemProperties(this);

        ContactInfo.init(this);

        DnsHelper.init(this);
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

                WorkerAutoUpdate.init(this);
                WorkerCleanup.init(this);
                WorkerDailyRules.init(this);
                WorkerSync.init(this);
            } catch (Throwable ex) {
                Log.e(ex);

                // Exception java.lang.RuntimeException:
                //  at android.app.ActivityThread.handleBindApplication (ActivityThread.java:6320)
                //  at android.app.ActivityThread.access$1800 (ActivityThread.java:221)
                //  at android.app.ActivityThread$H.handleMessage (ActivityThread.java:1860)
                //  at android.os.Handler.dispatchMessage (Handler.java:102)
                //  at android.os.Looper.loop (Looper.java:158)
                //  at android.app.ActivityThread.main (ActivityThread.java:7225)
                //  at java.lang.reflect.Method.invoke
                //  at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run (ZygoteInit.java:1230)
                //  at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:1120)
                // Caused by java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.Object.hashCode()' on a null object reference
                //  at java.util.Collections.secondaryHash (Collections.java:3427)
                //  at java.util.HashMap.put (HashMap.java:385)
                //  at androidx.work.impl.WorkDatabase_Impl.getRequiredTypeConverters (WorkDatabase_Impl.java:312)
                //  at androidx.room.RoomDatabase.init (RoomDatabase.java:272)
                //  at androidx.room.RoomDatabase$Builder.build (RoomDatabase.java:1487)
                //  at androidx.work.impl.WorkDatabase$Companion.create (WorkDatabase.kt:159)
                //  at androidx.work.impl.WorkDatabase.create (WorkDatabase.kt)
                //  at androidx.work.impl.WorkManagerImpl.<init> (WorkManagerImpl.java:259)
                //  at androidx.work.impl.WorkManagerImpl.<init> (WorkManagerImpl.java:234)
                //  at androidx.work.impl.WorkManagerImpl.initialize (WorkManagerImpl.java:213)
                //  at androidx.work.impl.WorkManagerImpl.getInstance (WorkManagerImpl.java:168)
                //  at androidx.work.WorkManager.getInstance (WorkManager.java:184)
                //  at eu.faircode.email.ApplicationEx.onCreate (ApplicationEx.java:278)
                //  at android.app.Instrumentation.callApplicationOnCreate (Instrumentation.java:1036)
                //  at android.app.ActivityThread.handleBindApplication (ActivityThread.java:6317)
            }
        }

        try {
            ContextCompat.registerReceiver(this,
                    onScreenOff,
                    new IntentFilter(Intent.ACTION_SCREEN_OFF),
                    ContextCompat.RECEIVER_NOT_EXPORTED);
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                Exception java.lang.RuntimeException:
                  at android.app.ActivityThread.handleBindApplication (ActivityThread.java:7690)
                  at android.app.ActivityThread.-$$Nest$mhandleBindApplication
                  at android.app.ActivityThread$H.handleMessage (ActivityThread.java:2478)
                  at android.os.Handler.dispatchMessage (Handler.java:106)
                  at android.os.Looper.loopOnce (Looper.java:230)
                  at android.os.Looper.loop (Looper.java:319)
                  at android.app.ActivityThread.main (ActivityThread.java:8893)
                  at java.lang.reflect.Method.invoke
                  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run (RuntimeInit.java:608)
                  at com.android.internal.os.ChildZygoteInit.runZygoteServer (ChildZygoteInit.java:136)
                  at com.android.internal.os.WebViewZygoteInit.main (WebViewZygoteInit.java:147)
                  at java.lang.reflect.Method.invoke
                  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run (RuntimeInit.java:608)
                  at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:1103)
                Caused by java.lang.SecurityException: Isolated process not allowed to call registerReceiver
                  at android.os.Parcel.createExceptionOrNull (Parcel.java:3069)
                  at android.os.Parcel.createException (Parcel.java:3053)
                  at android.os.Parcel.readException (Parcel.java:3036)
                  at android.os.Parcel.readException (Parcel.java:2978)
                  at android.app.IActivityManager$Stub$Proxy.registerReceiverWithFeature (IActivityManager.java:6137)
                  at android.app.ContextImpl.registerReceiverInternal (ContextImpl.java:1913)
                  at android.app.ContextImpl.registerReceiver (ContextImpl.java:1860)
                  at android.content.ContextWrapper.registerReceiver (ContextWrapper.java:791)
                  at androidx.core.content.ContextCompat$Api33Impl.registerReceiver (ContextCompat.java:1239)
                  at androidx.core.content.ContextCompat.registerReceiver (ContextCompat.java:870)
                  at androidx.core.content.ContextCompat.registerReceiver (ContextCompat.java:821)
                  at eu.faircode.email.ApplicationEx.onCreate (ApplicationEx.java:316)
                  at android.app.Instrumentation.callApplicationOnCreate (Instrumentation.java:1316)
                  at android.app.ActivityThread.handleBindApplication (ActivityThread.java:7685)
             */
        }

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
                case "schedule_start_weekend":
                case "schedule_end_weekend":
                case "weekend":
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
                case "debug":
                case "log_level":
                    Log.setLevel(this);
                    FairEmailLoggingProvider.setLevel(this);
                    break;
                default:
                    if (FragmentOptionsBackup.RESTART_OPTIONS.contains(key))
                        restart(this, key);
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
        try {
            /*
                java.lang.NoClassDefFoundError: Not a primitive type: '\u0000'
                    at androidx.core.content.ContextCompat$Api23Impl.getSystemService(Unknown Source:0)
                    at androidx.core.content.ContextCompat.getSystemService(SourceFile:7)
                    at eu.faircode.email.Helper.getSystemService(Unknown Source:4)
                    at eu.faircode.email.Log.logMemory(SourceFile:8)
                    at eu.faircode.email.ApplicationEx.onTrimMemory(SourceFile:18)
                    at android.app.ActivityThread.handleTrimMemory(ActivityThread.java:5453)
             */
            Log.logMemory(this, "Trim memory level=" + level);
            Map<String, String> crumb = new HashMap<>();
            crumb.put("level", Integer.toString(level));
            Log.breadcrumb("trim", crumb);
            super.onTrimMemory(level);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onLowMemory() {
        try {
            Log.logMemory(this, "Low memory");
            Map<String, String> crumb = new HashMap<>();
            crumb.put("free", Integer.toString(Log.getFreeMemMb()));
            Log.breadcrumb("low", crumb);

            ContactInfo.clearCache(this, false);

            super.onLowMemory();
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static void upgrade(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int version = prefs.getInt("version", BuildConfig.VERSION_CODE);
        if (version != BuildConfig.VERSION_CODE)
            EntityLog.log(context, "Upgrading from " + version + " to " + BuildConfig.VERSION_CODE);

        SharedPreferences.Editor editor = prefs.edit();

        if (version < BuildConfig.VERSION_CODE)
            editor.remove("crash_report_count");

        if (!Log.isTestRelease())
            editor.remove("test1").remove("test2").remove("test3").remove("test4").remove("test5");

        if (version < 468) {
            editor.remove("notify_trash");
            editor.remove("notify_archive");
            editor.remove("notify_reply");
            editor.remove("notify_flag");
            editor.remove("notify_seen");
        }

        if (version < 601) {
            editor.putBoolean("contact_images", prefs.getBoolean("autoimages", true));
            editor.remove("autoimages");
        }

        if (version < 612) {
            if (prefs.getBoolean("autonext", false))
                editor.putString("onclose", "next");
            editor.remove("autonext");
        }

        if (version < 693) {
            editor.remove("message_swipe");
            editor.remove("message_select");
        }

        if (version < 696) {
            String theme = prefs.getString("theme", "light");
            if ("grey".equals(theme))
                editor.putString("theme", "grey_dark");

            if (prefs.contains("ascending")) {
                editor.putBoolean("ascending_list", prefs.getBoolean("ascending", false));
                editor.remove("ascending");
            }
        }

        if (version < 701) {
            if (prefs.getBoolean("suggest_local", false)) {
                editor.putBoolean("suggest_sent", true);
                editor.remove("suggest_local");
            }
        }

        if (version < 703) {
            if (!prefs.getBoolean("style_toolbar", true)) {
                editor.putBoolean("compose_media", false);
                editor.remove("style_toolbar");
            }
        }

        if (version < 709) {
            if (prefs.getBoolean("swipe_reversed", false)) {
                editor.putBoolean("reversed", true);
                editor.remove("swipe_reversed");
            }
        }

        if (version < 741)
            editor.remove("send_dialog");

        if (version < 751) {
            if (prefs.contains("notify_snooze_duration")) {
                int minutes = prefs.getInt("notify_snooze_duration", 60);
                int hours = (int) Math.ceil(minutes / 60.0);
                editor.putInt("default_snooze", hours);
                editor.remove("notify_snooze_duration");
            }
        }

        if (version < 819) {
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
        }

        if (version < 844) {
            if (prefs.getBoolean("schedule", false))
                editor.putBoolean("enabled", true);
        }

        if (version < 874) {
            if (prefs.contains("experiments") &&
                    prefs.getBoolean("experiments", false))
                editor.putBoolean("quick_filter", true);
            editor.remove("experiments");
        }

        if (version < 889) {
            if (prefs.contains("autoresize")) {
                boolean autoresize = prefs.getBoolean("autoresize", true);
                editor.putBoolean("resize_images", autoresize);
                editor.putBoolean("resize_attachments", autoresize);
                editor.remove("autoresize");
            }
        }

        if (version < 930) {
            boolean large = context.getResources().getConfiguration()
                    .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
            editor.putBoolean("landscape3", large);
        }

        if (version < 949) {
            if (prefs.contains("automove")) {
                boolean automove = prefs.getBoolean("automove", false);
                editor.putBoolean("move_1_confirmed", automove);
                editor.remove("automove");
            }
        }

        if (version < 972) {
            if (prefs.contains("signature_end")) {
                boolean signature_end = prefs.getBoolean("signature_end", false);
                if (signature_end)
                    editor.putInt("signature_location", 2);
                editor.remove("signature_end");
            }
        }

        if (version < 978) {
            if (!prefs.contains("poll_interval"))
                editor.putInt("poll_interval", 0);
            editor.remove("first");
        }

        if (version < 1021) {
            boolean highlight_unread = prefs.getBoolean("highlight_unread", false);
            if (!highlight_unread)
                editor.putBoolean("highlight_unread", highlight_unread);
        }

        if (version < 1121) {
            if (!Helper.isPlayStoreInstall())
                editor.putBoolean("experiments", true);
        }

        if (version < 1124)
            editor.remove("experiments");

        if (version < 1181) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                editor.remove("background_service");
        }

        if (version < 1195)
            editor.remove("auto_optimize");

        if (version < 1229) {
            boolean monospaced = prefs.getBoolean("monospaced", false);
            if (monospaced && !BuildConfig.DEBUG)
                editor.putBoolean("text_font", false);
        }

        if (version < 1238) {
            if (!prefs.contains("subject_ellipsize"))
                editor.putString("subject_ellipsize", "middle");
            if (!prefs.contains("auto_optimize"))
                editor.putBoolean("auto_optimize", false);
        }

        if (version < 1253) {
            int threads = prefs.getInt("query_threads", 4);
            if (threads == 4)
                editor.remove("query_threads");
        }

        if (version < 1264) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N ||
                    "Blackview".equalsIgnoreCase(Build.MANUFACTURER) ||
                    "OnePlus".equalsIgnoreCase(Build.MANUFACTURER) ||
                    "HUAWEI".equalsIgnoreCase(Build.MANUFACTURER))
                editor.putInt("query_threads", 2);
        }

        if (version < 1274)
            ContactInfo.clearCache(context); // Favicon background

        if (version < 1336) {
            if (!prefs.contains("beige"))
                editor.putBoolean("beige", false);
        }

        if (version < 1385)
            editor.remove("parse_classes");

        if (version < 1401)
            editor.remove("tcp_keep_alive");

        if (version < 1407)
            editor.remove("print_html_confirmed");

        if (version < 1413)
            editor.remove("experiments");

        if (version < 1439) {
            if (!BuildConfig.DEBUG)
                editor.remove("experiments");
        }

        if (version < 1461) {
            if (!prefs.contains("theme"))
                editor.putString("theme", "blue_orange_light");
        }

        if (version < 1463) {
            if (!prefs.contains("autoscroll"))
                editor.putBoolean("autoscroll", true);
        }

        if (version < 1477) {
            if (!BuildConfig.DEBUG)
                editor.remove("experiments");
        }

        if (version < 1524) {
            if (BuildConfig.PLAY_STORE_RELEASE)
                editor.remove("experiments");
        }

        if (version < 1525) {
            if (!prefs.contains("download"))
                editor.putInt("download", 512 * 1024);
        }

        if (version < 1533) {
            if (!prefs.contains("biometrics_notify"))
                editor.putBoolean("biometrics_notify", false);
        }

        if (version < 1535) {
            editor.remove("identities_asked");
            editor.remove("identities_primary_hint");
        }

        if (version < 1539) {
            if (!prefs.contains("double_back"))
                editor.putBoolean("double_back", true);
        }

        if (version < 1540) {
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
        }

        if (version < 1556) {
            if (prefs.contains("last_search")) {
                editor.putString("last_search1", prefs.getString("last_search", null));
                editor.remove("last_search");
            }
        }

        if (version < 1558) {
            if (!prefs.contains("button_extra"))
                editor.putBoolean("button_extra", true);
        }

        if (version < 1598) {
            if (prefs.contains("deepl")) {
                String key = prefs.getString("deepl", null);
                editor.putString("deepl_key", key).remove("deepl");
            }
        }

        if (version < 1630) {
            boolean experiments = prefs.getBoolean("experiments", false);
            if (experiments)
                editor.putBoolean("deepl_enabled", true);
        }

        if (version < 1678) {
            Configuration config = context.getResources().getConfiguration();
            boolean normal = config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL);
            if (!normal) {
                if (!prefs.contains("landscape"))
                    editor.putBoolean("landscape", false);
                if (!prefs.contains("landscape3"))
                    editor.putBoolean("landscape3", false);
            }
        }

        if (version < 1721) {
            if (!prefs.contains("discard_delete"))
                editor.putBoolean("discard_delete", false);
        }

        if (version < 1753)
            repairFolders(context);

        if (version < 1772)
            editor.remove("conversation_actions");

        if (version < 1781) {
            if (prefs.contains("sort")) {
                String sort = prefs.getString("sort", "time");
                editor.putString("sort_unified", sort);
            }
            if (prefs.contains("ascending_list")) {
                boolean ascending = prefs.getBoolean("ascending_list", false);
                editor.putBoolean("ascending_unified", ascending);
            }
        }

        if (version < 1835) {
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
        }

        if (version < 1837) {
            if (!prefs.contains("compact_folders"))
                editor.putBoolean("compact_folders", false);
        }

        if (version < 1839) {
            boolean reply_all = prefs.getBoolean("reply_all", false);
            if (reply_all)
                editor.remove("reply_all").putString("answer_action", "reply_all");
        }

        if (version < 1847) {
            if (Helper.isAccessibilityEnabled(context))
                editor.putBoolean("send_chips", false);
        }

        if (version < 1855) {
            if (!prefs.contains("preview_lines"))
                editor.putInt("preview_lines", 2);
        }

        if (version < 1874) {
            boolean cards = prefs.getBoolean("cards", true);
            if (!cards)
                editor.remove("view_padding");
        }

        if (version < 1888) {
            int class_min_difference = prefs.getInt("class_min_difference", 50);
            if (class_min_difference == 0)
                editor.putBoolean("classification", false);
        }

        if (version < 1918) {
            if (prefs.contains("browse_links")) {
                boolean browse_links = prefs.getBoolean("browse_links", false);
                editor.remove("browse_links")
                        .putBoolean("open_with_tabs", !browse_links);
            }
        }

        if (version < 1927) {
            if (!prefs.contains("auto_identity"))
                editor.putBoolean("auto_identity", true);
        }

        if (version < 1931)
            editor.remove("button_force_light").remove("fake_dark");

        if (version < 1933) {
            editor.putBoolean("lt_enabled", false);
            if (prefs.contains("disable_top")) {
                editor.putBoolean("use_top", !prefs.getBoolean("disable_top", false));
                editor.remove("disable_top");
            }
        }

        if (version < 1947)
            editor.putBoolean("accept_unsupported", true);

        if (version < 1951) {
            if (prefs.contains("open_unsafe"))
                editor.putBoolean("open_safe", !prefs.getBoolean("open_unsafe", true));
        }

        if (version < 1955) {
            if (!prefs.contains("doubletap"))
                editor.putBoolean("doubletap", true);
        }

        if (version < 1960)
            editor.remove("sqlite_auto_vacuum");

        if (version < 1961) {
            if (!prefs.contains("photo_picker"))
                editor.putBoolean("photo_picker", true);
        }

        if (version < 1966)
            editor.remove("hide_timezone");

        if (version < 1994) {
            // 2022-10-28 Spamcop blocks Google's addresses
            editor.putBoolean("blocklist.Spamcop", false);
        }

        if (version < 2013) {
            if (prefs.contains("compose_block")) {
                if (prefs.getBoolean("experiments", false))
                    editor.putBoolean("compose_style", prefs.getBoolean("compose_block", false));
                editor.remove("compose_block");
            }
        }

        if (version < 2016) {
            if (!prefs.contains("reset_snooze"))
                editor.putBoolean("reset_snooze", false);
        }

        if (version < 2029) {
            if (!prefs.contains("plain_only_reply"))
                editor.putBoolean("plain_only_reply", true);
        }

        if (version < 2046)
            editor.remove("message_junk");

        if (version < 2069) {
            if (prefs.contains("swipe_sensitivity") && !prefs.contains("swipe_sensitivity_updated")) {
                int swipe_sensitivity = prefs.getInt("swipe_sensitivity", FragmentOptionsBehavior.DEFAULT_SWIPE_SENSITIVITY);
                if (swipe_sensitivity > 0) {
                    swipe_sensitivity--;
                    if (swipe_sensitivity == FragmentOptionsBehavior.DEFAULT_SWIPE_SENSITIVITY)
                        editor.remove("swipe_sensitivity");
                    else
                        editor.putInt("swipe_sensitivity", swipe_sensitivity - 1)
                                .putBoolean("swipe_sensitivity_updated", true);
                }
            }
        }

        if (version < 2075) {
            for (String name : new String[]{"seen", "unflagged", "unknown", "snoozed", "deleted"})
                if (prefs.contains("filter_" + name))
                    for (String _type : new String[]{EntityFolder.ARCHIVE, EntityFolder.TRASH, EntityFolder.JUNK}) {
                        String type = _type.toLowerCase(Locale.ROOT);
                        if (!prefs.contains("filter_" + type + "_" + name))
                            editor.putBoolean("filter_" + type + "_" + name, prefs.getBoolean("filter_" + name, false));
                    }
        }

        if (version < 2084) {
            boolean thread_sent_trash = prefs.getBoolean("thread_sent_trash", false);
            if (thread_sent_trash)
                editor.putBoolean("move_thread_sent", true);
            editor.remove("thread_sent_trash");
        }

        if (version < 2086) {
            boolean override_width = prefs.getBoolean("override_width", false);
            if (override_width)
                editor.putBoolean("overview_mode", true);
            editor.remove("override_width");
        }

        if (version < 2089) {
            if (!prefs.contains("auto_hide_answer"))
                editor.putBoolean("auto_hide_answer", !Helper.isAccessibilityEnabled(context));
        }

        if (version < 2108) {
            if (!prefs.getBoolean("updown", false))
                editor.putBoolean("updown", false);
        }

        if (version < 2113)
            editor.remove("send_more");

        if (version < 2137) {
            // https://support.google.com/faqs/answer/6346016
            if (!prefs.contains("cert_strict"))
                editor.putBoolean("cert_strict", !BuildConfig.PLAY_STORE_RELEASE);
        }

        if (version < 2162) {
            if (!BuildConfig.DEBUG)
                editor.putBoolean("tabular_unread_bg", false);
        }

        if (version < 2168) {
            if (Helper.isGoogle())
                editor.putBoolean("mod", true);
        }

        if (version < 2170)
            editor.putBoolean("mod", false);

        if (version < 2180) {
            if (Helper.isAndroid15())
                editor.putInt("last_sdk", 0);
        }

        if (version < 2187) {
            if (!prefs.contains("delete_unseen"))
                editor.putBoolean("delete_unseen", false);
            if (Helper.isPixelBeta())
                editor.putBoolean("motd", true);
        }

        if (version < 2191) {
            if ("a".equals(BuildConfig.REVISION))
                editor.remove("show_changelog");
        }

        if (version < 2196) {
            if (!prefs.contains("forward_new"))
                editor.putBoolean("forward_new", true);
        }

        if (version < 2206) {
            if (prefs.getInt("viewport_height", 0) == 16000 &&
                    (Helper.isGoogle() || Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU))
                editor.remove("viewport_height");
        }

        if (version < 2208) {
            if (!BuildConfig.DEBUG)
                ContactInfo.clearCache(context); // SVG scale
        }

        if (version < 2210) {
            if (!BuildConfig.DEBUG)
                editor.remove("outlook_last_checked").remove("outlook_checked");
        }

        if (version < 2212) {
            if (!BuildConfig.DEBUG)
                ContactInfo.clearCache(context); // SVG scale
        }

        if (version < 2218) {
            if (prefs.contains("color_stripe")) {
                boolean color_stripe = prefs.getBoolean("color_stripe", true);
                editor
                        .putInt("account_color", color_stripe ? 1 : 0)
                        .remove("color_stripe");
            }
        }

        if (version < 2243 && "a".equals(BuildConfig.REVISION)) {
            boolean beige = prefs.getBoolean("beige", true);
            String theme = prefs.getString("theme", "blue_orange_system");
            boolean you = theme.startsWith("you_");
            if (you && beige)
                editor.putBoolean("beige", false);
        }

        if (version < 2259)
            editor.putBoolean("thread_byref", true);

        if (version < 2271) {
            boolean color_stripe_wide = prefs.getBoolean("color_stripe_wide", false);
            if (color_stripe_wide)
                editor.putInt("account_color_size", 12);
            editor.remove("color_stripe_wide");
        }

        if (version < 2277) {
            if (!prefs.contains("restore_on_launch"))
                editor.putBoolean("restore_on_launch", false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !BuildConfig.DEBUG)
            editor.remove("background_service");

        if (version < BuildConfig.VERSION_CODE)
            editor.putInt("previous_version", version);
        editor.putInt("version", BuildConfig.VERSION_CODE);

        int last_sdk = prefs.getInt("last_sdk", Build.VERSION.SDK_INT);
        if (Helper.isAndroid15() && last_sdk <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            editor.remove("setup_reminder");
        editor.putInt("last_sdk", Build.VERSION.SDK_INT);

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
            if (activity instanceof ActivityView ||
                    (BuildConfig.DEBUG && activity instanceof ActivityCompose))
                ServiceSynchronize.state(activity, true);
        }

        @Override
        public void onActivityPrePaused(@NonNull Activity activity) {
            log(activity, "onActivityPrePaused");
            if (activity instanceof ActivityView ||
                    (BuildConfig.DEBUG && activity instanceof ActivityCompose))
                ServiceSynchronize.state(activity, false);
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
            synchronized (lock) {
                handler = new Handler(Looper.getMainLooper());
            }
        return handler;
    }
}
