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
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.PowerManager;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;

public class ServiceSend extends LifecycleService {
    private int lastUnsent = 0;

    private static boolean booted = false;

    private static final int IDENTITY_ERROR_AFTER = 30; // minutes

    @Override
    public void onCreate() {
        Log.i("Service send create");
        super.onCreate();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        DB db = DB.getInstance(this);

        db.operation().liveUnsent().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer unsent) {
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(Helper.NOTIFICATION_SEND, getNotificationService(unsent).build());
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.i("Service send destroy");

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        stopForeground(true);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(Helper.NOTIFICATION_SEND);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(Helper.NOTIFICATION_SEND, getNotificationService(null).build());

        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    NotificationCompat.Builder getNotificationService(Integer unsent) {
        if (unsent != null)
            lastUnsent = unsent;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "service");

        builder
                .setSmallIcon(R.drawable.baseline_send_24)
                .setContentTitle(getString(R.string.title_notification_sending))
                .setAutoCancel(false)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        if (lastUnsent > 0)
            builder.setContentText(getResources().getQuantityString(
                    R.plurals.title_notification_unsent, lastUnsent, lastUnsent));

        return builder;
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        private Thread thread = null;

        @Override
        public void onAvailable(Network network) {
            Log.i("Service send available=" + network);
            if (Helper.suitableNetwork(ServiceSend.this, false))
                run();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities caps) {
            Log.i("Service send caps=" + caps);
            if (Helper.suitableNetwork(ServiceSend.this, false))
                run();
        }

        private void run() {
            if (thread != null && thread.isAlive())
                return;

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wl = pm.newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":send");
                    try {
                        wl.acquire();

                        DB db = DB.getInstance(ServiceSend.this);
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

                                    switch (op.name) {
                                        case EntityOperation.SYNC:
                                            db.folder().setFolderError(outbox.id, null);
                                            break;

                                        case EntityOperation.SEND:
                                            message = db.message().getMessage(op.message);
                                            if (message == null)
                                                throw new MessageRemovedException();
                                            send(message);
                                            break;

                                        default:
                                            throw new IllegalArgumentException("Unknown operation=" + op.name);
                                    }

                                    db.operation().deleteOperation(op.id);
                                } catch (Throwable ex) {
                                    Log.e(outbox.name, ex);
                                    Core.reportError(ServiceSend.this, null, outbox, ex);

                                    db.operation().setOperationError(op.id, Helper.formatThrowable(ex));
                                    if (message != null)
                                        db.message().setMessageError(message.id, Helper.formatThrowable(ex));

                                    if (ex instanceof MessageRemovedException ||
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

                                if (!Helper.suitableNetwork(ServiceSend.this, false))
                                    break;
                            }

                            if (db.operation().getOperations(outbox.id).size() == 0)
                                stopSelf();
                        } catch (Throwable ex) {
                            Log.e(outbox.name, ex);
                            db.folder().setFolderError(outbox.id, Helper.formatThrowable(ex, true));
                        } finally {
                            db.folder().setFolderState(outbox.id, null);
                            db.folder().setFolderSyncState(outbox.id, null);
                        }
                    } finally {
                        wl.release();
                    }
                }
            });
            thread.start();
        }
    };

    private void send(EntityMessage message) throws MessagingException, IOException {
        DB db = DB.getInstance(this);

        // Mark attempt
        if (message.last_attempt == null) {
            message.last_attempt = new Date().getTime();
            db.message().setMessageLastAttempt(message.id, message.last_attempt);
        }

        EntityIdentity ident = db.identity().getIdentity(message.identity);
        String protocol = ident.getProtocol();

        // Get properties
        Properties props = MessageHelper.getSessionProperties(ident.auth_type, ident.realm, ident.insecure);

        String haddr;
        if (ident.use_ip) {
            InetAddress addr = InetAddress.getByName(ident.host);
            if (addr instanceof Inet4Address)
                haddr = "[" + Inet4Address.getLocalHost().getHostAddress() + "]";
            else
                haddr = "[IPv6:" + Inet6Address.getLocalHost().getHostAddress() + "]";
        } else
            haddr = ident.host;

        EntityLog.log(this, "Send localhost=" + haddr);
        props.put("mail." + protocol + ".localhost", haddr);

        // Create session
        final Session isession = Session.getInstance(props, null);
        isession.setDebug(true);

        // Create message
        MimeMessage imessage = MessageHelper.from(this, message, isession, ident.plain_only);

        // Add reply to
        if (ident.replyto != null)
            imessage.setReplyTo(new Address[]{new InternetAddress(ident.replyto)});

        // Add bcc
        if (ident.bcc != null) {
            List<Address> bcc = new ArrayList<>();
            Address[] existing = imessage.getRecipients(Message.RecipientType.BCC);
            if (existing != null)
                bcc.addAll(Arrays.asList(existing));
            bcc.add(new InternetAddress(ident.bcc));
            imessage.setRecipients(Message.RecipientType.BCC, bcc.toArray(new Address[0]));
        }

        // defacto standard
        if (ident.delivery_receipt)
            imessage.addHeader("Return-Receipt-To", ident.replyto == null ? ident.email : ident.replyto);

        // https://tools.ietf.org/html/rfc3798
        if (ident.read_receipt)
            imessage.addHeader("Disposition-Notification-To", ident.replyto == null ? ident.email : ident.replyto);

        // Create transport
        // TODO: cache transport?
        try (Transport itransport = isession.getTransport(protocol)) {
            // Connect transport
            db.identity().setIdentityState(ident.id, "connecting");
            try {
                itransport.connect(ident.host, ident.port, ident.user, ident.password);
            } catch (AuthenticationFailedException ex) {
                if (ident.auth_type == Helper.AUTH_TYPE_GMAIL) {
                    EntityAccount account = db.account().getAccount(ident.account);
                    ident.password = Helper.refreshToken(this, "com.google", ident.user, account.password);
                    DB.getInstance(this).identity().setIdentityPassword(ident.id, ident.password);
                    itransport.connect(ident.host, ident.port, ident.user, ident.password);
                } else
                    throw ex;
            }

            db.identity().setIdentityState(ident.id, "connected");

            // Send message
            Address[] to = imessage.getAllRecipients();
            itransport.sendMessage(imessage, to);
            EntityLog.log(this, "Sent via " + ident.host + "/" + ident.user +
                    " to " + TextUtils.join(", ", to));

            // Append replied/forwarded text
            StringBuilder sb = new StringBuilder();
            sb.append(Helper.readText(message.getFile(this)));
            File refFile = message.getRefFile(this);
            if (refFile.exists())
                sb.append(Helper.readText(refFile));
            Helper.writeText(message.getFile(this), sb.toString());

            try {
                db.beginTransaction();

                db.message().setMessageSent(message.id, imessage.getSentDate().getTime());
                db.message().setMessageSeen(message.id, true);
                db.message().setMessageUiSeen(message.id, true);
                db.message().setMessageError(message.id, null);

                EntityFolder sent = db.folder().getFolderByType(message.account, EntityFolder.SENT);
                if (ident.store_sent && sent != null) {
                    db.message().setMessageFolder(message.id, sent.id);
                    message.folder = sent.id;
                    EntityOperation.queue(this, db, message, EntityOperation.ADD);
                } else
                    db.message().setMessageUiHide(message.id, true);

                if (message.inreplyto != null) {
                    List<EntityMessage> replieds = db.message().getMessageByMsgId(message.account, message.inreplyto);
                    for (EntityMessage replied : replieds)
                        EntityOperation.queue(this, db, replied, EntityOperation.ANSWERED, true);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            if (refFile.exists())
                refFile.delete();

            db.identity().setIdentityConnected(ident.id, new Date().getTime());
            db.identity().setIdentityError(ident.id, null);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel("send", message.identity.intValue());

            if (message.to != null)
                for (Address recipient : message.to) {
                    String email = ((InternetAddress) recipient).getAddress();
                    String name = ((InternetAddress) recipient).getPersonal();
                    List<EntityContact> contacts = db.contact().getContacts(EntityContact.TYPE_TO, email);
                    if (contacts.size() == 0) {
                        EntityContact contact = new EntityContact();
                        contact.type = EntityContact.TYPE_TO;
                        contact.email = email;
                        contact.name = name;
                        db.contact().insertContact(contact);
                        Log.i("Inserted recipient contact=" + contact);
                    } else {
                        EntityContact contact = contacts.get(0);
                        if (name != null && !name.equals(contact.name)) {
                            contact.name = name;
                            db.contact().updateContact(contact);
                            Log.i("Updated recipient contact=" + contact);
                        }
                    }
                }
        } catch (MessagingException ex) {
            if (ex instanceof SendFailedException) {
                SendFailedException sfe = (SendFailedException) ex;

                StringBuilder sb = new StringBuilder();

                sb.append(sfe.getMessage());

                sb.append(' ').append(getString(R.string.title_address_sent));
                sb.append(' ').append(MessageHelper.formatAddresses(sfe.getValidSentAddresses()));

                sb.append(' ').append(getString(R.string.title_address_unsent));
                sb.append(' ').append(MessageHelper.formatAddresses(sfe.getValidUnsentAddresses()));

                sb.append(' ').append(getString(R.string.title_address_invalid));
                sb.append(' ').append(MessageHelper.formatAddresses(sfe.getInvalidAddresses()));

                ex = new SendFailedException(
                        sb.toString(),
                        sfe.getNextException(),
                        sfe.getValidSentAddresses(),
                        sfe.getValidUnsentAddresses(),
                        sfe.getInvalidAddresses());
            }

            db.identity().setIdentityError(ident.id, Helper.formatThrowable(ex));

            EntityLog.log(this, ident.name + " last attempt: " + new Date(message.last_attempt));

            long now = new Date().getTime();
            long delayed = now - message.last_attempt;
            if (delayed > IDENTITY_ERROR_AFTER * 60 * 1000L || ex instanceof SendFailedException) {
                Log.i("Reporting send error after=" + delayed);
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify("send", message.identity.intValue(),
                        Core.getNotificationError(this, ident.name, ex).build());
            }

            throw ex;
        } finally {
            db.identity().setIdentityState(ident.id, null);
        }
    }

    static void boot(final Context context) {
        if (!booted) {
            booted = true;

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
            });
            thread.start();
        }
    }

    static void start(Context context) {
        ContextCompat.startForegroundService(context,
                new Intent(context, ServiceSend.class));
    }
}
