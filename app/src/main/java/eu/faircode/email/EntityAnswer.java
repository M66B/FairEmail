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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.NonNull;
import androidx.core.view.MenuCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

// https://developer.android.com/training/data-storage/room/defining-data

@Entity(
        tableName = EntityAnswer.TABLE_NAME,
        foreignKeys = {
        },
        indices = {
        }
)
public class EntityAnswer implements Serializable {
    static final String TABLE_NAME = "answer";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public String uuid = UUID.randomUUID().toString();
    @NonNull
    public String name;
    public String group;
    @NonNull
    public Boolean standard;
    @NonNull
    public Boolean receipt;
    @NonNull
    public Boolean favorite;
    @NonNull
    public Boolean snippet;
    @NonNull
    public Boolean hide;
    @NonNull
    public Boolean external;
    public Integer color;
    @NonNull
    public String text;
    @NonNull
    public Integer applied = 0;
    public Long last_applied;

    private static final String PREF_PLACEHOLDER = "answer.value.";

    String getHtml(Context context, Address[] address) {
        return replacePlaceholders(context, text, address);
    }

    static String replacePlaceholders(Context context, String text, Address[] address) {
        String fullName = null;
        String email = null;
        if (address != null && address.length > 0) {
            fullName = ((InternetAddress) address[0]).getPersonal();
            email = ((InternetAddress) address[0]).getAddress();
        }

        if (fullName != null) {
            fullName = fullName.trim();
            if (fullName.startsWith("\""))
                fullName = fullName.substring(1);
            if (fullName.endsWith("\""))
                fullName = fullName.substring(0, fullName.length() - 1);
        }

        String first = fullName;
        String last = null;
        if (fullName != null) {
            int c = fullName.lastIndexOf(',');
            if (c > 0) {
                last = fullName.substring(0, c).trim();
                first = fullName.substring(c + 1).trim();
            } else {
                c = fullName.indexOf(' ');
                if (c > 0) {
                    first = fullName.substring(0, c).trim();
                    last = fullName.substring(c + 1).trim();
                } else {
                    c = fullName.indexOf('@');
                    if (c > 0) {
                        first = fullName.substring(0, c).trim();
                        last = null;
                    }
                }
            }
        }

        if (fullName != null && !fullName.equals(first)) {
            String[] parts = first.split("\\.");
            if (parts != null && parts.length > 0) {
                boolean initials = true;
                for (String part : parts)
                    if (part.trim().length() > 1) {
                        initials = false;
                        break;
                    }
                if (initials)
                    first = null;
            }
        }

        if (first == null) {
            String username = UriHelper.getEmailUser(email);
            if (username != null) {
                int dot = username.indexOf('.');
                if (dot < 0)
                    first = username;
                else
                    first = username.substring(0, dot);
                if (first.length() > 0)
                    first = first.substring(0, 1).toUpperCase() + first.substring(1).toLowerCase();
            }
        }

        first = Helper.trim(first, ".");
        last = Helper.trim(last, ".");

        text = text.replace("$name$", fullName == null ? "" : Html.escapeHtml(fullName));
        text = text.replace("$firstname$", first == null ? "" : Html.escapeHtml(first));
        text = text.replace("$lastname$", last == null ? "" : Html.escapeHtml(last));
        text = text.replace("$email$", email == null ? "" : Html.escapeHtml(email));

        int s = text.indexOf("$date");
        while (s >= 0) {
            int e = text.indexOf('$', s + 5);
            if (e < 0)
                break;

            Calendar c = null;
            String v = text.substring(s + 5, e);
            if (v.startsWith("-") || v.startsWith("+")) {
                Integer days = Helper.parseInt(v.substring(1));
                if (days != null && days >= 0 && days < 10 * 365) {
                    c = Calendar.getInstance();
                    c.add(Calendar.DATE, days * (v.startsWith("-") ? -1 : 1));
                }
            } else if (TextUtils.isEmpty(v))
                c = Calendar.getInstance();

            if (c == null)
                s = text.indexOf("$date", e + 1);
            else {
                v = Html.escapeHtml(SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG).format(c.getTime()));
                text = text.substring(0, s) + v + text.substring(e + 1);
                s = text.indexOf("$date", s + v.length());
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (String key : prefs.getAll().keySet())
            if (key.startsWith(PREF_PLACEHOLDER)) {
                String name = key.substring(PREF_PLACEHOLDER.length());
                String value = prefs.getString(key, null);

                String[] lines = (value == null ? new String[0] : value.split("\n"));
                for (int i = 0; i < lines.length; i++)
                    lines[i] = Html.escapeHtml(lines[i]);

                text = text.replace("$" + name + "$", TextUtils.join("<br>", lines));
            }

        return text;
    }

    static void setCustomPlaceholder(Context context, String name, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (TextUtils.isEmpty(value))
            prefs.edit().remove(EntityAnswer.PREF_PLACEHOLDER + name).apply();
        else
            prefs.edit().putString(EntityAnswer.PREF_PLACEHOLDER + name, value).apply();
    }

    static String getCustomPlaceholder(Context context, String name) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(EntityAnswer.PREF_PLACEHOLDER + name, null);
    }

    static List<String> getCustomPlaceholders(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        List<String> names = new ArrayList<>();
        for (String key : prefs.getAll().keySet())
            if (key.startsWith(EntityAnswer.PREF_PLACEHOLDER))
                names.add(key.substring(EntityAnswer.PREF_PLACEHOLDER.length()));

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
        Collections.sort(names, new Comparator<String>() {
            @Override
            public int compare(String n1, String n2) {
                return collator.compare(n1, n2);
            }
        });

        return names;
    }

    static void fillMenu(Menu main, boolean compose, List<EntityAnswer> answers, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean sort_answers = prefs.getBoolean("sort_answers", false);

        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.menu_item_icon_size);
        NumberFormat NF = NumberFormat.getNumberInstance();

        List<EntityAnswer> favorites = new ArrayList<>();
        Map<String, Integer> groupApplied = new HashMap<>();
        for (EntityAnswer answer : answers)
            if (compose && answer.favorite)
                favorites.add(answer);
            else if (answer.group != null) {
                Integer total = groupApplied.get(answer.group);
                if (total == null)
                    total = 0;
                groupApplied.put(answer.group, total + answer.applied);
            }

        Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        List<String> groups = new ArrayList<>(groupApplied.keySet());
        Collections.sort(groups, new Comparator<String>() {
            @Override
            public int compare(String g1, String g2) {
                Integer a1 = groupApplied.get(g1);
                Integer a2 = groupApplied.get(g2);
                if (!sort_answers || a1.equals(a2))
                    return collator.compare(g1, g2);
                else
                    return -a1.compareTo(a2);
            }
        });

        Collections.sort(answers, new Comparator<EntityAnswer>() {
            @Override
            public int compare(EntityAnswer a1, EntityAnswer a2) {
                if (!sort_answers || a1.applied.equals(a2.applied))
                    return collator.compare(a1.name, a2.name);
                else
                    return -a1.applied.compareTo(a2.applied);
            }
        });

        Collections.sort(favorites, new Comparator<EntityAnswer>() {
            @Override
            public int compare(EntityAnswer a1, EntityAnswer a2) {
                return collator.compare(a1.name, a2.name);
            }
        });

        int order = 0;

        Map<String, SubMenu> map = new HashMap<>();
        for (String group : groups) {
            SpannableStringBuilder ssb = new SpannableStringBuilderEx(group);

            int total = groupApplied.get(group);
            if (sort_answers && total > 0) {
                int start = ssb.length();
                ssb.append(" (").append(NF.format(total)).append(")");
                ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL),
                        start, ssb.length(), 0);
            }

            map.put(group, main.addSubMenu(Menu.NONE, order, order++, ssb));
        }

        for (EntityAnswer answer : answers) {
            if (compose && answer.favorite)
                continue;
            order++;

            SpannableStringBuilder ssb = new SpannableStringBuilder(answer.name);

            if (answer.color != null) {
                Drawable d = new ColorDrawable(answer.color);
                d.setBounds(0, 0, iconSize / 4, iconSize);

                ImageSpan imageSpan = new CenteredImageSpan(d);
                ssb.insert(0, "\uFFFC\u2002"); // object replacement character, en space
                ssb.setSpan(imageSpan, 0, 1, 0);
            }

            if (sort_answers && answer.applied > 0) {
                int start = ssb.length();
                ssb.append(" (").append(NF.format(answer.applied)).append(")");
                ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL),
                        start, ssb.length(), 0);
            }

            MenuItem item;
            if (answer.group == null)
                item = main.add(Menu.NONE, order, order++, ssb);
            else {
                SubMenu smenu = map.get(answer.group);
                item = smenu.add(answer.applied > 0 ? Menu.FIRST : Menu.NONE,
                        smenu.size(), smenu.size() + 1, ssb);
            }
            item.setIntent(new Intent().putExtra("id", answer.id));
        }

        if (compose && BuildConfig.DEBUG) {
            SubMenu profiles = main.addSubMenu(Menu.NONE, order, order++, "Profiles");
            for (EmailProvider p : EmailProvider.loadProfiles(context)) {
                SpannableStringBuilder ssb = new SpannableStringBuilderEx();
                int start;
                ssb.append("IMAP (account, receive)");

                ssb.append(" host ");
                start = ssb.length();
                ssb.append(p.imap.host == null ? "?" : p.imap.host);
                ssb.setSpan(new StyleSpan(Typeface.BOLD),
                        start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                ssb.append(" port ");
                start = ssb.length();
                ssb.append(Integer.toString(p.imap.port));
                ssb.setSpan(new StyleSpan(Typeface.BOLD),
                        start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                ssb.append(" encryption ");
                start = ssb.length();
                ssb.append(p.imap.starttls ? "STARTTLS" : "SSL/TLS");
                ssb.setSpan(new StyleSpan(Typeface.BOLD),
                        start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                ssb.append("\n\n");

                ssb.append("SMTP (identity, send)");

                ssb.append(" host ");
                start = ssb.length();
                ssb.append(p.smtp.host == null ? "?" : p.smtp.host);
                ssb.setSpan(new StyleSpan(Typeface.BOLD),
                        start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                ssb.append(" port ");
                start = ssb.length();
                ssb.append(Integer.toString(p.smtp.port));
                ssb.setSpan(new StyleSpan(Typeface.BOLD),
                        start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                ssb.append(" encryption ");
                start = ssb.length();
                ssb.append(p.smtp.starttls ? "STARTTLS" : "SSL/TLS");
                ssb.setSpan(new StyleSpan(Typeface.BOLD),
                        start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                ssb.append("\n\n");

                if (p.appPassword)
                    ssb.append("App password\n\n");

                if (p.documentation !=
                        null)
                    ssb.append(HtmlHelper.fromHtml(p.documentation.toString(), context)).append("\n\n");

                if (!TextUtils.isEmpty(p.link))
                    ssb.append(p.link).append("\n\n");

                profiles.add(999, profiles.size(), profiles.size() + 1, p.name +
                                (p.appPassword ? "+" : ""))
                        .setIntent(new Intent().putExtra("config", ssb));
            }
        }

        Drawable icon = context.getResources().getDrawable(R.drawable.twotone_star_24);
        icon.setBounds(0, 0, iconSize, iconSize);
        icon = icon.getConstantState().newDrawable().mutate();
        int color = Helper.resolveColor(context, R.attr.colorAccent);
        icon.setTint(color);

        for (EntityAnswer answer : favorites) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(answer.name);

            if (answer.color != null) {
                Drawable d = new ColorDrawable(answer.color);
                d.setBounds(0, 0, iconSize / 4, iconSize);

                ImageSpan imageSpan = new CenteredImageSpan(d);
                ssb.insert(0, "\uFFFC\u2002"); // object replacement character, en space
                ssb.setSpan(imageSpan, 0, 1, 0);
            }

            ImageSpan imageSpan = new CenteredImageSpan(icon);
            ssb.insert(0, "\uFFFC\u2002"); // object replacement character, en space
            ssb.setSpan(imageSpan, 0, 1, 0);

            main.add(Menu.NONE, order, order++, ssb)
                    .setIntent(new Intent().putExtra("id", answer.id));
        }

        if (sort_answers)
            MenuCompat.setGroupDividerEnabled(main, true);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("uuid", uuid);
        json.put("name", name);
        json.put("group", group);
        json.put("standard", standard);
        json.put("receipt", receipt);
        json.put("favorite", favorite);
        json.put("snippet", snippet);
        json.put("hide", hide);
        json.put("external", external);
        json.put("color", color);
        json.put("text", text);
        json.put("applied", applied);
        json.put("last_applied", last_applied);
        return json;
    }

    public static EntityAnswer fromJSON(JSONObject json) throws JSONException {
        EntityAnswer answer = new EntityAnswer();
        answer.id = json.getLong("id");
        if (json.has("uuid"))
            answer.uuid = json.getString("uuid");
        answer.name = json.getString("name");
        answer.group = json.optString("group");
        if (TextUtils.isEmpty(answer.group))
            answer.group = null;
        answer.standard = json.optBoolean("standard");
        answer.receipt = json.optBoolean("receipt");
        answer.favorite = json.optBoolean("favorite");
        answer.snippet = json.optBoolean("snippet");
        answer.hide = json.optBoolean("hide");
        answer.external = json.optBoolean("external");
        if (json.has("color") && !json.isNull("color"))
            answer.color = json.getInt("color");
        answer.text = json.getString("text");
        answer.applied = json.optInt("applied", 0);
        if (json.has("last_applied") && !json.isNull("last_applied"))
            answer.last_applied = json.getLong("last_applied");
        return answer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAnswer) {
            EntityAnswer other = (EntityAnswer) obj;
            return (Objects.equals(this.uuid, other.uuid) &&
                    this.name.equals(other.name) &&
                    Objects.equals(this.group, other.group) &&
                    this.standard.equals(other.standard) &&
                    this.receipt.equals(other.receipt) &&
                    this.favorite.equals(other.favorite) &&
                    this.snippet.equals(other.snippet) &&
                    this.hide.equals(other.hide) &&
                    this.external.equals(other.external) &&
                    this.text.equals(other.text) &&
                    Objects.equals(this.color, other.color) &&
                    this.applied.equals(other.applied) &&
                    Objects.equals(this.last_applied, other.last_applied));
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return name + (favorite ? " ★" : "");
    }
}
