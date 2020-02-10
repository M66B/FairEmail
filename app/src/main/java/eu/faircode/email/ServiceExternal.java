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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ServiceExternal extends Service {
    private static final String ACTION_POLL = BuildConfig.APPLICATION_ID + ".POLL";
    private static final String ACTION_ENABLE = BuildConfig.APPLICATION_ID + ".ENABLE";
    private static final String ACTION_DISABLE = BuildConfig.APPLICATION_ID + ".DISABLE";

    // adb shell am startservice -a eu.faircode.email.POLL --es account Gmail
    // adb shell am startservice -a eu.faircode.email.ENABLE --es account Gmail
    // adb shell am startservice -a eu.faircode.email.DISABLE --es account Gmail

    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "external");


    @Override
    public void onCreate() {
        Log.i("Service external create");
        super.onCreate();
        startForeground(Helper.NOTIFICATION_EXTERNAL, getNotification().build());
    }

    @Override
    public void onDestroy() {
        Log.i("Service external destroy");
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            EntityLog.log(this, "Service external intent=" + intent);
            Log.logExtras(intent);

            super.onStartCommand(intent, flags, startId);
            startForeground(Helper.NOTIFICATION_EXTERNAL, getNotification().build());

            if (intent == null)
                return START_NOT_STICKY;

            if (!ActivityBilling.isPro(this))
                return START_NOT_STICKY;

            final Context context = getApplicationContext();
            final String accountName = intent.getStringExtra("account");

            final Boolean enabled;
            String action = intent.getAction();
            if (ACTION_ENABLE.equals(action))
                enabled = true;
            else if (ACTION_DISABLE.equals(action))
                enabled = false;
            else // poll
                enabled = null;

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    DB db = DB.getInstance(context);

                    if (enabled == null) {
                        List<EntityAccount> accounts = db.account().getSynchronizingAccounts();
                        for (EntityAccount account : accounts)
                            if (accountName == null || accountName.equals(account.name)) {
                                List<EntityFolder> folders = db.folder().getSynchronizingFolders(account.id);
                                if (folders.size() > 0)
                                    Collections.sort(folders, folders.get(0).getComparator(context));
                                for (EntityFolder folder : folders)
                                    EntityOperation.sync(context, folder.id, false);
                            }
                        ServiceSynchronize.eval(context, "external poll account=" + accountName);
                    } else {
                        if (accountName == null) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            prefs.edit().putBoolean("enabled", enabled).apply();
                            ServiceSynchronize.eval(context, "external enabled=" + enabled);
                        } else {
                            EntityAccount account = db.account().getAccount(accountName);
                            if (account == null) {
                                EntityLog.log(context, "Account not found name=" + accountName);
                                return;
                            }

                            db.account().setAccountSynchronize(account.id, enabled);
                            ServiceSynchronize.eval(context, "external account=" + accountName + " enabled=" + enabled);
                        }
                    }
                }
            });

            return START_NOT_STICKY;
        } finally {
            stopSelf(startId);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private NotificationCompat.Builder getNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "service")
                        .setSmallIcon(R.drawable.baseline_compare_arrows_white_24)
                        .setContentTitle(getString(R.string.tile_synchronize))
                        .setAutoCancel(false)
                        .setShowWhen(false)
                        .setDefaults(0) // disable sound on pre Android 8
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                        .setLocalOnly(true);

        return builder;
    }
}
