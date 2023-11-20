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

import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;
import biweekly.util.com.google.ical.values.TimeValue;

/**
 * <p>
 * Contains {@link DateValue} comparison methods.
 * </p>
 * <p>
 * When we're pulling dates off the priority order, we need them to come off in
 * a consistent order, so we need a total ordering on date values.
 * </p>
 * <p>
 * This means that a DateValue with no time must not be equal to a DateTimeValue
 * at midnight. Since it obviously doesn't make sense for a DateValue to be
 * after a DateTimeValue the same day at 23:59:59, we put the DateValue before 0
 * hours of the same day.
 * </p>
 * <p>
 * If we didn't have a total ordering, then it would be harder to correctly
 * handle the example below because we'd have two EXDATEs that are equal
 * according to the comparison, but only the first should match.
 * </p>
 * 
 * <pre>
 *   RDATE:20060607
 *   EXDATE:20060607
 *   EXDATE:20060607T000000Z
 * </pre>
 * <p>
 * In the next example, the problem is worse because we may pull a candidate
 * RDATE off the priority queue and then not know whether to consume the EXDATE
 * or not.
 * </p>
 * 
 * <pre>
 *   RDATE:20060607
 *   RDATE:20060607T000000Z
 *   EXDATE:20060607
 * </pre>
 * <p>
 * Absent a total ordering, the following case could only be solved with
 * lookahead and ugly logic.
 * </p>
 * 
 * <pre>
 *   RDATE:20060607
 *   RDATE:20060607T000000Z
 *   EXDATE:20060607
 *   EXDATE:20060607T000000Z
 * </pre>
 * <p>
 * The conversion to GMT is also an implementation detail, so it's not clear
 * which timezone we should consider midnight in, and a total ordering allows us
 * to avoid timezone conversions during iteration.
 * </p>
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
final class DateValueComparison {
  /**
   * Reduces a date to a value that can be easily compared to others, consistent
   * with {@link DateValueImpl#compareTo}.
   * @param date the date
   * @return the value to use for comparisons
   */
  static long comparable(DateValue date) {
  long comp = (((((long) date.year()) << 4) + date.month()) << 5) + date.day();
    if (date instanceof TimeValue) {
      TimeValue time = (TimeValue) date;

      /*
       * We add 1 to comparable for timed values to make sure that timed events
       * are distinct from all-day events, in keeping with DateValue.compareTo.
       * 
       * It would be odd if an all day exclusion matched a midnight event on the
       * same day, but not one at another time of day.
       */
      return (((((comp << 5) + time.hour()) << 6) + time.minute()) << 6) + time.second() + 1;
    }
    return comp << 17;
  }

  private DateValueComparison() {
    //uninstantiable
  }
}
