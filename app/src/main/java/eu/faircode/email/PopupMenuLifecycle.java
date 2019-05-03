package eu.faircode.email;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class PopupMenuLifecycle extends PopupMenu implements LifecycleObserver {
    private LifecycleOwner owner;

    public PopupMenuLifecycle(@NonNull Context context, LifecycleOwner owner, @NonNull View anchor) {
        super(context, anchor);
        this.owner = owner;
    }

    @Override
    public void show() {
        super.show();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        this.dismiss();
        owner = null;
    }
}
