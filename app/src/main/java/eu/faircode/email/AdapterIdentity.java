package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
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

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterIdentity extends RecyclerView.Adapter<AdapterIdentity.ViewHolder> {
    private Context context;

    private List<TupleIdentityEx> all = new ArrayList<>();
    private List<TupleIdentityEx> filtered = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;
        ImageView ivPrimary;
        TextView tvName;
        ImageView ivSync;
        TextView tvAccount;
        TextView tvEmail;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            ivPrimary = itemView.findViewById(R.id.ivPrimary);
            tvName = itemView.findViewById(R.id.tvName);
            ivSync = itemView.findViewById(R.id.ivSync);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }

        private void wire() {
            itemView.setOnClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
        }

        private void bindTo(TupleIdentityEx identity) {
            ivPrimary.setVisibility(identity.primary ? View.VISIBLE : View.GONE);
            tvName.setText(identity.name);
            ivSync.setVisibility(identity.synchronize ? View.VISIBLE : View.INVISIBLE);
            tvAccount.setText(identity.accountName);
            tvEmail.setText(String.format("%s/%s:%d", identity.email, identity.host, identity.port));
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;
            TupleIdentityEx identity = filtered.get(pos);

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

    public void set(@NonNull List<TupleIdentityEx> identities) {
        Log.i(Helper.TAG, "Set identities=" + identities.size());

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(identities, new Comparator<TupleIdentityEx>() {
            @Override
            public int compare(TupleIdentityEx i1, TupleIdentityEx i2) {
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
        private List<TupleIdentityEx> prev;
        private List<TupleIdentityEx> next;

        MessageDiffCallback(List<TupleIdentityEx> prev, List<TupleIdentityEx> next) {
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
            TupleIdentityEx i1 = prev.get(oldItemPosition);
            TupleIdentityEx i2 = next.get(newItemPosition);
            return i1.id.equals(i2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleIdentityEx i1 = prev.get(oldItemPosition);
            TupleIdentityEx i2 = next.get(newItemPosition);
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

        TupleIdentityEx identity = filtered.get(position);
        holder.bindTo(identity);

        holder.wire();
    }
}
