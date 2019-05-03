package eu.faircode.email;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class SwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    SwipeListener(final Context context, final ISwipeListener listener) {
        final int width = context.getResources().getDisplayMetrics().widthPixels;
        final int MOVE_THRESHOLD = width / 3;
        final int SPEED_THRESHOLD = width;

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent me1, MotionEvent me2, float vx, float vy) {
                if (me1 == null || me2 == null)
                    return false;

                boolean consumed = false;
                int dx = Math.round(me2.getX() - me1.getX());
                int dy = Math.round(me2.getY() - me1.getY());
                if (Math.abs(dx) > Math.abs(dy)) {
                    Log.i("Swipe dx=" + dx + "/" + MOVE_THRESHOLD + " vx=" + vx + "/" + SPEED_THRESHOLD);
                    if (Math.abs(dx) > MOVE_THRESHOLD && Math.abs(vx) > SPEED_THRESHOLD)
                        try {
                            if (dx > 0)
                                consumed = listener.onSwipeRight();
                            else
                                consumed = listener.onSwipeLeft();
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                }
                return consumed;
            }
        });
    }

    public boolean onTouch(@NonNull View view, @NonNull MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    interface ISwipeListener {
        boolean onSwipeRight();

        boolean onSwipeLeft();
    }
}