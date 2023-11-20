package biweekly.io;

import java.util.TimeZone;

import biweekly.Messages;
import biweekly.component.VTimezone;
import biweekly.property.TimezoneId;
import biweekly.property.ValuedProperty;

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
 * Represents a timezone definition that is used within an iCalendar object.
 * @author Michael Angstadt
 */
public class TimezoneAssignment {
	private final TimeZone timezone;
	private final VTimezone component;
	private final String globalId;

	/**
	 * Creates a timezone that will be inserted into the iCalendar object as a
	 * VTIMEZONE component. This what most iCalendar files use.
	 * @param timezone the Java timezone object
	 * @param component the iCalendar component
	 * @throws IllegalArgumentException if the given {@link VTimezone} component
	 * doesn't have a {@link TimezoneId} property
	 */
	public TimezoneAssignment(TimeZone timezone, VTimezone component) {
		String id = ValuedProperty.getValue(component.getTimezoneId());
		if (id == null || id.trim().isEmpty()) {
			throw Messages.INSTANCE.getIllegalArgumentException(14);
		}

		this.timezone = timezone;
		this.component = component;
		this.globalId = null;
	}

	/**
	 * <p>
	 * Creates a timezone that will be inserted into the iCalendar object as a
	 * "global ID". This means that a {@link VTimezone} component containing the
	 * timezone definition will NOT be inserted into the iCalendar object.
	 * </p>
	 * <p>
	 * Because the timezone definition is not included inside of the iCalendar
	 * object, the client consuming the iCalendar object must know how to
	 * interpret such an ID. The iCalendar specification does not specify a list
	 * of such IDs, but suggests using the naming convention of an existing
	 * timezone specification, such as the
	 * <a href="http://www.twinsun.com/tz/tz-link.htm">public-domain TZ
	 * database</a>.
	 * </p>
	 * @param timezone the Java timezone object
	 * @param globalId the global ID (e.g. "America/New_York")
	 */
	public TimezoneAssignment(TimeZone timezone, String globalId) {
		this.timezone = timezone;
		this.component = null;
		this.globalId = globalId;
	}

	/**
	 * Creates a timezone whose VTIMEZONE component is downloaded from
	 * <a href="http://www.tzurl.org">tzurl.org</a>.
	 * @param timezone the Java timezone object
	 * @param outlookCompatible true to download a {@link VTimezone} component
	 * that is tailored for Microsoft Outlook email clients, false to download a
	 * standards-based one.
	 * @return the timezone assignment
	 * @throws IllegalArgumentException if an appropriate VTIMEZONE component
	 * cannot be found on the website
	 */
	public static TimezoneAssignment download(TimeZone timezone, boolean outlookCompatible) {
		TzUrlDotOrgGenerator generator = new TzUrlDotOrgGenerator(outlookCompatible);
		VTimezone component = generator.generate(timezone);
		return new TimezoneAssignment(timezone, component);
	}

	/**
	 * Gets the Java object associated with the timezone.
	 * @return the Java object
	 */
	public TimeZone getTimeZone() {
		return timezone;
	}

	/**
	 * Gets the iCalendar component associated with the timezone.
	 * @return the component or null if the timezone uses a global ID
	 */
	public VTimezone getComponent() {
		return component;
	}

	/**
	 * Gets the global ID associated with the timezone.
	 * @return the global ID or null if the timezone uses an iCalendar component
	 */
	public String getGlobalId() {
		return globalId;
	}
}
