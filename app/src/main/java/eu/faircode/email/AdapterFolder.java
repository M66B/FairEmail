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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterFolder extends RecyclerView.Adapter<AdapterFolder.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private long account;
    private boolean debug;
    private int dp12;

    private List<TupleFolderEx> all = new ArrayList<>();
    private List<TupleFolderEx> filtered = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View itemView;
        private View vwColor;
        private View vwLevel;
        private ImageView ivState;
        private TextView tvName;
        private TextView tvMessages;
        private ImageView ivUnified;
        private TextView tvType;
        private TextView tvAfter;
        private ImageView ivSync;
        private TextView tvKeywords;
        private TextView tvError;

        private final static int action_synchronize_now = 1;
        private final static int action_delete_local = 2;
        private final static int action_empty_trash = 3;
        private final static int action_edit_properties = 4;
        private final static int action_legend = 5;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            vwLevel = itemView.findViewById(R.id.vwLevel);
            ivState = itemView.findViewById(R.id.ivState);
            tvName = itemView.findViewById(R.id.tvName);
            tvMessages = itemView.findViewById(R.id.tvMessages);
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
            itemView.setAlpha(folder.hide ? 0.5f : 1.0f);

            vwColor.setBackgroundColor(folder.accountColor == null ? Color.TRANSPARENT : folder.accountColor);
            vwColor.setVisibility(account < 0 ? View.VISIBLE : View.GONE);

            if (account > 0) {
                ViewGroup.LayoutParams lp = vwLevel.getLayoutParams();
                lp.width = folder.level * dp12;
                vwLevel.setLayoutParams(lp);
            }

            if (folder.sync_state == null || "requested".equals(folder.sync_state)) {
                if ("connected".equals(folder.state))
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
                if ("requested".equals(folder.sync_state))
                    ivState.setImageResource(R.drawable.baseline_hourglass_empty_24);
                else if ("syncing".equals(folder.sync_state))
                    ivState.setImageResource(R.drawable.baseline_compare_arrows_24);
                else if ("downloading".equals(folder.sync_state))
                    ivState.setImageResource(R.drawable.baseline_cloud_download_24);
                else
                    ivState.setImageResource(android.R.drawable.stat_sys_warning);
            }
            ivState.setVisibility(
                    folder.synchronize || folder.state != null || folder.sync_state != null
                            ? View.VISIBLE : View.INVISIBLE);

            String name = folder.getDisplayName(context);
            if (folder.unseen > 0)
                tvName.setText(context.getString(R.string.title_folder_unseen, name, folder.unseen));
            else
                tvName.setText(name);
            tvName.setTypeface(null, folder.unseen > 0 ? Typeface.BOLD : Typeface.NORMAL);
            tvName.setTextColor(Helper.resolveColor(context, folder.unseen > 0 ? R.attr.colorUnread : android.R.attr.textColorSecondary));

            tvMessages.setText(String.format("%d/%d", folder.content, folder.messages));

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
                tvAfter.setText(String.format("%d/%d", folder.sync_days, folder.keep_days));
                ivSync.setImageResource(folder.synchronize ? R.drawable.baseline_sync_24 : R.drawable.baseline_sync_disabled_24);
            }

            tvKeywords.setText(TextUtils.join(" ", folder.keywords));
            tvKeywords.setVisibility(debug && folder.keywords.length > 0 ? View.VISIBLE : View.GONE);

            tvError.setText(folder.error);
            tvError.setVisibility(folder.error != null && BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
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
                            .putExtra("account", folder.account)
                            .putExtra("folder", folder.id)
                            .putExtra("outgoing", folder.isOutgoing()));
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleFolderEx folder = filtered.get(pos);

            PopupMenu popupMenu = new PopupMenu(context, itemView);

            popupMenu.getMenu().add(Menu.NONE, action_synchronize_now, 1, R.string.title_synchronize_now);

            if (!EntityFolder.DRAFTS.equals(folder.type))
                popupMenu.getMenu().add(Menu.NONE, action_delete_local, 2, R.string.title_delete_local);
            if (EntityFolder.TRASH.equals(folder.type))
                popupMenu.getMenu().add(Menu.NONE, action_empty_trash, 3, R.string.title_empty_trash);

            if (folder.account != null)
                popupMenu.getMenu().add(Menu.NONE, action_edit_properties, 4, R.string.title_edit_properties);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem target) {
                    switch (target.getItemId()) {
                        case action_synchronize_now:
                            onActionSynchronizeNow();
                            return true;

                        case action_delete_local:
                            OnActionDeleteLocal();
                            return true;

                        case action_empty_trash:
                            onActionEmptyTrash();
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
                    args.putLong("account", folder.account == null ? -1 : folder.account);
                    args.putLong("folder", folder.id);

                    new SimpleTask<Boolean>() {
                        @Override
                        protected Boolean onLoad(Context context, Bundle args) {
                            long aid = args.getLong("account");
                            long fid = args.getLong("folder");

                            DB db = DB.getInstance(context);
                            EntityOperation.sync(db, fid);

                            if (aid < 0) // outbox
                                return "connected".equals(db.folder().getFolder(fid).state);
                            else
                                return "connected".equals(db.account().getAccount(aid).state);
                        }

                        @Override
                        protected void onLoaded(Bundle args, Boolean connected) {
                            if (!connected)
                                Snackbar.make(itemView, R.string.title_sync_queued, Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(context, owner, ex);
                        }
                    }.load(context, owner, args);
                }

                private void OnActionDeleteLocal() {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putBoolean("outbox", folder.account == null);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onLoad(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean outbox = args.getBoolean("outbox");
                            Log.i(Helper.TAG, "Delete local messages outbox=" + outbox);
                            if (outbox)
                                DB.getInstance(context).message().deleteSeenMessages(id);
                            else
                                DB.getInstance(context).message().deleteLocalMessages(id);
                            return null;
                        }

                        @Override
                        public void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(context, owner, ex);
                        }
                    }.load(context, owner, args);
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
                                        protected Void onLoad(Context context, Bundle args) {
                                            long id = args.getLong("id");

                                            DB db = DB.getInstance(context);
                                            try {
                                                db.beginTransaction();

                                                for (Long mid : db.message().getMessageByFolder(id)) {
                                                    EntityMessage message = db.message().getMessage(mid);
                                                    EntityOperation.queue(db, message, EntityOperation.DELETE);
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
                                    }.load(context, owner, args);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
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
        this.owner = owner;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.debug = prefs.getBoolean("debug", false);

        this.dp12 = Math.round(12 * context.getResources().getDisplayMetrics().density);

        setHasStableIds(true);
    }

    private boolean showAll = false;

    void showHidden(boolean show) {
        showAll = show;
        set(account, all);
    }

    public void set(long account, @NonNull List<TupleFolderEx> folders) {
        Log.i(Helper.TAG, "Set account=" + account + " folders=" + folders.size());

        this.account = account;

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(folders, new Comparator<TupleFolderEx>() {
            @Override
            public int compare(TupleFolderEx f1, TupleFolderEx f2) {
                int s = Integer.compare(
                        EntityFolder.FOLDER_SORT_ORDER.indexOf(f1.type),
                        EntityFolder.FOLDER_SORT_ORDER.indexOf(f2.type));
                if (s != 0)
                    return s;
                int c = -f1.synchronize.compareTo(f2.synchronize);
                if (c != 0)
                    return c;
                return collator.compare(
                        f1.name == null ? "" : f1.name,
                        f2.name == null ? "" : f2.name);
            }
        });

        all = folders;

        List<TupleFolderEx> shown = new ArrayList<>();
        for (TupleFolderEx folder : folders)
            if (!folder.hide || showAll)
                shown.add(folder);

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new MessageDiffCallback(filtered, shown));

        filtered.clear();
        filtered.addAll(shown);

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
        diff.dispatchUpdatesTo(this);
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
        holder.bindTo(folder);

        holder.wire();
    }
}
