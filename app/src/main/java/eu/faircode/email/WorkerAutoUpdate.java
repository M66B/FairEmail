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

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class WorkerAutoUpdate extends Worker {
    private static final long UPDATE_INTERVAL = 7; // Days

    public WorkerAutoUpdate(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.i("Instance " + getName());
    }

    @NonNull
    @Override
    public Result doWork() {
        Thread.currentThread().setPriority(THREAD_PRIORITY_BACKGROUND);

        try {
            Log.i("Auto updating");
            DisconnectBlacklist.download(getApplicationContext());
            Log.i("Auto updated");
            return Result.success();
        } catch (Throwable ex) {
            Log.e(ex);
            return Result.failure();
        }
    }

    static void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean auto_update = prefs.getBoolean("disconnect_auto_update", false);
        try {
            if (auto_update) {
                Log.i("Queuing " + getName());
                PeriodicWorkRequest.Builder builder =
                        new PeriodicWorkRequest.Builder(WorkerAutoUpdate.class, UPDATE_INTERVAL, TimeUnit.DAYS)
                                .setConstraints(new Constraints.Builder()
                                        .setRequiredNetworkType(NetworkType.CONNECTED).build());
                WorkManager.getInstance(context)
                        .enqueueUniquePeriodicWork(getName(), ExistingPeriodicWorkPolicy.KEEP, builder.build());
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
        return WorkerAutoUpdate.class.getSimpleName();
    }
}
