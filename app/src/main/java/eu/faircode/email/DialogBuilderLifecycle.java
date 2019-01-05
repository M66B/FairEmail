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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class DialogBuilderLifecycle extends AlertDialog.Builder implements LifecycleObserver {
    private LifecycleOwner owner;
    private AlertDialog dialog;
    private CharSequence title = null;
    private CharSequence message = null;

    public DialogBuilderLifecycle(Context context, LifecycleOwner owner) {
        super(context);
        this.owner = owner;
    }

    public DialogBuilderLifecycle(Context context, int themeResId, LifecycleOwner owner) {
        super(context, themeResId);
        this.owner = owner;
    }

    @Override
    public AlertDialog.Builder setTitle(int titleId) {
        return setTitle(getContext().getString(titleId));
    }

    @Override
    public AlertDialog.Builder setTitle(@Nullable CharSequence title) {
        this.title = title;
        return this;
    }

    @Override
    public AlertDialog.Builder setMessage(int messageId) {
        return setMessage(getContext().getString(messageId));
    }

    @Override
    public AlertDialog.Builder setMessage(@Nullable CharSequence message) {
        this.message = message;
        return this;
    }

    @Override
    public AlertDialog create() {
        if (title == null && message != null) {
            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_message, null);
            TextView tvMessage = dview.findViewById(R.id.tvMessage);
            tvMessage.setText(message);
            setView(dview);
        } else {
            if (title != null)
                super.setTitle(title);
            if (message != null)
                super.setMessage(message);
        }
        dialog = super.create();
        owner.getLifecycle().addObserver(this);
        return dialog;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        dialog.dismiss();
        owner = null;
        dialog = null;
        title = null;
        message = null;
    }
}
