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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

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

    void setup(Configuration config, View drawerContainer, ActionBarDrawerToggle drawerToggle) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean normal = config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL);
        boolean landscape = prefs.getBoolean("landscape", true);
        boolean landscape3 = prefs.getBoolean("landscape3", false);

        if (normal && landscape && landscape3 &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setScrimColor(Color.TRANSPARENT);
            openDrawer(drawerContainer, false);
            drawerToggle.onDrawerOpened(drawerContainer);
        } else {
            setScrimColor(Helper.resolveColor(getContext(), R.attr.colorDrawerScrim));
            closeDrawer(drawerContainer, false);
            drawerToggle.onDrawerClosed(drawerContainer);
        }
    }

    public boolean isLocked(View view) {
        return (getDrawerLockMode(view) != LOCK_MODE_UNLOCKED);
    }

    public boolean isLocked() {
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

        return super.dispatchGenericMotionEvent(ev);
    }
}
