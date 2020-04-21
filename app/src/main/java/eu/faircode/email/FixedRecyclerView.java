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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.FastScrollerEx;
import androidx.recyclerview.widget.RecyclerView;

public class FixedRecyclerView extends RecyclerView {
    public FixedRecyclerView(@NonNull Context context) {
        super(context);
        initFastScrollerEx(context, null, R.attr.recyclerViewStyle);
    }

    public FixedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initFastScrollerEx(context, attrs, R.attr.recyclerViewStyle);
    }

    public FixedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initFastScrollerEx(context, attrs, defStyle);
    }

    private void initFastScrollerEx(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerView,
                defStyleAttr, 0);
        StateListDrawable verticalThumbDrawable = (StateListDrawable) a
                .getDrawable(R.styleable.RecyclerView_fastScrollVerticalThumbDrawable);
        Drawable verticalTrackDrawable = a
                .getDrawable(R.styleable.RecyclerView_fastScrollVerticalTrackDrawable);
        StateListDrawable horizontalThumbDrawable = (StateListDrawable) a
                .getDrawable(R.styleable.RecyclerView_fastScrollHorizontalThumbDrawable);
        Drawable horizontalTrackDrawable = a
                .getDrawable(R.styleable.RecyclerView_fastScrollHorizontalTrackDrawable);
        Resources resources = getContext().getResources();
        new FastScrollerEx(this, verticalThumbDrawable, verticalTrackDrawable,
                horizontalThumbDrawable, horizontalTrackDrawable,
                resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness),
                resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range),
                resources.getDimensionPixelOffset(R.dimen.fastscroll_margin));
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        try {
            return super.onTouchEvent(e);
        } catch (IllegalArgumentException ex) {
            /*
                java.lang.IllegalArgumentException: Position cannot be NO_POSITION.
                java.lang.IllegalArgumentException: Position cannot be NO_POSITION.
                at androidx.core.util.Preconditions.checkArgument(SourceFile:52)
                at androidx.recyclerview.selection.Range.extendRange(SourceFile:83)
                at androidx.recyclerview.selection.DefaultSelectionTracker.extendRange(SourceFile:299)
                at androidx.recyclerview.selection.DefaultSelectionTracker.extendProvisionalRange(SourceFile:282)
                at androidx.recyclerview.selection.GestureSelectionHelper.extendSelection(SourceFile:215)
                at androidx.recyclerview.selection.GestureSelectionHelper.handleMoveEvent(SourceFile:192)
                at androidx.recyclerview.selection.GestureSelectionHelper.handleTouch(SourceFile:145)
                at androidx.recyclerview.selection.GestureSelectionHelper.onTouchEvent(SourceFile:111)
                at androidx.recyclerview.selection.TouchEventRouter.onTouchEvent(SourceFile:103)
                at androidx.recyclerview.widget.RecyclerView.dispatchOnItemTouch(SourceFile:2947)
                at androidx.recyclerview.widget.RecyclerView.onTouchEvent(SourceFile:3090)
             */
            Log.w(ex);
            return false;
        } catch (NullPointerException ex) {
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'int android.view.View.getTop()' on a null object reference
                java.lang.NullPointerException: Attempt to invoke virtual method 'int android.view.View.getTop()' on a null object reference
                at androidx.recyclerview.selection.GestureSelectionHelper$RecyclerViewDelegate.getLastGlidedItemPosition(SourceFile:287)
                at androidx.recyclerview.selection.GestureSelectionHelper.handleMoveEvent(SourceFile:202)
                at androidx.recyclerview.selection.GestureSelectionHelper.handleTouch(SourceFile:151)
                at androidx.recyclerview.selection.GestureSelectionHelper.onInterceptTouchEvent(SourceFile:118)
                at androidx.recyclerview.selection.TouchEventRouter.onInterceptTouchEvent(SourceFile:91)
                at androidx.recyclerview.widget.RecyclerView.dispatchOnItemTouch(SourceFile:2962)
                at androidx.recyclerview.widget.RecyclerView.onTouchEvent(SourceFile:3090)
             */
            Log.w(ex);
            return false;
        } catch (IllegalStateException ex) {
            // Range start point not set
            Log.w(ex);
            return false;
        } catch (IndexOutOfBoundsException ex) {
            /*
                java.lang.ArrayIndexOutOfBoundsException: length=5; index=7
                        at java.util.Arrays$ArrayList.get(Arrays.java:3766)
                        at androidx.recyclerview.selection.ToolHandlerRegistry.get(SourceFile:69)
                        at androidx.recyclerview.selection.EventRouter.onInterceptTouchEvent(SourceFile:57)
                        at androidx.recyclerview.widget.RecyclerView.findInterceptingOnItemTouchListener(SourceFile:3151)
                        at androidx.recyclerview.widget.RecyclerView.dispatchToOnItemTouchListeners(SourceFile:3122)
                        at androidx.recyclerview.widget.RecyclerView.onTouchEvent(SourceFile:3283)
             */
            Log.w(ex);
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        try {
            return super.onInterceptTouchEvent(e);
        } catch (Throwable ex) {
            // IllegalStateException: Range start point not set
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'int android.view.View.getTop()' on a null object reference
                        at androidx.recyclerview.selection.GestureSelectionHelper$RecyclerViewDelegate.getLastGlidedItemPosition(SourceFile:276)
                        at androidx.recyclerview.selection.GestureSelectionHelper.handleMoveEvent(SourceFile:191)
                        at androidx.recyclerview.selection.GestureSelectionHelper.onTouchEvent(SourceFile:141)
                        at androidx.recyclerview.selection.GestureSelectionHelper.onInterceptTouchEvent(SourceFile:108)
                        at androidx.recyclerview.selection.EventRouter.onInterceptTouchEvent(SourceFile:57)
                        at androidx.recyclerview.widget.RecyclerView.findInterceptingOnItemTouchListener(SourceFile:3153)
                        at androidx.recyclerview.widget.RecyclerView.onInterceptTouchEvent(SourceFile:3172)
                        at eu.faircode.email.FixedRecyclerView.onInterceptTouchEvent(SourceFile:128)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2609)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3060)
             */
            Log.w(ex);
            return false;
        }
    }
}
