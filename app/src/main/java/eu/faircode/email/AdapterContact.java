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

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterContact extends RecyclerView.Adapter<AdapterContact.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private boolean contacts;

    private List<EntityContact> all = new ArrayList<>();
    private List<EntityContact> filtered = new ArrayList<>();

    private static NumberFormat nf = NumberFormat.getNumberInstance();

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private ImageView ivType;
        private ImageView ivAvatar;
        private TextView tvName;
        private TextView tvEmail;
        private TextView tvTimes;
        private TextView tvLast;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            ivType = itemView.findViewById(R.id.ivType);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTimes = itemView.findViewById(R.id.tvTimes);
            tvLast = itemView.findViewById(R.id.tvLast);
        }

        private void bindTo(EntityContact contact) {
            if (contact.type == EntityContact.TYPE_FROM)
                ivType.setImageResource(R.drawable.baseline_mail_24);
            else if (contact.type == EntityContact.TYPE_TO)
                ivType.setImageResource(R.drawable.baseline_send_24);
            else
                ivType.setImageDrawable(null);

            if (contact.avatar == null || !contacts)
                ivAvatar.setImageDrawable(null);
            else
                ivAvatar.setImageURI(Uri.parse(contact.avatar + "/photo"));

            tvName.setText(contact.name == null ? contact.email : contact.name);
            tvEmail.setText(contact.email);
            tvTimes.setText(nf.format(contact.times_contacted));
            tvLast.setText(contact.last_contacted == null ? null
                    : DateUtils.getRelativeTimeSpanString(context, contact.last_contacted));
        }
    }

    AdapterContact(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        setHasStableIds(true);
    }

    public void set(@NonNull List<EntityContact> contacts) {
        Log.i("Set contacts=" + contacts.size());

        all = contacts;

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
        private List<EntityContact> prev;
        private List<EntityContact> next;

        DiffCallback(List<EntityContact> prev, List<EntityContact> next) {
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
            EntityContact c1 = prev.get(oldItemPosition);
            EntityContact c2 = next.get(newItemPosition);
            return c1.id.equals(c2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityContact c1 = prev.get(oldItemPosition);
            EntityContact c2 = next.get(newItemPosition);
            return c1.equals(c2);
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
        return new ViewHolder(inflater.inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntityContact contact = filtered.get(position);
        holder.bindTo(contact);
    }
}
