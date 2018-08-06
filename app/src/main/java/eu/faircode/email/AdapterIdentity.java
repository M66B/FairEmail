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
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdapterIdentity extends RecyclerView.Adapter<AdapterIdentity.ViewHolder> {
    private Context context;

    private List<EntityIdentity> all = new ArrayList<>();
    private List<EntityIdentity> filtered = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;
        ImageView ivPrimary;
        TextView tvName;
        ImageView ivSync;
        TextView tvHost;
        TextView tvEmail;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            ivPrimary = itemView.findViewById(R.id.ivPrimary);
            tvName = itemView.findViewById(R.id.tvName);
            ivSync = itemView.findViewById(R.id.ivSync);
            tvHost = itemView.findViewById(R.id.tvHost);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }

        private void wire() {
            itemView.setOnClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;
            EntityIdentity identity = filtered.get(pos);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivitySetup.ACTION_EDIT_IDENTITY)
                            .putExtra("id", identity.id));
        }
    }

    AdapterIdentity(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    public void set(@NonNull List<EntityIdentity> identities) {
        Log.i(Helper.TAG, "Set identities=" + identities.size());

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(identities, new Comparator<EntityIdentity>() {
            @Override
            public int compare(EntityIdentity i1, EntityIdentity i2) {
                return collator.compare(i1.host, i2.host);
            }
        });

        all.clear();
        all.addAll(identities);

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
        diff.dispatchUpdatesTo(AdapterIdentity.this);
    }

    private class MessageDiffCallback extends DiffUtil.Callback {
        private List<EntityIdentity> prev;
        private List<EntityIdentity> next;

        MessageDiffCallback(List<EntityIdentity> prev, List<EntityIdentity> next) {
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
            EntityIdentity i1 = prev.get(oldItemPosition);
            EntityIdentity i2 = next.get(newItemPosition);
            return i1.id.equals(i2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityIdentity i1 = prev.get(oldItemPosition);
            EntityIdentity i2 = next.get(newItemPosition);
            return i1.equals(i2);
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
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_identity, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityIdentity identity = filtered.get(position);

        holder.ivPrimary.setVisibility(identity.primary ? View.VISIBLE : View.GONE);
        holder.tvName.setText(identity.name);
        holder.ivSync.setVisibility(identity.synchronize ? View.VISIBLE : View.INVISIBLE);
        holder.tvHost.setText(String.format("%s:%d", identity.host, identity.port));
        holder.tvEmail.setText(identity.email);

        holder.wire();
    }
}
