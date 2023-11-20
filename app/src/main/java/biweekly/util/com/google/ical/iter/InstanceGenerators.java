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
import java.util.List;

import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.com.google.ical.util.DTBuilder;
import biweekly.util.com.google.ical.util.Predicate;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.TimeValue;

/**
 * Factory for generators that operate on groups of generators to generate full
 * dates.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
class InstanceGenerators {
	/**
	 * A collector that yields each date in the period without doing any set
	 * collecting.
	 */
	static Generator serialInstanceGenerator(final Predicate<? super DateValue> filter, final Generator yearGenerator, final Generator monthGenerator, final Generator dayGenerator, final Generator hourGenerator, final Generator minuteGenerator, final Generator secondGenerator) {
		if (skipSubDayGenerators(hourGenerator, minuteGenerator, secondGenerator)) {
			//fast case for generators that are not more frequent than daily
			return new Generator() {
				@Override
				public boolean generate(DTBuilder builder) throws IteratorShortCircuitingException {
					//cascade through periods to compute the next date
					do {
						//until we run out of days in the current month
						while (!dayGenerator.generate(builder)) {
							//until we run out of months in the current year
							while (!monthGenerator.generate(builder)) {
								//if there are more years available fetch one
								if (!yearGenerator.generate(builder)) {
									//otherwise the recurrence is exhausted
									return false;
								}
							}
						}
						//apply filters to generated dates
					} while (!filter.apply(builder.toDateTime()));

					return true;
				}
			};
		} else {
			return new Generator() {
				@Override
				public boolean generate(DTBuilder builder) throws IteratorShortCircuitingException {
					//cascade through periods to compute the next date
					do {
						//until we run out of seconds in the current minute
						while (!secondGenerator.generate(builder)) {
							//until we run out of minutes in the current hour
							while (!minuteGenerator.generate(builder)) {
								//until we run out of hours in the current day
								while (!hourGenerator.generate(builder)) {
									//until we run out of days in the current month
									while (!dayGenerator.generate(builder)) {
										//until we run out of months in the current year
										while (!monthGenerator.generate(builder)) {
											//if there are more years available fetch one
											if (!yearGenerator.generate(builder)) {
												//otherwise the recurrence is exhausted
												return false;
											}
										}
									}
								}
							}
						}
						//apply filters to generated dates
					} while (!filter.apply(builder.toDateTime()));
					//TODO: maybe group the filters into different kinds so we don't
					//apply filters that only affect days to every second.

					return true;
				}
			};
		}
	}

	static Generator bySetPosInstanceGenerator(int[] setPos, final Frequency freq, final DayOfWeek wkst, final Predicate<? super DateValue> filter, final Generator yearGenerator, final Generator monthGenerator, final Generator dayGenerator, final Generator hourGenerator, final Generator minuteGenerator, final Generator secondGenerator) {
		final int[] uSetPos = Util.uniquify(setPos);

		final Generator serialInstanceGenerator = serialInstanceGenerator(filter, yearGenerator, monthGenerator, dayGenerator, hourGenerator, minuteGenerator, secondGenerator);

		//TODO(msamuel): does this work?
		final int maxPos = uSetPos[uSetPos.length - 1];
		final boolean allPositive = uSetPos[0] > 0;

		return new Generator() {
			DateValue pushback = null;

			/**
			 * Is this the first instance we generate? We need to know so that
			 * we don't clobber dtStart.
			 */
			boolean first = true;

			/**
			 * Do we need to halt iteration once the current set has been used?
			 */
			boolean done = false;

			/**
			 * The elements in the current set, filtered by set pos.
			 */
			List<DateValue> candidates;

			/**
			 * Index into candidates. The number of elements in candidates
			 * already consumed.
			 */
			int i;

			@Override
			public boolean generate(DTBuilder builder) throws IteratorShortCircuitingException {
				while (candidates == null || i >= candidates.size()) {
					if (done) {
						return false;
					}

					/*
					 * (1) Make sure that builder is appropriately initialized
					 * so that we only generate instances in the next set.
					 */
					DateValue d0 = null;
					if (pushback != null) {
						d0 = pushback;
						builder.year = d0.year();
						builder.month = d0.month();
						builder.day = d0.day();
						pushback = null;
					} else if (!first) {
						/*
						 * We need to skip ahead to the next item since we
						 * didn't exhaust the last period.
						 */
						switch (freq) {
						case YEARLY:
							if (!yearGenerator.generate(builder)) {
								return false;
							}
							// $FALL-THROUGH$
						case MONTHLY:
							while (!monthGenerator.generate(builder)) {
								if (!yearGenerator.generate(builder)) {
									return false;
								}
							}
							break;
						case WEEKLY:
							//consume because just incrementing date doesn't do anything
							DateValue nextWeek = Util.nextWeekStart(builder.toDateTime(), wkst);
							do {
								if (!serialInstanceGenerator.generate(builder)) {
									return false;
								}
							} while (builder.compareTo(nextWeek) < 0);
							d0 = builder.toDateTime();
							break;
						default:
							break;
						}
					} else {
						first = false;
					}

					/*
					 * (2) Build a set of the dates in the year/month/week that
					 * match the other rule.
					 */
					List<DateValue> dates = new ArrayList<DateValue>();
					if (d0 != null) {
						dates.add(d0);
					}

					/*
					 * Optimization: if min(bySetPos) > 0 then we already have
					 * absolute positions, so we don't need to generate all of
					 * the instances for the period. This speeds up things like
					 * the first weekday of the year:
					 * 
					 * RRULE:FREQ=YEARLY;BYDAY=MO,TU,WE,TH,FR,BYSETPOS=1
					 * 
					 * That would otherwise generate 260+ instances per one
					 * emitted.
					 * 
					 * TODO(msamuel): this may be premature. If needed, We could
					 * improve more generally by inferring a BYMONTH generator
					 * based on distribution of set positions within the year.
					 */
					int limit = allPositive ? maxPos : Integer.MAX_VALUE;

					while (limit > dates.size()) {
						if (!serialInstanceGenerator.generate(builder)) {
							/*
							 * If we can't generate any, then make sure we
							 * return false once the instances we have generated
							 * are exhausted. If this is returning false due to
							 * some artificial limit, such as the 100 year limit
							 * in serialYearGenerator, then we exit via an
							 * exception because otherwise we would pick the
							 * wrong elements for some uSetPoses that contain
							 * negative elements.
							 */
							done = true;
							break;
						}
						DateValue d = builder.toDateTime();
						boolean contained;
						if (d0 == null) {
							d0 = d;
							contained = true;
						} else {
							switch (freq) {
							case WEEKLY:
								int nb = TimeUtils.daysBetween(d, d0);
								/*
								 * Two dates (d, d0) are in the same week if
								 * there isn't a whole week in between them and
								 * the later day is later in the week than the
								 * earlier day.
								 */
								//@formatter:off
			                    contained =
				                    nb < 7 &&
				                    ((7 + TimeUtils.dayOfWeek(d).getCalendarConstant()
				                         - wkst.getCalendarConstant()) % 7) >
				                    ((7 + TimeUtils.dayOfWeek(d0).getCalendarConstant()
				                        - wkst.getCalendarConstant()) % 7);
			                    //@formatter:on
								break;
							case MONTHLY:
								contained = d0.month() == d.month() && d0.year() == d.year();
								break;
							case YEARLY:
								contained = d0.year() == d.year();
								break;
							default:
								done = true;
								return false;
							}
						}
						if (contained) {
							dates.add(d);
						} else {
							//reached end of the set
							pushback = d; //save d so we can use it later
							break;
						}
					}

					/*
					 * (3) Resolve the positions to absolute positions and order
					 * them.
					 */
					int[] absSetPos;
					if (allPositive) {
						absSetPos = uSetPos;
					} else {
						IntSet uAbsSetPos = new IntSet();
						for (int p : uSetPos) {
							if (p < 0) {
								p = dates.size() + p + 1;
							}
							uAbsSetPos.add(p);
						}
						absSetPos = uAbsSetPos.toIntArray();
					}

					candidates = new ArrayList<DateValue>();
					for (int p : absSetPos) {
						if (p >= 1 && p <= dates.size()) { // p is 1-indexed
							candidates.add(dates.get(p - 1));
						}
					}
					i = 0;
					if (candidates.isEmpty()) {
						//none in this region, so keep looking
						candidates = null;
						continue;
					}
				}

				/*
				 * (5) Emit a date. It will be checked against the end condition
				 * and dtStart elsewhere.
				 */
				DateValue d = candidates.get(i++);
				builder.year = d.year();
				builder.month = d.month();
				builder.day = d.day();
				if (d instanceof TimeValue) {
					TimeValue t = (TimeValue) d;
					builder.hour = t.hour();
					builder.minute = t.minute();
					builder.second = t.second();
				}
				return true;
			}
		};
	}

	static boolean skipSubDayGenerators(Generator hourGenerator, Generator minuteGenerator, Generator secondGenerator) {
		return secondGenerator instanceof SingleValueGenerator && minuteGenerator instanceof SingleValueGenerator && hourGenerator instanceof SingleValueGenerator;
	}

	private InstanceGenerators() {
		// uninstantiable
	}
}
