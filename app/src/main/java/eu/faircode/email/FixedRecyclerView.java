package eu.faircode.email;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class FixedRecyclerView extends RecyclerView {
    public FixedRecyclerView(@NonNull Context context) {
        super(context);
    }

    public FixedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        try {
            return super.onInterceptTouchEvent(e);
        } catch (IllegalStateException ex) {
            // Range start point not set
            Log.w(ex);
            return false;
        }
    }
}
