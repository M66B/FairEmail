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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class FragmentDialogContactEdit extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_contact, null);
        final Spinner spType = view.findViewById(R.id.spType);
        final EditText etEmail = view.findViewById(R.id.etEmail);
        final EditText etName = view.findViewById(R.id.etName);
        final EditText etGroup = view.findViewById(R.id.etGroup);

        final Bundle args = getArguments();
        int type = args.getInt("type");

        boolean junk = (type == EntityContact.TYPE_JUNK || type == EntityContact.TYPE_NO_JUNK);
        String[] values = getResources().getStringArray(R.array.contactTypes);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spinner_item1, android.R.id.text1, values) {
            @Override
            public boolean isEnabled(int position) {
                if (junk)
                    return (position == EntityContact.TYPE_JUNK || position == EntityContact.TYPE_NO_JUNK);
                else
                    return (position == EntityContact.TYPE_TO || position == EntityContact.TYPE_FROM);
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setEnabled(isEnabled(position));
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spType.setAdapter(adapter);

        spType.setSelection(args.getInt("type"));
        etEmail.setText(args.getString("email"));
        etName.setText(args.getString("name"));
        etGroup.setText(args.getString("group"));

        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        args.putInt("type", spType.getSelectedItemPosition());
                        args.putString("email", etEmail.getText().toString().trim());
                        args.putString("name", etName.getText().toString());
                        args.putString("group", etGroup.getText().toString().trim());
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
