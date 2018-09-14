package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.MailConnectException;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.StoreClosedException;
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
import javax.net.ssl.SSLException;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class ServiceSynchronize extends LifecycleService {
    private final Object lock = new Object();
    private ServiceManager serviceManager = new ServiceManager();

    private static final int NOTIFICATION_SYNCHRONIZE = 1;
    private static final int NOTIFICATION_UNSEEN = 2;

    private static final int CONNECT_BACKOFF_START = 8; // seconds
    private static final int CONNECT_BACKOFF_MAX = 1024; // seconds (1024 sec ~ 17 min)
    private static final long STORE_NOOP_INTERVAL = 9 * 60 * 1000L; // ms
    private static final int ATTACHMENT_AUTO_DOWNLOAD_SIZE = 32 * 1024; // bytes

    static final String ACTION_SYNCHRONIZE_FOLDER = BuildConfig.APPLICATION_ID + ".SYNCHRONIZE_FOLDER";
    static final String ACTION_PROCESS_OPERATIONS = BuildConfig.APPLICATION_ID + ".PROCESS_OPERATIONS";

    @Override
    public void onCreate() {
        Log.i(Helper.TAG, "Service create version=" + BuildConfig.VERSION_NAME);
        super.onCreate();
        startForeground(NOTIFICATION_SYNCHRONIZE, getNotificationService(0, 0, 0).build());

        // Listen for network changes
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        // Removed because of Android VPN service
        // builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        cm.registerNetworkCallback(builder.build(), serviceManager);

        DB db = DB.getInstance(this);

        db.account().liveStats().observe(this, new Observer<TupleAccountStats>() {
            @Override
            public void onChanged(@Nullable TupleAccountStats stats) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                nm.notify(NOTIFICATION_SYNCHRONIZE,
                        getNotificationService(stats.accounts, stats.operations, stats.unsent).build());
            }
        });

        db.message().liveUnseenUnified().observe(this, new Observer<List<EntityMessage>>() {
            private int prev_unseen = -1;

            @Override
            public void onChanged(List<EntityMessage> messages) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (messages.size() > 0) {
                    if (messages.size() > prev_unseen) {
                        nm.cancel(NOTIFICATION_UNSEEN);
                        nm.notify(NOTIFICATION_UNSEEN, getNotificationUnseen(messages).build());
                    }
                } else
                    nm.cancel(NOTIFICATION_UNSEEN);

                prev_unseen = messages.size();
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.i(Helper.TAG, "Service destroy");

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(serviceManager);

        serviceManager.stop();

        stopForeground(true);

        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.cancel(NOTIFICATION_SYNCHRONIZE);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Helper.TAG, "Service command intent=" + intent);
        super.onStartCommand(intent, flags, startId);

        if (intent != null)
            if ("reload".equals(intent.getAction()))
                serviceManager.restart();
            else if ("unseen".equals(intent.getAction())) {
                Bundle args = new Bundle();
                args.putLong("time", new Date().getTime());

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) {
                        long time = args.getLong("time");

                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            for (EntityAccount account : db.account().getAccounts(true))
                                db.account().setAccountSeenUntil(account.id, time);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        return null;
                    }

                    @Override
                    protected void onLoaded(Bundle args, Void data) {
                        Log.i(Helper.TAG, "Updated seen until");
                    }
                }.load(ServiceSynchronize.this, args);

            }

        return START_STICKY;
    }

    private Notification.Builder getNotificationService(int accounts, int operations, int unsent) {
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
                .setSmallIcon(R.drawable.baseline_compare_arrows_24)
                .setContentTitle(getResources().getQuantityString(R.plurals.title_notification_synchronizing, accounts, accounts))
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_SECRET);

        if (operations > 0)
            builder.setStyle(new Notification.BigTextStyle().setSummaryText(
                    getResources().getQuantityString(R.plurals.title_notification_operations, operations, operations)));

        if (unsent > 0)
            builder.setContentText(getResources().getQuantityString(R.plurals.title_notification_unsent, unsent, unsent));

        return builder;
    }

    private Notification.Builder getNotificationUnseen(List<EntityMessage> messages) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

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
                .setContentTitle(getResources().getQuantityString(R.plurals.title_notification_unseen, messages.size(), messages.size()))
                .setContentIntent(pi)
                .setSound(uri)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setDeleteIntent(pid);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O &&
                prefs.getBoolean("light", true)) {
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
            builder.setLights(0xff00ff00, 1000, 1000);
        }

        if (prefs.getBoolean("pro", false)) {
            DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
            StringBuilder sb = new StringBuilder();
            for (EntityMessage message : messages) {
                sb.append("<strong>").append(MessageHelper.getFormattedAddresses(message.from, false)).append("</strong>");
                if (!TextUtils.isEmpty(message.subject))
                    sb.append(": ").append(message.subject);
                sb.append(" ").append(df.format(new Date(message.sent == null ? message.received : message.sent)));
                sb.append("<br>");
            }

            builder.setStyle(new Notification.BigTextStyle().bigText(Html.fromHtml(sb.toString())));
        }

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

        builder.setStyle(new Notification.BigTextStyle().bigText(ex.toString()));

        return builder;
    }

    private void reportError(String account, String folder, Throwable ex) {
        // FolderClosedException: can happen when no connectivity

        // IllegalStateException:
        // - "This operation is not allowed on a closed folder"
        // - can happen when syncing message

        // ConnectionException
        // - failed to create new store connection (connectivity)

        // MailConnectException
        // - on connectity problems when connecting to store

        String action;
        if (TextUtils.isEmpty(account))
            action = folder;
        else if (TextUtils.isEmpty(folder))
            action = account;
        else
            action = account + "/" + folder;

        EntityLog.log(this, action + " " + Helper.formatThrowable(ex));

        if (ex instanceof SendFailedException) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.notify(action, 1, getNotificationError(action, ex).build());
        }

        if (BuildConfig.DEBUG &&
                !(ex instanceof SendFailedException) &&
                !(ex instanceof MailConnectException) &&
                !(ex instanceof FolderClosedException) &&
                !(ex instanceof IllegalStateException) &&
                !(ex instanceof AuthenticationFailedException) && // Also: Too many simultaneous connections
                !(ex instanceof StoreClosedException) &&
                !(ex instanceof MessagingException && ex.getCause() instanceof UnknownHostException) &&
                !(ex instanceof MessagingException && ex.getCause() instanceof ConnectionException) &&
                !(ex instanceof MessagingException && ex.getCause() instanceof SocketException) &&
                !(ex instanceof MessagingException && ex.getCause() instanceof SocketTimeoutException) &&
                !(ex instanceof MessagingException && ex.getCause() instanceof SSLException) &&
                !(ex instanceof MessagingException && "connection failure".equals(ex.getMessage()))) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.notify(action, 1, getNotificationError(action, ex).build());
        }
    }

    private void monitorAccount(final EntityAccount account, final ServiceState state) throws NoSuchProviderException {
        final DB db = DB.getInstance(this);
        final ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

        int backoff = CONNECT_BACKOFF_START;
        while (state.running) {
            EntityLog.log(this, account.name + " run");

            // Debug
            boolean debug = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("debug", false);
            System.setProperty("mail.socket.debug", Boolean.toString(debug));

            // Refresh token
            if (account.auth_type == Helper.AUTH_TYPE_GMAIL) {
                account.password = Helper.refreshToken(this, "com.google", account.user, account.password);
                db.account().setAccountPassword(account.id, account.password);
            }

            // Create session
            Properties props = MessageHelper.getSessionProperties(this, account.auth_type);
            final Session isession = Session.getInstance(props, null);
            isession.setDebug(debug);
            // adb -t 1 logcat | grep "fairemail\|System.out"

            final IMAPStore istore = (IMAPStore) isession.getStore("imaps");
            final Map<EntityFolder, IMAPFolder> folders = new HashMap<>();
            List<Thread> noops = new ArrayList<>();
            List<Thread> idlers = new ArrayList<>();
            try {
                // Listen for store events
                istore.addStoreListener(new StoreListener() {
                    @Override
                    public void notification(StoreEvent e) {
                        Log.i(Helper.TAG, account.name + " event: " + e.getMessage());
                        db.account().setAccountError(account.id, e.getMessage());

                        synchronized (state) {
                            state.notifyAll();
                        }
                    }
                });

                // Listen for folder events
                istore.addFolderListener(new FolderAdapter() {
                    @Override
                    public void folderCreated(FolderEvent e) {
                        Log.i(Helper.TAG, "Folder created=" + e.getFolder().getFullName());
                        synchronized (state) {
                            state.notifyAll();
                        }
                    }

                    @Override
                    public void folderRenamed(FolderEvent e) {
                        Log.i(Helper.TAG, "Folder renamed=" + e.getFolder());

                        String old = e.getFolder().getFullName();
                        String name = e.getNewFolder().getFullName();
                        int count = db.folder().renameFolder(account.id, old, name);
                        Log.i(Helper.TAG, "Renamed to " + name + " count=" + count);

                        synchronized (state) {
                            state.notifyAll();
                        }
                    }

                    @Override
                    public void folderDeleted(FolderEvent e) {
                        Log.i(Helper.TAG, "Folder deleted=" + e.getFolder().getFullName());
                        synchronized (state) {
                            state.notifyAll();
                        }
                    }
                });

                // Listen for connection events
                istore.addConnectionListener(new ConnectionAdapter() {

                    @Override
                    public void opened(ConnectionEvent e) {
                        Log.i(Helper.TAG, account.name + " opened");
                    }

                    @Override
                    public void disconnected(ConnectionEvent e) {
                        Log.e(Helper.TAG, account.name + " disconnected event");
                    }

                    @Override
                    public void closed(ConnectionEvent e) {
                        Log.e(Helper.TAG, account.name + " closed event");
                    }
                });

                // Initiate connection
                Log.i(Helper.TAG, account.name + " connect");
                for (EntityFolder folder : db.folder().getFolders(account.id))
                    db.folder().setFolderState(folder.id, null);
                db.account().setAccountState(account.id, "connecting");
                istore.connect(account.host, account.port, account.user, account.password);

                db.account().setAccountState(account.id, "connected");
                db.account().setAccountError(account.id, null);

                EntityLog.log(this, account.name + " connected");

                // Update folder list
                synchronizeFolders(account, istore, state);

                // Synchronize folders
                for (final EntityFolder folder : db.folder().getFolders(account.id, true)) {
                    Log.i(Helper.TAG, account.name + " sync folder " + folder.name);

                    db.folder().setFolderState(folder.id, "connecting");

                    final IMAPFolder ifolder = (IMAPFolder) istore.getFolder(folder.name);
                    try {
                        ifolder.open(Folder.READ_WRITE);
                    } catch (Throwable ex) {
                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                        throw ex;
                    }
                    folders.put(folder, ifolder);

                    db.folder().setFolderState(folder.id, "connected");
                    db.folder().setFolderError(folder.id, null);

                    // Keep folder connection alive
                    Thread noop = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Process pending operations
                                processOperations(folder, isession, istore, ifolder);

                                // Listen for new and deleted messages
                                ifolder.addMessageCountListener(new MessageCountAdapter() {
                                    @Override
                                    public void messagesAdded(MessageCountEvent e) {
                                        synchronized (lock) {
                                            try {
                                                Log.i(Helper.TAG, folder.name + " messages added");
                                                for (Message imessage : e.getMessages())
                                                    try {
                                                        synchronizeMessage(ServiceSynchronize.this, folder, ifolder, (IMAPMessage) imessage, false);
                                                    } catch (MessageRemovedException ex) {
                                                        Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                                    } catch (IOException ex) {
                                                        if (ex.getCause() instanceof MessageRemovedException)
                                                            Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                                        else
                                                            throw ex;
                                                    }
                                                EntityOperation.process(ServiceSynchronize.this); // download small attachments
                                            } catch (Throwable ex) {
                                                Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                                reportError(account.name, folder.name, ex);

                                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));

                                                synchronized (state) {
                                                    state.notifyAll();
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void messagesRemoved(MessageCountEvent e) {
                                        synchronized (lock) {
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

                                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));

                                                synchronized (state) {
                                                    state.notifyAll();
                                                }
                                            }
                                        }
                                    }
                                });

                                // Fetch e-mail
                                synchronizeMessages(account, folder, ifolder, state);

                                // Flags (like "seen") at the remote could be changed while synchronizing

                                // Listen for changed messages
                                ifolder.addMessageChangedListener(new MessageChangedListener() {
                                    @Override
                                    public void messageChanged(MessageChangedEvent e) {
                                        synchronized (lock) {
                                            try {
                                                try {
                                                    Log.i(Helper.TAG, folder.name + " message changed");
                                                    synchronizeMessage(ServiceSynchronize.this, folder, ifolder, (IMAPMessage) e.getMessage(), false);
                                                    EntityOperation.process(ServiceSynchronize.this); // download small attachments
                                                } catch (MessageRemovedException ex) {
                                                    Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                                } catch (IOException ex) {
                                                    if (ex.getCause() instanceof MessageRemovedException)
                                                        Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                                    else
                                                        throw ex;
                                                }
                                            } catch (Throwable ex) {
                                                Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                                reportError(account.name, folder.name, ex);

                                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));

                                                synchronized (state) {
                                                    state.notifyAll();
                                                }
                                            }
                                        }
                                    }
                                });

                                Log.i(Helper.TAG, folder.name + " start noop");
                                while (state.running && ifolder.isOpen()) {
                                    try {
                                        Thread.sleep(account.poll_interval * 60 * 1000L);

                                        Log.i(Helper.TAG, folder.name + " request NOOP");
                                        ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                                            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                                                Log.i(Helper.TAG, ifolder.getName() + " start NOOP");
                                                p.simpleCommand("NOOP", null);
                                                Log.i(Helper.TAG, ifolder.getName() + " end NOOP");
                                                return null;
                                            }
                                        });

                                    } catch (InterruptedException ex) {
                                        Log.w(Helper.TAG, folder.name + " noop " + ex.toString());
                                    }
                                }
                            } catch (Throwable ex) {
                                Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                reportError(account.name, folder.name, ex);

                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));

                                synchronized (state) {
                                    state.notifyAll();
                                }
                            } finally {
                                Log.i(Helper.TAG, folder.name + " end noop");
                            }
                        }
                    }, "sync.noop." + folder.id);
                    noop.start();
                    noops.add(noop);

                    // Receive folder events
                    Thread idle = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.i(Helper.TAG, folder.name + " start idle");
                                while (state.running && ifolder.isOpen()) {
                                    Log.i(Helper.TAG, folder.name + " do idle");
                                    ifolder.idle(false);
                                    Log.i(Helper.TAG, folder.name + " done idle");
                                }
                            } catch (Throwable ex) {
                                Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                reportError(account.name, folder.name, ex);

                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));

                                synchronized (state) {
                                    state.notifyAll();
                                }
                            } finally {
                                Log.i(Helper.TAG, folder.name + " end idle");
                            }
                        }
                    }, "sync.idle." + folder.id);
                    idle.start();
                    idlers.add(idle);
                }

                backoff = CONNECT_BACKOFF_START;

                BroadcastReceiver processFolder = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, final Intent intent) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                long fid = intent.getLongExtra("folder", -1);
                                Log.i(Helper.TAG, "Process folder=" + fid + " intent=" + intent);

                                // Get folder
                                EntityFolder folder = null;
                                IMAPFolder ifolder = null;
                                for (EntityFolder f : folders.keySet())
                                    if (f.id == fid) {
                                        folder = f;
                                        ifolder = folders.get(f);
                                        break;
                                    }

                                final boolean shouldClose = (ifolder == null);

                                try {
                                    if (folder == null)
                                        folder = db.folder().getFolder(fid);

                                    Log.i(Helper.TAG, folder.name + " run " + (shouldClose ? "offline" : "online"));

                                    if (ifolder == null) {
                                        // Prevent unnecessary folder connections
                                        if (ACTION_PROCESS_OPERATIONS.equals(intent.getAction()))
                                            if (db.operation().getOperationCount(fid) == 0)
                                                return;

                                        db.folder().setFolderState(folder.id, "connecting");

                                        ifolder = (IMAPFolder) istore.getFolder(folder.name);
                                        ifolder.open(Folder.READ_WRITE);

                                        db.folder().setFolderState(folder.id, "connected");
                                        db.folder().setFolderError(folder.id, null);
                                    }

                                    if (ACTION_PROCESS_OPERATIONS.equals(intent.getAction()))
                                        processOperations(folder, isession, istore, ifolder);
                                    else if (ACTION_SYNCHRONIZE_FOLDER.equals(intent.getAction())) {
                                        processOperations(folder, isession, istore, ifolder);
                                        synchronizeMessages(account, folder, ifolder, state);
                                    }

                                } catch (Throwable ex) {
                                    Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                    reportError(account.name, folder.name, ex);

                                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                } finally {
                                    if (shouldClose) {
                                        if (ifolder != null && ifolder.isOpen()) {
                                            db.folder().setFolderState(folder.id, "closing");
                                            try {
                                                ifolder.close(false);
                                            } catch (MessagingException ex) {
                                                Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                            }
                                        }
                                        db.folder().setFolderState(folder.id, null);
                                    }
                                }
                            }
                        });
                    }
                };

                // Listen for folder operations
                IntentFilter f = new IntentFilter();
                f.addAction(ACTION_SYNCHRONIZE_FOLDER);
                f.addAction(ACTION_PROCESS_OPERATIONS);
                f.addDataType("account/" + account.id);
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ServiceSynchronize.this);
                lbm.registerReceiver(processFolder, f);

                try {
                    // Keep store alive
                    while (state.running && istore.isConnected()) {
                        Log.i(Helper.TAG, "Checking folders");
                        for (EntityFolder folder : folders.keySet())
                            if (!folders.get(folder).isOpen())
                                throw new FolderClosedException(folders.get(folder));

                        // Wait for stop or folder error
                        Log.i(Helper.TAG, account.name + " wait");
                        synchronized (state) {
                            try {
                                state.wait(STORE_NOOP_INTERVAL);
                            } catch (InterruptedException ex) {
                                Log.w(Helper.TAG, account.name + " wait " + ex.toString());
                            }
                        }
                        Log.i(Helper.TAG, account.name + " waited");
                    }
                    Log.i(Helper.TAG, account.name + " done running=" + state.running);
                } finally {
                    lbm.unregisterReceiver(processFolder);
                }
            } catch (Throwable ex) {
                Log.e(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                reportError(account.name, null, ex);

                db.account().setAccountError(account.id, Helper.formatThrowable(ex));
            } finally {
                // Close store
                EntityLog.log(this, account.name + " closing");
                db.account().setAccountState(account.id, "closing");
                for (EntityFolder folder : folders.keySet())
                    db.folder().setFolderState(folder.id, "closing");
                try {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                EntityLog.log(ServiceSynchronize.this, account.name + " store closing");
                                istore.close();
                                EntityLog.log(ServiceSynchronize.this, account.name + " store closed");
                            } catch (Throwable ex) {
                                Log.w(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                            }
                        }
                    });
                    t.start();
                    try {
                        t.join(MessageHelper.NETWORK_TIMEOUT);
                        if (t.isAlive())
                            Log.w(Helper.TAG, account.name + " Close timeout");
                    } catch (InterruptedException ex) {
                        Log.w(Helper.TAG, account.name + " close wait " + ex.toString());
                        t.interrupt();
                    }
                } finally {
                    EntityLog.log(this, account.name + " closed");
                    db.account().setAccountState(account.id, null);
                    for (EntityFolder folder : folders.keySet())
                        db.folder().setFolderState(folder.id, null);
                }

                // Stop noop
                for (Thread noop : noops) {
                    noop.interrupt();
                    join(noop);
                }

                // Stop idle
                for (Thread idle : idlers) {
                    idle.interrupt();
                    join(idle);
                }
            }

            if (state.running) {
                try {
                    EntityLog.log(this, account.name + " backoff=" + backoff);
                    Thread.sleep(backoff * 1000L);

                    if (backoff < CONNECT_BACKOFF_MAX)
                        backoff *= 2;
                } catch (InterruptedException ex) {
                    Log.w(Helper.TAG, account.name + " backoff " + ex.toString());
                }
            }
        }

        EntityLog.log(this, account.name + " stopped");
    }

    private void processOperations(EntityFolder folder, Session isession, IMAPStore istore, IMAPFolder ifolder) throws MessagingException, JSONException, IOException {
        synchronized (lock) {
            try {
                Log.i(Helper.TAG, folder.name + " start process");

                DB db = DB.getInstance(this);
                List<EntityOperation> ops = db.operation().getOperationsByFolder(folder.id);
                Log.i(Helper.TAG, folder.name + " pending operations=" + ops.size());
                for (EntityOperation op : ops)
                    try {
                        Log.i(Helper.TAG, folder.name +
                                " start op=" + op.id + "/" + op.name +
                                " msg=" + op.message +
                                " args=" + op.args);

                        EntityMessage message = db.message().getMessage(op.message);
                        try {
                            if (message == null)
                                throw new MessageRemovedException();

                            db.message().setMessageError(message.id, null);

                            if (message.uid == null &&
                                    (EntityOperation.SEEN.equals(op.name) ||
                                            EntityOperation.DELETE.equals(op.name) ||
                                            EntityOperation.MOVE.equals(op.name) ||
                                            EntityOperation.HEADERS.equals(op.name)))
                                throw new IllegalArgumentException(op.name + " without uid");

                            JSONArray jargs = new JSONArray(op.args);

                            if (EntityOperation.SEEN.equals(op.name))
                                doSeen(folder, ifolder, message, jargs, db);

                            else if (EntityOperation.FLAG.equals(op.name))
                                doFlag(folder, ifolder, message, jargs, db);

                            else if (EntityOperation.ADD.equals(op.name))
                                doAdd(folder, isession, ifolder, message, jargs, db);

                            else if (EntityOperation.MOVE.equals(op.name))
                                doMove(folder, isession, istore, ifolder, message, jargs, db);

                            else if (EntityOperation.DELETE.equals(op.name))
                                doDelete(folder, ifolder, message, jargs, db);

                            else if (EntityOperation.SEND.equals(op.name))
                                doSend(message, db);

                            else if (EntityOperation.ATTACHMENT.equals(op.name))
                                doAttachment(folder, op, ifolder, message, jargs, db);

                            else if (EntityOperation.HEADERS.equals(op.name))
                                doHeaders(folder, ifolder, message, db);

                            else
                                throw new MessagingException("Unknown operation name=" + op.name);

                            // Operation succeeded
                            db.operation().deleteOperation(op.id);
                        } catch (Throwable ex) {
                            // TODO: SMTP response codes: https://www.ietf.org/rfc/rfc821.txt
                            if (ex instanceof SendFailedException)
                                reportError(null, folder.name, ex);

                            db.message().setMessageError(message.id, Helper.formatThrowable(ex));

                            if (ex instanceof MessageRemovedException ||
                                    ex instanceof FolderNotFoundException ||
                                    ex instanceof SendFailedException) {
                                Log.w(Helper.TAG, "Unrecoverable " + ex + "\n" + Log.getStackTraceString(ex));

                                // There is no use in repeating
                                db.operation().deleteOperation(op.id);
                                continue;
                            } else if (ex instanceof MessagingException) {
                                // Socket timeout is a recoverable condition (send message)
                                if (ex.getCause() instanceof SocketTimeoutException) {
                                    Log.w(Helper.TAG, "Recoverable " + ex + "\n" + Log.getStackTraceString(ex));
                                    // No need to inform user
                                    return;
                                }
                            }

                            throw ex;
                        }
                    } finally {
                        Log.i(Helper.TAG, folder.name + " end op=" + op.id + "/" + op.name);
                    }
            } finally {
                Log.i(Helper.TAG, folder.name + " end process");
            }
        }
    }

    private void doSeen(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, JSONException {
        // Mark message (un)seen
        boolean seen = jargs.getBoolean(0);
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        imessage.setFlag(Flags.Flag.SEEN, seen);

        db.message().setMessageSeen(message.id, seen);
    }

    private void doFlag(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, JSONException {
        // Star/unstar message
        boolean flagged = jargs.getBoolean(0);
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        imessage.setFlag(Flags.Flag.FLAGGED, flagged);

        db.message().setMessageFlagged(message.id, flagged);
    }

    private void doAdd(EntityFolder folder, Session isession, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, JSONException, IOException {
        // Append message
        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
        MimeMessage imessage = MessageHelper.from(this, message, null, attachments, isession);
        AppendUID[] uid = ifolder.appendUIDMessages(new Message[]{imessage});
        db.message().setMessageUid(message.id, uid[0].uid);
        Log.i(Helper.TAG, "Appended uid=" + uid[0].uid);

        if (message.uid != null) {
            Message iprev = ifolder.getMessageByUID(message.uid);
            if (iprev != null) {
                Log.i(Helper.TAG, "Deleting existing uid=" + message.uid);
                iprev.setFlag(Flags.Flag.DELETED, true);
                ifolder.expunge();
            }
        }
    }

    private void doMove(EntityFolder folder, Session isession, IMAPStore istore, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws JSONException, MessagingException, IOException {
        // Move message
        long id = jargs.getLong(0);
        EntityFolder target = db.folder().getFolder(id);
        if (target == null)
            throw new FolderNotFoundException();

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        if (istore.hasCapability("MOVE")) {
            Folder itarget = istore.getFolder(target.name);
            ifolder.moveMessages(new Message[]{imessage}, itarget);
        } else {
            Log.w(Helper.TAG, "MOVE by DELETE/APPEND");

            List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);

            if (!EntityFolder.ARCHIVE.equals(folder.type)) {
                imessage.setFlag(Flags.Flag.DELETED, true);
                ifolder.expunge();
            }

            MimeMessageEx icopy = MessageHelper.from(this, message, null, attachments, isession);
            Folder itarget = istore.getFolder(target.name);
            itarget.appendMessages(new Message[]{icopy});
        }
    }

    private void doDelete(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, JSONException {
        // Delete message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        imessage.setFlag(Flags.Flag.DELETED, true);
        ifolder.expunge();

        db.message().deleteMessage(message.id);
    }

    private void doSend(EntityMessage message, DB db) throws MessagingException, IOException {
        // Send message
        EntityIdentity ident = db.identity().getIdentity(message.identity);
        if (!ident.synchronize) {
            // Message will remain in outbox
            return;
        }

        // Refresh token
        if (ident.auth_type == Helper.AUTH_TYPE_GMAIL) {
            ident.password = Helper.refreshToken(this, "com.google", ident.user, ident.password);
            db.identity().setIdentityPassword(ident.id, ident.password);
        }

        // Create session
        Properties props = MessageHelper.getSessionProperties(this, ident.auth_type);
        final Session isession = Session.getInstance(props, null);

        // Create message
        MimeMessage imessage;
        EntityMessage reply = (message.replying == null ? null : db.message().getMessage(message.replying));
        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
        imessage = MessageHelper.from(this, message, reply, attachments, isession);

        if (ident.replyto != null)
            imessage.setReplyTo(new Address[]{new InternetAddress(ident.replyto)});

        // Create transport
        // TODO: cache transport?
        Transport itransport = isession.getTransport(ident.starttls ? "smtp" : "smtps");
        try {
            // Connect transport
            db.identity().setIdentityState(ident.id, "connecting");
            itransport.connect(ident.host, ident.port, ident.user, ident.password);
            db.identity().setIdentityState(ident.id, "connected");
            db.identity().setIdentityError(ident.id, null);

            // Send message
            Address[] to = imessage.getAllRecipients();
            itransport.sendMessage(imessage, to);
            Log.i(Helper.TAG, "Sent via " + ident.host + "/" + ident.user +
                    " to " + TextUtils.join(", ", to));

            try {
                db.beginTransaction();

                // Mark message as sent
                // - will be moved to sent folder by synchronize message later
                message.sent = imessage.getSentDate().getTime();
                message.seen = true;
                message.ui_seen = true;
                db.message().updateMessage(message);

                if (ident.store_sent) {
                    EntityFolder sent = db.folder().getFolderByType(ident.account, EntityFolder.SENT);
                    if (sent != null) {
                        message.folder = sent.id;
                        message.uid = null;
                        db.message().updateMessage(message);
                        Log.i(Helper.TAG, "Appending sent msgid=" + message.msgid);
                        EntityOperation.queue(db, message, EntityOperation.ADD); // Could already exist
                    }
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            EntityOperation.process(this);
        } catch (MessagingException ex) {
            db.identity().setIdentityError(ident.id, Helper.formatThrowable(ex));
            throw ex;
        } finally {
            try {
                itransport.close();
            } finally {
                db.identity().setIdentityState(ident.id, null);
            }
        }
    }

    private void doAttachment(EntityFolder folder, EntityOperation op, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws JSONException, MessagingException, IOException {
        // Download attachment
        int sequence = jargs.getInt(0);

        EntityAttachment attachment = db.attachment().getAttachment(op.message, sequence);
        if (attachment == null)
            return;

        try {
            // Get message
            Message imessage = ifolder.getMessageByUID(message.uid);
            if (imessage == null)
                throw new MessageRemovedException();

            // Get attachment
            MessageHelper helper = new MessageHelper((MimeMessage) imessage);
            EntityAttachment a = helper.getAttachments().get(sequence - 1);

            // Build filename
            File file = EntityAttachment.getFile(this, attachment.id);

            // Download attachment
            InputStream is = null;
            OutputStream os = null;
            try {
                is = a.part.getInputStream();
                os = new BufferedOutputStream(new FileOutputStream(file));

                int size = 0;
                byte[] buffer = new byte[Helper.ATTACHMENT_BUFFER_SIZE];
                for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                    size += len;
                    os.write(buffer, 0, len);

                    // Update progress
                    if (attachment.size != null)
                        db.attachment().setProgress(attachment.id, size * 100 / attachment.size);
                }

                // Store attachment data
                attachment.size = size;
                attachment.progress = null;
                attachment.available = true;
                db.attachment().updateAttachment(attachment);
            } finally {
                try {
                    if (is != null)
                        is.close();
                } finally {
                    if (os != null)
                        os.close();
                }
            }
            Log.i(Helper.TAG, folder.name + " downloaded bytes=" + attachment.size);
        } catch (Throwable ex) {
            // Reset progress on failure
            attachment.progress = null;
            db.attachment().updateAttachment(attachment);
            throw ex;
        }
    }

    private void doHeaders(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, DB db) throws MessagingException {
        Message imessage = ifolder.getMessageByUID(message.uid);
        Enumeration<Header> headers = imessage.getAllHeaders();
        StringBuilder sb = new StringBuilder();
        while (headers.hasMoreElements()) {
            Header header = headers.nextElement();
            sb.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        db.message().setMessageHeaders(message.id, sb.toString());
    }

    private void synchronizeFolders(EntityAccount account, IMAPStore istore, ServiceState state) throws MessagingException {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            Log.v(Helper.TAG, "Start sync folders");

            List<String> names = new ArrayList<>();
            for (EntityFolder folder : db.folder().getUserFolders(account.id))
                names.add(folder.name);
            Log.i(Helper.TAG, "Local folder count=" + names.size());

            Folder[] ifolders = istore.getDefaultFolder().list("*"); // TODO: is the pattern correct?
            Log.i(Helper.TAG, "Remote folder count=" + ifolders.length);

            for (Folder ifolder : ifolders) {
                String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                boolean selectable = true;
                for (String attr : attrs) {
                    if ("\\Noselect".equals(attr)) { // TODO: is this attribute correct?
                        selectable = false;
                        break;
                    }
                    if (attr.startsWith("\\"))
                        if (EntityFolder.SYSTEM_FOLDER_ATTR.contains(attr.substring(1))) {
                            selectable = false;
                            break;
                        }
                }

                if (selectable) {
                    Log.i(Helper.TAG, ifolder.getFullName() + " candidate attr=" + TextUtils.join(",", attrs));
                    EntityFolder folder = db.folder().getFolderByName(account.id, ifolder.getFullName());
                    if (folder == null) {
                        folder = new EntityFolder();
                        folder.account = account.id;
                        folder.name = ifolder.getFullName();
                        folder.type = EntityFolder.USER;
                        folder.synchronize = false;
                        folder.after = EntityFolder.DEFAULT_USER_SYNC;
                        db.folder().insertFolder(folder);
                        Log.i(Helper.TAG, folder.name + " added");
                    } else
                        names.remove(folder.name);
                }
            }

            Log.i(Helper.TAG, "Delete local folder=" + names.size());
            for (String name : names)
                db.folder().deleteFolder(account.id, name);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.v(Helper.TAG, "End sync folder");
        }
    }

    private void synchronizeMessages(EntityAccount account, EntityFolder folder, IMAPFolder ifolder, ServiceState state) throws MessagingException, IOException {
        DB db = DB.getInstance(this);
        try {
            Log.v(Helper.TAG, folder.name + " start sync after=" + folder.after);

            db.folder().setFolderState(folder.id, "syncing");

            // Get reference times
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -folder.after);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long ago = cal.getTimeInMillis();
            if (ago < 0)
                ago = 0;

            Log.i(Helper.TAG, folder.name + " ago=" + new Date(ago));

            // Delete old local messages
            int old = db.message().deleteMessagesBefore(folder.id, ago);
            Log.i(Helper.TAG, folder.name + " local old=" + old);

            // Get list of local uids
            List<Long> uids = db.message().getUids(folder.id, ago);
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

            for (Message imessage : imessages) {
                if (!state.running)
                    return;

                try {
                    uids.remove(ifolder.getUID(imessage));
                } catch (MessageRemovedException ex) {
                    Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    reportError(account.name, folder.name, ex);

                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                }
            }

            // Delete local messages not at remote
            Log.i(Helper.TAG, folder.name + " delete=" + uids.size());
            for (Long uid : uids) {
                int count = db.message().deleteMessage(folder.id, uid);
                Log.i(Helper.TAG, folder.name + " delete local uid=" + uid + " count=" + count);
            }

            // Add/update local messages
            int added = 0;
            int updated = 0;
            int unchanged = 0;
            Log.i(Helper.TAG, folder.name + " add=" + imessages.length);
            for (int i = imessages.length - 1; i >= 0; i--)
                try {
                    int status = synchronizeMessage(this, folder, ifolder, (IMAPMessage) imessages[i], false);
                    if (status > 0)
                        added++;
                    else if (status < 0)
                        updated++;
                    else
                        unchanged++;
                } catch (MessageRemovedException ex) {
                    Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                } catch (IOException ex) {
                    // Getting attachments
                    if (ex.getCause() instanceof MessageRemovedException)
                        Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    else
                        throw ex;
                }
            EntityOperation.process(this); // download small attachments

            Log.w(Helper.TAG, folder.name + " statistics added=" + added + " updated=" + updated + " unchanged=" + unchanged);
        } finally {
            Log.v(Helper.TAG, folder.name + " end sync");
            db.folder().setFolderState(folder.id, ifolder.isOpen() ? "connected" : "disconnected");
        }
    }

    static int synchronizeMessage(Context context, EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage, boolean found) throws MessagingException, IOException {
        long uid;
        try {
            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.FLAGS);
            ifolder.fetch(new Message[]{imessage}, fp);

            uid = ifolder.getUID(imessage);
            //Log.v(Helper.TAG, folder.name + " start sync uid=" + uid);

            if (imessage.isExpunged()) {
                Log.i(Helper.TAG, folder.name + " expunged uid=" + uid);
                return 0;
            }
            if (imessage.isSet(Flags.Flag.DELETED)) {
                Log.i(Helper.TAG, folder.name + " deleted uid=" + uid);
                return 0;
            }

            MessageHelper helper = new MessageHelper(imessage);
            boolean seen = helper.getSeen();
            boolean flagged = helper.getFlagged();

            DB db = DB.getInstance(context);
            try {
                int result = 0;

                db.beginTransaction();

                // Find message by uid (fast, no headers required)
                EntityMessage message = db.message().getMessageByUid(folder.id, uid);

                // Find message by Message-ID (slow, headers required)
                // - messages in inbox have same id as message sent to self
                // - messages in archive have same id as original
                if (message == null) {
                    // Will fetch headers within database transaction
                    String msgid = helper.getMessageID();
                    String[] refs = helper.getReferences();
                    String reference = (refs.length == 1 && refs[0].indexOf(BuildConfig.APPLICATION_ID) > 0 ? refs[0] : msgid);
                    Log.i(Helper.TAG, "Searching for " + msgid + " / " + reference);
                    for (EntityMessage dup : db.message().getMessageByMsgId(folder.account, msgid, reference)) {
                        EntityFolder dfolder = db.folder().getFolder(dup.folder);
                        boolean outbox = EntityFolder.OUTBOX.equals(dfolder.type);
                        Log.i(Helper.TAG, folder.name + " found as id=" + dup.id +
                                " folder=" + dfolder.type + ":" + dup.folder + "/" + folder.type + ":" + folder.id);

                        if (dup.folder.equals(folder.id) || outbox) {
                            Log.i(Helper.TAG, folder.name + " found as id=" + dup.id + " uid=" + dup.uid + " msgid=" + msgid);
                            dup.folder = folder.id;
                            dup.uid = uid;
                            if (TextUtils.isEmpty(dup.thread)) // outbox: only now the uid is known
                                dup.thread = helper.getThreadId(uid);
                            db.message().updateMessage(dup);
                            message = dup;
                            result = -1;
                        }
                    }
                }

                if (message != null) {
                    if (message.seen != seen || message.seen != message.ui_seen) {
                        message.seen = seen;
                        message.ui_seen = seen;
                        db.message().updateMessage(message);
                        Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid + " seen=" + seen);
                        result = -1;
                    }
                    if (message.flagged != flagged || message.flagged != message.ui_flagged) {
                        message.flagged = flagged;
                        message.ui_flagged = flagged;
                        db.message().updateMessage(message);
                        Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid + " flagged=" + flagged);
                        result = -1;
                    }
                    if (message.ui_hide) {
                        message.ui_hide = false;
                        db.message().updateMessage(message);
                        Log.i(Helper.TAG, folder.name + " unhidden id=" + message.id + " uid=" + message.uid);
                        result = -1;
                    }
                }

                if (message == null) {
                    // Will fetch message within database transaction
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

                    if (!EntityFolder.ARCHIVE.equals(folder.type)) {
                        message.msgid = helper.getMessageID();
                        if (TextUtils.isEmpty(message.msgid))
                            Log.w(Helper.TAG, "No Message-ID id=" + message.id + " uid=" + message.uid);
                    }

                    message.references = TextUtils.join(" ", helper.getReferences());
                    message.inreplyto = helper.getInReplyTo();
                    message.thread = helper.getThreadId(uid);
                    message.from = helper.getFrom();
                    message.to = helper.getTo();
                    message.cc = helper.getCc();
                    message.bcc = helper.getBcc();
                    message.reply = helper.getReply();
                    message.subject = imessage.getSubject();
                    message.received = imessage.getReceivedDate().getTime();
                    message.sent = (imessage.getSentDate() == null ? null : imessage.getSentDate().getTime());
                    message.seen = seen;
                    message.ui_seen = seen;
                    message.flagged = false;
                    message.ui_flagged = false;
                    message.ui_hide = false;
                    message.ui_found = found;

                    message.id = db.message().insertMessage(message);
                    message.write(context, helper.getHtml());
                    Log.i(Helper.TAG, folder.name + " added id=" + message.id + " uid=" + message.uid);

                    int sequence = 0;
                    for (EntityAttachment attachment : helper.getAttachments()) {
                        sequence++;
                        Log.i(Helper.TAG, "attachment seq=" + sequence +
                                " name=" + attachment.name + " type=" + attachment.type);
                        attachment.message = message.id;
                        attachment.sequence = sequence;
                        attachment.id = db.attachment().insertAttachment(attachment);

                        if (attachment.size != null && attachment.size < ATTACHMENT_AUTO_DOWNLOAD_SIZE)
                            EntityOperation.queue(db, message, EntityOperation.ATTACHMENT, sequence);
                    }

                    result = 1;
                }

                db.setTransactionSuccessful();
                return result;
            } finally {
                db.endTransaction();
            }
        } finally {
            //Log.v(Helper.TAG, folder.name + " end sync uid=" + uid);

            // Free memory
            imessage.invalidateHeaders();
        }
    }

    private class ServiceManager extends ConnectivityManager.NetworkCallback {
        private ServiceState state;
        private boolean running = false;
        private Thread main;
        private EntityFolder outbox = null;
        private ExecutorService lifecycle = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);
        private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

        @Override
        public void onAvailable(Network network) {
            ConnectivityManager cm = getSystemService(ConnectivityManager.class);
            NetworkInfo ni = cm.getNetworkInfo(network);
            EntityLog.log(ServiceSynchronize.this, "Network available " + network + " running=" + running + " " + ni);

            if (!running) {
                running = true;
                lifecycle.submit(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(Helper.TAG, "Starting service");
                        start();
                    }
                });
            }
        }

        @Override
        public void onLost(Network network) {
            EntityLog.log(ServiceSynchronize.this, "Network lost " + network + " running=" + running);

            if (running) {
                ConnectivityManager cm = getSystemService(ConnectivityManager.class);
                NetworkInfo ani = cm.getActiveNetworkInfo();
                EntityLog.log(ServiceSynchronize.this, "Network active=" + (ani == null ? null : ani.toString()));
                if (ani == null || !ani.isConnected()) {
                    EntityLog.log(ServiceSynchronize.this, "Network disconnected=" + ani);
                    running = false;
                    lifecycle.submit(new Runnable() {
                        @Override
                        public void run() {
                            stop();
                        }
                    });
                }
            }
        }

        private void start() {
            EntityLog.log(ServiceSynchronize.this, "Main start");

            state = new ServiceState();

            main = new Thread(new Runnable() {
                private List<Thread> threads = new ArrayList<>();

                @Override
                public void run() {
                    try {
                        DB db = DB.getInstance(ServiceSynchronize.this);

                        outbox = db.folder().getOutbox();
                        if (outbox == null) {
                            EntityLog.log(ServiceSynchronize.this, "No outbox, halt");
                            stopSelf();
                            return;
                        }

                        List<EntityAccount> accounts = db.account().getAccounts(true);
                        if (accounts.size() == 0) {
                            EntityLog.log(ServiceSynchronize.this, "No accounts, halt");
                            stopSelf();
                            return;
                        }

                        // Start monitoring outbox
                        IntentFilter f = new IntentFilter();
                        f.addAction(ACTION_SYNCHRONIZE_FOLDER);
                        f.addAction(ACTION_PROCESS_OPERATIONS);
                        f.addDataType("account/outbox");
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ServiceSynchronize.this);
                        lbm.registerReceiver(outboxReceiver, f);
                        db.folder().setFolderState(outbox.id, "connected");

                        lbm.sendBroadcast(new Intent(ACTION_PROCESS_OPERATIONS)
                                .setType("account/outbox")
                                .putExtra("folder", outbox.id));

                        // Start monitoring accounts
                        for (final EntityAccount account : accounts) {
                            Log.i(Helper.TAG, account.host + "/" + account.user + " run");
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        monitorAccount(account, state);
                                    } catch (Throwable ex) {
                                        // Fall-safe
                                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                    }
                                }
                            }, "sync.account." + account.id);
                            t.start();
                            threads.add(t);
                        }

                        EntityLog.log(ServiceSynchronize.this, "Main started");

                        synchronized (state) {
                            try {
                                if (state.running)
                                    state.wait();
                            } catch (InterruptedException ex) {
                                Log.w(Helper.TAG, "main wait " + ex.toString());
                            }
                        }

                        // Stop monitoring accounts
                        for (Thread t : threads) {
                            t.interrupt();
                            join(t);
                        }
                        threads.clear();

                        // Stop monitoring outbox
                        lbm.unregisterReceiver(outboxReceiver);
                        Log.i(Helper.TAG, outbox.name + " unlisten operations");
                        db.folder().setFolderState(outbox.id, null);

                        EntityLog.log(ServiceSynchronize.this, "Main exited");
                    } catch (Throwable ex) {
                        // Fail-safe
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    }
                }
            }, "sync.main");
            main.setPriority(THREAD_PRIORITY_BACKGROUND); // will be inherited
            main.start();
        }

        private void stop() {
            EntityLog.log(ServiceSynchronize.this, "Main stop");

            synchronized (state) {
                state.running = false;
                state.notifyAll();
            }

            // stop wait or backoff
            main.interrupt();
            join(main);

            EntityLog.log(ServiceSynchronize.this, "Main stopped");

            main = null;
            state = null;
        }

        private void restart() {
            if (running)
                lifecycle.submit(new Runnable() {
                    @Override
                    public void run() {
                        stop();
                        start();
                    }
                });
        }

        private BroadcastReceiver outboxReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                Log.v(Helper.TAG, outbox.name + " run operations");

                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        DB db = DB.getInstance(context);
                        try {
                            Log.i(Helper.TAG, outbox.name + " start operations");
                            db.folder().setFolderState(outbox.id, "syncing");
                            processOperations(outbox, null, null, null);
                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, outbox.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                            reportError(null, outbox.name, ex);

                            db.folder().setFolderError(outbox.id, Helper.formatThrowable(ex));
                        } finally {
                            Log.i(Helper.TAG, outbox.name + " end operations");
                            db.folder().setFolderState(outbox.id, null);
                        }
                    }
                });
            }
        };
    }

    private void join(Thread thread) {
        boolean joined = false;
        while (!joined)
            try {
                Log.i(Helper.TAG, "Joining " + thread.getName());
                thread.join();
                joined = true;
                Log.i(Helper.TAG, "Joined " + thread.getName());
            } catch (InterruptedException ex) {
                Log.w(Helper.TAG, thread.getName() + " join " + ex.toString());
            }
    }

    public static void start(Context context) {
        ContextCompat.startForegroundService(context, new Intent(context, ServiceSynchronize.class));
        JobDaily.schedule(context);
    }

    public static void reload(Context context, String reason) {
        Log.i(Helper.TAG, "Reload because of '" + reason + "'");
        context.startService(new Intent(context, ServiceSynchronize.class).setAction("reload"));
    }

    private class ServiceState {
        boolean running = true;
    }
}
