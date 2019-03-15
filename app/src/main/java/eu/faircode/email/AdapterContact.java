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
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterContact extends RecyclerView.Adapter<AdapterContact.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;
    private boolean contacts;
    private int colorAccent;
    private int textColorSecondary;

    private List<EntityContact> all = new ArrayList<>();
    private List<EntityContact> filtered = new ArrayList<>();

    private static NumberFormat nf = NumberFormat.getNumberInstance();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View itemView;
        private ImageView ivType;
        private ImageView ivAvatar;
        private TextView tvName;
        private TextView tvEmail;
        private TextView tvTimes;
        private TextView tvLast;
        private ImageView ivFavorite;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            ivType = itemView.findViewById(R.id.ivType);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTimes = itemView.findViewById(R.id.tvTimes);
            tvLast = itemView.findViewById(R.id.tvLast);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);
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

            ivFavorite.setImageResource(contact.favorite ? R.drawable.baseline_star_24 : R.drawable.baseline_star_border_24);
            ivFavorite.setImageTintList(ColorStateList.valueOf(contact.favorite ? colorAccent : textColorSecondary));
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            EntityContact contact = filtered.get(pos);

            Bundle args = new Bundle();
            args.putLong("id", contact.id);
            args.putBoolean("favorite", !contact.favorite);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean favorite = args.getBoolean("favorite");

                    DB db = DB.getInstance(context);
                    db.contact().setContactFavorite(id, favorite);

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Void data) {
                    Shortcuts.update(context, owner);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "contact:favorite");
        }

        @Override
        public boolean onLongClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            EntityContact contact = filtered.get(pos);

            Bundle args = new Bundle();
            args.putLong("id", contact.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    db.contact().deleteContact(id);

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Void data) {
                    Shortcuts.update(context, owner);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "contact:delete");

            return true;
        }
    }

    AdapterContact(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        this.contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        this.colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
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
        holder.unwire();
        EntityContact contact = filtered.get(position);
        holder.bindTo(contact);
        holder.wire();
    }
}
