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
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Spinner;

public class SpinnerEx extends Spinner {
    public SpinnerEx(Context context) {
        super(context);
        init(context);
    }

    public SpinnerEx(Context context, int mode) {
        super(context, mode);
        init(context);
    }

    public SpinnerEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SpinnerEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SpinnerEx(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
        init(context);
    }

    public SpinnerEx(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
        init(context);
    }

    public SpinnerEx(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes, mode, popupTheme);
        init(context);
    }

    private void init(Context context) {
        this.setFocusableInTouchMode(true);
    }

    private final OnFocusChangeListener listener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            setOnFocusChangeListener(null);
            performClick();
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (!hasFocus())
                setOnFocusChangeListener(listener);
        } else if (action == MotionEvent.ACTION_CANCEL)
            setOnFocusChangeListener(null);

        return super.onTouchEvent(event);
    }
}
