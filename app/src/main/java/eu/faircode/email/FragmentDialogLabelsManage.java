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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentDialogLabelsManage extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final long id = getArguments().getLong("id");
        String self = getArguments().getString("self");
        String[] labels = getArguments().getStringArray("labels");
        final String[] folders = getArguments().getStringArray("folders");

        List<String> l = new ArrayList<>();
        if (self != null)
            l.add(self);
        if (labels != null)
            l.addAll(Arrays.asList(labels));

        boolean[] checked = new boolean[folders.length];
        for (int i = 0; i < folders.length; i++)
            if (l.contains(folders[i]))
                checked[i] = true;

        return new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.twotone_label_24)
                .setTitle(R.string.title_manage_labels)
                .setMultiChoiceItems(folders, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        Bundle args = new Bundle();
                        args.putLong("id", id);
                        args.putString("label", folders[which]);
                        args.putBoolean("set", isChecked);

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                long id = args.getLong("id");
                                String label = args.getString("label");
                                boolean set = args.getBoolean("set");

                                DB db = DB.getInstance(context);
                                EntityMessage message = db.message().getMessage(id);
                                if (message == null)
                                    return null;

                                EntityOperation.queue(context, message, EntityOperation.LABEL, label, set);

                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(FragmentDialogLabelsManage.this, args, "label:set");
                    }
                })
                .setNegativeButton(R.string.title_setup_done, null)
                .create();
    }
}
