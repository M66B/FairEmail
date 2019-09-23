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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

public class FragmentOptionsBehavior extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swPull;
    private SwitchCompat swAutoScroll;
    private SwitchCompat swSwipeNav;
    private SwitchCompat swReversed;
    private SwitchCompat swDoubleTap;
    private SwitchCompat swAutoExpand;
    private SwitchCompat swExpandOne;
    private SwitchCompat swAutoClose;
    private Spinner spOnClose;
    private SwitchCompat swCollapse;
    private SwitchCompat swAutoRead;
    private SwitchCompat swAutoMove;
    private SwitchCompat swDiscardDelete;
    private SwitchCompat swDisableTracking;

    private final static String[] RESET_OPTIONS = new String[]{
            "pull", "autoscroll", "swipenav", "reversed", "doubletap", "autoexpand", "expand_one", "autoclose", "onclose",
            "collapse", "autoread", "automove", "discard_delete", "disable_tracking"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_behavior, container, false);

        // Get controls

        swPull = view.findViewById(R.id.swPull);
        swAutoScroll = view.findViewById(R.id.swAutoScroll);
        swSwipeNav = view.findViewById(R.id.swSwipeNav);
        swReversed = view.findViewById(R.id.swReversed);
        swDoubleTap = view.findViewById(R.id.swDoubleTap);
        swAutoExpand = view.findViewById(R.id.swAutoExpand);
        swExpandOne = view.findViewById(R.id.swExpandOne);
        swAutoClose = view.findViewById(R.id.swAutoClose);
        spOnClose = view.findViewById(R.id.spOnClose);
        swCollapse = view.findViewById(R.id.swCollapse);
        swAutoRead = view.findViewById(R.id.swAutoRead);
        swAutoMove = view.findViewById(R.id.swAutoMove);
        swDiscardDelete = view.findViewById(R.id.swDiscardDelete);
        swDisableTracking = view.findViewById(R.id.swDisableTracking);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

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

        swSwipeNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("swipenav", checked).apply();
            }
        });

        swReversed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("reversed", checked).apply();
            }
        });

        swDoubleTap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("doubletap", checked).apply();
            }
        });

        swAutoExpand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoexpand", checked).apply();
            }
        });

        swExpandOne.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("expand_one", checked).apply();
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

        swCollapse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("collapse", checked).apply();
            }
        });

        swAutoRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoread", checked).apply();
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

        swDisableTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("disable_tracking", checked).apply();
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

        swPull.setChecked(prefs.getBoolean("pull", true));
        swAutoScroll.setChecked(prefs.getBoolean("autoscroll", false));
        swSwipeNav.setChecked(prefs.getBoolean("swipenav", true));
        swReversed.setChecked(prefs.getBoolean("reversed", false));
        swDoubleTap.setChecked(prefs.getBoolean("doubletap", false));
        swAutoExpand.setChecked(prefs.getBoolean("autoexpand", true));
        swExpandOne.setChecked(prefs.getBoolean("expand_one", true));
        swAutoClose.setChecked(prefs.getBoolean("autoclose", true));

        String onClose = prefs.getString("onclose", "");
        String[] onCloseValues = getResources().getStringArray(R.array.onCloseValues);
        for (int pos = 0; pos < onCloseValues.length; pos++)
            if (onCloseValues[pos].equals(onClose)) {
                spOnClose.setSelection(pos);
                break;
            }

        spOnClose.setEnabled(!swAutoClose.isChecked());

        swCollapse.setChecked(prefs.getBoolean("collapse", false));
        swAutoRead.setChecked(prefs.getBoolean("autoread", false));
        swAutoMove.setChecked(!prefs.getBoolean("automove", false));
        swDiscardDelete.setChecked(prefs.getBoolean("discard_delete", false));
        swDisableTracking.setChecked(prefs.getBoolean("disable_tracking", true));
    }
}
