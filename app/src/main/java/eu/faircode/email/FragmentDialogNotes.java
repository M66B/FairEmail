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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.util.List;

public class FragmentDialogNotes extends FragmentDialogBase {
    private ViewButtonColor btnColor;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final long id = args.getLong("id");
        final String notes = args.getString("notes");

        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final Integer color = (TextUtils.isEmpty(notes)
                ? prefs.getInt("note_color", Color.TRANSPARENT)
                : args.getInt("color"));

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_notes, null);
        final EditText etNotes = view.findViewById(R.id.etNotes);
        btnColor = view.findViewById(R.id.btnColor);

        etNotes.setText(notes);
        btnColor.setColor(color);

        etNotes.selectAll();

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.hideKeyboard(etNotes);

                Bundle args = new Bundle();
                args.putInt("color", btnColor.getColor());
                args.putString("title", getString(R.string.title_color));
                args.putBoolean("reset", true);

                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentDialogNotes.this, 1);
                fragment.show(getParentFragmentManager(), "notes:color");
            }
        });

        final SimpleTask<Void> task = new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                String notes = args.getString("notes");
                Integer color = args.getInt("color");

                if ("".equals(notes.trim()))
                    notes = null;

                if (color == Color.TRANSPARENT)
                    color = null;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    db.message().setMessageNotes(message.id, notes, color);

                    if (TextUtils.isEmpty(message.msgid))
                        return null;

                    List<EntityMessage> messages =
                            db.message().getMessagesBySimilarity(message.account, message.id, message.msgid, message.hash);
                    if (messages == null)
                        return null;

                    for (EntityMessage m : messages)
                        db.message().setMessageNotes(m.id, notes, color);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                WidgetUnified.updateData(context);

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                WorkerFts.init(context, false);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putLong("id", id);
                        args.putString("notes", etNotes.getText().toString());
                        args.putInt("color", btnColor.getColor());

                        task.execute(getContext(), getActivity(), args, "message:note");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        if (!TextUtils.isEmpty(notes))
            builder.setNeutralButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Bundle args = new Bundle();
                    args.putLong("id", id);
                    args.putString("notes", "");
                    args.putInt("color", Color.TRANSPARENT);

                    task.execute(getContext(), getActivity(), args, "message:note");
                }
            });

        return builder.create();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK && data != null) {
                Bundle args = data.getBundleExtra("args");
                int color = args.getInt("color");
                btnColor.setColor(color);

                Context context = getContext();
                if (context != null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    prefs.edit().putInt("note_color", color).apply();
                }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }
}
