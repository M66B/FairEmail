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
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

public class WidgetUnified extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int colorWidgetForeground = context.getResources().getColor(R.color.colorWidgetForeground);
        int lightColorSeparator = context.getResources().getColor(R.color.lightColorSeparator);
        int darkColorSeparator = context.getResources().getColor(R.color.darkColorSeparator);

        for (int appWidgetId : appWidgetIds) {
            String name = prefs.getString("widget." + appWidgetId + ".name", null);
            long account = prefs.getLong("widget." + appWidgetId + ".account", -1L);
            long folder = prefs.getLong("widget." + appWidgetId + ".folder", -1L);
            String type = prefs.getString("widget." + appWidgetId + ".type", null);
            boolean daynight = prefs.getBoolean("widget." + appWidgetId + ".daynight", false);
            boolean semi = prefs.getBoolean("widget." + appWidgetId + ".semi", true);
            int background = prefs.getInt("widget." + appWidgetId + ".background", Color.TRANSPARENT);
            boolean separators = prefs.getBoolean("widget." + appWidgetId + ".separators", true);
            int font = prefs.getInt("widget." + appWidgetId + ".font", 0);
            int padding = prefs.getInt("widget." + appWidgetId + ".padding", 0);
            boolean caption = prefs.getBoolean("widget." + appWidgetId + ".caption", true);
            boolean refresh = prefs.getBoolean("widget." + appWidgetId + ".refresh", false);
            boolean compose = prefs.getBoolean("widget." + appWidgetId + ".compose", false);
            boolean standalone = prefs.getBoolean("widget." + appWidgetId + ".standalone", false);
            int version = prefs.getInt("widget." + appWidgetId + ".version", 0);

            if (version <= 1550)
                semi = true; // Legacy
            if (font == 0)
                font = 2; // Default medium
            if (padding == 0)
                padding = 2; // Default medium

            Intent view = new Intent(context, ActivityView.class);
            if (account < 0 && folder < 0 && type == null)
                view.setAction("unified");
            else {
                view.setAction("folder:" + folder);
                view.putExtra("account", account);
                view.putExtra("type", type);
            }
            view.putExtra("standalone", standalone);
            view.putExtra("refresh", true);
            view.putExtra("version", version);
            view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pi = PendingIntentCompat.getActivity(
                    context, appWidgetId, view, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent sync = new Intent(context, ServiceUI.class);
            sync.setAction("widget:" + appWidgetId);
            sync.putExtra("account", account);
            sync.putExtra("folder", folder);
            PendingIntent piSync = PendingIntentCompat.getService(
                    context, appWidgetId, sync, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent edit = new Intent(context, ActivityCompose.class);
            edit.setAction("widget:" + appWidgetId);
            edit.putExtra("action", "new");
            edit.putExtra("account", account);
            edit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent piCompose = PendingIntentCompat.getActivity(
                    context, appWidgetId, edit, PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_unified);

            views.setTextViewTextSize(R.id.title, TypedValue.COMPLEX_UNIT_SP, getFontSizeSp(font));

            int px = getPaddingPx(padding, context);
            views.setViewPadding(R.id.title, px, px, px, px);

            if (caption) {
                if (name == null)
                    views.setTextViewText(R.id.title, context.getString(R.string.title_folder_unified));
                else
                    views.setTextViewText(R.id.title, name);
            } else
                views.setTextViewText(R.id.title, null);
            views.setViewVisibility(R.id.title, caption || refresh || compose ? View.VISIBLE : View.GONE);
            views.setViewVisibility(R.id.padding, caption || refresh || compose ? View.GONE : View.VISIBLE);

            views.setOnClickPendingIntent(R.id.title, pi);

            views.setViewVisibility(R.id.refresh, refresh ? View.VISIBLE : View.GONE);
            views.setViewPadding(R.id.refresh, px, px, px, px);
            views.setOnClickPendingIntent(R.id.refresh, piSync);

            views.setViewVisibility(R.id.compose, compose ? View.VISIBLE : View.GONE);
            views.setViewPadding(R.id.compose, px, px, px, px);
            views.setOnClickPendingIntent(R.id.compose, piCompose);

            Intent service = new Intent(context, WidgetUnifiedService.class);
            service.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            service.setData(Uri.parse(service.toUri(Intent.URI_INTENT_SCHEME)));

            views.setRemoteAdapter(R.id.lv, service);

            Intent thread = new Intent(context, ActivityView.class);
            thread.setPackage(BuildConfig.APPLICATION_ID);
            thread.setAction("widget:" + appWidgetId);
            thread.putExtra("widget_account", account);
            thread.putExtra("widget_folder", folder);
            thread.putExtra("widget_type", type);
            thread.putExtra("filter_archive", !EntityFolder.ARCHIVE.equals(type));
            thread.putExtra("standalone", standalone);
            thread.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent piItem = PendingIntentCompat.getActivity(
                    context, ActivityView.PI_WIDGET, thread, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            views.setPendingIntentTemplate(R.id.lv, piItem);

            boolean syncing = prefs.getBoolean("widget." + appWidgetId + ".syncing", false);
            views.setImageViewResource(R.id.refresh, syncing
                    ? R.drawable.twotone_compare_arrows_24
                    : R.drawable.twotone_sync_24);

            // https://developer.android.com/guide/topics/ui/look-and-feel/darktheme
            if (!daynight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setColorStateListAttr(R.id.background, "setBackgroundTintList", 0);
                views.setColorStateListAttr(R.id.separator, "setBackgroundTintList", 0);
            }

            if (daynight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setInt(R.id.background, "setBackgroundColor", Color.WHITE);
                views.setColorStateListAttr(R.id.background, "setBackgroundTintList", android.R.attr.colorBackground);
                views.setColorStateListAttr(R.id.title, "setTextColor", android.R.attr.textColorPrimary);
                views.setInt(R.id.separator, "setBackgroundColor", Color.WHITE);
                views.setColorStateListAttr(R.id.separator, "setBackgroundTintList", android.R.attr.colorControlNormal);
                views.setColorAttr(R.id.refresh, "setColorFilter", android.R.attr.textColorPrimary);
                views.setColorAttr(R.id.compose, "setColorFilter", android.R.attr.textColorPrimary);
            } else if (background == Color.TRANSPARENT) {
                if (semi)
                    views.setInt(R.id.background, "setBackgroundResource", R.drawable.widget_background);
                else
                    views.setInt(R.id.background, "setBackgroundColor", background);

                views.setTextColor(R.id.title, colorWidgetForeground);
                views.setInt(R.id.separator, "setBackgroundColor", lightColorSeparator);
                views.setInt(R.id.refresh, "setColorFilter", colorWidgetForeground);
                views.setInt(R.id.compose, "setColorFilter", colorWidgetForeground);
            } else {
                float lum = (float) ColorUtils.calculateLuminance(background);

                if (semi)
                    background = ColorUtils.setAlphaComponent(background, 127);

                views.setInt(R.id.background, "setBackgroundColor", background);

                int fg = (lum > 0.7f ? Color.BLACK : colorWidgetForeground);
                views.setTextColor(R.id.title, fg);
                views.setInt(R.id.separator, "setBackgroundColor",
                        lum > 0.7f ? darkColorSeparator : lightColorSeparator);
                views.setInt(R.id.refresh, "setColorFilter", fg);
                views.setInt(R.id.compose, "setColorFilter", fg);
            }

            views.setViewVisibility(R.id.separator, caption && separators ? View.VISIBLE : View.GONE);

            int dp6 = Helper.dp2pixels(context, 6);
            views.setViewPadding(R.id.content, dp6, 0, dp6, 0);

            appWidgetManager.updateAppWidget(appWidgetId, views);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv);
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

    static int getFontSizeSp(int size) {
        switch (size) {
            case 1: // small
                return 14;
            case 3: // large
                return 22;
            case 4: // tiny
                return 10;
            default: // medium
                return 18;
        }
    }

    static int getPaddingPx(int padding, Context context) {
        switch (padding) {
            case 1: // small
                return Helper.dp2pixels(context, 3);
            case 3: // large
                return Helper.dp2pixels(context, 9);
            case 4: // tiny
                return Helper.dp2pixels(context, 1);
            default: // medium
                return Helper.dp2pixels(context, 6);
        }
    }

    static void init(Context context, int appWidgetId) {
        Log.i("Widget unified init=" + appWidgetId);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager == null) {
            Log.w("No app widget manager"); // Fairphone FP2
            return;
        }

        Intent intent = new Intent(context, WidgetUnified.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        context.sendBroadcast(intent);
    }

    static void updateData(Context context) {
        Log.i("Widget unified update");
        if (ActivityBilling.isPro(context)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            if (appWidgetManager == null) {
                Log.w("No app widget manager"); // Fairphone FP2
                return;
            }

            try {
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetUnified.class));
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv);
                EntityLog.log(context, "Updated widget data count=" + appWidgetIds.length);
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
}
