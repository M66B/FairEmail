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
import android.util.AttributeSet;

public class EditTextPlain extends FixedEditText {
    public EditTextPlain(Context context) {
        super(context);
    }

    public EditTextPlain(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPlain(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        try {
            if (id == android.R.id.paste) {
                Context context = getContext();
                ClipboardManager cbm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (cbm != null && cbm.hasPrimaryClip()) {
                    ClipData data = cbm.getPrimaryClip();
                    ClipData.Item item = data.getItemAt(0);

                    CharSequence text = item.coerceToText(context);
                    data = ClipData.newPlainText("coerced_plain_text", text);
                    cbm.setPrimaryClip(data);
                }
            }

            return super.onTextContextMenuItem(id);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }
}
