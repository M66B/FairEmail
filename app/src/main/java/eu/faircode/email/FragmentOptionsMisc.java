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

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import io.requery.android.database.sqlite.SQLiteDatabase;

public class FragmentOptionsMisc extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swExternalSearch;
    private SwitchCompat swFts;
    private TextView tvFtsIndexed;
    private TextView tvFtsPro;
    private SwitchCompat swEnglish;
    private SwitchCompat swWatchdog;
    private SwitchCompat swOptimize;
    private SwitchCompat swUpdates;
    private SwitchCompat swExperiments;
    private TextView tvExperimentsHint;
    private SwitchCompat swCrashReports;
    private TextView tvUuid;
    private SwitchCompat swDebug;
    private Button btnReset;
    private Button btnCleanup;
    private TextView tvLastCleanup;

    private TextView tvProcessors;
    private TextView tvMemoryClass;
    private TextView tvStorageSpace;
    private TextView tvFingerprint;

    private Group grpDebug;

    private final static String[] RESET_OPTIONS = new String[]{
            "fts", "english", "watchdog", "auto_optimize", "updates", "experiments", "crash_reports", "debug"
    };

    private final static String[] RESET_QUESTIONS = new String[]{
            "welcome", "crash_reports_asked", "review_asked", "review_later", "why",
            "reply_hint", "html_always_images", "print_html_confirmed", "move_1_confirmed", "move_n_confirmed",
            "identities_asked", "cc_bcc", "inline_image_hint", "compose_reference", "send_dialog",
            "setup_advanced"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_misc, container, false);

        // Get controls

        swExternalSearch = view.findViewById(R.id.swExternalSearch);
        swFts = view.findViewById(R.id.swFts);
        tvFtsIndexed = view.findViewById(R.id.tvFtsIndexed);
        tvFtsPro = view.findViewById(R.id.tvFtsPro);
        swEnglish = view.findViewById(R.id.swEnglish);
        swWatchdog = view.findViewById(R.id.swWatchdog);
        swOptimize = view.findViewById(R.id.swOptimize);
        swUpdates = view.findViewById(R.id.swUpdates);
        swExperiments = view.findViewById(R.id.swExperiments);
        tvExperimentsHint = view.findViewById(R.id.tvExperimentsHint);
        swCrashReports = view.findViewById(R.id.swCrashReports);
        tvUuid = view.findViewById(R.id.tvUuid);
        swDebug = view.findViewById(R.id.swDebug);
        btnReset = view.findViewById(R.id.btnReset);
        btnCleanup = view.findViewById(R.id.btnCleanup);
        tvLastCleanup = view.findViewById(R.id.tvLastCleanup);

        tvProcessors = view.findViewById(R.id.tvProcessors);
        tvMemoryClass = view.findViewById(R.id.tvMemoryClass);
        tvStorageSpace = view.findViewById(R.id.tvStorageSpace);
        tvFingerprint = view.findViewById(R.id.tvFingerprint);

        grpDebug = view.findViewById(R.id.grpDebug);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swExternalSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                PackageManager pm = getContext().getPackageManager();
                pm.setComponentEnabledSetting(
                        new ComponentName(getContext(), ActivitySearch.class),
                        checked
                                ? PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        });

        swFts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("fts", checked).apply();

                WorkerFts.init(getContext(), true);

                if (!checked) {
                    Bundle args = new Bundle();

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            SQLiteDatabase sdb = FtsDbHelper.getInstance(context);
                            FtsDbHelper.delete(sdb);
                            FtsDbHelper.optimize(sdb);

                            DB db = DB.getInstance(context);
                            db.message().resetFts();

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentOptionsMisc.this, args, "fts:reset");
                }
            }
        });

        Helper.linkPro(tvFtsPro);

        swEnglish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("english", checked).commit(); // apply won't work here
                restart();
            }
        });

        swWatchdog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("watchdog", checked).apply();
                WorkerWatchdog.init(getContext());
            }
        });

        swOptimize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_optimize", checked).apply();
            }
        });

        swUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("updates", checked).apply();
                if (!checked) {
                    NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel(Helper.NOTIFICATION_UPDATE);
                }
            }
        });

        tvExperimentsHint.setPaintFlags(tvExperimentsHint.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvExperimentsHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(getContext(), 125);
            }
        });

        swExperiments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("experiments", checked).apply();
            }
        });

        swCrashReports.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit()
                        .remove("crash_reports_asked")
                        .putBoolean("crash_reports", checked)
                        .apply();
                Log.setCrashReporting(checked);
            }
        });

        swDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("debug", checked).apply();
                grpDebug.setVisibility(checked || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetQuestions();
            }
        });

        btnCleanup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCleanup();
            }
        });

        tvFtsIndexed.setText(null);

        DB db = DB.getInstance(getContext());
        db.message().liveFts().observe(getViewLifecycleOwner(), new Observer<TupleFtsStats>() {
            private TupleFtsStats last = null;

            @Override
            public void onChanged(TupleFtsStats stats) {
                if (stats == null)
                    tvFtsIndexed.setText(null);
                else if (last == null || !last.equals(stats))
                    tvFtsIndexed.setText(getString(R.string.title_advanced_fts_indexed,
                            stats.fts,
                            stats.total,
                            Helper.humanReadableByteCount(FtsDbHelper.size(getContext()), true)));
                last = stats;
            }
        });

        setLastCleanup(prefs.getLong("last_cleanup", -1));

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            setOptions();
            if ("last_cleanup".equals(key))
                setLastCleanup(prefs.getLong(key, -1));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_default:
                onMenuDefault();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuDefault() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        for (String option : RESET_OPTIONS)
            editor.remove(option);
        editor.apply();
        ToastEx.makeText(getContext(), R.string.title_setup_done, Toast.LENGTH_LONG).show();
    }

    private void onResetQuestions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        for (String option : RESET_QUESTIONS)
            editor.remove(option);
        for (String key : prefs.getAll().keySet())
            if (key.endsWith(".show_full") || key.endsWith(".show_images"))
                editor.remove(key);
        editor.apply();
        ToastEx.makeText(getContext(), R.string.title_setup_done, Toast.LENGTH_LONG).show();
    }

    private void onCleanup() {
        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                btnCleanup.setEnabled(false);
                ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                btnCleanup.setEnabled(true);
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                WorkerCleanup.cleanup(context, true);
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "cleanup:run");
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        PackageManager pm = getContext().getPackageManager();
        int state = pm.getComponentEnabledSetting(new ComponentName(getContext(), ActivitySearch.class));

        swExternalSearch.setChecked(state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        swFts.setChecked(prefs.getBoolean("fts", false));
        swEnglish.setChecked(prefs.getBoolean("english", false));
        swWatchdog.setChecked(prefs.getBoolean("watchdog", true));
        swOptimize.setChecked(prefs.getBoolean("auto_optimize", false));
        swUpdates.setChecked(prefs.getBoolean("updates", true));
        swUpdates.setVisibility(
                Helper.isPlayStoreInstall() || !Helper.hasValidFingerprint(getContext())
                        ? View.GONE : View.VISIBLE);
        swExperiments.setChecked(prefs.getBoolean("experiments", false));
        swCrashReports.setChecked(prefs.getBoolean("crash_reports", false));
        tvUuid.setText(prefs.getString("uuid", null));
        swDebug.setChecked(prefs.getBoolean("debug", false));

        tvProcessors.setText(getString(R.string.title_advanced_processors, Runtime.getRuntime().availableProcessors()));

        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int class_mb = am.getMemoryClass();
        tvMemoryClass.setText(getString(R.string.title_advanced_memory_class, class_mb + " MB"));

        tvStorageSpace.setText(getString(R.string.title_advanced_storage_space,
                Helper.humanReadableByteCount(Helper.getAvailableStorageSpace(), true),
                Helper.humanReadableByteCount(Helper.getTotalStorageSpace(), true)));
        tvFingerprint.setText(Helper.getFingerprint(getContext()));

        grpDebug.setVisibility(swDebug.isChecked() || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }

    private void setLastCleanup(long time) {
        java.text.DateFormat DTF = Helper.getDateTimeInstance(getContext());
        tvLastCleanup.setText(
                getString(R.string.title_advanced_last_cleanup,
                        time < 0 ? "-" : DTF.format(time)));
    }
}
