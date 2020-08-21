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
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ContentLoadingProgressBar extends ProgressBar {
    private int visibility;
    private Handler handler;

    private static final int VISIBILITY_DELAY = 500; // milliseconds

    public ContentLoadingProgressBar(@NonNull Context context) {
        this(context, null);
        init();
    }

    public ContentLoadingProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public ContentLoadingProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ContentLoadingProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void setVisibility(int visibility) {
        this.visibility = visibility;

        handler.removeCallbacks(delayedShow);
        if (visibility == VISIBLE) {
            super.setVisibility(INVISIBLE);
            handler.postDelayed(delayedShow, VISIBILITY_DELAY);
        } else
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
                ContentLoadingProgressBar.super.setVisibility(VISIBLE);
        }
    };
}
