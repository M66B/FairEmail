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

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class WorkerSync extends Worker {
    private static final Semaphore semaphore = new Semaphore(1);

    public WorkerSync(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.i("Instance " + getName());
    }

    @NonNull
    @Override
    public Result doWork() {
        Thread.currentThread().setPriority(THREAD_PRIORITY_BACKGROUND);
        final Context context = getApplicationContext();

        try {
            semaphore.acquire();

            EntityLog.log(context, EntityLog.Type.Rules, "Cloud sync execute");
            CloudSync.execute(context, "sync", false);
            EntityLog.log(context, EntityLog.Type.Rules, "Cloud sync completed");

            return Result.success();
        } catch (Throwable ex) {
            Log.e(ex);
            return Result.failure();
        } finally {
            semaphore.release();
        }
    }

    static void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String user = prefs.getString("cloud_user", null);
        String password = prefs.getString("cloud_password", null);
        boolean enabled = !(TextUtils.isEmpty(BuildConfig.CLOUD_URI) ||
                TextUtils.isEmpty(user) ||
                TextUtils.isEmpty(password));
        Log.i("Cloud worker enabled=" + enabled);
        try {
            if (enabled) {
                Calendar cal = Calendar.getInstance();
                long now = cal.getTimeInMillis();
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 30);
                cal.set(Calendar.HOUR_OF_DAY, 1);
                long delay = cal.getTimeInMillis() - now;
                if (delay < 0)
                    cal.add(Calendar.DATE, 1);
                delay = cal.getTimeInMillis() - now;

                EntityLog.log(context, EntityLog.Type.Cloud,
                        "Queuing " + getName() + " delay=" + (delay / (60 * 1000L)) + "m");
                WorkManager.getInstance(context).cancelUniqueWork(getName());
                PeriodicWorkRequest.Builder builder =
                        new PeriodicWorkRequest.Builder(WorkerSync.class, 1, TimeUnit.DAYS)
                                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                                .setConstraints(new Constraints.Builder()
                                        .setRequiredNetworkType(NetworkType.CONNECTED).build());
                WorkManager.getInstance(context)
                        .enqueueUniquePeriodicWork(getName(), ExistingPeriodicWorkPolicy.KEEP, builder.build());
                Log.i("Queued " + getName());
            } else {
                EntityLog.log(context, EntityLog.Type.Cloud,
                        "Cancelling " + getName());
                WorkManager.getInstance(context).cancelUniqueWork(getName());
                Log.i("Cancelled " + getName());
            }
        } catch (Throwable ex) {
            // https://issuetracker.google.com/issues/138465476
            Log.w(ex);
        }
    }

    private static String getName() {
        return WorkerSync.class.getSimpleName();
    }
}
