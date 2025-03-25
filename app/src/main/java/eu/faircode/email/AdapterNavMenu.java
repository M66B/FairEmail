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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO;
import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterNavMenu extends RecyclerView.Adapter<AdapterNavMenu.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private boolean nav_count;
    private boolean nav_count_pinned;

    private int colorUnread;
    private int colorControlNormal;
    private int textColorSecondary;
    private int colorWarning;

    private boolean expanded = true;
    private List<NavMenuItem> items = new ArrayList<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private ImageView ivItem;
        private ImageView ivBadge;
        private TextView tvCount;
        private TextView tvItem;
        private TextView tvItemExtra;
        private ImageView ivExtra;
        private ImageView ivWarning;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ivItem = itemView.findViewById(R.id.ivItem);
            ivBadge = itemView.findViewById(R.id.ivBadge);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvItem = itemView.findViewById(R.id.tvItem);
            tvItemExtra = itemView.findViewById(R.id.tvItemExtra);
            ivExtra = itemView.findViewById(R.id.ivExtra);
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

            Integer color = menu.getColor();
            ivItem.setImageTintList(ColorStateList.valueOf(color == null
                    ? colorControlNormal : color));

            Integer count = menu.getCount();
            ivBadge.setVisibility(count == null || count == 0 || expanded
                    ? View.GONE : View.VISIBLE);

            tvCount.setText(Helper.formatNumber(count, 99, NF));
            tvCount.setVisibility(count == null || count == 0 || expanded || !nav_count_pinned
                    ? View.GONE : View.VISIBLE);

            if (count == null)
                tvItem.setText(context.getString(menu.getTitle()));
            else
                tvItem.setText(context.getString(R.string.title_name_count,
                        context.getString(menu.getTitle()), NF.format(count)));

            tvItem.setTextColor(count == null ? (color == null ? textColorSecondary : color) : colorUnread);
            tvItem.setTypeface(count == null ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);
            tvItem.setVisibility(expanded ? View.VISIBLE : View.GONE);

            ivItem.setContentDescription(tvItem.getText());
            ivItem.setImportantForAccessibility(expanded ? IMPORTANT_FOR_ACCESSIBILITY_NO : IMPORTANT_FOR_ACCESSIBILITY_YES);

            tvItemExtra.setText(menu.getSubtitle());
            tvItemExtra.setVisibility(menu.getSubtitle() != null && expanded ? View.VISIBLE : View.GONE);

            ivExtra.setImageResource(menu.getExtraIcon());
            ivExtra.setVisibility(menu.getExtraIcon() != 0 && expanded ? View.VISIBLE : View.GONE);
            ivWarning.setVisibility(menu.hasWarning() && expanded ? View.VISIBLE : View.GONE);
            view.setBackgroundColor(menu.hasWarning() && !expanded ? colorWarning : Color.TRANSPARENT);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            NavMenuItem menu = items.get(pos);
            if (menu != null)
                menu.onClick();
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            NavMenuItem menu = items.get(pos);
            return (menu != null && menu.onLongClick());
        }
    }

    AdapterNavMenu(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.nav_count = prefs.getBoolean("nav_count", false);
        this.nav_count_pinned = prefs.getBoolean("nav_count_pinned", false);
        boolean highlight_unread = prefs.getBoolean("highlight_unread", true);
        int colorHighlight = prefs.getInt("highlight_color", Helper.resolveColor(context, R.attr.colorUnreadHighlight));
        this.colorUnread = (highlight_unread ? colorHighlight : Helper.resolveColor(context, R.attr.colorUnread));
        this.colorControlNormal = Helper.resolveColor(context, androidx.appcompat.R.attr.colorControlNormal);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
        this.colorWarning = ColorUtils.setAlphaComponent(Helper.resolveColor(context, R.attr.colorWarning), 128);

        setHasStableIds(true);
    }

    public void set(@NonNull List<NavMenuItem> menus, boolean expanded) {
        Log.i("Set nav menus=" + menus.size() + " expanded=" + expanded);

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, menus), false);

        this.expanded = expanded;
        this.items = menus;

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

        try {
            diff.dispatchUpdatesTo(this);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        notifyDataSetChanged();
    }

    NavMenuItem get(int pos) {
        return items.get(pos);
    }

    int getPosition(NavMenuItem item) {
        return items.indexOf(item);
    }

    private static class DiffCallback extends DiffUtil.Callback {
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
            return m1.equals(m2);
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
