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

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.io.ParseWarning;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.property.RecurrenceRuleScribe;
import biweekly.io.text.ICalReader;
import biweekly.parameter.ParticipationStatus;
import biweekly.property.Action;
import biweekly.property.Attendee;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
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

    static ICalendar parse(Context context, File file) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            // https://en.wikipedia.org/wiki/Byte_order_mark#UTF-8
            byte[] utf8_bom = "\uFEFF".getBytes(StandardCharsets.UTF_8);
            byte[] bom = new byte[utf8_bom.length];
            is.mark(bom.length);
            is.read(bom);
            if (!Arrays.equals(bom, utf8_bom))
                is.reset();

            try (ICalReader reader = new ICalReader(is)) {
                ICalendar icalendar = reader.readNext();

                for (ParseWarning warning : reader.getWarnings())
                    EntityLog.log(context, "Event warning " + warning);

                // https://icalendar.org/validator.html
                if (icalendar == null)
                    throw new IOException("Invalid iCal file");

                return icalendar;
            }
        }
    }

    static String getTimeZoneID(ICalendar icalendar, ICalProperty property) {
        TimezoneInfo tzinfo = icalendar.getTimezoneInfo();
        TimezoneAssignment tza = (tzinfo == null ? null : tzinfo.getTimezone(property));
        TimeZone tz = (tza == null ? null : tza.getTimeZone());
        String tzid = (tz == null ? null : tz.getID());
        if (tzid == null)
            tzid = TimeZone.getDefault().getID();
        return tzid;
    }

    static Uri getOnlineMeetingUrl(Context context, VEvent event) {
        try {
            RawProperty prop = event.getExperimentalProperty("X-GOOGLE-CONFERENCE");
            if (prop == null)
                prop = event.getExperimentalProperty("X-MICROSOFT-ONLINEMEETINGEXTERNALLINK");
            if (prop == null)
                prop = event.getExperimentalProperty("X-MICROSOFT-SKYPETEAMSMEETINGURL");
            if (prop == null)
                return null;
            String url = prop.getValue();
            if (TextUtils.isEmpty(url))
                return null;
            Uri uri = Uri.parse(url);
            return (uri.isHierarchical() ? uri : null);
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
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
                       EntityAccount account, EntityMessage message) {
        String selectedAccount;
        String selectedName;
        try {
            JSONObject jselected = new JSONObject(account.calendar);
            selectedAccount = jselected.getString("account");
            selectedName = jselected.optString("name", null);
        } catch (Throwable ex) {
            Log.i(ex);
            selectedAccount = account.calendar;
            selectedName = null;
        }

        return insert(context, icalendar, event, status, selectedAccount, selectedName, message);
    }

    static Long insert(Context context, ICalendar icalendar, VEvent event, int status,
                       String selectedAccount, String selectedName, EntityMessage message) {
        Long existId = null;
        String uid = (event.getUid() == null ? null : event.getUid().getValue());
        if (!TextUtils.isEmpty(uid)) {
            existId = exists(context, selectedAccount, selectedName, uid);
            if (existId != null) {
                EntityLog.log(context, EntityLog.Type.General, message, "Event exists uid=" + uid + " id=" + existId);
            }
        }

        String organizer = (event.getOrganizer() == null ? null : event.getOrganizer().getEmail());

        String summary = (event.getSummary() == null ? null : event.getSummary().getValue());
        String description = (event.getDescription() == null ? null : event.getDescription().getValue());
        String location = (event.getLocation() == null ? null : event.getLocation().getValue());

        ICalDate start = (event.getDateStart() == null ? null : event.getDateStart().getValue());
        ICalDate end = (event.getDateEnd() == null ? null : event.getDateEnd().getValue());

        String tzstart = getTimeZoneID(icalendar, event.getDateStart());
        String tzend = getTimeZoneID(icalendar, event.getDateEnd());

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

                values.put(CalendarContract.Events.DTSTART, start.getTime());
                values.put(CalendarContract.Events.DTEND, end.getTime());
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC");
                values.put(CalendarContract.Events.EVENT_END_TIMEZONE, "UTC");

                if (rrule != null)
                    values.put(CalendarContract.Events.RRULE, rrule);

                if (!TextUtils.isEmpty(summary))
                    values.put(CalendarContract.Events.TITLE, summary);
                if (!TextUtils.isEmpty(description))
                    values.put(CalendarContract.Events.DESCRIPTION, description);
                if (!TextUtils.isEmpty(location))
                    values.put(CalendarContract.Events.EVENT_LOCATION, location);
                values.put(CalendarContract.Events.STATUS, status);

                long eventId;
                if (existId == null) {
                    Uri uri = resolver.insert(CalendarContract.Events.CONTENT_URI, values);
                    eventId = Long.parseLong(uri.getLastPathSegment());
                    EntityLog.log(context, EntityLog.Type.General, message, "Inserted event" +
                            " id=" + calId + ":" + eventId +
                            " uid=" + uid +
                            " organizer=" + organizer +
                            " start=" + new Date(start.getTime()) + "/" + tzstart +
                            " end=" + new Date(end.getTime()) + "/" + tzend +
                            " rrule=" + rrule +
                            " summary=" + summary +
                            " location=" + location +
                            " status=" + status);
                } else {
                    /*
                        java.lang.IllegalArgumentException: Cannot have both DTEND and DURATION in an event
                                at android.database.DatabaseUtils.readExceptionFromParcel(DatabaseUtils.java:172)
                                at android.database.DatabaseUtils.readExceptionFromParcel(DatabaseUtils.java:142)
                                at android.content.ContentProviderProxy.update(ContentProviderNative.java:685)
                                at android.content.ContentResolver.update(ContentResolver.java:2416)
                                at android.content.ContentResolver.update(ContentResolver.java:2378)
                                at eu.faircode.email.CalendarHelper.insert(CalendarHelper:285)
                     */
                    values.put(CalendarContract.Events.DURATION, "");

                    Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, existId);
                    int rows = resolver.update(uri, values, null, null);
                    EntityLog.log(context, EntityLog.Type.General, message, "Updated event" +
                            " id=" + calId + ":" + existId +
                            " uid=" + uid +
                            " organizer=" + organizer +
                            " start=" + new Date(start.getTime()) + "/" + tzstart +
                            " end=" + new Date(end.getTime()) + "/" + tzend +
                            " rrule=" + rrule +
                            " summary=" + summary +
                            " location=" + location +
                            " status=" + status +
                            " rows=" + rows);

                    rows = resolver.delete(CalendarContract.Attendees.CONTENT_URI,
                            CalendarContract.Attendees.EVENT_ID + " = ?",
                            new String[]{Long.toString(existId)});
                    EntityLog.log(context, EntityLog.Type.General, message, "Deleted event attendees for update" +
                            " id=" + calId + ":" + existId +
                            " rows=" + rows);

                    rows = resolver.delete(CalendarContract.Reminders.CONTENT_URI,
                            CalendarContract.Reminders.EVENT_ID + " = ?",
                            new String[]{Long.toString(existId)});
                    EntityLog.log(context, EntityLog.Type.General, message, "Deleted event reminders for update" +
                            " id=" + calId + ":" + existId +
                            " rows=" + rows);

                    eventId = existId;
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean ical_email = prefs.getBoolean("ical_email", false);

                for (Attendee a : event.getAttendees())
                    try {
                        String email = a.getEmail();
                        String name = a.getCommonName();
                        String role = (a.getRole() == null ? null : a.getRole().getValue());
                        String level = (a.getParticipationLevel() == null ? null
                                : a.getParticipationLevel().getValue(icalendar.getVersion()));
                        String pstatus = (a.getParticipationStatus() == null ? null : a.getParticipationStatus().getValue());

                        ContentValues avalues = new ContentValues();

                        if (!TextUtils.isEmpty(email) && ical_email)
                            avalues.put(CalendarContract.Attendees.ATTENDEE_EMAIL, email);

                        if (TextUtils.isEmpty(name))
                            name = email;
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
                        else  // NEEDS-ACTION
                            avalues.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_INVITED);

                        avalues.put(CalendarContract.Attendees.EVENT_ID, eventId);

                        Uri auri = resolver.insert(CalendarContract.Attendees.CONTENT_URI, avalues);
                        long attendeeId = Long.parseLong(auri.getLastPathSegment());
                        EntityLog.log(context, EntityLog.Type.General, message, "Inserted event attendee" +
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

                int arows = 0;
                String email = attendees.get(0).getEmail();
                if (!TextUtils.isEmpty(email)) {
                    ContentValues avalues = new ContentValues();
                    avalues.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED);

                    arows = resolver.update(
                            CalendarContract.Attendees.CONTENT_URI,
                            avalues,
                            CalendarContract.Attendees.EVENT_ID + " =? AND " + CalendarContract.Attendees.ATTENDEE_EMAIL + " =?",
                            new String[]{Long.toString(eventId), email});
                }

                EntityLog.log(context, EntityLog.Type.General, message,
                        "Updated event id=" + eventId + " uid=" + uid + " email=" + email +
                                " rows=" + rows + "/" + arows);
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
