package biweekly.property;

import java.util.Date;

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
 * Represents a property whose value is a date or a date-time.
 * @author Michael Angstadt
 */
public class DateOrDateTimeProperty extends ValuedProperty<ICalDate> {
	/**
	 * Creates a new property.
	 * @param value the date-time value
	 */
	public DateOrDateTimeProperty(ICalDate value) {
		super(value);
	}

	/**
	 * Creates a new property.
	 * @param value the date-time value
	 */
	public DateOrDateTimeProperty(Date value) {
		this(value, true);
	}

	/**
	 * Creates a new property.
	 * @param value the date-time value
	 * @param hasTime true if the value has a time component, false if it is
	 * strictly a date
	 */
	public DateOrDateTimeProperty(Date value, boolean hasTime) {
		this(createICalDate(value, hasTime));
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public DateOrDateTimeProperty(DateOrDateTimeProperty original) {
		super(original);
		value = (original.value == null) ? null : new ICalDate(original.value);
	}

	/**
	 * Sets the date-time value.
	 * @param value the date-time value
	 * @param hasTime true if the value has a time component, false if it is
	 * strictly a date
	 */
	public void setValue(Date value, boolean hasTime) {
		setValue((value == null) ? null : new ICalDate(value, hasTime));
	}

	private static ICalDate createICalDate(Date value, boolean hasTime) {
		if (value == null) {
			return null;
		}
		return (value instanceof ICalDate) ? (ICalDate) value : new ICalDate(value, hasTime);
	}
}
