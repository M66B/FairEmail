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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

// This simple task is simple to use, but it is also simple to cause bugs that can easily lead to crashes
// Make sure to not access any member in any outer scope from onExecute
// Results will not be delivered to destroyed fragments

public abstract class SimpleTask<T> implements LifecycleObserver {
    private boolean log = true;
    private boolean count = true;
    private int executing = 0;

    private String name;
    private Future<?> future;

    private static ExecutorService executor = null;
    private static final List<SimpleTask> tasks = new ArrayList<>();

    static final String ACTION_TASK_COUNT = BuildConfig.APPLICATION_ID + ".ACTION_TASK_COUNT";

    public SimpleTask<T> setLog(boolean log) {
        this.log = log;
        return this;
    }

    public SimpleTask<T> setCount(boolean count) {
        this.count = count;
        return this;
    }

    public void execute(Context context, LifecycleOwner owner, @NonNull Bundle args, @NonNull String name) {
        run(context, owner, args, name);
    }

    public void execute(LifecycleService service, @NonNull Bundle args, @NonNull String name) {
        run(service, service, args, name);
    }

    public void execute(AppCompatActivity activity, @NonNull Bundle args, @NonNull String name) {
        run(activity, activity, args, name);
    }

    public void execute(final Fragment fragment, @NonNull Bundle args, @NonNull String name) {
        try {
            if (fragment.getView() != null || fragment instanceof FragmentDialogBase)
                run(fragment.getContext(), fragment.getViewLifecycleOwner(), args, name);
        } catch (IllegalStateException ex) {
            Log.e(ex);
        }
    }

    private void run(final Context context, final LifecycleOwner owner, final Bundle args, final String name) {
        this.name = name;

        if (owner instanceof TwoStateOwner)
            Log.e(new Throwable("SimpleTask/TwoStateOwner"));

        // prevent garbage collection
        synchronized (tasks) {
            tasks.add(this);
            if (count)
                executing++;
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.sendBroadcast(new Intent(ACTION_TASK_COUNT).putExtra("count", executing));

        try {
            onPreExecute(args);
        } catch (Throwable ex) {
            Log.e(ex);
            onException(args, ex);
        }

        if (executor == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int threads = prefs.getInt("query_threads", Runtime.getRuntime().availableProcessors());
            Log.i("Task threads=" + threads);
            executor = Helper.getBackgroundExecutor(threads, "task");
        }

        future = executor.submit(new Runnable() {
            private Object data;
            private long elapsed;
            private Throwable ex;

            @Override
            public void run() {
                // Run in background thread
                try {
                    if (log)
                        Log.i("Executing task=" + name);
                    long start = new Date().getTime();
                    data = onExecute(context, args);
                    elapsed = new Date().getTime() - start;
                    if (log)
                        Log.i("Executed task=" + name + " elapsed=" + elapsed + " ms");
                } catch (Throwable ex) {
                    if (!(ex instanceof IllegalArgumentException))
                        Log.e(ex);
                    this.ex = ex;
                }

                // Run on UI thread
                ApplicationEx.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Lifecycle.State state = owner.getLifecycle().getCurrentState();
                        if (state.equals(Lifecycle.State.DESTROYED)) {
                            // No delivery
                            cleanup(context);
                        } else if (state.isAtLeast(Lifecycle.State.RESUMED)) {
                            // Inline delivery
                            Log.i("Deliver task " + name + " state=" + state + " elapse=" + elapsed + " ms");
                            deliver();
                            cleanup(context);
                        } else {
                            Log.i("Deferring task " + name + " state=" + state);
                            owner.getLifecycle().addObserver(new LifecycleObserver() {
                                @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
                                public void onAny() {
                                    Lifecycle.State state = owner.getLifecycle().getCurrentState();
                                    if (state.equals(Lifecycle.State.DESTROYED)) {
                                        Log.i("Destroyed task " + name);
                                        owner.getLifecycle().removeObserver(this);
                                        cleanup(context);
                                    } else if (state.isAtLeast(Lifecycle.State.RESUMED)) {
                                        Log.i("Deferred delivery task " + name);
                                        owner.getLifecycle().removeObserver(this);
                                        deliver();
                                        cleanup(context);
                                    } else
                                        Log.i("Deferring task " + name + " state=" + state);
                                }
                            });
                        }
                    }

                    private void deliver() {
                        try {
                            onPostExecute(args);
                        } catch (Throwable ex) {
                            Log.e(ex);
                            onException(args, ex);
                        } finally {
                            try {
                                if (ex == null) {
                                    if (log && BuildConfig.BETA_RELEASE) {
                                        Log.i("Crumb " + name);
                                        Map<String, String> crumb = new HashMap<>();
                                        crumb.put("name", name);
                                        Log.breadcrumb("task", crumb);
                                    }

                                    onExecuted(args, (T) data);
                                } else
                                    onException(args, ex);
                            } catch (Throwable ex) {
                                Log.e(ex);
                                onException(args, ex);
                            }
                        }
                    }
                });
            }
        });
    }

    void cancel(Context context) {
        if (future != null)
            if (future.cancel(false)) {
                Log.i("Cancelled task=" + name);
                cleanup(context);
            }
    }

    private void cleanup(Context context) {
        future = null;
        synchronized (tasks) {
            tasks.remove(this);
            if (count)
                executing--;
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.sendBroadcast(new Intent(ACTION_TASK_COUNT).putExtra("count", executing));
        Log.i("Remaining tasks=" + tasks.size());
    }

    protected void onPreExecute(Bundle args) {
    }

    protected abstract T onExecute(Context context, Bundle args) throws Throwable;

    protected void onExecuted(Bundle args, T data) {
    }

    protected abstract void onException(Bundle args, Throwable ex);

    protected void onPostExecute(Bundle args) {
    }

    static int getCount() {
        return tasks.size();
    }
}
