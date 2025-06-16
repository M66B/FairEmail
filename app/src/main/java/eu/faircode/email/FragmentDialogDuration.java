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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FragmentDialogDuration extends FragmentDialogBase {
    private final Calendar cal = Calendar.getInstance();

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong("fair:time", cal.getTimeInMillis());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString("title");
        boolean day = args.getBoolean("day");
        long time = args.getLong("time", 0);

        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int default_snooze = prefs.getInt("default_snooze", 1);
        if (default_snooze == 0)
            default_snooze = 1;

        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_duration, null);
        final Button btn1hour = dview.findViewById(R.id.btn1hour);
        final Button btn1day = dview.findViewById(R.id.btn1day);
        final Button btnMore = dview.findViewById(R.id.btnMore);
        final TextView tvDuration = dview.findViewById(R.id.tvDuration);
        final TimePicker timePicker = dview.findViewById(R.id.timePicker);
        final DatePicker datePicker = dview.findViewById(R.id.datePicker);
        final TextView tvSnoozeDoze = dview.findViewById(R.id.tvSnoozeDoze);

        final int colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
        final int textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        if (savedInstanceState == null) {
            if (time == 0) {
                cal.setTimeInMillis(new Date().getTime());
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.HOUR_OF_DAY, day ? 0 : cal.get(Calendar.HOUR_OF_DAY) + default_snooze);
            } else
                cal.setTimeInMillis(time);
        } else
            cal.setTimeInMillis(savedInstanceState.getLong("fair:time"));
        Log.i("Set init=" + new Date(cal.getTimeInMillis()));

        final DateFormat DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.FULL, SimpleDateFormat.SHORT);
        tvDuration.setText(DTF.format(cal.getTime()));
        tvDuration.setTextColor(cal.getTimeInMillis() < new Date().getTime() ? colorWarning : textColorSecondary);

        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(context));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
        } else {
            timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(cal.get(Calendar.MINUTE));
        }

        boolean isIgnoring = !Boolean.FALSE.equals(Helper.isIgnoringOptimizations(context));
        tvSnoozeDoze.setVisibility(isIgnoring ? View.GONE : View.VISIBLE);
        tvSnoozeDoze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        Dialog dialog = new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_timelapse_24)
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
                        args.putBoolean("reset", true);
                        args.putLong("duration", 0);
                        args.putLong("time", new Date().getTime());

                        sendResult(RESULT_OK);
                    }
                })
                .create();

        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                long now = new Date().getTime();

                long duration = 3600 * 1000L;
                if (view.getId() != R.id.btn1hour)
                    duration *= 24;

                Bundle args = getArguments();
                args.putLong("duration", duration);
                args.putLong("time", now + duration);

                sendResult(RESULT_OK);
            }
        };

        btn1hour.setOnClickListener(buttonListener);
        btn1day.setOnClickListener(buttonListener);

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, getViewLifecycleOwner(), btnMore);
                popupMenu.inflate(R.menu.menu_duration);

                DateFormat dtf = Helper.getTimeInstance(context, SimpleDateFormat.SHORT);

                Calendar cal = Calendar.getInstance();
                long now = cal.getTimeInMillis();
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.HOUR_OF_DAY, 8);
                long morning = cal.getTimeInMillis();
                String at8am = dtf.format(cal.getTimeInMillis());
                cal.set(Calendar.HOUR_OF_DAY, 12);
                long afternoon = cal.getTimeInMillis();
                String at12pm = dtf.format(cal.getTimeInMillis());
                cal.set(Calendar.HOUR_OF_DAY, 18);
                long evening = cal.getTimeInMillis();
                String at18pm = dtf.format(cal.getTimeInMillis());
                cal.add(Calendar.DATE, 1);
                int tomorrow = cal.get(Calendar.DAY_OF_WEEK);
                cal.add(Calendar.DATE, 1);
                int after_tomorrow = cal.get(Calendar.DAY_OF_WEEK);
                String[] daynames = new DateFormatSymbols().getWeekdays();

                popupMenu.getMenu().findItem(R.id.menu_this_afternoon)
                        .setTitle(getString(R.string.title_today_at, at12pm))
                        .setVisible(now < afternoon);
                popupMenu.getMenu().findItem(R.id.menu_this_evening)
                        .setTitle(getString(R.string.title_today_at, at18pm))
                        .setVisible(now < evening);

                popupMenu.getMenu().findItem(R.id.menu_tomorrow_morning)
                        .setTitle(getString(R.string.title_tomorrow_at, at8am));
                popupMenu.getMenu().findItem(R.id.menu_tomorrow_afternoon)
                        .setTitle(getString(R.string.title_tomorrow_at, at12pm));

                popupMenu.getMenu().findItem(R.id.menu_after_tomorrow_morning)
                        .setTitle(getString(R.string.title_day_at_time, daynames[after_tomorrow], at8am));
                popupMenu.getMenu().findItem(R.id.menu_after_tomorrow_afternoon)
                        .setTitle(getString(R.string.title_day_at_time, daynames[after_tomorrow], at12pm));

                popupMenu.getMenu().findItem(R.id.menu_saturday_norming)
                        .setTitle(getString(R.string.title_day_at_time, daynames[Calendar.SATURDAY], at8am))
                        .setVisible(tomorrow != Calendar.SATURDAY && after_tomorrow != Calendar.SATURDAY);
                popupMenu.getMenu().findItem(R.id.menu_monday_norming)
                        .setTitle(getString(R.string.title_day_at_time, daynames[Calendar.MONDAY], at8am))
                        .setVisible(tomorrow != Calendar.MONDAY && after_tomorrow != Calendar.MONDAY);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        dialog.dismiss();

                        Calendar cal = Calendar.getInstance();
                        long now = cal.getTimeInMillis();

                        int itemId = item.getItemId();
                        if (itemId == R.id.menu_tomorrow_morning ||
                                itemId == R.id.menu_after_tomorrow_morning ||
                                itemId == R.id.menu_saturday_norming ||
                                itemId == R.id.menu_monday_norming) {
                            cal.set(Calendar.HOUR_OF_DAY, 8);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            if (itemId == R.id.menu_tomorrow_morning)
                                cal.add(Calendar.DATE, 1);
                            else if (itemId == R.id.menu_after_tomorrow_morning)
                                cal.add(Calendar.DATE, 2);
                            else if (itemId == R.id.menu_saturday_norming) {
                                cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                                if (cal.getTimeInMillis() < now)
                                    cal.add(Calendar.DATE, 7);
                            } else if (itemId == R.id.menu_monday_norming) {
                                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                                if (cal.getTimeInMillis() < now)
                                    cal.add(Calendar.DATE, 7);
                            }
                        } else if (itemId == R.id.menu_this_afternoon ||
                                itemId == R.id.menu_tomorrow_afternoon ||
                                itemId == R.id.menu_after_tomorrow_afternoon) {
                            cal.set(Calendar.HOUR_OF_DAY, 12);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            if (itemId == R.id.menu_tomorrow_afternoon)
                                cal.add(Calendar.DATE, 1);
                            else if (itemId == R.id.menu_after_tomorrow_afternoon)
                                cal.add(Calendar.DATE, 2);
                        } else if (itemId == R.id.menu_this_evening) {
                            cal.set(Calendar.HOUR_OF_DAY, 18);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                        } else if (itemId == R.id.menu_next_week)
                            cal.add(Calendar.DATE, 7);

                        Bundle args = getArguments();
                        args.putLong("duration", cal.getTimeInMillis() - now);
                        args.putLong("time", cal.getTimeInMillis());

                        sendResult(RESULT_OK);
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hour, int minute) {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                tvDuration.setText(DTF.format(cal.getTime()));
                tvDuration.setTextColor(cal.getTimeInMillis() < new Date().getTime() ? colorWarning : textColorSecondary);
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
                        tvDuration.setTextColor(cal.getTimeInMillis() < new Date().getTime() ? colorWarning : textColorSecondary);
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
                startActivity(new Intent(getContext(), ActivityBilling.class));
                result = RESULT_CANCELED;
            }
        }

        super.sendResult(result);
    }
}
