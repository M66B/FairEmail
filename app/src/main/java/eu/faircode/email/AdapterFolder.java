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

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class AdapterFolder extends RecyclerView.Adapter<AdapterFolder.ViewHolder> {
    private Fragment parentFragment;
    private long account;
    private boolean primary;
    private boolean show_compact;
    private boolean show_hidden;
    private boolean show_flagged;
    private boolean subscribed_only;
    private IFolderSelectedListener listener;

    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private boolean subscriptions;

    private int dp12;
    private float textSize;
    private int textColorPrimary;
    private int textColorSecondary;
    private int colorUnread;

    private List<Long> disabledIds = new ArrayList<>();
    private List<TupleFolderEx> all = new ArrayList<>();
    private List<TupleFolderEx> items = new ArrayList<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;

        private View vwColor;
        private ImageView ivState;
        private ImageView ivReadOnly;

        private View vwLevel;
        private ImageButton ibExpander;

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
        private TextView tvFlagged;
        private ImageView ibFlagged;

        private TextView tvError;
        private Button btnHelp;

        private Group grpFlagged;
        private Group grpExtended;

        private TwoStateOwner powner = new TwoStateOwner(owner, "FolderPopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);

            vwColor = itemView.findViewById(R.id.vwColor);
            ivState = itemView.findViewById(R.id.ivState);
            ivReadOnly = itemView.findViewById(R.id.ivReadOnly);

            vwLevel = itemView.findViewById(R.id.vwLevel);
            ibExpander = itemView.findViewById(R.id.ibExpander);

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
            tvFlagged = itemView.findViewById(R.id.tvFlagged);
            ibFlagged = itemView.findViewById(R.id.ibFlagged);

            tvError = itemView.findViewById(R.id.tvError);
            btnHelp = itemView.findViewById(R.id.btnHelp);

            grpFlagged = itemView.findViewById(R.id.grpFlagged);
            grpExtended = itemView.findViewById(R.id.grpExtended);
        }

        private void wire() {
            view.setOnClickListener(this);
            ibExpander.setOnClickListener(this);
            if (tvFlagged != null)
                tvFlagged.setOnClickListener(this);
            if (ibFlagged != null)
                ibFlagged.setOnClickListener(this);
            if (listener == null)
                view.setOnLongClickListener(this);
            if (btnHelp != null)
                btnHelp.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            ibExpander.setOnClickListener(null);
            if (tvFlagged != null)
                tvFlagged.setOnClickListener(null);
            if (ibFlagged != null)
                ibFlagged.setOnClickListener(null);
            if (listener == null)
                view.setOnLongClickListener(null);
            if (btnHelp != null)
                btnHelp.setOnClickListener(null);
        }

        private void bindTo(final TupleFolderEx folder) {
            view.setActivated(folder.tbc != null || folder.rename != null || folder.tbd != null);
            view.setAlpha(
                    folder.hide ||
                            !folder.selectable ||
                            (folder.read_only && listener != null) ||
                            disabledIds.contains(folder.id)
                            ? Helper.LOW_LIGHT : 1.0f);

            if (textSize != 0)
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            if (listener == null) {
                vwColor.setBackgroundColor(folder.color == null ? Color.TRANSPARENT : folder.color);
                vwColor.setVisibility(ActivityBilling.isPro(context) ? View.VISIBLE : View.GONE);

                if (folder.sync_state == null || "requested".equals(folder.sync_state)) {
                    if (folder.executing > 0) {
                        ivState.setImageResource(R.drawable.baseline_dns_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_executing));
                    } else if ("waiting".equals(folder.state)) {
                        ivState.setImageResource(R.drawable.baseline_hourglass_empty_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_waiting));
                    } else if ("connected".equals(folder.state)) {
                        ivState.setImageResource(R.drawable.baseline_cloud_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_connected));
                    } else if ("connecting".equals(folder.state)) {
                        ivState.setImageResource(R.drawable.baseline_cloud_queue_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_connecting));
                    } else if ("closing".equals(folder.state)) {
                        ivState.setImageResource(R.drawable.baseline_close_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_closing));
                    } else if (folder.state == null) {
                        ivState.setImageResource(R.drawable.baseline_cloud_off_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_disconnected));
                    } else
                        ivState.setImageResource(R.drawable.baseline_warning_24);
                } else {
                    if ("syncing".equals(folder.sync_state)) {
                        ivState.setImageResource(R.drawable.baseline_compare_arrows_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_synchronizing));
                    } else if ("downloading".equals(folder.sync_state)) {
                        ivState.setImageResource(R.drawable.baseline_cloud_download_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_downloading));
                    } else
                        ivState.setImageResource(R.drawable.baseline_warning_24);
                }
                ivState.setVisibility(
                        folder.synchronize || folder.state != null || folder.sync_state != null
                                ? View.VISIBLE : View.INVISIBLE);

                if (folder.selectable)
                    ivReadOnly.setVisibility(!show_compact && folder.read_only ? View.VISIBLE : View.GONE);
            }

            ViewGroup.LayoutParams lp = vwLevel.getLayoutParams();
            lp.width = (account < 0 && !primary ? 1 : folder.indentation) * dp12;
            vwLevel.setLayoutParams(lp);

            ibExpander.setImageLevel(folder.collapsed ? 1 /* more */ : 0 /* less */);
            ibExpander.setContentDescription(context.getString(folder.collapsed ? R.string.title_accessibility_expand : R.string.title_accessibility_collapse));
            ibExpander.setVisibility((account < 0 && !primary) || !folder.expander
                    ? View.GONE
                    : folder.child_refs != null && folder.child_refs.size() > 0
                    ? View.VISIBLE : View.INVISIBLE);

            if (listener == null && folder.selectable) {
                ivUnified.setVisibility((account > 0 || primary) && folder.unified ? View.VISIBLE : View.GONE);
                ivSubscribed.setVisibility(subscriptions && folder.subscribed != null && folder.subscribed ? View.VISIBLE : View.GONE);
                ivRule.setVisibility(folder.rules > 0 ? View.VISIBLE : View.GONE);
                ivNotify.setVisibility(folder.notify ? View.VISIBLE : View.GONE);
            }

            if (folder.unseen > 0)
                tvName.setText(context.getString(R.string.title_name_count,
                        folder.getDisplayName(context, folder.parent_ref == null ? null : folder.parent_ref),
                        NF.format(folder.unseen)));
            else
                tvName.setText(folder.getDisplayName(context, folder.parent_ref));

            tvName.setTypeface(folder.unseen > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            tvName.setTextColor(folder.unseen > 0 ? colorUnread : textColorSecondary);

            if (listener == null && folder.selectable) {
                StringBuilder sb = new StringBuilder();
                if (folder.account == null)
                    sb.append(NF.format(folder.messages));
                else {
                    if (!show_compact) {
                        sb.append(NF.format(folder.content));
                        sb.append('/');
                    }
                    sb.append(NF.format(folder.messages));
                }
                tvMessages.setText(sb.toString());

                ivMessages.setImageResource(folder.download || EntityFolder.OUTBOX.equals(folder.type)
                        ? R.drawable.baseline_mail_24 : R.drawable.baseline_mail_outline_24);
            }

            if (folder.selectable)
                ivType.setImageResource(EntityFolder.getIcon(folder.type));

            if (listener != null)
                ivType.setVisibility(folder.selectable ? View.VISIBLE : View.GONE);

            if (listener == null && folder.selectable) {
                if (account < 0 && !primary)
                    tvType.setText(folder.accountName);
                else
                    tvType.setText(EntityFolder.localizeType(context, folder.type));

                tvTotal.setText(folder.total == null ? "" : NF.format(folder.total));

                if (folder.account == null) {
                    tvAfter.setText(null);
                    ivSync.setImageResource(R.drawable.baseline_sync_24);
                    ivSync.setContentDescription(context.getString(R.string.title_legend_synchronize_on));
                } else {
                    StringBuilder a = new StringBuilder();

                    if (folder.sync_days == Integer.MAX_VALUE)
                        a.append('∞');
                    else
                        a.append(NF.format(folder.sync_days));

                    a.append('/');

                    if (folder.keep_days == Integer.MAX_VALUE)
                        a.append('∞');
                    else
                        a.append(NF.format(folder.keep_days));

                    tvAfter.setText(a.toString());
                    ivSync.setImageResource(folder.synchronize ? R.drawable.baseline_sync_24 : R.drawable.baseline_sync_disabled_24);
                    ivSync.setContentDescription(context.getString(folder.synchronize ? R.string.title_legend_synchronize_on : R.string.title_legend_synchronize_off));
                }
                ivSync.setImageTintList(ColorStateList.valueOf(
                        folder.synchronize && folder.initialize != 0 && !EntityFolder.OUTBOX.equals(folder.type)
                                ? textColorPrimary : textColorSecondary));

                tvKeywords.setText(BuildConfig.DEBUG ? TextUtils.join(" ", folder.keywords) : null);
                tvKeywords.setVisibility(show_flagged ? View.VISIBLE : View.GONE);

                tvFlagged.setText(NF.format(folder.flagged));
                ibFlagged.setImageResource(folder.flagged == 0
                        ? R.drawable.baseline_star_border_24 : R.drawable.baseline_star_24);
                tvFlagged.setEnabled(folder.flagged > 0);
                ibFlagged.setEnabled(folder.flagged > 0);

                tvError.setText(folder.error);
                tvError.setVisibility(folder.error != null ? View.VISIBLE : View.GONE);
                if (btnHelp != null)
                    btnHelp.setVisibility(folder.error == null ? View.GONE : View.VISIBLE);

                grpFlagged.setVisibility(show_flagged ? View.VISIBLE : View.GONE);
                grpExtended.setVisibility(show_compact ? View.GONE : View.VISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btnHelp)
                Helper.viewFAQ(context, 22);
            else {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return;

                TupleFolderEx folder = items.get(pos);
                if (folder.tbd != null || !folder.selectable)
                    return;

                switch (view.getId()) {
                    case R.id.ibExpander:
                        onCollapse(folder);
                        break;
                    case R.id.tvFlagged:
                    case R.id.ibFlagged:
                        onFlagged(folder);
                        break;
                    default:
                        if (listener == null) {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                            .putExtra("account", folder.account)
                                            .putExtra("folder", folder.id)
                                            .putExtra("type", folder.type));
                        } else {
                            if (folder.read_only)
                                return;
                            if (disabledIds.contains(folder.id))
                                return;
                            listener.onFolderSelected(folder);
                        }
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
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "folder:collapse");
        }

        private void onFlagged(TupleFolderEx folder) {
            BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();
            criteria.in_senders = false;
            criteria.in_recipients = false;
            criteria.in_subject = false;
            criteria.in_keywords = false;
            criteria.in_message = false;
            criteria.with_flagged = true;
            FragmentMessages.search(
                    context, owner, parentFragment.getParentFragmentManager(),
                    folder.account, folder.id, false, criteria);
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleFolderEx folder = items.get(pos);
            if (folder.tbd != null)
                return false;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean debug = prefs.getBoolean("debug", false);

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            if (folder.selectable) {
                SpannableString ss = new SpannableString(folder.getDisplayName(context));
                ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
                ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, ss).setEnabled(false);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_synchronize_now, 1, R.string.title_synchronize_now);

                if (folder.account != null && folder.accountProtocol == EntityAccount.TYPE_IMAP) {
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_synchronize_more, 2, R.string.title_synchronize_more);

                    popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_local, 3, R.string.title_delete_local);
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_browsed, 4, R.string.title_delete_browsed);
                }

                if (EntityFolder.TRASH.equals(folder.type))
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_empty_trash, 5, R.string.title_empty_trash);
                else if (EntityFolder.JUNK.equals(folder.type))
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_empty_spam, 5, R.string.title_empty_spam);

                if (folder.account != null) {
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unified_folder, 6, R.string.title_unified_folder)
                            .setCheckable(true).setChecked(folder.unified);

                    popupMenu.getMenu().add(Menu.NONE, R.string.title_navigation_folder, 7, R.string.title_navigation_folder)
                            .setCheckable(true).setChecked(folder.navigation);

                    popupMenu.getMenu().add(Menu.NONE, R.string.title_notify_folder, 8, R.string.title_notify_folder)
                            .setCheckable(true).setChecked(folder.notify);
                }

                if (folder.account != null && folder.accountProtocol == EntityAccount.TYPE_IMAP) {
                    boolean subscriptions = prefs.getBoolean("subscriptions", false);
                    if (subscriptions && !folder.read_only)
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_subscribe, 9, R.string.title_subscribe)
                                .setCheckable(true).setChecked(folder.subscribed != null && folder.subscribed);

                    popupMenu.getMenu().add(Menu.NONE, R.string.title_synchronize_enabled, 10, R.string.title_synchronize_enabled)
                            .setCheckable(true).setChecked(folder.synchronize);

                    if (!folder.read_only)
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_rules, 11, R.string.title_edit_rules);
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_properties, 12, R.string.title_edit_properties);

                    if (folder.notify && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String channelId = EntityFolder.getNotificationChannelId(folder.id);
                        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationChannel channel = nm.getNotificationChannel(channelId);
                        if (channel == null)
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_create_channel, 13, R.string.title_create_channel);
                        else {
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_channel, 14, R.string.title_edit_channel);
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_channel, 15, R.string.title_delete_channel);
                        }
                    }
                }
            }

            if (EntityFolder.INBOX.equals(folder.type) && folder.accountProtocol == EntityAccount.TYPE_POP)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_rules, 11, R.string.title_edit_rules);

            if (folder.account != null && folder.accountProtocol == EntityAccount.TYPE_IMAP)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_create_sub_folder, 16, R.string.title_create_sub_folder)
                        .setEnabled(folder.inferiors);

            if (!folder.selectable && debug)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, 17, R.string.title_delete)
                        .setEnabled(folder.inferiors);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.string.title_synchronize_now:
                            onActionSync();
                            return true;

                        case R.string.title_synchronize_more:
                            onActionSyncMore();
                            return true;

                        case R.string.title_unified_folder:
                        case R.string.title_navigation_folder:
                        case R.string.title_notify_folder:
                        case R.string.title_synchronize_enabled:
                            onActionProperty(item.getItemId(), !item.isChecked());
                            return true;

                        case R.string.title_subscribe:
                            onActionSubscribe();
                            return true;

                        case R.string.title_delete_local:
                            OnActionDeleteLocal(false);
                            return true;

                        case R.string.title_delete_browsed:
                            OnActionDeleteLocal(true);
                            return true;

                        case R.string.title_empty_trash:
                            onActionEmpty(EntityFolder.TRASH);
                            return true;

                        case R.string.title_empty_spam:
                            onActionEmpty(EntityFolder.JUNK);
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

                        case R.string.title_delete:
                            onActionDeleteFolder();
                            return true;

                        default:
                            return false;
                    }
                }

                private void onActionSync() {
                    Bundle args = new Bundle();
                    args.putLong("folder", folder.id);
                    args.putInt("months", -1);
                    Intent data = new Intent();
                    data.putExtra("args", args);
                    parentFragment.onActivityResult(FragmentFolders.REQUEST_SYNC, RESULT_OK, data);
                }

                private void onActionSyncMore() {
                    Bundle args = new Bundle();
                    args.putLong("folder", folder.id);
                    args.putString("name", folder.getDisplayName(context));

                    FragmentDialogSync sync = new FragmentDialogSync();
                    sync.setArguments(args);
                    sync.setTargetFragment(parentFragment, FragmentFolders.REQUEST_SYNC);
                    sync.show(parentFragment.getParentFragmentManager(), "folder:sync");
                }

                private void onActionProperty(int property, boolean enabled) {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putLong("account", folder.account);
                    args.putInt("property", property);
                    args.putBoolean("enabled", enabled);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            long aid = args.getLong("account");
                            int property = args.getInt("property");
                            boolean enabled = args.getBoolean("enabled");

                            DB db = DB.getInstance(context);
                            switch (property) {
                                case R.string.title_unified_folder:
                                    db.folder().setFolderUnified(id, enabled);
                                    break;
                                case R.string.title_navigation_folder:
                                    db.folder().setFolderNavigation(id, enabled);
                                    break;
                                case R.string.title_notify_folder:
                                    db.folder().setFolderNotify(id, enabled);
                                    break;
                                case R.string.title_synchronize_enabled:
                                    db.folder().setFolderSynchronize(id, enabled);
                                    ServiceSynchronize.reload(context, aid, false, "folder sync=" + enabled);
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unknown folder property=" + property);
                            }

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "folder:enable");
                }

                private void onActionSubscribe() {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putBoolean("subscribed", !(folder.subscribed != null && folder.subscribed));

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean subscribed = args.getBoolean("subscribed");

                            EntityOperation.subscribe(context, id, subscribed);
                            ServiceSynchronize.eval(context, "subscribed=" + subscribed);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "folder:subscribe");
                }

                private void OnActionDeleteLocal(final boolean browsed) {
                    Bundle aargs = new Bundle();
                    aargs.putString("question", context.getString(R.string.title_ask_delete_local));
                    aargs.putLong("folder", folder.id);
                    aargs.putBoolean("browsed", browsed);

                    FragmentDialogAsk ask = new FragmentDialogAsk();
                    ask.setArguments(aargs);
                    ask.setTargetFragment(parentFragment, FragmentFolders.REQUEST_DELETE_LOCAL);
                    ask.show(parentFragment.getParentFragmentManager(), "folder:delete_local");
                }

                private void onActionEmpty(String type) {
                    Bundle aargs = new Bundle();
                    if (EntityFolder.TRASH.equals(type))
                        aargs.putString("question", context.getString(R.string.title_empty_trash_ask));
                    else if (EntityFolder.JUNK.equals(type))
                        aargs.putString("question", context.getString(R.string.title_empty_spam_ask));
                    else
                        throw new IllegalArgumentException("Invalid folder type=" + type);
                    aargs.putString("remark", context.getString(R.string.title_empty_all));
                    aargs.putLong("folder", folder.id);
                    aargs.putString("type", type);

                    FragmentDialogAsk ask = new FragmentDialogAsk();
                    ask.setArguments(aargs);
                    ask.setTargetFragment(parentFragment, FragmentFolders.REQUEST_EMPTY_FOLDER);
                    ask.show(parentFragment.getParentFragmentManager(), "folder:empty");
                }

                private void onActionEditRules() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_EDIT_RULES)
                                    .putExtra("account", folder.account)
                                    .putExtra("protocol", folder.accountProtocol)
                                    .putExtra("folder", folder.id)
                                    .putExtra("type", folder.type));
                }

                private void onActionEditProperties() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_EDIT_FOLDER)
                                    .putExtra("id", folder.id));
                }

                @RequiresApi(api = Build.VERSION_CODES.O)
                private void onActionCreateChannel() {
                    if (!ActivityBilling.isPro(context)) {
                        context.startActivity(new Intent(context, ActivityBilling.class));
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
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Log.w(ex);
                        ToastEx.makeText(context, context.getString(R.string.title_no_viewer, intent), Toast.LENGTH_LONG).show();
                    }
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

                private void onActionDeleteFolder() {
                    Bundle aargs = new Bundle();
                    aargs.putLong("id", folder.id);
                    aargs.putString("question", context.getString(R.string.title_folder_delete));

                    FragmentDialogAsk ask = new FragmentDialogAsk();
                    ask.setArguments(aargs);
                    ask.setTargetFragment(parentFragment, FragmentFolders.REQUEST_DELETE_FOLDER);
                    ask.show(parentFragment.getParentFragmentManager(), "folder:delete");
                }
            });

            popupMenu.show();

            return true;
        }
    }

    AdapterFolder(Fragment parentFragment, long account, boolean primary, boolean show_compact, boolean show_hidden, boolean show_flagged, IFolderSelectedListener listener) {
        this(parentFragment.getContext(), parentFragment.getViewLifecycleOwner(), account, primary, show_compact, show_hidden, show_flagged, listener);
        this.parentFragment = parentFragment;
    }

    AdapterFolder(Context context, LifecycleOwner owner, long account, boolean primary, boolean show_compact, boolean show_hidden, boolean show_flagged, IFolderSelectedListener listener) {
        this.account = account;
        this.primary = primary;
        this.show_compact = show_compact;
        this.show_hidden = show_hidden;
        this.show_flagged = show_flagged;
        this.listener = listener;

        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        if (zoom == 0)
            zoom = 1;

        this.subscriptions = prefs.getBoolean("subscriptions", false);
        this.subscribed_only = prefs.getBoolean("subscribed_only", false) && subscriptions;

        this.dp12 = Helper.dp2pixels(context, 12);
        this.textSize = Helper.getTextSize(context, zoom);
        this.textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        boolean highlight_unread = prefs.getBoolean("highlight_unread", true);
        this.colorUnread = Helper.resolveColor(context, highlight_unread ? R.attr.colorUnreadHighlight : android.R.attr.textColorPrimary);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterFolder.this + " parent destroyed");
                AdapterFolder.this.parentFragment = null;
            }
        });
    }

    void setCompact(boolean compact) {
        if (this.show_compact != compact) {
            this.show_compact = compact;
            notifyDataSetChanged();
        }
    }

    void setShowHidden(boolean show_hidden) {
        if (this.show_hidden != show_hidden) {
            this.show_hidden = show_hidden;
            set(all);
        }
    }

    void setShowFlagged(boolean show_flagged) {
        if (this.show_flagged != show_flagged) {
            this.show_flagged = show_flagged;
            notifyDataSetChanged();
        }
    }

    void setSubscribedOnly(boolean subscribed_only) {
        if (this.subscribed_only != subscribed_only) {
            this.subscribed_only = subscribed_only;
            set(all);
        }
    }

    void setDisabled(List<Long> ids) {
        disabledIds = ids;
    }

    public void set(@NonNull List<TupleFolderEx> folders) {
        Log.i("Set folders=" + folders.size());
        all = folders;

        List<TupleFolderEx> hierarchical;
        if (account < 0 && !primary) {
            if (folders.size() > 0)
                Collections.sort(folders, folders.get(0).getComparator(context));
            hierarchical = folders;
        } else {
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
            for (TupleFolderEx parent : parents) {
                parent.expander = anyChild;

                if (!parent.selectable && parent.child_refs != null && EntityFolder.USER.equals(parent.type))
                    for (TupleFolderEx child : parent.child_refs)
                        if (!EntityFolder.USER.equals(child.type)) {
                            parent.type = EntityFolder.SYSTEM;
                            break;
                        }
            }

            hierarchical = getHierarchical(parents, anyChild ? 0 : 1);
        }

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, hierarchical), false);

        items = hierarchical;

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

    public void search(String query, final int result, final ISearchResult intf) {
        if (TextUtils.isEmpty(query)) {
            intf.onNotFound();
            return;
        }

        // Expand all
        for (TupleFolderEx folder : all)
            folder.collapsed = false;
        set(all);

        // Delay search until after expanding
        ApplicationEx.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                int pos = -1;
                int next = -1;
                int count = result + 1;
                for (int i = 0; i < items.size(); i++)
                    if (items.get(i).getDisplayName(context).toLowerCase().contains(query.toLowerCase())) {
                        count--;
                        if (count == 0)
                            pos = i;
                        else if (count < 0) {
                            next = i;
                            break;
                        }
                    }
                if (pos < 0)
                    intf.onNotFound();
                else
                    intf.onFound(pos, next >= 0);
            }
        });
    }

    interface ISearchResult {
        void onFound(int pos, boolean hasNext);

        void onNotFound();
    }

    private List<TupleFolderEx> getHierarchical(List<TupleFolderEx> parents, int indentation) {
        List<TupleFolderEx> result = new ArrayList<>();

        if (parents.size() > 0)
            Collections.sort(parents, parents.get(0).getComparator(context));

        for (TupleFolderEx parent : parents) {
            if (parent.hide && !show_hidden)
                continue;

            List<TupleFolderEx> childs = null;
            if (parent.child_refs != null)
                childs = getHierarchical(parent.child_refs, indentation + 1);

            if (!subscribed_only ||
                    parent.accountProtocol != EntityAccount.TYPE_IMAP ||
                    (parent.subscribed != null && parent.subscribed) ||
                    (childs != null && childs.size() > 0)) {
                parent.indentation = indentation;
                result.add(parent);
                if (!parent.collapsed && childs != null)
                    result.addAll(childs);
            }
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

            if (f1.indentation != f2.indentation ||
                    f1.expander != f2.expander)
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
    public int getItemViewType(int position) {
        if (listener == null)
            return (items.get(position).selectable ? R.layout.item_folder : R.layout.item_folder_unselectable);
        else
            return R.layout.item_folder_select;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleFolderEx folder = items.get(position);
        holder.bindTo(folder);

        holder.wire();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.powner.recreate();
    }

    interface IFolderSelectedListener {
        void onFolderSelected(TupleFolderEx folder);
    }

    public static class FragmentDialogSync extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            String name = getArguments().getString("name");

            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sync, null);
            final TextView tvFolder = view.findViewById(R.id.tvFolder);
            final EditText etMonths = view.findViewById(R.id.etMonths);

            tvFolder.setText(name);
            etMonths.setText(null);

            return new AlertDialog.Builder(getContext())
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String months = etMonths.getText().toString();
                            if (TextUtils.isEmpty(months))
                                getArguments().putInt("months", 0);
                            else
                                try {
                                    getArguments().putInt("months", Integer.parseInt(months));
                                } catch (NumberFormatException ex) {
                                    Log.e(ex);
                                }
                            sendResult(RESULT_OK);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
