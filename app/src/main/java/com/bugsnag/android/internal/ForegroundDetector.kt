package com.bugsnag.android.internal

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import java.lang.ref.WeakReference
import kotlin.math.max

internal object ForegroundDetector : ActivityLifecycleCallbacks, Handler.Callback {

    /**
     * Same as `androidx.lifecycle.ProcessLifecycleOwner` and is used to avoid reporting
     * background / foreground changes when there is only 1 Activity being restarted for configuration
     * changes.
     */
    @VisibleForTesting
    internal const val BACKGROUND_TIMEOUT_MS = 700L

    /**
     * `Message.what` used to send the "in background" notification event. The `arg1` and `arg2`
     * contain the actual timestamp (relative to [SystemClock.elapsedRealtime()]) split into `int`
     * values.
     */
    @VisibleForTesting
    internal const val MSG_SEND_BACKGROUND = 1

    private const val INT_MASK = 0xffffffffL

    /**
     * We weak-ref all of the listeners to avoid keeping Client instances around forever. The
     * references are cleaned up each time we iterate over the list to notify the listeners.
     */
    private val listeners = ArrayList<WeakReference<OnActivityCallback>>()

    private val mainThreadHandler = Handler(Looper.getMainLooper(), this)

    private var observedApplication: Application? = null

    /**
     * The number of Activity instances: `onActivityCreated` - `onActivityDestroyed`
     */
    private var activityInstanceCount: Int = 0

    /**
     * The number of started Activity instances: `onActivityStarted` - `onActivityStopped`
     */
    private var startedActivityCount: Int = 0

    private var waitingForActivityRestart: Boolean = false

    /**
     * Marks the timestamp (relative to [SystemClock.elapsedRealtime]) that we initialised for the
     * first time.
     */
    internal val startupTime = SystemClock.elapsedRealtime()

    @VisibleForTesting
    internal var backgroundSent = true

    @JvmStatic
    var isInForeground: Boolean = false
        @VisibleForTesting
        internal set

    // This most recent time an Activity was stopped.
    @Volatile
    @JvmStatic
    var lastExitedForegroundMs = 0L

    // The first Activity in this 'session' was started at this time.
    @Volatile
    @JvmStatic
    var lastEnteredForegroundMs = 0L

    @JvmStatic
    fun registerOn(application: Application) {
        if (application === observedApplication) {
            return
        }

        observedApplication?.unregisterActivityLifecycleCallbacks(this)
        observedApplication = application
        application.registerActivityLifecycleCallbacks(this)
    }

    @JvmStatic
    @JvmOverloads
    fun registerActivityCallbacks(
        callbacks: OnActivityCallback,
        notifyCurrentState: Boolean = true,
    ) {
        synchronized(listeners) {
            listeners.add(WeakReference(callbacks))
        }

        if (notifyCurrentState) {
            callbacks.onForegroundStatus(
                isInForeground,
                if (isInForeground) lastEnteredForegroundMs else lastExitedForegroundMs
            )
        }
    }

    private inline fun notifyListeners(sendCallback: (OnActivityCallback) -> Unit) {
        synchronized(listeners) {
            if (listeners.isEmpty()) {
                return
            }

            try {
                val iterator = listeners.iterator()
                while (iterator.hasNext()) {
                    val ref = iterator.next()
                    val listener = ref.get()
                    if (listener == null) {
                        iterator.remove()
                    } else {
                        sendCallback(listener)
                    }
                }
            } catch (e: Exception) {
                // ignore callback errors
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityInstanceCount++
    }

    override fun onActivityStarted(activity: Activity) {
        if (startedActivityCount == 0 && !waitingForActivityRestart) {
            val startedTimestamp = SystemClock.elapsedRealtime()
            notifyListeners { it.onForegroundStatus(true, startedTimestamp) }
            lastEnteredForegroundMs = startedTimestamp
        }

        startedActivityCount++
        mainThreadHandler.removeMessages(MSG_SEND_BACKGROUND)
        isInForeground = true
        waitingForActivityRestart = false

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            notifyListeners { it.onActivityStarted(activity) }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount = max(0, startedActivityCount - 1)

        if (startedActivityCount == 0) {
            val stoppedTimestamp = SystemClock.elapsedRealtime()
            if (activity.isChangingConfigurations) {
                // isChangingConfigurations indicates that the Activity will be restarted
                // immediately, but we post a slightly delayed Message (with the current timestamp)
                // to handle cases where (for whatever reason) that doesn't happen
                // this follows the same logic as ProcessLifecycleOwner
                waitingForActivityRestart = true

                val backgroundMessage = mainThreadHandler.obtainMessage(MSG_SEND_BACKGROUND)
                backgroundMessage.timestamp = stoppedTimestamp
                mainThreadHandler.sendMessageDelayed(backgroundMessage, BACKGROUND_TIMEOUT_MS)
            } else {
                notifyListeners { it.onForegroundStatus(false, stoppedTimestamp) }
                isInForeground = false
                lastExitedForegroundMs = stoppedTimestamp
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            notifyListeners { it.onActivityStopped(activity) }
        }
    }

    override fun onActivityPostStarted(activity: Activity) {
        notifyListeners { it.onActivityStarted(activity) }
    }

    override fun onActivityPostStopped(activity: Activity) {
        notifyListeners { it.onActivityStopped(activity) }
    }

    override fun onActivityDestroyed(activity: Activity) {
        activityInstanceCount = max(0, activityInstanceCount - 1)
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what != MSG_SEND_BACKGROUND) {
            return false
        }

        waitingForActivityRestart = false

        if (!backgroundSent) {
            isInForeground = false
            backgroundSent = true

            val backgroundedTimestamp = msg.timestamp
            notifyListeners { it.onForegroundStatus(false, backgroundedTimestamp) }
            lastExitedForegroundMs = backgroundedTimestamp
        }

        return true
    }

    private var Message.timestamp: Long
        get() = (arg1.toLong() shl Int.SIZE_BITS) or arg2.toLong()
        set(timestamp) {
            arg1 = ((timestamp ushr Int.SIZE_BITS) and INT_MASK).toInt()
            arg2 = (timestamp and INT_MASK).toInt()
        }

    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    interface OnActivityCallback {
        fun onForegroundStatus(foreground: Boolean, timestamp: Long)

        fun onActivityStarted(activity: Activity)

        fun onActivityStopped(activity: Activity)
    }
}
