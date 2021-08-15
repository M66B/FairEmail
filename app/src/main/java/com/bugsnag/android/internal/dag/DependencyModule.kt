package com.bugsnag.android.internal.dag

import com.bugsnag.android.BackgroundTaskService
import com.bugsnag.android.TaskType

internal abstract class DependencyModule {

    private val properties = mutableListOf<Lazy<*>>()

    /**
     * Creates a new [Lazy] property that is marked as an object that should be resolved off the
     * main thread when [resolveDependencies] is called.
     */
    fun <T> future(initializer: () -> T): Lazy<T> {
        val lazy = lazy {
            initializer()
        }
        properties.add(lazy)
        return lazy
    }

    /**
     * Blocks until all dependencies in the module have been constructed. This provides the option
     * for modules to construct objects in a background thread, then have a user block on another
     * thread until all the objects have been constructed.
     */
    fun resolveDependencies(bgTaskService: BackgroundTaskService, taskType: TaskType) {
        kotlin.runCatching {
            bgTaskService.submitTask(
                taskType,
                Runnable {
                    properties.forEach { it.value }
                }
            ).get()
        }
    }
}
