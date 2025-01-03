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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.util.Date;

public class FragmentDialogReview extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);
        TextView tvHelp = dview.findViewById(R.id.tvHelp);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        Dialog dialog = new AlertDialog.Builder(getContext())
                .setView(dview)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit().putBoolean("review_asked", true).apply();
                        startActivity(Helper.getIntentRate(getContext()));
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit().putBoolean("review_asked", true).apply();
                    }
                })
                .setNeutralButton(R.string.title_later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit().putLong("review_later", new Date().getTime()).apply();
                    }
                })
                .create();

        tvHelp.setPaintFlags(tvHelp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                prefs.edit().putLong("review_later", new Date().getTime()).apply();
                startActivity(Helper.getIntentIssue(v.getContext(), "Review:issue"));
            }
        });

        return dialog;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.edit().putBoolean("review_asked", true).apply();
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }
}
