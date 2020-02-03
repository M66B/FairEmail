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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

public class FragmentOptionsBehavior extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swDoubleBack;
    private SwitchCompat swPull;
    private SwitchCompat swAutoScroll;
    private SwitchCompat swDoubleTap;
    private SwitchCompat swSwipeNav;
    private SwitchCompat swVolumeNav;
    private SwitchCompat swReversed;
    private SwitchCompat swAutoExpand;
    private SwitchCompat swExpandAll;
    private SwitchCompat swExpandOne;
    private SwitchCompat swAutoClose;
    private Spinner spOnClose;
    private SwitchCompat swQuickFilter;
    private SwitchCompat swQuickScroll;
    private SwitchCompat swCollapseMultiple;
    private SwitchCompat swAutoRead;
    private SwitchCompat swAutoUnflag;
    private SwitchCompat swResetImportance;
    private SwitchCompat swAutoMove;
    private SwitchCompat swDiscardDelete;
    private NumberPicker npDefaultSnooze;

    private final static String[] RESET_OPTIONS = new String[]{
            "double_back", "pull", "autoscroll", "doubletap", "swipenav", "volumenav", "reversed",
            "autoexpand", "expand_all", "expand_one", "collapse_multiple",
            "autoclose", "onclose", "quick_filter", "quick_scroll",
            "autoread", "autounflag", "reset_importance", "automove", "discard_delete",
            "default_snooze"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_behavior, container, false);

        // Get controls

        swDoubleBack = view.findViewById(R.id.swDoubleBack);
        swPull = view.findViewById(R.id.swPull);
        swAutoScroll = view.findViewById(R.id.swAutoScroll);
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
        swQuickFilter = view.findViewById(R.id.swQuickFilter);
        swQuickScroll = view.findViewById(R.id.swQuickScroll);
        swAutoRead = view.findViewById(R.id.swAutoRead);
        swAutoUnflag = view.findViewById(R.id.swAutoUnflag);
        swResetImportance = view.findViewById(R.id.swResetImportance);
        swAutoMove = view.findViewById(R.id.swAutoMove);
        swDiscardDelete = view.findViewById(R.id.swDiscardDelete);
        npDefaultSnooze = view.findViewById(R.id.npDefaultSnooze);

        npDefaultSnooze.setMinValue(1);
        npDefaultSnooze.setMaxValue(999);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swDoubleBack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("double_back", checked).apply();
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

        swAutoMove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("automove", !checked).apply();
            }
        });

        swDiscardDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("discard_delete", checked).apply();
            }
        });

        npDefaultSnooze.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                prefs.edit().putInt("default_snooze", newVal).apply();
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
        swPull.setChecked(prefs.getBoolean("pull", true));
        swAutoScroll.setChecked(prefs.getBoolean("autoscroll", true));
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

        swQuickFilter.setChecked(prefs.getBoolean("quick_filter", false));
        swQuickScroll.setChecked(prefs.getBoolean("quick_scroll", true));

        swAutoRead.setChecked(prefs.getBoolean("autoread", false));
        swAutoUnflag.setChecked(prefs.getBoolean("autounflag", false));
        swResetImportance.setChecked(prefs.getBoolean("reset_importance", false));
        swAutoMove.setChecked(!prefs.getBoolean("automove", false));
        swDiscardDelete.setChecked(prefs.getBoolean("discard_delete", false));

        npDefaultSnooze.setValue(prefs.getInt("default_snooze", 1));
    }
}
