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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputLayout;

public class FragmentDialogImport extends FragmentDialogBase {
    private TextInputLayout tilPassword1;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("fair:password1", tilPassword1 == null ? null : tilPassword1.getEditText().getText().toString());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_import, null);
        tilPassword1 = dview.findViewById(R.id.tilPassword1);
        CheckBox cbAccounts = dview.findViewById(R.id.cbAccounts);
        CheckBox cbDelete = dview.findViewById(R.id.cbDelete);
        CheckBox cbRules = dview.findViewById(R.id.cbRules);
        CheckBox cbContacts = dview.findViewById(R.id.cbContacts);
        CheckBox cbAnswers = dview.findViewById(R.id.cbAnswers);
        CheckBox cbSearches = dview.findViewById(R.id.cbSearches);
        CheckBox cbSettings = dview.findViewById(R.id.cbSettings);

        cbAccounts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbRules.setEnabled(checked);
                cbContacts.setEnabled(checked);
            }
        });

        if (savedInstanceState != null)
            tilPassword1.getEditText().setText(savedInstanceState.getString("fair:password1"));

        Dialog dialog = new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(R.string.title_add_image_select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password1 = tilPassword1.getEditText().getText().toString();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean debug = prefs.getBoolean("debug", false);

                        if (TextUtils.isEmpty(password1) && !(debug || BuildConfig.DEBUG)) {
                            ToastEx.makeText(context, R.string.title_setup_password_missing, Toast.LENGTH_LONG).show();
                            sendResult(RESULT_CANCELED);
                        } else {
                            ViewModelExport vme = new ViewModelProvider(getActivity()).get(ViewModelExport.class);
                            vme.setPassword(password1);
                            vme.setOptions("accounts", cbAccounts.isChecked());
                            vme.setOptions("delete", cbDelete.isChecked());
                            vme.setOptions("rules", cbRules.isChecked());
                            vme.setOptions("contacts", cbContacts.isChecked());
                            vme.setOptions("answers", cbAnswers.isChecked());
                            vme.setOptions("searches", cbSearches.isChecked());
                            vme.setOptions("settings", cbSettings.isChecked());
                            sendResult(RESULT_OK);
                        }
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

        Button btnOk = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);

        tilPassword1.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnOk.performClick();
                    return true;
                } else
                    return false;
            }
        });
    }
}
