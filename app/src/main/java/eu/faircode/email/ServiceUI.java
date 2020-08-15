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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.preference.PreferenceManager;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class ServiceUI extends IntentService {
    static final int PI_CLEAR = 1;
    static final int PI_TRASH = 2;
    static final int PI_JUNK = 3;
    static final int PI_ARCHIVE = 4;
    static final int PI_MOVE = 5;
    static final int PI_REPLY_DIRECT = 6;
    static final int PI_FLAG = 7;
    static final int PI_SEEN = 8;
    static final int PI_SNOOZE = 9;
    static final int PI_IGNORED = 10;
    static final int PI_THREAD = 11;
    static final int PI_WAKEUP = 12;
    static final int PI_SYNC = 13;
    static final int PI_BANNER = 14;

    static final int HIDE_BANNER = 3; // weeks

    public ServiceUI() {
        this(ServiceUI.class.getName());
    }

    public ServiceUI(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        Log.i("Service UI create");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("Service UI destroy");
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Under certain circumstances, a background app is placed on a temporary whitelist for several minutes
        // - Executing a PendingIntent from a notification.
        // https://developer.android.com/about/versions/oreo/background#services
        Log.i("Service UI intent=" + intent);
        Log.logExtras(intent);

        if (intent == null)
            return;

        String action = intent.getAction();
        if (action == null)
            return;

        try {
            String[] parts = action.split(":");
            long id = (parts.length > 1 ? Long.parseLong(parts[1]) : -1);
            long group = intent.getLongExtra("group", -1);

            switch (parts[0]) {
                case "clear":
                    onClear(id);
                    break;

                case "trash":
                    cancel(group, id);
                    onMove(id, EntityFolder.TRASH);
                    break;

                case "junk":
                    cancel(group, id);
                    onJunk(id);
                    break;

                case "archive":
                    cancel(group, id);
                    onMove(id, EntityFolder.ARCHIVE);
                    break;

                case "move":
                    cancel(group, id);
                    onMove(id);
                    break;

                case "reply":
                    cancel(group, id);
                    onSeen(id);
                    onReplyDirect(id, intent);
                    break;

                case "flag":
                    cancel(group, id);
                    onFlag(id);
                    break;

                case "seen":
                    cancel(group, id);
                    onSeen(id);
                    break;

                case "snooze":
                    cancel(group, id);
                    onSnooze(id);
                    break;

                case "ignore":
                    boolean view = intent.getBooleanExtra("view", false);
                    onIgnore(id, view);
                    break;

                case "wakeup":
                    // AlarmManager.RTC_WAKEUP
                    // When the alarm is dispatched, the app will also be added to the system's temporary whitelist
                    // for approximately 10 seconds to allow that application to acquire further wake locks in which to complete its work.
                    // https://developer.android.com/reference/android/app/AlarmManager
                    onWakeup(id);
                    break;

                case "sync":
                    boolean reschedule = intent.getBooleanExtra("reschedule", false);
                    onSync(id, reschedule);
                    break;

                case "daily":
                case "banner":
                    onBanner();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown UI action: " + parts[0]);
            }

            Map<String, String> crumb = new HashMap<>();
            crumb.put("action", action);
            Log.breadcrumb("serviceui", crumb);

            ServiceSynchronize.eval(this, "ui/" + action);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onClear(long group) {
        DB db = DB.getInstance(this);
        int cleared;
        if (group < 0)
            cleared = db.message().ignoreAll(null, -group);
        else
            cleared = db.message().ignoreAll(group == 0 ? null : group, null);
        Log.i("Cleared=" + cleared);
    }

    private void cancel(long group, long id) {
        String tag = "unseen." + group + ":" + id;

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(tag, 1);
    }

    private void onMove(long id, String folderType) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            EntityFolder folder = db.folder().getFolderByType(message.account, folderType);
            if (folder == null)
                return;

            EntityOperation.queue(this, message, EntityOperation.MOVE, folder.id);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onMove(long id) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            EntityAccount account = db.account().getAccount(message.account);
            if (account == null || account.move_to == null)
                return;

            EntityOperation.queue(this, message, EntityOperation.MOVE, account.move_to);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onJunk(long id) throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean block_sender = prefs.getBoolean("notify_block_sender", false);
        List<String> whitelist = EmailProvider.getDomainNames(this);

        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            EntityFolder junk = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
            if (junk == null)
                return;

            EntityOperation.queue(this, message, EntityOperation.MOVE, junk.id);

            if (block_sender) {
                EntityRule rule = EntityRule.blockSender(this, message, junk, false, whitelist);
                if (rule != null)
                    rule.id = db.rule().insertRule(rule);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onReplyDirect(long id, Intent intent) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefix_once = prefs.getBoolean("prefix_once", true);
        boolean plain_only = prefs.getBoolean("plain_only", false);

        Bundle results = RemoteInput.getResultsFromIntent(intent);
        String body = results.getString("text");
        if (body != null)
            body = "<p>" + body.replaceAll("\\r?\\n", "<br>") + "</p>";

        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage ref = db.message().getMessage(id);
            if (ref == null)
                throw new IllegalArgumentException("message not found");

            EntityIdentity identity = db.identity().getIdentity(ref.identity);
            if (identity == null)
                throw new IllegalArgumentException("identity not found");

            EntityFolder outbox = db.folder().getOutbox();
            if (outbox == null)
                throw new IllegalArgumentException("outbox not found");

            String subject = (ref.subject == null ? "" : ref.subject);
            if (prefix_once) {
                String re = getString(R.string.title_subject_reply, "");
                subject = subject.replaceAll("(?i)" + Pattern.quote(re.trim()), "").trim();
            }

            EntityMessage reply = new EntityMessage();
            reply.account = identity.account;
            reply.folder = outbox.id;
            reply.identity = identity.id;
            reply.msgid = EntityMessage.generateMessageId();
            reply.inreplyto = ref.msgid;
            reply.thread = ref.thread;
            reply.to = ref.from;
            reply.from = new Address[]{new InternetAddress(identity.email, identity.name, StandardCharsets.UTF_8.name())};
            reply.subject = getString(R.string.title_subject_reply, subject);
            reply.received = new Date().getTime();
            reply.seen = true;
            reply.ui_seen = true;
            reply.id = db.message().insertMessage(reply);

            File file = reply.getFile(this);
            Helper.writeText(file, body);

            db.message().setMessageContent(reply.id,
                    true,
                    HtmlHelper.getLanguage(this, body),
                    plain_only || ref.plain_only,
                    HtmlHelper.getPreview(body),
                    null);

            EntityOperation.queue(this, reply, EntityOperation.SEND);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        ServiceSend.start(this);
        ToastEx.makeText(this, R.string.title_queued, Toast.LENGTH_LONG).show();
    }

    private void onFlag(long id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean threading = prefs.getBoolean("threading", true);

        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            List<EntityMessage> messages = db.message().getMessagesByThread(
                    message.account, message.thread, threading ? null : id, message.folder);
            for (EntityMessage threaded : messages) {
                EntityOperation.queue(this, threaded, EntityOperation.FLAG, true);
                EntityOperation.queue(this, threaded, EntityOperation.SEEN, true);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onSeen(long id) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            EntityOperation.queue(this, message, EntityOperation.SEEN, true);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onSnooze(long id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int notify_snooze_duration = prefs.getInt("default_snooze", 1);

        long wakeup = new Date().getTime() + notify_snooze_duration * 3600 * 1000L;

        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            db.message().setMessageSnoozed(id, wakeup);
            db.message().setMessageUiIgnored(message.id, true);
            EntityMessage.snooze(this, id, wakeup);

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    private void onIgnore(long id, boolean open) {
        EntityMessage message;
        EntityFolder folder;

        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            message = db.message().getMessage(id);
            if (message == null)
                return;

            folder = db.folder().getFolder(message.folder);
            if (folder == null)
                return;

            db.message().setMessageUiIgnored(message.id, true);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (open) {
            Intent thread = new Intent(this, ActivityView.class);
            thread.setAction("thread:" + message.thread);
            thread.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            thread.putExtra("account", message.account);
            thread.putExtra("folder", message.folder);
            thread.putExtra("id", message.id);
            thread.putExtra("filter_archive", !EntityFolder.ARCHIVE.equals(folder.type));
            startActivity(thread);
        }
    }

    private void onWakeup(long id) {
        EntityFolder folder;

        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            folder = db.folder().getFolder(message.folder);
            if (folder == null)
                return;

            if (EntityFolder.OUTBOX.equals(folder.type)) {
                Log.i("Delayed send id=" + message.id);
                if (message.ui_snoozed != null) {
                    db.message().setMessageSnoozed(message.id, null);
                    EntityOperation.queue(this, message, EntityOperation.SEND);
                }
            } else {
                if (folder.notify) {
                    List<EntityAttachment> attachments = db.attachment().getAttachments(id);

                    // A new message ID is needed for a new (wearable) notification
                    db.message().deleteMessage(id);

                    message.id = null;
                    message.fts = false;
                    message.id = db.message().insertMessage(message);

                    if (message.content) {
                        File source = EntityMessage.getFile(this, id);
                        File target = message.getFile(this);
                        try {
                            Helper.copy(source, target);
                        } catch (IOException ex) {
                            Log.e(ex);
                            db.message().resetMessageContent(message.id);
                        }
                    }

                    for (EntityAttachment attachment : attachments) {
                        File source = attachment.getFile(this);

                        attachment.id = null;
                        attachment.message = message.id;
                        attachment.progress = null;
                        attachment.id = db.attachment().insertAttachment(attachment);

                        if (attachment.available) {
                            File target = attachment.getFile(this);
                            try {
                                Helper.copy(source, target);
                            } catch (IOException ex) {
                                Log.e(ex);
                                db.attachment().setError(attachment.id, Log.formatThrowable(ex, false));
                            }
                        }
                    }
                }
                db.message().setMessageSnoozed(message.id, null);
                db.message().setMessageUnsnoozed(message.id, true);
                EntityOperation.queue(this, message, EntityOperation.SEEN, false, false);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (EntityFolder.OUTBOX.equals(folder.type))
            ServiceSend.start(this);
    }

    private void onSync(long aid, boolean reschedule) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            List<EntityAccount> accounts = db.account().getPollAccounts(aid < 0 ? null : aid);
            for (EntityAccount account : accounts) {
                List<EntityFolder> folders = db.folder().getSynchronizingFolders(account.id);
                if (folders.size() > 0)
                    Collections.sort(folders, folders.get(0).getComparator(this));
                for (EntityFolder folder : folders)
                    EntityOperation.sync(this, folder.id, false);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (reschedule) {
            long now = new Date().getTime();
            long[] schedule = ServiceSynchronize.getSchedule(this);
            boolean poll = (schedule == null || (now >= schedule[0] && now < schedule[1]));
            schedule(this, poll);
        }
    }

    private void onBanner() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().remove("banner_hidden").apply();
    }

    static void sync(Context context, Long account) {
        context.startService(new Intent(context, ServiceUI.class)
                .setAction(account == null ? "sync" : "sync:" + account));
    }

    static void schedule(Context context, boolean poll) {
        Intent intent = new Intent(context, ServiceUI.class);
        intent.setAction("sync");
        intent.putExtra("reschedule", true);
        PendingIntent piSync = PendingIntent.getService(
                context, ServiceUI.PI_SYNC, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(piSync);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = prefs.getInt("poll_interval", ServiceSynchronize.DEFAULT_POLL_INTERVAL);
        if (poll && enabled && pollInterval > 0) {
            long now = new Date().getTime();
            long interval = pollInterval * 60 * 1000L;
            long next = now + interval - now % interval + 30 * 1000L;

            EntityLog.log(context, "Poll next=" + new Date(next));

            AlarmManagerCompat.setAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, next, piSync);
        }
    }

    private static PendingIntent getBannerIntent(Context context) {
        Intent banner = new Intent(context, ServiceUI.class);
        banner.setAction("banner");
        return PendingIntent.getService(context, ServiceUI.PI_BANNER, banner, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static void scheduleBanner(Context context, boolean set) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (set) {
            long now = new Date().getTime();
            long interval = AlarmManager.INTERVAL_DAY * HIDE_BANNER * 7;
            long due = interval - (now % interval);
            long trigger = now + due;
            Log.i("Set banner alarm at " + new Date(trigger) + " due=" + due);
            am.set(AlarmManager.RTC, trigger, getBannerIntent(context));
            prefs.edit().putLong("banner_hidden", trigger).apply();
        } else {
            Log.i("Cancel banner alarm");
            am.cancel(getBannerIntent(context));
            prefs.edit().remove("banner_hidden").apply();
        }
    }

    static void boot(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long banner_hidden = prefs.getLong("banner_hidden", 0);
        if (banner_hidden > 0) {
            Log.i("Restore banner alarm at " + new Date(banner_hidden));
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC, banner_hidden, getBannerIntent(context));
        }
    }
}
