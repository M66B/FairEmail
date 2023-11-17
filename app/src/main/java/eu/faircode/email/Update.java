package eu.faircode.email;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class Update {
    private static final int UPDATE_TIMEOUT = 15 * 1000; // milliseconds
    static final long UPDATE_DAILY = (BuildConfig.BETA_RELEASE ? 4 : 12) * 3600 * 1000L; // milliseconds
    static final long UPDATE_WEEKLY = 7 * 24 * 3600 * 1000L; // milliseconds

    static void check(ActivityBase context, boolean always) {
        if (Helper.isPlayStoreInstall())
            return;
        if (!Helper.hasValidFingerprint(context) && !(always && BuildConfig.DEBUG))
            return;

        long now = new Date().getTime();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean updates = prefs.getBoolean("updates", true);
        boolean beta = prefs.getBoolean("beta", false) && false;
        boolean weekly = prefs.getBoolean("weekly", Helper.hasPlayStore(context));
        long last_update_check = prefs.getLong("last_update_check", 0);

        if (!always && !updates)
            return;
        if (!always && last_update_check + (weekly ? UPDATE_WEEKLY : UPDATE_DAILY) > now)
            return;

        prefs.edit().putLong("last_update_check", now).apply();

        Bundle args = new Bundle();
        args.putBoolean("always", always);
        args.putBoolean("beta", beta);

        new SimpleTask<Info>() {
            @Override
            protected Info onExecute(Context context, Bundle args) throws Throwable {
                boolean beta = args.getBoolean("beta");

                StringBuilder response = new StringBuilder();
                HttpsURLConnection urlConnection = null;
                try {
                    URL latest = new URL(beta ? BuildConfig.BITBUCKET_DOWNLOADS_API : BuildConfig.GITHUB_LATEST_API);
                    urlConnection = (HttpsURLConnection) latest.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(UPDATE_TIMEOUT);
                    urlConnection.setConnectTimeout(UPDATE_TIMEOUT);
                    urlConnection.setDoOutput(false);
                    ConnectionHelper.setUserAgent(context, urlConnection);
                    urlConnection.connect();

                    int status = urlConnection.getResponseCode();
                    InputStream inputStream = (status == HttpsURLConnection.HTTP_OK
                            ? urlConnection.getInputStream() : urlConnection.getErrorStream());

                    if (inputStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = br.readLine()) != null)
                            response.append(line);
                    }

                    if (status == HttpsURLConnection.HTTP_FORBIDDEN) {
                        // {"message":"API rate limit exceeded for ...","documentation_url":"https://developer.github.com/v3/#rate-limiting"}
                        JSONObject jmessage = new JSONObject(response.toString());
                        if (jmessage.has("message"))
                            throw new IllegalArgumentException(jmessage.getString("message"));
                        throw new IOException("HTTP " + status + ": " + response);
                    }
                    if (status != HttpsURLConnection.HTTP_OK)
                        throw new IOException("HTTP " + status + ": " + response);

                    JSONObject jroot = new JSONObject(response.toString());

                    if (beta) {
                        if (!jroot.has("values"))
                            throw new IOException("values field missing");

                        JSONArray jvalues = jroot.getJSONArray("values");
                        for (int i = 0; i < jvalues.length(); i++) {
                            JSONObject jitem = jvalues.getJSONObject(i);
                            if (!jitem.has("links"))
                                continue;

                            JSONObject jlinks = jitem.getJSONObject("links");
                            if (!jlinks.has("self"))
                                continue;

                            JSONObject jself = jlinks.getJSONObject("self");
                            if (!jself.has("href"))
                                continue;

                            // .../FairEmail-v1.1995a-play-preview-release.apk
                            String link = jself.getString("href");
                            if (!link.endsWith(".apk"))
                                continue;

                            int slash = link.lastIndexOf('/');
                            if (slash < 0)
                                continue;

                            String[] c = link.substring(slash + 1).split("-");
                            if (c.length < 4 ||
                                    !"FairEmail".equals(c[0]) ||
                                    c[1].length() < 8 ||
                                    !"github".equals(c[2]) ||
                                    !"update".equals(c[3]))
                                continue;

                            // v1.1995a
                            Integer version = Helper.parseInt(c[1].substring(3, c[1].length() - 1));
                            if (version == null)
                                continue;
                            char revision = c[1].charAt(c[1].length() - 1);

                            int v = BuildConfig.VERSION_CODE;
                            char r = BuildConfig.REVISION.charAt(0);
                            if (BuildConfig.DEBUG || version > v || (version == v && revision > r)) {
                                Info info = new Info();
                                info.tag_name = c[1];
                                info.html_url = BuildConfig.BITBUCKET_DOWNLOADS_URI;
                                info.download_url = link;
                                return info;
                            }
                        }
                    } else {
                        if (!jroot.has("tag_name") || jroot.isNull("tag_name"))
                            throw new IOException("tag_name field missing");
                        if (!jroot.has("assets") || jroot.isNull("assets"))
                            throw new IOException("assets section missing");

                        // Get update info
                        Info info = new Info();
                        info.tag_name = jroot.getString("tag_name");
                        info.html_url = BuildConfig.GITHUB_LATEST_URI;

                        // Check if new release
                        JSONArray jassets = jroot.getJSONArray("assets");
                        for (int i = 0; i < jassets.length(); i++) {
                            JSONObject jasset = jassets.getJSONObject(i);
                            if (jasset.has("name") && !jasset.isNull("name")) {
                                String name = jasset.getString("name");
                                if (name.endsWith(".apk") && name.contains("github")) {
                                    info.download_url = jasset.optString("browser_download_url");
                                    Log.i("Latest version=" + info.tag_name);
                                    if (BuildConfig.DEBUG)
                                        return info;
                                    try {
                                        if (Double.parseDouble(info.tag_name) <=
                                                Double.parseDouble(BuildConfig.VERSION_NAME))
                                            return null;
                                        else
                                            return info;
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                        if (BuildConfig.VERSION_NAME.equals(info.tag_name))
                                            return null;
                                        else
                                            return info;
                                    }
                                }
                            }
                        }
                    }

                    return null;
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }

            @Override
            protected void onExecuted(Bundle args, Info info) {
                boolean always = args.getBoolean("always");
                if (info == null) {
                    if (always)
                        ToastEx.makeText(context, R.string.title_no_update, Toast.LENGTH_LONG).show();
                    return;
                }

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context, "update")
                                .setSmallIcon(R.drawable.baseline_get_app_white_24)
                                .setContentTitle(context.getString(R.string.title_updated, info.tag_name))
                                .setContentText(info.html_url)
                                .setAutoCancel(true)
                                .setShowWhen(false)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

                Intent update = new Intent(Intent.ACTION_VIEW, Uri.parse(info.html_url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent piUpdate = PendingIntentCompat.getActivity(
                        context, ActivityView.PI_UPDATE, update, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(piUpdate);

                Intent manage = new Intent(context, ActivitySetup.class)
                        .setAction("misc")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("tab", "misc");
                PendingIntent piManage = PendingIntentCompat.getActivity(
                        context, ActivitySetup.PI_MISC, manage, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionManage = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_settings_24,
                        context.getString(R.string.title_setup_manage),
                        piManage);
                builder.addAction(actionManage.build());

                if (!TextUtils.isEmpty(info.download_url)) {
                    Intent download = new Intent(Intent.ACTION_VIEW, Uri.parse(info.download_url))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent piDownload = PendingIntentCompat.getActivity(
                            context, 0, download, 0);
                    NotificationCompat.Action.Builder actionDownload = new NotificationCompat.Action.Builder(
                            R.drawable.twotone_cloud_download_24,
                            context.getString(R.string.title_download),
                            piDownload);
                    builder.addAction(actionDownload.build());
                }

                try {
                    NotificationManager nm =
                            Helper.getSystemService(context, NotificationManager.class);
                    if (NotificationHelper.areNotificationsEnabled(nm))
                        nm.notify(NotificationHelper.NOTIFICATION_UPDATE,
                                builder.build());
                } catch (Throwable ex) {
                    Log.w(ex);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (args.getBoolean("always"))
                    if (ex instanceof IllegalArgumentException || ex instanceof IOException)
                        ToastEx.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                    else
                        Log.unexpectedError(context.getSupportFragmentManager(), ex);
            }
        }.execute(context, args, "update:check");
    }

    private static class Info {
        String tag_name; // version
        String html_url;
        String download_url;
    }
}
