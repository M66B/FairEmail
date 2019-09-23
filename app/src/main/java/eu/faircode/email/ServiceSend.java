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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.PowerManager;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class ServiceSend extends ServiceBase {
    private int lastUnsent = 0;
    private boolean lastSuitable = false;
    private TwoStateOwner cowner;

    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private static final int IDENTITY_ERROR_AFTER = 30; // minutes

    @Override
    public void onCreate() {
        EntityLog.log(this, "Service send create");
        super.onCreate();
        startForeground(Helper.NOTIFICATION_SEND, getNotificationService(null, null).build());

        cowner = new TwoStateOwner(ServiceSend.this, "send");
        final DB db = DB.getInstance(this);
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // Observe unsent count
        db.operation().liveUnsent().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer unsent) {
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(Helper.NOTIFICATION_SEND, getNotificationService(unsent, null).build());
            }
        });

        // Observe send operations
        db.operation().liveOperations(null).observe(cowner, new Observer<List<EntityOperation>>() {
            private List<Long> handling = new ArrayList<>();
            private PowerManager.WakeLock wlFolder = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":send");

            @Override
            public void onChanged(final List<EntityOperation> operations) {
                boolean process = false;
                List<Long> ops = new ArrayList<>();
                for (EntityOperation op : operations) {
                    if (!handling.contains(op.id))
                        process = true;
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
                            try {
                                wlFolder.acquire();

                                EntityFolder outbox = db.folder().getOutbox();
                                try {
                                    db.folder().setFolderError(outbox.id, null);
                                    db.folder().setFolderSyncState(outbox.id, "syncing");

                                    List<EntityOperation> ops = db.operation().getOperations(outbox.id);
                                    Log.i(outbox.name + " pending operations=" + ops.size());
                                    for (EntityOperation op : ops) {
                                        EntityMessage message = null;
                                        try {
                                            Log.i(outbox.name +
                                                    " start op=" + op.id + "/" + op.name +
                                                    " msg=" + op.message +
                                                    " args=" + op.args);

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
                                            EntityLog.log(
                                                    ServiceSend.this,
                                                    outbox.name + " " + Helper.formatThrowable(ex, false));

                                            db.operation().setOperationError(op.id, Helper.formatThrowable(ex));
                                            if (message != null)
                                                db.message().setMessageError(message.id, Helper.formatThrowable(ex));

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
                                        }

                                        if (!ConnectionHelper.getNetworkState(ServiceSend.this).isSuitable())
                                            break;
                                    }

                                    if (db.operation().getOperations(outbox.id).size() == 0)
                                        stopSelf();

                                } catch (Throwable ex) {
                                    Log.e(outbox.name, ex);
                                    db.folder().setFolderError(outbox.id, Helper.formatThrowable(ex));
                                } finally {
                                    db.folder().setFolderState(outbox.id, null);
                                    db.folder().setFolderSyncState(outbox.id, null);
                                }

                            } finally {
                                wlFolder.release();
                            }
                        }
                    });
                }
            }
        });

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);
    }

    @Override
    public void onDestroy() {
        EntityLog.log(this, "Service send destroy");

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
        startForeground(Helper.NOTIFICATION_SEND, getNotificationService(null, null).build());
        return START_STICKY;
    }

    NotificationCompat.Builder getNotificationService(Integer unsent, Boolean suitable) {
        if (unsent != null)
            lastUnsent = unsent;
        if (suitable != null)
            lastSuitable = suitable;

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
            check();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities caps) {
            Log.i("Service send network=" + network + " caps=" + caps);
            check();
        }

        private void check() {
            boolean suitable = ConnectionHelper.getNetworkState(ServiceSend.this).isSuitable();
            Log.i("OUTBOX suitable=" + suitable);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(Helper.NOTIFICATION_SEND, getNotificationService(null, suitable).build());

            if (suitable)
                cowner.start();
            else
                cowner.stop();
        }
    };

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

        // Add reply to
        if (ident.replyto != null)
            imessage.setReplyTo(InternetAddress.parse(ident.replyto));

        // Add bcc
        if (ident.bcc != null) {
            List<Address> bcc = new ArrayList<>();
            Address[] existing = imessage.getRecipients(Message.RecipientType.BCC);
            if (existing != null)
                bcc.addAll(Arrays.asList(existing));
            bcc.addAll(Arrays.asList(InternetAddress.parse(ident.bcc)));
            imessage.setRecipients(Message.RecipientType.BCC, bcc.toArray(new Address[0]));
        }

        if (message.receipt_request == null || !message.receipt_request) {
            // defacto standard
            if (ident.delivery_receipt)
                imessage.addHeader("Return-Receipt-To", ident.replyto == null ? ident.email : ident.replyto);

            // https://tools.ietf.org/html/rfc3798
            if (ident.read_receipt)
                imessage.addHeader("Disposition-Notification-To", ident.replyto == null ? ident.email : ident.replyto);
        }

        // Prepare sent message
        Long sid = null;
        EntityFolder sent = db.folder().getFolderByType(message.account, EntityFolder.SENT);
        if (sent != null) {
            Log.i(sent.name + " Preparing sent message");

            long id = message.id;

            MessageHelper helper = new MessageHelper(imessage);

            if (message.uid != null) {
                Log.e("Outbox id=" + message.id + " uid=" + message.uid);
                message.uid = null;
            }

            message.id = null;
            message.folder = sent.id;
            message.identity = null;
            message.receipt_request = helper.getReceiptRequested();
            message.from = helper.getFrom();
            message.bcc = helper.getBcc();
            message.reply = helper.getReply();
            message.received = new Date().getTime();
            message.seen = true;
            message.ui_seen = true;
            message.ui_hide = Long.MAX_VALUE;
            message.error = null;
            message.id = db.message().insertMessage(message);

            message.getFile(this).createNewFile();
            EntityAttachment.copy(this, id, message.id);

            sid = message.id;
            message.id = id;
        }

        // Create transport
        try (MailService iservice = new MailService(
                this, ident.getProtocol(), ident.realm, ident.insecure, debug)) {
            iservice.setUseIp(ident.use_ip);

            // Connect transport
            db.identity().setIdentityState(ident.id, "connecting");
            iservice.connect(ident);
            db.identity().setIdentityState(ident.id, "connected");

            // Send message
            Address[] to = imessage.getAllRecipients();
            iservice.getTransport().sendMessage(imessage, to);
            long time = new Date().getTime();
            EntityLog.log(this,
                    "Sent via " + ident.host + "/" + ident.user +
                            " to " + TextUtils.join(", ", to));

            try {
                db.beginTransaction();

                db.message().deleteMessage(message.id);

                if (sid != null) {
                    MessageHelper helper = new MessageHelper(imessage);
                    MessageHelper.MessageParts parts = helper.getMessageParts();
                    String body = parts.getHtml(this);
                    Helper.writeText(EntityMessage.getFile(this, sid), body);
                    db.message().setMessageContent(message.id,
                            true,
                            parts.isPlainOnly(),
                            HtmlHelper.getPreview(body),
                            parts.getWarnings(message.warning));

                    db.message().setMessageSent(sid, time);
                    db.message().setMessageUiHide(sid, 0L);

                    // Check for sent orphans
                    EntityMessage orphan = db.message().getMessage(sid);
                    EntityOperation.queue(this, orphan, EntityOperation.EXISTS);
                }

                if (message.inreplyto != null) {
                    List<EntityMessage> replieds = db.message().getMessageByMsgId(message.account, message.inreplyto);
                    for (EntityMessage replied : replieds)
                        EntityOperation.queue(this, replied, EntityOperation.ANSWERED, true);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            // Reset identity
            db.identity().setIdentityConnected(ident.id, new Date().getTime());
            db.identity().setIdentityError(ident.id, null);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel("send:" + message.identity, 1);
        } catch (MessagingException ex) {
            Log.e(ex);

            if (sid != null)
                db.message().deleteMessage(sid);

            db.identity().setIdentityError(ident.id, Helper.formatThrowable(ex));

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
                    if (outbox != null && db.operation().getOperations(outbox.id).size() > 0)
                        start(context);
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
