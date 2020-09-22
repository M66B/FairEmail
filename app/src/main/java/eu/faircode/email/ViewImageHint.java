package eu.faircode.email;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class ViewImageHint extends AppCompatImageView implements View.OnClickListener {
    public ViewImageHint(@NonNull Context context) {
        super(context);
        setOnClickListener(this);
    }

    public ViewImageHint(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public ViewImageHint(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String title = getContentDescription().toString();
        if (!TextUtils.isEmpty(title)) {
            int[] pos = new int[2];
            getLocationOnScreen(pos);
            int dp6 = Helper.dp2pixels(v.getContext(), 6);

            Toast toast = ToastEx.makeTextBw(getContext(), title, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.START, pos[0], pos[1] + dp6);
            toast.show();
        }
    }
}