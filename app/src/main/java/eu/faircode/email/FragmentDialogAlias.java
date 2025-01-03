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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class FragmentDialogAlias extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_alias, null);
        final EditText etName = view.findViewById(R.id.etName);
        final EditText etEmail = view.findViewById(R.id.etEmail);
        final ImageButton ibInfo = view.findViewById(R.id.ibInfo);

        Bundle args = getArguments();
        etName.setText(args.getString("name"));
        etEmail.setText(args.getString("email"));

        etEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 9);
            }
        });

        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        args.putString("name", etName.getText().toString());
                        args.putString("email", etEmail.getText().toString());

                        new SimpleTask<EntityIdentity>() {
                            @Override
                            protected EntityIdentity onExecute(Context context, Bundle args) throws Throwable {
                                long id = args.getLong("id");
                                String name = args.getString("name");
                                String email = args.getString("email");

                                DB db = DB.getInstance(context);

                                EntityIdentity identity = db.identity().getIdentity(id);
                                if (identity == null)
                                    return null;

                                identity.id = null;
                                identity.name = name;
                                identity.email = email;
                                identity.primary = false;
                                identity.sign_key = null;
                                identity.sign_key_alias = null;
                                identity.error = null;
                                identity.last_connected = null;
                                identity.id = db.identity().insertIdentity(identity);

                                return identity;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(FragmentDialogAlias.this, args, "create:alias");

                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
