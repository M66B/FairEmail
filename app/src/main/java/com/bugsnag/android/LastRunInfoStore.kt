package com.bugsnag.android

import com.bugsnag.android.internal.ImmutableConfig
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

private const val KEY_VALUE_DELIMITER = "="
private const val KEY_CONSECUTIVE_LAUNCH_CRASHES = "consecutiveLaunchCrashes"
private const val KEY_CRASHED = "crashed"
private const val KEY_CRASHED_DURING_LAUNCH = "crashedDuringLaunch"

/**
 * Persists/loads [LastRunInfo] on disk, which allows Bugsnag to determine
 * whether the previous application launch crashed or not. This class is thread-safe.
 */
internal class LastRunInfoStore(config: ImmutableConfig) {

    val file: File = File(config.persistenceDirectory.value, "bugsnag/last-run-info")
    private val logger: Logger = config.logger
    private val lock = ReentrantReadWriteLock()

    fun persist(lastRunInfo: LastRunInfo) {
        lock.writeLock().withLock {
            try {
                persistImpl(lastRunInfo)
            } catch (exc: Throwable) {
                logger.w("Unexpectedly failed to persist LastRunInfo.", exc)
            }
        }
    }

    private fun persistImpl(lastRunInfo: LastRunInfo) {
        val text = KeyValueWriter().apply {
            add(KEY_CONSECUTIVE_LAUNCH_CRASHES, lastRunInfo.consecutiveLaunchCrashes)
            add(KEY_CRASHED, lastRunInfo.crashed)
            add(KEY_CRASHED_DURING_LAUNCH, lastRunInfo.crashedDuringLaunch)
        }.toString()
        file.parentFile?.mkdirs()
        file.writeText(text)
        logger.d("Persisted: $text")
    }

    fun load(): LastRunInfo? {
        return lock.readLock().withLock {
            try {
                loadImpl()
            } catch (exc: Throwable) {
                logger.w("Unexpectedly failed to load LastRunInfo.", exc)
                null
            }
        }
    }

    private fun loadImpl(): LastRunInfo? {
        if (!file.exists()) {
            return null
        }

        val lines = file.readText().split("\n").filter { it.isNotBlank() }

        if (lines.size != 3) {
            logger.w("Unexpected number of lines when loading LastRunInfo. Skipping load. $lines")
            return null
        }

        return try {
            val consecutiveLaunchCrashes = lines[0].asIntValue(KEY_CONSECUTIVE_LAUNCH_CRASHES)
            val crashed = lines[1].asBooleanValue(KEY_CRASHED)
            val crashedDuringLaunch = lines[2].asBooleanValue(KEY_CRASHED_DURING_LAUNCH)
            val runInfo = LastRunInfo(consecutiveLaunchCrashes, crashed, crashedDuringLaunch)
            logger.d("Loaded: $runInfo")
            runInfo
        } catch (exc: NumberFormatException) {
            // unlikely case where information was serialized incorrectly
            logger.w("Failed to read consecutiveLaunchCrashes from saved lastRunInfo", exc)
            null
        }
    }

    private fun String.asIntValue(key: String) =
        substringAfter("$key$KEY_VALUE_DELIMITER").toInt()

    private fun String.asBooleanValue(key: String) =
        substringAfter("$key$KEY_VALUE_DELIMITER").toBoolean()
}

private class KeyValueWriter {

    private val sb = StringBuilder()

    fun add(key: String, value: Any) {
        sb.append("$key$KEY_VALUE_DELIMITER$value")
        sb.append("\n")
    }

    override fun toString() = sb.toString()
}
