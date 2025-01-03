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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

public class FragmentDialogDataSaver extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        SpannableStringBuilder ssb = new SpannableStringBuilderEx();
        ssb.append(context.getString(R.string.title_hint_data_saver));
        ssb.append("\n\n");
        int start = ssb.length();
        ssb.append(context.getString(R.string.title_hint_dismiss));
        ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), start, ssb.length(), 0);

        Intent saver = new Intent(
                Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
                Uri.parse("package:" + BuildConfig.APPLICATION_ID));

        Intent settings = (saver.resolveActivity(context.getPackageManager()) == null
                ? new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                : saver);

        return new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_data_saver_off_24)
                .setTitle(R.string.title_setup_data)
                .setMessage(ssb)
                .setPositiveButton(R.string.title_fix, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(settings);
                    }
                })
                .setNegativeButton(R.string.title_dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        prefs.edit().putBoolean("datasaver_reminder", false).apply();
                    }
                })
                .create();
    }
}
