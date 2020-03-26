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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import static android.app.Activity.RESULT_OK;

public class FragmentDialogLink extends FragmentDialogBase {
    private EditText etLink;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("fair:link", etLink.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Uri uri = getArguments().getParcelable("uri");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_insert_link, null);
        etLink = view.findViewById(R.id.etLink);
        final TextView tvInsecure = view.findViewById(R.id.tvInsecure);

        etLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Uri uri = Uri.parse(editable.toString());
                tvInsecure.setVisibility(!uri.isOpaque() &&
                        "http".equals(uri.getScheme()) ? View.VISIBLE : View.GONE);
            }
        });

        if (savedInstanceState == null)
            etLink.setText(uri == null ? "https://" : uri.toString());
        else
            etLink.setText(savedInstanceState.getString("fair:link"));

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String link = etLink.getText().toString();
                        getArguments().putString("link", link);
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(RESULT_OK);
                    }
                })
                .create();
    }
}
