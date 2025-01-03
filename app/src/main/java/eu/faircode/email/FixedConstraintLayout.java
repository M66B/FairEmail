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
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class FixedConstraintLayout extends ConstraintLayout {
    public FixedConstraintLayout(@NonNull Context context) {
        super(context);
    }

    public FixedConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FixedConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            super.onLayout(changed, left, top, right, bottom);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    /*
        java.lang.IndexOutOfBoundsException: 2, 0
            at android.text.PackedIntVector.getValue(PackedIntVector.java:75)
            at android.text.DynamicLayout.getLineStart(DynamicLayout.java:1028)
            at android.text.Layout.getLineEnd(Layout.java:1675)
            at android.text.Layout.getOffsetForHorizontal(Layout.java:1544)
            at android.text.Layout.getOffsetForHorizontal(Layout.java:1529)
            at android.widget.TextView.getOffsetAtCoordinate(TextView.java:12988)
            at android.widget.Editor$HandleView.getOffsetAtCoordinate(Editor.java:4875)
            at android.widget.Editor$InsertionHandleView.updatePosition(Editor.java:5635)
            at android.widget.Editor$HandleView.onTouchEvent(Editor.java:5301)
            at android.widget.Editor$InsertionHandleView.onTouchEvent(Editor.java:5464)
            at android.view.View.dispatchTouchEvent(View.java:14309)
            at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3118)
            at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2799)
            at android.widget.PopupWindow$PopupDecorView.dispatchTouchEvent(PopupWindow.java:2553)
            at android.view.View.dispatchPointerEvent(View.java:14568)
            at android.view.ViewRootImpl$ViewPostImeInputStage.processPointerEvent(ViewRootImpl.java:6022)
            at android.view.ViewRootImpl$ViewPostImeInputStage.onProcess(ViewRootImpl.java:5825)
            at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:5316)
            at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:5373)
            at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:5339)
            at android.view.ViewRootImpl$AsyncInputStage.forward(ViewRootImpl.java:5491)
            at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:5347)
            at android.view.ViewRootImpl$AsyncInputStage.apply(ViewRootImpl.java:5548)
            at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:5320)
            at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:5373)
            at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:5339)
            at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:5347)
            at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:5320)
            at android.view.ViewRootImpl.deliverInputEvent(ViewRootImpl.java:8086)
            at android.view.ViewRootImpl.doProcessInputEvents(ViewRootImpl.java:8037)
            at android.view.ViewRootImpl.enqueueInputEvent(ViewRootImpl.java:7998)
            at android.view.ViewRootImpl$WindowInputEventReceiver.onInputEvent(ViewRootImpl.java:8209)
            at android.view.InputEventReceiver.dispatchInputEvent(InputEventReceiver.java:220)
            at android.view.InputEventReceiver.nativeConsumeBatchedInputEvents(Native Method)
            at android.view.InputEventReceiver.consumeBatchedInputEvents(InputEventReceiver.java:200)
            at android.view.ViewRootImpl.doConsumeBatchedInput(ViewRootImpl.java:8166)
            at android.view.ViewRootImpl$ConsumeBatchedInputRunnable.run(ViewRootImpl.java:8248)
            at android.view.Choreographer$CallbackRecord.run(Choreographer.java:972)
            at android.view.Choreographer.doCallbacks(Choreographer.java:796)
            at android.view.Choreographer.doFrame(Choreographer.java:724)
            at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:957)
            at android.os.Handler.handleCallback(Handler.java:938)
            at android.os.Handler.dispatchMessage(Handler.java:99)
     */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        try {
            return super.dispatchGenericMotionEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }
}
