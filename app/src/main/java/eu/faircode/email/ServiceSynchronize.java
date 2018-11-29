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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.content.BroadcastReceiver;
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
import android.util.LongSparseArray;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MailConnectException;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;

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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class ServiceSynchronize extends LifecycleService {
    private final Object lock = new Object();
    private TupleAccountStats lastStats = null;
    private ServiceManager serviceManager = new ServiceManager();

    private static final int NOTIFICATION_SYNCHRONIZE = 1;

    private static final int CONNECT_BACKOFF_START = 8; // seconds
    private static final int CONNECT_BACKOFF_MAX = 64; // seconds (totally 2 minutes)
    private static final int CONNECT_BACKOFF_AlARM = 15; // minutes
    private static final int SYNC_BATCH_SIZE = 20;
    private static final int DOWNLOAD_BATCH_SIZE = 20;
    private static final long RECONNECT_BACKOFF = 90 * 1000L; // milliseconds
    private static final int PREVIEW_SIZE = 250;
    private static final int ACCOUNT_ERROR_AFTER = 90; // minutes
    private static final long STOP_DELAY = 5000L; // milliseconds

    static final int PI_WHY = 1;
    static final int PI_CLEAR = 2;
    static final int PI_SEEN = 3;
    static final int PI_ARCHIVE = 4;
    static final int PI_TRASH = 5;
    static final int PI_IGNORED = 6;

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

        DB db = DB.getInstance(this);

        db.account().liveStats().observe(this, new Observer<TupleAccountStats>() {
            @Override
            public void onChanged(@Nullable TupleAccountStats stats) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                nm.notify(NOTIFICATION_SYNCHRONIZE, getNotificationService(stats).build());
            }
        });

        db.message().liveUnseenUnified().observe(this, new Observer<List<TupleMessageEx>>() {
            private LongSparseArray<List<Integer>> notifying = new LongSparseArray<>();

            @Override
            public void onChanged(List<TupleMessageEx> messages) {
                NotificationManager nm = getSystemService(NotificationManager.class);

                Widget.update(ServiceSynchronize.this, messages.size());

                LongSparseArray<String> accountName = new LongSparseArray<>();
                LongSparseArray<List<TupleMessageEx>> accountMessages = new LongSparseArray<>();

                for (int i = 0; i < notifying.size(); i++)
                    accountMessages.put(notifying.keyAt(i), new ArrayList<TupleMessageEx>());

                for (TupleMessageEx message : messages) {
                    long account = (message.accountNotify ? message.account : 0);
                    accountName.put(account, account > 0 ? message.accountName : null);
                    if (accountMessages.indexOfKey(account) < 0)
                        accountMessages.put(account, new ArrayList<TupleMessageEx>());
                    accountMessages.get(account).add(message);
                    if (notifying.indexOfKey(account) < 0)
                        notifying.put(account, new ArrayList<Integer>());
                }

                for (int i = 0; i < accountMessages.size(); i++) {
                    long account = accountMessages.keyAt(i);
                    List<Notification> notifications = getNotificationUnseen(
                            account, accountName.get(account), accountMessages.get(account));

                    List<Integer> all = new ArrayList<>();
                    List<Integer> added = new ArrayList<>();
                    List<Integer> removed = notifying.get(account);
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
                        nm.cancel("unseen:" + account, 0);

                    for (Integer id : removed)
                        nm.cancel("unseen:" + account, id);

                    for (Notification notification : notifications) {
                        Integer id = (int) notification.extras.getLong("id", 0);
                        if ((id == 0 && added.size() + removed.size() > 0) || added.contains(id))
                            nm.notify("unseen:" + account, id, notification);
                    }

                    notifying.put(account, all);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.i(Helper.TAG, "Service destroy");

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(serviceManager);

        serviceManager.service_destroy();

        Widget.update(this, -1);

        stopForeground(true);

        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.cancel(NOTIFICATION_SYNCHRONIZE);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent == null ? null : intent.getAction());
        Log.i(Helper.TAG, "Service command intent=" + intent + " action=" + action);

        startForeground(NOTIFICATION_SYNCHRONIZE, getNotificationService(null).build());

        super.onStartCommand(intent, flags, startId);

        if (action != null) {
            if ("why".equals(action)) {
                Intent why = new Intent(Intent.ACTION_VIEW);
                why.setData(Uri.parse("https://github.com/M66B/open-source-email/blob/master/FAQ.md#user-content-faq2"));
                why.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                PackageManager pm = getPackageManager();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (prefs.getBoolean("why", false) || why.resolveActivity(pm) == null) {
                    Intent main = new Intent(this, ActivityView.class);
                    main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(main);
                } else {
                    prefs.edit().putBoolean("why", true).apply();
                    startActivity(why);
                }

            } else if ("init".equals(action)) {
                // Network events will manage the service
                serviceManager.service_init();

            } else if ("reload".equals(action)) {
                serviceManager.queue_reload(true, intent.getStringExtra("reason"));

            } else if ("clear".equals(action)) {
                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) {
                        DB.getInstance(context).message().ignoreAll();
                        return null;
                    }
                }.load(this, new Bundle());

            } else if (action.startsWith("seen:") ||
                    action.startsWith("archive:") ||
                    action.startsWith("trash:") ||
                    action.startsWith("ignored:")) {
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
                                db.message().setMessageUiIgnored(message.id, true);
                                EntityOperation.queue(db, message, EntityOperation.SEEN, true);
                            } else if ("archive".equals(action)) {
                                EntityFolder archive = db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE);
                                if (archive == null)
                                    archive = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                                if (archive != null) {
                                    EntityOperation.queue(db, message, EntityOperation.SEEN, true);
                                    EntityOperation.queue(db, message, EntityOperation.MOVE, archive.id);
                                    db.message().setMessageUiHide(message.id, true);
                                }
                            } else if ("trash".equals(action)) {
                                EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                                if (trash != null) {
                                    EntityOperation.queue(db, message, EntityOperation.SEEN, true);
                                    EntityOperation.queue(db, message, EntityOperation.MOVE, trash.id);
                                    db.message().setMessageUiHide(message.id, true);
                                }
                            } else if ("ignored".equals(action))
                                db.message().setMessageUiIgnored(message.id, true);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        EntityOperation.process(context);

                        return null;
                    }
                }.load(this, args);
            }
        }

        return START_STICKY;
    }

    private Notification.Builder getNotificationService(TupleAccountStats stats) {
        if (stats == null)
            stats = lastStats;
        if (stats == null)
            stats = new TupleAccountStats();

        // Build pending intent
        Intent intent = new Intent(this, ServiceSynchronize.class);
        intent.setAction("why");
        PendingIntent pi = PendingIntent.getService(this, PI_WHY, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this, "service");
        else
            builder = new Notification.Builder(this);

        builder
                .setSmallIcon(R.drawable.baseline_compare_arrows_white_24)
                .setContentTitle(getResources().getQuantityString(
                        R.plurals.title_notification_synchronizing, stats.accounts, stats.accounts))
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_SECRET);

        if (stats.operations > 0)
            builder.setStyle(new Notification.BigTextStyle().setSummaryText(
                    getResources().getQuantityString(
                            R.plurals.title_notification_operations, stats.operations, stats.operations)));

        if (stats.unsent > 0)
            builder.setContentText(getResources().getQuantityString(
                    R.plurals.title_notification_unsent, stats.unsent, stats.unsent));

        lastStats = stats;

        return builder;
    }

    private List<Notification> getNotificationUnseen(long account, String accountName, List<TupleMessageEx> messages) {
        List<Notification> notifications = new ArrayList<>();

        if (messages.size() == 0)
            return notifications;

        boolean pro = Helper.isPro(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // https://developer.android.com/training/notify-user/group
        String group = Long.toString(account);

        // Build pending intent
        Intent view = new Intent(this, ActivityView.class);
        view.setAction("unified");
        view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piView = PendingIntent.getActivity(
                this, ActivityView.REQUEST_UNIFIED, view, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent clear = new Intent(this, ServiceSynchronize.class);
        clear.setAction("clear");
        PendingIntent piClear = PendingIntent.getService(this, PI_CLEAR, clear, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build public notification
        Notification.Builder pbuilder;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            pbuilder = new Notification.Builder(this);
        else
            pbuilder = new Notification.Builder(this, "notification");

        pbuilder
                .setSmallIcon(R.drawable.baseline_email_white_24)
                .setContentTitle(getResources().getQuantityString(R.plurals.title_notification_unseen, messages.size(), messages.size()))
                .setContentIntent(piView)
                .setNumber(messages.size())
                .setShowWhen(false)
                .setDeleteIntent(piClear)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        if (!TextUtils.isEmpty(accountName))
            pbuilder.setSubText(accountName);

        // Build notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder = new Notification.Builder(this);
        else
            builder = new Notification.Builder(this, "notification");

        builder
                .setSmallIcon(R.drawable.baseline_email_white_24)
                .setContentTitle(getResources().getQuantityString(R.plurals.title_notification_unseen, messages.size(), messages.size()))
                .setContentIntent(piView)
                .setNumber(messages.size())
                .setShowWhen(false)
                .setDeleteIntent(piClear)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setPublicVersion(pbuilder.build())
                .setGroup(group)
                .setGroupSummary(true);

        if (!TextUtils.isEmpty(accountName))
            builder.setSubText(accountName);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setSound(null);
        else
            builder.setGroupAlertBehavior(Notification.GROUP_ALERT_CHILDREN);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O &&
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
                sb.append(" ").append(df.format(new Date(message.received)));
                sb.append("<br>");
            }

            builder.setStyle(new Notification.BigTextStyle().bigText(Html.fromHtml(sb.toString())));
        }

        notifications.add(builder.build());

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        for (TupleMessageEx message : messages) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            Intent thread = new Intent(this, ActivityView.class);
            thread.setAction("thread:" + message.thread);
            thread.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            thread.putExtra("account", message.account);
            PendingIntent piContent = PendingIntent.getActivity(
                    this, ActivityView.REQUEST_THREAD, thread, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent ignored = new Intent(this, ServiceSynchronize.class);
            ignored.setAction("ignored:" + message.id);
            PendingIntent piDelete = PendingIntent.getService(this, PI_IGNORED, ignored, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent seen = new Intent(this, ServiceSynchronize.class);
            seen.setAction("seen:" + message.id);
            PendingIntent piSeen = PendingIntent.getService(this, PI_SEEN, seen, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent archive = new Intent(this, ServiceSynchronize.class);
            archive.setAction("archive:" + message.id);
            PendingIntent piArchive = PendingIntent.getService(this, PI_ARCHIVE, archive, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent trash = new Intent(this, ServiceSynchronize.class);
            trash.setAction("trash:" + message.id);
            PendingIntent piTrash = PendingIntent.getService(this, PI_TRASH, trash, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Action.Builder actionSeen = new Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.baseline_visibility_24),
                    getString(R.string.title_action_seen),
                    piSeen);

            Notification.Action.Builder actionArchive = new Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.baseline_archive_24),
                    getString(R.string.title_action_archive),
                    piArchive);

            Notification.Action.Builder actionTrash = new Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.baseline_delete_24),
                    getString(R.string.title_action_trash),
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
                    .setSubText(message.accountName)
                    .setContentIntent(piContent)
                    .setSound(uri)
                    .setWhen(message.received)
                    .setDeleteIntent(piDelete)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setVisibility(Notification.VISIBILITY_PRIVATE)
                    .setGroup(group)
                    .setGroupSummary(false)
                    .addAction(actionSeen.build())
                    .addAction(actionArchive.build())
                    .addAction(actionTrash.build());

            if (pro) {
                if (!TextUtils.isEmpty(message.subject))
                    mbuilder.setContentText(message.subject);

                if (!TextUtils.isEmpty(message.avatar)) {
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(
                                Uri.parse(message.avatar),
                                new String[]{ContactsContract.Contacts._ID},
                                null, null, null);
                        if (cursor.moveToNext()) {
                            Uri photo = Uri.withAppendedPath(
                                    ContactsContract.Contacts.CONTENT_URI,
                                    cursor.getLong(0) + "/photo");
                            mbuilder.setLargeIcon(Icon.createWithContentUri(photo));
                        }
                    } catch (SecurityException ex) {
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        mbuilder.addPerson(new Person.Builder()
                                .setUri(message.avatar)
                                .build());
                    else
                        mbuilder.addPerson(message.avatar);
                }

                if (message.accountColor != null) {
                    mbuilder.setColor(message.accountColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        mbuilder.setColorized(true);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                mbuilder.setGroupAlertBehavior(Notification.GROUP_ALERT_CHILDREN);

            notifications.add(mbuilder.build());
        }

        return notifications;
    }

    private Notification.Builder getNotificationError(String action, Throwable ex) {
        return getNotificationError(action, new Date().getTime(), ex);
    }

    private Notification.Builder getNotificationError(String action, long when, Throwable ex) {
        // Build pending intent
        Intent intent = new Intent(this, ActivitySetup.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                this, ActivitySetup.REQUEST_ERROR, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String text = ex.getMessage();
        if (TextUtils.isEmpty(text))
            text = ex.getClass().getName();

        // Build notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this, "error");
        else
            builder = new Notification.Builder(this);

        builder
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(getString(R.string.title_notification_failed, action))
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setWhen(when)
                .setShowWhen(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setOnlyAlertOnce(true)
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
        // - on connectivity problems when connecting to store

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

        // connection failure: Too many simultaneous connections

        if (BuildConfig.DEBUG &&
                !(ex instanceof SendFailedException) &&
                !(ex instanceof MailConnectException) &&
                !(ex instanceof FolderClosedException) &&
                !(ex instanceof IllegalStateException) &&
                !(ex instanceof AuthenticationFailedException) && // Also: Too many simultaneous connections
                !(ex instanceof StoreClosedException) &&
                !(ex instanceof MessageRemovedException) &&
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

    private void monitorAccount(final EntityAccount account, final ServiceState state) throws NoSuchProviderException, TimeoutException {
        final PowerManager pm = getSystemService(PowerManager.class);
        final PowerManager.WakeLock wl0 = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                BuildConfig.APPLICATION_ID + ":account." + account.id + ".monitor");
        try {
            wl0.acquire();

            final DB db = DB.getInstance(this);
            final ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

            int backoff = CONNECT_BACKOFF_START;
            while (state.running()) {
                state.reset();
                Log.i(Helper.TAG, account.name + " run");

                // Debug
                boolean debug = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("debug", false);
                debug = debug || BuildConfig.DEBUG;
                System.setProperty("mail.socket.debug", Boolean.toString(debug));

                // Create session
                Properties props = MessageHelper.getSessionProperties(account.auth_type, account.insecure);
                final Session isession = Session.getInstance(props, null);
                isession.setDebug(debug);
                // adb -t 1 logcat | grep "fairemail\|System.out"

                final IMAPStore istore = (IMAPStore) isession.getStore(account.starttls ? "imap" : "imaps");
                final Map<EntityFolder, IMAPFolder> folders = new HashMap<>();
                List<Thread> syncs = new ArrayList<>();
                List<Thread> idlers = new ArrayList<>();
                try {
                    // Listen for store events
                    istore.addStoreListener(new StoreListener() {
                        PowerManager.WakeLock wl = pm.newWakeLock(
                                PowerManager.PARTIAL_WAKE_LOCK,
                                BuildConfig.APPLICATION_ID + ":account." + account.id + ".store");

                        @Override
                        public void notification(StoreEvent e) {
                            try {
                                wl.acquire();
                                String type = (e.getMessageType() == StoreEvent.ALERT ? "alert" : "notice");
                                EntityLog.log(ServiceSynchronize.this, account.name + " " + type + ": " + e.getMessage());
                                if (e.getMessageType() == StoreEvent.ALERT) {
                                    db.account().setAccountError(account.id, e.getMessage());
                                    state.error();
                                }
                            } finally {
                                wl.release();
                            }
                        }
                    });

                    // Listen for folder events
                    istore.addFolderListener(new FolderAdapter() {
                        PowerManager.WakeLock wl = pm.newWakeLock(
                                PowerManager.PARTIAL_WAKE_LOCK,
                                BuildConfig.APPLICATION_ID + ":account." + account.id + ".folder");

                        @Override
                        public void folderCreated(FolderEvent e) {
                            try {
                                wl.acquire();
                                Log.i(Helper.TAG, "Folder created=" + e.getFolder().getFullName());
                                reload(ServiceSynchronize.this, "folder created");
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

                                reload(ServiceSynchronize.this, "folder renamed");
                            } finally {
                                wl.release();
                            }
                        }

                        @Override
                        public void folderDeleted(FolderEvent e) {
                            try {
                                wl.acquire();
                                Log.i(Helper.TAG, "Folder deleted=" + e.getFolder().getFullName());
                                reload(ServiceSynchronize.this, "folder deleted");
                            } finally {
                                wl.release();
                            }
                        }
                    });

                    // Listen for connection events
                    istore.addConnectionListener(new ConnectionAdapter() {
                        @Override
                        public void opened(ConnectionEvent e) {
                            Log.i(Helper.TAG, account.name + " opened event");
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
                    EntityLog.log(this, account.name + " connecting");
                    for (EntityFolder folder : db.folder().getFolders(account.id))
                        db.folder().setFolderState(folder.id, null);
                    db.account().setAccountState(account.id, "connecting");

                    try {
                        Helper.connect(this, istore, account);
                    } catch (Throwable ex) {
                        // Report account connection error
                        if (account.last_connected != null) {
                            EntityLog.log(this, account.name + " last connected: " + new Date(account.last_connected));
                            long now = new Date().getTime();
                            if (now - account.last_connected > ACCOUNT_ERROR_AFTER * 60 * 1000L) {
                                NotificationManager nm = getSystemService(NotificationManager.class);
                                nm.notify("receive", account.id.intValue(),
                                        getNotificationError(account.name, account.last_connected, ex).build());
                            }
                        }

                        throw ex;
                    }

                    final boolean capIdle = istore.hasCapability("IDLE");
                    Log.i(Helper.TAG, account.name + " idle=" + capIdle);

                    db.account().setAccountState(account.id, "connected");

                    NotificationManager nm = getSystemService(NotificationManager.class);
                    nm.cancel("receive", account.id.intValue());

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
                            if (ex instanceof MessagingException && "connection failure".equals(ex.getMessage())) {
                                Throwable ex1 = new MessagingException("Too many simultaneous connections?", (MessagingException) ex);
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex1));
                            } else
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                            throw ex;
                        }
                        folders.put(folder, ifolder);

                        db.folder().setFolderState(folder.id, "connected");
                        db.folder().setFolderError(folder.id, null);
                        db.folder().setFolderKeywords(folder.id, DB.Converters.fromStringArray(ifolder.getPermanentFlags().getUserFlags()));

                        Log.i(Helper.TAG, account.name + " folder " + folder.name + " flags=" + ifolder.getPermanentFlags());

                        // Synchronize folder
                        Thread sync = new Thread(new Runnable() {
                            PowerManager.WakeLock wl = pm.newWakeLock(
                                    PowerManager.PARTIAL_WAKE_LOCK,
                                    BuildConfig.APPLICATION_ID + ":account." + account.id + ".sync");

                            @Override
                            public void run() {
                                try {
                                    wl.acquire();

                                    // Process pending operations
                                    processOperations(folder, isession, istore, ifolder, state);

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
                                                                id = synchronizeMessage(
                                                                        ServiceSynchronize.this,
                                                                        folder, ifolder, (IMAPMessage) imessage,
                                                                        false, false, false);
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
                                                    state.error();
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
                                                    state.error();
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
                                                            id = synchronizeMessage(
                                                                    ServiceSynchronize.this,
                                                                    folder, ifolder, (IMAPMessage) e.getMessage(),
                                                                    false, false, false);
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
                                                    state.error();
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
                                    state.error();
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
                                        while (state.alive()) {
                                            Log.i(Helper.TAG, folder.name + " do idle");
                                            ifolder.idle(false);
                                            //Log.i(Helper.TAG, folder.name + " done idle");
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                        reportError(account.name, folder.name, ex);
                                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                        state.error();
                                    } finally {
                                        Log.i(Helper.TAG, folder.name + " end idle");
                                    }
                                }
                            }, "idler." + folder.id);
                            idler.start();
                            idlers.add(idler);
                        }
                    }

                    // Process folder actions
                    BroadcastReceiver processFolder = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, final Intent intent) {
                            executor.submit(new Runnable() {
                                PowerManager.WakeLock wl = pm.newWakeLock(
                                        PowerManager.PARTIAL_WAKE_LOCK,
                                        BuildConfig.APPLICATION_ID + ":account." + account.id + ".process");

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
                                                processOperations(folder, isession, istore, ifolder, state);

                                            else if (ACTION_SYNCHRONIZE_FOLDER.equals(intent.getAction())) {
                                                processOperations(folder, isession, istore, ifolder, state);
                                                synchronizeMessages(account, folder, ifolder, state);
                                            }

                                        } catch (Throwable ex) {
                                            Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                            reportError(account.name, folder.name, ex);
                                            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                            state.error();
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

                    for (EntityFolder folder : folders.keySet())
                        if (db.operation().getOperationCount(folder.id) > 0) {
                            Intent intent = new Intent();
                            intent.setType("account/" + account.id);
                            intent.setAction(ServiceSynchronize.ACTION_PROCESS_OPERATIONS);
                            intent.putExtra("folder", folder.id);
                            lbm.sendBroadcast(intent);
                        }

                    // Keep alive alarm receiver
                    BroadcastReceiver alarm = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            // Receiver runs on main thread
                            // Receiver has a wake lock for ~10 seconds
                            EntityLog.log(context, account.name + " keep alive wake lock=" + wl0.isHeld());
                            state.release();
                        }
                    };

                    String id = BuildConfig.APPLICATION_ID + ".POLL." + account.id;
                    PendingIntent pi = PendingIntent.getBroadcast(ServiceSynchronize.this, 0, new Intent(id), 0);
                    registerReceiver(alarm, new IntentFilter(id));

                    // Keep alive
                    AlarmManager am = getSystemService(AlarmManager.class);
                    try {
                        while (state.running()) {
                            if (!istore.isConnected())
                                throw new StoreClosedException(istore);

                            for (EntityFolder folder : folders.keySet())
                                if (capIdle) {
                                    if (!folders.get(folder).isOpen())
                                        throw new FolderClosedException(folders.get(folder));
                                } else
                                    synchronizeMessages(account, folder, folders.get(folder), state);

                            // Successfully connected: reset back off time
                            backoff = CONNECT_BACKOFF_START;

                            // Record successful connection
                            db.account().setAccountConnected(account.id, new Date().getTime());
                            db.account().setAccountError(account.id, capIdle ? null : getString(R.string.title_no_idle));

                            // Schedule keep alive alarm
                            EntityLog.log(this, account.name + " wait=" + account.poll_interval);
                            am.setAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    System.currentTimeMillis() + account.poll_interval * 60 * 1000L,
                                    pi);

                            try {
                                wl0.release();
                                state.acquire();
                            } catch (InterruptedException ex) {
                                EntityLog.log(this, account.name + " waited state=" + state);
                            } finally {
                                wl0.acquire();
                            }
                        }
                    } finally {
                        // Cleanup
                        am.cancel(pi);
                        unregisterReceiver(alarm);
                        lbm.unregisterReceiver(processFolder);
                    }

                    Log.i(Helper.TAG, account.name + " done state=" + state);
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    reportError(account.name, null, ex);

                    EntityLog.log(ServiceSynchronize.this, account.name + " " + Helper.formatThrowable(ex));
                    db.account().setAccountError(account.id, Helper.formatThrowable(ex));
                } finally {
                    EntityLog.log(this, account.name + " closing");
                    db.account().setAccountState(account.id, "closing");
                    for (EntityFolder folder : folders.keySet())
                        db.folder().setFolderState(folder.id, "closing");

                    // Close store
                    try {
                        EntityLog.log(ServiceSynchronize.this, account.name + " store closing");
                        istore.close();
                        EntityLog.log(ServiceSynchronize.this, account.name + " store closed");
                    } catch (Throwable ex) {
                        Log.w(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    } finally {
                        EntityLog.log(this, account.name + " closed");
                        db.account().setAccountState(account.id, null);
                    }

                    // Stop syncs
                    for (Thread sync : syncs)
                        state.join(sync);

                    // Stop idlers
                    for (Thread idler : idlers)
                        state.join(idler);

                    for (EntityFolder folder : folders.keySet())
                        db.folder().setFolderState(folder.id, null);
                }

                if (state.running())
                    try {
                        if (backoff <= CONNECT_BACKOFF_MAX) {
                            // Short back-off period, keep device awake
                            EntityLog.log(this, account.name + " backoff=" + backoff);
                            state.acquire(backoff * 1000L);
                        } else {
                            // Long back-off period, let device sleep
                            EntityLog.log(this, account.name + " backoff alarm=" + CONNECT_BACKOFF_AlARM);

                            BroadcastReceiver alarm = new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    state.release();
                                }
                            };

                            String id = BuildConfig.APPLICATION_ID + ".BACKOFF." + account.id;
                            PendingIntent pi = PendingIntent.getBroadcast(ServiceSynchronize.this, 0, new Intent(id), 0);
                            registerReceiver(alarm, new IntentFilter(id));

                            AlarmManager am = getSystemService(AlarmManager.class);
                            try {
                                am.setAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        System.currentTimeMillis() + CONNECT_BACKOFF_AlARM * 60 * 1000L,
                                        pi);

                                try {
                                    wl0.release();
                                    state.acquire(2 * CONNECT_BACKOFF_AlARM * 60 * 1000L);
                                } finally {
                                    wl0.acquire();
                                }
                            } finally {
                                // Cleanup
                                am.cancel(pi);
                                unregisterReceiver(alarm);
                            }
                        }

                        if (backoff <= CONNECT_BACKOFF_MAX)
                            backoff *= 2;
                    } catch (InterruptedException ex) {
                        Log.w(Helper.TAG, account.name + " backoff " + ex.toString());
                    }
            }
        } finally {
            EntityLog.log(this, account.name + " stopped");
            wl0.release();
        }
    }

    private void processOperations(EntityFolder folder, Session isession, IMAPStore istore, IMAPFolder ifolder, ServiceState state) throws MessagingException, JSONException, IOException {
        synchronized (lock) {
            try {
                Log.i(Helper.TAG, folder.name + " start process");

                DB db = DB.getInstance(this);
                List<EntityOperation> ops = db.operation().getOperationsByFolder(folder.id);
                Log.i(Helper.TAG, folder.name + " pending operations=" + ops.size());
                for (int i = 0; i < ops.size() && state.alive(); i++) {
                    EntityOperation op = ops.get(i);
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
                                throw new IllegalArgumentException(op.name + " without uid " + op.args);

                            JSONArray jargs = new JSONArray(op.args);

                            if (EntityOperation.SEEN.equals(op.name))
                                doSeen(folder, ifolder, message, jargs, db);

                            else if (EntityOperation.ANSWERED.equals(op.name))
                                doAnswered(folder, ifolder, message, jargs, db);

                            else if (EntityOperation.FLAG.equals(op.name))
                                doFlag(folder, ifolder, message, jargs, db);

                            else if (EntityOperation.KEYWORD.equals(op.name))
                                doKeyword(folder, ifolder, message, jargs, db);

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
                            reportError(null, folder.name, ex);

                            if (message != null &&
                                    !(ex instanceof MessageRemovedException) &&
                                    !(ex instanceof FolderClosedException) &&
                                    !(ex instanceof IllegalStateException))
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
                }
            } finally {
                Log.i(Helper.TAG, folder.name + " end process state=" + state);
            }
        }
    }

    private void doSeen(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, JSONException {
        // Mark message (un)seen
        if (!ifolder.getPermanentFlags().contains(Flags.Flag.SEEN)) {
            db.message().setMessageSeen(message.id, false);
            db.message().setMessageUiSeen(message.id, false);
            return;
        }

        boolean seen = jargs.getBoolean(0);
        if (message.seen.equals(seen))
            return;

        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        imessage.setFlag(Flags.Flag.SEEN, seen);

        db.message().setMessageSeen(message.id, seen);
    }

    private void doAnswered(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, JSONException {
        // Mark message (un)answered
        if (!ifolder.getPermanentFlags().contains(Flags.Flag.ANSWERED)) {
            db.message().setMessageAnswered(message.id, false);
            db.message().setMessageUiAnswered(message.id, false);
            return;
        }

        boolean answered = jargs.getBoolean(0);
        if (message.answered.equals(answered))
            return;

        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        imessage.setFlag(Flags.Flag.ANSWERED, answered);

        db.message().setMessageAnswered(message.id, answered);
    }

    private void doFlag(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, JSONException {
        // Star/unstar message
        if (!ifolder.getPermanentFlags().contains(Flags.Flag.FLAGGED)) {
            db.message().setMessageFlagged(message.id, false);
            db.message().setMessageUiFlagged(message.id, false);
            return;
        }

        boolean flagged = jargs.getBoolean(0);
        if (message.flagged.equals(flagged))
            return;

        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        imessage.setFlag(Flags.Flag.FLAGGED, flagged);

        db.message().setMessageFlagged(message.id, flagged);
    }

    private void doKeyword(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, JSONException {
        // Set/reset user flag
        if (!ifolder.getPermanentFlags().contains(Flags.Flag.USER)) {
            db.message().setMessageKeywords(message.id, DB.Converters.fromStringArray(null));
            return;
        }

        // https://tools.ietf.org/html/rfc3501#section-2.3.2
        String keyword = jargs.getString(0);
        boolean set = jargs.getBoolean(1);

        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        Flags flags = new Flags(keyword);
        imessage.setFlags(flags, set);

        List<String> keywords = new ArrayList<>(Arrays.asList(message.keywords));
        if (set) {
            if (!keywords.contains(keyword))
                keywords.add(keyword);
        } else
            keywords.remove(keyword);
        db.message().setMessageKeywords(message.id, DB.Converters.fromStringArray(keywords.toArray(new String[0])));
    }

    private void doAdd(EntityFolder folder, Session isession, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, JSONException, IOException {
        // Append message
        MimeMessage imessage = MessageHelper.from(this, message, isession);

        if (EntityFolder.DRAFTS.equals(folder.type) && ifolder.getPermanentFlags().contains(Flags.Flag.DRAFT))
            imessage.setFlag(Flags.Flag.DRAFT, true);

        AppendUID[] uid = ifolder.appendUIDMessages(new Message[]{imessage});
        Log.i(Helper.TAG, "Appended uid=" + uid[0].uid + " draft=" + imessage.getFlags().contains(Flags.Flag.DRAFT));

        db.message().setMessageUid(message.id, uid[0].uid);

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

            if (!EntityFolder.ARCHIVE.equals(folder.type)) {
                imessage.setFlag(Flags.Flag.DELETED, true);
                ifolder.expunge();
            }

            MimeMessageEx icopy = MessageHelper.from(this, message, isession);
            Folder itarget = istore.getFolder(target.name);
            itarget.appendMessages(new Message[]{icopy});
        }

        if (EntityFolder.ARCHIVE.equals(folder.type))
            db.message().setMessageUiHide(message.id, false);
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

        if (message.last_attempt == null) {
            message.last_attempt = new Date().getTime();
            db.message().setMessageLastAttempt(message.id, message.last_attempt);
        }

        // Create session
        Properties props = MessageHelper.getSessionProperties(ident.auth_type, ident.insecure);
        props.put("mail.smtp.localhost", ident.host);
        final Session isession = Session.getInstance(props, null);

        // Create message
        MimeMessage imessage = MessageHelper.from(this, message, isession);

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

            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.cancel("send", message.account.intValue());

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

                if (ident.store_sent || ident.sent_folder != null) {
                    EntityFolder sent;
                    if (ident.store_sent)
                        sent = db.folder().getFolderByType(ident.account, EntityFolder.SENT);
                    else
                        sent = db.folder().getFolder(ident.sent_folder);
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

            EntityLog.log(this, ident.name + " last attempt: " + new Date(message.last_attempt));

            long now = new Date().getTime();
            if (now - message.last_attempt > ACCOUNT_ERROR_AFTER * 60 * 1000L) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                nm.notify("send", message.account.intValue(), getNotificationError(ident.name, ex).build());
            }

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

        StringBuilder sb = new StringBuilder();
        if (BuildConfig.DEBUG)
            sb.append(imessage.getFlags().toString()).append("\n");

        Enumeration<Header> headers = imessage.getAllHeaders();
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
        String html = helper.getHtml();
        String text = (html == null ? null : Jsoup.parse(html).text());
        String preview = (text == null ? null : text.substring(0, Math.min(text.length(), PREVIEW_SIZE)));
        message.write(this, html);
        db.message().setMessageContent(message.id, true, preview);
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

            Log.v(Helper.TAG, "Start sync folders account=" + account.name);

            List<String> names = new ArrayList<>();
            for (EntityFolder folder : db.folder().getUserFolders(account.id))
                names.add(folder.name);
            Log.i(Helper.TAG, "Local folder count=" + names.size());

            Folder[] ifolders = istore.getDefaultFolder().list("*");
            Log.i(Helper.TAG, "Remote folder count=" + ifolders.length);

            for (Folder ifolder : ifolders) {
                boolean selectable = true;
                String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                for (String attr : attrs) {
                    if ("\\Noselect".equals(attr))
                        selectable = false;
                }

                if (selectable) {
                    EntityFolder folder = db.folder().getFolderByName(account.id, ifolder.getFullName());
                    if (folder == null) {
                        folder = new EntityFolder();
                        folder.account = account.id;
                        folder.name = ifolder.getFullName();
                        folder.type = EntityFolder.USER;
                        folder.synchronize = false;
                        folder.sync_days = EntityFolder.DEFAULT_USER_SYNC;
                        folder.keep_days = EntityFolder.DEFAULT_USER_SYNC;
                        db.folder().insertFolder(folder);
                        Log.i(Helper.TAG, folder.name + " added");
                    } else {
                        names.remove(folder.name);
                        Log.i(Helper.TAG, folder.name + " exists");
                    }
                }
            }

            Log.i(Helper.TAG, "Delete local folder=" + names.size());
            for (String name : names) {
                db.folder().deleteFolder(account.id, name);
                Log.i(Helper.TAG, name + " deleted");
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.v(Helper.TAG, "End sync folder");
        }
    }

    private void synchronizeMessages(EntityAccount account, EntityFolder folder, IMAPFolder ifolder, ServiceState state) throws MessagingException, IOException {
        DB db = DB.getInstance(this);
        try {
            Log.v(Helper.TAG, folder.name + " start sync after=" + folder.sync_days + "/" + folder.keep_days);

            db.folder().setFolderState(folder.id, "syncing");

            // Get reference times
            Calendar cal_sync = Calendar.getInstance();
            cal_sync.add(Calendar.DAY_OF_MONTH, -folder.sync_days);
            cal_sync.set(Calendar.HOUR_OF_DAY, 0);
            cal_sync.set(Calendar.MINUTE, 0);
            cal_sync.set(Calendar.SECOND, 0);
            cal_sync.set(Calendar.MILLISECOND, 0);

            Calendar cal_keep = Calendar.getInstance();
            cal_keep.add(Calendar.DAY_OF_MONTH, -folder.keep_days);
            cal_keep.set(Calendar.HOUR_OF_DAY, 0);
            cal_keep.set(Calendar.MINUTE, 0);
            cal_keep.set(Calendar.SECOND, 0);
            cal_keep.set(Calendar.MILLISECOND, 0);

            long sync_time = cal_sync.getTimeInMillis();
            if (sync_time < 0)
                sync_time = 0;

            long keep_time = cal_keep.getTimeInMillis();
            if (keep_time < 0)
                keep_time = 0;

            Log.i(Helper.TAG, folder.name + " sync=" + new Date(sync_time) + " keep=" + new Date(keep_time));

            // Delete old local messages
            int old = db.message().deleteMessagesBefore(folder.id, keep_time, false);
            Log.i(Helper.TAG, folder.name + " local old=" + old);

            // Get list of local uids
            List<Long> uids = db.message().getUids(folder.id, sync_time);
            Log.i(Helper.TAG, folder.name + " local count=" + uids.size());

            // Reduce list of local uids
            long search = SystemClock.elapsedRealtime();
            Message[] imessages = ifolder.search(new ReceivedDateTerm(ComparisonTerm.GE, new Date(sync_time)));
            Log.i(Helper.TAG, folder.name + " remote count=" + imessages.length +
                    " search=" + (SystemClock.elapsedRealtime() - search) + " ms");

            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(FetchProfile.Item.FLAGS);
            ifolder.fetch(imessages, fp);

            long fetch = SystemClock.elapsedRealtime();
            Log.i(Helper.TAG, folder.name + " remote fetched=" + (SystemClock.elapsedRealtime() - fetch) + " ms");

            for (int i = 0; i < imessages.length && state.alive(); i++) {
                Message imessage = imessages[i];

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
            for (int i = imessages.length - 1; i >= 0 && state.alive(); i -= SYNC_BATCH_SIZE) {
                int from = Math.max(0, i - SYNC_BATCH_SIZE + 1);
                //Log.i(Helper.TAG, folder.name + " update " + from + " .. " + i);

                Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);

                // Full fetch new/changed messages only
                List<Message> full = new ArrayList<>();
                for (Message imessage : isub) {
                    long uid = ifolder.getUID(imessage);
                    EntityMessage message = db.message().getMessageByUid(folder.id, uid, false);
                    if (message == null)
                        full.add(imessage);
                }
                if (full.size() > 0) {
                    long headers = SystemClock.elapsedRealtime();
                    ifolder.fetch(full.toArray(new Message[0]), fp);
                    Log.i(Helper.TAG, folder.name + " fetched headers=" + full.size() +
                            " " + (SystemClock.elapsedRealtime() - headers) + " ms");
                }

                for (int j = isub.length - 1; j >= 0 && state.alive(); j--)
                    try {
                        db.beginTransaction();
                        ids[from + j] = synchronizeMessage(
                                this,
                                folder, ifolder, (IMAPMessage) isub[j],
                                false, false, true);
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

                if (state.alive())
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
            }

            db.folder().setFolderState(folder.id, "downloading");

            //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);

            // Download messages/attachments
            Log.i(Helper.TAG, folder.name + " download=" + imessages.length);
            for (int i = imessages.length - 1; i >= 0 && state.alive(); i -= DOWNLOAD_BATCH_SIZE) {
                int from = Math.max(0, i - DOWNLOAD_BATCH_SIZE + 1);
                //Log.i(Helper.TAG, folder.name + " download " + from + " .. " + i);

                Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);
                // Fetch on demand

                for (int j = isub.length - 1; j >= 0 && state.alive(); j--)
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

                if (state.alive())
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
            }

        } finally {
            Log.v(Helper.TAG, folder.name + " end sync state=" + state);
            db.folder().setFolderState(folder.id, ifolder.isOpen() ? "connected" : "disconnected");
        }
    }

    static Long synchronizeMessage(
            Context context,
            EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage,
            boolean found, boolean browsed, boolean full) throws MessagingException, IOException {
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
        boolean answered = helper.getAnsered();
        boolean flagged = helper.getFlagged();
        String[] keywords = helper.getKeywords();

        DB db = DB.getInstance(context);

        // Find message by uid (fast, no headers required)
        EntityMessage message = db.message().getMessageByUid(folder.id, uid, found);

        // Find message by Message-ID (slow, headers required)
        // - messages in inbox have same id as message sent to self
        // - messages in archive have same id as original
        if (message == null) {
            // Will fetch headers within database transaction
            String msgid = helper.getMessageID();
            String[] refs = helper.getReferences();
            String reference = (refs.length == 1 && refs[0].indexOf(BuildConfig.APPLICATION_ID) > 0 ? refs[0] : msgid);
            Log.i(Helper.TAG, "Searching for " + msgid + " / " + reference);
            for (EntityMessage dup : db.message().getMessageByMsgId(folder.account, msgid, reference, found)) {
                EntityFolder dfolder = db.folder().getFolder(dup.folder);
                boolean outbox = EntityFolder.OUTBOX.equals(dfolder.type);
                Log.i(Helper.TAG, folder.name + " found as id=" + dup.id + "/" + dup.uid +
                        " folder=" + dfolder.type + ":" + dup.folder + "/" + folder.type + ":" + folder.id +
                        " msgid=" + dup.msgid + " thread=" + dup.thread);

                if (dup.folder.equals(folder.id) || outbox) {
                    String thread = helper.getThreadId(uid);
                    Log.i(Helper.TAG, folder.name + " found as id=" + dup.id + "/" + uid +
                            " msgid=" + msgid + " thread=" + thread);
                    dup.folder = folder.id; // From outbox
                    dup.uid = uid;
                    dup.msgid = msgid;
                    dup.thread = thread;
                    dup.error = null;
                    db.message().updateMessage(dup);
                    message = dup;
                }
            }
        }

        if (message == null) {
            // Build list of addresses
            Address[] recipients = helper.getTo();
            Address[] senders = helper.getFrom();
            if (recipients == null)
                recipients = new Address[0];
            if (senders == null)
                senders = new Address[0];
            Address[] all = Arrays.copyOf(recipients, recipients.length + senders.length);
            System.arraycopy(senders, 0, all, recipients.length, senders.length);

            List<String> emails = new ArrayList<>();
            for (Address address : all) {
                String to = ((InternetAddress) address).getAddress();
                if (!TextUtils.isEmpty(to)) {
                    to = to.toLowerCase();
                    emails.add(to);
                    String canonical = Helper.canonicalAddress(to);
                    if (!to.equals(canonical))
                        emails.add(canonical);
                }
            }
            String delivered = helper.getDeliveredTo();
            if (!TextUtils.isEmpty(delivered)) {
                delivered = delivered.toLowerCase();
                emails.add(delivered);
                String canonical = Helper.canonicalAddress(delivered);
                if (!delivered.equals(canonical))
                    emails.add(canonical);
            }

            // Search for identity
            EntityIdentity identity = null;
            for (String email : emails) {
                identity = db.identity().getIdentity(folder.account, email);
                if (identity != null)
                    break;
            }

            message = new EntityMessage();
            message.account = folder.account;
            message.folder = folder.id;
            message.identity = (identity == null ? null : identity.id);
            message.uid = uid;

            message.msgid = helper.getMessageID();
            if (TextUtils.isEmpty(message.msgid))
                Log.w(Helper.TAG, "No Message-ID id=" + message.id + " uid=" + message.uid);

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
            message.answered = answered;
            message.flagged = flagged;
            message.keywords = keywords;
            message.ui_seen = seen;
            message.ui_answered = answered;
            message.ui_flagged = flagged;
            message.ui_hide = false;
            message.ui_found = found;
            message.ui_ignored = false;
            message.ui_browsed = browsed;
            message.getAvatar(context);

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
            boolean update = false;

            if (!message.seen.equals(seen) || !message.seen.equals(message.ui_seen)) {
                update = true;
                message.seen = seen;
                message.ui_seen = seen;
                Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid + " seen=" + seen);
            }

            if (!message.answered.equals(answered) || !message.answered.equals(message.ui_answered)) {
                update = true;
                message.answered = answered;
                message.ui_answered = answered;
                Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid + " answered=" + answered);
            }

            if (!message.flagged.equals(flagged) || !message.flagged.equals(message.ui_flagged)) {
                update = true;
                message.flagged = flagged;
                message.ui_flagged = flagged;
                Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid + " flagged=" + flagged);
            }

            if (!Helper.equal(message.keywords, keywords)) {
                update = true;
                message.keywords = keywords;
                Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid +
                        " keywords=" + TextUtils.join(" ", keywords));
            }

            if (message.ui_hide && full) {
                update = true;
                message.ui_hide = false;
                Log.i(Helper.TAG, folder.name + " updated id=" + message.id + " uid=" + message.uid + " unhide");
            }

            boolean noavatar = TextUtils.isEmpty(message.avatar);
            message.getAvatar(context);
            if (noavatar != TextUtils.isEmpty(message.avatar))
                update = true;

            if (update)
                db.message().updateMessage(message);
        }

        return message.id;
    }

    private static void downloadMessage(Context context, EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage, long id) throws MessagingException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long download = prefs.getInt("download", 32768);
        if (download == 0)
            download = Long.MAX_VALUE;

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
            if (!metered || (message.size != null && message.size < download))
                fetch = true;

        if (!fetch)
            for (EntityAttachment attachment : attachments)
                if (!attachment.available)
                    if (!metered || (attachment.size != null && attachment.size < download)) {
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
            if (!metered || (message.size != null && message.size < download)) {
                String html = helper.getHtml();
                String text = (html == null ? null : Jsoup.parse(html).text());
                String preview = (text == null ? null : text.substring(0, Math.min(text.length(), PREVIEW_SIZE)));
                message.write(context, html);
                db.message().setMessageContent(message.id, true, preview);
                Log.i(Helper.TAG, folder.name + " downloaded message id=" + message.id + " size=" + message.size);
            }

        List<EntityAttachment> iattachments = null;
        for (int i = 0; i < attachments.size(); i++) {
            EntityAttachment attachment = attachments.get(i);
            if (!attachment.available)
                if (!metered || (attachment.size != null && attachment.size < download)) {
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
        private boolean started = false;
        private int queued = 0;
        private long lastLost = 0;
        private EntityFolder outbox = null;
        private ExecutorService queue = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);
        private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServiceSynchronize.this);
            boolean metered = prefs.getBoolean("metered", true);

            ConnectivityManager cm = getSystemService(ConnectivityManager.class);
            NetworkCapabilities nc = cm.getNetworkCapabilities(network);
            boolean unmetered = nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);

            if (!started && (metered || unmetered))
                EntityLog.log(ServiceSynchronize.this, "Network " + network + " capabilities " + capabilities);

            if (!started && suitableNetwork())
                queue_reload(true, "connect " + network);
        }

        @Override
        public void onAvailable(Network network) {
            ConnectivityManager cm = getSystemService(ConnectivityManager.class);
            EntityLog.log(ServiceSynchronize.this, "Available " + network + " " + cm.getNetworkInfo(network));

            if (!started && suitableNetwork())
                queue_reload(true, "connect " + network);
        }

        @Override
        public void onLost(Network network) {
            EntityLog.log(ServiceSynchronize.this, "Lost " + network);

            if (started && !suitableNetwork()) {
                lastLost = new Date().getTime();
                queue_reload(false, "disconnect " + network);
            }
        }

        private boolean suitableNetwork() {
            ConnectivityManager cm = getSystemService(ConnectivityManager.class);
            Network network = cm.getActiveNetwork();
            NetworkCapabilities nc = (network == null ? null : cm.getNetworkCapabilities(network));
            boolean unmetered = (!cm.isActiveNetworkMetered() ||
                    (nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServiceSynchronize.this);
            boolean metered = prefs.getBoolean("metered", true);

            // The connected state is deliberately ignored
            return (metered || unmetered);
        }

        private boolean isEnabled() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServiceSynchronize.this);
            return prefs.getBoolean("enabled", true);
        }

        private void service_init() {
            EntityLog.log(ServiceSynchronize.this, "Service init");
        }

        private void service_destroy() {
            EntityLog.log(ServiceSynchronize.this, "Service destroy");
            if (started)
                queue_reload(false, "service destroy");
        }

        private void start() {
            EntityLog.log(ServiceSynchronize.this, "Main start");

            state = new ServiceState();
            state.runnable(new Runnable() {
                PowerManager pm = getSystemService(PowerManager.class);
                PowerManager.WakeLock wl = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        BuildConfig.APPLICATION_ID + ":start");
                private List<ServiceState> threadState = new ArrayList<>();

                @Override
                public void run() {
                    try {
                        wl.acquire();

                        final DB db = DB.getInstance(ServiceSynchronize.this);

                        outbox = db.folder().getOutbox();
                        if (outbox == null) {
                            EntityLog.log(ServiceSynchronize.this, "No outbox");
                            return;
                        }

                        long ago = new Date().getTime() - lastLost;
                        if (ago < RECONNECT_BACKOFF)
                            try {
                                long backoff = RECONNECT_BACKOFF - ago;
                                EntityLog.log(ServiceSynchronize.this, "Main backoff=" + (backoff / 1000));
                                if (state.acquire(backoff))
                                    return;
                            } catch (InterruptedException ex) {
                                Log.w(Helper.TAG, "main backoff " + ex.toString());
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
                        List<EntityAccount> accounts = db.account().getAccounts(true);
                        for (final EntityAccount account : accounts) {
                            Log.i(Helper.TAG, account.host + "/" + account.user + " run");
                            final ServiceState astate = new ServiceState();
                            astate.runnable(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        monitorAccount(account, astate);
                                    } catch (Throwable ex) {
                                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                        EntityLog.log(ServiceSynchronize.this, account.name + " " + Helper.formatThrowable(ex));
                                        db.account().setAccountError(account.id, Helper.formatThrowable(ex));
                                    }
                                }
                            }, "sync.account." + account.id);
                            astate.start();
                            threadState.add(astate);
                        }

                        EntityLog.log(ServiceSynchronize.this, "Main started");

                        try {
                            wl.release();
                            state.acquire();
                        } catch (InterruptedException ex) {
                            Log.w(Helper.TAG, "main wait " + ex.toString());
                        } finally {
                            wl.acquire();
                        }

                        // Stop monitoring accounts
                        for (ServiceState astate : threadState)
                            astate.stop();
                        for (ServiceState astate : threadState)
                            astate.join();
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
            state.start();
        }

        private void stop() {
            EntityLog.log(ServiceSynchronize.this, "Main stop");

            state.stop();
            state.join();

            EntityLog.log(ServiceSynchronize.this, "Main stopped");

            state = null;
        }

        private void queue_reload(final boolean start, final String reason) {
            final boolean doStop = started;
            final boolean doStart = (start && isEnabled() && suitableNetwork());

            if (!doStop && !doStart)
                return;

            EntityLog.log(ServiceSynchronize.this, "Queue reload " +
                    " doStop=" + doStop + " doStart=" + doStart + " queued=" + queued + " " + reason);

            queued++;
            queue.submit(new Runnable() {
                PowerManager pm = getSystemService(PowerManager.class);
                PowerManager.WakeLock wl = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        BuildConfig.APPLICATION_ID + ":reload");

                @Override
                public void run() {
                    EntityLog.log(ServiceSynchronize.this, "Reload " +
                            " stop=" + doStop + " start=" + doStart + " queued=" + queued + " " + reason);

                    try {
                        wl.acquire();

                        if (doStop)
                            stop();

                        if (doStart)
                            start();

                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    } finally {
                        wl.release();

                        queued--;
                        EntityLog.log(ServiceSynchronize.this, "Reload done queued=" + queued);

                        if (queued == 0 && !isEnabled()) {
                            try {
                                Thread.sleep(STOP_DELAY);
                            } catch (InterruptedException ignored) {
                            }
                            if (queued == 0 && !isEnabled()) {
                                EntityLog.log(ServiceSynchronize.this, "Service stop");
                                stopSelf();
                            }
                        }
                    }
                }
            });

            started = doStart;
        }

        private BroadcastReceiver outboxReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                Log.v(Helper.TAG, outbox.name + " run operations");

                executor.submit(new Runnable() {
                    PowerManager pm = getSystemService(PowerManager.class);
                    PowerManager.WakeLock wl = pm.newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK,
                            BuildConfig.APPLICATION_ID + ":outbox");

                    @Override
                    public void run() {
                        try {
                            wl.acquire();
                            DB db = DB.getInstance(context);
                            try {
                                Log.i(Helper.TAG, outbox.name + " start operations");
                                db.folder().setFolderState(outbox.id, "syncing");
                                processOperations(outbox, null, null, null, state);
                                db.folder().setFolderError(outbox.id, null);
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

    public static void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("enabled", true)) {
            ContextCompat.startForegroundService(context,
                    new Intent(context, ServiceSynchronize.class)
                            .setAction("init"));
            JobDaily.schedule(context);
        }
    }

    public static void reload(Context context, String reason) {
        ContextCompat.startForegroundService(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("reload")
                        .putExtra("reason", reason));
        JobDaily.schedule(context);
    }

    private class ServiceState {
        private Thread thread;
        private Semaphore semaphore = new Semaphore(0);
        private boolean running = true;
        private boolean error = false;

        void runnable(Runnable runnable, String name) {
            thread = new Thread(runnable, name);
        }

        void release() {
            semaphore.release();
            yield();
        }

        void acquire() throws InterruptedException {
            semaphore.acquire();
        }

        boolean acquire(long milliseconds) throws InterruptedException {
            return semaphore.tryAcquire(milliseconds, TimeUnit.MILLISECONDS);
        }

        void error() {
            error = true;
            thread.interrupt();
            yield();
        }

        void yield() {
            try {
                // Give interrupted thread some time to acquire wake lock
                Thread.sleep(500L);
            } catch (InterruptedException ignored) {
            }
        }

        void reset() {
            error = false;
        }

        void start() {
            thread.setPriority(THREAD_PRIORITY_BACKGROUND);
            thread.start();
            yield();
        }

        void stop() {
            running = false;
            semaphore.release();
        }

        void join() {
            join(thread);
        }

        boolean running() {
            return running;
        }

        boolean alive() {
            return (running && !error);
        }

        void join(Thread thread) {
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

        @NonNull
        @Override
        public String toString() {
            return "[running=" + running + " error=" + error + "]";
        }
    }
}
