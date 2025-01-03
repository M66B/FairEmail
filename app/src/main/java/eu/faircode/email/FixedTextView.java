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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class FixedTextView extends AppCompatTextView {
    public FixedTextView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FixedTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FixedTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setCustomSelectionActionModeCallback(Helper.getActionModeWrapper(this));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (Throwable ex) {
            Log.w(ex);
/*
        java.lang.ArrayIndexOutOfBoundsException: length=...; index=...
                at android.text.TextLine.measure(TextLine.java:316)
                at android.text.TextLine.metrics(TextLine.java:271)
                at android.text.Layout.measurePara(Layout.java:2056)
                at android.text.Layout.getDesiredWidth(Layout.java:164)
                at android.widget.TextView.onMeasure(TextView.java:8291)
                at androidx.appcompat.widget.AppCompatTextView.onMeasure(SourceFile:554)
                at android.view.View.measure(View.java:22360)
*/
            setMeasuredDimension(0, 0);
        }
    }

    @Override
    public boolean onPreDraw() {
        try {
            return super.onPreDraw();
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.ArrayIndexOutOfBoundsException: length=54; index=54
                at android.text.TextLine.measure(TextLine.java:316)
                at android.text.TextLine.metrics(TextLine.java:271)
                at android.text.Layout.getLineExtent(Layout.java:1374)
                at android.text.Layout.getLineStartPos(Layout.java:700)
                at android.text.Layout.getHorizontal(Layout.java:1175)
                at android.text.Layout.getHorizontal(Layout.java:1144)
                at android.text.Layout.getPrimaryHorizontal(Layout.java:1115)
                at android.widget.TextView.bringPointIntoView(TextView.java:8944)
                at android.widget.TextView.onPreDraw(TextView.java:6475)
            */
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
        java.lang.ArrayIndexOutOfBoundsException: length=74; index=74
                at android.text.TextLine.draw(TextLine.java:241)
                at android.text.Layout.drawText(Layout.java:545)
                at android.text.Layout.draw(Layout.java:289)
                at android.widget.TextView.onDraw(TextView.java:6972)
                at android.view.View.draw(View.java:19380)
*/
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // https://issuetracker.google.com/issues/37068143
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN &&
                Build.VERSION.RELEASE.equals("6.0") && hasSelection()) {
            // Remove selection
            CharSequence text = getText();
            setText(null);
            setText(text);
        }

        try {
            return super.dispatchTouchEvent(event);
        } catch (Throwable ex) {
            /*
                Attempt to fix
                java.lang.IndexOutOfBoundsException: setSpan (-1 ... -1) starts before 0
                  at android.text.SpannableStringInternal.checkRange(SpannableStringInternal.java:434)
                  at android.text.SpannableStringInternal.setSpan(SpannableStringInternal.java:155)
                  at android.text.SpannableString.setSpan(SpannableString.java:46)
                  at android.text.Selection.setSelection(Selection.java:76)
                  at android.widget.Editor$SelectionHandleView.updateSelection(Editor.java:4687)
                  at android.widget.Editor$HandleView.positionAtCursorOffset(Editor.java:4262)
                  at android.widget.Editor$SelectionHandleView.positionAtCursorOffset(Editor.java:4870)
                  at android.widget.Editor$SelectionHandleView.positionAndAdjustForCrossingHandles(Editor.java:4918)
                  at android.widget.Editor$SelectionHandleView.updatePosition(Editor.java:4863)
                  at android.widget.Editor$HandleView.onTouchEvent(Editor.java:4407)
                  at android.widget.Editor$SelectionHandleView.onTouchEvent(Editor.java:4876)
                  at android.view.View.dispatchTouchEvent(View.java:10024)
                  at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:2632)
                  at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2321)
                  at android.widget.PopupWindow$PopupDecorView.dispatchTouchEvent(PopupWindow.java:2277)
                  at android.view.View.dispatchPointerEvent(View.java:10244)
                  at android.view.ViewRootImpl$ViewPostImeInputStage.processPointerEvent(ViewRootImpl.java:4468)
                  at android.view.ViewRootImpl$ViewPostImeInputStage.onProcess(ViewRootImpl.java:4336)
                  at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:3883)
                  at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:3936)
                  at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:3902)
                  at android.view.ViewRootImpl$AsyncInputStage.forward(ViewRootImpl.java:4029)
                  at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:3910)
                  at android.view.ViewRootImpl$AsyncInputStage.apply(ViewRootImpl.java:4086)
                  at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:3883)
                  at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:3936)
                  at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:3902)
                  at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:3910)
                  at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:3883)
                  at android.view.ViewRootImpl.deliverInputEvent(ViewRootImpl.java:6284)
                  at android.view.ViewRootImpl.doProcessInputEvents(ViewRootImpl.java:6258)
                  at android.view.ViewRootImpl.enqueueInputEvent(ViewRootImpl.java:6219)
                  at android.view.ViewRootImpl$WindowInputEventReceiver.onInputEvent(ViewRootImpl.java:6387)
                  at android.view.InputEventReceiver.dispatchInputEvent(InputEventReceiver.java:185)
                  at android.view.InputEventReceiver.nativeConsumeBatchedInputEvents(Native Method)
                  at android.view.InputEventReceiver.consumeBatchedInputEvents(InputEventReceiver.java:176)
                  at android.view.ViewRootImpl.doConsumeBatchedInput(ViewRootImpl.java:6358)
                  at android.view.ViewRootImpl$ConsumeBatchedInputRunnable.run(ViewRootImpl.java:6410)
                  at android.view.Choreographer$CallbackRecord.run(Choreographer.java:874)
                  at android.view.Choreographer.doCallbacks(Choreographer.java:686)
                  at android.view.Choreographer.doFrame(Choreographer.java:615)
                  at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:860)
                  at android.os.Handler.handleCallback(Handler.java:751)
             */
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
/*
        java.lang.IllegalArgumentException
                at com.android.internal.util.Preconditions.checkArgument(Preconditions.java:33)
                at android.widget.SelectionActionModeHelper$TextClassificationHelper.init(SelectionActionModeHelper.java:640)
                at android.widget.SelectionActionModeHelper.resetTextClassificationHelper(SelectionActionModeHelper.java:203)
                at android.widget.SelectionActionModeHelper.invalidateActionModeAsync(SelectionActionModeHelper.java:104)
                at android.widget.Editor.invalidateActionModeAsync(Editor.java:2028)
                at android.widget.Editor.showFloatingToolbar(Editor.java:1419)
                at android.widget.Editor.updateFloatingToolbarVisibility(Editor.java:1397)
                at android.widget.Editor.onTouchEvent(Editor.java:1367)
                at android.widget.TextView.onTouchEvent(TextView.java:9701)
*/
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        try {
            return super.onKeyPreIme(keyCode, event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        try {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        } catch (Throwable ex) {
/*
            java.lang.ClassCastException: android.text.SpannedString cannot be cast to android.text.Spannable
              at android.widget.Editor.onFocusChanged(Editor.java:1058)
              at android.widget.TextView.onFocusChanged(TextView.java:9262)
              at android.view.View.handleFocusGainInternal(View.java:5388)
              at android.view.View.requestFocusNoSearch(View.java:8131)
              at android.view.View.requestFocus(View.java:8110)
              at android.view.View.requestFocus(View.java:8077)
              at android.view.View.requestFocus(View.java:8056)
              at android.view.View.onTouchEvent(View.java:10359)
              at android.widget.TextView.onTouchEvent(TextView.java:9580)
              at android.view.View.dispatchTouchEvent(View.java:8981)
*/
            Log.w(ex);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        try {
            super.onWindowFocusChanged(hasWindowFocus);
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.IndexOutOfBoundsException: setSpan (-1 ... -1) starts before 0
                        at android.text.SpannableStringInternal.checkRange(SpannableStringInternal.java:434)
                        at android.text.SpannableStringInternal.setSpan(SpannableStringInternal.java:155)
                        at android.text.SpannableString.setSpan(SpannableString.java:46)
                        at android.text.Selection.setSelection(Selection.java:76)
                        at android.widget.Editor$SelectionModifierCursorController.resetDragAcceleratorState(Editor.java:5613)
                        at android.widget.Editor$SelectionModifierCursorController.resetTouchOffsets(Editor.java:5603)
                        at android.widget.Editor$SelectionModifierCursorController.<init>(Editor.java:5286)
                        at android.widget.Editor.getSelectionController(Editor.java:2253)
                        at android.widget.Editor.refreshTextActionMode(Editor.java:1922)
                        at android.widget.Editor.onWindowFocusChanged(Editor.java:1314)
                        at android.widget.TextView.onWindowFocusChanged(TextView.java:8735)
                        at android.view.View.dispatchWindowFocusChanged(View.java:10246)
                        at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1196)
             */
        }
    }

    @Override
    public boolean performClick() {
        try {
            return super.performClick();
        } catch (Throwable ex) {
            /*
                android.database.sqlite.SQLiteException: not an error (code 0 SQLITE_OK[0]): Could not open the database in read/write mode.
                android.database.sqlite.SQLiteException: not an error (code 0 SQLITE_OK[0]): Could not open the database in read/write mode.
                  at android.database.sqlite.SQLiteConnection.nativeOpen(Native Method)
                  at android.database.sqlite.SQLiteConnection.open(SQLiteConnection.java:284)
                  at android.database.sqlite.SQLiteConnection.open(SQLiteConnection.java:215)
                  at android.database.sqlite.SQLiteConnectionPool.openConnectionLocked(SQLiteConnectionPool.java:705)
                  at android.database.sqlite.SQLiteConnectionPool.open(SQLiteConnectionPool.java:272)
                  at android.database.sqlite.SQLiteConnectionPool.open(SQLiteConnectionPool.java:239)
                  at android.database.sqlite.SQLiteDatabase.openInner(SQLiteDatabase.java:1292)
                  at android.database.sqlite.SQLiteDatabase.open(SQLiteDatabase.java:1247)
                  at android.database.sqlite.SQLiteDatabase.openDatabase(SQLiteDatabase.java:903)
                  at android.database.sqlite.SQLiteDatabase.openDatabase(SQLiteDatabase.java:893)
                  at android.database.sqlite.SQLiteOpenHelper.getDatabaseLocked(SQLiteOpenHelper.java:365)
                  at android.database.sqlite.SQLiteOpenHelper.getReadableDatabase(SQLiteOpenHelper.java:322)
                  at com.androidx.galaxy.route.SpeedGps_MainActivity.readRadars(SpeedGps_MainActivity.java:2210)
                  at com.androidx.galaxy.route.SpeedGps_MainActivity.onClick(SpeedGps_MainActivity.java:1753)
                  at android.view.View.performClick(View.java:7357)
                  at android.widget.TextView.performClick(TextView.java:14263)
                  at android.view.View.performClickInternal(View.java:7323)
                  at android.view.View.access$3200(View.java:849)
                  at android.view.View$PerformClick.run(View.java:27884)
                  at android.os.Handler.handleCallback(Handler.java:873)
             */
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
/*
            java.lang.NullPointerException: Attempt to invoke virtual method 'int android.widget.Editor$SelectionModifierCursorController.getMinTouchOffset()' on a null object reference
                    at android.widget.Editor.touchPositionIsInSelection(Unknown:36)
                    at android.widget.Editor.performLongClick(Unknown:72)
                    at android.widget.TextView.performLongClick(Unknown:24)
*/
            Log.w(ex);
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            return super.onKeyDown(keyCode, event);
        } catch (Throwable ex) {
            /*
                java.lang.IllegalArgumentException
                  at com.android.internal.util.Preconditions.checkArgument(Preconditions.java:33)
                  at android.widget.SelectionActionModeHelper$TextClassificationHelper.init(SelectionActionModeHelper.java:641)
                  at android.widget.SelectionActionModeHelper.resetTextClassificationHelper(SelectionActionModeHelper.java:204)
                  at android.widget.SelectionActionModeHelper.startActionModeAsync(SelectionActionModeHelper.java:88)
                  at android.widget.Editor.startSelectionActionModeAsync(Editor.java:2021)
                  at android.widget.Editor.refreshTextActionMode(Editor.java:1966)
                  at android.widget.TextView.spanChange(TextView.java:9525)
                  at android.widget.TextView$ChangeWatcher.onSpanChanged(TextView.java:11973)
                  at android.text.SpannableStringBuilder.sendSpanChanged(SpannableStringBuilder.java:1292)
                  at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:748)
                  at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:672)
                  at android.text.Selection.extendSelection(Selection.java:102)
                  at android.text.Selection.extendLeft(Selection.java:324)
                  at android.text.method.ArrowKeyMovementMethod.left(ArrowKeyMovementMethod.java:72)
                  at android.text.method.BaseMovementMethod.handleMovementKey(BaseMovementMethod.java:165)
                  at android.text.method.ArrowKeyMovementMethod.handleMovementKey(ArrowKeyMovementMethod.java:65)
                  at android.text.method.BaseMovementMethod.onKeyDown(BaseMovementMethod.java:42)
                  at android.widget.TextView.doKeyDown(TextView.java:7367)
                  at android.widget.TextView.onKeyDown(TextView.java:7117)
                  at android.view.KeyEvent.dispatch(KeyEvent.java:2707)
             */
            Log.w(ex);
            return false;
        }
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        try {
            return super.startActionMode(callback);
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        try {
            return super.startActionMode(callback, type);
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        try {
            super.setText(text, type);
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.IndexOutOfBoundsException:
                  at android.text.PackedIntVector.getValue (PackedIntVector.java:71)
                  at android.text.DynamicLayout.getLineTop (DynamicLayout.java:602)
                  at android.text.Layout.getLineBottom (Layout.java:1260)
                  at android.widget.TextView.invalidateRegion (TextView.java:5379)
                  at android.widget.TextView.invalidateCursor (TextView.java:5348)
                  at android.widget.TextView.spanChange (TextView.java:8351)
                  at android.widget.TextView$ChangeWatcher.onSpanAdded (TextView.java:10550)
                  at android.text.SpannableStringInternal.sendSpanAdded (SpannableStringInternal.java:315)
                  at android.text.SpannableStringInternal.setSpan (SpannableStringInternal.java:138)
                  at android.text.SpannableString.setSpan (SpannableString.java:46)
                  at android.text.Selection.setSelection (Selection.java:76)
                  at android.text.Selection.setSelection (Selection.java:87)
                  at android.text.method.ArrowKeyMovementMethod.initialize (ArrowKeyMovementMethod.java:336)
                  at android.widget.TextView.setText (TextView.java:4555)
                  at android.widget.TextView.setText (TextView.java:4424)
                  at android.widget.TextView.setText (TextView.java:4379)
             */
        }
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        try {
            if (id == android.R.id.copy) {
                Context context = getContext();
                ClipboardManager cbm = Helper.getSystemService(context, ClipboardManager.class);

                int start = getSelectionStart();
                int end = getSelectionEnd();
                if (start > end) {
                    int s = start;
                    start = end;
                    end = s;
                }

                if (start != end && cbm != null) {
                    CharSequence selected = getText().subSequence(start, end);
                    if (selected instanceof Spanned) {
                        String html = HtmlHelper.toHtml((Spanned) selected, context);
                        cbm.setPrimaryClip(ClipData.newHtmlText(context.getString(R.string.app_name), selected, html));
                        if (getText() instanceof Spannable)
                            Selection.setSelection((Spannable) getText(), end);
                        return true;
                    }
                }
            }

            return super.onTextContextMenuItem(id);
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.RuntimeException: PARAGRAPH span must start at paragraph boundary
                        at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:683)
                        at android.text.SpannableStringBuilder.change(SpannableStringBuilder.java:423)
                        at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:534)
                        at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:492)
                        at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:34)
                        at android.widget.TextView.paste(TextView.java:9761)
                        at android.widget.TextView.onTextContextMenuItem(TextView.java:9434)
                        at android.widget.Editor$TextActionModeCallback.onActionItemClicked(Editor.java:3303)
             */
            return false;
        }
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        try {
            super.onInitializeAccessibilityNodeInfo(info);
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.IndexOutOfBoundsException: setSpan (-1 ... -1) starts before 0
                    at android.text.SpannableStringBuilder.checkRange(SpannableStringBuilder.java:1331)
                    at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:685)
                    at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:677)
                    at android.view.accessibility.AccessibilityNodeInfo.replaceClickableSpan(AccessibilityNodeInfo.java:2926)
                    at android.view.accessibility.AccessibilityNodeInfo.setText(AccessibilityNodeInfo.java:2890)
                    at android.widget.TextView.onInitializeAccessibilityNodeInfoInternal(TextView.java:12033)
                    at android.view.View.onInitializeAccessibilityNodeInfo(View.java:8627)
                    at android.view.View.createAccessibilityNodeInfoInternal(View.java:8586)
                    at android.view.View.createAccessibilityNodeInfo(View.java:8571)
                    at android.view.AccessibilityInteractionController$AccessibilityNodePrefetcher.prefetchDescendantsOfRealNode(AccessibilityInteractionController.java:1358)
                    at android.view.AccessibilityInteractionController$AccessibilityNodePrefetcher.prefetchDescendantsOfRealNode(AccessibilityInteractionController.java:1381)
                    at android.view.AccessibilityInteractionController$AccessibilityNodePrefetcher.prefetchDescendantsOfRealNode(AccessibilityInteractionController.java:1381)
                    at android.view.AccessibilityInteractionController$AccessibilityNodePrefetcher.prefetchDescendantsOfRealNode(AccessibilityInteractionController.java:1381)
                    at android.view.AccessibilityInteractionController$AccessibilityNodePrefetcher.prefetchAccessibilityNodeInfos(AccessibilityInteractionController.java:1183)
                    at android.view.AccessibilityInteractionController.findAccessibilityNodeInfoByAccessibilityIdUiThread(AccessibilityInteractionController.java:368)
                    at android.view.AccessibilityInteractionController.access$500(AccessibilityInteractionController.java:74)
                    at android.view.AccessibilityInteractionController$PrivateHandler.handleMessage(AccessibilityInteractionController.java:1547)
                    at android.os.Handler.dispatchMessage(Handler.java:106)
             */
        }
    }
}
