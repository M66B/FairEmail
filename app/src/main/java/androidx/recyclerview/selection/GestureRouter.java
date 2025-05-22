/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.recyclerview.selection;

import static androidx.core.util.Preconditions.checkArgument;

import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * GestureRouter is responsible for routing gestures detected by a GestureDetector
 * to registered handlers. The primary function is to divide events by tool-type
 * allowing handlers to cleanly implement tool-type specific policies.
 *
 * @param <T> listener type. Must extend OnGestureListener & OnDoubleTapListener.
 */
final class GestureRouter<T extends OnGestureListener & OnDoubleTapListener>
        implements OnGestureListener, OnDoubleTapListener {

    private final ToolSourceHandlerRegistry<T> mDelegates;

    GestureRouter(@NonNull T defaultDelegate) {
        checkArgument(defaultDelegate != null);
        mDelegates = new ToolSourceHandlerRegistry<>(defaultDelegate);
    }

    @SuppressWarnings("unchecked")
    GestureRouter() {
        this((T) new SimpleOnGestureListener());
    }

    /**
     * @param key
     * @param delegate the delegate, or null to unregister.
     */
    public void register(@NonNull ToolSourceKey key, @Nullable T delegate) {
        mDelegates.set(key, delegate);
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        return mDelegates.get(e).onSingleTapConfirmed(e);
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent e) {
        return mDelegates.get(e).onDoubleTap(e);
    }

    @Override
    public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
        return mDelegates.get(e).onDoubleTapEvent(e);
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return mDelegates.get(e).onDown(e);
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {
        mDelegates.get(e).onShowPress(e);
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return mDelegates.get(e).onSingleTapUp(e);
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
            float distanceX, float distanceY) {
        return mDelegates.get(e2).onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        try {
            mDelegates.get(e).onLongPress(e);
        } catch (Throwable ex) {
            eu.faircode.email.Log.w(ex);
            /*
                java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling eu.faircode.email.FixedRecyclerView{239c688b VFED.... ........ 0,0-800,1162 #7f0a04da app:id/rvMessage}, adapter:eu.faircode.email.AdapterMessage@209415c5, layout:eu.faircode.email.FragmentMessages$7@190d7b1a, context:eu.faircode.email.ActivityView@3e8522fb
                        at androidx.recyclerview.widget.RecyclerView.assertNotInLayoutOrScroll(SourceFile:3)
                        at androidx.recyclerview.widget.RecyclerView$RecyclerViewDataObserver.onItemRangeChanged(SourceFile:1)
                        at androidx.recyclerview.widget.RecyclerView$AdapterDataObservable.notifyItemRangeChanged(SourceFile:3)
                        at androidx.recyclerview.widget.RecyclerView$Adapter.notifyItemChanged(SourceFile:2)
                        at androidx.recyclerview.selection.EventBridge$TrackerToAdapterBridge.onItemStateChanged(SourceFile:3)
                        at androidx.recyclerview.selection.DefaultSelectionTracker.notifyItemStateChanged(SourceFile:3)
                        at androidx.recyclerview.selection.DefaultSelectionTracker.select(SourceFile:8)
                        at androidx.recyclerview.selection.MotionInputHandler.selectItem(SourceFile:4)
                        at androidx.recyclerview.selection.TouchInputHandler.onLongPress(SourceFile:10)
                        at androidx.recyclerview.selection.GestureRouter.onLongPress(SourceFile:1)
                        at android.view.GestureDetector.dispatchLongPress(GestureDetector.java:700)
                        at android.view.GestureDetector.access$200(GestureDetector.java:40)
                        at android.view.GestureDetector$GestureHandler.handleMessage(GestureDetector.java:273)
                        at android.os.Handler.dispatchMessage(Handler.java:102)
             */
        }
    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
            float velocityX, float velocityY) {
        return mDelegates.get(e2).onFling(e1, e2, velocityX, velocityY);
    }
}
