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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class WidgetUnifiedRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private int appWidgetId;

    private boolean threading;
    private boolean subject_top;
    private boolean subject_italic;
    private boolean color_stripe;
    private long folder;
    private long account;
    private boolean unseen;
    private boolean flagged;
    private int colorWidgetForeground;
    private int colorWidgetRead;
    private int colorSeparator;
    private boolean pro;
    private List<TupleMessageWidget> messages = new ArrayList<>();

    WidgetUnifiedRemoteViewsFactory(final Context context, Intent intent) {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        Log.i("Widget factory create id=" + appWidgetId);
    }

    @Override
    public void onDataSetChanged() {
        Log.i("Widget factory changed id=" + appWidgetId);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        threading = prefs.getBoolean("threading", true);
        subject_top = prefs.getBoolean("subject_top", false);
        subject_italic = prefs.getBoolean("subject_italic", true);
        color_stripe = prefs.getBoolean("color_stripe", true);
        account = prefs.getLong("widget." + appWidgetId + ".account", -1L);
        folder = prefs.getLong("widget." + appWidgetId + ".folder", -1L);
        unseen = prefs.getBoolean("widget." + appWidgetId + ".unseen", false);
        flagged = prefs.getBoolean("widget." + appWidgetId + ".flagged", false);
        colorWidgetForeground = ContextCompat.getColor(context, R.color.colorWidgetForeground);
        colorWidgetRead = ContextCompat.getColor(context, R.color.colorWidgetRead);
        colorSeparator = ContextCompat.getColor(context, R.color.lightColorSeparator);

        pro = ActivityBilling.isPro(context);

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            messages = db.message().getWidgetUnified(
                    account < 0 ? null : account,
                    folder < 0 ? null : folder,
                    threading, unseen, flagged);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onDestroy() {
        Log.i("Widget factory destroy id=" + appWidgetId);
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.item_widget_unified);
        int idFrom = (subject_top ? R.id.tvSubject : R.id.tvFrom);
        int idTime = (subject_top ? R.id.tvAccount : R.id.tvTime);
        int idSubject = (subject_top ? R.id.tvFrom : R.id.tvSubject);
        int idAccount = (subject_top ? R.id.tvTime : R.id.tvAccount);

        if (position >= messages.size())
            return views;

        try {
            TupleMessageWidget message = messages.get(position);

            Intent thread = new Intent(context, ActivityView.class);
            thread.putExtra("account", message.account);
            thread.putExtra("thread", message.thread);
            thread.putExtra("id", message.id);
            views.setOnClickFillInIntent(R.id.llMessage, thread);

            int colorBackground =
                    (message.accountColor == null || !pro ? colorSeparator : message.accountColor);
            views.setInt(R.id.stripe, "setBackgroundColor", colorBackground);
            views.setViewVisibility(R.id.stripe, account < 0 && color_stripe ? View.VISIBLE : View.GONE);

            SpannableString ssFrom = new SpannableString(pro
                    ? MessageHelper.formatAddressesShort(message.from)
                    : context.getString(R.string.title_pro_feature));
            SpannableString ssTime = new SpannableString(
                    Helper.getRelativeTimeSpanString(context, message.received));
            SpannableString ssSubject = new SpannableString(pro
                    ? TextUtils.isEmpty(message.subject) ? "" : message.subject
                    : context.getString(R.string.title_pro_feature));
            SpannableString ssAccount = new SpannableString(
                    TextUtils.isEmpty(message.accountName) ? "" : message.accountName);

            if (message.ui_seen) {
                if (subject_italic)
                    ssSubject.setSpan(new StyleSpan(Typeface.ITALIC), 0, ssSubject.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                ssFrom.setSpan(new StyleSpan(Typeface.BOLD), 0, ssFrom.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                ssTime.setSpan(new StyleSpan(Typeface.BOLD), 0, ssTime.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                ssSubject.setSpan(new StyleSpan(subject_italic ? Typeface.BOLD_ITALIC : Typeface.BOLD), 0, ssSubject.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                ssAccount.setSpan(new StyleSpan(Typeface.BOLD), 0, ssAccount.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            views.setTextViewText(idFrom, ssFrom);
            views.setTextViewText(idTime, ssTime);
            views.setTextViewText(idSubject, ssSubject);
            views.setTextViewText(idAccount, ssAccount);

            views.setTextColor(idFrom, message.ui_seen ? colorWidgetRead : colorWidgetForeground);
            views.setTextColor(idTime, message.ui_seen ? colorWidgetRead : colorWidgetForeground);
            views.setTextColor(idSubject, message.ui_seen ? colorWidgetRead : colorWidgetForeground);
            views.setTextColor(idAccount, message.ui_seen ? colorWidgetRead : colorWidgetForeground);

            views.setViewVisibility(idAccount, account < 0 ? View.VISIBLE : View.GONE);

        } catch (Throwable ex) {
            Log.e(ex);
        }

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (position >= messages.size())
            return -1;
        return messages.get(position).id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
