package eu.faircode.email;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

public abstract class SimpleLoader {
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

    public Object onLoad(Bundle args) {
        return null;
    }

    public void onLoaded(Bundle args, Result result) {
    }

    private static class CommonLoader extends AsyncTaskLoader<Result> {
        Bundle args;
        SimpleLoader loader;

        CommonLoader(Context context) {
            super(context);
        }

        void setArgs(Bundle args, SimpleLoader x) {
            this.args = args;
            this.loader = x;
        }

        @Override
        public Result loadInBackground() {
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
            onLoaded(common.args, data);

            common.args = null;
            common.loader = null;

            manager = null;
        }

        @Override
        public void onLoaderReset(Loader<Result> loader) {
        }
    };

    public static class Result {
        Throwable ex;
        Object data;
    }
}
