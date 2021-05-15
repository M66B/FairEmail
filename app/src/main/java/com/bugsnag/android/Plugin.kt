package com.bugsnag.android

/**
 * A plugin allows for additional functionality to be added to the Bugsnag SDK.
 */
interface Plugin {

    /**
     * Loads a plugin with the given Client. When this method is invoked the plugin should
     * activate its behaviour - for example, by capturing an additional source of errors.
     */
    fun load(client: Client)

    /**
     * Unloads a plugin. When this is invoked the plugin should cease all custom behaviour and
     * restore the application to its unloaded state.
     */
    fun unload()
}
