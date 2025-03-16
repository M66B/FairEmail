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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FragmentDialogRuleCheck extends FragmentDialogBase {
    private final static int MAX_CHECK = 10;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        long folder = getArguments().getLong("folder");
        String name = getArguments().getString("name");
        boolean daily = getArguments().getBoolean("daily");
        String condition = getArguments().getString("condition");
        String action = getArguments().getString("action");

        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rule_match, null);
        final TextView tvNoMessages = dview.findViewById(R.id.tvNoMessages);
        final RecyclerView rvMessage = dview.findViewById(R.id.rvMessage);
        final Button btnExecute = dview.findViewById(R.id.btnExecute);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

        rvMessage.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvMessage.setLayoutManager(llm);

        final AdapterRuleMatch adapter = new AdapterRuleMatch(getContext(), getViewLifecycleOwner());
        rvMessage.setAdapter(adapter);

        tvNoMessages.setVisibility(View.GONE);
        rvMessage.setVisibility(View.GONE);
        btnExecute.setVisibility(View.GONE);

        final Bundle args = new Bundle();
        args.putLong("folder", folder);
        args.putString("name", name);
        args.putBoolean("daily", daily);
        args.putString("condition", condition);
        args.putString("action", action);

        btnExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<Integer>() {
                    private Toast toast = null;

                    @Override
                    protected void onPreExecute(Bundle args) {
                        toast = ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG);
                        toast.show();
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        if (toast != null)
                            toast.cancel();
                    }

                    @Override
                    protected Integer onExecute(Context context, Bundle args) throws Throwable {
                        EntityRule rule = new EntityRule();
                        rule.name = args.getString("name");
                        rule.folder = args.getLong("folder");
                        rule.daily = args.getBoolean("daily");
                        rule.condition = args.getString("condition");
                        rule.action = args.getString("action");

                        int applied = 0;

                        DB db = DB.getInstance(context);
                        List<Long> ids =
                                db.message().getMessageIdsByFolder(rule.folder);
                        for (long mid : ids)
                            try {
                                db.beginTransaction();

                                EntityMessage message = db.message().getMessage(mid);
                                if (message == null || message.ui_hide)
                                    continue;

                                if (rule.matches(context, message, null, null))
                                    if (rule.execute(context, message, false, null))
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
                        dismiss();
                        ToastEx.makeText(getContext(), getString(R.string.title_rule_applied, applied), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        boolean report = !(ex instanceof IllegalArgumentException);
                        Log.unexpectedError(getParentFragmentManager(), ex, report, 71);
                    }
                }.execute(FragmentDialogRuleCheck.this, args, "rule:execute");
            }
        });

        new SimpleTask<List<EntityMessage>>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected List<EntityMessage> onExecute(Context context, Bundle args) throws Throwable {
                EntityRule rule = new EntityRule();
                rule.folder = args.getLong("folder");
                rule.daily = args.getBoolean("daily");
                rule.condition = args.getString("condition");
                rule.action = args.getString("action");
                rule.validate(context);

                List<EntityMessage> matching = new ArrayList<>();

                DB db = DB.getInstance(context);
                List<Long> ids =
                        db.message().getMessageIdsByFolder(rule.folder);
                for (long id : ids) {
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        continue;

                    if (rule.matches(context, message, null, null))
                        matching.add(message);

                    if (matching.size() >= MAX_CHECK)
                        break;
                }

                return matching;
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityMessage> messages) {
                adapter.set(messages);

                if (messages.size() > 0) {
                    rvMessage.setVisibility(View.VISIBLE);
                    btnExecute.setVisibility(View.VISIBLE);
                } else
                    tvNoMessages.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException) {
                    tvNoMessages.setText(new ThrowableWrapper(ex).getSafeMessage());
                    tvNoMessages.setVisibility(View.VISIBLE);
                } else
                    Log.unexpectedError(getParentFragmentManager(), ex, 71);
            }
        }.execute(this, args, "rule:check");

        return new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.baseline_mail_outline_24)
                .setTitle(R.string.title_rule_matched)
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
