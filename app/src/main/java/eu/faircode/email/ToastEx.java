package eu.faircode.email;

import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastEx extends Toast {
    public ToastEx(Context context) {
        super(context.getApplicationContext());
    }

    public static ToastEx makeText(Context context, int resId, int duration) throws Resources.NotFoundException {
        return makeText(context, context.getText(resId), duration);
    }

    public static ToastEx makeText(Context context, CharSequence text, int duration) {
        ToastEx toast = new ToastEx(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.toast, null);
        view.setBackground(context.getResources().getDrawable(R.drawable.toast_background, context.getTheme()));

        TextView tv = view.findViewById(android.R.id.message);
        tv.setTextColor(Helper.resolveColor(context, R.attr.colorInfoForeground));
        tv.setText(text);
        toast.setView(view);
        toast.setDuration(duration);
        // <dimen name="design_bottom_navigation_height">56dp</dimen>
        int dp = Helper.dp2pixels(context, 2 * 56);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, dp);
        return toast;
    }

    @Override
    public void show() {
        // https://developer.android.com/preview/features/toasts
        if (Looper.myLooper() != Looper.getMainLooper())
            Log.e("Toast from background");

        // https://stackoverflow.com/questions/56017928/toast-not-showing-in-android-q
        super.show();
    }
}
