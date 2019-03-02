package eu.faircode.email;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import androidx.annotation.Nullable;

public class ServiceUI extends IntentService {
    private PowerManager.WakeLock wl;
    private Map<EntityAccount, Store> accountStore = new HashMap<>();

    static final int PI_WHY = 1;
    static final int PI_SUMMARY = 2;
    static final int PI_CLEAR = 3;
    static final int PI_SEEN = 4;
    static final int PI_ARCHIVE = 5;
    static final int PI_TRASH = 6;
    static final int PI_IGNORED = 7;
    static final int PI_SNOOZED = 8;

    public ServiceUI() {
        this(ServiceUI.class.getName());
    }

    public ServiceUI(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        Log.i("Service UI create");
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":ui");
        wl.acquire();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("Service UI destroy");

        final DB db = DB.getInstance(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (EntityAccount account : accountStore.keySet())
                        try {
                            Log.i(account.name + " closing");
                            db.account().setAccountState(account.id, "closing");
                            accountStore.get(account).close();
                        } catch (Throwable ex) {
                            Log.w(ex);
                        } finally {
                            Log.i(account.name + " closed");
                            db.account().setAccountState(account.id, null);
                        }
                    accountStore.clear();
                } finally {
                    wl.release();
                }
            }
        }).start();

        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Under certain circumstances, a background app is placed on a temporary whitelist for several minutes
        // - Executing a PendingIntent from a notification.
        // https://developer.android.com/about/versions/oreo/background#services
        Log.i("Service UI intent=" + intent);

        if (intent == null)
            return;

        String action = intent.getAction();
        if (action == null)
            return;

        String[] parts = action.split(":");
        long id = (parts.length > 1 ? Long.parseLong(parts[1]) : -1);

        switch (parts[0]) {
            case "why":
                onWhy();
                break;

            case "summary":
                onSummary();
                break;

            case "clear":
                onClear();
                break;

            case "seen":
                onSeen(id);
                break;

            case "archive":
                onArchive(id);
                break;

            case "trash":
                onTrash(id);
                break;

            case "ignore":
                onIgnore(id);
                break;

            case "snooze":
                // AlarmManager.RTC_WAKEUP
                // When the alarm is dispatched, the app will also be added to the system's temporary whitelist
                // for approximately 10 seconds to allow that application to acquire further wake locks in which to complete its work.
                // https://developer.android.com/reference/android/app/AlarmManager
                onSnooze(id);
                break;

            case "process":
                onProcessOperations(id);
                break;

            case "fsync":
                onFolderSync(id);
                break;

            default:
                Log.w("Unknown action: " + parts[0]);
        }
    }

    private void onWhy() {
        Intent why = new Intent(Intent.ACTION_VIEW);
        why.setData(Uri.parse(Helper.FAQ_URI + "#user-content-faq2"));
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
    }

    private void onSummary() {
        Intent view = new Intent(this, ActivityView.class);
        view.setAction("unified");
        view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(view);
    }

    private void onClear() {
        DB.getInstance(this).message().ignoreAll();
    }

    private void onSeen(long id) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message != null)
                EntityOperation.queue(this, db, message, EntityOperation.SEEN, true);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onArchive(long id) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message != null) {
                EntityFolder archive = db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE);
                if (archive == null)
                    archive = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                if (archive != null)
                    EntityOperation.queue(this, db, message, EntityOperation.MOVE, archive.id);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onTrash(long id) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message != null) {
                EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                if (trash != null)
                    EntityOperation.queue(this, db, message, EntityOperation.MOVE, trash.id);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onIgnore(long id) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message != null)
                db.message().setMessageUiIgnored(message.id, true);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onSnooze(long id) {
        DB db = DB.getInstance(this);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message != null) {
                db.message().setMessageSnoozed(message.id, null);

                EntityFolder folder = db.folder().getFolder(message.folder);
                if (EntityFolder.OUTBOX.equals(folder.type)) {
                    Log.i("Delayed send id=" + message.id);
                    EntityOperation.queue(
                            this, db, message, EntityOperation.SEND);
                } else {
                    EntityOperation.queue(
                            this, db, message, EntityOperation.SEEN, false);
                    db.message().setMessageUiIgnored(message.id, false);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void onProcessOperations(long fid) {
        DB db = DB.getInstance(this);

        EntityFolder folder = db.folder().getFolder(fid);
        if (folder == null)
            return;
        EntityAccount account = db.account().getAccount(folder.account);
        if (account == null)
            return;

        Folder ifolder = null;
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.BETA_RELEASE);

            String protocol = account.getProtocol();

            // Get properties
            Properties props = MessageHelper.getSessionProperties(account.auth_type, account.realm, account.insecure);
            props.put("mail." + protocol + ".separatestoreconnection", "true");

            // Create session
            final Session isession = Session.getInstance(props, null);
            isession.setDebug(debug);

            Store istore = accountStore.get(account.id);
            if (istore == null || !istore.isConnected()) {
                // Connect account
                Log.i(account.name + " connecting");
                db.account().setAccountState(account.id, "connecting");
                istore = isession.getStore(protocol);
                Helper.connect(this, istore, account);
                db.account().setAccountState(account.id, "connected");
                db.account().setAccountConnected(account.id, new Date().getTime());
                db.account().setAccountError(account.id, null);
                Log.i(account.name + " connected");

                accountStore.put(account, istore);
            } else
                Log.i(account + " reusing connection");

            // Connect folder
            Log.i(folder.name + " connecting");
            db.folder().setFolderState(folder.id, "connecting");
            ifolder = istore.getFolder(folder.name);
            ifolder.open(Folder.READ_WRITE);
            db.folder().setFolderState(folder.id, "connected");
            db.folder().setFolderError(folder.id, null);
            Log.i(folder.name + " connected");

            // Process operations
            Core.processOperations(this, account, folder, isession, istore, ifolder, new Core.State());

        } catch (Throwable ex) {
            Log.w(ex);
            Core.reportError(this, account, folder, ex);
            db.account().setAccountError(account.id, Helper.formatThrowable(ex));
            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, false));
        } finally {
            if (ifolder != null)
                try {
                    Log.i(folder.name + " closing");
                    db.folder().setFolderState(folder.id, "closing");
                    ifolder.close();
                } catch (MessagingException ex) {
                    Log.w(ex);
                } finally {
                    Log.i(folder.name + " closed");
                    db.folder().setFolderState(folder.id, null);
                    db.folder().setFolderSyncState(folder.id, null);
                }
        }
    }

    private void onFolderSync(long aid) {
        DB db = DB.getInstance(this);
        EntityAccount account = db.account().getAccount(aid);
        if (account == null)
            return;

        Store istore = null;
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.BETA_RELEASE);

            String protocol = account.getProtocol();

            // Get properties
            Properties props = MessageHelper.getSessionProperties(account.auth_type, account.realm, account.insecure);
            props.put("mail." + protocol + ".separatestoreconnection", "true");

            // Create session
            final Session isession = Session.getInstance(props, null);
            isession.setDebug(debug);

            // Connect account
            Log.i(account.name + " connecting");
            db.account().setAccountState(account.id, "connecting");
            istore = isession.getStore(protocol);
            Helper.connect(this, istore, account);
            db.account().setAccountState(account.id, "connected");
            db.account().setAccountConnected(account.id, new Date().getTime());
            db.account().setAccountError(account.id, null);
            Log.i(account.name + " connected");

            // Synchronize folders
            Core.onSynchronizeFolders(this, account, istore, new Core.State());

        } catch (Throwable ex) {
            Log.w(ex);
            Core.reportError(this, account, null, ex);
            db.account().setAccountError(account.id, Helper.formatThrowable(ex));
        } finally {
            if (istore != null) {
                Log.i(account.name + " closing");
                db.account().setAccountState(account.id, "closing");

                try {
                    istore.close();
                } catch (MessagingException ex) {
                    Log.e(ex);
                }

                Log.i(account.name + " closed");
            }

            db.account().setAccountState(account.id, null);
        }
    }

    public static void process(Context context, long folder) {
        try {
            context.startService(
                    new Intent(context, ServiceUI.class)
                            .setAction("process:" + folder));
        } catch (IllegalStateException ex) {
            Log.w(ex);
            // The background service will handle the operation
        }
    }

    public static void fsync(Context context, long account) {
        try {
            context.startService(
                    new Intent(context, ServiceUI.class)
                            .setAction("fsync:" + account));
        } catch (IllegalStateException ex) {
            Log.w(ex);
            // The user went away
        }
    }
}
