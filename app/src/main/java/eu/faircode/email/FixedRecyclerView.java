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
    public boolean onTouchEvent(MotionEvent e) {
        try {
            return super.onTouchEvent(e);
        } catch (NullPointerException ex) {
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'int android.view.View.getTop()' on a null object reference
                java.lang.NullPointerException: Attempt to invoke virtual method 'int android.view.View.getTop()' on a null object reference
                at androidx.recyclerview.selection.GestureSelectionHelper$RecyclerViewDelegate.getLastGlidedItemPosition(SourceFile:287)
                at androidx.recyclerview.selection.GestureSelectionHelper.handleMoveEvent(SourceFile:202)
                at androidx.recyclerview.selection.GestureSelectionHelper.handleTouch(SourceFile:151)
                at androidx.recyclerview.selection.GestureSelectionHelper.onInterceptTouchEvent(SourceFile:118)
                at androidx.recyclerview.selection.TouchEventRouter.onInterceptTouchEvent(SourceFile:91)
                at androidx.recyclerview.widget.RecyclerView.dispatchOnItemTouch(SourceFile:2962)
                at androidx.recyclerview.widget.RecyclerView.onTouchEvent(SourceFile:3090)
             */
            Log.w(ex);
            return false;
        } catch (IllegalStateException ex) {
            // Range start point not set
            Log.w(ex);
            return false;
        }
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
