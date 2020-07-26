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

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.OnLifecycleEvent;

// This class can be used as an externally controlled standalone or child life cycle owner

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
                Log.d("LifeCycle " + name + " parent destroyed");
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
                Log.d("LifeCycle " + name + " state=" + registry.getCurrentState() + " " + registry);
            }
        });

        setState(Lifecycle.State.CREATED);
    }

    void start() {
        Lifecycle.State state = registry.getCurrentState();
        if (!state.equals(Lifecycle.State.STARTED) && !state.equals(Lifecycle.State.DESTROYED))
            setState(Lifecycle.State.STARTED);
    }

    void stop() {
        Lifecycle.State state = registry.getCurrentState();
        if (!state.equals(Lifecycle.State.CREATED) && !state.equals(Lifecycle.State.DESTROYED))
            setState(Lifecycle.State.CREATED);
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
        if (!state.equals(Lifecycle.State.DESTROYED)) {
            if (!state.equals(Lifecycle.State.CREATED))
                setState(Lifecycle.State.CREATED);
            setState(Lifecycle.State.DESTROYED);
        }
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }

    private void setState(@NonNull Lifecycle.State state) {
        try {
            registry.setCurrentState(state);
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.RuntimeException: Failed to call observer method
                  at androidx.lifecycle.ClassesInfoCache$MethodReference.invokeCallback(SourceFile:226)
                  at androidx.lifecycle.ClassesInfoCache$CallbackInfo.invokeMethodsForEvent(SourceFile:194)
                  at androidx.lifecycle.ClassesInfoCache$CallbackInfo.invokeCallbacks(SourceFile:186)
                  at androidx.lifecycle.ReflectiveGenericLifecycleObserver.onStateChanged(SourceFile:37)
                  at androidx.lifecycle.LifecycleRegistry$ObserverWithState.dispatchEvent(SourceFile:361)
                  at androidx.lifecycle.LifecycleRegistry.backwardPass(SourceFile:316)
                  at androidx.lifecycle.LifecycleRegistry.sync(SourceFile:334)
                  at androidx.lifecycle.LifecycleRegistry.moveToState(SourceFile:145)
                  at androidx.lifecycle.LifecycleRegistry.setCurrentState(SourceFile:118)
             */
        }
    }
}