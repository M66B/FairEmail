package eu.faircode.email;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class EditTextCompose extends AppCompatEditText {
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
}
