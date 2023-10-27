package biweekly.io;

import static biweekly.property.ValuedProperty.getValue;
import static biweekly.util.Google2445Utils.convertFromRawComponents;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import biweekly.Messages;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.Observance;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.property.ExceptionDates;
import biweekly.property.ExceptionRule;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceRule;
import biweekly.property.TimezoneName;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.UtcOffset;
import biweekly.util.com.google.ical.iter.RecurrenceIterator;
import biweekly.util.com.google.ical.iter.RecurrenceIteratorFactory;
import biweekly.util.com.google.ical.util.DTBuilder;
import biweekly.util.com.google.ical.values.DateTimeValue;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;

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
 * A timezone that is based on an iCalendar {@link VTimezone} component. This
 * class is not thread safe.
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class ICalTimeZone extends TimeZone {
	private final VTimezone component;
	private final Map<Observance, List<DateValue>> observanceDateCache;
	final List<Observance> sortedObservances;
	private final int rawOffset;
	private final TimeZone utc = TimeZone.getTimeZone("UTC");
	private final Calendar utcCalendar = Calendar.getInstance(utc);

	/**
	 * Creates a new timezone based on an iCalendar VTIMEZONE component.
	 * @param component the VTIMEZONE component to wrap
	 */
	public ICalTimeZone(VTimezone component) {
		this.component = component;

		int numObservances = component.getStandardTimes().size() + component.getDaylightSavingsTime().size();
		observanceDateCache = new IdentityHashMap<Observance, List<DateValue>>(numObservances);

		sortedObservances = calculateSortedObservances();

		rawOffset = calculateRawOffset();

		String id = getValue(component.getTimezoneId());
		if (id != null) {
			setID(id);
		}
	}

	/**
	 * Builds a list of all the observances in the VTIMEZONE component, sorted
	 * by DTSTART.
	 * @return the sorted observances
	 */
	private List<Observance> calculateSortedObservances() {
		List<DaylightSavingsTime> daylights = component.getDaylightSavingsTime();
		List<StandardTime> standards = component.getStandardTimes();

		int numObservances = standards.size() + daylights.size();
		List<Observance> sortedObservances = new ArrayList<Observance>(numObservances);

		sortedObservances.addAll(standards);
		sortedObservances.addAll(daylights);

		Collections.sort(sortedObservances, new Comparator<Observance>() {
			public int compare(Observance left, Observance right) {
				ICalDate startLeft = getValue(left.getDateStart());
				ICalDate startRight = getValue(right.getDateStart());
				if (startLeft == null && startRight == null) {
					return 0;
				}
				if (startLeft == null) {
					return -1;
				}
				if (startRight == null) {
					return 1;
				}

				return startLeft.getRawComponents().compareTo(startRight.getRawComponents());
			}
		});

		return Collections.unmodifiableList(sortedObservances);
	}

	@Override
	public String getDisplayName(boolean daylight, int style, Locale locale) {
		ListIterator<Observance> it = sortedObservances.listIterator(sortedObservances.size());
		while (it.hasPrevious()) {
			Observance observance = it.previous();

			if (daylight && observance instanceof DaylightSavingsTime) {
				List<TimezoneName> names = observance.getTimezoneNames();
				if (!names.isEmpty()) {
					String name = names.get(0).getValue();
					if (name != null) {
						return name;
					}
				}
			}

			if (!daylight && observance instanceof StandardTime) {
				List<TimezoneName> names = observance.getTimezoneNames();
				if (!names.isEmpty()) {
					String name = names.get(0).getValue();
					if (name != null) {
						return name;
					}
				}
			}
		}

		return super.getDisplayName(daylight, style, locale);
	}

	@Override
	public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
		int hour = millis / 1000 / 60 / 60;
		millis -= hour * 1000 * 60 * 60;
		int minute = millis / 1000 / 60;
		millis -= minute * 1000 * 60;
		int second = millis / 1000;

		Observance observance = getObservance(year, month + 1, day, hour, minute, second);
		if (observance == null) {
			/*
			 * Find the first observance that has a DTSTART property and a
			 * TZOFFSETFROM property.
			 */
			for (Observance obs : sortedObservances) {
				ICalDate dateStart = getValue(obs.getDateStart());
				if (dateStart == null) {
					continue;
				}

				UtcOffset offsetFrom = getValue(obs.getTimezoneOffsetFrom());
				if (offsetFrom == null) {
					continue;
				}

				return (int) offsetFrom.getMillis();
			}
			return 0;
		}

		UtcOffset offsetTo = getValue(observance.getTimezoneOffsetTo());
		return (offsetTo == null) ? 0 : (int) offsetTo.getMillis();
	}

	@Override
	public int getRawOffset() {
		return rawOffset;
	}

	private int calculateRawOffset() {
		Observance observance = getObservance(new Date());
		if (observance == null) {
			//return the offset of the first STANDARD component
			for (Observance obs : sortedObservances) {
				if (!(obs instanceof StandardTime)) {
					continue;
				}

				UtcOffset offsetTo = getValue(obs.getTimezoneOffsetTo());
				if (offsetTo == null) {
					continue;
				}

				return (int) offsetTo.getMillis();
			}
			return 0;
		}

		UtcOffset offset = getValue((observance instanceof StandardTime) ? observance.getTimezoneOffsetTo() : observance.getTimezoneOffsetFrom());
		return (offset == null) ? 0 : (int) offset.getMillis();
	}

	@Override
	public boolean inDaylightTime(Date date) {
		if (!useDaylightTime()) {
			return false;
		}

		Observance observance = getObservance(date);
		return (observance == null) ? false : (observance instanceof DaylightSavingsTime);
	}

	/**
	 * This method is not supported by this class.
	 * @throws UnsupportedOperationException thrown when this method is called
	 */
	@Override
	public void setRawOffset(int offset) {
		throw new UnsupportedOperationException(Messages.INSTANCE.getExceptionMessage(12));
	}

	@Override
	public boolean useDaylightTime() {
		for (Observance observance : sortedObservances) {
			if (observance instanceof DaylightSavingsTime) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the timezone information of a date.
	 * @param date the date
	 * @return the timezone information
	 */
	public Boundary getObservanceBoundary(Date date) {
		utcCalendar.setTime(date);
		int year = utcCalendar.get(Calendar.YEAR);
		int month = utcCalendar.get(Calendar.MONTH) + 1;
		int day = utcCalendar.get(Calendar.DATE);
		int hour = utcCalendar.get(Calendar.HOUR);
		int minute = utcCalendar.get(Calendar.MINUTE);
		int second = utcCalendar.get(Calendar.SECOND);

		return getObservanceBoundary(year, month, day, hour, minute, second);
	}

	/**
	 * Gets the observance that a date is effected by.
	 * @param date the date
	 * @return the observance or null if an observance cannot be found
	 */
	public Observance getObservance(Date date) {
		Boundary boundary = getObservanceBoundary(date);
		return (boundary == null) ? null : boundary.getObservanceIn();
	}

	/**
	 * <p>
	 * Gets the VTIMEZONE component that is being wrapped.
	 * </p>
	 * <p>
	 * Note that the ICalTimeZone class makes heavy use of caching. Any
	 * modifications made to the VTIMEZONE component that is returned by this
	 * method may effect the accuracy of this ICalTimeZone instance.
	 * </p>
	 * @return the VTIMEZONE component
	 */
	public VTimezone getComponent() {
		return component;
	}

	/**
	 * Gets the observance that a date is effected by.
	 * @param year the year
	 * @param month the month (1-12)
	 * @param day the day of the month
	 * @param hour the hour
	 * @param minute the minute
	 * @param second the second
	 * @return the observance or null if an observance cannot be found
	 */
	private Observance getObservance(int year, int month, int day, int hour, int minute, int second) {
		Boundary boundary = getObservanceBoundary(year, month, day, hour, minute, second);
		return (boundary == null) ? null : boundary.getObservanceIn();
	}

	/**
	 * Gets the observance information of a date.
	 * @param year the year
	 * @param month the month (1-12)
	 * @param day the day of the month
	 * @param hour the hour
	 * @param minute the minute
	 * @param second the second
	 * @return the observance information or null if none was found
	 */
	private Boundary getObservanceBoundary(int year, int month, int day, int hour, int minute, int second) {
		if (sortedObservances.isEmpty()) {
			return null;
		}

		DateValue givenTime = new DateTimeValueImpl(year, month, day, hour, minute, second);
		int closestIndex = -1;
		Observance closest = null;
		DateValue closestValue = null;
		for (int i = 0; i < sortedObservances.size(); i++) {
			Observance observance = sortedObservances.get(i);

			//skip observances that start after the given time
			ICalDate dtstart = getValue(observance.getDateStart());
			if (dtstart != null) {
				DateValue dtstartValue = convertFromRawComponents(dtstart);
				if (dtstartValue.compareTo(givenTime) > 0) {
					continue;
				}
			}

			DateValue dateValue = getObservanceDateClosestToTheGivenDate(observance, givenTime, false);
			if (dateValue != null && (closestValue == null || closestValue.compareTo(dateValue) < 0)) {
				closestValue = dateValue;
				closest = observance;
				closestIndex = i;
			}
		}

		Observance observanceIn = closest;
		DateValue observanceInStart = closestValue;
		Observance observanceAfter = null;
		DateValue observanceAfterStart = null;
		if (closestIndex < sortedObservances.size() - 1) {
			observanceAfter = sortedObservances.get(closestIndex + 1);
			observanceAfterStart = getObservanceDateClosestToTheGivenDate(observanceAfter, givenTime, true);
		}

		/*
		 * If any of the DTSTART properties are missing their time components,
		 * then observanceInStart/observanceAfterStart could be a DateValue
		 * object. If so, convert it to a DateTimeValue object (see Issue 77).
		 */
		if (observanceInStart != null && !(observanceInStart instanceof DateTimeValue)) {
			observanceInStart = new DTBuilder(observanceInStart).toDateTime();
		}
		if (observanceAfterStart != null && !(observanceAfterStart instanceof DateTimeValue)) {
			observanceAfterStart = new DTBuilder(observanceAfterStart).toDateTime();
		}

		return new Boundary((DateTimeValue) observanceInStart, observanceIn, (DateTimeValue) observanceAfterStart, observanceAfter);
	}

	/**
	 * Iterates through each of the timezone boundary dates defined by the given
	 * observance and finds the date that comes closest to the given date.
	 * @param observance the observance
	 * @param givenDate the given date
	 * @param after true to return the closest date <b>greater than</b> the
	 * given date, false to return the closest date <b>less than or equal to</b>
	 * the given date.
	 * @return the closest date
	 */
	private DateValue getObservanceDateClosestToTheGivenDate(Observance observance, DateValue givenDate, boolean after) {
		List<DateValue> dateCache = observanceDateCache.get(observance);
		if (dateCache == null) {
			dateCache = new ArrayList<DateValue>();
			observanceDateCache.put(observance, dateCache);
		}

		if (dateCache.isEmpty()) {
			DateValue prev = null, cur = null;
			boolean stopped = false;
			RecurrenceIterator it = createIterator(observance);
			while (it.hasNext()) {
				cur = it.next();
				dateCache.add(cur);

				if (givenDate.compareTo(cur) < 0) {
					//stop if we have passed the givenTime
					stopped = true;
					break;
				}

				prev = cur;
			}
			return after ? (stopped ? cur : null) : prev;
		}

		DateValue last = dateCache.get(dateCache.size() - 1);
		int comparison = last.compareTo(givenDate);
		if ((after && comparison <= 0) || comparison < 0) {
			RecurrenceIterator it = createIterator(observance);

			/*
			 * The "advanceTo()" method skips all dates that are less than the
			 * given date. I would have thought that we would have to call
			 * "next()" once because we want it to skip the date that is equal
			 * to the "last" date. But this causes all the unit tests to fail,
			 * so I guess not.
			 */
			it.advanceTo(last);
			//it.next();

			DateValue prev = null, cur = null;
			boolean stopped = false;
			while (it.hasNext()) {
				cur = it.next();
				dateCache.add(cur);

				if (givenDate.compareTo(cur) < 0) {
					//stop if we have passed the givenTime
					stopped = true;
					break;
				}

				prev = cur;
			}
			return after ? (stopped ? cur : null) : prev;
		}

		/*
		 * The date is somewhere in the cached list, so find it.
		 * 
		 * Note: Read the "binarySearch" method Javadoc carefully for an
		 * explanation of its return value.
		 */
		int index = Collections.binarySearch(dateCache, givenDate);

		if (index < 0) {
			/*
			 * The index where the date would be if it was inside the list.
			 */
			index = (index * -1) - 1;

			if (after) {
				/*
				 * This is where the date would be if it was inside the list, so
				 * we want to return the date value that's currently at that
				 * position.
				 */
				int afterIndex = index;

				return (afterIndex < dateCache.size()) ? dateCache.get(afterIndex) : null;
			}

			int beforeIndex = index - 1;
			if (beforeIndex < 0) {
				return null;
			}
			if (beforeIndex >= dateCache.size()) {
				return dateCache.get(dateCache.size() - 1);
			}
			return dateCache.get(beforeIndex);
		}

		/*
		 * An exact match was found.
		 */
		if (after) {
			int afterIndex = index + 1; //remember: the date must be >
			return (afterIndex < dateCache.size()) ? dateCache.get(afterIndex) : null;
		}
		return dateCache.get(index); //remember: the date must be <=
	}

	/**
	 * Creates an iterator which iterates over each of the dates in an
	 * observance.
	 * @param observance the observance
	 * @return the iterator
	 */
	RecurrenceIterator createIterator(Observance observance) {
		List<RecurrenceIterator> inclusions = new ArrayList<RecurrenceIterator>();
		List<RecurrenceIterator> exclusions = new ArrayList<RecurrenceIterator>();

		ICalDate dtstart = getValue(observance.getDateStart());
		if (dtstart != null) {
			DateValue dtstartValue = convertFromRawComponents(dtstart);

			//add DTSTART property
			inclusions.add(new DateValueRecurrenceIterator(Collections.singletonList(dtstartValue)));

			//add RRULE properties
			for (RecurrenceRule rrule : observance.getProperties(RecurrenceRule.class)) {
				Recurrence recur = rrule.getValue();
				if (recur != null) {
					inclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(recur, dtstartValue, utc));
				}
			}

			//add EXRULE properties
			for (ExceptionRule exrule : observance.getProperties(ExceptionRule.class)) {
				Recurrence recur = exrule.getValue();
				if (recur != null) {
					exclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(recur, dtstartValue, utc));
				}
			}
		}

		//add RDATE properties
		List<ICalDate> rdates = new ArrayList<ICalDate>();
		for (RecurrenceDates rdate : observance.getRecurrenceDates()) {
			rdates.addAll(rdate.getDates());
		}
		Collections.sort(rdates);
		inclusions.add(new DateRecurrenceIterator(rdates));

		//add EXDATE properties
		List<ICalDate> exdates = new ArrayList<ICalDate>();
		for (ExceptionDates exdate : observance.getProperties(ExceptionDates.class)) {
			exdates.addAll(exdate.getValues());
		}
		Collections.sort(exdates);
		exclusions.add(new DateRecurrenceIterator(exdates));

		RecurrenceIterator included = join(inclusions);
		if (exclusions.isEmpty()) {
			return included;
		}

		RecurrenceIterator excluded = join(exclusions);
		return RecurrenceIteratorFactory.except(included, excluded);
	}

	private static RecurrenceIterator join(List<RecurrenceIterator> iterators) {
		if (iterators.isEmpty()) {
			return new EmptyRecurrenceIterator();
		}

		RecurrenceIterator first = iterators.get(0);
		if (iterators.size() == 1) {
			return first;
		}

		List<RecurrenceIterator> theRest = iterators.subList(1, iterators.size());
		return RecurrenceIteratorFactory.join(first, theRest.toArray(new RecurrenceIterator[0]));
	}

	/**
	 * A recurrence iterator that doesn't have any elements.
	 */
	private static class EmptyRecurrenceIterator implements RecurrenceIterator {
		public boolean hasNext() {
			return false;
		}

		public DateValue next() {
			throw new NoSuchElementException();
		}

		public void advanceTo(DateValue newStartUtc) {
			//empty
		}

		public void remove() {
			//RecurrenceIterator does not support this method
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * A recurrence iterator that takes a collection of {@link DateValue}
	 * objects.
	 */
	private static class DateValueRecurrenceIterator extends IteratorWrapper<DateValue> {
		public DateValueRecurrenceIterator(Collection<DateValue> dates) {
			super(dates.iterator());
		}

		@Override
		protected DateValue toDateValue(DateValue value) {
			return value;
		}
	}

	/**
	 * A recurrence iterator that takes a collection of {@link ICalDate}
	 * objects.
	 */
	private static class DateRecurrenceIterator extends IteratorWrapper<ICalDate> {
		public DateRecurrenceIterator(Collection<ICalDate> dates) {
			super(dates.iterator());
		}

		@Override
		protected DateValue toDateValue(ICalDate value) {
			return convertFromRawComponents(value);
		}
	}

	/**
	 * A recurrence iterator that wraps an {@link Iterator}.
	 */
	private static abstract class IteratorWrapper<T> implements RecurrenceIterator {
		protected final Iterator<T> it;
		private DateValue next;

		public IteratorWrapper(Iterator<T> it) {
			this.it = it;
		}

		public DateValue next() {
			if (next != null) {
				DateValue value = next;
				next = null;
				return value;
			}
			return toDateValue(it.next());
		}

		public boolean hasNext() {
			return next != null || it.hasNext();
		}

		public void advanceTo(DateValue newStartUtc) {
			if (this.next != null && this.next.compareTo(newStartUtc) >= 0) {
				return;
			}

			while (it.hasNext()) {
				DateValue next = toDateValue(it.next());
				if (next.compareTo(newStartUtc) >= 0) {
					this.next = next;
					break;
				}
			}
		}

		public void remove() {
			//RecurrenceIterator does not support this method
			throw new UnsupportedOperationException();
		}

		protected abstract DateValue toDateValue(T next);
	}

	/**
	 * Holds the timezone observance information of a particular date.
	 */
	public static class Boundary {
		private final DateTimeValue observanceInStart, observanceAfterStart;
		private final Observance observanceIn, observanceAfter;

		public Boundary(DateTimeValue observanceInStart, Observance observanceIn, DateTimeValue observanceAfterStart, Observance observanceAfter) {
			this.observanceInStart = observanceInStart;
			this.observanceAfterStart = observanceAfterStart;
			this.observanceIn = observanceIn;
			this.observanceAfter = observanceAfter;
		}

		/**
		 * Gets start time of the observance that the date resides in.
		 * @return the time
		 */
		public DateTimeValue getObservanceInStart() {
			return observanceInStart;
		}

		/**
		 * Gets the start time the observance that comes after the observance
		 * that the date resides in.
		 * @return the time
		 */
		public DateTimeValue getObservanceAfterStart() {
			return observanceAfterStart;
		}

		/**
		 * Gets the observance that the date resides in.
		 * @return the observance
		 */
		public Observance getObservanceIn() {
			return observanceIn;
		}

		/**
		 * Gets the observance that comes after the observance that the date
		 * resides in.
		 * @return the observance
		 */
		public Observance getObservanceAfter() {
			return observanceAfter;
		}

		@Override
		public String toString() {
			return "Boundary [observanceInStart=" + observanceInStart + ", observanceAfterStart=" + observanceAfterStart + ", observanceIn=" + observanceIn + ", observanceAfter=" + observanceAfter + "]";
		}
	}
}
