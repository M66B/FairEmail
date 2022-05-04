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

import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteFullException;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.DeadSystemException;
import android.os.Debug;
import android.os.IBinder;
import android.os.OperationCanceledException;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.Printer;
import android.view.Display;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.bugsnag.android.BreadcrumbType;
import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Client;
import com.bugsnag.android.ErrorTypes;
import com.bugsnag.android.Event;
import com.bugsnag.android.OnErrorCallback;
import com.bugsnag.android.OnSessionCallback;
import com.bugsnag.android.Session;
import com.bugsnag.android.Severity;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MailConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertPathValidatorException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.FolderClosedException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.StoreClosedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import io.requery.android.database.CursorWindowAllocationException;

public class Log {
    private static Context ctx;

    private static int level = android.util.Log.INFO;
    private static final int MAX_CRASH_REPORTS = (BuildConfig.TEST_RELEASE ? 50 : 5);
    private static final String TAG = "fairemail";

    static final String TOKEN_REFRESH_REQUIRED =
            "Token refresh required. Is there a VPN based app running?";

    public static void setLevel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debug = prefs.getBoolean("debug", false);
        if (debug)
            level = android.util.Log.DEBUG;
        else
            level = prefs.getInt("log_level", getDefaultLogLevel());
        android.util.Log.d(TAG, "Log level=" + level);
    }

    public static int getDefaultLogLevel() {
        return (BuildConfig.DEBUG ? android.util.Log.INFO : android.util.Log.WARN);
    }

    public static int d(String msg) {
        if (level <= android.util.Log.DEBUG)
            return android.util.Log.d(TAG, msg);
        else
            return 0;
    }

    public static int d(String tag, String msg) {
        if (level <= android.util.Log.DEBUG)
            return android.util.Log.d(tag, msg);
        else
            return 0;
    }

    public static int i(String msg) {
        if (level <= android.util.Log.INFO || BuildConfig.DEBUG)
            return android.util.Log.i(TAG, msg);
        else
            return 0;
    }

    public static int i(String tag, String msg) {
        if (level <= android.util.Log.INFO || BuildConfig.DEBUG)
            return android.util.Log.i(tag, msg);
        else
            return 0;
    }

    public static int w(String msg) {
        return android.util.Log.w(TAG, msg);
    }

    public static int e(String msg) {
        if (BuildConfig.BETA_RELEASE)
            try {
                Throwable ex = new Throwable(msg);
                List<StackTraceElement> ss = new ArrayList<>(Arrays.asList(ex.getStackTrace()));
                ss.remove(0);
                ex.setStackTrace(ss.toArray(new StackTraceElement[0]));
                Bugsnag.notify(ex, new OnErrorCallback() {
                    @Override
                    public boolean onError(@NonNull Event event) {
                        event.setSeverity(Severity.ERROR);
                        return true;
                    }
                });
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        return android.util.Log.e(TAG, msg);
    }

    public static int i(Throwable ex) {
        return android.util.Log.i(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int w(Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            try {
                final StackTraceElement[] ste = new Throwable().getStackTrace();
                Bugsnag.notify(ex, new OnErrorCallback() {
                    @Override
                    public boolean onError(@NonNull Event event) {
                        event.setSeverity(Severity.INFO);
                        if (ste.length > 1)
                            event.addMetadata("extra", "caller", ste[1].toString());
                        return true;
                    }
                });
            } catch (Throwable ex1) {
                ex1.printStackTrace();
            }
        return android.util.Log.w(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            try {
                final StackTraceElement[] ste = new Throwable().getStackTrace();
                Bugsnag.notify(ex, new OnErrorCallback() {
                    @Override
                    public boolean onError(@NonNull Event event) {
                        event.setSeverity(Severity.WARNING);
                        if (ste.length > 1)
                            event.addMetadata("extra", "caller", ste[1].toString());
                        return true;
                    }
                });
            } catch (Throwable ex1) {
                ex1.printStackTrace();
            }
        return android.util.Log.e(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int i(String prefix, Throwable ex) {
        return android.util.Log.i(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int w(String prefix, Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            try {
                Bugsnag.notify(ex, new OnErrorCallback() {
                    @Override
                    public boolean onError(@NonNull Event event) {
                        event.setSeverity(Severity.INFO);
                        return true;
                    }
                });
            } catch (Throwable ex1) {
                ex1.printStackTrace();
            }
        return android.util.Log.w(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(String prefix, Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            try {
                Bugsnag.notify(ex, new OnErrorCallback() {
                    @Override
                    public boolean onError(@NonNull Event event) {
                        event.setSeverity(Severity.WARNING);
                        return true;
                    }
                });
            } catch (Throwable ex1) {
                ex1.printStackTrace();
            }
        return android.util.Log.e(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static void persist(String message) {
        if (ctx == null)
            Log.e(message);
        else
            EntityLog.log(ctx, message);
    }

    static void setCrashReporting(boolean enabled) {
        try {
            if (enabled)
                Bugsnag.startSession();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void breadcrumb(String name, String key, String value) {
        Map<String, String> crumb = new HashMap<>();
        crumb.put(key, value);
        breadcrumb(name, crumb);
    }

    public static void breadcrumb(String name, Map<String, String> crumb) {
        try {
            crumb.put("free", Integer.toString(Log.getFreeMemMb()));

            StringBuilder sb = new StringBuilder();
            sb.append("Breadcrumb ").append(name);
            Map<String, Object> ocrumb = new HashMap<>();
            for (String key : crumb.keySet()) {
                String val = crumb.get(key);
                sb.append(' ').append(key).append('=').append(val);
                ocrumb.put(key, val);
            }
            Log.i(sb.toString());
            Bugsnag.leaveBreadcrumb(name, ocrumb, BreadcrumbType.LOG);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    static void setup(Context context) {
        ctx = context;
        setLevel(context);
        setupBugsnag(context);
    }

    private static void setupBugsnag(final Context context) {
        try {
            Log.i("Configuring Bugsnag");

            // https://docs.bugsnag.com/platforms/android/sdk/
            com.bugsnag.android.Configuration config =
                    new com.bugsnag.android.Configuration("9d2d57476a0614974449a3ec33f2604a");

            if (BuildConfig.DEBUG)
                config.setReleaseStage("debug");
            else {
                String type;
                if (Helper.hasValidFingerprint(context)) {
                    if (BuildConfig.PLAY_STORE_RELEASE)
                        type = "play";
                    else if (BuildConfig.AMAZON_RELEASE)
                        type = "amazon";
                    else
                        type = "full";
                } else {
                    if (BuildConfig.APPLICATION_ID.startsWith("eu.faircode.email"))
                        type = "other";
                    else
                        type = "clone";
                }
                config.setReleaseStage(type + (BuildConfig.BETA_RELEASE ? "/beta" : ""));
            }

            config.setAutoTrackSessions(false);

            ErrorTypes etypes = new ErrorTypes();
            etypes.setAnrs(BuildConfig.DEBUG);
            etypes.setNdkCrashes(false);
            config.setEnabledErrorTypes(etypes);
            config.setMaxBreadcrumbs(BuildConfig.PLAY_STORE_RELEASE ? 50 : 100);

            Set<String> ignore = new HashSet<>();

            ignore.add("com.sun.mail.util.MailConnectException");

            ignore.add("android.accounts.AuthenticatorException");
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
            ignore.add("javax.mail.internet.AddressException");
            ignore.add("javax.mail.internet.ParseException");
            ignore.add("javax.mail.MessageRemovedException");
            ignore.add("javax.mail.FolderNotFoundException");
            ignore.add("javax.mail.ReadOnlyFolderException");
            ignore.add("javax.mail.FolderClosedException");
            ignore.add("com.sun.mail.util.FolderClosedIOException");
            ignore.add("javax.mail.StoreClosedException");

            ignore.add("org.xmlpull.v1.XmlPullParserException");

            config.setDiscardClasses(ignore);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            ActivityManager am = Helper.getSystemService(context, ActivityManager.class);

            String no_internet = context.getString(R.string.title_no_internet);

            String installer = context.getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID);
            config.addMetadata("extra", "revision", BuildConfig.REVISION);
            config.addMetadata("extra", "installer", installer == null ? "-" : installer);
            config.addMetadata("extra", "installed", new Date(Helper.getInstallTime(context)).toString());
            config.addMetadata("extra", "fingerprint", Helper.hasValidFingerprint(context));
            config.addMetadata("extra", "memory_class", am.getMemoryClass());
            config.addMetadata("extra", "memory_class_large", am.getLargeMemoryClass());
            config.addMetadata("extra", "build_host", Build.HOST);
            config.addMetadata("extra", "build_time", new Date(Build.TIME));

            config.addOnSession(new OnSessionCallback() {
                @Override
                public boolean onSession(@NonNull Session session) {
                    // opt-in
                    return prefs.getBoolean("crash_reports", false) || BuildConfig.TEST_RELEASE;
                }
            });

            config.addOnError(new OnErrorCallback() {
                @Override
                public boolean onError(@NonNull Event event) {
                    // opt-in
                    boolean crash_reports = prefs.getBoolean("crash_reports", false);
                    if (!crash_reports && !BuildConfig.TEST_RELEASE)
                        return false;

                    Throwable ex = event.getOriginalError();
                    boolean should = shouldNotify(ex);

                    if (should) {
                        event.addMetadata("extra", "pid", Integer.toString(android.os.Process.myPid()));
                        event.addMetadata("extra", "thread", Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
                        event.addMetadata("extra", "memory_free", getFreeMemMb());
                        event.addMetadata("extra", "memory_available", getAvailableMb());
                        event.addMetadata("extra", "native_allocated", Debug.getNativeHeapAllocatedSize() / 1024L / 1024L);
                        event.addMetadata("extra", "native_size", Debug.getNativeHeapSize() / 1024L / 1024L);

                        Boolean ignoringOptimizations = Helper.isIgnoringOptimizations(context);
                        event.addMetadata("extra", "optimizing", (ignoringOptimizations != null && !ignoringOptimizations));

                        String theme = prefs.getString("theme", "blue_orange_system");
                        event.addMetadata("extra", "theme", theme);
                        event.addMetadata("extra", "package", BuildConfig.APPLICATION_ID);
                    }

                    return should;
                }

                private boolean shouldNotify(Throwable ex) {
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
                            (no_internet.equals(ex.getMessage()) ||
                                    TOKEN_REFRESH_REQUIRED.equals(ex.getMessage()) ||
                                    "Not connected".equals(ex.getMessage()) ||
                                    "This operation is not allowed on a closed folder".equals(ex.getMessage())))
                        return false;

                    if (ex instanceof FileNotFoundException &&
                            ex.getMessage() != null &&
                            (ex.getMessage().startsWith("Download image failed") ||
                                    ex.getMessage().startsWith("http://") ||
                                    ex.getMessage().startsWith("https://") ||
                                    ex.getMessage().startsWith("content://")))
                        return false;

                    if (ex instanceof IOException &&
                            ex.getCause() instanceof MessageRemovedException)
                        return false;

                    if (ex instanceof IOException &&
                            ex.getMessage() != null &&
                            (ex.getMessage().startsWith("HTTP status=") ||
                                    "NetworkError".equals(ex.getMessage()) || // account manager
                                    "Resetting to invalid mark".equals(ex.getMessage()) ||
                                    "Mark has been invalidated.".equals(ex.getMessage())))
                        return false;

                    if (ex instanceof SSLPeerUnverifiedException ||
                            ex instanceof EmailService.UntrustedException)
                        return false;

                    if (ex instanceof SSLHandshakeException &&
                            ex.getCause() instanceof CertPathValidatorException)
                        return false; // checkUpdate!

                    if (ex instanceof RuntimeException &&
                            "Illegal meta data value: the child service doesn't exist".equals(ex.getMessage()))
                        return false;

                    if (isDead(ex))
                        return false;

                    // Rate limit
                    int count = prefs.getInt("crash_report_count", 0) + 1;
                    prefs.edit().putInt("crash_report_count", count).apply();

                    return (count <= MAX_CRASH_REPORTS);
                }
            });

            Bugsnag.start(context, config);

            Client client = Bugsnag.getClient();

            String uuid = prefs.getString("uuid", null);
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
                prefs.edit().putString("uuid", uuid).apply();
            }
            Log.i("uuid=" + uuid);
            client.setUser(uuid, null, null);

            if (prefs.getBoolean("crash_reports", false) || BuildConfig.TEST_RELEASE)
                Bugsnag.startSession();
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.AssertionError: No NameTypeIndex match for SHORT_DAYLIGHT
                  at android.icu.impl.TimeZoneNamesImpl$ZNames.getNameTypeIndex(TimeZoneNamesImpl.java:724)
                  at android.icu.impl.TimeZoneNamesImpl$ZNames.getName(TimeZoneNamesImpl.java:790)
                  at android.icu.impl.TimeZoneNamesImpl.getTimeZoneDisplayName(TimeZoneNamesImpl.java:183)
                  at android.icu.text.TimeZoneNames.getDisplayName(TimeZoneNames.java:261)
                  at java.util.TimeZone.getDisplayName(TimeZone.java:405)
                  at java.util.Date.toString(Date.java:1066)
             */
        }
    }

    static void logExtras(Intent intent) {
        if (intent != null)
            logBundle(intent.getExtras());
    }

    static void logBundle(Bundle data) {
        for (String extra : getExtras(data))
            i(extra);
    }

    static List<String> getExtras(Bundle data) {
        List<String> result = new ArrayList<>();
        if (data == null)
            return result;

        try {
            Set<String> keys = data.keySet();
            for (String key : keys) {
                Object v = data.get(key);

                Object value = v;
                int length = -1;
                if (v != null && v.getClass().isArray()) {
                    length = Array.getLength(v);
                    String[] elements = new String[Math.min(length, 10)];
                    for (int i = 0; i < elements.length; i++) {
                        Object element = Array.get(v, i);
                        if (element instanceof Long)
                            elements[i] = element + " (0x" + Long.toHexString((Long) element) + ")";
                        else
                            elements[i] = (element == null ? "<null>" : printableString(element.toString()));
                    }
                    value = TextUtils.join(",", elements);
                    if (length > 10)
                        value += ", ...";
                    value = "[" + value + "]";
                } else if (v instanceof Long)
                    value = v + " (0x" + Long.toHexString((Long) v) + ")";
                else if (v instanceof Bundle)
                    value = "{" + TextUtils.join(" ", getExtras((Bundle) v)) + "}";

                result.add(key + "=" + value + (value == null ? "" :
                        " (" + v.getClass().getSimpleName() + (length < 0 ? "" : ":" + length) + ")"));
            }
        } catch (Throwable ex) {
            // android.os.BadParcelableException: ClassNotFoundException when unmarshalling: ...

            // java.lang.RuntimeException: Failure delivering result ResultInfo{who=null, request=1172374955, result=0, data=Intent { (has extras) }} to activity {eu.faircode.email/eu.faircode.email.ActivityCompose}: java.lang.RuntimeException: Parcelable encountered ClassNotFoundException reading a Serializable object (name = com.lyrebirdstudio.imagecameralib.utils.ImageCameraLibReturnTypes)
            //        at android.app.ActivityThread.deliverResults(ActivityThread.java:4382)
            //        at android.app.ActivityThread.handleSendResult(ActivityThread.java:4426)
            //        at android.app.ActivityThread.-wrap20(Unknown)
            //        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1685)
            //        at android.os.Handler.dispatchMessage(Handler.java:106)
            //        at android.os.Looper.loop(Looper.java:164)
            //        at android.app.ActivityThread.main(ActivityThread.java:6626)
            //        at java.lang.reflect.Method.invoke(Method.java:-2)
            //        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:438)
            //        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:811)
            //
            // Caused by: java.lang.RuntimeException: Parcelable encountered ClassNotFoundException reading a Serializable object (name = com.lyrebirdstudio.imagecameralib.utils.ImageCameraLibReturnTypes)
            //        at android.os.Parcel.readSerializable(Parcel.java:3019)
            //        at android.os.Parcel.readValue(Parcel.java:2805)
            //        at android.os.Parcel.readArrayMapInternal(Parcel.java:3123)
            //        at android.os.BaseBundle.initializeFromParcelLocked(BaseBundle.java:273)
            //        at android.os.BaseBundle.unparcel(BaseBundle.java:226)
            //        at android.os.BaseBundle.keySet(BaseBundle.java:520)
            //        at eu.faircode.email.Log.getExtras(Log:565)
            //        at eu.faircode.email.Log.logBundle(Log:555)
            //        at eu.faircode.email.Log.logExtras(Log:551)
            //        at eu.faircode.email.ActivityBase.onActivityResult(ActivityBase:376)
            //        at android.app.Activity.dispatchActivityResult(Activity.java:7305)
            //        at android.app.ActivityThread.deliverResults(ActivityThread.java:4378)
            //        at android.app.ActivityThread.handleSendResult(ActivityThread.java:4426)
            //        at android.app.ActivityThread.-wrap20(Unknown)
            //        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1685)
            //        at android.os.Handler.dispatchMessage(Handler.java:106)
            //        at android.os.Looper.loop(Looper.java:164)
            //        at android.app.ActivityThread.main(ActivityThread.java:6626)
            //        at java.lang.reflect.Method.invoke(Method.java:-2)
            //        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:438)
            //        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:811)
            //
            // Caused by: java.lang.ClassNotFoundException: com.lyrebirdstudio.imagecameralib.utils.ImageCameraLibReturnTypes
            //
            // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.lyrebirdstudio.imagecameralib.utils.ImageCameraLibReturnTypes" on path: DexPathList[[zip file "/data/app/eu.faircode.email-b4dvFM1MrZ5iBeNXAXRJhQ==/base.apk"],nativeLibraryDirectories=[/data/app/eu.faircode.email-b4dvFM1MrZ5iBeNXAXRJhQ==/lib/arm, /data/app/eu.faircode.email-b4dvFM1MrZ5iBeNXAXRJhQ==/base.apk!/lib/armeabi-v7a, /system/lib, /system/vendor/lib]]
            Log.e(ex);
        }

        return result;
    }

    static String printableString(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char kar = value.charAt(i);
            if (kar == '\n')
                result.append('|');
            else if (kar == ' ')
                result.append('_');
            else if (!Helper.isPrintableChar(kar))
                result.append('{').append(Integer.toHexString(kar)).append('}');
            else
                result.append(kar);
        }
        return result.toString();
    }

    static void logMemory(Context context, String message) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = Helper.getSystemService(context, ActivityManager.class);
        activityManager.getMemoryInfo(mi);
        int mb = Math.round(mi.availMem / 0x100000L);
        int perc = Math.round(mi.availMem / (float) mi.totalMem * 100.0f);
        Log.i(message + " " + mb + " MB" + " " + perc + " %");
    }

    static boolean isOwnFault(Throwable ex) {
        if (!Helper.isSupportedDevice())
            return false;

        if (ex instanceof OutOfMemoryError ||
                ex.getCause() instanceof OutOfMemoryError)
            return false;

        if (ex instanceof RemoteException)
            return false;

        if (ex instanceof UnsatisfiedLinkError ||
                ex.getCause() instanceof UnsatisfiedLinkError)
            /*
                java.lang.UnsatisfiedLinkError: dlopen failed: couldn't map "/mnt/asec/eu.faircode.email-1/base.apk!/lib/arm64-v8a/libsqlite3x.so" segment 0: Permission denied
                  at java.lang.Runtime.loadLibrary0(Runtime.java:1016)
                  at java.lang.System.loadLibrary(System.java:1657)
                  at io.requery.android.database.sqlite.SQLiteDatabase.<clinit>(SourceFile:91)
             */
            return false;

        if (ex instanceof InternalError &&
                "Thread starting during runtime shutdown".equals(ex.getMessage()))
            /*
                java.lang.InternalError: Thread starting during runtime shutdown
                  at java.lang.Thread.nativeCreate(Native Method)
                  at java.lang.Thread.start(Thread.java:1063)
                  at java.util.concurrent.ThreadPoolExecutor.addWorker(ThreadPoolExecutor.java:921)
                  at java.util.concurrent.ThreadPoolExecutor.processWorkerExit(ThreadPoolExecutor.java:989)
                  at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1131)
                  at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:588)
                  at java.lang.Thread.run(Thread.java:818)
             */
            return false;

        if ("android.app.RemoteServiceException".equals(ex.getClass().getName()))
            /*
                android.app.RemoteServiceException: Bad notification for startForeground: java.util.ConcurrentModificationException
                  at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2204)
            */
            return false;

        if ("android.view.WindowManager$BadTokenException".equals(ex.getClass().getName()))
            /*
                android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@e9084db is not valid; is your activity running?
                  at android.view.ViewRootImpl.setView(ViewRootImpl.java:827)
                  at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:356)
                  at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:93)
                  at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:4084)
                  at android.app.servertransaction.ResumeActivityItem.execute(ResumeActivityItem.java:51)
                  at android.app.servertransaction.TransactionExecutor.executeLifecycleState(TransactionExecutor.java:145)
                  at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:70)
                  at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1976)
             */
            return false;

        if (ex instanceof NoSuchMethodError)
            /*
                java.lang.NoSuchMethodError: No direct method ()V in class Landroid/security/IKeyChainService$Stub; or its super classes (declaration of 'android.security.IKeyChainService$Stub' appears in /system/framework/framework.jar!classes2.dex)
                  at com.android.keychain.KeyChainService$1.(KeyChainService.java:95)
                  at com.android.keychain.KeyChainService.(KeyChainService.java:95)
                  at java.lang.Class.newInstance(Native Method)
                  at android.app.AppComponentFactory.instantiateService(AppComponentFactory.java:103)
             */
            return false;

        if (ex instanceof IllegalStateException &&
                "Drag shadow dimensions must be positive".equals(ex.getMessage()))
            /*
                Android 9 only
                java.lang.IllegalStateException: Drag shadow dimensions must be positive
                java.lang.IllegalStateException: Drag shadow dimensions must be positive
                  at android.view.View.startDragAndDrop(View.java:24027)
                  at android.widget.Editor.startDragAndDrop(Editor.java:1165)
                  at android.widget.Editor.performLongClick(Editor.java:1191)
                  at android.widget.TextView.performLongClick(TextView.java:11346)
                  at android.view.View.performLongClick(View.java:6653)
                  at android.view.View$CheckForLongPress.run(View.java:25855)
                  at android.os.Handler.handleCallback(Handler.java:873)
            */
            return false;

        if (ex instanceof IllegalStateException &&
                "Results have already been set".equals(ex.getMessage()))
            /*
                Play billing?
                java.lang.IllegalStateException: Results have already been set
                  at Gu.a(Unknown:8)
                  at Fq.a(Unknown:29)
                  at Fk.b(Unknown:17)
                  at Fk.a(Unknown:12)
                  at Fk.b(Unknown:5)
                  at Ex.a(Unknown:3)
                  at Ep.b(Unknown:9)
                  at Ep.a(Unknown:76)
                  at Ep.a(Unknown:16)
                  at GH.a(Unknown:2)
                  at Gz.a(Unknown:48)
                  at GC.handleMessage(Unknown:6)
                  at android.os.Handler.dispatchMessage(Handler.java:108)
                  at android.os.Looper.loop(Looper.java:166)
                  at android.os.HandlerThread.run(HandlerThread.java:65)
             */
            return false;

        if (ex instanceof IllegalArgumentException &&
                ex.getCause() instanceof RemoteException)
            /*
                java.lang.IllegalArgumentException
                  at android.os.Parcel.createException(Parcel.java:1954)
                  at android.os.Parcel.readException(Parcel.java:1918)
                  at android.os.Parcel.readException(Parcel.java:1868)
                  at android.view.IWindowSession$Stub$Proxy.addToDisplay(IWindowSession.java:826)
                  at android.view.ViewRootImpl.setView(ViewRootImpl.java:758)
                  at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:356)
                  at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:93)
                  at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:3906)
                  at android.app.servertransaction.ResumeActivityItem.execute(ResumeActivityItem.java:51)
                  at android.app.servertransaction.TransactionExecutor.executeLifecycleState(TransactionExecutor.java:145)
                  at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:70)
                  at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1816)
                  at android.os.Handler.dispatchMessage(Handler.java:106)
                  at android.os.Looper.loop(Looper.java:193)
                  at android.app.ActivityThread.main(ActivityThread.java:6718)
                  at java.lang.reflect.Method.invoke(Native Method)
                  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:491)
                  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:858)
                Caused by: android.os.RemoteException: Remote stack trace:
                  at android.view.SurfaceControl.nativeCreate(Native Method)
                  at android.view.SurfaceControl.<init>(SurfaceControl.java:630)
                  at android.view.SurfaceControl.<init>(SurfaceControl.java:60)
                  at android.view.SurfaceControl$Builder.build(SurfaceControl.java:386)
                  at com.android.server.wm.WindowContainer.onParentSet(WindowContainer.java:184)
             */
            return false;

        if (ex instanceof IllegalArgumentException &&
                ex.getMessage() != null &&
                ex.getMessage().startsWith("Tmp detached view should be removed from RecyclerView before it can be recycled"))
            /*
                Android 9 only?
                java.lang.IllegalArgumentException: Tmp detached view should be removed from RecyclerView before it can be recycled: ViewHolder{e3b70bd position=0 id=1, oldPos=-1, pLpos:-1 update tmpDetached no parent} androidx.recyclerview.widget.RecyclerView{f0fe5b1 VFED..... ......ID 0,0-641,456 #7f090293 app:id/rvAccount}, adapter:eu.faircode.email.AdapterNavAccount@9a6ea96, layout:androidx.recyclerview.widget.LinearLayoutManager@d8fc617, context:eu.faircode.email.ActivityView@82a6ec4
                at androidx.recyclerview.widget.RecyclerView$Recycler.recycleViewHolderInternal(SourceFile:6435)
                at androidx.recyclerview.widget.RecyclerView.removeAnimatingView(SourceFile:1456)
                at androidx.recyclerview.widget.RecyclerView$ItemAnimatorRestoreListener.onAnimationFinished(SourceFile:12690)
                at androidx.recyclerview.widget.RecyclerView$ItemAnimator.dispatchAnimationFinished(SourceFile:13190)
                at androidx.recyclerview.widget.SimpleItemAnimator.dispatchChangeFinished(SourceFile:317)
                at androidx.recyclerview.widget.DefaultItemAnimator$8.onAnimationEnd(SourceFile:391)
                at android.view.ViewPropertyAnimator$AnimatorEventListener.onAnimationEnd(ViewPropertyAnimator.java:1122)
            */
            return false;

        if (ex instanceof IllegalArgumentException &&
                "page introduces incorrect tiling".equals(ex.getMessage()))
            /*
                java.lang.IllegalArgumentException: page introduces incorrect tiling
                  at androidx.paging.PagedStorage.insertPage(SourceFile:545)
                  at androidx.paging.PagedStorage.tryInsertPageAndTrim(SourceFile:504)
                  at androidx.paging.TiledPagedList$1.onPageResult(SourceFile:60)
                  at androidx.paging.DataSource$LoadCallbackHelper$1.run(SourceFile:324)
                  at android.os.Handler.handleCallback(Handler.java:789)
            */
            return false;

        if (ex instanceof IllegalArgumentException &&
                "Can't interpolate between two incompatible pathData".equals(ex.getMessage()))
            /*
                java.lang.IllegalArgumentException: Can't interpolate between two incompatible pathData
                  at android.animation.AnimatorInflater$PathDataEvaluator.evaluate(AnimatorInflater.java:265)
                  at android.animation.AnimatorInflater$PathDataEvaluator.evaluate(AnimatorInflater.java:262)
                  at android.animation.KeyframeSet.getValue(KeyframeSet.java:210)
                  at android.animation.PropertyValuesHolder.calculateValue(PropertyValuesHolder.java:1018)
                  at android.animation.ValueAnimator.animateValue(ValueAnimator.java:1341)
                  at android.animation.ObjectAnimator.animateValue(ObjectAnimator.java:986)
                  at android.animation.ValueAnimator.animateBasedOnTime(ValueAnimator.java:1258)
                  at android.animation.ValueAnimator.doAnimationFrame(ValueAnimator.java:1306)
                  at android.animation.AnimationHandler.doAnimationFrame(AnimationHandler.java:146)
                  at android.animation.AnimationHandler.-wrap2(AnimationHandler.java)
                  at android.animation.AnimationHandler$1.doFrame(AnimationHandler.java:54)
                  at android.view.Choreographer$CallbackRecord.run(Choreographer.java:925)
                  at android.view.Choreographer.doCallbacks(Choreographer.java:702)
                  at android.view.Choreographer.doFrame(Choreographer.java:635)
                  at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:913)
                  at android.os.Handler.handleCallback(Handler.java:751)
             */
            return false;

        if (ex instanceof IllegalMonitorStateException)
            /*
                java.lang.IllegalMonitorStateException
                  at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.signal(AbstractQueuedSynchronizer.java:1959)
                  at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1142)
                  at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:849)
                  at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1092)
                  at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1152)
                  at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
                  at java.lang.Thread.run(Thread.java:764)
             */
            return false;

        if (ex instanceof RuntimeException &&
                ex.getCause() instanceof TransactionTooLargeException)
            // Some Android versions (Samsung) send images as clip data
            return false;

        if (ex instanceof RuntimeException &&
                ex.getMessage() != null &&
                (ex.getMessage().startsWith("Could not get application info") ||
                        ex.getMessage().startsWith("Unable to create service") ||
                        ex.getMessage().startsWith("Unable to start service") ||
                        ex.getMessage().startsWith("Unable to resume activity") ||
                        ex.getMessage().startsWith("Failure delivering result")))
            return false;
            /*
                java.lang.RuntimeException: Unable to unbind to service androidx.work.impl.background.systemjob.SystemJobService@291a412 with Intent { cmp=eu.faircode.email/androidx.work.impl.background.systemjob.SystemJobService }: java.lang.RuntimeException: android.os.DeadSystemException
                  at android.app.ActivityThread.handleUnbindService(ActivityThread.java:4352)

                java.lang.RuntimeException: Could not get application info.
                  at CH0.a(PG:11)
                  at org.chromium.content.browser.ChildProcessLauncherHelperImpl.a(PG:34)
                  at Fn2.run(PG:5)
                  at android.os.Handler.handleCallback(Handler.java:874)
                  at android.os.Handler.dispatchMessage(Handler.java:100)
                  at android.os.Looper.loop(Looper.java:198)
                  at android.os.HandlerThread.run(HandlerThread.java:65)

                java.lang.RuntimeException: Unable to create service eu.faircode.email.ServiceSynchronize: java.lang.NullPointerException: Attempt to invoke interface method 'java.util.List android.os.IUserManager.getProfiles(int, boolean)' on a null object reference
                  at android.app.ActivityThread.handleCreateService(ActivityThread.java:2739)

                java.lang.RuntimeException: Failure delivering result ResultInfo{who=@android:autoFillAuth:, request=2162688, result=-1, data=Intent { (has extras) }} to activity {eu.faircode.email/eu.faircode.email.ActivitySetup}: java.lang.NullPointerException: Attempt to invoke interface method 'java.lang.Object java.util.List.get(int)' on a null object reference
                  at android.app.ActivityThread.deliverResults(ActivityThread.java:4469)
                  at android.app.ActivityThread.handleSendResult(ActivityThread.java:4511)
                  at android.app.servertransaction.ActivityResultItem.execute(ActivityResultItem.java:49)
                  at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:108)
                  at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:68)
                  at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1821)
                  at android.os.Handler.dispatchMessage(Handler.java:106)
                  at android.os.Looper.loop(Looper.java:193)
                  at android.app.ActivityThread.main(ActivityThread.java:6874)
                  at java.lang.reflect.Method.invoke(Native Method)
                  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:493)
                  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:861)
                Caused by: java.lang.NullPointerException: Attempt to invoke interface method 'java.lang.Object java.util.List.get(int)' on a null object reference
                  at android.os.Parcel.createException(Parcel.java:1956)
                  at android.os.Parcel.readException(Parcel.java:1918)
                  at android.os.Parcel.readException(Parcel.java:1868)
                  at android.view.autofill.IAutoFillManager$Stub$Proxy.setAuthenticationResult(IAutoFillManager.java:729)
                  at android.view.autofill.AutofillManager.onAuthenticationResult(AutofillManager.java:1474)
                  at android.app.Activity.dispatchActivityResult(Activity.java:7497)
                  at android.app.ActivityThread.deliverResults(ActivityThread.java:4462)
                  ... 11 more
                Caused by: android.os.RemoteException: Remote stack trace:
                  at com.android.server.autofill.Session.setAuthenticationResultLocked(Session.java:1005)
                  at com.android.server.autofill.AutofillManagerServiceImpl.setAuthenticationResultLocked(AutofillManagerServiceImpl.java:325)
                  at com.android.server.autofill.AutofillManagerService$AutoFillManagerServiceStub.setAuthenticationResult(AutofillManagerService.java:863)
                  at android.view.autofill.IAutoFillManager$Stub.onTransact(IAutoFillManager.java:289)
                  at android.os.Binder.execTransact(Binder.java:731)
             */

        if (ex instanceof RuntimeException &&
                "InputChannel is not initialized.".equals(ex.getMessage()))
            return false;
            /*
                java.lang.RuntimeException: InputChannel is not initialized.
                  at android.view.InputEventReceiver.nativeInit(Native Method)
                  at android.view.InputEventReceiver.<init>(InputEventReceiver.java:72)
                  at android.view.ViewRootImpl$WindowInputEventReceiver.<init>(ViewRootImpl.java:7612)
                  at android.view.ViewRootImpl.setView(ViewRootImpl.java:957)
                  at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:387)
                  at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:96)
                  at android.widget.Toast$TN.handleShow(Toast.java:514)
                  at android.widget.Toast$TN$1.handleMessage(Toast.java:417)
                  at android.os.Handler.dispatchMessage(Handler.java:107)
                  at android.os.Looper.loop(Looper.java:214)
                  at android.app.ActivityThread.main(ActivityThread.java:7397)
             */

        if (ex.getMessage() != null &&
                (ex.getMessage().startsWith("Bad notification posted") ||
                        ex.getMessage().contains("ActivityRecord not found") ||
                        ex.getMessage().startsWith("Unable to create layer") ||
                        ex.getMessage().startsWith("Illegal meta data value") ||
                        ex.getMessage().startsWith("Context.startForegroundService") ||
                        ex.getMessage().startsWith("PARAGRAPH span must start at paragraph boundary")))
            return false;

        if (ex instanceof TimeoutException &&
                ex.getMessage() != null &&
                ex.getMessage().contains("finalize"))
            return false;

        if (ex instanceof CursorWindowAllocationException ||
                "android.database.CursorWindowAllocationException".equals(ex.getClass().getName()))
            /*
                android.database.CursorWindowAllocationException: Could not allocate CursorWindow '/data/user/0/eu.faircode.email/no_backup/androidx.work.workdb' of size 2097152 due to error -12.
                  at android.database.CursorWindow.nativeCreate(Native Method)
                  at android.database.CursorWindow.<init>(CursorWindow.java:139)
                  at android.database.CursorWindow.<init>(CursorWindow.java:120)
                  at android.database.AbstractWindowedCursor.clearOrCreateWindow(AbstractWindowedCursor.java:202)
                  at android.database.sqlite.SQLiteCursor.fillWindow(SQLiteCursor.java:147)
                  at android.database.sqlite.SQLiteCursor.getCount(SQLiteCursor.java:140)
                  at android.database.AbstractCursor.moveToPosition(AbstractCursor.java:232)
                  at android.database.AbstractCursor.moveToNext(AbstractCursor.java:281)
                  at androidx.room.InvalidationTracker$1.checkUpdatedTable(SourceFile:417)
                  at androidx.room.InvalidationTracker$1.run(SourceFile:388)
                  at androidx.work.impl.utils.SerialExecutor$Task.run(SourceFile:91)
             */
            return false;

        if (ex instanceof RuntimeException &&
                ex.getCause() instanceof CursorWindowAllocationException)
            /*
                java.lang.RuntimeException: Exception while computing database live data.
                  at androidx.room.RoomTrackingLiveData$1.run(SourceFile:10)
                  at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
                  at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
                  at java.lang.Thread.run(Thread.java:764)
                Caused by: io.requery.android.database.CursorWindowAllocationException: Cursor window allocation of 2048 kb failed.
                  at io.requery.android.database.CursorWindow.<init>(SourceFile:7)
                  at io.requery.android.database.CursorWindow.<init>(SourceFile:1)
                  at io.requery.android.database.AbstractWindowedCursor.clearOrCreateWindow(SourceFile:2)
                  at io.requery.android.database.sqlite.SQLiteCursor.fillWindow(SourceFile:1)
                  at io.requery.android.database.sqlite.SQLiteCursor.getCount(SourceFile:2)
                  at eu.faircode.email.DaoAttachment_Impl$14.call(SourceFile:16)
                  at eu.faircode.email.DaoAttachment_Impl$14.call(SourceFile:1)
                  at androidx.room.RoomTrackingLiveData$1.run(SourceFile:7)
             */
            return false;

        if (ex instanceof SQLiteFullException) // database or disk is full (code 13 SQLITE_FULL)
            return false;

        if ("android.util.SuperNotCalledException".equals(ex.getClass().getName()))
            /*
                android.util.SuperNotCalledException: Activity {eu.faircode.email/eu.faircode.email.ActivityView} did not call through to super.onResume()
                  at android.app.Activity.performResume(Activity.java:7304)
                  at android.app.ActivityThread.performNewIntents(ActivityThread.java:3165)
                  at android.app.ActivityThread.handleNewIntent(ActivityThread.java:3180)
                  at android.app.servertransaction.NewIntentItem.execute(NewIntentItem.java:49)
             */
            return false;

        if ("android.view.WindowManager$InvalidDisplayException".equals(ex.getClass().getName()))
            /*
                android.view.WindowManager$InvalidDisplayException: Unable to add window android.view.ViewRootImpl$W@d7b5a0b -- the specified display can not be found
                  at android.view.ViewRootImpl.setView(ViewRootImpl.java:854)
                  at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:356)
                  at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:93)
                  at android.widget.PopupWindow.invokePopup(PopupWindow.java:1492)
                  at android.widget.PopupWindow.showAsDropDown(PopupWindow.java:1342)
                  at androidx.appcompat.widget.AppCompatPopupWindow.showAsDropDown(SourceFile:77)
                  at androidx.core.widget.PopupWindowCompat.showAsDropDown(SourceFile:69)
                  at androidx.appcompat.widget.ListPopupWindow.show(SourceFile:754)
                  at androidx.appcompat.view.menu.CascadingMenuPopup.showMenu(SourceFile:486)
                  at androidx.appcompat.view.menu.CascadingMenuPopup.show(SourceFile:265)
                  at androidx.appcompat.view.menu.MenuPopupHelper.showPopup(SourceFile:290)
                  at androidx.appcompat.view.menu.MenuPopupHelper.tryShow(SourceFile:177)
                  at androidx.appcompat.widget.ActionMenuPresenter$OpenOverflowRunnable.run(SourceFile:792)
               */
            return false;

        StackTraceElement[] stack = ex.getStackTrace();
        if (ex instanceof IndexOutOfBoundsException &&
                stack.length > 0 &&
                "android.text.TextLine".equals(stack[0].getClassName()) &&
                "measure".equals(stack[0].getMethodName()))
            /*
                java.lang.IndexOutOfBoundsException: offset(21) should be less than line limit(20)
                  at android.text.TextLine.measure(Unknown Source:233)
                  at android.text.Layout.getHorizontal(Unknown Source:104)
                  at android.text.Layout.getHorizontal(Unknown Source:4)
                  at android.text.Layout.getPrimaryHorizontal(Unknown Source:4)
                  at android.text.Layout.getPrimaryHorizontal(Unknown Source:1)
                  at android.widget.Editor$ActionPinnedPopupWindow.computeLocalPosition(Unknown Source:275)
                  at android.widget.Editor$PinnedPopupWindow.show(Unknown Source:15)
                  at android.widget.Editor$ActionPinnedPopupWindow.show(Unknown Source:3)
                  at android.widget.Editor$EmailAddPopupWindow.show(Unknown Source:92)
                  at android.widget.Editor$1.run(Unknown Source:6)
                  at android.os.Handler.handleCallback(Unknown Source:2)
             */
            return false;

        if (ex instanceof IllegalArgumentException &&
                stack.length > 0 &&
                "android.os.Parcel".equals(stack[0].getClassName()) &&
                ("createException".equals(stack[0].getMethodName()) ||
                        "readException".equals(stack[0].getMethodName())))
            /*
                java.lang.IllegalArgumentException
                  at android.os.Parcel.createException(Parcel.java:1954)
                  at android.os.Parcel.readException(Parcel.java:1918)
                  at android.os.Parcel.readException(Parcel.java:1868)
                  at android.view.IWindowSession$Stub$Proxy.addToDisplay(IWindowSession.java:826)
                  at android.view.ViewRootImpl.setView(ViewRootImpl.java:758)
                  at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:356)
                  at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:93)
                  at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:3906)

                java.lang.NullPointerException: Attempt to invoke virtual method 'int com.android.server.job.controllers.JobStatus.getUid()' on a null object reference
                  at android.os.Parcel.readException(Parcel.java:1605)
                  at android.os.Parcel.readException(Parcel.java:1552)
                  at android.app.job.IJobCallback$Stub$Proxy.jobFinished(IJobCallback.java:167)
                  at android.app.job.JobService$JobHandler.handleMessage(JobService.java:147)
                  at android.os.Handler.dispatchMessage(Handler.java:102)
             */
            return false;

        if (ex instanceof NullPointerException &&
                stack.length > 0 &&
                "android.hardware.biometrics.BiometricPrompt".equals(stack[0].getClassName()))
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String android.hardware.fingerprint.FingerprintManager.getErrorString(int, int)' on a null object reference
                  at android.hardware.biometrics.BiometricPrompt.lambda$sendError$0(BiometricPrompt.java:490)
                  at android.hardware.biometrics.-$$Lambda$BiometricPrompt$HqBGXtBUWNc-v8NoHYsj2gLfaRw.run(Unknown Source:6)
                  at android.os.Handler.handleCallback(Handler.java:873)
             */
            return false;

        if (ex instanceof NullPointerException &&
                stack.length > 0 &&
                "android.graphics.Rect".equals(stack[0].getClassName()) &&
                "set".equals(stack[0].getMethodName()))
            /*
                java.lang.NullPointerException: Attempt to read from field 'int android.graphics.Rect.left' on a null object reference
                  at android.graphics.Rect.set(Rect.java:371)
                  at android.view.InsetsState.readFromParcel(InsetsState.java:453)
                  at android.view.IWindowSession$Stub$Proxy.addToDisplay(IWindowSession.java:1264)
                  at android.view.ViewRootImpl.setView(ViewRootImpl.java:865)
                  at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:387)
                  at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:96)
                  at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:4297)
             */
            return false;

        if (ex instanceof NullPointerException &&
                stack.length > 0 &&
                "android.app.ActivityThread".equals(stack[0].getClassName()) &&
                "handleStopActivity".equals(stack[0].getMethodName()))
            /*
                Android: 6.0.1
                java.lang.NullPointerException: Attempt to read from field 'android.app.Activity android.app.ActivityThread$ActivityClientRecord.activity' on a null object reference
                  at android.app.ActivityThread.handleStopActivity(ActivityThread.java:4766)
                  at android.app.ActivityThread.access$1400(ActivityThread.java:221)
                  at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1823)
                  at android.os.Handler.dispatchMessage(Handler.java:102)
                  at android.os.Looper.loop(Looper.java:158)
                  at android.app.ActivityThread.main(ActivityThread.java:7224)
             */
            return false;

        if (ex instanceof NullPointerException &&
                ex.getCause() instanceof RemoteException)
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'boolean com.android.server.autofill.RemoteFillService$PendingRequest.cancel()' on a null object reference
                    at android.os.Parcel.createException(Parcel.java:1956)
                    at android.os.Parcel.readException(Parcel.java:1918)
                    at android.os.Parcel.readException(Parcel.java:1868)
                    at android.app.IActivityManager$Stub$Proxy.reportAssistContextExtras(IActivityManager.java:7079)
                    at android.app.ActivityThread.handleRequestAssistContextExtras(ActivityThread.java:3338)
                    at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1839)
                    at android.os.Handler.dispatchMessage(Handler.java:106)
                    at android.os.Looper.loop(Looper.java:193)
                    at android.app.ActivityThread.main(ActivityThread.java:6971)
                    at java.lang.reflect.Method.invoke(Native Method)
                    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:493)
                    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:865)
                Caused by: android.os.RemoteException: Remote stack trace:
                    at com.android.server.autofill.RemoteFillService.cancelCurrentRequest(RemoteFillService.java:177)
                    at com.android.server.autofill.Session.cancelCurrentRequestLocked(Session.java:465)
                    at com.android.server.autofill.Session.access$1000(Session.java:118)
                    at com.android.server.autofill.Session$1.onHandleAssistData(Session.java:322)
                    at com.android.server.am.ActivityManagerService.reportAssistContextExtras(ActivityManagerService.java:14510)
             */
            return false;

        if (ex instanceof IndexOutOfBoundsException &&
                stack.length > 0 &&
                "android.text.SpannableStringInternal".equals(stack[0].getClassName()) &&
                "checkRange".equals(stack[0].getMethodName()))
            /*
                java.lang.IndexOutOfBoundsException: setSpan (-1 ... -1) starts before 0
                  at android.text.SpannableStringInternal.checkRange(SpannableStringInternal.java:478)
                  at android.text.SpannableStringInternal.setSpan(SpannableStringInternal.java:189)
                  at android.text.SpannableStringInternal.setSpan(SpannableStringInternal.java:178)
                  at android.text.SpannableString.setSpan(SpannableString.java:60)
                  at android.text.Selection.setSelection(Selection.java:93)
                  at android.text.Selection.setSelection(Selection.java:77)
                  at android.widget.Editor$SelectionHandleView.updateSelection(Editor.java:5281)
                  at android.widget.Editor$HandleView.positionAtCursorOffset(Editor.java:4676)
                  at android.widget.Editor$SelectionHandleView.positionAtCursorOffset(Editor.java:5466)
                  at android.widget.Editor$SelectionHandleView.positionAndAdjustForCrossingHandles(Editor.java:5528)
                  at android.widget.Editor$SelectionHandleView.updatePosition(Editor.java:5458)
                  at android.widget.Editor$HandleView.onTouchEvent(Editor.java:4989)
                  at android.widget.Editor$SelectionHandleView.onTouchEvent(Editor.java:5472)
                  at android.view.View.dispatchTouchEvent(View.java:12545)
                  at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3083)
                  at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2741)
                  at android.widget.PopupWindow$PopupDecorView.dispatchTouchEvent(PopupWindow.java:2407)
                  at android.view.View.dispatchPointerEvent(View.java:12789)
             */
            return false;

        if (ex instanceof IndexOutOfBoundsException) {
            for (StackTraceElement ste : stack)
                if ("android.widget.NumberPicker$SetSelectionCommand".equals(ste.getClassName()) &&
                        "run".equals(ste.getMethodName()))
                    return false;
            /*
                java.lang.IndexOutOfBoundsException: setSpan (2 ... 2) ends beyond length 0
                  at android.text.SpannableStringBuilder.checkRange(SpannableStringBuilder.java:1265)
                  at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:684)
                  at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:677)
                  at android.text.Selection.setSelection(Selection.java:76)
                  at android.widget.TextView.semSetSelection(TextView.java:11550)
                  at android.widget.EditText.setSelection(EditText.java:118)
                  at android.widget.NumberPicker$SetSelectionCommand.run(NumberPicker.java:2246)
                  at android.os.Handler.handleCallback(Handler.java:751)
             */
        }

        if (ex instanceof IndexOutOfBoundsException) {
            for (StackTraceElement ste : stack)
                if ("android.graphics.Paint".equals(ste.getClassName()) &&
                        "getTextRunCursor".equals(ste.getMethodName()))
                    return false;
            /*
                Android 6.0.1
                java.lang.IndexOutOfBoundsException
                  at android.graphics.Paint.getTextRunCursor(Paint.java:2160)
                  at android.graphics.Paint.getTextRunCursor(Paint.java:2112)
                  at android.widget.Editor.getNextCursorOffset(Editor.java:924)
                  at android.widget.Editor.access$4700(Editor.java:126)
                  at android.widget.Editor$SelectionEndHandleView.positionAndAdjustForCrossingHandles(Editor.java:4708)
                  at android.widget.Editor$SelectionEndHandleView.updatePosition(Editor.java:4692)
                  at android.widget.Editor$HandleView.onTouchEvent(Editor.java:4012)
                  at android.widget.Editor$SelectionEndHandleView.onTouchEvent(Editor.java:4726)
                  at android.view.View.dispatchTouchEvent(View.java:9377)
                  at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:2554)
                  at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2255)
                  at android.widget.PopupWindow$PopupDecorView.dispatchTouchEvent(PopupWindow.java:2015)
                  at android.view.View.dispatchPointerEvent(View.java:9597)
                  at android.view.ViewRootImpl$ViewPostImeInputStage.processPointerEvent(ViewRootImpl.java:4234)
                  at android.view.ViewRootImpl$ViewPostImeInputStage.onProcess(ViewRootImpl.java:4100)
                  at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:3646)
                  at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:3699)
                  at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:3665)
                  at android.view.ViewRootImpl$AsyncInputStage.forward(ViewRootImpl.java:3791)
                  at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:3673)
                  at android.view.ViewRootImpl$AsyncInputStage.apply(ViewRootImpl.java:3848)
                  at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:3646)
                  at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:3699)
                  at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:3665)
                  at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:3673)
                  at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:3646)
                  at android.view.ViewRootImpl.deliverInputEvent(ViewRootImpl.java:5926)
                  at android.view.ViewRootImpl.doProcessInputEvents(ViewRootImpl.java:5900)
                  at android.view.ViewRootImpl.enqueueInputEvent(ViewRootImpl.java:5861)
                  at android.view.ViewRootImpl$WindowInputEventReceiver.onInputEvent(ViewRootImpl.java:6029)
                  at android.view.InputEventReceiver.dispatchInputEvent(InputEventReceiver.java:185)
                  at android.view.InputEventReceiver.nativeConsumeBatchedInputEvents(Native Method)
                  at android.view.InputEventReceiver.consumeBatchedInputEvents(InputEventReceiver.java:176)
                  at android.view.ViewRootImpl.doConsumeBatchedInput(ViewRootImpl.java:6000)
                  at android.view.ViewRootImpl$ConsumeBatchedInputRunnable.run(ViewRootImpl.java:6052)
                  at android.view.Choreographer$CallbackRecord.run(Choreographer.java:858)
                  at android.view.Choreographer.doCallbacks(Choreographer.java:670)
                  at android.view.Choreographer.doFrame(Choreographer.java:600)
                  at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:844)
                  at android.os.Handler.handleCallback(Handler.java:739)
             */
        }

        if (ex instanceof StringIndexOutOfBoundsException) {
            for (StackTraceElement ste : stack)
                if ("android.widget.Editor$SuggestionsPopupWindow".equals(ste.getClassName()) &&
                        "highlightTextDifferences".equals(ste.getMethodName()))
                    return false;
            /*
                Android 7.0 Samsung
                java.lang.StringIndexOutOfBoundsException: length=175; regionStart=174; regionLength=7
                  at java.lang.String.substring(String.java:1931)
                  at android.widget.Editor$SuggestionsPopupWindow.highlightTextDifferences(Editor.java:4002)
                  at android.widget.Editor$SuggestionsPopupWindow.updateSuggestions(Editor.java:3933)
                  at android.widget.Editor$SuggestionsPopupWindow.show(Editor.java:3836)
                  at android.widget.Editor.replace(Editor.java:428)
                  at android.widget.Editor$3.run(Editor.java:2362)
                  at android.os.Handler.handleCallback(Handler.java:751)
                  at android.os.Handler.dispatchMessage(Handler.java:95)
                  at android.os.Looper.loop(Looper.java:154)
                  at android.app.ActivityThread.main(ActivityThread.java:6780)
                  at java.lang.reflect.Method.invoke(Native Method)
                  at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1500)
                  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1390)
               */
        }

        if (ex instanceof IllegalArgumentException &&
                stack.length > 0 &&
                "android.text.method.WordIterator".equals(stack[0].getClassName()) &&
                "checkOffsetIsValid".equals(stack[0].getMethodName()))
            /*
                https://issuetracker.google.com/issues/37068143
                https://android.googlesource.com/platform/frameworks/base/+/refs/heads/marshmallow-release/core/java/android/text/method/WordIterator.java
                java.lang.IllegalArgumentException: Invalid offset: -1. Valid range is [0, 1673]
                at android.text.method.WordIterator.checkOffsetIsValid(WordIterator.java:380)
                at android.text.method.WordIterator.isBoundary(WordIterator.java:101)
                at android.widget.Editor$SelectionStartHandleView.positionAtCursorOffset(Editor.java:4287)
                at android.widget.Editor$HandleView.updatePosition(Editor.java:3735)
                at android.widget.Editor$PositionListener.onPreDraw(Editor.java:2512)
                at android.view.ViewTreeObserver.dispatchOnPreDraw(ViewTreeObserver.java:944)
                at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:2412)
                at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:1321)
                at android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:6763)
                at android.view.Choreographer$CallbackRecord.run(Choreographer.java:894)
                at android.view.Choreographer.doCallbacks(Choreographer.java:696)
                at android.view.Choreographer.doFrame(Choreographer.java:631)
                at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:880)
                at android.os.Handler.handleCallback(Handler.java:815)
             */
            return false;

        if (ex instanceof IllegalArgumentException && ex.getCause() != null) {
            for (StackTraceElement ste : ex.getCause().getStackTrace())
                if ("android.view.textclassifier.TextClassifierImpl".equals(ste.getClassName()) &&
                        "validateInput".equals(ste.getMethodName()))
                    return true;
            /*
                java.lang.RuntimeException: An error occurred while executing doInBackground()
                        at android.os.AsyncTask$3.done(AsyncTask.java:353)
                        at java.util.concurrent.FutureTask.finishCompletion(FutureTask.java:383)
                        at java.util.concurrent.FutureTask.setException(FutureTask.java:252)
                        at java.util.concurrent.FutureTask.run(FutureTask.java:271)
                        at android.os.AsyncTask$SerialExecutor$1.run(AsyncTask.java:245)
                        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1162)
                        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:636)
                        at java.lang.Thread.run(Thread.java:764)
                Caused by: java.lang.IllegalArgumentException
                at com.android.internal.util.Preconditions.checkArgument(Preconditions.java:33)
                        at android.view.textclassifier.TextClassifierImpl.validateInput(TextClassifierImpl.java:484)
                        at android.view.textclassifier.TextClassifierImpl.classifyText(TextClassifierImpl.java:144)
                        at android.widget.SelectionActionModeHelper$TextClassificationHelper.classifyText(SelectionActionModeHelper.java:465)
                        at android.widget.SelectionActionModeHelper.-android_widget_SelectionActionModeHelper-mthref-1(SelectionActionModeHelper.java:83)
                        at android.widget.-$Lambda$tTszxdFZ0V9nXhnBpPsqeBMO0fw$5.$m$0(Unknown:4)
                        at android.widget.-$Lambda$tTszxdFZ0V9nXhnBpPsqeBMO0fw$5.get(Unknown)
                        at android.widget.SelectionActionModeHelper$TextClassificationAsyncTask.doInBackground(SelectionActionModeHelper.java:366)
                        at android.widget.SelectionActionModeHelper$TextClassificationAsyncTask.doInBackground(SelectionActionModeHelper.java:361)
                        at android.os.AsyncTask$2.call(AsyncTask.java:333)
                        at java.util.concurrent.FutureTask.run(FutureTask.java:266)
                        at android.os.AsyncTask$SerialExecutor$1.run(AsyncTask.java:245)
                        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1162)
                        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:636)
                        at java.lang.Thread.run(Thread.java:764)
             */
        }

        if (ex instanceof NullPointerException &&
                stack.length > 0 &&
                "view.AccessibilityInteractionController".equals(stack[0].getClassName()) &&
                "applyAppScaleAndMagnificationSpecIfNeeded".equals(stack[0].getMethodName()))
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'void android.graphics.RectF.scale(float)' on a null object reference
                  at android.view.AccessibilityInteractionController.applyAppScaleAndMagnificationSpecIfNeeded(AccessibilityInteractionController.java:872)
                  at android.view.AccessibilityInteractionController.applyAppScaleAndMagnificationSpecIfNeeded(AccessibilityInteractionController.java:796)
                  at android.view.AccessibilityInteractionController.updateInfosForViewportAndReturnFindNodeResult(AccessibilityInteractionController.java:924)
                  at android.view.AccessibilityInteractionController.findAccessibilityNodeInfoByAccessibilityIdUiThread(AccessibilityInteractionController.java:345)
                  at android.view.AccessibilityInteractionController.access$400(AccessibilityInteractionController.java:75)
                  at android.view.AccessibilityInteractionController$PrivateHandler.handleMessage(AccessibilityInteractionController.java:1393)
                  at android.os.Handler.dispatchMessage(Handler.java:107)
             */
            return false;

        if (ex instanceof NullPointerException) {
            for (StackTraceElement ste : stack)
                if ("android.app.job.IJobCallback$Stub$Proxy".equals(ste.getClassName()) &&
                        "jobFinished".equals(ste.getMethodName()))
                    return false;
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'int com.android.server.job.controllers.JobStatus.getUid()' on a null object reference
                  at android.os.Parcel.readException(Parcel.java:1605)
                  at android.os.Parcel.readException(Parcel.java:1552)
                  at android.app.job.IJobCallback$Stub$Proxy.jobFinished(IJobCallback.java:167)
                  at android.app.job.JobService$JobHandler.handleMessage(JobService.java:147)
                  at android.os.Handler.dispatchMessage(Handler.java:111)
                  at android.os.Looper.loop(Looper.java:207)
                  at android.app.ActivityThread.main(ActivityThread.java:5697)
                  at java.lang.reflect.Method.invoke(Native Method)
                  at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:905)
                  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:766)
             */
        }

        if (ex instanceof IllegalStateException &&
                stack.length > 0 &&
                "android.database.sqlite.SQLiteSession".equals(stack[0].getClassName()) &&
                "throwIfNoTransaction".equals(stack[0].getMethodName()))
            /*
                java.lang.IllegalStateException: Cannot perform this operation because there is no current transaction.
                  at android.database.sqlite.SQLiteSession.throwIfNoTransaction(SQLiteSession.java:917)
                  at android.database.sqlite.SQLiteSession.endTransaction(SQLiteSession.java:400)
                  at android.database.sqlite.SQLiteDatabase.endTransaction(SQLiteDatabase.java:585)
                  at androidx.sqlite.db.framework.FrameworkSQLiteDatabase.endTransaction(SourceFile:1)
                  at androidx.room.RoomDatabase.endTransaction(SourceFile:1)
                  at androidx.work.impl.WorkerWrapper.runWorker(SourceFile:66)
                  at androidx.work.impl.WorkerWrapper.run(SourceFile:3)
                  at androidx.work.impl.utils.SerialExecutor$Task.run(SourceFile:1)
             */
            return false;

        if (ex instanceof IllegalArgumentException &&
                stack.length > 0 &&
                "android.widget.SmartSelectSprite".equals(stack[0].getClassName()) &&
                "startAnimation".equals(stack[0].getMethodName()))
            /*
                java.lang.IllegalArgumentException: Center point is not inside any of the rectangles!
                  at android.widget.SmartSelectSprite.startAnimation(SmartSelectSprite.java:392)
                  at android.widget.SelectionActionModeHelper.startSelectionActionModeWithSmartSelectAnimation(SelectionActionModeHelper.java:319)
                  at android.widget.SelectionActionModeHelper.lambda$l1f1_V5lw6noQxI_3u11qF753Iw(Unknown Source:0)
                  at android.widget.-$$Lambda$SelectionActionModeHelper$l1f1_V5lw6noQxI_3u11qF753Iw.accept(Unknown Source:4)
                  at android.widget.SelectionActionModeHelper$TextClassificationAsyncTask.onPostExecute(SelectionActionModeHelper.java:910)
                  at android.widget.SelectionActionModeHelper$TextClassificationAsyncTask.onPostExecute(SelectionActionModeHelper.java:864)
                  at android.os.AsyncTask.finish(AsyncTask.java:695)
                  at android.os.AsyncTask.access$600(AsyncTask.java:180)
                  at android.os.AsyncTask$InternalHandler.handleMessage(AsyncTask.java:712)
                  at android.os.Handler.dispatchMessage(Handler.java:106)
             */
            return false;

        if (ex instanceof InflateException)
            /*
                android.view.InflateException: Binary XML file line #7: Binary XML file line #7: Error inflating class <unknown>
                Caused by: android.view.InflateException: Binary XML file line #7: Error inflating class <unknown>
                Caused by: java.lang.reflect.InvocationTargetException
                  at java.lang.reflect.Constructor.newInstance0(Native Method)
                  at java.lang.reflect.Constructor.newInstance(Constructor.java:343)
                  at android.view.LayoutInflater.createView(LayoutInflater.java:686)
                  at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:829)
                  at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:769)
                  at android.view.LayoutInflater.rInflate(LayoutInflater.java:902)
                  at android.view.LayoutInflater.rInflateChildren(LayoutInflater.java:863)
                  at android.view.LayoutInflater.inflate(LayoutInflater.java:554)
                  at android.view.LayoutInflater.inflate(LayoutInflater.java:461)
             */
            return false;

        for (StackTraceElement ste : stack) {
            String clazz = ste.getClassName();
            if (clazz != null && clazz.startsWith("org.chromium.net."))
                return false;
        }

        if (ex instanceof SecurityException &&
                ex.getMessage() != null &&
                ex.getMessage().contains("com.opera.browser"))
            /*
                java.lang.SecurityException: Permission Denial: starting Intent { act=android.intent.action.VIEW dat=https://tracking.dpd.de/... cmp=com.opera.browser/.leanplum.LeanplumCatchActivity (has extras) } from ProcessRecord{3d9efb1 6332:eu.faircode.email/u0a54} (pid=6332, uid=10054) not exported from uid 10113
                  at android.os.Parcel.readException(Parcel.java:1951)
                  at android.os.Parcel.readException(Parcel.java:1897)
                  at android.app.IActivityManager$Stub$Proxy.startActivity(IActivityManager.java:4430)
                  at android.app.Instrumentation.execStartActivity(Instrumentation.java:1610)
                  at android.app.ContextImpl.startActivity(ContextImpl.java:862)
                  at android.app.ContextImpl.startActivity(ContextImpl.java:839)
                  at android.view.textclassifier.TextClassification.lambda$-android_view_textclassifier_TextClassification_5020(TextClassification.java:166)
                  at android.view.textclassifier.-$Lambda$mxr44OLodDKdoE5ddAZvMdsFssQ.$m$0(Unknown Source:8)
                  at android.view.textclassifier.-$Lambda$mxr44OLodDKdoE5ddAZvMdsFssQ.onClick(Unknown Source:0)
                  at org.chromium.content.browser.selection.SelectionPopupControllerImpl.m(chromium-SystemWebViewGoogle.aab-stable-432415203:17)
                  at y5.onActionItemClicked(chromium-SystemWebViewGoogle.aab-stable-432415203:20)
                  at Bn.onActionItemClicked(chromium-SystemWebViewGoogle.aab-stable-432415203:1)
                  at com.android.internal.policy.DecorView$ActionModeCallback2Wrapper.onActionItemClicked(DecorView.java:2472)
                  at com.android.internal.view.FloatingActionMode$3.onMenuItemSelected(FloatingActionMode.java:101)
                  at com.android.internal.view.menu.MenuBuilder.dispatchMenuItemSelected(MenuBuilder.java:761)
                  at com.android.internal.view.menu.MenuItemImpl.invoke(MenuItemImpl.java:167)
                  at com.android.internal.view.menu.MenuBuilder.performItemAction(MenuBuilder.java:908)
                  at com.android.internal.view.menu.MenuBuilder.performItemAction(MenuBuilder.java:898)
                  at com.android.internal.view.FloatingActionMode.lambda$-com_android_internal_view_FloatingActionMode_5176(FloatingActionMode.java:129)
                  at com.android.internal.view.-$Lambda$IoKM3AcgDw3Ok5aFi0zlym2p3IA.$m$0(Unknown Source:4)
                  at com.android.internal.view.-$Lambda$IoKM3AcgDw3Ok5aFi0zlym2p3IA.onMenuItemClick(Unknown Source:0)
                  at com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$2.onClick(FloatingToolbar.java:423)
                  at android.view.View.performClick(View.java:6320)
                  at android.view.View$PerformClick.run(View.java:25087)
             */
            return false;

        if (isDead(ex))
            return false;

        if (BuildConfig.BETA_RELEASE)
            return true;

        while (ex != null) {
            for (StackTraceElement ste : stack)
                if (ste.getClassName().startsWith(BuildConfig.APPLICATION_ID))
                    return true;
            ex = ex.getCause();
        }

        return false;
    }

    private static boolean isDead(Throwable ex) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /*
                java.lang.RuntimeException: Failure from system
                  at android.app.ContextImpl.bindServiceCommon(ContextImpl.java:1327)
                  at android.app.ContextImpl.bindService(ContextImpl.java:1286)
                  at android.content.ContextWrapper.bindService(ContextWrapper.java:604)
                  at android.content.ContextWrapper.bindService(ContextWrapper.java:604)
                  at hq.run(PG:15)
                  at java.lang.Thread.run(Thread.java:818)
                Caused by: android.os.DeadObjectException
                  at android.os.BinderProxy.transactNative(Native Method)
                  at android.os.BinderProxy.transact(Binder.java:503)
                  at android.app.ActivityManagerProxy.bindService(ActivityManagerNative.java:3783)
                  at android.app.ContextImpl.bindServiceCommon(ContextImpl.java:1317)
             */
            Throwable cause = ex;
            while (cause != null) {
                if (cause instanceof DeadObjectException)
                    return true;
                cause = cause.getCause();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Throwable cause = ex;
            while (cause != null) {
                if (cause instanceof DeadSystemException)
                    return true;
                cause = cause.getCause();
            }
        }

        return false;
    }

    static String formatThrowable(Throwable ex) {
        return formatThrowable(ex, true);
    }

    static String formatThrowable(Throwable ex, boolean sanitize) {
        return formatThrowable(ex, " ", sanitize);
    }

    static String formatThrowable(Throwable ex, String separator, boolean sanitize) {
        if (ex == null)
            return null;

        if (sanitize) {
            if (ex instanceof MessageRemovedException)
                return null;

            if (ex instanceof AuthenticationFailedException &&
                    ex.getCause() instanceof SocketException)
                return null;

            if (ex instanceof ProtocolException &&
                    ex.getCause() instanceof InterruptedException)
                return null; // Interrupted waitIfIdle

            if (ex instanceof MessagingException &&
                    ("Not connected".equals(ex.getMessage()) || // POP3
                            "connection failure".equals(ex.getMessage()) ||
                            "failed to create new store connection".equals(ex.getMessage())))
                return null;

            if (ex instanceof MessagingException &&
                    ex.getCause() instanceof ConnectionException &&
                    ex.getCause().getMessage() != null &&
                    (ex.getCause().getMessage().contains("Read error") ||
                            ex.getCause().getMessage().contains("Write error") ||
                            ex.getCause().getMessage().contains("Unexpected end of ZLIB input stream") ||
                            ex.getCause().getMessage().contains("Socket is closed")))
                return null;

            // javax.mail.MessagingException: AU3 BAD User is authenticated but not connected.;
            //   nested exception is:
            //  com.sun.mail.iap.BadCommandException: AU3 BAD User is authenticated but not connected.
            // javax.mail.MessagingException: AU3 BAD User is authenticated but not connected.;
            //   nested exception is:
            // 	com.sun.mail.iap.BadCommandException: AU3 BAD User is authenticated but not connected.
            // 	at com.sun.mail.imap.IMAPFolder.logoutAndThrow(SourceFile:1156)
            // 	at com.sun.mail.imap.IMAPFolder.open(SourceFile:1063)
            // 	at com.sun.mail.imap.IMAPFolder.open(SourceFile:977)
            // 	at eu.faircode.email.ServiceSynchronize.monitorAccount(SourceFile:890)
            // 	at eu.faircode.email.ServiceSynchronize.access$1500(SourceFile:85)
            // 	at eu.faircode.email.ServiceSynchronize$7$1.run(SourceFile:627)
            // 	at java.lang.Thread.run(Thread.java:764)
            // Caused by: com.sun.mail.iap.BadCommandException: AU3 BAD User is authenticated but not connected.
            // 	at com.sun.mail.iap.Protocol.handleResult(SourceFile:415)
            // 	at com.sun.mail.imap.protocol.IMAPProtocol.select(SourceFile:1230)
            // 	at com.sun.mail.imap.IMAPFolder.open(SourceFile:1034)

            if (ex instanceof MessagingException &&
                    ex.getCause() instanceof BadCommandException &&
                    ex.getCause().getMessage() != null &&
                    ex.getCause().getMessage().contains("User is authenticated but not connected"))
                return null;

            if (ex instanceof IOException &&
                    ex.getCause() instanceof MessageRemovedException)
                return null;

            if (ex instanceof ConnectionException)
                return null;

            if (ex instanceof StoreClosedException ||
                    ex instanceof FolderClosedException ||
                    ex instanceof FolderClosedIOException ||
                    ex instanceof OperationCanceledException)
                return null;

            if (ex instanceof IllegalStateException &&
                    (TOKEN_REFRESH_REQUIRED.equals(ex.getMessage()) ||
                            "Not connected".equals(ex.getMessage()) ||
                            "This operation is not allowed on a closed folder".equals(ex.getMessage())))
                return null;
        }

        if (ex instanceof MailConnectException &&
                ex.getCause() instanceof SocketTimeoutException)
            ex = new Throwable("No response received from email server", ex);

        if (ex instanceof MessagingException &&
                ex.getCause() instanceof UnknownHostException)
            ex = new Throwable("Email server address lookup failed", ex);

        StringBuilder sb = new StringBuilder();
        if (BuildConfig.DEBUG)
            sb.append(ex.toString());
        else
            sb.append(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());

        Throwable cause = ex.getCause();
        while (cause != null) {
            if (BuildConfig.DEBUG)
                sb.append(separator).append(cause.toString());
            else
                sb.append(separator).append(cause.getMessage() == null ? cause.getClass().getName() : cause.getMessage());
            cause = cause.getCause();
        }

        return sb.toString();
    }

    static void writeCrashLog(Context context, Throwable ex) {
        File file = new File(context.getFilesDir(), "crash.log");
        Log.w("Writing exception to " + file);

        try (FileWriter out = new FileWriter(file, true)) {
            out.write(BuildConfig.VERSION_NAME + BuildConfig.REVISION + " " + new Date() + "\r\n");
            out.write(ex + "\r\n" + android.util.Log.getStackTraceString(ex) + "\r\n");
        } catch (IOException e) {
            Log.e(e);
        }
    }

    static EntityMessage getDebugInfo(Context context, int title, Throwable ex, String log) throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(title)).append("\n\n\n\n");
        sb.append(getAppInfo(context));
        if (ex != null)
            sb.append(ex.toString()).append("\n").append(android.util.Log.getStackTraceString(ex));
        if (log != null)
            sb.append(log);
        String body = "<pre class=\"fairemail_debug_info\">" + TextUtils.htmlEncode(sb.toString()) + "</pre>";

        EntityMessage draft;

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            List<TupleIdentityEx> identities = db.identity().getComposableIdentities(null);
            if (identities == null || identities.size() == 0)
                throw new IllegalArgumentException(context.getString(R.string.title_no_composable));

            EntityIdentity identity = identities.get(0);
            EntityFolder drafts = db.folder().getFolderByType(identity.account, EntityFolder.DRAFTS);
            if (drafts == null)
                throw new IllegalArgumentException(context.getString(R.string.title_no_drafts));

            draft = new EntityMessage();
            draft.account = drafts.account;
            draft.folder = drafts.id;
            draft.identity = identity.id;
            draft.msgid = EntityMessage.generateMessageId();
            draft.thread = draft.msgid;
            draft.to = new Address[]{myAddress()};
            draft.subject = context.getString(R.string.app_name) + " " +
                    BuildConfig.VERSION_NAME + BuildConfig.REVISION + " debug info";
            draft.received = new Date().getTime();
            draft.seen = true;
            draft.ui_seen = true;
            draft.id = db.message().insertMessage(draft);

            File file = draft.getFile(context);
            Helper.writeText(file, body);
            db.message().setMessageContent(draft.id, true, null, 0, null, null);

            attachSettings(context, draft.id, 1);
            attachAccounts(context, draft.id, 2);
            attachNetworkInfo(context, draft.id, 3);
            attachLog(context, draft.id, 4);
            attachOperations(context, draft.id, 5);
            attachTasks(context, draft.id, 6);
            attachLogcat(context, draft.id, 7);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                attachNotificationInfo(context, draft.id, 8);
            attachEnvironment(context, draft.id, 9);
            //if (MessageClassifier.isEnabled(context))
            //    attachClassifierData(context, draft.id, 10);

            EntityOperation.queue(context, draft, EntityOperation.ADD);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        ServiceSynchronize.eval(context, "debuginfo");

        return draft;
    }

    static void unexpectedError(FragmentManager manager, Throwable ex) {
        unexpectedError(manager, ex, true);
    }

    static void unexpectedError(FragmentManager manager, Throwable ex, boolean report) {
        Log.e(ex);

        if (ex instanceof OutOfMemoryError)
            report = false;

        Bundle args = new Bundle();
        args.putSerializable("ex", ex);
        args.putBoolean("report", report);

        FragmentDialogUnexpected fragment = new FragmentDialogUnexpected();
        fragment.setArguments(args);
        fragment.show(manager, "error:unexpected");
    }

    public static class FragmentDialogUnexpected extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Throwable ex = (Throwable) getArguments().getSerializable("ex");
            boolean report = getArguments().getBoolean("report", true);

            final Context context = getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View dview = inflater.inflate(R.layout.dialog_unexpected, null);
            TextView tvError = dview.findViewById(R.id.tvError);

            String message = Log.formatThrowable(ex, false);
            tvError.setText(message);

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setView(dview)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.menu_faq, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Uri uri = Helper.getSupportUri(context);
                            if (!TextUtils.isEmpty(message))
                                uri = uri
                                        .buildUpon()
                                        .appendQueryParameter("message", "Unexpected: " + message)
                                        .build();
                            Helper.view(context, uri, true);
                        }
                    });

            if (report)
                builder.setNeutralButton(R.string.title_report, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dialog will be dismissed
                        final Context context = getContext();

                        new SimpleTask<Long>() {
                            @Override
                            protected Long onExecute(Context context, Bundle args) throws Throwable {
                                return Log.getDebugInfo(context, R.string.title_unexpected_info_remark, ex, null).id;
                            }

                            @Override
                            protected void onExecuted(Bundle args, Long id) {
                                context.startActivity(new Intent(context, ActivityCompose.class)
                                        .putExtra("action", "edit")
                                        .putExtra("id", id));
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                if (ex instanceof IllegalArgumentException)
                                    ToastEx.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                                else
                                    ToastEx.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
                            }
                        }.execute(getContext(), getActivity(), new Bundle(), "error:unexpected");
                    }
                });

            return builder.create();
        }
    }

    private static StringBuilder getAppInfo(Context context) {
        StringBuilder sb = new StringBuilder();

        ContentResolver resolver = context.getContentResolver();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        PackageManager pm = context.getPackageManager();
        String installer = pm.getInstallerPackageName(BuildConfig.APPLICATION_ID);

        int targetSdk = -1;
        try {
            ApplicationInfo ai = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
            targetSdk = ai.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException ex) {
            sb.append(ex).append("\r\n");
        }

        // Get version info
        sb.append(String.format("%s %s/%d%s%s%s%s\r\n",
                context.getString(R.string.app_name),
                BuildConfig.VERSION_NAME + BuildConfig.REVISION,
                Helper.hasValidFingerprint(context) ? 1 : 3,
                BuildConfig.PLAY_STORE_RELEASE ? "p" : "",
                Helper.hasPlayStore(context) ? "s" : "",
                BuildConfig.DEBUG ? "d" : "",
                ActivityBilling.isPro(context) ? "+" : "-"));
        sb.append(String.format("Package: %s\r\n", BuildConfig.APPLICATION_ID));
        sb.append(String.format("Android: %s (SDK %d/%d)\r\n",
                Build.VERSION.RELEASE, Build.VERSION.SDK_INT, targetSdk));

        boolean reporting = prefs.getBoolean("crash_reports", false);
        if (reporting || BuildConfig.TEST_RELEASE) {
            String uuid = prefs.getString("uuid", null);
            sb.append(String.format("UUID: %s\r\n", uuid == null ? "-" : uuid));
        }

        sb.append(String.format("Installer: %s\r\n", installer));
        sb.append(String.format("Installed: %s\r\n", new Date(Helper.getInstallTime(context))));
        sb.append(String.format("Now: %s\r\n", new Date()));

        sb.append("\r\n");

        String osVersion = null;
        try {
            osVersion = System.getProperty("os.version");
        } catch (Throwable ex) {
            Log.e(ex);
        }

        // Get device info
        sb.append(String.format("Brand: %s\r\n", Build.BRAND));
        sb.append(String.format("Manufacturer: %s\r\n", Build.MANUFACTURER));
        sb.append(String.format("Model: %s\r\n", Build.MODEL));
        sb.append(String.format("Product: %s\r\n", Build.PRODUCT));
        sb.append(String.format("Device: %s\r\n", Build.DEVICE));
        sb.append(String.format("Host: %s\r\n", Build.HOST));
        sb.append(String.format("Time: %s\r\n", new Date(Build.TIME).toString()));
        sb.append(String.format("Display: %s\r\n", Build.DISPLAY));
        sb.append(String.format("Id: %s\r\n", Build.ID));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            sb.append(String.format("SoC: %s/%s\r\n", Build.SOC_MANUFACTURER, Build.SOC_MODEL));
        sb.append(String.format("OS version: %s\r\n", osVersion));
        sb.append(String.format("uid: %d\r\n", android.os.Process.myUid()));
        sb.append("\r\n");

        int[] contacts = ContactInfo.getStats();
        sb.append(String.format("Contact lookup: %d cached: %d\r\n",
                contacts[0], contacts[1]));

        Locale slocale = Resources.getSystem().getConfiguration().locale;
        String language = prefs.getString("language", null);
        sb.append(String.format("Locale: def=%s sys=%s lang=%s\r\n",
                Locale.getDefault(), slocale, language));

        String charset = MimeUtility.getDefaultJavaCharset();
        sb.append(String.format("Default charset: %s/%s\r\n", charset, MimeUtility.mimeCharset(charset)));

        sb.append("Transliterate: ")
                .append(TextHelper.canTransliterate())
                .append("\r\n");

        sb.append("\r\n");

        sb.append(String.format("Processors: %d\r\n", Runtime.getRuntime().availableProcessors()));

        ActivityManager am = Helper.getSystemService(context, ActivityManager.class);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        sb.append(String.format("Memory class: %d/%d MB Total: %s\r\n",
                am.getMemoryClass(), am.getLargeMemoryClass(), Helper.humanReadableByteCount(mi.totalMem)));

        long storage_available = Helper.getAvailableStorageSpace();
        long storage_total = Helper.getTotalStorageSpace();
        long storage_used = Helper.getSizeUsed(context.getFilesDir());
        sb.append(String.format("Storage space: %s/%s App: %s\r\n",
                Helper.humanReadableByteCount(storage_total - storage_available),
                Helper.humanReadableByteCount(storage_total),
                Helper.humanReadableByteCount(storage_used)));

        long cache_used = Helper.getSizeUsed(context.getCacheDir());
        long cache_quota = Helper.getCacheQuota(context);
        sb.append(String.format("Cache space: %s/%s\r\n",
                Helper.humanReadableByteCount(cache_used),
                Helper.humanReadableByteCount(cache_quota)));

        Runtime rt = Runtime.getRuntime();
        long hused = (rt.totalMemory() - rt.freeMemory()) / 1024L / 1024L;
        long hmax = rt.maxMemory() / 1024L / 1024L;
        long nheap = Debug.getNativeHeapAllocatedSize() / 1024L / 1024L;
        long nsize = Debug.getNativeHeapSize() / 1024 / 1024L;
        sb.append(String.format("Heap usage: %d/%d MiB native: %d/%d MiB\r\n", hused, hmax, nheap, nsize));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int ipc = IBinder.getSuggestedMaxIpcSizeBytes();
            sb.append(String.format("IPC max: %s\r\n", Helper.humanReadableByteCount(ipc)));
        }

        sb.append("\r\n");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            try {
                for (FileStore store : FileSystems.getDefault().getFileStores())
                    if (!store.isReadOnly() &&
                            store.getUsableSpace() != 0 &&
                            !"tmpfs".equals(store.type())) {
                        long total = store.getTotalSpace();
                        long unalloc = store.getUnallocatedSpace();
                        sb.append(String.format("%s %s %s/%s\r\n",
                                store,
                                store.type(),
                                Helper.humanReadableByteCount(total - unalloc),
                                Helper.humanReadableByteCount(total)));
                    }
                sb.append("\r\n");
            } catch (IOException ex) {
                sb.append(ex).append("\r\n");
            }

        WindowManager wm = Helper.getSystemService(context, WindowManager.class);
        Display display = wm.getDefaultDisplay();
        Point dim = new Point();
        display.getSize(dim);
        float density = context.getResources().getDisplayMetrics().density;
        sb.append(String.format("Density 1dp=%f\r\n", density));
        sb.append(String.format("Resolution: %.2f x %.2f dp\r\n", dim.x / density, dim.y / density));

        Configuration config = context.getResources().getConfiguration();

        String size;
        if (config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_XLARGE))
            size = "XLarge";
        else if (config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE))
            size = "Large";
        else if (config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL))
            size = "Medium";
        else if (config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_SMALL))
            size = "Small";
        else
            size = "size=" + (config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);

        String orientation;
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE)
            orientation = "Landscape";
        else if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
            orientation = "Portrait";
        else
            orientation = "orientation=" + config.orientation;

        sb.append(String.format("%s %s\r\n", size, orientation));

        try {
            float animation_scale = Settings.Global.getFloat(resolver,
                    Settings.Global.WINDOW_ANIMATION_SCALE, 0f);
            sb.append(String.format("Animation scale: %f %s\r\n", animation_scale,
                    animation_scale == 1f ? "" : "!!!"));
        } catch (Throwable ex) {
            sb.append(ex).append("\r\n");
        }

        int uiMode = context.getResources().getConfiguration().uiMode;
        sb.append(String.format("UI mode: 0x"))
                .append(Integer.toHexString(uiMode))
                .append(" night=").append(Helper.isNight(context))
                .append("\r\n");

        String uiType = Helper.getUiModeType(context);
        sb.append(String.format("UI type: %s %s\r\n", uiType,
                "normal".equals(uiType) ? "" : "!!!"));

        sb.append("\r\n");

        Boolean ignoring = Helper.isIgnoringOptimizations(context);
        sb.append(String.format("Battery optimizations: %s %s\r\n",
                ignoring == null ? null : Boolean.toString(!ignoring),
                Boolean.FALSE.equals(ignoring) ? "!!!" : ""));

        PowerManager power = Helper.getSystemService(context, PowerManager.class);
        boolean psaving = power.isPowerSaveMode();
        sb.append(String.format("Battery saving: %s %s\r\n", psaving, psaving ? "!!!" : ""));

        sb.append(String.format("Charging: %b; level: %d\r\n",
                Helper.isCharging(context), Helper.getBatteryLevel(context)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // https://developer.android.com/reference/android/app/usage/UsageStatsManager
            UsageStatsManager usm = Helper.getSystemService(context, UsageStatsManager.class);
            int bucket = usm.getAppStandbyBucket();
            boolean inactive = usm.isAppInactive(BuildConfig.APPLICATION_ID);
            sb.append(String.format("Standby bucket: %d-%b-%s %s\r\n",
                    bucket, inactive, Helper.getStandbyBucketName(bucket),
                    (bucket <= UsageStatsManager.STANDBY_BUCKET_ACTIVE && !inactive ? "" : "!!!")));
        }

        boolean canExact = AlarmManagerCompatEx.canScheduleExactAlarms(context);
        boolean hasExact = AlarmManagerCompatEx.hasExactAlarms(context);
        sb.append(String.format("ExactAlarms can=%b has=%b %s\r\n", canExact, hasExact,
                canExact ? "" : "!!!"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            boolean restricted = am.isBackgroundRestricted();
            sb.append(String.format("Background restricted: %b %s\r\n", restricted,
                    restricted ? "!!!" : ""));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            boolean saving = ConnectionHelper.isDataSaving(context);
            sb.append(String.format("Data saving: %b %s\r\n", saving,
                    saving ? "!!!" : ""));
        }

        try {
            int finish_activities = Settings.Global.getInt(resolver,
                    Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);
            sb.append(String.format("Always finish: %d %s\r\n", finish_activities,
                    finish_activities == 0 ? "" : "!!!"));
        } catch (Throwable ex) {
            sb.append(ex).append("\r\n");
        }

        sb.append("\r\n");

        return sb;
    }

    private static void attachSettings(Context context, long id, int sequence) {
        try {
            DB db = DB.getInstance(context);

            EntityAttachment attachment = new EntityAttachment();
            attachment.message = id;
            attachment.sequence = sequence;
            attachment.name = "settings.txt";
            attachment.type = "text/plain";
            attachment.disposition = Part.ATTACHMENT;
            attachment.size = null;
            attachment.progress = 0;
            attachment.id = db.attachment().insertAttachment(attachment);

            long size = 0;
            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                Map<String, ?> settings = prefs.getAll();
                List<String> keys = new ArrayList<>(settings.keySet());
                Collections.sort(keys);
                for (String key : keys) {
                    Object value = settings.get(key);
                    if ("wipe_mnemonic".equals(key) && value != null)
                        value = "[redacted]";
                    if (key != null && key.startsWith("oauth."))
                        value = "[redacted]";
                    size += write(os, key + "=" + value + "\r\n");
                }
            }

            db.attachment().setDownloaded(attachment.id, size);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static void attachAccounts(Context context, long id, int sequence) {
        try {
            DB db = DB.getInstance(context);

            EntityAttachment attachment = new EntityAttachment();
            attachment.message = id;
            attachment.sequence = sequence;
            attachment.name = "accounts.txt";
            attachment.type = "text/plain";
            attachment.disposition = Part.ATTACHMENT;
            attachment.size = null;
            attachment.progress = 0;
            attachment.id = db.attachment().insertAttachment(attachment);

            DateFormat dtf = Helper.getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

            long size = 0;
            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                List<EntityAccount> accounts = db.account().getAccounts();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean enabled = prefs.getBoolean("enabled", true);
                int pollInterval = ServiceSynchronize.getPollInterval(context);
                boolean metered = prefs.getBoolean("metered", true);
                Boolean ignoring = Helper.isIgnoringOptimizations(context);
                boolean canSchedule = AlarmManagerCompatEx.canScheduleExactAlarms(context);
                boolean auto_optimize = prefs.getBoolean("auto_optimize", false);
                boolean schedule = prefs.getBoolean("schedule", false);

                String ds = ConnectionHelper.getDataSaving(context);
                boolean vpn = ConnectionHelper.vpnActive(context);
                boolean ng = Helper.isInstalled(context, "eu.faircode.netguard");
                boolean tc = Helper.isInstalled(context, "net.kollnig.missioncontrol");

                size += write(os, "enabled=" + enabled + (enabled ? "" : " !!!") +
                        " interval=" + pollInterval + "\r\n" +
                        "metered=" + metered + (metered ? "" : " !!!") +
                        " restricted=" + ds + ("enabled".equals(ds) ? " !!!" : "") +
                        " vpn=" + vpn + (vpn ? " !!!" : "") +
                        " ng=" + ng + " tc=" + tc + "\r\n" +
                        "optimizing=" + (ignoring == null ? null : !ignoring) + (Boolean.FALSE.equals(ignoring) ? " !!!" : "") +
                        " canSchedule=" + canSchedule + (canSchedule ? "" : " !!!") +
                        " auto_optimize=" + auto_optimize + (auto_optimize ? " !!!" : "") + "\r\n" +
                        "accounts=" + accounts.size() +
                        " folders=" + db.folder().countTotal() +
                        " messages=" + db.message().countTotal() +
                        " rules=" + db.rule().countTotal() +
                        " ops=" + db.operation().getOperationCount() +
                        " outbox=" + db.message().countOutbox() +
                        "\r\n\r\n");

                if (schedule) {
                    int minuteStart = prefs.getInt("schedule_start", 0);
                    int minuteEnd = prefs.getInt("schedule_end", 0);

                    size += write(os, "schedule " +
                            (minuteStart / 60) + ":" + (minuteStart % 60) + "..." +
                            (minuteEnd / 60) + ":" + (minuteEnd % 60) + "\r\n");

                    String[] daynames = new DateFormatSymbols().getWeekdays();
                    for (int i = 0; i < 7; i++) {
                        boolean day = prefs.getBoolean("schedule_day" + i, true);
                        size += write(os, "schedule " + daynames[i + 1] + "=" + day + "\r\n");
                    }

                    size += write(os, "\r\n");
                }

                for (EntityAccount account : accounts) {
                    if (account.synchronize) {
                        int content = 0;
                        int messages = 0;
                        List<TupleFolderEx> folders = db.folder().getFoldersEx(account.id);
                        for (TupleFolderEx folder : folders) {
                            content += folder.content;
                            messages += folder.messages;
                        }

                        boolean unmetered = false;
                        boolean ignore_schedule = false;
                        try {
                            if (account.conditions != null) {
                                JSONObject jconditions = new JSONObject(account.conditions);
                                unmetered = jconditions.optBoolean("unmetered");
                                ignore_schedule = jconditions.optBoolean("ignore_schedule");
                            }
                        } catch (Throwable ignored) {
                        }

                        size += write(os, account.name + (account.primary ? "*" : "") +
                                " " + (account.protocol == EntityAccount.TYPE_IMAP ? "IMAP" : "POP") + "/" + account.auth_type +
                                " " + account.host + ":" + account.port + "/" + account.encryption +
                                " sync=" + account.synchronize +
                                " exempted=" + account.poll_exempted +
                                " poll=" + account.poll_interval +
                                " ondemand=" + account.ondemand +
                                " msgs=" + content + "/" + messages +
                                " ops=" + db.operation().getOperationCount(account.id) +
                                " ischedule=" + ignore_schedule + (ignore_schedule ? " !!!" : "") +
                                " unmetered=" + unmetered + (unmetered ? " !!!" : "") +
                                " " + account.state +
                                (account.last_connected == null ? "" : " " + dtf.format(account.last_connected)) +
                                (account.error == null ? "" : "\r\n" + account.error) +
                                "\r\n");

                        if (folders.size() > 0)
                            Collections.sort(folders, folders.get(0).getComparator(context));
                        for (TupleFolderEx folder : folders)
                            if (folder.synchronize) {
                                int unseen = db.message().countUnseen(folder.id);
                                int notifying = db.message().countNotifying(folder.id);
                                size += write(os, "- " + folder.name + " " + folder.type +
                                        (folder.unified ? " unified" : "") +
                                        (folder.notify ? " notify" : "") +
                                        " poll=" + folder.poll + "/" + folder.poll_factor +
                                        " days=" + folder.sync_days + "/" + folder.keep_days +
                                        " msgs=" + folder.content + "/" + folder.messages + "/" + folder.total +
                                        " ops=" + db.operation().getOperationCount(folder.id, null) +
                                        " unseen=" + unseen + " notifying=" + notifying +
                                        " " + folder.state +
                                        (folder.last_sync == null ? "" : " " + dtf.format(folder.last_sync)) +
                                        "\r\n");
                            }

                        List<TupleAccountSwipes> swipes = db.account().getAccountSwipes(account.id);
                        if (swipes == null)
                            size += write(os, "<> swipes?\r\n");
                        else
                            for (TupleAccountSwipes swipe : swipes) {
                                size += write(os, "> " + EntityMessage.getSwipeType(swipe.swipe_left) + " " +
                                        swipe.left_name + ":" + swipe.left_type + "\r\n");
                                size += write(os, "< " + EntityMessage.getSwipeType(swipe.swipe_right) + " " +
                                        swipe.right_name + ":" + swipe.right_type + "\r\n");
                            }

                        size += write(os, "\r\n");
                    }
                }

                for (EntityAccount account : accounts)
                    if (account.synchronize) {
                        List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                        for (EntityIdentity identity : identities)
                            if (identity.synchronize) {
                                size += write(os, account.name + "/" + identity.name + (identity.primary ? "*" : "") + " " +
                                        identity.display + " " + identity.email + " " +
                                        " " + identity.host + ":" + identity.port + "/" + identity.encryption +
                                        " ops=" + db.operation().getOperationCount(EntityOperation.SEND) +
                                        " " + identity.state +
                                        (identity.last_connected == null ? "" : " " + dtf.format(identity.last_connected)) +
                                        (identity.error == null ? "" : "\r\n" + identity.error) +
                                        "\r\n");
                            }
                    }

                size += write(os, "\r\n");

                for (EntityAccount account : accounts) {
                    int ops = db.operation().getOperationCount(account.id);
                    if (account.synchronize || ops > 0)
                        try {
                            JSONObject jaccount = account.toJSON();
                            jaccount.put("state", account.state == null ? "null" : account.state);
                            jaccount.put("warning", account.warning);
                            jaccount.put("operations", ops);
                            jaccount.put("error", account.error);
                            jaccount.put("capabilities", account.capabilities);

                            if (account.last_connected != null)
                                jaccount.put("last_connected", new Date(account.last_connected).toString());

                            jaccount.put("keep_alive_ok", account.keep_alive_ok);
                            jaccount.put("keep_alive_failed", account.keep_alive_failed);
                            jaccount.put("keep_alive_succeeded", account.keep_alive_succeeded);

                            jaccount.remove("password");

                            size += write(os, "==========\r\n");
                            size += write(os, jaccount.toString(2) + "\r\n");

                            List<EntityFolder> folders = db.folder().getFolders(account.id, false, false);
                            if (folders.size() > 0)
                                Collections.sort(folders, folders.get(0).getComparator(context));
                            for (EntityFolder folder : folders) {
                                JSONObject jfolder = folder.toJSON();
                                jfolder.put("level", folder.level);
                                jfolder.put("total", folder.total);
                                jfolder.put("initialize", folder.initialize);
                                jfolder.put("subscribed", folder.subscribed);
                                jfolder.put("state", folder.state == null ? "null" : folder.state);
                                jfolder.put("sync_state", folder.sync_state == null ? "null" : folder.sync_state);
                                jfolder.put("poll_count", folder.poll_count);
                                jfolder.put("read_only", folder.read_only);
                                jfolder.put("selectable", folder.selectable);
                                jfolder.put("inferiors", folder.inferiors);
                                jfolder.put("auto_add", folder.auto_add);
                                jfolder.put("operations", db.operation().getOperationCount(folder.id, null));
                                jfolder.put("error", folder.error);
                                if (folder.last_sync != null)
                                    jfolder.put("last_sync", new Date(folder.last_sync).toString());
                                if (folder.last_sync_count != null)
                                    jfolder.put("last_sync_count", folder.last_sync_count);
                                size += write(os, jfolder.toString(2) + "\r\n");
                            }

                            List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                            for (EntityIdentity identity : identities)
                                try {
                                    JSONObject jidentity = identity.toJSON();
                                    jidentity.remove("password");
                                    jidentity.remove("signature");
                                    size += write(os, "----------\r\n");
                                    size += write(os, jidentity.toString(2) + "\r\n");
                                } catch (JSONException ex) {
                                    size += write(os, ex.toString() + "\r\n");
                                }
                        } catch (JSONException ex) {
                            size += write(os, ex.toString() + "\r\n");
                        }
                }
            }

            db.attachment().setDownloaded(attachment.id, size);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static void attachNetworkInfo(Context context, long id, int sequence) {
        try {
            DB db = DB.getInstance(context);

            EntityAttachment attachment = new EntityAttachment();
            attachment.message = id;
            attachment.sequence = sequence;
            attachment.name = "network.txt";
            attachment.type = "text/plain";
            attachment.disposition = Part.ATTACHMENT;
            attachment.size = null;
            attachment.progress = 0;
            attachment.id = db.attachment().insertAttachment(attachment);

            long size = 0;
            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);

                NetworkInfo ani = cm.getActiveNetworkInfo();
                if (ani != null)
                    size += write(os, ani.toString() +
                            " connected=" + ani.isConnected() +
                            " metered=" + cm.isActiveNetworkMetered() +
                            " roaming=" + ani.isRoaming() +
                            " type=" + ani.getType() + "/" + ani.getTypeName() +
                            "\r\n\r\n");

                Network active = ConnectionHelper.getActiveNetwork(context);
                for (Network network : cm.getAllNetworks()) {
                    size += write(os, (network.equals(active) ? "active=" : "network=") + network + "\r\n");

                    NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                    size += write(os, " caps=" + caps + "\r\n");

                    LinkProperties props = cm.getLinkProperties(network);
                    size += write(os, " props=" + props + "\r\n");

                    size += write(os, "\r\n");
                }

                try {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces != null && interfaces.hasMoreElements()) {
                        NetworkInterface ni = interfaces.nextElement();
                        size += write(os, "Interface=" + ni + "\r\n");
                        for (InterfaceAddress iaddr : ni.getInterfaceAddresses()) {
                            InetAddress addr = iaddr.getAddress();
                            size += write(os, " addr=" + addr +
                                    (addr.isLoopbackAddress() ? " loopback" : "") +
                                    (addr.isSiteLocalAddress() ? " site local (LAN)" : "") +
                                    (addr.isLinkLocalAddress() ? " link local (device)" : "") +
                                    (addr.isAnyLocalAddress() ? " any local" : "") +
                                    (addr.isMulticastAddress() ? " multicast" : "") + "\r\n");
                        }
                        size += write(os, "\r\n");
                    }
                } catch (Throwable ex) {
                    size += write(os, ex.getMessage() + "\r\n");
                }

                ConnectionHelper.NetworkState state = ConnectionHelper.getNetworkState(context);
                size += write(os, "Connected=" + state.isConnected() + "\r\n");
                size += write(os, "Suitable=" + state.isSuitable() + "\r\n");
                size += write(os, "Unmetered=" + state.isUnmetered() + "\r\n");
                size += write(os, "Roaming=" + state.isRoaming() + "\r\n\r\n");

                size += write(os, "VPN active=" + ConnectionHelper.vpnActive(context) + "\r\n");
                size += write(os, "Data saving=" + ConnectionHelper.isDataSaving(context) + "\r\n");
                size += write(os, "Airplane=" + ConnectionHelper.airplaneMode(context) + "\r\n");

                size += write(os, "\r\n");
                size += write(os, getCiphers().toString());
            }

            db.attachment().setDownloaded(attachment.id, size);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static void attachLog(Context context, long id, int sequence) {
        try {
            DB db = DB.getInstance(context);

            EntityAttachment attachment = new EntityAttachment();
            attachment.message = id;
            attachment.sequence = sequence;
            attachment.name = "log.txt";
            attachment.type = "text/plain";
            attachment.disposition = Part.ATTACHMENT;
            attachment.size = null;
            attachment.progress = 0;
            attachment.id = db.attachment().insertAttachment(attachment);

            long size = 0;
            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                long from = new Date().getTime() - 24 * 3600 * 1000L;
                DateFormat TF = Helper.getTimeInstance(context);

                for (EntityLog entry : db.log().getLogs(from, null))
                    size += write(os, String.format("%s [%d:%d:%d:%d] %s\r\n",
                            TF.format(entry.time),
                            entry.type.ordinal(),
                            (entry.account == null ? 0 : entry.account),
                            (entry.folder == null ? 0 : entry.folder),
                            (entry.message == null ? 0 : entry.message),
                            entry.data));
            }

            db.attachment().setDownloaded(attachment.id, size);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static void attachOperations(Context context, long id, int sequence) {
        try {
            DB db = DB.getInstance(context);

            EntityAttachment attachment = new EntityAttachment();
            attachment.message = id;
            attachment.sequence = sequence;
            attachment.name = "operations.txt";
            attachment.type = "text/plain";
            attachment.disposition = Part.ATTACHMENT;
            attachment.size = null;
            attachment.progress = 0;
            attachment.id = db.attachment().insertAttachment(attachment);

            long size = 0;
            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                DateFormat TF = Helper.getTimeInstance(context);

                for (EntityOperation op : db.operation().getOperations()) {
                    EntityAccount account = (op.account == null ? null : db.account().getAccount(op.account));
                    EntityFolder folder = (op.folder == null ? null : db.folder().getFolder(op.folder));
                    size += write(os, String.format("%s %s/%s %d %s/%d %s %s %s\r\n",
                            TF.format(op.created),
                            account == null ? null : account.name,
                            folder == null ? null : folder.name,
                            op.message == null ? -1 : op.message,
                            op.name,
                            op.tries,
                            op.args,
                            op.state,
                            op.error));
                }
            }

            db.attachment().setDownloaded(attachment.id, size);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static void attachTasks(Context context, long id, int sequence) {
        try {
            DB db = DB.getInstance(context);

            EntityAttachment attachment = new EntityAttachment();
            attachment.message = id;
            attachment.sequence = sequence;
            attachment.name = "tasks.txt";
            attachment.type = "text/plain";
            attachment.disposition = Part.ATTACHMENT;
            attachment.size = null;
            attachment.progress = 0;
            attachment.id = db.attachment().insertAttachment(attachment);

            long size = 0;
            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                for (SimpleTask task : SimpleTask.getList())
                    size += write(os, String.format("%s\r\n", task.toString()));
                size += write(os, "\r\n");
                for (TwoStateOwner owner : TwoStateOwner.getList())
                    size += write(os, String.format("%s\r\n", owner.toString()));
            }

            db.attachment().setDownloaded(attachment.id, size);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static void attachLogcat(Context context, long id, int sequence) {
        try {
            DB db = DB.getInstance(context);

            EntityAttachment attachment = new EntityAttachment();
            attachment.message = id;
            attachment.sequence = sequence;
            attachment.name = "logcat.txt";
            attachment.type = "text/plain";
            attachment.disposition = Part.ATTACHMENT;
            attachment.size = null;
            attachment.progress = 0;
            attachment.id = db.attachment().insertAttachment(attachment);

            Process proc = null;
            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                String[] cmd = new String[]{"logcat",
                        "-d",
                        "-v", "threadtime",
                        //"-t", "1000",
                        Log.TAG + ":I"};
                proc = Runtime.getRuntime().exec(cmd);

                long size = 0;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null)
                        size += write(os, line + "\r\n");
                }

                db.attachment().setDownloaded(attachment.id, size);
            } finally {
                if (proc != null)
                    proc.destroy();
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void attachNotificationInfo(Context context, long id, int sequence) {
        try {
            DB db = DB.getInstance(context);

            EntityAttachment attachment = new EntityAttachment();
            attachment.message = id;
            attachment.sequence = sequence;
            attachment.name = "notification.txt";
            attachment.type = "text/plain";
            attachment.disposition = Part.ATTACHMENT;
            attachment.size = null;
            attachment.progress = 0;
            attachment.id = db.attachment().insertAttachment(attachment);

            long size = 0;
            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);


                String name;
                int filter = nm.getCurrentInterruptionFilter();
                switch (filter) {
                    case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                        name = "Unknown";
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        name = "All";
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                        name = "Priority";
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
                        name = "None";
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                        name = "Alarms";
                        break;
                    default:
                        name = Integer.toString(filter);
                }

                size += write(os, String.format("Interruption filter allow=%s %s\r\n\r\n",
                        name, (filter == NotificationManager.INTERRUPTION_FILTER_ALL ? "" : "!!!")));

                for (NotificationChannel channel : nm.getNotificationChannels())
                    try {
                        JSONObject jchannel = NotificationHelper.channelToJSON(channel);
                        size += write(os, jchannel.toString(2) + "\r\n\r\n");
                    } catch (JSONException ex) {
                        size += write(os, ex.toString() + "\r\n");
                    }

                size += write(os,
                        String.format("Importance none=%d; min=%d; low=%d; default=%d; high=%d; max=%d; unspecified=%d\r\n",
                                NotificationManager.IMPORTANCE_NONE,
                                NotificationManager.IMPORTANCE_MIN,
                                NotificationManager.IMPORTANCE_LOW,
                                NotificationManager.IMPORTANCE_DEFAULT,
                                NotificationManager.IMPORTANCE_HIGH,
                                NotificationManager.IMPORTANCE_MAX,
                                NotificationManager.IMPORTANCE_UNSPECIFIED));
                size += write(os,
                        String.format("Visibility private=%d; public=%d; secret=%d\r\n",
                                Notification.VISIBILITY_PRIVATE,
                                Notification.VISIBILITY_PUBLIC,
                                Notification.VISIBILITY_SECRET));
            }

            db.attachment().setDownloaded(attachment.id, size);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static void attachEnvironment(Context context, long id, int sequence) {
        try {
            DB db = DB.getInstance(context);

            EntityAttachment attachment = new EntityAttachment();
            attachment.message = id;
            attachment.sequence = sequence;
            attachment.name = "environment.txt";
            attachment.type = "text/plain";
            attachment.disposition = Part.ATTACHMENT;
            attachment.size = null;
            attachment.progress = 0;
            attachment.id = db.attachment().insertAttachment(attachment);

            long now = new Date().getTime();

            long size = 0;
            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                try {
                    PackageInfo pi = context.getPackageManager()
                            .getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_PERMISSIONS);
                    for (int i = 0; i < pi.requestedPermissions.length; i++)
                        if (pi.requestedPermissions[i] != null &&
                                pi.requestedPermissions[i].startsWith("android.permission.")) {
                            boolean granted = ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0);
                            size += write(os, String.format("%s=%b\r\n",
                                    pi.requestedPermissions[i].replace("android.permission.", ""), granted));
                        }
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }
                size += write(os, "\r\n");

                size += write(os, String.format("Configuration: %s\r\n\r\n",
                        context.getResources().getConfiguration()));

                for (Provider p : Security.getProviders())
                    size += write(os, String.format("%s\r\n", p));
                size += write(os, "\r\n");

                size += write(os, String.format("%s=%b\r\n",
                        Helper.getOpenKeychainPackage(context),
                        Helper.isOpenKeychainInstalled(context)));

                try {
                    int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
                    size += write(os, context.getString(R.string.title_advanced_aes_key_size,
                            Helper.humanReadableByteCount(maxKeySize, false)));
                    size += write(os, "\r\n");
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }
                size += write(os, "\r\n");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        Map<String, String> stats = Debug.getRuntimeStats();
                        for (String key : stats.keySet())
                            size += write(os, String.format("%s=%s\r\n", key, stats.get(key)));
                    } catch (Throwable ex) {
                        size += write(os, String.format("%s\r\n", ex));
                    }
                    size += write(os, "\r\n");
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        // https://developer.android.com/reference/android/app/ApplicationExitInfo
                        ActivityManager am = Helper.getSystemService(context, ActivityManager.class);
                        List<ApplicationExitInfo> infos = am.getHistoricalProcessExitReasons(
                                context.getPackageName(), 0, 20);
                        for (ApplicationExitInfo info : infos)
                            size += write(os, String.format("%s: %s %s/%s reason=%d status=%d importance=%d\r\n",
                                    new Date(info.getTimestamp()), info.getDescription(),
                                    Helper.humanReadableByteCount(info.getPss() * 1024L),
                                    Helper.humanReadableByteCount(info.getRss() * 1024L),
                                    info.getReason(), info.getStatus(), info.getReason()));
                    } catch (Throwable ex) {
                        size += write(os, String.format("%s\r\n", ex));
                    }

                    size += write(os, "\r\n");
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    try {
                        UsageStatsManager usm = Helper.getSystemService(context, UsageStatsManager.class);
                        UsageEvents events = usm.queryEventsForSelf(now - 12 * 3600L, now);
                        UsageEvents.Event event = new UsageEvents.Event();
                        while (events != null && events.hasNextEvent()) {
                            events.getNextEvent(event);
                            size += write(os, String.format("%s %s %s b=%d s=%d\r\n",
                                    new Date(event.getTimeStamp()),
                                    getEventType(event.getEventType()),
                                    event.getClassName(),
                                    event.getAppStandbyBucket(),
                                    event.getShortcutId()));
                        }
                    } catch (Throwable ex) {
                        size += write(os, String.format("%s\r\n", ex));
                    }

                try {
                    PackageManager pm = context.getPackageManager();
                    List<PermissionGroupInfo> groups = pm.getAllPermissionGroups(0);
                    groups.add(0, null); // Ungrouped

                    for (PermissionGroupInfo group : groups) {
                        String name = (group == null ? null : group.name);
                        size += write(os, String.format("\r\n%s\r\n", name == null ? "Ungrouped" : name));
                        size += write(os, "----------------------------------------\r\n");

                        try {
                            for (PermissionInfo permission : pm.queryPermissionsByGroup(name, 0))
                                size += write(os, String.format("%s\r\n", permission.name));
                        } catch (Throwable ex) {
                            size += write(os, String.format("%s\r\n", ex));
                        }
                    }
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }
            }

            db.attachment().setDownloaded(attachment.id, size);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static String getEventType(int type) {
        switch (type) {
            case UsageEvents.Event.ACTIVITY_PAUSED:
                return "Activity/paused";
            case UsageEvents.Event.ACTIVITY_RESUMED:
                return "Activity/resumed";
            case UsageEvents.Event.ACTIVITY_STOPPED:
                return "Activity/stopped";
            case UsageEvents.Event.CONFIGURATION_CHANGE:
                return "Configuration/change";
            case UsageEvents.Event.DEVICE_SHUTDOWN:
                return "Device/shutdown";
            case UsageEvents.Event.DEVICE_STARTUP:
                return "Device/startup";
            case UsageEvents.Event.FOREGROUND_SERVICE_START:
                return "Foreground/start";
            case UsageEvents.Event.FOREGROUND_SERVICE_STOP:
                return "Foreground/stop";
            case UsageEvents.Event.KEYGUARD_HIDDEN:
                return "Keyguard/hidden";
            case UsageEvents.Event.KEYGUARD_SHOWN:
                return "Keyguard/shown";
            case UsageEvents.Event.SCREEN_INTERACTIVE:
                return "Screen/interactive";
            case UsageEvents.Event.SCREEN_NON_INTERACTIVE:
                return "Screen/non-interactive";
            case UsageEvents.Event.SHORTCUT_INVOCATION:
                return "Shortcut/invocation";
            case UsageEvents.Event.STANDBY_BUCKET_CHANGED:
                return "Bucket/changed";
            case UsageEvents.Event.USER_INTERACTION:
                return "User/interaction";
            default:
                return Integer.toString(type);
        }
    }

    private static void attachClassifierData(Context context, long id, int sequence) throws IOException, JSONException {
        DB db = DB.getInstance(context);

        EntityAttachment attachment = new EntityAttachment();
        attachment.message = id;
        attachment.sequence = sequence;
        attachment.name = "classifier.json";
        attachment.type = "application/json";
        attachment.disposition = Part.ATTACHMENT;
        attachment.size = null;
        attachment.progress = 0;
        attachment.id = db.attachment().insertAttachment(attachment);

        MessageClassifier.save(context);
        File source = MessageClassifier.getFile(context, false);
        File target = attachment.getFile(context);
        Helper.copy(source, target);

        db.attachment().setDownloaded(attachment.id, target.length());
    }

    static SpannableStringBuilder getCiphers() {
        SpannableStringBuilder ssb = new SpannableStringBuilderEx();

        for (String protocol : new String[]{"SSL", "TLS"})
            try {
                int begin = ssb.length();
                ssb.append("Protocol: ").append(protocol);
                ssb.setSpan(new StyleSpan(Typeface.BOLD), begin, ssb.length(), 0);
                ssb.append("\r\n\r\n");

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init((KeyStore) null);

                ssb.append("Provider: ").append(tmf.getProvider().getName()).append("\r\n");
                ssb.append("Algorithm: ").append(tmf.getAlgorithm()).append("\r\n");

                TrustManager[] tms = tmf.getTrustManagers();
                if (tms != null)
                    for (TrustManager tm : tms)
                        ssb.append("Manager: ").append(tm.getClass().getName()).append("\r\n");

                SSLContext sslContext = SSLContext.getInstance(protocol);

                ssb.append("Context: ").append(sslContext.getProtocol()).append("\r\n\r\n");

                sslContext.init(null, tmf.getTrustManagers(), null);
                SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket();

                List<String> protocols = new ArrayList<>();
                protocols.addAll(Arrays.asList(socket.getEnabledProtocols()));

                for (String p : socket.getSupportedProtocols()) {
                    boolean enabled = protocols.contains(p);
                    if (!enabled)
                        ssb.append('(');
                    int start = ssb.length();
                    ssb.append(p);
                    if (!enabled) {
                        ssb.setSpan(new StrikethroughSpan(), start, ssb.length(), 0);
                        ssb.append(')');
                    }
                    ssb.append("\r\n");
                }
                ssb.append("\r\n");

                List<String> ciphers = new ArrayList<>();
                ciphers.addAll(Arrays.asList(socket.getEnabledCipherSuites()));

                for (String c : socket.getSupportedCipherSuites()) {
                    boolean enabled = ciphers.contains(c);
                    if (!enabled)
                        ssb.append('(');
                    int start = ssb.length();
                    ssb.append(c);
                    if (!enabled) {
                        ssb.setSpan(new StrikethroughSpan(), start, ssb.length(), 0);
                        ssb.append(')');
                    }
                    ssb.append("\r\n");
                }
                ssb.append("\r\n");
            } catch (Throwable ex) {
                ssb.append(ex.toString());
            }

        ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), 0, ssb.length(), 0);

        return ssb;
    }

    private static int write(OutputStream os, String text) throws IOException {
        byte[] bytes = text.getBytes();
        os.write(bytes);
        return bytes.length;
    }

    private static long getFreeMem() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory());
        long max = rt.maxMemory();
        return (max - used);
    }

    static int getFreeMemMb() {
        return (int) (getFreeMem() / 1024L / 1024L);
    }

    static int getAvailableMb() {
        Runtime rt = Runtime.getRuntime();
        return (int) (rt.maxMemory() / 1024L / 1024L);
    }

    static InternetAddress myAddress() throws UnsupportedEncodingException {
        return new InternetAddress("marcel+fairemail@faircode.eu", "FairCode", StandardCharsets.UTF_8.name());
    }

    static StringBuilder getSpans(CharSequence text) {
        StringBuilder sb = new StringBuilder();
        TextUtils.dumpSpans(text, new Printer() {
            @Override
            public void println(String x) {
                if (sb.length() > 0)
                    sb.append(' ');
                sb.append(x.replace('\n', '|')).append(']');
            }
        }, "[");
        return sb;
    }
}
