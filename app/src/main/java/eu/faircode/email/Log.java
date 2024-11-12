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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteFullException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.DeadSystemException;
import android.os.Debug;
import android.os.OperationCanceledException;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.Printer;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
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

import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.CertPathValidatorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.mail.AuthenticationFailedException;
import javax.mail.FolderClosedException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import javax.mail.internet.InternetAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class Log {
    private static Context ctx;

    private static final int MAX_CRASH_REPORTS = (Log.isTestRelease() ? 50 : 5);
    private static final String TAG = "fairemail";

    static final String TOKEN_REFRESH_REQUIRED =
            "Token refresh required. Is there a VPN based app running?";

    static final List<String> IGNORE_CLASSES = Collections.unmodifiableList(Arrays.asList(
            "com.sun.mail.util.MailConnectException",

            "android.accounts.AuthenticatorException",
            "android.accounts.OperationCanceledException",
            "android.app.RemoteServiceException",

            "java.lang.NoClassDefFoundError",
            "java.lang.UnsatisfiedLinkError",

            "java.nio.charset.MalformedInputException",

            "java.net.ConnectException",
            "java.net.SocketException",
            "java.net.SocketTimeoutException",
            "java.net.UnknownHostException",
            "java.lang.InterruptedException",

            "javax.mail.AuthenticationFailedException",
            "javax.mail.internet.AddressException",
            "javax.mail.internet.ParseException",
            "javax.mail.MessageRemovedException",
            "javax.mail.FolderNotFoundException",
            "javax.mail.ReadOnlyFolderException",
            "javax.mail.FolderClosedException",
            "com.sun.mail.util.FolderClosedIOException",
            "javax.mail.StoreClosedException",

            "org.xmlpull.v1.XmlPullParserException"
    ));

    static {
        System.loadLibrary("fairemail");
    }

    public static native void jni_set_log_level(int level);

    public static native long[] jni_safe_runtime_stats();

    public static int d(String msg) {
        return d(TAG, msg);
    }

    public static int d(String tag, String msg) {
        org.tinylog.Logger.tag(tag).debug(msg);
        return 0;
    }

    public static int i(String msg) {
        return i(TAG, msg);
    }

    public static int i(String tag, String msg) {
        org.tinylog.Logger.tag(tag).info(msg);
        return 0;
    }

    public static int w(String msg) {
        org.tinylog.Logger.tag(TAG).warn(msg);
        return 0;
    }

    public static int e(String msg) {
        if (BuildConfig.BETA_RELEASE)
            try {
                ThrowableWrapper ex = new ThrowableWrapper();
                ex.setMessage(msg);
                Bugsnag.notify(ex, new OnErrorCallback() {
                    @Override
                    public boolean onError(@NonNull Event event) {
                        event.setSeverity(Severity.ERROR);
                        return true;
                    }
                });
            } catch (Throwable ex) {
                Log.i(ex);
            }

        org.tinylog.Logger.tag(TAG).error(msg);
        return 0;
    }

    public static int i(Throwable ex) {
        org.tinylog.Logger.tag(TAG).info(ex);
        return 0;
    }

    public static int w(String tag, String msg) {
        org.tinylog.Logger.tag(tag).warn(msg);
        return 0;
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
                Log.i(ex1);
            }

        org.tinylog.Logger.tag(TAG).warn(ex);
        return 0;
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
                Log.i(ex1);
            }

        org.tinylog.Logger.tag(TAG).error(ex);
        return 0;
    }

    public static int i(String prefix, Throwable ex) {
        org.tinylog.Logger.tag(TAG).info(ex, prefix);
        return 0;
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
                Log.i(ex1);
            }

        org.tinylog.Logger.tag(TAG).warn(ex, prefix);
        return 0;
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
                Log.i(ex1);
            }

        org.tinylog.Logger.tag(TAG).error(ex, prefix);
        return 0;
    }

    public static void persist(String message) {
        if (ctx == null)
            org.tinylog.Logger.tag(TAG).error(message);
        else
            EntityLog.log(ctx, message);
    }

    public static void persist(EntityLog.Type type, String message) {
        if (ctx == null)
            org.tinylog.Logger.tag(TAG).error(type.name() + " " + message);
        else
            EntityLog.log(ctx, type, message);
    }

    static void setCrashReporting(boolean enabled) {
        try {
            if (enabled)
                Bugsnag.resumeSession();
            else
                Bugsnag.pauseSession();
        } catch (Throwable ex) {
            Log.i(ex);
        }
    }

    static void forceCrashReport(Context context, Throwable fatal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean crash_reports = prefs.getBoolean("crash_reports", false);
        try {
            prefs.edit().putBoolean("crash_reports", true).apply();
            setCrashReporting(true);
            Log.e(fatal);
        } finally {
            prefs.edit().putBoolean("crash_reports", crash_reports).apply();
            setCrashReporting(crash_reports);
        }
    }

    public static void breadcrumb(String name, Bundle args) {
        Map<String, String> crumb = new HashMap<>();
        for (String key : args.keySet()) {
            Object value = args.get(key);
            if (value instanceof Boolean)
                crumb.put(key, Boolean.toString((Boolean) value));
            else if (value instanceof Integer)
                crumb.put(key, Integer.toString((Integer) value));
            else if (value instanceof Long)
                crumb.put(key, Long.toString((Long) value));
            else if (value instanceof Float)
                crumb.put(key, Float.toString((Float) value));
            else if (value instanceof Double)
                crumb.put(key, Double.toString((Double) value));
            else if (value instanceof String || value instanceof Spanned) {
                String v = value.toString();
                if (v.length() > 50)
                    v = v.substring(0, 50) + "...";
                crumb.put(key, v);
            } else if (value == null)
                crumb.put(key, "<null>");
            else
                crumb.put(key, "<" + value.getClass().getName() + ">");
        }
        breadcrumb(name, crumb);
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
            Log.e(ex);
        }
    }

    static void setup(Context context) {
        ctx = context;
        setLevel(context);
        setupBugsnag(context);
    }

    static void setLevel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int level = prefs.getInt("log_level", getDefaultLogLevel());
        jni_set_log_level(level);
    }

    static int getDefaultLogLevel() {
        return (BuildConfig.DEBUG || Log.isTestRelease() ? android.util.Log.INFO : android.util.Log.WARN);
    }

    private static void setupBugsnag(final Context context) {
        try {
            Log.i("Configuring Bugsnag");

            // https://docs.bugsnag.com/platforms/android/sdk/
            com.bugsnag.android.Configuration config =
                    new com.bugsnag.android.Configuration("9d2d57476a0614974449a3ec33f2604a");
            config.setTelemetry(Collections.emptySet());

            if (BuildConfig.DEBUG)
                config.setReleaseStage("Debug");
            else
                config.setReleaseStage(getReleaseType(context));

            config.setAutoTrackSessions(false);

            ErrorTypes etypes = new ErrorTypes();
            etypes.setUnhandledExceptions(true);
            etypes.setAnrs(false);
            etypes.setNdkCrashes(false);
            config.setEnabledErrorTypes(etypes);
            config.setMaxBreadcrumbs(BuildConfig.PLAY_STORE_RELEASE ? 250 : 500);

            Set<Pattern> discardClasses = new HashSet<>();
            for (String clazz : IGNORE_CLASSES)
                discardClasses.add(Pattern.compile(clazz.replace(".", "\\.")));
            config.setDiscardClasses(discardClasses);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            ActivityManager am = Helper.getSystemService(context, ActivityManager.class);

            String no_internet = context.getString(R.string.title_no_internet);

            String installer = Helper.getInstallerName(context);
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
                    return prefs.getBoolean("crash_reports", false) || Log.isTestRelease();
                }
            });

            config.addOnError(new OnErrorCallback() {
                @Override
                public boolean onError(@NonNull Event event) {
                    // opt-in
                    boolean crash_reports = prefs.getBoolean("crash_reports", false);
                    if (!crash_reports && !Log.isTestRelease())
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
                        event.addMetadata("extra", "classifier_size", MessageClassifier.getSize(context));

                        Boolean ignoringOptimizations = Helper.isIgnoringOptimizations(context);
                        event.addMetadata("extra", "optimizing", (ignoringOptimizations != null && !ignoringOptimizations));

                        String theme = prefs.getString("theme", "blue_orange_system");
                        event.addMetadata("extra", "theme", theme);
                        event.addMetadata("extra", "package", BuildConfig.APPLICATION_ID);
                        event.addMetadata("extra", "locale", Locale.getDefault().toString());

                        Boolean foreground = Helper.isOnForeground();
                        if (foreground != null)
                            event.addMetadata("extra", "foreground", Boolean.toString(foreground));
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

            if (prefs.getBoolean("crash_reports", false) || Log.isTestRelease())
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

    static @NonNull String getReleaseType(Context context) {
        if (Helper.hasValidFingerprint(context)) {
            if (BuildConfig.PLAY_STORE_RELEASE) {
                String installer = Helper.getInstallerName(context);
                String type = "Play Store";
                if (installer != null && !Helper.PLAY_PACKAGE_NAME.equals(installer))
                    type += " (" + installer + ")";
                return type;
            } else if (BuildConfig.FDROID_RELEASE)
                return "Reproducible";
            else if (BuildConfig.AMAZON_RELEASE)
                return "Amazon";
            else
                return "GitHub";
        } else if (Helper.isSignedByFDroid(context))
            return "F-Droid";
        else {
            if (BuildConfig.APPLICATION_ID.startsWith("eu.faircode.email"))
                return "Other";
            else
                return "Clone";
        }
    }

    static boolean isTestRelease() {
        return BuildConfig.TEST_RELEASE;
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
                        else if (element instanceof Spanned)
                            elements[i] = "(span:" + Helper.getPrintableString(element.toString(), true) + ")";
                        else
                            elements[i] = (element == null ? "<null>" : Helper.getPrintableString(element.toString(), true));
                    }
                    value = TextUtils.join(",", elements);
                    if (length > 10)
                        value += ", ...";
                    value = "[" + value + "]";
                } else if (v instanceof Long)
                    value = v + " (0x" + Long.toHexString((Long) v) + ")";
                else if (v instanceof Spanned)
                    value = "(span:" + Helper.getPrintableString(v.toString(), true) + ")";
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

        if ("android.app.RemoteServiceException$CannotDeliverBroadcastException".equals(ex.getClass().getName()))
            /*
                android.app.RemoteServiceException$CannotDeliverBroadcastException: can't deliver broadcast
                    at android.app.ActivityThread.throwRemoteServiceException(ActivityThread.java:2180)
                    at android.app.ActivityThread.access$3000(ActivityThread.java:324)
                    at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2435)
                    at android.os.Handler.dispatchMessage(Handler.java:106)
             */
            return false;

        if ("android.app.RemoteServiceException$CannotPostForegroundServiceNotificationException".equals(ex.getClass().getName()))
            /*
                android.app.RemoteServiceException$CannotPostForegroundServiceNotificationException: Bad notification for startForeground
                    at android.app.ActivityThread.throwRemoteServiceException(ActivityThread.java:2219)
                    at android.app.ActivityThread.-$$Nest$mthrowRemoteServiceException(Unknown Source:0)
                    at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2505)
                    at android.os.Handler.dispatchMessage(Handler.java:106)
             */
            return false;

        if ("android.app.RemoteServiceException$BadForegroundServiceNotificationException".equals(ex.getClass().getName()))
            /*
                android.app.RemoteServiceException$BadForegroundServiceNotificationException: Bad notification(tag=null, id=100) posted from package eu.faircode.email, crashing app(uid=10122, pid=3370): Software rendering doesn't support hardware bitmaps
                    at android.app.ActivityThread.throwRemoteServiceException(ActivityThread.java:1982)
                    at android.app.ActivityThread.-$$Nest$mthrowRemoteServiceException(Unknown Source:0)
                    at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2238)
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

        if ("java.lang.Daemons$FinalizerWatchdogDaemon".equals(ex.getClass().getName()))
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.lang.Object.toString()' on a null object reference
                    at java.lang.Daemons$FinalizerWatchdogDaemon.finalizingObjectAsString(Daemons.java:605)
                    at java.lang.Daemons$FinalizerWatchdogDaemon.waitForProgress(Daemons.java:559)
                    at java.lang.Daemons$FinalizerWatchdogDaemon.runInternal(Daemons.java:412)
                    at java.lang.Daemons$Daemon.run(Daemons.java:145)
                    at java.lang.Thread.run(Thread.java:1012)
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

        if (ex instanceof IllegalStateException &&
                ex.getMessage() != null &&
                ex.getMessage().startsWith("Layout state should be one of"))
            /*
                Practically, this means OOM

                java.lang.IllegalStateException: Layout state should be one of 100 but it is 10
                    at androidx.recyclerview.widget.RecyclerView$State.assertLayoutStep(SourceFile:44)
                    at androidx.recyclerview.widget.RecyclerView.dispatchLayoutStep3(SourceFile:4)
                    at androidx.recyclerview.widget.RecyclerView.dispatchLayout(SourceFile:125)
                    at androidx.recyclerview.widget.RecyclerView.consumePendingUpdateOperations(SourceFile:107)
                    at androidx.recyclerview.widget.RecyclerView$ViewFlinger.run(SourceFile:19)
                    at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1105)
                    at android.view.Choreographer.doCallbacks(Choreographer.java:896)
                    at android.view.Choreographer.doFrame(Choreographer.java:810)
                    at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:1090)
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

        if ("android.database.CursorWindowAllocationException".equals(ex.getClass().getName()))
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
                ex.getCause() != null &&
                "android.database.CursorWindowAllocationException".equals(ex.getCause().getClass().getName()))
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

        if (ex instanceof NullPointerException &&
                stack.length > 0 &&
                "android.app.OplusActivityManager".equals(stack[0].getClassName()) &&
                "finishNotOrderReceiver".equals(stack[0].getMethodName()))
                /*
                    java.lang.NullPointerException: Attempt to invoke interface method 'boolean android.os.IBinder.transact(int, android.os.Parcel, android.os.Parcel, int)' on a null object reference
                        at android.app.OplusActivityManager.finishNotOrderReceiver(OplusActivityManager.java:2360)
                        at android.content.BroadcastReceiver$PendingResult.sendFinished(BroadcastReceiver.java:347)
                        at android.content.BroadcastReceiver$PendingResult.finish(BroadcastReceiver.java:302)
                        at android.app.ActivityThread.handleReceiver(ActivityThread.java:4352)
                 */
            return false;

        if (ex instanceof NullPointerException &&
                stack.length > 0 &&
                "android.widget.Editor$ActionPinnedPopupWindow".equals(stack[0].getClassName()) &&
                "computeLocalPosition".equals(stack[0].getMethodName()))
                /*
                    java.lang.NullPointerException: Attempt to invoke virtual method 'float android.text.Layout.getPrimaryHorizontal(int)' on a null object reference
                        at android.widget.Editor$ActionPinnedPopupWindow.computeLocalPosition(Editor.java:4134)
                        at android.widget.Editor$PinnedPopupWindow.show(Editor.java:3737)
                        at android.widget.Editor$ActionPinnedPopupWindow.show(Editor.java:4282)
                        at android.widget.Editor$ActionPopupWindow.show(Editor.java:5224)
                        at android.widget.Editor$HandleView$2.run(Editor.java:6783)
                        at android.os.Handler.handleCallback(Handler.java:938)
                 */
            return false;

        if (ex instanceof NullPointerException)
            for (StackTraceElement ste : stack)
                if ("java.lang.Daemons$FinalizerWatchdogDaemon".equals(ste.getClassName()))
                    return false;

        if (ex instanceof NullPointerException &&
                ex.getMessage() != null && ex.getMessage().contains("android.window.BackMotionEvent"))
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'float android.window.BackMotionEvent.getTouchX()' on a null object reference
                    at android.window.WindowOnBackInvokedDispatcher$OnBackInvokedCallbackWrapper.lambda$onBackStarted$1(WindowOnBackInvokedDispatcher.java:353)
                    at android.window.WindowOnBackInvokedDispatcher$OnBackInvokedCallbackWrapper.$r8$lambda$jWVwe-YeLRxW3tAMLuWZynG6e1k(Unknown Source:0)
                    at android.window.WindowOnBackInvokedDispatcher$OnBackInvokedCallbackWrapper$$ExternalSyntheticLambda4.run(Unknown Source:4)
                    at android.os.Handler.handleCallback(Handler.java:958)
                    at android.os.Handler.dispatchMessage(Handler.java:99)
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

        if (ex instanceof IndexOutOfBoundsException &&
                stack.length > 0 &&
                "android.text.PackedIntVector".equals(stack[0].getClassName()) &&
                "getValue".equals(stack[0].getMethodName()))
            /*
                java.lang.IndexOutOfBoundsException: 2, 1
                    at android.text.PackedIntVector.getValue(PackedIntVector.java:75)
                    at android.text.DynamicLayout.getLineTop(DynamicLayout.java:1001)
                    at android.text.Layout.getLineBottom(Layout.java:1652)
                    at android.widget.Editor.getCurrentLineAdjustedForSlop(Editor.java:6851)
                    at android.widget.Editor.access$8700(Editor.java:175)
                    at android.widget.Editor$InsertionHandleView.updatePosition(Editor.java:6317)
                    at android.widget.Editor$HandleView.onTouchEvent(Editor.java:5690)
                    at android.widget.Editor$InsertionHandleView.onTouchEvent(Editor.java:6235)
                    at android.view.View.dispatchTouchEvent(View.java:13484)
                    at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3222)
                    at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2904)
                    at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3222)
                    at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2904)
                    at android.widget.PopupWindow$PopupDecorView.dispatchTouchEvent(PopupWindow.java:2700)
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

        if (ex instanceof IndexOutOfBoundsException)
            /*
                java.lang.IndexOutOfBoundsException: charAt: 128 >= length 128
                    at android.text.SpannableStringBuilder.charAt(SpannableStringBuilder.java:125)
                    at android.text.CharSequenceCharacterIterator.next(CharSequenceCharacterIterator.java:67)
                    at android.icu.text.RuleBasedBreakIterator.handleNext(RuleBasedBreakIterator.java:886)
                    at android.icu.text.RuleBasedBreakIterator.-$$Nest$mhandleNext(Unknown Source:0)
                    at android.icu.text.RuleBasedBreakIterator$BreakCache.populateNear(RuleBasedBreakIterator.java:1486)
                    at android.icu.text.RuleBasedBreakIterator.isBoundary(RuleBasedBreakIterator.java:552)
                    at android.text.method.WordIterator.isBoundary(WordIterator.java:112)
                    at android.widget.Editor$SelectionHandleView.positionAtCursorOffset(Editor.java:6319)
                    at android.widget.Editor$HandleView.updatePosition(Editor.java:5290)
                    at android.widget.Editor$PositionListener.onPreDraw(Editor.java:3730)
                    at android.view.ViewTreeObserver.dispatchOnPreDraw(ViewTreeObserver.java:1176)
                    at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:4029)
             */
            for (StackTraceElement elm : stack)
                if ("android.text.method.WordIterator".equals(elm.getClassName()) &&
                        "isBoundary".equals(elm.getMethodName()))
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
            if (clazz != null && clazz.startsWith("org.chromium."))
                /*
                    android.content.res.Resources$NotFoundException:
                      at android.content.res.ResourcesImpl.getValue (ResourcesImpl.java:225)
                      at android.content.res.Resources.getInteger (Resources.java:1192)
                      at org.chromium.ui.base.DeviceFormFactor.a (chromium-TrichromeWebViewGoogle6432.aab-stable-500512534:105)
                      at y8.onCreateActionMode (chromium-TrichromeWebViewGoogle6432.aab-stable-500512534:744)
                      at px.onCreateActionMode (chromium-TrichromeWebViewGoogle6432.aab-stable-500512534:36)
                      at com.android.internal.policy.DecorView$ActionModeCallback2Wrapper.onCreateActionMode (DecorView.java:2722)
                      at com.android.internal.policy.DecorView.startActionMode (DecorView.java:926)
                      at com.android.internal.policy.DecorView.startActionModeForChild (DecorView.java:882)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.ViewGroup.startActionModeForChild (ViewGroup.java:1035)
                      at android.view.View.startActionMode (View.java:7654)
                      at org.chromium.content.browser.selection.SelectionPopupControllerImpl.B (chromium-TrichromeWebViewGoogle6432.aab-stable-500512534:31)
                      at uh0.a (chromium-TrichromeWebViewGoogle6432.aab-stable-500512534:1605)
                      at Kk0.i (chromium-TrichromeWebViewGoogle6432.aab-stable-500512534:259)
                      at B6.run (chromium-TrichromeWebViewGoogle6432.aab-stable-500512534:454)
                      at android.os.Handler.handleCallback (Handler.java:938)
                 */
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

        if (ex instanceof RuntimeException) {
            for (StackTraceElement ste : stack)
                if ("android.app.job.JobService$JobHandler".equals(ste.getClassName()) &&
                        "handleMessage".equals(ste.getMethodName()))
                    return false;
                /*
                    java.lang.RuntimeException: java.lang.NullPointerException: Attempt to invoke virtual method 'int com.android.server.job.controllers.JobStatus.getUid()' on a null object reference
                        at android.app.job.JobService$JobHandler.handleMessage(JobService.java:139)
                        at android.os.Handler.dispatchMessage(Handler.java:102)
                        at android.os.Looper.loop(Looper.java:150)
                        at android.app.ActivityThread.main(ActivityThread.java:5546)
                        at java.lang.reflect.Method.invoke(Native Method)
                        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:792)
                        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:682)
                 */
        }

        if (stack.length > 0 &&
                stack[0].getClassName() != null &&
                stack[0].getClassName().startsWith("com.android.internal.widget.FloatingToolbar"))
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'int android.util.Size.getWidth()' on a null object reference
                    at com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$11.onMeasure(FloatingToolbar.java:1430)
                    at android.view.View.measure(View.java:25787)
                    at com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup.measure(FloatingToolbar.java:1530)
                    at com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup.layoutMainPanelItems(FloatingToolbar.java:1284)
                    at com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup.layoutMenuItems(FloatingToolbar.java:554)
                    at com.android.internal.widget.FloatingToolbar.doShow(FloatingToolbar.java:283)
                    at com.android.internal.widget.FloatingToolbar.show(FloatingToolbar.java:221)
                    at com.android.internal.view.FloatingActionMode$FloatingToolbarVisibilityHelper.updateToolbarVisibility(FloatingActionMode.java:386)
                    at com.android.internal.view.FloatingActionMode$2.run(FloatingActionMode.java:75)
                    at android.os.Handler.handleCallback(Handler.java:938)
             */
            return false;

        if (ex.getMessage() != null && ex.getMessage().contains("adjustNativeLibraryPaths"))
            /*
                java.lang.NullPointerException: Attempt to read from field 'java.lang.String android.content.pm.ApplicationInfo.primaryCpuAbi' on a null object reference in method 'android.content.pm.ApplicationInfo android.app.LoadedApk.adjustNativeLibraryPaths(android.content.pm.ApplicationInfo)'
                    at android.app.LoadedApk.adjustNativeLibraryPaths(LoadedApk.java:191)
                    at android.app.LoadedApk.setApplicationInfo(LoadedApk.java:382)
                    at android.app.LoadedApk.updateApplicationInfo(LoadedApk.java:335)
                    at android.app.ActivityThread.handleDispatchPackageBroadcast(ActivityThread.java:6314)
                    at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2182)
                    at android.os.Handler.dispatchMessage(Handler.java:106)
                    at android.os.Looper.loopOnce(Looper.java:201)
                    at android.os.Looper.loop(Looper.java:288)
                    at android.app.ActivityThread.main(ActivityThread.java:7870)
                    at java.lang.reflect.Method.invoke(Native Method)
                    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:548)
                    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1003)
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

            //if (ex instanceof MessagingException &&
            //        ex.getMessage() != null &&
            //        ex.getMessage().startsWith("OAuth refresh"))
            //    return null;

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

        if (ex.getMessage() != null && ex.getMessage().contains("Read timed out"))
            ex = new Throwable("No response received from email server", ex);

        if (ex instanceof MessagingException &&
                ex.getCause() instanceof UnknownHostException)
            ex = new Throwable("Email server address lookup failed", ex);

        if (ConnectionHelper.isAborted(ex))
            ex = new Throwable("The server or network actively disconnected the connection", ex);

        StringBuilder sb = new StringBuilder();
        if (BuildConfig.DEBUG)
            sb.append(new ThrowableWrapper(ex).toSafeString());
        else
            sb.append(new ThrowableWrapper(ex).getSafeMessageOrName());

        Throwable cause = ex.getCause();
        while (cause != null) {
            if (BuildConfig.DEBUG)
                sb.append(separator).append(new ThrowableWrapper(cause).toSafeString());
            else
                sb.append(separator).append(new ThrowableWrapper(cause).getSafeMessageOrName());
            cause = cause.getCause();
        }

        return sb.toString();
    }

    static void unexpectedError(Fragment fragment, Throwable ex) {
        unexpectedError(fragment, ex, true);
    }

    static void unexpectedError(Fragment fragment, Throwable ex, boolean report) {
        try {
            unexpectedError(fragment.getParentFragmentManager(), ex, report);
        } catch (Throwable exex) {
            Log.w(exex);
            /*
                Exception java.lang.IllegalStateException:
                  at androidx.fragment.app.Fragment.getParentFragmentManager (Fragment.java:1107)
                  at eu.faircode.email.FragmentDialogForwardRaw.send (FragmentDialogForwardRaw.java:307)
                  at eu.faircode.email.FragmentDialogForwardRaw.access$200 (FragmentDialogForwardRaw.java:56)
                  at eu.faircode.email.FragmentDialogForwardRaw$4.onClick (FragmentDialogForwardRaw.java:239)
                  at androidx.appcompat.app.AlertController$ButtonHandler.handleMessage (AlertController.java:167)
                  at android.os.Handler.dispatchMessage (Handler.java:106)
                  at android.os.Looper.loopOnce (Looper.java:210)
                  at android.os.Looper.loop (Looper.java:299)
                  at android.app.ActivityThread.main (ActivityThread.java:8168)
                  at java.lang.reflect.Method.invoke (Method.java)
                  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run (RuntimeInit.java:556)
                  at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:1037)
             */
        }
    }

    static void unexpectedError(FragmentManager manager, Throwable ex) {
        unexpectedError(manager, ex, true);
    }

    static void unexpectedError(FragmentManager manager, Throwable ex, boolean report) {
        unexpectedError(manager, ex, report, null);
    }

    static void unexpectedError(FragmentManager manager, Throwable ex, int faq) {
        unexpectedError(manager, ex, false, faq);
    }

    static void unexpectedError(FragmentManager manager, Throwable ex, boolean report, Integer faq) {
        Log.e(ex);

        if (ex instanceof OutOfMemoryError)
            report = false;

        Bundle args = new Bundle();
        args.putSerializable("ex", ex);
        args.putBoolean("report", report);
        args.putInt("faq", faq == null ? 0 : faq);

        FragmentDialogUnexpected fragment = new FragmentDialogUnexpected();
        fragment.setArguments(args);
        fragment.show(manager, "error:unexpected");
    }

    public static class FragmentDialogUnexpected extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Bundle args = getArguments();
            final Throwable ex = (Throwable) args.getSerializable("ex");
            final boolean report = args.getBoolean("report", true);
            final int faq = args.getInt("faq");

            final Context context = getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View dview = inflater.inflate(R.layout.dialog_unexpected, null);
            TextView tvCaption = dview.findViewById(R.id.tvCaption);
            TextView tvError = dview.findViewById(R.id.tvError);
            Button btnHelp = dview.findViewById(R.id.btnHelp);

            tvCaption.setText(report ? R.string.title_unexpected_error : R.string.title_setup_error);

            String message = Log.formatThrowable(ex, false);
            tvError.setText(message);

            btnHelp.setVisibility(faq > 0 ? View.VISIBLE : View.GONE);
            btnHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(v.getContext(), faq);
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setView(dview)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.menu_faq, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Uri uri = Helper.getSupportUri(context, "Unexpected:error");
                            if (!TextUtils.isEmpty(message))
                                uri = uri
                                        .buildUpon()
                                        .appendQueryParameter("message", Helper.limit(message, 384))
                                        .build();
                            Helper.view(context, uri, true);
                        }
                    });

            if (report && DebugHelper.isAvailable())
                builder.setNeutralButton(R.string.title_report, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dialog will be dismissed
                        final Context context = getContext();

                        new SimpleTask<Long>() {
                            @Override
                            protected Long onExecute(Context context, Bundle args) throws Throwable {
                                EntityMessage m = DebugHelper.getDebugInfo(context,
                                        "report", R.string.title_unexpected_info_remark, ex, null, null);
                                return (m == null ? null : m.id);
                            }

                            @Override
                            protected void onExecuted(Bundle args, Long id) {
                                if (id == null)
                                    return;

                                context.startActivity(new Intent(context, ActivityCompose.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .putExtra("action", "edit")
                                        .putExtra("id", id));
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                // Ignored
                            }
                        }.execute(getContext(), getActivity(), new Bundle(), "error:unexpected");
                    }
                });

            return builder.create();
        }
    }

    static SpannableStringBuilder getCiphers() {
        SpannableStringBuilder ssb = new SpannableStringBuilderEx();

        for (Provider provider : new Provider[]{
                null, // Android
                new BouncyCastleJsseProvider(),
                new BouncyCastleJsseProvider(true)})
            for (String protocol : new String[]{"SSL", "TLS"})
                try {
                    int begin = ssb.length();

                    SSLContext sslContext = (provider == null
                            ? SSLContext.getInstance(protocol)
                            : SSLContext.getInstance(protocol, provider));

                    ssb.append("SSL protocol: ").append(sslContext.getProtocol()).append("\r\n");
                    Provider sslProvider = sslContext.getProvider();
                    ssb.append("SSL provider: ").append(sslProvider.getName());
                    if (sslProvider instanceof BouncyCastleJsseProvider) {
                        boolean fips = ((BouncyCastleJsseProvider) sslProvider).isFipsMode();
                        if (fips)
                            ssb.append(" FIPS");
                    }
                    ssb.append("\r\n");
                    ssb.append("SSL class: ").append(sslProvider.getClass().getName()).append("\r\n");

                    ssb.setSpan(new StyleSpan(Typeface.BOLD), begin, ssb.length(), 0);
                    ssb.append("\r\n");

                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init((KeyStore) null);

                    ssb.append("Trust provider: ").append(tmf.getProvider().getName()).append("\r\n");
                    ssb.append("Trust class: ").append(tmf.getProvider().getClass().getName()).append("\r\n");
                    ssb.append("Trust algorithm: ").append(tmf.getAlgorithm()).append("\r\n");

                    TrustManager[] tms = tmf.getTrustManagers();
                    if (tms != null)
                        for (TrustManager tm : tms)
                            ssb.append("Trust manager: ").append(tm.getClass().getName()).append("\r\n");
                    ssb.append("\r\n");

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
                    Log.e(ex);
                }

        ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), 0, ssb.length(), 0);

        return ssb;
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
