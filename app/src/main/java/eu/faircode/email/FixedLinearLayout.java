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
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class FixedLinearLayout extends LinearLayout {
    public FixedLinearLayout(Context context) {
        super(context);
    }

    public FixedLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FixedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    @Override
    public boolean post(Runnable action) {
        return super.post(new RunnableEx("post") {
            @Override
            protected void delegate() {
                action.run();
            }
        });
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        return super.postDelayed(new RunnableEx("postDelayed") {
            @Override
            protected void delegate() {
                action.run();
            }
        }, delayMillis);
    }
}