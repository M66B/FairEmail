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

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class FragmentRule extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;
    private ConstraintLayout content;

    private TextView tvFolder;
    private EditText etName;
    private AutoCompleteTextView etGroup;
    private EditText etOrder;
    private CheckBox cbEnabled;
    private CheckBox cbDaily;
    private EditText etAge;
    private CheckBox cbStop;

    private CheckBox cbSenderNot;
    private EditText etSender;
    private CheckBox cbSender;
    private ImageButton ibSender;
    private CheckBox cbKnownSender;

    private CheckBox cbRecipientNot;
    private EditText etRecipient;
    private CheckBox cbRecipient;
    private ImageButton ibRecipient;

    private CheckBox cbSubjectNot;
    private EditText etSubject;
    private CheckBox cbSubject;

    private CheckBox cbAttachments;
    private EditText etMimeType;

    private CheckBox cbHeaderNot;
    private EditText etHeader;
    private ImageButton ibHeader;
    private CheckBox cbHeader;

    private CheckBox cbBodyNot;
    private EditText etBody;
    private CheckBox cbBody;
    private CheckBox cbSkipQuotes;

    private TextView tvDateAfter;
    private TextView tvDateBefore;
    private Button btnDateAfter;
    private Button btnDateBefore;

    private Spinner spScheduleDayStart;
    private Spinner spScheduleDayEnd;
    private TextView tvScheduleHourStart;
    private TextView tvScheduleHourEnd;
    private CheckBox cbEveryDay;
    private EditText etYounger;

    private ImageButton ibExpression;
    private EditText etExpression;

    private Spinner spAction;
    private TextView tvActionRemark;

    private CheckBox cbHideSeen;

    private NumberPicker npDuration;
    private CheckBox cbScheduleEnd;
    private CheckBox cbSnoozeSeen;

    private ViewButtonColor btnColor;

    private Spinner spImportance;

    private EditText etKeyword;
    private RadioGroup rgKeyword;

    private Button btnFolder;
    private EditText etMoveCreate;
    private CheckBox cbMoveSeen;
    private CheckBox cbMoveThread;

    private Spinner spIdent;

    private Spinner spAnswer;
    private CheckBox cbAnswerSubject;
    private CheckBox cbOriginalText;
    private CheckBox cbWithAttachments;
    private EditText etTo;
    private ImageButton ibTo;
    private CheckBox cbResend;
    private CheckBox cbAttached;
    private CheckBox cbCc;
    private CheckBox cbChecks;

    private Button btnTtsSetup;
    private Button btnTtsData;

    private Button btnSound;
    private CheckBox cbLoop;
    private CheckBox cbAlarm;
    private EditText etAlarmDuration;

    private TextView tvAutomation;

    private EditText etNotes;
    private ViewButtonColor btnColorNotes;

    private Spinner spUrlMethod;
    private EditText etUrl;
    private EditText etContent;
    private TextView tvUrlHint;

    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;

    private Group grpReady;
    private Group grpAge;
    private Group grpHide;
    private Group grpSnooze;
    private Group grpFlag;
    private Group grpImportance;
    private Group grpKeyword;
    private Group grpMove;
    private Group grpMoveProp;
    private Group grpAnswer;
    private Group grpTts;
    private Group grpSound;
    private Group grpAutomation;
    private Group grpDelete;
    private Group grpLocalOnly;
    private Group grpNotes;
    private Group grpUrl;
    private Group grpSummarize;

    private ArrayAdapter<String> adapterGroup;
    private ArrayAdapter<String> adapterDay;
    private ArrayAdapter<Action> adapterAction;
    private ArrayAdapter<EntityIdentity> adapterIdentity;
    private ArrayAdapter<EntityAnswer> adapterAnswer;

    private long id = -1;
    private long copy = -1;
    private long account = -1;
    private int protocol = -1;
    private long folder = -1;
    private Uri sound = null;

    private DateFormat DF;

    private static final int REQUEST_SENDER = 1;
    private static final int REQUEST_RECIPIENT = 2;
    private static final int REQUEST_COLOR = 3;
    private final static int REQUEST_DELETE = 4;
    private final static int REQUEST_SCHEDULE_START = 5;
    private final static int REQUEST_SCHEDULE_END = 6;
    private static final int REQUEST_TO = 7;
    private final static int REQUEST_TTS_CHECK = 8;
    private final static int REQUEST_TTS_DATA = 9;
    private final static int REQUEST_RINGTONE = 10;
    private final static int REQUEST_AUDIO = 11;
    private final static int REQUEST_DATE_AFTER = 12;
    private final static int REQUEST_DATE_BEFORE = 13;
    private final static int REQUEST_FOLDER = 14;
    private final static int REQUEST_COLOR_NOTES = 15;

    private static final List<String> HEADER_CONDITIONS = Collections.unmodifiableList(Arrays.asList(
            "$$seen$",
            "$$answered$",
            "$$flagged$",
            "$$deleted$",
            "$$tls$",
            "$$dkim$",
            "$$spf$",
            "$$dmarc$",
            "$$auth$",
            "$$mx$",
            "$$blocklist$",
            "$$replydomain$",
            "$$nofrom$",
            "$$multifrom$",
            "$$automatic$",
            "$$lowpriority$",
            "$$highpriority$",
            "$$signed$",
            "$$encrypted$",
            "$$aligned$"
    ));

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
        protocol = args.getInt("protocol", EntityAccount.TYPE_IMAP);
        folder = args.getLong("folder", -1);

        if (savedInstanceState != null)
            sound = savedInstanceState.getParcelable("fair:sound");

        DF = Helper.getDateTimeInstance(getContext(), DateFormat.SHORT, DateFormat.SHORT);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_rule_caption);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_rule, container, false);

        // Get controls
        scroll = view.findViewById(R.id.scroll);
        content = view.findViewById(R.id.content);

        tvFolder = view.findViewById(R.id.tvFolder);
        etName = view.findViewById(R.id.etName);
        etGroup = view.findViewById(R.id.etGroup);
        etOrder = view.findViewById(R.id.etOrder);
        cbEnabled = view.findViewById(R.id.cbEnabled);
        cbDaily = view.findViewById(R.id.cbDaily);
        etAge = view.findViewById(R.id.etAge);
        cbStop = view.findViewById(R.id.cbStop);

        cbSenderNot = view.findViewById(R.id.cbSenderNot);
        etSender = view.findViewById(R.id.etSender);
        cbSender = view.findViewById(R.id.cbSender);
        ibSender = view.findViewById(R.id.ibSender);
        cbKnownSender = view.findViewById(R.id.cbKnownSender);

        cbRecipientNot = view.findViewById(R.id.cbRecipientNot);
        etRecipient = view.findViewById(R.id.etRecipient);
        cbRecipient = view.findViewById(R.id.cbRecipient);
        ibRecipient = view.findViewById(R.id.ibRecipient);

        cbSubjectNot = view.findViewById(R.id.cbSubjectNot);
        etSubject = view.findViewById(R.id.etSubject);
        cbSubject = view.findViewById(R.id.cbSubject);

        cbAttachments = view.findViewById(R.id.cbAttachments);
        etMimeType = view.findViewById(R.id.etMimeType);

        cbHeaderNot = view.findViewById(R.id.cbHeaderNot);
        etHeader = view.findViewById(R.id.etHeader);
        ibHeader = view.findViewById(R.id.ibHeader);
        cbHeader = view.findViewById(R.id.cbHeader);

        cbBodyNot = view.findViewById(R.id.cbBodyNot);
        etBody = view.findViewById(R.id.etBody);
        cbBody = view.findViewById(R.id.cbBody);
        cbSkipQuotes = view.findViewById(R.id.cbSkipQuotes);

        tvDateAfter = view.findViewById(R.id.tvDateAfter);
        tvDateBefore = view.findViewById(R.id.tvDateBefore);
        btnDateAfter = view.findViewById(R.id.btnDateAfter);
        btnDateBefore = view.findViewById(R.id.btnDateBefore);

        spScheduleDayStart = view.findViewById(R.id.spScheduleDayStart);
        spScheduleDayEnd = view.findViewById(R.id.spScheduleDayEnd);
        tvScheduleHourStart = view.findViewById(R.id.tvScheduleHourStart);
        tvScheduleHourEnd = view.findViewById(R.id.tvScheduleHourEnd);
        cbEveryDay = view.findViewById(R.id.cbEveryDay);
        etYounger = view.findViewById(R.id.etYounger);

        ibExpression = view.findViewById(R.id.ibExpression);
        etExpression = view.findViewById(R.id.etExpression);

        spAction = view.findViewById(R.id.spAction);
        tvActionRemark = view.findViewById(R.id.tvActionRemark);

        cbHideSeen = view.findViewById(R.id.cbHideSeen);

        npDuration = view.findViewById(R.id.npDuration);
        cbScheduleEnd = view.findViewById(R.id.cbScheduleEnd);
        cbSnoozeSeen = view.findViewById(R.id.cbSnoozeSeen);

        btnColor = view.findViewById(R.id.btnColor);

        spImportance = view.findViewById(R.id.spImportance);

        etKeyword = view.findViewById(R.id.etKeyword);
        rgKeyword = view.findViewById(R.id.rgKeyword);

        btnFolder = view.findViewById(R.id.btnFolder);
        etMoveCreate = view.findViewById(R.id.etMoveCreate);
        cbMoveSeen = view.findViewById(R.id.cbMoveSeen);
        cbMoveThread = view.findViewById(R.id.cbMoveThread);

        spIdent = view.findViewById(R.id.spIdent);

        spAnswer = view.findViewById(R.id.spAnswer);
        cbAnswerSubject = view.findViewById(R.id.cbAnswerSubject);
        cbOriginalText = view.findViewById(R.id.cbOriginalText);
        cbWithAttachments = view.findViewById(R.id.cbWithAttachments);
        etTo = view.findViewById(R.id.etTo);
        ibTo = view.findViewById(R.id.ibTo);
        cbResend = view.findViewById(R.id.cbResend);
        cbAttached = view.findViewById(R.id.cbAttached);
        cbCc = view.findViewById(R.id.cbCc);
        cbChecks = view.findViewById(R.id.cbChecks);

        btnTtsSetup = view.findViewById(R.id.btnTtsSetup);
        btnTtsData = view.findViewById(R.id.btnTtsData);

        btnSound = view.findViewById(R.id.btnSound);
        cbLoop = view.findViewById(R.id.cbLoop);
        cbAlarm = view.findViewById(R.id.cbAlarm);
        etAlarmDuration = view.findViewById(R.id.etAlarmDuration);

        tvAutomation = view.findViewById(R.id.tvAutomation);

        etNotes = view.findViewById(R.id.etNotes);
        btnColorNotes = view.findViewById(R.id.btnColorNotes);

        spUrlMethod = view.findViewById(R.id.spUrlMethod);
        etUrl = view.findViewById(R.id.etUrl);
        etContent = view.findViewById(R.id.etContent);
        tvUrlHint = view.findViewById(R.id.tvUrlHint);

        bottom_navigation = view.findViewById(R.id.bottom_navigation);

        pbWait = view.findViewById(R.id.pbWait);

        grpReady = view.findViewById(R.id.grpReady);
        grpAge = view.findViewById(R.id.grpAge);
        grpHide = view.findViewById(R.id.grpHide);
        grpSnooze = view.findViewById(R.id.grpSnooze);
        grpFlag = view.findViewById(R.id.grpFlag);
        grpImportance = view.findViewById(R.id.grpImportance);
        grpKeyword = view.findViewById(R.id.grpKeyword);
        grpMove = view.findViewById(R.id.grpMove);
        grpMoveProp = view.findViewById(R.id.grpMoveProp);
        grpAnswer = view.findViewById(R.id.grpAnswer);
        grpTts = view.findViewById(R.id.grpTts);
        grpSound = view.findViewById(R.id.grpSound);
        grpAutomation = view.findViewById(R.id.grpAutomation);
        grpDelete = view.findViewById(R.id.grpDelete);
        grpLocalOnly = view.findViewById(R.id.grpLocalOnly);
        grpNotes = view.findViewById(R.id.grpNotes);
        grpUrl = view.findViewById(R.id.grpUrl);
        grpSummarize = view.findViewById(R.id.grpSummarize);

        adapterGroup = new ArrayAdapter<>(getContext(), R.layout.spinner_item1_dropdown, android.R.id.text1);
        etGroup.setThreshold(1);
        etGroup.setAdapter(adapterGroup);

        cbDaily.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                grpAge.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        ibSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pick = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
                pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(Helper.getChooser(getContext(), pick), REQUEST_RECIPIENT);
            }
        });

        cbAttachments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                etMimeType.setEnabled(isChecked);
            }
        });

        ibHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(v.getContext(), getViewLifecycleOwner(), v);

                for (int i = 0; i < HEADER_CONDITIONS.size(); i++)
                    popupMenu.getMenu().add(Menu.NONE, i + 1, i + 1, HEADER_CONDITIONS.get(i));

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        etHeader.setText(item.getTitle());
                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        tvDateAfter.setText("-");
        tvDateBefore.setText("-");

        btnDateAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("title", getString(R.string.title_rule_time_after));
                args.putBoolean("day", true);

                Object time = tvDateAfter.getTag();
                if (time != null)
                    args.putLong("time", (long) time);

                FragmentDialogDuration fragment = new FragmentDialogDuration();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentRule.this, REQUEST_DATE_AFTER);
                fragment.show(getParentFragmentManager(), "date:after");
            }
        });

        btnDateBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("title", getString(R.string.title_rule_time_before));
                args.putBoolean("day", true);

                Object time = tvDateBefore.getTag();
                if (time != null)
                    args.putLong("time", (long) time);

                FragmentDialogDuration fragment = new FragmentDialogDuration();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentRule.this, REQUEST_DATE_BEFORE);
                fragment.show(getParentFragmentManager(), "date:before");
            }
        });

        adapterDay = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<String>());
        adapterDay.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spScheduleDayStart.setAdapter(adapterDay);
        spScheduleDayEnd.setAdapter(adapterDay);

        cbEveryDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                spScheduleDayStart.setEnabled(!isChecked);
                spScheduleDayEnd.setEnabled(!isChecked);
            }
        });

        int dp6 = Helper.dp2pixels(getContext(), 6);
        adapterAction = new ArrayAdapter<Action>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return getView(position, super.getView(position, convertView, parent));
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return getView(position, super.getDropDownView(position, convertView, parent));
            }

            private View getView(int position, View view) {
                Action action = getItem(position);
                if (action != null) {
                    TextView tv = view.findViewById(android.R.id.text1);
                    if (tv != null) {
                        tv.setText(action.name);
                        tv.setCompoundDrawablePadding(dp6);
                        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(action.icon, 0, 0, 0);
                    }
                }
                return view;
            }
        };
        adapterAction.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAction.setAdapter(adapterAction);

        btnFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<List<EntityAccount>>() {
                    @Override
                    protected List<EntityAccount> onExecute(Context context, Bundle args) {
                        DB db = DB.getInstance(context);
                        return db.account().getSynchronizingAccounts(EntityAccount.TYPE_IMAP);
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                        if (accounts == null)
                            return;

                        if (accounts.size() == 1) {
                            selectFolder(accounts.get(0).id);
                            return;
                        }

                        PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), btnFolder);

                        int order = 0;
                        for (EntityAccount account : accounts) {
                            order++;
                            popupMenu.getMenu().add(Menu.NONE, order, order, account.name)
                                    .setIntent(new Intent().putExtra("account", account.id));
                        }

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                long account = item.getIntent().getLongExtra("account", -1);
                                selectFolder(account);
                                return true;
                            }
                        });

                        popupMenu.show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }

                    private void selectFolder(long account) {
                        Bundle args = new Bundle();
                        args.putString("title", getString(R.string.title_rule_folder));
                        args.putLong("account", account);
                        args.putLongArray("disabled", new long[]{});

                        FragmentDialogSelectFolder fragment = new FragmentDialogSelectFolder();
                        fragment.setArguments(args);
                        fragment.setTargetFragment(FragmentRule.this, REQUEST_FOLDER);
                        fragment.show(getParentFragmentManager(), "rule:folder");
                    }

                }.execute(FragmentRule.this, new Bundle(), "rule:folder");
            }
        });

        adapterIdentity = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityIdentity>());
        adapterIdentity.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spIdent.setAdapter(adapterIdentity);

        adapterAnswer = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityAnswer>());
        adapterAnswer.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAnswer.setAdapter(adapterAnswer);

        String[] dayNames = DateFormatSymbols.getInstance().getWeekdays();
        for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++)
            adapterDay.add(dayNames[day]);

        AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parent.post(new Runnable() {
                    @Override
                    public void run() {
                        //parent.requestFocusFromTouch();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        };

        spScheduleDayStart.setOnItemSelectedListener(onItemSelectedListener);
        spScheduleDayEnd.setOnItemSelectedListener(onItemSelectedListener);

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

        ibExpression.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), "expression_conditions");
            }
        });

        List<Action> actions = new ArrayList<>();
        actions.add(new Action(EntityRule.TYPE_NOOP, getString(R.string.title_rule_noop), R.drawable.twotone_remove_circle_outline_24));
        actions.add(new Action(EntityRule.TYPE_SEEN, getString(R.string.title_rule_seen), R.drawable.twotone_drafts_24));
        actions.add(new Action(EntityRule.TYPE_UNSEEN, getString(R.string.title_rule_unseen), R.drawable.twotone_mail_24));
        actions.add(new Action(EntityRule.TYPE_HIDE, getString(R.string.title_rule_hide), R.drawable.twotone_visibility_off_24));
        actions.add(new Action(EntityRule.TYPE_IGNORE, getString(R.string.title_rule_ignore), R.drawable.twotone_notifications_off_24));
        actions.add(new Action(EntityRule.TYPE_SILENT, getString(R.string.title_rule_silent), R.drawable.twotone_volume_off_24));
        if (BuildConfig.DEBUG)
            actions.add(new Action(EntityRule.TYPE_LOCAL_ONLY, getString(R.string.title_rule_local_only), R.drawable.twotone_android_24));
        actions.add(new Action(EntityRule.TYPE_SNOOZE, getString(R.string.title_rule_snooze), R.drawable.twotone_timelapse_24));
        actions.add(new Action(EntityRule.TYPE_FLAG, getString(R.string.title_rule_flag), R.drawable.twotone_star_24));
        actions.add(new Action(EntityRule.TYPE_IMPORTANCE, getString(R.string.title_rule_importance), R.drawable.twotone_north_24));
        actions.add(new Action(EntityRule.TYPE_KEYWORD, getString(R.string.title_rule_keyword), R.drawable.twotone_label_important_24));
        actions.add(new Action(EntityRule.TYPE_NOTES, getString(R.string.title_rule_notes), R.drawable.twotone_sticky_note_2_24));
        actions.add(new Action(EntityRule.TYPE_MOVE, getString(R.string.title_rule_move), R.drawable.twotone_drive_file_move_24));
        if (protocol == EntityAccount.TYPE_IMAP)
            actions.add(new Action(EntityRule.TYPE_COPY, getString(R.string.title_rule_copy), R.drawable.twotone_file_copy_24));
        actions.add(new Action(EntityRule.TYPE_DELETE, getString(R.string.title_rule_delete), R.drawable.twotone_delete_forever_24));
        actions.add(new Action(EntityRule.TYPE_ANSWER, getString(R.string.title_rule_answer), R.drawable.twotone_reply_24));
        if (AI.isAvailable(getContext()))
            actions.add(new Action(EntityRule.TYPE_SUMMARIZE, getString(R.string.title_rule_summarize), R.drawable.twotone_smart_toy_24));
        actions.add(new Action(EntityRule.TYPE_TTS, getString(R.string.title_rule_tts), R.drawable.twotone_record_voice_over_24));
        actions.add(new Action(EntityRule.TYPE_SOUND, getString(R.string.title_rule_sound), R.drawable.twotone_play_arrow_24));
        actions.add(new Action(EntityRule.TYPE_AUTOMATION, getString(R.string.title_rule_automation), R.drawable.twotone_auto_awesome_24));
        actions.add(new Action(EntityRule.TYPE_URL, getString(R.string.title_rule_url), R.drawable.twotone_insert_link_45_24));
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

                adapterView.post(new Runnable() {
                    @Override
                    public void run() {
                        //adapterView.requestFocusFromTouch();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                onActionSelected(-1);
            }

            private void onActionSelected(int type) {
                showActionParameters(type);

                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                            return;
                        scroll.smoothScrollTo(0, content.getBottom());
                    }
                });
            }
        });

        cbAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etAlarmDuration.setEnabled(isChecked);
            }
        });

        etAlarmDuration.setHint(Integer.toString(MediaPlayerHelper.DEFAULT_ALARM_DURATION));
        etAlarmDuration.setEnabled(false);

        npDuration.setMinValue(0);
        npDuration.setMaxValue(999);

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("color", btnColor.getColor());
                args.putString("title", getString(R.string.title_flag_color));
                args.putBoolean("reset", true);
                args.putInt("faq", 187);

                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentRule.this, REQUEST_COLOR);
                fragment.show(getParentFragmentManager(), "rule:color");
            }
        });

        spImportance.setOnItemSelectedListener(onItemSelectedListener);
        spIdent.setOnItemSelectedListener(onItemSelectedListener);
        spAnswer.setOnItemSelectedListener(onItemSelectedListener);

        cbResend.setEnabled(false);
        etTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (cbResend == null)
                    return;

                cbResend.setEnabled(!TextUtils.isEmpty(s.toString()));
            }
        });

        ibTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pick = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(Helper.getChooser(getContext(), pick), REQUEST_TO);
            }
        });

        cbResend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                checked = (checked && compoundButton.isEnabled());
                spAnswer.setEnabled(!checked);
                cbAnswerSubject.setEnabled(!checked);
                cbOriginalText.setEnabled(!checked);
                cbWithAttachments.setEnabled(!checked);
                cbAttached.setEnabled(!checked && protocol == EntityAccount.TYPE_IMAP);
            }
        });

        btnTtsSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(intent, REQUEST_TTS_CHECK);
            }
        });

        btnTtsData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivityForResult(intent, REQUEST_TTS_DATA);
            }
        });

        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), v);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_rule_select_sound_ringtone, 1, R.string.title_rule_select_sound_ringtone);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_rule_select_sound_audio, 2, R.string.title_rule_select_sound_audio);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.string.title_rule_select_sound_ringtone) {
                            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.title_advanced_sound));
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, sound);
                            startActivityForResult(Helper.getChooser(getContext(), intent), REQUEST_RINGTONE);
                            return true;
                        } else if (itemId == R.string.title_rule_select_sound_audio) {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setType("audio/*");
                            Helper.openAdvanced(getContext(), intent);
                            startActivityForResult(Helper.getChooser(getContext(), intent), REQUEST_AUDIO);
                            return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        tvAutomation.setText(getString(R.string.title_rule_automation_hint,
                EntityRule.ACTION_AUTOMATION,
                TextUtils.join(",", EntityRule.EXTRA_ALL)));

        btnColorNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("color", btnColorNotes.getColor());
                args.putString("title", getString(R.string.title_rule_notes));
                args.putBoolean("reset", true);

                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentRule.this, REQUEST_COLOR_NOTES);
                fragment.show(getParentFragmentManager(), "rule:color:notes");
            }
        });

        etContent.setEnabled(false);
        spUrlMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String method = null;
                try {
                    String[] methods = getResources().getStringArray(R.array.httpMethodNames);
                    if (position >= 0 && position < methods.length)
                        method = methods[position];
                } catch (Throwable ex) {
                    Log.e(ex);
                }
                etContent.setEnabled("POST".equals(method) || "PUT".equals(method));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etContent.setEnabled(false);
            }
        });

        List<String> extras = new ArrayList<>();
        for (String extra : EntityRule.EXTRA_ALL)
            extras.add("$" + extra + "$");
        tvUrlHint.setText(getString(R.string.title_rule_url_hint, TextUtils.join(", ", extras)));

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_delete) {
                    onActionDelete();
                    return true;
                } else if (itemId == R.id.action_check) {
                    onActionCheck();
                    return true;
                } else if (itemId == R.id.action_save) {
                    onActionSave();
                    return true;
                }
                return false;
            }
        });

        // Initialize
        tvFolder.setText(null);
        bottom_navigation.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        grpAge.setVisibility(View.GONE);
        grpHide.setVisibility(View.GONE);
        grpSnooze.setVisibility(View.GONE);
        grpFlag.setVisibility(View.GONE);
        grpImportance.setVisibility(View.GONE);
        grpKeyword.setVisibility(View.GONE);
        grpMove.setVisibility(View.GONE);
        grpMoveProp.setVisibility(View.GONE);
        grpAnswer.setVisibility(View.GONE);
        grpTts.setVisibility(View.GONE);
        grpSound.setVisibility(View.GONE);
        grpAutomation.setVisibility(View.GONE);
        grpDelete.setVisibility(View.GONE);
        grpLocalOnly.setVisibility(View.GONE);
        grpNotes.setVisibility(View.GONE);
        grpUrl.setVisibility(View.GONE);
        grpSummarize.setVisibility(View.GONE);

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
                data.account = db.account().getAccount(aid);
                data.folder = db.folder().getFolder(fid);
                data.groups = db.rule().getGroups();
                data.identities = db.identity().getSynchronizingIdentities(aid);
                data.answers = db.answer().getAnswers(false);

                return data;
            }

            @Override
            protected void onExecuted(Bundle args, RefData data) {
                tvFolder.setText(String.format("%s:%s",
                        data.account == null ? "" : data.account.name,
                        data.folder.getDisplayName(getContext())));

                adapterGroup.clear();
                adapterGroup.addAll(data.groups);

                adapterIdentity.clear();
                adapterIdentity.addAll(data.identities);

                EntityAnswer none = new EntityAnswer();
                none.name = "-";
                none.favorite = false;
                data.answers.add(0, none);

                adapterAnswer.clear();
                adapterAnswer.addAll(data.answers);

                tvActionRemark.setText(
                        getString(R.string.title_rule_action_remark, data.folder.getDisplayName(getContext())));

                loadRule(savedInstanceState);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "rule:accounts");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rule, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_help) {
            onMenuHelp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuHelp() {
        Helper.viewFAQ(getContext(), 71);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Object tag = (btnFolder == null ? null : btnFolder.getTag());
        outState.putInt("fair:start", spScheduleDayStart == null ? 0 : spScheduleDayStart.getSelectedItemPosition());
        outState.putInt("fair:end", spScheduleDayEnd == null ? 0 : spScheduleDayEnd.getSelectedItemPosition());
        outState.putInt("fair:action", spAction == null ? 0 : spAction.getSelectedItemPosition());
        outState.putInt("fair:importance", spImportance == null ? 0 : spImportance.getSelectedItemPosition());
        outState.putLong("fair:target", tag == null ? -1 : (long) tag);
        outState.putCharSequence("fair:name", btnFolder == null ? null : btnFolder.getText());
        outState.putInt("fair:identity", spIdent == null ? 0 : spIdent.getSelectedItemPosition());
        outState.putInt("fair:answer", spAnswer == null ? 0 : spAnswer.getSelectedItemPosition());
        outState.putParcelable("fair:sound", sound);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_SENDER:
                    if (resultCode == RESULT_OK && data != null)
                        onPickContact(data, etSender);
                    break;
                case REQUEST_RECIPIENT:
                    if (resultCode == RESULT_OK && data != null)
                        onPickContact(data, etRecipient);
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
                        onScheduleStart(data.getBundleExtra("args"));
                    break;
                case REQUEST_SCHEDULE_END:
                    if (resultCode == RESULT_OK)
                        onScheduleEnd(data.getBundleExtra("args"));
                    break;
                case REQUEST_TO:
                    if (resultCode == RESULT_OK && data != null)
                        onPickContact(data, etTo);
                    break;
                case REQUEST_TTS_CHECK:
                    if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
                        ToastEx.makeText(getContext(), R.string.title_rule_tts_ok, Toast.LENGTH_LONG).show();
                    else {
                        Intent tts = new Intent();
                        tts.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        startActivity(tts);
                    }
                    break;
                case REQUEST_TTS_DATA:
                    break;
                case REQUEST_RINGTONE:
                    if (resultCode == RESULT_OK && data != null)
                        onSelectSound(data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI));
                    break;
                case REQUEST_AUDIO:
                    if (resultCode == RESULT_OK && data != null)
                        onSelectSound(data.getData());
                    break;
                case REQUEST_DATE_AFTER:
                    if (resultCode == RESULT_OK && data != null)
                        onDateAfter(data.getBundleExtra("args"));
                    break;
                case REQUEST_DATE_BEFORE:
                    if (resultCode == RESULT_OK && data != null)
                        onDateBefore(data.getBundleExtra("args"));
                    break;
                case REQUEST_FOLDER:
                    if (resultCode == RESULT_OK && data != null)
                        onFolderSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_COLOR_NOTES:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        btnColorNotes.setColor(args.getInt("color"));
                    }
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onPickContact(Intent data, final EditText et) {
        Uri uri = data.getData();
        if (uri == null) return;
        try (Cursor cursor = getContext().getContentResolver().query(uri,
                new String[]{
                        ContactsContract.CommonDataKinds.Email.ADDRESS
                },
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst())
                et.setText(cursor.getString(0));
        } catch (Throwable ex) {
            Log.e(ex);
            if (ex instanceof SecurityException)
                try {
                    String permission = android.Manifest.permission.READ_CONTACTS;
                    requestPermissions(new String[]{permission}, REQUEST_PERMISSIONS);
                } catch (Throwable ex1) {
                    Log.unexpectedError(FragmentRule.this, ex1);
                }
            else
                Log.unexpectedError(FragmentRule.this, ex);
        }
    }

    private void onSelectSound(Uri uri) {
        try {
            Log.i("Selected sound uri=" + uri);
            if (uri != null) {
                getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (!Helper.isPersisted(getContext(), uri, true, false))
                    throw new IllegalStateException("No permission granted to access selected image " + uri);
            }
        } catch (Throwable ex) {
            Log.w(ex);
        }
        this.sound = uri;
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

    private void onScheduleStart(Bundle args) {
        int minutes = args.getInt("minutes", 0);
        tvScheduleHourStart.setTag(minutes);
        tvScheduleHourStart.setText(Helper.formatHour(getContext(), minutes));
        cbScheduleEnd.setChecked(true);
    }

    private void onScheduleEnd(Bundle args) {
        int minutes = args.getInt("minutes", 0);
        tvScheduleHourEnd.setTag(minutes);
        tvScheduleHourEnd.setText(Helper.formatHour(getContext(), minutes));
        cbScheduleEnd.setChecked(true);
    }

    private void onDateAfter(Bundle args) {
        boolean reset = args.getBoolean("reset");
        long time = args.getLong("time");
        if (reset)
            time = 0;
        tvDateAfter.setTag(time);
        tvDateAfter.setText(time == 0 ? "-" : DF.format(time));
    }

    private void onDateBefore(Bundle args) {
        boolean reset = args.getBoolean("reset");
        long time = args.getLong("time");
        if (reset)
            time = 0;
        tvDateBefore.setTag(time);
        tvDateBefore.setText(time == 0 ? "-" : DF.format(time));
    }

    private void onFolderSelected(Bundle args) {
        long folder = args.getLong("folder");
        showFolder(folder);
    }

    private void showFolder(long id) {
        btnFolder.setTag(id);

        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<String>() {
            @Override
            protected String onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);

                EntityFolder folder = db.folder().getFolder(id);
                if (folder == null)
                    return null;

                EntityAccount account = db.account().getAccount(folder.account);
                if (account == null)
                    return null;

                return account.name + ":" + folder.name;
            }

            @Override
            protected void onExecuted(Bundle args, String name) {
                if (name == null)
                    name = getString(R.string.title_select);
                btnFolder.setText(name);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "rule:folder");
    }

    private void loadRule(final Bundle savedInstanceState) {
        Bundle rargs = new Bundle();
        rargs.putLong("id", copy < 0 ? id : copy);
        rargs.putString("sender", getArguments().getString("sender"));
        rargs.putString("recipient", getArguments().getString("recipient"));
        rargs.putString("subject", getArguments().getString("subject"));

        new SimpleTask<TupleRuleEx>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected TupleRuleEx onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                return db.rule().getRule(id);
            }

            @Override
            protected void onExecuted(Bundle args, TupleRuleEx rule) {
                if (copy > 0 && rule != null) {
                    rule.applied = 0;
                    rule.last_applied = null;
                }

                try {
                    if (savedInstanceState == null) {
                        JSONObject jcondition = (rule == null ? new JSONObject() : new JSONObject(rule.condition));
                        JSONObject jaction = (rule == null ? new JSONObject() : new JSONObject(rule.action));

                        JSONObject jgeneral = jcondition.optJSONObject("general");
                        JSONObject jsender = jcondition.optJSONObject("sender");
                        JSONObject jrecipient = jcondition.optJSONObject("recipient");
                        JSONObject jsubject = jcondition.optJSONObject("subject");
                        JSONObject jheader = jcondition.optJSONObject("header");
                        JSONObject jbody = jcondition.optJSONObject("body");
                        JSONObject jdate = jcondition.optJSONObject("date");
                        JSONObject jschedule = jcondition.optJSONObject("schedule");

                        etName.setText(rule == null ? args.getString("subject") : rule.name);
                        etGroup.setText(rule == null ? null : rule.group);
                        etOrder.setText(rule == null ? null : Integer.toString(rule.order));
                        cbEnabled.setChecked(rule == null || rule.enabled);
                        cbDaily.setChecked(rule != null && rule.daily);
                        etAge.setText(jgeneral == null ? null : Integer.toString(jgeneral.optInt("age")));
                        cbStop.setChecked(rule != null && rule.stop);

                        cbSenderNot.setChecked(jsender != null && jsender.optBoolean("not"));
                        etSender.setText(jsender == null ? args.getString("sender") : jsender.getString("value"));
                        cbSender.setChecked(jsender != null && jsender.getBoolean("regex"));
                        cbKnownSender.setChecked(jsender != null && jsender.optBoolean("known"));
                        etSender.setEnabled(!cbKnownSender.isChecked());
                        ibSender.setEnabled(!cbKnownSender.isChecked());
                        cbSender.setEnabled(!cbKnownSender.isChecked());

                        cbRecipientNot.setChecked(jrecipient != null && jrecipient.optBoolean("not"));
                        etRecipient.setText(jrecipient == null ? args.getString("recipient") : jrecipient.getString("value"));
                        cbRecipient.setChecked(jrecipient != null && jrecipient.getBoolean("regex"));

                        cbSubjectNot.setChecked(jsubject != null && jsubject.optBoolean("not"));
                        etSubject.setText(jsubject == null ? args.getString("subject") : jsubject.getString("value"));
                        cbSubject.setChecked(jsubject != null && jsubject.getBoolean("regex"));

                        cbAttachments.setChecked(jcondition.optBoolean("attachments"));
                        etMimeType.setText(jcondition.optString("mimetype"));
                        etMimeType.setEnabled(cbAttachments.isChecked());

                        cbHeaderNot.setChecked(jheader != null && jheader.optBoolean("not"));
                        etHeader.setText(jheader == null ? null : jheader.getString("value"));
                        cbHeader.setChecked(jheader != null && jheader.getBoolean("regex"));

                        cbBodyNot.setChecked(jbody != null && jbody.optBoolean("not"));
                        etBody.setText(jbody == null ? null : jbody.getString("value"));
                        cbBody.setChecked(jbody != null && jbody.getBoolean("regex"));
                        cbSkipQuotes.setChecked(jbody != null && jbody.optBoolean("skip_quotes"));

                        long after = (jdate != null && jdate.has("after") ? jdate.getLong("after") : 0);
                        long before = (jdate != null && jdate.has("before") ? jdate.getLong("before") : 0);

                        tvDateAfter.setTag(after);
                        tvDateAfter.setText(after == 0 ? "-" : DF.format(after));

                        tvDateBefore.setTag(before);
                        tvDateBefore.setText(before == 0 ? "-" : DF.format(before));

                        int start = (jschedule != null && jschedule.has("start") ? jschedule.getInt("start") : 0);
                        int end = (jschedule != null && jschedule.has("end") ? jschedule.getInt("end") : 0);

                        cbEveryDay.setChecked(jschedule != null && jschedule.optBoolean("all"));
                        etYounger.setText(jcondition.has("younger")
                                ? Integer.toString(jcondition.optInt("younger")) : null);

                        etExpression.setText(jcondition.optString("expression"));

                        spScheduleDayStart.setSelection(start / (24 * 60));
                        spScheduleDayEnd.setSelection(end / (24 * 60));

                        tvScheduleHourStart.setTag(start % (24 * 60));
                        tvScheduleHourStart.setText(Helper.formatHour(getContext(), start % (24 * 60)));

                        tvScheduleHourEnd.setTag(end % (24 * 60));
                        tvScheduleHourEnd.setText(Helper.formatHour(getContext(), end % (24 * 60)));

                        if (rule == null) {
                            for (int pos = 0; pos < adapterIdentity.getCount(); pos++)
                                if (adapterIdentity.getItem(pos).primary) {
                                    spIdent.setSelection(pos);
                                    break;
                                }
                        } else {
                            int type = jaction.getInt("type");
                            switch (type) {
                                case EntityRule.TYPE_HIDE:
                                    cbHideSeen.setChecked(jaction.optBoolean("seen", false));
                                    break;

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
                                    rgKeyword.check(jaction.optBoolean("set", true)
                                            ? R.id.keyword_add : R.id.keyword_delete);
                                    break;

                                case EntityRule.TYPE_MOVE:
                                case EntityRule.TYPE_COPY:
                                    if (copy < 0 || rule.account == account) {
                                        long target = jaction.optLong("target", -1);
                                        showFolder(target);
                                    }
                                    if (type == EntityRule.TYPE_MOVE) {
                                        etMoveCreate.setText(jaction.optString("create"));
                                        cbMoveSeen.setChecked(jaction.optBoolean("seen"));
                                        cbMoveThread.setChecked(jaction.optBoolean("thread"));
                                    }
                                    break;

                                case EntityRule.TYPE_ANSWER:
                                    if (copy < 0 || rule.account == account) {
                                        long identity = jaction.optLong("identity", -1);
                                        for (int pos = 0; pos < adapterIdentity.getCount(); pos++)
                                            if (adapterIdentity.getItem(pos).id.equals(identity)) {
                                                spIdent.setSelection(pos);
                                                break;
                                            }
                                    }

                                    long answer = jaction.optLong("answer", -1);
                                    for (int pos = 1; pos < adapterAnswer.getCount(); pos++)
                                        if (adapterAnswer.getItem(pos).id.equals(answer)) {
                                            spAnswer.setSelection(pos);
                                            break;
                                        }

                                    cbAnswerSubject.setChecked(jaction.optBoolean("answer_subject", false));
                                    cbOriginalText.setChecked(jaction.optBoolean("original_text", true));
                                    cbWithAttachments.setChecked(jaction.optBoolean("attachments"));

                                    etTo.setText(jaction.optString("to"));
                                    cbResend.setChecked(jaction.optBoolean("resend"));
                                    cbAttached.setChecked(jaction.optBoolean("attached"));
                                    cbCc.setChecked(jaction.optBoolean("cc"));
                                    cbChecks.setChecked(jaction.optBoolean("checks", true));
                                    break;

                                case EntityRule.TYPE_SOUND:
                                    if (jaction.has("uri"))
                                        FragmentRule.this.sound = Uri.parse(jaction.getString("uri"));
                                    boolean loop = jaction.optBoolean("loop");
                                    boolean alarm = jaction.optBoolean("alarm");
                                    int duration = jaction.optInt("duration", 0);
                                    cbLoop.setChecked(loop);
                                    cbAlarm.setChecked(alarm);
                                    etAlarmDuration.setEnabled(alarm);
                                    etAlarmDuration.setText(duration == 0 ? null : Integer.toString(duration));
                                    break;

                                case EntityRule.TYPE_NOTES:
                                    etNotes.setText(jaction.getString("notes"));
                                    btnColorNotes.setColor(
                                            !jaction.has("color") || jaction.isNull("color")
                                                    ? null : jaction.getInt("color"));
                                    break;

                                case EntityRule.TYPE_URL:
                                    etUrl.setText(jaction.getString("url"));
                                    etContent.setText(jaction.getString("body"));
                                    String method = jaction.optString("method");
                                    if (TextUtils.isEmpty(method))
                                        method = "GET";
                                    int pos = Arrays.asList(getResources().getStringArray(R.array.httpMethodNames))
                                            .indexOf(method);
                                    if (pos >= 0)
                                        spUrlMethod.setSelection(pos);
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
                    } else {
                        spScheduleDayStart.setSelection(savedInstanceState.getInt("fair:start"));
                        spScheduleDayEnd.setSelection(savedInstanceState.getInt("fair:end"));
                        spAction.setSelection(savedInstanceState.getInt("fair:action"));
                        spImportance.setSelection(savedInstanceState.getInt("fair:importance"));
                        btnFolder.setTag(savedInstanceState.getLong("fair:target"));
                        btnFolder.setText(savedInstanceState.getCharSequence("fair:name"));
                        spIdent.setSelection(savedInstanceState.getInt("fair:identity"));
                        spAnswer.setSelection(savedInstanceState.getInt("fair:answer"));

                        Action action = adapterAction.getItem(spAction.getSelectedItemPosition());
                        if (action != null)
                            showActionParameters(action.type);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                } finally {
                    grpReady.setVisibility(View.VISIBLE);
                    grpAge.setVisibility(cbDaily.isChecked() ? View.VISIBLE : View.GONE);
                    if (id < 0)
                        bottom_navigation.getMenu().removeItem(R.id.action_delete);
                    bottom_navigation.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, rargs, "rule:get");
    }

    private void showActionParameters(int type) {
        grpHide.setVisibility(type == EntityRule.TYPE_HIDE ? View.VISIBLE : View.GONE);
        grpSnooze.setVisibility(type == EntityRule.TYPE_SNOOZE ? View.VISIBLE : View.GONE);
        grpFlag.setVisibility(type == EntityRule.TYPE_FLAG ? View.VISIBLE : View.GONE);
        grpImportance.setVisibility(type == EntityRule.TYPE_IMPORTANCE ? View.VISIBLE : View.GONE);
        grpKeyword.setVisibility(type == EntityRule.TYPE_KEYWORD ? View.VISIBLE : View.GONE);
        grpMove.setVisibility(type == EntityRule.TYPE_MOVE || type == EntityRule.TYPE_COPY ? View.VISIBLE : View.GONE);
        grpMoveProp.setVisibility(type == EntityRule.TYPE_MOVE ? View.VISIBLE : View.GONE);
        grpAnswer.setVisibility(type == EntityRule.TYPE_ANSWER ? View.VISIBLE : View.GONE);
        grpTts.setVisibility(type == EntityRule.TYPE_TTS ? View.VISIBLE : View.GONE);
        grpSound.setVisibility(type == EntityRule.TYPE_SOUND ? View.VISIBLE : View.GONE);
        grpAutomation.setVisibility(type == EntityRule.TYPE_AUTOMATION ? View.VISIBLE : View.GONE);
        grpDelete.setVisibility(type == EntityRule.TYPE_DELETE ? View.VISIBLE : View.GONE);
        grpLocalOnly.setVisibility(type == EntityRule.TYPE_LOCAL_ONLY ? View.VISIBLE : View.GONE);
        grpNotes.setVisibility(type == EntityRule.TYPE_NOTES ? View.VISIBLE : View.GONE);
        grpUrl.setVisibility(type == EntityRule.TYPE_URL ? View.VISIBLE : View.GONE);
        grpSummarize.setVisibility(type == EntityRule.TYPE_SUMMARIZE ? View.VISIBLE : View.GONE);
    }

    private void onActionDelete() {
        Bundle args = new Bundle();
        args.putString("question", getString(R.string.title_ask_delete_rule));
        args.putBoolean("warning", true);

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(args);
        fragment.setTargetFragment(FragmentRule.this, REQUEST_DELETE);
        fragment.show(getParentFragmentManager(), "answer:delete");
    }

    private void onActionCheck() {
        try {
            JSONObject jcondition = getCondition();
            JSONObject jaction = getAction();

            Bundle args = new Bundle();
            args.putLong("folder", folder);
            args.putString("name", etName.getText().toString());
            args.putBoolean("daily", cbDaily.isChecked());
            args.putString("condition", jcondition.toString());
            args.putString("action", jaction.toString());

            FragmentDialogRuleCheck fragment = new FragmentDialogRuleCheck();
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
            Bundle args = new Bundle();
            args.putLong("id", id);
            args.putLong("folder", folder);
            args.putString("name", etName.getText().toString());
            args.putString("group", etGroup.getText().toString().trim());
            args.putString("order", etOrder.getText().toString());
            args.putBoolean("enabled", cbEnabled.isChecked());
            args.putBoolean("daily", cbDaily.isChecked());
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
                    String group = args.getString("group");
                    String order = args.getString("order");
                    boolean enabled = args.getBoolean("enabled");
                    boolean daily = args.getBoolean("daily");
                    boolean stop = args.getBoolean("stop");
                    String condition = args.getString("condition");
                    String action = args.getString("action");

                    if (TextUtils.isEmpty(name))
                        throw new IllegalArgumentException(context.getString(R.string.title_rule_name_missing));

                    if (TextUtils.isEmpty(group))
                        group = null;

                    JSONObject jcondition = new JSONObject(condition);
                    JSONObject jsender = jcondition.optJSONObject("sender");
                    JSONObject jrecipient = jcondition.optJSONObject("recipient");
                    JSONObject jsubject = jcondition.optJSONObject("subject");
                    JSONObject jheader = jcondition.optJSONObject("header");
                    JSONObject jbody = jcondition.optJSONObject("body");
                    JSONObject jdate = jcondition.optJSONObject("date");
                    JSONObject jschedule = jcondition.optJSONObject("schedule");

                    JSONObject jaction = new JSONObject(action);
                    int type = jaction.getInt("type");
                    if (type == EntityRule.TYPE_NOTES) {
                        String notes = jaction.optString("notes");
                        if (notes.startsWith(EntityRule.JSOUP_PREFIX)) {
                            jcondition.put("notes_jsoup", true);
                            condition = jcondition.toString();
                        }
                    }

                    if (jsender == null &&
                            jrecipient == null &&
                            jsubject == null &&
                            !jcondition.optBoolean("attachments") &&
                            jheader == null &&
                            jbody == null &&
                            jdate == null &&
                            jschedule == null &&
                            !jcondition.has("younger") &&
                            !jcondition.has("expression"))
                        throw new IllegalArgumentException(context.getString(R.string.title_rule_condition_missing));

                    if (TextUtils.isEmpty(order))
                        order = "10";

                    DB db = DB.getInstance(context);
                    if (id < 0) {
                        EntityRule rule = new EntityRule();
                        rule.folder = folder;
                        rule.name = name;
                        rule.group = group;
                        rule.order = Integer.parseInt(order);
                        rule.enabled = enabled;
                        rule.daily = daily;
                        rule.stop = stop;
                        rule.condition = condition;
                        rule.action = action;
                        rule.validate(context);
                        rule.id = db.rule().insertRule(rule);
                    } else {
                        EntityRule rule = db.rule().getRule(id);
                        rule.folder = folder;
                        rule.name = name;
                        rule.group = group;
                        rule.order = Integer.parseInt(order);
                        rule.enabled = enabled;
                        rule.daily = daily;
                        rule.stop = stop;
                        rule.condition = condition;
                        rule.action = action;
                        rule.validate((context));
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
                        Helper.setSnackbarOptions(
                                        Snackbar.make(view, new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG))
                                .show();
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

        JSONObject jgeneral = new JSONObject();

        String age = etAge.getText().toString().trim();
        if (!TextUtils.isEmpty(age) && TextUtils.isDigitsOnly(age))
            try {
                jgeneral.put("age", Integer.parseInt(age));
            } catch (Throwable ex) {
                Log.e(ex);
            }

        jcondition.put("general", jgeneral);

        String sender = etSender.getText().toString();
        boolean known = cbKnownSender.isChecked();
        if (!TextUtils.isEmpty(sender) || known) {
            JSONObject jsender = new JSONObject();
            jsender.put("not", cbSenderNot.isChecked());
            jsender.put("value", sender);
            jsender.put("regex", cbSender.isChecked());
            jsender.put("known", known);
            jcondition.put("sender", jsender);
        }

        String recipient = etRecipient.getText().toString();
        if (!TextUtils.isEmpty(recipient)) {
            JSONObject jrecipient = new JSONObject();
            jrecipient.put("not", cbRecipientNot.isChecked());
            jrecipient.put("value", recipient);
            jrecipient.put("regex", cbRecipient.isChecked());
            jcondition.put("recipient", jrecipient);
        }

        String subject = etSubject.getText().toString();
        if (!TextUtils.isEmpty(subject)) {
            JSONObject jsubject = new JSONObject();
            jsubject.put("not", cbSubjectNot.isChecked());
            jsubject.put("value", subject);
            jsubject.put("regex", cbSubject.isChecked());
            jcondition.put("subject", jsubject);
        }

        jcondition.put("attachments", cbAttachments.isChecked());
        jcondition.put("mimetype", etMimeType.getText().toString().trim());

        String header = etHeader.getText().toString();
        if (!TextUtils.isEmpty(header)) {
            JSONObject jheader = new JSONObject();
            jheader.put("not", cbHeaderNot.isChecked());
            jheader.put("value", header);
            jheader.put("regex", cbHeader.isChecked());
            jcondition.put("header", jheader);
        }

        String body = etBody.getText().toString();
        if (!TextUtils.isEmpty(body)) {
            JSONObject jbody = new JSONObject();
            jbody.put("not", cbBodyNot.isChecked());
            jbody.put("value", body);
            jbody.put("regex", cbBody.isChecked());
            jbody.put("skip_quotes", cbSkipQuotes.isChecked());
            jcondition.put("body", jbody);
        }

        Object hafter = tvDateAfter.getTag();
        Object hbefore = tvDateBefore.getTag();

        long after = (hafter == null ? 0 : (long) hafter);
        long before = (hbefore == null ? 0 : (long) hbefore);

        if (after != before) {
            JSONObject jdate = new JSONObject();
            if (after != 0)
                jdate.put("after", after);
            if (before != 0)
                jdate.put("before", before);
            jcondition.put("date", jdate);
        }

        int dstart = spScheduleDayStart.getSelectedItemPosition();
        int dend = spScheduleDayEnd.getSelectedItemPosition();

        Object hstart = tvScheduleHourStart.getTag();
        Object hend = tvScheduleHourEnd.getTag();
        if (hstart == null)
            hstart = 0;
        if (hend == null)
            hend = 0;

        boolean all = cbEveryDay.isChecked();
        int start = (all ? 0 : dstart) * 24 * 60 + (int) hstart;
        int end = (all ? 0 : dend) * 24 * 60 + (int) hend;

        if (start != end) {
            JSONObject jschedule = new JSONObject();
            jschedule.put("start", start);
            jschedule.put("end", end);
            jschedule.put("all", all);
            jcondition.put("schedule", jschedule);
        }

        String younger = etYounger.getText().toString().trim();
        if (!TextUtils.isEmpty(younger) && TextUtils.isDigitsOnly(younger))
            try {
                jcondition.put("younger", Integer.parseInt(younger));
            } catch (Throwable ex) {
                Log.e(ex);
            }

        String expression = etExpression.getText().toString().trim();
        if (!TextUtils.isEmpty(expression))
            jcondition.put("expression", expression);

        return jcondition;
    }

    private JSONObject getAction() throws JSONException {
        JSONObject jaction = new JSONObject();

        Action action = (Action) spAction.getSelectedItem();
        if (action != null) {
            jaction.put("type", action.type);
            switch (action.type) {
                case EntityRule.TYPE_HIDE:
                    jaction.put("seen", cbHideSeen.isChecked());
                    break;

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
                    jaction.put("set", rgKeyword.getCheckedRadioButtonId() == R.id.keyword_add);
                    break;

                case EntityRule.TYPE_MOVE:
                case EntityRule.TYPE_COPY:
                    Object tag = btnFolder.getTag();
                    jaction.put("target", tag instanceof Long ? (long) tag : -1);
                    if (action.type == EntityRule.TYPE_MOVE) {
                        jaction.put("create", etMoveCreate.getText().toString().trim());
                        jaction.put("seen", cbMoveSeen.isChecked());
                        jaction.put("thread", cbMoveThread.isChecked());
                    }
                    break;

                case EntityRule.TYPE_ANSWER:
                    EntityIdentity identity = (EntityIdentity) spIdent.getSelectedItem();
                    EntityAnswer answer = (EntityAnswer) spAnswer.getSelectedItem();
                    jaction.put("identity", identity == null ? -1 : identity.id);
                    jaction.put("answer", answer == null || answer.id == null ? -1 : answer.id);
                    jaction.put("answer_subject", cbAnswerSubject.isChecked());
                    jaction.put("original_text", cbOriginalText.isChecked());
                    jaction.put("attachments", cbWithAttachments.isChecked());
                    jaction.put("to", etTo.getText().toString().trim());
                    jaction.put("resend", cbResend.isChecked());
                    jaction.put("attached", cbAttached.isChecked());
                    jaction.put("cc", cbCc.isChecked());
                    jaction.put("checks", cbChecks.isChecked());
                    break;

                case EntityRule.TYPE_SOUND:
                    boolean loop = cbLoop.isChecked();
                    boolean alarm = cbAlarm.isChecked();
                    String duration = etAlarmDuration.getText().toString();
                    jaction.put("uri", sound);
                    jaction.put("loop", loop);
                    jaction.put("alarm", alarm);
                    if (alarm && !TextUtils.isEmpty(duration))
                        try {
                            jaction.put("duration", Integer.parseInt(duration));
                        } catch (NumberFormatException ex) {
                            Log.e(ex);
                        }
                    break;

                case EntityRule.TYPE_NOTES:
                    jaction.put("notes", etNotes.getText().toString());
                    int ncolor = btnColorNotes.getColor();
                    if (ncolor != Color.TRANSPARENT)
                        jaction.put("color", ncolor);
                    break;

                case EntityRule.TYPE_URL:
                    jaction.put("url", etUrl.getText().toString().trim());
                    jaction.put("body", etContent.getText().toString());
                    int pos = spUrlMethod.getSelectedItemPosition();
                    String[] methods = getResources().getStringArray(R.array.httpMethodNames);
                    if (pos >= 0 && pos < methods.length)
                        jaction.put("method", methods[pos]);
                    else
                        jaction.put("method", "GET");
                    break;
            }
        }

        return jaction;
    }

    private static class RefData {
        EntityAccount account;
        EntityFolder folder;
        List<String> groups;
        List<EntityIdentity> identities;
        List<EntityAnswer> answers;
    }

    private static class Action {
        int type;
        String name;
        int icon;

        Action(int type, String name, int icon) {
            this.type = type;
            this.name = name;
            this.icon = icon;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
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
                    android.text.format.DateFormat.is24HourFormat(getContext()));
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            getArguments().putInt("minutes", hour * 60 + minute);
            sendResult(RESULT_OK);
        }
    }
}
