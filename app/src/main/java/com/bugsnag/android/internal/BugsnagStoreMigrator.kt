package com.bugsnag.android.internal

import java.io.File

internal object BugsnagStoreMigrator {

    @JvmStatic
    fun moveToNewDirectory(persistenceDir: File) {
        val bugsnagDir = File(persistenceDir, "bugsnag")
        if (!bugsnagDir.isDirectory) {
            bugsnagDir.mkdirs()
        }
        val filesToMove = listOf(
            "last-run-info" to "last-run-info",
            "bugsnag-sessions" to "sessions",
            "user-info" to "user-info",
            "bugsnag-native" to "native",
            "bugsnag-errors" to "errors"
        )

        filesToMove.forEach { (from, to) ->
            val fromFile = File(persistenceDir, from)
            if (fromFile.exists()) {
                fromFile.renameTo(
                    File(bugsnagDir, to)
                )
            }
        }
    }
}
