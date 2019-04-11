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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

abstract class ActivityBase extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean contacts;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ApplicationEx.getLocalizedContext(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Create " + this.getClass().getName() + " version=" + BuildConfig.VERSION_NAME);

        this.contacts = hasPermission(Manifest.permission.READ_CONTACTS);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("theme", null);
        if ("system".equals(theme)) {
            int uiMode = getResources().getConfiguration().uiMode;
            Log.i("UI mode=" + uiMode);
            if ((uiMode & Configuration.UI_MODE_NIGHT_YES) != 0)
                setTheme(R.style.AppThemeDark);
        }
        if ("dark".equals(theme))
            setTheme(R.style.AppThemeDark);
        else if ("black".equals(theme))
            setTheme(R.style.AppThemeBlack);

        prefs.registerOnSharedPreferenceChangeListener(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        int before = Helper.getSize(outState);
        super.onSaveInstanceState(outState);
        int after = Helper.getSize(outState);
        Log.i("Saved instance " + this + " size=" + before + "/" + after);
        for (String key : outState.keySet())
            Log.i("Saved " + key + "=" + outState.get(key));
    }

    @Override
    protected void onResume() {
        Log.i("Resume " + this.getClass().getName());

        boolean contacts = hasPermission(Manifest.permission.READ_CONTACTS);
        if (!this.getClass().equals(ActivitySetup.class) && this.contacts != contacts) {
            Log.i("Contacts permission=" + contacts);
            finish();
            startActivity(getIntent());
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i("Pause " + this.getClass().getName());
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i("Config " + this.getClass().getName());
        super.onConfigurationChanged(newConfig);
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
                " request=" + requestCode + " result=" + resultCode + " data=" + data);
        super.onActivityResult(requestCode, resultCode, data);
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

    protected View getVisibleView() {
        for (Fragment fragment : getSupportFragmentManager().getFragments())
            if (fragment.getUserVisibleHint()) {
                Log.i("Visible fragment=" + fragment.getClass().getName());
                return fragment.getView();
            }

        Log.i("Visible activity=" + this.getClass().getName());
        return findViewById(android.R.id.content);
    }

    private List<IBackPressedListener> backPressedListeners = new ArrayList<>();

    public void addBackPressedListener(IBackPressedListener listener) {
        backPressedListeners.add(listener);
    }

    public void removeBackPressedListener(IBackPressedListener listener) {
        backPressedListeners.remove(listener);
    }

    @Override
    public void onBackPressed() {
        for (IBackPressedListener listener : backPressedListeners)
            if (listener.onBackPressed())
                return;
        super.onBackPressed();
    }

    public interface IBackPressedListener {
        boolean onBackPressed();
    }
}
