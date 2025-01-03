package eu.faircode.email;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

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

public class FixedScrollView extends ScrollView {
    public FixedScrollView(Context context) {
        super(context);
    }

    public FixedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FixedScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        try {
            super.onLayout(changed, l, t, r, b);
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.IndexOutOfBoundsException: Index: 1, Size: 1
                    at java.util.ArrayList.get(ArrayList.java:437)
                    at android.widget.ArrayAdapter.getItem(ArrayAdapter.java:394)
                    at android.widget.ArrayAdapter.createViewFromResource(ArrayAdapter.java:450)
                    at android.widget.ArrayAdapter.getView(ArrayAdapter.java:416)
                    at android.widget.Spinner.makeView(Spinner.java:724)
                    at android.widget.Spinner.layout(Spinner.java:672)
                    at android.widget.Spinner.onLayout(Spinner.java:634)
                    at android.view.View.layout(View.java:23347)
                    at android.view.ViewGroup.layout(ViewGroup.java:6563)
                    at androidx.constraintlayout.widget.ConstraintLayout.onLayout(SourceFile:12)
                    at android.view.View.layout(View.java:23347)
                    at android.view.ViewGroup.layout(ViewGroup.java:6563)
                    at android.widget.FrameLayout.layoutChildren(FrameLayout.java:332)
                    at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
                    at android.widget.ScrollView.onLayout(ScrollView.java:1701)
                    at android.view.View.layout(View.java:23347)
                    at android.view.ViewGroup.layout(ViewGroup.java:6563)
                    at android.widget.FrameLayout.layoutChildren(FrameLayout.java:332)
                    at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
                    at android.view.View.layout(View.java:23347)
                    at android.view.ViewGroup.layout(ViewGroup.java:6563)
                    at androidx.drawerlayout.widget.DrawerLayout.onLayout(SourceFile:10)
                    at android.view.View.layout(View.java:23347)
                    at android.view.ViewGroup.layout(ViewGroup.java:6563)
                    at android.widget.FrameLayout.layoutChildren(FrameLayout.java:332)
                    at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
                    at android.view.View.layout(View.java:23347)
                    at android.view.ViewGroup.layout(ViewGroup.java:6563)
                    at androidx.appcompat.widget.ActionBarOverlayLayout.onLayout(SourceFile:11)
                    at android.view.View.layout(View.java:23347)
                    at android.view.ViewGroup.layout(ViewGroup.java:6563)
                    at android.widget.FrameLayout.layoutChildren(FrameLayout.java:332)
                    at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
                    at android.view.View.layout(View.java:23347)
                    at android.view.ViewGroup.layout(ViewGroup.java:6563)
                    at android.widget.LinearLayout.setChildFrame(LinearLayout.java:1841)
                    at android.widget.LinearLayout.layoutVertical(LinearLayout.java:1673)
                    at android.widget.LinearLayout.onLayout(LinearLayout.java:1582)
                    at android.view.View.layout(View.java:23347)
                    at android.view.ViewGroup.layout(ViewGroup.java:6563)
                    at android.widget.FrameLayout.layoutChildren(FrameLayout.java:332)
                    at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
                    at com.android.internal.policy.DecorView.onLayout(DecorView.java:889)
                    at android.view.View.layout(View.java:23347)
                    at android.view.ViewGroup.layout(ViewGroup.java:6563)
                    at android.view.ViewRootImpl.performLayout(ViewRootImpl.java:4029)
                    at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:3434)
                    at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:2382)
                    at android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:9088)
                    at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1521)
                    at android.view.Choreographer.doCallbacks(Choreographer.java:1319)
                    at android.view.Choreographer.doFrame(Choreographer.java:1020)
                    at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:1506)
                    at android.os.Handler.handleCallback(Handler.java:938)
                    at android.os.Handler.dispatchMessage(Handler.java:99)
                    at android.os.Looper.loop(Looper.java:262)
                    at android.app.ActivityThread.main(ActivityThread.java:8304)
             */
        }
    }

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
