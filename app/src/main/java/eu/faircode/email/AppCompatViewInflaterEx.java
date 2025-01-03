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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatViewInflater;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

public class AppCompatViewInflaterEx extends AppCompatViewInflater {
    // <item name="viewInflaterClass">eu.faircode.email.AppCompatViewInflaterEx</item>
    @Nullable
    @Override
    protected View createView(Context context, String name, AttributeSet attrs) {
        /*
        java.lang.IndexOutOfBoundsException: 2, 0
            at android.text.PackedIntVector.getValue(PackedIntVector.java:75)
            at android.text.DynamicLayout.getLineStart(DynamicLayout.java:1028)
            at android.text.Layout.getLineEnd(Layout.java:1676)
            at android.text.Layout.getOffsetForHorizontal(Layout.java:1545)
            at android.text.Layout.getOffsetForHorizontal(Layout.java:1530)
            at android.widget.TextView.getOffsetAtCoordinate(TextView.java:13114)
            at android.widget.Editor$HandleView.getOffsetAtCoordinate(Editor.java:4966)
            at android.widget.Editor$InsertionHandleView.updatePosition(Editor.java:5757)
            at android.widget.Editor$HandleView.onTouchEvent(Editor.java:5423)
            at android.widget.Editor$InsertionHandleView.onTouchEvent(Editor.java:5586)
            at android.view.View.dispatchTouchEvent(View.java:14599)
            at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3120)
            at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2801)
            at android.widget.PopupWindow$PopupDecorView.dispatchTouchEvent(PopupWindow.java:2553)
            at android.view.View.dispatchPointerEvent(View.java:14858)
            at android.view.ViewRootImpl$ViewPostImeInputStage.processPointerEvent(ViewRootImpl.java:6446)
            at android.view.ViewRootImpl$ViewPostImeInputStage.onProcess(ViewRootImpl.java:6247)
            at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:5725)
            at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:5782)
            at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:5748)
            at android.view.ViewRootImpl$AsyncInputStage.forward(ViewRootImpl.java:5913)
            at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:5756)
            at android.view.ViewRootImpl$AsyncInputStage.apply(ViewRootImpl.java:5970)
            at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:5729)
            at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:5782)
            at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:5748)
            at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:5756)
            at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:5729)
            at android.view.ViewRootImpl.deliverInputEvent(ViewRootImpl.java:8696)
            at android.view.ViewRootImpl.doProcessInputEvents(ViewRootImpl.java:8647)
            at android.view.ViewRootImpl.enqueueInputEvent(ViewRootImpl.java:8616)
            at android.view.ViewRootImpl$WindowInputEventReceiver.onInputEvent(ViewRootImpl.java:8819)
            at android.view.InputEventReceiver.dispatchInputEvent(InputEventReceiver.java:259)
            at android.view.InputEventReceiver.nativeConsumeBatchedInputEvents(Native Method)
            at android.view.InputEventReceiver.consumeBatchedInputEvents(InputEventReceiver.java:239)
            at android.view.ViewRootImpl.doConsumeBatchedInput(ViewRootImpl.java:8776)
            at android.view.ViewRootImpl$ConsumeBatchedInputRunnable.run(ViewRootImpl.java:8905)
            at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1037)
            at android.view.Choreographer.doCallbacks(Choreographer.java:845)
            at android.view.Choreographer.doFrame(Choreographer.java:772)
            at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:1022)
            at android.os.Handler.handleCallback(Handler.java:938)
         */

        if ("LinearLayout".equals(name))
            return new FixedLinearLayout(context, attrs);

        if ("RelativeLayout".equals(name))
            return new FixedRelativeLayout(context, attrs);

        if ("FrameLayout".equals(name))
            return new FixedFrameLayout(context, attrs);

        if ("ScrollView".equals(name))
            return new FixedScrollView(context, attrs);

        if ("View".equals(name))
            return new FixedView(context, attrs);

        if ("androidx.coordinatorlayout.widget.CoordinatorLayout".equals(name))
            return new FixedCoordinatorLayout(context, attrs);

        if ("androidx.constraintlayout.widget.ConstraintLayout".equals(name))
            return new FixedConstraintLayout(context, attrs);

        if ("androidx.core.widget.NestedScrollView".equals(name))
            return new FixedNestedScrollView(context, attrs);

        return super.createView(context, name, attrs);
    }

    @NonNull
    @Override
    protected AppCompatTextView createTextView(Context context, AttributeSet attrs) {
        return new FixedTextView(context, attrs);
    }

    @NonNull
    @Override
    protected AppCompatImageView createImageView(Context context, AttributeSet attrs) {
        return new FixedImageView(context, attrs);
    }

    @NonNull
    @Override
    protected AppCompatImageButton createImageButton(Context context, AttributeSet attrs) {
        return new FixedImageButton(context, attrs);
    }
}