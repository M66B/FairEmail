package com.bugsnag.android

import java.util.Observable

internal open class BaseObservable : Observable() {
    fun notifyObservers(event: StateEvent) {
        setChanged()
        super.notifyObservers(event)
    }
}
