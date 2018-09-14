package eu.faircode.email;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ConstraintLayoutTouch extends ConstraintLayout {
    private IGestureListener listener = null;

    enum Direction {Left, Right, Up, Down}

    private static final long VELOCITY_THRESHOLD = 3000;

    public ConstraintLayoutTouch(Context context) {
        super(context);
    }

    public ConstraintLayoutTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConstraintLayoutTouch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (listener != null)
            gestureDetector.onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    void setGestureListener(IGestureListener listener) {
        this.listener = listener;
    }

    private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(final MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(final MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX,
                                final float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(final MotionEvent e) {
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2,
                               final float velocityX,
                               final float velocityY) {

            if (Math.abs(velocityX) < VELOCITY_THRESHOLD && Math.abs(velocityY) < VELOCITY_THRESHOLD)
                return false;

            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                if (velocityX >= 0)
                    listener.onSwipe(Direction.Right);
                else
                    listener.onSwipe(Direction.Left);
            } else {
                if (velocityY >= 0)
                    listener.onSwipe(Direction.Down);
                else
                    listener.onSwipe(Direction.Up);
            }

            return true;
        }
    });

    interface IGestureListener {
        void onSwipe(Direction direction);
    }
}
