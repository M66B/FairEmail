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
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterOperation extends RecyclerView.Adapter<AdapterOperation.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private LifecycleOwner owner;

    private List<TupleOperationEx> all = new ArrayList<>();
    private List<TupleOperationEx> filtered = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View itemView;
        private TextView tvFolder;
        private TextView tvMessage;
        private TextView tvOperation;
        private TextView tvTime;
        private TextView tvError;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView.findViewById(R.id.clItem);
            tvFolder = itemView.findViewById(R.id.tvFolder);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvOperation = itemView.findViewById(R.id.tvOperation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvError = itemView.findViewById(R.id.tvError);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            if (BuildConfig.DEBUG)
                itemView.setOnLongClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            if (BuildConfig.DEBUG)
                itemView.setOnLongClickListener(null);
        }

        private void bindTo(TupleOperationEx operation) {
            itemView.setAlpha(operation.synchronize ? 1.0f : Helper.LOW_LIGHT);

            StringBuilder sb = new StringBuilder();
            sb.append(operation.name);
            try {
                JSONArray jarray = new JSONArray(operation.args);
                if (jarray.length() > 0)
                    sb.append(' ').append(operation.args);
            } catch (JSONException ex) {
                sb.append(' ').append(ex.toString());
            }

            tvFolder.setText(operation.accountName + "/" + operation.folderName);
            tvMessage.setText(operation.message == null ? null : Long.toString(operation.message));
            tvOperation.setText(sb.toString());
            tvTime.setText(DateUtils.getRelativeTimeSpanString(context, operation.created));
            tvError.setText(operation.error);
            tvError.setVisibility(operation.error == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleOperationEx operation = filtered.get(pos);
            if (operation == null)
                return;

            if (operation.message == null) {
                Bundle args = new Bundle();
                args.putLong("id", operation.folder);

                new SimpleTask<EntityFolder>() {
                    @Override
                    protected EntityFolder onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        return DB.getInstance(context).folder().getFolder(id);
                    }

                    @Override
                    protected void onExecuted(Bundle args, EntityFolder folder) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                        .putExtra("account", folder.account)
                                        .putExtra("folder", folder.id));
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, owner, ex);
                    }
                }.execute(context, owner, args, "operation:open:folder");
            } else {
                Bundle args = new Bundle();
                args.putLong("id", operation.message);

                new SimpleTask<EntityMessage>() {
                    @Override
                    protected EntityMessage onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        return DB.getInstance(context).message().getMessage(id);
                    }

                    @Override
                    protected void onExecuted(Bundle args, EntityMessage message) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_VIEW_THREAD)
                                        .putExtra("account", message.account)
                                        .putExtra("thread", message.thread)
                                        .putExtra("id", message.id)
                                        .putExtra("found", false));
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, owner, ex);
                    }
                }.execute(context, owner, args, "operation:open:message");
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            TupleOperationEx operation = filtered.get(pos);
            if (operation == null)
                return false;

            Bundle args = new Bundle();
            args.putLong("id", operation.id);
            args.putLong("folder", operation.folder);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    long folder = args.getLong("folder");
                    DB db = DB.getInstance(context);
                    db.operation().deleteOperation(id);
                    db.folder().setFolderError(folder, null);
                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "operation:delete");

            return true;
        }
    }

    AdapterOperation(Context context, LifecycleOwner owner) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.owner = owner;
        setHasStableIds(true);
    }

    public void set(@NonNull List<TupleOperationEx> operations) {
        Log.i("Set operations=" + operations.size());

        all = operations;

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(filtered, all));

        filtered.clear();
        filtered.addAll(all);

        diff.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.i("Inserted @" + position + " #" + count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.i("Removed @" + position + " #" + count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.i("Moved " + fromPosition + ">" + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.i("Changed @" + position + " #" + count);
            }
        });
        diff.dispatchUpdatesTo(this);
    }

    private class DiffCallback extends DiffUtil.Callback {
        private List<TupleOperationEx> prev;
        private List<TupleOperationEx> next;

        DiffCallback(List<TupleOperationEx> prev, List<TupleOperationEx> next) {
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
            TupleOperationEx a1 = prev.get(oldItemPosition);
            TupleOperationEx a2 = next.get(newItemPosition);
            return a1.id.equals(a2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleOperationEx a1 = prev.get(oldItemPosition);
            TupleOperationEx a2 = next.get(newItemPosition);
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
        return new ViewHolder(inflater.inflate(R.layout.item_operation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleOperationEx operation = filtered.get(position);
        holder.bindTo(operation);

        holder.wire();
    }
}
