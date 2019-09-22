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
import android.widget.RemoteViews;

import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Widget extends AppWidgetProvider {
    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                DB db = DB.getInstance(context);
                update(context, appWidgetManager, appWidgetIds, db.message().getUnseenUnified());
            }
        });
    }

    static void update(Context context, Integer count) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, Widget.class));
        update(context, appWidgetManager, appWidgetIds, count);
    }

    private static void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Integer count) {
        NumberFormat nf = NumberFormat.getIntegerInstance();

        Intent view = new Intent(context, ActivityView.class);
        view.setAction("unified");
        view.putExtra("refresh", true);
        view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, ActivityView.REQUEST_UNIFIED, view, PendingIntent.FLAG_UPDATE_CURRENT);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            views.setOnClickPendingIntent(R.id.widget, pi);

            if (count == null)
                views.setTextViewText(R.id.tvCount, "?");
            else if (count > 99)
                views.setTextViewText(R.id.tvCount, "∞");
            else
                views.setTextViewText(R.id.tvCount, nf.format(count));

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
