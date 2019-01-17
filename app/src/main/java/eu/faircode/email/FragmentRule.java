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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;

public class FragmentRule extends FragmentBase {
    private ViewGroup view;
    private EditText etName;
    private Spinner spAccount;
    private Spinner spFolder;
    private EditText etOrder;
    private EditText etSender;
    private EditText etSubject;
    private EditText etText;
    private Spinner spAction;
    private Spinner spMove;
    private CheckBox cbMoveSeen;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private Group grpMove;

    private ArrayAdapter<EntityAccount> adapterAccount;
    private ArrayAdapter<EntityFolder> adapterFolder;
    private ArrayAdapter<Action> adapterAction;

    private long id = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        id = (args == null ? -1 : args.getLong("id", -1));
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_rule, container, false);

        // Get controls
        etName = view.findViewById(R.id.etName);
        spAccount = view.findViewById(R.id.spAccount);
        spFolder = view.findViewById(R.id.spFolder);
        etOrder = view.findViewById(R.id.etOrder);
        etSender = view.findViewById(R.id.etSender);
        etSubject = view.findViewById(R.id.etSubject);
        etText = view.findViewById(R.id.etText);
        spAction = view.findViewById(R.id.spAction);
        spMove = view.findViewById(R.id.spMove);
        cbMoveSeen = view.findViewById(R.id.cbMoveSeen);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        grpMove = view.findViewById(R.id.grpMove);

        adapterAccount = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityAccount>());
        adapterAccount.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAccount.setAdapter(adapterAccount);

        adapterFolder = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityFolder>());
        adapterFolder.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spFolder.setAdapter(adapterFolder);

        adapterAction = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<Action>());
        adapterAction.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAction.setAdapter(adapterAction);

        List<Action> actions = new ArrayList<>();
        actions.add(new Action(EntityRule.TYPE_SEEN, getString(R.string.title_seen)));
        actions.add(new Action(EntityRule.TYPE_UNSEEN, getString(R.string.title_unseen)));
        //actions.add(new Action(EntityRule.TYPE_MOVE, getString(R.string.title_move)));
        adapterAction.addAll(actions);

        spAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                EntityAccount account = (EntityAccount) adapterView.getAdapter().getItem(position);
                onAccountSelected(account.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                onAccountSelected(-1);
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_delete:
                        onActionTrash();
                        return true;
                    case R.id.action_save:
                        onActionSave();
                        return true;
                    default:
                        return false;
                }
            }
        });

        ((ActivityBase) getActivity()).addBackPressedListener(onBackPressedListener);

        // Initialize
        bottom_navigation.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        grpMove.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onDestroyView() {
        ((ActivityBase) getActivity()).removeBackPressedListener(onBackPressedListener);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new SimpleTask<List<EntityAccount>>() {
            @Override
            protected List<EntityAccount> onExecute(Context context, Bundle args) {
                return DB.getInstance(context).account().getAccounts(true);
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                if (accounts == null)
                    accounts = new ArrayList<>();

                adapterAccount.addAll(accounts);

                Bundle rargs = new Bundle();
                rargs.putLong("id", id);

                new SimpleTask<TupleRuleEx>() {
                    @Override
                    protected TupleRuleEx onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        return DB.getInstance(context).rule().getRule(id);
                    }

                    @Override
                    protected void onExecuted(Bundle args, TupleRuleEx rule) {
                        try {
                            JSONObject jcondition = (rule == null ? new JSONObject() : new JSONObject(rule.condition));
                            JSONObject jaction = (rule == null ? new JSONObject() : new JSONObject(rule.action));

                            etName.setText(rule == null ? null : rule.name);
                            etOrder.setText(rule == null ? null : Integer.toString(rule.order));
                            etSender.setText(jcondition.optString("sender"));
                            etSubject.setText(jcondition.optString("subject"));
                            etText.setText(jcondition.optString("text"));

                            int type = jaction.optInt("type", -1);
                            for (int pos = 0; pos < adapterAction.getCount(); pos++)
                                if (adapterAction.getItem(pos).type == type) {
                                    spAction.setSelection(pos);
                                    break;
                                }

                            bottom_navigation.findViewById(R.id.action_delete).setVisibility(rule == null ? View.GONE : View.VISIBLE);

                            if (rule == null) {
                                grpReady.setVisibility(View.VISIBLE);
                                bottom_navigation.setVisibility(View.VISIBLE);
                                pbWait.setVisibility(View.GONE);
                            } else {
                                spAccount.setTag(rule.account);
                                spFolder.setTag(rule.folder);

                                for (int pos = 0; pos < adapterAccount.getCount(); pos++)
                                    if (adapterAccount.getItem(pos).id.equals(rule.account)) {
                                        spAccount.setSelection(pos);
                                        onAccountSelected(rule.account);
                                        break;
                                    }
                            }
                        } catch (JSONException ex) {
                            Log.e(ex);
                        }
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(FragmentRule.this, rargs, "rule:get");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, null, "rule:accounts");
    }

    private void onAccountSelected(long account) {
        Bundle args = new Bundle();
        args.putLong("account", account);

        new SimpleTask<List<EntityFolder>>() {
            @Override
            protected List<EntityFolder> onExecute(Context context, Bundle args) {
                long account = args.getLong("account");
                return DB.getInstance(context).folder().getFolders(account);
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityFolder> folders) {
                adapterFolder.clear();
                adapterFolder.addAll(folders);

                long account = args.getLong("account");
                if (account == (Long) spAccount.getTag()) {
                    Long folder = (Long) spFolder.getTag();
                    for (int pos = 0; pos < folders.size(); pos++)
                        if (folders.get(pos).id.equals(folder)) {
                            spFolder.setSelection(pos);
                            break;
                        }
                } else
                    spFolder.setSelection(0);

                grpReady.setVisibility(View.VISIBLE);
                bottom_navigation.setVisibility(View.VISIBLE);
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentRule.this, args, "rule:folders");
    }

    private void onActionTrash() {
        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setMessage(R.string.title_ask_delete_rule)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putLong("id", id);

                        new SimpleTask<Void>() {
                            @Override
                            protected void onPreExecute(Bundle args) {
                                Helper.setViewsEnabled(view, false);
                            }

                            @Override
                            protected void onPostExecute(Bundle args) {
                                Helper.setViewsEnabled(view, true);
                            }

                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                long id = args.getLong("id");
                                DB.getInstance(context).rule().deleteRule(id);
                                return null;
                            }

                            @Override
                            protected void onExecuted(Bundle args, Void data) {
                                finish();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentRule.this, args, "rule:delete");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onActionSave() {
        try {
            Helper.setViewsEnabled(view, false);

            EntityFolder folder = (EntityFolder) spFolder.getSelectedItem();

            String sender = etSender.getText().toString();
            String subject = etSubject.getText().toString();
            String text = etText.getText().toString();

            JSONObject jcondition = new JSONObject();
            if (!TextUtils.isEmpty(sender))
                jcondition.put("sender", sender);
            if (!TextUtils.isEmpty(subject))
                jcondition.put("subject", subject);
            if (!TextUtils.isEmpty(text))
                jcondition.put("text", text);

            Action action = (Action) spAction.getSelectedItem();

            JSONObject jaction = new JSONObject();
            if (action != null)
                jaction.put("type", action.type);

            Bundle args = new Bundle();
            args.putLong("id", id);
            args.putLong("folder", folder == null ? -1 : folder.id);
            args.putString("name", etName.getText().toString());
            args.putString("order", etOrder.getText().toString());
            args.putString("condition", jcondition.toString());
            args.putString("action", jaction.toString());

            new SimpleTask<Void>() {
                @Override
                protected void onPreExecute(Bundle args) {
                    Helper.setViewsEnabled(view, false);
                }

                @Override
                protected void onPostExecute(Bundle args) {
                    Helper.setViewsEnabled(view, true);
                }

                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    long folder = args.getLong("folder");
                    String name = args.getString("name");
                    String order = args.getString("order");
                    String condition = args.getString("condition");
                    String action = args.getString("action");

                    if (TextUtils.isEmpty(name))
                        throw new IllegalArgumentException(getString(R.string.title_rule_name_missing));

                    if (TextUtils.isEmpty(order))
                        order = "1";

                    DB db = DB.getInstance(context);
                    if (id < 0) {
                        EntityRule rule = new EntityRule();
                        rule.folder = folder;
                        rule.name = name;
                        rule.order = Integer.parseInt(order);
                        rule.condition = condition;
                        rule.action = action;
                        rule.id = db.rule().insertRule(rule);
                    } else {
                        EntityRule rule = db.rule().getRule(id);
                        rule.folder = folder;
                        rule.name = name;
                        rule.order = Integer.parseInt(order);
                        rule.condition = condition;
                        rule.action = action;
                        db.rule().updateRule(rule);
                    }

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Void data) {
                    finish();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    if (ex instanceof IllegalArgumentException)
                        Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                    else
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                }
            }.execute(this, args, "rule:save");
        } catch (JSONException ex) {
            Log.e(ex);
        }
    }

    private void handleExit() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                    .setMessage(R.string.title_ask_save)
                    .setPositiveButton(R.string.title_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onActionSave();
                        }
                    })
                    .setNegativeButton(R.string.title_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
    }

    ActivityBase.IBackPressedListener onBackPressedListener = new ActivityBase.IBackPressedListener() {
        @Override
        public boolean onBackPressed() {
            handleExit();
            return true;
        }
    };

    private class Action {
        int type;
        String name;

        Action(int type, String name) {
            this.type = type;
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
}
