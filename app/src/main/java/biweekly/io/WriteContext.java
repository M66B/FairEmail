package biweekly.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
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
 * Stores information used to write the properties in an iCalendar object.
 * @author Michael Angstadt
 */
public class WriteContext {
	private final ICalVersion version;
	private final TimezoneInfo timezoneOptions;
	private final TimezoneAssignment globalTimezone;
	private final List<Date> dates = new ArrayList<Date>();
	private ICalComponent parent;

	public WriteContext(ICalVersion version, TimezoneInfo timezoneOptions, TimezoneAssignment globalTimezone) {
		this.version = version;
		this.timezoneOptions = timezoneOptions;
		this.globalTimezone = globalTimezone;
	}

	/**
	 * Gets the version of the iCalendar object that is being written.
	 * @return the iCalendar version
	 */
	public ICalVersion getVersion() {
		return version;
	}

	/**
	 * Gets the timezone options for this iCalendar object.
	 * @return the timezone options
	 */
	public TimezoneInfo getTimezoneInfo() {
		return timezoneOptions;
	}

	/**
	 * Gets the global timezone to format all date/time values in, regardless of
	 * each individual {@link ICalendar}'s timezone settings.
	 * @return the global timezone or null if not set
	 */
	public TimezoneAssignment getGlobalTimezone() {
		return globalTimezone;
	}

	/**
	 * Gets the parent component of the property that is being written.
	 * @return the parent component
	 */
	public ICalComponent getParent() {
		return parent;
	}

	/**
	 * Sets the parent component of the property that is being written.
	 * @param parent the parent component
	 */
	public void setParent(ICalComponent parent) {
		this.parent = parent;
	}

	/**
	 * Gets the timezoned date-time property values that are in the iCalendar
	 * object.
	 * @return the timezoned date-time property values
	 */
	public List<Date> getDates() {
		return dates;
	}

	/**
	 * Records the timezoned date-time values that are being written. This is
	 * used to generate a DAYLIGHT property for vCalendar objects.
	 * @param floating true if the date is floating, false if not
	 * @param tz the timezone to format the date in or null for UTC
	 * @param date the date value
	 */
	public void addDate(ICalDate date, boolean floating, TimeZone tz) {
		if (date != null && date.hasTime() && !floating && tz != null) {
			dates.add(date);
		}
	}
}
