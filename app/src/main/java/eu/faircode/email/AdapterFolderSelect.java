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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdapterFolderSelect extends RecyclerView.Adapter<AdapterFolderSelect.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private IFolderSelectedListener listener;
    private LayoutInflater inflater;

    private List<EntityFolder> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View view;
        private ImageView ivType;
        private TextView tvName;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ivType = itemView.findViewById(R.id.ivType);
            tvName = itemView.findViewById(R.id.tvName);
        }

        private void wire() {
            view.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
        }

        private void bindTo(EntityFolder folder) {
            ivType.setImageResource(EntityFolder.getIcon(folder.type));
            tvName.setText(folder.getDisplayName(context));
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            EntityFolder folder = items.get(pos);
            if (folder != null)
                listener.onFolderSelected(folder);
        }
    }

    AdapterFolderSelect(Context context, LifecycleOwner owner, IFolderSelectedListener listener) {
        this.context = context;
        this.owner = owner;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    public void set(@NonNull List<EntityFolder> folders) {
        Log.i("Set folders=" + folders.size());

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
        private List<EntityFolder> prev = new ArrayList<>();
        private List<EntityFolder> next = new ArrayList<>();

        DiffCallback(List<EntityFolder> prev, List<EntityFolder> next) {
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
            EntityFolder m1 = prev.get(oldItemPosition);
            EntityFolder m2 = next.get(newItemPosition);
            return m1.id.equals(m2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityFolder m1 = prev.get(oldItemPosition);
            EntityFolder m2 = next.get(newItemPosition);
            return m1.id.equals(m2.id);
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
        return new ViewHolder(inflater.inflate(R.layout.item_folder_select, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();
        EntityFolder folder = items.get(position);
        holder.bindTo(folder);
        holder.wire();
    }

    interface IFolderSelectedListener {
        void onFolderSelected(EntityFolder folder);
    }
}