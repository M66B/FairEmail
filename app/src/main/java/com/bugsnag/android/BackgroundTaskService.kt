package com.bugsnag.android

import androidx.annotation.VisibleForTesting
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * The type of task which is being submitted. This determines which execution queue
 * the task will be added to.
 */
internal enum class TaskType {

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

internal fun createExecutor(name: String, keepAlive: Boolean): ThreadPoolExecutor {
    val queue: BlockingQueue<Runnable> = LinkedBlockingQueue(TASK_QUEUE_SIZE)
    val threadFactory = ThreadFactory { Thread(it, name) }

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
internal class BackgroundTaskService(
    // these executors must remain single-threaded - the SDK makes assumptions
    // about synchronization based on this.
    @VisibleForTesting
    internal val errorExecutor: ThreadPoolExecutor = createExecutor(
        "Bugsnag Error thread",
        true
    ),

    @VisibleForTesting
    internal val sessionExecutor: ThreadPoolExecutor = createExecutor(
        "Bugsnag Session thread",
        true
    ),

    @VisibleForTesting
    internal val ioExecutor: ThreadPoolExecutor = createExecutor(
        "Bugsnag IO thread",
        true
    ),

    @VisibleForTesting
    internal val internalReportExecutor: ThreadPoolExecutor = createExecutor(
        "Bugsnag Internal Report thread",
        false
    ),

    @VisibleForTesting
    internal val defaultExecutor: ThreadPoolExecutor = createExecutor(
        "Bugsnag Default thread",
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
        return when (taskType) {
            TaskType.ERROR_REQUEST -> errorExecutor.submit(callable)
            TaskType.SESSION_REQUEST -> sessionExecutor.submit(callable)
            TaskType.IO -> ioExecutor.submit(callable)
            TaskType.INTERNAL_REPORT -> internalReportExecutor.submit(callable)
            TaskType.DEFAULT -> defaultExecutor.submit(callable)
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

    private fun ThreadPoolExecutor.awaitTerminationSafe() {
        try {
            awaitTermination(SHUTDOWN_WAIT_MS, TimeUnit.MILLISECONDS)
        } catch (ignored: InterruptedException) {
            // ignore interrupted exception as the JVM is shutting down
        }
    }
}
