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
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.BulletSpan;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NumberSpan extends BulletSpan {
    private int indentWidth;
    private int level;
    private int index;

    private TextPaint tp;
    private String number;
    private int numberWidth;
    private int margin;

    private static final List<String> SUPPORTED_TYPES = Collections.unmodifiableList(Arrays.asList(
            "lower-alpha", "lower-latin",
            "upper-alpha", "upper-latin",
            "lower-roman", "upper-roman"
    ));

    // https://developer.mozilla.org/en-US/docs/Web/CSS/list-style-type
    static boolean isSupportedType(String type) {
        if (TextUtils.isEmpty(type))
            return false;
        if (type.startsWith("decimal"))
            return true;
        if (SUPPORTED_TYPES.contains(type))
            return true;
        return false;
    }

    public NumberSpan(int indentWidth, int gapWidth, int color, float textSize, int level, int index) {
        this(indentWidth, gapWidth, color, textSize, level, index, null);
    }

    public NumberSpan(int indentWidth, int gapWidth, int color, float textSize, int level, int index, String type) {
        tp = new TextPaint();
        tp.setStyle(Paint.Style.FILL);
        tp.setColor(color);
        tp.setTypeface(Typeface.MONOSPACE);
        tp.setTextSize(textSize);

        this.indentWidth = indentWidth;
        this.level = level;
        this.index = index;

        if (TextUtils.isEmpty(type))
            number = index + ".";
        else {
            switch (type) {
                case "a":
                case "lower-alpha":
                case "lower-latin":
                    number = Character.toString((char) ((int) 'a' + index - 1));
                    break;
                case "A":
                case "upper-alpha":
                case "upper-latin":
                    number = Character.toString((char) ((int) 'A' + index - 1));
                    break;
                case "i":
                case "lower-roman":
                    number = Helper.toRoman(index).toLowerCase(Locale.ROOT);
                    break;
                case "I":
                case "upper-roman":
                    number = Helper.toRoman(index);
                    break;
                default:
                    number = Integer.toString(index);
            }
            number += '.';
        }

        numberWidth = Math.round(tp.measureText(number));
        margin = numberWidth + gapWidth;
    }

    float getTextSize() {
        return tp.getTextSize();
    }

    int getLevel() {
        return this.level;
    }

    void setLevel(int level) {
        this.level = level;
    }

    int getIndex() {
        return index;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        // https://issuetracker.google.com/issues/36956124
        // This is called before drawLeadingMargin to justify the text
        return indentWidth * (level + 1) + margin;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        if (text instanceof Spanned &&
                ((Spanned) text).getSpanStart(this) == start) {
            float textSize = tp.getTextSize();
            if (textSize > p.getTextSize())
                tp.setTextSize(p.getTextSize());
            int offset = (dir < 0 ? numberWidth : 0);
            c.drawText(number, x + indentWidth * (level + 1) * dir - offset, baseline, tp);
            tp.setTextSize(textSize);
        }
    }
}
