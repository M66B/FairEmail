package eu.faircode.email;

import android.content.Context;

public class DrawerItem {
    private int layout;
    private int id;
    private int icon;
    private Integer color;
    private String title;
    private boolean highlight;
    private Object data;

    DrawerItem(int layout) {
        this.id = 0;
        this.layout = layout;
    }

    DrawerItem(Context context, int layout, int icon, int title) {
        this.layout = layout;
        this.id = title;
        this.icon = icon;
        this.title = context.getString(title);
    }

    DrawerItem(int layout, int id, int icon, Integer color, String title, boolean highlight, Object data) {
        this.layout = layout;
        this.id = id;
        this.icon = icon;
        this.color = color;
        this.title = title;
        this.highlight = highlight;
        this.data = data;
    }

    int getLayout() {
        return this.layout;
    }

    int getId() {
        return this.id;
    }

    int getIcon() {
        return this.icon;
    }

    Integer getColor() {
        return this.color;
    }

    String getTitle() {
        return this.title;
    }

    boolean getHighlight() {
        return this.highlight;
    }

    Object getData() {
        return this.data;
    }
}
