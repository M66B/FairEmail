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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterOperation extends RecyclerView.Adapter<AdapterOperation.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;

    private List<EntityOperation> all = new ArrayList<>();
    private List<EntityOperation> filtered = new ArrayList<>();

    private DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.LONG);

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View itemView;
        private TextView tvMessage;
        private TextView tvName;
        private TextView tvArgs;
        private TextView tvTime;
        private TextView tvError;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvName = itemView.findViewById(R.id.tvName);
            tvArgs = itemView.findViewById(R.id.tvArgs);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvError = itemView.findViewById(R.id.tvError);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);
        }

        private void bindTo(EntityOperation operation) {
            tvMessage.setText(operation.message == null ? null : Long.toString(operation.message));
            tvName.setText(operation.name);
            tvArgs.setText(operation.args);
            tvTime.setText(df.format(new Date(operation.created)));
            tvError.setText(operation.error);
            tvError.setVisibility(operation.error == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            EntityOperation operation = filtered.get(pos);
            if (operation.message == null) {
                Bundle args = new Bundle();
                args.putLong("id", operation.folder);

                new SimpleTask<EntityFolder>() {
                    @Override
                    protected EntityFolder onLoad(Context context, Bundle args) {
                        long id = args.getLong("id");
                        return DB.getInstance(context).folder().getFolder(id);
                    }

                    @Override
                    protected void onLoaded(Bundle args, EntityFolder folder) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                        .putExtra("account", folder.account)
                                        .putExtra("folder", folder.id)
                                        .putExtra("outgoing", folder.isOutgoing()));
                    }
                }.load(context, owner, args);
            } else {
                Bundle args = new Bundle();
                args.putLong("id", operation.message);

                new SimpleTask<EntityMessage>() {
                    @Override
                    protected EntityMessage onLoad(Context context, Bundle args) {
                        long id = args.getLong("id");
                        return DB.getInstance(context).message().getMessage(id);
                    }

                    @Override
                    protected void onLoaded(Bundle args, EntityMessage message) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_VIEW_THREAD)
                                        .putExtra("account", message.account)
                                        .putExtra("thread", message.thread)
                                        .putExtra("id", message.id));
                    }
                }.load(context, owner, args);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            EntityOperation operation = filtered.get(pos);

            Bundle args = new Bundle();
            args.putLong("id", operation.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) {
                    DB.getInstance(context).operation().deleteOperation(args.getLong("id"));
                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.load(context, owner, args);

            return true;
        }
    }

    AdapterOperation(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        setHasStableIds(true);
    }

    public void set(@NonNull List<EntityOperation> operations) {
        Log.i(Helper.TAG, "Set operations=" + operations.size());

        all = operations;

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
        diff.dispatchUpdatesTo(this);
    }

    private class MessageDiffCallback extends DiffUtil.Callback {
        private List<EntityOperation> prev;
        private List<EntityOperation> next;

        MessageDiffCallback(List<EntityOperation> prev, List<EntityOperation> next) {
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
            EntityOperation a1 = prev.get(oldItemPosition);
            EntityOperation a2 = next.get(newItemPosition);
            return a1.id.equals(a2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityOperation a1 = prev.get(oldItemPosition);
            EntityOperation a2 = next.get(newItemPosition);
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
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_operation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityOperation operation = filtered.get(position);
        holder.bindTo(operation);

        holder.wire();
    }
}
