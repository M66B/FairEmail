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
import android.content.Intent;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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

    private List<TupleIdentityEx> items = new ArrayList<>();

    private DateFormat DTF;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private View vwColor;
        private ImageView ivSync;
        private ImageView ivOAuth;
        private ImageView ivPrimary;
        private TextView tvName;
        private TextView tvUser;
        private TextView tvHost;
        private ImageView ivState;
        private TextView tvAccount;
        private TextView tvSignKeyId;
        private TextView tvLast;
        private TextView tvError;

        private TwoStateOwner powner = new TwoStateOwner(owner, "IdentityPopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            ivSync = itemView.findViewById(R.id.ivSync);
            ivOAuth = itemView.findViewById(R.id.ivOAuth);
            ivPrimary = itemView.findViewById(R.id.ivPrimary);
            tvName = itemView.findViewById(R.id.tvName);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvHost = itemView.findViewById(R.id.tvHost);
            ivState = itemView.findViewById(R.id.ivState);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            tvSignKeyId = itemView.findViewById(R.id.tvSignKeyId);
            tvLast = itemView.findViewById(R.id.tvLast);
            tvError = itemView.findViewById(R.id.tvError);
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
            vwColor.setBackgroundColor(identity.color == null ? Color.TRANSPARENT : identity.color);
            vwColor.setVisibility(ActivityBilling.isPro(context) ? View.VISIBLE : View.INVISIBLE);

            ivSync.setImageResource(identity.synchronize ? R.drawable.baseline_sync_24 : R.drawable.baseline_sync_disabled_24);
            ivSync.setContentDescription(context.getString(identity.synchronize ? R.string.title_legend_synchronize_on : R.string.title_legend_synchronize_off));

            ivOAuth.setVisibility(identity.auth_type == EmailService.AUTH_TYPE_PASSWORD ? View.GONE : View.VISIBLE);
            ivPrimary.setVisibility(identity.primary ? View.VISIBLE : View.GONE);
            tvName.setText(identity.getDisplayName());
            tvUser.setText(identity.email);

            if ("connected".equals(identity.state)) {
                ivState.setImageResource(R.drawable.baseline_cloud_24);
                ivState.setContentDescription(context.getString(R.string.title_legend_connected));
            } else if ("connecting".equals(identity.state)) {
                ivState.setImageResource(R.drawable.baseline_cloud_queue_24);
                ivState.setContentDescription(context.getString(R.string.title_legend_connecting));
            } else {
                ivState.setImageDrawable(null);
                ivState.setContentDescription(null);
            }
            ivState.setVisibility(identity.synchronize ? View.VISIBLE : View.INVISIBLE);

            tvHost.setText(String.format("%s:%d", identity.host, identity.port));
            tvAccount.setText(identity.accountName);

            StringBuilder sb = new StringBuilder();
            if (identity.sign_key != null)
                sb.append(Long.toHexString(identity.sign_key));
            if (identity.sign_key_alias != null) {
                if (sb.length() != 0)
                    sb.append(", ");
                sb.append(identity.sign_key_alias);
            }
            if (identity.encrypt == 1) {
                if (sb.length() != 0)
                    sb.append(", ");
                sb.append("S/MIME");
            }

            tvSignKeyId.setText(context.getString(R.string.title_sign_key, sb.toString()));
            tvSignKeyId.setVisibility(sb.length() > 0 ? View.VISIBLE : View.GONE);

            tvLast.setText(context.getString(R.string.title_last_connected,
                    identity.last_connected == null ? "-" : DTF.format(identity.last_connected)));

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

            SpannableString ss = new SpannableString(identity.email);
            ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
            popupMenu.getMenu().add(Menu.NONE, 0, 0, ss).setEnabled(false);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_enabled, 1, R.string.title_enabled)
                    .setCheckable(true).setChecked(identity.synchronize);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_primary, 2, R.string.title_primary)
                    .setCheckable(true).setChecked(identity.primary);

            if (identity.sign_key != null || identity.sign_key_alias != null)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_reset_sign_key, 3, R.string.title_reset_sign_key);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_copy, 4, R.string.title_copy);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.string.title_enabled:
                            onActionSync(!item.isChecked());
                            return true;

                        case R.string.title_primary:
                            onActionPrimary(!item.isChecked());
                            return true;

                        case R.string.title_reset_sign_key:
                            onActionClearSignKey();
                            return true;

                        case R.string.title_copy:
                            onActionCopy();
                            return true;

                        default:
                            return false;
                    }
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

                                if (identity.primary)
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

                private void onActionCopy() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivitySetup.ACTION_EDIT_IDENTITY)
                                    .putExtra("id", identity.id)
                                    .putExtra("copy", true));
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

        this.DTF = Helper.getDateTimeInstance(context, DateFormat.SHORT, DateFormat.SHORT);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterIdentity.this + " parent destroyed");
                AdapterIdentity.this.parentFragment = null;
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
        diff.dispatchUpdatesTo(this);
    }

    private class DiffCallback extends DiffUtil.Callback {
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
        holder.unwire();

        TupleIdentityEx identity = items.get(position);
        holder.bindTo(identity);

        holder.wire();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.powner.recreate();
    }
}
