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

package biweekly.util.com.google.ical.iter;

import biweekly.util.ByDay;
import biweekly.util.DayOfWeek;
import biweekly.util.com.google.ical.util.DTBuilder;
import biweekly.util.com.google.ical.util.Predicate;
import biweekly.util.com.google.ical.util.Predicates;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.TimeValue;

/**
 * <p>
 * Factory for creating predicates used to filter out dates produced by a
 * generator that do not pass some secondary criterion. For example, the
 * recurrence rule below should generate every Friday the 13th:
 * </p>
 * 
 * <pre>
 * FREQ=MONTHLY;BYDAY=FR;BYMONTHDAY=13
 * </pre>
 * <p>
 * It is implemented as a generator that generates the 13th of every month (a
 * {@code byMonthDay} generator), and then the results of that are filtered by a
 * {@code byDayFilter} that tests whether the date falls on Friday.
 * </p>
 * <p>
 * A filter returns true to indicate the item is included in the recurrence.
 * </p>
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
class Filters {
	/**
	 * Constructs a day filter based on a BYDAY rule.
	 * @param days the BYDAY values
	 * @param weeksInYear true if the week numbers are meant to be weeks in the
	 * current year, false if they are meant to be weeks in the current month
	 * @param weekStart the day of the week that the week starts on
	 * @return the filter
	 */
	static Predicate<DateValue> byDayFilter(final ByDay[] days, final boolean weeksInYear, final DayOfWeek weekStart) {
		return new Predicate<DateValue>() {
			private static final long serialVersionUID = 1636822853835207274L;

			public boolean apply(DateValue date) {
				DayOfWeek dow = TimeUtils.dayOfWeek(date);
				int nDays;
				DayOfWeek firstDayOfWeek;

				//where does date appear in the year or month?
				//in [0, lengthOfMonthOrYear - 1]
				int instance;
				if (weeksInYear) {
					nDays = TimeUtils.yearLength(date.year());
					firstDayOfWeek = TimeUtils.firstDayOfWeekInMonth(date.year(), 1);
					instance = TimeUtils.dayOfYear(date.year(), date.month(), date.day());
				} else {
					nDays = TimeUtils.monthLength(date.year(), date.month());
					firstDayOfWeek = TimeUtils.firstDayOfWeekInMonth(date.year(), date.month());
					instance = date.day() - 1;
				}

				//which week of the year or month does this date fall on?
				//one-indexed
				int dateWeekNo = instance / 7;
				if (weekStart.getCalendarConstant() <= dow.getCalendarConstant()) {
					dateWeekNo += 1;
				}

				/*
				 * TODO(msamuel): According to section 4.3.10:
				 * 
				 * Week number one of the calendar year is the first week which
				 * contains at least four (4) days in that calendar year. This
				 * rule part is only valid for YEARLY rules.
				 * 
				 * That's mentioned under the BYWEEKNO rule, and there's no
				 * mention of it in the earlier discussion of the BYDAY rule.
				 * Does it apply to yearly week numbers calculated for BYDAY
				 * rules in a FREQ=YEARLY rule?
				 */

				for (int i = days.length - 1; i >= 0; i--) {
					ByDay day = days[i];

					if (day.getDay() == dow) {
						Integer weekNo = day.getNum();
						if (weekNo == null || weekNo == 0) {
							return true;
						}

						if (weekNo < 0) {
							weekNo = Util.invertWeekdayNum(day, firstDayOfWeek, nDays);
						}

						if (dateWeekNo == weekNo) {
							return true;
						}
					}
				}
				return false;
			}
		};
	}

	/**
	 * Constructs a day filter based on a BYDAY rule.
	 * @param monthDays days of the month (values must be in range [-31,31])
	 * @return the filter
	 */
	static Predicate<DateValue> byMonthDayFilter(final int[] monthDays) {
		return new Predicate<DateValue>() {
			private static final long serialVersionUID = -1618039447294490037L;

			public boolean apply(DateValue date) {
				int nDays = TimeUtils.monthLength(date.year(), date.month());
				for (int i = monthDays.length - 1; i >= 0; i--) {
					int day = monthDays[i];
					if (day < 0) {
						day += nDays + 1;
					}
					if (day == date.day()) {
						return true;
					}
				}
				return false;
			}
		};
	}

	/**
	 * Constructs a filter that accepts only every X week starting from the week
	 * containing the given date.
	 * @param interval the interval (for example, 3 for "every third week"; must
	 * be &gt; 0)
	 * @param weekStart the day of the week that the week starts on
	 * @param dtStart the filter will start at the week that contains this date
	 * @return the filter
	 */
	static Predicate<DateValue> weekIntervalFilter(final int interval, final DayOfWeek weekStart, final DateValue dtStart) {
		return new Predicate<DateValue>() {
			private static final long serialVersionUID = 7059994888520369846L;
			//the latest day with day of week weekStart on or before dtStart
			DateValue wkStart;
			{
				DTBuilder wkStartB = new DTBuilder(dtStart);
				wkStartB.day -= (7 + TimeUtils.dayOfWeek(dtStart).getCalendarConstant() - weekStart.getCalendarConstant()) % 7;
				wkStart = wkStartB.toDate();
			}

			public boolean apply(DateValue date) {
				int daysBetween = TimeUtils.daysBetween(date, wkStart);
				if (daysBetween < 0) {
					//date must be before dtStart.  Shouldn't occur in practice.
					daysBetween += (interval * 7 * (1 + daysBetween / (-7 * interval)));
				}
				int off = (daysBetween / 7) % interval;
				return off == 0;
			}
		};
	}

	private static final int LOW_24_BITS = ~(-1 << 24);
	private static final long LOW_60_BITS = ~(-1L << 60);

	/**
	 * Constructs an hour filter based on a BYHOUR rule.
	 * @param hours hours of the day (values must be in range [0,23])
	 * @return the filter
	 */
	static Predicate<DateValue> byHourFilter(int[] hours) {
		int hoursByBit = 0;
		for (int hour : hours) {
			hoursByBit |= 1 << hour;
		}
		if ((hoursByBit & LOW_24_BITS) == LOW_24_BITS) {
			return Predicates.alwaysTrue();
		}
		final int bitField = hoursByBit;
		return new Predicate<DateValue>() {
			private static final long serialVersionUID = -6284974028385246889L;

			public boolean apply(DateValue date) {
				if (!(date instanceof TimeValue)) {
					return false;
				}
				TimeValue tv = (TimeValue) date;
				return (bitField & (1 << tv.hour())) != 0;
			}
		};
	}

	/**
	 * Constructs a minute filter based on a BYMINUTE rule.
	 * @param minutes minutes of the hour (values must be in range [0,59])
	 * @return the filter
	 */
	static Predicate<DateValue> byMinuteFilter(int[] minutes) {
		long minutesByBit = 0;
		for (int minute : minutes) {
			minutesByBit |= 1L << minute;
		}
		if ((minutesByBit & LOW_60_BITS) == LOW_60_BITS) {
			return Predicates.alwaysTrue();
		}
		final long bitField = minutesByBit;
		return new Predicate<DateValue>() {
			private static final long serialVersionUID = 5028303473420393470L;

			public boolean apply(DateValue date) {
				if (!(date instanceof TimeValue)) {
					return false;
				}
				TimeValue tv = (TimeValue) date;
				return (bitField & (1L << tv.minute())) != 0;
			}
		};
	}

	/**
	 * Constructs a second filter based on a BYMINUTE rule.
	 * @param seconds seconds of the minute (values must be in rage [0,59])
	 * @return the filter
	 */
	static Predicate<DateValue> bySecondFilter(int[] seconds) {
		long secondsByBit = 0;
		for (int second : seconds) {
			secondsByBit |= 1L << second;
		}
		if ((secondsByBit & LOW_60_BITS) == LOW_60_BITS) {
			return Predicates.alwaysTrue();
		}
		final long bitField = secondsByBit;
		return new Predicate<DateValue>() {
			private static final long serialVersionUID = 4109739845053177924L;

			public boolean apply(DateValue date) {
				if (!(date instanceof TimeValue)) {
					return false;
				}
				TimeValue tv = (TimeValue) date;
				return (bitField & (1L << tv.second())) != 0;
			}
		};
	}

	private Filters() {
		//uninstantiable
	}
}
