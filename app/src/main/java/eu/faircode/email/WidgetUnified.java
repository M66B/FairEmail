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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

public class WidgetUnified extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean pro = ActivityBilling.isPro(context);
        for (int appWidgetId : appWidgetIds) {
            long account = prefs.getLong("widget." + appWidgetId + ".account", -1L);
            long folder = prefs.getLong("widget." + appWidgetId + ".folder", -1L);
            String type = prefs.getString("widget." + appWidgetId + ".type", null);

            Intent view = new Intent(context, ActivityView.class);
            view.setAction("folder:" + folder);
            view.putExtra("account", account);
            view.putExtra("type", type);
            view.putExtra("refresh", true);
            view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pi = PendingIntent.getActivity(context, appWidgetId, view, PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_unified);

            views.setViewVisibility(R.id.pro, pro ? View.GONE : View.VISIBLE);
            if (pro) {
                String name = prefs.getString("widget." + appWidgetId + ".name", null);
                if (name == null)
                    views.setTextViewText(R.id.title, context.getString(R.string.title_folder_unified));
                else
                    views.setTextViewText(R.id.title, name);

                views.setOnClickPendingIntent(R.id.title, pi);

                Intent service = new Intent(context, WidgetUnifiedService.class);
                service.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                service.setData(Uri.parse(service.toUri(Intent.URI_INTENT_SCHEME)));

                views.setRemoteAdapter(R.id.lv, service);

                Intent thread = new Intent(context, ActivityView.class);
                thread.setAction("widget");
                thread.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent piItem = PendingIntent.getActivity(
                        context, ActivityView.REQUEST_WIDGET, thread, PendingIntent.FLAG_UPDATE_CURRENT);

                views.setPendingIntentTemplate(R.id.lv, piItem);
            } else
                views.setTextViewText(R.id.pro, context.getText(R.string.title_pro_feature));

            appWidgetManager.updateAppWidget(appWidgetId, views);
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

            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetUnified.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv);
        }
    }
}
