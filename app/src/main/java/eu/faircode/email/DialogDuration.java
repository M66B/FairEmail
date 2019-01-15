package eu.faircode.email;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.lifecycle.LifecycleOwner;

public class DialogDuration {
    private static final long HOUR_MS = 3600L * 1000L;

    static void show(Context context, LifecycleOwner owner, int title, final IDialogDuration intf) {
        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_duration, null);
        final NumberPicker npHours = dview.findViewById(R.id.npHours);
        final NumberPicker npDays = dview.findViewById(R.id.npDays);
        final TextView tvTime = dview.findViewById(R.id.tvTime);
        final long now = new Date().getTime() / HOUR_MS * HOUR_MS;

        npHours.setMinValue(0);
        npHours.setMaxValue(24);

        npDays.setMinValue(0);
        npDays.setMaxValue(90);

        NumberPicker.OnValueChangeListener valueChanged = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int hours = npHours.getValue();
                int days = npDays.getValue();
                long duration = (hours + days * 24) * HOUR_MS;
                long time = now + duration;
                DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
                tvTime.setText(formatTime(time));
                tvTime.setVisibility(duration == 0 ? View.INVISIBLE : View.VISIBLE);
            }
        };

        npHours.setOnValueChangedListener(valueChanged);
        npDays.setOnValueChangedListener(valueChanged);
        valueChanged.onValueChange(null, 0, 0);

        new DialogBuilderLifecycle(context, owner)
                .setTitle(title)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int hours = npHours.getValue();
                        int days = npDays.getValue();
                        long duration = (hours + days * 24) * HOUR_MS;
                        long time = now + duration;
                        intf.onDurationSelected(duration, time);
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

    static String formatTime(long time) {
        DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
        return new SimpleDateFormat("E").format(time) + " " + df.format(time);
    }

    interface IDialogDuration {
        void onDurationSelected(long duration, long time);

        void onDismiss();
    }
}
