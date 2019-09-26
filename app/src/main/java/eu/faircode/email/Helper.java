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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.biometrics.BiometricManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.PowerManager;
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
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricPrompt;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.util.FolderClosedIOException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.FolderClosedException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;

public class Helper {
    static final int NOTIFICATION_SYNCHRONIZE = 1;
    static final int NOTIFICATION_SEND = 2;
    static final int NOTIFICATION_EXTERNAL = 3;
    static final int NOTIFICATION_UPDATE = 4;

    static final float LOW_LIGHT = 0.6f;

    static final int BUFFER_SIZE = 8192; // Same as in Files class

    static final String PGP_BEGIN_MESSAGE = "-----BEGIN PGP MESSAGE-----";
    static final String PGP_END_MESSAGE = "-----END PGP MESSAGE-----";

    static final String FAQ_URI = "https://github.com/M66B/FairEmail/blob/master/FAQ.md";
    static final String XDA_URI = "https://forum.xda-developers.com/showthread.php?t=3824168";
    static final String SUPPORT_URI = "https://contact.faircode.eu/?product=fairemailsupport";
    static final String PGP_URI = "https://f-droid.org/en/packages/org.sufficientlysecure.keychain/";
    static final String PLAY_APPS_URI = "https://play.google.com/store/apps/dev?id=8420080860664580239";
    static final String GITHUB_APPS_URI = "https://github.com/M66B?tab=repositories";

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

    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor(backgroundThreadFactory);

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

    static Boolean isIgnoringOptimizations(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm == null)
                return null;
            return pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID);
        }
        return null;
    }

    static boolean isPlayStoreInstall() {
        return BuildConfig.PLAY_STORE_RELEASE;
    }

    // View

    static Intent getChooser(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        if (pm.queryIntentActivities(intent, 0).size() == 1)
            return intent;
        else
            return Intent.createChooser(intent, context.getString(R.string.title_select_app));
    }

    static void view(Context context, Intent intent) {
        Uri uri = intent.getData();
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
            view(context, intent.getData(), false);
        else
            context.startActivity(intent);
    }

    static void view(Context context, Uri uri, boolean browse) {
        Log.i("View=" + uri);

        if (browse || !hasCustomTabs(context, uri)) {
            Intent view = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(getChooser(context, view));
        } else {
            // https://developer.chrome.com/multidevice/android/customtabs
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setNavigationBarColor(resolveColor(context, R.attr.colorPrimaryDark));
            builder.setToolbarColor(resolveColor(context, R.attr.colorPrimary));

            CustomTabsIntent customTabsIntent = builder.build();
            try {
                customTabsIntent.launchUrl(context, uri);
            } catch (ActivityNotFoundException ex) {
                Log.w(ex);
                ToastEx.makeText(context, context.getString(R.string.title_no_viewer, uri.toString()), Toast.LENGTH_LONG).show();
            } catch (Throwable ex) {
                Log.e(ex);
                ToastEx.makeText(context, Helper.formatThrowable(ex, false), Toast.LENGTH_LONG).show();
            }
        }
    }

    static void viewFAQ(Context context, int question) {
        if (question == 0)
            view(context, Uri.parse(FAQ_URI), false);
        else
            view(context, Uri.parse(Helper.FAQ_URI + "#user-content-faq" + question), false);
    }

    static Intent getIntentOpenKeychain() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(PGP_URI));
        return intent;
    }

    static Intent getIntentIssue(Context context) {
        if (ActivityBilling.isPro(context)) {
            String version = BuildConfig.VERSION_NAME + "/" +
                    (Helper.hasValidFingerprint(context) ? "1" : "3") +
                    (BuildConfig.PLAY_STORE_RELEASE ? "p" : "") +
                    (BuildConfig.DEBUG ? "d" : "") +
                    (ActivityBilling.isPro(context) ? "+" : "");
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
                    child instanceof RadioButton ||
                    (child instanceof Button && "disable".equals(child.getTag())))
                child.setEnabled(enabled);
            else if (child instanceof BottomNavigationView) {
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

    // https://issuetracker.google.com/issues/37054851

    static DateFormat getTimeInstance(Context context) {
        return Helper.getTimeInstance(context, SimpleDateFormat.MEDIUM);
    }

    static DateFormat getDateInstance(Context context) {
        return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
    }

    static DateFormat getTimeInstance(Context context, int style) {
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

    static DateFormat getDateTimeInstance(Context context) {
        return Helper.getDateTimeInstance(context, SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM);
    }

    static DateFormat getDateTimeInstance(Context context, int dateStyle, int timeStyle) {
        // TODO fix time format
        return SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle);
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

    static String localizeFolderType(Context context, String type) {
        int resid = context.getResources().getIdentifier(
                "title_folder_" + type.toLowerCase(Locale.ROOT),
                "string",
                context.getPackageName());
        return (resid > 0 ? context.getString(resid) : type);
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
        return formatThrowable(ex, true);
    }

    static String formatThrowable(Throwable ex, boolean santize) {
        return formatThrowable(ex, " ", santize);
    }

    static String formatThrowable(Throwable ex, String separator, boolean sanitize) {
        if (sanitize) {
            if (ex instanceof MessageRemovedException)
                return null;

            if (ex instanceof MessagingException &&
                    ex.getCause() instanceof ConnectionException &&
                    ex.getCause().getMessage() != null &&
                    (ex.getCause().getMessage().contains("Read error") ||
                            ex.getCause().getMessage().contains("Write error")))
                return null;

            if (ex instanceof IOException &&
                    ex.getCause() instanceof MessageRemovedException)
                return null;

            if (ex instanceof ConnectionException)
                return null;

            if (ex instanceof FolderClosedException || ex instanceof FolderClosedIOException)
                return null;

            if (ex instanceof IllegalStateException &&
                    ("Not connected".equals(ex.getMessage()) ||
                            "This operation is not allowed on a closed folder".equals(ex.getMessage())))
                return null;

            if (ex instanceof Core.AlertException)
                return ex.getMessage();
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
                    .setMessage(Helper.formatThrowable(ex, false))
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

    static void linkPro(final TextView tv) {
        if (ActivityBilling.isPro(tv.getContext()) && !BuildConfig.DEBUG)
            hide(tv);
        else {
            tv.getPaint().setUnderlineText(true);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tv.getContext().startActivity(new Intent(tv.getContext(), ActivityBilling.class));
                }
            });
        }
    }

    // Files

    static String sanitizeFilename(String name) {
        if (name == null)
            return null;

        return name.replaceAll("[?:\"*|/\\\\<>]", "_");
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
        try (FileOutputStream out = new FileOutputStream(file)) {
            if (content != null)
                out.write(content.getBytes());
        }
    }

    static String readStream(InputStream is, String charset) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(Math.max(BUFFER_SIZE, is.available()));
        byte[] buffer = new byte[BUFFER_SIZE];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer))
            os.write(buffer, 0, len);
        return new String(os.toByteArray(), charset);
    }

    static String readText(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return readStream(in, StandardCharsets.UTF_8.name());
        }
    }

    static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (FileOutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[BUFFER_SIZE];
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

        Matrix rotation = null;
        try {
            rotation = Helper.getImageRotation(file);
        } catch (IOException ex) {
            Log.w(ex);
        }

        if (factor > 1 || rotation != null) {
            Log.i("Decode image factor=" + factor);
            options.inJustDecodeBounds = false;
            options.inSampleSize = factor;
            Bitmap scaled = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            if (scaled != null && rotation != null) {
                Bitmap rotated = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), rotation, true);
                scaled.recycle();
                scaled = rotated;
            }

            return scaled;
        }

        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    static Matrix getImageRotation(File file) throws IOException {
        ExifInterface exif = new ExifInterface(file.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return null;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                return matrix;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                return matrix;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                return matrix;
            default:
                return null;
        }
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

    static String getFingerprint(Context context) {
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

    static boolean hasValidFingerprint(Context context) {
        String signed = getFingerprint(context);
        String expected = context.getString(R.string.fingerprint);
        return Objects.equals(signed, expected);
    }

    static boolean canAuthenticate(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return false;
        else if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT))
                return false;
            FingerprintManager fpm = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            return (fpm != null && fpm.isHardwareDetected() && fpm.hasEnrolledFingerprints());
        } else {
            @SuppressLint("WrongConstant")
            BiometricManager bm = (BiometricManager) context.getSystemService(Context.BIOMETRIC_SERVICE);
            return (bm != null && bm.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS);
        }
    }

    static boolean hasAuthentication(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("biometrics", false);
    }

    static boolean shouldAuthenticate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean biometrics = prefs.getBoolean("biometrics", false);

        if (biometrics) {
            long now = new Date().getTime();
            long last_authentication = prefs.getLong("last_authentication", 0);
            long biometrics_timeout = prefs.getInt("biometrics_timeout", 2) * 60 * 1000L;
            Log.i("Authentication valid until=" + new Date(last_authentication + biometrics_timeout));

            if (last_authentication + biometrics_timeout < now)
                return true;

            prefs.edit().putLong("last_authentication", now).apply();
        }

        return false;
    }

    static void authenticate(final FragmentActivity activity,
                             Boolean enabled, final
                             Runnable authenticated, final Runnable cancelled) {
        final Handler handler = new Handler();

        BiometricPrompt.PromptInfo.Builder info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(enabled == null ? R.string.app_name : R.string.title_setup_biometrics))
                .setNegativeButtonText(activity.getString(android.R.string.cancel));

        info.setSubtitle(activity.getString(enabled == null ? R.string.title_setup_biometrics_unlock
                : enabled
                ? R.string.title_setup_biometrics_disable
                : R.string.title_setup_biometrics_enable));

        BiometricPrompt prompt = new BiometricPrompt(activity, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(final int errorCode, @NonNull final CharSequence errString) {
                        Log.w("Biometric error " + errorCode + ": " + errString);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                                        errorCode == BiometricPrompt.ERROR_CANCELED ||
                                        errorCode == BiometricPrompt.ERROR_USER_CANCELED)
                                    cancelled.run();
                                else
                                    ToastEx.makeText(activity,
                                            errString + " (" + errorCode + ")",
                                            Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        Log.i("Biometric succeeded");

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                        prefs.edit().putLong("last_authentication", new Date().getTime()).apply();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                authenticated.run();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Log.w("Biometric failed");

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastEx.makeText(activity,
                                        R.string.title_unexpected_error,
                                        Toast.LENGTH_LONG).show();
                                cancelled.run();
                            }
                        });
                    }
                });

        prompt.authenticate(info.build());
    }

    static void clearAuthentication(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove("last_authentication").apply();
    }

    // Miscellaneous

    static <T> List<List<T>> chunkList(List<T> list, int size) {
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
