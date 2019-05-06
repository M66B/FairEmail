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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

public class EditTextCompose extends AppCompatEditText {
    private IInputContentListener listener = null;

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
    public boolean onTextContextMenuItem(int id) {
        if (id == android.R.id.paste && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return super.onTextContextMenuItem(android.R.id.pasteAsPlainText);
        else
            return super.onTextContextMenuItem(id);
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
                    if (listener == null)
                        throw new IllegalArgumentException("InputContent listener not set");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 &&
                            (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0)
                        info.requestPermission();

                    listener.onInputContent(info.getContentUri());
                    return true;
                } catch (Throwable ex) {
                    Log.w(ex);
                    return false;
                }
            }
        });
    }

    void setInputContentListener(IInputContentListener listener) {
        this.listener = listener;
    }

    interface IInputContentListener {
        void onInputContent(Uri uri);
    }
}
