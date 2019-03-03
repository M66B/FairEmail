package eu.faircode.email;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

public class ServiceUI extends IntentService {
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
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("Service UI destroy");
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
}
