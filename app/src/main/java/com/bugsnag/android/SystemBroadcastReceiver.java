package com.bugsnag.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/**
 * Used to automatically create breadcrumbs for system events
 * Broadcast actions and categories can be found in text files in the android folder
 * e.g. ~/Library/Android/sdk/platforms/android-9/data/broadcast_actions.txt
 * See http://stackoverflow.com/a/27601497
 */
class SystemBroadcastReceiver extends BroadcastReceiver {

    private static final String INTENT_ACTION_KEY = "Intent Action";

    private final Client client;
    private final Logger logger;
    private final Map<String, BreadcrumbType> actions;

    SystemBroadcastReceiver(@NonNull Client client, Logger logger) {
        this.client = client;
        this.logger = logger;
        this.actions = buildActions();
    }

    static SystemBroadcastReceiver register(final Client client,
                                            final Logger logger,
                                            BackgroundTaskService bgTaskService) {
        final SystemBroadcastReceiver receiver = new SystemBroadcastReceiver(client, logger);
        if (receiver.getActions().size() > 0) {
            try {
                bgTaskService.submitTask(TaskType.DEFAULT, new Runnable() {
                    @Override
                    public void run() {
                        IntentFilter intentFilter = receiver.getIntentFilter();
                        Context context = client.appContext;
                        ContextExtensionsKt.registerReceiverSafe(context,
                                receiver, intentFilter, logger);
                    }
                });
            } catch (RejectedExecutionException ex) {
                logger.w("Failed to register for automatic breadcrumb broadcasts", ex);
            }
            return receiver;
        } else {
            return null;
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        try {
            Map<String, Object> meta = new HashMap<>();
            String fullAction = intent.getAction();

            if (fullAction == null) {
                return;
            }

            String shortAction = shortenActionNameIfNeeded(fullAction);
            meta.put(INTENT_ACTION_KEY, fullAction); // always add the Intent Action

            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object valObj = extras.get(key);
                    if (valObj == null) {
                        continue;
                    }

                    String val = valObj.toString();

                    if (isAndroidKey(key)) { // shorten the Intent action
                        meta.put("Extra", String.format("%s: %s", shortAction, val));
                    } else {
                        meta.put(key, val);
                    }
                }
            }
            BreadcrumbType type = actions.get(fullAction);

            if (type == null) {
                type = BreadcrumbType.STATE;
            }
            client.leaveBreadcrumb(shortAction, meta, type);

        } catch (Exception ex) {
            logger.w("Failed to leave breadcrumb in SystemBroadcastReceiver: "
                    + ex.getMessage());
        }
    }

    private static boolean isAndroidKey(@NonNull String actionName) {
        return actionName.startsWith("android.");
    }

    @NonNull
    static String shortenActionNameIfNeeded(@NonNull String action) {
        if (isAndroidKey(action)) {
            return action.substring(action.lastIndexOf(".") + 1);
        } else {
            return action;
        }
    }

    /**
     * Builds a map of intent actions and their breadcrumb type (if enabled).
     *
     * Noisy breadcrumbs are omitted, along with anything that involves a state change.
     * @return the action map
     */
    @NonNull
    private Map<String, BreadcrumbType> buildActions() {

        Map<String, BreadcrumbType> actions = new HashMap<>();
        if (client.getConfig().shouldRecordBreadcrumbType(BreadcrumbType.USER)) {
            actions.put("android.appwidget.action.APPWIDGET_DELETED", BreadcrumbType.USER);
            actions.put("android.appwidget.action.APPWIDGET_DISABLED", BreadcrumbType.USER);
            actions.put("android.appwidget.action.APPWIDGET_ENABLED", BreadcrumbType.USER);
            actions.put("android.intent.action.CAMERA_BUTTON", BreadcrumbType.USER);
            actions.put("android.intent.action.CLOSE_SYSTEM_DIALOGS", BreadcrumbType.USER);
            actions.put("android.intent.action.DOCK_EVENT", BreadcrumbType.USER);
        }

        if (client.getConfig().shouldRecordBreadcrumbType(BreadcrumbType.STATE)) {
            actions.put("android.appwidget.action.APPWIDGET_HOST_RESTORED", BreadcrumbType.STATE);
            actions.put("android.appwidget.action.APPWIDGET_RESTORED", BreadcrumbType.STATE);
            actions.put("android.appwidget.action.APPWIDGET_UPDATE", BreadcrumbType.STATE);
            actions.put("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS", BreadcrumbType.STATE);
            actions.put("android.intent.action.ACTION_POWER_CONNECTED", BreadcrumbType.STATE);
            actions.put("android.intent.action.ACTION_POWER_DISCONNECTED", BreadcrumbType.STATE);
            actions.put("android.intent.action.ACTION_SHUTDOWN", BreadcrumbType.STATE);
            actions.put("android.intent.action.AIRPLANE_MODE", BreadcrumbType.STATE);
            actions.put("android.intent.action.BATTERY_LOW", BreadcrumbType.STATE);
            actions.put("android.intent.action.BATTERY_OKAY", BreadcrumbType.STATE);
            actions.put("android.intent.action.BOOT_COMPLETED", BreadcrumbType.STATE);
            actions.put("android.intent.action.CONFIGURATION_CHANGED", BreadcrumbType.STATE);
            actions.put("android.intent.action.CONTENT_CHANGED", BreadcrumbType.STATE);
            actions.put("android.intent.action.DATE_CHANGED", BreadcrumbType.STATE);
            actions.put("android.intent.action.DEVICE_STORAGE_LOW", BreadcrumbType.STATE);
            actions.put("android.intent.action.DEVICE_STORAGE_OK", BreadcrumbType.STATE);
            actions.put("android.intent.action.INPUT_METHOD_CHANGED", BreadcrumbType.STATE);
            actions.put("android.intent.action.LOCALE_CHANGED", BreadcrumbType.STATE);
            actions.put("android.intent.action.REBOOT", BreadcrumbType.STATE);
            actions.put("android.intent.action.SCREEN_OFF", BreadcrumbType.STATE);
            actions.put("android.intent.action.SCREEN_ON", BreadcrumbType.STATE);
            actions.put("android.intent.action.TIMEZONE_CHANGED", BreadcrumbType.STATE);
            actions.put("android.intent.action.TIME_SET", BreadcrumbType.STATE);
            actions.put("android.os.action.DEVICE_IDLE_MODE_CHANGED", BreadcrumbType.STATE);
            actions.put("android.os.action.POWER_SAVE_MODE_CHANGED", BreadcrumbType.STATE);
        }

        if (client.getConfig().shouldRecordBreadcrumbType(BreadcrumbType.NAVIGATION)) {
            actions.put("android.intent.action.DREAMING_STARTED", BreadcrumbType.NAVIGATION);
            actions.put("android.intent.action.DREAMING_STOPPED", BreadcrumbType.NAVIGATION);
        }

        return actions;
    }

    /**
     * @return the enabled actions
     */
    public Map<String, BreadcrumbType> getActions() {
        return actions;
    }

    /**
     * Creates a new Intent filter with all the intents to record breadcrumbs for
     *
     * @return The intent filter
     */
    @NonNull
    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();

        for (String action : actions.keySet()) {
            filter.addAction(action);
        }
        return filter;
    }

}
