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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
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
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Quota;
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

public class ServiceSynchronize extends ServiceBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Network lastActive = null;
    private Boolean lastSuitable = null;
    private long lastLost = 0;
    private int lastAccounts = 0;
    private int lastOperations = 0;
    private ConnectionHelper.NetworkState lastNetworkState = null;

    private boolean foreground = false;
    private Map<Long, Core.State> coreStates = new Hashtable<>();
    private MutableLiveData<ConnectionHelper.NetworkState> liveNetworkState = new MutableLiveData<>();
    private MutableLiveData<List<TupleAccountState>> liveAccountState = new MutableLiveData<>();
    private MediatorState liveAccountNetworkState = new MediatorState();

    private static final long QUIT_DELAY = 5 * 1000L; // milliseconds
    private static final long STILL_THERE_THRESHOLD = 3 * 60 * 1000L; // milliseconds
    static final int DEFAULT_POLL_INTERVAL = 0; // minutes
    private static final int OPTIMIZE_KEEP_ALIVE_INTERVAL = 12; // minutes
    private static final int OPTIMIZE_POLL_INTERVAL = 15; // minutes
    private static final int CONNECT_BACKOFF_START = 4; // seconds
    private static final int CONNECT_BACKOFF_MAX = 32; // seconds (totally 4+8+16+32=1 minute)
    private static final int CONNECT_BACKOFF_ALARM_START = 15; // minutes
    private static final int CONNECT_BACKOFF_ALARM_MAX = 60; // minutes
    private static final long CONNECT_BACKOFF_GRACE = 2 * 60 * 1000L; // milliseconds
    private static final long LOST_RECENTLY = 150 * 1000L; // milliseconds
    private static final int ACCOUNT_ERROR_AFTER = 60; // minutes
    private static final int ACCOUNT_ERROR_AFTER_POLL = 4; // times
    private static final int BACKOFF_ERROR_AFTER = 16; // seconds
    private static final int FAST_FAIL_THRESHOLD = 75; // percent
    private static final long AUTOFIX_TOO_MANY_FOLDERS = 3600 * 1000L; // milliseconds

    private static final String ACTION_NEW_MESSAGE_COUNT = BuildConfig.APPLICATION_ID + ".NEW_MESSAGE_COUNT";

    private static final List<String> PREF_EVAL = Collections.unmodifiableList(Arrays.asList(
            "enabled", "poll_interval" // restart account(s)
    ));

    private static final List<String> PREF_RELOAD = Collections.unmodifiableList(Arrays.asList(
            "sync_nodate",
            "sync_unseen",
            "sync_flagged",
            "delete_unseen",
            "sync_kept",
            "sync_folders",
            "sync_shared_folders",
            "prefer_ip4", "ssl_harden", // force reconnect
            "badge", "unseen_ignored", // force update badge/widget
            "protocol", "debug", // force reconnect
            "auth_plain",
            "auth_login",
            "auth_sasl"
    ));

    static final int PI_ALARM = 1;
    static final int PI_BACKOFF = 2;
    static final int PI_KEEPALIVE = 3;

    @Override
    public void onCreate() {
        EntityLog.log(this, "Service create version=" + BuildConfig.VERSION_NAME);
        super.onCreate();

        if (isBackgroundService(this))
            stopForeground(true);
        else
            startForeground(Helper.NOTIFICATION_SYNCHRONIZE, getNotificationService(null, null).build());

        // Listen for network changes
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        // Removed because of Android VPN service
        // builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        IntentFilter iif = new IntentFilter();
        iif.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        iif.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(connectionChangedReceiver, iif);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            registerReceiver(idleModeChangedReceiver, new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED));

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        DB db = DB.getInstance(this);

        db.account().liveAccountState().observe(this, new Observer<List<TupleAccountState>>() {
            @Override
            public void onChanged(List<TupleAccountState> accountStates) {
                liveAccountState.postValue(accountStates);
            }
        });

        liveAccountNetworkState.addSource(liveNetworkState, new Observer<ConnectionHelper.NetworkState>() {
            @Override
            public void onChanged(ConnectionHelper.NetworkState networkState) {
                liveAccountNetworkState.post(networkState);
            }
        });

        liveAccountNetworkState.addSource(liveAccountState, new Observer<List<TupleAccountState>>() {
            @Override
            public void onChanged(List<TupleAccountState> accountStates) {
                liveAccountNetworkState.post(accountStates);
            }
        });

        liveAccountNetworkState.observeForever(new Observer<List<TupleAccountNetworkState>>() {
            private boolean fts = false;
            private int lastEventId = 0;
            private int lastQuitId = -1;
            private List<TupleAccountNetworkState> accountStates = new ArrayList<>();
            private ExecutorService queue = Helper.getBackgroundExecutor(1, "service");

            @Override
            public void onChanged(List<TupleAccountNetworkState> accountNetworkStates) {
                if (accountNetworkStates == null) {
                    // Destroy
                    for (TupleAccountNetworkState prev : accountStates)
                        stop(prev);

                    quit(null);

                    accountStates.clear();
                    coreStates.clear();
                    liveAccountNetworkState.removeObserver(this);
                } else {
                    int accounts = 0;
                    int operations = 0;
                    boolean event = false;
                    boolean runService = false;
                    for (TupleAccountNetworkState current : accountNetworkStates) {
                        Log.d("### evaluating " + current);
                        if (current.accountState.shouldRun(current.enabled))
                            runService = true;
                        if (!isTransient(current.accountState) &&
                                ("connected".equals(current.accountState.state) || current.accountState.backoff_until != null))
                            accounts++;
                        if (current.accountState.synchronize)
                            operations += current.accountState.operations;

                        long account = current.command.getLong("account", -1);
                        if (account > 0 && !current.accountState.id.equals(account))
                            continue;

                        int index = accountStates.indexOf(current);
                        if (index < 0) {
                            if (current.canRun()) {
                                EntityLog.log(ServiceSynchronize.this, "### new " + current +
                                        " start=" + current.canRun() +
                                        " sync=" + current.accountState.isEnabled(current.enabled) +
                                        " enabled=" + current.accountState.synchronize +
                                        " ondemand=" + current.accountState.ondemand +
                                        " folders=" + current.accountState.folders +
                                        " ops=" + current.accountState.operations +
                                        " tbd=" + current.accountState.tbd +
                                        " state=" + current.accountState.state +
                                        " active=" + current.networkState.getActive());
                                event = true;
                                start(current, current.accountState.isEnabled(current.enabled), false);
                            }
                        } else {
                            TupleAccountNetworkState prev = accountStates.get(index);
                            Core.State state = coreStates.get(current.accountState.id);
                            if (state != null)
                                state.setNetworkState(current.networkState);

                            boolean reload = false;
                            boolean sync = current.command.getBoolean("sync", false);
                            boolean force = current.command.getBoolean("force", false);
                            switch (current.command.getString("name")) {
                                case "reload":
                                    reload = true;
                                    break;
                            }

                            accountStates.remove(index);

                            // Some networks disallow email server connections:
                            // - reload on network type change when disconnected
                            if (reload ||
                                    prev.canRun() != current.canRun() ||
                                    !prev.accountState.equals(current.accountState)) {
                                if (prev.canRun() || current.canRun())
                                    EntityLog.log(ServiceSynchronize.this, "### changed " + current +
                                            " reload=" + reload +
                                            " force=" + force +
                                            " stop=" + prev.canRun() +
                                            " start=" + current.canRun() +
                                            " sync=" + sync +
                                            " enabled=" + current.accountState.isEnabled(current.enabled) +
                                            " should=" + current.accountState.shouldRun(current.enabled) +
                                            " changed=" + !prev.accountState.equals(current.accountState) +
                                            " synchronize=" + current.accountState.synchronize +
                                            " ondemand=" + current.accountState.ondemand +
                                            " folders=" + current.accountState.folders +
                                            " ops=" + current.accountState.operations +
                                            " tbd=" + current.accountState.tbd +
                                            " state=" + current.accountState.state +
                                            " active=" + prev.networkState.getActive() + "/" + current.networkState.getActive());
                                if (prev.canRun()) {
                                    event = true;
                                    stop(prev);
                                }
                                if (current.canRun()) {
                                    event = true;
                                    start(current, current.accountState.isEnabled(current.enabled) || sync, force);
                                }
                            } else {
                                if (state != null) {
                                    Network p = prev.networkState.getActive();
                                    if (p != null && !p.equals(current.networkState.getActive())) {
                                        EntityLog.log(ServiceSynchronize.this, "### changed " + current +
                                                " active=" + prev.networkState.getActive() + "/" + current.networkState.getActive());
                                        state.error(new OperationCanceledException("Active network changed"));
                                    }
                                }
                            }
                        }

                        if (current.accountState.tbd == null)
                            accountStates.add(current);
                        else {
                            event = true;
                            delete(current);
                        }
                    }

                    if (event) {
                        lastEventId++;
                        EntityLog.log(ServiceSynchronize.this, "### eventId=" + lastEventId);
                    }

                    if (lastAccounts != accounts || lastOperations != operations) {
                        lastAccounts = accounts;
                        lastOperations = operations;
                        if (operations == 0) {
                            fts = true;
                            WorkerFts.init(ServiceSynchronize.this, false);
                        } else if (fts) {
                            fts = false;
                            WorkerFts.cancel(ServiceSynchronize.this);
                        }

                        if (!isBackgroundService(ServiceSynchronize.this))
                            try {
                                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                nm.notify(Helper.NOTIFICATION_SYNCHRONIZE, getNotificationService(lastAccounts, lastOperations).build());
                            } catch (Throwable ex) {
/*
                            java.lang.NullPointerException: Attempt to invoke interface method 'java.util.Iterator java.lang.Iterable.iterator()' on a null object reference
                                    at android.app.ApplicationPackageManager.getUserIfProfile(ApplicationPackageManager.java:2167)
                                    at android.app.ApplicationPackageManager.getUserBadgeForDensity(ApplicationPackageManager.java:1002)
                                    at android.app.Notification$Builder.getProfileBadgeDrawable(Notification.java:2890)
                                    at android.app.Notification$Builder.hasThreeLines(Notification.java:3105)
                                    at android.app.Notification$Builder.build(Notification.java:3659)
                                    at androidx.core.app.NotificationCompatBuilder.buildInternal(SourceFile:355)
                                    at androidx.core.app.NotificationCompatBuilder.build(SourceFile:247)
                                    at androidx.core.app.NotificationCompat$Builder.build(SourceFile:1677)
*/
                                Log.w(ex);
                            }
                    }

                    if (!runService && lastQuitId != lastEventId) {
                        lastQuitId = lastEventId;
                        EntityLog.log(ServiceSynchronize.this, "### quitting startId=" + lastEventId);
                        quit(lastEventId);
                    }
                }
            }

            private void start(final TupleAccountNetworkState accountNetworkState, boolean sync, boolean force) {
                EntityLog.log(ServiceSynchronize.this,
                        "Service start=" + accountNetworkState + " sync=" + sync + " force=" + force);

                final Core.State astate = new Core.State(accountNetworkState.networkState);
                astate.runnable(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            monitorAccount(accountNetworkState.accountState, astate, sync, force);
                        } catch (Throwable ex) {
                            Log.e(accountNetworkState.accountState.name, ex);
                        }
                    }
                }, "sync.account." + accountNetworkState.accountState.id);
                coreStates.put(accountNetworkState.accountState.id, astate);

                queue.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Map<String, String> crumb = new HashMap<>();
                            crumb.put("account", accountNetworkState.accountState.id.toString());
                            crumb.put("connected", Boolean.toString(accountNetworkState.networkState.isConnected()));
                            crumb.put("suitable", Boolean.toString(accountNetworkState.networkState.isSuitable()));
                            crumb.put("unmetered", Boolean.toString(accountNetworkState.networkState.isUnmetered()));
                            crumb.put("roaming", Boolean.toString(accountNetworkState.networkState.isRoaming()));
                            crumb.put("lastLost", new Date(lastLost).toString());
                            Log.breadcrumb("start", crumb);

                            Log.i("### start=" + accountNetworkState + " sync=" + sync);
                            astate.start();
                            EntityLog.log(ServiceSynchronize.this, "### started=" + accountNetworkState);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
            }

            private void stop(final TupleAccountNetworkState accountNetworkState) {
                final Core.State state = coreStates.get(accountNetworkState.accountState.id);
                if (state == null)
                    return;
                coreStates.remove(accountNetworkState.accountState.id);

                EntityLog.log(ServiceSynchronize.this, "Service stop=" + accountNetworkState);

                queue.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Map<String, String> crumb = new HashMap<>();
                            crumb.put("account", accountNetworkState.accountState.id.toString());
                            crumb.put("connected", Boolean.toString(accountNetworkState.networkState.isConnected()));
                            crumb.put("suitable", Boolean.toString(accountNetworkState.networkState.isSuitable()));
                            crumb.put("unmetered", Boolean.toString(accountNetworkState.networkState.isUnmetered()));
                            crumb.put("roaming", Boolean.toString(accountNetworkState.networkState.isRoaming()));
                            crumb.put("lastLost", new Date(lastLost).toString());
                            Log.breadcrumb("stop", crumb);

                            Log.i("### stop=" + accountNetworkState);
                            state.stop();
                            state.join();
                            EntityLog.log(ServiceSynchronize.this, "### stopped=" + accountNetworkState);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
            }

            private void delete(final TupleAccountNetworkState accountNetworkState) {
                EntityLog.log(ServiceSynchronize.this, "Service delete=" + accountNetworkState);

                queue.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DB db = DB.getInstance(ServiceSynchronize.this);
                            db.account().deleteAccount(accountNetworkState.accountState.id);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                nm.deleteNotificationChannel(EntityAccount.getNotificationChannelId(accountNetworkState.accountState.id));
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
            }

            private void quit(final Integer eventId) {
                queue.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EntityLog.log(ServiceSynchronize.this, "### quit eventId=" + eventId);

                            if (eventId == null) {
                                // Service destroy
                                DB db = DB.getInstance(ServiceSynchronize.this);
                                List<EntityOperation> ops = db.operation().getOperations(EntityOperation.SYNC);
                                for (EntityOperation op : ops)
                                    db.folder().setFolderSyncState(op.folder, null);
                            } else {
                                // Yield update notifications/widgets
                                try {
                                    Thread.sleep(QUIT_DELAY);
                                } catch (InterruptedException ex) {
                                    Log.w(ex);
                                }

                                if (!eventId.equals(lastEventId)) {
                                    EntityLog.log(ServiceSynchronize.this, "### quit cancelled eventId=" + eventId + "/" + lastEventId);
                                    return;
                                }

                                // Stop service
                                stopSelf();
                                EntityLog.log(ServiceSynchronize.this, "### stop self eventId=" + eventId);

                                WorkerCleanup.cleanupConditionally(ServiceSynchronize.this);
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
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
                    Helper.getBackgroundExecutor(1, "notify");

            @Override
            public void onChanged(final List<TupleMessageEx> messages) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Core.notifyMessages(ServiceSynchronize.this, messages, groupNotifying, foreground);
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

        db.message().liveWidgetUnseen(null).observe(cowner, new Observer<List<TupleMessageStats>>() {
            private Integer lastCount = null;
            private List<TupleMessageStats> last = null;

            @Override
            public void onChanged(List<TupleMessageStats> stats) {
                if (stats == null)
                    stats = new ArrayList<>();

                boolean changed = false;
                if (last == null || last.size() != stats.size())
                    changed = true;
                else
                    for (int i = 0; i < stats.size(); i++)
                        if (!last.get(i).equals(stats.get(i))) {
                            changed = true;
                            break;
                        }

                if (!changed)
                    return;

                last = stats;

                Widget.update(ServiceSynchronize.this);

                boolean badge = prefs.getBoolean("badge", true);
                boolean unseen_ignored = prefs.getBoolean("unseen_ignored", false);

                int count = 0;
                for (TupleMessageStats stat : stats) {
                    Integer unseen = (unseen_ignored ? stat.notifying : stat.unseen);
                    if (unseen != null)
                        count += unseen;
                }

                if (lastCount == null || !lastCount.equals(count)) {
                    lastCount = count;
                    // Broadcast new message count
                    try {
                        Intent intent = new Intent(ACTION_NEW_MESSAGE_COUNT);
                        intent.putExtra("count", count);
                        sendBroadcast(intent);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                    // Update badge
                    try {
                        if (count == 0 || !badge)
                            ShortcutBadger.removeCount(ServiceSynchronize.this);
                        else
                            ShortcutBadger.applyCount(ServiceSynchronize.this, count);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            }
        });

        db.message().liveWidgetUnified().observe(cowner, new Observer<List<TupleMessageWidgetCount>>() {
            private List<TupleMessageWidgetCount> last = null;

            @Override
            public void onChanged(List<TupleMessageWidgetCount> current) {
                if (current == null)
                    current = new ArrayList<>();

                boolean changed = false;
                if (last == null || last.size() != current.size())
                    changed = true;
                else
                    for (int i = 0; i < current.size(); i++)
                        if (!current.get(i).equals(last.get(i))) {
                            changed = true;
                            break;
                        }

                if (changed)
                    WidgetUnified.updateData(ServiceSynchronize.this);

                last = current;
            }
        });

        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (PREF_EVAL.contains(key)) {
            Bundle command = new Bundle();
            command.putString("pref", key);
            command.putString("name", "eval");
            liveAccountNetworkState.post(command);
        } else if (PREF_RELOAD.contains(key) || ConnectionHelper.PREF_NETWORK.contains(key)) {
            if (ConnectionHelper.PREF_NETWORK.contains(key))
                updateNetworkState(ConnectionHelper.getActiveNetwork(this), "preference");
            Bundle command = new Bundle();
            command.putString("pref", key);
            command.putString("name", "reload");
            liveAccountNetworkState.post(command);
        }
    }

    @Override
    public void onDestroy() {
        EntityLog.log(this, "Service destroy");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            unregisterReceiver(idleModeChangedReceiver);

        unregisterReceiver(connectionChangedReceiver);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        liveAccountNetworkState.postDestroy();

        TTSHelper.shutdown();

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
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i("Task removed=" + rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent == null ? null : intent.getAction());
        String reason = (intent == null ? null : intent.getStringExtra("reason"));
        EntityLog.log(ServiceSynchronize.this, "### Service command " + intent +
                " action=" + action + " reason=" + reason);
        Log.logExtras(intent);

        super.onStartCommand(intent, flags, startId);

        if (isBackgroundService(this))
            stopForeground(true);
        else
            startForeground(Helper.NOTIFICATION_SYNCHRONIZE, getNotificationService(null, null).build());

        if (action != null)
            try {
                switch (action.split(":")[0]) {
                    case "eval":
                        onEval(intent);
                        break;

                    case "reload":
                        onReload(intent);
                        break;

                    case "backoff":
                    case "keepalive":
                        onWakeup(intent);
                        break;

                    case "state":
                        onState(intent);
                        break;

                    case "alarm":
                        onAlarm(intent);
                        break;

                    case "watchdog":
                        onWatchdog(intent);
                        break;

                    default:
                        Log.w("Unknown action: " + action);
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }


        return START_STICKY;
    }

    private void onEval(Intent intent) {
        Bundle command = new Bundle();
        command.putString("name", "eval");
        command.putLong("account", intent.getLongExtra("account", -1));
        liveAccountNetworkState.post(command);
    }

    private void onReload(Intent intent) {
        boolean force = intent.getBooleanExtra("force", false);
        if (force)
            lastLost = 0;

        Bundle command = new Bundle();
        command.putString("name", "reload");
        command.putLong("account", intent.getLongExtra("account", -1));
        command.putBoolean("force", force);
        liveAccountNetworkState.post(command);
    }

    private void onWakeup(Intent intent) {
        String action = intent.getAction();
        long account = Long.parseLong(action.split(":")[1]);
        Core.State state = coreStates.get(account);

        if (state == null)
            EntityLog.log(this, "### wakeup missing account=" + account);
        else {
            EntityLog.log(this, "### waking up account=" + account);
            if (!state.release())
                EntityLog.log(this, "### waking up failed account=" + account);
        }
    }

    private void onState(Intent intent) {
        foreground = intent.getBooleanExtra("foreground", false);
    }

    private void onAlarm(Intent intent) {
        Bundle command = new Bundle();
        schedule(this, true);
        command.putString("name", "eval");
        command.putBoolean("sync", true);
        liveAccountNetworkState.post(command);
    }

    private void onWatchdog(Intent intent) {
        EntityLog.log(this, "Watchdog");
        schedule(this, false);
        networkCallback.onCapabilitiesChanged(null, null);
    }

    private NotificationCompat.Builder getNotificationService(Integer accounts, Integer operations) {
        if (accounts != null)
            this.lastAccounts = accounts;
        if (operations != null)
            this.lastOperations = operations;

        // Build pending intent
        Intent why = new Intent(this, ActivityView.class);
        why.setAction("why");
        why.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piWhy = PendingIntent.getActivity(this, ActivityView.REQUEST_WHY, why, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "service")
                        .setSmallIcon(R.drawable.baseline_compare_arrows_white_24)
                        .setContentTitle(getResources().getQuantityString(
                                R.plurals.title_notification_synchronizing, lastAccounts, lastAccounts))
                        .setContentIntent(piWhy)
                        .setAutoCancel(false)
                        .setShowWhen(false)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setDefaults(0) // disable sound on pre Android 8
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                        .setLocalOnly(true);

        if (lastOperations > 0)
            builder.setContentText(getResources().getQuantityString(
                    R.plurals.title_notification_operations, lastOperations, lastOperations));

        if (lastSuitable == null || !lastSuitable)
            builder.setSubText(getString(R.string.title_notification_waiting));

        return builder;
    }

    private NotificationCompat.Builder getNotificationAlert(String account, String message) {
        // Build pending intent
        Intent alert = new Intent(this, ActivityView.class);
        alert.setAction("alert");
        alert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piAlert = PendingIntent.getActivity(this, ActivityView.REQUEST_ALERT, alert, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "alerts")
                        .setSmallIcon(R.drawable.baseline_warning_white_24)
                        .setContentTitle(getString(R.string.title_notification_alert, account))
                        .setContentText(message)
                        .setContentIntent(piAlert)
                        .setAutoCancel(false)
                        .setShowWhen(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setOnlyAlertOnce(true)
                        .setCategory(NotificationCompat.CATEGORY_ERROR)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));

        return builder;
    }

    private void monitorAccount(
            final EntityAccount account, final Core.State state,
            final boolean sync, final boolean force) throws NoSuchProviderException {
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

            long thread = Thread.currentThread().getId();
            Long currentThread = thread;
            db.account().setAccountThread(account.id, thread);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (account.notify)
                    account.createNotificationChannel(this);
                else
                    account.deleteNotificationChannel(this);
            }

            int fast_fails = 0;
            long first_fail = 0;
            Throwable last_fail = null;
            state.setBackoff(CONNECT_BACKOFF_START);
            while (state.isRunning() &&
                    currentThread != null && currentThread.equals(thread)) {
                state.reset();
                Log.i(account.name + " run thread=" + currentThread);

                final ObjectHolder<TwoStateOwner> cowner = new ObjectHolder<>();
                final ExecutorService executor =
                        Helper.getBackgroundExecutor(1, "account_" + account.id);

                // Debug
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.DEBUG);

                final EmailService iservice = new EmailService(
                        this, account.getProtocol(), account.realm, account.encryption, account.insecure, debug);
                iservice.setPartialFetch(account.partial_fetch);
                iservice.setIgnoreBodyStructureSize(account.ignore_size);
                if (account.protocol != EntityAccount.TYPE_IMAP)
                    iservice.setLeaveOnServer(account.leave_on_server);

                final long start = new Date().getTime();

                iservice.setListener(new StoreListener() {
                    @Override
                    public void notification(StoreEvent e) {
                        String message = e.getMessage();
                        if (TextUtils.isEmpty(message))
                            message = "?";
                        if (e.getMessageType() == StoreEvent.NOTICE) {
                            EntityLog.log(ServiceSynchronize.this, account.name + " notice: " + message);

                            // Store NOOP
                            //iservice.getStore().isConnected();

                            if ("Still here".equals(message) && !isTransient(account)) {
                                long now = new Date().getTime();
                                if (now - start < STILL_THERE_THRESHOLD)
                                    optimizeAccount(account, message);
                            }
                        } else
                            try {
                                wlFolder.acquire();

                                EntityLog.log(ServiceSynchronize.this, account.name + " alert: " + message);

                                if (!ConnectionHelper.isMaxConnections(message))
                                    try {
                                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        nm.notify("alert:" + account.id, 1,
                                                getNotificationAlert(account.name, message).build());
                                    } catch (Throwable ex) {
                                        Log.w(ex);
                                    }
                            } finally {
                                wlFolder.release();
                            }
                    }
                });

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
                        // Immediately report auth errors
                        if (ex instanceof AuthenticationFailedException) {
                            if (ConnectionHelper.isIoError(ex)) {
                                if (!BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e(ex);
                            } else {
                                Log.e(ex);
                                try {
                                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    nm.notify("receive:" + account.id, 1,
                                            Core.getNotificationError(this, "error", account.name, ex)
                                                    .build());
                                } catch (Throwable ex1) {
                                    Log.w(ex1);
                                }
                                throw ex;
                            }
                        }

                        throw ex;
                    }

                    // https://tools.ietf.org/html/rfc2177
                    final boolean capIdle = iservice.hasCapability("IDLE");
                    Log.i(account.name + " idle=" + capIdle);
                    if (!capIdle || account.poll_interval < OPTIMIZE_KEEP_ALIVE_INTERVAL)
                        optimizeAccount(account, "IDLE");

                    db.account().setAccountState(account.id, "connected");
                    db.account().setAccountError(account.id, null);
                    db.account().setAccountWarning(account.id, null);
                    EntityLog.log(this, account.name + " connected");

                    db.account().setAccountMaxSize(account.id, iservice.getMaxSize());

                    // Listen for folder events
                    iservice.getStore().addFolderListener(new FolderAdapter() {
                        @Override
                        public void folderCreated(FolderEvent e) {
                            try {
                                wlFolder.acquire();

                                String name = e.getFolder().getFullName();
                                Log.i("Folder created=" + name);
                                if (db.folder().getFolderByName(account.id, name) == null)
                                    reload(ServiceSynchronize.this, account.id, false, "folder created");
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
                                    reload(ServiceSynchronize.this, account.id, false, "folder renamed");
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
                                    reload(ServiceSynchronize.this, account.id, false, "folder deleted");
                            } finally {
                                wlFolder.release();
                            }
                        }
                    });

                    // Update folder list
                    if (account.protocol == EntityAccount.TYPE_IMAP)
                        Core.onSynchronizeFolders(this, account, iservice.getStore(), state, force);

                    // Open synchronizing folders
                    List<EntityFolder> folders = db.folder().getFolders(account.id, false, true);
                    if (folders.size() > 0)
                        Collections.sort(folders, folders.get(0).getComparator(this));

                    for (final EntityFolder folder : folders) {
                        if (folder.synchronize && !folder.poll && capIdle && sync) {
                            Log.i(account.name + " sync folder " + folder.name);

                            db.folder().setFolderState(folder.id, "connecting");

                            final IMAPFolder ifolder = (IMAPFolder) iservice.getStore().getFolder(folder.name);
                            mapFolders.put(folder, ifolder);
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
                                } catch (Throwable ex1) {
                                    db.folder().setFolderError(folder.id, Log.formatThrowable(ex1));
                                    throw ex1;
                                }
                            } catch (FolderNotFoundException ex) {
                                Log.w(folder.name, ex);
                                db.folder().setFolderError(folder.id, Log.formatThrowable(ex));
                                db.folder().setFolderSynchronize(folder.id, false);
                                continue;
                            } catch (Throwable ex) {
                                db.folder().setFolderError(folder.id, Log.formatThrowable(ex));
                                if (EntityFolder.INBOX.equals(folder.type))
                                    throw ex;
                                else
                                    continue;
                                /*
                                    javax.mail.MessagingException: D2 NO Mailbox does not exist, or must be subscribed to.;
                                      nested exception is:
                                        com.sun.mail.iap.CommandFailedException: D2 NO Mailbox does not exist, or must be subscribed to.
                                    javax.mail.MessagingException: D2 NO Mailbox does not exist, or must be subscribed to.;
                                      nested exception is:
                                        com.sun.mail.iap.CommandFailedException: D2 NO Mailbox does not exist, or must be subscribed to.
                                        at com.sun.mail.imap.IMAPFolder.open(SourceFile:61)
                                        at com.sun.mail.imap.IMAPFolder.open(SourceFile:1)
                                        at eu.faircode.email.ServiceSynchronize.monitorAccount(SourceFile:63)
                                        at eu.faircode.email.ServiceSynchronize.access$900(SourceFile:1)
                                        at eu.faircode.email.ServiceSynchronize$4$1.run(SourceFile:1)
                                        at java.lang.Thread.run(Thread.java:919)
                                    Caused by: com.sun.mail.iap.CommandFailedException: D2 NO Mailbox does not exist, or must be subscribed to.
                                        at com.sun.mail.iap.Protocol.handleResult(SourceFile:8)
                                        at com.sun.mail.imap.protocol.IMAPProtocol.select(SourceFile:19)
                                        at com.sun.mail.imap.IMAPFolder.open(SourceFile:16)
                                 */
                            }

                            db.folder().setFolderState(folder.id, "connected");
                            db.folder().setFolderError(folder.id, null);

                            if (capIdle != MessageHelper.hasCapability(ifolder, "IDLE"))
                                Log.e("Conflicting IDLE=" + capIdle + " host=" + account.host);

                            int count = MessageHelper.getMessageCount(ifolder);
                            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);

                            Log.i(account.name + " folder " + folder.name + " flags=" + ifolder.getPermanentFlags());

                            // Listen for new and deleted messages
                            ifolder.addMessageCountListener(new MessageCountAdapter() {
                                @Override
                                public void messagesAdded(MessageCountEvent e) {
                                    try {
                                        wlMessage.acquire();
                                        Log.i(folder.name + " messages added");

                                        try {
                                            db.beginTransaction();

                                            for (Message imessage : e.getMessages()) {
                                                long uid = ifolder.getUID(imessage);
                                                EntityOperation.queue(ServiceSynchronize.this, folder, EntityOperation.FETCH, uid);
                                            }

                                            db.setTransactionSuccessful();
                                        } finally {
                                            db.endTransaction();
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(
                                                ServiceSynchronize.this,
                                                folder.name + " " + Log.formatThrowable(ex, false));
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

                                        try {
                                            db.beginTransaction();

                                            for (Message imessage : e.getMessages()) {
                                                long uid = ifolder.getUID(imessage);
                                                EntityOperation.queue(ServiceSynchronize.this, folder, EntityOperation.FETCH, uid, true);
                                            }

                                            db.setTransactionSuccessful();
                                        } finally {
                                            db.endTransaction();
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(
                                                ServiceSynchronize.this,
                                                folder.name + " " + Log.formatThrowable(ex, false));
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
                                                folder.name + " " + Log.formatThrowable(ex, false));
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
                                        while (ifolder.isOpen() && state.isRunning() && state.isRecoverable()) {
                                            Log.i(folder.name + " do idle");
                                            ifolder.idle(false);
                                            state.activity();
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(
                                                ServiceSynchronize.this,
                                                folder.name + " " + Log.formatThrowable(ex, false));
                                        state.error(new FolderClosedException(ifolder, "IDLE", new Exception(ex)));
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
                        } else {
                            mapFolders.put(folder, null);
                            db.folder().setFolderState(folder.id, null);
                            if (!capIdle && !folder.poll) {
                                folder.poll = true;
                                db.folder().setFolderPoll(folder.id, folder.poll);
                            }
                        }
                    }

                    Log.i(account.name + " observing operations");
                    getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            cowner.value = new TwoStateOwner(ServiceSynchronize.this, account.name);
                            cowner.value.start();

                            db.operation().liveOperations(account.id).observe(cowner.value, new Observer<List<TupleOperationEx>>() {
                                private List<Long> handling = new ArrayList<>();
                                private final Map<TupleOperationEx.PartitionKey, List<TupleOperationEx>> partitions = new HashMap<>();

                                private final PowerManager.WakeLock wlOperations = pm.newWakeLock(
                                        PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":operations." + account.id);

                                @Override
                                public void onChanged(final List<TupleOperationEx> _operations) {
                                    // Get new operations
                                    List<Long> ops = new ArrayList<>();
                                    Map<EntityFolder, List<TupleOperationEx>> added = new HashMap<>();
                                    for (TupleOperationEx op : _operations) {
                                        if (!handling.contains(op.id)) {
                                            boolean found = false;
                                            for (EntityFolder folder : mapFolders.keySet())
                                                if (Objects.equals(folder.id, op.folder)) {
                                                    found = true;
                                                    if (!added.containsKey(folder))
                                                        added.put(folder, new ArrayList<>());
                                                    added.get(folder).add(op);
                                                    break;
                                                }
                                            if (!found)
                                                Log.w(account.name + " folder not found operation=" + op.name);
                                        }
                                        ops.add(op.id);
                                    }
                                    handling = ops;

                                    for (EntityFolder folder : added.keySet()) {
                                        Log.i(folder.name + " queuing operations=" + added.size() +
                                                " init=" + folder.initialize + " poll=" + folder.poll);

                                        // Partition operations by priority
                                        boolean offline = (mapFolders.get(folder) == null);
                                        List<TupleOperationEx.PartitionKey> keys = new ArrayList<>();
                                        synchronized (partitions) {
                                            for (TupleOperationEx op : added.get(folder)) {
                                                TupleOperationEx.PartitionKey key = op.getPartitionKey(offline);

                                                if (!partitions.containsKey(key)) {
                                                    partitions.put(key, new ArrayList<>());
                                                    keys.add(key);
                                                }

                                                partitions.get(key).add(op);
                                            }
                                        }

                                        Collections.sort(keys, new Comparator<TupleOperationEx.PartitionKey>() {
                                            @Override
                                            public int compare(TupleOperationEx.PartitionKey k1, TupleOperationEx.PartitionKey k2) {
                                                Integer p1 = k1.getPriority();
                                                Integer p2 = k2.getPriority();
                                                int priority = p1.compareTo(p2);
                                                if (priority == 0) {
                                                    Long o1 = k1.getOrder();
                                                    Long o2 = k2.getOrder();
                                                    return o1.compareTo(o2);
                                                } else
                                                    return priority;
                                            }
                                        });

                                        for (TupleOperationEx.PartitionKey key : keys) {
                                            synchronized (partitions) {
                                                Log.i(folder.name +
                                                        " queuing partition=" + key +
                                                        " operations=" + partitions.get(key).size());
                                            }

                                            final long sequence = state.getSequence(folder.id, key.getPriority());

                                            executor.submit(new Helper.PriorityRunnable(key.getPriority(), key.getOrder()) {
                                                @Override
                                                public void run() {
                                                    super.run();
                                                    try {
                                                        wlOperations.acquire();

                                                        List<TupleOperationEx> partition;
                                                        synchronized (partitions) {
                                                            partition = partitions.get(key);
                                                            partitions.remove(key);
                                                        }

                                                        Log.i(folder.name +
                                                                " executing partition=" + key +
                                                                " operations=" + partition.size());

                                                        // Get folder
                                                        Folder ifolder = mapFolders.get(folder); // null when polling
                                                        boolean canOpen = (account.protocol == EntityAccount.TYPE_IMAP || EntityFolder.INBOX.equals(folder.type));
                                                        final boolean shouldClose = (ifolder == null && canOpen);

                                                        try {
                                                            Log.i(folder.name + " run " + (shouldClose ? "offline" : "online"));

                                                            if (shouldClose) {
                                                                // Prevent unnecessary folder connections
                                                                if (db.operation().getOperationCount(folder.id, null) == 0)
                                                                    return;

                                                                db.folder().setFolderState(folder.id, "connecting");

                                                                try {
                                                                    ifolder = iservice.getStore().getFolder(folder.name);
                                                                } catch (IllegalStateException ex) {
                                                                    if ("Not connected".equals(ex.getMessage()))
                                                                        return; // Store closed
                                                                    else
                                                                        throw ex;
                                                                }

                                                                try {
                                                                    ifolder.open(Folder.READ_WRITE);
                                                                    if (ifolder instanceof IMAPFolder)
                                                                        db.folder().setFolderReadOnly(folder.id, ((IMAPFolder) ifolder).getUIDNotSticky());
                                                                } catch (ReadOnlyFolderException ex) {
                                                                    Log.w(folder.name + " read only");
                                                                    ifolder.open(Folder.READ_ONLY);
                                                                    db.folder().setFolderReadOnly(folder.id, true);
                                                                }

                                                                db.folder().setFolderState(folder.id, "connected");
                                                                db.folder().setFolderError(folder.id, null);

                                                                int count = MessageHelper.getMessageCount(ifolder);
                                                                db.folder().setFolderTotal(folder.id, count < 0 ? null : count);

                                                                Log.i(account.name + " folder " + folder.name + " flags=" + ifolder.getPermanentFlags());
                                                            }

                                                            Core.processOperations(ServiceSynchronize.this,
                                                                    account, folder,
                                                                    partition,
                                                                    iservice.getStore(), ifolder,
                                                                    state, key.getPriority(), sequence);

                                                        } catch (Throwable ex) {
                                                            Log.e(folder.name, ex);
                                                            EntityLog.log(
                                                                    ServiceSynchronize.this,
                                                                    folder.name + " " + Log.formatThrowable(ex, false));
                                                            db.folder().setFolderError(folder.id, Log.formatThrowable(ex));
                                                            if (!(ex instanceof FolderNotFoundException))
                                                                state.error(new OperationCanceledException("Process"));
                                                        } finally {
                                                            if (shouldClose) {
                                                                if (ifolder != null && ifolder.isOpen()) {
                                                                    db.folder().setFolderState(folder.id, "closing");
                                                                    try {
                                                                        ifolder.close(false);
                                                                    } catch (Throwable ex) {
                                                                        Log.w(folder.name, ex);
                                                                    }
                                                                }
                                                                db.folder().setFolderState(folder.id, null);
                                                            }
                                                        }
                                                    } finally {
                                                        wlOperations.release();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }
                    });

                    // Keep alive
                    boolean first = true;
                    while (state.isRunning()) {
                        long idleTime = state.getIdleTime();
                        boolean tune_keep_alive = prefs.getBoolean("tune_keep_alive", true);
                        boolean tune = (tune_keep_alive && !first &&
                                !account.keep_alive_ok && account.poll_interval > 9 &&
                                Math.abs(idleTime - account.poll_interval * 60 * 1000L) < 60 * 1000L);
                        if (tune_keep_alive && !first && !account.keep_alive_ok)
                            EntityLog.log(this, account.name +
                                    " Tune interval=" + account.poll_interval +
                                    " idle=" + idleTime + "/" + tune);
                        try {
                            if (!state.isRecoverable())
                                throw new StoreClosedException(
                                        iservice.getStore(),
                                        "Unrecoverable",
                                        new Exception(state.getUnrecoverable()));

                            // Sends store NOOP
                            EntityLog.log(this, account.name + " checking store");
                            if (!iservice.getStore().isConnected())
                                throw new StoreClosedException(iservice.getStore(), "NOOP");

                            if (!getMainLooper().getThread().isAlive()) {
                                Log.e("App died");
                                EntityLog.log(this, account.name + " app died");
                                state.stop();
                                throw new StoreClosedException(iservice.getStore(), "App died");
                            }

                            if (sync) {
                                EntityLog.log(this, account.name + " checking folders");
                                for (EntityFolder folder : mapFolders.keySet())
                                    if (folder.synchronize)
                                        if (!folder.poll && capIdle) {
                                            // Sends folder NOOP
                                            if (!mapFolders.get(folder).isOpen())
                                                throw new StoreClosedException(iservice.getStore(), folder.name);
                                        } else {
                                            if (folder.poll_count == 0)
                                                EntityOperation.sync(this, folder.id, false);
                                            folder.poll_count = (folder.poll_count + 1) % folder.poll_factor;
                                            db.folder().setFolderPollCount(folder.id, folder.poll_count);
                                            Log.i(folder.name + " poll count=" + folder.poll_count);
                                        }
                            }
                        } catch (Throwable ex) {
                            if (tune) {
                                account.keep_alive_failed++;
                                account.keep_alive_succeeded = 0;
                                if (account.keep_alive_failed >= 3) {
                                    account.keep_alive_failed = 0;
                                    account.poll_interval = account.poll_interval - 2;
                                    db.account().setAccountKeepAliveInterval(account.id, account.poll_interval);
                                }
                                db.account().setAccountKeepAliveValues(account.id,
                                        account.keep_alive_failed, account.keep_alive_succeeded);
                                EntityLog.log(this, account.name + " keep alive" +
                                        " failed=" + account.keep_alive_failed +
                                        " succeeded=" + account.keep_alive_succeeded +
                                        " interval=" + account.poll_interval +
                                        " idle=" + idleTime);
                            }

                            throw ex;
                        }

                        if (tune) {
                            account.keep_alive_failed = 0;
                            account.keep_alive_succeeded++;
                            db.account().setAccountKeepAliveValues(account.id,
                                    account.keep_alive_failed, account.keep_alive_succeeded);
                            if (account.keep_alive_succeeded >= 3) {
                                account.keep_alive_ok = true;
                                db.account().setAccountKeepAliveOk(account.id, true);
                                if (!BuildConfig.PLAY_STORE_RELEASE)
                                    Log.w(account.host + " set keep-alive=" + account.poll_interval);
                                EntityLog.log(this, account.name + " keep alive ok");
                            } else
                                EntityLog.log(this, account.name + " keep alive" +
                                        " failed=" + account.keep_alive_failed +
                                        " succeeded=" + account.keep_alive_succeeded +
                                        " interval=" + account.poll_interval +
                                        " idle=" + idleTime);
                        }

                        // Successfully connected: reset back off time
                        state.setBackoff(CONNECT_BACKOFF_START);

                        // Record successful connection
                        account.last_connected = new Date().getTime();
                        EntityLog.log(this, account.name + " set last_connected=" + new Date(account.last_connected));
                        db.account().setAccountConnected(account.id, account.last_connected);
                        db.account().setAccountWarning(account.id, capIdle ? null : getString(R.string.title_no_idle));

                        // Get quota
                        if (iservice.hasCapability("QUOTA"))
                            try {
                                // https://tools.ietf.org/id/draft-melnikov-extra-quota-00.html
                                Quota[] quotas = ((IMAPStore) iservice.getStore()).getQuota("INBOX");
                                if (quotas != null) {
                                    long usage = 0;
                                    long limit = 0;
                                    for (Quota quota : quotas)
                                        if (quota.resources != null)
                                            for (Quota.Resource resource : quota.resources) {
                                                Log.i("Quota " + resource.name + " " + resource.usage + "/" + resource.limit);
                                                if ("STORAGE".equalsIgnoreCase(resource.name)) {
                                                    usage += resource.usage * 1024;
                                                    limit = Math.max(limit, resource.limit * 1024);
                                                }
                                            }
                                    db.account().setAccountQuota(account.id, usage, limit);
                                }
                            } catch (MessagingException ex) {
                                Log.w(ex);
                                db.account().setAccountQuota(account.id, null, null);
                            }
                        else
                            db.account().setAccountQuota(account.id, null, null);

                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.cancel("receive:" + account.id, 1);
                        nm.cancel("alert:" + account.id, 1);

                        // Schedule keep alive alarm
                        Intent intent = new Intent(this, ServiceSynchronize.class);
                        intent.setAction("keepalive:" + account.id);
                        PendingIntent pi = PendingIntentCompat.getForegroundService(
                                this, PI_KEEPALIVE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        try {
                            long duration = account.poll_interval * 60 * 1000L;
                            long trigger = System.currentTimeMillis() + duration;
                            EntityLog.log(this, "### " + account.name + " keep alive" +
                                    " wait=" + account.poll_interval + " until=" + new Date(trigger));
                            AlarmManagerCompat.setAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, trigger, pi);

                            try {
                                wlAccount.release();
                                state.acquire(2 * duration, false);
                                Log.i("### " + account.name + " keeping alive");
                            } catch (InterruptedException ex) {
                                EntityLog.log(this, account.name + " waited state=" + state);
                            } finally {
                                wlAccount.acquire();
                            }
                        } finally {
                            am.cancel(pi);
                        }

                        first = false;
                    }

                    Log.i(account.name + " done state=" + state);
                } catch (Throwable ex) {
                    last_fail = ex;
                    Log.e(account.name, ex);
                    EntityLog.log(this,
                            account.name + " " + Log.formatThrowable(ex, false));
                    db.account().setAccountError(account.id, Log.formatThrowable(ex));

                    // Report account connection error
                    if (account.last_connected != null && !ConnectionHelper.airplaneMode(this)) {
                        EntityLog.log(this, account.name + " last connected: " + new Date(account.last_connected));

                        int pollInterval = prefs.getInt("poll_interval", DEFAULT_POLL_INTERVAL);
                        long now = new Date().getTime();
                        long delayed = now - account.last_connected - account.poll_interval * 60 * 1000L;
                        long maxDelayed = (pollInterval > 0 && !account.poll_exempted
                                ? pollInterval * ACCOUNT_ERROR_AFTER_POLL : ACCOUNT_ERROR_AFTER) * 60 * 1000L;
                        if (delayed > maxDelayed && state.getBackoff() > BACKOFF_ERROR_AFTER) {
                            Log.i("Reporting sync error after=" + delayed);
                            Throwable warning = new Throwable(
                                    getString(R.string.title_no_sync,
                                            Helper.getDateTimeInstance(this, DateFormat.SHORT, DateFormat.SHORT)
                                                    .format(account.last_connected)), ex);
                            try {
                                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                nm.notify("receive:" + account.id, 1,
                                        Core.getNotificationError(this, "warning", account.name, warning)
                                                .build());
                            } catch (Throwable ex1) {
                                Log.w(ex1);
                            }
                        }
                    }
                } finally {
                    // Update state
                    EntityLog.log(this, account.name + " closing");

                    // Stop watching operations
                    Log.i(account.name + " stop watching operations");
                    getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (cowner.value != null)
                                    cowner.value.destroy();
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                        }
                    });

                    // Stop executing operations
                    Log.i(account.name + " stop executing operations");
                    state.resetBatches();
                    ((ThreadPoolExecutor) executor).getQueue().clear();

                    // Close folders
                    for (EntityFolder folder : mapFolders.keySet()) {
                        if (folder.synchronize && !folder.poll && mapFolders.get(folder) != null) {
                            db.folder().setFolderState(folder.id, "closing");
                            try {
                                if (iservice.getStore().isConnected())
                                    mapFolders.get(folder).close();
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }
                        }

                        db.folder().setFolderState(folder.id, null);
                    }

                    // Close store
                    try {
                        db.account().setAccountState(account.id, "closing");
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
                }

                if (state.isRunning()) {
                    long now = new Date().getTime();

                    // Check for fast successive server, connectivity, etc failures
                    long poll_interval = Math.min(account.poll_interval, CONNECT_BACKOFF_ALARM_START);
                    long fail_threshold = poll_interval * 60 * 1000L * FAST_FAIL_THRESHOLD / 100;
                    long was_connected = (account.last_connected == null ? 0 : now - account.last_connected);
                    if (was_connected < fail_threshold && !Helper.isCharging(this)) {
                        if (state.getBackoff() == CONNECT_BACKOFF_START) {
                            fast_fails++;
                            if (fast_fails == 1)
                                first_fail = now;
                            else {
                                long avg_fail = (now - first_fail) / fast_fails;
                                if (avg_fail < fail_threshold) {
                                    long missing = (fail_threshold - avg_fail) * fast_fails;
                                    int compensate = (int) (missing / (CONNECT_BACKOFF_ALARM_START * 60 * 1000L));
                                    if (compensate > 0) {
                                        if (account.last_connected != null &&
                                                now - account.last_connected < CONNECT_BACKOFF_GRACE)
                                            compensate = 1;

                                        int backoff = compensate * CONNECT_BACKOFF_ALARM_START;
                                        if (backoff > CONNECT_BACKOFF_ALARM_MAX)
                                            backoff = CONNECT_BACKOFF_ALARM_MAX;

                                        String msg = "Fast" +
                                                " fails=" + fast_fails +
                                                " was=" + (was_connected / 1000L) +
                                                " first=" + ((now - first_fail) / 1000L) +
                                                " poll=" + poll_interval +
                                                " avg=" + (avg_fail / 1000L) + "/" + (fail_threshold / 1000L) +
                                                " missing=" + (missing / 1000L) +
                                                " compensate=" + compensate +
                                                " backoff=" + backoff +
                                                " host=" + account.host +
                                                " ex=" + Log.formatThrowable(last_fail, false);
                                        if (compensate > 1)
                                            Log.e(msg);
                                        EntityLog.log(this, msg);

                                        state.setBackoff(backoff * 60);
                                    }
                                }

                                // Autofix too many simultaneous connections
                                if (ConnectionHelper.isMaxConnections(last_fail)) {
                                    boolean auto_optimize = prefs.getBoolean("auto_optimize", false);
                                    if (auto_optimize ||
                                            account.last_connected == null ||
                                            now - account.last_connected > AUTOFIX_TOO_MANY_FOLDERS) {
                                        int user = 0;
                                        int system = 0;
                                        List<EntityFolder> folders = db.folder().getFolders(account.id, false, true);
                                        if (folders != null) {
                                            for (EntityFolder folder : folders)
                                                if (folder.synchronize && !folder.poll && EntityFolder.USER.equals(folder.type)) {
                                                    user++;
                                                    db.folder().setFolderPoll(folder.id, true);
                                                }
                                            if (user == 0)
                                                for (EntityFolder folder : folders)
                                                    if (folder.synchronize && !folder.poll && !EntityFolder.INBOX.equals(folder.type)) {
                                                        system++;
                                                        db.folder().setFolderPoll(folder.id, true);
                                                    }
                                        }

                                        if (user > 0 || system > 0) {
                                            Log.e("Autofix user=" + user + " system=" + system + " host=" + account.host);
                                            EntityLog.log(this, account.name + " set poll user=" + user + " system=" + system);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        fast_fails = 0;
                        first_fail = 0;
                    }

                    int backoff = state.getBackoff();
                    int recently = (lastLost + LOST_RECENTLY < now ? 1 : 2);
                    EntityLog.log(this, account.name + " backoff=" + backoff + " recently=" + recently);

                    if (backoff <= CONNECT_BACKOFF_MAX) {
                        // Short back-off period, keep device awake
                        try {
                            db.account().setAccountBackoff(account.id, System.currentTimeMillis() + backoff * 1000L * recently);
                            state.acquire(backoff * 1000L * recently, true);
                        } catch (InterruptedException ex) {
                            Log.w(account.name + " backoff " + ex.toString());
                        } finally {
                            db.account().setAccountBackoff(account.id, null);
                        }
                    } else {
                        // Cancel transient sync operations
                        if (isTransient(account)) {
                            List<EntityOperation> syncs = db.operation().getOperations(account.id, EntityOperation.SYNC);
                            if (syncs != null) {
                                for (EntityOperation op : syncs) {
                                    db.folder().setFolderSyncState(op.folder, null);
                                    db.operation().deleteOperation(op.id);
                                }
                                Log.i(account.name + " cancelled syncs=" + syncs.size());
                            }
                        }

                        // Long back-off period, let device sleep
                        Intent intent = new Intent(this, ServiceSynchronize.class);
                        intent.setAction("backoff:" + account.id);
                        PendingIntent pi = PendingIntentCompat.getForegroundService(
                                this, PI_BACKOFF, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        try {
                            long trigger = System.currentTimeMillis() + backoff * 1000L;
                            EntityLog.log(this, "### " + account.name + " backoff until=" + new Date(trigger));
                            AlarmManagerCompat.setAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, trigger, pi);

                            try {
                                db.account().setAccountBackoff(account.id, trigger);
                                wlAccount.release();
                                state.acquire(2 * backoff * 1000L, true);
                                Log.i("### " + account.name + " backoff done");
                            } catch (InterruptedException ex) {
                                Log.w(account.name + " backoff " + ex.toString());
                            } finally {
                                wlAccount.acquire();
                                db.account().setAccountBackoff(account.id, null);
                            }
                        } finally {
                            am.cancel(pi);
                        }
                    }

                    if (backoff < CONNECT_BACKOFF_MAX)
                        state.setBackoff(backoff * 2);
                    else if (backoff == CONNECT_BACKOFF_MAX)
                        if (Helper.isCharging(this))
                            EntityLog.log(this, "Device is charging");
                        else
                            state.setBackoff(CONNECT_BACKOFF_ALARM_START * 60);
                    else if (backoff < CONNECT_BACKOFF_ALARM_MAX * 60)
                        state.setBackoff(backoff * 2);
                }

                currentThread = Thread.currentThread().getId();
            }

            if (currentThread == null || !currentThread.equals(thread))
                Log.e(account.name + " orphan thread id=" + currentThread + "/" + thread);
        } finally {
            EntityLog.log(this, account.name + " stopped");
            wlAccount.release();
        }
    }

    private boolean isTransient(EntityAccount account) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = prefs.getInt("poll_interval", DEFAULT_POLL_INTERVAL);
        return (!enabled || account.ondemand || (pollInterval > 0 && !account.poll_exempted));
    }

    private void optimizeAccount(EntityAccount account, String reason) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean auto_optimize = prefs.getBoolean("auto_optimize", false);
        if (!auto_optimize)
            return;

        DB db = DB.getInstance(this);

        int pollInterval = prefs.getInt("poll_interval", DEFAULT_POLL_INTERVAL);
        EntityLog.log(this, "Auto optimize account=" + account.name + " poll interval=" + pollInterval);
        if (pollInterval == 0) {
            try {
                db.beginTransaction();
                for (EntityAccount a : db.account().getAccounts())
                    db.account().setAccountPollExempted(a.id, !a.id.equals(account.id));
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            prefs.edit().putInt("poll_interval", OPTIMIZE_POLL_INTERVAL).apply();
        } else if (pollInterval <= 60 && account.poll_exempted) {
            db.account().setAccountPollExempted(account.id, false);
            ServiceSynchronize.eval(this, "Optimize=" + reason);
        }
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            updateNetworkState(network, "available");
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities caps) {
            updateNetworkState(network, "capabilities");
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties props) {
            updateNetworkState(network, "properties");
        }

        @Override
        public void onLost(@NonNull Network network) {
            updateNetworkState(network, "lost");
        }
    };

    private BroadcastReceiver connectionChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Received intent=" + intent +
                    " " + TextUtils.join(" ", Log.getExtras(intent.getExtras())));

            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                boolean on = intent.getBooleanExtra("state", false);
                if (!on)
                    lastLost = 0;
            }

            Network active = ConnectionHelper.getActiveNetwork(ServiceSynchronize.this);
            updateNetworkState(active, "connectivity");
        }
    };

    private synchronized void updateNetworkState(Network network, String reason) {
        Network active = ConnectionHelper.getActiveNetwork(this);
        if (active != null && !active.equals(lastActive)) {
            if (ConnectionHelper.isConnected(this, active)) {
                EntityLog.log(this, reason + ": new active network=" + active + "/" + lastActive);
                lastActive = active;
            }
        } else if (lastActive != null) {
            if (!ConnectionHelper.isConnected(this, lastActive)) {
                EntityLog.log(this, reason + ": lost active network=" + lastActive);
                lastActive = null;
                lastLost = new Date().getTime();
            }
        }

        if (Objects.equals(network, active)) {
            ConnectionHelper.NetworkState ns = ConnectionHelper.getNetworkState(this);
            if (!Objects.equals(lastNetworkState, ns)) {
                EntityLog.log(this, reason + ": updating state network=" + active +
                        " info=" + ConnectionHelper.getNetworkInfo(this, active) + " " + ns);
                lastNetworkState = ns;
                liveNetworkState.postValue(ns);
            }
        }

        boolean isSuitable = (lastNetworkState != null && lastNetworkState.isSuitable());
        if (lastSuitable == null || lastSuitable != isSuitable) {
            lastSuitable = isSuitable;
            EntityLog.log(this, reason + ": updated suitable=" + lastSuitable);

            if (!isBackgroundService(this))
                try {
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(Helper.NOTIFICATION_SYNCHRONIZE, getNotificationService(lastAccounts, lastOperations).build());
                } catch (Throwable ex) {
                    Log.w(ex);
                }
        }
    }

    private BroadcastReceiver idleModeChangedReceiver = new BroadcastReceiver() {
        @Override
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onReceive(Context context, Intent intent) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            EntityLog.log(context, "Doze mode=" + pm.isDeviceIdleMode() +
                    " ignoring=" + pm.isIgnoringBatteryOptimizations(context.getPackageName()));
        }
    };

    private class MediatorState extends MediatorLiveData<List<TupleAccountNetworkState>> {
        boolean running = true;
        private ConnectionHelper.NetworkState lastNetworkState = null;
        private List<TupleAccountState> lastAccountStates = null;

        private void post(Bundle command) {
            Log.i("### command posted");
            for (String extra : Log.getExtras(command))
                Log.i("### " + extra);
            post(command, lastNetworkState, lastAccountStates);
        }

        private void post(ConnectionHelper.NetworkState networkState) {
            lastNetworkState = networkState;
            post(null, lastNetworkState, lastAccountStates);
        }

        private void post(List<TupleAccountState> accountStates) {
            lastAccountStates = accountStates;
            post(null, lastNetworkState, lastAccountStates);
        }

        private void postDestroy() {
            if (running) {
                running = false;
                postValue(null);
            }
        }

        private void post(Bundle command, ConnectionHelper.NetworkState networkState, List<TupleAccountState> accountStates) {
            if (!running) {
                Log.i("### not running");
                return;
            }

            if (networkState == null)
                networkState = ConnectionHelper.getNetworkState(ServiceSynchronize.this);

            if (accountStates == null)
                return;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServiceSynchronize.this);
            boolean enabled = prefs.getBoolean("enabled", true);
            int pollInterval = prefs.getInt("poll_interval", DEFAULT_POLL_INTERVAL);

            long[] schedule = getSchedule(ServiceSynchronize.this);
            long now = new Date().getTime();
            boolean scheduled = (schedule == null || (now >= schedule[0] && now < schedule[1]));

            if (command == null) {
                command = new Bundle();
                command.putString("name", "eval");
            }

            List<TupleAccountNetworkState> result = new ArrayList<>();
            for (TupleAccountState accountState : accountStates)
                result.add(new TupleAccountNetworkState(
                        enabled && (pollInterval == 0 || accountState.poll_exempted) && scheduled,
                        command,
                        networkState,
                        accountState));

            postValue(result);
        }
    }

    static void boot(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        // Reset accounts
                        for (EntityAccount account : db.account().getAccounts())
                            db.account().setAccountState(account.id, null);

                        // reset folders
                        for (EntityFolder folder : db.folder().getFolders()) {
                            db.folder().setFolderState(folder.id, null);
                            db.folder().setFolderSyncState(folder.id, null);
                            db.folder().setFolderPollCount(folder.id, 0);
                        }

                        // Reset operations
                        db.operation().resetOperationStates();

                        // Restore notifications
                        db.message().clearNotifyingMessages();

                        // Restore snooze timers
                        for (EntityMessage message : db.message().getSnoozed(null))
                            EntityMessage.snooze(context, message.id, message.ui_snoozed);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    // Restore schedule
                    schedule(context, false);

                    // Init service
                    int accounts = db.account().getSynchronizingAccounts().size();
                    if (accounts > 0) {
                        // Reload: watchdog or user might have started service already
                        reload(context, null, false, "boot");
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }, "synchronize:boot");
        thread.setPriority(THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    private static void schedule(Context context, boolean sync) {
        Intent intent = new Intent(context, ServiceSynchronize.class);
        intent.setAction("alarm");
        PendingIntent pi = PendingIntentCompat.getForegroundService(
                context, PI_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        boolean poll;
        Long at = null;
        long[] schedule = getSchedule(context);
        if (schedule == null)
            poll = true;
        else {
            long now = new Date().getTime();
            long next = (now < schedule[0] ? schedule[0] : schedule[1]);
            poll = (now >= schedule[0] && now < schedule[1]);

            Log.i("Schedule now=" + new Date(now));
            Log.i("Schedule start=" + new Date(schedule[0]));
            Log.i("Schedule end=" + new Date(schedule[1]));
            Log.i("Schedule next=" + new Date(next));
            Log.i("Schedule poll=" + poll);

            AlarmManagerCompat.setAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, next, pi);

            if (sync & poll) {
                at = now + 30 * 1000L;
                Log.i("Sync at schedule start=" + new Date(at));
            }
        }

        ServiceUI.schedule(context, poll, at);
    }

    static long[] getSchedule(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        boolean schedule = prefs.getBoolean("schedule", false);

        if (!enabled || !schedule)
            return null;

        if (!ActivityBilling.isPro(context))
            return null;

        int minuteStart = prefs.getInt("schedule_start", 0);
        int minuteEnd = prefs.getInt("schedule_end", 0);

        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.HOUR_OF_DAY, minuteStart / 60);
        calStart.set(Calendar.MINUTE, minuteStart % 60);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        calStart.add(Calendar.DATE, -1);

        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.HOUR_OF_DAY, minuteEnd / 60);
        calEnd.set(Calendar.MINUTE, minuteEnd % 60);
        calEnd.set(Calendar.SECOND, 0);
        calEnd.set(Calendar.MILLISECOND, 0);
        if (minuteEnd > minuteStart)
            calEnd.add(Calendar.DATE, -1);

        long now = new Date().getTime();

        boolean found = false;
        for (int i = 0; i < 8; i++) {
            int sdow = calStart.get(Calendar.DAY_OF_WEEK) - 1;
            int edow = calEnd.get(Calendar.DAY_OF_WEEK) - 1;
            boolean son = prefs.getBoolean("schedule_day" + sdow, true);
            boolean eon = prefs.getBoolean("schedule_day" + edow, true);

            if (BuildConfig.DEBUG)
                Log.i("@@@ eval dow=" + sdow + "/" + edow +
                        " on=" + son + "/" + eon +
                        " start=" + new Date(calStart.getTimeInMillis()) +
                        " end=" + new Date(calEnd.getTimeInMillis()));

            if ((son || eon) &&
                    now < calEnd.getTimeInMillis() &&
                    (i > 0 || (now >= calStart.getTimeInMillis() && eon))) {
                found = true;

                if (!son) {
                    calStart.set(Calendar.HOUR_OF_DAY, 0);
                    calStart.set(Calendar.MINUTE, 0);
                    calStart.add(Calendar.DATE, 1);
                }

                if (!eon) {
                    calEnd.set(Calendar.HOUR_OF_DAY, 0);
                    calEnd.set(Calendar.MINUTE, 0);
                }

                break;
            }

            calStart.add(Calendar.DATE, 1);
            calEnd.add(Calendar.DATE, 1);
        }

        if (!found) {
            if (BuildConfig.DEBUG)
                Log.i("@@@ not found");
            return null;
        }

        long start = calStart.getTimeInMillis();
        long end = calEnd.getTimeInMillis();

        if (BuildConfig.DEBUG) {
            Log.i("@@@ start=" + new Date(start));
            Log.i("@@@ end=" + new Date(end));
        }

        if (now > end)
            Log.e("Schedule invalid now=" + new Date(now) + " end=" + new Date(end));
        if (start > end)
            Log.e("Schedule invalid start=" + new Date(start) + " end=" + new Date(end));

        return new long[]{start, end};
    }

    static void eval(Context context, String reason) {
        start(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("eval")
                        .putExtra("reason", reason));
    }

    static void reload(Context context, Long account, boolean force, String reason) {
        start(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("reload")
                        .putExtra("account", account == null ? -1 : account)
                        .putExtra("force", force)
                        .putExtra("reason", reason));
    }

    static void reschedule(Context context) {
        start(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("alarm"));
    }

    static void state(Context context, boolean foreground) {
        start(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("state")
                        .putExtra("foreground", foreground));
    }

    static void watchdog(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        if (enabled)
            start(context,
                    new Intent(context, ServiceSynchronize.class)
                            .setAction("watchdog"));
    }

    private static void start(Context context, Intent intent) {
        if (isBackgroundService(context))
            context.startService(intent);
        else
            ContextCompat.startForegroundService(context, intent);
    }

    private static boolean isBackgroundService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("background_service", false);
    }
}
