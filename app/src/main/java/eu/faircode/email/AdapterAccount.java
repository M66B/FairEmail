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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_GMAIL;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TouchDelegate;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterAccount extends RecyclerView.Adapter<AdapterAccount.ViewHolder> {
    private Fragment parentFragment;
    private boolean settings;
    private boolean compact;

    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private int colorStripeWidth;
    private int colorWarning;
    private int colorUnread;
    private int textColorSecondary;
    private boolean debug;

    private List<TupleAccountEx> items = new ArrayList<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();
    private DateFormat DTF;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private View vwColor;
        private ImageView ivOAuth;
        private ImageView ivPrimary;
        private ImageView ivNotify;
        private TextView tvName;
        private ImageView ivSync;
        private ImageButton ibInbox;
        private TextView tvUser;
        private ImageView ivState;
        private TextView tvHost;
        private TextView tvCreated;
        private TextView tvLast;
        private TextView tvUsage;
        private TextView tvBackoff;
        private TextView tvQuota;
        private TextView tvMaxSize;
        private TextView tvId;
        private TextView tvCapabilities;
        private TextView tvIdentity;
        private TextView tvDrafts;
        private TextView tvSent;
        private TextView tvWarning;
        private TextView tvError;
        private Button btnHelp;
        private Group grpSettings;

        private TwoStateOwner powner = new TwoStateOwner(owner, "AccountPopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            ivSync = itemView.findViewById(R.id.ivSync);
            ibInbox = itemView.findViewById(R.id.ibInbox);
            ivOAuth = itemView.findViewById(R.id.ivOAuth);
            ivPrimary = itemView.findViewById(R.id.ivPrimary);
            ivNotify = itemView.findViewById(R.id.ivNotify);
            tvName = itemView.findViewById(R.id.tvName);
            tvUser = itemView.findViewById(R.id.tvUser);
            ivState = itemView.findViewById(R.id.ivState);
            tvHost = itemView.findViewById(R.id.tvHost);
            tvCreated = itemView.findViewById(R.id.tvCreated);
            tvLast = itemView.findViewById(R.id.tvLast);
            tvUsage = itemView.findViewById(R.id.tvUsage);
            tvBackoff = itemView.findViewById(R.id.tvBackoff);
            tvQuota = itemView.findViewById(R.id.tvQuota);
            tvMaxSize = itemView.findViewById(R.id.tvMaxSize);
            tvId = itemView.findViewById(R.id.tvId);
            tvCapabilities = itemView.findViewById(R.id.tvCapabilities);
            tvIdentity = itemView.findViewById(R.id.tvIdentity);
            tvDrafts = itemView.findViewById(R.id.tvDrafts);
            tvSent = itemView.findViewById(R.id.tvSent);
            tvWarning = itemView.findViewById(R.id.tvWarning);
            tvError = itemView.findViewById(R.id.tvError);
            btnHelp = itemView.findViewById(R.id.btnHelp);
            grpSettings = itemView.findViewById(R.id.grpSettings);

            if (vwColor != null)
                vwColor.getLayoutParams().width = colorStripeWidth;
        }

        private void wire() {
            if (!settings)
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        int left;
                        int right;
                        if (view.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
                            left = view.getWidth() - view.getWidth() / 4;
                            right = view.getWidth();
                        } else {
                            left = 0;
                            right = view.getWidth() / 4;
                        }
                        Rect rect = new Rect(
                                left,
                                view.getTop(),
                                right,
                                view.getBottom());
                        view.setTouchDelegate(new TouchDelegate(rect, ibInbox));
                    }
                });

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            ibInbox.setOnClickListener(this);
            btnHelp.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
            ibInbox.setOnClickListener(null);
            btnHelp.setOnClickListener(null);
        }

        private void bindTo(TupleAccountEx account) {
            view.setActivated(account.tbd != null);
            view.setAlpha(account.synchronize ? 1.0f : Helper.LOW_LIGHT);
            vwColor.setBackgroundColor(account.color == null ? Color.TRANSPARENT : account.color);
            vwColor.setVisibility(ActivityBilling.isPro(context) ? View.VISIBLE : View.INVISIBLE);

            ivSync.setImageResource(account.synchronize ? R.drawable.twotone_sync_24 : R.drawable.twotone_sync_disabled_24);
            ivSync.setContentDescription(context.getString(account.synchronize ? R.string.title_legend_synchronize_on : R.string.title_legend_synchronize_off));

            ivOAuth.setImageDrawable(ContextCompat.getDrawable(context, account.auth_type == AUTH_TYPE_GMAIL
                    ? R.drawable.twotone_android_24 : R.drawable.twotone_security_24));
            ivOAuth.setVisibility(
                    settings && account.auth_type != AUTH_TYPE_PASSWORD ? View.VISIBLE : View.GONE);
            ivPrimary.setVisibility(account.primary ? View.VISIBLE : View.GONE);
            ivNotify.setVisibility(account.notify ? View.VISIBLE : View.GONE);

            if (settings) {
                tvName.setText(account.name);
                tvName.setTextColor(account.protocol == EntityAccount.TYPE_IMAP
                        ? textColorSecondary : colorWarning);
            } else {
                if (account.unseen > 0)
                    tvName.setText(context.getString(R.string.title_name_count, account.name, NF.format(account.unseen)));
                else
                    tvName.setText(account.name);

                tvName.setTypeface(account.unseen > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                tvName.setTextColor(account.unseen > 0 ? colorUnread : textColorSecondary);
            }

            StringBuilder user = new StringBuilder(account.user);
            if (account.provider != null && (BuildConfig.DEBUG || debug))
                user.append(" (").append(account.provider).append(')');
            tvUser.setText(user);

            if ("connected".equals(account.state)) {
                ivState.setImageResource(R.drawable.twotone_cloud_done_24);
                ivState.setContentDescription(context.getString(R.string.title_legend_connected));
            } else if ("connecting".equals(account.state)) {
                ivState.setImageResource(R.drawable.twotone_cloud_queue_24);
                ivState.setContentDescription(context.getString(R.string.title_legend_connecting));
            } else if ("closing".equals(account.state)) {
                ivState.setImageResource(R.drawable.twotone_cancel_24);
                ivState.setContentDescription(context.getString(R.string.title_legend_closing));
            } else {
                if (account.backoff_until == null) {
                    ivState.setImageResource(R.drawable.twotone_cloud_off_24);
                    ivState.setContentDescription(context.getString(R.string.title_legend_disconnected));
                } else {
                    ivState.setImageResource(R.drawable.twotone_update_24);
                    ivState.setContentDescription(context.getString(R.string.title_legend_backoff));
                }
            }
            ivState.setVisibility(account.synchronize || account.state != null ? View.VISIBLE : View.INVISIBLE);

            tvHost.setText(String.format("%s:%d", account.host, account.port));
            tvCreated.setVisibility(debug ? View.VISIBLE : View.GONE);
            tvCreated.setText(context.getString(R.string.title_created_at,
                    account.created == null ? null : DTF.format(account.created)));
            tvLast.setVisibility(compact ? View.GONE : View.VISIBLE);
            tvLast.setText(context.getString(R.string.title_last_connected,
                    (account.last_connected == null ? "-" : DTF.format(account.last_connected)) +
                            (BuildConfig.DEBUG ?
                                    " " + account.poll_interval +
                                            "/" + account.keep_alive_ok +
                                            "/" + account.keep_alive_failed +
                                            "/" + account.keep_alive_succeeded : "")));

            tvBackoff.setText(context.getString(R.string.title_backoff_until,
                    account.backoff_until == null ? "-" : DTF.format(account.backoff_until)));
            tvBackoff.setVisibility(account.backoff_until == null || !settings ? View.GONE : View.VISIBLE);

            Integer percent = account.getQuotaPercentage();
            boolean warning = (percent != null && percent > EntityAccount.QUOTA_WARNING);

            tvUsage.setText(settings || percent == null ? null : NF.format(percent) + "%");
            tvUsage.setVisibility(settings || percent == null || (compact && !warning) ? View.GONE : View.VISIBLE);
            tvQuota.setText(context.getString(R.string.title_storage_quota,
                    (account.quota_usage == null ? "-" : Helper.humanReadableByteCount(account.quota_usage)),
                    (account.quota_limit == null ? "-" : Helper.humanReadableByteCount(account.quota_limit))));
            tvQuota.setVisibility(settings && (account.quota_usage != null || account.quota_limit != null) ? View.VISIBLE : View.GONE);

            tvUsage.setTextColor(warning ? colorWarning : textColorSecondary);
            tvUsage.setTypeface(warning ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

            tvQuota.setTextColor(warning ? colorWarning : textColorSecondary);
            tvQuota.setTypeface(warning ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

            tvMaxSize.setText(account.max_size == null ? null : Helper.humanReadableByteCount(account.max_size));
            tvMaxSize.setVisibility(settings && account.max_size != null && BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
            if (tvMaxSize.getVisibility() == View.VISIBLE)
                tvQuota.setVisibility(View.VISIBLE);

            tvId.setText(account.id + "/" + account.uuid);
            tvId.setVisibility(settings && BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

            tvCapabilities.setText(account.capabilities);

            tvCapabilities.setVisibility(settings && (debug || BuildConfig.DEBUG) &&
                    !TextUtils.isEmpty(account.capabilities) ? View.VISIBLE : View.GONE);

            tvIdentity.setVisibility(account.identities > 0 || !settings ? View.GONE : View.VISIBLE);
            tvDrafts.setVisibility(account.drafts != null || !settings ? View.GONE : View.VISIBLE);
            tvSent.setVisibility(account.protocol != EntityAccount.TYPE_IMAP ||
                    account.sent != null || !settings ? View.GONE : View.VISIBLE);

            tvWarning.setText(account.warning);
            tvWarning.setVisibility(account.warning == null || !settings ? View.GONE : View.VISIBLE);

            tvError.setText(account.error);
            tvError.setVisibility(account.error == null ? View.GONE : View.VISIBLE);
            btnHelp.setVisibility(account.error == null ? View.GONE : View.VISIBLE);

            ibInbox.setVisibility(settings ? View.GONE : View.VISIBLE);
            grpSettings.setVisibility(settings ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btnHelp)
                Helper.viewFAQ(context, 22);
            else {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return;

                TupleAccountEx account = items.get(pos);
                if (account.tbd != null)
                    return;

                if (view.getId() == R.id.ibInbox) {
                    Bundle args = new Bundle();
                    args.putLong("id", account.id);

                    new SimpleTask<EntityFolder>() {
                        @Override
                        protected EntityFolder onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            return db.folder().getFolderByType(id, EntityFolder.INBOX);
                        }

                        @Override
                        protected void onExecuted(Bundle args, EntityFolder inbox) {
                            if (inbox == null)
                                return;

                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                            .putExtra("account", inbox.account)
                                            .putExtra("folder", inbox.id)
                                            .putExtra("type", inbox.type));

                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "account:inbox");
                } else {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(settings ? ActivitySetup.ACTION_EDIT_ACCOUNT : ActivityView.ACTION_VIEW_FOLDERS)
                                    .putExtra("id", account.id)
                                    .putExtra("protocol", account.protocol));
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleAccountEx account = items.get(pos);
            if (account.tbd != null)
                return false;

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            int order = 0;
            SpannableString ss = new SpannableString(account.name);
            ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
            popupMenu.getMenu().add(Menu.NONE, 0, order++, ss).setEnabled(false);

            if (settings)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_enabled, order++, R.string.title_enabled)
                        .setCheckable(true).setChecked(account.synchronize);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_primary, order++, R.string.title_primary)
                    .setCheckable(true).setChecked(account.primary);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = EntityAccount.getNotificationChannelId(account.id);
                NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                NotificationChannel channel = nm.getNotificationChannel(channelId);
                if (channel == null)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_create_channel, order++, R.string.title_create_channel);
                else {
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_channel, order++, R.string.title_edit_channel);
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_channel, order++, R.string.title_delete_channel);
                }
            }

            if (settings)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_properties, order++, R.string.title_edit_properties);

            if (account.protocol == EntityAccount.TYPE_IMAP && settings)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_copy, order++, R.string.title_copy);

            if (settings) {
                popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, order++, R.string.title_delete);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_log, order++, R.string.title_log);
            }

            if (debug)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_reset, order++, R.string.title_reset);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.string.title_enabled) {
                        onActionSync(!item.isChecked());
                        return true;
                    } else if (itemId == R.string.title_primary) {
                        onActionPrimary(!item.isChecked());
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
                    } else if (itemId == R.string.title_edit_properties) {
                        ViewHolder.this.onClick(view);
                        return true;
                    } else if (itemId == R.string.title_copy) {
                        onActionCopy();
                        return true;
                    } else if (itemId == R.string.title_delete) {
                        onActionDelete();
                        return true;
                    } else if (itemId == R.string.title_log) {
                        onActionLog();
                        return true;
                    } else if (itemId == R.string.title_reset) {
                        onActionReset();
                        return true;
                    }
                    return false;
                }

                private void onActionSync(boolean sync) {
                    Bundle args = new Bundle();
                    args.putLong("id", account.id);
                    args.putBoolean("sync", sync);

                    new SimpleTask<Boolean>() {
                        @Override
                        protected Boolean onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean sync = args.getBoolean("sync");

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                if (!sync) {
                                    db.account().setAccountWarning(id, null);
                                    db.account().setAccountError(id, null);
                                    db.account().setAccountConnected(id, null);
                                }

                                db.account().setAccountSynchronize(id, sync);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            ServiceSynchronize.eval(context, "account sync=" + sync);

                            return sync;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "account:enable");
                }

                private void onActionPrimary(boolean primary) {
                    Bundle args = new Bundle();
                    args.putLong("id", account.id);
                    args.putBoolean("primary", primary);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean primary = args.getBoolean("primary");

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                if (primary)
                                    db.account().resetPrimary();
                                db.account().setAccountPrimary(id, primary);

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
                    }.execute(context, owner, args, "account:primary");
                }

                @TargetApi(Build.VERSION_CODES.O)
                private void onActionCreateChannel() {
                    if (!ActivityBilling.isPro(context)) {
                        context.startActivity(new Intent(context, ActivityBilling.class));
                        return;
                    }

                    account.createNotificationChannel(context);

                    Bundle args = new Bundle();
                    args.putLong("id", account.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            db.account().setAccountNotify(id, true);

                            return null;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Void data) {
                            onActionEditChannel();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "create:channel");
                }

                @TargetApi(Build.VERSION_CODES.O)
                private void onActionEditChannel() {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
                            .putExtra(Settings.EXTRA_CHANNEL_ID, EntityAccount.getNotificationChannelId(account.id));
                    try {
                        context.startActivity(intent);
                    } catch (Throwable ex) {
                        Helper.reportNoViewer(context, intent, ex);
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.O)
                private void onActionDeleteChannel() {
                    Bundle args = new Bundle();
                    args.putLong("id", account.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            db.account().setAccountNotify(id, false);

                            return null;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Void data) {
                            account.deleteNotificationChannel(context);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "create:channel");
                }

                private void onActionCopy() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivitySetup.ACTION_EDIT_ACCOUNT)
                                    .putExtra("id", account.id)
                                    .putExtra("protocol", account.protocol)
                                    .putExtra("copy", true));
                }

                private void onActionDelete() {
                    new AlertDialog.Builder(view.getContext())
                            .setIcon(R.drawable.twotone_warning_24)
                            .setTitle(account.name)
                            .setMessage(R.string.title_account_delete)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onDelete();
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

                private void onActionLog() {
                    if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        parentFragment.getParentFragmentManager().popBackStack("logs", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    Bundle args = new Bundle();
                    args.putLong("account", account.id);

                    Fragment fragment = new FragmentLogs();
                    fragment.setArguments(args);

                    FragmentTransaction fragmentTransaction = parentFragment.getParentFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("logs");
                    fragmentTransaction.commit();
                }

                private void onActionReset() {
                    Bundle args = new Bundle();
                    args.putLong("id", account.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            db.account().resetCreated(id);

                            return null;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Void data) {
                            ToastEx.makeText(context, R.string.title_completed, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "account:reset");
                }

                private void onDelete() {
                    Bundle args = new Bundle();
                    args.putLong("id", account.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            db.account().setAccountTbd(id);

                            ServiceSynchronize.eval(context, "delete account");

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "account:delete");
                }
            });

            popupMenu.show();

            return true;
        }
    }

    AdapterAccount(final Fragment parentFragment, boolean settings, boolean compact) {
        this.parentFragment = parentFragment;
        this.settings = settings;
        this.compact = compact;

        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean color_stripe_wide = prefs.getBoolean("color_stripe_wide", false);
        this.colorStripeWidth = Helper.dp2pixels(context, color_stripe_wide ? 12 : 6);
        boolean highlight_unread = prefs.getBoolean("highlight_unread", true);
        this.colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
        int colorHighlight = prefs.getInt("highlight_color", Helper.resolveColor(context, R.attr.colorUnreadHighlight));
        this.colorUnread = (highlight_unread ? colorHighlight : Helper.resolveColor(context, R.attr.colorUnread));
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
        this.debug = prefs.getBoolean("debug", false);

        this.DTF = Helper.getDateTimeInstance(context, DateFormat.SHORT, DateFormat.MEDIUM);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterAccount.this + " parent destroyed");
                AdapterAccount.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<TupleAccountEx> accounts) {
        Log.i("Set accounts=" + accounts.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, accounts), false);

        items = accounts;

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

    void setCompact(boolean compact) {
        if (this.compact != compact)
            this.compact = compact;
    }

    private static class DiffCallback extends DiffUtil.Callback {
        private List<TupleAccountEx> prev = new ArrayList<>();
        private List<TupleAccountEx> next = new ArrayList<>();

        DiffCallback(List<TupleAccountEx> prev, List<TupleAccountEx> next) {
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
            TupleAccountEx f1 = prev.get(oldItemPosition);
            TupleAccountEx f2 = next.get(newItemPosition);
            return f1.id.equals(f2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleAccountEx f1 = prev.get(oldItemPosition);
            TupleAccountEx f2 = next.get(newItemPosition);
            return f1.equals(f2);
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    public TupleAccountEx getItemAtPosition(int pos) {
        if (pos >= 0 && pos < items.size())
            return items.get(pos);
        else
            return null;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TupleAccountEx account = items.get(position);
        holder.powner.recreate(account == null ? null : account.id);

        holder.unwire();
        holder.bindTo(account);
        holder.wire();
    }
}
