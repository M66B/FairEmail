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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

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

    void setup(Configuration config, View drawerContainer) {
        setScrimColor(Helper.resolveColor(getContext(), R.attr.colorDrawerScrim));
        if (config.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            setDrawerLockMode(LOCK_MODE_UNLOCKED);
            closeDrawer(drawerContainer, false);
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
        return (!isLocked() && super.onInterceptTouchEvent(ev));
    }
}
