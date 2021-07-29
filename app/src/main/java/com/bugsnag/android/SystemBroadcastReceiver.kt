package com.bugsnag.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.util.HashMap

/**
 * Used to automatically create breadcrumbs for system events
 * Broadcast actions and categories can be found in text files in the android folder
 * e.g. ~/Library/Android/sdk/platforms/android-9/data/broadcast_actions.txt
 * See http://stackoverflow.com/a/27601497
 */
internal class SystemBroadcastReceiver(
    private val client: Client,
    private val logger: Logger
) : BroadcastReceiver() {

    companion object {
        private const val INTENT_ACTION_KEY = "Intent Action"

        @JvmStatic
        fun register(ctx: Context, receiver: SystemBroadcastReceiver, logger: Logger) {
            if (receiver.actions.isNotEmpty()) {
                val filter = IntentFilter()
                receiver.actions.keys.forEach(filter::addAction)
                ctx.registerReceiverSafe(receiver, filter, logger)
            }
        }

        fun isAndroidKey(actionName: String): Boolean {
            return actionName.startsWith("android.")
        }

        fun shortenActionNameIfNeeded(action: String): String {
            return if (isAndroidKey(action)) {
                action.substringAfterLast('.')
            } else {
                action
            }
        }
    }

    val actions: Map<String, BreadcrumbType> = buildActions()

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val meta: MutableMap<String, Any> = HashMap()
            val fullAction = intent.action ?: return
            val shortAction = shortenActionNameIfNeeded(fullAction)
            meta[INTENT_ACTION_KEY] = fullAction // always add the Intent Action
            addExtrasToMetadata(intent, meta, shortAction)

            val type = actions[fullAction] ?: BreadcrumbType.STATE
            client.leaveBreadcrumb(shortAction, meta, type)
        } catch (ex: Exception) {
            logger.w("Failed to leave breadcrumb in SystemBroadcastReceiver: ${ex.message}")
        }
    }

    private fun addExtrasToMetadata(
        intent: Intent,
        meta: MutableMap<String, Any>,
        shortAction: String
    ) {
        val extras = intent.extras
        extras?.keySet()?.forEach { key ->
            val valObj = extras[key] ?: return@forEach
            val strVal = valObj.toString()
            if (isAndroidKey(key)) { // shorten the Intent action
                meta["Extra"] = "$shortAction: $strVal"
            } else {
                meta[key] = strVal
            }
        }
    }

    /**
     * Builds a map of intent actions and their breadcrumb type (if enabled).
     *
     * Noisy breadcrumbs are omitted, along with anything that involves a state change.
     * @return the action map
     */
    private fun buildActions(): Map<String, BreadcrumbType> {
        val actions: MutableMap<String, BreadcrumbType> = HashMap()
        val config = client.config

        if (!config.shouldDiscardBreadcrumb(BreadcrumbType.USER)) {
            actions["android.appwidget.action.APPWIDGET_DELETED"] = BreadcrumbType.USER
            actions["android.appwidget.action.APPWIDGET_DISABLED"] = BreadcrumbType.USER
            actions["android.appwidget.action.APPWIDGET_ENABLED"] = BreadcrumbType.USER
            actions["android.intent.action.CAMERA_BUTTON"] = BreadcrumbType.USER
            actions["android.intent.action.CLOSE_SYSTEM_DIALOGS"] = BreadcrumbType.USER
            actions["android.intent.action.DOCK_EVENT"] = BreadcrumbType.USER
        }
        if (!config.shouldDiscardBreadcrumb(BreadcrumbType.STATE)) {
            actions["android.appwidget.action.APPWIDGET_HOST_RESTORED"] = BreadcrumbType.STATE
            actions["android.appwidget.action.APPWIDGET_RESTORED"] = BreadcrumbType.STATE
            actions["android.appwidget.action.APPWIDGET_UPDATE"] = BreadcrumbType.STATE
            actions["android.appwidget.action.APPWIDGET_UPDATE_OPTIONS"] = BreadcrumbType.STATE
            actions["android.intent.action.ACTION_POWER_CONNECTED"] = BreadcrumbType.STATE
            actions["android.intent.action.ACTION_POWER_DISCONNECTED"] = BreadcrumbType.STATE
            actions["android.intent.action.ACTION_SHUTDOWN"] = BreadcrumbType.STATE
            actions["android.intent.action.AIRPLANE_MODE"] = BreadcrumbType.STATE
            actions["android.intent.action.BATTERY_LOW"] = BreadcrumbType.STATE
            actions["android.intent.action.BATTERY_OKAY"] = BreadcrumbType.STATE
            actions["android.intent.action.BOOT_COMPLETED"] = BreadcrumbType.STATE
            actions["android.intent.action.CONFIGURATION_CHANGED"] = BreadcrumbType.STATE
            actions["android.intent.action.CONTENT_CHANGED"] = BreadcrumbType.STATE
            actions["android.intent.action.DATE_CHANGED"] = BreadcrumbType.STATE
            actions["android.intent.action.DEVICE_STORAGE_LOW"] = BreadcrumbType.STATE
            actions["android.intent.action.DEVICE_STORAGE_OK"] = BreadcrumbType.STATE
            actions["android.intent.action.INPUT_METHOD_CHANGED"] = BreadcrumbType.STATE
            actions["android.intent.action.LOCALE_CHANGED"] = BreadcrumbType.STATE
            actions["android.intent.action.REBOOT"] = BreadcrumbType.STATE
            actions["android.intent.action.SCREEN_OFF"] = BreadcrumbType.STATE
            actions["android.intent.action.SCREEN_ON"] = BreadcrumbType.STATE
            actions["android.intent.action.TIMEZONE_CHANGED"] = BreadcrumbType.STATE
            actions["android.intent.action.TIME_SET"] = BreadcrumbType.STATE
            actions["android.os.action.DEVICE_IDLE_MODE_CHANGED"] = BreadcrumbType.STATE
            actions["android.os.action.POWER_SAVE_MODE_CHANGED"] = BreadcrumbType.STATE
        }
        if (!config.shouldDiscardBreadcrumb(BreadcrumbType.NAVIGATION)) {
            actions["android.intent.action.DREAMING_STARTED"] = BreadcrumbType.NAVIGATION
            actions["android.intent.action.DREAMING_STOPPED"] = BreadcrumbType.NAVIGATION
        }
        return actions
    }
}
