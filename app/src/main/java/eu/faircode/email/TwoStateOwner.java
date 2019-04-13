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
        create();
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

    private void create() {
        // Initialize
        registry = new LifecycleRegistry(this);
        registry.addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
            public void onAny() {
                if (BuildConfig.DEBUG)
                    Log.i("LifeCycle " + name + " state=" + registry.getCurrentState() + " " + registry);
            }
        });

        registry.markState(Lifecycle.State.CREATED);
    }

    void start() {
        registry.markState(Lifecycle.State.STARTED);
    }

    void stop() {
        registry.markState(Lifecycle.State.CREATED);
    }

    void restart() {
        stop();
        start();
    }

    void recreate() {
        destroy();
        create();
    }

    void destroy() {
        Lifecycle.State state = registry.getCurrentState();
        if (!state.equals(Lifecycle.State.CREATED))
            registry.markState(Lifecycle.State.CREATED);
        if (!state.equals(Lifecycle.State.DESTROYED))
            registry.markState(Lifecycle.State.DESTROYED);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }
}