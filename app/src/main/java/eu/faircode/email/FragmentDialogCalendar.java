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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FragmentDialogCalendar extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();

        String selectedAccount = getArguments().getString("account");

        List<Calendar> calendars = new ArrayList<>();
        try (Cursor cursor = resolver.query(CalendarContract.Calendars.CONTENT_URI,
                new String[]{
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.ACCOUNT_NAME,
                        CalendarContract.Calendars.ACCOUNT_TYPE
                },
                CalendarContract.Calendars.VISIBLE + " = 1 AND " +
                        CalendarContract.Calendars.IS_PRIMARY + " = 1",
                null,
                CalendarContract.Calendars.ACCOUNT_NAME)) {

            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String account = cursor.getString(1);
                String type = cursor.getString(2);
                if (account != null)
                    calendars.add(new Calendar(id, account, type));
            }
        }

        int checkedItem = -1;
        List<String> names = new ArrayList<>();
        for (int i = 0; i < calendars.size(); i++) {
            Calendar calendar = calendars.get(i);
            names.add(calendar.getTitle());
            if (calendar.account.equals(selectedAccount))
                checkedItem = i;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.twotone_event_24);
        builder.setTitle(R.string.title_calendar);

        builder.setSingleChoiceItems(names.toArray(new String[0]), checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar calendar = calendars.get(which);
                getArguments().putLong("id", calendar.id);
                getArguments().putString("account", calendar.account);
                getArguments().putString("type", calendar.type);
                sendResult(RESULT_OK);
                dismiss();
            }
        });

        builder.setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getArguments().putLong("id", -1);
                getArguments().putString("account", null);
                getArguments().putString("type", null);
                sendResult(RESULT_OK);
            }
        });

        builder.setPositiveButton(android.R.string.cancel, null);

        return builder.create();
    }


    private class Calendar {
        Calendar(long id, String account, String type) {
            this.id = id;
            this.account = account;
            this.type = type;
        }

        private long id;
        private String account;
        private String type;

        String getTitle() {
            return (this.account == null ? "-" : this.account);
        }
    }
}
