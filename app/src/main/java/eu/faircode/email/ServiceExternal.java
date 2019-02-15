package eu.faircode.email;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

public class ServiceExternal extends Service {
    private final static String ACTION_ENABLE = "eu.faircode.email.ENABLE";
    private final static String ACTION_DISABLE = "eu.faircode.email.DISABLE";

    // adb shell am startservice -a eu.faircode.email.ENABLE
    // adb shell am startservice -a eu.faircode.email.DISABLE


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            startForeground(Helper.NOTIFICATION_EXTERNAL, getNotification().build());

            Log.i("Received intent=" + intent);
            Log.logExtras(intent);

            if (intent == null)
                return START_NOT_STICKY;

            if (!Helper.isPro(this))
                return START_NOT_STICKY;

            Boolean enabled = null;
            if (ACTION_ENABLE.equals(intent.getAction()))
                enabled = true;
            else if (ACTION_DISABLE.equals(intent.getAction()))
                enabled = false;

            if (enabled != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putBoolean("schedule", false).apply();

                boolean previous = prefs.getBoolean("enabled", true);
                if (!enabled.equals(previous)) {
                    prefs.edit().putBoolean("enabled", enabled).apply();
                    ServiceSynchronize.reload(this, "external");
                }
            }

            return START_NOT_STICKY;
        } finally {
            stopForeground(true);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification.Builder getNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this, "service");
        else
            builder = new Notification.Builder(this);

        builder
                .setSmallIcon(R.drawable.baseline_compare_arrows_white_24)
                .setContentTitle(getString(R.string.tile_synchronize))
                .setAutoCancel(false)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_SECRET);

        return builder;
    }
}
