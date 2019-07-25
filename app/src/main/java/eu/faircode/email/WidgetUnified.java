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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class WidgetUnified extends AppWidgetProvider {
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        Intent view = new Intent(context, ActivityView.class);
        view.setAction("unified");
        view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, ActivityView.REQUEST_UNIFIED, view, PendingIntent.FLAG_UPDATE_CURRENT);

        for (int id : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_unified);

            views.setOnClickPendingIntent(R.id.title, pi);

            Intent service = new Intent(context, WidgetUnifiedService.class);
            service.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
            service.setData(Uri.parse(service.toUri(Intent.URI_INTENT_SCHEME)));

            views.setRemoteAdapter(R.id.lv, service);

            Intent thread = new Intent(context, ActivityView.class);
            thread.setAction("widget");
            thread.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent piItem = PendingIntent.getActivity(
                    context, ActivityView.REQUEST_WIDGET, thread, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setPendingIntentTemplate(R.id.lv, piItem);

            appWidgetManager.updateAppWidget(id, views);
        }
    }

    static void update(Context context) {
        Log.i("Widget unified update");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetUnified.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv);
    }
}
