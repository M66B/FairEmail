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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class TextViewAutoCompleteAction extends AppCompatAutoCompleteTextView {
    private Drawable drawable = null;
    private Runnable action = null;
    private boolean enabled = false;

    public TextViewAutoCompleteAction(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public TextViewAutoCompleteAction(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TextViewAutoCompleteAction(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        if (attrs == null)
            drawable = getContext().getDrawable(R.drawable.twotone_warning_24);
        else {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextViewAutoCompleteAction, 0, 0);
            drawable = a.getDrawable(R.styleable.TextViewAutoCompleteAction_end_drawable);
        }
        int colorControlNormal = Helper.resolveColor(context, androidx.appcompat.R.attr.colorControlNormal);
        drawable.setTint(colorControlNormal);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!enabled)
                    return false;
                if (getCompoundDrawables()[2] == null)
                    return false;
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > getWidth() - getPaddingRight() - drawable.getIntrinsicWidth()) {
                    if (action != null)
                        action.run();
                }
                return false;
            }
        });

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setActionEnabled(enabled);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
    }

    public void setActionRunnable(Runnable action) {
        this.action = action;
    }

    public void setActionEnabled(boolean enabled) {
        this.enabled = enabled;
        Drawable d = (enabled && getText().length() > 0 ? drawable : null);
        setCompoundDrawablesRelative(null, null, d, null);
    }
}
