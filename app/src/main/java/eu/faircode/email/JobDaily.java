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

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobDaily extends JobService {
    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private static final long CLEANUP_INTERVAL = 4 * 3600 * 1000L; // milliseconds
    private static final long CACHE_IMAGE_DURATION = 3 * 24 * 3600 * 1000L; // milliseconds
    private static final long KEEP_LOG_DURATION = 24 * 3600 * 1000L; // milliseconds

    public static void schedule(Context context) {
        Log.i("Scheduling daily job");

        JobInfo.Builder job = new JobInfo.Builder(Helper.JOB_DAILY, new ComponentName(context, JobDaily.class))
                .setPeriodic(CLEANUP_INTERVAL)
                .setRequiresDeviceIdle(true);

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(Helper.JOB_DAILY);
        if (scheduler.schedule(job.build()) == JobScheduler.RESULT_SUCCESS)
            Log.i("Scheduled daily job");
        else
            Log.e("Scheduling daily job failed");
    }

    @Override
    public boolean onStartJob(JobParameters args) {
        EntityLog.log(this, "Daily cleanup");

        executor.submit(new Runnable() {
            @Override
            public void run() {
                cleanup(getApplicationContext(), false);
            }
        });

        return false;
    }

    static void cleanup(Context context, boolean manual) {
        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            Log.i("Start daily job manual=" + manual);

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
            File[] references = new File(context.getFilesDir(), "references").listFiles();
            File[] raws = new File(context.getFilesDir(), "raw").listFiles();

            if (messages != null)
                files.addAll(Arrays.asList(messages));
            if (references != null)
                files.addAll(Arrays.asList(references));
            if (raws != null)
                files.addAll(Arrays.asList(raws));

            // Cleanup message files
            Log.i("Cleanup message files");
            for (File file : files) {
                long id = Long.parseLong(file.getName());
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
                    long id = Long.parseLong(file.getName());
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

            Log.i("Cleanup log");
            long before = now - KEEP_LOG_DURATION;
            int logs = db.log().deleteLogs(before);
            Log.i("Deleted logs=" + logs);

            db.setTransactionSuccessful();
        } catch (Throwable ex) {
            Log.e(ex);
        } finally {
            db.endTransaction();
            Log.i("End daily job");
        }
    }

    @Override
    public boolean onStopJob(JobParameters args) {
        return false;
    }
}

