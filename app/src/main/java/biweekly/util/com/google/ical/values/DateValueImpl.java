/*
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * All Rights Reserved.
 */

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

package biweekly.util.com.google.ical.values;

/**
 * A calendar date.
 * @author Neal Gafter
 * @author Michael Angstadt
 */
public class DateValueImpl implements DateValue {
	private final int year, month, day;

	/**
	 * Creates a new date value.
	 * @param year the year
	 * @param month the month (1-12)
	 * @param day the day (1-31)
	 */
	public DateValueImpl(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public int year() {
		return year;
	}

	public int month() {
		return month;
	}

	public int day() {
		return day;
	}

	@Override
	public String toString() {
		return String.format("%04d%02d%02d", year, month, day);
	}

	public final int compareTo(DateValue other) {
		//@formatter:off
		int n0 = day() +		//5 bits
			(month() << 5) +	//4 bits
			(year() << 9);
		int n1 = other.day() +
			(other.month() << 5) +
			(other.year() << 9);
		//@formatter:on

		if (n0 != n1) {
			return n0 - n1;
		}

		if (!(this instanceof TimeValue)) {
			return (other instanceof TimeValue) ? -1 : 0;
		}

		TimeValue self = (TimeValue) this;
		if (!(other instanceof TimeValue)) {
			return 1;
		}

		TimeValue othr = (TimeValue) other;
		//@formatter:off
		int m0 = self.second() +	//6 bits
			(self.minute() << 6) +	//6 bits
			(self.hour() << 12);
		int m1 = othr.second() +
			(othr.minute() << 6) +
			(othr.hour() << 12);
		//@formatter:on
		return m0 - m1;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DateValue)) {
			return false;
		}
		return compareTo((DateValue) o) == 0;
	}

	@Override
	public int hashCode() {
		return (year() << 9) + (month() << 5) + day();
	}
}
