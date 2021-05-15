package com.bugsnag.android

internal interface UserAware {
    fun getUser(): User
    fun setUser(id: String?, email: String?, name: String?)
}
