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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import java.util.Date;
import java.util.List;

public class ActivityMain extends ActivityBase implements FragmentManager.OnBackStackChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final long SPLASH_DELAY = 1500L; // milliseconds
    private static final long SERVICE_START_DELAY = 5 * 1000L; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (!Log.isSupportedDevice() && Helper.isPlayStoreInstall()) {
            setTheme(R.style.AppThemeBlueOrangeLight);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_unsupported);
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean eula = prefs.getBoolean("eula", false);

        prefs.registerOnSharedPreferenceChangeListener(this);

        if (eula) {
            super.onCreate(savedInstanceState);

            long start = new Date().getTime();
            Log.i("Main boot");

            final SimpleTask boot = new SimpleTask<Boolean>() {
                @Override
                protected void onPreExecute(Bundle args) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getWindow().setBackgroundDrawableResource(R.drawable.splash);
                        }
                    }, SPLASH_DELAY);
                }

                @Override
                protected Boolean onExecute(Context context, Bundle args) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    if (prefs.getBoolean("has_accounts", false))
                        return true;

                    DB db = DB.getInstance(context);
                    List<EntityAccount> accounts = db.account().getSynchronizingAccounts();
                    boolean hasAccounts = (accounts != null && accounts.size() > 0);

                    prefs.edit().putBoolean("has_accounts", hasAccounts).apply();

                    return hasAccounts;
                }

                @Override
                protected void onExecuted(Bundle args, Boolean hasAccounts) {
                    if (hasAccounts) {
                        Log.logBundle(args);

                        Intent view = new Intent(ActivityMain.this, ActivityView.class);
                        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        Intent saved = args.getParcelable("intent");
                        if (saved == null)
                            startActivity(view);
                        else
                            try {
                                startActivity(saved);
                            } catch (SecurityException ex) {
                                Log.w(ex);
                                startActivity(view);
                            }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ServiceSynchronize.eval(ActivityMain.this, "main");
                                ServiceSend.watchdog(ActivityMain.this);
                            }
                        }, SERVICE_START_DELAY);
                    } else
                        startActivity(new Intent(ActivityMain.this, ActivitySetup.class));

                    long end = new Date().getTime();
                    Log.i("Main booted " + (end - start) + " ms");

                    finish();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getSupportFragmentManager(), ex);
                }
            };

            if (Helper.shouldAuthenticate(this))
                Helper.authenticate(ActivityMain.this, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = getIntent();
                                Bundle args = new Bundle();
                                if (intent.hasExtra("intent"))
                                    args.putParcelable("intent", intent.getParcelableExtra("intent"));
                                boot.execute(ActivityMain.this, args, "main:accounts");
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    finish();
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                    /*
                                    java.lang.NullPointerException: Attempt to invoke virtual method 'int com.android.server.fingerprint.ClientMonitor.stop(boolean)' on a null object reference
                                        at android.os.Parcel.createException(Parcel.java:1956)
                                        at android.os.Parcel.readException(Parcel.java:1918)
                                        at android.os.Parcel.readException(Parcel.java:1868)
                                        at android.app.IActivityManager$Stub$Proxy.finishActivity(IActivityManager.java:3797)
                                        at android.app.Activity.finish(Activity.java:5608)
                                        at android.app.Activity.finish(Activity.java:5632)
                                        at eu.faircode.email.ActivityMain$3.run(SourceFile:111)
                                        at eu.faircode.email.Helper$3$1.run(SourceFile:706)
                                        at android.os.Handler.handleCallback(Handler.java:873)
                                        at android.os.Handler.dispatchMessage(Handler.java:99)
                                        at android.os.Looper.loop(Looper.java:193)
                                        at android.app.ActivityThread.main(ActivityThread.java:6718)
                                        at java.lang.reflect.Method.invoke(Method.java:-2)
                                        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:493)
                                        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:858)
                                    Caused by: android.os.RemoteException: Remote stack trace:
                                        at com.android.server.fingerprint.FingerprintService$5.onTaskStackChanged(FingerprintService.java:239)
                                        at com.android.server.am.TaskChangeNotificationController.lambda$new$0(TaskChangeNotificationController.java:70)
                                        at com.android.server.am.-$$Lambda$TaskChangeNotificationController$kftD881t3KfWCASQEbeTkieVI2M.accept(Unknown Source:0)
                                        at com.android.server.am.TaskChangeNotificationController.forAllLocalListeners(TaskChangeNotificationController.java:263)
                                        at com.android.server.am.TaskChangeNotificationController.notifyTaskStackChanged(TaskChangeNotificationController.java:276)
                                    */
                                }
                            }
                        });
            else
                boot.execute(this, new Bundle(), "main:accounts");
        } else {
            // Enable 3-col mode on large screen / compact view on small screens
            if (getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE))
                prefs.edit().putBoolean("landscape3", true).apply();
            else
                prefs.edit().putBoolean("compact", true).apply();

            setTheme(R.style.AppThemeBlueOrangeLight);
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
