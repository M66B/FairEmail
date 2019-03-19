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

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.DeadSystemException;
import android.os.RemoteException;
import android.webkit.CookieManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.annotation.RequiresApi;

public class ApplicationEx extends Application {
    private Thread.UncaughtExceptionHandler prev = null;

    private static final List<String> DEFAULT_CHANNEL_NAMES = Collections.unmodifiableList(Arrays.asList(
            "service", "notification", "warning", "error"
    ));

    @Override
    public void onCreate() {
        super.onCreate();

        prev = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                if (ownFault(ex)) {
                    Log.e(ex);

                    if (BuildConfig.BETA_RELEASE ||
                            !Helper.isPlayStoreInstall(ApplicationEx.this))
                        writeCrashLog(ApplicationEx.this, ex);

                    if (prev != null)
                        prev.uncaughtException(thread, ex);
                } else {
                    Log.w(ex);
                    System.exit(1);
                }
            }
        });

        createNotificationChannels();
        if (Helper.hasWebView(this))
            CookieManager.getInstance().setAcceptCookie(false);
        MessageHelper.setSystemProperties();
        Core.init(this);
    }

    private void createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel service = new NotificationChannel(
                    "service",
                    getString(R.string.channel_service),
                    NotificationManager.IMPORTANCE_MIN);
            service.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            service.setShowBadge(false);
            service.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            nm.createNotificationChannel(service);

            NotificationChannel notification = new NotificationChannel(
                    "notification",
                    getString(R.string.channel_notification),
                    NotificationManager.IMPORTANCE_HIGH);
            notification.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(notification);

            NotificationChannel warning = new NotificationChannel(
                    "warning",
                    getString(R.string.channel_warning),
                    NotificationManager.IMPORTANCE_HIGH);
            warning.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(warning);

            NotificationChannel error = new NotificationChannel(
                    "error",
                    getString(R.string.channel_error),
                    NotificationManager.IMPORTANCE_HIGH);
            error.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(error);

            NotificationChannelGroup group = new NotificationChannelGroup(
                    "contacts",
                    getString(R.string.channel_group_contacts));
            nm.createNotificationChannelGroup(group);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static JSONArray channelsToJSON(Context context) throws JSONException {
        JSONArray jchannels = new JSONArray();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        for (NotificationChannel channel : nm.getNotificationChannels())
            if (!DEFAULT_CHANNEL_NAMES.contains(channel.getId())) {
                JSONObject jchannel = new JSONObject();

                jchannel.put("id", channel.getId());
                jchannel.put("group", channel.getGroup());
                jchannel.put("name", channel.getName());
                jchannel.put("description", channel.getDescription());

                jchannel.put("importance", channel.getImportance());
                jchannel.put("dnd", channel.canBypassDnd());
                jchannel.put("visibility", channel.getLockscreenVisibility());
                jchannel.put("badge", channel.canShowBadge());

                Uri sound = channel.getSound();
                if (sound != null)
                    jchannel.put("sound", sound.toString());
                // audio attributes

                jchannel.put("light", channel.shouldShowLights());
                // color

                jchannel.put("vibrate", channel.shouldVibrate());
                // pattern

                jchannels.put(jchannel);
            }

        return jchannels;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static void channelsFromJSON(Context context, JSONArray jchannels) throws JSONException {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        for (int c = 0; c < jchannels.length(); c++) {
            JSONObject jchannel = (JSONObject) jchannels.get(c);

            String id = jchannel.getString("id");
            if (nm.getNotificationChannel(id) == null) {
                NotificationChannel channel = new NotificationChannel(
                        id,
                        jchannel.getString("name"),
                        jchannel.getInt("importance"));

                if (jchannel.has("group") && !jchannel.isNull("group"))
                    channel.setGroup(jchannel.getString("group"));
                else
                    channel.setGroup("contacts");

                if (jchannel.has("description") && !jchannel.isNull("description"))
                    channel.setDescription(jchannel.getString("description"));

                channel.setBypassDnd(jchannel.getBoolean("dnd"));
                channel.setLockscreenVisibility(jchannel.getInt("visibility"));
                channel.setShowBadge(jchannel.getBoolean("badge"));

                if (jchannel.has("sound") && !jchannel.isNull("sound")) {
                    Uri uri = Uri.parse(jchannel.getString("sound"));
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    if (ringtone != null)
                        channel.setSound(uri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                }

                channel.enableLights(jchannel.getBoolean("light"));
                channel.enableVibration(jchannel.getBoolean("vibrate"));

                Log.i("Creating channel=" + channel);
                nm.createNotificationChannel(channel);
            }
        }
    }

    public boolean ownFault(Throwable ex) {
        if (ex instanceof OutOfMemoryError)
            return false;

        if (ex instanceof RemoteException)
            return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            if (ex instanceof RuntimeException && ex.getCause() instanceof DeadSystemException)
                return false;

        if (BuildConfig.BETA_RELEASE)
            return true;

        while (ex != null) {
            for (StackTraceElement ste : ex.getStackTrace())
                if (ste.getClassName().startsWith(getPackageName()))
                    return true;
            ex = ex.getCause();
        }

        return false;
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
}
