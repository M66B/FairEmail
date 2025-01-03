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

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputLayout;

public class FragmentDialogExport extends FragmentDialogBase {
    private TextInputLayout tilPassword1;
    private TextInputLayout tilPassword2;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("fair:password1", tilPassword1 == null ? null : tilPassword1.getEditText().getText().toString());
        outState.putString("fair:password2", tilPassword2 == null ? null : tilPassword2.getEditText().getText().toString());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_export, null);
        tilPassword1 = dview.findViewById(R.id.tilPassword1);
        tilPassword2 = dview.findViewById(R.id.tilPassword2);

        if (savedInstanceState != null) {
            tilPassword1.getEditText().setText(savedInstanceState.getString("fair:password1"));
            tilPassword2.getEditText().setText(savedInstanceState.getString("fair:password2"));
        }

        Dialog dialog = new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(R.string.title_save_file, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ViewModelExport vme = new ViewModelProvider(getActivity()).get(ViewModelExport.class);
                        vme.setPassword(tilPassword1.getEditText().getText().toString());
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean debug = (BuildConfig.DEBUG || prefs.getBoolean("debug", false));

        Button btnOk = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);

        TextWatcher w = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String p1 = tilPassword1.getEditText().getText().toString();
                String p2 = tilPassword2.getEditText().getText().toString();
                btnOk.setEnabled((debug || !TextUtils.isEmpty(p1)) && p1.equals(p2));
                tilPassword2.setHint(!TextUtils.isEmpty(p2) && !p2.equals(p1)
                        ? R.string.title_setup_password_different
                        : R.string.title_setup_password_repeat);
            }
        };

        tilPassword1.getEditText().addTextChangedListener(w);
        tilPassword2.getEditText().addTextChangedListener(w);
        w.afterTextChanged(null);

        tilPassword2.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (btnOk.isEnabled())
                        btnOk.performClick();
                    return true;
                } else
                    return false;
            }
        });
    }
}
