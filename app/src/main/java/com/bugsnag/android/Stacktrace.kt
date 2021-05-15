package com.bugsnag.android

import java.io.IOException

/**
 * Serialize an exception stacktrace and mark frames as "in-project"
 * where appropriate.
 */
internal class Stacktrace : JsonStream.Streamable {

    companion object {
        private const val STACKTRACE_TRIM_LENGTH = 200

        /**
         * Calculates whether a stackframe is 'in project' or not by checking its class against
         * [Configuration.getProjectPackages].
         *
         * For example if the projectPackages included 'com.example', then
         * the `com.example.Foo` class would be considered in project, but `org.example.Bar` would
         * not.
         */
        fun inProject(className: String, projectPackages: Collection<String>): Boolean? {
            for (packageName in projectPackages) {
                if (className.startsWith(packageName)) {
                    return true
                }
            }
            return null
        }

        fun stacktraceFromJavaTrace(
            stacktrace: Array<StackTraceElement>,
            projectPackages: Collection<String>,
            logger: Logger
        ): Stacktrace {
            val frames = stacktrace.mapNotNull { serializeStackframe(it, projectPackages, logger) }
            return Stacktrace(frames)
        }

        private fun serializeStackframe(
            el: StackTraceElement,
            projectPackages: Collection<String>,
            logger: Logger
        ): Stackframe? {
            try {
                val methodName = when {
                    el.className.isNotEmpty() -> el.className + "." + el.methodName
                    else -> el.methodName
                }

                return Stackframe(
                    methodName,
                    if (el.fileName == null) "Unknown" else el.fileName,
                    el.lineNumber,
                    inProject(el.className, projectPackages)
                )
            } catch (lineEx: Exception) {
                logger.w("Failed to serialize stacktrace", lineEx)
                return null
            }
        }
    }

    val trace: List<Stackframe>

    constructor(frames: List<Stackframe>) {
        trace = limitTraceLength(frames)
    }

    private fun <T> limitTraceLength(frames: List<T>): List<T> {
        return when {
            frames.size >= STACKTRACE_TRIM_LENGTH -> frames.subList(0, STACKTRACE_TRIM_LENGTH)
            else -> frames
        }
    }

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        writer.beginArray()
        trace.forEach { writer.value(it) }
        writer.endArray()
    }
}
