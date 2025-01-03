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
import android.os.Build;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.BulletSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class BulletSpanEx extends BulletSpan {
    private int indentWidth;
    private int level;
    private String ltype;

    public BulletSpanEx(int indentWidth, int gapWidth, int color, int level) {
        this(indentWidth, gapWidth, color, level, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public BulletSpanEx(int indentWidth, int gapWidth, int color, int bulletRadius, int level) {
        this(indentWidth, gapWidth, color, bulletRadius, level, null);
    }

    public BulletSpanEx(int indentWidth, int gapWidth, int color, int level, String ltype) {
        super(gapWidth, color);
        this.indentWidth = indentWidth;
        this.level = level;
        this.ltype = ltype;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public BulletSpanEx(int indentWidth, int gapWidth, int color, int bulletRadius, int level, String ltype) {
        super(gapWidth, color, bulletRadius);
        this.indentWidth = indentWidth;
        this.level = level;
        this.ltype = ltype;
    }

    int getLevel() {
        return this.level;
    }

    void setLevel(int level) {
        this.level = level;
    }

    String getLType() {
        return this.ltype;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        // https://issuetracker.google.com/issues/36956124
        // This is called before drawLeadingMargin to justify the text
        int margin = indentWidth * (level + 1);
        if (!"none".equals(ltype))
            margin += super.getLeadingMargin(first);
        return margin;
    }

    @Override
    public void drawLeadingMargin(@NonNull Canvas canvas, @NonNull Paint paint, int x, int dir, int top, int baseline, int bottom, @NonNull CharSequence text, int start, int end, boolean first, @Nullable Layout layout) {
        if ("none".equals(ltype))
            return;

        boolean mWantColor = false;
        int mColor = 0; // STANDARD_COLOR
        int mBulletRadius = 4; // STANDARD_BULLET_RADIUS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mWantColor = true;
            mColor = getColor();
            mBulletRadius = getBulletRadius();
        }

        if (((Spanned) text).getSpanStart(this) == start) {
            Paint.Style style = paint.getStyle();
            int oldcolor = 0;

            if (mWantColor) {
                oldcolor = paint.getColor();
                paint.setColor(mColor);
            }

            paint.setStyle(Paint.Style.FILL);

            if (layout != null) {
                // "bottom" position might include extra space as a result of line spacing
                // configuration. Subtract extra space in order to show bullet in the vertical
                // center of characters.
                final int line = layout.getLineForOffset(start);
                //bottom = bottom - layout.getLineExtra(line);
            }

            final float yPosition = (top + bottom) / 2f;
            final float xPosition = x + dir * (mBulletRadius + indentWidth * (level + 1));

            if ("square".equals(ltype))
                canvas.drawRect(
                        xPosition - mBulletRadius,
                        yPosition - mBulletRadius,
                        xPosition + mBulletRadius,
                        yPosition + mBulletRadius,
                        paint);
            else
                canvas.drawCircle(xPosition, yPosition, mBulletRadius, paint);

            if (mWantColor) {
                paint.setColor(oldcolor);
            }

            paint.setStyle(style);
        }
    }
}
