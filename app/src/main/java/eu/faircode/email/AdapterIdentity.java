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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    private LayoutInflater inflater;

    private List<TupleIdentityEx> all = new ArrayList<>();
    private List<TupleIdentityEx> filtered = new ArrayList<>();

    private static final DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;
        View vwColor;
        ImageView ivPrimary;
        TextView tvName;
        ImageView ivSync;
        TextView tvUser;
        TextView tvHost;
        ImageView ivState;
        TextView tvAccount;
        TextView tvLast;
        TextView tvError;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            ivPrimary = itemView.findViewById(R.id.ivPrimary);
            tvName = itemView.findViewById(R.id.tvName);
            ivSync = itemView.findViewById(R.id.ivSync);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvHost = itemView.findViewById(R.id.tvHost);
            ivState = itemView.findViewById(R.id.ivState);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            tvLast = itemView.findViewById(R.id.tvLast);
            tvError = itemView.findViewById(R.id.tvError);
        }

        private void wire() {
            itemView.setOnClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
        }

        private void bindTo(TupleIdentityEx identity) {
            itemView.setActivated(identity.tbd != null);
            vwColor.setBackgroundColor(identity.color == null ? Color.TRANSPARENT : identity.color);
            ivPrimary.setVisibility(identity.primary ? View.VISIBLE : View.INVISIBLE);
            tvName.setText(identity.getDisplayName());
            ivSync.setImageResource(identity.synchronize ? R.drawable.baseline_sync_24 : R.drawable.baseline_sync_disabled_24);
            tvUser.setText(identity.email);

            if ("connected".equals(identity.state))
                ivState.setImageResource(R.drawable.baseline_cloud_24);
            else if ("connecting".equals(identity.state))
                ivState.setImageResource(R.drawable.baseline_cloud_queue_24);
            else
                ivState.setImageDrawable(null);
            ivState.setVisibility(identity.synchronize ? View.VISIBLE : View.INVISIBLE);

            tvHost.setText(String.format("%s:%d", identity.host, identity.port));
            tvAccount.setText(identity.accountName);
            tvLast.setText(context.getString(R.string.title_last_connected,
                    identity.last_connected == null ? "-" : df.format(identity.last_connected)));

            tvError.setText(identity.error);
            tvError.setVisibility(identity.error == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleIdentityEx identity = filtered.get(pos);
            if (identity.tbd != null)
                return;

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivitySetup.ACTION_EDIT_IDENTITY)
                            .putExtra("id", identity.id));
        }
    }

    AdapterIdentity(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    public void set(@NonNull List<TupleIdentityEx> identities) {
        Log.i("Set identities=" + identities.size());

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(identities, new Comparator<TupleIdentityEx>() {
            @Override
            public int compare(TupleIdentityEx i1, TupleIdentityEx i2) {
                return collator.compare(i1.host, i2.host);
            }
        });

        all = identities;

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
        private List<TupleIdentityEx> prev;
        private List<TupleIdentityEx> next;

        DiffCallback(List<TupleIdentityEx> prev, List<TupleIdentityEx> next) {
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
        return new ViewHolder(inflater.inflate(R.layout.item_identity, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleIdentityEx identity = filtered.get(position);
        holder.bindTo(identity);

        holder.wire();
    }
}
