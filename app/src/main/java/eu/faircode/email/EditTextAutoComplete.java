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
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.textclassifier.TextClassifier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class EditTextAutoComplete extends AppCompatAutoCompleteTextView {
    public EditTextAutoComplete(@NonNull Context context) {
        super(context);
    }

    public EditTextAutoComplete(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextAutoComplete(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSelection(int index) {
        try {
            super.setSelection(index);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void setSelection(int start, int stop) {
        try {
            super.setSelection(start, stop);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        try {
            super.setText(text, type);
        } catch (Throwable ex) {
            Log.w(ex);
        }
    }

    @Override
    public boolean onPreDraw() {
        try {
            return super.onPreDraw();
        } catch (Throwable ex) {
            Log.w(ex);
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Throwable ex) {
            Log.w(ex);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            return super.dispatchTouchEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        try {
            super.dispatchWindowFocusChanged(hasFocus);
        } catch (Throwable ex) {
            Log.w(ex);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        try {
            return super.onKeyPreIme(keyCode, event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        try {
            return super.onKeyUp(keyCode, event);
        } catch (Throwable ex) {
            Log.w(ex);
            return true;
        }
    }

    @Override
    public boolean performClick() {
        try {
            return super.performClick();
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public boolean performLongClick() {
        try {
            return super.performLongClick();
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        try {
            return super.startActionMode(callback);
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        try {
            return super.startActionMode(callback, type);
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    @NonNull
    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public TextClassifier getTextClassifier() {
        if (BuildConfig.DEBUG /*|| Helper.isSamsung()*/)
            return TextClassifier.NO_OP;
        else
            return super.getTextClassifier();
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        try {
            return super.onTextContextMenuItem(id);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }
}
