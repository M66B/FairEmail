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

import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteFullException;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.BadParcelableException;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Debug;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.text.TextUtils;
import android.view.Display;
import android.view.InflateException;
import android.view.WindowManager;
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
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertPathValidatorException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import io.requery.android.database.CursorWindowAllocationException;

public class Log {
    private static boolean debug = false;
    private static final int MAX_CRASH_REPORTS = 5;
    private static final String TAG = "fairemail";

    public static void setDebug(boolean value) {
        debug = value;
    }

    public static int d(String msg) {
        if (debug)
            return android.util.Log.d(TAG, msg);
        else
            return 0;
    }

    public static int i(String msg) {
        if (BuildConfig.BETA_RELEASE)
            return android.util.Log.i(TAG, msg);
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
        return android.util.Log.w(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(Throwable ex) {
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

    static void setCrashReporting(boolean enabled) {
        try {
            if (enabled)
                Bugsnag.startSession();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    static void breadcrumb(String name, Map<String, String> crumb) {
        try {
            Map<String, Object> ocrumb = new HashMap<>();
            for (String key : crumb.keySet())
                ocrumb.put(key, crumb.get(key));
            Bugsnag.leaveBreadcrumb(name, ocrumb, BreadcrumbType.LOG);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    static void setup(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        debug = prefs.getBoolean("debug", false);

        setupBugsnag(context);
    }

    private static void setupBugsnag(final Context context) {
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
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        String no_internet = context.getString(R.string.title_no_internet);

        String installer = context.getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID);
        config.addMetadata("extra", "installer", installer == null ? "-" : installer);
        config.addMetadata("extra", "installed", new Date(Helper.getInstallTime(context)).toString());
        config.addMetadata("extra", "fingerprint", Helper.hasValidFingerprint(context));
        config.addMetadata("extra", "memory_class", am.getMemoryClass());
        config.addMetadata("extra", "memory_class_large", am.getLargeMemoryClass());

        config.addOnSession(new OnSessionCallback() {
            @Override
            public boolean onSession(@NonNull Session session) {
                // opt-in
                return prefs.getBoolean("crash_reports", false);
            }
        });

        config.addOnError(new OnErrorCallback() {
            @Override
            public boolean onError(@NonNull Event event) {
                // opt-in
                boolean crash_reports = prefs.getBoolean("crash_reports", false);
                if (!crash_reports)
                    return false;

                Throwable ex = event.getOriginalError();
                boolean should = shouldNotify(ex);

                if (should) {
                    event.addMetadata("extra", "thread", Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
                    event.addMetadata("extra", "memory_free", getFreeMemMb());
                    event.addMetadata("extra", "memory_available", getAvailableMb());

                    Boolean ignoringOptimizations = Helper.isIgnoringOptimizations(context);
                    event.addMetadata("extra", "optimizing", (ignoringOptimizations != null && !ignoringOptimizations));

                    String theme = prefs.getString("theme", "light");
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

        if (prefs.getBoolean("crash_reports", false))
            Bugsnag.startSession();
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
                if (v != null && v.getClass().isArray()) {
                    int length = Array.getLength(v);
                    if (length <= 10) {
                        String[] elements = new String[length];
                        for (int i = 0; i < length; i++) {
                            Object element = Array.get(v, i);
                            if (element instanceof Long)
                                elements[i] = element.toString() + " (0x" + Long.toHexString((Long) element) + ")";
                            else
                                elements[i] = (element == null ? null : element.toString());
                        }
                        value = TextUtils.join(",", elements);
                    } else
                        value = "[" + length + "]";
                } else if (v instanceof Long)
                    value = v.toString() + " (0x" + Long.toHexString((Long) v) + ")";

                result.add(key + "=" + value + (value == null ? "" : " (" + v.getClass().getSimpleName() + ")"));
            }
        } catch (BadParcelableException ex) {
            // android.os.BadParcelableException: ClassNotFoundException when unmarshalling: ...
            Log.e(ex);
        }

        return result;
    }

    static void logMemory(Context context, String message) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        int mb = Math.round(mi.availMem / 0x100000L);
        int perc = Math.round(mi.availMem / (float) mi.totalMem * 100.0f);
        Log.i(message + " " + mb + " MB" + " " + perc + " %");
    }

    static boolean isOwnFault(Throwable ex) {
        if (!isSupportedDevice())
            return false;

        if (ex instanceof OutOfMemoryError)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
                if (cause instanceof DeadObjectException) // Includes DeadSystemException
                    return false;
                cause = cause.getCause();
            }
        }

        if (ex instanceof RuntimeException &&
                ex.getMessage() != null &&
                (ex.getMessage().contains("DeadSystemException") ||
                        ex.getMessage().startsWith("Could not get application info") ||
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
        if (stack.length > 0 &&
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

        if (stack.length > 0 &&
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

        if (stack.length > 0 &&
                "android.hardware.biometrics.BiometricPrompt".equals(stack[0].getClassName()))
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String android.hardware.fingerprint.FingerprintManager.getErrorString(int, int)' on a null object reference
                  at android.hardware.biometrics.BiometricPrompt.lambda$sendError$0(BiometricPrompt.java:490)
                  at android.hardware.biometrics.-$$Lambda$BiometricPrompt$HqBGXtBUWNc-v8NoHYsj2gLfaRw.run(Unknown Source:6)
                  at android.os.Handler.handleCallback(Handler.java:873)
             */
            return false;

        if (stack.length > 0 &&
                "android.text.SpannableStringInternal".equals(stack[0].getClassName()))
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

        if (stack.length > 0 &&
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

        if (stack.length > 0 &&
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

        if (BuildConfig.BETA_RELEASE)
            return true;

        while (ex != null) {
            for (StackTraceElement ste : ex.getStackTrace())
                if (ste.getClassName().startsWith(BuildConfig.APPLICATION_ID))
                    return true;
            ex = ex.getCause();
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

            if (ex instanceof MessagingException &&
                    ("connection failure".equals(ex.getMessage()) ||
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
                    ex instanceof FolderClosedException || ex instanceof FolderClosedIOException)
                return null;

            if (ex instanceof IllegalStateException &&
                    ("Not connected".equals(ex.getMessage()) ||
                            "This operation is not allowed on a closed folder".equals(ex.getMessage())))
                return null;
        }

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
        File file = new File(context.getCacheDir(), "crash.log");
        Log.w("Writing exception to " + file);

        try (FileWriter out = new FileWriter(file, true)) {
            out.write(BuildConfig.VERSION_NAME + " " + new Date() + "\r\n");
            out.write(ex + "\r\n" + android.util.Log.getStackTraceString(ex) + "\r\n");
        } catch (IOException e) {
            Log.e(e);
        }
    }

    static EntityMessage getDebugInfo(Context context, int title, Throwable ex, String log) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(title)).append("\n\n\n\n");
        sb.append(getAppInfo(context));
        if (ex != null)
            sb.append(ex.toString()).append("\n").append(android.util.Log.getStackTraceString(ex));
        if (log != null)
            sb.append(log);
        String body = "<pre>" + TextUtils.htmlEncode(sb.toString()) + "</pre>";

        EntityMessage draft;

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            List<TupleIdentityEx> identities = db.identity().getComposableIdentities(null);
            if (identities == null || identities.size() == 0)
                throw new IllegalArgumentException(context.getString(R.string.title_no_composable));

            EntityIdentity identity = identities.get(0);
            EntityFolder drafts = db.folder().getFolderByType(identity.account, EntityFolder.DRAFTS);

            draft = new EntityMessage();
            draft.account = drafts.account;
            draft.folder = drafts.id;
            draft.identity = identity.id;
            draft.msgid = EntityMessage.generateMessageId();
            draft.thread = draft.msgid;
            draft.to = new Address[]{myAddress()};
            draft.subject = context.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME + " debug info";
            draft.received = new Date().getTime();
            draft.seen = true;
            draft.ui_seen = true;
            draft.id = db.message().insertMessage(draft);

            File file = draft.getFile(context);
            Helper.writeText(file, body);
            db.message().setMessageContent(draft.id,
                    true,
                    HtmlHelper.getLanguage(context, body),
                    false,
                    HtmlHelper.getPreview(body),
                    null);

            attachSettings(context, draft.id, 1);
            attachAccounts(context, draft.id, 2);
            attachNetworkInfo(context, draft.id, 3);
            attachLog(context, draft.id, 4);
            attachOperations(context, draft.id, 5);
            attachLogcat(context, draft.id, 6);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                attachNotificationInfo(context, draft.id, 7);

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

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.title_unexpected_error)
                    .setMessage(Log.formatThrowable(ex, false))
                    .setPositiveButton(android.R.string.cancel, null);

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

        // Get version info
        String installer = context.getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID);
        sb.append(String.format("%s: %s/%s %s/%s%s%s%s\r\n",
                context.getString(R.string.app_name),
                BuildConfig.APPLICATION_ID,
                installer,
                BuildConfig.VERSION_NAME,
                Helper.hasValidFingerprint(context) ? "1" : "3",
                BuildConfig.PLAY_STORE_RELEASE ? "p" : "",
                BuildConfig.DEBUG ? "d" : "",
                ActivityBilling.isPro(context) ? "+" : ""));
        sb.append(String.format("Android: %s (SDK %d)\r\n", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
        sb.append("\r\n");

        // Get device info
        sb.append(String.format("uid: %s\r\n", android.os.Process.myUid()));
        sb.append(String.format("Brand: %s\r\n", Build.BRAND));
        sb.append(String.format("Manufacturer: %s\r\n", Build.MANUFACTURER));
        sb.append(String.format("Model: %s\r\n", Build.MODEL));
        sb.append(String.format("Product: %s\r\n", Build.PRODUCT));
        sb.append(String.format("Device: %s\r\n", Build.DEVICE));
        sb.append(String.format("Host: %s\r\n", Build.HOST));
        sb.append(String.format("Display: %s\r\n", Build.DISPLAY));
        sb.append(String.format("Id: %s\r\n", Build.ID));
        sb.append("\r\n");

        sb.append(String.format("Processors: %d\r\n", Runtime.getRuntime().availableProcessors()));

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        sb.append(String.format("Memory class: %d MB/%s\r\n",
                am.getMemoryClass(), Helper.humanReadableByteCount(mi.totalMem)));

        sb.append(String.format("Storage space: %s/%s\r\n",
                Helper.humanReadableByteCount(Helper.getAvailableStorageSpace()),
                Helper.humanReadableByteCount(Helper.getTotalStorageSpace())));

        Runtime rt = Runtime.getRuntime();
        long hused = (rt.totalMemory() - rt.freeMemory()) / 1024L;
        long hmax = rt.maxMemory() / 1024L;
        long nheap = Debug.getNativeHeapAllocatedSize() / 1024L;
        sb.append(String.format("Heap usage: %s/%s KiB native: %s KiB\r\n", hused, hmax, nheap));

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float density = context.getResources().getDisplayMetrics().density;
        sb.append(String.format("Density %f resolution: %.2f x %.2f dp %b\r\n",
                density,
                size.x / density, size.y / density,
                context.getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL)));

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean ignoring = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ignoring = pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID);
        sb.append(String.format("Battery optimizations: %b\r\n", !ignoring));
        sb.append(String.format("Charging: %b\r\n", Helper.isCharging(context)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            int bucket = usm.getAppStandbyBucket();
            sb.append(String.format("Standby bucket: %d\r\n", bucket));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean saving = (cm.getRestrictBackgroundStatus() == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED);
            sb.append(String.format("Data saving: %b\r\n", saving));
        }

        String charset = MimeUtility.getDefaultJavaCharset();
        sb.append(String.format("Default charset: %s/%s\r\n", charset, MimeUtility.mimeCharset(charset)));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean reporting = prefs.getBoolean("crash_reports", false);
        if (reporting) {
            String uuid = prefs.getString("uuid", null);
            sb.append(String.format("UUID: %s\r\n", uuid == null ? "-" : uuid));
        }

        sb.append("\r\n");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                // https://developer.android.com/reference/android/app/ApplicationExitInfo
                List<ApplicationExitInfo> infos = am.getHistoricalProcessExitReasons(
                        context.getPackageName(), 0, 20);
                for (ApplicationExitInfo info : infos)
                    sb.append(String.format("%s: %s %s/%s reason=%d status=%d importance=%d\r\n",
                            new Date(info.getTimestamp()), info.getDescription(),
                            Helper.humanReadableByteCount(info.getPss() * 1024L),
                            Helper.humanReadableByteCount(info.getRss() * 1024L),
                            info.getReason(), info.getStatus(), info.getReason()));
            } catch (Throwable ex) {
                Log.e(ex);
            }
            sb.append("\r\n");
        }

        sb.append(new Date(Helper.getInstallTime(context))).append("\r\n");
        sb.append(new Date()).append("\r\n");

        sb.append("\r\n");

        return sb;
    }

    private static void attachSettings(Context context, long id, int sequence) throws IOException {
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
            for (String key : keys)
                size += write(os, key + "=" + settings.get(key) + "\r\n");
        }

        db.attachment().setDownloaded(attachment.id, size);
    }

    private static void attachAccounts(Context context, long id, int sequence) throws IOException {
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
            size += write(os, "accounts=" + accounts.size() + "\r\n\r\n");

            for (EntityAccount account : accounts) {
                if (account.synchronize) {
                    size += write(os, account.name +
                            " " + (account.protocol == EntityAccount.TYPE_IMAP ? "IMAP" : "POP") + "/" + account.auth_type +
                            " " + account.host + ":" + account.port + "/" + account.encryption +
                            " sync=" + account.synchronize +
                            " exempted=" + account.poll_exempted +
                            " poll=" + account.poll_interval +
                            " " + account.state +
                            (account.last_connected == null ? "" : " " + dtf.format(account.last_connected)) +
                            "\r\n");

                    List<EntityFolder> folders = db.folder().getFolders(account.id, false, false);
                    if (folders.size() > 0)
                        Collections.sort(folders, folders.get(0).getComparator(context));
                    for (EntityFolder folder : folders)
                        if (folder.synchronize)
                            size += write(os, "- " + folder.name + " " + folder.type +
                                    " poll=" + folder.poll + "/" + folder.poll_factor +
                                    " days=" + folder.sync_days + "/" + folder.keep_days +
                                    " " + folder.state +
                                    (folder.last_sync == null ? "" : " " + dtf.format(folder.last_sync)) +
                                    "\r\n");

                    size += write(os, "\r\n");
                }
            }

            for (EntityAccount account : accounts)
                try {
                    JSONObject jaccount = account.toJSON();
                    jaccount.put("state", account.state == null ? "null" : account.state);
                    jaccount.put("warning", account.warning);
                    jaccount.put("error", account.error);

                    if (account.last_connected != null)
                        jaccount.put("last_connected", new Date(account.last_connected).toString());

                    jaccount.put("keep_alive_ok", account.keep_alive_ok);
                    jaccount.put("keep_alive_failed", account.keep_alive_failed);
                    jaccount.put("keep_alive_succeeded", account.keep_alive_succeeded);

                    jaccount.remove("user");
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
                        jfolder.put("read_only", folder.read_only);
                        jfolder.put("selectable", folder.selectable);
                        jfolder.put("inferiors", folder.inferiors);
                        jfolder.put("error", folder.error);
                        if (folder.last_sync != null)
                            jfolder.put("last_sync", new Date(folder.last_sync).toString());
                        size += write(os, jfolder.toString(2) + "\r\n");
                    }

                    List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                    for (EntityIdentity identity : identities)
                        try {
                            JSONObject jidentity = identity.toJSON();
                            jidentity.remove("user");
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

        db.attachment().setDownloaded(attachment.id, size);
    }

    private static void attachNetworkInfo(Context context, long id, int sequence) throws IOException {
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
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

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

            size += write(os, "VPN active=" + ConnectionHelper.vpnActive(context) + "\r\n\r\n");

            ConnectionHelper.NetworkState state = ConnectionHelper.getNetworkState(context);
            size += write(os, "Connected=" + state.isConnected() + "\r\n");
            size += write(os, "Suitable=" + state.isSuitable() + "\r\n");
            size += write(os, "Unmetered=" + state.isUnmetered() + "\r\n");
            size += write(os, "Roaming=" + state.isRoaming() + "\r\n");
        }

        db.attachment().setDownloaded(attachment.id, size);
    }

    private static void attachLog(Context context, long id, int sequence) throws IOException {
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

            for (EntityLog entry : db.log().getLogs(from))
                size += write(os, String.format("%s %s\r\n", TF.format(entry.time), entry.data));
        }

        db.attachment().setDownloaded(attachment.id, size);
    }

    private static void attachOperations(Context context, long id, int sequence) throws IOException {
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
    }

    private static void attachLogcat(Context context, long id, int sequence) throws IOException {
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
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void attachNotificationInfo(Context context, long id, int sequence) throws IOException {
        DB db = DB.getInstance(context);

        EntityAttachment attachment = new EntityAttachment();
        attachment.message = id;
        attachment.sequence = sequence;
        attachment.name = "channel.txt";
        attachment.type = "text/plain";
        attachment.disposition = Part.ATTACHMENT;
        attachment.size = null;
        attachment.progress = 0;
        attachment.id = db.attachment().insertAttachment(attachment);

        long size = 0;
        File file = attachment.getFile(context);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            for (NotificationChannel channel : nm.getNotificationChannels())
                try {
                    JSONObject jchannel = NotificationHelper.channelToJSON(channel);
                    size += write(os, jchannel.toString(2) + "\r\n\r\n");
                } catch (JSONException ex) {
                    size += write(os, ex.toString() + "\r\n");
                }

            size += write(os, "Importance none=0; min=1; low=2; default=3; high=4; max=5\r\n\r\n");
        }

        db.attachment().setDownloaded(attachment.id, size);
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

    static boolean isSupportedDevice() {
        if ("Amazon".equals(Build.BRAND) && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        /*
            java.lang.IllegalArgumentException: Comparison method violates its general contract!
            java.lang.IllegalArgumentException: Comparison method violates its general contract!
            at java.util.TimSort.mergeHi(TimSort.java:864)
            at java.util.TimSort.mergeAt(TimSort.java:481)
            at java.util.TimSort.mergeCollapse(TimSort.java:406)
            at java.util.TimSort.sort(TimSort.java:210)
            at java.util.TimSort.sort(TimSort.java:169)
            at java.util.Arrays.sort(Arrays.java:2010)
            at java.util.Collections.sort(Collections.java:1883)
            at android.view.ViewGroup$ChildListForAccessibility.init(ViewGroup.java:7181)
            at android.view.ViewGroup$ChildListForAccessibility.obtain(ViewGroup.java:7138)
            at android.view.ViewGroup.dispatchPopulateAccessibilityEventInternal(ViewGroup.java:2734)
            at android.view.View.dispatchPopulateAccessibilityEvent(View.java:5617)
            at android.view.View.sendAccessibilityEventUncheckedInternal(View.java:5582)
            at android.view.View.sendAccessibilityEventUnchecked(View.java:5566)
            at android.view.View.sendAccessibilityEventInternal(View.java:5543)
            at android.view.View.sendAccessibilityEvent(View.java:5512)
            at android.view.View.onFocusChanged(View.java:5449)
            at android.view.View.handleFocusGainInternal(View.java:5229)
            at android.view.ViewGroup.handleFocusGainInternal(ViewGroup.java:651)
            at android.view.View.requestFocusNoSearch(View.java:7950)
            at android.view.View.requestFocus(View.java:7929)
            at android.view.ViewGroup.requestFocus(ViewGroup.java:2612)
            at android.view.ViewGroup.onRequestFocusInDescendants(ViewGroup.java:2657)
            at android.view.ViewGroup.requestFocus(ViewGroup.java:2613)
            at android.view.View.requestFocus(View.java:7896)
            at android.view.View.requestFocus(View.java:7875)
            at androidx.recyclerview.widget.RecyclerView.recoverFocusFromState(SourceFile:3788)
            at androidx.recyclerview.widget.RecyclerView.dispatchLayoutStep3(SourceFile:4023)
            at androidx.recyclerview.widget.RecyclerView.dispatchLayout(SourceFile:3652)
            at androidx.recyclerview.widget.RecyclerView.consumePendingUpdateOperations(SourceFile:1877)
            at androidx.recyclerview.widget.RecyclerView$w.run(SourceFile:5044)
            at android.view.Choreographer$CallbackRecord.run(Choreographer.java:781)
            at android.view.Choreographer.doCallbacks(Choreographer.java:592)
            at android.view.Choreographer.doFrame(Choreographer.java:559)
            at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:767)
         */
            return false;
        }

        return true;
    }

    static boolean isXiaomi() {
        return "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER);
    }
}