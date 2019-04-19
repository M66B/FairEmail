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

import android.content.Intent;
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

public class FragmentAbout extends FragmentBase {
    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_about);

        Intent changelog = null;
        if (!Helper.isPlayStoreInstall(getContext())) {
            changelog = new Intent(Intent.ACTION_VIEW);
            changelog.setData(Uri.parse(BuildConfig.CHANGELOG));
            if (changelog.resolveActivity(getContext().getPackageManager()) == null)
                changelog = null;
        }

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        Button btnChangelog = view.findViewById(R.id.btnChangelog);
        TextView tvLimitations = view.findViewById(R.id.tvLimitations);

        tvVersion.setText(getString(R.string.title_version, BuildConfig.VERSION_NAME));
        btnChangelog.setVisibility(changelog == null ? View.GONE : View.VISIBLE);
        tvLimitations.setPaintFlags(tvLimitations.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        final Intent intent = changelog;
        btnChangelog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });

        return view;
    }
}
