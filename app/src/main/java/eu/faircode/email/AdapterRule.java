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

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

public class AdapterRule extends RecyclerView.Adapter<AdapterRule.ViewHolder> {
    private Fragment parentFragment;
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private DateFormat DF;
    private NumberFormat NF = NumberFormat.getNumberInstance();

    private int protocol = -1;
    private String search = null;
    private List<TupleRuleEx> all = new ArrayList<>();
    private List<TupleRuleEx> selected = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private TextView tvName;
        private TextView tvOrder;
        private ImageView ivStop;
        private TextView tvCondition;
        private TextView tvAction;
        private TextView tvLastApplied;
        private TextView tvApplied;

        private TwoStateOwner powner = new TwoStateOwner(owner, "RulePopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            tvName = itemView.findViewById(R.id.tvName);
            tvOrder = itemView.findViewById(R.id.tvOrder);
            ivStop = itemView.findViewById(R.id.ivStop);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvLastApplied = itemView.findViewById(R.id.tvLastApplied);
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
                List<Condition> conditions = new ArrayList<>();
                JSONObject jcondition = new JSONObject(rule.condition);
                if (jcondition.has("sender"))
                    conditions.add(new Condition(context.getString(R.string.title_rule_sender),
                            jcondition.getJSONObject("sender").optString("value"),
                            jcondition.getJSONObject("sender").optBoolean("regex")));
                if (jcondition.has("recipient"))
                    conditions.add(new Condition(context.getString(R.string.title_rule_recipient),
                            jcondition.getJSONObject("recipient").optString("value"),
                            jcondition.getJSONObject("recipient").optBoolean("regex")));
                if (jcondition.has("subject"))
                    conditions.add(new Condition(context.getString(R.string.title_rule_subject),
                            jcondition.getJSONObject("subject").optString("value"),
                            jcondition.getJSONObject("subject").optBoolean("regex")));
                if (jcondition.optBoolean("attachments"))
                    conditions.add(new Condition(context.getString(R.string.title_rule_attachments),
                            null, null));
                if (jcondition.has("header"))
                    conditions.add(new Condition(context.getString(R.string.title_rule_header),
                            jcondition.getJSONObject("header").optString("value"),
                            jcondition.getJSONObject("header").optBoolean("regex")));
                if (jcondition.has("body"))
                    conditions.add(new Condition(context.getString(R.string.title_rule_body),
                            jcondition.getJSONObject("body").optString("value"),
                            jcondition.getJSONObject("body").optBoolean("regex")));
                if (jcondition.has("date"))
                    conditions.add(new Condition(context.getString(R.string.title_rule_time_abs),
                            null, null));
                if (jcondition.has("schedule"))
                    conditions.add(new Condition(context.getString(R.string.title_rule_time_rel),
                            null, null));

                SpannableStringBuilder ssb = new SpannableStringBuilderEx();
                for (Condition condition : conditions) {
                    if (ssb.length() > 0)
                        ssb.append("\n");
                    ssb.append(condition.name);
                    if (!TextUtils.isEmpty(condition.condition)) {
                        ssb.append(" \"");
                        int start = ssb.length();
                        ssb.append(condition.condition);
                        ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, ssb.length(), 0);
                        ssb.append("\"");
                        if (Boolean.TRUE.equals(condition.regex))
                            ssb.append(" (*)");
                    }
                }

                tvCondition.setText(ssb);
            } catch (Throwable ex) {
                tvCondition.setText(ex.getMessage());
            }

            try {
                JSONObject jaction = new JSONObject(rule.action);

                String to = null;
                boolean resend = false;
                int type = jaction.getInt("type");
                if (type == EntityRule.TYPE_SNOOZE) {
                    int duration = jaction.optInt("duration", 0);
                    setAction(getAction(type), Integer.toString(duration));
                } else if (type == EntityRule.TYPE_IMPORTANCE) {
                    int importance = jaction.optInt("value");

                    String value = null;
                    if (importance == EntityMessage.PRIORITIY_LOW)
                        value = context.getString(R.string.title_importance_low);
                    else if (importance == EntityMessage.PRIORITIY_NORMAL)
                        value = context.getString(R.string.title_importance_normal);
                    else if (importance == EntityMessage.PRIORITIY_HIGH)
                        value = context.getString(R.string.title_importance_high);

                    setAction(getAction(type), value);
                } else if (type == EntityRule.TYPE_KEYWORD) {
                    setAction(getAction(type), jaction.optString("keyword"));
                } else if (type == EntityRule.TYPE_ANSWER) {
                    to = jaction.optString("to");
                    if (!TextUtils.isEmpty(to)) {
                        resend = jaction.optBoolean("resend");
                        setAction(resend ? R.string.title_rule_resend : getAction(type), to);
                    }
                } else
                    setAction(getAction(type), null);

                if (type == EntityRule.TYPE_MOVE || type == EntityRule.TYPE_COPY ||
                        (type == EntityRule.TYPE_ANSWER && TextUtils.isEmpty(to))) {
                    Bundle args = new Bundle();
                    args.putLong("id", rule.id);
                    args.putInt("type", type);
                    args.putLong("target", jaction.optLong("target", -1));
                    args.putLong("answer", jaction.optLong("answer", -1));

                    new SimpleTask<String>() {
                        @Override
                        protected String onExecute(Context context, Bundle args) throws Throwable {
                            DB db = DB.getInstance(context);
                            int type = args.getInt("type");
                            if (type == EntityRule.TYPE_MOVE || type == EntityRule.TYPE_COPY) {
                                long id = args.getLong("target");
                                EntityFolder folder = db.folder().getFolder(id);
                                return (folder == null ? null : folder.name);
                            } else if (type == EntityRule.TYPE_ANSWER) {
                                long id = args.getLong("answer");
                                EntityAnswer answer = db.answer().getAnswer(id);
                                return (answer == null ? null : answer.name);
                            } else
                                return null;
                        }

                        @Override
                        protected void onExecuted(Bundle args, String value) {
                            int pos = getAdapterPosition();
                            if (pos == RecyclerView.NO_POSITION)
                                return;

                            long id = args.getLong("id");
                            if (id != AdapterRule.this.getItemId(pos))
                                return;

                            setAction(getAction(args.getInt("type")), value);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            // Ignored
                        }
                    }.execute(context, owner, args, "rule:folder");
                }
            } catch (Throwable ex) {
                tvAction.setText(ex.getMessage());
            }

            tvLastApplied.setText(rule.last_applied == null ? "-" : DF.format(rule.last_applied));
            tvApplied.setText(NF.format(rule.applied));
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleRuleEx rule = selected.get(pos);

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

            final TupleRuleEx rule = selected.get(pos);

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
                    int itemId = item.getItemId();
                    if (itemId == R.string.title_rule_enabled) {
                        onActionEnabled(!item.isChecked());
                        return true;
                    } else if (itemId == R.string.title_rule_execute) {
                        onActionExecute();
                        return true;
                    } else if (itemId == R.string.title_reset) {
                        onActionReset();
                        return true;
                    } else if (itemId == R.string.title_move_to_folder) {
                        onActionMove();
                        return true;
                    } else if (itemId == R.string.title_copy) {
                        onActionCopy();
                        return true;
                    }
                    return false;
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
                        private Toast toast = null;

                        @Override
                        protected void onPreExecute(Bundle args) {
                            toast = ToastEx.makeText(context, R.string.title_executing, Toast.LENGTH_LONG);
                            toast.show();
                        }

                        @Override
                        protected void onPostExecute(Bundle args) {
                            if (toast != null)
                                toast.cancel();
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
                                throw new IllegalArgumentException(context.getString(R.string.title_rule_no_headers));

                            List<Long> ids = db.message().getMessageIdsByFolder(rule.folder);
                            if (ids == null)
                                return 0;

                            int applied = 0;
                            for (long mid : ids)
                                try {
                                    db.beginTransaction();

                                    EntityMessage message = db.message().getMessage(mid);
                                    if (message == null)
                                        continue;

                                    if (rule.matches(context, message, null, null))
                                        if (rule.execute(context, message))
                                            applied++;

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                            if (applied > 0)
                                ServiceSynchronize.eval(context, "rules/manual");

                            return applied;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Integer applied) {
                            ToastEx.makeText(context,
                                    context.getString(R.string.title_rule_applied, applied),
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        protected void onDestroyed(Bundle args) {
                            if (toast != null) {
                                toast.cancel();
                                toast = null;
                            }
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
                    }.execute(context, owner, args, "rule:reset");
                }

                private void onActionMove() {
                    Bundle args = new Bundle();
                    args.putInt("icon", R.drawable.twotone_drive_file_move_24);
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

        private void setAction(int resid, String value) {
            if (TextUtils.isEmpty(value))
                tvAction.setText(resid);
            else {
                SpannableStringBuilder ssb = new SpannableStringBuilderEx();
                ssb.append(context.getString(resid));
                ssb.append(" \"");
                int start = ssb.length();
                ssb.append(value);
                ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, ssb.length(), 0);
                ssb.append("\"");
                tvAction.setText(ssb);
            }
        }

        private int getAction(int type) {
            switch (type) {
                case EntityRule.TYPE_NOOP:
                    return R.string.title_rule_noop;
                case EntityRule.TYPE_SEEN:
                    return R.string.title_rule_seen;
                case EntityRule.TYPE_UNSEEN:
                    return R.string.title_rule_unseen;
                case EntityRule.TYPE_HIDE:
                    return R.string.title_rule_hide;
                case EntityRule.TYPE_IGNORE:
                    return R.string.title_rule_ignore;
                case EntityRule.TYPE_SNOOZE:
                    return R.string.title_rule_snooze;
                case EntityRule.TYPE_FLAG:
                    return R.string.title_rule_flag;
                case EntityRule.TYPE_IMPORTANCE:
                    return R.string.title_rule_importance;
                case EntityRule.TYPE_KEYWORD:
                    return R.string.title_rule_keyword;
                case EntityRule.TYPE_MOVE:
                    return R.string.title_rule_move;
                case EntityRule.TYPE_COPY:
                    return R.string.title_rule_copy;
                case EntityRule.TYPE_ANSWER:
                    return R.string.title_rule_answer;
                case EntityRule.TYPE_TTS:
                    return R.string.title_rule_tts;
                case EntityRule.TYPE_AUTOMATION:
                    return R.string.title_rule_automation;
                case EntityRule.TYPE_DELETE:
                    return R.string.title_rule_delete;
                case EntityRule.TYPE_SOUND:
                    return R.string.title_rule_sound;
                default:
                    throw new IllegalArgumentException("Unknown action type=" + type);
            }
        }

        private class Condition {
            private final String name;
            private final String condition;
            private final Boolean regex;

            Condition(String name, String condition, Boolean regex) {
                this.name = name;
                this.condition = condition;
                this.regex = regex;
            }
        }
    }

    AdapterRule(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        this.DF = Helper.getDateTimeInstance(this.context);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterRule.this + " parent destroyed");
                AdapterRule.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(int protocol, @NonNull List<TupleRuleEx> rules) {
        this.protocol = protocol;
        Log.i("Set protocol=" + protocol + " rules=" + rules.size() + " search=" + search);

        all = rules;

        List<TupleRuleEx> items;
        if (TextUtils.isEmpty(search))
            items = all;
        else {
            items = new ArrayList<>();
            String query = search.toLowerCase().trim();
            for (TupleRuleEx rule : rules)
                if (rule.matches(query))
                    items.add(rule);
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
        }
    }

    public void search(String query) {
        Log.i("Rules query=" + query);
        search = query;
        set(protocol, all);
    }

    private static class DiffCallback extends DiffUtil.Callback {
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
        return selected.get(position).id;
    }

    @Override
    public int getItemCount() {
        return selected.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_rule, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TupleRuleEx rule = selected.get(position);
        holder.powner.recreate(rule == null ? null : rule.id);

        holder.unwire();
        holder.bindTo(rule);
        holder.wire();
    }
}
