package com.bugsnag.android

import com.bugsnag.android.internal.InternalMetrics
import com.bugsnag.android.internal.InternalMetricsNoop
import java.util.concurrent.CopyOnWriteArrayList

internal data class CallbackState(
    val onErrorTasks: MutableCollection<OnErrorCallback> = CopyOnWriteArrayList(),
    val onBreadcrumbTasks: MutableCollection<OnBreadcrumbCallback> = CopyOnWriteArrayList(),
    val onSessionTasks: MutableCollection<OnSessionCallback> = CopyOnWriteArrayList(),
    val onSendTasks: MutableCollection<OnSendCallback> = CopyOnWriteArrayList()
) : CallbackAware {

    private var internalMetrics: InternalMetrics = InternalMetricsNoop()

    companion object {
        private const val onBreadcrumbName = "onBreadcrumb"
        private const val onErrorName = "onError"
        private const val onSendName = "onSendError"
        private const val onSessionName = "onSession"
    }

    fun setInternalMetrics(metrics: InternalMetrics) {
        internalMetrics = metrics
        internalMetrics.setCallbackCounts(getCallbackCounts())
    }

    override fun addOnError(onError: OnErrorCallback) {
        if (onErrorTasks.add(onError)) {
            internalMetrics.notifyAddCallback(onErrorName)
        }
    }

    override fun removeOnError(onError: OnErrorCallback) {
        if (onErrorTasks.remove(onError)) {
            internalMetrics.notifyRemoveCallback(onErrorName)
        }
    }

    override fun addOnBreadcrumb(onBreadcrumb: OnBreadcrumbCallback) {
        if (onBreadcrumbTasks.add(onBreadcrumb)) {
            internalMetrics.notifyAddCallback(onBreadcrumbName)
        }
    }

    override fun removeOnBreadcrumb(onBreadcrumb: OnBreadcrumbCallback) {
        if (onBreadcrumbTasks.remove(onBreadcrumb)) {
            internalMetrics.notifyRemoveCallback(onBreadcrumbName)
        }
    }

    override fun addOnSession(onSession: OnSessionCallback) {
        if (onSessionTasks.add(onSession)) {
            internalMetrics.notifyAddCallback(onSessionName)
        }
    }

    override fun removeOnSession(onSession: OnSessionCallback) {
        if (onSessionTasks.remove(onSession)) {
            internalMetrics.notifyRemoveCallback(onSessionName)
        }
    }

    fun addOnSend(onSend: OnSendCallback) {
        if (onSendTasks.add(onSend)) {
            internalMetrics.notifyAddCallback(onSendName)
        }
    }

    fun removeOnSend(onSend: OnSendCallback) {
        if (onSendTasks.remove(onSend)) {
            internalMetrics.notifyRemoveCallback(onSendName)
        }
    }

    fun runOnErrorTasks(event: Event, logger: Logger): Boolean {
        // optimization to avoid construction of iterator when no callbacks set
        if (onErrorTasks.isEmpty()) {
            return true
        }
        onErrorTasks.forEach {
            try {
                if (!it.onError(event)) {
                    return false
                }
            } catch (ex: Throwable) {
                logger.w("OnBreadcrumbCallback threw an Exception", ex)
            }
        }
        return true
    }

    fun runOnBreadcrumbTasks(breadcrumb: Breadcrumb, logger: Logger): Boolean {
        // optimization to avoid construction of iterator when no callbacks set
        if (onBreadcrumbTasks.isEmpty()) {
            return true
        }
        onBreadcrumbTasks.forEach {
            try {
                if (!it.onBreadcrumb(breadcrumb)) {
                    return false
                }
            } catch (ex: Throwable) {
                logger.w("OnBreadcrumbCallback threw an Exception", ex)
            }
        }
        return true
    }

    fun runOnSessionTasks(session: Session, logger: Logger): Boolean {
        // optimization to avoid construction of iterator when no callbacks set
        if (onSessionTasks.isEmpty()) {
            return true
        }
        onSessionTasks.forEach {
            try {
                if (!it.onSession(session)) {
                    return false
                }
            } catch (ex: Throwable) {
                logger.w("OnSessionCallback threw an Exception", ex)
            }
        }
        return true
    }

    fun runOnSendTasks(event: Event, logger: Logger): Boolean {
        onSendTasks.forEach {
            try {
                if (!it.onSend(event)) {
                    return false
                }
            } catch (ex: Throwable) {
                logger.w("OnSendCallback threw an Exception", ex)
            }
        }
        return true
    }

    fun runOnSendTasks(eventSource: () -> Event, logger: Logger): Boolean {
        if (onSendTasks.isEmpty()) {
            // avoid constructing event from eventSource if not needed
            return true
        }

        return this.runOnSendTasks(eventSource(), logger)
    }

    fun copy() = this.copy(
        onErrorTasks = onErrorTasks,
        onBreadcrumbTasks = onBreadcrumbTasks,
        onSessionTasks = onSessionTasks,
        onSendTasks = onSendTasks
    )

    private fun getCallbackCounts(): Map<String, Int> {
        return hashMapOf<String, Int>().also { map ->
            if (onBreadcrumbTasks.count() > 0) map[onBreadcrumbName] = onBreadcrumbTasks.count()
            if (onErrorTasks.count() > 0) map[onErrorName] = onErrorTasks.count()
            if (onSendTasks.count() > 0) map[onSendName] = onSendTasks.count()
            if (onSessionTasks.count() > 0) map[onSessionName] = onSessionTasks.count()
        }
    }
}
