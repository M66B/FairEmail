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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AdapterNavAccount extends RecyclerView.Adapter<AdapterNavAccount.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private List<TupleAccountEx> items = new ArrayList<>();

    private NumberFormat nf = NumberFormat.getNumberInstance();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View view;
        private ImageView ivItem;
        private TextView tvItem;
        private ImageView ivWarning;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ivItem = itemView.findViewById(R.id.ivItem);
            tvItem = itemView.findViewById(R.id.tvItem);
            ivWarning = itemView.findViewById(R.id.ivWarning);
        }

        private void wire() {
            view.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
        }

        private void bindTo(TupleAccountEx account) {
            ivItem.setImageResource("connected".equals(account.state)
                    ? account.primary ? R.drawable.baseline_folder_special_24 : R.drawable.baseline_folder_24
                    : R.drawable.baseline_folder_open_24);

            if (account.color == null)
                ivItem.clearColorFilter();
            else
                ivItem.setColorFilter(account.color);

            if (account.unseen == 0)
                tvItem.setText(account.name);
            else
                tvItem.setText(context.getString(R.string.title_name_count,
                        account.name, nf.format(account.unseen)));

            tvItem.setTextColor(Helper.resolveColor(context,
                    account.unseen == 0 ? android.R.attr.textColorSecondary : R.attr.colorUnread));

            ivWarning.setVisibility(account.error == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleAccountEx account = items.get(pos);
            if (account == null)
                return;

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_VIEW_FOLDERS)
                            .putExtra("id", account.id));
        }
    }

    AdapterNavAccount(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    public void set(@NonNull List<TupleAccountEx> accounts) {
        Log.i("Set nav accounts=" + accounts.size());

        if (accounts.size() > 0)
            Collections.sort(accounts, accounts.get(0).getComparator(context));

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
            TupleAccountEx a1 = prev.get(oldItemPosition);
            TupleAccountEx a2 = next.get(newItemPosition);
            return a1.id.equals(a2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleAccountEx a1 = prev.get(oldItemPosition);
            TupleAccountEx a2 = next.get(newItemPosition);
            return Objects.equals(a1.name, a2.name) &&
                    Objects.equals(a1.color, a2.color) &&
                    a1.unseen == a2.unseen &&
                    Objects.equals(a1.state, a2.state);
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
        return new ViewHolder(inflater.inflate(R.layout.item_nav, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();
        TupleAccountEx account = items.get(position);
        holder.bindTo(account);
        holder.wire();
    }
}
