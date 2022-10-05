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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TupleKeyword {
    public String name;
    public boolean selected;
    public Integer color;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TupleKeyword) {
            TupleKeyword other = (TupleKeyword) obj;
            return (this.name.equals(other.name) &&
                    this.selected == other.selected &&
                    Objects.equals(this.color, other.color));
        } else
            return false;
    }

    public static class Persisted {
        public String[] selected;
        public String[] available;
    }

    static List<TupleKeyword> from(Context context, Persisted data) {
        if (data.selected == null)
            data.selected = new String[0];
        if (data.available == null)
            data.available = new String[0];

        List<TupleKeyword> result = new ArrayList<>();

        List<String> all = new ArrayList<>();
        List<String> selected = Arrays.asList(data.selected);

        for (String keyword : data.selected)
            if (!all.contains(keyword))
                all.add(keyword);

        for (String keyword : data.available)
            if (!all.contains(keyword))
                all.add(keyword);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Collections.sort(all, new Comparator<String>() {
            @Override
            public int compare(String k1, String k2) {
                k1 = prefs.getString("kwtitle." + k1, getDefaultKeywordAlias(context, k1));
                k2 = prefs.getString("kwtitle." + k2, getDefaultKeywordAlias(context, k2));
                return k1.compareTo(k2);
            }
        });

        for (String keyword : all) {
            TupleKeyword k = new TupleKeyword();
            k.name = keyword;
            k.selected = selected.contains(keyword);

            String c1 = "kwcolor." + keyword;
            String c2 = "keyword." + keyword; // legacy
            if (prefs.contains(c1))
                k.color = prefs.getInt(c1, Color.GRAY);
            else if (prefs.contains(c2))
                k.color = prefs.getInt(c2, Color.GRAY);

            result.add(k);
        }

        return result;
    }

    static String getDefaultKeywordAlias(Context context, String keyword) {
        switch (keyword) {
            case "$label1": // Important
                return context.getString(R.string.title_keyword_label1);
            case "$label2": // Work
                return context.getString(R.string.title_keyword_label2);
            case "$label3": // Personal
                return context.getString(R.string.title_keyword_label3);
            case "$label4": // To do
                return context.getString(R.string.title_keyword_label4);
            case "$label5": // Later
                return context.getString(R.string.title_keyword_label5);
            default:
                return keyword;
        }
    }

    static Integer getDefaultKeywordColor(Context context, String keyword) {
        switch (keyword) {
            case "$label1": // Important
                return Color.parseColor("#FF0000");
            case "$label2": // Work
                return Color.parseColor("#FF9900");
            case "$label3": // Personal
                return Color.parseColor("#009900");
            case "$label4": // To do
                return Color.parseColor("#3333FF");
            case "$label5": // Later
                return Color.parseColor("#993399");
            default:
                return null;
        }
    }
}
