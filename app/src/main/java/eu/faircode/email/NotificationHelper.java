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

import static androidx.core.app.NotificationCompat.DEFAULT_LIGHTS;
import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.text.Html;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import me.leolin.shortcutbadger.ShortcutBadgerAlt;

class NotificationHelper {
    static final int NOTIFICATION_SYNCHRONIZE = 100;
    static final int NOTIFICATION_SEND = 200;
    static final int NOTIFICATION_EXTERNAL = 300;
    static final int NOTIFICATION_UPDATE = 400;
    static final int NOTIFICATION_TAGGED = 500;
    static final int NOTIFICATION_TTS = 600;

    private static final int MAX_NOTIFICATION_DISPLAY = 10; // per group
    private static final int MAX_NOTIFICATION_COUNT = 100; // per group
    private static final long SCREEN_ON_DURATION = 3000L; // milliseconds

    // Android applies a rate limit when updating a notification.
    // If you post updates to a notification too frequently—many in less than one second—
    // the system might drop updates.
    private static final int DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE = 5; // NotificationManagerService.java
    private static final int MAX_PREVIEW = 5000; // characters
    private static final long NOTIFY_DELAY = 1250L / DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE; // milliseconds

    private static final List<String> PERSISTENT_IDS = Collections.unmodifiableList(Arrays.asList(
            "service",
            "send",
            "notification",
            "progress",
            "update",
            "announcements",
            "warning",
            "error",
            "alerts",
            "LEAKCANARY_LOW",
            "LEAKCANARY_MAX"
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
        createNotificationChannel(nm, service);

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
        createNotificationChannel(nm, send);

        // Notify
        NotificationChannel notification = new NotificationChannel(
                "notification", context.getString(R.string.channel_notification),
                NotificationManager.IMPORTANCE_HIGH);
        notification.setDescription(context.getString(R.string.channel_notification_description));
        notification.enableLights(true);
        notification.setLightColor(Color.YELLOW);
        notification.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        //notification.setBypassDnd(true);
        createNotificationChannel(nm, notification);

        NotificationChannel progress = new NotificationChannel(
                "progress", context.getString(R.string.channel_progress),
                NotificationManager.IMPORTANCE_DEFAULT);
        notification.setDescription(context.getString(R.string.channel_progress_description));
        progress.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        progress.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        createNotificationChannel(nm, progress);

        if (!Helper.isPlayStoreInstall()) {
            // Update
            NotificationChannel update = new NotificationChannel(
                    "update", context.getString(R.string.channel_update),
                    NotificationManager.IMPORTANCE_HIGH);
            update.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            update.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            createNotificationChannel(nm, update);

            // Announcements
            NotificationChannel announcements = new NotificationChannel(
                    "announcements", context.getString(R.string.channel_announcements),
                    NotificationManager.IMPORTANCE_HIGH);
            announcements.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            announcements.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            createNotificationChannel(nm, announcements);
        }

        // Warnings
        NotificationChannel warning = new NotificationChannel(
                "warning", context.getString(R.string.channel_warning),
                NotificationManager.IMPORTANCE_HIGH);
        warning.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        warning.setBypassDnd(true);
        createNotificationChannel(nm, warning);

        // Errors
        NotificationChannel error = new NotificationChannel(
                "error",
                context.getString(R.string.channel_error),
                NotificationManager.IMPORTANCE_HIGH);
        error.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        error.setBypassDnd(true);
        createNotificationChannel(nm, error);

        // Server alerts
        NotificationChannel alerts = new NotificationChannel(
                "alerts",
                context.getString(R.string.channel_alert),
                NotificationManager.IMPORTANCE_HIGH);
        alerts.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        alerts.setBypassDnd(true);
        createNotificationChannel(nm, alerts);

        // Contacts grouping
        NotificationChannelGroup group = new NotificationChannelGroup(
                "contacts",
                context.getString(R.string.channel_group_contacts));
        createNotificationChannelGroup(nm, group);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNotificationChannel(NotificationManager nm, NotificationChannel channel) {
        try {
            nm.createNotificationChannel(channel);
        } catch (Throwable ex) {
            Log.e(ex);
            /*
            Caused by: java.lang.NullPointerException: Attempt to read from field 'android.os.IInterface com.android.server.notification.ManagedServices$ManagedServiceInfo.service' on a null object reference in method 'com.android.server.notification.ManagedServices$ManagedServiceInfo com.android.server.notification.ManagedServices.getServiceFromTokenLocked(android.os.IInterface)'
                at android.os.Parcel.createExceptionOrNull(Parcel.java:3017)
                at android.os.Parcel.createException(Parcel.java:2995)
                at android.os.Parcel.readException(Parcel.java:2978)
                at android.os.Parcel.readException(Parcel.java:2920)
                at android.app.INotificationManager$Stub$Proxy.createNotificationChannels(INotificationManager.java:3583)
                at android.app.NotificationManager.createNotificationChannels(NotificationManager.java:929)
                at android.app.NotificationManager.createNotificationChannel(NotificationManager.java:917)
                at eu.faircode.email.a0.a(Unknown Source:0)
                at eu.faircode.email.NotificationHelper.createNotificationChannels(SourceFile:54)
                at eu.faircode.email.ApplicationEx.onCreate(SourceFile:137)
                at android.app.Instrumentation.callApplicationOnCreate(Instrumentation.java:1278)
                at android.app.ActivityThread.handleBindApplication(ActivityThread.java:7083)
                ... 9 more
            Caused by: android.os.RemoteException: Remote stack trace:
                at com.android.server.notification.ManagedServices.getServiceFromTokenLocked(ManagedServices.java:1056)
                at com.android.server.notification.ManagedServices.isServiceTokenValidLocked(ManagedServices.java:1065)
                at com.android.server.notification.NotificationManagerService.isInteractionVisibleToListener(NotificationManagerService.java:10237)
                at com.android.server.notification.NotificationManagerService.-$$Nest$misInteractionVisibleToListener(Unknown Source:0)
                at com.android.server.notification.NotificationManagerService$NotificationListeners.notifyNotificationChannelChanged(NotificationManagerService.java:11498)
             */
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNotificationChannelGroup(NotificationManager nm, NotificationChannelGroup group) {
        try {
            nm.createNotificationChannelGroup(group);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static boolean areNotificationsEnabled(NotificationManager nm) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return true;
        else
            return nm.areNotificationsEnabled();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static String[] getChannelIds(Context context) {
        List<String> result = new ArrayList();

        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
        for (NotificationChannel channel : nm.getNotificationChannels()) {
            String id = channel.getId();
            if (!PERSISTENT_IDS.contains(id))
                result.add(id);
        }

        Collections.sort(result);

        return result.toArray(new String[0]);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static void deleteChannel(Context context, String id) {
        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
        nm.deleteNotificationChannel(id);
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
        if (sound != null) {
            jchannel.put("sound", sound.toString());
            AudioAttributes attr = channel.getAudioAttributes();
            try {
                jchannel.put("sound_content_type", attr.getContentType());
                jchannel.put("sound_usage", attr.getUsage());
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

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

        if (jchannel.has("sound") && !jchannel.isNull("sound"))
            try {
                Uri uri = Uri.parse(jchannel.getString("sound"));
                AudioAttributes attr;
                try {
                    AudioAttributes.Builder builder = new AudioAttributes.Builder();
                    if (jchannel.has("sound_content_type"))
                        builder.setContentType(jchannel.getInt("sound_content_type"));
                    else
                        builder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
                    if (jchannel.has("sound_usage"))
                        builder.setUsage(jchannel.getInt("sound_usage"));
                    else
                        builder.setUsage(AudioAttributes.USAGE_NOTIFICATION);
                    attr = builder.build();
                } catch (Throwable ex) {
                    Log.e(ex);
                    attr = Notification.AUDIO_ATTRIBUTES_DEFAULT;
                }
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                if (ringtone != null)
                    channel.setSound(uri, attr);
            } catch (Throwable ex) {
                Log.e(ex);
            }

        channel.enableLights(jchannel.getBoolean("light"));
        channel.enableVibration(jchannel.getBoolean("vibrate"));

        return channel;
    }

    static void notifyMessages(Context context, List<TupleMessageEx> messages, NotificationHelper.NotificationData data, boolean foreground) {
        if (messages == null)
            messages = new ArrayList<>();

        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
        if (nm == null)
            return;

        DB db = DB.getInstance(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean badge = prefs.getBoolean("badge", true);
        boolean notify_background_only = prefs.getBoolean("notify_background_only", false);
        boolean notify_summary = prefs.getBoolean("notify_summary", false);
        boolean notify_preview = prefs.getBoolean("notify_preview", true);
        boolean notify_preview_only = prefs.getBoolean("notify_preview_only", false);
        boolean notify_screen_on = prefs.getBoolean("notify_screen_on", false);
        boolean wearable_preview = prefs.getBoolean("wearable_preview", false);
        boolean biometrics = prefs.getBoolean("biometrics", false);
        String pin = prefs.getString("pin", null);
        boolean biometric_notify = prefs.getBoolean("biometrics_notify", true);
        boolean pro = ActivityBilling.isPro(context);

        boolean redacted = ((biometrics || !TextUtils.isEmpty(pin)) && !biometric_notify);
        if (redacted)
            notify_summary = true;
        if (notify_screen_on &&
                !(BuildConfig.DEBUG ||
                        Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU ||
                        Helper.hasPermission(context, Manifest.permission.TURN_SCREEN_ON)))
            notify_screen_on = false;

        Log.i("Notify messages=" + messages.size() +
                " biometrics=" + biometrics + "/" + biometric_notify +
                " summary=" + notify_summary);

        Map<Long, Integer> newMessages = new HashMap<>();

        Map<Long, List<TupleMessageEx>> groupMessages = new HashMap<>();
        for (long group : data.groupNotifying.keySet())
            groupMessages.put(group, new ArrayList<>());

        Map<String, Boolean> channelIdDisabled = new HashMap<>();

        // Current
        for (TupleMessageEx message : messages) {
            if (message.notifying == EntityMessage.NOTIFYING_IGNORE) {
                Log.e("Notify ignore");
                continue;
            }

            // Check if notification channel enabled
            if (message.notifying == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pro) {
                // Disabling a channel for a sender or folder doesn't disable notifications
                // because the (account) summary notification isn't disabled
                // So, suppress notifications here

                String mChannelId = message.getNotificationChannelId();
                if (mChannelId != null && !channelIdDisabled.containsKey(mChannelId)) {
                    NotificationChannel channel = nm.getNotificationChannel(mChannelId);
                    channelIdDisabled.put(mChannelId,
                            channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE);
                }

                String fChannelId = EntityFolder.getNotificationChannelId(message.folder);
                if (!channelIdDisabled.containsKey(fChannelId)) {
                    NotificationChannel channel = nm.getNotificationChannel(fChannelId);
                    channelIdDisabled.put(fChannelId,
                            channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE);
                }

                if (Boolean.TRUE.equals(channelIdDisabled.get(fChannelId)) ||
                        (mChannelId != null && Boolean.TRUE.equals(channelIdDisabled.get(mChannelId)))) {
                    db.message().setMessageUiIgnored(message.id, true);
                    Log.i("Notify disabled=" + message.id +
                            " " + mChannelId + "=" + channelIdDisabled.get(mChannelId) +
                            " " + fChannelId + "=" + channelIdDisabled.get(fChannelId));
                    continue;
                }
            }

            if (notify_preview && notify_preview_only && !message.content)
                continue;

            if (foreground && notify_background_only && message.notifying == 0) {
                Log.i("Notify foreground=" + message.id);
                if (!message.ui_ignored)
                    db.message().setMessageUiIgnored(message.id, true);
                continue;
            }

            long group = (pro && message.accountNotify ? message.account : 0);
            if (!message.folderUnified)
                group = -message.folder;
            if (!data.groupNotifying.containsKey(group))
                data.groupNotifying.put(group, new ArrayList<>());
            if (!groupMessages.containsKey(group))
                groupMessages.put(group, new ArrayList<>());

            if (message.notifying == 0) {
                // Handle clear notifying on boot/update
                EntityMessage msg = db.message().getMessage(message.id);
                if (msg != null && msg.notifying == 0) {
                    Log.i("Notify boot=" + msg.id);
                    data.groupNotifying.get(group).remove(msg.id);
                    data.groupNotifying.get(group).remove(-msg.id);
                }
            } else {
                long id = message.id * message.notifying;
                if (!data.groupNotifying.get(group).contains(id) &&
                        !data.groupNotifying.get(group).contains(-id)) {
                    Log.i("Notify database=" + id);
                    data.groupNotifying.get(group).add(id);
                }
            }

            if (message.ui_seen || message.ui_ignored || message.ui_hide)
                Log.i("Notify id=" + message.id +
                        " seen=" + message.ui_seen +
                        " ignored=" + message.ui_ignored +
                        " hide=" + message.ui_hide);
            else {
                // Prevent reappearing notifications
                EntityMessage msg = db.message().getMessage(message.id);
                if (msg == null || msg.ui_ignored) {
                    Log.i("Notify skip id=" + message.id + " msg=" + (msg != null));
                    continue;
                }

                Integer current = newMessages.get(group);
                newMessages.put(group, current == null ? 1 : current + 1);

                // This assumes the messages are properly ordered
                if (groupMessages.get(group).size() < MAX_NOTIFICATION_COUNT)
                    groupMessages.get(group).add(message);
                else {
                    EntityLog.log(context, "Notify max group=" + group +
                            " count=" + groupMessages.get(group).size() + "/" + MAX_NOTIFICATION_COUNT);
                    db.message().setMessageUiIgnored(message.id, true);
                }
            }
        }

        // Difference
        boolean flash = false;
        for (long group : groupMessages.keySet()) {
            List<Long> add = new ArrayList<>();
            List<Long> update = new ArrayList<>();
            List<Long> remove = new ArrayList<>(data.groupNotifying.get(group));
            for (int m = 0; m < groupMessages.get(group).size(); m++) {
                TupleMessageEx message = groupMessages.get(group).get(m);
                if (m >= MAX_NOTIFICATION_DISPLAY) {
                    // This is to prevent notification sounds when shifting messages up
                    if (!message.ui_silent) {
                        Log.i("Notify silence=" + message.id);
                        db.message().setMessageUiSilent(message.id, true);
                    }
                    continue;
                }

                long id = (message.content ? message.id : -message.id);
                if (remove.contains(id)) {
                    remove.remove(id);
                    Log.i("Notify existing=" + id);
                } else {
                    boolean existing = remove.contains(-id);
                    if (existing) {
                        if (message.content && notify_preview) {
                            Log.i("Notify preview=" + id);
                            add.add(id);
                            update.add(id);
                        }
                        remove.remove(-id);
                    } else {
                        flash = true;
                        add.add(id);
                    }
                    Log.i("Notify adding=" + id + " existing=" + existing);
                }
            }

            Integer prev = prefs.getInt("new_messages." + group, 0);
            Integer current = newMessages.get(group);
            if (current == null)
                current = 0;
            prefs.edit().putInt("new_messages." + group, current).apply();

            if (prev.equals(current) &&
                    remove.size() + add.size() == 0) {
                Log.i("Notify unchanged");
                continue;
            }

            boolean summary = (notify_summary ||
                    (group != 0 &&
                            groupMessages.get(group).size() > 0 &&
                            groupMessages.get(group).get(0).accountSummary));

            // Build notifications
            List<NotificationCompat.Builder> notifications = getNotificationUnseen(context,
                    group, groupMessages.get(group),
                    summary, current - prev, current,
                    redacted);

            Log.i("Notify group=" + group +
                    " new=" + prev + "/" + current +
                    " count=" + notifications.size() +
                    " add=" + add.size() +
                    " update=" + update.size() +
                    " remove=" + remove.size());

            for (Long id : remove) {
                String tag = "unseen." + group + "." + Math.abs(id);
                EntityLog.log(context, EntityLog.Type.Notification,
                        null, null, id == 0 ? null : Math.abs(id),
                        "Notify cancel tag=" + tag + " id=" + id);
                nm.cancel(tag, NotificationHelper.NOTIFICATION_TAGGED);

                data.groupNotifying.get(group).remove(id);
                db.message().setMessageNotifying(Math.abs(id), 0);
            }

            if (notifications.size() == 0) {
                String tag = "unseen." + group + "." + 0;
                EntityLog.log(context, EntityLog.Type.Notification,
                        "Notify cancel tag=" + tag);
                nm.cancel(tag, NotificationHelper.NOTIFICATION_TAGGED);
            }

            for (Long id : add) {
                data.groupNotifying.get(group).add(id);
                data.groupNotifying.get(group).remove(-id);
                db.message().setMessageNotifying(Math.abs(id), (int) Math.signum(id));
            }

            for (NotificationCompat.Builder builder : notifications) {
                long id = builder.getExtras().getLong("id", 0);
                if ((id == 0 && !prev.equals(current)) || add.contains(id)) {
                    // https://developer.android.com/training/wearables/notifications/bridger#non-bridged
                    if (id == 0) {
                        if (!summary)
                            builder.setLocalOnly(true);
                    } else {
                        if (wearable_preview ? id < 0 : update.contains(id))
                            builder.setLocalOnly(true);
                    }

                    String tag = "unseen." + group + "." + Math.abs(id);
                    Notification notification = builder.build();
                    EntityLog.log(context, EntityLog.Type.Notification,
                            null, null, id == 0 ? null : Math.abs(id),
                            "Notifying tag=" + tag +
                                    " id=" + id + " group=" + notification.getGroup() +
                                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.O
                                            ? " sdk=" + Build.VERSION.SDK_INT
                                            : " channel=" + notification.getChannelId()) +
                                    " sort=" + notification.getSortKey());
                    try {
                        if (NotificationHelper.areNotificationsEnabled(nm)) {
                            nm.notify(tag, NotificationHelper.NOTIFICATION_TAGGED, notification);
                            if (update.contains(id))
                                try {
                                    Log.i("Notify delay id=" + id);
                                    Thread.sleep(NOTIFY_DELAY);
                                } catch (InterruptedException ex) {
                                    Log.w(ex);
                                }
                        }

                        // https://github.com/leolin310148/ShortcutBadger/wiki/Xiaomi-Device-Support
                        if (id == 0 && badge && Helper.isXiaomi())
                            ShortcutBadgerAlt.applyNotification(context, notification, current);
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                }
            }
        }

        if (notify_screen_on && flash) {
            EntityLog.log(context, EntityLog.Type.Notification, "Notify screen on");
            PowerManager pm = Helper.getSystemService(context, PowerManager.class);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    BuildConfig.APPLICATION_ID + ":notification");
            wakeLock.acquire(SCREEN_ON_DURATION);
        }
    }

    private static List<NotificationCompat.Builder> getNotificationUnseen(
            Context context,
            long group, List<TupleMessageEx> messages,
            boolean notify_summary, int new_messages, int total_messages, boolean redacted) {
        List<NotificationCompat.Builder> notifications = new ArrayList<>();

        // Android 7+ N https://developer.android.com/training/notify-user/group
        // Android 8+ O https://developer.android.com/training/notify-user/channels
        // Android 7+ N https://android-developers.googleblog.com/2016/06/notifications-in-android-n.html

        // Group
        // < 0: folder
        // = 0: unified
        // > 0: account

        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
        if (messages == null || messages.size() == 0 || nm == null)
            return notifications;

        boolean pro = ActivityBilling.isPro(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notify_grouping = prefs.getBoolean("notify_grouping", true);
        boolean notify_private = prefs.getBoolean("notify_private", true);
        boolean notify_newest_first = prefs.getBoolean("notify_newest_first", false);
        MessageHelper.AddressFormat email_format = MessageHelper.getAddressFormat(context);
        boolean prefer_contact = prefs.getBoolean("prefer_contact", false);
        boolean flags = prefs.getBoolean("flags", true);
        boolean notify_messaging = prefs.getBoolean("notify_messaging", false);
        boolean notify_subtext = prefs.getBoolean("notify_subtext", true);
        boolean notify_preview = prefs.getBoolean("notify_preview", true);
        boolean notify_preview_all = prefs.getBoolean("notify_preview_all", false);
        boolean wearable_preview = prefs.getBoolean("wearable_preview", false);
        boolean notify_trash = (prefs.getBoolean("notify_trash", true) || !pro);
        boolean notify_junk = (prefs.getBoolean("notify_junk", false) && pro);
        boolean notify_archive = (prefs.getBoolean("notify_archive", true) || !pro);
        boolean notify_move = (prefs.getBoolean("notify_move", false) && pro);
        boolean notify_reply = (prefs.getBoolean("notify_reply", false) && pro);
        boolean notify_reply_direct = (prefs.getBoolean("notify_reply_direct", false) && pro);
        boolean notify_flag = (prefs.getBoolean("notify_flag", false) && flags && pro);
        boolean notify_seen = (prefs.getBoolean("notify_seen", true) || !pro);
        boolean notify_hide = (prefs.getBoolean("notify_hide", false) && pro);
        boolean notify_snooze = (prefs.getBoolean("notify_snooze", false) && pro);
        boolean notify_remove = prefs.getBoolean("notify_remove", true);
        boolean light = prefs.getBoolean("light", false);
        String sound = prefs.getString("sound", null);
        boolean alert_once = prefs.getBoolean("alert_once", true);
        boolean perform_expunge = prefs.getBoolean("perform_expunge", true);
        boolean delete_notification = prefs.getBoolean("delete_notification", false);

        // Get contact info
        Long latest = null;
        Map<Long, Address[]> messageFrom = new HashMap<>();
        Map<Long, ContactInfo[]> messageInfo = new HashMap<>();
        for (int m = 0; m < messages.size() && m < MAX_NOTIFICATION_DISPLAY; m++) {
            TupleMessageEx message = messages.get(m);

            if (latest == null || latest < message.received)
                latest = message.received;

            ContactInfo[] info = ContactInfo.get(context,
                    message.account, message.folderType,
                    message.bimi_selector, Boolean.TRUE.equals(message.dmarc),
                    message.isForwarder() ? message.submitter : message.from);

            Address[] modified = (message.from == null
                    ? new InternetAddress[0]
                    : Arrays.copyOf(message.from, message.from.length));
            for (int i = 0; i < modified.length; i++) {
                String displayName = info[i].getDisplayName();
                if (!TextUtils.isEmpty(displayName)) {
                    String email = ((InternetAddress) modified[i]).getAddress();
                    String personal = ((InternetAddress) modified[i]).getPersonal();
                    if (TextUtils.isEmpty(personal) || prefer_contact)
                        try {
                            modified[i] = new InternetAddress(email, displayName, StandardCharsets.UTF_8.name());
                        } catch (UnsupportedEncodingException ex) {
                            Log.w(ex);
                        }
                }
            }

            messageInfo.put(message.id, info);
            messageFrom.put(message.id, modified);
        }

        // Summary notification
        if (notify_summary ||
                (notify_grouping && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) {
            // Build pending intents
            Intent content;
            if (group < 0) {
                content = new Intent(context, ActivityView.class)
                        .setAction("folder:" + (-group) + (notify_remove ? ":" + group : ""));
                if (messages.size() > 0)
                    content.putExtra("type", messages.get(0).folderType);
            } else
                content = new Intent(context, ActivityView.class)
                        .setAction("unified" + (notify_remove ? ":" + group : ""));
            content.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent piContent = PendingIntentCompat.getActivity(
                    context, ActivityView.PI_UNIFIED, content, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent clear = new Intent(context, ServiceUI.class).setAction("clear:" + group);
            PendingIntent piClear = PendingIntentCompat.getService(
                    context, ServiceUI.PI_CLEAR, clear, PendingIntent.FLAG_UPDATE_CURRENT);

            // Build title
            String title = context.getResources().getQuantityString(
                    R.plurals.title_notification_unseen, total_messages, total_messages);

            long cgroup = (group >= 0
                    ? group
                    : (pro && messages.size() > 0 && messages.get(0).accountNotify ? messages.get(0).account : 0));

            // Build notification
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, EntityAccount.getNotificationChannelId(cgroup))
                            .setSmallIcon(messages.size() > 1
                                    ? R.drawable.baseline_mail_more_white_24
                                    : R.drawable.baseline_mail_white_24)
                            .setContentTitle(title)
                            .setContentIntent(piContent)
                            .setNumber(total_messages)
                            .setDeleteIntent(piClear)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setCategory(notify_summary
                                    ? NotificationCompat.CATEGORY_EMAIL : NotificationCompat.CATEGORY_STATUS)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setAllowSystemGeneratedContextualActions(false);

            if (latest != null)
                builder.setWhen(latest).setShowWhen(true);

            if (group != 0 && messages.size() > 0)
                builder.setSubText(messages.get(0).accountName);

            if (notify_summary) {
                builder.setOnlyAlertOnce(new_messages <= 0);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    if (new_messages > 0)
                        setLightAndSound(builder, light, sound);
                    else
                        builder.setSound(null);
            } else {
                builder
                        .setGroup(Long.toString(group))
                        .setGroupSummary(true)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    builder.setSound(null);
            }

            if (pro) {
                Integer color = null;
                for (TupleMessageEx message : messages) {
                    Integer mcolor = getColor(message);
                    if (mcolor == null) {
                        color = null;
                        break;
                    } else if (color == null)
                        color = mcolor;
                    else if (!color.equals(mcolor)) {
                        color = null;
                        break;
                    }
                }

                if (color != null) {
                    builder.setColor(color);
                    builder.setColorized(true);
                }
            }

            // Subtext should not be set, to show number of new messages

            if (notify_private) {
                Notification pub = builder.build();
                builder
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setPublicVersion(pub);
            }

            if (notify_preview)
                if (redacted)
                    builder.setContentText(context.getString(R.string.title_notification_redacted));
                else {
                    DateFormat DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
                    StringBuilder sb = new StringBuilder();
                    for (EntityMessage message : messages) {
                        Address[] afrom = messageFrom.get(message.id);
                        String from = MessageHelper.formatAddresses(afrom, email_format, false);
                        sb.append("<strong>").append(Html.escapeHtml(from)).append("</strong>");
                        if (!TextUtils.isEmpty(message.subject))
                            sb.append(": ").append(Html.escapeHtml(message.subject));
                        sb.append(" ").append(Html.escapeHtml(DTF.format(message.received)));
                        sb.append("<br>");
                    }

                    // Wearables
                    builder.setContentText(title);

                    // Device
                    builder.setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(HtmlHelper.fromHtml(sb.toString(), context))
                            .setSummaryText(title));
                }

            //builder.extend(new NotificationCompat.WearableExtender()
            //        .setDismissalId(BuildConfig.APPLICATION_ID));

            notifications.add(builder);
        }

        if (notify_summary)
            return notifications;

        // Message notifications
        for (int m = 0; m < messages.size() && m < MAX_NOTIFICATION_DISPLAY; m++) {
            TupleMessageEx message = messages.get(m);
            ContactInfo[] info = messageInfo.get(message.id);

            // Build arguments
            long id = (message.content ? message.id : -message.id);
            Bundle args = new Bundle();
            args.putLong("id", id);

            // Build pending intents
            Intent thread = new Intent(context, ActivityView.class);
            thread.setAction("thread:" + message.id);
            thread.putExtra("group", group);
            thread.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            thread.putExtra("account", message.account);
            thread.putExtra("folder", message.folder);
            thread.putExtra("type", message.folderType);
            thread.putExtra("thread", message.thread);
            thread.putExtra("filter_archive", !EntityFolder.ARCHIVE.equals(message.folderType));
            thread.putExtra("ignore", notify_remove);
            PendingIntent piContent = PendingIntentCompat.getActivity(
                    context, ActivityView.PI_THREAD, thread, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent ignore = new Intent(context, ServiceUI.class).setAction("ignore:" + message.id);
            PendingIntent piIgnore = PendingIntentCompat.getService(
                    context, ServiceUI.PI_IGNORED, ignore, PendingIntent.FLAG_UPDATE_CURRENT);

            // Get channel name
            String channelName = EntityAccount.getNotificationChannelId(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pro) {
                NotificationChannel channel = null;

                String channelId = message.getNotificationChannelId();
                if (channelId != null)
                    channel = nm.getNotificationChannel(channelId);

                if (channel == null)
                    channel = nm.getNotificationChannel(EntityFolder.getNotificationChannelId(message.folder));

                if (channel == null) {
                    if (message.accountNotify)
                        channelName = EntityAccount.getNotificationChannelId(message.account);
                } else
                    channelName = channel.getId();
            }

            String sortKey = String.format(Locale.ROOT, "%13d",
                    notify_newest_first ? (10000000000000L - message.received) : message.received);

            NotificationCompat.Builder mbuilder =
                    new NotificationCompat.Builder(context, channelName)
                            .addExtras(args)
                            .setSmallIcon(R.drawable.baseline_mail_white_24)
                            .setContentIntent(piContent)
                            .setWhen(message.received)
                            .setShowWhen(true)
                            .setSortKey(sortKey)
                            .setDeleteIntent(piIgnore)
                            .setPriority(EntityMessage.PRIORITIY_HIGH.equals(message.importance)
                                    ? NotificationCompat.PRIORITY_HIGH
                                    : NotificationCompat.PRIORITY_DEFAULT)
                            .setCategory(NotificationCompat.CATEGORY_EMAIL)
                            .setVisibility(notify_private
                                    ? NotificationCompat.VISIBILITY_PRIVATE
                                    : NotificationCompat.VISIBILITY_PUBLIC)
                            .setOnlyAlertOnce(alert_once)
                            .setAllowSystemGeneratedContextualActions(false);

            if (message.ui_silent) {
                mbuilder.setSilent(true);
                Log.i("Notify silent=" + message.id);
            }
            if (message.ui_local_only) {
                mbuilder.setLocalOnly(true);
                Log.i("Notify local=" + message.id);
            }

            if (notify_messaging) {
                // https://developer.android.com/training/cars/messaging
                String meName = MessageHelper.formatAddresses(message.to, email_format, false);
                String youName = MessageHelper.formatAddresses(message.from, email_format, false);

                // Names cannot be empty
                if (TextUtils.isEmpty(meName))
                    meName = "-";
                if (TextUtils.isEmpty(youName))
                    youName = "-";

                Person.Builder me = new Person.Builder().setName(meName);
                Person.Builder you = new Person.Builder().setName(youName);

                if (info[0].hasPhoto())
                    you.setIcon(IconCompat.createWithBitmap(info[0].getPhotoBitmap()));

                if (info[0].hasLookupUri())
                    you.setUri(info[0].getLookupUri().toString());

                NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(me.build());

                if (!TextUtils.isEmpty(message.subject))
                    messagingStyle.setConversationTitle(message.subject);

                messagingStyle.addMessage(
                        notify_preview && message.preview != null ? message.preview : "",
                        message.received,
                        you.build());

                mbuilder.setStyle(messagingStyle);
            }

            if (notify_grouping && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                mbuilder
                        .setGroup(Long.toString(group))
                        .setGroupSummary(false)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                setLightAndSound(mbuilder, light, sound);

            Address[] afrom = messageFrom.get(message.id);
            String from = MessageHelper.formatAddresses(afrom, email_format, false);
            mbuilder.setContentTitle(from);
            if (notify_subtext)
                if (message.folderUnified && EntityFolder.INBOX.equals(message.folderType))
                    mbuilder.setSubText(message.accountName);
                else
                    mbuilder.setSubText(message.accountName + " - " + message.getFolderName(context));

            DB db = DB.getInstance(context);

            if (message.content && notify_preview) {
                // Android will truncate the text
                String preview = message.preview;
                if (notify_preview_all)
                    try {
                        File file = message.getFile(context);
                        preview = HtmlHelper.getFullText(context, file);
                        if (preview != null && preview.length() > MAX_PREVIEW)
                            preview = preview.substring(0, MAX_PREVIEW);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                // Wearables
                StringBuilder sb = new StringBuilder();
                if (!TextUtils.isEmpty(message.subject))
                    sb.append(TextHelper.normalizeNotification(context, message.subject));
                if (wearable_preview && !TextUtils.isEmpty(preview)) {
                    if (sb.length() > 0)
                        sb.append(" - ");
                    sb.append(TextHelper.normalizeNotification(context, preview));
                }
                if (sb.length() > 0)
                    mbuilder.setContentText(sb.toString());

                // Device
                if (!notify_messaging) {
                    StringBuilder sbm = new StringBuilder();

                    if (message.keywords != null && BuildConfig.DEBUG)
                        for (String keyword : message.keywords)
                            if (keyword.startsWith("!"))
                                sbm.append(Html.escapeHtml(keyword)).append(": ");

                    if (!TextUtils.isEmpty(message.subject))
                        sbm.append("<em>").append(Html.escapeHtml(message.subject)).append("</em>").append("<br>");

                    if (!TextUtils.isEmpty(preview))
                        sbm.append(Html.escapeHtml(preview));

                    if (sbm.length() > 0) {
                        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle()
                                .bigText(HtmlHelper.fromHtml(sbm.toString(), context));
                        if (!TextUtils.isEmpty(message.subject))
                            bigText.setSummaryText(message.subject);

                        mbuilder.setStyle(bigText);
                    }
                }
            } else {
                if (!TextUtils.isEmpty(message.subject))
                    mbuilder.setContentText(TextHelper.normalizeNotification(context, message.subject));
            }

            if (info[0].hasPhoto())
                mbuilder.setLargeIcon(info[0].getPhotoBitmap());

            if (info[0].hasLookupUri()) {
                Person.Builder you = new Person.Builder()
                        .setUri(info[0].getLookupUri().toString());
                mbuilder.addPerson(you.build());
            }

            if (pro) {
                Integer color = getColor(message);
                if (color != null) {
                    mbuilder.setColor(color);
                    mbuilder.setColorized(true);
                }
            }

            // Notification actions

            List<NotificationCompat.Action> wactions = new ArrayList<>();

            if (notify_trash &&
                    !delete_notification &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP && perform_expunge) {
                EntityFolder folder = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                if (folder != null && !folder.id.equals(message.folder)) {
                    Intent trash = new Intent(context, ServiceUI.class)
                            .setAction("trash:" + message.id)
                            .putExtra("group", group);
                    PendingIntent piTrash = PendingIntentCompat.getService(
                            context, ServiceUI.PI_TRASH, trash, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action.Builder actionTrash = new NotificationCompat.Action.Builder(
                            R.drawable.twotone_delete_24,
                            context.getString(R.string.title_advanced_notify_action_trash),
                            piTrash)
                            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_DELETE)
                            .setShowsUserInterface(false)
                            .setAllowGeneratedReplies(false);
                    mbuilder.addAction(actionTrash.build());

                    wactions.add(actionTrash.build());
                }
            } else if (notify_trash &&
                    (delete_notification ||
                            (message.accountProtocol == EntityAccount.TYPE_POP && message.accountLeaveDeleted) ||
                            (message.accountProtocol == EntityAccount.TYPE_IMAP && !perform_expunge))) {
                Intent delete = new Intent(context, ServiceUI.class)
                        .setAction("delete:" + message.id)
                        .putExtra("group", group);
                PendingIntent piDelete = PendingIntentCompat.getService(
                        context, ServiceUI.PI_DELETE, delete, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionDelete = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_delete_forever_24,
                        context.getString(R.string.title_delete_permanently),
                        piDelete)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_DELETE)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                mbuilder.addAction(actionDelete.build());

                wactions.add(actionDelete.build());
            }

            if (notify_junk &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP) {
                EntityFolder folder = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
                if (folder != null && !folder.id.equals(message.folder)) {
                    Intent junk = new Intent(context, ServiceUI.class)
                            .setAction("junk:" + message.id)
                            .putExtra("group", group);
                    PendingIntent piJunk = PendingIntentCompat.getService(
                            context, ServiceUI.PI_JUNK, junk, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action.Builder actionJunk = new NotificationCompat.Action.Builder(
                            R.drawable.twotone_report_24,
                            context.getString(R.string.title_advanced_notify_action_junk),
                            piJunk)
                            .setShowsUserInterface(false)
                            .setAllowGeneratedReplies(false);
                    mbuilder.addAction(actionJunk.build());

                    wactions.add(actionJunk.build());
                }
            }

            if (notify_archive &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP) {
                EntityFolder folder = db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE);
                if (folder != null && !folder.id.equals(message.folder)) {
                    Intent archive = new Intent(context, ServiceUI.class)
                            .setAction("archive:" + message.id)
                            .putExtra("group", group);
                    PendingIntent piArchive = PendingIntentCompat.getService(
                            context, ServiceUI.PI_ARCHIVE, archive, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action.Builder actionArchive = new NotificationCompat.Action.Builder(
                            R.drawable.twotone_archive_24,
                            context.getString(R.string.title_advanced_notify_action_archive),
                            piArchive)
                            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_ARCHIVE)
                            .setShowsUserInterface(false)
                            .setAllowGeneratedReplies(false);
                    mbuilder.addAction(actionArchive.build());

                    wactions.add(actionArchive.build());
                }
            }

            if (notify_move &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP) {
                EntityAccount account = db.account().getAccount(message.account);
                if (account != null && account.move_to != null) {
                    EntityFolder folder = db.folder().getFolder(account.move_to);
                    if (folder != null && !folder.id.equals(message.folder)) {
                        Intent move = new Intent(context, ServiceUI.class)
                                .setAction("move:" + message.id)
                                .putExtra("group", group);
                        PendingIntent piMove = PendingIntentCompat.getService(
                                context, ServiceUI.PI_MOVE, move, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Action.Builder actionMove = new NotificationCompat.Action.Builder(
                                R.drawable.twotone_folder_24,
                                folder.getDisplayName(context),
                                piMove)
                                .setShowsUserInterface(false)
                                .setAllowGeneratedReplies(false);
                        mbuilder.addAction(actionMove.build());

                        wactions.add(actionMove.build());
                    }
                }
            }

            if (notify_reply && message.content) {
                List<TupleIdentityEx> identities = db.identity().getComposableIdentities(message.account);
                if (identities != null && identities.size() > 0) {
                    Intent reply = new Intent(context, ActivityCompose.class)
                            .setAction("reply:" + message.id)
                            .putExtra("action", "reply")
                            .putExtra("reference", message.id)
                            .putExtra("group", group);
                    reply.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent piReply = PendingIntentCompat.getActivity(
                            context, ActivityCompose.PI_REPLY, reply, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action.Builder actionReply = new NotificationCompat.Action.Builder(
                            R.drawable.twotone_reply_24,
                            context.getString(R.string.title_advanced_notify_action_reply),
                            piReply)
                            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                            .setShowsUserInterface(true)
                            .setAllowGeneratedReplies(false);
                    mbuilder.addAction(actionReply.build());
                }
            }

            if (message.content &&
                    message.identity != null &&
                    message.from != null && message.from.length > 0 &&
                    db.folder().getOutbox() != null) {
                Intent reply = new Intent(context, ServiceUI.class)
                        .setPackage(BuildConfig.APPLICATION_ID)
                        .setAction("reply:" + message.id)
                        .putExtra("group", group);
                PendingIntent piReply = PendingIntentCompat.getService(
                        context, ServiceUI.PI_REPLY_DIRECT, reply, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                NotificationCompat.Action.Builder actionReply = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_reply_24,
                        context.getString(R.string.title_advanced_notify_action_reply_direct),
                        piReply)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                RemoteInput.Builder input = new RemoteInput.Builder("text")
                        .setLabel(context.getString(R.string.title_advanced_notify_action_reply));
                actionReply.addRemoteInput(input.build())
                        .setAllowGeneratedReplies(false);
                if (notify_reply_direct) {
                    mbuilder.addAction(actionReply.build());
                    wactions.add(actionReply.build());
                } else
                    mbuilder.addInvisibleAction(actionReply.build());
            }

            if (notify_flag) {
                Intent flag = new Intent(context, ServiceUI.class)
                        .setAction("flag:" + message.id)
                        .putExtra("group", group);
                PendingIntent piFlag = PendingIntentCompat.getService(
                        context, ServiceUI.PI_FLAG, flag, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionFlag = new NotificationCompat.Action.Builder(
                        R.drawable.baseline_star_24,
                        context.getString(R.string.title_advanced_notify_action_flag),
                        piFlag)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_THUMBS_UP)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                mbuilder.addAction(actionFlag.build());

                wactions.add(actionFlag.build());
            }

            if (true) {
                Intent seen = new Intent(context, ServiceUI.class)
                        .setAction("seen:" + message.id)
                        .putExtra("group", group);
                PendingIntent piSeen = PendingIntentCompat.getService(
                        context, ServiceUI.PI_SEEN, seen, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionSeen = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_visibility_24,
                        context.getString(R.string.title_advanced_notify_action_seen),
                        piSeen)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                if (notify_seen) {
                    mbuilder.addAction(actionSeen.build());
                    wactions.add(actionSeen.build());
                } else
                    mbuilder.addInvisibleAction(actionSeen.build());
            }

            if (notify_hide) {
                Intent hide = new Intent(context, ServiceUI.class)
                        .setAction("hide:" + message.id)
                        .putExtra("group", group);
                PendingIntent piHide = PendingIntentCompat.getService(
                        context, ServiceUI.PI_HIDE, hide, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionHide = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_visibility_off_24,
                        context.getString(R.string.title_advanced_notify_action_hide),
                        piHide)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                mbuilder.addAction(actionHide.build());

                wactions.add(actionHide.build());
            }

            if (notify_snooze) {
                Intent snooze = new Intent(context, ServiceUI.class)
                        .setAction("snooze:" + message.id)
                        .putExtra("group", group);
                PendingIntent piSnooze = PendingIntentCompat.getService(
                        context, ServiceUI.PI_SNOOZE, snooze, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionSnooze = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_timelapse_24,
                        context.getString(R.string.title_advanced_notify_action_snooze),
                        piSnooze)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MUTE)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                mbuilder.addAction(actionSnooze.build());

                wactions.add(actionSnooze.build());
            }

            // https://developer.android.com/training/wearables/notifications
            // https://developer.android.com/reference/androidx/core/app/NotificationCompat.Action.WearableExtender
            mbuilder.extend(new NotificationCompat.WearableExtender()
                            .addActions(wactions)
                            .setDismissalId(BuildConfig.APPLICATION_ID + ":" + id)
                    /* .setBridgeTag(id < 0 ? "header" : "body") */);

            // https://developer.android.com/reference/androidx/core/app/NotificationCompat.CarExtender
            mbuilder.extend(new NotificationCompat.CarExtender());

            notifications.add(mbuilder);
        }

        return notifications;
    }

    private static Integer getColor(TupleMessageEx message) {
        if (!message.folderUnified && message.folderColor != null)
            return message.folderColor;
        return message.accountColor;
    }

    private static void setLightAndSound(NotificationCompat.Builder builder, boolean light, String sound) {
        int def = 0;

        if (light) {
            def |= DEFAULT_LIGHTS;
            Log.i("Notify light enabled");
        }

        if (!"".equals(sound)) {
            // Not silent sound
            Uri uri = (sound == null ? null : Uri.parse(sound));
            if (uri != null && !"content".equals(uri.getScheme()))
                uri = null;
            Log.i("Notify sound=" + uri);

            if (uri == null)
                def |= DEFAULT_SOUND;
            else
                builder.setSound(uri);
        }

        builder.setDefaults(def);
    }

    static class NotificationData {
        private Map<Long, List<Long>> groupNotifying = new HashMap<>();

        NotificationData(Context context) {
            // Get existing notifications
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                try {
                    NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                    for (StatusBarNotification sbn : nm.getActiveNotifications()) {
                        String tag = sbn.getTag();
                        if (tag != null && tag.startsWith("unseen.")) {
                            String[] p = tag.split(("\\."));
                            long group = Long.parseLong(p[1]);
                            long id = sbn.getNotification().extras.getLong("id", 0);

                            if (!groupNotifying.containsKey(group))
                                groupNotifying.put(group, new ArrayList<>());

                            if (id > 0) {
                                EntityLog.log(context, EntityLog.Type.Notification, null, null, id,
                                        "Notify restore " + tag + " id=" + id);
                                groupNotifying.get(group).add(id);
                            }
                        }
                    }
                } catch (Throwable ex) {
                    Log.w(ex);
                /*
                    java.lang.RuntimeException: Unable to create service eu.faircode.email.ServiceSynchronize: java.lang.NullPointerException: Attempt to invoke virtual method 'java.util.List android.content.pm.ParceledListSlice.getList()' on a null object reference
                            at android.app.ActivityThread.handleCreateService(ActivityThread.java:2944)
                            at android.app.ActivityThread.access$1900(ActivityThread.java:154)
                            at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1474)
                            at android.os.Handler.dispatchMessage(Handler.java:102)
                            at android.os.Looper.loop(Looper.java:234)
                            at android.app.ActivityThread.main(ActivityThread.java:5526)
                */
                }
        }
    }
}
