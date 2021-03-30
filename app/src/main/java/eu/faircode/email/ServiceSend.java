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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.PowerManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.sun.mail.smtp.SMTPSendFailedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ServiceSend extends ServiceBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private TupleUnsent lastUnsent = null;
    private Network lastActive = null;
    private boolean lastSuitable = false;

    private PowerManager.WakeLock wlOutbox;
    private TwoStateOwner owner = new TwoStateOwner("send");
    private List<Long> handling = new ArrayList<>();

    private static ExecutorService executor = Helper.getBackgroundExecutor(1, "send");

    private static final int RETRY_MAX = 3;
    private static final int CONNECTIVITY_DELAY = 5000; // milliseconds

    static final int PI_SEND = 1;
    static final int PI_EXISTS = 2;

    @Override
    public void onCreate() {
        EntityLog.log(this, "Service send create");
        super.onCreate();
        startForeground(Helper.NOTIFICATION_SEND, getNotificationService().build());

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wlOutbox = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":send");

        // Observe unsent count
        DB db = DB.getInstance(this);
        db.operation().liveUnsent().observe(this, new Observer<TupleUnsent>() {
            @Override
            public void onChanged(TupleUnsent unsent) {
                if (unsent == null || !unsent.equals(lastUnsent)) {
                    lastUnsent = unsent;
                    EntityLog.log(ServiceSend.this, "Unsent=" + (unsent == null ? null : unsent.count));

                    try {
                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.notify(Helper.NOTIFICATION_SEND, getNotificationService().build());
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }

                    if (unsent == null || unsent.count == 0)
                        stopSelf();
                }
            }
        });

        // Observe send operations
        db.operation().liveSend().observe(owner, new Observer<List<EntityOperation>>() {
            @Override
            public void onChanged(List<EntityOperation> operations) {
                if (operations == null)
                    operations = new ArrayList<>();

                final List<EntityOperation> process = new ArrayList<>();

                List<Long> ops = new ArrayList<>();
                for (EntityOperation op : operations) {
                    if (!handling.contains(op.id))
                        process.add(op);
                    ops.add(op.id);
                }
                handling = ops;

                if (process.size() > 0) {
                    EntityLog.log(ServiceSend.this,
                            "Send process=" + TextUtils.join(",", process) +
                                    " handling=" + TextUtils.join(",", handling));

                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            processOperations(process);
                        }
                    });
                }
            }
        });

        lastSuitable = ConnectionHelper.getNetworkState(this).isSuitable();
        if (lastSuitable)
            owner.start();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        IntentFilter iif = new IntentFilter();
        iif.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        iif.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(connectionChangedReceiver, iif);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        EntityLog.log(this, "Service send destroy");

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

        unregisterReceiver(connectionChangedReceiver);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        getMainHandler().removeCallbacks(_checkConnectivity);

        owner.stop();
        handling.clear();

        stopForeground(true);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(Helper.NOTIFICATION_SEND);

        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (ConnectionHelper.PREF_NETWORK.contains(key))
            checkConnectivity();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground(Helper.NOTIFICATION_SEND, getNotificationService().build());

        Log.i("Send intent=" + intent);
        Log.logExtras(intent);

        if (intent == null)
            return START_STICKY;

        String action = intent.getAction();
        if (action == null)
            return START_STICKY;

        String[] parts = action.split(":");
        switch (parts[0]) {
            case "exists":
                onExists(intent);
                break;
        }

        return START_STICKY;
    }

    NotificationCompat.Builder getNotificationService() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean alert_once = prefs.getBoolean("alert_once", true);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "send")
                        .setSmallIcon(R.drawable.baseline_send_white_24)
                        .setContentTitle(getString(R.string.title_notification_sending))
                        .setContentIntent(getPendingIntent(this))
                        .setAutoCancel(false)
                        .setShowWhen(true)
                        .setOnlyAlertOnce(alert_once)
                        .setDefaults(0) // disable sound on pre Android 8
                        .setLocalOnly(true)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        if (lastUnsent != null && lastUnsent.count != null)
            builder.setContentText(getResources().getQuantityString(
                    R.plurals.title_notification_unsent, lastUnsent.count, lastUnsent.count));
        if (lastUnsent == null || lastUnsent.busy == null || lastUnsent.busy == 0)
            builder.setSubText(getString(R.string.title_notification_idle));
        if (!lastSuitable)
            builder.setSubText(getString(R.string.title_notification_waiting));

        return builder;
    }

    NotificationCompat.Builder getNotificationError(String recipient, Throwable ex, int tries_left) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, tries_left == 0 ? "error" : "warning")
                .setSmallIcon(R.drawable.baseline_warning_white_24)
                .setContentTitle(getString(R.string.title_notification_sending_failed, recipient))
                .setContentIntent(getPendingIntent(this))
                .setAutoCancel(tries_left != 0)
                .setShowWhen(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOnlyAlertOnce(false)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        if (tries_left == 0) {
            builder.setContentText(Log.formatThrowable(ex, false))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(Log.formatThrowable(ex, "\n", false)));
        } else
            builder.setContentText(getString(R.string.title_notification_sending_left, tries_left));
        return builder;
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, ActivityView.class);
        intent.setAction("outbox");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntentCompat.getActivity(
                context, ActivityView.REQUEST_OUTBOX, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            Log.i("Service send available=" + network);
            checkConnectivity();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities caps) {
            Log.i("Service send network=" + network + " caps=" + caps);
            checkConnectivity();
        }

        @Override
        public void onLost(@NonNull Network network) {
            Log.i("Service send lost=" + network);
            checkConnectivity();
        }
    };

    private BroadcastReceiver connectionChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Received intent=" + intent +
                    " " + TextUtils.join(" ", Log.getExtras(intent.getExtras())));
            checkConnectivity();
        }
    };

    private void checkConnectivity() {
        getMainHandler().postDelayed(_checkConnectivity, CONNECTIVITY_DELAY);
    }

    private Runnable _checkConnectivity = new Runnable() {
        @Override
        public void run() {
            Network active = ConnectionHelper.getActiveNetwork(ServiceSend.this);
            boolean restart = !Objects.equals(lastActive, active);
            if (restart) {
                lastActive = active;
                EntityLog.log(ServiceSend.this, "Service send active=" + active);

                if (lastSuitable) {
                    EntityLog.log(ServiceSend.this, "Service send restart");
                    lastSuitable = false;
                    owner.stop();
                    handling.clear();
                }
            }

            boolean suitable = ConnectionHelper.getNetworkState(ServiceSend.this).isSuitable();
            if (lastSuitable != suitable) {
                lastSuitable = suitable;
                EntityLog.log(ServiceSend.this, "Service send suitable=" + suitable);

                try {
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(Helper.NOTIFICATION_SEND, getNotificationService().build());
                } catch (Throwable ex) {
                    Log.w(ex);
                }

                if (suitable)
                    owner.start();
                else {
                    owner.stop();
                    handling.clear();
                }
            }
        }
    };

    private void processOperations(List<EntityOperation> ops) {
        try {
            wlOutbox.acquire();

            DB db = DB.getInstance(this);
            EntityFolder outbox = db.folder().getOutbox();
            try {
                db.folder().setFolderError(outbox.id, null);
                db.folder().setFolderSyncState(outbox.id, "syncing");

                EntityLog.log(this, "Send processing operations=" + ops.size());

                while (ops.size() > 0) {
                    if (!ConnectionHelper.getNetworkState(this).isSuitable())
                        break;

                    EntityOperation op = ops.get(0);

                    EntityMessage message = null;
                    if (op.message != null)
                        message = db.message().getMessage(op.message);

                    try {
                        EntityLog.log(this, "Send start op=" + op.id + "/" + op.name +
                                " msg=" + op.message +
                                " tries=" + op.tries +
                                " args=" + op.args);

                        db.operation().setOperationTries(op.id, ++op.tries);
                        db.operation().setOperationError(op.id, null);

                        if (message != null)
                            db.message().setMessageError(message.id, null);

                        db.operation().setOperationState(op.id, "executing");

                        Map<String, String> crumb = new HashMap<>();
                        crumb.put("name", op.name);
                        crumb.put("args", op.args);
                        crumb.put("folder", op.folder + ":outbox");
                        if (op.message != null)
                            crumb.put("message", Long.toString(op.message));
                        crumb.put("free", Integer.toString(Log.getFreeMemMb()));
                        Log.breadcrumb("operation", crumb);

                        switch (op.name) {
                            case EntityOperation.SYNC:
                                onSync(outbox);
                                break;

                            case EntityOperation.SEND:
                                if (message == null)
                                    throw new MessageRemovedException();
                                onSend(message);
                                break;

                            case EntityOperation.ANSWERED:
                                break;

                            default:
                                throw new IllegalArgumentException("Unknown operation=" + op.name);
                        }

                        db.operation().deleteOperation(op.id);
                        ops.remove(op);
                    } catch (Throwable ex) {
                        Log.e(outbox.name, ex);
                        EntityLog.log(this, "Send " + Log.formatThrowable(ex, false));

                        boolean unrecoverable = (op.tries >= RETRY_MAX ||
                                ex instanceof OutOfMemoryError ||
                                ex instanceof MessageRemovedException ||
                                ex instanceof FileNotFoundException ||
                                (ex instanceof AuthenticationFailedException && !ConnectionHelper.isIoError(ex)) ||
                                ex instanceof SendFailedException ||
                                ex instanceof IllegalArgumentException);

                        db.operation().setOperationError(op.id, Log.formatThrowable(ex));
                        if (message != null) {
                            db.message().setMessageError(message.id, Log.formatThrowable(ex));

                            try {
                                int tries_left = (unrecoverable ? 0 : RETRY_MAX - op.tries);
                                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                nm.notify("send:" + message.id, 1, getNotificationError(
                                        MessageHelper.formatAddressesShort(message.to), ex, tries_left).build());
                            } catch (Throwable ex1) {
                                Log.w(ex1);
                            }
                        }

                        if (unrecoverable) {
                            Log.w("Unrecoverable");
                            db.operation().deleteOperation(op.id);
                            ops.remove(op);
                        } else
                            throw ex;
                    } finally {
                        EntityLog.log(this, "Send end op=" + op.id + "/" + op.name);
                        db.operation().setOperationState(op.id, null);
                    }
                }

            } catch (Throwable ex) {
                Log.e(outbox.name, ex);
                db.folder().setFolderError(outbox.id, Log.formatThrowable(ex));
            } finally {
                db.folder().setFolderState(outbox.id, null);
                db.folder().setFolderSyncState(outbox.id, null);
            }
        } finally {
            wlOutbox.release();
        }
    }

    private void onSync(EntityFolder outbox) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            db.folder().setFolderError(outbox.id, null);

            // Requeue non executing operations
            for (long id : db.message().getMessageByFolder(outbox.id)) {
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    continue;

                EntityOperation op = db.operation().getOperation(message.id, EntityOperation.SEND);
                if (op != null) {
                    if ("executing".equals(op.state))
                        continue;
                    db.operation().deleteOperation(op.id);
                }

                db.message().setMessageError(message.id, null);
                nm.cancel("send:" + message.id, 1);

                if (message.ui_snoozed == null)
                    EntityOperation.queue(this, message, EntityOperation.SEND);
                else
                    EntityMessage.snooze(this, message.id, message.ui_snoozed);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        ServiceSend.start(this);
    }

    private void onSend(EntityMessage message) throws MessagingException, IOException {
        DB db = DB.getInstance(this);

        EntityOperation operation = db.operation().getOperation(message.id, EntityOperation.SEND);
        if (operation == null)
            throw new IllegalArgumentException("Send cancelled");

        // Mark attempt
        if (message.last_attempt == null) {
            message.last_attempt = new Date().getTime();
            db.message().setMessageLastAttempt(message.id, message.last_attempt);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean debug = prefs.getBoolean("debug", false);

        if (message.identity == null)
            throw new IllegalArgumentException("Send without identity");

        EntityIdentity ident = db.identity().getIdentity(message.identity);
        if (ident == null)
            throw new IllegalArgumentException("Identity not found");
        if (!ident.synchronize)
            throw new IllegalArgumentException("Identity is disabled");

        if (!message.content)
            throw new IllegalArgumentException("Message body missing");

        // Update message ID
        if (message.from != null && message.from.length > 0) {
            String from = ((InternetAddress) message.from[0]).getAddress();
            int at = (from == null ? -1 : from.indexOf('@'));
            if (at > 0 && at + 1 < from.length())
                message.msgid = EntityMessage.generateMessageId(from.substring(at + 1));
        }

        // Set sent time
        message.sent = new Date().getTime();
        db.message().setMessageSent(message.id, message.sent);

        // Create message
        Properties props = MessageHelper.getSessionProperties();
        // https://javaee.github.io/javamail/docs/api/javax/mail/internet/package-summary.html
        if (ident.unicode)
            props.put("mail.mime.allowutf8", "true");
        Session isession = Session.getInstance(props, null);
        MimeMessage imessage = MessageHelper.from(this, message, ident, isession, true);

        // Prepare sent message
        Long sid = null;
        EntityFolder sent = db.folder().getFolderByType(message.account, EntityFolder.SENT);
        if (sent != null) {
            Log.i(sent.name + " Preparing sent message");

            long id = message.id;

            imessage.saveChanges();
            MessageHelper helper = new MessageHelper(imessage, ServiceSend.this);

            if (message.uid != null) {
                Log.e("Outbox id=" + message.id + " uid=" + message.uid);
                message.uid = null;
            }

            MessageHelper.MessageParts parts = helper.getMessageParts();
            String body = parts.getHtml(this);
            Boolean plain = parts.isPlainOnly();
            if (plain != null && plain)
                body = body.replace("<div x-plain=\"true\">", "<div>");

            String text = HtmlHelper.getFullText(body);
            String language = HtmlHelper.getLanguage(this, message.subject, text);
            String preview = HtmlHelper.getPreview(text);

            try {
                db.beginTransaction();

                message.id = null;
                message.folder = sent.id;
                message.identity = null;
                message.from = helper.getFrom();
                message.cc = helper.getCc();
                message.bcc = helper.getBcc();
                message.reply = helper.getReply();
                message.subject = helper.getSubject(); // Subject encryption
                message.encrypt = parts.getEncryption();
                message.ui_encrypt = message.encrypt;
                message.received = new Date().getTime();
                message.seen = true;
                message.ui_seen = true;
                message.ui_hide = true;
                message.error = null;
                message.id = db.message().insertMessage(message);

                File file = EntityMessage.getFile(this, message.id);
                Helper.writeText(file, body);
                db.message().setMessageContent(message.id,
                        true,
                        language,
                        parts.isPlainOnly(),
                        preview,
                        parts.getWarnings(message.warning));

                EntityAttachment.copy(this, id, message.id);

                Long size = null;
                if (body != null)
                    size = (long) body.length();

                Long total = size;
                List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                for (EntityAttachment attachment : attachments)
                    if (attachment.size != null)
                        total = (total == null ? 0 : total) + attachment.size;

                db.message().setMessageSize(message.id, size, total);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            sid = message.id;
            message.id = id;
        }

        // Create transport
        long start, end;
        Long max_size = null;
        try (EmailService iservice = new EmailService(
                this, ident.getProtocol(), ident.realm, ident.encryption, ident.insecure, debug)) {
            iservice.setUseIp(ident.use_ip, ident.ehlo);
            iservice.setUnicode(ident.unicode);

            if (message.receipt_request != null && message.receipt_request) {
                int receipt_type = prefs.getInt("receipt_type", 2);
                if (receipt_type == 1 || receipt_type == 2) // Delivery receipt
                    iservice.setDsnNotify("SUCCESS,FAILURE,DELAY");
            }

            // Connect transport
            db.identity().setIdentityState(ident.id, "connecting");
            iservice.connect(ident);
            if (BuildConfig.DEBUG && false)
                throw new IOException("Test");
            db.identity().setIdentityState(ident.id, "connected");

            if (ident.max_size == null)
                max_size = iservice.getMaxSize();

            Address[] to = imessage.getAllRecipients();
            String via = "via " + ident.host + "/" + ident.user +
                    " to " + (to == null ? null : TextUtils.join(", ", to));

            // Send message
            EntityLog.log(this, "Sending " + via);
            start = new Date().getTime();
            iservice.getTransport().sendMessage(imessage, to);
            end = new Date().getTime();
            EntityLog.log(this, "Sent " + via + " elapse=" + (end - start) + " ms");
        } catch (MessagingException ex) {
            Log.e(ex);

            if (ex instanceof SMTPSendFailedException) {
                SMTPSendFailedException sem = (SMTPSendFailedException) ex;
                ex = new SMTPSendFailedException(
                        sem.getCommand(),
                        sem.getReturnCode(),
                        getString(R.string.title_service_auth, sem.getMessage()),
                        sem.getNextException(),
                        sem.getValidSentAddresses(),
                        sem.getValidUnsentAddresses(),
                        sem.getInvalidAddresses());
            }

            if (sid != null)
                db.message().deleteMessage(sid);

            db.identity().setIdentityError(ident.id, Log.formatThrowable(ex));

            throw ex;
        } finally {
            db.identity().setIdentityState(ident.id, null);
        }

        try {
            db.beginTransaction();

            // Delete from outbox
            db.message().deleteMessage(message.id);

            // Show in sent folder
            if (sid != null) {
                db.message().setMessageReceived(sid, start);
                db.message().setMessageSent(sid, end);
                db.message().setMessageUiHide(sid, false);
            }

            // Mark replied
            if (message.inreplyto != null) {
                List<EntityMessage> replieds = db.message().getMessagesByMsgId(message.account, message.inreplyto);
                for (EntityMessage replied : replieds)
                    EntityOperation.queue(this, replied, EntityOperation.ANSWERED, true);
            }

            // Mark forwarded
            if (message.wasforwardedfrom != null) {
                List<EntityMessage> forwardeds = db.message().getMessagesByMsgId(message.account, message.wasforwardedfrom);
                for (EntityMessage forwarded : forwardeds)
                    EntityOperation.queue(this, forwarded, EntityOperation.KEYWORD, "$Forwarded", true);
            }

            // Update identity
            if (max_size != null)
                db.identity().setIdentityMaxSize(ident.id, max_size);
            db.identity().setIdentityConnected(ident.id, new Date().getTime());
            db.identity().setIdentityError(ident.id, null);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        ServiceSynchronize.eval(this, "sent");

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel("send:" + message.id, 1);

        // Check sent message
        if (sid != null) {
            try {
                db.beginTransaction();

                // Message could have been deleted
                EntityMessage orphan = db.message().getMessage(sid);
                if (orphan != null)
                    EntityOperation.queue(this, orphan, EntityOperation.EXISTS);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            ServiceSynchronize.eval(this, "orphan");
        }
    }

    private void onExists(Intent intent) {
        String action = intent.getAction();
        long id = Long.parseLong(action.split(":")[1]);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DB db = DB.getInstance(ServiceSend.this);

                    try {
                        db.beginTransaction();

                        // Message could have been deleted in the meantime
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return;

                        EntityOperation.queue(ServiceSend.this, message, EntityOperation.EXISTS, true);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    static void boot(final Context context) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    EntityLog.log(context, "Boot send service");

                    DB db = DB.getInstance(context);

                    EntityFolder outbox = db.folder().getOutbox();
                    if (outbox != null) {
                        int operations = db.operation().getOperations(EntityOperation.SEND).size();
                        if (operations > 0)
                            start(context);
                        else {
                            db.folder().setFolderState(outbox.id, null);
                            db.folder().setFolderSyncState(outbox.id, null);
                        }
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    static void start(Context context) {
        try {
            ContextCompat.startForegroundService(context, new Intent(context, ServiceSend.class));
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static void schedule(Context context, long delay) {
        Intent intent = new Intent(context, ServiceSend.class);
        PendingIntent pi = PendingIntentCompat.getForegroundService(
                context, PI_SEND, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long trigger = System.currentTimeMillis() + delay;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        AlarmManagerCompat.setAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, trigger, pi); // exact
    }

    static void watchdog(Context context) {
        boot(context);
    }
}
