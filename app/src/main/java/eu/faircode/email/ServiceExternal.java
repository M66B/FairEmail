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

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ServiceExternal extends Service {
    private static final String ACTION_POLL = BuildConfig.APPLICATION_ID + ".POLL";
    private static final String ACTION_ENABLE = BuildConfig.APPLICATION_ID + ".ENABLE";
    private static final String ACTION_DISABLE = BuildConfig.APPLICATION_ID + ".DISABLE";
    private static final String ACTION_DISCONNECT_ME = BuildConfig.APPLICATION_ID + ".DISCONNECT.ME";

    // adb shell am startservice -a eu.faircode.email.POLL --es account Gmail
    // adb shell am startservice -a eu.faircode.email.ENABLE --es account Gmail
    // adb shell am startservice -a eu.faircode.email.DISABLE --es account Gmail
    // adb shell am startservice -a eu.faircode.email.DISCONNECT

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

            final String action = intent.getAction();
            boolean pro = ActivityBilling.isPro(this);
            EntityLog.log(this, action + " pro=" + pro);

            if (!pro)
                return START_NOT_STICKY;

            final Context context = getApplicationContext();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        switch (action) {
                            case ACTION_POLL:
                                poll(context, intent);
                                break;
                            case ACTION_ENABLE:
                            case ACTION_DISABLE:
                                set(context, intent);
                                break;
                            case ACTION_DISCONNECT_ME:
                                disconnect(context, intent);
                                break;
                            default:
                                throw new IllegalArgumentException(action);
                        }
                    } catch (Throwable ex) {
                        Log.e(ex);
                        EntityLog.log(context, Log.formatThrowable(ex));
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

    private static void poll(Context context, Intent intent) {
        String accountName = intent.getStringExtra("account");

        DB db = DB.getInstance(context);
        List<EntityAccount> accounts;
        if (accountName == null)
            accounts = db.account().getSynchronizingAccounts();
        else {
            EntityAccount account = db.account().getAccount(accountName);
            if (account == null)
                throw new IllegalArgumentException("Account not found name=" + accountName);
            accounts = new ArrayList<>();
            accounts.add(account);
        }

        for (EntityAccount account : accounts) {
            List<EntityFolder> folders = db.folder().getSynchronizingFolders(account.id);
            if (folders.size() > 0)
                Collections.sort(folders, folders.get(0).getComparator(context));
            for (EntityFolder folder : folders)
                EntityOperation.sync(context, folder.id, false);
        }

        ServiceSynchronize.eval(context, "external poll account=" + accountName);
    }

    private static void set(Context context, Intent intent) {
        String accountName = intent.getStringExtra("account");
        boolean enabled = ACTION_ENABLE.equals(intent.getAction());

        DB db = DB.getInstance(context);
        if (accountName == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putBoolean("enabled", enabled).apply();
        } else {
            EntityAccount account = db.account().getAccount(accountName);
            if (account == null)
                throw new IllegalArgumentException("Account not found name=" + accountName);

            db.account().setAccountSynchronize(account.id, enabled);
            ServiceSynchronize.eval(context, "external account=" + accountName + " enabled=" + enabled);
        }
    }

    private static void disconnect(Context context, Intent intent) throws IOException, JSONException {
        DisconnectBlacklist.download(context);
    }
}
