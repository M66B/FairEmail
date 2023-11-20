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

import java.util.Arrays;

import biweekly.util.ByDay;
import biweekly.util.DayOfWeek;
import biweekly.util.com.google.ical.util.DTBuilder;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;
import biweekly.util.com.google.ical.values.TimeValue;

/**
 * Factory for field generators.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
final class Generators {
	/**
	 * <p>
	 * The maximum number of years generated between instances. See
	 * {@link ThrottledGenerator} for a description of the problem this solves.
	 * </p>
	 * <p>
	 * Note: This counts the maximum number of years generated, so for
	 * <code>FREQ=YEARLY;INTERVAL=4</code> the generator would try 100
	 * individual years over a span of 400 years before giving up and concluding
	 * that the rule generates no usable dates.
	 * </p>
	 */
	private static final int MAX_YEARS_BETWEEN_INSTANCES = 100;

	/**
	 * Constructs a generator that generates years successively counting from
	 * the first year passed in.
	 * @param interval number of years to advance each step
	 * @param dtStart the start date
	 * @return the year in dtStart the first time called and interval + last
	 * return value on subsequent calls
	 */
	static ThrottledGenerator serialYearGenerator(final int interval, final DateValue dtStart) {
		return new ThrottledGenerator() {
			//the last year seen
			int year = dtStart.year() - interval;

			int throttle = MAX_YEARS_BETWEEN_INSTANCES;

			@Override
			boolean generate(DTBuilder builder) throws IteratorShortCircuitingException {
				/*
				 * Make sure things halt even if the RRULE is bad. For example,
				 * the following rules should halt:
				 * 
				 * FREQ=YEARLY;BYMONTHDAY=30;BYMONTH=2
				 */
				if (--throttle < 0) {
					throw IteratorShortCircuitingException.instance();
				}
				year += interval;
				builder.year = year;
				return true;
			}

			@Override
			void workDone() {
				this.throttle = MAX_YEARS_BETWEEN_INSTANCES;
			}

			@Override
			public String toString() {
				return "serialYearGenerator:" + interval;
			}
		};
	}

	/**
	 * Constructs a generator that generates months in the given builder's year
	 * successively counting from the first month passed in.
	 * @param interval number of months to advance each step
	 * @param dtStart the start date
	 * @return the month in dtStart the first time called and interval + last
	 * return value on subsequent calls
	 */
	static Generator serialMonthGenerator(final int interval, final DateValue dtStart) {
		return new Generator() {
			int year = dtStart.year();
			int month = dtStart.month() - interval;
			{
				while (month < 1) {
					month += 12;
					--year;
				}
			}

			@Override
			boolean generate(DTBuilder builder) {
				int nmonth;
				if (year != builder.year) {
					int monthsBetween = (builder.year - year) * 12 - (month - 1);
					nmonth = ((interval - (monthsBetween % interval)) % interval) + 1;
					if (nmonth > 12) {
						/*
						 * Don't update year so that the difference calculation
						 * above is correct when this function is reentered with
						 * a different year
						 */
						return false;
					}
					year = builder.year;
				} else {
					nmonth = month + interval;
					if (nmonth > 12) {
						return false;
					}
				}
				month = builder.month = nmonth;
				return true;
			}

			@Override
			public String toString() {
				return "serialMonthGenerator:" + interval;
			}
		};
	}

	/**
	 * Constructs a generator that generates every day in the current month that
	 * is an integer multiple of interval days from dtStart.
	 * @param interval number of days to advance each step
	 * @param dtStart the start date
	 * @return the day in dtStart the first time called and interval + last
	 * return value on subsequent calls
	 */
	static Generator serialDayGenerator(final int interval, final DateValue dtStart) {
		return new Generator() {
			int year, month, date;
			/** ndays in the last month encountered */
			int nDays;

			{
				//step back one interval
				DateValue dtStartMinus1;
				{
					DTBuilder builder = new DTBuilder(dtStart);
					builder.day -= interval;
					dtStartMinus1 = builder.toDate();
				}
				year = dtStartMinus1.year();
				month = dtStartMinus1.month();
				date = dtStartMinus1.day();
				nDays = TimeUtils.monthLength(year, month);
			}

			@Override
			boolean generate(DTBuilder builder) {
				int ndate;
				if (year == builder.year && month == builder.month) {
					ndate = date + interval;
					if (ndate > nDays) {
						return false;
					}
				} else {
					nDays = TimeUtils.monthLength(builder.year, builder.month);
					if (interval != 1) {
						/*
						 * Calculate the number of days between the first of the
						 * new month and the old date and extend it to make it
						 * an integer multiple of interval.
						 */
						int daysBetween = TimeUtils.daysBetween(new DateValueImpl(builder.year, builder.month, 1), new DateValueImpl(year, month, date));
						ndate = ((interval - (daysBetween % interval)) % interval) + 1;
						if (ndate > nDays) {
							/*
							 * Need to return early without updating year or
							 * month so that the next time we enter with a
							 * different month, the daysBetween call above
							 * compares against the proper last date.
							 */
							return false;
						}
					} else {
						ndate = 1;
					}
					year = builder.year;
					month = builder.month;
				}
				date = builder.day = ndate;
				return true;
			}

			@Override
			public String toString() {
				return "serialDayGenerator:" + interval;
			}
		};
	}

	/**
	 * Constructs a generator that generates hours in the given date's day
	 * successively counting from the first hour passed in.
	 * @param interval number of hours to advance each step
	 * @param dtStart the start date
	 * @return the hour in dtStart the first time called and interval + last
	 * return value on subsequent calls
	 */
	static Generator serialHourGenerator(final int interval, final DateValue dtStart) {
		final TimeValue dtStartTime = TimeUtils.timeOf(dtStart);
		return new Generator() {
			int hour = dtStartTime.hour() - interval;
			int day = dtStart.day();
			int month = dtStart.month();
			int year = dtStart.year();

			@Override
			boolean generate(DTBuilder builder) {
				int nhour;
				if (day != builder.day || month != builder.month || year != builder.year) {
					int hoursBetween = daysBetween(builder, year, month, day) * 24 - hour;
					nhour = ((interval - (hoursBetween % interval)) % interval);
					if (nhour > 23) {
						/*
						 * Don't update day so that the difference calculation
						 * above is correct when this function is reentered with
						 * a different day.
						 */
						return false;
					}
					day = builder.day;
					month = builder.month;
					year = builder.year;
				} else {
					nhour = hour + interval;
					if (nhour > 23) {
						return false;
					}
				}
				hour = builder.hour = nhour;
				return true;
			}

			@Override
			public String toString() {
				return "serialHourGenerator:" + interval;
			}
		};
	}

	/**
	 * Constructs a generator that generates minutes in the given date's hour
	 * successively counting from the first minute passed in.
	 * @param interval number of minutes to advance each step
	 * @param dtStart the date
	 * @return the minute in dtStart the first time called and interval + last
	 * return value on subsequent calls
	 */
	static Generator serialMinuteGenerator(final int interval, final DateValue dtStart) {
		final TimeValue dtStartTime = TimeUtils.timeOf(dtStart);
		return new Generator() {
			int minute = dtStartTime.minute() - interval;
			int hour = dtStartTime.hour();
			int day = dtStart.day();
			int month = dtStart.month();
			int year = dtStart.year();

			@Override
			boolean generate(DTBuilder builder) {
				int nminute;
				if (hour != builder.hour || day != builder.day || month != builder.month || year != builder.year) {
					int minutesBetween = (daysBetween(builder, year, month, day) * 24 + builder.hour - hour) * 60 - minute;
					nminute = ((interval - (minutesBetween % interval)) % interval);
					if (nminute > 59) {
						/*
						 * Don't update day so that the difference calculation
						 * above is correct when this function is reentered with
						 * a different day.
						 */
						return false;
					}
					hour = builder.hour;
					day = builder.day;
					month = builder.month;
					year = builder.year;
				} else {
					nminute = minute + interval;
					if (nminute > 59) {
						return false;
					}
				}
				minute = builder.minute = nminute;
				return true;
			}

			@Override
			public String toString() {
				return "serialMinuteGenerator:" + interval;
			}
		};
	}

	/**
	 * Constructs a generator that generates seconds in the given date's minute
	 * successively counting from the first second passed in.
	 * @param interval number of seconds to advance each step
	 * @param dtStart the date
	 * @return the second in dtStart the first time called and interval + last
	 * return value on subsequent calls
	 */
	static Generator serialSecondGenerator(final int interval, final DateValue dtStart) {
		final TimeValue dtStartTime = TimeUtils.timeOf(dtStart);
		return new Generator() {
			int second = dtStartTime.second() - interval;
			int minute = dtStartTime.minute();
			int hour = dtStartTime.hour();
			int day = dtStart.day();
			int month = dtStart.month();
			int year = dtStart.year();

			@Override
			boolean generate(DTBuilder builder) {
				int nsecond;
				if (minute != builder.minute || hour != builder.hour || day != builder.day || month != builder.month || year != builder.year) {
					int secondsBetween = ((daysBetween(builder, year, month, day) * 24 + builder.hour - hour) * 60 + builder.minute - minute) * 60 - second;
					nsecond = ((interval - (secondsBetween % interval)) % interval);
					if (nsecond > 59) {
						/*
						 * Don't update day so that the difference calculation
						 * above is correct when this function is reentered with
						 * a different day.
						 */
						return false;
					}
					minute = builder.minute;
					hour = builder.hour;
					day = builder.day;
					month = builder.month;
					year = builder.year;
				} else {
					nsecond = second + interval;
					if (nsecond > 59) {
						return false;
					}
				}
				second = builder.second = nsecond;
				return true;
			}

			@Override
			public String toString() {
				return "serialSecondGenerator:" + interval;
			}
		};
	}

	/**
	 * Constructs a generator that yields the specified years in increasing
	 * order.
	 * @param years the years
	 * @param dtStart the start date
	 * @return the generator
	 */
	static Generator byYearGenerator(int[] years, final DateValue dtStart) {
		final int[] uyears = Util.uniquify(years);

		// index into years
		return new Generator() {
			int i;
			{
				while (i < uyears.length && dtStart.year() > uyears[i]) {
					++i;
				}
			}

			@Override
			boolean generate(DTBuilder builder) {
				if (i >= uyears.length) {
					return false;
				}
				builder.year = uyears[i++];
				return true;
			}

			@Override
			public String toString() {
				return "byYearGenerator";
			}
		};
	}

	/**
	 * Constructs a generator that yields the specified months in increasing
	 * order for each year.
	 * @param months the month values (each value must be in range [1,12])
	 * @param dtStart the start date
	 * @return the generator
	 */
	static Generator byMonthGenerator(int[] months, final DateValue dtStart) {
		final int[] umonths = Util.uniquify(months);

		return new Generator() {
			int i;
			int year = dtStart.year();

			@Override
			boolean generate(DTBuilder builder) {
				if (year != builder.year) {
					i = 0;
					year = builder.year;
				}
				if (i >= umonths.length) {
					return false;
				}
				builder.month = umonths[i++];
				return true;
			}

			@Override
			public String toString() {
				return "byMonthGenerator:" + Arrays.toString(umonths);
			}
		};
	}

	/**
	 * Constructs a generator that yields the specified hours in increasing
	 * order for each day.
	 * @param hours the hour values (each value must be in range [0,23])
	 * @param dtStart the start date
	 * @return the generator
	 */
	static Generator byHourGenerator(int[] hours, final DateValue dtStart) {
		final TimeValue dtStartTime = TimeUtils.timeOf(dtStart);
		final int[] uhours = (hours.length == 0) ? new int[] { dtStartTime.hour() } : Util.uniquify(hours);

		if (uhours.length == 1) {
			final int hour = uhours[0];

			return new SingleValueGenerator() {
				int year;
				int month;
				int day;

				@Override
				boolean generate(DTBuilder builder) {
					if (year != builder.year || month != builder.month || day != builder.day) {
						year = builder.year;
						month = builder.month;
						day = builder.day;
						builder.hour = hour;
						return true;
					}
					return false;
				}

				@Override
				int getValue() {
					return hour;
				}

				@Override
				public String toString() {
					return "byHourGenerator:" + hour;
				}
			};
		}

		return new Generator() {
			int i;
			int year = dtStart.year();
			int month = dtStart.month();
			int day = dtStart.day();
			{
				int hour = dtStartTime.hour();
				while (i < uhours.length && uhours[i] < hour) {
					++i;
				}
			}

			@Override
			boolean generate(DTBuilder builder) {
				if (year != builder.year || month != builder.month || day != builder.day) {
					i = 0;
					year = builder.year;
					month = builder.month;
					day = builder.day;
				}
				if (i >= uhours.length) {
					return false;
				}
				builder.hour = uhours[i++];
				return true;
			}

			@Override
			public String toString() {
				return "byHourGenerator:" + Arrays.toString(uhours);
			}
		};
	}

	/**
	 * Constructs a generator that yields the specified minutes in increasing
	 * order for each hour.
	 * @param minutes the minute values (each value must be in range [0,59])
	 * @param dtStart the start date
	 * @return the generator
	 */
	static Generator byMinuteGenerator(int[] minutes, final DateValue dtStart) {
		final TimeValue dtStartTime = TimeUtils.timeOf(dtStart);
		final int[] uminutes = (minutes.length == 0) ? new int[] { dtStartTime.minute() } : Util.uniquify(minutes);

		if (uminutes.length == 1) {
			final int minute = uminutes[0];

			return new SingleValueGenerator() {
				int year;
				int month;
				int day;
				int hour;

				@Override
				boolean generate(DTBuilder builder) {
					if (year != builder.year || month != builder.month || day != builder.day || hour != builder.hour) {
						year = builder.year;
						month = builder.month;
						day = builder.day;
						hour = builder.hour;
						builder.minute = minute;
						return true;
					}
					return false;
				}

				@Override
				int getValue() {
					return minute;
				}

				@Override
				public String toString() {
					return "byMinuteGenerator:" + minute;
				}
			};
		}

		return new Generator() {
			int i;
			int year = dtStart.year();
			int month = dtStart.month();
			int day = dtStart.day();
			int hour = dtStartTime.hour();
			{
				int minute = dtStartTime.minute();
				while (i < uminutes.length && uminutes[i] < minute) {
					++i;
				}
			}

			@Override
			boolean generate(DTBuilder builder) {
				if (year != builder.year || month != builder.month || day != builder.day || hour != builder.hour) {
					i = 0;
					year = builder.year;
					month = builder.month;
					day = builder.day;
					hour = builder.hour;
				}
				if (i >= uminutes.length) {
					return false;
				}
				builder.minute = uminutes[i++];
				return true;
			}

			@Override
			public String toString() {
				return "byMinuteGenerator:" + Arrays.toString(uminutes);
			}
		};
	}

	/**
	 * Constructs a generator that yields the specified seconds in increasing
	 * order for each minute.
	 * @param seconds the second values (each value must be in range [0,59])
	 * @param dtStart the start date
	 * @return the generator
	 */
	static Generator bySecondGenerator(int[] seconds, final DateValue dtStart) {
		final TimeValue dtStartTime = TimeUtils.timeOf(dtStart);
		final int[] useconds = (seconds.length == 0) ? new int[] { dtStartTime.second() } : Util.uniquify(seconds);

		if (useconds.length == 1) {
			final int second = useconds[0];

			return new SingleValueGenerator() {
				int year;
				int month;
				int day;
				int hour;
				int minute;

				@Override
				boolean generate(DTBuilder builder) {
					if (year != builder.year || month != builder.month || day != builder.day || hour != builder.hour || minute != builder.minute) {
						year = builder.year;
						month = builder.month;
						day = builder.day;
						hour = builder.hour;
						minute = builder.minute;
						builder.second = second;
						return true;
					}
					return false;
				}

				@Override
				int getValue() {
					return second;
				}

				@Override
				public String toString() {
					return "bySecondGenerator:" + second;
				}
			};
		}

		return new Generator() {
			int i;
			int year = dtStart.year();
			int month = dtStart.month();
			int day = dtStart.day();
			int hour = dtStartTime.hour();
			int minute = dtStartTime.minute();
			{
				int second = dtStartTime.second();
				while (i < useconds.length && useconds[i] < second) {
					++i;
				}
			}

			@Override
			boolean generate(DTBuilder builder) {
				if (year != builder.year || month != builder.month || day != builder.day || hour != builder.hour || minute != builder.minute) {
					i = 0;
					year = builder.year;
					month = builder.month;
					day = builder.day;
					hour = builder.hour;
					minute = builder.minute;
				}
				if (i >= useconds.length) {
					return false;
				}
				builder.second = useconds[i++];
				return true;
			}

			@Override
			public String toString() {
				return "bySecondGenerator:" + Arrays.toString(useconds);
			}
		};
	}

	/**
	 * Constructs a generator that yields the specified dates (possibly relative
	 * to end of month) in increasing order for each month seen.
	 * @param dates the date values (each value must be range [-31,31])
	 * @param dtStart the start date
	 * @return the generator
	 */
	static Generator byMonthDayGenerator(int[] dates, final DateValue dtStart) {
		final int[] udates = Util.uniquify(dates);

		return new Generator() {
			int year = dtStart.year();
			int month = dtStart.month();
			/** list of generated dates for the current month */
			int[] posDates;
			/** index of next date to return */
			int i = 0;

			{
				convertDatesToAbsolute();
			}

			private void convertDatesToAbsolute() {
				IntSet posDates = new IntSet();
				int nDays = TimeUtils.monthLength(year, month);
				for (int j = 0; j < udates.length; ++j) {
					int date = udates[j];
					if (date < 0) {
						date += nDays + 1;
					}
					if (date >= 1 && date <= nDays) {
						posDates.add(date);
					}
				}
				this.posDates = posDates.toIntArray();
			}

			@Override
			boolean generate(DTBuilder builder) {
				if (year != builder.year || month != builder.month) {
					year = builder.year;
					month = builder.month;

					convertDatesToAbsolute();

					i = 0;
				}
				if (i >= posDates.length) {
					return false;
				}
				builder.day = posDates[i++];
				return true;
			}

			@Override
			public String toString() {
				return "byMonthDayGenerator";
			}
		};
	}

	/**
	 * Constructs a day generator based on a BYDAY rule.
	 * @param days list of week/number pairs (e.g. SU,3MO means every Sunday and
	 * the 3rd Monday)
	 * @param weeksInYear true if the week numbers are meant to be weeks in the
	 * current year, false if they are meant to be weeks in the current month
	 * @param dtStart the start date
	 * @return the generator
	 */
	static Generator byDayGenerator(ByDay[] days, final boolean weeksInYear, final DateValue dtStart) {
		final ByDay[] udays = days.clone();

		return new Generator() {
			int year = dtStart.year();
			int month = dtStart.month();
			/** list of generated dates for the current month */
			int[] dates;
			/** index of next date to return */
			int i = 0;

			{
				generateDates();
				int day = dtStart.day();
				while (i < dates.length && dates[i] < day) {
					++i;
				}
			}

			void generateDates() {
				int nDays;
				DayOfWeek dow0;
				int nDaysInMonth = TimeUtils.monthLength(year, month);
				//index of the first day of the month in the month or year
				int d0;

				if (weeksInYear) {
					nDays = TimeUtils.yearLength(year);
					dow0 = TimeUtils.firstDayOfWeekInMonth(year, 1);
					d0 = TimeUtils.dayOfYear(year, month, 1);
				} else {
					nDays = nDaysInMonth;
					dow0 = TimeUtils.firstDayOfWeekInMonth(year, month);
					d0 = 0;
				}

				/*
				 * An index not greater than the first week of the month in the
				 * month or year.
				 */
				int w0 = d0 / 7;

				/*
				 * Iterate through days and resolve each [week, day of week]
				 * pair to a day of the month.
				 */
				IntSet udates = new IntSet();
				for (ByDay day : udays) {
					if (day.getNum() != null && day.getNum() != 0) {
						int date = Util.dayNumToDate(dow0, nDays, day.getNum(), day.getDay(), d0, nDaysInMonth);
						if (date != 0) {
							udates.add(date);
						}
					} else {
						int wn = w0 + 6;
						for (int w = w0; w <= wn; ++w) {
							int date = Util.dayNumToDate(dow0, nDays, w, day.getDay(), d0, nDaysInMonth);
							if (date != 0) {
								udates.add(date);
							}
						}
					}
				}
				dates = udates.toIntArray();
			}

			@Override
			boolean generate(DTBuilder builder) {
				if (year != builder.year || month != builder.month) {
					year = builder.year;
					month = builder.month;

					generateDates();
					//start at the beginning of the month
					i = 0;
				}
				if (i >= dates.length) {
					return false;
				}
				builder.day = dates[i++];
				return true;
			}

			@Override
			public String toString() {
				return "byDayGenerator:" + Arrays.toString(udays) + " by " + (weeksInYear ? "year" : "week");
			}
		};
	}

	/**
	 * Constructs a generator that yields each day in the current month that
	 * falls in one of the given weeks of the year.
	 * @param weekNumbers the week numbers (each value must be in range
	 * [-53,53])
	 * @param weekStart the day of the week that the week starts on
	 * @param dtStart the start date
	 * @return the generator
	 */
	static Generator byWeekNoGenerator(int[] weekNumbers, final DayOfWeek weekStart, final DateValue dtStart) {
		final int[] uWeekNumbers = Util.uniquify(weekNumbers);

		return new Generator() {
			int year = dtStart.year();
			int month = dtStart.month();
			/** number of weeks in the last year seen */
			int weeksInYear;
			/** dates generated anew for each month seen */
			int[] dates;
			/** index into dates */
			int i = 0;

			/**
			 * day of the year of the start of week 1 of the current year. Since
			 * week 1 may start on the previous year, this may be negative.
			 */
			int doyOfStartOfWeek1;

			{
				checkYear();
				checkMonth();
			}

			void checkYear() {
				//if the first day of January is weekStart, then there are 7
				//if the first day of January is weekStart + 1, then there are 6
				//if the first day of January is weekStart + 6, then there is 1
				DayOfWeek dowJan1 = TimeUtils.firstDayOfWeekInMonth(year, 1);
				int nDaysInFirstWeek = 7 - ((7 + dowJan1.getCalendarConstant() - weekStart.getCalendarConstant()) % 7);

				//number of days not in any week
				int nOrphanedDays = 0;

				/*
				 * According to RFC 2445:
				 * 
				 * Week number one of the calendar year is the first week which
				 * contains at least four (4) days in that calendar year.
				 */
				if (nDaysInFirstWeek < 4) {
					nOrphanedDays = nDaysInFirstWeek;
					nDaysInFirstWeek = 7;
				}

				/*
				 * Calculate the day of year (possibly negative) of the start of
				 * the first week in the year. This day must be of weekStart.
				 */
				doyOfStartOfWeek1 = nDaysInFirstWeek - 7 + nOrphanedDays;

				weeksInYear = (TimeUtils.yearLength(year) - nOrphanedDays + 6) / 7;
			}

			void checkMonth() {
				//the day of the year of the 1st day in the month
				int doyOfMonth1 = TimeUtils.dayOfYear(year, month, 1);

				//the week of the year of the 1st day of the month.  approximate.
				int weekOfMonth = ((doyOfMonth1 - doyOfStartOfWeek1) / 7) + 1;

				//the number of days in the month
				int nDays = TimeUtils.monthLength(year, month);

				//generate the dates in the month
				IntSet udates = new IntSet();
				for (int weekNo : uWeekNumbers) {
					if (weekNo < 0) {
						weekNo += weeksInYear + 1;
					}
					if (weekNo >= weekOfMonth - 1 && weekNo <= weekOfMonth + 6) {
						for (int d = 0; d < 7; ++d) {
							int date = ((weekNo - 1) * 7 + d + doyOfStartOfWeek1 - doyOfMonth1) + 1;
							if (date >= 1 && date <= nDays) {
								udates.add(date);
							}
						}
					}
				}
				dates = udates.toIntArray();
			}

			@Override
			boolean generate(DTBuilder builder) {
				/*
				 * This is a bit odd, since we're generating days within the
				 * given weeks of the year within the month/year from builder.
				 */
				if (year != builder.year || month != builder.month) {
					if (year != builder.year) {
						year = builder.year;
						checkYear();
					}
					month = builder.month;
					checkMonth();

					i = 0;
				}

				if (i >= dates.length) {
					return false;
				}
				builder.day = dates[i++];
				return true;
			}

			@Override
			public String toString() {
				return "byWeekNoGenerator";
			}
		};
	}

	/**
	 * Constructs a day generator that generates dates in the current month that
	 * fall on one of the given days of the year.
	 * @param yearDays the days of the year (values must be in range [-366,366])
	 * @param dtStart the start date
	 * @return the generator
	 */
	static Generator byYearDayGenerator(int[] yearDays, final DateValue dtStart) {
		final int[] uYearDays = Util.uniquify(yearDays);

		return new Generator() {
			int year = dtStart.year();
			int month = dtStart.month();
			int[] dates;
			int i = 0;

			{
				checkMonth();
			}

			void checkMonth() {
				//now, calculate the first week of the month
				int doyOfMonth1 = TimeUtils.dayOfYear(year, month, 1);
				int nDays = TimeUtils.monthLength(year, month);
				int nYearDays = TimeUtils.yearLength(year);
				IntSet udates = new IntSet();
				for (int yearDay : uYearDays) {
					if (yearDay < 0) {
						yearDay += nYearDays + 1;
					}
					int date = yearDay - doyOfMonth1;
					if (date >= 1 && date <= nDays) {
						udates.add(date);
					}
				}
				dates = udates.toIntArray();
			}

			@Override
			boolean generate(DTBuilder builder) {
				if (year != builder.year || month != builder.month) {
					year = builder.year;
					month = builder.month;

					checkMonth();

					i = 0;
				}
				if (i >= dates.length) {
					return false;
				}
				builder.day = dates[i++];
				return true;
			}

			@Override
			public String toString() {
				return "byYearDayGenerator";
			}
		};
	}

	private static int daysBetween(DTBuilder builder, int year, int month, int day) {
		if (year == builder.year && month == builder.month) {
			return builder.day - day;
		} else {
			return TimeUtils.daysBetween(builder.year, builder.month, builder.day, year, month, day);
		}
	}

	private Generators() {
		//uninstantiable
	}
}
