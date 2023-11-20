package biweekly.util;

import java.util.Calendar;
import java.util.Date;

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
 * Represents a date-time value. Includes extra information that is used within
 * this library.
 * @author Michael Angstadt
 */
public class ICalDate extends Date {
	private static final long serialVersionUID = -8172624513821588097L;

	private final DateTimeComponents rawComponents;
	private final boolean hasTime;

	/**
	 * Creates a new date-time value set to the current date and time.
	 */
	public ICalDate() {
		this(true);
	}

	/**
	 * Creates a new date-time value set to the current date or time.
	 * @param hasTime true to include the time component, false not to
	 */
	public ICalDate(boolean hasTime) {
		this(new Date(), null, hasTime);
	}

	/**
	 * Creates a new date-time value (includes the time component).
	 * @param date the date-time value
	 */
	public ICalDate(Date date) {
		this(date, true);
	}

	/**
	 * Creates a new date-time value.
	 * @param date the date-time value
	 * @param hasTime true to include the time component, false not to
	 */
	public ICalDate(Date date, boolean hasTime) {
		this(date, null, hasTime);
	}

	/**
	 * Creates a new date-time value.
	 * @param date the date-time value components
	 * @param hasTime true to include the time component, false not to
	 */
	public ICalDate(DateTimeComponents date, boolean hasTime) {
		this(date.toDate(), date, hasTime);
	}

	/**
	 * Copies another iCal date-time value.
	 * @param date the date-time value
	 */
	public ICalDate(ICalDate date) {
		this(date, (date.rawComponents == null) ? null : new DateTimeComponents(date.rawComponents), date.hasTime);
	}

	/**
	 * Creates a new date-time value.
	 * @param date the date-time value
	 * @param rawComponents the raw date-time value as parsed from the input
	 * stream
	 * @param hasTime true if the date-time value has a time component, false if
	 * not
	 */
	public ICalDate(Date date, DateTimeComponents rawComponents, boolean hasTime) {
		if (!hasTime) {
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			date = c.getTime();
		}

		setTime(date.getTime());
		this.rawComponents = rawComponents;
		this.hasTime = hasTime;
	}

	/**
	 * Gets the raw date-time components of the value as read from the input
	 * stream.
	 * @return the raw date-time components or null if not set
	 */
	public DateTimeComponents getRawComponents() {
		return rawComponents;
	}

	/**
	 * Gets whether the value contains a time component.
	 * @return true if the value contains a time component, false if it's
	 * strictly a date.
	 */
	public boolean hasTime() {
		return hasTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ICalDate) {
			ICalDate other = (ICalDate) obj;
			if (hasTime != other.hasTime) return false;
		}
		return super.equals(obj);
	}
}
