package com.bugsnag.android

import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Stores breadcrumbs added to the [Client] in a ring buffer. If the number of breadcrumbs exceeds
 * the maximum configured limit then the oldest breadcrumb in the ring buffer will be overwritten.
 *
 * When the breadcrumbs are required for generation of an event a [List] is constructed and
 * breadcrumbs added in the order of their addition.
 */
internal class BreadcrumbState(
    private val maxBreadcrumbs: Int,
    private val callbackState: CallbackState,
    private val logger: Logger
) : BaseObservable(), JsonStream.Streamable {

    /*
     * We use the `index` as both a pointer to the tail of our ring-buffer, and also as "cheat"
     * semaphore. When the ring-buffer is being copied - the index is set to a negative number,
     * which is an invalid array-index. By masking the `expected` value in a `compareAndSet` with
     * `validIndexMask`: the CAS operation will only succeed if it wouldn't interrupt a concurrent
     * `copy()` call.
     */
    private val validIndexMask: Int = Int.MAX_VALUE

    private val store = arrayOfNulls<Breadcrumb?>(maxBreadcrumbs)
    private val index = AtomicInteger(0)

    fun add(breadcrumb: Breadcrumb) {
        if (maxBreadcrumbs == 0 || !callbackState.runOnBreadcrumbTasks(breadcrumb, logger)) {
            return
        }

        // store the breadcrumb in the ring buffer
        val position = getBreadcrumbIndex()
        store[position] = breadcrumb

        updateState {
            // use direct field access to avoid overhead of accessor method
            StateEvent.AddBreadcrumb(
                breadcrumb.impl.message,
                breadcrumb.impl.type,
                // an encoding of milliseconds since the epoch
                "t${breadcrumb.impl.timestamp.time}",
                breadcrumb.impl.metadata ?: mutableMapOf()
            )
        }
    }

    /**
     * Retrieves the index in the ring buffer where the breadcrumb should be stored.
     */
    private fun getBreadcrumbIndex(): Int {
        while (true) {
            val currentValue = index.get() and validIndexMask
            val nextValue = (currentValue + 1) % maxBreadcrumbs
            if (index.compareAndSet(currentValue, nextValue)) {
                return currentValue
            }
        }
    }

    /**
     * Creates a copy of the breadcrumbs in the order of their addition.
     */
    fun copy(): List<Breadcrumb> {
        if (maxBreadcrumbs == 0) {
            return emptyList()
        }

        // Set a negative value that stops any other thread from adding a breadcrumb.
        // This handles reentrancy by waiting here until the old value has been reset.
        var tail = -1
        while (tail == -1) {
            tail = index.getAndSet(-1)
        }

        try {
            val result = arrayOfNulls<Breadcrumb>(maxBreadcrumbs)
            store.copyInto(result, 0, tail, maxBreadcrumbs)
            store.copyInto(result, maxBreadcrumbs - tail, 0, tail)
            return result.filterNotNull()
        } finally {
            index.set(tail)
        }
    }

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        val crumbs = copy()
        writer.beginArray()
        crumbs.forEach { it.toStream(writer) }
        writer.endArray()
    }
}
