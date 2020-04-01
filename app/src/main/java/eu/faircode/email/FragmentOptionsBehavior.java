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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class FragmentOptionsBehavior extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swDoubleBack;
    private SwitchCompat swConversationActions;
    private SwitchCompat swConversationActionsReplies;
    private SwitchCompat swLanguageDetection;
    private EditText etDefaultSnooze;
    private SwitchCompat swPull;
    private SwitchCompat swAutoScroll;
    private SwitchCompat swQuickFilter;
    private SwitchCompat swQuickScroll;
    private Button btnSwipes;
    private SwitchCompat swDoubleTap;
    private SwitchCompat swSwipeNav;
    private SwitchCompat swVolumeNav;
    private SwitchCompat swReversed;
    private SwitchCompat swAutoExpand;
    private SwitchCompat swExpandAll;
    private SwitchCompat swExpandOne;
    private SwitchCompat swAutoClose;
    private Spinner spOnClose;
    private SwitchCompat swCollapseMultiple;
    private SwitchCompat swAutoRead;
    private SwitchCompat swFlagSnoozed;
    private SwitchCompat swAutoUnflag;
    private SwitchCompat swAutoImportant;
    private SwitchCompat swResetImportance;
    private SwitchCompat swDiscardDelete;
    private Group grpConversationActions;

    private final static String[] RESET_OPTIONS = new String[]{
            "double_back", "conversation_actions", "conversation_actions_replies", "language_detection",
            "default_snooze",
            "pull", "autoscroll", "quick_filter", "quick_scroll",
            "doubletap", "swipenav", "volumenav", "reversed",
            "autoexpand", "expand_all", "expand_one", "collapse_multiple",
            "autoclose", "onclose",
            "autoread", "flag_snoozed", "autounflag", "auto_important", "reset_importance", "discard_delete",
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_behavior, container, false);

        // Get controls

        swDoubleBack = view.findViewById(R.id.swDoubleBack);
        swConversationActions = view.findViewById(R.id.swConversationActions);
        swConversationActionsReplies = view.findViewById(R.id.swConversationActionsReplies);
        swLanguageDetection = view.findViewById(R.id.swLanguageDetection);
        etDefaultSnooze = view.findViewById(R.id.etDefaultSnooze);
        swPull = view.findViewById(R.id.swPull);
        swAutoScroll = view.findViewById(R.id.swAutoScroll);
        swQuickFilter = view.findViewById(R.id.swQuickFilter);
        swQuickScroll = view.findViewById(R.id.swQuickScroll);
        btnSwipes = view.findViewById(R.id.btnSwipes);
        swDoubleTap = view.findViewById(R.id.swDoubleTap);
        swSwipeNav = view.findViewById(R.id.swSwipeNav);
        swVolumeNav = view.findViewById(R.id.swVolumeNav);
        swReversed = view.findViewById(R.id.swReversed);
        swAutoExpand = view.findViewById(R.id.swAutoExpand);
        swExpandAll = view.findViewById(R.id.swExpandAll);
        swExpandOne = view.findViewById(R.id.swExpandOne);
        swCollapseMultiple = view.findViewById(R.id.swCollapseMultiple);
        swAutoClose = view.findViewById(R.id.swAutoClose);
        spOnClose = view.findViewById(R.id.spOnClose);
        swAutoRead = view.findViewById(R.id.swAutoRead);
        swFlagSnoozed = view.findViewById(R.id.swFlagSnoozed);
        swAutoUnflag = view.findViewById(R.id.swAutoUnflag);
        swAutoImportant = view.findViewById(R.id.swAutoImportant);
        swResetImportance = view.findViewById(R.id.swResetImportance);
        swDiscardDelete = view.findViewById(R.id.swDiscardDelete);
        grpConversationActions = view.findViewById(R.id.grpConversationActions);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swDoubleBack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("double_back", checked).apply();
            }
        });

        swConversationActions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("conversation_actions", checked).apply();
                swConversationActionsReplies.setEnabled(checked);
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

        btnSwipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentDialogSwipes().show(getParentFragmentManager(), "setup:swipe");
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

        swReversed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("reversed", checked).apply();
            }
        });

        swAutoExpand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoexpand", checked).apply();
            }
        });

        swExpandAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("expand_all", checked).apply();
                swExpandOne.setEnabled(!checked);
                swCollapseMultiple.setEnabled(!swExpandOne.isChecked() || swExpandAll.isChecked());
            }
        });

        swExpandOne.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("expand_one", checked).apply();
                swCollapseMultiple.setEnabled(!swExpandOne.isChecked() || swExpandAll.isChecked());
            }
        });

        swCollapseMultiple.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("collapse_multiple", checked).apply();
            }
        });

        swAutoClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoclose", checked).apply();
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

        swAutoRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoread", checked).apply();
            }
        });

        swFlagSnoozed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("flag_snoozed", checked).apply();
            }
        });

        swAutoUnflag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autounflag", checked).apply();
            }
        });

        swAutoImportant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_important", checked).apply();
            }
        });

        swResetImportance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("reset_importance", checked).apply();
            }
        });

        swDiscardDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("discard_delete", checked).apply();
            }
        });

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("default_snooze".equals(key))
            return;

        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            setOptions();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_default:
                onMenuDefault();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuDefault() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        for (String option : RESET_OPTIONS)
            editor.remove(option);
        editor.apply();
        ToastEx.makeText(getContext(), R.string.title_setup_done, Toast.LENGTH_LONG).show();
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swDoubleBack.setChecked(prefs.getBoolean("double_back", true));
        swConversationActions.setChecked(prefs.getBoolean("conversation_actions", true));
        swConversationActionsReplies.setChecked(prefs.getBoolean("conversation_actions_replies", true));
        swConversationActionsReplies.setEnabled(swConversationActions.isChecked());
        swLanguageDetection.setChecked(prefs.getBoolean("language_detection", false));
        swLanguageDetection.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ? View.GONE : View.VISIBLE);

        int default_snooze = prefs.getInt("default_snooze", 1);
        etDefaultSnooze.setText(default_snooze == 1 ? null : Integer.toString(default_snooze));
        etDefaultSnooze.setHint("1");

        swPull.setChecked(prefs.getBoolean("pull", true));
        swAutoScroll.setChecked(prefs.getBoolean("autoscroll", true));
        swQuickFilter.setChecked(prefs.getBoolean("quick_filter", false));
        swQuickScroll.setChecked(prefs.getBoolean("quick_scroll", true));

        swDoubleTap.setChecked(prefs.getBoolean("doubletap", false));
        swSwipeNav.setChecked(prefs.getBoolean("swipenav", true));
        swVolumeNav.setChecked(prefs.getBoolean("volumenav", false));
        swReversed.setChecked(prefs.getBoolean("reversed", false));

        swAutoExpand.setChecked(prefs.getBoolean("autoexpand", true));
        swExpandAll.setChecked(prefs.getBoolean("expand_all", false));
        swExpandOne.setChecked(prefs.getBoolean("expand_one", true));
        swExpandOne.setEnabled(!swExpandAll.isChecked());
        swCollapseMultiple.setChecked(prefs.getBoolean("collapse_multiple", true));
        swCollapseMultiple.setEnabled(!swExpandOne.isChecked() || swExpandAll.isChecked());

        swAutoClose.setChecked(prefs.getBoolean("autoclose", true));

        String onClose = prefs.getString("onclose", "");
        String[] onCloseValues = getResources().getStringArray(R.array.onCloseValues);
        for (int pos = 0; pos < onCloseValues.length; pos++)
            if (onCloseValues[pos].equals(onClose)) {
                spOnClose.setSelection(pos);
                break;
            }

        spOnClose.setEnabled(!swAutoClose.isChecked());

        swAutoRead.setChecked(prefs.getBoolean("autoread", false));
        swFlagSnoozed.setChecked(prefs.getBoolean("flag_snoozed", false));
        swAutoUnflag.setChecked(prefs.getBoolean("autounflag", false));
        swAutoImportant.setChecked(prefs.getBoolean("auto_important", false));
        swResetImportance.setChecked(prefs.getBoolean("reset_importance", false));
        swDiscardDelete.setChecked(prefs.getBoolean("discard_delete", false));

        grpConversationActions.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? View.VISIBLE : View.GONE);
    }

    public static class FragmentDialogSwipes extends FragmentDialogBase {
        private Spinner spLeft;
        private Spinner spRight;
        private ArrayAdapter<EntityFolder> adapter;

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_swipes, null);
            spLeft = dview.findViewById(R.id.spLeft);
            spRight = dview.findViewById(R.id.spRight);

            adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityFolder>());
            adapter.setDropDownViewResource(R.layout.spinner_item1_dropdown);

            spLeft.setAdapter(adapter);
            spRight.setAdapter(adapter);

            List<EntityFolder> folders = FragmentAccount.getFolderActions(getContext());

            EntityFolder trash = new EntityFolder();
            trash.id = 2L;
            trash.name = getString(R.string.title_trash);
            folders.add(1, trash);

            EntityFolder archive = new EntityFolder();
            archive.id = 1L;
            archive.name = getString(R.string.title_archive);
            folders.add(1, archive);

            adapter.addAll(folders);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            int leftPos = prefs.getInt("swipe_left_default", 2); // Trash
            int rightPos = prefs.getInt("swipe_right_default", 1); // Archive

            spLeft.setSelection(leftPos);
            spRight.setSelection(rightPos);

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit()
                                    .putInt("swipe_left_default", spLeft.getSelectedItemPosition())
                                    .putInt("swipe_right_default", spRight.getSelectedItemPosition())
                                    .apply();

                            EntityFolder left = (EntityFolder) spLeft.getSelectedItem();
                            EntityFolder right = (EntityFolder) spRight.getSelectedItem();

                            final Context context = getContext();

                            Bundle args = new Bundle();
                            args.putLong("left", left == null ? 0 : left.id);
                            args.putLong("right", right == null ? 0 : right.id);

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onExecute(Context context, Bundle args) {
                                    long left = args.getLong("left");
                                    long right = args.getLong("right");

                                    DB db = DB.getInstance(context);
                                    try {
                                        db.beginTransaction();

                                        List<EntityAccount> accounts = db.account().getAccounts();
                                        for (EntityAccount account : accounts)
                                            if (account.protocol == EntityAccount.TYPE_IMAP)
                                                db.account().setAccountSwipes(
                                                        account.id,
                                                        getAction(context, left, account.id),
                                                        getAction(context, right, account.id));

                                        db.setTransactionSuccessful();
                                    } finally {
                                        db.endTransaction();
                                    }

                                    return null;
                                }

                                @Override
                                protected void onExecuted(Bundle args, Void data) {
                                    ToastEx.makeText(context, R.string.title_completed, Toast.LENGTH_LONG).show();
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Log.unexpectedError(getParentFragmentManager(), ex);
                                }

                                private Long getAction(Context context, long selection, long account) {
                                    if (selection < 0)
                                        return selection;
                                    else if (selection == 0)
                                        return null;
                                    else {
                                        DB db = DB.getInstance(context);
                                        String type = (selection == 2 ? EntityFolder.TRASH : EntityFolder.ARCHIVE);
                                        EntityFolder archive = db.folder().getFolderByType(account, type);
                                        return (archive == null ? null : archive.id);
                                    }
                                }
                            }.execute(getContext(), getViewLifecycleOwner(), args, "dialog:swipe");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
