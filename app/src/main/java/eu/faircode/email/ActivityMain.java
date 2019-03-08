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
import android.os.Build;
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

        if (!isSupportedDevice()) {
            setTheme(R.style.AppThemeLight);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_unsupported);
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        if (prefs.getBoolean("eula", false)) {
            super.onCreate(savedInstanceState);
            new SimpleTask<List<EntityAccount>>() {
                @Override
                protected List<EntityAccount> onExecute(Context context, Bundle args) {
                    return DB.getInstance(context).account().getSynchronizingAccounts();
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                    if (accounts == null || accounts.size() == 0)
                        startActivity(new Intent(ActivityMain.this, ActivitySetup.class));
                    else {
                        startActivity(new Intent(ActivityMain.this, ActivityView.class));
                        ServiceSynchronize.boot(ActivityMain.this);
                        ServiceSend.boot(ActivityMain.this);
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

    private boolean isSupportedDevice() {
        if ("Amazon".equals(Build.BRAND) && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        /*
            java.lang.IllegalArgumentException: Comparison method violates its general contract!
            java.lang.IllegalArgumentException: Comparison method violates its general contract!
            at java.util.TimSort.mergeHi(TimSort.java:864)
            at java.util.TimSort.mergeAt(TimSort.java:481)
            at java.util.TimSort.mergeCollapse(TimSort.java:406)
            at java.util.TimSort.sort(TimSort.java:210)
            at java.util.TimSort.sort(TimSort.java:169)
            at java.util.Arrays.sort(Arrays.java:2010)
            at java.util.Collections.sort(Collections.java:1883)
            at android.view.ViewGroup$ChildListForAccessibility.init(ViewGroup.java:7181)
            at android.view.ViewGroup$ChildListForAccessibility.obtain(ViewGroup.java:7138)
            at android.view.ViewGroup.dispatchPopulateAccessibilityEventInternal(ViewGroup.java:2734)
            at android.view.View.dispatchPopulateAccessibilityEvent(View.java:5617)
            at android.view.View.sendAccessibilityEventUncheckedInternal(View.java:5582)
            at android.view.View.sendAccessibilityEventUnchecked(View.java:5566)
            at android.view.View.sendAccessibilityEventInternal(View.java:5543)
            at android.view.View.sendAccessibilityEvent(View.java:5512)
            at android.view.View.onFocusChanged(View.java:5449)
            at android.view.View.handleFocusGainInternal(View.java:5229)
            at android.view.ViewGroup.handleFocusGainInternal(ViewGroup.java:651)
            at android.view.View.requestFocusNoSearch(View.java:7950)
            at android.view.View.requestFocus(View.java:7929)
            at android.view.ViewGroup.requestFocus(ViewGroup.java:2612)
            at android.view.ViewGroup.onRequestFocusInDescendants(ViewGroup.java:2657)
            at android.view.ViewGroup.requestFocus(ViewGroup.java:2613)
            at android.view.View.requestFocus(View.java:7896)
            at android.view.View.requestFocus(View.java:7875)
            at androidx.recyclerview.widget.RecyclerView.recoverFocusFromState(SourceFile:3788)
            at androidx.recyclerview.widget.RecyclerView.dispatchLayoutStep3(SourceFile:4023)
            at androidx.recyclerview.widget.RecyclerView.dispatchLayout(SourceFile:3652)
            at androidx.recyclerview.widget.RecyclerView.consumePendingUpdateOperations(SourceFile:1877)
            at androidx.recyclerview.widget.RecyclerView$w.run(SourceFile:5044)
            at android.view.Choreographer$CallbackRecord.run(Choreographer.java:781)
            at android.view.Choreographer.doCallbacks(Choreographer.java:592)
            at android.view.Choreographer.doFrame(Choreographer.java:559)
            at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:767)
         */
            return false;
        }

        return true;
    }
}
