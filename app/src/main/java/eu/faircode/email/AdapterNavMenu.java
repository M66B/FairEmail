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
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterNavMenu extends RecyclerView.Adapter<AdapterNavMenu.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private int colorUnread;
    private int textColorSecondary;

    private List<NavMenuItem> items = new ArrayList<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();

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

        private void bindTo(NavMenuItem menu) {
            ivItem.setImageResource(menu.getIcon());

            if (menu.getCount() == null)
                tvItem.setText(menu.getTitle());
            else
                tvItem.setText(context.getString(R.string.title_name_count,
                        context.getString(menu.getTitle()), NF.format(menu.getCount())));

            tvItem.setTextColor(menu.getCount() == null ? textColorSecondary : colorUnread);
            tvItem.setTypeface(menu.getCount() == null ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);

            tvItemExtra.setVisibility(View.GONE);

            ivExternal.setVisibility(menu.isExternal() ? View.VISIBLE : View.GONE);
            ivWarning.setVisibility(menu.hasWarning() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            NavMenuItem menu = items.get(pos);
            menu.onClick();
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            NavMenuItem menu = items.get(pos);
            return menu.onLongClick();
        }
    }

    AdapterNavMenu(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean highlight_unread = prefs.getBoolean("highlight_unread", true);
        int colorHighlight = prefs.getInt("highlight_color", Helper.resolveColor(context, R.attr.colorUnreadHighlight));
        this.colorUnread = (highlight_unread ? colorHighlight : Helper.resolveColor(context, R.attr.colorUnread));
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        setHasStableIds(true);
    }

    public void set(@NonNull List<NavMenuItem> menus) {
        Log.i("Set nav menus=" + menus.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, menus), false);

        items = menus;

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

    NavMenuItem get(int pos) {
        return items.get(pos);
    }

    private class DiffCallback extends DiffUtil.Callback {
        private List<NavMenuItem> prev = new ArrayList<>();
        private List<NavMenuItem> next = new ArrayList<>();

        DiffCallback(List<NavMenuItem> prev, List<NavMenuItem> next) {
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
            NavMenuItem m1 = prev.get(oldItemPosition);
            NavMenuItem m2 = next.get(newItemPosition);
            return m1.getTitle() == m2.getTitle();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            NavMenuItem m1 = prev.get(oldItemPosition);
            NavMenuItem m2 = next.get(newItemPosition);
            return m1.getIcon() == m2.getIcon() &&
                    m1.getTitle() == m2.getTitle() &&
                    Objects.equals(m1.getCount(), m2.getCount());
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getTitle();
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
        NavMenuItem menu = items.get(position);
        holder.bindTo(menu);
        holder.wire();
    }
}
