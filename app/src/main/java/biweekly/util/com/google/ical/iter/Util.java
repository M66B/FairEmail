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
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateValue;

/**
 * A dumping ground for utility functions that don't fit anywhere else.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
class Util {
	/**
	 * <p>
	 * Advances the given date to next date that falls on the given weekday. If
	 * the given date already falls on the given weekday, then the same date is
	 * returned.
	 * </p>
	 * <p>
	 * For example, if the date is a Thursday, and the week start is Monday,
	 * this method will return a date value that is set to the next Monday (4
	 * days in the future).
	 * </p>
	 * @param date the date
	 * @param weekday the day of the week that the week starts on
	 * @return the resultant date
	 */
	static DateValue nextWeekStart(DateValue date, DayOfWeek weekday) {
		DTBuilder builder = new DTBuilder(date);
		builder.day += (7 - ((7 + (TimeUtils.dayOfWeek(date).getCalendarConstant() - weekday.getCalendarConstant())) % 7)) % 7;
		return builder.toDate();
	}

	/**
	 * Returns a sorted copy of an integer array with duplicate values removed.
	 * @param ints the integer array
	 * @return the sorted copy with duplicates removed
	 */
	static int[] uniquify(int[] ints) {
		IntSet iset = new IntSet();
		for (int i : ints) {
			iset.add(i);
		}
		return iset.toIntArray();
	}

	/**
	 * Given a weekday number, such as {@code -1SU}, this method calculates the
	 * day of the month that it falls on. The weekday number may be refer to a
	 * week in the current month in some contexts, or a week in the current year
	 * in other contexts.
	 * @param dow0 the day of week of the first day in the current year/month
	 * @param nDays the number of days in the current year/month (must be one of
	 * the following values [28,29,30,31,365,366])
	 * @param weekNum the weekday number (for example, the -1 in {@code -1SU})
	 * @param dow the day of the week (for example, the SU in {@code -1SU})
	 * @param d0 the number of days between the first day of the current
	 * year/month and the current month
	 * @param nDaysInMonth the number of days in the current month
	 * @return the day of the month, or 0 if no such day exists
	 */
	static int dayNumToDate(DayOfWeek dow0, int nDays, int weekNum, DayOfWeek dow, int d0, int nDaysInMonth) {
		//if dow is wednesday, then this is the date of the first wednesday
		int firstDateOfGivenDow = 1 + ((7 + dow.getCalendarConstant() - dow0.getCalendarConstant()) % 7);

		int date;
		if (weekNum > 0) {
			date = ((weekNum - 1) * 7) + firstDateOfGivenDow - d0;
		} else { //count weeks from end of month
			//calculate last day of the given dow
			//since nDays <= 366, this should be > nDays
			int lastDateOfGivenDow = firstDateOfGivenDow + (7 * 54);
			lastDateOfGivenDow -= 7 * ((lastDateOfGivenDow - nDays + 6) / 7);
			date = lastDateOfGivenDow + 7 * (weekNum + 1) - d0;
		}
		return (date <= 0 || date > nDaysInMonth) ? 0 : date;
	}

	/**
	 * <p>
	 * Converts a relative week number (such as {@code -1SU}) to an absolute
	 * week number.
	 * </p>
	 * <p>
	 * For example, the week number {@code -1SU} refers to the last Sunday of
	 * either the month or year (depending on how this method was called). So if
	 * there are 5 Sundays in the given period, then given a week number of
	 * {@code -1SU}, this method would return 5. Similarly, {@code -2SU} would
	 * return 4.
	 * </p>
	 * @param weekdayNum the weekday number (must be a negative value, such as
	 * {@code -1SU})
	 * @param dow0 the day of the week of the first day of the week or month
	 * @param nDays the number of days in the month or year
	 * @return the absolute week number
	 */
	static int invertWeekdayNum(ByDay weekdayNum, DayOfWeek dow0, int nDays) {
		//how many are there of that week?
		return countInPeriod(weekdayNum.getDay(), dow0, nDays) + weekdayNum.getNum() + 1;
	}

	/**
	 * Counts the number of occurrences of a weekday in a given period.
	 * @param dow the weekday
	 * @param dow0 the weekday of the first day of the period
	 * @param nDays the number of days in the period
	 */
	static int countInPeriod(DayOfWeek dow, DayOfWeek dow0, int nDays) {
		//two cases:
		//   (1a) dow >= dow0: count === (nDays - (dow - dow0)) / 7
		//   (1b) dow < dow0:  count === (nDays - (7 - dow0 - dow)) / 7
		if (dow.getCalendarConstant() >= dow0.getCalendarConstant()) {
			return 1 + ((nDays - (dow.getCalendarConstant() - dow0.getCalendarConstant()) - 1) / 7);
		} else {
			return 1 + ((nDays - (7 - (dow0.getCalendarConstant() - dow.getCalendarConstant())) - 1) / 7);
		}
	}

	private Util() {
		//uninstantiable
	}
}
