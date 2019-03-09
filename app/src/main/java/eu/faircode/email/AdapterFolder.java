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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterFolder extends RecyclerView.Adapter<AdapterFolder.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private LifecycleOwner owner;

    private long account;
    private boolean debug;
    private int dp12;
    private float textSize;
    private int colorUnread;
    private int textColorSecondary;

    private List<TupleFolderEx> all = new ArrayList<>();
    private List<TupleFolderEx> filtered = new ArrayList<>();

    private NumberFormat nf = NumberFormat.getInstance();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View itemView;
        private View vwColor;
        private View vwLevel;
        private ImageView ivState;
        private ImageView ivNotify;
        private TextView tvName;
        private TextView tvMessages;
        private ImageView ivMessages;
        private ImageView ivUnified;
        private TextView tvType;
        private TextView tvAfter;
        private ImageView ivSync;
        private TextView tvKeywords;
        private TextView tvError;

        private final static int action_synchronize_now = 1;
        private final static int action_delete_local = 2;
        private final static int action_delete_browsed = 3;
        private final static int action_empty_trash = 4;
        private final static int action_edit_properties = 5;
        private final static int action_edit_rules = 6;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            vwLevel = itemView.findViewById(R.id.vwLevel);
            ivState = itemView.findViewById(R.id.ivState);
            ivNotify = itemView.findViewById(R.id.ivNotify);
            tvName = itemView.findViewById(R.id.tvName);
            tvMessages = itemView.findViewById(R.id.tvMessages);
            ivMessages = itemView.findViewById(R.id.ivMessages);
            ivUnified = itemView.findViewById(R.id.ivUnified);
            tvType = itemView.findViewById(R.id.tvType);
            tvAfter = itemView.findViewById(R.id.tvAfter);
            ivSync = itemView.findViewById(R.id.ivSync);
            tvKeywords = itemView.findViewById(R.id.tvKeywords);
            tvError = itemView.findViewById(R.id.tvError);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);
        }

        private void bindTo(TupleFolderEx folder) {
            itemView.setActivated(folder.tbc != null || folder.tbd != null);
            itemView.setAlpha(folder.hide ? 0.5f : 1.0f);

            if (textSize != 0)
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            vwColor.setBackgroundColor(folder.accountColor == null ? Color.TRANSPARENT : folder.accountColor);
            vwColor.setVisibility(account < 0 ? View.VISIBLE : View.GONE);

            if (account > 0) {
                ViewGroup.LayoutParams lp = vwLevel.getLayoutParams();
                lp.width = (EntityFolder.USER.equals(folder.type) ? folder.level : 0) * dp12;
                vwLevel.setLayoutParams(lp);
            }

            if (folder.sync_state == null || "requested".equals(folder.sync_state)) {
                if ("waiting".equals(folder.state))
                    ivState.setImageResource(R.drawable.baseline_hourglass_empty_24);
                else if ("connected".equals(folder.state))
                    ivState.setImageResource(R.drawable.baseline_cloud_24);
                else if ("connecting".equals(folder.state))
                    ivState.setImageResource(R.drawable.baseline_cloud_queue_24);
                else if ("closing".equals(folder.state))
                    ivState.setImageResource(R.drawable.baseline_close_24);
                else if (folder.state == null)
                    ivState.setImageResource(R.drawable.baseline_cloud_off_24);
                else
                    ivState.setImageResource(android.R.drawable.stat_sys_warning);
            } else {
                if ("syncing".equals(folder.sync_state))
                    ivState.setImageResource(R.drawable.baseline_compare_arrows_24);
                else if ("downloading".equals(folder.sync_state))
                    ivState.setImageResource(R.drawable.baseline_cloud_download_24);
                else
                    ivState.setImageResource(android.R.drawable.stat_sys_warning);
            }
            ivState.setVisibility(
                    folder.synchronize || folder.state != null || folder.sync_state != null
                            ? View.VISIBLE : View.INVISIBLE);

            ivNotify.setVisibility(folder.notify ? View.VISIBLE : View.GONE);

            String name = folder.getDisplayName(context);
            if (folder.unseen > 0)
                tvName.setText(context.getString(R.string.title_unseen_count, name, folder.unseen));
            else
                tvName.setText(name);
            tvName.setTypeface(null, folder.unseen > 0 ? Typeface.BOLD : Typeface.NORMAL);
            tvName.setTextColor(folder.unseen > 0 ? colorUnread : textColorSecondary);

            StringBuilder sb = new StringBuilder();
            sb.append(nf.format(folder.content));
            sb.append('/');
            sb.append(nf.format(folder.messages));
            sb.append('/');
            if (folder.total == null)
                sb.append('?');
            else
                sb.append(nf.format(folder.total));
            tvMessages.setText(sb.toString());

            ivMessages.setImageResource(folder.download || EntityFolder.OUTBOX.equals(folder.type)
                    ? R.drawable.baseline_mail_24 : R.drawable.baseline_mail_outline_24);

            ivUnified.setVisibility(account > 0 && folder.unified ? View.VISIBLE : View.INVISIBLE);

            if (account < 0)
                tvType.setText(folder.accountName);
            else {
                int resid = context.getResources().getIdentifier(
                        "title_folder_" + folder.type.toLowerCase(),
                        "string",
                        context.getPackageName());
                tvType.setText(resid > 0 ? context.getString(resid) : folder.type);
            }

            if (folder.account == null) {
                tvAfter.setText(null);
                ivSync.setImageResource(R.drawable.baseline_sync_24);
            } else {
                if (folder.keep_days == Integer.MAX_VALUE)
                    tvAfter.setText(String.format("%d/∞", folder.sync_days));
                else
                    tvAfter.setText(String.format("%d/%d", folder.sync_days, folder.keep_days));
                ivSync.setImageResource(folder.synchronize ? R.drawable.baseline_sync_24 : R.drawable.baseline_sync_disabled_24);
            }
            ivSync.setImageTintList(ColorStateList.valueOf(
                    folder.synchronize && folder.initialize && !EntityFolder.OUTBOX.equals(folder.type)
                            ? colorUnread : textColorSecondary));

            tvKeywords.setText(TextUtils.join(" ", folder.keywords));
            tvKeywords.setVisibility(debug && folder.keywords.length > 0 ? View.VISIBLE : View.GONE);

            tvError.setText(folder.error);
            tvError.setVisibility(folder.error != null ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleFolderEx folder = filtered.get(pos);
            if (folder.tbd != null)
                return;

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                            .putExtra("account", folder.account)
                            .putExtra("folder", folder.id));
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleFolderEx folder = filtered.get(pos);
            if (folder.tbd != null)
                return false;

            PopupMenu popupMenu = new PopupMenu(context, itemView);

            popupMenu.getMenu().add(Menu.NONE, action_synchronize_now, 1, R.string.title_synchronize_now);

            if (folder.account != null) { // outbox
                popupMenu.getMenu().add(Menu.NONE, action_delete_local, 2, R.string.title_delete_local);
                popupMenu.getMenu().add(Menu.NONE, action_delete_browsed, 3, R.string.title_delete_browsed);
            }

            if (EntityFolder.TRASH.equals(folder.type))
                popupMenu.getMenu().add(Menu.NONE, action_empty_trash, 4, R.string.title_empty_trash);

            if (folder.account != null) {
                popupMenu.getMenu().add(Menu.NONE, action_edit_rules, 5, R.string.title_edit_rules);
                popupMenu.getMenu().add(Menu.NONE, action_edit_properties, 6, R.string.title_edit_properties);
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem target) {
                    switch (target.getItemId()) {
                        case action_synchronize_now:
                            onActionSynchronizeNow();
                            return true;

                        case action_delete_local:
                            OnActionDeleteLocal(false);
                            return true;

                        case action_delete_browsed:
                            OnActionDeleteLocal(true);
                            return true;

                        case action_empty_trash:
                            onActionEmptyTrash();
                            return true;

                        case action_edit_rules:
                            onActionEditRules();
                            return true;

                        case action_edit_properties:
                            onActionEditProperties();
                            return true;

                        default:
                            return false;
                    }
                }

                private void onActionSynchronizeNow() {
                    Bundle args = new Bundle();
                    args.putLong("folder", folder.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long fid = args.getLong("folder");

                            if (!Helper.suitableNetwork(context, false))
                                throw new IllegalArgumentException(context.getString(R.string.title_no_internet));

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                EntityOperation.sync(context, fid, true);

                                db.setTransactionSuccessful();

                            } finally {
                                db.endTransaction();
                            }

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            if (ex instanceof IllegalArgumentException)
                                Snackbar.make(itemView, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                            else
                                Helper.unexpectedError(context, owner, ex);
                        }
                    }.execute(context, owner, args, "folder:sync");
                }

                private void OnActionDeleteLocal(boolean browsed) {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putBoolean("browsed", browsed);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean browsed = args.getBoolean("browsed");
                            Log.i("Delete local messages browsed=" + browsed);
                            if (browsed)
                                DB.getInstance(context).message().deleteBrowsedMessages(id);
                            else
                                DB.getInstance(context).message().deleteLocalMessages(id);
                            return null;
                        }

                        @Override
                        public void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(context, owner, ex);
                        }
                    }.execute(context, owner, args, "folder:delete:local");
                }

                private void onActionEmptyTrash() {
                    new DialogBuilderLifecycle(context, owner)
                            .setMessage(R.string.title_empty_trash_ask)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Bundle args = new Bundle();
                                    args.putLong("id", folder.id);

                                    new SimpleTask<Void>() {
                                        @Override
                                        protected Void onExecute(Context context, Bundle args) {
                                            long id = args.getLong("id");

                                            DB db = DB.getInstance(context);
                                            try {
                                                db.beginTransaction();

                                                for (Long mid : db.message().getMessageByFolder(id)) {
                                                    EntityMessage message = db.message().getMessage(mid);
                                                    EntityOperation.queue(context, db, message, EntityOperation.DELETE);
                                                }

                                                db.setTransactionSuccessful();
                                            } finally {
                                                db.endTransaction();
                                            }

                                            return null;
                                        }

                                        @Override
                                        protected void onException(Bundle args, Throwable ex) {
                                            Helper.unexpectedError(context, owner, ex);
                                        }
                                    }.execute(context, owner, args, "folder:delete");
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }

                private void onActionEditRules() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_EDIT_RULES)
                                    .putExtra("account", folder.account)
                                    .putExtra("folder", folder.id));
                }

                private void onActionEditProperties() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_EDIT_FOLDER)
                                    .putExtra("id", folder.id));
                }
            });

            popupMenu.show();

            return true;
        }
    }

    AdapterFolder(Context context, LifecycleOwner owner) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.owner = owner;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        if (zoom == 0)
            zoom = 1;

        this.debug = prefs.getBoolean("debug", false);

        this.dp12 = Helper.dp2pixels(context, 12);
        this.textSize = Helper.getTextSize(context, zoom);
        this.colorUnread = Helper.resolveColor(context, R.attr.colorUnread);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        setHasStableIds(true);
    }

    void showHidden(boolean show) {
        set(account, show, all);
    }

    public void set(long account, boolean showAll, @NonNull List<TupleFolderEx> _folders) {
        Log.i("Set account=" + account + " folders=" + _folders.size());

        this.account = account;

        List<EntityFolder> folders = new ArrayList<>();
        folders.addAll(_folders);
        EntityFolder.sort(context, folders, false);

        all.clear();
        for (EntityFolder folder : folders)
            all.add((TupleFolderEx) folder);

        List<TupleFolderEx> shown = new ArrayList<>();
        for (EntityFolder folder : folders)
            if (!folder.hide || showAll)
                shown.add((TupleFolderEx) folder);

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(filtered, shown));

        filtered.clear();
        filtered.addAll(shown);

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
        private List<TupleFolderEx> prev;
        private List<TupleFolderEx> next;

        DiffCallback(List<TupleFolderEx> prev, List<TupleFolderEx> next) {
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
        return new ViewHolder(inflater.inflate(R.layout.item_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleFolderEx folder = filtered.get(position);
        holder.bindTo(folder);

        holder.wire();
    }
}
