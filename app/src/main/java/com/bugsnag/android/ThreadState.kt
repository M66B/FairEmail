package com.bugsnag.android

import com.bugsnag.android.internal.ImmutableConfig
import java.io.IOException

/**
 * Capture and serialize the state of all threads at the time of an exception.
 */
internal class ThreadState @Suppress("LongParameterList") @JvmOverloads constructor(
    exc: Throwable?,
    isUnhandled: Boolean,
    sendThreads: ThreadSendPolicy,
    projectPackages: Collection<String>,
    logger: Logger,
    currentThread: java.lang.Thread? = null,
    stackTraces: MutableMap<java.lang.Thread, Array<StackTraceElement>>? = null
) : JsonStream.Streamable {

    internal constructor(
        exc: Throwable?,
        isUnhandled: Boolean,
        config: ImmutableConfig
    ) : this(exc, isUnhandled, config.sendThreads, config.projectPackages, config.logger)

    val threads: MutableList<Thread>

    init {
        val recordThreads = sendThreads == ThreadSendPolicy.ALWAYS ||
            (sendThreads == ThreadSendPolicy.UNHANDLED_ONLY && isUnhandled)

        threads = when {
            recordThreads -> captureThreadTrace(
                stackTraces ?: java.lang.Thread.getAllStackTraces(),
                currentThread ?: java.lang.Thread.currentThread(),
                exc,
                isUnhandled,
                projectPackages,
                logger
            )
            else -> mutableListOf()
        }
    }

    private fun captureThreadTrace(
        stackTraces: MutableMap<java.lang.Thread, Array<StackTraceElement>>,
        currentThread: java.lang.Thread,
        exc: Throwable?,
        isUnhandled: Boolean,
        projectPackages: Collection<String>,
        logger: Logger
    ): MutableList<Thread> {
        // API 24/25 don't record the currentThread, add it in manually
        // https://issuetracker.google.com/issues/64122757
        if (!stackTraces.containsKey(currentThread)) {
            stackTraces[currentThread] = currentThread.stackTrace
        }
        if (exc != null && isUnhandled) { // unhandled errors use the exception trace for thread traces
            stackTraces[currentThread] = exc.stackTrace
        }

        val currentThreadId = currentThread.id
        return stackTraces.keys
            .sortedBy { it.id }
            .mapNotNull { thread ->
                val trace = stackTraces[thread]

                if (trace != null) {
                    val stacktrace = Stacktrace(trace, projectPackages, logger)
                    val errorThread = thread.id == currentThreadId
                    Thread(thread.id, thread.name, ThreadType.ANDROID, errorThread, stacktrace, logger)
                } else {
                    null
                }
            }.toMutableList()
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
