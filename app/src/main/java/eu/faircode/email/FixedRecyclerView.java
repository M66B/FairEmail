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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class FixedRecyclerView extends RecyclerView {
    public FixedRecyclerView(@NonNull Context context) {
        super(context);
    }

    public FixedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
            Log.i(ex);
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
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        try {
            return super.onInterceptTouchEvent(e);
        } catch (IllegalStateException ex) {
            // Range start point not set
            Log.w(ex);
            return false;
        }
    }
}
