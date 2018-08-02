package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.UIDFolder;
import javax.mail.event.ConnectionAdapter;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.FolderAdapter;
import javax.mail.event.FolderEvent;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;

public class ServiceSynchronize extends LifecycleService {
    private ServiceState state = new ServiceState();
    private ExecutorService executor = Executors.newCachedThreadPool();

    private static final int NOTIFICATION_SYNCHRONIZE = 1;

    private static final long NOOP_INTERVAL = 9 * 60 * 1000L; // ms
    private static final int FETCH_BATCH_SIZE = 10;

    static final String ACTION_PROCESS_OPERATIONS = BuildConfig.APPLICATION_ID + ".PROCESS_OPERATIONS.";

    private class ServiceState {
        boolean running = false;
        List<Thread> threads = new ArrayList<>(); // accounts
    }

    public ServiceSynchronize() {
        System.setProperty("mail.mime.ignoreunknownencoding", "true");
    }

    @Override
    public void onCreate() {
        Log.i(Helper.TAG, "Service create");
        super.onCreate();
        startForeground(NOTIFICATION_SYNCHRONIZE, getNotification(0, -1).build());

        // Listen for network changes
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        DB.getInstance(this).account().liveStats().observe(this, new Observer<TupleAccountStats>() {
            @Override
            public void onChanged(@Nullable TupleAccountStats stats) {
                if (stats != null) {
                    NotificationManager nm = getSystemService(NotificationManager.class);
                    nm.notify(NOTIFICATION_SYNCHRONIZE, getNotification(stats.accounts, stats.operations).build());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.i(Helper.TAG, "Service destroy");

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        networkCallback.onLost(cm.getActiveNetwork());

        stopForeground(true);

        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.cancel(NOTIFICATION_SYNCHRONIZE);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Helper.TAG, "Service start");
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    private Notification.Builder getNotification(int acounts, int operations) {
        // Build pending intent
        Intent intent = new Intent(this, ActivityView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                this, ActivityView.REQUEST_VIEW, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this, "service");
        else
            builder = new Notification.Builder(this);

        builder
                .setSmallIcon(R.drawable.baseline_mail_outline_24)
                .setContentTitle(getString(R.string.title_synchronizing, acounts))
                .setContentText(getString(R.string.title_operations, operations))
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_SECRET);

        if (operations >= 0)
            builder.setContentText(getString(R.string.title_operations, operations));

        return builder;
    }

    private Notification.Builder getNotification(String action, Throwable ex) {
        // Build pending intent
        Intent intent = new Intent(this, ActivityView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                this, ActivityView.REQUEST_VIEW, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this, "error");
        else
            builder = new Notification.Builder(this);

        builder
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(getString(R.string.title_failed, action))
                .setContentText(Helper.formatThrowable(ex))
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setShowWhen(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_ERROR)
                .setVisibility(Notification.VISIBILITY_SECRET);

        return builder;
    }

    private void monitorAccount(final EntityAccount account) {
        final NotificationManager nm = getSystemService(NotificationManager.class);
        Log.i(Helper.TAG, account.name + " start ");

        while (state.running) {
            IMAPStore istore = null;
            try {
                Properties props = MessageHelper.getSessionProperties();
                props.put("mail.imaps.peek", "true");
                //props.put("mail.imaps.minidletime", "5000");
                Session isession = Session.getDefaultInstance(props, null);
                // isession.setDebug(true);
                // adb -t 1 logcat | grep "eu.faircode.email\|System.out"

                istore = (IMAPStore) isession.getStore("imaps");
                final IMAPStore fstore = istore;

                // Listen for events
                istore.addStoreListener(new StoreListener() {
                    @Override
                    public void notification(StoreEvent e) {
                        Log.i(Helper.TAG, account.name + " event: " + e.getMessage());

                        // Check connection
                        synchronized (state) {
                            state.notifyAll();
                        }
                    }
                });
                istore.addFolderListener(new FolderAdapter() {
                    @Override
                    public void folderCreated(FolderEvent e) {
                        // TODO: folder created
                    }

                    @Override
                    public void folderRenamed(FolderEvent e) {
                        // TODO: folder renamed
                    }

                    @Override
                    public void folderDeleted(FolderEvent e) {
                        // TODO: folder deleted
                    }
                });

                // Listen for connection changes
                istore.addConnectionListener(new ConnectionAdapter() {
                    List<Thread> folderThreads = new ArrayList<>();

                    @Override
                    public void opened(ConnectionEvent e) {
                        Log.i(Helper.TAG, account.name + " opened");
                        try {
                            synchronizeFolders(account, fstore);

                            DB db = DB.getInstance(ServiceSynchronize.this);
                            for (final EntityFolder folder : db.folder().getFolders(account.id, true)) {
                                Log.i(Helper.TAG, account.name + " sync folder " + folder.name);
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            monitorFolder(folder, fstore);
                                        } catch (Throwable ex) {
                                            Log.e(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                            nm.notify("error", folder.id.intValue(), getNotification(folder.name, ex).build());

                                            // Cascade up
                                            try {
                                                fstore.close();
                                            } catch (MessagingException e1) {
                                                Log.w(Helper.TAG, account.name + " " + e1 + "\n" + Log.getStackTraceString(e1));
                                            }
                                        }
                                    }
                                }, "sync.folder." + folder.id);
                                folderThreads.add(thread);
                                thread.start();
                            }
                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                            nm.notify("error", account.id.intValue(), getNotification(account.name, ex).build());

                            // Cascade up
                            try {
                                fstore.close();
                            } catch (MessagingException e1) {
                                Log.w(Helper.TAG, account.name + " " + e1 + "\n" + Log.getStackTraceString(e1));
                            }
                        }
                    }

                    @Override
                    public void disconnected(ConnectionEvent e) {
                        Log.e(Helper.TAG, account.name + " disconnected");

                        // Check connection
                        synchronized (state) {
                            state.notifyAll();
                        }
                    }

                    @Override
                    public void closed(ConnectionEvent e) {
                        Log.e(Helper.TAG, account.name + " closed");

                        // Check connection
                        synchronized (state) {
                            state.notifyAll();
                        }
                    }
                });

                // Initiate connection
                Log.i(Helper.TAG, account.name + " connect");
                istore.connect(account.host, account.port, account.user, account.password);

                // Keep alive
                boolean connected = false;
                do {
                    try {
                        synchronized (state) {
                            state.wait();
                        }
                    } catch (InterruptedException ex) {
                        Log.w(Helper.TAG, account.name + " " + ex.toString());
                    }
                    if (state.running) {
                        Log.i(Helper.TAG, account.name + " NOOP");
                        connected = istore.isConnected();
                    }
                } while (state.running && connected);

                if (state.running)
                    Log.w(Helper.TAG, account.name + " not connected anymore");
                else
                    Log.i(Helper.TAG, account.name + " not running anymore");

            } catch (Throwable ex) {
                Log.w(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
            } finally {
                if (istore != null) {
                    try {
                        istore.close();
                    } catch (MessagingException ex) {
                        Log.w(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    }
                }
            }

            if (state.running) {
                try {
                    Thread.sleep(10 * 1000L); // TODO: logarithmic back off
                } catch (InterruptedException ex) {
                    Log.w(Helper.TAG, account.name + " " + ex.toString());
                }
            }
        }

        Log.i(Helper.TAG, account.name + " stopped");
    }

    private void monitorFolder(final EntityFolder folder, final IMAPStore istore) throws MessagingException, JSONException {
        final NotificationManager nm = getSystemService(NotificationManager.class);

        IMAPFolder ifolder = null;
        try {
            Log.i(Helper.TAG, folder.name + " start");

            ifolder = (IMAPFolder) istore.getFolder(folder.name);
            final IMAPFolder ffolder = ifolder;
            ifolder.open(Folder.READ_WRITE);

            // Listen for new and deleted messages
            ifolder.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(MessageCountEvent e) {
                    try {
                        Log.i(Helper.TAG, folder.name + " messages added");
                        for (Message imessage : e.getMessages())
                            synchronizeMessage(folder, ffolder, (IMAPMessage) imessage);
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                        nm.notify("error", folder.id.intValue(), getNotification(folder.name, ex).build());

                        // Cascade up
                        try {
                            istore.close();
                        } catch (MessagingException e1) {
                            Log.w(Helper.TAG, folder.name + " " + e1 + "\n" + Log.getStackTraceString(e1));
                        }
                    }
                }

                @Override
                public void messagesRemoved(MessageCountEvent e) {
                    try {
                        Log.i(Helper.TAG, folder.name + " messages removed");
                        for (Message imessage : e.getMessages()) {
                            long uid = ffolder.getUID(imessage);
                            DB db = DB.getInstance(ServiceSynchronize.this);
                            db.message().deleteMessage(folder.id, uid);
                            Log.i(Helper.TAG, "Deleted uid=" + uid);
                        }
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                        nm.notify("error", folder.id.intValue(), getNotification(folder.name, ex).build());

                        // Cascade up
                        try {
                            istore.close();
                        } catch (MessagingException e1) {
                            Log.w(Helper.TAG, folder.name + " " + e1 + "\n" + Log.getStackTraceString(e1));
                        }
                    }
                }
            });

            // Fetch e-mail
            synchronizeMessages(folder, ifolder);

            // Flags (like "seen") at the remote could be changed while synchronizing

            // Listen for changed messages
            ifolder.addMessageChangedListener(new MessageChangedListener() {
                @Override
                public void messageChanged(MessageChangedEvent e) {
                    try {
                        Log.i(Helper.TAG, folder.name + " message changed");
                        synchronizeMessage(folder, ffolder, (IMAPMessage) e.getMessage());
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                        nm.notify("error", folder.id.intValue(), getNotification(folder.name, ex).build());

                        // Cascade up
                        try {
                            istore.close();
                        } catch (MessagingException e1) {
                            Log.w(Helper.TAG, folder.name + " " + e1 + "\n" + Log.getStackTraceString(e1));
                        }
                    }
                }
            });

            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(Helper.TAG, folder.name + " submit process id=" + folder.id);
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                synchronized (folder) {
                                    processOperations(folder, istore, ffolder);
                                }
                            } catch (Throwable ex) {
                                Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                nm.notify("error", folder.id.intValue(), getNotification(folder.name, ex).build());

                                // Cascade up
                                try {
                                    istore.close();
                                } catch (MessagingException e1) {
                                    Log.w(Helper.TAG, folder.name + " " + e1 + "\n" + Log.getStackTraceString(e1));
                                }
                            }
                        }
                    });
                }
            };

            // Listen for process operations requests
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ServiceSynchronize.this);
            lbm.registerReceiver(receiver, new IntentFilter(ACTION_PROCESS_OPERATIONS + folder.id));
            Log.i(Helper.TAG, folder.name + " listen process id=" + folder.id);
            try {
                lbm.sendBroadcast(new Intent(ACTION_PROCESS_OPERATIONS + folder.id));

                // Keep alive
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean open;
                            do {
                                try {
                                    Thread.sleep(NOOP_INTERVAL);
                                } catch (InterruptedException ex) {
                                    Log.w(Helper.TAG, folder.name + " " + ex.toString());
                                }
                                open = ffolder.isOpen();
                                if (open)
                                    noop(folder, ffolder);
                            } while (open);
                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                            nm.notify("error", folder.id.intValue(), getNotification(folder.name, ex).build());

                            // Cascade up
                            try {
                                istore.close();
                            } catch (MessagingException e1) {
                                Log.w(Helper.TAG, folder.name + " " + e1 + "\n" + Log.getStackTraceString(e1));
                            }
                        }
                    }
                }, "sync.noop." + folder.id);
                thread.start();

                // Idle
                while (state.running) {
                    Log.i(Helper.TAG, folder.name + " start idle");
                    ifolder.idle(false);
                    Log.i(Helper.TAG, folder.name + " end idle");
                }
            } finally {
                lbm.unregisterReceiver(receiver);
                Log.i(Helper.TAG, folder.name + " unlisten process id=" + folder.id);
            }
        } finally {
            if (ifolder != null && ifolder.isOpen()) {
                try {
                    ifolder.close(false);
                } catch (MessagingException ex) {
                    Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                }
            }
            Log.i(Helper.TAG, folder.name + " stop");
        }
    }

    private void processOperations(EntityFolder folder, IMAPStore istore, IMAPFolder ifolder) throws MessagingException, JSONException {
        try {
            Log.i(Helper.TAG, folder.name + " start process");

            DB db = DB.getInstance(ServiceSynchronize.this);
            DaoOperation operation = db.operation();
            DaoMessage message = db.message();
            for (TupleOperationEx op : operation.getOperations(folder.id)) {
                Log.i(Helper.TAG, folder.name +
                        " Process op=" + op.id + "/" + op.name +
                        " args=" + op.args +
                        " msg=" + op.message);

                JSONArray jargs = new JSONArray(op.args);

                if (EntityOperation.SEEN.equals(op.name)) {
                    // Mark message (un)seen
                    try {
                        Message imessage = ifolder.getMessageByUID(op.uid);
                        if (imessage != null)
                            imessage.setFlag(Flags.Flag.SEEN, jargs.getBoolean(0));
                        else
                            Log.w(Helper.TAG, "Remote message not found uid=" + op.uid);
                    } catch (MessagingException ex) {
                        // Countermeasure
                        Log.i(Helper.TAG, folder.name + " countermeasure " + op.id + "/" + op.name);
                        EntityMessage msg = message.getMessage(op.message);
                        msg.ui_seen = msg.seen;
                        message.updateMessage(msg);
                        throw ex;
                    }

                } else if (EntityOperation.ADD.equals(op.name)) {
                    // Append message
                    try {
                        EntityMessage msg = message.getMessage(op.message);
                        Properties props = MessageHelper.getSessionProperties();
                        Session isession = Session.getDefaultInstance(props, null);
                        MimeMessage imessage = MessageHelper.from(msg, isession);

                        ifolder.appendMessages(new Message[]{imessage});

                        // Draft can be saved multiple times
                        if (msg.uid != null) {
                            Message previously = ifolder.getMessageByUID(msg.uid);
                            previously.setFlag(Flags.Flag.DELETED, true);
                            ifolder.expunge();
                        }

                        message.deleteMessage(op.message);
                    } catch (MessagingException ex) {
                        // Countermeasure
                        // TODO: try again?
                        throw ex;
                    }

                } else if (EntityOperation.MOVE.equals(op.name)) {
                    // Move message
                    try {
                        Message imessage = ifolder.getMessageByUID(op.uid);
                        EntityFolder archive = db.folder().getFolder(jargs.getLong(0));
                        Folder target = istore.getFolder(archive.name);

                        ifolder.moveMessages(new Message[]{imessage}, target);

                        message.deleteMessage(op.message);
                    } catch (MessagingException ex) {
                        // Countermeasure
                        Log.i(Helper.TAG, folder.name + " countermeasure " + op.id + "/" + op.name);
                        EntityMessage msg = message.getMessage(op.message);
                        msg.ui_hide = false;
                        message.updateMessage(msg);
                        throw ex;
                    }

                } else if (EntityOperation.DELETE.equals(op.name)) {
                    // Delete message
                    try {
                        if (op.uid != null) {
                            Message imessage = ifolder.getMessageByUID(op.uid);
                            if (imessage != null) {
                                imessage.setFlag(Flags.Flag.DELETED, true);
                                ifolder.expunge();
                            } else
                                Log.w(Helper.TAG, "Remote message not found uid=" + op.uid);
                        } else {
                            // Not appended draft
                            Log.w(Helper.TAG, "Delete without uid id=" + op.message);
                        }

                        message.deleteMessage(op.message);
                    } catch (MessagingException ex) {
                        // Countermeasure
                        Log.i(Helper.TAG, folder.name + " countermeasure " + op.id + "/" + op.name);
                        EntityMessage msg = message.getMessage(op.message);
                        msg.ui_hide = false;
                        message.updateMessage(msg);
                        throw ex;
                    }

                } else if (EntityOperation.SEND.equals(op.name)) {
                    // Send message
                    EntityMessage msg = message.getMessage(op.message);
                    EntityMessage reply = (msg.replying == null ? null : message.getMessage(msg.replying));
                    EntityIdentity ident = db.identity().getIdentity(msg.identity);

                    try {
                        Properties props = MessageHelper.getSessionProperties();
                        Session isession = Session.getDefaultInstance(props, null);

                        MimeMessage imessage;
                        if (reply == null)
                            imessage = MessageHelper.from(msg, isession);
                        else
                            imessage = MessageHelper.from(msg, reply, isession);
                        if (ident.replyto != null)
                            imessage.setReplyTo(new Address[]{new InternetAddress(ident.replyto)});

                        Transport itransport = isession.getTransport(ident.starttls ? "smtp" : "smtps");
                        try {
                            itransport.connect(ident.host, ident.port, ident.user, ident.password);

                            Address[] to = imessage.getRecipients(Message.RecipientType.TO);
                            itransport.sendMessage(imessage, to);
                            Log.i(Helper.TAG, "Sent via " + ident.host + "/" + ident.user +
                                    " to " + TextUtils.join(", ", to));

                            // Make sure the message is sent only once
                            operation.deleteOperation(op.id);

                            message.deleteMessage(op.message);
                        } finally {
                            itransport.close();
                        }

                    } catch (MessagingException ex) {
                        // Countermeasure
                        Log.i(Helper.TAG, folder.name + " countermeasure " + op.id + "/" + op.name);
                        EntityFolder drafts = db.folder().getPrimaryDraftFolder();
                        msg.folder = drafts.id;
                        message.updateMessage(msg);
                        // Message will not be sent to remote
                        throw ex;
                    }

                } else
                    throw new MessagingException("Unknown operation name=" + op.name);

                operation.deleteOperation(op.id);
            }
        } finally {
            Log.i(Helper.TAG, folder.name + " end process");
        }
    }

    private void synchronizeFolders(EntityAccount account, IMAPStore istore) throws MessagingException {
        try {
            Log.i(Helper.TAG, "Start sync folders");

            DaoFolder dao = DB.getInstance(this).folder();

            List<String> names = new ArrayList<>();
            for (EntityFolder folder : dao.getUserFolders(account.id))
                names.add(folder.name);
            Log.i(Helper.TAG, "Local folder count=" + names.size());

            Folder[] ifolders = istore.getDefaultFolder().list("*");
            Log.i(Helper.TAG, "Remote folder count=" + ifolders.length);

            for (Folder ifolder : ifolders) {
                String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                boolean candidate = true;
                for (String attr : attrs) {
                    if ("\\Noselect".equals(attr)) {
                        candidate = false;
                        break;
                    }
                    if (attr.startsWith("\\"))
                        if (EntityFolder.STANDARD_FOLDER_ATTR.contains(attr.substring(1))) {
                            candidate = false;
                            break;
                        }
                }
                if (candidate) {
                    Log.i(Helper.TAG, ifolder.getFullName() + " candidate attr=" + TextUtils.join(",", attrs));
                    EntityFolder folder = dao.getFolder(account.id, ifolder.getFullName());
                    if (folder == null) {
                        folder = new EntityFolder();
                        folder.account = account.id;
                        folder.name = ifolder.getFullName();
                        folder.type = EntityFolder.TYPE_USER;
                        folder.synchronize = false;
                        folder.after = 0;
                        dao.insertFolder(folder);
                        Log.i(Helper.TAG, folder.name + " added");
                    } else
                        names.remove(folder.name);
                }
            }

            Log.i(Helper.TAG, "Delete local folder=" + names.size());
            for (String name : names)
                dao.deleteFolder(account.id, name);
        } finally {
            Log.i(Helper.TAG, "End sync folder");
        }
    }

    private void synchronizeMessages(EntityFolder folder, IMAPFolder ifolder) throws MessagingException, JSONException {
        try {
            Log.i(Helper.TAG, folder.name + " start sync after=" + folder.after);

            DB db = DB.getInstance(ServiceSynchronize.this);
            DaoMessage dao = db.message();

            // Get reference times
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -folder.after);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long ago = cal.getTimeInMillis();
            Log.i(Helper.TAG, folder.name + " ago=" + new Date(ago));

            // Delete old local messages
            int old = dao.deleteMessagesBefore(folder.id, ago);
            Log.i(Helper.TAG, folder.name + " local old=" + old);

            // Get list of local uids
            List<Long> uids = dao.getUids(folder.id, ago);
            Log.i(Helper.TAG, folder.name + " local count=" + uids.size());

            // Reduce list of local uids
            long search = SystemClock.elapsedRealtime();
            Message[] imessages = ifolder.search(new ReceivedDateTerm(ComparisonTerm.GE, new Date(ago)));
            Log.i(Helper.TAG, folder.name + " remote count=" + imessages.length +
                    " search=" + (SystemClock.elapsedRealtime() - search) + " ms");

            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.FLAGS);
            ifolder.fetch(imessages, fp);

            long fetch = SystemClock.elapsedRealtime();
            Log.i(Helper.TAG, folder.name + " remote fetched=" + (SystemClock.elapsedRealtime() - fetch) + " ms");

            List<Message> added = new ArrayList<>();
            for (Message imessage : imessages)
                if (!imessage.isExpunged() && !imessage.isSet(Flags.Flag.DELETED)) {
                    long uid = ifolder.getUID(imessage);
                    if (!uids.remove(uid))
                        added.add(imessage);
                }

            // Delete local messages not at remote
            Log.i(Helper.TAG, folder.name + " delete=" + uids.size());
            for (Long uid : uids) {
                Log.i(Helper.TAG, folder.name + " delete local uid=" + uid);
                dao.deleteMessage(folder.id, uid);
            }
            Log.i(Helper.TAG, folder.name + " synced");

            Log.i(Helper.TAG, folder.name + " added count=" + added.size());
            for (int batch = 0; batch < added.size(); batch += FETCH_BATCH_SIZE) {
                Log.i(Helper.TAG, folder.name + " fetch @" + batch);
                try {
                    db.beginTransaction();
                    for (int i = 0; i < FETCH_BATCH_SIZE && batch + i < added.size(); i++)
                        synchronizeMessage(folder, ifolder, (IMAPMessage) added.get(batch + i));
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        } finally {
            Log.i(Helper.TAG, folder.name + " end sync");
        }
    }

    private void synchronizeMessage(EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage) throws MessagingException, JSONException {
        FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        fp.add(IMAPFolder.FetchProfileItem.FLAGS);
        ifolder.fetch(new Message[]{imessage}, fp);

        boolean expunged = imessage.isExpunged();
        boolean deleted = (!expunged && imessage.isSet(Flags.Flag.DELETED));
        if (expunged || deleted) {
            Log.i(Helper.TAG, "Message gone expunged=" + expunged + " deleted=" + deleted);
            return;
        }

        long uid = ifolder.getUID(imessage);
        Log.i(Helper.TAG, folder.name + " sync uid=" + uid);

        MessageHelper helper = new MessageHelper(imessage);
        boolean seen = helper.getSeen();

        DB db = DB.getInstance(ServiceSynchronize.this);
        EntityMessage message = db.message().getMessage(folder.id, uid);
        if (message == null) {
            FetchProfile fp1 = new FetchProfile();
            fp1.add(FetchProfile.Item.ENVELOPE);
            fp1.add(FetchProfile.Item.CONTENT_INFO);
            fp1.add(IMAPFolder.FetchProfileItem.HEADERS);
            fp1.add(IMAPFolder.FetchProfileItem.MESSAGE);
            ifolder.fetch(new Message[]{imessage}, fp1);

            message = new EntityMessage();
            message.account = folder.account;
            message.folder = folder.id;
            message.uid = uid;
            message.msgid = helper.getMessageID();
            message.references = TextUtils.join(" ", helper.getReferences());
            message.inreplyto = helper.getInReplyTo();
            message.thread = helper.getThreadId(uid);
            message.from = helper.getFrom();
            message.to = helper.getTo();
            message.cc = helper.getCc();
            message.bcc = null;
            message.reply = helper.getReply();
            message.subject = imessage.getSubject();
            message.body = helper.getHtml();
            message.received = imessage.getReceivedDate().getTime();
            message.sent = imessage.getSentDate().getTime();
            message.seen = seen;
            message.ui_seen = seen;
            message.ui_hide = false;

            message.id = db.message().insertMessage(message);
            Log.i(Helper.TAG, folder.name + " added uid=" + uid + " id=" + message.id);
        } else if (message.seen != seen) {
            message.seen = seen;
            message.ui_seen = seen;

            db.message().updateMessage(message);
            Log.i(Helper.TAG, folder.name + " updated uid=" + uid + " id=" + message.id);
        }
    }

    private void noop(EntityFolder folder, final IMAPFolder ifolder) throws MessagingException {
        Log.i(Helper.TAG, folder.name + " request NOOP");
        ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                Log.i(Helper.TAG, ifolder.getName() + " start NOOP");
                p.simpleCommand("NOOP", null);
                Log.i(Helper.TAG, ifolder.getName() + " end NOOP");
                return null;
            }
        });
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        private Thread mainThread;
        private EntityFolder outbox = null;

        @Override
        public void onAvailable(Network network) {
            Log.i(Helper.TAG, "Available " + network);

            synchronized (state) {
                if (!state.running) {
                    state.threads.clear();
                    state.running = true;

                    mainThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DB db = DB.getInstance(ServiceSynchronize.this);
                            try {
                                List<EntityAccount> accounts = db.account().getAccounts(true);
                                if (accounts.size() == 0) {
                                    Log.i(Helper.TAG, "No accounts, halt");
                                    stopSelf();
                                } else
                                    for (final EntityAccount account : accounts) {
                                        Log.i(Helper.TAG, account.host + "/" + account.user + " run");
                                        Thread thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    monitorAccount(account);
                                                } catch (Throwable ex) {
                                                    // Fallsafe
                                                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                                }
                                            }
                                        }, "sync.account." + account.id);
                                        state.threads.add(thread);
                                        thread.start();
                                    }
                            } catch (Throwable ex) {
                                // Failsafe
                                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                            }

                            outbox = db.folder().getOutbox();
                            if (outbox != null) {
                                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ServiceSynchronize.this);
                                lbm.registerReceiver(receiverOutbox, new IntentFilter(ACTION_PROCESS_OPERATIONS + outbox.id));
                                Log.i(Helper.TAG, outbox.name + " listen process id=" + outbox.id);
                                lbm.sendBroadcast(new Intent(ACTION_PROCESS_OPERATIONS + outbox.id));
                            }

                        }
                    }, "sync.main");
                    mainThread.start();
                }
            }
        }

        @Override
        public void onLost(Network network) {
            Log.i(Helper.TAG, "Lost " + network);

            synchronized (state) {
                if (state.running) {
                    state.running = false;
                    state.notifyAll();
                }
            }

            if (outbox != null) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ServiceSynchronize.this);
                lbm.unregisterReceiver(receiverOutbox);
                Log.i(Helper.TAG, outbox.name + " unlisten process id=" + outbox.id);
            }
        }

        BroadcastReceiver receiverOutbox = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(Helper.TAG, outbox.name + " submit process id=" + outbox.id);
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            synchronized (outbox) {
                                processOperations(outbox, null, null);
                            }
                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, outbox.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                            NotificationManager nm = getSystemService(NotificationManager.class);
                            nm.notify("error", outbox.id.intValue(), getNotification(outbox.name, ex).build());
                        }
                    }
                });
            }
        };
    };

    public static void start(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);

            NotificationChannel service = new NotificationChannel(
                    "service",
                    context.getString(R.string.channel_service),
                    NotificationManager.IMPORTANCE_MIN);
            service.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            nm.createNotificationChannel(service);

            NotificationChannel error = new NotificationChannel(
                    "error",
                    context.getString(R.string.channel_error),
                    NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(error);
        }

        ContextCompat.startForegroundService(context, new Intent(context, ServiceSynchronize.class));
    }

    public static void restart(Context context, String reason) {
        Log.i(Helper.TAG, "Restart because of '" + reason + "'");
        context.stopService(new Intent(context, ServiceSynchronize.class));
        start(context);
    }
}
