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

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.ViewHolder> {
    private Context context;

    private List<TupleMessageEx> all = new ArrayList<>();
    private List<TupleMessageEx> filtered = new ArrayList<>();

    private ExecutorService executor = Executors.newCachedThreadPool();

    enum ViewType {FOLDER, THREAD}

    private ViewType viewType;

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        View itemView;
        TextView tvAddress;
        TextView tvTime;
        TextView tvSubject;
        TextView tvCount;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvCount = itemView.findViewById(R.id.tvCount);
        }

        private void wire() {
            itemView.setOnClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
        }

        @Override
        public void onClick(View view) {
            final TupleMessageEx message = filtered.get(getLayoutPosition());

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    if (EntityFolder.TYPE_DRAFTS.equals(message.folderType))
                        context.startActivity(
                                new Intent(context, ActivityCompose.class)
                                        .putExtra("id", message.id));
                    else {
                        if (!message.seen && !message.ui_seen) {
                            message.ui_seen = !message.ui_seen;
                            DB.getInstance(context).message().updateMessage(message);
                            EntityOperation.queue(context, message, EntityOperation.SEEN, message.ui_seen);
                        }

                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_VIEW_MESSAGE)
                                        .putExtra("folder", message.folder)
                                        .putExtra("id", message.id));
                    }
                }
            });
        }
    }

    AdapterMessage(Context context, ViewType viewType) {
        this.context = context;
        this.viewType = viewType;
        setHasStableIds(true);
    }

    public void set(List<TupleMessageEx> messages) {
        Log.i(Helper.TAG, "Set messages=" + messages.size());

        Collections.sort(messages, new Comparator<TupleMessageEx>() {
            @Override
            public int compare(TupleMessageEx m1, TupleMessageEx m2) {
                if (EntityFolder.isOutgoing(m1.folderType))
                    return -Long.compare(m1.received, m2.received);
                else
                    return -Long.compare(m1.sent, m2.sent);
            }
        });

        all.clear();
        all.addAll(messages);

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new MessageDiffCallback(filtered, all));

        filtered.clear();
        filtered.addAll(all);

        diff.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.i(Helper.TAG, "Inserted @" + position + " #" + count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.i(Helper.TAG, "Removed @" + position + " #" + count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.i(Helper.TAG, "Moved " + fromPosition + ">" + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.i(Helper.TAG, "Changed @" + position + " #" + count);
            }
        });
        diff.dispatchUpdatesTo(AdapterMessage.this);
    }

    private class MessageDiffCallback extends DiffUtil.Callback {
        private List<TupleMessageEx> prev;
        private List<TupleMessageEx> next;

        MessageDiffCallback(List<TupleMessageEx> prev, List<TupleMessageEx> next) {
            this.prev = prev;
            this.next = next;
        }

        @Override
        public int getOldListSize() {
            return prev.size();
        }

        @Override
        public int getNewListSize() {
            return next.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            TupleMessageEx m1 = prev.get(oldItemPosition);
            TupleMessageEx m2 = next.get(newItemPosition);
            return m1.id.equals(m2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleMessageEx m1 = prev.get(oldItemPosition);
            TupleMessageEx m2 = next.get(newItemPosition);
            return m1.equals(m2);
        }
    }

    @Override
    public long getItemId(int position) {
        return filtered.get(position).id;
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleMessageEx message = filtered.get(position);

        if (EntityFolder.isOutgoing(message.folderType)) {
            holder.tvAddress.setText(message.to == null ? null : MessageHelper.getFormattedAddresses(message.to));
            holder.tvTime.setText(DateUtils.getRelativeTimeSpanString(context, message.received));
        } else {
            holder.tvAddress.setText(message.from == null ? null : MessageHelper.getFormattedAddresses(message.from));
            holder.tvTime.setText(message.sent == null ? null : DateUtils.getRelativeTimeSpanString(context, message.sent));
        }

        holder.tvSubject.setText(message.subject);
        if (viewType == ViewType.FOLDER) {
            holder.tvCount.setText(Integer.toString(message.count));
            holder.tvCount.setVisibility(message.count > 1 ? View.VISIBLE : View.GONE);
        } else
            holder.tvCount.setText(Helper.localizeFolderName(context, message.folderName));

        boolean unseen = (message.thread == null ? !message.seen : message.unseen > 0);
        int visibility = (unseen ? Typeface.BOLD : Typeface.NORMAL);
        holder.tvAddress.setTypeface(null, visibility);
        holder.tvTime.setTypeface(null, visibility);
        holder.tvSubject.setTypeface(null, visibility);
        holder.tvCount.setTypeface(null, visibility);

        holder.wire();
    }
}
