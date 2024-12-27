package com.bugsnag.android.internal

import java.io.File

internal object BugsnagStoreMigrator {

    @JvmStatic
    fun migrateLegacyFiles(persistenceDir: Lazy<File>): File {
        val originalDir = persistenceDir.value
        val bugsnagDir = File(originalDir, "bugsnag")
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
            val fromFile = File(originalDir, from)
            if (fromFile.exists()) {
                fromFile.renameTo(File(bugsnagDir, to))
            }
        }

        return bugsnagDir
    }
}
