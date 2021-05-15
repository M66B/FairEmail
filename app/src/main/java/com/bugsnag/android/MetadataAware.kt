package com.bugsnag.android

internal interface MetadataAware {
    fun addMetadata(section: String, value: Map<String, Any?>)
    fun addMetadata(section: String, key: String, value: Any?)

    fun clearMetadata(section: String)
    fun clearMetadata(section: String, key: String)

    fun getMetadata(section: String): Map<String, Any>?
    fun getMetadata(section: String, key: String): Any?
}
