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

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class FragmentDialogSelectIdentity extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        final int dp6 = Helper.dp2pixels(context, 6);
        final int dp12 = Helper.dp2pixels(context, 12);

        final ArrayAdapter<TupleIdentityEx> adapter = new ArrayAdapter<TupleIdentityEx>(context, R.layout.spinner_account, android.R.id.text1) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TupleIdentityEx identity = (TupleIdentityEx) getItem(position);

                View vwColor = view.findViewById(R.id.vwColor);
                TextView tv = view.findViewById(android.R.id.text1);

                int vpad = (getCount() > 10 ? dp6 : dp12);
                tv.setPadding(0, vpad, 0, vpad);

                vwColor.setBackgroundColor(identity.color == null ? Color.TRANSPARENT : identity.color);
                tv.setText(identity.getDisplayName());

                return view;
            }
        };

        // TODO: spinner
        new SimpleTask<List<TupleIdentityEx>>() {
            @Override
            protected List<TupleIdentityEx> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                return db.identity().getComposableIdentities(null);
            }

            @Override
            protected void onExecuted(Bundle args, List<TupleIdentityEx> identities) {
                adapter.addAll(identities);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, getArguments(), "select:identity");

        return new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_person_24)
                .setTitle(R.string.title_list_identities)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TupleIdentityEx identity = adapter.getItem(which);
                        Bundle args = getArguments();
                        args.putLong("id", identity.id);
                        args.putString("html", identity.signature);
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}