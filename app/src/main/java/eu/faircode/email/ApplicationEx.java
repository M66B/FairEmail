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

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.DeadSystemException;
import android.os.RemoteException;
import android.view.OrientationEventListener;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.bugsnag.android.BeforeNotify;
import com.bugsnag.android.BeforeSend;
import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Client;
import com.bugsnag.android.Error;
import com.bugsnag.android.Report;
import com.sun.mail.iap.ProtocolException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.mail.MessagingException;

public class ApplicationEx extends Application {
    private Thread.UncaughtExceptionHandler prev = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(getLocalizedContext(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        logMemory("App create version=" + BuildConfig.VERSION_NAME);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean crash_reports = prefs.getBoolean("crash_reports", false);

        prev = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                if (!crash_reports && ownFault(ex)) {
                    Log.e(ex);

                    if (BuildConfig.BETA_RELEASE ||
                            !Helper.isPlayStoreInstall(ApplicationEx.this))
                        writeCrashLog(ApplicationEx.this, ex);

                    if (prev != null)
                        prev.uncaughtException(thread, ex);
                } else {
                    Log.w(ex);
                    System.exit(1);
                }
            }
        });

        setupBugsnag();

        upgrade(this);

        createNotificationChannels();

        if (Helper.hasWebView(this))
            CookieManager.getInstance().setAcceptCookie(false);

        MessageHelper.setSystemProperties(this);
        ContactInfo.init(this);

        WorkerWatchdog.init(this);
        WorkerCleanup.queue(this);
    }

    @Override
    public void onTrimMemory(int level) {
        logMemory("Trim memory level=" + level);
        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        logMemory("Low memory");
        super.onLowMemory();
    }

    private void logMemory(String message) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        int mb = Math.round(mi.availMem / 0x100000L);
        int perc = Math.round(mi.availMem / (float) mi.totalMem * 100.0f);
        Log.i(message + " " + mb + " MB" + " " + perc + " %");
    }

    private void setupBugsnag() {
        // https://docs.bugsnag.com/platforms/android/sdk/
        com.bugsnag.android.Configuration config =
                new com.bugsnag.android.Configuration("9d2d57476a0614974449a3ec33f2604a");

        if (BuildConfig.DEBUG)
            config.setReleaseStage("debug");
        else {
            String type = "other";
            if (Helper.hasValidFingerprint(this))
                if (BuildConfig.PLAY_STORE_RELEASE)
                    type = "play";
                else
                    type = "full";
            config.setReleaseStage(type + (BuildConfig.BETA_RELEASE ? "/beta" : ""));
        }

        config.setAutoCaptureSessions(false);

        config.setDetectAnrs(false);
        config.setDetectNdkCrashes(false);

        List<String> ignore = new ArrayList<>();

        ignore.add("com.sun.mail.util.MailConnectException");

        ignore.add("android.accounts.OperationCanceledException");
        ignore.add("android.app.RemoteServiceException");

        ignore.add("java.lang.NoClassDefFoundError");
        ignore.add("java.lang.UnsatisfiedLinkError");

        ignore.add("java.nio.charset.MalformedInputException");

        ignore.add("java.net.ConnectException");
        ignore.add("java.net.SocketException");
        ignore.add("java.net.SocketTimeoutException");
        ignore.add("java.net.UnknownHostException");

        ignore.add("javax.mail.AuthenticationFailedException");
        ignore.add("javax.mail.FolderClosedException");
        ignore.add("javax.mail.internet.AddressException");
        ignore.add("javax.mail.MessageRemovedException");
        ignore.add("javax.mail.ReadOnlyFolderException");
        ignore.add("javax.mail.StoreClosedException");

        ignore.add("org.xmlpull.v1.XmlPullParserException");

        config.setIgnoreClasses(ignore.toArray(new String[0]));

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        config.beforeSend(new BeforeSend() {
            @Override
            public boolean run(@NonNull Report report) {
                Error error = report.getError();
                if (error != null) {
                    Throwable ex = error.getException();

                    if (ex instanceof MessagingException &&
                            (ex.getCause() instanceof IOException ||
                                    ex.getCause() instanceof ProtocolException))
                        // IOException includes SocketException, SocketTimeoutException
                        // ProtocolException includes ConnectionException
                        return false;

                    if (ex instanceof MessagingException &&
                            ("connection failure".equals(ex.getMessage()) ||
                                    "failed to create new store connection".equals(ex.getMessage()) ||
                                    "Failed to fetch headers".equals(ex.getMessage()) ||
                                    "Failed to load IMAP envelope".equals(ex.getMessage()) ||
                                    "Unable to load BODYSTRUCTURE".equals(ex.getMessage())))
                        return false;

                    if (ex instanceof IllegalStateException &&
                            ("Not connected".equals(ex.getMessage()) ||
                                    "This operation is not allowed on a closed folder".equals(ex.getMessage())))
                        return false;

                    if (ex instanceof FileNotFoundException &&
                            ex.getMessage() != null &&
                            (ex.getMessage().startsWith("Download image failed") ||
                                    ex.getMessage().startsWith("https://ipinfo.io/") ||
                                    ex.getMessage().startsWith("https://autoconfig.thunderbird.net/")))
                        return false;
                }

                return prefs.getBoolean("crash_reports", false); // opt-in
            }
        });

        Bugsnag.init(this, config);

        Client client = Bugsnag.getClient();

        try {
            Log.i("Disabling orientation listener");
            Field fOrientationListener = Client.class.getDeclaredField("orientationListener");
            fOrientationListener.setAccessible(true);
            OrientationEventListener orientationListener = (OrientationEventListener) fOrientationListener.get(client);
            orientationListener.disable();
            Log.i("Disabled orientation listener");
        } catch (Throwable ex) {
            Log.e(ex);
        }

        String uuid = prefs.getString("uuid", null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            prefs.edit().putString("uuid", uuid).apply();
        }
        Log.i("uuid=" + uuid);
        client.setUserId(uuid);

        if (prefs.getBoolean("crash_reports", false))
            Bugsnag.startSession();

        final String installer = getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID);
        final boolean fingerprint = Helper.hasValidFingerprint(this);

        Bugsnag.beforeNotify(new BeforeNotify() {
            @Override
            public boolean run(@NonNull Error error) {
                error.addToTab("extra", "installer", installer == null ? "-" : installer);
                error.addToTab("extra", "fingerprint", fingerprint);
                error.addToTab("extra", "free", Log.getFreeMemMb());
                return true;
            }
        });
    }

    static void upgrade(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int version = prefs.getInt("version", BuildConfig.VERSION_CODE);
        Log.i("Upgrading from " + version + " to " + BuildConfig.VERSION_CODE);

        SharedPreferences.Editor editor = prefs.edit();

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
        }


        if (BuildConfig.DEBUG && false) {
            editor.remove("app_support");
            editor.remove("notify_archive");
            editor.remove("message_swipe");
            editor.remove("message_select");
            editor.remove("folder_actions");
            editor.remove("folder_sync");
        }

        editor.putInt("version", BuildConfig.VERSION_CODE);

        editor.apply();
    }

    static Context getLocalizedContext(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean english = prefs.getBoolean("english", false);

        if (english) {
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(Locale.US);
            return context.createConfigurationContext(config);
        } else
            return context;
    }

    private void createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Sync
            NotificationChannel service = new NotificationChannel(
                    "service", getString(R.string.channel_service),
                    NotificationManager.IMPORTANCE_MIN);
            service.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            service.setShowBadge(false);
            service.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            nm.createNotificationChannel(service);

            // Send
            NotificationChannel send = new NotificationChannel(
                    "send", getString(R.string.channel_send),
                    NotificationManager.IMPORTANCE_DEFAULT);
            send.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            send.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(send);

            // Notify
            NotificationChannel notification = new NotificationChannel(
                    "notification", getString(R.string.channel_notification),
                    NotificationManager.IMPORTANCE_HIGH);
            notification.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notification.enableLights(true);
            nm.createNotificationChannel(notification);

            // Update
            if (!Helper.isPlayStoreInstall(this)) {
                NotificationChannel update = new NotificationChannel(
                        "update", getString(R.string.channel_update),
                        NotificationManager.IMPORTANCE_HIGH);
                update.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                update.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                nm.createNotificationChannel(update);
            }

            // Warn
            NotificationChannel warning = new NotificationChannel(
                    "warning", getString(R.string.channel_warning),
                    NotificationManager.IMPORTANCE_HIGH);
            warning.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(warning);

            // Error
            NotificationChannel error = new NotificationChannel(
                    "error",
                    getString(R.string.channel_error),
                    NotificationManager.IMPORTANCE_HIGH);
            error.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(error);

            // Contacts grouping
            NotificationChannelGroup group = new NotificationChannelGroup(
                    "contacts",
                    getString(R.string.channel_group_contacts));
            nm.createNotificationChannelGroup(group);
        }
    }

    public boolean ownFault(Throwable ex) {
        if (ex instanceof OutOfMemoryError)
            return false;

        if (ex instanceof RemoteException)
            return false;

        /*
            java.lang.NoSuchMethodError: No direct method ()V in class Landroid/security/IKeyChainService$Stub; or its super classes (declaration of 'android.security.IKeyChainService$Stub' appears in /system/framework/framework.jar!classes2.dex)
            java.lang.NoSuchMethodError: No direct method ()V in class Landroid/security/IKeyChainService$Stub; or its super classes (declaration of 'android.security.IKeyChainService$Stub' appears in /system/framework/framework.jar!classes2.dex)
            at com.android.keychain.KeyChainService$1.(KeyChainService.java:95)
            at com.android.keychain.KeyChainService.(KeyChainService.java:95)
            at java.lang.Class.newInstance(Native Method)
            at android.app.AppComponentFactory.instantiateService(AppComponentFactory.java:103)
         */
        if (ex instanceof NoSuchMethodError)
            return false;

        if (ex.getMessage() != null &&
                (ex.getMessage().startsWith("Bad notification posted") ||
                        ex.getMessage().contains("ActivityRecord not found") ||
                        ex.getMessage().startsWith("Unable to create layer")))
            return false;

        if (ex instanceof TimeoutException &&
                ex.getMessage() != null &&
                ex.getMessage().startsWith("com.sun.mail.imap.IMAPStore.finalize"))
            return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            if (ex instanceof RuntimeException && ex.getCause() instanceof DeadSystemException)
                return false;

        if (BuildConfig.BETA_RELEASE)
            return true;

        while (ex != null) {
            for (StackTraceElement ste : ex.getStackTrace())
                if (ste.getClassName().startsWith(getPackageName()))
                    return true;
            ex = ex.getCause();
        }

        return false;
    }

    static void writeCrashLog(Context context, Throwable ex) {
        File file = new File(context.getCacheDir(), "crash.log");
        Log.w("Writing exception to " + file);

        try (FileWriter out = new FileWriter(file, true)) {
            out.write(BuildConfig.VERSION_NAME + " " + new Date() + "\r\n");
            out.write(ex + "\r\n" + android.util.Log.getStackTraceString(ex) + "\r\n");
        } catch (IOException e) {
            Log.e(e);
        }
    }
}
