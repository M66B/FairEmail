package eu.faircode.email;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.android.colorpicker.ColorPickerDialog;

public class ColorPickerDialogEx extends ColorPickerDialog implements LifecycleObserver {
    private LifecycleOwner owner;

    private ColorPickerDialogEx() {
    }

    ColorPickerDialogEx(LifecycleOwner owner) {
        super();
        this.owner = owner;
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        owner.getLifecycle().addObserver(this);
        super.show(manager, tag);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onOwnerPause() {
        dismiss();
        this.owner = null;
    }
}
