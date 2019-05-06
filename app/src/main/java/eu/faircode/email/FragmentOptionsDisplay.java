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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

public class FragmentOptionsDisplay extends FragmentBase {
    private Spinner spStartup;
    private SwitchCompat swDate;
    private SwitchCompat swThreading;
    private SwitchCompat swAvatars;
    private SwitchCompat swIdenticons;
    private SwitchCompat swCircular;
    private SwitchCompat swNameEmail;
    private SwitchCompat swSubjectItalic;
    private SwitchCompat swFlags;
    private SwitchCompat swPreview;
    private SwitchCompat swAddresses;
    private SwitchCompat swMonospaced;
    private SwitchCompat swHtml;
    private SwitchCompat swImages;
    private SwitchCompat swActionbar;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);

        View view = inflater.inflate(R.layout.fragment_options_display, container, false);

        // Get controls

        spStartup = view.findViewById(R.id.spStartup);
        swDate = view.findViewById(R.id.swDate);
        swThreading = view.findViewById(R.id.swThreading);
        swAvatars = view.findViewById(R.id.swAvatars);
        swIdenticons = view.findViewById(R.id.swIdenticons);
        swCircular = view.findViewById(R.id.swCircular);
        swNameEmail = view.findViewById(R.id.swNameEmail);
        swSubjectItalic = view.findViewById(R.id.swSubjectItalic);
        swFlags = view.findViewById(R.id.swFlags);
        swPreview = view.findViewById(R.id.swPreview);
        swAddresses = view.findViewById(R.id.swAddresses);
        swMonospaced = view.findViewById(R.id.swMonospaced);
        swHtml = view.findViewById(R.id.swHtml);
        swImages = view.findViewById(R.id.swImages);
        swActionbar = view.findViewById(R.id.swActionbar);

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        setOptions();

        spStartup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] values = getResources().getStringArray(R.array.startupValues);
                prefs.edit().putString("startup", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("startup").apply();
            }
        });

        swDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("date", checked).apply();
            }
        });

        swThreading.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("threading", checked).apply();
            }
        });

        swAvatars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("avatars", checked).apply();
                ContactInfo.clearCache();
            }
        });

        swIdenticons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("identicons", checked).apply();
                ContactInfo.clearCache();
            }
        });

        swCircular.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("circular", checked).apply();
                ContactInfo.clearCache();
            }
        });

        swNameEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("name_email", checked).apply();
            }
        });

        swSubjectItalic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("subject_italic", checked).apply();
            }
        });

        swFlags.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("flags", checked).apply();
            }
        });

        swPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preview", checked).apply();
            }
        });

        swAddresses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("addresses", checked).apply();
            }
        });

        swMonospaced.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("monospaced", checked).apply();
            }
        });

        swHtml.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autohtml", checked).apply();
            }
        });

        swImages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoimages", checked).apply();
            }
        });

        swActionbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("actionbar", checked).apply();
            }
        });

        return view;
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean compact = prefs.getBoolean("compact", false);

        String startup = prefs.getString("startup", "unified");
        String[] startupValues = getResources().getStringArray(R.array.startupValues);
        for (int pos = 0; pos < startupValues.length; pos++)
            if (startupValues[pos].equals(startup)) {
                spStartup.setSelection(pos);
                break;
            }

        swDate.setChecked(prefs.getBoolean("date", true));
        swThreading.setChecked(prefs.getBoolean("threading", true));
        swAvatars.setChecked(prefs.getBoolean("avatars", true));
        swIdenticons.setChecked(prefs.getBoolean("identicons", false));
        swCircular.setChecked(prefs.getBoolean("circular", true));
        swNameEmail.setChecked(prefs.getBoolean("name_email", !compact));
        swSubjectItalic.setChecked(prefs.getBoolean("subject_italic", true));
        swFlags.setChecked(prefs.getBoolean("flags", true));
        swPreview.setChecked(prefs.getBoolean("preview", false));
        swAddresses.setChecked(prefs.getBoolean("addresses", false));
        swMonospaced.setChecked(prefs.getBoolean("monospaced", false));
        swHtml.setChecked(prefs.getBoolean("autohtml", false));
        swImages.setChecked(prefs.getBoolean("autoimages", false));
        swActionbar.setChecked(prefs.getBoolean("actionbar", true));
    }
}
