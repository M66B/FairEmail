// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

import biweekly.util.com.google.ical.values.DateTimeValue;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;
import biweekly.util.com.google.ical.values.TimeValue;

/**
 * A mutable buffer used to build {@link DateValue}s and {@link DateTimeValue}s.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class DTBuilder {
	/**
	 * The year.
	 */
	public int year;

	/**
	 * The month. This value is one-indexed, so "1" represents January.
	 */
	public int month;

	/**
	 * The day of the month.
	 */
	public int day;

	/**
	 * The hour.
	 */
	public int hour;

	/**
	 * The minute.
	 */
	public int minute;

	/**
	 * The second.
	 */
	public int second;

	/**
	 * Creates a new date builder.
	 * @param year the initial year
	 * @param month the initial month (this value is one-indexed, so "1"
	 * represents January)
	 * @param day the initial day
	 * @param hour the initial hour
	 * @param minute the initial minute
	 * @param second the initial second
	 */
	public DTBuilder(int year, int month, int day, int hour, int minute, int second) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	/**
	 * Creates a new date builder. This constructor sets the time components to
	 * zero.
	 * @param year the initial year
	 * @param month the initial month (this value is one-indexed, so "1"
	 * represents January)
	 * @param day the initial day
	 */
	public DTBuilder(int year, int month, int day) {
		this(year, month, day, 0, 0, 0);
	}

	/**
	 * Creates a new date builder, initializing it to the given date value.
	 * @param date the date value to initialize the builder with
	 */
	public DTBuilder(DateValue date) {
		this.year = date.year();
		this.month = date.month();
		this.day = date.day();
		if (date instanceof TimeValue) {
			TimeValue tv = (TimeValue) date;
			this.hour = tv.hour();
			this.minute = tv.minute();
			this.second = tv.second();
		}
	}

	/**
	 * Produces a normalized date-time value, using zero for the time fields if
	 * none were provided.
	 * @return the date-time value
	 */
	public DateTimeValue toDateTime() {
		normalize();
		return new DateTimeValueImpl(year, month, day, hour, minute, second);
	}

	/**
	 * Produces a normalized date value.
	 * @return the date value
	 */
	public DateValue toDate() {
		normalize();
		return new DateValueImpl(year, month, day);
	}

	/**
	 * <p>
	 * Compares the value of this builder to a given {@link DateValue}. Note
	 * that this method's behavior is undefined unless {@link #normalize} is
	 * called first.
	 * </p>
	 * <p>
	 * If you're not sure whether it's appropriate to use this method, use
	 * <code>toDateValue().compareTo(dv)</code> instead.
	 * </p>
	 * @param date the date value to compare against
	 * @return a negative value if this date builder is less than the given date
	 * value, a positive value if this date builder is greater than the given
	 * date value, or zero if they are equal
	 */
	public int compareTo(DateValue date) {
		long dvComparable = (((((long) date.year()) << 4) + date.month()) << 5) + date.day();
		long dtbComparable = ((((long) year << 4) + month << 5)) + day;
		if (date instanceof TimeValue) {
			TimeValue tv = (TimeValue) date;
			dvComparable = (((((dvComparable << 5) + tv.hour()) << 6) + tv.minute()) << 6) + tv.second();
			dtbComparable = (((((dtbComparable << 5) + hour) << 6) + minute) << 6) + second;
		}
		long delta = dtbComparable - dvComparable;
		return delta < 0 ? -1 : delta == 0 ? 0 : 1;
	}

	/**
	 * Makes sure that the fields are in the proper ranges (for example,
	 * converts 32 January to 1 February, and 25:00:00 to 1:00:00 of the next
	 * day).
	 */
	public void normalize() {
		normalizeTime();
		normalizeDate();
	}

	@Override
	public String toString() {
		return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DTBuilder)) {
			return false;
		}
		DTBuilder that = (DTBuilder) o;
		return this.year == that.year && this.month == that.month && this.day == that.day && this.hour == that.hour && this.minute == that.minute && this.second == that.second;
	}

	@Override
	public int hashCode() {
		return ((((((((year << 4) + month << 5) + day) << 5) + hour) << 6) + minute) << 6) + second;
	}

	/**
	 * Makes sure that the time fields are in the proper ranges (for example,
	 * converts 25:00:00 to 1:00:00 of the next day).
	 */
	private void normalizeTime() {
		int addMinutes = ((second < 0) ? (second - 59) : second) / 60;
		second -= addMinutes * 60;
		minute += addMinutes;
		int addHours = ((minute < 0) ? (minute - 59) : minute) / 60;
		minute -= addHours * 60;
		hour += addHours;
		int addDays = ((hour < 0) ? (hour - 23) : hour) / 24;
		hour -= addDays * 24;
		day += addDays;
	}

	/**
	 * Makes sure that the date fields are in the proper ranges (for example,
	 * converts 32 January to 1 February).
	 */
	private void normalizeDate() {
		while (day <= 0) {
			int days = TimeUtils.yearLength(month > 2 ? year : year - 1);
			day += days;
			--year;
		}

		if (month <= 0) {
			int years = month / 12 - 1;
			year += years;
			month -= 12 * years;
		} else if (month > 12) {
			int years = (month - 1) / 12;
			year += years;
			month -= 12 * years;
		}

		while (true) {
			if (month == 1) {
				int yearLength = TimeUtils.yearLength(year);
				if (day > yearLength) {
					++year;
					day -= yearLength;
				}
			}

			int monthLength = TimeUtils.monthLength(year, month);
			if (day <= monthLength) {
				break;
			}

			day -= monthLength;
			if (++month > 12) {
				month -= 12;
				++year;
			}
		}
	}
}
