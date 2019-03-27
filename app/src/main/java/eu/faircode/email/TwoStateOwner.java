package eu.faircode.email;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.OnLifecycleEvent;

public class TwoStateOwner implements LifecycleOwner {
    private String name;
    private LifecycleRegistry registry;

    // https://developer.android.com/topic/libraries/architecture/lifecycle#lc

    TwoStateOwner(String aname) {
        name = aname;

        // Initialize registry
        registry = new LifecycleRegistry(this);
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        // Logging
        registry.addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
            public void onAny() {
                if (BuildConfig.DEBUG)
                    Log.i("LifeCycle " + name + " state=" + registry.getCurrentState());
            }
        });
    }

    TwoStateOwner(LifecycleOwner owner, String aname) {
        this(aname);

        // Destroy when parent destroyed
        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                if (BuildConfig.DEBUG)
                    Log.i("LifeCycle " + name + " parent destroyed");
                destroy();
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
        stop();
        start();
    }

    void destroy() {
        Lifecycle.State state = registry.getCurrentState();
        if (state.equals(Lifecycle.State.INITIALIZED))
            registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        if (state.equals(Lifecycle.State.STARTED))
            registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP); // transition to created
        if (!state.equals(Lifecycle.State.DESTROYED))
            registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }
}