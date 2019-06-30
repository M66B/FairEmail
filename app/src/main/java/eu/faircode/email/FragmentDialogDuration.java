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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentDialogDuration extends DialogFragment {
    private Calendar cal = Calendar.getInstance();

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong("fair:time", cal.getTimeInMillis());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String title = getArguments().getString("title");

        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_duration, null);
        final TextView tvDuration = dview.findViewById(R.id.tvDuration);
        final TimePicker timePicker = dview.findViewById(R.id.timePicker);
        final DatePicker datePicker = dview.findViewById(R.id.datePicker);

        if (savedInstanceState == null)
            cal.setTimeInMillis((new Date().getTime() / (3600 * 1000L) + 1) * (3600 * 1000L));
        else
            cal.setTimeInMillis(savedInstanceState.getLong("fair:time"));
        Log.i("Set init=" + new Date(cal.getTimeInMillis()));

        final DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.SHORT);
        tvDuration.setText(df.format(cal.getTime()));

        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getContext()));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
        } else {
            timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(cal.get(Calendar.MINUTE));
        }

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hour, int minute) {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                tvDuration.setText(df.format(cal.getTime()));
                Log.i("Set hour=" + hour + " minute=" + minute +
                        " time=" + new Date(cal.getTimeInMillis()));
            }
        });

        datePicker.init(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int month, int day) {
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, day);
                        tvDuration.setText(df.format(cal.getTime()));
                        Log.i("Set year=" + year + " month=" + month + " day=" + day +
                                " time=" + new Date(cal.getTimeInMillis()));
                    }
                }
        );

        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long now = new Date().getTime();
                        long duration = (cal.getTimeInMillis() - now);
                        if (duration < 0)
                            duration = 0;
                        Log.i("Set duration=" + duration + " time=" + new Date(cal.getTimeInMillis()));
                        sendResult(RESULT_OK, duration, cal.getTimeInMillis());
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        sendResult(RESULT_CANCELED, 0, 0);
                    }
                })
                .create();
    }

    private void sendResult(int result, long duration, long time) {
        Bundle args = getArguments();
        args.putLong("duration", duration);
        args.putLong("time", time);

        Fragment target = getTargetFragment();
        if (target != null) {
            Intent data = new Intent();
            data.putExtra("args", args);
            target.onActivityResult(getTargetRequestCode(), result, data);
        }
    }
}
