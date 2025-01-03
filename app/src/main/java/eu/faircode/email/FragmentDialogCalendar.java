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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentDialogCalendar extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();

        Bundle args = getArguments();
        boolean forevent = args.getBoolean("forevent");

        String selectedCalendar = args.getString("calendar");
        String selectedAccount;
        String selectedName;
        try {
            JSONObject jselected = new JSONObject(selectedCalendar);
            selectedAccount = jselected.getString("account");
            selectedName = jselected.optString("name", null);
        } catch (Throwable ex) {
            Log.i(ex);
            selectedAccount = selectedCalendar;
            selectedName = null;
        }

        List<Calendar> calendars = new ArrayList<>();
        try (Cursor cursor = resolver.query(CalendarContract.Calendars.CONTENT_URI,
                new String[]{
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.ACCOUNT_NAME,
                        CalendarContract.Calendars.ACCOUNT_TYPE,
                        CalendarContract.Calendars.IS_PRIMARY,
                        CalendarContract.Calendars.VISIBLE,
                        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
                },
                CalendarContract.Calendars.VISIBLE + " <> 0",
                null,
                CalendarContract.Calendars.ACCOUNT_NAME + "," +
                        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )) {
            if (cursor != null) {
                int colId = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID);
                int colAccount = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME);
                int colType = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_TYPE);
                int colPrimary = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY);
                int colVisible = cursor.getColumnIndex(CalendarContract.Calendars.VISIBLE);
                int colDisplay = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(colId);
                    String account = cursor.getString(colAccount);
                    String type = cursor.getString(colType);
                    boolean primary = (colPrimary >= 0 && cursor.getInt(colPrimary) != 0);
                    boolean visible = (colVisible >= 0 && cursor.getInt(colVisible) != 0);
                    String name = (colDisplay >= 0 ? cursor.getString(colDisplay) : null);
                    if (account != null)
                        calendars.add(new Calendar(id, account, type, primary, visible, name));
                }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        int checkedItem = -1;
        List<String> names = new ArrayList<>();
        for (int i = 0; i < calendars.size(); i++) {
            Calendar calendar = calendars.get(i);
            names.add(calendar.getTitle());
            if (Objects.equals(calendar.account, selectedAccount) &&
                    (selectedName == null || Objects.equals(calendar.name, selectedName)))
                checkedItem = i;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.twotone_event_24);
        builder.setTitle(R.string.title_calendar);

        builder.setSingleChoiceItems(names.toArray(new String[0]), checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar calendar = calendars.get(which);
                args.putLong("id", calendar.id);
                args.putString("account", calendar.account);
                args.putString("type", calendar.type);
                args.putString("name", calendar.name);
                sendResult(RESULT_OK);
                dismiss();
            }
        });

        if (!forevent)
            builder.setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    args.putLong("id", -1);
                    args.putString("account", null);
                    args.putString("type", null);
                    sendResult(RESULT_OK);
                }
            });

        builder.setPositiveButton(android.R.string.cancel, null);

        return builder.create();
    }

    private class Calendar {
        private long id;
        private String account;
        private String type;
        private boolean primary;
        private boolean visible;
        private String name;

        Calendar(long id, String account, String type, boolean primary, boolean visible, String name) {
            this.id = id;
            this.account = account;
            this.type = type;
            this.primary = primary;
            this.visible = visible;
            this.name = name;
        }

        String getTitle() {
            return (this.visible ? "" : "(") +
                    (this.account == null ? "-" : this.account) +
                    (BuildConfig.DEBUG && false ? ":" + (this.type == null ? "-" : this.type) : "") +
                    (TextUtils.isEmpty(this.name) || Objects.equals(this.account, this.name) ? "" : ":" + this.name) +
                    (this.visible ? "" : ")") +
                    " " + (this.primary ? "*" : "");
        }
    }
}
