package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

//
// This simple loader is simple to use, but it is also simple to cause bugs that can easily lead to crashes
// Make sure to not access any member in any outer scope from onLoad
//

public abstract class SimpleLoader<T> {
    private Context context;
    private LoaderManager manager;

    public void load(AppCompatActivity activity, int id, Bundle args) {
        this.context = activity;
        this.manager = LoaderManager.getInstance(activity);
        manager.restartLoader(id, args, callbacks).forceLoad();
    }

    public void load(Fragment fragment, int id, Bundle args) {
        this.context = fragment.getContext();
        this.manager = LoaderManager.getInstance(fragment);
        manager.restartLoader(id, args, callbacks).forceLoad();
    }

    public T onLoad(Bundle args) throws Throwable {
        // Be careful not to access members in outer scopes
        return null;
    }

    public void onLoaded(Bundle args, T data) {
    }

    public void onException(Bundle args, Throwable ex) {
    }

    protected Context getContext() {
        return context;
    }

    private static class CommonLoader extends AsyncTaskLoader<Result> {
        boolean loading = false;
        Bundle args;
        SimpleLoader loader;

        CommonLoader(Context context) {
            super(context);
        }

        void setArgs(Bundle args, SimpleLoader x) {
            this.args = args;
            this.loader = x;
        }

        protected void onStartLoading() {
            if (!loading)
                forceLoad();
        }

        @Override
        public Result loadInBackground() {
            loading = true;
            Result result = new Result();
            try {
                result.data = loader.onLoad(args);
            } catch (Throwable ex) {
                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                result.ex = ex;
            }
            return result;
        }
    }

    private LoaderManager.LoaderCallbacks callbacks = new LoaderManager.LoaderCallbacks<Result>() {
        @Override
        public Loader<Result> onCreateLoader(int id, Bundle args) {
            CommonLoader loader = new CommonLoader(context);
            loader.setArgs(args, SimpleLoader.this);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Result> loader, Result data) {
            manager.destroyLoader(loader.getId());

            CommonLoader common = (CommonLoader) loader;
            if (data.ex == null)
                onLoaded(common.args, (T) data.data);
            else
                onException(common.args, data.ex);

            common.args = null;
            common.loader = null;

            manager = null;
        }

        @Override
        public void onLoaderReset(Loader<Result> loader) {
        }
    };

    private static class Result {
        Throwable ex;
        Object data;
    }
}
