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

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.lifecycle.Observer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.os.Looper.getMainLooper;

public class WidgetUnifiedRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private DateFormat DTF;

    private Handler handler;
    private TwoStateOwner owner;
    private List<EntityMessage> messages = new ArrayList<>();

    WidgetUnifiedRemoteViewsFactory(final Context context) {
        this.context = context;
        this.DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

        this.handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                DB db = DB.getInstance(context);
                owner = new TwoStateOwner("WidgetUnified");
                db.message().liveWidgetUnified().observe(owner, new Observer<List<EntityMessage>>() {
                    @Override
                    public void onChanged(List<EntityMessage> messages) {
                        if (messages == null)
                            messages = new ArrayList<>();

                        boolean changed = false;
                        if (WidgetUnifiedRemoteViewsFactory.this.messages.size() == messages.size()) {
                            for (int i = 0; i < messages.size(); i++) {
                                EntityMessage m1 = messages.get(i);
                                EntityMessage m2 = WidgetUnifiedRemoteViewsFactory.this.messages.get(i);
                                if (!m1.id.equals(m2.id) ||
                                        !MessageHelper.equal(m1.from, m2.from) ||
                                        !m1.received.equals(m2.received) ||
                                        !Objects.equals(m1.subject, m2.subject) ||
                                        m1.ui_seen != m2.ui_seen) {
                                    changed = true;
                                    break;
                                }
                            }
                        } else
                            changed = true;

                        WidgetUnifiedRemoteViewsFactory.this.messages = messages;

                        if (changed)
                            WidgetUnified.update(context);
                    }
                });
            }
        });
    }

    @Override
    public void onCreate() {
        Log.i("Widget factory create");
        handler.post(new Runnable() {
            @Override
            public void run() {
                owner.start();
            }
        });
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
        Log.i("Widget factory destroy");
        handler.post(new Runnable() {
            @Override
            public void run() {
                owner.destroy();
            }
        });
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        EntityMessage message = messages.get(position);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.item_widget_unified);

        Intent thread = new Intent(context, ActivityView.class);
        thread.putExtra("account", message.account);
        thread.putExtra("thread", message.thread);
        thread.putExtra("id", message.id);
        views.setOnClickFillInIntent(R.id.llMessage, thread);

        SpannableString from = new SpannableString(MessageHelper.formatAddressesShort(message.from));
        SpannableString time = new SpannableString(DTF.format(message.received));
        SpannableString subject = new SpannableString(TextUtils.isEmpty(message.subject) ? "" : message.subject);

        if (!message.ui_seen) {
            from.setSpan(new StyleSpan(Typeface.BOLD), 0, from.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            time.setSpan(new StyleSpan(Typeface.BOLD), 0, time.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            subject.setSpan(new StyleSpan(Typeface.BOLD), 0, subject.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        views.setTextViewText(R.id.tvFrom, from);
        views.setTextViewText(R.id.tvTime, time);
        views.setTextViewText(R.id.tvSubject, subject);

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
