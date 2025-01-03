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

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

public class FragmentEula extends FragmentBase {
    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_welcome);

        View view = inflater.inflate(R.layout.fragment_eula, container, false);

        TextView tvGplV3 = view.findViewById(R.id.tvGplV3);
        tvGplV3.setPaintFlags(tvGplV3.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvGplV3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.view(view.getContext(), Uri.parse(Helper.LICENSE_URI), true);
            }
        });

        Button btnAgree = view.findViewById(R.id.btnOk);
        Button btnDisagree = view.findViewById(R.id.btnCancel);

        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                prefs.edit().putBoolean("eula", true).apply();
            }
        });

        btnDisagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        return view;
    }
}
