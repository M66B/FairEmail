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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class FragmentDialogDebug extends FragmentDialogBase {
    private boolean enabled;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            enabled = savedInstanceState.getBoolean("fair:enabled");

        final Context context = getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_debug, null);
        final EditText etIssue = view.findViewById(R.id.etIssue);
        final CheckBox cbContact = view.findViewById(R.id.cbContact);

        etIssue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                enabled = !TextUtils.isEmpty(editable.toString().trim());
                setEnabled(enabled);
            }
        });

        etIssue.post(new Runnable() {
            @Override
            public void run() {
                etIssue.requestFocus();
                Helper.showKeyboard(etIssue);
            }
        });

        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = getArguments();
                        args.putString("issue", etIssue.getText().toString());
                        args.putBoolean("contact", cbContact.isChecked());

                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        setEnabled(enabled);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("fair:enabled", enabled);
        super.onSaveInstanceState(outState);
    }

    private void setEnabled(boolean value) {
        Button ok = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        if (ok != null)
            ok.setEnabled(value);
    }
}
