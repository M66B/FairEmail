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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.app.AlarmManager;
import android.app.Notification;
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
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.os.PowerManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.car.app.connection.CarConnection;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import me.leolin.shortcutbadger.ShortcutBadgerAlt;

public class ServiceSynchronize extends ServiceBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Network lastActive = null;
    private Boolean lastSuitable = null;
    private long lastLost = 0;
    private int lastAccounts = 0;
    private int lastOperations = 0;
    private ConnectionHelper.NetworkState lastNetworkState = null;
    private boolean isInCall = false;
    private boolean isInCar = false;
    private boolean isOptimizing = false;

    private MutableLiveData<Boolean> foreground = new MutableLiveData<>();
    private final Map<Long, Core.State> coreStates = new Hashtable<>();
    private final MutableLiveData<ConnectionHelper.NetworkState> liveNetworkState = new MutableLiveData<>();
    private final MutableLiveData<List<TupleAccountState>> liveAccountState = new MutableLiveData<>();
    private final MediatorState liveAccountNetworkState = new MediatorState();

    private static final ExecutorService executorService =
            Helper.getBackgroundExecutor(1, "sync");
    private static final ExecutorService executorNotify =
            Helper.getBackgroundExecutor(1, "notify");

    private static final long BACKUP_DELAY = 30 * 1000L; // milliseconds
    private static final int QUIT_DELAY = 10; // seconds
    private static final long WATCHDOG_INTERVAL = 60 * 60 * 1000L; // milliseconds

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
            "download_headers", "download_eml",
            "prefer_ip4", "bind_socket", "standalone_vpn", "tcp_keep_alive", // force reconnect
            "ssl_harden", "ssl_harden_strict", "cert_strict", "bouncy_castle", "bc_fips", // force reconnect
            "experiments", "debug", "protocol", // force reconnect
            "auth_plain", "auth_login", "auth_ntlm", "auth_sasl", "auth_apop", // force reconnect
            "keep_alive_poll", "empty_pool", "idle_done", // force reconnect
            "exact_alarms" // force schedule
    ));

    static final int PI_ALARM = 1;
    static final int PI_BACKOFF = 2;
    static final int PI_KEEPALIVE = 3;
    static final int PI_ENABLE = 4;
    static final int PI_POLL = 5;
    static final int PI_WATCHDOG = 6;
    static final int PI_UNSNOOZE = 7;
    static final int PI_EXISTS = 8;

    @Override
    public void onCreate() {
        EntityLog.log(this, "Service create" +
                " version=" + BuildConfig.VERSION_NAME + BuildConfig.REVISION +
                " process=" + android.os.Process.myPid());
        super.onCreate();

        if (isBackgroundService(this))
            stopForeground(true);
        else
            startForeground(NotificationHelper.NOTIFICATION_SYNCHRONIZE,
                    getNotificationService(null, null));

        isOptimizing = Boolean.FALSE.equals(Helper.isIgnoringOptimizations(this));

        // Listen for network changes
        ConnectivityManager cm = Helper.getSystemService(this, ConnectivityManager.class);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        // Removed because of Android VPN service
        // builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        IntentFilter iif = new IntentFilter();
        iif.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        iif.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        ContextCompat.registerReceiver(this,
                connectionChangedReceiver,
                iif,
                ContextCompat.RECEIVER_NOT_EXPORTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ContextCompat.registerReceiver(this,
                    idleModeChangedReceiver,
                    new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED),
                    ContextCompat.RECEIVER_NOT_EXPORTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            ContextCompat.registerReceiver(this,
                    dataSaverChanged,
                    new IntentFilter(ConnectivityManager.ACTION_RESTRICT_BACKGROUND_CHANGED),
                    ContextCompat.RECEIVER_NOT_EXPORTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            IntentFilter suspend = new IntentFilter();
            suspend.addAction(Intent.ACTION_MY_PACKAGE_SUSPENDED);
            suspend.addAction(Intent.ACTION_MY_PACKAGE_UNSUSPENDED);
            ContextCompat.registerReceiver(this,
                    suspendChanged,
                    suspend,
                    ContextCompat.RECEIVER_NOT_EXPORTED);
        }

        ContextCompat.registerReceiver(this,
                batteryChanged,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED);

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
            private boolean lastConnected = false;
            private int lastEventId = 0;
            private int lastQuitId = -1;
            private List<Long> initialized = new ArrayList<>();
            private List<TupleAccountNetworkState> accountStates = new ArrayList<>();
            private PowerManager pm = Helper.getSystemService(ServiceSynchronize.this, PowerManager.class);
            private PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":service");

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
                    int enabled = 0;
                    int connected = 0;
                    int accounts = 0;
                    int operations = 0;
                    boolean event = false;
                    boolean runFts = true;
                    boolean runService = false;
                    for (TupleAccountNetworkState current : accountNetworkStates) {
                        Log.d("### evaluating " + current);
                        if (!initialized.contains(current.accountState.id)) {
                            initialized.add(current.accountState.id);
                            init(current);
                        }
                        if (current.accountState.shouldRun(current.enabled))
                            runService = true;
                        if (!current.accountState.isTransient(ServiceSynchronize.this)) {
                            if (current.accountState.isEnabled(current.enabled))
                                enabled++;
                            if ("connected".equals(current.accountState.state))
                                connected++;
                            if ("connected".equals(current.accountState.state) || current.accountState.backoff_until != null)
                                accounts++;
                        }
                        if (current.accountState.synchronize)
                            operations += current.accountState.operations;
                        if (current.accountState.operations > 0 && current.canRun(ServiceSynchronize.this))
                            runFts = false;

                        long account = current.command.getLong("account", -1);
                        if (account > 0 && !current.accountState.id.equals(account))
                            continue;

                        boolean sync = current.command.getBoolean("sync", false);
                        boolean force = current.command.getBoolean("force", false);
                        if (force) {
                            sync = true;
                            current.accountState.operations++;
                        }

                        int index = accountStates.indexOf(current);
                        if (index < 0) {
                            if (current.canRun(ServiceSynchronize.this)) {
                                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                        "### new " + current +
                                                " force=" + force +
                                                " start=" + current.canRun(ServiceSynchronize.this) +
                                                " sync=" + current.accountState.isEnabled(current.enabled) +
                                                " enabled=" + current.accountState.synchronize +
                                                " ondemand=" + current.accountState.ondemand +
                                                " folders=" + current.accountState.folders +
                                                " ops=" + current.accountState.operations +
                                                " tbd=" + current.accountState.tbd +
                                                " state=" + current.accountState.state +
                                                " active=" + current.networkState.getActive());
                                event = true;
                                start(current, current.accountState.isEnabled(current.enabled) || sync, force);
                            }
                        } else {
                            boolean reload = false;
                            switch (current.command.getString("name")) {
                                case "reload":
                                    reload = true;
                                    break;
                            }

                            TupleAccountNetworkState prev = accountStates.get(index);
                            Core.State state = coreStates.get(current.accountState.id);
                            if (state != null)
                                state.setNetworkState(current.networkState);

                            accountStates.remove(index);

                            // Some networks disallow email server connections:
                            // - reload on network type change when disconnected
                            if (reload ||
                                    prev.canRun(ServiceSynchronize.this) != current.canRun(ServiceSynchronize.this) ||
                                    !prev.accountState.equals(current.accountState)) {
                                if (prev.canRun(ServiceSynchronize.this) || current.canRun(ServiceSynchronize.this))
                                    EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                            "### changed " + current +
                                                    " reload=" + reload +
                                                    " force=" + force +
                                                    " stop=" + prev.canRun(ServiceSynchronize.this) +
                                                    " start=" + current.canRun(ServiceSynchronize.this) +
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
                                if (prev.canRun(ServiceSynchronize.this)) {
                                    event = true;
                                    stop(prev);
                                }
                                if (current.canRun(ServiceSynchronize.this)) {
                                    event = true;
                                    boolean dosync = (sync ||
                                            current.accountState.isEnabled(current.enabled) ||
                                            !prev.accountState.equals(current.accountState)); // Token refreshed
                                    start(current, dosync, force);
                                }
                            } else if (current.canRun(ServiceSynchronize.this) &&
                                    state != null && !state.isAlive()) {
                                Log.e(current + " died");
                                EntityLog.log(ServiceSynchronize.this, "### died " + current);
                                event = true;
                                start(current, current.accountState.isEnabled(current.enabled) || sync, force);
                            } else {
                                if (state != null) {
                                    Network p = prev.networkState.getActive();
                                    if (p != null && !p.equals(current.networkState.getActive())) {
                                        EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                                "### changed " + current +
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

                    boolean ok = (enabled > 0 && connected == enabled);
                    if (lastConnected != ok) {
                        lastConnected = ok;
                        prefs.edit().putBoolean("connected", ok).apply();
                        WidgetSync.update(ServiceSynchronize.this);
                    }

                    if (event) {
                        lastEventId++;
                        EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                "### eventId=" + lastEventId);
                    }

                    if (lastAccounts != accounts || lastOperations != operations) {
                        lastAccounts = accounts;
                        lastOperations = operations;
                        if (runFts) {
                            fts = true;
                            WorkerFts.init(ServiceSynchronize.this, false);
                        } else if (fts) {
                            fts = false;
                            WorkerFts.cancel(ServiceSynchronize.this);
                        }

                        getMainHandler().removeCallbacks(backup);
                        getMainHandler().postDelayed(backup, BACKUP_DELAY);

                        if (!isBackgroundService(ServiceSynchronize.this))
                            try {
                                NotificationManager nm =
                                        Helper.getSystemService(ServiceSynchronize.this, NotificationManager.class);
                                if (NotificationHelper.areNotificationsEnabled(nm))
                                    nm.notify(NotificationHelper.NOTIFICATION_SYNCHRONIZE,
                                            getNotificationService(lastAccounts, lastOperations));
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
                        EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                "### quitting" +
                                        " run=" + runService +
                                        " startId=" + lastQuitId + "/" + lastEventId);
                        lastQuitId = lastEventId;
                        quit(lastEventId);
                    }
                }
            }

            private void init(final TupleAccountNetworkState accountNetworkState) {
                executorService.submit(new RunnableEx("state#init") {
                    @Override
                    public void delegate() {
                        long start = new Date().getTime();
                        try {
                            wl.acquire(Helper.WAKELOCK_MAX);

                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                    "### init " + accountNetworkState);

                            DB db = DB.getInstance(ServiceSynchronize.this);
                            try {
                                db.beginTransaction();

                                db.account().setAccountState(accountNetworkState.accountState.id, null);
                                db.account().setAccountBackoff(accountNetworkState.accountState.id, null);

                                for (EntityFolder folder : db.folder().getFolders(accountNetworkState.accountState.id, false, false)) {
                                    db.folder().setFolderState(folder.id, null);
                                    if (db.operation().getOperationCount(folder.id, EntityOperation.SYNC) == 0)
                                        db.folder().setFolderSyncState(folder.id, null);
                                    db.folder().setFolderPollCount(folder.id, 0);
                                }

                                db.operation().resetOperationStates(accountNetworkState.accountState.id);

                                db.setTransactionSuccessful();
                            } catch (Throwable ex) {
                                Log.e(ex);
                            } finally {
                                db.endTransaction();
                            }
                        } finally {
                            if (wl.isHeld())
                                wl.release();
                            else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                Log.e("state#init released elapse=" + (new Date().getTime() - start));
                        }
                    }
                });
            }

            private void start(final TupleAccountNetworkState accountNetworkState, boolean sync, boolean force) {
                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                        "Service start=" + accountNetworkState + " sync=" + sync + " force=" + force);

                final Core.State astate = new Core.State(accountNetworkState.networkState);
                astate.runnable(new RunnableEx("state#monitor") {
                    @Override
                    public void delegate() {
                        try {
                            ServiceMonitor.monitorAccount(ServiceSynchronize.this,
                                    accountNetworkState.accountState, astate, sync, force);
                        } catch (Throwable ex) {
                            Log.e(accountNetworkState.accountState.name, ex);
                        }
                    }
                }, "sync.account." + accountNetworkState.accountState.id);
                coreStates.put(accountNetworkState.accountState.id, astate);

                executorService.submit(new RunnableEx("state#start") {
                    @Override
                    public void delegate() {
                        long start = new Date().getTime();
                        try {
                            wl.acquire(Helper.WAKELOCK_MAX);

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
                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                    "### started=" + accountNetworkState);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        } finally {
                            if (wl.isHeld())
                                wl.release();
                            else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                Log.e("state#start released elapse=" + (new Date().getTime() - start));
                        }
                    }
                });
            }

            private void stop(final TupleAccountNetworkState accountNetworkState) {
                final Core.State state = coreStates.get(accountNetworkState.accountState.id);
                if (state == null)
                    return;
                coreStates.remove(accountNetworkState.accountState.id);

                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                        "Service stop=" + accountNetworkState);

                executorService.submit(new RunnableEx("state#stop") {
                    @Override
                    public void delegate() {
                        long start = new Date().getTime();
                        try {
                            wl.acquire(Helper.WAKELOCK_MAX);

                            Map<String, String> crumb = new HashMap<>();
                            crumb.put("account", accountNetworkState.accountState.id.toString());
                            crumb.put("connected", Boolean.toString(accountNetworkState.networkState.isConnected()));
                            crumb.put("suitable", Boolean.toString(accountNetworkState.networkState.isSuitable()));
                            crumb.put("unmetered", Boolean.toString(accountNetworkState.networkState.isUnmetered()));
                            crumb.put("roaming", Boolean.toString(accountNetworkState.networkState.isRoaming()));
                            crumb.put("lastLost", new Date(lastLost).toString());
                            Log.breadcrumb("stop", crumb);

                            Log.i("### stop=" + accountNetworkState);
                            db.account().setAccountThread(accountNetworkState.accountState.id, null);
                            state.stop();
                            state.join();
                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                    "### stopped=" + accountNetworkState);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        } finally {
                            if (wl.isHeld())
                                wl.release();
                            else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                Log.e("state#stop released elapse=" + (new Date().getTime() - start));
                        }
                    }
                });
            }

            private void delete(final TupleAccountNetworkState accountNetworkState) {
                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                        "Service delete=" + accountNetworkState);

                executorService.submit(new RunnableEx("state#delete") {
                    @Override
                    public void delegate() {
                        long start = new Date().getTime();
                        try {
                            wl.acquire(Helper.WAKELOCK_MAX);

                            DB db = DB.getInstance(ServiceSynchronize.this);
                            db.account().deleteAccount(accountNetworkState.accountState.id);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationManager nm = Helper.getSystemService(ServiceSynchronize.this, NotificationManager.class);
                                nm.deleteNotificationChannel(EntityAccount.getNotificationChannelId(accountNetworkState.accountState.id));
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        } finally {
                            if (wl.isHeld())
                                wl.release();
                            else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                Log.e("state#delete released elapse=" + (new Date().getTime() - start));
                        }
                    }
                });
            }

            private void quit(final Integer eventId) {
                executorService.submit(new RunnableEx("state#quit") {
                    @Override
                    public void delegate() {
                        long start = new Date().getTime();
                        try {
                            wl.acquire(Helper.WAKELOCK_MAX);

                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                    "### quit eventId=" + eventId);

                            if (eventId == null) {
                                // Service destroy
                                DB db = DB.getInstance(ServiceSynchronize.this);
                                List<EntityOperation> ops = db.operation().getOperations(EntityOperation.SYNC);
                                for (EntityOperation op : ops)
                                    db.folder().setFolderSyncState(op.folder, null);

                                getMainHandler().removeCallbacks(backup);
                                MessageClassifier.save(ServiceSynchronize.this);
                            } else {
                                // Yield update notifications/widgets
                                for (int i = 0; i < QUIT_DELAY; i++) {
                                    try {
                                        Thread.sleep(1000L);
                                    } catch (InterruptedException ex) {
                                        Log.w(ex);
                                    }

                                    if (!eventId.equals(lastEventId)) {
                                        EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                                "### quit cancelled eventId=" + eventId + "/" + lastEventId);
                                        return;
                                    }
                                }

                                // Stop service
                                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                        "### stopping self eventId=" + eventId);
                                stopSelf();
                                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                        "### stopped self eventId=" + eventId);

                                WorkerCleanup.cleanupConditionally(getApplicationContext());
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        } finally {
                            if (wl.isHeld())
                                wl.release();
                            else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                Log.e("state#quit released elapse=" + (new Date().getTime() - start));
                        }
                    }
                });
            }

            private final Runnable backup = new RunnableEx("state#backup") {
                @Override
                public void delegate() {
                    executorService.submit(new RunnableEx("state#backup#exec") {
                        @Override
                        public void delegate() {
                            long start = new Date().getTime();
                            try {
                                wl.acquire(Helper.WAKELOCK_MAX);

                                MessageClassifier.save(ServiceSynchronize.this);
                            } catch (Throwable ex) {
                                Log.e(ex);
                            } finally {
                                if (wl.isHeld())
                                    wl.release();
                                else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e("state#backup released elapse=" + (new Date().getTime() - start));
                            }
                        }
                    });
                }
            };
        });

        final TwoStateOwner cowner = new TwoStateOwner(this, "liveSynchronizing");

        db.folder().liveSynchronizing().observe(this, new Observer<List<TupleFolderSync>>() {
            private List<Long> lastAccounts = new ArrayList<>();
            private List<Long> lastFolders = new ArrayList<>();

            @Override
            public void onChanged(List<TupleFolderSync> syncs) {
                int syncing = 0;
                boolean changed = false;
                List<Long> accounts = new ArrayList<>();
                List<Long> folders = new ArrayList<>();
                if (syncs != null)
                    for (TupleFolderSync sync : syncs) {
                        if ("syncing".equals(sync.sync_state))
                            syncing++;

                        if (sync.unified && !accounts.contains(sync.account)) {
                            accounts.add(sync.account);
                            if (lastAccounts.contains(sync.account))
                                lastAccounts.remove(sync.account); // same
                            else
                                changed = true; // new
                        }

                        folders.add(sync.folder);
                        if (lastFolders.contains(sync.folder))
                            lastFolders.remove(sync.folder); // same
                        else
                            changed = true; // new
                    }

                changed = (changed || lastAccounts.size() > 0 || lastFolders.size() > 0); // deleted
                lastAccounts = accounts;
                lastFolders = folders;

                Log.i("Changed=" + changed +
                        " syncing=" + syncing +
                        " folders=" + folders.size() +
                        " accounts=" + accounts.size());

                if (syncing == 0)
                    cowner.start();
                else
                    cowner.stop();

                if (!changed)
                    return;

                for (String _key : prefs.getAll().keySet())
                    if (_key.startsWith("widget.") && _key.endsWith(".refresh") &&
                            prefs.getBoolean(_key, false)) {
                        int appWidgetId = Integer.parseInt(_key.split("\\.")[1]);

                        long account = prefs.getLong("widget." + appWidgetId + ".account", -1L);
                        long folder = prefs.getLong("widget." + appWidgetId + ".folder", -1L);

                        boolean state;
                        if (folder > 0)
                            state = folders.contains(folder);
                        else if (account > 0)
                            state = accounts.contains(account);
                        else
                            state = (accounts.size() > 0);

                        String key = "widget." + appWidgetId + ".syncing";
                        if (state != prefs.getBoolean(key, false)) {
                            prefs.edit().putBoolean(key, state).apply();
                            WidgetUnified.init(ServiceSynchronize.this, appWidgetId);
                        }
                    }
            }
        });

        // New message notifications batching

        NotificationHelper.NotificationData notificationData = new NotificationHelper.NotificationData(this);

        MutableLiveData<List<TupleMessageEx>> mutableUnseenNotify = new MutableLiveData<>();
        db.message().liveUnseenNotify().observe(cowner, new Observer<List<TupleMessageEx>>() {
            @Override
            public void onChanged(List<TupleMessageEx> messages) {
                mutableUnseenNotify.setValue(messages);
            }
        });

        final TwoStateOwner mowner = new TwoStateOwner(this, "mutableUnseenNotify");
        mowner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
            public void onStateChanged() {
                Lifecycle.State state = mowner.getLifecycle().getCurrentState();
                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Debug, "Owner state=" + state);
                if (state.equals(Lifecycle.State.DESTROYED))
                    mowner.getLifecycle().removeObserver(this);
            }
        });

        foreground.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean foreground) {
                Log.i("Observed foreground=" + foreground);
                boolean fg = Boolean.TRUE.equals(foreground);
                if (!fg && (isInCall || isInCar))
                    mowner.stop();
                else
                    mowner.start();
            }
        });

        MediaPlayerHelper.liveInCall(this, this, new MediaPlayerHelper.IInCall() {
            @Override
            public void onChanged(boolean inCall) {
                boolean suppress = prefs.getBoolean("notify_suppress_in_call", false);
                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Debug,
                        "In call=" + inCall + " suppress=" + suppress);
                isInCall = (inCall && suppress);
                boolean fg = Boolean.TRUE.equals(foreground.getValue());
                if (!fg && (isInCall || isInCar))
                    mowner.stop();
                else
                    mowner.start();
            }
        });

        new CarConnection(this).getType().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer connectionState) {
                boolean projection = (connectionState != null &&
                        connectionState == CarConnection.CONNECTION_TYPE_PROJECTION);
                boolean suppress = prefs.getBoolean("notify_suppress_in_car", false);
                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Debug,
                        "Projection=" + projection + " state=" + connectionState + " suppress=" + suppress);
                isInCar = (projection && suppress);
                boolean fg = Boolean.TRUE.equals(foreground.getValue());
                if (!fg && (isInCall || isInCar))
                    mowner.stop();
                else
                    mowner.start();
            }
        });

        mutableUnseenNotify.observe(mowner, new Observer<List<TupleMessageEx>>() {
            @Override
            public void onChanged(final List<TupleMessageEx> messages) {
                executorNotify.submit(new RunnableEx("mutableUnseenNotify") {
                    @Override
                    public void delegate() {
                        try {
                            boolean fg = Boolean.TRUE.equals(foreground.getValue());
                            NotificationHelper.notifyMessages(ServiceSynchronize.this, messages, notificationData, fg);
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

        // Message count widgets

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

                EntityLog.log(ServiceSynchronize.this, "Widget update");
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

                    EntityLog.log(ServiceSynchronize.this, "Badge count=" + count +
                            " enabled=" + badge + " Unseen/ignored=" + unseen_ignored);

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
                            ShortcutBadgerAlt.removeCount(ServiceSynchronize.this);
                        else
                            ShortcutBadgerAlt.applyCount(ServiceSynchronize.this, count);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            }
        });

        // Message list widgets

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
                updateNetworkState(null, "preference");
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

        unregisterReceiver(batteryChanged);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            unregisterReceiver(suspendChanged);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            unregisterReceiver(dataSaverChanged);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            unregisterReceiver(idleModeChangedReceiver);

        unregisterReceiver(connectionChangedReceiver);

        ConnectivityManager cm = Helper.getSystemService(this, ConnectivityManager.class);
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

        NotificationManager nm = Helper.getSystemService(this, NotificationManager.class);
        nm.cancel(NotificationHelper.NOTIFICATION_SYNCHRONIZE);

        super.onDestroy();
        CoalMine.watch(this, this.getClass().getName() + "#onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i("Task removed=" + rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String action = (intent == null ? null : intent.getAction());
            String reason = (intent == null ? null : intent.getStringExtra("reason"));
            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                    "### Service command " + intent +
                            " action=" + action + " reason=" + reason);
            Log.logExtras(intent);

            super.onStartCommand(intent, flags, startId);

            if (isBackgroundService(this))
                stopForeground(true);
            else
                startForeground(NotificationHelper.NOTIFICATION_SYNCHRONIZE,
                        getNotificationService(null, null));

            if (action != null) {
                switch (action.split(":")[0]) {
                    case "enable":
                        onEnable(intent);
                        break;

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

                    case "unsnooze":
                        onUnsnooze(intent);
                        break;

                    case "exists":
                        onExists(intent);
                        break;

                    case "state":
                        onState(intent);
                        break;

                    case "poll":
                        onPoll(intent);
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
            }
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                at android.app.ApplicationPackageManager.getUserIfProfile(ApplicationPackageManager.java:2190)
                at android.app.ApplicationPackageManager.getUserBadgeForDensity(ApplicationPackageManager.java:1006)
                at android.app.Notification$Builder.getProfileBadgeDrawable(Notification.java:2890)
                at android.app.Notification$Builder.hasThreeLines(Notification.java:3105)
                at android.app.Notification$Builder.build(Notification.java:3659)
                at androidx.core.app.NotificationCompatBuilder.buildInternal(NotificationCompatBuilder:426)
                at androidx.core.app.NotificationCompatBuilder.build(NotificationCompatBuilder:318)
                at androidx.core.app.NotificationCompat$Builder.build(NotificationCompat:2346)
                at eu.faircode.email.ServiceSynchronize.onStartCommand(ServiceSynchronize:890)
             */
        }

        return START_STICKY;
    }

    private void onEnable(Intent intent) {
        boolean enabled = intent.getBooleanExtra("enabled", true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean("enabled", enabled).apply();
        onEval(intent);
    }

    private void onEval(Intent intent) {
        Bundle command = new Bundle();
        command.putString("name", "eval");
        command.putLong("account", intent.getLongExtra("account", -1));
        liveAccountNetworkState.post(command);
    }

    private void onReload(Intent intent) {
        boolean force = intent.getBooleanExtra("force", false);
        if (force) {
            lastLost = 0;
            updateNetworkState(null, "force");
        }

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
            EntityLog.log(this, EntityLog.Type.Scheduling,
                    "### wakeup missing account=" + account);
        else {
            EntityLog.log(this, EntityLog.Type.Scheduling,
                    "### waking up account=" + account);
            if (!state.release())
                EntityLog.log(this, EntityLog.Type.Scheduling,
                        "### waking up failed account=" + account);
        }
    }

    private void onUnsnooze(Intent intent) {
        String action = intent.getAction();
        long id = Long.parseLong(action.split(":")[1]);

        Helper.getSerialExecutor().submit(new RunnableEx("unsnooze") {
            @Override
            public void delegate() {
                try {
                    EntityFolder folder;

                    DB db = DB.getInstance(ServiceSynchronize.this);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return;

                        folder = db.folder().getFolder(message.folder);
                        if (folder == null)
                            return;

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            return;

                        if (EntityFolder.OUTBOX.equals(folder.type)) {
                            Log.i("Delayed send id=" + message.id);
                            if (message.ui_snoozed != null) {
                                db.message().setMessageSnoozed(message.id, null);
                                EntityOperation.queue(ServiceSynchronize.this, message, EntityOperation.SEND);
                            }
                        } else {
                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                                    folder.name + " Unsnooze" +
                                            " id=" + message.id +
                                            " ui_seen=" + message.ui_seen + "" +
                                            " ui_ignored=" + message.ui_ignored +
                                            " ui_hide=" + message.ui_hide +
                                            " notifying=" + message.notifying +
                                            " silent=" + message.ui_silent +
                                            " local=" + message.ui_local_only +
                                            " received=" + new Date(message.received) +
                                            " sent=" + (message.sent == null ? null : new Date(message.sent)) +
                                            " created=" + (account.created == null ? null : new Date(account.created)) +
                                            " notify=" + folder.notify +
                                            " sync=" + account.synchronize);

                            if (folder.notify) {
                                List<EntityAttachment> attachments = db.attachment().getAttachments(id);

                                // A new message ID is needed for a new (wearable) notification
                                db.message().deleteMessage(id);

                                message.id = null;
                                message.fts = false;
                                message.ui_silent = false;
                                message.ui_local_only = false;
                                message.notifying = 0;
                                message.stored = new Date().getTime();
                                message.id = db.message().insertMessage(message);

                                if (message.content) {
                                    File source = EntityMessage.getFile(ServiceSynchronize.this, id);
                                    File target = message.getFile(ServiceSynchronize.this);
                                    try {
                                        Helper.copy(source, target);
                                    } catch (IOException ex) {
                                        Log.e(ex);
                                        db.message().resetMessageContent(message.id);
                                    }
                                }

                                for (EntityAttachment attachment : attachments) {
                                    File source = attachment.getFile(ServiceSynchronize.this);

                                    attachment.id = null;
                                    attachment.message = message.id;
                                    attachment.progress = null;
                                    attachment.id = db.attachment().insertAttachment(attachment);

                                    if (attachment.available) {
                                        File target = attachment.getFile(ServiceSynchronize.this);
                                        try {
                                            Helper.copy(source, target);
                                        } catch (IOException ex) {
                                            Log.e(ex);
                                            db.attachment().setError(attachment.id, Log.formatThrowable(ex, false));
                                        }
                                    }
                                }
                            }

                            // Show thread
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServiceSynchronize.this);
                            boolean threading = prefs.getBoolean("threading", true);
                            List<EntityMessage> messages = db.message().getMessagesByThread(
                                    message.account, message.thread, threading ? null : message.id, null);
                            for (EntityMessage threaded : messages)
                                db.message().setMessageSnoozed(threaded.id, null);

                            db.message().setMessageUnsnoozed(message.id, true);
                            EntityOperation.queue(ServiceSynchronize.this, message, EntityOperation.SEEN, false, false);
                        }

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    if (EntityFolder.OUTBOX.equals(folder.type))
                        ServiceSend.start(ServiceSynchronize.this);
                    else
                        ServiceSynchronize.eval(ServiceSynchronize.this, "unsnooze");
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private void onExists(Intent intent) {
        String action = intent.getAction();
        long id = Long.parseLong(action.split(":")[1]);

        Helper.getSerialExecutor().submit(new RunnableEx("exists") {
            @Override
            public void delegate() {
                try {
                    DB db = DB.getInstance(ServiceSynchronize.this);

                    try {
                        db.beginTransaction();

                        // Message could have been deleted in the meantime
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return;

                        EntityOperation.queue(ServiceSynchronize.this, message, EntityOperation.EXISTS, true);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    eval(ServiceSynchronize.this, "exists/delayed");
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private void onState(Intent intent) {
        boolean fg = intent.getBooleanExtra("foreground", false);
        foreground.postValue(fg);
        for (Core.State state : coreStates.values())
            state.setForeground(fg);
    }

    private void onPoll(Intent intent) {
        Helper.getSerialExecutor().submit(new RunnableEx("poll") {
            @Override
            public void delegate() {
                try {
                    long now = new Date().getTime();
                    long[] schedule = getSchedule(ServiceSynchronize.this);
                    boolean scheduled = (schedule == null || (now >= schedule[0] && now < schedule[1]));

                    boolean work = false;
                    DB db = DB.getInstance(ServiceSynchronize.this);
                    try {
                        db.beginTransaction();

                        List<EntityAccount> accounts = db.account().getPollAccounts(null);
                        for (EntityAccount account : accounts) {
                            JSONObject jcondition = new JSONObject();
                            try {
                                if (!TextUtils.isEmpty(account.conditions))
                                    jcondition = new JSONObject(account.conditions);
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }

                            if (scheduled || jcondition.optBoolean("ignore_schedule")) {
                                work = true;

                                List<EntityFolder> folders = db.folder().getSynchronizingFolders(account.id);
                                if (folders.size() > 0)
                                    Collections.sort(folders, folders.get(0).getComparator(ServiceSynchronize.this));
                                for (EntityFolder folder : folders)
                                    if (folder.poll ||
                                            !account.poll_exempted ||
                                            account.protocol == EntityAccount.TYPE_POP ||
                                            !BuildConfig.DEBUG)
                                        EntityOperation.poll(ServiceSynchronize.this, folder.id);
                            }
                        }

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    schedule(ServiceSynchronize.this, work, true, null);

                    // Prevent service stop
                    eval(ServiceSynchronize.this, "poll");
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private void onAlarm(Intent intent) {
        schedule(this, true);

        Bundle command = new Bundle();
        command.putString("name", "reload"); // eval will not work if manual sync running
        command.putBoolean("sync", true);
        liveAccountNetworkState.post(command);
    }

    private void onWatchdog(Intent intent) {
        EntityLog.log(this, EntityLog.Type.Scheduling, "Watchdog");
        schedule(this, false);

        if (lastNetworkState == null || !lastNetworkState.isSuitable())
            updateNetworkState(null, "watchdog");

        onEval(intent);

        ServiceSend.boot(this);

        scheduleWatchdog(this);
    }

    private Notification getNotificationService(Integer accounts, Integer operations) {
        if (accounts != null)
            this.lastAccounts = accounts;
        if (operations != null)
            this.lastOperations = operations;

        // Build pending intent
        Intent why = new Intent(this, ActivityView.class);
        why.setAction("why");
        why.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piWhy = PendingIntentCompat.getActivity(
                this, ActivityView.PI_WHY, why, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "service")
                        .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFAULT)
                        .setSmallIcon(R.drawable.baseline_compare_arrows_white_24)
                        .setContentIntent(piWhy)
                        .setAutoCancel(false)
                        .setShowWhen(false)
                        .setDefaults(0) // disable sound on pre Android 8
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                        .setLocalOnly(true)
                        .setOngoing(true);

        if (lastAccounts > 0)
            builder.setContentTitle(getResources().getQuantityString(
                    R.plurals.title_notification_synchronizing, lastAccounts, lastAccounts));
        else
            builder.setContentTitle(getString(R.string.title_check_operations));

        if (lastOperations > 0)
            builder.setContentText(getResources().getQuantityString(
                    R.plurals.title_notification_operations, lastOperations, lastOperations));

        if (lastSuitable == null || !lastSuitable)
            builder.setSubText(getString(R.string.title_notification_waiting));

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            // Android O+: this will always immediately be followed by a call to onCapabilitiesChanged/onLinkPropertiesChanged
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
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
        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Network,
                    "Network " + network + " blocked=" + blocked);
        }

        @Override
        public void onLost(@NonNull Network network) {
            updateNetworkState(network, "lost");
        }
    };

    private final BroadcastReceiver connectionChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Received intent=" + intent +
                    " " + TextUtils.join(" ", Log.getExtras(intent.getExtras())));

            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                boolean on = intent.getBooleanExtra("state", false);
                EntityLog.log(context, EntityLog.Type.Network,
                        "Airplane mode on=" + on);
                if (!on)
                    lastLost = 0;
            }

            updateNetworkState(null, "connectivity");
        }
    };

    private final BroadcastReceiver idleModeChangedReceiver = new BroadcastReceiver() {
        @Override
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onReceive(Context context, Intent intent) {
            PowerManager pm = Helper.getSystemService(context, PowerManager.class);
            EntityLog.log(context, "Doze mode=" + pm.isDeviceIdleMode() +
                    " ignoring=" + pm.isIgnoringBatteryOptimizations(context.getPackageName()));
        }
    };

    private final BroadcastReceiver dataSaverChanged = new BroadcastReceiver() {
        @Override
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void onReceive(Context context, Intent intent) {
            Log.i("Received intent=" + intent +
                    " " + TextUtils.join(" ", Log.getExtras(intent.getExtras())));

            ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
            Integer status = (cm == null ? null : cm.getRestrictBackgroundStatus());
            EntityLog.log(context, "Data saver=" + status);

            updateNetworkState(null, "datasaver");
        }
    };

    private final BroadcastReceiver suspendChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EntityLog.log(context, intent.getAction() + " " +
                    TextUtils.join(", ", Log.getExtras(intent.getExtras())));
        }
    };

    private final BroadcastReceiver batteryChanged = new BroadcastReceiver() {
        private Integer lastLevel = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (!Objects.equals(level, lastLevel)) {
                lastLevel = level;
                EntityLog.log(context, intent.getAction() + " " +
                        TextUtils.join(", ", Log.getExtras(intent.getExtras())));
            }
        }
    };

    private void updateNetworkState(final Network network, final String reason) {
        getMainHandler().post(new RunnableEx("network") {
            @Override
            public void delegate() {
                try {
                    Network active = ConnectionHelper.getActiveNetwork(ServiceSynchronize.this);

                    if (active != null && !active.equals(lastActive)) {
                        if (ConnectionHelper.isConnected(ServiceSynchronize.this, active)) {
                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Network,
                                    reason + ": new active network=" + active + "/" + lastActive);
                            lastActive = active;
                        }
                    } else if (lastActive != null) {
                        if (!ConnectionHelper.isConnected(ServiceSynchronize.this, lastActive)) {
                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Network,
                                    reason + ": lost active network=" + lastActive);
                            lastActive = null;
                            lastLost = new Date().getTime();
                        }
                    }

                    if (network == null || Objects.equals(network, active)) {
                        ConnectionHelper.NetworkState ns = ConnectionHelper.getNetworkState(ServiceSynchronize.this);
                        if (!Objects.equals(lastNetworkState, ns)) {
                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Network,
                                    reason + ": updating state network=" + active +
                                            " info=" + ConnectionHelper.getNetworkInfo(ServiceSynchronize.this, active) + " " + ns);
                            lastNetworkState = ns;
                            liveNetworkState.postValue(ns);
                        }
                    }

                    boolean isSuitable = (lastNetworkState != null && lastNetworkState.isSuitable());
                    if (lastSuitable == null || lastSuitable != isSuitable) {
                        lastSuitable = isSuitable;
                        EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Network,
                                reason + ": updated suitable=" + lastSuitable);

                        if (!isBackgroundService(ServiceSynchronize.this))
                            try {
                                NotificationManager nm =
                                        Helper.getSystemService(ServiceSynchronize.this, NotificationManager.class);
                                if (NotificationHelper.areNotificationsEnabled(nm))
                                    nm.notify(NotificationHelper.NOTIFICATION_SYNCHRONIZE,
                                            getNotificationService(lastAccounts, lastOperations));
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private class MediatorState extends MediatorLiveData<List<TupleAccountNetworkState>> {
        private boolean running = true;
        private Bundle lastCommand = null;
        private ConnectionHelper.NetworkState lastNetworkState = null;
        private List<TupleAccountState> lastAccountStates = null;

        private void post(Bundle command) {
            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling,
                    "### command " + TextUtils.join(" ", Log.getExtras(command)));

            if (command.getBoolean("sync") || command.getBoolean("force"))
                lastNetworkState = ConnectionHelper.getNetworkState(ServiceSynchronize.this);

            post(command, lastNetworkState, lastAccountStates);
        }

        private void post(ConnectionHelper.NetworkState networkState) {
            lastNetworkState = networkState;
            post(lastCommand, lastNetworkState, lastAccountStates);
        }

        private void post(List<TupleAccountState> accountStates) {
            lastAccountStates = accountStates;
            post(lastCommand, lastNetworkState, lastAccountStates);
        }

        private void postDestroy() {
            if (running) {
                running = false;
                postValue(null);
            }
        }

        private void post(Bundle command, ConnectionHelper.NetworkState networkState, List<TupleAccountState> accountStates) {
            try {
                if (!running) {
                    Log.i("### not running");
                    return;
                }

                if (networkState == null)
                    networkState = ConnectionHelper.getNetworkState(ServiceSynchronize.this);

                if (accountStates == null) {
                    EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Scheduling, "### no accounts");
                    lastCommand = command;
                    return;
                }

                lastCommand = null;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServiceSynchronize.this);
                boolean enabled = prefs.getBoolean("enabled", true);
                int pollInterval = getPollInterval(ServiceSynchronize.this);

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
                            enabled && (pollInterval == 0 || accountState.isExempted(ServiceSynchronize.this)),
                            scheduled,
                            command,
                            networkState,
                            accountState));

                postValue(result);
            } catch (Throwable ex) {
                Log.e(ex);
                /*
                    java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String android.content.Context.getPackageName()' on a null object reference
                            at androidx.preference.PreferenceManager.getDefaultSharedPreferencesName(PreferenceManager:124)
                            at androidx.preference.PreferenceManager.getDefaultSharedPreferences(PreferenceManager:119)
                            at eu.faircode.email.ServiceSynchronize$MediatorState.post(ServiceSynchronize:2596)
                            at eu.faircode.email.ServiceSynchronize$MediatorState.post(ServiceSynchronize:2569)
                            at eu.faircode.email.ServiceSynchronize$MediatorState.access$400(ServiceSynchronize:2546)
                            at eu.faircode.email.ServiceSynchronize$3.onChanged(ServiceSynchronize:219)
                            at eu.faircode.email.ServiceSynchronize$3.onChanged(ServiceSynchronize:216)
                            at androidx.lifecycle.MediatorLiveData$Source.onChanged(MediatorLiveData:152)
                            at androidx.lifecycle.LiveData.considerNotify(LiveData:133)
                            at androidx.lifecycle.LiveData.dispatchingValue(LiveData:151)
                            at androidx.lifecycle.LiveData.setValue(LiveData:309)
                            at androidx.lifecycle.MutableLiveData.setValue(MutableLiveData:50)
                            at androidx.lifecycle.LiveData$1.run(LiveData:93)
                            at android.os.Handler.handleCallback(Handler.java:761)
                 */
            }
        }
    }

    static void boot(final Context context) {
        Helper.getSerialExecutor().submit(new RunnableEx("boot") {
            @Override
            public void delegate() {
                try {
                    EntityLog.log(context, "Boot sync service");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        // Restore notifications
                        db.message().clearNotifyingMessages();

                        // Restore snooze timers
                        for (EntityMessage message : db.message().getSnoozed(null))
                            EntityMessage.snooze(context, message.id, message.ui_snoozed);

                        db.setTransactionSuccessful();
                    } catch (IllegalArgumentException ex) {
                        Log.w(ex);
                    } finally {
                        db.endTransaction();
                    }

                    // Restore schedule
                    schedule(context, false);

                    // Init service
                    eval(context, "boot");
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private static void schedule(Context context, boolean polled) {
        Intent intent = new Intent(context, ServiceSynchronize.class);
        intent.setAction("alarm");
        PendingIntent pi = PendingIntentCompat.getForegroundService(
                context, PI_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        long now = new Date().getTime();
        long[] schedule = getSchedule(context);
        boolean scheduled = (schedule == null || (now >= schedule[0] && now < schedule[1]));

        if (schedule != null) {
            long next = (now < schedule[0] ? schedule[0] : schedule[1]);

            Log.i("Schedule now=" + new Date(now));
            Log.i("Schedule start=" + new Date(schedule[0]));
            Log.i("Schedule end=" + new Date(schedule[1]));
            Log.i("Schedule next=" + new Date(next));
            Log.i("Schedule scheduled=" + scheduled);

            AlarmManagerCompatEx.setAndAllowWhileIdle(context, am, AlarmManager.RTC_WAKEUP, next, pi);
        }

        Helper.getSerialExecutor().submit(new RunnableEx("schedule") {
            @Override
            protected void delegate() {
                boolean work = false;
                DB db = DB.getInstance(context);
                List<EntityAccount> accounts = db.account().getPollAccounts(null);
                for (EntityAccount account : accounts) {
                    JSONObject jcondition = new JSONObject();
                    try {
                        if (!TextUtils.isEmpty(account.conditions))
                            jcondition = new JSONObject(account.conditions);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                    if (scheduled || jcondition.optBoolean("ignore_schedule")) {
                        work = true;
                        break;
                    }
                }

                Long at = null;
                if (scheduled && polled) {
                    at = now + 30 * 1000L;
                    Log.i("Sync at schedule start=" + new Date(at));
                }

                schedule(context, work, polled, at);
            }
        });
    }

    private static void schedule(Context context, boolean scheduled, boolean polled, Long at) {
        Intent intent = new Intent(context, ServiceSynchronize.class);
        intent.setAction("poll");
        PendingIntent piSync = PendingIntentCompat.getForegroundService(
                context, PI_POLL, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(piSync);

        if (at == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean enabled = prefs.getBoolean("enabled", true);
            int pollInterval = getPollInterval(context);
            if (scheduled && enabled && pollInterval > 0) {
                long now = new Date().getTime();
                long interval = pollInterval * 60 * 1000L;
                long next = now - now % interval + 30 * 1000L;
                if (polled || next < now)
                    next += interval;
                if (polled && next < now + interval / 5)
                    next += interval;

                EntityLog.log(context, EntityLog.Type.Scheduling,
                        "Poll next=" + new Date(next) + " polled=" + polled);

                AlarmManagerCompatEx.setAndAllowWhileIdle(context, am, AlarmManager.RTC_WAKEUP, next, piSync);
            }
        } else
            AlarmManagerCompatEx.setAndAllowWhileIdle(context, am, AlarmManager.RTC_WAKEUP, at, piSync);
    }

    long getLastLost() {
        return lastLost;
    }

    static int getPollInterval(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("poll_interval", 0); // minutes
    }

    static long[] getSchedule(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        boolean schedule = prefs.getBoolean("schedule", false);

        if (!enabled || !schedule)
            return null;

        if (!ActivityBilling.isPro(context))
            return null;

        Calendar calStart = Calendar.getInstance();
        boolean weekend = CalendarHelper.isWeekend(context, calStart);
        int defStart = (weekend ? prefs.getInt("schedule_start", 0) : 0);
        int defEnd = (weekend ? prefs.getInt("schedule_end", 0) : 0);

        int minuteStart = prefs.getInt("schedule_start" + (weekend ? "_weekend" : ""), defStart);
        int minuteEnd = prefs.getInt("schedule_end" + (weekend ? "_weekend" : ""), defEnd);

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

            if (BuildConfig.DEBUG && false)
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

                if (BuildConfig.DEBUG)
                    Log.i("@@@ eval dow=" + sdow + "/" + edow +
                            " on=" + son + "/" + eon +
                            " start=" + new Date(calStart.getTimeInMillis()) +
                            " end=" + new Date(calEnd.getTimeInMillis()));

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

    static void scheduleWatchdog(Context context) {
        try {
            Intent intent = new Intent(context, ServiceSynchronize.class)
                    .setAction("watchdog");
            PendingIntent pi;
            if (isBackgroundService(context))
                pi = PendingIntentCompat.getService(context, PI_WATCHDOG, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            else {
                // Workaround for Xiaomi Android 11
                pi = PendingIntentCompat.getForegroundService(context, PI_WATCHDOG, intent, PendingIntent.FLAG_NO_CREATE);
                if (pi == null)
                    pi = PendingIntentCompat.getForegroundService(context, PI_WATCHDOG, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean watchdog = prefs.getBoolean("watchdog", true);
            boolean enabled = prefs.getBoolean("enabled", true);
            if (watchdog && enabled) {
                long now = new Date().getTime();
                long next = now - now % WATCHDOG_INTERVAL + WATCHDOG_INTERVAL + WATCHDOG_INTERVAL / 4;
                if (next < now + WATCHDOG_INTERVAL / 5)
                    next += WATCHDOG_INTERVAL;
                EntityLog.log(context, "Watchdog next=" + new Date(next));
                AlarmManagerCompatEx.setAndAllowWhileIdle(context, am, AlarmManager.RTC_WAKEUP, next, pi);
            }
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                Redmi Note 8 Pro Android 11 (SDK 30)

                java.lang.RuntimeException:
                  at android.app.ActivityThread.handleBindApplication (ActivityThread.java:7019)
                  at android.app.ActivityThread.access$1600 (ActivityThread.java:263)
                  at android.app.ActivityThread$H.handleMessage (ActivityThread.java:2034)
                  at android.os.Handler.dispatchMessage (Handler.java:106)
                  at android.os.Looper.loop (Looper.java:236)
                  at android.app.ActivityThread.main (ActivityThread.java:8057)
                  at java.lang.reflect.Method.invoke (Native Method)
                  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run (RuntimeInit.java:620)
                  at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:1011)
                Caused by: java.lang.SecurityException:
                  at android.os.Parcel.createExceptionOrNull (Parcel.java:2376)
                  at android.os.Parcel.createException (Parcel.java:2360)
                  at android.os.Parcel.readException (Parcel.java:2343)
                  at android.os.Parcel.readException (Parcel.java:2285)
                  at android.app.IActivityManager$Stub$Proxy.getIntentSenderWithFeature (IActivityManager.java:6884)
                  at android.app.PendingIntent.buildServicePendingIntent (PendingIntent.java:657)
                  at android.app.PendingIntent.getForegroundService (PendingIntent.java:645)
                  at eu.faircode.email.PendingIntentCompat.getForegroundService (PendingIntentCompat.java:51)
                  at eu.faircode.email.ServiceSynchronize.scheduleWatchdog (ServiceSynchronize.java:2972)
                  at eu.faircode.email.ApplicationEx.onCreate (ApplicationEx.java:229)
             */
        }
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
        start(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("watchdog"));
    }

    static void stop(Context context) {
        context.stopService(new Intent(context, ServiceSynchronize.class));
    }

    static void restart(Context context) {
        stop(context);
        eval(context, "restart");
    }

    private static void start(Context context, Intent intent) {
        try {
            if (isBackgroundService(context))
                context.startService(intent);
            else
                ContextCompat.startForegroundService(context, intent);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static boolean isBackgroundService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("background_service", false);
    }
}
