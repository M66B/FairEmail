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

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.RemoteInput;
import androidx.preference.PreferenceManager;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    static final int PI_HIDE = 9;
    static final int PI_SNOOZE = 10;
    static final int PI_IGNORED = 11;
    static final int PI_DELETE = 12;
    static final int PI_ALARM = 13;

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
        CoalMine.watch(this, this.getClass().getName() + "#onDestroy");
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

                case "delete":
                    cancel(group, id);
                    onDelete(id);
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

                case "hide":
                    cancel(group, id);
                    onHide(id);
                    break;

                case "snooze":
                    cancel(group, id);
                    onSnooze(id);
                    break;

                case "ignore":
                    cancel(group, id);
                    onIgnore(id);
                    break;

                case "wakeup":
                    // ignore
                    break;

                case "sync":
                    onSync(id, -1L, false);
                    break;

                case "widget":
                    onWidget(intent, (int) id);
                    break;

                case "exists":
                    // ignore
                    break;

                case "alarm":
                    onAlarm(intent);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown UI action: " + parts[0]);
            }

            Map<String, String> crumb = new HashMap<>();
            crumb.put("action", action);
            Log.breadcrumb("serviceui", crumb);

            ServiceSynchronize.state(this, true);
            ServiceSynchronize.eval(this, "ui/" + action);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onClear(long group) {
        // Group
        // < 0: folder
        // = 0: unified
        // > 0: account
        DB db = DB.getInstance(this);
        int cleared;
        if (group < 0)
            cleared = db.message().ignoreAll(null, -group, null);
        else
            cleared = db.message().ignoreAll(group == 0 ? null : group, null, null);
        EntityLog.log(this, EntityLog.Type.Notification,
                "Notify clear group=" + group + " cleared=" + cleared);
    }

    private void cancel(long group, long id) {
        // https://issuetracker.google.com/issues/159152393
        String tag = "unseen." + group + ":" + id;

        NotificationManager nm = Helper.getSystemService(this, NotificationManager.class);
        nm.cancel(tag, NotificationHelper.NOTIFICATION_TAGGED);
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

    private void onDelete(long id) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            EntityOperation.queue(this, message, EntityOperation.DELETE);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onJunk(long id) throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean block_sender = prefs.getBoolean("notify_block_sender", false);

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

            if (block_sender)
                EntityContact.update(this,
                        message.account, message.identity, message.from,
                        EntityContact.TYPE_JUNK, message.received);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onReplyDirect(long id, Intent intent) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean plain_only = prefs.getBoolean("plain_only", false);

        DB db = DB.getInstance(this);

        EntityMessage ref = db.message().getMessage(id);
        if (ref == null)
            throw new IllegalArgumentException("message not found");

        EntityIdentity identity = db.identity().getIdentity(ref.identity);
        if (identity == null)
            throw new IllegalArgumentException("identity not found");

        EntityFolder outbox = db.folder().getOutbox();
        if (outbox == null)
            throw new IllegalArgumentException("outbox not found");


        ClipData clip = intent.getClipData();
        Intent inner = (clip != null && clip.getItemCount() > 0 ? clip.getItemAt(0).getIntent() : null);

        Bundle results = RemoteInput.getResultsFromIntent(intent);
        EntityLog.log(this, "Reply direct intent=" + intent +
                " extras: " + TextUtils.join(" ", Log.getExtras(intent.getExtras())) +
                " inner=" + inner + (inner == null ? "" : " extras: " + TextUtils.join(" ", Log.getExtras(inner.getExtras()))) +
                " results: " + Log.getExtras(results));

        Object obj = results.get("text");
        String body = (obj == null ? null : "<p>" + obj.toString().replaceAll("\\r?\\n", "<br>") + "</p>");

        String text = HtmlHelper.getFullText(this, body);
        String language = HtmlHelper.getLanguage(this, ref.subject, text);
        String preview = HtmlHelper.getPreview(text);

        try {
            db.beginTransaction();

            EntityMessage reply = new EntityMessage();
            reply.account = identity.account;
            reply.folder = outbox.id;
            reply.identity = identity.id;
            reply.msgid = EntityMessage.generateMessageId();
            reply.inreplyto = ref.msgid;
            reply.thread = ref.thread;
            reply.to = ref.from;
            reply.from = new Address[]{new InternetAddress(identity.email, identity.name, StandardCharsets.UTF_8.name())};
            reply.subject = EntityMessage.getSubject(this, ref.language, ref.subject, false);
            reply.received = new Date().getTime();
            reply.seen = true;
            reply.ui_seen = true;
            reply.id = db.message().insertMessage(reply);

            File file = reply.getFile(this);
            Helper.writeText(file, body);

            db.message().setMessageContent(reply.id,
                    true,
                    language,
                    plain_only || ref.isPlainOnly() ? 1 : 0,
                    preview,
                    null);

            EntityOperation.queue(this, reply, EntityOperation.SEND);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        ServiceSend.start(this);
    }

    private void onFlag(long id) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            db.message().setMessageUiIgnored(message.id, true);
            EntityOperation.queue(this, message, EntityOperation.FLAG, true);

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

    private void onHide(long id) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            db.message().setMessageSnoozed(message.id, Long.MAX_VALUE);
            db.message().setMessageUiIgnored(message.id, true);

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    private void onSnooze(long id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean flag_snoozed = prefs.getBoolean("flag_snoozed", false);
        int default_snooze = prefs.getInt("default_snooze", 1);
        if (default_snooze == 0)
            default_snooze = 1;

        long wakeup = new Date().getTime() + default_snooze * 3600 * 1000L;

        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            db.message().setMessageSnoozed(id, wakeup);
            db.message().setMessageUiIgnored(message.id, true);
            EntityMessage.snooze(this, id, wakeup);

            if (flag_snoozed)
                EntityOperation.queue(this, message, EntityOperation.FLAG, true);

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    private void onIgnore(long id) {
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
    }

    private void onSync(long aid, long fid, boolean unified) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            List<EntityAccount> accounts = db.account().getPollAccounts(aid < 0 ? null : aid);
            for (EntityAccount account : accounts) {
                List<EntityFolder> folders;
                if (fid < 0)
                    folders = db.folder().getSynchronizingFolders(account.id);
                else
                    folders = Arrays.asList(db.folder().getFolder(fid));
                if (folders.size() > 0)
                    Collections.sort(folders, folders.get(0).getComparator(this));
                for (EntityFolder folder : folders)
                    if (!unified || folder.unified)
                        EntityOperation.sync(this, folder.id, true);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onWidget(Intent intent, int appWidgetId) {
        long aid = intent.getLongExtra("account", -1L);
        long fid = intent.getLongExtra("folder", -1L);
        onSync(aid, fid, fid < 0);
    }

    private void onAlarm(Intent intent) {
        MediaPlayerHelper.stop(this);
    }

    static void sync(Context context, Long account) {
        try {
            Intent sync = new Intent(context, ServiceUI.class)
                    .setAction(account == null ? "sync" : "sync:" + account);
            context.startService(sync);
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.IllegalStateException: Not allowed to start service Intent { act=sync cmp=eu.faircode.email/.ServiceUI }: app is in background uid UidRecord{ac095c9 u0a94 CEM  bg:+9d20h57m50s144ms idle change:cached procs:1 seq(0,0,0)}
                        at android.app.ContextImpl.startServiceCommon(ContextImpl.java:1715)
                        at android.app.ContextImpl.startService(ContextImpl.java:1670)
                        at android.content.ContextWrapper.startService(ContextWrapper.java:720)
                        at eu.faircode.email.ServiceUI.sync(ServiceUI:487)
                        at eu.faircode.email.ServiceTileUnseen.onClick(ServiceTileUnseen:103)
                        at android.service.quicksettings.TileService$H.handleMessage(TileService.java:449)
             */
        }
    }

    static void ignore(Context context, long id, long group) {
        try {
            Intent ignore = new Intent(context, ServiceUI.class)
                    .setAction("ignore:" + id)
                    .putExtra("group", group);
            context.startService(ignore);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }
}
