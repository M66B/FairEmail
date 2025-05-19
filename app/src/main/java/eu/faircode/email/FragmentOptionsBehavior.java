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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FragmentOptionsBehavior extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private View view;
    private ImageButton ibHelp;
    private SwitchCompat swRestoreOnLaunch;
    private SwitchCompat swSyncOnlaunch;
    private SwitchCompat swDoubleBack;
    private SwitchCompat swConversationActions;
    private SwitchCompat swConversationActionsReplies;
    private SwitchCompat swLanguageDetection;
    private EditText etDefaultSnooze;
    private SwitchCompat swPull;
    private SwitchCompat swPullAll;
    private SwitchCompat swAutoScroll;
    private SwitchCompat swQuickFilter;
    private SwitchCompat swQuickScroll;
    private SwitchCompat swQuickActions;
    private Button btnSwipes;
    private SeekBar sbSwipeSensitivity;
    private SwitchCompat swFolderNav;
    private SwitchCompat swDoubleTap;
    private SwitchCompat swSwipeNav;
    private SwitchCompat swVolumeNav;
    private SwitchCompat swUpDown;
    private SwitchCompat swReversed;
    private SwitchCompat swSwipeClose;
    private SwitchCompat swSwipeMove;
    private SwitchCompat swAutoExpand;
    private SwitchCompat swExpandFirst;
    private SwitchCompat swExpandAll;
    private SwitchCompat swExpandOne;
    private SwitchCompat swAutoClose;
    private Spinner spSeenDelay;
    private TextView tvAutoSeenHint;
    private TextView tvOnClose;
    private Spinner spOnClose;
    private SwitchCompat swAutoCloseUnseen;
    private SwitchCompat swAutoCloseSend;
    private SwitchCompat swCollapseMarked;
    private Spinner spUndoTimeout;
    private SwitchCompat swCollapseMultiple;
    private SwitchCompat swAutoRead;
    private SwitchCompat swAutoUnflag;
    private SwitchCompat swResetImportance;
    private SwitchCompat swPhotoPicker;
    private SwitchCompat swFlagSnoozed;
    private SwitchCompat swFlagUnsnoozed;
    private SwitchCompat swImportantUnsnoozed;
    private SwitchCompat swAutoImportant;
    private SwitchCompat swResetSnooze;
    private SwitchCompat swAutoBlockSender;
    private SwitchCompat swAutoHideAnswer;
    private SwitchCompat swSwipeReply;
    private SwitchCompat swMoveThreadAll;
    private SwitchCompat swMoveThreadSent;
    private SwitchCompat swSwipeTrashAll;
    private SwitchCompat swGmailDeleteAll;
    private Button btnDefaultFolder;
    private TextView tvDefaultFolder;

    final static int MAX_SWIPE_SENSITIVITY = 10;
    final static int DEFAULT_SWIPE_SENSITIVITY = 6;

    final static int REQUEST_DEFAULT_FOLDER = 1;

    final static List<String> RESET_OPTIONS = Collections.unmodifiableList(Arrays.asList(
            "restore_on_launch", "sync_on_launch", "double_back", "conversation_actions", "conversation_actions_replies", "language_detection",
            "photo_picker", "default_snooze",
            "pull", "pull_all", "autoscroll", "quick_filter", "quick_scroll", "quick_actions", "swipe_sensitivity", "foldernav",
            "doubletap", "swipenav", "volumenav", "updown", "reversed", "swipe_close", "swipe_move",
            "autoexpand", "expand_first", "expand_all", "expand_one", "collapse_multiple",
            "seen_delay",
            "autoclose", "onclose", "autoclose_unseen", "autoclose_send", "collapse_marked",
            "undo_timeout",
            "autoread", "flag_snoozed", "flag_unsnoozed", "autounflag", "important_unsnoozed", "auto_important", "reset_importance",
            "reset_snooze", "auto_block_sender", "auto_hide_answer", "swipe_reply",
            "move_thread_all", "move_thread_sent", "swipe_trash_all", "gmail_delete_all",
            "default_folder"
    ));

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_options_behavior, container, false);

        // Get controls

        ibHelp = view.findViewById(R.id.ibHelp);
        swRestoreOnLaunch = view.findViewById(R.id.swRestoreOnLaunch);
        swSyncOnlaunch = view.findViewById(R.id.swSyncOnlaunch);
        swDoubleBack = view.findViewById(R.id.swDoubleBack);
        swConversationActions = view.findViewById(R.id.swConversationActions);
        swConversationActionsReplies = view.findViewById(R.id.swConversationActionsReplies);
        swLanguageDetection = view.findViewById(R.id.swLanguageDetection);
        etDefaultSnooze = view.findViewById(R.id.etDefaultSnooze);
        swPull = view.findViewById(R.id.swPull);
        swPullAll = view.findViewById(R.id.swPullAll);
        swAutoScroll = view.findViewById(R.id.swAutoScroll);
        swQuickFilter = view.findViewById(R.id.swQuickFilter);
        swQuickScroll = view.findViewById(R.id.swQuickScroll);
        swQuickActions = view.findViewById(R.id.swQuickActions);
        btnSwipes = view.findViewById(R.id.btnSwipes);
        sbSwipeSensitivity = view.findViewById(R.id.sbSwipeSensitivity);
        swFolderNav = view.findViewById(R.id.swFolderNav);
        swDoubleTap = view.findViewById(R.id.swDoubleTap);
        swSwipeNav = view.findViewById(R.id.swSwipeNav);
        swVolumeNav = view.findViewById(R.id.swVolumeNav);
        swUpDown = view.findViewById(R.id.swUpDown);
        swReversed = view.findViewById(R.id.swReversed);
        swSwipeClose = view.findViewById(R.id.swSwipeClose);
        swSwipeMove = view.findViewById(R.id.swSwipeMove);
        swAutoExpand = view.findViewById(R.id.swAutoExpand);
        swExpandFirst = view.findViewById(R.id.swExpandFirst);
        swExpandAll = view.findViewById(R.id.swExpandAll);
        swExpandOne = view.findViewById(R.id.swExpandOne);
        swCollapseMultiple = view.findViewById(R.id.swCollapseMultiple);
        spSeenDelay = view.findViewById(R.id.spSeenDelay);
        tvAutoSeenHint = view.findViewById(R.id.tvAutoSeenHint);
        swAutoClose = view.findViewById(R.id.swAutoClose);
        tvOnClose = view.findViewById(R.id.tvOnClose);
        spOnClose = view.findViewById(R.id.spOnClose);
        swAutoCloseUnseen = view.findViewById(R.id.swAutoCloseUnseen);
        swAutoCloseSend = view.findViewById(R.id.swAutoCloseSend);
        swCollapseMarked = view.findViewById(R.id.swCollapseMarked);
        spUndoTimeout = view.findViewById(R.id.spUndoTimeout);
        swAutoRead = view.findViewById(R.id.swAutoRead);
        swAutoUnflag = view.findViewById(R.id.swAutoUnflag);
        swResetImportance = view.findViewById(R.id.swResetImportance);
        swPhotoPicker = view.findViewById(R.id.swPhotoPicker);
        swFlagSnoozed = view.findViewById(R.id.swFlagSnoozed);
        swFlagUnsnoozed = view.findViewById(R.id.swFlagUnsnoozed);
        swImportantUnsnoozed = view.findViewById(R.id.swImportantUnsnoozed);
        swAutoImportant = view.findViewById(R.id.swAutoImportant);
        swResetSnooze = view.findViewById(R.id.swResetSnooze);
        swAutoBlockSender = view.findViewById(R.id.swAutoBlockSender);
        swAutoHideAnswer = view.findViewById(R.id.swAutoHideAnswer);
        swSwipeReply = view.findViewById(R.id.swSwipeReply);
        swMoveThreadAll = view.findViewById(R.id.swMoveThreadAll);
        swMoveThreadSent = view.findViewById(R.id.swMoveThreadSent);
        swSwipeTrashAll = view.findViewById(R.id.swSwipeTrashAll);
        swGmailDeleteAll = view.findViewById(R.id.swGmailDeleteAll);
        btnDefaultFolder = view.findViewById(R.id.btnDefaultFolder);
        tvDefaultFolder = view.findViewById(R.id.tvDefaultFolder);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int[] undoValues = getResources().getIntArray(R.array.undoValues);

        ibHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "Options:behavior"), false);
            }
        });

        swDoubleBack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("double_back", checked).apply();
            }
        });

        swRestoreOnLaunch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("restore_on_launch", checked).apply();
            }
        });

        swSyncOnlaunch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sync_on_launch", checked).apply();
            }
        });

        swConversationActions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("conversation_actions", checked).apply();
                swConversationActionsReplies.setEnabled(checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
            }
        });

        swConversationActionsReplies.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("conversation_actions_replies", checked).apply();
            }
        });

        swLanguageDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("language_detection", checked).apply();
            }
        });

        etDefaultSnooze.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int default_snooze = (s.length() > 0 ? Integer.parseInt(s.toString()) : 1);
                    if (default_snooze == 1)
                        prefs.edit().remove("default_snooze").apply();
                    else
                        prefs.edit().putInt("default_snooze", default_snooze).apply();
                } catch (NumberFormatException ex) {
                    Log.e(ex);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        swPull.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("pull", checked).apply();
                swPullAll.setEnabled(checked);
            }
        });

        swPullAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("pull_all", checked).apply();
            }
        });

        swAutoScroll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoscroll", checked).apply();
            }
        });

        swQuickFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("quick_filter", checked).apply();
            }
        });

        swQuickScroll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("quick_scroll", checked).apply();
            }
        });

        swQuickActions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("quick_actions", checked).apply();
            }
        });

        btnSwipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentDialogSwipes().show(getParentFragmentManager(), "setup:swipe");
            }
        });

        sbSwipeSensitivity.setMax(MAX_SWIPE_SENSITIVITY);

        sbSwipeSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("swipe_sensitivity", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        swFolderNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("foldernav", checked).apply();
            }
        });

        swDoubleTap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("doubletap", checked).apply();
            }
        });

        swSwipeNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("swipenav", checked).apply();
            }
        });

        swVolumeNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("volumenav", checked).apply();
            }
        });

        swUpDown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("updown", checked).apply();
            }
        });

        swReversed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("reversed", checked).apply();
            }
        });

        swSwipeClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("swipe_close", checked).apply();
            }
        });

        swSwipeMove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("swipe_move", checked).apply();
            }
        });

        swAutoExpand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoexpand", checked).apply();
                swExpandFirst.setEnabled(checked);
            }
        });

        swExpandFirst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("expand_first", checked).apply();
            }
        });

        swExpandAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("expand_all", checked).apply();
                swExpandOne.setEnabled(!checked);
            }
        });

        swExpandOne.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("expand_one", checked).apply();
            }
        });

        swCollapseMultiple.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("collapse_multiple", checked).apply();
            }
        });

        spSeenDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int value = undoValues[position];
                prefs.edit().putInt("seen_delay", value).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("seen_delay").apply();
            }
        });

        tvAutoSeenHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(v.getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_VIEW_ACCOUNTS));
            }
        });

        swAutoClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoclose", checked).apply();
                tvOnClose.setEnabled(!checked);
                spOnClose.setEnabled(!checked);
            }
        });

        spOnClose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] values = getResources().getStringArray(R.array.onCloseValues);
                String value = values[position];
                if (TextUtils.isEmpty(value))
                    prefs.edit().remove("onclose").apply();
                else
                    prefs.edit().putString("onclose", value).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("onclose").apply();
            }
        });

        swAutoCloseUnseen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoclose_unseen", checked).apply();
            }
        });

        swAutoCloseSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoclose_send", checked).apply();
            }
        });

        swCollapseMarked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("collapse_marked", checked).apply();
            }
        });

        spUndoTimeout.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int value = undoValues[position];
                prefs.edit().putInt("undo_timeout", value).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("undo_timeout").apply();
            }
        });

        swAutoRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoread", checked).apply();
            }
        });

        swAutoUnflag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autounflag", checked).apply();
            }
        });

        swResetImportance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("reset_importance", checked).apply();
            }
        });

        swPhotoPicker.setVisibility(Helper.hasPhotoPicker() ? View.VISIBLE : View.GONE);
        swPhotoPicker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("photo_picker", checked).apply();
            }
        });

        swFlagSnoozed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("flag_snoozed", checked).apply();
                swFlagUnsnoozed.setEnabled(checked);
            }
        });

        swFlagUnsnoozed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("flag_unsnoozed", checked).apply();
            }
        });

        swImportantUnsnoozed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("important_unsnoozed", checked).apply();
            }
        });

        swAutoImportant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_important", checked).apply();
            }
        });

        swResetSnooze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("reset_snooze", checked).apply();
            }
        });

        swAutoBlockSender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_block_sender", checked).apply();
            }
        });

        swAutoHideAnswer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_hide_answer", checked).apply();
            }
        });

        swSwipeReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("swipe_reply", checked).apply();
            }
        });

        swMoveThreadAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("move_thread_all", checked).apply();
                swMoveThreadSent.setEnabled(!checked);
            }
        });

        swMoveThreadSent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("move_thread_sent", checked).apply();
            }
        });

        swSwipeTrashAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("swipe_trash_all", checked).apply();
            }
        });

        swGmailDeleteAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("gmail_delete_all", checked).apply();
            }
        });

        Intent tree = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Helper.openAdvanced(getContext(), tree);
        PackageManager pm = getContext().getPackageManager();
        btnDefaultFolder.setEnabled(tree.resolveActivity(pm) != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        btnDefaultFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Helper.getChooser(getContext(), tree), REQUEST_DEFAULT_FOLDER);
            }
        });

        // Initialize
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_DEFAULT_FOLDER:
                    if (resultCode == RESULT_OK && data != null)
                        onDefaultFolder(data.getData());
                    else
                        onDefaultFolder(null);
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (!RESET_OPTIONS.contains(key))
            return;

        if ("default_snooze".equals(key))
            return;

        getMainHandler().removeCallbacks(update);
        getMainHandler().postDelayed(update, FragmentOptions.DELAY_SETOPTIONS);
    }

    private Runnable update = new RunnableEx("behavior") {
        @Override
        protected void delegate() {
            setOptions();
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_default) {
            FragmentOptions.reset(getContext(), RESET_OPTIONS, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOptions() {
        try {
            if (view == null || getContext() == null)
                return;

            int[] undoValues = getResources().getIntArray(R.array.undoValues);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            swRestoreOnLaunch.setChecked(prefs.getBoolean("restore_on_launch", true));
            swSyncOnlaunch.setChecked(prefs.getBoolean("sync_on_launch", false));
            swDoubleBack.setChecked(prefs.getBoolean("double_back", false));
            swConversationActions.setChecked(prefs.getBoolean("conversation_actions", Helper.isGoogle()));
            swConversationActions.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
            swConversationActionsReplies.setChecked(prefs.getBoolean("conversation_actions_replies", true));
            swConversationActionsReplies.setEnabled(swConversationActions.isChecked() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
            swLanguageDetection.setChecked(prefs.getBoolean("language_detection", false));

            int default_snooze = prefs.getInt("default_snooze", 1);
            etDefaultSnooze.setText(default_snooze == 1 ? null : Integer.toString(default_snooze));
            etDefaultSnooze.setHint("1");

            swPull.setChecked(prefs.getBoolean("pull", true));
            swPullAll.setChecked(prefs.getBoolean("pull_all", false));
            swPullAll.setEnabled(swPull.isChecked());
            swAutoScroll.setChecked(prefs.getBoolean("autoscroll", false));
            swQuickFilter.setChecked(prefs.getBoolean("quick_filter", false));
            swQuickScroll.setChecked(prefs.getBoolean("quick_scroll", true));
            swQuickActions.setChecked(prefs.getBoolean("quick_actions", true));

            int swipe_sensitivity = prefs.getInt("swipe_sensitivity", DEFAULT_SWIPE_SENSITIVITY);
            sbSwipeSensitivity.setProgress(swipe_sensitivity);

            swFolderNav.setChecked(prefs.getBoolean("foldernav", false));

            swDoubleTap.setChecked(prefs.getBoolean("doubletap", false));
            swSwipeNav.setChecked(prefs.getBoolean("swipenav", true));
            swVolumeNav.setChecked(prefs.getBoolean("volumenav", false));
            swUpDown.setChecked(prefs.getBoolean("updown", true));
            swReversed.setChecked(prefs.getBoolean("reversed", false));
            swSwipeClose.setChecked(prefs.getBoolean("swipe_close", false));
            swSwipeMove.setChecked(prefs.getBoolean("swipe_move", false));

            swAutoExpand.setChecked(prefs.getBoolean("autoexpand", true));
            swExpandFirst.setChecked(prefs.getBoolean("expand_first", true));
            swExpandFirst.setEnabled(swAutoExpand.isChecked());
            swExpandAll.setChecked(prefs.getBoolean("expand_all", false));
            swExpandOne.setChecked(prefs.getBoolean("expand_one", true));
            swExpandOne.setEnabled(!swExpandAll.isChecked());
            swCollapseMultiple.setChecked(prefs.getBoolean("collapse_multiple", true));

            int seen_delay = prefs.getInt("seen_delay", 0);
            for (int pos = 0; pos < undoValues.length; pos++)
                if (undoValues[pos] == seen_delay) {
                    spSeenDelay.setSelection(pos);
                    break;
                }

            swAutoClose.setChecked(prefs.getBoolean("autoclose", true));

            String onClose = prefs.getString("onclose", "");
            String[] onCloseValues = getResources().getStringArray(R.array.onCloseValues);
            for (int pos = 0; pos < onCloseValues.length; pos++)
                if (onCloseValues[pos].equals(onClose)) {
                    spOnClose.setSelection(pos);
                    break;
                }

            tvOnClose.setEnabled(!swAutoClose.isChecked());
            spOnClose.setEnabled(!swAutoClose.isChecked());

            swAutoCloseUnseen.setChecked(prefs.getBoolean("autoclose_unseen", false));
            swAutoCloseSend.setChecked(prefs.getBoolean("autoclose_send", false));
            swCollapseMarked.setChecked(prefs.getBoolean("collapse_marked", true));

            int undo_timeout = prefs.getInt("undo_timeout", 5000);
            for (int pos = 0; pos < undoValues.length; pos++)
                if (undoValues[pos] == undo_timeout) {
                    spUndoTimeout.setSelection(pos);
                    break;
                }

            swAutoRead.setChecked(prefs.getBoolean("autoread", false));
            swAutoUnflag.setChecked(prefs.getBoolean("autounflag", false));
            swResetImportance.setChecked(prefs.getBoolean("reset_importance", false));

            swPhotoPicker.setChecked(prefs.getBoolean("photo_picker", true));
            swFlagSnoozed.setChecked(prefs.getBoolean("flag_snoozed", false));
            swFlagUnsnoozed.setChecked(prefs.getBoolean("flag_unsnoozed", false));
            swFlagUnsnoozed.setEnabled(swFlagSnoozed.isChecked());
            swImportantUnsnoozed.setChecked(prefs.getBoolean("important_unsnoozed", false));
            swAutoImportant.setChecked(prefs.getBoolean("auto_important", false));
            swResetSnooze.setChecked(prefs.getBoolean("reset_snooze", true));
            swAutoBlockSender.setChecked(prefs.getBoolean("auto_block_sender", true));
            swAutoHideAnswer.setChecked(prefs.getBoolean("auto_hide_answer", false));
            swSwipeReply.setChecked(prefs.getBoolean("swipe_reply", false));

            swMoveThreadAll.setChecked(prefs.getBoolean("move_thread_all", false));
            swMoveThreadSent.setChecked(prefs.getBoolean("move_thread_sent", false));
            swMoveThreadSent.setEnabled(!swMoveThreadAll.isChecked());
            swSwipeTrashAll.setChecked(prefs.getBoolean("swipe_trash_all", true));
            swGmailDeleteAll.setChecked(prefs.getBoolean("gmail_delete_all", false));

            tvDefaultFolder.setText(prefs.getString("default_folder", null));
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onDefaultFolder(Uri uri) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (uri == null)
            prefs.edit().remove("default_folder").apply();
        else
            prefs.edit().putString("default_folder", uri.toString()).apply();
    }
}
