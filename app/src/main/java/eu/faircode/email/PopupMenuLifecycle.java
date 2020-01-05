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
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class PopupMenuLifecycle extends PopupMenu implements LifecycleObserver {

    public PopupMenuLifecycle(@NonNull Context context, LifecycleOwner owner, @NonNull View anchor) {
        super(context, anchor);
        Log.i("Instantiate " + this);

        owner.getLifecycle().addObserver(this);
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

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        Log.i("Destroy " + this);
        this.dismiss();
    }
}
