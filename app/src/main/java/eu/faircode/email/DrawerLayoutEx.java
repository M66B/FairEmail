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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;

public class DrawerLayoutEx extends DrawerLayout {
    public DrawerLayoutEx(@NonNull Context context) {
        super(context);
    }

    public DrawerLayoutEx(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerLayoutEx(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isLocked(View view) {
        return (getDrawerLockMode(view) != LOCK_MODE_UNLOCKED);
    }

    private boolean isLocked() {
        return (getDrawerLockMode(Gravity.LEFT) == LOCK_MODE_LOCKED_OPEN ||
                getDrawerLockMode(Gravity.RIGHT) == LOCK_MODE_LOCKED_OPEN);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        if (isLocked()) {
            Rect rect = new Rect();
            getChildAt(1).getHitRect(rect);
            if (!rect.contains((int) ev.getX(), (int) ev.getY()))
                return false;
        }

        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Throwable ex) {
            Log.w(ex);
/*
            java.lang.NullPointerException: Attempt to get length of null array
            java.lang.NullPointerException: Attempt to get length of null array
            at androidx.customview.widget.ViewDragHelper.checkTouchSlop(SourceFile:1334)
            at androidx.drawerlayout.widget.DrawerLayout.onInterceptTouchEvent(SourceFile:1512)
*/
            return false;
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
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        if (isLocked()) {
            View content = getChildAt(0);
            Rect rect = new Rect();
            content.getHitRect(rect);
            rect.left += content.getPaddingLeft();
            rect.right -= content.getPaddingRight();
            if (rect.contains((int) ev.getX(), (int) ev.getY()))
                return content.dispatchGenericMotionEvent(ev);
        }

        try {
            return super.dispatchGenericMotionEvent(ev);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (getDrawerLockMode(Gravity.LEFT) == LOCK_MODE_LOCKED_OPEN) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == View.VISIBLE && isContentView(child))
                    child.addFocusables(views, direction, focusableMode);
            }
        } else
            super.addFocusables(views, direction, focusableMode);
    }

    boolean isContentView(View child) {
        return ((LayoutParams) child.getLayoutParams()).gravity == Gravity.NO_GRAVITY;
    }
}
