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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.WriteContext;
import biweekly.io.scribe.property.RecurrenceRuleScribe;
import biweekly.parameter.ParticipationStatus;
import biweekly.property.Attendee;
import biweekly.property.RecurrenceRule;
import biweekly.util.ICalDate;

public class CalendarHelper {
    static void insert(Context context, ICalendar icalendar, VEvent event,
                       String selectedAccount, String selectedName, EntityMessage message) {
        String summary = (event.getSummary() == null ? null : event.getSummary().getValue());
        String description = (event.getDescription() == null ? null : event.getDescription().getValue());
        String location = (event.getLocation() == null ? null : event.getLocation().getValue());

        ICalDate start = (event.getDateStart() == null ? null : event.getDateStart().getValue());
        ICalDate end = (event.getDateEnd() == null ? null : event.getDateEnd().getValue());

        String rrule = null;
        RecurrenceRule recurrence = event.getRecurrenceRule();
        if (recurrence != null) {
            RecurrenceRuleScribe scribe = new RecurrenceRuleScribe();
            WriteContext wcontext = new WriteContext(ICalVersion.V2_0, icalendar.getTimezoneInfo(), null);
            rrule = scribe.writeText(recurrence, wcontext);
        }

        String uid = (event.getUid() == null ? null : event.getUid().getValue());

        if (TextUtils.isEmpty(uid) || start == null || end == null)
            return;

        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(CalendarContract.Calendars.CONTENT_URI,
                new String[]{CalendarContract.Calendars._ID},
                CalendarContract.Calendars.VISIBLE + " <> 0 AND " +
                        CalendarContract.Calendars.ACCOUNT_NAME + " = ?" +
                        (selectedName == null
                                ? ""
                                : " AND " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ?"),
                selectedName == null
                        ? new String[]{selectedAccount}
                        : new String[]{selectedAccount, selectedName},
                null)) {
            if (cursor.getCount() == 0)
                EntityLog.log(context, EntityLog.Type.General, message,
                        "Account not found account=" + selectedAccount + ":" + selectedName);

            if (cursor.moveToNext()) {
                // https://developer.android.com/guide/topics/providers/calendar-provider#add-event
                // https://developer.android.com/reference/android/provider/CalendarContract.EventsColumns
                long calId = cursor.getLong(0);

                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.CALENDAR_ID, calId);
                if (!TextUtils.isEmpty(uid))
                    values.put(CalendarContract.Events.UID_2445, uid);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
                values.put(CalendarContract.Events.DTSTART, start.getTime());
                values.put(CalendarContract.Events.DTEND, end.getTime());
                if (rrule != null)
                    values.put(CalendarContract.Events.RRULE, rrule);
                if (!TextUtils.isEmpty(summary))
                    values.put(CalendarContract.Events.TITLE, summary);
                if (!TextUtils.isEmpty(description))
                    values.put(CalendarContract.Events.DESCRIPTION, description);
                if (!TextUtils.isEmpty(location))
                    values.put(CalendarContract.Events.EVENT_LOCATION, location);
                values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_TENTATIVE);

                Uri uri = resolver.insert(CalendarContract.Events.CONTENT_URI, values);
                long eventId = Long.parseLong(uri.getLastPathSegment());
                EntityLog.log(context, EntityLog.Type.General, message, "Inserted event" +
                        " id=" + calId + ":" + eventId +
                        " uid=" + uid +
                        " tz=" + TimeZone.getDefault().getID() +
                        " start=" + new Date(start.getTime()) +
                        " end=" + new Date(end.getTime()) +
                        " rrule=" + rrule +
                        " summary=" + summary +
                        " location=" + location);

                for (Attendee a : event.getAttendees())
                    try {
                        String email = a.getEmail();
                        String name = a.getCommonName();
                        String role = (a.getRole() == null ? null : a.getRole().getValue());
                        String level = (a.getParticipationLevel() == null ? null
                                : a.getParticipationLevel().getValue(icalendar.getVersion()));
                        String status = (a.getParticipationStatus() == null ? null : a.getParticipationStatus().getValue());

                        ContentValues avalues = new ContentValues();

                        if (!TextUtils.isEmpty(email))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_EMAIL, email);

                        if (!TextUtils.isEmpty(name))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_NAME, name);

                        if ("ORGANIZER".equals(role))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_RELATIONSHIP, CalendarContract.Attendees.RELATIONSHIP_ORGANIZER);
                        else if ("ATTENDEE".equals(role))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_RELATIONSHIP, CalendarContract.Attendees.RELATIONSHIP_ATTENDEE);

                        if ("REQUIRE".equals(level) || "REQ-PARTICIPANT".equals(level))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_TYPE, CalendarContract.Attendees.TYPE_REQUIRED);
                        else if ("REQUEST".equals(level) || "OPT-PARTICIPANT".equals(level))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_TYPE, CalendarContract.Attendees.TYPE_OPTIONAL);

                        if ("ACCEPTED".equals(status) || "CONFIRMED".equals(status))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED);
                        else if ("DECLINED".equals(status))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED);
                        else if ("TENTATIVE".equals(status))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_TENTATIVE);
                        else if ("NEEDS-ACTION".equals(status))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_NONE);

                        avalues.put(CalendarContract.Attendees.EVENT_ID, eventId);

                        Uri auri = resolver.insert(CalendarContract.Attendees.CONTENT_URI, avalues);
                        long attendeeId = Long.parseLong(auri.getLastPathSegment());
                        EntityLog.log(context, EntityLog.Type.General, message, "Inserted attendee" +
                                " id=" + eventId + ":" + attendeeId +
                                " email=" + email +
                                " name=" + name +
                                " role=" + role +
                                " level=" + level +
                                " status=" + status);
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
            }
        }
    }

    static void update(Context context, VEvent event, EntityMessage message) {
        String uid = (event.getUid() == null ? null : event.getUid().getValue());
        if (TextUtils.isEmpty(uid))
            return;

        List<Attendee> attendees = event.getAttendees();
        if (attendees == null || attendees.size() == 0)
            return;

        ParticipationStatus status = attendees.get(0).getParticipationStatus();
        if (!ParticipationStatus.ACCEPTED.equals(status) &&
                !ParticipationStatus.DECLINED.equals(status))
            return;

        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(CalendarContract.Events.CONTENT_URI,
                new String[]{CalendarContract.Events._ID},
                CalendarContract.Events.UID_2445 + " = ?",
                new String[]{uid},
                null)) {
            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(0);

                // https://developer.android.com/guide/topics/providers/calendar-provider#modify-calendar
                Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
                ContentValues values = new ContentValues();
                if (ParticipationStatus.ACCEPTED.equals(status))
                    values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);
                else
                    values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CANCELED);
                int rows = resolver.update(updateUri, values, null, null);

                EntityLog.log(context, EntityLog.Type.General, message,
                        "Updated event id=" + eventId + " uid=" + uid + " rows=" + rows);
            }
        }
    }

    static void delete(Context context, VEvent event, EntityMessage message) {
        String uid = (event.getUid() == null ? null : event.getUid().getValue());
        if (TextUtils.isEmpty(uid))
            return;

        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(CalendarContract.Events.CONTENT_URI,
                new String[]{CalendarContract.Events._ID},
                CalendarContract.Events.UID_2445 + " = ? ",
                new String[]{uid},
                null)) {
            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(0);

                // https://developer.android.com/guide/topics/providers/calendar-provider#delete-event
                Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
                int rows = resolver.delete(deleteUri, null, null);
                EntityLog.log(context, EntityLog.Type.General, message,
                        "Deleted event id=" + eventId + " uid=" + uid + " rows=" + rows);
            }
        }
    }
}
