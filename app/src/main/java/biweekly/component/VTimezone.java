package biweekly.component;

import java.util.Date;
import java.util.List;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.property.LastModified;
import biweekly.property.TimezoneId;
import biweekly.property.TimezoneUrl;

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
 * Defines a timezone's UTC offsets throughout the year.
 * </p>
 * 
 * <p>
 * <b>Examples:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VTimezone timezone = new VTimezone("Eastern Standard Time");
 * 
 * StandardTime standard = new StandardTime();
 * DateTimeComponents componentsStandard = new DateTimeComponents(1998, 10, 25, 2, 0, 0, false);
 * standard.setDateStart(componentsStandard);
 * standard.setTimezoneOffsetFrom(-4, 0);
 * standard.setTimezoneOffsetTo(-5, 0);
 * timezone.addStandardTime(standard);
 * 
 * DaylightSavingsTime daylight = new DaylightSavingsTime();
 * DateTimeComponents componentsDaylight = new DateTimeComponents(1999, 4, 4, 2, 0, 0, false);
 * daylight.setDateStart(componentsDaylight);
 * daylight.setTimezoneOffsetFrom(-5, 0);
 * daylight.setTimezoneOffsetTo(-4, 0);
 * timezone.addDaylightSavingsTime(daylight);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-62">RFC 5545
 * p.62-71</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-60">RFC 2445 p.60-7</a>
 */
/*
 * Note: References to the vCal 1.0 spec are omitted from the property
 * getter/setter method Javadocs because vCal does not use the VTIMEZONE
 * component.
 */
public class VTimezone extends ICalComponent {
	/**
	 * Creates a new timezone component.
	 * @param identifier a unique identifier for this timezone (allows it to be
	 * referenced by date-time properties that support timezones).
	 */
	public VTimezone(String identifier) {
		setTimezoneId(identifier);
	}

	/**
	 * Copy constructor.
	 * @param original the component to make a copy of
	 */
	public VTimezone(VTimezone original) {
		super(original);
	}

	/**
	 * Gets the ID for this timezone. This is a <b>required</b> property.
	 * @return the timezone ID or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-102">RFC 5545
	 * p.102-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-97">RFC 2445
	 * p.97-8</a>
	 */
	public TimezoneId getTimezoneId() {
		return getProperty(TimezoneId.class);
	}

	/**
	 * Sets an ID for this timezone. This is a <b>required</b> property.
	 * @param timezoneId the timezone ID or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-102">RFC 5545
	 * p.102-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-97">RFC 2445
	 * p.97-8</a>
	 */
	public void setTimezoneId(TimezoneId timezoneId) {
		setProperty(TimezoneId.class, timezoneId);
	}

	/**
	 * Sets an ID for this timezone. This is a <b>required</b> property.
	 * @param timezoneId the timezone ID or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-102">RFC 5545
	 * p.102-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-97">RFC 2445
	 * p.97-8</a>
	 */
	public TimezoneId setTimezoneId(String timezoneId) {
		TimezoneId prop = (timezoneId == null) ? null : new TimezoneId(timezoneId);
		setTimezoneId(prop);
		return prop;
	}

	/**
	 * Gets the date-time that the timezone data was last changed.
	 * @return the last modified date or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
	 * p.138</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
	 * p.131</a>
	 */
	public LastModified getLastModified() {
		return getProperty(LastModified.class);
	}

	/**
	 * Sets the date-time that the timezone data was last changed.
	 * @param lastModified the last modified date or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
	 * p.138</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
	 * p.131</a>
	 */
	public void setLastModified(LastModified lastModified) {
		setProperty(LastModified.class, lastModified);
	}

	/**
	 * Sets the date-time that the timezone data was last changed.
	 * @param lastModified the last modified date or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
	 * p.138</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
	 * p.131</a>
	 */
	public LastModified setLastModified(Date lastModified) {
		LastModified prop = (lastModified == null) ? null : new LastModified(lastModified);
		setLastModified(prop);
		return prop;
	}

	/**
	 * Gets the timezone URL, which points to an iCalendar object that contains
	 * further information on the timezone.
	 * @return the URL or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-106">RFC 5545
	 * p.106</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-101">RFC 2445
	 * p.101</a>
	 */
	public TimezoneUrl getTimezoneUrl() {
		return getProperty(TimezoneUrl.class);
	}

	/**
	 * Sets the timezone URL, which points to an iCalendar object that contains
	 * further information on the timezone.
	 * @param url the URL or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-106">RFC 5545
	 * p.106</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-101">RFC 2445
	 * p.101</a>
	 */
	public void setTimezoneUrl(TimezoneUrl url) {
		setProperty(TimezoneUrl.class, url);
	}

	/**
	 * Sets the timezone URL, which points to an iCalendar object that contains
	 * further information on the timezone.
	 * @param url the timezone URL (e.g.
	 * "http://example.com/America-New_York.ics") or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-106">RFC 5545
	 * p.106</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-101">RFC 2445
	 * p.101</a>
	 */
	public TimezoneUrl setTimezoneUrl(String url) {
		TimezoneUrl prop = (url == null) ? null : new TimezoneUrl(url);
		setTimezoneUrl(prop);
		return prop;
	}

	/**
	 * Gets the timezone's "standard" observance time ranges.
	 * @return the "standard" observance time ranges (any changes made this list
	 * will affect the parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-62">RFC 5545
	 * p.62-71</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-60">RFC 2445
	 * p.60-7</a>
	 */
	public List<StandardTime> getStandardTimes() {
		return getComponents(StandardTime.class);
	}

	/**
	 * Adds a "standard" observance time range.
	 * @param standardTime the "standard" observance time
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-62">RFC 5545
	 * p.62-71</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-60">RFC 2445
	 * p.60-7</a>
	 */
	public void addStandardTime(StandardTime standardTime) {
		addComponent(standardTime);
	}

	/**
	 * Gets the timezone's "daylight savings" observance time ranges.
	 * @return the "daylight savings" observance time ranges (any changes made
	 * this list will affect the parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-62">RFC 5545
	 * p.62-71</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-60">RFC 2445
	 * p.60-7</a>
	 */
	public List<DaylightSavingsTime> getDaylightSavingsTime() {
		return getComponents(DaylightSavingsTime.class);
	}

	/**
	 * Adds a "daylight savings" observance time range.
	 * @param daylightSavingsTime the "daylight savings" observance time
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-62">RFC 5545
	 * p.62-71</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-60">RFC 2445
	 * p.60-7</a>
	 */
	public void addDaylightSavingsTime(DaylightSavingsTime daylightSavingsTime) {
		addComponent(daylightSavingsTime);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (version == ICalVersion.V1_0) {
			warnings.add(new ValidationWarning(48, version));
		}

		checkRequiredCardinality(warnings, TimezoneId.class);
		checkOptionalCardinality(warnings, LastModified.class, TimezoneUrl.class);

		//STANDARD or DAYLIGHT must be defined
		if (getStandardTimes().isEmpty() && getDaylightSavingsTime().isEmpty()) {
			warnings.add(new ValidationWarning(21));
		}
	}

	@Override
	public VTimezone copy() {
		return new VTimezone(this);
	}
}
