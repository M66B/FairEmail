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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteFullException;
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

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

import net.openid.appauth.AuthState;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Quota;
import javax.mail.ReadOnlyFolderException;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.event.FolderAdapter;
import javax.mail.event.FolderEvent;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;

import me.leolin.shortcutbadger.ShortcutBadgerAlt;

public class ServiceSynchronize extends ServiceBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Network lastActive = null;
    private Boolean lastSuitable = null;
    private long lastAcquired = 0;
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

    static final int DEFAULT_BACKOFF_POWER = 3; // 2^3=8 seconds (totally 8+2x20=48 seconds)

    private static final long MSG_START_DELAY = 15 * 1000L; // milliseconds
    private static final long MSG_STOP_DELAY = 1000L + 500L; // milliseconds
    private static final long BACKUP_DELAY = 30 * 1000L; // milliseconds
    private static final long PURGE_DELAY = 30 * 1000L; // milliseconds
    private static final int QUIT_DELAY = 10; // seconds
    private static final long STILL_THERE_THRESHOLD = 3 * 60 * 1000L; // milliseconds
    private static final int TUNE_KEEP_ALIVE_INTERVAL_MIN = 9; // minutes
    private static final int TUNE_KEEP_ALIVE_INTERVAL_STEP = 2; // minutes
    private static final int OPTIMIZE_POLL_INTERVAL = 15; // minutes
    private static final int CONNECT_BACKOFF_START = 8; // seconds
    private static final int CONNECT_BACKOFF_INTERMEDIATE = 5; // minutes
    private static final int CONNECT_BACKOFF_ALARM_START = 15; // minutes
    private static final int CONNECT_BACKOFF_ALARM_MAX = 60; // minutes
    private static final long CONNECT_BACKOFF_GRACE = 2 * 60 * 1000L; // milliseconds
    private static final long LOST_RECENTLY = 150 * 1000L; // milliseconds
    private static final int ACCOUNT_ERROR_AFTER = 90; // minutes
    private static final int ACCOUNT_ERROR_AFTER_POLL = 4; // times
    private static final int FAST_FAIL_THRESHOLD = 75; // percent
    private static final int FAST_FAIL_COUNT = 3;
    private static final int FETCH_YIELD_DURATION = 50; // milliseconds
    private static final long WATCHDOG_INTERVAL = 60 * 60 * 1000L; // milliseconds
    private static final long MAX_QUOTA = 1000 * 1000 * 1000L; // KB

    private static final String ACTION_NEW_MESSAGE_COUNT = BuildConfig.APPLICATION_ID + ".NEW_MESSAGE_COUNT";

    private static final List<String> PREF_EVAL = Collections.unmodifiableList(Arrays.asList(
            "enabled", "poll_interval", "poll_metered", "poll_unmetered", "last_daily" // restart account(s)
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
            "prefer_ip4", "prefer_ip6", "bind_socket", "standalone_vpn", // force reconnect
            "dns_extra", "dns_custom", // force reconnect
            "tcp_keep_alive", // force reconnect
            "ssl_harden", "ssl_harden_strict", "cert_strict", "cert_transparency", "check_names", "bouncy_castle", "bc_fips", // force reconnect
            "experiments", "debug", "protocol", // force reconnect
            //"restart_interval", // force reconnect
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
            try {
                startForeground(NotificationHelper.NOTIFICATION_SYNCHRONIZE,
                        getNotificationService(null, null));
                EntityLog.log(this, EntityLog.Type.Debug3,
                        "onCreate class=" + this.getClass().getName());
            } catch (Throwable ex) {
                if (Helper.isPlayStoreInstall())
                    Log.i(ex);
                else
                    Log.e(ex);
            }

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

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        //    IntentFilter suspend = new IntentFilter();
        //    suspend.addAction(Intent.ACTION_MY_PACKAGE_SUSPENDED);
        //    suspend.addAction(Intent.ACTION_MY_PACKAGE_UNSUSPENDED);
        //    ContextCompat.registerReceiver(this,
        //            suspendChanged,
        //            suspend,
        //            ContextCompat.RECEIVER_NOT_EXPORTED);
        //}

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
            private final Map<String, Semaphore> startSerializer = new HashMap<>();
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
                            int start_delay = prefs.getInt("start_delay", 0);
                            if (start_delay > 0) {
                                Semaphore sem;
                                synchronized (startSerializer) {
                                    if (!startSerializer.containsKey(accountNetworkState.accountState.host))
                                        startSerializer.put(accountNetworkState.accountState.host, new Semaphore(1));
                                    sem = startSerializer.get(accountNetworkState.accountState.host);
                                }
                                sem.acquire();
                                getMainHandler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        sem.release();
                                    }
                                }, start_delay * 1000L);
                            }
                            monitorAccount(accountNetworkState.accountState, astate, sync, force);
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
                            crumb.put("lastAcquired", new Date(lastAcquired).toString());
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
                            crumb.put("lastAcquired", new Date(lastAcquired).toString());
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

        final Runnable updateStop = new Runnable() {
            @Override
            public void run() {
                Log.i("Stop new messages");
                cowner.stop();
            }
        };

        final Runnable updateNew = new Runnable() {
            @Override
            public void run() {
                Log.i("Start new messages");
                cowner.restart();
                getMainHandler().postDelayed(updateStop, MSG_STOP_DELAY);
                getMainHandler().postDelayed(this, MSG_START_DELAY);
            }
        };

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

                if (syncing == 0) {
                    getMainHandler().removeCallbacks(updateNew);
                    getMainHandler().removeCallbacks(updateStop);
                    cowner.start();
                } else {
                    cowner.stop();
                    if (!getMainHandler().hasCallbacks(updateNew))
                        getMainHandler().postDelayed(updateNew, MSG_START_DELAY);
                }

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
                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Debug1, "Owner state=" + state);
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
                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Debug1,
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
                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Debug1,
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
        EntityLog.log(this, EntityLog.Type.Debug3,
                "Service destroy class=" + this.getClass().getName());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        unregisterReceiver(batteryChanged);

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        //    unregisterReceiver(suspendChanged);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            unregisterReceiver(dataSaverChanged);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            unregisterReceiver(idleModeChangedReceiver);

        unregisterReceiver(connectionChangedReceiver);

        ConnectivityManager cm = Helper.getSystemService(this, ConnectivityManager.class);
        cm.unregisterNetworkCallback(networkCallback);

        liveAccountNetworkState.postDestroy();

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
                try {
                    startForeground(NotificationHelper.NOTIFICATION_SYNCHRONIZE,
                            getNotificationService(null, null));
                    String msg = "onStartCommand" +
                            " class=" + this.getClass().getName() +
                            " action=" + action;
                    EntityLog.log(this, EntityLog.Type.Debug2, msg);
                } catch (Throwable ex) {
                    if (Helper.isPlayStoreInstall())
                        Log.i(ex);
                    else
                        Log.e(ex);
                }

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

    @Override
    public void onTimeout(int startId) {
        String msg = "onTimeout" +
                " class=" + this.getClass().getName() +
                " ignoring=" + Helper.isIgnoringOptimizations(this);
        Log.e(new Throwable(msg));
        EntityLog.log(this, EntityLog.Type.Debug3, msg);
        stopSelf();
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

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServiceSynchronize.this);
                            boolean threading = prefs.getBoolean("threading", true);
                            boolean flag_unsnoozed = prefs.getBoolean("flag_unsnoozed", false);
                            boolean important_unsnoozed = prefs.getBoolean("important_unsnoozed", false);

                            // Show thread
                            List<EntityMessage> messages = db.message().getMessagesByThread(
                                    message.account, message.thread, threading ? null : message.id, null);
                            for (EntityMessage threaded : messages)
                                db.message().setMessageSnoozed(threaded.id, null);

                            db.message().setMessageUnsnoozed(message.id, true);
                            if (flag_unsnoozed)
                                EntityOperation.queue(ServiceSynchronize.this, message, EntityOperation.FLAG, false);
                            if (important_unsnoozed) {
                                db.message().setMessageImportance(message.id, EntityMessage.PRIORITIY_HIGH);
                                EntityOperation.queue(ServiceSynchronize.this, message, EntityOperation.KEYWORD,
                                        MessageHelper.FLAG_HIGH_IMPORTANCE, true);

                            }
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
                                            !account.isExempted(ServiceSynchronize.this) ||
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

        ServiceSend.watchdog(this);

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

    private NotificationCompat.Builder getNotificationAlert(EntityAccount account, String message) {
        String title = getString(R.string.title_notification_alert, account.name);

        // Build pending intent
        Intent intent = new Intent(this, ActivityError.class);
        intent.setAction("alert:" + account.id);
        intent.putExtra("type", "alert");
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("provider", account.provider);
        intent.putExtra("account", account.id);
        intent.putExtra("protocol", account.protocol);
        intent.putExtra("auth_type", account.auth_type);
        intent.putExtra("host", account.host);
        intent.putExtra("address", account.user);
        intent.putExtra("faq", 23);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piAlert = PendingIntentCompat.getActivity(
                this, ActivityError.PI_ALERT, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "alerts")
                        .setSmallIcon(R.drawable.baseline_warning_white_24)
                        .setContentTitle(title)
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
        final PowerManager pm = Helper.getSystemService(this, PowerManager.class);
        final PowerManager.WakeLock wlAccount = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id);
        final PowerManager.WakeLock wlFolder = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id + ".folder");
        final PowerManager.WakeLock wlMessage = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id + ".message");

        long start = new Date().getTime();
        try {
            wlAccount.acquire(Helper.WAKELOCK_MAX);

            boolean forced = false;
            final DB db = DB.getInstance(this);

            Long currentThread = Thread.currentThread().getId();
            Long accountThread = currentThread;
            db.account().setAccountThread(account.id, accountThread);

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
            if (account.backoff_until != null)
                db.account().setAccountBackoff(account.id, null);
            while (state.isRunning() && currentThread.equals(accountThread)) {
                state.reset();
                Log.i(account.name + " run thread=" + currentThread);

                final ObjectHolder<TwoStateOwner> cowner = new ObjectHolder<>();
                final ExecutorService executor = Helper.getBackgroundExecutor(1, "operation." + account.id);

                // Debug
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean subscriptions = prefs.getBoolean("subscriptions", false);
                boolean keep_alive_poll = prefs.getBoolean("keep_alive_poll", false);
                boolean empty_pool = prefs.getBoolean("empty_pool", true);
                boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.DEBUG);

                final EmailService iservice = new EmailService(this, account, EmailService.PURPOSE_USE, debug);
                iservice.setPartialFetch(account.partial_fetch);
                iservice.setRawFetch(account.raw_fetch);
                iservice.setIgnoreBodyStructureSize(account.ignore_size);
                if (account.protocol != EntityAccount.TYPE_IMAP)
                    iservice.setLeaveOnServer(account.leave_on_server);

                if (account.keep_alive_noop) {
                    int timeout = prefs.getInt("timeout", EmailService.DEFAULT_CONNECT_TIMEOUT);
                    int restart_interval = prefs.getInt("restart_interval", EmailService.DEFAULT_RESTART_INTERVAL);
                    int factor = (timeout == 0 ? 0 : restart_interval / timeout);
                    int idle_interval = timeout * factor;
                    Log.i("Restart interval=" + restart_interval + " timeout=" + timeout + " factor=" + factor + " idle=" + idle_interval);
                    iservice.setRestartIdleInterval(idle_interval);
                }

                final Date lastStillHere = new Date(0);

                iservice.setListener(new StoreListener() {
                    @Override
                    public void notification(StoreEvent e) {
                        String message = e.getMessage();
                        if (TextUtils.isEmpty(message))
                            message = "?";
                        if (e.getMessageType() == StoreEvent.NOTICE) {
                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Account, account,
                                    account.name + " notice: " + message);

                            if ("Still here".equals(message) &&
                                    !account.isTransient(ServiceSynchronize.this)) {
                                long now = new Date().getTime();
                                long last = lastStillHere.getTime();
                                if (last > 0) {
                                    long elapsed = now - last;
                                    if (elapsed < STILL_THERE_THRESHOLD)
                                        optimizeAccount(account, "'" + message + "'" +
                                                " elapsed=" + elapsed + " ms");
                                }
                                lastStillHere.setTime(now);
                            }
                        } else {
                            long start = new Date().getTime();
                            try {
                                wlFolder.acquire(Helper.WAKELOCK_MAX);

                                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Account, account,
                                        account.name + " alert: " + message);

                                if (!ConnectionHelper.isMaxConnections(message))
                                    try {
                                        NotificationManager nm =
                                                Helper.getSystemService(ServiceSynchronize.this, NotificationManager.class);
                                        if (NotificationHelper.areNotificationsEnabled(nm))
                                            nm.notify("alert:" + account.id,
                                                    NotificationHelper.NOTIFICATION_TAGGED,
                                                    getNotificationAlert(account, message).build());
                                    } catch (Throwable ex) {
                                        Log.w(ex);
                                    }
                            } finally {
                                if (wlFolder.isHeld())
                                    wlFolder.release();
                                else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e("folder notice released elapse=" + (new Date().getTime() - start));
                            }
                        }
                    }
                });

                final Runnable purge = new RunnableEx("purge") {
                    @Override
                    public void delegate() {
                        executor.submit(new RunnableEx("purge#exec") {
                            @Override
                            public void delegate() {
                                long start = new Date().getTime();
                                try {
                                    wlAccount.acquire(Helper.WAKELOCK_MAX);

                                    // Close cached connections
                                    Log.i(account.name + " Empty connection pool");
                                    ((IMAPStore) iservice.getStore()).emptyConnectionPool(false);
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                } finally {
                                    if (wlAccount.isHeld())
                                        wlAccount.release();
                                    else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                        Log.e("purge released elapse=" + (new Date().getTime() - start));
                                }
                            }
                        });
                    }
                };

                final long group = Thread.currentThread().getId();
                final Map<EntityFolder, IMAPFolder> mapFolders = new LinkedHashMap<>();
                List<Thread> idlers = new ArrayList<>();
                try {
                    // Initiate connection
                    EntityLog.log(this, EntityLog.Type.Account, account,
                            account.name + " connecting");
                    db.folder().setFolderStates(account.id, null);
                    db.account().setAccountState(account.id, "connecting");

                    try {
                        iservice.connect(account);
                        lastStillHere.setTime(0);
                    } catch (Throwable ex) {
                        // Immediately report auth errors
                        if (ex instanceof AuthenticationFailedException) {
                            if (ConnectionHelper.isIoError(ex)) {
                                if (!BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e(ex);
                            } else {
                                Log.e(ex);

                                // Allow Android account manager to refresh the access token
                                if (account.auth_type != AUTH_TYPE_PASSWORD &&
                                        state.getBackoff() <= CONNECT_BACKOFF_ALARM_START * 60)
                                    throw ex;

                                try {
                                    state.setBackoff(2 * CONNECT_BACKOFF_ALARM_MAX * 60);
                                    NotificationManager nm =
                                            Helper.getSystemService(this, NotificationManager.class);
                                    if (NotificationHelper.areNotificationsEnabled(nm))
                                        nm.notify("receive:" + account.id,
                                                NotificationHelper.NOTIFICATION_TAGGED,
                                                Core.getNotificationError(this, "error", account, null, null, ex)
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
                    final boolean capIdle =
                            iservice.hasCapability("IDLE");
                    final boolean capUtf8 =
                            iservice.hasCapability("UTF8=ACCEPT") ||
                                    iservice.hasCapability("UTF8=ONLY");
                    final boolean capNotify = iservice.hasCapability("NOTIFY");

                    String capabilities = TextUtils.join(" ", iservice.getCapabilities());
                    EntityLog.log(this, EntityLog.Type.Protocol, account, capabilities);
                    if (capabilities.length() > 500)
                        capabilities = capabilities.substring(0, 500) + "...";

                    Log.i(account.name + " idle=" + capIdle);
                    if (!capIdle || account.poll_interval < TUNE_KEEP_ALIVE_INTERVAL_MIN)
                        optimizeAccount(account, "IDLE");

                    db.account().setAccountState(account.id, "connected");
                    db.account().setAccountCapabilities(account.id, capabilities, capIdle, capUtf8);
                    db.account().setAccountError(account.id, null);
                    db.account().setAccountWarning(account.id, null);

                    Store istore = iservice.getStore();
                    if (istore instanceof IMAPStore) {
                        Map<String, String> caps = ((IMAPStore) istore).getCapabilities();
                        EntityLog.log(this, EntityLog.Type.Account, account,
                                account.name + " connected" +
                                        " caps=" + (caps == null ? null : TextUtils.join(" ", caps.keySet())));
                    } else
                        EntityLog.log(this, EntityLog.Type.Account, account,
                                account.name + " connected");

                    db.account().setAccountMaxSize(account.id, iservice.getMaxSize());
                    if (istore instanceof IMAPStore)
                        updateQuota(this, ((IMAPStore) iservice.getStore()), account);

                    // Listen for folder events
                    iservice.getStore().addFolderListener(new FolderAdapter() {
                        @Override
                        public void folderCreated(FolderEvent e) {
                            long start = new Date().getTime();
                            try {
                                wlFolder.acquire(Helper.WAKELOCK_MAX);

                                String name = e.getFolder().getFullName();
                                Log.i("Folder created=" + name);
                                if (db.folder().getFolderByName(account.id, name) == null)
                                    reload(ServiceSynchronize.this, account.id, false, "folder created");
                            } finally {
                                if (wlFolder.isHeld())
                                    wlFolder.release();
                                else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e("folder created released elapse=" + (new Date().getTime() - start));
                            }
                        }

                        @Override
                        public void folderRenamed(FolderEvent e) {
                            long start = new Date().getTime();
                            try {
                                wlFolder.acquire(Helper.WAKELOCK_MAX);

                                String old = e.getFolder().getFullName();
                                String name = e.getNewFolder().getFullName();
                                Log.i("Folder renamed from=" + old + " to=" + name);

                                int count = db.folder().renameFolder(account.id, old, name);
                                Log.i("Renamed to " + name + " count=" + count);
                                if (count != 0)
                                    reload(ServiceSynchronize.this, account.id, false, "folder renamed");
                            } finally {
                                if (wlFolder.isHeld())
                                    wlFolder.release();
                                else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e("folder renamed released elapse=" + (new Date().getTime() - start));
                            }
                        }

                        @Override
                        public void folderDeleted(FolderEvent e) {
                            long start = new Date().getTime();
                            try {
                                wlFolder.acquire(Helper.WAKELOCK_MAX);

                                String name = e.getFolder().getFullName();
                                Log.i("Folder deleted=" + name);
                                if (db.folder().getFolderByName(account.id, name) != null)
                                    reload(ServiceSynchronize.this, account.id, false, "folder deleted");
                            } finally {
                                if (wlFolder.isHeld())
                                    wlFolder.release();
                                else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e("folder deleted released elapse=" + (new Date().getTime() - start));
                            }
                        }

                        @Override
                        public void folderChanged(FolderEvent e) {
                            long start = new Date().getTime();
                            try {
                                wlFolder.acquire(Helper.WAKELOCK_MAX);

                                String name = e.getFolder().getFullName();
                                EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Account, account,
                                        "Folder changed=" + name);
                                EntityFolder folder = db.folder().getFolderByName(account.id, name);
                                if (folder != null && folder.selectable)
                                    EntityOperation.sync(ServiceSynchronize.this, folder.id, false);
                            } finally {
                                if (wlFolder.isHeld())
                                    wlFolder.release();
                                else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e("folder changed released elapse=" + (new Date().getTime() - start));
                            }
                        }
                    });

                    // Update folder list
                    Core.onSynchronizeFolders(this,
                            account, iservice.getStore(), state,
                            false, force && !forced);

                    // Open synchronizing folders
                    List<EntityFolder> folders = db.folder().getFolders(account.id, false, true);
                    if (folders.size() > 0)
                        Collections.sort(folders, folders.get(0).getComparator(this));

                    for (final EntityFolder folder : folders) {
                        if (folder.selectable && folder.synchronize && !folder.poll && capIdle && sync) {
                            Log.i(account.name + " sync folder " + folder.name);

                            db.folder().setFolderState(folder.id, "connecting");

                            final IMAPFolder ifolder = (IMAPFolder) iservice.getStore().getFolder(folder.name);
                            mapFolders.put(folder, ifolder);
                            try {
                                if (BuildConfig.DEBUG && "Postausgang".equals(folder.name))
                                    throw new ReadOnlyFolderException(ifolder);
                                ifolder.open(Folder.READ_WRITE);
                                folder.read_only = ifolder.getUIDNotSticky();
                                db.folder().setFolderReadOnly(folder.id, folder.read_only);
                            } catch (ReadOnlyFolderException ex) {
                                Log.w(folder.name + " read only");
                                try {
                                    ifolder.open(Folder.READ_ONLY);
                                    folder.read_only = true;
                                    db.folder().setFolderReadOnly(folder.id, folder.read_only);
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
                                /*
                                    Search javax.mail.MessagingException: DAE2 NO [CANNOT] Invalid mailbox name: Name must not have '/' characters (0.000 + 0.000 secs).;
                                      nested exception is:
                                        com.sun.mail.iap.CommandFailedException: DAE2 NO [CANNOT] Invalid mailbox name: Name must not have '/' characters (0.000 + 0.000 secs).
                                    javax.mail.MessagingException: DAE2 NO [CANNOT] Invalid mailbox name: Name must not have '/' characters (0.000 + 0.000 secs).;
                                      nested exception is:
                                        com.sun.mail.iap.CommandFailedException: DAE2 NO [CANNOT] Invalid mailbox name: Name must not have '/' characters (0.000 + 0.000 secs).
                                        at com.sun.mail.imap.IMAPFolder.open(SourceFile:61)
                                        at com.sun.mail.imap.IMAPFolder.open(SourceFile:1)
                                        at eu.faircode.email.BoundaryCallbackMessages.load_server(SourceFile:17)
                                        at eu.faircode.email.BoundaryCallbackMessages.access$500(SourceFile:1)
                                        at eu.faircode.email.BoundaryCallbackMessages$3.run(SourceFile:20)
                                        at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:462)
                                        at java.util.concurrent.FutureTask.run(FutureTask.java:266)
                                        at eu.faircode.email.Helper$PriorityFuture.run(SourceFile:1)
                                        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
                                        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
                                        at java.lang.Thread.run(Thread.java:920)
                                    Caused by: com.sun.mail.iap.CommandFailedException: DAE2 NO [CANNOT] Invalid mailbox name: Name must not have '/' characters (0.000 + 0.000 secs).
                                        at com.sun.mail.iap.Protocol.handleResult(SourceFile:8)
                                        at com.sun.mail.imap.protocol.IMAPProtocol.select(SourceFile:19)
                                        at com.sun.mail.imap.IMAPFolder.open(SourceFile:16)
                                        ... 10 more
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
                                    long start = new Date().getTime();
                                    try {
                                        wlMessage.acquire(Helper.WAKELOCK_MAX);
                                        fetch(folder, ifolder, e.getMessages(), false, false, "added");
                                        Thread.sleep(FETCH_YIELD_DURATION);
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Account, folder,
                                                account.name + "/" + folder.name + " added " + Log.formatThrowable(ex, false));
                                        EntityOperation.sync(ServiceSynchronize.this, folder.id, false);
                                    } finally {
                                        if (wlMessage.isHeld())
                                            wlMessage.release();
                                        else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                            Log.e("message added released elapse=" + (new Date().getTime() - start));
                                    }
                                }

                                @Override
                                public void messagesRemoved(MessageCountEvent e) {
                                    long start = new Date().getTime();
                                    try {
                                        wlMessage.acquire(Helper.WAKELOCK_MAX);
                                        fetch(folder, ifolder, e.getMessages(), false, true, "removed");
                                        Thread.sleep(FETCH_YIELD_DURATION);
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Account, folder,
                                                account.name + "/" + folder.name + " removed " + Log.formatThrowable(ex, false));
                                        EntityOperation.sync(ServiceSynchronize.this, folder.id, false);
                                    } finally {
                                        if (wlMessage.isHeld())
                                            wlMessage.release();
                                        else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                            Log.e("message removed released elapse=" + (new Date().getTime() - start));
                                    }
                                }
                            });

                            // Flags (like "seen") at the remote could be changed while synchronizing

                            // Listen for changed messages
                            ifolder.addMessageChangedListener(new MessageChangedListener() {
                                @Override
                                public void messageChanged(MessageChangedEvent e) {
                                    long start = new Date().getTime();
                                    try {
                                        wlMessage.acquire(Helper.WAKELOCK_MAX);
                                        Message imessage = e.getMessage();
                                        fetch(folder, ifolder, new Message[]{imessage}, true, false, "changed");
                                        Thread.sleep(FETCH_YIELD_DURATION);
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Account, folder,
                                                account.name + "/" + folder.name + " changed " + Log.formatThrowable(ex, false));
                                        EntityOperation.sync(ServiceSynchronize.this, folder.id, false);
                                    } finally {
                                        if (wlMessage.isHeld())
                                            wlMessage.release();
                                        else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                            Log.e("message changed released elapse=" + (new Date().getTime() - start));
                                    }
                                }
                            });

                            // Idle folder
                            Thread idler = new Thread(new RunnableEx("idle") {
                                @Override
                                public void delegate() {
                                    try {
                                        Log.i(folder.name + " start idle");
                                        while (ifolder.isOpen() && state.isRunning() && state.isRecoverable()) {
                                            Log.i(folder.name + " do idle");
                                            ifolder.idle(false);
                                            state.activity();
                                        }
                                    } catch (Throwable ex) {
                                        /*
                                            javax.mail.FolderClosedException: * BYE Jakarta Mail Exception: java.net.SocketTimeoutException: Read timed out
                                                at com.sun.mail.imap.IMAPFolder.handleIdle(SourceFile:252)
                                                at com.sun.mail.imap.IMAPFolder.idle(SourceFile:7)
                                                at eu.faircode.email.ServiceSynchronize$21.delegate(SourceFile:78)
                                                at eu.faircode.email.RunnableEx.run(SourceFile:1)
                                                at java.lang.Thread.run(Thread.java:1012)
                                            ... javax.mail.StoreClosedException: NOOP INBOX
                                            javax.mail.StoreClosedException: NOOP INBOX
                                                at eu.faircode.email.ServiceSynchronize.monitorAccount(SourceFile:151)
                                                at eu.faircode.email.ServiceSynchronize.access$1200(Unknown Source:0)
                                                at eu.faircode.email.ServiceSynchronize$4$2.delegate(SourceFile:15)
                                                at eu.faircode.email.RunnableEx.run(SourceFile:1)
                                                at java.lang.Thread.run(Thread.java:1012)
                                         */
                                        Log.e(folder.name, ex);
                                        EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Account, folder,
                                                account.name + "/" + folder.name + " idle " + Log.formatThrowable(ex, false));
                                        state.error(new FolderClosedException(ifolder, "IDLE", new Exception(ex)));
                                    } finally {
                                        Log.i(folder.name + " end idle");
                                    }
                                }
                            }, "idler." + folder.id);
                            idler.setPriority(THREAD_PRIORITY_BACKGROUND);
                            idler.start();
                            idlers.add(idler);

                            EntityOperation.sync(this, folder.id, false, force && !forced);

                            if (capNotify && subscriptions &&
                                    EntityFolder.INBOX.equals(folder.type) &&
                                    MessageHelper.hasCapability(ifolder, "NOTIFY"))
                                ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                                    @Override
                                    public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                                        EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Account, account,
                                                account.name + " NOTIFY enable");

                                        // https://tools.ietf.org/html/rfc5465
                                        Argument arg = new Argument();
                                        arg.writeAtom("SET STATUS" +
                                                " (selected (MessageNew (uid) MessageExpunge FlagChange))" +
                                                " (subscribed (MessageNew MessageExpunge FlagChange))");

                                        Response[] responses = protocol.command("NOTIFY", arg);

                                        if (responses.length == 0)
                                            throw new ProtocolException("No response");
                                        if (!responses[responses.length - 1].isOK()) {
                                            Log.w(new ProtocolException(responses[responses.length - 1]));
                                            return null;
                                        }

                                        for (int i = 0; i < responses.length - 1; i++) {
                                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Account, account,
                                                    account.name + " " + responses[i]);
                                            if (responses[i] instanceof IMAPResponse) {
                                                IMAPResponse ir = (IMAPResponse) responses[i];
                                                if (ir.keyEquals("STATUS")) {
                                                    String mailbox = ir.readAtomString();
                                                    EntityFolder f = db.folder().getFolderByName(account.id, mailbox);
                                                    if (f != null)
                                                        EntityOperation.sync(ServiceSynchronize.this, f.id, false);
                                                }
                                            }
                                        }

                                        return null;
                                    }
                                });
                        } else {
                            mapFolders.put(folder, null);
                            db.folder().setFolderState(folder.id, null);
                            if (!capIdle && !folder.poll) {
                                folder.poll = true;
                                db.folder().setFolderPoll(folder.id, folder.poll);
                            }
                        }
                    }

                    forced = true;

                    final long serial = state.getSerial();

                    Log.i(account.name + " observing operations");
                    getMainHandler().post(new RunnableEx("observe#start") {
                        @Override
                        public void delegate() {
                            cowner.value = new TwoStateOwner(ServiceSynchronize.this, account.name);
                            cowner.value.start();

                            db.operation().liveOperations(account.id).observe(cowner.value, new Observer<List<TupleOperationEx>>() {
                                private final DutyCycle dc = new DutyCycle(account.name + " operations");
                                private final List<Long> handling = new ArrayList<>();
                                private final Map<TupleOperationEx.PartitionKey, List<TupleOperationEx>> partitions = new HashMap<>();

                                private final PowerManager.WakeLock wlOperations = pm.newWakeLock(
                                        PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":operations." + account.id);

                                @Override
                                public void onChanged(final List<TupleOperationEx> _operations) {
                                    // Get new operations
                                    List<Long> all = new ArrayList<>();
                                    Map<Long, List<TupleOperationEx>> added = new LinkedHashMap<>();
                                    for (TupleOperationEx op : _operations) {
                                        all.add(op.id);
                                        if (!handling.contains(op.id)) {
                                            if (!added.containsKey(op.folder))
                                                added.put(op.folder, new ArrayList<>());
                                            added.get(op.folder).add(op);
                                        }
                                    }
                                    handling.clear();
                                    handling.addAll(all);

                                    if (empty_pool && istore instanceof IMAPStore) {
                                        getMainHandler().removeCallbacks(purge);
                                        if (handling.size() == 0)
                                            getMainHandler().postDelayed(purge, PURGE_DELAY);
                                    }

                                    for (Long fid : added.keySet()) {
                                        EntityFolder found = null;
                                        for (EntityFolder f : mapFolders.keySet())
                                            if (Objects.equals(fid, f.id)) {
                                                found = f;
                                                break;
                                            }
                                        if (found == null) {
                                            Log.w(account.name + " folder not found operation=" + fid);
                                            continue;
                                        }

                                        final EntityFolder folder = found;
                                        Log.i(account.name + "/" + folder.name + " queuing operations=" + added.size() +
                                                " init=" + folder.initialize + " poll=" + folder.poll);

                                        // Partition operations by priority
                                        boolean offline = (mapFolders.get(folder) == null);
                                        List<TupleOperationEx.PartitionKey> keys = new ArrayList<>();
                                        synchronized (partitions) {
                                            for (TupleOperationEx op : added.get(folder.id)) {
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
                                            int ops;
                                            synchronized (partitions) {
                                                ops = partitions.get(key).size();
                                                Log.i(account.name + "/" + folder.name +
                                                        " queuing partition=" + key +
                                                        " serial=" + serial +
                                                        " operations=" + ops);
                                            }

                                            Map<String, String> crumb = new HashMap<>();
                                            crumb.put("account", folder.account == null ? null : Long.toString(folder.account));
                                            crumb.put("folder", folder.name + "/" + folder.type + ":" + folder.id);
                                            crumb.put("partition", key.toString());
                                            crumb.put("operations", Integer.toString(ops));
                                            crumb.put("serial", Long.toString(serial));
                                            Log.breadcrumb("Queuing", crumb);

                                            executor.submit(new Helper.PriorityRunnable(group, key.getPriority(), key.getOrder()) {
                                                @Override
                                                public void run() {
                                                    super.run();

                                                    long timeout = Helper.WAKELOCK_MAX;
                                                    long start = new Date().getTime();
                                                    try {
                                                        List<TupleOperationEx> partition;
                                                        synchronized (partitions) {
                                                            partition = partitions.get(key);
                                                            partitions.remove(key);
                                                        }

                                                        for (TupleOperationEx op : partition)
                                                            if (EntityOperation.SYNC.equals(op.name) ||
                                                                    EntityOperation.PURGE.equals(op.name)) {
                                                                timeout = 24 * 3600 * 1000L;
                                                                break;
                                                            }

                                                        wlOperations.acquire(timeout);

                                                        Log.i(account.name + "/" + folder.name +
                                                                " executing partition=" + key +
                                                                " serial=" + serial +
                                                                " operations=" + partition.size());

                                                        Map<String, String> crumb = new HashMap<>();
                                                        crumb.put("account", folder.account == null ? null : Long.toString(folder.account));
                                                        crumb.put("folder", folder.name + "/" + folder.type + ":" + folder.id);
                                                        crumb.put("partition", key.toString());
                                                        crumb.put("operations", Integer.toString(partition.size()));
                                                        crumb.put("serial", Long.toString(serial));
                                                        Log.breadcrumb("Executing", crumb);

                                                        // Get folder
                                                        Folder ifolder = mapFolders.get(folder); // null when polling
                                                        boolean canOpen = (EntityFolder.INBOX.equals(folder.type) ||
                                                                (account.protocol == EntityAccount.TYPE_IMAP && !folder.local));
                                                        final boolean shouldClose = (ifolder == null && canOpen);

                                                        try {
                                                            Log.i(account.name + "/" + folder.name + " run " + (shouldClose ? "offline" : "online"));

                                                            if (shouldClose) {
                                                                // Prevent unnecessary folder connections
                                                                if (db.operation().getOperationCount(folder.id, null) == 0)
                                                                    return;

                                                                db.folder().setFolderState(folder.id, "connecting");

                                                                try {
                                                                    ifolder = iservice.getStore().getFolder(folder.name);
                                                                } catch (IllegalStateException | MessagingException ex) {
                                                                    if ("Not connected".equals(ex.getMessage())) {
                                                                        Log.i(ex);
                                                                        return; // Store closed
                                                                    } else
                                                                        throw ex;
                                                                }

                                                                try {
                                                                    try {
                                                                        ifolder.open(Folder.READ_WRITE);
                                                                        if (ifolder instanceof IMAPFolder) {
                                                                            folder.read_only = ((IMAPFolder) ifolder).getUIDNotSticky();
                                                                            db.folder().setFolderReadOnly(folder.id, folder.read_only);
                                                                        }
                                                                    } catch (ReadOnlyFolderException ex) {
                                                                        Log.w(folder.name + " read only");
                                                                        ifolder.open(Folder.READ_ONLY);
                                                                        folder.read_only = true;
                                                                        db.folder().setFolderReadOnly(folder.id, folder.read_only);
                                                                    }
                                                                } catch (MessagingException ex) {
                                                                    /*
                                                                        javax.mail.MessagingException: GS38 NO Mailbox doesn't exist: 0 XXX (0.020 + 0.000 + 0.019 secs).;
                                                                          nested exception is:
                                                                            com.sun.mail.iap.CommandFailedException: GS38 NO Mailbox doesn't exist: 0 XXX (0.020 + 0.000 + 0.019 secs).
                                                                            at com.sun.mail.imap.IMAPFolder.open(SourceFile:61)
                                                                            at com.sun.mail.imap.IMAPFolder.open(SourceFile:1)
                                                                            at eu.faircode.email.ServiceSynchronize$19$1$2.run(SourceFile:30)
                                                                            at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:459)
                                                                            at java.util.concurrent.FutureTask.run(FutureTask.java:266)
                                                                            at eu.faircode.email.Helper$PriorityFuture.run(SourceFile:1)
                                                                            at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
                                                                            at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
                                                                            at java.lang.Thread.run(Thread.java:764)
                                                                        Caused by: com.sun.mail.iap.CommandFailedException: GS38 NO Mailbox doesn't exist: 0 XXX (0.020 + 0.000 + 0.019 secs).
                                                                            at com.sun.mail.iap.Protocol.handleResult(SourceFile:8)
                                                                            at com.sun.mail.imap.protocol.IMAPProtocol.select(SourceFile:19)
                                                                            at com.sun.mail.imap.IMAPFolder.open(SourceFile:16)
                                                                     */
                                                                    if (ex.getCause() instanceof ProtocolException &&
                                                                            !ConnectionHelper.isIoError(ex))
                                                                        throw new FolderNotFoundException(ifolder, ex.getMessage(), ex);
                                                                    else
                                                                        throw ex;
                                                                }

                                                                db.folder().setFolderState(folder.id, "connected");
                                                                db.folder().setFolderError(folder.id, null);

                                                                int count = MessageHelper.getMessageCount(ifolder);
                                                                db.folder().setFolderTotal(folder.id, count < 0 ? null : count);

                                                                Log.i(account.name + " folder " + folder.name + " flags=" + ifolder.getPermanentFlags());
                                                            }

                                                            try {
                                                                dc.start();
                                                                Core.processOperations(ServiceSynchronize.this,
                                                                        account, folder,
                                                                        partition,
                                                                        iservice, ifolder,
                                                                        state, serial);
                                                            } finally {
                                                                dc.stop(state.getForeground(), executor);
                                                            }

                                                        } catch (Throwable ex) {
                                                            if (ex instanceof OperationCanceledException ||
                                                                    (ex instanceof IllegalStateException &&
                                                                            "Folder not open".equals(ex.getMessage())))
                                                                Log.i(folder.name, ex); // Illegal state: getMessageCount
                                                            else
                                                                Log.e(folder.name, ex);
                                                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Account, folder,
                                                                    account.name + "/" + folder.name + " process " + Log.formatThrowable(ex, false));
                                                            db.folder().setFolderError(folder.id, Log.formatThrowable(ex));

                                                            if (!(ex instanceof FolderNotFoundException))
                                                                state.error(new Core.OperationCanceledExceptionEx("Process", ex));
                                                        } finally {
                                                            if (shouldClose) {
                                                                if (ifolder != null && ifolder.isOpen()) {
                                                                    db.folder().setFolderState(folder.id, "closing");
                                                                    try {
                                                                        boolean expunge =
                                                                                (account.protocol == EntityAccount.TYPE_POP &&
                                                                                        !account.leave_on_server && account.client_delete);
                                                                        ifolder.close(expunge);
                                                                    } catch (Throwable ex) {
                                                                        Log.w(folder.name, ex);
                                                                    }
                                                                }
                                                                db.folder().setFolderState(folder.id, null);
                                                            }
                                                        }
                                                    } catch (Throwable ex) {
                                                        if ("Not connected".equals(ex.getMessage()))
                                                            Log.i(ex);
                                                        else
                                                            Log.e(ex);
                                                    } finally {
                                                        if (wlOperations.isHeld())
                                                            wlOperations.release();
                                                        else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                                            Log.e(key + " released elapse=" + (new Date().getTime() - start) + " timeout=" + timeout);
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
                                !account.keep_alive_ok &&
                                account.poll_interval - TUNE_KEEP_ALIVE_INTERVAL_STEP >= TUNE_KEEP_ALIVE_INTERVAL_MIN &&
                                Math.abs(idleTime - account.poll_interval * 60 * 1000L) < 60 * 1000L);
                        if (tune_keep_alive && !first && !account.keep_alive_ok)
                            EntityLog.log(this, EntityLog.Type.Account, account,
                                    account.name +
                                            " Tune interval=" + account.poll_interval +
                                            " idle=" + idleTime + "/" + tune);
                        try {
                            if (!state.isRecoverable()) {
                                Throwable unrecoverable = state.getUnrecoverable();
                                Exception cause =
                                        (unrecoverable instanceof Exception
                                                ? (Exception) unrecoverable
                                                : new Exception(unrecoverable));
                                throw new StoreClosedException(iservice.getStore(), "Unrecoverable", cause);
                            }

                            // Check token expiration
                            if (!account.isTransient(this)) {
                                Long expirationTime = iservice.getAccessTokenExpirationTime();
                                if (expirationTime != null && expirationTime < new Date().getTime()) {
                                    EntityLog.log(this, "### " + account.name + " token expired" +
                                            " expired=" + new Date(expirationTime) +
                                            " user=" + account.provider + ":" + account.user);
                                    throw new IllegalStateException(Log.TOKEN_REFRESH_REQUIRED);
                                }
                            }

                            // Sends store NOOP
                            if (EmailService.SEPARATE_STORE_CONNECTION) {
                                EntityLog.log(this, EntityLog.Type.Account, account,
                                        account.name + " checking store" +
                                                " memory=" + Log.getFreeMemMb() +
                                                " battery=" + Helper.getBatteryLevel(this));
                                if (!iservice.getStore().isConnected())
                                    throw new StoreClosedException(iservice.getStore(), "NOOP");
                            }

                            if (!getMainLooper().getThread().isAlive()) {
                                Log.e("App died");
                                EntityLog.log(this, EntityLog.Type.Account, account,
                                        account.name + " app died");
                                state.stop();
                                throw new StoreClosedException(iservice.getStore(), "App died");
                            }

                            if (sync) {
                                EntityLog.log(this, EntityLog.Type.Account, account,
                                        account.name + " checking folders");
                                for (EntityFolder folder : mapFolders.keySet())
                                    if (folder.selectable && folder.synchronize)
                                        if (!folder.poll && capIdle) {
                                            // Sends folder NOOP
                                            if (!mapFolders.get(folder).isOpen())
                                                throw new StoreClosedException(iservice.getStore(), "NOOP " + folder.name);
                                            if (keep_alive_poll)
                                                EntityOperation.poll(this, folder.id);
                                        } else {
                                            if (folder.poll_count == 0) {
                                                EntityLog.log(this, EntityLog.Type.Account, folder,
                                                        account.name + "/" + folder.name + " queue sync poll");
                                                EntityOperation.poll(this, folder.id);
                                            }
                                            folder.poll_count = (folder.poll_count + 1) % folder.poll_factor;
                                            db.folder().setFolderPollCount(folder.id, folder.poll_count);
                                            EntityLog.log(this, EntityLog.Type.Account, folder,
                                                    account.name + "/" + folder.name +
                                                            " poll count=" + folder.poll_count +
                                                            " factor=" + folder.poll_factor);
                                        }
                                if (!first)
                                    Core.onSynchronizeFolders(this,
                                            account, iservice.getStore(), state,
                                            true, false);
                            }
                        } catch (Throwable ex) {
                            if (tune) {
                                account.keep_alive_failed++;
                                account.keep_alive_succeeded = 0;
                                if (account.keep_alive_failed >= 3) {
                                    account.keep_alive_failed = 0;
                                    account.poll_interval = account.poll_interval - TUNE_KEEP_ALIVE_INTERVAL_STEP;
                                    db.account().setAccountKeepAliveInterval(account.id, account.poll_interval);
                                }
                                db.account().setAccountKeepAliveValues(account.id,
                                        account.keep_alive_failed, account.keep_alive_succeeded);
                                EntityLog.log(this, EntityLog.Type.Account, account,
                                        account.name + " keep alive" +
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
                                EntityLog.log(this, EntityLog.Type.Account, account,
                                        account.name + " keep alive ok");
                            } else
                                EntityLog.log(this, EntityLog.Type.Account, account,
                                        account.name + " keep alive" +
                                                " failed=" + account.keep_alive_failed +
                                                " succeeded=" + account.keep_alive_succeeded +
                                                " interval=" + account.poll_interval +
                                                " idle=" + idleTime);
                        }

                        // Successfully connected: reset back off time
                        state.setBackoff(CONNECT_BACKOFF_START);

                        // Record successful connection
                        account.last_connected = new Date().getTime();
                        EntityLog.log(this, EntityLog.Type.Account, account,
                                account.name + " set last_connected=" + new Date(account.last_connected));
                        db.account().setAccountConnected(account.id, account.last_connected);
                        db.account().setAccountWarning(account.id, capIdle ? null : getString(R.string.title_no_idle));

                        NotificationManager nm = Helper.getSystemService(this, NotificationManager.class);
                        nm.cancel("receive:" + account.id, NotificationHelper.NOTIFICATION_TAGGED);
                        nm.cancel("alert:" + account.id, NotificationHelper.NOTIFICATION_TAGGED);

                        // Schedule keep alive alarm
                        Intent intent = new Intent(this, ServiceSynchronize.class);
                        intent.setAction("keepalive:" + account.id);
                        PendingIntent pi = PendingIntentCompat.getForegroundService(
                                this, PI_KEEPALIVE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager am = Helper.getSystemService(this, AlarmManager.class);
                        try {
                            long duration = account.poll_interval * 60 * 1000L;
                            long trigger = System.currentTimeMillis() + duration;

                            Long expirationTime = null;
                            if (!account.isTransient(this)) {
                                expirationTime = iservice.getAccessTokenExpirationTime();
                                if (expirationTime != null &&
                                        expirationTime < trigger &&
                                        expirationTime > new Date().getTime()) {
                                    expirationTime += AuthState.EXPIRY_TIME_TOLERANCE_MS;
                                    EntityLog.log(this, "### " + account.name + " expedite keep alive" +
                                            " from " + new Date(trigger) + " to " + new Date(expirationTime));
                                    trigger = expirationTime;
                                }
                            }

                            EntityLog.log(this, EntityLog.Type.Account, account,
                                    "### " + account.name + " keep alive" +
                                            " wait=" + account.poll_interval +
                                            " until=" + new Date(trigger) +
                                            " expiration=" + (expirationTime == null ? null : new Date(expirationTime)));
                            AlarmManagerCompatEx.setAndAllowWhileIdle(ServiceSynchronize.this, am, AlarmManager.RTC_WAKEUP, trigger, pi);

                            try {
                                if (wlAccount.isHeld())
                                    wlAccount.release();
                                else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e("keeping alive released elapse=" + (new Date().getTime() - start));
                                state.acquire(2 * duration, false);
                                Log.i("### " + account.name + " keeping alive");
                            } catch (InterruptedException ex) {
                                EntityLog.log(this, EntityLog.Type.Account, account,
                                        account.name + " waited state=" + state);
                            } finally {
                                start = new Date().getTime();
                                wlAccount.acquire(Helper.WAKELOCK_MAX);
                            }
                        } finally {
                            am.cancel(pi);
                        }

                        first = false;
                    }

                    Log.i(account.name + " done state=" + state);
                } catch (Throwable ex) {
                    last_fail = ex;
                    iservice.dump(account.name);
                    if (ex.getMessage() != null && ex.getMessage().startsWith("OAuth refresh"))
                        Log.i(account.name, ex);
                    else
                        Log.e(account.name, ex);
                    EntityLog.log(this, EntityLog.Type.Account, account,
                            account.name + " connect " + ex + "\n" + android.util.Log.getStackTraceString(ex));
                    db.account().setAccountError(account.id, Log.formatThrowable(ex));

                    // Report account connection error
                    if (account.last_connected != null && !ConnectionHelper.airplaneMode(this)) {
                        EntityLog.log(this, EntityLog.Type.Account, account,
                                account.name + " last connected: " + new Date(account.last_connected));

                        int pollInterval = getPollInterval(this);
                        long now = new Date().getTime();
                        long delayed = now - account.last_connected - account.poll_interval * 60 * 1000L;
                        long maxDelayed = (pollInterval > 0 && !account.isExempted(this)
                                ? pollInterval * ACCOUNT_ERROR_AFTER_POLL : ACCOUNT_ERROR_AFTER) * 60 * 1000L;
                        // android.database.sqlite.SQLiteFullException: database or disk is full (code 13 SQLITE_FULL)
                        if (ex instanceof SQLiteFullException ||
                                (delayed > maxDelayed &&
                                        state.getBackoff() >= CONNECT_BACKOFF_ALARM_START * 60)) {
                            Log.i("Reporting sync error after=" + delayed);
                            Throwable warning = new Throwable(
                                    getString(R.string.title_no_sync,
                                            Helper.getDateTimeInstance(this, DateFormat.SHORT, DateFormat.SHORT)
                                                    .format(account.last_connected)), ex);
                            try {
                                NotificationManager nm =
                                        Helper.getSystemService(this, NotificationManager.class);
                                if (NotificationHelper.areNotificationsEnabled(nm))
                                    nm.notify("receive:" + account.id,
                                            NotificationHelper.NOTIFICATION_TAGGED,
                                            Core.getNotificationError(this, "warning", account, null, null, warning)
                                                    .build());
                            } catch (Throwable ex1) {
                                Log.w(ex1);
                            }
                        }
                    }
                } finally {
                    // Update state
                    EntityLog.log(this, EntityLog.Type.Account, account,
                            account.name + " closing");

                    // Cancel purge
                    getMainHandler().removeCallbacks(purge);

                    // Stop watching operations
                    Log.i(account.name + " stop watching operations");
                    final CountDownLatch latch = new CountDownLatch(1);

                    getMainHandler().post(new RunnableEx("observe#stop") {
                        @Override
                        public void delegate() {
                            try {
                                if (cowner.value != null)
                                    cowner.value.destroy();
                            } catch (Throwable ex) {
                                Log.e(ex);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });

                    try {
                        latch.await(5000L, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                        Log.i(ex);
                    }

                    // Stop executing operations
                    Log.i(account.name + " stop executing operations");
                    state.nextSerial();
                    for (Runnable task : ((ThreadPoolExecutor) executor).getQueue().toArray(new Runnable[0]))
                        if (task instanceof Helper.PriorityRunnable &&
                                ((Helper.PriorityRunnable) task).getGroup() == group)
                            ((ThreadPoolExecutor) executor).remove(task);

                    // Close store
                    try {
                        db.account().setAccountState(account.id, "closing");
                        for (EntityFolder folder : mapFolders.keySet())
                            if (folder.selectable && folder.synchronize && !folder.poll && mapFolders.get(folder) != null)
                                db.folder().setFolderState(folder.id, "closing");
                        EntityLog.log(this, EntityLog.Type.Account, account,
                                account.name + " store closing");
                        iservice.close();
                        EntityLog.log(this, EntityLog.Type.Account, account,
                                account.name + " store closed");
                    } catch (Throwable ex) {
                        Log.w(account.name, ex);
                    } finally {
                        EntityLog.log(this, EntityLog.Type.Account, account,
                                account.name + " closed");
                        db.account().setAccountState(account.id, null);
                        for (EntityFolder folder : mapFolders.keySet())
                            db.folder().setFolderState(folder.id, null);
                    }

                    // Stop idlers
                    for (Thread idler : idlers)
                        state.join(idler);
                    idlers.clear();
                }

                if (state.isRunning()) {
                    long now = new Date().getTime();
                    ConnectivityManager cm = Helper.getSystemService(this, ConnectivityManager.class);
                    boolean logarithmic_backoff = prefs.getBoolean("logarithmic_backoff", true);
                    int max_backoff_power = prefs.getInt("max_backoff_power", DEFAULT_BACKOFF_POWER - 3);
                    int max_backoff = (int) Math.pow(2, max_backoff_power + 3);

                    if (logarithmic_backoff) {
                        // Check for fast successive server, connectivity, etc failures
                        long poll_interval = Math.min(account.poll_interval, CONNECT_BACKOFF_ALARM_START);
                        long fail_threshold = poll_interval * 60 * 1000L * FAST_FAIL_THRESHOLD / 100;
                        long was_connected = (account.last_connected == null ? 0 : now - account.last_connected);
                        if (was_connected < fail_threshold) {
                            if (state.getBackoff() == CONNECT_BACKOFF_START) {
                                fast_fails++;
                                if (fast_fails == 1)
                                    first_fail = now;
                                else if (fast_fails >= FAST_FAIL_COUNT) {
                                    long avg_fail = (now - first_fail) / fast_fails;
                                    if (avg_fail < fail_threshold) {
                                        long missing = (fail_threshold - avg_fail) * fast_fails;
                                        int compensate = (int) (missing / (CONNECT_BACKOFF_ALARM_START * 60 * 1000L));
                                        if (compensate > 0) {
                                            if (was_connected != 0 && was_connected < CONNECT_BACKOFF_GRACE)
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
                                                    " backoff=" + backoff + "/" + max_backoff +
                                                    " network=" + (cm == null ? null : cm.getActiveNetworkInfo()) +
                                                    " host=" + account.host +
                                                    " ex=" + Log.formatThrowable(last_fail, false);
                                            if (compensate > 2)
                                                Log.e(msg);
                                            EntityLog.log(this, EntityLog.Type.Account, account, msg);

                                            state.setBackoff(backoff * 60);
                                        }
                                    }
                                }
                            }
                        } else {
                            fast_fails = 0;
                            first_fail = 0;
                        }
                    }

                    int backoff = state.getBackoff();
                    int recently = (lastLost + LOST_RECENTLY < now ? 1 : 2);
                    EntityLog.log(this, EntityLog.Type.Account, account,
                            account.name + " backoff=" + backoff + "/" + max_backoff +
                                    " recently=" + recently + "x" +
                                    " logarithmic=" + logarithmic_backoff +
                                    " network=" + (cm == null ? null : cm.getActiveNetworkInfo()) +
                                    " ex=" + Log.formatThrowable(last_fail, false));

                    if (logarithmic_backoff) {
                        if (backoff < max_backoff)
                            state.setBackoff(backoff * 2);
                        else if (backoff == max_backoff)
                            if (AlarmManagerCompatEx.hasExactAlarms(this))
                                state.setBackoff(CONNECT_BACKOFF_INTERMEDIATE * 60);
                            else
                                state.setBackoff(CONNECT_BACKOFF_ALARM_START * 60);
                        else if (backoff == CONNECT_BACKOFF_INTERMEDIATE * 60)
                            state.setBackoff(CONNECT_BACKOFF_ALARM_START * 60);
                        else if (backoff < CONNECT_BACKOFF_ALARM_MAX * 60) {
                            int b = backoff * 2;
                            if (b > CONNECT_BACKOFF_ALARM_MAX * 60)
                                b = CONNECT_BACKOFF_ALARM_MAX * 60;
                            state.setBackoff(b);
                        }
                    } else {
                        // Linear back-off
                        int b = backoff + (backoff < CONNECT_BACKOFF_INTERMEDIATE * 60 ? 60 : 5 * 60);
                        if (b > CONNECT_BACKOFF_ALARM_MAX * 60)
                            b = CONNECT_BACKOFF_ALARM_MAX * 60;
                        state.setBackoff(b);
                    }

                    Map<String, String> crumb = new HashMap<>();
                    crumb.put("account", account.name);
                    crumb.put("backoff", Integer.toString(backoff));
                    crumb.put("max_backoff", Integer.toString(max_backoff));
                    crumb.put("recently", Integer.toString(recently));
                    crumb.put("logarithmic", Boolean.toString(logarithmic_backoff));
                    Log.breadcrumb("Backing off", crumb);

                    if (backoff <= max_backoff) {
                        // Short back-off period, keep device awake
                        try {
                            long interval = backoff * 1000L * recently;
                            db.account().setAccountBackoff(account.id, System.currentTimeMillis() + interval);
                            state.acquire(interval, true);
                        } catch (InterruptedException ex) {
                            Log.w(account.name + " backoff " + ex.toString());
                        } finally {
                            db.account().setAccountBackoff(account.id, null);
                        }
                    } else {
                        // Cancel transient sync operations
                        if (account.isTransient(this)) {
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

                        AlarmManager am = Helper.getSystemService(this, AlarmManager.class);
                        try {
                            long trigger = System.currentTimeMillis() + backoff * 1000L;
                            EntityLog.log(this, EntityLog.Type.Account, account,
                                    "### " + account.name + " backoff until=" + new Date(trigger));
                            AlarmManagerCompatEx.setAndAllowWhileIdle(ServiceSynchronize.this, am, AlarmManager.RTC_WAKEUP, trigger, pi);

                            try {
                                db.account().setAccountBackoff(account.id, trigger);
                                if (wlAccount.isHeld())
                                    wlAccount.release();
                                else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e("backoff released elapse=" + (new Date().getTime() - start));
                                state.acquire(2 * backoff * 1000L, true);
                                Log.i("### " + account.name + " backoff done");
                            } catch (InterruptedException ex) {
                                Log.w(account.name + " backoff " + ex.toString());
                            } finally {
                                start = new Date().getTime();
                                wlAccount.acquire(Helper.WAKELOCK_MAX);
                                db.account().setAccountBackoff(account.id, null);
                            }
                        } finally {
                            am.cancel(pi);
                        }
                    }
                }

                accountThread = db.account().getAccountThread(account.id);
            }

            if (!currentThread.equals(accountThread) && accountThread != null)
                Log.i(account.name + " orphan thread id=" + currentThread + "/" + accountThread);
        } finally {
            EntityLog.log(this, EntityLog.Type.Account, account,
                    account.name + " stopped running=" + state.isRunning());
            if (wlAccount.isHeld())
                wlAccount.release();
            else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                Log.e("account released elapse=" + (new Date().getTime() - start));
        }
    }

    private void fetch(EntityFolder folder, IMAPFolder ifolder, Message[] messages, boolean invalidate, boolean deleted, String reason) throws MessagingException {
        Log.i(folder.name + " " + messages.length + " messages " + reason);

        List<Long> uids = new ArrayList<>();
        for (Message imessage : messages)
            try {
                long uid = ifolder.getUID(imessage);
                uids.add(uid);
            } catch (MessageRemovedException ex) {
                Log.w(ex);
            }

        Log.i(folder.name + " messages " + reason + " uids=" + TextUtils.join(",", uids));

        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            for (long uid : uids)
                EntityOperation.queue(this, folder, EntityOperation.FETCH, uid, invalidate, deleted);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void updateQuota(Context context, IMAPStore istore, EntityAccount account) {
        DB db = DB.getInstance(this);
        try {
            if (istore.hasCapability("QUOTA")) {
                // https://datatracker.ietf.org/doc/html/rfc2087
                Quota[] quotas = istore.getQuota("INBOX");
                if (quotas != null) {
                    Long usage = null;
                    Long limit = null;
                    for (Quota quota : quotas)
                        if (quota.resources != null)
                            for (Quota.Resource resource : quota.resources) {
                                EntityLog.log(context, EntityLog.Type.Account, account,
                                        account.name + " quota " +
                                                " root=\"" + quota.quotaRoot + "\"" +
                                                " resource=\"" + resource.name + "\"" +
                                                " " + resource.usage + "/" + resource.limit);
                                // (STORAGE nnnnn 9999999999999999)
                                if ("STORAGE".equalsIgnoreCase(resource.name)) {
                                    if (resource.usage * 1024 >= 0 && resource.usage < MAX_QUOTA)
                                        usage = (usage == null ? 0L : usage) + resource.usage * 1024;
                                    if (resource.limit * 1024 > 0 && resource.limit < MAX_QUOTA)
                                        limit = Math.max(limit == null ? 0L : limit, resource.limit * 1024);
                                }
                            }
                    EntityLog.log(context, EntityLog.Type.Account,
                            account.name + " Quota" +
                                    " records=" + quotas.length +
                                    " usage=" + (usage == null ? null : Helper.humanReadableByteCount(usage)) +
                                    " limit=" + (limit == null ? null : Helper.humanReadableByteCount(limit)) +
                                    " " + (usage == null || limit == null ? "?" : 100 * usage / limit) + " %");
                    db.account().setAccountQuota(account.id, usage, limit);
                }
            } else
                db.account().setAccountQuota(account.id, null, null);
        } catch (MessagingException ex) {
            Log.w(ex);
            db.account().setAccountQuota(account.id, null, null);
        }
    }

    private void optimizeAccount(EntityAccount account, String reason) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean auto_optimize = prefs.getBoolean("auto_optimize", false);
        if (!auto_optimize)
            return;

        DB db = DB.getInstance(this);

        int pollInterval = getPollInterval(this);
        EntityLog.log(this, EntityLog.Type.Account, account,
                account.name + " auto optimize" +
                        " reason=" + reason +
                        " poll interval=" + pollInterval);
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
        } else if (pollInterval <= 60 && account.isExempted(this)) {
            db.account().setAccountPollExempted(account.id, false);
            eval(this, "Optimize=" + reason);
        }
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
            Log.i("Received " + intent +
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
            Log.i("Received " + intent +
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
                            lastActive = active;
                            lastAcquired = new Date().getTime();
                            DnsHelper.clear(ServiceSynchronize.this);
                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Network,
                                    reason + ": new active network=" + active + "/" + lastActive);
                        }
                    } else if (lastActive != null) {
                        if (!ConnectionHelper.isConnected(ServiceSynchronize.this, lastActive)) {
                            lastActive = null;
                            lastLost = new Date().getTime();
                            EntityLog.log(ServiceSynchronize.this, EntityLog.Type.Network,
                                    reason + ": lost active network=" + lastActive +
                                            " after=" + (lastLost - lastAcquired) / 1000 + " s");
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
        EntityLog.log(context, "### Reload account=" + account + " force=" + force + " reason=" + reason);
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
        EntityLog.log(context, EntityLog.Type.Debug3, "Foreground=" + foreground);
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
            if (Helper.isPlayStoreInstall())
                Log.i(ex);
            else
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
