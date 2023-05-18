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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.property.RecurrenceRuleScribe;
import biweekly.parameter.ParticipationStatus;
import biweekly.property.Action;
import biweekly.property.Attendee;
import biweekly.property.RecurrenceRule;
import biweekly.property.Trigger;
import biweekly.util.Duration;
import biweekly.util.ICalDate;

public class CalendarHelper {
    static boolean isWeekend(Context context, Calendar calendar) {
        return isWeekend(context, calendar.get(Calendar.DAY_OF_WEEK));
    }

    static boolean isWeekend(Context context, int aday) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String weekend = prefs.getString("weekend", Calendar.SATURDAY + "," + Calendar.SUNDAY);
        for (String day : weekend.split(","))
            if (!TextUtils.isEmpty(day) && aday == Integer.parseInt(day))
                return true;
        return false;
    }

    static String formatHour(Context context, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
        cal.set(Calendar.MINUTE, minutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return Helper.getTimeInstance(context, SimpleDateFormat.SHORT).format(cal.getTime());
    }

    static Long exists(Context context, String selectedAccount, String selectedName, String uid) {
        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(CalendarContract.Events.CONTENT_URI,
                new String[]{CalendarContract.Events._ID},
                CalendarContract.Calendars.ACCOUNT_NAME + " = ?" +
                        " AND " + (selectedName == null
                        ? "(" + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " IS NULL" +
                        " OR " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ''" +
                        " OR " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ?)"
                        : CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ?") +
                        " AND " + CalendarContract.Events.UID_2445 + " = ?",
                selectedName == null
                        ? new String[]{selectedAccount, selectedAccount, uid}
                        : new String[]{selectedAccount, selectedName, uid},
                null)) {
            if (cursor.moveToNext())
                return cursor.getLong(0);
            else
                return null;
        }
    }

    static Long insert(Context context, ICalendar icalendar, VEvent event, int status,
                       String selectedAccount, String selectedName, EntityMessage message) {
        String uid = (event.getUid() == null ? null : event.getUid().getValue());
        if (!TextUtils.isEmpty(uid)) {
            Long existId = exists(context, selectedAccount, selectedName, uid);
            if (existId != null) {
                EntityLog.log(context, EntityLog.Type.General, message, "Event exists uid=" + uid + " id=" + existId);
                if (BuildConfig.DEBUG)
                    delete(context, event, message);
                else
                    return existId;
            }
        }

        String organizer = (event.getOrganizer() == null ? null : event.getOrganizer().getEmail());

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

        if (start == null || end == null) {
            EntityLog.log(context, EntityLog.Type.General, message,
                    "Event start=" + start + " end=" + end);
            return null;
        }

        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(CalendarContract.Calendars.CONTENT_URI,
                new String[]{CalendarContract.Calendars._ID},
                CalendarContract.Calendars.VISIBLE + " <> 0" +
                        " AND " + CalendarContract.Calendars.ACCOUNT_NAME + " = ?" +
                        " AND " + (selectedName == null
                        ? "(" + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " IS NULL" +
                        " OR " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ''" +
                        " OR " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ?)"
                        : CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ?"),
                selectedName == null
                        ? new String[]{selectedAccount, selectedAccount}
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
                if (!TextUtils.isEmpty(organizer))
                    values.put(CalendarContract.Events.ORGANIZER, organizer);

                // Assume one time zone
                TimezoneInfo tzinfo = icalendar.getTimezoneInfo();
                TimezoneAssignment tza = (tzinfo == null ? null : tzinfo.getTimezone(event.getDateStart()));
                TimeZone tz = (tza == null ? null : tza.getTimeZone());
                values.put(CalendarContract.Events.EVENT_TIMEZONE,
                        tz == null ? TimeZone.getDefault().getID() : tz.getID());

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
                values.put(CalendarContract.Events.STATUS, status);

                Uri uri = resolver.insert(CalendarContract.Events.CONTENT_URI, values);
                long eventId = Long.parseLong(uri.getLastPathSegment());
                EntityLog.log(context, EntityLog.Type.General, message, "Inserted event" +
                        " id=" + calId + ":" + eventId +
                        " uid=" + uid +
                        " organizer=" + organizer +
                        " tz=" + (tz == null ? null : tz.getID()) +
                        " start=" + new Date(start.getTime()) +
                        " end=" + new Date(end.getTime()) +
                        " rrule=" + rrule +
                        " summary=" + summary +
                        " location=" + location +
                        " status=" + status);

                for (Attendee a : event.getAttendees())
                    try {
                        String email = a.getEmail();
                        String name = a.getCommonName();
                        String role = (a.getRole() == null ? null : a.getRole().getValue());
                        String level = (a.getParticipationLevel() == null ? null
                                : a.getParticipationLevel().getValue(icalendar.getVersion()));
                        String pstatus = (a.getParticipationStatus() == null ? null : a.getParticipationStatus().getValue());

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

                        if ("ACCEPTED".equals(pstatus) || "CONFIRMED".equals(pstatus))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED);
                        else if ("DECLINED".equals(pstatus))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED);
                        else if ("TENTATIVE".equals(pstatus))
                            avalues.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_TENTATIVE);
                        else if ("NEEDS-ACTION".equals(pstatus))
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
                                " status=" + pstatus);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                if (status == CalendarContract.Events.STATUS_CONFIRMED) {
                    for (VAlarm valarm : event.getAlarms())
                        try {
                            // BEGIN:VALARM
                            // ACTION:DISPLAY
                            // DESCRIPTION:This is an event reminder
                            // TRIGGER:-P0DT0H30M0S
                            // END:VALARM
                            Action action = valarm.getAction();
                            Trigger trigger = valarm.getTrigger();
                            EntityLog.log(context, EntityLog.Type.General, message, "Event reminder" +
                                    " action=" + (action == null ? null : action.getValue()) +
                                    " related=" + (trigger == null ? null : trigger.getRelated()) +
                                    " duration=" + (trigger == null ? null : trigger.getDuration()));
                            if (action != null && trigger != null &&
                                    Action.DISPLAY.equals(action.getValue()) &&
                                    trigger.getRelated() == null && trigger.getDuration() != null) {
                                Duration duration = trigger.getDuration();
                                Integer w = duration.getWeeks();
                                Integer d = duration.getDays();
                                Integer h = duration.getHours();
                                Integer m = duration.getMinutes();

                                int minutes = (w == null ? 0 : w * 7 * 24 * 60) +
                                        (d == null ? 0 : d * 24 * 60) +
                                        (h == null ? 0 : h * 60) +
                                        (m == null ? 0 : m);

                                ContentValues cv = new ContentValues();
                                cv.put(CalendarContract.Reminders.EVENT_ID, eventId);
                                cv.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                                cv.put(CalendarContract.Reminders.MINUTES, minutes);

                                Uri reminder = resolver.insert(CalendarContract.Reminders.CONTENT_URI, cv);
                                EntityLog.log(context, EntityLog.Type.General, message, "Inserted event reminder" +
                                        " w=" + w + " d=" + d + " h=" + h + " m=" + m +
                                        " uri=" + reminder);
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                }

                return eventId;
            }
        }

        return null;
    }

    static void update(Context context, VEvent event, EntityMessage message) {
        String uid = (event.getUid() == null ? null : event.getUid().getValue());
        if (TextUtils.isEmpty(uid)) {
            EntityLog.log(context, EntityLog.Type.General, message,
                    "Update event: no uid");
            return;
        }

        List<Attendee> attendees = event.getAttendees();
        if (attendees == null || attendees.size() == 0) {
            EntityLog.log(context, EntityLog.Type.General, message,
                    "Update event: no attendees");
            return;
        }

        ParticipationStatus status = attendees.get(0).getParticipationStatus();
        if (!ParticipationStatus.ACCEPTED.equals(status) &&
                !ParticipationStatus.DECLINED.equals(status)) {
            EntityLog.log(context, EntityLog.Type.General, message,
                    "Update event: not accepted/declined");
            return;
        }

        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(CalendarContract.Events.CONTENT_URI,
                new String[]{CalendarContract.Events._ID},
                CalendarContract.Events.UID_2445 + " = ?",
                new String[]{uid},
                null)) {
            if (cursor.getCount() == 0)
                EntityLog.log(context, EntityLog.Type.General, message,
                        "Update event: uid not found");
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
