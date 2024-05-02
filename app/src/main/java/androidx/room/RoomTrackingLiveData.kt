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
package androidx.room

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.LiveData
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A LiveData implementation that closely works with [InvalidationTracker] to implement
 * database drive [androidx.lifecycle.LiveData] queries that are strongly hold as long
 * as they are active.
 *
 * We need this extra handling for [androidx.lifecycle.LiveData] because when they are
 * observed forever, there is no [androidx.lifecycle.Lifecycle] that will keep them in
 * memory but they should stay. We cannot add-remove observer in [LiveData.onActive],
 * [LiveData.onInactive] because that would mean missing changes in between or doing an
 * extra query on every UI rotation.
 *
 * This [LiveData] keeps a weak observer to the [InvalidationTracker] but it is hold
 * strongly by the [InvalidationTracker] as long as it is active.
 */
@SuppressLint("RestrictedApi")
internal class RoomTrackingLiveData<T> (
    val database: RoomDatabase,
    private val container: InvalidationLiveDataContainer,
    val inTransaction: Boolean,
    val computeFunction: Callable<T?>,
    tableNames: Array<out String>
) : LiveData<T>() {
    val observer: InvalidationTracker.Observer = object : InvalidationTracker.Observer(tableNames) {
        override fun onInvalidated(tables: Set<String>) {
            ArchTaskExecutor.getInstance().executeOnMainThread(invalidationRunnable)
        }
    }
    val invalid = AtomicBoolean(true)
    val computing = AtomicBoolean(false)
    val registeredObserver = AtomicBoolean(false)
    val queued = eu.faircode.email.ObjectHolder<Int>(0)
    val lock = Object()
    val refreshRunnable = Runnable {
		eu.faircode.email.Log.i("MMM Refresh")
		synchronized(lock) {
			queued.value--
			if (queued.value < 0) {
				eu.faircode.email.Log.e("$computeFunction queued=$queued.value")
				queued.value = 0
			}
		}
		eu.faircode.email.Log.i("MMM Refreshing")

		if (registeredObserver.compareAndSet(false, true)) {
			database.invalidationTracker.addWeakObserver(observer)
		}

		var value: T? = null
		var computed = false
		synchronized(computeFunction) {
			var retry = 0
			while (!computed) {
				try {
					value = computeFunction.call()
					computed = true
				} catch (e: Throwable) {
					if (++retry > 5) {
						eu.faircode.email.Log.e(e)
						break
					}
					eu.faircode.email.Log.w(e)
					try {
						Thread.sleep(2000L)
					} catch (ignored: InterruptedException) {
					}
				}
			}
		}
		if (computed) {
			postValue(value)
		}
		eu.faircode.email.Log.i("MMM Refreshed")
	}

    val invalidationRunnable = Runnable {
		val isActive = hasActiveObservers()
		if (invalid.compareAndSet(false, true)) {
			if (isActive) {
			eu.faircode.email.Log.i("MMM Invalidate")
				synchronized(lock) {
					if (queued.value > 0) {
						eu.faircode.email.Log.persist(eu.faircode.email.EntityLog.Type.Debug, "$computeFunction queued=$queued.value")
					} else {
						queued.value++
						queryExecutor.execute(refreshRunnable)
					}
					eu.faircode.email.Log.i("MMM Invalidated")
				}
			}
		}
	}

    @Suppress("UNCHECKED_CAST")
    override fun onActive() {
		super.onActive()
		container.onActive(this as LiveData<Any>)
		eu.faircode.email.Log.i("MMM Active")
		synchronized(lock) {
			queued.value++
			queryExecutor.execute(refreshRunnable)
		}
		eu.faircode.email.Log.i("MMM Actived")
	}

    @Suppress("UNCHECKED_CAST")
    override fun onInactive() {
        super.onInactive()
        container.onInactive(this as LiveData<Any>)
    }

    val queryExecutor: Executor
        get() = if (inTransaction) {
            database.transactionExecutor
        } else {
            database.queryExecutor
        }
}
