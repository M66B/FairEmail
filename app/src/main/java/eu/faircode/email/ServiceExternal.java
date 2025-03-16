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

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class ServiceExternal extends ServiceBase {
    private static final String ACTION_POLL = BuildConfig.APPLICATION_ID + ".POLL";
    private static final String ACTION_ENABLE = BuildConfig.APPLICATION_ID + ".ENABLE";
    private static final String ACTION_DISABLE = BuildConfig.APPLICATION_ID + ".DISABLE";
    private static final String ACTION_INTERVAL = BuildConfig.APPLICATION_ID + ".INTERVAL";
    private static final String ACTION_RULE = BuildConfig.APPLICATION_ID + ".RULE";
    private static final String ACTION_TEMPLATE = BuildConfig.APPLICATION_ID + ".TEMPLATE";
    private static final String ACTION_DISCONNECT_ME = BuildConfig.APPLICATION_ID + ".DISCONNECT.ME";
    private static final String ACTION_ADGUARD = BuildConfig.APPLICATION_ID + ".ADGUARD";

    // adb shell am start-foreground-service -a eu.faircode.email.POLL --es account Gmail
    // adb shell am start-foreground-service -a eu.faircode.email.ENABLE --es account Gmail
    // adb shell am start-foreground-service -a eu.faircode.email.DISABLE --es account Gmail
    // adb shell am start-foreground-service -a eu.faircode.email.INTERVAL --ei minutes {0, 15, 30, 60, 120, 240, 480, 1440}
    // adb shell am start-foreground-service -a eu.faircode.email.RULE --es account Gmail -es rule Test
    // adb shell am start-foreground-service -a eu.faircode.email.TEMPLATE --es template ... --es identity ... --es to ... --es cc ... --es subject ...
    // adb shell am start-foreground-service -a eu.faircode.email.DISCONNECT
    // adb shell am start-foreground-service -a eu.faircode.email.ADGUARD

    @Override
    public void onCreate() {
        Log.i("Service external create");
        super.onCreate();
        try {
            startForeground(NotificationHelper.NOTIFICATION_EXTERNAL, getNotification());
            EntityLog.log(this, EntityLog.Type.Debug2,
                    "onCreate class=" + this.getClass().getName());
        } catch (Throwable ex) {
            if (Helper.isPlayStoreInstall())
                Log.i(ex);
            else
                Log.e(ex);
        }
    }

    @Override
    public void onTimeout(int startId) {
        String msg = "onTimeout" +
                " class=" + this.getClass().getName() +
                " ignoring=" + Helper.isIgnoringOptimizations(this);
        Log.e(new Throwable(msg));
        EntityLog.log(this, EntityLog.Type.Debug3, msg);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.i("Service external destroy");
        stopForeground(true);
        super.onDestroy();
        CoalMine.watch(this, this.getClass().getName() + "#onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            EntityLog.log(this, "Service external intent=" + intent);
            Log.logExtras(intent);

            super.onStartCommand(intent, flags, startId);
            try {
                startForeground(NotificationHelper.NOTIFICATION_EXTERNAL, getNotification());
                EntityLog.log(this, EntityLog.Type.Debug2,
                        "onStartCommand class=" + this.getClass().getName());
            } catch (Throwable ex) {
                if (Helper.isPlayStoreInstall())
                    Log.i(ex);
                else
                    Log.e(ex);
            }

            if (intent == null)
                return START_NOT_STICKY;

            final String action = intent.getAction();
            EntityLog.log(this, action);

            final Context context = getApplicationContext();
            Helper.getSerialExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        switch (action) {
                            case ACTION_POLL:
                                poll(context, intent);
                                break;
                            case ACTION_ENABLE:
                            case ACTION_DISABLE:
                                set(context, intent);
                                break;
                            case ACTION_INTERVAL:
                                interval(context, intent);
                                break;
                            case ACTION_RULE:
                                rule(context, intent);
                                break;
                            case ACTION_TEMPLATE:
                                template(context, intent);
                                break;
                            case ACTION_DISCONNECT_ME:
                                disconnect(context, intent);
                                break;
                            case ACTION_ADGUARD:
                                adguard(context, intent);
                                break;
                            default:
                                throw new IllegalArgumentException(action);
                        }
                    } catch (Throwable ex) {
                        Log.e(ex);
                        EntityLog.log(context, "External " + Log.formatThrowable(ex));
                    }
                }
            });

            return START_NOT_STICKY;
        } finally {
            stopSelf(startId);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification getNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "service")
                        .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFERRED)
                        .setSmallIcon(R.drawable.baseline_compare_arrows_white_24)
                        .setContentTitle(getString(R.string.tile_synchronize))
                        .setAutoCancel(false)
                        .setShowWhen(false)
                        .setDefaults(0) // disable sound on pre Android 8
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                        .setLocalOnly(true)
                        .setOngoing(true);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    private static void poll(Context context, Intent intent) {
        String accountName = intent.getStringExtra("account");

        DB db = DB.getInstance(context);
        List<EntityAccount> accounts;
        if (accountName == null)
            accounts = db.account().getSynchronizingAccounts(null);
        else {
            EntityAccount account = db.account().getAccount(accountName);
            if (account == null)
                throw new IllegalArgumentException("Account not found name=" + accountName);
            accounts = new ArrayList<>();
            accounts.add(account);
        }

        for (EntityAccount account : accounts) {
            List<EntityFolder> folders = db.folder().getSynchronizingFolders(account.id);
            if (folders.size() > 0)
                Collections.sort(folders, folders.get(0).getComparator(context));
            for (EntityFolder folder : folders)
                EntityOperation.sync(context, folder.id, false);
        }

        ServiceSynchronize.eval(context, "external poll account=" + accountName);
    }

    private static void interval(Context context, Intent intent) {
        int minutes = intent.getIntExtra("minutes", 0);
        int[] values = context.getResources().getIntArray(R.array.pollIntervalValues);
        for (int value : values)
            if (value >= minutes) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putInt("poll_interval", value).apply();
                break;
            }
    }

    private static void set(Context context, Intent intent) {
        String accountName = intent.getStringExtra("account");
        boolean enabled = ACTION_ENABLE.equals(intent.getAction());

        DB db = DB.getInstance(context);
        if (accountName == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putBoolean("enabled", enabled).apply();
        } else {
            EntityAccount account = db.account().getAccount(accountName);
            if (account == null)
                throw new IllegalArgumentException("Account not found name=" + accountName);

            db.account().setAccountOnDemand(account.id, !enabled);
        }

        ServiceSynchronize.eval(context, "external account=" + accountName + " enabled=" + enabled);
    }

    private static void rule(Context context, Intent intent) throws IOException, JSONException, MessagingException {
        String accountName = intent.getStringExtra("account");
        String ruleName = intent.getStringExtra("rule");

        DB db = DB.getInstance(context);
        EntityAccount account = db.account().getAccount(accountName);
        if (account == null)
            throw new IllegalArgumentException("Account not found name=" + accountName);

        List<EntityRule> rules = db.rule().getRuleByName(account.id, ruleName);
        if (rules == null || rules.size() == 0)
            throw new IllegalArgumentException("Rule not found name=" + ruleName);
        if (rules.size() != 1)
            throw new IllegalArgumentException("Rule ambiguous name=" + ruleName);

        EntityRule rule = rules.get(0);
        List<Long> ids = db.message().getMessageIdsByFolder(rule.folder);
        if (ids == null || ids.size() == 0)
            return;

        // Check header conditions
        for (long mid : ids) {
            EntityMessage message = db.message().getMessage(mid);
            if (message == null || message.ui_hide)
                continue;
            rule.matches(context, message, null, null);
        }

        int applied = 0;
        for (long mid : ids)
            try {
                db.beginTransaction();

                EntityMessage message = db.message().getMessage(mid);
                if (message == null || message.ui_hide)
                    continue;

                EntityLog.log(context, "Executing rules message=" + message.id);
                applied = EntityRule.run(context, rules, message, false, null, null);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        EntityLog.log(context, "Executing rule=" + rule.name + " applied=" + applied);
    }

    private static void template(Context context, Intent intent) throws AddressException, IOException {
        String templateName = intent.getStringExtra("template");
        String identityName = intent.getStringExtra("identity");
        String toName = intent.getStringExtra("to");
        String ccName = intent.getStringExtra("cc");
        String subject = intent.getStringExtra("subject");

        DB db = DB.getInstance(context);
        List<EntityAnswer> answers = db.answer().getAnswerByName(templateName);
        List<EntityIdentity> identity = db.identity().getIdentityByDisplayName(identityName);
        Address[] to = InternetAddress.parse(toName);
        Address[] cc = TextUtils.isEmpty(ccName) ? null : InternetAddress.parse(ccName);

        if (answers == null || answers.size() == 0)
            throw new IllegalArgumentException("Unknown template: " + templateName);
        if (answers.size() != 1)
            throw new IllegalArgumentException("Ambiguous template: " + templateName);
        if (identity == null || identity.size() == 0)
            throw new IllegalArgumentException("Unknown identity: " + identityName);
        if (identity.size() != 1)
            throw new IllegalArgumentException("Ambiguous identity: " + identityName);
        if (to == null || to.length == 0)
            throw new IllegalArgumentException("No to recipients: " + toName);

        EntityFolder outbox = EntityFolder.getOutbox(context);

        Address[] from = new Address[]{
                new InternetAddress(identity.get(0).email, identity.get(0).name, StandardCharsets.UTF_8.name())};
        if (subject == null) // Allow empty string
            subject = answers.get(0).name;
        EntityAnswer.Data answerData = answers.get(0).getData(context, to);
        String body = answerData.getHtml();

        EntityMessage msg = new EntityMessage();
        msg.account = identity.get(0).account;
        msg.folder = outbox.id;
        msg.identity = identity.get(0).id;
        msg.msgid = EntityMessage.generateMessageId();
        msg.thread = msg.msgid;
        msg.from = from;
        msg.to = to;
        msg.cc = cc;
        msg.subject = subject;
        msg.received = new Date().getTime();
        msg.sender = MessageHelper.getSortKey(msg.from);
        Uri lookupUri = ContactInfo.getLookupUri(msg.from);
        msg.avatar = (lookupUri == null ? null : lookupUri.toString());
        msg.id = db.message().insertMessage(msg);

        File file = msg.getFile(context);
        Helper.writeText(file, body);
        String text = HtmlHelper.getFullText(context, body);
        msg.preview = HtmlHelper.getPreview(text);
        msg.language = HtmlHelper.getLanguage(context, msg.subject, text);
        db.message().setMessageContent(msg.id,
                true,
                msg.language,
                0,
                msg.preview,
                null);

        answerData.insertAttachments(context, msg.id);

        EntityOperation.queue(context, msg, EntityOperation.SEND);
        ServiceSend.start(context);
    }

    private static void disconnect(Context context, Intent intent) throws IOException, JSONException {
        DisconnectBlacklist.download(context);
    }

    private static void adguard(Context context, Intent intent) throws IOException, JSONException {
        Adguard.download(context);
    }
}
