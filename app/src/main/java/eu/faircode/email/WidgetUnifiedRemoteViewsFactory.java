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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

public class WidgetUnifiedRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private int appWidgetId;

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
        DB db = DB.getInstance(context);
        messages = db.message().getWidgetUnified();
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

        try {
            TupleMessageWidget message = messages.get(position);

            Intent thread = new Intent(context, ActivityView.class);
            thread.putExtra("account", message.account);
            thread.putExtra("thread", message.thread);
            thread.putExtra("id", message.id);
            views.setOnClickFillInIntent(R.id.llMessage, thread);

            String froms = MessageHelper.formatAddressesShort(message.from);
            if (message.unseen > 1)
                froms = context.getString(R.string.title_name_count, froms, Integer.toString(message.unseen));

            SpannableString from = new SpannableString(froms);
            SpannableString time = new SpannableString(Helper.getRelativeTimeSpanString(context, message.received));
            SpannableString subject = new SpannableString(TextUtils.isEmpty(message.subject) ? "" : message.subject);
            SpannableString account = new SpannableString(TextUtils.isEmpty(message.accountName) ? "" : message.accountName);

            if (!message.ui_seen) {
                from.setSpan(new StyleSpan(Typeface.BOLD), 0, from.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                time.setSpan(new StyleSpan(Typeface.BOLD), 0, time.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                subject.setSpan(new StyleSpan(Typeface.BOLD), 0, subject.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                account.setSpan(new StyleSpan(Typeface.BOLD), 0, account.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            views.setTextViewText(R.id.tvFrom, from);
            views.setTextViewText(R.id.tvTime, time);
            views.setTextViewText(R.id.tvSubject, subject);
            views.setTextViewText(R.id.tvAccount, account);
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
        return messages.get(position).id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
