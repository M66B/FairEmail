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

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FragmentOptionsGeneral extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swEnabled;
    private Spinner spPollInterval;
    private SwitchCompat swSchedule;
    private TextView tvScheduleStart;
    private TextView tvScheduleEnd;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);

        View view = inflater.inflate(R.layout.fragment_options_general, container, false);

        // Get controls

        swEnabled = view.findViewById(R.id.swEnabled);
        spPollInterval = view.findViewById(R.id.spPollInterval);
        swSchedule = view.findViewById(R.id.swSchedule);
        tvScheduleStart = view.findViewById(R.id.tvScheduleStart);
        tvScheduleEnd = view.findViewById(R.id.tvScheduleEnd);

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        setOptions();

        // General

        swEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("enabled", checked).apply();
                spPollInterval.setEnabled(checked);
                swSchedule.setEnabled(checked);
                ServiceSynchronize.reload(getContext(), true, "enabled=" + checked);
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
                    WorkerPoll.init(getContext());
                    ServiceSynchronize.reload(getContext(), "poll");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("poll_interval").apply();
                WorkerPoll.init(getContext());
                ServiceSynchronize.reload(getContext(), "poll");
            }
        });

        swSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    if (Helper.isPro(getContext())) {
                        prefs.edit().putBoolean("schedule", true).apply();
                        ServiceSynchronize.reschedule(getContext());
                    } else {
                        swSchedule.setChecked(false);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                        fragmentTransaction.commit();
                    }
                } else {
                    prefs.edit().putBoolean("schedule", false).apply();
                    ServiceSynchronize.reload(getContext(), "schedule=" + checked);
                }
            }
        });

        tvScheduleStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("start", true);
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.setArguments(args);
                timePicker.show(getFragmentManager(), "timePicker");
            }
        });

        tvScheduleEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("start", false);
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.setArguments(args);
                timePicker.show(getFragmentManager(), "timePicker");
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

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swEnabled.setChecked(prefs.getBoolean("enabled", true));
        spPollInterval.setEnabled(swEnabled.isChecked());
        swSchedule.setEnabled(swEnabled.isChecked());

        int pollInterval = prefs.getInt("poll_interval", 0);
        int[] pollIntervalValues = getResources().getIntArray(R.array.pollIntervalValues);
        for (int pos = 0; pos < pollIntervalValues.length; pos++)
            if (pollIntervalValues[pos] == pollInterval) {
                spPollInterval.setTag(pollInterval);
                spPollInterval.setSelection(pos);
                break;
            }

        swSchedule.setChecked(prefs.getBoolean("schedule", false));
        tvScheduleStart.setText(formatHour(getContext(), prefs.getInt("schedule_start", 0)));
        tvScheduleEnd.setText(formatHour(getContext(), prefs.getInt("schedule_end", 0)));
    }

    private String formatHour(Context context, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
        cal.set(Calendar.MINUTE, minutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return Helper.getTimeInstance(context, SimpleDateFormat.SHORT).format(cal.getTime());
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
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

            return new TimePickerDialog(getActivity(), this,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            Bundle args = getArguments();
            boolean start = args.getBoolean("start");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("schedule_" + (start ? "start" : "end"), hour * 60 + minute);
            editor.putBoolean("schedule", true);
            editor.apply();

            ServiceSynchronize.reschedule(getContext());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("enabled".equals(key))
            swEnabled.setChecked(prefs.getBoolean(key, true));
        else if ("schedule".equals(key))
            swSchedule.setChecked(prefs.getBoolean(key, false));
        else if ("schedule_start".equals(key))
            tvScheduleStart.setText(formatHour(getContext(), prefs.getInt(key, 0)));
        else if ("schedule_end".equals(key))
            tvScheduleEnd.setText(formatHour(getContext(), prefs.getInt(key, 0)));
    }
}
