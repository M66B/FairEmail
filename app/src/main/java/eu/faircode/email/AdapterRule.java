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
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

public class AdapterRule extends RecyclerView.Adapter<AdapterRule.ViewHolder> {
    private Fragment parentFragment;
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private int protocol = -1;
    private List<TupleRuleEx> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private TextView tvName;
        private TextView tvOrder;
        private ImageView ivStop;
        private TextView tvCondition;
        private TextView tvAction;
        private TextView tvApplied;

        private NumberFormat NF = NumberFormat.getNumberInstance();
        private TwoStateOwner powner = new TwoStateOwner(owner, "RulePopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            tvName = itemView.findViewById(R.id.tvName);
            tvOrder = itemView.findViewById(R.id.tvOrder);
            ivStop = itemView.findViewById(R.id.ivStop);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvApplied = itemView.findViewById(R.id.tvApplied);
        }

        private void wire() {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
        }

        private void bindTo(TupleRuleEx rule) {
            view.setActivated(!rule.enabled);
            tvName.setText(rule.name);
            tvOrder.setText(Integer.toString(rule.order));
            ivStop.setVisibility(rule.stop ? View.VISIBLE : View.INVISIBLE);

            try {
                List<String> condition = new ArrayList<>();
                JSONObject jcondition = new JSONObject(rule.condition);
                if (jcondition.has("sender"))
                    condition.add(context.getString(R.string.title_rule_sender));
                if (jcondition.has("recipient"))
                    condition.add(context.getString(R.string.title_rule_recipient));
                if (jcondition.has("subject"))
                    condition.add(context.getString(R.string.title_rule_subject));
                if (jcondition.has("header"))
                    condition.add(context.getString(R.string.title_rule_header));
                if (jcondition.has("schedule"))
                    condition.add(context.getString(R.string.title_rule_time));
                tvCondition.setText(TextUtils.join(" & ", condition));
            } catch (Throwable ex) {
                tvCondition.setText(ex.getMessage());
            }

            try {
                JSONObject jaction = new JSONObject(rule.action);
                int type = jaction.getInt("type");
                switch (type) {
                    case EntityRule.TYPE_NOOP:
                        tvAction.setText(R.string.title_rule_noop);
                        break;
                    case EntityRule.TYPE_SEEN:
                        tvAction.setText(R.string.title_rule_seen);
                        break;
                    case EntityRule.TYPE_UNSEEN:
                        tvAction.setText(R.string.title_rule_unseen);
                        break;
                    case EntityRule.TYPE_HIDE:
                        tvAction.setText(R.string.title_rule_hide);
                        break;
                    case EntityRule.TYPE_IGNORE:
                        tvAction.setText(R.string.title_rule_ignore);
                        break;
                    case EntityRule.TYPE_SNOOZE:
                        tvAction.setText(R.string.title_rule_snooze);
                        break;
                    case EntityRule.TYPE_FLAG:
                        tvAction.setText(R.string.title_rule_flag);
                        break;
                    case EntityRule.TYPE_IMPORTANCE:
                        tvAction.setText(R.string.title_rule_importance);
                        break;
                    case EntityRule.TYPE_KEYWORD:
                        tvAction.setText(R.string.title_rule_keyword);
                        break;
                    case EntityRule.TYPE_MOVE:
                        tvAction.setText(R.string.title_rule_move);
                        break;
                    case EntityRule.TYPE_COPY:
                        tvAction.setText(R.string.title_rule_copy);
                        break;
                    case EntityRule.TYPE_ANSWER:
                        tvAction.setText(R.string.title_rule_answer);
                        break;
                    case EntityRule.TYPE_TTS:
                        tvAction.setText(R.string.title_rule_tts);
                        break;
                    case EntityRule.TYPE_AUTOMATION:
                        tvAction.setText(R.string.title_rule_automation);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown action type=" + type);
                }
            } catch (Throwable ex) {
                tvAction.setText(ex.getMessage());
            }

            tvApplied.setText(NF.format(rule.applied));
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleRuleEx rule = items.get(pos);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_EDIT_RULE)
                            .putExtra("id", rule.id)
                            .putExtra("account", rule.account)
                            .putExtra("folder", rule.folder)
                            .putExtra("protocol", protocol));
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleRuleEx rule = items.get(pos);

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            SpannableString ss = new SpannableString(rule.name);
            ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
            popupMenu.getMenu().add(Menu.NONE, 0, 0, ss).setEnabled(false);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_rule_enabled, 1, R.string.title_rule_enabled)
                    .setCheckable(true).setChecked(rule.enabled);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_rule_execute, 2, R.string.title_rule_execute)
                    .setEnabled(ActivityBilling.isPro(context));
            popupMenu.getMenu().add(Menu.NONE, R.string.title_reset, 3, R.string.title_reset);
            if (protocol == EntityAccount.TYPE_IMAP) {
                popupMenu.getMenu().add(Menu.NONE, R.string.title_move_to_folder, 4, R.string.title_move_to_folder);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_copy, 5, R.string.title_copy);
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.string.title_rule_enabled:
                            onActionEnabled(!item.isChecked());
                            return true;

                        case R.string.title_rule_execute:
                            onActionExecute();
                            return true;

                        case R.string.title_reset:
                            onActionReset();
                            return true;

                        case R.string.title_move_to_folder:
                            onActionMove();
                            return true;

                        case R.string.title_copy:
                            onActionCopy();
                            return true;

                        default:
                            return false;
                    }
                }

                private void onActionEnabled(boolean enabled) {
                    Bundle args = new Bundle();
                    args.putLong("id", rule.id);
                    args.putBoolean("enabled", enabled);

                    new SimpleTask<Boolean>() {
                        @Override
                        protected Boolean onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean enabled = args.getBoolean("enabled");

                            DB db = DB.getInstance(context);
                            db.rule().setRuleEnabled(id, enabled);

                            return enabled;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "rule:enable");
                }

                private void onActionExecute() {
                    Bundle args = new Bundle();
                    args.putLong("id", rule.id);

                    new SimpleTask<Integer>() {
                        @Override
                        protected void onPreExecute(Bundle args) {
                            ToastEx.makeText(context, R.string.title_executing, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        protected Integer onExecute(Context context, Bundle args) throws JSONException, MessagingException, IOException {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            EntityRule rule = db.rule().getRule(id);
                            if (rule == null)
                                return 0;

                            JSONObject jcondition = new JSONObject(rule.condition);
                            JSONObject jheader = jcondition.optJSONObject("header");
                            if (jheader != null)
                                return 0;

                            int applied = 0;
                            List<Long> ids =
                                    db.message().getMessageIdsByFolder(rule.folder);
                            for (long mid : ids)
                                try {
                                    db.beginTransaction();

                                    EntityMessage message = db.message().getMessage(mid);
                                    if (message == null)
                                        continue;

                                    if (rule.matches(context, message, null))
                                        if (rule.execute(context, message))
                                            applied++;

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                            return applied;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Integer applied) {
                            if (applied > 0)
                                ServiceSynchronize.eval(context, "rules/manual");

                            Snackbar.make(
                                    parentFragment.getView(),
                                    context.getString(R.string.title_rule_applied, applied), Snackbar.LENGTH_LONG)
                                    .setGestureInsetBottomIgnored(true).show();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex, false);
                        }
                    }.execute(context, owner, args, "rule:execute");
                }

                private void onActionReset() {
                    Bundle args = new Bundle();
                    args.putLong("id", rule.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            db.rule().resetRule(id);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "rule:execute");
                }

                private void onActionMove() {
                    Bundle args = new Bundle();
                    args.putString("title", context.getString(R.string.title_move_to_folder));
                    args.putLong("account", rule.account);
                    args.putLongArray("disabled", new long[]{rule.folder});
                    args.putLong("rule", rule.id);

                    FragmentDialogFolder fragment = new FragmentDialogFolder();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(parentFragment, FragmentRules.REQUEST_MOVE);
                    fragment.show(parentFragment.getParentFragmentManager(), "rule:move");
                }

                private void onActionCopy() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_EDIT_RULE)
                                    .putExtra("id", rule.id)
                                    .putExtra("account", rule.account)
                                    .putExtra("folder", rule.folder)
                                    .putExtra("protocol", protocol)
                                    .putExtra("copy", true));
                }
            });

            popupMenu.show();

            return true;
        }
    }

    AdapterRule(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterRule.this + " parent destroyed");
                AdapterRule.this.parentFragment = null;
            }
        });
    }

    public void set(int protocol, @NonNull List<TupleRuleEx> rules) {
        this.protocol = protocol;
        Log.i("Set protocol=" + protocol + " rules=" + rules.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, rules), false);

        items = rules;

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
        private List<TupleRuleEx> prev = new ArrayList<>();
        private List<TupleRuleEx> next = new ArrayList<>();

        DiffCallback(List<TupleRuleEx> prev, List<TupleRuleEx> next) {
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
            TupleRuleEx r1 = prev.get(oldItemPosition);
            TupleRuleEx r2 = next.get(newItemPosition);
            return r1.id.equals(r2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleRuleEx r1 = prev.get(oldItemPosition);
            TupleRuleEx r2 = next.get(newItemPosition);
            return r1.equals(r2);
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
        return new ViewHolder(inflater.inflate(R.layout.item_rule, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();
        TupleRuleEx rule = items.get(position);
        holder.bindTo(rule);
        holder.wire();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.powner.recreate();
    }
}
