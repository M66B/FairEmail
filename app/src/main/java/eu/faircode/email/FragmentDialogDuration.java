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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentDialogDuration extends FragmentDialogBase {
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
        final Button btn1hour = dview.findViewById(R.id.btn1hour);
        final Button btn1day = dview.findViewById(R.id.btn1day);
        final TimePicker timePicker = dview.findViewById(R.id.timePicker);
        final DatePicker datePicker = dview.findViewById(R.id.datePicker);

        if (savedInstanceState == null)
            cal.setTimeInMillis((new Date().getTime() / (3600 * 1000L) + 1) * (3600 * 1000L));
        else
            cal.setTimeInMillis(savedInstanceState.getLong("fair:time"));
        Log.i("Set init=" + new Date(cal.getTimeInMillis()));

        final DateFormat DTF = Helper.getDateTimeInstance(getContext(), SimpleDateFormat.FULL, SimpleDateFormat.SHORT);
        tvDuration.setText(DTF.format(cal.getTime()));

        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getContext()));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
        } else {
            timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(cal.get(Calendar.MINUTE));
        }

        Dialog dialog = new AlertDialog.Builder(getContext())
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

                        Bundle args = getArguments();
                        args.putLong("duration", duration);
                        args.putLong("time", cal.getTimeInMillis());

                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle args = getArguments();
                        args.putLong("duration", 0);
                        args.putLong("time", new Date().getTime());

                        sendResult(RESULT_OK);
                    }
                })
                .create();

        btn1hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                long now = new Date().getTime();
                long duration = 3600 * 1000L;

                Bundle args = getArguments();
                args.putLong("duration", duration);
                args.putLong("time", now + duration);

                sendResult(RESULT_OK);
            }
        });

        btn1day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                long now = new Date().getTime();
                long duration = 24 * 3600 * 1000L;

                Bundle args = getArguments();
                args.putLong("duration", duration);
                args.putLong("time", now + duration);

                sendResult(RESULT_OK);
            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hour, int minute) {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                tvDuration.setText(DTF.format(cal.getTime()));
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
                        tvDuration.setText(DTF.format(cal.getTime()));
                        Log.i("Set year=" + year + " month=" + month + " day=" + day +
                                " time=" + new Date(cal.getTimeInMillis()));
                    }
                }
        );

        return dialog;
    }

    @Override
    protected void sendResult(int result) {
        if (result == RESULT_OK) {
            if (!ActivityBilling.isPro(getContext())) {
                getActivity().startActivity(new Intent(getContext(), ActivityBilling.class));
                result = RESULT_CANCELED;
            }
        }

        super.sendResult(result);
    }
}
