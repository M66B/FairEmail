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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

// This simple task is simple to use, but it is also simple to cause bugs that can easily lead to crashes
// Make sure to not access any member in any outer scope from onExecute
// Results will not be delivered to destroyed fragments

public abstract class SimpleTask<T> implements LifecycleObserver {
    private boolean log = true;
    private boolean count = true;
    private boolean keepawake = false;

    private String id;
    private String name;
    private long started;
    private boolean destroyed;
    private boolean reported;
    private Lifecycle.State state;
    private Future<?> future;
    private ExecutorService localExecutor;
    private Handler handler = null;

    private static PowerManager.WakeLock wl = null;
    private static final List<SimpleTask> tasks = new ArrayList<>();

    private static final ExecutorService serialExecutor =
            Helper.getBackgroundExecutor(1, "tasks/serial");

    private static final ExecutorService globalExecutor =
            Helper.getBackgroundExecutor(0, "tasks/global");

    private static final int REPORT_AFTER = 15 * 60 * 1000; // milliseconds

    static final String ACTION_TASK_COUNT = BuildConfig.APPLICATION_ID + ".ACTION_TASK_COUNT";

    @NonNull
    public SimpleTask<T> setId(String id) {
        this.id = id;
        return this;
    }

    @NonNull
    public SimpleTask<T> setLog(boolean log) {
        this.log = log;
        if (!log)
            this.count = false;
        return this;
    }

    @NonNull
    public SimpleTask<T> setCount(boolean count) {
        this.count = count;
        return this;
    }

    @NonNull
    public SimpleTask<T> setKeepAwake(boolean value) {
        this.keepawake = value;
        return this;
    }

    @NonNull
    public SimpleTask<T> setExecutor(ExecutorService executor) {
        this.localExecutor = executor;
        return this;
    }

    public SimpleTask<T> serial() {
        return setExecutor(serialExecutor);
    }

    @NonNull
    public SimpleTask<T> setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    @NonNull
    private ExecutorService getExecutor(Context context) {
        if (wl == null) {
            PowerManager pm = Helper.getSystemService(context, PowerManager.class);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":task");
        }

        if (localExecutor != null)
            return localExecutor;

        return globalExecutor;
    }

    @NonNull
    private Handler getHandler() {
        return (handler == null ? ApplicationEx.getMainHandler() : handler);
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
        this.started = new Date().getTime();

        if (Log.isTestRelease())
            Log.breadcrumb("SimpleTask", args);

        for (String key : args.keySet()) {
            Object value = args.get(key);
            if (value instanceof Spanned)
                args.putCharSequence(key, new SpannableStringBuilderEx((Spanned) value));
        }

        if (owner instanceof TwoStateOwner)
            Log.e(new Throwable("SimpleTask/TwoStateOwner"));

        // prevent garbage collection
        synchronized (tasks) {
            if (id != null)
                for (SimpleTask task : new ArrayList<>(tasks))
                    if (id.equals(task.id))
                        task.cancel(context);
            tasks.add(this);
        }

        try {
            onPreExecute(args);
        } catch (Throwable ex) {
            Log.e(ex);
            try {
                onException(args, ex);
            } catch (Throwable exex) {
                Log.e(exex);
            }
        }

        LifecycleObserver watcher = new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroy() {
                EntityLog.log(context, EntityLog.Type.Debug1, "Owner gone task=" + name);
                destroyed = true;
                onDestroyed(args);
                owner.getLifecycle().removeObserver(this);
            }
        };

        Context tcontext;
        if (context instanceof ActivityBase) {
            int themeId = ((ActivityBase) context).getThemeId();
            if (themeId == 0)
                themeId = context.getApplicationInfo().theme;
            tcontext = ApplicationEx.getThemedContext(context, themeId);
        } else
            tcontext = context.getApplicationContext();

        future = getExecutor(context).submit(new Runnable() {
            private Object data;
            private long elapsed;
            private Throwable error;

            @Override
            public void run() {
                // Run in background thread
                long start = new Date().getTime();
                try {
                    if (keepawake)
                        wl.acquire();
                    else
                        wl.acquire(Helper.WAKELOCK_MAX);

                    if (log)
                        Log.i("Executing task=" + name);
                    data = onExecute(tcontext, args);
                    elapsed = new Date().getTime() - start;
                    if (log)
                        Log.i("Executed task=" + name + " elapsed=" + elapsed + " ms");
                } catch (Throwable ex) {
                    if (ex instanceof IllegalArgumentException)
                        Log.i(ex);
                    else
                        Log.e(ex);
                    error = ex;
                } finally {
                    if (wl.isHeld())
                        wl.release();
                    else if (!keepawake &&
                            !BuildConfig.PLAY_STORE_RELEASE &&
                            !Boolean.FALSE.equals(Helper.isIgnoringOptimizations(tcontext)))
                        Log.e(name + " released elapse=" + (new Date().getTime() - start));
                }

                // Run on UI thread
                ApplicationEx.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        state = owner.getLifecycle().getCurrentState();
                        if (state.equals(Lifecycle.State.DESTROYED)) {
                            // No delivery
                            cleanup(context);
                        } else {
                            owner.getLifecycle().removeObserver(watcher);

                            if (state.isAtLeast(Lifecycle.State.RESUMED)) {
                                // Inline delivery
                                Log.i("Deliver task " + name + " state=" + state + " elapse=" + elapsed + " ms");
                                deliver();
                                cleanup(context);
                            } else {
                                Log.i("Deferring task " + name + " state=" + state);
                                owner.getLifecycle().addObserver(new LifecycleObserver() {
                                    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
                                    public void onAny() {
                                        state = owner.getLifecycle().getCurrentState();
                                        if (state.equals(Lifecycle.State.DESTROYED)) {
                                            Log.i("Destroyed task " + name);
                                            onDestroyed(args);
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
                    }

                    private void deliver() {
                        if ("androidx.fragment.app.FragmentViewLifecycleOwner".equals(owner.getClass().getName()))
                            try {
                                Field mFragment = owner.getClass().getDeclaredField("mFragment");
                                mFragment.setAccessible(true);
                                Fragment fragment = (Fragment) mFragment.get(owner);
                                if (fragment != null &&
                                        (fragment.getContext() == null || fragment.getActivity() == null)) {
                                    // Since deliver is executed for resumed fragments only, this should never happen
                                    Log.e("Fragment without activity" +
                                            " task=" + name +
                                            " context=" + (fragment.getContext() != null) +
                                            " activity=" + (fragment.getActivity() != null) +
                                            " fragment=" + fragment.getClass().getName() +
                                            " lifecycle=" + owner.getLifecycle().getCurrentState());
                                    return;
                                }
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }
                        try {
                            onPostExecute(args);
                        } catch (Throwable ex) {
                            Log.e(ex);
                            try {
                                onException(args, ex);
                            } catch (Throwable exex) {
                                Log.e(exex);
                            }
                        } finally {
                            try {
                                if (error == null) {
                                    if (log && BuildConfig.BETA_RELEASE) {
                                        Log.i("Crumb " + name);
                                        Map<String, String> crumb = new HashMap<>();
                                        crumb.put("name", name);
                                        Log.breadcrumb("task", crumb);
                                    }

                                    onExecuted(args, (T) data);
                                } else
                                    try {
                                        onException(args, error);
                                    } catch (Throwable exex) {
                                        Log.e(exex);
                                    }
                            } catch (Throwable ex) {
                                Log.e(ex);
                                try {
                                    onException(args, ex);
                                } catch (Throwable exex) {
                                    Log.e(exex);
                                }
                            }
                        }
                    }
                });
            }
        });

        owner.getLifecycle().addObserver(watcher);

        updateTaskCount(context);
    }

    public boolean isAlive() {
        return !this.destroyed;
    }

    void cancel(Context context) {
        try {
            ExecutorService executor = getExecutor(context);
            if (executor instanceof ThreadPoolExecutor && future instanceof Runnable) {
                boolean removed = ((ThreadPoolExecutor) executor).remove((Runnable) future);
                Log.i("Remove task=" + name + " removed=" + removed);
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        if (future != null && future.cancel(false)) {
            Log.i("Cancelled task=" + name);
            cleanup(context);
        }
    }

    private void cleanup(Context context) {
        started = 0;
        reported = false;
        future = null;
        synchronized (tasks) {
            tasks.remove(this);
        }

        updateTaskCount(context);
    }

    private void updateTaskCount(Context context) {
        // Check tasks
        long now = new Date().getTime();
        synchronized (tasks) {
            for (SimpleTask task : tasks)
                if (task.future != null && !task.future.isDone()) {
                    long elapsed = now - task.started;
                    if (elapsed > REPORT_AFTER && !task.reported) {
                        task.reported = true;
                        Log.e("Long running task " + task +
                                " tasks=" + getCountLocked() + "/" + tasks.size());
                    }
                }
        }

        int executing = getCount();
        Log.i("Remaining tasks=" + executing + "/" + tasks.size());
        if (context == null)
            Log.e("Context is null");
        else {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(new Intent(ACTION_TASK_COUNT).putExtra("count", executing));
        }
    }

    protected void onPreExecute(Bundle args) {
    }

    protected abstract T onExecute(Context context, Bundle args) throws Throwable;

    protected void postProgress(CharSequence status) {
        postProgress(status, null);
    }

    protected void postProgress(CharSequence status, Bundle data) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!destroyed)
                        onProgress(status, data);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    protected void onProgress(CharSequence status, Bundle data) {
    }

    protected void onExecuted(Bundle args, T data) {
    }

    protected abstract void onException(Bundle args, Throwable ex);

    protected void onPostExecute(Bundle args) {
    }

    protected void onDestroyed(Bundle args) {
    }

    @Override
    public String toString() {
        long now = new Date().getTime();
        long elapsed = now - started;
        return name +
                " elapsed=" + (started == 0 ? null : elapsed / 1000) +
                " done=" + (future == null ? null : future.isDone()) +
                " cancelled=" + (future == null ? null : future.isCancelled() +
                " state=" + state);
    }

    static int getCount() {
        synchronized (tasks) {
            return getCountLocked();
        }
    }

    private static int getCountLocked() {
        int executing = 0;
        for (SimpleTask task : tasks)
            if (task.count &&
                    task.future != null && !task.future.isDone())
                executing++;
        return executing;
    }

    static List<SimpleTask> getList() {
        synchronized (tasks) {
            return new ArrayList<>(tasks);
        }
    }
}
