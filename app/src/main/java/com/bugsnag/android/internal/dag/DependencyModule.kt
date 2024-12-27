package com.bugsnag.android.internal.dag

import com.bugsnag.android.internal.BackgroundTaskService
import com.bugsnag.android.internal.TaskType

internal interface DependencyModule

internal abstract class BackgroundDependencyModule(
    @JvmField
    val bgTaskService: BackgroundTaskService,
    @JvmField
    val taskType: TaskType = TaskType.DEFAULT
) : DependencyModule {
    /**
     * Convenience function to create and schedule a `RunnableProvider` of [taskType] with
     * [bgTaskService]. The returned `RunnableProvider` will be implemented using the `provider`
     * lambda as its `invoke` implementation.
     */
    inline fun <R> provider(crossinline provider: () -> R): RunnableProvider<R> {
        return bgTaskService.provider(taskType, provider)
    }

    /**
     * Return a `RunnableProvider` containing the result of applying the given [mapping] to
     * this `Provider`. The `RunnableProvider` will be scheduled with [bgTaskService] as a
     * [taskType] when this function returns.
     *
     * This function behaves similar to `List.map` or `Any.let` but with `Provider` encapsulation
     * to handle value reuse and threading.
     */
    internal inline fun <E, R> Provider<E>.map(crossinline mapping: (E) -> R): RunnableProvider<R> {
        val task = object : RunnableProvider<R>() {
            override fun invoke(): R = mapping(this@map.get())
        }

        bgTaskService.execute(taskType, task)
        return task
    }
}
