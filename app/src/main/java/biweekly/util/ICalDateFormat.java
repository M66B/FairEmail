package biweekly.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

/**
 * Defines all of the date formats that are used in iCalendar objects, and also
 * parses/formats iCalendar dates. These date formats are defined in the ISO8601
 * specification.
 * @author Michael Angstadt
 */
public enum ICalDateFormat {
	//@formatter:off
	/**
	 * Example: 20120701
	 */
	DATE_BASIC(
	"yyyyMMdd"),
	
	/**
	 * Example: 2012-07-01
	 */
	DATE_EXTENDED(
	"yyyy-MM-dd"),
	
	/**
	 * Example: 20120701T142110-0500
	 */
	DATE_TIME_BASIC(
	"yyyyMMdd'T'HHmmssZ"),
	
	/**
	 * Example: 20120701T142110
	 */
	DATE_TIME_BASIC_WITHOUT_TZ(
	"yyyyMMdd'T'HHmmss"),
	
	/**
	 * Example: 2012-07-01T14:21:10-05:00
	 */
	DATE_TIME_EXTENDED(
	"yyyy-MM-dd'T'HH:mm:ssZ"){
		@Override
		public DateFormat getDateFormat(TimeZone timezone) {
			DateFormat df = new SimpleDateFormat(formatStr, Locale.ROOT){
				private static final long serialVersionUID = -297452842012115768L;
				
				@Override
				public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition){
					StringBuffer sb = super.format(date, toAppendTo, fieldPosition);
					
					//add a colon between the hour and minute offsets
					sb.insert(sb.length()-2, ':');
					
					return sb;
				}
			};
			
			if (timezone != null){
				df.setTimeZone(timezone);
			}
			
			return df;
		}
	},
	
	/**
	 * Example: 2012-07-01T14:21:10
	 */
	DATE_TIME_EXTENDED_WITHOUT_TZ(
	"yyyy-MM-dd'T'HH:mm:ss"),
	
	/**
	 * Example: 20120701T192110Z
	 */
	UTC_TIME_BASIC(
	"yyyyMMdd'T'HHmmss'Z'"){
		@Override
		public DateFormat getDateFormat(TimeZone timezone) {
			//always use the UTC timezone
			timezone = TimeZone.getTimeZone("UTC");
			return super.getDateFormat(timezone);
		}
	},
	
	/**
	 * Example: 2012-07-01T19:21:10Z
	 */
	UTC_TIME_EXTENDED(
	"yyyy-MM-dd'T'HH:mm:ss'Z'"){
		@Override
		public DateFormat getDateFormat(TimeZone timezone) {
			//always use the UTC timezone
			timezone = TimeZone.getTimeZone("UTC");
			return super.getDateFormat(timezone);
		}
	};
	//@formatter:on

	/**
	 * The {@link SimpleDateFormat} format string used for parsing dates.
	 */
	protected final String formatStr;

	/**
	 * @param formatStr the {@link SimpleDateFormat} format string used for
	 * parsing dates.
	 */
	ICalDateFormat(String formatStr) {
		this.formatStr = formatStr;
	}

	/**
	 * Builds a {@link DateFormat} object for parsing and formating dates in
	 * this ISO format.
	 * @return the {@link DateFormat} object
	 */
	public DateFormat getDateFormat() {
		return getDateFormat(null);
	}

	/**
	 * Builds a {@link DateFormat} object for parsing and formating dates in
	 * this ISO format.
	 * @param timezone the timezone the date is in or null for the default
	 * timezone
	 * @return the {@link DateFormat} object
	 */
	public DateFormat getDateFormat(TimeZone timezone) {
		DateFormat df = new SimpleDateFormat(formatStr, Locale.ROOT);
		if (timezone != null) {
			df.setTimeZone(timezone);
		}
		return df;
	}

	/**
	 * Formats a date in this ISO format.
	 * @param date the date to format
	 * @return the date string
	 */
	public String format(Date date) {
		return format(date, null);
	}

	/**
	 * Formats a date in this ISO format.
	 * @param date the date to format
	 * @param timezone the timezone to format the date in or null for the
	 * default timezone
	 * @return the date string
	 */
	public String format(Date date, TimeZone timezone) {
		DateFormat df = getDateFormat(timezone);
		return df.format(date);
	}

	/**
	 * Parses an iCalendar date.
	 * @param dateStr the date string to parse (e.g. "20130609T181023Z")
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date string isn't in one of the
	 * accepted ISO8601 formats
	 */
	public static Date parse(String dateStr) {
		return parse(dateStr, null);
	}

	/**
	 * Parses an iCalendar date.
	 * @param dateStr the date string to parse (e.g. "20130609T181023Z")
	 * @param timezone the timezone to parse the date under or null to use the
	 * JVM's default timezone. If the date string contains its own UTC offset,
	 * then that will be used instead.
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date string isn't in one of the
	 * accepted ISO8601 formats
	 */
	public static Date parse(String dateStr, TimeZone timezone) {
		TimestampPattern p = new TimestampPattern(dateStr);
		if (!p.matches()) {
			throw parseException(dateStr);
		}

		if (p.hasOffset()) {
			timezone = TimeZone.getTimeZone("UTC");
		} else if (timezone == null) {
			timezone = TimeZone.getDefault();
		}

		Calendar c = Calendar.getInstance(timezone);
		c.clear();

		c.set(Calendar.YEAR, p.year());
		c.set(Calendar.MONTH, p.month() - 1);
		c.set(Calendar.DATE, p.date());

		if (p.hasTime()) {
			c.set(Calendar.HOUR_OF_DAY, p.hour());
			c.set(Calendar.MINUTE, p.minute());
			c.set(Calendar.SECOND, p.second());
			c.set(Calendar.MILLISECOND, p.millisecond());

			if (p.hasOffset()) {
				c.set(Calendar.ZONE_OFFSET, p.offsetMillis());
			}
		}

		return c.getTime();
	}

	/**
	 * Wrapper for a complex regular expression that parses multiple date
	 * formats.
	 */
	private static class TimestampPattern {
		//@formatter:off
		private static final Pattern regex = Pattern.compile(
			"^(\\d{4})-?(\\d{2})-?(\\d{2})" +
			"(" +
				"T(\\d{2}):?(\\d{2}):?(\\d{2})(\\.\\d+)?" +
				"(" +
					"Z|([-+])((\\d{2})|((\\d{2}):?(\\d{2})))" +
				")?" +
			")?$"
		);
		//@formatter:on

		private final Matcher m;
		private final boolean matches;

		public TimestampPattern(String str) {
			m = regex.matcher(str);
			matches = m.find();
		}

		public boolean matches() {
			return matches;
		}

		public int year() {
			return parseInt(1);
		}

		public int month() {
			return parseInt(2);
		}

		public int date() {
			return parseInt(3);
		}

		public boolean hasTime() {
			return m.group(5) != null;
		}

		public int hour() {
			return parseInt(5);
		}

		public int minute() {
			return parseInt(6);
		}

		public int second() {
			return parseInt(7);
		}

		public int millisecond() {
			if (m.group(8) == null) {
				return 0;
			}

			double ms = Double.parseDouble(m.group(8)) * 1000;
			return (int) Math.round(ms);
		}

		public boolean hasOffset() {
			return m.group(9) != null;
		}

		public int offsetMillis() {
			if (m.group(9).equals("Z")) {
				return 0;
			}

			int positive = m.group(10).equals("+") ? 1 : -1;

			int offsetHour, offsetMinute;
			if (m.group(12) != null) {
				offsetHour = parseInt(12);
				offsetMinute = 0;
			} else {
				offsetHour = parseInt(14);
				offsetMinute = parseInt(15);
			}

			return (offsetHour * 60 * 60 * 1000 + offsetMinute * 60 * 1000) * positive;
		}

		private int parseInt(int group) {
			return Integer.parseInt(m.group(group));
		}
	}

	/**
	 * Determines whether a date string has a time component.
	 * @param dateStr the date string (e.g. "20130601T120000")
	 * @return true if it has a time component, false if not
	 */
	public static boolean dateHasTime(String dateStr) {
		return dateStr.contains("T");
	}

	/**
	 * Determines whether a date string is in UTC time or has a timezone offset.
	 * @param dateStr the date string (e.g. "20130601T120000Z",
	 * "20130601T120000-0400")
	 * @return true if it has a timezone, false if not
	 */
	public static boolean dateHasTimezone(String dateStr) {
		return isUTC(dateStr) || dateStr.matches(".*?[-+]\\d\\d:?\\d\\d");
	}

	/**
	 * Determines if a date string is in UTC time.
	 * @param dateStr the date string (e.g. "20130601T120000Z")
	 * @return true if it's in UTC, false if not
	 */
	public static boolean isUTC(String dateStr) {
		return dateStr.endsWith("Z");
	}

	/**
	 * Gets the {@link TimeZone} object that corresponds to the given ID.
	 * @param timezoneId the timezone ID (e.g. "America/New_York")
	 * @return the timezone object or null if not found
	 */
	public static TimeZone parseTimeZoneId(String timezoneId) {
		TimeZone timezone = TimeZone.getTimeZone(timezoneId);
		return "GMT".equals(timezone.getID()) && !"GMT".equalsIgnoreCase(timezoneId) ? null : timezone;
	}

	private static IllegalArgumentException parseException(String dateStr) {
		return new IllegalArgumentException("Date string \"" + dateStr + "\" is not in a valid ISO-8601 format.");
	}
}
