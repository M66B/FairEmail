package com.bugsnag.android

import com.bugsnag.android.internal.ImmutableConfig
import com.bugsnag.android.internal.dag.Provider
import java.io.IOException

/**
 * Stateless information set by the notifier about your app can be found on this class. These values
 * can be accessed and amended if necessary.
 */
open class App internal constructor(
    /**
     * The architecture of the running application binary
     */
    var binaryArch: String?,

    /**
     * The package name of the application
     */
    var id: String?,

    /**
     * The release stage set in [Configuration.releaseStage]
     */
    var releaseStage: String?,

    /**
     * The version of the application set in [Configuration.version]
     */
    var version: String?,

    /**
     The revision ID from the manifest (React Native apps only)
     */
    var codeBundleId: String?,

    /**
     * The unique identifier for the build of the application set in [Configuration.buildUuid]
     */
    buildUuid: Provider<String?>?,

    /**
     * The application type set in [Configuration#version]
     */
    var type: String?,

    /**
     * The version code of the application set in [Configuration.versionCode]
     */
    var versionCode: Number?
) : JsonStream.Streamable {

    private var buildUuidProvider: Provider<String?>? = buildUuid

    var buildUuid: String? = null
        get() = field ?: buildUuidProvider?.getOrNull()
        set(value) {
            field = value
            buildUuidProvider = null
        }

    internal constructor(
        config: ImmutableConfig,
        binaryArch: String?,
        id: String?,
        releaseStage: String?,
        version: String?,
        codeBundleId: String?
    ) : this(
        binaryArch,
        id,
        releaseStage,
        version,
        codeBundleId,
        config.buildUuid,
        config.appType,
        config.versionCode
    )

    internal open fun serialiseFields(writer: JsonStream) {
        writer.name("binaryArch").value(binaryArch)
        writer.name("buildUUID").value(buildUuid)
        writer.name("codeBundleId").value(codeBundleId)
        writer.name("id").value(id)
        writer.name("releaseStage").value(releaseStage)
        writer.name("type").value(type)
        writer.name("version").value(version)
        writer.name("versionCode").value(versionCode)
    }

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        writer.beginObject()
        serialiseFields(writer)
        writer.endObject()
    }
}
