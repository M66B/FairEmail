package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

public class WidgetSync extends AppWidgetProvider {
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        boolean connected = prefs.getBoolean("connected", false);

        try {
            Intent intent = new Intent(context, ServiceSynchronize.class)
                    .setAction("enable")
                    .putExtra("enabled", !enabled);
            PendingIntent pi = PendingIntentCompat.getForegroundService(
                    context, ServiceSynchronize.PI_ENABLE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            int colorWidgetForeground = context.getResources().getColor(R.color.colorWidgetForeground);

            for (int appWidgetId : appWidgetIds) {
                boolean daynight = prefs.getBoolean("widget." + appWidgetId + ".daynight", false);
                boolean semi = prefs.getBoolean("widget." + appWidgetId + ".semi", true);
                int background = prefs.getInt("widget." + appWidgetId + ".background", Color.TRANSPARENT);
                int version = prefs.getInt("widget." + appWidgetId + ".version", 0);

                if (version <= 1550)
                    semi = true; // Legacy

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_sync);
                views.setOnClickPendingIntent(R.id.ivSync, pi);

                views.setImageViewResource(R.id.ivSync, enabled ? R.drawable.twotone_sync_24 : R.drawable.twotone_sync_disabled_24);
                views.setInt(R.id.ivSync, "setImageAlpha",
                        !enabled || connected ? 255 : Math.round(Helper.LOW_LIGHT * 255));

                if (!daynight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    views.setColorStateListAttr(R.id.background, "setBackgroundTintList", 0);

                if (daynight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    views.setInt(R.id.background, "setBackgroundColor", Color.WHITE);
                    views.setColorStateListAttr(R.id.background, "setBackgroundTintList", android.R.attr.colorBackground);
                    views.setColorAttr(R.id.ivSync, "setColorFilter", android.R.attr.textColorPrimary);
                } else if (background == Color.TRANSPARENT) {
                    if (semi)
                        views.setInt(R.id.background, "setBackgroundResource", R.drawable.widget_background);
                    else
                        views.setInt(R.id.background, "setBackgroundColor", background);
                    views.setInt(R.id.ivSync, "setColorFilter", colorWidgetForeground);
                } else {
                    float lum = (float) ColorUtils.calculateLuminance(background);

                    if (semi)
                        background = ColorUtils.setAlphaComponent(background, 127);

                    views.setInt(R.id.background, "setBackgroundColor", background);

                    int fg = (lum > 0.7f ? Color.BLACK : colorWidgetForeground);
                    views.setInt(R.id.ivSync, "setColorFilter", fg);
                }

                int dp6 = Helper.dp2pixels(context, 6);
                views.setViewPadding(R.id.content, dp6, dp6, dp6, dp6);

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            for (int appWidgetId : appWidgetIds) {
                String prefix = "widget." + appWidgetId + ".";
                for (String key : prefs.getAll().keySet())
                    if (key.startsWith(prefix))
                        editor.remove(key);
            }
            editor.apply();
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static void init(Context context, int appWidgetId) {
        update(context);
    }

    static void update(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager == null) {
            Log.w("No app widget manager"); // Fairphone FP2
            return;
        }

        try {
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetSync.class));

            Intent intent = new Intent(context, WidgetSync.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            context.sendBroadcast(intent);
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.RuntimeException: system server dead?
                  at android.appwidget.AppWidgetManager.getAppWidgetIds(AppWidgetManager.java:1053)
                  at eu.faircode.email.Widget.update(SourceFile:111)
                  at eu.faircode.email.ServiceSynchronize$6.onChanged(SourceFile:460)
                  at eu.faircode.email.ServiceSynchronize$6.onChanged(SourceFile:439)
                  at androidx.lifecycle.LiveData.considerNotify(SourceFile:131)
                  at androidx.lifecycle.LiveData.dispatchingValue(SourceFile:149)
                  at androidx.lifecycle.LiveData.setValue(SourceFile:307)
                  at androidx.lifecycle.LiveData$1.run(SourceFile:91)
                Caused by: android.os.DeadObjectException
             */
        }
    }
}
