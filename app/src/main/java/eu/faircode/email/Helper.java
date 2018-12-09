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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sun.mail.imap.IMAPStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class Helper {
    static final String TAG = "fairemail";

    static final int JOB_DAILY = 1001;

    static final int AUTH_TYPE_PASSWORD = 1;
    static final int AUTH_TYPE_GMAIL = 2;

    static ThreadFactory backgroundThreadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setPriority(THREAD_PRIORITY_BACKGROUND);
            return thread;
        }
    };

    static void view(Context context, LifecycleOwner owner, Intent intent) {
        Uri uri = intent.getData();
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
            view(context, owner, intent.getData());
        else
            context.startActivity(intent);
    }

    static void view(Context context, LifecycleOwner owner, Uri uri) {
        Log.i(Helper.TAG, "Custom tab=" + uri);

        // https://developer.chrome.com/multidevice/android/customtabs
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(Helper.resolveColor(context, R.attr.colorPrimary));

        CustomTabsIntent customTabsIntent = builder.build();
        try {
            customTabsIntent.launchUrl(context, uri);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, context.getString(R.string.title_no_viewer, uri.toString()), Toast.LENGTH_LONG).show();
        } catch (Throwable ex) {
            Helper.unexpectedError(context, owner, ex);
        }
    }

    static Intent getChooser(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        if (pm.queryIntentActivities(intent, 0).size() == 1)
            return intent;
        else
            return Intent.createChooser(intent, context.getString(R.string.title_select_app));
    }

    static Intent getIntentPrivacy() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://email.faircode.eu/privacy/"));
        return intent;
    }

    static Intent getIntentOpenKeychain() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://f-droid.org/en/packages/org.sufficientlysecure.keychain/"));
        return intent;
    }

    static int resolveColor(Context context, int attr) {
        int[] attrs = new int[]{attr};
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs);
        int color = a.getColor(0, 0xFF0000);
        a.recycle();
        return color;
    }

    static void setViewsEnabled(ViewGroup view, boolean enabled) {
        for (int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);
            if (child instanceof Spinner ||
                    child instanceof EditText ||
                    child instanceof CheckBox ||
                    child instanceof ImageView /* =ImageButton */)
                child.setEnabled(enabled);
            if (child instanceof BottomNavigationView) {
                Menu menu = ((BottomNavigationView) child).getMenu();
                menu.setGroupEnabled(0, enabled);
            } else if (child instanceof ViewGroup)
                setViewsEnabled((ViewGroup) child, enabled);
        }
    }

    static String localizeFolderName(Context context, String name) {
        if ("INBOX".equals(name))
            return context.getString(R.string.title_folder_inbox);
        else if ("OUTBOX".equals(name))
            return context.getString(R.string.title_folder_outbox);
        else
            return name;
    }

    static String formatThrowable(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());

        Throwable cause = ex.getCause();
        while (cause != null) {
            sb.append(" ").append(cause.getMessage() == null ? cause.getClass().getName() : cause.getMessage());
            cause = cause.getCause();
        }

        return sb.toString();
    }

    static void unexpectedError(final Context context, final LifecycleOwner owner, final Throwable ex) {
        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));

        if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            new DialogBuilderLifecycle(context, owner)
                    .setTitle(R.string.title_unexpected_error)
                    .setMessage(ex.toString())
                    .setPositiveButton(android.R.string.cancel, null)
                    .setNeutralButton(R.string.title_report, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new SimpleTask<Long>() {
                                @Override
                                protected Long onLoad(Context context, Bundle args) throws Throwable {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(context.getString(R.string.title_crash_info_remark)).append("\n\n\n\n");
                                    sb.append(Helper.getAppInfo(context));
                                    sb.append(ex + "\n" + Log.getStackTraceString(ex));

                                    String body = "<pre>" + sb.toString().replaceAll("\\r?\\n", "<br />") + "</pre>";

                                    EntityMessage draft = null;

                                    DB db = DB.getInstance(context);
                                    try {
                                        db.beginTransaction();

                                        EntityFolder drafts = db.folder().getPrimaryDrafts();
                                        if (drafts != null) {
                                            draft = new EntityMessage();
                                            draft.account = drafts.account;
                                            draft.folder = drafts.id;
                                            draft.msgid = EntityMessage.generateMessageId();
                                            draft.to = new Address[]{Helper.myAddress()};
                                            draft.subject = context.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME + " unexpected error";
                                            draft.content = true;
                                            draft.received = new Date().getTime();
                                            draft.setContactInfo(context);
                                            draft.id = db.message().insertMessage(draft);
                                            draft.write(context, body);

                                            EntityOperation.queue(db, draft, EntityOperation.ADD);
                                        }

                                        db.setTransactionSuccessful();
                                    } finally {
                                        db.endTransaction();
                                    }

                                    return (draft == null ? null : draft.id);
                                }

                                @Override
                                protected void onLoaded(Bundle args, Long id) {
                                    if (id != null)
                                        context.startActivity(
                                                new Intent(context, ActivityCompose.class)
                                                        .putExtra("action", "edit")
                                                        .putExtra("id", id));
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
                                }
                            }.load(context, owner, new Bundle());
                        }
                    })
                    .show();
    }

    static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return new DecimalFormat("@@").format(bytes / Math.pow(unit, exp)) + " " + pre + "B";
    }

    static Address myAddress() throws UnsupportedEncodingException {
        return new InternetAddress("marcel+fairemail@faircode.eu", "FairCode");
    }

    static String canonicalAddress(String address) {
        String[] a = address.split("@");
        if (a.length > 0)
            a[0] = a[0].split("\\+")[0];
        return TextUtils.join("@", a);
    }

    static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    static String getExtension(String filename) {
        if (filename == null)
            return null;
        int index = filename.lastIndexOf(".");
        if (index < 0)
            return null;
        return filename.substring(index + 1);
    }

    static Boolean isMetered(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
            return cm.isActiveNetworkMetered();

        if (!cm.isActiveNetworkMetered()) {
            Log.i(Helper.TAG, "isMetered: active network is unmetered");
            return false;
        }

        Network active = cm.getActiveNetwork();
        if (active == null) {
            Log.i(Helper.TAG, "isMetered: no active network");
            return null;
        }

        NetworkInfo ni = cm.getNetworkInfo(active);
        if (ni == null) {
            Log.i(Helper.TAG, "isMetered: active no info");
            return null;
        }

        Log.i(Helper.TAG, "isMetered: active info=" + ni);

        if (!ni.isConnected()) {
            Log.i(Helper.TAG, "isMetered: active not connected");
            return null;
        }
        NetworkCapabilities caps = cm.getNetworkCapabilities(active);
        if (caps == null) {
            Log.i(Helper.TAG, "isMetered: active no caps");
            return null;
        }

        Log.i(Helper.TAG, "isMetered: active caps=" + caps);

        boolean unmetered = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        if (unmetered) {
            Log.i(Helper.TAG, "isMetered: active unmetered");
            return false;
        }

        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
            Log.i(Helper.TAG, "isMetered: active metered");
            return true;
        }

        // VPN: evaluate underlying networks

        Network[] networks = cm.getAllNetworks();
        if (networks == null) {
            Log.i(Helper.TAG, "isMetered: no underlying networks");
            return null;
        }

        boolean connected = false;
        for (Network network : networks) {
            ni = cm.getNetworkInfo(network);
            if (ni == null) {
                Log.i(Helper.TAG, "isMetered: no underlying info");
                return null;
            }

            Log.i(Helper.TAG, "isMetered: underlying info=" + ni);

            caps = cm.getNetworkCapabilities(network);
            if (caps == null) {
                Log.i(Helper.TAG, "isMetered: no underlying caps");
                return null;
            }

            if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                Log.i(Helper.TAG, "isMetered: underlying caps=" + caps);

                if (ni.isConnected()) {
                    connected = true;
                    Log.i(Helper.TAG, "isMetered: underlying is connected");

                    if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                        Log.i(Helper.TAG, "isMetered: underlying is unmetered");
                        return false;
                    }
                } else
                    Log.i(Helper.TAG, "isMetered: underlying is disconnected");
            } else
                Log.i(Helper.TAG, "isMetered: underlying is VPN");
        }

        if (!connected) {
            Log.i(Helper.TAG, "isMetered: underlying disconnected");
            return null;
        }

        Log.i(Helper.TAG, "isMetered: underlying is metered");
        return true;
    }

    static void connect(Context context, IMAPStore istore, EntityAccount account) throws
            MessagingException {
        try {
            istore.connect(account.host, account.port, account.user, account.password);
        } catch (AuthenticationFailedException ex) {
            if (account.auth_type == Helper.AUTH_TYPE_GMAIL) {
                account.password = Helper.refreshToken(context, "com.google", account.user, account.password);
                DB.getInstance(context).account().setAccountPassword(account.id, account.password);
                istore.connect(account.host, account.port, account.user, account.password);
            } else
                throw ex;
        }
    }

    static String refreshToken(Context context, String type, String name, String current) {
        try {
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType(type);
            for (Account account : accounts)
                if (name.equals(account.name)) {
                    Log.i(Helper.TAG, "Refreshing token");
                    am.invalidateAuthToken(type, current);
                    String refreshed = am.blockingGetAuthToken(account, getAuthTokenType(type), true);
                    Log.i(Helper.TAG, "Refreshed token");
                    return refreshed;
                }
        } catch (Throwable ex) {
            Log.w(TAG, ex + "\n" + Log.getStackTraceString(ex));
        }
        return current;
    }

    static String getAuthTokenType(String type) {
        if ("com.google".equals(type))
            return "oauth2:https://mail.google.com/";
        return null;
    }

    static boolean isPlayStoreInstall(Context context) {
        if (false && BuildConfig.DEBUG)
            return true;
        try {
            return "com.android.vending".equals(context.getPackageManager().getInstallerPackageName(context.getPackageName()));
        } catch (Throwable ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            return false;
        }
    }

    static StringBuilder getAppInfo(Context context) {
        StringBuilder sb = new StringBuilder();

        // Get version info
        String installer = context.getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID);
        sb.append(String.format("%s: %s/%s %s/%s%s\r\n",
                context.getString(R.string.app_name),
                BuildConfig.APPLICATION_ID,
                installer,
                BuildConfig.VERSION_NAME,
                Helper.hasValidFingerprint(context) ? "1" : "3",
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

        sb.append("\r\n");

        return sb;
    }

    static String sha256(String data) throws NoSuchAlgorithmException {
        return sha256(data.getBytes());
    }

    static String sha256(byte[] data) throws NoSuchAlgorithmException {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static String getBillingResponseText(@BillingClient.BillingResponse int responseCode) {
        switch (responseCode) {
            case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
                // Billing API version is not supported for the type requested
                return "BILLING_UNAVAILABLE";

            case BillingClient.BillingResponse.DEVELOPER_ERROR:
                // Invalid arguments provided to the API.
                return "DEVELOPER_ERROR";

            case BillingClient.BillingResponse.ERROR:
                // Fatal error during the API action
                return "ERROR";

            case BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED:
                // Requested feature is not supported by Play Store on the current device.
                return "FEATURE_NOT_SUPPORTED";

            case BillingClient.BillingResponse.ITEM_ALREADY_OWNED:
                // Failure to purchase since item is already owned
                return "ITEM_ALREADY_OWNED";

            case BillingClient.BillingResponse.ITEM_NOT_OWNED:
                // Failure to consume since item is not owned
                return "ITEM_NOT_OWNED";

            case BillingClient.BillingResponse.ITEM_UNAVAILABLE:
                // Requested product is not available for purchase
                return "ITEM_UNAVAILABLE";

            case BillingClient.BillingResponse.OK:
                // Success
                return "OK";

            case BillingClient.BillingResponse.SERVICE_DISCONNECTED:
                // Play Store service is not connected now - potentially transient state.
                return "SERVICE_DISCONNECTED";

            case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
                // Network connection is down
                return "SERVICE_UNAVAILABLE";

            case BillingClient.BillingResponse.USER_CANCELED:
                // User pressed back or canceled a dialog
                return "USER_CANCELED";

            default:
                return Integer.toString(responseCode);
        }
    }

    public static String getFingerprint(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            String pkg = context.getPackageName();
            PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] bytes = digest.digest(cert);
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes)
                sb.append(Integer.toString(b & 0xff, 16).toUpperCase());
            return sb.toString();
        } catch (Throwable ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            return null;
        }
    }

    public static boolean hasValidFingerprint(Context context) {
        String signed = getFingerprint(context);
        String expected = context.getString(R.string.fingerprint);
        return (signed != null && signed.equals(expected));
    }

    static boolean isPro(Context context) {
        if (false && BuildConfig.DEBUG)
            return true;
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pro", false);
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
        if (a1.length != a2.length)
            return false;

        for (int i = 0; i < a1.length; i++)
            if (!a1[i].equals(a2[i]))
                return false;

        return true;
    }

    static String sanitizeKeyword(String keyword) {
        // ()}%*"\]
        return keyword.replaceAll("[^A-Za-z0-9$_.]", "");
    }
}
