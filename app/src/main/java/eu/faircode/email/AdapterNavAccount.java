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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AdapterNavAccount extends RecyclerView.Adapter<AdapterNavAccount.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private int colorUnread;
    private int textColorSecondary;

    private List<TupleAccountEx> items = new ArrayList<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();
    private DateFormat DTF;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private ImageView ivItem;
        private TextView tvItem;
        private TextView tvItemExtra;
        private ImageView ivExternal;
        private ImageView ivWarning;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ivItem = itemView.findViewById(R.id.ivItem);
            tvItem = itemView.findViewById(R.id.tvItem);
            tvItemExtra = itemView.findViewById(R.id.tvItemExtra);
            ivExternal = itemView.findViewById(R.id.ivExternal);
            ivWarning = itemView.findViewById(R.id.ivWarning);
        }

        private void wire() {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
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
                        account.name, NF.format(account.unseen)));

            tvItem.setTextColor(account.unseen == 0 ? textColorSecondary : colorUnread);
            tvItem.setTypeface(account.unseen == 0 ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);

            tvItemExtra.setText(account.last_connected == null ? null : DTF.format(account.last_connected));

            ivExternal.setVisibility(View.GONE);
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

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            TupleAccountEx account = items.get(pos);
            if (account == null)
                return false;

            Bundle args = new Bundle();
            args.putLong("id", account.id);

            new SimpleTask<EntityFolder>() {
                @Override
                protected EntityFolder onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    return db.folder().getFolderByType(id, EntityFolder.INBOX);
                }

                @Override
                protected void onExecuted(Bundle args, EntityFolder inbox) {
                    if (inbox != null) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                        .putExtra("account", inbox.account)
                                        .putExtra("folder", inbox.id)
                                        .putExtra("type", inbox.type));
                    }
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    // Ignored
                }
            }.execute(context, owner, args, "account:inbox");
            return true;
        }
    }

    AdapterNavAccount(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean highlight_unread = prefs.getBoolean("highlight_unread", false);
        this.colorUnread = Helper.resolveColor(context, highlight_unread ? R.attr.colorUnreadHighlight : android.R.attr.textColorPrimary);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        this.DTF = Helper.getTimeInstance(context, SimpleDateFormat.SHORT);

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
                    Objects.equals(a1.state, a2.state) &&
                    Objects.equals(a1.last_connected, a2.last_connected);
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
