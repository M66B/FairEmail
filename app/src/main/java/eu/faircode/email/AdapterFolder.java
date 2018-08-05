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
import android.graphics.Typeface;
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

public class AdapterFolder extends RecyclerView.Adapter<AdapterFolder.ViewHolder> {
    private Context context;

    private List<TupleFolderEx> all = new ArrayList<>();
    private List<TupleFolderEx> filtered = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        View itemView;
        TextView tvName;
        TextView tvAfter;
        ImageView ivSync;
        TextView tvCount;
        TextView tvType;
        TextView tvAccount;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            tvName = itemView.findViewById(R.id.tvName);
            tvAfter = itemView.findViewById(R.id.tvAfter);
            ivSync = itemView.findViewById(R.id.ivSync);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvType = itemView.findViewById(R.id.tvType);
            tvAccount = itemView.findViewById(R.id.tvAccount);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;
            TupleFolderEx folder = filtered.get(pos);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                            .putExtra("folder", folder.id));
        }

        @Override
        public boolean onLongClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;
            TupleFolderEx folder = filtered.get(pos);

            if (!EntityFolder.TYPE_OUTBOX.equals(folder.type)) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_EDIT_FOLDER)
                                .putExtra("id", folder.id));
                return true;
            }

            return false;
        }
    }

    AdapterFolder(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    public void set(@NonNull List<TupleFolderEx> folders) {
        Log.i(Helper.TAG, "Set folders=" + folders.size());

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(folders, new Comparator<TupleFolderEx>() {
            @Override
            public int compare(TupleFolderEx f1, TupleFolderEx f2) {
                int s = EntityFolder.isUser(f1.type).compareTo(EntityFolder.isUser(f2.type));
                if (s == 0) {
                    int a = collator.compare(
                            f1.accountName == null ? "" : f1.accountName,
                            f2.accountName == null ? "" : f2.accountName);
                    if (a == 0)
                        return collator.compare(f1.name, f2.name);
                    else
                        return a;
                } else
                    return s;
            }
        });

        all.clear();
        all.addAll(folders);

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
        diff.dispatchUpdatesTo(AdapterFolder.this);
    }

    private class MessageDiffCallback extends DiffUtil.Callback {
        private List<TupleFolderEx> prev;
        private List<TupleFolderEx> next;

        MessageDiffCallback(List<TupleFolderEx> prev, List<TupleFolderEx> next) {
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
            TupleFolderEx f1 = prev.get(oldItemPosition);
            TupleFolderEx f2 = next.get(newItemPosition);
            return f1.id.equals(f2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleFolderEx f1 = prev.get(oldItemPosition);
            TupleFolderEx f2 = next.get(newItemPosition);
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
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleFolderEx folder = filtered.get(position);

        String name = Helper.localizeFolderName(context, folder.name);
        if (folder.unseen > 0)
            holder.tvName.setText(context.getString(R.string.title_folder_unseen, name, folder.unseen));
        else
            holder.tvName.setText(name);
        holder.tvName.setTypeface(null, folder.unseen > 0 ? Typeface.BOLD : Typeface.NORMAL);

        holder.tvAfter.setText(Integer.toString(folder.after));
        holder.tvAfter.setVisibility(folder.synchronize ? View.VISIBLE : View.INVISIBLE);

        holder.ivSync.setVisibility(folder.synchronize ? View.VISIBLE : View.INVISIBLE);
        holder.tvCount.setText(Integer.toString(folder.messages));

        int resid = context.getResources().getIdentifier(
                "title_folder_" + folder.type.toLowerCase(),
                "string",
                context.getPackageName());
        holder.tvType.setText(resid > 0 ? context.getString(resid) : folder.type);

        holder.tvAccount.setText(folder.accountName);

        holder.wire();
    }
}
