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

package biweekly.util.com.google.ical.compat.javautil;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import biweekly.util.com.google.ical.iter.RecurrenceIterable;
import biweekly.util.com.google.ical.iter.RecurrenceIterator;
import biweekly.util.com.google.ical.iter.RecurrenceIteratorFactory;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;
import biweekly.util.com.google.ical.values.TimeValue;

/**
 * A factory for converting RRULEs and RDATEs into
 * <code>Iterator&lt;Date&gt;</code> and <code>Iterable&lt;Date&gt;</code>.
 * @see RecurrenceIteratorFactory
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class DateIteratorFactory {
	/**
	 * Creates a date iterator from a recurrence iterator.
	 * @param rit the recurrence iterator
	 * @return the date iterator
	 */
	public static DateIterator createDateIterator(RecurrenceIterator rit) {
		return new RecurrenceIteratorWrapper(rit);
	}

	/**
	 * Creates a date iterable from a recurrence iterable.
	 * @param rit the recurrence iterable
	 * @return the date iterable
	 */
	public static DateIterable createDateIterable(RecurrenceIterable rit) {
		return new RecurrenceIterableWrapper(rit);
	}

	private static final class RecurrenceIterableWrapper implements DateIterable {
		private final RecurrenceIterable it;

		public RecurrenceIterableWrapper(RecurrenceIterable it) {
			this.it = it;
		}

		public DateIterator iterator() {
			return new RecurrenceIteratorWrapper(it.iterator());
		}
	}

	private static final class RecurrenceIteratorWrapper implements DateIterator {
		private final RecurrenceIterator it;
		private final Calendar utcCalendar = new GregorianCalendar(TimeUtils.utcTimezone());

		public RecurrenceIteratorWrapper(RecurrenceIterator it) {
			this.it = it;
		}

		public boolean hasNext() {
			return it.hasNext();
		}

		public Date next() {
			return toDate(it.next());
		}

		public void advanceTo(Date d) {
			it.advanceTo(toDateValue(d));
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Converts a {@link DateValue} object into a Java {@link Date} object.
		 * @param dateValue the date value object (assumed to be in UTC time)
		 * @return the Java date object
		 */
		private Date toDate(DateValue dateValue) {
			TimeValue time = TimeUtils.timeOf(dateValue);
			utcCalendar.clear();
			//@formatter:off
			utcCalendar.set(
				dateValue.year(),
				dateValue.month() - 1, //java.util's dates are zero-indexed
				dateValue.day(),
				time.hour(),
				time.minute(),
				time.second()
			);
			//@formatter:on
			return utcCalendar.getTime();
		}

		/**
		 * Converts a Java {@link Date} object into a {@link DateValue} object.
		 * The {@link DateValue} object will be in UTC time.
		 * @param date the Java date object
		 * @return the date value object (in UTC time)
		 */
		private DateValue toDateValue(Date date) {
			utcCalendar.setTime(date);

			int year = utcCalendar.get(Calendar.YEAR);
			int month = utcCalendar.get(Calendar.MONTH) + 1; //java.util's dates are zero-indexed
			int day = utcCalendar.get(Calendar.DAY_OF_MONTH);
			int hour = utcCalendar.get(Calendar.HOUR_OF_DAY);
			int minute = utcCalendar.get(Calendar.MINUTE);
			int second = utcCalendar.get(Calendar.SECOND);

			/*
			 * We need to treat midnight as a date value so that passing in
			 * dateValueToDate(<some-date-value>) will not advance past any
			 * occurrences of some-date-value in the iterator.
			 */
			if ((hour | minute | second) == 0) {
				return new DateValueImpl(year, month, day);
			}
			return new DateTimeValueImpl(year, month, day, hour, minute, second);
		}
	}

	private DateIteratorFactory() {
		// uninstantiable
	}
}
