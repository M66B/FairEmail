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
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WorkerCleanup extends Worker {
    private static final int CLEANUP_INTERVAL = 4; // hours
    private static final long CACHE_IMAGE_DURATION = 3 * 24 * 3600 * 1000L; // milliseconds
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
        cleanup(getApplicationContext(), false);
        return Result.success();
    }

    static void cleanup(Context context, boolean manual) {
        DB db = DB.getInstance(context);
        try {
            Log.i("Start cleanup manual=" + manual);

            // Cleanup folders
            Log.i("Cleanup kept messages");
            for (EntityFolder folder : db.folder().getFolders()) {
                Calendar cal_keep = Calendar.getInstance();
                cal_keep.add(Calendar.DAY_OF_MONTH, -folder.keep_days);
                cal_keep.set(Calendar.HOUR_OF_DAY, 12);
                cal_keep.set(Calendar.MINUTE, 0);
                cal_keep.set(Calendar.SECOND, 0);
                cal_keep.set(Calendar.MILLISECOND, 0);

                long keep_time = cal_keep.getTimeInMillis();
                if (keep_time < 0)
                    keep_time = 0;

                int messages = db.message().deleteMessagesBefore(folder.id, keep_time, false);
                if (messages > 0)
                    Log.i("Cleanup folder=" + folder.account + "/" + folder.name +
                            " before=" + new Date(keep_time) + " deleted=" + messages);
            }

            long now = new Date().getTime();

            List<File> files = new ArrayList<>();
            File[] messages = new File(context.getFilesDir(), "messages").listFiles();
            File[] revision = new File(context.getFilesDir(), "revision").listFiles();
            File[] references = new File(context.getFilesDir(), "references").listFiles();
            File[] raws = new File(context.getFilesDir(), "raw").listFiles();

            if (messages != null)
                files.addAll(Arrays.asList(messages));
            if (revision != null)
                files.addAll(Arrays.asList(revision));
            if (references != null)
                files.addAll(Arrays.asList(references));
            if (raws != null)
                files.addAll(Arrays.asList(raws));

            // Cleanup message files
            Log.i("Cleanup message files");
            for (File file : files) {
                long id = Long.parseLong(file.getName().split("\\.")[0]);
                if (db.message().countMessage(id) == 0) {
                    Log.i("Deleting " + file);
                    if (!file.delete())
                        Log.w("Error deleting " + file);
                }
            }

            // Cleanup attachment files
            Log.i("Cleanup attachment files");
            File[] attachments = new File(context.getFilesDir(), "attachments").listFiles();
            if (attachments != null)
                for (File file : attachments) {
                    long id = Long.parseLong(file.getName().split("\\.")[0]);
                    if (db.attachment().countAttachment(id) == 0) {
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
                    if (file.isFile())
                        if (manual || now - file.lastModified() > CACHE_IMAGE_DURATION) {
                            Log.i("Deleting " + file);
                            if (!file.delete())
                                Log.w("Error deleting " + file);
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
        Log.i("Queuing " + getName() + " every " + CLEANUP_INTERVAL + " hours");

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(WorkerCleanup.class, CLEANUP_INTERVAL, TimeUnit.HOURS)
                        .build();
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(getName(), ExistingPeriodicWorkPolicy.REPLACE, workRequest);

        Log.i("Queued " + getName());
    }

    static void cancel(Context context) {
        Log.i("Cancelling " + getName());
        WorkManager.getInstance(context).cancelUniqueWork(getName());
        Log.i("Cancelled " + getName());
    }

    private static String getName() {
        return WorkerCleanup.class.getSimpleName();
    }
}
