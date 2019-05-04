package eu.faircode.email;

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
