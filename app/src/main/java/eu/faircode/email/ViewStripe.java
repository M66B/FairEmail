package eu.faircode.email;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

public class ViewStripe extends CardView {
    public ViewStripe(@NonNull Context context) {
        super(context);
    }

    public ViewStripe(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewStripe(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean circular = prefs.getBoolean("circular", true);
        setRadius(circular ? Helper.dp2pixels(getContext(), 3) / 2f : 0f);
        setElevation(0);
    }

    @Override
    public void setBackgroundColor(int color) {
        setCardBackgroundColor(color);
    }
}
