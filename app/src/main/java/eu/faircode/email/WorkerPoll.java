package eu.faircode.email;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class WorkerPoll extends Worker {
    public WorkerPoll(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.i("Instance " + getName());
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i("Running " + getName());
        ServiceSynchronize.onshot(getApplicationContext());
        return Result.success();
    }

    static void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = prefs.getInt("poll_interval", 0);
        if (enabled && pollInterval > 0) {
            Log.i("Queuing " + getName() + " every " + pollInterval + " minutes");
            PeriodicWorkRequest workRequest =
                    new PeriodicWorkRequest.Builder(WorkerPoll.class, pollInterval, TimeUnit.MINUTES)
                            .build();
            WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(getName(), ExistingPeriodicWorkPolicy.REPLACE, workRequest);

            Log.i("Queued " + getName());
        } else {

            Log.i("Cancelling " + getName());
            WorkManager.getInstance(context).cancelUniqueWork(getName());
            Log.i("Cancelled " + getName());
        }
    }

    private static String getName() {
        return WorkerPoll.class.getSimpleName();
    }
}
