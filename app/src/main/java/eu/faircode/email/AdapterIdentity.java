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

    private List<TupleIdentityEx> items = new ArrayList<>();

    private DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View view;
        private View vwColor;
        private ImageView ivSync;
        private ImageView ivPrimary;
        private TextView tvName;
        private TextView tvUser;
        private TextView tvHost;
        private ImageView ivState;
        private TextView tvAccount;
        private TextView tvLast;
        private TextView tvError;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            ivSync = itemView.findViewById(R.id.ivSync);
            ivPrimary = itemView.findViewById(R.id.ivPrimary);
            tvName = itemView.findViewById(R.id.tvName);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvHost = itemView.findViewById(R.id.tvHost);
            ivState = itemView.findViewById(R.id.ivState);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            tvLast = itemView.findViewById(R.id.tvLast);
            tvError = itemView.findViewById(R.id.tvError);
        }

        private void wire() {
            view.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
        }

        private void bindTo(TupleIdentityEx identity) {
            view.setActivated(identity.tbd != null);
            vwColor.setBackgroundColor(identity.color == null ? Color.TRANSPARENT : identity.color);

            ivSync.setImageResource(identity.synchronize ? R.drawable.baseline_sync_24 : R.drawable.baseline_sync_disabled_24);

            ivPrimary.setVisibility(identity.primary ? View.VISIBLE : View.GONE);
            tvName.setText(identity.getDisplayName());
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

            TupleIdentityEx identity = items.get(pos);
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
                int n = collator.compare(i1.getDisplayName(), i2.getDisplayName());
                if (n != 0)
                    return n;
                int e = collator.compare(i1.email, i2.email);
                if (e != 0)
                    return e;
                return i1.id.compareTo(i2.id);
            }
        });

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, identities), false);

        items = identities;

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
        private List<TupleIdentityEx> prev = new ArrayList<>();
        private List<TupleIdentityEx> next = new ArrayList<>();

        DiffCallback(List<TupleIdentityEx> prev, List<TupleIdentityEx> next) {
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
        return items.get(position).id;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_identity, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleIdentityEx identity = items.get(position);
        holder.bindTo(identity);

        holder.wire();
    }
}
