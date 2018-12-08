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
import android.os.Handler;
import android.os.Looper;
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
import java.util.Collections;
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
import javax.mail.search.FlagTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.net.ssl.SSLException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class ServiceSynchronize extends LifecycleService {
    private TupleAccountStats lastStats = null;
    private ServiceManager serviceManager = new ServiceManager();
    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private static final int NOTIFICATION_SYNCHRONIZE = 1;

    private static final int CONNECT_BACKOFF_START = 8; // seconds
    private static final int CONNECT_BACKOFF_MAX = 64; // seconds (totally 2 minutes)
    private static final int CONNECT_BACKOFF_AlARM = 15; // minutes
    private static final int SYNC_BATCH_SIZE = 20;
    private static final int DOWNLOAD_BATCH_SIZE = 20;
    private static final long RECONNECT_BACKOFF = 90 * 1000L; // milliseconds
    private static final int PREVIEW_SIZE = 250;
    private static final int ACCOUNT_ERROR_AFTER = 90; // minutes
    private static final int IDENTITY_ERROR_AFTER = 30; // minutes
    private static final long STOP_DELAY = 5000L; // milliseconds

    static final int PI_WHY = 1;
    static final int PI_CLEAR = 2;
    static final int PI_SEEN = 3;
    static final int PI_ARCHIVE = 4;
    static final int PI_TRASH = 5;
    static final int PI_IGNORED = 6;

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
            public void onChanged(final List<TupleMessageEx> messages) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
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
                                    if (id != 0) {
                                        all.add(id);
                                        if (removed.contains(id))
                                            removed.remove(id);
                                        else {
                                            removed.remove(Integer.valueOf(-id));
                                            added.add(id);
                                        }
                                    }
                                }

                                if (notifications.size() == 0)
                                    nm.cancel("unseen:" + account, 0);

                                for (Integer id : removed)
                                    nm.cancel("unseen:" + account, Math.abs(id));

                                for (Notification notification : notifications) {
                                    Integer id = (int) notification.extras.getLong("id", 0);
                                    if ((id == 0 && added.size() + removed.size() > 0) || added.contains(id))
                                        nm.notify("unseen:" + account, Math.abs(id), notification);
                                }

                                notifying.put(account, all);
                            }
                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                        }
                    }
                });
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
            final String[] parts = action.split(":");
            switch (parts[0]) {
                case "why":
                    Intent why = new Intent(Intent.ACTION_VIEW);
                    why.setData(Uri.parse("https://github.com/M66B/open-source-email/blob/master/FAQ.md#user-content-faq2"));
                    why.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    PackageManager pm = getPackageManager();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    if (prefs.getBoolean("why", false) || why.resolveActivity(pm) == null) {
                        Intent view = new Intent(this, ActivityView.class);
                        view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(view);
                    } else {
                        prefs.edit().putBoolean("why", true).apply();
                        startActivity(why);
                    }
                    break;

                case "init":
                    // Network events will manage the service
                    serviceManager.service_init();
                    break;

                case "reload":
                    serviceManager.queue_reload(true, intent.getStringExtra("reason"));
                    break;

                case "clear":
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            DB.getInstance(ServiceSynchronize.this).message().ignoreAll();
                        }
                    });
                    break;

                case "seen":
                case "archive":
                case "trash":
                case "ignore":
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            long id = Long.parseLong(parts[1]);

                            DB db = DB.getInstance(ServiceSynchronize.this);
                            try {
                                db.beginTransaction();

                                EntityMessage message = db.message().getMessage(id);
                                switch (parts[0]) {
                                    case "seen":
                                        EntityOperation.queue(db, message, EntityOperation.SEEN, true);
                                        break;

                                    case "archive":
                                        EntityFolder archive = db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE);
                                        if (archive == null)
                                            archive = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                                        if (archive != null) {
                                            EntityOperation.queue(db, message, EntityOperation.SEEN, true);
                                            EntityOperation.queue(db, message, EntityOperation.MOVE, archive.id);
                                        }
                                        break;

                                    case "trash":
                                        EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                                        if (trash != null) {
                                            EntityOperation.queue(db, message, EntityOperation.SEEN, true);
                                            EntityOperation.queue(db, message, EntityOperation.MOVE, trash.id);
                                        }
                                        break;

                                    case "ignore":
                                        db.message().setMessageUiIgnored(message.id, true);
                                        break;

                                    default:
                                        Log.w(Helper.TAG, "Unknown action: " + parts[0]);
                                }

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }
                        }
                    });
                    break;

                default:
                    Log.w(Helper.TAG, "Unknown action: " + action);
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

        String summary = getResources().getQuantityString(
                R.plurals.title_notification_unseen, messages.size(), messages.size());

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
                .setContentTitle(summary)
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

            builder.setStyle(new Notification.BigTextStyle()
                    .bigText(Html.fromHtml(sb.toString()))
                    .setSummaryText(summary));
        }

        notifications.add(builder.build());

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        for (TupleMessageEx message : messages) {
            Bundle args = new Bundle();
            args.putLong("id", message.content ? message.id : -message.id);

            Intent thread = new Intent(this, ActivityView.class);
            thread.setAction("thread:" + message.thread);
            thread.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            thread.putExtra("account", message.account);
            thread.putExtra("id", message.id);
            PendingIntent piContent = PendingIntent.getActivity(
                    this, ActivityView.REQUEST_THREAD, thread, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent ignored = new Intent(this, ServiceSynchronize.class);
            ignored.setAction("ignore:" + message.id);
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
                    .setOnlyAlertOnce(true)
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

                if (message.content)
                    try {
                        String html = message.read(ServiceSynchronize.this);
                        StringBuilder sb = new StringBuilder();
                        if (!TextUtils.isEmpty(message.subject))
                            sb.append(message.subject).append("<br>");
                        sb.append(Jsoup.parse(html).text());
                        mbuilder.setStyle(new Notification.BigTextStyle().bigText(Html.fromHtml(sb.toString())));
                    } catch (IOException ex) {
                        Log.e(Helper.TAG, ex + "/n" + Log.getStackTraceString(ex));
                        mbuilder.setStyle(new Notification.BigTextStyle().bigText(ex.toString()));
                    }

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

    private Notification.Builder getNotificationError(String title, Throwable ex) {
        return getNotificationError(title, new Date().getTime(), ex, true);
    }

    private Notification.Builder getNotificationError(String title, long when, Throwable ex, boolean debug) {
        // Build pending intent
        Intent intent = new Intent(this, ActivitySetup.class);
        if (debug)
            intent.setAction("error");
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
                .setContentTitle(getString(R.string.title_notification_failed, title))
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

    private void reportError(EntityAccount account, EntityFolder folder, Throwable ex) {
        // FolderClosedException: can happen when no connectivity

        // IllegalStateException:
        // - "This operation is not allowed on a closed folder"
        // - can happen when syncing message

        // ConnectionException
        // - failed to create new store connection (connectivity)

        // MailConnectException
        // - on connectivity problems when connecting to store

        String title;
        if (account == null)
            title = folder.name;
        else if (folder == null)
            title = account.name;
        else
            title = account.name + "/" + folder.name;

        String tag = "error:" + (account == null ? 0 : account.id) + ":" + (folder == null ? 0 : folder.id);

        EntityLog.log(this, title + " " + Helper.formatThrowable(ex));

        if ((ex instanceof SendFailedException) || (ex instanceof AlertException)) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.notify(tag, 1, getNotificationError(title, ex).build());
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
            nm.notify(tag, 1, getNotificationError(title, ex).build());
        }
    }

    private void monitorAccount(final EntityAccount account, final ServiceState state) throws NoSuchProviderException {
        final PowerManager pm = getSystemService(PowerManager.class);
        final PowerManager.WakeLock wlAccount = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id);
        try {
            wlAccount.acquire();

            final DB db = DB.getInstance(this);

            int backoff = CONNECT_BACKOFF_START;
            while (state.running()) {
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
                List<Thread> idlers = new ArrayList<>();
                List<Handler> handlers = new ArrayList<>();
                try {
                    // Listen for store events
                    istore.addStoreListener(new StoreListener() {
                        @Override
                        public void notification(StoreEvent e) {
                            try {
                                wlAccount.acquire();
                                String type = (e.getMessageType() == StoreEvent.ALERT ? "alert" : "notice");
                                EntityLog.log(ServiceSynchronize.this, account.name + " " + type + ": " + e.getMessage());
                                if (e.getMessageType() == StoreEvent.ALERT) {
                                    db.account().setAccountError(account.id, e.getMessage());
                                    reportError(account, null, new AlertException(e.getMessage()));
                                    state.error();
                                }
                            } finally {
                                wlAccount.release();
                            }
                        }
                    });

                    // Listen for folder events
                    istore.addFolderListener(new FolderAdapter() {
                        @Override
                        public void folderCreated(FolderEvent e) {
                            try {
                                wlAccount.acquire();
                                Log.i(Helper.TAG, "Folder created=" + e.getFolder().getFullName());
                                reload(ServiceSynchronize.this, "folder created");
                            } finally {
                                wlAccount.release();
                            }
                        }

                        @Override
                        public void folderRenamed(FolderEvent e) {
                            try {
                                wlAccount.acquire();
                                Log.i(Helper.TAG, "Folder renamed=" + e.getFolder().getFullName());

                                String old = e.getFolder().getFullName();
                                String name = e.getNewFolder().getFullName();
                                int count = db.folder().renameFolder(account.id, old, name);
                                Log.i(Helper.TAG, "Renamed to " + name + " count=" + count);

                                reload(ServiceSynchronize.this, "folder renamed");
                            } finally {
                                wlAccount.release();
                            }
                        }

                        @Override
                        public void folderDeleted(FolderEvent e) {
                            try {
                                wlAccount.acquire();
                                Log.i(Helper.TAG, "Folder deleted=" + e.getFolder().getFullName());
                                EntityFolder folder = db.folder().getFolderByName(account.id, e.getFolder().getFullName());
                                if (folder != null)
                                    db.folder().setFolderTbd(folder.id);
                                reload(ServiceSynchronize.this, "folder deleted");
                            } finally {
                                wlAccount.release();
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
                            long delayed = now - account.last_connected;
                            if (delayed > ACCOUNT_ERROR_AFTER * 60 * 1000L) {
                                Log.i(Helper.TAG, "Reporting sync error after=" + delayed);
                                NotificationManager nm = getSystemService(NotificationManager.class);
                                nm.notify("receive", account.id.intValue(),
                                        getNotificationError(account.name, account.last_connected, ex, false).build());
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

                    // Open synchronizing folders
                    final ExecutorService pollExecutor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);
                    for (final EntityFolder folder : db.folder().getFolders(account.id)) {
                        if (folder.synchronize && !folder.poll && capIdle) {
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

                            Log.i(Helper.TAG, account.name + " folder " + folder.name + " flags=" + ifolder.getPermanentFlags());

                            // Listen for new and deleted messages
                            ifolder.addMessageCountListener(new MessageCountAdapter() {
                                @Override
                                public void messagesAdded(MessageCountEvent e) {
                                    try {
                                        wlAccount.acquire();
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
                                                EntityMessage message;
                                                try {
                                                    db.beginTransaction();
                                                    message = synchronizeMessage(
                                                            ServiceSynchronize.this,
                                                            folder, ifolder, (IMAPMessage) imessage, false, false);
                                                    db.setTransactionSuccessful();
                                                } finally {
                                                    db.endTransaction();
                                                }

                                                Boolean isMetered = Helper.isMetered(ServiceSynchronize.this);
                                                boolean metered = (isMetered == null || isMetered);

                                                try {
                                                    db.beginTransaction();
                                                    downloadMessage(
                                                            ServiceSynchronize.this, metered,
                                                            folder, ifolder, (IMAPMessage) imessage, message.id);
                                                    db.setTransactionSuccessful();
                                                } finally {
                                                    db.endTransaction();
                                                }
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
                                        reportError(account, folder, ex);
                                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                        state.error();
                                    } finally {
                                        wlAccount.release();
                                    }
                                }

                                @Override
                                public void messagesRemoved(MessageCountEvent e) {
                                    try {
                                        wlAccount.acquire();
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
                                        reportError(account, folder, ex);
                                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                        state.error();
                                    } finally {
                                        wlAccount.release();
                                    }
                                }
                            });

                            // Flags (like "seen") at the remote could be changed while synchronizing

                            // Listen for changed messages
                            ifolder.addMessageChangedListener(new MessageChangedListener() {
                                @Override
                                public void messageChanged(MessageChangedEvent e) {
                                    try {
                                        wlAccount.acquire();
                                        try {
                                            Log.i(Helper.TAG, folder.name + " message changed");

                                            FetchProfile fp = new FetchProfile();
                                            fp.add(UIDFolder.FetchProfileItem.UID);
                                            fp.add(IMAPFolder.FetchProfileItem.FLAGS);
                                            ifolder.fetch(new Message[]{e.getMessage()}, fp);

                                            EntityMessage message;
                                            try {
                                                db.beginTransaction();
                                                message = synchronizeMessage(
                                                        ServiceSynchronize.this,
                                                        folder, ifolder, (IMAPMessage) e.getMessage(), false, false);
                                                db.setTransactionSuccessful();
                                            } finally {
                                                db.endTransaction();
                                            }

                                            Boolean isMetered = Helper.isMetered(ServiceSynchronize.this);
                                            boolean metered = (isMetered == null || isMetered);

                                            try {
                                                db.beginTransaction();
                                                downloadMessage(
                                                        ServiceSynchronize.this, metered,
                                                        folder, ifolder, (IMAPMessage) e.getMessage(), message.id);
                                                db.setTransactionSuccessful();
                                            } finally {
                                                db.endTransaction();
                                            }
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
                                        reportError(account, folder, ex);
                                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                        state.error();
                                    } finally {
                                        wlAccount.release();
                                    }
                                }
                            });

                            // Idle folder
                            Thread idler = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Log.i(Helper.TAG, folder.name + " start idle");
                                        while (state.running()) {
                                            Log.v(Helper.TAG, folder.name + " do idle");
                                            ifolder.idle(false);
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                        reportError(account, folder, ex);
                                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                        state.error();
                                    } finally {
                                        Log.i(Helper.TAG, folder.name + " end idle");
                                    }
                                }
                            }, "idler." + folder.id);
                            idler.start();
                            idlers.add(idler);

                            EntityOperation.sync(db, folder.id);
                        } else
                            folders.put(folder, null);

                        // Observe operations
                        Handler handler = new Handler(getMainLooper()) {
                            private LiveData<List<EntityOperation>> liveOperations;

                            @Override
                            public void handleMessage(android.os.Message msg) {
                                Log.i(Helper.TAG, account.name + "/" + folder.name + " observe=" + msg.what);
                                try {
                                    if (msg.what == 0)
                                        liveOperations.removeObserver(observer);
                                    else {
                                        liveOperations = db.operation().liveOperations(folder.id);
                                        liveOperations.observe(ServiceSynchronize.this, observer);
                                    }
                                } catch (Throwable ex) {
                                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                }
                            }

                            private Observer<List<EntityOperation>> observer = new Observer<List<EntityOperation>>() {
                                private List<Long> handling = new ArrayList<>();
                                private final ExecutorService folderExecutor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);
                                private final PowerManager.WakeLock wlFolder = pm.newWakeLock(
                                        PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":folder." + folder.id);

                                @Override
                                public void onChanged(List<EntityOperation> operations) {
                                    boolean process = false;
                                    List<Long> current = new ArrayList<>();
                                    for (EntityOperation op : operations) {
                                        if (!handling.contains(op.id))
                                            process = true;
                                        current.add(op.id);
                                    }
                                    handling = current;

                                    if (handling.size() > 0 && process) {
                                        Log.i(Helper.TAG, folder.name + " operations=" + operations.size());
                                        (folder.poll ? pollExecutor : folderExecutor).submit(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    wlFolder.acquire();
                                                    Log.i(Helper.TAG, folder.name + " process");

                                                    // Get folder
                                                    IMAPFolder ifolder = folders.get(folder); // null when polling
                                                    final boolean shouldClose = (ifolder == null);

                                                    try {
                                                        Log.i(Helper.TAG, folder.name + " run " + (shouldClose ? "offline" : "online"));

                                                        if (ifolder == null) {
                                                            // Prevent unnecessary folder connections
                                                            if (db.operation().getOperationCount(folder.id, null) == 0)
                                                                return;

                                                            db.folder().setFolderState(folder.id, "connecting");

                                                            ifolder = (IMAPFolder) istore.getFolder(folder.name);
                                                            ifolder.open(Folder.READ_WRITE);

                                                            db.folder().setFolderState(folder.id, "connected");
                                                            db.folder().setFolderError(folder.id, null);
                                                        }

                                                        processOperations(account, folder, isession, istore, ifolder, state);

                                                    } catch (Throwable ex) {
                                                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                                        reportError(account, folder, ex);
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
                                                    wlFolder.release();
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public boolean equals(@Nullable Object obj) {
                                    boolean eq = super.equals(obj);
                                    Log.i(Helper.TAG, account.name + "/" + folder.name + " equal=" + eq + " observer=" + observer + " other=" + obj);
                                    return eq;
                                }
                            };
                        };

                        // Start watching for operations
                        handler.sendEmptyMessage(1);
                        handlers.add(handler);
                    }

                    // Keep alive alarm receiver
                    BroadcastReceiver alarm = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            // Receiver runs on main thread
                            // Receiver has a wake lock for ~10 seconds
                            EntityLog.log(context, account.name + " keep alive wake lock=" + wlAccount.isHeld());
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
                                if (folder.synchronize)
                                    if (!folder.poll && capIdle) {
                                        if (!folders.get(folder).isOpen())
                                            throw new FolderClosedException(folders.get(folder));
                                    } else
                                        EntityOperation.sync(db, folder.id);

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
                                wlAccount.release();
                                state.acquire();
                            } catch (InterruptedException ex) {
                                EntityLog.log(this, account.name + " waited state=" + state);
                            } finally {
                                wlAccount.acquire();
                            }
                        }
                    } finally {
                        // Cleanup
                        am.cancel(pi);
                        unregisterReceiver(alarm);
                    }

                    Log.i(Helper.TAG, account.name + " done state=" + state);
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, account.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    reportError(account, null, ex);

                    EntityLog.log(ServiceSynchronize.this, account.name + " " + Helper.formatThrowable(ex));
                    db.account().setAccountError(account.id, Helper.formatThrowable(ex));
                } finally {
                    // Stop watching for operations
                    for (Handler handler : handlers)
                        handler.sendEmptyMessage(0);
                    handlers.clear();

                    EntityLog.log(this, account.name + " closing");
                    db.account().setAccountState(account.id, "closing");
                    for (EntityFolder folder : folders.keySet())
                        if (folder.synchronize && !folder.poll)
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

                    // Stop idlers
                    for (Thread idler : idlers)
                        state.join(idler);
                    idlers.clear();

                    for (EntityFolder folder : folders.keySet())
                        if (folder.synchronize && !folder.poll)
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
                                    wlAccount.release();
                                    state.acquire(2 * CONNECT_BACKOFF_AlARM * 60 * 1000L);
                                } finally {
                                    wlAccount.acquire();
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
            wlAccount.release();
        }
    }

    private void processOperations(EntityAccount account, EntityFolder folder, Session isession, IMAPStore istore, IMAPFolder ifolder, ServiceState state) throws MessagingException, JSONException, IOException {
        try {
            Log.i(Helper.TAG, folder.name + " start process");

            DB db = DB.getInstance(this);
            List<EntityOperation> ops = db.operation().getOperationsByFolder(
                    folder.id, EntityFolder.OUTBOX.equals(folder.type));
            Log.i(Helper.TAG, folder.name + " pending operations=" + ops.size());
            for (int i = 0; i < ops.size() && state.running(); i++) {
                EntityOperation op = ops.get(i);
                try {
                    Log.i(Helper.TAG, folder.name +
                            " start op=" + op.id + "/" + op.name +
                            " msg=" + op.message +
                            " args=" + op.args);

                    // Fetch most recent copy of message
                    EntityMessage message = null;
                    if (op.message != null)
                        message = db.message().getMessage(op.message);

                    try {
                        if (message == null && !EntityOperation.SYNC.equals(op.name))
                            throw new MessageRemovedException();

                        db.operation().setOperationError(op.id, null);
                        if (message != null)
                            db.message().setMessageError(message.id, null);

                        if (message != null && message.uid == null &&
                                !(EntityOperation.ADD.equals(op.name) ||
                                        EntityOperation.SEND.equals(op.name) ||
                                        EntityOperation.SYNC.equals(op.name)))
                            throw new IllegalArgumentException(op.name + " without uid " + op.args);

                        JSONArray jargs = new JSONArray(op.args);

                        // Operations should use database transaction when needed

                        if (EntityOperation.SEEN.equals(op.name))
                            doSeen(folder, ifolder, message, jargs, db);

                        else if (EntityOperation.FLAG.equals(op.name))
                            doFlag(folder, ifolder, message, jargs, db);

                        else if (EntityOperation.ANSWERED.equals(op.name))
                            doAnswered(folder, ifolder, message, jargs, db);

                        else if (EntityOperation.KEYWORD.equals(op.name))
                            doKeyword(folder, ifolder, message, jargs, db);

                        else if (EntityOperation.ADD.equals(op.name))
                            doAdd(folder, isession, istore, ifolder, message, jargs, db);

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

                        else if (EntityOperation.SYNC.equals(op.name))
                            if (EntityFolder.OUTBOX.equals(folder.type))
                                db.folder().setFolderError(folder.id, null);
                            else
                                synchronizeMessages(account, folder, ifolder, state);

                        else
                            throw new MessagingException("Unknown operation name=" + op.name);

                        // Operation succeeded
                        db.operation().deleteOperation(op.id);
                    } catch (Throwable ex) {
                        // TODO: SMTP response codes: https://www.ietf.org/rfc/rfc821.txt
                        reportError(account, folder, ex);

                        db.operation().setOperationError(op.id, Helper.formatThrowable(ex));

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

        try {
            db.beginTransaction();

            message = db.message().getMessage(message.id);

            List<String> keywords = new ArrayList<>(Arrays.asList(message.keywords));
            if (set) {
                if (!keywords.contains(keyword))
                    keywords.add(keyword);
            } else
                keywords.remove(keyword);
            db.message().setMessageKeywords(message.id, DB.Converters.fromStringArray(keywords.toArray(new String[0])));

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void doAdd(EntityFolder folder, Session isession, IMAPStore istore, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, JSONException, IOException {
        // Append message
        MimeMessage imessage = MessageHelper.from(this, message, isession);

        if (EntityFolder.DRAFTS.equals(folder.type) && ifolder.getPermanentFlags().contains(Flags.Flag.DRAFT))
            imessage.setFlag(Flags.Flag.DRAFT, true);

        if (istore.hasCapability("UIDPLUS")) {
            AppendUID[] uid = ifolder.appendUIDMessages(new Message[]{imessage});
            Log.i(Helper.TAG, "Appended uid=" + uid[0].uid + " draft=" + imessage.getFlags().contains(Flags.Flag.DRAFT));
            db.message().setMessageUid(message.id, uid[0].uid);
        } else {
            ifolder.appendMessages(new Message[]{imessage});
            db.message().setMessageUid(message.id, null);
        }

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

            // Delete source
            imessage.setFlag(Flags.Flag.DELETED, true);
            ifolder.expunge();

            // Append target
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

        // defacto standard
        if (ident.delivery_receipt)
            imessage.addHeader("Return-Receipt-To", ident.replyto == null ? ident.email : ident.replyto);

        // https://tools.ietf.org/html/rfc3798
        if (ident.read_receipt)
            imessage.addHeader("Disposition-Notification-To", ident.replyto == null ? ident.email : ident.replyto);

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
            nm.cancel("send", message.identity.intValue());

            // Send message
            Address[] to = imessage.getAllRecipients();
            itransport.sendMessage(imessage, to);
            Log.i(Helper.TAG, "Sent via " + ident.host + "/" + ident.user +
                    " to " + TextUtils.join(", ", to));

            try {
                db.beginTransaction();

                // Message could be moved
                message = db.message().getMessage(message.id);

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
        } catch (MessagingException ex) {
            if (ex instanceof SendFailedException) {
                SendFailedException sfe = (SendFailedException) ex;

                StringBuilder sb = new StringBuilder();

                sb.append(sfe.getMessage());

                sb.append(' ').append(getString(R.string.title_address_sent));
                sb.append(' ').append(MessageHelper.getFormattedAddresses(sfe.getValidSentAddresses(), true));

                sb.append(' ').append(getString(R.string.title_address_unsent));
                sb.append(' ').append(MessageHelper.getFormattedAddresses(sfe.getValidUnsentAddresses(), true));

                sb.append(' ').append(getString(R.string.title_address_invalid));
                sb.append(' ').append(MessageHelper.getFormattedAddresses(sfe.getInvalidAddresses(), true));

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
            if (delayed > IDENTITY_ERROR_AFTER * 60 * 1000L) {
                Log.i(Helper.TAG, "Reporting send error after=" + delayed);
                NotificationManager nm = getSystemService(NotificationManager.class);
                nm.notify("send", message.identity.intValue(), getNotificationError(ident.name, ex).build());
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
                if (folder.tbd == null)
                    names.add(folder.name);
                else {
                    IMAPFolder ifolder = (IMAPFolder) istore.getFolder(folder.name);
                    if (ifolder.exists())
                        ifolder.delete(false);
                    db.folder().deleteFolder(folder.id);
                }
            Log.i(Helper.TAG, "Local folder count=" + names.size());

            Folder defaultFolder = istore.getDefaultFolder();
            char separator = defaultFolder.getSeparator();
            Folder[] ifolders = defaultFolder.list("*");
            Log.i(Helper.TAG, "Remote folder count=" + ifolders.length + " separator=" + separator);

            for (Folder ifolder : ifolders) {
                boolean selectable = true;
                String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                for (String attr : attrs) {
                    if ("\\Noselect".equals(attr))
                        selectable = false;
                }

                if (selectable) {
                    String fullName = ifolder.getFullName();
                    int level = EntityFolder.getLevel(separator, fullName);
                    EntityFolder folder = db.folder().getFolderByName(account.id, fullName);
                    if (folder == null) {
                        folder = new EntityFolder();
                        folder.account = account.id;
                        folder.name = fullName;
                        folder.type = EntityFolder.USER;
                        folder.level = level;
                        folder.synchronize = false;
                        folder.poll = ("imap.gmail.com".equals(account.host));
                        folder.sync_days = EntityFolder.DEFAULT_USER_SYNC;
                        folder.keep_days = EntityFolder.DEFAULT_USER_SYNC;
                        db.folder().insertFolder(folder);
                        Log.i(Helper.TAG, folder.name + " added");
                    } else {
                        names.remove(folder.name);
                        Log.i(Helper.TAG, folder.name + " exists");
                        db.folder().setFolderLevel(folder.id, level);
                    }
                }
            }

            Log.i(Helper.TAG, "Create remote count=" + names.size());
            for (String name : names) {
                Log.i(Helper.TAG, name + " create");
                IMAPFolder ifolder = (IMAPFolder) istore.getFolder(name);
                ifolder.create(Folder.HOLDS_MESSAGES);
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
            // Refresh parameters
            folder = db.folder().getFolder(folder.id);

            Log.v(Helper.TAG, folder.name + " start sync after=" + folder.sync_days + "/" + folder.keep_days);

            db.folder().setFolderSyncState(folder.id, "syncing");

            // Get reference times
            Calendar cal_sync = Calendar.getInstance();
            cal_sync.add(Calendar.DAY_OF_MONTH, -folder.sync_days);
            cal_sync.set(Calendar.HOUR_OF_DAY, 0);
            cal_sync.set(Calendar.MINUTE, 0);
            cal_sync.set(Calendar.SECOND, 0);
            cal_sync.set(Calendar.MILLISECOND, 0);

            Calendar cal_keep = Calendar.getInstance();
            cal_keep.add(Calendar.DAY_OF_MONTH, -folder.keep_days);
            cal_keep.set(Calendar.HOUR_OF_DAY, 12);
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
            Message[] imessages = ifolder.search(
                    new OrTerm(
                            new ReceivedDateTerm(ComparisonTerm.GE, new Date(sync_time)),
                            new FlagTerm(new Flags(Flags.Flag.FLAGGED), true)
                    )
            );
            Log.i(Helper.TAG, folder.name + " remote count=" + imessages.length +
                    " search=" + (SystemClock.elapsedRealtime() - search) + " ms");

            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(FetchProfile.Item.FLAGS);
            ifolder.fetch(imessages, fp);

            long fetch = SystemClock.elapsedRealtime();
            Log.i(Helper.TAG, folder.name + " remote fetched=" + (SystemClock.elapsedRealtime() - fetch) + " ms");

            for (int i = 0; i < imessages.length && state.running(); i++)
                try {
                    uids.remove(ifolder.getUID(imessages[i]));
                } catch (MessageRemovedException ex) {
                    Log.w(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    reportError(account, folder, ex);
                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
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
            for (int i = imessages.length - 1; i >= 0 && state.running(); i -= SYNC_BATCH_SIZE) {
                int from = Math.max(0, i - SYNC_BATCH_SIZE + 1);
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

                for (int j = isub.length - 1; j >= 0 && state.running(); j--)
                    try {
                        db.beginTransaction();
                        EntityMessage message = synchronizeMessage(
                                this,
                                folder, ifolder, (IMAPMessage) isub[j], false, true);
                        ids[from + j] = message.id;
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

                if (state.running())
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
            }

            db.folder().setFolderSyncState(folder.id, "downloading");

            //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);

            // Download messages/attachments
            Log.i(Helper.TAG, folder.name + " download=" + imessages.length);
            for (int i = imessages.length - 1; i >= 0 && state.running(); i -= DOWNLOAD_BATCH_SIZE) {
                int from = Math.max(0, i - DOWNLOAD_BATCH_SIZE + 1);

                Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);
                // Fetch on demand

                Boolean isMetered = Helper.isMetered(ServiceSynchronize.this);
                boolean metered = (isMetered == null || isMetered);

                for (int j = isub.length - 1; j >= 0 && state.running(); j--)
                    try {
                        db.beginTransaction();
                        if (ids[from + j] != null) {
                            downloadMessage(
                                    this, metered,
                                    folder, ifolder, (IMAPMessage) isub[j], ids[from + j]);
                            Thread.sleep(20);
                        }
                        db.setTransactionSuccessful();
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (FolderClosedIOException ex) {
                        throw ex;
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, folder.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                    } finally {
                        db.endTransaction();
                        // Free memory
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }

                if (state.running())
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
            }

            db.folder().setFolderError(folder.id, null);
        } finally {
            Log.v(Helper.TAG, folder.name + " end sync state=" + state);
            db.folder().setFolderSyncState(folder.id, null);
        }
    }

    static EntityMessage synchronizeMessage(
            Context context,
            EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage,
            boolean browsed, boolean full) throws MessagingException, IOException {
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
        EntityMessage message = db.message().getMessageByUid(folder.id, uid);

        // Find message by Message-ID (slow, headers required)
        // - messages in inbox have same id as message sent to self
        // - messages in archive have same id as original
        if (message == null) {
            // Will fetch headers within database transaction
            String msgid = helper.getMessageID();
            Log.i(Helper.TAG, "Searching for " + msgid);
            for (EntityMessage dup : db.message().getMessageByMsgId(folder.account, msgid)) {
                EntityFolder dfolder = db.folder().getFolder(dup.folder);
                boolean outbox = EntityFolder.OUTBOX.equals(dfolder.type);
                Log.i(Helper.TAG, folder.name + " found as id=" + dup.id + "/" + dup.uid +
                        " folder=" + dfolder.type + ":" + dup.folder + "/" + folder.type + ":" + folder.id +
                        " msgid=" + dup.msgid + " thread=" + dup.thread);

                if (dup.folder.equals(folder.id) || outbox) {
                    String thread = helper.getThreadId(uid);
                    Log.i(Helper.TAG, folder.name + " found as id=" + dup.id + "/" +
                            " uid=" + dup.uid + "/" + uid +
                            " msgid=" + msgid + " thread=" + thread);
                    dup.folder = folder.id; // From outbox
                    if (dup.uid == null)
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
            message.ui_found = false;
            message.ui_ignored = false;
            message.ui_browsed = browsed;

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

            if (update)
                db.message().updateMessage(message);
        }

        List<String> fkeywords = new ArrayList<>(Arrays.asList(folder.keywords));

        for (String keyword : keywords)
            if (!fkeywords.contains(keyword)) {
                Log.i(Helper.TAG, folder.name + " adding keyword=" + keyword);
                fkeywords.add(keyword);
            }

        if (folder.keywords.length != fkeywords.size()) {
            Collections.sort(fkeywords);
            db.folder().setFolderKeywords(folder.id, DB.Converters.fromStringArray(fkeywords.toArray(new String[0])));
        }

        return message;
    }

    private static void downloadMessage(Context context, boolean metered, EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage, long id) throws MessagingException, IOException {
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

        if (message.setContactInfo(context))
            db.message().updateMessage(message);

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
        private ExecutorService queue = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
            try {
                if (!started) {
                    EntityLog.log(ServiceSynchronize.this, "Network " + network + " capabilities " + capabilities);
                    if (suitableNetwork())
                        queue_reload(true, "connect " + network);
                }
            } catch (Throwable ex) {
                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            }
        }

        @Override
        public void onAvailable(Network network) {
            try {
                ConnectivityManager cm = getSystemService(ConnectivityManager.class);
                EntityLog.log(ServiceSynchronize.this, "Available " + network + " " + cm.getNetworkInfo(network));

                if (!started && suitableNetwork())
                    queue_reload(true, "connect " + network);
            } catch (Throwable ex) {
                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            }
        }

        @Override
        public void onLost(Network network) {
            try {
                EntityLog.log(ServiceSynchronize.this, "Lost " + network);

                if (started && !suitableNetwork()) {
                    lastLost = new Date().getTime();
                    queue_reload(false, "disconnect " + network);
                }
            } catch (Throwable ex) {
                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            }
        }

        private boolean suitableNetwork() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServiceSynchronize.this);
            boolean metered = prefs.getBoolean("metered", true);

            Boolean isMetered = Helper.isMetered(ServiceSynchronize.this);

            boolean suitable = (isMetered != null && (metered || !isMetered));
            EntityLog.log(ServiceSynchronize.this,
                    "suitable=" + suitable + " metered=" + metered + " isMetered=" + isMetered);

            // The connected state is deliberately ignored
            return suitable;
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
                        PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":main");
                private List<ServiceState> threadState = new ArrayList<>();

                @Override
                public void run() {
                    try {
                        wl.acquire();

                        final DB db = DB.getInstance(ServiceSynchronize.this);

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
                        Handler handler = null;
                        final EntityFolder outbox = db.folder().getOutbox();
                        if (outbox != null) {
                            db.folder().setFolderError(outbox.id, null);

                            handler = new Handler(Looper.getMainLooper()) {
                                private LiveData<List<EntityOperation>> liveOperations;

                                @Override
                                public void handleMessage(android.os.Message msg) {
                                    Log.i(Helper.TAG, outbox.name + " observe=" + msg.what);
                                    if (msg.what == 0)
                                        liveOperations.removeObserver(observer);
                                    else {
                                        liveOperations = db.operation().liveOperations(outbox.id);
                                        liveOperations.observe(ServiceSynchronize.this, observer);
                                    }
                                }

                                private Observer<List<EntityOperation>> observer = new Observer<List<EntityOperation>>() {
                                    private List<Long> handling = new ArrayList<>();
                                    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);
                                    PowerManager pm = getSystemService(PowerManager.class);
                                    PowerManager.WakeLock wl = pm.newWakeLock(
                                            PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":outbox");

                                    @Override
                                    public void onChanged(List<EntityOperation> operations) {
                                        boolean process = false;
                                        List<Long> current = new ArrayList<>();
                                        for (EntityOperation op : operations) {
                                            if (!handling.contains(op.id))
                                                process = true;
                                            current.add(op.id);
                                        }
                                        handling = current;

                                        if (handling.size() > 0 && process) {
                                            Log.i(Helper.TAG, outbox.name + " operations=" + operations.size());
                                            executor.submit(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        wl.acquire();
                                                        Log.i(Helper.TAG, outbox.name + " process");

                                                        db.folder().setFolderSyncState(outbox.id, "syncing");
                                                        processOperations(null, outbox, null, null, null, state);
                                                        db.folder().setFolderError(outbox.id, null);
                                                    } catch (Throwable ex) {
                                                        Log.e(Helper.TAG, outbox.name + " " + ex + "\n" + Log.getStackTraceString(ex));
                                                        reportError(null, outbox, ex);
                                                        db.folder().setFolderError(outbox.id, Helper.formatThrowable(ex));
                                                    } finally {
                                                        db.folder().setFolderSyncState(outbox.id, null);
                                                        wl.release();
                                                        EntityLog.log(ServiceSynchronize.this, "Outbox wake lock=" + wl.isHeld());
                                                    }
                                                }
                                            });
                                        }
                                    }
                                };
                            };
                            handler.sendEmptyMessage(1);
                            db.folder().setFolderState(outbox.id, "connected");
                        }

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
                        if (outbox != null) {
                            Log.i(Helper.TAG, outbox.name + " unlisten operations");
                            handler.sendEmptyMessage(0);
                            db.folder().setFolderState(outbox.id, null);
                        }

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
                        PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":manage");

                @Override
                public void run() {
                    try {
                        wl.acquire();

                        EntityLog.log(ServiceSynchronize.this, "Reload " +
                                " stop=" + doStop + " start=" + doStart + " queued=" + queued + " " + reason);

                        if (doStop)
                            stop();

                        DB db = DB.getInstance(ServiceSynchronize.this);
                        int accounts = db.account().deleteAccountsTbd();
                        int identities = db.identity().deleteIdentitiesTbd();
                        if (accounts > 0 || identities > 0)
                            Log.i(Helper.TAG, "Deleted accounts=" + accounts + " identities=" + identities);

                        if (doStart)
                            start();

                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    } finally {
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

                        wl.release();
                    }
                }
            });

            started = doStart;
        }
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
    }

    private class ServiceState {
        private Thread thread;
        private Semaphore semaphore = new Semaphore(0);
        private boolean running = true;

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
            return "[running=" + running + "]";
        }
    }

    private class AlertException extends Throwable {
        private String alert;

        AlertException(String alert) {
            this.alert = alert;
        }

        @Override
        public String getMessage() {
            return alert;
        }
    }
}
