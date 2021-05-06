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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.text.Layout;
import android.text.style.BulletSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class BulletSpanEx extends BulletSpan {
    private int indentWidth;
    private int level;

    public BulletSpanEx(int indentWidth, int gapWidth, int color, int level) {
        super(gapWidth, color);
        this.indentWidth = indentWidth;
        this.level = level;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public BulletSpanEx(int indentWidth, int gapWidth, int color, int bulletRadius, int level) {
        super(gapWidth, color, bulletRadius);
        this.indentWidth = indentWidth;
        this.level = level;
    }

    int getLevel() {
        return this.level;
    }

    void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        // https://issuetracker.google.com/issues/36956124
        // This is called before drawLeadingMargin to justify the text
        return indentWidth * level + super.getLeadingMargin(first);
    }

    @Override
    public void drawLeadingMargin(@NonNull Canvas canvas, @NonNull Paint paint, int x, int dir, int top, int baseline, int bottom, @NonNull CharSequence text, int start, int end, boolean first, @Nullable Layout layout) {
        super.drawLeadingMargin(canvas, paint, x + indentWidth * level, dir, top, baseline, bottom, text, start, end, first, layout);
    }
}
