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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;

import java.text.DateFormat;
import java.util.List;

public class FragmentAbout extends FragmentBase {
    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_about);
        setHasOptionsMenu(true);

        final Context context = getContext();

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        TextView tvRelease = view.findViewById(R.id.tvRelease);
        TextView tvDownloaded = view.findViewById(R.id.tvDownloaded);
        TextView tvUpdated = view.findViewById(R.id.tvUpdated);
        TextView tvGplV3 = view.findViewById(R.id.tvGplV3);
        LinearLayout llContributors = view.findViewById(R.id.llContributors);

        String version = BuildConfig.VERSION_NAME + BuildConfig.REVISION;
        tvVersion.setText(getString(R.string.title_version, version));
        tvRelease.setText(BuildConfig.RELEASE_NAME);

        String type = Log.getReleaseType(context) + (BuildConfig.DEBUG ? " (Debug)" : "");
        tvDownloaded.setText(getString(R.string.app_download, type));

        long last = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(BuildConfig.APPLICATION_ID, 0);
            last = pi.lastUpdateTime;
        } catch (Throwable ex) {
            Log.e(ex);
        }

        DateFormat DF = Helper.getDateTimeInstance(context, DateFormat.SHORT, DateFormat.SHORT);
        tvUpdated.setText(getString(R.string.app_updated, last == 0 ? "-" : DF.format(last)));

        tvUpdated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.PLAY_STORE_RELEASE)
                    Helper.view(v.getContext(), Helper.getIntentRate(v.getContext()));
                else
                    Helper.view(v.getContext(), Uri.parse(BuildConfig.CHANGELOG), false);
            }
        });

        tvGplV3.setPaintFlags(tvGplV3.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvGplV3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.view(view.getContext(), Uri.parse(Helper.LICENSE_URI), true);
            }
        });

        TypedValue style = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.style.TextAppearance_AppCompat_Small, style, true);

        List<Contributor> contributors = Contributor.loadContributors(context);
        for (Contributor contributor : contributors) {
            TextView tv = new TextView(context);
            TextViewCompat.setTextAppearance(tv, style.data);
            tv.setText(contributor.toString());
            llContributors.addView(tv);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_about, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_changelog) {
            onMenuChangelog();
            return true;
        } else if (itemId == R.id.menu_attribution) {
            onMenuAttribution();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuChangelog() {
        Bundle args = new Bundle();
        args.putString("name", "CHANGELOG.md");
        FragmentDialogMarkdown fragment = new FragmentDialogMarkdown();
        fragment.setArguments(args);
        fragment.show(getParentFragmentManager(), "changelog");
    }

    private void onMenuAttribution() {
        Bundle args = new Bundle();
        args.putString("name", "ATTRIBUTION.md");
        FragmentDialogMarkdown fragment = new FragmentDialogMarkdown();
        fragment.setArguments(args);
        fragment.show(getParentFragmentManager(), "attribution");
    }
}
