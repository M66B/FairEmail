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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;

public class FragmentDialogStill extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_setup, null);
        TextView tvDozeDevice = dview.findViewById(R.id.tvDozeDevice);
        TextView tvDozeAndroid = dview.findViewById(R.id.tvDozeAndroid);
        ImageButton ibInfo = dview.findViewById(R.id.ibInfo);
        CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
        Group grp2 = dview.findViewById(R.id.grp2);
        Group grp3 = dview.findViewById(R.id.grp3);

        tvDozeDevice.setPaintFlags(tvDozeDevice.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvDozeDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(context, Uri.parse(Helper.DONTKILL_URI), true);
            }
        });

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.DONTKILL_URI), true);
            }
        });

        cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putBoolean("setup_reminder", !isChecked).apply();
            }
        });

        boolean hasPermissions = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        Boolean isIgnoring = Helper.isIgnoringOptimizations(context);
        boolean isKilling = Helper.isKilling() && !(isIgnoring == null || isIgnoring);
        boolean isRequired = Helper.isDozeRequired() && !(isIgnoring == null || isIgnoring);

        tvDozeDevice.setVisibility(isKilling && !isRequired ? View.VISIBLE : View.GONE);
        tvDozeAndroid.setVisibility(isRequired ? View.VISIBLE : View.GONE);
        cbNotAgain.setVisibility(isRequired ? View.GONE : View.VISIBLE);

        grp2.setVisibility(hasPermissions ? View.GONE : View.VISIBLE);
        grp3.setVisibility(isIgnoring == null || isIgnoring ? View.GONE : View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                });

        if (!isRequired)
            builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }
}
