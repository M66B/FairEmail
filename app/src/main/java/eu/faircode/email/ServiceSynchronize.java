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
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;

import androidx.annotation.Nullable;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.sun.mail.imap.IMAPFolder;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.ReadOnlyFolderException;
import javax.mail.StoreClosedException;
import javax.mail.event.FolderAdapter;
import javax.mail.event.FolderEvent;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;

import me.leolin.shortcutbadger.ShortcutBadger;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class ServiceSynchronize extends ServiceBase {
    private ConnectionHelper.NetworkState networkState = new ConnectionHelper.NetworkState();
    private Core.State state = null;
    private int lastStartId = -1;
    private boolean started = false;
    private int queued = 0;
    private long lastLost = 0;
    private TupleAccountStats lastStats = new TupleAccountStats();
    private ExecutorService queue = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private static boolean sync = true;
    private static boolean oneshot = false;

    private static final int CONNECT_BACKOFF_START = 8; // seconds
    private static final int CONNECT_BACKOFF_MAX = 64; // seconds (totally 2 minutes)
    private static final int CONNECT_BACKOFF_AlARM = 15; // minutes
    private static final long RECONNECT_BACKOFF = 90 * 1000L; // milliseconds
    private static final int ACCOUNT_ERROR_AFTER = 60; // minutes
    private static final int BACKOFF_ERROR_AFTER = 16; // seconds
    private static final long ONESHOT_DURATION = 90 * 1000L; // milliseconds
    private static final long STOP_DELAY = 5000L; // milliseconds
    private static final long CHECK_ALIVE_INTERVAL = 19 * 60 * 1000L; // milliseconds

    static final int PI_ALARM = 1;
    static final int PI_ONESHOT = 2;

    @Override
    public void onCreate() {
        EntityLog.log(this, "Service create version=" + BuildConfig.VERSION_NAME);
        super.onCreate();
        startForeground(Helper.NOTIFICATION_SYNCHRONIZE, getNotificationService(null).build());

        // Listen for network changes
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        // Removed because of Android VPN service
        // builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        cm.registerNetworkCallback(builder.build(), onNetworkCallback);

        registerReceiver(onScreenOff, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        DB db = DB.getInstance(this);

        db.account().liveStats().observe(this, new Observer<TupleAccountStats>() {
            private TupleAccountStats lastStats = null;

            @Override
            public void onChanged(@Nullable TupleAccountStats stats) {
                if (stats != null && !stats.equals(lastStats)) {
                    try {
                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.notify(Helper.NOTIFICATION_SYNCHRONIZE, getNotificationService(stats).build());
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                    if (oneshot && stats.operations > 0)
                        onOneshot(true);
                }

                lastStats = stats;
            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        db.message().liveUnseenWidget().observe(this, new Observer<TupleMessageStats>() {
            private Integer lastUnseen = null;

            @Override
            public void onChanged(TupleMessageStats stats) {
                if (stats == null)
                    stats = new TupleMessageStats();

                boolean unseen_ignored = prefs.getBoolean("unseen_ignored", false);
                Integer unseen = (unseen_ignored ? stats.notifying : stats.unseen);
                if (unseen == null)
                    unseen = 0;

                if (lastUnseen == null || !lastUnseen.equals(unseen)) {
                    Log.i("Stats " + stats);
                    lastUnseen = unseen;
                    setUnseen(unseen);
                }
            }
        });

        final TwoStateOwner cowner = new TwoStateOwner(this, "liveUnseenNotify");

        db.folder().liveSynchronizing().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                Log.i("Synchronizing folders=" + count);
                if (count == null || count == 0)
                    cowner.start();
                else
                    cowner.stop();
            }
        });

        Map<Long, List<Long>> groupNotifying = new HashMap<>();

        // Get existing notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            try {
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                for (StatusBarNotification sbn : nm.getActiveNotifications()) {
                    String tag = sbn.getTag();
                    if (tag != null && tag.startsWith("unseen.")) {
                        String[] p = tag.split(("\\."));
                        long group = Long.parseLong(p[1]);
                        long id = sbn.getNotification().extras.getLong("id", 0);

                        if (!groupNotifying.containsKey(group))
                            groupNotifying.put(group, new ArrayList<>());

                        if (id > 0) {
                            Log.i("Notify restore " + tag + " id=" + id);
                            groupNotifying.get(group).add(id);
                        }
                    }
                }
            } catch (Throwable ex) {
                Log.w(ex);
                /*
                    java.lang.RuntimeException: Unable to create service eu.faircode.email.ServiceSynchronize: java.lang.NullPointerException: Attempt to invoke virtual method 'java.util.List android.content.pm.ParceledListSlice.getList()' on a null object reference
                            at android.app.ActivityThread.handleCreateService(ActivityThread.java:2944)
                            at android.app.ActivityThread.access$1900(ActivityThread.java:154)
                            at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1474)
                            at android.os.Handler.dispatchMessage(Handler.java:102)
                            at android.os.Looper.loop(Looper.java:234)
                            at android.app.ActivityThread.main(ActivityThread.java:5526)
                */
            }

        db.message().liveUnseenNotify().observe(cowner, new Observer<List<TupleMessageEx>>() {
            private ExecutorService executor =
                    Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

            @Override
            public void onChanged(final List<TupleMessageEx> messages) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Core.notifyMessages(ServiceSynchronize.this, messages, groupNotifying);
                        } catch (SecurityException ex) {
                            Log.w(ex);
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServiceSynchronize.this);
                            prefs.edit().remove("sound").apply();
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
            }
        });

        db.message().liveWidgetUnified(false, false, false).observe(this, new Observer<List<TupleMessageWidget>>() {
            private List<TupleMessageWidget> last = null;

            @Override
            public void onChanged(List<TupleMessageWidget> messages) {
                if (messages == null)
                    messages = new ArrayList<>();

                boolean changed = false;
                if (last != null && last.size() == messages.size()) {
                    for (int i = 0; i < last.size(); i++) {
                        TupleMessageWidget m1 = last.get(i);
                        TupleMessageWidget m2 = messages.get(i);
                        if (!m1.id.equals(m2.id) ||
                                !Objects.equals(m1.account, m2.account) ||
                                !Objects.equals(m1.accountName, m2.accountName) ||
                                !MessageHelper.equal(m1.from, m2.from) ||
                                !m1.received.equals(m2.received) ||
                                !Objects.equals(m1.subject, m2.subject) ||
                                !(m1.unseen == m2.unseen) ||
                                !(m1.unflagged == m2.unflagged)) {
                            changed = true;
                            break;
                        }
                    }
                } else
                    changed = true;

                last = messages;

                if (changed)
                    WidgetUnified.update(ServiceSynchronize.this);
            }
        });
    }

    @Override
    public void onDestroy() {
        EntityLog.log(this, "Service destroy");

        unregisterReceiver(onScreenOff);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(onNetworkCallback);

        setUnseen(null);

        if (state != null && state.isRunning()) {
            Log.e("Destroy while monitor running ignoring=" + Helper.isIgnoringOptimizations(this));
            state.stop();
        }

        try {
            stopForeground(true);
        } catch (Throwable ex) {
            Log.e(ex);
/*
            OnePlus A6013 - Android 9

            java.lang.RuntimeException: Unable to stop service eu.faircode.email.ServiceSynchronize@3995fc9: java.lang.NullPointerException: Attempt to invoke virtual method 'long java.lang.Long.longValue()' on a null object reference
                    at android.app.ActivityThread.handleStopService(ActivityThread.java:3908)
                    at android.app.ActivityThread.access$1900(ActivityThread.java:209)
                    at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1826)
                    at android.os.Handler.dispatchMessage(Handler.java:106)
                    at android.os.Looper.loop(Looper.java:193)
                    at android.app.ActivityThread.main(ActivityThread.java:6954)
                    at java.lang.reflect.Method.invoke(Method.java:-2)
                    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:537)
                    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:858)
            Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'long java.lang.Long.longValue()' on a null object reference
                    at android.os.Parcel.createException(Parcel.java:1956)
                    at android.os.Parcel.readException(Parcel.java:1918)
                    at android.os.Parcel.readException(Parcel.java:1868)
                    at android.app.IActivityManager$Stub$Proxy.setServiceForeground(IActivityManager.java:5111)
                    at android.app.Service.stopForeground(Service.java:724)
                    at android.app.Service.stopForeground(Service.java:710)
*/
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(Helper.NOTIFICATION_SYNCHRONIZE);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lastStartId = startId;
        String action = (intent == null ? null : intent.getAction());
        Log.i("Service command intent=" + intent + " action=" + action);
        Log.logExtras(intent);

        super.onStartCommand(intent, flags, startId);
        startForeground(Helper.NOTIFICATION_SYNCHRONIZE, getNotificationService(null).build());

        if (action != null)
            try {
                switch (action) {
                    case "alarm":
                        onAlarm();
                        break;

                    case "reload":
                        onReload(
                                intent.getBooleanExtra("clear", false),
                                intent.getStringExtra("reason"));
                        break;

                    case "reset":
                        onReset();
                        break;

                    case "oneshot_start":
                        onOneshot(true);
                        break;

                    case "oneshot_end":
                        onOneshot(false);
                        break;

                    case "watchdog":
                        onWatchdog();
                        break;

                    default:
                        Log.w("Unknown action: " + action);
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }

        return START_STICKY;
    }

    private NotificationCompat.Builder getNotificationService(TupleAccountStats stats) {
        if (stats != null)
            lastStats = stats;

        // Build pending intent
        Intent why = new Intent(this, ActivityView.class);
        why.setAction("why");
        PendingIntent piWhy = PendingIntent.getActivity(this, ActivityView.REQUEST_WHY, why, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "service")
                        .setSmallIcon(R.drawable.baseline_compare_arrows_white_24)
                        .setContentTitle(getResources().getQuantityString(
                                R.plurals.title_notification_synchronizing, lastStats.accounts, lastStats.accounts))
                        .setContentIntent(piWhy)
                        .setAutoCancel(false)
                        .setShowWhen(false)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        if (lastStats.operations > 0)
            builder.setContentText(getResources().getQuantityString(
                    R.plurals.title_notification_operations, lastStats.operations, lastStats.operations));

        if (!networkState.isSuitable())
            builder.setSubText(getString(R.string.title_notification_waiting));

        return builder;
    }

    private void setUnseen(Integer unseen) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean badge = prefs.getBoolean("badge", true);

        Widget.update(this, unseen);

        try {
            if (unseen == null || !badge)
                ShortcutBadger.removeCount(this);
            else
                ShortcutBadger.applyCount(this, unseen);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onAlarm() {
        schedule(this);
        onReload(true, "alarm");
    }

    private void onReload(boolean clear, String reason) {
        synchronized (this) {
            try {
                queue_reload(true, clear, reason);
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }
    }

    private void onReset() {
        lastLost = 0;
        onReload(true, "reset");
    }

    private void onOneshot(boolean start) {
        Log.i("Oneshot start=" + start);

        Intent alarm = new Intent(this, ServiceSynchronize.class);
        alarm.setAction("oneshot_end");
        PendingIntent piOneshot;

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            piOneshot = PendingIntent.getService(this, PI_ONESHOT, alarm, PendingIntent.FLAG_UPDATE_CURRENT);
        else
            piOneshot = PendingIntent.getForegroundService(this, PI_ONESHOT, alarm, PendingIntent.FLAG_UPDATE_CURRENT);

        am.cancel(piOneshot);

        oneshot = start;

        if (start) {
            // Network events will manage the service
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ONESHOT_DURATION, piOneshot);
            else
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ONESHOT_DURATION, piOneshot);
        } else
            onReload(true, "oneshot end");
    }

    private void onWatchdog() {
        EntityLog.log(this, "Service watchdog");
        // Network events will manage the service
    }

    private void queue_reload(final boolean start, final boolean clear, final String reason) {
        final boolean doStop = started;
        final boolean doStart = (start && isEnabled() && networkState.isSuitable());

        EntityLog.log(this, "Queue reload" +
                " doStop=" + doStop + " doStart=" + doStart + " queued=" + queued + " reason=" + reason);

        started = doStart;

        try {
            queued++;
            queue.submit(new Runnable() {
                private PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                private PowerManager.WakeLock wl = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":manage");

                @Override
                public void run() {
                    DB db = DB.getInstance(ServiceSynchronize.this);

                    try {
                        wl.acquire();

                        EntityLog.log(ServiceSynchronize.this, "Reload" +
                                " stop=" + doStop + " start=" + doStart + " queued=" + queued + " " + reason);

                        Map<String, String> crumb = new HashMap<>();
                        crumb.put("oneshot", Boolean.toString(oneshot));
                        crumb.put("started", Boolean.toString(started));
                        crumb.put("doStop", Boolean.toString(doStop));
                        crumb.put("doStart", Boolean.toString(doStart));
                        crumb.put("queued", Integer.toString(queued));
                        crumb.put("reason", reason == null ? "" : reason);
                        crumb.put("connected", Boolean.toString(networkState.isConnected()));
                        crumb.put("suitable", Boolean.toString(networkState.isSuitable()));
                        crumb.put("unmetered", Boolean.toString(networkState.isUnmetered()));
                        crumb.put("roaming", Boolean.toString(networkState.isRoaming()));
                        crumb.put("lastLost", new Date(lastLost).toString());
                        Log.breadcrumb("reload", crumb);

                        if (doStop)
                            stop();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            for (EntityAccount account : db.account().getAccountsTbd())
                                nm.deleteNotificationChannel(EntityAccount.getNotificationChannelId(account.id));
                        }

                        int accounts = db.account().deleteAccountsTbd();
                        int identities = db.identity().deleteIdentitiesTbd();
                        if (accounts > 0 || identities > 0)
                            Log.i("Deleted accounts=" + accounts + " identities=" + identities);

                        if (doStart) {
                            if (clear)
                                db.account().clearAccountConnected();
                            start(!oneshot || sync);
                        }

                    } catch (Throwable ex) {
                        Log.e(ex);
                    } finally {
                        queued--;
                        EntityLog.log(ServiceSynchronize.this, "Reload done queued=" + queued);

                        if (!doStart && queued == 0 && !isEnabled()) {
                            try {
                                Thread.sleep(STOP_DELAY);
                            } catch (InterruptedException ignored) {
                            }
                            if (queued == 0 && !isEnabled())
                                stopService(lastStartId);
                        }

                        wl.release();
                    }
                }
            });
        } catch (RejectedExecutionException ex) {
            Log.w(ex);
        }
    }

    private boolean isEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = prefs.getInt("poll_interval", 0);
        return ((enabled && pollInterval == 0) || oneshot);
    }

    private void start(final boolean sync) {
        EntityLog.log(this, "Main start");

        final Thread main = Thread.currentThread();

        if (state != null && state.isRunning()) {
            Log.e("Monitor running ignoring=" + Helper.isIgnoringOptimizations(this));
            state.stop();
        }

        state = new Core.State(networkState);
        state.runnable(new Runnable() {
            private PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            private PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":main");

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

                    // Start monitoring accounts
                    List<EntityAccount> accounts = db.account().getSynchronizingAccounts();
                    for (final EntityAccount account : accounts) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (account.notify)
                                account.createNotificationChannel(ServiceSynchronize.this);
                            else
                                account.deleteNotificationChannel(ServiceSynchronize.this);
                        }

                        Log.i(account.host + "/" + account.user + " run");
                        final Core.State astate = new Core.State(state);
                        astate.runnable(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    monitorAccount(account, astate, sync);
                                } catch (Throwable ex) {
                                    Log.e(account.name, ex);
                                }
                            }
                        }, "sync.account." + account.id);
                        astate.start();
                        state.childs.add(astate);
                    }

                    EntityLog.log(ServiceSynchronize.this, "Main started");

                    try {
                        wl.release();
                        while (!state.acquire(CHECK_ALIVE_INTERVAL)) {
                            if (!main.isAlive()) {
                                Log.e("Main thread died");
                                state.stop();
                            }
                        }
                    } catch (InterruptedException ex) {
                        Log.w("main wait " + ex.toString());
                    } finally {
                        wl.acquire();
                    }

                    // Stop monitoring accounts
                    for (Core.State astate : state.childs)
                        astate.stop();
                    for (Core.State astate : state.childs)
                        astate.join();
                    state.childs.clear();

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
        EntityLog.log(this, "Main stop");

        state.stop();
        state.join();

        EntityLog.log(this, "Main stopped");

        state = null;
    }

    private void stopService(int startId) {
        EntityLog.log(this, "Service stop");

        DB db = DB.getInstance(this);
        List<EntityOperation> ops = db.operation().getOperations(EntityOperation.SYNC);
        for (EntityOperation op : ops)
            db.folder().setFolderSyncState(op.folder, null);

        stopSelf(startId);
    }

    private void monitorAccount(final EntityAccount account, final Core.State state, final boolean sync) throws NoSuchProviderException {
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wlAccount = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id);
        final PowerManager.WakeLock wlFolder = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id + ".folder");
        final PowerManager.WakeLock wlMessage = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id + ".message");
        try {
            wlAccount.acquire();

            final DB db = DB.getInstance(this);

            int backoff = CONNECT_BACKOFF_START;
            while (state.isRunning()) {
                state.reset();
                Log.i(account.name + " run");

                Handler handler = new Handler(getMainLooper());
                final List<TwoStateOwner> cowners = new ArrayList<>();

                // Debug
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.DEBUG);

                final MailService iservice = new MailService(
                        this, account.getProtocol(), account.realm, account.insecure, debug);
                iservice.setPartialFetch(account.partial_fetch);
                if (account.pop)
                    iservice.setLeaveOnServer(account.browse);

                final Map<EntityFolder, IMAPFolder> mapFolders = new HashMap<>();
                List<Thread> idlers = new ArrayList<>();
                try {
                    // Initiate connection
                    EntityLog.log(this, account.name + " connecting");
                    db.folder().setFolderStates(account.id, null);
                    db.account().setAccountState(account.id, "connecting");

                    try {
                        iservice.connect(account);
                    } catch (Throwable ex) {
                        if (ex instanceof AuthenticationFailedException) {
                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nm.notify("receive:" + account.id, 1,
                                    Core.getNotificationError(this, "error", account.name, ex)
                                            .build());
                            throw ex;
                        }

                        // Report account connection error
                        if (account.last_connected != null && !ConnectionHelper.airplaneMode(this)) {
                            EntityLog.log(this, account.name + " last connected: " + new Date(account.last_connected));

                            long now = new Date().getTime();
                            long delayed = now - account.last_connected - account.poll_interval * 60 * 1000L;
                            if (delayed > ACCOUNT_ERROR_AFTER * 60 * 1000L && backoff > BACKOFF_ERROR_AFTER) {
                                Log.i("Reporting sync error after=" + delayed);
                                Throwable warning = new Throwable(
                                        getString(R.string.title_no_sync,
                                                Helper.getDateTimeInstance(this, DateFormat.SHORT, DateFormat.SHORT)
                                                        .format(account.last_connected)), ex);
                                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                nm.notify("receive:" + account.id, 1,
                                        Core.getNotificationError(this, "warning", account.name, warning)
                                                .build());
                            }
                        }

                        throw ex;
                    }

                    final boolean capIdle = iservice.hasCapability("IDLE");
                    Log.i(account.name + " idle=" + capIdle);

                    db.account().setAccountState(account.id, "connected");
                    db.account().setAccountError(account.id, null);
                    db.account().setAccountWarning(account.id, null);
                    EntityLog.log(this, account.name + " connected");

                    // Listen for store events
                    iservice.getStore().addStoreListener(new StoreListener() {
                        @Override
                        public void notification(StoreEvent e) {
                            if (e.getMessageType() == StoreEvent.NOTICE)
                                EntityLog.log(ServiceSynchronize.this, account.name + " notice: " + e.getMessage());
                            else
                                try {
                                    wlFolder.acquire();

                                    String message = e.getMessage();
                                    Log.w(account.name + " alert: " + message);
                                    EntityLog.log(
                                            ServiceSynchronize.this, account.name + " " +
                                                    Helper.formatThrowable(new Core.AlertException(message), false));
                                    db.account().setAccountError(account.id, message);

                                    if (message != null && !message.startsWith("Too many simultaneous connections")) {
                                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        nm.notify("alert:" + account.id, 1,
                                                Core.getNotificationError(
                                                        ServiceSynchronize.this, "warning", account.name,
                                                        new Core.AlertException(message))
                                                        .build());
                                    }

                                    state.error(null);
                                } finally {
                                    wlFolder.release();
                                }
                        }
                    });

                    // Listen for folder events
                    iservice.getStore().addFolderListener(new FolderAdapter() {
                        @Override
                        public void folderCreated(FolderEvent e) {
                            try {
                                wlFolder.acquire();

                                String name = e.getFolder().getFullName();
                                Log.i("Folder created=" + name);
                                if (db.folder().getFolderByName(account.id, name) == null)
                                    reload(ServiceSynchronize.this, "folder created");
                            } finally {
                                wlFolder.release();
                            }
                        }

                        @Override
                        public void folderRenamed(FolderEvent e) {
                            try {
                                wlFolder.acquire();

                                String old = e.getFolder().getFullName();
                                String name = e.getNewFolder().getFullName();
                                Log.i("Folder renamed from=" + old + " to=" + name);

                                int count = db.folder().renameFolder(account.id, old, name);
                                Log.i("Renamed to " + name + " count=" + count);
                                if (count == 0)
                                    reload(ServiceSynchronize.this, "folder renamed");
                            } finally {
                                wlFolder.release();
                            }
                        }

                        @Override
                        public void folderDeleted(FolderEvent e) {
                            try {
                                wlFolder.acquire();

                                String name = e.getFolder().getFullName();
                                Log.i("Folder deleted=" + name);
                                if (db.folder().getFolderByName(account.id, name) != null)
                                    reload(ServiceSynchronize.this, "folder deleted");
                            } finally {
                                wlFolder.release();
                            }
                        }
                    });

                    // Update folder list
                    if (!account.pop)
                        Core.onSynchronizeFolders(this, account, iservice.getStore(), state);

                    // Open synchronizing folders
                    final ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

                    List<EntityFolder> folders = db.folder().getFolders(account.id, false, true);
                    Collections.sort(folders, new Comparator<EntityFolder>() {
                        @Override
                        public int compare(EntityFolder f1, EntityFolder f2) {
                            int s1 = EntityFolder.FOLDER_SORT_ORDER.indexOf(f1.type);
                            int s2 = EntityFolder.FOLDER_SORT_ORDER.indexOf(f2.type);
                            int s = Integer.compare(s1, s2);
                            if (s != 0)
                                return s;

                            return f1.name.compareTo(f2.name);
                        }
                    });

                    for (final EntityFolder folder : folders) {
                        if (folder.synchronize && !folder.poll && capIdle && sync) {
                            Log.i(account.name + " sync folder " + folder.name);

                            db.folder().setFolderState(folder.id, "connecting");

                            final IMAPFolder ifolder = (IMAPFolder) iservice.getStore().getFolder(folder.name);
                            try {
                                if (BuildConfig.DEBUG && "Postausgang".equals(folder.name))
                                    throw new ReadOnlyFolderException(ifolder);
                                ifolder.open(Folder.READ_WRITE);
                                db.folder().setFolderReadOnly(folder.id, ifolder.getUIDNotSticky());
                            } catch (ReadOnlyFolderException ex) {
                                Log.w(folder.name + " read only");
                                try {
                                    ifolder.open(Folder.READ_ONLY);
                                    db.folder().setFolderReadOnly(folder.id, true);
                                } catch (MessagingException ex1) {
                                    Log.w(folder.name, ex1);
                                    db.folder().setFolderState(folder.id, null);
                                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex1));
                                    continue;
                                }
                            } catch (FolderNotFoundException ex) {
                                Log.w(folder.name, ex);
                                db.folder().deleteFolder(folder.id);
                                continue;
                            } catch (MessagingException ex) {
                                Log.w(folder.name, ex);
                                db.folder().setFolderState(folder.id, null);
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                continue;
                            } catch (Throwable ex) {
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                throw ex;
                            }
                            mapFolders.put(folder, ifolder);

                            db.folder().setFolderState(folder.id, "connected");
                            db.folder().setFolderError(folder.id, null);

                            int count = ifolder.getMessageCount();
                            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);

                            Log.i(account.name + " folder " + folder.name + " flags=" + ifolder.getPermanentFlags());

                            // Listen for new and deleted messages
                            ifolder.addMessageCountListener(new MessageCountAdapter() {
                                @Override
                                public void messagesAdded(MessageCountEvent e) {
                                    try {
                                        wlMessage.acquire();
                                        Log.i(folder.name + " messages added");

                                        for (Message imessage : e.getMessages()) {
                                            long uid = ifolder.getUID(imessage);
                                            EntityOperation.queue(ServiceSynchronize.this, folder, EntityOperation.FETCH, uid);
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(
                                                ServiceSynchronize.this,
                                                folder.name + " " + Helper.formatThrowable(ex, false));
                                        state.error(ex);
                                    } finally {
                                        wlMessage.release();
                                    }
                                }

                                @Override
                                public void messagesRemoved(MessageCountEvent e) {
                                    try {
                                        wlMessage.acquire();
                                        Log.i(folder.name + " messages removed");

                                        for (Message imessage : e.getMessages()) {
                                            long uid = ifolder.getUID(imessage);
                                            EntityOperation.queue(ServiceSynchronize.this, folder, EntityOperation.FETCH, uid);
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(
                                                ServiceSynchronize.this,
                                                folder.name + " " + Helper.formatThrowable(ex, false));
                                        state.error(ex);
                                    } finally {
                                        wlMessage.release();
                                    }
                                }
                            });

                            // Flags (like "seen") at the remote could be changed while synchronizing

                            // Listen for changed messages
                            ifolder.addMessageChangedListener(new MessageChangedListener() {
                                @Override
                                public void messageChanged(MessageChangedEvent e) {
                                    try {
                                        wlMessage.acquire();
                                        Log.i(folder.name + " message changed");

                                        long uid = ifolder.getUID(e.getMessage());
                                        EntityOperation.queue(ServiceSynchronize.this, folder, EntityOperation.FETCH, uid);
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(
                                                ServiceSynchronize.this,
                                                folder.name + " " + Helper.formatThrowable(ex, false));
                                        state.error(ex);
                                    } finally {
                                        wlMessage.release();
                                    }
                                }
                            });

                            // Idle folder
                            Thread idler = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Log.i(folder.name + " start idle");
                                        while (state.isRunning() && state.isRecoverable()) {
                                            Log.i(folder.name + " do idle");
                                            ifolder.idle(false);
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(
                                                ServiceSynchronize.this,
                                                folder.name + " " + Helper.formatThrowable(ex, false));
                                        state.error(new FolderClosedException(ifolder, "IDLE"));
                                    } finally {
                                        Log.i(folder.name + " end idle");
                                    }
                                }
                            }, "idler." + folder.id);
                            idler.setPriority(THREAD_PRIORITY_BACKGROUND);
                            idler.start();
                            idlers.add(idler);

                            if (sync && folder.selectable)
                                EntityOperation.sync(this, folder.id, false);
                        } else
                            mapFolders.put(folder, null);

                        Log.i(folder.name + " observing");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TwoStateOwner cowner = new TwoStateOwner(ServiceSynchronize.this, folder.name);
                                cowners.add(cowner);
                                cowner.start();

                                db.operation().liveOperations(folder.id).observe(cowner, new Observer<List<EntityOperation>>() {
                                    private List<Long> handling = new ArrayList<>();
                                    private final PowerManager.WakeLock wlFolder = pm.newWakeLock(
                                            PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":folder." + folder.id);

                                    @Override
                                    public void onChanged(final List<EntityOperation> operations) {
                                        boolean process = false;
                                        List<Long> ops = new ArrayList<>();
                                        for (EntityOperation op : operations) {
                                            if (!handling.contains(op.id))
                                                process = true;
                                            ops.add(op.id);
                                        }
                                        handling = ops;

                                        if (handling.size() > 0 && process) {
                                            Log.i(folder.name + " operations=" + operations.size() +
                                                    " init=" + folder.initialize + " poll=" + folder.poll);

                                            executor.submit(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        wlFolder.acquire();
                                                        Log.i(folder.name + " process");

                                                        // Get folder
                                                        Folder ifolder = mapFolders.get(folder); // null when polling
                                                        boolean canOpen = (!account.pop || EntityFolder.INBOX.equals(folder.type));
                                                        final boolean shouldClose = (ifolder == null && canOpen);

                                                        try {
                                                            Log.i(folder.name + " run " + (shouldClose ? "offline" : "online"));

                                                            if (shouldClose) {
                                                                // Prevent unnecessary folder connections
                                                                if (db.operation().getOperationCount(folder.id, null) == 0)
                                                                    return;

                                                                db.folder().setFolderState(folder.id, "connecting");

                                                                ifolder = iservice.getStore().getFolder(folder.name);
                                                                ifolder.open(Folder.READ_WRITE);

                                                                db.folder().setFolderState(folder.id, "connected");

                                                                db.folder().setFolderError(folder.id, null);
                                                            }

                                                            Core.processOperations(ServiceSynchronize.this,
                                                                    account, folder,
                                                                    iservice.getStore(), ifolder,
                                                                    state);

                                                        } catch (FolderNotFoundException ex) {
                                                            Log.w(folder.name, ex);
                                                            db.folder().deleteFolder(folder.id);
                                                        } catch (Throwable ex) {
                                                            Log.e(folder.name, ex);
                                                            EntityLog.log(
                                                                    ServiceSynchronize.this,
                                                                    folder.name + " " + Helper.formatThrowable(ex, false));
                                                            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                                                            state.error(ex);
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
                                });
                            }
                        });
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

                    String id = BuildConfig.APPLICATION_ID + ".POLL." + account.id + "." + new Random().nextInt();
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(id), 0);
                    registerReceiver(alarm, new IntentFilter(id));

                    // Keep alive
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    try {
                        while (state.isRunning()) {
                            if (!state.isRecoverable())
                                throw new StoreClosedException(iservice.getStore(), "Unrecoverable");

                            // Sends store NOOP
                            if (!iservice.getStore().isConnected())
                                throw new StoreClosedException(iservice.getStore(), "NOOP");

                            if (sync)
                                for (EntityFolder folder : mapFolders.keySet())
                                    if (folder.synchronize)
                                        if (!folder.poll && capIdle) {
                                            // Sends folder NOOP
                                            if (!mapFolders.get(folder).isOpen())
                                                throw new StoreClosedException(iservice.getStore(), folder.name);
                                        } else
                                            EntityOperation.sync(this, folder.id, false);

                            // Successfully connected: reset back off time
                            backoff = CONNECT_BACKOFF_START;

                            // Record successful connection
                            account.last_connected = new Date().getTime();
                            EntityLog.log(this, account.name + " set last_connected=" + new Date(account.last_connected));
                            db.account().setAccountConnected(account.id, account.last_connected);
                            db.account().setAccountWarning(account.id, capIdle ? null : getString(R.string.title_no_idle));

                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nm.cancel("receive:" + account.id, 1);

                            // Schedule keep alive alarm
                            EntityLog.log(this, account.name + " wait=" + account.poll_interval);
                            AlarmManagerCompat.setAndAllowWhileIdle(am,
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
                        try {
                            unregisterReceiver(alarm);
                        } catch (IllegalArgumentException ex) {
                            Log.e(ex);
                        }
                    }

                    Log.i(account.name + " done state=" + state);
                } catch (StoreClosedException ex) {
                    Log.w(account.name, ex);
                } catch (Throwable ex) {
                    Log.e(account.name, ex);
                    EntityLog.log(
                            ServiceSynchronize.this,
                            account.name + " " + Helper.formatThrowable(ex, false));
                    db.account().setAccountError(account.id, Helper.formatThrowable(ex));
                } finally {
                    // Stop watching for operations
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (TwoStateOwner owner : cowners)
                                owner.destroy();
                        }
                    });

                    // Update state
                    EntityLog.log(this, account.name + " closing");
                    db.account().setAccountState(account.id, "closing");
                    for (EntityFolder folder : mapFolders.keySet())
                        if (folder.synchronize && !folder.poll && mapFolders.get(folder) != null)
                            db.folder().setFolderState(folder.id, "closing");

                    // Close store
                    try {
                        EntityLog.log(this, account.name + " store closing");
                        iservice.close();
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

                    // Update state
                    for (EntityFolder folder : mapFolders.keySet())
                        if (folder.synchronize && !folder.poll && mapFolders.get(folder) != null)
                            db.folder().setFolderState(folder.id, null);
                }

                if (state.isRunning())
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

                            String id = BuildConfig.APPLICATION_ID + ".BACKOFF." + account.id + "." + new Random().nextInt();
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
                                try {
                                    unregisterReceiver(alarm);
                                } catch (IllegalArgumentException ex) {
                                    Log.e(ex);
                                }
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

    private ConnectivityManager.NetworkCallback onNetworkCallback = new ConnectivityManager.NetworkCallback() {
        private Boolean lastSuitable = null;

        @Override
        public void onAvailable(Network network) {
            updateState();

            synchronized (ServiceSynchronize.this) {
                try {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    EntityLog.log(ServiceSynchronize.this, "Available " + network +
                            " capabilities " + cm.getNetworkCapabilities(network) +
                            " connected=" + networkState.isConnected() +
                            " suitable=" + networkState.isSuitable() +
                            " unmetered=" + networkState.isUnmetered() +
                            " roaming=" + networkState.isRoaming() +
                            " started=" + started);

                    if (networkState.isSuitable())
                        if (started) {
                            EntityLog.log(ServiceSynchronize.this, "Checking account states");

                            Thread check = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        DB db = DB.getInstance(ServiceSynchronize.this);

                                        boolean disconnected = false;
                                        List<EntityAccount> accounts = db.account().getSynchronizingAccounts();
                                        for (EntityAccount account : accounts)
                                            if (!"connected".equals(account.state)) {
                                                disconnected = true;
                                                break;
                                            }

                                        if (disconnected)
                                            new Handler(getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        synchronized (ServiceSynchronize.this) {
                                                            queue_reload(true, false, "Some accounts disconnected");
                                                        }
                                                    } catch (Throwable ex) {
                                                        Log.e(ex);
                                                    }
                                                }
                                            });
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                                }
                            }, "synchronize:connectivity");
                            check.setPriority(THREAD_PRIORITY_BACKGROUND);
                            check.start();
                        } else
                            queue_reload(true, false, "connect " + network);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
            updateState();

            synchronized (ServiceSynchronize.this) {
                try {
                    if (!started) {
                        EntityLog.log(ServiceSynchronize.this, "Network " + network + " capabilities " + capabilities);
                        if (networkState.isSuitable())
                            queue_reload(true, false, "capabilities " + network);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }

        @Override
        public void onLost(Network network) {
            updateState();

            synchronized (ServiceSynchronize.this) {
                try {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    EntityLog.log(ServiceSynchronize.this, "Lost " + network +
                            " active=" + cm.getActiveNetworkInfo() +
                            " connected=" + networkState.isConnected() +
                            " suitable=" + networkState.isSuitable() +
                            " unmetered=" + networkState.isUnmetered() +
                            " roaming=" + networkState.isRoaming() +
                            " started=" + started);

                    if (started && !networkState.isSuitable()) {
                        lastLost = new Date().getTime();
                        queue_reload(false, false, "disconnect " + network);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }

        private void updateState() {
            networkState.update(ConnectionHelper.getNetworkState(ServiceSynchronize.this));

            if (lastSuitable == null || lastSuitable != networkState.isSuitable()) {
                lastSuitable = networkState.isSuitable();
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(Helper.NOTIFICATION_SYNCHRONIZE, getNotificationService(lastStats).build());
            }
        }
    };

    private BroadcastReceiver onScreenOff = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Received " + intent);
            Log.logExtras(intent);
            Helper.clearAuthentication(ServiceSynchronize.this);
        }
    };

    static void boot(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DB db = DB.getInstance(context);

                    // Restore notifications
                    db.message().clearNotifyingMessages();

                    // Restore snooze timers
                    for (EntityMessage message : db.message().getSnoozed())
                        EntityMessage.snooze(context, message.id, message.ui_snoozed);

                    // Restore schedule
                    schedule(context);

                    // Conditionally init service
                    int accounts = db.account().getSynchronizingAccounts().size();
                    if (accounts > 0)
                        watchdog(context);
                    else {
                        for (EntityAccount account : db.account().getAccounts())
                            db.account().setAccountState(account.id, null);

                        for (EntityFolder folder : db.folder().getFolders()) {
                            db.folder().setFolderState(folder.id, null);
                            db.folder().setFolderSyncState(folder.id, null);
                        }
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }, "synchronize:boot");
        thread.setPriority(THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    private static void schedule(Context context) {
        Intent alarm = new Intent(context, ServiceSynchronize.class);
        alarm.setAction("alarm");
        PendingIntent piAlarm;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            piAlarm = PendingIntent.getService(context, PI_ALARM, alarm, PendingIntent.FLAG_UPDATE_CURRENT);
        else
            piAlarm = PendingIntent.getForegroundService(context, PI_ALARM, alarm, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(piAlarm);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("schedule", false) || !ActivityBilling.isPro(context))
            return;

        int minuteStart = prefs.getInt("schedule_start", 0);
        int minuteEnd = prefs.getInt("schedule_end", 0);

        if (minuteEnd <= minuteStart)
            minuteEnd += 24 * 60;

        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.HOUR_OF_DAY, minuteStart / 60);
        calStart.set(Calendar.MINUTE, minuteStart % 60);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.HOUR_OF_DAY, minuteEnd / 60);
        calEnd.set(Calendar.MINUTE, minuteEnd % 60);
        calEnd.set(Calendar.SECOND, 0);
        calEnd.set(Calendar.MILLISECOND, 0);

        long now = new Date().getTime();
        if (now > calEnd.getTimeInMillis()) {
            calStart.set(Calendar.DAY_OF_MONTH, calStart.get(Calendar.DAY_OF_MONTH) + 1);
            calEnd.set(Calendar.DAY_OF_MONTH, calEnd.get(Calendar.DAY_OF_MONTH) + 1);
        }

        long start = calStart.getTimeInMillis();
        long end = calEnd.getTimeInMillis();
        long next = (now < start ? start : end);

        Log.i("Schedule now=" + new Date(now));
        Log.i("Schedule start=" + new Date(start));
        Log.i("Schedule end=" + new Date(end));
        Log.i("Schedule next=" + new Date(next));

        boolean enabled = (now >= start && now < end);
        prefs.edit().putBoolean("enabled", enabled).apply();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, piAlarm);
        else
            am.set(AlarmManager.RTC_WAKEUP, next, piAlarm);

        WorkerPoll.init(context);
    }

    static void reschedule(Context context) {
        ContextCompat.startForegroundService(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("alarm"));
    }

    static void reload(Context context, String reason) {
        reload(context, false, reason);
    }

    static void reload(Context context, boolean clear, String reason) {
        ContextCompat.startForegroundService(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("reload")
                        .putExtra("clear", clear)
                        .putExtra("reason", reason));
    }

    static void reset(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = prefs.getInt("poll_interval", 0);
        if (!enabled || pollInterval > 0) {
            ServiceSynchronize.sync = true;
            ServiceSynchronize.oneshot = true;
        }
        ContextCompat.startForegroundService(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("reset"));
    }

    static void process(Context context, boolean sync) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = prefs.getInt("poll_interval", 0);
        if (!enabled || pollInterval > 0) {
            ServiceSynchronize.sync = sync;
            ServiceSynchronize.oneshot = true;
            ContextCompat.startForegroundService(context,
                    new Intent(context, ServiceSynchronize.class)
                            .setAction("oneshot_start"));
        }
    }

    static void watchdog(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = prefs.getInt("poll_interval", 0);
        if (enabled && pollInterval == 0)
            ContextCompat.startForegroundService(context,
                    new Intent(context, ServiceSynchronize.class)
                            .setAction("watchdog"));
    }
}
