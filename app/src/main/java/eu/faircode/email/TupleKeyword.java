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

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        List<TupleKeyword> result = new ArrayList<>();

        List<String> keywords = new ArrayList<>();

        for (String keyword : data.selected)
            if (!keywords.contains(keyword))
                keywords.add(keyword);

        for (String keyword : data.available)
            if (!keywords.contains(keyword))
                keywords.add(keyword);

        Collections.sort(keywords);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        for (String keyword : keywords) {
            TupleKeyword k = new TupleKeyword();
            k.name = keyword;
            k.selected = Arrays.asList(data.selected).contains(keyword);

            String c = "keyword." + keyword;
            if (prefs.contains(c))
                k.color = prefs.getInt(c, -1);

            result.add(k);
        }

        return result;
    }
}
