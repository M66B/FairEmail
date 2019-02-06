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
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.LongSparseArray;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MailConnectException;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
import javax.mail.search.MessageIDTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
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
    private static final int ACCOUNT_ERROR_AFTER = 60; // minutes
    private static final int IDENTITY_ERROR_AFTER = 30; // minutes
    private static final long STOP_DELAY = 5000L; // milliseconds
    private static final long YIELD_DURATION = 200L; // milliseconds

    static final int PI_WHY = 1;
    static final int PI_CLEAR = 2;
    static final int PI_SEEN = 3;
    static final int PI_ARCHIVE = 4;
    static final int PI_TRASH = 5;
    static final int PI_IGNORED = 6;
    static final int PI_SNOOZED = 7;

    @Override
    public void onCreate() {
        Log.i("Service create version=" + BuildConfig.VERSION_NAME);
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
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(NOTIFICATION_SYNCHRONIZE, getNotificationService(stats).build());
            }
        });

        db.message().liveUnseenNotify().observe(this, new Observer<List<TupleMessageEx>>() {
            private LongSparseArray<List<Integer>> notifying = new LongSparseArray<>();

            @Override
            public void onChanged(final List<TupleMessageEx> messages) {
                executor.submit(new Runnable() {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wl = pm.newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":notify");

                    @Override
                    public void run() {
                        try {
                            wl.acquire();
                            Log.i("Notification messages=" + messages.size());

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
                                        if (removed.contains(id)) {
                                            removed.remove(id);
                                            Log.i("Notification removing=" + id);
                                        } else {
                                            removed.remove(Integer.valueOf(-id));
                                            added.add(id);
                                            Log.i("Notification adding=" + id);
                                        }
                                    }
                                }

                                int headers = 0;
                                for (Integer id : added)
                                    if (id < 0)
                                        headers++;

                                Log.i("Notification account=" + account +
                                        " notifications=" + notifications.size() + " all=" + all.size() +
                                        " added=" + added.size() + " removed=" + removed.size() + " headers=" + headers);

                                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                if (notifications.size() == 0 ||
                                        (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && headers > 0))
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
                            Log.e(ex);
                        } finally {
                            wl.release();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.i("Service destroy");

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(serviceManager);

        serviceManager.service_destroy();

        Widget.update(this, -1);

        stopForeground(true);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_SYNCHRONIZE);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent == null ? null : intent.getAction());
        Log.i("Service command intent=" + intent + " action=" + action);

        startForeground(NOTIFICATION_SYNCHRONIZE, getNotificationService(null).build());

        super.onStartCommand(intent, flags, startId);

        if (action != null)
            try {
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
                        serviceManager.service_reload(intent.getStringExtra("reason"));
                        break;

                    case "clear":
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    DB.getInstance(ServiceSynchronize.this).message().ignoreAll();
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }
                            }
                        });
                        break;

                    case "seen":
                    case "archive":
                    case "trash":
                    case "ignore":
                    case "snooze":
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                DB db = DB.getInstance(ServiceSynchronize.this);
                                try {
                                    db.beginTransaction();

                                    long id = Long.parseLong(parts[1]);
                                    EntityMessage message = db.message().getMessage(id);
                                    if (message == null)
                                        return;

                                    switch (parts[0]) {
                                        case "seen":
                                            EntityOperation.queue(ServiceSynchronize.this, db, message, EntityOperation.SEEN, true);
                                            break;

                                        case "archive":
                                            EntityFolder archive = db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE);
                                            if (archive == null)
                                                archive = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                                            if (archive != null)
                                                EntityOperation.queue(ServiceSynchronize.this, db, message, EntityOperation.MOVE, archive.id);
                                            break;

                                        case "trash":
                                            EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                                            if (trash != null)
                                                EntityOperation.queue(ServiceSynchronize.this, db, message, EntityOperation.MOVE, trash.id);
                                            break;

                                        case "ignore":
                                            db.message().setMessageUiIgnored(message.id, true);
                                            break;

                                        case "snooze":
                                            db.message().setMessageSnoozed(message.id, null);

                                            EntityFolder folder = db.folder().getFolder(message.folder);
                                            if (EntityFolder.OUTBOX.equals(folder.type)) {
                                                Log.i("Delayed send id=" + message.id);
                                                EntityOperation.queue(
                                                        ServiceSynchronize.this, db, message, EntityOperation.SEND);
                                            } else {
                                                EntityOperation.queue(
                                                        ServiceSynchronize.this, db, message, EntityOperation.SEEN, false);
                                                db.message().setMessageUiIgnored(message.id, false);
                                            }
                                            break;

                                        default:
                                            Log.w("Unknown action: " + parts[0]);
                                    }

                                    db.setTransactionSuccessful();
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                } finally {
                                    db.endTransaction();
                                }
                            }
                        });
                        break;

                    default:
                        Log.w("Unknown action: " + action);
                }
            } catch (Throwable ex) {
                Log.e(ex);
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

        // Get contact info
        Map<TupleMessageEx, ContactInfo> messageContact = new HashMap<>();
        for (TupleMessageEx message : messages)
            messageContact.put(message, ContactInfo.get(this, message.from, false));

        // Build pending intent
        Intent view = new Intent(this, ActivityView.class);
        view.setAction("unified");
        view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piView = PendingIntent.getActivity(
                this, ActivityView.REQUEST_UNIFIED, view, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent clear = new Intent(this, ServiceSynchronize.class);
        clear.setAction("clear");
        PendingIntent piClear = PendingIntent.getService(this, PI_CLEAR, clear, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelName = (account == 0 ? "notification" : EntityAccount.getNotificationChannelName(account));

        // Build public notification
        Notification.Builder pbuilder;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            pbuilder = new Notification.Builder(this);
        else
            pbuilder = new Notification.Builder(this, channelName);

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
            builder = new Notification.Builder(this, channelName);

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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (prefs.getBoolean("light", false))
                builder.setLights(Color.GREEN, 1000, 1000);

            String sound = prefs.getString("sound", null);
            if (sound == null) {
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(uri);
            } else
                builder.setSound(Uri.parse(sound));

            builder.setOnlyAlertOnce(true);
        } else
            builder.setGroupAlertBehavior(Notification.GROUP_ALERT_CHILDREN);

        if (pro) {
            DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
            StringBuilder sb = new StringBuilder();
            for (EntityMessage message : messages) {
                sb.append("<strong>").append(messageContact.get(message).getDisplayName(true)).append("</strong>");
                if (!TextUtils.isEmpty(message.subject))
                    sb.append(": ").append(message.subject);
                sb.append(" ").append(df.format(message.received));
                sb.append("<br>");
            }

            builder.setStyle(new Notification.BigTextStyle()
                    .bigText(Html.fromHtml(sb.toString()))
                    .setSummaryText(summary));
        }

        notifications.add(builder.build());

        for (TupleMessageEx message : messages) {
            ContactInfo info = messageContact.get(message);

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
                    R.drawable.baseline_visibility_24,
                    getString(R.string.title_action_seen),
                    piSeen);

            Notification.Action.Builder actionArchive = new Notification.Action.Builder(
                    R.drawable.baseline_archive_24,
                    getString(R.string.title_action_archive),
                    piArchive);

            Notification.Action.Builder actionTrash = new Notification.Action.Builder(
                    R.drawable.baseline_delete_24,
                    getString(R.string.title_action_trash),
                    piTrash);

            Notification.Builder mbuilder;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                mbuilder = new Notification.Builder(this);
            else
                mbuilder = new Notification.Builder(this, channelName);

            String folderName = message.folderDisplay == null
                    ? Helper.localizeFolderName(this, message.folderName)
                    : message.folderDisplay;

            mbuilder
                    .addExtras(args)
                    .setSmallIcon(R.drawable.baseline_email_white_24)
                    .setContentTitle(info.getDisplayName(true))
                    .setSubText(message.accountName + " · " + folderName)
                    .setContentIntent(piContent)
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

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                mbuilder.setSound(null);

            if (pro) {
                if (!TextUtils.isEmpty(message.subject))
                    mbuilder.setContentText(message.subject);

                if (message.content)
                    try {
                        String body = Helper.readText(EntityMessage.getFile(this, message.id));
                        StringBuilder sb = new StringBuilder();
                        if (!TextUtils.isEmpty(message.subject))
                            sb.append(message.subject).append("<br>");
                        sb.append(HtmlHelper.getPreview(body));
                        mbuilder.setStyle(new Notification.BigTextStyle().bigText(Html.fromHtml(sb.toString())));
                    } catch (IOException ex) {
                        Log.e(ex);
                        mbuilder.setStyle(new Notification.BigTextStyle().bigText(ex.toString()));
                    }

                if (info.hasPhoto())
                    mbuilder.setLargeIcon(info.getPhotoBitmap());

                if (info.hasLookupUri())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        mbuilder.addPerson(new Person.Builder()
                                .setUri(info.getLookupUri().toString())
                                .build());
                    else
                        mbuilder.addPerson(info.getLookupUri().toString());

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
        return getNotificationError("error", title, ex, true);
    }

    private Notification.Builder getNotificationError(String channel, String title, Throwable ex, boolean debug) {
        // Build pending intent
        Intent intent = new Intent(this, ActivitySetup.class);
        if (debug)
            intent.setAction("error");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                this, ActivitySetup.REQUEST_ERROR, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this, channel);
        else
            builder = new Notification.Builder(this);

        builder
                .setSmallIcon(R.drawable.baseline_warning_white_24)
                .setContentTitle(getString(R.string.title_notification_failed, title))
                .setContentText(Helper.formatThrowable(ex))
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setShowWhen(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_ERROR)
                .setVisibility(Notification.VISIBILITY_SECRET);

        builder.setStyle(new Notification.BigTextStyle()
                .bigText(Helper.formatThrowable(ex, "\n")));

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
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(tag, 1, getNotificationError(title, ex).build());
        }
    }

    private void monitorAccount(final EntityAccount account, final ServiceState state) throws NoSuchProviderException {
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wlAccount = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id);
        try {
            wlAccount.acquire();

            final DB db = DB.getInstance(this);

            int backoff = CONNECT_BACKOFF_START;
            while (state.running()) {
                Log.i(account.name + " run");

                // Debug
                boolean debug = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("debug", false);
                debug = debug || BuildConfig.DEBUG;
                System.setProperty("mail.socket.debug", Boolean.toString(debug));

                // Create session
                Properties props = MessageHelper.getSessionProperties(account.auth_type, account.realm, account.insecure);
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
                                if (e.getMessageType() == StoreEvent.ALERT) {
                                    EntityLog.log(ServiceSynchronize.this, account.name + " " + type + ": " + e.getMessage());
                                    db.account().setAccountError(account.id, e.getMessage());
                                    reportError(account, null, new AlertException(e.getMessage()));
                                    state.error();
                                } else
                                    Log.i(account.name + " " + type + ": " + e.getMessage());
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
                                Log.i("Folder created=" + e.getFolder().getFullName());
                                reload(ServiceSynchronize.this, "folder created");
                            } finally {
                                wlAccount.release();
                            }
                        }

                        @Override
                        public void folderRenamed(FolderEvent e) {
                            try {
                                wlAccount.acquire();
                                Log.i("Folder renamed=" + e.getFolder().getFullName());

                                String old = e.getFolder().getFullName();
                                String name = e.getNewFolder().getFullName();
                                int count = db.folder().renameFolder(account.id, old, name);
                                Log.i("Renamed to " + name + " count=" + count);

                                reload(ServiceSynchronize.this, "folder renamed");
                            } finally {
                                wlAccount.release();
                            }
                        }

                        @Override
                        public void folderDeleted(FolderEvent e) {
                            try {
                                wlAccount.acquire();
                                Log.i("Folder deleted=" + e.getFolder().getFullName());
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
                            Log.i(account.name + " opened event");
                        }

                        @Override
                        public void disconnected(ConnectionEvent e) {
                            Log.e(account.name + " disconnected event");
                        }

                        @Override
                        public void closed(ConnectionEvent e) {
                            Log.e(account.name + " closed event");
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
                            long delayed = now - account.last_connected - account.poll_interval * 60 * 1000L;
                            if (delayed > ACCOUNT_ERROR_AFTER * 60 * 1000L) {
                                Log.i("Reporting sync error after=" + delayed);
                                Throwable warning = new Throwable(
                                        getString(R.string.title_no_sync,
                                                SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                                        .format((account.last_connected))), ex);
                                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                nm.notify("receive", account.id.intValue(),
                                        getNotificationError("warning", account.name, warning, false).build());
                            }
                        }

                        throw ex;
                    }

                    final boolean capIdle = istore.hasCapability("IDLE");
                    final boolean capUidPlus = istore.hasCapability("UIDPLUS");
                    Log.i(account.name + " idle=" + capIdle + " uidplus=" + capUidPlus);

                    db.account().setAccountState(account.id, "connected");

                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel("receive", account.id.intValue());

                    EntityLog.log(this, account.name + " connected");

                    // Update folder list
                    synchronizeFolders(account, istore, state);

                    // Open synchronizing folders
                    final ExecutorService pollExecutor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);
                    for (final EntityFolder folder : db.folder().getFolders(account.id)) {
                        if (folder.synchronize && !folder.poll && capIdle) {
                            Log.i(account.name + " sync folder " + folder.name);

                            db.folder().setFolderState(folder.id, "connecting");

                            final IMAPFolder ifolder = (IMAPFolder) istore.getFolder(folder.name);
                            try {
                                ifolder.open(Folder.READ_WRITE);
                            } catch (MessagingException ex) {
                                // Including ReadOnlyFolderException
                                db.folder().setFolderState(folder.id, null);
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                continue;
                            } catch (Throwable ex) {
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                throw ex;
                            }
                            folders.put(folder, ifolder);

                            db.folder().setFolderState(folder.id, "connected");
                            db.folder().setFolderError(folder.id, null);

                            Log.i(account.name + " folder " + folder.name + " flags=" + ifolder.getPermanentFlags());

                            // Listen for new and deleted messages
                            ifolder.addMessageCountListener(new MessageCountAdapter() {
                                @Override
                                public void messagesAdded(MessageCountEvent e) {
                                    try {
                                        wlAccount.acquire();
                                        Log.i(folder.name + " messages added");

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
                                                            folder, ifolder, (IMAPMessage) imessage,
                                                            false,
                                                            db.rule().getEnabledRules(folder.id));
                                                    db.setTransactionSuccessful();
                                                } finally {
                                                    db.endTransaction();
                                                }

                                                if (db.folder().getFolderDownload(folder.id))
                                                    try {
                                                        db.beginTransaction();
                                                        downloadMessage(ServiceSynchronize.this,
                                                                folder, ifolder,
                                                                (IMAPMessage) imessage, message.id);
                                                        db.setTransactionSuccessful();
                                                    } finally {
                                                        db.endTransaction();
                                                    }
                                            } catch (MessageRemovedException ex) {
                                                Log.w(folder.name, ex);
                                            } catch (FolderClosedException ex) {
                                                throw ex;
                                            } catch (IOException ex) {
                                                if (ex.getCause() instanceof MessagingException) {
                                                    Log.w(folder.name, ex);
                                                    if (!(ex.getCause() instanceof MessageRemovedException))
                                                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                                } else
                                                    throw ex;
                                            } catch (Throwable ex) {
                                                Log.e(folder.name, ex);
                                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                            }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        reportError(account, folder, ex);
                                        state.error();
                                    } finally {
                                        wlAccount.release();
                                    }
                                }

                                @Override
                                public void messagesRemoved(MessageCountEvent e) {
                                    try {
                                        wlAccount.acquire();
                                        Log.i(folder.name + " messages removed");
                                        for (Message imessage : e.getMessages())
                                            try {
                                                long uid = ifolder.getUID(imessage);

                                                DB db = DB.getInstance(ServiceSynchronize.this);
                                                int count = db.message().deleteMessage(folder.id, uid);

                                                Log.i(folder.name + " deleted uid=" + uid + " count=" + count);
                                            } catch (MessageRemovedException ex) {
                                                Log.w(folder.name, ex);
                                            }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
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
                                            Log.i(folder.name + " message changed");

                                            FetchProfile fp = new FetchProfile();
                                            fp.add(UIDFolder.FetchProfileItem.UID);
                                            fp.add(IMAPFolder.FetchProfileItem.FLAGS);
                                            ifolder.fetch(new Message[]{e.getMessage()}, fp);

                                            EntityMessage message;
                                            try {
                                                db.beginTransaction();
                                                message = synchronizeMessage(
                                                        ServiceSynchronize.this,
                                                        folder, ifolder, (IMAPMessage) e.getMessage(),
                                                        false,
                                                        db.rule().getEnabledRules(folder.id));
                                                db.setTransactionSuccessful();
                                            } finally {
                                                db.endTransaction();
                                            }

                                            if (db.folder().getFolderDownload(folder.id))
                                                try {
                                                    db.beginTransaction();
                                                    downloadMessage(ServiceSynchronize.this,
                                                            folder, ifolder,
                                                            (IMAPMessage) e.getMessage(), message.id);
                                                    db.setTransactionSuccessful();
                                                } finally {
                                                    db.endTransaction();
                                                }
                                        } catch (MessageRemovedException ex) {
                                            Log.w(folder.name, ex);
                                        } catch (FolderClosedException ex) {
                                            throw ex;
                                        } catch (IOException ex) {
                                            if (ex.getCause() instanceof MessagingException) {
                                                Log.w(folder.name, ex);
                                                if (!(ex.getCause() instanceof MessageRemovedException))
                                                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                            } else
                                                throw ex;
                                        } catch (Throwable ex) {
                                            Log.e(folder.name, ex);
                                            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        reportError(account, folder, ex);
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
                                        Log.i(folder.name + " start idle");
                                        while (state.running()) {
                                            Log.i(folder.name + " do idle");
                                            ifolder.idle(false);
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        reportError(account, folder, ex);
                                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                        state.error();
                                    } finally {
                                        Log.i(folder.name + " end idle");
                                    }
                                }
                            }, "idler." + folder.id);
                            idler.setPriority(THREAD_PRIORITY_BACKGROUND);
                            idler.start();
                            idlers.add(idler);

                            EntityOperation.sync(db, folder.id);
                        } else
                            folders.put(folder, null);

                        // Observe operations
                        Handler handler = new Handler(getMainLooper()) {
                            private List<Long> handling = new ArrayList<>();
                            private LiveData<List<EntityOperation>> liveOperations;

                            @Override
                            public void handleMessage(android.os.Message msg) {
                                Log.i(account.name + "/" + folder.name + " observe=" + msg.what);
                                try {
                                    if (msg.what == 0) {
                                        liveOperations.removeObserver(observer);
                                        handling.clear();
                                    } else {
                                        liveOperations = db.operation().liveOperations(folder.id);
                                        liveOperations.observe(ServiceSynchronize.this, observer);
                                    }
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }
                            }

                            private Observer<List<EntityOperation>> observer = new Observer<List<EntityOperation>>() {
                                private final ExecutorService folderExecutor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);
                                private final PowerManager.WakeLock wlFolder = pm.newWakeLock(
                                        PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":folder." + folder.id);

                                @Override
                                public void onChanged(final List<EntityOperation> operations) {
                                    boolean process = false;
                                    List<Long> current = new ArrayList<>();
                                    for (EntityOperation op : operations) {
                                        if (!handling.contains(op.id))
                                            process = true;
                                        current.add(op.id);
                                    }
                                    handling = current;

                                    if (handling.size() > 0 && process) {
                                        Log.i(folder.name + " operations=" + operations.size());
                                        (folder.poll ? pollExecutor : folderExecutor).submit(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    wlFolder.acquire();
                                                    Log.i(folder.name + " process");

                                                    // Get folder
                                                    IMAPFolder ifolder = folders.get(folder); // null when polling
                                                    final boolean shouldClose = (ifolder == null);

                                                    try {
                                                        Log.i(folder.name + " run " + (shouldClose ? "offline" : "online"));

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
                                                        Log.e(folder.name, ex);
                                                        reportError(account, folder, ex);
                                                        // IllegalStateException: sync when store disconnected
                                                        if (!(ex instanceof IllegalStateException))
                                                            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                                        state.error();
                                                    } finally {
                                                        if (shouldClose) {
                                                            if (ifolder != null && ifolder.isOpen()) {
                                                                db.folder().setFolderState(folder.id, "closing");
                                                                try {
                                                                    ifolder.close(false);
                                                                } catch (MessagingException ex) {
                                                                    Log.w(folder.name, ex);
                                                                }
                                                            }
                                                            if (folder.synchronize && (folder.poll || !capIdle))
                                                                db.folder().setFolderState(folder.id, "waiting");
                                                            else
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
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(id), 0);
                    registerReceiver(alarm, new IntentFilter(id));

                    // Keep alive
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
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
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                                am.set(
                                        AlarmManager.RTC_WAKEUP,
                                        System.currentTimeMillis() + account.poll_interval * 60 * 1000L,
                                        pi);
                            else
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

                    Log.i(account.name + " done state=" + state);
                } catch (Throwable ex) {
                    Log.e(account.name, ex);
                    reportError(account, null, ex);

                    EntityLog.log(this, account.name + " " + Helper.formatThrowable(ex));
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
                        EntityLog.log(this, account.name + " store closing");
                        istore.close();
                        EntityLog.log(this, account.name + " store closed");
                    } catch (Throwable ex) {
                        Log.w(account.name, ex);
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
                            PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(id), 0);
                            registerReceiver(alarm, new IntentFilter(id));

                            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            try {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                                    am.set(
                                            AlarmManager.RTC_WAKEUP,
                                            System.currentTimeMillis() + CONNECT_BACKOFF_AlARM * 60 * 1000L,
                                            pi);
                                else
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
                        Log.w(account.name + " backoff " + ex.toString());
                    }
            }
        } finally {
            EntityLog.log(this, account.name + " stopped");
            wlAccount.release();
        }
    }

    private void processOperations(
            EntityAccount account, EntityFolder folder,
            Session isession, IMAPStore istore, IMAPFolder ifolder,
            ServiceState state)
            throws MessagingException, JSONException, IOException {
        try {
            Log.i(folder.name + " start process");

            DB db = DB.getInstance(this);
            List<EntityOperation> ops = db.operation().getOperations(folder.id);
            Log.i(folder.name + " pending operations=" + ops.size());
            for (int i = 0; i < ops.size() && state.running(); i++) {
                EntityOperation op = ops.get(i);
                try {
                    Log.i(folder.name +
                            " start op=" + op.id + "/" + op.name +
                            " msg=" + op.message +
                            " args=" + op.args);

                    // Fetch most recent copy of message
                    EntityMessage message = null;
                    if (op.message != null)
                        message = db.message().getMessage(op.message);

                    JSONArray jargs = new JSONArray(op.args);

                    try {
                        if (message == null && !EntityOperation.SYNC.equals(op.name))
                            throw new MessageRemovedException();

                        db.operation().setOperationError(op.id, null);
                        if (message != null)
                            db.message().setMessageError(message.id, null);

                        if (message != null && message.uid == null &&
                                !(EntityOperation.ADD.equals(op.name) ||
                                        EntityOperation.DELETE.equals(op.name) ||
                                        EntityOperation.SEND.equals(op.name) ||
                                        EntityOperation.SYNC.equals(op.name)))
                            throw new IllegalArgumentException(op.name + " without uid " + op.args);

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

                        else if (EntityOperation.RAW.equals(op.name))
                            doRaw(folder, ifolder, message, jargs, db);

                        else if (EntityOperation.BODY.equals(op.name))
                            doBody(folder, ifolder, message, db);

                        else if (EntityOperation.ATTACHMENT.equals(op.name))
                            doAttachment(folder, op, ifolder, message, jargs, db);

                        else if (EntityOperation.SYNC.equals(op.name))
                            if (EntityFolder.OUTBOX.equals(folder.type))
                                db.folder().setFolderError(folder.id, null);
                            else
                                synchronizeMessages(account, folder, ifolder, jargs, state);

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
                                ex instanceof SendFailedException ||
                                ex instanceof IllegalArgumentException) {
                            Log.w("Unrecoverable", ex);

                            // There is no use in repeating
                            db.operation().deleteOperation(op.id);

                            // Cleanup
                            if (message != null) {
                                if (ex instanceof MessageRemovedException)
                                    db.message().deleteMessage(message.id);

                                Long newid = null;

                                if (EntityOperation.MOVE.equals(op.name) &&
                                        jargs.length() > 2)
                                    newid = jargs.getLong(2);

                                if ((EntityOperation.ADD.equals(op.name) ||
                                        EntityOperation.RAW.equals(op.name)) &&
                                        jargs.length() > 0 && !jargs.isNull(0))
                                    newid = jargs.getLong(0);

                                // Delete temporary copy in target folder
                                if (newid != null) {
                                    db.message().deleteMessage(newid);
                                    db.message().setMessageUiHide(message.id, false);
                                }
                            }

                            continue;
                        } else if (ex instanceof MessagingException) {
                            // Socket timeout is a recoverable condition (send message)
                            if (ex.getCause() instanceof SocketTimeoutException) {
                                Log.w("Recoverable", ex);
                                // No need to inform user
                                return;
                            }
                        }

                        throw ex;
                    }
                } finally {
                    Log.i(folder.name + " end op=" + op.id + "/" + op.name);
                }
            }
        } finally {
            Log.i(folder.name + " end process state=" + state);
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
        // Add message
        if (TextUtils.isEmpty(message.msgid))
            throw new IllegalArgumentException("Message ID missing");

        // Get message
        MimeMessage imessage;
        if (folder.id.equals(message.folder)) {
            // Pre flight checks
            if (!message.content)
                throw new IllegalArgumentException("Message body missing");

            List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
            for (EntityAttachment attachment : attachments)
                if (!attachment.available)
                    throw new IllegalArgumentException("Attachment missing");

            imessage = MessageHelper.from(this, message, isession);
        } else {
            // Cross account move
            File file = EntityMessage.getRawFile(this, message.id);
            if (!file.exists())
                throw new IllegalArgumentException("raw message file not found");

            InputStream is = null;
            try {
                Log.i(folder.name + " reading " + file);
                is = new BufferedInputStream(new FileInputStream(file));
                imessage = new MimeMessage(isession, is);
            } finally {
                if (is != null)
                    is.close();
            }
        }

        // Handle auto read
        boolean autoread = false;
        if (jargs.length() > 1) {
            autoread = jargs.getBoolean(1);
            if (ifolder.getPermanentFlags().contains(Flags.Flag.SEEN)) {
                if (autoread && !imessage.isSet(Flags.Flag.SEEN)) {
                    Log.i(folder.name + " autoread");
                    imessage.setFlag(Flags.Flag.SEEN, true);
                }
            }
        }

        // Handle draft
        if (EntityFolder.DRAFTS.equals(folder.type))
            if (ifolder.getPermanentFlags().contains(Flags.Flag.DRAFT))
                imessage.setFlag(Flags.Flag.DRAFT, true);

        // Add message
        long uid = append(istore, ifolder, imessage, message.msgid);
        Log.i(folder.name + " appended id=" + message.id + " uid=" + uid);
        db.message().setMessageUid(message.id, uid);

        if (folder.id.equals(message.folder)) {
            // Delete previous message
            Message[] ideletes = ifolder.search(new MessageIDTerm(message.msgid));
            for (Message idelete : ideletes) {
                long duid = ifolder.getUID(idelete);
                if (duid == uid)
                    Log.i(folder.name + " append confirmed uid=" + duid);
                else {
                    Log.i(folder.name + " deleting uid=" + duid + " msgid=" + message.msgid);
                    idelete.setFlag(Flags.Flag.DELETED, true);
                }
            }
            ifolder.expunge();
        } else {
            // Cross account move
            if (autoread) {
                Log.i(folder.name + " queuing SEEN id=" + message.id);
                EntityOperation.queue(this, db, message, EntityOperation.SEEN, true);
            }

            Log.i(folder.name + " queuing DELETE id=" + message.id);
            EntityOperation.queue(this, db, message, EntityOperation.DELETE);
        }
    }

    private void doMove(EntityFolder folder, Session isession, IMAPStore istore, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws JSONException, MessagingException, IOException {
        // Move message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        // Get parameters
        boolean autoread = jargs.getBoolean(1);

        // Get target folder
        long id = jargs.getLong(0);
        EntityFolder target = db.folder().getFolder(id);
        if (target == null)
            throw new FolderNotFoundException();
        IMAPFolder itarget = (IMAPFolder) istore.getFolder(target.name);

        if (istore.hasCapability("MOVE") &&
                !EntityFolder.DRAFTS.equals(folder.type) &&
                !EntityFolder.DRAFTS.equals(target.type)) {
            // Autoread
            if (ifolder.getPermanentFlags().contains(Flags.Flag.SEEN)) {
                if (autoread && !imessage.isSet(Flags.Flag.SEEN))
                    imessage.setFlag(Flags.Flag.SEEN, true);
            }

            // Move message to
            ifolder.moveMessages(new Message[]{imessage}, itarget);
        } else {
            Log.w(folder.name + " MOVE by DELETE/APPEND");

            // Serialize source message
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            imessage.writeTo(bos);

            // Deserialize target message
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            Message icopy = new MimeMessage(isession, bis);

            try {
                // Needed to read flags
                itarget.open(Folder.READ_WRITE);

                // Auto read
                if (itarget.getPermanentFlags().contains(Flags.Flag.SEEN)) {
                    if (autoread && !icopy.isSet(Flags.Flag.SEEN)) {
                        Log.i("Copy autoread");
                        icopy.setFlag(Flags.Flag.SEEN, true);
                    }
                }

                // Move from drafts
                if (EntityFolder.DRAFTS.equals(folder.type))
                    if (itarget.getPermanentFlags().contains(Flags.Flag.DRAFT))
                        icopy.setFlag(Flags.Flag.DRAFT, false);

                // Move to drafts
                if (EntityFolder.DRAFTS.equals(target.type))
                    if (itarget.getPermanentFlags().contains(Flags.Flag.DRAFT))
                        icopy.setFlag(Flags.Flag.DRAFT, true);

                // Append target
                long uid = append(istore, itarget, icopy, message.msgid);
                Log.i(folder.name + " appended id=" + message.id + " uid=" + uid);
                db.message().setMessageUid(message.id, uid);

                // Some providers, like Gmail, don't honor the appended seen flag
                if (itarget.getPermanentFlags().contains(Flags.Flag.SEEN)) {
                    boolean seen = (autoread || message.ui_seen);
                    icopy = itarget.getMessageByUID(uid);
                    if (seen != icopy.isSet(Flags.Flag.SEEN)) {
                        Log.i(folder.name + " Fixing id=" + message.id + " seen=" + seen);
                        icopy.setFlag(Flags.Flag.SEEN, seen);
                    }
                }

                // This is not based on an actual case, so this is just a safeguard
                if (itarget.getPermanentFlags().contains(Flags.Flag.DRAFT)) {
                    boolean draft = EntityFolder.DRAFTS.equals(target.type);
                    icopy = itarget.getMessageByUID(uid);
                    if (draft != icopy.isSet(Flags.Flag.DRAFT)) {
                        Log.i(folder.name + " Fixing id=" + message.id + " draft=" + draft);
                        icopy.setFlag(Flags.Flag.DRAFT, draft);
                    }
                }

                // Delete source
                imessage.setFlag(Flags.Flag.DELETED, true);
                ifolder.expunge();
            } catch (Throwable ex) {
                if (itarget.isOpen())
                    itarget.close();
                throw ex;
            }
        }
    }

    private void doDelete(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException {
        // Delete message
        if (TextUtils.isEmpty(message.msgid))
            throw new IllegalArgumentException("Message ID missing");

        Message[] imessages = ifolder.search(new MessageIDTerm(message.msgid));
        for (Message imessage : imessages) {
            Log.i(folder.name + " deleting uid=" + message.uid + " msgid=" + message.msgid);
            imessage.setFlag(Flags.Flag.DELETED, true);
        }
        ifolder.expunge();

        db.message().deleteMessage(message.id);
    }

    private void doSend(EntityMessage message, DB db) throws MessagingException, IOException {
        // Send message
        EntityIdentity ident = db.identity().getIdentity(message.identity);

        // Mark attempt
        if (message.last_attempt == null) {
            message.last_attempt = new Date().getTime();
            db.message().setMessageLastAttempt(message.id, message.last_attempt);
        }

        String transportType = (ident.starttls ? "smtp" : "smtps");

        // Get properties
        Properties props = MessageHelper.getSessionProperties(ident.auth_type, ident.realm, ident.insecure);
        props.put("mail.smtp.localhost", ident.host);

        // Create session
        final Session isession = Session.getInstance(props, null);

        // Create message
        MimeMessage imessage = MessageHelper.from(this, message, isession);

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
        Transport itransport = isession.getTransport(transportType);
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

            // Send message
            Long sid = null;
            try {
                // Append replied/forwarded text
                String body = Helper.readText(EntityMessage.getFile(this, message.id));
                File refFile = EntityMessage.getRefFile(this, message.id);
                if (refFile.exists())
                    body += Helper.readText(refFile);

                EntityFolder sent = db.folder().getFolderByType(ident.account, EntityFolder.SENT);
                if (sent != null) {
                    long id = message.id;
                    long folder = message.folder;

                    message.id = null;
                    message.folder = sent.id;
                    message.seen = true;
                    message.ui_seen = true;
                    message.ui_hide = true;
                    message.ui_browsed = true; // prevent deleting on sync
                    message.error = null;
                    message.id = db.message().insertMessage(message);
                    Helper.writeText(EntityMessage.getFile(this, message.id), body);

                    sid = message.id;
                    message.id = id;
                    message.folder = folder;
                    message.seen = false;
                    message.ui_seen = false;
                    message.ui_browsed = false;
                    message.ui_hide = false;

                    EntityAttachment.copy(this, db, message.id, sid);
                }

                Address[] to = imessage.getAllRecipients();
                itransport.sendMessage(imessage, to);
                EntityLog.log(this, "Sent via " + ident.host + "/" + ident.user +
                        " to " + TextUtils.join(", ", to));

                try {
                    db.beginTransaction();

                    if (sid == null) {
                        db.message().setMessageSent(message.id, imessage.getSentDate().getTime());
                        db.message().setMessageSeen(message.id, true);
                        db.message().setMessageUiSeen(message.id, true);
                        db.message().setMessageError(message.id, null);
                        Helper.writeText(EntityMessage.getFile(this, message.id), body);
                    } else {
                        db.message().setMessageSent(sid, imessage.getSentDate().getTime());
                        db.message().setMessageUiHide(sid, false);
                        db.message().deleteMessage(message.id);

                        if (ident.store_sent) {
                            message.id = sid;
                            message.folder = sent.id;
                            EntityOperation.queue(this, db, message, EntityOperation.ADD);
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (refFile.exists())
                    refFile.delete();

                if (message.inreplyto != null) {
                    List<EntityMessage> replieds = db.message().getMessageByMsgId(message.account, message.inreplyto);
                    for (EntityMessage replied : replieds)
                        if (replied.uid != null)
                            EntityOperation.queue(this, db, replied, EntityOperation.ANSWERED, true);
                }

                db.identity().setIdentityConnected(ident.id, new Date().getTime());
                db.identity().setIdentityError(ident.id, null);

                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel("send", message.identity.intValue());
            } catch (Throwable ex) {
                if (sid != null)
                    db.message().deleteMessage(sid);
                throw ex;
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
            if (delayed > IDENTITY_ERROR_AFTER * 60 * 1000L) {
                Log.i("Reporting send error after=" + delayed);
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
        if (message.headers != null)
            return;

        IMAPMessage imessage = (IMAPMessage) ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        MessageHelper helper = new MessageHelper(imessage);
        db.message().setMessageHeaders(message.id, helper.getHeaders());
    }

    private void doRaw(EntityFolder folder, IMAPFolder ifolder, EntityMessage message, JSONArray jargs, DB db) throws MessagingException, IOException, JSONException {
        if (message.raw == null || !message.raw) {
            IMAPMessage imessage = (IMAPMessage) ifolder.getMessageByUID(message.uid);
            if (imessage == null)
                throw new MessageRemovedException();

            File file = EntityMessage.getRawFile(this, message.id);

            OutputStream os = null;
            try {
                os = new BufferedOutputStream(new FileOutputStream(file));
                imessage.writeTo(os);
                db.message().setMessageRaw(message.id, true);
            } finally {
                if (os != null)
                    os.close();
            }
        }

        if (jargs.length() > 0) {
            long target = jargs.getLong(2);
            jargs.remove(2);
            Log.i(folder.name + " queuing ADD id=" + message.id + ":" + target);

            EntityOperation operation = new EntityOperation();
            operation.folder = target;
            operation.message = message.id;
            operation.name = EntityOperation.ADD;
            operation.args = jargs.toString();
            operation.created = new Date().getTime();
            operation.id = db.operation().insertOperation(operation);
        }
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
        MessageHelper.MessageParts parts = helper.getMessageParts();
        String body = parts.getHtml(this);
        String preview = HtmlHelper.getPreview(body);
        Helper.writeText(EntityMessage.getFile(this, message.id), body);
        db.message().setMessageContent(message.id, true, preview);
        db.message().setMessageWarning(message.id, parts.getWarnings(message.warning));
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
        MessageHelper.MessageParts parts = helper.getMessageParts();
        parts.downloadAttachment(this, db, attachment.id, sequence);
    }

    private long append(IMAPStore istore, IMAPFolder ifolder, Message imessage, String msgid) throws MessagingException {
        if (istore.hasCapability("UIDPLUS")) {
            AppendUID[] uids = ifolder.appendUIDMessages(new Message[]{imessage});
            if (uids == null || uids.length == 0)
                throw new MessageRemovedException("Message not appended");
            return uids[0].uid;
        } else {
            ifolder.appendMessages(new Message[]{imessage});
            long uid = -1;
            Message[] messages = ifolder.search(new MessageIDTerm(msgid));
            if (messages != null)
                for (Message iappended : messages) {
                    long muid = ifolder.getUID(iappended);
                    // RFC3501: Unique identifiers are assigned in a strictly ascending fashion
                    if (muid > uid)
                        uid = muid;
                }

            if (uid < 0)
                throw new MessageRemovedException("uid not found");

            return uid;
        }
    }

    private void synchronizeFolders(EntityAccount account, IMAPStore istore, ServiceState state) throws MessagingException {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            Log.i("Start sync folders account=" + account.name);

            List<String> names = new ArrayList<>();
            for (EntityFolder folder : db.folder().getFolders(account.id))
                if (folder.tbc != null) {
                    Log.i(folder.name + " creating");
                    IMAPFolder ifolder = (IMAPFolder) istore.getFolder(folder.name);
                    if (!ifolder.exists())
                        ifolder.create(Folder.HOLDS_MESSAGES);
                    db.folder().resetFolderTbc(folder.id);
                } else if (folder.tbd != null && folder.tbd) {
                    Log.i(folder.name + " deleting");
                    IMAPFolder ifolder = (IMAPFolder) istore.getFolder(folder.name);
                    if (ifolder.exists())
                        ifolder.delete(false);
                    db.folder().deleteFolder(folder.id);
                } else
                    names.add(folder.name);
            Log.i("Local folder count=" + names.size());

            Folder defaultFolder = istore.getDefaultFolder();
            char separator = defaultFolder.getSeparator();
            EntityLog.log(this, account.name + " folder separator=" + separator);

            Folder[] ifolders = defaultFolder.list("*");
            Log.i("Remote folder count=" + ifolders.length + " separator=" + separator);

            for (Folder ifolder : ifolders) {
                String fullName = ifolder.getFullName();
                String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                String type = EntityFolder.getType(attrs, fullName);

                EntityLog.log(this, account.name + ":" + fullName +
                        " attrs=" + TextUtils.join(" ", attrs) + " type=" + type);

                if (type != null) {
                    names.remove(fullName);

                    int level = EntityFolder.getLevel(separator, fullName);
                    String display = null;
                    if (account.prefix != null && fullName.startsWith(account.prefix + separator))
                        display = fullName.substring(account.prefix.length() + 1);

                    EntityFolder folder = db.folder().getFolderByName(account.id, fullName);
                    if (folder == null) {
                        folder = new EntityFolder();
                        folder.account = account.id;
                        folder.name = fullName;
                        folder.display = display;
                        folder.type = (EntityFolder.SYSTEM.equals(type) ? type : EntityFolder.USER);
                        folder.level = level;
                        folder.synchronize = false;
                        folder.poll = ("imap.gmail.com".equals(account.host));
                        folder.sync_days = EntityFolder.DEFAULT_SYNC;
                        folder.keep_days = EntityFolder.DEFAULT_KEEP;
                        db.folder().insertFolder(folder);
                        Log.i(folder.name + " added type=" + folder.type);
                    } else {
                        Log.i(folder.name + " exists type=" + folder.type);

                        if (folder.display == null) {
                            if (display != null) {
                                db.folder().setFolderDisplay(folder.id, display);
                                EntityLog.log(this, account.name + ":" + folder.name +
                                        " removed prefix display=" + display + " separator=" + separator);
                            }
                        } else {
                            if (account.prefix == null && folder.name.endsWith(separator + folder.display)) {
                                db.folder().setFolderDisplay(folder.id, null);
                                EntityLog.log(this, account.name + ":" + folder.name +
                                        " restored prefix display=" + folder.display + " separator=" + separator);
                            }
                        }

                        db.folder().setFolderLevel(folder.id, level);

                        // Compatibility
                        if ("Inbox_sub".equals(folder.type))
                            db.folder().setFolderType(folder.id, EntityFolder.USER);
                        if (EntityFolder.USER.equals(folder.type) && EntityFolder.SYSTEM.equals(type))
                            db.folder().setFolderType(folder.id, type);
                        if (EntityFolder.SYSTEM.equals(folder.type) && EntityFolder.USER.equals(type))
                            db.folder().setFolderType(folder.id, type);
                    }
                }
            }

            Log.i("Delete local count=" + names.size());
            for (String name : names) {
                Log.i(name + " delete");
                db.folder().deleteFolder(account.id, name);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i("End sync folder");
        }
    }

    private void synchronizeMessages(EntityAccount account, final EntityFolder folder, IMAPFolder ifolder, JSONArray jargs, ServiceState state) throws JSONException, MessagingException, IOException {
        final DB db = DB.getInstance(this);
        try {
            int sync_days = jargs.getInt(0);
            int keep_days = jargs.getInt(1);
            boolean download = jargs.getBoolean(2);

            if (keep_days == sync_days)
                keep_days++;

            Log.i(folder.name + " start sync after=" + sync_days + "/" + keep_days);

            db.folder().setFolderSyncState(folder.id, "syncing");

            // Get reference times
            Calendar cal_sync = Calendar.getInstance();
            cal_sync.add(Calendar.DAY_OF_MONTH, -sync_days);
            cal_sync.set(Calendar.HOUR_OF_DAY, 0);
            cal_sync.set(Calendar.MINUTE, 0);
            cal_sync.set(Calendar.SECOND, 0);
            cal_sync.set(Calendar.MILLISECOND, 0);

            Calendar cal_keep = Calendar.getInstance();
            cal_keep.add(Calendar.DAY_OF_MONTH, -keep_days);
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

            Log.i(folder.name + " sync=" + new Date(sync_time) + " keep=" + new Date(keep_time));

            // Delete old local messages
            int old = db.message().deleteMessagesBefore(folder.id, keep_time, false);
            Log.i(folder.name + " local old=" + old);

            // Get list of local uids
            final List<Long> uids = db.message().getUids(folder.id, null);
            Log.i(folder.name + " local count=" + uids.size());

            // Reduce list of local uids
            SearchTerm searchTerm = new ReceivedDateTerm(ComparisonTerm.GE, new Date(sync_time));
            if (ifolder.getPermanentFlags().contains(Flags.Flag.FLAGGED))
                searchTerm = new OrTerm(searchTerm, new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));

            long search = SystemClock.elapsedRealtime();
            Message[] imessages = ifolder.search(searchTerm);
            Log.i(folder.name + " remote count=" + imessages.length +
                    " search=" + (SystemClock.elapsedRealtime() - search) + " ms");

            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(FetchProfile.Item.FLAGS);
            ifolder.fetch(imessages, fp);

            long fetch = SystemClock.elapsedRealtime();
            Log.i(folder.name + " remote fetched=" + (SystemClock.elapsedRealtime() - fetch) + " ms");

            for (int i = 0; i < imessages.length && state.running(); i++)
                try {
                    uids.remove(ifolder.getUID(imessages[i]));
                } catch (MessageRemovedException ex) {
                    Log.w(folder.name, ex);
                } catch (Throwable ex) {
                    Log.e(folder.name, ex);
                    reportError(account, folder, ex);
                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                }

            if (uids.size() > 0) {
                ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                    @Override
                    public Object doCommand(IMAPProtocol protocol) {
                        Log.i("Executing uid fetch count=" + uids.size());
                        Response[] responses = protocol.command(
                                "UID FETCH " + TextUtils.join(",", uids) + " (UID)", null);

                        for (int i = 0; i < responses.length; i++) {
                            if (responses[i] instanceof FetchResponse) {
                                FetchResponse fr = (FetchResponse) responses[i];
                                UID uid = fr.getItem(UID.class);
                                if (uid != null)
                                    uids.remove(uid.uid);
                            } else {
                                if (responses[i].isOK())
                                    Log.i(folder.name + " response=" + responses[i]);
                                else {
                                    Log.e(folder.name + " response=" + responses[i]);
                                    db.folder().setFolderError(folder.id, responses[i].toString());
                                }
                            }
                        }
                        return null;
                    }
                });

                long getuid = SystemClock.elapsedRealtime();
                Log.i(folder.name + " remote uids=" + (SystemClock.elapsedRealtime() - getuid) + " ms");
            }

            // Delete local messages not at remote
            Log.i(folder.name + " delete=" + uids.size());
            for (Long uid : uids) {
                int count = db.message().deleteMessage(folder.id, uid);
                Log.i(folder.name + " delete local uid=" + uid + " count=" + count);
            }

            List<EntityRule> rules = db.rule().getEnabledRules(folder.id);

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
            Log.i(folder.name + " add=" + imessages.length);
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
                    Log.i(folder.name + " fetched headers=" + full.size() +
                            " " + (SystemClock.elapsedRealtime() - headers) + " ms");
                }

                for (int j = isub.length - 1; j >= 0 && state.running(); j--)
                    try {
                        db.beginTransaction();
                        EntityMessage message = synchronizeMessage(
                                this,
                                folder, ifolder, (IMAPMessage) isub[j],
                                false,
                                rules);
                        ids[from + j] = message.id;
                        db.setTransactionSuccessful();
                    } catch (MessageRemovedException ex) {
                        Log.w(folder.name, ex);
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (IOException ex) {
                        if (ex.getCause() instanceof MessagingException) {
                            Log.w(folder.name, ex);
                            if (!(ex.getCause() instanceof MessageRemovedException))
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                        } else
                            throw ex;
                    } catch (Throwable ex) {
                        Log.e(folder.name, ex);
                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                    } finally {
                        db.endTransaction();
                        // Reduce memory usage
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }
            }

            // Delete not synchronized messages without uid
            db.message().deleteOrphans(folder.id);

            // Add local sent messages to remote sent folder
            if (EntityFolder.SENT.equals(folder.type)) {
                List<EntityMessage> orphans = db.message().getSentOrphans(folder.id);
                Log.i(folder.name + " sent orphans=" + orphans.size());
                for (EntityMessage orphan : orphans) {
                    Log.i(folder.name + " adding orphan id=" + orphan.id);
                    EntityOperation.queue(this, db, orphan, EntityOperation.ADD);
                    db.message().setMessageUiBrowsed(orphan.id, false); // Prevent adding again
                }
            }

            if (download) {
                db.folder().setFolderSyncState(folder.id, "downloading");

                //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);

                // Download messages/attachments
                Log.i(folder.name + " download=" + imessages.length);
                for (int i = imessages.length - 1; i >= 0 && state.running(); i -= DOWNLOAD_BATCH_SIZE) {
                    int from = Math.max(0, i - DOWNLOAD_BATCH_SIZE + 1);

                    Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);
                    // Fetch on demand

                    for (int j = isub.length - 1; j >= 0 && state.running(); j--)
                        try {
                            db.beginTransaction();
                            if (ids[from + j] != null)
                                downloadMessage(
                                        this,
                                        folder, ifolder,
                                        (IMAPMessage) isub[j], ids[from + j]);
                            db.setTransactionSuccessful();
                        } catch (FolderClosedException ex) {
                            throw ex;
                        } catch (FolderClosedIOException ex) {
                            throw ex;
                        } catch (Throwable ex) {
                            Log.e(folder.name, ex);
                        } finally {
                            db.endTransaction();
                            // Free memory
                            ((IMAPMessage) isub[j]).invalidateHeaders();
                        }
                }
            }

            if (state.running)
                db.folder().setFolderInitialized(folder.id);

            db.folder().setFolderSync(folder.id, new Date().getTime());
            db.folder().setFolderError(folder.id, null);

        } finally {
            Log.i(folder.name + " end sync state=" + state);
            db.folder().setFolderSyncState(folder.id, null);
        }
    }

    static EntityMessage synchronizeMessage(
            Context context,
            EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage,
            boolean browsed,
            List<EntityRule> rules) throws MessagingException, IOException {
        long uid = ifolder.getUID(imessage);

        if (imessage.isExpunged()) {
            Log.i(folder.name + " expunged uid=" + uid);
            throw new MessageRemovedException();
        }
        if (imessage.isSet(Flags.Flag.DELETED)) {
            Log.i(folder.name + " deleted uid=" + uid);
            throw new MessageRemovedException();
        }

        MessageHelper helper = new MessageHelper(imessage);
        boolean seen = helper.getSeen();
        boolean answered = helper.getAnsered();
        boolean flagged = helper.getFlagged();
        String flags = helper.getFlags();
        String[] keywords = helper.getKeywords();
        boolean filter = false;

        DB db = DB.getInstance(context);

        // Find message by uid (fast, no headers required)
        EntityMessage message = db.message().getMessageByUid(folder.id, uid);

        // Find message by Message-ID (slow, headers required)
        // - messages in inbox have same id as message sent to self
        // - messages in archive have same id as original
        if (message == null) {
            // Will fetch headers within database transaction
            String msgid = helper.getMessageID();
            Log.i(folder.name + " searching for " + msgid);
            for (EntityMessage dup : db.message().getMessageByMsgId(folder.account, msgid)) {
                EntityFolder dfolder = db.folder().getFolder(dup.folder);
                Log.i(folder.name + " found as id=" + dup.id + "/" + dup.uid +
                        " folder=" + dfolder.type + ":" + dup.folder + "/" + folder.type + ":" + folder.id +
                        " msgid=" + dup.msgid + " thread=" + dup.thread);

                if (dup.folder.equals(folder.id)) {
                    String thread = helper.getThreadId(uid);
                    Log.i(folder.name + " found as id=" + dup.id +
                            " uid=" + dup.uid + "/" + uid +
                            " msgid=" + msgid + " thread=" + thread);
                    dup.folder = folder.id; // outbox to sent

                    if (dup.uid == null) {
                        Log.i(folder.name + " set uid=" + uid);
                        dup.uid = uid;
                        filter = true;
                    } else
                        Log.w(folder.name + " changed uid=" + dup.uid + " -> " + uid);

                    dup.msgid = msgid;
                    dup.thread = thread;
                    dup.error = null;
                    db.message().updateMessage(dup);
                    message = dup;
                }
            }

            if (message == null)
                filter = true;
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
                Log.w("No Message-ID id=" + message.id + " uid=" + message.uid);

            message.references = TextUtils.join(" ", helper.getReferences());
            message.inreplyto = helper.getInReplyTo();
            message.deliveredto = helper.getDeliveredTo();
            message.thread = helper.getThreadId(uid);
            message.sender = MessageHelper.getSortKey(helper.getFrom());
            message.from = helper.getFrom();
            message.to = helper.getTo();
            message.cc = helper.getCc();
            message.bcc = helper.getBcc();
            message.reply = helper.getReply();
            message.subject = helper.getSubject();
            message.size = helper.getSize();
            message.content = false;
            message.received = imessage.getReceivedDate().getTime();
            message.sent = (imessage.getSentDate() == null ? null : imessage.getSentDate().getTime());
            message.seen = seen;
            message.answered = answered;
            message.flagged = flagged;
            message.flags = flags;
            message.keywords = keywords;
            message.ui_seen = seen;
            message.ui_answered = answered;
            message.ui_flagged = flagged;
            message.ui_hide = false;
            message.ui_found = false;
            message.ui_ignored = seen;
            message.ui_browsed = browsed;

            Uri lookupUri = ContactInfo.getLookupUri(context, message.from);
            message.avatar = (lookupUri == null ? null : lookupUri.toString());

            // Check sender
            Address sender = helper.getSender();
            if (sender != null && senders.length > 0) {
                String[] f = ((InternetAddress) senders[0]).getAddress().split("@");
                String[] s = ((InternetAddress) sender).getAddress().split("@");
                if (f.length > 1 && s.length > 1) {
                    if (!f[1].equals(s[1]))
                        message.warning = context.getString(R.string.title_via, s[1]);
                }
            }

            message.id = db.message().insertMessage(message);

            Log.i(folder.name + " added id=" + message.id + " uid=" + message.uid);

            int sequence = 1;
            MessageHelper.MessageParts parts = helper.getMessageParts();
            for (EntityAttachment attachment : parts.getAttachments()) {
                Log.i(folder.name + " attachment seq=" + sequence +
                        " name=" + attachment.name + " type=" + attachment.type +
                        " cid=" + attachment.cid + " pgp=" + attachment.encryption);
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
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " seen=" + seen);
            }

            if (!message.answered.equals(answered) || !message.answered.equals(message.ui_answered)) {
                update = true;
                message.answered = answered;
                message.ui_answered = answered;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " answered=" + answered);
            }

            if (!message.flagged.equals(flagged) || !message.flagged.equals(message.ui_flagged)) {
                update = true;
                message.flagged = flagged;
                message.ui_flagged = flagged;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " flagged=" + flagged);
            }

            if (flags == null ? message.flags != null : !flags.equals(message.flags)) {
                update = true;
                message.flags = flags;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " flags=" + flags);
            }

            if (!Helper.equal(message.keywords, keywords)) {
                update = true;
                message.keywords = keywords;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid +
                        " keywords=" + TextUtils.join(" ", keywords));
            }

            if (message.ui_hide && db.operation().getOperationCount(folder.id, message.id) == 0) {
                update = true;
                message.ui_hide = false;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " unhide");
            }

            if (message.ui_browsed) {
                update = true;
                message.ui_browsed = false;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " unbrowse");
            }

            if (message.avatar == null) {
                Uri lookupUri = ContactInfo.getLookupUri(context, message.from);
                if (lookupUri != null) {
                    update = true;
                    message.avatar = lookupUri.toString();
                    Log.i(folder.name + " updated id=" + message.id + " lookup=" + lookupUri);
                }
            }

            if (update)
                db.message().updateMessage(message);
            else
                Log.i(folder.name + " unchanged uid=" + uid);
        }

        List<String> fkeywords = new ArrayList<>(Arrays.asList(folder.keywords));

        for (String keyword : keywords)
            if (!fkeywords.contains(keyword)) {
                Log.i(folder.name + " adding keyword=" + keyword);
                fkeywords.add(keyword);
            }

        if (folder.keywords.length != fkeywords.size()) {
            Collections.sort(fkeywords);
            db.folder().setFolderKeywords(folder.id, DB.Converters.fromStringArray(fkeywords.toArray(new String[0])));
        }

        if (filter && Helper.isPro(context))
            try {
                for (EntityRule rule : rules)
                    if (rule.matches(context, message, imessage)) {
                        rule.execute(context, db, message);
                        if (rule.stop)
                            break;
                    }
            } catch (Throwable ex) {
                Log.e(ex);
                db.message().setMessageError(message.id, Helper.formatThrowable(ex));
            }

        return message;
    }

    static void downloadMessage(
            Context context,
            EntityFolder folder, IMAPFolder ifolder,
            IMAPMessage imessage, long id) throws MessagingException, IOException {
        DB db = DB.getInstance(context);
        EntityMessage message = db.message().getMessage(id);
        if (message == null)
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long maxSize = prefs.getInt("download", 32768);
        if (maxSize == 0)
            maxSize = Long.MAX_VALUE;

        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
        MessageHelper helper = new MessageHelper(imessage);
        Boolean isMetered = Helper.isMetered(context, false);
        boolean metered = (isMetered == null || isMetered);

        boolean fetch = false;
        if (!message.content)
            if (!metered || (message.size != null && message.size < maxSize))
                fetch = true;

        if (!fetch)
            for (EntityAttachment attachment : attachments)
                if (!attachment.available)
                    if (!metered || (attachment.size != null && attachment.size < maxSize)) {
                        fetch = true;
                        break;
                    }

        if (fetch) {
            Log.i(folder.name + " fetching message id=" + message.id);
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

            MessageHelper.MessageParts parts = helper.getMessageParts();

            if (!message.content) {
                if (!metered || (message.size != null && message.size < maxSize)) {
                    String body = parts.getHtml(context);
                    Helper.writeText(EntityMessage.getFile(context, message.id), body);
                    db.message().setMessageContent(message.id, true, HtmlHelper.getPreview(body));
                    db.message().setMessageWarning(message.id, parts.getWarnings(message.warning));
                    Log.i(folder.name + " downloaded message id=" + message.id + " size=" + message.size);
                }
            }

            for (EntityAttachment attachment : attachments)
                if (!attachment.available)
                    if (!metered || (attachment.size != null && attachment.size < maxSize))
                        if (!parts.downloadAttachment(context, db, attachment.id, attachment.sequence))
                            break;
        }
    }

    private class ServiceManager extends ConnectivityManager.NetworkCallback {
        private ServiceState state;
        private boolean started = false;
        private int queued = 0;
        private long lastLost = 0;
        private ExecutorService queue = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

        @Override
        public void onAvailable(Network network) {
            synchronized (this) {
                try {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    EntityLog.log(ServiceSynchronize.this, "Available " + network + " " + cm.getNetworkInfo(network));

                    if (!started && suitableNetwork())
                        queue_reload(true, "connect " + network);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
            synchronized (this) {
                try {
                    if (!started) {
                        EntityLog.log(ServiceSynchronize.this, "Network " + network + " capabilities " + capabilities);
                        if (suitableNetwork())
                            queue_reload(true, "capabilities " + network);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }

        @Override
        public void onLost(Network network) {
            synchronized (this) {
                try {
                    EntityLog.log(ServiceSynchronize.this, "Lost " + network);

                    if (started && !suitableNetwork()) {
                        lastLost = new Date().getTime();
                        queue_reload(false, "disconnect " + network);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }

        private boolean suitableNetwork() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServiceSynchronize.this);
            boolean metered = prefs.getBoolean("metered", true);

            Boolean isMetered = Helper.isMetered(ServiceSynchronize.this, true);

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

        private void service_reload(String reason) {
            synchronized (this) {
                try {
                    serviceManager.queue_reload(true, reason);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }

        private void service_destroy() {
            synchronized (this) {
                EntityLog.log(ServiceSynchronize.this, "Service destroy");
                if (started)
                    queue_reload(false, "service destroy");
            }
        }

        private void start() {
            EntityLog.log(ServiceSynchronize.this, "Main start");

            state = new ServiceState();
            state.runnable(new Runnable() {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
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
                                Log.w("main backoff " + ex.toString());
                            }

                        // Start monitoring outbox
                        Handler handler = null;
                        final EntityFolder outbox = db.folder().getOutbox();
                        if (outbox != null) {
                            db.folder().setFolderError(outbox.id, null);

                            handler = new Handler(getMainLooper()) {
                                private List<Long> handling = new ArrayList<>();
                                private LiveData<List<EntityOperation>> liveOperations;

                                @Override
                                public void handleMessage(android.os.Message msg) {
                                    Log.i(outbox.name + " observe=" + msg.what);
                                    if (msg.what == 0) {
                                        liveOperations.removeObserver(observer);
                                        handling.clear();
                                    } else {
                                        liveOperations = db.operation().liveOperations(outbox.id);
                                        liveOperations.observe(ServiceSynchronize.this, observer);
                                    }
                                }

                                private Observer<List<EntityOperation>> observer = new Observer<List<EntityOperation>>() {
                                    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);
                                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wl = pm.newWakeLock(
                                            PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":outbox");

                                    @Override
                                    public void onChanged(final List<EntityOperation> operations) {
                                        boolean process = false;
                                        List<Long> current = new ArrayList<>();
                                        for (EntityOperation op : operations) {
                                            if (!handling.contains(op.id))
                                                process = true;
                                            current.add(op.id);
                                        }
                                        handling = current;

                                        if (handling.size() > 0 && process) {
                                            Log.i(outbox.name + " operations=" + operations.size());
                                            executor.submit(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        wl.acquire();
                                                        Log.i(outbox.name + " process");

                                                        db.folder().setFolderSyncState(outbox.id, "syncing");
                                                        processOperations(null, outbox, null, null, null, state);
                                                        db.folder().setFolderError(outbox.id, null);
                                                    } catch (Throwable ex) {
                                                        Log.e(outbox.name, ex);
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
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                                if (account.notify)
                                    account.createNotificationChannel(ServiceSynchronize.this);
                                else
                                    account.deleteNotificationChannel(ServiceSynchronize.this);

                            Log.i(account.host + "/" + account.user + " run");
                            final ServiceState astate = new ServiceState();
                            astate.runnable(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        monitorAccount(account, astate);
                                    } catch (Throwable ex) {
                                        Log.e(ex);
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
                            Log.w("main wait " + ex.toString());
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
                            Log.i(outbox.name + " unlisten operations");
                            handler.sendEmptyMessage(0);
                            db.folder().setFolderState(outbox.id, null);
                        }

                        EntityLog.log(ServiceSynchronize.this, "Main exited");
                    } catch (Throwable ex) {
                        // Fail-safe
                        Log.e(ex);
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

            EntityLog.log(ServiceSynchronize.this, "Queue reload " +
                    " doStop=" + doStop + " doStart=" + doStart + " queued=" + queued + " " + reason);

            started = doStart;

            queued++;
            queue.submit(new Runnable() {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
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

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            for (EntityAccount account : db.account().getAccountsTbd())
                                nm.deleteNotificationChannel(EntityAccount.getNotificationChannelName(account.id));
                        }

                        int accounts = db.account().deleteAccountsTbd();
                        int identities = db.identity().deleteIdentitiesTbd();
                        if (accounts > 0 || identities > 0)
                            Log.i("Deleted accounts=" + accounts + " identities=" + identities);

                        if (doStart)
                            start();

                    } catch (Throwable ex) {
                        Log.e(ex);
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
            thread.setPriority(THREAD_PRIORITY_BACKGROUND);
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

        private void yield() {
            try {
                // Give interrupted thread some time to acquire wake lock
                Thread.sleep(YIELD_DURATION);
            } catch (InterruptedException ignored) {
            }
        }

        void start() {
            thread.start();
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
                    Log.i("Joining " + thread.getName());
                    thread.join();
                    joined = true;
                    Log.i("Joined " + thread.getName());
                } catch (InterruptedException ex) {
                    Log.w(thread.getName() + " join " + ex.toString());
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
