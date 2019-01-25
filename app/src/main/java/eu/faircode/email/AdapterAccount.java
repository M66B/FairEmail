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

public class AdapterAccount extends RecyclerView.Adapter<AdapterAccount.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;

    private List<EntityAccount> all = new ArrayList<>();
    private List<EntityAccount> filtered = new ArrayList<>();

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
            tvLast = itemView.findViewById(R.id.tvLast);
            tvError = itemView.findViewById(R.id.tvError);
        }

        private void wire() {
            itemView.setOnClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
        }

        private void bindTo(EntityAccount account) {
            itemView.setActivated(account.tbd != null);
            vwColor.setBackgroundColor(account.color == null ? Color.TRANSPARENT : account.color);
            ivPrimary.setVisibility(account.primary ? View.VISIBLE : View.INVISIBLE);
            tvName.setText(account.name);
            ivSync.setImageResource(account.synchronize ? R.drawable.baseline_sync_24 : R.drawable.baseline_sync_disabled_24);
            tvUser.setText(account.user);
            tvHost.setText(String.format("%s:%d", account.host, account.port));

            if ("connected".equals(account.state))
                ivState.setImageResource(R.drawable.baseline_cloud_24);
            else if ("connecting".equals(account.state))
                ivState.setImageResource(R.drawable.baseline_cloud_queue_24);
            else if ("closing".equals(account.state))
                ivState.setImageResource(R.drawable.baseline_close_24);
            else
                ivState.setImageResource(R.drawable.baseline_cloud_off_24);
            ivState.setVisibility(account.synchronize ? View.VISIBLE : View.INVISIBLE);

            tvLast.setText(context.getString(R.string.title_last_connected,
                    account.last_connected == null ? "-" : df.format(account.last_connected)));

            tvError.setText(account.error);
            tvError.setVisibility(account.error == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            EntityAccount account = filtered.get(pos);
            if (account.tbd != null)
                return;

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivitySetup.ACTION_EDIT_ACCOUNT)
                            .putExtra("id", account.id));
        }
    }

    AdapterAccount(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);

        setHasStableIds(true);
    }

    public void set(@NonNull List<EntityAccount> accounts) {
        Log.i("Set accounts=" + accounts.size());

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(accounts, new Comparator<EntityAccount>() {
            @Override
            public int compare(EntityAccount a1, EntityAccount a2) {
                return collator.compare(a1.host, a2.host);
            }
        });

        all = accounts;

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new MessageDiffCallback(filtered, all));

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

    private class MessageDiffCallback extends DiffUtil.Callback {
        private List<EntityAccount> prev;
        private List<EntityAccount> next;

        MessageDiffCallback(List<EntityAccount> prev, List<EntityAccount> next) {
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
            EntityAccount f1 = prev.get(oldItemPosition);
            EntityAccount f2 = next.get(newItemPosition);
            return f1.id.equals(f2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityAccount f1 = prev.get(oldItemPosition);
            EntityAccount f2 = next.get(newItemPosition);
            return f1.equals(f2);
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
        return new ViewHolder(inflater.inflate(R.layout.item_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityAccount account = filtered.get(position);
        holder.bindTo(account);

        holder.wire();
    }
}
