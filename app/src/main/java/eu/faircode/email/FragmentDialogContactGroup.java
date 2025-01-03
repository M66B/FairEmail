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
import static android.widget.AdapterView.INVALID_POSITION;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import java.text.NumberFormat;

public class FragmentDialogContactGroup extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final long working = args.getLong("working");
        int focussed = args.getInt("focussed");

        final Context context = getContext();
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_contact_group, null);
        final ImageButton ibInfo = dview.findViewById(R.id.ibInfo);
        final Spinner spGroup = dview.findViewById(R.id.spGroup);
        final Spinner spTarget = dview.findViewById(R.id.spTarget);
        final Spinner spType = dview.findViewById(R.id.spType);
        final TextView tvNoPermission = dview.findViewById(R.id.tvNoPermission);

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.URI_SUPPORT_CONTACT_GROUP), true);
            }
        });

        spTarget.setSelection(focussed);
        tvNoPermission.setVisibility(View.GONE);

        new SimpleTask<Cursor>() {
            @Override
            protected Cursor onExecute(Context context, Bundle args) {
                final String[] projection = new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE,
                        ContactsContract.Groups.SUMMARY_COUNT,
                        ContactsContract.Groups.ACCOUNT_NAME,
                        ContactsContract.Groups.ACCOUNT_TYPE,
                };

                boolean permission = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
                args.putBoolean("permission", permission);

                Cursor contacts = new MatrixCursor(projection);
                if (permission)
                    try {
                        ContentResolver resolver = context.getContentResolver();
                        Cursor cursor = resolver.query(
                                ContactsContract.Groups.CONTENT_SUMMARY_URI,
                                projection,
                                // ContactsContract.Groups.GROUP_VISIBLE + " = 1" + " AND " +
                                ContactsContract.Groups.DELETED + " = 0" +
                                        " AND " + ContactsContract.Groups.SUMMARY_COUNT + " > 0",
                                null,
                                ContactsContract.Groups.TITLE
                        );
                        if (cursor != null)
                            contacts = cursor;
                    } catch (SecurityException ex) {
                        Log.w(ex);
                    }

                DB db = DB.getInstance(context);
                Cursor local = db.contact().getGroups(
                        null,
                        context.getString(R.string.app_name),
                        BuildConfig.APPLICATION_ID);

                return new MergeCursor(new Cursor[]{contacts, local});
            }

            @Override
            protected void onExecuted(Bundle args, Cursor cursor) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                        context,
                        R.layout.spinner_contact_group,
                        cursor,
                        new String[]{ContactsContract.Groups.TITLE, ContactsContract.Groups.ACCOUNT_NAME},
                        new int[]{R.id.tvGroup, R.id.tvAccount},
                        0);

                final NumberFormat NF = NumberFormat.getInstance();

                adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                        if (view.getId() == R.id.tvGroup) {
                            String title = cursor.getString(1);
                            if (TextUtils.isEmpty(title))
                                title = "-";
                            int count = cursor.getInt(2);
                            ((TextView) view).setText(context.getString(R.string.title_name_count, title, NF.format(count)));
                            return true;
                        } else if (view.getId() == R.id.tvAccount) {
                            String account = cursor.getString(3);
                            String type = cursor.getString(4);
                            ((TextView) view).setText(account + (BuildConfig.DEBUG ? "/" + type : ""));
                            return true;
                        } else
                            return false;
                    }
                });

                spGroup.setAdapter(adapter);

                boolean permission = args.getBoolean("permission");
                tvNoPermission.setVisibility(permission ? View.GONE : View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "compose:groups");

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            int target = spTarget.getSelectedItemPosition();
                            Cursor cursor = (Cursor) spGroup.getSelectedItem();
                            if (target != INVALID_POSITION &&
                                    cursor != null && cursor.getCount() > 0) {
                                long group = cursor.getLong(0);
                                String name = cursor.getString(1);

                                Bundle args = getArguments();
                                args.putLong("id", working);
                                args.putInt("target", target);
                                args.putLong("group", group);
                                args.putString("name", name);
                                args.putInt("type", spType.getSelectedItemPosition());

                                sendResult(RESULT_OK);
                            } else
                                sendResult(RESULT_CANCELED);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
