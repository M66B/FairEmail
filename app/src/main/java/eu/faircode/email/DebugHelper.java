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

import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.verify.domain.DomainVerificationManager;
import android.content.pm.verify.domain.DomainVerificationUserState;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.IBinder;
import android.os.LocaleList;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.provider.Settings;
import android.security.NetworkSecurityPolicy;
import android.text.TextUtils;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.TextServicesManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.emoji2.text.EmojiCompat;
import androidx.preference.PreferenceManager;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;

import net.openid.appauth.AuthState;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.mail.Address;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class DebugHelper {
    static final String CRASH_LOG_NAME = "crash.log";

    private static final long MAX_LOG_SIZE = 8 * 1024 * 1024L;
    private static final long MIN_FILE_SIZE = 1024 * 1024L;
    private static final long MIN_ZIP_SIZE = 2 * 1024 * 1024L;

    // https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html
    // https://docs.oracle.com/javase/8/docs/api/java/net/doc-files/net-properties.html
    private static final List<String> NETWORK_PROPS = Collections.unmodifiableList(Arrays.asList(
            "java.net.preferIPv4Stack",
            "java.net.preferIPv6Addresses",
            "http.proxyHost",
            "http.proxyPort",
            "http.nonProxyHosts",
            "https.proxyHost",
            "https.proxyPort",
            //"ftp.proxyHost",
            //"ftp.proxyPort",
            //"ftp.nonProxyHosts",
            "socksProxyHost",
            "socksProxyPort",
            "socksProxyVersion",
            "java.net.socks.username",
            //"java.net.socks.password",
            "http.agent",
            "http.keepalive",
            "http.maxConnections",
            "http.maxRedirects",
            "http.auth.digest.validateServer",
            "http.auth.digest.validateProxy",
            "http.auth.digest.cnonceRepeat",
            "http.auth.ntlm.domain",
            "jdk.https.negotiate.cbt",
            "networkaddress.cache.ttl",
            "networkaddress.cache.negative.ttl"
    ));

    static boolean isAvailable() {
        return true;
    }

    static EntityMessage getDebugInfo(Context context, String source, int title, Throwable ex, String log, Bundle args) throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(title)).append("\n\n");
        if (args != null) {
            sb.append(args.getString("issue")).append('\n');
            if (args.containsKey("account"))
                sb.append('\n').append("Account: ").append(args.getString("account"));
            if (args.containsKey("contact"))
                sb.append('\n').append("Prior contact: ").append(args.getBoolean("contact"));
        }
        sb.append("\n\n");
        sb.append(getAppInfo(context));
        if (ex != null) {
            ThrowableWrapper w = new ThrowableWrapper(ex);
            sb.append(w.toSafeString()).append("\n").append(w.getSafeStackTraceString());
        }
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
            draft.from = new Address[]{new InternetAddress(identity.email, identity.name, StandardCharsets.UTF_8.name())};
            draft.to = new Address[]{Log.myAddress()};
            draft.subject = context.getString(R.string.app_name) + " " + getVersionInfo(context) + " debug info - " + source;
            draft.received = new Date().getTime();
            draft.seen = true;
            draft.ui_seen = true;
            draft.id = db.message().insertMessage(draft);

            File file = draft.getFile(context);
            Helper.writeText(file, body);  // TODO CASA system info
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

    private static StringBuilder getAppInfo(Context context) {
        StringBuilder sb = new StringBuilder();

        ContentResolver resolver = context.getContentResolver();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean main_log = prefs.getBoolean("main_log", true);
        boolean protocol = prefs.getBoolean("protocol", false);
        long last_cleanup = prefs.getLong("last_cleanup", 0);

        PackageManager pm = context.getPackageManager();

        // Get version info
        sb.append(String.format("%s %s\r\n", context.getString(R.string.app_name), getVersionInfo(context)));
        sb.append(String.format("Package: %s uid: %d\r\n",
                BuildConfig.APPLICATION_ID, android.os.Process.myUid()));
        sb.append(String.format("Android: %s (SDK device=%d target=%d)\r\n",
                Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Helper.getTargetSdk(context)));

        String miui = Helper.getMIUIVersion();
        sb.append(String.format("MIUI: %s\r\n", miui == null ? "-" : miui));

        boolean reporting = prefs.getBoolean("crash_reports", false);
        String uuid = (reporting || Log.isTestRelease()
                ? prefs.getString("uuid", null) : null);
        sb.append(String.format("Bugsnag UUID: %s\r\n", uuid == null ? "-" : uuid));

        try {
            ApplicationInfo app = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String build_uuid = app.metaData.getString("com.bugsnag.android.BUILD_UUID");
            sb.append(String.format("Build UUID: %s\r\n", build_uuid == null ? "-" : build_uuid));
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(ex);
        }

        String gms = null;
        try {
            PackageInfo pi = pm.getPackageInfo("com.google.android.gms", 0);
            if (pi != null)
                gms = pi.versionName + " #" + pi.versionCode;
        } catch (Throwable ignored) {
        }

        String installer = Helper.getInstallerName(context);
        sb.append(String.format("Release: %s\r\n", Log.getReleaseType(context)));
        sb.append(String.format("Play Store: %s Services: %s\r\n", Helper.hasPlayStore(context), gms));
        sb.append(String.format("Installer: %s\r\n", installer == null ? "-" : installer));
        sb.append(String.format("Installed: %s\r\n", new Date(Helper.getInstallTime(context))));
        sb.append(String.format("Updated: %s\r\n", new Date(Helper.getUpdateTime(context))));
        sb.append(String.format("Last cleanup: %s\r\n", new Date(last_cleanup)));
        sb.append(String.format("Now: %s\r\n", new Date()));
        sb.append(String.format("Zone: %s\r\n", TimeZone.getDefault().getID()));

        String language = prefs.getString("language", null);
        sb.append(String.format("Locale: def=%s lang=%s\r\n",
                Locale.getDefault(), language));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            sb.append(String.format("System: %s\r\n",
                    Resources.getSystem().getConfiguration().locale));
        else {
            LocaleList ll = Resources.getSystem().getConfiguration().getLocales();
            for (int i = 0; i < ll.size(); i++)
                sb.append(String.format("System: %s\r\n", ll.get(i)));
        }

        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            try {
                TextServicesManager tsm = (TextServicesManager) context.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE);
                SpellCheckerInfo sci = (tsm == null ? null : tsm.getCurrentSpellCheckerInfo());
                if (sci != null)
                    for (int i = 0; i < sci.getSubtypeCount(); i++)
                        sb.append(String.format("Spell: %s\r\n", sci.getSubtypeAt(i).getLocale()));
            } catch (Throwable ex) {
                sb.append(ex).append("\r\n");
            }

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
        sb.append(String.format("Device: %s Arc: %b\r\n", Build.DEVICE, Helper.isArc()));
        sb.append(String.format("Host: %s\r\n", Build.HOST));
        sb.append(String.format("Time: %s\r\n", new Date(Build.TIME).toString()));
        sb.append(String.format("Display: %s\r\n", Build.DISPLAY));
        sb.append(String.format("Id: %s\r\n", Build.ID));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            sb.append(String.format("SoC: %s/%s\r\n", Build.SOC_MANUFACTURER, Build.SOC_MODEL));
        sb.append(String.format("OS version: %s\r\n", osVersion));
        sb.append("\r\n");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                // https://developer.android.com/reference/android/app/ApplicationExitInfo
                boolean exits = false;
                long from = new Date().getTime() - 30 * 24 * 3600 * 1000L;
                ActivityManager am = Helper.getSystemService(context, ActivityManager.class);
                List<ApplicationExitInfo> infos = am.getHistoricalProcessExitReasons(
                        context.getPackageName(), 0, 100);
                for (ApplicationExitInfo info : infos)
                    if (info.getTimestamp() > from &&
                            info.getImportance() >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE) {
                        exits = true;
                        sb.append(String.format("%s: %s\r\n",
                                new Date(info.getTimestamp()),
                                Helper.getExitReason(info.getReason())));
                    }
                if (!exits)
                    sb.append("No crashes\r\n");
                sb.append("\r\n");
            } catch (Throwable ex) {
                sb.append(ex).append("\r\n");
            }
        }

        boolean log = (prefs.getInt("log_level", android.util.Log.WARN) <= android.util.Log.INFO);
        sb.append(String.format("Log main: %b debug: %b protocol: %b\r\n", main_log, log, protocol));

        int[] contacts = ContactInfo.getStats();
        sb.append(String.format("Contact lookup: %d cached: %d\r\n",
                contacts[0], contacts[1]));

        sb.append(String.format("Accessibility: %b\r\n", Helper.isAccessibilityEnabled(context)));

        String charset = MimeUtility.getDefaultJavaCharset();
        sb.append(String.format("Default charset: %s/%s\r\n", charset, MimeUtility.mimeCharset(charset)));

        String emoji;
        try {
            if (EmojiCompat.isConfigured()) {
                int emojiState = EmojiCompat.get().getLoadState();
                switch (emojiState) {
                    case EmojiCompat.LOAD_STATE_LOADING:
                        emoji = "Loading";
                        break;
                    case EmojiCompat.LOAD_STATE_SUCCEEDED:
                        emoji = "Loaded";
                        break;
                    case EmojiCompat.LOAD_STATE_FAILED:
                        emoji = "Failed";
                        break;
                    case EmojiCompat.LOAD_STATE_DEFAULT:
                        emoji = "Not loaded";
                        break;
                    default:
                        emoji = "?" + emojiState;
                }
            } else
                emoji = "Disabled";
        } catch (Throwable ex) {
            Log.e(ex);
            emoji = ex.toString();
        }

        sb.append("Emoji: ").append(emoji).append("\r\n");

        sb.append("Transliterate: ")
                .append(TextHelper.canTransliterate())
                .append("\r\n");

        sb.append("Classifier: ")
                .append(Helper.humanReadableByteCount(MessageClassifier.getSize(context)))
                .append("\r\n");

        sb.append("\r\n");

        int cpus = Runtime.getRuntime().availableProcessors();
        sb.append(String.format("Processors: %d\r\n", cpus));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            long running = SystemClock.uptimeMillis() - android.os.Process.getStartUptimeMillis();
            long cpu = android.os.Process.getElapsedCpuTime();
            int util = (int) (running == 0 ? 0 : 100 * cpu / running / cpus);
            sb.append(String.format("Uptime: %s CPU: %s %d%%\r\n",
                    Helper.formatDuration(running), Helper.formatDuration(cpu), util));
        }

        Boolean largeHeap;
        try {
            ApplicationInfo info = pm.getApplicationInfo(context.getPackageName(), 0);
            largeHeap = (info.flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
        } catch (Throwable ex) {
            largeHeap = null;
        }

        ActivityManager am = Helper.getSystemService(context, ActivityManager.class);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        sb.append(String.format("Memory class: %d/%d MB Large: %s Total: %s Low: %b Small: %b\r\n",
                am.getMemoryClass(), am.getLargeMemoryClass(),
                largeHeap == null ? "?" : Boolean.toString(largeHeap),
                Helper.humanReadableByteCount(mi.totalMem),
                am.isLowRamDevice(),
                Helper.hasSmallMemoryClass(context)));

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

        WindowManager wm = Helper.getSystemService(context, WindowManager.class);
        Display display = wm.getDefaultDisplay();
        Point dim = new Point();
        display.getSize(dim);
        float density = context.getResources().getDisplayMetrics().density;
        sb.append(String.format("Density 1dp=%f\r\n", density));
        sb.append(String.format("Resolution: %.2f x %.2f dp\r\n", dim.x / density, dim.y / density));
        //sb.append(String.format("Max. texture: %d px\r\n", Helper.getMaxTextureSize()));
        sb.append(String.format("Foldable: %b\r\n", Helper.canFold(context)));

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

        sb.append(String.format("Darken support: %b\r\n",
                WebViewEx.isFeatureSupported(context, WebViewFeature.ALGORITHMIC_DARKENING)));
        try {
            PackageInfo pkg = WebViewCompat.getCurrentWebViewPackage(context);
            sb.append(String.format("WebView %d/%s has=%b\r\n",
                    pkg == null ? -1 : pkg.versionCode,
                    pkg == null ? null : pkg.versionName,
                    Helper.hasWebView(context)));
        } catch (Throwable ex) {
            sb.append(ex).append("\r\n");
        }

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

                for (String key : new String[]{"confirm_images", "ask_images", "confirm_html", "ask_html"})
                    size += write(os, String.format("%s=%b\r\n", key, prefs.getBoolean(key, true)));

                size += write(os, "\r\n");

                Map<String, ?> settings = prefs.getAll();
                List<String> keys = new ArrayList<>(settings.keySet());
                Collections.sort(keys);
                for (String key : keys) {
                    Object value = settings.get(key);
                    if ("wipe_mnemonic".equals(key) && value != null)
                        value = "[redacted]";
                    else if ("cloud_user".equals(key) && value != null)
                        value = "[redacted]";
                    else if ("cloud_password".equals(key) && value != null)
                        value = "[redacted]";
                    else if ("pin".equals(key) && value != null)
                        value = "[redacted]";
                    else if (key != null && key.startsWith("oauth."))
                        value = "[redacted]";
                    else if (key != null && key.startsWith("graph.contacts."))
                        value = "[redacted]";
                    size += write(os, key + "=" + value + "\r\n");
                }

                size += write(os, "\r\n");

                try {
                    List<String> names = new ArrayList<>();

                    Properties props = System.getProperties();
                    Enumeration<?> pnames = props.propertyNames();
                    while (pnames.hasMoreElements())
                        names.add((String) pnames.nextElement());

                    Collections.sort(names);
                    for (String name : names)
                        size += write(os, name + "=" + props.getProperty(name) + "\r\n");
                } catch (Throwable ex) {
                    size += write(os, ex.getMessage() + "\r\n");
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
                boolean poll_metered = prefs.getBoolean("poll_metered", false);
                boolean poll_unmetered = prefs.getBoolean("poll_unmetered", false);
                boolean metered = prefs.getBoolean("metered", true);
                Boolean ignoring = Helper.isIgnoringOptimizations(context);
                boolean canSchedule = AlarmManagerCompatEx.canScheduleExactAlarms(context);
                boolean auto_optimize = prefs.getBoolean("auto_optimize", false);
                boolean schedule = prefs.getBoolean("schedule", false);
                String startup = prefs.getString("startup", "unified");

                String ds = ConnectionHelper.getDataSaving(context);
                boolean vpn = ConnectionHelper.vpnActive(context);
                boolean ng = false;
                try {
                    PackageManager pm = context.getPackageManager();
                    pm.getPackageInfo("eu.faircode.netguard", 0);
                    ng = true;
                } catch (Throwable ignored) {
                }

                Integer bucket = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    try {
                        UsageStatsManager usm = Helper.getSystemService(context, UsageStatsManager.class);
                        bucket = usm.getAppStandbyBucket();
                    } catch (Throwable ignored) {
                    }

                Integer filter = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                    filter = nm.getCurrentInterruptionFilter();
                }

                StringBuilder filters = new StringBuilder();
                StringBuilder sorts = new StringBuilder();
                for (String key : prefs.getAll().keySet())
                    if (key.startsWith("filter_")) {
                        Object value = prefs.getAll().get(key);
                        if (Boolean.TRUE.equals(value))
                            filters.append(' ').append(key.substring(7)).append('=').append(value);
                    } else if (key.startsWith("sort_")) {
                        Object value = prefs.getAll().get(key);
                        sorts.append(' ').append(key).append('=').append(value);
                    }

                size += write(os, "enabled=" + enabled + (enabled ? "" : " !!!") +
                        " interval=" + pollInterval + "\r\n" +
                        (pollInterval != 0 && poll_metered ? "poll metered=" + poll_metered + " !!!\r\n" : "") +
                        (pollInterval != 0 && poll_unmetered ? "poll unmetered=" + poll_unmetered + " !!!\r\n" : "") +
                        "metered=" + metered + (metered ? "" : " !!!") +
                        " saving=" + ds + ("enabled".equals(ds) ? " !!!" : "") +
                        " vpn=" + vpn + (vpn ? " !!!" : "") +
                        " ng=" + ng + "\r\n" +
                        "optimizing=" + (ignoring == null ? null : !ignoring) + (Boolean.FALSE.equals(ignoring) ? " !!!" : "") +
                        " bucket=" + (bucket == null ? null :
                        Helper.getStandbyBucketName(bucket) +
                                (bucket > UsageStatsManager.STANDBY_BUCKET_ACTIVE ? " !!!" : "")) +
                        " canSchedule=" + canSchedule + (canSchedule ? "" : " !!!") +
                        " auto_optimize=" + auto_optimize + (auto_optimize ? " !!!" : "") +
                        " notifications=" + (filter == null ? null :
                        Helper.getInterruptionFilter(filter) +
                                (filter == NotificationManager.INTERRUPTION_FILTER_ALL ? "" : " !!!")) + "\r\n" +
                        "accounts=" + accounts.size() +
                        " folders=" + db.folder().countSync() + "/" + db.folder().countTotal() +
                        " messages=" + db.message().countTotal() +
                        " rules=" + db.rule().countTotal(null, null) +
                        " ops=" + db.operation().getOperationCount() +
                        " outbox=" + db.message().countOutbox() + "\r\n" +
                        "startup=" + startup + "\r\n" +
                        "filter " + filters + " " + sorts +
                        "\r\n\r\n");

                if (schedule) {
                    int minuteStart = prefs.getInt("schedule_start", 0);
                    int minuteEnd = prefs.getInt("schedule_end", 0);
                    int minuteStartWeekend = prefs.getInt("schedule_start_weekend", minuteStart);
                    int minuteEndWeekend = prefs.getInt("schedule_end_weekend", minuteEnd);

                    size += write(os, String.format("schedule %s...%s weekend %s...%s\r\n",
                            CalendarHelper.formatHour(context, minuteStart),
                            CalendarHelper.formatHour(context, minuteEnd),
                            CalendarHelper.formatHour(context, minuteStartWeekend),
                            CalendarHelper.formatHour(context, minuteEndWeekend)));

                    String[] daynames = new DateFormatSymbols().getWeekdays();
                    for (int i = 0; i < 7; i++) {
                        boolean day = prefs.getBoolean("schedule_day" + i, true);
                        boolean weekend = CalendarHelper.isWeekend(context, i + 1);
                        size += write(os, String.format("schedule %s=%b %s\r\n",
                                daynames[i + 1], day, weekend ? "weekend" : ""));
                    }

                    size += write(os, "\r\n");
                }

                for (EntityAccount account : accounts)
                    if (account.synchronize)
                        try {
                            String info = "pwd";
                            if (account.auth_type == ServiceAuthenticator.AUTH_TYPE_OAUTH ||
                                    account.auth_type == ServiceAuthenticator.AUTH_TYPE_GRAPH)
                                info = getTokenInfo(account.password, account.auth_type);
                            size += write(os, String.format("%s %s\r\n", account.name, info));

                            List<EntityIdentity> identities = db.identity().getSynchronizingIdentities(account.id);
                            for (EntityIdentity identity : identities)
                                if (identity.auth_type == ServiceAuthenticator.AUTH_TYPE_OAUTH ||
                                        identity.auth_type == ServiceAuthenticator.AUTH_TYPE_GRAPH)
                                    size += write(os, String.format("- %s %s\r\n",
                                            identity.name, getTokenInfo(identity.password, identity.auth_type)));
                        } catch (Throwable ex) {
                            size += write(os, ex.toString() + "\r\n");
                        }

                size += write(os, "\r\n");

                Map<Long, EntityFolder> unified = new HashMap<>();
                for (EntityFolder folder : db.folder().getFoldersByType(EntityFolder.INBOX))
                    unified.put(folder.id, folder);
                for (EntityFolder folder : db.folder().getFoldersUnified(null, false))
                    unified.put(folder.id, folder);

                for (Long fid : unified.keySet()) {
                    EntityFolder folder = unified.get(fid);
                    EntityAccount account = db.account().getAccount(folder.account);
                    size += write(os, String.format("%s/%s:%s sync=%b unified=%b\r\n",
                            (account == null ? null : account.name),
                            folder.name, folder.type, folder.synchronize, folder.unified));
                }

                size += write(os, "\r\n");

                for (EntityAccount account : accounts) {
                    if (account.synchronize) {
                        int content = 0;
                        int messages = 0;
                        List<TupleFolderEx> folders = db.folder().getFoldersEx(account.id);
                        for (TupleFolderEx folder : folders) {
                            content += folder.content;
                            messages += folder.messages;
                        }

                        int blocked = db.contact().countBlocked(account.id);

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

                        size += write(os, account.id + ":" + account.name + (account.primary ? "*" : "") +
                                " " + (account.protocol == EntityAccount.TYPE_IMAP ? "IMAP" : "POP") +
                                " [" + (account.provider == null ? "" : account.provider) +
                                ":" + ServiceAuthenticator.getAuthTypeName(account.auth_type) + "]" +
                                " " + account.host + ":" + account.port + "/" +
                                EmailService.getEncryptionName(account.encryption) +
                                (account.insecure ? " !!!" : "") +
                                " sync=" + account.synchronize +
                                " exempted=" + account.poll_exempted + (pollInterval > 0 && account.poll_exempted ? " !!!" : "") +
                                " poll=" + account.poll_interval +
                                " ondemand=" + account.ondemand + (account.ondemand ? " !!!" : "") +
                                " msgs=" + content + "/" + messages + " max=" + account.max_messages +
                                " blocked=" + blocked + (blocked == 0 ? "" : " !!!") +
                                " rules=" + db.rule().countTotal(account.id, null) +
                                " ops=" + db.operation().getOperationCount(account.id) +
                                " schedule=" + (!ignore_schedule) + (ignore_schedule ? " !!!" : "") +
                                " unmetered=" + unmetered + (unmetered ? " !!!" : "") +
                                " quota=" + (account.quota_usage == null ? "-" : Helper.humanReadableByteCount(account.quota_usage)) +
                                "/" + (account.quota_limit == null ? "-" : Helper.humanReadableByteCount(account.quota_limit)) +
                                " " + account.state +
                                (account.last_connected == null ? "" : " " + dtf.format(account.last_connected)) +
                                (account.error == null ? "" : "\r\n" + account.error) +
                                "\r\n");

                        if (folders.size() > 0)
                            Collections.sort(folders, folders.get(0).getComparator(context));
                        for (TupleFolderEx folder : folders)
                            if (folder.synchronize || account.protocol == EntityAccount.TYPE_POP) {
                                int unseen = db.message().countUnseen(folder.id);
                                int hidden = db.message().countHidden(folder.id);
                                int notifying = db.message().countNotifying(folder.id);
                                size += write(os, "- " + folder.id + ":" + folder.name + " " +
                                        folder.type + (folder.inherited_type == null ? "" : "/" + folder.inherited_type) +
                                        (folder.unified ? " unified" : "") +
                                        (folder.notify ? " notify" : "") +
                                        (Boolean.TRUE.equals(folder.subscribed) ? " subscribed" : "") +
                                        " poll=" + folder.poll + (folder.poll || EntityFolder.INBOX.equals(folder.type) ? "" : " !!! ") +
                                        " factor=" + folder.poll_factor +
                                        " days=" + getDays(folder.sync_days) + "/" + getDays(folder.keep_days) +
                                        " msgs=" + folder.content + "/" + folder.messages + "/" + folder.total +
                                        " rules=" + db.rule().countTotal(account.id, folder.id) +
                                        " ops=" + db.operation().getOperationCount(folder.id, null) +
                                        " unseen=" + unseen + " hidden=" + hidden + " notifying=" + notifying +
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
                                        identity.display + " " + identity.email +
                                        (identity.self ? "" : " !self") +
                                        " [" + (identity.provider == null ? "" : identity.provider) +
                                        ":" + identity.user +
                                        ":" + ServiceAuthenticator.getAuthTypeName(identity.auth_type) + "]" +
                                        (TextUtils.isEmpty(identity.sender_extra_regex) ? "" : " regex=" + identity.sender_extra_regex) +
                                        (!identity.sender_extra ? "" : " edit" +
                                                (identity.sender_extra_name ? "+name" : "-name") +
                                                (identity.reply_extra_name ? "+copy" : "-copy")) +
                                        " " + identity.host + ":" + identity.port + "/" +
                                        EmailService.getEncryptionName(identity.encryption) +
                                        (identity.insecure ? " !!!" : "") +
                                        " ops=" + db.operation().getOperationCount(EntityOperation.SEND) +
                                        " max=" + (identity.max_size == null ? "-" : Helper.humanReadableByteCount(identity.max_size)) +
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
                                jfolder.put("inherited_type", folder.inherited_type);
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
                                jfolder.put("flags", folder.flags == null ? null : TextUtils.join(",", folder.flags));
                                jfolder.put("keywords", folder.keywords == null ? null : TextUtils.join(",", folder.keywords));
                                jfolder.put("tbc", Boolean.TRUE.equals(folder.tbc));
                                jfolder.put("rename", folder.rename);
                                jfolder.put("tbd", Boolean.TRUE.equals(folder.tbd));
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

            Boolean isValidated = null;
            Boolean isCaptive = null;

            long size = 0;
            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);

                NetworkInfo ani = cm.getActiveNetworkInfo();
                if (ani != null)
                    size += write(os, "Active network info=" + ani +
                            " connecting=" + ani.isConnectedOrConnecting() +
                            " connected=" + ani.isConnected() +
                            " available=" + ani.isAvailable() +
                            " state=" + ani.getState() + "/" + ani.getDetailedState() +
                            " metered=" + cm.isActiveNetworkMetered() +
                            " roaming=" + ani.isRoaming() +
                            " type=" + ani.getType() + "/" + ani.getTypeName() +
                            "\r\n\r\n");

                Network active = ConnectionHelper.getActiveNetwork(context);
                NetworkInfo a = (active == null ? null : cm.getNetworkInfo(active));
                NetworkCapabilities c = (active == null ? null : cm.getNetworkCapabilities(active));
                LinkProperties p = (active == null ? null : cm.getLinkProperties(active));
                boolean n = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
                size += write(os, "Active network=" + active + " native=" + n + "\r\n");
                size += write(os, "   info=" + a +
                        " connecting=" + (a == null ? null : a.isConnectedOrConnecting()) +
                        " connected=" + (a == null ? null : a.isConnected()) +
                        " available=" + (a == null ? null : a.isAvailable()) +
                        " state=" + (a == null ? null : a.getState() + "/" + a.getDetailedState()) +
                        " roaming=" + (a == null ? null : a.isRoaming()) +
                        " type=" + (a == null ? null : a.getType() + "/" + a.getTypeName()) +
                        "\r\n");
                size += write(os, "   caps=" + c + "\r\n");
                size += write(os, "   props=" + p + "\r\n\r\n");

                for (Network network : cm.getAllNetworks()) {
                    size += write(os, (network.equals(active) ? "active=" : "network=") + network + "\r\n");

                    NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                    size += write(os, " caps=" + caps + "\r\n");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (isValidated == null)
                            isValidated = false;
                        if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
                            isValidated = true;

                        if (isCaptive == null)
                            isCaptive = false;
                        if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL))
                            isCaptive = true;
                    }

                    LinkProperties props = cm.getLinkProperties(network);
                    size += write(os, " props=" + props + "\r\n");

                    size += write(os, "\r\n");
                }

                try {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces != null && interfaces.hasMoreElements()) {
                        NetworkInterface ni = interfaces.nextElement();
                        size += write(os, "Interface=" + ni + " up=" + ni.isUp() + "\r\n");
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
                size += write(os, "Roaming=" + state.isRoaming() + "\r\n");
                size += write(os, "\r\n");

                boolean[] has46 = ConnectionHelper.has46(context);

                boolean mx;
                try {
                    DnsHelper.checkMx(context, new Address[]{Log.myAddress()});
                    mx = true;
                } catch (Throwable ignored) {
                    mx = false;
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean dns_custom = prefs.getBoolean("dns_custom", false);

                size += write(os, "DNS custom=" + dns_custom +
                        " servers=" + TextUtils.join(", ", DnsHelper.getDnsServers(context)) + "\r\n");
                size += write(os, "MX=" + mx + "\r\n");
                size += write(os, "Has IPv4=" + has46[0] + " IPv6=" + has46[1] + "\r\n");
                size += write(os, "VPN active=" + ConnectionHelper.vpnActive(context) + "\r\n");
                size += write(os, "Data saving=" + ConnectionHelper.isDataSaving(context) + "\r\n");
                size += write(os, "Airplane=" + ConnectionHelper.airplaneMode(context) + "\r\n");
                size += write(os, "Private" +
                        " DNS=" + ConnectionHelper.isPrivateDnsActive(context) +
                        " server=" + ConnectionHelper.getPrivateDnsServerName(context) + "\r\n");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    size += write(os, "Cleartext permitted= " +
                            NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted() + "\r\n");
                size += write(os, "\r\n");

                int timeout = prefs.getInt("timeout", EmailService.DEFAULT_CONNECT_TIMEOUT);
                boolean metered = prefs.getBoolean("metered", true);
                int download = prefs.getInt("download", MessageHelper.DEFAULT_DOWNLOAD_SIZE);
                boolean download_limited = prefs.getBoolean("download_limited", false);
                boolean roaming = prefs.getBoolean("roaming", true);
                boolean rlah = prefs.getBoolean("rlah", true);
                boolean download_headers = prefs.getBoolean("download_headers", false);
                boolean download_eml = prefs.getBoolean("download_eml", false);
                boolean download_plain = prefs.getBoolean("download_plain", false);
                boolean standalone_vpn = prefs.getBoolean("standalone_vpn", false);
                boolean require_validated = prefs.getBoolean("require_validated", false);
                boolean require_validated_captive = prefs.getBoolean("require_validated_captive", true);
                boolean vpn_only = prefs.getBoolean("vpn_only", false);
                boolean tcp_keep_alive = prefs.getBoolean("tcp_keep_alive", false);
                boolean ssl_harden = prefs.getBoolean("ssl_harden", false);
                boolean ssl_harden_strict = (ssl_harden && prefs.getBoolean("ssl_harden_strict", false));
                boolean cert_strict = prefs.getBoolean("cert_strict", true);
                boolean cert_transparency = prefs.getBoolean("cert_transparency", false);
                boolean open_safe = prefs.getBoolean("open_safe", false);

                size += write(os, "timeout=" + timeout + "s" + (timeout == EmailService.DEFAULT_CONNECT_TIMEOUT ? "" : " !!!") + "\r\n");
                size += write(os, "metered=" + metered + (metered ? "" : " !!!") + "\r\n");
                size += write(os, "download=" + Helper.humanReadableByteCount(download) +
                        " unmetered=" + download_limited + (download_limited ? " !!!" : "") + "\r\n");
                size += write(os, "roaming=" + roaming + (roaming ? "" : " !!!") + "\r\n");
                size += write(os, "rlah=" + rlah + (rlah ? "" : " !!!") + "\r\n");

                size += write(os, "headers=" + download_headers + (download_headers ? " !!!" : "") + "\r\n");
                size += write(os, "eml=" + download_eml + (download_eml ? " !!!" : "") + "\r\n");
                size += write(os, "plain=" + download_plain + (download_plain ? " !!!" : "") + "\r\n");

                size += write(os, "captive=" + (isCaptive == null ? "-" : Boolean.toString(isCaptive)) + "\r\n");
                size += write(os, "validation=" + require_validated + (require_validated ? " !!!" : "") +
                        " captive=" + require_validated_captive + (require_validated_captive ? "" : " !!!") + "\r\n");
                size += write(os, "validated=" + (isValidated == null ? "-" : Boolean.toString(isValidated)) +
                        (Boolean.FALSE.equals(isValidated) &&
                                (Boolean.TRUE.equals(isCaptive) ? require_validated_captive : require_validated) ? " !!!" : "") + "\r\n");

                size += write(os, "standalone_vpn=" + standalone_vpn + (standalone_vpn ? " !!!" : "") + "\r\n");
                size += write(os, "vpn_only=" + vpn_only + (vpn_only ? " !!!" : "") + "\r\n");

                size += write(os, "tcp_keep_alive=" + tcp_keep_alive + (tcp_keep_alive ? " !!!" : "") + "\r\n");
                size += write(os, "ssl_harden=" + ssl_harden + (ssl_harden ? " !!!" : "") + "\r\n");
                size += write(os, "ssl_harden_strict=" + ssl_harden_strict + (ssl_harden_strict ? " !!!" : "") + "\r\n");
                size += write(os, "cert_strict=" + cert_strict + (cert_strict ? " !!!" : "") + "\r\n");
                size += write(os, "cert_transparency=" + cert_transparency + (cert_transparency ? " !!!" : "") + "\r\n");
                size += write(os, "open_safe=" + open_safe + "\r\n");

                for (String key : prefs.getAll().keySet())
                    if (key.startsWith("dns_"))
                        size += write(os, key + "=" + prefs.getAll().get(key) + "\r\n");

                size += write(os, "\r\n");
                size += write(os, Log.getCiphers().toString());

                try {
                    String algo = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(algo);
                    tmf.init((KeyStore) null);

                    TrustManager[] tms = tmf.getTrustManagers();
                    if (tms != null)
                        for (TrustManager tm : tms) {
                            size += write(os, String.format("Trust manager: %s (%s)\n",
                                    tm.getClass().getName(), algo));
                            if (tm instanceof X509TrustManager)
                                for (X509Certificate cert : ((X509TrustManager) tm).getAcceptedIssuers())
                                    size += write(os, String.format("- %s\n", cert.getIssuerDN()));
                        }
                } catch (Throwable ex) {
                    size += write(os, ex.getMessage() + "\r\n");
                }
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
                    if (entry.data != null && entry.data.contains("backoff="))
                        size += write(os, String.format("%s %s\r\n",
                                TF.format(entry.time),
                                entry.data));

                size += write(os, "\r\n");

                for (EntityLog entry : db.log().getLogs(from, null)) {
                    size += write(os, String.format("%s [%d:%d:%d:%d:%d] %s\r\n",
                            TF.format(entry.time),
                            entry.type.ordinal(),
                            (entry.thread == null ? 0 : entry.thread),
                            (entry.account == null ? 0 : entry.account),
                            (entry.folder == null ? 0 : entry.folder),
                            (entry.message == null ? 0 : entry.message),
                            entry.data));
                    if (size > MAX_LOG_SIZE) {
                        size += write(os, "<truncated>\r\n");
                        break;
                    }
                }
            }

            db.attachment().setDownloaded(attachment.id, size);
            if (!BuildConfig.DEBUG && size > MIN_ZIP_SIZE)
                attachment.zip(context);
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

            List<File> files = new ArrayList<>();
            files.addAll(Arrays.asList(FairEmailLoggingProvider.getLogFiles(context)));

            File logcat = new File(context.getFilesDir(), "logcat.txt");

            try {

                // https://cheatsheetseries.owasp.org/cheatsheets/OS_Command_Injection_Defense_Cheat_Sheet.html#java
                ProcessBuilder pb = new ProcessBuilder("logcat", // CASA "/system/bin/logcat",
                        "-d",
                        "-v", "threadtime",
                        //"-t", "1000",
                        "fairemail" + ":I");
                Map<String, String> env = pb.environment();
                env.clear();
                pb.directory(context.getFilesDir());

                Process proc = null;
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(logcat))) {
                    proc = pb.start();
                    Helper.copy(proc.getInputStream(), os);
                } finally {
                    if (proc != null)
                        proc.destroy();
                }

                files.add(logcat);
            } catch (Throwable ex) {
                Log.e(ex);
            }

            attachment.zip(context, files.toArray(new File[0]));

            Helper.secureDelete(logcat);

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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    boolean permission = Helper.hasPermission(context, Manifest.permission.POST_NOTIFICATIONS);
                    boolean enabled = nm.areNotificationsEnabled();
                    size += write(os, String.format("Permission=%b %s Enabled=%b %s\r\n",
                            permission, (permission ? "" : "!!!"),
                            enabled, (enabled ? "" : "!!!")));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    boolean paused = nm.areNotificationsPaused();
                    size += write(os, String.format("Paused=%b %s\r\n",
                            paused, (paused ? "!!!" : "")));
                }

                int filter = nm.getCurrentInterruptionFilter();
                size += write(os, String.format("Interruption filter allow=%s %s\r\n\r\n",
                        Helper.getInterruptionFilter(filter),
                        (filter == NotificationManager.INTERRUPTION_FILTER_ALL ? "" : "!!!")));

                size += write(os, String.format("InCall=%b DND=%b\r\n\r\n",
                        MediaPlayerHelper.isInCall(context),
                        MediaPlayerHelper.isDnd(context)));

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                StringBuilder options = new StringBuilder();
                for (String key : prefs.getAll().keySet())
                    if (key.startsWith("notify_")) {
                        Object value = prefs.getAll().get(key);
                        boolean mark = false;
                        if ("notify_known".equals(key) && Boolean.TRUE.equals(value))
                            mark = true;
                        if ("notify_background_only".equals(key) && Boolean.TRUE.equals(value))
                            mark = true;
                        if ("notify_suppress_in_car".equals(key) && Boolean.TRUE.equals(value))
                            mark = true;
                        options.append(' ').append(key).append('=')
                                .append(value)
                                .append(mark ? " !!!" : "")
                                .append("\r\n");
                    }

                if (options.length() > 0) {
                    options.append("\r\n");
                    size += write(os, options.toString());
                }

                List<EntityAccount> accounts = db.account().getAccounts();
                for (EntityAccount account : accounts) {
                    size += write(os, String.format("%d %s notify=%b\r\n",
                            account.id, account.name, account.notify));
                }
                size += write(os, "\r\n");

                for (NotificationChannel channel : nm.getNotificationChannels())
                    try {
                        JSONObject jchannel = NotificationHelper.channelToJSON(channel);
                        size += write(os, jchannel.toString(2) + "\r\n\r\n");
                    } catch (JSONException ex) {
                        size += write(os, ex + "\r\n");
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
                size += write(os, String.format("Interruption filter\r\n"));
                size += write(os, String.format("- All: no notifications are suppressed.\r\n"));
                size += write(os, String.format("- Priority: all notifications are suppressed except those that match the priority criteria. Some audio streams are muted.\r\n"));
                size += write(os, String.format("- None: all notifications are suppressed and all audio streams (except those used for phone calls) and vibrations are muted.\r\n"));
                size += write(os, String.format("- Alarm: all notifications except those of category alarm are suppressed. Some audio streams are muted.\r\n"));
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
            PackageManager pm = context.getPackageManager();

            long size = 0;

            boolean safOpen = false;
            try {
                Intent open = new Intent(Intent.ACTION_GET_CONTENT);
                open.addCategory(Intent.CATEGORY_OPENABLE);
                open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                open.setType("*/*");
                safOpen = (open.resolveActivity(pm) != null);
            } catch (Throwable ex) {
                Log.e(ex);
            }

            boolean safCreate = false;
            try {
                Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                create.addCategory(Intent.CATEGORY_OPENABLE);
                create.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                create.setType("*/*");
                create.putExtra(Intent.EXTRA_TITLE, "x.x");
                Helper.openAdvanced(context, create);
                safCreate = (create.resolveActivity(pm) != null);
            } catch (Throwable ex) {
                Log.e(ex);
            }

            File file = attachment.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                size += write(os, String.format("SAF open=%b create=%b\r\n", safOpen, safCreate));
                size += write(os, String.format("Photo picker=%b\r\n", Helper.hasPhotoPicker()));
                size += write(os, String.format("Double tap timeout=%d\r\n", ViewConfiguration.getDoubleTapTimeout()));
                size += write(os, String.format("Long press timeout=%d\r\n", ViewConfiguration.getLongPressTimeout()));

                String s = Helper.getTimePattern(context, SimpleDateFormat.SHORT);
                String m = Helper.getTimePattern(context, SimpleDateFormat.MEDIUM);

                size += write(os, String.format("Time 24h=%b\r\n",
                        android.text.format.DateFormat.is24HourFormat(context)));
                size += write(os, String.format("Time short format=%s time=%s\r\n", s,
                        new SimpleDateFormat(s).format(now)));
                size += write(os, String.format("Time medium format=%s time=%s\r\n", m,
                        new SimpleDateFormat(m).format(now)));
                size += write(os, String.format("Time short=%s\r\n",
                        Helper.getTimeInstance(context, SimpleDateFormat.SHORT).format(now)));
                size += write(os, String.format("Time medium=%s\r\n",
                        Helper.getTimeInstance(context, SimpleDateFormat.MEDIUM).format(now)));
                size += write(os, String.format("Time long=%s\r\n",
                        Helper.getTimeInstance(context, SimpleDateFormat.LONG).format(now)));
                size += write(os, String.format("Date short=%s\r\n",
                        Helper.getDateInstance(context, SimpleDateFormat.SHORT).format(now)));
                size += write(os, String.format("Date medium=%s\r\n",
                        Helper.getDateInstance(context, SimpleDateFormat.MEDIUM).format(now)));
                size += write(os, String.format("Date long=%s\r\n",
                        Helper.getDateInstance(context, SimpleDateFormat.LONG).format(now)));
                size += write(os, String.format("Date/time short=%s\r\n",
                        Helper.getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(now)));
                size += write(os, String.format("Date/time medium=%s\r\n",
                        Helper.getDateTimeInstance(context, SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM).format(now)));
                size += write(os, String.format("Date/time long=%s\r\n",
                        Helper.getDateTimeInstance(context, SimpleDateFormat.LONG, SimpleDateFormat.LONG).format(now)));

                BiometricManager bm = BiometricManager.from(context);
                boolean secure = (bm.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                        == BiometricManager.BIOMETRIC_SUCCESS);
                size += write(os, String.format("Device credentials allowed=%b\r\n", secure));

                for (Class<?> cls : new Class[]{
                        ActivitySendSelf.class,
                        ActivitySearch.class,
                        ActivityAnswer.class,
                        ReceiverAutoStart.class})
                    size += write(os, String.format("%s=%b\r\n",
                            cls.getSimpleName(), Helper.isComponentEnabled(context, cls)));
                size += write(os, "\r\n");

                try {
                    ApplicationInfo app = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                    List<String> metas = Log.getExtras(app.metaData);
                    size += write(os, "Manifest metas=" + (metas == null ? null : metas.size()) + "\r\n");
                    for (String meta : metas)
                        size += write(os, String.format("%s\r\n", meta));
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }
                size += write(os, "\r\n");

                int flags = PackageManager.GET_RESOLVED_FILTER;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    flags |= PackageManager.MATCH_ALL;

                try {
                    Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
                    List<ResolveInfo> homes = context.getPackageManager().queryIntentActivities(home, PackageManager.MATCH_DEFAULT_ONLY);
                    size += write(os, "Launchers=" + (homes == null ? null : homes.size()) + "\r\n");
                    if (homes != null)
                        for (ResolveInfo ri : homes)
                            size += write(os, String.format("Launcher=%s\r\n", ri.activityInfo.packageName));

                    ResolveInfo rid = context.getPackageManager().resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
                    size += write(os, String.format("Default launcher=%s\r\n", (rid == null ? null : rid.activityInfo.packageName)));
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }
                size += write(os, "\r\n");

                try {
                    Intent open = new Intent(Intent.ACTION_GET_CONTENT);
                    open.addCategory(Intent.CATEGORY_OPENABLE);
                    open.setType("*/*");

                    ResolveInfo main = pm.resolveActivity(open, 0);

                    List<ResolveInfo> ris = pm.queryIntentActivities(open, flags);
                    size += write(os, "File selectors=" + (ris == null ? null : ris.size()) + "\r\n");
                    if (ris != null)
                        for (ResolveInfo ri : ris) {
                            boolean p = Objects.equals(main == null ? null : main.activityInfo.packageName, ri.activityInfo.packageName);
                            CharSequence label = pm.getApplicationLabel(ri.activityInfo.applicationInfo);
                            size += write(os, String.format("File selector %s%s (%s)\r\n",
                                    ri.activityInfo.packageName, p ? "*" : "", label == null ? null : label.toString()));
                        }

                } catch (Throwable ex) {
                    size += write(os, "\r\n");
                }
                size += write(os, "\r\n");

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW)
                            //.addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.parse("http://example.com/"));
                    ResolveInfo main = pm.resolveActivity(intent, 0);

                    List<ResolveInfo> ris = pm.queryIntentActivities(intent, flags);
                    size += write(os, "Browsers=" + (ris == null ? null : ris.size()) + "\r\n");
                    if (ris != null)
                        for (ResolveInfo ri : ris) {
                            CharSequence label = pm.getApplicationLabel(ri.activityInfo.applicationInfo);

                            Intent serviceIntent = new Intent();
                            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
                            serviceIntent.setPackage(ri.activityInfo.packageName);
                            boolean tabs = (pm.resolveService(serviceIntent, 0) != null);

                            StringBuilder sb = new StringBuilder();
                            sb.append("Browser=").append(ri.activityInfo.packageName);
                            if (Objects.equals(main == null ? null : main.activityInfo.packageName, ri.activityInfo.packageName))
                                sb.append("*");
                            sb.append(" (").append(label).append(")");
                            sb.append(" tabs=").append(tabs);
                            sb.append(" view=").append(ri.filter.hasAction(Intent.ACTION_VIEW));
                            sb.append(" browsable=").append(ri.filter.hasCategory(Intent.CATEGORY_BROWSABLE));
                            sb.append(" authorities=").append(ri.filter.authoritiesIterator() != null);
                            sb.append(" schemes=");

                            boolean first = true;
                            Iterator<String> schemeIter = ri.filter.schemesIterator();
                            while (schemeIter.hasNext()) {
                                String scheme = schemeIter.next();
                                if (first)
                                    first = false;
                                else
                                    sb.append(',');
                                sb.append(scheme);
                            }

                            if (tabs && BuildConfig.DEBUG && false)
                                try {
                                    boolean bindable = context.bindService(serviceIntent, new CustomTabsServiceConnection() {
                                        @Override
                                        public void onCustomTabsServiceConnected(@NonNull final ComponentName component, final CustomTabsClient client) {
                                            try {
                                                context.unbindService(this);
                                            } catch (Throwable ex) {
                                                Log.e(ex);
                                            }
                                        }

                                        @Override
                                        public void onServiceDisconnected(final ComponentName component) {
                                            // Do nothing
                                        }
                                    }, 0);
                                    sb.append(" bindable=").append(bindable);
                                } catch (Throwable ex) {
                                    size += write(os, ex.toString() + "\r\n");
                                }

                            sb.append("\r\n");
                            size += write(os, sb.toString());
                        }

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    String open_with_pkg = prefs.getString("open_with_pkg", null);
                    boolean open_with_tabs = prefs.getBoolean("open_with_tabs", true);
                    size += write(os, String.format("Selected: %s tabs=%b\r\n",
                            open_with_pkg, open_with_tabs));
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }
                size += write(os, "\r\n");

                try {
                    Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    ResolveInfo main = pm.resolveActivity(intent, 0);
                    List<ResolveInfo> ris = pm.queryIntentActivities(intent, flags);
                    size += write(os, "Recorders=" + (ris == null ? null : ris.size()) + "\r\n");
                    if (ris != null)
                        for (ResolveInfo ri : ris) {
                            CharSequence label = pm.getApplicationLabel(ri.activityInfo.applicationInfo);

                            StringBuilder sb = new StringBuilder();
                            sb.append("Recorder=").append(ri.activityInfo.packageName);
                            if (Objects.equals(main.activityInfo.packageName, ri.activityInfo.packageName))
                                sb.append("*");
                            sb.append(" (").append(label).append(")");

                            sb.append("\r\n");
                            size += write(os, sb.toString());
                        }
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }
                size += write(os, "\r\n");

                try {
                    List<UriPermission> uperms = context.getContentResolver().getPersistedUriPermissions();
                    size += write(os, "Persisted URIs=" + (uperms == null ? null : uperms.size()) + "\r\n");
                    if (uperms != null)
                        for (UriPermission uperm : uperms) {
                            size += write(os, String.format("%s r=%b w=%b %s\r\n",
                                    uperm.getUri().toString(),
                                    uperm.isReadPermission(),
                                    uperm.isWritePermission(),
                                    new Date(uperm.getPersistedTime())));
                        }
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }
                size += write(os, "\r\n");

                try {
                    PackageInfo pi = pm.getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_PERMISSIONS);
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

                for (String prop : NETWORK_PROPS)
                    size += write(os, prop + "=" + System.getProperty(prop) + "\r\n");
                size += write(os, "\r\n");

                ApplicationInfo ai = context.getApplicationInfo();
                if (ai != null)
                    size += write(os, String.format("Source: %s\r\n public: %s\r\n",
                            ai.sourceDir, ai.publicSourceDir));
                size += write(os, String.format("Files: %s\r\n", context.getFilesDir()));

                File external = Helper.getExternalFilesDir(context);
                boolean emulated = (external != null && Environment.isExternalStorageEmulated(external));
                size += write(os, String.format("External: %s emulated: %b\r\n", external, emulated));

                size += write(os, String.format("Cache: %s\r\n  external: %s\n",
                        context.getCacheDir(), context.getExternalCacheDir()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    size += write(os, String.format("Data: %s\r\n", context.getDataDir().getAbsolutePath()));
                size += write(os, String.format("Database: %s\r\n",
                        context.getDatabasePath(DB.DB_NAME)));

                size += write(os, String.format("sqlite: %s json: %b\r\n", DB.getSqliteVersion(), DB.hasJson()));

                try {
                    TupleFtsStats stats = db.message().getFts();
                    size += write(os, String.format("fts: %d/%d %s\r\n", stats.fts, stats.total,
                            Helper.humanReadableByteCount(Fts4DbHelper.size(context))));
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }

                size += write(os, "\r\n");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        DomainVerificationManager dvm = Helper.getSystemService(context, DomainVerificationManager.class);
                        DomainVerificationUserState userState = dvm.getDomainVerificationUserState(context.getPackageName());
                        Map<String, Integer> hostToStateMap = userState.getHostToStateMap();
                        for (String key : hostToStateMap.keySet()) {
                            Integer stateValue = hostToStateMap.get(key);
                            if (stateValue == DomainVerificationUserState.DOMAIN_STATE_VERIFIED)
                                size += write(os, String.format("Verified: %s\r\n", key));
                            else if (stateValue == DomainVerificationUserState.DOMAIN_STATE_SELECTED)
                                size += write(os, String.format("selected: %s\r\n", key));
                            else
                                size += write(os, String.format("Unverified: %s (%d)\r\n", key,
                                        stateValue == null ? -1 : stateValue));
                        }
                    } catch (Throwable ex) {
                        size += write(os, String.format("%s\r\n", ex));
                    }
                    size += write(os, "\r\n");
                }

                try {
                    List<WorkInfo> works = WorkManager
                            .getInstance(context)
                            .getWorkInfos(WorkQuery.fromStates(
                                    WorkInfo.State.ENQUEUED,
                                    WorkInfo.State.BLOCKED,
                                    WorkInfo.State.RUNNING))
                            .get();
                    for (WorkInfo work : works) {
                        size += write(os, String.format("Work: %s\r\n",
                                work.toString()));
                    }
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }

                size += write(os, "\r\n");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    try {
                        Map<Integer, Integer> exts = SdkExtensions.getAllExtensionVersions();
                        for (Integer ext : exts.keySet())
                            size += write(os, String.format("Extension %d / %d\r\n", ext, exts.get(ext)));
                        if (exts.size() > 0)
                            size += write(os, "\r\n");

                        size += write(os, String.format("Max. pick images: %d\r\n\r\n", MediaStore.getPickImagesMaxLimit()));
                    } catch (Throwable ex) {
                        size += write(os, String.format("%s\r\n", ex));
                    }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        for (FileStore store : FileSystems.getDefault().getFileStores())
                            if (!store.isReadOnly() &&
                                    store.getUsableSpace() != 0 &&
                                    !"tmpfs".equals(store.type())) {
                                long total = store.getTotalSpace();
                                long unalloc = store.getUnallocatedSpace();
                                size += write(os, String.format("%s %s %s/%s\r\n",
                                        store,
                                        store.type(),
                                        Helper.humanReadableByteCount(total - unalloc),
                                        Helper.humanReadableByteCount(total)));
                            }
                    } catch (IOException ex) {
                        size += write(os, String.format("%s\r\n", ex));
                    }
                    size += write(os, "\r\n");
                }

                List<File> files = new ArrayList<>();
                try {
                    files.addAll(Helper.listFiles(context.getFilesDir(), MIN_FILE_SIZE));
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }
                try {
                    files.addAll(Helper.listFiles(context.getCacheDir(), MIN_FILE_SIZE));
                } catch (Throwable ex) {
                    size += write(os, String.format("%s\r\n", ex));
                }

                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return -Long.compare(f1.length(), f2.length());
                    }
                });

                for (int i = 0; i < Math.min(100, files.size()); i++)
                    size += write(os, String.format("%d %s %s\r\n", i + 1,
                            Helper.humanReadableByteCount(files.get(i).length()),
                            files.get(i).getAbsoluteFile()));
                size += write(os, "\r\n");

                size += write(os, String.format("Configuration: %s\r\n\r\n",
                        context.getResources().getConfiguration()));

                for (Provider p : Security.getProviders())
                    size += write(os, String.format("%s\r\n", p));
                size += write(os, "\r\n");

                String pgpPackage = PgpHelper.getPackageName(context);
                boolean pgpInstalled = PgpHelper.isOpenKeychainInstalled(context);
                size += write(os, String.format("%s=%b\r\n", pgpPackage, pgpInstalled));

                if (pgpInstalled)
                    try {
                        PackageInfo pi = pm.getPackageInfo(pgpPackage, PackageManager.GET_PERMISSIONS);
                        for (int i = 0; i < pi.requestedPermissions.length; i++) {
                            boolean granted = ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0);
                            size += write(os, String.format("- %s=%b\r\n", pi.requestedPermissions[i], granted));
                        }
                    } catch (Throwable ex) {
                        size += write(os, String.format("%s\r\n", ex));
                    }

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
                                context.getPackageName(), 0, 100);
                        for (ApplicationExitInfo info : infos)
                            size += write(os, String.format("%s: %s %s/%s reason=%s status=%d importance=%d\r\n",
                                    new Date(info.getTimestamp()), info.getDescription(),
                                    Helper.humanReadableByteCount(info.getPss() * 1024L),
                                    Helper.humanReadableByteCount(info.getRss() * 1024L),
                                    Helper.getExitReason(info.getReason()), info.getStatus(), info.getImportance()));
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
                                    Helper.getEventType(event.getEventType()),
                                    event.getClassName(),
                                    event.getAppStandbyBucket(),
                                    event.getShortcutId()));
                        }
                    } catch (Throwable ex) {
                        size += write(os, String.format("%s\r\n", ex));
                    }

                try {
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

    private static String getVersionInfo(Context context) {
        return String.format("%s%s/%d%s%s%s\r\n",
                BuildConfig.VERSION_NAME,
                BuildConfig.REVISION,
                Helper.hasValidFingerprint(context) ? 1 : 3,
                BuildConfig.PLAY_STORE_RELEASE ? "p" : "",
                BuildConfig.DEBUG ? "d" : "",
                ActivityBilling.isPro(context) ? "+" : "-");
    }

    private static String getDays(Integer days) {
        if (days == null)
            return "?";
        else
            return (days == Integer.MAX_VALUE ? "∞" : Integer.toString(days));
    }

    static String getTokenInfo(String password, int auth_type) throws JSONException {
        AuthState authState = AuthState.jsonDeserialize(password);
        Long expiration = authState.getAccessTokenExpirationTime();
        TokenResponse t = authState.getLastTokenResponse();
        Set<String> scopeSet = (t == null ? null : t.getScopeSet());
        String[] scopes = (scopeSet == null ? new String[0] : scopeSet.toArray(new String[0]));
        return String.format("%s expire=%s need=%b %s",
                ServiceAuthenticator.getAuthTypeName(auth_type),
                (expiration == null ? null : new Date(expiration)),
                authState.getNeedsTokenRefresh(),
                TextUtils.join(",", scopes));
    }

    private static int write(OutputStream os, String text) throws IOException {
        byte[] bytes = text.getBytes();
        os.write(bytes);  // TODO CASA system info
        return bytes.length;
    }

    static void writeCrashLog(Context context, Throwable ex) {
        File file = new File(context.getFilesDir(), CRASH_LOG_NAME);
        Log.w("Writing exception to " + file);

        try (FileWriter out = new FileWriter(file, true)) {
            out.write(BuildConfig.VERSION_NAME + BuildConfig.REVISION + " " + new Date() + "\r\n");
            ThrowableWrapper w = new ThrowableWrapper(ex);
            out.write(w.toSafeString() + "\r\n");
            out.write(w.getSafeStackTraceString() + "\r\n");
        } catch (IOException e) {
            Log.e(e);
        }
    }
}
