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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class WorkerDailyRules extends Worker {
    private static final Semaphore semaphore = new Semaphore(1);

    public WorkerDailyRules(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.i("Instance " + getName());
    }

    @NonNull
    @Override
    public Result doWork() {
        Thread.currentThread().setPriority(THREAD_PRIORITY_BACKGROUND);

        try {
            daily(getApplicationContext());
            return Result.success();
        } catch (Throwable ex) {
            Log.e(ex);
            return Result.failure();
        }
    }

    static void daily(Context context) {
        DB db = DB.getInstance(context);
        try {
            semaphore.acquire();
            EntityLog.log(context, EntityLog.Type.Rules, "Running daily rules");

            List<EntityAccount> accounts = db.account().getSynchronizingAccounts(null);
            for (EntityAccount account : accounts) {
                List<EntityFolder> folders = db.folder().getFolders(account.id, false, false);
                for (EntityFolder folder : folders) {
                    List<EntityRule> rules = db.rule().getEnabledRules(folder.id, true);
                    if (rules.size() == 0)
                        continue;

                    int count = 0;
                    List<Long> mids = db.message().getMessageIdsByFolder(folder.id);
                    for (long mid : mids)
                        try {
                            EntityMessage message = db.message().getMessage(mid);
                            if (message == null || message.ui_hide)
                                continue;
                            count++;

                            boolean defer = false;
                            boolean needsHeaders = EntityRule.needsHeaders(message, rules);
                            boolean needsBody = EntityRule.needsBody(message, rules);

                            if (needsHeaders && message.headers == null) {
                                defer = true;
                                EntityOperation.queue(context, message, EntityOperation.HEADERS);
                            }

                            if (needsBody && !message.content) {
                                defer = true;
                                EntityOperation.queue(context, message, EntityOperation.BODY);
                            }

                            if (defer) {
                                EntityOperation.queue(context, message, EntityOperation.RULE, -1L);
                                continue;
                            }

                            for (EntityRule rule : rules)
                                if (rule.matches(context, message, null, null)) {
                                    rule.execute(context, message);
                                    if (rule.stop)
                                        break;
                                }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }

                    EntityLog.log(context, EntityLog.Type.Rules, folder,
                            "Executed " + count + " daily rules for " + account.name + "/" + folder.name);
                }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        } finally {
            semaphore.release();
            EntityLog.log(context, EntityLog.Type.Rules, "Completed daily rules");
        }
    }

    static void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        try {
            if (enabled) {
                Calendar cal = Calendar.getInstance();
                long delay = cal.getTimeInMillis();
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.HOUR_OF_DAY, 1);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                delay = cal.getTimeInMillis() - delay;

                Log.i("Queuing " + getName() + " delay=" + (delay / (60 * 1000L)) + "m");
                PeriodicWorkRequest.Builder builder =
                        new PeriodicWorkRequest.Builder(WorkerDailyRules.class, 1, TimeUnit.DAYS)
                                .setInitialDelay(delay, TimeUnit.MILLISECONDS);
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
        return WorkerDailyRules.class.getSimpleName();
    }
}
