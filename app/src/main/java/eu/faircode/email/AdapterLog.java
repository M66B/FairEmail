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
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterLog extends RecyclerView.Adapter<AdapterLog.ViewHolder> {
    private Fragment parentFragment;
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private Long account = null;
    private Long folder = null;
    private Long message = null;
    private List<EntityLog.Type> types = new ArrayList<>();
    private List<EntityLog> all = new ArrayList<>();
    private List<EntityLog> selected = new ArrayList<>();

    private DateFormat TF;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTime;
        private TextView tvData;

        ViewHolder(View itemView) {
            super(itemView);

            tvTime = itemView.findViewById(R.id.tvTime);
            tvData = itemView.findViewById(R.id.tvData);
        }

        private void bindTo(EntityLog log) {
            tvTime.setText(TF.format(log.time));
            SpannableStringBuilder ssb = new SpannableStringBuilderEx(log.data);

            Integer color = log.getColor(context);
            if (color != null)
                ssb.setSpan(new ForegroundColorSpan(color), 0, ssb.length(), 0);

            tvData.setText(ssb);
        }
    }

    AdapterLog(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(parentFragment.getContext());

        this.TF = Helper.getTimeInstance(context);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterLog.this + " parent destroyed");
                AdapterLog.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<EntityLog> logs,
                    Long account, Long folder, Long message,
                    @NonNull List<EntityLog.Type> types,
                    Runnable callback) {
        Log.i("Set logs=" + logs.size());

        this.all = logs;
        this.account = account;
        this.folder = folder;
        this.message = message;
        this.types = types;

        new SimpleTask<List<EntityLog>>() {
            @Override
            protected List<EntityLog> onExecute(Context context, Bundle args) throws Throwable {
                List<EntityLog> items = new ArrayList<>();
                for (EntityLog log : logs)
                    if (account == null && folder == null && message == null) {
                        if (types.contains(log.type))
                            items.add(log);
                    } else {
                        if ((account == null || account.equals(log.account)) &&
                                (folder == null || folder.equals(log.folder)) &&
                                (message == null || message.equals(log.message)))
                            items.add(log);
                    }
                return items;
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityLog> items) {
                DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(selected, items), false);

                selected = items;

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
                    diff.dispatchUpdatesTo(AdapterLog.this);
                    if (callback != null)
                        callback.run();
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
            }
        }.serial().execute(context, owner, new Bundle(), "logs:filter");
    }

    public void setTypes(@NonNull List<EntityLog.Type> types) {
        set(all, account, folder, message, types, null);
    }

    private static class DiffCallback extends DiffUtil.Callback {
        private List<EntityLog> prev = new ArrayList<>();
        private List<EntityLog> next = new ArrayList<>();

        DiffCallback(List<EntityLog> prev, List<EntityLog> next) {
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
            EntityLog l1 = prev.get(oldItemPosition);
            EntityLog l2 = next.get(newItemPosition);
            return l1.id.equals(l2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityLog l1 = prev.get(oldItemPosition);
            EntityLog l2 = next.get(newItemPosition);
            return l1.equals(l2);
        }
    }

    @Override
    public long getItemId(int position) {
        return selected.get(position).id;
    }

    @Override
    public int getItemCount() {
        return selected.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_log, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntityLog log = selected.get(position);
        holder.bindTo(log);
    }
}
