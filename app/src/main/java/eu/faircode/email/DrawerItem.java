package eu.faircode.email;

import android.content.Context;

public class DrawerItem {
    private long id;
    private int menu;
    private int layout;
    private int icon;
    private Integer color;
    private int resid;
    private String title;
    private boolean highlight;

    DrawerItem(long id) {
        this.id = id;
        this.layout = R.layout.item_drawer_separator;
    }

    DrawerItem(long id, int icon, int resid) {
        this.id = id;
        this.menu = resid;
        this.layout = R.layout.item_drawer;
        this.icon = icon;
        this.resid = resid;
    }

    DrawerItem(long id, int menu, int icon, String title, boolean highlight) {
        this.id = id;
        this.menu = menu;
        this.layout = R.layout.item_drawer;
        this.icon = icon;
        this.title = title;
        this.highlight = highlight;
    }

    DrawerItem(long id, int icon, String title, Integer color, boolean highlight) {
        this.id = id;
        this.layout = R.layout.item_drawer;
        this.icon = icon;
        this.color = color;
        this.title = title;
        this.highlight = highlight;
    }

    int getLayout() {
        return this.layout;
    }

    long getId() {
        return this.id;
    }

    boolean isEnabled() {
        return (this.layout != R.layout.item_drawer_separator);
    }

    int getMenuId() {
        return this.menu;
    }

    int getIcon() {
        return this.icon;
    }

    Integer getColor() {
        return this.color;
    }

    String getTitle(Context context) {
        if (this.title == null && resid > 0)
            this.title = context.getString(resid);
        return this.title;
    }

    boolean getHighlight() {
        return this.highlight;
    }
}
