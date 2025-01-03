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

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class AdapterOperation extends RecyclerView.Adapter<AdapterOperation.ViewHolder> {
    private Fragment parentFragment;
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private List<TupleOperationEx> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private ImageView ivState;
        private TextView tvFolder;
        private TextView tvOperation;
        private TextView tvTime;
        private TextView tvError;

        private TwoStateOwner powner = new TwoStateOwner(owner, "OperationPopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ivState = itemView.findViewById(R.id.ivState);
            tvFolder = itemView.findViewById(R.id.tvFolder);
            tvOperation = itemView.findViewById(R.id.tvOperation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvError = itemView.findViewById(R.id.tvError);
        }

        private void wire() {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
        }

        private void bindTo(TupleOperationEx operation) {
            view.setAlpha(operation.synchronize ? 1.0f : Helper.LOW_LIGHT);

            StringBuilder sb = new StringBuilder();
            sb
                    .append(operation.name).append(':')
                    .append(operation.getPriority(false)).append("/")
                    .append(operation.tries);
            try {
                JSONArray jarray = new JSONArray(operation.args);
                if (jarray.length() > 0)
                    sb.append(' ').append(operation.args);
            } catch (JSONException ex) {
                Log.e(ex);
            }

            String folderName =
                    (operation.accountName == null ? "" : operation.accountName + "/") + operation.folderName;

            ivState.setVisibility(operation.state == null ? View.INVISIBLE : View.VISIBLE);
            tvFolder.setText(folderName + ":" + operation.folder);
            tvOperation.setText(sb.toString());
            tvTime.setText(Helper.getRelativeTimeSpanString(context, operation.created));
            tvError.setText(operation.error);
            tvError.setVisibility(operation.error == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleOperationEx operation = items.get(pos);
            if (operation == null || !operation.synchronize)
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
                                        .putExtra("folder", folder.id)
                                        .putExtra("type", folder.type));
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
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
                        if (message == null)
                            return;

                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_VIEW_THREAD)
                                        .putExtra("account", message.account)
                                        .putExtra("folder", message.folder)
                                        .putExtra("thread", message.thread)
                                        .putExtra("id", message.id)
                                        .putExtra("found", false));
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                    }
                }.execute(context, owner, args, "operation:open:message");
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleOperationEx operation = items.get(pos);
            if (operation == null)
                return false;

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            SpannableString ss = new SpannableString(operation.name + ":" + operation.id);
            ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
            popupMenu.getMenu().add(Menu.NONE, 0, 0, ss)
                    .setEnabled(false);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, 1, R.string.title_delete);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.string.title_delete) {
                        onActionDelete();
                        return true;
                    }
                    return false;
                }

                private void onActionDelete() {
                    Bundle args = new Bundle();
                    args.putLong("id", operation.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            DB db = DB.getInstance(context);
                            EntityOperation operation = db.operation().getOperation(id);
                            if (operation == null)
                                return null;

                            if (db.operation().deleteOperation(operation.id) > 0)
                                operation.cleanup(context, false);

                            if (EntityOperation.SYNC.equals(operation.name))
                                db.folder().setFolderSyncState(operation.folder, null);

                            db.folder().setFolderError(operation.folder, null);
                            if (operation.message != null)
                                db.message().setMessageError(operation.message, null);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "operation:delete");
                }
            });

            popupMenu.show();

            return true;
        }
    }

    AdapterOperation(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterOperation.this + " parent destroyed");
                AdapterOperation.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<TupleOperationEx> operations) {
        Log.i("Set operations=" + operations.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, operations), false);

        items = operations;

        diff.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d("Inserted @" + position + " #" + count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d("Removed @" + position + " #" + count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d("Moved " + fromPosition + ">" + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.d("Changed @" + position + " #" + count);
            }
        });

        try {
            diff.dispatchUpdatesTo(this);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static class DiffCallback extends DiffUtil.Callback {
        private List<TupleOperationEx> prev = new ArrayList<>();
        private List<TupleOperationEx> next = new ArrayList<>();

        DiffCallback(List<TupleOperationEx> prev, List<TupleOperationEx> next) {
            this.prev.addAll(prev);
            this.next.addAll(next);
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
        return items.get(position).id;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_operation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TupleOperationEx operation = items.get(position);
        holder.powner.recreate(operation == null ? null : operation.id);

        holder.unwire();
        holder.bindTo(operation);
        holder.wire();
    }
}
