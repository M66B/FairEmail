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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.room.Ignore;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TupleKeyword {
    public String name;
    public boolean selected;
    public boolean partial;
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
        @Ignore
        public String[] all_selected;
        public String[] available;

        public Persisted() {
        }

        public Persisted(List<String> selected, List<String> all, List<String> available) {
            this.selected = selected.toArray(new String[0]);
            this.all_selected = all.toArray(new String[0]);
            this.available = available.toArray(new String[0]);
        }
    }

    static List<TupleKeyword> from(Context context, Persisted data) {
        if (data.selected == null)
            data.selected = new String[0];
        if (data.available == null)
            data.available = new String[0];

        List<TupleKeyword> result = new ArrayList<>();

        List<String> all = new ArrayList<>();
        List<String> some = Arrays.asList(data.selected);
        List<String> selected = Arrays.asList(data.all_selected == null ? data.selected : data.all_selected);

        for (String keyword : selected)
            if (!all.contains(keyword))
                all.add(keyword);

        for (String keyword : data.available)
            if (!all.contains(keyword))
                all.add(keyword);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String global = prefs.getString("global_keywords", null);
        if (global != null)
            for (String kw : global.split(" "))
                if (!all.contains(kw))
                    all.add(kw);

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(all, new Comparator<String>() {
            @Override
            public int compare(String k1, String k2) {
                k1 = prefs.getString("kwtitle." + k1, getDefaultKeywordAlias(context, k1));
                k2 = prefs.getString("kwtitle." + k2, getDefaultKeywordAlias(context, k2));
                return collator.compare(k1, k2);
            }
        });

        for (String keyword : all) {
            TupleKeyword k = new TupleKeyword();
            k.name = keyword;
            k.selected = selected.contains(keyword);
            k.partial = (k.selected && !some.contains(keyword));

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

    static String getKeyword(Context context, String title) {
        if (TextUtils.isEmpty(title))
            return title;

        if (title.equalsIgnoreCase(context.getString(R.string.title_keyword_label1)))
            return "$label1";
        if (title.equalsIgnoreCase(context.getString(R.string.title_keyword_label2)))
            return "$label2";
        if (title.equalsIgnoreCase(context.getString(R.string.title_keyword_label3)))
            return "$label3";
        if (title.equalsIgnoreCase(context.getString(R.string.title_keyword_label4)))
            return "$label4";
        if (title.equalsIgnoreCase(context.getString(R.string.title_keyword_label5)))
            return "$label5";

        if (title.equalsIgnoreCase(context.getString(R.string.title_keyword_displayed)))
            return MessageHelper.FLAG_DISPLAYED;
        if (title.equalsIgnoreCase(context.getString(R.string.title_keyword_delivered)))
            return MessageHelper.FLAG_DELIVERED;
        if (title.equalsIgnoreCase(context.getString(R.string.title_keyword_not_displayed)))
            return MessageHelper.FLAG_NOT_DISPLAYED;
        if (title.equalsIgnoreCase(context.getString(R.string.title_keyword_not_delivered)))
            return MessageHelper.FLAG_NOT_DELIVERED;
        if (title.equalsIgnoreCase(context.getString(R.string.title_keyword_complaint)))
            return MessageHelper.FLAG_COMPLAINT;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (String key : prefs.getAll().keySet())
            if (key != null && key.startsWith("kwtitle.") &&
                    title.equalsIgnoreCase(prefs.getString(key, null))) {
                int dot = key.indexOf('.');
                if (dot >= 0)
                    return key.substring(dot + 1);
            }

        return title;
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
            case MessageHelper.FLAG_DISPLAYED:
                return context.getString(R.string.title_keyword_displayed);
            case MessageHelper.FLAG_DELIVERED:
                return context.getString(R.string.title_keyword_delivered);
            case MessageHelper.FLAG_NOT_DISPLAYED:
                return context.getString(R.string.title_keyword_not_displayed);
            case MessageHelper.FLAG_NOT_DELIVERED:
                return context.getString(R.string.title_keyword_not_delivered);
            case MessageHelper.FLAG_COMPLAINT:
                return context.getString(R.string.title_keyword_complaint);
            case MessageHelper.FLAG_PHISHING:
                return context.getString(R.string.title_keyword_phishing);
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

            case MessageHelper.FLAG_DISPLAYED:
            case MessageHelper.FLAG_DELIVERED:
                return Helper.resolveColor(context, R.attr.colorVerified);
            case MessageHelper.FLAG_NOT_DISPLAYED:
            case MessageHelper.FLAG_NOT_DELIVERED:
            case MessageHelper.FLAG_COMPLAINT:
            case MessageHelper.FLAG_PHISHING:
                return Helper.resolveColor(context, androidx.appcompat.R.attr.colorError);

            default:
                return null;
        }
    }
}
