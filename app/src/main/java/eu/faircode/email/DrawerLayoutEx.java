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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

public class DrawerLayoutEx extends DrawerLayout {
    private boolean locked = false;

    public DrawerLayoutEx(@NonNull Context context) {
        super(context);
    }

    public DrawerLayoutEx(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerLayoutEx(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void setup(Configuration config) {
        setScrimColor(Helper.resolveColor(getContext(), R.attr.colorDrawerScrim));

/*
        ViewGroup childContent = (ViewGroup) getChildAt(0);
        ViewGroup childDrawer = (ViewGroup) getChildAt(1);
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            locked = true;
            setDrawerLockMode(LOCK_MODE_LOCKED_OPEN);
            setScrimColor(Color.TRANSPARENT);
            childContent.setPaddingRelative(childDrawer.getLayoutParams().width, 0, 0, 0);
        } else {
            locked = false;
            setDrawerLockMode(LOCK_MODE_UNLOCKED);
            setScrimColor(Helper.resolveColor(getContext(), R.attr.colorDrawerScrim));
            childContent.setPaddingRelative(0, 0, 0, 0);
            closeDrawers();
        }
 */
    }

    @Override
    public boolean isDrawerOpen(@NonNull View drawer) {
        return (!locked && super.isDrawerOpen(drawer));
    }

    @Override
    public boolean isDrawerOpen(int drawerGravity) {
        return (!locked && super.isDrawerOpen(drawerGravity));
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        return (!locked && super.onInterceptTouchEvent(ev));
    }

    @Override
    public void closeDrawer(@NonNull View drawerView) {
        if (!locked)
            super.closeDrawer(drawerView);
    }
}
