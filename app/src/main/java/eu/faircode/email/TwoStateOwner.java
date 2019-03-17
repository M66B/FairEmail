package eu.faircode.email;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.OnLifecycleEvent;

public class TwoStateOwner implements LifecycleOwner {
    private LifecycleRegistry registry;

    TwoStateOwner() {
        registry = new LifecycleRegistry(this);
    }

    TwoStateOwner(LifecycleOwner owner) {
        this();
        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
            }
        });
    }

    void start() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START);
    }

    void stop() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }

    void restart() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        start();
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }
}