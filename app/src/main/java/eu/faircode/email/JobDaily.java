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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobDaily extends JobService {
    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private static final long CLEANUP_INTERVAL = 4 * 3600 * 1000L; // milliseconds

    public static void schedule(Context context) {
        Log.i(Helper.TAG, "Scheduling daily job");

        JobInfo.Builder job = new JobInfo.Builder(Helper.JOB_DAILY, new ComponentName(context, JobDaily.class))
                .setPeriodic(CLEANUP_INTERVAL)
                .setRequiresDeviceIdle(true);

        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        scheduler.cancel(Helper.JOB_DAILY);
        if (scheduler.schedule(job.build()) == JobScheduler.RESULT_SUCCESS)
            Log.i(Helper.TAG, "Scheduled daily job");
        else
            Log.e(Helper.TAG, "Scheduling daily job failed");
    }

    @Override
    public boolean onStartJob(JobParameters args) {
        EntityLog.log(this, "Daily cleanup");

        final DB db = DB.getInstance(this);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    db.beginTransaction();

                    Log.i(Helper.TAG, "Start daily job");

                    // Cleanup folders
                    Log.i(Helper.TAG, "Cleanup folders");
                    for (EntityFolder folder : db.folder().getFolders()) {
                        Calendar cal_keep = Calendar.getInstance();
                        cal_keep.add(Calendar.DAY_OF_MONTH, -folder.keep_days);
                        cal_keep.set(Calendar.HOUR_OF_DAY, 0);
                        cal_keep.set(Calendar.MINUTE, 0);
                        cal_keep.set(Calendar.SECOND, 0);
                        cal_keep.set(Calendar.MILLISECOND, 0);

                        long keep_time = cal_keep.getTimeInMillis();
                        if (keep_time < 0)
                            keep_time = 0;

                        int messages = db.message().deleteMessagesBefore(folder.id, keep_time, false);
                        Log.i(Helper.TAG, "Cleanup folder=" + folder.account + ":" + folder.name +
                                " deleted before=" + new Date(keep_time) + " count=" + messages);
                    }

                    // Cleanup message files
                    Log.i(Helper.TAG, "Cleanup message files");
                    File[] messages = new File(getFilesDir(), "messages").listFiles();
                    if (messages != null)
                        for (File file : messages)
                            if (file.isFile()) {
                                long id = Long.parseLong(file.getName());
                                if (db.message().countMessage(id) == 0) {
                                    Log.i(Helper.TAG, "Cleanup message id=" + id);
                                    if (!file.delete())
                                        Log.w(Helper.TAG, "Error deleting " + file);
                                }
                            }

                    // Cleanup attachment files
                    Log.i(Helper.TAG, "Cleanup attachment files");
                    File[] attachments = new File(getFilesDir(), "attachments").listFiles();
                    if (attachments != null)
                        for (File file : attachments)
                            if (file.isFile()) {
                                long id = Long.parseLong(file.getName());
                                if (db.attachment().countAttachment(id) == 0) {
                                    Log.i(Helper.TAG, "Cleanup attachment id=" + id);
                                    if (!file.delete())
                                        Log.w(Helper.TAG, "Error deleting " + file);
                                }
                            }

                    Log.i(Helper.TAG, "Cleanup log");
                    long before = new Date().getTime() - 24 * 3600 * 1000L;
                    int logs = db.log().deleteLogs(before);
                    Log.i(Helper.TAG, "Deleted logs=" + logs);

                    db.setTransactionSuccessful();
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                } finally {
                    db.endTransaction();
                    Log.i(Helper.TAG, "End daily job");
                }
            }
        });

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters args) {
        return false;
    }
}

