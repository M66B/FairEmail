package com.bugsnag.android

import java.io.IOException

/**
 * Represents a single stackframe from a [Throwable]
 */
class Stackframe : JsonStream.Streamable {
    /**
     * The name of the method that was being executed
     */
    var method: String?
        set(value) {
            nativeFrame?.method = value
            field = value
        }

    /**
     * The location of the source file
     */
    var file: String?
        set(value) {
            nativeFrame?.file = value
            field = value
        }

    /**
     * The line number within the source file this stackframe refers to
     */
    var lineNumber: Number?
        set(value) {
            nativeFrame?.lineNumber = value
            field = value
        }

    /**
     * Whether the package is considered to be in your project for the purposes of grouping and
     * readability on the Bugsnag dashboard. Project package names can be set in
     * [Configuration.projectPackages]
     */
    var inProject: Boolean?

    /**
     * Lines of the code surrounding the frame, where the lineNumber is the key (React Native only)
     */
    var code: Map<String, String?>?

    /**
     * The column number of the frame (React Native only)
     */
    var columnNumber: Number?

    /**
     * The type of the error
     */
    var type: ErrorType? = null
        set(value) {
            nativeFrame?.type = value
            field = value
        }

    @JvmOverloads
    internal constructor(
        method: String?,
        file: String?,
        lineNumber: Number?,
        inProject: Boolean?,
        code: Map<String, String?>? = null,
        columnNumber: Number? = null
    ) {
        this.method = method
        this.file = file
        this.lineNumber = lineNumber
        this.inProject = inProject
        this.code = code
        this.columnNumber = columnNumber
    }

    private var nativeFrame: NativeStackframe? = null

    constructor(nativeFrame: NativeStackframe) : this(
        nativeFrame.method,
        nativeFrame.file,
        nativeFrame.lineNumber,
        false,
        null
    ) {
        this.nativeFrame = nativeFrame
        this.type = nativeFrame.type
    }

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        val ndkFrame = nativeFrame
        if (ndkFrame != null) {
            ndkFrame.toStream(writer)
            return
        }

        writer.beginObject()
        writer.name("method").value(method)
        writer.name("file").value(file)
        writer.name("lineNumber").value(lineNumber)
        writer.name("inProject").value(inProject)
        writer.name("columnNumber").value(columnNumber)

        type?.let {
            writer.name("type").value(it.desc)
        }

        code?.let { map: Map<String, String?> ->
            writer.name("code")

            map.forEach {
                writer.beginObject()
                writer.name(it.key)
                writer.value(it.value)
                writer.endObject()
            }
        }
        writer.endObject()
    }
}
