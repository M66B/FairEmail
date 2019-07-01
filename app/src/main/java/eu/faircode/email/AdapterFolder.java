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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Collator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class AdapterFolder extends RecyclerView.Adapter<AdapterFolder.ViewHolder> {
    private Fragment parentFragment;
    private long account;
    private boolean show_hidden;
    private IFolderSelectedListener listener;

    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private boolean subscriptions;
    private boolean debug;
    private int dp12;
    private float textSize;
    private int colorUnread;
    private int textColorSecondary;

    private List<Long> disabledIds = new ArrayList<>();
    private List<TupleFolderEx> all = new ArrayList<>();
    private List<TupleFolderEx> items = new ArrayList<>();

    private NumberFormat nf = NumberFormat.getNumberInstance();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private View vwColor;
        private ImageView ivState;
        private ImageView ivReadOnly;

        private View vwLevel;
        private ImageView ivExpander;

        private ImageView ivUnified;
        private ImageView ivSubscribed;
        private ImageView ivRule;
        private ImageView ivNotify;
        private TextView tvName;
        private TextView tvMessages;
        private ImageView ivMessages;

        private ImageView ivType;
        private TextView tvType;
        private TextView tvTotal;
        private TextView tvAfter;
        private ImageView ivSync;

        private TextView tvKeywords;
        private TextView tvError;

        private TwoStateOwner powner = new TwoStateOwner(owner, "FolderPopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            ivState = itemView.findViewById(R.id.ivState);
            ivReadOnly = itemView.findViewById(R.id.ivReadOnly);

            vwLevel = itemView.findViewById(R.id.vwLevel);
            ivExpander = itemView.findViewById(R.id.ivExpander);

            ivUnified = itemView.findViewById(R.id.ivUnified);
            ivSubscribed = itemView.findViewById(R.id.ivSubscribed);
            ivRule = itemView.findViewById(R.id.ivRule);
            ivNotify = itemView.findViewById(R.id.ivNotify);
            tvName = itemView.findViewById(R.id.tvName);
            tvMessages = itemView.findViewById(R.id.tvMessages);
            ivMessages = itemView.findViewById(R.id.ivMessages);

            ivType = itemView.findViewById(R.id.ivType);
            tvType = itemView.findViewById(R.id.tvType);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvAfter = itemView.findViewById(R.id.tvAfter);
            ivSync = itemView.findViewById(R.id.ivSync);

            tvKeywords = itemView.findViewById(R.id.tvKeywords);
            tvError = itemView.findViewById(R.id.tvError);
        }

        private void wire() {
            view.setOnClickListener(this);
            ivExpander.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            ivExpander.setOnClickListener(null);
            view.setOnLongClickListener(null);
        }

        private void bindTo(final TupleFolderEx folder) {
            view.setActivated(folder.tbc != null || folder.tbd != null);
            view.setAlpha(folder.hide || disabledIds.contains(folder.id) ? Helper.LOW_LIGHT : 1.0f);

            if (textSize != 0)
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            if (listener == null) {
                vwColor.setBackgroundColor(folder.accountColor == null ? Color.TRANSPARENT : folder.accountColor);
                vwColor.setVisibility(account < 0 && Helper.isPro(context) ? View.VISIBLE : View.GONE);

                if (folder.sync_state == null || "requested".equals(folder.sync_state)) {
                    if (folder.executing > 0)
                        ivState.setImageResource(R.drawable.baseline_dns_24);
                    else if ("waiting".equals(folder.state))
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
                        ivState.setImageResource(R.drawable.baseline_warning_24);
                } else {
                    if ("syncing".equals(folder.sync_state))
                        ivState.setImageResource(R.drawable.baseline_compare_arrows_24);
                    else if ("downloading".equals(folder.sync_state))
                        ivState.setImageResource(R.drawable.baseline_cloud_download_24);
                    else
                        ivState.setImageResource(R.drawable.baseline_warning_24);
                }
                ivState.setVisibility(
                        folder.synchronize || folder.state != null || folder.sync_state != null
                                ? View.VISIBLE : View.INVISIBLE);

                ivReadOnly.setVisibility(folder.read_only ? View.VISIBLE : View.GONE);
            }

            ViewGroup.LayoutParams lp = vwLevel.getLayoutParams();
            lp.width = (account < 0 ? 1 : folder.indentation) * dp12;
            vwLevel.setLayoutParams(lp);

            ivExpander.setImageLevel(folder.collapsed ? 1 /* more */ : 0 /* less */);
            ivExpander.setVisibility(account < 0 || !folder.expander
                    ? View.GONE
                    : folder.child_refs != null && folder.child_refs.size() > 0
                    ? View.VISIBLE : View.INVISIBLE);

            if (listener == null) {
                ivUnified.setVisibility(account > 0 && folder.unified ? View.VISIBLE : View.GONE);
                ivSubscribed.setVisibility(subscriptions && folder.subscribed != null && folder.subscribed ? View.VISIBLE : View.GONE);
                ivRule.setVisibility(folder.rules > 0 ? View.VISIBLE : View.GONE);
                ivNotify.setVisibility(folder.notify ? View.VISIBLE : View.GONE);
            }

            if (folder.unseen > 0)
                tvName.setText(context.getString(R.string.title_name_count,
                        folder.getDisplayName(context, folder.parent_ref == null ? null : folder.parent_ref),
                        nf.format(folder.unseen)));
            else
                tvName.setText(folder.getDisplayName(context, folder.parent_ref == null ? null : folder.parent_ref));

            tvName.setTypeface(folder.unseen > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            tvName.setTextColor(folder.unseen > 0 ? colorUnread : textColorSecondary);

            if (listener == null) {
                StringBuilder sb = new StringBuilder();
                if (folder.account == null)
                    sb.append(nf.format(folder.messages));
                else {
                    sb.append(nf.format(folder.content));
                    sb.append('/');
                    sb.append(nf.format(folder.messages));
                }
                tvMessages.setText(sb.toString());

                ivMessages.setImageResource(folder.download || EntityFolder.OUTBOX.equals(folder.type)
                        ? R.drawable.baseline_mail_24 : R.drawable.baseline_mail_outline_24);
            }

            ivType.setImageResource(EntityFolder.getIcon(folder.type));

            if (listener == null) {
                if (account < 0)
                    tvType.setText(folder.accountName);
                else {
                    int resid = context.getResources().getIdentifier(
                            "title_folder_" + folder.type.toLowerCase(),
                            "string",
                            context.getPackageName());
                    tvType.setText(resid > 0 ? context.getString(resid) : folder.type);
                }

                tvTotal.setText(folder.total == null ? "" : nf.format(folder.total));

                if (folder.account == null) {
                    tvAfter.setText(null);
                    ivSync.setImageResource(R.drawable.baseline_sync_24);
                } else {
                    StringBuilder a = new StringBuilder();
                    a.append(nf.format(folder.sync_days));
                    a.append('/');
                    if (folder.keep_days == Integer.MAX_VALUE)
                        a.append('∞');
                    else
                        a.append(nf.format(folder.keep_days));
                    tvAfter.setText(a.toString());
                    ivSync.setImageResource(folder.synchronize ? R.drawable.baseline_sync_24 : R.drawable.baseline_sync_disabled_24);
                }
                ivSync.setImageTintList(ColorStateList.valueOf(
                        folder.synchronize && folder.initialize != 0 && !EntityFolder.OUTBOX.equals(folder.type)
                                ? colorUnread : textColorSecondary));

                tvKeywords.setText(TextUtils.join(" ", folder.keywords));
                tvKeywords.setVisibility(debug && folder.keywords.length > 0 ? View.VISIBLE : View.GONE);

                tvError.setText(folder.error);
                tvError.setVisibility(folder.error != null ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleFolderEx folder = items.get(pos);
            if (folder.tbd != null)
                return;

            if (view.getId() == R.id.ivExpander)
                onCollapse(folder);
            else {
                if (listener == null) {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                    .putExtra("account", folder.account)
                                    .putExtra("folder", folder.id));
                } else {
                    if (disabledIds.contains(folder.id))
                        return;
                    listener.onFolderSelected(folder);
                }
            }
        }

        private void onCollapse(TupleFolderEx folder) {
            if (listener != null) {
                folder.collapsed = !folder.collapsed;
                set(all);
                return;
            }

            Bundle args = new Bundle();
            args.putLong("id", folder.id);
            args.putBoolean("collapsed", !folder.collapsed);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean collapsed = args.getBoolean("collapsed");

                    DB db = DB.getInstance(context);
                    db.folder().setFolderCollapsed(id, collapsed);

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(parentFragment.getFragmentManager(), ex);
                }
            }.execute(context, owner, args, "folder:collapse");
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleFolderEx folder = items.get(pos);
            if (folder.tbd != null)
                return false;

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            popupMenu.getMenu().add(Menu.NONE, 0, 0, folder.getDisplayName(context)).setEnabled(false);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_synchronize_now, 1, R.string.title_synchronize_now);

            if (folder.account != null) {
                popupMenu.getMenu().add(Menu.NONE, R.string.title_synchronize_all, 2, R.string.title_synchronize_all);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_local, 3, R.string.title_delete_local);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_browsed, 4, R.string.title_delete_browsed);

                if (EntityFolder.TRASH.equals(folder.type))
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_empty_trash, 5, R.string.title_empty_trash);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String startup = prefs.getString("startup", "unified");
                if (!"accounts".equals(startup))
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unified_folder, 6, R.string.title_unified_folder)
                            .setCheckable(true).setChecked(folder.unified);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_navigation_folder, 7, R.string.title_navigation_folder)
                        .setCheckable(true).setChecked(folder.navigation);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_notify_folder, 8, R.string.title_notify_folder)
                        .setCheckable(true).setChecked(folder.notify);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_synchronize_enabled, 9, R.string.title_synchronize_enabled)
                        .setCheckable(true).setChecked(folder.synchronize);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_rules, 10, R.string.title_edit_rules);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_properties, 11, R.string.title_edit_properties);

                if (folder.notify && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String channelId = EntityFolder.getNotificationChannelId(folder.id);
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel channel = nm.getNotificationChannel(channelId);
                    if (channel == null)
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_create_channel, 12, R.string.title_create_channel);
                    else {
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_channel, 13, R.string.title_edit_channel);
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_channel, 14, R.string.title_delete_channel);
                    }
                }

                popupMenu.getMenu().add(Menu.NONE, R.string.title_create_sub_folder, 15, R.string.title_create_sub_folder);
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.string.title_synchronize_now:
                            Bundle args = new Bundle();
                            args.putLong("folder", folder.id);
                            args.putBoolean("all", false);
                            Intent data = new Intent();
                            data.putExtra("args", args);
                            parentFragment.onActivityResult(FragmentFolders.REQUEST_SYNC, RESULT_OK, data);
                            return true;

                        case R.string.title_synchronize_all:
                            onActionSynchronizeAll();
                            return true;

                        case R.string.title_unified_folder:
                        case R.string.title_navigation_folder:
                        case R.string.title_notify_folder:
                        case R.string.title_synchronize_enabled:
                            onActionProperty(item.getItemId(), !item.isChecked());
                            return true;

                        case R.string.title_delete_local:
                            OnActionDeleteLocal(false);
                            return true;

                        case R.string.title_delete_browsed:
                            OnActionDeleteLocal(true);
                            return true;

                        case R.string.title_empty_trash:
                            onActionEmptyTrash();
                            return true;

                        case R.string.title_edit_rules:
                            onActionEditRules();
                            return true;

                        case R.string.title_edit_properties:
                            onActionEditProperties();
                            return true;

                        case R.string.title_create_channel:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                onActionCreateChannel();
                            return true;

                        case R.string.title_edit_channel:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                onActionEditChannel();
                            return true;

                        case R.string.title_delete_channel:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                onActionDeleteChannel();
                            return true;

                        case R.string.title_create_sub_folder:
                            onActionCreateFolder();
                            return true;

                        default:
                            return false;
                    }
                }

                private void onActionSynchronizeAll() {
                    Bundle aargs = new Bundle();
                    aargs.putString("question",
                            context.getString(R.string.title_ask_sync_all, folder.getDisplayName(context)));
                    aargs.putLong("folder", folder.id);
                    aargs.putBoolean("all", true);

                    FragmentDialogAsk ask = new FragmentDialogAsk();
                    ask.setArguments(aargs);
                    ask.setTargetFragment(parentFragment, FragmentFolders.REQUEST_SYNC);
                    ask.show(parentFragment.getFragmentManager(), "folder:sync");
                }

                private void onActionProperty(int property, boolean enabled) {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putInt("property", property);
                    args.putBoolean("enabled", enabled);

                    new SimpleTask<Boolean>() {
                        @Override
                        protected Boolean onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            int property = args.getInt("property");
                            boolean enabled = args.getBoolean("enabled");

                            DB db = DB.getInstance(context);
                            switch (property) {
                                case R.string.title_unified_folder:
                                    db.folder().setFolderUnified(id, enabled);
                                    return false;
                                case R.string.title_navigation_folder:
                                    db.folder().setFolderNavigation(id, enabled);
                                    return false;
                                case R.string.title_notify_folder:
                                    db.folder().setFolderNotify(id, enabled);
                                    return false;
                                case R.string.title_synchronize_enabled:
                                    db.folder().setFolderSynchronize(id, enabled);
                                    return true;
                                default:
                                    return false;
                            }
                        }

                        @Override
                        protected void onExecuted(Bundle args, Boolean reload) {
                            if (reload)
                                ServiceSynchronize.reload(context, "folder property changed");
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(parentFragment.getFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "folder:enable");
                }

                private void OnActionDeleteLocal(final boolean browsed) {
                    Bundle aargs = new Bundle();
                    aargs.putString("question", context.getString(R.string.title_ask_delete_local));
                    aargs.putLong("folder", folder.id);
                    aargs.putBoolean("browsed", browsed);

                    FragmentDialogAsk ask = new FragmentDialogAsk();
                    ask.setArguments(aargs);
                    ask.setTargetFragment(parentFragment, FragmentFolders.REQUEST_DELETE_LOCAL);
                    ask.show(parentFragment.getFragmentManager(), "folder:delete_local");
                }

                private void onActionEmptyTrash() {
                    Bundle aargs = new Bundle();
                    aargs.putString("question", context.getString(R.string.title_empty_trash_ask));
                    aargs.putLong("folder", folder.id);

                    FragmentDialogAsk ask = new FragmentDialogAsk();
                    ask.setArguments(aargs);
                    ask.setTargetFragment(parentFragment, FragmentFolders.REQUEST_EMPTY_TRASH);
                    ask.show(parentFragment.getFragmentManager(), "folder:empty_trash");
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

                @RequiresApi(api = Build.VERSION_CODES.O)
                private void onActionCreateChannel() {
                    if (!Helper.isPro(context)) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                        return;
                    }

                    folder.createNotificationChannel(context);
                    onActionEditChannel();
                }

                @RequiresApi(api = Build.VERSION_CODES.O)
                private void onActionEditChannel() {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
                            .putExtra(Settings.EXTRA_CHANNEL_ID, EntityFolder.getNotificationChannelId(folder.id));
                    context.startActivity(intent);
                }

                @RequiresApi(api = Build.VERSION_CODES.O)
                private void onActionDeleteChannel() {
                    folder.deleteNotificationChannel(context);
                }

                private void onActionCreateFolder() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_EDIT_FOLDER)
                                    .putExtra("account", folder.account)
                                    .putExtra("parent", folder.name));
                }
            });

            popupMenu.show();

            return true;
        }
    }

    AdapterFolder(Fragment parentFragment, long account, boolean show_hidden, IFolderSelectedListener listener) {
        this(parentFragment.getContext(), parentFragment.getViewLifecycleOwner(), account, show_hidden, listener);
        this.parentFragment = parentFragment;
    }

    AdapterFolder(Context context, LifecycleOwner owner, long account, boolean show_hidden, IFolderSelectedListener listener) {
        this.account = account;
        this.show_hidden = show_hidden;
        this.listener = listener;

        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        if (zoom == 0)
            zoom = 1;

        this.subscriptions = prefs.getBoolean("subscriptions", false);
        this.debug = prefs.getBoolean("debug", false);

        this.dp12 = Helper.dp2pixels(context, 12);
        this.textSize = Helper.getTextSize(context, zoom);
        this.colorUnread = Helper.resolveColor(context, R.attr.colorUnread);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.i(AdapterFolder.this + " parent destroyed");
                AdapterFolder.this.parentFragment = null;
                AdapterFolder.this.context = null;
                AdapterFolder.this.owner = null;
            }
        });
    }

    void setShowHidden(boolean show_hidden) {
        if (this.show_hidden != show_hidden) {
            this.show_hidden = show_hidden;
            set(all);
        }
    }

    void setDisabled(List<Long> ids) {
        disabledIds = ids;
    }

    public void set(@NonNull List<TupleFolderEx> folders) {
        Log.i("Set folders=" + folders.size());
        all = folders;

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        if (folders.size() > 0)
            Collections.sort(folders, folders.get(0).getComparator(context));

        List<TupleFolderEx> hierarchical;
        if (account < 0)
            hierarchical = folders;
        else {
            List<TupleFolderEx> parents = new ArrayList<>();
            Map<Long, TupleFolderEx> idFolder = new HashMap<>();
            Map<Long, List<TupleFolderEx>> parentChilds = new HashMap<>();

            for (TupleFolderEx folder : folders) {
                idFolder.put(folder.id, folder);
                if (folder.parent == null)
                    parents.add(folder);
                else {
                    if (!parentChilds.containsKey(folder.parent))
                        parentChilds.put(folder.parent, new ArrayList<TupleFolderEx>());
                    parentChilds.get(folder.parent).add(folder);
                }
            }

            TupleFolderEx root = new TupleFolderEx();
            root.name = "[root]";
            root.child_refs = parents;
            for (TupleFolderEx parent : parents)
                parent.parent_ref = root;

            for (long pid : parentChilds.keySet()) {
                TupleFolderEx parent = idFolder.get(pid);
                if (parent != null) {
                    parent.child_refs = parentChilds.get(pid);
                    for (TupleFolderEx child : parent.child_refs)
                        child.parent_ref = parent;
                }
            }

            boolean anyChild = false;
            for (TupleFolderEx parent : parents)
                if (parent.child_refs != null && parent.child_refs.size() > 0) {
                    anyChild = true;
                    break;
                }
            for (TupleFolderEx parent : parents)
                parent.expander = anyChild;

            hierarchical = getHierarchical(parents, anyChild ? 0 : 1);
        }

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, hierarchical), false);

        items = hierarchical;

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

    List<TupleFolderEx> getHierarchical(List<TupleFolderEx> parents, int indentation) {
        List<TupleFolderEx> result = new ArrayList<>();

        for (TupleFolderEx parent : parents)
            if (!parent.hide || show_hidden) {
                parent.indentation = indentation;
                result.add(parent);
                if (!parent.collapsed && parent.child_refs != null)
                    result.addAll(getHierarchical(parent.child_refs, indentation + 1));
            }

        return result;
    }

    private class DiffCallback extends DiffUtil.Callback {
        private List<TupleFolderEx> prev = new ArrayList<>();
        private List<TupleFolderEx> next = new ArrayList<>();

        DiffCallback(List<TupleFolderEx> prev, List<TupleFolderEx> next) {
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
            TupleFolderEx f1 = prev.get(oldItemPosition);
            TupleFolderEx f2 = next.get(newItemPosition);
            return f1.id.equals(f2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleFolderEx f1 = prev.get(oldItemPosition);
            TupleFolderEx f2 = next.get(newItemPosition);
            if (!f1.equals(f2))
                return false;

            TupleFolderEx p1 = f1.parent_ref;
            TupleFolderEx p2 = f2.parent_ref;
            while (p1 != null && p2 != null) {
                if (p1.hide != p2.hide)
                    return false;

                if (p1.collapsed != p2.collapsed)
                    return false;

                p1 = p1.parent_ref;
                p2 = p2.parent_ref;
            }

            return true;
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
        return new ViewHolder(inflater.inflate(
                listener == null ? R.layout.item_folder : R.layout.item_folder_select,
                parent, false));
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.powner.recreate();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleFolderEx folder = items.get(position);
        holder.bindTo(folder);

        holder.wire();
    }

    interface IFolderSelectedListener {
        void onFolderSelected(TupleFolderEx folder);
    }
}
