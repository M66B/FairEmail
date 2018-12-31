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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.OnLifecycleEvent;

//
// This simple task is simple to use, but it is also simple to cause bugs that can easily lead to crashes
// Make sure to not access any member in any outer scope from onExecute
// Results will not be delivered to destroyed fragments
//

public abstract class SimpleTask<T> implements LifecycleObserver {
    private LifecycleOwner owner;
    private boolean paused;
    private Bundle args;
    private Result stored;

    private static ExecutorService executor = Executors.newCachedThreadPool(Helper.backgroundThreadFactory);

    public void execute(Context context, LifecycleOwner owner, Bundle args) {
        run(context, owner, args);
    }

    public void execute(LifecycleService service, Bundle args) {
        run(service, service, args);
    }

    public void execute(AppCompatActivity activity, Bundle args) {
        run(activity, activity, args);
    }

    public void execute(final Fragment fragment, Bundle args) {
        try {
            run(fragment.getContext(), fragment.getViewLifecycleOwner(), args);
        } catch (IllegalStateException ex) {
            Log.w(ex);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        Log.i("Start task " + this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Log.i("Stop task " + this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        Log.i("Resume task " + this);
        paused = false;
        if (stored != null) {
            Log.i("Deferred delivery task " + this);
            deliver(args, stored);
            stored = null;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        Log.i("Pause task " + this);
        paused = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreated() {
        Log.i("Created task " + this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroyed() {
        Log.i("Destroy task " + this);
        owner.getLifecycle().removeObserver(this);
        owner = null;
        paused = true;
        args = null;
        stored = null;
    }

    private void run(final Context context, LifecycleOwner owner, final Bundle args) {
        this.owner = owner;
        this.paused = false;
        this.args = null;
        this.stored = null;

        try {
            onPreExecute(args);
        } catch (Throwable ex) {
            Log.e(ex);
        }

        owner.getLifecycle().addObserver(this);

        final Handler handler = new Handler();

        // Run in background thread
        executor.submit(new Runnable() {
            @Override
            public void run() {
                final Result result = new Result();

                try {
                    result.data = onExecute(context, args);
                } catch (Throwable ex) {
                    Log.e(ex);
                    result.ex = ex;
                }

                // Run on main thread
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        deliver(args, result);
                    }
                });
            }
        });
    }

    private void deliver(Bundle args, Result result) {
        if (paused) {
            Log.i("Deferring delivery task " + this);
            this.args = args;
            this.stored = result;
        } else {
            Log.i("Delivery task " + this);
            try {
                onPostExecute(args);
            } catch (Throwable ex) {
                Log.e(ex);
            } finally {
                try {
                    if (result.ex == null)
                        onExecuted(args, (T) result.data);
                    else
                        onException(args, result.ex);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
                onDestroyed();
            }
        }
    }

    protected void onPreExecute(Bundle args) {
    }

    protected abstract T onExecute(Context context, Bundle args) throws Throwable;

    protected void onExecuted(Bundle args, T data) {
    }

    protected abstract void onException(Bundle args, Throwable ex);

    protected void onPostExecute(Bundle args) {
    }

    private static class Result {
        Throwable ex;
        Object data;
    }
}
