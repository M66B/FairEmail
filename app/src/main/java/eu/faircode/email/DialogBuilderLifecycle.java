package eu.faircode.email;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class DialogBuilderLifecycle extends AlertDialog.Builder implements LifecycleObserver {
    private LifecycleOwner owner;
    private AlertDialog dialog;

    public DialogBuilderLifecycle(Context context, LifecycleOwner owner) {
        super(context);
        this.owner = owner;
    }

    public DialogBuilderLifecycle(Context context, int themeResId, LifecycleOwner owner) {
        super(context, themeResId);
        this.owner = owner;
    }

    @Override
    public AlertDialog create() {
        dialog = super.create();
        owner.getLifecycle().addObserver(this);
        return dialog;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        dialog.dismiss();
        owner = null;
        dialog = null;
    }
}
