package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdapterMessage extends PagedListAdapter<TupleMessageEx, AdapterMessage.ViewHolder> {
    private Context context;
    private ViewType viewType;
    private boolean debug;
    private ExecutorService executor = Executors.newCachedThreadPool();

    enum ViewType {FOLDER, THREAD}

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        View itemView;
        TextView tvFrom;
        TextView tvTime;
        ImageView ivAttachments;
        TextView tvSubject;
        TextView tvCount;
        ProgressBar pbLoading;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            tvFrom = itemView.findViewById(R.id.tvFrom);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivAttachments = itemView.findViewById(R.id.ivAttachments);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvCount = itemView.findViewById(R.id.tvCount);
            pbLoading = itemView.findViewById(R.id.pbLoading);
        }

        private void wire() {
            itemView.setOnClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
        }

        private void clear() {
            tvFrom.setText(null);
            tvTime.setText(null);
            tvSubject.setText(null);
            ivAttachments.setVisibility(View.GONE);
            tvCount.setText(null);
            pbLoading.setVisibility(View.VISIBLE);
        }

        private void bindTo(TupleMessageEx message) {
            boolean outgoing = EntityFolder.isOutgoing(message.folderType);
            boolean outbox = EntityFolder.TYPE_OUTBOX.equals(message.folderType);

            pbLoading.setVisibility(View.GONE);

            if (outgoing) {
                tvFrom.setText(MessageHelper.getFormattedAddresses(message.to));
                tvTime.setText(DateUtils.getRelativeTimeSpanString(context, message.received));
            } else {
                tvFrom.setText(MessageHelper.getFormattedAddresses(message.from));
                tvTime.setText(message.sent == null ? null : DateUtils.getRelativeTimeSpanString(context, message.sent));
            }

            tvSubject.setText(message.subject);

            String extra = (debug ? (message.ui_hide ? "HIDDEN " : "") + message.uid + "/" + message.id + " " : "");
            if (viewType == ViewType.FOLDER) {
                tvCount.setText(extra + Integer.toString(message.count));
                tvCount.setVisibility(debug || message.count > 1 ? View.VISIBLE : View.GONE);
            } else
                tvCount.setText(extra + Helper.localizeFolderName(context, message.folderName));

            ivAttachments.setVisibility(message.attachments > 0 ? View.VISIBLE : View.GONE);

            boolean unseen = (message.thread == null && !outbox ? message.unseen > 0 : !message.seen);

            int typeface = (unseen ? Typeface.BOLD : Typeface.NORMAL);
            tvFrom.setTypeface(null, typeface);
            tvTime.setTypeface(null, typeface);
            tvSubject.setTypeface(null, typeface);
            tvCount.setTypeface(null, typeface);

            tvFrom.setTextColor(Helper.resolveColor(context, unseen ? R.attr.colorUnread : android.R.attr.textColorSecondary));
            tvTime.setTextColor(Helper.resolveColor(context, unseen ? R.attr.colorUnread : android.R.attr.textColorSecondary));
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;
            final TupleMessageEx message = getItem(pos);

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (EntityFolder.TYPE_DRAFTS.equals(message.folderType))
                            context.startActivity(
                                    new Intent(context, ActivityCompose.class)
                                            .putExtra("id", message.id));
                        else {
                            boolean outbox = EntityFolder.TYPE_OUTBOX.equals(message.folderType);
                            if (!outbox && !message.seen && !message.ui_seen) {
                                message.ui_seen = !message.ui_seen;
                                DB.getInstance(context).message().updateMessage(message);
                                EntityOperation.queue(context, message, EntityOperation.SEEN, message.ui_seen);
                                EntityOperation.process(context);
                            }

                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_VIEW_MESSAGE)
                                            .putExtra("id", message.id));
                        }
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    }
                }
            });
        }
    }

    AdapterMessage(Context context, ViewType viewType) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.viewType = viewType;
        this.debug = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug", false);
    }

    public static final DiffUtil.ItemCallback<TupleMessageEx> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TupleMessageEx>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull TupleMessageEx prev, @NonNull TupleMessageEx next) {
                    return prev.id.equals(next.id);
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull TupleMessageEx prev, @NonNull TupleMessageEx next) {
                    return prev.equals(next);
                }
            };

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleMessageEx message = getItem(position);
        if (message == null)
            holder.clear();
        else {
            holder.bindTo(message);
            holder.wire();
        }
    }
}
