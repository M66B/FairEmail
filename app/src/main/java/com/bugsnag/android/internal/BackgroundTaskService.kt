package com.bugsnag.android.internal

import androidx.annotation.VisibleForTesting
import com.bugsnag.android.internal.dag.RunnableProvider
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.lang.Thread as JThread

/**
 * The type of task which is being submitted. This determines which execution queue
 * the task will be added to.
 */
enum class TaskType {

    /**
     * A task that sends an error request. Any filesystem operations
     * that persist/delete errors must be submitted using this type.
     */
    ERROR_REQUEST,

    /**
     * A task that sends a session request. Any filesystem operations
     * that persist/delete sessions must be submitted using this type.
     */
    SESSION_REQUEST,

    /**
     * A task that performs I/O, such as reading a file on disk. This should NOT include operations
     * related to error/session storage - use [ERROR_REQUEST] or [SESSION_REQUEST] instead.
     */
    IO,

    /**
     * A task that sends an internal error report to Bugsnag.
     */
    INTERNAL_REPORT,

    /**
     * Any other task that needs to run in the background. These will typically be
     * short-lived operations that take <100ms, such as registering a
     * [android.content.BroadcastReceiver].
     */
    DEFAULT
}

private const val SHUTDOWN_WAIT_MS = 1500L

// these values have been loosely adapted from android.os.AsyncTask over the years.
private const val THREAD_POOL_SIZE = 1
private const val KEEP_ALIVE_SECS = 30L
private const val TASK_QUEUE_SIZE = 128

private class TaskTypeThread(runnable: Runnable, name: String, val taskType: TaskType) :
    JThread(runnable, name)

internal val JThread.taskType get() = (this as? TaskTypeThread)?.taskType

internal fun createExecutor(name: String, type: TaskType, keepAlive: Boolean): ExecutorService {
    val queue: BlockingQueue<Runnable> = LinkedBlockingQueue(TASK_QUEUE_SIZE)
    val threadFactory = ThreadFactory { TaskTypeThread(it, name, type) }

    // certain executors (error/session/io) should always keep their threads alive, but others
    // are less important so are allowed a pool size of 0 that expands on demand.
    val coreSize = when {
        keepAlive -> THREAD_POOL_SIZE
        else -> 0
    }
    return ThreadPoolExecutor(
        coreSize,
        THREAD_POOL_SIZE,
        KEEP_ALIVE_SECS,
        TimeUnit.SECONDS,
        queue,
        threadFactory
    )
}

/**
 * Provides a service for submitting lengthy tasks to run on background threads.
 *
 * A [TaskType] must be submitted with each task, which routes it to the appropriate executor.
 * Setting the correct [TaskType] is critical as it can be used to enforce thread confinement.
 * It also avoids short-running operations being held up by long-running operations submitted
 * to the same executor.
 */
class BackgroundTaskService(
    // these executors must remain single-threaded - the SDK makes assumptions
    // about synchronization based on this.
    @get:VisibleForTesting
    internal val errorExecutor: ExecutorService = createExecutor(
        "Bugsnag Error thread",
        TaskType.ERROR_REQUEST,
        true
    ),

    @get:VisibleForTesting
    internal val sessionExecutor: ExecutorService = createExecutor(
        "Bugsnag Session thread",
        TaskType.SESSION_REQUEST,
        true
    ),

    @get:VisibleForTesting
    internal val ioExecutor: ExecutorService = createExecutor(
        "Bugsnag IO thread",
        TaskType.IO,
        true
    ),

    @get:VisibleForTesting
    internal val internalReportExecutor: ExecutorService = createExecutor(
        "Bugsnag Internal Report thread",
        TaskType.INTERNAL_REPORT,
        false
    ),

    @get:VisibleForTesting
    internal val defaultExecutor: ExecutorService = createExecutor(
        "Bugsnag Default thread",
        TaskType.DEFAULT,
        false
    )
) {

    /**
     * Submits a task for execution on a single-threaded executor. It is guaranteed that tasks
     * with the same [TaskType] are executed in the order of submission.
     *
     * The caller is responsible for catching and handling
     * [java.util.concurrent.RejectedExecutionException] if the executor is saturated.
     *
     * On process termination the service will attempt to wait for previously submitted jobs
     * with the task type [TaskType.ERROR_REQUEST], [TaskType.SESSION_REQUEST] and [TaskType.IO].
     * This is a best-effort attempt - no guarantee can be made that the operations will complete.
     */
    @Throws(RejectedExecutionException::class)
    fun submitTask(taskType: TaskType, runnable: Runnable): Future<*> {
        return submitTask(taskType, Executors.callable(runnable))
    }

    /**
     * @see [submitTask]
     */
    @Throws(RejectedExecutionException::class)
    fun <T> submitTask(taskType: TaskType, callable: Callable<T>): Future<T> {
        val task = FutureTask(callable)
        execute(taskType, task)
        return SafeFuture(task, taskType)
    }

    fun execute(taskType: TaskType, task: Runnable) {
        when (taskType) {
            TaskType.ERROR_REQUEST -> errorExecutor.execute(task)
            TaskType.SESSION_REQUEST -> sessionExecutor.execute(task)
            TaskType.IO -> ioExecutor.execute(task)
            TaskType.INTERNAL_REPORT -> internalReportExecutor.execute(task)
            TaskType.DEFAULT -> defaultExecutor.execute(task)
        }
    }

    /**
     * Notifies the background service that the process is about to terminate. This causes it to
     * shutdown submission of tasks to executors, while allowing for in-flight tasks
     * to be completed within a reasonable grace period.
     */
    fun shutdown() {
        // don't wait for existing tasks to complete for these executors, as they are
        // less essential
        internalReportExecutor.shutdownNow()
        defaultExecutor.shutdownNow()

        // Wait a little while for these ones to shut down
        errorExecutor.shutdown()
        sessionExecutor.shutdown()
        ioExecutor.shutdown()

        errorExecutor.awaitTerminationSafe()
        sessionExecutor.awaitTerminationSafe()
        ioExecutor.awaitTerminationSafe()
    }

    inline fun <R> provider(
        taskType: TaskType,
        crossinline provider: () -> R
    ): RunnableProvider<R> {
        val task = object : RunnableProvider<R>() {
            override fun invoke(): R = provider()
        }

        execute(taskType, task)
        return task
    }

    private fun ExecutorService.awaitTerminationSafe() {
        try {
            awaitTermination(SHUTDOWN_WAIT_MS, TimeUnit.MILLISECONDS)
        } catch (ignored: InterruptedException) {
            // ignore interrupted exception as the JVM is shutting down
        }
    }

    private class SafeFuture<V>(
        private val delegate: FutureTask<V>,
        private val taskType: TaskType
    ) : Future<V> by delegate {
        override fun get(): V {
            ensureTaskGetSafe()
            return delegate.get()
        }

        override fun get(timeout: Long, unit: TimeUnit?): V {
            ensureTaskGetSafe()
            return delegate.get(timeout, unit)
        }

        private fun ensureTaskGetSafe() {
            if (!delegate.isDone && JThread.currentThread().taskType == taskType) {
                // if this is the execution queue for the wrapped FutureTask && it is not yet 'done'
                // then it has not yet been started, so we run it immediately
                delegate.run()
            }
        }
    }
}
