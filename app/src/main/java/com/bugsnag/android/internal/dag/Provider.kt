package com.bugsnag.android.internal.dag

import android.os.Looper
import androidx.annotation.VisibleForTesting
import java.util.concurrent.atomic.AtomicInteger

/**
 * A lightweight abstraction similar to `Lazy` or `Future` allowing values to be calculated on
 * separate threads, or to be pre-computed.
 */
interface Provider<E> {
    /**
     * Same as [get] but will return `null` instead of throwing an exception if the value could
     * not be computed.
     */
    fun getOrNull(): E?

    /**
     * Return the value sourced from this provider, throwing an exception if the provider failed
     * to calculate a value. Anything thrown from here will have been captured when attempting
     * to calculate the value.
     */
    fun get(): E
}

/**
 * The primary implementation of [Provider], usually created using the
 * [BackgroundDependencyModule.provider] function. Similar conceptually to
 * [java.util.concurrent.FutureTask] but with a more compact implementation. The implementation
 * of [RunnableProvider.get] is special because it behaves more like [Lazy.value] in that getting
 * a value that is still pending will cause it to be run on the current thread instead of waiting
 * for it to be run "sometime in the future". This makes RunnableProviders less bug-prone when
 * dealing with single-thread executors (such as those in [BackgroundTaskService]). RunnableProvider
 * also has special handling for the main-thread, ensuring no computational work (such as IO) is
 * done on the main thread.
 */
abstract class RunnableProvider<E> : Provider<E>, Runnable {
    private val state = AtomicInteger(TASK_STATE_PENDING)

    @Volatile
    private var value: Any? = null

    /**
     * Calculate the value of this [Provider]. This function will be called at-most once by [run].
     * Do not call this function directly, instead use [get] and [getOrNull] which implement the
     * correct threading behaviour and will reuse the value if it has been previously calculated.
     */
    abstract operator fun invoke(): E

    override fun getOrNull(): E? {
        return getOr { return null }
    }

    override fun get(): E {
        return getOr { throw value as Throwable }
    }

    private inline fun getOr(failureHandler: () -> E): E {
        while (true) {
            when (state.get()) {
                TASK_STATE_RUNNING -> awaitResult()
                TASK_STATE_PENDING -> {
                    if (isMainThread()) {
                        // When the calling thread is the 'main' thread, we *always* wait for the
                        // background workers to [invoke] this Provider, assuming that the Provider
                        // is performing some kind of IO that should be kept away from the main
                        // thread. Ideally this doesn't happen, but this behaviour avoids the
                        // need for complicated callback mechanisms.
                        awaitResult()
                    } else {
                        // If the Provider has yet to be computed, we will try and run it on the
                        // current thread. This potentially causes run() to happen on a different
                        // Thread to the expected worker (TaskType), effectively like work-stealing.
                        run()
                    }
                }

                TASK_STATE_COMPLETE -> @Suppress("UNCHECKED_CAST") return value as E
                TASK_STATE_FAILED -> failureHandler()
            }
        }
    }

    private fun isMainThread(): Boolean {
        return Thread.currentThread() === mainThread
    }

    /**
     * Cause the current thread to wait (block) until this `Provider` [isComplete]. Upon returning
     * the [isComplete] function will return `true`.
     */
    private fun awaitResult() {
        synchronized(this) {
            while (!isComplete()) {
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (this as Object).wait()
            }
        }
    }

    private fun isComplete() = when (state.get()) {
        TASK_STATE_PENDING, TASK_STATE_RUNNING -> false
        else -> true
    }

    /**
     * The main entry point for a provider, typically called by a worker thread from
     * [BackgroundTaskService]. If [run] has already been called this will be a no-op (including
     * a reentrant thread), as such the task state *must* be checked after calling this.
     *
     * This should not be called, and instead [get] or [getOrNull] should be used to obtain the
     * value produced by [invoke].
     */
    final override fun run() {
        if (state.compareAndSet(TASK_STATE_PENDING, TASK_STATE_RUNNING)) {
            try {
                value = invoke()
                state.set(TASK_STATE_COMPLETE)
            } catch (ex: Throwable) {
                value = ex
                state.set(TASK_STATE_FAILED)
            } finally {
                synchronized(this) {
                    // wakeup any waiting threads
                    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                    (this as Object).notifyAll()
                }
            }
        }
    }

    @VisibleForTesting
    internal companion object {
        /**
         * The `Provider` task state before the provider has started actually running. This state
         * indicates that the task has been constructed, has typically been scheduled but has
         * not actually started running yet.
         */
        private const val TASK_STATE_PENDING = 0

        /**
         * The `Provider` task state when running. Once the [run] function returns the state will
         * be either [TASK_STATE_COMPLETE] or [TASK_STATE_FAILED].
         */
        private const val TASK_STATE_RUNNING = 1

        /**
         * The `Provider` state of a successfully completed task. When this is the state the
         * provider value can be obtained immediately without error.
         */
        private const val TASK_STATE_COMPLETE = 2

        /**
         * The `Provider` state of a task where [invoke] failed with an error or exception.
         */
        private const val TASK_STATE_FAILED = 999

        /**
         * We cache the main thread to avoid any locks within [Looper.getMainLooper]. This is
         * settable for unit tests, so that there doesn't have to be a valid Looper when they run.
         *
         * Actually access is done via the [mainThread] property.
         */
        @VisibleForTesting
        @Suppress("ObjectPropertyNaming") // backing property from 'mainThread'
        internal var _mainThread: Thread? = null
            get() {
                if (field == null) {
                    field = Looper.getMainLooper().thread
                }
                return field
            }

        internal val mainThread: Thread get() = _mainThread!!
    }
}

data class ValueProvider<T>(private val value: T) : Provider<T> {
    override fun getOrNull(): T? = get()
    override fun get(): T = value
}
