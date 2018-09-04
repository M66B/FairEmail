package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

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
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobDaily extends JobService {
    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    public static void schedule(Context context) {
        Log.i(Helper.TAG, "Scheduling daily job");

        JobInfo.Builder job = new JobInfo.Builder(Helper.JOB_DAILY, new ComponentName(context, JobDaily.class))
                .setPeriodic(24 * 3600 * 1000L)
                .setRequiresDeviceIdle(true);

        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        scheduler.cancel(Helper.JOB_DAILY);
        if (scheduler.schedule(job.build()) == JobScheduler.RESULT_SUCCESS)
            Log.i(Helper.TAG, "Scheduled daily job");
        else
            Log.e(Helper.TAG, "Failed to schedule daily job");
    }

    @Override
    public boolean onStartJob(JobParameters args) {
        Log.i(Helper.TAG, "Starting daily job");

        final DB db = DB.getInstance(this);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(Helper.TAG, "Start daily job");

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

                // Cleanup found messages
                Log.i(Helper.TAG, "Cleanup found messages");
                int found = db.message().deleteFoundMessages();
                Log.i(Helper.TAG, "Deleted found messages=" + found);

                Log.i(Helper.TAG, "End daily job");
            }
        });

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters args) {
        return false;
    }
}

