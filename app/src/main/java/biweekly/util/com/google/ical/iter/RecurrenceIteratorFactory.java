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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import biweekly.util.ByDay;
import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.Google2445Utils;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.util.Predicate;
import biweekly.util.com.google.ical.util.Predicates;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateTimeValue;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;
import biweekly.util.com.google.ical.values.TimeValue;

/**
 * <p>
 * Calculates the occurrences of an individual RRULE definition or groups of
 * RRULEs, RDATEs, EXRULEs, and EXDATEs.
 * </p>
 * <p>
 * <b>Glossary</b>
 * </p>
 * <ul>
 * <li>Period - year|month|day|...</li>
 * <li>Day of the week - an int in [0,6]</li>
 * <li>Day of the year - zero indexed in [0,365]</li>
 * <li>Day of the month - 1 indexed in [1,31]</li>
 * <li>Month - 1 indexed integer in [1,12]</li>
 * </ul>
 * <p>
 * <b>Abstractions</b>
 * </p>
 * <ul>
 * <li>Generator - a function corresponding to an RRULE part that takes a date
 * and returns a later (year or month or day depending on its period) within the
 * next larger period. A generator ignores all periods in its input smaller than
 * its period.</li>
 * <li>Filter - a function that returns true iff the given date matches the
 * subrule.</li>
 * <li>Condition - returns true if the given date is past the end of the
 * recurrence.</li>
 * </ul>
 * <p>
 * All the functions that represent rule parts are stateful.
 * </p>
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class RecurrenceIteratorFactory {
	/**
	 * Creates a recurrence iterator from an RDATE or EXDATE list.
	 * @param dates the list of dates
	 * @return the iterator
	 */
	public static RecurrenceIterator createRecurrenceIterator(Collection<? extends DateValue> dates) {
		DateValue[] datesArray = dates.toArray(new DateValue[0]);
		return new RDateIteratorImpl(datesArray);
	}

	/**
	 * Creates a recurrence iterable from an RRULE.
	 * @param rrule the recurrence rule
	 * @param dtStart the start date of the series
	 * @param tzid the timezone that the start date is in, as well as the
	 * timezone to iterate in
	 * @return the iterable
	 */
	public static RecurrenceIterable createRecurrenceIterable(final Recurrence rrule, final DateValue dtStart, final TimeZone tzid) {
		return new RecurrenceIterable() {
			public RecurrenceIterator iterator() {
				return createRecurrenceIterator(rrule, dtStart, tzid);
			}
		};
	}

	/**
	 * Creates a recurrence iterator from an RRULE.
	 * @param rrule the recurrence rule
	 * @param dtStart the start date of the series
	 * @param tzid the timezone that the start date is in, as well as the
	 * timezone to iterate in
	 * @return the iterator
	 */
	public static RecurrenceIterator createRecurrenceIterator(Recurrence rrule, DateValue dtStart, TimeZone tzid) {
		Frequency freq = rrule.getFrequency();

		/*
		 * If the given RRULE is malformed and does not have a frequency
		 * specified, default to "yearly".
		 */
		if (freq == null) {
			freq = Frequency.YEARLY;
		}

		DayOfWeek wkst = rrule.getWorkweekStarts();

		ICalDate until = rrule.getUntil();
		int count = toInt(rrule.getCount());
		int interval = toInt(rrule.getInterval());
		ByDay[] byDay = rrule.getByDay().toArray(new ByDay[0]);
		int[] byMonth = toIntArray(rrule.getByMonth());
		int[] byMonthDay = toIntArray(rrule.getByMonthDay());
		int[] byWeekNo = toIntArray(rrule.getByWeekNo());
		int[] byYearDay = toIntArray(rrule.getByYearDay());
		int[] bySetPos = toIntArray(rrule.getBySetPos());
		int[] byHour = toIntArray(rrule.getByHour());
		int[] byMinute = toIntArray(rrule.getByMinute());
		int[] bySecond = toIntArray(rrule.getBySecond());
		boolean canShortcutAdvance = true;

		if (interval <= 0) {
			interval = 1;
		}

		if (wkst == null) {
			wkst = DayOfWeek.MONDAY;
		}

		//optimize out BYSETPOS where possible
		if (bySetPos.length > 0) {
			switch (freq) {
			case HOURLY:
				if (byHour.length > 0 && byMinute.length <= 1 && bySecond.length <= 1) {
					byHour = filterBySetPos(byHour, bySetPos);
				}

				/*
				 * Handling bySetPos for rules that are more frequent than daily
				 * tends to lead to large amounts of processor being used before
				 * other work limiting features can kick in since there many
				 * seconds between dtStart and where the year limit kicks in.
				 * There are no known use cases for the use of bySetPos with
				 * hourly minutely and secondly rules so we just ignore it.
				 */
				bySetPos = NO_INTS;
				break;
			case MINUTELY:
				if (byMinute.length > 0 && bySecond.length <= 1) {
					byMinute = filterBySetPos(byMinute, bySetPos);
				}
				//see bySetPos handling comment above
				bySetPos = NO_INTS;
				break;
			case SECONDLY:
				if (bySecond.length > 0) {
					bySecond = filterBySetPos(bySecond, bySetPos);
				}
				//see bySetPos handling comment above
				bySetPos = NO_INTS;
				break;
			default:
			}

			canShortcutAdvance = false;
		}

		DateValue start = dtStart;
		if (bySetPos.length > 0) {
			/*
			 * Roll back until the beginning of the period to make sure that any
			 * positive indices are indexed properly. The actual iterator
			 * implementation is responsible for anything < dtStart.
			 */
			switch (freq) {
			case YEARLY:
				if (dtStart instanceof TimeValue) {
					TimeValue tv = (TimeValue) dtStart;
					start = new DateTimeValueImpl(start.year(), 1, 1, tv.hour(), tv.minute(), tv.second());
				} else {
					start = new DateValueImpl(start.year(), 1, 1);
				}
				break;
			case MONTHLY:
				if (dtStart instanceof TimeValue) {
					TimeValue tv = (TimeValue) dtStart;
					start = new DateTimeValueImpl(start.year(), start.month(), 1, tv.hour(), tv.minute(), tv.second());
				} else {
					start = new DateValueImpl(start.year(), start.month(), 1);
				}
				break;
			case WEEKLY:
				int d = (7 + wkst.ordinal() - TimeUtils.dayOfWeek(dtStart).getCalendarConstant()) % 7;
				start = TimeUtils.add(dtStart, new DateValueImpl(0, 0, -d));
				break;
			default:
				break;
			}
		}

		/*
		 * Recurrences are implemented as a sequence of periodic generators.
		 * First a year is generated, and then months, and within months, days.
		 */
		ThrottledGenerator yearGenerator = Generators.serialYearGenerator(freq == Frequency.YEARLY ? interval : 1, dtStart);
		Generator monthGenerator = null;
		Generator dayGenerator = null;
		Generator secondGenerator = null;
		Generator minuteGenerator = null;
		Generator hourGenerator = null;

		/*
		 * When multiple generators are specified for a period, they act as a
		 * union operator. We could have multiple generators (say, for day) and
		 * then run each and merge the results, but some generators are more
		 * efficient than others. So to avoid generating 53 Sundays and throwing
		 * away all but 1 for RRULE:FREQ=YEARLY;BYDAY=TU;BYWEEKNO=1, we
		 * reimplement some of the more prolific generators as filters.
		 */
		// TODO(msamuel): don't need a list here
		List<Predicate<? super DateValue>> filters = new ArrayList<Predicate<? super DateValue>>();

		switch (freq) {
		case SECONDLY:
			if (bySecond.length == 0 || interval != 1) {
				secondGenerator = Generators.serialSecondGenerator(interval, dtStart);
				if (bySecond.length > 0) {
					filters.add(Filters.bySecondFilter(bySecond));
				}
			}
			break;
		case MINUTELY:
			if (byMinute.length == 0 || interval != 1) {
				minuteGenerator = Generators.serialMinuteGenerator(interval, dtStart);
				if (byMinute.length > 0) {
					filters.add(Filters.byMinuteFilter(byMinute));
				}
			}
			break;
		case HOURLY:
			if (byHour.length == 0 || interval != 1) {
				hourGenerator = Generators.serialHourGenerator(interval, dtStart);
				if (byHour.length > 0) {
					filters.add(Filters.byHourFilter(bySecond));
				}
			}
			break;
		case DAILY:
			break;
		case WEEKLY:
			/*
			 * Week is not considered a period because a week may span multiple
			 * months and/or years. There are no week generators, so a filter is
			 * used to make sure that FREQ=WEEKLY;INTERVAL=2 only generates
			 * dates within the proper week.
			 */
			if (byDay.length > 0) {
				dayGenerator = Generators.byDayGenerator(byDay, false, start);
				byDay = NO_DAYS;
				if (interval > 1) {
					filters.add(Filters.weekIntervalFilter(interval, wkst, dtStart));
				}
			} else {
				dayGenerator = Generators.serialDayGenerator(interval * 7, dtStart);
			}
			break;
		case YEARLY:
			if (byYearDay.length > 0) {
				/*
				 * The BYYEARDAY rule part specifies a COMMA separated list of
				 * days of the year. Valid values are 1 to 366 or -366 to -1.
				 * For example, -1 represents the last day of the year (December
				 * 31st) and -306 represents the 306th to the last day of the
				 * year (March 1st).
				 */
				dayGenerator = Generators.byYearDayGenerator(byYearDay, start);
				break;
			}
			// $FALL-THROUGH$
		case MONTHLY:
			if (byMonthDay.length > 0) {
				/*
				 * The BYMONTHDAY rule part specifies a COMMA separated list of
				 * days of the month. Valid values are 1 to 31 or -31 to -1. For
				 * example, -10 represents the tenth to the last day of the
				 * month.
				 */
				dayGenerator = Generators.byMonthDayGenerator(byMonthDay, start);
				byMonthDay = NO_INTS;
			} else if (byWeekNo.length > 0 && Frequency.YEARLY == freq) {
				/*
				 * The BYWEEKNO rule part specifies a COMMA separated list of
				 * ordinals specifying weeks of the year. This rule part is only
				 * valid for YEARLY rules.
				 */
				dayGenerator = Generators.byWeekNoGenerator(byWeekNo, wkst, start);
				byWeekNo = NO_INTS;
			} else if (byDay.length > 0) {
				/*
				 * Each BYDAY value can also be preceded by a positive (n) or
				 * negative (-n) integer. If present, this indicates the nth
				 * occurrence of the specific day within the MONTHLY or YEARLY
				 * RRULE. For example, within a MONTHLY rule, +1MO (or simply
				 * 1MO) represents the first Monday within the month, whereas
				 * -1MO represents the last Monday of the month. If an integer
				 * modifier is not present, it means all days of this type
				 * within the specified frequency. For example, within a MONTHLY
				 * rule, MO represents all Mondays within the month.
				 */
				dayGenerator = Generators.byDayGenerator(byDay, Frequency.YEARLY == freq && byMonth.length == 0, start);
				byDay = NO_DAYS;
			} else {
				if (Frequency.YEARLY == freq) {
					monthGenerator = Generators.byMonthGenerator(new int[] { dtStart.month() }, start);
				}
				dayGenerator = Generators.byMonthDayGenerator(new int[] { dtStart.day() }, start);
			}
			break;
		}

		if (secondGenerator == null) {
			secondGenerator = Generators.bySecondGenerator(bySecond, start);
		}
		if (minuteGenerator == null) {
			if (byMinute.length == 0 && freq.compareTo(Frequency.MINUTELY) < 0) {
				minuteGenerator = Generators.serialMinuteGenerator(1, dtStart);
			} else {
				minuteGenerator = Generators.byMinuteGenerator(byMinute, start);
			}
		}
		if (hourGenerator == null) {
			if (byHour.length == 0 && freq.compareTo(Frequency.HOURLY) < 0) {
				hourGenerator = Generators.serialHourGenerator(1, dtStart);
			} else {
				hourGenerator = Generators.byHourGenerator(byHour, start);
			}
		}

		if (dayGenerator == null) {
			boolean dailyOrMoreOften = freq.compareTo(Frequency.DAILY) <= 0;
			if (byMonthDay.length > 0) {
				dayGenerator = Generators.byMonthDayGenerator(byMonthDay, start);
				byMonthDay = NO_INTS;
			} else if (byDay.length > 0) {
				dayGenerator = Generators.byDayGenerator(byDay, Frequency.YEARLY == freq, start);
				byDay = NO_DAYS;
			} else if (dailyOrMoreOften) {
				dayGenerator = Generators.serialDayGenerator(Frequency.DAILY == freq ? interval : 1, dtStart);
			} else {
				dayGenerator = Generators.byMonthDayGenerator(new int[] { dtStart.day() }, start);
			}
		}

		if (byDay.length > 0) {
			filters.add(Filters.byDayFilter(byDay, Frequency.YEARLY == freq, wkst));
			byDay = NO_DAYS;
		}

		if (byMonthDay.length > 0) {
			filters.add(Filters.byMonthDayFilter(byMonthDay));
		}

		//generator inference common to all periods
		if (byMonth.length > 0) {
			monthGenerator = Generators.byMonthGenerator(byMonth, start);
		} else if (monthGenerator == null) {
			monthGenerator = Generators.serialMonthGenerator(freq == Frequency.MONTHLY ? interval : 1, dtStart);
		}

		/*
		 * The condition tells the iterator when to halt. The condition is
		 * exclusive, so the date that triggers it will not be included.
		 */
		Predicate<DateValue> condition;
		if (count != 0) {
			condition = Conditions.countCondition(count);

			/*
			 * We can't shortcut because the countCondition must see every
			 * generated instance.
			 * 
			 * TODO(msamuel): If count is large, we might try predicting the end
			 * date so that we can convert the COUNT condition to an UNTIL
			 * condition.
			 */
			canShortcutAdvance = false;
		} else if (until != null) {
			DateValue untilUtc;
			if (until.hasTime()) {
				TimeZone utc = TimeZone.getTimeZone("UTC");
				untilUtc = Google2445Utils.convert(until, utc);
			} else {
				//treat the ICalDate object as a timezone-less, calendar date
				Calendar c = Calendar.getInstance();
				c.setTime(until);
				untilUtc = new DateValueImpl( //@formatter:off
					c.get(Calendar.YEAR),
					c.get(Calendar.MONTH) + 1,
					c.get(Calendar.DAY_OF_MONTH)
				); //@formatter:on
			}

			if ((untilUtc instanceof TimeValue) != (dtStart instanceof TimeValue)) {
				// TODO(msamuel): warn
				if (dtStart instanceof TimeValue) {
					untilUtc = TimeUtils.dayStart(untilUtc);
				} else {
					untilUtc = TimeUtils.toDateValue(untilUtc);
				}
			}
			condition = Conditions.untilCondition(untilUtc);
		} else {
			condition = Predicates.alwaysTrue();
		}

		//combine filters into a single function
		Predicate<? super DateValue> filter;
		switch (filters.size()) {
		case 0:
			filter = Predicates.<DateValue> alwaysTrue();
			break;
		case 1:
			filter = filters.get(0);
			break;
		default:
			filter = Predicates.and(filters);
			break;
		}

		Generator instanceGenerator;
		if (bySetPos.length > 0) {
			instanceGenerator = InstanceGenerators.bySetPosInstanceGenerator(bySetPos, freq, wkst, filter, yearGenerator, monthGenerator, dayGenerator, hourGenerator, minuteGenerator, secondGenerator);
		} else {
			instanceGenerator = InstanceGenerators.serialInstanceGenerator(filter, yearGenerator, monthGenerator, dayGenerator, hourGenerator, minuteGenerator, secondGenerator);
		}

		return new RRuleIteratorImpl(dtStart, tzid, condition, instanceGenerator, yearGenerator, monthGenerator, dayGenerator, hourGenerator, minuteGenerator, secondGenerator, canShortcutAdvance);
	}

	/**
	 * Generates a recurrence iterator that iterates over the union of the given
	 * recurrence iterators.
	 * @param first the first recurrence iterator
	 * @param rest the other recurrence iterators
	 * @return the union iterator
	 */
	public static RecurrenceIterator join(RecurrenceIterator first, RecurrenceIterator... rest) {
		List<RecurrenceIterator> all = new ArrayList<RecurrenceIterator>();
		all.add(first);
		all.addAll(Arrays.asList(rest));
		return new CompoundIteratorImpl(all, Collections.<RecurrenceIterator> emptyList());
	}

	/**
	 * <p>
	 * Generates a recurrence iterator that iterates over all the dates in a
	 * {@link RecurrenceIterator}, excluding those dates found in another
	 * {@link RecurrenceIterator}.
	 * </p>
	 * <p>
	 * Exclusions trump inclusions, and {@link DateValue dates} and
	 * {@link DateTimeValue date-times} never match one another.
	 * </p>
	 * @param included the dates to include
	 * @param excluded the dates to exclude
	 * @return the resultant iterator
	 */
	public static RecurrenceIterator except(RecurrenceIterator included, RecurrenceIterator excluded) {
		return new CompoundIteratorImpl(Collections.singleton(included), Collections.singleton(excluded));
	}

	/**
	 * <p>
	 * Creates an optimized version of an array based on the given BYSETPOS
	 * array.
	 * </p>
	 * <p>
	 * For example, given the array <code>BYMONTH=2,3,4,5</code> and a BYSETPOS
	 * of <code>BYSETPOS=1,-1</code>, this method will return
	 * <code>BYMONTH=2,5</code>.
	 * </p>
	 * @param members the array to optimize
	 * @param bySetPos the BYSETPOS array
	 * @return the optimized array
	 */
	private static int[] filterBySetPos(int[] members, int[] bySetPos) {
		members = Util.uniquify(members);
		IntSet iset = new IntSet();
		for (int pos : bySetPos) {
			if (pos == 0) {
				continue;
			}
			if (pos < 0) {
				pos += members.length;
			} else {
				--pos; // Zero-index.
			}
			if (pos >= 0 && pos < members.length) {
				iset.add(members[pos]);
			}
		}
		return iset.toIntArray();
	}

	/**
	 * Converts an {@link Integer} list to an int array. Null values are
	 * converted to zero.
	 * @param list the {@link Integer} list
	 * @return the int array
	 */
	private static int[] toIntArray(List<Integer> list) {
		int[] array = new int[list.size()];
		int i = 0;
		for (Integer intObj : list) {
			array[i++] = toInt(intObj);
		}
		return array;
	}

	private static int toInt(Integer integer) {
		return (integer == null) ? 0 : integer;
	}

	private static final int[] NO_INTS = new int[0];
	private static final ByDay[] NO_DAYS = new ByDay[0];

	private RecurrenceIteratorFactory() {
		//uninstantiable
	}
}
