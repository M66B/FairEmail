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

public class ViewImageHint extends AppCompatImageView implements View.OnLongClickListener {
    private OnLongClickListener listener;

    public ViewImageHint(@NonNull Context context) {
        super(context);
        setOnLongClickListener(this);
    }

    public ViewImageHint(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOnLongClickListener(this);
    }

    public ViewImageHint(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnLongClickListener(this);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        if (listener == this) {
            super.setOnLongClickListener(listener);
            return;
        }

        this.listener = listener;
    }

    @Override
    public boolean onLongClick(View v) {
        if (listener == null || !listener.onLongClick(v)) {
            String title = getContentDescription().toString();
            if (!TextUtils.isEmpty(title)) {
                int[] pos = new int[2];
                getLocationInWindow(pos);

                Toast toast = ToastEx.makeText(getContext(), title, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.START, pos[0], pos[1]);
                toast.show();

                return true;
            }
        }

        return false;
    }
}