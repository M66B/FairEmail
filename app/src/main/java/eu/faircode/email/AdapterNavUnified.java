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

import java.text.Collator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AdapterNavUnified extends RecyclerView.Adapter<AdapterNavUnified.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private int dp6;
    private int dp12;
    private boolean nav_count;
    private boolean nav_count_pinned;
    private boolean nav_unseen_drafts;
    private boolean nav_categories;
    private int colorUnread;
    private int textColorSecondary;
    private boolean show_unexposed;

    private boolean expanded = true;
    private List<TupleFolderUnified> items = new ArrayList<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
        }

        private void unwire() {
            view.setOnClickListener(null);
        }

        private void bindTo(TupleFolderUnified folder) {
            int start = (folder.category == null ? 0 : (expanded ? dp12 : dp6));
            view.setPaddingRelative(start, 0, 0, 0);

            if (folder.type == null)
                ivItem.setImageResource(R.drawable.twotone_all_inbox_24);
            else if (EntityFolder.INBOX.equals(folder.type))
                ivItem.setImageResource(folder.folders > 1 && !folder.unified
                        ? R.drawable.twotone_all_inbox_24
                        : R.drawable.twotone_move_to_inbox_24);
            else if (EntityFolder.OUTBOX.equals(folder.type)) {
                if ("syncing".equals(folder.sync_state))
                    ivItem.setImageResource(R.drawable.twotone_compare_arrows_24);
                else
                    ivItem.setImageResource(EntityFolder.getIcon(folder.type));
            } else
                ivItem.setImageResource(EntityFolder.getIcon(folder.type));

            if (folder.color != null && folder.colorCount == 1)
                ivItem.setColorFilter(folder.color);
            else
                ivItem.clearColorFilter();

            int count;
            if (EntityFolder.OUTBOX.equals(folder.type) ||
                    (!nav_unseen_drafts && EntityFolder.DRAFTS.equals(folder.type)))
                count = folder.messages;
            else
                count = folder.unseen;

            ivBadge.setVisibility(count == 0 || expanded ? View.GONE : View.VISIBLE);

            tvCount.setText(Helper.formatNumber(count, 99, NF));
            tvCount.setVisibility(count == 0 || expanded || !nav_count_pinned ? View.GONE : View.VISIBLE);

            String name = (folder.category != null ? folder.category :
                    (folder.type == null
                            ? context.getString(R.string.title_folder_unified)
                            : EntityFolder.localizeType(context, folder.type)));

            int unexposed = (show_unexposed ? folder.unexposed : 0);
            if (count > 0 || unexposed > 0) {
                StringBuilder sb = new StringBuilder();
                if (count > 0)
                    sb.append(NF.format(count));
                if (unexposed > 0)
                    sb.append('\u2B51');
                tvItem.setText(context.getString(R.string.title_name_count, name, sb));
            } else
                tvItem.setText(name);

            tvItem.setTextColor(count == 0 ? textColorSecondary : colorUnread);
            tvItem.setTypeface(count == 0 ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);
            tvItem.setVisibility(expanded ? View.VISIBLE : View.GONE);

            ivItem.setContentDescription(tvItem.getText());
            ivItem.setImportantForAccessibility(expanded ? IMPORTANT_FOR_ACCESSIBILITY_NO : IMPORTANT_FOR_ACCESSIBILITY_YES);

            tvItemExtra.setText(NF.format(folder.messages));
            tvItemExtra.setVisibility(nav_count && expanded ? View.VISIBLE : View.GONE);

            ivExtra.setVisibility(View.GONE);
            ivWarning.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleFolderUnified folder = items.get(pos);
            if (folder == null)
                return;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String startup = prefs.getString("startup", "unified");

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            if (EntityFolder.OUTBOX.equals(folder.type))
                lbm.sendBroadcast(new Intent(ActivityView.ACTION_VIEW_OUTBOX));
            else if ("inbox".equals(startup) && EntityFolder.INBOX.equals(folder.type) && folder.category == null)
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                .putExtra("type", (String) null)
                                .putExtra("unified", true));
            else if (folder.folders > 1 || folder.type == null || folder.category != null)
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                .putExtra("type", folder.type)
                                .putExtra("category", folder.category)
                                .putExtra("unified", folder.type == null));
            else {
                Bundle args = new Bundle();
                args.putString("type", folder.type);

                new SimpleTask<EntityFolder>() {
                    @Override
                    protected EntityFolder onExecute(Context context, Bundle args) throws Throwable {
                        String type = args.getString("type");

                        DB db = DB.getInstance(context);
                        List<EntityFolder> folders = db.folder().getFoldersByType(type);

                        return (folders != null && folders.size() == 1 ? folders.get(0) : null);
                    }

                    @Override
                    protected void onExecuted(Bundle args, EntityFolder one) {
                        if (one == null)
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                            .putExtra("type", folder.type));
                        else
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                            .putExtra("account", one.account)
                                            .putExtra("folder", one.id)
                                            .putExtra("type", one.type));
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                        .putExtra("type", folder.type));
                    }
                }.execute(context, owner, args, "nav:folder");
            }
        }
    }

    AdapterNavUnified(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);

        this.dp6 = Helper.dp2pixels(context, 6);
        this.dp12 = Helper.dp2pixels(context, 12);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.nav_count = prefs.getBoolean("nav_count", false);
        this.nav_count_pinned = prefs.getBoolean("nav_count_pinned", false);
        this.nav_unseen_drafts = prefs.getBoolean("nav_unseen_drafts", false);
        this.nav_categories = prefs.getBoolean("nav_categories", false);
        boolean highlight_unread = prefs.getBoolean("highlight_unread", true);
        int colorHighlight = prefs.getInt("highlight_color", Helper.resolveColor(context, R.attr.colorUnreadHighlight));
        this.colorUnread = (highlight_unread ? colorHighlight : Helper.resolveColor(context, R.attr.colorUnread));
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
        this.show_unexposed = prefs.getBoolean("show_unexposed", false);
    }

    public void set(@NonNull List<TupleFolderUnified> folders, boolean expanded) {
        Log.i("Set nav unified=" + folders.size());

        boolean show = false;
        Map<String, TupleFolderUnified> map = new HashMap<>();
        TupleFolderUnified unified = new TupleFolderUnified();
        for (TupleFolderUnified type : new ArrayList<>(folders)) {
            if (!nav_categories || !EntityFolder.INBOX.equals(type.type))
                type.category = null;
            for (int i = 0; i < 1 + (type.category == null ? 0 : 1); i++) {
                if (i > 0) {
                    type = new TupleFolderUnified(type);
                    type.category = null;
                }

                String key = (type.category + "/" + type.type);
                TupleFolderUnified f = map.get(key);
                if (!EntityFolder.SYSTEM.equals(type.type) &&
                        !EntityFolder.USER.equals(type.type))
                    if (f == null)
                        map.put(key, type);
                    else {
                        f.folders += type.folders;
                        f.messages += type.messages;
                        f.unseen += type.unseen;
                        f.unexposed += type.unexposed;

                        if (Objects.equals(f.color, type.color) ||
                                (f.color == null && f.folders == type.folders)) {
                            f.color = type.color;
                            f.colorCount += type.colorCount;
                        } else
                            f.colorCount++;
                    }

                if (type.unified && i == 0) {
                    unified.folders += type.folders;
                    unified.messages += type.messages;
                    unified.unseen += type.unseen;
                    unified.unexposed = type.unexposed;

                    if (Objects.equals(unified.color, type.color) ||
                            (unified.color == null && unified.folders == type.folders)) {
                        unified.color = type.color;
                        unified.colorCount += type.colorCount;
                    } else
                        unified.colorCount++;
                }

                if ((EntityFolder.INBOX.equals(type.type) && !type.unified) ||
                        (!EntityFolder.INBOX.equals(type.type) && type.unified))
                    show = true;
            }
        }

        TupleFolderUnified inbox = map.get(EntityFolder.INBOX);
        if (inbox != null)
            inbox.unified = show;

        List<TupleFolderUnified> types = new ArrayList<>();
        for (String type : map.keySet())
            types.add(map.get(type));
        if (unified.folders > 0 && show)
            types.add(unified);

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(types, new Comparator<TupleFolderUnified>() {
            @Override
            public int compare(TupleFolderUnified f1, TupleFolderUnified f2) {
                int i1 = EntityFolder.FOLDER_SORT_ORDER.indexOf(f1.type);
                int i2 = EntityFolder.FOLDER_SORT_ORDER.indexOf(f2.type);
                int f = Integer.compare(i1, i2);
                if (f == 0)
                    return collator.compare(
                            f1.category == null ? "" : f1.category,
                            f2.category == null ? "" : f2.category);
                else
                    return f;
            }
        });

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, types), false);

        this.expanded = expanded;
        this.items = types;

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

    private static class DiffCallback extends DiffUtil.Callback {
        private List<TupleFolderUnified> prev = new ArrayList<>();
        private List<TupleFolderUnified> next = new ArrayList<>();

        DiffCallback(List<TupleFolderUnified> prev, List<TupleFolderUnified> next) {
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
            TupleFolderUnified f1 = prev.get(oldItemPosition);
            TupleFolderUnified f2 = next.get(newItemPosition);
            return (Objects.equals(f1.type, f2.type) && f1.unified == f2.unified);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleFolderUnified f1 = prev.get(oldItemPosition);
            TupleFolderUnified f2 = next.get(newItemPosition);
            return f1.equals(f2);
        }
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
        TupleFolderUnified folder = items.get(position);
        holder.bindTo(folder);
        holder.wire();
    }
}
