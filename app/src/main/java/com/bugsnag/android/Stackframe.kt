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

    /**
     * The location of the source file
     */
    var file: String?

    /**
     * The line number within the source file this stackframe refers to
     */
    var lineNumber: Number?

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
     * The address of the instruction where the event occurred.
     */
    var frameAddress: Long? = null

    /**
     * The address of the function where the event occurred.
     */
    var symbolAddress: Long? = null

    /**
     * The address of the library where the event occurred.
     */
    var loadAddress: Long? = null

    /**
     * Identifies the exact build this frame originates from.
     */
    var codeIdentifier: String? = null

    /**
     * Whether this frame identifies the program counter
     */
    var isPC: Boolean? = null

    /**
     * The type of the error
     */
    var type: ErrorType? = null

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

    constructor(nativeFrame: NativeStackframe) : this(
        nativeFrame.method,
        nativeFrame.file,
        nativeFrame.lineNumber,
        null,
        null
    ) {
        this.frameAddress = nativeFrame.frameAddress
        this.symbolAddress = nativeFrame.symbolAddress
        this.loadAddress = nativeFrame.loadAddress
        this.codeIdentifier = nativeFrame.codeIdentifier
        this.isPC = nativeFrame.isPC
        this.type = nativeFrame.type
    }

    internal constructor(json: Map<String, Any?>) {
        method = json["method"] as? String
        file = json["file"] as? String
        lineNumber = json["lineNumber"] as? Number
        inProject = json["inProject"] as? Boolean
        columnNumber = json["columnNumber"] as? Number
        frameAddress = (json["frameAddress"] as? Number)?.toLong()
        symbolAddress = (json["symbolAddress"] as? Number)?.toLong()
        loadAddress = (json["loadAddress"] as? Number)?.toLong()
        codeIdentifier = (json["codeIdentifier"] as? String)
        isPC = json["isPC"] as? Boolean

        @Suppress("UNCHECKED_CAST")
        code = json["code"] as? Map<String, String?>
        type = (json["type"] as? String)?.let { ErrorType.fromDescriptor(it) }
    }

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        writer.beginObject()
        writer.name("method").value(method)
        writer.name("file").value(file)
        writer.name("lineNumber").value(lineNumber)

        inProject?.let { writer.name("inProject").value(it) }

        writer.name("columnNumber").value(columnNumber)

        frameAddress?.let { writer.name("frameAddress").value(it) }
        symbolAddress?.let { writer.name("symbolAddress").value(it) }
        loadAddress?.let { writer.name("loadAddress").value(it) }
        codeIdentifier?.let { writer.name("codeIdentifier").value(it) }
        isPC?.let { writer.name("isPC").value(it) }

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
