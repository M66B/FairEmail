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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class WorkerCleanup extends Worker {
    private static final int CLEANUP_INTERVAL = 4; // hours
    private static final long KEEP_FILES_DURATION = 3600 * 1000L; // milliseconds
    private static final long KEEP_IMAGES_DURATION = 3 * 24 * 3600 * 1000L; // milliseconds
    private static final long KEEP_CONTACTS_DURATION = 365 * 24 * 3600 * 1000L; // milliseconds
    private static final int KEEP_CONTACTS_COUNT = 10000;

    private static Semaphore semaphore = new Semaphore(1);

    public WorkerCleanup(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.i("Instance " + getName());
    }

    @NonNull
    @Override
    public Result doWork() {
        EntityLog.log(getApplicationContext(),
                "Running " + getName() +
                        " process=" + android.os.Process.myPid());

        Thread.currentThread().setPriority(THREAD_PRIORITY_BACKGROUND);
        cleanup(getApplicationContext(), false);

        return Result.success();
    }

    static void cleanupConditionally(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        if (enabled) {
            Log.i("Skip cleanup enabled=" + enabled);
            return;
        }

        long now = new Date().getTime();
        long last_cleanup = prefs.getLong("last_cleanup", 0);
        if (last_cleanup + 2 * CLEANUP_INTERVAL * 3600 * 1000L > now) {
            Log.i("Skip cleanup last=" + new Date(last_cleanup));
            return;
        }

        cleanup(context, false);
    }

    static void cleanup(Context context, boolean manual) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fts = prefs.getBoolean("fts", true);
        boolean cleanup_attachments = prefs.getBoolean("cleanup_attachments", false);
        boolean download_headers = prefs.getBoolean("download_headers", false);
        boolean download_eml = prefs.getBoolean("download_eml", false);
        boolean sqlite_analyze = prefs.getBoolean("sqlite_analyze", true);

        long start = new Date().getTime();
        DB db = DB.getInstance(context);
        try {
            semaphore.acquire();
            EntityLog.log(context, "Start cleanup manual=" + manual);

            if (manual) {
                // Check message files
                Log.i("Checking message files");
                List<Long> mids = db.message().getMessageWithContent();
                for (Long mid : mids) {
                    EntityMessage message = db.message().getMessage(mid);
                    if (message != null) {
                        File file = message.getFile(context);
                        if (!file.exists()) {
                            Log.w("Message file missing id=" + mid);
                            db.message().resetMessageContent(mid);
                        }
                    }
                }

                // Check attachments files
                Log.i("Checking attachments files");
                List<Long> aids = db.attachment().getAttachmentAvailable();
                for (Long aid : aids) {
                    EntityAttachment attachment = db.attachment().getAttachment(aid);
                    if (attachment != null) {
                        File file = attachment.getFile(context);
                        if (!file.exists()) {
                            Log.w("Attachment file missing id=" + aid);
                            db.attachment().setAvailable(aid, false);
                        }
                    }
                }

                // Delete old attachments
                if (cleanup_attachments) {
                    int purged = db.attachment().purge(new Date().getTime());
                    Log.i("Attachments purged=" + purged);

                    // Clear raw headers
                    if (!download_headers) {
                        int headers = db.message().clearMessageHeaders();
                        Log.i("Cleared message headers=" + headers);
                    }

                    // Clear raw message files
                    if (!download_eml) {
                        int eml = db.message().clearRawMessages();
                        Log.i("Cleared raw messages=" + eml);
                    }
                }

                // Restore alarms
                try {
                    for (EntityMessage message : db.message().getSnoozed(null))
                        EntityMessage.snooze(context, message.id, message.ui_snoozed);
                } catch (IllegalArgumentException ex) {
                    Log.w(ex);
                }

                ServiceSynchronize.reschedule(context);

                DnsBlockList.clearCache();
                MessageClassifier.cleanup(context);
                ContactInfo.clearCache(context);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.i("Checking notification channels");
                    NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                    for (NotificationChannel channel : nm.getNotificationChannels()) {
                        String cid = channel.getId();
                        Log.i("Notification channel id=" + cid + " name=" + channel.getName());
                        String[] parts = cid.split("\\.");
                        if (parts.length > 1 && "notification".equals(parts[0]))
                            if (parts.length == 2 && TextUtils.isDigitsOnly(parts[1])) {
                                long id = Integer.parseInt(parts[1]);
                                EntityAccount account = db.account().getAccount(id);
                                Log.i("Notification channel id=" + cid + " account=" + (account == null ? null : account.id));
                                if (account == null)
                                    nm.deleteNotificationChannel(cid);
                            } else if (parts.length == 3 && TextUtils.isDigitsOnly(parts[2])) {
                                long id = Integer.parseInt(parts[2]);
                                EntityFolder folder = db.folder().getFolder(id);
                                Log.i("Notification channel id=" + cid + " folder=" + (folder == null ? null : folder.id));
                                if (folder == null)
                                    nm.deleteNotificationChannel(cid);
                            }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Log.i("Checking for pma files");
                    File pma = new File(context.getDataDir(), "app_webview/BrowserMetrics");
                    File[] files = pma.listFiles();
                    if (files != null)
                        for (File file : files)
                            if (file.getName().endsWith(".pma")) {
                                Log.i("Deleting " + file);
                                file.delete();
                            }
                }
            }

            long now = new Date().getTime();

            List<File> files = new ArrayList<>();
            File[] messages = Helper.listFiles(new File(context.getFilesDir(), "messages")).toArray(new File[0]);
            File[] revision = new File(context.getFilesDir(), "revision").listFiles();
            File[] references = new File(context.getFilesDir(), "references").listFiles();
            File[] encryption = new File(context.getFilesDir(), "encryption").listFiles();
            File[] photos = new File(context.getFilesDir(), "photo").listFiles();
            File[] calendars = new File(context.getFilesDir(), "calendar").listFiles();

            if (messages != null)
                files.addAll(Arrays.asList(messages));
            if (revision != null)
                files.addAll(Arrays.asList(revision));
            if (references != null)
                files.addAll(Arrays.asList(references));
            if (encryption != null)
                files.addAll(Arrays.asList(encryption));
            if (photos != null)
                files.addAll(Arrays.asList(photos));
            if (calendars != null)
                files.addAll(Arrays.asList(calendars));

            // Cleanup message files
            Log.i("Cleanup message files");
            for (File file : files)
                if (manual || file.lastModified() + KEEP_FILES_DURATION < now)
                    try {
                        String name = file.getName().split("\\.")[0];
                        int us = name.indexOf('_');
                        if (us > 0)
                            name = name.substring(0, us);
                        long id = Long.parseLong(name);
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null || !message.content) {
                            Log.i("Deleting " + file);
                            if (!file.delete())
                                Log.w("Error deleting " + file);
                        }
                    } catch (NumberFormatException ex) {
                        Log.e(file.getAbsolutePath(), ex);
                        file.delete();
                    }

            // Cleanup raw message files
            if (!download_eml) {
                Log.i("Cleanup raw message files");
                File[] raws = new File(context.getFilesDir(), "raw").listFiles();
                if (raws != null)
                    for (File file : raws)
                        if (manual || file.lastModified() + KEEP_FILES_DURATION < now)
                            try {
                                long id = Long.parseLong(file.getName().split("\\.")[0]);
                                EntityMessage message = db.message().getMessage(id);
                                if (manual && cleanup_attachments && message != null) {
                                    EntityAccount account = db.account().getAccount(message.account);
                                    if (account != null && account.protocol == EntityAccount.TYPE_IMAP) {
                                        message.raw = false;
                                        db.message().setMessageRaw(message.id, message.raw);
                                    }
                                }
                                if (message == null || message.raw == null || !message.raw) {
                                    Log.i("Deleting " + file);
                                    if (!file.delete())
                                        Log.w("Error deleting " + file);
                                }
                            } catch (NumberFormatException ex) {
                                Log.e(file.getAbsolutePath(), ex);
                                file.delete();
                            }
            }

            // Cleanup attachment files
            Log.i("Cleanup attachment files");
            File[] attachments = new File(EntityAttachment.getRoot(context), "attachments").listFiles();
            if (attachments != null)
                for (File file : attachments)
                    if (manual || file.lastModified() + KEEP_FILES_DURATION < now)
                        try {
                            long id = Long.parseLong(file.getName().split("\\.")[0]);
                            EntityAttachment attachment = db.attachment().getAttachment(id);
                            if (attachment == null || !attachment.available) {
                                Log.i("Deleting " + file);
                                if (!file.delete())
                                    Log.w("Error deleting " + file);
                            }
                        } catch (NumberFormatException ex) {
                            Log.e(file.getAbsolutePath(), ex);
                            file.delete();
                        }

            // Cleanup cached images
            Log.i("Cleanup cached image files");
            File[] images = new File(context.getFilesDir(), "images").listFiles();
            if (images != null)
                for (File file : images)
                    if (manual || file.lastModified() + KEEP_FILES_DURATION < now)
                        try {
                            long id = Long.parseLong(file.getName().split("[_\\.]")[0]);
                            EntityMessage message = db.message().getMessage(id);
                            if (manual || message == null ||
                                    file.lastModified() + KEEP_IMAGES_DURATION < now) {
                                Log.i("Deleting " + file);
                                if (!file.delete())
                                    Log.w("Error deleting " + file);
                            }
                        } catch (NumberFormatException ex) {
                            Log.e(file.getAbsolutePath(), ex);
                            file.delete();
                        }

            // Cleanup shared files
            File[] shared = new File(context.getFilesDir(), "shared").listFiles();
            if (shared != null)
                for (File file : shared)
                    if (manual || file.lastModified() + KEEP_FILES_DURATION < now) {
                        Log.i("Deleting " + file);
                        if (!file.delete())
                            Log.w("Error deleting " + file);
                    }

            // Cleanup contact info
            if (manual)
                ContactInfo.clearCache(context);
            else
                ContactInfo.cleanup(context);

            Log.i("Cleanup FTS=" + fts);
            if (fts) {
                int deleted = 0;
                SQLiteDatabase sdb = Fts4DbHelper.getInstance(context);
                try (Cursor cursor = Fts4DbHelper.getIds(sdb)) {
                    while (cursor.moveToNext()) {
                        long rowid = cursor.getLong(0);
                        EntityMessage message = db.message().getMessage(rowid);
                        if (message == null || !message.fts) {
                            Log.i("Deleting FTS rowid=" + rowid);
                            Fts4DbHelper.delete(sdb, rowid);
                            deleted++;
                        }
                    }
                }
                Log.i("Cleanup FTS=" + deleted);
                if (manual)
                    Fts4DbHelper.optimize(sdb);
            }

            Log.i("Cleanup contacts");
            try {
                db.beginTransaction();
                int contacts = db.contact().countContacts();
                int deleted = (contacts < KEEP_CONTACTS_COUNT ? 0 :
                        db.contact().deleteContacts(now - KEEP_CONTACTS_DURATION));
                db.setTransactionSuccessful();
                Log.i("Contacts=" + contacts + " deleted=" + deleted);
            } finally {
                db.endTransaction();
            }

            if (sqlite_analyze) {
                // https://sqlite.org/lang_analyze.html
                Log.i("Running analyze");
                long analyze = new Date().getTime();
                try (Cursor cursor = db.getOpenHelper().getWritableDatabase().query("PRAGMA analysis_limit=1000; PRAGMA optimize;")) {
                    cursor.moveToNext();
                }
                EntityLog.log(context, "Analyze=" + (new Date().getTime() - analyze) + " ms");
            }

            DB.createEmergencyBackup(context);

            if (manual) {
                // https://www.sqlite.org/lang_vacuum.html
                long size = context.getDatabasePath(db.getOpenHelper().getDatabaseName()).length();
                long available = Helper.getAvailableStorageSpace();
                if (size > 0 && size * 2.5 < available) {
                    Log.i("Running VACUUM" +
                            " size=" + Helper.humanReadableByteCount(size) +
                            "/" + Helper.humanReadableByteCount(available));
                    db.getOpenHelper().getWritableDatabase().execSQL("VACUUM;");
                } else
                    Log.w("Insufficient space for VACUUM" +
                            " size=" + Helper.humanReadableByteCount(size) +
                            "/" + Helper.humanReadableByteCount(available));
            }
        } catch (Throwable ex) {
            Log.e(ex);
        } finally {
            semaphore.release();
            EntityLog.log(context, "End cleanup=" + (new Date().getTime() - start) + " ms");

            long now = new Date().getTime();
            prefs.edit()
                    .remove("crash_report_count")
                    .putLong("last_cleanup", now)
                    .apply();
        }
    }

    static void init(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean enabled = prefs.getBoolean("enabled", true);
            if (enabled) {
                Log.i("Queuing " + getName() + " every " + CLEANUP_INTERVAL + " hours");

                PeriodicWorkRequest workRequest =
                        new PeriodicWorkRequest.Builder(WorkerCleanup.class, CLEANUP_INTERVAL, TimeUnit.HOURS)
                                .setInitialDelay(CLEANUP_INTERVAL, TimeUnit.HOURS)
                                .build();
                WorkManager.getInstance(context)
                        .enqueueUniquePeriodicWork(getName(), ExistingPeriodicWorkPolicy.KEEP, workRequest);

                Log.i("Queued " + getName());
            } else {
                Log.i("Cancelling " + getName());
                WorkManager.getInstance(context).cancelUniqueWork(getName());
                Log.i("Cancelled " + getName());
            }
        } catch (IllegalStateException ex) {
            // https://issuetracker.google.com/issues/138465476
            Log.w(ex);
        }
    }

    private static String getName() {
        return WorkerCleanup.class.getSimpleName();
    }
}
