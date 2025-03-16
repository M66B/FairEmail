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

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.Date;
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
                    EntityLog.log(context, "Executing daily rules for " + account.name + "/" + folder.name);

                    List<EntityRule> rules = db.rule().getEnabledRules(folder.id, true);
                    if (rules == null || rules.size() == 0)
                        continue;
                    EntityLog.log(context, "Executing daily rules count=" + rules.size());

                    int count = 0;
                    List<Long> ids = db.message().getMessageIdsByFolder(folder.id);
                    if (ids == null || ids.size() == 0)
                        continue;
                    EntityLog.log(context, "Executing daily rules messages=" + ids.size());

                    for (long mid : ids)
                        try {
                            db.beginTransaction();

                            EntityMessage message = db.message().getMessage(mid);
                            if (message == null || message.ui_hide)
                                continue;
                            count++;

                            boolean defer = false;
                            boolean needsHeaders = EntityRule.needsHeaders(message, rules);
                            boolean needsBody = EntityRule.needsBody(message, rules);

                            if (needsHeaders && message.headers == null) {
                                defer = true;
                                EntityLog.log(context, "Deferring daily rules for headers message=" + message.id);
                                EntityOperation.queue(context, message, EntityOperation.HEADERS);
                            }

                            if (needsBody && !message.content) {
                                defer = true;
                                EntityLog.log(context, "Deferring daily rules for body message=" + message.id);
                                EntityOperation.queue(context, message, EntityOperation.BODY);
                            }

                            if (defer)
                                EntityOperation.queue(context, message, EntityOperation.RULE, -1L, false);
                            else {
                                EntityLog.log(context, "Executing daily rules message=" + message.id);
                                EntityRule.run(context, rules, message, false, null, null);
                            }

                            db.setTransactionSuccessful();
                        } catch (Throwable ex) {
                            Log.e(ex);
                        } finally {
                            db.endTransaction();
                        }

                    EntityLog.log(context, EntityLog.Type.Rules, folder,
                            "Executed " + count + " daily rules for " + account.name + "/" + folder.name);
                }
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putLong("last_daily", new Date().getTime()).apply();
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
                long now = cal.getTimeInMillis();
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.HOUR_OF_DAY, 1);
                long delay = cal.getTimeInMillis() - now;
                if (delay < 0)
                    cal.add(Calendar.DATE, 1);
                delay = cal.getTimeInMillis() - now;

                EntityLog.log(context, EntityLog.Type.Rules, "Queuing " + getName() + " delay=" + (delay / (60 * 1000L)) + "m");
                WorkManager.getInstance(context).cancelUniqueWork(getName());
                PeriodicWorkRequest.Builder builder =
                        new PeriodicWorkRequest.Builder(WorkerDailyRules.class, 1, TimeUnit.DAYS)
                                .setInitialDelay(delay, TimeUnit.MILLISECONDS);
                WorkManager.getInstance(context)
                        .enqueueUniquePeriodicWork(getName(), ExistingPeriodicWorkPolicy.KEEP, builder.build());
                EntityLog.log(context, EntityLog.Type.Rules, "Queued " + getName());
            } else {
                EntityLog.log(context, EntityLog.Type.Rules, "Cancelling " + getName());
                WorkManager.getInstance(context).cancelUniqueWork(getName());
                EntityLog.log(context, EntityLog.Type.Rules, "Cancelled " + getName());
            }
        } catch (Throwable ex) {
            // https://issuetracker.google.com/issues/138465476
            Log.w(ex);
        }
    }

    private static String getName() {
        return WorkerDailyRules.class.getSimpleName();
    }
}
