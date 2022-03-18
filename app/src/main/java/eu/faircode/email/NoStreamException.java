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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.FileNotFoundException;

public class NoStreamException extends SecurityException {
    private Uri uri;

    private NoStreamException(@NonNull Uri uri) {
        Log.w("Read uri=" + uri);
        this.uri = uri;
    }

    static void check(Uri uri, Context context) throws FileNotFoundException {
        if (uri == null)
            throw new FileNotFoundException("Selected file");

        if ("content".equals(uri.getScheme()))
            return;
        if (Helper.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE))
            return;

        throw new NoStreamException(uri);
    }

    void report(Context context) {
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_no_stream, null);

        TextView tvUri = dview.findViewById(R.id.tvUri);
        ImageButton ibInfo = dview.findViewById(R.id.ibInfo);

        tvUri.setText(uri == null ? null : uri.toString());

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 49);
            }
        });

        new AlertDialog.Builder(context)
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.title_setup_grant, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                        intent.setData(uri);
                        context.startActivity(intent);
                    }
                })
                .show();
    }
}
