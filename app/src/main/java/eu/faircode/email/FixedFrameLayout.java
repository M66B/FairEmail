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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class FixedFrameLayout extends FrameLayout {
    public FixedFrameLayout(@NonNull Context context) {
        super(context);
    }

    public FixedFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FixedFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            super.onLayout(changed, left, top, right, bottom);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        try {
            return super.dispatchGenericMotionEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    private Map<Runnable, Runnable> mapRunnable = null;

    @NonNull
    private Map<Runnable, Runnable> getMapRunnable() {
        if (mapRunnable == null)
            mapRunnable = new WeakHashMap<>();
        return mapRunnable;
    }

    @Override
    public boolean post(Runnable action) {
        Runnable wrapped = new RunnableEx("post") {
            @Override
            protected void delegate() {
                action.run();
            }
        };
        getMapRunnable().put(action, wrapped);
        return super.post(wrapped);
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        Runnable wrapped = new RunnableEx("postDelayed") {
            @Override
            protected void delegate() {
                action.run();
            }
        };
        getMapRunnable().put(action, wrapped);
        return super.postDelayed(wrapped, delayMillis);
    }

    @Override
    public boolean removeCallbacks(Runnable action) {
        Runnable wrapped = getMapRunnable().get(action);
        if (wrapped == null)
            return super.removeCallbacks(action);
        else
            return super.removeCallbacks(wrapped);
    }
}
