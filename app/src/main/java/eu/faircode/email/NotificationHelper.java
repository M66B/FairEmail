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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class NotificationHelper {
    static final int NOTIFICATION_SYNCHRONIZE = 100;
    static final int NOTIFICATION_SEND = 200;
    static final int NOTIFICATION_EXTERNAL = 300;
    static final int NOTIFICATION_UPDATE = 400;
    static final int NOTIFICATION_TAGGED = 500;

    private static final List<String> PERSISTENT_IDS = Collections.unmodifiableList(Arrays.asList(
            "service",
            "send",
            "notification",
            "progress",
            "update",
            "warning",
            "error",
            "alerts"
    ));

    @RequiresApi(api = Build.VERSION_CODES.O)
    static void createNotificationChannels(Context context) {
        // https://issuetracker.google.com/issues/65108694
        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);

        // Sync
        NotificationChannel service = new NotificationChannel(
                "service", context.getString(R.string.channel_service),
                NotificationManager.IMPORTANCE_MIN);
        service.setDescription(context.getString(R.string.channel_service_description));
        service.setSound(null, null);
        service.enableVibration(false);
        service.enableLights(false);
        service.setShowBadge(false);
        service.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        nm.createNotificationChannel(service);

        // Send
        NotificationChannel send = new NotificationChannel(
                "send", context.getString(R.string.channel_send),
                NotificationManager.IMPORTANCE_DEFAULT);
        send.setDescription(context.getString(R.string.channel_send_description));
        send.setSound(null, null);
        send.enableVibration(false);
        send.enableLights(false);
        send.setShowBadge(false);
        send.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        nm.createNotificationChannel(send);

        // Notify
        NotificationChannel notification = new NotificationChannel(
                "notification", context.getString(R.string.channel_notification),
                NotificationManager.IMPORTANCE_HIGH);
        notification.setDescription(context.getString(R.string.channel_notification_description));
        notification.enableLights(true);
        notification.setLightColor(Color.YELLOW);
        notification.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notification.setBypassDnd(true);
        nm.createNotificationChannel(notification);

        NotificationChannel progress = new NotificationChannel(
                "progress", context.getString(R.string.channel_progress),
                NotificationManager.IMPORTANCE_DEFAULT);
        notification.setDescription(context.getString(R.string.channel_progress_description));
        progress.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        progress.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        nm.createNotificationChannel(progress);

        // Update
        if (!Helper.isPlayStoreInstall()) {
            NotificationChannel update = new NotificationChannel(
                    "update", context.getString(R.string.channel_update),
                    NotificationManager.IMPORTANCE_HIGH);
            update.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            update.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.createNotificationChannel(update);
        }

        // Warnings
        NotificationChannel warning = new NotificationChannel(
                "warning", context.getString(R.string.channel_warning),
                NotificationManager.IMPORTANCE_HIGH);
        warning.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        warning.setBypassDnd(true);
        nm.createNotificationChannel(warning);

        // Errors
        NotificationChannel error = new NotificationChannel(
                "error",
                context.getString(R.string.channel_error),
                NotificationManager.IMPORTANCE_HIGH);
        error.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        error.setBypassDnd(true);
        nm.createNotificationChannel(error);

        // Server alerts
        NotificationChannel alerts = new NotificationChannel(
                "alerts",
                context.getString(R.string.channel_alert),
                NotificationManager.IMPORTANCE_HIGH);
        alerts.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        alerts.setBypassDnd(true);
        nm.createNotificationChannel(alerts);

        // Contacts grouping
        NotificationChannelGroup group = new NotificationChannelGroup(
                "contacts",
                context.getString(R.string.channel_group_contacts));
        nm.createNotificationChannelGroup(group);
    }

    static boolean areNotificationsEnabled(NotificationManager nm) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return true;
        else
            return nm.areNotificationsEnabled();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static void clear(Context context) {
        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
        for (NotificationChannel channel : nm.getNotificationChannels()) {
            String id = channel.getId();
            if (!PERSISTENT_IDS.contains(id)) {
                EntityLog.log(context, "Deleting channel=" + id);
                nm.deleteNotificationChannel(id);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static JSONObject channelToJSON(NotificationChannel channel) throws JSONException {
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

        return jchannel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static NotificationChannel channelFromJSON(Context context, JSONObject jchannel) throws JSONException {
        int importance = jchannel.getInt("importance");
        if (importance < NotificationManager.IMPORTANCE_MIN ||
                importance > NotificationManager.IMPORTANCE_MAX)
            importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(
                jchannel.getString("id"),
                jchannel.getString("name"),
                importance);

        String group = jchannel.optString("group");
        if (!TextUtils.isEmpty(group))
            channel.setGroup(group);

        if (jchannel.has("description") && !jchannel.isNull("description"))
            channel.setDescription(jchannel.getString("description"));

        channel.setBypassDnd(jchannel.getBoolean("dnd"));

        int visibility = jchannel.getInt("visibility");
        if (visibility == Notification.VISIBILITY_PRIVATE ||
                visibility == Notification.VISIBILITY_PUBLIC ||
                visibility == Notification.VISIBILITY_SECRET)
            channel.setLockscreenVisibility(visibility);

        channel.setShowBadge(jchannel.getBoolean("badge"));

        if (jchannel.has("sound") && !jchannel.isNull("sound")) {
            Uri uri = Uri.parse(jchannel.getString("sound"));
            Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
            if (ringtone != null)
                channel.setSound(uri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        }

        channel.enableLights(jchannel.getBoolean("light"));
        channel.enableVibration(jchannel.getBoolean("vibrate"));

        return channel;
    }
}
