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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterRuleMatch extends RecyclerView.Adapter<AdapterRuleMatch.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private List<EntityMessage> items = new ArrayList<>();

    private DateFormat D;
    private DateFormat DTF;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView tvTime;
        private TextView tvSubject;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSubject = itemView.findViewById(R.id.tvSubject);
        }

        private void wire() {
        }

        private void unwire() {
        }

        private void bindTo(EntityMessage message) {
            tvTime.setText(D.format(message.received) + " " + DTF.format(message.received));
            tvSubject.setText(message.subject);
        }
    }

    AdapterRuleMatch(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);

        this.D = new SimpleDateFormat("E");
        this.DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterRuleMatch.this + " parent destroyed");
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<EntityMessage> messages) {
        Log.i("Set matched messages=" + messages.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, messages), false);

        items = messages;

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
        private List<EntityMessage> prev = new ArrayList<>();
        private List<EntityMessage> next = new ArrayList<>();

        DiffCallback(List<EntityMessage> prev, List<EntityMessage> next) {
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
            EntityMessage m1 = prev.get(oldItemPosition);
            EntityMessage m2 = next.get(newItemPosition);
            return m1.id.equals(m2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityMessage m1 = prev.get(oldItemPosition);
            EntityMessage m2 = next.get(newItemPosition);
            return m1.id.equals(m2.id);
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
        return new ViewHolder(inflater.inflate(R.layout.item_rule_match, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();
        EntityMessage message = items.get(position);
        holder.bindTo(message);
        holder.wire();
    }
}