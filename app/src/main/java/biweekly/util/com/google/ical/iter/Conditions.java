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

import biweekly.util.com.google.ical.util.Predicate;
import biweekly.util.com.google.ical.values.DateValue;

/**
 * Factory for predicates used to test whether a recurrence is over.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
final class Conditions {
	/**
	 * Constructs a condition that fails after counting a certain number of
	 * dates.
	 * @param count the number of dates to count before the condition fails
	 * @return the condition
	 */
	static Predicate<DateValue> countCondition(final int count) {
		return new Predicate<DateValue>() {
			private static final long serialVersionUID = -3770774958208833665L;
			int count_ = count;

			public boolean apply(DateValue value) {
				return --count_ >= 0;
			}

			@Override
			public String toString() {
				return "CountCondition:" + count_;
			}
		};
	}

	/**
	 * Constructs a condition that passes all dates that are less than or equal
	 * to the given date.
	 * @param until the date
	 * @return the condition
	 */
	static Predicate<DateValue> untilCondition(final DateValue until) {
		return new Predicate<DateValue>() {
			private static final long serialVersionUID = -130394842437801858L;

			public boolean apply(DateValue date) {
				return date.compareTo(until) <= 0;
			}

			@Override
			public String toString() {
				return "UntilCondition:" + until;
			}
		};
	}

	private Conditions() {
		//uninstantiable
	}
}
