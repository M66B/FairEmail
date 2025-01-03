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
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.textclassifier.TextClassifier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatEditText;

public class FixedEditText extends AppCompatEditText {
    public FixedEditText(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FixedEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FixedEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setCustomSelectionActionModeCallback(Helper.getActionModeWrapper(this));
    }

    @Override
    public void setSelection(int index) {
        try {
            super.setSelection(index);
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.IndexOutOfBoundsException: setSpan (2 ... 2) ends beyond length 0
                        at android.text.SpannableStringBuilder.checkRange(SpannableStringBuilder.java:1265)
                        at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:684)
                        at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:677)
                        at android.text.Selection.setSelection(Selection.java:76)
                        at android.widget.EditText.setSelection(EditText.java:96)
                        at android.widget.NumberPicker$SetSelectionCommand.run(NumberPicker.java:2246)
                        at android.os.Handler.handleCallback(Handler.java:754)
                        at android.os.Handler.dispatchMessage(Handler.java:95)
             */
        }
    }

    @Override
    public void setSelection(int start, int stop) {
        try {
            super.setSelection(start, stop);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        try {
            super.setText(text, type);
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.IndexOutOfBoundsException: charAt: -1 < 0
                  at android.text.SpannableStringBuilder.charAt(SpannableStringBuilder.java:122)
                  at java.lang.Character.codePointBefore(Character.java:5002)
                  at android.widget.SpellChecker.spellCheck(SpellChecker.java:287)
                  at android.widget.SpellChecker.access$1000(SpellChecker.java:48)
                  at android.widget.SpellChecker$SpellParser.parse(SpellChecker.java:741)
                  at android.widget.SpellChecker$SpellParser.parse(SpellChecker.java:520)
                  at android.widget.SpellChecker.spellCheck(SpellChecker.java:245)
                  at android.widget.Editor.updateSpellCheckSpans(Editor.java:775)
                  at android.widget.Editor.sendOnTextChanged(Editor.java:1470)
                  at android.widget.TextView.sendOnTextChanged(TextView.java:10576)
                  at android.widget.TextView.setText(TextView.java:6299)
                  at android.widget.TextView.setText(TextView.java:6124)
                  at android.widget.EditText.setText(EditText.java:122)
                  at android.widget.TextView.setText(TextView.java:6076)
             */
        }
    }

    @Override
    public boolean onPreDraw() {
        try {
            return super.onPreDraw();
        } catch (Throwable ex) {
            Log.w(ex);
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
                java.lang.ArrayIndexOutOfBoundsException: length=39; index=-3
                  at android.text.DynamicLayout.getBlockIndex(DynamicLayout.java:648)
                  at android.widget.Editor.drawHardwareAccelerated(Editor.java:1703)
                  at android.widget.Editor.onDraw(Editor.java:1672)
                  at android.widget.TextView.onDraw(TextView.java:6914)
                  at android.view.View.draw(View.java:19200)
                Fixed in Android 9:
                https://android-review.googlesource.com/c/platform/frameworks/base/+/634929
            */
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            return super.dispatchTouchEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        try {
            super.dispatchWindowFocusChanged(hasFocus);
        } catch (Throwable ex) {
            /*
				java.lang.SecurityException: No access to content://com.sec.android.semclipboardprovider/images: neither user 1010197 nor current process has android.permission.INTERACT_ACROSS_USERS_FULL or android.permission.INTERACT_ACROSS_USERS
				  at android.os.Parcel.createException(Parcel.java:2088)
				  at android.os.Parcel.readException(Parcel.java:2056)
				  at android.os.Parcel.readException(Parcel.java:2004)
				  at android.sec.clipboard.IClipboardService$Stub$Proxy.getClipData(IClipboardService.java:951)
				  at com.samsung.android.content.clipboard.SemClipboardManager.getLatestClip(SemClipboardManager.java:612)
				  at android.widget.EditText.updateClipboardFilter(EditText.java:316)
				  at android.widget.EditText.dispatchWindowFocusChanged(EditText.java:297)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewGroup.dispatchWindowFocusChanged(ViewGroup.java:1502)
				  at android.view.ViewRootImpl.handleWindowFocusChanged(ViewRootImpl.java:3458)
				  at android.view.ViewRootImpl.access$1300(ViewRootImpl.java:205)
				  at android.view.ViewRootImpl$ViewRootHandler.handleMessage(ViewRootImpl.java:5361)
				  at android.os.Handler.dispatchMessage(Handler.java:107)
             */
            Log.w(ex);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        try {
            return super.onKeyPreIme(keyCode, event);
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.IndexOutOfBoundsException: setSpan (-1 ... -1) starts before 0
                        at android.text.SpannableStringInternal.checkRange(SpannableStringInternal.java:434)
                        at android.text.SpannableStringInternal.setSpan(SpannableStringInternal.java:155)
                        at android.text.SpannableString.setSpan(SpannableString.java:46)
                        at android.text.Selection.setSelection(Selection.java:76)
                        at android.widget.TextView.semSetSelection(TextView.java:11458)
                        at android.widget.TextView.semSetSelection(TextView.java:11472)
                        at android.widget.Editor$TextActionModeCallback.onDestroyActionMode(Editor.java:4268)
                        at com.android.internal.policy.DecorView$ActionModeCallback2Wrapper.onDestroyActionMode(DecorView.java:2957)
                        at com.android.internal.view.FloatingActionMode.finish(FloatingActionMode.java:307)
                        at android.widget.Editor.stopTextActionMode(Editor.java:2356)
                        at android.widget.TextView.stopTextActionMode(TextView.java:11253)
                        at android.widget.TextView.handleBackInTextActionModeIfNeeded(TextView.java:7099)
                        at android.widget.TextView.onKeyPreIme(TextView.java:7052)
                        at android.view.View.dispatchKeyEventPreIme(View.java:10527)
                        at android.view.ViewGroup.dispatchKeyEventPreIme(ViewGroup.java:1679)
                        at android.view.ViewRootImpl$ViewPreImeInputStage.processKeyEvent(ViewRootImpl.java:4801)
                        at android.view.ViewRootImpl$ViewPreImeInputStage.onProcess(ViewRootImpl.java:4787)
                        at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:4493)
                        at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:4546)
                        at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:4512)
                        at android.view.ViewRootImpl$AsyncInputStage.forward(ViewRootImpl.java:4645)
                        at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:4520)
                        at android.view.ViewRootImpl$AsyncInputStage.apply(ViewRootImpl.java:4702)
                        at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:4493)
                        at android.view.ViewRootImpl.deliverInputEvent(ViewRootImpl.java:7000)
                        at android.view.ViewRootImpl.doProcessInputEvents(ViewRootImpl.java:6929)
                        at android.view.ViewRootImpl.enqueueInputEvent(ViewRootImpl.java:6890)
                        at android.view.ViewRootImpl$WindowInputEventReceiver.onInputEvent(ViewRootImpl.java:7110)
                        at android.view.InputEventReceiver.dispatchInputEvent(InputEventReceiver.java:185)
             */
            return false;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        try {
            return super.onKeyUp(keyCode, event);
        } catch (Throwable ex) {
            Log.w(ex);
            return true;
        }
    }

    @Override
    public boolean performClick() {
        try {
            return super.performClick();
        } catch (Throwable ex) {
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
            // callback class: android.widget.Editor$TextActionModeCallback
            return super.startActionMode(callback, type);
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    @NonNull
    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public TextClassifier getTextClassifier() {
        /*
            https://issuetracker.google.com/issues/188103468

            java.lang.RuntimeException: An error occurred while executing doInBackground()
              at android.os.AsyncTask$3.done(AsyncTask.java:353)
              at java.util.concurrent.FutureTask.finishCompletion(FutureTask.java:383)
              at java.util.concurrent.FutureTask.setException(FutureTask.java:252)
              at java.util.concurrent.FutureTask.run(FutureTask.java:271)
              at android.os.AsyncTask$SerialExecutor$1.run(AsyncTask.java:245)
              at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1162)
              at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:636)
              at java.lang.Thread.run(Thread.java:764)
            Caused by: java.lang.IllegalArgumentException
              at com.android.internal.util.Preconditions.checkArgument(Preconditions.java:33)
              at android.view.textclassifier.TextClassifierImpl.validateInput(TextClassifierImpl.java:520)
              at android.view.textclassifier.TextClassifierImpl.classifyText(TextClassifierImpl.java:152)
              at android.widget.SelectionActionModeHelper$TextClassificationHelper.performClassification(SelectionActionModeHelper.java:707)
              at android.widget.SelectionActionModeHelper$TextClassificationHelper.classifyText(SelectionActionModeHelper.java:655)
              at android.widget.SelectionActionModeHelper.-android_widget_SelectionActionModeHelper-mthref-1(SelectionActionModeHelper.java:94)
              at android.widget.-$Lambda$tTszxdFZ0V9nXhnBpPsqeBMO0fw$1.$m$2(Unknown Source:4)
              at android.widget.-$Lambda$tTszxdFZ0V9nXhnBpPsqeBMO0fw$1.get(Unknown Source:21)
              at android.widget.SelectionActionModeHelper$TextClassificationAsyncTask.doInBackground(SelectionActionModeHelper.java:572)
              at android.widget.SelectionActionModeHelper$TextClassificationAsyncTask.doInBackground(SelectionActionModeHelper.java:567)
              at android.os.AsyncTask$2.call(AsyncTask.java:333)
         */
        if (BuildConfig.DEBUG /*|| Helper.isSamsung()*/)
            return TextClassifier.NO_OP;
        else
            return super.getTextClassifier();
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        try {
            return super.onTextContextMenuItem(id);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }
}
