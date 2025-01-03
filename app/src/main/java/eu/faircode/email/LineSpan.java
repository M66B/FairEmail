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
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LineSpan extends ReplacementSpan {
    private int lineColor;
    private float strokeWidth;
    private float dashLength;

    LineSpan(int lineColor, float strokeWidth, float dashLength) {
        this.lineColor = lineColor;
        this.strokeWidth = strokeWidth;
        this.dashLength = dashLength;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return 0;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        int ypos = (top + bottom) / 2;
        int c = paint.getColor();
        float s = paint.getStrokeWidth();
        PathEffect p = paint.getPathEffect();
        paint.setColor(lineColor);
        paint.setStrokeWidth(strokeWidth);
        if (dashLength != 0)
            paint.setPathEffect(new DashPathEffect(new float[]{dashLength, dashLength}, 0));
        canvas.drawLine(0, ypos, canvas.getWidth(), ypos, paint);
        paint.setColor(c);
        paint.setStrokeWidth(s);
        paint.setPathEffect(p);
    }
}
