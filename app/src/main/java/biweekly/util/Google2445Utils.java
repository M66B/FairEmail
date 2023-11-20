package biweekly.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import biweekly.component.ICalComponent;
import biweekly.property.DateStart;
import biweekly.property.ExceptionDates;
import biweekly.property.ExceptionRule;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceRule;
import biweekly.property.ValuedProperty;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;
import biweekly.util.com.google.ical.compat.javautil.DateIteratorFactory;
import biweekly.util.com.google.ical.iter.RecurrenceIterable;
import biweekly.util.com.google.ical.iter.RecurrenceIterator;
import biweekly.util.com.google.ical.iter.RecurrenceIteratorFactory;
import biweekly.util.com.google.ical.values.DateTimeValue;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

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
 * Contains utility methods related to the google-rfc-2445 project.
 * @author Michael Angstadt
 * @see <a href="https://code.google.com/p/google-rfc-2445/">google-rfc-2445</a>
 */
public final class Google2445Utils {
	/**
	 * <p>
	 * Converts an {@link ICalDate} object to a google-rfc-2445
	 * {@link DateValue} object using the {@link DateTimeComponents raw date
	 * components} of the {@link ICalDate}.
	 * </p>
	 * <p>
	 * If the {@link ICalDate} object does not have raw date components, then
	 * the returned {@link DateValue} object will represent the {@link ICalDate}
	 * in the local timezone.
	 * </p>
	 * @param date the date object
	 * @return the google-rfc-2445 object
	 */
	public static DateValue convertFromRawComponents(ICalDate date) {
		DateTimeComponents raw = date.getRawComponents();
		if (raw == null) {
			raw = new DateTimeComponents(date);
		}

		return convert(raw);
	}

	/**
	 * Converts a {@link DateTimeComponents} object to a google-rfc-2445
	 * {@link DateValue} object.
	 * @param components the date time components object
	 * @return the google-rfc-2445 object
	 */
	public static DateValue convert(DateTimeComponents components) {
		if (components.hasTime()) {
			//@formatter:off
			return new DateTimeValueImpl(
				components.getYear(),
				components.getMonth(),
				components.getDate(),
				components.getHour(),
				components.getMinute(),
				components.getSecond()
			);
			//@formatter:on
		}

		//@formatter:off
		return new DateValueImpl(
			components.getYear(),
			components.getMonth(),
			components.getDate()
		);
		//@formatter:on
	}

	/**
	 * Converts an {@link ICalDate} object to a google-rfc-2445
	 * {@link DateValue} object.
	 * @param date the date object
	 * @param timezone the timezone the returned object will be in
	 * @return the google-rfc-2445 object
	 */
	public static DateValue convert(ICalDate date, TimeZone timezone) {
		Calendar c = Calendar.getInstance(timezone);
		c.setTime(date);

		/*
		 * Do not create a "DateValueImpl" object if "date.hasTime() == false".
		 * This interferes with any recurrence iterators the object is passed
		 * into.
		 * 
		 * See: https://github.com/mangstadt/biweekly/issues/47
		 */

		//@formatter:off
		return new DateTimeValueImpl(
			c.get(Calendar.YEAR),
			c.get(Calendar.MONTH)+1,
			c.get(Calendar.DATE),
			c.get(Calendar.HOUR_OF_DAY),
			c.get(Calendar.MINUTE),
			c.get(Calendar.SECOND)
		);
		//@formatter:on
	}

	/**
	 * Converts an {@link ICalDate} object to a google-rfc-2445
	 * {@link DateValue} object. The returned object will be in UTC.
	 * @param date the date object
	 * @return the google-rfc-2445 object (in UTC)
	 */
	public static DateValue convertUtc(ICalDate date) {
		return convert(date, utc());
	}

	/**
	 * Converts a google-rfc-2445 {@link DateValue} object to a {@link ICalDate}
	 * object.
	 * @param date the date value object
	 * @param timezone the timezone the date value is in
	 * @return the converted object
	 */
	public static ICalDate convert(DateValue date, TimeZone timezone) {
		Calendar c = Calendar.getInstance(timezone);
		c.clear();
		c.set(Calendar.YEAR, date.year());
		c.set(Calendar.MONTH, date.month() - 1);
		c.set(Calendar.DATE, date.day());

		boolean hasTime = (date instanceof DateTimeValue);
		if (hasTime) {
			DateTimeValue dateTime = (DateTimeValue) date;
			c.set(Calendar.HOUR_OF_DAY, dateTime.hour());
			c.set(Calendar.MINUTE, dateTime.minute());
			c.set(Calendar.SECOND, dateTime.second());
		}

		return new ICalDate(c.getTime(), hasTime);
	}

	/**
	 * Converts a google-rfc-2445 {@link DateValue} object to a {@link ICalDate}
	 * object. It is assumed that the given {@link DateValue} object is in UTC.
	 * @param date the date value object (in UTC)
	 * @return the converted object
	 */
	public static ICalDate convertUtc(DateValue date) {
		return convert(date, utc());
	}

	/**
	 * Creates a recurrence iterator based on the given recurrence rule.
	 * @param recurrence the recurrence rule
	 * @param start the start date
	 * @param timezone the timezone to iterate in. This is needed in order to
	 * account for when the iterator passes over a daylight savings boundary.
	 * @return the recurrence iterator
	 */
	public static RecurrenceIterator createRecurrenceIterator(Recurrence recurrence, ICalDate start, TimeZone timezone) {
		DateValue startValue = convert(start, timezone);
		return RecurrenceIteratorFactory.createRecurrenceIterator(recurrence, startValue, timezone);
	}

	/**
	 * Creates a recurrence iterator based on the given recurrence rule.
	 * @param recurrence the recurrence rule
	 * @param start the start date
	 * @param timezone the timezone to iterate in. This is needed in order to
	 * account for when the iterator passes over a daylight savings boundary.
	 * @return the recurrence iterator
	 */
	public static RecurrenceIterable createRecurrenceIterable(Recurrence recurrence, ICalDate start, TimeZone timezone) {
		DateValue startValue = convert(start, timezone);
		return RecurrenceIteratorFactory.createRecurrenceIterable(recurrence, startValue, timezone);
	}

	/**
	 * <p>
	 * Creates an iterator that computes the dates defined by the
	 * {@link RecurrenceRule} and {@link RecurrenceDates} properties (if
	 * present), and excludes those dates which are defined by the
	 * {@link ExceptionRule} and {@link ExceptionDates} properties (if present).
	 * </p>
	 * <p>
	 * In order for {@link RecurrenceRule} and {@link ExceptionRule} properties
	 * to be included in this iterator, a {@link DateStart} property must be
	 * defined.
	 * </p>
	 * <p>
	 * {@link Period} values in {@link RecurrenceDates} properties are not
	 * supported and are ignored.
	 * </p>
	 * @param component the component
	 * @param timezone the timezone to iterate in. This is needed in order to
	 * adjust for when the iterator passes over a daylight savings boundary.
	 * This parameter is ignored if the start date of the given component does
	 * not have a time component.
	 * @return the iterator
	 */
	public static DateIterator getDateIterator(ICalComponent component, TimeZone timezone) {
		DateStart dtstart = component.getProperty(DateStart.class);
		ICalDate start = ValuedProperty.getValue(dtstart);

		/*
		 * If the start date is just a date and does not have a time component,
		 * then the default timezone must be used, because this is the timezone
		 * biweekly used to parse the date.
		 */
		if (start != null && !start.hasTime()) {
			timezone = TimeZone.getDefault();
		}

		/////////////INCLUDE/////////////

		List<RecurrenceIterator> include = new ArrayList<RecurrenceIterator>();

		if (start != null) {
			for (RecurrenceRule rrule : component.getProperties(RecurrenceRule.class)) {
				Recurrence recurrence = ValuedProperty.getValue(rrule);
				if (recurrence != null) {
					include.add(createRecurrenceIterator(recurrence, start, timezone));
				}
			}
		}

		List<ICalDate> allDates = new ArrayList<ICalDate>();
		for (RecurrenceDates rdate : component.getProperties(RecurrenceDates.class)) {
			allDates.addAll(rdate.getDates());
		}
		if (!allDates.isEmpty()) {
			include.add(new ICalDateRecurrenceIterator(allDates));
		}

		if (include.isEmpty()) {
			if (start == null) {
				return new EmptyDateIterator();
			}
			include.add(new ICalDateRecurrenceIterator(Collections.singletonList(start)));
		}

		/////////////EXCLUDE/////////////

		List<RecurrenceIterator> exclude = new ArrayList<RecurrenceIterator>();

		if (start != null) {
			for (ExceptionRule exrule : component.getProperties(ExceptionRule.class)) {
				Recurrence recurrence = ValuedProperty.getValue(exrule);
				if (recurrence != null) {
					exclude.add(createRecurrenceIterator(recurrence, start, timezone));
				}
			}
		}

		allDates = new ArrayList<ICalDate>();
		for (ExceptionDates exdate : component.getProperties(ExceptionDates.class)) {
			allDates.addAll(exdate.getValues());
		}
		if (!allDates.isEmpty()) {
			exclude.add(new ICalDateRecurrenceIterator(allDates));
		}

		/////////////JOIN/////////////

		RecurrenceIterator includeJoined = join(include);
		if (exclude.isEmpty()) {
			return DateIteratorFactory.createDateIterator(includeJoined);
		}

		RecurrenceIterator excludeJoined = join(exclude);
		RecurrenceIterator iterator = RecurrenceIteratorFactory.except(includeJoined, excludeJoined);
		return DateIteratorFactory.createDateIterator(iterator);
	}

	/**
	 * Creates a single {@link RecurrenceIterator} that is a union of the given
	 * iterators.
	 * @param iterators the iterators
	 * @return the union of the given iterators
	 * @see RecurrenceIteratorFactory#join
	 */
	private static RecurrenceIterator join(List<RecurrenceIterator> iterators) {
		if (iterators.size() == 1) {
			return iterators.get(0);
		}
		RecurrenceIterator first = iterators.get(0);
		List<RecurrenceIterator> rest = iterators.subList(1, iterators.size());
		return RecurrenceIteratorFactory.join(first, rest.toArray(new RecurrenceIterator[0]));
	}

	/**
	 * Returns a UTC timezone object.
	 * @return the timezone object
	 */
	private static TimeZone utc() {
		return TimeZone.getTimeZone("UTC");
	}

	/**
	 * A {@link DateIterator} with nothing in it.
	 */
	public static class EmptyDateIterator implements DateIterator {
		public boolean hasNext() {
			return false;
		}

		public Date next() {
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void advanceTo(Date newStartUtc) {
			//empty
		}
	}

	private static class ICalDateRecurrenceIterator implements RecurrenceIterator {
		private final List<ICalDate> dates;
		private int index = 0;

		/*
		 * Note: I don't think a timezone needs to be passed in here, because it
		 * appears that the DateValue objects are expected to be in UTC (judging
		 * by the parameter name in the "advanceTo" method).
		 */
		public ICalDateRecurrenceIterator(List<ICalDate> dates) {
			this.dates = new ArrayList<ICalDate>(dates);
			Collections.sort(this.dates);
		}

		public boolean hasNext() {
			return index < dates.size();
		}

		public DateValue next() {
			ICalDate next = dates.get(index++);
			return convertUtc(next);
		}

		public void advanceTo(DateValue newStartUtc) {
			ICalDate newStart = convertUtc(newStartUtc);
			while (index < dates.size() && newStart.compareTo(dates.get(index)) > 0) {
				index++;
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private Google2445Utils() {
		//hide
	}
}
