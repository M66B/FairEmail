package biweekly.property;

import java.util.Date;

import biweekly.parameter.Range;
import biweekly.util.ICalDate;

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
 * <p>
 * Records the original value of the {@link DateStart} property if a recurrence
 * instance has been modified. It is used in conjunction with the {@link Uid}
 * and {@link Sequence} properties to uniquely identify a recurrence instance.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * //date-time value
 * Date datetime = ...
 * RecurrenceId recurrenceId = new RecurrenceId(datetime);
 * event.setRecurrenceId(recurrenceId);
 * 
 * //date value
 * Date date = ...
 * RecurrenceId recurrenceId = new RecurrenceId(date, false);
 * event.setRecurrenceId(recurrenceId);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-112">RFC 5545
 * p.112-4</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-107">RFC 2445
 * p.107-9</a>
 */
public class RecurrenceId extends DateOrDateTimeProperty {
	/**
	 * Creates a recurrence ID property.
	 * @param originalStartDate the original start date
	 */
	public RecurrenceId(ICalDate originalStartDate) {
		super(originalStartDate);
	}

	/**
	 * Creates a recurrence ID property.
	 * @param originalStartDate the original start date
	 */
	public RecurrenceId(Date originalStartDate) {
		super(originalStartDate);
	}

	/**
	 * Creates a recurrence ID property.
	 * @param originalStartDate the original start date
	 * @param hasTime true to include the time component of the date, false not
	 * to
	 */
	public RecurrenceId(Date originalStartDate, boolean hasTime) {
		super(originalStartDate, hasTime);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public RecurrenceId(RecurrenceId original) {
		super(original);
	}

	/**
	 * Gets the effective range of recurrence instances from the instance
	 * specified by this property.
	 * @return the range or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-23">RFC 5545
	 * p.23-4</a>
	 */
	public Range getRange() {
		return parameters.getRange();
	}

	/**
	 * Sets the effective range of recurrence instances from the instance
	 * specified by this property.
	 * @param range the range or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-23">RFC 5545
	 * p.23-4</a>
	 */
	public void setRange(Range range) {
		parameters.setRange(range);
	}

	@Override
	public RecurrenceId copy() {
		return new RecurrenceId(this);
	}
}
