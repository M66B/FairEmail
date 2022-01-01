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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.text.SpannableStringBuilder;

public class SpannableStringBuilderEx extends SpannableStringBuilder {
    public SpannableStringBuilderEx() {
        super();
    }

    public SpannableStringBuilderEx(CharSequence text) {
        super(text);
    }

    public SpannableStringBuilderEx(CharSequence text, int start, int end) {
        super(text, start, end);
    }

    @Override
    public void setSpan(Object what, int start, int end, int flags) {
        try {
            super.setSpan(what, start, end, flags);
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.IndexOutOfBoundsException: setSpan (-1 ... -1) starts before 0
                        at android.text.SpannableStringInternal.checkRange(SpannableStringInternal.java:497)
                        at android.text.SpannableStringInternal.setSpan(SpannableStringInternal.java:197)
                        at android.text.SpannableStringInternal.setSpan(SpannableStringInternal.java:184)
                        at android.text.SpannableString.setSpan(SpannableString.java:60)
                        at android.text.Selection.setSelection(Selection.java:96)
                        at android.text.Selection.setSelection(Selection.java:78)
                        at android.widget.Editor$SelectionStartHandleView.updateSelection(Editor.java:7649)
                        at android.widget.Editor$HandleView.positionAtCursorOffset(Editor.java:6886)
                        at android.widget.Editor$HandleView.updatePosition(Editor.java:6920)
                        at android.widget.Editor$PositionListener.onPreDraw(Editor.java:3660)
                        at android.view.ViewTreeObserver.dispatchOnPreDraw(ViewTreeObserver.java:1093)
                        at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:3194)
                        at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:2046)
                        at android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:8349)
                        at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1058)
                        at android.view.Choreographer.doCallbacks(Choreographer.java:880)
                        at android.view.Choreographer.doFrame(Choreographer.java:813)
                        at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:1043)
                        at android.os.Handler.handleCallback(Handler.java:938)
                        at android.os.Handler.dispatchMessage(Handler.java:99)
                        at android.os.Looper.loop(Looper.java:236)
                        at android.app.ActivityThread.main(ActivityThread.java:7861)
             */
        }
    }
}
