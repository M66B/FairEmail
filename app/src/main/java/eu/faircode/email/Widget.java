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
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Widget extends AppWidgetProvider {
    private static final ExecutorService executor = Helper.getBackgroundExecutor(1, "widget");

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean unseen_ignored = prefs.getBoolean("unseen_ignored", false);

                DB db = DB.getInstance(context);
                NumberFormat nf = NumberFormat.getIntegerInstance();
                int colorWidgetForeground = context.getResources().getColor(R.color.colorWidgetForeground);

                for (int appWidgetId : appWidgetIds) {
                    String name = prefs.getString("widget." + appWidgetId + ".name", null);
                    long account = prefs.getLong("widget." + appWidgetId + ".account", -1L);
                    long folder = prefs.getLong("widget." + appWidgetId + ".folder", -1L);
                    boolean daynight = prefs.getBoolean("widget." + appWidgetId + ".daynight", false);
                    boolean semi = prefs.getBoolean("widget." + appWidgetId + ".semi", true);
                    int background = prefs.getInt("widget." + appWidgetId + ".background", Color.TRANSPARENT);
                    int foreground = prefs.getInt("widget." + appWidgetId + ".foreground", Color.TRANSPARENT);
                    int layout = prefs.getInt("widget." + appWidgetId + ".layout", 0);
                    boolean top = prefs.getBoolean("widget." + appWidgetId + ".top", false);
                    int size = prefs.getInt("widget." + appWidgetId + ".text_size", -1);
                    boolean standalone = prefs.getBoolean("widget." + appWidgetId + ".standalone", false);
                    int version = prefs.getInt("widget." + appWidgetId + ".version", 0);

                    if (version <= 1550)
                        semi = true; // Legacy

                    List<EntityFolder> folders = null;
                    if (folder < 0)
                        folders = db.folder().getNotifyingFolders(account);
                    else {
                        EntityFolder f = db.folder().getFolder(folder);
                        if (f != null)
                            folders = Arrays.asList(f);
                    }
                    if (folders == null)
                        folders = new ArrayList<>();

                    PendingIntent pi;
                    if (folders.size() == 1) {
                        Intent view = new Intent(context, ActivityView.class);
                        view.setAction("folder:" + folders.get(0).id);
                        view.putExtra("account", account);
                        view.putExtra("type", folders.get(0).type);
                        view.putExtra("standalone", standalone);
                        view.putExtra("refresh", true);
                        view.putExtra("version", version);
                        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        pi = PendingIntentCompat.getActivity(
                                context, appWidgetId, view, PendingIntent.FLAG_UPDATE_CURRENT);
                    } else {
                        if (account < 0) {
                            Intent view = new Intent(context, ActivityView.class);
                            view.setAction("unified");
                            view.putExtra("standalone", standalone);
                            view.putExtra("refresh", true);
                            view.putExtra("version", version);
                            view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            pi = PendingIntentCompat.getActivity(
                                    context, ActivityView.PI_UNIFIED, view, PendingIntent.FLAG_UPDATE_CURRENT);
                        } else {
                            Intent view = new Intent(context, ActivityView.class);
                            view.setAction("folders:" + account);
                            view.putExtra("standalone", standalone);
                            view.putExtra("refresh", true);
                            view.putExtra("version", version);
                            view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            pi = PendingIntentCompat.getActivity(
                                    context, appWidgetId, view, PendingIntent.FLAG_UPDATE_CURRENT);
                        }
                    }

                    TupleMessageStats stats = db.message().getWidgetUnseen(
                            account < 0 ? null : account,
                            folder < 0 ? null : folder);
                    EntityLog.log(context, "Widget account=" + account + " folder=" + folder +
                            " ignore=" + unseen_ignored + " " + stats);

                    Integer unseen = (unseen_ignored ? stats.notifying : stats.unseen);
                    if (unseen == null)
                        unseen = 0;

                    RemoteViews views = new RemoteViews(context.getPackageName(),
                            layout == 0 ? R.layout.widget : R.layout.widget_new);

                    views.setOnClickPendingIntent(R.id.background, pi);

                    if (!daynight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        views.setColorStateListAttr(R.id.background, "setBackgroundTintList", 0);

                    // Set background
                    if (daynight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        views.setInt(R.id.background, "setBackgroundColor", Color.WHITE);
                        views.setColorStateListAttr(R.id.background, "setBackgroundTintList", android.R.attr.colorBackground);
                    } else if (semi)
                        if (background == Color.TRANSPARENT)
                            views.setInt(R.id.background, "setBackgroundResource",
                                    R.drawable.widget_background);
                        else
                            views.setInt(R.id.background, "setBackgroundColor",
                                    ColorUtils.setAlphaComponent(background, 127));
                    else
                        views.setInt(R.id.background, "setBackgroundColor", background);

                    // Set image
                    if (layout == 1)
                        views.setImageViewResource(R.id.ivMessage, unseen == 0
                                ? R.drawable.baseline_mail_outline_widget_24
                                : R.drawable.baseline_mail_widget_24);
                    else
                        views.setImageViewResource(R.id.ivMessage, unseen == 0
                                ? R.drawable.twotone_mail_outline_24
                                : R.drawable.baseline_mail_24);

                    // Set color
                    if (daynight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        views.setColorAttr(R.id.ivMessage, "setColorFilter",
                                foreground == Color.TRANSPARENT ? android.R.attr.textColorPrimary : foreground);
                        if (layout == 0)
                            views.setColorStateListAttr(R.id.tvCount, "setTextColor", android.R.attr.textColorPrimary);
                        else {
                            views.setTextColor(R.id.tvCount, colorWidgetForeground);
                            views.setTextColor(R.id.tvCountTop, colorWidgetForeground);
                        }
                        views.setColorStateListAttr(R.id.tvAccount, "setTextColor", android.R.attr.textColorPrimary);
                    } else if (background == Color.TRANSPARENT) {
                        views.setInt(R.id.ivMessage, "setColorFilter",
                                foreground == Color.TRANSPARENT ? colorWidgetForeground : foreground);
                        views.setTextColor(R.id.tvCount, colorWidgetForeground);
                        views.setTextColor(R.id.tvCountTop, colorWidgetForeground);
                        views.setTextColor(R.id.tvAccount, colorWidgetForeground);
                    } else {
                        float lum = (float) ColorUtils.calculateLuminance(background);
                        int fg = (lum > 0.7f ? Color.BLACK : colorWidgetForeground);
                        views.setInt(R.id.ivMessage, "setColorFilter",
                                foreground == Color.TRANSPARENT ? fg : foreground);
                        views.setTextColor(R.id.tvCount, layout == 0 ? fg : colorWidgetForeground);
                        views.setTextColor(R.id.tvCountTop, layout == 0 ? fg : colorWidgetForeground);
                        views.setTextColor(R.id.tvAccount, fg);
                    }

                    // Set count
                    String count = Helper.formatNumber(unseen, 99, nf);
                    views.setTextViewText(R.id.tvCount, count);
                    views.setViewVisibility(R.id.tvCount, top || (layout == 1 && unseen == 0) ? View.GONE : View.VISIBLE);

                    views.setTextViewText(R.id.tvCountTop, count);
                    views.setViewVisibility(R.id.tvCountTop, !top || (layout == 1 && unseen == 0) ? View.GONE : View.VISIBLE);

                    if (size < 0)
                        size = 0; // small
                    float textSize = Helper.getTextSize(context, size);
                    views.setTextViewTextSize(R.id.tvCount, TypedValue.COMPLEX_UNIT_PX, textSize);
                    views.setTextViewTextSize(R.id.tvCountTop, TypedValue.COMPLEX_UNIT_PX, textSize);

                    // Set account name
                    if (TextUtils.isEmpty(name))
                        views.setViewVisibility(R.id.tvAccount, ViewStripe.GONE);
                    else {
                        views.setTextViewText(R.id.tvAccount, name);
                        views.setViewVisibility(R.id.tvAccount, ViewStripe.VISIBLE);
                    }

                    int pad = Helper.dp2pixels(context, layout == 0 ? 3 : 6);
                    views.setViewPadding(R.id.content, pad, pad, pad, pad);

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        });
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
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, Widget.class));

            Intent intent = new Intent(context, Widget.class);
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
