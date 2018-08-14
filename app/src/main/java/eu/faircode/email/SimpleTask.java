package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.OnLifecycleEvent;

//
// This simple task is simple to use, but it is also simple to cause bugs that can easily lead to crashes
// Make sure to not access any member in any outer scope from onLoad
//

public abstract class SimpleTask<T> implements LifecycleObserver {
    private boolean paused = false;
    private Bundle args = null;
    private Result stored = null;

    public void load(Context context, LifecycleOwner owner, Bundle args) {
        run(context, owner, args);
    }

    public void load(LifecycleService service, Bundle args) {
        run(service, service, args);
    }

    public void load(AppCompatActivity activity, Bundle args) {
        run(activity, activity, args);
    }

    public void load(Fragment fragment, Bundle args) {
        run(fragment.getContext(), fragment.getViewLifecycleOwner(), args);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        paused = false;
        if (stored != null) {
            deliver(args, stored);
            stored = null;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        paused = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroyed() {
        paused = true;
        args = null;
        stored = null;
    }

    private void run(final Context context, LifecycleOwner owner, final Bundle args) {
        owner.getLifecycle().addObserver(this);

        new AsyncTask<Object, Object, Result>() {
            @Override
            protected Result doInBackground(Object... params) {
                Result result = new Result();
                try {
                    result.data = onLoad(context, args);
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    result.ex = ex;
                }
                return result;
            }

            @Override
            protected void onPostExecute(Result result) {
                deliver(args, result);
            }
        }.execute(args);
    }

    private void deliver(Bundle args, Result result) {
        if (paused) {
            this.args = args;
            this.stored = result;
        } else {
            if (result.ex == null)
                onLoaded(args, (T) result.data);
            else
                onException(args, result.ex);
        }
    }

    protected T onLoad(Context context, Bundle args) throws Throwable {
        // Be careful not to access members in outer scopes
        return null;
    }

    protected void onLoaded(Bundle args, T data) {
    }

    protected void onException(Bundle args, Throwable ex) {
    }

    private static class Result {
        Throwable ex;
        Object data;
    }
}
