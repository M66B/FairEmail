package com.bugsnag.android.internal

import android.app.Application

class BugsnagContentProvider : AbstractStartupProvider() {
    override fun onCreate(): Boolean {
        (context?.applicationContext as? Application)?.let { app ->
            ForegroundDetector.registerOn(app)
        }

        return true
    }
}
