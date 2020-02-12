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
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class ServiceSend extends ServiceBase {
    private int lastUnsent = 0;
    private boolean lastSuitable = false;

    private PowerManager.WakeLock wlOutbox;
    private ExecutorService executor = Helper.getBackgroundExecutor(1, "send");

    private static final int IDENTITY_ERROR_AFTER = 30; // minutes

    @Override
    public void onCreate() {
        EntityLog.log(this, "Service send create");
        super.onCreate();
        startForeground(Helper.NOTIFICATION_SEND, getNotificationService().build());

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wlOutbox = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":send");

        // Observe unsent count
        DB db = DB.getInstance(this);
        db.operation().liveUnsent().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer unsent) {
                if (unsent != null && lastUnsent != unsent) {
                    lastUnsent = unsent;

                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(Helper.NOTIFICATION_SEND, getNotificationService().build());
                }
            }
        });

        // Observe send operations
        db.operation().liveOperations(null).observe(this, new Observer<List<TupleOperationEx>>() {
            private List<Long> handling = new ArrayList<>();

            @Override
            public void onChanged(final List<TupleOperationEx> operations) {
                boolean process = false;
                List<Long> ops = new ArrayList<>();
                for (EntityOperation op : operations) {
                    if (!handling.contains(op.id))
                        process = true;
                    if (!EntityOperation.SYNC.equals(op.name))
                        ops.add(op.id);
                }
                for (Long h : handling)
                    if (!ops.contains(h))
                        process = true;

                handling = ops;

                if (process) {
                    Log.i("OUTBOX operations=" + operations.size());

                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            processOperations();
                        }
                    });
                }
            }
        });

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        IntentFilter iif = new IntentFilter();
        iif.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        iif.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(connectionChangedReceiver, iif);
    }

    @Override
    public void onDestroy() {
        EntityLog.log(this, "Service send destroy");

        unregisterReceiver(connectionChangedReceiver);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        stopForeground(true);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(Helper.NOTIFICATION_SEND);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground(Helper.NOTIFICATION_SEND, getNotificationService().build());
        return START_STICKY;
    }

    NotificationCompat.Builder getNotificationService() {
        // Build pending intent
        Intent intent = new Intent(this, ActivityView.class);
        intent.setAction("outbox");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                this, ActivityView.REQUEST_OUTBOX, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "send")
                        .setSmallIcon(R.drawable.baseline_send_24)
                        .setContentTitle(getString(R.string.title_notification_sending))
                        .setContentIntent(pi)
                        .setAutoCancel(false)
                        .setShowWhen(true)
                        .setDefaults(0) // disable sound on pre Android 8
                        .setLocalOnly(true)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        if (lastUnsent > 0)
            builder.setContentText(getResources().getQuantityString(
                    R.plurals.title_notification_unsent, lastUnsent, lastUnsent));
        if (!lastSuitable)
            builder.setSubText(getString(R.string.title_notification_waiting));

        return builder;
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
        boolean suitable = ConnectionHelper.getNetworkState(ServiceSend.this).isSuitable();
        if (lastSuitable != suitable) {
            lastSuitable = suitable;
            EntityLog.log(ServiceSend.this, "Service send suitable=" + suitable);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(Helper.NOTIFICATION_SEND, getNotificationService().build());

            if (suitable)
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        processOperations();
                    }
                });
        }
    }

    private void processOperations() {
        try {
            wlOutbox.acquire();

            DB db = DB.getInstance(this);
            EntityFolder outbox = db.folder().getOutbox();
            try {
                db.folder().setFolderError(outbox.id, null);
                db.folder().setFolderSyncState(outbox.id, "syncing");

                List<TupleOperationEx> ops = db.operation().getOperations(outbox.id);
                Log.i(outbox.name + " pending operations=" + ops.size());
                for (EntityOperation op : ops) {
                    EntityMessage message = null;
                    try {
                        Log.i(outbox.name +
                                " start op=" + op.id + "/" + op.name +
                                " msg=" + op.message +
                                " args=" + op.args);

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
                                db.folder().setFolderError(outbox.id, null);
                                break;

                            case EntityOperation.SEND:
                                message = db.message().getMessage(op.message);
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
                    } catch (Throwable ex) {
                        Log.e(outbox.name, ex);
                        EntityLog.log(this, outbox.name + " " + Log.formatThrowable(ex, false));

                        db.operation().setOperationError(op.id, Log.formatThrowable(ex));
                        if (message != null)
                            db.message().setMessageError(message.id, Log.formatThrowable(ex));

                        if (ex instanceof OutOfMemoryError ||
                                ex instanceof MessageRemovedException ||
                                ex instanceof FileNotFoundException ||
                                ex instanceof SendFailedException ||
                                ex instanceof IllegalArgumentException) {
                            Log.w("Unrecoverable");
                            db.operation().deleteOperation(op.id);
                            continue;
                        } else
                            throw ex;
                    } finally {
                        Log.i(outbox.name + " end op=" + op.id + "/" + op.name);
                        db.operation().setOperationState(op.id, null);
                    }

                    if (!ConnectionHelper.getNetworkState(this).isSuitable())
                        break;
                }

                if (db.operation().getOperations(outbox.id).size() == 0)
                    stopSelf();

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

    private void onSend(EntityMessage message) throws MessagingException, IOException {
        DB db = DB.getInstance(this);

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

        if (!message.content)
            throw new IllegalArgumentException("Message body missing");

        // Create message
        Properties props = MessageHelper.getSessionProperties();
        Session isession = Session.getInstance(props, null);
        MimeMessage imessage = MessageHelper.from(this, message, ident, isession);

        // Prepare sent message
        Long sid = null;
        EntityFolder sent = db.folder().getFolderByType(message.account, EntityFolder.SENT);
        if (sent != null) {
            Log.i(sent.name + " Preparing sent message");

            long id = message.id;

            imessage.saveChanges();
            MessageHelper helper = new MessageHelper(imessage);

            if (message.uid != null) {
                Log.e("Outbox id=" + message.id + " uid=" + message.uid);
                message.uid = null;
            }

            MessageHelper.MessageParts parts = helper.getMessageParts(this);
            String body = parts.getHtml(this);

            try {
                db.beginTransaction();

                message.id = null;
                message.folder = sent.id;
                message.identity = null;
                message.from = helper.getFrom();
                message.bcc = helper.getBcc();
                message.reply = helper.getReply();
                message.received = new Date().getTime();
                message.seen = true;
                message.ui_seen = true;
                message.ui_hide = true;
                message.error = null;
                message.id = db.message().insertMessage(message);

                Helper.writeText(EntityMessage.getFile(this, message.id), body);
                db.message().setMessageContent(message.id,
                        true,
                        parts.isPlainOnly(),
                        HtmlHelper.getPreview(body),
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
        try (EmailService iservice = new EmailService(
                this, ident.getProtocol(), ident.realm, ident.insecure, debug)) {
            iservice.setUseIp(ident.use_ip);

            // Connect transport
            db.identity().setIdentityState(ident.id, "connecting");
            iservice.connect(ident);
            db.identity().setIdentityState(ident.id, "connected");

            Address[] to = imessage.getAllRecipients();
            String via = "via " + ident.host + "/" + ident.user +
                    " to " + TextUtils.join(", ", to);

            // Send message
            EntityLog.log(this, "Sending " + via);
            iservice.getTransport().sendMessage(imessage, to);
            long time = new Date().getTime();
            EntityLog.log(this, "Sent " + via);

            try {
                db.beginTransaction();

                // Delete from outbox
                db.message().deleteMessage(message.id);

                // Show in sent folder
                if (sid != null) {
                    db.message().setMessageSent(sid, time);
                    db.message().setMessageUiHide(sid, false);
                }

                if (message.inreplyto != null) {
                    List<EntityMessage> replieds = db.message().getMessagesByMsgId(message.account, message.inreplyto);
                    for (EntityMessage replied : replieds)
                        EntityOperation.queue(this, replied, EntityOperation.ANSWERED, true);
                }

                // Check sent message
                if (sid != null) {
                    // Check for sent orphans
                    EntityMessage orphan = db.message().getMessage(sid);
                    EntityOperation.queue(this, orphan, EntityOperation.EXISTS);
                }

                // Reset identity
                db.identity().setIdentityConnected(ident.id, new Date().getTime());
                db.identity().setIdentityError(ident.id, null);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            ServiceSynchronize.eval(ServiceSend.this, "sent");

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel("send:" + message.identity, 1);
        } catch (MessagingException ex) {
            Log.e(ex);

            if (sid != null)
                db.message().deleteMessage(sid);

            db.identity().setIdentityError(ident.id, Log.formatThrowable(ex));

            if (ex instanceof AuthenticationFailedException ||
                    ex instanceof SendFailedException) {
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify("send:" + message.identity, 1,
                        Core.getNotificationError(this, "error", ident.name, ex)
                                .build());
                throw ex;
            }

            EntityLog.log(this, ident.name + " last attempt: " + new Date(message.last_attempt));

            long now = new Date().getTime();
            long delayed = now - message.last_attempt;
            if (delayed > IDENTITY_ERROR_AFTER * 60 * 1000L) {
                Log.i("Reporting send error after=" + delayed);
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify("send:" + message.identity, 1,
                        Core.getNotificationError(this, "warning", ident.name, ex).build());
            }

            throw ex;
        } finally {
            db.identity().setIdentityState(ident.id, null);
        }
    }

    static void boot(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DB db = DB.getInstance(context);
                    EntityFolder outbox = db.folder().getOutbox();
                    if (outbox != null) {
                        int operations = db.operation().getOperations(outbox.id).size();
                        if (operations > 0)
                            start(context);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }, "send:boot");
        thread.setPriority(THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    static void start(Context context) {
        ContextCompat.startForegroundService(context,
                new Intent(context, ServiceSend.class));
    }

    static void watchdog(Context context) {
        boot(context);
    }
}
