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
        initFastScrollerEx(context, null, androidx.recyclerview.R.attr.recyclerViewStyle);
    }

    public FixedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initFastScrollerEx(context, attrs, androidx.recyclerview.R.attr.recyclerViewStyle);
    }

    public FixedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initFastScrollerEx(context, attrs, defStyle);
    }

    private void initFastScrollerEx(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, androidx.recyclerview.R.styleable.RecyclerView,
                defStyleAttr, 0);
        StateListDrawable verticalThumbDrawable = (StateListDrawable) a
                .getDrawable(androidx.recyclerview.R.styleable.RecyclerView_fastScrollVerticalThumbDrawable);
        Drawable verticalTrackDrawable = a
                .getDrawable(androidx.recyclerview.R.styleable.RecyclerView_fastScrollVerticalTrackDrawable);
        StateListDrawable horizontalThumbDrawable = (StateListDrawable) a
                .getDrawable(androidx.recyclerview.R.styleable.RecyclerView_fastScrollHorizontalThumbDrawable);
        Drawable horizontalTrackDrawable = a
                .getDrawable(androidx.recyclerview.R.styleable.RecyclerView_fastScrollHorizontalTrackDrawable);

        if (verticalThumbDrawable == null)
            verticalThumbDrawable = (StateListDrawable) context.getDrawable(R.drawable.scroll_thumb);
        if (verticalTrackDrawable == null)
            verticalTrackDrawable = context.getDrawable(R.drawable.scroll_track);

        if (horizontalThumbDrawable == null)
            horizontalThumbDrawable = (StateListDrawable) context.getDrawable(R.drawable.scroll_thumb);
        if (horizontalTrackDrawable == null)
            horizontalTrackDrawable = context.getDrawable(R.drawable.scroll_track);

        Resources resources = getContext().getResources();
        new FastScrollerEx(this, verticalThumbDrawable, verticalTrackDrawable,
                horizontalThumbDrawable, horizontalTrackDrawable,
                resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness),
                resources.getDimensionPixelSize(androidx.recyclerview.R.dimen.fastscroll_minimum_range),
                resources.getDimensionPixelOffset(androidx.recyclerview.R.dimen.fastscroll_margin));
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        try {
            super.onLayout(changed, l, t, r, b);
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.IllegalArgumentException: Comparison method violates its general contract!
                        at java.util.TimSort.mergeHi(TimSort.java:864)
                        at java.util.TimSort.mergeAt(TimSort.java:481)
                        at java.util.TimSort.mergeForceCollapse(TimSort.java:422)
                        at java.util.TimSort.sort(TimSort.java:219)
                        at java.util.TimSort.sort(TimSort.java:169)
                        at java.util.Arrays.sort(Arrays.java:2010)
                        at java.util.Collections.sort(Collections.java:1883)
                        at android.view.ViewGroup$ChildListForAccessibility.init(ViewGroup.java:7181)
                        at android.view.ViewGroup$ChildListForAccessibility.obtain(ViewGroup.java:7138)
                        at android.view.ViewGroup.dispatchPopulateAccessibilityEventInternal(ViewGroup.java:2734)
                        at android.view.View.dispatchPopulateAccessibilityEvent(View.java:5617)
                        at android.view.ViewGroup.dispatchPopulateAccessibilityEventInternal(ViewGroup.java:2740)
                        at android.view.View.dispatchPopulateAccessibilityEvent(View.java:5617)
                        at android.view.ViewGroup.dispatchPopulateAccessibilityEventInternal(ViewGroup.java:2740)
                        at android.view.View$AccessibilityDelegate.dispatchPopulateAccessibilityEvent(View.java:21273)
                        at android.view.View.dispatchPopulateAccessibilityEvent(View.java:5615)
                        at android.view.View.sendAccessibilityEventUncheckedInternal(View.java:5582)
                        at android.view.View$AccessibilityDelegate.sendAccessibilityEventUnchecked(View.java:21252)
                        at android.view.View.sendAccessibilityEventUnchecked(View.java:5564)
                        at android.view.View.sendAccessibilityEventInternal(View.java:5543)
                        at android.view.View$AccessibilityDelegate.sendAccessibilityEvent(View.java:21210)
                        at android.view.View.sendAccessibilityEvent(View.java:5510)
                        at android.view.View.onFocusChanged(View.java:5449)
                        at android.view.View.handleFocusGainInternal(View.java:5229)
                        at android.view.ViewGroup.handleFocusGainInternal(ViewGroup.java:651)
                        at android.view.View.requestFocusNoSearch(View.java:7950)
                        at android.view.View.requestFocus(View.java:7929)
                        at android.view.ViewGroup.requestFocus(ViewGroup.java:2612)
                        at android.view.ViewGroup.onRequestFocusInDescendants(ViewGroup.java:2657)
                        at android.view.ViewGroup.requestFocus(ViewGroup.java:2613)
                        at android.view.ViewGroup.onRequestFocusInDescendants(ViewGroup.java:2657)
                        at android.view.ViewGroup.requestFocus(ViewGroup.java:2613)
                        at android.view.View.requestFocus(View.java:7896)
                        at android.view.View.requestFocus(View.java:7875)
                        at androidx.recyclerview.widget.RecyclerView.recoverFocusFromState(SourceFile:4074)
                        at androidx.recyclerview.widget.RecyclerView.dispatchLayoutStep3(SourceFile:4313)
                        at androidx.recyclerview.widget.RecyclerView.dispatchLayout(SourceFile:3937)
                        at androidx.recyclerview.widget.RecyclerView.onLayout(SourceFile:4484)
                        at android.view.View.layout(View.java:16076)
             */
        }
    }
}
