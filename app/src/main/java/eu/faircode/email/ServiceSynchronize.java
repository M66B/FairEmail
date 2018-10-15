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

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MailConnectException;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final int CONNECT_BACKOFF_START = 8; // seconds
    private static final int CONNECT_BACKOFF_MAX = 1024; // seconds (1024 sec ~ 17 min)
    private static final int SYNC_BATCH_SIZE = 20;
    private static final int DOWNLOAD_BATCH_SIZE = 20;
    private static final int MESSAGE_AUTO_DOWNLOAD_SIZE = 32 * 1024; // bytes
    private static final int ATTACHMENT_AUTO_DOWNLOAD_SIZE = 32 * 1024; // bytes
    private static final long RECONNECT_BACKOFF = 90 * 1000L; // milliseconds

    static final int PI_UNSEEN = 1;
    static final int PI_SEEN = 2;
    static final int PI_TRASH = 3;

    static final String ACTION_SYNCHRONIZE_FOLDER = BuildConfig.APPLICATION_ID + ".SYNCHRONIZE_FOLDER";
    static final String ACTION_PROCESS_OPERATIONS = BuildConfig.APPLICATION_ID + ".PROCESS_OPERATIONS";

    @Override
    public void onCreate() {
        Log.i(Helper.TAG, "Service create version=" + BuildConfig.VERSION_NAME);
        super.onCreate();

        // Listen for network changes
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        // Removed because of Android VPN service
        // builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        cm.registerNetworkCallback(builder.build(), serviceManager);
    }

    @Override
    public void onDestroy() {
        Log.i(Helper.TAG, "Service destroy");

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(serviceManager);

        serviceManager.onLost(null);

        stopForeground(true);

        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.cancel(NOTIFICATION_SYNCHRONIZE);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent == null ? null : intent.getAction());
        Log.i(Helper.TAG, "Service command intent=" + intent + " action=" + action);
        super.onStartCommand(intent, flags, startId);

        startForeground(NOTIFICATION_SYNCHRONIZE, getNotificationService(0, 0, 0).build());

        DB db = DB.getInstance(this);

        db.account().liveStats().removeObservers(this);
        db.account().liveStats().observe(this, new Observer<TupleAccountStats>() {
            @Override
            public void onChanged(@Nullable TupleAccountStats stats) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                nm.notify(NOTIFICATION_SYNCHRONIZE,
                        getNotificationService(stats.accounts, stats.operations, stats.unsent).build());
            }
        });

        db.message().liveUnseenUnified().removeObservers(this);
        db.message().liveUnseenUnified().observe(this, new Observer<List<EntityMessage>>() {
            private List<Integer> notifying = new ArrayList<>();

            @Override
            public void onChanged(List<EntityMessage> messages) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                List<Notification> notifications = getNotificationUnseen(messages);

                List<Integer> all = new ArrayList<>();
                List<Integer> added = new ArrayList<>();
                List<Integer> removed = new ArrayList<>(notifying);
                for (Notification notification : notifications) {
                    Integer id = (int) notification.extras.getLong("id", 0);
                    if (id > 0) {
                        all.add(id);
                        if (removed.contains(id))
                            removed.remove(id);
                        else
                            added.add(id);
                    }
                }

                if (notifications.size() == 0)
                    nm.cancel("unseen", 0);

                for (Integer id : removed)
                    nm.cancel("unseen", id);

                for (Notification notification : notifications) {
                    Integer id = (int) notification.extras.getLong("id", 0);
                    if ((id == 0 && added.size() + removed.size() > 0) || added.contains(id))
                        nm.notify("unseen", id, notification);
                }

                notifying = all;
            }
        });

        if (action != null) {
            if ("start".equals(action))
                serviceManager.queue_start();
            else if ("stop".equals(action))
                serviceManager.queue_stop();
            else if ("reload".equals(action))
                serviceManager.queue_reload();
            else if (action.startsWith("seen:") || action.startsWith("trash:")) {
                Bundle args = new Bundle();
                args.putLong("id", Long.parseLong(action.split(":")[1]));
                args.putString("action", action.split(":")[0]);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) {
                        long id = args.getLong("id");
                        String action = args.getString("action");

                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            EntityMessage message = db.message().getMessage(id);
                            if ("seen".equals(action)) {
                                db.message().setMessageUiSeen(message.id, true);
                                EntityOperation.queue(db, message, EntityOperation.SEEN, true);
                            } else if ("trash".equals(action)) {
                                db.message().setMessageUiHide(message.id, true);
                                EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                                if (trash != null)
                                    EntityOperation.queue(db, message, EntityOperation.MOVE, trash.id);
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        EntityOperation.process(context);

                        return null;
                    }

                    @Override
                    protected void onLoaded(Bundle args, Void data) {
                        Log.i(Helper.TAG, "Set seen");
                    }
                }.load(this, args);
            }
        }

        return START_STICKY;
    }

    private Notification.Builder getNotificationService(int accounts, int operations, int unsent) {
        // Build pending intent
        Intent intent = new Intent(this, ActivityView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                this, ActivityView.REQUEST_UNIFIED, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this, "service");
        else
            builder = new Notification.Builder(this);

        builder
                .setSmallIcon(R.drawable.baseline_compare_arrows_white_24)
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

    private List<Notification> getNotificationUnseen(List<EntityMessage> messages) {
        // https://developer.android.com/training/notify-user/group
        List<Notification> notifications = new ArrayList<>();

        if (messages.size() == 0)
            return notifications;

        boolean pro = Helper.isPro(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Build pending intent
        Intent view = new Intent(this, ActivityView.class);
        view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piView = PendingIntent.getActivity(
                this, ActivityView.REQUEST_UNIFIED, view, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder = new Notification.Builder(this);
        else
            builder = new Notification.Builder(this, "notification");

        builder
                .setSmallIcon(R.drawable.baseline_email_white_24)
                .setContentTitle(getResources().getQuantityString(R.plurals.title_notification_unseen, messages.size(), messages.size()))
                .setContentText("")
                .setContentIntent(piView)
                .setNumber(messages.size())
                .setShowWhen(false)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setGroup(BuildConfig.APPLICATION_ID)
                .setGroupSummary(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setSound(null);
        else
            builder.setGroupAlertBehavior(Notification.GROUP_ALERT_CHILDREN);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O &&
                prefs.getBoolean("light", false)) {
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
            builder.setLights(0xff00ff00, 1000, 1000);
        }

        if (pro) {
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

        notifications.add(builder.build());

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        for (EntityMessage message : messages) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            Intent thread = new Intent(this, ActivityView.class);
            thread.setAction("thread:" + message.id);
            thread.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent piThread = PendingIntent.getActivity(
                    this, ActivityView.REQUEST_THREAD, thread, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent seen = new Intent(this, ServiceSynchronize.class);
            seen.setAction("seen:" + message.id);
            PendingIntent piSeen = PendingIntent.getService(this, PI_SEEN, seen, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent trash = new Intent(this, ServiceSynchronize.class);
            trash.setAction("trash:" + message.id);
            PendingIntent piTrash = PendingIntent.getService(this, PI_TRASH, trash, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Action.Builder actionSeen = new Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.baseline_visibility_24),
                    getString(R.string.title_seen),
                    piSeen);

            Notification.Action.Builder actionTrash = new Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.baseline_delete_24),
                    getString(R.string.title_trash),
                    piTrash);

            Notification.Builder mbuilder;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                mbuilder = new Notification.Builder(this);
            else
                mbuilder = new Notification.Builder(this, "notification");

            mbuilder
                    .addExtras(args)
                    .setSmallIcon(R.drawable.baseline_mail_24)
                    .setContentTitle(MessageHelper.getFormattedAddresses(message.from, true))
                    .setContentIntent(piThread)
                    .setSound(uri)
                    .setWhen(message.sent == null ? message.received : message.sent)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setVisibility(Notification.VISIBILITY_PRIVATE)
                    .setGroup(BuildConfig.APPLICATION_ID)
                    .setGroupSummary(false)
                    .addAction(actionSeen.build())
                    .addAction(actionTrash.build());

            if (pro)
                if (!TextUtils.isEmpty(message.subject))
                    mbuilder.setContentText(message.subject);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                mbuilder.setGroupAlertBehavior(Notification.GROUP_ALERT_CHILDREN);

            notifications.add(mbuilder.build());
        }

        return notifications;
    }

    private Notification.Builder getNotificationError(String action, Throwable ex) {
        // Build pending intent
        Intent intent = new Intent(this, ActivityView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                this, ActivityView.REQUEST_ERROR, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
        final PowerManager pm = getSystemService(PowerManager.class);
        final PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                BuildConfig.APPLICATION_ID + ":account." + account.id);
        try {
            wl.acquire();

            final DB db = DB.getInstance(this);
            final ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

            int backoff = CONNECT_BACKOFF_START;
            while (state.running) {
                EntityLog.log(this, account.name + " run");

                // Debug
                boolean debug = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("debug", false);
                debug = debug || BuildConfig.DEBUG;
                System.setProperty("mail.socket.debug", Boolean.toString(debug));

                // Create session
                Properties props = MessageHelper.getSessionProperties(account.auth_type);
                final Session isession = Session.getInstance(props, null);
                isession.setDebug(debug);
                // adb -t 1 logcat | grep "fairemail\|System.out"

                final IMAPStore istore = (IMAPStore) isession.getStore("imaps");
                final Map<EntityFolder, IMAPFolder> folders = new HashMap<>();
                List<Thread> syncs = new ArrayList<>();
                List<Thread> idlers = new ArrayList<>();
                try {
                    // Listen for store events
                    istore.addStoreListener(new StoreListener() {
                        @Override
                        public void notification(StoreEvent e) {
                            try {
                                wl.acquire();
                                Log.i(Helper.TAG, account.name + " event: " + e.getMessage());
                                db.account().setAccountError(account.id, e.getMessage());
                                state.thread.interrupt();
                                yieldWakelock();
                            } finally {
                                wl.release();
                            }
                        }
                    });

                    // Listen for folder events
                    istore.addFolderListener(new FolderAdapter() {
                        @Override
                        public void folderCreated(FolderEvent e) {
                            try {
                                wl.acquire();
                                Log.i(Helper.TAG, "Folder created=" + e.getFolder().getFullName());
                                state.thread.interrupt();
                                yieldWakelock();
                            } finally {
                                wl.release();
                            }
                        }

                        @Override
                        public void folderRenamed(FolderEvent e) {
                            try {
                                wl.acquire();
                                Log.i(Helper.TAG, "Folder renamed=" + e.getFolder());

                                String old = e.getFolder().getFullName();
                                String name = e.getNewFolder().getFullName();
                                int count = db.folder().renameFolder(account.id, old, name);
                                Log.i(Helper.TAG, "Renamed to " + name + " count=" + count);

                                state.thread.interrupt();
                                yieldWakelock();
                            } finally {
                                wl.release();
                            }
                        }

                        @Override
                        public void folderDeleted(FolderEvent e) {
                            try {
                                wl.acquire();
                                Log.i(Helper.TAG, "Folder deleted=" + e.getFolder().getFullName());
                                state.thread.interrupt();
                                yieldWakelock();
                            } finally {
                                wl.release();
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
                    Helper.connect(this, istore, account);
                    final boolean capIdle = istore.hasCapability("IDLE");
                    Log.i(Helper.TAG, account.name + " idle=" + capIdle);
                    db.account().setAccountState(account.id, "connected");
                    db.account().setAccountError(account.id, null);

                    EntityLog.log(this, account.name + " connected");

                    // Update folder list
                    synchronizeFolders(account, istore, state);

                    // Open folders
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

                        // Synchronize folder
                        Thread sync = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    wl.acquire();

                                    // Process pending operations
                                    processOperations(folder, isession, istore, ifolder);

                                    // Listen for new and deleted messages
                                    ifolder.addMessageCountListener(new MessageCountAdapter() {
                                        @Override
                                        public void messagesAdded(MessageCountEvent e) {
                                            synchronized (lock) {
                                                try {
                                                    wl.acquire();
                                                    Log.i(Helper.TAG, folder.name + " messages added");

                                                    FetchProfile fp = new FetchProfile();
                                                    fp.add(FetchProfile.Item.ENVELOPE);
                                                    fp.add(FetchProfile.Item.FLAGS);
                                                    fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
                                                    fp.add(UIDFolder.FetchProfileItem.UID);
                                                    fp.add(IMAPFolder.FetchProfileItem.HEADERS);
                                                    fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
                                                    fp.add(FetchProfile.Item.SIZE);
                                                    fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
                                                    ifolder.fetch(e.getMessages(), fp);

                                                    for (Message imessage : e.getMessages())
                                                        try {
                                                            long id;
                                                            try {
                                                                db.beginTransaction();
                                                                id = synchronizeMessage(ServiceSynchronize.this, folder, ifolder, (IMAPMessage) imessage, false);
                                                                db.setTransactionSuccessful();
                                                            } finally {
                                                                db.endTransaction();
                                                            }
                                                            downloadMessage(ServiceSynchronize.this, folder, ifolder, (IMAPMessage) imessage, id);
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

                                                    state.thread.interrupt();
                                                    yieldWakelock();
                                                } finally {
                                                    wl.release();
                                                }
                                            }
                                        }

                                        @Override
                                        public void messagesRemoved(MessageCountEvent e) {
                                            synchronized (lock) {
                                                try {
                                                    wl.acquire();
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

                                                    state.thread.interrupt();
                                                } finally {
                                                    wl.release();
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
                                                    wl.acquire();
                                                    try {
                                                        Log.i(Helper.TAG, folder.name + " message changed");

                                                        FetchProfile fp = new FetchProfile();
                                                        fp.add(UIDFolder.FetchProfileItem.UID);
                                                        fp.add(IMAPFolder.FetchProfileItem.FLAGS);
                                                        ifolder.fetch(new Message[]{e.getMessage()}, fp);

                                                        long id;
                                                        try {
                                                            db.beginTransaction();
                                                            id = synchronizeMessage(ServiceSynchronize.this, folder, ifolder, (IMAPMessage) e.getMessage(), false);
                                                            db.setTransactionSuccessful();
                                                        } finally {
                                                            db.endTransaction();
                                                        }
                                                        downloadMessage(ServiceSynchronize.this, folder, ifolder, (IMAPMessage) e.getMessage(), id);
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

                                                    state.thread.interrupt();
                                                    yieldWakelock();
                                                } finally {
                                                    wl.release();
                                                }
                                            }
                                        }
                                    });
                                } catch (Throwable ex) {
                                    Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                    reportError(account.name, folder.name, ex);

                                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));

                                    state.thread.interrupt();
                                    yieldWakelock();
                                } finally {
                                    wl.release();
                                }
                            }
                        }, "sync." + folder.id);
                        sync.start();
                        syncs.add(sync);

                        // Idle folder
                        if (capIdle) {
                            Thread idler = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Log.i(Helper.TAG, folder.name + " start idle");
                                        while (state.running) {
                                            Log.i(Helper.TAG, folder.name + " do idle");
                                            ifolder.idle(false);
                                            //Log.i(Helper.TAG, folder.name + " done idle");
                                        }
                                    } catch (FolderClosedException ignored) {
                                    } catch (Throwable ex) {
                                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                        reportError(account.name, folder.name, ex);

                                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));

                                        state.thread.interrupt();
                                        yieldWakelock();
                                    } finally {
                                        Log.i(Helper.TAG, folder.name + " end idle");
                                    }
                                }
                            }, "idler." + folder.id);
                            idler.start();
                            idlers.add(idler);
                        }
                    }

                    // Successfully connected: reset back off time
                    backoff = CONNECT_BACKOFF_START;

                    // Process folder actions
                    BroadcastReceiver processFolder = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, final Intent intent) {
                            executor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    long fid = intent.getLongExtra("folder", -1);
                                    try {
                                        wl.acquire();
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

                                        final boolean shouldClose = (folder == null);

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
                                    } finally {
                                        wl.release();
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

                    // Keep alive alarm receiver
                    BroadcastReceiver alarm = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            // Receiver runs on main thread
                            // Receiver has a wake lock for ~10 seconds
                            EntityLog.log(context, account.name + " keep alive wake lock=" + wl.isHeld());
                            state.thread.interrupt();
                            yieldWakelock();
                        }
                    };

                    String id = BuildConfig.APPLICATION_ID + ".POLL." + account.id;
                    PendingIntent pi = PendingIntent.getBroadcast(ServiceSynchronize.this, 0, new Intent(id), 0);
                    registerReceiver(alarm, new IntentFilter(id));

                    // Keep alive
                    AlarmManager am = getSystemService(AlarmManager.class);
                    try {
                        while (state.running) {
                            // Schedule keep alive alarm
                            EntityLog.log(this, account.name + " wait=" + account.poll_interval);
                            am.setAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    System.currentTimeMillis() + account.poll_interval * 60 * 1000L,
                                    pi);

                            try {
                                wl.release();
                                Thread.sleep(Long.MAX_VALUE);
                            } catch (InterruptedException ex) {
                                EntityLog.log(this, account.name + " waited running=" + state.running);
                            } finally {
                                wl.acquire();
                            }

                            if (state.running) {
                                if (!istore.isConnected())
                                    throw new StoreClosedException(istore);

                                for (EntityFolder folder : folders.keySet())
                                    if (capIdle) {
                                        if (!folders.get(folder).isOpen())
                                            throw new FolderClosedException(folders.get(folder));
                                    } else
                                        synchronizeMessages(account, folder, folders.get(folder), state);
                            }

                        }
                    } finally {
                        // Cleanup
                        am.cancel(pi);
                        unregisterReceiver(alarm);
                        lbm.unregisterReceiver(processFolder);
                    }

                    Log.i(Helper.TAG, account.name + " done running=" + state.running);
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    reportError(account.name, null, ex);

                    db.account().setAccountError(account.id, Helper.formatThrowable(ex));
                } finally {
                    EntityLog.log(this, account.name + " closing");
                    db.account().setAccountState(account.id, "closing");
                    for (EntityFolder folder : folders.keySet())
                        db.folder().setFolderState(folder.id, "closing");

                    // Stop syncs
                    for (Thread sync : syncs) {
                        sync.interrupt();
                        join(sync);
                    }

                    // Close store
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

                    // Stop idlers
                    for (Thread idler : idlers) {
                        idler.interrupt();
                        join(idler);
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
        } finally {
            EntityLog.log(this, account.name + " stopped");
            wl.release();
        }
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

                            else if (EntityOperation.HEADERS.equals(op.name))
                                doHeaders(folder, ifolder, message, db);

                            else if (EntityOperation.BODY.equals(op.name))
                                doBody(folder, ifolder, message, db);

                            else if (EntityOperation.ATTACHMENT.equals(op.name))
                                doAttachment(folder, op, ifolder, message, jargs, db);

                            else
                                throw new MessagingException("Unknown operation name=" + op.name);

                            // Operation succeeded
                            db.operation().deleteOperation(op.id);
                        } catch (Throwable ex) {
                            // TODO: SMTP response codes: https://www.ietf.org/rfc/rfc821.txt
                            if (ex instanceof SendFailedException)
                                reportError(null, folder.name, ex);

                            if (message != null)
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
        if (message.seen == seen)
            return;

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

        // Create session
        Properties props = MessageHelper.getSessionProperties(ident.auth_type);
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

    private void doHeaders(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, DB db) throws MessagingException {
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        Enumeration<Header> headers = imessage.getAllHeaders();
        StringBuilder sb = new StringBuilder();
        while (headers.hasMoreElements()) {
            Header header = headers.nextElement();
            sb.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        db.message().setMessageHeaders(message.id, sb.toString());
    }

    private void doBody(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, DB db) throws MessagingException, IOException {
        // Download message body
        if (message.content)
            return;

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        MessageHelper helper = new MessageHelper((MimeMessage) imessage);
        message.write(this, helper.getHtml());
        db.message().setMessageContent(message.id, true);
    }

    private void doAttachment(EntityFolder folder, EntityOperation op, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws JSONException, MessagingException, IOException {
        // Download attachment
        int sequence = jargs.getInt(0);

        // Get attachment
        EntityAttachment attachment = db.attachment().getAttachment(op.message, sequence);
        if (attachment.available)
            return;

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        // Download attachment
        MessageHelper helper = new MessageHelper((MimeMessage) imessage);
        EntityAttachment a = helper.getAttachments().get(sequence - 1);
        attachment.part = a.part;
        attachment.download(this, db);
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
                boolean system = false;
                boolean selectable = true;
                for (String attr : attrs) {
                    if ("\\Noselect".equals(attr)) { // TODO: is this attribute correct?
                        selectable = false;
                        break;
                    }
                    if (attr.startsWith("\\")) {
                        attr = attr.substring(1);
                        if (EntityFolder.SYSTEM_FOLDER_ATTR.contains(attr)) {
                            int index = EntityFolder.SYSTEM_FOLDER_ATTR.indexOf(attr);
                            system = EntityFolder.SYSTEM.equals(EntityFolder.SYSTEM_FOLDER_TYPE.get(index));
                            if (!system)
                                selectable = false;
                            break;
                        }
                    }
                }

                if (selectable) {
                    Log.i(Helper.TAG, ifolder.getFullName() + " candidate attr=" + TextUtils.join(",", attrs));
                    EntityFolder folder = db.folder().getFolderByName(account.id, ifolder.getFullName());
                    if (folder == null) {
                        folder = new EntityFolder();
                        folder.account = account.id;
                        folder.name = ifolder.getFullName();
                        folder.type = (system ? EntityFolder.SYSTEM : EntityFolder.USER);
                        folder.synchronize = false;
                        folder.after = EntityFolder.DEFAULT_USER_SYNC;
                        db.folder().insertFolder(folder);
                        Log.i(Helper.TAG, folder.name + " added");
                    } else {
                        if (system)
                            db.folder().setFolderType(folder.id, EntityFolder.SYSTEM);
                        names.remove(folder.name);
                    }
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
            fp.add(FetchProfile.Item.FLAGS);
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

            fp.add(FetchProfile.Item.ENVELOPE);
            // fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
            // fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.HEADERS);
            // fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
            fp.add(FetchProfile.Item.SIZE);
            fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);

            // Add/update local messages
            Long[] ids = new Long[imessages.length];
            Log.i(Helper.TAG, folder.name + " add=" + imessages.length);
            for (int i = imessages.length - 1; i >= 0; i -= SYNC_BATCH_SIZE) {
                int from = Math.max(0, i - SYNC_BATCH_SIZE + 1);
                //Log.i(Helper.TAG, folder.name + " update " + from + " .. " + i);

                Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);

                // Full fetch new/changed messages only
                List<Message> full = new ArrayList<>();
                for (Message imessage : isub) {
                    long uid = ifolder.getUID(imessage);
                    EntityMessage message = db.message().getMessageByUid(folder.id, uid);
                    if (message == null)
                        full.add(imessage);
                }
                if (full.size() > 0) {
                    long headers = SystemClock.elapsedRealtime();
                    ifolder.fetch(full.toArray(new Message[0]), fp);
                    Log.i(Helper.TAG, folder.name + " fetched headers=" + full.size() +
                            " " + (SystemClock.elapsedRealtime() - headers) + " ms");
                }

                for (int j = isub.length - 1; j >= 0; j--)
                    try {
                        db.beginTransaction();
                        ids[from + j] = synchronizeMessage(this, folder, ifolder, (IMAPMessage) isub[j], false);
                        db.setTransactionSuccessful();
                        Thread.sleep(20);
                    } catch (MessageRemovedException ex) {
                        Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (FolderClosedIOException ex) {
                        throw ex;
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    } finally {
                        db.endTransaction();
                        // Reduce memory usage
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }

            db.folder().setFolderState(folder.id, "downloading");

            //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);

            // Download messages/attachments
            Log.i(Helper.TAG, folder.name + " download=" + imessages.length);
            for (int i = imessages.length - 1; i >= 0; i -= DOWNLOAD_BATCH_SIZE) {
                int from = Math.max(0, i - DOWNLOAD_BATCH_SIZE + 1);
                //Log.i(Helper.TAG, folder.name + " download " + from + " .. " + i);

                Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);
                // Fetch on demand

                for (int j = isub.length - 1; j >= 0; j--)
                    try {
                        //Log.i(Helper.TAG, folder.name + " download index=" + (from + j) + " id=" + ids[from + j]);
                        if (ids[from + j] != null) {
                            downloadMessage(this, folder, ifolder, (IMAPMessage) isub[j], ids[from + j]);
                            Thread.sleep(20);
                        }
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (FolderClosedIOException ex) {
                        throw ex;
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    } finally {
                        // Free memory
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }

        } finally {
            Log.v(Helper.TAG, folder.name + " end sync");
            db.folder().setFolderState(folder.id, ifolder.isOpen() ? "connected" : "disconnected");
        }
    }

    static Long synchronizeMessage(Context context, EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage, boolean found) throws MessagingException, IOException {
        long uid = ifolder.getUID(imessage);

        if (imessage.isExpunged()) {
            Log.i(Helper.TAG, folder.name + " expunged uid=" + uid);
            throw new MessageRemovedException();
        }
        if (imessage.isSet(Flags.Flag.DELETED)) {
            Log.i(Helper.TAG, folder.name + " deleted uid=" + uid);
            throw new MessageRemovedException();
        }

        MessageHelper helper = new MessageHelper(imessage);
        boolean seen = helper.getSeen();
        boolean flagged = helper.getFlagged();

        DB db = DB.getInstance(context);

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
                }
            }
        }

        if (message == null) {
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
            message.deliveredto = helper.getDeliveredTo();
            message.thread = helper.getThreadId(uid);
            message.from = helper.getFrom();
            message.to = helper.getTo();
            message.cc = helper.getCc();
            message.bcc = helper.getBcc();
            message.reply = helper.getReply();
            message.subject = imessage.getSubject();
            message.size = helper.getSize();
            message.content = false;
            message.received = imessage.getReceivedDate().getTime();
            message.sent = (imessage.getSentDate() == null ? null : imessage.getSentDate().getTime());
            message.seen = seen;
            message.ui_seen = seen;
            message.flagged = false;
            message.ui_flagged = false;
            message.ui_hide = false;
            message.ui_found = found;

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    if (message.from != null)
                        for (int i = 0; i < message.from.length; i++) {
                            String email = ((InternetAddress) message.from[i]).getAddress();
                            Cursor cursor = null;
                            try {
                                ContentResolver resolver = context.getContentResolver();
                                cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                        new String[]{
                                                ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                                                ContactsContract.Contacts.DISPLAY_NAME
                                        },
                                        ContactsContract.CommonDataKinds.Email.ADDRESS + " = ?",
                                        new String[]{email}, null);
                                if (cursor.moveToNext()) {
                                    int colContactId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID);
                                    int colDisplayName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                                    long contactId = cursor.getLong(colContactId);
                                    String displayName = cursor.getString(colDisplayName);

                                    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                                    message.avatar = uri.toString();

                                    if (!TextUtils.isEmpty(displayName))
                                        ((InternetAddress) message.from[i]).setPersonal(displayName);
                                }
                            } finally {
                                if (cursor != null)
                                    cursor.close();
                            }
                        }
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                }
            }

            message.id = db.message().insertMessage(message);

            Log.i(Helper.TAG, folder.name + " added id=" + message.id + " uid=" + message.uid);

            int sequence = 1;
            for (EntityAttachment attachment : helper.getAttachments()) {
                Log.i(Helper.TAG, folder.name + " attachment seq=" + sequence +
                        " name=" + attachment.name + " type=" + attachment.type + " cid=" + attachment.cid);
                if (!TextUtils.isEmpty(attachment.cid) &&
                        db.attachment().getAttachment(message.id, attachment.cid) != null) {
                    Log.i(Helper.TAG, "Skipping duplicated CID");
                    continue;
                }
                attachment.message = message.id;
                attachment.sequence = sequence++;
                attachment.id = db.attachment().insertAttachment(attachment);
            }
        } else {
            if (message.seen != seen || message.seen != message.ui_seen) {
                message.seen = seen;
                message.ui_seen = seen;
                db.message().updateMessage(message);
                Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid + " seen=" + seen);
            }

            if (message.flagged != flagged || message.flagged != message.ui_flagged) {
                message.flagged = flagged;
                message.ui_flagged = flagged;
                db.message().updateMessage(message);
                Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid + " flagged=" + flagged);
            }

            if (message.ui_hide) {
                message.ui_hide = false;
                db.message().updateMessage(message);
                Log.i(Helper.TAG, folder.name + " unhidden id=" + message.id + " uid=" + message.uid);
            }
        }

        return message.id;
    }

    private static void downloadMessage(Context context, EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage, long id) throws MessagingException, IOException {
        DB db = DB.getInstance(context);
        EntityMessage message = db.message().getMessage(id);
        if (message == null)
            return;
        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
        MessageHelper helper = new MessageHelper(imessage);

        ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        boolean metered = (cm == null || cm.isActiveNetworkMetered());

        boolean fetch = false;
        if (!message.content)
            if (!metered || (message.size != null && message.size < MESSAGE_AUTO_DOWNLOAD_SIZE))
                fetch = true;

        if (!fetch)
            for (EntityAttachment attachment : attachments)
                if (!attachment.available)
                    if (!metered || (attachment.size != null && attachment.size < ATTACHMENT_AUTO_DOWNLOAD_SIZE)) {
                        fetch = true;
                        break;
                    }

        if (fetch) {
            Log.i(Helper.TAG, folder.name + " fetching message id=" + message.id);
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.HEADERS);
            fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
            fp.add(FetchProfile.Item.SIZE);
            fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
            ifolder.fetch(new Message[]{imessage}, fp);
        }

        if (!message.content)
            if (!metered || (message.size != null && message.size < MESSAGE_AUTO_DOWNLOAD_SIZE)) {
                message.write(context, helper.getHtml());
                db.message().setMessageContent(message.id, true);
                Log.i(Helper.TAG, folder.name + " downloaded message id=" + message.id + " size=" + message.size);
            }

        List<EntityAttachment> iattachments = null;
        for (int i = 0; i < attachments.size(); i++) {
            EntityAttachment attachment = attachments.get(i);
            if (!attachment.available)
                if (!metered || (attachment.size != null && attachment.size < ATTACHMENT_AUTO_DOWNLOAD_SIZE)) {
                    if (iattachments == null)
                        iattachments = helper.getAttachments();
                    attachment.part = iattachments.get(i).part;
                    attachment.download(context, db);
                    Log.i(Helper.TAG, folder.name + " downloaded message id=" + message.id + " attachment=" + attachment.name + " size=" + message.size);
                }
        }
    }

    private class ServiceManager extends ConnectivityManager.NetworkCallback {
        private ServiceState state;
        private boolean running = false;
        private long lastLost = 0;
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
                NetworkInfo ani = (network == null ? null : cm.getActiveNetworkInfo());
                EntityLog.log(ServiceSynchronize.this, "Network active=" + (ani == null ? null : ani.toString()));
                if (ani == null || !ani.isConnected()) {
                    EntityLog.log(ServiceSynchronize.this, "Network disconnected=" + ani);
                    running = false;
                    lastLost = new Date().getTime();
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
            state.thread = new Thread(new Runnable() {
                private List<ServiceState> threadState = new ArrayList<>();

                @Override
                public void run() {
                    PowerManager pm = getSystemService(PowerManager.class);
                    PowerManager.WakeLock wl = pm.newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK,
                            BuildConfig.APPLICATION_ID + ":start");
                    try {
                        wl.acquire();

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

                        long ago = new Date().getTime() - lastLost;
                        if (ago < RECONNECT_BACKOFF)
                            try {
                                long backoff = RECONNECT_BACKOFF - ago;
                                EntityLog.log(ServiceSynchronize.this, "Main backoff=" + (backoff / 1000));
                                Thread.sleep(backoff);
                            } catch (InterruptedException ex) {
                                Log.w(Helper.TAG, "main backoff " + ex.toString());
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
                        db.folder().setFolderError(outbox.id, null);

                        lbm.sendBroadcast(new Intent(ACTION_PROCESS_OPERATIONS)
                                .setType("account/outbox")
                                .putExtra("folder", outbox.id));

                        // Start monitoring accounts
                        for (final EntityAccount account : accounts) {
                            Log.i(Helper.TAG, account.host + "/" + account.user + " run");
                            final ServiceState astate = new ServiceState();
                            astate.thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        monitorAccount(account, astate);
                                    } catch (Throwable ex) {
                                        // Fall-safe
                                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                    }
                                }
                            }, "sync.account." + account.id);
                            astate.thread.start();
                            threadState.add(astate);
                        }

                        EntityLog.log(ServiceSynchronize.this, "Main started");

                        try {
                            yieldWakelock();
                            wl.release();
                            Thread.sleep(Long.MAX_VALUE);
                        } catch (InterruptedException ex) {
                            Log.w(Helper.TAG, "main wait " + ex.toString());
                        } finally {
                            wl.acquire();
                        }

                        // Stop monitoring accounts
                        for (ServiceState astate : threadState) {
                            astate.running = false;
                            astate.thread.interrupt();
                            join(astate.thread);
                        }
                        threadState.clear();

                        // Stop monitoring outbox
                        lbm.unregisterReceiver(outboxReceiver);
                        Log.i(Helper.TAG, outbox.name + " unlisten operations");
                        db.folder().setFolderState(outbox.id, null);

                        EntityLog.log(ServiceSynchronize.this, "Main exited");
                    } catch (Throwable ex) {
                        // Fail-safe
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    } finally {
                        wl.release();
                        EntityLog.log(ServiceSynchronize.this, "Start wake lock=" + wl.isHeld());
                    }
                }
            }, "sync.main");
            state.thread.setPriority(THREAD_PRIORITY_BACKGROUND); // will be inherited
            state.thread.start();
            yieldWakelock();
        }

        private void stop() {
            PowerManager pm = getSystemService(PowerManager.class);
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    BuildConfig.APPLICATION_ID + ":stop");
            try {
                wl.acquire();
                EntityLog.log(ServiceSynchronize.this, "Main stop");

                state.running = false;
                state.thread.interrupt();
                join(state.thread);

                EntityLog.log(ServiceSynchronize.this, "Main stopped");

                state = null;
            } finally {
                wl.release();
                EntityLog.log(ServiceSynchronize.this, "Stop wake lock=" + wl.isHeld());
            }
        }

        private void queue_reload() {
            if (running)
                lifecycle.submit(new Runnable() {
                    @Override
                    public void run() {
                        stop();
                        start();
                    }
                });
        }

        private void queue_start() {
            if (!running) {
                running = true;
                lifecycle.submit(new Runnable() {
                    @Override
                    public void run() {
                        start();
                    }
                });
            }
        }

        private void queue_stop() {
            if (running) {
                running = false;
                lifecycle.submit(new Runnable() {
                    @Override
                    public void run() {
                        stop();
                        stopSelf();
                    }
                });
            }
        }

        private BroadcastReceiver outboxReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                Log.v(Helper.TAG, outbox.name + " run operations");

                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager pm = getSystemService(PowerManager.class);
                        PowerManager.WakeLock wl = pm.newWakeLock(
                                PowerManager.PARTIAL_WAKE_LOCK,
                                BuildConfig.APPLICATION_ID + ":outbox");

                        try {
                            wl.acquire();
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
                        } finally {
                            wl.release();
                            EntityLog.log(ServiceSynchronize.this, "Outbox wake lock=" + wl.isHeld());
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

    private void yieldWakelock() {
        try {
            // Give interrupted thread some time to acquire wake lock
            Thread.sleep(500L);
        } catch (InterruptedException ignored) {
        }
    }

    public static void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("enabled", true))
            ContextCompat.startForegroundService(context, new Intent(context, ServiceSynchronize.class));
    }

    public static void start(Context context) {
        context.startService(new Intent(context, ServiceSynchronize.class).setAction("start"));
    }

    public static void stop(Context context) {
        context.startService(new Intent(context, ServiceSynchronize.class).setAction("stop"));
    }

    public static void reload(Context context, String reason) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("enabled", true)) {
            Log.i(Helper.TAG, "Reload because of '" + reason + "'");
            context.startService(new Intent(context, ServiceSynchronize.class).setAction("reload"));
        }
    }

    private class ServiceState {
        boolean running = true;
        Thread thread;
    }
}
