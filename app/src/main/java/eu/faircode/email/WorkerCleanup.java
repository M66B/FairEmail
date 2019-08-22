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

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class WorkerCleanup extends Worker {
    private static final int CLEANUP_INTERVAL = 4; // hours
    private static final long KEEP_FILES_DURATION = 3600 * 1000L; // milliseconds
    private static final long KEEP_CONTACTS_DURATION = 180 * 24 * 3600 * 1000L; // milliseconds
    private static final long KEEP_LOG_DURATION = 24 * 3600 * 1000L; // milliseconds

    public WorkerCleanup(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.i("Instance " + getName());
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i("Running " + getName());
        cleanup(getApplicationContext(), getInputData().getBoolean("manual", false));
        return Result.success();
    }

    static void cleanup(Context context, boolean manual) {
        DB db = DB.getInstance(context);
        try {
            Log.i("Start cleanup manual=" + manual);
            Thread.currentThread().setPriority(THREAD_PRIORITY_BACKGROUND);

            if (manual) {
                // Check message files
                Log.i("Checking message files");
                List<Long> mids = db.message().getMessageWithContent();
                File dir = new File(context.getFilesDir(), "messages");
                if (!dir.exists())
                    dir.mkdir();
                for (Long mid : mids) {
                    File file = new File(dir, mid.toString());
                    if (!file.exists()) {
                        Log.w("Message file missing id=" + mid);
                        db.message().setMessageContent(mid, false);
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

                // Restore alarms
                for (EntityMessage message : db.message().getSnoozed())
                    EntityMessage.snooze(context, message.id, message.ui_snoozed);
            }

            long now = new Date().getTime();

            List<File> files = new ArrayList<>();
            File[] messages = new File(context.getFilesDir(), "messages").listFiles();
            File[] revision = new File(context.getFilesDir(), "revision").listFiles();
            File[] references = new File(context.getFilesDir(), "references").listFiles();

            if (messages != null)
                files.addAll(Arrays.asList(messages));
            if (revision != null)
                files.addAll(Arrays.asList(revision));
            if (references != null)
                files.addAll(Arrays.asList(references));

            // Cleanup message files
            Log.i("Cleanup message files");
            for (File file : files)
                if (manual || file.lastModified() + KEEP_FILES_DURATION < now) {
                    long id = Long.parseLong(file.getName().split("\\.")[0]);
                    Boolean content = db.message().getMessageByIdHasContent(id);
                    if (content == null || !content) {
                        Log.i("Deleting " + file);
                        if (!file.delete())
                            Log.w("Error deleting " + file);
                    }
                }

            // Cleanup message files
            Log.i("Cleanup raw message files");
            File[] raws = new File(context.getFilesDir(), "raw").listFiles();
            if (raws != null)
                for (File file : raws)
                    if (manual || file.lastModified() + KEEP_FILES_DURATION < now) {
                        long id = Long.parseLong(file.getName().split("\\.")[0]);
                        Boolean raw = db.message().getMessageByIdHasRaw(id);
                        if (raw == null || !raw) {
                            Log.i("Deleting " + file);
                            if (!file.delete())
                                Log.w("Error deleting " + file);
                        }
                    }

            // Cleanup attachment files
            Log.i("Cleanup attachment files");
            File[] attachments = new File(context.getFilesDir(), "attachments").listFiles();
            if (attachments != null)
                for (File file : attachments)
                    if (manual || file.lastModified() + KEEP_FILES_DURATION < now) {
                        long id = Long.parseLong(file.getName().split("\\.")[0]);
                        EntityAttachment attachment = db.attachment().getAttachment(id);
                        if (attachment == null || !attachment.available) {
                            Log.i("Deleting " + file);
                            if (!file.delete())
                                Log.w("Error deleting " + file);
                        }
                    }

            // Cleanup cached images
            Log.i("Cleanup cached image files");
            File[] images = new File(context.getCacheDir(), "images").listFiles();
            if (images != null)
                for (File file : images)
                    if (manual || file.lastModified() + KEEP_FILES_DURATION < now) {
                        long id = Long.parseLong(file.getName().split("_")[0]);
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null) {
                            Log.i("Deleting " + file);
                            if (!file.delete())
                                Log.w("Error deleting " + file);
                        }
                    }

            Log.i("Cleanup contacts");
            int contacts = db.contact().deleteContacts(now - KEEP_CONTACTS_DURATION);
            Log.i("Deleted contacts=" + contacts);

            Log.i("Cleanup log");
            int logs = db.log().deleteLogs(now - KEEP_LOG_DURATION);
            Log.i("Deleted logs=" + logs);
        } catch (Throwable ex) {
            Log.e(ex);
        } finally {
            Log.i("End cleanup");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putLong("last_cleanup", new Date().getTime()).apply();
        }
    }

    static void queue(Context context) {
        try {
            Log.i("Queuing " + getName() + " every " + CLEANUP_INTERVAL + " hours");

            PeriodicWorkRequest workRequest =
                    new PeriodicWorkRequest.Builder(WorkerCleanup.class, CLEANUP_INTERVAL, TimeUnit.HOURS)
                            .setInitialDelay(CLEANUP_INTERVAL, TimeUnit.HOURS)
                            .build();
            WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(getName(), ExistingPeriodicWorkPolicy.KEEP, workRequest);

            Log.i("Queued " + getName());
        } catch (IllegalStateException ex) {
            // https://issuetracker.google.com/issues/138465476
            Log.w(ex);
        }
    }

    static void queueOnce(Context context) {
        try {
            Log.i("Queuing " + getName() + " once");

            Data data = new Data.Builder().putBoolean("manual", true).build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(WorkerCleanup.class)
                    .setInputData(data)
                    .build();
            WorkManager.getInstance(context).enqueue(workRequest);

            Log.i("Queued " + getName() + " once");
        } catch (IllegalStateException ex) {
            // https://issuetracker.google.com/issues/138465476
            Log.w(ex);
        }
    }

    static void cancel(Context context) {
        try {
            Log.i("Cancelling " + getName());
            WorkManager.getInstance(context).cancelUniqueWork(getName());
            Log.i("Cancelled " + getName());
        } catch (IllegalStateException ex) {
            Log.w(ex);
        }
    }

    private static String getName() {
        return WorkerCleanup.class.getSimpleName();
    }
}
