package eu.faircode.email;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

public class TwoStateOwner implements LifecycleOwner {
    private LifecycleRegistry registry;

    TwoStateOwner() {
        registry = new LifecycleRegistry(this);
    }

    void start() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START);
    }

    void stop() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }
}