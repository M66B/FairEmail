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
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Widget extends AppWidgetProvider {
    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "widget");

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean unseen_ignored = prefs.getBoolean("unseen_ignored", false);

                DB db = DB.getInstance(context);
                NumberFormat nf = NumberFormat.getIntegerInstance();

                for (int appWidgetId : appWidgetIds) {
                    long account = prefs.getLong("widget." + appWidgetId + ".account", -1L);
                    String name = prefs.getString("widget." + appWidgetId + ".name", null);

                    List<EntityFolder> folders = db.folder().getNotifyingFolders(account);
                    if (folders == null)
                        folders = new ArrayList<>();

                    PendingIntent pi;
                    if (folders.size() == 1) {
                        Intent view = new Intent(context, ActivityView.class);
                        view.setAction("folder:" + folders.get(0).id);
                        view.putExtra("account", account);
                        view.putExtra("type", folders.get(0).type);
                        view.putExtra("refresh", true);
                        view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        pi = PendingIntent.getActivity(context, appWidgetId, view, PendingIntent.FLAG_UPDATE_CURRENT);
                    } else {
                        if (account < 0) {
                            Intent view = new Intent(context, ActivityView.class);
                            view.setAction("unified");
                            view.putExtra("refresh", true);
                            view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            pi = PendingIntent.getActivity(context, ActivityView.REQUEST_UNIFIED, view, PendingIntent.FLAG_UPDATE_CURRENT);
                        } else {
                            Intent view = new Intent(context, ActivityView.class);
                            view.setAction("folders:" + account);
                            view.putExtra("refresh", true);
                            view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            pi = PendingIntent.getActivity(context, appWidgetId, view, PendingIntent.FLAG_UPDATE_CURRENT);
                        }
                    }

                    TupleMessageStats stats = db.message().getWidgetUnseen(account < 0 ? null : account);
                    Integer unseen = (unseen_ignored ? stats.notifying : stats.unseen);
                    if (unseen == null)
                        unseen = 0;

                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

                    views.setOnClickPendingIntent(R.id.widget, pi);

                    views.setImageViewResource(R.id.ivMessage, unseen == 0
                            ? R.drawable.baseline_mail_outline_24
                            : R.drawable.baseline_mail_24);
                    views.setTextViewText(R.id.tvCount, unseen < 100 ? nf.format(unseen) : "∞");

                    if (!TextUtils.isEmpty(name)) {
                        views.setTextViewText(R.id.tvAccount, name);
                        views.setViewVisibility(R.id.tvAccount, ViewStripe.VISIBLE);
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        });
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

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, Widget.class));

        Intent intent = new Intent(context, Widget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }
}
