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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.GenericLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagedList;

public class BoundaryCallbackMessages extends PagedList.BoundaryCallback<TupleMessageEx> {
    private ViewModelBrowse model;
    private Handler handler;
    private IBoundaryCallbackMessages intf;
    private boolean searching = false;

    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    interface IBoundaryCallbackMessages {
        void onLoading();

        void onLoaded();

        void onError(Throwable ex);
    }

    BoundaryCallbackMessages(LifecycleOwner owner, ViewModelBrowse _model, IBoundaryCallbackMessages intf) {
        this.model = _model;
        this.handler = new Handler();
        this.intf = intf;

        owner.getLifecycle().addObserver(new GenericLifecycleObserver() {
            @Override
            public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY)
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            model.clear();
                            model = null;
                        }
                    });
            }
        });
    }

    boolean isSearching() {
        return searching;
    }

    @Override
    public void onZeroItemsLoaded() {
        Log.i("onZeroItemsLoaded");
        load();
    }

    @Override
    public void onItemAtEndLoaded(final TupleMessageEx itemAtEnd) {
        Log.i("onItemAtEndLoaded");
        load();
    }

    private void load() {
        if (model != null)
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        searching = model.isSearching();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                intf.onLoading();
                            }
                        });
                        model.load();
                    } catch (final Throwable ex) {
                        Log.e("Boundary", ex);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                intf.onError(ex);
                            }
                        });
                    } finally {
                        searching = false;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                intf.onLoaded();
                            }
                        });
                    }
                }
            });
    }
}