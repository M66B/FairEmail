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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ActivityMain extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        if (!Helper.isPlayStoreInstall(this)) {
            Log.i("Third party install");
            prefs.edit().putBoolean("play_store", false).apply();
        }

        if (prefs.getBoolean("eula", false)) {
            super.onCreate(savedInstanceState);
            new SimpleTask<List<EntityAccount>>() {
                @Override
                protected List<EntityAccount> onExecute(Context context, Bundle args) {
                    return DB.getInstance(context).account().getAccounts(true);
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                    if (accounts == null || accounts.size() == 0)
                        startActivity(new Intent(ActivityMain.this, ActivitySetup.class));
                    else {
                        startActivity(new Intent(ActivityMain.this, ActivityView.class));
                        ServiceSynchronize.init(ActivityMain.this);
                    }
                    finish();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(ActivityMain.this, ActivityMain.this, ex);
                }
            }.execute(this, new Bundle(), "main:accounts");
        } else {
            setTheme(R.style.AppThemeLight);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new FragmentEula()).addToBackStack("eula");
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onBackStackChanged() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0)
            finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("eula".equals(key))
            if (prefs.getBoolean(key, false))
                recreate();
    }
}
