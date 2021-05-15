package com.bugsnag.android

import java.io.IOException
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

internal class BreadcrumbState(
    maxBreadcrumbs: Int,
    val callbackState: CallbackState,
    val logger: Logger
) : BaseObservable(), JsonStream.Streamable {

    val store: Queue<Breadcrumb> = ConcurrentLinkedQueue()

    private val maxBreadcrumbs: Int

    init {
        when {
            maxBreadcrumbs > 0 -> this.maxBreadcrumbs = maxBreadcrumbs
            else -> this.maxBreadcrumbs = 0
        }
    }

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        pruneBreadcrumbs()
        writer.beginArray()
        store.forEach { it.toStream(writer) }
        writer.endArray()
    }

    fun add(breadcrumb: Breadcrumb) {
        if (!callbackState.runOnBreadcrumbTasks(breadcrumb, logger)) {
            return
        }

        store.add(breadcrumb)
        pruneBreadcrumbs()
        notifyObservers(
            StateEvent.AddBreadcrumb(
                breadcrumb.message,
                breadcrumb.type,
                DateUtils.toIso8601(breadcrumb.timestamp),
                breadcrumb.metadata ?: mutableMapOf()
            )
        )
    }

    private fun pruneBreadcrumbs() {
        // Remove oldest breadcrumbState until new max size reached
        while (store.size > maxBreadcrumbs) {
            store.poll()
        }
    }
}
