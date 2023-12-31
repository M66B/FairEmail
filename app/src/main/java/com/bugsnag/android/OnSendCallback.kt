package com.bugsnag.android

/**
 * A callback to be invoked before an [Event] is uploaded to a server. Similar to
 * [OnErrorCallback], an `OnSendCallback` may modify the `Event`
 * contents or even reject the entire payload by returning `false`.
 */
fun interface OnSendCallback {
    fun onSend(event: Event): Boolean
}
