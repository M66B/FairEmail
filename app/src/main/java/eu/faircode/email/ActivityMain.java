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

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ActivityMain extends ActivityBase implements FragmentManager.OnBackStackChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final long SPLASH_DELAY = 1500L; // milliseconds
    private static final long SERVICE_START_DELAY = 5 * 1000L; // milliseconds
    private static final long IGNORE_STORAGE_SPACE = 24 * 60 * 60 * 1000L; // milliseconds

    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "main");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long now = new Date().getTime();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean accept_unsupported = prefs.getBoolean("accept_unsupported", false);
        long accept_space = prefs.getLong("accept_space", 0);

        long cake = Helper.getAvailableStorageSpace();
        if (cake < Helper.MIN_REQUIRED_SPACE && accept_space < now) {
            setTheme(R.style.AppThemeBlueOrangeLight);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_space);

            TextView tvRemaining = findViewById(R.id.tvRemaining);
            tvRemaining.setText(getString(R.string.app_cake_remaining,
                    Helper.humanReadableByteCount(cake)));

            TextView tvRequired = findViewById(R.id.tvRequired);
            tvRequired.setText(getString(R.string.app_cake_required,
                    Helper.humanReadableByteCount(Helper.MIN_REQUIRED_SPACE, true)));

            Button btnFix = findViewById(R.id.btnFix);
            btnFix.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                    v.getContext().startActivity(intent);
                }
            });

            Button btnContinue = findViewById(R.id.btnContinue);
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prefs.edit().putLong("accept_space", now + IGNORE_STORAGE_SPACE).commit();
                    ApplicationEx.restart(v.getContext(), "accept_space");
                }
            });

            return;
        }

        if (!accept_unsupported &&
                !Helper.isSupportedDevice() &&
                Helper.isPlayStoreInstall()) {
            setTheme(R.style.AppThemeBlueOrangeLight);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_unsupported);

            Button btnContinue = findViewById(R.id.btnContinue);
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prefs.edit().putBoolean("accept_unsupported", true).commit();
                    ApplicationEx.restart(v.getContext(), "accept_unsupported");
                }
            });

            return;
        }

        Intent intent = getIntent();
        Uri data = (intent == null ? null : intent.getData());
        if (data != null &&
                (("message".equals(data.getScheme()) &&
                        ("email.faircode.eu".equals(data.getHost()) ||
                                BuildConfig.APPLICATION_ID.equals(data.getHost()))) ||
                        ("https".equals(data.getScheme()) &&
                                "link.fairemail.net".equals(data.getHost())))) {
            super.onCreate(savedInstanceState);

            Bundle args = new Bundle();
            args.putParcelable("data", data);

            new SimpleTask<EntityMessage>() {
                @Override
                protected EntityMessage onExecute(Context context, Bundle args) {
                    Uri data = args.getParcelable("data");
                    long id;
                    if ("email.faircode.eu".equals(data.getHost()) ||
                            "link.fairemail.net".equals(data.getHost()))
                        id = Long.parseLong(data.getFragment());
                    else {
                        String path = data.getPath();
                        if (path == null)
                            return null;
                        String[] parts = path.split("/");
                        if (parts.length < 1)
                            return null;
                        id = Long.parseLong(parts[1]);
                    }

                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    if (message != null) {
                        EntityFolder folder = db.folder().getFolder(message.folder);
                        if (folder != null)
                            args.putString("type", folder.type);
                    }

                    return message;
                }

                @Override
                protected void onExecuted(Bundle args, EntityMessage message) {
                    finish();

                    if (message == null) {
                        startActivity(new Intent(ActivityMain.this, ActivityView.class));
                        return;
                    }

                    String type = args.getString("type");

                    Intent thread = new Intent(ActivityMain.this, ActivityView.class);
                    thread.setAction("thread:" + message.id);
                    thread.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    thread.putExtra("account", message.account);
                    thread.putExtra("folder", message.folder);
                    thread.putExtra("type", type);
                    thread.putExtra("thread", message.thread);
                    thread.putExtra("filter_archive", !EntityFolder.ARCHIVE.equals(type));
                    thread.putExtra("pinned", true);
                    thread.putExtra("msgid", message.msgid);

                    startActivity(thread);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    // Ignored
                }
            }.setExecutor(executor).execute(this, args, "message:linked");

            return;
        }

        boolean eula = prefs.getBoolean("eula", false);
        boolean sync_on_launch = prefs.getBoolean("sync_on_launch", false);

        prefs.registerOnSharedPreferenceChangeListener(this);

        if (eula) {
            try {
                super.onCreate(savedInstanceState);
            } catch (RuntimeException ex) {
                Log.e(ex);
                // https://issuetracker.google.com/issues/181805603
                finish();
                startActivity(getIntent());
                return;
            }

            long start = new Date().getTime();
            Log.i("Main boot");

            final Runnable splash = new Runnable() {
                @Override
                public void run() {
                    getWindow().setBackgroundDrawableResource(R.drawable.splash);
                }
            };

            final SimpleTask<Boolean> boot = new SimpleTask<Boolean>() {
                @Override
                protected void onPreExecute(Bundle args) {
                    getMainHandler().postDelayed(splash, SPLASH_DELAY);
                }

                @Override
                protected void onPostExecute(Bundle args) {
                    getMainHandler().removeCallbacks(splash);
                }

                @Override
                protected Boolean onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(context);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    String last_activity = prefs.getString("last_activity", null);
                    long composing = prefs.getLong("last_composing", -1L);
                    if (ActivityCompose.class.getName().equals(last_activity) && composing >= 0) {
                        EntityMessage draft = db.message().getMessage(composing);
                        if (draft == null || draft.ui_hide)
                            prefs.edit()
                                    .remove("last_activity")
                                    .remove("last_composing")
                                    .apply();
                    }

                    if (prefs.getBoolean("has_accounts", false))
                        return true;

                    List<EntityAccount> accounts = db.account().getSynchronizingAccounts(null);
                    boolean hasAccounts = (accounts != null && accounts.size() > 0);

                    prefs.edit().putBoolean("has_accounts", hasAccounts).apply();

                    return hasAccounts;
                }

                @Override
                protected void onExecuted(Bundle args, Boolean hasAccounts) {
                    Bundle options = null;
                    try {
                        if (BuildConfig.DEBUG)
                            options = ActivityOptions.makeCustomAnimation(ActivityMain.this,
                                    R.anim.activity_open_enter, 0).toBundle();
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                    if (hasAccounts) {
                        Intent view = new Intent(ActivityMain.this, ActivityView.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        // VX-N3
                        // https://developer.android.com/docs/quality-guidelines/core-app-quality
                        boolean restore_on_launch = prefs.getBoolean("restore_on_launch", true);
                        if (restore_on_launch) {
                            String last_activity = prefs.getString("last_activity", null);
                            long composing = prefs.getLong("last_composing", -1L);
                            if (ActivityCompose.class.getName().equals(last_activity) && composing >= 0)
                                view = new Intent(ActivityMain.this, ActivityCompose.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .putExtra("action", "edit")
                                        .putExtra("id", composing);
                        } else
                            view.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        Intent saved = args.getParcelable("intent");
                        if (saved == null) {
                            startActivity(view, options);
                            if (sync_on_launch)
                                ServiceUI.sync(ActivityMain.this, null);
                        } else
                            try {
                                startActivity(saved);
                            } catch (SecurityException ex) {
                                Log.w(ex);
                                startActivity(view);
                            }

                        getMainHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ServiceSynchronize.watchdog(ActivityMain.this);
                                ServiceSend.watchdog(ActivityMain.this);
                            }
                        }, SERVICE_START_DELAY);
                    } else {
                        Intent setup = new Intent(ActivityMain.this, ActivitySetup.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(setup, options);
                    }

                    long end = new Date().getTime();
                    Log.i("Main booted " + (end - start) + " ms");

                    finish();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    // Log.unexpectedError() won't work here
                    Log.e(ex);

                    LayoutInflater inflater = LayoutInflater.from(ActivityMain.this);
                    View dview = inflater.inflate(R.layout.dialog_unexpected, null);
                    TextView tvError = dview.findViewById(R.id.tvError);

                    String message = Log.formatThrowable(ex, false);
                    tvError.setText(message);

                    new AlertDialog.Builder(ActivityMain.this)
                            .setView(dview)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setNeutralButton(R.string.menu_faq, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri uri = Helper.getSupportUri(ActivityMain.this, "Main:error")
                                            .buildUpon()
                                            .appendQueryParameter("message",
                                                    Helper.limit(Log.formatThrowable(ex, false), 384))
                                            .build();
                                    Helper.view(ActivityMain.this, uri, true);
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    finish();
                                }
                            })
                            .show();
                }
            }.setExecutor(executor);

            if (Helper.shouldAuthenticate(this, false))
                getMainHandler().post(new RunnableEx("authenticate") {
                    @Override
                    public void delegate() {
                        Helper.authenticate(ActivityMain.this, ActivityMain.this, null,
                                new RunnableEx("auth:succeeded") {
                                    @Override
                                    public void delegate() {
                                        Intent intent = getIntent();
                                        Bundle args = new Bundle();
                                        if (intent.hasExtra("intent"))
                                            args.putParcelable("intent", intent.getParcelableExtra("intent"));
                                        boot.execute(ActivityMain.this, args, "main:accounts:auth");
                                    }
                                },
                                new RunnableEx("auth:cancelled") {
                                    @Override
                                    public void delegate() {
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
                    }
                });
            else
                boot.execute(this, new Bundle(), "main:accounts");
        } else {
            SharedPreferences.Editor editor = prefs.edit();
            Configuration config = getResources().getConfiguration();

            // Default enable compact mode for smaller screens
            if (!config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) {
                editor.putBoolean("compact", true);
                //editor.putBoolean("compact_folders", true);
            }

            // Default disable landscape columns for small screens
            // Disable last sync time / nav menu for small screens
            if (!config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL)) {
                editor.putBoolean("landscape", false);
                editor.putBoolean("nav_last_sync", false);
            }
            editor.putBoolean("landscape3", false);

            // Default send bubbles off when accessibility enabled
            if (Helper.isAccessibilityEnabled(this))
                editor.putBoolean("send_chips", false);

            editor.apply();

            if (Helper.isNight(this))
                setTheme(R.style.AppThemeBlueOrangeDark);
            else
                setTheme(R.style.AppThemeBlueOrangeLight);

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportFragmentManager().addOnBackStackChangedListener(this);

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
        if ("eula".equals(key)) {
            boolean eula = prefs.getBoolean(key, false);
            if (eula) {
                // recreate is done without animation, unfortunately
                recreate();
            }
        }
    }
}
