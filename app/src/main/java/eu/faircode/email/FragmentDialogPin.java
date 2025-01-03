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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

public class FragmentDialogPin extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pin_set, null);
        final EditText etPin = dview.findViewById(R.id.etPin);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pin = etPin.getText().toString();
                        if (TextUtils.isEmpty(pin))
                            prefs.edit().remove("pin").apply();
                        else {
                            boolean pro = ActivityBilling.isPro(getContext());
                            if (pro) {
                                Helper.setAuthenticated(getContext());
                                prefs.edit()
                                        .remove("biometrics")
                                        .putString("pin", pin)
                                        .apply();
                            } else
                                startActivity(new Intent(getContext(), ActivityBilling.class));
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        String pin = prefs.getString("pin", null);
        if (!TextUtils.isEmpty(pin))
            builder.setNeutralButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    prefs.edit().remove("pin").apply();
                }
            });

        final Dialog dialog = builder.create();

        etPin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    return true;
                } else
                    return false;
            }
        });

        etPin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        ApplicationEx.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;
                etPin.requestFocus();
            }
        });

        return dialog;
    }
}
