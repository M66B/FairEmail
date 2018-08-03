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
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdapterAttachment extends RecyclerView.Adapter<AdapterAttachment.ViewHolder> {
    private Context context;
    private ExecutorService executor = Executors.newCachedThreadPool();

    private List<EntityAttachment> all = new ArrayList<>();
    private List<EntityAttachment> filtered = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        View itemView;
        TextView tvName;
        TextView tvSize;
        ImageView ivDownload;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            tvName = itemView.findViewById(R.id.tvName);
            tvSize = itemView.findViewById(R.id.tvSize);
            ivDownload = itemView.findViewById(R.id.ivDownload);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            ivDownload.setOnClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            ivDownload.setOnClickListener(null);
        }

        @Override
        public void onClick(View view) {
            final EntityAttachment attachment = filtered.get(getLayoutPosition());
            if (attachment != null && attachment.content == null)
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        EntityMessage message = DB.getInstance(context).message().getMessage(attachment.message);
                        EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, attachment.sequence);
                    }
                });
        }
    }

    AdapterAttachment(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    public void set(List<EntityAttachment> attachments) {
        Log.i(Helper.TAG, "Set attachments=" + attachments.size());

        Collections.sort(attachments, new Comparator<EntityAttachment>() {
            @Override
            public int compare(EntityAttachment a1, EntityAttachment a2) {
                return a1.sequence.compareTo(a2.sequence);
            }
        });

        all.clear();
        all.addAll(attachments);

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
        diff.dispatchUpdatesTo(AdapterAttachment.this);
    }

    private class MessageDiffCallback extends DiffUtil.Callback {
        private List<EntityAttachment> prev;
        private List<EntityAttachment> next;

        MessageDiffCallback(List<EntityAttachment> prev, List<EntityAttachment> next) {
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
            EntityAttachment a1 = prev.get(oldItemPosition);
            EntityAttachment a2 = next.get(newItemPosition);
            return a1.id.equals(a2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityAttachment a1 = prev.get(oldItemPosition);
            EntityAttachment a2 = next.get(newItemPosition);
            return a1.equals(a2);
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
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_attachment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityAttachment attachment = filtered.get(position);
        holder.tvName.setText(attachment.name);
        holder.tvSize.setVisibility((attachment.content == null ? View.GONE : View.VISIBLE));
        holder.ivDownload.setVisibility((attachment.content == null ? View.VISIBLE : View.GONE));

        if (attachment.content != null)
            holder.tvSize.setText(Helper.humanReadableByteCount(attachment.content.length, false));

        holder.wire();
    }
}
