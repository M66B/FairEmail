package com.bugsnag.android

import java.util.concurrent.CopyOnWriteArrayList

internal data class CallbackState(
    val onErrorTasks: MutableCollection<OnErrorCallback> = CopyOnWriteArrayList(),
    val onBreadcrumbTasks: MutableCollection<OnBreadcrumbCallback> = CopyOnWriteArrayList(),
    val onSessionTasks: MutableCollection<OnSessionCallback> = CopyOnWriteArrayList(),
    val onSendTasks: MutableCollection<OnSendCallback> = CopyOnWriteArrayList()
) : CallbackAware {

    override fun addOnError(onError: OnErrorCallback) {
        onErrorTasks.add(onError)
    }

    override fun removeOnError(onError: OnErrorCallback) {
        onErrorTasks.remove(onError)
    }

    override fun addOnBreadcrumb(onBreadcrumb: OnBreadcrumbCallback) {
        onBreadcrumbTasks.add(onBreadcrumb)
    }

    override fun removeOnBreadcrumb(onBreadcrumb: OnBreadcrumbCallback) {
        onBreadcrumbTasks.remove(onBreadcrumb)
    }

    override fun addOnSession(onSession: OnSessionCallback) {
        onSessionTasks.add(onSession)
    }

    override fun removeOnSession(onSession: OnSessionCallback) {
        onSessionTasks.remove(onSession)
    }

    fun addOnSend(onSend: OnSendCallback) {
        onSendTasks.add(onSend)
    }

    fun removeOnSend(onSend: OnSendCallback) {
        onSendTasks.remove(onSend)
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
}
