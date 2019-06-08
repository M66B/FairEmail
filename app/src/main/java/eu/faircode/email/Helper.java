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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Spannable;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.FolderClosedException;
import javax.mail.MessageRemovedException;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;

public class Helper {
    static final int NOTIFICATION_SYNCHRONIZE = 1;
    static final int NOTIFICATION_SEND = 2;
    static final int NOTIFICATION_EXTERNAL = 3;

    static final float LOW_LIGHT = 0.6f;

    static final String FAQ_URI = "https://github.com/M66B/open-source-email/blob/master/FAQ.md";
    static final String XDA_URI = "https://forum.xda-developers.com/android/apps-games/source-email-t3824168";

    static ThreadFactory backgroundThreadFactory = new ThreadFactory() {
        private final AtomicInteger threadId = new AtomicInteger();

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("FairEmail_bg_" + threadId.getAndIncrement());
            thread.setPriority(THREAD_PRIORITY_BACKGROUND);
            return thread;
        }
    };

    static ThreadFactory foregroundThreadFactory = new ThreadFactory() {
        private final AtomicInteger threadId = new AtomicInteger();

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("FairEmail_fg_" + threadId.getAndIncrement());
            return thread;
        }
    };

    // Features

    static boolean hasPermission(Context context, String name) {
        return (ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED);
    }

    static boolean hasCustomTabs(Context context, Uri uri) {
        PackageManager pm = context.getPackageManager();
        Intent view = new Intent(Intent.ACTION_VIEW, uri);

        for (ResolveInfo info : pm.queryIntentActivities(view, 0)) {
            Intent intent = new Intent();
            intent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            intent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(intent, 0) != null)
                return true;
        }

        return false;
    }

    static boolean hasWebView(Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_WEBVIEW))
            try {
                new WebView(context);
                return true;
            } catch (Throwable ex) {
                return false;
            }
        else
            return false;
    }

    static boolean canPrint(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_PRINTING);
    }

    // View

    static Intent getChooser(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        if (pm.queryIntentActivities(intent, 0).size() == 1)
            return intent;
        else
            return Intent.createChooser(intent, context.getString(R.string.title_select_app));
    }

    static void view(Context context, LifecycleOwner owner, Intent intent) {
        Uri uri = intent.getData();
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
            view(context, owner, intent.getData(), false);
        else
            context.startActivity(intent);
    }

    static void view(Context context, LifecycleOwner owner, Uri uri, boolean browse) {
        Log.i("View=" + uri);

        if (!hasCustomTabs(context, uri))
            browse = true;

        if (browse) {
            Intent view = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(getChooser(context, view));
        } else {
            // https://developer.chrome.com/multidevice/android/customtabs
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(resolveColor(context, R.attr.colorPrimary));

            CustomTabsIntent customTabsIntent = builder.build();
            try {
                customTabsIntent.launchUrl(context, uri);
            } catch (ActivityNotFoundException ex) {
                Log.w(ex);
                Toast.makeText(context, context.getString(R.string.title_no_viewer, uri.toString()), Toast.LENGTH_LONG).show();
            } catch (Throwable ex) {
                Log.e(ex);
                unexpectedError(context, owner, ex);
            }
        }
    }

    static Intent getIntentSetupHelp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/M66B/open-source-email/blob/master/SETUP.md#setup-help"));
        return intent;
    }

    static Intent getIntentFAQ() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Helper.FAQ_URI));
        return intent;
    }

    static Intent getIntentPrivacy() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/M66B/open-source-email/blob/master/PRIVACY.md#fairemail"));
        return intent;
    }

    static Intent getIntentOpenKeychain() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://f-droid.org/en/packages/org.sufficientlysecure.keychain/"));
        return intent;
    }

    static Intent getIntentIssue(Context context) {
        if (BuildConfig.BETA_RELEASE) {
            String version = BuildConfig.VERSION_NAME + "/" +
                    (Helper.hasValidFingerprint(context) ? "1" : "3") +
                    (Helper.isPro(context) ? "+" : "");
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setPackage(BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            try {
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Log.myAddress().getAddress()});
            } catch (UnsupportedEncodingException ex) {
                Log.w(ex);
            }
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.title_issue_subject, version));
            return intent;
        } else
            return new Intent(Intent.ACTION_VIEW, Uri.parse(XDA_URI));
    }

    // Graphics

    static int dp2pixels(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }

    static float getTextSize(Context context, int zoom) {
        TypedArray ta = null;
        try {
            if (zoom == 0)
                ta = context.obtainStyledAttributes(
                        R.style.TextAppearance_AppCompat_Small, new int[]{android.R.attr.textSize});
            else if (zoom == 2)
                ta = context.obtainStyledAttributes(
                        R.style.TextAppearance_AppCompat_Large, new int[]{android.R.attr.textSize});
            else
                ta = context.obtainStyledAttributes(
                        R.style.TextAppearance_AppCompat_Medium, new int[]{android.R.attr.textSize});
            return ta.getDimension(0, 0);
        } finally {
            if (ta != null)
                ta.recycle();
        }
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
                    child instanceof ImageView /* =ImageButton */ ||
                    (child instanceof Button && "disable".equals(child.getTag())))
                child.setEnabled(enabled);
            if (child instanceof BottomNavigationView) {
                Menu menu = ((BottomNavigationView) child).getMenu();
                menu.setGroupEnabled(0, enabled);
            } else if (child instanceof ViewGroup)
                setViewsEnabled((ViewGroup) child, enabled);
        }
    }

    static void hide(View view) {
        view.setPadding(0, 0, 0, 0);

        ViewGroup.LayoutParams lparam = view.getLayoutParams();
        lparam.width = 0;
        lparam.height = 0;
        if (lparam instanceof ConstraintLayout.LayoutParams)
            ((ConstraintLayout.LayoutParams) lparam).setMargins(0, 0, 0, 0);
        view.setLayoutParams(lparam);
    }

    static boolean isDarkTheme(Context context) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.themeName, tv, true);
        return (tv.string != null && !"light".contentEquals(tv.string));
    }

    // Formatting

    static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return new DecimalFormat("@@").format(bytes / Math.pow(unit, exp)) + " " + pre + "B";
    }

    static DateFormat getTimeInstance(Context context, int style) {
        // https://issuetracker.google.com/issues/37054851
        if (context != null &&
                (style == SimpleDateFormat.SHORT || style == SimpleDateFormat.MEDIUM)) {
            Locale locale = Locale.getDefault();
            boolean is24Hour = android.text.format.DateFormat.is24HourFormat(context);
            String skeleton = (is24Hour ? "Hm" : "hm");
            if (style == SimpleDateFormat.MEDIUM)
                skeleton += "s";
            String pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, skeleton);
            return new SimpleDateFormat(pattern, locale);
        } else
            return SimpleDateFormat.getTimeInstance(style);
    }

    static CharSequence getRelativeTimeSpanString(Context context, long millis) {
        long now = System.currentTimeMillis();
        long span = Math.abs(now - millis);
        Time nowTime = new Time();
        Time thenTime = new Time();
        nowTime.set(now);
        thenTime.set(millis);
        if (span < DateUtils.DAY_IN_MILLIS && nowTime.weekDay == thenTime.weekDay)
            return getTimeInstance(context, SimpleDateFormat.SHORT).format(millis);
        else
            return DateUtils.getRelativeTimeSpanString(context, millis);
    }

    static String ellipsize(String text, int maxLen) {
        if (text == null || text.length() < maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...";
    }

    static void clearComposingText(Spannable text) {
        Object[] spans = text.getSpans(0, text.length(), Object.class);
        for (Object span : spans)
            if ((text.getSpanFlags(span) & Spanned.SPAN_COMPOSING) != 0)
                text.removeSpan(span);
    }

    static String localizeFolderName(Context context, String name) {
        if (name != null && "INBOX".equals(name.toUpperCase()))
            return context.getString(R.string.title_folder_inbox);
        else if ("OUTBOX".equals(name))
            return context.getString(R.string.title_folder_outbox);
        else
            return name;
    }

    static String formatThrowable(Throwable ex) {
        return formatThrowable(ex, false, " ");
    }

    static String formatThrowable(Throwable ex, boolean sanitize) {
        return formatThrowable(ex, sanitize, " ");
    }

    static String formatThrowable(Throwable ex, boolean sanitize, String separator) {
        if (sanitize) {
            if (ex instanceof MessageRemovedException)
                return null;
            if (ex instanceof FolderClosedException)
                return null;
            if (ex instanceof IllegalStateException &&
                    ("Not connected".equals(ex.getMessage()) ||
                            "This operation is not allowed on a closed folder".equals(ex.getMessage())))
                return null;
            //if (ex instanceof MailConnectException && ex.getCause() instanceof UnknownHostException)
            //    return null;
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

    static void unexpectedError(final Context context, final LifecycleOwner owner, final Throwable ex) {
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
                                protected Long onExecute(Context context, Bundle args) throws Throwable {
                                    return Log.getDebugInfo(context, R.string.title_crash_info_remark, ex, null).id;
                                }

                                @Override
                                protected void onExecuted(Bundle args, Long id) {
                                    context.startActivity(
                                            new Intent(context, ActivityCompose.class)
                                                    .putExtra("action", "edit")
                                                    .putExtra("id", id));
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    if (ex instanceof IllegalArgumentException)
                                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                                    else
                                        Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
                                }
                            }.execute(context, owner, new Bundle(), "error:unexpected");
                        }
                    })
                    .show();
        else
            ApplicationEx.writeCrashLog(context, ex);
    }

    // Files

    static String sanitizeFilename(String name) {
        return (name == null ? null : name.replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
    }

    static String getExtension(String filename) {
        if (filename == null)
            return null;
        int index = filename.lastIndexOf(".");
        if (index < 0)
            return null;
        return filename.substring(index + 1);
    }

    static void writeText(File file, String content) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(content == null ? "" : content);
        }
    }

    static String readText(File file) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                body.append(line);
                body.append('\n');
            }
            return body.toString();
        }
    }

    static void copy(File src, File dst) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(src))) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(dst))) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
            }
        }
    }

    static Bitmap decodeImage(File file, int scaleToPixels) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        int factor = 1;
        while (options.outWidth / factor > scaleToPixels)
            factor *= 2;

        if (factor > 1) {
            Log.i("Decode image factor=" + factor);
            options.inJustDecodeBounds = false;
            options.inSampleSize = factor;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }

        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    // Cryptography

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
            Log.e(ex);
            return null;
        }
    }

    public static boolean hasValidFingerprint(Context context) {
        String signed = getFingerprint(context);
        String expected = context.getString(R.string.fingerprint);
        return Objects.equals(signed, expected);
    }

    // Miscellaneous

    static String sanitizeKeyword(String keyword) {
        // https://tools.ietf.org/html/rfc3501
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyword.length(); i++) {
            // flag-keyword    = atom
            // atom            = 1*ATOM-CHAR
            // ATOM-CHAR       = <any CHAR except atom-specials>
            char kar = keyword.charAt(i);
            // atom-specials   = "(" / ")" / "{" / SP / CTL / list-wildcards / quoted-specials / resp-specials
            if (kar == '(' || kar == ')' || kar == '{' || kar == ' ' || Character.isISOControl(kar))
                continue;
            // list-wildcards  = "%" / "*"
            if (kar == '%' || kar == '*')
                continue;
            // quoted-specials = DQUOTE / "\"
            if (kar == '"' || kar == '\\')
                continue;
            // resp-specials   = "]"
            if (kar == ']')
                continue;
            sb.append(kar);
        }
        return sb.toString();
    }

    static boolean isPlayStoreInstall(Context context) {
        return BuildConfig.PLAY_STORE_RELEASE;
    }

    static boolean isPro(Context context) {
        if (false && BuildConfig.DEBUG)
            return true;
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pro", false);
    }

    public static <T> List<List<T>> chunkList(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>(list.size() / size);
        for (int i = 0; i < list.size(); i += size)
            result.add(list.subList(i, i + size < list.size() ? i + size : list.size()));
        return result;
    }

    static long[] toLongArray(List<Long> list) {
        long[] result = new long[list.size()];
        for (int i = 0; i < list.size(); i++)
            result[i] = list.get(i);
        return result;
    }

    static long[] toLongArray(Set<Long> set) {
        long[] result = new long[set.size()];
        int i = 0;
        for (Long value : set)
            result[i++] = value;
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

    static int getSize(Bundle bundle) {
        Parcel p = Parcel.obtain();
        bundle.writeToParcel(p, 0);
        return p.dataSize();
    }
}
