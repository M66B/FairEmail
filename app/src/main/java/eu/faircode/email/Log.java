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

import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Severity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class Log {
    static final String TAG = "fairemail";

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
        return android.util.Log.e(TAG, msg);
    }

    public static int i(Throwable ex) {
        return android.util.Log.w(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int w(Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            Bugsnag.notify(ex, Severity.INFO);
        return android.util.Log.w(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            Bugsnag.notify(ex, Severity.WARNING);
        return android.util.Log.e(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int w(String prefix, Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            Bugsnag.notify(ex, Severity.INFO);
        return android.util.Log.w(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(String prefix, Throwable ex) {
        if (BuildConfig.BETA_RELEASE)
            Bugsnag.notify(ex, Severity.WARNING);
        return android.util.Log.e(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    static void logExtras(Intent intent) {
        if (intent != null)
            logBundle(intent.getExtras());
    }

    static void logBundle(Bundle data) {
        if (data != null) {
            Set<String> keys = data.keySet();
            StringBuilder stringBuilder = new StringBuilder();
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
                                elements[i] = "0x" + Long.toHexString((Long) element);
                            else
                                elements[i] = (element == null ? null : element.toString());
                        }
                        value = TextUtils.join(",", elements);
                    }
                } else if (v instanceof Long)
                    value = "0x" + Long.toHexString((Long) v);

                stringBuilder.append(key)
                        .append("=")
                        .append(value)
                        .append(value == null ? "" : " (" + v.getClass().getSimpleName() + ")")
                        .append("\r\n");
            }
            i(stringBuilder.toString());
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
        String body = "<pre>" + sb.toString().replaceAll("\\r?\\n", "<br />") + "</pre>";

        EntityMessage draft;

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            EntityFolder drafts = db.folder().getPrimaryDrafts();
            if (drafts == null)
                throw new IllegalArgumentException(context.getString(R.string.title_no_primary_drafts));

            List<EntityIdentity> identities = db.identity().getIdentities(drafts.account);
            EntityIdentity primary = null;
            for (EntityIdentity identity : identities) {
                if (identity.primary) {
                    primary = identity;
                    break;
                } else if (primary == null)
                    primary = identity;
            }

            draft = new EntityMessage();
            draft.account = drafts.account;
            draft.folder = drafts.id;
            draft.identity = (primary == null ? null : primary.id);
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
        return draft;
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
                Helper.isPro(context) ? "+" : ""));
        sb.append(String.format("Android: %s (SDK %d)\r\n", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
        sb.append("\r\n");

        // Get device info
        sb.append(String.format("Brand: %s\r\n", Build.BRAND));
        sb.append(String.format("Manufacturer: %s\r\n", Build.MANUFACTURER));
        sb.append(String.format("Model: %s\r\n", Build.MODEL));
        sb.append(String.format("Product: %s\r\n", Build.PRODUCT));
        sb.append(String.format("Device: %s\r\n", Build.DEVICE));
        sb.append(String.format("Host: %s\r\n", Build.HOST));
        sb.append(String.format("Display: %s\r\n", Build.DISPLAY));
        sb.append(String.format("Id: %s\r\n", Build.ID));
        sb.append("\r\n");

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float density = context.getResources().getDisplayMetrics().density;
        sb.append(String.format("Resolution: %.2f x %.2f dp %b\r\n",
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
        String uuid = prefs.getString("uuid", null);

        sb.append(String.format("UUID: %s\r\n", uuid == null ? "-" : uuid));

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
        attachment.size = null;
        attachment.progress = 0;
        attachment.id = db.attachment().insertAttachment(attachment);

        File file = attachment.getFile(context);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {

            long size = 0;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            Map<String, ?> settings = prefs.getAll();
            for (String key : settings.keySet())
                size += write(os, key + "=" + settings.get(key) + "\r\n");

            db.attachment().setDownloaded(attachment.id, size);
        }
    }

    private static void attachAccounts(Context context, long id, int sequence) throws IOException {
        DB db = DB.getInstance(context);

        EntityAttachment attachment = new EntityAttachment();
        attachment.message = id;
        attachment.sequence = sequence;
        attachment.name = "accounts.txt";
        attachment.type = "text/plain";
        attachment.size = null;
        attachment.progress = 0;
        attachment.id = db.attachment().insertAttachment(attachment);

        File file = attachment.getFile(context);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {

            long size = 0;

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

            db.attachment().setDownloaded(attachment.id, size);
        }
    }

    private static void attachNetworkInfo(Context context, long id, int sequence) throws IOException {
        DB db = DB.getInstance(context);

        EntityAttachment attachment = new EntityAttachment();
        attachment.message = id;
        attachment.sequence = sequence;
        attachment.name = "network.txt";
        attachment.type = "text/plain";
        attachment.size = null;
        attachment.progress = 0;
        attachment.id = db.attachment().insertAttachment(attachment);

        File file = attachment.getFile(context);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {

            long size = 0;
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            Network active = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                active = cm.getActiveNetwork();

            for (Network network : cm.getAllNetworks()) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                size += write(os, (network.equals(active) ? "active=" : "network=") + network + " capabilities=" + caps + "\r\n\r\n");
            }

            db.attachment().setDownloaded(attachment.id, size);
        }
    }

    private static void attachLog(Context context, long id, int sequence) throws IOException {
        DB db = DB.getInstance(context);

        EntityAttachment attachment = new EntityAttachment();
        attachment.message = id;
        attachment.sequence = sequence;
        attachment.name = "log.txt";
        attachment.type = "text/plain";
        attachment.size = null;
        attachment.progress = 0;
        attachment.id = db.attachment().insertAttachment(attachment);

        File file = attachment.getFile(context);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {

            long size = 0;
            long from = new Date().getTime() - 24 * 3600 * 1000L;
            DateFormat DF = SimpleDateFormat.getTimeInstance();

            for (EntityLog entry : db.log().getLogs(from))
                size += write(os, String.format("%s %s\r\n", DF.format(entry.time), entry.data));

            db.attachment().setDownloaded(attachment.id, size);
        }
    }

    private static void attachOperations(Context context, long id, int sequence) throws IOException {
        DB db = DB.getInstance(context);

        EntityAttachment attachment = new EntityAttachment();
        attachment.message = id;
        attachment.sequence = sequence;
        attachment.name = "operations.txt";
        attachment.type = "text/plain";
        attachment.size = null;
        attachment.progress = 0;
        attachment.id = db.attachment().insertAttachment(attachment);

        File file = attachment.getFile(context);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {

            long size = 0;
            DateFormat DF = SimpleDateFormat.getTimeInstance();

            for (EntityOperation op : db.operation().getOperations())
                size += write(os, String.format("%s %d %s %s %s\r\n",
                        DF.format(op.created),
                        op.message == null ? -1 : op.message,
                        op.name,
                        op.args,
                        op.error));

            db.attachment().setDownloaded(attachment.id, size);
        }
    }

    private static void attachLogcat(Context context, long id, int sequence) throws IOException {
        DB db = DB.getInstance(context);

        EntityAttachment attachment = new EntityAttachment();
        attachment.message = id;
        attachment.sequence = sequence;
        attachment.name = "logcat.txt";
        attachment.type = "text/plain";
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


    static long getFreeMem() {
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
}