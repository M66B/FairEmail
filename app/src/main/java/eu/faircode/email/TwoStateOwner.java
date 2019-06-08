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
                //if (BuildConfig.DEBUG)
                //    Log.i("LifeCycle " + name + " state=" + registry.getCurrentState() + " " + registry);
            }
        });

        registry.setCurrentState(Lifecycle.State.CREATED);
    }

    void start() {
        registry.setCurrentState(Lifecycle.State.STARTED);
    }

    void stop() {
        registry.setCurrentState(Lifecycle.State.CREATED);
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
            registry.setCurrentState(Lifecycle.State.CREATED);
        if (!state.equals(Lifecycle.State.DESTROYED))
            registry.setCurrentState(Lifecycle.State.DESTROYED);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }
}