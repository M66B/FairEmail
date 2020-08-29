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
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class ViewTextDelayed extends AppCompatTextView {
    private int visibility = VISIBLE;

    private static final int VISIBILITY_DELAY = 500; // milliseconds

    public ViewTextDelayed(@NonNull Context context) {
        super(context);
    }

    public ViewTextDelayed(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewTextDelayed(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVisibility(int visibility) {
        this.visibility = visibility;

        ApplicationEx.getMainHandler().removeCallbacks(delayedShow);
        ApplicationEx.getMainHandler().removeCallbacks(delayedHide);

        if (visibility == VISIBLE)
            ApplicationEx.getMainHandler().postDelayed(delayedShow, VISIBILITY_DELAY);
        else if (visibility == GONE)
            ApplicationEx.getMainHandler().postDelayed(delayedHide, VISIBILITY_DELAY);
        else
            super.setVisibility(visibility);
    }

    @Override
    public int getVisibility() {
        return this.visibility;
    }

    private final Runnable delayedShow = new Runnable() {
        @Override
        public void run() {
            if (visibility == VISIBLE)
                ViewTextDelayed.super.setVisibility(VISIBLE);
        }
    };

    private final Runnable delayedHide = new Runnable() {
        @Override
        public void run() {
            if (visibility == GONE)
                ViewTextDelayed.super.setVisibility(GONE);
        }
    };
}
