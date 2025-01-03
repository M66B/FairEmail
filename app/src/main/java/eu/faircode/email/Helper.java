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

import static android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_NONE;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ApplicationExitInfo;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.ext.SdkExtensions;
import android.os.storage.StorageManager;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.security.KeyChainException;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.SoftwareKeyboardControllerCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

public class Helper {
    private static Integer targetSdk = null;
    private static Boolean hasWebView = null;
    private static Boolean hasPlayStore = null;
    private static Boolean hasValidFingerprint = null;
    private static Boolean isSmartwatch = null;
    private static String installerName = "?";

    static final float LOW_LIGHT = 0.6f;

    static final int WAKELOCK_MAX = 30 * 60 * 1000; // milliseconds
    static final int BUFFER_SIZE = 8192; // Same as in Files class
    static final long MIN_REQUIRED_SPACE = 100 * 1000L * 1000L;
    static final long AUTH_AUTOCANCEL_TIMEOUT = 60 * 1000L; // milliseconds
    static final int PIN_FAILURE_DELAY = 3; // seconds
    static final long PIN_FAILURE_DELAY_MAX = 20 * 60 * 1000L; // milliseconds
    static final float BNV_LUMINANCE_THRESHOLD = 0.7f;
    static final float MIN_SNACKBAR_LUMINANCE = 0.3f;

    static final String PLAY_PACKAGE_NAME = "com.android.vending";

    static final String PGP_OPENKEYCHAIN_PACKAGE = "org.sufficientlysecure.keychain";
    static final String PGP_BEGIN_MESSAGE = "-----BEGIN PGP MESSAGE-----";
    static final String PGP_END_MESSAGE = "-----END PGP MESSAGE-----";

    static final String PACKAGE_WEBVIEW = "https://play.google.com/store/apps/details?id=com.google.android.webview";
    static final String PRIVACY_URI = "https://email.faircode.eu/privacy/";
    static final String TUTORIALS_URI = "https://github.com/M66B/FairEmail/tree/master/tutorials#main";
    static final String XDA_URI = "https://forum.xda-developers.com/showthread.php?t=3824168";
    static final String SUPPORT_URI = "https://contact.faircode.eu/?product=fairemailsupport";
    static final String TEST_URI = "https://play.google.com/apps/testing/" + BuildConfig.APPLICATION_ID;
    static final String BIMI_PRIVACY_URI = "https://datatracker.ietf.org/doc/html/draft-brotman-ietf-bimi-guidance-03#section-7.4";
    static final String LT_PRIVACY_URI = "https://languagetool.org/legal/privacy";
    static final String GITHUB_PRIVACY_URI = "https://docs.github.com/en/site-policy/privacy-policies/github-privacy-statement";
    static final String BITBUCKET_PRIVACY_URI = "https://www.atlassian.com/legal/privacy-policy";
    static final String ID_COMMAND_URI = "https://datatracker.ietf.org/doc/html/rfc2971#section-3.1";
    static final String AUTH_RESULTS_URI = "https://datatracker.ietf.org/doc/html/rfc7601";
    static final String FAVICON_PRIVACY_URI = "https://en.wikipedia.org/wiki/Favicon";
    static final String LICENSE_URI = "https://www.gnu.org/licenses/gpl-3.0.html";
    static final String DONTKILL_URI = "https://dontkillmyapp.com/";
    static final String URI_SUPPORT_RESET_OPEN = "https://support.google.com/pixelphone/answer/6271667";
    static final String URI_SUPPORT_CONTACT_GROUP = "https://support.google.com/contacts/answer/30970";
    static final String GOOGLE_PRIVACY_URI = "https://policies.google.com/privacy";

    // https://developer.android.com/distribute/marketing-tools/linking-to-google-play#PerformingSearch
    private static final String PLAY_STORE_SEARCH = "https://play.google.com/store/search";

    private static final String[] ROMAN_1000 = {"", "M", "MM", "MMM"};
    private static final String[] ROMAN_100 = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    private static final String[] ROMAN_10 = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    private static final String[] ROMAN_1 = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    static final String REGEX_UUID = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    static final Pattern EMAIL_ADDRESS = Pattern.compile(
            "[\\S&&[^\"@<>()]]{1,256}" +
                    "\\@" +
                    "[\\p{L}0-9][\\p{L}0-9\\-\\_]{0,64}" +
                    "(" +
                    "\\." +
                    "[\\p{L}0-9][\\p{L}0-9\\-\\_]{0,25}" +
                    ")+"
    );

    private static ExecutorService sSerialExecutor = null;
    private static ExecutorService sParallelExecutor = null;
    private static ExecutorService sUIExecutor = null;
    private static ExecutorService sMediaExecutor = null;
    private static ExecutorService sDownloadExecutor = null;

    static ExecutorService getSerialExecutor() {
        if (sSerialExecutor == null)
            sSerialExecutor = getBackgroundExecutor(1, "serial");
        return sSerialExecutor;
    }

    static ExecutorService getParallelExecutor() {
        if (sParallelExecutor == null)
            sParallelExecutor = getBackgroundExecutor(0, "parallel");
        return sParallelExecutor;
    }

    static ExecutorService getUIExecutor() {
        if (sUIExecutor == null)
            sUIExecutor = getBackgroundExecutor(0, "UI");
        return sUIExecutor;
    }

    static ExecutorService getMediaTaskExecutor() {
        if (sMediaExecutor == null)
            sMediaExecutor = getBackgroundExecutor(1, "media");
        return sMediaExecutor;
    }

    static ExecutorService getDownloadTaskExecutor() {
        if (sDownloadExecutor == null)
            sDownloadExecutor = getBackgroundExecutor(0, "download");
        return sDownloadExecutor;
    }

    static ExecutorService getBackgroundExecutor(int threads, final String name) {
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger threadId = new AtomicInteger();

            @Override
            public Thread newThread(@NonNull Runnable runnable) {
                int delay = 1;
                while (true)
                    try {
                        Thread thread = new Thread(runnable);
                        thread.setName("FairEmail_bg_" + name + "_" + threadId.getAndIncrement());
                        thread.setPriority(THREAD_PRIORITY_BACKGROUND);
                        return thread;
                    } catch (OutOfMemoryError ex) {
                        Log.w(ex);
                        try {
                            Thread.sleep(delay * 1000L);
                        } catch (InterruptedException ignored) {
                        }
                        delay *= 2;
                        if (delay > 7)
                            throw ex;
                    }
            }
        };

        if (threads == 0) {
            // java.lang.OutOfMemoryError: pthread_create (1040KB stack) failed: Try again
            // 1040 KB native stack size / 32 KB thread stack size ~ 32 threads
            int processors = Runtime.getRuntime().availableProcessors(); // Modern devices: 8
            threads = Math.max(8, processors * 2) + 1;
            return new ThreadPoolExecutorEx(
                    name,
                    threads,
                    threads,
                    3, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    factory);
        } else if (threads == 1)
            return new ThreadPoolExecutorEx(
                    name,
                    threads, threads,
                    3, TimeUnit.SECONDS,
                    new PriorityBlockingQueue<Runnable>(10, new PriorityComparator()),
                    factory) {
                private final AtomicLong sequenceId = new AtomicLong();

                @Override
                protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
                    RunnableFuture<T> task = super.newTaskFor(runnable, value);
                    if (runnable instanceof PriorityRunnable)
                        return new PriorityFuture<T>(task,
                                ((PriorityRunnable) runnable).getPriority(),
                                ((PriorityRunnable) runnable).getOrder());
                    else
                        return new PriorityFuture<>(task, 0, sequenceId.getAndIncrement());
                }
            };
        else
            return new ThreadPoolExecutorEx(
                    name,
                    threads, threads,
                    3, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    factory);
    }

    private static class ThreadPoolExecutorEx extends ThreadPoolExecutor {
        private String name;

        public ThreadPoolExecutorEx(
                String name,
                int corePoolSize, int maximumPoolSize,
                long keepAliveTime, TimeUnit unit,
                BlockingQueue<Runnable> workQueue,
                ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
            if (keepAliveTime != 0)
                allowCoreThreadTimeOut(true);
            this.name = name;
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            Log.d("Executing " + t.getName());
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            Log.d("Executed " + name + " pending=" + getQueue().size());
        }
    }

    private static class PriorityFuture<T> implements RunnableFuture<T> {
        private int priority;
        private long order;
        private RunnableFuture<T> wrapped;

        PriorityFuture(RunnableFuture<T> wrapped, int priority, long order) {
            this.wrapped = wrapped;
            this.priority = priority;
            this.order = order;
        }

        public int getPriority() {
            return this.priority;
        }

        public long getOrder() {
            return this.order;
        }

        @Override
        public void run() {
            wrapped.run();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return wrapped.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return wrapped.isCancelled();
        }

        @Override
        public boolean isDone() {
            return wrapped.isDone();
        }

        @Override
        public T get() throws ExecutionException, InterruptedException {
            return wrapped.get();
        }

        @Override
        public T get(long timeout, @NonNull TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
            return wrapped.get(timeout, unit);
        }
    }

    private static class PriorityComparator implements Comparator<Runnable> {
        @Override
        public int compare(Runnable r1, Runnable r2) {
            if (r1 instanceof PriorityFuture<?> && r2 instanceof PriorityFuture<?>) {
                Integer p1 = ((PriorityFuture<?>) r1).getPriority();
                Integer p2 = ((PriorityFuture<?>) r2).getPriority();
                int p = p1.compareTo(p2);
                if (p == 0) {
                    Long o1 = ((PriorityFuture<?>) r1).getOrder();
                    Long o2 = ((PriorityFuture<?>) r2).getOrder();
                    return o1.compareTo(o2);
                } else
                    return p;
            } else
                return 0;
        }
    }

    static class PriorityRunnable implements Runnable {
        private long group;
        private int priority;
        private long order;

        long getGroup() {
            return this.group;
        }

        int getPriority() {
            return this.priority;
        }

        long getOrder() {
            return this.order;
        }

        PriorityRunnable(long group, int priority, long order) {
            this.group = group;
            this.priority = priority;
            this.order = order;
        }

        @Override
        public void run() {
            Log.i("Run priority=" + priority);
        }
    }

    // Features

    static boolean hasPermission(Context context, String name) {
        if (Manifest.permission.WRITE_CALENDAR.equals(name))
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) !=
                    PackageManager.PERMISSION_GRANTED)
                return false;
        return (ContextCompat.checkSelfPermission(context, name) ==
                PackageManager.PERMISSION_GRANTED);
    }

    static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions)
            if (!hasPermission(context, permission))
                return false;
        return true;
    }

    static String[] getDesiredPermissions(Context context) {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_CONTACTS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);

        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_PERMISSIONS);
            for (int i = 0; i < pi.requestedPermissions.length; i++)
                if (Manifest.permission.READ_CALENDAR.equals(pi.requestedPermissions[i]))
                    permissions.add(Manifest.permission.READ_CALENDAR);
                else if (Manifest.permission.WRITE_CALENDAR.equals(pi.requestedPermissions[i]))
                    permissions.add(Manifest.permission.WRITE_CALENDAR);
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return permissions.toArray(new String[0]);
    }

    static String[] getOAuthPermissions() {
        List<String> permissions = new ArrayList<>();
        //permissions.add(Manifest.permission.READ_CONTACTS); // profile
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            permissions.add(Manifest.permission.GET_ACCOUNTS);
        return permissions.toArray(new String[0]);
    }

    private static boolean hasCustomTabs(Context context, Uri uri, String pkg) {
        PackageManager pm = context.getPackageManager();
        Intent view = new Intent(Intent.ACTION_VIEW, uri);

        int flags = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? 0 : PackageManager.MATCH_ALL);
        List<ResolveInfo> ris = pm.queryIntentActivities(view, flags); // action whitelisted
        for (ResolveInfo info : ris) {
            Intent intent = new Intent();
            intent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            intent.setPackage(info.activityInfo.packageName);
            if (pkg != null && !pkg.equals(info.activityInfo.packageName))
                continue;
            if (pm.resolveService(intent, 0) != null)
                return true;
        }

        return false;
    }

    static boolean hasWebView(Context context) {
        if (hasWebView == null)
            hasWebView = _hasWebView(context);
        return hasWebView;
    }

    private static boolean _hasWebView(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            if (pm.hasSystemFeature(PackageManager.FEATURE_WEBVIEW)) {
                WebView view = new WebView(context);
                view.setOverScrollMode(View.OVER_SCROLL_NEVER);
                return true;
            } else
                return false;
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                Caused by: java.lang.RuntimeException: Package manager has died
                    at android.app.ApplicationPackageManager.hasSystemFeature(ApplicationPackageManager.java:414)
                    at eu.faircode.email.Helper.hasWebView(SourceFile:375)
                    at eu.faircode.email.ApplicationEx.onCreate(SourceFile:110)
                    at android.app.Instrumentation.callApplicationOnCreate(Instrumentation.java:1014)
                    at android.app.ActivityThread.handleBindApplication(ActivityThread.java:4751)

                    Chromium WebView package does not exist
                    android.webkit.WebViewFactory$MissingWebViewPackageException: Failed to load WebView provider: No WebView installed
                        at android.webkit.WebViewFactory.getWebViewContextAndSetProvider(WebViewFactory.java:428)
                        at android.webkit.WebViewFactory.getProviderClass(WebViewFactory.java:493)
                        at android.webkit.WebViewFactory.getProvider(WebViewFactory.java:348)
                        at android.webkit.WebView.getFactory(WebView.java:2594)
                        at android.webkit.WebView.ensureProviderCreated(WebView.java:2588)
                        at android.webkit.WebView.setOverScrollMode(WebView.java:2656)
                        at android.view.View.<init>(View.java:5325)
                        at android.view.View.<init>(View.java:5466)
                        at android.view.ViewGroup.<init>(ViewGroup.java:702)
                        at android.widget.AbsoluteLayout.<init>(AbsoluteLayout.java:56)
                        at android.webkit.WebView.<init>(WebView.java:421)
                        at android.webkit.WebView.<init>(WebView.java:363)
                        at android.webkit.WebView.<init>(WebView.java:345)
                        at android.webkit.WebView.<init>(WebView.java:332)
                        at android.webkit.WebView.<init>(WebView.java:322)
                        at eu.faircode.email.WebViewEx.<init>(SourceFile:1)
             */
            return false;
        }
    }

    static boolean canPrint(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.hasSystemFeature(PackageManager.FEATURE_PRINTING);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static Boolean isIgnoringOptimizations(Context context) {
        try {
            if (isArc() || isWatch(context))
                return true;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                return null;

            PowerManager pm = Helper.getSystemService(context, PowerManager.class);
            if (pm == null)
                return null;

            return pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID);
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    static Integer getBatteryLevel(Context context) {
        try {
            BatteryManager bm = Helper.getSystemService(context, BatteryManager.class);
            if (bm == null)
                return null;
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    static boolean isCharging(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return false;
        try {
            BatteryManager bm = Helper.getSystemService(context, BatteryManager.class);
            if (bm == null)
                return false;
            return bm.isCharging();
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static boolean isPlayStoreInstall() {
        return BuildConfig.PLAY_STORE_RELEASE;
    }

    static boolean isAmazonInstall() {
        return BuildConfig.AMAZON_RELEASE;
    }

    static boolean hasPlayStore(Context context) {
        if (hasPlayStore == null)
            try {
                PackageManager pm = context.getPackageManager();
                pm.getPackageInfo("com.android.vending", 0);
                hasPlayStore = true;
            } catch (PackageManager.NameNotFoundException ex) {
                Log.i(ex);
                hasPlayStore = false;
            } catch (Throwable ex) {
                Log.e(ex);
                hasPlayStore = false;
            }
        return hasPlayStore;
    }

    static boolean isSecure(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                ContentResolver resolver = context.getContentResolver();
                int enabled = Settings.System.getInt(resolver, Settings.Secure.LOCK_PATTERN_ENABLED, 0);
                return (enabled != 0);
            } else {
                KeyguardManager kgm = Helper.getSystemService(context, KeyguardManager.class);
                return (kgm != null && kgm.isDeviceSecure());
            }
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static boolean isInstalled(Context context, String pkg) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(pkg, 0);
            return true;
        } catch (Throwable ex) {
            Log.i(ex);
            return false;
        }
    }

    static boolean isComponentEnabled(Context context, Class<?> clazz) {
        PackageManager pm = context.getPackageManager();
        int state = pm.getComponentEnabledSetting(new ComponentName(context, clazz));

        if (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            try {
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                        PackageManager.GET_ACTIVITIES |
                                PackageManager.GET_RECEIVERS |
                                PackageManager.GET_SERVICES |
                                PackageManager.GET_PROVIDERS |
                                PackageManager.GET_DISABLED_COMPONENTS);

                List<ComponentInfo> components = new ArrayList<>();
                if (pi.activities != null)
                    Collections.addAll(components, pi.activities);
                if (pi.services != null)
                    Collections.addAll(components, pi.services);
                if (pi.providers != null)
                    Collections.addAll(components, pi.providers);

                for (ComponentInfo component : components)
                    if (component.name.equals(clazz.getName()))
                        return component.isEnabled();
            } catch (PackageManager.NameNotFoundException ex) {
                Log.w(ex);
            }
        }

        return (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    static void enableComponent(Context context, Class<?> clazz, boolean whether) {
        enableComponent(context, clazz.getName(), whether);
    }

    static void enableComponent(Context context, String name, boolean whether) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, name),
                whether
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    static void setKeyboardIncognitoMode(EditText view, Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean incognito_keyboard = prefs.getBoolean("incognito_keyboard", false);
        if (incognito_keyboard)
            try {
                view.setImeOptions(view.getImeOptions() | EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);
            } catch (Throwable ex) {
                Log.e(ex);
            }
    }

    static boolean isAccessibilityEnabled(Context context) {
        try {
            AccessibilityManager am = Helper.getSystemService(context, AccessibilityManager.class);
            return (am != null && am.isEnabled());
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static String getStandbyBucketName(int bucket) {
        switch (bucket) {
            case 5:
                return "exempted";
            case UsageStatsManager.STANDBY_BUCKET_ACTIVE:
                return "active";
            case UsageStatsManager.STANDBY_BUCKET_WORKING_SET:
                return "workingset";
            case UsageStatsManager.STANDBY_BUCKET_FREQUENT:
                return "frequent";
            case UsageStatsManager.STANDBY_BUCKET_RARE:
                return "rare";
            case UsageStatsManager.STANDBY_BUCKET_RESTRICTED:
                return "restricted";
            default:
                return Integer.toString(bucket);
        }
    }

    static String getEventType(int type) {
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

    static String getExitReason(int reason) {
        switch (reason) {
            case ApplicationExitInfo.REASON_UNKNOWN:
                return "Unknown";
            case ApplicationExitInfo.REASON_EXIT_SELF:
                return "ExitSelf";
            case ApplicationExitInfo.REASON_SIGNALED:
                return "Signaled";
            case ApplicationExitInfo.REASON_LOW_MEMORY:
                return "LowMemory";
            case ApplicationExitInfo.REASON_CRASH:
                return "Crash";
            case ApplicationExitInfo.REASON_CRASH_NATIVE:
                return "CrashNative";
            case ApplicationExitInfo.REASON_ANR:
                return "ANR";
            case ApplicationExitInfo.REASON_INITIALIZATION_FAILURE:
                return "InitializationFailure";
            case ApplicationExitInfo.REASON_PERMISSION_CHANGE:
                return "PermissionChange";
            case ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE:
                return "ExcessiveResourceUsage";
            case ApplicationExitInfo.REASON_USER_REQUESTED:
                return "UserRequested";
            case ApplicationExitInfo.REASON_USER_STOPPED:
                return "UserStopped";
            case ApplicationExitInfo.REASON_DEPENDENCY_DIED:
                return "DependencyDied";
            case ApplicationExitInfo.REASON_OTHER:
                return "Other";
            default:
                return Integer.toString(reason);
        }
    }

    static String getInterruptionFilter(int filter) {
        switch (filter) {
            case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                return "Unknown";
            case NotificationManager.INTERRUPTION_FILTER_ALL:
                return "All";
            case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                return "Priority";
            case NotificationManager.INTERRUPTION_FILTER_NONE:
                return "None";
            case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                return "Alarms";
            default:
                return Integer.toString(filter);
        }
    }

    static <T extends Object> T getSystemService(Context context, Class<T> type) {
        return ContextCompat.getSystemService(context.getApplicationContext(), type);
    }

    static boolean hasPhotoPicker() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU ||
                (Build.VERSION.SDK_INT > Build.VERSION_CODES.R &&
                        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2));
    }

    static Boolean isOnForeground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            return null;
        try {
            ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(appProcessInfo);
            return (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                    appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE);
        } catch (Throwable ex) {
            Log.w(ex);
            return null;
        }
    }

    static boolean hasSmallMemoryClass(Context context) {
        try {
            ActivityManager am = Helper.getSystemService(context, ActivityManager.class);
            return (am.getMemoryClass() < 256);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    // View

    static int getMaxTextureSize() {
        try {
            EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (display == EGL14.EGL_NO_DISPLAY) {
                Log.e("eglGetDisplay failed");
                return -1;
            }

            try {
                int[] version = new int[2];
                boolean result = EGL14.eglInitialize(display, version, 0, version, 1);
                if (!result) {
                    Log.e("eglInitialize failed");
                    return -1;
                }

                int[] attr = {
                        EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
                        EGL14.EGL_LEVEL, 0,
                        EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                        EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                        EGL14.EGL_NONE
                };
                EGLConfig[] configs = new EGLConfig[1];
                int[] count = new int[1];
                result = EGL14.eglChooseConfig(display, attr, 0,
                        configs, 0, 1, count, 0);
                if (!result || count[0] == 0) {
                    Log.e("eglChooseConfig failed");
                    return -1;
                }

                int[] surfAttr = {
                        EGL14.EGL_WIDTH, 64,
                        EGL14.EGL_HEIGHT, 64,
                        EGL14.EGL_NONE
                };
                EGLSurface surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfAttr, 0);
                if (surface == EGL14.EGL_NO_SURFACE) {
                    Log.e("eglCreatePbufferSurface failed");
                    return -1;
                }

                try {
                    int[] ctxAttrib = {
                            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                            EGL14.EGL_NONE
                    };
                    EGLContext ctx = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttrib, 0);
                    if (ctx == EGL14.EGL_NO_CONTEXT) {
                        Log.e("eglCreateContext failed");
                        return -1;
                    }

                    try {
                        result = EGL14.eglMakeCurrent(display, surface, surface, ctx);
                        if (!result) {
                            Log.e("eglMakeCurrent failed");
                            return -1;
                        }

                        try {
                            int[] maxSize = new int[1];
                            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0);
                            return maxSize[0];
                        } finally {
                            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                        }
                    } finally {
                        EGL14.eglDestroyContext(display, ctx);
                    }
                } finally {
                    EGL14.eglDestroySurface(display, surface);
                }
            } finally {
                EGL14.eglTerminate(display);
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return -1;
    }

    static int getActionBarHeight(Context context) {
        return Helper.dp2pixels(context, 56);
    }

    static int getBottomNavigationHeight(Context context) {
        int resid = context.getResources().getIdentifier("design_bottom_navigation_height", "dimen", context.getPackageName());
        if (resid <= 0)
            return Helper.dp2pixels(context, 56);
        else
            return context.getResources().getDimensionPixelSize(resid);
    }

    static Snackbar.SnackbarLayout findSnackbarLayout(View rootView) {
        if (rootView instanceof Snackbar.SnackbarLayout)
            return (Snackbar.SnackbarLayout) rootView;

        if (rootView instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) rootView).getChildCount(); i++)
                if (findSnackbarLayout(((ViewGroup) rootView).getChildAt(i)) != null)
                    return findSnackbarLayout(((ViewGroup) rootView).getChildAt(i));
            return null;
        }

        return null;
    }

    static @NonNull List<View> getViewsWithTag(@NonNull View view, @NonNull String tag) {
        List<View> result = new ArrayList<>();
        if (view != null && tag.equals(view.getTag()))
            result.add(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i <= group.getChildCount(); i++)
                result.addAll(getViewsWithTag(group.getChildAt(i), tag));
        }
        return result;
    }

    static ObjectAnimator getFabAnimator(View fab, LifecycleOwner owner) {
        ObjectAnimator.AnimatorUpdateListener listener = new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;
                fab.setScaleX((float) animation.getAnimatedValue());
                fab.setScaleY((float) animation.getAnimatedValue());
            }
        };

        ObjectAnimator animator = ObjectAnimator.ofFloat(fab, "alpha", 0.9f, 1.1f);
        animator.setDuration(750L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(listener);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                try {
                    animator.removeUpdateListener(listener);
                    owner.getLifecycle().removeObserver(this);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });

        return animator;
    }

    static Intent getChooser(Context context, Intent intent) {
        return getChooser(context, intent, false);
    }

    static Intent getChooser(Context context, Intent intent, boolean share) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean app_chooser = prefs.getBoolean("app_chooser", false);
        boolean app_chooser_share = prefs.getBoolean("app_chooser_share", false);
        if (share ? !app_chooser_share : !app_chooser)
            return intent;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            PackageManager pm = context.getPackageManager();
            if (pm.queryIntentActivities(intent, 0).size() == 1)
                return intent;
        }

        return Intent.createChooser(intent, context.getString(R.string.title_select_app));
    }

    static void share(Context context, File file, String type, String name) {
        // https://developer.android.com/reference/androidx/core/content/FileProvider
        Uri uri = FileProviderEx.getUri(context, BuildConfig.APPLICATION_ID, file, name);
        share(context, uri, type, name);
    }

    static void share(Context context, Uri uri, String type, String name) {
        try {
            _share(context, uri, type, name);
        } catch (Throwable ex) {
            // java.lang.IllegalArgumentException: Failed to resolve canonical path for ...
            Log.e(ex);
        }
    }

    private static void _share(Context context, Uri uri, String type, String name) {
        Log.i("uri=" + uri + " type=" + type);

        // Build intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndTypeAndNormalize(uri, type);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean share_task = prefs.getBoolean("share_task", false);
        if (share_task)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (launchAdjacent(context, true))
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK);

        if (!TextUtils.isEmpty(name))
            intent.putExtra(Intent.EXTRA_TITLE, Helper.sanitizeFilename(name));
        Log.i("Intent=" + intent + " type=" + type);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // Get targets
            List<ResolveInfo> ris = null;
            try {
                PackageManager pm = context.getPackageManager();
                int flags = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? 0 : PackageManager.MATCH_ALL);
                ris = pm.queryIntentActivities(intent, flags);
                for (ResolveInfo ri : ris) {
                    Log.i("Target=" + ri);
                    context.grantUriPermission(ri.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            } catch (Throwable ex) {
                Log.e(ex);
                /*
                    java.lang.RuntimeException: Package manager has died
                      at android.app.ApplicationPackageManager.queryIntentActivitiesAsUser(ApplicationPackageManager.java:571)
                      at android.app.ApplicationPackageManager.queryIntentActivities(ApplicationPackageManager.java:557)
                      at eu.faircode.email.Helper.share(SourceFile:489)
                 */
            }

            // Check if viewer available
            if (ris == null || ris.size() == 0)
                if (isTnef(type, null))
                    viewFAQ(context, 155);
                else
                    reportNoViewer(context, intent, null);
            else
                context.startActivity(getChooser(context, intent, true));
        } else
            context.startActivity(getChooser(context, intent, true));
    }

    static boolean isTnef(String type, String name) {
        // https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format
        if ("application/ms-tnef".equals(type) ||
                "application/vnd.ms-tnef".equals(type))
            return true;

        if ("application/octet-stream".equals(type) &&
                "winmail.dat".equalsIgnoreCase(name))
            return true;

        return false;
    }

    static void view(Context context, Intent intent) {
        Uri uri = intent.getData();
        if (UriHelper.isHyperLink(uri))
            view(context, intent.getData(), false);
        else
            try {
                context.startActivity(intent);
            } catch (Throwable ex) {
                reportNoViewer(context, intent, ex);
            }
    }

    static void view(Context context, Uri uri, boolean browse) {
        view(context, uri, null, browse, false);
    }

    static void view(Context context, Uri uri, boolean browse, boolean task) {
        view(context, uri, null, browse, task);
    }

    static void view(Context context, Uri uri, String mimeType, boolean browse, boolean task) {
        if (context == null) {
            Log.e(new Throwable("view"));
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String open_with_pkg = prefs.getString("open_with_pkg", null);
        boolean open_with_tabs = prefs.getBoolean("open_with_tabs", true);

        EntityLog.log(context, "View=" + uri +
                " browse=" + browse +
                " task=" + task +
                " pkg=" + open_with_pkg + ":" + open_with_tabs +
                " isHyperLink=" + UriHelper.isHyperLink(uri) +
                " isInstalled=" + isInstalled(context, open_with_pkg) +
                " hasCustomTabs=" + hasCustomTabs(context, uri, open_with_pkg));

        if ("file".equals(uri.getScheme())) {
            reportNoViewer(context, uri, new SecurityException("Cannot open files"));
            return;
        }

        if (UriHelper.isHyperLink(uri))
            uri = UriHelper.fix(uri);
        else {
            open_with_pkg = null;
            open_with_tabs = false;
        }

        if (!"chooser".equals(open_with_pkg)) {
            if (open_with_pkg != null && !isInstalled(context, open_with_pkg))
                open_with_pkg = null;

            if (open_with_tabs && !hasCustomTabs(context, uri, open_with_pkg))
                open_with_tabs = false;
        }

        Intent view = new Intent(Intent.ACTION_VIEW);
        if (mimeType == null)
            view.setData(uri);
        else
            view.setDataAndType(uri, mimeType);
        if (task)
            view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (launchAdjacent(context, false))
            view.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK);

        if ("chooser".equals(open_with_pkg) && !open_with_tabs) {
            try {
                EntityLog.log(context, "Launching chooser uri=" + uri +
                        " intent=" + view +
                        " extras=" + TextUtils.join(", ", Log.getExtras(view.getExtras())));
                Intent chooser = Intent.createChooser(view, context.getString(R.string.title_select_app));
                context.startActivity(chooser);
            } catch (ActivityNotFoundException ex) {
                Log.w(ex);
                reportNoViewer(context, uri, ex);
            }
        } else if (browse || !open_with_tabs) {
            try {
                if (!"chooser".equals(open_with_pkg))
                    view.setPackage(open_with_pkg);
                EntityLog.log(context, "Launching view uri=" + uri +
                        " intent=" + view +
                        " extras=" + TextUtils.join(", ", Log.getExtras(view.getExtras())));
                context.startActivity(view);
            } catch (Throwable ex) {
                reportNoViewer(context, uri, ex);
            }
        } else {
            int colorPrimary = resolveColor(context, androidx.appcompat.R.attr.colorPrimary);
            int colorPrimaryDark = resolveColor(context, androidx.appcompat.R.attr.colorPrimaryDark);

            CustomTabColorSchemeParams.Builder schemes = new CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(colorPrimary)
                    .setSecondaryToolbarColor(colorPrimaryDark)
                    .setNavigationBarColor(colorPrimaryDark);

            // https://developer.chrome.com/multidevice/android/customtabs
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder()
                    .setDefaultColorSchemeParams(schemes.build())
                    .setColorScheme(Helper.isDarkTheme(context)
                            ? CustomTabsIntent.COLOR_SCHEME_DARK
                            : CustomTabsIntent.COLOR_SCHEME_LIGHT)
                    .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                    .setUrlBarHidingEnabled(true)
                    .setSendToExternalDefaultHandlerEnabled(true)
                    .setStartAnimations(context, R.anim.activity_open_enter, R.anim.activity_open_exit)
                    .setExitAnimations(context, R.anim.activity_close_enter, R.anim.activity_close_exit);

            Locale locale = Locale.getDefault();
            Locale slocale = Resources.getSystem().getConfiguration().locale;

            List<String> languages = new ArrayList<>();
            languages.add(locale.toLanguageTag() + ";q=1.0");
            if (!TextUtils.isEmpty(locale.getLanguage()))
                languages.add(locale.getLanguage() + ";q=0.9");
            if (!slocale.equals(locale)) {
                languages.add(slocale.toLanguageTag() + ";q=0.8");
                if (!TextUtils.isEmpty(slocale.getLanguage()))
                    languages.add(slocale.getLanguage() + ";q=0.7");
            }
            languages.add("*;q=0.5");

            Bundle headers = new Bundle();
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language
            headers.putString("Accept-Language", TextUtils.join(", ", languages));

            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.putExtra(Browser.EXTRA_HEADERS, headers);
            if (!"chooser".equals(open_with_pkg))
                customTabsIntent.intent.setPackage(open_with_pkg);

            try {
                EntityLog.log(context, "Launching tab uri=" + uri +
                        " intent=" + customTabsIntent.intent +
                        " extras=" + TextUtils.join(", ", Log.getExtras(customTabsIntent.intent.getExtras())));
                customTabsIntent.launchUrl(context, uri);
            } catch (Throwable ex) {
                reportNoViewer(context, uri, ex);
            }
        }
    }

    private static boolean launchAdjacent(Context context, boolean document) {
        // https://developer.android.com/guide/topics/large-screens/multi-window-support#launch_adjacent
        Configuration config = context.getResources().getConfiguration();
        boolean portrait = (config.orientation == Configuration.ORIENTATION_PORTRAIT);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return (prefs.getBoolean("adjacent_" + (portrait ? "portrait" : "landscape"), false) &&
                prefs.getBoolean("adjacent_" + (document ? "documents" : "links"), document));
    }

    static boolean customTabsWarmup(Context context) {
        if (context == null)
            return false;

        try {
            /*
                E AndroidRuntime: FATAL EXCEPTION: main
                E AndroidRuntime: Process: eu.faircode.email.debug, PID: 25922
                E AndroidRuntime: java.lang.IllegalStateException: Custom Tabs Service connected before an applicationcontext has been provided.
                E AndroidRuntime: 	at androidx.browser.customtabs.CustomTabsServiceConnection.onServiceConnected(CustomTabsServiceConnection.java:52)
                E AndroidRuntime: 	at android.app.LoadedApk$ServiceDispatcher.doConnected(LoadedApk.java:2198)
                E AndroidRuntime: 	at android.app.LoadedApk$ServiceDispatcher$RunConnection.run(LoadedApk.java:2231)
                E AndroidRuntime: 	at android.os.Handler.handleCallback(Handler.java:958)
                E AndroidRuntime: 	at android.os.Handler.dispatchMessage(Handler.java:99)
                E AndroidRuntime: 	at android.os.Looper.loopOnce(Looper.java:205)
                E AndroidRuntime: 	at android.os.Looper.loop(Looper.java:294)
                E AndroidRuntime: 	at android.app.ActivityThread.main(ActivityThread.java:8177)
                E AndroidRuntime: 	at java.lang.reflect.Method.invoke(Native Method)
                E AndroidRuntime: 	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:552)
                E AndroidRuntime: 	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:971)
             */
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String open_with_pkg = prefs.getString("open_with_pkg", null);
            boolean open_with_tabs = prefs.getBoolean("open_with_tabs", true);
            if (open_with_tabs && !TextUtils.isEmpty(open_with_pkg)) {
                Log.i("Warming up " + open_with_pkg);
                return CustomTabsClient.connectAndInitialize(context, open_with_pkg);
            }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return false;
    }

    static String getFAQLocale() {
        switch (Locale.getDefault().getLanguage()) {
            case "de":
                return "de-rDE";
            case "fr":
                return "fr-rFR";
            case "it":
                return "it-rIT";
            case "ro":
                return "ro-rRO";
            default:
                return null;
        }
    }

    static void viewFAQ(Context context, int question) {
        viewFAQ(context, question, true /* Google translate */);
    }

    private static void viewFAQ(Context context, int question, boolean english) {
        // Redirection is done to prevent text editors from opening the link
        // https://email.faircode.eu/faq -> https://github.com/M66B/FairEmail/blob/master/FAQ.md
        // https://email.faircode.eu/docs -> https://github.com/M66B/FairEmail/tree/master/docs
        // https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq1
        // https://github.com/M66B/FairEmail/blob/master/docs/FAQ-de-rDE.md#user-content-faq1

        String base;
        String locale = (english ? null : getFAQLocale());
        if (locale == null)
            base = "https://m66b.github.io/FairEmail/";
        else
            base = "https://email.faircode.eu/docs/FAQ-" + locale + ".md";

        if (question == 0)
            view(context, Uri.parse(base + "#top"), "text/html", false, false);
        else
            view(context, Uri.parse(base + "#faq" + question), "text/html", false, false);
    }

    static Uri getPrivacyUri(Context context) {
        // https://translate.google.com/translate?sl=auto&tl=<language>&u=<url>
        return Uri.parse(PRIVACY_URI)
                .buildUpon()
                .appendQueryParameter("language", Locale.getDefault().getLanguage())
                .appendQueryParameter("tag", Locale.getDefault().toLanguageTag())
                .build();
    }

    private static String getAnnotatedVersion(Context context) {
        return BuildConfig.VERSION_NAME + BuildConfig.REVISION + "/" +
                (Helper.hasValidFingerprint(context) ? "1" : "3") +
                (BuildConfig.PLAY_STORE_RELEASE ? "p" : "") +
                (BuildConfig.DEBUG ? "d" : "") +
                (ConnectionHelper.vpnActive(context) ? "v" : "") +
                (ActivityBilling.isPro(context) ? "+" : "");
    }

    static Uri getSupportUri(Context context, String reference) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = prefs.getString("language", null);
        Locale slocale = Resources.getSystem().getConfiguration().locale;

        return Uri.parse(SUPPORT_URI)
                .buildUpon()
                .appendQueryParameter("version", getAnnotatedVersion(context))
                .appendQueryParameter("locale", slocale.toString())
                .appendQueryParameter("language", language == null ? "" : language)
                .appendQueryParameter("installed", Helper.hasValidFingerprint(context) ? "" : "Other")
                .appendQueryParameter("reference", reference)
                .build();
    }

    static Intent getIntentIssue(Context context, String reference) {
        if (ActivityBilling.isPro(context) && false) {
            String version = getAnnotatedVersion(context);
            Intent intent = new Intent(Intent.ACTION_SEND);
            //intent.setPackage(BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");

            try {
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Log.myAddress().getAddress()});
            } catch (UnsupportedEncodingException ex) {
                Log.w(ex);
            }

            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.title_issue_subject, version));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String language = prefs.getString("language", null);
            boolean reporting = prefs.getBoolean("crash_reports", false);
            String uuid = prefs.getString("uuid", null);
            Locale slocale = Resources.getSystem().getConfiguration().locale;

            String html = "<br><br>";

            html += "<p style=\"font-size:small;\">";
            html += "Android: " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")<br>";
            html += "Device: " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.DEVICE + "<br>";
            html += "Locale: " + Html.escapeHtml(slocale.toString()) + "<br>";
            if (language != null)
                html += "Language: " + Html.escapeHtml(language) + "<br>";
            if ((reporting || Log.isTestRelease()) && uuid != null)
                html += "UUID: " + Html.escapeHtml(uuid) + "<br>";
            html += "</p>";

            intent.putExtra(Intent.EXTRA_TEXT, HtmlHelper.getText(context, html));
            intent.putExtra(Intent.EXTRA_HTML_TEXT, html);

            return intent;
        } else {
            if (Helper.hasValidFingerprint(context) || true)
                return new Intent(Intent.ACTION_VIEW, getSupportUri(context, reference));
            else
                return new Intent(Intent.ACTION_VIEW, Uri.parse(XDA_URI));
        }
    }

    static Intent getIntentRate(Context context) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
    }

    static String getInstallerName(Context context) {
        if ("?".equals(installerName))
            try {
                PackageManager pm = context.getPackageManager();
                installerName = pm.getInstallerPackageName(BuildConfig.APPLICATION_ID);
            } catch (Throwable ex) {
                Log.e(ex);
                installerName = null;
            }
        return installerName;
    }

    static long getInstallTime(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(BuildConfig.APPLICATION_ID, 0);
            if (pi != null)
                return pi.firstInstallTime;
        } catch (Throwable ex) {
            Log.e(ex);
        }
        return 0;
    }

    static long getUpdateTime(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(BuildConfig.APPLICATION_ID, 0);
            if (pi != null)
                return pi.lastUpdateTime;
        } catch (Throwable ex) {
            Log.e(ex);
        }
        return 0;
    }

    static int getTargetSdk(Context context) {
        if (targetSdk == null)
            try {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
                targetSdk = ai.targetSdkVersion;
            } catch (Throwable ex) {
                Log.e(ex);
                targetSdk = Build.VERSION.SDK_INT;
            }
        return targetSdk;
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

        /*
            Brand: HUAWEI
            Manufacturer: HUAWEI
            Model: BAH2-L09
            Product: BAH2-L09
            Device: HWBAH2
            Android: 8.0.0

            java.lang.ArrayIndexOutOfBoundsException: length=3; index=-1
            at android.text.DynamicLayout.getBlockIndex(DynamicLayout.java:646)
            at android.widget.Editor.drawHardwareAccelerated(Editor.java:1744)
            at android.widget.Editor.onDraw(Editor.java:1713)
            at android.widget.TextView.onDraw(TextView.java:7051)
            at eu.faircode.email.FixedEditText.onDraw(SourceFile:1)
            at android.view.View.draw(View.java:19314)
            at android.view.View.updateDisplayListIfDirty(View.java:18250)
            at android.view.View.draw(View.java:19042)
            at android.view.ViewGroup.drawChild(ViewGroup.java:4271)
            at android.view.ViewGroup.dispatchDraw(ViewGroup.java:4054)
            at androidx.constraintlayout.widget.ConstraintLayout.dispatchDraw(SourceFile:5)
            at android.view.View.updateDisplayListIfDirty(View.java:18241)
            at android.view.ViewGroup.recreateChildDisplayList(ViewGroup.java:4252)
            at android.view.ViewGroup.dispatchGetDisplayList(ViewGroup.java:4232)
            at android.view.View.updateDisplayListIfDirty(View.java:18209)
            at android.view.View.draw(View.java:19042)
            at android.view.ViewGroup.drawChild(ViewGroup.java:4271)
            at android.view.ViewGroup.dispatchDraw(ViewGroup.java:4054)
            at androidx.constraintlayout.widget.ConstraintLayout.dispatchDraw(SourceFile:5)
            at android.view.View.updateDisplayListIfDirty(View.java:18241)
            at android.view.View.draw(View.java:19042)
            at android.view.ViewGroup.drawChild(ViewGroup.java:4271)
            at androidx.coordinatorlayout.widget.CoordinatorLayout.drawChild(SourceFile:17)
            at android.view.ViewGroup.dispatchDraw(ViewGroup.java:4054)
            at android.view.View.draw(View.java:19317)
         */
        if ("HWBAH2".equals(Build.DEVICE))
            return false;

        return true;
    }

    static boolean isGoogle() {
        return "Google".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isPixelBeta() {
        return (isGoogle() &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                Build.PRODUCT != null && Build.PRODUCT.endsWith("_beta"));
    }

    static boolean isSamsung() {
        return "Samsung".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isOnePlus() {
        return "OnePlus".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isHuawei() {
        return "HUAWEI".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isXiaomi() {
        return "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isZte() {
        return "ZTE".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isRedmiNote() {
        // Manufacturer: Xiaomi
        // Model: Redmi Note 8 Pro
        // Model: Redmi Note 10S
        return isXiaomi() &&
                !TextUtils.isEmpty(Build.MODEL) &&
                Build.MODEL.toLowerCase(Locale.ROOT).contains("redmi") &&
                Build.MODEL.toLowerCase(Locale.ROOT).contains("note");
    }

    static boolean isMeizu() {
        return "Meizu".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isAsus() {
        return "asus".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isWiko() {
        return "WIKO".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isLenovo() {
        return "LENOVO".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isOppo() {
        return "OPPO".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isVivo() {
        return "vivo".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isRealme() {
        return "realme".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isBlackview() {
        return "Blackview".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isSony() {
        return "sony".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isUnihertz() {
        return "Unihertz".equalsIgnoreCase(Build.MANUFACTURER);
    }

    static boolean isSurfaceDuo() {
        return (isSurfaceDuo2() ||
                ("Microsoft".equalsIgnoreCase(Build.MANUFACTURER) && "Surface Duo".equals(Build.MODEL)));
    }

    static boolean isSurfaceDuo2() {
        /*
            Brand: surface
            Manufacturer: Microsoft
            Model: Surface Duo 2
            Product: duo2
            Device: duo2
         */
        return ("Microsoft".equalsIgnoreCase(Build.MANUFACTURER) && "Surface Duo 2".equals(Build.MODEL));
    }

    static boolean isFold6() {
        return ("Samsung".equalsIgnoreCase(Build.MANUFACTURER) && "SM-F956U1".equals(Build.MODEL));
    }

    static boolean isArc() {
        // https://github.com/google/talkback/blob/master/utils/src/main/java/com/google/android/accessibility/utils/FeatureSupport.java
        return (Build.DEVICE != null) && Build.DEVICE.matches(".+_cheets|cheets_.+");
    }

    static boolean canFold(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static boolean isWatch(Context context) {
        if (isSmartwatch == null)
            isSmartwatch = _isWatch(context);
        return isSmartwatch;
    }

    private static boolean _isWatch(Context context) {
        try {
            UiModeManager uimm = Helper.getSystemService(context, UiModeManager.class);
            if (uimm == null)
                return false;
            int uiModeType = uimm.getCurrentModeType();
            return (uiModeType == Configuration.UI_MODE_TYPE_WATCH);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static boolean isStaminaEnabled(Context context) {
        // https://dontkillmyapp.com/sony
        if (!isSony())
            return false;

        try {
            ContentResolver resolver = context.getContentResolver();
            return (Settings.Secure.getInt(resolver, "somc.stamina_mode", 0) > 0);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static boolean isKilling() {
        // https://dontkillmyapp.com/
        return (isSamsung() ||
                isOnePlus() ||
                isHuawei() ||
                isXiaomi() ||
                isMeizu() ||
                isAsus() ||
                isWiko() ||
                isLenovo() ||
                isOppo() ||
                isVivo() ||
                isRealme() ||
                isBlackview() ||
                isSony() ||
                isUiThread());
    }

    static boolean isAggressivelyKilling() {
        return (BuildConfig.DEBUG ||
                isSamsung() ||
                isOnePlus() ||
                isHuawei() ||
                isXiaomi() ||
                isMeizu() ||
                isAsus());
    }

    static boolean isAndroid12() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S);
    }

    static boolean isAndroid15() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE);
    }

    static String getMIUIVersion() {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            get.setAccessible(true);
            String miui = (String) get.invoke(c, "ro.miui.ui.version.code");
            return (TextUtils.isEmpty(miui) ? null : miui);
        } catch (Throwable ex) {
            Log.w(ex);
            return null;
        }
    }

    static String getUiModeType(Context context) {
        try {
            UiModeManager uimm = Helper.getSystemService(context, UiModeManager.class);
            int uiModeType = uimm.getCurrentModeType();
            switch (uiModeType & Configuration.UI_MODE_TYPE_MASK) {
                case Configuration.UI_MODE_TYPE_UNDEFINED:
                    return "undefined";
                case Configuration.UI_MODE_TYPE_NORMAL:
                    return "normal";
                case Configuration.UI_MODE_TYPE_DESK:
                    return "desk";
                case Configuration.UI_MODE_TYPE_CAR:
                    return "car";
                case Configuration.UI_MODE_TYPE_TELEVISION:
                    return "television";
                case Configuration.UI_MODE_TYPE_APPLIANCE:
                    return "appliance";
                case Configuration.UI_MODE_TYPE_WATCH:
                    return "watch";
                case Configuration.UI_MODE_TYPE_VR_HEADSET:
                    return "vr headset";
                default:
                    return Integer.toString(uiModeType);
            }
        } catch (Throwable ex) {
            Log.w(ex);
            return null;
        }
    }

    static void reportNoViewer(Context context, @NonNull Uri uri, @Nullable Throwable ex) {
        reportNoViewer(context, new Intent().setData(uri), ex);
    }

    static void reportNoViewer(Context context, @NonNull Intent intent, @Nullable Throwable ex) {
        if (context == null)
            return;

        if (ex != null) {
            if (ex instanceof ActivityNotFoundException && BuildConfig.PLAY_STORE_RELEASE)
                Log.w(ex);
            else
                Log.e(ex);
        }

        if (Helper.isTnef(intent.getType(), null)) {
            Helper.viewFAQ(context, 155);
            return;
        }

        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_no_viewer, null);
        TextView tvName = dview.findViewById(R.id.tvName);
        TextView tvFullName = dview.findViewById(R.id.tvFullName);
        TextView tvType = dview.findViewById(R.id.tvType);
        TextView tvException = dview.findViewById(R.id.tvException);

        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        Uri data = intent.getData();
        String type = intent.getType();
        String fullName = (data == null ? intent.toString() : data.getLastPathSegment());
        String extension = (data == null ? null : getExtension(data.getLastPathSegment()));

        tvName.setText(title == null ? fullName : title);
        tvFullName.setText(fullName);
        tvFullName.setVisibility(title == null ? View.GONE : View.VISIBLE);

        tvType.setText(type);

        tvException.setText(ex == null ? null : new ThrowableWrapper(ex).toSafeString());
        tvException.setVisibility(ex == null ? View.GONE : View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dview)
                .setNeutralButton(R.string.menu_faq, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Helper.getSupportUri(context, "Report:viewer");
                        view(context, uri, true);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        if (hasPlayStore(context) && !TextUtils.isEmpty(extension)) {
            builder.setPositiveButton(R.string.title_no_viewer_search, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        Uri search = Uri.parse(PLAY_STORE_SEARCH)
                                .buildUpon()
                                .appendQueryParameter("q", extension)
                                .build();
                        Intent intent = new Intent(Intent.ACTION_VIEW, search);
                        context.startActivity(intent);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });
        }

        builder.show();
    }

    static void excludeFromRecents(Context context) {
        try {
            ActivityManager am = Helper.getSystemService(context, ActivityManager.class);
            if (am == null)
                return;

            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            if (tasks == null || tasks.size() == 0)
                return;

            tasks.get(0).setExcludeFromRecents(true);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static int getOffset(TextView widget, Spannable buffer, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();

        x += widget.getScrollX();
        y += widget.getScrollY();

        Layout layout = widget.getLayout();
        int line = layout.getLineForVertical(y);
        return layout.getOffsetForHorizontal(line, x);
    }

    static String getWho(Fragment fragment) {
        String who;
        try {
            Class<?> cls = fragment.getClass();
            while (!cls.isAssignableFrom(Fragment.class))
                cls = cls.getSuperclass();
            Field f = cls.getDeclaredField("mWho");
            f.setAccessible(true);
            return (String) f.get(fragment);
        } catch (Throwable ex) {
            Log.w(ex);
            String we = fragment.toString();
            int pa = we.indexOf('(');
            int sp = we.indexOf(' ', pa);
            return we.substring(pa + 1, sp);
        }
    }

    static String getRequestKey(Fragment fragment) {
        return fragment.getClass().getName() + ":result:" + getWho(fragment);
    }

    static void setColor(Drawable drawable, int color) {
        drawable = drawable.mutate();
        if (drawable instanceof ShapeDrawable)
            ((ShapeDrawable) drawable).getPaint().setColor(color);
        else if (drawable instanceof GradientDrawable)
            ((GradientDrawable) drawable).setColor(color);
        else if (drawable instanceof ColorDrawable)
            ((ColorDrawable) drawable).setColor(color);
    }

    static void clearViews(Object instance) {
        try {
            String cname = instance.getClass().getSimpleName();
            for (Field field : instance.getClass().getDeclaredFields()) {
                String fname = cname + ":" + field.getName();

                Class<?> ftype = field.getType();
                Class<?> type = (ftype.isArray() ? ftype.getComponentType() : ftype);

                if (type == null) {
                    Log.e(fname + "=null");
                    continue;
                }

                if (instance instanceof FragmentDialogPrint &&
                        WebView.class.isAssignableFrom(type)) {
                    Log.i(fname + " clear skip");
                    continue;
                }

                if (View.class.isAssignableFrom(type) ||
                        Animator.class.isAssignableFrom(type) ||
                        Snackbar.class.isAssignableFrom(type) ||
                        SelectionTracker.class.isAssignableFrom(type) ||
                        SelectionTracker.SelectionPredicate.class.isAssignableFrom(type) ||
                        PagerAdapter.class.isAssignableFrom(type) ||
                        RecyclerView.Adapter.class.isAssignableFrom(type) ||
                        TwoStateOwner.class.isAssignableFrom(type))
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Clearing ").append(fname);

                        field.setAccessible(true);

                        if (!ftype.isArray()) {
                            try {
                                if (View.class.isAssignableFrom(type)) {
                                    View v = (View) field.get(instance);
                                    if (v != null) {
                                        sb.append(" tag");
                                        v.setTag(null);
                                    }
                                }

                                if (TextView.class.isAssignableFrom(type)) {
                                    TextView tv = (TextView) field.get(instance);
                                    if (tv != null) {
                                        sb.append(" drawables");
                                        tv.setCompoundDrawables(null, null, null, null);
                                    }
                                }

                                if (ImageView.class.isAssignableFrom(type)) {
                                    ImageView iv = (ImageView) field.get(instance);
                                    if (iv != null) {
                                        sb.append(" drawable");
                                        iv.setImageDrawable(null);
                                    }
                                }

                                if (WebView.class.isAssignableFrom(type)) {
                                    WebView wv = (WebView) field.get(instance);
                                    if (wv != null) {
                                        sb.append(" html");
                                        wv.loadDataWithBaseURL(null, "", "text/html", StandardCharsets.UTF_8.name(), null);
                                    }
                                }

                                if (Animator.class.isAssignableFrom(type)) {
                                    Animator animator = (Animator) field.get(instance);
                                    if (animator != null) {
                                        sb.append(" animator");
                                        if (animator.isStarted())
                                            animator.cancel();
                                        animator.setTarget(null);
                                    }
                                }

                                if (Snackbar.class.isAssignableFrom(type)) {
                                    Snackbar snackbar = (Snackbar) field.get(instance);
                                    if (snackbar != null) {
                                        sb.append(" action");
                                        snackbar.setAction(null, null);
                                    }
                                }
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                        }

                        field.set(instance, null);

                        Log.i(sb.toString());
                    } catch (Throwable ex) {
                        Log.e(new Throwable(fname, ex));
                    }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static Bundle getBackgroundActivityOptions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return null;
        else {
            ActivityOptions options = ActivityOptions.makeBasic();
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU)
                options.setPendingIntentBackgroundActivityLaunchAllowed(true);
            else
                options.setPendingIntentBackgroundActivityStartMode(MODE_BACKGROUND_ACTIVITY_START_ALLOWED);
            return options.toBundle();
        }
    }

    static Fragment recreateFragment(Fragment fragment, FragmentManager fm) {
        try {
            Fragment.SavedState savedState = fm.saveFragmentInstanceState(fragment);
            Bundle args = fragment.getArguments();

            Fragment newFragment = fragment.getClass().newInstance();
            newFragment.setInitialSavedState(savedState);
            newFragment.setArguments(args);

            return newFragment;
        } catch (Throwable e) {
            throw new RuntimeException("Cannot recreate fragment=" + fragment, e);
        }
    }

    static void performHapticFeedback(View view, int feedbackConstant) {
        performHapticFeedback(view, feedbackConstant, 0);
    }

    static void performHapticFeedback(@NonNull View view, int feedbackConstant, int flags) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
            boolean haptic_feedback = prefs.getBoolean("haptic_feedback", true);
            if (haptic_feedback)
                view.performHapticFeedback(feedbackConstant);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    // Graphics

    static int dp2pixels(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }

    static int pixels2dp(Context context, float pixels) {
        float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(pixels / scale);
    }

    static float getTextSize(Context context, int zoom) {
        TypedArray ta = null;
        try {
            if (zoom == 0)
                ta = context.obtainStyledAttributes(
                        androidx.appcompat.R.style.TextAppearance_AppCompat_Small, new int[]{android.R.attr.textSize});
            else if (zoom == 2)
                ta = context.obtainStyledAttributes(
                        androidx.appcompat.R.style.TextAppearance_AppCompat_Large, new int[]{android.R.attr.textSize});
            else
                ta = context.obtainStyledAttributes(
                        androidx.appcompat.R.style.TextAppearance_AppCompat_Medium, new int[]{android.R.attr.textSize});
            return ta.getDimension(0, 0);
        } finally {
            if (ta != null)
                ta.recycle();
        }
    }

    static int resolveColor(Context context, int attr) {
        return resolveColor(context, attr, 0xFF0000);
    }

    static int resolveColor(Context context, int attr, int def) {
        int[] attrs = new int[]{attr};
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs);
        int color = a.getColor(0, def);
        a.recycle();
        return color;
    }

    static void setViewsEnabled(ViewGroup view, boolean enabled) {
        for (int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);
            if ("ignore".equals(child.getTag()))
                continue;
            if (child instanceof Spinner ||
                    child instanceof EditText ||
                    child instanceof CheckBox ||
                    child instanceof ImageView /* =ImageButton */ ||
                    child instanceof RadioButton ||
                    (child instanceof Button && "disable".equals(child.getTag()))) {
                if (child instanceof ImageView && ((ImageView) child).getImageTintList() != null)
                    child.setAlpha(enabled ? 1.0f : LOW_LIGHT);
                child.setEnabled(enabled);
            } else if (child instanceof BottomNavigationView) {
                Menu menu = ((BottomNavigationView) child).getMenu();
                menu.setGroupEnabled(0, enabled);
            } else if (child instanceof RecyclerView)
                ; // do nothing
            else if (child instanceof ViewGroup)
                setViewsEnabled((ViewGroup) child, enabled);
        }
    }

    static void hide(View view) {
        view.setPadding(1, 1, 0, 0);

        ViewGroup.LayoutParams lparam = view.getLayoutParams();
        lparam.width = 1;
        lparam.height = 1;
        if (lparam instanceof ConstraintLayout.LayoutParams)
            ((ConstraintLayout.LayoutParams) lparam).setMargins(0, 0, 0, 0);
        view.setLayoutParams(lparam);
    }

    static Snackbar setSnackbarOptions(Snackbar snackbar) {
        snackbar.setGestureInsetBottomIgnored(true);
        int colorAccent = Helper.resolveColor(snackbar.getContext(), android.R.attr.colorAccent);
        double lum = ColorUtils.calculateLuminance(colorAccent);
        if (lum < MIN_SNACKBAR_LUMINANCE) {
            colorAccent = ColorUtils.blendARGB(colorAccent, Color.WHITE, MIN_SNACKBAR_LUMINANCE);
            snackbar.setActionTextColor(colorAccent);
        }
        return snackbar;
    }

    static void setSnackbarLines(Snackbar snackbar, int lines) {
        View sv = snackbar.getView();
        if (sv == null)
            return;
        TextView tv = sv.findViewById(com.google.android.material.R.id.snackbar_text);
        if (tv == null)
            return;
        tv.setMaxLines(lines);
    }

    static boolean isNight(Context context) {
        // https://developer.android.com/guide/topics/ui/look-and-feel/darktheme#configuration_changes
        int uiMode = context.getResources().getConfiguration().uiMode;
        Log.i("UI mode=0x" + Integer.toHexString(uiMode));
        return ((uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
    }

    static boolean isDarkTheme(Context context) {
        // R.attr.isLightTheme
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.themeName, tv, true);
        return (tv.string != null && !"light".contentEquals(tv.string));
    }

    static void showKeyboard(final View view) {
        view.post(new RunnableEx("showKeyboard") {
            @Override
            protected void delegate() {
                Log.i("showKeyboard view=" + view);
                new SoftwareKeyboardControllerCompat(view).show();
            }
        });
    }

    static void hideKeyboard(final View view) {
        view.post(new RunnableEx("hideKeyboard") {
            @Override
            protected void delegate() {
                Log.i("hideKeyboard view=" + view);
                new SoftwareKeyboardControllerCompat(view).hide();
            }
        });
    }

    static boolean isKeyboardVisible(final View view) {
        try {
            if (view == null)
                return false;
            View root = view.getRootView();
            if (root == null)
                return false;
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(root);
            if (insets == null)
                return false;
            boolean visible = insets.isVisible(WindowInsetsCompat.Type.ime());
            Log.i("isKeyboardVisible=" + visible);
            return visible;
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static String getViewName(View view) {
        StringBuilder sb = new StringBuilder(_getViewName(view));
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent instanceof View)
                sb.insert(0, '/').insert(0, _getViewName((View) parent));
            parent = parent.getParent();
        }
        return sb.toString();
    }

    private static String _getViewName(View view) {
        if (view == null)
            return "<null>";
        int id = view.getId();
        if (id == View.NO_ID)
            return "";
        try {
            return view.getContext().getResources().getResourceEntryName(id);
        } catch (Throwable ex) {
            return new ThrowableWrapper(ex).toSafeString();
        }
    }

    static int getBytesPerPixel(Bitmap.Config config) {
        switch (config) {
            case ALPHA_8:
                return 1;
            case RGB_565:
                return 2;
            case ARGB_4444:
                return 4;
            case ARGB_8888:
                return 8;
            case RGBA_F16:
                return 8;
            case HARDWARE:
                return 0;
            default:
                return 8;
        }
    }

    // Formatting

    private static final DecimalFormat df = new DecimalFormat("@@");

    static String humanReadableByteCount(long bytes) {
        return humanReadableByteCount(bytes, true);
    }

    static String humanReadableByteCount(long bytes, boolean si) {
        int sign = (int) Math.signum(bytes);
        bytes = Math.abs(bytes);

        int unit = (si ? 1000 : 1024);
        if (bytes < unit)
            return sign * bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return df.format(sign * bytes / Math.pow(unit, exp)) + " " + pre + "B";
    }

    static boolean isPrintableChar(int codepoint) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(codepoint);
        if (block == null || block == Character.UnicodeBlock.SPECIALS)
            return false;
        return !Character.isISOControl(codepoint);
    }
    // https://issuetracker.google.com/issues/37054851

    static String getPrintableString(String value, boolean debug) {
        if (debug) {
            if (value == null)
                return "<null>";
            if (TextUtils.isEmpty(value))
                return "<empty>";
        } else
            return value;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); ) {
            int codepoint = value.codePointAt(i);
            if (debug && codepoint == 10)
                result.append('|');
            else if (debug && codepoint == 32)
                result.append('_');
            else if (!Helper.isPrintableChar(codepoint) || codepoint == 160)
                result.append('{').append(Integer.toHexString(codepoint)).append('}');
            else
                result.append(Character.toChars(codepoint));
            i += Character.charCount(codepoint);
        }
        return result.toString();
    }

    static DateFormat getTimeInstance(Context context) {
        return getTimeInstance(context, SimpleDateFormat.MEDIUM);
    }

    static DateFormat getTimeInstance(Context context, int style) {
        if (context != null &&
                (style == SimpleDateFormat.SHORT || style == SimpleDateFormat.MEDIUM))
            return new SimpleDateFormat(getTimePattern(context, style));
        else
            return SimpleDateFormat.getTimeInstance(style);
    }

    static DateFormat getDateInstance(Context context) {
        return getDateInstance(context, SimpleDateFormat.MEDIUM);
    }

    static DateFormat getDateInstance(Context context, int style) {
        return SimpleDateFormat.getDateInstance(style);
    }

    static DateFormat getDateTimeInstance(Context context) {
        return getDateTimeInstance(context, SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM);
    }

    static DateFormat getDateTimeInstance(Context context, int dateStyle, int timeStyle) {
        if (context != null &&
                (timeStyle == SimpleDateFormat.SHORT || timeStyle == SimpleDateFormat.MEDIUM)) {
            DateFormat dateFormat = getDateInstance(context, dateStyle);
            if (dateFormat instanceof SimpleDateFormat) {
                String datePattern = ((SimpleDateFormat) dateFormat).toPattern();
                String timePattern = getTimePattern(context, timeStyle);
                return new SimpleDateFormat(datePattern + " " + timePattern);
            }
        }

        return SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle);
    }

    static String getTimePattern(Context context, int style) {
        // https://issuetracker.google.com/issues/37054851
        boolean is24Hour = android.text.format.DateFormat.is24HourFormat(context);
        String skeleton = (is24Hour ? "Hm" : "hm");
        if (style == SimpleDateFormat.MEDIUM)
            skeleton += "s";
        return android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton);
    }

    static CharSequence getRelativeDateSpanString(Context context, long millis) {
        return getRelativeTimeSpanString(context, millis, false, true, false);
    }

    static CharSequence getRelativeTimeSpanString(Context context, long millis) {
        return getRelativeTimeSpanString(context, millis, true, false, false);
    }

    static CharSequence getRelativeDateTimeSpanString(Context context, long millis, boolean condensed) {
        return getRelativeTimeSpanString(context, millis, true, true, condensed);
    }

    private static CharSequence getRelativeTimeSpanString(Context context, long millis, boolean withTime, boolean withDate, boolean condensed) {
        Calendar cal0 = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();
        cal0.setTimeInMillis(millis);

        boolean thisYear = (cal0.get(Calendar.YEAR) == cal1.get(Calendar.YEAR));
        boolean thisMonth = (cal0.get(Calendar.MONTH) == cal1.get(Calendar.MONTH));
        boolean thisDay = (cal0.get(Calendar.DAY_OF_MONTH) == cal1.get(Calendar.DAY_OF_MONTH));
        if (withDate) {
            try {
                if (condensed && thisYear && thisMonth && thisDay)
                    return getTimeInstance(context, SimpleDateFormat.SHORT).format(millis);
                String skeleton = (thisYear ? "MMM-d" : "yyyy-M-d");
                if (withTime) {
                    boolean is24Hour = android.text.format.DateFormat.is24HourFormat(context);
                    skeleton += (is24Hour ? " Hm" : " hm");
                }
                String format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton);
                return new SimpleDateFormat(format).format(millis);
            } catch (Throwable ex) {
                Log.e(ex);
                DateFormat df = (withTime
                        ? getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
                        : getDateInstance(context, SimpleDateFormat.SHORT));
                return df.format(millis);
            }
        } else if (thisYear && thisMonth && thisDay)
            return getTimeInstance(context, SimpleDateFormat.SHORT).format(millis);
        else
            return DateUtils.getRelativeTimeSpanString(context, millis);
    }

    static String formatHour(Context context, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
        cal.set(Calendar.MINUTE, minutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return Helper.getTimeInstance(context, SimpleDateFormat.SHORT).format(cal.getTime());
    }

    static String formatDuration(long ms) {
        return formatDuration(ms, true);
    }

    static String formatDuration(long ms, boolean withFraction) {
        int sign = (ms < 0 ? -1 : 1);
        ms = Math.abs(ms);
        int days = (int) (ms / (24 * 3600 * 1000L));
        ms = ms % (24 * 3600 * 1000L);
        long seconds = ms / 1000;
        ms = ms % 1000;
        return (sign < 0 ? "-" : "") +
                (days > 0 ? days + " " : "") +
                DateUtils.formatElapsedTime(seconds) +
                (ms == 0 || !withFraction ? "" : "." + ms);
    }

    static String formatNumber(Integer number, long max, NumberFormat nf) {
        if (number == null)
            return null;
        return nf.format(Math.min(number, max)) + (number > max ? "+" : "");
    }

    static void linkPro(final TextView tv) {
        if (ActivityBilling.isPro(tv.getContext()) && !BuildConfig.DEBUG)
            hide(tv);
        else {
            tv.getPaint().setUnderlineText(true);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(
                            new Intent(v.getContext(), ActivityBilling.class));
                }
            });
        }
    }

    static String getString(Context context, String language, int resid, Object... formatArgs) {
        if (language == null)
            return context.getString(resid, formatArgs);
        return getString(context, new Locale(language), resid, formatArgs);
    }

    static String getString(Context context, Locale locale, int resid, Object... formatArgs) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        Resources res = context.createConfigurationContext(configuration).getResources();
        return res.getString(resid, formatArgs);
    }

    static String[] getStrings(Context context, int resid, Object... formatArgs) {
        return getStrings(context, null, resid, formatArgs);
    }

    static String[] getStrings(Context context, String language, int resid, Object... formatArgs) {
        List<Locale> locales = new ArrayList<>();

        if (language != null)
            locales.add(new Locale(language));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Locale l = Locale.getDefault();
            locales.add(l);
            if (!"en".equals(language) && !"en".equals(l.getLanguage()))
                locales.add(new Locale("en"));
        } else {
            LocaleList ll = context.getResources().getConfiguration().getLocales();
            for (int i = 0; i < ll.size(); i++) {
                Locale l = ll.get(i);
                locales.add(l);
            }
        }

        List<String> result = new ArrayList<>();
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        for (Locale locale : locales) {
            configuration.setLocale(locale);
            Resources res = context.createConfigurationContext(configuration).getResources();
            String text = res.getString(resid, formatArgs);
            if (!result.contains(text))
                result.add(text);
        }

        return result.toArray(new String[0]);
    }

    static String getLocalizedAsset(Context context, String name) throws IOException {
        if (name == null || !name.contains("."))
            throw new IllegalArgumentException(name);

        String[] list = context.getResources().getAssets().list("");
        if (list == null)
            throw new IllegalArgumentException();

        List<String> names = new ArrayList<>();
        String[] c = name.split("\\.");
        List<String> assets = Arrays.asList(list);

        List<Locale> locales = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            locales.add(Locale.getDefault());
        else {
            LocaleList ll = context.getResources().getConfiguration().getLocales();
            for (int i = 0; i < ll.size(); i++)
                locales.add(ll.get(i));
        }

        for (Locale locale : locales) {
            String language = locale.getLanguage();
            String country = locale.getCountry();
            if ("en".equals(language) && "US".equals(country))
                names.add(name);
            else {
                String localized = c[0] + "-" + language + "-r" + country + "." + c[1];
                if (assets.contains(localized))
                    names.add(localized);
            }
        }

        for (Locale locale : locales) {
            String prefix = c[0] + "-" + locale.getLanguage();
            for (String asset : assets)
                if (asset.startsWith(prefix))
                    names.add(asset);
        }

        names.add(name);

        String asset = names.get(0);
        Log.i("Using " + asset +
                " of " + TextUtils.join(",", names) +
                " (" + TextUtils.join(",", locales) + ")");
        return asset;
    }

    static boolean containsWhiteSpace(String text) {
        return text.matches(".*\\s+.*");
    }

    static boolean containsControlChars(String text) {
        int codePoint;
        for (int offset = 0; offset < text.length(); ) {
            codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            switch (Character.getType(codePoint)) {
                case Character.CONTROL:     // \p{Cc}
                case Character.FORMAT:      // \p{Cf}
                case Character.PRIVATE_USE: // \p{Co}
                case Character.SURROGATE:   // \p{Cs}
                case Character.UNASSIGNED:  // \p{Cn}
                    return true;
            }
        }
        return false;
    }

    static Integer parseInt(String text) {
        if (TextUtils.isEmpty(text))
            return null;

        if (!TextUtils.isDigitsOnly(text))
            return null;

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    static String toRoman(int value) {
        if (value < 0 || value >= 4000)
            return Integer.toString(value);
        return ROMAN_1000[value / 1000] +
                ROMAN_100[(value % 1000) / 100] +
                ROMAN_10[(value % 100) / 10] +
                ROMAN_1[value % 10];
    }

    static ActionMode.Callback getActionModeWrapper(TextView view) {
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                try {
                    int order = 1000;
                    menu.add(Menu.CATEGORY_SECONDARY, R.string.title_insert_contact, order++,
                            view.getContext().getString(R.string.title_insert_contact));
                    menu.add(Menu.CATEGORY_SECONDARY, R.string.title_select_block, order++,
                            view.getContext().getString(R.string.title_select_block));
                } catch (Throwable ex) {
                    Log.e(ex);
                }
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                try {
                    String selected = getSelected();
                    boolean email = (selected != null && Helper.EMAIL_ADDRESS.matcher(selected).matches());
                    menu.findItem(R.string.title_insert_contact).setVisible(email);
                } catch (Throwable ex) {
                    Log.e(ex);
                    menu.findItem(R.string.title_insert_contact).setVisible(false);
                }

                try {
                    Pair<Integer, Integer> block = StyleHelper.getParagraph(view, true);
                    boolean ablock = (block != null &&
                            block.first == view.getSelectionStart() &&
                            block.second == view.getSelectionEnd());
                    menu.findItem(R.string.title_select_block).setVisible(!ablock);
                } catch (Throwable ex) {
                    Log.e(ex);
                    menu.findItem(R.string.title_select_block).setVisible(false);
                }

                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    Intent intent = item.getIntent();
                    if (intent != null &&
                            Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
                        item.setIntent(null);
                        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                try {
                                    int start = view.getSelectionStart();
                                    int end = view.getSelectionEnd();
                                    if (start > end) {
                                        int tmp = start;
                                        start = end;
                                        end = tmp;
                                    }
                                    CharSequence selected = view.getText();
                                    if (start >= 0 && end <= selected.length())
                                        selected = selected.subSequence(start, end);
                                    intent.putExtra(Intent.EXTRA_PROCESS_TEXT, selected);
                                    view.getContext().startActivity(intent);
                                } catch (Throwable ex) {
                                    reportNoViewer(view.getContext(), intent, ex);
                                    /*
                                        java.lang.SecurityException: Permission Denial: starting Intent { act=android.intent.action.PROCESS_TEXT typ=text/plain cmp=com.microsoft.launcher/com.microsoft.bing.ProcessTextSearch launchParam=MultiScreenLaunchParams { mDisplayId=0 mFlags=0 } (has extras) } from ProcessRecord{befc028 15098:eu.faircode.email/u0a406} (pid=15098, uid=10406) not exported from uid 10021
                                            at android.os.Parcel.readException(Parcel.java:1693)
                                            at android.os.Parcel.readException(Parcel.java:1646)
                                            at android.app.ActivityManagerProxy.startActivity(ActivityManagerNative.java:3530)
                                            at android.app.Instrumentation.execStartActivity(Instrumentation.java:1645)
                                            at android.app.Activity.startActivityForResult(Activity.java:5033)
                                            at android.view.View.startActivityForResult(View.java:6413)
                                            at android.widget.Editor$ProcessTextIntentActionsHandler.fireIntent(Editor.java:7597)
                                            at android.widget.Editor$ProcessTextIntentActionsHandler.performMenuItemAction(Editor.java:7542)
                                            at android.widget.Editor$TextActionModeCallback.onActionItemClicked(Editor.java:4246)
                                            at com.android.internal.policy.DecorView$ActionModeCallback2Wrapper.onActionItemClicked(DecorView.java:2971)
                                            at com.android.internal.view.FloatingActionMode$3.onMenuItemSelected(FloatingActionMode.java:95)
                                            at com.android.internal.view.menu.MenuBuilder.dispatchMenuItemSelected(MenuBuilder.java:761)
                                            at com.android.internal.view.menu.MenuItemImpl.invoke(MenuItemImpl.java:157)
                                            at com.android.internal.view.menu.MenuBuilder.performItemAction(MenuBuilder.java:904)
                                            at com.android.internal.view.menu.MenuBuilder.performItemAction(MenuBuilder.java:894)
                                            at com.android.internal.view.FloatingActionMode$4.onMenuItemClick(FloatingActionMode.java:124)
                                            at com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$23.onItemClick(FloatingToolbar.java:1898)
                                            at android.widget.AdapterView.performItemClick(AdapterView.java:339)
                                            at android.widget.AbsListView.performItemClick(AbsListView.java:1705)
                                            at android.widget.AbsListView$PerformClick.run(AbsListView.java:4171)
                                            at android.widget.AbsListView$13.run(AbsListView.java:6735)
                                     */
                                }
                                return true;
                            }
                        });
                    }
                }

                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getGroupId() == Menu.CATEGORY_SECONDARY)
                    try {
                        int id = item.getItemId();
                        if (id == R.string.title_insert_contact) {
                            String email = getSelected();
                            String name = UriHelper.getEmailUser(email);

                            Intent insert = new Intent();
                            insert.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
                            if (!TextUtils.isEmpty(name))
                                insert.putExtra(ContactsContract.Intents.Insert.NAME, name);
                            insert.setAction(Intent.ACTION_INSERT);
                            insert.setType(ContactsContract.Contacts.CONTENT_TYPE);
                            view.getContext().startActivity(insert);
                        } else if (id == R.string.title_select_block) {
                            Pair<Integer, Integer> block = StyleHelper.getParagraph(view, true);
                            if (block != null)
                                android.text.Selection.setSelection((Spannable) view.getText(), block.first, block.second);
                            return true;
                        }
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }

            String getSelected() {
                int start = view.getSelectionStart();
                int end = view.getSelectionEnd();
                return (start >= 0 && start < end ? view.getText().subSequence(start, end).toString() : null);
            }
        };
    }

    static boolean isEndChar(char c) {
        return (isSentenceChar(c) || c == ',');
    }

    static boolean isSentenceChar(char c) {
        return (c == '.' /* Latin */ ||
                c == '。' /* Chinese */ ||
                c == ':' || c == ';' ||
                c == '?' || c == '!');
    }

    static String trim(String value, String chars) {
        if (value == null)
            return null;

        for (Character kar : chars.toCharArray()) {
            String k = kar.toString();
            while (value.startsWith(k))
                value = value.substring(1);
            while (value.endsWith(k))
                value = value.substring(0, value.length() - 1);
        }

        return value;
    }

    static String limit(String value, int max) {
        if (TextUtils.isEmpty(value) || value.length() < max)
            return value;
        return value.substring(0, max);
    }

    // Files

    static {
        System.loadLibrary("fairemail");
    }

    public static native void sync();

    private static final Map<File, Boolean> exists = new HashMap<>();

    static File ensureExists(Context context, String subdir) {
        File dir = new File(context.getFilesDir(), subdir);
        dir.mkdirs();

        synchronized (exists) {
            if (exists.containsKey(dir))
                return dir;
            exists.put(dir, true);
        }

        return dir;
    }

    static File getExternalFilesDir(Context context) {
        return getExternalFilesDir(context, null);
    }

    static File getExternalFilesDir(Context context, String type) {
        File[] dirs = ContextCompat.getExternalFilesDirs(context, type);
        if (dirs == null || dirs.length == 0)
            return context.getExternalFilesDir(type);
        else
            return dirs[0];
    }

    static String sanitizeFilename(String name) {
        if (name == null)
            return null;

        return name
                // Canonical files names cannot contain NUL
                .replace("\0", "")
                .replaceAll("[?:\"*|/\\\\<>]", "_");
    }

    static String getExtension(String filename) {
        if (filename == null)
            return null;
        int index = filename.lastIndexOf(".");
        if (index < 0)
            return null;
        return filename.substring(index + 1);
    }

    static String guessMimeType(String filename) {
        String type = null;

        String extension = Helper.getExtension(filename);
        if (extension != null) {
            extension = extension.toLowerCase(Locale.ROOT);

            if (extension.endsWith(")")) {
                int p = extension.lastIndexOf('(');
                if (p > 0 && p < extension.length() - 1)
                    if (TextUtils.isDigitsOnly(extension.substring(p + 1, extension.length() - 1)))
                        extension = extension.substring(0, p).trim();
            }

            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        if (TextUtils.isEmpty(type))
            if ("csv".equals(extension))
                return "text/csv";
            else if ("eml".equals(extension))
                return "message/rfc822";
            else if ("gpx".equals(extension))
                return "application/gpx+xml";
            else if ("log".equals(extension))
                return "text/plain";
            else if ("ovpn".equals(extension))
                return "application/x-openvpn-profile";
            else if ("mbox".equals(extension))
                return "application/mbox"; // https://tools.ietf.org/html/rfc4155
            else
                return "application/octet-stream";

        return type;
    }

    static String guessExtension(String mimeType) {
        String extension = null;

        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ROOT);
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }

        if (TextUtils.isEmpty(extension))
            if ("text/csv".equals(mimeType))
                return "csv";
            else if ("message/rfc822".equals(mimeType))
                return "eml";
            else if ("application/gpx+xml".equals(mimeType))
                return "gpx";
            else if ("application/x-openvpn-profile".equals(mimeType))
                return "ovpn";

        return extension;
    }

    @NonNull
    static UriInfo getInfo(Uri uri, Context context) {
        UriInfo result = new UriInfo();

        // https://stackoverflow.com/questions/76094229/android-13-photo-video-picker-file-name-from-the-uri-is-garbage
        DocumentFile dfile = null;
        try {
            dfile = DocumentFile.fromSingleUri(context, uri);
            if (dfile != null) {
                result.name = dfile.getName();
                result.type = dfile.getType();
                result.size = dfile.length();
                EntityLog.log(context, "UriInfo dfile " + result + " uri=" + uri);
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        // Check name
        if (TextUtils.isEmpty(result.name))
            result.name = uri.getLastPathSegment();

        // Check type
        if (!TextUtils.isEmpty(result.type))
            try {
                new ContentType(result.type);
            } catch (ParseException ex) {
                Log.w(new Throwable(result.type, ex));
                result.type = null;
            }

        if (TextUtils.isEmpty(result.type) ||
                "*/*".equals(result.type) ||
                "application/*".equals(result.type) ||
                "application/octet-stream".equals(result.type))
            result.type = Helper.guessMimeType(result.name);

        if (result.size != null && result.size <= 0)
            result.size = null;

        EntityLog.log(context, "UriInfo result " + result + " uri=" + uri);

        return result;
    }

    static class UriInfo {
        String name;
        String type;
        Long size;

        boolean isImage() {
            return ImageHelper.isImage(type);
        }

        @NonNull
        @Override
        public String toString() {
            return "name=" + name + " type=" + type + " size=" + size;
        }
    }

    static void writeText(File file, String content) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            if (content != null)
                out.write(content.getBytes());
        }
    }

    static String readStream(InputStream is) throws IOException {
        return readStream(is, StandardCharsets.UTF_8);
    }

    static String readStream(InputStream is, Charset charset) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(Math.max(BUFFER_SIZE, is.available()));
        byte[] buffer = new byte[BUFFER_SIZE];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer))
            os.write(buffer, 0, len);
        return new String(os.toByteArray(), charset);
    }

    static String readText(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return readStream(in);
        }
    }

    public static void readBuffer(InputStream is, byte[] buffer) throws IOException {
        int left = buffer.length;
        while (left > 0) {
            int count = is.read(buffer, buffer.length - left, left);
            if (count < 0)
                throw new IOException("EOF");
            left -= count;
        }
    }

    static byte[] readBytes(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(Math.max(BUFFER_SIZE, is.available()));
        byte[] buffer = new byte[BUFFER_SIZE];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer))
            os.write(buffer, 0, len);
        return os.toByteArray();
    }

    public static String readLine(InputStream is, Charset charset) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b = is.read();
        if (b < 0)
            return null;
        while (b >= 0 && b != '\n') {
            bos.write(b);
            b = is.read();
        }
        return new String(bos.toByteArray(), charset);
    }

    static void copy(File src, File dst) throws IOException {
        try (InputStream is = new FileInputStream(src)) {
            try (OutputStream os = new FileOutputStream(dst)) {
                copy(is, os);
            }
        }
    }

    static long copy(Context context, Uri uri, File file) throws IOException {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is == null)
                throw new FileNotFoundException(uri.toString());
            try (OutputStream os = new FileOutputStream(file)) {
                return copy(is, os);
            }
        }
    }

    static long copy(InputStream is, OutputStream os) throws IOException {
        long size = 0;
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        while ((len = is.read(buf)) > 0) {
            size += len;
            os.write(buf, 0, len);
        }
        return size;
    }

    static List<File> listFiles(File dir) {
        return listFiles(dir, null);
    }

    static List<File> listFiles(File dir, Long minSize) {
        List<File> files = new ArrayList<>();
        if (dir != null) {
            File[] listed = dir.listFiles();
            if (listed != null)
                for (File file : listed)
                    if (file.isDirectory())
                        files.addAll(listFiles(file, minSize));
                    else if (minSize == null || file.length() > minSize)
                        files.add(file);
        }
        return files;
    }

    static void secureDelete(File file) {
        try {
            if (file.exists()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    if (!file.delete())
                        Log.w("File not found: " + file);
                } else
                    Files.delete(Paths.get(file.getAbsolutePath()));
            }
        } catch (IOException ex) {
            Log.e(ex);
        }
    }

    static long getAvailableStorageSpace() {
        StatFs stats = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        return stats.getAvailableBlocksLong() * stats.getBlockSizeLong();
    }

    static long getTotalStorageSpace() {
        StatFs stats = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        return stats.getTotalBytes();
    }

    static long getCacheQuota(Context context) {
        // https://developer.android.com/reference/android/content/Context#getCacheDir()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            try {
                StorageManager sm = Helper.getSystemService(context, StorageManager.class);
                File cache = context.getCacheDir();
                return sm.getCacheQuotaBytes(sm.getUuidForPath(cache));
            } catch (IOException ex) {
                Log.w(ex);
            }
        return 0;
    }

    static long getSizeUsed(File dir) {
        long size = 0;
        File[] listed = dir.listFiles();
        if (listed != null)
            for (File file : listed)
                if (file.isDirectory())
                    size += getSizeUsed(file);
                else
                    size += file.length();
        return size;
    }

    static void openAdvanced(Context context, Intent intent) {
        // https://issuetracker.google.com/issues/72053350
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra("android.content.extra.FANCY", true);
        intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
        intent.putExtra("android.provider.extra.SHOW_ADVANCED", true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String default_folder = prefs.getString("default_folder", null);
        if (default_folder != null)
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(default_folder));
    }

    static class ByteArrayInOutStream extends ByteArrayOutputStream {
        public ByteArrayInOutStream() {
            super();
        }

        public ByteArrayInOutStream(int size) {
            super(size);
        }

        public ByteArrayInputStream getInputStream() {
            ByteArrayInputStream in = new ByteArrayInputStream(this.buf, 0, this.count);
            this.buf = null;
            return in;
        }
    }

    static boolean isUiThread() {
        return (Looper.myLooper() == Looper.getMainLooper());
    }

    static boolean isPersisted(Context context, Uri uri, boolean read, boolean write) {
        try {
            List<UriPermission> uperms = context.getContentResolver().getPersistedUriPermissions();
            for (UriPermission uperm : uperms)
                if (uperm.getUri().equals(uri)) {
                    boolean canRead = uperm.isReadPermission();
                    boolean canWrite = uperm.isWritePermission();
                    Log.i(uri + " read=" + read + "/" + canRead + " write=" + write + "/" + canWrite);
                    return (!read || canRead) && (!write || canWrite);
                }
            return false;
        } catch (Throwable ex) {
            Log.e(ex);
            return !BuildConfig.DEBUG;
        }
    }

    // Cryptography

    static String sha256(String data) throws NoSuchAlgorithmException {
        return sha256(data.getBytes());
    }

    static String sha1(byte[] data) throws NoSuchAlgorithmException {
        return sha("SHA-1", data);
    }

    static String sha256(byte[] data) throws NoSuchAlgorithmException {
        return sha("SHA-256", data);
    }

    static String md5(byte[] data) throws NoSuchAlgorithmException {
        return sha("MD5", data);
    }

    static String sha(String digest, byte[] data) throws NoSuchAlgorithmException {
        byte[] bytes = MessageDigest.getInstance(digest).digest(data);
        return hex(bytes);
    }

    static String getHash(InputStream is, String algorithm) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        int count;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((count = is.read(buffer)) != -1)
            digest.update(buffer, 0, count);

        return hex(digest.digest());
    }

    static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static String getFingerprint(Context context) {
        return getFingerprint(context, "SHA1");
    }

    static String getFingerprint(Context context, String hash) {
        try {
            PackageManager pm = context.getPackageManager();
            String pkg = context.getPackageName();
            PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest digest = MessageDigest.getInstance(hash);
            byte[] bytes = digest.digest(cert);
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes)
                sb.append(String.format("%02X", b));
            return sb.toString();
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    static boolean hasValidFingerprint(Context context) {
        if (hasValidFingerprint == null) {
            hasValidFingerprint = false;

            String signed = getFingerprint(context);
            String[] fingerprints = new String[]{
                    context.getString(R.string.fingerprint),
                    context.getString(R.string.fingerprint_amazon)
            };

            for (String fingerprint : fingerprints)
                if (Objects.equals(signed, fingerprint)) {
                    hasValidFingerprint = true;
                    break;
                }
        }
        return hasValidFingerprint;
    }

    static boolean isSignedByFDroid(Context context) {
        String signed = getFingerprint(context);
        String fingerprint = context.getString(R.string.fingerprint_fdroid);
        return Objects.equals(signed, fingerprint);
    }

    static boolean canAuthenticate(Context context) {
        try {
            BiometricManager bm = BiometricManager.from(context);
            if (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS)
                return true;
            if (bm.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS)
                return true;
            return false;
        } catch (Throwable ex) {
            /*
                java.lang.SecurityException: eu.faircode.email from uid 10377 not allowed to perform USE_FINGERPRINT
                  at android.os.Parcel.createException(Parcel.java:1953)
                  at android.os.Parcel.readException(Parcel.java:1921)
                  at android.os.Parcel.readException(Parcel.java:1871)
                  at android.hardware.fingerprint.IFingerprintService$Stub$Proxy.isHardwareDetected(IFingerprintService.java:460)
                  at android.hardware.fingerprint.FingerprintManager.isHardwareDetected(FingerprintManager.java:792)
                  at androidx.core.hardware.fingerprint.FingerprintManagerCompat.isHardwareDetected(SourceFile:3)
                  at androidx.biometric.BiometricManager.canAuthenticateWithFingerprint(SourceFile:3)
                  at androidx.biometric.BiometricManager.canAuthenticateWithFingerprintOrUnknownBiometric(SourceFile:2)
                  at androidx.biometric.BiometricManager.canAuthenticateCompat(SourceFile:10)
                  at androidx.biometric.BiometricManager.canAuthenticate(SourceFile:5)
             */
            Log.e(ex);
            return false;
        }
    }

    static boolean shouldAuthenticate(Context context, boolean pausing) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean biometrics = prefs.getBoolean("biometrics", false);
        String pin = prefs.getString("pin", null);

        if (biometrics || !TextUtils.isEmpty(pin)) {
            long now = new Date().getTime();
            long last_authentication = prefs.getLong("last_authentication", 0);
            long biometrics_timeout = prefs.getInt("biometrics_timeout", 2) * 60 * 1000L;
            boolean autolock_nav = prefs.getBoolean("autolock_nav", false);
            Log.i("Authentication valid until=" + new Date(last_authentication + biometrics_timeout));

            if (last_authentication + biometrics_timeout < now)
                return true;

            if (autolock_nav && pausing)
                last_authentication = now - biometrics_timeout + 5 * 1000L;
            else
                last_authentication = now;
            prefs.edit().putLong("last_authentication", last_authentication).apply();
        }

        return false;
    }

    static boolean shouldAutoLock(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean biometrics = prefs.getBoolean("biometrics", false);
        String pin = prefs.getString("pin", null);
        boolean autolock = prefs.getBoolean("autolock", true);
        return (autolock && (biometrics || !TextUtils.isEmpty(pin)));
    }

    static void authenticate(final FragmentActivity activity, final LifecycleOwner owner,
                             Boolean enabled, final
                             Runnable authenticated, final Runnable cancelled) {
        // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android12-release/packages/SystemUI/src/com/android/systemui/biometrics/AuthController.java#195
        ApplicationEx.getMainHandler().post(new RunnableEx("authenticate") {
            @Override
            public void delegate() {
                Log.i("Authenticate " + activity);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                String pin = prefs.getString("pin", null);

                if (enabled != null || TextUtils.isEmpty(pin)) {
                    Log.i("Authenticate biometric enabled=" + enabled);
                    BiometricPrompt.PromptInfo.Builder info = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle(activity.getString(enabled == null ? R.string.app_name : R.string.title_setup_biometrics));

                    BiometricManager bm = BiometricManager.from(activity);
                    int authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK;
                    if (bm.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS)
                        authenticators |= BiometricManager.Authenticators.DEVICE_CREDENTIAL;
                    else
                        info.setNegativeButtonText(activity.getString(android.R.string.cancel));
                    info.setAllowedAuthenticators(authenticators)
                            .setConfirmationRequired(false);

                    info.setSubtitle(activity.getString(enabled == null ? R.string.title_setup_biometrics_unlock
                            : enabled
                            ? R.string.title_setup_biometrics_disable
                            : R.string.title_setup_biometrics_enable));

                    final BiometricPrompt prompt = new BiometricPrompt(activity, Helper.getUIExecutor(),
                            new BiometricPrompt.AuthenticationCallback() {
                                private int fails = 0;

                                @Override
                                public void onAuthenticationError(final int errorCode, @NonNull final CharSequence errString) {
                                    if (isBioCancelled(errorCode) || errorCode == BiometricPrompt.ERROR_UNABLE_TO_PROCESS)
                                        Log.w("Authenticate biometric error " + errorCode + ": " + errString);
                                    else
                                        Log.e("Authenticate biometric error " + errorCode + ": " + errString);

                                    if (isBioHardwareFailure(errorCode)) {
                                        prefs.edit().remove("biometrics").apply();
                                        ApplicationEx.getMainHandler().post(authenticated);
                                        return;
                                    }

                                    if (!isBioCancelled(errorCode))
                                        ApplicationEx.getMainHandler().post(new RunnableEx("auth:error") {
                                            @Override
                                            public void delegate() {
                                                ToastEx.makeText(activity,
                                                        "Error " + errorCode + ": " + errString,
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });

                                    ApplicationEx.getMainHandler().post(cancelled);
                                }

                                @Override
                                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                                    Log.i("Authenticate biometric succeeded");
                                    setAuthenticated(activity);
                                    ApplicationEx.getMainHandler().post(authenticated);
                                }

                                @Override
                                public void onAuthenticationFailed() {
                                    Log.w("Authenticate biometric failed");
                                    if (++fails >= 3)
                                        ApplicationEx.getMainHandler().post(cancelled);
                                }
                            });

                    prompt.authenticate(info.build());

                    final Runnable cancelPrompt = new RunnableEx("auth:cancelprompt") {
                        @Override
                        public void delegate() {
                            try {
                                Log.i("Authenticate cancel prompt");
                                prompt.cancelAuthentication();
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                        }
                    };

                    ApplicationEx.getMainHandler().postDelayed(cancelPrompt, AUTH_AUTOCANCEL_TIMEOUT);

                    owner.getLifecycle().addObserver(new LifecycleObserver() {
                        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                        public void onDestroy() {
                            Log.i("Authenticate destroyed");
                            ApplicationEx.getMainHandler().removeCallbacks(cancelPrompt);
                            try {
                                prompt.cancelAuthentication();
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                            owner.getLifecycle().removeObserver(this);
                        }
                    });

                } else {
                    Log.i("Authenticate PIN");
                    final View dview = LayoutInflater.from(activity).inflate(R.layout.dialog_pin_ask, null);
                    final EditText etPin = dview.findViewById(R.id.etPin);

                    etPin.setEnabled(false);

                    final AlertDialog dialog = new AlertDialog.Builder(activity)
                            .setView(dview)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                                    String pin = prefs.getString("pin", "");
                                    String entered = etPin.getText().toString();

                                    Log.i("Authenticate PIN ok=" + pin.equals(entered));
                                    if (pin.equals(entered)) {
                                        prefs.edit()
                                                .remove("pin_failure_at")
                                                .remove("pin_failure_count")
                                                .apply();
                                        setAuthenticated(activity);
                                        ApplicationEx.getMainHandler().post(authenticated);
                                    } else {
                                        if (!TextUtils.isEmpty(entered)) {
                                            int count = prefs.getInt("pin_failure_count", 0) + 1;
                                            prefs.edit()
                                                    .putLong("pin_failure_at", new Date().getTime())
                                                    .putInt("pin_failure_count", count)
                                                    .apply();
                                        }
                                        ApplicationEx.getMainHandler().post(cancelled);
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i("Authenticate PIN cancelled");
                                    ApplicationEx.getMainHandler().post(cancelled);
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    Log.i("Authenticate PIN dismissed");
                                    if (shouldAuthenticate(activity, false)) // Some Android versions call dismiss on OK
                                        ApplicationEx.getMainHandler().post(cancelled);
                                }
                            })
                            .create();

                    etPin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                                return true;
                            } else
                                return false;
                        }
                    });

                    try {
                        dialog.show();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                        long pin_failure_at = prefs.getLong("pin_failure_at", 0);
                        int pin_failure_count = prefs.getInt("pin_failure_count", 0);
                        long wait = (long) Math.pow(PIN_FAILURE_DELAY, pin_failure_count) * 1000L;
                        if (wait > PIN_FAILURE_DELAY_MAX)
                            wait = PIN_FAILURE_DELAY_MAX;
                        long delay = pin_failure_at + wait - new Date().getTime();
                        etPin.setHint(getDateTimeInstance(activity).format(pin_failure_at + wait));
                        Log.i("PIN wait=" + wait + " delay=" + delay);
                        dview.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    etPin.setCompoundDrawables(null, null, null, null);
                                    etPin.setHint(R.string.title_advanced_pin);
                                    etPin.setEnabled(true);
                                    etPin.requestFocus();
                                    showKeyboard(etPin);
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }
                            }
                        }, delay < 0 ? 0 : delay);

                    } catch (Throwable ex) {
                        Log.e(ex);
                /*
                    java.lang.RuntimeException: Unable to start activity ComponentInfo{eu.faircode.email/eu.faircode.email.ActivityMain}: java.lang.RuntimeException: InputChannel is not initialized.
                      at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3477)
                      at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3620)
                      at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:83)
                      at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:135)
                      at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:95)
                      at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2183)
                      at android.os.Handler.dispatchMessage(Handler.java:107)
                      at android.os.Looper.loop(Looper.java:241)
                      at android.app.ActivityThread.main(ActivityThread.java:7604)
                      at java.lang.reflect.Method.invoke(Native Method)
                      at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:492)
                      at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:941)
                    Caused by: java.lang.RuntimeException: InputChannel is not initialized.
                      at android.view.InputEventReceiver.nativeInit(Native Method)
                      at android.view.InputEventReceiver.<init>(InputEventReceiver.java:71)
                      at android.view.ViewRootImpl$WindowInputEventReceiver.<init>(ViewRootImpl.java:7758)
                      at android.view.ViewRootImpl.setView(ViewRootImpl.java:1000)
                      at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:393)
                      at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:95)
                      at android.app.Dialog.show(Dialog.java:342)
                      at eu.faircode.email.Helper.authenticate(SourceFile:15)
                      at eu.faircode.email.ActivityMain.onCreate(SourceFile:24)
                      at android.app.Activity.performCreate(Activity.java:7822)
                 */
                    }
                }
            }
        });
    }

    static void setAuthenticated(Context context) {
        Date now = new Date();
        Log.i("Authenticated now=" + now);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putLong("last_authentication", now.getTime()).apply();
    }

    static void clearAuthentication(Context context) {
        Log.i("Authenticate clear");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove("last_authentication").apply();
    }

    static void setupPasswordToggle(FragmentActivity activity, TextInputLayout tilPassword) {
        boolean can = canAuthenticate(activity);
        boolean secure = isSecure(activity);

        tilPassword.setEndIconMode(can || secure ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);
        tilPassword.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransformationMethod tm = tilPassword.getEditText().getTransformationMethod();
                if (tm == null)
                    tilPassword.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());
                else {
                    if (can) {
                        BiometricPrompt.PromptInfo.Builder info = new BiometricPrompt.PromptInfo.Builder()
                                .setTitle(activity.getString(R.string.title_setup_biometrics))
                                .setSubtitle(activity.getString(R.string.title_password));

                        BiometricManager bm = BiometricManager.from(activity);
                        int authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK;
                        if (bm.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS)
                            authenticators |= BiometricManager.Authenticators.DEVICE_CREDENTIAL;
                        else
                            info.setNegativeButtonText(activity.getString(android.R.string.cancel));
                        info.setAllowedAuthenticators(authenticators)
                                .setConfirmationRequired(false);

                        BiometricPrompt prompt = new BiometricPrompt(activity, Helper.getUIExecutor(),
                                new BiometricPrompt.AuthenticationCallback() {
                                    @Override
                                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                                        tilPassword.post(new RunnableEx("tilPassword") {
                                            @Override
                                            protected void delegate() {
                                                tilPassword.getEditText().setTransformationMethod(null);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                                        tilPassword.post(new RunnableEx("tilPassword") {
                                            @Override
                                            protected void delegate() {
                                                if (isBioCancelled(errorCode))
                                                    return;
                                                else if (isBioHardwareFailure(errorCode))
                                                    tilPassword.getEditText().setTransformationMethod(null);
                                                else
                                                    ToastEx.makeText(activity, "Error " + errorCode + ": " + errString, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                        prompt.authenticate(info.build());
                    } else if (secure)
                        tilPassword.getEditText().setTransformationMethod(null);
                }
            }
        });
    }

    private static boolean isBioCancelled(int errorCode) {
        return (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                errorCode == BiometricPrompt.ERROR_CANCELED ||
                errorCode == BiometricPrompt.ERROR_USER_CANCELED);
    }

    private static boolean isBioHardwareFailure(int errorCode) {
        return (errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE ||
                errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS || // No fingerprints enrolled.
                errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
                errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL);
    }

    static void selectKeyAlias(final Activity activity, final LifecycleOwner owner, final String alias, final IKeyAlias intf) {
        final Context context = activity.getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (alias != null)
                    try {
                        if (KeyChain.getPrivateKey(context, alias) != null) {
                            Log.i("Private key available alias=" + alias);
                            deliver(alias);
                            return;
                        }
                    } catch (KeyChainException ex) {
                        Log.w(ex);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                ApplicationEx.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        KeyChain.choosePrivateKeyAlias(activity, new KeyChainAliasCallback() {
                                    @Override
                                    public void alias(@Nullable final String alias) {
                                        Log.i("Selected key alias=" + alias);
                                        deliver(alias);
                                    }
                                },
                                null, null, null, -1, alias);
                    }
                });
            }

            private void deliver(final String selected) {
                ApplicationEx.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                            if (selected == null) {
                                intf.onNothingSelected();
                                ToastEx.makeText(activity, R.string.title_no_key_selected, Toast.LENGTH_LONG).show();
                            } else
                                intf.onSelected(selected);
                        } else {
                            owner.getLifecycle().addObserver(new LifecycleObserver() {
                                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                                public void onStart() {
                                    owner.getLifecycle().removeObserver(this);
                                    if (selected == null) {
                                        intf.onNothingSelected();
                                        ToastEx.makeText(activity, R.string.title_no_key_selected, Toast.LENGTH_LONG).show();
                                    } else
                                        intf.onSelected(selected);
                                }

                                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                                public void onDestroy() {
                                    owner.getLifecycle().removeObserver(this);
                                }
                            });
                        }
                    }
                });
            }
        }).start();
    }

    interface IKeyAlias {
        void onSelected(String alias);

        void onNothingSelected();
    }

    public static String HMAC(String algo, int blocksize, byte[] key, byte[] text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algo);

        if (key.length > blocksize)
            key = md.digest(key);

        byte[] ipad = new byte[blocksize];
        byte[] opad = new byte[blocksize];

        for (int i = 0; i < key.length; i++) {
            ipad[i] = key[i];
            opad[i] = key[i];
        }

        for (int i = 0; i < blocksize; i++) {
            ipad[i] ^= 0x36;
            opad[i] ^= 0x5c;
        }

        byte[] digest;

        md.update(ipad);
        md.update(text);
        digest = md.digest();

        md.update(opad);
        md.update(digest);
        digest = md.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : digest)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // Miscellaneous

    static void gc(String reason) {
        if (!BuildConfig.DEBUG)
            return;
        try {
            Log.i("GC " + reason);
            Runtime.getRuntime().gc();
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static <T> List<List<T>> chunkList(List<T> list, int size) {
        if (list == null || list.isEmpty())
            return new ArrayList<>();
        List<List<T>> result = new ArrayList<>(list.size() / size);
        for (int i = 0; i < list.size(); i += size)
            result.add(new ArrayList<>(list.subList(i, i + size < list.size() ? i + size : list.size())));
        return result;
    }

    static int[] toIntArray(List<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++)
            result[i] = list.get(i);
        return result;
    }

    static List<Integer> fromIntArray(int[] array) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < array.length; i++)
            result.add(array[i]);
        return result;
    }

    static long[] toLongArray(List<Long> list) {
        long[] result = new long[list.size()];
        for (int i = 0; i < list.size(); i++)
            result[i] = list.get(i);
        return result;
    }

    static List<Long> fromLongArray(long[] array) {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < array.length; i++)
            result.add(array[i]);
        return result;
    }

    static boolean equal(String[] a1, String[] a2) {
        if (a1 == null && a2 == null)
            return true;

        if (a1 == null || a2 == null)
            return false;

        if (a1.length != a2.length)
            return false;

        for (int i = 0; i < a1.length; i++)
            if (!a1[i].equals(a2[i]))
                return false;

        return true;
    }

    static int getSize(Bundle bundle) {
        Parcel p = Parcel.obtain();
        bundle.writeToParcel(p, 0);
        return p.dataSize();
    }

    static void clearAll(Context context) {
        ActivityManager am = Helper.getSystemService(context, ActivityManager.class);
        am.clearApplicationUserData();
    }

    static class MaximumLengthStream extends FilterInputStream {
        private int max;
        private int count = 0;

        protected MaximumLengthStream(InputStream in, int max) {
            super(in);
            this.max = max;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                count++;
                checkLength();
            }
            return b;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int read = super.read(b);
            if (read > 0) {
                count += read;
                checkLength();
            }
            return read;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = super.read(b, off, len);
            if (read > 0) {
                count += read;
                checkLength();
            }
            return read;
        }

        private void checkLength() throws IOException {
            if (count > max)
                throw new IOException("Stream larger than " + max + " bytes");
        }
    }
}
