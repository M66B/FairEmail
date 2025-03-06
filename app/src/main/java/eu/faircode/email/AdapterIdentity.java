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

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_GMAIL;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_OAUTH;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdapterIdentity extends RecyclerView.Adapter<AdapterIdentity.ViewHolder> {
    private Fragment parentFragment;
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private int colorStripeWidth;
    private int colorWarning;
    private int textColorTertiary;
    private boolean debug;

    private List<TupleIdentityEx> items = new ArrayList<>();

    private DateFormat DTF;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private View vwColor;
        private ImageView ivSync;
        private ImageView ivOAuth;
        private ImageView ivPrimary;
        private ImageView ivGroup;
        private TextView tvName;
        private TextView tvUser;
        private TextView tvHost;
        private ImageView ivState;
        private TextView tvAccount;
        private TextView tvSignKeyId;
        private TextView tvLast;
        private TextView tvMaxSize;
        private TextView tvDrafts;
        private TextView tvError;

        private TwoStateOwner powner = new TwoStateOwner(owner, "IdentityPopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            ivSync = itemView.findViewById(R.id.ivSync);
            ivOAuth = itemView.findViewById(R.id.ivOAuth);
            ivPrimary = itemView.findViewById(R.id.ivPrimary);
            ivGroup = itemView.findViewById(R.id.ivGroup);
            tvName = itemView.findViewById(R.id.tvName);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvHost = itemView.findViewById(R.id.tvHost);
            ivState = itemView.findViewById(R.id.ivState);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            tvSignKeyId = itemView.findViewById(R.id.tvSignKeyId);
            tvLast = itemView.findViewById(R.id.tvLast);
            tvMaxSize = itemView.findViewById(R.id.tvMaxSize);
            tvDrafts = itemView.findViewById(R.id.tvDrafts);
            tvError = itemView.findViewById(R.id.tvError);

            if (vwColor != null)
                vwColor.getLayoutParams().width = colorStripeWidth;
        }

        private void wire() {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
        }

        private void bindTo(TupleIdentityEx identity) {
            view.setAlpha(identity.synchronize && identity.accountSynchronize ? 1.0f : Helper.LOW_LIGHT);
            Integer color = (identity.color == null ? identity.accountColor : identity.color);
            vwColor.setBackgroundColor(color == null ? Color.TRANSPARENT : color);
            vwColor.setVisibility(ActivityBilling.isPro(context) ? View.VISIBLE : View.INVISIBLE);

            ivSync.setImageResource(identity.synchronize ? R.drawable.twotone_sync_24 : R.drawable.twotone_sync_disabled_24);
            ivSync.setContentDescription(context.getString(identity.synchronize ? R.string.title_legend_synchronize_on : R.string.title_legend_synchronize_off));

            ivOAuth.setVisibility(identity.auth_type == AUTH_TYPE_PASSWORD ? View.GONE : View.VISIBLE);
            ivOAuth.setImageResource(
                    identity.auth_type == AUTH_TYPE_GMAIL || identity.auth_type == AUTH_TYPE_OAUTH
                            ? R.drawable.twotone_security_24 : R.drawable.twotone_hub_24);
            ivPrimary.setVisibility(identity.primary ? View.VISIBLE : View.GONE);
            ivGroup.setVisibility(identity.self ? View.GONE : View.VISIBLE);
            tvName.setText(identity.getDisplayName());

            StringBuilder user = new StringBuilder(identity.email);
            if (identity.provider != null && (BuildConfig.DEBUG || debug))
                user.append(" (").append(identity.provider).append(')');
            tvUser.setText(user);

            if ("connected".equals(identity.state)) {
                ivState.setImageResource(R.drawable.twotone_cloud_done_24);
                ivState.setContentDescription(context.getString(R.string.title_legend_connected));
            } else if ("connecting".equals(identity.state)) {
                ivState.setImageResource(R.drawable.twotone_cloud_queue_24);
                ivState.setContentDescription(context.getString(R.string.title_legend_connecting));
            } else {
                ivState.setImageDrawable(null);
                ivState.setContentDescription(null);
            }
            ivState.setVisibility(identity.synchronize ? View.VISIBLE : View.INVISIBLE);

            tvHost.setText(String.format("%s:%d/%s",
                    identity.host,
                    identity.port,
                    EmailService.getEncryptionName(identity.encryption)));
            tvHost.setTextColor(identity.insecure ? colorWarning : textColorTertiary);
            tvAccount.setText(identity.accountName);

            StringBuilder sb = new StringBuilder();
            if (identity.sign_key != null)
                sb.append(Long.toHexString(identity.sign_key));
            if (identity.sign_key_alias != null) {
                if (sb.length() != 0)
                    sb.append(", ");
                sb.append(identity.sign_key_alias);
            }

            tvSignKeyId.setText(context.getString(R.string.title_sign_key, sb.toString()));
            tvSignKeyId.setVisibility(sb.length() > 0 ? View.VISIBLE : View.GONE);

            tvLast.setText(context.getString(R.string.title_last_connected,
                    (identity.last_connected == null ? "-" : DTF.format(identity.last_connected))) +
                    (BuildConfig.DEBUG ?
                            "/" + (identity.last_modified == null ? "-" : DTF.format(identity.last_modified)) : ""));

            tvMaxSize.setText(identity.max_size == null ? null : Helper.humanReadableByteCount(identity.max_size));
            tvMaxSize.setVisibility(identity.max_size == null ? View.GONE : View.VISIBLE);

            tvDrafts.setVisibility(identity.drafts == null ? View.VISIBLE : View.GONE);

            tvError.setText(identity.error);
            tvError.setVisibility(identity.error == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleIdentityEx identity = items.get(pos);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivitySetup.ACTION_EDIT_IDENTITY)
                            .putExtra("id", identity.id));
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleIdentityEx identity = items.get(pos);

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            int order = 0;
            SpannableString ss = new SpannableString(identity.email);
            ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
            popupMenu.getMenu().add(Menu.NONE, 0, order++, ss).setEnabled(false);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_enabled, order++, R.string.title_enabled)
                    .setCheckable(true).setChecked(identity.synchronize);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_primary, order++, R.string.title_primary)
                    .setCheckable(true).setChecked(identity.primary);

            if (parentFragment instanceof FragmentIdentities)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_color, order++, R.string.title_edit_color);

            if (identity.sign_key != null || identity.sign_key_alias != null)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_reset_sign_key, order++, R.string.title_reset_sign_key);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_advanced_create_alias, order++, R.string.title_advanced_create_alias);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_properties, order++, R.string.title_edit_properties);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_copy, order++, R.string.title_copy);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, order++, R.string.title_delete);

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
                    } else if (itemId == R.string.title_edit_color) {
                        onActionEditColor();
                        return true;
                    } else if (itemId == R.string.title_reset_sign_key) {
                        onActionClearSignKey();
                        return true;
                    } else if (itemId == R.string.title_advanced_create_alias) {
                        onActionAlias();
                        return true;
                    } else if (itemId == R.string.title_edit_properties) {
                        onClick(view);
                        return true;
                    } else if (itemId == R.string.title_copy) {
                        onActionCopy();
                        return true;
                    } else if (itemId == R.string.title_delete) {
                        onActionDelete();
                        return true;
                    }
                    return false;
                }

                private void onActionSync(boolean sync) {
                    Bundle args = new Bundle();
                    args.putLong("id", identity.id);
                    args.putBoolean("sync", sync);

                    new SimpleTask<Boolean>() {
                        @Override
                        protected Boolean onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean sync = args.getBoolean("sync");

                            DB db = DB.getInstance(context);
                            if (!sync)
                                db.identity().setIdentityError(id, null);
                            db.identity().setIdentitySynchronize(id, sync);

                            return sync;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "identity:enable");
                }

                private void onActionPrimary(boolean primary) {
                    Bundle args = new Bundle();
                    args.putLong("id", identity.id);
                    args.putLong("account", identity.account);
                    args.putBoolean("primary", primary);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            long account = args.getLong("account");
                            boolean primary = args.getBoolean("primary");

                            DB db = DB.getInstance(context);

                            try {
                                db.beginTransaction();

                                if (primary)
                                    db.identity().resetPrimary(account);

                                db.identity().setIdentityPrimary(id, primary);

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
                    }.execute(context, owner, args, "identity:primary");
                }

                private void onActionEditColor() {
                    Bundle args = new Bundle();
                    args.putLong("id", identity.id);
                    args.putInt("color", identity.color == null ? Color.TRANSPARENT : identity.color);
                    args.putString("title", context.getString(R.string.title_color));
                    args.putBoolean("reset", true);

                    FragmentDialogColor fragment = new FragmentDialogColor();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(parentFragment, ActivitySetup.REQUEST_EDIT_IDENITY_COLOR);
                    fragment.show(parentFragment.getParentFragmentManager(), "edit:color");
                }

                private void onActionClearSignKey() {
                    Bundle args = new Bundle();
                    args.putLong("id", identity.id);

                    new SimpleTask<Boolean>() {
                        @Override
                        protected Boolean onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                db.identity().setIdentitySignKey(id, null);
                                db.identity().setIdentitySignKeyAlias(id, null);

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
                    }.execute(context, owner, args, "identitty:clear_sign_key");
                }

                private void onActionAlias() {
                    Bundle args = new Bundle();
                    args.putLong("id", identity.id);
                    args.putString("name", identity.name);
                    args.putString("email", identity.email);

                    FragmentDialogAlias fragment = new FragmentDialogAlias();
                    fragment.setArguments(args);
                    fragment.show(parentFragment.getParentFragmentManager(), "alias:create");
                }

                private void onActionCopy() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivitySetup.ACTION_EDIT_IDENTITY)
                                    .putExtra("id", identity.id)
                                    .putExtra("copy", true));
                }

                private void onActionDelete() {
                    new AlertDialog.Builder(view.getContext())
                            .setIcon(R.drawable.twotone_warning_24)
                            .setTitle(identity.email)
                            .setMessage(R.string.title_identity_delete)
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

                private void onDelete() {
                    Bundle args = new Bundle();
                    args.putLong("id", identity.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            db.identity().deleteIdentity(id);

                            Core.clearIdentities();

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "identity:delete");
                }
            });

            popupMenu.show();

            return true;
        }
    }

    AdapterIdentity(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean color_stripe_wide = prefs.getBoolean("color_stripe_wide", false);
        this.colorStripeWidth = Helper.dp2pixels(context, color_stripe_wide ? 12 : 6);
        this.colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
        this.textColorTertiary = Helper.resolveColor(context, android.R.attr.textColorTertiary);
        this.debug = prefs.getBoolean("debug", false);

        this.DTF = Helper.getDateTimeInstance(context, DateFormat.SHORT, DateFormat.SHORT);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterIdentity.this + " parent destroyed");
                AdapterIdentity.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<TupleIdentityEx> identities) {
        Log.i("Set identities=" + identities.size());

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(identities, new Comparator<TupleIdentityEx>() {
            @Override
            public int compare(TupleIdentityEx i1, TupleIdentityEx i2) {
                int c = collator.compare(
                        i1.accountCategory == null ? "" : i1.accountCategory,
                        i2.accountCategory == null ? "" : i2.accountCategory);
                if (c != 0)
                    return c;
                int n = collator.compare(i1.getDisplayName(), i2.getDisplayName());
                if (n != 0)
                    return n;
                int e = collator.compare(i1.email, i2.email);
                if (e != 0)
                    return e;
                return i1.id.compareTo(i2.id);
            }
        });

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, identities), false);

        items = identities;

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

    private static class DiffCallback extends DiffUtil.Callback {
        private List<TupleIdentityEx> prev = new ArrayList<>();
        private List<TupleIdentityEx> next = new ArrayList<>();

        DiffCallback(List<TupleIdentityEx> prev, List<TupleIdentityEx> next) {
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
            TupleIdentityEx i1 = prev.get(oldItemPosition);
            TupleIdentityEx i2 = next.get(newItemPosition);
            return i1.id.equals(i2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleIdentityEx i1 = prev.get(oldItemPosition);
            TupleIdentityEx i2 = next.get(newItemPosition);
            return i1.equals(i2);
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    public TupleIdentityEx getItemAtPosition(int pos) {
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
        return new ViewHolder(inflater.inflate(R.layout.item_identity, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TupleIdentityEx identity = items.get(position);
        holder.powner.recreate(identity == null ? null : identity.id);

        holder.unwire();
        holder.bindTo(identity);
        holder.wire();
    }
}
