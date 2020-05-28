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
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
    private ImageView ibSender;
    private CheckBox cbKnownSender;

    private EditText etRecipient;
    private CheckBox cbRecipient;
    private ImageView ibRecipient;

    private EditText etSubject;
    private CheckBox cbSubject;

    private CheckBox cbAttachments;

    private EditText etHeader;
    private CheckBox cbHeader;

    private Spinner spScheduleDayStart;
    private Spinner spScheduleDayEnd;
    private TextView tvScheduleHourStart;
    private TextView tvScheduleHourEnd;

    private Spinner spAction;
    private TextView tvActionRemark;

    private NumberPicker npDuration;
    private CheckBox cbScheduleEnd;
    private CheckBox cbSnoozeSeen;

    private ViewButtonColor btnColor;

    private Spinner spImportance;

    private EditText etKeyword;

    private Spinner spTarget;
    private CheckBox cbMoveSeen;
    private CheckBox cbMoveThread;

    private Spinner spIdent;

    private Spinner spAnswer;
    private CheckBox cbCc;

    private TextView tvAutomation;

    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;

    private Group grpReady;
    private Group grpSnooze;
    private Group grpFlag;
    private Group grpImportance;
    private Group grpKeyword;
    private Group grpMove;
    private Group grpMoveProp;
    private Group grpAnswer;
    private Group grpAutomation;

    private ArrayAdapter<String> adapterDay;
    private ArrayAdapter<Action> adapterAction;
    private ArrayAdapter<AccountFolder> adapterTarget;
    private ArrayAdapter<EntityIdentity> adapterIdentity;
    private ArrayAdapter<EntityAnswer> adapterAnswer;

    private long id = -1;
    private long copy = -1;
    private long account = -1;
    private long folder = -1;

    private final static int MAX_CHECK = 10;

    private static final int REQUEST_SENDER = 1;
    private static final int REQUEST_RECIPIENT = 2;
    private static final int REQUEST_COLOR = 3;
    private final static int REQUEST_DELETE = 4;
    private final static int REQUEST_SCHEDULE_START = 5;
    private final static int REQUEST_SCHEDULE_END = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        if (args.getBoolean("copy"))
            copy = args.getLong("id", -1);
        else
            id = args.getLong("id", -1);
        account = args.getLong("account", -1);
        folder = args.getLong("folder", -1);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_rule_caption);

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
        ibSender = view.findViewById(R.id.ibSender);
        cbKnownSender = view.findViewById(R.id.cbKnownSender);

        etRecipient = view.findViewById(R.id.etRecipient);
        cbRecipient = view.findViewById(R.id.cbRecipient);
        ibRecipient = view.findViewById(R.id.ibRecipient);

        etSubject = view.findViewById(R.id.etSubject);
        cbSubject = view.findViewById(R.id.cbSubject);

        cbAttachments = view.findViewById(R.id.cbAttachments);

        etHeader = view.findViewById(R.id.etHeader);
        cbHeader = view.findViewById(R.id.cbHeader);

        spScheduleDayStart = view.findViewById(R.id.spScheduleDayStart);
        spScheduleDayEnd = view.findViewById(R.id.spScheduleDayEnd);
        tvScheduleHourStart = view.findViewById(R.id.tvScheduleHourStart);
        tvScheduleHourEnd = view.findViewById(R.id.tvScheduleHourEnd);

        spAction = view.findViewById(R.id.spAction);
        tvActionRemark = view.findViewById(R.id.tvActionRemark);

        npDuration = view.findViewById(R.id.npDuration);
        cbScheduleEnd = view.findViewById(R.id.cbScheduleEnd);
        cbSnoozeSeen = view.findViewById(R.id.cbSnoozeSeen);

        btnColor = view.findViewById(R.id.btnColor);

        spImportance = view.findViewById(R.id.spImportance);

        etKeyword = view.findViewById(R.id.etKeyword);

        spTarget = view.findViewById(R.id.spTarget);
        cbMoveSeen = view.findViewById(R.id.cbMoveSeen);
        cbMoveThread = view.findViewById(R.id.cbMoveThread);

        spIdent = view.findViewById(R.id.spIdent);

        spAnswer = view.findViewById(R.id.spAnswer);
        cbCc = view.findViewById(R.id.cbCc);

        tvAutomation = view.findViewById(R.id.tvAutomation);

        bottom_navigation = view.findViewById(R.id.bottom_navigation);

        pbWait = view.findViewById(R.id.pbWait);

        grpReady = view.findViewById(R.id.grpReady);
        grpSnooze = view.findViewById(R.id.grpSnooze);
        grpFlag = view.findViewById(R.id.grpFlag);
        grpImportance = view.findViewById(R.id.grpImportance);
        grpKeyword = view.findViewById(R.id.grpKeyword);
        grpMove = view.findViewById(R.id.grpMove);
        grpMoveProp = view.findViewById(R.id.grpMoveProp);
        grpAnswer = view.findViewById(R.id.grpAnswer);
        grpAutomation = view.findViewById(R.id.grpAutomation);

        ibSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pick = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                if (pick.resolveActivity(getContext().getPackageManager()) == null)
                    Snackbar.make(view, R.string.title_no_contacts, Snackbar.LENGTH_LONG).show();
                else
                    startActivityForResult(Helper.getChooser(getContext(), pick), REQUEST_SENDER);
            }
        });

        cbKnownSender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etSender.setEnabled(!isChecked);
                ibSender.setEnabled(!isChecked);
                cbSender.setEnabled(!isChecked);
            }
        });

        ibRecipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pick = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                if (pick.resolveActivity(getContext().getPackageManager()) == null)
                    Snackbar.make(view, R.string.title_no_contacts, Snackbar.LENGTH_LONG).show();
                else
                    startActivityForResult(Helper.getChooser(getContext(), pick), REQUEST_RECIPIENT);
            }
        });

        adapterDay = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<String>());
        adapterDay.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spScheduleDayStart.setAdapter(adapterDay);
        spScheduleDayEnd.setAdapter(adapterDay);

        adapterAction = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<Action>());
        adapterAction.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAction.setAdapter(adapterAction);

        adapterTarget = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<AccountFolder>());
        adapterTarget.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spTarget.setAdapter(adapterTarget);

        adapterIdentity = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityIdentity>());
        adapterIdentity.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spIdent.setAdapter(adapterIdentity);

        adapterAnswer = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityAnswer>());
        adapterAnswer.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAnswer.setAdapter(adapterAnswer);

        String[] dayNames = DateFormatSymbols.getInstance().getWeekdays();
        for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++)
            adapterDay.add(dayNames[day]);

        tvScheduleHourStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object time = v.getTag();
                Bundle args = new Bundle();
                args.putLong("minutes", time == null ? 0 : (int) time);
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.setArguments(args);
                timePicker.setTargetFragment(FragmentRule.this, REQUEST_SCHEDULE_START);
                timePicker.show(getParentFragmentManager(), "timePicker");
            }
        });

        tvScheduleHourEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object time = v.getTag();
                Bundle args = new Bundle();
                args.putLong("minutes", time == null ? 0 : (int) time);
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.setArguments(args);
                timePicker.setTargetFragment(FragmentRule.this, REQUEST_SCHEDULE_END);
                timePicker.show(getParentFragmentManager(), "timePicker");
            }
        });

        List<Action> actions = new ArrayList<>();
        actions.add(new Action(EntityRule.TYPE_NOOP, getString(R.string.title_rule_noop)));
        actions.add(new Action(EntityRule.TYPE_SEEN, getString(R.string.title_rule_seen)));
        actions.add(new Action(EntityRule.TYPE_UNSEEN, getString(R.string.title_rule_unseen)));
        actions.add(new Action(EntityRule.TYPE_HIDE, getString(R.string.title_rule_hide)));
        actions.add(new Action(EntityRule.TYPE_IGNORE, getString(R.string.title_rule_ignore)));
        actions.add(new Action(EntityRule.TYPE_SNOOZE, getString(R.string.title_rule_snooze)));
        actions.add(new Action(EntityRule.TYPE_FLAG, getString(R.string.title_rule_flag)));
        actions.add(new Action(EntityRule.TYPE_IMPORTANCE, getString(R.string.title_rule_importance)));
        actions.add(new Action(EntityRule.TYPE_KEYWORD, getString(R.string.title_rule_keyword)));
        actions.add(new Action(EntityRule.TYPE_MOVE, getString(R.string.title_rule_move)));
        actions.add(new Action(EntityRule.TYPE_COPY, getString(R.string.title_rule_copy)));
        actions.add(new Action(EntityRule.TYPE_ANSWER, getString(R.string.title_rule_answer)));
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

        npDuration.setMinValue(0);
        npDuration.setMaxValue(999);

        tvActionRemark.setVisibility(View.GONE);

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("color", btnColor.getColor());
                args.putString("title", getString(R.string.title_flag_color));
                args.putBoolean("reset", true);

                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentRule.this, REQUEST_COLOR);
                fragment.show(getParentFragmentManager(), "rule:color");
            }
        });

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
                        onActionDelete();
                        return true;
                    case R.id.action_check:
                        onActionCheck();
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
        grpSnooze.setVisibility(View.GONE);
        grpFlag.setVisibility(View.GONE);
        grpImportance.setVisibility(View.GONE);
        grpKeyword.setVisibility(View.GONE);
        grpMove.setVisibility(View.GONE);
        grpMoveProp.setVisibility(View.GONE);
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

                data.folders = new ArrayList<>();
                List<EntityAccount> accounts = db.account().getSynchronizingAccounts();
                if (accounts != null)
                    for (EntityAccount account : accounts) {
                        List<EntityFolder> folders = db.folder().getFolders(account.id, true, true);
                        if (folders != null) {
                            if (folders.size() > 0)
                                Collections.sort(folders, folders.get(0).getComparator(null));
                            for (EntityFolder folder : folders)
                                data.folders.add(new AccountFolder(account, folder));
                        }
                    }

                data.identities = db.identity().getSynchronizingIdentities(aid);
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
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "rule:accounts");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_SENDER:
                    if (resultCode == RESULT_OK && data != null)
                        onPickContact(data, true);
                    break;
                case REQUEST_RECIPIENT:
                    if (resultCode == RESULT_OK && data != null)
                        onPickContact(data, true);
                    break;
                case REQUEST_COLOR:
                    if (resultCode == RESULT_OK && data != null) {
                        if (!ActivityBilling.isPro(getContext())) {
                            startActivity(new Intent(getContext(), ActivityBilling.class));
                            return;
                        }

                        Bundle args = data.getBundleExtra("args");
                        btnColor.setColor(args.getInt("color"));
                    }
                    break;
                case REQUEST_DELETE:
                    if (resultCode == RESULT_OK)
                        onDelete();
                    break;
                case REQUEST_SCHEDULE_START:
                    if (resultCode == RESULT_OK)
                        onScheduleStart(data);
                    break;
                case REQUEST_SCHEDULE_END:
                    if (resultCode == RESULT_OK)
                        onScheduleEnd(data);
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onPickContact(Intent data, boolean sender) {
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
            Log.unexpectedError(getParentFragmentManager(), ex);
        }
    }

    private void onDelete() {
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
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "rule:delete");
    }

    private void onScheduleStart(Intent data) {
        int minutes = data.getIntExtra("minutes", 0);
        tvScheduleHourStart.setTag(minutes);
        tvScheduleHourStart.setText(formatHour(getContext(), minutes));
        cbScheduleEnd.setChecked(true);
    }

    private void onScheduleEnd(Intent data) {
        int minutes = data.getIntExtra("minutes", 0);
        tvScheduleHourEnd.setTag(minutes);
        tvScheduleHourEnd.setText(formatHour(getContext(), minutes));
        cbScheduleEnd.setChecked(true);
    }

    private void loadRule() {
        Bundle rargs = new Bundle();
        rargs.putLong("id", copy < 0 ? id : copy);
        rargs.putString("sender", getArguments().getString("sender"));
        rargs.putString("recipient", getArguments().getString("recipient"));
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
                    JSONObject jschedule = jcondition.optJSONObject("schedule");

                    etName.setText(rule == null ? args.getString("subject") : rule.name);
                    etOrder.setText(rule == null ? null : Integer.toString(rule.order));
                    cbEnabled.setChecked(rule == null || rule.enabled);
                    cbStop.setChecked(rule != null && rule.stop);

                    etSender.setText(jsender == null ? args.getString("sender") : jsender.getString("value"));
                    cbSender.setChecked(jsender != null && jsender.getBoolean("regex"));
                    cbKnownSender.setChecked(jsender != null && jsender.optBoolean("known"));
                    etSender.setEnabled(!cbKnownSender.isChecked());
                    ibSender.setEnabled(!cbKnownSender.isChecked());
                    cbSender.setEnabled(!cbKnownSender.isChecked());

                    etRecipient.setText(jrecipient == null ? args.getString("recipient") : jrecipient.getString("value"));
                    cbRecipient.setChecked(jrecipient != null && jrecipient.getBoolean("regex"));

                    etSubject.setText(jsubject == null ? args.getString("subject") : jsubject.getString("value"));
                    cbSubject.setChecked(jsubject != null && jsubject.getBoolean("regex"));

                    cbAttachments.setChecked(jcondition.optBoolean("attachments"));

                    etHeader.setText(jheader == null ? null : jheader.getString("value"));
                    cbHeader.setChecked(jheader != null && jheader.getBoolean("regex"));

                    int start = (jschedule != null && jschedule.has("start") ? jschedule.getInt("start") : 0);
                    int end = (jschedule != null && jschedule.has("end") ? jschedule.getInt("end") : 0);

                    spScheduleDayStart.setSelection(start / (24 * 60));
                    spScheduleDayEnd.setSelection(end / (24 * 60));

                    tvScheduleHourStart.setTag(start % (24 * 60));
                    tvScheduleHourStart.setText(formatHour(getContext(), start % (24 * 60)));

                    tvScheduleHourEnd.setTag(end % (24 * 60));
                    tvScheduleHourEnd.setText(formatHour(getContext(), end % (24 * 60)));

                    if (rule == null) {
                        for (int pos = 0; pos < adapterIdentity.getCount(); pos++)
                            if (adapterIdentity.getItem(pos).primary) {
                                spIdent.setSelection(pos);
                                break;
                            }
                    } else {
                        int type = jaction.getInt("type");
                        switch (type) {
                            case EntityRule.TYPE_SNOOZE:
                                npDuration.setValue(jaction.optInt("duration", 0));
                                cbScheduleEnd.setChecked(jaction.optBoolean("schedule_end", false));
                                cbSnoozeSeen.setChecked(jaction.optBoolean("seen", false));
                                break;

                            case EntityRule.TYPE_FLAG:
                                btnColor.setColor(
                                        !jaction.has("color") || jaction.isNull("color")
                                                ? null : jaction.getInt("color"));
                                break;

                            case EntityRule.TYPE_IMPORTANCE:
                                spImportance.setSelection(jaction.optInt("value"));
                                break;

                            case EntityRule.TYPE_KEYWORD:
                                etKeyword.setText(jaction.getString("keyword"));
                                break;

                            case EntityRule.TYPE_MOVE:
                            case EntityRule.TYPE_COPY:
                                long target = jaction.optLong("target", -1);
                                for (int pos = 0; pos < adapterTarget.getCount(); pos++)
                                    if (adapterTarget.getItem(pos).folder.id.equals(target)) {
                                        spTarget.setSelection(pos);
                                        break;
                                    }
                                if (type == EntityRule.TYPE_MOVE) {
                                    cbMoveSeen.setChecked(jaction.optBoolean("seen"));
                                    cbMoveThread.setChecked(jaction.optBoolean("thread"));
                                }
                                break;

                            case EntityRule.TYPE_ANSWER:
                                long identity = jaction.optLong("identity", -1);
                                for (int pos = 0; pos < adapterIdentity.getCount(); pos++)
                                    if (adapterIdentity.getItem(pos).id.equals(identity)) {
                                        spIdent.setSelection(pos);
                                        break;
                                    }

                                long answer = jaction.optLong("answer", -1);
                                for (int pos = 0; pos < adapterAnswer.getCount(); pos++)
                                    if (adapterAnswer.getItem(pos).id.equals(answer)) {
                                        spAnswer.setSelection(pos);
                                        break;
                                    }

                                cbCc.setChecked(jaction.optBoolean("cc"));
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
                } catch (Throwable ex) {
                    Log.e(ex);
                } finally {
                    grpReady.setVisibility(View.VISIBLE);
                    bottom_navigation.findViewById(R.id.action_delete).setVisibility(id < 0 ? View.GONE : View.VISIBLE);
                    bottom_navigation.setVisibility(View.VISIBLE);
                    pbWait.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, rargs, "rule:get");
    }

    private void showActionParameters(int type) {
        grpSnooze.setVisibility(type == EntityRule.TYPE_SNOOZE ? View.VISIBLE : View.GONE);
        grpFlag.setVisibility(type == EntityRule.TYPE_FLAG ? View.VISIBLE : View.GONE);
        grpImportance.setVisibility(type == EntityRule.TYPE_IMPORTANCE ? View.VISIBLE : View.GONE);
        grpKeyword.setVisibility(type == EntityRule.TYPE_KEYWORD ? View.VISIBLE : View.GONE);
        grpMove.setVisibility(type == EntityRule.TYPE_MOVE || type == EntityRule.TYPE_COPY ? View.VISIBLE : View.GONE);
        grpMoveProp.setVisibility(type == EntityRule.TYPE_MOVE ? View.VISIBLE : View.GONE);
        grpAnswer.setVisibility(type == EntityRule.TYPE_ANSWER ? View.VISIBLE : View.GONE);
        grpAutomation.setVisibility(type == EntityRule.TYPE_AUTOMATION ? View.VISIBLE : View.GONE);
    }

    private void onActionDelete() {
        Bundle args = new Bundle();
        args.putString("question", getString(R.string.title_ask_delete_rule));

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(args);
        fragment.setTargetFragment(FragmentRule.this, REQUEST_DELETE);
        fragment.show(getParentFragmentManager(), "answer:delete");
    }

    private void onActionCheck() {
        try {
            JSONObject jcondition = getCondition();
            JSONObject jaction = getAction();

            JSONObject jheader = jcondition.optJSONObject("header");
            if (jheader != null) {
                Snackbar.make(view, R.string.title_rule_no_headers, Snackbar.LENGTH_LONG).show();
                return;
            }

            Bundle args = new Bundle();
            args.putLong("folder", folder);
            args.putString("condition", jcondition.toString());
            args.putString("action", jaction.toString());

            FragmentDialogCheck fragment = new FragmentDialogCheck();
            fragment.setArguments(args);
            fragment.show(getParentFragmentManager(), "rule:check");

        } catch (JSONException ex) {
            Log.e(ex);
        }
    }

    private void onActionSave() {
        if (!ActivityBilling.isPro(getContext())) {
            startActivity(new Intent(getContext(), ActivityBilling.class));
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
                    JSONObject jschedule = jcondition.optJSONObject("schedule");

                    if (jsender == null &&
                            jrecipient == null &&
                            jsubject == null &&
                            !jcondition.optBoolean("attachments") &&
                            jheader == null &&
                            jschedule == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_rule_condition_missing));

                    if (TextUtils.isEmpty(order))
                        order = "10";

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
                        Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, args, "rule:save");
        } catch (JSONException ex) {
            Log.e(ex);
        }
    }

    private JSONObject getCondition() throws JSONException {
        JSONObject jcondition = new JSONObject();

        String sender = etSender.getText().toString();
        boolean known = cbKnownSender.isChecked();
        if (!TextUtils.isEmpty(sender) || known) {
            JSONObject jsender = new JSONObject();
            jsender.put("value", sender);
            jsender.put("regex", cbSender.isChecked());
            jsender.put("known", known);
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

        jcondition.put("attachments", cbAttachments.isChecked());

        String header = etHeader.getText().toString();
        if (!TextUtils.isEmpty(header)) {
            JSONObject jheader = new JSONObject();
            jheader.put("value", header);
            jheader.put("regex", cbHeader.isChecked());
            jcondition.put("header", jheader);
        }

        int dstart = spScheduleDayStart.getSelectedItemPosition();
        int dend = spScheduleDayEnd.getSelectedItemPosition();
        Object hstart = tvScheduleHourStart.getTag();
        Object hend = tvScheduleHourEnd.getTag();
        if (hstart == null)
            hstart = 0;
        if (hend == null)
            hend = 0;

        int start = dstart * 24 * 60 + (int) hstart;
        int end = dend * 24 * 60 + (int) hend;

        if (start != end) {
            JSONObject jschedule = new JSONObject();
            jschedule.put("start", start);
            jschedule.put("end", end);
            jcondition.put("schedule", jschedule);
        }

        return jcondition;
    }

    private JSONObject getAction() throws JSONException {
        JSONObject jaction = new JSONObject();

        Action action = (Action) spAction.getSelectedItem();
        if (action != null) {
            jaction.put("type", action.type);
            switch (action.type) {
                case EntityRule.TYPE_SNOOZE:
                    jaction.put("duration", npDuration.getValue());
                    jaction.put("schedule_end", cbScheduleEnd.isChecked());
                    jaction.put("seen", cbSnoozeSeen.isChecked());
                    break;

                case EntityRule.TYPE_FLAG:
                    int color = btnColor.getColor();
                    if (color != Color.TRANSPARENT)
                        jaction.put("color", color);
                    break;

                case EntityRule.TYPE_IMPORTANCE:
                    jaction.put("value", spImportance.getSelectedItemPosition());
                    break;

                case EntityRule.TYPE_KEYWORD:
                    jaction.put("keyword", MessageHelper.sanitizeKeyword(etKeyword.getText().toString()));
                    break;

                case EntityRule.TYPE_MOVE:
                case EntityRule.TYPE_COPY:
                    AccountFolder target = (AccountFolder) spTarget.getSelectedItem();
                    jaction.put("target", target == null ? -1 : target.folder.id);
                    if (action.type == EntityRule.TYPE_MOVE) {
                        jaction.put("seen", cbMoveSeen.isChecked());
                        jaction.put("thread", cbMoveThread.isChecked());
                    }
                    break;

                case EntityRule.TYPE_ANSWER:
                    EntityIdentity identity = (EntityIdentity) spIdent.getSelectedItem();
                    EntityAnswer answer = (EntityAnswer) spAnswer.getSelectedItem();
                    jaction.put("identity", identity == null ? -1 : identity.id);
                    jaction.put("answer", answer == null ? -1 : answer.id);
                    jaction.put("cc", cbCc.isChecked());
                    break;
            }
        }

        return jaction;
    }

    private class AccountFolder {
        EntityAccount account;
        EntityFolder folder;

        public AccountFolder(EntityAccount account, EntityFolder folder) {
            this.account = account;
            this.folder = folder;
        }

        @NonNull
        @Override
        public String toString() {
            return account.name + "/" + folder.name;
        }
    }

    private class RefData {
        EntityFolder folder;
        List<AccountFolder> folders;
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

    private String formatHour(Context context, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
        cal.set(Calendar.MINUTE, minutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return Helper.getTimeInstance(context, SimpleDateFormat.SHORT).format(cal.getTime());
    }

    public static class TimePickerFragment extends FragmentDialogBase implements TimePickerDialog.OnTimeSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            int minutes = args.getInt("minutes");

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
            cal.set(Calendar.MINUTE, minutes % 60);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return new TimePickerDialog(getContext(), this,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(getContext()));
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            Fragment target = getTargetFragment();
            if (target != null) {
                Intent data = new Intent();
                data.putExtra("minutes", hour * 60 + minute);
                target.onActivityResult(getTargetRequestCode(), RESULT_OK, data);
            }
        }
    }

    public static class FragmentDialogCheck extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            long folder = getArguments().getLong("folder");
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
            args.putString("condition", condition);
            args.putString("action", action);

            btnExecute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SimpleTask<Integer>() {
                        @Override
                        protected void onPreExecute(Bundle args) {
                            ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        protected Integer onExecute(Context context, Bundle args) throws Throwable {
                            EntityRule rule = new EntityRule();
                            rule.folder = args.getLong("folder");
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
                                ServiceSynchronize.eval(getContext(), "rules/manual");

                            ToastEx.makeText(getContext(), getString(R.string.title_rule_applied, applied), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            if (ex instanceof IllegalArgumentException)
                                ToastEx.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                            else
                                Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogCheck.this, args, "rule:execute");
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
                    rule.condition = args.getString("condition");
                    rule.action = args.getString("action");

                    List<EntityMessage> matching = new ArrayList<>();

                    DB db = DB.getInstance(context);
                    List<Long> ids =
                            db.message().getMessageIdsByFolder(rule.folder);
                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            continue;

                        if (rule.matches(context, message, null))
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
                        tvNoMessages.setText(ex.getMessage());
                        tvNoMessages.setVisibility(View.VISIBLE);
                    } else
                        Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, args, "rule:check");

            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.title_rule_matched)
                    .setView(dview)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
