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

import java.util.TimeZone;

import biweekly.util.com.google.ical.util.DTBuilder;
import biweekly.util.com.google.ical.util.Predicate;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;
import biweekly.util.com.google.ical.values.TimeValue;

/**
 * Iterates over dates in an RRULE or EXRULE series.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
final class RRuleIteratorImpl implements RecurrenceIterator {
	/**
	 * Determines when the recurrence ends. The condition is applied
	 * <b>after</b> the date is converted to UTC.
	 */
	private final Predicate<? super DateValue> condition;

	/**
	 * Applies the various period generators to generate an entire date. This
	 * may involve generating a set of dates and discarding all but those that
	 * match the BYSETPOS rule.
	 */
	private final Generator instanceGenerator;

	/**
	 * Populates the builder's year field. Returns false if there aren't more
	 * years available.
	 */
	private final ThrottledGenerator yearGenerator;

	/**
	 * Populates the builder's month field. Returns false if there aren't more
	 * months available in the builder's year.
	 */
	private final Generator monthGenerator;

	/**
	 * A date that has been computed but not yet yielded to the user.
	 */
	private DateValue pendingUtc;

	/**
	 * Used to build successive dates. At the start of the building process,
	 * this contains the last date generated. Different periods are successively
	 * inserted into it.
	 */
	private DTBuilder builder;

	/**
	 * True iff the recurrence has been exhausted.
	 */
	private boolean done;

	/**
	 * The start date of the recurrence.
	 */
	private final DateValue dtStart;

	/**
	 * False iff shortcutting advance would break the semantics of the
	 * iteration. This may happen when, for example, the end condition requires
	 * that it see every item.
	 */
	private final boolean canShortcutAdvance;

	/**
	 * The timezone that resultant dates should be converted <b>from</b>. All
	 * date fields, parameters, and local variables in this class are in this
	 * timezone, unless they carry the UTC suffix.
	 */
	private final TimeZone tzid;

	/**
	 * Creates the iterator.
	 * @param dtStart the start date of the recurrence
	 * @param tzid the timezone that resultant dates should be converted from
	 * @param condition determines when the recurrence ends
	 * @param instanceGenerator applies the various period generators to
	 * generate an entire date
	 * @param yearGenerator populates each date's year field
	 * @param monthGenerator populates each date's month field
	 * @param dayGenerator populates each date's day field
	 * @param hourGenerator populates each date's hour field
	 * @param minuteGenerator populates each date's minute field
	 * @param secondGenerator populates each date's second field
	 * @param canShortcutAdvance false iff shortcutting advance would break the
	 * semantics of the iteration, true if not
	 */
	RRuleIteratorImpl(DateValue dtStart, TimeZone tzid, Predicate<? super DateValue> condition, Generator instanceGenerator, ThrottledGenerator yearGenerator, Generator monthGenerator, Generator dayGenerator, Generator hourGenerator, Generator minuteGenerator, Generator secondGenerator, boolean canShortcutAdvance) {

		this.condition = condition;
		this.instanceGenerator = instanceGenerator;
		this.yearGenerator = yearGenerator;
		this.monthGenerator = monthGenerator;
		this.dtStart = dtStart;
		this.tzid = tzid;
		this.canShortcutAdvance = canShortcutAdvance;

		int initWorkLimit = 1000;

		/*
		 * Initialize the builder and skip over any extraneous start instances.
		 */
		builder = new DTBuilder(dtStart);

		/*
		 * Apply the generators from largest field to smallest so we can start
		 * by applying the smallest field iterator when asked to generate a
		 * date.
		 */
		try {
			Generator[] toInitialize;
			if (InstanceGenerators.skipSubDayGenerators(hourGenerator, minuteGenerator, secondGenerator)) {
				toInitialize = new Generator[] { yearGenerator, monthGenerator };
				builder.hour = ((SingleValueGenerator) hourGenerator).getValue();
				builder.minute = ((SingleValueGenerator) minuteGenerator).getValue();
				builder.second = ((SingleValueGenerator) secondGenerator).getValue();
			} else {
				toInitialize = new Generator[] { yearGenerator, monthGenerator, dayGenerator, hourGenerator, minuteGenerator, };
			}
			for (int i = 0; i != toInitialize.length;) {
				if (toInitialize[i].generate(builder)) {
					++i;
				} else {
					if (--i < 0) { //no years left
						done = true;
						break;
					}
				}

				if (--initWorkLimit == 0) {
					done = true;
					break;
				}
			}
		} catch (Generator.IteratorShortCircuitingException ex) {
			done = true;
		}

		while (!done) {
			pendingUtc = generateInstance();
			if (pendingUtc == null) {
				done = true;
				break;
			}

			if (pendingUtc.compareTo(TimeUtils.toUtc(dtStart, tzid)) >= 0) {
				/*
				 * We only apply the condition to the ones past dtStart to avoid
				 * counting useless instances.
				 */
				if (!condition.apply(pendingUtc)) {
					done = true;
					pendingUtc = null;
				}
				break;
			}

			if (--initWorkLimit == 0) {
				done = true;
				break;
			}
		}
	}

	public boolean hasNext() {
		if (pendingUtc == null) {
			fetchNext();
		}
		return pendingUtc != null;
	}

	public DateValue next() {
		if (pendingUtc == null) {
			fetchNext();
		}
		DateValue next = pendingUtc;
		pendingUtc = null;
		return next;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void advanceTo(DateValue dateUtc) {
		/*
		 * Don't throw away a future pending date since the iterators will not
		 * generate it again.
		 */
		if (pendingUtc != null && dateUtc.compareTo(pendingUtc) <= 0) {
			return;
		}

		DateValue dateLocal = TimeUtils.fromUtc(dateUtc, tzid);

		//short-circuit if we're already past dateUtc
		if (dateLocal.compareTo(builder.toDate()) <= 0) {
			return;
		}

		pendingUtc = null;

		try {
			if (canShortcutAdvance) {
				//skip years before date.year
				if (builder.year < dateLocal.year()) {
					do {
						if (!yearGenerator.generate(builder)) {
							done = true;
							return;
						}
					} while (builder.year < dateLocal.year());
					while (!monthGenerator.generate(builder)) {
						if (!yearGenerator.generate(builder)) {
							done = true;
							return;
						}
					}
				}
				
				if (builder.month < dateLocal.month()) {
					builder.day = 1;
				}

				//skip months before date.year/date.month
				while (builder.year == dateLocal.year() && builder.month < dateLocal.month()) {
					while (!monthGenerator.generate(builder)) {
						//if there are more years available fetch one
						if (!yearGenerator.generate(builder)) {
							//otherwise the recurrence is exhausted
							done = true;
							return;
						}
					}
				}
			}

			//consume any remaining instances
			while (!done) {
				DateValue dUtc = generateInstance();
				if (dUtc == null) {
					done = true;
					return;
				}

				if (!condition.apply(dUtc)) {
					done = true;
					return;
				}

				if (dUtc.compareTo(dateUtc) >= 0) {
					pendingUtc = dUtc;
					break;
				}
			}
		} catch (Generator.IteratorShortCircuitingException ex) {
			done = true;
		}
	}

	/** calculates and stored the next date in this recurrence. */
	private void fetchNext() {
		if (pendingUtc != null || done) {
			return;
		}

		DateValue dUtc = generateInstance();

		//check the exit condition
		if (dUtc == null || !condition.apply(dUtc)) {
			done = true;
			return;
		}

		pendingUtc = dUtc;
		yearGenerator.workDone();
	}

	private static final DateValue MIN_DATE = new DateValueImpl(Integer.MIN_VALUE, 1, 1);

	/**
	 * Make sure the iterator is monotonically increasing. The local time is
	 * guaranteed to be monotonic, but because of daylight savings shifts, the
	 * time in UTC may not be.
	 */
	private DateValue lastUtc_ = MIN_DATE;

	/**
	 * Generates a date.
	 * @return a date value in UTC or null if a date value could not be
	 * generated
	 */
	private DateValue generateInstance() {
		try {
			do {
				if (!instanceGenerator.generate(builder)) {
					return null;
				}
				DateValue dUtc = dtStart instanceof TimeValue ? TimeUtils.toUtc(builder.toDateTime(), tzid) : builder.toDate();
				if (dUtc.compareTo(lastUtc_) > 0) {
					return dUtc;
				}
			} while (true);
		} catch (Generator.IteratorShortCircuitingException ex) {
			return null;
		}
	}
}
