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

import android.text.TextPaint;
import android.text.style.SubscriptSpan;

import androidx.annotation.NonNull;

public class SubscriptSpanEx extends SubscriptSpan {
    @Override
    public void updateDrawState(@NonNull TextPaint textPaint) {
        super.updateDrawState(textPaint);
        textPaint.setTextSize(textPaint.getTextSize() * HtmlHelper.FONT_SMALL);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint textPaint) {
        super.updateMeasureState(textPaint);
        textPaint.setTextSize(textPaint.getTextSize() * HtmlHelper.FONT_SMALL);
    }
}
