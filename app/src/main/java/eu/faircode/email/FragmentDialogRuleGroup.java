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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class FragmentDialogRuleGroup extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_rule_group, null);
        final AutoCompleteTextView etGroup = view.findViewById(R.id.etGroup);

        ArrayAdapter adapterGroup = new ArrayAdapter<>(context, R.layout.spinner_item1_dropdown, android.R.id.text1);
        etGroup.setThreshold(1);
        etGroup.setAdapter(adapterGroup);

        new SimpleTask<List<String>>() {
            @Override
            protected List<String> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                return db.rule().getGroups();
            }

            @Override
            protected void onExecuted(Bundle args, List<String> groups) {
                adapterGroup.clear();
                adapterGroup.addAll(groups);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "rule:groups");

        Bundle args = getArguments();
        etGroup.setText(args.getString("name"));

        etGroup.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != EditorInfo.IME_ACTION_DONE)
                    return false;
                AlertDialog dialog = (AlertDialog) getDialog();
                if (dialog == null)
                    return false;
                Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (btnOk == null)
                    return false;
                btnOk.performClick();
                return true;
            }
        });

        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String group = etGroup.getText().toString().trim();
                        args.putString("name", TextUtils.isEmpty(group) ? null : group);
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
