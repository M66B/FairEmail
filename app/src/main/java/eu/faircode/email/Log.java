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
import android.app.Dialog;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
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
import android.text.TextUtils;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.bugsnag.android.BeforeNotify;
import com.bugsnag.android.BeforeSend;
import com.bugsnag.android.BreadcrumbType;
import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Callback;
import com.bugsnag.android.Client;
import com.bugsnag.android.Error;
import com.bugsnag.android.Report;
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
import java.lang.reflect.Field;
import java.net.SocketException;
import java.security.cert.CertPathValidatorException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

public class Log {
    private static final int MAX_CRASH_REPORTS = 5;
    private static final String TAG = "fairemail";

    public static int d(String msg) {
        if (BuildConfig.DEBUG && false)
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
                List<StackTraceElement> ss = new ArrayList<>(Arrays.asList(new Throwable().getStackTrace()));
                ss.remove(0);
                Bugsnag.notify("Internal error", msg, ss.toArray(new StackTraceElement[0]), new Callback() {
                    @Override
                    public void beforeNotify(@NonNull Report report) {
                        report.getError().setSeverity(Severity.ERROR);
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
                Bugsnag.notify(ex, Severity.INFO);
            } catch (Throwable ex1) {
                ex1.printStackTrace();
            }
        return android.util.Log.w(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            try {
                Bugsnag.notify(ex, Severity.WARNING);
            } catch (Throwable ex1) {
                ex1.printStackTrace();
            }
        return android.util.Log.e(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int w(String prefix, Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            try {
                Bugsnag.notify(ex, Severity.INFO);
            } catch (Throwable ex1) {
                ex1.printStackTrace();
            }
        return android.util.Log.w(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(String prefix, Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            try {
                Bugsnag.notify(ex, Severity.WARNING);
            } catch (Throwable ex1) {
                ex1.printStackTrace();
            }
        return android.util.Log.e(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    static void setCrashReporting(boolean enabled) {
        try {
            if (enabled)
                Bugsnag.startSession();
            else
                Bugsnag.stopSession();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    static void breadcrumb(String name, Map<String, String> crumb) {
        try {
            Bugsnag.leaveBreadcrumb(name, BreadcrumbType.LOG, crumb);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    static void setupBugsnag(Context context) {
        // https://docs.bugsnag.com/platforms/android/sdk/
        com.bugsnag.android.Configuration config =
                new com.bugsnag.android.Configuration("9d2d57476a0614974449a3ec33f2604a");

        if (BuildConfig.DEBUG)
            config.setReleaseStage("debug");
        else {
            String type = "other";
            if (Helper.hasValidFingerprint(context))
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

        config.setIgnoreClasses(ignore.toArray(new String[0]));

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String no_internet = context.getString(R.string.title_no_internet);

        config.beforeSend(new BeforeSend() {
            @Override
            public boolean run(@NonNull Report report) {
                // opt-in
                boolean crash_reports = prefs.getBoolean("crash_reports", false);
                if (!crash_reports)
                    return false;

                Throwable ex = report.getError().getException();
                return shouldNotify(ex);
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

        Bugsnag.init(context, config);

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

        final String installer = context.getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID);
        final boolean fingerprint = Helper.hasValidFingerprint(context);
        final Boolean ignoringOptimizations = Helper.isIgnoringOptimizations(context);

        Bugsnag.beforeNotify(new BeforeNotify() {
            @Override
            public boolean run(@NonNull Error error) {
                error.addToTab("extra", "installer", installer == null ? "-" : installer);
                error.addToTab("extra", "fingerprint", fingerprint);
                error.addToTab("extra", "thread", Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
                error.addToTab("extra", "free", Log.getFreeMemMb());
                error.addToTab("extra", "optimizing", (ignoringOptimizations != null && !ignoringOptimizations));

                String theme = prefs.getString("theme", "light");
                error.addToTab("extra", "theme", theme);
                error.addToTab("extra", "package", BuildConfig.APPLICATION_ID);
                return true;
            }
        });
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

        /*
            android.app.RemoteServiceException: Bad notification for startForeground: java.util.ConcurrentModificationException
            android.app.RemoteServiceException: Bad notification for startForeground: java.util.ConcurrentModificationException
            at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2204)
        */
        if ("android.app.RemoteServiceException".equals(ex.getClass().getName()))
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

        if (ex instanceof ArrayIndexOutOfBoundsException) {
            /*
                java.lang.ArrayIndexOutOfBoundsException: length=74; index=74
                at android.text.TextLine.measure(TextLine.java:343)
                at android.text.TextLine.metrics(TextLine.java:298)
                at android.text.Layout.measurePara(Layout.java:2118)
                at android.text.Layout.getDesiredWidthWithLimit(Layout.java:196)
                at android.widget.TextView.onMeasure(TextView.java:8511)
                at androidx.appcompat.widget.AppCompatTextView.onMeasure(SourceFile:551)
                at android.view.View.measure(View.java:23190)
                at androidx.constraintlayout.widget.ConstraintLayout$Measurer.measure(SourceFile:724)
            */
            return false;
        }

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

        if (ex instanceof RuntimeException &&
                ex.getMessage() != null &&
                ex.getMessage().startsWith("Could not get application info"))
            return false;
        /*
                java.lang.RuntimeException: Could not get application info.
                 java.lang.RuntimeException: Could not get application info.
                   at CH0.a(PG:11)
                   at org.chromium.content.browser.ChildProcessLauncherHelperImpl.a(PG:34)
                   at Fn2.run(PG:5)
                   at android.os.Handler.handleCallback(Handler.java:874)
                   at android.os.Handler.dispatchMessage(Handler.java:100)
                   at android.os.Looper.loop(Looper.java:198)
                   at android.os.HandlerThread.run(HandlerThread.java:65)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            if (ex instanceof RuntimeException && ex.getCause() instanceof DeadObjectException)
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

    static String formatThrowable(Throwable ex, boolean santize) {
        return formatThrowable(ex, " ", santize);
    }

    static String formatThrowable(Throwable ex, String separator, boolean sanitize) {
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
                throw new IllegalArgumentException(context.getString(R.string.title_no_identities));

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
            Helper.writeText(draft.getFile(context), body);
            db.message().setMessageContent(draft.id,
                    true,
                    false,
                    HtmlHelper.getPreview(body),
                    null);

            attachSettings(context, draft.id, 1);
            attachAccounts(context, draft.id, 2);
            attachNetworkInfo(context, draft.id, 3);
            attachLog(context, draft.id, 4);
            attachOperations(context, draft.id, 5);
            attachLogcat(context, draft.id, 6);

            EntityOperation.queue(context, draft, EntityOperation.ADD);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        ServiceSynchronize.eval(context, "debuginfo");

        return draft;
    }

    static void unexpectedError(FragmentManager manager, Throwable ex) {
        Log.e(ex);

        Bundle args = new Bundle();
        args.putSerializable("ex", ex);

        FragmentDialogUnexpected fragment = new FragmentDialogUnexpected();
        fragment.setArguments(args);
        fragment.show(manager, "error:unexpected");
    }

    public static class FragmentDialogUnexpected extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Throwable ex = (Throwable) getArguments().getSerializable("ex");

            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.title_unexpected_error)
                    .setMessage(Log.formatThrowable(ex, false))
                    .setPositiveButton(android.R.string.cancel, null)
                    .setNeutralButton(R.string.title_report, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dialog will be dismissed
                            final Context context = getContext();

                            new SimpleTask<Long>() {
                                @Override
                                protected Long onExecute(Context context, Bundle args) throws Throwable {
                                    return Log.getDebugInfo(context, R.string.title_crash_info_remark, ex, null).id;
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
                    })
                    .create();
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
        sb.append(String.format("Memory class: %d\r\n", am.getMemoryClass()));

        sb.append(String.format("Storage space: %s/%s\r\n",
                Helper.humanReadableByteCount(Helper.getAvailableStorageSpace(), true),
                Helper.humanReadableByteCount(Helper.getTotalStorageSpace(), true)));

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean reporting = prefs.getBoolean("crash_reports", false);
        if (reporting) {
            String uuid = prefs.getString("uuid", null);
            sb.append(String.format("UUID: %s\r\n", uuid == null ? "-" : uuid));
        }

        sb.append("\r\n");

        sb.append(new Date().toString()).append("\r\n");

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
            for (String key : settings.keySet())
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

        long size = 0;
        File file = attachment.getFile(context);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            List<EntityAccount> accounts = db.account().getAccounts();
            for (EntityAccount account : accounts)
                try {
                    JSONObject jaccount = account.toJSON();
                    jaccount.remove("user");
                    jaccount.remove("password");
                    size += write(os, "==========\r\n");
                    size += write(os, jaccount.toString(2) + "\r\n");

                    List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                    for (EntityIdentity identity : identities)
                        try {
                            JSONObject jidentity = identity.toJSON();
                            jidentity.remove("user");
                            jidentity.remove("password");
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
                size += write(os, ani.toString() + " metered=" + cm.isActiveNetworkMetered() + "\r\n\r\n");

            Network active = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                active = cm.getActiveNetwork();

            for (Network network : cm.getAllNetworks()) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                size += write(os, (network.equals(active) ? "active=" : "network=") + network + " capabilities=" + caps + "\r\n\r\n");
            }
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

            for (EntityOperation op : db.operation().getOperations())
                size += write(os, String.format("%s %d %s/%d %s %s %s\r\n",
                        TF.format(op.created),
                        op.message == null ? -1 : op.message,
                        op.name,
                        op.tries,
                        op.args,
                        op.state,
                        op.error));
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

    static InternetAddress myAddress() throws UnsupportedEncodingException {
        return new InternetAddress("marcel+fairemail@faircode.eu", "FairCode");
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