package eu.faircode.email;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import androidx.lifecycle.LifecycleOwner;

public class DialogDuration {
    static void show(Context context, LifecycleOwner owner, int title, final IDialogDuration intf) {
        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_duration, null);
        final TimePicker timePicker = dview.findViewById(R.id.timePicker);
        final DatePicker datePicker = dview.findViewById(R.id.datePicker);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime() / (60 * 1000L) * (60 * 1000L));
        Log.i("Set init=" + new Date(cal.getTimeInMillis()));

        timePicker.setIs24HourView(DateFormat.is24HourFormat(context));
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
                        Log.i("Set year=" + year + " month=" + month + " day=" + day +
                                " time=" + new Date(cal.getTimeInMillis()));
                    }
                }
        );

        new DialogBuilderLifecycle(context, owner)
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
                        intf.onDurationSelected(duration, cal.getTimeInMillis());
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        intf.onDismiss();
                    }
                })
                .show();
    }

    interface IDialogDuration {
        void onDurationSelected(long duration, long time);

        void onDismiss();
    }
}
