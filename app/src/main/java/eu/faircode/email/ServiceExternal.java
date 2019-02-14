package eu.faircode.email;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

public class ServiceExternal extends IntentService {
    private final static String ACTION_ENABLE = "eu.faircode.email.ENABLE";
    private final static String ACTION_DISABLE = "eu.faircode.email.DISABLE";

    // adb shell am startservice -a eu.faircode.email.ENABLE
    // adb shell am startservice -a eu.faircode.email.DISABLE

    public ServiceExternal() {
        super(ServiceExternal.class.getName());
    }

    public ServiceExternal(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null)
            return;

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
    }
}
