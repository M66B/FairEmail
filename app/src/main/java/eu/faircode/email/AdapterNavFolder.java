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

public class AdapterNavFolder extends RecyclerView.Adapter<AdapterNavFolder.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private List<TupleFolderNav> items = new ArrayList<>();

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

        private void bindTo(TupleFolderNav folder) {
            if (EntityFolder.OUTBOX.equals(folder.type)) {
                if ("syncing".equals(folder.sync_state))
                    ivItem.setImageResource(R.drawable.baseline_compare_arrows_24);
                else
                    ivItem.setImageResource(R.drawable.baseline_send_24);

                ivItem.clearColorFilter();
            } else {
                if ("syncing".equals(folder.sync_state))
                    ivItem.setImageResource(R.drawable.baseline_compare_arrows_24);
                else if ("downloading".equals(folder.sync_state))
                    ivItem.setImageResource(R.drawable.baseline_cloud_download_24);
                else if (folder.executing > 0)
                    ivItem.setImageResource(R.drawable.baseline_dns_24);
                else
                    ivItem.setImageResource("connected".equals(folder.state)
                            ? R.drawable.baseline_folder_24
                            : R.drawable.baseline_folder_open_24);

                if (folder.accountColor == null || !Helper.isPro(context))
                    ivItem.clearColorFilter();
                else
                    ivItem.setColorFilter(folder.accountColor);
            }

            int count = (EntityFolder.OUTBOX.equals(folder.type) ? folder.operations : folder.unseen);

            if (count == 0)
                tvItem.setText(folder.getDisplayName(context));
            else
                tvItem.setText(context.getString(R.string.title_name_count,
                        folder.getDisplayName(context), nf.format(count)));

            tvItem.setTextColor(Helper.resolveColor(context,
                    count == 0 ? android.R.attr.textColorSecondary : R.attr.colorUnread));

            ivWarning.setVisibility(folder.error == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleFolderNav folder = items.get(pos);
            if (folder == null)
                return;

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                            .putExtra("account", folder.account)
                            .putExtra("folder", folder.id));
        }
    }

    AdapterNavFolder(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    public void set(@NonNull List<TupleFolderNav> folders) {
        Log.i("Set nav folders=" + folders.size());

        if (folders.size() > 0)
            Collections.sort(folders, folders.get(0).getComparator(context));

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, folders), false);

        items = folders;

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
        private List<TupleFolderNav> prev = new ArrayList<>();
        private List<TupleFolderNav> next = new ArrayList<>();

        DiffCallback(List<TupleFolderNav> prev, List<TupleFolderNav> next) {
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
            TupleFolderNav f1 = prev.get(oldItemPosition);
            TupleFolderNav f2 = next.get(newItemPosition);
            return f1.id.equals(f2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleFolderNav f1 = prev.get(oldItemPosition);
            TupleFolderNav f2 = next.get(newItemPosition);
            return (f1.name.equals(f2.name) &&
                    f1.type.equals(f2.type) &&
                    Objects.equals(f1.display, f2.display) &&
                    Objects.equals(f1.accountColor, f2.accountColor) &&
                    Objects.equals(f1.state, f2.state) &&
                    Objects.equals(f1.sync_state, f2.sync_state) &&
                    f1.unseen == f2.unseen &&
                    f1.operations == f2.operations &&
                    f1.executing == f2.executing);
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
        TupleFolderNav folder = items.get(position);
        holder.bindTo(folder);
        holder.wire();
    }
}
