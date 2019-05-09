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
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FragmentAbout extends FragmentBase {
    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_about);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        TextView tvLimitations = view.findViewById(R.id.tvLimitations);

        tvVersion.setText(getString(R.string.title_version, BuildConfig.VERSION_NAME));
        tvLimitations.setPaintFlags(tvLimitations.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_about, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        PackageManager pm = getContext().getPackageManager();
        menu.findItem(R.id.menu_changelog).setVisible(
                !Helper.isPlayStoreInstall(getContext()) &&
                        getIntentChangelog().resolveActivity(pm) != null);
        menu.findItem(R.id.menu_issue).setVisible(
                Helper.getIntentIssue(getContext()).resolveActivity(pm) != null);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_changelog:
                onMenuChangelog();
                return true;
            case R.id.menu_issue:
                onMenuIssue();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuChangelog() {
        startActivity(getIntentChangelog());
    }

    private void onMenuIssue() {
        startActivity(Helper.getIntentIssue(getContext()));
    }

    private Intent getIntentChangelog() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(BuildConfig.CHANGELOG));
        return intent;
    }
}
