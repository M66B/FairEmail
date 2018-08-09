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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.smtp.SMTPSendFailedException;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
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

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ServiceSynchronize extends LifecycleService {
    private ServiceState state = new ServiceState();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final int NOTIFICATION_SYNCHRONIZE = 1;
    private static final int NOTIFICATION_UNSEEN = 2;

    private static final long NOOP_INTERVAL = 9 * 60 * 1000L; // ms
    private static final int FETCH_BATCH_SIZE = 10;
    private static final int DOWNLOAD_BUFFER_SIZE = 8192; // bytes

    static final String ACTION_PROCESS_FOLDER = BuildConfig.APPLICATION_ID + ".PROCESS_FOLDER";
    static final String ACTION_PROCESS_OUTBOX = BuildConfig.APPLICATION_ID + ".PROCESS_OUTBOX";

    private class ServiceState {
        boolean running = false;
        List<Thread> threads = new ArrayList<>(); // accounts
    }

    public ServiceSynchronize() {
        // https://docs.oracle.com/javaee/6/api/javax/mail/internet/package-summary.html
        System.setProperty("mail.mime.ignoreunknownencoding", "true");
        System.setProperty("mail.mime.decodefilename", "true");
        System.setProperty("mail.mime.encodefilename", "true");
    }

    @Override
    public void onCreate() {
        Log.i(Helper.TAG, "Service create");
        super.onCreate();
        startForeground(NOTIFICATION_SYNCHRONIZE, getNotificationService(0, 0).build());

        // Listen for network changes
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        DB.getInstance(this).account().liveStats().observe(this, new Observer<TupleAccountStats>() {
            private int prev_unseen = -1;

            @Override
            public void onChanged(@Nullable TupleAccountStats stats) {
                if (stats != null) {
                    NotificationManager nm = getSystemService(NotificationManager.class);
                    nm.notify(NOTIFICATION_SYNCHRONIZE,
                            getNotificationService(stats.accounts, stats.operations).build());

                    if (stats.unseen > 0) {
                        if (stats.unseen > prev_unseen) {
                            nm.cancel(NOTIFICATION_UNSEEN);
                            nm.notify(NOTIFICATION_UNSEEN, getNotificationUnseen(stats.unseen).build());
                        }
                    } else
                        nm.cancel(NOTIFICATION_UNSEEN);

                    prev_unseen = stats.unseen;
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

        if ("unseen".equals(intent.getAction())) {
            final long now = new Date().getTime();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    DaoAccount dao = DB.getInstance(ServiceSynchronize.this).account();
                    for (EntityAccount account : dao.getAccounts(true)) {
                        account.seen_until = now;
                        dao.updateAccount(account);
                    }
                    Log.i(Helper.TAG, "Updated seen until");
                }
            });
        }

        return START_STICKY;
    }

    private Notification.Builder getNotificationService(int accounts, int operations) {
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
                .setContentTitle(getResources().getQuantityString(R.plurals.title_notification_synchronizing, accounts, accounts))
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_SECRET);

        if (operations > 0)
            builder.setContentText(getResources().getQuantityString(R.plurals.title_notification_operations, operations, operations));

        return builder;
    }

    private Notification.Builder getNotificationUnseen(int unseen) {
        // Build pending intent
        Intent intent = new Intent(this, ActivityView.class);
        intent.setAction("unseen");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                this, ActivityView.REQUEST_UNSEEN, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent delete = new Intent(this, ServiceSynchronize.class);
        delete.setAction("unseen");
        PendingIntent pid = PendingIntent.getService(this, 1, delete, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Build notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this, "notification");
        else
            builder = new Notification.Builder(this);

        builder
                .setSmallIcon(R.drawable.baseline_mail_24)
                .setContentTitle(getResources().getQuantityString(R.plurals.title_notification_unseen, unseen, unseen))
                .setContentIntent(pi)
                .setSound(uri)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setDeleteIntent(pid);

        return builder;
    }

    private Notification.Builder getNotificationError(String action, Throwable ex) {
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
                .setContentTitle(getString(R.string.title_notification_failed, action))
                .setContentText(Helper.formatThrowable(ex))
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setShowWhen(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_ERROR)
                .setVisibility(Notification.VISIBILITY_SECRET);

        return builder;
    }

    private void reportError(String account, String folder, Throwable ex) {
        String action = account + "/" + folder;
        if (!(ex instanceof IllegalStateException) && // This operation is not allowed on a closed folder
                !(ex instanceof FolderClosedException)) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.notify(action, 1, getNotificationError(action, ex).build());
        }
    }

    private void monitorAccount(final EntityAccount account) {
        Log.i(Helper.TAG, account.name + " start ");

        while (state.running) {
            IMAPStore istore = null;
            try {
                Properties props = MessageHelper.getSessionProperties();
                props.put("mail.imaps.peek", "true");
                props.setProperty("mail.mime.address.strict", "false");
                props.setProperty("mail.mime.decodetext.strict", "false");
                //props.put("mail.imaps.minidletime", "5000");
                Session isession = Session.getInstance(props, null);
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
                    Map<Long, IMAPFolder> mapFolder = new HashMap<>();

                    @Override
                    public void opened(ConnectionEvent e) {
                        Log.i(Helper.TAG, account.name + " opened");
                        try {
                            DB db = DB.getInstance(ServiceSynchronize.this);

                            synchronizeFolders(account, fstore);

                            for (final EntityFolder folder : db.folder().getFolders(account.id, true)) {
                                Log.i(Helper.TAG, account.name + " sync folder " + folder.name);
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        IMAPFolder ifolder = null;
                                        try {
                                            Log.i(Helper.TAG, folder.name + " start");

                                            ifolder = (IMAPFolder) fstore.getFolder(folder.name);
                                            ifolder.open(Folder.READ_WRITE);

                                            synchronized (mapFolder) {
                                                mapFolder.put(folder.id, ifolder);
                                            }

                                            monitorFolder(account, folder, fstore, ifolder);
                                        } catch (FolderNotFoundException ex) {
                                            Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                        } catch (Throwable ex) {
                                            Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                            reportError(account.name, folder.name, ex);

                                            // Cascade up
                                            try {
                                                fstore.close();
                                            } catch (MessagingException e1) {
                                                Log.w(Helper.TAG, account.name + " " + e1 + "\n" + Log.getStackTraceString(e1));
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
                                }, "sync.folder." + folder.id);
                                folderThreads.add(thread);
                                thread.start();
                            }

                            IntentFilter f = new IntentFilter(ACTION_PROCESS_FOLDER);
                            f.addDataType("account/" + account.id);
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ServiceSynchronize.this);
                            lbm.registerReceiver(processReceiver, f);
                            Log.i(Helper.TAG, "listen process folder");
                            for (final EntityFolder folder : db.folder().getFolders(account.id))
                                if (!EntityFolder.TYPE_OUTBOX.equals(folder.type))
                                    lbm.sendBroadcast(new Intent(ACTION_PROCESS_FOLDER)
                                            .setType("account/" + account.id)
                                            .putExtra("folder", folder.id));

                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                            reportError(account.name, null, ex);

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

                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ServiceSynchronize.this);
                        lbm.unregisterReceiver(processReceiver);

                        synchronized (mapFolder) {
                            mapFolder.clear();
                        }

                        // Check connection
                        synchronized (state) {
                            state.notifyAll();
                        }
                    }

                    @Override
                    public void closed(ConnectionEvent e) {
                        Log.e(Helper.TAG, account.name + " closed");

                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ServiceSynchronize.this);
                        lbm.unregisterReceiver(processReceiver);

                        synchronized (mapFolder) {
                            mapFolder.clear();
                        }

                        // Check connection
                        synchronized (state) {
                            state.notifyAll();
                        }
                    }

                    BroadcastReceiver processReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            final long fid = intent.getLongExtra("folder", -1);

                            IMAPFolder ifolder;
                            synchronized (mapFolder) {
                                ifolder = mapFolder.get(fid);
                            }
                            final boolean shouldClose = (ifolder == null);
                            final IMAPFolder ffolder = ifolder;

                            Log.i(Helper.TAG, "run operations folder=" + fid + " offline=" + shouldClose);
                            executor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    DB db = DB.getInstance(ServiceSynchronize.this);
                                    EntityFolder folder = db.folder().getFolder(fid);
                                    IMAPFolder ifolder = ffolder;
                                    try {
                                        Log.i(Helper.TAG, folder.name + " start operations");

                                        if (ifolder == null) {
                                            // Prevent unnecessary folder connections
                                            if (db.operation().getOperationCount(fid) == 0)
                                                return;

                                            ifolder = (IMAPFolder) fstore.getFolder(folder.name);
                                            ifolder.open(Folder.READ_WRITE);
                                        }

                                        processOperations(folder, fstore, ifolder);
                                    } catch (FolderNotFoundException ex) {
                                        Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                    } catch (Throwable ex) {
                                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                        reportError(account.name, folder.name, ex);
                                    } finally {
                                        if (shouldClose)
                                            if (ifolder != null && ifolder.isOpen()) {
                                                try {
                                                    ifolder.close(false);
                                                } catch (MessagingException ex) {
                                                    Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                                }
                                            }
                                        Log.i(Helper.TAG, folder.name + " stop operations");
                                    }
                                }
                            });
                        }
                    };
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

    private void monitorFolder(final EntityAccount account, final EntityFolder folder, final IMAPStore istore, final IMAPFolder ifolder) throws MessagingException, JSONException, IOException {
        // Listen for new and deleted messages
        ifolder.addMessageCountListener(new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent e) {
                try {
                    Log.i(Helper.TAG, folder.name + " messages added");
                    for (Message imessage : e.getMessages())
                        synchronizeMessage(folder, ifolder, (IMAPMessage) imessage);
                } catch (MessageRemovedException ex) {
                    Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    reportError(account.name, folder.name, ex);

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
                    for (Message imessage : e.getMessages())
                        try {
                            long uid = ifolder.getUID(imessage);
                            DB db = DB.getInstance(ServiceSynchronize.this);
                            int count = db.message().deleteMessage(folder.id, uid);
                            Log.i(Helper.TAG, "Deleted uid=" + uid + " count=" + count);
                        } catch (MessageRemovedException ex) {
                            Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                        }
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    reportError(account.name, folder.name, ex);

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
                    synchronizeMessage(folder, ifolder, (IMAPMessage) e.getMessage());
                } catch (MessageRemovedException ex) {
                    Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    reportError(account.name, folder.name, ex);

                    // Cascade up
                    try {
                        istore.close();
                    } catch (MessagingException e1) {
                        Log.w(Helper.TAG, folder.name + " " + e1 + "\n" + Log.getStackTraceString(e1));
                    }
                }
            }
        });

        // Keep alive
        Log.i(Helper.TAG, folder.name + " start");
        try {
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
                            open = ifolder.isOpen();
                            if (open)
                                noop(folder, ifolder);
                        } while (open);
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                        reportError(account.name, folder.name, ex);

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
            Log.i(Helper.TAG, folder.name + " end");
        }
    }

    private void processOperations(EntityFolder folder, IMAPStore istore, IMAPFolder ifolder) throws MessagingException, JSONException, IOException {
        try {
            Log.i(Helper.TAG, folder.name + " start process");

            DB db = DB.getInstance(this);
            DaoOperation operation = db.operation();
            DaoMessage message = db.message();
            for (TupleOperationEx op : operation.getOperations(folder.id))
                try {
                    Log.i(Helper.TAG, folder.name +
                            " start op=" + op.id + "/" + op.name +
                            " args=" + op.args +
                            " msg=" + op.message);

                    JSONArray jargs = new JSONArray(op.args);
                    try {
                        if (EntityOperation.SEEN.equals(op.name)) {
                            // Mark message (un)seen
                            Message imessage = ifolder.getMessageByUID(op.uid);
                            if (imessage == null)
                                throw new MessageRemovedException();
                            imessage.setFlag(Flags.Flag.SEEN, jargs.getBoolean(0));

                        } else if (EntityOperation.ADD.equals(op.name)) {
                            // Append message
                            EntityMessage msg = message.getMessage(op.message);
                            if (msg == null)
                                return;

                            // Disconnect from remote to prevent deletion
                            Long uid = msg.uid;
                            if (msg.uid != null) {
                                msg.uid = null;
                                message.updateMessage(msg);
                            }

                            // Execute append
                            Properties props = MessageHelper.getSessionProperties();
                            Session isession = Session.getInstance(props, null);
                            MimeMessage imessage = MessageHelper.from(msg, isession);
                            ifolder.appendMessages(new Message[]{imessage});

                            // Drafts can be appended multiple times
                            if (uid != null) {
                                Message previously = ifolder.getMessageByUID(uid);
                                if (previously == null)
                                    throw new MessageRemovedException();

                                previously.setFlag(Flags.Flag.DELETED, true);
                                ifolder.expunge();
                            }

                        } else if (EntityOperation.MOVE.equals(op.name)) {
                            EntityFolder target = db.folder().getFolder(jargs.getLong(0));
                            if (target == null)
                                throw new FolderNotFoundException();

                            // Move message
                            Message imessage = ifolder.getMessageByUID(op.uid);
                            if (imessage == null)
                                throw new MessageRemovedException();

                            Folder itarget = istore.getFolder(target.name);
                            if (istore.hasCapability("MOVE"))
                                ifolder.moveMessages(new Message[]{imessage}, itarget);
                            else {
                                Log.i(Helper.TAG, "MOVE by APPEND/DELETE");
                                EntityMessage msg = message.getMessage(op.message);

                                // Execute append
                                Properties props = MessageHelper.getSessionProperties();
                                Session isession = Session.getInstance(props, null);
                                MimeMessage icopy = MessageHelper.from(msg, isession);
                                itarget.appendMessages(new Message[]{icopy});

                                // Execute delete
                                imessage.setFlag(Flags.Flag.DELETED, true);
                                ifolder.expunge();
                            }

                            message.deleteMessage(op.message);

                        } else if (EntityOperation.DELETE.equals(op.name)) {
                            // Delete message
                            if (op.uid != null) {
                                Message imessage = ifolder.getMessageByUID(op.uid);
                                if (imessage == null)
                                    throw new MessageRemovedException();
                                imessage.setFlag(Flags.Flag.DELETED, true);
                                ifolder.expunge();
                            }

                            message.deleteMessage(op.message);

                        } else if (EntityOperation.SEND.equals(op.name)) {
                            // Send message
                            EntityMessage msg = message.getMessage(op.message);
                            if (msg == null)
                                return;

                            EntityMessage reply = (msg.replying == null ? null : message.getMessage(msg.replying));
                            EntityIdentity ident = db.identity().getIdentity(msg.identity);

                            if (ident == null || !ident.synchronize) {
                                // Message will remain in outbox
                                return;
                            }

                            // Create session
                            Properties props = MessageHelper.getSessionProperties();
                            Session isession = Session.getInstance(props, null);

                            // Create message
                            MimeMessage imessage;
                            if (reply == null)
                                imessage = MessageHelper.from(msg, isession);
                            else
                                imessage = MessageHelper.from(msg, reply, isession);
                            if (ident.replyto != null)
                                imessage.setReplyTo(new Address[]{new InternetAddress(ident.replyto)});

                            // Create transport
                            Transport itransport = isession.getTransport(ident.starttls ? "smtp" : "smtps");
                            try {
                                // Connect transport
                                itransport.connect(ident.host, ident.port, ident.user, ident.password);

                                // Send message
                                Address[] to = imessage.getAllRecipients();
                                itransport.sendMessage(imessage, to);
                                Log.i(Helper.TAG, "Sent via " + ident.host + "/" + ident.user +
                                        " to " + TextUtils.join(", ", to));

                                msg.sent = new Date().getTime();
                                msg.seen = true;
                                msg.ui_seen = true;

                                EntityFolder sent = db.folder().getFolderByType(ident.account, EntityFolder.TYPE_SENT);
                                if (sent != null) {
                                    Log.i(Helper.TAG, "Moving to sent folder=" + sent.id);
                                    msg.folder = sent.id;
                                }

                                message.updateMessage(msg);

                            } finally {
                                itransport.close();
                            }
                            // TODO: cache transport?

                        } else if (EntityOperation.ATTACHMENT.equals(op.name)) {
                            int sequence = jargs.getInt(0);
                            EntityAttachment attachment = db.attachment().getAttachment(op.message, sequence);
                            if (attachment == null)
                                return;

                            try {
                                // Get message
                                Message imessage = ifolder.getMessageByUID(op.uid);
                                if (imessage == null)
                                    throw new MessageRemovedException();

                                // Get attachment
                                MessageHelper helper = new MessageHelper((MimeMessage) imessage);
                                EntityAttachment a = helper.getAttachments().get(sequence - 1);

                                // Download attachment
                                InputStream is = a.part.getInputStream();
                                ByteArrayOutputStream os = new ByteArrayOutputStream();
                                byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
                                for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                                    os.write(buffer, 0, len);

                                    // Update progress
                                    if (attachment.size != null) {
                                        attachment.progress = os.size() * 100 / attachment.size;
                                        db.attachment().updateAttachment(attachment);
                                        Log.i(Helper.TAG, "Progress %=" + attachment.progress);
                                    }
                                }

                                // Store attachment data
                                attachment.progress = null;
                                attachment.content = os.toByteArray();
                                db.attachment().updateAttachment(attachment);
                                Log.i(Helper.TAG, "Downloaded bytes=" + attachment.content.length);
                            } catch (Throwable ex) {
                                // Reset progress on failure
                                attachment.progress = null;
                                db.attachment().updateAttachment(attachment);
                                throw ex;
                            }
                        } else
                            throw new MessagingException("Unknown operation name=" + op.name);

                        // Operation succeeded
                        operation.deleteOperation(op.id);

                    } catch (MessageRemovedException ex) {
                        Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));

                        // There is no use in repeating
                        operation.deleteOperation(op.id);
                    } catch (FolderNotFoundException ex) {
                        Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));

                        // There is no use in repeating
                        operation.deleteOperation(op.id);
                    } catch (SMTPSendFailedException ex) {
                        // TODO: response codes: https://www.ietf.org/rfc/rfc821.txt
                        Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));

                        // There is probably no use in repeating
                        operation.deleteOperation(op.id);
                        throw ex;
                    } catch (MessagingException ex) {
                        // Socket timeout is a recoverable condition (send message)
                        if (ex.getCause() instanceof SocketTimeoutException) {
                            Log.w(Helper.TAG, "Recoverable " + ex);
                            // No need to inform user
                            return;
                        } else
                            throw ex;
                    }
                } finally {
                    Log.i(Helper.TAG, folder.name + " end op=" + op.id + "/" + op.name);
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

            Folder[] ifolders = istore.getDefaultFolder().list("*"); // TODO: is the pattern correct?
            Log.i(Helper.TAG, "Remote folder count=" + ifolders.length);

            for (Folder ifolder : ifolders) {
                String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                boolean candidate = true;
                for (String attr : attrs) {
                    if ("\\Noselect".equals(attr)) { // TODO: is this attribute correct?
                        candidate = false;
                        break;
                    }
                    if (attr.startsWith("\\"))
                        if (EntityFolder.SYSTEM_FOLDER_ATTR.contains(attr.substring(1))) {
                            candidate = false;
                            break;
                        }
                }
                if (candidate) {
                    Log.i(Helper.TAG, ifolder.getFullName() + " candidate attr=" + TextUtils.join(",", attrs));
                    EntityFolder folder = dao.getFolderByName(account.id, ifolder.getFullName());
                    if (folder == null) {
                        folder = new EntityFolder();
                        folder.account = account.id;
                        folder.name = ifolder.getFullName();
                        folder.type = EntityFolder.TYPE_USER;
                        folder.synchronize = false;
                        folder.after = EntityFolder.DEFAULT_USER_SYNC;
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

    private void synchronizeMessages(EntityFolder folder, IMAPFolder ifolder) throws MessagingException, JSONException, IOException {
        try {
            Log.i(Helper.TAG, folder.name + " start sync after=" + folder.after);

            DB db = DB.getInstance(this);
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

            for (Message imessage : imessages)
                try {
                    uids.remove(ifolder.getUID(imessage));
                } catch (MessageRemovedException ex) {
                    Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                }

            // Delete local messages not at remote
            Log.i(Helper.TAG, folder.name + " delete=" + uids.size());
            for (Long uid : uids) {
                int count = dao.deleteMessage(folder.id, uid);
                Log.i(Helper.TAG, folder.name + " delete local uid=" + uid + " count=" + count);
            }

            // Add/update local messages
            Log.i(Helper.TAG, folder.name + " add=" + imessages.length);
            for (int batch = 0; batch < imessages.length; batch += FETCH_BATCH_SIZE) {
                Log.i(Helper.TAG, folder.name + " fetch @" + batch);
                try {
                    db.beginTransaction();
                    for (int i = 0; i < FETCH_BATCH_SIZE && batch + i < imessages.length; i++)
                        try {
                            synchronizeMessage(folder, ifolder, (IMAPMessage) imessages[batch + i]);
                        } catch (MessageRemovedException ex) {
                            Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                        }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        } finally {
            Log.i(Helper.TAG, folder.name + " end sync");
        }
    }

    private void synchronizeMessage(EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage) throws MessagingException, JSONException, IOException {
        long uid = -1;
        try {
            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.FLAGS);
            ifolder.fetch(new Message[]{imessage}, fp);

            uid = ifolder.getUID(imessage);
            Log.v(Helper.TAG, folder.name + " start sync uid=" + uid);

            if (imessage.isExpunged()) {
                Log.i(Helper.TAG, folder.name + " expunged uid=" + uid);
                return;
            }
            if (imessage.isSet(Flags.Flag.DELETED)) {
                Log.i(Helper.TAG, folder.name + " deleted uid=" + uid);
                return;
            }

            MessageHelper helper = new MessageHelper(imessage);
            boolean seen = helper.getSeen();

            DB db = DB.getInstance(this);
            EntityMessage message = db.message().getMessage(folder.id, uid);
            if (message == null) {
                FetchProfile fp1 = new FetchProfile();
                fp1.add(FetchProfile.Item.ENVELOPE);
                fp1.add(FetchProfile.Item.CONTENT_INFO);
                fp1.add(IMAPFolder.FetchProfileItem.HEADERS);
                fp1.add(IMAPFolder.FetchProfileItem.MESSAGE);
                ifolder.fetch(new Message[]{imessage}, fp1);

                long id = MimeMessageEx.getId(imessage);
                message = db.message().getMessage(id);
                if (message != null && message.folder != folder.id) {
                    if (EntityFolder.TYPE_ARCHIVE.equals(folder.type))
                        message = null;
                    else // Outbox to sent
                        message.folder = folder.id;
                }
                boolean update = (message != null);
                if (message == null)
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
                message.bcc = helper.getBcc();
                message.reply = helper.getReply();
                message.subject = imessage.getSubject();
                message.body = helper.getHtml();
                message.received = imessage.getReceivedDate().getTime();
                message.sent = (imessage.getSentDate() == null ? null : imessage.getSentDate().getTime());
                message.seen = seen;
                message.ui_seen = seen;
                message.ui_hide = false;

                if (update) {
                    db.message().updateMessage(message);
                    Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid);
                } else {
                    message.id = db.message().insertMessage(message);
                    Log.i(Helper.TAG, folder.name + " added id=" + message.id + " uid=" + message.uid);
                }

                int sequence = 0;
                for (EntityAttachment attachment : helper.getAttachments()) {
                    sequence++;
                    Log.i(Helper.TAG, "attachment seq=" + sequence +
                            " name=" + attachment.name + " type=" + attachment.type);
                    attachment.message = message.id;
                    attachment.sequence = sequence;
                    attachment.id = db.attachment().insertAttachment(attachment);
                }
            } else if (message.seen != seen) {
                message.seen = seen;
                message.ui_seen = seen;
                // TODO: synchronize all data?
                db.message().updateMessage(message);
                Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid);
            } else {
                Log.v(Helper.TAG, folder.name + " unchanged id=" + message.id + " uid=" + message.uid);
            }

        } finally {
            Log.v(Helper.TAG, folder.name + " end sync uid=" + uid);
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
                                lbm.registerReceiver(receiverOutbox, new IntentFilter(ACTION_PROCESS_OUTBOX));
                                Log.i(Helper.TAG, outbox.name + " listen operations");
                                lbm.sendBroadcast(new Intent(ACTION_PROCESS_OUTBOX));
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
                Log.i(Helper.TAG, outbox.name + " unlisten operations");
            }
        }

        BroadcastReceiver receiverOutbox = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(Helper.TAG, outbox.name + " run operations");
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.i(Helper.TAG, outbox.name + " start operations");
                            synchronized (outbox) {
                                processOperations(outbox, null, null);
                            }
                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, outbox.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                            reportError(null, outbox.name, ex);
                        } finally {
                            Log.i(Helper.TAG, outbox.name + " end operations");
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

            NotificationChannel notification = new NotificationChannel(
                    "notification",
                    context.getString(R.string.channel_notification),
                    NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(notification);

            NotificationChannel error = new NotificationChannel(
                    "error",
                    context.getString(R.string.channel_error),
                    NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(error);
        }

        ContextCompat.startForegroundService(context, new Intent(context, ServiceSynchronize.class));
    }

    public static void stop(Context context, String reason) {
        Log.i(Helper.TAG, "Stop because of '" + reason + "'");
        context.stopService(new Intent(context, ServiceSynchronize.class));
    }

    public static void restart(Context context, String reason) {
        Log.i(Helper.TAG, "Restart because of '" + reason + "'");
        context.stopService(new Intent(context, ServiceSynchronize.class));
        start(context);
    }
}
