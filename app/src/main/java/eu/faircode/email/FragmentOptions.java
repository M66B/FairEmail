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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

public class FragmentOptions extends FragmentEx {
    private SwitchCompat swEnabled;
    private SwitchCompat swAvatars;
    private SwitchCompat swIdenticons;
    private SwitchCompat swCompact;
    private SwitchCompat swPreview;
    private SwitchCompat swLight;
    private SwitchCompat swBrowse;
    private SwitchCompat swSwipe;
    private SwitchCompat swNav;
    private SwitchCompat swInsecure;
    private Spinner spDownload;
    private SwitchCompat swDebug;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);

        View view = inflater.inflate(R.layout.fragment_options, container, false);

        // Get controls
        swEnabled = view.findViewById(R.id.swEnabled);
        swCompact = view.findViewById(R.id.swCompact);
        swAvatars = view.findViewById(R.id.swAvatars);
        swIdenticons = view.findViewById(R.id.swIdenticons);
        swPreview = view.findViewById(R.id.swPreview);
        swLight = view.findViewById(R.id.swLight);
        swBrowse = view.findViewById(R.id.swBrowse);
        swSwipe = view.findViewById(R.id.swSwipe);
        swNav = view.findViewById(R.id.swNav);
        swInsecure = view.findViewById(R.id.swInsecure);
        spDownload = view.findViewById(R.id.spDownload);
        swDebug = view.findViewById(R.id.swDebug);

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swEnabled.setChecked(prefs.getBoolean("enabled", true));
        swEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("enabled", checked).apply();
                if (checked)
                    ServiceSynchronize.start(getContext());
                else
                    ServiceSynchronize.stop(getContext());
            }
        });

        swCompact.setChecked(prefs.getBoolean("compact", false));
        swCompact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("compact", checked).apply();
            }
        });

        swAvatars.setChecked(prefs.getBoolean("avatars", true));
        swAvatars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("avatars", checked).apply();
            }
        });

        swIdenticons.setChecked(prefs.getBoolean("identicons", false));
        swIdenticons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("identicons", checked).apply();
            }
        });

        swPreview.setChecked(prefs.getBoolean("preview", false));
        swPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preview", checked).apply();
            }
        });

        swLight.setChecked(prefs.getBoolean("light", false));
        swLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("light", checked).apply();
            }
        });

        swBrowse.setChecked(prefs.getBoolean("browse", true));
        swBrowse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("browse", checked).apply();
            }
        });

        swSwipe.setChecked(prefs.getBoolean("swipe", true));
        swSwipe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("swipe", checked).apply();
            }
        });

        swNav.setChecked(prefs.getBoolean("navigation", true));
        swNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("navigation", checked).apply();
            }
        });

        swInsecure.setChecked(prefs.getBoolean("insecure", false));
        swInsecure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("insecure", checked).apply();
            }
        });

        int download = prefs.getInt("download", 32768);
        final int[] values = getResources().getIntArray(R.array.downloadValues);
        for (int i = 0; i < values.length; i++)
            if (values[i] == download) {
                spDownload.setSelection(i);
                break;
            }
        spDownload.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt("download", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("download").apply();
            }
        });

        swDebug.setChecked(prefs.getBoolean("debug", false));
        swDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("debug", checked).apply();
                ServiceSynchronize.reload(getContext(), "debug=" + checked);
            }
        });

        swLight.setVisibility(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O ? View.VISIBLE : View.GONE);

        return view;
    }
}
