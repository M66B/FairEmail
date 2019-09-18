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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class ActivityBase extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context originalContext;
    private boolean contacts;
    private List<IBackPressedListener> backPressedListeners = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context base) {
        originalContext = base;
        super.attachBaseContext(ApplicationEx.getLocalizedContext(base));
    }

    Context getOriginalContext() {
        return originalContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Create " + this.getClass().getName() + " version=" + BuildConfig.VERSION_NAME);
        Intent intent = getIntent();
        if (intent != null) {
            Log.i(intent.toString());
            Log.logBundle(intent.getExtras());
        }

        this.contacts = hasPermission(Manifest.permission.READ_CONTACTS);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!this.getClass().equals(ActivityMain.class)) {
            String theme = prefs.getString("theme", "light");

            if ("dark".equals(theme))
                setTheme(R.style.AppThemeDark);
            else if ("black".equals(theme))
                setTheme(R.style.AppThemeBlack);
            else if ("grey_light".equals(theme))
                setTheme(R.style.AppThemeGreyLight);
            else if ("grey_dark".equals(theme))
                setTheme(R.style.AppThemeGreyDark);
            else if ("system".equals(theme)) {
                int uiMode = getResources().getConfiguration().uiMode;
                Log.i("UI mode=" + uiMode);
                if ((uiMode & Configuration.UI_MODE_NIGHT_YES) != 0)
                    setTheme(R.style.AppThemeBlack);
            } else if ("grey_system".equals(theme)) {
                int uiMode = getResources().getConfiguration().uiMode;
                Log.i("UI mode=" + uiMode);
                if ((uiMode & Configuration.UI_MODE_NIGHT_YES) != 0)
                    setTheme(R.style.AppThemeGreyDark);
                else
                    setTheme(R.style.AppThemeGreyLight);
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        int before = Helper.getSize(outState);
        super.onSaveInstanceState(outState);
        int after = Helper.getSize(outState);
        Log.i("Saved instance " + this + " size=" + before + "/" + after);

        Map<String, String> crumb = new HashMap<>();
        crumb.put("name", this.getClass().getName());
        crumb.put("before", Integer.toString(before));
        crumb.put("after", Integer.toString(after));
        Log.breadcrumb("onSaveInstanceState", crumb);

        for (String key : outState.keySet())
            Log.i("Saved " + this + " " + key + "=" + outState.get(key));
    }

    @Override
    protected void onResume() {
        Log.i("Resume " + this.getClass().getName());

        boolean contacts = hasPermission(Manifest.permission.READ_CONTACTS);
        if (!this.getClass().equals(ActivitySetup.class) && this.contacts != contacts) {
            Log.i("Contacts permission=" + contacts);
            finish();
            startActivity(getIntent());
        } else if (!this.getClass().equals(ActivityMain.class) && Helper.shouldAuthenticate(this)) {
            Intent intent = getIntent();
            finish();
            startActivity(
                    new Intent(this, ActivityMain.class)
                            .putExtra("intent", intent));
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i("Pause " + this.getClass().getName());
        super.onPause();

        if (!this.getClass().equals(ActivityMain.class) && Helper.shouldAuthenticate(this))
            finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i("Config " + this.getClass().getName());
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onUserInteraction() {
        Log.i("User interaction");

        if (!this.getClass().equals(ActivityMain.class) && Helper.shouldAuthenticate(this)) {
            finish();
            startActivity(new Intent(this, ActivityMain.class));
        }
    }

    @Override
    protected void onUserLeaveHint() {
        Log.i("User leaving");
    }

    @Override
    protected void onStop() {
        super.onStop();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && !pm.isInteractive()) {
            Log.i("Stop with screen off");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean biometrics = prefs.getBoolean("biometrics", false);
            if (biometrics) {
                Helper.clearAuthentication(this);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("Destroy " + this.getClass().getName());
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i("Result class=" + this.getClass().getSimpleName() +
                " request=" + requestCode + " result=" + resultCode);
        Log.logExtras(data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            if (Helper.hasAuthentication(this))
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            super.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(ex);
            ToastEx.makeText(this, getString(R.string.title_no_viewer, intent.getAction()), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            if (Helper.hasAuthentication(this))
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            super.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException ex) {
            Log.e(ex);
            ToastEx.makeText(this, getString(R.string.title_no_viewer, intent.getAction()), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.i("Preference " + key + "=" + prefs.getAll().get(key));
        if ("theme".equals(key)) {
            finish();
            if (this.getClass().equals(ActivitySetup.class))
                startActivity(getIntent());
        } else if (!this.getClass().equals(ActivitySetup.class) &&
                Arrays.asList(FragmentOptions.OPTIONS_RESTART).contains(key))
            finish();
    }

    public boolean hasPermission(String name) {
        return Helper.hasPermission(this, name);
    }

    void addBackPressedListener(final IBackPressedListener listener, LifecycleOwner owner) {
        Log.i("Adding back listener=" + listener);
        backPressedListeners.add(listener);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.i("Removing back listener=" + listener);
                backPressedListeners.remove(listener);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backHandled())
            return;
        super.onBackPressed();
    }

    protected boolean backHandled() {
        for (IBackPressedListener listener : backPressedListeners)
            if (listener.onBackPressed())
                return true;
        return false;
    }

    public interface IBackPressedListener {
        boolean onBackPressed();
    }
}
