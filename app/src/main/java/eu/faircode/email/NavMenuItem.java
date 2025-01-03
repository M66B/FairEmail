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

import java.util.Objects;
import java.util.concurrent.Callable;

public class NavMenuItem {
    private int icon;
    private Integer color;
    private int title;
    private String subtitle = null;
    private int extra_icon;
    private Integer count = null;
    private boolean warning = false;
    private boolean separated = false;
    private Runnable click;
    private Callable<Boolean> longClick;

    NavMenuItem(int icon, int title, Runnable click) {
        this.icon = icon;
        this.title = title;
        this.click = click;
    }

    NavMenuItem(int icon, int title, Runnable click, Callable<Boolean> longClick) {
        this.icon = icon;
        this.title = title;
        this.click = click;
        this.longClick = longClick;
    }

    NavMenuItem setColor(Integer color) {
        this.color = color;
        return this;
    }

    NavMenuItem setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    NavMenuItem setExtraIcon(int icon) {
        this.extra_icon = icon;
        return this;
    }

    void setCount(Integer count) {
        if (count != null && count == 0)
            count = null;
        this.count = count;
    }

    NavMenuItem setExternal(boolean external) {
        setExtraIcon(external ? R.drawable.twotone_open_in_new_24 : 0);
        return this;
    }

    NavMenuItem setWarning(boolean warning) {
        this.warning = warning;
        return this;
    }

    NavMenuItem setSeparated() {
        this.separated = true;
        return this;
    }

    int getIcon() {
        return this.icon;
    }

    Integer getColor() {
        return this.color;
    }

    int getTitle() {
        return this.title;
    }

    String getSubtitle() {
        return this.subtitle;
    }

    int getExtraIcon() {
        return this.extra_icon;
    }

    Integer getCount() {
        return this.count;
    }

    boolean hasWarning() {
        return this.warning;
    }

    boolean isSeparated() {
        return this.separated;
    }

    void onClick() {
        try {
            click.run();
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    boolean onLongClick() {
        try {
            return (longClick != null && longClick.call());
        } catch (Throwable ex) {
            Log.e(ex);
        }
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof NavMenuItem) {
            NavMenuItem other = (NavMenuItem) object;
            return (this.icon == other.icon &&
                    Objects.equals(this.color, other.color) &&
                    this.title == other.title &&
                    Objects.equals(this.subtitle, other.subtitle) &&
                    this.extra_icon == other.extra_icon &&
                    Objects.equals(this.count, other.count) &&
                    this.warning == other.warning &&
                    this.separated == other.separated);
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(icon, color, title, subtitle, extra_icon, count, warning, separated);
    }
}
