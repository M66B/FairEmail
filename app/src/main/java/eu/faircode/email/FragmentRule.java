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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentRule extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;
    private ConstraintLayout content;

    private TextView tvFolder;
    private EditText etName;
    private EditText etOrder;
    private CheckBox cbEnabled;
    private CheckBox cbStop;

    private EditText etSender;
    private CheckBox cbSender;
    private ImageView ivSender;

    private EditText etRecipient;
    private CheckBox cbRecipient;
    private ImageView ivRecipient;

    private EditText etSubject;
    private CheckBox cbSubject;

    private EditText etHeader;
    private CheckBox cbHeader;

    private Spinner spAction;
    private TextView tvActionRemark;
    private Spinner spTarget;
    private Spinner spIdent;
    private Spinner spAnswer;
    private CheckBox cbCc;
    private TextView tvAutomation;

    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;

    private Group grpReady;
    private Group grpMove;
    private Group grpAnswer;
    private Group grpAutomation;

    private ArrayAdapter<Action> adapterAction;
    private ArrayAdapter<EntityFolder> adapterTarget;
    private ArrayAdapter<EntityIdentity> adapterIdentity;
    private ArrayAdapter<EntityAnswer> adapterAnswer;

    private long id = -1;
    private long account = -1;
    private long folder = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        id = args.getLong("id", -1);
        account = args.getLong("account", -1);
        folder = args.getLong("folder", -1);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_rules);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_rule, container, false);

        // Get controls
        scroll = view.findViewById(R.id.scroll);
        content = view.findViewById(R.id.content);

        tvFolder = view.findViewById(R.id.tvFolder);
        etName = view.findViewById(R.id.etName);
        etOrder = view.findViewById(R.id.etOrder);
        cbEnabled = view.findViewById(R.id.cbEnabled);
        cbStop = view.findViewById(R.id.cbStop);

        etSender = view.findViewById(R.id.etSender);
        cbSender = view.findViewById(R.id.cbSender);
        ivSender = view.findViewById(R.id.ivSender);

        etRecipient = view.findViewById(R.id.etRecipient);
        cbRecipient = view.findViewById(R.id.cbRecipient);
        ivRecipient = view.findViewById(R.id.ivRecipient);

        etSubject = view.findViewById(R.id.etSubject);
        cbSubject = view.findViewById(R.id.cbSubject);

        etHeader = view.findViewById(R.id.etHeader);
        cbHeader = view.findViewById(R.id.cbHeader);

        spAction = view.findViewById(R.id.spAction);
        tvActionRemark = view.findViewById(R.id.tvActionRemark);
        spTarget = view.findViewById(R.id.spTarget);
        spIdent = view.findViewById(R.id.spIdent);
        spAnswer = view.findViewById(R.id.spAnswer);
        cbCc = view.findViewById(R.id.cbCc);
        tvAutomation = view.findViewById(R.id.tvAutomation);

        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);

        grpReady = view.findViewById(R.id.grpReady);
        grpMove = view.findViewById(R.id.grpMove);
        grpAnswer = view.findViewById(R.id.grpAnswer);
        grpAutomation = view.findViewById(R.id.grpAutomation);

        ivSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pick = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                if (pick.resolveActivity(getContext().getPackageManager()) == null)
                    Snackbar.make(view, R.string.title_no_contacts, Snackbar.LENGTH_LONG).show();
                else
                    startActivityForResult(Helper.getChooser(getContext(), pick), ActivityView.REQUEST_SENDER);
            }
        });

        ivRecipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pick = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                if (pick.resolveActivity(getContext().getPackageManager()) == null)
                    Snackbar.make(view, R.string.title_no_contacts, Snackbar.LENGTH_LONG).show();
                else
                    startActivityForResult(Helper.getChooser(getContext(), pick), ActivityView.REQUEST_RECIPIENT);
            }
        });

        adapterAction = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<Action>());
        adapterAction.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAction.setAdapter(adapterAction);

        adapterTarget = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityFolder>());
        adapterTarget.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spTarget.setAdapter(adapterTarget);

        adapterIdentity = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityIdentity>());
        adapterIdentity.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spIdent.setAdapter(adapterIdentity);

        adapterAnswer = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityAnswer>());
        adapterAnswer.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAnswer.setAdapter(adapterAnswer);

        List<Action> actions = new ArrayList<>();
        actions.add(new Action(EntityRule.TYPE_SEEN, getString(R.string.title_seen)));
        actions.add(new Action(EntityRule.TYPE_UNSEEN, getString(R.string.title_unseen)));
        actions.add(new Action(EntityRule.TYPE_MOVE, getString(R.string.title_move)));
        actions.add(new Action(EntityRule.TYPE_ANSWER, getString(R.string.title_answer_reply)));
        actions.add(new Action(EntityRule.TYPE_AUTOMATION, getString(R.string.title_rule_automation)));
        adapterAction.addAll(actions);

        spAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Integer prev = (Integer) adapterView.getTag();
                if (prev != null && !prev.equals(position)) {
                    Action action = (Action) adapterView.getAdapter().getItem(position);
                    onActionSelected(action.type);
                }
                adapterView.setTag(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                onActionSelected(-1);
            }

            private void onActionSelected(int type) {
                showActionParameters(type);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.smoothScrollTo(0, content.getBottom());
                    }
                });
            }
        });

        tvActionRemark.setVisibility(View.GONE);

        tvAutomation.setText(getString(R.string.title_rule_automation_hint,
                EntityRule.ACTION_AUTOMATION,
                TextUtils.join(",", new String[]{
                        EntityRule.EXTRA_RULE,
                        EntityRule.EXTRA_SENDER,
                        EntityRule.EXTRA_SUBJECT})));

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
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

        // Initialize
        tvFolder.setText(null);
        bottom_navigation.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        grpMove.setVisibility(View.GONE);
        grpAnswer.setVisibility(View.GONE);
        grpAutomation.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLong("folder", folder);

        new SimpleTask<RefData>() {
            @Override
            protected RefData onExecute(Context context, Bundle args) {
                long aid = args.getLong("account");
                long fid = args.getLong("folder");

                RefData data = new RefData();

                DB db = DB.getInstance(context);
                data.folder = db.folder().getFolder(fid);
                data.folders = db.folder().getFolders(aid);

                if (data.folders == null)
                    data.folders = new ArrayList<>();

                for (EntityFolder folder : data.folders)
                    folder.display = folder.getDisplayName(context);
                EntityFolder.sort(context, data.folders, true);

                data.identities = db.identity().getIdentities(aid);
                data.answers = db.answer().getAnswers(false);

                return data;
            }

            @Override
            protected void onExecuted(Bundle args, RefData data) {
                tvFolder.setText(data.folder.getDisplayName(getContext()));

                adapterTarget.clear();
                adapterTarget.addAll(data.folders);

                adapterIdentity.clear();
                adapterIdentity.addAll(data.identities);

                adapterAnswer.clear();
                adapterAnswer.addAll(data.answers);

                tvActionRemark.setText(
                        getString(R.string.title_rule_action_remark, data.folder.getDisplayName(getContext())));
                tvActionRemark.setVisibility(View.VISIBLE);

                loadRule();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "rule:accounts");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Request=" + requestCode + " result=" + resultCode + " data=" + data);
        if (resultCode == RESULT_OK) {
            if (data != null && requestCode == ActivityView.REQUEST_SENDER)
                handlePickContact(data, true);
            if (data != null && requestCode == ActivityView.REQUEST_RECIPIENT)
                handlePickContact(data, false);
        }
    }

    private void handlePickContact(Intent data, boolean sender) {
        Uri uri = data.getData();
        if (uri == null) return;
        try (Cursor cursor = getContext().getContentResolver().query(uri,
                new String[]{
                        ContactsContract.CommonDataKinds.Email.ADDRESS
                },
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst())
                if (sender)
                    etSender.setText(cursor.getString(0));
                else
                    etRecipient.setText(cursor.getString(0));
        } catch (Throwable ex) {
            Log.e(ex);
            Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
        }
    }

    private void loadRule() {
        Bundle rargs = new Bundle();
        rargs.putLong("id", id);
        rargs.putString("sender", getArguments().getString("sender"));
        rargs.putString("subject", getArguments().getString("subject"));

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

                    JSONObject jsender = jcondition.optJSONObject("sender");
                    JSONObject jrecipient = jcondition.optJSONObject("recipient");
                    JSONObject jsubject = jcondition.optJSONObject("subject");
                    JSONObject jheader = jcondition.optJSONObject("header");

                    etName.setText(rule == null ? null : rule.name);
                    etOrder.setText(rule == null ? null : Integer.toString(rule.order));
                    cbEnabled.setChecked(rule == null || rule.enabled);
                    cbStop.setChecked(rule != null && rule.stop);

                    etSender.setText(jsender == null ? args.getString("sender") : jsender.getString("value"));
                    cbSender.setChecked(jsender != null && jsender.getBoolean("regex"));

                    etRecipient.setText(jrecipient == null ? null : jrecipient.getString("value"));
                    cbRecipient.setChecked(jrecipient != null && jrecipient.getBoolean("regex"));

                    etSubject.setText(jsubject == null ? args.getString("subject") : jsubject.getString("value"));
                    cbSubject.setChecked(jsubject != null && jsubject.getBoolean("regex"));

                    etHeader.setText(jheader == null ? null : jheader.getString("value"));
                    cbHeader.setChecked(jheader != null && jheader.getBoolean("regex"));

                    if (rule == null) {
                        for (int pos = 0; pos < adapterIdentity.getCount(); pos++)
                            if (adapterIdentity.getItem(pos).primary) {
                                spIdent.setSelection(pos);
                                break;
                            }
                    } else {
                        int type = jaction.getInt("type");
                        switch (type) {
                            case EntityRule.TYPE_MOVE:
                                long target = jaction.getLong("target");
                                for (int pos = 0; pos < adapterTarget.getCount(); pos++)
                                    if (adapterTarget.getItem(pos).id.equals(target)) {
                                        spTarget.setSelection(pos);
                                        break;
                                    }
                                break;

                            case EntityRule.TYPE_ANSWER:
                                long identity = jaction.getLong("identity");
                                for (int pos = 0; pos < adapterIdentity.getCount(); pos++)
                                    if (adapterIdentity.getItem(pos).id.equals(identity)) {
                                        spIdent.setSelection(pos);
                                        break;
                                    }

                                long answer = jaction.getLong("answer");
                                for (int pos = 0; pos < adapterAnswer.getCount(); pos++)
                                    if (adapterAnswer.getItem(pos).id.equals(answer)) {
                                        spAnswer.setSelection(pos);
                                        break;
                                    }

                                boolean cc = (jaction.has("cc") && jaction.getBoolean("cc"));
                                cbCc.setChecked(cc);
                                break;
                        }

                        for (int pos = 0; pos < adapterAction.getCount(); pos++)
                            if (adapterAction.getItem(pos).type == type) {
                                spAction.setTag(pos);
                                spAction.setSelection(pos);
                                break;
                            }

                        showActionParameters(type);
                    }

                    grpReady.setVisibility(View.VISIBLE);
                    bottom_navigation.findViewById(R.id.action_delete).setVisibility(rule == null ? View.GONE : View.VISIBLE);
                    bottom_navigation.setVisibility(View.VISIBLE);
                    pbWait.setVisibility(View.GONE);
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
        if (!Helper.isPro(getContext())) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
            fragmentTransaction.commit();
            return;
        }

        try {
            Helper.setViewsEnabled(view, false);

            Bundle args = new Bundle();
            args.putLong("id", id);
            args.putLong("folder", folder);
            args.putString("name", etName.getText().toString());
            args.putString("order", etOrder.getText().toString());
            args.putBoolean("enabled", cbEnabled.isChecked());
            args.putBoolean("stop", cbStop.isChecked());
            args.putString("condition", getCondition().toString());
            args.putString("action", getAction().toString());

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
                protected Void onExecute(Context context, Bundle args) throws JSONException {
                    long id = args.getLong("id");
                    long folder = args.getLong("folder");
                    String name = args.getString("name");
                    String order = args.getString("order");
                    boolean enabled = args.getBoolean("enabled");
                    boolean stop = args.getBoolean("stop");
                    String condition = args.getString("condition");
                    String action = args.getString("action");

                    if (TextUtils.isEmpty(name))
                        throw new IllegalArgumentException(context.getString(R.string.title_rule_name_missing));

                    JSONObject jcondition = new JSONObject(condition);
                    JSONObject jsender = jcondition.optJSONObject("sender");
                    JSONObject jrecipient = jcondition.optJSONObject("recipient");
                    JSONObject jsubject = jcondition.optJSONObject("subject");
                    JSONObject jheader = jcondition.optJSONObject("header");

                    if (jsender == null && jrecipient == null && jsubject == null && jheader == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_rule_condition_missing));

                    if (TextUtils.isEmpty(order))
                        order = "1";

                    DB db = DB.getInstance(context);
                    if (id < 0) {
                        EntityRule rule = new EntityRule();
                        rule.folder = folder;
                        rule.name = name;
                        rule.order = Integer.parseInt(order);
                        rule.enabled = enabled;
                        rule.stop = stop;
                        rule.condition = condition;
                        rule.action = action;
                        rule.id = db.rule().insertRule(rule);
                    } else {
                        EntityRule rule = db.rule().getRule(id);
                        rule.folder = folder;
                        rule.name = name;
                        rule.order = Integer.parseInt(order);
                        rule.enabled = enabled;
                        rule.stop = stop;
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

    private void showActionParameters(int type) {
        grpMove.setVisibility(type == EntityRule.TYPE_MOVE ? View.VISIBLE : View.GONE);
        grpAnswer.setVisibility(type == EntityRule.TYPE_ANSWER ? View.VISIBLE : View.GONE);
        grpAutomation.setVisibility(type == EntityRule.TYPE_AUTOMATION ? View.VISIBLE : View.GONE);
    }

    private JSONObject getCondition() throws JSONException {
        JSONObject jcondition = new JSONObject();

        String sender = etSender.getText().toString();
        if (!TextUtils.isEmpty(sender)) {
            JSONObject jsender = new JSONObject();
            jsender.put("value", sender);
            jsender.put("regex", cbSender.isChecked());
            jcondition.put("sender", jsender);
        }

        String recipient = etRecipient.getText().toString();
        if (!TextUtils.isEmpty(recipient)) {
            JSONObject jrecipient = new JSONObject();
            jrecipient.put("value", recipient);
            jrecipient.put("regex", cbRecipient.isChecked());
            jcondition.put("recipient", jrecipient);
        }

        String subject = etSubject.getText().toString();
        if (!TextUtils.isEmpty(subject)) {
            JSONObject jsubject = new JSONObject();
            jsubject.put("value", subject);
            jsubject.put("regex", cbSubject.isChecked());
            jcondition.put("subject", jsubject);
        }

        String header = etHeader.getText().toString();
        if (!TextUtils.isEmpty(header)) {
            JSONObject jheader = new JSONObject();
            jheader.put("value", header);
            jheader.put("regex", cbHeader.isChecked());
            jcondition.put("header", jheader);
        }

        return jcondition;
    }

    private JSONObject getAction() throws JSONException {
        JSONObject jaction = new JSONObject();

        Action action = (Action) spAction.getSelectedItem();
        if (action != null) {
            jaction.put("type", action.type);
            switch (action.type) {
                case EntityRule.TYPE_MOVE:
                    EntityFolder target = (EntityFolder) spTarget.getSelectedItem();
                    jaction.put("target", target.id);
                    break;

                case EntityRule.TYPE_ANSWER:
                    EntityIdentity identity = (EntityIdentity) spIdent.getSelectedItem();
                    EntityAnswer answer = (EntityAnswer) spAnswer.getSelectedItem();
                    boolean cc = cbCc.isChecked();
                    jaction.put("identity", identity.id);
                    jaction.put("answer", answer.id);
                    jaction.put("cc", cc);
                    break;
            }
        }

        return jaction;
    }

    private class RefData {
        EntityFolder folder;
        List<EntityFolder> folders;
        List<EntityIdentity> identities;
        List<EntityAnswer> answers;
    }

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
