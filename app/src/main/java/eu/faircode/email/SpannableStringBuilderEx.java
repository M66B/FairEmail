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

import android.text.SpannableStringBuilder;

import androidx.annotation.Nullable;

import java.lang.reflect.Array;

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
            /*
                java.lang.IndexOutOfBoundsException: setSpan (196 ... 203) ends beyond length 202
                        at android.text.SpannableStringBuilder.checkRange(SpannableStringBuilder.java:1326)
                        at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:685)
                        at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:677)
                        at androidx.emoji2.text.SpannableBuilder.setSpan(SourceFile:18)
                        at android.widget.Editor$SuggestionsPopupWindow.updateSuggestions(Editor.java:4123)
             */
        }
    }

    @Override
    public <T> T[] getSpans(int queryStart, int queryEnd, @Nullable Class<T> kind) {
        try {
            return super.getSpans(queryStart, queryEnd, kind);
        } catch (Throwable ex) {
            /*
                java.lang.ArrayIndexOutOfBoundsException: length=...; index=...
                        at android.text.SpannableStringBuilder.getSpansRec(SpannableStringBuilder.java:984)
                        at android.text.SpannableStringBuilder.getSpansRec(SpannableStringBuilder.java:988)
                        at android.text.SpannableStringBuilder.getSpans(SpannableStringBuilder.java:877)
                        at android.text.SpannableStringBuilder.getSpans(SpannableStringBuilder.java:847)
                        at androidx.emoji2.text.SpannableBuilder.getSpans(SpannableBuilder:160)
             */
            /*
                java.lang.ArrayStoreException: android.text.Selection$START cannot be stored in an array of type android.text.style.CharacterStyle[]
                        at android.text.SpannableStringBuilder.getSpansRec(SpannableStringBuilder.java:987)
                        at android.text.SpannableStringBuilder.getSpansRec(SpannableStringBuilder.java:991)
                        at android.text.SpannableStringBuilder.getSpansRec(SpannableStringBuilder.java:991)
                        at android.text.SpannableStringBuilder.getSpansRec(SpannableStringBuilder.java:954)
                        at android.text.SpannableStringBuilder.getSpans(SpannableStringBuilder.java:880)
                        at android.text.SpannableStringBuilder.getSpans(SpannableStringBuilder.java:849)
                        at androidx.emoji2.text.SpannableBuilder.getSpans(SpannableBuilder:160)
             */
            Log.e(ex);
            return (T[]) Array.newInstance(kind, 0);
        }
    }

    @Override
    public SpannableStringBuilder replace(int start, int end, CharSequence tb, int tbstart, int tbend) {
        try {
            // https://issuetracker.google.com/issues/233525229
            return super.replace(start, end, tb, tbstart, tbend);
        } catch (Throwable ex) {
            /*
                java.lang.IllegalArgumentException: Invalid offset: 95. Valid range is [0, 53]
                        at android.text.method.WordIterator.checkOffsetIsValid(WordIterator.java:401)
                        at android.text.method.WordIterator.isBoundary(WordIterator.java:101)
                        at android.widget.Editor$SelectionHandleView.positionAtCursorOffset(Editor.java:6044)
                        at android.widget.Editor$HandleView.invalidate(Editor.java:4881)
                        at android.widget.Editor$SelectionModifierCursorController.invalidateHandles(Editor.java:6906)
                        at android.widget.Editor.invalidateHandlesAndActionMode(Editor.java:2241)
                        at android.widget.TextView.spanChange(TextView.java:10979)
                        at android.widget.TextView$ChangeWatcher.onSpanRemoved(TextView.java:13846)
                        at android.text.SpannableStringBuilder.sendSpanRemoved(SpannableStringBuilder.java:1297)
                        at android.text.SpannableStringBuilder.removeSpan(SpannableStringBuilder.java:502)
                        at android.text.SpannableStringBuilder.removeSpan(SpannableStringBuilder.java:802)
                        at android.text.SpannableStringBuilder.removeSpan(SpannableStringBuilder.java:790)
                        at androidx.emoji2.text.SpannableBuilder.removeSpan(SpannableBuilder:179)
                        at android.widget.TextView.removeAdjacentSuggestionSpans(TextView.java:10774)
                        at android.widget.Editor.updateSpellCheckSpans(Editor.java:1000)
                        at android.widget.Editor.sendOnTextChanged(Editor.java:1610)
                        at android.widget.TextView.sendOnTextChanged(TextView.java:10793)
                        at android.widget.TextView.handleTextChanged(TextView.java:10904)
                        at android.widget.TextView$ChangeWatcher.onTextChanged(TextView.java:13807)
                        at android.text.SpannableStringBuilder.sendTextChanged(SpannableStringBuilder.java:1268)
                        at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:577)
                        at androidx.emoji2.text.SpannableBuilder.replace(SpannableBuilder:315)
                        at android.text.SpannableStringBuilder.insert(SpannableStringBuilder.java:226)
                        at androidx.emoji2.text.SpannableBuilder.insert(SpannableBuilder:323)
                        at androidx.emoji2.text.SpannableBuilder.insert(SpannableBuilder:49)
             */
            Log.e(ex);
            return this;
        }
    }
}
