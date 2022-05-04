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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class FragmentOptionsSynchronize extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swEnabled;
    private SwitchCompat swOptimize;
    private ImageButton ibOptimizeInfo;
    private Spinner spPollInterval;
    private TextView tvPollBattery;
    private RecyclerView rvExempted;
    private SwitchCompat swSchedule;
    private TextView tvSchedulePro;
    private TextView tvScheduleStart;
    private TextView tvScheduleEnd;
    private CheckBox[] cbDay;
    private TextView tvScheduleIgnore;
    private ImageButton ibSchedules;

    private SwitchCompat swQuickSyncImap;
    private SwitchCompat swQuickSyncPop;
    private SwitchCompat swNodate;
    private SwitchCompat swUnseen;
    private SwitchCompat swFlagged;
    private SwitchCompat swDeleteUnseen;
    private SwitchCompat swSyncKept;
    private SwitchCompat swGmailThread;
    private SwitchCompat swOutlookThread;
    private SwitchCompat swSubjectThreading;
    private TextView tvSubjectThreading;
    private SwitchCompat swSyncFolders;
    private SwitchCompat swSyncFoldersPoll;
    private SwitchCompat swSyncSharedFolders;
    private SwitchCompat swSubscriptions;
    private SwitchCompat swTuneKeepAlive;

    private SwitchCompat swCheckAuthentication;
    private ImageButton ibCheckAuthenticationInfo;
    private SwitchCompat swCheckTls;
    private ImageButton ibCheckTlsInfo;
    private SwitchCompat swCheckReply;
    private SwitchCompat swCheckMx;
    private SwitchCompat swCheckBlocklist;
    private SwitchCompat swUseBlocklist;
    private SwitchCompat swUseBlocklistPop;
    private RecyclerView rvBlocklist;
    private AdapterBlocklist badapter;

    private Group grpExempted;

    private AdapterAccountExempted adapter;

    private final static String[] RESET_OPTIONS = new String[]{
            "enabled", "poll_interval", "auto_optimize", "schedule", "schedule_start", "schedule_end",
            "sync_quick_imap", "sync_quick_pop",
            "sync_nodate", "sync_unseen", "sync_flagged", "delete_unseen", "sync_kept",
            "gmail_thread_id", "outlook_thread_id", "subject_threading",
            "sync_folders", "sync_folders_poll", "sync_shared_folders", "subscriptions",
            "check_authentication", "check_tls", "check_reply_domain", "check_mx",
            "check_blocklist", "use_blocklist", "use_blocklist_pop",
            "tune_keep_alive"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_synchronize, container, false);

        // Get controls

        swEnabled = view.findViewById(R.id.swEnabled);
        swOptimize = view.findViewById(R.id.swOptimize);
        ibOptimizeInfo = view.findViewById(R.id.ibOptimizeInfo);
        spPollInterval = view.findViewById(R.id.spPollInterval);
        tvPollBattery = view.findViewById(R.id.tvPollBattery);

        swSchedule = view.findViewById(R.id.swSchedule);
        rvExempted = view.findViewById(R.id.rvExempted);
        tvSchedulePro = view.findViewById(R.id.tvSchedulePro);
        tvScheduleStart = view.findViewById(R.id.tvScheduleStart);
        tvScheduleEnd = view.findViewById(R.id.tvScheduleEnd);
        cbDay = new CheckBox[]{
                view.findViewById(R.id.cbDay0),
                view.findViewById(R.id.cbDay1),
                view.findViewById(R.id.cbDay2),
                view.findViewById(R.id.cbDay3),
                view.findViewById(R.id.cbDay4),
                view.findViewById(R.id.cbDay5),
                view.findViewById(R.id.cbDay6)
        };
        tvScheduleIgnore = view.findViewById(R.id.tvScheduleIgnore);
        ibSchedules = view.findViewById(R.id.ibSchedules);

        swQuickSyncImap = view.findViewById(R.id.swQuickSyncImap);
        swQuickSyncPop = view.findViewById(R.id.swQuickSyncPop);
        swNodate = view.findViewById(R.id.swNodate);
        swUnseen = view.findViewById(R.id.swUnseen);
        swFlagged = view.findViewById(R.id.swFlagged);
        swDeleteUnseen = view.findViewById(R.id.swDeleteUnseen);
        swSyncKept = view.findViewById(R.id.swSyncKept);
        swGmailThread = view.findViewById(R.id.swGmailThread);
        swOutlookThread = view.findViewById(R.id.swOutlookThread);
        swSubjectThreading = view.findViewById(R.id.swSubjectThreading);
        tvSubjectThreading = view.findViewById(R.id.tvSubjectThreading);
        swSyncFolders = view.findViewById(R.id.swSyncFolders);
        swSyncFoldersPoll = view.findViewById(R.id.swSyncFoldersPoll);
        swSyncSharedFolders = view.findViewById(R.id.swSyncSharedFolders);
        swSubscriptions = view.findViewById(R.id.swSubscriptions);
        swTuneKeepAlive = view.findViewById(R.id.swTuneKeepAlive);

        swCheckAuthentication = view.findViewById(R.id.swCheckAuthentication);
        ibCheckAuthenticationInfo = view.findViewById(R.id.ibCheckAuthenticationInfo);
        swCheckTls = view.findViewById(R.id.swCheckTls);
        ibCheckTlsInfo = view.findViewById(R.id.ibCheckTlsInfo);
        swCheckReply = view.findViewById(R.id.swCheckReply);
        swCheckMx = view.findViewById(R.id.swCheckMx);
        swCheckBlocklist = view.findViewById(R.id.swCheckBlocklist);
        swUseBlocklist = view.findViewById(R.id.swUseBlocklist);
        swUseBlocklistPop = view.findViewById(R.id.swUseBlocklistPop);
        rvBlocklist = view.findViewById(R.id.rvBlocklist);

        grpExempted = view.findViewById(R.id.grpExempted);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("enabled", checked).apply();
            }
        });

        swOptimize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_optimize", checked).apply();
                ServiceSynchronize.reload(getContext(), null, false, "optimize");
            }
        });

        ibOptimizeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 39);
            }
        });

        spPollInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Object tag = adapterView.getTag();
                int current = (tag == null ? 0 : (Integer) tag);
                int[] values = getResources().getIntArray(R.array.pollIntervalValues);
                int value = values[position];
                if (value != current) {
                    adapterView.setTag(value);
                    prefs.edit().putInt("poll_interval", value).apply();
                    tvPollBattery.setVisibility(value > 0 && value < 15 ? View.VISIBLE : View.GONE);
                    grpExempted.setVisibility(value == 0 ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                adapterView.setTag(null);
                prefs.edit().remove("poll_interval").apply();
                grpExempted.setVisibility(View.GONE);
            }
        });

        rvExempted.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvExempted.setLayoutManager(llm);

        adapter = new AdapterAccountExempted(getViewLifecycleOwner(), getContext());
        rvExempted.setAdapter(adapter);

        swSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("schedule", checked).apply();
            }
        });

        Helper.linkPro(tvSchedulePro);

        tvScheduleStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("start", true);
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.setArguments(args);
                timePicker.show(getParentFragmentManager(), "timePicker");
            }
        });

        tvScheduleEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("start", false);
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.setArguments(args);
                timePicker.show(getParentFragmentManager(), "timePicker");
            }
        });

        String[] daynames = new DateFormatSymbols().getWeekdays();
        for (int i = 0; i < 7; i++) {
            final int day = i;
            cbDay[i].setText(daynames[i + 1]);
            cbDay[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean("schedule_day" + day, isChecked).apply();
                }
            });
        }

        tvScheduleIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(v.getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_VIEW_ACCOUNTS));
            }
        });

        ibSchedules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 78);
            }
        });

        swQuickSyncImap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sync_quick_imap", checked).apply();
            }
        });

        swQuickSyncPop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sync_quick_pop", checked).apply();
            }
        });

        swNodate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sync_nodate", checked).apply();
            }
        });

        swUnseen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sync_unseen", checked).apply();
            }
        });

        swFlagged.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sync_flagged", checked).apply();
            }
        });

        swDeleteUnseen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("delete_unseen", checked).apply();
            }
        });

        swSyncKept.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sync_kept", checked).apply();
            }
        });

        swGmailThread.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("gmail_thread_id", checked).apply();
                swSubjectThreading.setEnabled(!swGmailThread.isChecked() && !swOutlookThread.isChecked());
            }
        });

        swOutlookThread.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("outlook_thread_id", checked).apply();
                swSubjectThreading.setEnabled(!swGmailThread.isChecked() && !swOutlookThread.isChecked());
            }
        });

        swSubjectThreading.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("subject_threading", checked).apply();
            }
        });

        swSyncFolders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sync_folders", checked).apply();
                swSyncFoldersPoll.setEnabled(checked);
                swSyncSharedFolders.setEnabled(checked);
            }
        });

        swSyncFoldersPoll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sync_folders_poll", checked).apply();
            }
        });

        swSyncSharedFolders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sync_shared_folders", checked).apply();
            }
        });

        swSubscriptions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("subscriptions", checked).apply();
            }
        });

        swTuneKeepAlive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("tune_keep_alive", checked).apply();
            }
        });

        swCheckAuthentication.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("check_authentication", checked).apply();
                swCheckTls.setEnabled(checked);
            }
        });

        ibCheckAuthenticationInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.AUTH_RESULTS_URI), true);
            }
        });

        swCheckTls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("check_tls", checked).apply();
            }
        });

        ibCheckTlsInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 176);
            }
        });

        swCheckReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("check_reply_domain", checked).apply();
            }
        });

        swCheckMx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("check_mx", checked).apply();
            }
        });

        swCheckBlocklist.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("check_blocklist", checked).apply();
                swUseBlocklist.setEnabled(checked);
                swUseBlocklistPop.setEnabled(checked);
            }
        });

        swUseBlocklist.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("use_blocklist", checked).apply();
            }
        });

        swUseBlocklistPop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("use_blocklist_pop", checked).apply();
            }
        });

        rvBlocklist.setHasFixedSize(false);
        rvBlocklist.setLayoutManager(new LinearLayoutManager(getContext()));
        badapter = new AdapterBlocklist(getContext(), DnsBlockList.getListsAvailable());
        rvBlocklist.setAdapter(badapter);

        // Initialize
        FragmentDialogTheme.setBackground(getContext(), view, false);

        swOutlookThread.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        tvSubjectThreading.setText(getString(R.string.title_advanced_subject_threading_hint, MessageHelper.MAX_SUBJECT_AGE));

        DB db = DB.getInstance(getContext());
        db.account().liveSynchronizingAccounts().observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
            @Override
            public void onChanged(List<EntityAccount> accounts) {
                if (accounts == null)
                    accounts = new ArrayList<>();
                adapter.set(accounts);
            }
        });

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
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            setOptions();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_default) {
            FragmentOptions.reset(getContext(), RESET_OPTIONS, new Runnable() {
                @Override
                public void run() {
                    DnsBlockList.reset(getContext());
                    rvBlocklist.getAdapter().notifyDataSetChanged();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean pro = ActivityBilling.isPro(getContext());

        swEnabled.setChecked(prefs.getBoolean("enabled", true));
        swOptimize.setChecked(prefs.getBoolean("auto_optimize", false));

        int pollInterval = prefs.getInt("poll_interval", 0);
        int[] pollIntervalValues = getResources().getIntArray(R.array.pollIntervalValues);
        for (int pos = 0; pos < pollIntervalValues.length; pos++)
            if (pollIntervalValues[pos] == pollInterval) {
                spPollInterval.setTag(pollInterval);
                spPollInterval.setSelection(pos);
                break;
            }

        tvPollBattery.setVisibility(pollInterval > 0 && pollInterval < 15 ? View.VISIBLE : View.GONE);
        grpExempted.setVisibility(pollInterval == 0 ? View.GONE : View.VISIBLE);

        swSchedule.setChecked(prefs.getBoolean("schedule", false) && pro);
        swSchedule.setEnabled(pro);
        tvScheduleStart.setText(formatHour(getContext(), prefs.getInt("schedule_start", 0)));
        tvScheduleEnd.setText(formatHour(getContext(), prefs.getInt("schedule_end", 0)));
        for (int i = 0; i < 7; i++)
            cbDay[i].setChecked(prefs.getBoolean("schedule_day" + i, true));

        swQuickSyncImap.setChecked(prefs.getBoolean("sync_quick_imap", false));
        swQuickSyncPop.setChecked(prefs.getBoolean("sync_quick_pop", true));
        swNodate.setChecked(prefs.getBoolean("sync_nodate", false));
        swUnseen.setChecked(prefs.getBoolean("sync_unseen", false));
        swFlagged.setChecked(prefs.getBoolean("sync_flagged", false));
        swDeleteUnseen.setChecked(prefs.getBoolean("delete_unseen", false));
        swSyncKept.setChecked(prefs.getBoolean("sync_kept", true));
        swGmailThread.setChecked(prefs.getBoolean("gmail_thread_id", false));
        swOutlookThread.setChecked(prefs.getBoolean("outlook_thread_id", false));
        swSubjectThreading.setChecked(prefs.getBoolean("subject_threading", false));
        swSubjectThreading.setEnabled(!swGmailThread.isChecked() && !swOutlookThread.isChecked());
        swSyncFolders.setChecked(prefs.getBoolean("sync_folders", true));
        swSyncFoldersPoll.setChecked(prefs.getBoolean("sync_folders_poll", false));
        swSyncFoldersPoll.setEnabled(swSyncFolders.isChecked());
        swSyncSharedFolders.setChecked(prefs.getBoolean("sync_shared_folders", false));
        swSyncSharedFolders.setEnabled(swSyncFolders.isChecked());
        swSubscriptions.setChecked(prefs.getBoolean("subscriptions", false));
        swTuneKeepAlive.setChecked(prefs.getBoolean("tune_keep_alive", true));
        swCheckAuthentication.setChecked(prefs.getBoolean("check_authentication", true));
        swCheckTls.setChecked(prefs.getBoolean("check_tls", false));
        swCheckTls.setEnabled(swCheckAuthentication.isChecked());
        swCheckReply.setChecked(prefs.getBoolean("check_reply_domain", true));
        swCheckMx.setChecked(prefs.getBoolean("check_mx", false));
        swCheckBlocklist.setChecked(prefs.getBoolean("check_blocklist", false));
        swUseBlocklist.setChecked(prefs.getBoolean("use_blocklist", false));
        swUseBlocklist.setEnabled(swCheckBlocklist.isChecked());
        swUseBlocklistPop.setChecked(prefs.getBoolean("use_blocklist_pop", false));
        swUseBlocklistPop.setEnabled(swCheckBlocklist.isChecked());
    }

    private String formatHour(Context context, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
        cal.set(Calendar.MINUTE, minutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return Helper.getTimeInstance(context, SimpleDateFormat.SHORT).format(cal.getTime());
    }

    public static class TimePickerFragment extends FragmentDialogBase implements TimePickerDialog.OnTimeSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            boolean start = args.getBoolean("start");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            int minutes = prefs.getInt("schedule_" + (start ? "start" : "end"), 0);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
            cal.set(Calendar.MINUTE, minutes % 60);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return new TimePickerDialog(getContext(), this,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(getContext()));
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            Bundle args = getArguments();
            boolean start = args.getBoolean("start");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("schedule_" + (start ? "start" : "end"), hour * 60 + minute);
            editor.putBoolean("schedule", true);
            editor.apply();
        }
    }

    public static class AdapterAccountExempted extends RecyclerView.Adapter<AdapterAccountExempted.ViewHolder> {
        private Context context;
        private LifecycleOwner owner;
        private LayoutInflater inflater;

        private List<EntityAccount> items = new ArrayList<>();

        public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
            private CheckBox cbExempted;

            ViewHolder(View itemView) {
                super(itemView);
                cbExempted = itemView.findViewById(R.id.cbExempted);
            }

            private void wire() {
                cbExempted.setOnCheckedChangeListener(this);
            }

            private void unwire() {
                cbExempted.setOnCheckedChangeListener(null);
            }

            private void bindTo(EntityAccount account) {
                cbExempted.setEnabled(!account.ondemand && account.protocol == EntityAccount.TYPE_IMAP);
                cbExempted.setChecked(account.poll_exempted);
                cbExempted.setText(account.name);
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return;

                EntityAccount account = items.get(pos);

                Bundle args = new Bundle();
                args.putLong("id", account.id);
                args.putBoolean("exempted", isChecked);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        boolean exempted = args.getBoolean("exempted");

                        DB db = DB.getInstance(context);
                        db.account().setAccountPollExempted(id, exempted);

                        ServiceSynchronize.eval(context, "exempted");

                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.e(ex);
                    }
                }.execute(context, owner, args, "set:exempted");
            }
        }

        AdapterAccountExempted(LifecycleOwner owner, Context context) {
            this.owner = owner;
            this.context = context;
            this.inflater = LayoutInflater.from(context);

            setHasStableIds(true);
        }

        public void set(@NonNull List<EntityAccount> accounts) {
            Log.i("Set accounts=" + accounts.size());

            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, accounts), false);
            items = accounts;

            try {
                diff.dispatchUpdatesTo(this);
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        private class DiffCallback extends DiffUtil.Callback {
            private List<EntityAccount> prev = new ArrayList<>();
            private List<EntityAccount> next = new ArrayList<>();

            DiffCallback(List<EntityAccount> prev, List<EntityAccount> next) {
                this.prev.addAll(prev);
                this.next.addAll(next);
            }

            @Override
            public int getOldListSize() {
                return prev.size();
            }

            @Override
            public int getNewListSize() {
                return next.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                EntityAccount a1 = prev.get(oldItemPosition);
                EntityAccount a2 = next.get(newItemPosition);
                return a1.id.equals(a2.id);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                EntityAccount a1 = prev.get(oldItemPosition);
                EntityAccount a2 = next.get(newItemPosition);
                return (a1.ondemand == a2.ondemand &&
                        a1.poll_exempted == a2.poll_exempted &&
                        Objects.equals(a1.name, a2.name));
            }
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.item_account_exempted, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.unwire();
            EntityAccount account = items.get(position);
            holder.bindTo(account);
            holder.wire();
        }
    }

    public static class AdapterBlocklist extends RecyclerView.Adapter<AdapterBlocklist.ViewHolder> {
        private Context context;
        private LayoutInflater inflater;

        private List<DnsBlockList.BlockList> items;

        public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
            private CheckBox cbEnabled;

            ViewHolder(View itemView) {
                super(itemView);
                cbEnabled = itemView.findViewById(R.id.cbEnabled);
            }

            private void wire() {
                cbEnabled.setOnCheckedChangeListener(this);
            }

            private void unwire() {
                cbEnabled.setOnCheckedChangeListener(null);
            }

            private void bindTo(DnsBlockList.BlockList blocklist) {
                cbEnabled.setText(blocklist.name);
                cbEnabled.setChecked(DnsBlockList.isEnabled(context, blocklist));
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return;

                DnsBlockList.BlockList blocklist = items.get(pos);
                DnsBlockList.setEnabled(context, blocklist, isChecked);
            }
        }

        AdapterBlocklist(Context context, List<DnsBlockList.BlockList> items) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);

            setHasStableIds(true);
            this.items = items;
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.item_blocklist_enabled, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.unwire();
            DnsBlockList.BlockList blocklist = items.get(position);
            holder.bindTo(blocklist);
            holder.wire();
        }
    }
}
