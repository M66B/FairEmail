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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AdapterOrder extends RecyclerView.Adapter<AdapterOrder.ViewHolder> {
    private Fragment parentFragment;
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private List<EntityOrder> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView tvTitle;
        private TextView tvSubTitle;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
        }

        private void bindTo(EntityOrder item) {
            String[] text = item.getSortTitle(context);
            tvTitle.setText(text[0]);
            tvSubTitle.setText(text[1]);
        }
    }

    AdapterOrder(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterOrder.this + " parent destroyed");
                AdapterOrder.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<EntityOrder> items) {
        Log.i("Set sort items=" + items.size());

        if (items.size() > 0)
            Collections.sort(items, items.get(0).getComparator(context));

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(this.items, items), false);

        this.items = items;

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
        private List<EntityOrder> prev = new ArrayList<>();
        private List<EntityOrder> next = new ArrayList<>();

        DiffCallback(List<EntityOrder> prev, List<EntityOrder> next) {
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
            EntityOrder s1 = prev.get(oldItemPosition);
            EntityOrder s2 = next.get(newItemPosition);
            return s1.getSortId().equals(s2.getSortId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityOrder s1 = prev.get(oldItemPosition);
            EntityOrder s2 = next.get(newItemPosition);
            return (Objects.equals(s1.order, s2.order));
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getSortId();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    List<EntityOrder> getItems() {
        return this.items;
    }

    void onMove(int from, int to) {
        if (from < 0 || from >= items.size() ||
                to < 0 || to >= items.size())
            return;

        if (from < to)
            for (int i = from; i < to; i++)
                Collections.swap(items, i, i + 1);
        else
            for (int i = from; i > to; i--)
                Collections.swap(items, i, i - 1);

        notifyItemMoved(from, to);
    }

    void onReset() {
        List<EntityOrder> list = new ArrayList<>();
        for (EntityOrder item : items) {
            item.order = null;
            list.add(item);
        }
        set(list);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntityOrder item = items.get(position);
        holder.bindTo(item);
    }
}

