package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class SwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    SwipeListener(final Context context, final ISwipeListener listener) {
        final int width = Math.min(
                context.getResources().getDisplayMetrics().widthPixels,
                context.getResources().getDisplayMetrics().heightPixels);
        final int MOVE_THRESHOLD = width / 3;
        final int SPEED_THRESHOLD = width / 2;

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent me1, MotionEvent me2, float distanceX, float distanceY) {
                if (me1 == null || me2 == null)
                    return false;
                if (me1.getPointerCount() > 1 || me2.getPointerCount() > 1)
                    return false;

                boolean consumed = false;
                int dx = Math.round(me2.getX() - me1.getX());
                int dy = Math.round(me2.getY() - me1.getY());
                long dt = me2.getEventTime() - me1.getEventTime();
                long vx = (dt == 0 ? 0 : dx * 1000 / dt);
                if (Math.abs(dx) > Math.abs(dy)) {
                    Log.i("Swipe dx=" + dx + "/" + MOVE_THRESHOLD +
                            " dt=" + dt + " vx=" + vx + "/" + SPEED_THRESHOLD);
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