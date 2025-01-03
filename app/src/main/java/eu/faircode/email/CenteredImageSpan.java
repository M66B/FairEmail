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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CenteredImageSpan extends ImageSpan {
    public CenteredImageSpan(final Drawable drawable) {
        this(drawable, DynamicDrawableSpan.ALIGN_BOTTOM);
    }

    public CenteredImageSpan(final Drawable drawable, final int verticalAlignment) {
        super(drawable, verticalAlignment);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
                     int start, int end, float x,
                     int top, int y, int bottom, @NonNull Paint paint) {
        getDrawable().draw(canvas);
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text,
                       int start, int end,
                       @Nullable Paint.FontMetricsInt fm) {
        Drawable d = getDrawable();
        Rect rect = d.getBounds();

        if (fm != null) {
            int descent = 0;
            int padding = 0;
            if (rect.bottom - (fm.descent - fm.ascent) >= 0) {
                descent = fm.descent;
                padding = rect.bottom - (fm.descent - fm.ascent);
            }

            fm.descent = padding / 2 + descent;
            fm.bottom = fm.descent;

            fm.ascent = -rect.bottom + fm.descent;
            fm.top = fm.ascent;
        }

        return rect.right;
    }
}
