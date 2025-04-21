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

import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterFolder extends RecyclerView.Adapter<AdapterFolder.ViewHolder> {
    private Fragment parentFragment;
    private long account;
    private boolean unified;
    private boolean primary;
    private boolean show_compact;
    private boolean show_hidden;
    private boolean show_flagged;
    private boolean subscribed_only;
    private boolean sort_unread_atop;
    private IFolderSelectedListener listener;

    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;
    private ViewModelSelected selectedModel;

    private boolean subscriptions;

    private int dp3;
    private int dp6;
    private int dp12;
    private float textSize;
    private int colorStripeWidth;
    private int textColorPrimary;
    private int textColorSecondary;
    private int colorUnread;
    private int colorControlNormal;
    private int colorSeparator;
    private boolean show_unexposed;
    private boolean debug;

    private String search = null;
    private List<Long> disabledIds = new ArrayList<>();
    private List<TupleFolderEx> all = new ArrayList<>();
    private List<TupleFolderEx> selected = new ArrayList<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();

    private static final int DENSE_ITEMS_THRESHOLD_FEW = 10;
    private static final int DENSE_ITEMS_THRESHOLD_MANY = 50;

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
        private ImageView ivAutoAdd;
        private TextView tvName;
        private TextView tvMessages;
        private ImageButton ibMessages;
        private ImageButton ibFlaggedEnd;

        private ImageView ivType;
        private TextView tvType;
        private TextView tvTotal;
        private TextView tvAfter;
        private ImageButton ibSync;
        private TextView tvFlaggedEnd;

        private TextView tvKeywords;
        private TextView tvFlagged;
        private ImageButton ibFlagged;

        private TextView tvError;
        private Button btnHelp;

        private Group grpFlagged;
        private Group grpFlaggedEnd;
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
            ivAutoAdd = itemView.findViewById(R.id.ivAutoAdd);
            tvName = itemView.findViewById(R.id.tvName);
            tvMessages = itemView.findViewById(R.id.tvMessages);
            ibMessages = itemView.findViewById(R.id.ibMessages);
            ibFlaggedEnd = itemView.findViewById(R.id.ibFlaggedEnd);

            ivType = itemView.findViewById(R.id.ivType);
            tvType = itemView.findViewById(R.id.tvType);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvAfter = itemView.findViewById(R.id.tvAfter);
            ibSync = itemView.findViewById(R.id.ibSync);
            tvFlaggedEnd = itemView.findViewById(R.id.tvFlaggedEnd);

            tvKeywords = itemView.findViewById(R.id.tvKeywords);
            tvFlagged = itemView.findViewById(R.id.tvFlagged);
            ibFlagged = itemView.findViewById(R.id.ibFlagged);

            tvError = itemView.findViewById(R.id.tvError);
            btnHelp = itemView.findViewById(R.id.btnHelp);

            grpFlagged = itemView.findViewById(R.id.grpFlagged);
            grpFlaggedEnd = itemView.findViewById(R.id.grpFlaggedEnd);
            grpExtended = itemView.findViewById(R.id.grpExtended);

            if (vwColor != null)
                vwColor.getLayoutParams().width = colorStripeWidth;
        }

        private void wire() {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            ibExpander.setOnClickListener(this);
            if (tvMessages != null)
                tvMessages.setOnClickListener(this);
            if (ibMessages != null)
                ibMessages.setOnClickListener(this);
            if (ibFlaggedEnd != null)
                ibFlaggedEnd.setOnClickListener(this);
            if (ibSync != null)
                ibSync.setOnClickListener(this);
            if (tvFlaggedEnd != null)
                tvFlaggedEnd.setOnClickListener(this);
            if (tvFlagged != null)
                tvFlagged.setOnClickListener(this);
            if (ibFlagged != null)
                ibFlagged.setOnClickListener(this);
            if (btnHelp != null)
                btnHelp.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
            ibExpander.setOnClickListener(null);
            if (tvMessages != null)
                tvMessages.setOnClickListener(null);
            if (ibMessages != null)
                ibMessages.setOnClickListener(null);
            if (ibFlaggedEnd != null)
                ibFlaggedEnd.setOnClickListener(null);
            if (ibSync != null)
                ibSync.setOnClickListener(null);
            if (tvFlaggedEnd != null)
                tvFlaggedEnd.setOnClickListener(null);
            if (tvFlagged != null)
                tvFlagged.setOnClickListener(null);
            if (ibFlagged != null)
                ibFlagged.setOnClickListener(null);
            if (btnHelp != null)
                btnHelp.setOnClickListener(null);
        }

        private void bindTo(final TupleFolderEx folder) {
            boolean disabled = isDisabled(folder);

            int p = 0;
            if (show_compact)
                if (all.size() < DENSE_ITEMS_THRESHOLD_FEW)
                    p = dp6;
                else if (all.size() < DENSE_ITEMS_THRESHOLD_MANY)
                    p = dp3;
            view.setPadding(p, p, p, p);
            view.setActivated(folder.tbc != null || folder.rename != null || folder.tbd != null);
            view.setAlpha(folder.hide || folder.isHidden(listener != null) || disabled ? Helper.LOW_LIGHT : 1.0f);

            if (listener == null && selectedModel != null)
                itemView.setBackgroundColor(
                        selectedModel.isSelected(folder.id)
                                ? colorSeparator : Color.TRANSPARENT);

            if (textSize != 0)
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            if (listener == null) {
                Integer color =
                        (folder.color == null && EntityFolder.INBOX.equals(folder.type)
                                ? folder.accountColor : folder.color);
                vwColor.setBackgroundColor(color == null ? Color.TRANSPARENT : color);
                vwColor.setVisibility(ActivityBilling.isPro(context) ? View.VISIBLE : View.GONE);

                if (folder.sync_state == null || "requested".equals(folder.sync_state)) {
                    if (folder.executing > 0) {
                        ivState.setImageResource(R.drawable.twotone_dns_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_executing));
                    } else if ("connected".equals(folder.state)) {
                        ivState.setImageResource(R.drawable.twotone_cloud_done_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_connected));
                    } else if ("connecting".equals(folder.state)) {
                        ivState.setImageResource(R.drawable.twotone_cloud_queue_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_connecting));
                    } else if ("closing".equals(folder.state)) {
                        ivState.setImageResource(R.drawable.twotone_cancel_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_closing));
                    } else if (folder.state == null) {
                        ivState.setImageResource(R.drawable.twotone_cloud_off_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_disconnected));
                    } else
                        ivState.setImageResource(R.drawable.twotone_warning_24);
                } else {
                    if ("syncing".equals(folder.sync_state)) {
                        ivState.setImageResource(R.drawable.twotone_compare_arrows_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_synchronizing));
                    } else if ("downloading".equals(folder.sync_state)) {
                        ivState.setImageResource(R.drawable.twotone_cloud_download_24);
                        ivState.setContentDescription(context.getString(R.string.title_legend_downloading));
                    } else
                        ivState.setImageResource(R.drawable.twotone_warning_24);
                }
                ivState.setVisibility(
                        (folder.selectable && folder.synchronize) || folder.state != null || folder.sync_state != null
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
                ivUnified.setVisibility(
                        (account > 0 || primary) && folder.unified && !show_compact
                                ? View.VISIBLE : View.GONE);
                ivSubscribed.setVisibility(
                        subscriptions && folder.subscribed != null && folder.subscribed && !show_compact
                                ? View.VISIBLE : View.GONE);
                ivRule.setVisibility(
                        folder.rules > 0 && !show_compact
                                ? View.VISIBLE : View.GONE);
                ivNotify.setVisibility(
                        folder.notify && !show_compact
                                ? View.VISIBLE : View.GONE);
                ivAutoAdd.setVisibility(BuildConfig.DEBUG &&
                        EntityFolder.SENT.equals(folder.type) &&
                        (folder.auto_add == null || folder.auto_add)
                        ? View.VISIBLE : View.GONE);
            }

            int cunseen = (folder.collapsed ? folder.childs_unseen : 0);
            int unseen = folder.unseen + cunseen;
            int unexposed = (show_unexposed ? folder.unexposed : 0);

            if (unseen > 0 || unexposed > 0) {
                StringBuilder sb = new StringBuilder();
                if (unseen > 0) {
                    if (cunseen > 0)
                        sb.append('\u25BE');
                    sb.append(NF.format(unseen));
                }
                if (unexposed > 0)
                    sb.append('\u2B51');
                tvName.setText(context.getString(R.string.title_name_count,
                        folder.getDisplayName(context, folder.parent_ref), sb));
            } else
                tvName.setText(folder.getDisplayName(context, folder.parent_ref));

            tvName.setTypeface(unseen > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            tvName.setTextColor(unseen > 0 ? colorUnread : textColorSecondary);

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

                ibMessages.setImageResource(folder.download || EntityFolder.OUTBOX.equals(folder.type)
                        ? R.drawable.twotone_mail_24 : R.drawable.twotone_mail_outline_24);
            }

            if (folder.selectable)
                ivType.setImageResource(EntityFolder.getIcon(folder.type));

            if (listener != null) {
                ivType.setVisibility(folder.selectable ? View.VISIBLE : View.GONE);
                ivType.setImageTintList(ColorStateList.valueOf(folder.color == null ? colorControlNormal : folder.color));
            }

            if (listener == null && folder.selectable) {
                if (account < 0 && !primary)
                    tvType.setText(folder.accountName);
                else
                    tvType.setText(EntityFolder.localizeType(context, folder.type) +
                            (folder.inherited_type == null || !(BuildConfig.DEBUG || EntityFolder.SENT.equals(folder.inherited_type))
                                    ? ""
                                    : "/" + EntityFolder.localizeType(context, folder.inherited_type)) +
                            (EntityFolder.FLAGGED.equals(folder.subtype) ? "*" : ""));

                tvTotal.setText(folder.total == null ? null : NF.format(folder.total));

                if (folder.account == null) {
                    tvAfter.setText(null);
                    ibSync.setImageResource(R.drawable.twotone_sync_24);
                    ibSync.setContentDescription(context.getString(R.string.title_legend_synchronize_on));
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
                    if (folder.synchronize) {
                        ibSync.setImageResource(folder.poll
                                ? R.drawable.twotone_hourglass_top_24
                                : R.drawable.twotone_sync_24);
                        ibSync.setContentDescription(context.getString(folder.poll
                                ? R.string.title_legend_synchronize_poll
                                : R.string.title_legend_synchronize_on));
                    } else {
                        ibSync.setImageResource(R.drawable.twotone_sync_disabled_24);
                        ibSync.setContentDescription(context.getString(R.string.title_legend_synchronize_off));
                    }
                }
                ibSync.setImageTintList(ColorStateList.valueOf(
                        folder.synchronize && folder.initialize != 0 &&
                                !EntityFolder.OUTBOX.equals(folder.type) &&
                                folder.accountProtocol == EntityAccount.TYPE_IMAP
                                ? textColorPrimary : textColorSecondary));
                ibSync.setEnabled(folder.last_sync != null);
                tvFlaggedEnd.setText(NF.format(folder.flagged));
                ibFlaggedEnd.setImageResource(folder.flagged == 0
                        ? R.drawable.twotone_star_border_24 : R.drawable.twotone_star_24);

                tvKeywords.setText(debug ?
                        (folder.separator == null ? "" : folder.separator + " ") +
                                (folder.namespace == null ? "" : folder.namespace + " ") +
                                (folder.flags == null ? null : TextUtils.join(" ", folder.flags) + " ") +
                                TextUtils.join(" ", folder.keywords) : null);

                tvFlagged.setText(NF.format(folder.flagged));
                ibFlagged.setImageResource(folder.flagged == 0
                        ? R.drawable.twotone_star_border_24 : R.drawable.twotone_star_24);

                tvError.setText(folder.error);
                tvError.setVisibility(folder.error != null ? View.VISIBLE : View.GONE);
                if (btnHelp != null)
                    btnHelp.setVisibility(folder.error == null ? View.GONE : View.VISIBLE);

                grpFlagged.setVisibility(show_flagged && show_compact ? View.VISIBLE : View.GONE);
                grpFlaggedEnd.setVisibility(show_flagged && !show_compact ? View.VISIBLE : View.GONE);
                grpExtended.setVisibility(show_compact ? View.GONE : View.VISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btnHelp)
                Helper.viewFAQ(view.getContext(), 22);
            else {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return;

                TupleFolderEx folder = selected.get(pos);
                if (folder.tbd != null)
                    return;

                int id = view.getId();
                if (id == R.id.ibExpander) {
                    onCollapse(folder, pos);
                } else if (show_flagged &&
                        (id == R.id.tvMessages || id == R.id.ibMessages)) {
                    onUnread(folder);
                } else if (id == R.id.tvFlagged || id == R.id.ibFlagged ||
                        id == R.id.ibFlaggedEnd || id == R.id.tvFlaggedEnd) {
                    onFlagged(folder);
                } else if (id == R.id.ibSync) {
                    onLastSync(folder);
                } else {
                    if (isDisabled(folder))
                        return;

                    if (listener == null) {
                        if (selectedModel != null)
                            selectedModel.select(folder.id);

                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                        .putExtra("account", folder.account)
                                        .putExtra("folder", folder.id)
                                        .putExtra("type", folder.type));
                    } else
                        listener.onFolderSelected(folder);
                }
            }
        }

        private boolean isDisabled(EntityFolder folder) {
            return (!folder.selectable ||
                    (folder.read_only && listener != null) ||
                    disabledIds.contains(folder.id));
        }

        private void onCollapse(TupleFolderEx folder, int pos) {
            if (listener != null) {
                folder.collapsed = !folder.collapsed;
                notifyItemChanged(pos); // Update expander
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

        private void onUnread(TupleFolderEx folder) {
            BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();
            criteria.in_senders = false;
            criteria.in_recipients = false;
            criteria.in_subject = false;
            criteria.in_keywords = false;
            criteria.in_message = false;
            criteria.in_notes = false;
            criteria.with_unseen = true;
            FragmentMessages.search(
                    context, owner, parentFragment.getParentFragmentManager(),
                    folder.account, folder.id, false, criteria);
        }

        private void onFlagged(TupleFolderEx folder) {
            BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();
            criteria.in_senders = false;
            criteria.in_recipients = false;
            criteria.in_subject = false;
            criteria.in_keywords = false;
            criteria.in_message = false;
            criteria.in_notes = false;
            criteria.with_flagged = true;
            FragmentMessages.search(
                    context, owner, parentFragment.getParentFragmentManager(),
                    folder.account, folder.id, false, criteria);
        }

        private void onLastSync(TupleFolderEx folder) {
            if (folder.last_sync == null)
                return;
            DateFormat DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.LONG, SimpleDateFormat.LONG);
            ToastEx.makeText(context, DTF.format(folder.last_sync), Toast.LENGTH_LONG).show();
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleFolderEx folder = selected.get(pos);
            if (folder.tbd != null || folder.local)
                return false;

            if (listener != null)
                return listener.onFolderLongPress(folder);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean perform_expunge = prefs.getBoolean("perform_expunge", true);
            boolean debug = prefs.getBoolean("debug", false);

            int order = 1;
            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            String title;
            if (folder.last_sync == null)
                title = folder.getDisplayName(context);
            else
                title = context.getString(R.string.title_name_count,
                        folder.getDisplayName(context),
                        Helper.getRelativeTimeSpanString(context, folder.last_sync));

            SpannableString ss = new SpannableString(title);
            ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
            popupMenu.getMenu().add(Menu.NONE, 0, 0, ss).setEnabled(false);

            if (folder.selectable)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_synchronize_now, order++, R.string.title_synchronize_now);

            if (folder.selectable) {
                if (folder.account != null && folder.accountProtocol == EntityAccount.TYPE_IMAP) {
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_synchronize_more, order++, R.string.title_synchronize_more);

                    popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_local, order++, R.string.title_delete_local);
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_browsed, order++, R.string.title_delete_browsed);
                    if (!perform_expunge || BuildConfig.DEBUG)
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_expunge, order++, R.string.title_expunge);
                }

                popupMenu.getMenu().add(Menu.NONE, R.string.title_mark_all_read, order++, R.string.title_mark_all_read);

                if (EntityFolder.TRASH.equals(folder.type))
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_empty_trash, order++, R.string.title_empty_trash);
                else if (EntityFolder.JUNK.equals(folder.type))
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_empty_spam, order++, R.string.title_empty_spam);

                if (folder.account != null && folder.accountProtocol == EntityAccount.TYPE_IMAP)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_synchronize_enabled, order++, R.string.title_synchronize_enabled)
                            .setCheckable(true).setChecked(folder.synchronize);

                if (folder.account != null) {
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_notify_folder, order++, R.string.title_notify_folder)
                            .setCheckable(true).setChecked(folder.notify);

                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unified_folder, order++, R.string.title_unified_folder)
                            .setCheckable(true).setChecked(folder.unified);

                    popupMenu.getMenu().add(Menu.NONE, R.string.title_navigation_folder, order++, R.string.title_navigation_folder)
                            .setCheckable(true).setChecked(folder.navigation);
                }

                if (folder.account != null && folder.accountProtocol == EntityAccount.TYPE_IMAP) {
                    boolean subscriptions = prefs.getBoolean("subscriptions", false);
                    if (subscriptions && !folder.read_only)
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_subscribe, order++, R.string.title_subscribe)
                                .setCheckable(true).setChecked(folder.subscribed != null && folder.subscribed);

                    if (!folder.read_only) {
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_rules, order++, R.string.title_edit_rules);
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_execute_rules, order++, R.string.title_execute_rules);
                    }
                }

                if (parentFragment instanceof FragmentFolders)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_color, order++, R.string.title_edit_color);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_properties, order++, R.string.title_edit_properties);

                if (folder.account != null && folder.accountProtocol == EntityAccount.TYPE_IMAP) {
                    if (folder.notify && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String channelId = EntityFolder.getNotificationChannelId(folder.id);
                        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                        NotificationChannel channel = nm.getNotificationChannel(channelId);
                        if (channel == null)
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_create_channel, order++, R.string.title_create_channel);
                        else {
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_channel, order++, R.string.title_edit_channel);
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_channel, order++, R.string.title_delete_channel);
                        }
                    }
                }
            }

            if (folder.accountProtocol == EntityAccount.TYPE_POP) {
                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_rules, order++, R.string.title_edit_rules);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_execute_rules, order++, R.string.title_execute_rules);
            }

            if (folder.accountProtocol == EntityAccount.TYPE_POP ||
                    (folder.selectable && (debug || BuildConfig.DEBUG))) {
                popupMenu.getMenu().add(Menu.NONE, R.string.title_export_messages, order++, R.string.title_export_messages);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_import_messages, order++, R.string.title_import_messages);
            }

            if (!folder.selectable)
                popupMenu.getMenu()
                        .add(Menu.NONE, R.string.title_hide_folder, order++, R.string.title_hide_folder)
                        .setCheckable(true)
                        .setChecked(folder.hide);

            int children = 0;
            if (folder.child_refs != null)
                for (TupleFolderEx child : folder.child_refs)
                    if (child.selectable)
                        children++;
            if (children > 0) {
                SubMenu submenu = popupMenu.getMenu()
                        .addSubMenu(Menu.NONE, Menu.NONE, order++, R.string.title_synchronize_subfolders);

                submenu.add(Menu.FIRST, R.string.title_synchronize_now, 1, R.string.title_synchronize_now);
                submenu.add(Menu.FIRST, R.string.title_synchronize_more, 2, R.string.title_synchronize_more);
                submenu.add(Menu.FIRST, R.string.title_synchronize_batch_enable, 3, R.string.title_synchronize_batch_enable);
                submenu.add(Menu.FIRST, R.string.title_synchronize_batch_disable, 4, R.string.title_synchronize_batch_disable);
                submenu.add(Menu.FIRST, R.string.title_notify_batch_enable, 5, R.string.title_notify_batch_enable);
                submenu.add(Menu.FIRST, R.string.title_notify_batch_disable, 6, R.string.title_notify_batch_disable);
                submenu.add(Menu.FIRST, R.string.title_unified_inbox_add, 7, R.string.title_unified_inbox_add);
                submenu.add(Menu.FIRST, R.string.title_unified_inbox_delete, 8, R.string.title_unified_inbox_delete);
                submenu.add(Menu.FIRST, R.string.title_navigation_folder, 9, R.string.title_navigation_folder);
                submenu.add(Menu.FIRST, R.string.title_navigation_folder_hide, 10, R.string.title_navigation_folder_hide);
                submenu.add(Menu.FIRST, R.string.title_download_batch_enable, 11, R.string.title_download_batch_enable);
                submenu.add(Menu.FIRST, R.string.title_download_batch_disable, 12, R.string.title_download_batch_disable);
                if (parentFragment instanceof FragmentFolders)
                    submenu.add(Menu.FIRST, R.string.title_edit_color, 13, R.string.title_edit_color);
            }

            if (folder.account != null && folder.accountProtocol == EntityAccount.TYPE_IMAP)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_create_sub_folder, order++, R.string.title_create_sub_folder)
                        .setEnabled(folder.inferiors);

            if (folder.selectable && Shortcuts.can(context))
                popupMenu.getMenu().add(Menu.NONE, R.string.title_pin, order++, R.string.title_pin);

            if (!folder.read_only && EntityFolder.USER.equals(folder.type) &&
                    (folder.child_refs == null || folder.child_refs.isEmpty()))
                popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, order++, R.string.title_delete);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getGroupId() == Menu.FIRST) {
                        int itemId = item.getItemId();
                        if (itemId == R.string.title_synchronize_now) {
                            onActionSync(true);
                            return true;
                        } else if (itemId == R.string.title_synchronize_more) {
                            onActionSyncMore(true);
                            return true;
                        } else if (itemId == R.string.title_synchronize_batch_enable) {
                            onActionEnableSync(true);
                            return true;
                        } else if (itemId == R.string.title_synchronize_batch_disable) {
                            onActionEnableSync(false);
                            return true;
                        } else if (itemId == R.string.title_notify_batch_enable) {
                            onActionEnableNotify(true);
                            return true;
                        } else if (itemId == R.string.title_notify_batch_disable) {
                            onActionEnableNotify(false);
                            return true;
                        } else if (itemId == R.string.title_unified_inbox_add) {
                            onActionUnifiedInbox(true);
                            return true;
                        } else if (itemId == R.string.title_unified_inbox_delete) {
                            onActionUnifiedInbox(false);
                            return true;
                        } else if (itemId == R.string.title_navigation_folder) {
                            onActionEnableNavigationMenu(true);
                            return true;
                        } else if (itemId == R.string.title_navigation_folder_hide) {
                            onActionEnableNavigationMenu(false);
                            return true;
                        } else if (itemId == R.string.title_download_batch_enable) {
                            onActionEnableDownload(true);
                            return true;
                        } else if (itemId == R.string.title_download_batch_disable) {
                            onActionEnableDownload(false);
                            return true;
                        } else if (itemId == R.string.title_edit_color) {
                            onActionEditColor(true);
                            return true;
                        }
                        return false;
                    }

                    int itemId = item.getItemId();
                    if (itemId == R.string.title_synchronize_now) {
                        onActionSync(false);
                        return true;
                    } else if (itemId == R.string.title_synchronize_more) {
                        onActionSyncMore(false);
                        return true;
                    } else if (itemId == R.string.title_delete_local) {
                        OnActionDeleteLocal(false);
                        return true;
                    } else if (itemId == R.string.title_delete_browsed) {
                        OnActionDeleteLocal(true);
                        return true;
                    } else if (itemId == R.string.title_expunge) {
                        onActionExpunge();
                        return true;
                    } else if (itemId == R.string.title_synchronize_enabled ||
                            itemId == R.string.title_notify_folder ||
                            itemId == R.string.title_unified_folder ||
                            itemId == R.string.title_navigation_folder) {
                        onActionProperty(itemId, !item.isChecked());
                        return true;
                    } else if (itemId == R.string.title_subscribe) {
                        onActionSubscribe();
                        return true;
                    } else if (itemId == R.string.title_mark_all_read) {
                        onActionMarkAllRead();
                        return true;
                    } else if (itemId == R.string.title_empty_trash) {
                        onActionEmpty(EntityFolder.TRASH);
                        return true;
                    } else if (itemId == R.string.title_empty_spam) {
                        onActionEmpty(EntityFolder.JUNK);
                        return true;
                    } else if (itemId == R.string.title_edit_rules) {
                        onActionEditRules();
                        return true;
                    } else if (itemId == R.string.title_execute_rules) {
                        onActionExecuteRules();
                        return true;
                    } else if (itemId == R.string.title_export_messages) {
                        onActionExportMessages();
                        return true;
                    } else if (itemId == R.string.title_import_messages) {
                        onActionImportMessages();
                        return true;
                    } else if (itemId == R.string.title_edit_color) {
                        onActionEditColor(false);
                        return true;
                    } else if (itemId == R.string.title_edit_properties) {
                        onActionEditProperties();
                        return true;
                    } else if (itemId == R.string.title_create_channel) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            onActionCreateChannel();
                        return true;
                    } else if (itemId == R.string.title_edit_channel) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            onActionEditChannel();
                        return true;
                    } else if (itemId == R.string.title_delete_channel) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            onActionDeleteChannel();
                        return true;
                    } else if (itemId == R.string.title_hide_folder) {
                        onActionHide();
                        return true;
                    } else if (itemId == R.string.title_create_sub_folder) {
                        onActionCreateFolder();
                        return true;
                    } else if (itemId == R.string.title_pin) {
                        onActionPinFolder();
                        return true;
                    } else if (itemId == R.string.title_delete) {
                        onActionDeleteFolder();
                        return true;
                    }
                    return false;
                }

                private void onActionSync(boolean children) {
                    Bundle args = new Bundle();
                    args.putLong("folder", folder.id);
                    args.putBoolean("children", children);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long fid = args.getLong("folder");
                            boolean children = args.getBoolean("children");

                            if (!ConnectionHelper.getNetworkState(context).isSuitable())
                                throw new IllegalStateException(context.getString(R.string.title_no_internet));

                            boolean now = true;

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                EntityFolder folder = db.folder().getFolder(fid);
                                if (folder == null)
                                    return null;

                                if (folder.selectable)
                                    EntityOperation.sync(context, folder.id, true, !children);

                                if (children) {
                                    List<EntityFolder> folders = EntityFolder.getChildFolders(context, folder.id);
                                    for (EntityFolder child : folders)
                                        if (child.selectable)
                                            EntityOperation.sync(context, child.id, true);
                                }

                                if (folder.account != null) {
                                    EntityAccount account = db.account().getAccount(folder.account);
                                    if (account != null && !"connected".equals(account.state))
                                        now = false;
                                }

                                db.setTransactionSuccessful();

                            } finally {
                                db.endTransaction();
                            }

                            ServiceSynchronize.eval(context, "refresh/folder");

                            if (!now)
                                throw new IllegalArgumentException(context.getString(R.string.title_no_connection));

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            if (ex instanceof IllegalStateException) {
                                Snackbar snackbar = Helper.setSnackbarOptions(
                                        Snackbar.make(parentFragment.getView(), new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG));
                                snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                .putExtra("tab", "connection"));
                                    }
                                });
                                snackbar.show();
                            } else if (ex instanceof IllegalArgumentException)
                                Helper.setSnackbarOptions(
                                                Snackbar.make(view, new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG))
                                        .show();
                            else
                                Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "folder:sync");
                }

                private void onActionEnableSync(boolean enabled) {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putLong("account", folder.account);
                    args.putBoolean("enabled", enabled);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                            long id = args.getLong("id");
                            long aid = args.getLong("account");
                            boolean enabled = args.getBoolean("enabled");

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                List<EntityFolder> children = EntityFolder.getChildFolders(context, id);
                                for (EntityFolder child : children)
                                    db.folder().setFolderSynchronize(child.id, enabled);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            ServiceSynchronize.reload(context, aid, false, "child sync=" + enabled);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "children:sync");
                }

                private void onActionEnableNotify(boolean enabled) {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putBoolean("enabled", enabled);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                            long id = args.getLong("id");
                            boolean enabled = args.getBoolean("enabled");

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                List<EntityFolder> children = EntityFolder.getChildFolders(context, id);
                                for (EntityFolder child : children)
                                    db.folder().setFolderNotify(child.id, enabled);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "children:notify");
                }

                private void onActionUnifiedInbox(boolean add) {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putBoolean("add", add);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                            long id = args.getLong("id");
                            boolean add = args.getBoolean("add");

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                List<EntityFolder> children = EntityFolder.getChildFolders(context, id);
                                for (EntityFolder child : children)
                                    db.folder().setFolderUnified(child.id, add);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "children:unified");
                }

                private void onActionEnableNavigationMenu(boolean enabled) {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putBoolean("enabled", enabled);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                            long id = args.getLong("id");
                            boolean enabled = args.getBoolean("enabled");

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                List<EntityFolder> children = EntityFolder.getChildFolders(context, id);
                                for (EntityFolder child : children)
                                    db.folder().setFolderNavigation(child.id, enabled);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "children:navigation");
                }

                private void onActionSyncMore(boolean children) {
                    Bundle args = new Bundle();
                    args.putLong("folder", folder.id);
                    args.putString("name", folder.getDisplayName(context));
                    args.putBoolean("children", children);

                    FragmentDialogSync sync = new FragmentDialogSync();
                    sync.setArguments(args);
                    sync.show(parentFragment.getParentFragmentManager(), "folder:months");
                }

                private void onActionEnableDownload(boolean enabled) {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putBoolean("enabled", enabled);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                            long id = args.getLong("id");
                            boolean enabled = args.getBoolean("enabled");

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                List<EntityFolder> children = EntityFolder.getChildFolders(context, id);
                                for (EntityFolder child : children)
                                    db.folder().setFolderDownload(child.id, enabled);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "children:download");
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
                            if (property == R.string.title_unified_folder) {
                                db.folder().setFolderUnified(id, enabled);
                            } else if (property == R.string.title_navigation_folder) {
                                db.folder().setFolderNavigation(id, enabled);
                            } else if (property == R.string.title_notify_folder) {
                                db.folder().setFolderNotify(id, enabled);
                            } else if (property == R.string.title_synchronize_enabled) {
                                db.folder().setFolderSynchronize(id, enabled);
                                ServiceSynchronize.reload(context, aid, false, "folder sync=" + enabled);
                            } else {
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

                private void onActionExpunge() {
                    new AlertDialog.Builder(view.getContext())
                            .setIcon(R.drawable.twotone_warning_24)
                            .setTitle(R.string.title_expunge)
                            .setMessage(R.string.title_expunge_remark)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    expunge();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing
                                }
                            })
                            .show();
                }

                private void expunge() {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected void onPreExecute(Bundle args) {
                            ToastEx.makeText(context, R.string.title_executing, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            EntityFolder folder = db.folder().getFolder(id);
                            if (folder == null)
                                return null;

                            EntityOperation.queue(context, folder, EntityOperation.EXPUNGE);
                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "folder:expunge");
                }

                private void onActionMarkAllRead() {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean all_read_asked = prefs.getBoolean("all_read_asked", false);
                    if (all_read_asked) {
                        FragmentMessages.markAllRead(parentFragment, folder.type, folder.id, AdapterMessage.ViewType.FOLDER);
                        return;
                    }

                    Bundle args = new Bundle();
                    args.putString("type", folder.type);
                    args.putLong("folder", folder.id);

                    args.putString("question", context.getString(R.string.title_mark_all_read));
                    args.putString("notagain", "all_read_asked");

                    FragmentDialogAsk ask = new FragmentDialogAsk();
                    ask.setArguments(args);
                    ask.setTargetFragment(parentFragment, FragmentFolders.REQUEST_ALL_READ);
                    ask.show(parentFragment.getParentFragmentManager(), "folder:allread");
                }

                private void onActionEmpty(String type) {
                    Bundle aargs = new Bundle();
                    if (EntityFolder.TRASH.equals(type))
                        aargs.putString("question", context.getString(R.string.title_empty_trash_ask));
                    else if (EntityFolder.JUNK.equals(type))
                        aargs.putString("question", context.getString(R.string.title_empty_spam_ask));
                    else
                        throw new IllegalArgumentException("Invalid folder type=" + type);
                    aargs.putBoolean("warning", true);
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

                private void onActionExecuteRules() {
                    Bundle args = new Bundle();
                    args.putString("question", context.getString(R.string.title_execute_rules));
                    args.putLong("id", folder.id);

                    FragmentDialogAsk ask = new FragmentDialogAsk();
                    ask.setArguments(args);
                    ask.setTargetFragment(parentFragment, FragmentFolders.REQUEST_EXECUTE_RULES);
                    ask.show(parentFragment.getParentFragmentManager(), "folder:execute");
                }

                private void onActionExportMessages() {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    prefs.edit().putBoolean("debug", false).apply();

                    String filename = Helper.sanitizeFilename(
                            folder.accountName.replace(" ", "_") + "_" +
                                    folder.getDisplayName(context).replace(" ", "_") + "_" +
                                    new SimpleDateFormat("yyyyMMdd").format(new Date().getTime()) + ".mbox");

                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_TITLE, filename);
                    Helper.openAdvanced(context, intent);

                    if (intent.resolveActivity(context.getPackageManager()) == null) { // system/GET_CONTENT whitelisted
                        Log.unexpectedError(parentFragment.getParentFragmentManager(),
                                new IllegalArgumentException(context.getString(R.string.title_no_saf)), 25);
                        return;
                    }

                    parentFragment.getArguments().putLong("selected_folder", folder.id);

                    parentFragment.startActivityForResult(
                            Helper.getChooser(context, intent),
                            FragmentFolders.REQUEST_EXPORT_MESSAGES);
                }

                private void onActionImportMessages() {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    prefs.edit().putBoolean("debug", false).apply();

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setType("*/*");

                    if (intent.resolveActivity(context.getPackageManager()) == null) { // system/GET_CONTENT whitelisted
                        Log.unexpectedError(parentFragment.getParentFragmentManager(),
                                new IllegalArgumentException(context.getString(R.string.title_no_saf)), 25);
                        return;
                    }

                    parentFragment.getArguments().putLong("selected_folder", folder.id);

                    parentFragment.startActivityForResult(
                            Helper.getChooser(context, intent),
                            FragmentFolders.REQUEST_IMPORT_MESSAGES);
                }

                private void onActionEditColor(boolean children) {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putBoolean("children", children);
                    args.putInt("color", folder.color == null ? Color.TRANSPARENT : folder.color);
                    args.putString("title", context.getString(R.string.title_color));
                    args.putBoolean("reset", true);

                    FragmentDialogColor fragment = new FragmentDialogColor();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(parentFragment, FragmentFolders.REQUEST_EDIT_FOLDER_COLOR);
                    fragment.show(parentFragment.getParentFragmentManager(), "edit:color");
                }

                private void onActionEditProperties() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_EDIT_FOLDER)
                                    .putExtra("id", folder.id)
                                    .putExtra("account", folder.account)
                                    .putExtra("imap", folder.accountProtocol == EntityAccount.TYPE_IMAP));
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
                    } catch (Throwable ex) {
                        Helper.reportNoViewer(context, intent, ex);
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.O)
                private void onActionDeleteChannel() {
                    folder.deleteNotificationChannel(context);
                }

                private void onActionHide() {
                    Bundle args = new Bundle();
                    args.putLong("id", folder.id);
                    args.putBoolean("hide", !folder.hide);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                            long id = args.getLong("id");
                            boolean hide = args.getBoolean("hide");

                            DB db = DB.getInstance(context);
                            db.folder().setFolderHide(id, hide);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "folder:hide");
                }

                private void onActionCreateFolder() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_EDIT_FOLDER)
                                    .putExtra("account", folder.account)
                                    .putExtra("parent", folder.name));
                }

                private void onActionPinFolder() {
                    ShortcutInfoCompat.Builder builder = Shortcuts.getShortcut(context, folder);
                    Shortcuts.requestPinShortcut(context, builder.build());
                }

                private void onActionDeleteFolder() {
                    Bundle aargs = new Bundle();
                    aargs.putLong("id", folder.id);
                    aargs.putString("remark", folder.name);
                    aargs.putString("question", context.getString(R.string.title_folder_delete));
                    aargs.putBoolean("warning", true);

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

    AdapterFolder(Fragment parentFragment, long account, boolean unified, boolean primary, boolean show_compact, boolean show_hidden, boolean show_flagged, IFolderSelectedListener listener) {
        this(parentFragment.getContext(), parentFragment.getViewLifecycleOwner(), account, unified, primary, show_compact, show_hidden, show_flagged, listener);
        this.parentFragment = parentFragment;
    }

    AdapterFolder(Context context, LifecycleOwner owner, long account, boolean unified, boolean primary, boolean show_compact, boolean show_hidden, boolean show_flagged, IFolderSelectedListener listener) {
        this.account = account;
        this.unified = unified;
        this.primary = primary;
        this.show_compact = show_compact;
        this.show_hidden = show_hidden;
        this.show_flagged = show_flagged;
        this.listener = listener;

        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        if (context instanceof FragmentActivity && BuildConfig.DEBUG)
            this.selectedModel = new ViewModelProvider((FragmentActivity) context)
                    .get(ViewModelSelected.class);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        if (zoom == 0)
            zoom = 1;

        this.subscriptions = prefs.getBoolean("subscriptions", false);
        this.subscribed_only = prefs.getBoolean("subscribed_only", false) && subscriptions;
        this.sort_unread_atop = prefs.getBoolean("sort_unread_atop", false);

        this.dp3 = Helper.dp2pixels(context, 3);
        this.dp6 = Helper.dp2pixels(context, 6);
        this.dp12 = Helper.dp2pixels(context, 12);
        this.textSize = Helper.getTextSize(context, zoom);
        int account_color_size = prefs.getInt("account_color_size", 6);
        this.colorStripeWidth = Helper.dp2pixels(context, account_color_size);
        this.textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        boolean highlight_unread = prefs.getBoolean("highlight_unread", true);
        int colorHighlight = prefs.getInt("highlight_color", Helper.resolveColor(context, R.attr.colorUnreadHighlight));
        this.colorUnread = (highlight_unread ? colorHighlight : Helper.resolveColor(context, R.attr.colorUnread));
        this.colorControlNormal = Helper.resolveColor(context, androidx.appcompat.R.attr.colorControlNormal);
        this.colorSeparator = Helper.resolveColor(context, R.attr.colorSeparator);
        this.show_unexposed = prefs.getBoolean("show_unexposed", false);
        this.debug = prefs.getBoolean("debug", false);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterFolder.this + " parent destroyed");
                AdapterFolder.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    void setCompact(boolean compact) {
        if (this.show_compact != compact)
            this.show_compact = compact;
    }

    void setShowHidden(boolean show_hidden) {
        if (this.show_hidden != show_hidden) {
            this.show_hidden = show_hidden;
            set(all);
        }
    }

    void setShowFlagged(boolean show_flagged) {
        if (this.show_flagged != show_flagged)
            this.show_flagged = show_flagged;
    }

    void setSubscribedOnly(boolean subscribed_only) {
        if (this.subscribed_only != subscribed_only) {
            this.subscribed_only = subscribed_only;
            set(all);
        }
    }

    void setSortUnreadAtop(boolean sort_unread_atop) {
        if (this.sort_unread_atop != sort_unread_atop) {
            this.sort_unread_atop = sort_unread_atop;
            set(all);
        }
    }

    void setDisabled(List<Long> ids) {
        disabledIds = ids;
    }

    public void set(@NonNull List<TupleFolderEx> folders) {
        Log.i("Set folders=" + folders.size() + " search=" + search);
        all = folders;

        List<TupleFolderEx> hierarchical;
        if (account < 0 && !primary) {
            List<TupleFolderEx> filtered = new ArrayList<>();
            for (TupleFolderEx folder : folders)
                if (show_hidden || !folder.isHidden(listener != null))
                    filtered.add(folder);

            if (filtered.size() > 0)
                Collections.sort(filtered, filtered.get(0).getComparator(context));

            if (sort_unread_atop)
                Collections.sort(filtered, new Comparator<TupleFolderEx>() {
                    @Override
                    public int compare(TupleFolderEx f1, TupleFolderEx f2) {
                        return -Boolean.compare(f1.unseen > 0, f2.unseen > 0);
                    }
                });

            hierarchical = filtered;
        } else {
            List<TupleFolderEx> parents = new ArrayList<>();
            Map<Long, TupleFolderEx> idFolder = new HashMap<>();
            Map<Long, List<TupleFolderEx>> parentChilds = new HashMap<>();

            for (TupleFolderEx folder : folders) {
                folder.indentation = 0;
                folder.expander = true;
                folder.parent_ref = null;
                folder.child_refs = null;
                folder.childs_unseen = 0;

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

            hierarchical = getHierarchical(parents, anyChild ? 0 : 1, sort_unread_atop);
        }

        List<TupleFolderEx> items;
        if (TextUtils.isEmpty(search))
            items = hierarchical;
        else {
            items = new ArrayList<>();
            String query = search.toLowerCase().trim();
            for (TupleFolderEx item : hierarchical)
                if (item.getDisplayName(context).toLowerCase().contains(query))
                    items.add(item);
        }

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(selected, items), false);

        selected = items;

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
            /*
                java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling eu.faircode.email.FixedRecyclerView{bc0fa01 VFED..... ........ 0,0-1080,1984 #7f0a0533 app:id/rvFolder}, adapter:eu.faircode.email.AdapterFolder@b1cf0a6, layout:androidx.recyclerview.widget.LinearLayoutManager@3093ae7, context:eu.faircode.email.ActivityView@832e020
                    at androidx.recyclerview.widget.RecyclerView.assertNotInLayoutOrScroll(SourceFile:3)
                    at androidx.recyclerview.widget.RecyclerView$RecyclerViewDataObserver.onItemRangeChanged(SourceFile:1)
                    at androidx.recyclerview.widget.RecyclerView$AdapterDataObservable.notifyItemRangeChanged(SourceFile:3)
                    at androidx.recyclerview.widget.RecyclerView$Adapter.notifyItemRangeChanged(SourceFile:2)
                    at androidx.recyclerview.widget.AdapterListUpdateCallback.onChanged(SourceFile:1)
                    at androidx.recyclerview.widget.BatchingListUpdateCallback.dispatchLastEvent(SourceFile:2)
                    at androidx.recyclerview.widget.BatchingListUpdateCallback.onChanged(SourceFile:4)
                    at androidx.recyclerview.widget.DiffUtil$DiffResult.dispatchUpdatesTo(SourceFile:34)
                    at androidx.recyclerview.widget.DiffUtil$DiffResult.dispatchUpdatesTo(SourceFile:1)
                    at eu.faircode.email.AdapterFolder.set(SourceFile:46)
                    at eu.faircode.email.FragmentFolders$12.onChanged(SourceFile:3)
                    at eu.faircode.email.FragmentFolders$12.onChanged(SourceFile:1)
                    at androidx.lifecycle.LiveData.considerNotify(SourceFile:6)
                    at androidx.lifecycle.LiveData.dispatchingValue(SourceFile:8)
                    at androidx.lifecycle.LiveData.setValue(SourceFile:4)
                    at androidx.lifecycle.LiveData$1.run(SourceFile:5)
                    at android.os.Handler.handleCallback(Handler.java:938)
             */
        }
    }

    public void search(String query) {
        Log.i("Contacts query=" + query);
        search = query;
        set(all);
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
                for (int i = 0; i < selected.size(); i++)
                    if (selected.get(i).getDisplayName(context).toLowerCase().contains(query.toLowerCase())) {
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

    private List<TupleFolderEx> getHierarchical(List<TupleFolderEx> parents, int indentation, boolean sort_unread_atop) {
        List<TupleFolderEx> result = new ArrayList<>();

        if (parents.size() > 0)
            Collections.sort(parents, parents.get(0).getComparator(context));

        if (sort_unread_atop)
            Collections.sort(parents, new Comparator<TupleFolderEx>() {
                @Override
                public int compare(TupleFolderEx f1, TupleFolderEx f2) {
                    return -Boolean.compare(f1.unseen > 0, f2.unseen > 0);
                }
            });

        for (TupleFolderEx parent : parents) {
            if (parent.hide && !show_hidden)
                continue;

            List<TupleFolderEx> childs = null;
            if (parent.child_refs != null) {
                childs = getHierarchical(parent.child_refs, indentation + 1, sort_unread_atop);
                for (TupleFolderEx child : childs) {
                    parent.childs_unseen += child.unseen;
                    if (child.collapsed)
                        parent.childs_unseen += child.childs_unseen;
                }
            }

            if (!subscribed_only ||
                    EntityFolder.INBOX.equals(parent.type) ||
                    parent.accountProtocol != EntityAccount.TYPE_IMAP ||
                    (parent.subscribed != null && parent.subscribed) ||
                    (childs != null && childs.size() > 0)) {
                parent.indentation = indentation;
                if (show_hidden || !parent.isHidden(listener != null)) {
                    result.add(parent);
                    if (!parent.collapsed && childs != null)
                        result.addAll(childs);
                }
            }
        }

        return result;
    }

    private static class DiffCallback extends DiffUtil.Callback {
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

            if (p1 != null || p2 != null)
                return false;

            return true;
        }
    }

    @Override
    public long getItemId(int position) {
        return selected.get(position).id;
    }

    int getPositionForKey(long key) {
        for (int pos = 0; pos < selected.size(); pos++)
            if (selected.get(pos).id.equals(key))
                return pos;
        return RecyclerView.NO_POSITION;
    }

    public TupleFolderEx getItemAtPosition(int pos) {
        if (pos >= 0 && pos < selected.size())
            return selected.get(pos);
        else
            return null;
    }

    @Override
    public int getItemCount() {
        return selected.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (listener == null)
            return (selected.get(position).selectable ? R.layout.item_folder : R.layout.item_folder_unselectable);
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
        TupleFolderEx folder = selected.get(position);
        holder.powner.recreate(folder == null ? null : folder.id);

        holder.unwire();
        holder.bindTo(folder);
        holder.wire();
    }

    interface IFolderSelectedListener {
        void onFolderSelected(@NonNull TupleFolderEx folder);

        boolean onFolderLongPress(@NonNull TupleFolderEx folder);
    }
}
