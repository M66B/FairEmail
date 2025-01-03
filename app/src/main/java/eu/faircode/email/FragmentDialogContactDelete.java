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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class FragmentDialogContactDelete extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.twotone_warning_24)
                .setTitle(getString(R.string.title_delete_contacts))
                .setMessage(getString(R.string.title_delete_contacts_remark))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                Long account = args.getLong("account");
                                boolean junk = args.getBoolean("junk");

                                if (account < 0)
                                    account = null;
                                int[] types = (junk
                                        ? new int[]{EntityContact.TYPE_JUNK, EntityContact.TYPE_NO_JUNK}
                                        : new int[]{EntityContact.TYPE_FROM, EntityContact.TYPE_TO});

                                DB db = DB.getInstance(context);
                                int count = db.contact().clearContacts(account, types);
                                Log.i("Cleared contacts=" + count);
                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(getContext(), getActivity(), getArguments(), "contacts:delete");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
