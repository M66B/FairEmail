/*
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * All Rights Reserved.
 */

/*
 Copyright (c) 2013-2023, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package biweekly.util.com.google.ical.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biweekly.util.DayOfWeek;
import biweekly.util.com.google.ical.values.DateTimeValue;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;
import biweekly.util.com.google.ical.values.TimeValue;

/**
 * Utility methods for working with times and dates.
 * @author Neal Gafter
 * @author Michael Angstadt
 */
public class TimeUtils {
	private static TimeZone ZULU = new SimpleTimeZone(0, "Etc/GMT");

	/**
	 * Gets the UTC timezone.
	 * @return the UTC timezone
	 */
	public static TimeZone utcTimezone() {
		return ZULU;
	}

	/**
	 * Get a "time_t" in milliseconds given a number of seconds since the
	 * Dershowitz/Reingold epoch relative to a given timezone.
	 * @param epochSecs the number of seconds since the Dershowitz/Reingold
	 * epoch relative to the given timezone
	 * @param zone timezone against which epochSecs applies
	 * @return the number of milliseconds since 00:00:00 Jan 1, 1970 GMT
	 */
	private static long timetMillisFromEpochSecs(long epochSecs, TimeZone zone) {
		DateTimeValue date = timeFromSecsSinceEpoch(epochSecs);
		Calendar cal = new GregorianCalendar(zone);
		cal.clear();
		cal.set(date.year(), date.month() - 1, date.day(), date.hour(), date.minute(), date.second());
		return cal.getTimeInMillis();
	}

	private static DateTimeValue convert(DateTimeValue time, TimeZone zone, int sense) {
		if (zone == null || zone.hasSameRules(ZULU) || time.year() == 0) {
			return time;
		}

		TimeZone epochTz, dateTimeValueTz;
		if (sense > 0) {
			//time is in UTC; convert to time in zone provided
			epochTz = ZULU;
			dateTimeValueTz = zone;
		} else {
			//time is in local time; convert to UTC
			epochTz = zone;
			dateTimeValueTz = ZULU;
		}

		long epochSeconds = secsSinceEpoch(time);
		long timetMillis = timetMillisFromEpochSecs(epochSeconds, epochTz);
		return toDateTimeValue(timetMillis, dateTimeValueTz);
	}

	/**
	 * Converts a {@link DateValue} from UTC to another timezone.
	 * @param date the date value (in UTC)
	 * @param zone the timezone to convert to
	 * @return the converted date value
	 */
	public static DateValue fromUtc(DateValue date, TimeZone zone) {
		return (date instanceof DateTimeValue) ? fromUtc((DateTimeValue) date, zone) : date;
	}

	/**
	 * Converts a {@link DateTimeValue} from UTC to another timezone.
	 * @param date the date-time value (in UTC)
	 * @param zone the timezone to convert to
	 * @return the converted date-time value
	 */
	public static DateTimeValue fromUtc(DateTimeValue date, TimeZone zone) {
		return convert(date, zone, +1);
	}

	/**
	 * Converts a {@link DateValue} to UTC.
	 * @param date the date value
	 * @param zone the timezone the date value is in
	 * @return the converted date value
	 */
	public static DateValue toUtc(DateValue date, TimeZone zone) {
		return (date instanceof TimeValue) ? convert((DateTimeValue) date, zone, -1) : date;
	}

	/**
	 * Adds a duration to a date.
	 * @param date the date
	 * @param duration the duration to add to the date
	 * @return the result
	 */
	public static DateValue add(DateValue date, DateValue duration) {
		DTBuilder db = new DTBuilder(date);
		db.year += duration.year();
		db.month += duration.month();
		db.day += duration.day();
		if (duration instanceof TimeValue) {
			TimeValue tdur = (TimeValue) duration;
			db.hour += tdur.hour();
			db.minute += tdur.minute();
			db.second += tdur.second();
			return db.toDateTime();
		}
		return (date instanceof TimeValue) ? db.toDateTime() : db.toDate();
	}

	/**
	 * Calculates the number of days between two dates.
	 * @param date1 the first date
	 * @param date2 the second date
	 * @return the number of days
	 */
	public static int daysBetween(DateValue date1, DateValue date2) {
		return fixedFromGregorian(date1) - fixedFromGregorian(date2);
	}

	/**
	 * Calculates the number of days between two dates.
	 * @param year1 the year of the first date
	 * @param month1 the month of the first date
	 * @param day1 the day of the first date
	 * @param year2 the year of the second date
	 * @param month2 the month of the second date
	 * @param day2 the day of the second date
	 * @return the number of days
	 */
	public static int daysBetween(int year1, int month1, int day1, int year2, int month2, int day2) {
		return fixedFromGregorian(year1, month1, day1) - fixedFromGregorian(year2, month2, day2);
	}

	/**
	 * <p>
	 * Calculates the number of days since the epoch.
	 * </p>
	 * <p>
	 * This is the imaginary beginning of year zero in a hypothetical backward
	 * extension of the Gregorian calendar through time. See
	 * "Calendrical Calculations" by Reingold and Dershowitz.
	 * </p>
	 * @param date the date to start from
	 * @return the number of days
	 */
	private static int fixedFromGregorian(DateValue date) {
		return fixedFromGregorian(date.year(), date.month(), date.day());
	}

	/**
	 * <p>
	 * Calculates the number of days since the epoch.
	 * </p>
	 * <p>
	 * This is the imaginary beginning of year zero in a hypothetical backward
	 * extension of the Gregorian calendar through time. See
	 * "Calendrical Calculations" by Reingold and Dershowitz.
	 * </p>
	 * @param year the year of the date to start from
	 * @param month the month of the date to start from
	 * @param day the day of the date to start from
	 * @return the number of days
	 */
	public static int fixedFromGregorian(int year, int month, int day) {
		int yearM1 = year - 1;
		return 365 * yearM1 + yearM1 / 4 - yearM1 / 100 + yearM1 / 400 + (367 * month - 362) / 12 + (month <= 2 ? 0 : isLeapYear(year) ? -1 : -2) + day;
	}

	/**
	 * Determines if a year is a leap year.
	 * @param year the year
	 * @return true if it's a leap year, false if not
	 */
	public static boolean isLeapYear(int year) {
		return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0));
	}

	/**
	 * Determines how many days are in a year.
	 * @param year the year
	 * @return the number of days
	 */
	public static int yearLength(int year) {
		return isLeapYear(year) ? 366 : 365;
	}

	/**
	 * Calculates the number of days in a month.
	 * @param year the year
	 * @param month the month (in range [1,12])
	 * @return the number of days
	 */
	public static int monthLength(int year, int month) {
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			return 31;
		case 4:
		case 6:
		case 9:
		case 11:
			return 30;
		case 2:
			return isLeapYear(year) ? 29 : 28;
		default:
			throw new AssertionError(month);
		}
	}

	private static int[] MONTH_START_TO_DOY = new int[12];
	static {
		for (int m = 1; m < 12; ++m) {
			MONTH_START_TO_DOY[m] = MONTH_START_TO_DOY[m - 1] + monthLength(1970, m);
		}
	}

	/**
	 * Gets the day of the year for a given date.
	 * @param year the date's year
	 * @param month the date's month
	 * @param date the date's day
	 * @return the day of the year (in range [0-365])
	 */
	public static int dayOfYear(int year, int month, int date) {
		int leapAdjust = month > 2 && isLeapYear(year) ? 1 : 0;
		return MONTH_START_TO_DOY[month - 1] + leapAdjust + date - 1;
	}

	private static final DayOfWeek[] DAYS_OF_WEEK = DayOfWeek.values();

	/**
	 * Gets the day of the week the given date falls on.
	 * @param date the date
	 * @return the day of the week
	 */
	public static DayOfWeek dayOfWeek(DateValue date) {
		int dayIndex = fixedFromGregorian(date.year(), date.month(), date.day()) % 7;
		if (dayIndex < 0) {
			dayIndex += 7;
		}
		return DAYS_OF_WEEK[dayIndex];
	}

	/**
	 * Gets the day of the week of the first day in the given month.
	 * @param year the year
	 * @param month the month (1-12)
	 * @return the day of the week
	 */
	public static DayOfWeek firstDayOfWeekInMonth(int year, int month) {
		int result = fixedFromGregorian(year, month, 1) % 7;
		if (result < 0) {
			result += 7;
		}
		return DAYS_OF_WEEK[result];
	}

	/**
	 * Computes the gregorian time from the number of seconds since the
	 * Proleptic Gregorian Epoch. See "Calendrical Calculations", Reingold and
	 * Dershowitz.
	 * @param secsSinceEpoch the number of seconds since the epoch
	 * @return the gregorian time
	 */
	public static DateTimeValue timeFromSecsSinceEpoch(long secsSinceEpoch) {
		// TODO: should we handle -ve years?
		int secsInDay = (int) (secsSinceEpoch % SECS_PER_DAY);
		int daysSinceEpoch = (int) (secsSinceEpoch / SECS_PER_DAY);
		int approx = (int) ((daysSinceEpoch + 10) * 400L / 146097);
		int year = (daysSinceEpoch >= fixedFromGregorian(approx + 1, 1, 1)) ? approx + 1 : approx;
		int jan1 = fixedFromGregorian(year, 1, 1);
		int priorDays = daysSinceEpoch - jan1;
		int march1 = fixedFromGregorian(year, 3, 1);
		int correction = (daysSinceEpoch < march1) ? 0 : isLeapYear(year) ? 1 : 2;
		int month = (12 * (priorDays + correction) + 373) / 367;
		int month1 = fixedFromGregorian(year, month, 1);
		int day = daysSinceEpoch - month1 + 1;
		int second = secsInDay % 60;
		int minutesInDay = secsInDay / 60;
		int minute = minutesInDay % 60;
		int hour = minutesInDay / 60;
		if (!(hour >= 0 && hour < 24)) {
			throw new AssertionError("Input was: " + secsSinceEpoch + "to make hour: " + hour);
		}
		return new DateTimeValueImpl(year, month, day, hour, minute, second);
	}

	private static final long SECS_PER_DAY = 60L * 60 * 24;

	/**
	 * Computes the number of seconds from the Proleptic Gregorian epoch to the
	 * given time. See "Calendrical Calculations", Reingold and Dershowitz.
	 * @param date the date
	 * @return the number of seconds
	 */
	public static long secsSinceEpoch(DateValue date) {
		long result = fixedFromGregorian(date) * SECS_PER_DAY;
		if (date instanceof TimeValue) {
			TimeValue time = (TimeValue) date;
			result += time.second() + 60 * (time.minute() + 60 * time.hour());
		}
		return result;
	}

	/**
	 * Builds a date-time value that represents the start of the given day (all
	 * time components set to 0).
	 * @param date the day
	 * @return the date-time value
	 */
	public static DateTimeValue dayStart(DateValue date) {
		return new DateTimeValueImpl(date.year(), date.month(), date.day(), 0, 0, 0);
	}

	/**
	 * Converts the given date to a {@link DateValue} object if it is a
	 * {@link TimeValue} instance. If it is not a {@link TimeValue} instance,
	 * then the same date object is returned unchanged.
	 * @param date the date
	 * @return the date value
	 */
	public static DateValue toDateValue(DateValue date) {
		return (date instanceof TimeValue) ? new DateValueImpl(date.year(), date.month(), date.day()) : date;
	}

	private static final TimeZone BOGUS_TIMEZONE = TimeZone.getTimeZone("noSuchTimeZone");

	private static final Pattern UTC_TZID = Pattern.compile("^GMT([+-]0(:00)?)?$|UTC|Zulu|Etc\\/GMT|Greenwich.*", Pattern.CASE_INSENSITIVE);

	/**
	 * Returns the timezone with the given name or null if no such timezone.
	 * @param tzString the timezone name (e.g. "America/New_York")
	 * @return the timezone or null if no such timezone exists
	 */
	public static TimeZone timeZoneForName(String tzString) {
		TimeZone tz = TimeZone.getTimeZone(tzString);
		if (!tz.hasSameRules(BOGUS_TIMEZONE)) {
			return tz;
		}

		/*
		 * See if the user really was asking for GMT because if
		 * TimeZone.getTimeZone can't recognize tzString, then that is what it
		 * will return.
		 */
		Matcher m = UTC_TZID.matcher(tzString);
		return m.matches() ? TimeUtils.utcTimezone() : null;
	}

	/**
	 * Builds a {@link DateTimeValue} object from the given data.
	 * @param millisFromEpoch the number of milliseconds from the epoch
	 * @param zone the timezone the number of milliseconds is in
	 * @return the {@link DateTimeValue} object
	 */
	public static DateTimeValue toDateTimeValue(long millisFromEpoch, TimeZone zone) {
		GregorianCalendar c = new GregorianCalendar(zone);
		c.clear();
		c.setTimeInMillis(millisFromEpoch);
		//@formatter:off
		return new DateTimeValueImpl (
			c.get(Calendar.YEAR),
			c.get(Calendar.MONTH) + 1,
			c.get(Calendar.DAY_OF_MONTH),
			c.get(Calendar.HOUR_OF_DAY),
			c.get(Calendar.MINUTE),
			c.get(Calendar.SECOND)
		);
		//@formatter:on
	}

	private static final TimeValue MIDNIGHT = new TimeValue() {
		public int hour() {
			return 0;
		}

		public int minute() {
			return 0;
		}

		public int second() {
			return 0;
		}
	};

	/**
	 * Gets the time component of a date value.
	 * @param date the date value
	 * @return the date value's time component or a time value representing
	 * midnight if the date value does not have a time component
	 */
	public static TimeValue timeOf(DateValue date) {
		return (date instanceof TimeValue) ? (TimeValue) date : MIDNIGHT;
	}

	private TimeUtils() {
		// uninstantiable
	}
}
