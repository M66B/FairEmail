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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class PopupMenuLifecycle extends PopupMenu {

    public PopupMenuLifecycle(@NonNull Context context, LifecycleOwner owner, @NonNull View anchor) {
        super(new ContextThemeWrapper(context, R.style.popupMenuStyle), anchor, Gravity.NO_GRAVITY);
        Log.i("Instantiate " + this);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroy() {
                Log.i("Destroy " + this);
                PopupMenuLifecycle.this.dismiss();
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void insertIcons(Context context) {
        Context wrapped = new ContextThemeWrapper(context, R.style.popupMenuStyle);
        insertIcons(wrapped, getMenu(), false);
    }

    @Override
    public void show() {
        Log.i("Show " + this);
        try {
            super.show();
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    public void showWithIcons(Context context, View anchor) {
        Context wrapped = new ContextThemeWrapper(context, R.style.popupMenuStyle);
        MenuPopupHelper menuHelper = new MenuPopupHelper(wrapped, (MenuBuilder) getMenu(), anchor);
        menuHelper.setForceShowIcon(true);
        menuHelper.setGravity(Gravity.END);
        menuHelper.show();
    }

    @Override
    public void setOnMenuItemClickListener(@Nullable OnMenuItemClickListener listener) {
        super.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    // Handle click just before destroy
                    return listener.onMenuItemClick(item);
                } catch (Throwable ex) {
                    Log.w(ex);
                    return false;
                }
            }
        });
    }

    static void insertIcons(Context context, Menu menu, boolean submenu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            CharSequence title = item.getTitle();
            insertIcon(context, item, submenu);
            if (item.hasSubMenu()) {
                SubMenu sub = item.getSubMenu();
                boolean has = false;
                for (int j = 0; j < sub.size(); j++)
                    if (sub.getItem(j).getIcon() != null) {
                        has = true;
                        insertIcons(context, sub, true);
                        break;
                    }
                if (has)
                    sub.setHeaderTitle(title);
            }
        }
    }

    static void insertIcon(Context context, MenuItem menuItem, boolean submenu) {
        Drawable icon = menuItem.getIcon();
        if (icon == null)
            icon = new ColorDrawable(Color.TRANSPARENT);
        else {
            icon = icon.getConstantState().newDrawable().mutate();
            int color = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
            icon.setTint(color);
            if (!menuItem.isEnabled())
                icon.setAlpha(Math.round(Helper.LOW_LIGHT * 255));
        }

        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.menu_item_icon_size);
        icon.setBounds(0, 0, iconSize, iconSize);
        ImageSpan imageSpan = new CenteredImageSpan(icon);

        SpannableStringBuilder ssb = new SpannableStringBuilderEx(menuItem.getTitle());
        ssb.insert(0, "\uFFFC\u2002"); // object replacement character, en space
        ssb.setSpan(imageSpan, 0, 1, 0);
        menuItem.setTitle(ssb);
        if (submenu)
            menuItem.setIcon(null);
        menuItem.setTitleCondensed("");
    }
}
