package biweekly.io;

import java.util.ArrayList;
import java.util.List;

import biweekly.ICalVersion;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.util.ICalDate;
import biweekly.util.ListMultimap;

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
 * Stores information used during the parsing of an iCalendar object.
 * @author Michael Angstadt
 */
public class ParseContext {
	private ICalVersion version;
	private List<ParseWarning> warnings = new ArrayList<ParseWarning>();
	private ListMultimap<String, TimezonedDate> timezonedDates = new ListMultimap<String, TimezonedDate>();
	private List<TimezonedDate> floatingDates = new ArrayList<TimezonedDate>();
	private Integer lineNumber;
	private String propertyName;

	/**
	 * Gets the version of the iCalendar object being parsed.
	 * @return the iCalendar version
	 */
	public ICalVersion getVersion() {
		return version;
	}

	/**
	 * Sets the version of the iCalendar object being parsed.
	 * @param version the iCalendar version
	 */
	public void setVersion(ICalVersion version) {
		this.version = version;
	}

	/**
	 * Gets the line number the parser is currently on.
	 * @return the line number or null if not applicable
	 */
	public Integer getLineNumber() {
		return lineNumber;
	}

	/**
	 * Sets the line number the parser is currently on.
	 * @param lineNumber the line number or null if not applicable
	 */
	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * Gets the name of the property that the parser is currently parsing.
	 * @return the property name (e.g. "DTSTART") or null if not applicable
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Sets the name of the property that the parser is currently parsing.
	 * @param propertyName the property name (e.g. "DTSTART") or null if not
	 * applicable
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * Adds a parsed date to this parse context so its timezone can be applied
	 * to it after the iCalendar object has been parsed (if it has one).
	 * @param icalDate the parsed date
	 * @param property the property that the date value belongs to
	 * @param parameters the property's parameters
	 */
	public void addDate(ICalDate icalDate, ICalProperty property, ICalParameters parameters) {
		if (!icalDate.hasTime()) {
			//dates don't have timezones
			return;
		}

		if (icalDate.getRawComponents().isUtc()) {
			//it's a UTC date, so it was already parsed under the correct timezone
			return;
		}

		//TODO handle UTC offsets within the date strings (not part of iCal standard)
		String tzid = parameters.getTimezoneId();
		if (tzid == null) {
			addFloatingDate(property, icalDate);
		} else {
			addTimezonedDate(tzid, property, icalDate);
		}
	}

	/**
	 * Keeps track of a date-time property value that uses a timezone so it can
	 * be parsed later. Timezones cannot be handled until the entire iCalendar
	 * object has been parsed.
	 * @param tzid the timezone ID (TZID parameter)
	 * @param property the property
	 * @param date the date object that was assigned to the property object
	 */
	public void addTimezonedDate(String tzid, ICalProperty property, ICalDate date) {
		timezonedDates.put(tzid, new TimezonedDate(date, property));
	}

	/**
	 * Gets the list of date-time property values that use a timezone.
	 * @return the date-time property values that use a timezone (key = TZID;
	 * value = the property)
	 */
	public ListMultimap<String, TimezonedDate> getTimezonedDates() {
		return timezonedDates;
	}

	/**
	 * Keeps track of a date-time property that does not have a timezone
	 * (floating time), so it can be added to the {@link TimezoneInfo} object
	 * after the iCalendar object is parsed.
	 * @param property the property
	 * @param date the property's date value
	 */
	public void addFloatingDate(ICalProperty property, ICalDate date) {
		floatingDates.add(new TimezonedDate(date, property));
	}

	/**
	 * Gets the date-time properties that are in floating time (lacking a
	 * timezone).
	 * @return the floating date-time properties
	 */
	public List<TimezonedDate> getFloatingDates() {
		return floatingDates;
	}

	/**
	 * Adds a parse warning.
	 * @param code the warning code
	 * @param args the warning message arguments
	 */
	public void addWarning(int code, Object... args) {
		//@formatter:off
		warnings.add(new ParseWarning.Builder(this)
			.message(code, args)
		.build());
		//@formatter:on
	}

	/**
	 * Adds a parse warning.
	 * @param message the warning message
	 */
	public void addWarning(String message) {
		//@formatter:off
		warnings.add(new ParseWarning.Builder(this)
			.message(message)
		.build());
		//@formatter:on
	}

	/**
	 * Gets the parse warnings.
	 * @return the parse warnings
	 */
	public List<ParseWarning> getWarnings() {
		return warnings;
	}

	/**
	 * Represents a property whose date-time value has a timezone.
	 * @author Michael Angstadt
	 */
	public static class TimezonedDate {
		private final ICalDate date;
		private final ICalProperty property;

		/**
		 * @param date the date object that was assigned to the property object
		 * @param property the property object
		 */
		public TimezonedDate(ICalDate date, ICalProperty property) {
			this.date = date;
			this.property = property;
		}

		/**
		 * Gets the date object that was assigned to the property object (should
		 * be parsed under the JVM's default timezone)
		 * @return the date object
		 */
		public ICalDate getDate() {
			return date;
		}

		/**
		 * Gets the property object.
		 * @return the property
		 */
		public ICalProperty getProperty() {
			return property;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((date == null) ? 0 : date.hashCode());
			result = prime * result + ((property == null) ? 0 : property.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			TimezonedDate other = (TimezonedDate) obj;
			if (date == null) {
				if (other.date != null) return false;
			} else if (!date.equals(other.date)) return false;
			if (property == null) {
				if (other.property != null) return false;
			} else if (!property.equals(other.property)) return false;
			return true;
		}
	}
}
