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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class FixedEditText extends AppCompatEditText {
    public FixedEditText(@NonNull Context context) {
        super(context);
    }

    public FixedEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSelection(int index) {
        try {
            super.setSelection(index);
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.IndexOutOfBoundsException: setSpan (2 ... 2) ends beyond length 0
                        at android.text.SpannableStringBuilder.checkRange(SpannableStringBuilder.java:1265)
                        at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:684)
                        at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:677)
                        at android.text.Selection.setSelection(Selection.java:76)
                        at android.widget.EditText.setSelection(EditText.java:96)
                        at android.widget.NumberPicker$SetSelectionCommand.run(NumberPicker.java:2246)
                        at android.os.Handler.handleCallback(Handler.java:754)
                        at android.os.Handler.dispatchMessage(Handler.java:95)
             */
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
            /*
                java.lang.ArrayIndexOutOfBoundsException: length=39; index=-3
                  at android.text.DynamicLayout.getBlockIndex(DynamicLayout.java:648)
                  at android.widget.Editor.drawHardwareAccelerated(Editor.java:1703)
                  at android.widget.Editor.onDraw(Editor.java:1672)
                  at android.widget.TextView.onDraw(TextView.java:6914)
                  at android.view.View.draw(View.java:19200)
            */
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
            /*
				java.lang.SecurityException: No access to content://com.sec.android.semclipboardprovider/images: neither user 1010197 nor current process has android.permission.INTERACT_ACROSS_USERS_FULL or android.permission.INTERACT_ACROSS_USERS
				  at android.os.Parcel.createException(Parcel.java:2088)
				  at android.os.Parcel.readException(Parcel.java:2056)
				  at android.os.Parcel.readException(Parcel.java:2004)
				  at android.sec.clipboard.IClipboardService$Stub$Proxy.getClipData(IClipboardService.java:951)
				  at com.samsung.android.content.clipboard.SemClipboardManager.getLatestClip(SemClipboardManager.java:612)
				  at android.widget.EditText.updateClipboardFilter(EditText.java:316)
				  at android.widget.EditText.dispatchWindowFocusChanged(EditText.java:297)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewRootImpl.handleWindowFocusChanged(ViewRootImpl.java:3458)
				  at android.view.ViewRootImpl.access$1300(ViewRootImpl.java:205)
				  at android.view.ViewRootImpl$ViewRootHandler.handleMessage(ViewRootImpl.java:5361)
				  at android.os.Handler.dispatchMessage(Handler.java:107)
             */
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
    public boolean performLongClick() {
        try {
            return super.performLongClick();
        } catch (Throwable ex) {
            /*
            java.lang.IllegalStateException: Drag shadow dimensions must be positive
                    at android.view.View.startDragAndDrop(View.java:27316)
                    at android.widget.Editor.startDragAndDrop(Editor.java:1340)
                    at android.widget.Editor.performLongClick(Editor.java:1374)
                    at android.widget.TextView.performLongClick(TextView.java:13544)
                    at android.view.View.performLongClick(View.java:7928)
                    at android.view.View$CheckForLongPress.run(View.java:29321)
             */
            Log.w(ex);
            return false;
        }
    }
}
