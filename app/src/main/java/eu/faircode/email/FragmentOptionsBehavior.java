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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

public class FragmentOptionsBehavior extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swPull;
    private SwitchCompat swAutoScroll;
    private SwitchCompat swSwipeNav;
    private SwitchCompat swAutoExpand;
    private SwitchCompat swAutoClose;
    private SwitchCompat swAutoNext;
    private SwitchCompat swCollapse;
    private SwitchCompat swAutoRead;
    private SwitchCompat swAutoMove;
    private SwitchCompat swAutoResize;
    private Spinner spAutoResize;
    private TextView tvAutoResize;
    private SwitchCompat swPrefixOnce;
    private SwitchCompat swAutoSend;

    private final static String[] RESET_OPTIONS = new String[]{
            "pull", "autoscroll", "swipenav", "autoexpand", "autoclose", "autonext",
            "collapse", "autoread", "automove", "autoresize", "resize", "prefix_once", "autosend"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_behavior, container, false);

        // Get controls

        swPull = view.findViewById(R.id.swPull);
        swAutoScroll = view.findViewById(R.id.swAutoScroll);
        swSwipeNav = view.findViewById(R.id.swSwipeNav);
        swAutoExpand = view.findViewById(R.id.swAutoExpand);
        swAutoClose = view.findViewById(R.id.swAutoClose);
        swAutoNext = view.findViewById(R.id.swAutoNext);
        swCollapse = view.findViewById(R.id.swCollapse);
        swAutoRead = view.findViewById(R.id.swAutoRead);
        swAutoMove = view.findViewById(R.id.swAutoMove);
        swAutoResize = view.findViewById(R.id.swAutoResize);
        spAutoResize = view.findViewById(R.id.spAutoResize);
        tvAutoResize = view.findViewById(R.id.tvAutoResize);
        swPrefixOnce = view.findViewById(R.id.swPrefixOnce);
        swAutoSend = view.findViewById(R.id.swAutoSend);

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

        swAutoExpand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoexpand", checked).apply();
            }
        });

        swAutoClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoclose", checked).apply();
                swAutoNext.setEnabled(!checked);
            }
        });

        swAutoNext.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autonext", checked).apply();
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

        swAutoResize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoresize", checked).apply();
                spAutoResize.setEnabled(checked);
            }
        });

        spAutoResize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int[] values = getResources().getIntArray(R.array.resizeValues);
                prefs.edit().putInt("resize", values[position]).apply();
                tvAutoResize.setText(getString(R.string.title_advanced_resize_pixels, values[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("resize").apply();
            }
        });

        swPrefixOnce.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("prefix_once", checked).apply();
            }
        });

        swAutoSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autosend", !checked).apply();
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
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swPull.setChecked(prefs.getBoolean("pull", true));
        swAutoScroll.setChecked(prefs.getBoolean("autoscroll", false));
        swSwipeNav.setChecked(prefs.getBoolean("swipenav", true));
        swAutoExpand.setChecked(prefs.getBoolean("autoexpand", true));
        swAutoClose.setChecked(prefs.getBoolean("autoclose", true));
        swAutoNext.setChecked(prefs.getBoolean("autonext", false));
        swAutoNext.setEnabled(!swAutoClose.isChecked());
        swCollapse.setChecked(prefs.getBoolean("collapse", false));
        swAutoRead.setChecked(prefs.getBoolean("autoread", false));
        swAutoMove.setChecked(!prefs.getBoolean("automove", false));
        swAutoResize.setChecked(prefs.getBoolean("autoresize", true));

        int resize = prefs.getInt("resize", FragmentCompose.REDUCED_IMAGE_SIZE);
        int[] resizeValues = getResources().getIntArray(R.array.resizeValues);
        for (int pos = 0; pos < resizeValues.length; pos++)
            if (resizeValues[pos] == resize) {
                spAutoResize.setSelection(pos);
                tvAutoResize.setText(getString(R.string.title_advanced_resize_pixels, resizeValues[pos]));
                break;
            }
        spAutoResize.setEnabled(swAutoResize.isChecked());

        swPrefixOnce.setChecked(prefs.getBoolean("prefix_once", false));
        swAutoSend.setChecked(!prefs.getBoolean("autosend", false));
    }
}
