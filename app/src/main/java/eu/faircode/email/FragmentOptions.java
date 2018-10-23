package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FragmentOptions extends FragmentEx {
    private CheckBox cbEnabled;
    private CheckBox cbAvatars;
    private CheckBox cbLight;
    private CheckBox cbBrowse;
    private CheckBox cbSwipe;
    private CheckBox cbCompact;
    private CheckBox cbInsecure;
    private CheckBox cbDebug;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);

        View view = inflater.inflate(R.layout.fragment_options, container, false);

        // Get controls
        cbEnabled = view.findViewById(R.id.cbEnabled);
        cbAvatars = view.findViewById(R.id.cbAvatars);
        cbLight = view.findViewById(R.id.cbLight);
        cbBrowse = view.findViewById(R.id.cbBrowse);
        cbSwipe = view.findViewById(R.id.cbSwipe);
        cbCompact = view.findViewById(R.id.cbCompact);
        cbInsecure = view.findViewById(R.id.cbInsecure);
        cbDebug = view.findViewById(R.id.cbDebug);

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        cbEnabled.setChecked(prefs.getBoolean("enabled", true));
        cbEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("enabled", checked).apply();
                if (checked)
                    ServiceSynchronize.start(getContext());
                else
                    ServiceSynchronize.stop(getContext());
            }
        });

        cbAvatars.setChecked(prefs.getBoolean("avatars", true));
        cbAvatars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("avatars", checked).apply();
            }
        });

        cbLight.setChecked(prefs.getBoolean("light", false));
        cbLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("light", checked).apply();
            }
        });

        cbBrowse.setChecked(prefs.getBoolean("browse", true));
        cbBrowse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("browse", checked).apply();
            }
        });

        cbSwipe.setChecked(prefs.getBoolean("swipe", true));
        cbSwipe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("swipe", checked).apply();
            }
        });

        cbCompact.setChecked(prefs.getBoolean("compact", false));
        cbCompact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("compact", checked).apply();
            }
        });

        cbInsecure.setChecked(prefs.getBoolean("insecure", false));
        cbInsecure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("insecure", checked).apply();
            }
        });

        cbDebug.setChecked(prefs.getBoolean("debug", false));
        cbDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("debug", checked).apply();
                ServiceSynchronize.reload(getContext(), "debug=" + checked);
            }
        });

        cbLight.setVisibility(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O ? View.VISIBLE : View.GONE);

        return view;
    }
}
