package biweekly.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import biweekly.ICalVersion;

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
 * Defines the calendar system that this iCalendar object uses for all its date
 * values. If none is specified, then the calendar is assumed to be in
 * "gregorian" format.
 * </p>
 * 
 * <p>
 * <b>Code sample (creating):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar();
 * ical.setCalendarScale(CalendarScale.gregorian());
 * 
 * ical = new ICalendar();
 * ical.setCalendarScale(new CalendarScale("another-calendar-system"));
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = ...
 * CalendarScale calscale = ical.getCalendarscale();
 * 
 * if (calscale.isGregorian()) {
 *   //...
 * } else {
 *   String value = calscale.getValue();
 *   //...
 * }
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-76">RFC 5545 p.76-7</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-73">RFC 2445 p.73-4</a>
 */
public class CalendarScale extends EnumProperty {
	public static final String GREGORIAN = "GREGORIAN";

	/**
	 * Creates a new calendar scale property. Use of this constructor is
	 * discouraged and may put the property in an invalid state. Use one of the
	 * static factory methods instead.
	 * @param value the value of the property (e.g. "gregorian")
	 */
	public CalendarScale(String value) {
		super(value);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public CalendarScale(CalendarScale original) {
		super(original);
	}

	/**
	 * Creates a new property whose value is set to "gregorian".
	 * @return the new property
	 */
	public static CalendarScale gregorian() {
		return new CalendarScale(GREGORIAN);
	}

	/**
	 * Determines whether the property is set to "gregorian".
	 * @return true if it's set to "gregorian", false if not
	 */
	public boolean isGregorian() {
		return is(GREGORIAN);
	}

	@Override
	protected Collection<String> getStandardValues(ICalVersion version) {
		return Collections.singletonList(GREGORIAN);
	}

	@Override
	protected Collection<ICalVersion> getValueSupportedVersions() {
		if (value == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0);
	}

	@Override
	public CalendarScale copy() {
		return new CalendarScale(this);
	}
}
