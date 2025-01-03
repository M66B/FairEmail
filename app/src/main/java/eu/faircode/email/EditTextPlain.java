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
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

public class EditTextPlain extends FixedEditText {
    public EditTextPlain(Context context) {
        super(context);
        Helper.setKeyboardIncognitoMode(this, context);
    }

    public EditTextPlain(Context context, AttributeSet attrs) {
        super(context, attrs);
        Helper.setKeyboardIncognitoMode(this, context);
    }

    public EditTextPlain(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Helper.setKeyboardIncognitoMode(this, context);
    }

    @Override
    protected void onAttachedToWindow() {
        // Spellchecker workaround
        boolean enabled = isEnabled();
        super.setEnabled(true);
        super.onAttachedToWindow();
        super.setEnabled(enabled);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        InputConnection ic = super.onCreateInputConnection(editorInfo);
        if (ic == null)
            return null;

        return new InputConnectionWrapper(ic, true) {
            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                try {
                    return super.commitText(text.toString(), newCursorPosition);
                } catch (Throwable ex) {
                    Log.w(ex);
                    return false;
                    /*
                        https://issuetracker.google.com/issues/216891011
                        java.lang.IndexOutOfBoundsException: 199, -198
                            at android.text.PackedIntVector.deleteAt(PackedIntVector.java:222)
                            at android.text.DynamicLayout.reflow(DynamicLayout.java:317)
                            at android.text.DynamicLayout.-wrap0(Unknown Source:0)
                            at android.text.DynamicLayout$ChangeWatcher.reflow(DynamicLayout.java:750)
                            at android.text.DynamicLayout$ChangeWatcher.onSpanChanged(DynamicLayout.java:779)
                            at androidx.emoji2.text.SpannableBuilder$WatcherWrapper.onSpanChanged(SourceFile:2)
                            at android.text.SpannableStringBuilder.sendSpanChanged(SpannableStringBuilder.java:1296)
                            at android.text.SpannableStringBuilder.sendToSpanWatchers(SpannableStringBuilder.java:651)
                            at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:581)
                            at androidx.emoji2.text.SpannableBuilder.replace(SourceFile:7)
                            at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:504)
                            at androidx.emoji2.text.SpannableBuilder.replace(SourceFile:4)
                            at androidx.emoji2.text.SpannableBuilder.replace(SourceFile:1)
                            at android.view.inputmethod.BaseInputConnection.replaceText(BaseInputConnection.java:848)
                            at android.view.inputmethod.BaseInputConnection.commitText(BaseInputConnection.java:197)
                            at com.android.internal.widget.EditableInputConnection.commitText(EditableInputConnection.java:183)
                            at android.view.inputmethod.InputConnectionWrapper.commitText(InputConnectionWrapper.java:158)
                            at android.view.inputmethod.InputConnectionWrapper.commitText(InputConnectionWrapper.java:158)
                            at com.android.internal.view.IInputConnectionWrapper.executeMessage(IInputConnectionWrapper.java:344)
                            at com.android.internal.view.IInputConnectionWrapper$MyHandler.handleMessage(IInputConnectionWrapper.java:85)
                     */
                }
            }

            @Override
            public CharSequence getSelectedText(int flags) {
                try {
                    return super.getSelectedText(flags);
                } catch (Throwable ex) {
                    Log.w(ex);
                    return null;
                    /*
                        java.lang.IndexOutOfBoundsException: getChars (-1 ... 52) starts before 0
                          at android.text.SpannableStringBuilder.checkRange(SpannableStringBuilder.java:1314)
                          at android.text.SpannableStringBuilder.getChars(SpannableStringBuilder.java:1191)
                          at android.text.TextUtils.getChars(TextUtils.java:100)
                          at android.text.SpannableStringBuilder.<init>(SpannableStringBuilder.java:67)
                          at android.text.SpannableStringBuilder.subSequence(SpannableStringBuilder.java:1183)
                          at android.view.inputmethod.BaseInputConnection.getSelectedText(BaseInputConnection.java:528)
                          at android.view.inputmethod.InputConnectionWrapper.getSelectedText(InputConnectionWrapper.java:94)
                          at com.android.internal.view.IInputConnectionWrapper.executeMessage(IInputConnectionWrapper.java:286)
                          at com.android.internal.view.IInputConnectionWrapper$MyHandler.handleMessage(IInputConnectionWrapper.java:85)
                          at android.os.Handler.dispatchMessage(Handler.java:106)
                     */
                }
            }
        };
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        try {
            if (id == android.R.id.paste) {
                Context context = getContext();
                ClipboardManager cbm = Helper.getSystemService(context, ClipboardManager.class);
                if (cbm != null && cbm.hasPrimaryClip()) {
                    ClipData data = cbm.getPrimaryClip();
                    ClipDescription description = (data == null ? null : data.getDescription());
                    ClipData.Item item = (data == null ? null : data.getItemAt(0));
                    CharSequence text = (item == null ? null : item.coerceToText(context));
                    if (text != null) {
                        CharSequence label = (description == null ? "coerced_plain_text" : description.getLabel());
                        data = ClipData.newPlainText(label, text.toString());
                        cbm.setPrimaryClip(data);
                    }
                }
            }

            return super.onTextContextMenuItem(id);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }
}
