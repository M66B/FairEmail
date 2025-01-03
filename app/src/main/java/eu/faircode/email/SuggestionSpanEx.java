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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.text.TextPaint;
import android.text.style.SuggestionSpan;

import androidx.preference.PreferenceManager;

import java.lang.reflect.Field;

public class SuggestionSpanEx extends SuggestionSpan {
    private String description;
    private final int highlightColor;
    private final int underlineColor;
    private final int underlineThickness;

    public SuggestionSpanEx(Context context, String description, String[] suggestions, boolean misspelled) {
        super(context, suggestions, 0);

        int flags = (misspelled || Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                ? SuggestionSpan.FLAG_MISSPELLED
                : SuggestionSpan.FLAG_GRAMMAR_ERROR);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean easy_correct = prefs.getBoolean("easy_correct", false);
        if (easy_correct)
            flags |= SuggestionSpan.FLAG_EASY_CORRECT;

        this.setFlags(flags);

        this.description = description;
        highlightColor = Helper.resolveColor(context, android.R.attr.textColorHighlight);
        underlineColor = (misspelled ? Color.MAGENTA : highlightColor);
        underlineThickness = Helper.dp2pixels(context, misspelled ? 2 : (BuildConfig.DEBUG ? 1 : 2));
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            try {
                Field fUnderlineColor = tp.getClass().getDeclaredField("underlineColor");
                Field fUnderlineThickness = tp.getClass().getDeclaredField("underlineThickness");
                fUnderlineColor.setAccessible(true);
                fUnderlineThickness.setAccessible(true);
                fUnderlineColor.set(tp, underlineColor);
                fUnderlineThickness.set(tp, underlineThickness);
            } catch (Throwable ex) {
                Log.i(ex);
                tp.bgColor = highlightColor;
            }
        else {
            tp.underlineColor = underlineColor;
            tp.underlineThickness = underlineThickness;
        }
    }
}