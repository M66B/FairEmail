/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.room;


import android.annotation.SuppressLint;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import eu.faircode.email.EntityLog;

/**
 * A LiveData implementation that closely works with {@link InvalidationTracker} to implement
 * database drive {@link androidx.lifecycle.LiveData} queries that are strongly hold as long
 * as they are active.
 * <p>
 * We need this extra handling for {@link androidx.lifecycle.LiveData} because when they are
 * observed forever, there is no {@link androidx.lifecycle.Lifecycle} that will keep them in
 * memory but they should stay. We cannot add-remove observer in {@link LiveData#onActive()},
 * {@link LiveData#onInactive()} because that would mean missing changes in between or doing an
 * extra query on every UI rotation.
 * <p>
 * This {@link LiveData} keeps a weak observer to the {@link InvalidationTracker} but it is hold
 * strongly by the {@link InvalidationTracker} as long as it is active.
 */
class RoomTrackingLiveData<T> extends LiveData<T> {
    @SuppressWarnings("WeakerAccess")
    final RoomDatabase mDatabase;

    @SuppressWarnings("WeakerAccess")
    final boolean mInTransaction;

    @SuppressWarnings("WeakerAccess")
    final Callable<T> mComputeFunction;

    private final InvalidationLiveDataContainer mContainer;

    @SuppressWarnings("WeakerAccess")
    final InvalidationTracker.Observer mObserver;

    final AtomicInteger queued = new AtomicInteger(0);
    final AtomicInteger running = new AtomicInteger(0);

    @SuppressWarnings("WeakerAccess")
    final AtomicBoolean mRegisteredObserver = new AtomicBoolean(false);

    @SuppressWarnings("WeakerAccess")
    final Runnable mRefreshRunnable = new Runnable() {
        @WorkerThread
        @Override
        public void run() {
            if (mRegisteredObserver.compareAndSet(false, true)) {
                mDatabase.getInvalidationTracker().addWeakObserver(mObserver);
            }
            try {
                running.incrementAndGet();

                T value = null;
                boolean computed = false;
                synchronized (mComputeFunction) {
                    int retry = 0;
                    while (!computed) {
                        try {
                            value = mComputeFunction.call();
                            computed = true;
                        } catch (Throwable e) {
                            if (++retry > 5) {
                                eu.faircode.email.Log.e(e);
                                break;
                            }
                            eu.faircode.email.Log.w(e);
                            try {
                                Thread.sleep(2000L);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }
                }
                if (computed) {
                    postValue(value);
                }
            } finally {
                queued.decrementAndGet();
                running.decrementAndGet();
            }
        }
    };

    @SuppressWarnings("WeakerAccess")
    final Runnable mInvalidationRunnable = new Runnable() {
        @MainThread
        @Override
        public void run() {
            if (running.get() == 0 && queued.get() > 0) {
                eu.faircode.email.Log.persist(EntityLog.Type.Debug,
                        mComputeFunction + " running=" + running + " queued=" + queued);
                return;
            }
            boolean isActive = hasActiveObservers();
            if (isActive) {
                queued.incrementAndGet();
                getQueryExecutor().execute(mRefreshRunnable);
            }
        }
    };

    @SuppressLint("RestrictedApi")
    RoomTrackingLiveData(
            RoomDatabase database,
            InvalidationLiveDataContainer container,
            boolean inTransaction,
            Callable<T> computeFunction,
            String[] tableNames) {
        mDatabase = database;
        mInTransaction = inTransaction;
        mComputeFunction = computeFunction;
        mContainer = container;
        mObserver = new InvalidationTracker.Observer(tableNames) {
            @Override
            public void onInvalidated(@NonNull Set<String> tables) {
                ArchTaskExecutor.getInstance().executeOnMainThread(mInvalidationRunnable);
            }
        };
    }

    @Override
    protected void onActive() {
        super.onActive();
        mContainer.onActive(this);
        getQueryExecutor().execute(mRefreshRunnable);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        mContainer.onInactive(this);
    }

    Executor getQueryExecutor() {
        if (mInTransaction) {
            return mDatabase.getTransactionExecutor();
        } else {
            return mDatabase.getQueryExecutor();
        }
    }
}
