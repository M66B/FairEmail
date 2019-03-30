package eu.faircode.email;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.OnLifecycleEvent;

public class TwoStateOwner implements LifecycleOwner {
    private String name;
    private LifecycleRegistry registry;
    private Handler handler;

    // https://developer.android.com/topic/libraries/architecture/lifecycle#lc

    TwoStateOwner(String aname) {
        name = aname;

        // Initialize
        registry = new LifecycleRegistry(this);
        handler = new Handler(Looper.getMainLooper());
        transition(Lifecycle.State.CREATED);

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
        transition(Lifecycle.State.STARTED);
    }

    void stop() {
        transition(Lifecycle.State.CREATED);
    }

    void restart() {
        stop();
        start();
    }

    void destroy() {
        if (Looper.myLooper() == Looper.getMainLooper())
            _destroy();
        else
            handler.post(new Runnable() {
                @Override
                public void run() {
                    _destroy();
                }
            });
    }

    void _destroy() {
        Lifecycle.State state = registry.getCurrentState();
        if (!state.equals(Lifecycle.State.CREATED))
            registry.markState(Lifecycle.State.CREATED);
        if (!state.equals(Lifecycle.State.DESTROYED))
            registry.markState(Lifecycle.State.DESTROYED);
    }

    private void transition(final Lifecycle.State state) {
        if (Looper.myLooper() == Looper.getMainLooper())
            registry.markState(state);
        else
            handler.post(new Runnable() {
                @Override
                public void run() {
                    registry.markState(state);
                }
            });
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }
}