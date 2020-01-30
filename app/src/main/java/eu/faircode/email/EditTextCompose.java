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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.QuoteSpan;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

import org.jsoup.nodes.Document;

public class EditTextCompose extends AppCompatEditText {
    private ISelection selectionListener = null;
    private IInputContentListener inputContentListener = null;

    public EditTextCompose(Context context) {
        super(context);
    }

    public EditTextCompose(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextCompose(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (selectionListener != null)
            selectionListener.onSelected(hasSelection());
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        try {
            if (id == android.R.id.paste) {
                Context context = getContext();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard.hasPrimaryClip()) {
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

                    String html = item.getHtmlText();
                    if (html == null) {
                        CharSequence text = item.getText();
                        if (text == null)
                            return false;
                        html = "<div>" + HtmlHelper.formatPre(text.toString()) + "</div>";
                    }
                    Document document = HtmlHelper.sanitize(context, html, false, false);
                    Spanned paste = HtmlHelper.fromHtml(document.html());

                    int colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);

                    SpannableStringBuilder ssb = new SpannableStringBuilder(paste);
                    QuoteSpan[] spans = ssb.getSpans(0, ssb.length(), QuoteSpan.class);
                    for (QuoteSpan span : spans) {
                        ssb.setSpan(
                                new StyledQuoteSpan(context, colorPrimary),
                                ssb.getSpanStart(span),
                                ssb.getSpanEnd(span),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb.removeSpan(span);
                    }

                    int start = getSelectionStart();
                    int end = getSelectionEnd();

                    if (start < 0)
                        start = 0;
                    if (end < 0)
                        end = 0;

                    if (start > end) {
                        int tmp = start;
                        start = end;
                        end = tmp;
                    }

                    if (start == end)
                        getText().insert(start, ssb);
                    else
                        getText().replace(start, end, ssb);

                    return true;
                }
            }

            return super.onTextContextMenuItem(id);
        } catch (Throwable ex) {
            /*
                java.lang.RuntimeException: PARAGRAPH span must start at paragraph boundary
                        at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:619)
                        at android.text.SpannableStringBuilder.change(SpannableStringBuilder.java:391)
                        at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:496)
                        at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:454)
                        at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:33)
                        at android.widget.TextView.paste(TextView.java:8891)
                        at android.widget.TextView.onTextContextMenuItem(TextView.java:8706)
             */
            Log.w(ex);
            return false;
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        //https://developer.android.com/guide/topics/text/image-keyboard
        InputConnection ic = super.onCreateInputConnection(editorInfo);
        if (ic == null)
            return null;

        EditorInfoCompat.setContentMimeTypes(editorInfo, new String[]{"image/*"});

        return InputConnectionCompat.createWrapper(ic, editorInfo, new InputConnectionCompat.OnCommitContentListener() {
            @Override
            public boolean onCommitContent(InputContentInfoCompat info, int flags, Bundle opts) {
                Log.i("Uri=" + info.getContentUri());
                try {
                    if (inputContentListener == null)
                        throw new IllegalArgumentException("InputContent listener not set");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 &&
                            (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0)
                        info.requestPermission();

                    inputContentListener.onInputContent(info.getContentUri());
                    return true;
                } catch (Throwable ex) {
                    Log.w(ex);
                    return false;
                }
            }
        });
    }

    void setInputContentListener(IInputContentListener listener) {
        this.inputContentListener = listener;
    }

    interface IInputContentListener {
        void onInputContent(Uri uri);
    }

    void setSelectionListener(ISelection listener) {
        this.selectionListener = listener;
    }

    interface ISelection {
        void onSelected(boolean selection);
    }
}
