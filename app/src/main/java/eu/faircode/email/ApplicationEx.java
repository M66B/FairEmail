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

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Printer;
import android.webkit.CookieManager;

import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ApplicationEx extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Thread.UncaughtExceptionHandler prev = null;

    private static final List<String> OPTIONS_RESTART = Collections.unmodifiableList(Arrays.asList(
            "secure", // privacy
            "shortcuts", // misc
            "language", // misc
            "query_threads" // misc
    ));

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(getLocalizedContext(base));
    }

    static Context getLocalizedContext(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.contains("english")) {
            boolean english = prefs.getBoolean("english", false);
            if (english)
                prefs.edit()
                        .remove("english")
                        .putString("language", Locale.US.toLanguageTag())
                        .commit();
        }

        String language = prefs.getString("language", null);
        if (language != null) {
            Locale locale = Locale.forLanguageTag(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        }

        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        long start = new Date().getTime();
        Log.logMemory(this, "App create version=" + BuildConfig.VERSION_NAME);

        getMainLooper().setMessageLogging(new Printer() {
            @Override
            public void println(String msg) {
                if (BuildConfig.DEBUG)
                    Log.d("Loop: " + msg);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean crash_reports = prefs.getBoolean("crash_reports", false);
        prefs.registerOnSharedPreferenceChangeListener(this);

        prev = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
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

        upgrade(this);

        createNotificationChannels();

        DB.setupViewInvalidation(this);

        if (Helper.hasWebView(this))
            CookieManager.getInstance().setAcceptCookie(false);

        MessageHelper.setSystemProperties(this);
        ContactInfo.init(this);

        DisconnectBlacklist.init(this);

        WorkerWatchdog.init(this);
        WorkerCleanup.queue(this);

        registerReceiver(onScreenOff, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        if (BuildConfig.DEBUG)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DnsHelper.test(ApplicationEx.this);
                }
            }).start();

        long end = new Date().getTime();
        Log.i("App created " + (end - start) + " ms");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (OPTIONS_RESTART.contains(key))
            restart();
    }

    void restart() {
        Intent intent = new Intent(this, ActivityMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    @Override
    public void onTrimMemory(int level) {
        Log.logMemory(this, "Trim memory level=" + level);
        Map<String, String> crumb = new HashMap<>();
        crumb.put("level", Integer.toString(level));
        crumb.put("free", Integer.toString(Log.getFreeMemMb()));
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !BuildConfig.DEBUG)
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
        }

        if (version < BuildConfig.VERSION_CODE)
            editor.putInt("previous_version", version);
        editor.putInt("version", BuildConfig.VERSION_CODE);

        editor.apply();
    }

    private void createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // https://issuetracker.google.com/issues/65108694
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Sync
            NotificationChannel service = new NotificationChannel(
                    "service", getString(R.string.channel_service),
                    NotificationManager.IMPORTANCE_MIN);
            service.setDescription(getString(R.string.channel_service_description));
            service.setSound(null, null);
            service.enableVibration(false);
            service.enableLights(false);
            service.setShowBadge(false);
            service.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            nm.createNotificationChannel(service);

            // Send
            NotificationChannel send = new NotificationChannel(
                    "send", getString(R.string.channel_send),
                    NotificationManager.IMPORTANCE_DEFAULT);
            send.setDescription(getString(R.string.channel_send_description));
            send.setSound(null, null);
            send.enableVibration(false);
            send.enableLights(false);
            send.setShowBadge(false);
            send.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(send);

            // Notify
            NotificationChannel notification = new NotificationChannel(
                    "notification", getString(R.string.channel_notification),
                    NotificationManager.IMPORTANCE_HIGH);
            notification.setDescription(getString(R.string.channel_notification_description));
            notification.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notification.enableLights(true);
            notification.setLightColor(Color.YELLOW);
            nm.createNotificationChannel(notification);

            // Update
            if (!Helper.isPlayStoreInstall()) {
                NotificationChannel update = new NotificationChannel(
                        "update", getString(R.string.channel_update),
                        NotificationManager.IMPORTANCE_HIGH);
                update.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                update.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                nm.createNotificationChannel(update);
            }

            // Warnings
            NotificationChannel warning = new NotificationChannel(
                    "warning", getString(R.string.channel_warning),
                    NotificationManager.IMPORTANCE_HIGH);
            warning.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(warning);

            // Errors
            NotificationChannel error = new NotificationChannel(
                    "error",
                    getString(R.string.channel_error),
                    NotificationManager.IMPORTANCE_HIGH);
            error.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(error);

            // Server alerts
            NotificationChannel alerts = new NotificationChannel(
                    "alerts",
                    getString(R.string.channel_alert),
                    NotificationManager.IMPORTANCE_HIGH);
            alerts.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(alerts);

            // Contacts grouping
            NotificationChannelGroup group = new NotificationChannelGroup(
                    "contacts",
                    getString(R.string.channel_group_contacts));
            nm.createNotificationChannelGroup(group);
        }
    }

    private BroadcastReceiver onScreenOff = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Received " + intent);
            Log.logExtras(intent);
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
