package com.bugsnag.android

import com.bugsnag.android.internal.ImmutableConfig
import java.io.IOException
import java.lang.Thread as JavaThread

/**
 * Capture and serialize the state of all threads at the time of an exception.
 */
internal class ThreadState @Suppress("LongParameterList") constructor(
    exc: Throwable?,
    isUnhandled: Boolean,
    maxThreads: Int,
    sendThreads: ThreadSendPolicy,
    projectPackages: Collection<String>,
    logger: Logger,
    currentThread: JavaThread = JavaThread.currentThread(),
    allThreads: List<JavaThread> = allThreads()
) : JsonStream.Streamable {

    internal constructor(
        exc: Throwable?,
        isUnhandled: Boolean,
        config: ImmutableConfig
    ) : this(exc, isUnhandled, config.maxReportedThreads, config.sendThreads, config.projectPackages, config.logger)

    val threads: MutableList<Thread>

    init {
        val recordThreads = sendThreads == ThreadSendPolicy.ALWAYS ||
            (sendThreads == ThreadSendPolicy.UNHANDLED_ONLY && isUnhandled)

        threads = when {
            recordThreads -> captureThreadTrace(
                allThreads,
                currentThread,
                exc,
                isUnhandled,
                maxThreads,
                projectPackages,
                logger
            )
            else -> mutableListOf()
        }
    }

    companion object {
        private fun rootThreadGroup(): ThreadGroup {
            var group = JavaThread.currentThread().threadGroup!!

            while (group.parent != null) {
                group = group.parent
            }

            return group
        }

        internal fun allThreads(): List<JavaThread> {
            val rootGroup = rootThreadGroup()
            val threadCount = rootGroup.activeCount()
            val threads: Array<JavaThread?> = arrayOfNulls(threadCount)
            rootGroup.enumerate(threads)
            return threads.filterNotNull()
        }
    }

    private fun captureThreadTrace(
        allThreads: List<JavaThread>,
        currentThread: JavaThread,
        exc: Throwable?,
        isUnhandled: Boolean,
        maxThreadCount: Int,
        projectPackages: Collection<String>,
        logger: Logger
    ): MutableList<Thread> {
        fun toBugsnagThread(thread: JavaThread): Thread {
            val isErrorThread = thread.id == currentThread.id
            val stackTrace = Stacktrace(
                if (isErrorThread) {
                    if (exc != null && isUnhandled) { // unhandled errors use the exception trace for thread traces
                        exc.stackTrace
                    } else {
                        currentThread.stackTrace
                    }
                } else {
                    thread.stackTrace
                },
                projectPackages, logger
            )

            return Thread(
                thread.id,
                thread.name,
                ThreadType.ANDROID,
                isErrorThread,
                Thread.State.forThread(thread),
                stackTrace,
                logger
            )
        }

        // Keep the lowest ID threads (ordered). Anything after maxThreadCount is lost.
        // Note: We must ensure that currentThread is always present in the final list regardless.
        val keepThreads = allThreads.sortedBy { it.id }.take(maxThreadCount)

        val reportThreads = if (keepThreads.contains(currentThread)) {
            keepThreads
        } else {
            // API 24/25 don't record the currentThread, so add it in manually
            // https://issuetracker.google.com/issues/64122757
            // currentThread may also have been removed if its ID occurred after maxThreadCount
            keepThreads.take(Math.max(maxThreadCount - 1, 0)).plus(currentThread).sortedBy { it.id }
        }.map { toBugsnagThread(it) }.toMutableList()

        if (allThreads.size > maxThreadCount) {
            reportThreads.add(
                Thread(
                    -1,
                    "[${allThreads.size - maxThreadCount} threads omitted as the maxReportedThreads limit ($maxThreadCount) was exceeded]",
                    ThreadType.EMPTY,
                    false,
                    Thread.State.UNKNOWN,
                    Stacktrace(arrayOf(StackTraceElement("", "", "-", 0)), projectPackages, logger),
                    logger
                )
            )
        }
        return reportThreads
    }

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        writer.beginArray()
        for (thread in threads) {
            writer.value(thread)
        }
        writer.endArray()
    }
}
