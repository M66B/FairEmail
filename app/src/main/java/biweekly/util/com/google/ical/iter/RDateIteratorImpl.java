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
import java.util.NoSuchElementException;

import biweekly.util.com.google.ical.values.DateValue;

/**
 * A recurrence iterator that iterates over an array of dates.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
final class RDateIteratorImpl implements RecurrenceIterator {
	private final DateValue[] datesUtc;
	private int i;

	/**
	 * Creates a new recurrence iterator.
	 * @param datesUtc the dates to iterate over (assumes they are all in UTC)
	 */
	RDateIteratorImpl(DateValue[] datesUtc) {
		datesUtc = datesUtc.clone();
		Arrays.sort(datesUtc);
		this.datesUtc = removeDuplicates(datesUtc);
	}

	public boolean hasNext() {
		return i < datesUtc.length;
	}

	public DateValue next() {
		if (i >= datesUtc.length) {
			throw new NoSuchElementException();
		}
		return datesUtc[i++];
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void advanceTo(DateValue newStartUtc) {
		long startCmp = DateValueComparison.comparable(newStartUtc);
		while (i < datesUtc.length && startCmp > DateValueComparison.comparable(datesUtc[i])) {
			++i;
		}
	}

	/**
	 * Removes duplicates from a list of date values.
	 * @param dates the date values (must be sorted in ascending order)
	 * @return a new array if any elements were removed or the original array if
	 * no elements were removed
	 */
	private static DateValue[] removeDuplicates(DateValue[] dates) {
		int k = 0;
		for (int i = 1; i < dates.length; ++i) {
			if (!dates[i].equals(dates[k])) {
				dates[++k] = dates[i];
			}
		}

		if (++k < dates.length) {
			DateValue[] uniqueDates = new DateValue[k];
			System.arraycopy(dates, 0, uniqueDates, 0, k);
			return uniqueDates;
		}
		return dates;
	}
}
