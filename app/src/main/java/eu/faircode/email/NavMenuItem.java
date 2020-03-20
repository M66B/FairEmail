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

public class NavMenuItem {
    private int icon;
    private int title;
    private Integer count = null;
    private boolean external = false;
    private boolean warning = false;
    private boolean separated = false;
    private Runnable click;
    private Runnable longClick;

    NavMenuItem(int icon, int title, Runnable click) {
        this.icon = icon;
        this.title = title;
        this.click = click;
    }

    NavMenuItem(int icon, int title, Runnable click, Runnable longClick) {
        this.icon = icon;
        this.title = title;
        this.click = click;
        this.longClick = longClick;
    }

    void setCount(Integer count) {
        if (count != null && count == 0)
            count = null;
        this.count = count;
    }

    NavMenuItem setExternal(boolean external) {
        this.external = external;
        return this;
    }

    void setWarning(boolean warning) {
        this.warning = warning;
    }

    NavMenuItem setSeparated() {
        this.separated = true;
        return this;
    }

    int getIcon() {
        return this.icon;
    }

    int getTitle() {
        return this.title;
    }

    Integer getCount() {
        return this.count;
    }

    boolean isSeparated() {
        return this.separated;
    }

    boolean isExternal() {
        return this.external;
    }

    boolean hasWarning() {
        return this.warning;
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
            if (longClick != null)
                longClick.run();
            return (longClick != null);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }
}
