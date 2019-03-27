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
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.Collator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterAccount extends RecyclerView.Adapter<AdapterAccount.ViewHolder> {
    private Context context;
    private boolean settings;
    private LayoutInflater inflater;

    private int colorUnread;
    private int textColorSecondary;

    private List<TupleAccountEx> items = new ArrayList<>();

    private NumberFormat nf = NumberFormat.getNumberInstance();
    private DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View view;
        private View vwColor;
        private ImageView ivPrimary;
        private ImageView ivNotify;
        private TextView tvName;
        private ImageView ivSync;
        private TextView tvUser;
        private ImageView ivState;
        private TextView tvHost;
        private TextView tvLast;
        private TextView tvDrafts;
        private TextView tvWarning;
        private TextView tvError;
        private Group grpSettings;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            ivSync = itemView.findViewById(R.id.ivSync);
            ivPrimary = itemView.findViewById(R.id.ivPrimary);
            ivNotify = itemView.findViewById(R.id.ivNotify);
            tvName = itemView.findViewById(R.id.tvName);
            tvUser = itemView.findViewById(R.id.tvUser);
            ivState = itemView.findViewById(R.id.ivState);
            tvHost = itemView.findViewById(R.id.tvHost);
            tvLast = itemView.findViewById(R.id.tvLast);
            tvDrafts = itemView.findViewById(R.id.tvDrafts);
            tvWarning = itemView.findViewById(R.id.tvWarning);
            tvError = itemView.findViewById(R.id.tvError);
            grpSettings = itemView.findViewById(R.id.grpSettings);
        }

        private void wire() {
            view.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
        }

        private void bindTo(TupleAccountEx account) {
            view.setActivated(account.tbd != null);
            vwColor.setBackgroundColor(account.color == null ? Color.TRANSPARENT : account.color);

            ivSync.setImageResource(account.synchronize ? R.drawable.baseline_sync_24 : R.drawable.baseline_sync_disabled_24);

            ivPrimary.setVisibility(account.primary ? View.VISIBLE : View.GONE);
            ivNotify.setVisibility(account.notify ? View.VISIBLE : View.GONE);

            if (settings)
                tvName.setText(account.name);
            else {
                if (account.unseen > 0)
                    tvName.setText(context.getString(R.string.title_name_count, account.name, nf.format(account.unseen)));
                else
                    tvName.setText(account.name);

                tvName.setTypeface(null, account.unseen > 0 ? Typeface.BOLD : Typeface.NORMAL);
                tvName.setTextColor(account.unseen > 0 ? colorUnread : textColorSecondary);
            }

            tvUser.setText(account.user);

            if ("connected".equals(account.state))
                ivState.setImageResource(R.drawable.baseline_cloud_24);
            else if ("connecting".equals(account.state))
                ivState.setImageResource(R.drawable.baseline_cloud_queue_24);
            else if ("closing".equals(account.state))
                ivState.setImageResource(R.drawable.baseline_close_24);
            else
                ivState.setImageResource(R.drawable.baseline_cloud_off_24);
            ivState.setVisibility(account.synchronize ? View.VISIBLE : View.INVISIBLE);

            tvHost.setText(String.format("%s:%d", account.host, account.port));
            tvLast.setText(context.getString(R.string.title_last_connected,
                    account.last_connected == null ? "-" : df.format(account.last_connected)));

            tvDrafts.setVisibility(account.drafts || !settings ? View.GONE : View.VISIBLE);

            tvWarning.setText(account.warning);
            tvWarning.setVisibility(account.warning == null || !settings ? View.GONE : View.VISIBLE);

            tvError.setText(account.error);
            tvError.setVisibility(account.error == null ? View.GONE : View.VISIBLE);

            grpSettings.setVisibility(settings ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleAccountEx account = items.get(pos);
            if (account.tbd != null)
                return;

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(settings ? ActivitySetup.ACTION_EDIT_ACCOUNT : ActivityView.ACTION_VIEW_FOLDERS)
                            .putExtra("id", account.id));
        }
    }

    AdapterAccount(Context context, boolean settings) {
        this.context = context;
        this.settings = settings;
        this.inflater = LayoutInflater.from(context);

        this.colorUnread = Helper.resolveColor(context, R.attr.colorUnread);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        setHasStableIds(true);
    }

    public void set(@NonNull List<TupleAccountEx> accounts) {
        Log.i("Set accounts=" + accounts.size());

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(accounts, new Comparator<TupleAccountEx>() {
            @Override
            public int compare(TupleAccountEx a1, TupleAccountEx a2) {
                int n = collator.compare(a1.name, a2.name);
                if (n != 0)
                    return n;
                int e = collator.compare(a1.user, a2.user);
                if (e != 0)
                    return e;
                return a1.id.compareTo(a2.id);
            }
        });

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, accounts), false);

        items = accounts;

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
        private List<TupleAccountEx> prev = new ArrayList<>();
        private List<TupleAccountEx> next = new ArrayList<>();

        DiffCallback(List<TupleAccountEx> prev, List<TupleAccountEx> next) {
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
            TupleAccountEx f1 = prev.get(oldItemPosition);
            TupleAccountEx f2 = next.get(newItemPosition);
            return f1.id.equals(f2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleAccountEx f1 = prev.get(oldItemPosition);
            TupleAccountEx f2 = next.get(newItemPosition);
            return f1.uiEquals(f2);
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
        return new ViewHolder(inflater.inflate(R.layout.item_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleAccountEx account = items.get(position);
        holder.bindTo(account);

        holder.wire();
    }
}
