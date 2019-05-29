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

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;

import com.bugsnag.android.Bugsnag;

import java.text.SimpleDateFormat;

public class FragmentOptionsMisc extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swBadge;
    private SwitchCompat swSubscriptions;
    private SwitchCompat swSubscribedOnly;
    private SwitchCompat swEnglish;
    private SwitchCompat swAuthentication;
    private SwitchCompat swParanoid;
    private TextView tvParanoidHint;
    private SwitchCompat swWatchdog;
    private SwitchCompat swUpdates;
    private SwitchCompat swCrashReports;
    private SwitchCompat swDebug;

    private TextView tvMemoryClass;
    private TextView tvLastCleanup;
    private TextView tvUuid;

    private Group grpDebug;

    private final static String[] RESET_OPTIONS = new String[]{
            "badge", "subscriptions", "subscribed_only", "english", "authentication", "paranoid", "watchdog", "updates", "crash_reports", "debug"
    };

    private final static String[] RESET_QUESTIONS = new String[]{
            "show_html_confirmed", "show_images_confirmed", "print_html_confirmed", "edit_ref_confirmed", "crash_reports_confirmed"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_misc, container, false);

        // Get controls

        swBadge = view.findViewById(R.id.swBadge);
        swSubscriptions = view.findViewById(R.id.swSubscriptions);
        swSubscribedOnly = view.findViewById(R.id.swSubscribedOnly);
        swEnglish = view.findViewById(R.id.swEnglish);
        swAuthentication = view.findViewById(R.id.swAuthentication);
        swParanoid = view.findViewById(R.id.swParanoid);
        tvParanoidHint = view.findViewById(R.id.tvParanoidHint);
        swWatchdog = view.findViewById(R.id.swWatchdog);
        swUpdates = view.findViewById(R.id.swUpdates);
        swCrashReports = view.findViewById(R.id.swCrashReports);
        swDebug = view.findViewById(R.id.swDebug);

        tvMemoryClass = view.findViewById(R.id.tvMemoryClass);
        tvLastCleanup = view.findViewById(R.id.tvLastCleanup);
        tvUuid = view.findViewById(R.id.tvUuid);

        grpDebug = view.findViewById(R.id.grpDebug);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swBadge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("badge", checked).apply();
                ServiceSynchronize.reload(getContext(), "badge");
            }
        });

        swSubscriptions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("subscriptions", checked).apply();
            }
        });

        swSubscribedOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("subscribed_only", checked).apply();
                ServiceSynchronize.reload(getContext(), "subscribed_only");
            }
        });

        swEnglish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("english", checked).commit(); // apply won't work here
                restart();
            }
        });

        swAuthentication.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("authentication", checked).apply();
            }
        });

        swParanoid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("paranoid", checked).apply();
            }
        });

        final Intent faq = new Intent(Intent.ACTION_VIEW);
        faq.setData(Uri.parse(Helper.FAQ_URI + "#user-content-faq86"));
        faq.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (faq.resolveActivity(getContext().getPackageManager()) != null) {
            tvParanoidHint.getPaint().setUnderlineText(true);
            tvParanoidHint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(faq);
                }
            });
        }

        swWatchdog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("watchdog", checked).apply();
                WorkerWatchdog.init(getContext());
            }
        });

        swUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("updates", checked).apply();
            }
        });

        swCrashReports.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("crash_reports", checked).apply();
                if (checked)
                    Bugsnag.startSession();
                else
                    Bugsnag.stopSession();
            }
        });

        swDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("debug", checked).apply();
                grpDebug.setVisibility(checked || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
                ServiceSynchronize.reload(getContext(), "debug=" + checked);
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
        setOptions();
        if ("last_cleanup".equals(key))
            setLastCleanup(prefs.getLong(key, -1));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options_misc, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_default:
                onMenuDefault(RESET_OPTIONS);
                return true;
            case R.id.menu_reset_questions:
                onMenuDefault(RESET_QUESTIONS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuDefault(String[] options) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        for (String option : options)
            editor.remove(option);
        editor.apply();
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swBadge.setChecked(prefs.getBoolean("badge", true));
        swSubscriptions.setChecked(prefs.getBoolean("subscriptions", false));
        swSubscribedOnly.setChecked(prefs.getBoolean("subscribed_only", false));
        swEnglish.setChecked(prefs.getBoolean("english", false));
        swAuthentication.setChecked(prefs.getBoolean("authentication", false));
        swParanoid.setChecked(prefs.getBoolean("paranoid", true));
        swWatchdog.setChecked(prefs.getBoolean("watchdog", true));
        swUpdates.setChecked(prefs.getBoolean("updates", true));
        swUpdates.setVisibility(Helper.isPlayStoreInstall(getContext()) ? View.GONE : View.VISIBLE);
        swCrashReports.setChecked(prefs.getBoolean("crash_reports", false));
        swDebug.setChecked(prefs.getBoolean("debug", false));

        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int class_mb = am.getMemoryClass();
        tvMemoryClass.setText(getString(R.string.title_advanced_memory_class, class_mb + " MB"));
        tvUuid.setText(prefs.getString("uuid", null));

        grpDebug.setVisibility(swDebug.isChecked() || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }

    private void setLastCleanup(long time) {
        java.text.DateFormat df = SimpleDateFormat.getDateTimeInstance();
        tvLastCleanup.setText(
                getString(R.string.title_advanced_last_cleanup,
                        time < 0 ? "-" : df.format(time)));
    }

    private void restart() {
        Intent intent = new Intent(getContext(), ActivityMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Runtime.getRuntime().exit(0);
    }
}
