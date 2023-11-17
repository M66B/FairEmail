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

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.OperationCanceledException;
import android.os.PowerManager;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

import net.openid.appauth.AuthState;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
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

public class ServiceMonitor {
    static final int DEFAULT_BACKOFF_POWER = 3; // 2^3=8 seconds (totally 8+2x20=48 seconds)

    private static final long PURGE_DELAY = 30 * 1000L; // milliseconds
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

    static void monitorAccount(ServiceSynchronize context,
                               final EntityAccount account, final Core.State state,
                               final boolean sync, final boolean force) throws NoSuchProviderException {
        final PowerManager pm = Helper.getSystemService(context, PowerManager.class);
        final PowerManager.WakeLock wlAccount = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id);
        final PowerManager.WakeLock wlFolder = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id + ".folder");
        final PowerManager.WakeLock wlMessage = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id + ".message");
        boolean isOptimizing = Boolean.FALSE.equals(Helper.isIgnoringOptimizations(context));

        long start = new Date().getTime();
        try {
            wlAccount.acquire(Helper.WAKELOCK_MAX);

            boolean forced = false;
            final DB db = DB.getInstance(context);

            Long currentThread = Thread.currentThread().getId();
            Long accountThread = currentThread;
            db.account().setAccountThread(account.id, accountThread);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (account.notify)
                    account.createNotificationChannel(context);
                else
                    account.deleteNotificationChannel(context);
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean subscriptions = prefs.getBoolean("subscriptions", false);
                boolean keep_alive_poll = prefs.getBoolean("keep_alive_poll", false);
                boolean empty_pool = prefs.getBoolean("empty_pool", true);
                boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.DEBUG);

                final EmailService iservice = new EmailService(
                        context, account.getProtocol(), account.realm, account.encryption, account.insecure, account.unicode, debug);
                iservice.setPartialFetch(account.partial_fetch);
                iservice.setRawFetch(account.raw_fetch);
                iservice.setIgnoreBodyStructureSize(account.ignore_size);
                if (account.protocol != EntityAccount.TYPE_IMAP)
                    iservice.setLeaveOnServer(account.leave_on_server);

                if (account.keep_alive_noop) {
                    int timeout = prefs.getInt("timeout", EmailService.DEFAULT_CONNECT_TIMEOUT);
                    iservice.setRestartIdleInterval(timeout * 2 * 6); // 20 x 2 x 6 = 4 min
                }

                final Date lastStillHere = new Date(0);

                iservice.setListener(new StoreListener() {
                    @Override
                    public void notification(StoreEvent e) {
                        String message = e.getMessage();
                        if (TextUtils.isEmpty(message))
                            message = "?";
                        if (e.getMessageType() == StoreEvent.NOTICE) {
                            EntityLog.log(context, EntityLog.Type.Account, account,
                                    account.name + " notice: " + message);

                            if ("Still here".equals(message) &&
                                    !account.isTransient(context)) {
                                long now = new Date().getTime();
                                long last = lastStillHere.getTime();
                                if (last > 0) {
                                    long elapsed = now - last;
                                    if (elapsed < STILL_THERE_THRESHOLD)
                                        optimizeAccount(context, account, "'" + message + "'" +
                                                " elapsed=" + elapsed + " ms");
                                }
                                lastStillHere.setTime(now);
                            }
                        } else {
                            long start = new Date().getTime();
                            try {
                                wlFolder.acquire(Helper.WAKELOCK_MAX);

                                EntityLog.log(context, EntityLog.Type.Account, account,
                                        account.name + " alert: " + message);

                                if (!ConnectionHelper.isMaxConnections(message))
                                    try {
                                        NotificationManager nm =
                                                Helper.getSystemService(context, NotificationManager.class);
                                        if (NotificationHelper.areNotificationsEnabled(nm))
                                            nm.notify("alert:" + account.id,
                                                    NotificationHelper.NOTIFICATION_TAGGED,
                                                    getNotificationAlert(context, account, message).build());
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
                    EntityLog.log(context, EntityLog.Type.Account, account,
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
                                            Helper.getSystemService(context, NotificationManager.class);
                                    if (NotificationHelper.areNotificationsEnabled(nm))
                                        nm.notify("receive:" + account.id,
                                                NotificationHelper.NOTIFICATION_TAGGED,
                                                Core.getNotificationError(context, "error", account, 0, ex)
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
                    if (capabilities.length() > 500)
                        capabilities = capabilities.substring(0, 500) + "...";

                    Log.i(account.name + " idle=" + capIdle);
                    if (!capIdle || account.poll_interval < TUNE_KEEP_ALIVE_INTERVAL_MIN)
                        optimizeAccount(context, account, "IDLE");

                    db.account().setAccountState(account.id, "connected");
                    db.account().setAccountCapabilities(account.id, capabilities, capIdle, capUtf8);
                    db.account().setAccountError(account.id, null);
                    db.account().setAccountWarning(account.id, null);

                    Store istore = iservice.getStore();
                    if (istore instanceof IMAPStore) {
                        Map<String, String> caps = ((IMAPStore) istore).getCapabilities();
                        EntityLog.log(context, EntityLog.Type.Account, account,
                                account.name + " connected" +
                                        " caps=" + (caps == null ? null : TextUtils.join(" ", caps.keySet())));
                    } else
                        EntityLog.log(context, EntityLog.Type.Account, account,
                                account.name + " connected");

                    db.account().setAccountMaxSize(account.id, iservice.getMaxSize());
                    if (istore instanceof IMAPStore)
                        updateQuota(context, ((IMAPStore) iservice.getStore()), account);

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
                                    ServiceSynchronize.reload(context, account.id, false, "folder created");
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
                                    ServiceSynchronize.reload(context, account.id, false, "folder renamed");
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
                                    ServiceSynchronize.reload(context, account.id, false, "folder deleted");
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
                                EntityLog.log(context, EntityLog.Type.Account, account,
                                        "Folder changed=" + name);
                                EntityFolder folder = db.folder().getFolderByName(account.id, name);
                                if (folder != null && folder.selectable)
                                    EntityOperation.sync(context, folder.id, false);
                            } finally {
                                if (wlFolder.isHeld())
                                    wlFolder.release();
                                else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e("folder changed released elapse=" + (new Date().getTime() - start));
                            }
                        }
                    });

                    // Update folder list
                    Core.onSynchronizeFolders(context,
                            account, iservice.getStore(), state,
                            false, force && !forced);

                    // Open synchronizing folders
                    List<EntityFolder> folders = db.folder().getFolders(account.id, false, true);
                    if (folders.size() > 0)
                        Collections.sort(folders, folders.get(0).getComparator(context));

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
                                        fetch(context, folder, ifolder, e.getMessages(), false, false, "added");
                                        Thread.sleep(FETCH_YIELD_DURATION);
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(context, EntityLog.Type.Account, folder,
                                                account.name + "/" + folder.name + " added " + Log.formatThrowable(ex, false));
                                        EntityOperation.sync(context, folder.id, false);
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
                                        fetch(context, folder, ifolder, e.getMessages(), false, true, "removed");
                                        Thread.sleep(FETCH_YIELD_DURATION);
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(context, EntityLog.Type.Account, folder,
                                                account.name + "/" + folder.name + " removed " + Log.formatThrowable(ex, false));
                                        EntityOperation.sync(context, folder.id, false);
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
                                        fetch(context, folder, ifolder, new Message[]{imessage}, true, false, "changed");
                                        Thread.sleep(FETCH_YIELD_DURATION);
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        EntityLog.log(context, EntityLog.Type.Account, folder,
                                                account.name + "/" + folder.name + " changed " + Log.formatThrowable(ex, false));
                                        EntityOperation.sync(context, folder.id, false);
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
                                        EntityLog.log(context, EntityLog.Type.Account, folder,
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

                            EntityOperation.sync(context, folder.id, false, force && !forced);

                            if (capNotify && subscriptions && EntityFolder.INBOX.equals(folder.type))
                                ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                                    @Override
                                    public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                                        EntityLog.log(context, EntityLog.Type.Account, account,
                                                account.name + " NOTIFY enable");

                                        // https://tools.ietf.org/html/rfc5465
                                        Argument arg = new Argument();
                                        arg.writeAtom("SET STATUS" +
                                                " (selected (MessageNew (uid) MessageExpunge FlagChange))" +
                                                " (subscribed (MessageNew MessageExpunge FlagChange))");

                                        Response[] responses = protocol.command("NOTIFY", arg);

                                        if (responses.length == 0)
                                            throw new ProtocolException("No response");
                                        if (!responses[responses.length - 1].isOK())
                                            throw new ProtocolException(responses[responses.length - 1]);

                                        for (int i = 0; i < responses.length - 1; i++) {
                                            EntityLog.log(context, EntityLog.Type.Account, account,
                                                    account.name + " " + responses[i]);
                                            if (responses[i] instanceof IMAPResponse) {
                                                IMAPResponse ir = (IMAPResponse) responses[i];
                                                if (ir.keyEquals("STATUS")) {
                                                    String mailbox = ir.readAtomString();
                                                    EntityFolder f = db.folder().getFolderByName(account.id, mailbox);
                                                    if (f != null)
                                                        EntityOperation.sync(context, f.id, false);
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
                    context.getMainHandler().post(new RunnableEx("observe#start") {
                        @Override
                        public void delegate() {
                            cowner.value = new TwoStateOwner(context, account.name);
                            cowner.value.start();

                            db.operation().liveOperations(account.id).observe(cowner.value, new Observer<List<TupleOperationEx>>() {
                                private DutyCycle dc = new DutyCycle(account.name + " operations");
                                private List<Long> handling = new ArrayList<>();
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
                                    handling = all;

                                    if (empty_pool && istore instanceof IMAPStore) {
                                        context.getMainHandler().removeCallbacks(purge);
                                        if (handling.size() == 0)
                                            context.getMainHandler().postDelayed(purge, PURGE_DELAY);
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
                                                                } catch (IllegalStateException ex) {
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
                                                                Core.processOperations(context,
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
                                                            EntityLog.log(context, EntityLog.Type.Account, folder,
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
                            EntityLog.log(context, EntityLog.Type.Account, account,
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
                            if (!account.isTransient(context)) {
                                Long expirationTime = iservice.getAccessTokenExpirationTime();
                                if (expirationTime != null && expirationTime < new Date().getTime()) {
                                    EntityLog.log(context, "### " + account.name + " token expired" +
                                            " expired=" + new Date(expirationTime) +
                                            " user=" + account.provider + ":" + account.user);
                                    throw new IllegalStateException(Log.TOKEN_REFRESH_REQUIRED);
                                }
                            }

                            // Sends store NOOP
                            if (EmailService.SEPARATE_STORE_CONNECTION) {
                                EntityLog.log(context, EntityLog.Type.Account, account,
                                        account.name + " checking store" +
                                                " memory=" + Log.getFreeMemMb() +
                                                " battery=" + Helper.getBatteryLevel(context));
                                if (!iservice.getStore().isConnected())
                                    throw new StoreClosedException(iservice.getStore(), "NOOP");
                            }

                            if (!context.getMainLooper().getThread().isAlive()) {
                                Log.e("App died");
                                EntityLog.log(context, EntityLog.Type.Account, account,
                                        account.name + " app died");
                                state.stop();
                                throw new StoreClosedException(iservice.getStore(), "App died");
                            }

                            if (sync) {
                                EntityLog.log(context, EntityLog.Type.Account, account,
                                        account.name + " checking folders");
                                for (EntityFolder folder : mapFolders.keySet())
                                    if (folder.selectable && folder.synchronize)
                                        if (!folder.poll && capIdle) {
                                            // Sends folder NOOP
                                            if (!mapFolders.get(folder).isOpen())
                                                throw new StoreClosedException(iservice.getStore(), "NOOP " + folder.name);
                                            if (keep_alive_poll)
                                                EntityOperation.poll(context, folder.id);
                                        } else {
                                            if (folder.poll_count == 0) {
                                                EntityLog.log(context, EntityLog.Type.Account, folder,
                                                        account.name + "/" + folder.name + " queue sync poll");
                                                EntityOperation.poll(context, folder.id);
                                            }
                                            folder.poll_count = (folder.poll_count + 1) % folder.poll_factor;
                                            db.folder().setFolderPollCount(folder.id, folder.poll_count);
                                            EntityLog.log(context, EntityLog.Type.Account, folder,
                                                    account.name + "/" + folder.name +
                                                            " poll count=" + folder.poll_count +
                                                            " factor=" + folder.poll_factor);
                                        }
                                if (!first)
                                    Core.onSynchronizeFolders(context,
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
                                EntityLog.log(context, EntityLog.Type.Account, account,
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
                                EntityLog.log(context, EntityLog.Type.Account, account,
                                        account.name + " keep alive ok");
                            } else
                                EntityLog.log(context, EntityLog.Type.Account, account,
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
                        EntityLog.log(context, EntityLog.Type.Account, account,
                                account.name + " set last_connected=" + new Date(account.last_connected));
                        db.account().setAccountConnected(account.id, account.last_connected);
                        db.account().setAccountWarning(account.id, capIdle ? null : context.getString(R.string.title_no_idle));

                        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                        nm.cancel("receive:" + account.id, NotificationHelper.NOTIFICATION_TAGGED);
                        nm.cancel("alert:" + account.id, NotificationHelper.NOTIFICATION_TAGGED);

                        // Schedule keep alive alarm
                        Intent intent = new Intent(context, ServiceSynchronize.class);
                        intent.setAction("keepalive:" + account.id);
                        PendingIntent pi = PendingIntentCompat.getForegroundService(
                                context, ServiceSynchronize.PI_KEEPALIVE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager am = Helper.getSystemService(context, AlarmManager.class);
                        try {
                            long duration = account.poll_interval * 60 * 1000L;
                            long trigger = System.currentTimeMillis() + duration;

                            Long expirationTime = null;
                            if (!account.isTransient(context)) {
                                expirationTime = iservice.getAccessTokenExpirationTime();
                                if (expirationTime != null &&
                                        expirationTime < trigger &&
                                        expirationTime > new Date().getTime()) {
                                    expirationTime += AuthState.EXPIRY_TIME_TOLERANCE_MS;
                                    EntityLog.log(context, "### " + account.name + " expedite keep alive" +
                                            " from " + new Date(trigger) + " to " + new Date(expirationTime));
                                    trigger = expirationTime;
                                }
                            }

                            EntityLog.log(context, EntityLog.Type.Account, account,
                                    "### " + account.name + " keep alive" +
                                            " wait=" + account.poll_interval +
                                            " until=" + new Date(trigger) +
                                            " expiration=" + (expirationTime == null ? null : new Date(expirationTime)));
                            AlarmManagerCompatEx.setAndAllowWhileIdle(context, am, AlarmManager.RTC_WAKEUP, trigger, pi);

                            try {
                                if (wlAccount.isHeld())
                                    wlAccount.release();
                                else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                                    Log.e("keeping alive released elapse=" + (new Date().getTime() - start));
                                state.acquire(2 * duration, false);
                                Log.i("### " + account.name + " keeping alive");
                            } catch (InterruptedException ex) {
                                EntityLog.log(context, EntityLog.Type.Account, account,
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
                    EntityLog.log(context, EntityLog.Type.Account, account,
                            account.name + " connect " + ex + "\n" + android.util.Log.getStackTraceString(ex));
                    db.account().setAccountError(account.id, Log.formatThrowable(ex));

                    // Report account connection error
                    if (account.last_connected != null && !ConnectionHelper.airplaneMode(context)) {
                        EntityLog.log(context, EntityLog.Type.Account, account,
                                account.name + " last connected: " + new Date(account.last_connected));

                        int pollInterval = ServiceSynchronize.getPollInterval(context);
                        long now = new Date().getTime();
                        long delayed = now - account.last_connected - account.poll_interval * 60 * 1000L;
                        long maxDelayed = (pollInterval > 0 && !account.isExempted(context)
                                ? pollInterval * ACCOUNT_ERROR_AFTER_POLL : ACCOUNT_ERROR_AFTER) * 60 * 1000L;
                        if (delayed > maxDelayed &&
                                state.getBackoff() >= CONNECT_BACKOFF_ALARM_START * 60) {
                            Log.i("Reporting sync error after=" + delayed);
                            Throwable warning = new Throwable(
                                    context.getString(R.string.title_no_sync,
                                            Helper.getDateTimeInstance(context, DateFormat.SHORT, DateFormat.SHORT)
                                                    .format(account.last_connected)), ex);
                            try {
                                NotificationManager nm =
                                        Helper.getSystemService(context, NotificationManager.class);
                                if (NotificationHelper.areNotificationsEnabled(nm))
                                    nm.notify("receive:" + account.id,
                                            NotificationHelper.NOTIFICATION_TAGGED,
                                            Core.getNotificationError(context, "warning", account, 0, warning)
                                                    .build());
                            } catch (Throwable ex1) {
                                Log.w(ex1);
                            }
                        }
                    }
                } finally {
                    // Update state
                    EntityLog.log(context, EntityLog.Type.Account, account,
                            account.name + " closing");

                    // Cancel purge
                    context.getMainHandler().removeCallbacks(purge);

                    // Stop watching operations
                    Log.i(account.name + " stop watching operations");
                    final CountDownLatch latch = new CountDownLatch(1);

                    context.getMainHandler().post(new RunnableEx("observe#stop") {
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
                        EntityLog.log(context, EntityLog.Type.Account, account,
                                account.name + " store closing");
                        iservice.close();
                        EntityLog.log(context, EntityLog.Type.Account, account,
                                account.name + " store closed");
                    } catch (Throwable ex) {
                        Log.w(account.name, ex);
                    } finally {
                        EntityLog.log(context, EntityLog.Type.Account, account,
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
                    ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
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
                                            EntityLog.log(context, EntityLog.Type.Account, account, msg);

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
                    int recently = (context.getLastLost() + LOST_RECENTLY < now ? 1 : 2);
                    EntityLog.log(context, EntityLog.Type.Account, account,
                            account.name + " backoff=" + backoff + "/" + max_backoff +
                                    " recently=" + recently + "x" +
                                    " logarithmic=" + logarithmic_backoff +
                                    " network=" + (cm == null ? null : cm.getActiveNetworkInfo()) +
                                    " ex=" + Log.formatThrowable(last_fail, false));

                    if (logarithmic_backoff) {
                        if (backoff < max_backoff)
                            state.setBackoff(backoff * 2);
                        else if (backoff == max_backoff)
                            if (AlarmManagerCompatEx.hasExactAlarms(context))
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
                        if (account.isTransient(context)) {
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
                        Intent intent = new Intent(context, ServiceSynchronize.class);
                        intent.setAction("backoff:" + account.id);
                        PendingIntent pi = PendingIntentCompat.getForegroundService(
                                context, ServiceSynchronize.PI_BACKOFF, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager am = Helper.getSystemService(context, AlarmManager.class);
                        try {
                            long trigger = System.currentTimeMillis() + backoff * 1000L;
                            EntityLog.log(context, EntityLog.Type.Account, account,
                                    "### " + account.name + " backoff until=" + new Date(trigger));
                            AlarmManagerCompatEx.setAndAllowWhileIdle(context, am, AlarmManager.RTC_WAKEUP, trigger, pi);

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
            EntityLog.log(context, EntityLog.Type.Account, account,
                    account.name + " stopped running=" + state.isRunning());
            if (wlAccount.isHeld())
                wlAccount.release();
            else if (!isOptimizing && !BuildConfig.PLAY_STORE_RELEASE)
                Log.e("account released elapse=" + (new Date().getTime() - start));
        }
    }

    private static void fetch(Context context, EntityFolder folder, IMAPFolder ifolder, Message[] messages, boolean invalidate, boolean deleted, String reason) throws MessagingException {
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

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            for (long uid : uids)
                EntityOperation.queue(context, folder, EntityOperation.FETCH, uid, invalidate, deleted);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static void updateQuota(Context context, IMAPStore istore, EntityAccount account) {
        DB db = DB.getInstance(context);
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
                                        account.name + " Quota " + resource.name + " " + resource.usage + "/" + resource.limit);
                                // (STORAGE nnnnn 9999999999999999)
                                if ("STORAGE".equalsIgnoreCase(resource.name)) {
                                    if (resource.usage * 1024 >= 0)
                                        usage = (usage == null ? 0L : usage) + resource.usage * 1024;
                                    if (resource.limit * 1024 > 0)
                                        limit = Math.max(limit == null ? 0L : limit, resource.limit * 1024);
                                }
                            }
                    db.account().setAccountQuota(account.id, usage, limit);
                }
            } else
                db.account().setAccountQuota(account.id, null, null);
        } catch (MessagingException ex) {
            Log.w(ex);
            db.account().setAccountQuota(account.id, null, null);
        }
    }

    private static void optimizeAccount(Context context, EntityAccount account, String reason) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean auto_optimize = prefs.getBoolean("auto_optimize", false);
        if (!auto_optimize)
            return;

        DB db = DB.getInstance(context);

        int pollInterval = ServiceSynchronize.getPollInterval(context);
        EntityLog.log(context, EntityLog.Type.Account, account,
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
        } else if (pollInterval <= 60 && account.isExempted(context)) {
            db.account().setAccountPollExempted(account.id, false);
            ServiceSynchronize.eval(context, "Optimize=" + reason);
        }
    }

    private static NotificationCompat.Builder getNotificationAlert(Context context, EntityAccount account, String message) {
        String title = context.getString(R.string.title_notification_alert, account.name);

        // Build pending intent
        Intent intent = new Intent(context, ActivityError.class);
        intent.setAction("alert:" + account.id);
        intent.putExtra("type", "alert");
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("provider", account.provider);
        intent.putExtra("account", account.id);
        intent.putExtra("protocol", account.protocol);
        intent.putExtra("auth_type", account.auth_type);
        intent.putExtra("faq", 23);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piAlert = PendingIntentCompat.getActivity(
                context, ActivityError.PI_ALERT, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "alerts")
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
}
