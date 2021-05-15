package com.bugsnag.android

internal class ErrorInternal @JvmOverloads internal constructor(
    var errorClass: String,
    var errorMessage: String?,
    stacktrace: Stacktrace,
    var type: ErrorType = ErrorType.ANDROID
) : JsonStream.Streamable {

    val stacktrace: List<Stackframe> = stacktrace.trace

    internal companion object {
        fun createError(exc: Throwable, projectPackages: Collection<String>, logger: Logger): MutableList<Error> {
            val errors = mutableListOf<ErrorInternal>()

            var currentEx: Throwable? = exc
            while (currentEx != null) {
                // Somehow it's possible for stackTrace to be null in rare cases
                val stacktrace = currentEx.stackTrace ?: arrayOf<StackTraceElement>()
                val trace = Stacktrace.stacktraceFromJavaTrace(stacktrace, projectPackages, logger)
                errors.add(ErrorInternal(currentEx.javaClass.name, currentEx.localizedMessage, trace))
                currentEx = currentEx.cause
            }
            return errors.map { Error(it, logger) }.toMutableList()
        }
    }

    override fun toStream(writer: JsonStream) {
        writer.beginObject()
        writer.name("errorClass").value(errorClass)
        writer.name("message").value(errorMessage)
        writer.name("type").value(type.desc)
        writer.name("stacktrace").value(stacktrace)
        writer.endObject()
    }
}
